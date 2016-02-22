package aircraft.components.nacelles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.componentmodel.Component;
import aircraft.components.Aircraft;
import aircraft.components.powerPlant.Engine;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;

/** 
 * The Nacelle is considered 
 * the structural part of the engine
 * 
 * @author Lorenzo Attanasio
 *
 */
public class Nacelle extends Component{

	public enum MountingPosition {
		WING,
		FUSELAGE,
		UNDERCARRIAGE_HOUSING
	}

	private String _id;
	private static int idCounter = 0;
	private Amount<Length> _X0, _Y0, _Z0;
	private Amount<Length> _heightFromGround;
	private Amount<Length> _length;
	private Amount<Length> _diameterMean, _diameterInlet, _diameterOutlet;
	private Amount<Length> _roughness;
	private Amount<Area> _surfaceWetted;

	private OperatingConditions _theOperatingConditions;
	private Amount<Mass> _mass, _massReference, _totalMass;
	private MountingPosition _mountingPosition;

	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Double[] _percentDifference;
	private Amount<Mass> _massEstimated;

	private Amount<Length> _xCG, _yCG, _xCGReference, 
	_xCGEstimated, _yCGReference, _yCGEstimated,
	_zCG, _zCGEstimated, _zCGReference;

	private Double[] _percentDifferenceXCG;

	private Aircraft _theAircraft;
	private Engine _theEngine;
	private NacWeightsManager weights;
	private NacBalanceManager balance;
	private NacAerodynamicsManager aerodynamics;
	/**
	 * Define a single nacelle object regardless of the
	 * aircraft and the engine which corresponds to the nacelle.
	 * The corresponding engine must be set to run the analyses.
	 * 
	 * @author Lorenzo Attanasio
	 * @param name
	 * @param description
	 * @param x coordinate of forward-most point of the nacelle measured from aircraft nose
	 * @param y
	 * @param z
	 */
	public Nacelle(String name, String description, double x, double y, double z) {

		super("", name, description, x, y, z);

		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);

