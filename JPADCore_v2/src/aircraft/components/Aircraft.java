package aircraft.components;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACStructuralCalculatorManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.Costs;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.Fuselage.FuselageBuilder;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import aircraft.components.nacelles.Nacelle;
import aircraft.components.nacelles.NacellesManager;
import aircraft.components.powerPlant.PowerPlant;
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ComponentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import parser.ExprParser.casePart_return;

/**
 * This class holds all the data related with the aircraft
 * An aircraft object can be passed to each component
 * in order to make it aware of all the available data
 *
 * @authors Vittorio Trifari,
 * 		    Agostino De Marco,
 *  		Vincenzo Cusati,
 *  	    Manuela Ruocco
 */
public class Aircraft {

	private String _id;
	private AeroConfigurationTypeEnum _type = AeroConfigurationTypeEnum.EMPTY;
	private AircraftTypeEnum _typeVehicle;

	private ACAerodynamicsManager _theAerodynamics;
	private ACStructuralCalculatorManager _theStructures;
	private ACPerformanceManager _thePerformances;
	private ACWeightsManager _theWeights;
	private ACBalanceManager _theBalance;
	private Costs _theCosts;

	private Fuselage _theFuselage;
	private LiftingSurface _theWing;
	private LiftingSurface _theHTail;
	private LiftingSurface _theVTail;
	private LiftingSurface _theCanard;
	private PowerPlant _thePowerPlant;
	private NacellesManager _theNacelles;
	private FuelTanks _theFuelTank;
	private LandingGears _theLandingGears;
	private Systems _theSystems;
	private CabinConfiguration _theConfiguration;
	
	private Double _sWetTotal = 0.;

	private List<Object> _componentsList = new ArrayList<Object>();
	
	private double _lifeSpan = 14.; //typical life span in year

	/**
	 * Create an aircraft without components
	 *
	 * @author Lorenzo Attanasio
	 */
	public Aircraft (String id) {
		this._id = id;
		initialize();
	}

	/**
	 * Overload of the previous builder that creates an aircraft without components
	 * corresponding to the name specified.
	 *
	 * @author Vittorio Trifari
	 */
	public Aircraft(AircraftEnum aircraftName) {
//		initialize(aircraftName);
	}

