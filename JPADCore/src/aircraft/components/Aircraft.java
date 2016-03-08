package aircraft.components;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACStructuralCalculatorManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.MyCosts;
import aircraft.componentmodel.AeroComponent;
import aircraft.componentmodel.Component;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.Canard;
import aircraft.components.liftingSurface.HTail;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.VTail;
import aircraft.components.liftingSurface.Wing;
import aircraft.components.nacelles.Nacelle;
import aircraft.components.nacelles.NacellesManager;
import aircraft.components.powerPlant.PowerPlant;
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ComponentEnum;

/**
 * This class holds all the data related with the aircraft
 * An aircraft object can be passed to each component
 * in order to make it aware of all the available data
 *
 * @author Lorenzo Attanasio
 */
public class Aircraft {

	private static final String _id = "11";

	private PowerPlant _thePowerPlant;
	private ACStructuralCalculatorManager _theStructures;
	private Systems _theSystems;

	private ACPerformanceManager _thePerformances;
	private ACWeightsManager _theWeights;

	// TODO: remove _aeroComponents ??!!
	private List<AeroComponent> _aeroComponents = new ArrayList<AeroComponent>();

	private AeroConfigurationTypeEnum _type = AeroConfigurationTypeEnum.EMPTY;
	private List<String> _components = new ArrayList<String>();
	private List<Component> _componentsList = new ArrayList<Component>();
	private List<LiftingSurface> _liftingSurfaceList = new ArrayList<LiftingSurface>();

	private Fuselage _theFuselage = null;
	private Wing _theWing = null;
	private HTail _theHTail = null;
	private VTail _theVTail = null;
	private Canard _theCanard = null;
	private Wing _exposedWing = null;

	//TODO: remove nacelle from aircraft, leave only nacelleS
	private Nacelle _theNacelle = null;
	private LandingGear _theLandingGear = null;

	private Double _sWetTotal = 0.;
	private String _name;
	private AircraftTypeEnum _typeVehicle;
	private Configuration _theConfiguration;

	private FuelTank _theFuelTank;

	private ACBalanceManager _theBalance;

	private double _lifeSpan = 14.; //typical life span in year
	private NacellesManager _theNacelles;
	private ACAerodynamicsManager _theAerodynamics;
	private MyCosts _theCosts;


	/**
	 * Create an aircraft without components
	 *
	 * @author Lorenzo Attanasio
	 */
	public Aircraft() {
		initialize();
	}

	/**
	 * Overload of the previous builder that creates an aircraft without components
	 * corresponding to the name specified.
	 *
	 * @author Vittorio Trifari
	 */
	public Aircraft(AircraftEnum aircraftName) {
		initialize(aircraftName);
	}

	/**
	 * Creates an aircraft according to user's needs.
	 * The user may pass a variable number of components
	 *
	 * @param components
	 */
	public Aircraft(Object ... components) {

		initialize();

		// TODO: check if components is a list that contains duplicates
		for (int i = 0; i < components.length; ++i) {
			addItem(components[i]);
		}

	}

	public void initialize() {
		_name = "";
		_typeVehicle = AircraftTypeEnum.TURBOPROP;
		_theConfiguration = new Configuration();
		_theBalance = new ACBalanceManager();
		_theWeights = new ACWeightsManager();
		_theAerodynamics = new ACAerodynamicsManager(this);
		_thePerformances = new ACPerformanceManager(this);
		_theCosts = new MyCosts(this);
	}

	/**
	 * Overload of the initialize method that recognize the name of the given aircraft and
	 * initialize it with it's data.
	 *
	 * @author Vittorio Trifari
	 * @param aircraftName
	 */
	public void initialize(AircraftEnum aircraftName) {

		// TODO: complete with other default aircraft
//		switch(aircraftName) {
		switch(aircraftName) {
		case ATR72:
			_name = "";
			_typeVehicle = AircraftTypeEnum.TURBOPROP;
			_theConfiguration = new Configuration(AircraftEnum.ATR72);
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager(AircraftEnum.ATR72);
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager(AircraftEnum.ATR72);
			_theCosts = new MyCosts(this);
			break;

		case B747_100B:
			_name = "";
			_typeVehicle = AircraftTypeEnum.JET;
			_theConfiguration = new Configuration(AircraftEnum.B747_100B);
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager(AircraftEnum.B747_100B);
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager(AircraftEnum.B747_100B);
			// TODO: These data are incorrect because referred to ATR-72. Fix when available
			_theCosts = new MyCosts(this);
			break;
			
		case AGILE_DC1:
			_name = "";
			_typeVehicle = AircraftTypeEnum.JET;
			_theConfiguration = new Configuration(AircraftEnum.AGILE_DC1);
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager(AircraftEnum.AGILE_DC1);
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager(AircraftEnum.AGILE_DC1);
			_theCosts = new MyCosts(this);
			break;
		
		
		}
	}

