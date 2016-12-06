package aircraft.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.LandingGears.MountingPosition;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.Fuselage.FuselageBuilder;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import analyses.ACAnalysisManager;
import calculators.aerodynamics.DragCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.RegulationsEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import de.dlr.sc.tigl.Tigl.WingCoordinates;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

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
public class Aircraft implements IAircraft {

	private String _id;
	private AeroConfigurationTypeEnum _type = AeroConfigurationTypeEnum.EMPTY;
	private AircraftTypeEnum _typeVehicle;
	private RegulationsEnum _regulations;

	private ACAnalysisManager _theAnalysisManager;
	
	private Fuselage _theFuselage;
	private LiftingSurface _theWing;
	private LiftingSurface _theExposedWing;
	private LiftingSurface _theHTail;
	private LiftingSurface _theVTail;
	private LiftingSurface _theCanard;
	private PowerPlant _thePowerPlant;
	private Nacelles _theNacelles;
	private FuelTank _theFuelTank;
	private LandingGears _theLandingGears;
	private Systems _theSystems;
	private CabinConfiguration _theCabinConfiguration;
	
	private List<Object> _componentsList;
	
	private Amount<Area> _sWetTotal = Amount.valueOf(0.0, SI.SQUARE_METRE);
	private Double _kExcr = 0.0;
	private Amount<Length> _wingACToCGDistance = Amount.valueOf(0.0, SI.METER);
	
	private double _lifeSpan = 14.; //typical life span in year

	//===================================================================================================
	// Builder pattern 
	//===================================================================================================
	public static class AircraftBuilder {
		
		// required parameters
		private String __id;
		private AircraftTypeEnum __typeVehicle;
		private RegulationsEnum __regulations;

		// optional parameters ... defaults
		// ...
		private List<Object> __componentsList = new ArrayList<Object>();
		
		private Fuselage __theFuselage;
		private LiftingSurface __theWing;
		private LiftingSurface __theExposedWing;
		private LiftingSurface __theHTail;
		private LiftingSurface __theVTail;
		private LiftingSurface __theCanard;
		private PowerPlant __thePowerPlant;
		private Nacelles __theNacelles;
		private FuelTank __theFuelTank;
		private LandingGears __theLandingGears;
		private Systems __theSystems;
		private CabinConfiguration __theCabinConfiguration;
		
		private ACAnalysisManager __theAnalysisManager;
		
		public AircraftBuilder (String id, AerodynamicDatabaseReader aeroDatabaseReader, HighLiftDatabaseReader highLiftDatabaseReader) {
			this.__id = id;
			initialize(AircraftEnum.ATR72, aeroDatabaseReader, highLiftDatabaseReader);
		}
		