	public void initialize() {
		_typeVehicle = AircraftTypeEnum.TURBOPROP;
		_theConfiguration = new CabinConfiguration.ConfigurationBuilder(_id).build();
		_theBalance = new ACBalanceManager();
		_theWeights = new ACWeightsManager();
		_theAerodynamics = new ACAerodynamicsManager(this);
		_thePerformances = new ACPerformanceManager(this);
		_theCosts = new Costs(this);
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
			_id = "ATR-72";
			_typeVehicle = AircraftTypeEnum.TURBOPROP;
			_theConfiguration = new CabinConfiguration.ConfigurationBuilder(_id, AircraftEnum.ATR72).build();
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager();
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager();
			_theCosts = new Costs(this);
			break;

		case B747_100B:
			_id = "B747-100B";
			_typeVehicle = AircraftTypeEnum.JET;
			_theConfiguration = new CabinConfiguration.ConfigurationBuilder(_id, AircraftEnum.B747_100B).build();
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager();
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager();
			// TODO: These data are incorrect because referred to ATR-72. Fix when available
			_theCosts = new Costs(this);
			break;
			
		case AGILE_DC1:
			_id = "AGILE DC-1";
			_typeVehicle = AircraftTypeEnum.JET;
			_theConfiguration = new CabinConfiguration.ConfigurationBuilder(_id, AircraftEnum.AGILE_DC1).build();
			_theBalance = new ACBalanceManager();
			_theWeights = new ACWeightsManager();
			_theAerodynamics = new ACAerodynamicsManager(this);
			_thePerformances = new ACPerformanceManager();
			_theCosts = new Costs(this);
			break;
		
		}
	}

	/**
	 * Method that creates a default aircraft by giving it's name
	 * TODO: add other aircraft data (actually only ATR-72/B747-100B are present)
	 *
	 * @author Vittorio Trifari
	 * @return
	 */
	public static Aircraft createDefaultAircraft(
			AircraftEnum aircraftName,
			AerodynamicDatabaseReader aeroDatabaseReader
			) {
			
		Aircraft aircraft = new Aircraft(aircraftName);
		aircraft.createFuselage(aircraftName);
		aircraft.createWing(aircraftName, aeroDatabaseReader);
		
		switch(aircraftName) {
		case ATR72 :
			
			aircraft.getWing().setXApexConstructionAxes(Amount.valueOf(12.5, SI.METER));
			aircraft.getWing().setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
			aircraft.getWing().setZApexConstructionAxes(Amount.valueOf(1.0, SI.METER));
			aircraft.getWing().setRiggingAngle(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
			
			break;
		default:
			break;
			
		}
		
//		aircraft.createHTail(aircraftName);
//		aircraft.createVTail(aircraftName);
//		aircraft.createFuelTank(aircraftName);
//		aircraft.createPowerPlant(aircraftName);
//		aircraft.createNacelles(aircraftName);
//		aircraft.createLandingGear(aircraftName);
//		aircraft.createSystems(aircraftName);
//		aircraft.createExposedWing(aircraftName);
		return aircraft;
	}

	/**
	 * Overload of the fuselage creator that recognize the aircraft name and initialize with
	 * their parameters.
	 *
	 * @author Vittorio Trifari
	 */
	public void createFuselage(AircraftEnum aircraftName) {
		_theFuselage = new FuselageBuilder("Fuselage")
				.fuselageCreator(
						new FuselageCreator
							.FuselageBuilder("Fuselage", aircraftName)
								.build()
						)
				.build();
		
		_theFuselage.getFuselageCreator().calculateGeometry();
		
		_componentsList.add(_theFuselage);
	}

	/**
	 * Overload of the wing builder that recognize the aircraft and sets it's relative values.
	 *
	 * @author Vittorio Trifari
	 */
	public void createWing(AircraftEnum aircraftName, AerodynamicDatabaseReader aeroDatabaseReader) {

		_theWing = new LiftingSurfaceBuilder("Wing", ComponentEnum.WING, aeroDatabaseReader)
				.liftingSurfaceCreator(
						new LiftingSurfaceCreator
							.LiftingSurfaceCreatorBuilder("Wing", Boolean.TRUE, aircraftName, ComponentEnum.WING)
								.build()
						)
				.build();
		
		_theWing.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, Boolean.TRUE);
		
		_componentsList.add(_theWing);
	}

	/**
	 * This method creates an exposed wing
	 *
	 * @author Manuela Ruocco
	 */
