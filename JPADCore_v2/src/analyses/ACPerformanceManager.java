package analyses;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;
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

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.performance.ClimbCalc;
import calculators.performance.DescentCalc;
import calculators.performance.FlightManeuveringEnvelopeCalc;
import calculators.performance.LandingCalc;
import calculators.performance.MissionProfileCalc;
import calculators.performance.PayloadRangeCalcMissionProfile;
import calculators.performance.PerformanceCalcUtils;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.TakeOffCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.FlightEnvelopeMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.SpecificRangeMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import junit.framework.AssertionFailedError;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

public class ACPerformanceManager {

	/*
	 *******************************************************************************
	 * @author Vittorio Trifari
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACPerformanceManager _thePerformanceInterface;

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	// Take-Off
	private Map<Double, TakeOffCalc> _theTakeOffCalculatorMap;
	private Map<Double, Amount<Length>> _takeOffDistanceAEOMap;
	private Map<Double, Amount<Length>> _takeOffDistanceFAR25Map;
	private Map<Double, Amount<Length>> _balancedFieldLengthMap;
	private Map<Double, Amount<Length>> _groundRollDistanceTakeOffMap;
	private Map<Double, Amount<Length>> _rotationDistanceTakeOffMap;
	private Map<Double, Amount<Length>> _airborneDistanceTakeOffMap;
	private Map<Double, Amount<Velocity>> _vStallTakeOffMap;
	private Map<Double, Amount<Velocity>> _vMCMap;
	private Map<Double, Amount<Velocity>>_vRotationMap;
	private Map<Double, Amount<Velocity>> _vLiftOffMap;
	private Map<Double, Amount<Velocity>>_v1Map;
	private Map<Double, Amount<Velocity>> _v2Map;
	private Map<Double, Amount<Duration>> _takeOffDurationMap;
	private Map<Double, double[]> _thrustMomentOEIMap;
	private Map<Double, double[]> _yawingMomentOEIMap;
	//..............................................................................
	// Climb
	private Map<Double, ClimbCalc> _theClimbCalculatorMap;
	private Map<Double, List<RCMap>> _rcAEOMap;
	private Map<Double, List<RCMap>> _rcOEIMap;
	private Map<Double, CeilingMap> _ceilingAEOMap;
	private Map<Double, CeilingMap> _ceilingOEIMap;
	private Map<Double, List<DragMap>> _dragListAEOMap;
	private Map<Double, List<ThrustMap>> _thrustListAEOMap;
	private Map<Double, List<DragMap>> _dragListOEIMap;
	private Map<Double, List<ThrustMap>> _thrustListOEIMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeAEOMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeOEIMap;
	
	private Map<Double, Amount<Length>> _absoluteCeilingAEOMap;
	private Map<Double, Amount<Length>> _serviceCeilingAEOMap;
	private Map<Double, Amount<Duration>> _minimumClimbTimeAEOMap;
	private Map<Double, Amount<Duration>> _climbTimeAtSpecificClimbSpeedAEOMap;
	private Map<Double, Amount<Mass>> _fuelUsedDuringClimbMap;
	
	private Map<Double, Amount<Length>> _absoluteCeilingOEIMap;
	private Map<Double, Amount<Length>> _serviceCeilingOEIMap;
	//..............................................................................
	// Cruise
	private Map<Double, List<DragMap>> _dragListAltitudeParameterizationMap;
	private Map<Double, List<ThrustMap>> _thrustListAltitudeParameterizationMap;
	private Map<Double, List<DragMap>> _dragListWeightParameterizationMap;
	private Map<Double, List<ThrustMap>> _thrustListWeightParameterizationMap;
	private Map<Double, List<Amount<Force>>> _weightListCruiseMap;
	
	private Map<Double, List<DragThrustIntersectionMap>> _intersectionListMap;
	private Map<Double, List<FlightEnvelopeMap>> _cruiseEnvelopeListMap;
	
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyWeightMap;
	
	private Map<Double, List<SpecificRangeMap>> _specificRangeMap;
	
	private Map<Double, Amount<Velocity>> _maxSpeesTASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _minSpeesTASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _maxSpeesCASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _minSpeesCASAtCruiseAltitudeMap;
	private Map<Double, Double> _maxMachAtCruiseAltitudeMap;
	private Map<Double, Double> _minMachAtCruiseAltitudeMap;
	private Map<Double, Double> _efficiencyAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Force>> _thrustAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Force>> _dragAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Power>> _powerAvailableAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Power>> _powerNeededAtCruiseAltitudeAndMachMap;
	//..............................................................................
	// Descent
	private Map<Double, DescentCalc> _theDescentCalculatorMap;
	private Map<Double, List<Amount<Length>>> _descentLengthsMap;
	private Map<Double, List<Amount<Duration>>> _descentTimesMap;
	private Map<Double, List<Amount<Angle>>> _descentAnglesMap;
	private Map<Double, Amount<Length>> _totalDescentLengthMap;
	private Map<Double, Amount<Duration>> _totalDescentTimeMap;
	private Map<Double, Amount<Mass>> _totalDescentFuelUsedMap;
	//..............................................................................
	// Landing
	private Map<Double, LandingCalc> _theLandingCalculatorMap;
	private Map<Double, Amount<Length>> _landingDistanceMap;
	private Map<Double, Amount<Length>> _landingDistanceFAR25Map;
	private Map<Double, Amount<Length>> _groundRollDistanceLandingMap;
	private Map<Double, Amount<Length>> _flareDistanceLandingMap;
	private Map<Double, Amount<Length>> _airborneDistanceLandingMap;
	private Map<Double, Amount<Velocity>> _vStallLandingMap;
	private Map<Double, Amount<Velocity>> _vApproachMap;
	private Map<Double, Amount<Velocity>> _vFlareMap;
	private Map<Double, Amount<Velocity>> _vTouchDownMap;
	private Map<Double, Amount<Duration>> _landingDurationMap;
	//..............................................................................
	// Payload-Range
	private Map<Double, PayloadRangeCalcMissionProfile> _thePayloadRangeCalculatorMap;
	
	private Map<Double, Amount<Length>> _rangeAtMaxPayloadMap;
	private Map<Double, Amount<Length>> _rangeAtDesignPayloadMap;
	private Map<Double, Amount<Length>> _rangeAtMaxFuelMap;	
	private Map<Double, Amount<Length>> _rangeAtZeroPayloadMap;
	private Map<Double, Amount<Mass>> _takeOffMassAtZeroPayloadMap;
	private Map<Double, Amount<Mass>> _maxPayloadMap;
	private Map<Double, Amount<Mass>> _designPayloadMap;
	private Map<Double, Amount<Mass>> _payloadAtMaxFuelMap;
	private Map<Double, Integer> _passengersNumberAtMaxPayloadMap;
	private Map<Double, Integer> _passengersNumberAtDesignPayloadMap;
	private Map<Double, Integer> _passengersNumberAtMaxFuelMap;
	private Map<Double, Amount<Mass>> _requiredMassAtMaxPayloadMap;
	private Map<Double, Amount<Mass>> _requiredMassAtDesignPayloadMap;
	
	private Map<Double, List<Amount<Length>>> _rangeArrayMap;
	private Map<Double, List<Double>> _payloadArrayMap;
	
	private Map<Double, double[][]> _rangeMatrixMap;
	private Map<Double, double[][]> _payloadMatrixMap;
	//..............................................................................
	// Maneuvering and Gust Flight Envelope 
	private Map<Double, FlightManeuveringEnvelopeCalc> _theEnvelopeCalculatorMap;
	private Map<Double, Amount<Velocity>> _stallSpeedFullFlapMap;
	private Map<Double, Amount<Velocity>> _stallSpeedCleanMap;
	private Map<Double, Amount<Velocity>> _stallSpeedInvertedMap;
	private Map<Double, Amount<Velocity>> _maneuveringSpeedMap;
	private Map<Double, Amount<Velocity>> _maneuveringFlapSpeedMap;
	private Map<Double, Amount<Velocity>> _maneuveringSpeedInvertedMap;
	private Map<Double, Amount<Velocity>> _designFlapSpeedMap;
	private Map<Double, Double> _positiveLoadFactorManeuveringSpeedMap;
	private Map<Double, Double> _positiveLoadFactorCruisingSpeedMap;
	private Map<Double, Double> _positiveLoadFactorDiveSpeedMap;
	private Map<Double, Double> _positiveLoadFactorDesignFlapSpeedMap;
	private Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedMap;
	private Map<Double, Double> _negativeLoadFactorCruisingSpeedMap;
	private Map<Double, Double> _negativeLoadFactorDiveSpeedMap;
	private Map<Double, Double> _positiveLoadFactorManeuveringSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorCruisingSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorDiveSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorDesignFlapSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorCruisingSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorDiveSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorDesignFlapSpeedWithGustMap;
	//..............................................................................
	// Mission profile
	private Map<Double, MissionProfileCalc> _theMissionProfileCalculatorMap;
	private Map<Double, List<Amount<Length>>> _altitudeListMap;
	private Map<Double, List<Amount<Length>>> _rangeListMap;
	private Map<Double, List<Amount<Duration>>> _timeListMap;
	private Map<Double, List<Amount<Mass>>> _fuelUsedListMap;
	private Map<Double, List<Amount<Mass>>> _massListMap;
	private Map<Double, List<Amount<Velocity>>> _speedTASMissionListMap;
	private Map<Double, List<Double>> _machMissionListMap;
	private Map<Double, List<Double>> _liftingCoefficientMissionListMap;
	private Map<Double, List<Double>> _dragCoefficientMissionListMap;
	private Map<Double, List<Double>> _efficiencyMissionListMap;
	private Map<Double, List<Amount<Force>>> _thrustMissionListMap;
	private Map<Double, List<Amount<Force>>> _dragMissionListMap;
	
	private Map<Double, Amount<Mass>> _initialFuelMassMap;
	private Map<Double, Amount<Mass>> _totalFuelUsedMap;
	private Map<Double, Amount<Duration>> _totalMissionTimeMap;
	private Map<Double, Amount<Mass>> _initialMissionMassMap;
	private Map<Double, Amount<Mass>> _endMissionMassMap;
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		//..............................................................................
		// Take-Off
		_theTakeOffCalculatorMap = new HashMap<>();
		_takeOffDistanceAEOMap = new HashMap<>();
		_balancedFieldLengthMap = new HashMap<>();
		_takeOffDistanceFAR25Map = new HashMap<>();
		_groundRollDistanceTakeOffMap = new HashMap<>();
		_rotationDistanceTakeOffMap = new HashMap<>();
		_airborneDistanceTakeOffMap = new HashMap<>();
		_vStallTakeOffMap = new HashMap<>();
		_vMCMap = new HashMap<>();
		_vRotationMap = new HashMap<>();
		_vLiftOffMap = new HashMap<>();
		_v1Map = new HashMap<>();
		_v2Map = new HashMap<>();
		_takeOffDurationMap = new HashMap<>();
		_thrustMomentOEIMap = new HashMap<>();
		_yawingMomentOEIMap = new HashMap<>();
		//..............................................................................
		// Climb
		_theClimbCalculatorMap = new HashMap<>();
		_rcAEOMap = new HashMap<>();
		_rcOEIMap = new HashMap<>();
		_ceilingAEOMap = new HashMap<>();
		_ceilingOEIMap = new HashMap<>();
		_dragListAEOMap = new HashMap<>();
		_thrustListAEOMap = new HashMap<>();
		_dragListOEIMap = new HashMap<>();
		_thrustListOEIMap = new HashMap<>();
		_efficiencyAltitudeAEOMap = new HashMap<>();
		_efficiencyAltitudeOEIMap = new HashMap<>();
		
		_absoluteCeilingAEOMap = new HashMap<>();
		_serviceCeilingAEOMap = new HashMap<>();
		_minimumClimbTimeAEOMap = new HashMap<>();
		_climbTimeAtSpecificClimbSpeedAEOMap = new HashMap<>();
		_fuelUsedDuringClimbMap = new HashMap<>();
		
		_absoluteCeilingOEIMap = new HashMap<>();
		_serviceCeilingOEIMap = new HashMap<>();
		//..............................................................................
		// Cruise
		_dragListAltitudeParameterizationMap = new HashMap<>();
		_thrustListAltitudeParameterizationMap = new HashMap<>();
		_dragListWeightParameterizationMap = new HashMap<>();
		_thrustListWeightParameterizationMap = new HashMap<>();
		_weightListCruiseMap = new HashMap<>();
		
		_intersectionListMap = new HashMap<>();
		_cruiseEnvelopeListMap = new HashMap<>();
		
		_efficiencyAltitudeMap = new HashMap<>();
		_efficiencyWeightMap = new HashMap<>();
		
		_specificRangeMap = new HashMap<>();
		
		_maxSpeesTASAtCruiseAltitudeMap = new HashMap<>();
		_minSpeesTASAtCruiseAltitudeMap = new HashMap<>();
		_maxSpeesCASAtCruiseAltitudeMap = new HashMap<>();
		_minSpeesCASAtCruiseAltitudeMap = new HashMap<>();
		_maxMachAtCruiseAltitudeMap = new HashMap<>();
		_minMachAtCruiseAltitudeMap = new HashMap<>();
		_efficiencyAtCruiseAltitudeAndMachMap = new HashMap<>();
		_thrustAtCruiseAltitudeAndMachMap = new HashMap<>();
		_dragAtCruiseAltitudeAndMachMap = new HashMap<>();
		_powerAvailableAtCruiseAltitudeAndMachMap = new HashMap<>();
		_powerNeededAtCruiseAltitudeAndMachMap = new HashMap<>();
		//.............................................................................
		// Descent
		_theDescentCalculatorMap = new HashMap<>();
		_descentLengthsMap = new HashMap<>();
		_descentTimesMap = new HashMap<>();
		_descentAnglesMap = new HashMap<>();
		_totalDescentLengthMap = new HashMap<>();
		_totalDescentTimeMap = new HashMap<>();
		_totalDescentFuelUsedMap = new HashMap<>();
		//..............................................................................
		// Landing
		_theLandingCalculatorMap = new HashMap<>();
		_landingDistanceMap = new HashMap<>();
		_landingDistanceFAR25Map = new HashMap<>();
		_groundRollDistanceLandingMap = new HashMap<>();
		_flareDistanceLandingMap = new HashMap<>();
		_airborneDistanceLandingMap = new HashMap<>();
		_vStallLandingMap = new HashMap<>();
		_vApproachMap = new HashMap<>();
		_vFlareMap = new HashMap<>();
		_vTouchDownMap = new HashMap<>();
		_landingDurationMap = new HashMap<>();
		//..............................................................................
		// Payload-Range
		_thePayloadRangeCalculatorMap = new HashMap<>();
		
		_rangeAtMaxPayloadMap = new HashMap<>();
		_rangeAtDesignPayloadMap = new HashMap<>();
		_rangeAtMaxFuelMap = new HashMap<>();	
		_rangeAtZeroPayloadMap = new HashMap<>();
		_takeOffMassAtZeroPayloadMap = new HashMap<>();
		_maxPayloadMap = new HashMap<>();
		_designPayloadMap = new HashMap<>();
		_payloadAtMaxFuelMap = new HashMap<>();
		_passengersNumberAtMaxPayloadMap = new HashMap<>();
		_passengersNumberAtDesignPayloadMap = new HashMap<>();
		_passengersNumberAtMaxFuelMap = new HashMap<>();
		_requiredMassAtMaxPayloadMap = new HashMap<>();
		_requiredMassAtDesignPayloadMap = new HashMap<>();
		
		_rangeArrayMap = new HashMap<>();
		_payloadArrayMap = new HashMap<>();
		
		_rangeMatrixMap = new HashMap<>();
		_payloadMatrixMap = new HashMap<>();
		//..............................................................................
		// Maneuvering and Gust Flight Envelope 
		_theEnvelopeCalculatorMap = new HashMap<>();
		_stallSpeedFullFlapMap = new HashMap<>();
		_stallSpeedCleanMap = new HashMap<>();
		_stallSpeedInvertedMap = new HashMap<>();
		_maneuveringSpeedMap = new HashMap<>();
		_maneuveringFlapSpeedMap = new HashMap<>();
		_maneuveringSpeedInvertedMap = new HashMap<>();
		_designFlapSpeedMap = new HashMap<>();
		_positiveLoadFactorManeuveringSpeedMap = new HashMap<>();
		_positiveLoadFactorCruisingSpeedMap = new HashMap<>();
		_positiveLoadFactorDiveSpeedMap = new HashMap<>();
		_positiveLoadFactorDesignFlapSpeedMap = new HashMap<>();
		_negativeLoadFactorManeuveringSpeedInvertedMap = new HashMap<>();
		_negativeLoadFactorCruisingSpeedMap = new HashMap<>();
		_negativeLoadFactorDiveSpeedMap = new HashMap<>();
		_positiveLoadFactorManeuveringSpeedWithGustMap = new HashMap<>();
		_positiveLoadFactorCruisingSpeedWithGustMap = new HashMap<>();
		_positiveLoadFactorDiveSpeedWithGustMap = new HashMap<>();
		_positiveLoadFactorDesignFlapSpeedWithGustMap = new HashMap<>();
		_negativeLoadFactorManeuveringSpeedInvertedWithGustMap = new HashMap<>();
		_negativeLoadFactorCruisingSpeedWithGustMap = new HashMap<>();
		_negativeLoadFactorDiveSpeedWithGustMap = new HashMap<>();
		_negativeLoadFactorDesignFlapSpeedWithGustMap = new HashMap<>();
		//..............................................................................
		// Mission profile
		_theMissionProfileCalculatorMap = new HashMap<>();
		_altitudeListMap = new HashMap<>();
		_rangeListMap = new HashMap<>();
		_timeListMap = new HashMap<>();
		_fuelUsedListMap = new HashMap<>();
		_massListMap = new HashMap<>();
		_speedTASMissionListMap = new HashMap<>();
		_machMissionListMap = new HashMap<>();
		_liftingCoefficientMissionListMap = new HashMap<>();
		_dragCoefficientMissionListMap = new HashMap<>();
		_efficiencyMissionListMap = new HashMap<>();
		_thrustMissionListMap = new HashMap<>();
		_dragMissionListMap = new HashMap<>();
	
		_initialFuelMassMap = new HashMap<>();
		_totalFuelUsedMap = new HashMap<>();
		_totalMissionTimeMap = new HashMap<>();
		_initialMissionMassMap = new HashMap<>();
		_endMissionMassMap = new HashMap<>();
		
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ACPerformanceManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions
			) throws IOException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading performance analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		//---------------------------------------------------------------------------------------
		// WEIGHTS FROM FILE INSTRUCTION
		Boolean readWeightsFromPreviousAnalysisFlag;
		String readWeightsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_previous_file");
		if(readWeightsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readWeightsFromPreviousAnalysisFlag = Boolean.TRUE;
		else
			readWeightsFromPreviousAnalysisFlag = Boolean.FALSE;
		
		//---------------------------------------------------------------------------------------
		// AERODYNAMICS FROM FILE INSTRUCTION
		Boolean readAerodynamicsFromPreviousAnalysisFlag;
		String readAerodynamicsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@aerodynamics_from_previous_file");
		if(readAerodynamicsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.TRUE;
		else
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.FALSE;

		//===========================================================================================
		// READING WEIGHTS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACWeightsManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> maximumFuelMass = null;
		Amount<Mass> singlePassengerMass = null;
		
		if(readWeightsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheWeights() != null) {

					//...............................................................
					// MAXIMUM TAKE-OFF MASS
					maximumTakeOffMass = theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(SI.KILOGRAM);

					//...............................................................
					// OPERATING EMPTY MASS
					operatingEmptyMass = theAircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().to(SI.KILOGRAM);

					//...............................................................
					// MAXIMUM FUEL MASS
					if(theAircraft.getFuelTank() != null) 
						maximumFuelMass = theAircraft.getFuelTank().getFuelMass().to(SI.KILOGRAM);
					else {
						System.err.println("WARNING!! THE FUEL TANK DOES NOT EXIST ... TERMINATING");
						System.exit(1);
					}
					
					//...............................................................
					// SINGLE PASSENGER MASS
					singlePassengerMass = theAircraft.getTheAnalysisManager().getTheWeights().getPaxSingleMass().to(SI.KILOGRAM);
						
				}
				else {
					System.err.println("WARNING!! THE WEIGHTS ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {
			//...............................................................
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_take_off_mass");

			//...............................................................
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//performance/weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/operating_empty_mass");

			//...............................................................
			// MAXIMUM FUEL MASS
			String maximumFuelMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_fuel_mass");
			if(maximumFuelMassProperty != null)
				maximumFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_fuel_mass");

			//...............................................................
			// SINGLE PASSENGER MASS
			String singlePassengerMassProperty = reader.getXMLPropertyByPath("//performance/weights/single_passenger_mass");
			if(singlePassengerMassProperty != null)
				singlePassengerMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/single_passenger_mass");
		}

		//===========================================================================================
		// READING AERODYNAMICS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACAerodynamicAndStabilityManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		Map<Double, MyInterpolatingFunction> tauRudderMap = new HashMap<>();

		List<Double> centerOfGravityList = new ArrayList<>();

		Map<Double,Double> cLmaxClean  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaClean  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaTakeOff  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaLanding  = new HashMap<>();
		Map<Double,Double> cLmaxTakeOff  = new HashMap<>();
		Map<Double,Double> cLZeroTakeOff  = new HashMap<>();
		Map<Double,Double> cLmaxLanding  = new HashMap<>();
		Map<Double,Double> cLZeroLanding  = new HashMap<>();

		Map<Double, Double> cD0 = new HashMap<>();
		Map<Double,Double> oswaldCruise = new HashMap<>();
		Map<Double,Double> oswaldClimb = new HashMap<>();
		Map<Double,Double> oswaldTakeOff = new HashMap<>();
		Map<Double,Double> oswaldLanding = new HashMap<>();
		Map<Double,Double> deltaCD0TakeOff  = new HashMap<>();
		Map<Double,Double> deltaCD0Landing  = new HashMap<>();
		Map<Double,Double> deltaCD0LandingGears  = new HashMap<>();
		Map<Double,Double> deltaCD0Spoilers  = new HashMap<>();

		Map<Double,Double[]> polarCLCruise  = new HashMap<>();
		Map<Double,Double[]> polarCDCruise  = new HashMap<>();
		Map<Double,Double[]> polarCLClimb  = new HashMap<>();
		Map<Double,Double[]> polarCDClimb  = new HashMap<>();
		Map<Double,Double[]> polarCLTakeOff  = new HashMap<>();
		Map<Double,Double[]> polarCDTakeOff  = new HashMap<>();
		Map<Double,Double[]> polarCLLanding  = new HashMap<>();
		Map<Double,Double[]> polarCDLanding  = new HashMap<>();

		if(readAerodynamicsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability() != null) {

					List<MyInterpolatingFunction> tauRudderFunctionList = new ArrayList<>();
					MyInterpolatingFunction tauRudderFunctionTakeOff = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionClimb = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionCruise = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionLanding = new MyInterpolatingFunction();
					List<Double> xcgTakeOff = new ArrayList<>();
					List<Double> xcgClimb = new ArrayList<>();
					List<Double> xcgCruise = new ArrayList<>();
					List<Double> xcgLanding = new ArrayList<>();

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF) != null) { 
						xcgTakeOff = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionTakeOff = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE TAKE-OFF AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB) != null) {
						xcgClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE CLIMB AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE) != null) {
						xcgCruise = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionCruise = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE CRUISE AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING) != null) {
						xcgLanding = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE LANDING AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					tauRudderFunctionList.add(tauRudderFunctionTakeOff);
					tauRudderFunctionList.add(tauRudderFunctionClimb);
					tauRudderFunctionList.add(tauRudderFunctionCruise);
					tauRudderFunctionList.add(tauRudderFunctionLanding);

					//---------------------------------------------------------------------------------------------------------
					// TEST: SAME Xcg FOR EACH CONDITION... 
					//---------------------------------------------------------------------------------------------------------
					try {
						assertEquals("WARNING! THE TAKE-OFF AND CLIMB XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgClimb);
						assertEquals("WARNING! THE TAKE-OFF AND CRUISE XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgCruise);
						assertEquals("WARNING! THE TAKE-OFF AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgLanding);
						assertEquals("WARNING! THE CLIMB AND CRUISE XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgClimb, xcgCruise);
						assertEquals("WARNING! THE CLIMB AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgClimb, xcgLanding);
						assertEquals("WARNING! THE CRUISE AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgCruise, xcgLanding);
					}
					catch (AssertionFailedError e) {
						System.err.println("TERMINATING ...");
						System.exit(1);
					}

					centerOfGravityList.addAll(
							theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
							.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getXCGAircraft()
							);

					centerOfGravityList.stream().forEach(xcg -> tauRudderMap.put(xcg, tauRudderFunctionList.get(centerOfGravityList.indexOf(xcg))));

					//---------------------------------------------------------------------------------------------------------
					// TAKE-OFF 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionTakeOffMap = new HashMap<>();

					List<Integer> indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						liftCurveFunctionTakeOffMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						//.......................................
						// CUTTTING ALL CURVES UNTIL MAXIMUM CL
						//.......................................
						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.TAKE_OFF).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						//-------------------------------------------------------------				
						liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaTakeOff.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxTakeOff.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						cLZeroTakeOff.put(
								centerOfGravityList.get(i), 
								liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(0.0)
								);

						polarCLTakeOff.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						polarCDTakeOff.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);
					}

					//---------------------------------------------------------------------------------------------------------
					// CLIMB 
					//---------------------------------------------------------------------------------------------------------
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {


						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CLIMB).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						polarCLClimb.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						polarCDClimb.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);
					}

					//---------------------------------------------------------------------------------------------------------
					// CRUISE 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionCruiseMap = new HashMap<>();
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {


						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CRUISE).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						liftCurveFunctionCruiseMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaClean.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxClean.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						polarCLCruise.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						polarCDCruise.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);
					}
					//---------------------------------------------------------------------------------------------------------
					// LANDING
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionLandingMap = new HashMap<>();
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.LANDING).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						liftCurveFunctionLandingMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaLanding.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxLanding.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						cLZeroLanding.put(
								centerOfGravityList.get(i), 
								liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(0.0)
								);

						polarCLLanding.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						polarCDLanding.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);
					}
				}
				else {
					System.err.println("WARNING!! THE AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {

			Boolean parabolicDragPolarFlag = Boolean.FALSE;
			String paraboliDragPolarProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//@parabolicDragPolar");
			if(paraboliDragPolarProperty.equalsIgnoreCase("TRUE"))
				parabolicDragPolarFlag = Boolean.TRUE;
			else if(paraboliDragPolarProperty.equalsIgnoreCase("FALSE"))
				parabolicDragPolarFlag = Boolean.FALSE;
			else {
				System.err.println("ERROR : parabolicDragPolar HAS TO BE TRUE/FALSE !");
				System.exit(1);
			}

			//...............................................................
			// Xcg LIST
			List<String> xCGListProperty = MyXMLReaderUtils.getXMLPropertiesByPath(reader.getXmlDoc(), reader.getXpath(),"//xcg/@value");
			if(!xCGListProperty.isEmpty())
				xCGListProperty.stream().forEach(xcg -> centerOfGravityList.add(Double.valueOf(xcg)));

			//---------------------------------------------------------------
			// TAU RUDDER
			List<String> calculateTauRudderListProperty = MyXMLReaderUtils
					.getXMLPropertiesByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//tau_rudder_function/@calculate");

			if(!calculateTauRudderListProperty.isEmpty()) {

				List<MyInterpolatingFunction> tauRudderFunctionList = new ArrayList<>();

				for(int i=0; i<calculateTauRudderListProperty.size(); i++) {

					MyInterpolatingFunction tauRudderInterpolatingFunction = new MyInterpolatingFunction();

					if(calculateTauRudderListProperty.get(i).equalsIgnoreCase("TRUE")) {

						List<Double> tauRudderArray = new ArrayList<>();

						double[] deltaRudderArray = MyArrayUtils.linspace(-25, 25, 51);
						for(int j=0; j<deltaRudderArray.length; j++)
							tauRudderArray.add(
									LiftCalc.calculateTauIndexElevator(
											theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(), 
											theAircraft.getVTail().getLiftingSurfaceCreator().getAspectRatio(), 
											theAircraft.getVTail().getHighLiftDatabaseReader(), 
											theAircraft.getVTail().getAerodynamicDatabaseReader(), 
											Amount.valueOf(deltaRudderArray[j], NonSI.DEGREE_ANGLE)
											)
									);

						tauRudderInterpolatingFunction.interpolateLinear(
								deltaRudderArray, MyArrayUtils.convertToDoublePrimitive(tauRudderArray)
								);

						tauRudderFunctionList.add(tauRudderInterpolatingFunction);

					}
					else {

						List<Double> tauRudderFunction = new ArrayList<>();
						List<Amount<Angle>> tauRudderFunctionDeltaRudder = new ArrayList<>();

						String tauRudderFunctionProperty = reader.getXMLPropertyByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/tau");
						if(tauRudderFunctionProperty != null)
							tauRudderFunction = reader.readArrayDoubleFromXML("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/tau"); 
						String tauRudderFunctionDeltaRudderProperty = reader.getXMLPropertyByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/delta_rudder");
						if(tauRudderFunctionDeltaRudderProperty != null)
							tauRudderFunctionDeltaRudder = reader.readArrayofAmountFromXML("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/delta_rudder");

						if(tauRudderFunction.size() > 1)
							if(tauRudderFunction.size() != tauRudderFunctionDeltaRudder.size()) {
								System.err.println("TAU RUDDER ARRAY AND THE RELATED DELTA RUDDER ARRAY MUST HAVE THE SAME LENGTH !");
								System.exit(1);
							}
						if(tauRudderFunction.size() == 1) {
							tauRudderFunction.add(tauRudderFunction.get(0));
							tauRudderFunctionDeltaRudder.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
							tauRudderFunctionDeltaRudder.add(Amount.valueOf(360.0, NonSI.DEGREE_ANGLE));
						}

						tauRudderInterpolatingFunction.interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										tauRudderFunctionDeltaRudder.stream()
										.map(f -> f.to(NonSI.DEGREE_ANGLE))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertToDoublePrimitive(tauRudderFunction)
								);

						tauRudderFunctionList.add(tauRudderInterpolatingFunction);
					};

					tauRudderMap.put(centerOfGravityList.get(i), tauRudderFunctionList.get(i));
				}
			}

			//...............................................................
			// CLmax CLEAN
			List<String> cLmaxCleanProperty = reader.getXMLPropertiesByPath("//cLmax_clean_configuration");
			if(!cLmaxCleanProperty.isEmpty())
				for(int i=0; i<cLmaxCleanProperty.size(); i++)
					cLmaxClean.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxCleanProperty.get(i))
							);
			//...............................................................
			// CLalpha CLEAN
			List<String> cLAlphaCleanProperty = reader.getXMLPropertiesByPath("//cL_alpha_clean_configuration");
			List<Amount<?>> cLAlphaCleanList = new ArrayList<>();
			if(!cLAlphaCleanProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaCleanList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_clean_configuration")
							);
			}
			for(int i=0; i<cLAlphaCleanList.size(); i++)
				cLAlphaClean.put(
						centerOfGravityList.get(i), 
						cLAlphaCleanList.get(i)
						);
				
			//...............................................................
			// CLalpha TAKE-OFF
			List<String> cLAlphaTakeOffProperty = reader.getXMLPropertiesByPath("//cL_alpha_take_off");
			List<Amount<?>> cLAlphaTakeOffList = new ArrayList<>();
			if(!cLAlphaTakeOffProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaTakeOffList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_take_off")
							);
			}
			for(int i=0; i<cLAlphaTakeOffList.size(); i++)
				cLAlphaTakeOff.put(
						centerOfGravityList.get(i), 
						cLAlphaTakeOffList.get(i)
						);
			//...............................................................
			// CLalpha LANDING
			List<String> cLAlphaLandingProperty = reader.getXMLPropertiesByPath("//cL_alpha_landing");
			List<Amount<?>> cLAlphaLandingList = new ArrayList<>();
			if(!cLAlphaLandingProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaLandingList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_landing")
							);
			}
			for(int i=0; i<cLAlphaLandingList.size(); i++)
				cLAlphaLanding.put(
						centerOfGravityList.get(i), 
						cLAlphaLandingList.get(i)
						);
			//...............................................................
			// CLmax TAKE-OFF
			List<String> cLmaxTakeOffProperty = reader.getXMLPropertiesByPath("//cLmax_take_off_configuration");
			if(!cLmaxTakeOffProperty.isEmpty())
				for(int i=0; i<cLmaxTakeOffProperty.size(); i++)
					cLmaxTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxTakeOffProperty.get(i))
							);
			//...............................................................
			// CL0 TAKE-OFF
			List<String> cL0TakeOffProperty = reader.getXMLPropertiesByPath("//cL0_take_off_configuration");
			if(!cL0TakeOffProperty.isEmpty())
				for(int i=0; i<cL0TakeOffProperty.size(); i++)
					cLZeroTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0TakeOffProperty.get(i))
							);
			//...............................................................
			// CLmax LANDING
			List<String> cLmaxLandingProperty = reader.getXMLPropertiesByPath("//cLmax_landing_configuration");
			if(!cLmaxLandingProperty.isEmpty())
				for(int i=0; i<cLmaxLandingProperty.size(); i++)
					cLmaxLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxLandingProperty.get(i))
							);
			//...............................................................
			// CL0 LANDING
			List<String> cL0LandingProperty = reader.getXMLPropertiesByPath("//cL0_landing_configuration");
			if(!cL0LandingProperty.isEmpty())
				for(int i=0; i<cL0LandingProperty.size(); i++)
					cLZeroLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0LandingProperty.get(i))
							);
			
			if(parabolicDragPolarFlag == Boolean.FALSE) {
				
				//...............................................................
				// POLAR CURVE CRUISE
				Map<Double, List<String>> polarCLCruiseProperty = new HashMap<>();
				Map<Double, List<String>> polarCDCruiseProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLCruiseProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CL")
								);
							polarCDCruiseProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLCruiseProperty.get(xcg).isEmpty())
								polarCLCruise.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CL")
												)
										);
							if(!polarCDCruiseProperty.get(xcg).isEmpty())
								polarCDCruise.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CD")
												)
										);
						});

				//...............................................................
				// POLAR CURVE CLIMB
				Map<Double, List<String>> polarCLClimbProperty = new HashMap<>();
				Map<Double, List<String>> polarCDClimbProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLClimbProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CL")
								);
							polarCDClimbProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLClimbProperty.get(xcg).isEmpty())
								polarCLClimb.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CL")
												)
										);
							if(!polarCDClimbProperty.get(xcg).isEmpty())
								polarCDClimb.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CD")
												)
										);
						});
				
				//...............................................................
				// POLAR CURVE TAKE-OFF
				Map<Double, List<String>> polarCLTakeOffProperty = new HashMap<>();
				Map<Double, List<String>> polarCDTakeOffProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLTakeOffProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CL")
								);
							polarCDTakeOffProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLTakeOffProperty.get(xcg).isEmpty())
								polarCLTakeOff.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CL")
												)
										);
							if(!polarCDTakeOffProperty.get(xcg).isEmpty())
								polarCDTakeOff.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CD")
												)
										);
						});
				
				//...............................................................
				// POLAR CURVE LANDING
				Map<Double, List<String>> polarCLLandingProperty = new HashMap<>();
				Map<Double, List<String>> polarCDLandingProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLLandingProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CL")
								);
							polarCDLandingProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLLandingProperty.get(xcg).isEmpty())
								polarCLLanding.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CL")
												)
										);
							if(!polarCDLandingProperty.get(xcg).isEmpty())
								polarCDLanding.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CD")
												)
										);
						});
				
			}
			else {
				
				//...............................................................
				// CD0
				List<String> cD0Property = reader.getXMLPropertiesByPath("//cD0");
				if(!cD0Property.isEmpty())
					for(int i=0; i<cD0Property.size(); i++)
						cD0.put(
							centerOfGravityList.get(i),
							Double.valueOf(cD0Property.get(i))
							);
				//...............................................................
				// OSWALD CRUISE
				List<String> oswladCruiseProperty = reader.getXMLPropertiesByPath("//oswald_cruise");
				if(!oswladCruiseProperty.isEmpty())
					for(int i=0; i<oswladCruiseProperty.size(); i++)
						oswaldCruise.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladCruiseProperty.get(i))
							);
				//...............................................................
				// OSWALD CLIMB
				List<String> oswladClimbProperty = reader.getXMLPropertiesByPath("//oswald_climb");
				if(!oswladClimbProperty.isEmpty())
					for(int i=0; i<oswladClimbProperty.size(); i++)
						oswaldClimb.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladClimbProperty.get(i))
							);
				//...............................................................
				// OSWALD TO
				List<String> oswladTOProperty = reader.getXMLPropertiesByPath("//oswald_take_off");
				if(!oswladTOProperty.isEmpty())
					for(int i=0; i<oswladTOProperty.size(); i++)
						oswaldTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladTOProperty.get(i))
							);
				//...............................................................
				// OSWALD LND
				List<String> oswladLNDProperty = reader.getXMLPropertiesByPath("//oswald_landing");
				if(!oswladLNDProperty.isEmpty())
					for(int i=0; i<oswladLNDProperty.size(); i++)
						oswaldLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladLNDProperty.get(i))
							);
				//...............................................................
				// DeltaCD0 TAKE-OFF
				List<String> deltaCD0TakeOffProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_take_off");
				if(!deltaCD0TakeOffProperty.isEmpty())
					for(int i=0; i<deltaCD0TakeOffProperty.size(); i++)
						deltaCD0TakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(deltaCD0TakeOffProperty.get(i))
							);
				//...............................................................
				// DeltaCD0 LANDING
				List<String> deltaCD0LandingProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_landing");
				if(!deltaCD0LandingProperty.isEmpty())
					for(int i=0; i<deltaCD0LandingProperty.size(); i++)
						deltaCD0Landing.put(
							centerOfGravityList.get(i),
							Double.valueOf(deltaCD0LandingProperty.get(i))
							);
				//...............................................................
				// DeltaCD0 LANDING GEARS
				List<String> deltaCD0LandingGearsProperty = reader.getXMLPropertiesByPath("//delta_CD0_landing_gears");
				if(!deltaCD0LandingGearsProperty.isEmpty())
					for(int i=0; i<deltaCD0LandingGearsProperty.size(); i++)
						deltaCD0LandingGears.put(
							centerOfGravityList.get(i),
							Double.valueOf(deltaCD0LandingGearsProperty.get(i))
							);
				//...............................................................
				// DeltaCD0 SPOILERS
				List<String> deltaCD0SpoilersProperty = reader.getXMLPropertiesByPath("//delta_CD0_spoilers");
				if(!deltaCD0SpoilersProperty.isEmpty())
					for(int i=0; i<deltaCD0SpoilersProperty.size(); i++)
						deltaCD0Spoilers.put(
							centerOfGravityList.get(i),
							Double.valueOf(deltaCD0SpoilersProperty.get(i))
							);
					
				int numberOfPolarPoints = 50;

				centerOfGravityList.stream().forEach(xcg -> {

					//...............................................................
					// POLAR CURVE CRUISE
					polarCLCruise.put(
							xcg, 
							MyArrayUtils.linspaceDouble(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDCruise.put(
							xcg,
							new Double[polarCLCruise.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE CLIMB
					polarCLClimb.put(
							xcg, 
							MyArrayUtils.linspaceDouble(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDClimb.put(
							xcg,
							new Double[polarCLClimb.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE TAKE-OFF
					polarCLTakeOff.put(
							xcg, 
							MyArrayUtils.linspaceDouble(-0.2, cLmaxTakeOff.get(xcg), numberOfPolarPoints)
							);				
					polarCDTakeOff.put(
							xcg,
							new Double[polarCLTakeOff.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE LANDING
					polarCLLanding.put(
							xcg, 
							MyArrayUtils.linspaceDouble(-0.2, cLmaxLanding.get(xcg), numberOfPolarPoints)
							);				
					polarCDLanding.put(
							xcg,
							new Double[polarCLLanding.get(xcg).length]
							);

					//...............................................................
					// building the CD arrays...
					for(int i=0; i<numberOfPolarPoints; i++) {
						polarCDClimb.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLClimb.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldClimb.get(xcg), 
								theOperatingConditions.getMachClimb(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachClimb(),
										theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getType()
										)
								);
						polarCDCruise.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLCruise.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldCruise.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getType()
										)
								);
						polarCDTakeOff.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0TakeOff.get(xcg) + deltaCD0LandingGears.get(xcg),
								polarCLTakeOff.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldTakeOff.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLTakeOff.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getType()
										)
								);
						polarCDLanding.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0Landing.get(xcg) + deltaCD0LandingGears.get(xcg) + deltaCD0Spoilers.get(xcg),
								polarCLLanding.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldLanding.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLLanding.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getType()
										)
								);
					}
				});
			}
		}
		
		//===========================================================================================
		// READING TAKE-OFF AND LANDING DATA ...	

		// default values
		Amount<Velocity> windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		List<Double> muFunction = new ArrayList<>();
		List<Amount<Velocity>> muFunctionSpeed = new ArrayList<>();
		List<Double> muBrakeFunction = new ArrayList<>();
		List<Amount<Velocity>> muBrakeFunctionSpeed = new ArrayList<>();
		
		Amount<Duration> dtRotation = Amount.valueOf(3.0, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Length> obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		Double kRotation = 1.05;
		Double alphaDotRotation = 3.0;
		Double kCLmax = 0.9;
		Double dragDueToEngineFailure = 0.0;
		Double kAlphaDot = 0.04;
		
		Double kLandingWeight = 0.97;
		Amount<Length> obstacleLanding = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		Amount<Angle> thetaApproach = Amount.valueOf(3.0, NonSI.DEGREE_ANGLE);
		Double kApproach = 1.3;
		Double kFlare = 1.23;
		Double kTouchDown = 1.15;
		Amount<Duration> freeRollDuration = Amount.valueOf(2.0, SI.SECOND);
		
		//...............................................................
		// dt ROTATION
		String dtRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_rotation");
		if(dtRotationProperty != null)
			dtRotation = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/dt_rotation");				
		
		//...............................................................
		// dt HOLD
		String dtHoldProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold");
		if(dtHoldProperty != null)
			dtHold = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/dt_hold");				
		
		//...............................................................
		// ALPHA GROUND
		String alphaGroundProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground");
		if(alphaGroundProperty != null)
			alphaGround = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/alpha_ground");
		
		//...............................................................
		// WIND SPEED
		String windSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway");
		if(windSpeedProperty != null)
			windSpeed = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/wind_speed_along_runway");
		
		//...............................................................
		// OBSTACLE TAKE-OFF
		String obstacleTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off");
		if(obstacleTakeOffProperty != null)
			obstacleTakeOff = reader.getXMLAmountLengthByPath("//performance/takeoff_landing/obstacle_take_off");
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT FUNCTION
		String wheelsFrictionCoefficientFunctionProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_function/friction_coefficient");
		if(wheelsFrictionCoefficientFunctionProperty != null)
			muFunction = reader.readArrayDoubleFromXML("//performance/takeoff_landing/wheels_friction_coefficient_function/friction_coefficient"); 
		String wheelsFrictionCoefficientFunctionSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_function/speed");
		if(wheelsFrictionCoefficientFunctionSpeedProperty != null)
			muFunctionSpeed = reader.readArrayofAmountFromXML("//performance/takeoff_landing/wheels_friction_coefficient_function/speed");
		
		if(muFunction.size() > 1)
			if(muFunction.size() != muFunctionSpeed.size())
			{
				System.err.println("FRICTION COEFFICIENT ARRAY AND THE RELATED SPEED ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(muFunction.size() == 1) {
			muFunction.add(muFunction.get(0));
			muFunctionSpeed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			muFunctionSpeed.add(Amount.valueOf(10000.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction muInterpolatingFunction = new MyInterpolatingFunction();
		muInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						muFunctionSpeed.stream()
						.map(f -> f.to(SI.METERS_PER_SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(muFunction)
				);
			
		//...............................................................
		// WHEELS FRICTION COEFFICIENT (WITH BRAKES) FUNCTION
		String wheelsFrictionCoefficientBrakesFunctionProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/friction_coefficient_with_brakes");
		if(wheelsFrictionCoefficientBrakesFunctionProperty != null)
			muBrakeFunction = reader.readArrayDoubleFromXML("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/friction_coefficient_with_brakes"); 
		String wheelsFrictionCoefficientBrakesFunctionSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/speed");
		if(wheelsFrictionCoefficientBrakesFunctionSpeedProperty != null)
			muBrakeFunctionSpeed = reader.readArrayofAmountFromXML("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/speed");
		
		if(muBrakeFunction.size() > 1)
			if(muBrakeFunction.size() != muBrakeFunctionSpeed.size())
			{
				System.err.println("FRICTION COEFFICIENT (WITH BRAKES) ARRAY AND THE RELATED SPEED ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(muBrakeFunction.size() == 1) {
			muBrakeFunction.add(muBrakeFunction.get(0));
			muBrakeFunctionSpeed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			muBrakeFunctionSpeed.add(Amount.valueOf(10000.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction muBrakeInterpolatingFunction = new MyInterpolatingFunction();
		muBrakeInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						muBrakeFunctionSpeed.stream()
						.map(f -> f.to(SI.METERS_PER_SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(muBrakeFunction)
				);
		
		//...............................................................
		// K ROTATION
		String kRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation");
		if(kRotationProperty != null)
			kRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation"));
		
		//...............................................................
		// ALPHA DOT ROTATION
		String alphaDotRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_dot_rotation");
		if(alphaDotRotationProperty != null)
			alphaDotRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_dot_rotation"));
		
		//...............................................................
		// K CLmax
		String kCLmaxProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax");
		if(kCLmaxProperty != null)
			kCLmax = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax"));
		
		//...............................................................
		// DRAG DUE TO ENGINE FAILURE
		String dragDueToEngineFailureProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/drag_due_to_engine_failure");
		if(dragDueToEngineFailureProperty != null)
			dragDueToEngineFailure = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/drag_due_to_engine_failure"));

		//...............................................................
		// K ALPHA DOT
		String kAlphaDotProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot");
		if(kAlphaDotProperty != null)
			kAlphaDot = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot"));

		//...............................................................
		// K LANDING WEIGHT
		String kLandingWeightProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight");
		if(kLandingWeightProperty != null)
			kLandingWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight"));
		
		//...............................................................
		// OBSTACLE LANDING
		String obstacleLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing");
		if(obstacleLandingProperty != null)
			obstacleLanding = reader.getXMLAmountLengthByPath("//performance/takeoff_landing/obstacle_landing");		
		
		//...............................................................
		// THETA APPROACH
		String thetaApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/theta_approach");
		if(thetaApproachProperty != null)
			thetaApproach = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/theta_approach");		
		
		//...............................................................
		// K APPROACH
		String kApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach");
		if(kApproachProperty != null)
			kApproach = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach"));
		
		//...............................................................
		// K FLARE
		String kFlareProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare");
		if(kFlareProperty != null)
			kFlare = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare"));
		
		//...............................................................
		// K TOUCH DOWN
		String kTouchDownProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down");
		if(kTouchDownProperty != null)
			kTouchDown = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down"));
		
		//...............................................................
		// FREE ROLL DURATION
		String freeRollDurationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/free_roll_duration");
		if(freeRollDurationProperty != null)
			freeRollDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/free_roll_duration");
		
		
		//===========================================================================================
		// READING CLIMB DATA ...	
		Double kClimbWeightAEO = 1.0;
		Double kClimbWeightOEI = 0.97;
		Amount<Velocity> climbSpeed = null;
		Amount<Length> initialClimbAltitude = null;
		Amount<Length> finalClimbAltitude = null;
		
		//...............................................................
		// K CLIMB WEIGHT AEO
		String kClimbWeightAEOProperty = reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AEO");
		if(kClimbWeightAEOProperty != null)
			kClimbWeightAEO = Double.valueOf(reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AEO"));
		
		//...............................................................
		// K CLIMB WEIGHT OEI
		String kClimbWeightOEIProperty = reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_OEI");
		if(kClimbWeightOEIProperty != null)
			kClimbWeightOEI = Double.valueOf(reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_OEI"));
		
		//...............................................................
		// CLIMB SPEED
		String climbSpeedProperty = reader.getXMLPropertyByPath("//performance/climb/climb_speed_CAS");
		if(climbSpeedProperty != null)
			climbSpeed = reader.getXMLAmountWithUnitByPath("//performance/climb/climb_speed_CAS").to(SI.METERS_PER_SECOND);
		
		//...............................................................
		// INITIAL CLIMB ALTITUDE
		String initialClimbAltitudeProperty = reader.getXMLPropertyByPath("//performance/climb/initial_climb_altitude");
		if(initialClimbAltitudeProperty != null)
			initialClimbAltitude = reader.getXMLAmountLengthByPath("//performance/climb/initial_climb_altitude");
		
		//...............................................................
		// FINAL CLIMB ALTITUDE
		String finalClimbAltitudeProperty = reader.getXMLPropertyByPath("//performance/climb/final_climb_altitude");
		if(finalClimbAltitudeProperty != null)
			finalClimbAltitude = reader.getXMLAmountLengthByPath("//performance/climb/final_climb_altitude");
		
		//===========================================================================================
		// READING CRUISE DATA ...	
		List<Amount<Length>> altitudeListCruise = null;
		Double kCruiseWeight = 0.97;
		
		//...............................................................
		// ALTITUDE LIST CRUISE
		List<String> altitudeListCruiseProperty = reader.getXMLPropertiesByPath("//performance/cruise/altitudes_array");
		if(!altitudeListCruiseProperty.isEmpty()) {
			altitudeListCruise = reader.readArrayofAmountFromXML("//performance/cruise/altitudes_array"); 
		}
		//...............................................................
		// K CRUISE WEIGHT 
		String kCruiseWeightProperty = reader.getXMLPropertyByPath("//performance/cruise/k_cruise_weight");
		if(kCruiseWeightProperty != null)
			kCruiseWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/cruise/k_cruise_weight"));
		
		
		//===========================================================================================
		// READING FLIGHT MANEUVERING AND GUST ENVELOPE DATA ...
		Double cLmaxInverted = -1.0;
		//...............................................................
		// CL MAX INVERTED
		String cLmaxInvertedProperty = reader.getXMLPropertyByPath("//performance/flight_maneuvering_and_gust_envelope/cLmax_inverted");
		if(cLmaxInvertedProperty != null)
			cLmaxInverted = Double.valueOf(reader.getXMLPropertyByPath("//performance/flight_maneuvering_and_gust_envelope/cLmax_inverted"));

		
		//===========================================================================================
		// READING DESCENT DATA ...
		Amount<Velocity> rateOfDescent = null;
		Amount<Velocity> speedDescentCAS = null;
		Double kDescentWeight = 0.9;
		Amount<Length> initialDescentAltitude = null;
		Amount<Length> finalDescentAltitude = null;
		//...............................................................
		// RATE OF DESCENT 
		String rateOfDescentProperty = reader.getXMLPropertyByPath("//performance/descent/rate_of_descent");
		if(rateOfDescentProperty != null)
			rateOfDescent = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/descent/rate_of_descent");		
		//...............................................................
		// SPEED DESCENT CAS
		String speedDescentCASProperty = reader.getXMLPropertyByPath("//performance/descent/descent_speed_CAS");
		if(speedDescentCASProperty != null)
			speedDescentCAS = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/descent/descent_speed_CAS");
		//...............................................................
		// K DESCENT WEIGHT
		String kDescentWeightProperty = reader.getXMLPropertyByPath("//performance/descent/k_descent_weight");
		if(kDescentWeightProperty != null)
			kDescentWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/descent/k_descent_weight"));
		//...............................................................
		// INITIAL DESCENT ALTITUDE
		String initialDescentAltitudeProperty = reader.getXMLPropertyByPath("//performance/descent/initial_descent_altitude");
		if(initialDescentAltitudeProperty != null)
			initialDescentAltitude = reader.getXMLAmountLengthByPath("//performance/descent/initial_descent_altitude");
		
		//...............................................................
		// FINAL DESCENT ALTITUDE
		String finalDescentAltitudeProperty = reader.getXMLPropertyByPath("//performance/descent/final_descent_altitude");
		if(finalDescentAltitudeProperty != null)
			finalDescentAltitude = reader.getXMLAmountLengthByPath("//performance/descent/final_descent_altitude");
		
		//===========================================================================================
		// READING MISSION PROFILE DATA ...
		Amount<Length> missionRange = null;
		Amount<Length> alternateCruiseLength = Amount.valueOf(0.0, SI.METER);
		Amount<Length> alternateCruiseAltitude = Amount.valueOf(15.24, SI.METER);
		Amount<Duration> holdingDuration = Amount.valueOf(0.0, SI.SECOND);
		Amount<Length> holdingAltitude = Amount.valueOf(15.24, SI.METER);
		Double holdingMachNumber = 0.01; // default value but != 0.0
		Double fuelReserve = 0.0;
		Boolean calculateSfcCruise = null;
		List<Double> sfcCruiseList = new ArrayList<>();
		List<Double> throttleList = new ArrayList<>();
		Boolean calculateSfcAlternateCruise = null;
		List<Double> sfcAlternateCruiseList = new ArrayList<>();
		List<Double> throttleAlternateCruiseList = new ArrayList<>();
		Boolean calculateSfcHolding = null;
		List<Double> sfcHoldingList = new ArrayList<>();
		List<Double> throttleHoldingList = new ArrayList<>();
		Amount<Length> takeOffMissionAltitude = null;
		Double landingFuelFlow = 0.0;
		
		//...............................................................
		// MISSION RANGE
		List<String> missionRangeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/mission_range");
		if(!missionRangeProperty.isEmpty()) {
			missionRange = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/mission_range"); 
		}
		//...............................................................
		// ALTERNATE CRUISE LENGTH
		List<String> alternateCruiseLengthProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/alternate_cruise_length");
		if(!alternateCruiseLengthProperty.isEmpty()) {
			alternateCruiseLength = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/alternate_cruise_length"); 
		}
		//...............................................................
		// ALTERNATE CRUISE ALTITUDE
		List<String> alternateCruiseAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/alternate_cruise_altitude");
		if(!alternateCruiseAltitudeProperty.isEmpty()) {
			alternateCruiseAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/alternate_cruise_altitude"); 
		}
		//...............................................................
		// HOLDING DURATION
		List<String> holdingDurationProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_duration");
		if(!holdingDurationProperty.isEmpty()) {
			holdingDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/mission_profile_and_payload_range/holding_duration"); 
		}
		//...............................................................
		// HOLDING ALTITUDE
		List<String> holdingAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_altitude");
		if(!holdingAltitudeProperty.isEmpty()) {
			holdingAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/holding_altitude"); 
		}
		//...............................................................
		// HOLDING MACH NUMBER
		List<String> holdingMachNumberProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_mach_number");
		if(!holdingMachNumberProperty.isEmpty()) {
			holdingMachNumber = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/holding_mach_number")); 
		}
		//...............................................................
		// FUEL RESERVE
		List<String> fuelReserveProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/fuel_reserve");
		if(!fuelReserveProperty.isEmpty()) {
			fuelReserve = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/fuel_reserve")); 
		}
		//...............................................................
		// SFC FUNCTION CRUISE
		String sfcFunctionCruiseCalculateProperty = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(),
				"//performance/mission_profile_and_payload_range/cruise_sfc_function/@calculate"
				);
		MyInterpolatingFunction sfcCruiseFunction = new MyInterpolatingFunction();
		
		if(sfcFunctionCruiseCalculateProperty != null) {
			
			if(sfcFunctionCruiseCalculateProperty.equalsIgnoreCase("TRUE")) 
				calculateSfcCruise = Boolean.TRUE;
				
			else if(sfcFunctionCruiseCalculateProperty.equalsIgnoreCase("FALSE")) {
				
				calculateSfcCruise = Boolean.FALSE;
				
				String sfcFunctionCruiseProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/cruise_sfc_function/sfc");
				if(sfcFunctionCruiseProperty != null)
					sfcCruiseList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/cruise_sfc_function/sfc"); 
				String throttleFunctionProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/cruise_sfc_function/throttle");
				if(throttleFunctionProperty != null)
					throttleList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/cruise_sfc_function/throttle");
				
				if(sfcCruiseList.size() > 1)
					if(sfcCruiseList.size() != throttleList.size())
					{
						System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
						System.exit(1);
					}
				if(sfcCruiseList.size() == 1) {
					sfcCruiseList.add(sfcCruiseList.get(0));
					throttleList.add(0.0);
					throttleList.add(1.0);
				}
				
				sfcCruiseFunction.interpolateLinear(
						MyArrayUtils.convertToDoublePrimitive(throttleList),
						MyArrayUtils.convertToDoublePrimitive(sfcCruiseList)
						);
			}
			else {
				System.err.println("SFC CRUISE CALCULATE ATTRIBUTTE HAS TO BE 'TRUE' OR 'FALSE' !!");
				System.exit(1);
			}
		}
		
		//...............................................................
		// SFC FUNCTION ALTERNATE CRUISE
		String sfcFunctionAlternateCruiseCalculateProperty = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(),
				"//performance/mission_profile_and_payload_range/alternate_cruise_sfc_function/@calculate"
				);
		MyInterpolatingFunction sfcAlternateCruiseFunction = new MyInterpolatingFunction();
		
		if(sfcFunctionAlternateCruiseCalculateProperty != null) {
			
			if(sfcFunctionAlternateCruiseCalculateProperty.equalsIgnoreCase("TRUE")) 
				calculateSfcAlternateCruise = Boolean.TRUE;
				
			else if(sfcFunctionCruiseCalculateProperty.equalsIgnoreCase("FALSE")) {
				
				calculateSfcAlternateCruise = Boolean.FALSE;
				
				String sfcFunctionAlternateCruiseProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/alternate_cruise_sfc_function/sfc");
				if(sfcFunctionAlternateCruiseProperty != null)
					sfcAlternateCruiseList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/alternate_cruise_sfc_function/sfc"); 
				String throttleAlternateCruiseFunctionProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/alternate_cruise_sfc_function/throttle");
				if(throttleAlternateCruiseFunctionProperty != null)
					throttleAlternateCruiseList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/alternate_cruise_sfc_function/throttle");
				
				if(sfcAlternateCruiseList.size() > 1)
					if(sfcAlternateCruiseList.size() != throttleAlternateCruiseList.size())
					{
						System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
						System.exit(1);
					}
				if(sfcAlternateCruiseList.size() == 1) {
					sfcAlternateCruiseList.add(sfcAlternateCruiseList.get(0));
					throttleAlternateCruiseList.add(0.0);
					throttleAlternateCruiseList.add(1.0);
				}
				
				sfcAlternateCruiseFunction.interpolateLinear(
						MyArrayUtils.convertToDoublePrimitive(throttleAlternateCruiseList),
						MyArrayUtils.convertToDoublePrimitive(sfcAlternateCruiseList)
						);
			}
			else {
				System.err.println("SFC CRUISE CALCULATE ATTRIBUTTE HAS TO BE 'TRUE' OR 'FALSE' !!");
				System.exit(1);
			}
		}

		//...............................................................
		// SFC FUNCTION HOLDING
		String sfcFunctionHoldingCalculateProperty = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(),
				"//performance/mission_profile_and_payload_range/holding_sfc_function/@calculate"
				);
		MyInterpolatingFunction sfcHoldingFunction = new MyInterpolatingFunction();
		
		if(sfcFunctionHoldingCalculateProperty != null) {
			
			if(sfcFunctionHoldingCalculateProperty.equalsIgnoreCase("TRUE")) 
				calculateSfcHolding = Boolean.TRUE;
				
			else if(sfcFunctionHoldingCalculateProperty.equalsIgnoreCase("FALSE")) {
				
				calculateSfcHolding = Boolean.FALSE;
				
				String sfcFunctionHoldingProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/holding_sfc_function/sfc");
				if(sfcFunctionHoldingProperty != null)
					sfcHoldingList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/holding_sfc_function/sfc"); 
				String throttleHoldingFunctionProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/holding_sfc_function/throttle");
				if(throttleHoldingFunctionProperty != null)
					throttleHoldingList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/holding_sfc_function/throttle");
				
				if(sfcHoldingList.size() > 1)
					if(sfcHoldingList.size() != throttleHoldingList.size())
					{
						System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
						System.exit(1);
					}
				if(sfcHoldingList.size() == 1) {
					sfcHoldingList.add(sfcHoldingList.get(0));
					throttleHoldingList.add(0.0);
					throttleHoldingList.add(1.0);
				}
				
				sfcHoldingFunction.interpolateLinear(
						MyArrayUtils.convertToDoublePrimitive(throttleHoldingList),
						MyArrayUtils.convertToDoublePrimitive(sfcHoldingList)
						);
			}
			else {
				System.err.println("SFC CRUISE CALCULATE ATTRIBUTTE HAS TO BE 'TRUE' OR 'FALSE' !!");
				System.exit(1);
			}
		}

		//...............................................................
		// TAKE OFF MISSION ALITUDE
		List<String> takeOffMissionAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/take_off_mission_altitude");
		if(!takeOffMissionAltitudeProperty.isEmpty()) {
			takeOffMissionAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/take_off_mission_altitude"); 
		}
		//...............................................................
		// LANDING FUEL FLOW
		List<String> landingFuelFlowProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/landing_ground_idle_fuel_flow");
		if(!landingFuelFlowProperty.isEmpty()) {
			landingFuelFlow = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/landing_ground_idle_fuel_flow")); 
		}
		
		//===========================================================================================
		// READING PLOT LIST ...	
		List<PerformancePlotEnum> plotList = new ArrayList<PerformancePlotEnum>();
		if(theAircraft.getTheAnalysisManager().getPlotPerformance() == Boolean.TRUE) {

			//...............................................................
			// TAKE-OFF
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.TAKE_OFF)) {
				String takeOffSimulationsProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/simulations/@perform");
				if (takeOffSimulationsProperty != null) {
					if(takeOffSimulationsProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.TAKE_OFF_SIMULATIONS);
				}

				String balancedFieldLengthChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/balanced_field_length/@perform");
				if (balancedFieldLengthChartProperty != null) {
					if(balancedFieldLengthChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.BALANCED_FIELD_LENGTH);
				}
				
				String vmcPlotProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/vMC/@perform");
				if (vmcPlotProperty != null) {
					if(vmcPlotProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.VMC);
				}
			}				
			//...............................................................
			// CLIMB 
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.CLIMB)) {

				String thrustDragClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/thrust_drag/@perform");
				if (thrustDragClimbCurvesProperty != null) {
					if(thrustDragClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.THRUST_DRAG_CURVES_CLIMB);
				}
				
				String powerNeededAndAvailableClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/power_needed_and_available/@perform");
				if (powerNeededAndAvailableClimbCurvesProperty != null) {
					if(powerNeededAndAvailableClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CLIMB);
				}
				
				String climbEfficiencyChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/efficiency/@perform");
				if (climbEfficiencyChartProperty != null) {
					if(climbEfficiencyChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.EFFICIENCY_CURVES_CLIMB);
				}
				
				String rateOfClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/rate_of_climb_curves/@perform");
				if (rateOfClimbCurvesProperty != null) {
					if(rateOfClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.RATE_OF_CLIMB_CURVES);
				}

				String climbAngleCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/climb_angle_curves/@perform");
				if (climbAngleCurvesProperty != null) {
					if(climbAngleCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_ANGLE_CURVES);
				}
				
				String maxRateOfClimbEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/max_rate_of_climb_envelope/@perform");
				if (maxRateOfClimbEnvelopeProperty != null) {
					if(maxRateOfClimbEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.MAX_RATE_OF_CLIMB_ENVELOPE);
				}	
				
				String maxClimbAngleEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/max_climb_angle_envelope/@perform");
				if (maxClimbAngleEnvelopeProperty != null) {
					if(maxClimbAngleEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.MAX_CLIMB_ANGLE_ENVELOPE);
				}	
				
				String climbTimeChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/climb_time/@perform");
				if (climbTimeChartProperty != null) {
					if(climbTimeChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_TIME);
				}	
				
				String fuelUsedChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/fuel_used/@perform");
				if (fuelUsedChartProperty != null) {
					if(fuelUsedChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_FUEL_USED);
				}	
				
			}

			//...............................................................
			// CRUISE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.CRUISE)) {

				String thrustDragChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/thrust_drag/@perform");
				if (thrustDragChartProperty != null) {
					if(thrustDragChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.THRUST_DRAG_CURVES_CRUISE);
				}

				String powerNeededAndAvailableCruiseCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/power_needed_and_available/@perform");
				if (powerNeededAndAvailableCruiseCurvesProperty != null) {
					if(powerNeededAndAvailableCruiseCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CRUISE);
				}
				
				String efficiencyChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/efficiency/@perform");
				if (efficiencyChartProperty != null) {
					if(efficiencyChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.EFFICIENCY_CURVES);
				}

				String cruiseGridChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/cruise_grid/@perform");
				if (cruiseGridChartProperty != null) {
					if(cruiseGridChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CRUISE_GRID_CHART);
				}

				String cruiseFlightEnvelopeChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/flight_envelope/@perform");
				if (cruiseFlightEnvelopeChartProperty != null) {
					if(cruiseFlightEnvelopeChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE);
				}

			}
			//...............................................................
			// DESCENT
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.DESCENT)) {
				
				String descentProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/descent/@perform");
				if (descentProperty != null) {
					if(descentProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.DESCENT);
				}
				
			}
			//...............................................................
			// LANDING
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.LANDING)) {

				String landingSimulationsProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/landing/simulations/@perform");
				if (landingSimulationsProperty != null) {
					if(landingSimulationsProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.LANDING_SIMULATIONS);
				}

			}
			//...............................................................
			// PAYLOAD-RANGE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.PAYLOAD_RANGE)) {

				String payloadRangeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/payload_range/@perform");
				if (payloadRangeProperty != null) {
					if(payloadRangeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.PAYLOAD_RANGE);
				}

			}
			//...............................................................
			// V-n DIAGRAM
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.V_n_DIAGRAM)) {

				String maneuveringFlightAndGustEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/maneuvering_flight_and_gust_envelope/@perform");
				if (maneuveringFlightAndGustEnvelopeProperty != null) {
					if(maneuveringFlightAndGustEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM);
				}
			}
			//...............................................................
			// MISSION PROFILE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.MISSION_PROFILE)) {

				String rangeProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/range_profile/@perform");
				if (rangeProfileProperty != null) {
					if(rangeProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.RANGE_PROFILE);
				}
				
				String timeProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/time_profile/@perform");
				if (timeProfileProperty != null) {
					if(timeProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.TIME_PROFILE);
				}
				
				String fuelUsedProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/fuel_used_profile/@perform");
				if (fuelUsedProfileProperty != null) {
					if(fuelUsedProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.FUEL_USED_PROFILE);
				}
				
				String weightProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/weight_profile/@perform");
				if (weightProfileProperty != null) {
					if(weightProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.WEIGHT_PROFILE);
				}
			}
		}
		
		//===========================================================================================
		// BUILDING THE CALCULATOR ...
		IACPerformanceManager thePerformanceManagerInterface = new IACPerformanceManager.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setMaximumTakeOffMass(maximumTakeOffMass.to(SI.KILOGRAM))
				.setOperatingEmptyMass(operatingEmptyMass.to(SI.KILOGRAM))
				.setMaximumFuelMass(maximumFuelMass.to(SI.KILOGRAM))
				.setSinglePassengerMass(singlePassengerMass.to(SI.KILOGRAM))
				.addAllXcgPositionList(centerOfGravityList)
				.putAllCLmaxClean(cLmaxClean)
				.putAllCLAlphaClean(cLAlphaClean)
				.putAllCLAlphaTakeOff(cLAlphaTakeOff)
				.putAllCLAlphaLanding(cLAlphaLanding)
				.putAllCLmaxTakeOff(cLmaxTakeOff)
				.putAllCLZeroTakeOff(cLZeroTakeOff)
				.putAllCLmaxLanding(cLmaxLanding)
				.putAllCLZeroLanding(cLZeroLanding)
				.putAllPolarCLCruise(polarCLCruise)
				.putAllPolarCDCruise(polarCDCruise)
				.putAllPolarCLClimb(polarCLClimb)
				.putAllPolarCDClimb(polarCDClimb)
				.putAllPolarCLTakeOff(polarCLTakeOff)
				.putAllPolarCDTakeOff(polarCDTakeOff)
				.putAllPolarCLLanding(polarCLLanding)
				.putAllPolarCDLanding(polarCDLanding)
				.putAllTauRudderMap(tauRudderMap)
				.setMuFunction(muInterpolatingFunction)
				.setMuBrakeFunction(muBrakeInterpolatingFunction)
				.setDtRotation(dtRotation)
				.setDtHold(dtHold)
				.setAlphaGround(alphaGround)
				.setWindSpeed(windSpeed.to(SI.METERS_PER_SECOND))
				.setObstacleTakeOff(obstacleTakeOff.to(SI.METER))
				.setKRotation(kRotation)
				.setAlphaDotRotation(alphaDotRotation)
				.setKCLmax(kCLmax)
				.setDragDueToEngineFailure(dragDueToEngineFailure)
				.setKAlphaDot(kAlphaDot)
				.setKLandingWeight(kLandingWeight)
				.setObstacleLanding(obstacleLanding.to(SI.METER))
				.setThetaApproach(thetaApproach.to(NonSI.DEGREE_ANGLE))
				.setKApproach(kApproach)
				.setKFlare(kFlare)
				.setKTouchDown(kTouchDown)
				.setFreeRollDuration(freeRollDuration.to(SI.SECOND))
				.setKClimbWeightAEO(kClimbWeightAEO)
				.setKClimbWeightOEI(kClimbWeightOEI)
				.setClimbSpeedCAS(climbSpeed.to(SI.METERS_PER_SECOND))
				.setInitialClimbAltitude(initialClimbAltitude.to(SI.METER))
				.setFinalClimbAltitude(finalClimbAltitude.to(SI.METER))
				.addAllAltitudeListCruise(altitudeListCruise.stream().map(a -> a.to(SI.METER)).collect(Collectors.toList()))
				.setKCruiseWeight(kCruiseWeight)
				.setCLmaxInverted(cLmaxInverted)
				.setRateOfDescent(rateOfDescent.to(SI.METERS_PER_SECOND))
				.setSpeedDescentCAS(speedDescentCAS.to(SI.METERS_PER_SECOND))
				.setKDescentWeight(kDescentWeight)
				.setInitialDescentAltitude(initialDescentAltitude.to(SI.METER))
				.setFinalDescentAltitude(finalDescentAltitude.to(SI.METER))
				.setMissionRange(missionRange.to(SI.METER))
				.setAlternateCruiseLength(alternateCruiseLength.to(SI.METER))
				.setAlternateCruiseAltitude(alternateCruiseAltitude.to(SI.METER))
				.setHoldingDuration(holdingDuration.to(SI.SECOND))
				.setHoldingAltitude(holdingAltitude.to(SI.METER))
				.setHoldingMachNumber(holdingMachNumber)
				.setFuelReserve(fuelReserve)
				.setFirstGuessCruiseLength(missionRange.to(SI.METER).divide(3)) // first guess value equal to 1/3 of the total mission range
				.setCalculateSFCCruise(calculateSfcCruise)
				.setCalculateSFCAlternateCruise(calculateSfcAlternateCruise)
				.setCalculateSFCHolding(calculateSfcHolding)
				.setSfcFunctionCruise(sfcCruiseFunction)
				.setSfcFunctionAlternateCruise(sfcAlternateCruiseFunction)
				.setSfcFunctionHolding(sfcHoldingFunction)
				.setFirstGuessInitialMissionFuelMass(maximumFuelMass.to(SI.KILOGRAM).divide(2)) // first guess value equal to 1/2 of the maximum fuel
				.setTakeOffMissionAltitude(takeOffMissionAltitude.to(SI.METER))
				.setLandingFuelFlow(landingFuelFlow)
				.addAllTaskList(theAircraft.getTheAnalysisManager().getTaskListPerformance())
				.addAllPlotList(plotList)
				.build();
		
		ACPerformanceManager thePerformanceManager = new ACPerformanceManager();
		thePerformanceManager.setThePerformanceInterface(thePerformanceManagerInterface);
		
		return thePerformanceManager;
	}

	/**
	 * This method reads the task list, initializes the related calculators inner classes and 
	 * performe the required calculation
	 */
	public void calculate(String resultsFolderPath) {

		initializeData();

		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "PERFORMANCE"
				+ File.separator
				);

