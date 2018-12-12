package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.fuselage.FuselageWeightManager;
import analyses.landinggears.LandingGearsWeightManager;
import analyses.liftingsurface.LiftingSurfaceWeightManager;
import analyses.nacelles.NacelleWeightManager;
import analyses.powerplant.EngineWeightManager;
import analyses.systems.SystemsWeightManager;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 */
public class ACWeightsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACWeightsManager _theWeightsManagerInterface;
	private static int _maxIteration = 20;  
	private FuelFractionDatabaseReader _fuelFractionDatabaseReader;
	
	/* Aerosapce Alluminuim - page 583 Sadraey Aircraft Design System Engineering Approach */
	private Amount<VolumetricDensity> _materialDensity = Amount.valueOf(2711.0,VolumetricDensity.UNIT);

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	private Amount<Mass> _fuselageReferenceMass;
	private Amount<Mass> _wingReferenceMass;
	private Amount<Mass> _horizontalTailReferenceMass;
	private Amount<Mass> _verticalTailReferenceMass;
	private Amount<Mass> _canardReferenceMass;
	private Amount<Mass> _nacellesReferenceMass;
	private Amount<Mass> _powerPlantReferenceMass;
	private Amount<Mass> _landingGearsReferenceMass;
	private Amount<Mass> _systemsReferenceMass;
	
	private Amount<Mass> _fuselageMass;
	private Amount<Mass> _wingMass;
	private Amount<Mass> _hTailMass;
	private Amount<Mass> _vTailMass;
	private Amount<Mass> _canardMass;
	private Amount<Mass> _nacellesMass;
	private Amount<Mass> _powerPlantMass;
	private Amount<Mass> _landingGearsMass;
	private Amount<Mass> _apuMass;
	private Amount<Mass> _airConditioningAndAntiIcingSystemMass;
	private Amount<Mass> _instrumentsAndNavigationSystemMass;
	private Amount<Mass> _hydraulicAndPneumaticSystemMass;
	private Amount<Mass> _electricalSystemsMass;
	private Amount<Mass> _controlSurfaceMass;
	private Amount<Mass> _furnishingsAndEquipmentsMass;
	
	private Amount<Mass> _paxMass;
	private Amount<Mass> _paxMassMax;
	private Amount<Mass> _crewMass;
	private Amount<Mass> _structuralMass;
	private Amount<Mass> _operatingItemMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _emptyMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _maximumLandingMass;
	private Amount<Mass> _manufacturerEmptyMass;
	private Amount<Mass> _zeroFuelMass;
	private Amount<Mass> _takeOffMass;
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _trappedFuelOilMass;
	private Amount<Mass> _fuelMass;

	private List<Amount<Mass>> _maximumTakeOffMassList;

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._fuelFractionDatabaseReader = new FuelFractionDatabaseReader(
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
				"FuelFractions.h5"
				);
		this._maximumTakeOffMassList = new ArrayList<>();
		
		/* Component masses initialization ... */
		this._fuselageMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._wingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._hTailMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._vTailMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._canardMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._nacellesMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._powerPlantMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._landingGearsMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._apuMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._airConditioningAndAntiIcingSystemMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._instrumentsAndNavigationSystemMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._electricalSystemsMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._controlSurfaceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		this._furnishingsAndEquipmentsMass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		/* Passengers and crew mass */
		_paxMass = _theWeightsManagerInterface.getSinglePassengerMass().to(SI.KILOGRAM).times(
				_theWeightsManagerInterface.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()
				);
		_crewMass = Amount.valueOf(
				_theWeightsManagerInterface.getTheAircraft().getCabinConfiguration().getTotalCrewNumber() 
				* 76.5145485, 
				SI.KILOGRAM
				); 
		_paxMassMax = _theWeightsManagerInterface.getSinglePassengerMass().to(SI.KILOGRAM).times(
				_theWeightsManagerInterface.getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber()
				);

		// Operating items mass
		if (_theWeightsManagerInterface.getReferenceMissionRange().doubleValue(NonSI.NAUTICAL_MILE) < 2000)  
			_operatingItemMass = Amount.valueOf(
					8.617
					* _theWeightsManagerInterface.getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber(),
					SI.KILOGRAM
					);
		else 
			_operatingItemMass = Amount.valueOf(
					14.97
					* _theWeightsManagerInterface.getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber(),
					SI.KILOGRAM
					);
		
	}

	private void initializeComponentsWeightManagers () {
		
		if(_theWeightsManagerInterface.getTheAircraft().getFuselage() != null)
			_theWeightsManagerInterface.getTheAircraft().getFuselage().setTheWeight(new FuselageWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getWing() != null)
			_theWeightsManagerInterface.getTheAircraft().getWing().setTheWeightManager(new LiftingSurfaceWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getHTail() != null)
			_theWeightsManagerInterface.getTheAircraft().getHTail().setTheWeightManager(new LiftingSurfaceWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getVTail() != null)
			_theWeightsManagerInterface.getTheAircraft().getVTail().setTheWeightManager(new LiftingSurfaceWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getCanard() != null)
			_theWeightsManagerInterface.getTheAircraft().getCanard().setTheWeightManager(new LiftingSurfaceWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getNacelles() != null)
			_theWeightsManagerInterface.getTheAircraft().getNacelles().setTheWeights(new NacelleWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant() != null)
			_theWeightsManagerInterface.getTheAircraft().getPowerPlant().setTheWeights(new EngineWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getLandingGears() != null)
			_theWeightsManagerInterface.getTheAircraft().getLandingGears().setTheWeigths(new LandingGearsWeightManager());
		if(_theWeightsManagerInterface.getTheAircraft().getSystems() != null)
			_theWeightsManagerInterface.getTheAircraft().getSystems().setTheWeightManager(new SystemsWeightManager());
		
	}
	
	private void initializeReferenceMasses () {
		
		this._fuselageReferenceMass = null;
		this._wingReferenceMass = null;
		this._horizontalTailReferenceMass = null;
		this._verticalTailReferenceMass = null;
		this._canardReferenceMass = null;
		this._nacellesReferenceMass = null;
		this._powerPlantReferenceMass = null;
		this._landingGearsReferenceMass = null;
		this._systemsReferenceMass = null;
		
	}
	
	@SuppressWarnings("unchecked")
	public static ACWeightsManager importFromXML (String pathToXML, Aircraft theAircraft, OperatingConditions theOperatingConditions) throws HDF5LibraryException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// FIRST GUESS MAXIMUM TAKE-OFF MASS
		Amount<Mass> firstGuessMaximumTakeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String firstGuessMaximumTakeOffMassProperty = reader.getXMLPropertyByPath("//weights/global_data/first_guess_maximum_take_off_mass");
		if(firstGuessMaximumTakeOffMassProperty != null)
			firstGuessMaximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/first_guess_maximum_take_off_mass");
		else {
			System.err.println("FIRST GUESS MAXIMUM TAKE-OFF MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// FIRST GUESS MAXIMUM TAKE-OFF MASS
		double relativeMaximumLanidngMass = 0.9;
		String relativeMaximumLanidngMassProperty = reader.getXMLPropertyByPath("//weights/global_data/relative_maximum_landing_mass");
		if(relativeMaximumLanidngMassProperty != null)
			relativeMaximumLanidngMass = Double.valueOf(relativeMaximumLanidngMassProperty);

		//---------------------------------------------------------------
		// SINGLE PASSENGER MASS
		Amount<Mass> singlePassengerMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String singlePassengerMassProperty = reader.getXMLPropertyByPath("//weights/global_data/single_passenger_mass");
		if(singlePassengerMassProperty != null)
			singlePassengerMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/single_passenger_mass");
		else {
			System.err.println("SINGLE PASSENGER MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// REFERENCE MISSION RANGE
		Amount<Length> referenceMissionRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		String referenceMissionRangeProperty = reader.getXMLPropertyByPath("//weights/global_data/reference_mission_range");
		if(referenceMissionRangeProperty != null)
			referenceMissionRange = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//weights/global_data/reference_mission_range");
		else {
			System.err.println("REFERENCE MISSION RANGE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//--------------------------------------------------------------
		// MISSION PROFILE DATA INITIALIZATION
		Amount<Mass> missionFuelMass = null;
		Amount<Length> cruiseRange = null;
		Double cruiseSFC = null;
		Double cruiseEfficiency = null;
		Amount<Length> alternateRange = null;
		Amount<Length> alternateAltitude = null;
		Double alternateMach = null;
		Double alternateSFC = null;
		Double alternateEfficiency = null;
		Amount<Duration> holdingDuration = null;
		Amount<Length> holdingAltitude = null;
		Double holdingMach = null;
		Double holdingSFC = null;
		Double holdingEfficiency = 0.0;
		
		//---------------------------------------------------------------
		// ESTIMATE MISSION FUEL FLAG
		boolean estimateMissionFuel = false;
		String estimateMissionFuelString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//mission_profile_data/@estimate_mission_fuel");
		if(estimateMissionFuelString != null) {
			if(estimateMissionFuelString.equalsIgnoreCase("FALSE"))
				estimateMissionFuel = false;
			else
				estimateMissionFuel = true;
		}

		//---------------------------------------------------------------
		if(estimateMissionFuel == false) {

			//---------------------------------------------------------------
			// MISSION FUEL
			String missionFuelMassString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//mission_profile_data/@mission_fuel_mass");
			if(missionFuelMassString != null) 
				missionFuelMass = Amount.valueOf(Double.valueOf(missionFuelMassString), SI.KILOGRAM);
			else {
				System.err.println("WARINING (IMPORT WEIGHTS DATA) - ESTIMATE MISSION FUEL FLAG IS 'TRUE' BUT NO MISSION FUEL MASS HAS BEEN ASSIGNED ... RETURNING");
				System.exit(1);
			}
			
		}
		//---------------------------------------------------------------
		else {
			
			//---------------------------------------------------------------
			// CRUISE RANGE
			String cruiseRangeProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/cruise_phase/range");
			if(cruiseRangeProperty != null)
				cruiseRange = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//weights/mission_profile_data/cruise_phase/range");
			else {
				System.err.println("CRUISE RANGE REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// CRUISE SFC
			String cruiseSFCProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/cruise_phase/sfc");
			if(cruiseSFCProperty != null)
				cruiseSFC = Double.valueOf(cruiseSFCProperty);
			else {
				System.err.println("CRUISE SFC REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// CRUISE EFFICIENCY
			String cruiseEfficiencyProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/cruise_phase/efficiency");
			if(cruiseEfficiencyProperty != null)
				cruiseEfficiency = Double.valueOf(cruiseEfficiencyProperty);
			else {
				System.err.println("CRUISE EFFICIENCY REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// ALTERNATE RANGE
			String alternateRangeProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/alternate_phase/range");
			if(alternateRangeProperty != null)
				alternateRange = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//weights/mission_profile_data/alternate_phase/range");
			else {
				System.err.println("ALTERNATE RANGE REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// ALTERNATE ALTITUDE
			String alternateAltitudeProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/alternate_phase/altitude");
			if(alternateAltitudeProperty != null)
				alternateAltitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//weights/mission_profile_data/alternate_phase/altitude");
			else {
				System.err.println("ALTERNATE ALTITUDE REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// ALTERNATE MACH
			String alternateMachProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/alternate_phase/mach");
			if(alternateMachProperty != null)
				alternateMach = Double.valueOf(alternateMachProperty);
			else {
				System.err.println("ALTERNATE MACH REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// ALTERNATE SFC
			String alternateSFCProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/alternate_phase/sfc");
			if(alternateSFCProperty != null)
				alternateSFC = Double.valueOf(alternateMachProperty);
			else {
				System.err.println("ALTERNATE SFC REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// ALTERNATE EFFICIENCY
			String alternateEfficiencyProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/alternate_phase/efficiency");
			if(alternateEfficiencyProperty != null)
				alternateEfficiency = Double.valueOf(alternateEfficiencyProperty);
			else {
				System.err.println("ALTERNATE EFFICIENCY REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// HOLDING DURATION
			String holdingDurationProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/holding_phase/duration");
			if(holdingDurationProperty != null)
				holdingDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//weights/mission_profile_data/holding_phase/duration");
			else {
				System.err.println("HOLDING DURATION REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// HOLDING ALTITUDE
			String holdingAltitudeProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/holding_phase/altitude");
			if(holdingAltitudeProperty != null)
				holdingAltitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//weights/mission_profile_data/holding_phase/altitude");
			else {
				System.err.println("HOLDING ALTITUDE REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// HOLDING MACH
			String holdingMachProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/holding_phase/mach");
			if(holdingMachProperty != null)
				holdingMach = Double.valueOf(holdingMachProperty);
			else {
				System.err.println("HOLDING MACH REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// HOLDING SFC
			String holdingSFCProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/holding_phase/sfc");
			if(holdingSFCProperty != null)
				holdingSFC = Double.valueOf(holdingSFCProperty);
			else {
				System.err.println("HOLDING SFC REQUIRED !! \n ... returning ");
				return null; 
			}

			//---------------------------------------------------------------
			// HOLDING EFFICIENCY
			String holdingEfficiencyProperty = reader.getXMLPropertyByPath("//weights/mission_profile_data/holding_phase/efficiency");
			if(holdingEfficiencyProperty != null)
				holdingEfficiency = Double.valueOf(holdingEfficiencyProperty);
			else {
				System.err.println("HOLDING EFFICIENCY REQUIRED !! \n ... returning ");
				return null; 
			}
		}
		
		//---------------------------------------------------------------
		// FUSELAGE CALIBRATION FACTOR
		double fuselageCalibrationFactor = 1.0;
		String fuselageCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/fuselage_calibration_factor");
		if(fuselageCalibrationFactorProperty != null)
			fuselageCalibrationFactor = Double.valueOf(fuselageCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// WING CALIBRATION FACTOR
		double wingCalibrationFactor = 1.0;
		String wingCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/wing_calibration_factor");
		if(wingCalibrationFactorProperty != null)
			wingCalibrationFactor = Double.valueOf(wingCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// HTAIL CALIBRATION FACTOR
		double hTailCalibrationFactor = 1.0;
		String hTailCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/horizontal_tail_calibration_factor");
		if(hTailCalibrationFactorProperty != null)
			hTailCalibrationFactor = Double.valueOf(hTailCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// VTAIL CALIBRATION FACTOR
		double vTailCalibrationFactor = 1.0;
		String vTailCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/vertical_tail_calibration_factor");
		if(vTailCalibrationFactorProperty != null)
			vTailCalibrationFactor = Double.valueOf(vTailCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// CANARD CALIBRATION FACTOR
		double canardCalibrationFactor = 1.0;
		String canardCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/canard_calibration_factor");
		if(canardCalibrationFactorProperty != null)
			canardCalibrationFactor = Double.valueOf(canardCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// NACELLES CALIBRATION FACTOR
		double nacellesCalibrationFactor = 1.0;
		String nacellesCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/nacelles_calibration_factor");
		if(nacellesCalibrationFactorProperty != null)
			nacellesCalibrationFactor = Double.valueOf(nacellesCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// POWER PLANT CALIBRATION FACTOR
		double powerPlantCalibrationFactor = 1.0;
		String powerPlantCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/power_plant_calibration_factor");
		if(powerPlantCalibrationFactorProperty != null)
			powerPlantCalibrationFactor = Double.valueOf(powerPlantCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// LANDING GEARS CALIBRATION FACTOR
		double landingGearsCalibrationFactor = 1.0;
		String landingGearsCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/landing_gears_calibration_factor");
		if(landingGearsCalibrationFactorProperty != null)
			landingGearsCalibrationFactor = Double.valueOf(landingGearsCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// APU CALIBRATION FACTOR
		double apuCalibrationFactor = 1.0;
		String apuCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/APU_calibration_factor");
		if(apuCalibrationFactorProperty != null)
			apuCalibrationFactor = Double.valueOf(apuCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// AIR CONDITIONING AND ANTI-ICING CALIBRATION FACTOR
		double airConditioningAndAntiIcingCalibrationFactor = 1.0;
		String airConditioningAndAntiIcingCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/air_conditioning_and_anti_icing_system_calibration_factor");
		if(airConditioningAndAntiIcingCalibrationFactorProperty != null)
			airConditioningAndAntiIcingCalibrationFactor = Double.valueOf(airConditioningAndAntiIcingCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// INSTRUMENTS AND NAVIGATION SYSTEM CALIBRATION FACTOR
		double instrumentsAndNavigationSystemCalibrationFactor = 1.0;
		String instrumentsAndNavigationSystemCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/instruments_and_navigation_system_calibration_factor");
		if(instrumentsAndNavigationSystemCalibrationFactorProperty != null)
			instrumentsAndNavigationSystemCalibrationFactor = Double.valueOf(instrumentsAndNavigationSystemCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// HYDRAULIC AND PNEUMATIC SYSTEMS CALIBRATION FACTOR
		double hydraulicAndPneumaticSystemsCalibrationFactor = 1.0;
		String hydraulicAndPneumaticSystemsCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/electrical_systems_calibration_factor");
		if(hydraulicAndPneumaticSystemsCalibrationFactorProperty != null)
			hydraulicAndPneumaticSystemsCalibrationFactor = Double.valueOf(hydraulicAndPneumaticSystemsCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// ELECTRICAL SYSTEMS CALIBRATION FACTOR
		double electricalSystemsCalibrationFactor = 1.0;
		String electricalSystemsCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/electrical_systems_calibration_factor");
		if(electricalSystemsCalibrationFactorProperty != null)
			electricalSystemsCalibrationFactor = Double.valueOf(electricalSystemsCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// CONTROL SURFACE CALIBRATION FACTOR
		double controlSurfaceCalibrationFactor = 1.0;
		String controlSurfaceCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/control_surfaces_calibration_factor");
		if(electricalSystemsCalibrationFactorProperty != null)
			electricalSystemsCalibrationFactor = Double.valueOf(controlSurfaceCalibrationFactorProperty);
		
		//---------------------------------------------------------------
		// FURNISHINGS AND EQUIPMENTS CALIBRATION FACTOR
		double furnishingsAndEquiplmentsCalibrationFactor = 1.0;
		String furnishingsAndEquiplmentsCalibrationFactorProperty = reader.getXMLPropertyByPath("//weights/calibration/furnishings_and_equipments_calibration_factor");
		if(furnishingsAndEquiplmentsCalibrationFactorProperty != null)
			furnishingsAndEquiplmentsCalibrationFactor = Double.valueOf(furnishingsAndEquiplmentsCalibrationFactorProperty);

		IACWeightsManager theWeigthsManagerInterface = new IACWeightsManager.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setFirstGuessMaxTakeOffMass(firstGuessMaximumTakeOffMass)
				.setRelativeMaximumLandingMass(relativeMaximumLanidngMass)
				.setSinglePassengerMass(singlePassengerMass)
				.setEstimateMissionFuelFlag(estimateMissionFuel)
				.setMissionFuel(missionFuelMass)
				.setReferenceMissionRange(referenceMissionRange)
				.setCruiseRange(cruiseRange)
				.setCruiseSFC(cruiseSFC)
				.setCruiseEfficiency(cruiseEfficiency)
				.setAlternateCruiseRange(alternateRange)
				.setAlternateCruiseAltitide(alternateAltitude)
				.setAlternateCruiseMachNumber(alternateMach)
				.setAlternateCruiseSFC(alternateSFC)
				.setAlternateCruiseEfficiency(alternateEfficiency)
				.setHoldingDuration(holdingDuration)
				.setHoldingAltitide(holdingAltitude)
				.setHoldingMachNumber(holdingMach)
				.setHoldingSFC(holdingSFC)
				.setHoldingEfficiency(holdingEfficiency)
				.setFuselageCalibrationFactor(fuselageCalibrationFactor)
				.setWingCalibrationFactor(wingCalibrationFactor)
				.setHTailCalibrationFactor(hTailCalibrationFactor)
				.setVTailCalibrationFactor(vTailCalibrationFactor)
				.setCanardCalibrationFactor(canardCalibrationFactor)
				.setNacellesCalibrationFactor(nacellesCalibrationFactor)
				.setPowerPlantCalibrationFactor(powerPlantCalibrationFactor)
				.setLandingGearsCalibrationFactor(landingGearsCalibrationFactor)
				.setAPUCalibrationFactor(apuCalibrationFactor)
				.setAirConditioningAndAntiIcingSystemCalibrationFactor(airConditioningAndAntiIcingCalibrationFactor)
				.setInstrumentsAndNavigationSystemCalibrationFactor(instrumentsAndNavigationSystemCalibrationFactor)
				.setHydraulicAndPneumaticCalibrationFactor(hydraulicAndPneumaticSystemsCalibrationFactor)
				.setElectricalSystemsCalibrationFactor(electricalSystemsCalibrationFactor)
				.setControlSurfaceCalibrationFactor(controlSurfaceCalibrationFactor)
				.setFurnishingsAndEquipmentsCalibrationFactor(furnishingsAndEquiplmentsCalibrationFactor)
				.build();
		
		ACWeightsManager theWeightsManager = new ACWeightsManager();
		theWeightsManager.setTheWeightsManagerInterface(theWeigthsManagerInterface);
		
		return theWeightsManager;

	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tWeights Analysis\n")
				.append("\t-------------------------------------\n")
				.append("\tReference Range: " + _theWeightsManagerInterface.getReferenceMissionRange().to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\tSingle Passenger Mass: " + _theWeightsManagerInterface.getSinglePassengerMass().to(SI.KILOGRAM) + "\n")
				.append("\t-------------------------------------\n")
				.append("\t-------------------------------------\n")
				.append("\tMaximum Take-Off Mass: " + _maximumTakeOffMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tTake-Off Mass: " + _takeOffMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Landing Mass: " + _maximumLandingMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Passengers Mass: " + _paxMassMax.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tPassengers Mass: " + _paxMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tCrew Mass: " + _crewMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tMax Fuel Mass: " + _theWeightsManagerInterface.getTheAircraft().getFuelTank().getFuelMass().to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tDesign Mission Fuel Mass: " + _fuelMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Zero Fuel Mass: " + _maximumZeroFuelMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tZero Fuel Mass: " + _zeroFuelMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tOperating Empty Mass: " + _operatingEmptyMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tEmpty Mass: " + _emptyMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tManufacturer Empty Mass: " + _manufacturerEmptyMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tStructural Mass: " + _structuralMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tOperating Item Mass: " + _operatingItemMass.to(SI.KILOGRAM) + "\n")
				.append("\t·····································\n")
				.append("\tTrapped Fuel Oil Mass: " + _trappedFuelOilMass.to(SI.KILOGRAM) + "\n")
				.append("\t-------------------------------------\n")
				.append("\tCOMPONENTS:\n")
				.append("\t-------------------------------------\n");
		if(_theWeightsManagerInterface.getTheAircraft().getFuselage() != null)
				sb.append("\t\tFuselage Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tFuselage Calibration Factor: " + _theWeightsManagerInterface.getFuselageCalibrationFactor() + "\n")
				.append("\t\tFuselage Estimated Mass (Calibrated): " + _fuselageMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getWing() != null)
				sb.append("\t\tWing Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tWing Calibration Factor: " + _theWeightsManagerInterface.getWingCalibrationFactor() + "\n")
				.append("\t\tWing Estimated Mass (Calibrated): " + _wingMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getHTail() != null)
				sb.append("\t\tHorizontal Tail Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tHorizontal Tail Calibration Factor: " + _theWeightsManagerInterface.getHTailCalibrationFactor() + "\n")
				.append("\t\tHorizontal Tail Estimated Mass (Calibrated): " + _hTailMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getVTail() != null)
				sb.append("\t\tVertical Tail Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tVertical Tail Calibration Factor: " + _theWeightsManagerInterface.getVTailCalibrationFactor() + "\n")
				.append("\t\tVertical Tail Estimated Mass (Calibrated): " + _vTailMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getCanard() != null)
				sb.append("\t\tCanard Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tCanard Calibration Factor: " + _theWeightsManagerInterface.getCanardCalibrationFactor() + "\n")
				.append("\t\tCanard Estimated Mass (Calibrated): " + _canardMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getNacelles() != null)
				sb.append("\t\tNacelles Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tNacelles Calibration Factor: " + _theWeightsManagerInterface.getNacellesCalibrationFactor() + "\n")
				.append("\t\tNacelles Estimated Mass (Calibrated): " + _nacellesMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant() != null)
				sb.append("\t\tPower Plant Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tPower Plant Calibration Factor: " + _theWeightsManagerInterface.getPowerPlantCalibrationFactor() + "\n")
				.append("\t\tPower Plant Estimated Mass (Calibrated): " + _powerPlantMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t·····································\n");
		if(_theWeightsManagerInterface.getTheAircraft().getLandingGears() != null)
				sb.append("\t\tLanding Gears Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassEstimated().to(SI.KILOGRAM) + "\n")
				.append("\t\tLanding Gears Calibration Factor: " + _theWeightsManagerInterface.getLandingGearsCalibrationFactor() + "\n")
				.append("\t\tLanding Gears Estimated Mass (Calibrated): " + _landingGearsMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t-------------------------------------\n");
		if(_theWeightsManagerInterface.getTheAircraft().getSystems() != null)
				sb.append("\t\tSYSTEMS AND EQUIPMENTS:\n")
				.append("\t\t-------------------------------------\n")
				.append("\t\t\tAPU Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedAPU().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tAPU Calibration Factor: " + _theWeightsManagerInterface.getAPUCalibrationFactor() + "\n")
				.append("\t\t\tAPU Estimated Mass (Calibrated): " + _apuMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tAir Conditioning and Anti-Icing System Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedAirConditioningAndAntiIcing().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tAPU Calibration Factor: " + _theWeightsManagerInterface.getAirConditioningAndAntiIcingSystemCalibrationFactor() + "\n")
				.append("\t\t\tAir Conditioning and Anti-Icing System Estimated Mass (Calibrated): " + _airConditioningAndAntiIcingSystemMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tInstruments and Navigation System Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedInstrumentsAndNavigation().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tInstruments and Navigation System Calibration Factor: " + _theWeightsManagerInterface.getInstrumentsAndNavigationSystemCalibrationFactor() + "\n")
				.append("\t\t\tInstruments and Navigation System Estimated Mass (Calibrated): " + _instrumentsAndNavigationSystemMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tHydraulic and Pneumatic Systems Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedHydraulicAndPneumatic().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tHydraulic and Pneumatic Systems Calibration Factor: " + _theWeightsManagerInterface.getHydraulicAndPneumaticCalibrationFactor() + "\n")
				.append("\t\t\tHydraulic and Pneumatic Systems Estimated Mass (Calibrated): " + _hydraulicAndPneumaticSystemMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tElectrical Systems Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedElectricalSystems().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tElectrical Systems Calibration Factor: " + _theWeightsManagerInterface.getElectricalSystemsCalibrationFactor() + "\n")
				.append("\t\t\tElectrical Systems Estimated Mass (Calibrated): " + _electricalSystemsMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tControl Surfaces Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedControlSurface().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tControl Surfaces Calibration Factor: " + _theWeightsManagerInterface.getControlSurfaceCalibrationFactor() + "\n")
				.append("\t\t\tControl Surfaces Estimated Mass (Calibrated): " + _controlSurfaceMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tFurnishings and Equipments Estimated Mass: " + _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedFurnishingsAndEquipment().to(SI.KILOGRAM) + "\n")
				.append("\t\t\tFurnishings and Equipments Calibration Factor: " + _theWeightsManagerInterface.getFurnishingsAndEquipmentsCalibrationFactor() + "\n")
				.append("\t\t\tFurnishings and Equipments Estimated Mass (Calibrated): " + _furnishingsAndEquipmentsMass.to(SI.KILOGRAM) + "\n")
				.append("\t\t\t·····································\n")
				;
				
		return sb.toString();
		
	}
	
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
			outputFile.delete();		
			System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		//--------------------------------------------------------------------------------
		// GLOBAL ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		Sheet sheet = wb.createSheet("GLOBAL RESULTS");
		List<Object[]> dataListGlobal = new ArrayList<>();
		dataListGlobal.add(new Object[] {"Description","Unit","Value"});
		dataListGlobal.add(new Object[] {"Reference Range","nmi", _theWeightsManagerInterface.getReferenceMissionRange().doubleValue(NonSI.NAUTICAL_MILE)});
		dataListGlobal.add(new Object[] {"Single passenger Mass","kg", _theWeightsManagerInterface.getSinglePassengerMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Mass","kg",_maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Take-Off Mass","kg",_takeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Landing Mass","kg",_maximumLandingMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Fuel Mass","kg", _theWeightsManagerInterface.getTheAircraft().getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Design Mission Fuel Mass","kg", _fuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Mass","kg",_maximumZeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Zero Fuel Mass","kg",_zeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Mass","kg",_paxMassMax.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Actual Passengers Mass","kg",_paxMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Empty Mass","kg",_operatingEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Empty Mass","kg",_emptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Mass","kg",_trappedFuelOilMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Crew Mass","kg",_crewMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Item Mass","kg",_operatingItemMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Mass","kg",_manufacturerEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Structural Mass","kg",_structuralMass.doubleValue(SI.KILOGRAM)});
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = wb.createFont();
	    font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
        
        Row row = sheet.createRow(0);
		Object[] objArr = dataListGlobal.get(0);
		int cellnum = 0;
		for (Object obj : objArr) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellStyle(styleHead);
			if (obj instanceof Date) {
				cell.setCellValue((Date) obj);
			} else if (obj instanceof Boolean) {
				cell.setCellValue((Boolean) obj);
			} else if (obj instanceof String) {
				cell.setCellValue((String) obj);
			} else if (obj instanceof Double) {
				cell.setCellValue((Double) obj);
			}
			sheet.setDefaultColumnWidth(35);
    		sheet.setColumnWidth(1, 2048);
    		sheet.setColumnWidth(2, 3840);
		}
	
		int rownum = 1;
		for (int i = 1; i < dataListGlobal.size(); i++) {
			objArr = dataListGlobal.get(i);
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}
		}

		//--------------------------------------------------------------------------------
		// FUSELAGE WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListFuselage.add(new Object[] {"Reference Mass","kg", getFuselageReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListFuselage.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListFuselage.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getFuselageCalibrationFactor()});
			dataListFuselage.add(new Object[] {"Estimated Mass (calibrated)", "kg", _fuselageMass.doubleValue(SI.KILOGRAM)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexFuselage=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getPercentDifference()[indexFuselage]
							}
							);
				indexFuselage++;
			}

			Row rowFuselage = sheetFuselage.createRow(0);
			Object[] objArrFuselage = dataListFuselage.get(0);
			int cellnumFuselage = 0;
			for (Object obj : objArrFuselage) {
				Cell cell = rowFuselage.createCell(cellnumFuselage++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetFuselage.setDefaultColumnWidth(35);
				sheetFuselage.setColumnWidth(1, 2048);
				sheetFuselage.setColumnWidth(2, 3840);
			}

			int rownumFuselage = 1;
			for (int j = 1; j < dataListFuselage.size(); j++) {
				objArrFuselage = dataListFuselage.get(j);
				rowFuselage = sheetFuselage.createRow(rownumFuselage++);
				cellnumFuselage = 0;
				for (Object obj : objArrFuselage) {
					Cell cell = rowFuselage.createCell(cellnumFuselage++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// WING WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListWing.add(new Object[] {"Reference Mass","kg", getWingReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListWing.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListWing.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getWingCalibrationFactor()});
			dataListWing.add(new Object[] {"Estimated Mass (calibrated)", "kg", _wingMass.doubleValue(SI.KILOGRAM)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexWing=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getPercentDifference()[indexWing]
							}
							);
				indexWing++;
			}

			Row rowWing = sheetWing.createRow(0);
			Object[] objArrWing = dataListWing.get(0);
			int cellnumWing = 0;
			for (Object obj : objArrWing) {
				Cell cell = rowWing.createCell(cellnumWing++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetWing.setDefaultColumnWidth(35);
				sheetWing.setColumnWidth(1, 2048);
				sheetWing.setColumnWidth(2, 3840);
			}

			int rownumWing = 1;
			for (int j = 1; j < dataListWing.size(); j++) {
				objArrWing = dataListWing.get(j);
				rowWing = sheetWing.createRow(rownumWing++);
				cellnumWing = 0;
				for (Object obj : objArrWing) {
					Cell cell = rowWing.createCell(cellnumWing++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// HORIZONTAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListHTail.add(new Object[] {"Reference Mass","kg", getHorizontalTailReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListHTail.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListHTail.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getHTailCalibrationFactor()});
			dataListHTail.add(new Object[] {"Estimated Mass (calibrated)", "kg", _hTailMass.doubleValue(SI.KILOGRAM)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexHTail=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getPercentDifference()[indexHTail]
							}
							);
				indexHTail++;
			}

			Row rowHTail = sheetHTail.createRow(0);
			Object[] objArrHTail = dataListHTail.get(0);
			int cellnumHTail = 0;
			for (Object obj : objArrHTail) {
				Cell cell = rowHTail.createCell(cellnumHTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetHTail.setDefaultColumnWidth(35);
				sheetHTail.setColumnWidth(1, 2048);
				sheetHTail.setColumnWidth(2, 3840);
			}

			int rownumHTail = 1;
			for (int j = 1; j < dataListHTail.size(); j++) {
				objArrHTail = dataListHTail.get(j);
				rowHTail = sheetHTail.createRow(rownumHTail++);
				cellnumHTail = 0;
				for (Object obj : objArrHTail) {
					Cell cell = rowHTail.createCell(cellnumHTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// VERTICAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListVTail.add(new Object[] {"Reference Mass","kg", getVerticalTailReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListVTail.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListVTail.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getVTailCalibrationFactor()});
			dataListVTail.add(new Object[] {"Estimated Mass (calibrated)", "kg", _vTailMass.doubleValue(SI.KILOGRAM)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexVTail=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getPercentDifference()[indexVTail]
							}
							);
				indexVTail++;
			}

			Row rowVTail = sheetVTail.createRow(0);
			Object[] objArrVTail = dataListVTail.get(0);
			int cellnumVTail = 0;
			for (Object obj : objArrVTail) {
				Cell cell = rowVTail.createCell(cellnumVTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetVTail.setDefaultColumnWidth(35);
				sheetVTail.setColumnWidth(1, 2048);
				sheetVTail.setColumnWidth(2, 3840);
			}

			int rownumVTail = 1;
			for (int j = 1; j < dataListVTail.size(); j++) {
				objArrVTail = dataListVTail.get(j);
				rowVTail = sheetVTail.createRow(rownumVTail++);
				cellnumVTail = 0;
				for (Object obj : objArrVTail) {
					Cell cell = rowVTail.createCell(cellnumVTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// CANARD WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListCanard.add(new Object[] {"Reference Mass","kg", getCanardReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListCanard.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListCanard.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getCanardCalibrationFactor()});
			dataListCanard.add(new Object[] {"Estimated Mass (calibrated)", "kg", _canardMass.doubleValue(SI.KILOGRAM)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexCanard=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getPercentDifference()[indexCanard]
							}
							);
				indexCanard++;
			}

			Row rowCanard = sheetCanard.createRow(0);
			Object[] objArrCanard = dataListCanard.get(0);
			int cellnumCanard = 0;
			for (Object obj : objArrCanard) {
				Cell cell = rowCanard.createCell(cellnumCanard++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetCanard.setDefaultColumnWidth(35);
				sheetCanard.setColumnWidth(1, 2048);
				sheetCanard.setColumnWidth(2, 3840);
			}

			int rownumCanard = 1;
			for (int j = 1; j < dataListCanard.size(); j++) {
				objArrCanard = dataListCanard.get(j);
				rowCanard = sheetCanard.createRow(rownumCanard++);
				cellnumCanard = 0;
				for (Object obj : objArrCanard) {
					Cell cell = rowCanard.createCell(cellnumCanard++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// NACELLES WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListNacelles.add(new Object[] {"Total Reference Mass","kg", _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalMassReference().doubleValue(SI.KILOGRAM)});
			dataListNacelles.add(new Object[] {"Total Mass Estimated","kg", 
					_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalMassEstimated().doubleValue(SI.KILOGRAM), 
					_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalPercentDifference()[0]
			});
			dataListNacelles.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getNacellesCalibrationFactor()});
			dataListNacelles.add(new Object[] {"Total Mass Estimated (calibrated)","kg", _nacellesMass.doubleValue(SI.KILOGRAM)});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH NACELLE"});
			dataListNacelles.add(new Object[] {" "});
			for(int iNacelle = 0; iNacelle < _theWeightsManagerInterface.getTheAircraft().getNacelles().getNacellesNumber(); iNacelle++) {
				dataListNacelles.add(new Object[] {"NACELLE " + (iNacelle+1)});
				int indexNacelles=0;
				for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getMassMap().keySet()) {
					if(_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getMassMap().get(methods) != null) 
						dataListNacelles.add(
								new Object[] {
										methods.toString(),
										"kg",
										_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
										_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getPercentDifference()[indexNacelles]
								}
								);
					indexNacelles++;
				}
				dataListNacelles.add(new Object[] {"Reference Mass","kg", _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getMassRefereceList().get(iNacelle)});
				dataListNacelles.add(new Object[] {"Estimated Mass ","kg", _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getMassEstimatedList().get(0).doubleValue(SI.KILOGRAM)});
				dataListNacelles.add(new Object[] {" "});
			}
			
			Row rowNacelles = sheetNacelles.createRow(0);
			Object[] objArrNacelles = dataListNacelles.get(0);
			int cellnumNacelles = 0;
			for (Object obj : objArrNacelles) {
				Cell cell = rowNacelles.createCell(cellnumNacelles++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetNacelles.setDefaultColumnWidth(35);
				sheetNacelles.setColumnWidth(1, 2048);
				sheetNacelles.setColumnWidth(2, 3840);
			}

			int rownumNacelles = 1;
			for (int j = 1; j < dataListNacelles.size(); j++) {
				objArrNacelles = dataListNacelles.get(j);
				rowNacelles = sheetNacelles.createRow(rownumNacelles++);
				cellnumNacelles = 0;
				for (Object obj : objArrNacelles) {
					Cell cell = rowNacelles.createCell(cellnumNacelles++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// POWER PLANT WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListPowerPlant.add(new Object[] {"Total Reference Mass","kg", _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalMassReference().doubleValue(SI.KILOGRAM)});
			dataListPowerPlant.add(new Object[] {"Total Mass Estimated","kg", 
					_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalMassEstimated().doubleValue(SI.KILOGRAM),
					_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalPercentDifference()[0]
			});
			dataListPowerPlant.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getPowerPlantCalibrationFactor()});
			dataListPowerPlant.add(new Object[] {"Total Mass Estimated (calibrated)","kg", _powerPlantMass.doubleValue(SI.KILOGRAM)});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH ENGINE"});
			dataListPowerPlant.add(new Object[] {" "});
			for(int iEngine = 0; iEngine < _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getEngineNumber(); iEngine++) {
				dataListPowerPlant.add(new Object[] {"ENGINE " + (iEngine+1)});
				dataListPowerPlant.add(new Object[] {"Reference Mass","kg", _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getMassRefereceList().get(iEngine)});
				int indexEngine=0;
				for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getMassMap().keySet()) {
					if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getMassMap().get(methods) != null) 
						dataListPowerPlant.add(
								new Object[] {
										methods.toString(),
										"kg",
										_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
										_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getPercentDifference()[indexEngine]
								}
								);
					indexEngine++;
				}
				dataListPowerPlant.add(new Object[] {"Estimated Mass ","kg", _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getMassEstimatedList().get(0).doubleValue(SI.KILOGRAM)});
				dataListPowerPlant.add(new Object[] {" "});
				}
			
			Row rowEngines = sheetPowerPlant.createRow(0);
			Object[] objArrEngines = dataListPowerPlant.get(0);
			int cellnumEngines = 0;
			for (Object obj : objArrEngines) {
				Cell cell = rowEngines.createCell(cellnumEngines++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetPowerPlant.setDefaultColumnWidth(35);
				sheetPowerPlant.setColumnWidth(1, 2048);
				sheetPowerPlant.setColumnWidth(2, 3840);
			}

			int rownumEngines = 1;
			for (int j = 1; j < dataListPowerPlant.size(); j++) {
				objArrEngines = dataListPowerPlant.get(j);
				rowEngines = sheetPowerPlant.createRow(rownumEngines++);
				cellnumEngines = 0;
				for (Object obj : objArrEngines) {
					Cell cell = rowEngines.createCell(cellnumEngines++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// LANDING GEARS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListLandingGears.add(new Object[] {"Overall Reference Mass","kg", getLandingGearsReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListLandingGears.add(new Object[] 
					{"Estimated Overall Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListLandingGears.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getLandingGearsCalibrationFactor()});
			dataListLandingGears.add(new Object[] {"Estimated Overall Mass (calibrated)", "kg", _landingGearsMass.doubleValue(SI.KILOGRAM)});
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexLandingGears=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassMap().get(methods) != null) {
					dataListLandingGears.add(new Object[] {"OVERALL WEIGHT:"});
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getPercentDifference()[indexLandingGears]
							}
							);
					dataListLandingGears.add(new Object[] {"FRONT GEAR WEIGHT:"});
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getFrontGearMassMap().get(methods).doubleValue(SI.KILOGRAM)
							}
							);
					dataListLandingGears.add(new Object[] {"MAIN GEAR WEIGHT:"});
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMainGearMassMap().get(methods).doubleValue(SI.KILOGRAM)
							}
							);
				}
				indexLandingGears++;
			}

			Row rowLandingGears = sheetLandingGears.createRow(0);
			Object[] objArrLandingGears = dataListLandingGears.get(0);
			int cellnumLandingGears = 0;
			for (Object obj : objArrLandingGears) {
				Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetLandingGears.setDefaultColumnWidth(35);
				sheetLandingGears.setColumnWidth(1, 2048);
				sheetLandingGears.setColumnWidth(2, 3840);
			}

			int rownumLandingGears = 1;
			for (int j = 1; j < dataListLandingGears.size(); j++) {
				objArrLandingGears = dataListLandingGears.get(j);
				rowLandingGears = sheetLandingGears.createRow(rownumLandingGears++);
				cellnumLandingGears = 0;
				for (Object obj : objArrLandingGears) {
					Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// SYSTEMS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theWeightsManagerInterface.getTheAircraft().getSystems() != null) {
			Sheet sheetSystems = wb.createSheet("SYSTEMS");
			List<Object[]> dataListSystems = new ArrayList<>();
			dataListSystems.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListSystems.add(new Object[] {"Reference Mass","kg", getSystemsReferenceMass().doubleValue(SI.KILOGRAM)});
			dataListSystems.add(new Object[] 
					{"Overall Estimated Mass ",
							"kg",
							_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM),
							_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).
							minus(_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							divide(_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassReference().to(SI.KILOGRAM)).
							getEstimatedValue()*100
					});
			dataListSystems.add(new Object[] {"Overall Estimated Mass (calibrated)","kg", 
					_apuMass.to(SI.KILOGRAM)
					.plus(_airConditioningAndAntiIcingSystemMass.to(SI.KILOGRAM))
					.plus(_instrumentsAndNavigationSystemMass.to(SI.KILOGRAM))
					.plus(_electricalSystemsMass.to(SI.KILOGRAM))
					.plus(_hydraulicAndPneumaticSystemMass.to(SI.KILOGRAM))
					.plus(_controlSurfaceMass.to(SI.KILOGRAM))
					.plus(_furnishingsAndEquipmentsMass.to(SI.KILOGRAM))
					.doubleValue(SI.KILOGRAM)
					});
			dataListSystems.add(new Object[] {" "});
			dataListSystems.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexSystems=0;
			for(MethodEnum methods : _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassMap().keySet()) {
				if(_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassMap().get(methods) != null) {
					dataListSystems.add(new Object[] {"OVERALL WEIGHT:"});
					dataListSystems.add(
							new Object[] {
									"Method: " + methods.toString(),
									"kg",
									_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassMap().get(methods).doubleValue(SI.KILOGRAM),
									_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getPercentDifference()[indexSystems]
							}
							);
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"APU:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedAPU().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getAPUCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _apuMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"AIR CONDITIONING AND ANTI-ICING SYSTEM:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedAirConditioningAndAntiIcing().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getAirConditioningAndAntiIcingSystemCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _airConditioningAndAntiIcingSystemMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"INSTRUMENTS AND NAVIGATION SYSTEM:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedInstrumentsAndNavigation().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getInstrumentsAndNavigationSystemCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _instrumentsAndNavigationSystemMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"HYDRAULIC AND PNEUMATIC SYSTEMS:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedHydraulicAndPneumatic().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getHydraulicAndPneumaticCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _hydraulicAndPneumaticSystemMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"ELECTRICAL SYSTEMS:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedElectricalSystems().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getElectricalSystemsCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _electricalSystemsMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"CONTROL SURFACES:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedControlSurface().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getControlSurfaceCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _controlSurfaceMass.doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {" "});
					dataListSystems.add(new Object[] {"FURNISHINGS AND EQUIPMENTS:"});
					dataListSystems.add(new Object[] {"Mass", "kg", _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimatedFurnishingsAndEquipment().doubleValue(SI.KILOGRAM)});
					dataListSystems.add(new Object[] {"Calibration Factor","", _theWeightsManagerInterface.getFurnishingsAndEquipmentsCalibrationFactor()});
					dataListSystems.add(new Object[] {"Mass (calibrated)", "kg", _furnishingsAndEquipmentsMass.doubleValue(SI.KILOGRAM)});
				}
				
				indexSystems++;
			}
			

			Row rowSystems = sheetSystems.createRow(0);
			Object[] objArrSystems = dataListSystems.get(0);
			int cellnumSystems = 0;
			for (Object obj : objArrSystems) {
				Cell cell = rowSystems.createCell(cellnumSystems++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetSystems.setDefaultColumnWidth(35);
				sheetSystems.setColumnWidth(1, 2048);
				sheetSystems.setColumnWidth(2, 3840);
			}

			int rownumSystems = 1;
			for (int j = 1; j < dataListSystems.size(); j++) {
				objArrSystems = dataListSystems.get(j);
				rowSystems = sheetSystems.createRow(rownumSystems++);
				cellnumSystems = 0;
				for (Object obj : objArrSystems) {
					Cell cell = rowSystems.createCell(cellnumSystems++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}

	public void plotWeightBreakdown(String weightsFolderPath) {
		
		List<String> labels = new ArrayList<>();
		List<Double> values = new ArrayList<>();
		
		Double fuselageMass = 0.0;
		Double wingMass = 0.0;
		Double hTailMass = 0.0;
		Double vTailMass = 0.0;
		Double canardMass = 0.0;
		Double nacellesMass = 0.0;
		Double landingGearsMass = 0.0;
		
		Double powerPlantMass = 0.0;
		Double systemsMass = 0.0;
		
		Double operatingItemMass = 0.0;
		Double crewMass = 0.0;
		
		Double maxPassengersMass = 0.0;
		
		Double maxFuelMass = 0.0;
		
		Double maxTakeOffMass = _theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM);
		
		if(_theWeightsManagerInterface.getTheAircraft().getFuselage() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassEstimated() != null) {
				labels.add("Fuselage");
				fuselageMass = _theWeightsManagerInterface.getTheAircraft().getFuselage().getTheWeight().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(fuselageMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getWing() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassEstimated() != null) {
				labels.add("Wing");
				wingMass = _theWeightsManagerInterface.getTheAircraft().getWing().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(wingMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getHTail() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassEstimated() != null) {
				labels.add("Horizontal Tail");
				hTailMass = _theWeightsManagerInterface.getTheAircraft().getHTail().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(hTailMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getVTail() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassEstimated() != null) {
				labels.add("Vertical Tail");
				vTailMass = _theWeightsManagerInterface.getTheAircraft().getVTail().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(vTailMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getCanard() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassEstimated() != null) {
				labels.add("Canard");
				canardMass = _theWeightsManagerInterface.getTheAircraft().getCanard().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(canardMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getNacelles() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalMassEstimated() != null) {
				labels.add("Nacelles");
				nacellesMass = _theWeightsManagerInterface.getTheAircraft().getNacelles().getTheWeights().getTotalMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(nacellesMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getLandingGears() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassEstimated() != null) {
				labels.add("Landing Gears");
				landingGearsMass = _theWeightsManagerInterface.getTheAircraft().getLandingGears().getTheWeigths().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(landingGearsMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalMassEstimated() != null) {
				labels.add("Power Plant");
				powerPlantMass = _theWeightsManagerInterface.getTheAircraft().getPowerPlant().getTheWeights().getTotalMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(powerPlantMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getSystems() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimated() != null) {
				labels.add("Systems");
				systemsMass = _theWeightsManagerInterface.getTheAircraft().getSystems().getTheWeightManager().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(systemsMass/maxTakeOffMass*100.0);
			}
		if(_theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getOperatingItemMass() != null) {
			labels.add("Operating Items");
			operatingItemMass = _theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getOperatingItemMass().doubleValue(SI.KILOGRAM);
			values.add(operatingItemMass/maxTakeOffMass*100.0);
		}
		if(_theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getCrewMass() != null) {
			labels.add("Crew");
			crewMass = _theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getCrewMass().doubleValue(SI.KILOGRAM);
			values.add(crewMass/maxTakeOffMass*100.0);
		}
		if(_theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getPaxMassMax() != null) {
			labels.add("Passengers");
			maxPassengersMass = _theWeightsManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().getPaxMassMax().doubleValue(SI.KILOGRAM);
			values.add(maxPassengersMass/maxTakeOffMass*100.0);
		}
		if(_theWeightsManagerInterface.getTheAircraft().getFuelTank() != null)
			if(_theWeightsManagerInterface.getTheAircraft().getFuelTank().getFuelMass() != null) {
				labels.add("Fuel");
				maxFuelMass = _theWeightsManagerInterface.getTheAircraft().getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM);
				values.add(maxFuelMass/maxTakeOffMass*100.0);
			}

		MyChartToFileUtils.plotPieChart(
				labels, 
				values, 
				"Weights Breakdown", 
				false, 
				weightsFolderPath, 
				"Weights_Breakdown"
				);
		
	}
	
	/** 
	 * Calculate mass of selected configuration. When comparing some/all available methods 
	 * for the selected component the iterative procedure is done using the first selected method.
	 * 
	 * @param aircraft
	 * @param methodsMap
	 */
	public void calculateAllMasses(
			Aircraft aircraft,
			OperatingConditions operatingConditions,
			Map <ComponentEnum, List<MethodEnum>> methodsMap
			) {

		System.out.println("\n-----------------------------------------------");
		System.out.println("----- WEIGHT ESTIMATION PROCEDURE STARTED -----");
		System.out.println("-----------------------------------------------\n");
		
		initializeData();
		
		/*
		 * Initialization of the _maximumZeroFuelMass and _manufacturerEmptyMass. 
		 * For the first iteration a value equal to 75% and 50% of the_maximumTakeOffMass has been chosen respectively.
		 * From the second iteration on, the estimated value of the _maximumZeroFuelMass (at iteration i-1) will be used. 
		 * (_maximumZeroFuelWeight, _maximumTakeOffWeight and _maximumLandingMass must be calculated since they are used by
		 *  LiftingSurface and LandingGears to estimate their masses)
		 */
		_maximumTakeOffMass = _theWeightsManagerInterface.getFirstGuessMaxTakeOffMass();
		_manufacturerEmptyMass = _theWeightsManagerInterface.getFirstGuessMaxTakeOffMass().to(SI.KILOGRAM).times(0.5);
		_maximumZeroFuelMass = _theWeightsManagerInterface.getFirstGuessMaxTakeOffMass().to(SI.KILOGRAM).times(0.75);
		_maximumLandingMass = _theWeightsManagerInterface.getFirstGuessMaxTakeOffMass().to(SI.KILOGRAM).times(0.97);
		
		/*
		 * The maximum take-off mass will be stored in a dedicated list to evaluate the while condition. 
		 */
		_maximumTakeOffMassList.add(Amount.valueOf(0.0, SI.KILOGRAM));
		_maximumTakeOffMassList.add(_theWeightsManagerInterface.getFirstGuessMaxTakeOffMass().to(SI.KILOGRAM));
		
		/*
		 * The while loop will evaluate all the aircraft masses until the maximum take-off changes less then the threshold value 
		 */
		int i=1;
		while ((Math.abs((_maximumTakeOffMassList.get(i).minus(_maximumTakeOffMassList.get(i-1)))
				.divide(_maximumTakeOffMassList.get(i))
				.getEstimatedValue())
				* 100
				>= 0.01)
				) {
			
			/*
			 * Initialization of all components weight managers and reference masses.
			 */
			initializeComponentsWeightManagers();
			initializeReferenceMasses();
			
			/*
			 * The method estimateComponentsReferenceMasses is used to estimate all the components reference masses.
			 */
			estimateComponentsReferenceMasses(aircraft);
			calculateFuelMasses(aircraft);
			
			/*
			 * All the following methods are use to estimate all the aircraft masses.
			 */
			
			calculateSystemsAndPowerPlantMasses(aircraft, methodsMap);
			// --- END OF SYSTEMS AND POWER PLANT MASS-----------------------------------
			
			calculateStructuralMass(aircraft, operatingConditions, methodsMap);
			// --- END OF STRUCTURE MASS-----------------------------------
			
			calculateManufacturerEmptyMass();
			// --- END OF MANUFACTURER EMPTY MASS-----------------------------------
			
			calculateOperatingEmptyMass();
			// --- END OF OPERATING EMPTY MASS-----------------------------------
			
			calculateEmptyMass();
			// --- END OF EMPTY MASS-----------------------------------
			
			calculateMaximumZeroFuelMass(aircraft);
			// --- END ZERO FUEL MASS-----------------------------------
			
			calculateMaximumTakeOffMass();
			// --- END TAKE-OFF MASS-----------------------------------
			
			calculateMaximumLandingMass();
			// --- END LANDING MASS-----------------------------------
			
			_maximumTakeOffMassList.add(_maximumTakeOffMass.to(SI.KILOGRAM));
			System.out.println("Iteration " + (i) + " --> Maximum Take-Off Mass: " + _maximumTakeOffMass);
			
			i++;
			
			if (i > _maxIteration) {
				System.err.println("WARNING (WEIHGTS ANALYSIS): MAXIMUM NUMBER OF ITERATION REACHED!! THE LAST CALCULATED VALUE WILL BE USED ...");
				break;
			}
			
		}
		
		System.out.println("\n-----------------------------------------------");
		System.out.println("--- WEIGHT ESTIMATION PROCEDURE COMPLETED -----");
		System.out.println("-----------------------------------------------\n");
		
	}

	private void calculateSystemsAndPowerPlantMasses (Aircraft aircraft, Map <ComponentEnum, List<MethodEnum>> methodsMap) {
		
		if (aircraft.getPowerPlant() != null) {
			aircraft.getPowerPlant().getTheWeights().calculateTotalMass(aircraft, methodsMap);
			_powerPlantMass = aircraft.getPowerPlant().getTheWeights().getTotalMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getPowerPlantCalibrationFactor()
					);
		}
		if (aircraft.getSystems() != null) {
			aircraft.getSystems().getTheWeightManager().calculateMass(aircraft, methodsMap);
			_apuMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedAPU().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getAPUCalibrationFactor()
					);
			_airConditioningAndAntiIcingSystemMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedAirConditioningAndAntiIcing().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getAirConditioningAndAntiIcingSystemCalibrationFactor()
					);
			_instrumentsAndNavigationSystemMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedInstrumentsAndNavigation().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getInstrumentsAndNavigationSystemCalibrationFactor()
					);
			_hydraulicAndPneumaticSystemMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedHydraulicAndPneumatic().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getHydraulicAndPneumaticCalibrationFactor()
					);
			_electricalSystemsMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedElectricalSystems().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getElectricalSystemsCalibrationFactor()
					);
			_controlSurfaceMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedControlSurface().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getControlSurfaceCalibrationFactor()
					);
			_furnishingsAndEquipmentsMass = aircraft.getSystems().getTheWeightManager().getMassEstimatedFurnishingsAndEquipment().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getFurnishingsAndEquipmentsCalibrationFactor()
					);
		}
		
	}
	
	private void calculateStructuralMass(
			Aircraft aircraft, 
			OperatingConditions operatingConditions,
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		if(aircraft.getNacelles() != null) {
			aircraft.getNacelles().getTheWeights().calculateTotalMass(aircraft, methodsMap);
			_nacellesMass = aircraft.getNacelles().getTheWeights().getTotalMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getNacellesCalibrationFactor()
					);
		}
		if(aircraft.getWing() != null) {
			aircraft.getWing().getTheWeightManager().calculateMass(aircraft, ComponentEnum.WING, methodsMap);
			_wingMass = aircraft.getWing().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getWingCalibrationFactor()
					);
		}
		if(aircraft.getFuselage() != null) {
			aircraft.getFuselage().getTheWeight().calculateMass(aircraft, operatingConditions, methodsMap);
			_fuselageMass = aircraft.getFuselage().getTheWeight().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getFuselageCalibrationFactor()
					);
		}
		if(aircraft.getHTail() != null) {
			aircraft.getHTail().getTheWeightManager().calculateMass(aircraft, ComponentEnum.HORIZONTAL_TAIL, methodsMap);
			_hTailMass = aircraft.getHTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getHTailCalibrationFactor()
					);
		}
		if(aircraft.getVTail() != null) {
			aircraft.getVTail().getTheWeightManager().calculateMass(aircraft, ComponentEnum.VERTICAL_TAIL, methodsMap);
			_vTailMass = aircraft.getVTail().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getVTailCalibrationFactor()
					);
		}
		if(aircraft.getCanard() != null) {
			aircraft.getCanard().getTheWeightManager().calculateMass(aircraft, ComponentEnum.CANARD, methodsMap);
			_canardMass = aircraft.getCanard().getTheWeightManager().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getCanardCalibrationFactor()
					);
		}
		if(aircraft.getLandingGears() != null) {
			aircraft.getLandingGears().getTheWeigths().calculateMass(aircraft, methodsMap);
			_landingGearsMass = aircraft.getLandingGears().getTheWeigths().getMassEstimated().to(SI.KILOGRAM).times(
					_theWeightsManagerInterface.getLandingGearsCalibrationFactor()
					);
		}

		_structuralMass = _fuselageMass.to(SI.KILOGRAM)
				.plus(_wingMass.to(SI.KILOGRAM))
				.plus(_hTailMass.to(SI.KILOGRAM))
				.plus(_vTailMass.to(SI.KILOGRAM))
				.plus(_canardMass.to(SI.KILOGRAM))
				.plus(_nacellesMass.to(SI.KILOGRAM))
				.plus(_landingGearsMass.to(SI.KILOGRAM));
		
	}

	private void calculateManufacturerEmptyMass() {
		
		_manufacturerEmptyMass = 
				_structuralMass.to(SI.KILOGRAM)
				.plus(_powerPlantMass.to(SI.KILOGRAM))
				.plus(_apuMass.to(SI.KILOGRAM))
				.plus(_airConditioningAndAntiIcingSystemMass.to(SI.KILOGRAM))
				.plus(_instrumentsAndNavigationSystemMass.to(SI.KILOGRAM))
				.plus(_hydraulicAndPneumaticSystemMass.to(SI.KILOGRAM))
				.plus(_electricalSystemsMass.to(SI.KILOGRAM))
				.plus(_controlSurfaceMass.to(SI.KILOGRAM))
				.plus(_furnishingsAndEquipmentsMass.to(SI.KILOGRAM));
	}
	
	private void calculateOperatingEmptyMass() {
		
		_operatingEmptyMass = 
				_manufacturerEmptyMass.to(SI.KILOGRAM)
				.plus(_operatingItemMass.to(SI.KILOGRAM))
				.plus(_crewMass.to(SI.KILOGRAM));
		
	}

	private void calculateMaximumZeroFuelMass(Aircraft aircraft) {
		
		// Zero fuel mass
		_zeroFuelMass = 
				_operatingEmptyMass.to(SI.KILOGRAM)
				.plus(_paxMass.to(SI.KILOGRAM));

		// Maximum zero fuel mass
		aircraft.getTheAnalysisManager().getTheWeights().setMaximumZeroFuelMass(
				aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
						_paxMassMax));
		
	}
	
	private void calculateMaximumTakeOffMass() {
		
		// Take-off mass
		_takeOffMass = 
				_zeroFuelMass.to(SI.KILOGRAM)
				.plus(_fuelMass.to(SI.KILOGRAM));

		// Maximum take-off mass
		_maximumTakeOffMass = 
				_maximumZeroFuelMass.to(SI.KILOGRAM)
				.plus(_fuelMass.to(SI.KILOGRAM));
		
	}
	
	private void calculateMaximumLandingMass() {
		
		_maximumLandingMass = _maximumTakeOffMass.times(0.9);
		
	}
	
	private void calculateFuelMasses(Aircraft aircraft) {
		
		//..........................................................
		// MAX FUEL MASS
		//..........................................................
		aircraft.getFuelTank().calculateFuelMass();
		
		//..........................................................
		// MISSION FUEL MASS
		//..........................................................
		
		if(_theWeightsManagerInterface.getEstimateMissionFuelFlag() == false)
			
			_fuelMass = _theWeightsManagerInterface.getMissionFuel().to(SI.KILOGRAM);
		
		else {

			// calculating the mass ratios from statistical values...
			double[][] fuelFractionTable = null;
			double mffRatio = 1.0;

			try {
				fuelFractionTable = _fuelFractionDatabaseReader.getFuelFractionTable("FuelFractions_Roskam");
			} catch (HDF5LibraryException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			switch (aircraft.getTypeVehicle()){

			case TURBOPROP: int indexTurboprop = 5;
			for (int i=0; i<6; i++) 
				mffRatio *= fuelFractionTable[indexTurboprop][i];
			break;

			case JET: int indexJet = 6;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexJet][i]; 
			break;

			case BUSINESS_JET: int indexBusinessJet = 4;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexBusinessJet][i]; 
			break;

			case FIGHTER: int indexFighter = 8;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexFighter][i]; 
			break;

			case ACROBATIC: int indexAcrobatic = 7;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexAcrobatic][i]; 
			break;

			case COMMUTER: int indexCommuter = 5;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexCommuter][i]; 
			break;

			case GENERAL_AVIATION:
				if(aircraft.getPowerPlant().getEngineNumber() == 1) {
					int indexSingleEngine = 1;	
					for (int i=0; i<6; i++)
						mffRatio *= fuelFractionTable[indexSingleEngine][i];
				}
				else if(aircraft.getPowerPlant().getEngineNumber() == 2) {
					int indexTwinEngine = 2;
					for (int i=0; i<6; i++)
						mffRatio *= fuelFractionTable[indexTwinEngine][i];
				}
				break;
			}

			// calculating the mass ratios of cruise, alternate and holding phases (ASSUMPTION: BREGUET JET FOR EACH CONDITION)...
			double massRatioCruise = 1.0;
			double massRatioAlternate = 1.0;
			double massRatioHolding = 1.0;

			massRatioCruise = 1/Math.exp(
					(_theWeightsManagerInterface.getCruiseRange().doubleValue(NonSI.NAUTICAL_MILE)*_theWeightsManagerInterface.getCruiseSFC())
					/(_theWeightsManagerInterface.getCruiseEfficiency()*
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											_theWeightsManagerInterface.getTheOperatingConditions().getMachCruise(),
											_theWeightsManagerInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioAlternate = 1/Math.exp(
					(_theWeightsManagerInterface.getAlternateCruiseRange().doubleValue(NonSI.NAUTICAL_MILE)*_theWeightsManagerInterface.getAlternateCruiseSFC())
					/(_theWeightsManagerInterface.getAlternateCruiseEfficiency()*
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											_theWeightsManagerInterface.getAlternateCruiseMachNumber(),
											_theWeightsManagerInterface.getAlternateCruiseAltitide().doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioHolding = 1/Math.exp(
					(_theWeightsManagerInterface.getHoldingDuration().doubleValue(NonSI.HOUR)*_theWeightsManagerInterface.getHoldingSFC())
					/(_theWeightsManagerInterface.getHoldingEfficiency())
					);

			// calculating the mission fuel mass ...
			mffRatio *= massRatioCruise*massRatioAlternate*massRatioHolding;

			_fuelMass = _maximumTakeOffMass.times(1-mffRatio);
		}
	}

	private void calculateEmptyMass() {

		if(_maximumTakeOffMass.doubleValue(NonSI.POUND) < 100000)
			_trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
		else
			_trappedFuelOilMass = _maximumTakeOffMass.times(0.005);

		_emptyMass =_operatingEmptyMass.minus(_crewMass).minus(_trappedFuelOilMass);

	}
	
	private void estimateComponentsReferenceMasses(Aircraft aircraft) {
		
		/*
		 * @see: STANFORD STATISTICS
		 * 
		 * ESTIMATION OF THE REFERENCE MASS OF EACH COMPONENT 
		 */
		if(aircraft.getFuselage() != null) 
			if(this.getFuselageReferenceMass() == null) {
				this.setFuselageReferenceMass(_maximumTakeOffMass.times(.10));
				aircraft.getFuselage().getTheWeight().setMassReference(getFuselageReferenceMass());
			}
		if(aircraft.getWing() != null)
			if(this.getWingReferenceMass() == null) {
				this.setWingReferenceMass(_maximumTakeOffMass.times(.106));
				aircraft.getWing().getTheWeightManager().setMassReference(getWingReferenceMass());
			}
		if(aircraft.getHTail() != null) 
			if(this.getHorizontalTailReferenceMass() == null) {
				this.setHorizontalTailReferenceMass(_maximumTakeOffMass.times(.0115));
				aircraft.getHTail().getTheWeightManager().setMassReference(getHorizontalTailReferenceMass());
			}
		if(aircraft.getVTail() != null)
			if(this.getVerticalTailReferenceMass() == null) {
				this.setVerticalTailReferenceMass(_maximumTakeOffMass.times(.0115));
				aircraft.getVTail().getTheWeightManager().setMassReference(getVerticalTailReferenceMass());
			}
		if(aircraft.getCanard() != null)
			if(this.getCanardReferenceMass() == null) {
				this.setCanardReferenceMass(_maximumTakeOffMass.times(.01));
				aircraft.getCanard().getTheWeightManager().setMassReference(getCanardReferenceMass());
			}
		if(aircraft.getPowerPlant() != null)
			if(this.getPowerPlantReferenceMass() == null) {
				this.setPowerPlantReferenceMass(_maximumTakeOffMass.times(.083));
				aircraft.getPowerPlant().getTheWeights().estimateReferenceMasses(_theWeightsManagerInterface.getTheAircraft());
			}
		if(aircraft.getNacelles() != null)
			if(this.getNacellesReferenceMass() == null) {
				this.setNacellesReferenceMass(_maximumTakeOffMass.times(.019));
				aircraft.getNacelles().getTheWeights().estimateReferenceMasses(_theWeightsManagerInterface.getTheAircraft());
			}
		if(aircraft.getLandingGears() != null) 
			if(this.getLandingGearsReferenceMass() == null) {
				this.setLandingGearsReferenceMass(_maximumTakeOffMass.times(.041));
				aircraft.getLandingGears().getTheWeigths().setMassReference(getLandingGearsReferenceMass());
			}
		if(aircraft.getSystems() != null)
			if(this.getSystemsReferenceMass() == null) {
				setSystemsReferenceMass(_maximumTakeOffMass.times(.136));
				aircraft.getSystems().getTheWeightManager().setMassReference(getSystemsReferenceMass());
			}
	}
	
	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	public IACWeightsManager getTheWeightsManagerInterface() {
		return _theWeightsManagerInterface;
	}

	public void setTheWeightsManagerInterface(IACWeightsManager _theWeightsManagerInterface) {
		this._theWeightsManagerInterface = _theWeightsManagerInterface;
	}

	public static int getMaxIteration() {
		return _maxIteration;
	}

	public static void setMaxIteration(int _maxIteration) {
		ACWeightsManager._maxIteration = _maxIteration;
	}

	public Amount<VolumetricDensity> getMaterialDensity() {
		return _materialDensity;
	}

	public void setMaterialDensity(Amount<VolumetricDensity> _materialDensity) {
		this._materialDensity = _materialDensity;
	}

	public FuelFractionDatabaseReader getFuelFractionDatabaseReader() {
		return _fuelFractionDatabaseReader;
	}

	public void setFuelFractionDatabaseReader(FuelFractionDatabaseReader _fuelFractionDatabaseReader) {
		this._fuelFractionDatabaseReader = _fuelFractionDatabaseReader;
	}

	public Amount<Mass> getFuselageMass() {
		return _fuselageMass;
	}

	public void setFuselageMass(Amount<Mass> _fuselageMass) {
		this._fuselageMass = _fuselageMass;
	}

	public Amount<Mass> getWingMass() {
		return _wingMass;
	}

	public void setWingMass(Amount<Mass> _wingMass) {
		this._wingMass = _wingMass;
	}

	public Amount<Mass> getHTailMass() {
		return _hTailMass;
	}

	public void setHTailMass(Amount<Mass> _hTailMass) {
		this._hTailMass = _hTailMass;
	}

	public Amount<Mass> getVTailMass() {
		return _vTailMass;
	}

	public void setVTailMass(Amount<Mass> _vTailMass) {
		this._vTailMass = _vTailMass;
	}

	public Amount<Mass> getCanardMass() {
		return _canardMass;
	}

	public void setCanardMass(Amount<Mass> _canardMass) {
		this._canardMass = _canardMass;
	}

	public Amount<Mass> getNacellesMass() {
		return _nacellesMass;
	}

	public void setNacellesMass(Amount<Mass> _nacellesMass) {
		this._nacellesMass = _nacellesMass;
	}

	public Amount<Mass> getPowerPlantMass() {
		return _powerPlantMass;
	}

	public void setPowerPlantMass(Amount<Mass> _powerPlantMass) {
		this._powerPlantMass = _powerPlantMass;
	}

	public Amount<Mass> getLandingGearsMass() {
		return _landingGearsMass;
	}

	public void setLandingGearsMass(Amount<Mass> _landingGearsMass) {
		this._landingGearsMass = _landingGearsMass;
	}

	public Amount<Mass> getAPUMass() {
		return _apuMass;
	}

	public void setAPUMass(Amount<Mass> _apuMass) {
		this._apuMass = _apuMass;
	}

	public Amount<Mass> getAirConditioningAndAntiIcingSystemMass() {
		return _airConditioningAndAntiIcingSystemMass;
	}

	public void setAirConditioningAndAntiIcingSystemMass(Amount<Mass> _airConditioningAndAntiIcingSystemMass) {
		this._airConditioningAndAntiIcingSystemMass = _airConditioningAndAntiIcingSystemMass;
	}

	public Amount<Mass> getInstrumentsAndNavigationSystemMass() {
		return _instrumentsAndNavigationSystemMass;
	}

	public void setInstrumentsAndNavigationSystemMass(Amount<Mass> _instrumentsAndNavigationSystemMass) {
		this._instrumentsAndNavigationSystemMass = _instrumentsAndNavigationSystemMass;
	}

	public Amount<Mass> getElectricalSystemsMass() {
		return _electricalSystemsMass;
	}

	public void setElectricalSystemsMass(Amount<Mass> _electricalSystemsMass) {
		this._electricalSystemsMass = _electricalSystemsMass;
	}

	public Amount<Mass> getControlSurfaceMass() {
		return _controlSurfaceMass;
	}

	public void setControlSurfaceMass(Amount<Mass> _controlSurfaceMass) {
		this._controlSurfaceMass = _controlSurfaceMass;
	}

	public Amount<Mass> getFurnishingsAndEquipmentsMass() {
		return _furnishingsAndEquipmentsMass;
	}

	public void setFurnishingsAndEquipmentsMass(Amount<Mass> _furnishingsAndEquipmentsMass) {
		this._furnishingsAndEquipmentsMass = _furnishingsAndEquipmentsMass;
	}

	public Amount<Mass> getPaxMass() {
		return _paxMass;
	}

	public void setPaxMass(Amount<Mass> _paxMass) {
		this._paxMass = _paxMass;
	}

	public Amount<Mass> getPaxMassMax() {
		return _paxMassMax;
	}

	public void setPaxMassMax(Amount<Mass> _paxMassMax) {
		this._paxMassMax = _paxMassMax;
	}

	public Amount<Mass> getCrewMass() {
		return _crewMass;
	}

	public void setCrewMass(Amount<Mass> _creMass) {
		this._crewMass = _creMass;
	}

	public Amount<Mass> getStructuralMass() {
		return _structuralMass;
	}

	public void setStructuralMass(Amount<Mass> _structuralMass) {
		this._structuralMass = _structuralMass;
	}

	public Amount<Mass> getOperatingItemMass() {
		return _operatingItemMass;
	}

	public void setOperatingItemMass(Amount<Mass> _operatingItemMass) {
		this._operatingItemMass = _operatingItemMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}

	public Amount<Mass> getEmptyMass() {
		return _emptyMass;
	}

	public void setEmptyMass(Amount<Mass> _emptyMass) {
		this._emptyMass = _emptyMass;
	}

	public Amount<Mass> getMaximumZeroFuelMass() {
		return _maximumZeroFuelMass;
	}

	public void setMaximumZeroFuelMass(Amount<Mass> _maximumZeroFuelMass) {
		this._maximumZeroFuelMass = _maximumZeroFuelMass;
	}

	public Amount<Mass> getMaximumLandingMass() {
		return _maximumLandingMass;
	}

	public void setMaximumLandingMass(Amount<Mass> _maximumLandingMass) {
		this._maximumLandingMass = _maximumLandingMass;
	}

	public Amount<Mass> getManufacturerEmptyMass() {
		return _manufacturerEmptyMass;
	}

	public void setManufacturerEmptyMass(Amount<Mass> _manufacturerEmptyMass) {
		this._manufacturerEmptyMass = _manufacturerEmptyMass;
	}

	public Amount<Mass> getZeroFuelMass() {
		return _zeroFuelMass;
	}

	public void setZeroFuelMass(Amount<Mass> _zeroFuelMass) {
		this._zeroFuelMass = _zeroFuelMass;
	}

	public Amount<Mass> getTakeOffMass() {
		return _takeOffMass;
	}

	public void setTakeOffMass(Amount<Mass> _takeOffMass) {
		this._takeOffMass = _takeOffMass;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}

	public Amount<Mass> getTrappedFuelOilMass() {
		return _trappedFuelOilMass;
	}

	public void setTrappedFuelOilMass(Amount<Mass> _trappedFuelOilMass) {
		this._trappedFuelOilMass = _trappedFuelOilMass;
	}

	public Amount<Mass> getFuelMass() {
		return _fuelMass;
	}

	public void setFuelMass(Amount<Mass> _fuelMass) {
		this._fuelMass = _fuelMass;
	}

	public List<Amount<Mass>> getMaximumTakeOffMassList() {
		return _maximumTakeOffMassList;
	}

	public void setMaximumTakeOffMassList(List<Amount<Mass>> _maximumTakeOffMassList) {
		this._maximumTakeOffMassList = _maximumTakeOffMassList;
	}

	public Amount<Mass> getFuselageReferenceMass() {
		return _fuselageReferenceMass;
	}

	public void setFuselageReferenceMass(Amount<Mass> _fuselageReferenceMass) {
		this._fuselageReferenceMass = _fuselageReferenceMass;
	}

	public Amount<Mass> getWingReferenceMass() {
		return _wingReferenceMass;
	}

	public void setWingReferenceMass(Amount<Mass> _wingReferenceMass) {
		this._wingReferenceMass = _wingReferenceMass;
	}

	public Amount<Mass> getHorizontalTailReferenceMass() {
		return _horizontalTailReferenceMass;
	}

	public void setHorizontalTailReferenceMass(Amount<Mass> _horizontalTailReferenceMass) {
		this._horizontalTailReferenceMass = _horizontalTailReferenceMass;
	}

	public Amount<Mass> getVerticalTailReferenceMass() {
		return _verticalTailReferenceMass;
	}

	public void setVerticalTailReferenceMass(Amount<Mass> _verticalTailReferenceMass) {
		this._verticalTailReferenceMass = _verticalTailReferenceMass;
	}

	public Amount<Mass> getCanardReferenceMass() {
		return _canardReferenceMass;
	}

	public void setCanardReferenceMass(Amount<Mass> _canardReferenceMass) {
		this._canardReferenceMass = _canardReferenceMass;
	}

	public Amount<Mass> getNacellesReferenceMass() {
		return _nacellesReferenceMass;
	}

	public void setNacellesReferenceMass(Amount<Mass> _nacellesReferenceMass) {
		this._nacellesReferenceMass = _nacellesReferenceMass;
	}

	public Amount<Mass> getPowerPlantReferenceMass() {
		return _powerPlantReferenceMass;
	}

	public void setPowerPlantReferenceMass(Amount<Mass> _powerPlantReferenceMass) {
		this._powerPlantReferenceMass = _powerPlantReferenceMass;
	}

	public Amount<Mass> getLandingGearsReferenceMass() {
		return _landingGearsReferenceMass;
	}

	public void setLandingGearsReferenceMass(Amount<Mass> _landingGearsReferenceMass) {
		this._landingGearsReferenceMass = _landingGearsReferenceMass;
	}

	public Amount<Mass> getSystemsReferenceMass() {
		return _systemsReferenceMass;
	}

	public void setSystemsReferenceMass(Amount<Mass> _systemsReferenceMass) {
		this._systemsReferenceMass = _systemsReferenceMass;
	}

	public Amount<Mass> getHydraulicAndPneumaticSystemMass() {
		return _hydraulicAndPneumaticSystemMass;
	}

	public void setHydraulicAndPneumaticSystemMass(Amount<Mass> _hydraulicAndPneumaticSystemMass) {
		this._hydraulicAndPneumaticSystemMass = _hydraulicAndPneumaticSystemMass;
	}

}