	/**
	 * @deprecated it must be used createDefaultAircraft(AircraftEnum aircraftName)
	 * 
	 * Create a default aircraft (ATR-72)
	 *
	 * @return
	 */
	public static Aircraft createDefaultAircraft() {
		Aircraft aircraft = new Aircraft();
		aircraft.createFuselage();
		aircraft.createWing();
		aircraft.createHTail();
		aircraft.createVTail();
		aircraft.createFuelTank();
		aircraft.createPowerPlant();
		aircraft.createNacelles();
		aircraft.createLandingGear();
		aircraft.createSystems();
		return aircraft;
	}

	/**
	 * Overload of the previous method that creates a default aircraft by giving it's name
	 * (ATR-72/B747-100B/F100/A320)
	 * TODO: add other aircraft data (actually only ATR-72/B747-100B are present)
	 *
	 * @author Vittorio Trifari
	 * @return
	 */
	public static Aircraft createDefaultAircraft(AircraftEnum aircraftName) {
		Aircraft aircraft = new Aircraft(aircraftName);
		aircraft.createFuselage(aircraftName);
		aircraft.createWing(aircraftName);
		aircraft.createHTail(aircraftName);
		aircraft.createVTail(aircraftName);
		aircraft.createFuelTank(aircraftName);
		aircraft.createPowerPlant(aircraftName);
		aircraft.createNacelles(aircraftName);
		aircraft.createLandingGear(aircraftName);
		aircraft.createSystems(aircraftName);
		aircraft.createExposedWing(aircraftName);
		return aircraft;
	}

	public void addItem(Object component) {

		if (component instanceof ComponentEnum) {

			if (component == ComponentEnum.FUSELAGE) {
				createFuselage();

			} else if (component == ComponentEnum.WING) {
				createWing();

			} else if (component == ComponentEnum.NACELLE) {
				createNacelle();

			} else if (component == ComponentEnum.HORIZONTAL_TAIL) {
				createHTail();

			} else if(component == ComponentEnum.VERTICAL_TAIL) {
				createVTail();

			} else if(component == ComponentEnum.CANARD) {
				createCanard();

			} else if(component == ComponentEnum.LANDING_GEAR) {
				createLandingGear();

			} else if(component == ComponentEnum.FUEL_TANK) {
				createFuelTank();

			} else if (component == ComponentEnum.POWER_PLANT) {
				createPowerPlant();

			} else if (component == ComponentEnum.SYSTEMS) {
				createSystems();

			} else {
				System.out.println("Invalid component");
			}
		}
	}

	public void createFuselage() {
		_theFuselage = new Fuselage(
				"Fuselage", // name
				"Data from AC_ATR_72_REV05.pdf", // description
				0.0, 0.0, 0.0 // Fuselage apex (x,y,z)-coordinates in construction axes
				);
		_componentsList.add(_theFuselage);
	}

	/**
	 * Overload of the fuselage creator that recognize the aircraft name and initialize with
	 * their parameters.
	 *
	 * @author Vittorio Trifari
	 */
	public void createFuselage(AircraftEnum aircraftName) {
		_theFuselage = new Fuselage(
				aircraftName,
				"Fuselage", // name
				"Data available for ATR-72 and B747_100B", // description
				0.0, 0.0, 0.0 // Fuselage apex (x,y,z)-coordinates in construction axes
				);
		_componentsList.add(_theFuselage);
	}

	public void createWing() {
		_theWing = new Wing(
				"Wing", // name
				"Data from AC_ATR_72_REV05.pdf",
				11.0, 0.0, 1.6,
				ComponentEnum.WING,
				_theFuselage,
				_theNacelle,
				_theHTail,
				_theVTail
				);

		_componentsList.add(_theWing);
		_liftingSurfaceList.add(_theWing);
	}

