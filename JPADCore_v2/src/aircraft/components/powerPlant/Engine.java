package aircraft.components.powerPlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.componentmodel.Component;
import aircraft.components.Aircraft;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;

public class Engine extends Component{

	private String _id;
	private static int idCounter = 0;
	public static final ComponentEnum type = ComponentEnum.ENGINE;
	private EngineTypeEnum _engineType;
	private Amount<Length> _X0, _Y0, _Z0;
	private EngineMountingPositionEnum _mountingPoint;
	private Amount<Power> _p0;
	private Amount<Force> _t0;
	private Amount<Mass> 
	_dryMass = null, 
	_dryMassPublicDomain = null, 
	_totalMass = null;

	private CenterOfGravity _cg;
	private Amount<Length> _length;
	private Double _bpr;
	private int _numberOfCompressorStages, _numberOfShafts; 
	private double _overallPressureRatio;
	private Amount<Velocity> _v0;
	private Amount<Angle> _muT;
	private ArrayList<MethodEnum> _methodsList;
	private Double[] _percentDifference;
	private Amount<Length> _xCG;

	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Double[] _percentDifferenceXCG;
	
	private Aircraft _theAircraft;
	private EngWeightsManager weights;
	private EngBalanceManager balance;
	// ---------------------------
	private double _SFC;
	// ---------------------------
	
	public Engine(String name, String description, double x, double y,double z){

		super("", name, description, x, y, z);

		_name = name;
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

	}
	
	public Engine(String name, String description, 
			double x, double y,double z, 
			Aircraft aircraft) {
		
		this(name, description, x, y, z);
		_theAircraft = aircraft;
		
		_id = aircraft.getPowerPlant().getId() + idCounter + "99";
		idCounter++;
		
		initialize();
	}
	
	/**
	 * Overload of the previous builder that recognize aircraft name and generates it's default engine.
	 * 
	 * @author Vittorio Trifari
	 */
	public Engine(AircraftEnum aircraftName, String name, String description, 
			double x, double y,double z, 
			Aircraft aircraft) {
		
		this(name, description, x, y, z);
		_theAircraft = aircraft;
		
		_id = aircraft.getPowerPlant().getId() + idCounter + "99";
		idCounter++;
		
		initialize(aircraftName);
	}
	
	public void initialize() {
		
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		// PW127 Data
		_engineType = EngineTypeEnum.TURBOPROP;

		_length = Amount.valueOf(2.13, SI.METER);

		// By-pass ratio
		set_bpr(0.0);
		
		_numberOfCompressorStages = 5;
		_numberOfShafts = 2;
		_overallPressureRatio = 15.;

		// Reference dry engine mass (from public domain data)
		_dryMassPublicDomain = Amount.valueOf(1064., NonSI.POUND).to(SI.KILOGRAM);

		_t0 = Amount.valueOf(8000., SI.NEWTON);
		
		// Single engine maximum power output (from public domain data)
		_p0 = Amount.valueOf(2160., NonSI.HORSEPOWER).to(SI.WATT);

		// Reference speed at take-off
		_v0 = Amount.valueOf(5., SI.METERS_PER_SECOND);

		/** Reference total engine mass (dry + something) */
		_totalMass = Amount.valueOf(1557.8, SI.KILOGRAM);

		_muT = Amount.valueOf(0., SI.RADIAN);

		// Engine position
		_mountingPoint = EngineMountingPositionEnum.WING;
		
		weights = new EngWeightsManager(_theAircraft, this);
		balance = new EngBalanceManager(this);
		
	}
	
