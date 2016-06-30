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

import aircraft.components.Aircraft;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;

public class Engine {

	private String _id;
	private EngineTypeEnum _engineType;
	private EngineMountingPositionEnum _mountingPoint;
	
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private static int idCounter = 0;
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
	private EngineWeightsManager weights;
	private EngineBalanceManager balance;
	// ---------------------------
	private double _SFC;
	// ---------------------------
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	
	/**
	 * Overload of the previous builder that recognize aircraft name and generates it's default engine.
	 * 
	 * @author Vittorio Trifari
	 */
	public Engine(AircraftEnum aircraftName, String name, Aircraft aircraft) {
		
		_theAircraft = aircraft;
		
		_id = aircraft.getPowerPlant().getId() + idCounter + "99";
		idCounter++;
		
		initialize(aircraftName);
	}
	
	public void initialize() {
		
		_cg = new CenterOfGravity(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);

		// PW127 Data
		_engineType = EngineTypeEnum.TURBOPROP;

		_length = Amount.valueOf(2.13, SI.METER);

		// By-pass ratio
		setBPR(0.0);
		
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
		
		weights = new EngineWeightsManager(_theAircraft, this);
		balance = new EngineBalanceManager(this);
		
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
			_cg = new CenterOfGravity(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);

			// PW127 Data
			_engineType = EngineTypeEnum.TURBOPROP;

			_length = Amount.valueOf(2.13, SI.METER);

			// By-pass ratio
			setBPR(0.0);
			
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
			
			weights = new EngineWeightsManager(_theAircraft, this);
			balance = new EngineBalanceManager(this);
			break;
			
		case B747_100B:
			_cg = new CenterOfGravity(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);

			// PWJT9D-7 Data
			_engineType = EngineTypeEnum.TURBOFAN;

			_length = Amount.valueOf(3.26, SI.METER);

			// By-pass ratio
			setBPR(5.0);
			
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
			
			weights = new EngineWeightsManager(_theAircraft, this);
			balance = new EngineBalanceManager(this);
			break;
			
		case AGILE_DC1:
			//PW1700G
			_cg = new CenterOfGravity(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);

			_engineType = EngineTypeEnum.TURBOFAN;

			_length = Amount.valueOf(2.739, SI.METER);

			// By-pass ratio
			setBPR(6.0);
			
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
			
			weights = new EngineWeightsManager(_theAircraft, this);
			balance = new EngineBalanceManager(this);
			break;
		}
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _X0) {
		this._xApexConstructionAxes = _X0;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _Y0) {
		this._yApexConstructionAxes = _Y0;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _Z0) {
		this._zApexConstructionAxes = _Z0;
	}

	public EngineMountingPositionEnum getMountingPoint() {
		return _mountingPoint;
	}

	public void setMountingPoint(EngineMountingPositionEnum _mountingPoint) {
		this._mountingPoint = _mountingPoint;
	}

	public Amount<Power> getP0() {
		return _p0;
	}

	public void setP0(Amount<Power> _p0) {
		this._p0 = _p0;
	}

	public Amount<Force> getT0() {
		return _t0;
	}

	public void setT0(Amount<Force> _t0) {
		this._t0 = _t0;
	}
	
	// ----------------------------------
	// TODO: implement a method for SFC
	public double getSFC(){
		return _SFC;
	}
	
	public void setSFC(double SFC){
		this._SFC = SFC;
	}
	
	// ----------------------------------
	public Amount<Mass> getDryMass() {
		return _dryMass;
	}

	public void setDryMass(Amount<Mass> _dryMass) {
		this._dryMass = _dryMass;
	}

	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}

	public void setTotalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}

	public EngineTypeEnum getEngineType() {
		return _engineType;
	}

	public void setEngineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	public Amount<Angle> getMuT() {
		return _muT;
	}

	public void setMuT(Amount<Angle> _muT) {
		this._muT = _muT;
	}

	public Double getBPR() {
		return _bpr;
	}

	public void setBPR(Double _BPR) {
		this._bpr = _BPR;
	}

	public Amount<Mass> getDryMassPublicDomain() {
		return _dryMassPublicDomain;
	}

	public EngineWeightsManager getWeights() {
		return weights;
	}

	public Amount<Length> getLength() {
		return _length;
	}

	public void setLength(Amount<Length> _length) {
		this._length = _length;
	}

	public EngineBalanceManager getBalance() {
		return balance;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}
	
	public int getNumberOfCompressorStages() {
		return _numberOfCompressorStages;
	}

	public void setNumberOfCompressorStages(int _numberOfCompressorStages) {
		this._numberOfCompressorStages = _numberOfCompressorStages;
	}

	public int getNumberOfShafts() {
		return _numberOfShafts;
	}

	public void setNumberOfShafts(int _numberOfShafts) {
		this._numberOfShafts = _numberOfShafts;
	}

	public double getOverallPressureRatio() {
		return _overallPressureRatio;
	}

	public void setOverallPressureRatio(double _overallPressureRatio) {
		this._overallPressureRatio = _overallPressureRatio;
	}



}