	/**
	 * Overload of the wing builder that recognize the aircraft and sets it's relative values.
	 *
	 * @author Vittorio Trifari
	 */
	public void createWing(AircraftEnum aircraftName) {

		switch(aircraftName) {
		case ATR72:
			_theWing = new Wing(
					aircraftName,
					"Wing", // name
					"Data from AC_ATR_72_REV05.pdf",
					11.0, 0.0, 1.6,
					ComponentEnum.WING,
					_theFuselage,
					_theNacelle,
					_theHTail,
					_theVTail
					);
			break;
		case B747_100B:
			_theWing = new Wing(
					aircraftName,
					"Wing", // name
					"Data from REPORT_B747_100B in database",
					18.2, 0.0, -2.875,
					ComponentEnum.WING,
					_theFuselage,
					_theNacelle,
					_theHTail,
					_theVTail
					);
			break;
			
			case AGILE_DC1:
				_theWing = new Wing(
						aircraftName,
						"Wing", // name
						"Data from AGILE_D2.5 doc",
						11.5, 0.0, 0.39,
						ComponentEnum.WING,
						_theFuselage,
						_theNacelle,
						_theHTail,
						_theVTail
						);
		}

		_componentsList.add(_theWing);
		_liftingSurfaceList.add(_theWing);
	}

	/**
	 * This method creates an exposed wing
	 *
	 * @author Manuela Ruocco
	 */
	public void createExposedWing(AircraftEnum aircraftName) {

		if ( (this.get_wing() != null) && (this.get_fuselage() != null)) {

			this.get_fuselage().calculateGeometry();
			this.get_fuselage().checkGeometry();
			this.set_sWetTotal(this.get_fuselage().get_sWet().getEstimatedValue());

			this.get_wing().calculateGeometry();
			// this.get_wing().updateAirfoilsGeometry();
			this.get_wing().getGeometry().calculateAll();
			this.set_sWetTotal(this.get_wing().get_surfaceWettedExposed().getEstimatedValue());

			_exposedWing = new Wing(
					aircraftName,
					"Wing", // name
					"Data from AC_ATR_72_REV05.pdf",
					this.get_wing().get_X0().getEstimatedValue(),
						this.get_fuselage().getWidthAtX(this.get_wing().get_xLEMacActualBRF().getEstimatedValue()),
							this.get_wing().get_Z0().getEstimatedValue(),
					ComponentEnum.WING,
					_theFuselage,
					_theNacelle,
					_theHTail,
					_theVTail
					);

		}

		_componentsList.add(_theWing);
		_liftingSurfaceList.add(_theWing);
	}

	public void createNacelle() {
		_theNacelle = new Nacelle(
				"Nacelle",
				"Data from AC_ATR_72_REV05.pdf",
				8.61, 4.05, 1.3255
				);

		_componentsList.add(_theNacelle);
	}

	public void createNacelles() {
		_theNacelles = new NacellesManager(this);
		_theNacelles.initializeNacelles();
		_theNacelles.setEngines();

		for (int i=0; i < _theNacelles.get_nacellesNumber(); i++){
			_componentsList.add(_theNacelles.get_nacellesList().get(i));
		}
	}

	/**
	 * Overload of the default creator that recognize aircraft name and creates it's nacelles.
	 *
	 * @author Vittorio Trifari
	 */
	public void createNacelles(AircraftEnum aircraftName) {
		_theNacelles = new NacellesManager(aircraftName, this);
		_theNacelles.initializeNacelles(aircraftName);
		_theNacelles.setEngines();

		for (int i=0; i < _theNacelles.get_nacellesNumber(); i++){
			_componentsList.add(_theNacelles.get_nacellesList().get(i));
		}
	}

	public void createHTail() {
		_theHTail =  new HTail(
				"HTail",
				"Data taken from ...",
				24.6,
				0.0,
				7.72 - _theFuselage.get_heightFromGround().getEstimatedValue() - _theFuselage.get__diam_C().getEstimatedValue()/2,
				ComponentEnum.HORIZONTAL_TAIL,
				_theFuselage,
				_theNacelle,
				_theWing,
				_theVTail
				);

		_componentsList.add(_theHTail);
		_liftingSurfaceList.add(_theHTail);
	}

