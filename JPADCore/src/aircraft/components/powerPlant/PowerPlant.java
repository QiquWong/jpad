package aircraft.components.powerPlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Frequency;
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
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * The Propulsion System includes engines, engine exhaust, 
 * reverser, starting, controls, lubricating, and fuel systems.
 * The output of this class is the entire propulsion system (that
 * is, all engines are included) 
 */

//TODO: this class should not extend MyComponent
public class PowerPlant extends Component{

	public static final String _id = "8";
	private String _name;
	private Amount<Length> _X0, _Y0, _Z0,
	_length;


	private List<CenterOfGravity> _cgList = new ArrayList<CenterOfGravity>();
	public static final List<Engine> engineList = new ArrayList<Engine>();
	private EngineTypeEnum _engineType;
	private Amount<Power> _P0Total;
	private Amount<Force> _T0Total;
	private Integer _engineNumber;
	private Amount<Length> _diameter;
	private Amount<Frequency> _rpm, _rps;
	private Amount<Velocity> _v0;

	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Double[] _percentDifference;
	private Amount<Mass> 
	_massDryEngine, _massDryEngineEstimated,
	_massDryEngineActual, _massDryEngineActualTotal, 
	_totalMass, _dryMassPublicDomain, _totalEngineMass,
	_massDryEngineTotalEstimated;

	private Double _percentTotalDifference;
	private CenterOfGravity _cg;

	private EngineMountingPositionEnum _position;
	private Double[] _percentDifferenceXCG;
	private Amount<Length> _xCG;
	private Amount<Angle> _muT;

	/** Check if engines are all the same */
	private Boolean _engineEqual = false;
	private Amount<Mass> _massReference;
	private Aircraft _theAircraft;
	private CenterOfGravity _totalCG;

	private double etaEfficiency;
	private double nBlade;
	Amount<Length> fanDiameter;

	public PowerPlant(String name, String description, double x, double y,double z){

		super(getId(), name, description, x, y, z);

		_name = name;
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

		initialize();
	}

	public PowerPlant(String name, String description, 
			double x, double y,double z,
			Aircraft aircraft) {

		this(name, description, x, y, z);
		_theAircraft = aircraft;
		initializeEngines();
	}
	
	/**
	 * Overload of the previous builder that recognize aircraft name and initialize engines with it's data.
	 * 
	 * @author Vittorio Trifari
	 */
	public PowerPlant(AircraftEnum aircraftName, String name, String description, 
			double x, double y,double z,
			Aircraft aircraft) {

		this(name, description, x, y, z);
		_theAircraft = aircraft;
		initializeEngines(aircraftName);
	}

	public void initialize() {
		_cg = new CenterOfGravity(_X0, _Y0, _Z0);

		// PW127 Data
		_engineNumber = 2;
		_engineType = EngineTypeEnum.TURBOPROP;

		_length = Amount.valueOf(2.13, SI.METER);

		// Reference dry engine mass (from public domain data)
		_massDryEngineActual = Amount.valueOf(1064, NonSI.POUND).to(SI.KILOGRAM);

		// Reference speed at take-off
		_v0 = Amount.valueOf(5, SI.METERS_PER_SECOND);

		// Reference total power plant mass
		_dryMassPublicDomain = Amount.valueOf(1557.8, SI.KILOGRAM);

		_muT = Amount.valueOf(0., SI.RADIAN);

		// Engine position
		_position = EngineMountingPositionEnum.WING;

	}

	//TODO check the behaviour when reading engines from file
	public void initializeEngines() {
		for (int i=0; i < _engineNumber; i++) {
			engineList.add(new Engine("Engine_" + i, "", 0.0, 0.0, 0.0, _theAircraft));
		}
	}
	