	/**
	 * Overload of the default initializer that recognize aircraft name and initialize 
	 * the correct engine.
	 * 
	 * @author Vittorio Trifari
	 */
	public void initialize(AircraftEnum aircraftName) {
		
		switch(aircraftName) {
		
		case ATR72:
			_cg = new CenterOfGravity(_X0, _Y0, _Z0);

			// PW127 Data
			_engineType = EngineTypeEnum.TURBOPROP;

			_length = Amount.valueOf(2.13, SI.METER);

			// By-pass ratio
			set_bpr(0.0);
			
			_numberOfCompressorStages = 5;
			_numberOfShafts = 2;
			_overallPressureRatio = 15.;

			// Reference dry engine mass (from public domain data)
			_dryMassPublicDomain = Amount.valueOf(1064., NonSI.POUND).to(SI.KILOGRAM);

			_t0 = Amount.valueOf(7700.0, NonSI.POUND_FORCE).to(SI.NEWTON);
			
			// Single engine maximum power output (from public domain data)
			_p0 = Amount.valueOf(2750., NonSI.HORSEPOWER).to(SI.WATT);

			// Reference speed at take-off
			_v0 = Amount.valueOf(5., SI.METERS_PER_SECOND);

			/** Reference total engine mass (dry + something) */
			_totalMass = Amount.valueOf(1557.8, SI.KILOGRAM);

			_muT = Amount.valueOf(0., SI.RADIAN);

			// Engine position
			_mountingPoint = EngineMountingPositionEnum.WING;
			
			weights = new EngWeightsManager(_theAircraft, this);
			balance = new EngBalanceManager(this);
			break;
			
		case B747_100B:
			_cg = new CenterOfGravity(_X0, _Y0, _Z0);

			// PWJT9D-7 Data
			_engineType = EngineTypeEnum.TURBOFAN;

			_length = Amount.valueOf(3.26, SI.METER);

			// By-pass ratio
			set_bpr(5.0);
			
			_numberOfCompressorStages = 14;
			_numberOfShafts = 2;
			_overallPressureRatio = 23.4;

			// Reference dry engine mass (from public domain data)
			_dryMassPublicDomain = Amount.valueOf(3905.0, NonSI.POUND).to(SI.KILOGRAM);

//			_t0 = Amount.valueOf(261185.0000, SI.NEWTON);
			_t0 = Amount.valueOf(204000.0000, SI.NEWTON);
			
			// Single engine maximum power output (from public domain data)
			_p0 = Amount.valueOf(1588677.8400, NonSI.HORSEPOWER).to(SI.WATT);

			// Reference speed at take-off
			_v0 = Amount.valueOf(5., SI.METERS_PER_SECOND);

			/** Reference total engine mass (dry + something) */
			_totalMass = Amount.valueOf(4010., SI.KILOGRAM);

			_muT = Amount.valueOf(0., SI.RADIAN);

			// Engine position
			_mountingPoint = EngineMountingPositionEnum.WING;
			
			weights = new EngWeightsManager(_theAircraft, this);
			balance = new EngBalanceManager(this);
			break;
			
		case AGILE_DC1:
			//PW1700G
			_cg = new CenterOfGravity(_X0, _Y0, _Z0);

			_engineType = EngineTypeEnum.TURBOFAN;

			_length = Amount.valueOf(2.739, SI.METER);

			// By-pass ratio
			set_bpr(6.0);
			
			_numberOfCompressorStages = 5; // TODO: CHECK
			_numberOfShafts = 2;// TODO: CHECK
			_overallPressureRatio = 15.;// TODO: CHECK

			// Reference dry engine mass (from public domain data)
			_dryMassPublicDomain = Amount.valueOf(1162.6, NonSI.POUND).to(SI.KILOGRAM);

			_t0 = Amount.valueOf(7000*AtmosphereCalc.g0.getEstimatedValue(), SI.NEWTON);
			
			// Single engine maximum power output (from public domain data)
			_p0 = Amount.valueOf(80397.37218, NonSI.HORSEPOWER).to(SI.WATT); // TODO: check

			// Reference speed at take-off
			_v0 = Amount.valueOf(5., SI.METERS_PER_SECOND);

			/** Reference total engine mass (dry + something) */
			_totalMass = Amount.valueOf(1162.6, SI.KILOGRAM);

			_muT = Amount.valueOf(0., SI.RADIAN);

			// Engine position
			_mountingPoint = EngineMountingPositionEnum.WING;
			
			weights = new EngWeightsManager(_theAircraft, this);
			balance = new EngBalanceManager(this);
			break;
		}
	}

	public Amount<Length> get_X0() {
		return _X0;
	}

	public void set_X0(Amount<Length> _X0) {
		this._X0 = _X0;
	}

	public Amount<Length> get_Y0() {
		return _Y0;
	}

	public void set_Y0(Amount<Length> _Y0) {
		this._Y0 = _Y0;
	}

	public Amount<Length> get_Z0() {
		return _Z0;
	}

	public void set_Z0(Amount<Length> _Z0) {
		this._Z0 = _Z0;
	}

	public EngineMountingPositionEnum get_mountingPoint() {
		return _mountingPoint;
	}

	public void set_mountingPoint(EngineMountingPositionEnum _mountingPoint) {
		this._mountingPoint = _mountingPoint;
	}

	public Amount<Power> get_p0() {
		return _p0;
	}

	public void set_p0(Amount<Power> _p0) {
		this._p0 = _p0;
	}

	public Amount<Force> get_t0() {
		return _t0;
	}

	public void set_t0(Amount<Force> _t0) {
		this._t0 = _t0;
	}
	
	// ----------------------------------
	// TODO: implement a method for SFC
	public double getSFC(){
		return _SFC;
	}
	
	public double setSFC(double SFC){
		this._SFC = SFC;
	}
	
	// ----------------------------------
	public Amount<Mass> get_dryMass() {
		return _dryMass;
	}

	public void set_dryMass(Amount<Mass> _dryMass) {
		this._dryMass = _dryMass;
	}

	public Amount<Mass> get_totalMass() {
		return _totalMass;
	}

	public void set_totalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}

	public EngineTypeEnum get_engineType() {
		return _engineType;
	}

	public void set_engineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
	}

	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}

	public Double[] get_percentDifference() {
		return _percentDifference;
	}

	public Amount<Angle> get_muT() {
		return _muT;
	}

	public void set_muT(Amount<Angle> _muT) {
		this._muT = _muT;
	}

	public Double get_bpr() {
		return _bpr;
	}

	public void set_bpr(Double _BPR) {
		this._bpr = _BPR;
	}

	public Amount<Mass> get_dryMassPublicDomain() {
		return _dryMassPublicDomain;
	}

	public EngWeightsManager getWeights() {
		return weights;
	}

	public Amount<Length> get_length() {
		return _length;
	}

	public void set_length(Amount<Length> _length) {
		this._length = _length;
	}

	public EngBalanceManager getBalance() {
		return balance;
	}

	public String get_id() {
		return _id;
	}

	public int get_numberOfCompressorStages() {
		return _numberOfCompressorStages;
	}

	public void set_numberOfCompressorStages(int _numberOfCompressorStages) {
		this._numberOfCompressorStages = _numberOfCompressorStages;
	}

	public int get_numberOfShafts() {
		return _numberOfShafts;
	}

	public void set_numberOfShafts(int _numberOfShafts) {
		this._numberOfShafts = _numberOfShafts;
	}

	public double get_overallPressureRatio() {
		return _overallPressureRatio;
	}

	public void set_overallPressureRatio(double _overallPressureRatio) {
		this._overallPressureRatio = _overallPressureRatio;
	}



}