	/**
	 * Overload of the H-Tail creator that recognize aircraft name and sets create it's H-tail.
	 *
	 * @author Vittorio Trifari
	 */
	public void createHTail(AircraftEnum aircraftName) {

		switch(aircraftName) {

		case ATR72:
			_theHTail =  new HTail(
					aircraftName,
					"HTail",
					"Data taken from ...",
					24.6,
					0.0,
					7.72 - _theFuselage.get_heightFromGround().getEstimatedValue() - _theFuselage.get__diam_C().getEstimatedValue()/2,
					ComponentEnum.HORIZONTAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theVTail
					);
			break;

		case B747_100B:
			_theHTail =  new HTail(
					aircraftName,
					"HTail",
					"Data taken from REPORT-B747_100B",
					60.7,
					0.0,
					0.4850,
					ComponentEnum.HORIZONTAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theVTail
					);
			break;
			
		case AGILE_DC1:
			_theHTail =  new HTail(
					aircraftName,
					"HTail",
					"Data taken from AGILE_D2.5 doc",
					29.72,
					0.0,
					2.46,
					ComponentEnum.HORIZONTAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theVTail
					);
			break;
		}

		_componentsList.add(_theHTail);
		_liftingSurfaceList.add(_theHTail);
	}

	public void createVTail() {
		_theVTail =  new VTail(
				"VTail",
				"Data taken from ...",
				21.9,
				0.0,
				1.30,
				ComponentEnum.VERTICAL_TAIL,
				_theFuselage,
				_theNacelle,
				_theWing,
				_theHTail
				);

		_componentsList.add(_theVTail);
		_liftingSurfaceList.add(_theVTail);
	}

	/**
	 * Overload of the default creator that recognize the aircraft name and creates a V-tail with it's values.
	 *
	 * @author Vittorio Trifari
	 */
	public void createVTail(AircraftEnum aircraftName) {

		switch(aircraftName) {
		case ATR72:
			_theVTail =  new VTail(
					aircraftName,
					"VTail",
					"Data taken from ...",
					21.9,
					0.0,
					1.30,
					ComponentEnum.VERTICAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theHTail
					);
			break;

		case B747_100B:
			_theVTail =  new VTail(
					aircraftName,
					"VTail",
					"Data taken from REPORT-B747_100B",
					56.2900,
					0.0,
					2.8150,
					ComponentEnum.VERTICAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theHTail
					);
			break;
			
		case AGILE_DC1:
			_theVTail =  new VTail(
					aircraftName,
					"VTail",
					"Data taken from AGILE_D2.5 doc",
					28.12,
					0.0,
					2.98,
					ComponentEnum.VERTICAL_TAIL,
					_theFuselage,
					_theNacelle,
					_theWing,
					_theHTail
					);
			break;
			
		}

		_componentsList.add(_theVTail);
		_liftingSurfaceList.add(_theVTail);
	}

	public void createCanard() {
		_theCanard =  new Canard(
				"Canard",
				"Data taken from ...",
				21.9,
				0.0,
				_theFuselage.get_heightFromGround().getEstimatedValue() + _theFuselage.get__diam_C().getEstimatedValue(),
				ComponentEnum.CANARD,
				_theFuselage,
				_theNacelle,
				_theWing,
				_theVTail
				);

		_componentsList.add(_theCanard);
		_liftingSurfaceList.add(_theCanard);
	}

	public void createLandingGear() {
		_theLandingGear = new LandingGear(
				"Landing Gear",
				"ATR 72 Landing Gear",
				12.5, 0., 0.);
		_componentsList.add(_theLandingGear);
	}

	/**
	 * Overload of the default creator that recognize aircraft name and
	 * sets it's landing gear data.
	 *
	 * @author Vittorio Trifari
	 */
	public void createLandingGear(AircraftEnum aircraftName) {

		switch(aircraftName) {

		case ATR72:
			_theLandingGear = new LandingGear(
					aircraftName,
					"Landing Gear",
					"ATR 72 Landing Gear",
					12.5, 0., 0.);
			_componentsList.add(_theLandingGear);
			break;

		case B747_100B:
			_theLandingGear = new LandingGear(
					aircraftName,
					"Landing Gear",
					"B747-100B Landing Gear",
					5., 0., 0.);
			_componentsList.add(_theLandingGear);
			break;
			
		case AGILE_DC1:
			_theLandingGear = new LandingGear(
					aircraftName,
					"Landing Gear",
					"AGILE_DC1 Landing Gear",
					12.5, 0., 0.);
			_componentsList.add(_theLandingGear);
			break;
		}
	}

