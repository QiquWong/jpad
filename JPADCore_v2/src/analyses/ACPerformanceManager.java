package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import calculators.performance.PayloadRangeCalc;
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
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

public class ACPerformanceManager implements IACPerformanceManger {

	/*
	 *******************************************************************************
	 * @author Vittorio Trifari
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private String _id;
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	//..............................................................................
	// Weights
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _maximumFuelMass;
	private Amount<Mass> _singlePassengerMass;
	//..............................................................................
	// Balance
	private Amount<Length> _xCGMaxAft;
	//..............................................................................
	// Aerodynamics
	private Double _cLmaxClean;
	private Amount<?> _cLAlphaClean;
	private Double _cLmaxTakeOff;
	private Amount<?> _cLAlphaTakeOff;
	private Double _cLZeroTakeOff;
	private Double _cLmaxLanding;
	private Amount<?> _cLAlphaLanding;
	private Double _cLZeroLanding;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Double[] _polarCLTakeOff;
	private Double[] _polarCDTakeOff;
	private Double[] _polarCLLanding;
	private Double[] _polarCDLanding;	
	//..............................................................................
	// Take-off & Landing
	private Amount<Velocity> _windSpeed;
	private Double _mu;
	private Double _muBrake;
	
	private Amount<Duration> _dtRotation;
	private Amount<Duration> _dtHold;
	private Amount<Angle> _alphaGround;
	private Amount<Length> _obstacleTakeOff;
	private Double _kRotation;
	private Double _kLiftOff;
	private Double _kCLmax;
	private Double _dragDueToEnigneFailure;
	private Double _kAlphaDot;
	private Double _alphaReductionRate;
	
	private Double _kLandingWeight;
	private Amount<Length> _obstacleLanding;
	private Amount<Angle> _thetaApproach;
	private Double _kApproach;
	private Double _kFlare;
	private Double _kTouchDown;
	private Amount<Duration> _freeRollDuration;
	//..............................................................................
	// Climb
	private Double _kClimbWeightAOE;
	private Double _kClimbWeightOEI;
	private Amount<Velocity> _climbSpeed;
	//..............................................................................
	// Cruise
	private List<Amount<Length>> _altitudeListCruise;
	private Double _kCruiseWeight;
	//..............................................................................
	// Fligth maneuvering and gust envelope
	private Double _cLmaxInverted;
	//..............................................................................
	// Descent
	private Amount<Velocity> _speedDescentCAS;
	private Amount<Velocity> _rateOfDescent;
	//..............................................................................
	// Mission Profile:
	private Amount<Length> _alternateCruiseLength;
	private Amount<Length> _alternateCruiseAltitude;
	private Double _alternateCruiseMachNumber;
	private Amount<Duration> _holdingDuration;
	private Amount<Length> _holdingAltitude;
	private Double _holdingMachNumber;
	private Double _fuelReserve;
	private Amount<Length> _firstGuessCruiseLength;
	private Amount<Mass> _firstGuessInitialMissionFuelMass;
	private Amount<Length> _takeOffMissionAltitude;
	private Double _cruiseMissionMachNumber;
	private Double _landingFuelFlow;
	//..............................................................................
	// Plot and Task Maps
	private List<PerformanceEnum> _taskList;
	private List<PerformancePlotEnum> _plotList;

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	// Take-Off
	private TakeOffCalc _theTakeOffCalculator;
	private Amount<Length> _takeOffDistanceAOE;
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
	private List<RCMap> _rcMapAOE;
	private List<RCMap> _rcMapOEI;
	private CeilingMap _ceilingMapAOE;
	private CeilingMap _ceilingMapOEI;
	private List<DragMap> _dragListAOE;
	private List<ThrustMap> _thrustListAOE;
	private List<DragMap> _dragListOEI;
	private List<ThrustMap> _thrustListOEI;
	
	private Amount<Velocity> _maxRateOfClimbAtCruiseAltitudeAOE;
	private Amount<Angle> _maxThetaAtCruiseAltitudeAOE;
	private Amount<Length> _absoluteCeilingAOE;
	private Amount<Length> _serviceCeilingAOE;
	private Amount<Duration> _minimumClimbTimeAOE;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedAOE;
	
	private Amount<Length> _absoluteCeilingOEI;
	private Amount<Length> _serviceCeilingOEI;
	private Amount<Duration> _minimumClimbTimeOEI;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedOEI;
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
	private Double _specificRangeAtCruiseAltitudeAndMach;
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
	private PayloadRangeCalc _thePayloadRangeCalculator;
	
	private Double _bestRangeMach;
	
	private Amount<Length> _rangeAtMaxPayloadBestRange;
	private Amount<Length> _rangeAtMaxPayloadCurrentMach;
	private Amount<Mass> _fuelMassAtMaxPayload;
	private Double _cLAtMaxPayloadBestRange;
	private Double _cLAtMaxPayloadCurrentMach;
	private Double _cDAtMaxPayloadBestRange;
	private Double _cDAtMaxPayloadCurrentMach;
	private Double _efficiencyAtMaxPayloadBestRange;
	private Double _efficiencyAtMaxPayloadCurrentMach;
	private Double _sfcAtMaxPayloadBestRange;
	private Double _sfcAtMaxPayloadCurrentMach;
	
	private Amount<Length> _rangeAtMaxFuelBestRange;
	private Amount<Length> _rangeAtMaxFuelCurrentMach;
	private Amount<Mass> _payloadAtMaxFuel;
	private Double _numberOfPassengersAtMaxFuel;
	private Double _cLAtMaxFuelBestRange;
	private Double _cLAtMaxFuelCurrentMach;
	private Double _cDAtMaxFuelBestRange;
	private Double _cDAtMaxFuelCurrentMach;
	private Double _efficiencyAtMaxFuelBestRange;
	private Double _efficiencyAtMaxFuelCurrentMach;
	private Double _sfcAtZeroPayloadBestRange;
	private Double _sfcAtMaxFuelCurrentMach;	
	
	private Amount<Length> _rangeAtZeroPayloadBestRange;
	private Amount<Length> _rangeAtZeroPayloadCurrentMach;
	private Amount<Mass> _takeOffMassAtZeroPayload;
	private Double _cLAtZeroPayloadBestRange;
	private Double _cLAtZeroPayloadCurrentMach;
	private Double _cDAtZeroPayloadBestRange;
	private Double _cDAtZeroPayloadCurrentMach;
	private Double _efficiencyAtZeroPayloadBestRange;
	private Double _efficiencyAtZeroPayloadCurrentMach;
	private Double _sfcAtMaxFuelBestRange;	
	private Double _sfcAtZeroPayloadCurrentMach;
	
	private List<Amount<Length>> _rangeArrayBestRange;
	private List<Amount<Length>> _rangeArrayCurrentMach;
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
	private Amount<Mass> _totalFuelUsed;
	private Amount<Duration> _totalMissionTime;
	private Amount<Mass> _endMissionMass;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACPerformanceCalculatorBuilder {
	
		// required parameters
		private String __id;
		private Aircraft __theAircraft;
		private OperatingConditions __theOperatingConditions;
		
		// optional parameters ... default
		//..............................................................................
		// Weights
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __maximumFuelMass;
		private Amount<Mass> __singlePassengerMass;
		//..............................................................................
		// Balance
		private Amount<Length> __xCGMaxAft;
		//..............................................................................
		// Aerodynamics
		private Double __cLmaxClean;
		private Amount<?> __cLAlphaClean;
		private Double __cLmaxTakeOff;
		private Amount<?> __cLAlphaTakeOff;
		private Double __cLZeroTakeOff;
		private Double __cLmaxLanding;
		private Amount<?> __cLAlphaLanding;
		private Double __cLZeroLanding;
		private Double[] __polarCLCruise;
		private Double[] __polarCDCruise;
		private Double[] __polarCLClimb;
		private Double[] __polarCDClimb;
		private Double[] __polarCLTakeOff;
		private Double[] __polarCDTakeOff;
		private Double[] __polarCLLanding;
		private Double[] __polarCDLanding;	
		//..............................................................................
		// Take-off & Landing
		private Amount<Velocity> __windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		private Double __mu = 0.03;
		private Double __muBrake = 0.4;
		
		private Amount<Duration> __dtRotation = Amount.valueOf(3.0, SI.SECOND);
		private Amount<Duration> __dtHold = Amount.valueOf(0.5, SI.SECOND);
		private Amount<Angle> __alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		private Amount<Length> __obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		private Double __kRotation = 1.05;
		private Double __kLiftOff = 1.1;
		private Double __kCLmax = 0.9;
		private Double __dragDueToEngineFailure = 0.0;
		private Double __kAlphaDot = 0.04;
		private Double __alphaReductionRate = -4.0; //(deg/s)
		
		private Double __kLandingWeight = 0.97;
		private Amount<Length> __obstacleLanding = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		private Amount<Angle> __thetaApproach = Amount.valueOf(3.0, NonSI.DEGREE_ANGLE);
		private Double __kApproach = 1.3;
		private Double __kFlare = 1.23;
		private Double __kTouchDown = 1.15;
		private Amount<Duration> __freeRollDuration = Amount.valueOf(2.0, SI.SECOND);
		//..............................................................................
		// Climb
		private Double __kClimbWeightAOE = 1.0;
		private Double __kClimbWeightOEI = 0.97;
		private Amount<Velocity> __climbSpeed;
		//..............................................................................
		// Cruise
		private List<Amount<Length>> __altitudeListCruise;
		private Double __kCruiseWeight = 0.98;
		//..............................................................................
		// Flight maneuvering and gust envelope
		private Double __cLmaxInverted = -1.0;
		//..............................................................................
		// Descent
		private Amount<Velocity> __speedDescent;
		private Amount<Velocity> __rateOfDescent;
		//..............................................................................
		// Mission profile
		private Amount<Length> __alternateCruiseLength;
		private Amount<Length> __alternateCruiseAltitude;
		private Double __alternateCruiseMachNumber;
		private Amount<Duration> __holdingDuration;
		private Amount<Length> __holdingAltitude;
		private Double __holdingMachNumber;
		private Double __fuelReserve;
		private Amount<Length> __firstGuessCruiseLength;
		private Amount<Mass> __firstGuessInitialMissionFuelMass;
		private Amount<Length> __takeOffMissionAltitude;
		private Double __cruiseMissionMachNumber;
		private Double __landingFuelFlow;
		//..............................................................................
		// Plot and Task Maps
		private List<PerformanceEnum> __taskList = new ArrayList<PerformanceEnum>();
		private List<PerformancePlotEnum> __plotList = new ArrayList<PerformancePlotEnum>();
		//..............................................................................
		
		public ACPerformanceCalculatorBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder aircraft(Aircraft aircraft) {
			this.__theAircraft = aircraft;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder operatingConditions(OperatingConditions operatingConditions) {
			this.__theOperatingConditions = operatingConditions;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder operatingEmptyMass(Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumFuelMass(Amount<Mass> maximumFuelMass) {
			this.__maximumFuelMass = maximumFuelMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder singlePassengerMass(Amount<Mass> singlePassengerMass) {
			this.__singlePassengerMass = singlePassengerMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder xCGMaxAft (Amount<Length> xCGMaxAft) {
			this.__xCGMaxAft = xCGMaxAft;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxClean(Double cLmaxClean) {
			this.__cLmaxClean = cLmaxClean;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLAlphaClean(Amount<?> cLAlphaClean) {
			this.__cLAlphaClean = cLAlphaClean;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLAlphaTakeOff(Amount<?> cLAlphaTakeOff) {
			this.__cLAlphaTakeOff = cLAlphaTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLAlphaLanding(Amount<?> cLAlphaLanding) {
			this.__cLAlphaLanding = cLAlphaLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLCruise (Double[] polarCLCruise) {
			this.__polarCLCruise = polarCLCruise;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDCruise (Double[] polarCDCruise) {
			this.__polarCDCruise = polarCDCruise;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLClimb (Double[] polarCLClimb) {
			this.__polarCLClimb = polarCLClimb;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDClimb (Double[] polarCDClimb) {
			this.__polarCDClimb = polarCDClimb;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLTakeOff (Double[] polarCLTakeOff) {
			this.__polarCLTakeOff = polarCLTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDTakeOff (Double[] polarCDTakeOff) {
			this.__polarCDTakeOff = polarCDTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLLanding (Double[] polarCLLanding) {
			this.__polarCLLanding = polarCLLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDLanding (Double[] polarCDLanding) {
			this.__polarCDLanding = polarCDLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxTakeOff(Double cLmaxTakeOff) {
			this.__cLmaxTakeOff = cLmaxTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLZeroTakeOff(Double cLZeroTakeOff) {
			this.__cLZeroTakeOff = cLZeroTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxLanding(Double cLmaxLanding) {
			this.__cLmaxLanding = cLmaxLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLZeroLanding(Double cLZeroLanding) {
			this.__cLZeroLanding = cLZeroLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder dtRotation(Amount<Duration> dtRotation) {
			this.__dtRotation = dtRotation;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder dtHold(Amount<Duration> dtHold) {
			this.__dtHold = dtHold;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alphaGround(Amount<Angle> alphaGround) {
			this.__alphaGround = alphaGround;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder windSpeed(Amount<Velocity> windSpeed) {
			this.__windSpeed = windSpeed;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder obstacleTakeOff(Amount<Length> obstacleTakeOff) {
			this.__obstacleTakeOff = obstacleTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder obstacleLanding(Amount<Length> obstacleLanding) {
			this.__obstacleLanding = obstacleLanding;
			return this;
		}

		public ACPerformanceCalculatorBuilder mu(Double mu) {
			this.__mu = mu;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder muBrake(Double muBrake) {
			this.__muBrake = muBrake;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kRotation(Double kRot) {
			this.__kRotation = kRot;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kLiftOff(Double kLO) {
			this.__kLiftOff = kLO;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kCLmax(Double kCLmax) {
			this.__kCLmax = kCLmax;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder dragDueToEngineFailure(Double dragFailure) {
			this.__dragDueToEngineFailure = dragFailure;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kAlphaDot(Double kAlphaDot) {
			this.__kAlphaDot = kAlphaDot;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alphaReductionRate(Double alphaRed) {
			this.__alphaReductionRate = alphaRed;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kLandingWeight (Double kLandingWeight) {
			this.__kLandingWeight = kLandingWeight;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder thetaApproach(Amount<Angle> thetaApproach) {
			this.__thetaApproach = thetaApproach;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kApproach(Double kA) {
			this.__kApproach = kA;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kFlare(Double kFlare) {
			this.__kFlare = kFlare;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kTouchDown(Double kTD) {
			this.__kTouchDown = kTD;
			return this;
		}

		public ACPerformanceCalculatorBuilder freeRollDuration(Amount<Duration> nFreeRoll) {
			this.__freeRollDuration = nFreeRoll;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxInverted(Double cLmaxInverted) {
			this.__cLmaxInverted = cLmaxInverted;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kClimbWeightAOE(Double kClimbWeightAOE) {
			this.__kClimbWeightAOE = kClimbWeightAOE;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kClimbWeightOEI(Double kClimbWeightOEI) {
			this.__kClimbWeightOEI = kClimbWeightOEI;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder climbSpeed(Amount<Velocity> climbSpeed) {
			this.__climbSpeed = climbSpeed;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder altitudeListCruise(List<Amount<Length>> altitudeListCruise) {
			this.__altitudeListCruise = altitudeListCruise;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kCruiseWeight(Double kCruiseWeight) {
			this.__kCruiseWeight = kCruiseWeight;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder rateOfDescent(Amount<Velocity> rateOfDescent) {
			this.__rateOfDescent = rateOfDescent;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder speedDescentCAS(Amount<Velocity> speedDescent) {
			this.__speedDescent = speedDescent;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alternateCruiseLength(Amount<Length> alternateCruiseLength) {
			this.__alternateCruiseLength = alternateCruiseLength;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alternateCruiseAltitude(Amount<Length> alternateCruiseAltitude) {
			this.__alternateCruiseAltitude = alternateCruiseAltitude;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alternateCruiseMachNumber(Double alternateCruiseMachNumber) {
			this.__alternateCruiseMachNumber = alternateCruiseMachNumber;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder holdingDuration(Amount<Duration> holdingDuration) {
			this.__holdingDuration = holdingDuration;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder holdingAltitude(Amount<Length> holdingAltitude) {
			this.__holdingAltitude = holdingAltitude;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder holdingMachNumber(Double holdingMachNumber) {
			this.__holdingMachNumber = holdingMachNumber;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder fuelReserve(Double fuelReserve) {
			this.__fuelReserve = fuelReserve;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder firstGuessCruiseLength(Amount<Length> firstGuessCruiseLength) {
			this.__firstGuessCruiseLength = firstGuessCruiseLength;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder firstGuessFuelMass(Amount<Mass> firstGuessFuelMass) {
			this.__firstGuessInitialMissionFuelMass = firstGuessFuelMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder takeOffMissionAltitude(Amount<Length> takeOffMissionAltitude) {
			this.__takeOffMissionAltitude = takeOffMissionAltitude;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cruiseMissionMachNumber(Double cruiseMissionMachNumber) {
			this.__cruiseMissionMachNumber = cruiseMissionMachNumber;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder landingFuelFlow(Double landingFuelFlow) {
			this.__landingFuelFlow = landingFuelFlow;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder taskList(List<PerformanceEnum> taskList) {
			this.__taskList = taskList;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder plotList(List<PerformancePlotEnum> plotList) {
			this.__plotList = plotList;
			return this;
		}
		
		public ACPerformanceManager build() {
			return new ACPerformanceManager(this);
		}
		
	}
	
	private ACPerformanceManager(ACPerformanceCalculatorBuilder builder) {
		
		this._id = builder.__id;
		this._theAircraft = builder.__theAircraft;
		this._theOperatingConditions = builder.__theOperatingConditions;
		
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._maximumFuelMass = builder.__maximumFuelMass;
		this._singlePassengerMass = builder.__singlePassengerMass;
		
		this._xCGMaxAft = builder.__xCGMaxAft;
		
		this._cLmaxClean = builder.__cLmaxClean;
		this._cLAlphaClean = builder.__cLAlphaClean;
		this._cLmaxTakeOff = builder.__cLmaxTakeOff;
		this._cLAlphaTakeOff = builder.__cLAlphaTakeOff;
		this._cLZeroTakeOff = builder.__cLZeroTakeOff;
		this._cLmaxLanding = builder.__cLmaxLanding;
		this._cLAlphaLanding = builder.__cLAlphaLanding;
		this._cLZeroLanding = builder.__cLZeroLanding;
		this._polarCLCruise = builder.__polarCLCruise;
		this._polarCDCruise = builder.__polarCDCruise;
		this._polarCLClimb = builder.__polarCLClimb;
		this._polarCDClimb = builder.__polarCDClimb;
		this._polarCLTakeOff = builder.__polarCLTakeOff;
		this._polarCDTakeOff = builder.__polarCDTakeOff;
		this._polarCLLanding = builder.__polarCLLanding;
		this._polarCDLanding = builder.__polarCDLanding;
		
		this._windSpeed = builder.__windSpeed;
		this._mu = builder.__mu;
		this._muBrake = builder.__muBrake;

		this._dtRotation = builder.__dtRotation;
		this._dtHold = builder.__dtHold;
		this._alphaGround = builder.__alphaGround;
		this._obstacleTakeOff = builder.__obstacleTakeOff;
		this._kRotation = builder.__kRotation;
		this._kLiftOff = builder.__kLiftOff;
		this._kCLmax = builder.__kCLmax;
		this._dragDueToEnigneFailure = builder.__dragDueToEngineFailure;
		this._kAlphaDot = builder.__kAlphaDot;
		this._alphaReductionRate = builder.__alphaReductionRate;
		
		this._kLandingWeight = builder.__kLandingWeight;
		this._obstacleLanding = builder.__obstacleLanding;
		this._thetaApproach = builder.__thetaApproach;
		this._kApproach = builder.__kApproach;
		this._kFlare = builder.__kFlare;
		this._kTouchDown = builder.__kTouchDown;
		this._freeRollDuration = builder.__freeRollDuration;
		
		this._kClimbWeightAOE = builder.__kClimbWeightAOE;
		this._kClimbWeightOEI = builder.__kClimbWeightOEI;
		this._climbSpeed = builder.__climbSpeed;
		
		this._altitudeListCruise = builder.__altitudeListCruise;
		this._kCruiseWeight = builder.__kCruiseWeight;
		
		this._cLmaxInverted = builder.__cLmaxInverted;
		
		this._rateOfDescent = builder.__rateOfDescent;
		this._speedDescentCAS = builder.__speedDescent;
		
	    this._alternateCruiseLength = builder.__alternateCruiseLength;
	    this._alternateCruiseAltitude = builder.__alternateCruiseAltitude;
	    this._alternateCruiseMachNumber = builder.__alternateCruiseMachNumber;
	    this._holdingDuration = builder.__holdingDuration;
	    this._holdingAltitude = builder.__holdingAltitude;
	    this._holdingMachNumber = builder.__holdingMachNumber;
	    this._fuelReserve = builder.__fuelReserve;
	    this._firstGuessCruiseLength = builder.__firstGuessCruiseLength;
	    this._firstGuessInitialMissionFuelMass = builder.__firstGuessInitialMissionFuelMass;
		this._takeOffMissionAltitude = builder.__takeOffMissionAltitude;
		this._cruiseMissionMachNumber = builder.__cruiseMissionMachNumber;
		this._landingFuelFlow = builder.__landingFuelFlow;
	    
		this._taskList = builder.__taskList;
		this._plotList = builder.__plotList;
		
	}
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	@SuppressWarnings({ "resource", "unchecked" })
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
		String fileWeightsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_weights");
		Boolean readWeightsFromXLSFlag;
		String readWeightsFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_xls_file");
		if(readWeightsFromXLSString.equalsIgnoreCase("true"))
			readWeightsFromXLSFlag = Boolean.TRUE;
		else
			readWeightsFromXLSFlag = Boolean.FALSE;
		
		//---------------------------------------------------------------------------------------
		// WEIGHTS FROM FILE INSTRUCTION
		String fileBalanceXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_balance");
		Boolean readBalanceFromXLSFlag;
		String readBalanceFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@balance_from_xls_file");
		if(readBalanceFromXLSString.equalsIgnoreCase("true"))
			readBalanceFromXLSFlag = Boolean.TRUE;
		else
			readBalanceFromXLSFlag = Boolean.FALSE;
		
		//---------------------------------------------------------------------------------------
		// AERODYNAMICS FROM FILE INSTRUCTION
		String fileAerodynamicsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_aerodynamics");
		Boolean readAerodynamicsFromXLSFlag;
		String readAerodynamicsFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@aerodynamics_from_xls_file");
		if(readAerodynamicsFromXLSString.equalsIgnoreCase("true"))
			readAerodynamicsFromXLSFlag = Boolean.TRUE;
		else
			readAerodynamicsFromXLSFlag = Boolean.FALSE;

		//===========================================================================================
		// READING WEIGHTS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> maximumFuelMass = null;
		Amount<Mass> singlePassengerMass = null;
		
		if(readWeightsFromXLSFlag == Boolean.TRUE) {

			File weightsFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "WEIGHTS"
					+ File.separator
					+ fileWeightsXLS);
			if(weightsFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(weightsFile);
				Workbook workbook;
				if (weightsFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (weightsFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//...............................................................
				// MAXIMUM TAKE-OFF MASS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell maximumTakeOffMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Take-Off Mass").get(0)).getCell(2);
					if(maximumTakeOffMassCell != null)
						maximumTakeOffMass = Amount.valueOf(maximumTakeOffMassCell.getNumericCellValue(), SI.KILOGRAM);
				}
				
				//...............................................................
				// OPERATING EMPTY MASS
				Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(2);
				if(operatingEmptyMassCell != null)
					operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

				//...............................................................
				// MAXIMUM FUEL MASS
				Cell maximumFuelMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Fuel Mass").get(0)).getCell(2);
				if(maximumFuelMassCell != null)
					maximumFuelMass = Amount.valueOf(maximumFuelMassCell.getNumericCellValue(), SI.KILOGRAM);
				//...............................................................
				// SINGLE PASSENGER MASS
				Cell singlePassengerMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Single passenger Mass").get(0)).getCell(2);
				if(singlePassengerMassCell != null)
					singlePassengerMass = Amount.valueOf(singlePassengerMassCell.getNumericCellValue(), SI.KILOGRAM);
			}
			else {
				System.err.println("FILE '" + weightsFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
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
		// READING BALANCE DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		Amount<Length> xCGMaxAft = null;		
		
		if(readBalanceFromXLSFlag == Boolean.TRUE) {
			
			File balanceFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "BALANCE"
					+ File.separator
					+ fileBalanceXLS);
			
			if(balanceFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(balanceFile);
				Workbook workbook;
				if (balanceFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (balanceFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}
				
				//...............................................................
				// XCG Max Forward
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell xCGMaxAftCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Max aft Xcg MAC").get(0)).getCell(2);
					if(xCGMaxAftCell != null)
						xCGMaxAft = Amount.valueOf(
								(xCGMaxAftCell.getNumericCellValue()
								* theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
								+ (theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
										+ theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
								SI.METER);
				}
			}
		}
		else {
			//...............................................................
			// XCG Max Forward
			String xCGMaxAftProperty = reader.getXMLPropertyByPath("//performance/balance/xCG_max_aft");
			if(xCGMaxAftProperty != null)
				xCGMaxAft = Amount.valueOf(
						(Double.valueOf(reader.getXMLPropertyByPath("//performance/balance/xCG_max_aft"))
								* theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
								/100)
						+ (theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
								+ theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
						SI.METER);
		}
		
		//===========================================================================================
		// READING AERODYNAMICS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */

		Double cD0 = null;
		Double oswaldCruise = null;
		Double oswaldClimb = null;
		Double oswaldTakeOff = null;
		Double oswaldLanding = null;
		Double cLmaxClean = null;
		Amount<?> cLAlphaClean = null;
		Amount<?> cLAlphaTakeOff = null;
		Amount<?> cLAlphaLanding = null;
		Double deltaCD0TakeOff = null;
		Double deltaCD0Landing = null;
		Double deltaCD0LandingGears = null;
		Double deltaCD0Spoilers = null;
		Double cLmaxTakeOff = null;
		Double cLZeroTakeOff = null;
		Double cLmaxLanding = null;
		Double cLZeroLanding = null;
		Double[] polarCLCruise = null;
		Double[] polarCDCruise = null;
		Double[] polarCLClimb = null;
		Double[] polarCDClimb = null;
		Double[] polarCLTakeOff = null;
		Double[] polarCDTakeOff = null;
		Double[] polarCLLanding = null;
		Double[] polarCDLanding = null;
		
		if(readAerodynamicsFromXLSFlag == Boolean.TRUE) {
			
			File aerodynamicsFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "AERODYNAMICS"
					+ File.separator
					+ fileAerodynamicsXLS);
			
			if(aerodynamicsFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(aerodynamicsFile);
				Workbook workbook;
				if (aerodynamicsFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (aerodynamicsFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}
				
				////////////////////////////////////////////////////////////////////////////
				//																		  //
				// TODO : READ ALL REQUIRED DATA FROM THE AERODYNAMICS XML WHEN AVAILABLE //
				//																		  //
				////////////////////////////////////////////////////////////////////////////
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
				return null;
			}
			
			//...............................................................
			// CD0
			String cD0Property = reader.getXMLPropertyByPath("//performance/aerodynamics/cD0");
			if(cD0Property != null)
				cD0 = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cD0"));
			//...............................................................
			// OSWALD CRUISE
			String oswladCruiseProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_cruise");
			if(oswladCruiseProperty != null)
				oswaldCruise = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_cruise"));
			//...............................................................
			// OSWALD CLIMB
			String oswladClimbProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_climb");
			if(oswladClimbProperty != null)
				oswaldClimb = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_climb"));
			//...............................................................
			// OSWALD TO
			String oswladTOProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_take_off");
			if(oswladTOProperty != null)
				oswaldTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_take_off"));
			//...............................................................
			// OSWALD LND
			String oswladLNDProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_landing");
			if(oswladLNDProperty != null)
				oswaldLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_landing"));
			//...............................................................
			// CLmax CLEAN
			String cLmaxCleanProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration");
			if(cLmaxCleanProperty != null)
				cLmaxClean = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration"));
			//...............................................................
			// CLalpha CLEAN
			String cLAlphaCleanProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_clean_configuration");
			if(cLAlphaCleanProperty != null)
				cLAlphaClean = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_clean_configuration"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//...............................................................
			// CLalpha TAKE-OFF
			String cLAlphaTakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_take_off");
			if(cLAlphaTakeOffProperty != null)
				cLAlphaTakeOff = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_take_off"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//...............................................................
			// CLalpha LANDING
			String cLAlphaLandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_landing");
			if(cLAlphaLandingProperty != null)
				cLAlphaLanding = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_landing"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//...............................................................
			// DeltaCD0 TAKE-OFF
			String deltaCD0TakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_flap_take_off");
			if(deltaCD0TakeOffProperty != null)
				deltaCD0TakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_flap_take_off"));
			//...............................................................
			// DeltaCD0 LANDING
			String deltaCD0LandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_flap_landing");
			if(deltaCD0LandingProperty != null)
				deltaCD0Landing = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_flap_landing"));
			//...............................................................
			// DeltaCD0 LANDING GEARS
			String deltaCD0LandingGearsProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears");
			if(deltaCD0LandingGearsProperty != null)
				deltaCD0LandingGears = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears"));
			//...............................................................
			// DeltaCD0 SPOILERS
			String deltaCD0SpoilersProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_spoilers");
			if(deltaCD0SpoilersProperty != null)
				deltaCD0Spoilers = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_spoilers"));
			//...............................................................
			// CLmax TAKE-OFF
			String cLmaxTakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration");
			if(cLmaxTakeOffProperty != null)
				cLmaxTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration"));
			//...............................................................
			// CL0 TAKE-OFF
			String cL0TakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration");
			if(cL0TakeOffProperty != null)
				cLZeroTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration"));
			//...............................................................
			// CLmax LANDING
			String cLmaxLandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration");
			if(cLmaxLandingProperty != null)
				cLmaxLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration"));
			//...............................................................
			// CL0 LANDING
			String cL0LandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration");
			if(cL0LandingProperty != null)
				cLZeroLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_landing_configuration"));
			
			if(parabolicDragPolarFlag == Boolean.FALSE) {
				
				//...............................................................
				// POLAR CURVE CRUISE
				List<String> polarCLCruiseProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/cruise/polar_curve_CL");
				if(!polarCLCruiseProperty.isEmpty()) {
					polarCLCruise = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/cruise/polar_curve_CL")
							); 
				}
				List<String> polarCDCruiseProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/cruise/polar_curve_CD");
				if(!polarCDCruiseProperty.isEmpty()) {
					polarCDCruise = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/cruise/polar_curve_CD")
							); 
				}
				//...............................................................
				// POLAR CURVE CLIMB
				List<String> polarCLClimbProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/climb/polar_curve_CL");
				if(!polarCLClimbProperty.isEmpty()) {
					polarCLClimb = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/climb/polar_curve_CL")
							); 
				}
				List<String> polarCDClimbProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/climb/polar_curve_CD");
				if(!polarCDClimbProperty.isEmpty()) {
					polarCDClimb = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/climb/polar_curve_CD")
							); 
				}
				//...............................................................
				// POLAR CURVE TAKE-OFF
				List<String> polarCLTakeOffProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/take_off/polar_curve_CL");
				if(!polarCLTakeOffProperty.isEmpty()) {
					polarCLTakeOff = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/take_off/polar_curve_CL")
							); 
				}
				List<String> polarCDTakeOffProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/take_off/polar_curve_CD");
				if(!polarCDTakeOffProperty.isEmpty()) {
					polarCDTakeOff = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/take_off/polar_curve_CD")
							); 
				}
				//...............................................................
				// POLAR CURVE LANDING
				List<String> polarCLLandingProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/landing/polar_curve_CL");
				if(!polarCLLandingProperty.isEmpty()) {
					polarCLLanding = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/landing/polar_curve_CL")
							);
				}
				List<String> polarCDLandingProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/landing/polar_curve_CD");
				if(!polarCDLandingProperty.isEmpty()) {
					polarCDLanding = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/landing/polar_curve_CD")
							); 
				}
			}
			else {
				
				int numberOfPolarPoints = 50;
				
				//...............................................................
				// POLAR CURVE CRUISE
				polarCLCruise = MyArrayUtils.linspaceDouble(-0.2, cLmaxClean+0.2, numberOfPolarPoints);				
				polarCDCruise = new Double[polarCLCruise.length];
				
				//...............................................................
				// POLAR CURVE CLIMB
				polarCLClimb = MyArrayUtils.linspaceDouble(-0.2, cLmaxClean+0.2, numberOfPolarPoints);				
				polarCDClimb = new Double[polarCLClimb.length];
				
				//...............................................................
				// POLAR CURVE TAKE-OFF
				polarCLTakeOff = MyArrayUtils.linspaceDouble(-0.2, cLmaxTakeOff+0.2, numberOfPolarPoints);
				polarCDTakeOff = new Double[polarCLTakeOff.length]; 
				
				//...............................................................
				// POLAR CURVE LANDING
				polarCLLanding = MyArrayUtils.linspaceDouble(-0.2, cLmaxLanding+0.2, numberOfPolarPoints);
				polarCDLanding = new Double[polarCLLanding.length];
				
				// building the CD arrays...
				for(int i=0; i<numberOfPolarPoints; i++) {
					polarCDClimb[i] = DragCalc.calculateCDTotal(
							cD0,
							polarCLClimb[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldClimb, 
							theOperatingConditions.getMachClimb(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLCruise[i],
									theOperatingConditions.getMachClimb(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
					polarCDCruise[i] = DragCalc.calculateCDTotal(
							cD0,
							polarCLCruise[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldCruise, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLCruise[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
					polarCDTakeOff[i] = DragCalc.calculateCDTotal(
							cD0 + deltaCD0TakeOff + deltaCD0LandingGears,
							polarCLTakeOff[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldTakeOff, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLTakeOff[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
					polarCDLanding[i] = DragCalc.calculateCDTotal(
							cD0 + deltaCD0Landing + deltaCD0LandingGears + deltaCD0Spoilers,
							polarCLLanding[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldLanding, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLLanding[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
				}
			}
		}
		
		//===========================================================================================
		// READING TAKE-OFF AND LANDING DATA ...	

		// default values
		Amount<Velocity> windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Double mu = 0.03;
		Double muBrake = 0.4;
		
		Amount<Duration> dtRotation = Amount.valueOf(3.0, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Length> obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		Double kRotation = 1.05;
		Double kLiftOff = 1.1;
		Double kCLmax = 0.9;
		Double dragDueToEngineFailure = 0.0;
		Double kAlphaDot = 0.04;
		Double alphaReductionRate = -4.0;
		
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
			dtRotation = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_rotation")), SI.SECOND);				
		
		//...............................................................
		// dt HOLD
		String dtHoldProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold");
		if(dtHoldProperty != null)
			dtHold = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold")), SI.SECOND);				
		
		//...............................................................
		// ALPHA GROUND
		String alphaGroundProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground");
		if(alphaGroundProperty != null)
			alphaGround = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground")), NonSI.DEGREE_ANGLE);
		
		//...............................................................
		// WIND SPEED
		String windSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway");
		if(windSpeedProperty != null)
			windSpeed = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway")), SI.METERS_PER_SECOND);
		
		//...............................................................
		// OBSTACLE TAKE-OFF
		String obstacleTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off");
		if(obstacleTakeOffProperty != null)
			obstacleTakeOff = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off")), SI.METER);
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT
		String wheelsFrictionCoefficientProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient");
		if(wheelsFrictionCoefficientProperty != null)
			mu = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient"));
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT WITH BRAKES
		String wheelsFrictionCoefficientWithBrakesProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes");
		if(wheelsFrictionCoefficientWithBrakesProperty != null)
			muBrake = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes"));
		
		//...............................................................
		// K ROTATION
		String kRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation");
		if(kRotationProperty != null)
			kRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation"));
		
		//...............................................................
		// K LIFT OFF
		String kLiftOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_lift_off");
		if(kLiftOffProperty != null)
			kLiftOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_lift_off"));
		
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
		// ALPHA REDUCTION RATE
		String alphaReductionRateProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_reduction_rate");
		if(alphaReductionRateProperty != null)
			alphaReductionRate = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_reduction_rate"));

		//...............................................................
		// K LANDING WEIGHT
		String kLandingWeightProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight");
		if(kLandingWeightProperty != null)
			kLandingWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight"));
		
		//...............................................................
		// OBSTACLE LANDING
		String obstacleLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing");
		if(obstacleLandingProperty != null)
			obstacleLanding = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing")), SI.METER);		
		
		//...............................................................
		// THETA APPROACH
		String thetaApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/theta_approach");
		if(thetaApproachProperty != null)
			thetaApproach = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/theta_approach")), NonSI.DEGREE_ANGLE);		
		
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
			freeRollDuration = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/free_roll_duration")), SI.SECOND);
		
		
		//===========================================================================================
		// READING CLIMB DATA ...	
		Double kClimbWeightAOE = 1.0;
		Double kClimbWeightOEI = 0.97;
		Amount<Velocity> climbSpeed = null;
		
		//...............................................................
		// K CLIMB WEIGHT AOE
		String kClimbWeightAOEProperty = reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AOE");
		if(kClimbWeightAOEProperty != null)
			kClimbWeightAOE = Double.valueOf(reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AOE"));
		
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
		
		
		//===========================================================================================
		// READING CRUISE DATA ...	
		List<Amount<Length>> altitudeListCruise = null;
		Double kCruiseWeight = 0.98;
		
		//...............................................................
		// ALTITUDE LIST CRUISE
		List<String> altitudeListCruiseProperty = reader.getXMLPropertiesByPath("//performance/cruise/altitudes_array");
		if(!altitudeListCruiseProperty.isEmpty()) {
			altitudeListCruise = reader.readArrayofAmountFromXML("//performance/cruise/altitudes_array"); 
		}
		//...............................................................
		// K CLIMB WEIGHT AOE
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
		//...............................................................
		// RATE OF DESCENT 
		String rateOfDescentProperty = reader.getXMLPropertyByPath("//performance/descent/rate_of_descent");
		if(rateOfDescentProperty != null)
			rateOfDescent = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//performance/descent/rate_of_descent"
									)
							),
					SI.METERS_PER_SECOND
					);		
		//...............................................................
		// SPEED DESCENT CAS
		String speedDescentCASProperty = reader.getXMLPropertyByPath("//performance/descent/descent_speed_CAS");
		if(speedDescentCASProperty != null)
			speedDescentCAS = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//performance/descent/descent_speed_CAS"
									)
							),
					SI.METERS_PER_SECOND
					);		
		
		
		//===========================================================================================
		// READING MISSION PROFILE DATA ...
		Amount<Length> alternateCruiseLength = Amount.valueOf(0.0, SI.METER);
		Amount<Length> alternateCruiseAltitude = Amount.valueOf(15.24, SI.METER);
		Double alternateCruiseMachNumber = 0.01; // default value but != 0.0
		Amount<Duration> holdingDuration = Amount.valueOf(0.0, SI.SECOND);
		Amount<Length> holdingAltitude = Amount.valueOf(0.0, SI.METER);
		Double holdingMachNumber = 0.01; // default value but != 0.0
		Double fuelReserve = 0.0;
		Amount<Length> firstGuessCruiseLength = null;
		Amount<Mass> firstGuessInitialFuelMass = null;
		Amount<Length> takeOffMissionAltitude = null;
		Double cruiseMissionMachNumber = null;
		Double landingFuelFlow = 0.0;
		
		//...............................................................
		// ALTERNATE CRUISE LENGTH
		List<String> alternateCruiseLengthProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/alternate_cruise_length");
		if(!alternateCruiseLengthProperty.isEmpty()) {
			alternateCruiseLength = reader.getXMLAmountLengthByPath("//performance/mission_profile/alternate_cruise_length"); 
		}
		//...............................................................
		// ALTERNATE CRUISE ALTITUDE
		List<String> alternateCruiseAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/alternate_cruise_altitude");
		if(!alternateCruiseAltitudeProperty.isEmpty()) {
			alternateCruiseAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile/alternate_cruise_altitude"); 
		}
		//...............................................................
		// ALTERNATE CRUISE MACH NUMBER
		List<String> alternateCruiseMachNumberProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/alternate_cruise_mach_number");
		if(!alternateCruiseMachNumberProperty.isEmpty()) {
			alternateCruiseMachNumber = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile/alternate_cruise_mach_number")); 
		}
		//...............................................................
		// HOLDING DURATION
		List<String> holdingDurationProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/holding_duration");
		if(!holdingDurationProperty.isEmpty()) {
			holdingDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/mission_profile/holding_duration"); 
		}
		//...............................................................
		// HOLDING ALTITUDE
		List<String> holdingAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/holding_altitude");
		if(!holdingAltitudeProperty.isEmpty()) {
			holdingAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile/holding_altitude"); 
		}
		//...............................................................
		// HOLDING MACH NUMBER
		List<String> holdingMachNumberProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/holding_mach_number");
		if(!holdingMachNumberProperty.isEmpty()) {
			holdingMachNumber = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile/holding_mach_number")); 
		}
		//...............................................................
		// FUEL RESERVE
		List<String> fuelReserveProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/fuel_reserve");
		if(!fuelReserveProperty.isEmpty()) {
			fuelReserve = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile/fuel_reserve")); 
		}
		//...............................................................
		// FIRST GUESS CRUISE LENGTH
		List<String> firstGuessCruiseLengthProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/cruise_length");
		if(!firstGuessCruiseLengthProperty.isEmpty()) {
			firstGuessCruiseLength = reader.getXMLAmountLengthByPath("//performance/mission_profile/cruise_length"); 
		}
		//...............................................................
		// FIRST GUESS INITIAL FUEL MASS
		List<String> firstGuessInitialFuelMassProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/initial_mission_fuel");
		if(!firstGuessInitialFuelMassProperty.isEmpty()) {
			firstGuessInitialFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/mission_profile/initial_mission_fuel"); 
		}
		//...............................................................
		// TAKE OFF MISSION ALITUDE
		List<String> takeOffMissionAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/take_off_mission_altitude");
		if(!takeOffMissionAltitudeProperty.isEmpty()) {
			takeOffMissionAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile/take_off_mission_altitude"); 
		}
		//...............................................................
		// CRUISE MISSION MACH NUMBER
		List<String> cruiseMissionMachNumberProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/cruise_mission_mach_number");
		if(!cruiseMissionMachNumberProperty.isEmpty()) {
			cruiseMissionMachNumber = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile/cruise_mission_mach_number")); 
		}
		//...............................................................
		// LANDING FUEL FLOW
		List<String> landingFuelFlowProperty = reader.getXMLPropertiesByPath("//performance/mission_profile/landing_ground_idle_fuel_flow");
		if(!landingFuelFlowProperty.isEmpty()) {
			landingFuelFlow = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile/landing_ground_idle_fuel_flow")); 
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
		ACPerformanceManager thePerformanceCalculator = new ACPerformanceCalculatorBuilder()
				.id(id)
				.aircraft(theAircraft)
				.operatingConditions(theOperatingConditions)
				.maximumTakeOffMass(maximumTakeOffMass)
				.operatingEmptyMass(operatingEmptyMass)
				.maximumFuelMass(maximumFuelMass)
				.singlePassengerMass(singlePassengerMass)
				.xCGMaxAft(xCGMaxAft)
				.cLmaxClean(cLmaxClean)
				.cLAlphaClean(cLAlphaClean)
				.cLAlphaTakeOff(cLAlphaTakeOff)
				.cLAlphaLanding(cLAlphaLanding)
				.cLmaxTakeOff(cLmaxTakeOff)
				.cLZeroTakeOff(cLZeroTakeOff)
				.cLmaxLanding(cLmaxLanding)
				.cLZeroLanding(cLZeroLanding)
				.polarCLCruise(polarCLCruise)
				.polarCDCruise(polarCDCruise)
				.polarCLClimb(polarCLClimb)
				.polarCDClimb(polarCDClimb)
				.polarCLTakeOff(polarCLTakeOff)
				.polarCDTakeOff(polarCDTakeOff)
				.polarCLLanding(polarCLLanding)
				.polarCDLanding(polarCDLanding)
				.dtRotation(dtRotation)
				.dtHold(dtHold)
				.alphaGround(alphaGround)
				.windSpeed(windSpeed)
				.obstacleTakeOff(obstacleTakeOff)
				.mu(mu)
				.muBrake(muBrake)
				.kRotation(kRotation)
				.kLiftOff(kLiftOff)
				.kCLmax(kCLmax)
				.dragDueToEngineFailure(dragDueToEngineFailure)
				.kAlphaDot(kAlphaDot)
				.alphaReductionRate(alphaReductionRate)
				.kLandingWeight(kLandingWeight)
				.obstacleLanding(obstacleLanding)
				.thetaApproach(thetaApproach)
				.kApproach(kApproach)
				.kFlare(kFlare)
				.kTouchDown(kTouchDown)
				.freeRollDuration(freeRollDuration)
				.kClimbWeightAOE(kClimbWeightAOE)
				.kClimbWeightOEI(kClimbWeightOEI)
				.climbSpeed(climbSpeed)
				.altitudeListCruise(altitudeListCruise)
				.kCruiseWeight(kCruiseWeight)
				.cLmaxInverted(cLmaxInverted)
				.rateOfDescent(rateOfDescent)
				.speedDescentCAS(speedDescentCAS)
				.alternateCruiseLength(alternateCruiseLength)
				.alternateCruiseAltitude(alternateCruiseAltitude)
				.alternateCruiseMachNumber(alternateCruiseMachNumber)
				.holdingDuration(holdingDuration)
				.holdingAltitude(holdingAltitude)
				.holdingMachNumber(holdingMachNumber)
				.fuelReserve(fuelReserve)
				.firstGuessCruiseLength(firstGuessCruiseLength)
				.firstGuessFuelMass(firstGuessInitialFuelMass)
				.takeOffMissionAltitude(takeOffMissionAltitude)
				.cruiseMissionMachNumber(cruiseMissionMachNumber)
				.landingFuelFlow(landingFuelFlow)
				.taskList(theAircraft.getTheAnalysisManager().getTaskListPerformance())
				.plotList(plotList)
				.build();
		
		return thePerformanceCalculator;
	}
	
	/**
	 * This method reads the task list, initializes the related calculators inner classes and 
	 * performe the required calculation
	 */
	public void calculatePerformance(String resultsFolderPath) {
		
		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "PERFORMANCE"
				+ File.separator
				);
		
		if(_taskList.contains(PerformanceEnum.TAKE_OFF)) {
			
			String takeOffFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "TAKE_OFF"
					+ File.separator
					);
			
			CalcTakeOff calcTakeOff = new CalcTakeOff();
			calcTakeOff.performTakeOffSimulation(
					_maximumTakeOffMass, 
					_theOperatingConditions.getAltitudeTakeOff(),
					_theOperatingConditions.getMachTakeOff()
					);
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotTakeOffPerformance(takeOffFolderPath);
			calcTakeOff.calculateBalancedFieldLength();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotBalancedFieldLength(takeOffFolderPath);
			calcTakeOff.calculateVMC();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcTakeOff.plotVMC(takeOffFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.CLIMB)) {
			
			String climbFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "CLIMB"
					+ File.separator
					);
			
			CalcClimb calcClimb = new CalcClimb();
			calcClimb.calculateClimbPerformance(
					_maximumTakeOffMass.times(_kClimbWeightAOE),
					_maximumTakeOffMass.times(_kClimbWeightOEI)
					);
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcClimb.plotClimbPerformance(climbFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.CRUISE)) {
			
			String cruiseFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "CRUISE"
					+ File.separator
					);
			
			_weightListCruise = new ArrayList<Amount<Force>>();
			
			for(int i=0; i<5; i++) {
				Amount<Force> cruiseWeight = 
						Amount.valueOf(
								(_maximumTakeOffMass
								.times(_kCruiseWeight)
								.times(AtmosphereCalc.g0)
								.getEstimatedValue()
								),
								SI.NEWTON
								);
				
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
			calcCruise.calculateThrustAndDrag(_maximumTakeOffMass.times(_kCruiseWeight));
			calcCruise.calculateFlightEnvelope(_maximumTakeOffMass.times(_kCruiseWeight));
			calcCruise.calculateEfficiency();
			calcCruise.calculateCruiseGrid();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcCruise.plotCruiseOutput(cruiseFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.DESCENT)) {
			
			String descentFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "DESCENT"
					+ File.separator
					);
			
			CalcDescent calcDescent = new CalcDescent();
			calcDescent.calculateDescentPerformance(
					_theOperatingConditions.getAltitudeCruise(),
					Amount.valueOf(15.24, SI.METER)
					);
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcDescent.plotDescentPerformance(descentFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.LANDING)) {
			
			String landingFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "LANDING"
					+ File.separator
					);
			
			CalcLanding calcLanding = new CalcLanding();
			calcLanding.performLandingSimulation(_maximumTakeOffMass.times(_kLandingWeight));
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcLanding.plotLandingPerformance(landingFolderPath);
		}
		
		if(_taskList.contains(PerformanceEnum.PAYLOAD_RANGE)) {
		
			String payloadRangeFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "PAYLOAD_RANGE"
					+ File.separator
					);
			
			CalcPayloadRange calcPayloadRange = new CalcPayloadRange();
			calcPayloadRange.fromRangeBreguet();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcPayloadRange.plotPayloadRange(payloadRangeFolderPath);
		
		}
		
		if(_taskList.contains(PerformanceEnum.V_n_DIAGRAM)) {
			
			String maneuveringFlightAndGustEnvelopeFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "V-n_DIAGRAM"
					+ File.separator
					);
			
			CalcFlightManeuveringAndGustEnvelope calcEnvelope =  new CalcFlightManeuveringAndGustEnvelope();
			calcEnvelope.fromRegulations();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcEnvelope.plotVnDiagram(maneuveringFlightAndGustEnvelopeFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.MISSION_PROFILE)) {
			
			String missionProfilesFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "MISSION_PROFILES"
					+ File.separator
					);
			
			CalcMissionProfile calcMissionProfile = new CalcMissionProfile();
			calcMissionProfile.calculateMissionProfileIterative();
			if(_theAircraft.getTheAnalysisManager().getPlotPerformance() == true)
				calcMissionProfile.plotProfiles(missionProfilesFolderPath);
			
		}
		
//		// PRINT RESULTS
//		try {
//			toXLSFile(performanceFolderPath + "Performance");
//		} catch (InvalidFormatException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
		
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
        if(_taskList.contains(PerformanceEnum.TAKE_OFF)) {
        	Sheet sheet = wb.createSheet("TAKE-OFF");
        	List<Object[]> dataListTakeOff = new ArrayList<>();

        	dataListTakeOff.add(new Object[] {"Description","Unit","Value"});
        	dataListTakeOff.add(new Object[] {"Ground roll distance","m", _groundRollDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","m", _rotationDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","m", _airborneDistanceTakeOff.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"AOE take-off distance","m", _takeOffDistanceAOE.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","m", _takeOffDistanceFAR25.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","m", _balancedFieldLength.doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","m/s", _vMC.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","m/s", _vStallTakeOff.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","m/s", _v1.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","m/s", _vRotation.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","m/s", _vLiftOff.doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","m/s", _v2.doubleValue(SI.METERS_PER_SECOND)});
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
        if(_taskList.contains(PerformanceEnum.CLIMB)) {
        	Sheet sheetClimb = wb.createSheet("CLIMB");
        	List<Object[]> dataListClimb = new ArrayList<>();

        	dataListClimb.add(new Object[] {"Description","Unit","Value"});
        	dataListClimb.add(new Object[] {"Absolute ceiling AOE","m", _absoluteCeilingAOE.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling AOE","m", _serviceCeilingAOE.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Max rate of climb at cruise altitude AOE","m/s", _maxRateOfClimbAtCruiseAltitudeAOE.doubleValue(SI.METERS_PER_SECOND)});
        	dataListClimb.add(new Object[] {"Max climb angle at cruise altitude AOE","deg", _maxThetaAtCruiseAltitudeAOE.doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListClimb.add(new Object[] {"Minimum time to climb AOE","min", _minimumClimbTimeAOE.doubleValue(NonSI.MINUTE)});
        	if(_climbTimeAtSpecificClimbSpeedAOE != null)
        		dataListClimb.add(new Object[] {"Time to climb at given climb speed AOE","min", _climbTimeAtSpecificClimbSpeedAOE.doubleValue(NonSI.MINUTE)});
        	dataListClimb.add(new Object[] {" "});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","m", _absoluteCeilingOEI.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","m", _serviceCeilingOEI.doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Minimum time to climb OEI","min", _minimumClimbTimeOEI.doubleValue(NonSI.MINUTE)});
        	if(_climbTimeAtSpecificClimbSpeedOEI != null)
        		dataListClimb.add(new Object[] {"Time to climb at given climb speed OEI","min", _climbTimeAtSpecificClimbSpeedOEI.doubleValue(NonSI.MINUTE)});

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
        if(_taskList.contains(PerformanceEnum.CRUISE)) {
        	Sheet sheetCruise = wb.createSheet("CRUISE");
        	List<Object[]> dataListCruise = new ArrayList<>();
        	
        	dataListCruise.add(new Object[] {"Description","Unit","Value"});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","N", _thrustAtCruiseAltitudeAndMach.doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","N", _dragAtCruiseAltitudeAndMach.doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","W", _powerAvailableAtCruiseAltitudeAndMach.doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","W", _powerNeededAtCruiseAltitudeAndMach.doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","m/s", _minSpeesCASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","m/s", _maxSpeesCASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","m/s", _minSpeesTASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","m/s", _maxSpeesTASAtCruiseAltitude.doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min Mach number at cruise altitude","", _minMachAtCruiseAltitude});
        	dataListCruise.add(new Object[] {"Max Mach number at cruise altitude","", _maxMachAtCruiseAltitude});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Efficiency at cruise altitude and Mach","", _efficiencyAtCruiseAltitudeAndMach});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Specific range at cruise altitude and Mach","nmi/lbs", _specificRangeAtCruiseAltitudeAndMach});

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
        if(_taskList.contains(PerformanceEnum.DESCENT)) {
        	Sheet sheetDescent = wb.createSheet("DESCENT");
        	List<Object[]> dataListDescent = new ArrayList<>();

        	dataListDescent.add(new Object[] {"Description","Unit","Value"});
        	dataListDescent.add(new Object[] {"Descent length","nmi", _totalDescentLength.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListDescent.add(new Object[] {"Descent length","km", _totalDescentLength.doubleValue(SI.KILOMETER)});
        	dataListDescent.add(new Object[] {"Descent duration","min", _totalDescentTime.doubleValue(NonSI.MINUTE)});

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
        if(_taskList.contains(PerformanceEnum.LANDING)) {
        	Sheet sheetLanding = wb.createSheet("LANDING");
        	List<Object[]> dataListLanding = new ArrayList<>();

        	dataListLanding.add(new Object[] {"Description","Unit","Value"});
        	dataListLanding.add(new Object[] {"Ground roll distance","m", _groundRollDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Flare distance","m", _flareDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Airborne distance","m", _airborneDistanceLanding.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Landing distance","m", _landingDistance.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","m", _landingDistanceFAR25.doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","m/s", _vStallLanding.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","m/s", _vTouchDown.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","m/s", _vFlare.doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","m/s", _vApproach.doubleValue(SI.METERS_PER_SECOND)});
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
        if(_taskList.contains(PerformanceEnum.MISSION_PROFILE)) {
        	Sheet sheetMissionProfile = wb.createSheet("MISSION PROFILE");
        	List<Object[]> dataListMissionProfile = new ArrayList<>();

        	dataListMissionProfile.add(new Object[] {"Description","Unit","Value"});
        	dataListMissionProfile.add(new Object[] {"Total mission distance","nmi", _theAircraft.getTheAnalysisManager().getReferenceRange().doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListMissionProfile.add(new Object[] {"Total mission duration","min", _totalMissionTime.doubleValue(NonSI.MINUTE)});
        	dataListMissionProfile.add(new Object[] {"Aircraft mass at mission start","kg", _maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Aircraft mass at mission end","kg", _endMissionMass.doubleValue(SI.KILOGRAM)});
        	dataListMissionProfile.add(new Object[] {"Total fuel used","kg", _totalFuelUsed.doubleValue(SI.KILOGRAM)});

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
        if(_taskList.contains(PerformanceEnum.PAYLOAD_RANGE)) {
        	Sheet sheetPayloadRange = wb.createSheet("PAYLOAD-RANGE");
        	List<Object[]> dataListPayloadRange = new ArrayList<>();

        	dataListPayloadRange.add(new Object[] {"Description","Unit","Value"});
        	dataListPayloadRange.add(new Object[] {"CURRENT MACH CONDITION", _theOperatingConditions.getMachCruise()});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxPayloadCurrentMach.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _thePayloadRangeCalculator.getPaxSingleMass().times(_thePayloadRangeCalculator.getnPassMax()).doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _thePayloadRangeCalculator.getnPassMax()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtMaxPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtMaxPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtMaxPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtMaxPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX FUEL"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxFuelCurrentMach.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _thePayloadRangeCalculator.getPaxSingleMass().times(_thePayloadRangeCalculator.getnPassActual()).doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _thePayloadRangeCalculator.getnPassActual()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getMaxFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtMaxFuelCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtMaxFuelCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtMaxFuelCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtMaxFuelCurrentMach()});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT ZERO PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtZeroPayloadCurrentMach.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePayloadRangeCalculator.getTakeOffMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", 0.0});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", 0.0});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getMaxFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtZeroPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtZeroPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtZeroPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtZeroPayloadCurrentMach()});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"BEST RANGE MACH CONDITION", _thePayloadRangeCalculator.getBestRangeMach()});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxPayloadBestRange.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _thePayloadRangeCalculator.getPaxSingleMass().times(_thePayloadRangeCalculator.getnPassMax()).doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _thePayloadRangeCalculator.getnPassMax()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtMaxPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtMaxPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtMaxPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtMaxPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT MAX FUEL"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxFuelBestRange.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", _thePayloadRangeCalculator.getPaxSingleMass().times(_thePayloadRangeCalculator.getnPassActual()).doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", _thePayloadRangeCalculator.getnPassActual()});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getMaxFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtMaxFuelBestRange()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtMaxFuelBestRange()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtMaxFuelBestRange()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtMaxFuelBestRange()});
        	dataListPayloadRange.add(new Object[] {""});
        	dataListPayloadRange.add(new Object[] {"RANGE AT ZERO PAYLOAD"});
        	dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtZeroPayloadBestRange.doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePayloadRangeCalculator.getTakeOffMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"Payload mass","kg", 0.0});
        	dataListPayloadRange.add(new Object[] {"Passengers number","", 0.0});
        	dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePayloadRangeCalculator.getMaxFuelMass().doubleValue(SI.KILOGRAM)});
        	dataListPayloadRange.add(new Object[] {"SFC","lb/(lbf*hr)", _thePayloadRangeCalculator.getSfcAtZeroPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"CL","", _thePayloadRangeCalculator.getcLAtZeroPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"CD","", _thePayloadRangeCalculator.getcDAtZeroPayloadBestRange()});
        	dataListPayloadRange.add(new Object[] {"Efficiency","", _thePayloadRangeCalculator.getEfficiencyAtZeroPayloadBestRange()});

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
        if(_taskList.contains(PerformanceEnum.V_n_DIAGRAM)) {
        	Sheet sheetVnDiagram = wb.createSheet("V-n DIAGRAM");
        	List<Object[]> dataListVnDiagram = new ArrayList<>();

        	dataListVnDiagram.add(new Object[] {"Description","Unit","Value"});
        	dataListVnDiagram.add(new Object[] {"REGULATION",_theAircraft.getRegulations().toString()});
        	dataListVnDiagram.add(new Object[] {"AIRCRAFT TYPE",_theAircraft.getTypeVehicle().toString()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"BASIC MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","m/s", _theEnvelopeCalculator.getStallSpeedClean().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","m/s", _theEnvelopeCalculator.getStallSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Point A"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point C"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point D"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point E"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point F"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point H"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInverted()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE"});
        	dataListVnDiagram.add(new Object[] {"Point A'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point C'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point D'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point E'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculator.getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point F'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculator.getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point H'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculator.getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInvertedWithGust()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"FLAP MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","m/s", _theEnvelopeCalculator.getStallSpeedFullFlap().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Point A_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point I"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE (with flaps)"});
        	dataListVnDiagram.add(new Object[] {"Point A'_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculator.getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point I'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculator.getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
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
		if(_taskList.contains(PerformanceEnum.TAKE_OFF)) {
			
			sb.append("\tTAKE-OFF\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceTakeOff + "\n")
			.append("\t\tRotation distance = " + _rotationDistanceTakeOff + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceTakeOff + "\n")
			.append("\t\tAOE take-off distance = " + _takeOffDistanceAOE + "\n")
			.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25 + "\n")
			.append("\t\tBalanced field length = " + _balancedFieldLength + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOff + "\n")
			.append("\t\tMiminum control speed (VMC) = " + _vMC + "\n")
			.append("\t\tDecision speed (V1) = " + _v1 + "\n")
			.append("\t\tRotation speed (V_Rot) = " + _vRotation + "\n")
			.append("\t\tLift-off speed (V_LO) = " + _vLiftOff + "\n")
			.append("\t\tTake-off safety speed (V2) = " + _v2 + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tTake-off duration = " + _takeOffDuration + "\n")
			.append("\t-------------------------------------\n")
			;
		}
		if(_taskList.contains(PerformanceEnum.CLIMB)) {
			
			sb.append("\tCLIMB\n")
			.append("\t-------------------------------------\n")
			.append("\t\tAbsolute ceiling AOE = " + _absoluteCeilingAOE + "\n")
			.append("\t\tService ceiling AOE = " + _serviceCeilingAOE + "\n")
			.append("\t\tMaximum rate of climb at cruise altitude AOE = " + _maxRateOfClimbAtCruiseAltitudeAOE + "\n")
			.append("\t\tMaximum theta at cruise altitude AOE = " + _maxThetaAtCruiseAltitudeAOE + "\n")
			.append("\t\tMinimum time to climb AOE = " + _minimumClimbTimeAOE + "\n");
			if(_climbTimeAtSpecificClimbSpeedAOE != null)
				sb.append("\t\tTime to climb at given climb speed AOE = " + _climbTimeAtSpecificClimbSpeedAOE + "\n");
			
			sb.append("\t\t.....................................\n")
			.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEI + "\n")
			.append("\t\tService ceiling OEI = " + _serviceCeilingOEI + "\n")
			.append("\t\tMinimum time to climb at absolute ceiling OEI = " + _minimumClimbTimeOEI + "\n");
			if(_climbTimeAtSpecificClimbSpeedOEI != null)
				sb.append("\t\tTime to climb at given climb speed OEI = " + _climbTimeAtSpecificClimbSpeedOEI + "\n");
			
			sb.append("\t-------------------------------------\n");
		}
		if(_taskList.contains(PerformanceEnum.CRUISE)) {
			
			sb.append("\tCRUISE\n")
			.append("\t-------------------------------------\n")
			.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMach + "\n")
			.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMach + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMach + "\n")
			.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMach + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitude + "\n")
			.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitude + "\n")
			.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitude + "\n")
			.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitude + "\n")
			.append("\t\tMin Mach at cruise altitude = " + _minMachAtCruiseAltitude + "\n")
			.append("\t\tMax Mach at cruise altitude = " + _maxMachAtCruiseAltitude + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tEfficiency at cruise altitude and Mach = " + _efficiencyAtCruiseAltitudeAndMach + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpecific range at cruise altitude and Mach = " + _specificRangeAtCruiseAltitudeAndMach + "nmi/lbs \n")
			.append("\t-------------------------------------\n");
			
		}
		if(_taskList.contains(PerformanceEnum.DESCENT)) {
			
			sb.append("\tDESCENT\n")
			.append("\t-------------------------------------\n")
			.append("\t\tDescent length = " + _totalDescentLength.to(NonSI.NAUTICAL_MILE) + "\n")
			.append("\t\tDescent duration = " + _totalDescentTime + "\n")
			.append("\t-------------------------------------\n");
			;
			
		}
		if(_taskList.contains(PerformanceEnum.LANDING)) {
			
			sb.append("\tLANDING\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceLanding + "\n")
			.append("\t\tFlare distance = " + _flareDistanceLanding + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceLanding + "\n")
			.append("\t\tLanding distance = " + _landingDistance + "\n")
			.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25 + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed landing (VsLND)= " + _vStallLanding + "\n")
			.append("\t\tTouchdown speed (V_TD) = " + _vTouchDown + "\n")
			.append("\t\tFlare speed (V_Flare) = " + _vFlare + "\n")
			.append("\t\tApproach speed (V_Approach) = " + _vApproach + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tLanding duration = " + _landingDuration + "\n")
			.append("\t-------------------------------------\n")
			;
		}
		if(_taskList.contains(PerformanceEnum.MISSION_PROFILE)) {
			
			sb.append("\tMISSION PROFILE\n")
			.append("\t-------------------------------------\n")
			.append("\t\tTotal mission distance = " + _theAircraft.getTheAnalysisManager().getReferenceRange() + "\n")
			.append("\t\tTotal mission duration = " + _totalMissionTime + "\n")
			.append("\t\tAircraft mass at mission start = " + _maximumTakeOffMass + "\n")
			.append("\t\tAircraft mass at mission end = " + _endMissionMass + "\n")
			.append("\t\tTotal fuel mass used = " + _totalFuelUsed + "\n")
			.append("\t-------------------------------------\n")
			;
			
		}
		if(_taskList.contains(PerformanceEnum.PAYLOAD_RANGE)) {
			sb.append("\tPAYLOAD-RANGE\n")
			.append(_thePayloadRangeCalculator.toString());
		}
		if(_taskList.contains(PerformanceEnum.V_n_DIAGRAM)) {
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
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											_theAircraft.getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theTakeOffCalculator = new TakeOffCalc(
					_theAircraft,
					altitude,
					mach,
					takeOffMass,
					_dtRotation,
					_dtHold,
					_kCLmax,
					_kRotation,
					_kLiftOff,
					_dragDueToEnigneFailure,
					_theOperatingConditions.getThrottleTakeOff(), 
					_kAlphaDot,
					_alphaReductionRate,
					_mu,
					_muBrake,
					wingToGroundDistance,
					_obstacleTakeOff,
					_windSpeed,
					_alphaGround,
					_theAircraft.getWing().getRiggingAngle(),
					_cLmaxTakeOff,
					_cLZeroTakeOff,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);

			// Distances:
			_groundRollDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0);
			_rotationDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1).minus(_groundRollDistanceTakeOff);
			_airborneDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2).minus(_rotationDistanceTakeOff).minus(_groundRollDistanceTakeOff);
			_takeOffDistanceAOE = _groundRollDistanceTakeOff.plus(_rotationDistanceTakeOff).plus(_airborneDistanceTakeOff);
			_takeOffDistanceFAR25 = _takeOffDistanceAOE.times(1.15);
			
			// Velocities:
			_vStallTakeOff = _theTakeOffCalculator.getvSTakeOff();
			_vRotation = _theTakeOffCalculator.getvRot();
			_vLiftOff = _theTakeOffCalculator.getvLO();
			_v2 = _theTakeOffCalculator.getTakeOffResults().getSpeed().get(2);
			
			// Duration:
			_takeOffDuration = _theTakeOffCalculator.getTakeOffResults().getTime().get(2);
			
		}
		
		public void calculateBalancedFieldLength() {
			
			_theTakeOffCalculator.calculateBalancedFieldLength();
			
			_v1 = _theTakeOffCalculator.getV1();
			_balancedFieldLength = _theTakeOffCalculator.getBalancedFieldLength();
			
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
			List<Amount<Length>> vX = _theAircraft.getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
			List<Amount<Length>> vZUpper = _theAircraft.getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();
			List<Amount<Length>> vZLower = _theAircraft.getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();
			
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
									_theAircraft.getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX()
									.plus(_theAircraft.getVTail().getXApexConstructionAxes())
									.plus(_theAircraft.getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().times(0.25))
									.doubleValue(SI.METER)
									),
							SI.METER
							);
			
			double tailConeTipToFuselageRadiusRatio = 
					_theAircraft.getFuselage().getFuselageCreator().getHeightT()
					.divide(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.getEstimatedValue();
			
			veDSCDatabaseReader.runAnalysis(
					_theAircraft.getWing().getAspectRatio(), 
					_theAircraft.getWing().getPositionRelativeToAttachment(), 
					_theAircraft.getVTail().getAspectRatio(), 
					_theAircraft.getVTail().getSpan().doubleValue(SI.METER), 
					_theAircraft.getHTail().getPositionRelativeToAttachment(),
					diameterAtVTailQuarteMAC.doubleValue(SI.METER), 
					tailConeTipToFuselageRadiusRatio
					);

			Airfoil vTailMeanAirfoil = new Airfoil(
					LiftingSurface.calculateMeanAirfoil(
							_theAircraft.getVTail()
							),
					_theAircraft.getVTail().getAerodynamicDatabaseReader()
					);
			
			if(_theAircraft.getTheAnalysisManager().getTheBalance() == null)
				_theAircraft.calculateArms(_theAircraft.getVTail(),_xCGMaxAft);
			
			// cNb vertical [1/deg]
			double cNbVertical = MomentCalc.calcCNbetaVerticalTail(
					_theAircraft.getWing().getAspectRatio(), 
					_theAircraft.getVTail().getAspectRatio(),
					_theAircraft.getVTail().getLiftingSurfaceCreator().getLiftingSurfaceArm().doubleValue(SI.METER),
					_theAircraft.getWing().getSpan().doubleValue(SI.METER),
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.getVTail().getSurface().doubleValue(SI.SQUARE_METRE), 
					_theAircraft.getVTail().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
					vTailMeanAirfoil.getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
					_theOperatingConditions.getMachTakeOff(), 
					veDSCDatabaseReader.getkFv(),
					veDSCDatabaseReader.getkWv(),
					veDSCDatabaseReader.getkHv())/(180/Math.PI);
					
			//..................................................................................
			// CALCULATING THE THRUST YAWING MOMENT
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER)
							),
					SpeedCalc.calculateTAS(
							_theOperatingConditions.getMachTakeOff(),
							_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER)
							),
					250
					);

			double[] thrust = new double[speed.length];
			if ((_theAircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP) 
					&& (Double.valueOf(
							_theAircraft
							.getPowerPlant()
							.getTurbopropEngineDatabaseReader()
							.getThrustAPR(
									_theOperatingConditions.getMachTakeOff(),
									_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							) != 0.0
						)
					)
				thrust = ThrustCalc.calculateThrustVsSpeed(
						_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_theOperatingConditions.getThrottleTakeOff(),
						_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER),
						EngineOperatingConditionEnum.APR,
						_theAircraft.getPowerPlant().getEngineType(),
						_theAircraft.getPowerPlant(),
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineNumber()-1,
						speed
						);
			else
				thrust = ThrustCalc.calculateThrustVsSpeed(
						_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_theOperatingConditions.getThrottleTakeOff(),
						_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER),
						EngineOperatingConditionEnum.TAKE_OFF,
						_theAircraft.getPowerPlant().getEngineType(),
						_theAircraft.getPowerPlant(),
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineNumber()-1,
						speed
						);

			List<Amount<Length>> enginesArms = new ArrayList<>();
			for(int i=0; i<_theAircraft.getPowerPlant().getEngineList().size(); i++)
				enginesArms.add(_theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes());
			
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
					_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
					_theAircraft.getVTail().getAspectRatio(), 
					_theAircraft.getVTail().getHighLiftDatabaseReader(),
					_theAircraft.getVTail().getAerodynamicDatabaseReader(),
					_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMaximumDeflection()
					);
			
			for(int i=0; i < thrust.length; i++){
			_yawingMomentOEI[i] = cNbVertical*
					tau*
					_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)*
					0.5*
					_theOperatingConditions.getDensityTakeOff().getEstimatedValue()*
					Math.pow(speed[i],2)*
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)*
					_theAircraft.getWing().getSpan().doubleValue(SI.METER);
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
			
			_vMC = Amount.valueOf(
					speed[indexOfVMC],
					SI.METERS_PER_SECOND
					);
			
		}

		public void plotTakeOffPerformance(String takeOffFolderPath) {

			if(_plotList.contains(PerformancePlotEnum.TAKE_OFF_SIMULATIONS))
				try {
					_theTakeOffCalculator.createTakeOffCharts(takeOffFolderPath);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

		}
		
		public void plotBalancedFieldLength(String takeOffFolderPath) {
			
			if(_plotList.contains(PerformancePlotEnum.BALANCED_FIELD_LENGTH))
				_theTakeOffCalculator.createBalancedFieldLengthChart(takeOffFolderPath);
			
		}
		
		public void plotVMC(String takeOffFolderPath) {
			
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER)
							)/_vStallTakeOff.doubleValue(SI.METERS_PER_SECOND),
					SpeedCalc.calculateTAS(
							_theOperatingConditions.getMachTakeOff(),
							_theOperatingConditions.getAltitudeTakeOff().doubleValue(SI.METER)
							)/_vStallTakeOff.doubleValue(SI.METERS_PER_SECOND),
					250
					);
			
			double[][] thrustPlotVector = new double [2][speed.length];
			
			for(int i=0; i < speed.length; i++){
			thrustPlotVector[0][i] = _thrustMomentOEI[i];
			thrustPlotVector[1][i] = _yawingMomentOEI[i];
			}
			String[] legendValue = new String[2];
			legendValue[0] = "Thrust Moment";
			legendValue[1] = "Yawning Moment";
			
			MyChartToFileUtils.plot(speed,thrustPlotVector,
					null, null, null, null,
					"V/VsTO", "Thrust - Yawing Moment",
					"", "N m",legendValue,
					takeOffFolderPath, "VMC");

			
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
				Amount<Mass> startClimbMassAOE,
				Amount<Mass> startClimbMassOEI
				) {
			
			_theClimbCalculator = new ClimbCalc(
					_theAircraft,
					_theOperatingConditions,
					_cLmaxClean,
					_polarCLClimb,
					_polarCDClimb,
					_climbSpeed,
					_dragDueToEnigneFailure
					);
			
			_theClimbCalculator.calculateClimbPerformance(
					startClimbMassAOE,
					startClimbMassOEI
					);	
			
		}
		
		public void plotClimbPerformance(String climbFolderPath) {
			
			_theClimbCalculator.plotClimbPerformance(_plotList, climbFolderPath);
			
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
			Airfoil meanAirfoil = new Airfoil(
					LiftingSurface.calculateMeanAirfoil(_theAircraft.getWing()),
					_theAircraft.getWing().getAerodynamicDatabaseReader()
					);

			_dragListAltitudeParameterization = new ArrayList<DragMap>();
			_thrustListAltitudeParameterization = new ArrayList<ThrustMap>();

			double[] speedArrayAltitudeParameterization = new double[100];

			for(int i=0; i<_altitudeListCruise.size(); i++) {
				//..................................................................................................
				speedArrayAltitudeParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_altitudeListCruise.get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean
								),
						SpeedCalc.calculateTAS(
								_theOperatingConditions.getMachCruise(),
								MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_altitudeListCruise))
								),
						100
						);
				//..................................................................................................
				_dragListAltitudeParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_altitudeListCruise.get(i).doubleValue(SI.METER),
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								speedArrayAltitudeParameterization,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);

				//..................................................................................................
				_thrustListAltitudeParameterization.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_altitudeListCruise.get(i).doubleValue(SI.METER),
								_theOperatingConditions.getThrottleCruise(),
								speedArrayAltitudeParameterization,
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineType(), 
								_theAircraft.getPowerPlant(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
								)
						);
			}

			List<Amount<Force>> thrustAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Force>> dragAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerAvailableAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerNeededAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_altitudeListCruise.size(); i++) {
				thrustAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAltitudeParameterization.get(i).getSpeed(),
										_thrustListAltitudeParameterization.get(i).getThrust(),
										SpeedCalc.calculateTAS(
												_theOperatingConditions.getMachCruise(),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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
												_theOperatingConditions.getMachCruise(),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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
												_theOperatingConditions.getMachCruise(),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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
												_theOperatingConditions.getMachCruise(),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
												)
										),
								SI.WATT
								)
						);
			}
				
				
			_thrustAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeListCruise),
									MyArrayUtils.convertListOfAmountTodoubleArray(thrustAltitudesAtCruiseMach),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							);
			_dragAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeListCruise),
									MyArrayUtils.convertListOfAmountTodoubleArray(dragAltitudesAtCruiseMach),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							);
			_powerAvailableAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeListCruise),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerAvailableAltitudesAtCruiseMach),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							);
			_powerNeededAtCruiseAltitudeAndMach = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeListCruise),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerNeededAltitudesAtCruiseMach),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean
								),
						SpeedCalc.calculateTAS(
								_theOperatingConditions.getMachCruise(),
								MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_altitudeListCruise))
								),
						100
						);
				//..................................................................................................
				_dragListWeightParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								speedArrayWeightParameterization,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
			}
			//..................................................................................................
			_thrustListWeightParameterization.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
							_theOperatingConditions.getThrottleCruise(),
							MyArrayUtils.linspace(
									SpeedCalc.calculateSpeedStall(
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
											_weightListCruise.get(0).doubleValue(SI.NEWTON),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_cLmaxClean
											),
									SpeedCalc.calculateTAS(
											_theOperatingConditions.getMachCruise(),
											MyArrayUtils.getMin(MyArrayUtils.convertListOfAmountToDoubleArray(_altitudeListCruise))
											),
									100
									),
							EngineOperatingConditionEnum.CRUISE,
							_theAircraft.getPowerPlant().getEngineType(), 
							_theAircraft.getPowerPlant(),
							_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_theAircraft.getPowerPlant().getEngineNumber(),
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
							)
					);
		}

		public void calculateFlightEnvelope(Amount<Mass> startCruiseMass) {

			Airfoil meanAirfoil = new Airfoil(
					LiftingSurface.calculateMeanAirfoil(_theAircraft.getWing()),
					_theAircraft.getWing().getAerodynamicDatabaseReader()
					);

			_intersectionList = new ArrayList<>();
			_cruiseEnvelopeList = new ArrayList<>();

			int nPointSpeed = 500;
			int nPointAltitude = 50;
			double[] altitudeArray = MyArrayUtils.linspace(
					0.0, // meter
					15000, // meter 
					nPointAltitude
					);
			double[] speedArray = new double[nPointSpeed];
			List<DragMap> dragList = new ArrayList<>();
			List<ThrustMap> thrustList = new ArrayList<>();
			
			for(int i=0; i<altitudeArray.length; i++) {
				//..................................................................................................
				speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitudeArray[i],
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean
								),
						SpeedCalc.calculateTAS(1.0, altitudeArray[i]),
						nPointSpeed
						);
				//..................................................................................................
				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								altitudeArray[i],
								(startCruiseMass
									.times(AtmosphereCalc.g0)
									.getEstimatedValue()
									),
								speedArray,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
						
				//..................................................................................................
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								altitudeArray[i],
								_theOperatingConditions.getThrottleCruise(),
								speedArray,
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineType(), 
								_theAircraft.getPowerPlant(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
								)
						);
			}
			for(int i=0; i<altitudeArray.length; i++) {
				//..................................................................................................
				speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitudeArray[i],
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean
								),
						SpeedCalc.calculateTAS(1.0, altitudeArray[i]),
						nPointSpeed
						);
				//..................................................................................................
				_intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								altitudeArray[i],
								speedArray,
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								dragList,
								thrustList
								)
						);
			}
			for (int i=0; i<altitudeArray.length; i++) 
				_cruiseEnvelopeList.add(
						PerformanceCalcUtils.calculateEnvelope(
								altitudeArray[i],
								(startCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theOperatingConditions.getThrottleCruise(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
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
		
			for(int i=0; i<_cruiseEnvelopeList.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_cruiseEnvelopeList.get(i).getAltitude()
						).getDensity()*1000/1.225; 
				
				if(_cruiseEnvelopeList.get(i).getMinSpeed() != 0.0) {
					altitudeList.add(_cruiseEnvelopeList.get(i).getAltitude());
					minSpeedTASList.add(_cruiseEnvelopeList.get(i).getMinSpeed());
					minSpeedCASList.add(_cruiseEnvelopeList.get(i).getMinSpeed()*(Math.sqrt(sigma)));
					minMachList.add(_cruiseEnvelopeList.get(i).getMinMach());
					maxSpeedTASList.add(_cruiseEnvelopeList.get(i).getMaxSpeed());
					maxSpeedCASList.add(_cruiseEnvelopeList.get(i).getMaxSpeed()*(Math.sqrt(sigma)));
					maxMachList.add(_cruiseEnvelopeList.get(i).getMaxMach());
				}
			}
				
			_minSpeesTASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedTASList),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_maxSpeesTASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedTASList),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_minSpeesCASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedCASList),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_maxSpeesCASAtCruiseAltitude = 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedCASList),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							);
			_minMachAtCruiseAltitude = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(minMachList),
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
							);
			_maxMachAtCruiseAltitude = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(maxMachList),
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
							);
			
			if(_maxMachAtCruiseAltitude < _theOperatingConditions.getMachCruise()) {
				System.err.println("THE CHOSEN CRUISE MACH NUMBER IS NOT INSIDE THE FLIGHT ENVELOPE !");
				System.exit(1);
			}
				

		}

		public void calculateEfficiency() {
			
			_efficiencyMapAltitude = new HashMap<>();
			_efficiencyMapWeight = new HashMap<>();
			
			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			for(int i=0; i<_altitudeListCruise.size(); i++) {
				List<Double> liftAltitudeParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
				for(int j=0; j<_dragListAltitudeParameterization.get(i).getSpeed().length; j++) {
					liftAltitudeParameterization.add(
							LiftCalc.calculateLift(
									_dragListAltitudeParameterization.get(i).getSpeed()[j],
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_dragListAltitudeParameterization.get(i).getAltitude(),
									LiftCalc.calculateLiftCoeff(
											(_maximumTakeOffMass
													.times(_kCruiseWeight)
													.times(AtmosphereCalc.g0)
													.getEstimatedValue()
													),
											_dragListAltitudeParameterization.get(i).getSpeed()[j],
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
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
			
			for(int i=0; i<_altitudeListCruise.size(); i++)
				efficiencyAltitudesAtCruiseMach.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAltitudeParameterization.get(i).getSpeed(),
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitude
										.get("Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude())
										),
								SpeedCalc.calculateTAS(
										_theOperatingConditions.getMachCruise(),
										_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
										)
								)
						);
			
			_efficiencyAtCruiseAltitudeAndMach = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeListCruise),
							MyArrayUtils.convertToDoublePrimitive(efficiencyAltitudesAtCruiseMach),
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
									LiftCalc.calculateLiftCoeff(
											_dragListWeightParameterization.get(i).getWeight(),
											_dragListWeightParameterization.get(i).getSpeed()[j],
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
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

			List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
			for(int i=0; i<_dragListWeightParameterization.size(); i++) {
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_thrustListWeightParameterization.get(0).getSpeed(),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								_dragListWeightParameterization,
								_thrustListWeightParameterization
								)
						);
			}
			
			Double[] machArray = null;
			Double[] efficiency = null;
			Double[] sfc = null;
			Double[] specificRange = null;
 			
			_specificRangeMap = new ArrayList<>();
			
			for(int i=0; i<_weightListCruise.size(); i++) { 

				machArray = MyArrayUtils.linspaceDouble(
						intersectionList.get(i).getMinMach(),
						intersectionList.get(i).getMaxMach(),
						_dragListWeightParameterization.get(i).getSpeed().length);

				sfc = SpecificRangeCalc.calculateSfcVsMach(
						machArray,
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineType(),
						_theAircraft.getPowerPlant()
						);

				efficiency = MyArrayUtils.convertFromDoublePrimitive(
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
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller(),
						_theAircraft.getPowerPlant().getEngineType()
						);
				
				_specificRangeMap.add(
						new SpecificRangeMap(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_theOperatingConditions.getThrottleCruise(),
								_weightListCruise.get(i).doubleValue(SI.NEWTON),
								EngineOperatingConditionEnum.CRUISE,
								specificRange,
								machArray,
								efficiency,
								sfc
								)
						);
			}
			
			int weightIndex = _weightListCruise.size()-1;
			_specificRangeAtCruiseAltitudeAndMach = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_specificRangeMap.get(weightIndex).getMach()),
							MyArrayUtils.convertToDoublePrimitive(_specificRangeMap.get(weightIndex).getSpecificRange()),
							_theOperatingConditions.getMachCruise()
							);
		}
				
		public void plotCruiseOutput(String cruiseFolderPath) {

			if(_plotList.contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustAltitudes = new ArrayList<Double[]>();
				List<String> legendAltitudes = new ArrayList<String>();

				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedAltitudeParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAltitudeParameterization.get(i).getSpeed()));
					dragAndThrustAltitudes.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAltitudeParameterization.get(i).getDrag()));
					legendAltitudes.add("Drag at " + _dragListAltitudeParameterization.get(i).getAltitude() + " m");
				}
				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedAltitudeParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAltitudeParameterization.get(i).getSpeed()));
					dragAndThrustAltitudes.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAltitudeParameterization.get(i).getThrust()));
					legendAltitudes.add("Thrust at " + _thrustListAltitudeParameterization.get(i).getAltitude() + " m");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization, dragAndThrustAltitudes,
							"Drag and Thrust curves at different altitudes",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendAltitudes,
							cruiseFolderPath, "Drag_and_Thrust_curves_altitudes"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedWeightsParameterization = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustWeights = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedWeightsParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_dragListWeightParameterization.get(i).getSpeed()));
					dragAndThrustWeights.add(MyArrayUtils.convertFromDoublePrimitive(_dragListWeightParameterization.get(i).getDrag()));
					legendWeights.add("Drag at " + Math.round(_dragListWeightParameterization.get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListWeightParameterization.get(0).getSpeed()));
				dragAndThrustWeights.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListWeightParameterization.get(0).getThrust()));
				legendWeights.add("Thrust");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization, dragAndThrustWeights,
							"Drag and Thrust curves at different weights",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendWeights,
							cruiseFolderPath, "Drag_and_Thrust_curves_weights"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}

			if(_plotList.contains(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableAltitudes = new ArrayList<Double[]>();
				List<String> legendAltitudes = new ArrayList<String>();

				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedAltitudeParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAltitudeParameterization.get(i).getSpeed()));
					powerNeededAndAvailableAltitudes.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAltitudeParameterization.get(i).getPower()));
					legendAltitudes.add("Power needed at " + _dragListAltitudeParameterization.get(i).getAltitude() + " m");
				}
				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedAltitudeParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAltitudeParameterization.get(i).getSpeed()));
					powerNeededAndAvailableAltitudes.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAltitudeParameterization.get(i).getPower()));
					legendAltitudes.add("Power available at " + _thrustListAltitudeParameterization.get(i).getAltitude() + " m");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization, powerNeededAndAvailableAltitudes,
							"Power needed and available at different altitudes",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendAltitudes,
							cruiseFolderPath, "Power_needed_and_available_altitudes"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedWeightsParameterization = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableWeights = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_altitudeListCruise.size(); i++) {
					speedWeightsParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_dragListWeightParameterization.get(i).getSpeed()));
					powerNeededAndAvailableWeights.add(MyArrayUtils.convertFromDoublePrimitive(_dragListWeightParameterization.get(i).getPower()));
					legendWeights.add("Power needed at " + Math.round(_dragListWeightParameterization.get(i).getWeight()/9.81) + " kg");
				}
				speedWeightsParameterization.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListWeightParameterization.get(0).getSpeed()));
				powerNeededAndAvailableWeights.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListWeightParameterization.get(0).getPower()));
				legendWeights.add("Power available");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization, powerNeededAndAvailableWeights,
							"Power needed and available at different weights",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendWeights,
							cruiseFolderPath, "Power_needed_and_available_weights"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			if(_plotList.contains(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE)) {
				
				List<Double> altitudeList = new ArrayList<>();
				List<Double> speedTASList = new ArrayList<>();
				List<Double> speedCASList = new ArrayList<>();
				List<Double> machList = new ArrayList<>();
			
				for(int i=0; i<_cruiseEnvelopeList.size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MIN VALUES
					if(_cruiseEnvelopeList.get(i).getMinSpeed() != 0.0) {
						altitudeList.add(_cruiseEnvelopeList.get(i).getAltitude());
						speedTASList.add(_cruiseEnvelopeList.get(i).getMinSpeed());
						speedCASList.add(_cruiseEnvelopeList.get(i).getMinSpeed()*(Math.sqrt(sigma)));
						machList.add(_cruiseEnvelopeList.get(i).getMinMach());
					}
				}
				for(int i=0; i<_cruiseEnvelopeList.size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getAltitude()
							).getDensity()*1000/1.225; 
					
					// MAX VALUES
					if(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed() != 0.0) {
						altitudeList.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getAltitude());
						speedTASList.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed());
						speedCASList.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxSpeed()*(Math.sqrt(sigma)));
						machList.add(_cruiseEnvelopeList.get(_cruiseEnvelopeList.size()-1-i).getMaxMach());
					}
				}
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedTASList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList),
						null, null, 0.0, null,
						"Speed(TAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_TAS"
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedCASList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList),
						null, null, 0.0, null,
						"Speed(CAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_CAS"
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(machList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList),
						null, null, 0.0, null,
						"Mach", "Altitude",
						"", "m",
						cruiseFolderPath, "Cruise_flight_envelope_MACH"
						);
				
			}
			
			if(_plotList.contains(PerformancePlotEnum.EFFICIENCY_CURVES)) {
				
				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedListAltitudeParameterization = new ArrayList<>();
				List<Double[]> efficiencyListAltitudeParameterization = new ArrayList<>();
				List<String> legendAltitude = new ArrayList<>();
				for(int i=0; i<_altitudeListCruise.size(); i++) {
					speedListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoublePrimitive(
									_dragListAltitudeParameterization.get(i).getSpeed()
									)
							);
					efficiencyListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoublePrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyMapAltitude.get(
													"Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude()
													)
											)
									)
							);
					legendAltitude.add("Altitude = " + _dragListAltitudeParameterization.get(i).getAltitude());
				}
				
				try {
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendAltitude,
							cruiseFolderPath, "Efficiency_curves_altitude"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedListWeightParameterization = new ArrayList<>();
				List<Double[]> efficiencyListWeightParameterization = new ArrayList<>();
				List<String> legendWeight = new ArrayList<>();
				for(int i=0; i<_weightListCruise.size(); i++) {
					speedListWeightParameterization.add(
							MyArrayUtils.convertFromDoublePrimitive(
									_dragListWeightParameterization.get(i).getSpeed()
									)
							);
					efficiencyListWeightParameterization.add(
							MyArrayUtils.convertFromDoublePrimitive(
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
							speedListWeightParameterization, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights"
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			
			if(_plotList.contains(PerformancePlotEnum.CRUISE_GRID_CHART)) {
				
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
							cruiseFolderPath
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
				Amount<Length> endDescentAltitude
				) {
			
			_theDescentCalculator = new DescentCalc(
					_theAircraft,
					_speedDescentCAS,
					_rateOfDescent,
					initialDescentAltitude,
					endDescentAltitude
					);
					
			_theDescentCalculator.calculateDescentPerformance();
			_descentLengths = _theDescentCalculator.getDescentLengths();
			_descentTimes = _theDescentCalculator.getDescentTimes();
			_descentAngles = _theDescentCalculator.getDescentAngles();
			_totalDescentLength = _theDescentCalculator.getTotalDescentLength();
			_totalDescentTime = _theDescentCalculator.getTotalDescentTime();
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
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											_theAircraft.getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theLandingCalculator = new LandingCalc(
					_theAircraft, 
					_theOperatingConditions,
					landingMass,
					_kApproach,
					_kFlare,
					_kTouchDown,
					_mu,
					_muBrake,
					wingToGroundDistance,
					_obstacleLanding, 
					_windSpeed,
					_alphaGround,
					_theAircraft.getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE),
					_thetaApproach,
					_cLmaxLanding,
					_cLZeroLanding,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
					_theOperatingConditions.getReverseThrottleLanding(),
					_freeRollDuration
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
			if(_plotList.contains(PerformancePlotEnum.LANDING_SIMULATIONS)) {
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
		
		public void fromRangeBreguet() {
			
			// TODO : CALCULATE OSWALD FACTOR FROM DRAG POLAR (K*CL^2). 
			//		  THIS IS REQUIRED FOR THE BEST RANGE CALCULATION
			
			_thePayloadRangeCalculator = new PayloadRangeCalc(
					_theAircraft,
					_theOperatingConditions,
					_maximumTakeOffMass,
					_operatingEmptyMass,
					_maximumFuelMass,
					_polarCLCruise,
					_polarCDCruise,
					_theOperatingConditions.getMachCruise(),
					_theOperatingConditions.getAltitudeCruise(),
					_singlePassengerMass
					);
			
			//------------------------------------------------------------
			// CRUISE MACH AND BEST RANGE MACH COMPARISON
			_thePayloadRangeCalculator.createPayloadRangeMachComparison();
			
			//------------------------------------------------------------
			// MAX TAKE-OFF MASS PARAMETERIZATION
			_thePayloadRangeCalculator.createPayloadRangeMaxTakeOffMassParameterization();
			
			//------------------------------------------------------------
			// COLLECTING RESULTS
			_bestRangeMach = _thePayloadRangeCalculator.getBestRangeMach();
			
			_rangeAtMaxPayloadBestRange = _thePayloadRangeCalculator.getRangeAtMaxPayloadBestRange();
			_rangeAtMaxPayloadCurrentMach = _thePayloadRangeCalculator.getRangeAtMaxPayloadCurrentMach();
			_fuelMassAtMaxPayload = _thePayloadRangeCalculator.getFuelMass();
			_cLAtMaxPayloadBestRange = _thePayloadRangeCalculator.getcLAtMaxPayloadBestRange();
			_cLAtMaxPayloadCurrentMach = _thePayloadRangeCalculator.getcLAtMaxPayloadCurrentMach();
			_cDAtMaxPayloadBestRange = _thePayloadRangeCalculator.getcDAtMaxPayloadBestRange();
			_cDAtMaxPayloadCurrentMach = _thePayloadRangeCalculator.getcDAtMaxPayloadCurrentMach();
			_efficiencyAtMaxPayloadBestRange = _thePayloadRangeCalculator.getEfficiencyAtMaxPayloadBestRange();
			_efficiencyAtMaxPayloadCurrentMach = _thePayloadRangeCalculator.getEfficiencyAtMaxPayloadCurrentMach();
			_sfcAtMaxPayloadBestRange = _thePayloadRangeCalculator.getSfcAtMaxPayloadBestRange();
			_sfcAtMaxPayloadCurrentMach = _thePayloadRangeCalculator.getSfcAtMaxPayloadCurrentMach();
			
			_rangeAtMaxFuelBestRange = _thePayloadRangeCalculator.getRangeAtMaxFuelBestRange();
			_rangeAtMaxFuelCurrentMach = _thePayloadRangeCalculator.getRangeAtMaxFuelCurrentMach();
			_payloadAtMaxFuel = _thePayloadRangeCalculator.getPayloadMaxFuel();
			_numberOfPassengersAtMaxFuel = _thePayloadRangeCalculator.getnPassActual();
			_cLAtMaxFuelBestRange = _thePayloadRangeCalculator.getcLAtMaxFuelBestRange();
			_cLAtMaxFuelCurrentMach = _thePayloadRangeCalculator.getcLAtMaxFuelCurrentMach();
			_cDAtMaxFuelBestRange = _thePayloadRangeCalculator.getcDAtMaxFuelBestRange();
			_cDAtMaxFuelCurrentMach = _thePayloadRangeCalculator.getcDAtMaxFuelCurrentMach();
			_efficiencyAtMaxFuelBestRange = _thePayloadRangeCalculator.getEfficiencyAtMaxFuelBestRange();
			_efficiencyAtMaxFuelCurrentMach = _thePayloadRangeCalculator.getEfficiencyAtMaxFuelCurrentMach();
			_sfcAtZeroPayloadBestRange = _thePayloadRangeCalculator.getSfcAtMaxFuelBestRange();
			_sfcAtMaxFuelCurrentMach = _thePayloadRangeCalculator.getSfcAtMaxFuelCurrentMach();	
			
			_rangeAtZeroPayloadBestRange = _thePayloadRangeCalculator.getRangeAtZeroPayloadBestRange();
			_rangeAtZeroPayloadCurrentMach = _thePayloadRangeCalculator.getRangeAtZeroPayloadCurrentMach();
			_takeOffMassAtZeroPayload = _thePayloadRangeCalculator.getTakeOffMass();
			_cLAtZeroPayloadBestRange = _thePayloadRangeCalculator.getcLAtZeroPayloadBestRange();
			_cLAtZeroPayloadCurrentMach = _thePayloadRangeCalculator.getcLAtZeroPayloadCurrentMach();
			_cDAtZeroPayloadBestRange = _thePayloadRangeCalculator.getcDAtZeroPayloadBestRange();
			_cDAtZeroPayloadCurrentMach = _thePayloadRangeCalculator.getcDAtZeroPayloadCurrentMach();
			_efficiencyAtZeroPayloadBestRange = _thePayloadRangeCalculator.getEfficiencyAtZeroPayloadBestRange();
			_efficiencyAtZeroPayloadCurrentMach = _thePayloadRangeCalculator.getEfficiencyAtZeroPayloadCurrentMach();
			_sfcAtMaxFuelBestRange = _thePayloadRangeCalculator.getSfcAtZeroPayloadBestRange();	
			_sfcAtZeroPayloadCurrentMach = _thePayloadRangeCalculator.getSfcAtZeroPayloadCurrentMach();
			
			_rangeArrayBestRange = _thePayloadRangeCalculator.getRangeArrayBestRange();
			_rangeArrayCurrentMach = _thePayloadRangeCalculator.getRangeArrayCurrentMach();
			_payloadArray = _thePayloadRangeCalculator.getPayloadArray();
			
			_rangeMatrix = _thePayloadRangeCalculator.getRangeMatrix();
			_payloadMatrix = _thePayloadRangeCalculator.getPayloadMatrix();
		}
		public void plotPayloadRange(String payloadRangeFolderPath) {
			
			if(_plotList.contains(PerformancePlotEnum.PAYLOAD_RANGE)) {
				_thePayloadRangeCalculator.createPayloadRangeChartsMachParameterization(payloadRangeFolderPath);
				_thePayloadRangeCalculator.createPayloadRangeChartsMaxTakeOffMassParameterization(payloadRangeFolderPath);
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
					_theAircraft.getRegulations(),
					_theAircraft.getTypeVehicle(),
					_cLmaxClean,
					_cLmaxLanding,
					_cLmaxInverted,
					_theAircraft.getTheAnalysisManager().getPositiveLimitLoadFactor(),
					_theAircraft.getTheAnalysisManager().getNegativeLimitLoadFactor(),
					_theAircraft.getTheAnalysisManager().getVMaxCruise(),
					_theAircraft.getTheAnalysisManager().getVDive(),
					_cLAlphaClean.to(SI.RADIAN),
					_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(),
					_theOperatingConditions.getAltitudeCruise(),
					_maximumTakeOffMass,
					_maximumTakeOffMass.times(_kLandingWeight),
					_theAircraft.getWing().getSurface()
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
			if(_plotList.contains(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM)) 
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
			
			Amount<Mass> firstGuessInitialMissionMass = _operatingEmptyMass
					.plus(_singlePassengerMass.times(_theAircraft.getCabinConfiguration().getNPax()))
					.plus(_firstGuessInitialMissionFuelMass); 
			
			_theMissionProfileCalculator = new MissionProfileCalc(
					_theAircraft,
					_theOperatingConditions,
					firstGuessInitialMissionMass,
					_takeOffMissionAltitude,
					_firstGuessCruiseLength,
					_cruiseMissionMachNumber,
					_alternateCruiseLength,
					_alternateCruiseAltitude,
					_alternateCruiseMachNumber,
					_holdingDuration,
					_holdingAltitude,
					_holdingMachNumber,
					_landingFuelFlow,
					_cLmaxClean,
					_cLAlphaClean,
					_cLmaxTakeOff,
					_cLAlphaTakeOff,
					_cLZeroTakeOff,
					_cLmaxLanding,
					_cLAlphaLanding,
					_cLZeroLanding,
					_polarCLCruise,
					_polarCDCruise,
					_polarCLClimb,
					_polarCDClimb,
					_polarCLTakeOff,
					_polarCDTakeOff,
					_polarCLLanding,
					_polarCDLanding,
					_windSpeed,
					_mu,
					_muBrake,
					_dtRotation,
					_dtHold,
					_alphaGround,
					_obstacleTakeOff,
					_kRotation,
					_kLiftOff,
					_kCLmax,
					_dragDueToEnigneFailure,
					_kAlphaDot,
					_alphaReductionRate,
					_obstacleLanding,
					_thetaApproach,
					_kApproach,
					_kFlare,
					_kTouchDown,
					_freeRollDuration,
					_climbSpeed,
					_speedDescentCAS,
					_rateOfDescent
					);
				
			_theMissionProfileCalculator.calculateProfiles();
			
			_altitudeList = _theMissionProfileCalculator.getAltitudeList();
			_rangeList = _theMissionProfileCalculator.getRangeList();
			_timeList = _theMissionProfileCalculator.getTimeList();
			_fuelUsedList = _theMissionProfileCalculator.getFuelUsedList();
			_massList = _theMissionProfileCalculator.getMassList();
			_totalFuelUsed = _theMissionProfileCalculator.getTotalFuelUsed();
			_totalMissionTime = _theMissionProfileCalculator.getTotalMissionTime();
			_endMissionMass = _theMissionProfileCalculator.getEndMissionMass();
			
		}
		
		public void plotProfiles(String missionProfilesFolderPath) {
			
			_theMissionProfileCalculator.plotProfiles(
					_plotList,
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
	public String getId() {
		return _id;
	}
	public void setId(String _id) {
		this._id = _id;
	}
	public Aircraft getTheAircraft() {
		return _theAircraft;
	}
	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}
	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}
	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}
	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}
	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}
	public Amount<Mass> getMaximumFuelMass() {
		return _maximumFuelMass;
	}
	public void setMaximumFuelMass(Amount<Mass> _maximumFuelMass) {
		this._maximumFuelMass = _maximumFuelMass;
	}
	public Amount<Mass> getSinglePassengerMass() {
		return _singlePassengerMass;
	}

	public void setSinglePassengerMass(Amount<Mass> _singlePassengerMass) {
		this._singlePassengerMass = _singlePassengerMass;
	}

	public Double getCLmaxClean() {
		return _cLmaxClean;
	}
	public void setCLmaxClean(Double _cLmaxClean) {
		this._cLmaxClean = _cLmaxClean;
	}
	public Amount<?> getCLAlphaClean() {
		return _cLAlphaClean;
	}
	public void setCLAlphaClean(Amount<?> _cLAlphaClean) {
		this._cLAlphaClean = _cLAlphaClean;
	}
	public Amount<?> getCLAlphaTakeOff() {
		return _cLAlphaTakeOff;
	}
	public void setCLAlphaTakeOff(Amount<?> _cLAlphaHighLift) {
		this._cLAlphaTakeOff = _cLAlphaHighLift;
	}
	public Amount<?> getCLAlphaLanding() {
		return _cLAlphaLanding;
	}
	public void setCLAlphaLanding(Amount<?> _cLAlphaLanding) {
		this._cLAlphaLanding = _cLAlphaLanding;
	}
	public Amount<Duration> getDtRotation() {
		return _dtRotation;
	}
	public void setDtRotation(Amount<Duration> _dtRotation) {
		this._dtRotation = _dtRotation;
	}
	public Amount<Duration> getDtHold() {
		return _dtHold;
	}
	public void setDtHold(Amount<Duration> _dtHold) {
		this._dtHold = _dtHold;
	}
	public Amount<Angle> getAlphaGround() {
		return _alphaGround;
	}
	public void setAlphaGround(Amount<Angle> _alphaGround) {
		this._alphaGround = _alphaGround;
	}
	public Amount<Velocity> getWindSpeed() {
		return _windSpeed;
	}
	public void setWindSpeed(Amount<Velocity> _windSpeed) {
		this._windSpeed = _windSpeed;
	}
	public Amount<Length> getObstacleTakeOff() {
		return _obstacleTakeOff;
	}
	public void setObstacleTakeOff(Amount<Length> _obstacleTakeOff) {
		this._obstacleTakeOff = _obstacleTakeOff;
	}
	public Double getMu() {
		return _mu;
	}
	public void setMu(Double _mu) {
		this._mu = _mu;
	}
	public Double getMuBrake() {
		return _muBrake;
	}
	public void setMuBrake(Double _muBrake) {
		this._muBrake = _muBrake;
	}
	public Double getCLmaxTakeOff() {
		return _cLmaxTakeOff;
	}
	public void setCLmaxTakeOff(Double _cLmaxTakeOff) {
		this._cLmaxTakeOff = _cLmaxTakeOff;
	}
	public Double getCLZeroTakeOff() {
		return _cLZeroTakeOff;
	}
	public void setCLZeroTakeOff(Double _cLZeroTakeOff) {
		this._cLZeroTakeOff = _cLZeroTakeOff;
	}
	public Double getCLZeroLanding() {
		return _cLZeroLanding;
	}
	public void setCLZeroLanding(Double _cLZeroLanding) {
		this._cLZeroLanding = _cLZeroLanding;
	}
	public Double getKRotation() {
		return _kRotation;
	}
	public void setKRotation(Double _kRotation) {
		this._kRotation = _kRotation;
	}
	public Double getKLiftOff() {
		return _kLiftOff;
	}
	public void setKLiftOff(Double _kLiftOff) {
		this._kLiftOff = _kLiftOff;
	}
	public Double getKCLmax() {
		return _kCLmax;
	}
	public void setKCLmax(Double _kCLmax) {
		this._kCLmax = _kCLmax;
	}
	public Double getDragDueToEngineFailure() {
		return _dragDueToEnigneFailure;
	}
	public void setDragDueToEngineFailure(Double _dragDueToFailure) {
		this._dragDueToEnigneFailure = _dragDueToFailure;
	}
	public Double getKAlphaDot() {
		return _kAlphaDot;
	}
	public void setKAlphaDot(Double _kAlphaDot) {
		this._kAlphaDot = _kAlphaDot;
	}
	public Double getAlphaReductionRate() {
		return _alphaReductionRate;
	}
	public void setAlphaReductionRate(Double _alphaReductionRate) {
		this._alphaReductionRate = _alphaReductionRate;
	}
	public Double getCLmaxLanding() {
		return _cLmaxLanding;
	}
	public void setCLmaxLanding(Double _cLmaxLanding) {
		this._cLmaxLanding = _cLmaxLanding;
	}
	public Amount<Length> getObstacleLanding() {
		return _obstacleLanding;
	}
	public void setObstacleLanding(Amount<Length> _obstacleLanding) {
		this._obstacleLanding = _obstacleLanding;
	}
	public Amount<Angle> getThetaApproach() {
		return _thetaApproach;
	}
	public void setThetaApproach(Amount<Angle> _thetaApproach) {
		this._thetaApproach = _thetaApproach;
	}
	public Double getKApproach() {
		return _kApproach;
	}
	public void setKApproach(Double _kApproach) {
		this._kApproach = _kApproach;
	}
	public Double getKFlare() {
		return _kFlare;
	}
	public void setKFlare(Double _kFlare) {
		this._kFlare = _kFlare;
	}
	public Double getKTouchDown() {
		return _kTouchDown;
	}
	public void setKTouchDown(Double _kTouchDown) {
		this._kTouchDown = _kTouchDown;
	}
	public Amount<Duration> getFreeRollDuration() {
		return _freeRollDuration;
	}
	public void setFreeRollDuration(Amount<Duration> _freeRollDuration) {
		this._freeRollDuration = _freeRollDuration;
	}
	public Double getCLmaxInverted() {
		return _cLmaxInverted;
	}
	public void setCLmaxInverted(Double _cLmaxInverted) {
		this._cLmaxInverted = _cLmaxInverted;
	}
	public List<PerformanceEnum> getTaskList() {
		return _taskList;
	}
	public void setTaskList(List<PerformanceEnum> _taskList) {
		this._taskList = _taskList;
	}
	public List<PerformancePlotEnum> getPlotList() {
		return _plotList;
	}
	public void setPlotList(List<PerformancePlotEnum> _plotList) {
		this._plotList = _plotList;
	}

	public Amount<Length> getTakeOffDistanceAOE() {
		return _takeOffDistanceAOE;
	}

	public void setTakeOffDistanceAOE(Amount<Length> _takeOffDistanceAOE) {
		this._takeOffDistanceAOE = _takeOffDistanceAOE;
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

	public Double[] getPolarCLCruise() {
		return _polarCLCruise;
	}

	public void setPolarCLCruise(Double[] _polarCLCruise) {
		this._polarCLCruise = _polarCLCruise;
	}

	public Double[] getPolarCDCruise() {
		return _polarCDCruise;
	}

	public void setPolarCDCruise(Double[] _polarCDCruise) {
		this._polarCDCruise = _polarCDCruise;
	}

	public Double[] getPolarCLClimb() {
		return _polarCLClimb;
	}

	public void setPolarCLClimb(Double[] _polarCLClimb) {
		this._polarCLClimb = _polarCLClimb;
	}

	public Double[] getPolarCDClimb() {
		return _polarCDClimb;
	}

	public void setPolarCDClimb(Double[] _polarCDClimb) {
		this._polarCDClimb = _polarCDClimb;
	}

	public Double[] getPolarCLTakeOff() {
		return _polarCLTakeOff;
	}

	public void setPolarCLTakeOff(Double[] _polarCLTakeOff) {
		this._polarCLTakeOff = _polarCLTakeOff;
	}

	public Double[] getPolarCDTakeOff() {
		return _polarCDTakeOff;
	}

	public void setPolarCDTakeOff(Double[] _polarCDTakeOff) {
		this._polarCDTakeOff = _polarCDTakeOff;
	}

	public Double[] getPolarCLLanding() {
		return _polarCLLanding;
	}

	public void setPolarCLLanding(Double[] _polarCLLanding) {
		this._polarCLLanding = _polarCLLanding;
	}

	public Double[] getPolarCDLanding() {
		return _polarCDLanding;
	}

	public void setPolarCDLanding(Double[] _polarCDLanding) {
		this._polarCDLanding = _polarCDLanding;
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

	public Amount<Velocity> getSpeedDescentCAS() {
		return _speedDescentCAS;
	}

	public Amount<Velocity> getRateOfDescent() {
		return _rateOfDescent;
	}

	public void setRateOfDescent(Amount<Velocity> _rateOfDescent) {
		this._rateOfDescent = _rateOfDescent;
	}

	public void setSpeedDescentCAS(Amount<Velocity> _vDescent) {
		this._speedDescentCAS = _vDescent;
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

	public PayloadRangeCalc getThePayloadRangeCalculator() {
		return _thePayloadRangeCalculator;
	}

	public void setThePayloadRangeCalculator(PayloadRangeCalc _thePayloadRangeCalculator) {
		this._thePayloadRangeCalculator = _thePayloadRangeCalculator;
	}

	public Double getBestRangeMach() {
		return _bestRangeMach;
	}

	public void setBestRangeMach(Double bestRangeMach) {
		this._bestRangeMach = bestRangeMach;
	}

	public Amount<Length> getRangeAtMaxPayloadBestRange() {
		return _rangeAtMaxPayloadBestRange;
	}

	public void setRangeAtMaxPayloadBestRange(Amount<Length> rangeAtMaxPayloadBestRange) {
		this._rangeAtMaxPayloadBestRange = rangeAtMaxPayloadBestRange;
	}

	public Amount<Length> getRangeAtMaxPayloadCurrentMach() {
		return _rangeAtMaxPayloadCurrentMach;
	}

	public void setRangeAtMaxPayloadCurrentMach(Amount<Length> rangeAtMaxPayloadCurrentMach) {
		this._rangeAtMaxPayloadCurrentMach = rangeAtMaxPayloadCurrentMach;
	}

	public Amount<Mass> getFuelMassAtMaxPayload() {
		return _fuelMassAtMaxPayload;
	}

	public void setFuelMassAtMaxPayload(Amount<Mass> fuelMassAtMaxPayload) {
		this._fuelMassAtMaxPayload = fuelMassAtMaxPayload;
	}

	public Double getcLAtMaxPayloadBestRange() {
		return _cLAtMaxPayloadBestRange;
	}

	public void setcLAtMaxPayloadBestRange(Double cLAtMaxPayloadBestRange) {
		this._cLAtMaxPayloadBestRange = cLAtMaxPayloadBestRange;
	}

	public Double getcLAtMaxPayloadCurrentMach() {
		return _cLAtMaxPayloadCurrentMach;
	}

	public void setcLAtMaxPayloadCurrentMach(Double cLAtMaxPayloadCurrentMach) {
		this._cLAtMaxPayloadCurrentMach = cLAtMaxPayloadCurrentMach;
	}

	public Double getcDAtMaxPayloadBestRange() {
		return _cDAtMaxPayloadBestRange;
	}

	public void setcDAtMaxPayloadBestRange(Double cDAtMaxPayloadBestRange) {
		this._cDAtMaxPayloadBestRange = cDAtMaxPayloadBestRange;
	}

	public Double getcDAtMaxPayloadCurrentMach() {
		return _cDAtMaxPayloadCurrentMach;
	}

	public void setcDAtMaxPayloadCurrentMach(Double cDAtMaxPayloadCurrentMach) {
		this._cDAtMaxPayloadCurrentMach = cDAtMaxPayloadCurrentMach;
	}

	public Double getEfficiencyAtMaxPayloadBestRange() {
		return _efficiencyAtMaxPayloadBestRange;
	}

	public void setEfficiencyAtMaxPayloadBestRange(Double efficiencyAtMaxPayloadBestRange) {
		this._efficiencyAtMaxPayloadBestRange = efficiencyAtMaxPayloadBestRange;
	}

	public Double getEfficiencyAtMaxPayloadCurrentMach() {
		return _efficiencyAtMaxPayloadCurrentMach;
	}

	public void setEfficiencyAtMaxPayloadCurrentMach(Double efficiencyAtMaxPayloadCurrentMach) {
		this._efficiencyAtMaxPayloadCurrentMach = efficiencyAtMaxPayloadCurrentMach;
	}

	public Double getSfcAtMaxPayloadBestRange() {
		return _sfcAtMaxPayloadBestRange;
	}

	public void setSfcAtMaxPayloadBestRange(Double sfcAtMaxPayloadBestRange) {
		this._sfcAtMaxPayloadBestRange = sfcAtMaxPayloadBestRange;
	}

	public Double getSfcAtMaxPayloadCurrentMach() {
		return _sfcAtMaxPayloadCurrentMach;
	}

	public void setSfcAtMaxPayloadCurrentMach(Double sfcAtMaxPayloadCurrentMach) {
		this._sfcAtMaxPayloadCurrentMach = sfcAtMaxPayloadCurrentMach;
	}

	public Amount<Length> getRangeAtMaxFuelBestRange() {
		return _rangeAtMaxFuelBestRange;
	}

	public void setRangeAtMaxFuelBestRange(Amount<Length> rangeAtMaxFuelBestRange) {
		this._rangeAtMaxFuelBestRange = rangeAtMaxFuelBestRange;
	}

	public Amount<Length> getRangeAtMaxFuelCurrentMach() {
		return _rangeAtMaxFuelCurrentMach;
	}

	public void setRangeAtMaxFuelCurrentMach(Amount<Length> rangeAtMaxFuelCurrentMach) {
		this._rangeAtMaxFuelCurrentMach = rangeAtMaxFuelCurrentMach;
	}

	public Amount<Mass> getPayloadAtMaxFuel() {
		return _payloadAtMaxFuel;
	}

	public void setPayloadAtMaxFuel(Amount<Mass> payloadAtMaxFuel) {
		this._payloadAtMaxFuel = payloadAtMaxFuel;
	}

	public Double getNumberOfPassengersAtMaxFuel() {
		return _numberOfPassengersAtMaxFuel;
	}

	public void setNumberOfPassengersAtMaxFuel(Double numberOfPassengersAtMaxFuel) {
		this._numberOfPassengersAtMaxFuel = numberOfPassengersAtMaxFuel;
	}

	public Double getcLAtMaxFuelBestRange() {
		return _cLAtMaxFuelBestRange;
	}

	public void setcLAtMaxFuelBestRange(Double cLAtMaxFuelBestRange) {
		this._cLAtMaxFuelBestRange = cLAtMaxFuelBestRange;
	}

	public Double getcLAtMaxFuelCurrentMach() {
		return _cLAtMaxFuelCurrentMach;
	}

	public void setcLAtMaxFuelCurrentMach(Double cLAtMaxFuelCurrentMach) {
		this._cLAtMaxFuelCurrentMach = cLAtMaxFuelCurrentMach;
	}

	public Double getcDAtMaxFuelBestRange() {
		return _cDAtMaxFuelBestRange;
	}

	public void setcDAtMaxFuelBestRange(Double cDAtMaxFuelBestRange) {
		this._cDAtMaxFuelBestRange = cDAtMaxFuelBestRange;
	}

	public Double getcDAtMaxFuelCurrentMach() {
		return _cDAtMaxFuelCurrentMach;
	}

	public void setcDAtMaxFuelCurrentMach(Double cDAtMaxFuelCurrentMach) {
		this._cDAtMaxFuelCurrentMach = cDAtMaxFuelCurrentMach;
	}

	public Double getEfficiencyAtMaxFuelBestRange() {
		return _efficiencyAtMaxFuelBestRange;
	}

	public void setEfficiencyAtMaxFuelBestRange(Double efficiencyAtMaxFuelBestRange) {
		this._efficiencyAtMaxFuelBestRange = efficiencyAtMaxFuelBestRange;
	}

	public Double getEfficiencyAtMaxFuelCurrentMach() {
		return _efficiencyAtMaxFuelCurrentMach;
	}

	public void setEfficiencyAtMaxFuelCurrentMach(Double efficiencyAtMaxFuelCurrentMach) {
		this._efficiencyAtMaxFuelCurrentMach = efficiencyAtMaxFuelCurrentMach;
	}

	public Double getSfcAtZeroPayloadBestRange() {
		return _sfcAtZeroPayloadBestRange;
	}

	public void setSfcAtZeroPayloadBestRange(Double sfcAtZeroPayloadBestRange) {
		this._sfcAtZeroPayloadBestRange = sfcAtZeroPayloadBestRange;
	}

	public Double getSfcAtMaxFuelCurrentMach() {
		return _sfcAtMaxFuelCurrentMach;
	}

	public void setSfcAtMaxFuelCurrentMach(Double sfcAtMaxFuelCurrentMach) {
		this._sfcAtMaxFuelCurrentMach = sfcAtMaxFuelCurrentMach;
	}

	public Amount<Length> getRangeAtZeroPayloadBestRange() {
		return _rangeAtZeroPayloadBestRange;
	}

	public void setRangeAtZeroPayloadBestRange(Amount<Length> rangeAtZeroPayloadBestRange) {
		this._rangeAtZeroPayloadBestRange = rangeAtZeroPayloadBestRange;
	}

	public Amount<Length> getRangeAtZeroPayloadCurrentMach() {
		return _rangeAtZeroPayloadCurrentMach;
	}

	public void setRangeAtZeroPayloadCurrentMach(Amount<Length> rangeAtZeroPayloadCurrentMach) {
		this._rangeAtZeroPayloadCurrentMach = rangeAtZeroPayloadCurrentMach;
	}

	public Amount<Mass> getTakeOffMassAtZeroPayload() {
		return _takeOffMassAtZeroPayload;
	}

	public void setTakeOffMassAtZeroPayload(Amount<Mass> takeOffMassAtZeroPayload) {
		this._takeOffMassAtZeroPayload = takeOffMassAtZeroPayload;
	}

	public Double getcLAtZeroPayloadBestRange() {
		return _cLAtZeroPayloadBestRange;
	}

	public void setcLAtZeroPayloadBestRange(Double cLAtZeroPayloadBestRange) {
		this._cLAtZeroPayloadBestRange = cLAtZeroPayloadBestRange;
	}

	public Double getcLAtZeroPayloadCurrentMach() {
		return _cLAtZeroPayloadCurrentMach;
	}

	public void setcLAtZeroPayloadCurrentMach(Double cLAtZeroPayloadCurrentMach) {
		this._cLAtZeroPayloadCurrentMach = cLAtZeroPayloadCurrentMach;
	}

	public Double getcDAtZeroPayloadBestRange() {
		return _cDAtZeroPayloadBestRange;
	}

	public void setcDAtZeroPayloadBestRange(Double cDAtZeroPayloadBestRange) {
		this._cDAtZeroPayloadBestRange = cDAtZeroPayloadBestRange;
	}

	public Double getcDAtZeroPayloadCurrentMach() {
		return _cDAtZeroPayloadCurrentMach;
	}

	public void setcDAtZeroPayloadCurrentMach(Double cDAtZeroPayloadCurrentMach) {
		this._cDAtZeroPayloadCurrentMach = cDAtZeroPayloadCurrentMach;
	}

	public Double getEfficiencyAtZeroPayloadBestRange() {
		return _efficiencyAtZeroPayloadBestRange;
	}

	public void setEfficiencyAtZeroPayloadBestRange(Double efficiencyAtZeroPayloadBestRange) {
		this._efficiencyAtZeroPayloadBestRange = efficiencyAtZeroPayloadBestRange;
	}

	public Double getEfficiencyAtZeroPayloadCurrentMach() {
		return _efficiencyAtZeroPayloadCurrentMach;
	}

	public void setEfficiencyAtZeroPayloadCurrentMach(double efficiencyAtZeroPayloadCurrentMach) {
		this._efficiencyAtZeroPayloadCurrentMach = efficiencyAtZeroPayloadCurrentMach;
	}

	public Double getSfcAtMaxFuelBestRange() {
		return _sfcAtMaxFuelBestRange;
	}

	public void setSfcAtMaxFuelBestRange(Double sfcAtMaxFuelBestRange) {
		this._sfcAtMaxFuelBestRange = sfcAtMaxFuelBestRange;
	}

	public Double getSfcAtZeroPayloadCurrentMach() {
		return _sfcAtZeroPayloadCurrentMach;
	}

	public void setSfcAtZeroPayloadCurrentMach(Double sfcAtZeroPayloadCurrentMach) {
		this._sfcAtZeroPayloadCurrentMach = sfcAtZeroPayloadCurrentMach;
	}

	public List<Amount<Length>> getRangeArrayBestRange() {
		return _rangeArrayBestRange;
	}

	public void setRangeArrayBestRange(List<Amount<Length>> rangeArrayBestRange) {
		this._rangeArrayBestRange = rangeArrayBestRange;
	}

	public List<Amount<Length>> getRangeArrayCurrentMach() {
		return _rangeArrayCurrentMach;
	}

	public void setRangeArrayCurrentMach(List<Amount<Length>> rangeArrayCurrentMach) {
		this._rangeArrayCurrentMach = rangeArrayCurrentMach;
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

	public Amount<Velocity> getMaxRateOfClimbAtCruiseAltitudeAOE() {
		return _maxRateOfClimbAtCruiseAltitudeAOE;
	}

	public void setMaxRateOfClimbAtCruiseAltitudeAOE(Amount<Velocity> _maxRateOfClimbAtCruiseAltitude) {
		this._maxRateOfClimbAtCruiseAltitudeAOE = _maxRateOfClimbAtCruiseAltitude;
	}

	public Amount<Angle> getMaxThetaAtCruiseAltitudeAOE() {
		return _maxThetaAtCruiseAltitudeAOE;
	}

	public void setMaxThetaAtCruiseAltitudeAOE(Amount<Angle> _maxThetaAtCruiseAltitude) {
		this._maxThetaAtCruiseAltitudeAOE = _maxThetaAtCruiseAltitude;
	}

	public Amount<Length> getAbsoluteCeilingAOE() {
		return _absoluteCeilingAOE;
	}

	public void setAbsoluteCeilingAOE(Amount<Length> _absoluteCeiling) {
		this._absoluteCeilingAOE = _absoluteCeiling;
	}

	public Amount<Length> getServiceCeilingAOE() {
		return _serviceCeilingAOE;
	}

	public void setServiceCeilingAOE(Amount<Length> _serviceCeiling) {
		this._serviceCeilingAOE = _serviceCeiling;
	}

	public Amount<Duration> getMinimumClimbTimeAOE() {
		return _minimumClimbTimeAOE;
	}

	public void setMinimumClimbTimeAOE(Amount<Duration> _minimumClimbTime) {
		this._minimumClimbTimeAOE = _minimumClimbTime;
	}

	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedAOE() {
		return _climbTimeAtSpecificClimbSpeedAOE;
	}

	public void setClimbTimeAtSpecificClimbSpeedAOE(Amount<Duration> _climbTimeAtSpecificClimbSpeed) {
		this._climbTimeAtSpecificClimbSpeedAOE = _climbTimeAtSpecificClimbSpeed;
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

	public Amount<Duration> getMinimumClimbTimeOEI() {
		return _minimumClimbTimeOEI;
	}

	public void setMinimumClimbTimeOEI(Amount<Duration> _minimumClimbTimeOEI) {
		this._minimumClimbTimeOEI = _minimumClimbTimeOEI;
	}

	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedOEI() {
		return _climbTimeAtSpecificClimbSpeedOEI;
	}

	public void setClimbTimeAtSpecificClimbSpeedOEI(Amount<Duration> _climbTimeAtSpecificClimbSpeedOEI) {
		this._climbTimeAtSpecificClimbSpeedOEI = _climbTimeAtSpecificClimbSpeedOEI;
	}

	public List<RCMap> getRCMapAOE() {
		return _rcMapAOE;
	}

	public void setRCMapAOE(List<RCMap> _rcMapAOE) {
		this._rcMapAOE = _rcMapAOE;
	}

	public List<RCMap> getRCMapOEI() {
		return _rcMapOEI;
	}

	public void setRCMapOEI(List<RCMap> _rcMapOEI) {
		this._rcMapOEI = _rcMapOEI;
	}

	public CeilingMap getCeilingMapAOE() {
		return _ceilingMapAOE;
	}

	public void setCeilingMapAOE(CeilingMap _ceilingMapAOE) {
		this._ceilingMapAOE = _ceilingMapAOE;
	}

	public CeilingMap getCeilingMapOEI() {
		return _ceilingMapOEI;
	}

	public void setCeilingMapOEI(CeilingMap _ceilingMapOEI) {
		this._ceilingMapOEI = _ceilingMapOEI;
	}

	public Amount<Velocity> getClimbSpeed() {
		return _climbSpeed;
	}

	public void setClimbSpeed(Amount<Velocity> _climbSpeed) {
		this._climbSpeed = _climbSpeed;
	}

	/**
	 * @return the _dragListAltitudeParameterization
	 */
	public List<DragMap> getDragListAltitudeParameterization() {
		return _dragListAltitudeParameterization;
	}

	/**
	 * @param _dragListAltitudeParameterization the _dragListAltitudeParameterization to set
	 */
	public void setDragListAltitudeParameterization(List<DragMap> _dragListAltitudeParameterization) {
		this._dragListAltitudeParameterization = _dragListAltitudeParameterization;
	}

	/**
	 * @return the _thrustListAltitudeParameterization
	 */
	public List<ThrustMap> getThrustListAltitudeParameterization() {
		return _thrustListAltitudeParameterization;
	}

	/**
	 * @param _thrustListAltitudeParameterization the _thrustListAltitudeParameterization to set
	 */
	public void setThrustListAltitudeParameterization(List<ThrustMap> _thrustListAltitudeParameterization) {
		this._thrustListAltitudeParameterization = _thrustListAltitudeParameterization;
	}

	/**
	 * @return the _dragListWeightParameterization
	 */
	public List<DragMap> getDragListWeightParameterization() {
		return _dragListWeightParameterization;
	}

	/**
	 * @param _dragListWeightParameterization the _dragListWeightParameterization to set
	 */
	public void setDragListWeightParameterization(List<DragMap> _dragListWeightParameterization) {
		this._dragListWeightParameterization = _dragListWeightParameterization;
	}

	/**
	 * @return the _thrustListWeightParameterization
	 */
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

	public List<Amount<Length>> getAltitudeListCruise() {
		return _altitudeListCruise;
	}

	public void setAltitudeListCruise(List<Amount<Length>> _altitudeListCruise) {
		this._altitudeListCruise = _altitudeListCruise;
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

	public List<DragMap> getDragListAOE() {
		return _dragListAOE;
	}

	public void setDragListAOE(List<DragMap> _dragListAOE) {
		this._dragListAOE = _dragListAOE;
	}

	public List<DragMap> getDragListOEI() {
		return _dragListOEI;
	}

	public void setDragListOEI(List<DragMap> _dragListOEI) {
		this._dragListOEI = _dragListOEI;
	}

	public List<ThrustMap> getThrustListAOE() {
		return _thrustListAOE;
	}

	public void setThrustListAOE(List<ThrustMap> _thrustListAOE) {
		this._thrustListAOE = _thrustListAOE;
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

	public Double getSpecificRangeAtCruiseAltitudeAndMach() {
		return _specificRangeAtCruiseAltitudeAndMach;
	}

	public void setSpecificRangeAtCruiseAltitudeAndMach(Double _specificRangeAtCruiseAltitudeAndMach) {
		this._specificRangeAtCruiseAltitudeAndMach = _specificRangeAtCruiseAltitudeAndMach;
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

	public Amount<Length> getXCGMTOM() {
		return _xCGMaxAft;
	}

	public void setXCGMTOM(Amount<Length> _xCGMaxAft) {
		this._xCGMaxAft = _xCGMaxAft;
	}

	public Amount<Velocity> getVMC() {
		return _vMC;
	}

	public void setVMC(Amount<Velocity> _vMC) {
		this._vMC = _vMC;
	}

	public Amount<Length> getXCGMaxAft() {
		return _xCGMaxAft;
	}

	public void setXCGMaxAft(Amount<Length> _xCGMaxAft) {
		this._xCGMaxAft = _xCGMaxAft;
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

	public Double getKLandingWeight() {
		return _kLandingWeight;
	}

	public void setKLandingWeight(Double _kLandingWeight) {
		this._kLandingWeight = _kLandingWeight;
	}

	public Double getKClimbWeightAOE() {
		return _kClimbWeightAOE;
	}

	public void setKClimbWeightAOE(Double _kClimbWeightAOE) {
		this._kClimbWeightAOE = _kClimbWeightAOE;
	}
	
	public Double getKClimbWeightOEI() {
		return _kClimbWeightOEI;
	}

	public void setKClimbWeightOEI(Double _kClimbWeightOEI) {
		this._kClimbWeightOEI = _kClimbWeightOEI;
	}

	public Double getKCruiseWeight() {
		return _kCruiseWeight;
	}

	public void setKCruiseWeight(Double _kCruiseWeight) {
		this._kCruiseWeight = _kCruiseWeight;
	}

	public Amount<Length> getAlternateCruiseLength() {
		return _alternateCruiseLength;
	}

	public void setAlternateCruiseLength(Amount<Length> _alternateCruiseLength) {
		this._alternateCruiseLength = _alternateCruiseLength;
	}

	public Amount<Length> getAlternateCruiseAltitude() {
		return _alternateCruiseAltitude;
	}

	public void setAlternateCruiseAltitude(Amount<Length> _alternateCruiseAltitude) {
		this._alternateCruiseAltitude = _alternateCruiseAltitude;
	}

	public Amount<Duration> getHoldingDuration() {
		return _holdingDuration;
	}

	public void setHoldingDuration(Amount<Duration> _holdingDuration) {
		this._holdingDuration = _holdingDuration;
	}

	public Amount<Length> getHoldingAltitude() {
		return _holdingAltitude;
	}

	public void setHoldingAltitude(Amount<Length> _holdingAltitude) {
		this._holdingAltitude = _holdingAltitude;
	}

	public Double getFuelReserve() {
		return _fuelReserve;
	}

	public void setFuelReserve(Double _fuelReserve) {
		this._fuelReserve = _fuelReserve;
	}

	public Amount<Length> getFirstGuessCruiseLength() {
		return _firstGuessCruiseLength;
	}

	public void setFirstGuessCruiseLength(Amount<Length> _firstGuessCruiseLength) {
		this._firstGuessCruiseLength = _firstGuessCruiseLength;
	}

	public Amount<Mass> getFirstGuessInitialMissionFuelMass() {
		return _firstGuessInitialMissionFuelMass;
	}

	public void setFirstGuessInitialMissionFuelMass(Amount<Mass> _firstGuessInitialMissionFuelMass) {
		this._firstGuessInitialMissionFuelMass = _firstGuessInitialMissionFuelMass;
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

	public Amount<Length> getTakeOffMissionAltitude() {
		return _takeOffMissionAltitude;
	}

	public void setTakeOffMissionAltitude(Amount<Length> _takeOffMissionAltitude) {
		this._takeOffMissionAltitude = _takeOffMissionAltitude;
	}

	public Double getCruiseMissionMachNumber() {
		return _cruiseMissionMachNumber;
	}

	public void setCruiseMissionMachNumber(Double _cruiseMissionMachNumber) {
		this._cruiseMissionMachNumber = _cruiseMissionMachNumber;
	}

	public DescentCalc getTheDescentCalculator() {
		return _theDescentCalculator;
	}

	public void setTheDescentCalculator(DescentCalc _theDescentCalculator) {
		this._theDescentCalculator = _theDescentCalculator;
	}

	public Double getAlternateCruiseMachNumber() {
		return _alternateCruiseMachNumber;
	}

	public void setAlternateCruiseMachNumber(Double _alternateCruiseMachNumber) {
		this._alternateCruiseMachNumber = _alternateCruiseMachNumber;
	}

	public Double getHoldingMachNumber() {
		return _holdingMachNumber;
	}

	public void setHoldingMachNumber(Double _holdingMachNumber) {
		this._holdingMachNumber = _holdingMachNumber;
	}

	public Double getLandingFuelFlow() {
		return _landingFuelFlow;
	}

	public void setLandingFuelFlow(Double _landingFuelFlow) {
		this._landingFuelFlow = _landingFuelFlow;
	}

}