		_cg.set_xBRF(_X0);
		_cg.set_yBRF(_Y0);
		_cg.set_zBRF(_Z0);
	}

	public Nacelle(String name, String description, 
			double x, double y, double z,
			Aircraft aircraft) {
		this(name, description, x, y, z);
		_theAircraft = aircraft;
		
		_id = aircraft.get_theNacelles().getId() + idCounter + "99";
		idCounter++;
		
		initializeData();
	}
	
	/**
	 * Overload of the previous builder that recognize aircraft name and initialize 
	 * it'a nacelles data.
	 * 
	 * @author Vittorio Trifari
	 */
	public Nacelle(AircraftEnum aircraftName, String name, String description, 
			double x, double y, double z,
			Aircraft aircraft) {
		this(name, description, x, y, z);
		_theAircraft = aircraft;
		
		_id = aircraft.get_theNacelles().getId() + idCounter + "99";
		idCounter++;
		
		initializeData(aircraftName);
	}

	/**
	 * Define a nacelle linking it to an engine
	 * This is mandatory to execute the analyses
	 * 
	 * @author Lorenzo Attanasio
	 * @param name
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @param engine
	 */
	public Nacelle(String name, String description, 
			double x, double y, double z,
			Aircraft aircraft,
			Engine engine) {

		this(name, description, x, y, z, aircraft);
		_theEngine = engine;
		initializeWeights();
		initializeBalance();
		initializeAerodynamics();
	}

	public void initializeData() {
		_length = Amount.valueOf(4.371,SI.METER);
		_diameterMean = Amount.valueOf(1.4,SI.METER);
		_diameterInlet = Amount.valueOf(1.2,SI.METER);
		_diameterOutlet = Amount.valueOf(0.2,SI.METER);

		_roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
		_heightFromGround = Amount.valueOf(3.19, SI.METER);

		//		_z = Amount.valueOf(_heightFromGround.getEstimatedValue() 
		//				- _theFuselage.get_heightGround().getEstimatedValue() 
		//				- _theFuselage.get_diam_C().getEstimatedValue()/2, SI.METER);
		_surfaceWetted = Amount.valueOf(12.5, Area.UNIT); // matlab file ATR72

		_massReference = Amount.valueOf(409.4, SI.KILOGRAM);
		_mountingPosition = MountingPosition.WING;
		
		calculateSurfaceWetted();
	}
	
	/**
	 * Overload of the default initializer that recognize aircraft name and sets it's
	 * nacelle data.
	 * 
	 * @author Vittorio Trifari
	 */
	public void initializeData(AircraftEnum aircraftName) {
		
		switch(aircraftName) {
		
		case ATR72:
			_length = Amount.valueOf(4.371,SI.METER);
			_diameterMean = Amount.valueOf(1.4,SI.METER);
			_diameterInlet = Amount.valueOf(1.2,SI.METER);
			_diameterOutlet = Amount.valueOf(0.2,SI.METER);

			_roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
			_heightFromGround = Amount.valueOf(3.19, SI.METER);

			//		_z = Amount.valueOf(_heightFromGround.getEstimatedValue() 
			//				- _theFuselage.get_heightGround().getEstimatedValue() 
			//				- _theFuselage.get_diam_C().getEstimatedValue()/2, SI.METER);
			_surfaceWetted = Amount.valueOf(12.5, Area.UNIT); // matlab file ATR72

			_massReference = Amount.valueOf(409.4, SI.KILOGRAM);
			_mountingPosition = MountingPosition.WING;
			
			calculateSurfaceWetted();
			break;
			
		case B747_100B:
			_length = Amount.valueOf(7.6,SI.METER);
			_diameterMean = Amount.valueOf(2.0,SI.METER);
			_diameterInlet = Amount.valueOf(1.2,SI.METER);
			_diameterOutlet = Amount.valueOf(0.2,SI.METER);

			_roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
			_heightFromGround = Amount.valueOf(0.81, SI.METER);

			//		_z = Amount.valueOf(_heightFromGround.getEstimatedValue() 
			//				- _theFuselage.get_heightGround().getEstimatedValue() 
			//				- _theFuselage.get_diam_C().getEstimatedValue()/2, SI.METER);
			_surfaceWetted = Amount.valueOf(95.5044, Area.UNIT); // matlab file ATR72

			_massReference = Amount.valueOf(1184.2500, SI.KILOGRAM);
			_mountingPosition = MountingPosition.WING;
			
			calculateSurfaceWetted();
			break;
			
		case AGILE_DC1:
			_length = Amount.valueOf(2.739,SI.METER);
			_diameterMean = Amount.valueOf(1.226,SI.METER);
			_diameterInlet = Amount.valueOf(1.226,SI.METER);
			_diameterOutlet = Amount.valueOf(1.226,SI.METER);

			_roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
			_heightFromGround = Amount.valueOf(3.6, SI.METER);

			_surfaceWetted = Amount.valueOf(12.1, Area.UNIT); // ADAS

			_massReference = Amount.valueOf(395.95, SI.KILOGRAM);
			_mountingPosition = MountingPosition.WING;
			
			calculateSurfaceWetted();
			break;
		}
	}

	public void initializeWeights() {
		if (weights == null) 
			weights = new NacWeightsManager(_theAircraft, this);
	}

	public void initializeAerodynamics() {
		if (aerodynamics == null) 
			aerodynamics = new NacAerodynamicsManager(_theAircraft, this);
		//TODO: find a way to manage this
//		aerodynamics.set_mach(_theOperatingConditions.get_machCurrent());
//		aerodynamics.set_altitude(_theOperatingConditions.get_altitude().doubleValue(SI.METER));
	}
	
	public void initializeBalance() {
		if (balance == null)
			balance = new NacBalanceManager(this);
	}

	/**
	 * Wetted surface is considered as two times the external surface
	 * (the air flows both outside and inside)
	 */
	public void calculateSurfaceWetted() {
		_surfaceWetted = _length.times(_diameterMean.times(Math.PI * 2)).to(SI.SQUARE_METRE);
	}

	/**
	 * Invoke all the methods to evaluate 
	 * nacelle related quantities
	 * 
	 * @author Lorenzo Attanasio
	 */
	public void calculateAll() {
		initializeWeights();
		initializeBalance();
		initializeAerodynamics();
		
		weights.calculateAll();
		balance.calculateAll();
		aerodynamics.calculateAll();
	}


	//	@SuppressWarnings("unchecked")
	//	@Override
	//	public void calculateCG(
	//			MyAircraft aircraft, 
	//			MyOperatingConditions conditions,
	//			MyMethodEnum method) {
	//
	//		_cg.setLRForigin(_X0, _Y0, _Z0);
	//		_cg.set_xLRFref(_length.times(0.4));
	//		_cg.set_yLRFref(_diameterMean.divide(2));
	//		_cg.set_zLRFref(_diameterMean.divide(2));
	//
	//		// Initialize _methodsList again to clear it
	//		// from old entries
	//		_methodsList = new ArrayList<MyMethodEnum>();
	//
	//		switch(method) {
	//
	//		// page 313 Torenbeek (1982)
	//		case TORENBEEK_1982 : { 
	//			_methodsList.add(method);
	//			_xCG = _length.times(0.4);
	//			_xCGMap.put(method, _xCG);
	//		} break;
	//
	//		default : break;
	//
	//		}
	//
	//		_methodsMap.put(MyAnalysisTypeEnum.BALANCE, _methodsList);
	//		_percentDifferenceXCG = new Double[_xCGMap.size()];
	//
	//		_cg.set_xLRF((Amount<Length>) MyWriteUtils.compareMethods(
	//				_cg.get_xLRFref(), 
	//				_xCGMap,
	//				_percentDifferenceXCG,
	//				30.).getFilteredMean());
	//		
	//		_cg.calculateCGinBRF();
	//
	//	}


	public Double formFactor(){

		//matlab file ATR72
		return (1 + 0.165 
				+ 0.91/(_length.getEstimatedValue()/_diameterMean.getEstimatedValue())); 	
	}

	public Amount<Length> get_heightFromGround() {
		return _heightFromGround;
	}

	public void set_heightFromGround(Amount<Length> _height) {
		this._heightFromGround = _height;
	}

	public Amount<Length> get_length() {
		return _length;
	}

	public void set_length(Amount<Length> _lenght) {
		this._length = _lenght;
	}

	public Amount<Area> get_surfaceWetted() {
		return _surfaceWetted;
	}

	public void set_surfaceWetted(Amount<Area> _sWet) {
		this._surfaceWetted = _sWet;
	}

	public Amount<Length> get_diameterMean() {
		return _diameterMean;
	}

	public void set_diameterMean(Amount<Length> _diameter) {
		this._diameterMean = _diameter;
	}

	public Amount<Length> get_diameterInlet() {
		return _diameterInlet;
	}

	public void set_diameterInlet(Amount<Length> _inletDiameter) {
		this._diameterInlet = _inletDiameter;
	}

	public Amount<Length> get_diameterOutlet() {
		return _diameterOutlet;
	}

	public void set_diameterOutlet(Amount<Length> _exitDiameter) {
		this._diameterOutlet = _exitDiameter;
	}

	public Amount<Length> get_roughness() {
		return _roughness;
	}

	public void set_roughness(Amount<Length> _roughness) {
		this._roughness = _roughness;
	}

	public Amount<Mass> get_mass() {
		return _mass;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public MountingPosition get_mounting() {
		return _mountingPosition;
	}


	public void set_mounting(MountingPosition _mounting) {
		this._mountingPosition = _mounting;
	}


	public Amount<Mass> get_massReference() {
		return _massReference;
	}


	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}


	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}


	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	public List<MethodEnum> get_methodsList() {
		return _methodsList;
	}


	public Double[] get_percentDifference() {
		return _percentDifference;
	}


	public Amount<Mass> get_totalMass() {
		return _totalMass;
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


	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}


	public Engine get_theEngine() {
		return _theEngine;
	}

	public void set_theEngine(Engine _theEngine) {
		this._theEngine = _theEngine;
	}

	public NacWeightsManager getWeights() {
		initializeWeights();
		return weights;
	}

	public NacAerodynamicsManager getAerodynamics() {
		initializeAerodynamics();
		return aerodynamics;
	}

	public NacBalanceManager getBalance() {
		initializeBalance();
		return balance;
	}

	public String get_id() {
		return _id;
	}

}