	public void createFuelTank() {

		_theFuelTank = new FuelTank(
				"Fuel Tank",
				"ATR 72 Fuel Tank",
				12.5, 0., 0.);
		_componentsList.add(_theFuelTank);
	}

	/**
	 * Overload of the creator of the fuel tank that recognize aircraft name and sets it's data.
	 *
	 * @author Vittorio Trifari
	 */
	public void createFuelTank(AircraftEnum aircraftName) {

		switch(aircraftName) {
		case ATR72:
			_theFuelTank = new FuelTank(
					aircraftName,
					"Fuel Tank",
					"ATR 72 Fuel Tank",
					12.5, 0., 0.);
			_componentsList.add(_theFuelTank);
			break;

		case B747_100B:
			_theFuelTank = new FuelTank(
					aircraftName,
					"Fuel Tank",
					"B747-100B Fuel Tank",
					34.14, 14.44, -2.8750);
			_componentsList.add(_theFuelTank);
			break;
			
		case AGILE_DC1:
			_theFuelTank = new FuelTank(
					aircraftName,
					"Fuel Tank",
					"AGILE_DC1 Fuel Tank",
					12.891, 4.968, -1.782);
			_componentsList.add(_theFuelTank);
			break;
		}
	}

	public void createPowerPlant() {

		
		_thePowerPlant = new PowerPlant(
				"Power plant",
				"Data taken from ...",
				0.0, 0.0, 0.0,
				this);
		_componentsList.add(_thePowerPlant);}
	

	/**
	 * Overload of the default creator that recognize aircraft name and sets it's values.
	 *
	 * @author Vittorio Trifari
	 */
	public void createPowerPlant(AircraftEnum aircraftName) {

		switch(aircraftName) {
		case ATR72:
		_thePowerPlant = new PowerPlant(
				aircraftName,
				"Power plant",
				"Data taken from ...",
				8.6100, 4.0500, 1.3255,
				this);
		_componentsList.add(_thePowerPlant);
		break;

		case B747_100B:
			_thePowerPlant = new PowerPlant(
					aircraftName,
					"Power plant",
					"Data taken from ...",
					0.0, 0.0, 0.0,
					this);
			_componentsList.add(_thePowerPlant);
			break;

		case AGILE_DC1:
			_thePowerPlant = new PowerPlant(
					aircraftName,
					"Power plant",
					"Data taken from ...",
					0.0,0.0,0.0,
					this);
			_componentsList.add(_thePowerPlant);
			break;
		}
	}

	public void createSystems() {
		_theSystems = new Systems(
				"Systems",
				"Data taken from ...",
				0.0,
				0.0,
				0.0);
		_componentsList.add(_theSystems);

	}

	/**
	 * Overload of the default creator that recognize aircraft name and
	 * sets it's data.
	 *
	 * @author Vittorio Trifari
	 */
	public void createSystems(AircraftEnum aircraftName) {
		_theSystems = new Systems(
				aircraftName,
				"Systems",
				"Data taken from ...",
				0.0,
				0.0,
				0.0);
		_componentsList.add(_theSystems);

	}