		for(int i=0; i<_thePerformanceInterface.getXcgPositionList().size(); i++) {

			String xcgFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "XCG_" + _thePerformanceInterface.getXcgPositionList().get(i)
					+ File.separator
					);
			
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {

				String takeOffFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "TAKE_OFF"
						+ File.separator
						);

				CalcTakeOff calcTakeOff = new CalcTakeOff();
				calcTakeOff.performTakeOffSimulation(
						_thePerformanceInterface.getMaximumTakeOffMass(), 
						_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().to(SI.METER),
						_thePerformanceInterface.getTheOperatingConditions().getMachTakeOff(),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotTakeOffPerformance(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
				calcTakeOff.calculateBalancedFieldLength(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotBalancedFieldLength(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
				calcTakeOff.calculateVMC(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotVMC(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {

				String climbFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "CLIMB"
						+ File.separator
						);

				CalcClimb calcClimb = new CalcClimb();
				calcClimb.calculateClimbPerformance(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightAEO()),
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightOEI()),
						_thePerformanceInterface.getInitialClimbAltitude(),
						_thePerformanceInterface.getFinalClimbAltitude(),
						true,
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcClimb.plotClimbPerformance(climbFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {

				String cruiseFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "CRUISE"
						+ File.separator
						);

				_weightListCruiseMap.put(_thePerformanceInterface.getXcgPositionList().get(i), new ArrayList<Amount<Force>>());

				Amount<Force> cruiseWeight = 
						Amount.valueOf(
								(_thePerformanceInterface.getMaximumTakeOffMass()
										.times(_thePerformanceInterface.getKCruiseWeight())
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								SI.NEWTON
								);
				for(int k=0; k<5; k++) {			
					_weightListCruiseMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).add(
							Amount.valueOf(
									Math.round(
											(cruiseWeight)
											.minus((cruiseWeight)
													.times(0.05*(4-k))
													)
											.getEstimatedValue()
											),
									SI.NEWTON
									)
							);
				}

				CalcCruise calcCruise = new CalcCruise();
				calcCruise.calculateThrustAndDrag(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				calcCruise.calculateFlightEnvelope(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				calcCruise.calculateEfficiency(_thePerformanceInterface.getXcgPositionList().get(i));
				calcCruise.calculateCruiseGrid(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcCruise.plotCruiseOutput(cruiseFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {

				String descentFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "DESCENT"
						+ File.separator
						);

				CalcDescent calcDescent = new CalcDescent();
				calcDescent.calculateDescentPerformance(
						_thePerformanceInterface.getInitialDescentAltitude().to(SI.METER),
						_thePerformanceInterface.getFinalDescentAltitude().to(SI.METER),
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKDescentWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcDescent.plotDescentPerformance(descentFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {

				String landingFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "LANDING"
						+ File.separator
						);

				CalcLanding calcLanding = new CalcLanding();
				calcLanding.performLandingSimulation(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcLanding.plotLandingPerformance(landingFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {

				String payloadRangeFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "PAYLOAD_RANGE"
						+ File.separator
						);

				CalcPayloadRange calcPayloadRange = new CalcPayloadRange();
				calcPayloadRange.fromMissionProfile(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcPayloadRange.plotPayloadRange(payloadRangeFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {

				String maneuveringFlightAndGustEnvelopeFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "V-n_DIAGRAM"
						+ File.separator
						);

				CalcFlightManeuveringAndGustEnvelope calcEnvelope =  new CalcFlightManeuveringAndGustEnvelope();
				calcEnvelope.fromRegulations(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcEnvelope.plotVnDiagram(maneuveringFlightAndGustEnvelopeFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {

				String missionProfilesFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "MISSION_PROFILES"
						+ File.separator
						);

				CalcMissionProfile calcMissionProfile = new CalcMissionProfile();
				calcMissionProfile.calculateMissionProfileIterative(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcMissionProfile.plotProfiles(missionProfilesFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			// PRINT RESULTS
			try {
				toXLSFile(
						xcgFolderPath + "Performance_" + _thePerformanceInterface.getXcgPositionList().get(i),
						_thePerformanceInterface.getXcgPositionList().get(i));
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
	}
	
	public void toXLSFile(String filenameWithPathAndExt, Double xcg) throws InvalidFormatException, IOException {
		
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
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = wb.createFont();
	    font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
		
        //--------------------------------------------------------------------------------
        // TAKE-OFF ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {
        	Sheet sheet = wb.createSheet("TAKE-OFF");
        	List<Object[]> dataListTakeOff = new ArrayList<>();

        	dataListTakeOff.add(new Object[] {"Description","Unit","Value"});
        	dataListTakeOff.add(new Object[] {"Ground roll distance","m", _groundRollDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","m", _rotationDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","m", _airborneDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","m", _takeOffDistanceAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","m", _takeOffDistanceFAR25Map.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","m", _balancedFieldLengthMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {" "});
           	dataListTakeOff.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","ft", _rotationDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","ft", _airborneDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","ft", _takeOffDistanceAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","ft", _takeOffDistanceFAR25Map.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","ft", _balancedFieldLengthMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","m/s", _vStallTakeOffMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","m/s", _v1Map.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","m/s", _vRotationMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","m/s", _vMCMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","m/s", _vLiftOffMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","m/s", _v2Map.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","kn", _vStallTakeOffMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","kn", _v1Map.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","kn", _vRotationMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","kn", _vMCMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","kn", _vLiftOffMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","kn", _v2Map.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"V1/VsTO","", _v1Map.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_Rot/VsTO","", _vRotationMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"VMC/VsTO"," ", _vMCMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_LO/VsTO","", _vLiftOffMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V2/VsTO","", _v2Map.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Take-off duration","s", _takeOffDurationMap.get(xcg).doubleValue(SI.SECOND)});

        	Row rowTakeOff = sheet.createRow(0);
        	Object[] objArrTakeOff = dataListTakeOff.get(0);
        	int cellnumTakeOff = 0;
        	for (Object obj : objArrTakeOff) {
        		Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
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

        	int rownumTakeOff = 1;
        	for (int i = 1; i < dataListTakeOff.size(); i++) {
        		objArrTakeOff = dataListTakeOff.get(i);
        		rowTakeOff = sheet.createRow(rownumTakeOff++);
        		cellnumTakeOff = 0;
        		for (Object obj : objArrTakeOff) {
        			Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
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
		// CLIMB ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {
        	Sheet sheetClimb = wb.createSheet("CLIMB");
        	List<Object[]> dataListClimb = new ArrayList<>();

        	dataListClimb.add(new Object[] {"Description","Unit","Value"});
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","m", _absoluteCeilingAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","ft", _absoluteCeilingAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","m", _serviceCeilingAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","ft", _serviceCeilingAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Minimum time to climb AEO","min", _minimumClimbTimeAEOMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	if(_climbTimeAtSpecificClimbSpeedAEOMap.get(xcg) != null)
        		dataListClimb.add(new Object[] {"Time to climb at given climb speed AEO","min", _climbTimeAtSpecificClimbSpeedAEOMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	dataListClimb.add(new Object[] {"Fuel used during climb AEO","kg", _fuelUsedDuringClimbMap.get(xcg).doubleValue(SI.KILOGRAM)});
        	dataListClimb.add(new Object[] {" "});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","m", _absoluteCeilingOEIMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","ft", _absoluteCeilingOEIMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","m", _serviceCeilingOEIMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","ft", _serviceCeilingOEIMap.get(xcg).doubleValue(NonSI.FOOT)});

        	Row rowClimb = sheetClimb.createRow(0);
        	Object[] objArrClimb = dataListClimb.get(0);
        	int cellnumClimb = 0;
        	for (Object obj : objArrClimb) {
        		Cell cell = rowClimb.createCell(cellnumClimb++);
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
        		sheetClimb.setDefaultColumnWidth(35);
        		sheetClimb.setColumnWidth(1, 2048);
        		sheetClimb.setColumnWidth(2, 3840);
        	}

        	int rownumClimb = 1;
        	for (int i = 1; i < dataListClimb.size(); i++) {
        		objArrClimb = dataListClimb.get(i);
        		rowClimb = sheetClimb.createRow(rownumClimb++);
        		cellnumClimb = 0;
        		for (Object obj : objArrClimb) {
        			Cell cell = rowClimb.createCell(cellnumClimb++);
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
        // CRUISE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {
        	Sheet sheetCruise = wb.createSheet("CRUISE");
        	List<Object[]> dataListCruise = new ArrayList<>();
        	
        	dataListCruise.add(new Object[] {"Description","Unit","Value"});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","N", _thrustAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","lb", _thrustAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","N", _dragAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","lb", _dragAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","W", _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","hp", _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","W", _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","hp", _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","m/s", _minSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","m/s", _maxSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","kn", _minSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","kn", _maxSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","m/s", _minSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","m/s", _maxSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","kn", _minSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","kn", _maxSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min Mach number at cruise altitude","", _minMachAtCruiseAltitudeMap.get(xcg)});
        	dataListCruise.add(new Object[] {"Max Mach number at cruise altitude","", _maxMachAtCruiseAltitudeMap.get(xcg)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Efficiency at cruise altitude and Mach","", _efficiencyAtCruiseAltitudeAndMachMap.get(xcg)});
        	dataListCruise.add(new Object[] {" "});

        	Row rowCruise = sheetCruise.createRow(0);
        	Object[] objArrCruise = dataListCruise.get(0);
        	int cellnumCruise = 0;
        	for (Object obj : objArrCruise) {
        		Cell cell = rowCruise.createCell(cellnumCruise++);
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
        		sheetCruise.setDefaultColumnWidth(35);
        		sheetCruise.setColumnWidth(1, 2048);
        		sheetCruise.setColumnWidth(2, 3840);
        	}

        	int rownumCruise = 1;
        	for (int i = 1; i < dataListCruise.size(); i++) {
        		objArrCruise = dataListCruise.get(i);
        		rowCruise = sheetCruise.createRow(rownumCruise++);
        		cellnumCruise = 0;
        		for (Object obj : objArrCruise) {
        			Cell cell = rowCruise.createCell(cellnumCruise++);
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
        // DESCENT ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {
        	Sheet sheetDescent = wb.createSheet("DESCENT");
        	List<Object[]> dataListDescent = new ArrayList<>();

        	dataListDescent.add(new Object[] {"Description","Unit","Value"});
        	dataListDescent.add(new Object[] {"Descent length","nmi", _totalDescentLengthMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListDescent.add(new Object[] {"Descent length","km", _totalDescentLengthMap.get(xcg).doubleValue(SI.KILOMETER)});
        	dataListDescent.add(new Object[] {"Descent duration","min", _totalDescentTimeMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	dataListDescent.add(new Object[] {"Fuel used during descent","kg", _totalDescentFuelUsedMap.get(xcg).doubleValue(SI.KILOGRAM)});

        	Row rowDescent = sheetDescent.createRow(0);
        	Object[] objArrDescent = dataListDescent.get(0);
        	int cellnumDescent = 0;
        	for (Object obj : objArrDescent) {
        		Cell cell = rowDescent.createCell(cellnumDescent++);
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
        		sheetDescent.setDefaultColumnWidth(35);
        		sheetDescent.setColumnWidth(1, 2048);
        		sheetDescent.setColumnWidth(2, 3840);
        	}

        	int rownumDescent = 1;
        	for (int i = 1; i < dataListDescent.size(); i++) {
        		objArrDescent = dataListDescent.get(i);
        		rowDescent = sheetDescent.createRow(rownumDescent++);
        		cellnumDescent = 0;
        		for (Object obj : objArrDescent) {
        			Cell cell = rowDescent.createCell(cellnumDescent++);
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
        // LANDING ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {
        	Sheet sheetLanding = wb.createSheet("LANDING");
        	List<Object[]> dataListLanding = new ArrayList<>();

        	dataListLanding.add(new Object[] {"Description","Unit","Value"});
        	dataListLanding.add(new Object[] {"Ground roll distance","m", _groundRollDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Flare distance","m", _flareDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Airborne distance","m", _airborneDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Landing distance","m", _landingDistanceMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","m", _landingDistanceFAR25Map.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Flare distance","ft", _flareDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Airborne distance","ft", _airborneDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Landing distance","ft", _landingDistanceMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","ft", _landingDistanceFAR25Map.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","m/s", _vStallLandingMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","m/s", _vTouchDownMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","m/s", _vFlareMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","m/s", _vApproachMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","kn", _vStallLandingMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","kn", _vTouchDownMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","kn", _vFlareMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","kn", _vApproachMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"V_TD/VsLND","", _vTouchDownMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_Flare/VsLND","", _vFlareMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_A/VsLND","", _vApproachMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Landing duration","s", _landingDurationMap.get(xcg).doubleValue(SI.SECOND)});

        	Row rowLanding = sheetLanding.createRow(0);
        	Object[] objArrLanding = dataListLanding.get(0);
        	int cellnumLanding = 0;
        	for (Object obj : objArrLanding) {
        		Cell cell = rowLanding.createCell(cellnumLanding++);
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
        		sheetLanding.setDefaultColumnWidth(35);
        		sheetLanding.setColumnWidth(1, 2048);
        		sheetLanding.setColumnWidth(2, 3840);
        	}

        	int rownumLanding = 1;
        	for (int i = 1; i < dataListLanding.size(); i++) {
        		objArrLanding = dataListLanding.get(i);
        		rowLanding = sheetLanding.createRow(rownumLanding++);
        		cellnumLanding = 0;
        		for (Object obj : objArrLanding) {
        			Cell cell = rowLanding.createCell(cellnumLanding++);
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
        // MISSION PROFILE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
        	Sheet sheetMissionProfile = wb.createSheet("MISSION PROFILE");
        	List<Object[]> dataListMissionProfile = new ArrayList<>();

        	if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE)) {

        		dataListMissionProfile.add(new Object[] {"Description","Unit","Value"});
        		dataListMissionProfile.add(new Object[] {"Total mission distance","nmi", _thePerformanceInterface.getMissionRange().to(NonSI.NAUTICAL_MILE)
        				.plus(_thePerformanceInterface.getAlternateCruiseLength()).to(NonSI.NAUTICAL_MILE)
        				.doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Total mission duration","min", _totalMissionTimeMap.get(xcg).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Aircraft mass at mission start","kg", _initialMissionMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft mass at mission end","kg", _endMissionMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Initial fuel mass for the assigned mission","kg", _initialFuelMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Total fuel used","kg", _totalFuelUsedMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Fuel reserve","%", _thePerformanceInterface.getFuelReserve()*100});
        		dataListMissionProfile.add(new Object[] {"Design passengers number","", _thePerformanceInterface.getTheAircraft().getCabinConfiguration().getNPax().doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Passengers number for this mission","", _theMissionProfileCalculatorMap.get(xcg).getPassengersNumber().doubleValue()});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off range","nmi", _rangeListMap.get(xcg).get(1).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Climb range","nmi", _rangeListMap.get(xcg).get(2).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(1).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Cruise range","nmi", _rangeListMap.get(xcg).get(3).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(2).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"First descent range","nmi", _rangeListMap.get(xcg).get(4).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(3).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Second climb range","nmi", _rangeListMap.get(xcg).get(5).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(4).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise range","nmi", _rangeListMap.get(xcg).get(6).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(5).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Second descent range","nmi", _rangeListMap.get(xcg).get(7).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(6).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Holding range","nmi", _rangeListMap.get(xcg).get(8).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(7).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Third descent range","nmi", _rangeListMap.get(xcg).get(9).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(8).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Landing range","nmi", _rangeListMap.get(xcg).get(10).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(9).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off duration","min", _timeListMap.get(xcg).get(1).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb duration","min", _timeListMap.get(xcg).get(2).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(1).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Cruise duration","min", _timeListMap.get(xcg).get(3).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(2).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"First descent duration","min", _timeListMap.get(xcg).get(4).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(3).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Second climb duration","min", _timeListMap.get(xcg).get(5).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(4).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise duration","min", _timeListMap.get(xcg).get(6).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(5).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Second descent duration","min", _timeListMap.get(xcg).get(7).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(6).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Holding duration","min", _timeListMap.get(xcg).get(8).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(7).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Third descent duration","min", _timeListMap.get(xcg).get(9).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(8).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Landing duration","min", _timeListMap.get(xcg).get(10).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(9).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off used fuel","kg", _fuelUsedListMap.get(xcg).get(1).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Climb used fuel","kg", _fuelUsedListMap.get(xcg).get(2).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(1).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Cruise used fuel","kg", _fuelUsedListMap.get(xcg).get(3).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(2).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"First descent used fuel","kg", _fuelUsedListMap.get(xcg).get(4).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(3).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Climb used fuel","kg", _fuelUsedListMap.get(xcg).get(5).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(4).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise used fuel","kg", _fuelUsedListMap.get(xcg).get(6).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(5).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Second descent used fuel","kg", _fuelUsedListMap.get(xcg).get(7).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(6).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Holding used fuel","kg", _fuelUsedListMap.get(xcg).get(8).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(7).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Third descent used fuel","kg", _fuelUsedListMap.get(xcg).get(9).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(8).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Landing used fuel","kg", _fuelUsedListMap.get(xcg).get(10).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(9).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at take-off start","kg", _massListMap.get(xcg).get(1).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at climb start","kg", _massListMap.get(xcg).get(2).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at cruise start","kg", _massListMap.get(xcg).get(3).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at first descent start","kg", _massListMap.get(xcg).get(4).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at second climb start","kg", _massListMap.get(xcg).get(5).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at alternate cruise start","kg", _massListMap.get(xcg).get(6).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at second descent start","kg", _massListMap.get(xcg).get(7).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at holding start","kg", _massListMap.get(xcg).get(8).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at third descnet start","kg", _massListMap.get(xcg).get(9).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at landing start","kg", _massListMap.get(xcg).get(10).doubleValue(SI.KILOGRAM)});        	
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"TAKE-OFF"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off start","kn", _speedTASMissionListMap.get(xcg).get(0).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off ending","kn", _speedTASMissionListMap.get(xcg).get(1).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at take-off start"," ", _machMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at take-off ending"," ", _machMissionListMap.get(xcg).get(1).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CL at take-off start"," ", _liftingCoefficientMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at take-off ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at take-off start"," ", _dragCoefficientMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at take-off ending"," ", _dragCoefficientMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at take-off start"," ", _efficiencyMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at take-off ending"," ", _efficiencyMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at take-off start","lbf", _thrustMissionListMap.get(xcg).get(0).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at take-off ending","lbf", _thrustMissionListMap.get(xcg).get(1).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at take-off start","lbf", _dragMissionListMap.get(xcg).get(0).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at take-off ending","lbf", _dragMissionListMap.get(xcg).get(1).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"CLIMB"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb start","kn", _speedTASMissionListMap.get(xcg).get(2).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb ending","kn", _speedTASMissionListMap.get(xcg).get(3).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at climb start"," ", _machMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at climb ending"," ", _machMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at climb start"," ", _liftingCoefficientMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at climb ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at climb start"," ", _dragCoefficientMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at climb ending"," ", _dragCoefficientMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at climb start"," ", _efficiencyMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at climb ending"," ", _efficiencyMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at climb start","lbf", _thrustMissionListMap.get(xcg).get(2).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at climb ending","lbf", _thrustMissionListMap.get(xcg).get(3).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at climb start","lbf", _dragMissionListMap.get(xcg).get(2).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at climb ending","lbf", _dragMissionListMap.get(xcg).get(3).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"CRUISE"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise start","kn", _speedTASMissionListMap.get(xcg).get(4).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise ending","kn", _speedTASMissionListMap.get(xcg).get(5).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at cruise start"," ", _machMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at cruise ending"," ", _machMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at cruise start"," ", _liftingCoefficientMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at cruise ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at cruise start"," ", _dragCoefficientMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at cruise ending"," ", _dragCoefficientMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at cruise start"," ", _efficiencyMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at cruise ending"," ", _efficiencyMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at cruise start","lbf", _thrustMissionListMap.get(xcg).get(4).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at cruise ending","lbf", _thrustMissionListMap.get(xcg).get(5).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at cruise start","lbf", _dragMissionListMap.get(xcg).get(4).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at cruise ending","lbf", _dragMissionListMap.get(xcg).get(5).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"FIRST DESCENT"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent start","kn", _speedTASMissionListMap.get(xcg).get(6).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent ending","kn", _speedTASMissionListMap.get(xcg).get(7).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at first descent start"," ", _machMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at first descent ending"," ", _machMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at first descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at first descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at first descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at first descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at first descent start"," ", _efficiencyMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at first descent ending"," ", _efficiencyMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at first descent start","lbf", _thrustMissionListMap.get(xcg).get(6).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at first descent ending","lbf", _thrustMissionListMap.get(xcg).get(7).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at second climb start", "lbf", _dragMissionListMap.get(xcg).get(6).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at second climb ending", "lbf", _dragMissionListMap.get(xcg).get(7).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});

        		if(_thePerformanceInterface.getAlternateCruiseAltitude().doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
        			dataListMissionProfile.add(new Object[] {"SECOND CLIMB"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb start","kn", _speedTASMissionListMap.get(xcg).get(8).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb ending","kn", _speedTASMissionListMap.get(xcg).get(9).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at second climb start"," ", _machMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at second climb ending"," ", _machMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second climb start", " ", _liftingCoefficientMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second climb ending", " ", _liftingCoefficientMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second climb start", " ", _dragCoefficientMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second climb ending", " ", _dragCoefficientMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second climb start", " ", _efficiencyMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second climb ending", " ", _efficiencyMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at second climb start", "lbf", _thrustMissionListMap.get(xcg).get(8).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at second climb ending", "lbf", _thrustMissionListMap.get(xcg).get(9).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at first descent start","lbf", _dragMissionListMap.get(xcg).get(8).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at first descent ending","lbf", _dragMissionListMap.get(xcg).get(9).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"ALTERNATE CRUISE"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise start","kn", _speedTASMissionListMap.get(xcg).get(10).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise ending","kn", _speedTASMissionListMap.get(xcg).get(11).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at alternate cruise start"," ", _machMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at alternate cruise ending"," ", _machMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at alternate cruise start"," ", _liftingCoefficientMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at alternate cruise ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at alternate cruise start"," ", _dragCoefficientMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at alternate cruise ending"," ", _dragCoefficientMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise start"," ", _efficiencyMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise ending"," ", _efficiencyMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise start","lbf", _thrustMissionListMap.get(xcg).get(10).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise ending","lbf", _thrustMissionListMap.get(xcg).get(11).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at alternate cruise start","lbf", _dragMissionListMap.get(xcg).get(10).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at alternate cruise ending","lbf", _dragMissionListMap.get(xcg).get(11).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"SECOND DESCENT"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent start","kn", _speedTASMissionListMap.get(xcg).get(12).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent ending","kn", _speedTASMissionListMap.get(xcg).get(13).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at second descent start"," ", _machMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at second descent ending"," ", _machMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second descent start"," ", _efficiencyMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second descent ending"," ", _efficiencyMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at second descent start","lbf", _thrustMissionListMap.get(xcg).get(12).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at second descent ending","lbf", _thrustMissionListMap.get(xcg).get(13).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second descent start","lbf", _dragMissionListMap.get(xcg).get(12).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second descent ending","lbf", _dragMissionListMap.get(xcg).get(13).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {" "});
        		}
        		if(_thePerformanceInterface.getHoldingDuration().doubleValue(NonSI.MINUTE) != 0.0) {
        			dataListMissionProfile.add(new Object[] {"HOLDING"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding start","kn", _speedTASMissionListMap.get(xcg).get(14).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding ending","kn", _speedTASMissionListMap.get(xcg).get(15).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at holding start"," ", _machMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at holding ending"," ", _machMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at holding start"," ", _liftingCoefficientMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at holding ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at holding start"," ", _dragCoefficientMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at holding ending"," ", _dragCoefficientMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at holding start"," ", _efficiencyMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at holding ending"," ", _efficiencyMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at holding start","lbf", _thrustMissionListMap.get(xcg).get(14).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at holding ending","lbf", _thrustMissionListMap.get(xcg).get(15).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at holding start","lbf", _dragMissionListMap.get(xcg).get(14).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at holding ending","lbf", _dragMissionListMap.get(xcg).get(15).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"THIRD DESCENT"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent start","kn", _speedTASMissionListMap.get(xcg).get(16).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent ending","kn", _speedTASMissionListMap.get(xcg).get(17).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at third descent start"," ", _machMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at third descent ending"," ", _machMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at third descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at third descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at third descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at third descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at third descent start"," ", _efficiencyMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at third descent ending"," ", _efficiencyMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at third descent start","lbf", _thrustMissionListMap.get(xcg).get(16).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at third descent ending","lbf", _thrustMissionListMap.get(xcg).get(17).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at third descent start","lbf", _dragMissionListMap.get(xcg).get(16).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at third descent ending","lbf", _dragMissionListMap.get(xcg).get(17).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {" "});
        		}
        		dataListMissionProfile.add(new Object[] {"LANDING"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing start","kn", _speedTASMissionListMap.get(xcg).get(18).doubleValue(NonSI.KNOT)});        	
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing ending","kn", _speedTASMissionListMap.get(xcg).get(19).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at landing start"," ", _machMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"Mach at landing ending"," ", _machMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at landing start", " ", _liftingCoefficientMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CL at landing ending", " ", _liftingCoefficientMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at landing start", " ", _dragCoefficientMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CD at landing ending", " ", _dragCoefficientMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at landing start", " ", _efficiencyMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"Efficiency at landing ending", " ", _efficiencyMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at landing start", "lbf", _thrustMissionListMap.get(xcg).get(18).doubleValue(NonSI.POUND_FORCE)});        	
        		dataListMissionProfile.add(new Object[] {"Thrust at landing ending", "lbf", _thrustMissionListMap.get(xcg).get(19).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at landing start", "lbf", _dragMissionListMap.get(xcg).get(18).doubleValue(NonSI.POUND_FORCE)});        	
        		dataListMissionProfile.add(new Object[] {"Drag at landing ending", "lbf", _dragMissionListMap.get(xcg).get(19).doubleValue(NonSI.POUND_FORCE)});

        		Row rowMissionProfile = sheetMissionProfile.createRow(0);
        		Object[] objArrMissionProfile = dataListMissionProfile.get(0);
        		int cellnumMissionProfile = 0;
        		for (Object obj : objArrMissionProfile) {
        			Cell cell = rowMissionProfile.createCell(cellnumMissionProfile++);
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
        			sheetMissionProfile.setDefaultColumnWidth(35);
        			sheetMissionProfile.setColumnWidth(1, 2048);
        			sheetMissionProfile.setColumnWidth(2, 3840);
        		}

        		int rownumMissionProfile = 1;
        		for (int i = 1; i < dataListMissionProfile.size(); i++) {
        			objArrMissionProfile = dataListMissionProfile.get(i);
        			rowMissionProfile = sheetMissionProfile.createRow(rownumMissionProfile++);
        			cellnumMissionProfile = 0;
        			for (Object obj : objArrMissionProfile) {
        				Cell cell = rowMissionProfile.createCell(cellnumMissionProfile++);
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
        }
        //--------------------------------------------------------------------------------
        // PAYLOAD-RANGE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
        	Sheet sheetPayloadRange = wb.createSheet("PAYLOAD-RANGE");
        	List<Object[]> dataListPayloadRange = new ArrayList<>();

        	if(_thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			) {

        		dataListPayloadRange.add(new Object[] {"Description","Unit","Value"});
        		dataListPayloadRange.add(new Object[] {"ALTITUDE","ft",_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(NonSI.FOOT)});
        		dataListPayloadRange.add(new Object[] {"MACH"," ",_thePerformanceInterface.getTheOperatingConditions().getMachCruise()});
        		dataListPayloadRange.add(new Object[] {" "});
        		dataListPayloadRange.add(new Object[] {"RANGE AT MAX PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _maxPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxPayloadMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtMaxPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {""});
        		dataListPayloadRange.add(new Object[] {"RANGE AT DESIGN PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtDesignPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _designPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtDesignPayloadMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtDesignPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {""});
        		dataListPayloadRange.add(new Object[] {"RANGE AT MAX FUEL"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxFuelMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _payloadAtMaxFuelMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxFuelMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePerformanceInterface.getMaximumFuelMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"RANGE AT ZERO PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtZeroPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _takeOffMassAtZeroPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", 0.0});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", 0.0});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePerformanceInterface.getMaximumFuelMass().doubleValue(SI.KILOGRAM)});

        		Row rowPayloadRange = sheetPayloadRange.createRow(0);
        		Object[] objArrPayloadRange = dataListPayloadRange.get(0);
        		int cellnumPayloadRange = 0;
        		for (Object obj : objArrPayloadRange) {
        			Cell cell = rowPayloadRange.createCell(cellnumPayloadRange++);
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
        			sheetPayloadRange.setDefaultColumnWidth(35);
        			sheetPayloadRange.setColumnWidth(1, 2048);
        			sheetPayloadRange.setColumnWidth(2, 3840);
        		}

        		int rownumPayloadRange = 1;
        		for (int i = 1; i < dataListPayloadRange.size(); i++) {
        			objArrPayloadRange = dataListPayloadRange.get(i);
        			rowPayloadRange = sheetPayloadRange.createRow(rownumPayloadRange++);
        			cellnumPayloadRange = 0;
        			for (Object obj : objArrPayloadRange) {
        				Cell cell = rowPayloadRange.createCell(cellnumPayloadRange++);
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
        }
        //--------------------------------------------------------------------------------
        // V-n DIAGRAM ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
        	Sheet sheetVnDiagram = wb.createSheet("V-n DIAGRAM");
        	List<Object[]> dataListVnDiagram = new ArrayList<>();

        	dataListVnDiagram.add(new Object[] {"Description","Unit","Value"});
        	dataListVnDiagram.add(new Object[] {"REGULATION"," ",_thePerformanceInterface.getTheAircraft().getRegulations().toString()});
        	dataListVnDiagram.add(new Object[] {"AIRCRAFT TYPE"," ",_thePerformanceInterface.getTheAircraft().getTypeVehicle().toString()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"BASIC MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point C"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point D"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point E"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point F"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point H"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInverted()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE"});
        	dataListVnDiagram.add(new Object[] {"Point A'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point C'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point D'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point E'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point F'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point H'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInvertedWithGust()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"FLAP MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point I"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE (with flaps)"});
        	dataListVnDiagram.add(new Object[] {"Point A'_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point I'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust()});

        	Row rowVnDiagram = sheetVnDiagram.createRow(0);
        	Object[] objArrVnDiagram = dataListVnDiagram.get(0);
        	int cellnumVnDiagram = 0;
        	for (Object obj : objArrVnDiagram) {
        		Cell cell = rowVnDiagram.createCell(cellnumVnDiagram++);
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
        		sheetVnDiagram.setDefaultColumnWidth(35);
        		sheetVnDiagram.setColumnWidth(1, 2048);
        		sheetVnDiagram.setColumnWidth(2, 3840);
        	}

        	int rownumVnDiagram = 1;
        	for (int i = 1; i < dataListVnDiagram.size(); i++) {
        		objArrVnDiagram = dataListVnDiagram.get(i);
        		rowVnDiagram = sheetVnDiagram.createRow(rownumVnDiagram++);
        		cellnumVnDiagram = 0;
        		for (Object obj : objArrVnDiagram) {
        			Cell cell = rowVnDiagram.createCell(cellnumVnDiagram++);
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
	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tPerformance Analysis\n")
				.append("\t-------------------------------------\n")
				;

		for(int i=0; i<_thePerformanceInterface.getXcgPositionList().size(); i++) {

			Double xcg = _thePerformanceInterface.getXcgPositionList().get(i);
			
			sb.append("\n\t-------------------------------------\n")
			.append("\tXCG = " + xcg + "\n")
			.append("\t-------------------------------------\n")
			;
			
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {

				sb.append("\tTAKE-OFF\n")
				.append("\t-------------------------------------\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tRotation distance = " + _rotationDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAEO take-off distance = " + _takeOffDistanceAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25Map.get(xcg).to(SI.METER) + "\n")
				.append("\t\tBalanced field length = " + _balancedFieldLengthMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tRotation distance = " + _rotationDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAEO take-off distance = " + _takeOffDistanceAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25Map.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tBalanced field length = " + _balancedFieldLengthMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tDecision speed (V1) = " + _v1Map.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tRotation speed (V_Rot) = " + _vRotationMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMiminum control speed (VMC) = " + _vMCMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tLift-off speed (V_LO) = " + _vLiftOffMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tTake-off safety speed (V2) = " + _v2Map.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOffMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tDecision speed (V1) = " + _v1Map.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tRotation speed (V_Rot) = " + _vRotationMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMiminum control speed (VMC) = " + _vMCMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tLift-off speed (V_LO) = " + _vLiftOffMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tTake-off safety speed (V2) = " + _v2Map.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tV1/VsTO = " + _v1Map.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV_Rot/VsTO = " + _vRotationMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tVMC/VsTO = " + _vMCMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV_LO/VsTO = " + _vLiftOffMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV2/VsTO = " + _v2Map.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off duration = " + _takeOffDurationMap.get(xcg) + "\n")
				.append("\t-------------------------------------\n")
				;
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {

				sb.append("\tCLIMB\n")
				.append("\t-------------------------------------\n")
				.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tService ceiling AEO = " + _serviceCeilingAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tService ceiling AEO = " + _serviceCeilingAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tMinimum time to climb AEO = " + _minimumClimbTimeAEOMap.get(xcg).to(NonSI.MINUTE) + "\n");
				if(_climbTimeAtSpecificClimbSpeedAEOMap.get(xcg) != null)
					sb.append("\t\tTime to climb at given climb speed AEO = " + _climbTimeAtSpecificClimbSpeedAEOMap.get(xcg).to(NonSI.MINUTE) + "\n");
				sb.append("\t\tFuel used durign climb AEO = " + _fuelUsedDuringClimbMap.get(xcg).to(SI.KILOGRAM) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEIMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEIMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tService ceiling OEI = " + _serviceCeilingOEIMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tService ceiling OEI = " + _serviceCeilingOEIMap.get(xcg).to(NonSI.FOOT) + "\n");

				sb.append("\t-------------------------------------\n");

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {

				sb.append("\tCRUISE\n")
				.append("\t-------------------------------------\n")
				.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMachMap.get(xcg).to(SI.NEWTON) + "\n")
				.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.POUND_FORCE) + "\n")
				.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMachMap.get(xcg).to(SI.NEWTON) + "\n")
				.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.POUND_FORCE) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).to(SI.WATT) + "\n")
				.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.HORSEPOWER) + "\n")
				.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).to(SI.WATT) + "\n")
				.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.HORSEPOWER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin Mach at cruise altitude = " + _minMachAtCruiseAltitudeMap.get(xcg) + "\n")
				.append("\t\tMax Mach at cruise altitude = " + _maxMachAtCruiseAltitudeMap.get(xcg) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tEfficiency at cruise altitude and Mach = " + _efficiencyAtCruiseAltitudeAndMachMap.get(xcg) + "\n")
				.append("\t-------------------------------------\n");

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {

				sb.append("\tDESCENT\n")
				.append("\t-------------------------------------\n")
				.append("\t\tDescent length = " + _totalDescentLengthMap.get(xcg).to(SI.KILOMETER) + "\n")
				.append("\t\tDescent length = " + _totalDescentLengthMap.get(xcg).to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tDescent duration = " + _totalDescentTimeMap.get(xcg).to(NonSI.MINUTE) + "\n")
				.append("\t\tFuel used during descent = " + _totalDescentFuelUsedMap.get(xcg).to(SI.KILOGRAM) + "\n")
				.append("\t-------------------------------------\n");
				;

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {

				sb.append("\tLANDING\n")
				.append("\t-------------------------------------\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFlare distance = " + _flareDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tLanding distance = " + _landingDistanceMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25Map.get(xcg).to(SI.METER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFlare distance = " + _flareDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tLanding distance = " + _landingDistanceMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25Map.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed landing (VsLND)= " + _vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tTouchdown speed (V_TD) = " + _vTouchDownMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tFlare speed (V_Flare) = " + _vFlareMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tApproach speed (V_Approach) = " + _vApproachMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed landing (VsLND)= " + _vStallLandingMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tTouchdown speed (V_TD) = " + _vTouchDownMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tFlare speed (V_Flare) = " + _vFlareMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tApproach speed (V_Approach) = " + _vApproachMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tV_TD/VsLND = " + _vTouchDownMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\tV_Flare/VsLND = " + _vFlareMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\tV_Approach/VsLND = " + _vApproachMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tLanding duration = " + _landingDurationMap.get(xcg) + "\n")
				.append("\t-------------------------------------\n")
				;
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
				sb.append("\tMISSION PROFILE\n");
				if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE))
					sb.append(_theMissionProfileCalculatorMap.get(xcg).toString());
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
				sb.append("\tPAYLOAD-RANGE\n");
				if(_thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						)
				sb.append(_thePayloadRangeCalculatorMap.get(xcg).toString());
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
				sb.append("\tV-n DIAGRAM\n")
				.append(_theEnvelopeCalculatorMap.get(xcg).toString());
			}
		}

		return sb.toString();
	}
	
	
	//............................................................................
	// TAKE-OFF INNER CLASS
	//............................................................................
	public class CalcTakeOff {
		
		public void performTakeOffSimulation(
				Amount<Mass> takeOffMass,
				Amount<Length> altitude,
				Double mach,
				Double xcg
				) {
			
			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theTakeOffCalculatorMap.put(
					xcg, 
					new TakeOffCalc(
							_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurface(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							altitude.to(SI.METER),
							mach,
							takeOffMass.to(SI.KILOGRAM),
							_thePerformanceInterface.getDtRotation(),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getKCLmax(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleGroundIdleTakeOff(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleTakeOff(), 
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							wingToGroundDistance,
							_thePerformanceInterface.getObstacleTakeOff(),
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getTheAircraft().getWing().getRiggingAngle(),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
							)
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theTakeOffCalculatorMap.get(xcg).calculateTakeOffDistanceODE(null, false, true);

			// Distances:
			_groundRollDistanceTakeOffMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(0).to(NonSI.FOOT)
					);
			_rotationDistanceTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(1)
					.minus(_groundRollDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT)
					);
			_airborneDistanceTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(2)
					.minus(_rotationDistanceTakeOffMap.get(xcg))
					.minus(_groundRollDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT));
			_takeOffDistanceAEOMap.put(
					xcg, 
					_groundRollDistanceTakeOffMap.get(xcg)
					.plus(_rotationDistanceTakeOffMap.get(xcg))
					.plus(_airborneDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT));
			_takeOffDistanceFAR25Map.put(
					xcg, 
					_takeOffDistanceAEOMap.get(xcg).times(1.15).to(NonSI.FOOT)
					);
			
			// Velocities:
			_vStallTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getvSTakeOff().to(NonSI.KNOT)
					);
			_vRotationMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getvRot().to(NonSI.KNOT)
					);
			_vLiftOffMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getvLO().to(NonSI.KNOT)
					);
			_v2Map.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getV2().to(NonSI.KNOT)
					);
			
			// Duration:
			_takeOffDurationMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getTime().get(2)
					);
			
		}
		
		public void calculateBalancedFieldLength(Double xcg) {
			
			_theTakeOffCalculatorMap.get(xcg).calculateBalancedFieldLength();
			
			_v1Map.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getV1().to(NonSI.KNOT)
					);
			_balancedFieldLengthMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getBalancedFieldLength().to(NonSI.FOOT)
					);
			
		}
		
		public void calculateVMC(Double xcg) {
			
			Amount<Length> dimensionalXcg = 
					_thePerformanceInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().to(SI.METER).times(xcg)
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER));
			
			String veDSCDatabaseFileName = "VeDSC_database.h5";
			
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(
							MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), veDSCDatabaseFileName
							),
					MyConfiguration.getDir(FoldersEnum.DATABASE_DIR)
					);

			// GETTING THE FUSELAGE HEGHT AR V-TAIL MAC (c/4)
			List<Amount<Length>> vX = _thePerformanceInterface.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
			List<Amount<Length>> vZUpper = _thePerformanceInterface.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();
			List<Amount<Length>> vZLower = _thePerformanceInterface.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();
			
			List<Amount<Length>> sectionHeightsList = new ArrayList<>();
			List<Amount<Length>> xListInterpolation = new ArrayList<>();
			for(int i=vX.size()-5; i<vX.size(); i++) {
				sectionHeightsList.add(
						vZUpper.get(i).minus(vZLower.get(i))
						);
				xListInterpolation.add(vX.get(i));
			}
			
			Amount<Length> diameterAtVTailQuarteMAC = 
					Amount.valueOf( 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(xListInterpolation),
									MyArrayUtils.convertListOfAmountTodoubleArray(sectionHeightsList),
									_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
									.plus(_thePerformanceInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
									.plus(_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().times(0.25))
									.doubleValue(SI.METER)
									),
							SI.METER
							);
			
			double tailConeTipToFuselageRadiusRatio = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT()
					.divide(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionHeight().divide(2))
					.getEstimatedValue();
			
			veDSCDatabaseReader.runAnalysis(
					_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getWing().getPositionRelativeToAttachment(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
					_thePerformanceInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
					diameterAtVTailQuarteMAC.doubleValue(SI.METER), 
					tailConeTipToFuselageRadiusRatio
					);

			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getTheBalance() == null)
				_thePerformanceInterface.getTheAircraft().calculateArms(_thePerformanceInterface.getTheAircraft().getVTail(), dimensionalXcg);
			
			// cNb vertical [1/deg]
			double cNbVertical = MomentCalc.calcCNbetaVerticalTail(
					_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getAspectRatio(),
					_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getLiftingSurfaceArm().doubleValue(SI.METER),
					_thePerformanceInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER),
					_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					_thePerformanceInterface.getTheAircraft().getVTail().getSurface().doubleValue(SI.SQUARE_METRE), 
					_thePerformanceInterface.getTheAircraft().getVTail().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
					_thePerformanceInterface.getTheAircraft().getVTail().getAirfoilList().get(0)
						.getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
					_thePerformanceInterface.getTheOperatingConditions().getMachTakeOff(), 
					veDSCDatabaseReader.getkFv(),
					veDSCDatabaseReader.getkWv(),
					veDSCDatabaseReader.getkHv()
					);
			
			//..................................................................................
			// CALCULATING THE THRUST YAWING MOMENT
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER)
							),
					_theTakeOffCalculatorMap.get(xcg).getvSTakeOff().times(1.2).doubleValue(SI.METERS_PER_SECOND),
					250
					);

			double[] thrust = new double[speed.length];
			if ((_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP) 
					&& (Double.valueOf(
							_thePerformanceInterface.getTheAircraft()
							.getPowerPlant()
							.getTurbopropEngineDatabaseReader()
							.getThrustAPR(
									_thePerformanceInterface.getTheOperatingConditions().getMachTakeOff(),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER),
									_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
									)
							) != 0.0
						)
					)
				thrust = ThrustCalc.calculateThrustVsSpeed(
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_thePerformanceInterface.getTheOperatingConditions().getThrottleTakeOff(),
						_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER),
						EngineOperatingConditionEnum.APR,
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber()-1,
						speed
						);
			else
				thrust = ThrustCalc.calculateThrustVsSpeed(
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_thePerformanceInterface.getTheOperatingConditions().getThrottleTakeOff(),
						_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER),
						EngineOperatingConditionEnum.TAKE_OFF,
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
						_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber()-1,
						speed
						);

			List<Amount<Length>> enginesArms = new ArrayList<>();
			for(int i=0; i<_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
				enginesArms.add(_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes());
			
			Amount<Length> maxEngineArm = 
					Amount.valueOf(
							MyArrayUtils.getMax(
									MyArrayUtils.convertListOfAmountToDoubleArray(
											enginesArms
											)
									),
							SI.METER
							);
			
			_thrustMomentOEIMap.put(
					xcg, 
					new double[thrust.length]
					);
			
			for(int i=0; i < thrust.length; i++){
				_thrustMomentOEIMap.get(xcg)[i] = thrust[i]*maxEngineArm.doubleValue(SI.METER);
			}

			//..................................................................................
			// CALCULATING THE VERTICAL TAIL YAWING MOMENT
			_yawingMomentOEIMap.put(xcg, new double[_thrustMomentOEIMap.get(xcg).length]);
			
			for(int i=0; i < thrust.length; i++){
			_yawingMomentOEIMap.get(xcg)[i] = cNbVertical*
					_thePerformanceInterface.getTauRudderMap().get(xcg).value(
							_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)
							)*
					_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)*
					0.5*
					_thePerformanceInterface.getTheOperatingConditions().getDensityTakeOff().getEstimatedValue()*
					Math.pow(speed[i],2)*
					_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE)*
					_thePerformanceInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER);
			}
			
			//..................................................................................
			// CALCULATING THE VMC
			
			double[] curvesIntersection = MyArrayUtils.intersectArraysSimple(
					_thrustMomentOEIMap.get(xcg),
					_yawingMomentOEIMap.get(xcg)
					);
			int indexOfVMC = 0;
			for(int i=0; i<curvesIntersection.length; i++)
				if(curvesIntersection[i] != 0.0) {
					indexOfVMC = i;
				}			

			if(indexOfVMC != 0)
				_vMCMap.put(
						xcg, 
						Amount.valueOf(
								speed[indexOfVMC],
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
			else {
				System.err.println("WARNING: (VMC - TAKE-OFF) NO INTERSECTION FOUND ...");
				_vMCMap.put(
						xcg, 
						Amount.valueOf(
								0.0,
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
			}
			
		}

		public void plotTakeOffPerformance(String takeOffFolderPath, Double xcg) {

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.TAKE_OFF_SIMULATIONS))
				try {
					_theTakeOffCalculatorMap.get(xcg).createTakeOffCharts(
							takeOffFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

		}
		
		public void plotBalancedFieldLength(String takeOffFolderPath, Double xcg) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.BALANCED_FIELD_LENGTH))
				_theTakeOffCalculatorMap.get(xcg).createBalancedFieldLengthChart(
						takeOffFolderPath,
						_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
						);
			
		}
		
		public void plotVMC(String takeOffFolderPath, Double xcg) {
			
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER)
							)/_vStallTakeOffMap.get(xcg).doubleValue(SI.METERS_PER_SECOND),
					1.20, // maximum possible value of the VMC
					250
					);
			
			double[][] thrustPlotVector = new double [2][speed.length];
			double[][] speedPlotVector = new double [2][speed.length];
			
			for(int i=0; i < speed.length; i++){
				speedPlotVector[0][i] = speed[i];
				speedPlotVector[1][i] = speed[i];
				thrustPlotVector[0][i] = _thrustMomentOEIMap.get(xcg)[i];
				thrustPlotVector[1][i] = _yawingMomentOEIMap.get(xcg)[i];
			}
			String[] legendValue = new String[2];
			legendValue[0] = "Thrust Moment";
			legendValue[1] = "Yawning Moment";
			
			MyChartToFileUtils.plot(speedPlotVector,thrustPlotVector,
					null, null, null, null,
					"V/VsTO", "Thrust - Yawing Moment",
					"", "N m",legendValue,
					takeOffFolderPath, "VMC",
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance());
			
		}
		
	}
	//............................................................................
	// END OF THE TAKE-OFF INNER CLASS
	//............................................................................
	
	//............................................................................
	// CLIMB INNER CLASS
	//............................................................................
	public class CalcClimb {

		public void calculateClimbPerformance(
				Amount<Mass> startClimbMassAEO,
				Amount<Mass> startClimbMassOEI,
				Amount<Length> initialClimbAltitude,
				Amount<Length> finalClimbAltitude,
				boolean performOEI ,
				Double xcg
				) {
			
			_theClimbCalculatorMap.put(
					xcg, 
					new ClimbCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getCLmaxClean().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getDragDueToEngineFailure()
							)
					);
			
			_theClimbCalculatorMap.get(xcg).calculateClimbPerformance(
					startClimbMassAEO,
					startClimbMassOEI,
					initialClimbAltitude,
					finalClimbAltitude,
					performOEI
					);
			
			_rcAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getRCMapAEO());
			_rcOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getRCMapOEI());
			_ceilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getCeilingMapAEO());
			_ceilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getCeilingMapOEI());
			_dragListAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getDragListAEO());
			_thrustListAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getThrustListOEI());
			_dragListOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getDragListOEI());
			_thrustListOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getThrustListOEI());
			_efficiencyAltitudeAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getEfficiencyMapAltitudeAEO());
			_absoluteCeilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getAbsoluteCeilingAEO());
			_serviceCeilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getServiceCeilingAEO());
			_minimumClimbTimeAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getMinimumClimbTimeAEO());
			_climbTimeAtSpecificClimbSpeedAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTimeAtSpecificClimbSpeedAEO());
			_fuelUsedDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalFuelUsed());
			
			_absoluteCeilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getAbsoluteCeilingOEI());
			_serviceCeilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getServiceCeilingOEI());
			
		}
		
		public void plotClimbPerformance(String climbFolderPath, Double xcg) {
			
			_theClimbCalculatorMap.get(xcg).plotClimbPerformance(_thePerformanceInterface.getPlotList(), climbFolderPath);
			
		}
	}
	//............................................................................
	// END OF THE CLIMB INNER CLASS
	//............................................................................

	//............................................................................
	// CRUISE INNER CLASS
	//............................................................................
	public class CalcCruise {
		
		public void calculateThrustAndDrag(Amount<Mass> startCruiseMass, Double xcg) {
			
			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));

			_dragListAltitudeParameterizationMap.put(xcg, new ArrayList<DragMap>());
			_thrustListAltitudeParameterizationMap.put(xcg, new ArrayList<ThrustMap>());

			double[] speedArrayAltitudeParameterization = new double[100];

			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				//..................................................................................................
				speedArrayAltitudeParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER)
								),
						100
						);
				
				if (!MathArrays.isMonotonic(speedArrayAltitudeParameterization, OrderDirection.INCREASING, true)) {
					System.err.println("WARNING: (THRUST CALCULATION ALTITUDE PARAMETERIZATION - CRUISE) THE SPEED ARRAY IS NOT MONOTONIC INCREASING. CRUISE STALL SPEED IS BIGGER THAN CRUISE SPEED. TERMINATING ...");
					return;
				}
				
				//..................................................................................................
				_dragListAltitudeParameterizationMap.get(xcg).add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								speedArrayAltitudeParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCDCruise().get(xcg)),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);

				//..................................................................................................
				_thrustListAltitudeParameterizationMap.get(xcg).add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								speedArrayAltitudeParameterization,
								EngineOperatingConditionEnum.CRUISE,
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(), 
								_thePerformanceInterface.getTheAircraft().getPowerPlant(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
								)
						);
			}

			List<Amount<Force>> thrustAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Force>> dragAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerAvailableAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerNeededAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				
				thrustAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed(),
										_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust(),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.NEWTON
								)
						);
				dragAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed(),
										_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag(),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.NEWTON
								)
						);
				powerAvailableAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed(),
										_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower(),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.WATT
								)
						);
				powerNeededAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed(),
										_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower(),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.WATT
								)
						);
			}
				
				
			_thrustAtCruiseAltitudeAndMachMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(thrustAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							)
					);
			_dragAtCruiseAltitudeAndMachMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(dragAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							)
					);
			_powerAvailableAtCruiseAltitudeAndMachMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerAvailableAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							)
					);
			_powerNeededAtCruiseAltitudeAndMachMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerNeededAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							)
					); 
			

			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			_dragListWeightParameterizationMap.put(xcg, new ArrayList<DragMap>());
			_thrustListWeightParameterizationMap.put(xcg, new ArrayList<ThrustMap>());
			
			double[] speedArrayWeightParameterization = new double[100];

			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				//..................................................................................................
				speedArrayWeightParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_thePerformanceInterface.getAltitudeListCruise()))
								),
						100
						);
				
				if (!MathArrays.isMonotonic(speedArrayWeightParameterization, OrderDirection.INCREASING, true)) {
					System.err.println("WARNING: (THRUST CALCULATION WEIGHT PARAMETERIZATION - CRUISE) THE SPEED ARRAY IS NOT MONOTONIC INCREASING. CRUISE STALL SPEED IS BIGGER THAN CRUISE SPEED. TERMINATING ...");
					System.exit(1);
				}
				//..................................................................................................
				_dragListWeightParameterizationMap.get(xcg).add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON),
								speedArrayWeightParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCDCruise().get(xcg)),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
			}
			//..................................................................................................
			_thrustListWeightParameterizationMap.get(xcg).add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							MyArrayUtils.linspace(
									SpeedCalc.calculateSpeedStall(
											_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
											_weightListCruiseMap.get(xcg).get(0).doubleValue(SI.NEWTON),
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
											),
									SpeedCalc.calculateTAS(
											_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
											MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_thePerformanceInterface.getAltitudeListCruise()))
											),
									100
									),
							EngineOperatingConditionEnum.CRUISE,
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(), 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
							)
					);
		}

		public void calculateFlightEnvelope(Amount<Mass> startCruiseMass, Double xcg) {

			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));

			_intersectionListMap.put(xcg, new ArrayList<>());
			_cruiseEnvelopeListMap.put(xcg, new ArrayList<>());

			List<DragMap> dragList = new ArrayList<>();
			List<ThrustMap> thrustList = new ArrayList<>();
			List<Amount<Length>> altitude = new ArrayList<>();
			altitude.add(Amount.valueOf(0.0, SI.METER));
			Amount<Length> deltaAltitude = Amount.valueOf(100, SI.METER);
			
			int nPointSpeed = 5000;
			double[] speedArray = new double[nPointSpeed];
			int i=0;
			
			// FIRST ITERATION STEP:
			//..................................................................................................
			speedArray = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							altitude.get(0).doubleValue(SI.METER),
							(startCruiseMass
									.times(AtmosphereCalc.g0)
									.getEstimatedValue()),
							_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
							),
					SpeedCalc.calculateTAS(2.0, altitude.get(0).doubleValue(SI.METER)),
					nPointSpeed
					);
			//..................................................................................................
			dragList.add(
					DragCalc.calculateDragAndPowerRequired(
							altitude.get(0).doubleValue(SI.METER),
							(startCruiseMass
								.times(AtmosphereCalc.g0)
								.getEstimatedValue()
								),
							speedArray,
							_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCDCruise().get(xcg)),
							_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
							meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
							meanAirfoil.getAirfoilCreator().getType()
							)
					);
					
			//..................................................................................................
			thrustList.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							altitude.get(0).doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							speedArray,
							EngineOperatingConditionEnum.CRUISE,
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(), 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
							)
					);
			//..................................................................................................
			_intersectionListMap.get(xcg).add(
					PerformanceCalcUtils.calculateDragThrustIntersection(
							altitude.get(0).doubleValue(SI.METER),
							speedArray,
							(startCruiseMass
									.times(AtmosphereCalc.g0)
									.getEstimatedValue()
									),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							dragList,
							thrustList
							)
					);

			while((_intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMaxSpeed()
					- _intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMinSpeed())
					>= 0.0001
					) {

				if ((_intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMaxSpeed()
						- _intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMinSpeed())
						< 0.0001
						) {
					System.err.println("");
				}
				
				if(i >= 1)
					altitude.add(altitude.get(i-1).plus(deltaAltitude));

				if(altitude.get(i).doubleValue(NonSI.FOOT) >= 50000) {
					System.err.println("WARNING: (FLIGHT ENVELOPE - CRUISE) MAXIMUM ALTITUDE OF 50000ft REACHED!!");
					break;
				}
					
				//..................................................................................................
				speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitude.get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								),
						SpeedCalc.calculateTAS(2.0, altitude.get(i).doubleValue(SI.METER)),
						nPointSpeed
						);
				//..................................................................................................
				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								altitude.get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								speedArray,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCDCruise().get(xcg)),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);

				//..................................................................................................
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								altitude.get(i).doubleValue(SI.METER),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								speedArray,
								EngineOperatingConditionEnum.CRUISE,
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(), 
								_thePerformanceInterface.getTheAircraft().getPowerPlant(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
								)
						);
				//..................................................................................................
				_intersectionListMap.get(xcg).add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								altitude.get(i).doubleValue(SI.METER),
								speedArray,
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								dragList,
								thrustList
								)
						);
				i++;
			}

			for (int j=0; j<altitude.size(); j++) 
				_cruiseEnvelopeListMap.get(xcg).add(
						PerformanceCalcUtils.calculateEnvelope(
								altitude.get(j).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								EngineOperatingConditionEnum.CRUISE,
								_intersectionListMap.get(xcg)
								)
						);

			List<Double> altitudeList = new ArrayList<>();
			List<Double> minSpeedTASList = new ArrayList<>();
			List<Double> minSpeedCASList = new ArrayList<>();
			List<Double> minMachList = new ArrayList<>();
			List<Double> maxSpeedTASList = new ArrayList<>();
			List<Double> maxSpeedCASList = new ArrayList<>();
			List<Double> maxMachList = new ArrayList<>();

			for(int j=0; j<_cruiseEnvelopeListMap.get(xcg).size(); j++) {

				double sigma = OperatingConditions.getAtmosphere(
						_cruiseEnvelopeListMap.get(xcg).get(j).getAltitude()
						).getDensity()*1000/1.225; 

				if(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed() != 0.0) {
					altitudeList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getAltitude());
					minSpeedTASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinSpeed());
					minSpeedCASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinSpeed()*(Math.sqrt(sigma)));
					minMachList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinMach());
					maxSpeedTASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed());
					maxSpeedCASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed()*(Math.sqrt(sigma)));
					maxMachList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxMach());
				}
			}

			_minSpeesTASAtCruiseAltitudeMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_maxSpeesTASAtCruiseAltitudeMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_minSpeesCASAtCruiseAltitudeMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_maxSpeesCASAtCruiseAltitudeMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_minMachAtCruiseAltitudeMap.put(
					xcg, 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(minMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);
			_maxMachAtCruiseAltitudeMap.put(
					xcg,  
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(maxMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);

			if(_maxMachAtCruiseAltitudeMap.get(xcg) < _thePerformanceInterface.getTheOperatingConditions().getMachCruise()) {
				System.err.println("WARNING: (FLIGHT ENVELOPE - CRUISE) THE CHOSEN CRUISE MACH NUMBER IS NOT INSIDE THE FLIGHT ENVELOPE");
			}
		}

		public void calculateEfficiency(Double xcg) {

			_efficiencyAltitudeMap.put(xcg, new HashMap<>());
			_efficiencyWeightMap.put(xcg, new HashMap<>());

			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				List<Double> liftAltitudeParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
				for(int j=0; j<_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed().length; j++) {
					liftAltitudeParameterization.add(
							LiftCalc.calculateLift(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()[j],
									_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(),
									LiftCalc.calculateLiftCoeff(
											(_thePerformanceInterface.getMaximumTakeOffMass()
													.times(_thePerformanceInterface.getKCruiseWeight())
													.times(AtmosphereCalc.g0)
													.getEstimatedValue()
													),
											_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()[j],
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude()
											)
									)			
							);
					efficiencyListCurrentAltitude.add(
							liftAltitudeParameterization.get(j)
							/ _dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag()[j]
							);
				}
				_efficiencyAltitudeMap.get(xcg).put(
						"Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(),
						efficiencyListCurrentAltitude
						);
			}
			
			List<Double> efficiencyAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++)
				efficiencyAltitudesAtCruiseMach.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed(),
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyAltitudeMap.get(xcg)
										.get("Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude())
										),
								SpeedCalc.calculateTAS(
										_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
										_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
										)
								)
						);
			
			_efficiencyAtCruiseAltitudeAndMachMap.put(
					xcg, 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_thePerformanceInterface.getAltitudeListCruise().stream()
									.map(a -> a.to(SI.METER))
									.collect(Collectors.toList())
									),
							MyArrayUtils.convertToDoublePrimitive(efficiencyAltitudesAtCruiseMach),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);
			
			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				List<Double> liftWeightParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentWeight = new ArrayList<>();
				for(int j=0; j<_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().length; j++) {
					liftWeightParameterization.add(
							LiftCalc.calculateLift(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()[j],
									_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
									LiftCalc.calculateLiftCoeff(
											_dragListWeightParameterizationMap.get(xcg).get(i).getWeight(),
											_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()[j],
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
											)
									)			
							);
					efficiencyListCurrentWeight.add(
							liftWeightParameterization.get(j)
							/ _dragListWeightParameterizationMap.get(xcg).get(i).getDrag()[j]
							);
				}
				_efficiencyWeightMap.get(xcg).put(
						"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight(),
						efficiencyListCurrentWeight
						);
			}			
		}
		
		public void calculateCruiseGrid(Double xcg) {

			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));
			
			List<DragMap> dragListWeightParameterization = new ArrayList<DragMap>();
			List<ThrustMap> thrustListWeightParameterization = new ArrayList<ThrustMap>();
			
			double[] speedArrayWeightParameterization = new double[100];
			speedArrayWeightParameterization = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_weightListCruiseMap.get(xcg).get(_weightListCruiseMap.get(xcg).size()-1).doubleValue(SI.NEWTON), 
							_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE), 
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
							),
					SpeedCalc.calculateTAS(
							1.0,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							),
					100
					);
			
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				//..................................................................................................
				dragListWeightParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON),
								speedArrayWeightParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								MyArrayUtils.convertToDoublePrimitive(_thePerformanceInterface.getPolarCDCruise().get(xcg)),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
			}
			//..................................................................................................
			thrustListWeightParameterization.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							speedArrayWeightParameterization,
							EngineOperatingConditionEnum.CRUISE,
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(), 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR()
							)
					);
			
			List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
			for(int i=0; i<_dragListWeightParameterizationMap.get(xcg).size(); i++) {
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								thrustListWeightParameterization.get(0).getSpeed(),
								_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								dragListWeightParameterization,
								thrustListWeightParameterization
								)
						);
			}
			
			Double[] machArray = null;
			Double[] efficiency = null;
			Double[] sfc = null;
			Double[] specificRange = null;
 			
			_specificRangeMap.put(xcg, new ArrayList<>());
			
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) { 
				if(intersectionList.get(i).getMaxMach() != 0.0) {
					machArray = MyArrayUtils.linspaceDouble(
							intersectionList.get(i).getMinMach(),
							intersectionList.get(i).getMaxMach(),
							_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().length);

					sfc = SpecificRangeCalc.calculateSfcVsMach(
							machArray,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant()
							);

					efficiency = MyArrayUtils.convertFromDoubleToPrimitive(
							MyArrayUtils.convertToDoublePrimitive(
									_efficiencyWeightMap.get(xcg).get(
											"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight()
											)
									)
							);

					specificRange = SpecificRangeCalc.calculateSpecificRangeVsMach(
							Amount.valueOf(
									_weightListCruiseMap.get(xcg).get(i).divide(AtmosphereCalc.g0).getEstimatedValue(),
									SI.KILOGRAM
									),
							machArray,
							sfc,
							efficiency,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getEtaPropeller(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType()
							);

					_specificRangeMap.get(xcg).add(
							new SpecificRangeMap(
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
									_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
									_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON),
									EngineOperatingConditionEnum.CRUISE,
									specificRange,
									machArray,
									efficiency,
									sfc
									)
							);
				}
			}
		}
				
		public void plotCruiseOutput(String cruiseFolderPath, Double xcg) {

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedAltitudeParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustAltitudes_SI = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustAltitudes_Imperial = new ArrayList<Double[]>();
				List<String> legendAltitudes_SI = new ArrayList<String>();
				List<String> legendAltitudes_Imperial = new ArrayList<String>();

				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag()));
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Drag at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Drag at " + Amount.valueOf(_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust()));
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Thrust at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Thrust at " + Amount.valueOf(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_SI, dragAndThrustAltitudes_SI,
							"Drag and Thrust curves at different altitudes",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendAltitudes_SI,
							cruiseFolderPath, "Drag_and_Thrust_curves_altitudes_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_Imperial, dragAndThrustAltitudes_Imperial,
							"Drag and Thrust curves at different altitudes",
							"Speed", "Forces",
							null, null, null, null,
							"kn", "lb",
							true, legendAltitudes_Imperial,
							cruiseFolderPath, "Drag_and_Thrust_curves_altitudes_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedWeightsParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedWeightsParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustWeights_SI = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustWeights_Imperial = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()));
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterizationMap.get(xcg).get(i).getDrag()));
					dragAndThrustWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getDrag())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendWeights.add("Drag at " + Math.round(_dragListWeightParameterizationMap.get(xcg).get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()));
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				dragAndThrustWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterizationMap.get(xcg).get(0).getThrust()));
				dragAndThrustWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterizationMap.get(xcg).get(0).getThrust())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
								.toArray()
								)
						);				
				legendWeights.add("Thrust");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization_SI, dragAndThrustWeights_SI,
							"Drag and Thrust curves at different weights",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendWeights,
							cruiseFolderPath, "Drag_and_Thrust_curves_weights_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedWeightsParameterization_Imperial, dragAndThrustWeights_Imperial,
							"Drag and Thrust curves at different weights",
							"Speed", "Forces",
							null, null, null, null,
							"kn", "lb",
							true, legendWeights,
							cruiseFolderPath, "Drag_and_Thrust_curves_weights_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedAltitudeParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableAltitudes_SI = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableAltitudes_Imperial = new ArrayList<Double[]>();
				List<String> legendAltitudes_SI = new ArrayList<String>();
				List<String> legendAltitudes_Imperial = new ArrayList<String>();

				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower()));
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Power needed at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Power needed at " + Amount.valueOf(_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower()));
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Power available at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Power available at " + Amount.valueOf(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_SI, powerNeededAndAvailableAltitudes_SI,
							"Power needed and available at different altitudes",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendAltitudes_SI,
							cruiseFolderPath, "Power_needed_and_available_altitudes_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_Imperial, powerNeededAndAvailableAltitudes_Imperial,
							"Power needed and available at different altitudes",
							"Speed", "Powers",
							null, null, null, null,
							"kn", "hp",
							true, legendAltitudes_Imperial,
							cruiseFolderPath, "Power_needed_and_available_altitudes_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				
				List<Double[]> speedWeightsParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedWeightsParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableWeights_SI = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableWeights_Imperial = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()));
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterizationMap.get(xcg).get(i).getPower()));
					powerNeededAndAvailableWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendWeights.add("Power needed at " + Math.round(_dragListWeightParameterizationMap.get(xcg).get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()));
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				powerNeededAndAvailableWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterizationMap.get(xcg).get(0).getPower()));
				powerNeededAndAvailableWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterizationMap.get(xcg).get(0).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
								.toArray()
								)
						);
				legendWeights.add("Power available");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization_SI, powerNeededAndAvailableWeights_SI,
							"Power needed and available at different weights",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendWeights,
							cruiseFolderPath, "Power_needed_and_available_weights_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedWeightsParameterization_Imperial, powerNeededAndAvailableWeights_Imperial,
							"Power needed and available at different weights",
							"Speed", "Powers",
							null, null, null, null,
							"kn", "hp",
							true, legendWeights,
							cruiseFolderPath, "Power_needed_and_available_weights_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE)) {
				
				List<Double> altitudeList_SI = new ArrayList<>();
				List<Double> altitudeList_Imperial = new ArrayList<>();
				List<Double> speedTASList_SI = new ArrayList<>();
				List<Double> speedTASList_Imperial = new ArrayList<>();
				List<Double> speedCASList_SI = new ArrayList<>();
				List<Double> speedCASList_Imperial = new ArrayList<>();
				List<Double> machList = new ArrayList<>();
			
				for(int i=0; i<_cruiseEnvelopeListMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MIN VALUES
					if(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed() != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude());
						altitudeList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed());
						speedTASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed(), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed()*(Math.sqrt(sigma)));
						speedCASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed()*(Math.sqrt(sigma)), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinMach());
					}
				}
				for(int i=0; i<_cruiseEnvelopeListMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MAX VALUES
					if(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed() != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude());
						altitudeList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed());
						speedTASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed(), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed()*(Math.sqrt(sigma)));
						speedCASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed()*(Math.sqrt(sigma)), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxMach());
					}
				}
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedTASList_SI),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Speed(TAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_TAS_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedCASList_SI),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Speed(CAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_CAS_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(machList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Mach", "Altitude",
						"", "m",
						cruiseFolderPath, "Cruise_flight_envelope_MACH_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedTASList_Imperial),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Speed(TAS)", "Altitude",
						"kn", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_TAS_IMPERIAL",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedCASList_Imperial),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Speed(CAS)", "Altitude",
						"kn", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_CAS_IMPERIAL",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(machList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Mach", "Altitude",
						"", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_MACH_IMPERIAL",true
						);
				
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.EFFICIENCY_CURVES)) {
				
				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedListAltitudeParameterization_TAS_SI = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_TAS_Imperial = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_CAS_SI = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_CAS_Imperial = new ArrayList<>();
				List<Double[]> machListAltitudeParameterization = new ArrayList<>();
				List<Double[]> efficiencyListAltitudeParameterization = new ArrayList<>();
				List<String> legendAltitude_SI = new ArrayList<>();
				List<String> legendAltitude_Imperial = new ArrayList<>();
				for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude()
							).getSpeedOfSound(); 
					
					speedListAltitudeParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									)
							);
					speedListAltitudeParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					speedListAltitudeParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.toArray()
									)
							);
					speedListAltitudeParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					machListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x/speedOfSound)
									.toArray()
									)
							);
					efficiencyListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyAltitudeMap.get(xcg).get(
													"Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude()
													)
											)
									)
							);
					legendAltitude_SI.add("Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude());
					legendAltitude_Imperial.add("Altitude = " + Amount.valueOf(_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
				}
				
				try {
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_TAS_SI, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_TAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_CAS_SI, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_CAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListAltitudeParameterization, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_Mach_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_TAS_Imperial, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_TAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_CAS_Imperial, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_CAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListAltitudeParameterization, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_Mach_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedListWeightParameterization_TAS_SI = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_TAS_Imperial = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_CAS_SI = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_CAS_Imperial = new ArrayList<>();
				List<Double[]> machListWeightParameterization = new ArrayList<>();
				List<Double[]> efficiencyListWeightParameterization = new ArrayList<>();
				List<String> legendWeight = new ArrayList<>();
				for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude()
							).getSpeedOfSound(); 
					
					speedListWeightParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									)
							);
					speedListWeightParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					speedListWeightParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.toArray()
									)
							);
					speedListWeightParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					machListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed())
									.map(x -> x/speedOfSound)
									.toArray()
									)
							);
					efficiencyListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyWeightMap.get(xcg).get(
													"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight()
													)
											)
									)
							);
					legendWeight.add("Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight());
				}
				
				try {
					MyChartToFileUtils.plot(
							speedListWeightParameterization_TAS_SI, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_TAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_CAS_SI, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_CAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_TAS_Imperial, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_TAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_CAS_Imperial, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_CAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListWeightParameterization, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_Mach",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.CRUISE_GRID_CHART)) {
				
				List<Double[]> specificRange = new ArrayList<>();
				List<Double[]> mach = new ArrayList<>();
				List<String> legend = new ArrayList<>();
				
				for(int i=0; i<_specificRangeMap.get(xcg).size(); i++) {
					specificRange.add(_specificRangeMap.get(xcg).get(i).getSpecificRange());
					mach.add(_specificRangeMap.get(xcg).get(i).getMach());
					legend.add("Mass = " + _specificRangeMap.get(xcg).get(i).getWeight()/9.81);
				}
				
				try {
					SpecificRangeCalc.createSpecificRangeChart(
							specificRange,
							mach,
							legend,
							cruiseFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	//............................................................................
	// END OF THE CRUISE INNER CLASS
	//............................................................................
	
	//............................................................................
	// DESCENT INNER CLASS
	//............................................................................
	public class CalcDescent {
		
		public void calculateDescentPerformance(
				Amount<Length> initialDescentAltitude,
				Amount<Length> endDescentAltitude,
				Amount<Mass> initialDescentMass,
				Double xcg
				) {
			
			_theDescentCalculatorMap.put(
					xcg, 
					new DescentCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent(),
							initialDescentAltitude,
							endDescentAltitude,
							initialDescentMass,
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg)
							)
					);
					
			_theDescentCalculatorMap.get(xcg).calculateDescentPerformance();
			
			_descentLengthsMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentLengths());
			_descentTimesMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentTimes());
			_descentAnglesMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentAngles());
			_totalDescentLengthMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentLength());
			_totalDescentTimeMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentTime());
			_totalDescentFuelUsedMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentFuelUsed());
		}
		
		public void plotDescentPerformance(String descentFolderPath, Double xcg) {
			
			_theDescentCalculatorMap.get(xcg).plotDescentPerformance(descentFolderPath);
			
		}
		
	}
	//............................................................................
	// END OF THE DESCENT INNER CLASS
	//............................................................................
	
	//............................................................................
	// LANDING INNER CLASS
	//............................................................................
	public class CalcLanding {

		public void performLandingSimulation(Amount<Mass> landingMass, Double xcg) {

			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theLandingCalculatorMap.put(
					xcg, 
					new LandingCalc(
							_thePerformanceInterface.getTheAircraft(), 
							_thePerformanceInterface.getTheOperatingConditions(),
							landingMass,
							_thePerformanceInterface.getKApproach(),
							_thePerformanceInterface.getKFlare(),
							_thePerformanceInterface.getKTouchDown(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							wingToGroundDistance,
							_thePerformanceInterface.getObstacleLanding(), 
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE),
							_thePerformanceInterface.getThetaApproach(),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getCLAlphaLanding().get(xcg).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleGroundIdleLanding(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg)
							)
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theLandingCalculatorMap.get(xcg).calculateLandingDistance();
			
			// Distances:
			_groundRollDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsGround());
			_flareDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsFlare());
			_airborneDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsApproach());
			_landingDistanceMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsTotal());
			_landingDistanceFAR25Map.put(xcg, _theLandingCalculatorMap.get(xcg).getsTotal().divide(0.6));
			
			// Velocities:
			_vStallLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvSLanding());
			_vTouchDownMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvTD());
			_vFlareMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvFlare());
			_vApproachMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvA());
			
			// Duration:
			_landingDurationMap.put(
					xcg, 
					_theLandingCalculatorMap.get(xcg).getTime().get(_theLandingCalculatorMap.get(xcg).getTime().size()-1)
					);
			
		}
		public void plotLandingPerformance(String landingFolderPath, Double xcg) {
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.LANDING_SIMULATIONS)) {
				try {
					_theLandingCalculatorMap.get(xcg).createLandingCharts(landingFolderPath);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//............................................................................
	// END OF THE LANDING INNER CLASS
	//............................................................................

	//............................................................................
	// PAYLOAD-RANGE INNER CLASS
	//............................................................................
	public class CalcPayloadRange {

		public void fromMissionProfile(Double xcg) {

			_thePayloadRangeCalculatorMap.put(
					xcg, 
					new PayloadRangeCalcMissionProfile(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getTakeOffMissionAltitude(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getOperatingEmptyMass(),
							_thePerformanceInterface.getMaximumFuelMass(),
							_thePerformanceInterface.getSinglePassengerMass(),
							_thePerformanceInterface.getFirstGuessCruiseLength(),
							_thePerformanceInterface.getCalculateSFCCruise(),
							_thePerformanceInterface.getCalculateSFCAlternateCruise(),
							_thePerformanceInterface.getCalculateSFCHolding(),
							_thePerformanceInterface.getSfcFunctionCruise(),
							_thePerformanceInterface.getSfcFunctionAlternateCruise(),
							_thePerformanceInterface.getSfcFunctionHolding(),
							_thePerformanceInterface.getAlternateCruiseLength(),
							_thePerformanceInterface.getAlternateCruiseAltitude(),
							_thePerformanceInterface.getHoldingDuration(),
							_thePerformanceInterface.getHoldingAltitude(),
							_thePerformanceInterface.getHoldingMachNumber(),
							_thePerformanceInterface.getLandingFuelFlow(),
							_thePerformanceInterface.getFuelReserve(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg),
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							_thePerformanceInterface.getDtRotation(),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getObstacleTakeOff(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getKCLmax(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getObstacleLanding(),
							_thePerformanceInterface.getThetaApproach(),
							_thePerformanceInterface.getKApproach(),
							_thePerformanceInterface.getKFlare(),
							_thePerformanceInterface.getKTouchDown(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent()
							)
					);
			
			//------------------------------------------------------------
			// CRUISE MACH AND ALTITUDE
			_thePayloadRangeCalculatorMap.get(xcg).createPayloadRange();
			
			_rangeAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload());
			_rangeAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload());
			_rangeAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel());	
			_rangeAtZeroPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload());
			_takeOffMassAtZeroPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getTakeOffMassZeroPayload());
			_maxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getMaxPayload());
			_designPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getDesignPayload());
			_payloadAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPayloadAtMaxFuel());
			_passengersNumberAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtMaxPayload());
			_passengersNumberAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtDesignPayload());
			_passengersNumberAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtMaxFuel());
			_requiredMassAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRequiredMassAtMaxPayload());
			_requiredMassAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRequiredMassAtDesignPayload());
			
			_rangeArrayMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeArray());
			_payloadArrayMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPayloadArray());
			
		}
		public void plotPayloadRange(String payloadRangeFolderPath, Double xcg) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.PAYLOAD_RANGE)) {
				if(_rangeAtMaxPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtDesignPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtMaxFuelMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtZeroPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						)
					_thePayloadRangeCalculatorMap.get(xcg).createPayloadRangeChart(payloadRangeFolderPath);
				else
					System.err.println("WARNING: (PLOT - PAYLOAD-RANGE) UNABLE TO PLOT THE PAYLOAD-RANGE DIAGRAM");
			}
			
		}
	}
	//............................................................................
	// END OF THE PAYLOAD-RANGE INNER CLASS
	//............................................................................
	
	//............................................................................
	// FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................
	public class CalcFlightManeuveringAndGustEnvelope {
		
		public void fromRegulations(Double xcg) {
			
			_theEnvelopeCalculatorMap.put(
					xcg, 
					new FlightManeuveringEnvelopeCalc(
							_thePerformanceInterface.getTheAircraft().getRegulations(),
							_thePerformanceInterface.getTheAircraft().getTypeVehicle(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance(),
							_thePerformanceInterface.getCLmaxClean().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLmaxInverted(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPositiveLimitLoadFactor(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getNegativeLimitLoadFactor(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVMaxCruise(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVDive(),
							_thePerformanceInterface.getCLAlphaClean().get(xcg),
							_thePerformanceInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
							_thePerformanceInterface.getTheAircraft().getWing().getSurface()
							)
					);
			
			_theEnvelopeCalculatorMap.get(xcg).calculateManeuveringEnvelope();
			
			_stallSpeedCleanMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean());
			_stallSpeedFullFlapMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap());
			_stallSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted());
			_maneuveringSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed());
			_maneuveringFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed());
			_maneuveringSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted());
			_designFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed());
			_positiveLoadFactorManeuveringSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeed());
			_positiveLoadFactorCruisingSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeed());
			_positiveLoadFactorDiveSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeed());
			_positiveLoadFactorDesignFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed());
			_negativeLoadFactorManeuveringSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInverted());
			_negativeLoadFactorCruisingSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeed());
			_negativeLoadFactorDiveSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeed());
			_positiveLoadFactorManeuveringSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeedWithGust());
			_positiveLoadFactorCruisingSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeedWithGust());
			_positiveLoadFactorDiveSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeedWithGust());
			_positiveLoadFactorDesignFlapSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust());
			_negativeLoadFactorManeuveringSpeedInvertedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInvertedWithGust());
			_negativeLoadFactorCruisingSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeedWithGust());
			_negativeLoadFactorDiveSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeedWithGust());
			_negativeLoadFactorDesignFlapSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDesignFlapSpeedWithGust());
		
		}
		public void plotVnDiagram(String maneuveringEnvelopeFolderPath, Double xcg) {	
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM)) 
				_theEnvelopeCalculatorMap.get(xcg).plotManeuveringEnvelope(maneuveringEnvelopeFolderPath);;
			
		}
		
	}
	//............................................................................
	// END OF THE FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................	
	
	//............................................................................
	// MISSION PROFILE INNER CLASS
	//............................................................................
	public class CalcMissionProfile {

		public void calculateMissionProfileIterative(Double xcg) {

			_theMissionProfileCalculatorMap.put(
					xcg, 
					new MissionProfileCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getOperatingEmptyMass(),
							_thePerformanceInterface.getSinglePassengerMass(),
							_thePerformanceInterface.getTheAircraft().getCabinConfiguration().getNPax(),
							_thePerformanceInterface.getFirstGuessInitialMissionFuelMass(),
							_thePerformanceInterface.getMissionRange(),
							_thePerformanceInterface.getTakeOffMissionAltitude(),
							_thePerformanceInterface.getFirstGuessCruiseLength(),
							_thePerformanceInterface.getCalculateSFCCruise(),
							_thePerformanceInterface.getCalculateSFCAlternateCruise(),
							_thePerformanceInterface.getCalculateSFCHolding(),
							_thePerformanceInterface.getSfcFunctionCruise(),
							_thePerformanceInterface.getSfcFunctionAlternateCruise(),
							_thePerformanceInterface.getSfcFunctionHolding(),
							_thePerformanceInterface.getAlternateCruiseLength(),
							_thePerformanceInterface.getAlternateCruiseAltitude(),
							_thePerformanceInterface.getHoldingDuration(),
							_thePerformanceInterface.getHoldingAltitude(),
							_thePerformanceInterface.getHoldingMachNumber(),
							_thePerformanceInterface.getLandingFuelFlow(),
							_thePerformanceInterface.getFuelReserve(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg),
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							_thePerformanceInterface.getDtRotation(),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getObstacleTakeOff(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getKCLmax(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getObstacleLanding(),
							_thePerformanceInterface.getThetaApproach(),
							_thePerformanceInterface.getKApproach(),
							_thePerformanceInterface.getKFlare(),
							_thePerformanceInterface.getKTouchDown(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent()
							)
					);
				
			_theMissionProfileCalculatorMap.get(xcg).calculateProfiles();
			
			_altitudeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getAltitudeList());
			_rangeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getRangeList());
			_timeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTimeList());
			_fuelUsedListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getFuelUsedList());
			_massListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getMassList());
			_speedTASMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getSpeedTASMissionList());
			_machMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getMachMissionList());
			_liftingCoefficientMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getLiftingCoefficientMissionList());
			_dragCoefficientMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getDragCoefficientMissionList());
			_efficiencyMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getEfficiencyMissionList());
			_thrustMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getThrustMissionList());
			_dragMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getDragMissionList());
			
			_initialFuelMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getInitialFuelMass());
			_totalFuelUsedMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTotalFuelUsed());
			_totalMissionTimeMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTotalMissionTime());
			_initialMissionMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getInitialMissionMass());
			_endMissionMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getEndMissionMass());
			
		}
		
		public void plotProfiles(String missionProfilesFolderPath, Double xcg) {
			
			if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE))
				_theMissionProfileCalculatorMap.get(xcg).plotProfiles(
						_thePerformanceInterface.getPlotList(),
						missionProfilesFolderPath
						);
			else
				System.err.println("WARNING: (PLOT - MISSION PROFILE) UNABLE TO GENERATE MISSION PROFILE CHARTS");
			
		}
		
	}
	//............................................................................
	// END OF THE MISSION PROFILE INNER CLASS
	//............................................................................	

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	
	public IACPerformanceManager getThePerformanceInterface() {
		return _thePerformanceInterface;
	}

	public void setThePerformanceInterface(IACPerformanceManager _thePerformanceInterface) {
		this._thePerformanceInterface = _thePerformanceInterface;
	}

	public Map<Double, TakeOffCalc> getTheTakeOffCalculatorMap() {
		return _theTakeOffCalculatorMap;
	}

	public void setTheTakeOffCalculatorMap(Map<Double, TakeOffCalc> _theTakeOffCalculatorMap) {
		this._theTakeOffCalculatorMap = _theTakeOffCalculatorMap;
	}

	public Map<Double, Amount<Length>> getTakeOffDistanceAEOMap() {
		return _takeOffDistanceAEOMap;
	}

	public void setTakeOffDistanceAEOMap(Map<Double, Amount<Length>> _takeOffDistanceAEOMap) {
		this._takeOffDistanceAEOMap = _takeOffDistanceAEOMap;
	}

	public Map<Double, Amount<Length>> getTakeOffDistanceFAR25Map() {
		return _takeOffDistanceFAR25Map;
	}

	public void setTakeOffDistanceFAR25Map(Map<Double, Amount<Length>> _takeOffDistanceFAR25Map) {
		this._takeOffDistanceFAR25Map = _takeOffDistanceFAR25Map;
	}

	public Map<Double, Amount<Length>> getBalancedFieldLengthMap() {
		return _balancedFieldLengthMap;
	}

	public void setBalancedFieldLengthMap(Map<Double, Amount<Length>> _balancedFieldLengthMap) {
		this._balancedFieldLengthMap = _balancedFieldLengthMap;
	}

	public Map<Double, Amount<Length>> getGroundRollDistanceTakeOffMap() {
		return _groundRollDistanceTakeOffMap;
	}

	public void setGroundRollDistanceTakeOffMap(Map<Double, Amount<Length>> _groundRollDistanceTakeOffMap) {
		this._groundRollDistanceTakeOffMap = _groundRollDistanceTakeOffMap;
	}

	public Map<Double, Amount<Length>> getRotationDistanceTakeOffMap() {
		return _rotationDistanceTakeOffMap;
	}

	public void setRotationDistanceTakeOffMap(Map<Double, Amount<Length>> _rotationDistanceTakeOffMap) {
		this._rotationDistanceTakeOffMap = _rotationDistanceTakeOffMap;
	}

	public Map<Double, Amount<Length>> getAirborneDistanceTakeOffMap() {
		return _airborneDistanceTakeOffMap;
	}

	public void setAirborneDistanceTakeOffMap(Map<Double, Amount<Length>> _airborneDistanceTakeOffMap) {
		this._airborneDistanceTakeOffMap = _airborneDistanceTakeOffMap;
	}

	public Map<Double, Amount<Velocity>> getVStallTakeOffMap() {
		return _vStallTakeOffMap;
	}

	public void setVStallTakeOffMap(Map<Double, Amount<Velocity>> _vStallTakeOffMap) {
		this._vStallTakeOffMap = _vStallTakeOffMap;
	}

	public Map<Double, Amount<Velocity>> getVMCMap() {
		return _vMCMap;
	}

	public void setVMCMap(Map<Double, Amount<Velocity>> _vMCMap) {
		this._vMCMap = _vMCMap;
	}

	public Map<Double, Amount<Velocity>> getVRotationMap() {
		return _vRotationMap;
	}

	public void setVRotationMap(Map<Double, Amount<Velocity>> _vRotationMap) {
		this._vRotationMap = _vRotationMap;
	}

	public Map<Double, Amount<Velocity>> getVLiftOffMap() {
		return _vLiftOffMap;
	}

	public void setVLiftOffMap(Map<Double, Amount<Velocity>> _vLiftOffMap) {
		this._vLiftOffMap = _vLiftOffMap;
	}

	public Map<Double, Amount<Velocity>> getV1Map() {
		return _v1Map;
	}

	public void setV1Map(Map<Double, Amount<Velocity>> _v1Map) {
		this._v1Map = _v1Map;
	}

	public Map<Double, Amount<Velocity>> getV2Map() {
		return _v2Map;
	}

	public void setV2Map(Map<Double, Amount<Velocity>> _v2Map) {
		this._v2Map = _v2Map;
	}

	public Map<Double, Amount<Duration>> getTakeOffDurationMap() {
		return _takeOffDurationMap;
	}

	public void setTakeOffDurationMap(Map<Double, Amount<Duration>> _takeOffDurationMap) {
		this._takeOffDurationMap = _takeOffDurationMap;
	}

	public Map<Double, double[]> getThrustMomentOEIMap() {
		return _thrustMomentOEIMap;
	}

	public void setThrustMomentOEIMap(Map<Double, double[]> _thrustMomentOEIMap) {
		this._thrustMomentOEIMap = _thrustMomentOEIMap;
	}

	public Map<Double, double[]> getYawingMomentOEIMap() {
		return _yawingMomentOEIMap;
	}

	public void setYawingMomentOEIMap(Map<Double, double[]> _yawingMomentOEIMap) {
		this._yawingMomentOEIMap = _yawingMomentOEIMap;
	}

	public Map<Double, ClimbCalc> getTheClimbCalculatorMap() {
		return _theClimbCalculatorMap;
	}

	public void setTheClimbCalculatorMap(Map<Double, ClimbCalc> _theClimbCalculatorMap) {
		this._theClimbCalculatorMap = _theClimbCalculatorMap;
	}

	public Map<Double, List<RCMap>> getRcAEOMap() {
		return _rcAEOMap;
	}

	public void setRcAEOMap(Map<Double, List<RCMap>> _rcMapAEOMap) {
		this._rcAEOMap = _rcMapAEOMap;
	}

	public Map<Double, List<RCMap>> getRcOEIMap() {
		return _rcOEIMap;
	}

	public void setRcOEIMap(Map<Double, List<RCMap>> _rcMapOEIMap) {
		this._rcOEIMap = _rcMapOEIMap;
	}

	public Map<Double, CeilingMap> getCeilingAEOMap() {
		return _ceilingAEOMap;
	}

	public void setCeilingAEOMap(Map<Double, CeilingMap> _ceilingMapAEOMap) {
		this._ceilingAEOMap = _ceilingMapAEOMap;
	}

	public Map<Double, CeilingMap> getCeilingOEIMap() {
		return _ceilingOEIMap;
	}

	public void setCeilingOEIMap(Map<Double, CeilingMap> _ceilingMapOEIMap) {
		this._ceilingOEIMap = _ceilingMapOEIMap;
	}

	public Map<Double, List<DragMap>> getDragListAEOMap() {
		return _dragListAEOMap;
	}

	public void setDragListAEOMap(Map<Double, List<DragMap>> _dragListAEOMap) {
		this._dragListAEOMap = _dragListAEOMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListAEOMap() {
		return _thrustListAEOMap;
	}

	public void setThrustListAEOMap(Map<Double, List<ThrustMap>> _thrustListAEOMap) {
		this._thrustListAEOMap = _thrustListAEOMap;
	}

	public Map<Double, List<DragMap>> getDragListOEIMap() {
		return _dragListOEIMap;
	}

	public void setDragListOEIMap(Map<Double, List<DragMap>> _dragListOEIMap) {
		this._dragListOEIMap = _dragListOEIMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListOEIMap() {
		return _thrustListOEIMap;
	}

	public void setThrustListOEIMap(Map<Double, List<ThrustMap>> _thrustListOEIMap) {
		this._thrustListOEIMap = _thrustListOEIMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeAEOMap() {
		return _efficiencyAltitudeAEOMap;
	}

	public void setEfficiencyAltitudeAEOMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeAEOMap) {
		this._efficiencyAltitudeAEOMap = _efficiencyMapAltitudeAEOMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeOEIMap() {
		return _efficiencyAltitudeOEIMap;
	}

	public void setEfficiencyAltitudeOEIMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeOEIMap) {
		this._efficiencyAltitudeOEIMap = _efficiencyMapAltitudeOEIMap;
	}

	public Map<Double, Amount<Length>> getAbsoluteCeilingAEOMap() {
		return _absoluteCeilingAEOMap;
	}

	public void setAbsoluteCeilingAEOMap(Map<Double, Amount<Length>> _absoluteCeilingAEOMap) {
		this._absoluteCeilingAEOMap = _absoluteCeilingAEOMap;
	}

	public Map<Double, Amount<Length>> getServiceCeilingAEOMap() {
		return _serviceCeilingAEOMap;
	}

	public void setServiceCeilingAEOMap(Map<Double, Amount<Length>> _serviceCeilingAEOMap) {
		this._serviceCeilingAEOMap = _serviceCeilingAEOMap;
	}

	public Map<Double, Amount<Duration>> getMinimumClimbTimeAEOMap() {
		return _minimumClimbTimeAEOMap;
	}

	public void setMinimumClimbTimeAEOMap(Map<Double, Amount<Duration>> _minimumClimbTimeAEOMap) {
		this._minimumClimbTimeAEOMap = _minimumClimbTimeAEOMap;
	}

	public Map<Double, Amount<Duration>> getClimbTimeAtSpecificClimbSpeedAEOMap() {
		return _climbTimeAtSpecificClimbSpeedAEOMap;
	}

	public void setClimbTimeAtSpecificClimbSpeedAEOMap(
			Map<Double, Amount<Duration>> _climbTimeAtSpecificClimbSpeedAEOMap) {
		this._climbTimeAtSpecificClimbSpeedAEOMap = _climbTimeAtSpecificClimbSpeedAEOMap;
	}

	public Map<Double, Amount<Mass>> getFuelUsedDuringClimbMap() {
		return _fuelUsedDuringClimbMap;
	}

	public void setFuelUsedDuringClimbMap(Map<Double, Amount<Mass>> _fuelUsedDuringClimbMap) {
		this._fuelUsedDuringClimbMap = _fuelUsedDuringClimbMap;
	}

	public Map<Double, Amount<Length>> getAbsoluteCeilingOEIMap() {
		return _absoluteCeilingOEIMap;
	}

	public void setAbsoluteCeilingOEIMap(Map<Double, Amount<Length>> _absoluteCeilingOEIMap) {
		this._absoluteCeilingOEIMap = _absoluteCeilingOEIMap;
	}

	public Map<Double, Amount<Length>> getServiceCeilingOEIMap() {
		return _serviceCeilingOEIMap;
	}

	public void setServiceCeilingOEIMap(Map<Double, Amount<Length>> _serviceCeilingOEIMap) {
		this._serviceCeilingOEIMap = _serviceCeilingOEIMap;
	}

	public Map<Double, List<DragMap>> getDragListAltitudeParameterizationMap() {
		return _dragListAltitudeParameterizationMap;
	}

	public void setDragListAltitudeParameterizationMap(Map<Double, List<DragMap>> _dragListAltitudeParameterizationMap) {
		this._dragListAltitudeParameterizationMap = _dragListAltitudeParameterizationMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListAltitudeParameterizationMap() {
		return _thrustListAltitudeParameterizationMap;
	}

	public void setThrustListAltitudeParameterizationMap(
			Map<Double, List<ThrustMap>> _thrustListAltitudeParameterizationMap) {
		this._thrustListAltitudeParameterizationMap = _thrustListAltitudeParameterizationMap;
	}

	public Map<Double, List<DragMap>> getDragListWeightParameterizationMap() {
		return _dragListWeightParameterizationMap;
	}

	public void setDragListWeightParameterizationMap(Map<Double, List<DragMap>> _dragListWeightParameterizationMap) {
		this._dragListWeightParameterizationMap = _dragListWeightParameterizationMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListWeightParameterizationMap() {
		return _thrustListWeightParameterizationMap;
	}

	public void setThrustListWeightParameterizationMap(Map<Double, List<ThrustMap>> _thrustListWeightParameterizationMap) {
		this._thrustListWeightParameterizationMap = _thrustListWeightParameterizationMap;
	}

	public Map<Double, List<Amount<Force>>> getWeightListCruiseMap() {
		return _weightListCruiseMap;
	}

	public void setWeightListCruiseMap(Map<Double, List<Amount<Force>>> _weightListCruiseMap) {
		this._weightListCruiseMap = _weightListCruiseMap;
	}

	public Map<Double, List<DragThrustIntersectionMap>> getIntersectionListMap() {
		return _intersectionListMap;
	}

	public void setIntersectionListMap(Map<Double, List<DragThrustIntersectionMap>> _intersectionListMap) {
		this._intersectionListMap = _intersectionListMap;
	}

	public Map<Double, List<FlightEnvelopeMap>> getCruiseEnvelopeListMap() {
		return _cruiseEnvelopeListMap;
	}

	public void setCruiseEnvelopeListMap(Map<Double, List<FlightEnvelopeMap>> _cruiseEnvelopeListMap) {
		this._cruiseEnvelopeListMap = _cruiseEnvelopeListMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeMap() {
		return _efficiencyAltitudeMap;
	}

	public void setEfficiencyAltitudeMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeMap) {
		this._efficiencyAltitudeMap = _efficiencyMapAltitudeMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyWeightMap() {
		return _efficiencyWeightMap;
	}

	public void setEfficiencyWeightMap(Map<Double, Map<String, List<Double>>> _efficiencyMapWeightMap) {
		this._efficiencyWeightMap = _efficiencyMapWeightMap;
	}

	public Map<Double, List<SpecificRangeMap>> getSpecificRangeMap() {
		return _specificRangeMap;
	}

	public void setSpecificRangeMap(Map<Double, List<SpecificRangeMap>> _specificRangeMapMap) {
		this._specificRangeMap = _specificRangeMapMap;
	}

	public Map<Double, Amount<Velocity>> getMaxSpeesTASAtCruiseAltitudeMap() {
		return _maxSpeesTASAtCruiseAltitudeMap;
	}

	public void setMaxSpeesTASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _maxSpeesTASAtCruiseAltitudeMap) {
		this._maxSpeesTASAtCruiseAltitudeMap = _maxSpeesTASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMinSpeesTASAtCruiseAltitudeMap() {
		return _minSpeesTASAtCruiseAltitudeMap;
	}

	public void setMinSpeesTASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _minSpeesTASAtCruiseAltitudeMap) {
		this._minSpeesTASAtCruiseAltitudeMap = _minSpeesTASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMaxSpeesCASAtCruiseAltitudeMap() {
		return _maxSpeesCASAtCruiseAltitudeMap;
	}

	public void setMaxSpeesCASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _maxSpeesCASAtCruiseAltitudeMap) {
		this._maxSpeesCASAtCruiseAltitudeMap = _maxSpeesCASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMinSpeesCASAtCruiseAltitudeMap() {
		return _minSpeesCASAtCruiseAltitudeMap;
	}

	public void setMinSpeesCASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _minSpeesCASAtCruiseAltitudeMap) {
		this._minSpeesCASAtCruiseAltitudeMap = _minSpeesCASAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getMaxMachAtCruiseAltitudeMap() {
		return _maxMachAtCruiseAltitudeMap;
	}

	public void setMaxMachAtCruiseAltitudeMap(Map<Double, Double> _maxMachAtCruiseAltitudeMap) {
		this._maxMachAtCruiseAltitudeMap = _maxMachAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getMinMachAtCruiseAltitudeMap() {
		return _minMachAtCruiseAltitudeMap;
	}

	public void setMinMachAtCruiseAltitudeMap(Map<Double, Double> _minMachAtCruiseAltitudeMap) {
		this._minMachAtCruiseAltitudeMap = _minMachAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getEfficiencyAtCruiseAltitudeAndMachMap() {
		return _efficiencyAtCruiseAltitudeAndMachMap;
	}

	public void setEfficiencyAtCruiseAltitudeAndMachMap(Map<Double, Double> _efficiencyAtCruiseAltitudeAndMachMap) {
		this._efficiencyAtCruiseAltitudeAndMachMap = _efficiencyAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Force>> getThrustAtCruiseAltitudeAndMachMap() {
		return _thrustAtCruiseAltitudeAndMachMap;
	}

	public void setThrustAtCruiseAltitudeAndMachMap(Map<Double, Amount<Force>> _thrustAtCruiseAltitudeAndMachMap) {
		this._thrustAtCruiseAltitudeAndMachMap = _thrustAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Force>> getDragAtCruiseAltitudeAndMachMap() {
		return _dragAtCruiseAltitudeAndMachMap;
	}

	public void setDragAtCruiseAltitudeAndMachMap(Map<Double, Amount<Force>> _dragAtCruiseAltitudeAndMachMap) {
		this._dragAtCruiseAltitudeAndMachMap = _dragAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Power>> getPowerAvailableAtCruiseAltitudeAndMachMap() {
		return _powerAvailableAtCruiseAltitudeAndMachMap;
	}

	public void setPowerAvailableAtCruiseAltitudeAndMachMap(
			Map<Double, Amount<Power>> _powerAvailableAtCruiseAltitudeAndMachMap) {
		this._powerAvailableAtCruiseAltitudeAndMachMap = _powerAvailableAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Power>> getPowerNeededAtCruiseAltitudeAndMachMap() {
		return _powerNeededAtCruiseAltitudeAndMachMap;
	}

	public void setPowerNeededAtCruiseAltitudeAndMachMap(
			Map<Double, Amount<Power>> _powerNeededAtCruiseAltitudeAndMachMap) {
		this._powerNeededAtCruiseAltitudeAndMachMap = _powerNeededAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, DescentCalc> getTheDescentCalculatorMap() {
		return _theDescentCalculatorMap;
	}

	public void setTheDescentCalculatorMap(Map<Double, DescentCalc> _theDescentCalculatorMap) {
		this._theDescentCalculatorMap = _theDescentCalculatorMap;
	}

	public Map<Double, List<Amount<Length>>> getDescentLengthsMap() {
		return _descentLengthsMap;
	}

	public void setDescentLengthsMap(Map<Double, List<Amount<Length>>> _descentLengthsMap) {
		this._descentLengthsMap = _descentLengthsMap;
	}

	public Map<Double, List<Amount<Duration>>> getDescentTimesMap() {
		return _descentTimesMap;
	}

	public void setDescentTimesMap(Map<Double, List<Amount<Duration>>> _descentTimesMap) {
		this._descentTimesMap = _descentTimesMap;
	}

	public Map<Double, List<Amount<Angle>>> getDescentAnglesMap() {
		return _descentAnglesMap;
	}

	public void setDescentAnglesMap(Map<Double, List<Amount<Angle>>> _descentAnglesMap) {
		this._descentAnglesMap = _descentAnglesMap;
	}

	public Map<Double, Amount<Length>> getTotalDescentLengthMap() {
		return _totalDescentLengthMap;
	}

	public void setTotalDescentLengthMap(Map<Double, Amount<Length>> _totalDescentLengthMap) {
		this._totalDescentLengthMap = _totalDescentLengthMap;
	}

	public Map<Double, Amount<Duration>> getTotalDescentTimeMap() {
		return _totalDescentTimeMap;
	}

	public void setTotalDescentTimeMap(Map<Double, Amount<Duration>> _totalDescentTimeMap) {
		this._totalDescentTimeMap = _totalDescentTimeMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentFuelUsedMap() {
		return _totalDescentFuelUsedMap;
	}

	public void setTotalDescentFuelUsedMap(Map<Double, Amount<Mass>> _totalDescentFuelUsedMap) {
		this._totalDescentFuelUsedMap = _totalDescentFuelUsedMap;
	}

	public Map<Double, LandingCalc> getTheLandingCalculatorMap() {
		return _theLandingCalculatorMap;
	}

	public void setTheLandingCalculatorMap(Map<Double, LandingCalc> _theLandingCalculatorMap) {
		this._theLandingCalculatorMap = _theLandingCalculatorMap;
	}

	public Map<Double, Amount<Length>> getLandingDistanceMap() {
		return _landingDistanceMap;
	}

	public void setLandingDistanceMap(Map<Double, Amount<Length>> _landingDistanceMap) {
		this._landingDistanceMap = _landingDistanceMap;
	}

	public Map<Double, Amount<Length>> getLandingDistanceFAR25Map() {
		return _landingDistanceFAR25Map;
	}

	public void setLandingDistanceFAR25Map(Map<Double, Amount<Length>> _landingDistanceFAR25Map) {
		this._landingDistanceFAR25Map = _landingDistanceFAR25Map;
	}

	public Map<Double, Amount<Length>> getGroundRollDistanceLandingMap() {
		return _groundRollDistanceLandingMap;
	}

	public void setGroundRollDistanceLandingMap(Map<Double, Amount<Length>> _groundRollDistanceLandingMap) {
		this._groundRollDistanceLandingMap = _groundRollDistanceLandingMap;
	}

	public Map<Double, Amount<Length>> getFlareDistanceLandingMap() {
		return _flareDistanceLandingMap;
	}

	public void setFlareDistanceLandingMap(Map<Double, Amount<Length>> _flareDistanceLandingMap) {
		this._flareDistanceLandingMap = _flareDistanceLandingMap;
	}

	public Map<Double, Amount<Length>> getAirborneDistanceLandingMap() {
		return _airborneDistanceLandingMap;
	}

	public void setAirborneDistanceLandingMap(Map<Double, Amount<Length>> _airborneDistanceLandingMap) {
		this._airborneDistanceLandingMap = _airborneDistanceLandingMap;
	}

	public Map<Double, Amount<Velocity>> getVStallLandingMap() {
		return _vStallLandingMap;
	}

	public void setVStallLandingMap(Map<Double, Amount<Velocity>> _vStallLandingMap) {
		this._vStallLandingMap = _vStallLandingMap;
	}

	public Map<Double, Amount<Velocity>> getVApproachMap() {
		return _vApproachMap;
	}

	public void setVApproachMap(Map<Double, Amount<Velocity>> _vApproachMap) {
		this._vApproachMap = _vApproachMap;
	}

	public Map<Double, Amount<Velocity>> getVFlareMap() {
		return _vFlareMap;
	}

	public void setVFlareMap(Map<Double, Amount<Velocity>> _vFlareMap) {
		this._vFlareMap = _vFlareMap;
	}

	public Map<Double, Amount<Velocity>> getVTouchDownMap() {
		return _vTouchDownMap;
	}

	public void setVTouchDownMap(Map<Double, Amount<Velocity>> _vTouchDownMap) {
		this._vTouchDownMap = _vTouchDownMap;
	}

	public Map<Double, Amount<Duration>> getLandingDurationMap() {
		return _landingDurationMap;
	}

	public void setLandingDurationMap(Map<Double, Amount<Duration>> _landingDurationMap) {
		this._landingDurationMap = _landingDurationMap;
	}

	public Map<Double, PayloadRangeCalcMissionProfile> getThePayloadRangeCalculatorMap() {
		return _thePayloadRangeCalculatorMap;
	}

	public void setThePayloadRangeCalculatorMap(
			Map<Double, PayloadRangeCalcMissionProfile> _thePayloadRangeCalculatorMap) {
		this._thePayloadRangeCalculatorMap = _thePayloadRangeCalculatorMap;
	}

	public Map<Double, Amount<Length>> getRangeAtMaxPayloadMap() {
		return _rangeAtMaxPayloadMap;
	}

	public void setRangeAtMaxPayloadMap(Map<Double, Amount<Length>> _rangeAtMaxPayloadMap) {
		this._rangeAtMaxPayloadMap = _rangeAtMaxPayloadMap;
	}

	public Map<Double, Amount<Length>> getRangeAtDesignPayloadMap() {
		return _rangeAtDesignPayloadMap;
	}

	public void setRangeAtDesignPayloadMap(Map<Double, Amount<Length>> _rangeAtDesignPayloadMap) {
		this._rangeAtDesignPayloadMap = _rangeAtDesignPayloadMap;
	}

	public Map<Double, Amount<Length>> getRangeAtMaxFuelMap() {
		return _rangeAtMaxFuelMap;
	}

	public void setRangeAtMaxFuelMap(Map<Double, Amount<Length>> _rangeAtMaxFuelMap) {
		this._rangeAtMaxFuelMap = _rangeAtMaxFuelMap;
	}

	public Map<Double, Amount<Length>> getRangeAtZeroPayloadMap() {
		return _rangeAtZeroPayloadMap;
	}

	public void setRangeAtZeroPayloadMap(Map<Double, Amount<Length>> _rangeAtZeroPayloadMap) {
		this._rangeAtZeroPayloadMap = _rangeAtZeroPayloadMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffMassAtZeroPayloadMap() {
		return _takeOffMassAtZeroPayloadMap;
	}

	public void setTakeOffMassAtZeroPayloadMap(Map<Double, Amount<Mass>> _takeOffMassAtZeroPayloadMap) {
		this._takeOffMassAtZeroPayloadMap = _takeOffMassAtZeroPayloadMap;
	}

	public Map<Double, Amount<Mass>> getMaxPayloadMap() {
		return _maxPayloadMap;
	}

	public void setMaxPayloadMap(Map<Double, Amount<Mass>> _maxPayloadMap) {
		this._maxPayloadMap = _maxPayloadMap;
	}

	public Map<Double, Amount<Mass>> getDesignPayloadMap() {
		return _designPayloadMap;
	}

	public void setDesignPayloadMap(Map<Double, Amount<Mass>> _designPayloadMap) {
		this._designPayloadMap = _designPayloadMap;
	}

	public Map<Double, Amount<Mass>> getPayloadAtMaxFuelMap() {
		return _payloadAtMaxFuelMap;
	}

	public void setPayloadAtMaxFuelMap(Map<Double, Amount<Mass>> _payloadAtMaxFuelMap) {
		this._payloadAtMaxFuelMap = _payloadAtMaxFuelMap;
	}

	public Map<Double, Integer> getPassengersNumberAtMaxPayloadMap() {
		return _passengersNumberAtMaxPayloadMap;
	}

	public void setPassengersNumberAtMaxPayloadMap(Map<Double, Integer> _passengersNumberAtMaxPayloadMap) {
		this._passengersNumberAtMaxPayloadMap = _passengersNumberAtMaxPayloadMap;
	}

	public Map<Double, Integer> getPassengersNumberAtDesignPayloadMap() {
		return _passengersNumberAtDesignPayloadMap;
	}

	public void setPassengersNumberAtDesignPayloadMap(Map<Double, Integer> _passengersNumberAtDesignPayloadMap) {
		this._passengersNumberAtDesignPayloadMap = _passengersNumberAtDesignPayloadMap;
	}

	public Map<Double, Integer> getPassengersNumberAtMaxFuelMap() {
		return _passengersNumberAtMaxFuelMap;
	}

	public void setPassengersNumberAtMaxFuelMap(Map<Double, Integer> _passengersNumberAtMaxFuelMap) {
		this._passengersNumberAtMaxFuelMap = _passengersNumberAtMaxFuelMap;
	}

	public Map<Double, Amount<Mass>> getRequiredMassAtMaxPayloadMap() {
		return _requiredMassAtMaxPayloadMap;
	}

	public void setRequiredMassAtMaxPayloadMap(Map<Double, Amount<Mass>> _requiredMassAtMaxPayloadMap) {
		this._requiredMassAtMaxPayloadMap = _requiredMassAtMaxPayloadMap;
	}

	public Map<Double, Amount<Mass>> getRequiredMassAtDesignPayloadMap() {
		return _requiredMassAtDesignPayloadMap;
	}

	public void setRequiredMassAtDesignPayloadMap(Map<Double, Amount<Mass>> _requiredMassAtDesignPayloadMap) {
		this._requiredMassAtDesignPayloadMap = _requiredMassAtDesignPayloadMap;
	}

	public Map<Double, List<Amount<Length>>> getRangeArrayMap() {
		return _rangeArrayMap;
	}

	public void setRangeArrayMap(Map<Double, List<Amount<Length>>> _rangeArrayMap) {
		this._rangeArrayMap = _rangeArrayMap;
	}

	public Map<Double, List<Double>> getPayloadArrayMap() {
		return _payloadArrayMap;
	}

	public void setPayloadArrayMap(Map<Double, List<Double>> _payloadArrayMap) {
		this._payloadArrayMap = _payloadArrayMap;
	}

	public Map<Double, double[][]> getRangeMatrixMap() {
		return _rangeMatrixMap;
	}

	public void setRangeMatrixMap(Map<Double, double[][]> _rangeMatrixMap) {
		this._rangeMatrixMap = _rangeMatrixMap;
	}

	public Map<Double, double[][]> getPayloadMatrixMap() {
		return _payloadMatrixMap;
	}

	public void setPayloadMatrixMap(Map<Double, double[][]> _payloadMatrixMap) {
		this._payloadMatrixMap = _payloadMatrixMap;
	}

	public Map<Double, FlightManeuveringEnvelopeCalc> getTheEnvelopeCalculatorMap() {
		return _theEnvelopeCalculatorMap;
	}

	public void setTheEnvelopeCalculatorMap(Map<Double, FlightManeuveringEnvelopeCalc> _theEnvelopeCalculatorMap) {
		this._theEnvelopeCalculatorMap = _theEnvelopeCalculatorMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedFullFlapMap() {
		return _stallSpeedFullFlapMap;
	}

	public void setStallSpeedFullFlapMap(Map<Double, Amount<Velocity>> _stallSpeedFullFlapMap) {
		this._stallSpeedFullFlapMap = _stallSpeedFullFlapMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedCleanMap() {
		return _stallSpeedCleanMap;
	}

	public void setStallSpeedCleanMap(Map<Double, Amount<Velocity>> _stallSpeedCleanMap) {
		this._stallSpeedCleanMap = _stallSpeedCleanMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedInvertedMap() {
		return _stallSpeedInvertedMap;
	}

	public void setStallSpeedInvertedMap(Map<Double, Amount<Velocity>> _stallSpeedInvertedMap) {
		this._stallSpeedInvertedMap = _stallSpeedInvertedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringSpeedMap() {
		return _maneuveringSpeedMap;
	}

	public void setManeuveringSpeedMap(Map<Double, Amount<Velocity>> _maneuveringSpeedMap) {
		this._maneuveringSpeedMap = _maneuveringSpeedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringFlapSpeedMap() {
		return _maneuveringFlapSpeedMap;
	}

	public void setManeuveringFlapSpeedMap(Map<Double, Amount<Velocity>> _maneuveringFlapSpeedMap) {
		this._maneuveringFlapSpeedMap = _maneuveringFlapSpeedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringSpeedInvertedMap() {
		return _maneuveringSpeedInvertedMap;
	}

	public void setManeuveringSpeedInvertedMap(Map<Double, Amount<Velocity>> _maneuveringSpeedInvertedMap) {
		this._maneuveringSpeedInvertedMap = _maneuveringSpeedInvertedMap;
	}

	public Map<Double, Amount<Velocity>> getDesignFlapSpeedMap() {
		return _designFlapSpeedMap;
	}

	public void setDesignFlapSpeedMap(Map<Double, Amount<Velocity>> _designFlapSpeedMap) {
		this._designFlapSpeedMap = _designFlapSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorManeuveringSpeedMap() {
		return _positiveLoadFactorManeuveringSpeedMap;
	}

	public void setPositiveLoadFactorManeuveringSpeedMap(Map<Double, Double> _positiveLoadFactorManeuveringSpeedMap) {
		this._positiveLoadFactorManeuveringSpeedMap = _positiveLoadFactorManeuveringSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorCruisingSpeedMap() {
		return _positiveLoadFactorCruisingSpeedMap;
	}

	public void setPositiveLoadFactorCruisingSpeedMap(Map<Double, Double> _positiveLoadFactorCruisingSpeedMap) {
		this._positiveLoadFactorCruisingSpeedMap = _positiveLoadFactorCruisingSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDiveSpeedMap() {
		return _positiveLoadFactorDiveSpeedMap;
	}

	public void setPositiveLoadFactorDiveSpeedMap(Map<Double, Double> _positiveLoadFactorDiveSpeedMap) {
		this._positiveLoadFactorDiveSpeedMap = _positiveLoadFactorDiveSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDesignFlapSpeedMap() {
		return _positiveLoadFactorDesignFlapSpeedMap;
	}

	public void setPositiveLoadFactorDesignFlapSpeedMap(Map<Double, Double> _positiveLoadFactorDesignFlapSpeedMap) {
		this._positiveLoadFactorDesignFlapSpeedMap = _positiveLoadFactorDesignFlapSpeedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorManeuveringSpeedInvertedMap() {
		return _negativeLoadFactorManeuveringSpeedInvertedMap;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedMap(
			Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedMap) {
		this._negativeLoadFactorManeuveringSpeedInvertedMap = _negativeLoadFactorManeuveringSpeedInvertedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorCruisingSpeedMap() {
		return _negativeLoadFactorCruisingSpeedMap;
	}

	public void setNegativeLoadFactorCruisingSpeedMap(Map<Double, Double> _negativeLoadFactorCruisingSpeedMap) {
		this._negativeLoadFactorCruisingSpeedMap = _negativeLoadFactorCruisingSpeedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDiveSpeedMap() {
		return _negativeLoadFactorDiveSpeedMap;
	}

	public void setNegativeLoadFactorDiveSpeedMap(Map<Double, Double> _negativeLoadFactorDiveSpeedMap) {
		this._negativeLoadFactorDiveSpeedMap = _negativeLoadFactorDiveSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorManeuveringSpeedWithGustMap() {
		return _positiveLoadFactorManeuveringSpeedWithGustMap;
	}

	public void setPositiveLoadFactorManeuveringSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorManeuveringSpeedWithGustMap) {
		this._positiveLoadFactorManeuveringSpeedWithGustMap = _positiveLoadFactorManeuveringSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorCruisingSpeedWithGustMap() {
		return _positiveLoadFactorCruisingSpeedWithGustMap;
	}

	public void setPositiveLoadFactorCruisingSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorCruisingSpeedWithGustMap) {
		this._positiveLoadFactorCruisingSpeedWithGustMap = _positiveLoadFactorCruisingSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDiveSpeedWithGustMap() {
		return _positiveLoadFactorDiveSpeedWithGustMap;
	}

	public void setPositiveLoadFactorDiveSpeedWithGustMap(Map<Double, Double> _positiveLoadFactorDiveSpeedWithGustMap) {
		this._positiveLoadFactorDiveSpeedWithGustMap = _positiveLoadFactorDiveSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDesignFlapSpeedWithGustMap() {
		return _positiveLoadFactorDesignFlapSpeedWithGustMap;
	}

	public void setPositiveLoadFactorDesignFlapSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorDesignFlapSpeedWithGustMap) {
		this._positiveLoadFactorDesignFlapSpeedWithGustMap = _positiveLoadFactorDesignFlapSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorManeuveringSpeedInvertedWithGustMap() {
		return _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGustMap(
			Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedWithGustMap) {
		this._negativeLoadFactorManeuveringSpeedInvertedWithGustMap = _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorCruisingSpeedWithGustMap() {
		return _negativeLoadFactorCruisingSpeedWithGustMap;
	}

	public void setNegativeLoadFactorCruisingSpeedWithGustMap(
			Map<Double, Double> _negativeLoadFactorCruisingSpeedWithGustMap) {
		this._negativeLoadFactorCruisingSpeedWithGustMap = _negativeLoadFactorCruisingSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDiveSpeedWithGustMap() {
		return _negativeLoadFactorDiveSpeedWithGustMap;
	}

	public void setNegativeLoadFactorDiveSpeedWithGustMap(Map<Double, Double> _negativeLoadFactorDiveSpeedWithGustMap) {
		this._negativeLoadFactorDiveSpeedWithGustMap = _negativeLoadFactorDiveSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDesignFlapSpeedWithGustMap() {
		return _negativeLoadFactorDesignFlapSpeedWithGustMap;
	}

	public void setNegativeLoadFactorDesignFlapSpeedWithGustMap(
			Map<Double, Double> _negativeLoadFactorDesignFlapSpeedWithGustMap) {
		this._negativeLoadFactorDesignFlapSpeedWithGustMap = _negativeLoadFactorDesignFlapSpeedWithGustMap;
	}

	public Map<Double, MissionProfileCalc> getTheMissionProfileCalculatorMap() {
		return _theMissionProfileCalculatorMap;
	}

	public void setTheMissionProfileCalculatorMap(Map<Double, MissionProfileCalc> _theMissionProfileCalculatorMap) {
		this._theMissionProfileCalculatorMap = _theMissionProfileCalculatorMap;
	}

	public Map<Double, List<Amount<Length>>> getAltitudeListMap() {
		return _altitudeListMap;
	}

	public void setAltitudeListMap(Map<Double, List<Amount<Length>>> _altitudeListMap) {
		this._altitudeListMap = _altitudeListMap;
	}

	public Map<Double, List<Amount<Length>>> getRangeListMap() {
		return _rangeListMap;
	}

	public void setRangeListMap(Map<Double, List<Amount<Length>>> _rangeListMap) {
		this._rangeListMap = _rangeListMap;
	}

	public Map<Double, List<Amount<Duration>>> getTimeListMap() {
		return _timeListMap;
	}

	public void setTimeListMap(Map<Double, List<Amount<Duration>>> _timeListMap) {
		this._timeListMap = _timeListMap;
	}

	public Map<Double, List<Amount<Mass>>> getFuelUsedListMap() {
		return _fuelUsedListMap;
	}

	public void setFuelUsedListMap(Map<Double, List<Amount<Mass>>> _fuelUsedListMap) {
		this._fuelUsedListMap = _fuelUsedListMap;
	}

	public Map<Double, List<Amount<Mass>>> getMassListMap() {
		return _massListMap;
	}

	public void setMassListMap(Map<Double, List<Amount<Mass>>> _massListMap) {
		this._massListMap = _massListMap;
	}

	public Map<Double, List<Amount<Velocity>>> getSpeedTASMissionListMap() {
		return _speedTASMissionListMap;
	}

	public void setSpeedTASMissionListMap(Map<Double, List<Amount<Velocity>>> _speedTASMissionListMap) {
		this._speedTASMissionListMap = _speedTASMissionListMap;
	}

	public Map<Double, List<Double>> getMachMissionListMap() {
		return _machMissionListMap;
	}

	public void setMachMissionListMap(Map<Double, List<Double>> _machMissionListMap) {
		this._machMissionListMap = _machMissionListMap;
	}

	public Map<Double, List<Double>> getLiftingCoefficientMissionListMap() {
		return _liftingCoefficientMissionListMap;
	}

	public void setLiftingCoefficientMissionListMap(Map<Double, List<Double>> _liftingCoefficientMissionListMap) {
		this._liftingCoefficientMissionListMap = _liftingCoefficientMissionListMap;
	}

	public Map<Double, List<Double>> getDragCoefficientMissionListMap() {
		return _dragCoefficientMissionListMap;
	}

	public void setDragCoefficientMissionListMap(Map<Double, List<Double>> _dragCoefficientMissionListMap) {
		this._dragCoefficientMissionListMap = _dragCoefficientMissionListMap;
	}

	public Map<Double, List<Double>> getEfficiencyMissionListMap() {
		return _efficiencyMissionListMap;
	}

	public void setEfficiencyMissionListMap(Map<Double, List<Double>> _efficiencyMissionListMap) {
		this._efficiencyMissionListMap = _efficiencyMissionListMap;
	}

	public Map<Double, List<Amount<Force>>> getThrustMissionListMap() {
		return _thrustMissionListMap;
	}

	public void setThrustMissionListMap(Map<Double, List<Amount<Force>>> _thrustMissionListMap) {
		this._thrustMissionListMap = _thrustMissionListMap;
	}

	public Map<Double, List<Amount<Force>>> getDragMissionListMap() {
		return _dragMissionListMap;
	}

	public void setDragMissionListMap(Map<Double, List<Amount<Force>>> _dragMissionListMap) {
		this._dragMissionListMap = _dragMissionListMap;
	}

	public Map<Double, Amount<Mass>> getInitialFuelMassMap() {
		return _initialFuelMassMap;
	}

	public void setInitialFuelMassMap(Map<Double, Amount<Mass>> _initialFuelMassMap) {
		this._initialFuelMassMap = _initialFuelMassMap;
	}

	public Map<Double, Amount<Mass>> getTotalFuelUsedMap() {
		return _totalFuelUsedMap;
	}

	public void setTotalFuelUsedMap(Map<Double, Amount<Mass>> _totalFuelUsedMap) {
		this._totalFuelUsedMap = _totalFuelUsedMap;
	}

	public Map<Double, Amount<Duration>> getTotalMissionTimeMap() {
		return _totalMissionTimeMap;
	}

	public void setTotalMissionTimeMap(Map<Double, Amount<Duration>> _totalMissionTimeMap) {
		this._totalMissionTimeMap = _totalMissionTimeMap;
	}

	public Map<Double, Amount<Mass>> getInitialMissionMassMap() {
		return _initialMissionMassMap;
	}

	public void setInitialMissionMassMap(Map<Double, Amount<Mass>> _initialMissionMassMap) {
		this._initialMissionMassMap = _initialMissionMassMap;
	}

	public Map<Double, Amount<Mass>> getEndMissionMassMap() {
		return _endMissionMassMap;
	}

	public void setEndMissionMassMap(Map<Double, Amount<Mass>> _endMissionMassMap) {
		this._endMissionMassMap = _endMissionMassMap;
	}

}