	/**
	 * Overload of the default initializer that recognize aircraft name and generates 
	 * an Engine with the specified aircraft data.
	 * 
	 * @author Vittorio Trifari
	 */
	public void initializeEngines(AircraftEnum aircraftName) {
		
		switch(aircraftName) {
		case ATR72:
			_engineNumber = 2;
			_engineType = EngineTypeEnum.TURBOPROP;
				engineList.add(new Engine(aircraftName, "Engine_1", "",8.6100, 4.0500, 1.3200, _theAircraft));
				engineList.add(new Engine(aircraftName, "Engine_2", "",8.6100, -4.0500, 1.3200, _theAircraft));
			break;
		case B747_100B:
			_engineNumber = 4;
			_engineType = EngineTypeEnum.TURBOFAN;
				engineList.add(new Engine(aircraftName, "Engine_1" , "", 23.770, 11.820, -2.462, _theAircraft));
				engineList.add(new Engine(aircraftName, "Engine_1" , "", 31.693, 21.951, -2.462, _theAircraft));
				engineList.add(new Engine(aircraftName, "Engine_1" , "", 23.770, -11.820, -2.462, _theAircraft));
				engineList.add(new Engine(aircraftName, "Engine_1" , "", 31.693, -21.951, -2.462, _theAircraft));
				
			
			break;
			
		case AGILE_DC1:
			_engineNumber = 2;
			_engineType = EngineTypeEnum.TURBOFAN;
//			for (int i=0; i < _engineNumber; i++) {
//				engineList.add(new Engine(aircraftName, "Engine_" + i, "", 0.0, 0.0, 0.0, _theAircraft));
			engineList.add(new Engine(aircraftName, "Engine_1", "", 12.891, 4.869, -1.782, _theAircraft));
			engineList.add(new Engine(aircraftName, "Engine_2", "", 12.891, -4.869, -1.782, _theAircraft));
//			}
			break;
		}
	}

	public void calculateDerivedVariables() {

		_massDryEngineActualTotal = _massDryEngineActual.times(_engineNumber);
		_T0Total = Amount.valueOf(0., SI.NEWTON);
		_P0Total = Amount.valueOf(0., SI.WATT);

		//		if (_engineType==MyEngineTypeEnum.TURBOPROP || _engineType==MyEngineTypeEnum.PISTON) {
		//			_rpm = Amount.valueOf(2100, MyUnits.RPM);
		//			_rps = _rpm.to(SI.HERTZ);
		//			_diameter = Amount.valueOf(3.93, SI.METER);
		//			_J = _v0.divide(_diameter.times(_rps)).getEstimatedValue();
		//
		//			_eta = 0.75;
		//			_cP = _P0.divide((conditions.get_densityCurrent().times(_rps.pow(3)).times(_diameter.pow(5)))).getEstimatedValue();
		//			
		//			//TODO: need for a more accurate estimation of Ct for v--->0
		//			//		_cT = _eta*_cP/_J;
		//			_cT = 0.12;
		//
		//			_T0 = Amount.valueOf(conditions.get_densityCurrent().times(_cT).times(_rps.pow(2)).times(_diameter.pow(4)).getEstimatedValue(),
		//					SI.NEWTON);
		//		}

		for(int i=0; i < _engineNumber; i++) {
			_T0Total = _T0Total.plus(engineList.get(i).get_t0());
			_P0Total = _P0Total.plus(engineList.get(i).get_p0());
		}

	}


	@Override
	public void calculateMass() {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_dryMassPublicDomain = Amount.valueOf(0., SI.KILOGRAM);

		for(int i=0; i < _engineNumber; i++) {
			engineList.get(i).getWeights().calculateAll();
			_totalMass = _totalMass.plus(engineList.get(i).get_totalMass());
			_dryMassPublicDomain = _dryMassPublicDomain.plus(engineList.get(i).getWeights().get_dryMassPublicDomain());
		}

		_percentTotalDifference = _totalMass.
				minus(_dryMassPublicDomain).
				divide(_dryMassPublicDomain).
				getEstimatedValue()*100.;
	}


	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();		
		for(int i=0; i < _engineNumber; i++) {
			engineList.get(i).getBalance().calculateAll();
			_cgList.add(engineList.get(i).getBalance().get_cg());
			_totalCG = _totalCG.plus(engineList.get(i).getBalance().get_cg()
					.times(engineList.get(i).get_totalMass().doubleValue(SI.KILOGRAM)));
		}
		
		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	public Amount<Mass> get_massDryEngineActual() {
		return _massDryEngineActual;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._massDryEngine = _mass;
	}