//	public void createExposedWing(AircraftEnum aircraftName) {
//
//		if ( (this.getWing() != null) && (this.getFuselage() != null)) {
//
//			this.getFuselage().getFuselageCreator().calculateGeometry();
//			this.getFuselage().getFuselageCreator().checkGeometry();
//			this.setSWetTotal(this.getFuselage().getFuselageCreator().getsWet().getEstimatedValue());
//
//			this.getWing().getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, Boolean.TRUE);
//			this.setSWetTotal(this.getWing().getLiftingSurfaceCreator()._surfaceWettedExposed().getEstimatedValue());
//
//			_exposedWing = new Wing(
//					aircraftName,
//					"Wing", // name
//					"Data from AC_ATR_72_REV05.pdf",
//					this.getWing().get_X0().getEstimatedValue(),
//						this.getFuselage().getWidthAtX(this.getWing().get_xLEMacActualBRF().getEstimatedValue()),
//							this.getWing().get_Z0().getEstimatedValue(),
//					ComponentEnum.WING,
//					_theFuselage,
//					_theNacelle,
//					_theHTail,
//					_theVTail
//					);
//
//		}
//
//		_componentsList.add(_theWing);
//		_liftingSurfaceList.add(_theWing);
//	}
//
//	public void createNacelle() {
//		_theNacelle = new Nacelle(
//				"Nacelle",
//				"Data from AC_ATR_72_REV05.pdf",
//				8.61, 4.05, 1.3255
//				);
//
//		_componentsList.add(_theNacelle);
//	}
//
//	public void createNacelles() {
//		_theNacelles = new NacellesManager(this);
//		_theNacelles.initializeNacelles();
//		_theNacelles.setEngines();
//
//		for (int i=0; i < _theNacelles.get_nacellesNumber(); i++){
//			_componentsList.add(_theNacelles.get_nacellesList().get(i));
//		}
//	}
//
//	/**
//	 * Overload of the default creator that recognize aircraft name and creates it's nacelles.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createNacelles(AircraftEnum aircraftName) {
//		_theNacelles = new NacellesManager(aircraftName, this);
//		_theNacelles.initializeNacelles(aircraftName);
//		_theNacelles.setEngines();
//
//		for (int i=0; i < _theNacelles.get_nacellesNumber(); i++){
//			_componentsList.add(_theNacelles.get_nacellesList().get(i));
//		}
//	}
//
//	public void createHTail() {
//		_theHTail =  new HTail(
//				"HTail",
//				"Data taken from ...",
//				24.6,
//				0.0,
//				7.72 - _theFuselage.get_heightFromGround().getEstimatedValue() - _theFuselage.get__diam_C().getEstimatedValue()/2,
//				ComponentEnum.HORIZONTAL_TAIL,
//				_theFuselage,
//				_theNacelle,
//				_theWing,
//				_theVTail
//				);
//
//		_componentsList.add(_theHTail);
//		_liftingSurfaceList.add(_theHTail);
//	}
//
//	/**
//	 * Overload of the H-Tail creator that recognize aircraft name and sets create it's H-tail.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createHTail(AircraftEnum aircraftName) {
//
//		switch(aircraftName) {
//
//		case ATR72:
//			_theHTail =  new HTail(
//					aircraftName,
//					"HTail",
//					"Data taken from ...",
//					24.6,
//					0.0,
//					7.72 - _theFuselage.get_heightFromGround().getEstimatedValue() - _theFuselage.get__diam_C().getEstimatedValue()/2,
//					ComponentEnum.HORIZONTAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theVTail
//					);
//			break;
//
//		case B747_100B:
//			_theHTail =  new HTail(
//					aircraftName,
//					"HTail",
//					"Data taken from REPORT-B747_100B",
//					60.7,
//					0.0,
//					0.4850,
//					ComponentEnum.HORIZONTAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theVTail
//					);
//			break;
//			
//		case AGILE_DC1:
//			_theHTail =  new HTail(
//					aircraftName,
//					"HTail",
//					"Data taken from AGILE_D2.5 doc",
//					29.72,
//					0.0,
//					2.46,
//					ComponentEnum.HORIZONTAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theVTail
//					);
//			break;
//		}
//
//		_componentsList.add(_theHTail);
//		_liftingSurfaceList.add(_theHTail);
//	}
//
//	public void createVTail() {
//		_theVTail =  new VTail(
//				"VTail",
//				"Data taken from ...",
//				21.9,
//				0.0,
//				1.30,
//				ComponentEnum.VERTICAL_TAIL,
//				_theFuselage,
//				_theNacelle,
//				_theWing,
//				_theHTail
//				);
//
//		_componentsList.add(_theVTail);
//		_liftingSurfaceList.add(_theVTail);
//	}
//
//	/**
//	 * Overload of the default creator that recognize the aircraft name and creates a V-tail with it's values.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createVTail(AircraftEnum aircraftName) {
//
//		switch(aircraftName) {
//		case ATR72:
//			_theVTail =  new VTail(
//					aircraftName,
//					"VTail",
//					"Data taken from ...",
//					21.9,
//					0.0,
//					1.30,
//					ComponentEnum.VERTICAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theHTail
//					);
//			break;
//
//		case B747_100B:
//			_theVTail =  new VTail(
//					aircraftName,
//					"VTail",
//					"Data taken from REPORT-B747_100B",
//					56.2900,
//					0.0,
//					2.8150,
//					ComponentEnum.VERTICAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theHTail
//					);
//			break;
//			
//		case AGILE_DC1:
//			_theVTail =  new VTail(
//					aircraftName,
//					"VTail",
//					"Data taken from AGILE_D2.5 doc",
//					28.12,
//					0.0,
//					2.98,
//					ComponentEnum.VERTICAL_TAIL,
//					_theFuselage,
//					_theNacelle,
//					_theWing,
//					_theHTail
//					);
//			break;
//			
//		}
//
//		_componentsList.add(_theVTail);
//		_liftingSurfaceList.add(_theVTail);
//	}
//
//	public void createCanard() {
//		_theCanard =  new Canard(
//				"Canard",
//				"Data taken from ...",
//				21.9,
//				0.0,
//				_theFuselage.get_heightFromGround().getEstimatedValue() + _theFuselage.get__diam_C().getEstimatedValue(),
//				ComponentEnum.CANARD,
//				_theFuselage,
//				_theNacelle,
//				_theWing,
//				_theVTail
//				);
//
//		_componentsList.add(_theCanard);
//		_liftingSurfaceList.add(_theCanard);
//	}
//
//	public void createLandingGear() {
//		_theLandingGear = new LandingGears(
//				"Landing Gear",
//				"ATR 72 Landing Gear",
//				12.5, 0., 0.);
//		_componentsList.add(_theLandingGear);
//	}
//
//	/**
//	 * Overload of the default creator that recognize aircraft name and
//	 * sets it's landing gear data.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createLandingGear(AircraftEnum aircraftName) {
//
//		switch(aircraftName) {
//
//		case ATR72:
//			_theLandingGear = new LandingGears(
//					aircraftName,
//					"Landing Gear",
//					"ATR 72 Landing Gear",
//					12.5, 0., 0.);
//			_componentsList.add(_theLandingGear);
//			break;
//
//		case B747_100B:
//			_theLandingGear = new LandingGears(
//					aircraftName,
//					"Landing Gear",
//					"B747-100B Landing Gear",
//					5., 0., 0.);
//			_componentsList.add(_theLandingGear);
//			break;
//			
//		case AGILE_DC1:
//			_theLandingGear = new LandingGears(
//					aircraftName,
//					"Landing Gear",
//					"AGILE_DC1 Landing Gear",
//					12.5, 0., 0.);
//			_componentsList.add(_theLandingGear);
//			break;
//		}
//	}
//
//	public void createFuelTank() {
//
//		_theFuelTank = new FuelTanks(
//				"Fuel Tank",
//				"ATR 72 Fuel Tank",
//				12.5, 0., 0.);
//		_componentsList.add(_theFuelTank);
//	}
//
//	/**
//	 * Overload of the creator of the fuel tank that recognize aircraft name and sets it's data.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createFuelTank(AircraftEnum aircraftName) {
//
//		switch(aircraftName) {
//		case ATR72:
//			_theFuelTank = new FuelTanks(
//					aircraftName,
//					"Fuel Tank",
//					"ATR 72 Fuel Tank",
//					12.5, 0., 0.);
//			_componentsList.add(_theFuelTank);
//			break;
//
//		case B747_100B:
//			_theFuelTank = new FuelTanks(
//					aircraftName,
//					"Fuel Tank",
//					"B747-100B Fuel Tank",
//					34.14, 14.44, -2.8750);
//			_componentsList.add(_theFuelTank);
//			break;
//			
//		case AGILE_DC1:
//			_theFuelTank = new FuelTanks(
//					aircraftName,
//					"Fuel Tank",
//					"AGILE_DC1 Fuel Tank",
//					12.891, 4.968, -1.782);
//			_componentsList.add(_theFuelTank);
//			break;
//		}
//	}
//
//	public void createPowerPlant() {
//
//		
//		_thePowerPlant = new PowerPlant(
//				"Power plant",
//				"Data taken from ...",
//				0.0, 0.0, 0.0,
//				this);
//		_componentsList.add(_thePowerPlant);}
//	
//
//	/**
//	 * Overload of the default creator that recognize aircraft name and sets it's values.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createPowerPlant(AircraftEnum aircraftName) {
//
//		switch(aircraftName) {
//		case ATR72:
//		_thePowerPlant = new PowerPlant(
//				aircraftName,
//				"Power plant",
//				"Data taken from ...",
//				8.6100, 4.0500, 1.3255,
//				this);
//		_componentsList.add(_thePowerPlant);
//		break;
//
//		case B747_100B:
//			_thePowerPlant = new PowerPlant(
//					aircraftName,
//					"Power plant",
//					"Data taken from ...",
//					0.0, 0.0, 0.0,
//					this);
//			_componentsList.add(_thePowerPlant);
//			break;
//
//		case AGILE_DC1:
//			_thePowerPlant = new PowerPlant(
//					aircraftName,
//					"Power plant",
//					"Data taken from ...",
//					0.0,0.0,0.0,
//					this);
//			_componentsList.add(_thePowerPlant);
//			break;
//		}
//	}
//
//	public void createSystems() {
//		_theSystems = new Systems(
//				"Systems",
//				"Data taken from ...",
//				0.0,
//				0.0,
//				0.0);
//		_componentsList.add(_theSystems);
//
//	}
//
//	/**
//	 * Overload of the default creator that recognize aircraft name and
//	 * sets it's data.
//	 *
//	 * @author Vittorio Trifari
//	 */
//	public void createSystems(AircraftEnum aircraftName) {
//		_theSystems = new Systems(
//				aircraftName,
//				"Systems",
//				"Data taken from ...",
//				0.0,
//				0.0,
//				0.0);
//		_componentsList.add(_theSystems);
//
//	}
//
//	// REPLACED WITH _configuration list
//	private void updateType()
//	{
//		// TO DO: check all expected cases
//
//		if (
//				_theFuselage == null &&
//				_theWing == null &&
//				_theNacelle == null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.EMPTY;
//		}
//		if (
//				_theFuselage != null &&
//				_theWing == null &&
//				_theNacelle == null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.FUSELAGE_ISOLATED;
//		}
//		if (
//				_theFuselage == null &&
//				_theWing != null &&
//				_theNacelle == null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.WING_ISOLATED;
//		}
//		if (
//				_theFuselage != null &&
//				_theWing != null &&
//				_theNacelle == null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.WING_FUSELAGE;
//		}
//		if (
//				_theFuselage != null &&
//				_theWing != null &&
//				_theNacelle != null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_NACELLES;
//		}
//		if (
//				_theFuselage != null &&
//				_theWing != null &&
//				_theNacelle != null &&
//				_theHTail != null
//				)
//		{
//			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_HTAIL_NACELLES;
//		}
//		else
//		{
//			_type = AeroConfigurationTypeEnum.UNKNOWN;
//		}
//		//////////////////////////////////////////
//		// TO DO: update this code as needed
//		//////////////////////////////////////////
//
//	} // end of updateType
//
//
//	private void deleteFuselage() {
//		_theFuselage = null;
//		updateType();
//	}
//
//
//	private void deleteWing()
//	{
//		_theWing = null;
//		updateType();
//	}
//
//
//	private void deleteNacelle()
//	{
//		_theNacelle = null;
//		updateType();
//	}
//
//
//	private void deleteHTail()
//	{
//		_theHTail = null;
//		updateType();
//	}
//
//
//	private void deleteVTail()
//	{
//		_theVTail = null;
//		updateType();
//	}
//
//
//	//////////////////////////////////////////
//	// TO DO: update this code as needed
//	//////////////////////////////////////////
//
//	public AircraftTypeEnum get_typeVehicle() {
//		return _typeVehicle;
//	}
//
//	public void set_typeVehicle(AircraftTypeEnum _typeVehicle) {
//		this._typeVehicle = _typeVehicle;
//	}
//
//	public String get_name() {
//		return _id;
//	}
//
//	public void set_name(String _name) {
//		this._id = _name;
//	}
//
//	public AeroConfigurationTypeEnum get_type() {
//		return _type;
//	}
//
//	/**
//	 * Return the object corresponding to enum constant
//	 *
//	 * @param component
//	 * @return
//	 */
//	public Component get_component(ComponentEnum component) {
//
//		if (component == ComponentEnum.FUSELAGE) {
//			return _theFuselage;
//
//		} else if (component == ComponentEnum.WING) {
//			return _theWing;
//
//		} else if (component == ComponentEnum.HORIZONTAL_TAIL) {
//			return _theHTail;
//
//		} else if (component == ComponentEnum.VERTICAL_TAIL) {
//			return _theVTail;
//
//		} else if (component == ComponentEnum.CANARD) {
//			return _theCanard;
//
//		} else if (component == ComponentEnum.FUEL_TANK) {
//			return _theFuelTank;
//
//		} else if (component == ComponentEnum.NACELLE) {
//			return _theNacelle;
//
//		} else if (component == ComponentEnum.POWER_PLANT) {
//			return _thePowerPlant;
//
//		} else if (component == ComponentEnum.SYSTEMS) {
//			return _theSystems;
//
//		} else if (component == ComponentEnum.LANDING_GEAR) {
//			return _theLandingGear;
//
//		} else  {
//			return null;
//		}
//
//		//		else {
//		//			return _thePropulsion;
//		//		}
//	}
//
	public Fuselage getFuselage() {
		return _theFuselage;
	}
	
	public LiftingSurface getWing() {
		return _theWing;
	}
