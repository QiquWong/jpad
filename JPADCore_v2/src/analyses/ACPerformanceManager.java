package analyses;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.junit.Assert;

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
import configuration.enumerations.ComponentEnum;
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
import standaloneutils.MyXLSUtils;
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
	
	/*
	 * TODO: - THE CALCULATE METHOD HAS TO BE UPDATED ALLOWING TO MULTI-CG PERFORMANCE EVAULATION
	 *       - ALL THE OUTPUT HAVE TO BECOME MAPS
	 *       - ELIMINATE THE INITIAL CRUISE RANGE AND INITIAL MISSION FUEL FROM THE INPUT FILE
	 *         AND GIVE THEM DEFAULT VALUE (RANGE -> BREGUET; FUEL -> MAX_FUEL/2)
	 *       - CRATE A FOLDER FOR EACH XCG IN OUTPUT TO STORE FILES AND CHARTS (IN THIS WAY YOU DO
	 *         NOT NEED TO MODIFY THE METHOD THAT CREATE CHARTS AND XLSX)
	 *       - DEBUG ALL!! (IN PARTICULAR DURING MISSION PROFILE ANALYSIS OR PAYLOAD RANGE WHEN THE SECOND CLIMB FREEZES THE EXECUTION)
	 *       
	 *       - IN THE COSTS ANALYSIS, DATA FROM THE PERFORMANCE MODULE WILL HAVE TO BE UPDATED TAKING INTO
	 *         ACCOUNT FOR THE DIFFERENT XCG POSITION 
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
	private TakeOffCalc _theTakeOffCalculator;
	private Amount<Length> _takeOffDistanceAEO;
	private Amount<Length> _takeOffDistanceFAR25;
	private Amount<Length> _balancedFieldLength;
	private Amount<Length> _groundRollDistanceTakeOff;
	private Amount<Length> _rotationDistanceTakeOff;
	private Amount<Length> _airborneDistanceTakeOff;
	private Amount<Velocity> _vStallTakeOff;
	private Amount<Velocity> _vMC;
	private Amount<Velocity> _vRotation;
	private Amount<Velocity> _vLiftOff;
	private Amount<Velocity> _v1;
	private Amount<Velocity> _v2;
	private Amount<Duration> _takeOffDuration;
	private double[] _thrustMomentOEI;;
	private double[] _yawingMomentOEI;
	//..............................................................................
	// Climb
	private ClimbCalc _theClimbCalculator;
	private List<RCMap> _rcMapAEO;
	private List<RCMap> _rcMapOEI;
	private CeilingMap _ceilingMapAEO;
	private CeilingMap _ceilingMapOEI;
	private List<DragMap> _dragListAEO;
	private List<ThrustMap> _thrustListAEO;
	private List<DragMap> _dragListOEI;
	private List<ThrustMap> _thrustListOEI;
	private Map<String, List<Double>> _efficiencyMapAltitudeAEO;
	private Map<String, List<Double>> _efficiencyMapAltitudeOEI;
	
	private Amount<Length> _absoluteCeilingAEO;
	private Amount<Length> _serviceCeilingAEO;
	private Amount<Duration> _minimumClimbTimeAEO;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedAEO;
	private Amount<Mass> _fuelUsedDuringClimb;
	
	private Amount<Length> _absoluteCeilingOEI;
	private Amount<Length> _serviceCeilingOEI;
	//..............................................................................
	// Cruise
	private List<DragMap> _dragListAltitudeParameterization;
	private List<ThrustMap> _thrustListAltitudeParameterization;
	private List<DragMap> _dragListWeightParameterization;
	private List<ThrustMap> _thrustListWeightParameterization;
	private List<Amount<Force>> _weightListCruise;
	
	private List<DragThrustIntersectionMap> _intersectionList;
	private List<FlightEnvelopeMap> _cruiseEnvelopeList;
	
	private Map<String, List<Double>> _efficiencyMapAltitude;
	private Map<String, List<Double>> _efficiencyMapWeight;
	
	private List<SpecificRangeMap> _specificRangeMap;
	
	private Amount<Velocity> _maxSpeesTASAtCruiseAltitude;
	private Amount<Velocity> _minSpeesTASAtCruiseAltitude;
	private Amount<Velocity> _maxSpeesCASAtCruiseAltitude;
	private Amount<Velocity> _minSpeesCASAtCruiseAltitude;
	private Double _maxMachAtCruiseAltitude;
	private Double _minMachAtCruiseAltitude;
	private Double _efficiencyAtCruiseAltitudeAndMach;
	private Amount<Force> _thrustAtCruiseAltitudeAndMach;
	private Amount<Force> _dragAtCruiseAltitudeAndMach;
	private Amount<Power> _powerAvailableAtCruiseAltitudeAndMach;
	private Amount<Power> _powerNeededAtCruiseAltitudeAndMach;
	//..............................................................................
	// Descent
	private DescentCalc _theDescentCalculator;
	private List<Amount<Length>> _descentLengths;
	private List<Amount<Duration>> _descentTimes;
	private List<Amount<Angle>> _descentAngles;
	private Amount<Length> _totalDescentLength;
	private Amount<Duration> _totalDescentTime;
	private Amount<Mass> _totalDescentFuelUsed;
	//..............................................................................
	// Landing
	private LandingCalc _theLandingCalculator;
	private Amount<Length> _landingDistance;
	private Amount<Length> _landingDistanceFAR25;
	private Amount<Length> _groundRollDistanceLanding;
	private Amount<Length> _flareDistanceLanding;
	private Amount<Length> _airborneDistanceLanding;
	private Amount<Velocity> _vStallLanding;
	private Amount<Velocity> _vApproach;
	private Amount<Velocity> _vFlare;
	private Amount<Velocity> _vTouchDown;
	private Amount<Duration> _landingDuration;
	//..............................................................................
	// Payload-Range
	private PayloadRangeCalcMissionProfile _thePayloadRangeCalculator;
	
	private Amount<Length> _rangeAtMaxPayload;
	private Amount<Length> _rangeAtDesignPayload;
	private Amount<Length> _rangeAtMaxFuel;	
	private Amount<Length> _rangeAtZeroPayload;
	private Amount<Mass> _takeOffMassAtZeroPayload;
	private Amount<Mass> _maxPayload;
	private Amount<Mass> _designPayload;
	private Amount<Mass> _payloadAtMaxFuel;
	private Integer _passengersNumberAtMaxPayload;
	private Integer _passengersNumberAtDesignPayload;
	private Integer _passengersNumberAtMaxFuel;
	private Amount<Mass> _requiredMassAtMaxPayload;
	private Amount<Mass> _requiredMassAtDesignPayload;
	
	private List<Amount<Length>> _rangeArray;
	private List<Double> _payloadArray;
	
	private double[][] _rangeMatrix;
	private double[][] _payloadMatrix;
	//..............................................................................
	// Maneuvering and Gust Flight Envelope 
	private FlightManeuveringEnvelopeCalc _theEnvelopeCalculator;
	private Amount<Velocity> _stallSpeedFullFlap;
	private Amount<Velocity> _stallSpeedClean;
	private Amount<Velocity> _stallSpeedInverted;
	private Amount<Velocity> _maneuveringSpeed;
	private Amount<Velocity> _maneuveringFlapSpeed;
	private Amount<Velocity> _maneuveringSpeedInverted;
	private Amount<Velocity> _designFlapSpeed;
	private Double _positiveLoadFactorManeuveringSpeed;
	private Double _positiveLoadFactorCruisingSpeed;
	private Double _positiveLoadFactorDiveSpeed;
	private Double _positiveLoadFactorDesignFlapSpeed;
	private Double _negativeLoadFactorManeuveringSpeedInverted;
	private Double _negativeLoadFactorCruisingSpeed;
	private Double _negativeLoadFactorDiveSpeed;
	private Double _positiveLoadFactorManeuveringSpeedWithGust;
	private Double _positiveLoadFactorCruisingSpeedWithGust;
	private Double _positiveLoadFactorDiveSpeedWithGust;
	private Double _positiveLoadFactorDesignFlapSpeedWithGust;
	private Double _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	private Double _negativeLoadFactorCruisingSpeedWithGust;
	private Double _negativeLoadFactorDiveSpeedWithGust;
	private Double _negativeLoadFactorDesignFlapSpeedWithGust;
	//..............................................................................
	// Mission profile
	private MissionProfileCalc _theMissionProfileCalculator;
	private List<Amount<Length>> _altitudeList;
	private List<Amount<Length>> _rangeList;
	private List<Amount<Duration>> _timeList;
	private List<Amount<Mass>> _fuelUsedList;
	private List<Amount<Mass>> _massList;
	private List<Amount<Velocity>> _speedTASMissionList;
	private List<Double> _machMissionList;
	private List<Double> _liftingCoefficientMissionList;
	private List<Double> _dragCoefficientMissionList;
	private List<Double> _efficiencyMissionList;
	private List<Amount<Force>> _thrustMissionList;
	private List<Amount<Force>> _dragMissionList;
	
	private Amount<Mass> _initialFuelMass;
	private Amount<Mass> _totalFuelUsed;
	private Amount<Duration> _totalMissionTime;
	private Amount<Mass> _initialMissionMass;
	private Amount<Mass> _endMissionMass;
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	@SuppressWarnings({ "resource", "unchecked", "unused" })
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
		Map<Double,Double>oswaldClimb = new HashMap<>();
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

					////////////////////////////////////////////////////////////////////////////
					//																		  //
					// TODO : CHECK THAT ALL THE XCG OF ALL CONDITIONS ARE THE SAME,          //
					//		  OTHERWISE TERMINATE AND GIVE A WARNING    					  //
					//																	      //
					////////////////////////////////////////////////////////////////////////////

					List<Double> xcgTakeOff = new ArrayList<>();
					List<Double> xcgClimb = new ArrayList<>();
					List<Double> xcgCruise = new ArrayList<>();
					List<Double> xcgLanding = new ArrayList<>();

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF) != null) 
						xcgTakeOff = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
						.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getXCGAircraft();
					else 
						System.err.println("WARNING!! THE TAKE-OFF AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");
					
					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB) != null)
						xcgClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
						.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface().getXCGAircraft();
					else 
						System.err.println("WARNING!! THE CLIMB AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE) != null)
						xcgCruise = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
						.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface().getXCGAircraft();
					else 
						System.err.println("WARNING!! THE CRUISE AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");
					
					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING) != null)
						xcgLanding = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
						.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface().getXCGAircraft();
					else 
						System.err.println("WARNING!! THE LANDING AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					
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
					
					//---------------------------------------------------------------------------------------------------------
					// TAKE-OFF 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionTakeOffMap = new HashMap<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						liftCurveFunctionTakeOffMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getAlphaBodyList()
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i))
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
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
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
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
										)
								);

						polarCDTakeOff.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i))
										)
								);
					}

					//---------------------------------------------------------------------------------------------------------
					// CLIMB 
					//---------------------------------------------------------------------------------------------------------
					for(int i=0; i<centerOfGravityList.size(); i++) {

						polarCLClimb.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
										)
								);

						polarCDClimb.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i))
										)
								);
					}
					//---------------------------------------------------------------------------------------------------------
					// CRUISE 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionCruiseMap = new HashMap<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						liftCurveFunctionCruiseMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getAlphaBodyList()
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i))
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
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						polarCLCruise.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
										)
								);

						polarCDCruise.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i))
										)
								);
					}
					//---------------------------------------------------------------------------------------------------------
					// LANDING
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionLandingMap = new HashMap<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						liftCurveFunctionLandingMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getAlphaBodyList()
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i))
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
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
										)
								);

						polarCDLanding.put(
								centerOfGravityList.get(i),
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
										.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i))
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
			List<String> xCGListProperty = reader.getXMLPropertiesByPath("//xcg");
			if(!xCGListProperty.isEmpty())
				xCGListProperty.stream().forEach(xcg -> centerOfGravityList.add(Double.valueOf(xcg)));
			//...............................................................
			// CLmax CLEAN
			List<String> cLmaxCleanProperty = reader.getXMLPropertiesByPath("//cLmax_clean_configuration");
			if(!cLmaxCleanProperty.isEmpty())
				cLmaxCleanProperty.stream().forEach(cLmax -> cLmaxClean.put(
						centerOfGravityList.get(cLmaxCleanProperty.indexOf(cLmax)),
						Double.valueOf(cLmax))
						);
			//...............................................................
			// CLalpha CLEAN
			List<String> cLAlphaCleanProperty = reader.getXMLPropertiesByPath("//cL_alpha_clean_configuration");
			List<Amount<?>> cLAlphaCleanList = new ArrayList<>();
			if(!cLAlphaCleanProperty.isEmpty()) {
				cLAlphaCleanList = reader.readArrayofUnknownAmountFromXML("//cL_alpha_clean_configuration");
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
				cLAlphaTakeOffList = reader.readArrayofUnknownAmountFromXML("//cL_alpha_take_off");
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
				cLAlphaLandingList = reader.readArrayofUnknownAmountFromXML("//cL_alpha_landing");
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
				cLmaxTakeOffProperty.stream().forEach(cLmax -> cLmaxTakeOff.put(
						centerOfGravityList.get(cLmaxTakeOffProperty.indexOf(cLmax)),
						Double.valueOf(cLmax))
						);
			//...............................................................
			// CL0 TAKE-OFF
			List<String> cL0TakeOffProperty = reader.getXMLPropertiesByPath("//cL0_take_off_configuration");
			if(!cL0TakeOffProperty.isEmpty())
				cL0TakeOffProperty.stream().forEach(cL0 -> cLZeroTakeOff.put(
						centerOfGravityList.get(cL0TakeOffProperty.indexOf(cL0)),
						Double.valueOf(cL0))
						);
			//...............................................................
			// CLmax LANDING
			List<String> cLmaxLandingProperty = reader.getXMLPropertiesByPath("//cLmax_landing_configuration");
			if(!cLmaxLandingProperty.isEmpty())
				cLmaxLandingProperty.stream().forEach(cLmax -> cLmaxLanding.put(
						centerOfGravityList.get(cLmaxLandingProperty.indexOf(cLmax)),
						Double.valueOf(cLmax))
						);
			//...............................................................
			// CL0 LANDING
			List<String> cL0LandingProperty = reader.getXMLPropertiesByPath("//cL0_landing_configuration");
			if(!cL0LandingProperty.isEmpty())
				cL0LandingProperty.stream().forEach(cL0 -> cLZeroLanding.put(
						centerOfGravityList.get(cL0LandingProperty.indexOf(cL0)),
						Double.valueOf(cL0))
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
												polarCLCruiseProperty.get(xcg).stream().map(cL -> Double.valueOf(cL)).collect(Collectors.toList())
												)
										);
							if(!polarCDCruiseProperty.get(xcg).isEmpty())
								polarCDCruise.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												polarCDCruiseProperty.get(xcg).stream().map(cD -> Double.valueOf(cD)).collect(Collectors.toList())
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
												polarCLClimbProperty.get(xcg).stream().map(cL -> Double.valueOf(cL)).collect(Collectors.toList())
												)
										);
							if(!polarCDClimbProperty.get(xcg).isEmpty())
								polarCDClimb.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												polarCDClimbProperty.get(xcg).stream().map(cD -> Double.valueOf(cD)).collect(Collectors.toList())
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
												polarCLTakeOffProperty.get(xcg).stream().map(cL -> Double.valueOf(cL)).collect(Collectors.toList())
												)
										);
							if(!polarCDTakeOffProperty.get(xcg).isEmpty())
								polarCDTakeOff.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												polarCDTakeOffProperty.get(xcg).stream().map(cD -> Double.valueOf(cD)).collect(Collectors.toList())
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
												polarCLLandingProperty.get(xcg).stream().map(cL -> Double.valueOf(cL)).collect(Collectors.toList())
												)
										);
							if(!polarCDLandingProperty.get(xcg).isEmpty())
								polarCDLanding.put(
										xcg, 
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												polarCDLandingProperty.get(xcg).stream().map(cD -> Double.valueOf(cD)).collect(Collectors.toList())
												)
										);
						});
				
			}
			else {
				
				//...............................................................
				// CD0
				List<String> cD0Property = reader.getXMLPropertiesByPath("//cD0");
				if(!cD0Property.isEmpty())
					cD0Property.stream().forEach(cD -> cD0.put(
							centerOfGravityList.get(cD0Property.indexOf(cD)),
							Double.valueOf(cD))
							);
				//...............................................................
				// OSWALD CRUISE
				List<String> oswladCruiseProperty = reader.getXMLPropertiesByPath("//oswald_cruise");
				if(!oswladCruiseProperty.isEmpty())
					oswladCruiseProperty.stream().forEach(e -> oswaldCruise.put(
							centerOfGravityList.get(oswladCruiseProperty.indexOf(e)),
							Double.valueOf(e))
							);
				//...............................................................
				// OSWALD CLIMB
				List<String> oswladClimbProperty = reader.getXMLPropertiesByPath("//oswald_climb");
				if(!oswladClimbProperty.isEmpty())
					oswladClimbProperty.stream().forEach(e -> oswaldClimb.put(
							centerOfGravityList.get(oswladClimbProperty.indexOf(e)),
							Double.valueOf(e))
							);
				//...............................................................
				// OSWALD TO
				List<String> oswladTOProperty = reader.getXMLPropertiesByPath("//oswald_take_off");
				if(!oswladTOProperty.isEmpty())
					oswladTOProperty.stream().forEach(e -> oswaldTakeOff.put(
							centerOfGravityList.get(oswladTOProperty.indexOf(e)),
							Double.valueOf(e))
							);
				//...............................................................
				// OSWALD LND
				List<String> oswladLNDProperty = reader.getXMLPropertiesByPath("//oswald_landing");
				if(!oswladLNDProperty.isEmpty())
					oswladLNDProperty.stream().forEach(e -> oswaldLanding.put(
							centerOfGravityList.get(oswladLNDProperty.indexOf(e)),
							Double.valueOf(e))
							);
				//...............................................................
				// DeltaCD0 TAKE-OFF
				List<String> deltaCD0TakeOffProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_take_off");
				if(!deltaCD0TakeOffProperty.isEmpty())
					deltaCD0TakeOffProperty.stream().forEach(dCD0 -> deltaCD0TakeOff.put(
							centerOfGravityList.get(deltaCD0TakeOffProperty.indexOf(dCD0)),
							Double.valueOf(dCD0))
							);
				//...............................................................
				// DeltaCD0 LANDING
				List<String> deltaCD0LandingProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_landing");
				if(!deltaCD0LandingProperty.isEmpty())
					deltaCD0LandingProperty.stream().forEach(dCD0 -> deltaCD0Landing.put(
							centerOfGravityList.get(deltaCD0LandingProperty.indexOf(dCD0)),
							Double.valueOf(dCD0))
							);
				//...............................................................
				// DeltaCD0 LANDING GEARS
				List<String> deltaCD0LandingGearsProperty = reader.getXMLPropertiesByPath("//delta_CD0_landing_gears");
				if(!deltaCD0LandingGearsProperty.isEmpty())
					deltaCD0LandingGearsProperty.stream().forEach(dCD0 -> deltaCD0LandingGears.put(
							centerOfGravityList.get(deltaCD0LandingGearsProperty.indexOf(dCD0)),
							Double.valueOf(dCD0))
							);
				//...............................................................
				// DeltaCD0 SPOILERS
				List<String> deltaCD0SpoilersProperty = reader.getXMLPropertiesByPath("//delta_CD0_spoilers");
				if(!deltaCD0SpoilersProperty.isEmpty())
					deltaCD0SpoilersProperty.stream().forEach(dCD0 -> deltaCD0Spoilers.put(
							centerOfGravityList.get(deltaCD0SpoilersProperty.indexOf(dCD0)),
							Double.valueOf(dCD0))
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
		Amount<Length> firstGuessCruiseLength = null;
		List<Double> sfcList = new ArrayList<>();
		List<Double> throttleList = new ArrayList<>();
		List<Double> sfcAlternateCruiseList = new ArrayList<>();
		List<Double> throttleAlternateCruiseList = new ArrayList<>();
		List<Double> sfcHoldingList = new ArrayList<>();
		List<Double> throttleHoldingList = new ArrayList<>();
		Amount<Mass> firstGuessInitialFuelMass = null;
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
		// FIRST GUESS CRUISE LENGTH
		List<String> firstGuessCruiseLengthProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/cruise_length");
		if(!firstGuessCruiseLengthProperty.isEmpty()) {
			firstGuessCruiseLength = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/cruise_length"); 
		}
		//...............................................................
		// SFC FUNCTION CRUISE
		String sfcFunctionCruiseProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/cruise_sfc_function/sfc");
		if(sfcFunctionCruiseProperty != null)
			sfcList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/cruise_sfc_function/sfc"); 
		String throttleFunctionProperty = reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/cruise_sfc_function/throttle");
		if(throttleFunctionProperty != null)
			throttleList = reader.readArrayDoubleFromXML("//performance/mission_profile_and_payload_range/cruise_sfc_function/throttle");
		
		if(sfcList.size() > 1)
			if(sfcList.size() != throttleList.size())
			{
				System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(sfcList.size() == 1) {
			sfcList.add(sfcList.get(0));
			throttleList.add(0.0);
			throttleList.add(1.0);
		}
		
		MyInterpolatingFunction sfcFunction = new MyInterpolatingFunction();
		sfcFunction.interpolateLinear(
				MyArrayUtils.convertToDoublePrimitive(throttleList),
				MyArrayUtils.convertToDoublePrimitive(sfcList)
				);
		//...............................................................
		// SFC FUNCTION ALTERNATE CRUISE
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
		
		MyInterpolatingFunction sfcAlternateCruiseFunction = new MyInterpolatingFunction();
		sfcAlternateCruiseFunction.interpolateLinear(
				MyArrayUtils.convertToDoublePrimitive(throttleAlternateCruiseList),
				MyArrayUtils.convertToDoublePrimitive(sfcAlternateCruiseList)
				);
		//...............................................................
		// SFC FUNCTION HOLDING
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
		
		MyInterpolatingFunction sfcHoldingFunction = new MyInterpolatingFunction();
		sfcHoldingFunction.interpolateLinear(
				MyArrayUtils.convertToDoublePrimitive(throttleHoldingList),
				MyArrayUtils.convertToDoublePrimitive(sfcHoldingList)
				);
		//...............................................................
		// FIRST GUESS INITIAL FUEL MASS
		List<String> firstGuessInitialFuelMassProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/initial_mission_fuel");
		if(!firstGuessInitialFuelMassProperty.isEmpty()) {
			firstGuessInitialFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/mission_profile_and_payload_range/initial_mission_fuel"); 
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
				.setMaximumTakeOffMass(maximumTakeOffMass)
				.setOperatingEmptyMass(operatingEmptyMass)
				.setMaximumFuelMass(maximumFuelMass)
				.setSinglePassengerMass(singlePassengerMass)
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
				.setMuFunction(muInterpolatingFunction)
				.setMuBrakeFunction(muBrakeInterpolatingFunction)
				.setDtRotation(dtRotation)
				.setDtHold(dtHold)
				.setAlphaGround(alphaGround)
				.setWindSpeed(windSpeed)
				.setObstacleTakeOff(obstacleTakeOff)
				.setKRotation(kRotation)
				.setAlphaDotRotation(alphaDotRotation)
				.setKCLmax(kCLmax)
				.setDragDueToEngineFailure(dragDueToEngineFailure)
				.setKAlphaDot(kAlphaDot)
				.setKLandingWeight(kLandingWeight)
				.setObstacleLanding(obstacleLanding)
				.setThetaApproach(thetaApproach)
				.setKApproach(kApproach)
				.setKFlare(kFlare)
				.setKTouchDown(kTouchDown)
				.setFreeRollDuration(freeRollDuration)
				.setKClimbWeightAEO(kClimbWeightAEO)
				.setKClimbWeightOEI(kClimbWeightOEI)
				.setClimbSpeedCAS(climbSpeed)
				.setInitialClimbAltitude(initialClimbAltitude)
				.setFinalClimbAltitude(finalClimbAltitude)
				.addAllAltitudeListCruise(altitudeListCruise)
				.setKCruiseWeight(kCruiseWeight)
				.setCLmaxInverted(cLmaxInverted)
				.setRateOfDescent(rateOfDescent)
				.setSpeedDescentCAS(speedDescentCAS)
				.setKDescentWeight(kDescentWeight)
				.setInitialDescentAltitude(initialDescentAltitude)
				.setFinalDescentAltitude(finalDescentAltitude)
				.setMissionRange(missionRange)
				.setAlternateCruiseLength(alternateCruiseLength)
				.setAlternateCruiseAltitude(alternateCruiseAltitude)
				.setHoldingDuration(holdingDuration)
				.setHoldingAltitude(holdingAltitude)
				.setHoldingMachNumber(holdingMachNumber)
				.setFuelReserve(fuelReserve)
				.setFirstGuessCruiseLength(firstGuessCruiseLength)
				.setSfcFunctionCruise(sfcFunction)
				.setSfcFunctionAlternateCruise(sfcAlternateCruiseFunction)
				.setSfcFunctionHolding(sfcHoldingFunction)
				.setFirstGuessInitialMissionFuelMass(firstGuessInitialFuelMass)
				.setTakeOffMissionAltitude(takeOffMissionAltitude)
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
		
		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "PERFORMANCE"
				+ File.separator
				);
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {
			
			String takeOffFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "TAKE_OFF"
					+ File.separator
					);
			
			CalcTakeOff calcTakeOff = new CalcTakeOff();
			calcTakeOff.performTakeOffSimulation(
					_thePerformanceInterface.getMaximumTakeOffMass(), 
					_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().to(SI.METER),
					_thePerformanceInterface.getTheOperatingConditions().getMachTakeOff()
					);
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotTakeOffPerformance(takeOffFolderPath);
			calcTakeOff.calculateBalancedFieldLength();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotBalancedFieldLength(takeOffFolderPath);
			calcTakeOff.calculateVMC();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotVMC(takeOffFolderPath);
			
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {
			
			String climbFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "CLIMB"
					+ File.separator
					);
			
			CalcClimb calcClimb = new CalcClimb();
			calcClimb.calculateClimbPerformance(
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightAEO()),
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightOEI()),
					_thePerformanceInterface.getInitialClimbAltitude(),
					_thePerformanceInterface.getFinalClimbAltitude(),
					true
					);
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcClimb.plotClimbPerformance(climbFolderPath);
			
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {
			
			String cruiseFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "CRUISE"
					+ File.separator
					);
			
			_weightListCruise = new ArrayList<Amount<Force>>();
			
			Amount<Force> cruiseWeight = 
					Amount.valueOf(
							(_thePerformanceInterface.getMaximumTakeOffMass()
							.times(_thePerformanceInterface.getKCruiseWeight())
							.times(AtmosphereCalc.g0)
							.getEstimatedValue()
							),
							SI.NEWTON
							);
			for(int i=0; i<5; i++) {			
				_weightListCruise.add(
						Amount.valueOf(
								Math.round(
										(cruiseWeight)
										.minus((cruiseWeight)
												.times(0.05*(4-i))
												)
										.getEstimatedValue()
										),
								SI.NEWTON
								)
						);
			}
			
			CalcCruise calcCruise = new CalcCruise();
			calcCruise.calculateThrustAndDrag(
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight())
					);
			calcCruise.calculateFlightEnvelope(
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight())
					);
			calcCruise.calculateEfficiency();
			calcCruise.calculateCruiseGrid();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcCruise.plotCruiseOutput(cruiseFolderPath);
			
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {
			
			String descentFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "DESCENT"
					+ File.separator
					);
			
			CalcDescent calcDescent = new CalcDescent();
			calcDescent.calculateDescentPerformance(
					_thePerformanceInterface.getInitialDescentAltitude().to(SI.METER),
					_thePerformanceInterface.getFinalDescentAltitude().to(SI.METER),
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKDescentWeight())
					);
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcDescent.plotDescentPerformance(descentFolderPath);
			
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {
			
			String landingFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "LANDING"
					+ File.separator
					);
			
			CalcLanding calcLanding = new CalcLanding();
			calcLanding.performLandingSimulation(_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()));
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcLanding.plotLandingPerformance(landingFolderPath);
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
		
			String payloadRangeFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "PAYLOAD_RANGE"
					+ File.separator
					);
			
			CalcPayloadRange calcPayloadRange = new CalcPayloadRange();
			calcPayloadRange.fromMissionProfile();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcPayloadRange.plotPayloadRange(payloadRangeFolderPath);
		
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
			
			String maneuveringFlightAndGustEnvelopeFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "V-n_DIAGRAM"
					+ File.separator
					);
			
			CalcFlightManeuveringAndGustEnvelope calcEnvelope =  new CalcFlightManeuveringAndGustEnvelope();
			calcEnvelope.fromRegulations();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcEnvelope.plotVnDiagram(maneuveringFlightAndGustEnvelopeFolderPath);
			
		}
		
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
			
			String missionProfilesFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "MISSION_PROFILES"
					+ File.separator
					);
			
			CalcMissionProfile calcMissionProfile = new CalcMissionProfile();
			calcMissionProfile.calculateMissionProfileIterative();
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
				calcMissionProfile.plotProfiles(missionProfilesFolderPath);
			
		}
		
		// PRINT RESULTS
		try {
			toXLSFile(performanceFolderPath + "Performance");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
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
        	dataListTakeOff.add(new Object[] {"Ground roll distance","m", _groundRollDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","m", _rotationDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","m", _airborneDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","m", _takeOffDistanceAEO.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","m", _takeOffDistanceFAR25.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","m", _balancedFieldLength.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {" "});
           	dataListTakeOff.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceTakeOff.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","ft", _rotationDistanceTakeOff.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","ft", _airborneDistanceTakeOff.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","ft", _takeOffDistanceAEO.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","ft", _takeOffDistanceFAR25.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","ft", _balancedFieldLength.doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","m/s", _vStallTakeOff.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","m/s", _v1.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","m/s", _vRotation.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","m/s", _vMC.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","m/s", _vLiftOff.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","m/s", _v2.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","kn", _vStallTakeOff.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","kn", _v1.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","kn", _vRotation.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","kn", _vMC.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","kn", _vLiftOff.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","kn", _v2.doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"V1/VsTO","", _v1.divide(_vStallTakeOff).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_Rot/VsTO","", _vRotation.divide(_vStallTakeOff).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"VMC/VsTO"," ", _vMC.divide(_vStallTakeOff).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_LO/VsTO","", _vLiftOff.divide(_vStallTakeOff).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V2/VsTO","", _v2.divide(_vStallTakeOff).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Take-off duration","s", _takeOffDuration.doubleValue(SI.SECOND)});

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
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","m", _absoluteCeilingAEO.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","ft", _absoluteCeilingAEO.doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","m", _serviceCeilingAEO.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","ft", _serviceCeilingAEO.doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Minimum time to climb AEO","min", _minimumClimbTimeAEO.doubleValue(NonSI.MINUTE)});
        	if(_climbTimeAtSpecificClimbSpeedAEO != null)
        		dataListClimb.add(new Object[] {"Time to climb at given climb speed AEO","min", _climbTimeAtSpecificClimbSpeedAEO.doubleValue(NonSI.MINUTE)});
        	dataListClimb.add(new Object[] {"Fuel used during climb AEO","kg", _fuelUsedDuringClimb.doubleValue(SI.KILOGRAM)});
        	dataListClimb.add(new Object[] {" "});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","m", _absoluteCeilingOEI.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","ft", _absoluteCeilingOEI.doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","m", _serviceCeilingOEI.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","ft", _serviceCeilingOEI.doubleValue(NonSI.FOOT)});

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
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","N", _thrustAtCruiseAltitudeAndMach.doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","lb", _thrustAtCruiseAltitudeAndMach.doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","N", _dragAtCruiseAltitudeAndMach.doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","lb", _dragAtCruiseAltitudeAndMach.doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","W", _powerAvailableAtCruiseAltitudeAndMach.doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","hp", _powerAvailableAtCruiseAltitudeAndMach.doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","W", _powerNeededAtCruiseAltitudeAndMach.doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","hp", _powerNeededAtCruiseAltitudeAndMach.doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","m/s", _minSpeesCASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","m/s", _maxSpeesCASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","kn", _minSpeesCASAtCruiseAltitude.doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","kn", _maxSpeesCASAtCruiseAltitude.doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","m/s", _minSpeesTASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","m/s", _maxSpeesTASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","kn", _minSpeesTASAtCruiseAltitude.doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","kn", _maxSpeesTASAtCruiseAltitude.doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min Mach number at cruise altitude","", _minMachAtCruiseAltitude});
        	dataListCruise.add(new Object[] {"Max Mach number at cruise altitude","", _maxMachAtCruiseAltitude});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Efficiency at cruise altitude and Mach","", _efficiencyAtCruiseAltitudeAndMach});
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
        	dataListDescent.add(new Object[] {"Descent length","nmi", _totalDescentLength.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListDescent.add(new Object[] {"Descent length","km", _totalDescentLength.doubleValue(SI.KILOMETER)});
        	dataListDescent.add(new Object[] {"Descent duration","min", _totalDescentTime.doubleValue(NonSI.MINUTE)});
        	dataListDescent.add(new Object[] {"Fuel used during descent","kg", _totalDescentFuelUsed.doubleValue(SI.KILOGRAM)});

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
        	dataListLanding.add(new Object[] {"Ground roll distance","m", _groundRollDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Flare distance","m", _flareDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Airborne distance","m", _airborneDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Landing distance","m", _landingDistance.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","m", _landingDistanceFAR25.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceLanding.doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Flare distance","ft", _flareDistanceLanding.doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Airborne distance","ft", _airborneDistanceLanding.doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Landing distance","ft", _landingDistance.doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","ft", _landingDistanceFAR25.doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","m/s", _vStallLanding.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","m/s", _vTouchDown.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","m/s", _vFlare.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","m/s", _vApproach.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","kn", _vStallLanding.doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","kn", _vTouchDown.doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","kn", _vFlare.doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","kn", _vApproach.doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"V_TD/VsLND","", _vTouchDown.divide(_vStallLanding).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_Flare/VsLND","", _vFlare.divide(_vStallLanding).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_A/VsLND","", _vApproach.divide(_vStallLanding).getEstimatedValue()});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Landing duration","s", _landingDuration.doubleValue(SI.SECOND)});

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

        	dataListMissionProfile.add(new Object[] {"Description","Unit","Value"});
        	dataListMissionProfile.add(new Object[] {"Total mission distance","nmi", _thePerformanceInterface.getMissionRange().to(NonSI.NAUTICAL_MILE)
        																						 .plus(_thePerformanceInterface.getAlternateCruiseLength()).to(NonSI.NAUTICAL_MILE)
        																						 .doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Total mission duration","min", _totalMissionTime.doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Aircraft mass at mission start","kg", _initialMissionMass.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft mass at mission end","kg", _endMissionMass.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Initial fuel mass for the assigned mission","kg", _initialFuelMass.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Total fuel used","kg", _totalFuelUsed.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Fuel reserve","%", _thePerformanceInterface.getFuelReserve()*100});
        	dataListMissionProfile.add(new Object[] {"Design passengers number","", _thePerformanceInterface.getTheAircraft().getCabinConfiguration().getNPax().doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Passengers number for this mission","", _theMissionProfileCalculator.getPassengersNumber().doubleValue()});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"Take-off range","nmi", _rangeList.get(1).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Climb range","nmi", _rangeList.get(2).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(1).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Cruise range","nmi", _rangeList.get(3).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(2).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"First descent range","nmi", _rangeList.get(4).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(3).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Second climb range","nmi", _rangeList.get(5).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(4).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Alternate cruise range","nmi", _rangeList.get(6).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(5).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Second descent range","nmi", _rangeList.get(7).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(6).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Holding range","nmi", _rangeList.get(8).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(7).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Third descent range","nmi", _rangeList.get(9).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(8).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Landing range","nmi", _rangeList.get(10).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(9).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"Take-off duration","min", _timeList.get(1).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Climb duration","min", _timeList.get(2).to(NonSI.MINUTE).minus(_timeList.get(1).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Cruise duration","min", _timeList.get(3).to(NonSI.MINUTE).minus(_timeList.get(2).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"First descent duration","min", _timeList.get(4).to(NonSI.MINUTE).minus(_timeList.get(3).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Second climb duration","min", _timeList.get(5).to(NonSI.MINUTE).minus(_timeList.get(4).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Alternate cruise duration","min", _timeList.get(6).to(NonSI.MINUTE).minus(_timeList.get(5).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Second descent duration","min", _timeList.get(7).to(NonSI.MINUTE).minus(_timeList.get(6).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Holding duration","min", _timeList.get(8).to(NonSI.MINUTE).minus(_timeList.get(7).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Third descent duration","min", _timeList.get(9).to(NonSI.MINUTE).minus(_timeList.get(8).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Landing duration","min", _timeList.get(10).to(NonSI.MINUTE).minus(_timeList.get(9).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"Take-off used fuel","kg", _fuelUsedList.get(1).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Climb used fuel","kg", _fuelUsedList.get(2).to(SI.KILOGRAM).minus(_fuelUsedList.get(1).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Cruise used fuel","kg", _fuelUsedList.get(3).to(SI.KILOGRAM).minus(_fuelUsedList.get(2).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"First descent used fuel","kg", _fuelUsedList.get(4).to(SI.KILOGRAM).minus(_fuelUsedList.get(3).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Climb used fuel","kg", _fuelUsedList.get(5).to(SI.KILOGRAM).minus(_fuelUsedList.get(4).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Alternate cruise used fuel","kg", _fuelUsedList.get(6).to(SI.KILOGRAM).minus(_fuelUsedList.get(5).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Second descent used fuel","kg", _fuelUsedList.get(7).to(SI.KILOGRAM).minus(_fuelUsedList.get(6).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Holding used fuel","kg", _fuelUsedList.get(8).to(SI.KILOGRAM).minus(_fuelUsedList.get(7).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Third descent used fuel","kg", _fuelUsedList.get(9).to(SI.KILOGRAM).minus(_fuelUsedList.get(8).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Landing used fuel","kg", _fuelUsedList.get(10).to(SI.KILOGRAM).minus(_fuelUsedList.get(9).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at take-off start","kg", _massList.get(1).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at climb start","kg", _massList.get(2).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at cruise start","kg", _massList.get(3).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at first descent start","kg", _massList.get(4).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at second climb start","kg", _massList.get(5).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at alternate cruise start","kg", _massList.get(6).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at second descent start","kg", _massList.get(7).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at holding start","kg", _massList.get(8).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at third descnet start","kg", _massList.get(9).doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft weight at landing start","kg", _massList.get(10).doubleValue(SI.KILOGRAM)});        	
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"TAKE-OFF"});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off start","kn", _speedTASMissionList.get(0).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off ending","kn", _speedTASMissionList.get(1).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Mach at take-off start"," ", _machMissionList.get(0).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Mach at take-off ending"," ", _machMissionList.get(1).doubleValue()});        	
        	dataListMissionProfile.add(new Object[] {"CL at take-off start"," ", _liftingCoefficientMissionList.get(0).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at take-off ending"," ", _liftingCoefficientMissionList.get(1).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at take-off start"," ", _dragCoefficientMissionList.get(0).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at take-off ending"," ", _dragCoefficientMissionList.get(1).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at take-off start"," ", _efficiencyMissionList.get(0).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at take-off ending"," ", _efficiencyMissionList.get(1).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Thrust at take-off start","lbf", _thrustMissionList.get(0).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Thrust at take-off ending","lbf", _thrustMissionList.get(1).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at take-off start","lbf", _dragMissionList.get(0).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at take-off ending","lbf", _dragMissionList.get(1).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"CLIMB"});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb start","kn", _speedTASMissionList.get(2).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb ending","kn", _speedTASMissionList.get(3).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Mach at climb start"," ", _machMissionList.get(2).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Mach at climb ending"," ", _machMissionList.get(3).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at climb start"," ", _liftingCoefficientMissionList.get(2).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at climb ending"," ", _liftingCoefficientMissionList.get(3).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at climb start"," ", _dragCoefficientMissionList.get(2).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at climb ending"," ", _dragCoefficientMissionList.get(3).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at climb start"," ", _efficiencyMissionList.get(2).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at climb ending"," ", _efficiencyMissionList.get(3).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Thrust at climb start","lbf", _thrustMissionList.get(2).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Thrust at climb ending","lbf", _thrustMissionList.get(3).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at climb start","lbf", _dragMissionList.get(2).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at climb ending","lbf", _dragMissionList.get(3).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"CRUISE"});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise start","kn", _speedTASMissionList.get(4).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise ending","kn", _speedTASMissionList.get(5).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Mach at cruise start"," ", _machMissionList.get(4).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Mach at cruise ending"," ", _machMissionList.get(5).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at cruise start"," ", _liftingCoefficientMissionList.get(4).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at cruise ending"," ", _liftingCoefficientMissionList.get(5).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at cruise start"," ", _dragCoefficientMissionList.get(4).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at cruise ending"," ", _dragCoefficientMissionList.get(5).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at cruise start"," ", _efficiencyMissionList.get(4).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at cruise ending"," ", _efficiencyMissionList.get(5).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Thrust at cruise start","lbf", _thrustMissionList.get(4).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Thrust at cruise ending","lbf", _thrustMissionList.get(5).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at cruise start","lbf", _dragMissionList.get(4).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at cruise ending","lbf", _dragMissionList.get(5).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	dataListMissionProfile.add(new Object[] {"FIRST DESCENT"});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent start","kn", _speedTASMissionList.get(6).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent ending","kn", _speedTASMissionList.get(7).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Mach at first descent start"," ", _machMissionList.get(6).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Mach at first descent ending"," ", _machMissionList.get(7).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at first descent start"," ", _liftingCoefficientMissionList.get(6).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at first descent ending"," ", _liftingCoefficientMissionList.get(7).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at first descent start"," ", _dragCoefficientMissionList.get(6).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at first descent ending"," ", _dragCoefficientMissionList.get(7).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at first descent start"," ", _efficiencyMissionList.get(6).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at first descent ending"," ", _efficiencyMissionList.get(7).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Thrust at first descent start","lbf", _thrustMissionList.get(6).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Thrust at first descent ending","lbf", _thrustMissionList.get(7).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at second climb start", "lbf", _dragMissionList.get(6).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at second climb ending", "lbf", _dragMissionList.get(7).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {" "});
        	
        	if(_thePerformanceInterface.getAlternateCruiseAltitude().doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
        		dataListMissionProfile.add(new Object[] {"SECOND CLIMB"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb start","kn", _speedTASMissionList.get(8).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb ending","kn", _speedTASMissionList.get(9).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at second climb start"," ", _machMissionList.get(8).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at second climb ending"," ", _machMissionList.get(9).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at second climb start", " ", _liftingCoefficientMissionList.get(8).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at second climb ending", " ", _liftingCoefficientMissionList.get(9).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at second climb start", " ", _dragCoefficientMissionList.get(8).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at second climb ending", " ", _dragCoefficientMissionList.get(9).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at second climb start", " ", _efficiencyMissionList.get(8).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at second climb ending", " ", _efficiencyMissionList.get(9).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at second climb start", "lbf", _thrustMissionList.get(8).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at second climb ending", "lbf", _thrustMissionList.get(9).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at first descent start","lbf", _dragMissionList.get(8).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at first descent ending","lbf", _dragMissionList.get(9).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"ALTERNATE CRUISE"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise start","kn", _speedTASMissionList.get(10).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise ending","kn", _speedTASMissionList.get(11).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at alternate cruise start"," ", _machMissionList.get(10).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at alternate cruise ending"," ", _machMissionList.get(11).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at alternate cruise start"," ", _liftingCoefficientMissionList.get(10).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at alternate cruise ending"," ", _liftingCoefficientMissionList.get(11).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at alternate cruise start"," ", _dragCoefficientMissionList.get(10).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at alternate cruise ending"," ", _dragCoefficientMissionList.get(11).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise start"," ", _efficiencyMissionList.get(10).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise ending"," ", _efficiencyMissionList.get(11).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise start","lbf", _thrustMissionList.get(10).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise ending","lbf", _thrustMissionList.get(11).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at alternate cruise start","lbf", _dragMissionList.get(10).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at alternate cruise ending","lbf", _dragMissionList.get(11).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"SECOND DESCENT"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent start","kn", _speedTASMissionList.get(12).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent ending","kn", _speedTASMissionList.get(13).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at second descent start"," ", _machMissionList.get(12).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at second descent ending"," ", _machMissionList.get(13).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at second descent start"," ", _liftingCoefficientMissionList.get(12).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at second descent ending"," ", _liftingCoefficientMissionList.get(13).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at second descent start"," ", _dragCoefficientMissionList.get(12).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at second descent ending"," ", _dragCoefficientMissionList.get(13).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at second descent start"," ", _efficiencyMissionList.get(12).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at second descent ending"," ", _efficiencyMissionList.get(13).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at second descent start","lbf", _thrustMissionList.get(12).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at second descent ending","lbf", _thrustMissionList.get(13).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at second descent start","lbf", _dragMissionList.get(12).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at second descent ending","lbf", _dragMissionList.get(13).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        	}
        	if(_thePerformanceInterface.getHoldingDuration().doubleValue(NonSI.MINUTE) != 0.0) {
        		dataListMissionProfile.add(new Object[] {"HOLDING"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding start","kn", _speedTASMissionList.get(14).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding ending","kn", _speedTASMissionList.get(15).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at holding start"," ", _machMissionList.get(14).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at holding ending"," ", _machMissionList.get(15).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at holding start"," ", _liftingCoefficientMissionList.get(14).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at holding ending"," ", _liftingCoefficientMissionList.get(15).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at holding start"," ", _dragCoefficientMissionList.get(14).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at holding ending"," ", _dragCoefficientMissionList.get(15).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at holding start"," ", _efficiencyMissionList.get(14).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at holding ending"," ", _efficiencyMissionList.get(15).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at holding start","lbf", _thrustMissionList.get(14).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at holding ending","lbf", _thrustMissionList.get(15).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at holding start","lbf", _dragMissionList.get(14).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at holding ending","lbf", _dragMissionList.get(15).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"THIRD DESCENT"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent start","kn", _speedTASMissionList.get(16).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent ending","kn", _speedTASMissionList.get(17).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at third descent start"," ", _machMissionList.get(16).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at third descent ending"," ", _machMissionList.get(17).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at third descent start"," ", _liftingCoefficientMissionList.get(16).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at third descent ending"," ", _liftingCoefficientMissionList.get(17).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at third descent start"," ", _dragCoefficientMissionList.get(16).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at third descent ending"," ", _dragCoefficientMissionList.get(17).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at third descent start"," ", _efficiencyMissionList.get(16).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at third descent ending"," ", _efficiencyMissionList.get(17).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at third descent start","lbf", _thrustMissionList.get(16).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at third descent ending","lbf", _thrustMissionList.get(17).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at third descent start","lbf", _dragMissionList.get(16).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at third descent ending","lbf", _dragMissionList.get(17).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {" "});
        	}
        	dataListMissionProfile.add(new Object[] {"LANDING"});
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing start","kn", _speedTASMissionList.get(18).doubleValue(NonSI.KNOT)});        	
        	dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing ending","kn", _speedTASMissionList.get(19).doubleValue(NonSI.KNOT)});
        	dataListMissionProfile.add(new Object[] {"Mach at landing start"," ", _machMissionList.get(18).doubleValue()});        	
        	dataListMissionProfile.add(new Object[] {"Mach at landing ending"," ", _machMissionList.get(19).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CL at landing start", " ", _liftingCoefficientMissionList.get(18).doubleValue()});        	
        	dataListMissionProfile.add(new Object[] {"CL at landing ending", " ", _liftingCoefficientMissionList.get(19).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"CD at landing start", " ", _dragCoefficientMissionList.get(18).doubleValue()});        	
        	dataListMissionProfile.add(new Object[] {"CD at landing ending", " ", _dragCoefficientMissionList.get(19).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Efficiency at landing start", " ", _efficiencyMissionList.get(18).doubleValue()});        	
        	dataListMissionProfile.add(new Object[] {"Efficiency at landing ending", " ", _efficiencyMissionList.get(19).doubleValue()});
        	dataListMissionProfile.add(new Object[] {"Thrust at landing start", "lbf", _thrustMissionList.get(18).doubleValue(NonSI.POUND_FORCE)});        	
        	dataListMissionProfile.add(new Object[] {"Thrust at landing ending", "lbf", _thrustMissionList.get(19).doubleValue(NonSI.POUND_FORCE)});
        	dataListMissionProfile.add(new Object[] {"Drag at landing start", "lbf", _dragMissionList.get(18).doubleValue(NonSI.POUND_FORCE)});        	
        	dataListMissionProfile.add(new Object[] {"Drag at landing ending", "lbf", _dragMissionList.get(19).doubleValue(NonSI.POUND_FORCE)});
        	
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
        //--------------------------------------------------------------------------------
        // PAYLOAD-RANGE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
        	Sheet sheetPayloadRange = wb.createSheet("PAYLOAD-RANGE");
        	List<Object[]> dataListPayloadRange = new ArrayList<>();

        	dataListPayloadRange.add(new Object[] {"Description","Unit","Value"});
        	dataListPayloadRange.add(new Object[] {"ALTITUDE","ft",_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(NonSI.FOOT)});
        	dataListPayloadRange.add(new Object[] {"MACH"," ",_thePerformanceInterface.getTheOperatingConditions().getMachCruise()});
        	dataListPayloadRange.add(new Object[] {" "});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _maxPayload.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxPayload.doubleValue()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtMaxPayload.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT DESIGN PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtDesignPayload.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _designPayload.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtDesignPayload.doubleValue()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtDesignPayload.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX FUEL"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxFuel.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _payloadAtMaxFuel.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxFuel.doubleValue()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePerformanceInterface.getMaximumFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"RANGE AT ZERO PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtZeroPayload.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _takeOffMassAtZeroPayload.doubleValue(SI.KILOGRAM)});
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
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","m/s", _theEnvelopeCalculator.getStallSpeedClean().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","kn", _theEnvelopeCalculator.getStallSpeedClean().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","m/s", _theEnvelopeCalculator.getStallSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","kn", _theEnvelopeCalculator.getStallSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point C"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point D"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculator.getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point E"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculator.getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point F"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point H"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInverted()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE"});
        	dataListVnDiagram.add(new Object[] {"Point A'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point C'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point D'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculator.getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point E'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculator.getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point F'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point H'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInvertedWithGust()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"FLAP MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","m/s", _theEnvelopeCalculator.getStallSpeedFullFlap().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","kn", _theEnvelopeCalculator.getStallSpeedFullFlap().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point I"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE (with flaps)"});
        	dataListVnDiagram.add(new Object[] {"Point A'_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point I'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeedWithGust()});

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
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {
			
			sb.append("\tTAKE-OFF\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceTakeOff.to(SI.METER) + "\n")
			.append("\t\tRotation distance = " + _rotationDistanceTakeOff.to(SI.METER) + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceTakeOff.to(SI.METER) + "\n")
			.append("\t\tAEO take-off distance = " + _takeOffDistanceAEO.to(SI.METER) + "\n")
			.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25.to(SI.METER) + "\n")
			.append("\t\tBalanced field length = " + _balancedFieldLength.to(SI.METER) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceTakeOff.to(NonSI.FOOT) + "\n")
			.append("\t\tRotation distance = " + _rotationDistanceTakeOff.to(NonSI.FOOT) + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceTakeOff.to(NonSI.FOOT) + "\n")
			.append("\t\tAEO take-off distance = " + _takeOffDistanceAEO.to(NonSI.FOOT) + "\n")
			.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25.to(NonSI.FOOT) + "\n")
			.append("\t\tBalanced field length = " + _balancedFieldLength.to(NonSI.FOOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOff.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tDecision speed (V1) = " + _v1.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tRotation speed (V_Rot) = " + _vRotation.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tMiminum control speed (VMC) = " + _vMC.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tLift-off speed (V_LO) = " + _vLiftOff.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tTake-off safety speed (V2) = " + _v2.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOff.to(NonSI.KNOT) + "\n")
			.append("\t\tDecision speed (V1) = " + _v1.to(NonSI.KNOT) + "\n")
			.append("\t\tRotation speed (V_Rot) = " + _vRotation.to(NonSI.KNOT) + "\n")
			.append("\t\tMiminum control speed (VMC) = " + _vMC.to(NonSI.KNOT) + "\n")
			.append("\t\tLift-off speed (V_LO) = " + _vLiftOff.to(NonSI.KNOT) + "\n")
			.append("\t\tTake-off safety speed (V2) = " + _v2.to(NonSI.KNOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tV1/VsTO = " + _v1.to(SI.METERS_PER_SECOND).divide(_vStallTakeOff.to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tV_Rot/VsTO = " + _vRotation.to(SI.METERS_PER_SECOND).divide(_vStallTakeOff.to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tVMC/VsTO = " + _vMC.to(SI.METERS_PER_SECOND).divide(_vStallTakeOff.to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tV_LO/VsTO = " + _vLiftOff.to(SI.METERS_PER_SECOND).divide(_vStallTakeOff.to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tV2/VsTO = " + _v2.to(SI.METERS_PER_SECOND).divide(_vStallTakeOff.to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tTake-off duration = " + _takeOffDuration + "\n")
			.append("\t-------------------------------------\n")
			;
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {
			
			sb.append("\tCLIMB\n")
			.append("\t-------------------------------------\n")
			.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEO.to(SI.METER) + "\n")
			.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEO.to(NonSI.FOOT) + "\n")
			.append("\t\tService ceiling AEO = " + _serviceCeilingAEO.to(SI.METER) + "\n")
			.append("\t\tService ceiling AEO = " + _serviceCeilingAEO.to(NonSI.FOOT) + "\n")
			.append("\t\tMinimum time to climb AEO = " + _minimumClimbTimeAEO.to(NonSI.MINUTE) + "\n");
			if(_climbTimeAtSpecificClimbSpeedAEO != null)
				sb.append("\t\tTime to climb at given climb speed AEO = " + _climbTimeAtSpecificClimbSpeedAEO.to(NonSI.MINUTE) + "\n");
			sb.append("\t\tFuel used durign climb AEO = " + _fuelUsedDuringClimb.to(SI.KILOGRAM) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEI.to(SI.METER) + "\n")
			.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEI.to(NonSI.FOOT) + "\n")
			.append("\t\tService ceiling OEI = " + _serviceCeilingOEI.to(SI.METER) + "\n")
			.append("\t\tService ceiling OEI = " + _serviceCeilingOEI.to(NonSI.FOOT) + "\n");
			
			sb.append("\t-------------------------------------\n");
			
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {
			
			sb.append("\tCRUISE\n")
			.append("\t-------------------------------------\n")
			.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMach.to(SI.NEWTON) + "\n")
			.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMach.to(NonSI.POUND_FORCE) + "\n")
			.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMach.to(SI.NEWTON) + "\n")
			.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMach.to(NonSI.POUND_FORCE) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMach.to(SI.WATT) + "\n")
			.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMach.to(NonSI.HORSEPOWER) + "\n")
			.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMach.to(SI.WATT) + "\n")
			.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMach.to(NonSI.HORSEPOWER) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitude.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitude.to(NonSI.KNOT) + "\n")
			.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitude.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitude.to(NonSI.KNOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitude.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitude.to(NonSI.KNOT) + "\n")
			.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitude.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitude.to(NonSI.KNOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tMin Mach at cruise altitude = " + _minMachAtCruiseAltitude + "\n")
			.append("\t\tMax Mach at cruise altitude = " + _maxMachAtCruiseAltitude + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tEfficiency at cruise altitude and Mach = " + _efficiencyAtCruiseAltitudeAndMach + "\n")
			.append("\t-------------------------------------\n");
			
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {
			
			sb.append("\tDESCENT\n")
			.append("\t-------------------------------------\n")
			.append("\t\tDescent length = " + _totalDescentLength.to(SI.KILOMETER) + "\n")
			.append("\t\tDescent length = " + _totalDescentLength.to(NonSI.NAUTICAL_MILE) + "\n")
			.append("\t\tDescent duration = " + _totalDescentTime.to(NonSI.MINUTE) + "\n")
			.append("\t\tFuel used during descent = " + _totalDescentFuelUsed.to(SI.KILOGRAM) + "\n")
			.append("\t-------------------------------------\n");
			;
			
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {
			
			sb.append("\tLANDING\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceLanding.to(SI.METER) + "\n")
			.append("\t\tFlare distance = " + _flareDistanceLanding.to(SI.METER) + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceLanding.to(SI.METER) + "\n")
			.append("\t\tLanding distance = " + _landingDistance.to(SI.METER) + "\n")
			.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25.to(SI.METER) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceLanding.to(NonSI.FOOT) + "\n")
			.append("\t\tFlare distance = " + _flareDistanceLanding.to(NonSI.FOOT) + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceLanding.to(NonSI.FOOT) + "\n")
			.append("\t\tLanding distance = " + _landingDistance.to(NonSI.FOOT) + "\n")
			.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25.to(NonSI.FOOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed landing (VsLND)= " + _vStallLanding.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tTouchdown speed (V_TD) = " + _vTouchDown.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tFlare speed (V_Flare) = " + _vFlare.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tApproach speed (V_Approach) = " + _vApproach.to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed landing (VsLND)= " + _vStallLanding.to(NonSI.KNOT) + "\n")
			.append("\t\tTouchdown speed (V_TD) = " + _vTouchDown.to(NonSI.KNOT) + "\n")
			.append("\t\tFlare speed (V_Flare) = " + _vFlare.to(NonSI.KNOT) + "\n")
			.append("\t\tApproach speed (V_Approach) = " + _vApproach.to(NonSI.KNOT) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tV_TD/VsLND = " + _vTouchDown.to(SI.METERS_PER_SECOND).divide(_vStallLanding.to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
			.append("\t\tV_Flare/VsLND = " + _vFlare.to(SI.METERS_PER_SECOND).divide(_vStallLanding.to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
			.append("\t\tV_Approach/VsLND = " + _vApproach.to(SI.METERS_PER_SECOND).divide(_vStallLanding.to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tLanding duration = " + _landingDuration + "\n")
			.append("\t-------------------------------------\n")
			;
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
			sb.append("\tMISSION PROFILE\n")
			.append(_theMissionProfileCalculator.toString());
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
			sb.append("\tPAYLOAD-RANGE\n")
			.append(_thePayloadRangeCalculator.toString());
		}
		if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
			sb.append("\tV-n DIAGRAM\n")
			.append(_theEnvelopeCalculator.toString());
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
				double mach
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
			
			_theTakeOffCalculator = new TakeOffCalc(
					_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(),
					_thePerformanceInterface.getTheAircraft().getWing().getSurface(),
					_thePerformanceInterface.getTheAircraft().getPowerPlant(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getThePerformance().getPolarCLTakeOff(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getThePerformance().getPolarCDTakeOff(),
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
					_cLmaxTakeOff,
					_cLZeroTakeOff,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theTakeOffCalculator.calculateTakeOffDistanceODE(null, false, true);

			// Distances:
			_groundRollDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0).to(NonSI.FOOT);
			_rotationDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1).minus(_groundRollDistanceTakeOff).to(NonSI.FOOT);
			_airborneDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2).minus(_rotationDistanceTakeOff).minus(_groundRollDistanceTakeOff).to(NonSI.FOOT);
			_takeOffDistanceAEO = _groundRollDistanceTakeOff.plus(_rotationDistanceTakeOff).plus(_airborneDistanceTakeOff).to(NonSI.FOOT);
			_takeOffDistanceFAR25 = _takeOffDistanceAEO.times(1.15).to(NonSI.FOOT);
			
			// Velocities:
			_vStallTakeOff = _theTakeOffCalculator.getvSTakeOff().to(NonSI.KNOT);
			_vRotation = _theTakeOffCalculator.getvRot().to(NonSI.KNOT);
			_vLiftOff = _theTakeOffCalculator.getvLO().to(NonSI.KNOT);
			_v2 = _theTakeOffCalculator.getV2().to(NonSI.KNOT);
			
			// Duration:
			_takeOffDuration = _theTakeOffCalculator.getTakeOffResults().getTime().get(2);
			
		}
		
		public void calculateBalancedFieldLength() {
			
			_theTakeOffCalculator.calculateBalancedFieldLength();
			
			_v1 = _theTakeOffCalculator.getV1().to(NonSI.KNOT);
			_balancedFieldLength = _theTakeOffCalculator.getBalancedFieldLength().to(NonSI.FOOT);
			
		}
		
		public void calculateVMC() {
			
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
				_thePerformanceInterface.getTheAircraft().calculateArms(_thePerformanceInterface.getTheAircraft().getVTail(),_xCGMaxAft);
			
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
					_theTakeOffCalculator.getvSTakeOff().times(1.13).doubleValue(SI.METERS_PER_SECOND),
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
			
			_thrustMomentOEI = new double[thrust.length]; 
			for(int i=0; i < thrust.length; i++){
				_thrustMomentOEI[i] = thrust[i]*maxEngineArm.doubleValue(SI.METER);
			}

			//..................................................................................
			// CALCULATING THE VERTICAL TAIL YAWING MOMENT
			_yawingMomentOEI = new double[_thrustMomentOEI.length];
			
			double tau = LiftCalc.calculateTauIndexElevator(
					_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
					_thePerformanceInterface.getTheAircraft().getVTail().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getHighLiftDatabaseReader(),
					_thePerformanceInterface.getTheAircraft().getVTail().getAerodynamicDatabaseReader(),
					_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMaximumDeflection()
					);
			
//			double tau = 0.5284; // (Only for IRON)
			
			for(int i=0; i < thrust.length; i++){
			_yawingMomentOEI[i] = cNbVertical*
					tau*
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
					_thrustMomentOEI,
					_yawingMomentOEI
					);
			int indexOfVMC = 0;
			for(int i=0; i<curvesIntersection.length; i++)
				if(curvesIntersection[i] != 0.0) {
					indexOfVMC = i;
				}			
			
			if(indexOfVMC != 0)
				_vMC = Amount.valueOf(
						speed[indexOfVMC],
						SI.METERS_PER_SECOND
						).to(NonSI.KNOT);
			else
				_vMC = Amount.valueOf(
						0.0,
						SI.METERS_PER_SECOND
						).to(NonSI.KNOT);
			
		}

		public void plotTakeOffPerformance(String takeOffFolderPath) {

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.TAKE_OFF_SIMULATIONS))
				try {
					_theTakeOffCalculator.createTakeOffCharts(
							takeOffFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

		}
		
		public void plotBalancedFieldLength(String takeOffFolderPath) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.BALANCED_FIELD_LENGTH))
				_theTakeOffCalculator.createBalancedFieldLengthChart(
						takeOffFolderPath,
						_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
						);
			
		}
		
		public void plotVMC(String takeOffFolderPath) {
			
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().doubleValue(SI.METER)
							)/_vStallTakeOff.doubleValue(SI.METERS_PER_SECOND),
					1.13, // maximum value of the VMC from FAR regulations
					250
					);
			
			double[][] thrustPlotVector = new double [2][speed.length];
			double[][] speedPlotVector = new double [2][speed.length];
			
			for(int i=0; i < speed.length; i++){
				speedPlotVector[0][i] = speed[i];
				speedPlotVector[1][i] = speed[i];
				thrustPlotVector[0][i] = _thrustMomentOEI[i];
				thrustPlotVector[1][i] = _yawingMomentOEI[i];
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
				boolean performOEI 
				) {
			
			_theClimbCalculator = new ClimbCalc(
					_thePerformanceInterface.getTheAircraft(),
					_thePerformanceInterface.getTheOperatingConditions(),
					_cLmaxClean,
					_polarCLClimb,
					_polarCDClimb,
					_thePerformanceInterface.getClimbSpeedCAS(),
					_thePerformanceInterface.getDragDueToEngineFailure()
					);
			
			_theClimbCalculator.calculateClimbPerformance(
					startClimbMassAEO,
					startClimbMassOEI,
					initialClimbAltitude,
					finalClimbAltitude,
					performOEI
					);
			
			_rcMapAEO = _theClimbCalculator.getRCMapAEO();
			_rcMapOEI = _theClimbCalculator.getRCMapOEI();
			_ceilingMapAEO = _theClimbCalculator.getCeilingMapAEO();
			_ceilingMapOEI = _theClimbCalculator.getCeilingMapOEI();
			_dragListAEO = _theClimbCalculator.getDragListAEO();
			_thrustListAEO = _theClimbCalculator.getThrustListOEI();
			_dragListOEI = _theClimbCalculator.getDragListOEI();
			_thrustListOEI = _theClimbCalculator.getThrustListOEI();
			_efficiencyMapAltitudeAEO = _theClimbCalculator.getEfficiencyMapAltitudeAEO();
			_absoluteCeilingAEO = _theClimbCalculator.getAbsoluteCeilingAEO();
			_serviceCeilingAEO = _theClimbCalculator.getServiceCeilingAEO();
			_minimumClimbTimeAEO = _theClimbCalculator.getMinimumClimbTimeAEO();
			_climbTimeAtSpecificClimbSpeedAEO = _theClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
			_fuelUsedDuringClimb = _theClimbCalculator.getClimbTotalFuelUsed();
			
			_absoluteCeilingOEI = _theClimbCalculator.getAbsoluteCeilingOEI();
			_serviceCeilingOEI = _theClimbCalculator.getServiceCeilingOEI();
			
		}
		
		public void plotClimbPerformance(String climbFolderPath) {
			
			_theClimbCalculator.plotClimbPerformance(_thePerformanceInterface.getPlotList(), climbFolderPath);
			
		}
	}
	//............................................................................
	// END OF THE CLIMB INNER CLASS
	//............................................................................

	//............................................................................
	// CRUISE INNER CLASS
	//............................................................................
	public class CalcCruise {
		
		public void calculateThrustAndDrag(Amount<Mass> startCruiseMass) {
			
			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));

			_dragListAltitudeParameterization = new ArrayList<DragMap>();
			_thrustListAltitudeParameterization = new ArrayList<ThrustMap>();

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
								MyArrayUtils.getMax(_polarCLCruise)
								),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
								),
						100
						);
				//..................................................................................................
				_dragListAltitudeParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								speedArrayAltitudeParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);

				//..................................................................................................
				_thrustListAltitudeParameterization.add(
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
										_thrustListAltitudeParameterization.get(i).getSpeed(),
										_thrustListAltitudeParameterization.get(i).getThrust(),
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
										_dragListAltitudeParameterization.get(i).getSpeed(),
										_dragListAltitudeParameterization.get(i).getDrag(),
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
										_thrustListAltitudeParameterization.get(i).getSpeed(),
										_thrustListAltitudeParameterization.get(i).getPower(),
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
										_dragListAltitudeParameterization.get(i).getSpeed(),
										_dragListAltitudeParameterization.get(i).getPower(),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.WATT
								)
						);
			}
				
				
			_thrustAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(thrustAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							);
			_dragAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(dragAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							);
			_powerAvailableAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerAvailableAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							);
			_powerNeededAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerNeededAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							); 
			

			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			_dragListWeightParameterization = new ArrayList<DragMap>();
			_thrustListWeightParameterization = new ArrayList<ThrustMap>();
			
			double[] speedArrayWeightParameterization = new double[100];

			for(int i=0; i<_weightListCruise.size(); i++) {
				//..................................................................................................
				speedArrayWeightParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise)
								),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_thePerformanceInterface.getAltitudeListCruise()))
								),
						100
						);
				//..................................................................................................
				_dragListWeightParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								speedArrayWeightParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_thePerformanceInterface.getTheAircraft().getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
			}
			//..................................................................................................
			_thrustListWeightParameterization.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							MyArrayUtils.linspace(
									SpeedCalc.calculateSpeedStall(
											_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
											_weightListCruise.get(0).doubleValue(SI.NEWTON),
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											MyArrayUtils.getMax(_polarCLCruise)
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

		public void calculateFlightEnvelope(Amount<Mass> startCruiseMass) {

			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));

			_intersectionList = new ArrayList<>();
			_cruiseEnvelopeList = new ArrayList<>();

			List<DragMap> dragList = new ArrayList<>();
			List<ThrustMap> thrustList = new ArrayList<>();
			List<Amount<Length>> altitude = new ArrayList<>();
			altitude.add(Amount.valueOf(0.0, SI.METER));
			Amount<Length> deltaAltitude = Amount.valueOf(100, SI.METER);
			
			int nPointSpeed = 1000;
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
							MyArrayUtils.getMax(_polarCLCruise)
							),
					SpeedCalc.calculateTAS(1.0, altitude.get(0).doubleValue(SI.METER)),
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
							MyArrayUtils.getMax(_polarCLCruise),
							MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
							MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
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
			_intersectionList.add(
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
							MyArrayUtils.getMax(_polarCLCruise),
							dragList,
							thrustList
							)
					);
			
			while((_intersectionList.get(_intersectionList.size()-1).getMaxSpeed()
					- _intersectionList.get(_intersectionList.size()-1).getMinSpeed())
					>= 0.0001
					) {
				
				if(i >= 1)
					altitude.add(altitude.get(i-1).plus(deltaAltitude));
				
				//..................................................................................................
				speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitude.get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise)
								),
						SpeedCalc.calculateTAS(1.0, altitude.get(i).doubleValue(SI.METER)),
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
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
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
				_intersectionList.add(
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
								MyArrayUtils.getMax(_polarCLCruise),
								dragList,
								thrustList
								)
						);
				i++;
				
			}
			
			for (int j=0; j<altitude.size(); j++) 
				_cruiseEnvelopeList.add(
						PerformanceCalcUtils.calculateEnvelope(
								altitude.get(j).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								EngineOperatingConditionEnum.CRUISE,
								_intersectionList
								)
						);
			
			List<Double> altitudeList = new ArrayList<>();
			List<Double> minSpeedTASList = new ArrayList<>();
			List<Double> minSpeedCASList = new ArrayList<>();
			List<Double> minMachList = new ArrayList<>();
			List<Double> maxSpeedTASList = new ArrayList<>();
			List<Double> maxSpeedCASList = new ArrayList<>();
			List<Double> maxMachList = new ArrayList<>();
		
			for(int j=0; j<_cruiseEnvelopeList.size(); j++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_cruiseEnvelopeList.get(j).getAltitude()
						).getDensity()*1000/1.225; 
				
				if(_cruiseEnvelopeList.get(j).getMaxSpeed() != 0.0) {
					altitudeList.add(_cruiseEnvelopeList.get(j).getAltitude());
					minSpeedTASList.add(_cruiseEnvelopeList.get(j).getMinSpeed());
					minSpeedCASList.add(_cruiseEnvelopeList.get(j).getMinSpeed()*(Math.sqrt(sigma)));
					minMachList.add(_cruiseEnvelopeList.get(j).getMinMach());
					maxSpeedTASList.add(_cruiseEnvelopeList.get(j).getMaxSpeed());
					maxSpeedCASList.add(_cruiseEnvelopeList.get(j).getMaxSpeed()*(Math.sqrt(sigma)));
					maxMachList.add(_cruiseEnvelopeList.get(j).getMaxMach());
				}
			}
				
			_minSpeesTASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_maxSpeesTASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_minSpeesCASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_maxSpeesCASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_minMachAtCruiseAltitude = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(minMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							);
			_maxMachAtCruiseAltitude = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(maxMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							);
			
			if(_maxMachAtCruiseAltitude < _thePerformanceInterface.getTheOperatingConditions().getMachCruise()) {
				System.err.println("THE CHOSEN CRUISE MACH NUMBER IS NOT INSIDE THE FLIGHT ENVELOPE !");
			}
				

		}

		public void calculateEfficiency() {
			
			_efficiencyMapAltitude = new HashMap<>();
			_efficiencyMapWeight = new HashMap<>();
			
			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				List<Double> liftAltitudeParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
				for(int j=0; j<_dragListAltitudeParameterization.get(i).getSpeed().length; j++) {
					liftAltitudeParameterization.add(
							LiftCalc.calculateLift(
									_dragListAltitudeParameterization.get(i).getSpeed()[j],
									_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_dragListAltitudeParameterization.get(i).getAltitude(),
									LiftCalc.calculateLiftCoeff(
											(_thePerformanceInterface.getMaximumTakeOffMass()
													.times(_thePerformanceInterface.getKCruiseWeight())
													.times(AtmosphereCalc.g0)
													.getEstimatedValue()
													),
											_dragListAltitudeParameterization.get(i).getSpeed()[j],
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_dragListAltitudeParameterization.get(i).getAltitude()
											)
									)			
							);
					efficiencyListCurrentAltitude.add(
							liftAltitudeParameterization.get(j)
							/ _dragListAltitudeParameterization.get(i).getDrag()[j]
							);
				}
				_efficiencyMapAltitude.put(
						"Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude(),
						efficiencyListCurrentAltitude
						);
			}
			
			List<Double> efficiencyAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++)
				efficiencyAltitudesAtCruiseMach.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAltitudeParameterization.get(i).getSpeed(),
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitude
										.get("Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude())
										),
								SpeedCalc.calculateTAS(
										_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
										_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
										)
								)
						);
			
			_efficiencyAtCruiseAltitudeAndMach = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_thePerformanceInterface.getAltitudeListCruise().stream()
									.map(a -> a.to(SI.METER))
									.collect(Collectors.toList())
									),
							MyArrayUtils.convertToDoublePrimitive(efficiencyAltitudesAtCruiseMach),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							);
			
			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			for(int i=0; i<_weightListCruise.size(); i++) {
				List<Double> liftWeightParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentWeight = new ArrayList<>();
				for(int j=0; j<_dragListWeightParameterization.get(i).getSpeed().length; j++) {
					liftWeightParameterization.add(
							LiftCalc.calculateLift(
									_dragListWeightParameterization.get(i).getSpeed()[j],
									_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
									LiftCalc.calculateLiftCoeff(
											_dragListWeightParameterization.get(i).getWeight(),
											_dragListWeightParameterization.get(i).getSpeed()[j],
											_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
											)
									)			
							);
					efficiencyListCurrentWeight.add(
							liftWeightParameterization.get(j)
							/ _dragListWeightParameterization.get(i).getDrag()[j]
							);
				}
				_efficiencyMapWeight.put(
						"Weight = " + _dragListWeightParameterization.get(i).getWeight(),
						efficiencyListCurrentWeight
						);
			}			
		}
		
		public void calculateCruiseGrid() {

			Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing()));
			
			List<DragMap> dragListWeightParameterization = new ArrayList<DragMap>();
			List<ThrustMap> thrustListWeightParameterization = new ArrayList<ThrustMap>();
			
			double[] speedArrayWeightParameterization = new double[100];
			speedArrayWeightParameterization = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_weightListCruise.get(_weightListCruise.size()-1).doubleValue(SI.NEWTON), 
							_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE), 
							MyArrayUtils.getMax(_polarCLCruise)
							),
					SpeedCalc.calculateTAS(
							1.0,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							),
					100
					);
			
			for(int i=0; i<_weightListCruise.size(); i++) {
				//..................................................................................................
				dragListWeightParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								speedArrayWeightParameterization,
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
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
			for(int i=0; i<_dragListWeightParameterization.size(); i++) {
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
								thrustListWeightParameterization.get(0).getSpeed(),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								_thePerformanceInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								dragListWeightParameterization,
								thrustListWeightParameterization
								)
						);
			}
			
			Double[] machArray = null;
			Double[] efficiency = null;
			Double[] sfc = null;
			Double[] specificRange = null;
 			
			_specificRangeMap = new ArrayList<>();
			
			for(int i=0; i<_weightListCruise.size(); i++) { 
				if(intersectionList.get(i).getMaxMach() != 0.0) {
					machArray = MyArrayUtils.linspaceDouble(
							intersectionList.get(i).getMinMach(),
							intersectionList.get(i).getMaxMach(),
							_dragListWeightParameterization.get(i).getSpeed().length);

					sfc = SpecificRangeCalc.calculateSfcVsMach(
							machArray,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineType(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant()
							);

					efficiency = MyArrayUtils.convertFromDoubleToPrimitive(
							MyArrayUtils.convertToDoublePrimitive(
									_efficiencyMapWeight.get(
											"Weight = " + _dragListWeightParameterization.get(i).getWeight()
											)
									)
							);

					specificRange = SpecificRangeCalc.calculateSpecificRangeVsMach(
							Amount.valueOf(
									_weightListCruise.get(i).divide(AtmosphereCalc.g0).getEstimatedValue(),
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

					_specificRangeMap.add(
							new SpecificRangeMap(
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
									_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
									_weightListCruise.get(i).doubleValue(SI.NEWTON),
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
				
		public void plotCruiseOutput(String cruiseFolderPath) {

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
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterization.get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterization.get(i).getDrag()));
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getDrag())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Drag at " + _dragListAltitudeParameterization.get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Drag at " + Amount.valueOf(_dragListAltitudeParameterization.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterization.get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterization.get(i).getThrust()));
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterization.get(i).getThrust())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Thrust at " + _thrustListAltitudeParameterization.get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Thrust at " + Amount.valueOf(_thrustListAltitudeParameterization.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
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

				for (int i=0; i<_weightListCruise.size(); i++) {
					speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterization.get(i).getSpeed()));
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterization.get(i).getDrag()));
					dragAndThrustWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getDrag())
									.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendWeights.add("Drag at " + Math.round(_dragListWeightParameterization.get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterization.get(0).getSpeed()));
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterization.get(0).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				dragAndThrustWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterization.get(0).getThrust()));
				dragAndThrustWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterization.get(0).getThrust())
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
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterization.get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListAltitudeParameterization.get(i).getPower()));
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Power needed at " + _dragListAltitudeParameterization.get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Power needed at " + Amount.valueOf(_dragListAltitudeParameterization.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterization.get(i).getSpeed()));
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListAltitudeParameterization.get(i).getPower()));
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_thrustListAltitudeParameterization.get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Power available at " + _thrustListAltitudeParameterization.get(i).getAltitude() + " m");
					legendAltitudes_Imperial.add("Power available at " + Amount.valueOf(_thrustListAltitudeParameterization.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
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

				for (int i=0; i<_weightListCruise.size(); i++) {
					speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterization.get(i).getSpeed()));
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					powerNeededAndAvailableWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_dragListWeightParameterization.get(i).getPower()));
					powerNeededAndAvailableWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getPower())
									.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
									.toArray()
									)
							);
					legendWeights.add("Power needed at " + Math.round(_dragListWeightParameterization.get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterization.get(0).getSpeed()));
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterization.get(0).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				powerNeededAndAvailableWeights_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_thrustListWeightParameterization.get(0).getPower()));
				powerNeededAndAvailableWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListWeightParameterization.get(0).getPower())
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
			
				for(int i=0; i<_cruiseEnvelopeList.size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MIN VALUES
					if(_cruiseEnvelopeList.get(i).getMinSpeed() != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeList.get(i).getAltitude());
						altitudeList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeList.get(i).getMinSpeed());
						speedTASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(i).getMinSpeed(), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeList.get(i).getMinSpeed()*(Math.sqrt(sigma)));
						speedCASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(i).getMinSpeed()*(Math.sqrt(sigma)), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeList.get(i).getMinMach());
					}
				}
				for(int i=0; i<_cruiseEnvelopeList.size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MAX VALUES
					if(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed() != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getAltitude());
						altitudeList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed());
						speedTASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed(), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed()*(Math.sqrt(sigma)));
						speedCASList_Imperial.add(Amount.valueOf(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed()*(Math.sqrt(sigma)), SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxMach());
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
							_cruiseEnvelopeList.get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(i).getAltitude()
							).getSpeedOfSound(); 
					
					speedListAltitudeParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterization.get(i).getSpeed()
									)
							);
					speedListAltitudeParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					speedListAltitudeParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.toArray()
									)
							);
					speedListAltitudeParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					machListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListAltitudeParameterization.get(i).getSpeed())
									.map(x -> x/speedOfSound)
									.toArray()
									)
							);
					efficiencyListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyMapAltitude.get(
													"Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude()
													)
											)
									)
							);
					legendAltitude_SI.add("Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude());
					legendAltitude_Imperial.add("Altitude = " + Amount.valueOf(_dragListAltitudeParameterization.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
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
				for(int i=0; i<_weightListCruise.size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(i).getAltitude()
							).getSpeedOfSound(); 
					
					speedListWeightParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterization.get(i).getSpeed()
									)
							);
					speedListWeightParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					speedListWeightParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.toArray()
									)
							);
					speedListWeightParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> x*Math.sqrt(sigma))
									.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					machListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									Arrays.stream(_dragListWeightParameterization.get(i).getSpeed())
									.map(x -> x/speedOfSound)
									.toArray()
									)
							);
					efficiencyListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyMapWeight.get(
													"Weight = " + _dragListWeightParameterization.get(i).getWeight()
													)
											)
									)
							);
					legendWeight.add("Weight = " + _dragListWeightParameterization.get(i).getWeight());
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
				
				for(int i=0; i<_specificRangeMap.size(); i++) {
					specificRange.add(_specificRangeMap.get(i).getSpecificRange());
					mach.add(_specificRangeMap.get(i).getMach());
					legend.add("Mass = " + _specificRangeMap.get(i).getWeight()/9.81);
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
				Amount<Mass> initialDescentMass
				) {
			
			_theDescentCalculator = new DescentCalc(
					_thePerformanceInterface.getTheAircraft(),
					_thePerformanceInterface.getSpeedDescentCAS(),
					_thePerformanceInterface.getRateOfDescent(),
					initialDescentAltitude,
					endDescentAltitude,
					initialDescentMass,
					_polarCLCruise,
					_polarCDCruise
					);
					
			_theDescentCalculator.calculateDescentPerformance();
			_descentLengths = _theDescentCalculator.getDescentLengths();
			_descentTimes = _theDescentCalculator.getDescentTimes();
			_descentAngles = _theDescentCalculator.getDescentAngles();
			_totalDescentLength = _theDescentCalculator.getTotalDescentLength();
			_totalDescentTime = _theDescentCalculator.getTotalDescentTime();
			_totalDescentFuelUsed = _theDescentCalculator.getTotalDescentFuelUsed();
		}
		
		public void plotDescentPerformance(String descentFolderPath) {
			
			_theDescentCalculator.plotDescentPerformance(descentFolderPath);
			
		}
		
	}
	//............................................................................
	// END OF THE DESCENT INNER CLASS
	//............................................................................
	
	//............................................................................
	// LANDING INNER CLASS
	//............................................................................
	public class CalcLanding {

		public void performLandingSimulation(Amount<Mass> landingMass) {

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
			
			_theLandingCalculator = new LandingCalc(
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
					_cLmaxLanding,
					_cLZeroLanding,
					_cLAlphaLanding.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
					_thePerformanceInterface.getTheOperatingConditions().getThrottleGroundIdleLanding(),
					_thePerformanceInterface.getFreeRollDuration()
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theLandingCalculator.calculateLandingDistance();
			
			// Distances:
			_groundRollDistanceLanding = _theLandingCalculator.getsGround();
			_flareDistanceLanding = _theLandingCalculator.getsFlare();
			_airborneDistanceLanding = _theLandingCalculator.getsApproach();
			_landingDistance = _theLandingCalculator.getsTotal();
			_landingDistanceFAR25 = _theLandingCalculator.getsTotal().divide(0.6);
			
			// Velocities:
			_vStallLanding = _theLandingCalculator.getvSLanding();
			_vTouchDown = _theLandingCalculator.getvTD();
			_vFlare = _theLandingCalculator.getvFlare();
			_vApproach = _theLandingCalculator.getvA();
			
			// Duration:
			_landingDuration = _theLandingCalculator.getTime().get(_theLandingCalculator.getTime().size()-1);
			
		}
		public void plotLandingPerformance(String landingFolderPath) {
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.LANDING_SIMULATIONS)) {
				try {
					_theLandingCalculator.createLandingCharts(landingFolderPath);
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
		
		public void fromMissionProfile() {
			
			_thePayloadRangeCalculator = new PayloadRangeCalcMissionProfile(
					_thePerformanceInterface.getTheAircraft(),
					_thePerformanceInterface.getTheOperatingConditions(),
					_thePerformanceInterface.getTakeOffMissionAltitude(),
					_thePerformanceInterface.getMaximumTakeOffMass(),
					_thePerformanceInterface.getOperatingEmptyMass(),
					_thePerformanceInterface.getMaximumFuelMass(),
					_thePerformanceInterface.getSinglePassengerMass(),
					_thePerformanceInterface.getFirstGuessCruiseLength(),
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
					MyArrayUtils.getMax(_polarCLCruise),
					_cLmaxTakeOff,
					_cLAlphaTakeOff,
					_cLZeroTakeOff,
					_cLmaxLanding,
					_cLZeroLanding,
					_polarCLClimb,
					_polarCDClimb,
					_polarCLCruise,
					_polarCDCruise,
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
					);
			
			//------------------------------------------------------------
			// CRUISE MACH AND ALTITUDE
			_thePayloadRangeCalculator.createPayloadRange();
			
			_rangeAtMaxPayload = _thePayloadRangeCalculator.getRangeAtMaxPayload();
			_rangeAtDesignPayload = _thePayloadRangeCalculator.getRangeAtDesignPayload();
			_rangeAtMaxFuel = _thePayloadRangeCalculator.getRangeAtMaxFuel();	
			_rangeAtZeroPayload = _thePayloadRangeCalculator.getRangeAtZeroPayload();
			_takeOffMassAtZeroPayload = _thePayloadRangeCalculator.getTakeOffMassZeroPayload();
			_maxPayload = _thePayloadRangeCalculator.getMaxPayload();
			_designPayload = _thePayloadRangeCalculator.getDesignPayload();
			_payloadAtMaxFuel = _thePayloadRangeCalculator.getPayloadAtMaxFuel();
			_passengersNumberAtMaxPayload = _thePayloadRangeCalculator.getPassengersNumberAtMaxPayload();
			_passengersNumberAtDesignPayload = _thePayloadRangeCalculator.getPassengersNumberAtDesignPayload();
			_passengersNumberAtMaxFuel = _thePayloadRangeCalculator.getPassengersNumberAtMaxFuel();
			_requiredMassAtMaxPayload = _thePayloadRangeCalculator.getRequiredMassAtMaxPayload();
			_requiredMassAtDesignPayload = _thePayloadRangeCalculator.getRequiredMassAtDesignPayload();
			
			_rangeArray = _thePayloadRangeCalculator.getRangeArray();
			_payloadArray = _thePayloadRangeCalculator.getPayloadArray();
			
		}
		public void plotPayloadRange(String payloadRangeFolderPath) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.PAYLOAD_RANGE)) {
				_thePayloadRangeCalculator.createPayloadRangeChart(payloadRangeFolderPath);
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
		
		public void fromRegulations() {
			
			_theEnvelopeCalculator = new FlightManeuveringEnvelopeCalc(
					_thePerformanceInterface.getTheAircraft().getRegulations(),
					_thePerformanceInterface.getTheAircraft().getTypeVehicle(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance(),
					_cLmaxClean,
					_cLmaxLanding,
					_cLmaxInverted,
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPositiveLimitLoadFactor(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getNegativeLimitLoadFactor(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVMaxCruise(),
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVDive(),
					_cLAlphaClean.to(SI.RADIAN),
					_thePerformanceInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(),
					_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
					_thePerformanceInterface.getMaximumTakeOffMass(),
					_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
					_thePerformanceInterface.getTheAircraft().getWing().getSurface()
					);
			
			_theEnvelopeCalculator.calculateManeuveringEnvelope();
			
			_stallSpeedClean = _theEnvelopeCalculator.getStallSpeedClean();
			_stallSpeedFullFlap = _theEnvelopeCalculator.getStallSpeedFullFlap();
			_stallSpeedInverted = _theEnvelopeCalculator.getStallSpeedInverted();
			_maneuveringSpeed = _theEnvelopeCalculator.getManeuveringSpeed();
			_maneuveringFlapSpeed = _theEnvelopeCalculator.getManeuveringFlapSpeed();
			_maneuveringSpeedInverted = _theEnvelopeCalculator.getManeuveringSpeedInverted();
			_designFlapSpeed = _theEnvelopeCalculator.getDesignFlapSpeed();
			_positiveLoadFactorManeuveringSpeed = _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeed();
			_positiveLoadFactorCruisingSpeed = _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeed();
			_positiveLoadFactorDiveSpeed = _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeed();
			_positiveLoadFactorDesignFlapSpeed = _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed();
			_negativeLoadFactorManeuveringSpeedInverted = _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInverted();
			_negativeLoadFactorCruisingSpeed = _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeed();
			_negativeLoadFactorDiveSpeed = _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeed();
			_positiveLoadFactorManeuveringSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeedWithGust();
			_positiveLoadFactorCruisingSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeedWithGust();
			_positiveLoadFactorDiveSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeedWithGust();
			_positiveLoadFactorDesignFlapSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeedWithGust();
			_negativeLoadFactorManeuveringSpeedInvertedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInvertedWithGust();
			_negativeLoadFactorCruisingSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeedWithGust();
			_negativeLoadFactorDiveSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeedWithGust();
			_negativeLoadFactorDesignFlapSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorDesignFlapSpeedWithGust();
		
		}
		public void plotVnDiagram(String maneuveringEnvelopeFolderPath) {	
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM)) 
				_theEnvelopeCalculator.plotManeuveringEnvelope(maneuveringEnvelopeFolderPath);;
			
		}
		
	}
	//............................................................................
	// END OF THE FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................	
	
	//............................................................................
	// MISSION PROFILE INNER CLASS
	//............................................................................
	public class CalcMissionProfile {
		
		public void calculateMissionProfileIterative() {
			
			_theMissionProfileCalculator = new MissionProfileCalc(
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
					MyArrayUtils.getMax(_polarCLCruise),
					_cLmaxTakeOff,
					_cLAlphaTakeOff,
					_cLZeroTakeOff,
					_cLmaxLanding,
					_cLZeroLanding,
					_polarCLClimb,
					_polarCDClimb,
					_polarCLCruise,
					_polarCDCruise,
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
					);
				
			_theMissionProfileCalculator.calculateProfiles();
			
			_altitudeList = _theMissionProfileCalculator.getAltitudeList();
			_rangeList = _theMissionProfileCalculator.getRangeList();
			_timeList = _theMissionProfileCalculator.getTimeList();
			_fuelUsedList = _theMissionProfileCalculator.getFuelUsedList();
			_massList = _theMissionProfileCalculator.getMassList();
			_speedTASMissionList = _theMissionProfileCalculator.getSpeedTASMissionList();
			_machMissionList = _theMissionProfileCalculator.getMachMissionList();
			_liftingCoefficientMissionList = _theMissionProfileCalculator.getLiftingCoefficientMissionList();
			_dragCoefficientMissionList = _theMissionProfileCalculator.getDragCoefficientMissionList();
			_efficiencyMissionList = _theMissionProfileCalculator.getEfficiencyMissionList();
			_thrustMissionList = _theMissionProfileCalculator.getThrustMissionList();
			_dragMissionList = _theMissionProfileCalculator.getDragMissionList();
			
			_initialFuelMass = _theMissionProfileCalculator.getInitialFuelMass();
			_totalFuelUsed = _theMissionProfileCalculator.getTotalFuelUsed();
			_totalMissionTime = _theMissionProfileCalculator.getTotalMissionTime();
			_initialMissionMass = _theMissionProfileCalculator.getInitialMissionMass();
			_endMissionMass = _theMissionProfileCalculator.getEndMissionMass();
			
		}
		
		public void plotProfiles(String missionProfilesFolderPath) {
			
			_theMissionProfileCalculator.plotProfiles(
					_thePerformanceInterface.getPlotList(),
					missionProfilesFolderPath
					);
			
		}
		
	}
	//............................................................................
	// END OF THE MISSION PROFILE INNER CLASS
	//............................................................................	
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------

	public Amount<Length> getTakeOffDistanceAEO() {
		return _takeOffDistanceAEO;
	}

	public void setTakeOffDistanceAEO(Amount<Length> _takeOffDistanceAEO) {
		this._takeOffDistanceAEO = _takeOffDistanceAEO;
	}

	public Amount<Length> getTakeOffDistanceFAR25() {
		return _takeOffDistanceFAR25;
	}

	public void setTakeOffDistanceFAR25(Amount<Length> _takeOffDistanceFAR25) {
		this._takeOffDistanceFAR25 = _takeOffDistanceFAR25;
	}

	public Amount<Length> getBalancedFieldLength() {
		return _balancedFieldLength;
	}

	public void setBalancedFieldLength(Amount<Length> _balancedFieldLength) {
		this._balancedFieldLength = _balancedFieldLength;
	}

	public Amount<Length> getGroundRollDistanceTakeOff() {
		return _groundRollDistanceTakeOff;
	}

	public void setGroundRollDistanceTakeOff(Amount<Length> _groundRoll) {
		this._groundRollDistanceTakeOff = _groundRoll;
	}

	public Amount<Length> getRotationDistanceTakeOff() {
		return _rotationDistanceTakeOff;
	}

	public void setRotationDistanceTakeOff(Amount<Length> _rotation) {
		this._rotationDistanceTakeOff = _rotation;
	}

	public Amount<Length> getAirborneDistanceTakeOff() {
		return _airborneDistanceTakeOff;
	}

	public void setAirborneDistanceTakeOff(Amount<Length> _airborne) {
		this._airborneDistanceTakeOff = _airborne;
	}

	public Amount<Velocity> getVStallTakeOff() {
		return _vStallTakeOff;
	}

	public void setVStallTakeOff(Amount<Velocity> _vsTO) {
		this._vStallTakeOff = _vsTO;
	}

	public Amount<Velocity> getVRotation() {
		return _vRotation;
	}

	public void setVRotation(Amount<Velocity> _vRotation) {
		this._vRotation = _vRotation;
	}

	public Amount<Velocity> getVLiftOff() {
		return _vLiftOff;
	}

	public void setVLiftOff(Amount<Velocity> _vLiftOff) {
		this._vLiftOff = _vLiftOff;
	}

	public Amount<Velocity> getV1() {
		return _v1;
	}

	public void setV1(Amount<Velocity> _v1) {
		this._v1 = _v1;
	}

	public Amount<Velocity> getV2() {
		return _v2;
	}

	public void setV2(Amount<Velocity> _v2) {
		this._v2 = _v2;
	}

	public Amount<Length> getLandingDistance() {
		return _landingDistance;
	}

	public void setLandingDistance(Amount<Length> _landingDistance) {
		this._landingDistance = _landingDistance;
	}

	public Amount<Length> getLandingDistanceFAR25() {
		return _landingDistanceFAR25;
	}

	public void setLandingDistanceFAR25(Amount<Length> _landingDistanceFAR25) {
		this._landingDistanceFAR25 = _landingDistanceFAR25;
	}

	public Amount<Length> getGroundRollDistanceLanding() {
		return _groundRollDistanceLanding;
	}

	public void setGroundRollDistanceLanding(Amount<Length> _groundRollDistanceLanding) {
		this._groundRollDistanceLanding = _groundRollDistanceLanding;
	}

	public Amount<Length> getFlareDistanceLanding() {
		return _flareDistanceLanding;
	}

	public void setFlareDistanceLanding(Amount<Length> _rotationDistanceLanding) {
		this._flareDistanceLanding = _rotationDistanceLanding;
	}

	public Amount<Length> getAirborneDistanceLanding() {
		return _airborneDistanceLanding;
	}

	public void setAirborneDistanceLanding(Amount<Length> _airborneDistanceLanding) {
		this._airborneDistanceLanding = _airborneDistanceLanding;
	}

	public Amount<Velocity> getVStallLanding() {
		return _vStallLanding;
	}

	public void setVStallLanding(Amount<Velocity> _vStallLanding) {
		this._vStallLanding = _vStallLanding;
	}

	public Amount<Velocity> getVApproach() {
		return _vApproach;
	}

	public void setVApproach(Amount<Velocity> _vApproach) {
		this._vApproach = _vApproach;
	}

	public Amount<Velocity> getVFlare() {
		return _vFlare;
	}

	public void setVFlare(Amount<Velocity> _vFlare) {
		this._vFlare = _vFlare;
	}

	public Amount<Velocity> getVTouchDown() {
		return _vTouchDown;
	}

	public void setVTouchDown(Amount<Velocity> _vTouchDown) {
		this._vTouchDown = _vTouchDown;
	}

	public Amount<Velocity> getStallSpeedFullFlap() {
		return _stallSpeedFullFlap;
	}

	public void setStallSpeedFullFlap(Amount<Velocity> _stallSpeedFullFlap) {
		this._stallSpeedFullFlap = _stallSpeedFullFlap;
	}

	public Amount<Velocity> getStallSpeedClean() {
		return _stallSpeedClean;
	}

	public void setStallSpeedClean(Amount<Velocity> _stallSpeedClean) {
		this._stallSpeedClean = _stallSpeedClean;
	}

	public Amount<Velocity> getStallSpeedInverted() {
		return _stallSpeedInverted;
	}

	public void setStallSpeedInverted(Amount<Velocity> _stallSpeedInverted) {
		this._stallSpeedInverted = _stallSpeedInverted;
	}

	public Amount<Velocity> getManeuveringSpeed() {
		return _maneuveringSpeed;
	}

	public void setManeuveringSpeed(Amount<Velocity> _maneuveringSpeed) {
		this._maneuveringSpeed = _maneuveringSpeed;
	}

	public Amount<Velocity> getManeuveringFlapSpeed() {
		return _maneuveringFlapSpeed;
	}

	public void setManeuveringFlapSpeed(Amount<Velocity> _maneuveringFlapSpeed) {
		this._maneuveringFlapSpeed = _maneuveringFlapSpeed;
	}

	public Amount<Velocity> getManeuveringSpeedInverted() {
		return _maneuveringSpeedInverted;
	}

	public void setManeuveringSpeedInverted(Amount<Velocity> _maneuveringSpeedInverted) {
		this._maneuveringSpeedInverted = _maneuveringSpeedInverted;
	}

	public Amount<Velocity> getDesignFlapSpeed() {
		return _designFlapSpeed;
	}

	public void setDesignFlapSpeed(Amount<Velocity> _designFlapSpeed) {
		this._designFlapSpeed = _designFlapSpeed;
	}

	public Double getPositiveLoadFactorManeuveringSpeed() {
		return _positiveLoadFactorManeuveringSpeed;
	}

	public void setPositiveLoadFactorManeuveringSpeed(Double _positiveLoadFactorManeuveringSpeed) {
		this._positiveLoadFactorManeuveringSpeed = _positiveLoadFactorManeuveringSpeed;
	}

	public Double getPositiveLoadFactorCruisingSpeed() {
		return _positiveLoadFactorCruisingSpeed;
	}

	public void setPositiveLoadFactorCruisingSpeed(Double _positiveLoadFactorCruisingSpeed) {
		this._positiveLoadFactorCruisingSpeed = _positiveLoadFactorCruisingSpeed;
	}

	public Double getPositiveLoadFactorDiveSpeed() {
		return _positiveLoadFactorDiveSpeed;
	}

	public void setPositiveLoadFactorDiveSpeed(Double _positiveLoadFactorDiveSpeed) {
		this._positiveLoadFactorDiveSpeed = _positiveLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorDesignFlapSpeed() {
		return _positiveLoadFactorDesignFlapSpeed;
	}

	public void setPositiveLoadFactorDesignFlapSpeed(Double _positiveLoadFactorDesignFlapSpeed) {
		this._positiveLoadFactorDesignFlapSpeed = _positiveLoadFactorDesignFlapSpeed;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInverted() {
		return _negativeLoadFactorManeuveringSpeedInverted;
	}

	public void setNegativeLoadFactorManeuveringSpeedInverted(Double _negativeLoadFactorManeuveringSpeedInverted) {
		this._negativeLoadFactorManeuveringSpeedInverted = _negativeLoadFactorManeuveringSpeedInverted;
	}

	public Double getNegativeLoadFactorCruisingSpeed() {
		return _negativeLoadFactorCruisingSpeed;
	}

	public void setNegativeLoadFactorCruisingSpeed(Double _negativeLoadFactorCruisingSpeed) {
		this._negativeLoadFactorCruisingSpeed = _negativeLoadFactorCruisingSpeed;
	}

	public Double getNegativeLoadFactorDiveSpeed() {
		return _negativeLoadFactorDiveSpeed;
	}

	public void setNegativeLoadFactorDiveSpeed(Double _negativeLoadFactorDiveSpeed) {
		this._negativeLoadFactorDiveSpeed = _negativeLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorManeuveringSpeedWithGust() {
		return _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public void setPositiveLoadFactorManeuveringSpeedWithGust(Double _positiveLoadFactorManeuveringSpeedWithGust) {
		this._positiveLoadFactorManeuveringSpeedWithGust = _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public Double getPositiveLoadFactorCruisingSpeedWithGust() {
		return _positiveLoadFactorCruisingSpeedWithGust;
	}

	public void setPositiveLoadFactorCruisingSpeedWithGust(Double _positiveLoadFactorCruisingSpeedWithGust) {
		this._positiveLoadFactorCruisingSpeedWithGust = _positiveLoadFactorCruisingSpeedWithGust;
	}

	public Double getPositiveLoadFactorDiveSpeedWithGust() {
		return _positiveLoadFactorDiveSpeedWithGust;
	}

	public void setPositiveLoadFactorDiveSpeedWithGust(Double _positiveLoadFactorDiveSpeedWithGust) {
		this._positiveLoadFactorDiveSpeedWithGust = _positiveLoadFactorDiveSpeedWithGust;
	}

	public Double getPositiveLoadFactorDesignFlapSpeedWithGust() {
		return _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public void setPositiveLoadFactorDesignFlapSpeedWithGust(Double _positiveLoadFactorDesignFlapSpeedWithGust) {
		this._positiveLoadFactorDesignFlapSpeedWithGust = _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInvertedWithGust() {
		return _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGust(
			Double _negativeLoadFactorManeuveringSpeedInvertedWithGust) {
		this._negativeLoadFactorManeuveringSpeedInvertedWithGust = _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	}

	public Double getNegativeLoadFactorCruisingSpeedWithGust() {
		return _negativeLoadFactorCruisingSpeedWithGust;
	}

	public void setNegativeLoadFactorCruisingSpeedWithGust(Double _negativeLoadFactorCruisingSpeedWithGust) {
		this._negativeLoadFactorCruisingSpeedWithGust = _negativeLoadFactorCruisingSpeedWithGust;
	}

	public Double getNegativeLoadFactorDiveSpeedWithGust() {
		return _negativeLoadFactorDiveSpeedWithGust;
	}

	public void setNegativeLoadFactorDiveSpeedWithGust(Double _negativeLoadFactorDiveSpeedWithGust) {
		this._negativeLoadFactorDiveSpeedWithGust = _negativeLoadFactorDiveSpeedWithGust;
	}

	public Double getNegativeLoadFactorDesignFlapSpeedWithGust() {
		return _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public void setNegativeLoadFactorDesignFlapSpeedWithGust(Double _negativeLoadFactorDesignFlapSpeedWithGust) {
		this._negativeLoadFactorDesignFlapSpeedWithGust = _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public TakeOffCalc getTheTakeOffCalculator() {
		return _theTakeOffCalculator;
	}

	public void setTheTakeOffCalculator(TakeOffCalc _theTakeOffCalculator) {
		this._theTakeOffCalculator = _theTakeOffCalculator;
	}

	public LandingCalc getTheLandingCalculator() {
		return _theLandingCalculator;
	}

	public void setTheLandingCalculator(LandingCalc _theLandingCalculator) {
		this._theLandingCalculator = _theLandingCalculator;
	}

	public FlightManeuveringEnvelopeCalc getTheEnvelopeCalculator() {
		return _theEnvelopeCalculator;
	}

	public void setTheEnvelopeCalculator(FlightManeuveringEnvelopeCalc _theEnvelopeCalculator) {
		this._theEnvelopeCalculator = _theEnvelopeCalculator;
	}

	public Amount<Mass> getPayloadAtMaxFuel() {
		return _payloadAtMaxFuel;
	}

	public void setPayloadAtMaxFuel(Amount<Mass> payloadAtMaxFuel) {
		this._payloadAtMaxFuel = payloadAtMaxFuel;
	}

	public List<Double> getPayloadArray() {
		return _payloadArray;
	}

	public void setPayloadArray(List<Double> payloadArray) {
		this._payloadArray = payloadArray;
	}

	public double[][] getRangeMatrix() {
		return _rangeMatrix;
	}

	public void setRangeMatrix(double[][] rangeMatrix) {
		this._rangeMatrix = rangeMatrix;
	}

	public double[][] getPayloadMatrix() {
		return _payloadMatrix;
	}

	public void setPayloadMatrix(double[][] payloadMatrix) {
		this._payloadMatrix = payloadMatrix;
	}

	public Amount<Length> getAbsoluteCeilingAEO() {
		return _absoluteCeilingAEO;
	}

	public void setAbsoluteCeilingAEO(Amount<Length> _absoluteCeiling) {
		this._absoluteCeilingAEO = _absoluteCeiling;
	}

	public Amount<Length> getServiceCeilingAEO() {
		return _serviceCeilingAEO;
	}

	public void setServiceCeilingAEO(Amount<Length> _serviceCeiling) {
		this._serviceCeilingAEO = _serviceCeiling;
	}

	public Amount<Duration> getMinimumClimbTimeAEO() {
		return _minimumClimbTimeAEO;
	}

	public void setMinimumClimbTimeAEO(Amount<Duration> _minimumClimbTime) {
		this._minimumClimbTimeAEO = _minimumClimbTime;
	}

	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedAEO() {
		return _climbTimeAtSpecificClimbSpeedAEO;
	}

	public void setClimbTimeAtSpecificClimbSpeedAEO(Amount<Duration> _climbTimeAtSpecificClimbSpeed) {
		this._climbTimeAtSpecificClimbSpeedAEO = _climbTimeAtSpecificClimbSpeed;
	}

	public Amount<Length> getAbsoluteCeilingOEI() {
		return _absoluteCeilingOEI;
	}

	public void setAbsoluteCeilingOEI(Amount<Length> _absoluteCeilingOEI) {
		this._absoluteCeilingOEI = _absoluteCeilingOEI;
	}

	public Amount<Length> getServiceCeilingOEI() {
		return _serviceCeilingOEI;
	}

	public void setServiceCeilingOEI(Amount<Length> _serviceCeilingOEI) {
		this._serviceCeilingOEI = _serviceCeilingOEI;
	}

	public List<RCMap> getRCMapAEO() {
		return _rcMapAEO;
	}

	public void setRCMapAEO(List<RCMap> _rcMapAEO) {
		this._rcMapAEO = _rcMapAEO;
	}

	public List<RCMap> getRCMapOEI() {
		return _rcMapOEI;
	}

	public void setRCMapOEI(List<RCMap> _rcMapOEI) {
		this._rcMapOEI = _rcMapOEI;
	}

	public CeilingMap getCeilingMapAEO() {
		return _ceilingMapAEO;
	}

	public void setCeilingMapAEO(CeilingMap _ceilingMapAEO) {
		this._ceilingMapAEO = _ceilingMapAEO;
	}

	public CeilingMap getCeilingMapOEI() {
		return _ceilingMapOEI;
	}

	public void setCeilingMapOEI(CeilingMap _ceilingMapOEI) {
		this._ceilingMapOEI = _ceilingMapOEI;
	}

	public List<DragMap> getDragListAltitudeParameterization() {
		return _dragListAltitudeParameterization;
	}

	public void setDragListAltitudeParameterization(List<DragMap> _dragListAltitudeParameterization) {
		this._dragListAltitudeParameterization = _dragListAltitudeParameterization;
	}

	public List<ThrustMap> getThrustListAltitudeParameterization() {
		return _thrustListAltitudeParameterization;
	}

	public void setThrustListAltitudeParameterization(List<ThrustMap> _thrustListAltitudeParameterization) {
		this._thrustListAltitudeParameterization = _thrustListAltitudeParameterization;
	}

	public List<DragMap> getDragListWeightParameterization() {
		return _dragListWeightParameterization;
	}

	public void setDragListWeightParameterization(List<DragMap> _dragListWeightParameterization) {
		this._dragListWeightParameterization = _dragListWeightParameterization;
	}

	public List<ThrustMap> getThrustListWeightParameterization() {
		return _thrustListWeightParameterization;
	}

	public void setThrustListWeightParameterization(List<ThrustMap> _thrustListWeightParameterization) {
		this._thrustListWeightParameterization = _thrustListWeightParameterization;
	}

	public List<Amount<Force>> getWeightListCruise() {
		return _weightListCruise;
	}

	public void setWeightListCruise(List<Amount<Force>> _weightListCruise) {
		this._weightListCruise = _weightListCruise;
	}

	public Map<String, List<Double>> getEfficiencyMapAltitude() {
		return _efficiencyMapAltitude;
	}

	public void setEfficiencyMapAltitude(Map<String, List<Double>> _efficiencyMapAltitude) {
		this._efficiencyMapAltitude = _efficiencyMapAltitude;
	}

	public Map<String, List<Double>> getEfficiencyMapWeight() {
		return _efficiencyMapWeight;
	}

	public void setEfficiencyMapWeight(Map<String, List<Double>> _efficiencyMapWeight) {
		this._efficiencyMapWeight = _efficiencyMapWeight;
	}

	public List<SpecificRangeMap> getSpecificRangeMap() {
		return _specificRangeMap;
	}

	public void setSpecificRangeMap(List<SpecificRangeMap> _specificRangeMap) {
		this._specificRangeMap = _specificRangeMap;
	}

	public List<Amount<Length>> getAltitudeList() {
		return _altitudeList;
	}

	public void setAltitudeList(List<Amount<Length>> _altitudeList) {
		this._altitudeList = _altitudeList;
	}

	public List<Amount<Length>> getRangeList() {
		return _rangeList;
	}

	public void setRangeList(List<Amount<Length>> _rangeList) {
		this._rangeList = _rangeList;
	}

	public List<Amount<Duration>> getTimeList() {
		return _timeList;
	}

	public void setTimeList(List<Amount<Duration>> _timeList) {
		this._timeList = _timeList;
	}

	public List<Amount<Mass>> getFuelUsedList() {
		return _fuelUsedList;
	}

	public void setFuelUsedList(List<Amount<Mass>> _fuelUsedList) {
		this._fuelUsedList = _fuelUsedList;
	}

	public List<Amount<Mass>> getMassList() {
		return _massList;
	}

	public void setMassList(List<Amount<Mass>> _weightList) {
		this._massList = _weightList;
	}

	public Amount<Mass> getTotalFuelUsed() {
		return _totalFuelUsed;
	}

	public void setTotalFuelUsed(Amount<Mass> _totalFuelUsed) {
		this._totalFuelUsed = _totalFuelUsed;
	}

	public Amount<Duration> getTotalMissionTime() {
		return _totalMissionTime;
	}

	public void setTotalMissionTime(Amount<Duration> _totalMissionTime) {
		this._totalMissionTime = _totalMissionTime;
	}

	public Amount<Duration> getTakeOffDuration() {
		return _takeOffDuration;
	}

	public void setTakeOffDuration(Amount<Duration> _takeOffDuration) {
		this._takeOffDuration = _takeOffDuration;
	}

	public Amount<Duration> getLandingDuration() {
		return _landingDuration;
	}

	public void setLandingDuration(Amount<Duration> _landingDuration) {
		this._landingDuration = _landingDuration;
	}

	public List<DragMap> getDragListAEO() {
		return _dragListAEO;
	}

	public void setDragListAEO(List<DragMap> _dragListAEO) {
		this._dragListAEO = _dragListAEO;
	}

	public List<DragMap> getDragListOEI() {
		return _dragListOEI;
	}

	public void setDragListOEI(List<DragMap> _dragListOEI) {
		this._dragListOEI = _dragListOEI;
	}

	public List<ThrustMap> getThrustListAEO() {
		return _thrustListAEO;
	}

	public void setThrustListAEO(List<ThrustMap> _thrustListAEO) {
		this._thrustListAEO = _thrustListAEO;
	}

	public List<ThrustMap> getThrustListOEI() {
		return _thrustListOEI;
	}

	public void setThrustListOEI(List<ThrustMap> _thrustListOEI) {
		this._thrustListOEI = _thrustListOEI;
	}

	public List<Amount<Length>> getDescentLengths() {
		return _descentLengths;
	}

	public void setDescentLengths(List<Amount<Length>> _descentLength) {
		this._descentLengths = _descentLength;
	}

	public List<Amount<Duration>> getDescentTimes() {
		return _descentTimes;
	}

	public void setDescentTimes(List<Amount<Duration>> _descentTime) {
		this._descentTimes = _descentTime;
	}

	public List<Amount<Angle>> getDescentAngles() {
		return _descentAngles;
	}

	public void setDescentAngles(List<Amount<Angle>> _descentAngle) {
		this._descentAngles = _descentAngle;
	}

	public Amount<Length> getTotalDescentLength() {
		return _totalDescentLength;
	}

	public void setTotalDescentLength(Amount<Length> _totalDescentLength) {
		this._totalDescentLength = _totalDescentLength;
	}

	public Amount<Duration> getTotalDescentTime() {
		return _totalDescentTime;
	}

	public void setTotalDescentTime(Amount<Duration> _totalDescentTime) {
		this._totalDescentTime = _totalDescentTime;
	}

	public Amount<Velocity> getMaxSpeesTASAtCruiseAltitude() {
		return _maxSpeesTASAtCruiseAltitude;
	}

	public void setMaxSpeesTASAtCruiseAltitude(Amount<Velocity> _maxSpeesTASAtCruiseAltitude) {
		this._maxSpeesTASAtCruiseAltitude = _maxSpeesTASAtCruiseAltitude;
	}

	public Amount<Velocity> getMinSpeesTASAtCruiseAltitude() {
		return _minSpeesTASAtCruiseAltitude;
	}

	public void setMinSpeesTASAtCruiseAltitude(Amount<Velocity> _minSpeesTASAtCruiseAltitude) {
		this._minSpeesTASAtCruiseAltitude = _minSpeesTASAtCruiseAltitude;
	}

	public Amount<Velocity> getMaxSpeesCASAtCruiseAltitude() {
		return _maxSpeesCASAtCruiseAltitude;
	}

	public void setMaxSpeesCASAtCruiseAltitude(Amount<Velocity> _maxSpeesCASAtCruiseAltitude) {
		this._maxSpeesCASAtCruiseAltitude = _maxSpeesCASAtCruiseAltitude;
	}

	public Amount<Velocity> getMinSpeesCASAtCruiseAltitude() {
		return _minSpeesCASAtCruiseAltitude;
	}

	public void setMinSpeesCASAtCruiseAltitude(Amount<Velocity> _minSpeesCASAtCruiseAltitude) {
		this._minSpeesCASAtCruiseAltitude = _minSpeesCASAtCruiseAltitude;
	}

	public Double getMaxMachAtCruiseAltitude() {
		return _maxMachAtCruiseAltitude;
	}

	public void setMaxMachAtCruiseAltitude(Double _maxMachAtCruiseAltitude) {
		this._maxMachAtCruiseAltitude = _maxMachAtCruiseAltitude;
	}

	public Double getMinMachAtCruiseAltitude() {
		return _minMachAtCruiseAltitude;
	}

	public void setMinMachAtCruiseAltitude(Double _minMachAtCruiseAltitude) {
		this._minMachAtCruiseAltitude = _minMachAtCruiseAltitude;
	}

	public Double getEfficiencyAtCruiseAltitudeAndMach() {
		return _efficiencyAtCruiseAltitudeAndMach;
	}

	public void setEfficiencyAtCruiseAltitudeAndMach(Double _efficiencyAtCruiseAltitudeAndMach) {
		this._efficiencyAtCruiseAltitudeAndMach = _efficiencyAtCruiseAltitudeAndMach;
	}

	public Amount<Force> getThrustAtCruiseAltitudeAndMach() {
		return _thrustAtCruiseAltitudeAndMach;
	}

	public void setThrustAtCruiseAltitudeAndMach(Amount<Force> _thrustAtCruiseAltitudeAndMach) {
		this._thrustAtCruiseAltitudeAndMach = _thrustAtCruiseAltitudeAndMach;
	}

	public Amount<Force> getDragAtCruiseAltitudeAndMach() {
		return _dragAtCruiseAltitudeAndMach;
	}

	public void setDragAtCruiseAltitudeAndMach(Amount<Force> _dragAtCruiseAltitudeAndMach) {
		this._dragAtCruiseAltitudeAndMach = _dragAtCruiseAltitudeAndMach;
	}

	public Amount<Power> getPowerAvailableAtCruiseAltitudeAndMach() {
		return _powerAvailableAtCruiseAltitudeAndMach;
	}

	public void setPowerAvailableAtCruiseAltitudeAndMach(Amount<Power> _powerAvailableAtCruiseAltitudeAndMach) {
		this._powerAvailableAtCruiseAltitudeAndMach = _powerAvailableAtCruiseAltitudeAndMach;
	}

	public Amount<Power> getPowerNeededAtCruiseAltitudeAndMach() {
		return _powerNeededAtCruiseAltitudeAndMach;
	}

	public void setPowerNeededAtCruiseAltitudeAndMach(Amount<Power> _powerNeededAtCruiseAltitudeAndMach) {
		this._powerNeededAtCruiseAltitudeAndMach = _powerNeededAtCruiseAltitudeAndMach;
	}

	public Amount<Mass> getEndMissionMass() {
		return _endMissionMass;
	}

	public void setEndMissionMass(Amount<Mass> _endMissionMass) {
		this._endMissionMass = _endMissionMass;
	}

	public Amount<Velocity> getVMC() {
		return _vMC;
	}

	public void setVMC(Amount<Velocity> _vMC) {
		this._vMC = _vMC;
	}

	public double[] getThrustMomentOEI() {
		return _thrustMomentOEI;
	}

	public void setThrustMomentOEI(double[] _thrustMomentOEI) {
		this._thrustMomentOEI = _thrustMomentOEI;
	}

	public double[] getYawingMomentOEI() {
		return _yawingMomentOEI;
	}

	public void setYawingMomentOEI(double[] _yawingMomentOEI) {
		this._yawingMomentOEI = _yawingMomentOEI;
	}

	public ClimbCalc getTheClimbCalculator() {
		return _theClimbCalculator;
	}

	public void setTheClimbCalculator(ClimbCalc _theClimbCalculator) {
		this._theClimbCalculator = _theClimbCalculator;
	}

	public MissionProfileCalc getTheMissionProfileCalculator() {
		return _theMissionProfileCalculator;
	}

	public void setTheMissionProfileCalculator(MissionProfileCalc _theMissionProfileCalculator) {
		this._theMissionProfileCalculator = _theMissionProfileCalculator;
	}

	public DescentCalc getTheDescentCalculator() {
		return _theDescentCalculator;
	}

	public void setTheDescentCalculator(DescentCalc _theDescentCalculator) {
		this._theDescentCalculator = _theDescentCalculator;
	}

	public Amount<Mass> getInitialMissionMass() {
		return _initialMissionMass;
	}

	public void setInitialMissionMass(Amount<Mass> _initialMissionMass) {
		this._initialMissionMass = _initialMissionMass;
	}

	public Amount<Mass> getInitialFuelMass() {
		return _initialFuelMass;
	}

	public void setInitialFuelMass(Amount<Mass> _initialFuelMass) {
		this._initialFuelMass = _initialFuelMass;
	}

	public Amount<Length> getRangeAtMaxPayload() {
		return _rangeAtMaxPayload;
	}

	public void setRangeAtMaxPayload(Amount<Length> _rangeAtMaxPayload) {
		this._rangeAtMaxPayload = _rangeAtMaxPayload;
	}

	public Amount<Length> getRangeAtDesignPayload() {
		return _rangeAtDesignPayload;
	}

	public void setRangeAtDesignPayload(Amount<Length> _rangeAtDesignPayload) {
		this._rangeAtDesignPayload = _rangeAtDesignPayload;
	}

	public Amount<Length> getRangeAtMaxFuel() {
		return _rangeAtMaxFuel;
	}

	public void setRangeAtMaxFuel(Amount<Length> _rangeAtMaxFuel) {
		this._rangeAtMaxFuel = _rangeAtMaxFuel;
	}

	public Amount<Mass> getMaxPayload() {
		return _maxPayload;
	}

	public void setMaxPayload(Amount<Mass> _maxPayload) {
		this._maxPayload = _maxPayload;
	}

	public Amount<Mass> getDesignPayload() {
		return _designPayload;
	}

	public void setDesignPayload(Amount<Mass> _designPayload) {
		this._designPayload = _designPayload;
	}

	public Integer getPassengersNumberAtMaxPayload() {
		return _passengersNumberAtMaxPayload;
	}

	public void setPassengersNumberAtMaxPayload(Integer _passengersNumberAtMaxPayload) {
		this._passengersNumberAtMaxPayload = _passengersNumberAtMaxPayload;
	}

	public Integer getPassengersNumberAtDesignPayload() {
		return _passengersNumberAtDesignPayload;
	}

	public void setPassengersNumberAtDesignPayload(Integer _passengersNumberAtDesignPayload) {
		this._passengersNumberAtDesignPayload = _passengersNumberAtDesignPayload;
	}

	public Integer getPassengersNumberAtMaxFuel() {
		return _passengersNumberAtMaxFuel;
	}

	public void setPassengersNumberAtMaxFuel(Integer _passengersNumberAtMaxFuel) {
		this._passengersNumberAtMaxFuel = _passengersNumberAtMaxFuel;
	}

	public Amount<Mass> getRequiredMassAtMaxPayload() {
		return _requiredMassAtMaxPayload;
	}

	public void setRequiredMassAtMaxPayload(Amount<Mass> _requiredMassAtMaxPayload) {
		this._requiredMassAtMaxPayload = _requiredMassAtMaxPayload;
	}

	public Amount<Mass> getRequiredMassAtDesignPayload() {
		return _requiredMassAtDesignPayload;
	}

	public void setRequiredMassAtDesignPayload(Amount<Mass> _requiredMassAtDesignPayload) {
		this._requiredMassAtDesignPayload = _requiredMassAtDesignPayload;
	}

	public List<Amount<Length>> geRrangeArray() {
		return _rangeArray;
	}

	public void setRangeArray(List<Amount<Length>> _rangeArray) {
		this._rangeArray = _rangeArray;
	}

	public PayloadRangeCalcMissionProfile getThePayloadRangeCalculator() {
		return _thePayloadRangeCalculator;
	}

	public void setThePayloadRangeCalculator(PayloadRangeCalcMissionProfile _thePayloadRangeCalculator) {
		this._thePayloadRangeCalculator = _thePayloadRangeCalculator;
	}

	public Amount<Length> getRangeAtZeroPayload() {
		return _rangeAtZeroPayload;
	}

	public void setRangeAtZeroPayload(Amount<Length> _rangeAtZeroPayload) {
		this._rangeAtZeroPayload = _rangeAtZeroPayload;
	}

	public Amount<Mass> getTakeOffMassAtZeroPayload() {
		return _takeOffMassAtZeroPayload;
	}

	public void setTakeOffMassAtZeroPayload(Amount<Mass> _takeOffMassAtZeroPayload) {
		this._takeOffMassAtZeroPayload = _takeOffMassAtZeroPayload;
	}

	public Map<String, List<Double>> getEfficiencyMapAltitudeAEO() {
		return _efficiencyMapAltitudeAEO;
	}

	public void setEfficiencyMapAltitudeAEO(Map<String, List<Double>> _efficiencyMapAltitudeAEO) {
		this._efficiencyMapAltitudeAEO = _efficiencyMapAltitudeAEO;
	}

	public Map<String, List<Double>> getEfficiencyMapAltitudeOEI() {
		return _efficiencyMapAltitudeOEI;
	}

	public void setEfficiencyMapAltitudeOEI(Map<String, List<Double>> _efficiencyMapAltitudeOEI) {
		this._efficiencyMapAltitudeOEI = _efficiencyMapAltitudeOEI;
	}

	public List<Amount<Velocity>> getSpeedTASMissionList() {
		return _speedTASMissionList;
	}

	public void setSpeedTASMissionList(List<Amount<Velocity>> _speedTASMissionList) {
		this._speedTASMissionList = _speedTASMissionList;
	}

	public List<Double> getMachMissionList() {
		return _machMissionList;
	}

	public void setMachMissionList(List<Double> _machMissionList) {
		this._machMissionList = _machMissionList;
	}

	public List<Double> getLiftingCoefficientMissionList() {
		return _liftingCoefficientMissionList;
	}

	public void setLiftingCoefficientMissionList(List<Double> _liftingCoefficientMissionList) {
		this._liftingCoefficientMissionList = _liftingCoefficientMissionList;
	}

	public List<Double> getDragCoefficientMissionList() {
		return _dragCoefficientMissionList;
	}

	public void setDragCoefficientMissionList(List<Double> _dragCoefficientMissionList) {
		this._dragCoefficientMissionList = _dragCoefficientMissionList;
	}

	public List<Double> getEfficiencyMissionList() {
		return _efficiencyMissionList;
	}

	public void setEfficiencyMissionList(List<Double> _efficiencyMissionList) {
		this._efficiencyMissionList = _efficiencyMissionList;
	}

	public List<Amount<Force>> getThrustMissionList() {
		return _thrustMissionList;
	}

	public void setThrustMissionList(List<Amount<Force>> _thrustMissionList) {
		this._thrustMissionList = _thrustMissionList;
	}

	public List<Amount<Force>> getDragMissionList() {
		return _dragMissionList;
	}

	public void setDragMissionList(List<Amount<Force>> _dragMissionList) {
		this._dragMissionList = _dragMissionList;
	}

	public Amount<Mass> getFuelUsedDuringClimb() {
		return _fuelUsedDuringClimb;
	}

	public void setFuelUsedDuringClimb(Amount<Mass> _fuelUsedDuringClimb) {
		this._fuelUsedDuringClimb = _fuelUsedDuringClimb;
	}

	public Amount<Mass> getTotalDescentFuelUsed() {
		return _totalDescentFuelUsed;
	}

	public void setTotalDescentFuelUsed(Amount<Mass> _totalDescentFuelUsed) {
		this._totalDescentFuelUsed = _totalDescentFuelUsed;
	}

	public IACPerformanceManager getThePerformanceInterface() {
		return _thePerformanceInterface;
	}

	public void setThePerformanceInterface(IACPerformanceManager _thePerformanceInterface) {
		this._thePerformanceInterface = _thePerformanceInterface;
	}

}