		public AircraftBuilder (String id, AircraftEnum aircraftName, AerodynamicDatabaseReader aeroDatabaseReader, HighLiftDatabaseReader highLiftDatabaseReader) {
			this.__id = id;
			initialize(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
		}
		
		private void initialize(AircraftEnum aircraftName, AerodynamicDatabaseReader aeroDatabaseReader, HighLiftDatabaseReader highLiftDatabaseReader) {

			switch(aircraftName) {
			case ATR72:
				__id = "ATR-72";
				__typeVehicle = AircraftTypeEnum.TURBOPROP;
				__regulations = RegulationsEnum.FAR_25;
				__theCabinConfiguration = new CabinConfiguration.ConfigurationBuilder("Aircraft cabin configuration", AircraftEnum.ATR72).build();
				
				__componentsList.clear();
				
				createFuselage(aircraftName);
				__theFuselage.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				
				createWing(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theWing.setXApexConstructionAxes(Amount.valueOf(11.0, SI.METER));
				__theWing.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setZApexConstructionAxes(Amount.valueOf(1.6, SI.METER));
				__theWing.setRiggingAngle(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
				
				createVTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theVTail.setXApexConstructionAxes(Amount.valueOf(21.6, SI.METER));
				__theVTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setZApexConstructionAxes(Amount.valueOf(1.3, SI.METER));
				__theVTail.setRiggingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				createHTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theHTail.setXApexConstructionAxes(Amount.valueOf(25.3, SI.METER));
				__theHTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setZApexConstructionAxes(Amount.valueOf(5.7374, SI.METER));
				__theHTail.setRiggingAngle(Amount.valueOf(1.0, NonSI.DEGREE_ANGLE));
						
				createPowerPlant(aircraftName);
				
				createNacelles(aircraftName);
				
				createFuelTank();
				__theFuelTank.setXApexConstructionAxes(__theWing.getXApexConstructionAxes()
															.plus(__theWing.getChordRoot()
																	.times(__theWing.getLiftingSurfaceCreator()
																						.getMainSparNonDimensionalPosition()
																						)
																	)
															);
				__theFuelTank.setYApexConstructionAxes(__theWing.getYApexConstructionAxes());
				__theFuelTank.setZApexConstructionAxes(__theWing.getZApexConstructionAxes());
				
				createLandingGears(aircraftName);
				__theLandingGears.setXApexConstructionAxes(Amount.valueOf(12.5, SI.METER));
				__theLandingGears.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setMountingPosition(LandingGears.MountingPosition.FUSELAGE);
				
				createSystems(aircraftName);
				__theSystems.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));

				break;

		    // TODO : COMPLETE THIS!
			case B747_100B:
				__id = "B747-100B";
				__typeVehicle = AircraftTypeEnum.JET;
				__regulations = RegulationsEnum.FAR_25;
				__theCabinConfiguration = new CabinConfiguration
						.ConfigurationBuilder("Aircraft cabin configuration", AircraftEnum.B747_100B)
							.build();
				
				__componentsList.clear();
				
				createFuselage(aircraftName);
				__theFuselage.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				
				createWing(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theWing.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setRiggingAngle(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
				
				createHTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theHTail.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setRiggingAngle(Amount.valueOf(1.0, NonSI.DEGREE_ANGLE));
				
				createVTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theVTail.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setRiggingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				createPowerPlant(aircraftName);
				
				createNacelles(aircraftName);
				
				createFuelTank();
				__theFuelTank.setXApexConstructionAxes(__theWing.getXApexConstructionAxes()
															.plus(__theWing.getChordRoot()
																	.times(__theWing.getLiftingSurfaceCreator()
																						.getMainSparNonDimensionalPosition()
																						)
																	)
															);
				__theFuelTank.setYApexConstructionAxes(__theWing.getYApexConstructionAxes());
				__theFuelTank.setZApexConstructionAxes(__theWing.getZApexConstructionAxes());
				
				createLandingGears(aircraftName);
				__theLandingGears.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setMountingPosition(LandingGears.MountingPosition.WING);
				
				createSystems(aircraftName);
				__theSystems.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));

				break;
				
			// TODO : COMPLETE THIS!
			case AGILE_DC1:
				__id = "AGILE DC-1";
				__typeVehicle = AircraftTypeEnum.JET;
				__regulations = RegulationsEnum.FAR_25;
				__theCabinConfiguration = new CabinConfiguration
						.ConfigurationBuilder("Aircraft cabin configuration", AircraftEnum.AGILE_DC1)
							.build();
				
				__componentsList.clear();
				
				createFuselage(aircraftName);
				__theFuselage.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theFuselage.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				
				createWing(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theWing.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theWing.setRiggingAngle(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
				
				createHTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theHTail.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theHTail.setRiggingAngle(Amount.valueOf(1.0, NonSI.DEGREE_ANGLE));
				
				createVTail(aircraftName, aeroDatabaseReader, highLiftDatabaseReader);
				__theVTail.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theVTail.setRiggingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				createPowerPlant(aircraftName);
				
				createNacelles(aircraftName);
				
				createFuelTank();
				__theFuelTank.setXApexConstructionAxes(__theWing.getXApexConstructionAxes()
															.plus(__theWing.getChordRoot()
																	.times(__theWing.getLiftingSurfaceCreator()
																						.getMainSparNonDimensionalPosition()
																						)
																	)
															);
				__theFuelTank.setYApexConstructionAxes(__theWing.getYApexConstructionAxes());
				__theFuelTank.setZApexConstructionAxes(__theWing.getZApexConstructionAxes());
				
				createLandingGears(aircraftName);
				__theLandingGears.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theLandingGears.setMountingPosition(LandingGears.MountingPosition.WING);
				
				createSystems(aircraftName);
				__theSystems.setXApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setYApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				__theSystems.setZApexConstructionAxes(Amount.valueOf(0.0, SI.METER));
				
				break;
			default:
				break;
			
			}
		}
		
		private void createFuselage(AircraftEnum aircraftName) {
			__theFuselage = new FuselageBuilder("Fuselage")
					.fuselageCreator(
							new FuselageCreator
								.FuselageBuilder("Fuselage", aircraftName)
									.build()
							)
					.build();
			
			__theFuselage.getFuselageCreator().calculateGeometry();
			__componentsList.add(__theFuselage);
		}
		
		private void createWing(
				AircraftEnum aircraftName,
				AerodynamicDatabaseReader aeroDatabaseReader,
				HighLiftDatabaseReader highLiftDatabaseReader) {

			__theWing = new LiftingSurfaceBuilder("Wing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(
							new LiftingSurfaceCreator
								.LiftingSurfaceCreatorBuilder("Wing", Boolean.TRUE, aircraftName, ComponentEnum.WING)
									.build()
							)
					.build();
			
			__theWing.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			__componentsList.add(__theWing);
		}
		
		private void createHTail(
				AircraftEnum aircraftName,
				AerodynamicDatabaseReader aeroDatabaseReader,
				HighLiftDatabaseReader highLiftDatabaseReader) {

			__theHTail = new LiftingSurfaceBuilder("Horizontal tail", ComponentEnum.HORIZONTAL_TAIL, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(
							new LiftingSurfaceCreator
								.LiftingSurfaceCreatorBuilder("Horizontal tail", Boolean.TRUE, aircraftName, ComponentEnum.HORIZONTAL_TAIL)
									.build()
							)
					.build();
			
			__theHTail.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			__componentsList.add(__theHTail);
		}
		
		private void createVTail(
				AircraftEnum aircraftName,
				AerodynamicDatabaseReader aeroDatabaseReader,
				HighLiftDatabaseReader highLiftDatabaseReader) {

			__theVTail = new LiftingSurfaceBuilder("Vertical tail", ComponentEnum.VERTICAL_TAIL, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(
							new LiftingSurfaceCreator
								.LiftingSurfaceCreatorBuilder("Vertical tail", Boolean.FALSE, aircraftName, ComponentEnum.VERTICAL_TAIL)
									.build()
							)
					.build();
			
			__theVTail.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			__componentsList.add(__theVTail);
		}
		
		@SuppressWarnings("unused")
		private void createCanard(
				AircraftEnum aircraftName,
				AerodynamicDatabaseReader aeroDatabaseReader,
				HighLiftDatabaseReader highLiftDatabaseReader) {

			__theCanard = new LiftingSurfaceBuilder("Canard", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(
							new LiftingSurfaceCreator
								.LiftingSurfaceCreatorBuilder("Canard", Boolean.TRUE, aircraftName, ComponentEnum.CANARD)
									.build()
							)
					.build();
			
			__theCanard.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			__componentsList.add(__theCanard);
		}
		
		private void createPowerPlant(AircraftEnum aircraftName) {
			__thePowerPlant = new PowerPlant.PowerPlantBuilder("Power Plant", aircraftName).build();
			__componentsList.add(__thePowerPlant);
		}
		
		private void createNacelles(AircraftEnum aircraftName) {
			__theNacelles = new Nacelles.NacellesBuilder("Nacelles", aircraftName).build();
			__componentsList.add(__theNacelles);
		}
		
		private void createFuelTank() {
			__theFuelTank = new FuelTank.FuelTankBuilder("Fuel Tank", __theWing).build();
			__componentsList.add(__theFuelTank);
		}
		
		private void createLandingGears(AircraftEnum aircraftName) {
			__theLandingGears = new LandingGears.LandingGearsBuilder("Landing Gears", aircraftName).build();
			__componentsList.add(__theLandingGears);
		}
		
		private void createSystems(AircraftEnum aircraftName) {
			__theSystems = new Systems.SystemsBuilder("Systems", aircraftName).build();
			__componentsList.add(__theSystems);
		}
		
		public AircraftBuilder name(String id) {
			this.__id = id;
			return this;
		}
		
		public AircraftBuilder aircraftType(AircraftTypeEnum aircraftType) {
			this.__typeVehicle = aircraftType;
			return this;
		}
		
		public AircraftBuilder regulations(RegulationsEnum regulations) {
			this.__regulations = regulations;
			return this;
		}
		
		public AircraftBuilder fuselage(Fuselage fuselage) {
			this.__theFuselage = fuselage;
			return this;
		}
		
		public AircraftBuilder xApexFuselage(Amount<Length> xApex) {
			if(__theFuselage != null)
				this.__theFuselage.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexFuselage(Amount<Length> yApex) {
			if(__theFuselage != null)
				this.__theFuselage.setYApexConstructionAxes(yApex);
			return this;
		}
		
		public AircraftBuilder zApexFuselage(Amount<Length> zApex) {
			if(__theFuselage != null)
				this.__theFuselage.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder wing(LiftingSurface wing) {
			this.__theWing = wing;
			return this;
		}
		
		public AircraftBuilder xApexWing(Amount<Length> xApex) {
			if(__theWing != null)
				this.__theWing.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexWing(Amount<Length> yApex) {
			if(__theWing != null)
				this.__theWing.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexWing(Amount<Length> zApex) {
			if(__theWing != null)
				this.__theWing.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder horizontalTail(LiftingSurface hTail) {
			this.__theHTail = hTail;
			return this;
		}
		
		public AircraftBuilder xApexHTail(Amount<Length> xApex) {
			if(__theHTail != null)
				this.__theHTail.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexHTail(Amount<Length> yApex) {
			if(__theHTail != null)
				this.__theHTail.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexHTail(Amount<Length> zApex) {
			if(__theHTail != null)
				this.__theHTail.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder verticalTail(LiftingSurface vTail) {
			this.__theVTail = vTail;
			return this;
		}
		
		public AircraftBuilder xApexVTail(Amount<Length> xApex) {
			if(__theVTail != null)
				this.__theVTail.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexVTail(Amount<Length> yApex) {
			if(__theVTail != null)
				this.__theVTail.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexVTail(Amount<Length> zApex) {
			if(__theVTail != null)
				this.__theVTail.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder canard(LiftingSurface canard) {
			this.__theCanard = canard;
			return this;
		}
		
		public AircraftBuilder xApexCanard(Amount<Length> xApex) {
			if(__theCanard != null)
				this.__theCanard.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexCanard(Amount<Length> yApex) {
			if(__theCanard != null)
				this.__theCanard.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexCanard(Amount<Length> zApex) {
			if(__theCanard != null)
				this.__theCanard.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder powerPlant (PowerPlant powerPlant) {
			this.__thePowerPlant = powerPlant;
			return this;
		}
		
		public AircraftBuilder nacelles (Nacelles nacelles) {
			this.__theNacelles = nacelles;
			return this;
		}
		
		public AircraftBuilder fuelTank (FuelTank fuelTanks) {
			this.__theFuelTank = fuelTanks;
			return this;
		}
		
		public AircraftBuilder xApexFuelTank(Amount<Length> xApex) {
			this.__theFuelTank.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexFuelTank(Amount<Length> yApex) {
			this.__theFuelTank.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexFuelTank(Amount<Length> zApex) {
			this.__theFuelTank.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder landingGears (LandingGears landingGears) {
			this.__theLandingGears = landingGears;
			return this;
		}
		
		public AircraftBuilder xApexLandingGears(Amount<Length> xApex) {
			if(__theLandingGears != null)
				this.__theLandingGears.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexLandingGears(Amount<Length> yApex) {
			if(__theLandingGears != null)
				this.__theLandingGears.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexLandingGears(Amount<Length> zApex) {
			if(__theLandingGears != null)
				this.__theLandingGears.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder landingGearsMountingPosition(LandingGears.MountingPosition mountingPosition) {
			if(__theLandingGears != null)
				this.__theLandingGears.setMountingPosition(mountingPosition);
			return this;
		}
		
		public AircraftBuilder systems (Systems systems) {
			this.__theSystems = systems;
			return this;
		}
		
		public AircraftBuilder xApexSystems(Amount<Length> xApex) {
			if(__theSystems != null)
				this.__theSystems.setXApexConstructionAxes(xApex);
			return this;
		}
		
		public AircraftBuilder yApexSystem(Amount<Length> yApex) {
			if(__theSystems != null)
				this.__theSystems.setYApexConstructionAxes(yApex);
			return this;
		}
	
		public AircraftBuilder zApexSystems(Amount<Length> zApex) {
			if(__theSystems != null)
				this.__theSystems.setZApexConstructionAxes(zApex);
			return this;
		}
		
		public AircraftBuilder cabinConfiguration (CabinConfiguration cabinConfiguration) {
			this.__theCabinConfiguration = cabinConfiguration;
			return this;
		}
		
		public Aircraft build() {
			return new Aircraft(this);
		}
	}
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================

	public Aircraft(AircraftBuilder builder) {
		
		this._id = builder.__id;
		this._typeVehicle = builder.__typeVehicle;
		this._regulations = builder.__regulations;
		
		this._theAnalysisManager = builder.__theAnalysisManager;
		this._theCabinConfiguration = builder.__theCabinConfiguration;
		
		this._theFuselage = builder.__theFuselage;
		this._theWing = builder.__theWing;
		this._theExposedWing = builder.__theExposedWing;
		this._theHTail = builder.__theHTail;
		this._theVTail = builder.__theVTail;
		this._theCanard = builder.__theCanard;
		this._thePowerPlant = builder.__thePowerPlant;
		this._theNacelles = builder.__theNacelles;
		this._theFuelTank = builder.__theFuelTank;
		this._theLandingGears = builder.__theLandingGears;
		this._theSystems = builder.__theSystems;
		
		this._componentsList = builder.__componentsList;
		
		//-------------------------------------------------------------		
		if(_thePowerPlant != null) {
			if(_theWing != null) {
				int indexOfEngineUpperWing = 0;
				for(int i=0; i<_thePowerPlant.getEngineNumber(); i++) {
					if(_thePowerPlant.getEngineList().get(i).getMountingPosition() == EngineMountingPositionEnum.WING)
						if(_thePowerPlant.getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
								> _theWing.getZApexConstructionAxes().doubleValue(SI.METER))
							indexOfEngineUpperWing += 1;
				}
				_theWing.setNumberOfEngineOverTheWing(indexOfEngineUpperWing);
			}
		}
		//-------------------------------------------------------------
		updateType();
		if((this._theFuselage != null) && (this._theWing != null)) { 
			calculateExposedWing(_theWing, _theFuselage);
			this._theWing.setExposedWing(this._theExposedWing);
			this._theWing.getLiftingSurfaceCreator().setSurfaceWettedExposed(
					this._theExposedWing.getLiftingSurfaceCreator().getSurfaceWetted()
					);
		}
		else
			this._theWing.getLiftingSurfaceCreator().setSurfaceWettedExposed(
					this._theWing.getLiftingSurfaceCreator().getSurfaceWetted()
					);
		
		// setup the positionRelativeToAttachment variable
		if(_theWing != null)
			this._theWing.setPositionRelativeToAttachment(
					(_theFuselage.getFuselageCreator().getSectionCylinderHeight().divide(2))
					.plus(_theWing.getZApexConstructionAxes())
					.divide(_theFuselage
							.getFuselageCreator()
							.getSectionCylinderHeight()
							)
					.getEstimatedValue()
					);
		
		if(_theHTail != null)
			this._theHTail.setPositionRelativeToAttachment(
					(_theHTail.getZApexConstructionAxes()
							.minus(_theVTail
									.getZApexConstructionAxes()
									)
							).divide(_theVTail.getSpan())
					.getEstimatedValue()
					);
		
		if(_theVTail != null)
			this._theVTail.setPositionRelativeToAttachment(0.0);
		
		if(_theCanard != null)
			this._theCanard.setPositionRelativeToAttachment(
					(_theFuselage.getFuselageCreator().getSectionCylinderHeight().divide(2))
					.plus(_theCanard.getZApexConstructionAxes())
					.divide(_theFuselage
							.getFuselageCreator()
							.getSectionCylinderHeight()
							)
					.getEstimatedValue()
					);
		
		//----------------------------------------
		if(_theWing != null)
			calculateLiftingSurfaceACToWingACdistance(this._theWing);
		if(_theHTail != null)
			calculateLiftingSurfaceACToWingACdistance(this._theHTail);
		if(_theVTail != null)
			calculateLiftingSurfaceACToWingACdistance(this._theVTail);
		if(_theCanard != null)
			calculateLiftingSurfaceACToWingACdistance(this._theCanard);

		//----------------------------------------
		calculateSWetTotal();
		calculateKExcrescences();
	}
	
	private void updateType() {

		if (
				_theFuselage == null &&
				_theWing == null &&
				_theNacelles == null
				) 
			_type = AeroConfigurationTypeEnum.EMPTY;
		
		else if (
				_theFuselage != null &&
				_theWing == null &&
				_theNacelles == null
				)
			_type = AeroConfigurationTypeEnum.FUSELAGE_ISOLATED;
		
		else if (
				_theFuselage == null &&
				_theWing != null &&
				_theNacelles == null
				)
			_type = AeroConfigurationTypeEnum.WING_ISOLATED;
		
		else if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelles == null
				)
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE;
		
		else if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelles != null
				)
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_NACELLES;
		
		else if (
				_theFuselage != null &&
				_theWing != null &&
				_theNacelles != null &&
				_theHTail != null
				)
			_type = AeroConfigurationTypeEnum.WING_FUSELAGE_HTAIL_NACELLES;
		
		else
			_type = AeroConfigurationTypeEnum.UNKNOWN;
		
	} // end of updateType

	private void calculateExposedWing(LiftingSurface theWing, Fuselage theFuselage) {
		
		Amount<Length> sectionWidthAtZ = Amount.valueOf(
				0.5 * theFuselage.getFuselageCreator()
				.getSectionWidthAtZ(
						theWing.getZApexConstructionAxes()
						.doubleValue(SI.METER)),
				SI.METER);

		Amount<Length> chordRootExposed = Amount.valueOf(
				theWing.getChordAtYActual(sectionWidthAtZ.doubleValue(SI.METER)),
				SI.METER
				);
		AirfoilCreator exposedWingRootAirfoil = LiftingSurface.calculateAirfoilAtY(
				theWing,
				sectionWidthAtZ.doubleValue(SI.METER)
				);
		exposedWingRootAirfoil.setXCoords(theWing.getAirfoilList().get(0).getAirfoilCreator().getXCoords());
		exposedWingRootAirfoil.setZCoords(theWing.getAirfoilList().get(0).getAirfoilCreator().getZCoords());
		
		Amount<Length> exposedWingFirstPanelSpan = theWing.getLiftingSurfaceCreator()
				.getPanels().get(0)
				.getSpan()
				.minus(sectionWidthAtZ);	

		LiftingSurfacePanelCreator exposedWingFirstPanel = new LiftingSurfacePanelCreator
				.LiftingSurfacePanelBuilder(
						"Exposed wing first panel",
						chordRootExposed,
						theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordTip(),
						exposedWingRootAirfoil,
						theWing.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilTip(),
						theWing.getLiftingSurfaceCreator().getPanels().get(0).getTwistGeometricAtTip(),
						exposedWingFirstPanelSpan,
						theWing.getLiftingSurfaceCreator().getPanels().get(0).getSweepLeadingEdge(),
						theWing.getLiftingSurfaceCreator().getPanels().get(0).getDihedral())
				.build();

		List<LiftingSurfacePanelCreator> exposedWingPanels = new ArrayList<LiftingSurfacePanelCreator>();

		exposedWingPanels.add(exposedWingFirstPanel);

		for(int i=1; i<theWing.getLiftingSurfaceCreator().getPanels().size(); i++)
			exposedWingPanels.add(theWing.getLiftingSurfaceCreator().getPanels().get(i));

		this._theExposedWing = new LiftingSurfaceBuilder("Exposed wing", ComponentEnum.WING, theWing.getAerodynamicDatabaseReader(), theWing.getHighLiftDatabaseReader())
				.liftingSurfaceCreator(
						new LiftingSurfaceCreator
							.LiftingSurfaceCreatorBuilder("Exposed wing", Boolean.TRUE, ComponentEnum.WING)
								.build()
						)
				.build();
		this._theExposedWing.getLiftingSurfaceCreator().getPanels().clear();
		this._theExposedWing.getLiftingSurfaceCreator().setPanels(exposedWingPanels);
		this._theExposedWing.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, Boolean.TRUE);
		this._theExposedWing.populateAirfoilList(
				theWing.getAerodynamicDatabaseReader(),
				Boolean.FALSE);
		
		this._theExposedWing.setXApexConstructionAxes(theWing.getXApexConstructionAxes());
		this._theExposedWing.setYApexConstructionAxes(Amount.valueOf(
				0.5 * theFuselage.getFuselageCreator().getSectionWidthAtZ(
						theWing.getZApexConstructionAxes().doubleValue(SI.METER)),
				SI.METER)
				);
		this._theExposedWing.setZApexConstructionAxes(theWing.getZApexConstructionAxes());
		this._theExposedWing.setRiggingAngle(theWing.getRiggingAngle());

	_componentsList.add(_theExposedWing);
	
	}

	public void calculateSWetTotal() {
		
		if(this._theFuselage != null)
			this._sWetTotal = this._sWetTotal.plus(this._theFuselage.getsWet());
		
		if(this._theExposedWing != null)
			this._sWetTotal = this._sWetTotal.plus(this._theExposedWing.getLiftingSurfaceCreator().getSurfaceWetted());
			
		// TODO : FOR HTAIL, VTAIL AND CANARD THE EXPOSED WING IS NOT CALCULATED ... IS THIS ACCEPTABLE?
		if(this._theHTail != null)
			this._sWetTotal = this._sWetTotal.plus(this._theHTail.getLiftingSurfaceCreator().getSurfaceWetted());
		
		if(this._theVTail != null)
			this._sWetTotal = this._sWetTotal.plus(this._theVTail.getLiftingSurfaceCreator().getSurfaceWetted());
		
		if(this._theCanard != null)
			this._sWetTotal = this._sWetTotal.plus(this._theCanard.getLiftingSurfaceCreator().getSurfaceWetted());
			
		if(this._theNacelles != null)
			this._sWetTotal = this._sWetTotal.plus(this._theNacelles.getSurfaceWetted());
		
	}
	
	public void calculateKExcrescences() {
		_kExcr = DragCalc.calculateKExcrescences(getSWetTotal().doubleValue(SI.SQUARE_METRE));
		
		if(this._theFuselage != null)
			this._theFuselage.setKExcr(_kExcr);
		
		if(this._theWing != null)
			this._theWing.setKExcr(_kExcr);
		
		if(this._theHTail != null)
			this._theHTail.setKExcr(_kExcr);
		
		if(this._theVTail != null)
			this._theVTail.setKExcr(_kExcr);
		
		if(this._theCanard != null)
			this._theCanard.setKExcr(_kExcr);
		
		if(this._theNacelles != null)
			this._theNacelles.setKExcr(_kExcr);
	}
	
	public void calculateArms(LiftingSurface theLiftingSurface, Amount<Length> xcgMTOM){
		
		if(theLiftingSurface.getType() == ComponentEnum.WING) {
			calculateAircraftCGToWingACdistance(xcgMTOM);
			theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
					getWingACToCGDistance()
					);
		}
		else if( // case CG behind AC wing
				xcgMTOM.doubleValue(SI.METER) > 
				(_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
						.plus(_theWing.getXApexConstructionAxes()).getEstimatedValue() + 
							_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()*0.25)
				) {
			
			if((theLiftingSurface.getType() == ComponentEnum.HORIZONTAL_TAIL)
					|| (theLiftingSurface.getType() == ComponentEnum.VERTICAL_TAIL)) {
			
				calculateAircraftCGToWingACdistance(xcgMTOM);
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.minus(getWingACToCGDistance())
						);
			}
			else if (theLiftingSurface.getType() == ComponentEnum.CANARD) {
				calculateAircraftCGToWingACdistance(xcgMTOM);
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.plus(getWingACToCGDistance())
						);
			}
		}
		else if( // case AC wing behind CG
				xcgMTOM.doubleValue(SI.METER) <= 
				(_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
						.plus(_theWing.getXApexConstructionAxes()).getEstimatedValue() + 
							_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()*0.25)
				) {
			if((theLiftingSurface.getType() == ComponentEnum.HORIZONTAL_TAIL)
					|| (theLiftingSurface.getType() == ComponentEnum.VERTICAL_TAIL)) {
			
				calculateAircraftCGToWingACdistance(xcgMTOM);
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.plus(getWingACToCGDistance())
						);
			}
			else if (theLiftingSurface.getType() == ComponentEnum.CANARD) {
				calculateAircraftCGToWingACdistance(xcgMTOM);
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.minus(getWingACToCGDistance())
						);
			}
		}
	}

	private void calculateAircraftCGToWingACdistance(Amount<Length> xCGMTOM){
		_wingACToCGDistance = Amount.valueOf(
				Math.abs(
						xCGMTOM.doubleValue(SI.METER) -
						(_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
								.plus(_theWing.getXApexConstructionAxes()).getEstimatedValue() + 
									_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()*0.25)
						), 
				SI.METER);
	}

	private void calculateLiftingSurfaceACToWingACdistance(LiftingSurface theLiftingSurface) {
		theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceACTOWingACDistance(
				Amount.valueOf(
						Math.abs(
								theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
								.plus(theLiftingSurface.getXApexConstructionAxes())
								.getEstimatedValue() + 
								theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()*0.25 - 
								(_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
										.plus(_theWing.getXApexConstructionAxes()).getEstimatedValue() + 
										0.25*_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue())
								),
						SI.METER)
				);
	}

	private void calculateVolumetricRatio(LiftingSurface theLiftingSurface) {
		
		if ((theLiftingSurface.getType() == ComponentEnum.HORIZONTAL_TAIL)
				|| (theLiftingSurface.getType() == ComponentEnum.CANARD)) {
			theLiftingSurface.getLiftingSurfaceCreator().setVolumetricRatio(
					(theLiftingSurface.getSurface().divide(_theWing.getSurface()))
					.times(theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.divide(_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord()))
			.getEstimatedValue()
			);
		} 
		else if(theLiftingSurface.getType() == ComponentEnum.VERTICAL_TAIL) {
			theLiftingSurface.getLiftingSurfaceCreator().setVolumetricRatio(
					(theLiftingSurface.getSurface().divide(_theWing.getSurface()))
					.times(theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance()
							.divide(_theWing.getSpan()))
					.getEstimatedValue()
					);
		}
	}
	
	public static Aircraft importFromXML (String pathToXML,
									      String liftingSurfacesDir,
									      String fuselagesDir,
									      String engineDir,
									      String nacelleDir,
									      String landingGearsDir,
									      String systemsDir,
									      String cabinConfigurationDir,
									      String airfoilsDir,
									      AerodynamicDatabaseReader aeroDatabaseReader,
									      HighLiftDatabaseReader highLiftDatabaseReader) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading aircraft data from file ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		AircraftTypeEnum type;
		String typeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		if(typeProperty.equalsIgnoreCase("TURBOPROP"))
			type = AircraftTypeEnum.TURBOPROP;
		else if(typeProperty.equalsIgnoreCase("BUSINESS_JET"))
			type = AircraftTypeEnum.BUSINESS_JET;
		else if(typeProperty.equalsIgnoreCase("JET"))
			type = AircraftTypeEnum.JET;
		else if(typeProperty.equalsIgnoreCase("GENERAL_AVIATION"))
			type = AircraftTypeEnum.GENERAL_AVIATION;
		else if(typeProperty.equalsIgnoreCase("FIGHTER"))
			type = AircraftTypeEnum.FIGHTER;
		else if(typeProperty.equalsIgnoreCase("ACROBATIC"))
			type = AircraftTypeEnum.ACROBATIC;
		else if(typeProperty.equalsIgnoreCase("COMMUTER"))
			type = AircraftTypeEnum.COMMUTER;
		else {
			System.err.println("INVALID AIRCRAFT TYPE !!!");
			return null;
		}
		
		RegulationsEnum regulations;
		String regulationsProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		if(regulationsProperty.equalsIgnoreCase("FAR_23"))
			regulations = RegulationsEnum.FAR_23;
		else if(regulationsProperty.equalsIgnoreCase("FAR_25"))
			regulations = RegulationsEnum.FAR_25;
		else {
			System.err.println("INVALID AIRCRAFT REGULATIONS TYPE !!!");
			return null;
		}
		
		//---------------------------------------------------------------------------------
		// FUSELAGE
		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		
		Fuselage theFuselage = null;
		Amount<Length> xApexFuselage = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexFuselage = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexFuselage = Amount.valueOf(0.0, SI.METER);
		
		if(fuselageFileName != null) {
			String fuselagePath = fuselagesDir + File.separator + fuselageFileName;
			FuselageCreator fuselageCreator = FuselageCreator.importFromXML(fuselagePath);
			theFuselage = new FuselageBuilder("MyFuselage")
					.fuselageCreator(fuselageCreator)
						.build();
			
			theFuselage.getFuselageCreator().calculateGeometry();
			
			xApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/x");
			yApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/y");
			zApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/z");
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION
		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");
		
		CabinConfiguration theCabinConfiguration = null;
		if(cabinConfigrationFileName != null) {
			String cabinConfigurationPath = cabinConfigurationDir + File.separator + cabinConfigrationFileName;
			theCabinConfiguration = CabinConfiguration.importFromXML(cabinConfigurationPath);
		}
		
		//---------------------------------------------------------------------------------
		// WING
		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		
		LiftingSurface theWing = null;
		Amount<Length> xApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleWing = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(wingFileName != null) {
			String wingPath = liftingSurfacesDir + File.separator + wingFileName;
			LiftingSurfaceCreator wingCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, wingPath, airfoilsDir);
			theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(wingCreator)
						.build();

			theWing.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, Boolean.TRUE);
			theWing.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			
			xApexWing = reader.getXMLAmountLengthByPath("//wing/position/x");
			yApexWing = reader.getXMLAmountLengthByPath("//wing/position/y");
			zApexWing = reader.getXMLAmountLengthByPath("//wing/position/z");
			riggingAngleWing = reader.getXMLAmountAngleByPath("//wing/rigging_angle");
			theWing.setRiggingAngle(riggingAngleWing);
		}
		
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL
		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		
		LiftingSurface theHorizontalTail = null;
		Amount<Length> xApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleHTail = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(hTailFileName != null) {
			String hTailPath = liftingSurfacesDir + File.separator + hTailFileName;
			LiftingSurfaceCreator hTailCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.HORIZONTAL_TAIL, hTailPath, airfoilsDir);
			theHorizontalTail = new LiftingSurfaceBuilder("MyHorizontalTail", ComponentEnum.HORIZONTAL_TAIL, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(hTailCreator)
						.build();
		
			theHorizontalTail.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.HORIZONTAL_TAIL, Boolean.TRUE);
			theHorizontalTail.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			
			xApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/x");
			yApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/y");
			zApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/z");
			riggingAngleHTail = reader.getXMLAmountAngleByPath("//horizontal_tail/rigging_angle");
			theHorizontalTail.setRiggingAngle(riggingAngleHTail);
		}
		
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL
		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		
		LiftingSurface theVerticalTail = null;
		Amount<Length> xApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleVTail = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(vTailFileName != null) {
			String vTailPath = liftingSurfacesDir + File.separator + vTailFileName;
			LiftingSurfaceCreator vTailCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.VERTICAL_TAIL, vTailPath, airfoilsDir);
			theVerticalTail = new LiftingSurfaceBuilder("MyVerticalTail", ComponentEnum.VERTICAL_TAIL, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(vTailCreator)
						.build();

			theVerticalTail.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.VERTICAL_TAIL, Boolean.FALSE);
			theVerticalTail.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			
			xApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/x");
			yApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/y");
			zApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/z");
			riggingAngleVTail = reader.getXMLAmountAngleByPath("//vertical_tail/rigging_angle");
			theVerticalTail.setRiggingAngle(riggingAngleVTail);
		}
		
		//---------------------------------------------------------------------------------
		// CANARD
		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		
		LiftingSurface theCanard = null;
		Amount<Length> xApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleCanard = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(canardFileName != null) {
			String canardPath = liftingSurfacesDir + File.separator + canardFileName;
			LiftingSurfaceCreator canardCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.CANARD, canardPath, airfoilsDir);
			theCanard = new LiftingSurfaceBuilder("MyCanard", ComponentEnum.CANARD, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(canardCreator)
						.build();
			
			theCanard.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.CANARD, Boolean.TRUE);
			theCanard.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			
			xApexCanard = reader.getXMLAmountLengthByPath("//canard/position/x");
			yApexCanard = reader.getXMLAmountLengthByPath("//canard/position/y");
			zApexCanard = reader.getXMLAmountLengthByPath("//canard/position/z");
			riggingAngleCanard = reader.getXMLAmountAngleByPath("//canard/rigging_angle");
			theCanard.setRiggingAngle(riggingAngleCanard);
		}
		
		//---------------------------------------------------------------------------------
		// POWER PLANT
		List<Engine> engineList = new ArrayList<Engine>();
		
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");

		List<String> xApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/x");
		List<String> yApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/y");
		List<String> zApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/z");		
		List<String> tiltingAngleListProperties = reader.getXMLPropertiesByPath("//engine/tilting_angle");
		List<String> mountingPointListProperties = reader.getXMLPropertiesByPath("//engine/mounting_point");

		List<Amount<Length>> xApexPowerPlantList = new ArrayList<>();
		List<Amount<Length>> yApexPowerPlantList = new ArrayList<>();
		List<Amount<Length>> zApexPowerPlantList = new ArrayList<>();
		List<Amount<Angle>> tiltingAngleList = new ArrayList<>();
		List<EngineMountingPositionEnum> mountingPointList = new ArrayList<>();
		
		for(int i=0; i<mountingPointListProperties.size(); i++) {
			if(mountingPointListProperties.get(i).equalsIgnoreCase("AFT_FUSELAGE"))
				mountingPointList.add(EngineMountingPositionEnum.AFT_FUSELAGE);
			else if(mountingPointListProperties.get(i).equalsIgnoreCase("BURIED"))
				mountingPointList.add(EngineMountingPositionEnum.BURIED);
			else if(mountingPointListProperties.get(i).equalsIgnoreCase("REAR_FUSELAGE"))
				mountingPointList.add(EngineMountingPositionEnum.REAR_FUSELAGE);
			else if(mountingPointListProperties.get(i).equalsIgnoreCase("WING"))
				mountingPointList.add(EngineMountingPositionEnum.WING);
			else if(mountingPointListProperties.get(i).equalsIgnoreCase("HTAIL"))
				mountingPointList.add(EngineMountingPositionEnum.HTAIL);
			else {
				System.err.println("INVALID ENGINE MOUNTING POSITION !!! ");
				return null;
			}
			
			xApexPowerPlantList.add(
					Amount.valueOf(
							Double.valueOf(
									xApexPowerPlantListProperties.get(i)
									),
							SI.METER)
					);
			yApexPowerPlantList.add(
					Amount.valueOf(
							Double.valueOf(
									yApexPowerPlantListProperties.get(i)
									),
							SI.METER)
					);
			zApexPowerPlantList.add(
					Amount.valueOf(
							Double.valueOf(
									zApexPowerPlantListProperties.get(i)
									),
							SI.METER)
					);
			tiltingAngleList.add(
					Amount.valueOf(
							Double.valueOf(
									tiltingAngleListProperties.get(i)
									),
							NonSI.DEGREE_ANGLE)
					);
		}
		
		System.out.println("Engines found: " + nodelistEngines.getLength());
		for (int i = 0; i < nodelistEngines.getLength(); i++) {
			Node nodeEngine  = nodelistEngines.item(i); // .getNodeValue();
			Element elementEngine = (Element) nodeEngine;
			String engineFileName = elementEngine.getAttribute("file");
			System.out.println("[" + i + "]\nEngine file: " + elementEngine.getAttribute("file"));
			
			String enginePath = engineDir + File.separator + engineFileName;
			engineList.add(Engine.importFromXML(enginePath));
			
			engineList.get(i).setXApexConstructionAxes(xApexPowerPlantList.get(i));
			engineList.get(i).setYApexConstructionAxes(yApexPowerPlantList.get(i));
			engineList.get(i).setZApexConstructionAxes(zApexPowerPlantList.get(i));
			engineList.get(i).setTiltingAngle(tiltingAngleList.get(i));
			engineList.get(i).setMountingPosition(mountingPointList.get(i));
			
		}

		PowerPlant thePowerPlant = new PowerPlant.PowerPlantBuilder("MyPowerPlant", engineList).build();
		
		//---------------------------------------------------------------------------------
		// NACELLES
		List<NacelleCreator> nacelleList = new ArrayList<NacelleCreator>();
		
		NodeList nodelistNacelle = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");

		List<String> xApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/x");
		List<String> yApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/y");
		List<String> zApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/z");		
		List<String> mountingPointNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/mounting_point");

		List<Amount<Length>> xApexNacellesList = new ArrayList<>();
		List<Amount<Length>> yApexNacellesList = new ArrayList<>();
		List<Amount<Length>> zApexNacellesList = new ArrayList<>();
		List<NacelleCreator.MountingPosition> mountingPointNacellesList = new ArrayList<>();
		
		for(int i=0; i<mountingPointNacellesListProperties.size(); i++) {
			if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("WING"))
				mountingPointNacellesList.add(NacelleCreator.MountingPosition.WING);
			else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("FUSELAGE"))
				mountingPointNacellesList.add(NacelleCreator.MountingPosition.FUSELAGE);
			else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("UNDERCARRIAGE_HOUSING"))
				mountingPointNacellesList.add(NacelleCreator.MountingPosition.UNDERCARRIAGE_HOUSING);
			else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("HTAIL"))
				mountingPointNacellesList.add(NacelleCreator.MountingPosition.HTAIL);
			else {
				System.err.println("INVALID NACELLE MOUNTING POSITION !!! ");
				return null;
			}
			
			xApexNacellesList.add(
					Amount.valueOf(
							Double.valueOf(
									xApexNacellesListProperties.get(i)
									),
							SI.METER)
					);
			yApexNacellesList.add(
					Amount.valueOf(
							Double.valueOf(
									yApexNacellesListProperties.get(i)
									),
							SI.METER)
					);
			zApexNacellesList.add(
					Amount.valueOf(
							Double.valueOf(
									zApexNacellesListProperties.get(i)
									),
							SI.METER)
					);
		}
		
		System.out.println("Nacelles found: " + nodelistNacelle.getLength());
		for (int i = 0; i < nodelistNacelle.getLength(); i++) {
			Node nodeNacelle  = nodelistNacelle.item(i); // .getNodeValue();
			Element elementNacelle = (Element) nodeNacelle;
			String nacelleFileName = elementNacelle.getAttribute("file");
			System.out.println("[" + i + "]\nNacelle file: " + elementNacelle.getAttribute("file"));
			
			String nacellePath = nacelleDir + File.separator + nacelleFileName;
			nacelleList.add(NacelleCreator.importFromXML(nacellePath, engineDir));
			
			nacelleList.get(i).setXApexConstructionAxes(xApexNacellesList.get(i));
			nacelleList.get(i).setYApexConstructionAxes(yApexNacellesList.get(i));
			nacelleList.get(i).setZApexConstructionAxes(zApexNacellesList.get(i));
			nacelleList.get(i).setMountingPosition(mountingPointNacellesList.get(i));
			
		}

		Nacelles theNacelles = new Nacelles.NacellesBuilder("MyNacelle", nacelleList).build();
		
		//---------------------------------------------------------------------------------
		// LANDING GEARS
		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		
		LandingGears.MountingPosition mountingPosition = null;
		LandingGears theLandingGears = null;
		Amount<Length> xApexLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexLandingGears = Amount.valueOf(0.0, SI.METER);
		
		if(landingGearsFileName != null) {
			String landingGearsPath = landingGearsDir + File.separator + landingGearsFileName;
			theLandingGears = LandingGears.importFromXML(landingGearsPath);
			
			xApexLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/x");
			yApexLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/y");
			zApexLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/z");
			String mountingPositionProperty = reader.getXMLPropertyByPath("//landing_gears/mounting_point");
			if(mountingPositionProperty.equalsIgnoreCase("FUSELAGE"))
				mountingPosition = MountingPosition.FUSELAGE;
			else if(mountingPositionProperty.equalsIgnoreCase("WING"))
				mountingPosition = MountingPosition.WING;
			else if(mountingPositionProperty.equalsIgnoreCase("NACELLE"))
				mountingPosition = MountingPosition.NACELLE;
			else {
				System.err.println("INVALID LANDING GEARS MOUNTING POSITION !!! ");
				return null;
			}
				
		}
		
		//---------------------------------------------------------------------------------
		// SYSTEMS
		String systemsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//systems/@file");
		
		Systems theSystems = null;
		Amount<Length> xApexSystems = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexSystems = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexSystems = Amount.valueOf(0.0, SI.METER);
		
		if(systemsFileName != null) {
			String systemsPath = systemsDir + File.separator + systemsFileName;
			theSystems = Systems.importFromXML(systemsPath);
			
			xApexSystems = reader.getXMLAmountLengthByPath("//systems/position/x");
			yApexSystems = reader.getXMLAmountLengthByPath("//systems/position/y");
			zApexSystems = reader.getXMLAmountLengthByPath("//systems/position/z");
		}
		
		//---------------------------------------------------------------------------------
		Aircraft theAircraft = new AircraftBuilder(id, aeroDatabaseReader, highLiftDatabaseReader)
				.name(id)
				.aircraftType(type)
				.regulations(regulations)
				.fuselage(theFuselage)
				.xApexFuselage(xApexFuselage)
				.yApexFuselage(yApexFuselage)
				.zApexFuselage(zApexFuselage)
				.cabinConfiguration(theCabinConfiguration)
				.wing(theWing)
				.xApexWing(xApexWing)
				.yApexWing(yApexWing)
				.zApexWing(zApexWing)
				.horizontalTail(theHorizontalTail)
				.xApexHTail(xApexHTail)
				.yApexHTail(yApexHTail)
				.zApexHTail(zApexHTail)
				.verticalTail(theVerticalTail)
				.xApexVTail(xApexVTail)
				.yApexVTail(yApexVTail)
				.zApexVTail(zApexVTail)
				.canard(theCanard)
				.xApexCanard(xApexCanard)
				.yApexCanard(yApexCanard)
				.zApexCanard(zApexCanard)
				.fuelTank(new FuelTank.FuelTankBuilder("Fuel Tank", theWing).build())
				.xApexFuelTank(theWing.getXApexConstructionAxes()
						.plus(theWing.getChordRoot()
								.times(theWing.getLiftingSurfaceCreator()
										.getMainSparNonDimensionalPosition()
										)
								)
						)
				.yApexFuelTank(theWing.getYApexConstructionAxes())
				.zApexFuelTank(theWing.getZApexConstructionAxes())
				.powerPlant(thePowerPlant)
				.nacelles(theNacelles)
				.landingGears(theLandingGears)
				.xApexLandingGears(xApexLandingGears)
				.yApexLandingGears(yApexLandingGears)
				.zApexLandingGears(zApexLandingGears)
				.landingGearsMountingPosition(mountingPosition)
				.systems(theSystems)
				.xApexSystems(xApexSystems)
				.yApexSystem(yApexSystems)
				.zApexSystems(zApexSystems)
				.build();
		
		return theAircraft;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();

		sb.append("\t-------------------------------------\n")
		  .append("\tThe Aircraft\n")
		  .append("\t-------------------------------------\n")
		  .append("\tId: '" + _id + "'\n")
		  .append("\tType: '" + _typeVehicle + "'\n");
		
		if(_theFuselage != null)
			sb.append(_theFuselage.getFuselageCreator().toString());
		
		if(_theWing != null)
			sb.append(_theWing.getLiftingSurfaceCreator().toString());
		
		if(_theExposedWing != null)
			sb.append(_theExposedWing.getLiftingSurfaceCreator().toString());
		
		if(_theHTail != null)
			sb.append(_theHTail.getLiftingSurfaceCreator().toString());
		
		if(_theVTail != null)
			sb.append(_theVTail.getLiftingSurfaceCreator().toString());
		
		if(_theCanard != null)
			sb.append(_theCanard.getLiftingSurfaceCreator().toString());
		
		if(_theCabinConfiguration != null)
			sb.append(_theCabinConfiguration.toString());
		
		if(_theFuelTank != null)
			sb.append(_theFuelTank.toString());
		
		if(_thePowerPlant != null)
			sb.append(_thePowerPlant.toString());
		
		if(_theNacelles != null)
			sb.append(_theNacelles.toString());
		
		if(_theLandingGears != null)
			sb.append(_theLandingGears.toString());
		
		if(_theSystems != null)
			sb.append(_theSystems.toString());
		
		return sb.toString();
		
	}
	
	@Override
	public void deleteFuselage() {
		_theFuselage = null;
		updateType();
	}

	@Override
	public void deleteWing()
	{
		_theWing = null;
		updateType();
	}

	@Override
	public void deleteExposedWing()
	{
		_theExposedWing = null;
		updateType();
	}
	
	@Override
	public void deleteHTail()
	{
		_theHTail = null;
		updateType();
	}

	@Override
	public void deleteVTail()
	{
		_theVTail = null;
		updateType();
	}

	@Override
	public void deleteCanard()
	{
		_theCanard = null;
		updateType();
	}
	
	@Override
	public void deletePowerPlant()
	{
		_thePowerPlant = null;
		updateType();
	}
	
	@Override
	public void deleteNacelles()
	{
		_theNacelles = null;
		updateType();
	}
	
	@Override
	public void deleteFuelTank()
	{
		_theFuelTank = null;
		updateType();
	}
	
	@Override
	public void deleteLandingGears()
	{
		_theLandingGears= null;
		updateType();
	}
	
	@Override
	public void deleteSystems()
	{
		_theSystems = null;
		updateType();
	}
	
	@Override
	public AircraftTypeEnum getTypeVehicle() {
		return _typeVehicle;
	}

	@Override
	public void setTypeVehicle(AircraftTypeEnum _typeVehicle) {
		this._typeVehicle = _typeVehicle;
	}

	/**
	 * @return the _regulations
	 */
	public RegulationsEnum getRegulations() {
		return _regulations;
	}

	/**
	 * @param _regulations the _regulations to set
	 */
	public void setRegulations(RegulationsEnum _regulations) {
		this._regulations = _regulations;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId(String _name) {
		this._id = _name;
	}
	
	@Override
	public AeroConfigurationTypeEnum getType() {
		return _type;
	}

	@Override
	public double getLifeSpan() {
		return _lifeSpan;
	}
	
	@Override
	public void setLifeSpan(double _lifeSpan) {
		this._lifeSpan = _lifeSpan;
	}
	
	public Amount<Area> getSWetTotal() {
		return _sWetTotal;
	}
	
	public void setSWetTotal(Amount<Area> _sWetTotal) {
		this._sWetTotal = _sWetTotal;
	}

	public Double getKExcr() {
		return _kExcr;
	}

	public void setKExcr(Double kExcr) {
		this._kExcr = kExcr;
	}

	@Override
	public List<Object> getComponentsList() {
		return _componentsList;
	}
	
	@Override
	public ACAnalysisManager getTheAnalysisManager() {
		return this._theAnalysisManager;
	}
	
	@Override
	public void setTheAnalysisManager(ACAnalysisManager theAnalysisManager) {
		this._theAnalysisManager = theAnalysisManager;
	}
	
	@Override
	public Fuselage getFuselage() {
		return _theFuselage;
	}
	
	@Override
	public void setFuselage(Fuselage fuselage) {
		this._theFuselage = fuselage;
	}
	
	@Override
	public LiftingSurface getWing() {
		return _theWing;
	}
	
	@Override
	public void setWing(LiftingSurface wing) {
		this._theWing = wing;
	}
	
	@Override
	public LiftingSurface getExposedWing() {
		return _theExposedWing;
	}
	
	@Override
	public void setExposedWing(LiftingSurface exposedWing) {
		this._theExposedWing = exposedWing;
	}
	
	@Override
	public LiftingSurface getHTail() {
		return _theHTail;
	}

	@Override
	public void setHTail(LiftingSurface hTail) {
		this._theHTail = hTail;
	}
	
	@Override
	public LiftingSurface getVTail() {
		return _theVTail;
	}

	@Override
	public void setVTail(LiftingSurface vTail) {
		this._theVTail = vTail;
	}
	
	@Override
	public LiftingSurface getCanard() {
		return _theCanard;
	}

	@Override
	public void setCanard(LiftingSurface canard) {
		this._theCanard = canard;
	}
	
	@Override
	public PowerPlant getPowerPlant() {
		return _thePowerPlant;
	}
	
	@Override
	public void setPowerPlant(PowerPlant powerPlant) {
		this._thePowerPlant = powerPlant;
	}
	
	@Override
	public Nacelles getNacelles() {
		return _theNacelles;
	}
	
	@Override
	public void setNacelles(Nacelles nacelles) {
		this._theNacelles = nacelles;
	}
	
	@Override
	public FuelTank getFuelTank() {
		return _theFuelTank;
	}
	
	@Override
	public void setFuelTank(FuelTank fuelTank) {
		this._theFuelTank = fuelTank;
	}
	
	@Override
	public LandingGears getLandingGears() {
		return _theLandingGears;
	}
	
	@Override
	public void setLandingGears(LandingGears landingGears) {
		this._theLandingGears = landingGears;
	}
	
	@Override
	public Systems getSystems() {
		return _theSystems;
	}
	
	@Override
	public void setSystems(Systems systems) {
		this._theSystems = systems;
	}
	
	@Override
	public CabinConfiguration getCabinConfiguration() {
		return _theCabinConfiguration;
	}
	
	@Override
	public void setCabinConfiguration(CabinConfiguration theCabinConfiguration) {
		this._theCabinConfiguration = theCabinConfiguration;
	}
	
	@Override
	public Amount<Length> getWingACToCGDistance() {
		return _wingACToCGDistance;
	}

	@Override
	public void setWingACToCGDistance(Amount<Length> _wingACToCGDistance) {
		this._wingACToCGDistance = _wingACToCGDistance;
	}
	
} // end of class