//
//	public LiftingSurface getHTail() {
//		return _theHTail;
//	}
//
//	public LiftingSurface getVTail() {
//		return _theVTail;
//	}
//
//	public LiftingSurface getCanard() {
//		return _theCanard;
//	}
//
//	public Double getSWetTotal() {
//		return _sWetTotal;
//	}
//
//	public void setSWetTotal(Double sWet) {
//		_sWetTotal = _sWetTotal + sWet;
//	}
//
//	//TODO check if some of these functions must be removed
//	public void addAerocomponent(AeroComponent aercomp) {
//		this._aeroComponents.add(aercomp);
//	}
//	public void removeAerocomponent(AeroComponent aercomp) {
//		this._aeroComponents.remove(aercomp);
//	}
//
//	public void resetAerocomponent() {
//		this._aeroComponents.clear();
//	}
//
//	public List<AeroComponent> getMyComponent() {
//		return _aeroComponents;
//	}
//
//	public String getName() {
//		return _name;
//	}
//
//	public void setName(String name) {
//		this._name = name;
//	}
//
//	public CabinConfiguration get_configuration() {
//		return _theConfiguration;
//	}
//
//
//	public PowerPlant get_powerPlant() {
//		return _thePowerPlant;
//	}
//
//	public ACStructuralCalculatorManager get_structures() {
//		return _theStructures;
//	}
//
//	public Systems get_systems() {
//		return _theSystems;
//	}
//
//	public ACPerformanceManager get_performances() {
//		return _thePerformances;
//	}
//
//	public LandingGears get_landingGear() {
//		return _theLandingGear;
//	}
//
//
//	public ACWeightsManager get_weights() {
//		return _theWeights;
//	}
//
//	public List<Object> getComponentsList() {
//		return _componentsList;
//	}
//
//	public FuelTanks get_theFuelTank() {
//		return _theFuelTank;
//	}
//
//	public void set_theFuelTank(FuelTanks _theFuelTank) {
//		this._theFuelTank = _theFuelTank;
//	}
//
//	public ACBalanceManager get_theBalance() {
//		return _theBalance;
//	}
//
//	public void set_theBalance(ACBalanceManager _theBalance) {
//		this._theBalance = _theBalance;
//	}
//
//	public static String getId() {
//		return _id;
//	}
//
//	public double get_lifeSpan() {
//		return _lifeSpan;
//	}
//
//	public void set_lifeSpan(double _lifeSpan) {
//		this._lifeSpan = _lifeSpan;
//	}
//
//	public NacellesManager get_theNacelles() {
//		return _theNacelles;
//	}
//
//	public List<LiftingSurface> get_liftingSurfaceList() {
//		return _liftingSurfaceList;
//	}
//
//	public ACAerodynamicsManager get_theAerodynamics() {
//		return _theAerodynamics;
//	}
//
//	public Costs get_theCosts() {
//		return _theCosts;
//	}
//
//	public LiftingSurface getExposedWing() {
//		return _exposedWing;
//	}
//
//	public void setExposedWing(LiftingSurface exposedWing) {
//		this._exposedWing = exposedWing;
//	}

} // end of class