	public Amount<Power> get_P0Total() {
		return _P0Total;
	}

	public void set_P0Total(Amount<Power> _P0) {
		this._P0Total = _P0;
	}

	public Amount<Force> get_T0Total() {
		return _T0Total;
	}

	public void set_T0Total(Amount<Force> _T0Total) {
		this._T0Total = _T0Total;
	}

	public Integer get_engineNumber() {
		return _engineNumber;
	}

	public void set_engineNumber(Integer _engineNumber) {
		this._engineNumber = _engineNumber;
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


	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public void set_massDryEngine(Amount<Mass> _massDryEngine) {
		this._massDryEngine = _massDryEngine;
	}


	public Amount<Mass> get_totalMass() {
		return _totalMass;
	}


	public Amount<Mass> get_dryMassPublicDomain() {
		return _dryMassPublicDomain;
	}


	public void set_totalPowerPlantMassActual(
			Amount<Mass> _totalPowerPlantMassReference) {
		this._dryMassPublicDomain = _totalPowerPlantMassReference;
	}


	public Double get_percentTotalDifference() {
		return _percentTotalDifference;
	}


	@Override
	public Amount<Length> get_X0() { return _X0; }

	@Override
	public void set_X0(Amount<Length> x) { _X0 = x; };

	@Override
	public Amount<Length> get_Y0() { return _Y0; }

	@Override
	public void set_Y0(Amount<Length> y) { _Y0 = y; };

	@Override
	public Amount<Length> get_Z0() { return _Z0; }

	@Override
	public void set_Z0(Amount<Length> z) { _Z0 = z; }


	public Amount<Mass> get_massDryEngineActualTotal() {
		return _massDryEngineActualTotal;
	}


	public Amount<Mass> get_massDryEngine() {
		return _massDryEngine;
	}


	public Amount<Mass> get_massDryEngineEstimated() {
		return _massDryEngineEstimated;
	}


	public EngineMountingPositionEnum get_position() {
		return _position;
	}


	public void set_position(EngineMountingPositionEnum _position) {
		this._position = _position;
	}


	public String get_name() {
		return _name;
	}


	public void set_name(String _name) {
		this._name = _name;
	}


	public Amount<Length> get_length() {
		return _length;
	}


	public void set_length(Amount<Length> _length) {
		this._length = _length;
	}


	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}


	public void set_xCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}


	public Map<MethodEnum, Amount<Length>> get_yCGMap() {
		return _yCGMap;
	}


	public void set_yCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}


	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}


	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}


	public Amount<Angle> get_muT() {
		return _muT;
	}


	public void set_muT(Amount<Angle> _muT) {
		this._muT = _muT;
	}

	public static String getId() {
		return "8";
	}

	public List<Engine> get_engineList() {
		return engineList;
	}

	public Boolean is_engineEqual() {
		return _engineEqual;
	}

	public void set_engineEqual(Boolean _engineEqual) {
		this._engineEqual = _engineEqual;
	}

	public CenterOfGravity get_totalCG() {
		return _totalCG;
	}

	public List<CenterOfGravity> get_cgList() {
		return _cgList;
	}

	public void set_cgList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public double getEtaEfficiency() {
		return etaEfficiency;
	}

	public void setEtaEfficiency(double etaEfficiency) {
		this.etaEfficiency = etaEfficiency;
	}

	public double getnBlade() {
		return nBlade;
	}

	public void setnBlade(double nBlade) {
		this.nBlade = nBlade;
	}

	public Amount<Length> getFanDiameter() {
		return fanDiameter;
	}

	public void setFanDiameter(Amount<Length> fanDiameter) {
		this.fanDiameter = fanDiameter;
	}

	
}