	// REPLACED WITH _configuration list
	private void updateType()
	{
		// TO DO: check all expected cases

		if (
				_theFuselage == null &&
				_theWing == null &&
				_theNacelle == null
				)
		{
			_type = AeroConfigurationTypeEnum.EMPTY;
		}
		if (
				_theFuselage != null &&
				_theWing == null &&
				_theNacelle == null
				)
		{
			_type = AeroConfigurationTypeEnum.FUSELAGE_ISOLATED;
		}
		if (
				_theFuselage == null &&
				_theWing != null &&
				_theNacelle == null
				)
		{
			_type = AeroConfigurationTypeEnum.WING_ISOLATED;
		}
		if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelle == null
				)
		{
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE;
		}
		if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelle != null
				)
		{
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_NACELLES;
		}
		if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelle != null &&
				_theHTail != null
				)
		{
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_HTAIL_NACELLES;
		}
		else
		{
			_type = AeroConfigurationTypeEnum.UNKNOWN;
		}
		//////////////////////////////////////////
		// TO DO: update this code as needed
		//////////////////////////////////////////

	} // end of updateType


	private void deleteFuselage() {
		_theFuselage = null;
		updateType();
	}


	private void deleteWing()
	{
		_theWing = null;
		updateType();
	}


	private void deleteNacelle()
	{
		_theNacelle = null;
		updateType();
	}


	private void deleteHTail()
	{
		_theHTail = null;
		updateType();
	}


	private void deleteVTail()
	{
		_theVTail = null;
		updateType();
	}


	//////////////////////////////////////////
	// TO DO: update this code as needed
	//////////////////////////////////////////

	public AircraftTypeEnum get_typeVehicle() {
		return _typeVehicle;
	}

	public void set_typeVehicle(AircraftTypeEnum _typeVehicle) {
		this._typeVehicle = _typeVehicle;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public AeroConfigurationTypeEnum get_type() {
		return _type;
	}

	public Fuselage get_fuselage() {
		return _theFuselage;
	}

	/**
	 * Return the object corresponding to enum constant
	 *
	 * @param component
	 * @return
	 */
	public Component get_component(ComponentEnum component) {

		if (component == ComponentEnum.FUSELAGE) {
			return _theFuselage;

		} else if (component == ComponentEnum.WING) {
			return _theWing;

		} else if (component == ComponentEnum.HORIZONTAL_TAIL) {
			return _theHTail;

		} else if (component == ComponentEnum.VERTICAL_TAIL) {
			return _theVTail;

		} else if (component == ComponentEnum.CANARD) {
			return _theCanard;

		} else if (component == ComponentEnum.FUEL_TANK) {
			return _theFuelTank;

		} else if (component == ComponentEnum.NACELLE) {
			return _theNacelle;

		} else if (component == ComponentEnum.POWER_PLANT) {
			return _thePowerPlant;

		} else if (component == ComponentEnum.SYSTEMS) {
			return _theSystems;

		} else if (component == ComponentEnum.LANDING_GEAR) {
			return _theLandingGear;

		} else  {
			return null;
		}

		//		else {
		//			return _thePropulsion;
		//		}
	}


	public Wing get_wing() {
		return _theWing;
	}

	public HTail get_HTail() {
		return _theHTail;
	}

	public VTail get_VTail() {
		return _theVTail;
	}

	public Canard get_Canard() {
		return _theCanard;
	}

	public List<String> get_components() {
		return _components;
	}

	public Double get_sWetTotal() {
		return _sWetTotal;
	}

	public void set_sWetTotal(Double sWet) {
		_sWetTotal = _sWetTotal + sWet;
	}

	//TODO check if some of these functions must be removed
	public void addAerocomponent(AeroComponent aercomp) {
		this._aeroComponents.add(aercomp);
	}
	public void removeAerocomponent(AeroComponent aercomp) {
		this._aeroComponents.remove(aercomp);
	}

	public void resetAerocomponent() {
		this._aeroComponents.clear();
	}

	public List<AeroComponent> getMyComponent() {
		return _aeroComponents;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public Configuration get_configuration() {
		return _theConfiguration;
	}


	public PowerPlant get_powerPlant() {
		return _thePowerPlant;
	}

	public ACStructuralCalculatorManager get_structures() {
		return _theStructures;
	}

	public Systems get_systems() {
		return _theSystems;
	}

	public ACPerformanceManager get_performances() {
		return _thePerformances;
	}

	public LandingGear get_landingGear() {
		return _theLandingGear;
	}


	public ACWeightsManager get_weights() {
		return _theWeights;
	}

	public List<Component> get_componentsList() {
		return _componentsList;
	}

	public FuelTank get_theFuelTank() {
		return _theFuelTank;
	}

	public void set_theFuelTank(FuelTank _theFuelTank) {
		this._theFuelTank = _theFuelTank;
	}

	public ACBalanceManager get_theBalance() {
		return _theBalance;
	}

	public void set_theBalance(ACBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}

	public static String getId() {
		return _id;
	}

	public double get_lifeSpan() {
		return _lifeSpan;
	}

	public void set_lifeSpan(double _lifeSpan) {
		this._lifeSpan = _lifeSpan;
	}

	public NacellesManager get_theNacelles() {
		return _theNacelles;
	}

	public List<LiftingSurface> get_liftingSurfaceList() {
		return _liftingSurfaceList;
	}

	public ACAerodynamicsManager get_theAerodynamics() {
		return _theAerodynamics;
	}

	public MyCosts get_theCosts() {
		return _theCosts;
	}

	public Wing get_exposedWing() {
		return _exposedWing;
	}

	public void set_exposedWing(Wing _exposedWing) {
		this._exposedWing = _exposedWing;
	}

} // end of class
