package calculators.performance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MissionPhasesEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class MissionProfileCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// INUPT:
	//............................................................................................
	
	public static final int numberOfStepClimb = 10;
	public static final int numberOfStepCruise = 30;
	public static final int numberOfStepDescent = 10;
	public static final int numberOfStepAlternateCruise = 10;
	public static final int numberOfStepHolding = 10; 
	
	// Geomtrical data (from Aircraft Object)
	private Aircraft theAircraft;
	
	// Mission profile info
	private Amount<Length> missionRange;
	private Amount<Length> alternateCruiseAltitude;
	private Amount<Length> alternateCruiseRange;
	private Amount<Length> holdingAltitude;
	private Amount<Duration> holdingDuration;
	private double fuelReserve;
	private Amount<Mass> firstGuessInitialFuelMass;
	
	// Operating Conditions
	private OperatingConditions theOperatingConditions;
	
	// Weights
	private Amount<Mass> maximumTakeOffMass;
	private Amount<Mass> operatingEmptyMass;
	private Amount<Mass> singlePassengerMass;
	private int deisgnPassengersNumber;
	
	// Aerodynamics
	private double cLmaxClean;
	private double cLmaxTakeOff;
	private Amount<?> cLAlphaTakeOff;
	private double cLZeroTakeOff;
	private double cLmaxLanding;
	private Amount<?> cLAlphaLanding;
	private double cLZeroLanding;
	private double[] polarCLTakeOff;
	private double[] polarCDTakeOff;
	private double[] polarCLClimb;
	private double[] polarCDClimb;
	private double[] polarCLCruise;
	private double[] polarCDCruise;
	private double[] polarCLLanding;
	private double[] polarCDLanding;
	
	// Take-Off input data
	private Amount<Velocity> windSpeed;
	private MyInterpolatingFunction mu;
	private MyInterpolatingFunction muBrake;
	private Amount<Duration> dtHold;
	private Amount<Angle> alphaGround;
	private Amount<Length> obstacleTakeOff;
	private double kRotation;
	private double kCLmaxTakeOff;
	private double dragDueToEnigneFailure;
	private double kAlphaDot;
	private double alphaDotInitial;

	// Landing input data
	private Amount<Length> obstacleLanding;
	private Amount<Angle> approachAngle;
	private double kCLmaxLanding;
	private double kApproach;
	private double kFlare;
	private double kTouchDown;
	private Amount<Duration> freeRollDuration;
	
	// Climb input data
	private Amount<Velocity> climbSpeed;
	
	// Descent input data
	private Amount<Velocity> speedDescentCAS;
	private Amount<Velocity> rateOfDescent;
	
	// Calibration factors - Thrust
	double takeOffCalibrationFactorThrust;
	double aprCalibrationFactorThrust;
	double climbCalibrationFactorThrust;
	private double continuousCalibrationFactorThrust;
	double cruiseCalibrationFactorThrust;
	double flightIdleCalibrationFactorThrust;
	double groundIdleCalibrationFactorThrust;
	
	// Calibration factors - SFC
	double takeOffCalibrationFactorSFC;
	double aprCalibrationFactorSFC;
	double climbCalibrationFactorSFC;
	double cruiseCalibrationFactorSFC;
	double flightIdleCalibrationFactorSFC;
	double groundIdleCalibrationFactorSFC;
	
	// Calibration factors - EmissionIndexNOx
	double takeOffCalibrationFactorEmissionIndexNOx;
	double aprCalibrationFactorEmissionIndexNOx;
	double climbCalibrationFactorEmissionIndexNOx;
	double cruiseCalibrationFactorEmissionIndexNOx;
	double flightIdleCalibrationFactorEmissionIndexNOx;
	double groundIdleCalibrationFactorEmissionIndexNOx;
	
	// Calibration factors - EmissionIndexCO
	double takeOffCalibrationFactorEmissionIndexCO;
	double aprCalibrationFactorEmissionIndexCO;
	double climbCalibrationFactorEmissionIndexCO;
	double cruiseCalibrationFactorEmissionIndexCO;
	double flightIdleCalibrationFactorEmissionIndexCO;
	double groundIdleCalibrationFactorEmissionIndexCO;
	
	// Calibration factors - EmissionIndexHC
	double takeOffCalibrationFactorEmissionIndexHC;
	double aprCalibrationFactorEmissionIndexHC;
	double climbCalibrationFactorEmissionIndexHC;
	double cruiseCalibrationFactorEmissionIndexHC;
	double flightIdleCalibrationFactorEmissionIndexHC;
	double groundIdleCalibrationFactorEmissionIndexHC;
	
	// Calibration factors - EmissionIndexSoot
	double takeOffCalibrationFactorEmissionIndexSoot;
	double aprCalibrationFactorEmissionIndexSoot;	
	double climbCalibrationFactorEmissionIndexSoot;
	double cruiseCalibrationFactorEmissionIndexSoot;
	double flightIdleCalibrationFactorEmissionIndexSoot;
	double groundIdleCalibrationFactorEmissionIndexSoot;
	
	// Calibration factors - EmissionIndexCO2
	double takeOffCalibrationFactorEmissionIndexCO2;
	double aprCalibrationFactorEmissionIndexCO2;
	double climbCalibrationFactorEmissionIndexCO2;
	double cruiseCalibrationFactorEmissionIndexCO2;
	double flightIdleCalibrationFactorEmissionIndexCO2;
	double groundIdleCalibrationFactorEmissionIndexCO2;
	
	// Calibration factors - EmissionIndexSOx
	double takeOffCalibrationFactorEmissionIndexSOx;
	double aprCalibrationFactorEmissionIndexSOx;
	double climbCalibrationFactorEmissionIndexSOx;
	double cruiseCalibrationFactorEmissionIndexSOx;
	double flightIdleCalibrationFactorEmissionIndexSOx;
	double groundIdleCalibrationFactorEmissionIndexSOx;
	
	// Calibration factors - EmissionIndexCO
	double takeOffCalibrationFactorEmissionIndexH2O;
	double aprCalibrationFactorEmissionIndexH2O;
	double climbCalibrationFactorEmissionIndexH2O;
	double cruiseCalibrationFactorEmissionIndexH2O;
	double flightIdleCalibrationFactorEmissionIndexH2O;
	double groundIdleCalibrationFactorEmissionIndexH2O;
	
	//............................................................................................
	// Output:
	private Boolean missionProfileStopped = Boolean.FALSE;
	
	private Map<MissionPhasesEnum, List<Amount<Length>>> rangeMap;
	private Map<MissionPhasesEnum, List<Amount<Length>>> altitudeMap;
	private Map<MissionPhasesEnum, List<Amount<Duration>>> timeMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> fuelUsedMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionNOxMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionCOMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionHCMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionSootMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionCO2Map;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionSOxMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> emissionH2OMap;
	private Map<MissionPhasesEnum, List<Amount<Mass>>> massMap;
	private Map<MissionPhasesEnum, List<Amount<Velocity>>> speedCASMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Velocity>>> speedTASMissionMap;
	private Map<MissionPhasesEnum, List<Double>> machMissionMap;
	private Map<MissionPhasesEnum, List<Double>> liftingCoefficientMissionMap;
	private Map<MissionPhasesEnum, List<Double>> dragCoefficientMissionMap;
	private Map<MissionPhasesEnum, List<Double>> efficiencyMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Force>>> dragMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Force>>> totalThrustMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Force>>> thermicThrustMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Force>>> electricThrustMissionMap;
	private Map<MissionPhasesEnum, List<Double>> sfcMissionMap;
	private Map<MissionPhasesEnum, List<Double>> fuelFlowMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Velocity>>> rateOfClimbMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Angle>>> climbAngleMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Power>>> fuelPowerMap;
	private Map<MissionPhasesEnum, List<Amount<Power>>> batteryPowerMap;
	private Map<MissionPhasesEnum, List<Amount<Energy>>> fuelEnergyMap;
	private Map<MissionPhasesEnum, List<Amount<Energy>>> batteryEnergyMap;	
	
	//----------------------------------------------------------------------
	// QUANTITES TO BE ADDED IN MAPS PER PHASE AT THE END OF THE ITERATION
	//----------------------------------------------------------------------
	// TAKE-OFF
	private List<Amount<Length>> rangeTakeOff;
	private List<Amount<Length>> altitudeTakeOff;
	private List<Amount<Duration>> timeTakeOff;
	private List<Amount<Mass>> fuelUsedTakeOff;
	private List<Amount<Mass>> aircraftMassTakeOff;
	private List<Amount<Mass>> emissionNOxTakeOff;
	private List<Amount<Mass>> emissionCOTakeOff;
	private List<Amount<Mass>> emissionHCTakeOff;
	private List<Amount<Mass>> emissionSootTakeOff;
	private List<Amount<Mass>> emissionCO2TakeOff;
	private List<Amount<Mass>> emissionSOxTakeOff;
	private List<Amount<Mass>> emissionH2OTakeOff;
	private List<Amount<Velocity>> speedTASTakeOff;
	private List<Amount<Velocity>> speedCASTakeOff;
	private List<Double> machTakeOff;
	private List<Double> cLTakeOff;
	private List<Double> cDTakeOff;
	private List<Double> efficiencyTakeOff;		
	private List<Amount<Force>> dragTakeOff;
	private List<Amount<Force>> totalThrustTakeOff;
	private List<Amount<Force>> thermicThrustTakeOff;
	private List<Amount<Force>> electricThrustTakeOff;
	private List<Double> sfcTakeOff;
	private List<Double> fuelFlowTakeOff;
	private List<Amount<Velocity>> rateOfClimbTakeOff;
	private List<Amount<Angle>> climbAngleTakeOff;
	private List<Amount<Power>> fuelPowerTakeOff;
	private List<Amount<Power>> batteryPowerTakeOff;
	private List<Amount<Energy>> fuelEnergyTakeOff;
	private List<Amount<Energy>> batteryEnergyTakeOff;
	
	//......................................................................
	// CLIMB
	private List<Amount<Length>> rangeClimb;
	private List<Amount<Length>> altitudeClimb;
	private List<Amount<Duration>> timeClimb;
	private List<Amount<Mass>> fuelUsedClimb;
	private List<Amount<Mass>> aircraftMassClimb;
	private List<Amount<Mass>> emissionNOxClimb;
	private List<Amount<Mass>> emissionCOClimb;
	private List<Amount<Mass>> emissionHCClimb;
	private List<Amount<Mass>> emissionSootClimb;
	private List<Amount<Mass>> emissionCO2Climb;
	private List<Amount<Mass>> emissionSOxClimb;
	private List<Amount<Mass>> emissionH2OClimb;
	private List<Amount<Velocity>> speedTASClimb;
	private List<Amount<Velocity>> speedCASClimb;
	private List<Double> machClimb;
	private List<Double> cLClimb;
	private List<Double> cDClimb;
	private List<Double> efficiencyClimb;		
	private List<Amount<Force>> dragClimb;
	private List<Amount<Force>> totalThrustClimb;
	private List<Amount<Force>> thermicThrustClimb;
	private List<Amount<Force>> electricThrustClimb;
	private List<Double> sfcClimb;
	private List<Double> fuelFlowClimb;
	private List<Amount<Velocity>> rateOfClimbClimb;
	private List<Amount<Angle>> climbAngleClimb;
	private List<Amount<Power>> fuelPowerClimb;
	private List<Amount<Power>> batteryPowerClimb;
	private List<Amount<Energy>> fuelEnergyClimb;
	private List<Amount<Energy>> batteryEnergyClimb;
	
	//......................................................................
	// CRUISE
	private List<Amount<Length>> rangeCruise;
	private List<Amount<Length>> altitudeCruise;
	private List<Amount<Duration>> timeCruise;
	private List<Amount<Mass>> fuelUsedCruise;
	private List<Amount<Mass>> aircraftMassCruise;
	private List<Amount<Mass>> emissionNOxCruise;
	private List<Amount<Mass>> emissionCOCruise;
	private List<Amount<Mass>> emissionHCCruise;
	private List<Amount<Mass>> emissionSootCruise;
	private List<Amount<Mass>> emissionCO2Cruise;
	private List<Amount<Mass>> emissionSOxCruise;
	private List<Amount<Mass>> emissionH2OCruise;
	private List<Amount<Velocity>> speedTASCruise;
	private List<Amount<Velocity>> speedCASCruise;
	private List<Double> machCruise;
	private List<Double> cLCruise;
	private List<Double> cDCruise;
	private List<Double> efficiencyCruise;		
	private List<Amount<Force>> dragCruise;
	private List<Amount<Force>> totalThrustCruise;
	private List<Amount<Force>> thermicThrustCruise;
	private List<Amount<Force>> electricThrustCruise;
	private List<Double> sfcCruise;
	private List<Double> fuelFlowCruise;
	private List<Amount<Velocity>> rateOfClimbCruise;
	private List<Amount<Angle>> climbAngleCruise;
	private List<Amount<Power>> fuelPowerCruise;
	private List<Amount<Power>> batteryPowerCruise;
	private List<Amount<Energy>> fuelEnergyCruise;
	private List<Amount<Energy>> batteryEnergyCruise;
	
	//......................................................................
	// FIRST DESCENT
	private List<Amount<Length>> rangeFirstDescent;
	private List<Amount<Length>> altitudeFirstDescent;
	private List<Amount<Duration>> timeFirstDescent;
	private List<Amount<Mass>> fuelUsedFirstDescent;
	private List<Amount<Mass>> aircraftMassFirstDescent;
	private List<Amount<Mass>> emissionNOxFirstDescent;
	private List<Amount<Mass>> emissionCOFirstDescent;
	private List<Amount<Mass>> emissionHCFirstDescent;
	private List<Amount<Mass>> emissionSootFirstDescent;
	private List<Amount<Mass>> emissionCO2FirstDescent;
	private List<Amount<Mass>> emissionSOxFirstDescent;
	private List<Amount<Mass>> emissionH2OFirstDescent;
	private List<Amount<Velocity>> speedTASFirstDescent;
	private List<Amount<Velocity>> speedCASFirstDescent;
	private List<Double> machFirstDescent;
	private List<Double> cLFirstDescent;
	private List<Double> cDFirstDescent;
	private List<Double> efficiencyFirstDescent;		
	private List<Amount<Force>> dragFirstDescent;
	private List<Amount<Force>> totalThrustFirstDescent;
	private List<Amount<Force>> thermicThrustFirstDescent;
	private List<Amount<Force>> electricThrustFirstDescent;
	private List<Double> sfcFirstDescent;
	private List<Double> fuelFlowFirstDescent;
	private List<Amount<Velocity>> rateOfClimbFirstDescent;
	private List<Amount<Angle>> climbAngleFirstDescent;
	private List<Amount<Power>> fuelPowerFirstDescent;
	private List<Amount<Power>> batteryPowerFirstDescent;
	private List<Amount<Energy>> fuelEnergyFirstDescent;
	private List<Amount<Energy>> batteryEnergyFirstDescent;
	
	//......................................................................
	// SECOND CLIMB
	private List<Amount<Length>> rangeSecondClimb;
	private List<Amount<Length>> altitudeSecondClimb;
	private List<Amount<Duration>> timeSecondClimb;
	private List<Amount<Mass>> fuelUsedSecondClimb;
	private List<Amount<Mass>> aircraftMassSecondClimb;
	private List<Amount<Mass>> emissionNOxSecondClimb;
	private List<Amount<Mass>> emissionCOSecondClimb;
	private List<Amount<Mass>> emissionHCSecondClimb;
	private List<Amount<Mass>> emissionSootSecondClimb;
	private List<Amount<Mass>> emissionCO2SecondClimb;
	private List<Amount<Mass>> emissionSOxSecondClimb ;
	private List<Amount<Mass>> emissionH2OSecondClimb ;
	private List<Amount<Velocity>> speedTASSecondClimb;
	private List<Amount<Velocity>> speedCASSecondClimb;
	private List<Double> machSecondClimb;
	private List<Double> cLSecondClimb;
	private List<Double> cDSecondClimb;
	private List<Double> efficiencySecondClimb;		
	private List<Amount<Force>> dragSecondClimb;
	private List<Amount<Force>> totalThrustSecondClimb;
	private List<Amount<Force>> thermicThrustSecondClimb;
	private List<Amount<Force>> electricThrustSecondClimb;
	private List<Double> sfcSecondClimb;
	private List<Double> fuelFlowSecondClimb;
	private List<Amount<Velocity>> rateOfClimbSecondClimb;
	private List<Amount<Angle>> climbAngleSecondClimb;
	private List<Amount<Power>> fuelPowerSecondClimb;
	private List<Amount<Power>> batteryPowerSecondClimb;
	private List<Amount<Energy>> fuelEnergySecondClimb;
	private List<Amount<Energy>> batteryEnergySecondClimb;
	
	//......................................................................
	// ALTERNATE CRUISE
	private List<Amount<Length>> rangeAlternateCruise;
	private List<Amount<Length>> altitudeAlternateCruise;
	private List<Amount<Duration>> timeAlternateCruise;
	private List<Amount<Mass>> fuelUsedAlternateCruise;
	private List<Amount<Mass>> aircraftMassAlternateCruise;
	private List<Amount<Mass>> emissionNOxAlternateCruise;
	private List<Amount<Mass>> emissionCOAlternateCruise;
	private List<Amount<Mass>> emissionHCAlternateCruise;
	private List<Amount<Mass>> emissionSootAlternateCruise;
	private List<Amount<Mass>> emissionCO2AlternateCruise;
	private List<Amount<Mass>> emissionSOxAlternateCruise;
	private List<Amount<Mass>> emissionH2OAlternateCruise;
	private List<Amount<Velocity>> speedTASAlternateCruise;
	private List<Amount<Velocity>> speedCASAlternateCruise;
	private List<Double> machAlternateCruise;
	private List<Double> cLAlternateCruise;
	private List<Double> cDAlternateCruise;
	private List<Double> efficiencyAlternateCruise;		
	private List<Amount<Force>> dragAlternateCruise;
	private List<Amount<Force>> totalThrustAlternateCruise;
	private List<Amount<Force>> thermicThrustAlternateCruise;
	private List<Amount<Force>> electricThrustAlternateCruise;
	private List<Double> sfcAlternateCruise;
	private List<Double> fuelFlowAlternateCruise;
	private List<Amount<Velocity>> rateOfClimbAlternateCruise;
	private List<Amount<Angle>> climbAngleAlternateCruise;
	private List<Amount<Power>> fuelPowerAlternateCruise;
	private List<Amount<Power>> batteryPowerAlternateCruise;
	private List<Amount<Energy>> fuelEnergyAlternateCruise;
	private List<Amount<Energy>> batteryEnergyAlternateCruise;
	
	//......................................................................
	// SECOND DESCENT
	private List<Amount<Length>> rangeSecondDescent;
	private List<Amount<Length>> altitudeSecondDescent;
	private List<Amount<Duration>> timeSecondDescent;
	private List<Amount<Mass>> fuelUsedSecondDescent;
	private List<Amount<Mass>> aircraftMassSecondDescent;
	private List<Amount<Mass>> emissionNOxSecondDescent;
	private List<Amount<Mass>> emissionCOSecondDescent;
	private List<Amount<Mass>> emissionHCSecondDescent;
	private List<Amount<Mass>> emissionSootSecondDescent;
	private List<Amount<Mass>> emissionCO2SecondDescent;
	private List<Amount<Mass>> emissionSOxSecondDescent;
	private List<Amount<Mass>> emissionH2OSecondDescent;
	private List<Amount<Velocity>> speedTASSecondDescent;
	private List<Amount<Velocity>> speedCASSecondDescent;
	private List<Double> machSecondDescent;
	private List<Double> cLSecondDescent;
	private List<Double> cDSecondDescent;
	private List<Double> efficiencySecondDescent;		
	private List<Amount<Force>> dragSecondDescent;
	private List<Amount<Force>> totalThrustSecondDescent;
	private List<Amount<Force>> thermicThrustSecondDescent;
	private List<Amount<Force>> electricThrustSecondDescent;
	private List<Double> sfcSecondDescent;
	private List<Double> fuelFlowSecondDescent;
	private List<Amount<Velocity>> rateOfClimbSecondDescent;
	private List<Amount<Angle>> climbAngleSecondDescent;
	private List<Amount<Power>> fuelPowerSecondDescent;
	private List<Amount<Power>> batteryPowerSecondDescent;
	private List<Amount<Energy>> fuelEnergySecondDescent;
	private List<Amount<Energy>> batteryEnergySecondDescent;
	
	//......................................................................
	// HOLDING
	private List<Amount<Length>> rangeHolding;
	private List<Amount<Length>> altitudeHolding;
	private List<Amount<Duration>> timeHolding;
	private List<Amount<Mass>> fuelUsedHolding;
	private List<Amount<Mass>> aircraftMassHolding;
	private List<Amount<Mass>> emissionNOxHolding;
	private List<Amount<Mass>> emissionCOHolding;
	private List<Amount<Mass>> emissionHCHolding;
	private List<Amount<Mass>> emissionSootHolding;
	private List<Amount<Mass>> emissionCO2Holding;
	private List<Amount<Mass>> emissionSOxHolding;
	private List<Amount<Mass>> emissionH2OHolding;
	private List<Amount<Velocity>> speedTASHolding;
	private List<Amount<Velocity>> speedCASHolding;
	private List<Double> machHolding;
	private List<Double> cLHolding;
	private List<Double> cDHolding;
	private List<Double> efficiencyHolding;		
	private List<Amount<Force>> dragHolding;
	private List<Amount<Force>> totalThrustHolding;
	private List<Amount<Force>> thermicThrustHolding;
	private List<Amount<Force>> electricThrustHolding;
	private List<Double> sfcHolding;
	private List<Double> fuelFlowHolding;
	private List<Amount<Velocity>> rateOfClimbHolding;
	private List<Amount<Angle>> climbAngleHolding;
	private List<Amount<Power>> fuelPowerHolding;
	private List<Amount<Power>> batteryPowerHolding;
	private List<Amount<Energy>> fuelEnergyHolding;
	private List<Amount<Energy>> batteryEnergyHolding;
	
	//......................................................................
	// LANDING
	private List<Amount<Length>> rangeLanding;
	private List<Amount<Length>> altitudeLanding;
	private List<Amount<Duration>> timeLanding;
	private List<Amount<Mass>> fuelUsedLanding;
	private List<Amount<Mass>> aircraftMassLanding;
	private List<Amount<Mass>> emissionNOxLanding;
	private List<Amount<Mass>> emissionCOLanding;
	private List<Amount<Mass>> emissionHCLanding;
	private List<Amount<Mass>> emissionSootLanding;
	private List<Amount<Mass>> emissionCO2Landing;
	private List<Amount<Mass>> emissionSOxLanding;
	private List<Amount<Mass>> emissionH2OLanding;
	private List<Amount<Velocity>> speedTASLanding;
	private List<Amount<Velocity>> speedCASLanding;
	private List<Double> machLanding;
	private List<Double> cLLanding;
	private List<Double> cDLanding;
	private List<Double> efficiencyLanding;		
	private List<Amount<Force>> dragLanding;
	private List<Amount<Force>> totalThrustLanding;
	private List<Amount<Force>> thermicThrustLanding;
	private List<Amount<Force>> electricThrustLanding;
	private List<Double> sfcLanding;
	private List<Double> fuelFlowLanding;
	private List<Amount<Velocity>> rateOfClimbLanding;
	private List<Amount<Angle>> climbAngleLanding;
	private List<Amount<Power>> fuelPowerLanding;
	private List<Amount<Power>> batteryPowerLanding;
	private List<Amount<Energy>> fuelEnergyLanding;
	private List<Amount<Energy>> batteryEnergyLanding;
	
	//......................................................................
	// TOTAL OUTPUT QUANTITIES
	private Amount<Mass> initialFuelMass;
	private Amount<Mass> initialMissionMass;
	private Amount<Mass> endMissionMass;
	private Amount<Mass> totalFuel;
	private Amount<Mass> blockFuel;
	private Amount<Duration> totalTime;
	private Amount<Duration> blockTime;
	private Amount<Length> totalRange;
	private Amount<Power> totalFuelPower;
	private Amount<Power> totalBatteryPower;
	private Amount<Energy> totalFuelEnergy;
	private Amount<Energy> totalBatteryEnergy;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public MissionProfileCalc(
			Aircraft theAircraft, 
			OperatingConditions theOperatingConditions,
			Amount<Length> missionRange,
			Amount<Length> alternateCruiseRange,
			Amount<Length> alternateCruiseAltitude,
			Amount<Duration> holdingDuration,
			Amount<Length> holdingAltitude,
			double fuelReserve,
			Amount<Mass> firstGuessInitialFuelMass,
			Amount<Mass> maximumTakeOffMass,
			Amount<Mass> operatingEmptyMass, 
			Amount<Mass> singlePassengerMass, 
			int deisgnPassengersNumber,
			double cLmaxClean, 
			double cLmaxTakeOff,
			Amount<?> cLAlphaTakeOff, 
			double cLZeroTakeOff,
			double cLmaxLanding,
			Amount<?> cLAlphaLanding,
			double cLZeroLanding, 
			double[] polarCLTakeOff,
			double[] polarCDTakeOff,
			double[] polarCLClimb,
			double[] polarCDClimb,
			double[] polarCLCruise,
			double[] polarCDCruise,
			double[] polarCLLanding, 
			double[] polarCDLanding, 
			Amount<Velocity> windSpeed,
			MyInterpolatingFunction mu, 
			MyInterpolatingFunction muBrake,
			Amount<Duration> dtHold,
			Amount<Angle> alphaGround, 
			Amount<Length> obstacleTakeOff, 
			double kRotation,
			double kCLmaxTakeOff,
			double dragDueToEnigneFailure,
			double kAlphaDot,
			double alphaDotInitial,
			Amount<Length> obstacleLanding,
			Amount<Angle> approachAngle, 
			double kCLmaxLanding,
			double kApproach, 
			double kFlare, 
			double kTouchDown,
			Amount<Duration> freeRollDuration,
			Amount<Velocity> climbSpeed,
			Amount<Velocity> speedDescentCAS,
			Amount<Velocity> rateOfDescent,
			double takeOffCalibrationFactorThrust,
			double climbCalibrationFactorThrust,
			double continuousCalibrationFactorThrust,
			double cruiseCalibrationFactorThrust,
			double flightIdleCalibrationFactorThrust,
			double groundIdleCalibrationFactorThrust,
			double takeOffCalibrationFactorSFC,
			double climbCalibrationFactorSFC,
			double cruiseCalibrationFactorSFC,
			double flightIdleCalibrationFactorSFC,
			double groundIdleCalibrationFactorSFC,
			double takeOffCalibrationFactorEmissionIndexNOx,
			double climbCalibrationFactorEmissionIndexNOx,
			double cruiseCalibrationFactorEmissionIndexNOx,
			double flightIdleCalibrationFactorEmissionIndexNOx,
			double groundIdleCalibrationFactorEmissionIndexNOx,
			double takeOffCalibrationFactorEmissionIndexCO,
			double climbCalibrationFactorEmissionIndexCO,
			double cruiseCalibrationFactorEmissionIndexCO,
			double flightIdleCalibrationFactorEmissionIndexCO,
			double groundIdleCalibrationFactorEmissionIndexCO,
			double takeOffCalibrationFactorEmissionIndexHC,
			double climbCalibrationFactorEmissionIndexHC,
			double cruiseCalibrationFactorEmissionIndexHC,
			double flightIdleCalibrationFactorEmissionIndexHC,
			double groundIdleCalibrationFactorEmissionIndexHC,
			double takeOffCalibrationFactorEmissionIndexSoot,
			double climbCalibrationFactorEmissionIndexSoot,
			double cruiseCalibrationFactorEmissionIndexSoot,
			double flightIdleCalibrationFactorEmissionIndexSoot,
			double groundIdleCalibrationFactorEmissionIndexSoot,
			double takeOffCalibrationFactorEmissionIndexCO2,
			double climbCalibrationFactorEmissionIndexCO2,
			double cruiseCalibrationFactorEmissionIndexCO2,
			double flightIdleCalibrationFactorEmissionIndexCO2,
			double groundIdleCalibrationFactorEmissionIndexCO2,
			double takeOffCalibrationFactorEmissionIndexSOx,
			double climbCalibrationFactorEmissionIndexSOx,
			double cruiseCalibrationFactorEmissionIndexSOx,
			double flightIdleCalibrationFactorEmissionIndexSOx,
			double groundIdleCalibrationFactorEmissionIndexSOx,
			double takeOffCalibrationFactorEmissionIndexH2O,
			double climbCalibrationFactorEmissionIndexH2O,
			double cruiseCalibrationFactorEmissionIndexH2O,
			double flightIdleCalibrationFactorEmissionIndexH2O,
			double groundIdleCalibrationFactorEmissionIndexH2O
			) {
		
		this.theAircraft = theAircraft;
		this.theOperatingConditions = theOperatingConditions;
		this.missionRange = missionRange;
		this.alternateCruiseRange = alternateCruiseRange;
		this.alternateCruiseAltitude = alternateCruiseAltitude;
		this.holdingAltitude = holdingAltitude;
		this.holdingDuration = holdingDuration;
		this.fuelReserve = fuelReserve;
		this.firstGuessInitialFuelMass = firstGuessInitialFuelMass;
		this.maximumTakeOffMass = maximumTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.singlePassengerMass = singlePassengerMass;
		this.deisgnPassengersNumber = deisgnPassengersNumber;
		this.cLmaxClean = cLmaxClean;
		this.cLmaxTakeOff = cLmaxTakeOff;
		this.cLAlphaTakeOff = cLAlphaTakeOff;
		this.cLZeroTakeOff = cLZeroTakeOff;
		this.cLAlphaLanding = cLAlphaLanding;
		this.cLmaxLanding = cLmaxLanding;
		this.cLZeroLanding = cLZeroLanding;
		this.polarCLTakeOff = polarCLTakeOff;
		this.polarCDTakeOff = polarCDTakeOff;
		this.polarCLClimb = polarCLClimb;
		this.polarCDClimb = polarCDClimb;
		this.polarCLCruise = polarCLCruise;
		this.polarCDCruise = polarCDCruise;
		this.polarCLLanding = polarCLLanding;
		this.polarCDLanding = polarCDLanding;
		this.windSpeed = windSpeed;
		this.mu = mu;
		this.muBrake = muBrake;
		this.dtHold = dtHold;
		this.alphaGround = alphaGround;
		this.obstacleTakeOff = obstacleTakeOff;
		this.kRotation = kRotation;
		this.kCLmaxTakeOff = kCLmaxTakeOff;
		this.dragDueToEnigneFailure = dragDueToEnigneFailure;
		this.kAlphaDot = kAlphaDot;
		this.alphaDotInitial = alphaDotInitial;
		this.obstacleLanding = obstacleLanding;
		this.approachAngle = approachAngle;
		this.kCLmaxLanding = kCLmaxLanding;
		this.kApproach = kApproach;
		this.kFlare = kFlare;
		this.kTouchDown = kTouchDown;
		this.freeRollDuration = freeRollDuration;
		this.climbSpeed = climbSpeed;
		this.speedDescentCAS = speedDescentCAS;
		this.rateOfDescent = rateOfDescent;
		this.takeOffCalibrationFactorThrust = takeOffCalibrationFactorThrust;
		this.climbCalibrationFactorThrust = climbCalibrationFactorThrust;
		this.continuousCalibrationFactorThrust = continuousCalibrationFactorThrust;
		this.cruiseCalibrationFactorThrust = cruiseCalibrationFactorThrust;
		this.flightIdleCalibrationFactorThrust = flightIdleCalibrationFactorThrust;
		this.groundIdleCalibrationFactorThrust = groundIdleCalibrationFactorThrust;
		this.takeOffCalibrationFactorSFC = takeOffCalibrationFactorSFC;
		this.climbCalibrationFactorSFC = climbCalibrationFactorSFC;
		this.cruiseCalibrationFactorSFC = cruiseCalibrationFactorSFC;
		this.flightIdleCalibrationFactorSFC = flightIdleCalibrationFactorSFC;
		this.groundIdleCalibrationFactorSFC = groundIdleCalibrationFactorSFC;
		this.takeOffCalibrationFactorEmissionIndexNOx = takeOffCalibrationFactorEmissionIndexNOx;
		this.climbCalibrationFactorEmissionIndexNOx = climbCalibrationFactorEmissionIndexNOx;
		this.cruiseCalibrationFactorEmissionIndexNOx = cruiseCalibrationFactorEmissionIndexNOx;
		this.flightIdleCalibrationFactorEmissionIndexNOx = flightIdleCalibrationFactorEmissionIndexNOx;
		this.groundIdleCalibrationFactorEmissionIndexNOx = groundIdleCalibrationFactorEmissionIndexNOx;
		this.takeOffCalibrationFactorEmissionIndexCO = takeOffCalibrationFactorEmissionIndexCO;
		this.climbCalibrationFactorEmissionIndexCO = climbCalibrationFactorEmissionIndexCO;
		this.cruiseCalibrationFactorEmissionIndexCO = cruiseCalibrationFactorEmissionIndexCO;
		this.flightIdleCalibrationFactorEmissionIndexCO = flightIdleCalibrationFactorEmissionIndexCO;
		this.groundIdleCalibrationFactorEmissionIndexCO = groundIdleCalibrationFactorEmissionIndexCO;
		this.takeOffCalibrationFactorEmissionIndexHC = takeOffCalibrationFactorEmissionIndexHC;
		this.climbCalibrationFactorEmissionIndexHC = climbCalibrationFactorEmissionIndexHC;
		this.cruiseCalibrationFactorEmissionIndexHC = cruiseCalibrationFactorEmissionIndexHC;
		this.flightIdleCalibrationFactorEmissionIndexHC = flightIdleCalibrationFactorEmissionIndexHC;
		this.groundIdleCalibrationFactorEmissionIndexHC = groundIdleCalibrationFactorEmissionIndexHC;
		this.takeOffCalibrationFactorEmissionIndexSoot = takeOffCalibrationFactorEmissionIndexSoot;
		this.climbCalibrationFactorEmissionIndexSoot = climbCalibrationFactorEmissionIndexSoot;
		this.cruiseCalibrationFactorEmissionIndexSoot = cruiseCalibrationFactorEmissionIndexSoot;
		this.flightIdleCalibrationFactorEmissionIndexSoot = flightIdleCalibrationFactorEmissionIndexSoot;
		this.groundIdleCalibrationFactorEmissionIndexSoot = groundIdleCalibrationFactorEmissionIndexSoot;
		this.takeOffCalibrationFactorEmissionIndexCO2 = takeOffCalibrationFactorEmissionIndexCO2;
		this.climbCalibrationFactorEmissionIndexCO2 = climbCalibrationFactorEmissionIndexCO2;
		this.cruiseCalibrationFactorEmissionIndexCO2 = cruiseCalibrationFactorEmissionIndexCO2;
		this.flightIdleCalibrationFactorEmissionIndexCO2 = flightIdleCalibrationFactorEmissionIndexCO2;
		this.groundIdleCalibrationFactorEmissionIndexCO2 = groundIdleCalibrationFactorEmissionIndexCO2;
		this.takeOffCalibrationFactorEmissionIndexSOx = takeOffCalibrationFactorEmissionIndexSOx;
		this.climbCalibrationFactorEmissionIndexSOx = climbCalibrationFactorEmissionIndexSOx;
		this.cruiseCalibrationFactorEmissionIndexSOx = cruiseCalibrationFactorEmissionIndexSOx;
		this.flightIdleCalibrationFactorEmissionIndexSOx = flightIdleCalibrationFactorEmissionIndexSOx;
		this.groundIdleCalibrationFactorEmissionIndexSOx = groundIdleCalibrationFactorEmissionIndexSOx;
		this.takeOffCalibrationFactorEmissionIndexH2O = takeOffCalibrationFactorEmissionIndexH2O;
		this.climbCalibrationFactorEmissionIndexH2O = climbCalibrationFactorEmissionIndexH2O;
		this.cruiseCalibrationFactorEmissionIndexH2O = cruiseCalibrationFactorEmissionIndexH2O;
		this.flightIdleCalibrationFactorEmissionIndexH2O = flightIdleCalibrationFactorEmissionIndexH2O;
		this.groundIdleCalibrationFactorEmissionIndexH2O = groundIdleCalibrationFactorEmissionIndexH2O;
		
		this.rangeMap = new TreeMap<>();
		this.altitudeMap = new TreeMap<>();
		this.timeMap = new TreeMap<>();
		this.fuelUsedMap = new TreeMap<>();
		this.massMap = new TreeMap<>();
		this.emissionNOxMap = new TreeMap<>();
		this.emissionCOMap = new TreeMap<>();
		this.emissionHCMap = new TreeMap<>();
		this.emissionSootMap = new TreeMap<>();
		this.emissionCO2Map = new TreeMap<>();
		this.emissionSOxMap = new TreeMap<>();
		this.emissionH2OMap = new TreeMap<>();
		this.speedCASMissionMap = new TreeMap<>();
		this.speedTASMissionMap = new TreeMap<>();
		this.machMissionMap = new TreeMap<>();
		this.liftingCoefficientMissionMap = new TreeMap<>();
		this.dragCoefficientMissionMap = new TreeMap<>();
		this.efficiencyMissionMap = new TreeMap<>();
		this.dragMissionMap = new TreeMap<>();
		this.totalThrustMissionMap = new TreeMap<>();
		this.thermicThrustMissionMap = new TreeMap<>();
		this.electricThrustMissionMap = new TreeMap<>();
		this.sfcMissionMap = new TreeMap<>();
		this.fuelFlowMissionMap = new TreeMap<>();
		this.rateOfClimbMissionMap = new TreeMap<>();
		this.climbAngleMissionMap = new TreeMap<>();
		this.fuelPowerMap = new TreeMap<>();
		this.batteryPowerMap = new TreeMap<>();
		this.fuelEnergyMap = new TreeMap<>();
		this.batteryEnergyMap = new TreeMap<>();	
		
	}

	//--------------------------------------------------------------------------------------------
	// METHODS:

	private void initializePhasesLists(boolean isFuelWeightLoop, boolean isCruiseLoop, boolean isAlternateCruiseLoop) {

		if(isCruiseLoop == false && isAlternateCruiseLoop == false) {

			//----------------------------------------------------------------------
			// TAKE-OFF
			this.rangeTakeOff = new ArrayList<>();
			this.altitudeTakeOff = new ArrayList<>();
			this.timeTakeOff = new ArrayList<>();
			this.fuelUsedTakeOff = new ArrayList<>();
			this.aircraftMassTakeOff = new ArrayList<>();
			this.emissionNOxTakeOff = new ArrayList<>();
			this.emissionCOTakeOff = new ArrayList<>();
			this.emissionHCTakeOff = new ArrayList<>();
			this.emissionSootTakeOff = new ArrayList<>();
			this.emissionCO2TakeOff = new ArrayList<>();
			this.emissionSOxTakeOff = new ArrayList<>();
			this.emissionH2OTakeOff = new ArrayList<>();
			this.speedTASTakeOff = new ArrayList<>();
			this.speedCASTakeOff = new ArrayList<>();
			this.machTakeOff = new ArrayList<>();
			this.cLTakeOff = new ArrayList<>();
			this.cDTakeOff = new ArrayList<>();
			this.efficiencyTakeOff = new ArrayList<>();		
			this.dragTakeOff = new ArrayList<>();
			this.totalThrustTakeOff = new ArrayList<>();
			this.thermicThrustTakeOff = new ArrayList<>();
			this.electricThrustTakeOff = new ArrayList<>();
			this.sfcTakeOff = new ArrayList<>();
			this.fuelFlowTakeOff = new ArrayList<>();
			this.rateOfClimbTakeOff = new ArrayList<>();
			this.climbAngleTakeOff = new ArrayList<>();
			this.fuelPowerTakeOff = new ArrayList<>();
			this.batteryPowerTakeOff = new ArrayList<>();
			this.fuelEnergyTakeOff = new ArrayList<>();
			this.batteryEnergyTakeOff = new ArrayList<>();

			//......................................................................
			// CLIMB
			this.rangeClimb = new ArrayList<>();
			this.altitudeClimb = new ArrayList<>();
			this.timeClimb = new ArrayList<>();
			this.fuelUsedClimb = new ArrayList<>();
			this.aircraftMassClimb = new ArrayList<>();
			this.emissionNOxClimb = new ArrayList<>();
			this.emissionCOClimb = new ArrayList<>();
			this.emissionHCClimb = new ArrayList<>();
			this.emissionSootClimb = new ArrayList<>();
			this.emissionCO2Climb = new ArrayList<>();
			this.emissionSOxClimb = new ArrayList<>();
			this.emissionH2OClimb = new ArrayList<>();
			this.speedTASClimb = new ArrayList<>();
			this.speedCASClimb = new ArrayList<>();
			this.machClimb = new ArrayList<>();
			this.cLClimb = new ArrayList<>();
			this.cDClimb = new ArrayList<>();
			this.efficiencyClimb = new ArrayList<>();		
			this.dragClimb = new ArrayList<>();
			this.totalThrustClimb = new ArrayList<>();
			this.thermicThrustClimb = new ArrayList<>();
			this.electricThrustClimb = new ArrayList<>();
			this.sfcClimb = new ArrayList<>();
			this.fuelFlowClimb = new ArrayList<>();
			this.rateOfClimbClimb = new ArrayList<>();
			this.climbAngleClimb = new ArrayList<>();
			this.fuelPowerClimb = new ArrayList<>();
			this.batteryPowerClimb = new ArrayList<>();
			this.fuelEnergyClimb = new ArrayList<>();
			this.batteryEnergyClimb = new ArrayList<>();

		}

		if (isAlternateCruiseLoop == false) {

			//......................................................................
			// CRUISE
			this.rangeCruise = new ArrayList<>();
			this.altitudeCruise = new ArrayList<>();
			this.timeCruise = new ArrayList<>();
			this.fuelUsedCruise = new ArrayList<>();
			this.aircraftMassCruise = new ArrayList<>();
			this.emissionNOxCruise = new ArrayList<>();
			this.emissionCOCruise = new ArrayList<>();
			this.emissionHCCruise = new ArrayList<>();
			this.emissionSootCruise = new ArrayList<>();
			this.emissionCO2Cruise = new ArrayList<>();
			this.emissionSOxCruise = new ArrayList<>();
			this.emissionH2OCruise = new ArrayList<>();
			this.speedTASCruise = new ArrayList<>();
			this.speedCASCruise = new ArrayList<>();
			this.machCruise = new ArrayList<>();
			this.cLCruise = new ArrayList<>();
			this.cDCruise = new ArrayList<>();
			this.efficiencyCruise = new ArrayList<>();		
			this.dragCruise = new ArrayList<>();
			this.totalThrustCruise = new ArrayList<>();
			this.thermicThrustCruise = new ArrayList<>();
			this.electricThrustCruise = new ArrayList<>();
			this.sfcCruise = new ArrayList<>();
			this.fuelFlowCruise = new ArrayList<>();
			this.rateOfClimbCruise = new ArrayList<>();
			this.climbAngleCruise = new ArrayList<>();
			this.fuelPowerCruise = new ArrayList<>();
			this.batteryPowerCruise = new ArrayList<>();
			this.fuelEnergyCruise = new ArrayList<>();
			this.batteryEnergyCruise = new ArrayList<>();

			//......................................................................
			// FIRST DESCENT
			this.rangeFirstDescent = new ArrayList<>();
			this.altitudeFirstDescent = new ArrayList<>();
			this.timeFirstDescent = new ArrayList<>();
			this.fuelUsedFirstDescent = new ArrayList<>();
			this.aircraftMassFirstDescent = new ArrayList<>();
			this.emissionNOxFirstDescent = new ArrayList<>();
			this.emissionCOFirstDescent = new ArrayList<>();
			this.emissionHCFirstDescent = new ArrayList<>();
			this.emissionSootFirstDescent = new ArrayList<>();
			this.emissionCO2FirstDescent = new ArrayList<>();
			this.emissionSOxFirstDescent = new ArrayList<>();
			this.emissionH2OFirstDescent = new ArrayList<>();
			this.speedTASFirstDescent = new ArrayList<>();
			this.speedCASFirstDescent = new ArrayList<>();
			this.machFirstDescent = new ArrayList<>();
			this.cLFirstDescent = new ArrayList<>();
			this.cDFirstDescent = new ArrayList<>();
			this.efficiencyFirstDescent = new ArrayList<>();		
			this.dragFirstDescent = new ArrayList<>();
			this.totalThrustFirstDescent = new ArrayList<>();
			this.thermicThrustFirstDescent = new ArrayList<>();
			this.electricThrustFirstDescent = new ArrayList<>();
			this.sfcFirstDescent = new ArrayList<>();
			this.fuelFlowFirstDescent = new ArrayList<>();
			this.rateOfClimbFirstDescent = new ArrayList<>();
			this.climbAngleFirstDescent = new ArrayList<>();
			this.fuelPowerFirstDescent = new ArrayList<>();
			this.batteryPowerFirstDescent = new ArrayList<>();
			this.fuelEnergyFirstDescent = new ArrayList<>();
			this.batteryEnergyFirstDescent = new ArrayList<>();

			//......................................................................
			// SECOND CLIMB
			this.rangeSecondClimb = new ArrayList<>();
			this.altitudeSecondClimb = new ArrayList<>();
			this.timeSecondClimb = new ArrayList<>();
			this.fuelUsedSecondClimb = new ArrayList<>();
			this.aircraftMassSecondClimb = new ArrayList<>();
			this.emissionNOxSecondClimb = new ArrayList<>();
			this.emissionCOSecondClimb = new ArrayList<>();
			this.emissionHCSecondClimb = new ArrayList<>();
			this.emissionSootSecondClimb = new ArrayList<>();
			this.emissionCO2SecondClimb = new ArrayList<>();
			this.emissionSOxSecondClimb  = new ArrayList<>();
			this.emissionH2OSecondClimb  = new ArrayList<>();
			this.speedTASSecondClimb = new ArrayList<>();
			this.speedCASSecondClimb = new ArrayList<>();
			this.machSecondClimb = new ArrayList<>();
			this.cLSecondClimb = new ArrayList<>();
			this.cDSecondClimb = new ArrayList<>();
			this.efficiencySecondClimb = new ArrayList<>();		
			this.dragSecondClimb = new ArrayList<>();
			this.totalThrustSecondClimb = new ArrayList<>();
			this.thermicThrustSecondClimb = new ArrayList<>();
			this.electricThrustSecondClimb = new ArrayList<>();
			this.sfcSecondClimb = new ArrayList<>();
			this.fuelFlowSecondClimb = new ArrayList<>();
			this.rateOfClimbSecondClimb = new ArrayList<>();
			this.climbAngleSecondClimb = new ArrayList<>();
			this.fuelPowerSecondClimb = new ArrayList<>();
			this.batteryPowerSecondClimb = new ArrayList<>();
			this.fuelEnergySecondClimb = new ArrayList<>();
			this.batteryEnergySecondClimb = new ArrayList<>();

		}
		
		//......................................................................
		// ALTERNATE CRUISE
		this.rangeAlternateCruise = new ArrayList<>();
		this.altitudeAlternateCruise = new ArrayList<>();
		this.timeAlternateCruise = new ArrayList<>();
		this.fuelUsedAlternateCruise = new ArrayList<>();
		this.aircraftMassAlternateCruise = new ArrayList<>();
		this.emissionNOxAlternateCruise = new ArrayList<>();
		this.emissionCOAlternateCruise = new ArrayList<>();
		this.emissionHCAlternateCruise = new ArrayList<>();
		this.emissionSootAlternateCruise = new ArrayList<>();
		this.emissionCO2AlternateCruise = new ArrayList<>();
		this.emissionSOxAlternateCruise = new ArrayList<>();
		this.emissionH2OAlternateCruise = new ArrayList<>();
		this.speedTASAlternateCruise = new ArrayList<>();
		this.speedCASAlternateCruise = new ArrayList<>();
		this.machAlternateCruise = new ArrayList<>();
		this.cLAlternateCruise = new ArrayList<>();
		this.cDAlternateCruise = new ArrayList<>();
		this.efficiencyAlternateCruise = new ArrayList<>();		
		this.dragAlternateCruise = new ArrayList<>();
		this.totalThrustAlternateCruise = new ArrayList<>();
		this.thermicThrustAlternateCruise = new ArrayList<>();
		this.electricThrustAlternateCruise = new ArrayList<>();
		this.sfcAlternateCruise = new ArrayList<>();
		this.fuelFlowAlternateCruise = new ArrayList<>();
		this.rateOfClimbAlternateCruise = new ArrayList<>();
		this.climbAngleAlternateCruise = new ArrayList<>();
		this.fuelPowerAlternateCruise = new ArrayList<>();
		this.batteryPowerAlternateCruise = new ArrayList<>();
		this.fuelEnergyAlternateCruise = new ArrayList<>();
		this.batteryEnergyAlternateCruise = new ArrayList<>();
		
		//......................................................................
		// SECOND DESCENT
		this.rangeSecondDescent = new ArrayList<>();
		this.altitudeSecondDescent = new ArrayList<>();
		this.timeSecondDescent = new ArrayList<>();
		this.fuelUsedSecondDescent = new ArrayList<>();
		this.aircraftMassSecondDescent = new ArrayList<>();
		this.emissionNOxSecondDescent = new ArrayList<>();
		this.emissionCOSecondDescent = new ArrayList<>();
		this.emissionHCSecondDescent = new ArrayList<>();
		this.emissionSootSecondDescent = new ArrayList<>();
		this.emissionCO2SecondDescent = new ArrayList<>();
		this.emissionSOxSecondDescent = new ArrayList<>();
		this.emissionH2OSecondDescent = new ArrayList<>();
		this.speedTASSecondDescent = new ArrayList<>();
		this.speedCASSecondDescent = new ArrayList<>();
		this.machSecondDescent = new ArrayList<>();
		this.cLSecondDescent = new ArrayList<>();
		this.cDSecondDescent = new ArrayList<>();
		this.efficiencySecondDescent = new ArrayList<>();		
		this.dragSecondDescent = new ArrayList<>();
		this.totalThrustSecondDescent = new ArrayList<>();
		this.thermicThrustSecondDescent = new ArrayList<>();
		this.electricThrustSecondDescent = new ArrayList<>();
		this.sfcSecondDescent = new ArrayList<>();
		this.fuelFlowSecondDescent = new ArrayList<>();
		this.rateOfClimbSecondDescent = new ArrayList<>();
		this.climbAngleSecondDescent = new ArrayList<>();
		this.fuelPowerSecondDescent = new ArrayList<>();
		this.batteryPowerSecondDescent = new ArrayList<>();
		this.fuelEnergySecondDescent = new ArrayList<>();
		this.batteryEnergySecondDescent = new ArrayList<>();
		
		//......................................................................
		// HOLDING
		this.rangeHolding = new ArrayList<>();
		this.altitudeHolding = new ArrayList<>();
		this.timeHolding = new ArrayList<>();
		this.fuelUsedHolding = new ArrayList<>();
		this.aircraftMassHolding = new ArrayList<>();
		this.emissionNOxHolding = new ArrayList<>();
		this.emissionCOHolding = new ArrayList<>();
		this.emissionHCHolding = new ArrayList<>();
		this.emissionSootHolding = new ArrayList<>();
		this.emissionCO2Holding = new ArrayList<>();
		this.emissionSOxHolding = new ArrayList<>();
		this.emissionH2OHolding = new ArrayList<>();
		this.speedTASHolding = new ArrayList<>();
		this.speedCASHolding = new ArrayList<>();
		this.machHolding = new ArrayList<>();
		this.cLHolding = new ArrayList<>();
		this.cDHolding = new ArrayList<>();
		this.efficiencyHolding = new ArrayList<>();		
		this.dragHolding = new ArrayList<>();
		this.totalThrustHolding = new ArrayList<>();
		this.thermicThrustHolding = new ArrayList<>();
		this.electricThrustHolding = new ArrayList<>();
		this.sfcHolding = new ArrayList<>();
		this.fuelFlowHolding = new ArrayList<>();
		this.rateOfClimbHolding = new ArrayList<>();
		this.climbAngleHolding = new ArrayList<>();
		this.fuelPowerHolding = new ArrayList<>();
		this.batteryPowerHolding = new ArrayList<>();
		this.fuelEnergyHolding = new ArrayList<>();
		this.batteryEnergyHolding = new ArrayList<>();

		if(isCruiseLoop == false && isAlternateCruiseLoop == false && isFuelWeightLoop == false) {

			//......................................................................
			// LANDING
			this.rangeLanding = new ArrayList<>();
			this.altitudeLanding = new ArrayList<>();
			this.timeLanding = new ArrayList<>();
			this.fuelUsedLanding = new ArrayList<>();
			this.aircraftMassLanding = new ArrayList<>();
			this.emissionNOxLanding = new ArrayList<>();
			this.emissionCOLanding = new ArrayList<>();
			this.emissionHCLanding = new ArrayList<>();
			this.emissionSootLanding = new ArrayList<>();
			this.emissionCO2Landing = new ArrayList<>();
			this.emissionSOxLanding = new ArrayList<>();
			this.emissionH2OLanding = new ArrayList<>();
			this.speedTASLanding = new ArrayList<>();
			this.speedCASLanding = new ArrayList<>();
			this.machLanding = new ArrayList<>();
			this.cLLanding = new ArrayList<>();
			this.cDLanding = new ArrayList<>();
			this.efficiencyLanding = new ArrayList<>();		
			this.dragLanding = new ArrayList<>();
			this.totalThrustLanding = new ArrayList<>();
			this.thermicThrustLanding = new ArrayList<>();
			this.electricThrustLanding = new ArrayList<>();
			this.sfcLanding = new ArrayList<>();
			this.fuelFlowLanding = new ArrayList<>();
			this.rateOfClimbLanding = new ArrayList<>();
			this.climbAngleLanding = new ArrayList<>();
			this.fuelPowerLanding = new ArrayList<>();
			this.batteryPowerLanding = new ArrayList<>();
			this.fuelEnergyLanding = new ArrayList<>();
			this.batteryEnergyLanding = new ArrayList<>();

		}
	}
	
	public void calculateProfiles(Amount<Velocity> vMC) {

		initializePhasesLists(false, false, false);
		
		initialMissionMass = operatingEmptyMass
				.plus(singlePassengerMass.times(deisgnPassengersNumber))
				.plus(firstGuessInitialFuelMass); 

		initialFuelMass = firstGuessInitialFuelMass;

		//----------------------------------------------------------------------
		// ERROR FLAGS
		boolean cruiseMaxMachNumberErrorFlag = false;
		boolean alternateCruiseBestMachNumberErrorFlag = false;
		boolean holdingBestMachNumberErrorFlag = false;
		
		//----------------------------------------------------------------------
		// PHASE CALCULATORS
		TakeOffCalc theTakeOffCalculator = null;
		ClimbCalc theClimbCalculator = null;
		ClimbCalc theSecondClimbCalculator = null;
		DescentCalc theFirstDescentCalculator = null;
		DescentCalc theSecondDescentCalculator = null;
		LandingCalc theLandingCalculator = null;
		
		//----------------------------------------------------------------------
		// ITERATION START ...
		//----------------------------------------------------------------------
		if(initialMissionMass.doubleValue(SI.KILOGRAM) > maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {
			initialMissionMass = maximumTakeOffMass;
			initialFuelMass = maximumTakeOffMass
					.minus(operatingEmptyMass)
					.minus(singlePassengerMass.times(deisgnPassengersNumber)); 
		}
		
		Amount<Length> currentCruiseRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Length> currentAlternateCruiseRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> newInitialFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		totalFuel = Amount.valueOf(0.0, SI.KILOGRAM);
		int i = 0;

		do {
			
			if(i>0)
				initializePhasesLists(true, false, false);

			if(i >= 1)
				initialFuelMass = newInitialFuelMass;

			if(i > 100) {
				System.err.println("WARNING: (MISSION PROFILE) MAXIMUM NUMBER OF ITERATION REACHED");
				break;
			}

			//--------------------------------------------------------------------
			// TAKE-OFF
			Amount<Length> wingToGroundDistance = 
					theAircraft.getFuselage().getHeightFromGround()
					.plus(theAircraft.getFuselage().getSectionCylinderHeight().divide(2))
					.plus(theAircraft.getWing().getZApexConstructionAxes()
							.plus(theAircraft.getWing().getSemiSpan()
									.times(
											Math.sin(
													theAircraft.getWing()	
													.getDihedralMean()
													.doubleValue(SI.RADIAN)
													)
											)
									)
							);

			theTakeOffCalculator = new TakeOffCalc(
					theAircraft.getWing().getAspectRatio(),
					theAircraft.getWing().getSurfacePlanform(),
					theAircraft.getFuselage().getUpsweepAngle(),
					theAircraft.getPowerPlant(),
					polarCLTakeOff,
					polarCDTakeOff,
					theOperatingConditions.getAltitudeTakeOff(),
					theOperatingConditions.getDeltaTemperatureTakeOff(),
					maximumTakeOffMass,
					dtHold,
					kCLmaxTakeOff,
					kRotation,
					alphaDotInitial,
					dragDueToEnigneFailure, 
					1.0, // throttle take-off (100%) 
					kAlphaDot,
					mu,
					muBrake,
					obstacleTakeOff, 
					wingToGroundDistance,
					windSpeed,
					alphaGround,
					cLmaxTakeOff,
					cLZeroTakeOff, 
					cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
					takeOffCalibrationFactorThrust, 
					1.0, // apr thrust correction factor (not needed)
					groundIdleCalibrationFactorThrust, 
					takeOffCalibrationFactorSFC, 
					1.0, // apr SFC correction factor (not needed)
					groundIdleCalibrationFactorSFC,
					takeOffCalibrationFactorEmissionIndexNOx,
					takeOffCalibrationFactorEmissionIndexCO,
					takeOffCalibrationFactorEmissionIndexHC,
					takeOffCalibrationFactorEmissionIndexSoot,
					takeOffCalibrationFactorEmissionIndexCO2,
					takeOffCalibrationFactorEmissionIndexSOx,
					takeOffCalibrationFactorEmissionIndexH2O,
					aprCalibrationFactorEmissionIndexNOx,
					aprCalibrationFactorEmissionIndexCO,
					aprCalibrationFactorEmissionIndexHC,
					aprCalibrationFactorEmissionIndexSoot,
					aprCalibrationFactorEmissionIndexCO2,
					aprCalibrationFactorEmissionIndexSOx,
					aprCalibrationFactorEmissionIndexH2O,
					groundIdleCalibrationFactorEmissionIndexNOx,
					groundIdleCalibrationFactorEmissionIndexCO,
					groundIdleCalibrationFactorEmissionIndexHC,
					groundIdleCalibrationFactorEmissionIndexSoot,
					groundIdleCalibrationFactorEmissionIndexCO2,
					groundIdleCalibrationFactorEmissionIndexSOx,
					groundIdleCalibrationFactorEmissionIndexH2O
					);

			theTakeOffCalculator.calculateTakeOffDistanceODE(null, false, false, vMC);
			
			rangeTakeOff.addAll(theTakeOffCalculator.getGroundDistance());			
			altitudeTakeOff.addAll(theTakeOffCalculator.getVerticalDistance());
			timeTakeOff.addAll(theTakeOffCalculator.getTime());
			fuelUsedTakeOff.addAll(theTakeOffCalculator.getFuelUsed());
			aircraftMassTakeOff.addAll(
					theTakeOffCalculator.getWeight().stream()
					.map(w -> Amount.valueOf(
							w.doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.KILOGRAM)
							)
					.collect(Collectors.toList())
					);
			emissionNOxTakeOff.addAll(theTakeOffCalculator.getEmissionsNOx());
			emissionCOTakeOff.addAll(theTakeOffCalculator.getEmissionsCO());
			emissionHCTakeOff.addAll(theTakeOffCalculator.getEmissionsHC());
			emissionSootTakeOff.addAll(theTakeOffCalculator.getEmissionsSoot());
			emissionCO2TakeOff.addAll(theTakeOffCalculator.getEmissionsCO2());
			emissionSOxTakeOff.addAll(theTakeOffCalculator.getEmissionsSOx());
			emissionH2OTakeOff.addAll(theTakeOffCalculator.getEmissionsH2O());
			speedTASTakeOff.addAll(theTakeOffCalculator.getSpeedTAS());
			speedCASTakeOff.addAll(theTakeOffCalculator.getSpeedCAS());
			machTakeOff.addAll(theTakeOffCalculator.getMach());
			cLTakeOff.addAll(theTakeOffCalculator.getcL());
			cDTakeOff.addAll(theTakeOffCalculator.getcD());
			dragTakeOff.addAll(theTakeOffCalculator.getDrag());
			totalThrustTakeOff.addAll(theTakeOffCalculator.getThrust());
			fuelFlowTakeOff.addAll(theTakeOffCalculator.getFuelFlow());
			rateOfClimbTakeOff.addAll(theTakeOffCalculator.getRateOfClimb());
			climbAngleTakeOff.addAll(theTakeOffCalculator.getGamma());
			
			for(int iTakeOff=0; iTakeOff<timeTakeOff.size(); iTakeOff++) {
				
				/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
				thermicThrustTakeOff.add(totalThrustTakeOff.get(iTakeOff));
				electricThrustTakeOff.add(Amount.valueOf(0.0, SI.NEWTON));
				sfcTakeOff.add(
						(fuelFlowTakeOff.get(iTakeOff)/totalThrustTakeOff.get(iTakeOff).doubleValue(SI.NEWTON))
						/(0.224809)
						/(0.454/3600)
						);
				efficiencyTakeOff.add(cLTakeOff.get(iTakeOff)/cDTakeOff.get(iTakeOff));		
				fuelPowerTakeOff.add(
						Amount.valueOf(
								thermicThrustTakeOff.get(iTakeOff).doubleValue(SI.NEWTON)
								* speedTASTakeOff.get(iTakeOff).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				batteryPowerTakeOff.add(
						Amount.valueOf(
								electricThrustTakeOff.get(iTakeOff).doubleValue(SI.NEWTON)
								* speedTASTakeOff.get(iTakeOff).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				fuelEnergyTakeOff.add(
						Amount.valueOf(
								fuelPowerTakeOff.get(iTakeOff).doubleValue(SI.WATT)
								* timeTakeOff.get(iTakeOff).doubleValue(SI.SECOND),
								SI.JOULE
								)
						);
				batteryEnergyTakeOff.add(Amount.valueOf(0.0, SI.JOULE));
			}
			
			//--------------------------------------------------------------------
			// CLIMB 
			Amount<Mass> initialMassClimb = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);
			
			theClimbCalculator = new ClimbCalc(
					theAircraft,
					theOperatingConditions,
					cLmaxClean, 
					polarCLClimb,
					polarCDClimb,
					climbSpeed,
					dragDueToEnigneFailure,
					climbCalibrationFactorThrust,
					continuousCalibrationFactorThrust,
					climbCalibrationFactorSFC,
					climbCalibrationFactorEmissionIndexNOx,
					climbCalibrationFactorEmissionIndexCO,
					climbCalibrationFactorEmissionIndexHC,
					climbCalibrationFactorEmissionIndexSoot,
					climbCalibrationFactorEmissionIndexCO2,
					climbCalibrationFactorEmissionIndexSOx,
					climbCalibrationFactorEmissionIndexH2O
					);
			theClimbCalculator.setNumberOfStepClimb(numberOfStepClimb); 
			
			theClimbCalculator.calculateClimbPerformance(
					initialMassClimb,
					initialMassClimb,
					obstacleTakeOff.to(SI.METER),
					theOperatingConditions.getAltitudeCruise().to(SI.METER),
					false,
					false
					);

			rangeClimb.addAll(theClimbCalculator.getRangeClimb());			
			altitudeClimb.addAll(theClimbCalculator.getAltitudeClimb());
			timeClimb.addAll(theClimbCalculator.getTimeClimb());
			fuelUsedClimb.addAll(theClimbCalculator.getFuelUsedClimb());
			aircraftMassClimb.addAll(theClimbCalculator.getAircraftMassClimb());
			emissionNOxClimb.addAll(theClimbCalculator.getEmissionNOxClimb());
			emissionCOClimb.addAll(theClimbCalculator.getEmissionCOClimb());
			emissionHCClimb.addAll(theClimbCalculator.getEmissionHCClimb());
			emissionSootClimb.addAll(theClimbCalculator.getEmissionSootClimb());
			emissionCO2Climb.addAll(theClimbCalculator.getEmissionCO2Climb());
			emissionSOxClimb.addAll(theClimbCalculator.getEmissionSOxClimb());
			emissionH2OClimb.addAll(theClimbCalculator.getEmissionH2OClimb());
			speedTASClimb.addAll(theClimbCalculator.getSpeedTASClimb());
			speedCASClimb.addAll(theClimbCalculator.getSpeedCASClimb());
			machClimb.addAll(theClimbCalculator.getMachClimb());
			cLClimb.addAll(theClimbCalculator.getCLClimb());
			cDClimb.addAll(theClimbCalculator.getCDClimb());
			efficiencyClimb.addAll(theClimbCalculator.getEfficiencyClimb());
			dragClimb.addAll(theClimbCalculator.getDragClimb());
			totalThrustClimb.addAll(theClimbCalculator.getTotalThrustClimb());
			fuelFlowClimb.addAll(theClimbCalculator.getFuelFlowClimb());
			sfcClimb.addAll(theClimbCalculator.getSfcClimb());
			rateOfClimbClimb.addAll(theClimbCalculator.getRateOfClimbClimb());
			climbAngleClimb.addAll(theClimbCalculator.getClimbAngleClimb());
			
			for(int iClimb=0; iClimb<timeClimb.size(); iClimb++) {
				/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
				thermicThrustClimb.add(totalThrustClimb.get(iClimb));
				electricThrustClimb.add(Amount.valueOf(0.0, SI.NEWTON));
				fuelPowerClimb.add(
						Amount.valueOf(
								thermicThrustClimb.get(iClimb).doubleValue(SI.NEWTON)
								* speedTASClimb.get(iClimb).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				batteryPowerClimb.add(
						Amount.valueOf(
								electricThrustClimb.get(iClimb).doubleValue(SI.NEWTON)
								* speedTASClimb.get(iClimb).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				fuelEnergyClimb.add(
						Amount.valueOf(
								fuelPowerClimb.get(iClimb).doubleValue(SI.WATT)
								* timeClimb.get(iClimb).doubleValue(SI.SECOND),
								SI.JOULE
								)
						);
				batteryEnergyClimb.add(Amount.valueOf(0.0, SI.JOULE));
			}
			
			//--------------------------------------------------------------------
			// CRUISE (CONSTANT MACH AND ALTITUDE)
			Amount<Mass> initialMassCruise = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
					- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			currentCruiseRange = missionRange;
			totalRange = Amount.valueOf(0.0, SI.METER);

			for (int iCruise=0; iCruise < 5; iCruise++) {
				
				initializePhasesLists(true, true, false);
				
				List<Amount<Length>> cruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
						MyArrayUtils.linspace(
								0.0,
								currentCruiseRange.doubleValue(SI.METER),
								numberOfStepCruise
								),
						SI.METER
						);

				Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(theAircraft.getWing());

				int nPointSpeed = 1000;
				List<Amount<Velocity>> speedArray = 
						MyArrayUtils.convertDoubleArrayToListOfAmount(
								MyArrayUtils.linspace(
										SpeedCalc.calculateSpeedStall(
												theOperatingConditions.getAltitudeCruise(),
												theOperatingConditions.getDeltaTemperatureCruise(),
												initialMassCruise,
												theAircraft.getWing().getSurfacePlanform(),
												MyArrayUtils.getMax(polarCLClimb)
												).doubleValue(SI.METERS_PER_SECOND),
										SpeedCalc.calculateTAS(
												1.0,
												theOperatingConditions.getAltitudeCruise(),
												theOperatingConditions.getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND),
										nPointSpeed
										),
								SI.METERS_PER_SECOND
								);

				List<DragMap> dragList = new ArrayList<>();
				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								initialMassCruise,
								speedArray,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								polarCLCruise,
								polarCDCruise,
								theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				List<ThrustMap> thrustList = new ArrayList<>();
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								theOperatingConditions.getThrottleCruise(),
								initialMassCruise,
								speedArray, 
								EngineOperatingConditionEnum.CRUISE, 
								theAircraft.getPowerPlant(),
								false, 
								cruiseCalibrationFactorThrust
								)
						);

				List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								speedArray,
								initialMassCruise,
								theOperatingConditions.getThrottleCruise(), 
								EngineOperatingConditionEnum.CRUISE, 
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								dragList,
								thrustList
								)
						);

				if(intersectionList.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
					return;
				}

				List<Double> cruiseMissionMachNumber = new ArrayList<>();
				List<Amount<Velocity>> cruiseSpeedList = new ArrayList<>();
				List<Amount<Velocity>> cruiseSpeedCASList = new ArrayList<>();
				double sigma = OperatingConditions.getAtmosphere(
						theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
						).getDensityRatio();
				if(theOperatingConditions.getMachCruise() <= intersectionList.get(0).getMaxMach()) {
					cruiseMissionMachNumber.add(theOperatingConditions.getMachCruise());
					cruiseSpeedList.add(
							SpeedCalc.calculateTAS(
									theOperatingConditions.getMachCruise(),
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise()
									).to(NonSI.KNOT)
							);
					
					cruiseSpeedCASList.add(cruiseSpeedList.get(0).times(Math.sqrt(sigma)));
				}
				else {
					cruiseMissionMachNumber.add(intersectionList.get(0).getMaxMach());
					cruiseSpeedList.add(intersectionList.get(0).getMaxSpeed().to(NonSI.KNOT));
					cruiseSpeedCASList.add(cruiseSpeedList.get(0).times(Math.sqrt(sigma)));
					if(cruiseMaxMachNumberErrorFlag == false) {
						System.err.println("WARNING: (CRUISE - MISSION PROFILE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						cruiseMaxMachNumberErrorFlag = true;
					}
				}

				List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
				aircraftMassPerStep.add(initialMassCruise);

				List<Double> cLSteps = new ArrayList<>();
				cLSteps.add(
						LiftCalc.calculateLiftCoeff(
								Amount.valueOf(
										aircraftMassPerStep.get(0)
										.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
										.doubleValue(SI.KILOGRAM),
										SI.NEWTON
										),
								cruiseSpeedList.get(0),
								theAircraft.getWing().getSurfacePlanform(),
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise()
								)
						);
				List<Double> cDSteps = new ArrayList<>();
				cDSteps.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								polarCLCruise,
								polarCDCruise,
								cLSteps.get(0)
								)
						+ DragCalc.calculateCDWaveLockKorn(
								cLSteps.get(0), 
								cruiseMissionMachNumber.get(0), 
								AerodynamicCalc.calculateMachCriticalKornMason(
										cLSteps.get(0), 
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(), 
										meanAirfoil.getType()
										)
								)
//								(IRON LOOP2) + (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
//								+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
//								-0.003767654434394
//								)
						);

				List<Amount<Force>> dragPerStep = new ArrayList<>();
				dragPerStep.add(
						DragCalc.calculateDragAtSpeed(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								theAircraft.getWing().getSurfacePlanform(),
								cruiseSpeedList.get(0),
								cDSteps.get(0)
								)
						);

				List<Amount<Force>> thrustFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					thrustFormDatabaseList.add(
							ThrustCalc.calculateThrustDatabase(
									theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
									EngineOperatingConditionEnum.CRUISE,
									theOperatingConditions.getAltitudeCruise(),
									cruiseMissionMachNumber.get(0),
									theOperatingConditions.getDeltaTemperatureCruise(), 
									theOperatingConditions.getThrottleCruise(), 
									cruiseCalibrationFactorThrust
									)
							);
				}
				
				List<Double> phi = new ArrayList<>();
				try {
				phi.add(dragPerStep.get(0).doubleValue(SI.NEWTON)
						/ thrustFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						);
				} catch (ArithmeticException e) {
					System.err.println("WARNING: (CRUISE - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
					return;
				}

				if(phi.get(0) > 1.0) {
					phi.remove(0);
					phi.add(0, 1.0);
				}
				
				List<Double> sfcFormDatabaseList = new ArrayList<>();
				List<Double> fuelFlowFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					sfcFormDatabaseList.add(
							theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
									cruiseMissionMachNumber.get(0),
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise(),
									phi.get(0),
									EngineOperatingConditionEnum.CRUISE,
									cruiseCalibrationFactorSFC
									)
							);
					fuelFlowFormDatabaseList.add(
							thrustFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*sfcFormDatabaseList.get(iEng)
							);
				}
				
				List<Double> sfcList = new ArrayList<>();
				sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
				
				List<Double> fuelFlows = new ArrayList<>();
				fuelFlows.add(fuelFlowFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				List<Amount<Duration>> times = new ArrayList<>();
				times.add(Amount.valueOf(0.0, SI.SECOND));

				List<Amount<Mass>> fuelUsedPerStep = new ArrayList<>();
				fuelUsedPerStep.add(Amount.valueOf(0.0, SI.KILOGRAM));

				List<Amount<Mass>> emissionNOxPerStep = new ArrayList<>();
				emissionNOxPerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionCOPerStep = new ArrayList<>();
				emissionCOPerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionHCPerStep = new ArrayList<>();
				emissionHCPerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionSootPerStep = new ArrayList<>();
				emissionSootPerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionCO2PerStep = new ArrayList<>();
				emissionCO2PerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionSOxPerStep = new ArrayList<>();
				emissionSOxPerStep.add(Amount.valueOf(0.0, SI.GRAM));
				
				List<Amount<Mass>> emissionH2OPerStep = new ArrayList<>();
				emissionH2OPerStep.add(Amount.valueOf(0.0, SI.GRAM));				
				
				for (int j=1; j<cruiseSteps.size(); j++) {

					times.add(
							times.get(times.size()-1)
							.plus(
									Amount.valueOf(
											(cruiseSteps.get(j).doubleValue(SI.METER)
													- cruiseSteps.get(j-1).doubleValue(SI.METER))
											/ cruiseSpeedList.get(j-1).doubleValue(SI.METERS_PER_SECOND),
											SI.SECOND
											).to(NonSI.MINUTE)
									)
							);
					
					fuelUsedPerStep.add(
							fuelUsedPerStep.get(fuelUsedPerStep.size()-1)
							.plus(
									Amount.valueOf(
											fuelFlows.get(j-1)
											* (times.get(j).doubleValue(NonSI.MINUTE) - times.get(j-1).doubleValue(NonSI.MINUTE)),
											SI.KILOGRAM
											)
									)
							);
					
					List<Double> emissionIndexNOxList = new ArrayList<>();
					List<Double> emissionIndexCOList = new ArrayList<>();
					List<Double> emissionIndexHCList = new ArrayList<>();
					List<Double> emissionIndexSootList = new ArrayList<>();
					List<Double> emissionIndexCO2List = new ArrayList<>();
					List<Double> emissionIndexSOxList = new ArrayList<>();
					List<Double> emissionIndexH2OList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						emissionIndexNOxList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getNOxEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexNOx
										)
								);
						emissionIndexCOList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCOEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexCO
										)
								);
						emissionIndexHCList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getHCEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexHC
										)
								);
						emissionIndexSootList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSootEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexSoot
										)
								);
						emissionIndexCO2List.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCO2EmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexCO2
										)
								);
						emissionIndexSOxList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSOxEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexSOx
										)
								);
						emissionIndexH2OList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getH2OEmissionIndex(
										cruiseMissionMachNumber.get(j-1),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j-1),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorEmissionIndexH2O
										)
								);
					}
					emissionNOxPerStep.add(
							emissionNOxPerStep.get(emissionNOxPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexNOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionCOPerStep.add(
							emissionCOPerStep.get(emissionCOPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexCOList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionHCPerStep.add(
							emissionHCPerStep.get(emissionHCPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexHCList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionSootPerStep.add(
							emissionSootPerStep.get(emissionSootPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexSootList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionCO2PerStep.add(
							emissionCO2PerStep.get(emissionCO2PerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexCO2List.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionSOxPerStep.add(
							emissionSOxPerStep.get(emissionSOxPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexSOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionH2OPerStep.add(
							emissionH2OPerStep.get(emissionH2OPerStep.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexH2OList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					
					aircraftMassPerStep.add(
							aircraftMassPerStep.get(j-1).to(SI.KILOGRAM)
							.minus(
									Amount.valueOf(
											fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM),
											SI.KILOGRAM
											)
									)
							);

					dragList.add(
							DragCalc.calculateDragAndPowerRequired(
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise(),
									aircraftMassPerStep.get(j),
									speedArray,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									polarCLCruise,
									polarCDCruise,
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					thrustList.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									theOperatingConditions.getAltitudeCruise(), 
									theOperatingConditions.getDeltaTemperatureCruise(),
									theOperatingConditions.getThrottleCruise(),
									aircraftMassPerStep.get(j), 
									speedArray, 
									EngineOperatingConditionEnum.CRUISE, 
									theAircraft.getPowerPlant(), 
									false, 
									cruiseCalibrationFactorThrust
									)
							);

					intersectionList.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise(), 
									speedArray,
									aircraftMassPerStep.get(j),
									theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									dragList,
									thrustList
									)
							);

					if(intersectionList.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					if(theOperatingConditions.getMachCruise() <= intersectionList.get(j).getMaxMach()) {
						cruiseMissionMachNumber.add(theOperatingConditions.getMachCruise());
						cruiseSpeedList.add(
								SpeedCalc.calculateTAS(
										theOperatingConditions.getMachCruise(),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise()
										).to(NonSI.KNOT)
								);
						cruiseSpeedCASList.add(cruiseSpeedList.get(j).times(Math.sqrt(sigma)));
					}
					else {
						cruiseMissionMachNumber.add(intersectionList.get(j).getMaxMach());
						cruiseSpeedList.add(intersectionList.get(j).getMaxSpeed().to(NonSI.KNOT));
						cruiseSpeedCASList.add(cruiseSpeedList.get(j).times(Math.sqrt(sigma)));
						if(cruiseMaxMachNumberErrorFlag == false) {
							System.err.println("WARNING: (CRUISE - MISSION PROFILE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							cruiseMaxMachNumberErrorFlag = true;
						}
					}

					cLSteps.add(
							LiftCalc.calculateLiftCoeff(
									Amount.valueOf(
											aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											SI.NEWTON
											), 
									cruiseSpeedList.get(j),
									theAircraft.getWing().getSurfacePlanform(),
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise()
									)
							);
					
					cDSteps.add(
							MyMathUtils.getInterpolatedValue1DLinear(
									polarCLCruise,
									polarCDCruise,
									cLSteps.get(j))
							+ DragCalc.calculateCDWaveLockKorn(
									cLSteps.get(j), 
									cruiseMissionMachNumber.get(j), 
									AerodynamicCalc.calculateMachCriticalKornMason(
											cLSteps.get(j), 
											theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
											meanAirfoil.getThicknessToChordRatio(), 
											meanAirfoil.getType()
											)
									)
//									+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
//											+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
//											-0.003767654434394
//											)
							);

					dragPerStep.add(
							DragCalc.calculateDragAtSpeed(
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise(), 
									theAircraft.getWing().getSurfacePlanform(), 
									cruiseSpeedList.get(j),
									cDSteps.get(j)
									)
							);

					thrustFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						thrustFormDatabaseList.add(
								ThrustCalc.calculateThrustDatabase(
										theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
										theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
										EngineOperatingConditionEnum.CRUISE,
										theOperatingConditions.getAltitudeCruise(),
										cruiseMissionMachNumber.get(j),
										theOperatingConditions.getDeltaTemperatureCruise(), 
										theOperatingConditions.getThrottleCruise(), 
										cruiseCalibrationFactorThrust
										)
								);
					}
					
					try {
						phi.add(dragPerStep.get(j).doubleValue(SI.NEWTON)
								/ thrustFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								);
						} catch (ArithmeticException e) {
							System.err.println("WARNING: (CRUISE - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
							return;
						}

					if(phi.get(j) > 1.0) {
						phi.remove(j);
						phi.add(j, 1.0);
					}

					sfcFormDatabaseList = new ArrayList<>();
					fuelFlowFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						sfcFormDatabaseList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
										cruiseMissionMachNumber.get(j),
										theOperatingConditions.getAltitudeCruise(),
										theOperatingConditions.getDeltaTemperatureCruise(),
										phi.get(j),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorSFC
										)
								);
						fuelFlowFormDatabaseList.add(
								thrustFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*sfcFormDatabaseList.get(iEng)
								);
					}
					
					sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
					
					fuelFlows.add(fuelFlowFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				}

				rangeCruise.addAll(cruiseSteps);			
				timeCruise.addAll(times);
				fuelUsedCruise.addAll(fuelUsedPerStep);
				aircraftMassCruise.addAll(aircraftMassPerStep);
				emissionNOxCruise.addAll(emissionNOxPerStep);
				emissionCOCruise.addAll(emissionCOPerStep);
				emissionHCCruise.addAll(emissionHCPerStep);
				emissionSootCruise.addAll(emissionSootPerStep);
				emissionCO2Cruise.addAll(emissionCO2PerStep);
				emissionSOxCruise.addAll(emissionSOxPerStep);
				emissionH2OCruise.addAll(emissionH2OPerStep);
				speedTASCruise.addAll(cruiseSpeedList);
				speedCASCruise.addAll(cruiseSpeedCASList);
				machCruise.addAll(cruiseMissionMachNumber);
				cLCruise.addAll(cLSteps);
				cDCruise.addAll(cDSteps);
				dragCruise.addAll(dragPerStep);
				totalThrustCruise.addAll(dragPerStep);
				sfcCruise.addAll(sfcList);
				fuelFlowCruise.addAll(fuelFlows);
				
				for(int iCr=0; iCr<timeCruise.size(); iCr++) {
					/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
					thermicThrustCruise.add(totalThrustCruise.get(iCr));
					electricThrustCruise.add(Amount.valueOf(0.0, SI.NEWTON));
					altitudeCruise.add(theOperatingConditions.getAltitudeCruise());
					climbAngleCruise.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
					rateOfClimbCruise.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
					efficiencyCruise.add(cLCruise.get(iCr)/cDCruise.get(iCr));
					fuelPowerCruise.add(
							Amount.valueOf(
									thermicThrustCruise.get(iCr).doubleValue(SI.NEWTON)
									* speedTASCruise.get(iCr).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerCruise.add(
							Amount.valueOf(
									electricThrustCruise.get(iCr).doubleValue(SI.NEWTON)
									* speedTASCruise.get(iCr).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					fuelEnergyCruise.add(
							Amount.valueOf(
									fuelPowerCruise.get(iCr).doubleValue(SI.WATT)
									* timeCruise.get(iCr).doubleValue(SI.SECOND),
									SI.JOULE
									)
							);
					batteryEnergyCruise.add(Amount.valueOf(0.0, SI.JOULE));
				}
				
				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				Amount<Mass> initialMassDescent = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);

				theFirstDescentCalculator = new DescentCalc(
						theAircraft,
						theOperatingConditions,
						speedDescentCAS,
						rateOfDescent,
						theOperatingConditions.getAltitudeCruise().to(SI.METER),
						holdingAltitude.to(SI.METER),
						initialMassDescent,
						polarCLClimb,
						polarCDClimb,
						cruiseCalibrationFactorThrust,
						cruiseCalibrationFactorSFC,
						flightIdleCalibrationFactorThrust,
						flightIdleCalibrationFactorSFC,
						cruiseCalibrationFactorEmissionIndexNOx,
						cruiseCalibrationFactorEmissionIndexCO,
						cruiseCalibrationFactorEmissionIndexHC,
						cruiseCalibrationFactorEmissionIndexSoot,
						cruiseCalibrationFactorEmissionIndexCO2,
						cruiseCalibrationFactorEmissionIndexSOx,
						cruiseCalibrationFactorEmissionIndexH2O,
						flightIdleCalibrationFactorEmissionIndexNOx,
						flightIdleCalibrationFactorEmissionIndexCO,
						flightIdleCalibrationFactorEmissionIndexHC,
						flightIdleCalibrationFactorEmissionIndexSoot,
						flightIdleCalibrationFactorEmissionIndexCO2,
						flightIdleCalibrationFactorEmissionIndexSOx,
						flightIdleCalibrationFactorEmissionIndexH2O
						);
				theFirstDescentCalculator.setNumberOfStepDescent(numberOfStepDescent);
				
				theFirstDescentCalculator.calculateDescentPerformance();
				
				rangeFirstDescent.addAll(theFirstDescentCalculator.getDescentLengths());			
				altitudeFirstDescent.addAll(theFirstDescentCalculator.getDescentAltitudes());
				timeFirstDescent.addAll(theFirstDescentCalculator.getDescentTimes());
				fuelUsedFirstDescent.addAll(theFirstDescentCalculator.getFuelUsedPerStep());
				aircraftMassFirstDescent.addAll(theFirstDescentCalculator.getAircraftMassPerStep());
				emissionNOxFirstDescent.addAll(theFirstDescentCalculator.getEmissionNOxPerStep());
				emissionCOFirstDescent.addAll(theFirstDescentCalculator.getEmissionCOPerStep());
				emissionHCFirstDescent.addAll(theFirstDescentCalculator.getEmissionHCPerStep());
				emissionSootFirstDescent.addAll(theFirstDescentCalculator.getEmissionSootPerStep());
				emissionCO2FirstDescent.addAll(theFirstDescentCalculator.getEmissionCO2PerStep());
				emissionSOxFirstDescent.addAll(theFirstDescentCalculator.getEmissionSOxPerStep());
				emissionH2OFirstDescent.addAll(theFirstDescentCalculator.getEmissionH2OPerStep());
				speedTASFirstDescent.addAll(theFirstDescentCalculator.getSpeedListTAS());
				speedCASFirstDescent.addAll(theFirstDescentCalculator.getSpeedListCAS());
				machFirstDescent.addAll(theFirstDescentCalculator.getMachList());
				cLFirstDescent.addAll(theFirstDescentCalculator.getCLSteps());
				cDFirstDescent.addAll(theFirstDescentCalculator.getCDSteps());
				efficiencyFirstDescent.addAll(theFirstDescentCalculator.getEfficiencyPerStep());
				dragFirstDescent.addAll(theFirstDescentCalculator.getDragPerStep());
				totalThrustFirstDescent.addAll(theFirstDescentCalculator.getThrustPerStep());
				fuelFlowFirstDescent.addAll(theFirstDescentCalculator.getInterpolatedFuelFlowList());
				sfcFirstDescent.addAll(theFirstDescentCalculator.getInterpolatedSFCList());
				rateOfClimbFirstDescent.addAll(theFirstDescentCalculator.getRateOfDescentList());
				climbAngleFirstDescent.addAll(theFirstDescentCalculator.getDescentAngles());
				
				for(int iFirstDescent=0; iFirstDescent<timeFirstDescent.size(); iFirstDescent++) {
					/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
					thermicThrustFirstDescent.add(totalThrustFirstDescent.get(iFirstDescent));
					electricThrustFirstDescent.add(Amount.valueOf(0.0, SI.NEWTON));
					fuelPowerFirstDescent.add(
							Amount.valueOf(
									thermicThrustFirstDescent.get(iFirstDescent).doubleValue(SI.NEWTON)
									* speedTASFirstDescent.get(iFirstDescent).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerFirstDescent.add(
							Amount.valueOf(
									electricThrustFirstDescent.get(iFirstDescent).doubleValue(SI.NEWTON)
									* speedTASFirstDescent.get(iFirstDescent).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					fuelEnergyFirstDescent.add(
							Amount.valueOf(
									fuelPowerFirstDescent.get(iFirstDescent).doubleValue(SI.WATT)
									* timeFirstDescent.get(iFirstDescent).doubleValue(SI.SECOND),
									SI.JOULE
									)
							);
					batteryEnergyFirstDescent.add(Amount.valueOf(0.0, SI.JOULE));
				}
				
				//--------------------------------------------------------------------
				// SECOND CLIMB (up to ALTERNATE altitude)
				Amount<Mass> initialMassSecondClimb = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
						- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);
				
				theSecondClimbCalculator = new ClimbCalc(
						theAircraft,
						theOperatingConditions,
						cLmaxClean, 
						polarCLClimb,
						polarCDClimb,
						climbSpeed,
						dragDueToEnigneFailure,
						climbCalibrationFactorThrust,
						continuousCalibrationFactorThrust,
						climbCalibrationFactorSFC,
						climbCalibrationFactorEmissionIndexNOx,
						climbCalibrationFactorEmissionIndexCO,
						climbCalibrationFactorEmissionIndexHC,
						climbCalibrationFactorEmissionIndexSoot,
						climbCalibrationFactorEmissionIndexCO2,
						climbCalibrationFactorEmissionIndexSOx,
						climbCalibrationFactorEmissionIndexH2O
						);
				theSecondClimbCalculator.setNumberOfStepClimb(numberOfStepClimb); 
				
				theSecondClimbCalculator.calculateClimbPerformance(
						initialMassSecondClimb,
						initialMassSecondClimb,
						holdingAltitude.to(SI.METER),
						alternateCruiseAltitude.to(SI.METER),
						false,
						false
						);

				rangeSecondClimb.addAll(theSecondClimbCalculator.getRangeClimb());			
				altitudeSecondClimb.addAll(theSecondClimbCalculator.getAltitudeClimb());
				timeSecondClimb.addAll(theSecondClimbCalculator.getTimeClimb());
				fuelUsedSecondClimb.addAll(theSecondClimbCalculator.getFuelUsedClimb());
				aircraftMassSecondClimb.addAll(theSecondClimbCalculator.getAircraftMassClimb());
				emissionNOxSecondClimb.addAll(theSecondClimbCalculator.getEmissionNOxClimb());
				emissionCOSecondClimb.addAll(theSecondClimbCalculator.getEmissionCOClimb());
				emissionHCSecondClimb.addAll(theSecondClimbCalculator.getEmissionHCClimb());
				emissionSootSecondClimb.addAll(theSecondClimbCalculator.getEmissionSootClimb());
				emissionCO2SecondClimb.addAll(theSecondClimbCalculator.getEmissionCO2Climb());
				emissionSOxSecondClimb.addAll(theSecondClimbCalculator.getEmissionSOxClimb());
				emissionH2OSecondClimb.addAll(theSecondClimbCalculator.getEmissionH2OClimb());
				speedTASSecondClimb.addAll(theSecondClimbCalculator.getSpeedTASClimb());
				speedCASSecondClimb.addAll(theSecondClimbCalculator.getSpeedCASClimb());
				machSecondClimb.addAll(theSecondClimbCalculator.getMachClimb());
				cLSecondClimb.addAll(theSecondClimbCalculator.getCLClimb());
				cDSecondClimb.addAll(theSecondClimbCalculator.getCDClimb());
				efficiencySecondClimb.addAll(theSecondClimbCalculator.getEfficiencyClimb());
				dragSecondClimb.addAll(theSecondClimbCalculator.getDragClimb());
				totalThrustSecondClimb.addAll(theSecondClimbCalculator.getTotalThrustClimb());
				fuelFlowSecondClimb.addAll(theSecondClimbCalculator.getFuelFlowClimb());
				sfcSecondClimb.addAll(theSecondClimbCalculator.getSfcClimb());
				rateOfClimbSecondClimb.addAll(theSecondClimbCalculator.getRateOfClimbClimb());
				climbAngleSecondClimb.addAll(theSecondClimbCalculator.getClimbAngleClimb());
				
				for(int iSecondClimb=0; iSecondClimb<timeSecondClimb.size(); iSecondClimb++) {
					/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
					thermicThrustSecondClimb.add(totalThrustSecondClimb.get(iSecondClimb));
					electricThrustSecondClimb.add(Amount.valueOf(0.0, SI.NEWTON));
					fuelPowerSecondClimb.add(
							Amount.valueOf(
									thermicThrustSecondClimb.get(iSecondClimb).doubleValue(SI.NEWTON)
									* speedTASSecondClimb.get(iSecondClimb).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerSecondClimb.add(
							Amount.valueOf(
									electricThrustSecondClimb.get(iSecondClimb).doubleValue(SI.NEWTON)
									* speedTASSecondClimb.get(iSecondClimb).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					fuelEnergySecondClimb.add(
							Amount.valueOf(
									fuelPowerSecondClimb.get(iSecondClimb).doubleValue(SI.WATT)
									* timeSecondClimb.get(iSecondClimb).doubleValue(SI.SECOND),
									SI.JOULE
									)
							);
					batteryEnergySecondClimb.add(Amount.valueOf(0.0, SI.JOULE));
				}
				
				//--------------------------------------------------------------------
				// ALTERNATE CRUISE (AT MAX EFFICIENCY)
				Amount<Mass> initialMassAlternateCruise = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
						- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
						- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);

				currentAlternateCruiseRange = alternateCruiseRange;

				for (int iAlternate=0; iAlternate < 5; iAlternate++) {
					
					initializePhasesLists(true ,false, true);
					
					List<Amount<Length>> alternateCruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
							MyArrayUtils.linspace(
									0.0,
									currentAlternateCruiseRange.doubleValue(SI.METER),
									numberOfStepAlternateCruise
									),
							SI.METER
							);
					
					List<Amount<Velocity>> speedArrayAlternate = 
							MyArrayUtils.convertDoubleArrayToListOfAmount(
									MyArrayUtils.linspace(
											SpeedCalc.calculateSpeedStall(
													alternateCruiseAltitude,
													theOperatingConditions.getDeltaTemperatureCruise(),
													initialMassAlternateCruise,
													theAircraft.getWing().getSurfacePlanform(),
													MyArrayUtils.getMax(polarCLClimb)
													).doubleValue(SI.METERS_PER_SECOND),
											SpeedCalc.calculateTAS(
													1.0,
													alternateCruiseAltitude,
													theOperatingConditions.getDeltaTemperatureCruise()
													).doubleValue(SI.METERS_PER_SECOND),
											nPointSpeed
											),
									SI.METERS_PER_SECOND
									);

					List<DragMap> dragListAlternate = new ArrayList<>();
					dragListAlternate.add(
							DragCalc.calculateDragAndPowerRequired(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									initialMassAlternateCruise,
									speedArrayAlternate,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									polarCLCruise,
									polarCDCruise,
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListAlternate = new ArrayList<>();
					thrustListAlternate.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									theOperatingConditions.getThrottleCruise(),
									initialMassAlternateCruise,
									speedArrayAlternate,
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getPowerPlant(),
									false,
									cruiseCalibrationFactorThrust
									)
							);

					List<DragThrustIntersectionMap> intersectionListAlternate = new ArrayList<>();
					intersectionListAlternate.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									speedArrayAlternate,
									initialMassAlternateCruise,
									theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									dragListAlternate,
									thrustListAlternate
									)
							);

					if(intersectionListAlternate.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Double> rangeFactorAlternateCruiseList = new ArrayList<>();
					double[] cLRangeAlternateCruiseArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(polarCLCruise),
							50
							); 
					
					for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {
						if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
								|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
							rangeFactorAlternateCruiseList.add(
									Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLCruise,
											polarCDCruise,
											cLRangeAlternateCruiseArray[iCL]
											)
									);
						else {
							rangeFactorAlternateCruiseList.add(
									cLRangeAlternateCruiseArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													polarCLCruise,
													polarCDCruise,
													cLRangeAlternateCruiseArray[iCL]
													)
									);
						}
					}

					int iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
					double bestMachAlternateCruise = SpeedCalc.calculateMach(
							alternateCruiseAltitude,
							theOperatingConditions.getDeltaTemperatureCruise(),
							SpeedCalc.calculateSpeedAtCL(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									initialMassAlternateCruise,
									theAircraft.getWing().getSurfacePlanform(), 
									cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
									)
							);

					List<Double> alternateCruiseMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> alternateCruiseSpeedList = new ArrayList<>();
					List<Amount<Velocity>> alternateCruiseSpeedCASList = new ArrayList<>();
					double sigmaAlternateCruise = OperatingConditions.getAtmosphere(
							alternateCruiseAltitude.doubleValue(SI.METER),
							theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
							).getDensityRatio();
					if(bestMachAlternateCruise <= intersectionListAlternate.get(0).getMaxMach()) {
						alternateCruiseMachNumberList.add(bestMachAlternateCruise);
						alternateCruiseSpeedList.add(
								SpeedCalc.calculateTAS(
										bestMachAlternateCruise,
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise()
										).to(NonSI.KNOT)
								);
						alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(0).times(Math.sqrt(sigmaAlternateCruise)));
					}
					else {
						alternateCruiseMachNumberList.add(intersectionListAlternate.get(0).getMaxMach());
						alternateCruiseSpeedList.add(intersectionListAlternate.get(0).getMaxSpeed().to(NonSI.KNOT));
						alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(0).times(Math.sqrt(sigmaAlternateCruise)));
						if(alternateCruiseBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							alternateCruiseBestMachNumberErrorFlag = true;
						}
					}

					List<Amount<Mass>> aircraftMassPerStepAlternateCruise = new ArrayList<>();
					aircraftMassPerStepAlternateCruise.add(initialMassAlternateCruise);

					List<Double> cLStepsAlternateCruise = new ArrayList<>();
					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									Amount.valueOf(
											aircraftMassPerStepAlternateCruise.get(0)
											.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
											.doubleValue(SI.KILOGRAM),
											SI.NEWTON
											),
									alternateCruiseSpeedList.get(0),
									theAircraft.getWing().getSurfacePlanform(),
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise()
									)
							);

					List<Double> cDStepsAlternateCruise = new ArrayList<>();
					cDStepsAlternateCruise.add(
							MyMathUtils.getInterpolatedValue1DLinear(
									polarCLCruise,
									polarCDCruise,
									cLStepsAlternateCruise.get(0)
									)
							+ DragCalc.calculateCDWaveLockKorn(
									cLStepsAlternateCruise.get(0), 
									alternateCruiseMachNumberList.get(0), 
									AerodynamicCalc.calculateMachCriticalKornMason(
											cLStepsAlternateCruise.get(0), 
											theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
											meanAirfoil.getThicknessToChordRatio(), 
											meanAirfoil.getType()
											)
									)
//									(IRON LOOP2) + (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
//									+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
//									-0.003767654434394
//									)
							);
					
					List<Amount<Force>> dragPerStepAlternateCruise = new ArrayList<>();
					dragPerStepAlternateCruise.add(
							DragCalc.calculateDragAtSpeed(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									theAircraft.getWing().getSurfacePlanform(),
									alternateCruiseSpeedList.get(0),
									cDStepsAlternateCruise.get(0)
									)
							);

					List<Amount<Force>> thrustAlternateCruiseFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						thrustAlternateCruiseFormDatabaseList.add(
								ThrustCalc.calculateThrustDatabase(
										theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
										theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
										EngineOperatingConditionEnum.CRUISE,
										alternateCruiseAltitude,
										alternateCruiseMachNumberList.get(0),
										theOperatingConditions.getDeltaTemperatureCruise(), 
										theOperatingConditions.getThrottleCruise(), 
										cruiseCalibrationFactorThrust
										)
								);
					}
					
					List<Double> phiAlternateCruise = new ArrayList<>();
					try {
						phiAlternateCruise.add(dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
								/ thrustAlternateCruiseFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								);
						} catch (ArithmeticException e) {
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
							return;
						}

					if(phiAlternateCruise.get(0) > 1.0) {
						phiAlternateCruise.remove(0);
						phiAlternateCruise.add(0, 1.0);
					}
					
					List<Double> sfcAlternateCruiseFormDatabaseList = new ArrayList<>();
					List<Double> fuelFlowAlternateCruiseFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						sfcAlternateCruiseFormDatabaseList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
										alternateCruiseMachNumberList.get(0),
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										phiAlternateCruise.get(0),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorSFC
										)
								);
						fuelFlowAlternateCruiseFormDatabaseList.add(
								thrustAlternateCruiseFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*sfcAlternateCruiseFormDatabaseList.get(iEng)
								);
					}
					
					List<Double> sfcAlternateCruiseList = new ArrayList<>();
					sfcAlternateCruiseList.add(sfcAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
					
					List<Double> fuelFlowsAlternateCruise = new ArrayList<>();
					fuelFlowsAlternateCruise.add(fuelFlowAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());
					
					List<Amount<Duration>> timesAlternateCruise = new ArrayList<>();
					timesAlternateCruise.add(Amount.valueOf(0.0, SI.SECOND));

					List<Amount<Mass>> fuelUsedPerStepAlternateCruise = new ArrayList<>();
					fuelUsedPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.KILOGRAM));

					List<Amount<Mass>> emissionNOxPerStepAlternateCruise = new ArrayList<>();
					emissionNOxPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionCOPerStepAlternateCruise = new ArrayList<>();
					emissionCOPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionHCPerStepAlternateCruise = new ArrayList<>();
					emissionHCPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionSootPerStepAlternateCruise = new ArrayList<>();
					emissionSootPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionCO2PerStepAlternateCruise = new ArrayList<>();
					emissionCO2PerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionSOxPerStepAlternateCruise = new ArrayList<>();
					emissionSOxPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionH2OPerStepAlternateCruise = new ArrayList<>();
					emissionH2OPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.GRAM));		

					for (int j=1; j<alternateCruiseSteps.size(); j++) {

						timesAlternateCruise.add(
								timesAlternateCruise.get(timesAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												(alternateCruiseSteps.get(j).doubleValue(SI.METER)
														- alternateCruiseSteps.get(j-1).doubleValue(SI.METER))
												/ alternateCruiseSpeedList.get(j-1).doubleValue(SI.METERS_PER_SECOND),
												SI.SECOND
												).to(NonSI.MINUTE)
										)
								);
						
						fuelUsedPerStepAlternateCruise.add(
								fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												fuelFlowsAlternateCruise.get(j-1)
												* (timesAlternateCruise.get(j).doubleValue(NonSI.MINUTE) - timesAlternateCruise.get(j-1).doubleValue(NonSI.MINUTE)),
												SI.KILOGRAM
												)
										)
								);
						
						List<Double> emissionIndexNOxListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexCOListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexHCListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexSootListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexCO2ListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexSOxListAlternateCruise = new ArrayList<>();
						List<Double> emissionIndexH2OListAlternateCruise = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							emissionIndexNOxListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getNOxEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexNOx
											)
									);
							emissionIndexCOListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCOEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexCO
											)
									);
							emissionIndexHCListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getHCEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexHC
											)
									);
							emissionIndexSootListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSootEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexSoot
											)
									);
							emissionIndexCO2ListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCO2EmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexCO2
											)
									);
							emissionIndexSOxListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSOxEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexSOx
											)
									);
							emissionIndexH2OListAlternateCruise.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getH2OEmissionIndex(
											alternateCruiseMachNumberList.get(j-1),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexH2O
											)
									);
						}
						emissionNOxPerStepAlternateCruise.add(
								emissionNOxPerStepAlternateCruise.get(emissionNOxPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexNOxListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCOPerStepAlternateCruise.add(
								emissionCOPerStepAlternateCruise.get(emissionCOPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCOListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);		
						emissionHCPerStepAlternateCruise.add(
								emissionHCPerStepAlternateCruise.get(emissionHCPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexHCListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSootPerStepAlternateCruise.add(
								emissionSootPerStepAlternateCruise.get(emissionSootPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSootListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCO2PerStepAlternateCruise.add(
								emissionCO2PerStepAlternateCruise.get(emissionCO2PerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCO2ListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSOxPerStepAlternateCruise.add(
								emissionSOxPerStepAlternateCruise.get(emissionSOxPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSOxListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionH2OPerStepAlternateCruise.add(
								emissionH2OPerStepAlternateCruise.get(emissionH2OPerStepAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexH2OListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						
						aircraftMassPerStepAlternateCruise.add(
								aircraftMassPerStepAlternateCruise.get(j-1)
								.minus(
										Amount.valueOf(
												fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM),
												SI.KILOGRAM
												)
										)
								);

						dragListAlternate.add(
								DragCalc.calculateDragAndPowerRequired(
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										aircraftMassPerStepAlternateCruise.get(j),
										speedArrayAlternate,
										theAircraft.getWing().getSurfacePlanform(),
										MyArrayUtils.getMax(polarCLClimb),
										polarCLCruise,
										polarCDCruise,
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListAlternate.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										theOperatingConditions.getThrottleCruise(),
										aircraftMassPerStepAlternateCruise.get(j),
										speedArrayAlternate,
										EngineOperatingConditionEnum.CRUISE,
										theAircraft.getPowerPlant(),
										false,
										cruiseCalibrationFactorThrust
										)
								);

						intersectionListAlternate.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										speedArrayAlternate,
										aircraftMassPerStepAlternateCruise.get(j),
										theOperatingConditions.getThrottleCruise(),
										EngineOperatingConditionEnum.CRUISE,
										theAircraft.getWing().getSurfacePlanform(),
										MyArrayUtils.getMax(polarCLClimb),
										dragListAlternate,
										thrustListAlternate
										)
								);

						if(intersectionListAlternate.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
							missionProfileStopped = Boolean.TRUE;
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) ALTERNATE CRUISE MACH NUMBER = 0.0. RETURNING ... ");
							return;
						}

						rangeFactorAlternateCruiseList = new ArrayList<>();
						for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {
							if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
									|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
								rangeFactorAlternateCruiseList.add(
										Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLCruise,
												polarCDCruise,
												cLRangeAlternateCruiseArray[iCL]
												)
										);
							else 
								rangeFactorAlternateCruiseList.add(
										cLRangeAlternateCruiseArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														polarCLCruise,
														polarCDCruise,
														cLRangeAlternateCruiseArray[iCL]
														)
										);
						}

						iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
						bestMachAlternateCruise = SpeedCalc.calculateMach(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise(),
								SpeedCalc.calculateSpeedAtCL(
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										aircraftMassPerStepAlternateCruise.get(j),
										theAircraft.getWing().getSurfacePlanform(), 
										cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
										)
								);

						if(bestMachAlternateCruise <= intersectionListAlternate.get(j).getMaxMach()) {
							alternateCruiseMachNumberList.add(bestMachAlternateCruise);
							alternateCruiseSpeedList.add(
									SpeedCalc.calculateTAS(
											bestMachAlternateCruise,
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise()
											).to(NonSI.KNOT)
									);
							alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(j).times(Math.sqrt(sigmaAlternateCruise)));
						}
						else {
							alternateCruiseMachNumberList.add(intersectionListAlternate.get(j).getMaxMach());
							alternateCruiseSpeedList.add(intersectionListAlternate.get(j).getMaxSpeed().to(NonSI.KNOT));
							alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(j).times(Math.sqrt(sigmaAlternateCruise)));
							if(alternateCruiseBestMachNumberErrorFlag == false) {
								System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
								alternateCruiseBestMachNumberErrorFlag = true;
							}
						}

						cLStepsAlternateCruise.add(
								LiftCalc.calculateLiftCoeff(
										Amount.valueOf(
												aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												SI.NEWTON
												),
										alternateCruiseSpeedList.get(j),
										theAircraft.getWing().getSurfacePlanform(),
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise()
										)
								);
						
						cDStepsAlternateCruise.add(
								MyMathUtils.getInterpolatedValue1DLinear(
										polarCLCruise,
										polarCDCruise,
										cLStepsAlternateCruise.get(j))
								+ DragCalc.calculateCDWaveLockKorn(
										cLStepsAlternateCruise.get(j), 
										alternateCruiseMachNumberList.get(j), 
										AerodynamicCalc.calculateMachCriticalKornMason(
												cLStepsAlternateCruise.get(j), 
												theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
												meanAirfoil.getThicknessToChordRatio(), 
												meanAirfoil.getType()
												)
										)
//										+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
//												+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
//												-0.003767654434394
//												)
								);
						
						dragPerStepAlternateCruise.add(
								DragCalc.calculateDragAtSpeed(
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(), 
										theAircraft.getWing().getSurfacePlanform(), 
										alternateCruiseSpeedList.get(j),
										cDStepsAlternateCruise.get(j)
										)
								);

						thrustAlternateCruiseFormDatabaseList = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							thrustAlternateCruiseFormDatabaseList.add(
									ThrustCalc.calculateThrustDatabase(
											theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
											theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
											EngineOperatingConditionEnum.CRUISE,
											alternateCruiseAltitude,
											alternateCruiseMachNumberList.get(j),
											theOperatingConditions.getDeltaTemperatureCruise(), 
											theOperatingConditions.getThrottleCruise(), 
											cruiseCalibrationFactorThrust
											)
									);
						}
						
						try {
							phiAlternateCruise.add(dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
									/ thrustAlternateCruiseFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									);
							} catch (ArithmeticException e) {
								System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
								return;
							}

						if(phiAlternateCruise.get(j) > 1.0) {
							phiAlternateCruise.remove(j);
							phiAlternateCruise.add(j, 1.0);
						}
						
						sfcAlternateCruiseFormDatabaseList = new ArrayList<>();
						fuelFlowAlternateCruiseFormDatabaseList = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							sfcAlternateCruiseFormDatabaseList.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
											alternateCruiseMachNumberList.get(j),
											alternateCruiseAltitude,
											theOperatingConditions.getDeltaTemperatureCruise(),
											phiAlternateCruise.get(j),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorSFC
											)
									);
							fuelFlowAlternateCruiseFormDatabaseList.add(
									thrustAlternateCruiseFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*sfcAlternateCruiseFormDatabaseList.get(iEng)
									);
						}
						
						sfcAlternateCruiseList.add(sfcAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
						
						fuelFlowsAlternateCruise.add(fuelFlowAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());
						
					}

					rangeAlternateCruise.addAll(alternateCruiseSteps);			
					timeAlternateCruise.addAll(timesAlternateCruise);
					fuelUsedAlternateCruise.addAll(fuelUsedPerStepAlternateCruise);
					aircraftMassAlternateCruise.addAll(aircraftMassPerStepAlternateCruise);
					emissionNOxAlternateCruise.addAll(emissionNOxPerStepAlternateCruise);
					emissionCOAlternateCruise.addAll(emissionCOPerStepAlternateCruise);
					emissionHCAlternateCruise.addAll(emissionHCPerStepAlternateCruise);
					emissionSootAlternateCruise.addAll(emissionSootPerStepAlternateCruise);
					emissionCO2AlternateCruise.addAll(emissionCO2PerStepAlternateCruise);
					emissionSOxAlternateCruise.addAll(emissionSOxPerStepAlternateCruise);
					emissionH2OAlternateCruise.addAll(emissionH2OPerStepAlternateCruise);
					speedTASAlternateCruise.addAll(alternateCruiseSpeedList);
					speedCASAlternateCruise.addAll(alternateCruiseSpeedCASList);
					machAlternateCruise.addAll(alternateCruiseMachNumberList);
					cLAlternateCruise.addAll(cLStepsAlternateCruise);
					cDAlternateCruise.addAll(cDStepsAlternateCruise);
					dragAlternateCruise.addAll(dragPerStepAlternateCruise);
					totalThrustAlternateCruise.addAll(dragPerStepAlternateCruise);
					sfcAlternateCruise.addAll(sfcAlternateCruiseList);
					fuelFlowAlternateCruise.addAll(fuelFlowsAlternateCruise);
					
					for(int iAltCr=0; iAltCr<timeAlternateCruise.size(); iAltCr++) {
						/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
						thermicThrustAlternateCruise.add(totalThrustAlternateCruise.get(iAltCr));
						electricThrustAlternateCruise.add(Amount.valueOf(0.0, SI.NEWTON));
						altitudeAlternateCruise.add(alternateCruiseAltitude);
						climbAngleAlternateCruise.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
						rateOfClimbAlternateCruise.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
						efficiencyAlternateCruise.add(cLAlternateCruise.get(iAltCr)/cDAlternateCruise.get(iAltCr));
						fuelPowerAlternateCruise.add(
								Amount.valueOf(
										thermicThrustAlternateCruise.get(iAltCr).doubleValue(SI.NEWTON)
										* speedTASAlternateCruise.get(iAltCr).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerAlternateCruise.add(
								Amount.valueOf(
										electricThrustAlternateCruise.get(iAltCr).doubleValue(SI.NEWTON)
										* speedTASAlternateCruise.get(iAltCr).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						fuelEnergyAlternateCruise.add(
								Amount.valueOf(
										fuelPowerAlternateCruise.get(iAltCr).doubleValue(SI.WATT)
										* timeAlternateCruise.get(iAltCr).doubleValue(SI.SECOND),
										SI.JOULE
										)
								);
						batteryEnergyAlternateCruise.add(Amount.valueOf(0.0, SI.JOULE));
					}
					
					//--------------------------------------------------------------------
					// DESCENT (up to HOLDING altitude)
					Amount<Mass> initialMassSecondDescent = Amount.valueOf(
							initialMissionMass.doubleValue(SI.KILOGRAM)
							- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
							- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
							- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
							- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM),
							SI.KILOGRAM
							);

					theSecondDescentCalculator = new DescentCalc(
							theAircraft,
							theOperatingConditions,
							speedDescentCAS,
							rateOfDescent,
							alternateCruiseAltitude.to(SI.METER),
							holdingAltitude.to(SI.METER),
							initialMassSecondDescent,
							polarCLClimb,
							polarCDClimb,
							cruiseCalibrationFactorThrust,
							cruiseCalibrationFactorSFC,
							flightIdleCalibrationFactorThrust,
							flightIdleCalibrationFactorSFC,
							cruiseCalibrationFactorEmissionIndexNOx,
							cruiseCalibrationFactorEmissionIndexCO,
							cruiseCalibrationFactorEmissionIndexHC,
							cruiseCalibrationFactorEmissionIndexSoot,
							cruiseCalibrationFactorEmissionIndexCO2,
							cruiseCalibrationFactorEmissionIndexSOx,
							cruiseCalibrationFactorEmissionIndexH2O,
							flightIdleCalibrationFactorEmissionIndexNOx,
							flightIdleCalibrationFactorEmissionIndexCO,
							flightIdleCalibrationFactorEmissionIndexHC,
							flightIdleCalibrationFactorEmissionIndexSoot,
							flightIdleCalibrationFactorEmissionIndexCO2,
							flightIdleCalibrationFactorEmissionIndexSOx,
							flightIdleCalibrationFactorEmissionIndexH2O
							);
					theSecondDescentCalculator.setNumberOfStepDescent(numberOfStepDescent);
					
					theSecondDescentCalculator.calculateDescentPerformance();

					rangeSecondDescent.addAll(theSecondDescentCalculator.getDescentLengths());			
					altitudeSecondDescent.addAll(theSecondDescentCalculator.getDescentAltitudes());
					timeSecondDescent.addAll(theSecondDescentCalculator.getDescentTimes());
					fuelUsedSecondDescent.addAll(theSecondDescentCalculator.getFuelUsedPerStep());
					aircraftMassSecondDescent.addAll(theSecondDescentCalculator.getAircraftMassPerStep());
					emissionNOxSecondDescent.addAll(theSecondDescentCalculator.getEmissionNOxPerStep());
					emissionCOSecondDescent.addAll(theSecondDescentCalculator.getEmissionCOPerStep());
					emissionHCSecondDescent.addAll(theSecondDescentCalculator.getEmissionHCPerStep());
					emissionSootSecondDescent.addAll(theSecondDescentCalculator.getEmissionSootPerStep());
					emissionCO2SecondDescent.addAll(theSecondDescentCalculator.getEmissionCO2PerStep());
					emissionSOxSecondDescent.addAll(theSecondDescentCalculator.getEmissionSOxPerStep());
					emissionH2OSecondDescent.addAll(theSecondDescentCalculator.getEmissionH2OPerStep());
					speedTASSecondDescent.addAll(theSecondDescentCalculator.getSpeedListTAS());
					speedCASSecondDescent.addAll(theSecondDescentCalculator.getSpeedListCAS());
					machSecondDescent.addAll(theSecondDescentCalculator.getMachList());
					cLSecondDescent.addAll(theSecondDescentCalculator.getCLSteps());
					cDSecondDescent.addAll(theSecondDescentCalculator.getCDSteps());
					efficiencySecondDescent.addAll(theSecondDescentCalculator.getEfficiencyPerStep());
					dragSecondDescent.addAll(theSecondDescentCalculator.getDragPerStep());
					totalThrustSecondDescent.addAll(theSecondDescentCalculator.getThrustPerStep());
					fuelFlowSecondDescent.addAll(theSecondDescentCalculator.getInterpolatedFuelFlowList());
					sfcSecondDescent.addAll(theSecondDescentCalculator.getInterpolatedSFCList());
					rateOfClimbSecondDescent.addAll(theSecondDescentCalculator.getRateOfDescentList());
					climbAngleSecondDescent.addAll(theSecondDescentCalculator.getDescentAngles());
					
					for(int iSecondDescent=0; iSecondDescent<timeSecondDescent.size(); iSecondDescent++) {
						/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
						thermicThrustSecondDescent.add(totalThrustSecondDescent.get(iSecondDescent));
						electricThrustSecondDescent.add(Amount.valueOf(0.0, SI.NEWTON));
						fuelPowerSecondDescent.add(
								Amount.valueOf(
										thermicThrustSecondDescent.get(iSecondDescent).doubleValue(SI.NEWTON)
										* speedTASSecondDescent.get(iSecondDescent).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerSecondDescent.add(
								Amount.valueOf(
										electricThrustSecondDescent.get(iSecondDescent).doubleValue(SI.NEWTON)
										* speedTASSecondDescent.get(iSecondDescent).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						fuelEnergySecondDescent.add(
								Amount.valueOf(
										fuelPowerSecondDescent.get(iSecondDescent).doubleValue(SI.WATT)
										* timeSecondDescent.get(iSecondDescent).doubleValue(SI.SECOND),
										SI.JOULE
										)
								);
						batteryEnergySecondDescent.add(Amount.valueOf(0.0, SI.JOULE));
					}

					//--------------------------------------------------------------------
					// HOLDING (BEST ENDURANCE) - CLIMB DELTA TEMPERATURE
					Amount<Mass> initialMassHolding = Amount.valueOf(
							initialMissionMass.doubleValue(SI.KILOGRAM)
							- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
							- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
							- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
							- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
							- theSecondDescentCalculator.getFuelUsedPerStep().get(theSecondDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM),
							SI.KILOGRAM
							);

					List<Amount<Duration>> timeHoldingArray = MyArrayUtils.convertDoubleArrayToListOfAmount( 
							MyArrayUtils.linspace(
									0.0,
									holdingDuration.doubleValue(NonSI.MINUTE),
									numberOfStepHolding
									),
							NonSI.MINUTE
							);

					List<Amount<Velocity>> speedArrayHolding = MyArrayUtils.convertDoubleArrayToListOfAmount( 
							MyArrayUtils.linspace(
									SpeedCalc.calculateSpeedStall(
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											initialMassHolding,
											theAircraft.getWing().getSurfacePlanform(),
											MyArrayUtils.getMax(polarCLClimb)
											).doubleValue(SI.METERS_PER_SECOND),
									SpeedCalc.calculateTAS(
											1.0,
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb()
											).doubleValue(SI.METERS_PER_SECOND),
									nPointSpeed
									),
							SI.METERS_PER_SECOND
							);

					List<DragMap> dragListHolding = new ArrayList<>();
					dragListHolding.add(
							DragCalc.calculateDragAndPowerRequired(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									initialMassHolding,
									speedArrayHolding,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									polarCLClimb,
									polarCDClimb,
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListHolding = new ArrayList<>();
					thrustListHolding.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									theOperatingConditions.getThrottleCruise(),
									initialMassHolding,
									speedArrayHolding,
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getPowerPlant(),
									false,
									cruiseCalibrationFactorThrust
									)
							);

					List<DragThrustIntersectionMap> intersectionListHolding = new ArrayList<>();
					intersectionListHolding.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									speedArrayHolding,
									initialMassHolding,
									theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									dragListHolding,
									thrustListHolding
									)
							);

					if(intersectionListHolding.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (HOLDING - MISSION PROFILE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Double> enduranceFactorHoldingList = new ArrayList<>();
					double[] cLEnduranceHoldingArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(polarCLClimb),
							50
							); 
					for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {
						if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
								|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
							enduranceFactorHoldingList.add(
									cLEnduranceHoldingArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													polarCLClimb,
													polarCDClimb,
													cLEnduranceHoldingArray[iCL]
													)
									);
						else 
							enduranceFactorHoldingList.add(
									Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLClimb,
											polarCDClimb,
											cLEnduranceHoldingArray[iCL]
											)
									);
					}

					int iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
					double bestMachHolding = SpeedCalc.calculateMach(
							holdingAltitude,
							theOperatingConditions.getDeltaTemperatureClimb(),
							SpeedCalc.calculateSpeedAtCL(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									initialMassHolding,
									theAircraft.getWing().getSurfacePlanform(), 
									cLEnduranceHoldingArray[iBestSpeedHolding]
									)
							);

					List<Double> holdingMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> holdingSpeedList = new ArrayList<>();
					List<Amount<Velocity>> holdingSpeedCASList = new ArrayList<>();
					double sigmaHolding = OperatingConditions.getAtmosphere(
							holdingAltitude.doubleValue(SI.METER),
							theOperatingConditions.getDeltaTemperatureClimb().doubleValue(SI.CELSIUS)
							).getDensityRatio();
					if(bestMachHolding <= intersectionListHolding.get(0).getMaxMach()) {
						holdingMachNumberList.add(bestMachHolding);
						holdingSpeedList.add(
								SpeedCalc.calculateTAS(
										bestMachHolding,
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb()
										).to(NonSI.KNOT)
								);
						holdingSpeedCASList.add(holdingSpeedList.get(0).times(Math.sqrt(sigmaHolding)));
					}
					else {
						holdingMachNumberList.add(intersectionListHolding.get(0).getMaxMach());
						holdingSpeedList.add(intersectionListHolding.get(0).getMaxSpeed().to(NonSI.KNOT));
						holdingSpeedCASList.add(holdingSpeedList.get(0).times(Math.sqrt(sigmaHolding)));
						if(holdingBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							holdingBestMachNumberErrorFlag = true;
						}
					}

					List<Amount<Mass>> aircraftMassPerStepHolding = new ArrayList<>();
					aircraftMassPerStepHolding.add(initialMassHolding);
					
					List<Double> cLStepsHolding = new ArrayList<>();
					cLStepsHolding.add(
							LiftCalc.calculateLiftCoeff(
									Amount.valueOf(
											aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											SI.NEWTON
											),
									holdingSpeedList.get(0),
									theAircraft.getWing().getSurfacePlanform(),
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb()
									)
							);

					List<Double> cDStepsHolding = new ArrayList<>();
					cDStepsHolding.add(
							MyMathUtils.getInterpolatedValue1DLinear(
									polarCLClimb,
									polarCDClimb,
									cLStepsHolding.get(0)
									)
							+ DragCalc.calculateCDWaveLockKorn(
									cLStepsHolding.get(0), 
									holdingMachNumberList.get(0), 
									AerodynamicCalc.calculateMachCriticalKornMason(
											cLStepsHolding.get(0), 
											theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
											meanAirfoil.getThicknessToChordRatio(), 
											meanAirfoil.getType()
											)
									)
							);
					
					List<Amount<Force>> dragPerStepHolding = new ArrayList<>();
					dragPerStepHolding.add(
							DragCalc.calculateDragAtSpeed(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									theAircraft.getWing().getSurfacePlanform(),
									holdingSpeedList.get(0),
									cDStepsHolding.get(0)
									)
							);

					List<Amount<Force>> thrustHoldingFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						thrustHoldingFormDatabaseList.add(
								ThrustCalc.calculateThrustDatabase(
										theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
										theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
										EngineOperatingConditionEnum.CRUISE,
										holdingAltitude,
										holdingMachNumberList.get(0),
										theOperatingConditions.getDeltaTemperatureClimb(), 
										theOperatingConditions.getThrottleCruise(), 
										cruiseCalibrationFactorThrust
										)
								);
					}
					
					List<Double> phiHolding = new ArrayList<>();
					try {
						phiHolding.add(dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
								/ thrustHoldingFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								);
						} catch (ArithmeticException e) {
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
							return;
						}

					if(phiHolding.get(0) > 1.0) {
						phiHolding.remove(0);
						phiHolding.add(0, 1.0);
					}
					
					List<Double> sfcHoldingFormDatabaseList = new ArrayList<>();
					List<Double> fuelFlowHoldingFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						sfcHoldingFormDatabaseList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
										holdingMachNumberList.get(0),
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										phiHolding.get(0),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorSFC
										)
								);
						fuelFlowHoldingFormDatabaseList.add(
								thrustHoldingFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*sfcHoldingFormDatabaseList.get(iEng)
								);
					}
					
					List<Double> sfcHoldingList = new ArrayList<>();
					sfcHoldingList.add(sfcHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
					
					List<Double> fuelFlowsHolding = new ArrayList<>();
					fuelFlowsHolding.add(fuelFlowHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

					List<Amount<Mass>> fuelUsedPerStepHolding = new ArrayList<>();
					fuelUsedPerStepHolding.add(Amount.valueOf(0.0, SI.KILOGRAM));

					List<Amount<Mass>> emissionNOxPerStepHolding = new ArrayList<>();
					emissionNOxPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionCOPerStepHolding = new ArrayList<>();
					emissionCOPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionHCPerStepHolding = new ArrayList<>();
					emissionHCPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionSootPerStepHolding = new ArrayList<>();
					emissionSootPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionCO2PerStepHolding = new ArrayList<>();
					emissionCO2PerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionSOxPerStepHolding = new ArrayList<>();
					emissionSOxPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));
					
					List<Amount<Mass>> emissionH2OPerStepHolding = new ArrayList<>();
					emissionH2OPerStepHolding.add(Amount.valueOf(0.0, SI.GRAM));	
					
					for(int j=1; j<timeHoldingArray.size(); j++) {

						fuelUsedPerStepHolding.add(
								fuelUsedPerStepHolding.get(fuelUsedPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												fuelFlowsHolding.get(j-1)
												* (timeHoldingArray.get(j).doubleValue(NonSI.MINUTE) - timeHoldingArray.get(j-1).doubleValue(NonSI.MINUTE)),
												SI.KILOGRAM
												)
										)
								);
						
						List<Double> emissionIndexNOxListHolding = new ArrayList<>();
						List<Double> emissionIndexCOListHolding = new ArrayList<>();
						List<Double> emissionIndexHCListHolding = new ArrayList<>();
						List<Double> emissionIndexSootListHolding = new ArrayList<>();
						List<Double> emissionIndexCO2ListHolding = new ArrayList<>();
						List<Double> emissionIndexSOxListHolding = new ArrayList<>();
						List<Double> emissionIndexH2OListHolding = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							emissionIndexNOxListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getNOxEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexNOx
											)
									);
							emissionIndexCOListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCOEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexCO
											)
									);
							emissionIndexHCListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getHCEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexHC
											)
									);
							emissionIndexSootListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSootEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexSoot
											)
									);
							emissionIndexCO2ListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getCO2EmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexCO2
											)
									);
							emissionIndexSOxListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSOxEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexSOx
											)
									);
							emissionIndexH2OListHolding.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getH2OEmissionIndex(
											holdingMachNumberList.get(j-1),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j-1),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorEmissionIndexH2O
											)
									);
						}
						emissionNOxPerStepHolding.add(
								emissionNOxPerStepHolding.get(emissionNOxPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexNOxListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCOPerStepHolding.add(
								emissionCOPerStepHolding.get(emissionCOPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCOListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionHCPerStepHolding.add(
								emissionHCPerStepHolding.get(emissionHCPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexHCListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSootPerStepHolding.add(
								emissionSootPerStepHolding.get(emissionSootPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSootListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCO2PerStepHolding.add(
								emissionCO2PerStepHolding.get(emissionCO2PerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCO2ListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSOxPerStepHolding.add(
								emissionSOxPerStepHolding.get(emissionSOxPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSOxListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionH2OPerStepHolding.add(
								emissionH2OPerStepHolding.get(emissionH2OPerStepHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexH2OListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						
						aircraftMassPerStepHolding.add(
								aircraftMassPerStepHolding.get(j-1)
								.minus(
										Amount.valueOf(
												fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM),
												SI.KILOGRAM
												)
										)
								);

						dragListHolding.add(
								DragCalc.calculateDragAndPowerRequired(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										aircraftMassPerStepHolding.get(j),
										speedArrayHolding,
										theAircraft.getWing().getSurfacePlanform(),
										MyArrayUtils.getMax(polarCLClimb),
										polarCLClimb,
										polarCDClimb,
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListHolding.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										theOperatingConditions.getThrottleCruise(),
										aircraftMassPerStepHolding.get(j),
										speedArrayHolding,
										EngineOperatingConditionEnum.CRUISE,
										theAircraft.getPowerPlant(),
										false,
										cruiseCalibrationFactorThrust
										)
								);

						intersectionListHolding.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										speedArrayHolding,
										aircraftMassPerStepHolding.get(j),
										theOperatingConditions.getThrottleCruise(),
										EngineOperatingConditionEnum.CRUISE,
										theAircraft.getWing().getSurfacePlanform(),
										MyArrayUtils.getMax(polarCLClimb),
										dragListHolding,
										thrustListHolding
										)
								);

						if(intersectionListHolding.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
							missionProfileStopped = Boolean.TRUE;
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
							return;
						}

						enduranceFactorHoldingList = new ArrayList<>();
						for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {
							if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
									|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
								enduranceFactorHoldingList.add(
										cLEnduranceHoldingArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														polarCLClimb,
														polarCDClimb,
														cLEnduranceHoldingArray[iCL]
														)
										);
							else 
								enduranceFactorHoldingList.add(
										Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLClimb,
												polarCDClimb,
												cLEnduranceHoldingArray[iCL]
												)
										);
						}

						iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
						bestMachHolding = SpeedCalc.calculateMach(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								SpeedCalc.calculateSpeedAtCL(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										aircraftMassPerStepHolding.get(0),
										theAircraft.getWing().getSurfacePlanform(), 
										cLEnduranceHoldingArray[iBestSpeedHolding]
										)
								);

						if(bestMachHolding <= intersectionListHolding.get(j).getMaxMach()) {
							holdingMachNumberList.add(bestMachHolding);
							holdingSpeedList.add(
									SpeedCalc.calculateTAS(
											bestMachHolding,
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb()
											).to(NonSI.KNOT)
									);
							holdingSpeedCASList.add(holdingSpeedList.get(j).times(Math.sqrt(sigmaHolding)));
						}
						else {
							holdingMachNumberList.add(intersectionListHolding.get(j).getMaxMach());
							holdingSpeedList.add(intersectionListHolding.get(j).getMaxSpeed().to(NonSI.KNOT));
							holdingSpeedCASList.add(holdingSpeedList.get(j).times(Math.sqrt(sigmaHolding)));
							if(holdingBestMachNumberErrorFlag == false) {
								System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
								holdingBestMachNumberErrorFlag = true;
							}
							
						}

						cLStepsHolding.add(
								LiftCalc.calculateLiftCoeff(
										Amount.valueOf(
												aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												SI.NEWTON
												),
										holdingSpeedList.get(j),
										theAircraft.getWing().getSurfacePlanform(),
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb()
										)
								);
						
						cDStepsHolding.add(
								MyMathUtils.getInterpolatedValue1DLinear(
										polarCLClimb,
										polarCDClimb,
										cLStepsHolding.get(j))
								+ DragCalc.calculateCDWaveLockKorn(
										cLStepsHolding.get(j), 
										holdingMachNumberList.get(j), 
										AerodynamicCalc.calculateMachCriticalKornMason(
												cLStepsHolding.get(j), 
												theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
												meanAirfoil.getThicknessToChordRatio(), 
												meanAirfoil.getType()
												)
										)
								);
						
						dragPerStepHolding.add(
								DragCalc.calculateDragAtSpeed(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(), 
										theAircraft.getWing().getSurfacePlanform(), 
										holdingSpeedList.get(j),
										cDStepsHolding.get(j)
										)
								);

						thrustHoldingFormDatabaseList = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							thrustHoldingFormDatabaseList.add(
									ThrustCalc.calculateThrustDatabase(
											theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
											theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
											EngineOperatingConditionEnum.CRUISE,
											holdingAltitude,
											holdingMachNumberList.get(j),
											theOperatingConditions.getDeltaTemperatureClimb(), 
											theOperatingConditions.getThrottleCruise(), 
											cruiseCalibrationFactorThrust
											)
									);
						}
						
						try {
							phiHolding.add(dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
									/ thrustHoldingFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									);
							} catch (ArithmeticException e) {
								System.err.println("WARNING: (HOLDING - MISSION PROFILE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
								return;
							}

						if(phiHolding.get(j) > 1.0) {
							phiHolding.remove(j);
							phiHolding.add(j, 1.0);
						}

						sfcHoldingFormDatabaseList = new ArrayList<>();
						fuelFlowHoldingFormDatabaseList = new ArrayList<>();
						for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
							sfcHoldingFormDatabaseList.add(
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
											holdingMachNumberList.get(j),
											holdingAltitude,
											theOperatingConditions.getDeltaTemperatureClimb(),
											phiHolding.get(j),
											EngineOperatingConditionEnum.CRUISE,
											cruiseCalibrationFactorSFC
											)
									);
							fuelFlowHoldingFormDatabaseList.add(
									thrustHoldingFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*sfcHoldingFormDatabaseList.get(iEng)
									);
						}
						
						sfcHoldingList.add(sfcHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());
						
						fuelFlowsHolding.add(fuelFlowHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());
						
					}

					timeHolding.addAll(timeHoldingArray);
					fuelUsedHolding.addAll(fuelUsedPerStepHolding);
					aircraftMassHolding.addAll(aircraftMassPerStepHolding);
					emissionNOxHolding.addAll(emissionNOxPerStepHolding);
					emissionCOHolding.addAll(emissionCOPerStepHolding);
					emissionHCHolding.addAll(emissionHCPerStepHolding);
					emissionSootHolding.addAll(emissionSootPerStepHolding);
					emissionCO2Holding.addAll(emissionCO2PerStepHolding);
					emissionSOxHolding.addAll(emissionSOxPerStepHolding);
					emissionH2OHolding.addAll(emissionH2OPerStepHolding);
					speedTASHolding.addAll(alternateCruiseSpeedList);
					speedCASHolding.addAll(alternateCruiseSpeedCASList);
					machHolding.addAll(alternateCruiseMachNumberList);
					cLHolding.addAll(cLStepsHolding);
					cDHolding.addAll(cDStepsHolding);
					dragHolding.addAll(dragPerStepHolding);
					totalThrustHolding.addAll(dragPerStepHolding);
					sfcHolding.addAll(sfcHoldingList);
					fuelFlowHolding.addAll(fuelFlowsHolding);
					
					for(int iHold=0; iHold<timeHolding.size(); iHold++) {
						/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
						thermicThrustHolding.add(totalThrustHolding.get(iHold));
						electricThrustHolding.add(Amount.valueOf(0.0, SI.NEWTON));
						rangeHolding.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
						altitudeHolding.add(holdingAltitude);
						climbAngleHolding.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
						rateOfClimbHolding.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
						efficiencyHolding.add(cLHolding.get(iHold)/cDHolding.get(iHold));
						fuelPowerHolding.add(
								Amount.valueOf(
										thermicThrustHolding.get(iHold).doubleValue(SI.NEWTON)
										* speedTASHolding.get(iHold).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerHolding.add(
								Amount.valueOf(
										electricThrustHolding.get(iHold).doubleValue(SI.NEWTON)
										* speedTASHolding.get(iHold).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						fuelEnergyHolding.add(
								Amount.valueOf(
										fuelPowerHolding.get(iHold).doubleValue(SI.WATT)
										* timeHolding.get(iHold).doubleValue(SI.SECOND),
										SI.JOULE
										)
								);
						batteryEnergyHolding.add(Amount.valueOf(0.0, SI.JOULE));
					}

					//--------------------------------------------------------------------
					// LANDING 
					Amount<Mass> intialMassLanding = Amount.valueOf(
							initialMissionMass.doubleValue(SI.KILOGRAM)
							- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
							- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
							- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
							- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
							- theSecondDescentCalculator.getFuelUsedPerStep().get(theSecondDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
							- fuelUsedPerStepHolding.get(fuelUsedPerStepHolding.size()-1).doubleValue(SI.KILOGRAM),
							SI.KILOGRAM
							);

					// only if iAlternate=0 and iCruise=0 i=0 (just one time to reduce computational time)
					if(iAlternate == 0 && iCruise == 0 && i==0) {
						theLandingCalculator = new LandingCalc(
								holdingAltitude,
								theOperatingConditions.getAltitudeLanding(), 
								theOperatingConditions.getDeltaTemperatureLanding(), 
								approachAngle, 
								intialMassLanding,
								theAircraft.getPowerPlant(),
								polarCLLanding,
								polarCDLanding, 
								theAircraft.getWing().getAspectRatio(), 
								theAircraft.getWing().getSurfacePlanform(),
								freeRollDuration,
								mu, 
								muBrake,
								wingToGroundDistance, 
								kCLmaxLanding, 
								cLmaxLanding, 
								cLZeroLanding, 
								cLAlphaLanding, 
								theOperatingConditions.getThrottleLanding(), 
								cruiseCalibrationFactorThrust,
								flightIdleCalibrationFactorThrust,
								groundIdleCalibrationFactorThrust,
								cruiseCalibrationFactorSFC,
								flightIdleCalibrationFactorSFC,
								groundIdleCalibrationFactorSFC,
								cruiseCalibrationFactorEmissionIndexNOx,
								cruiseCalibrationFactorEmissionIndexCO,
								cruiseCalibrationFactorEmissionIndexHC,
								cruiseCalibrationFactorEmissionIndexSoot,
								cruiseCalibrationFactorEmissionIndexCO2,
								cruiseCalibrationFactorEmissionIndexSOx,
								cruiseCalibrationFactorEmissionIndexH2O,
								flightIdleCalibrationFactorEmissionIndexNOx,
								flightIdleCalibrationFactorEmissionIndexCO,
								flightIdleCalibrationFactorEmissionIndexHC,
								flightIdleCalibrationFactorEmissionIndexSoot,
								flightIdleCalibrationFactorEmissionIndexCO2,
								flightIdleCalibrationFactorEmissionIndexSOx,
								flightIdleCalibrationFactorEmissionIndexH2O,
								groundIdleCalibrationFactorEmissionIndexNOx,
								groundIdleCalibrationFactorEmissionIndexCO,
								groundIdleCalibrationFactorEmissionIndexHC,
								groundIdleCalibrationFactorEmissionIndexSoot,
								groundIdleCalibrationFactorEmissionIndexCO2,
								groundIdleCalibrationFactorEmissionIndexSOx,
								groundIdleCalibrationFactorEmissionIndexH2O,
								theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
								);

						theLandingCalculator.calculateLanding(true);

						rangeLanding.addAll(theLandingCalculator.getGroundDistanceList());			
						altitudeLanding.addAll(theLandingCalculator.getVerticalDistanceList());
						timeLanding.addAll(theLandingCalculator.getTimeList());
						fuelUsedLanding.addAll(theLandingCalculator.getFuelUsedList());
						aircraftMassLanding.addAll(
								theLandingCalculator.getWeightList().stream()
								.map(w -> Amount.valueOf(
										w.doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										SI.KILOGRAM)
										)
								.collect(Collectors.toList())
								);
						emissionNOxLanding.addAll(theLandingCalculator.getEmissionNOxList());
						emissionCOLanding.addAll(theLandingCalculator.getEmissionCOList());
						emissionHCLanding.addAll(theLandingCalculator.getEmissionHCList());
						emissionSootLanding.addAll(theLandingCalculator.getEmissionSootList());
						emissionCO2Landing.addAll(theLandingCalculator.getEmissionCO2List());
						emissionSOxLanding.addAll(theLandingCalculator.getEmissionSOxList());
						emissionH2OLanding.addAll(theLandingCalculator.getEmissionH2OList());
						speedTASLanding.addAll(theLandingCalculator.getSpeedTASList());
						speedCASLanding.addAll(theLandingCalculator.getSpeedCASList());
						machLanding.addAll(theLandingCalculator.getMachList());
						cLLanding.addAll(theLandingCalculator.getcLList());
						cDLanding.addAll(theLandingCalculator.getcDList());
						dragLanding.addAll(theLandingCalculator.getDragList());
						totalThrustLanding.addAll(theLandingCalculator.getThrustList());
						fuelFlowLanding.addAll(theLandingCalculator.getFuelFlowList());
						rateOfClimbLanding.addAll(theLandingCalculator.getRateOfClimbList());
						climbAngleLanding.addAll(theLandingCalculator.getGammaList());

						for(int iLanding=0; iLanding<timeLanding.size(); iLanding++) {
							/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
							thermicThrustLanding.add(totalThrustLanding.get(iLanding));
							electricThrustLanding.add(Amount.valueOf(0.0, SI.NEWTON));
							sfcLanding.add(
									(fuelFlowLanding.get(iLanding)/totalThrustLanding.get(iLanding).doubleValue(SI.NEWTON))
									/(0.224809)
									/(0.454/3600)
									);
							efficiencyLanding.add(cLLanding.get(iLanding)/cDLanding.get(iLanding));		
							fuelPowerLanding.add(
									Amount.valueOf(
											thermicThrustLanding.get(iLanding).doubleValue(SI.NEWTON)
											* speedTASLanding.get(iLanding).doubleValue(SI.METERS_PER_SECOND),
											SI.WATT
											)
									);
							batteryPowerLanding.add(
									Amount.valueOf(
											electricThrustLanding.get(iLanding).doubleValue(SI.NEWTON)
											* speedTASLanding.get(iLanding).doubleValue(SI.METERS_PER_SECOND),
											SI.WATT
											)
									);
							fuelEnergyLanding.add(
									Amount.valueOf(
											fuelPowerLanding.get(iLanding).doubleValue(SI.WATT)
											* timeLanding.get(iLanding).doubleValue(SI.SECOND),
											SI.JOULE
											)
									);
							batteryEnergyLanding.add(Amount.valueOf(0.0, SI.JOULE));
						}
					}
					//.....................................................................
					// CHECK ON TOTAL ALTERNATE RANGE
					Amount<Length> totalAlternateRange = Amount.valueOf(
							rangeSecondClimb.get(rangeSecondClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
							+ rangeAlternateCruise.get(rangeAlternateCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
							+ rangeSecondDescent.get(rangeSecondDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
							+ rangeHolding.get(rangeHolding.size()-1).doubleValue(NonSI.NAUTICAL_MILE),
							NonSI.NAUTICAL_MILE
							);
					if(Math.abs(alternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) - totalAlternateRange.doubleValue(NonSI.NAUTICAL_MILE)) < 1.0)
						break;
					
					//.....................................................................
					// NEW ITERATION ALTERNATE CRUISE LENGTH
					currentAlternateCruiseRange = Amount.valueOf( 
							currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
							+ ( alternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
									- rangeSecondClimb.get(rangeSecondClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
									- rangeAlternateCruise.get(rangeAlternateCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
									- rangeSecondDescent.get(rangeSecondDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
									- rangeHolding.get(rangeHolding.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
									),
							NonSI.NAUTICAL_MILE
							);
					if(currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL THAN ZERO, RETURNING ... ");
						return;
					}
				}
				
				//.....................................................................
				// CHECK ON TOTAL MISSION RANGE
				Amount<Length> totalMissionRange = Amount.valueOf(
						rangeTakeOff.get(rangeTakeOff.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeClimb.get(rangeClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeCruise.get(rangeCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeFirstDescent.get(rangeFirstDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeLanding.get(rangeLanding.size()-1).doubleValue(NonSI.NAUTICAL_MILE),
						NonSI.NAUTICAL_MILE
						);
				if(Math.abs(missionRange.doubleValue(NonSI.NAUTICAL_MILE) - totalMissionRange.doubleValue(NonSI.NAUTICAL_MILE)) < 1.0)
					break;
				
				//.....................................................................
				// NEW ITERATION CRUISE LENGTH
				currentCruiseRange = Amount.valueOf( 
						currentCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
						+ ( missionRange.doubleValue(NonSI.NAUTICAL_MILE)
								- rangeTakeOff.get(rangeTakeOff.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeClimb.get(rangeClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeCruise.get(rangeCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeFirstDescent.get(rangeFirstDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeLanding.get(rangeLanding.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								),
						NonSI.NAUTICAL_MILE
						);
				if(currentCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (NEW CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL THAN ZERO, RETURNING ... ");
					return;
				}
				
			} 
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			totalFuel = Amount.valueOf(
					fuelUsedTakeOff.get(fuelUsedTakeOff.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedClimb.get(fuelUsedClimb.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedCruise.get(fuelUsedCruise.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedFirstDescent.get(fuelUsedFirstDescent.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedSecondClimb.get(fuelUsedSecondClimb.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedAlternateCruise.get(fuelUsedAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedSecondDescent.get(fuelUsedSecondDescent.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedHolding.get(fuelUsedHolding.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedLanding.get(fuelUsedLanding.size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);
			
			newInitialFuelMass = totalFuel.to(SI.KILOGRAM).divide(1-fuelReserve); 
			initialMissionMass = operatingEmptyMass
					.plus(singlePassengerMass.times(deisgnPassengersNumber))
					.plus(newInitialFuelMass); 
			
			if(initialMissionMass.doubleValue(SI.KILOGRAM) > maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {

				System.err.println("MAXIMUM TAKE-OFF MASS SURPASSED !! REDUCING PASSENGERS NUMBER TO INCREASE THE FUEL ... ");
				
				deisgnPassengersNumber += (int) Math.ceil(
						(maximumTakeOffMass.minus(initialMissionMass))
						.divide(singlePassengerMass)
						.getEstimatedValue()
						)
						;
				initialMissionMass = maximumTakeOffMass;
				
			}
			
			i++;
			
		} while ( Math.abs(
				(initialFuelMass.to(SI.KILOGRAM).minus(totalFuel.to(SI.KILOGRAM)))
				.divide(initialFuelMass.to(SI.KILOGRAM))
				.times(100)
				.getEstimatedValue()
				)- (fuelReserve*100)
				>= 0.01
				);
		
		if(theTakeOffCalculator.isRotationSpeedWarningFlag() == true)
			System.err.println("WARNING: (MISSION PROFILE :: SIMULATION - TAKE-OFF) THE CHOSEN VRot IS LESS THAN 1.05*VMC. THIS LATTER WILL BE USED ...");
		if(theTakeOffCalculator.isTailStrike() == true)
			System.err.println("WARNING: (MISSION PROFILE :: SIMULATION - TAKE-OFF) TAIL STRIKE !! ");
		if(theFirstDescentCalculator.getDescentMaxIterationErrorFlag() == true) 
			System.err.println("WARNING: (MISSION PROFILE :: ITERATIVE LOOP CRUISE/IDLE - FIRST DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		if(theSecondDescentCalculator.getDescentMaxIterationErrorFlag() == true) 
			System.err.println("WARNING: (MISSION PROFILE :: ITERATIVE LOOP CRUISE/IDLE - SECOND DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		
		//-------------------------------------------------------------------------------------------------
		// MANAGING OUTPUT DATA:
		this.totalFuel = Amount.valueOf(
				fuelUsedTakeOff.get(fuelUsedTakeOff.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedClimb.get(fuelUsedClimb.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedCruise.get(fuelUsedCruise.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedFirstDescent.get(fuelUsedFirstDescent.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedSecondClimb.get(fuelUsedSecondClimb.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedAlternateCruise.get(fuelUsedAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedSecondDescent.get(fuelUsedSecondDescent.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedHolding.get(fuelUsedHolding.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedLanding.get(fuelUsedLanding.size()-1).doubleValue(SI.KILOGRAM),
				SI.KILOGRAM
				);
		this.blockFuel = Amount.valueOf(
				fuelUsedTakeOff.get(fuelUsedTakeOff.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedClimb.get(fuelUsedClimb.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedCruise.get(fuelUsedCruise.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedFirstDescent.get(fuelUsedFirstDescent.size()-1).doubleValue(SI.KILOGRAM)
				+ fuelUsedLanding.get(fuelUsedLanding.size()-1).doubleValue(SI.KILOGRAM),
				SI.KILOGRAM
				);
		this.endMissionMass = this.initialMissionMass.to(SI.KILOGRAM).minus(this.totalFuel.to(SI.KILOGRAM));
		this.totalTime = Amount.valueOf(
				timeTakeOff.get(timeTakeOff.size()-1).doubleValue(NonSI.MINUTE)
				+ timeClimb.get(timeClimb.size()-1).doubleValue(NonSI.MINUTE)
				+ timeCruise.get(timeCruise.size()-1).doubleValue(NonSI.MINUTE)
				+ timeFirstDescent.get(timeFirstDescent.size()-1).doubleValue(NonSI.MINUTE)
				+ timeSecondClimb.get(timeSecondClimb.size()-1).doubleValue(NonSI.MINUTE)
				+ timeAlternateCruise.get(timeAlternateCruise.size()-1).doubleValue(NonSI.MINUTE)
				+ timeSecondDescent.get(timeSecondDescent.size()-1).doubleValue(NonSI.MINUTE)
				+ timeHolding.get(timeHolding.size()-1).doubleValue(NonSI.MINUTE)
				+ timeLanding.get(timeLanding.size()-1).doubleValue(NonSI.MINUTE),
				NonSI.MINUTE
				);
		this.blockTime = Amount.valueOf(
				timeTakeOff.get(timeTakeOff.size()-1).doubleValue(NonSI.MINUTE)
				+ timeClimb.get(timeClimb.size()-1).doubleValue(NonSI.MINUTE)
				+ timeCruise.get(timeCruise.size()-1).doubleValue(NonSI.MINUTE)
				+ timeFirstDescent.get(timeFirstDescent.size()-1).doubleValue(NonSI.MINUTE)
				+ timeLanding.get(timeLanding.size()-1).doubleValue(NonSI.MINUTE),
				NonSI.MINUTE
				);
		this.totalRange = Amount.valueOf(
				rangeTakeOff.get(rangeTakeOff.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeClimb.get(rangeClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeCruise.get(rangeCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeFirstDescent.get(rangeFirstDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeSecondClimb.get(rangeSecondClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeAlternateCruise.get(rangeAlternateCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeSecondDescent.get(rangeSecondDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeHolding.get(rangeHolding.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeLanding.get(rangeLanding.size()-1).doubleValue(NonSI.NAUTICAL_MILE),
				NonSI.NAUTICAL_MILE
				);
		this.totalFuelPower = Amount.valueOf(
				fuelPowerTakeOff.get(fuelPowerTakeOff.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerClimb.get(fuelPowerClimb.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerCruise.get(fuelPowerCruise.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerFirstDescent.get(fuelPowerFirstDescent.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerSecondClimb.get(fuelPowerSecondClimb.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerAlternateCruise.get(fuelPowerAlternateCruise.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerSecondDescent.get(fuelPowerSecondDescent.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerHolding.get(fuelPowerHolding.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ fuelPowerLanding.get(fuelPowerLanding.size()-1).doubleValue(SI.KILO(SI.WATT)),
				SI.KILO(SI.WATT)
				);
		this.totalBatteryPower = Amount.valueOf(
				batteryPowerTakeOff.get(batteryPowerTakeOff.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerClimb.get(batteryPowerClimb.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerCruise.get(batteryPowerCruise.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerFirstDescent.get(batteryPowerFirstDescent.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerSecondClimb.get(batteryPowerSecondClimb.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerAlternateCruise.get(batteryPowerAlternateCruise.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerSecondDescent.get(batteryPowerSecondDescent.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerHolding.get(batteryPowerHolding.size()-1).doubleValue(SI.KILO(SI.WATT))
				+ batteryPowerLanding.get(batteryPowerLanding.size()-1).doubleValue(SI.KILO(SI.WATT)),
				SI.KILO(SI.WATT)
				);
		this.totalFuelEnergy = Amount.valueOf(
				fuelEnergyTakeOff.get(fuelEnergyTakeOff.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyClimb.get(fuelEnergyClimb.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyCruise.get(fuelEnergyCruise.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyFirstDescent.get(fuelEnergyFirstDescent.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergySecondClimb.get(fuelEnergySecondClimb.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyAlternateCruise.get(fuelEnergyAlternateCruise.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergySecondDescent.get(fuelEnergySecondDescent.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyHolding.get(fuelEnergyHolding.size()-1).doubleValue(SI.JOULE)
				+ fuelEnergyLanding.get(fuelEnergyLanding.size()-1).doubleValue(SI.JOULE),
				SI.JOULE
				);
		this.totalBatteryEnergy = Amount.valueOf(
				batteryEnergyTakeOff.get(batteryEnergyTakeOff.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyClimb.get(batteryEnergyClimb.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyCruise.get(batteryEnergyCruise.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyFirstDescent.get(batteryEnergyFirstDescent.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergySecondClimb.get(batteryEnergySecondClimb.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyAlternateCruise.get(batteryEnergyAlternateCruise.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergySecondDescent.get(batteryEnergySecondDescent.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyHolding.get(batteryEnergyHolding.size()-1).doubleValue(SI.JOULE)
				+ batteryEnergyLanding.get(batteryEnergyLanding.size()-1).doubleValue(SI.JOULE),
				SI.JOULE
				);
		
		//.................................................................................................
		// RANGE
		this.rangeMap.put(MissionPhasesEnum.TAKE_OFF, rangeTakeOff.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.CLIMB, rangeClimb.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.CRUISE, rangeCruise.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.FIRST_DESCENT, rangeFirstDescent.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.SECOND_CLIMB, rangeSecondClimb.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, rangeAlternateCruise.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.SECOND_DESCENT, rangeSecondDescent.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.HOLDING, rangeHolding.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		this.rangeMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, rangeLanding.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// ALTITUDE
		this.altitudeMap.put(MissionPhasesEnum.TAKE_OFF, altitudeTakeOff.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.CLIMB, altitudeClimb.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.CRUISE, altitudeCruise.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.FIRST_DESCENT, altitudeFirstDescent.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.SECOND_CLIMB, altitudeSecondClimb.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, altitudeAlternateCruise.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.SECOND_DESCENT, altitudeSecondDescent.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.HOLDING, altitudeHolding.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		this.altitudeMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, altitudeLanding.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		
		//.................................................................................................
		// TIME
		this.timeMap.put(MissionPhasesEnum.TAKE_OFF, timeTakeOff.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.CLIMB, timeClimb.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.CRUISE, timeCruise.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.FIRST_DESCENT, timeFirstDescent.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.SECOND_CLIMB, timeSecondClimb.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, timeAlternateCruise.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.SECOND_DESCENT, timeSecondDescent.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.HOLDING, timeHolding.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		this.timeMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, timeLanding.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// FUEL USED
		this.fuelUsedMap.put(MissionPhasesEnum.TAKE_OFF, fuelUsedTakeOff.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.CLIMB, fuelUsedClimb.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.CRUISE, fuelUsedCruise.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.FIRST_DESCENT, fuelUsedFirstDescent.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.SECOND_CLIMB, fuelUsedSecondClimb.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, fuelUsedAlternateCruise.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.SECOND_DESCENT, fuelUsedSecondDescent.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.HOLDING, fuelUsedHolding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.fuelUsedMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, fuelUsedLanding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// AIRCRAFT MASS
		this.massMap.put(MissionPhasesEnum.TAKE_OFF, aircraftMassTakeOff.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.CLIMB, aircraftMassClimb.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.CRUISE, aircraftMassCruise.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.FIRST_DESCENT, aircraftMassFirstDescent.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.SECOND_CLIMB, aircraftMassSecondClimb.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, aircraftMassAlternateCruise.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.SECOND_DESCENT, aircraftMassSecondDescent.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.HOLDING, aircraftMassHolding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		this.massMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, aircraftMassLanding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION NOx
		this.emissionNOxMap.put(MissionPhasesEnum.TAKE_OFF, emissionNOxTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.CLIMB, emissionNOxClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.CRUISE, emissionNOxCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionNOxFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionNOxSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionNOxAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionNOxSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.HOLDING, emissionNOxHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionNOxMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionNOxLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION CO
		this.emissionCOMap.put(MissionPhasesEnum.TAKE_OFF, emissionCOTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.CLIMB, emissionCOClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.CRUISE, emissionCOCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionCOFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionCOSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionCOAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionCOSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.HOLDING, emissionCOHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCOMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionCOLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION HC
		this.emissionHCMap.put(MissionPhasesEnum.TAKE_OFF, emissionHCTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.CLIMB, emissionHCClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.CRUISE, emissionHCCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionHCFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionHCSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionHCAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionHCSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.HOLDING, emissionHCHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionHCMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionHCLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION Soot
		this.emissionSootMap.put(MissionPhasesEnum.TAKE_OFF, emissionSootTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.CLIMB, emissionSootClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.CRUISE, emissionSootCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionSootFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionSootSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionSootAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionSootSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.HOLDING, emissionSootHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSootMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionSootLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION CO2
		this.emissionCO2Map.put(MissionPhasesEnum.TAKE_OFF, emissionCO2TakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.CLIMB, emissionCO2Climb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.CRUISE, emissionCO2Cruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.FIRST_DESCENT, emissionCO2FirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.SECOND_CLIMB, emissionCO2SecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionCO2AlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.SECOND_DESCENT, emissionCO2SecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.HOLDING, emissionCO2Holding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionCO2Map.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionCO2Landing.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION SOx
		this.emissionSOxMap.put(MissionPhasesEnum.TAKE_OFF, emissionSOxTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.CLIMB, emissionSOxClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.CRUISE, emissionSOxCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionSOxFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionSOxSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionSOxAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionSOxSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.HOLDING, emissionSOxHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionSOxMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionSOxLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// EMISSION H2O
		this.emissionH2OMap.put(MissionPhasesEnum.TAKE_OFF, emissionH2OTakeOff.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.CLIMB, emissionH2OClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.CRUISE, emissionH2OCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.FIRST_DESCENT, emissionH2OFirstDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.SECOND_CLIMB, emissionH2OSecondClimb.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, emissionH2OAlternateCruise.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.SECOND_DESCENT, emissionH2OSecondDescent.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.HOLDING, emissionH2OHolding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		this.emissionH2OMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, emissionH2OLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
		//.................................................................................................
		// SPEED TAS
		this.speedTASMissionMap.put(MissionPhasesEnum.TAKE_OFF, speedTASTakeOff.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.CLIMB, speedTASClimb.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.CRUISE, speedTASCruise.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, speedTASFirstDescent.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, speedTASSecondClimb.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, speedTASAlternateCruise.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, speedTASSecondDescent.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.HOLDING, speedTASHolding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedTASMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, speedTASLanding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		
		//.................................................................................................
		// SPEED CAS
		this.speedCASMissionMap.put(MissionPhasesEnum.TAKE_OFF, speedCASTakeOff.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.CLIMB, speedCASClimb.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.CRUISE, speedCASCruise.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, speedCASFirstDescent.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, speedCASSecondClimb.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, speedCASAlternateCruise.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, speedCASSecondDescent.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.HOLDING, speedCASHolding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		this.speedCASMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, speedCASLanding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		
		//.................................................................................................
		// MACH
		this.machMissionMap.put(MissionPhasesEnum.TAKE_OFF, machTakeOff);
		this.machMissionMap.put(MissionPhasesEnum.CLIMB, machClimb);
		this.machMissionMap.put(MissionPhasesEnum.CRUISE, machCruise);
		this.machMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, machFirstDescent);
		this.machMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, machSecondClimb);
		this.machMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, machAlternateCruise);
		this.machMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, machSecondDescent);
		this.machMissionMap.put(MissionPhasesEnum.HOLDING, machHolding);
		this.machMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, machLanding);
		
		//.................................................................................................
		// CL
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.TAKE_OFF, cLTakeOff);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.CLIMB, cLClimb);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.CRUISE, cLCruise);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, cLFirstDescent);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, cLSecondClimb);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, cLAlternateCruise);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, cLSecondDescent);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.HOLDING, cLHolding);
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, cLLanding);
		
		//.................................................................................................
		// CD
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.TAKE_OFF, cDTakeOff);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.CLIMB, cDClimb);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.CRUISE, cDCruise);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, cDFirstDescent);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, cDSecondClimb);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, cDAlternateCruise);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, cDSecondDescent);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.HOLDING, cDHolding);
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, cDLanding);
		
		//.................................................................................................
		// EFFICIENCY
		this.efficiencyMissionMap.put(MissionPhasesEnum.TAKE_OFF, efficiencyTakeOff);
		this.efficiencyMissionMap.put(MissionPhasesEnum.CLIMB, efficiencyClimb);
		this.efficiencyMissionMap.put(MissionPhasesEnum.CRUISE, efficiencyCruise);
		this.efficiencyMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, efficiencyFirstDescent);
		this.efficiencyMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, efficiencySecondClimb);
		this.efficiencyMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, efficiencyAlternateCruise);
		this.efficiencyMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, efficiencySecondDescent);
		this.efficiencyMissionMap.put(MissionPhasesEnum.HOLDING, efficiencyHolding);
		this.efficiencyMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, efficiencyLanding);
		
		//.................................................................................................
		// DRAG
		this.dragMissionMap.put(MissionPhasesEnum.TAKE_OFF, dragTakeOff.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.CLIMB, dragClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.CRUISE, dragCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, dragFirstDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, dragSecondClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, dragAlternateCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, dragSecondDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.HOLDING, dragHolding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.dragMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, dragLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// TOTAL THRUST
		this.totalThrustMissionMap.put(MissionPhasesEnum.TAKE_OFF, totalThrustTakeOff.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.CLIMB, totalThrustClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.CRUISE, totalThrustCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, totalThrustFirstDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, totalThrustSecondClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, totalThrustAlternateCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, totalThrustSecondDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.HOLDING, totalThrustHolding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.totalThrustMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, totalThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// THERMIC THRUST
		this.thermicThrustMissionMap.put(MissionPhasesEnum.TAKE_OFF, thermicThrustTakeOff.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.CLIMB, thermicThrustClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.CRUISE, thermicThrustCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, thermicThrustFirstDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, thermicThrustSecondClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, thermicThrustAlternateCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, thermicThrustSecondDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.HOLDING, thermicThrustHolding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.thermicThrustMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, thermicThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// ELECTRIC THRUST
		this.electricThrustMissionMap.put(MissionPhasesEnum.TAKE_OFF, electricThrustTakeOff.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.CLIMB, electricThrustClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.CRUISE, electricThrustCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, electricThrustFirstDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, electricThrustSecondClimb.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, electricThrustAlternateCruise.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, electricThrustSecondDescent.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.HOLDING, electricThrustHolding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		this.electricThrustMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, electricThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// SFC
		this.sfcMissionMap.put(MissionPhasesEnum.TAKE_OFF, sfcTakeOff.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.CLIMB, sfcClimb.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.CRUISE, sfcCruise.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, sfcFirstDescent.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, sfcSecondClimb.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, sfcAlternateCruise.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, sfcSecondDescent.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.HOLDING, sfcHolding.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, sfcLanding.stream().map(sfc -> sfc.isNaN() ? 0.0 : sfc).collect(Collectors.toList())); /* lb/lb*hr */
		
		//.................................................................................................
		// FUEL FLOW
		this.fuelFlowMissionMap.put(MissionPhasesEnum.TAKE_OFF, fuelFlowTakeOff.stream().map(e -> e*60.0).collect(Collectors.toList())); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.CLIMB, fuelFlowClimb); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.CRUISE, fuelFlowCruise); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, fuelFlowFirstDescent); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, fuelFlowSecondClimb); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, fuelFlowAlternateCruise); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, fuelFlowSecondDescent); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.HOLDING, fuelFlowHolding); /* kg/min */
		this.fuelFlowMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, fuelFlowLanding.stream().map(e -> e*60.0).collect(Collectors.toList())); /* kg/min */
		
		//.................................................................................................
		// RATE OF CLIMB
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.TAKE_OFF, rateOfClimbTakeOff.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.CLIMB, rateOfClimbClimb.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.CRUISE, rateOfClimbCruise.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, rateOfClimbFirstDescent.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, rateOfClimbSecondClimb.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, rateOfClimbAlternateCruise.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, rateOfClimbSecondDescent.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.HOLDING, rateOfClimbHolding.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, rateOfClimbLanding.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// CLIMB ANGLE
		this.climbAngleMissionMap.put(MissionPhasesEnum.TAKE_OFF, climbAngleTakeOff.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.CLIMB, climbAngleClimb.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.CRUISE, climbAngleCruise.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, climbAngleFirstDescent.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, climbAngleSecondClimb.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, climbAngleAlternateCruise.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, climbAngleSecondDescent.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.HOLDING, climbAngleHolding.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		this.climbAngleMissionMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, climbAngleLanding.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// FUEL POWER
		this.fuelPowerMap.put(MissionPhasesEnum.TAKE_OFF, fuelPowerTakeOff.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.CLIMB, fuelPowerClimb.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.CRUISE, fuelPowerCruise.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.FIRST_DESCENT, fuelPowerFirstDescent.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.SECOND_CLIMB, fuelPowerSecondClimb.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, fuelPowerAlternateCruise.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.SECOND_DESCENT, fuelPowerSecondDescent.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.HOLDING, fuelPowerHolding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.fuelPowerMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, fuelPowerLanding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		
		//.................................................................................................
		// BATTERY POWER
		this.batteryPowerMap.put(MissionPhasesEnum.TAKE_OFF, batteryPowerTakeOff.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.CLIMB, batteryPowerClimb.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.CRUISE, batteryPowerCruise.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.FIRST_DESCENT, batteryPowerFirstDescent.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.SECOND_CLIMB, batteryPowerSecondClimb.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, batteryPowerAlternateCruise.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.SECOND_DESCENT, batteryPowerSecondDescent.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.HOLDING, batteryPowerHolding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		this.batteryPowerMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, batteryPowerLanding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		
		//.................................................................................................
		// FUEL ENERGY
		this.fuelEnergyMap.put(MissionPhasesEnum.TAKE_OFF, fuelEnergyTakeOff.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.CLIMB, fuelEnergyClimb.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.CRUISE, fuelEnergyCruise.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.FIRST_DESCENT, fuelEnergyFirstDescent.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.SECOND_CLIMB, fuelEnergySecondClimb.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, fuelEnergyAlternateCruise.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.SECOND_DESCENT, fuelEnergySecondDescent.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.HOLDING, fuelEnergyHolding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.fuelEnergyMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, fuelEnergyLanding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// BATTERY ENERGY
		this.batteryEnergyMap.put(MissionPhasesEnum.TAKE_OFF, batteryEnergyTakeOff.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.CLIMB, batteryEnergyClimb.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.CRUISE, batteryEnergyCruise.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.FIRST_DESCENT, batteryEnergyFirstDescent.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.SECOND_CLIMB, batteryEnergySecondClimb.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, batteryEnergyAlternateCruise.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.SECOND_DESCENT, batteryEnergySecondDescent.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.HOLDING, batteryEnergyHolding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		this.batteryEnergyMap.put(MissionPhasesEnum.APPROACH_AND_LANDING, batteryEnergyLanding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		
	}

	@SuppressWarnings("unchecked")
	public void plotProfiles(
			List<PerformancePlotEnum> _plotList,
			String _missionProfilesFolderPath) {
		
		//.....................................................................................
		// COMMON LISTS FOR ALL CHARTS
		List<Amount<Length>> altitudeListPlot = new ArrayList<>();
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.TAKE_OFF));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.CLIMB));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.CRUISE));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.FIRST_DESCENT));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.SECOND_CLIMB));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.SECOND_DESCENT));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.HOLDING));
		altitudeListPlot.addAll(altitudeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
		
		List<Amount<Duration>> timeListPlot = new ArrayList<>();
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.TAKE_OFF)
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.CLIMB).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.CRUISE).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.HOLDING).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		timeListPlot.addAll(
				timeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
				.map(time -> time.plus(timeListPlot.get(timeListPlot.size()-1)))
				.collect(Collectors.toList())
				);
		//.....................................................................................
		
		if(_plotList.contains(PerformancePlotEnum.RANGE_PROFILE)) { 
			
			List<Amount<Length>> rangeListPlot = new ArrayList<>();
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			rangeListPlot.addAll(
					rangeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(range -> range.plus(rangeListPlot.get(rangeListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rangeListPlot.stream()
							.map(r -> r.to(SI.KILOMETER))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"km", "m",
					_missionProfilesFolderPath, "Range_Profile_SI",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rangeListPlot.stream()
							.map(r -> r.to(NonSI.NAUTICAL_MILE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"nmi", "ft",
					_missionProfilesFolderPath, "Range_Profile_IMPERIAL",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.TIME_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "m",
					_missionProfilesFolderPath, "Time_Profile_(min)_SI",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "ft",
					_missionProfilesFolderPath, "Time_Profile_(min)_IMPERIAL",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "m",
					_missionProfilesFolderPath, "Time_Profile_(hours)_SI",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "ft",
					_missionProfilesFolderPath, "Time_Profile_(hours)_IMPERIAL",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_USED_PROFILE)) { 
			
			List<Amount<Mass>> fuelUsedListPlot = new ArrayList<>();
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			fuelUsedListPlot.addAll(
					fuelUsedMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(fuel -> fuel.plus(fuelUsedListPlot.get(fuelUsedListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							fuelUsedListPlot.stream()
							.map(f -> f.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"kg", "m",
					_missionProfilesFolderPath, "Fuel_used_Profile_SI",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							fuelUsedListPlot.stream()
							.map(f -> f.to(NonSI.POUND))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							altitudeListPlot.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"lb", "ft",
					_missionProfilesFolderPath, "Fuel_used_Profile_IMPERIAL",
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.WEIGHT_PROFILE)) { 
			
			List<Amount<Mass>> aircraftMassListPlot = new ArrayList<>();
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.TAKE_OFF));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.CLIMB));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.CRUISE));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.FIRST_DESCENT));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.SECOND_CLIMB));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.SECOND_DESCENT));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.HOLDING));
			aircraftMassListPlot.addAll(massMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							aircraftMassListPlot.stream()
							.map(m -> m.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					0.0, null, null, null,
					"Time", "Aircraft mass",
					"min", "kg",
					_missionProfilesFolderPath, "Mass_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.EMISSIONS_PROFILE)) { 

			//..........................................................................................
			// NOx EMISSIONS
			List<Amount<Mass>> emissionNOxListPlot = new ArrayList<>();
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionNOxListPlot.addAll(
					emissionNOxMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionNOxListPlot.get(emissionNOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// CO EMISSIONS
			List<Amount<Mass>> emissionCOListPlot = new ArrayList<>();
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCOListPlot.addAll(
					emissionCOMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionCOListPlot.get(emissionCOListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// HC EMISSION
			List<Amount<Mass>> emissionHCListPlot = new ArrayList<>();
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionHCListPlot.addAll(
					emissionHCMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionHCListPlot.get(emissionHCListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// SOOT EMISSION
			List<Amount<Mass>> emissionSootListPlot = new ArrayList<>();
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSootListPlot.addAll(
					emissionSootMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionSootListPlot.get(emissionSootListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// CO2 EMISSION
			List<Amount<Mass>> emissionCO2ListPlot = new ArrayList<>();
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionCO2ListPlot.addAll(
					emissionCO2Map.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionCO2ListPlot.get(emissionCO2ListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// SOx EMISSION
			List<Amount<Mass>> emissionSOxListPlot = new ArrayList<>();
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionSOxListPlot.addAll(
					emissionSOxMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionSOxListPlot.get(emissionSOxListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			//..........................................................................................
			// H2O EMISSION
			List<Amount<Mass>> emissionH2OListPlot = new ArrayList<>();
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.TAKE_OFF)
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.CLIMB).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.CRUISE).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.FIRST_DESCENT).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.SECOND_CLIMB).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.SECOND_DESCENT).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.HOLDING).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			emissionH2OListPlot.addAll(
					emissionH2OMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).stream()
					.map(e -> e.plus(emissionH2OListPlot.get(emissionH2OListPlot.size()-1)))
					.collect(Collectors.toList())
					);
			
			List<Double[]> xList1 = new ArrayList<>();
			for(int i=0; i<5; i++) {
				xList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).toArray())
						);
			}
			List<Double[]> xList2 = new ArrayList<>();
			for(int i=0; i<2; i++) {
				xList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).toArray())
						);
			}
			
			List<Double[]> yList1 = new ArrayList<>();
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionNOxListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionCOListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionHCListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionSootListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionSOxListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			
			List<Double[]> yList2 = new ArrayList<>();
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionCO2ListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					emissionH2OListPlot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray())
					);
			
			List<String> legend1 = new ArrayList<>();
			legend1.add("NOx");
			legend1.add("CO");
			legend1.add("HC");
			legend1.add("Soot");
			legend1.add("SOx");
			
			List<String> legend2 = new ArrayList<>();
			legend2.add("CO2");
			legend2.add("H2O");
			
			try {
				MyChartToFileUtils.plot(
						xList1, yList1, 
						"Emissions (NOx, CO, HC, Soot, SOx)", "Time", "Emissions", 
						0.0, null, null, null, 
						"min", "g", 
						true, legend1, 
						_missionProfilesFolderPath, "Emission_NOx_CO_HC_Soot_SOx_profile", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						xList2, yList2, 
						"Emissions (CO2, H2O)", "Time", "Emissions", 
						0.0, null, null, null, 
						"min", "g", 
						true, legend2, 
						_missionProfilesFolderPath, "Emission_CO2_H2O_profile", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.SPEED_PROFILE)) { 

			List<Amount<Velocity>> speedTASListPlot = new ArrayList<>();
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.CLIMB));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.CRUISE));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.HOLDING));
			speedTASListPlot.addAll(speedTASMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Velocity>> speedCASListPlot = new ArrayList<>();
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.CLIMB));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.CRUISE));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.HOLDING));
			speedCASListPlot.addAll(speedCASMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Double[]> xList = new ArrayList<>();
			for(int i=0; i<2; i++) {
				xList.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).toArray())
						);
			}
			
			List<Double[]> yList1 = new ArrayList<>();
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					speedTASListPlot.stream().mapToDouble(e -> e.doubleValue(SI.METERS_PER_SECOND)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					speedCASListPlot.stream().mapToDouble(e -> e.doubleValue(SI.METERS_PER_SECOND)).toArray())
					);
			
			List<Double[]> yList2 = new ArrayList<>();
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					speedTASListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.KNOT)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					speedCASListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.KNOT)).toArray())
					);
			
			List<String> legend = new ArrayList<>();
			legend.add("Speed TAS");
			legend.add("Speed CAS");
			
			try {
				MyChartToFileUtils.plot(
						xList, yList1, 
						"Speed Profile", "Time", "Speed", 
						0.0, null, null, null, 
						"min", "m/s", 
						true, legend, 
						_missionProfilesFolderPath, "Speed_profile_SI", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						xList, yList2, 
						"Speed Profile", "Time", "Speed", 
						0.0, null, null, null, 
						"min", "kts", 
						true, legend, 
						_missionProfilesFolderPath, "Speed_profile_IMPERIAL", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.MACH_PROFILE)) { 
			
			List<Double> machListPlot = new ArrayList<>();
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.CLIMB));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.CRUISE));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.HOLDING));
			machListPlot.addAll(machMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									machListPlot
									)
							),
					0.0, null, null, null,
					"Time", "Mach number",
					"min", "",
					_missionProfilesFolderPath, "Mach_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.CL_PROFILE)) { 
			
			List<Double> cLListPlot = new ArrayList<>();
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.CLIMB));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.CRUISE));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.HOLDING));
			cLListPlot.addAll(liftingCoefficientMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									cLListPlot
									)
							),
					0.0, null, null, null,
					"Time", "CL",
					"min", "",
					_missionProfilesFolderPath, "Lifting_Coefficient_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.CD_PROFILE)) { 
			
			List<Double> cDListPlot = new ArrayList<>();
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.CLIMB));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.CRUISE));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.HOLDING));
			cDListPlot.addAll(dragCoefficientMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									cDListPlot
									)
							),
					0.0, null, null, null,
					"Time", "CD",
					"min", "",
					_missionProfilesFolderPath, "Drag_Coefficient_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.EFFICIENCY_PROFILE)) { 
			
			List<Double> efficiencyListPlot = new ArrayList<>();
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.CLIMB));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.CRUISE));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.HOLDING));
			efficiencyListPlot.addAll(efficiencyMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									efficiencyListPlot
									)
							),
					0.0, null, null, null,
					"Time", "Efficiency",
					"min", "",
					_missionProfilesFolderPath, "Efficiency_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.DRAG_THRUST_PROFILE)) { 

			List<Amount<Force>> dragListPlot = new ArrayList<>();
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.CLIMB));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.CRUISE));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.HOLDING));
			dragListPlot.addAll(dragMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Force>> totalThrustListPlot = new ArrayList<>();
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.CLIMB));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.CRUISE));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.HOLDING));
			totalThrustListPlot.addAll(totalThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Force>> thermicThrustListPlot = new ArrayList<>();
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.CLIMB));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.CRUISE));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.HOLDING));
			thermicThrustListPlot.addAll(thermicThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Force>> electricThrustListPlot = new ArrayList<>();
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.CLIMB));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.CRUISE));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.HOLDING));
			electricThrustListPlot.addAll(electricThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Double[]> xList = new ArrayList<>();
			for(int i=0; i<4; i++) {
				xList.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).toArray())
						);
			}
			
			List<Double[]> yList1 = new ArrayList<>();
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					dragListPlot.stream().mapToDouble(e -> e.doubleValue(SI.NEWTON)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					totalThrustListPlot.stream().mapToDouble(e -> e.doubleValue(SI.NEWTON)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					thermicThrustListPlot.stream().mapToDouble(e -> e.doubleValue(SI.NEWTON)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					electricThrustListPlot.stream().mapToDouble(e -> e.doubleValue(SI.NEWTON)).toArray())
					);
			
			List<Double[]> yList2 = new ArrayList<>();
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					dragListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.POUND_FORCE)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					totalThrustListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.POUND_FORCE)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					thermicThrustListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.POUND_FORCE)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					electricThrustListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.POUND_FORCE)).toArray())
					);
			
			List<String> legend = new ArrayList<>();
			legend.add("Drag");
			legend.add("Total Thrust");
			legend.add("Thermic Thrust");
			legend.add("Electrical Thrust");
			
			try {
				MyChartToFileUtils.plot(
						xList, yList1, 
						"Drag and Thrust Profile", "Time", "Forces", 
						0.0, null, null, null, 
						"min", "N", 
						true, legend, 
						_missionProfilesFolderPath, "Drag_Thrust_profile_SI", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						xList, yList2, 
						"Drag and Thrust Profile", "Time", "Forces", 
						0.0, null, null, null, 
						"min", "lbf", 
						true, legend, 
						_missionProfilesFolderPath, "Drag_Thrust_profile_IMPERIAL", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_FLOW_PROFILE)) { 
			
			List<Double> fuelFlowListPlot = new ArrayList<>();
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.CLIMB));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.CRUISE));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.HOLDING));
			fuelFlowListPlot.addAll(fuelFlowMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									fuelFlowListPlot
									)
							),
					0.0, null, null, null,
					"Time", "Fuel Flow",
					"min", "kg/min",
					_missionProfilesFolderPath, "Fuel_Flow_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.SFC_PROFILE)) { 
			
			List<Double> sfcListPlot = new ArrayList<>();
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.CLIMB));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.CRUISE));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.HOLDING));
			sfcListPlot.addAll(sfcMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									sfcListPlot
									)
							),
					0.0, null, null, null,
					"Time", "Specific Fuel Consmption",
					"min", "lb/lb*hr",
					_missionProfilesFolderPath, "SFC_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.RATE_OF_CLIMB_PROFILE)) { 
			
			List<Amount<Velocity>> rateOfClimbListPlot = new ArrayList<>();
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.CLIMB));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.CRUISE));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.HOLDING));
			rateOfClimbListPlot.addAll(rateOfClimbMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimbListPlot.stream()
							.map(t -> t.to(SI.METERS_PER_SECOND))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Time", "Rate of Climb",
					"min", "m/s",
					_missionProfilesFolderPath, "Rate_of_Climb_Profile_SI", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimbListPlot.stream()
							.map(t -> t.to(MyUnits.FOOT_PER_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Time", "Rate of Climb",
					"min", "ft/min",
					_missionProfilesFolderPath, "Rate_of_Climb_Profile_IMPERIAL", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.CLIMB_ANGLE_PROFILE)) { 
			
			List<Amount<Angle>> climbAngleListPlot = new ArrayList<>();
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.TAKE_OFF));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.CLIMB));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.CRUISE));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.FIRST_DESCENT));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_CLIMB));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_DESCENT));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.HOLDING));
			climbAngleListPlot.addAll(climbAngleMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							climbAngleListPlot.stream()
							.map(t -> t.to(SI.RADIAN).times(100.0))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Time", "Climb Gradient",
					"min", "%",
					_missionProfilesFolderPath, "Climb_Gradient_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							timeListPlot.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							climbAngleListPlot.stream()
							.map(t -> t.to(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Time", "Climb Angle",
					"min", "deg",
					_missionProfilesFolderPath, "Climb_Angle_Profile", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
		}
	
		if(_plotList.contains(PerformancePlotEnum.POWER_PROFILE)) { 

			List<Amount<Power>> fuelPowerListPlot = new ArrayList<>();
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.TAKE_OFF));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.CLIMB));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.CRUISE));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.FIRST_DESCENT));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.SECOND_CLIMB));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.SECOND_DESCENT));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.HOLDING));
			fuelPowerListPlot.addAll(fuelPowerMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Power>> batteryPowerListPlot = new ArrayList<>();
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.TAKE_OFF));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.CLIMB));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.CRUISE));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.FIRST_DESCENT));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.SECOND_CLIMB));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.SECOND_DESCENT));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.HOLDING));
			batteryPowerListPlot.addAll(batteryPowerMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Double[]> xList = new ArrayList<>();
			for(int i=0; i<2; i++) {
				xList.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).toArray())
						);
			}
			
			List<Double[]> yList1 = new ArrayList<>();
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					fuelPowerListPlot.stream().mapToDouble(e -> e.doubleValue(SI.KILO(SI.WATT))).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					batteryPowerListPlot.stream().mapToDouble(e -> e.doubleValue(SI.KILO(SI.WATT))).toArray())
					);
			
			List<Double[]> yList2 = new ArrayList<>();
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					fuelPowerListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.HORSEPOWER)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					batteryPowerListPlot.stream().mapToDouble(e -> e.doubleValue(NonSI.HORSEPOWER)).toArray())
					);
			
			List<String> legend = new ArrayList<>();
			legend.add("Fuel Power");
			legend.add("Battery Power");
			
			try {
				MyChartToFileUtils.plot(
						xList, yList1, 
						"Power Profile", "Time", "Powers", 
						0.0, null, null, null, 
						"min", "kW", 
						true, legend, 
						_missionProfilesFolderPath, "Power_profile_SI", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						xList, yList2, 
						"Power Profile", "Time", "Powers", 
						0.0, null, null, null, 
						"min", "hp", 
						true, legend, 
						_missionProfilesFolderPath, "Power_profile_IMPERIAL", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.ENERGY_PROFILE)) { 

			List<Amount<Energy>> fuelEnergyListPlot = new ArrayList<>();
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.TAKE_OFF));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.CLIMB));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.CRUISE));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.FIRST_DESCENT));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.SECOND_CLIMB));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.SECOND_DESCENT));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.HOLDING));
			fuelEnergyListPlot.addAll(fuelEnergyMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Amount<Energy>> batteryEnergyListPlot = new ArrayList<>();
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.TAKE_OFF));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.CLIMB));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.CRUISE));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.FIRST_DESCENT));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.SECOND_CLIMB));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.ALTERNATE_CRUISE));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.SECOND_DESCENT));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.HOLDING));
			batteryEnergyListPlot.addAll(batteryEnergyMap.get(MissionPhasesEnum.APPROACH_AND_LANDING));
			
			List<Double[]> xList = new ArrayList<>();
			for(int i=0; i<2; i++) {
				xList.add(MyArrayUtils.convertFromDoubleToPrimitive(
						timeListPlot.stream().mapToDouble(t -> t.doubleValue(NonSI.HOUR)).toArray())
						);
			}
			
			List<Double[]> yList1 = new ArrayList<>();
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					fuelEnergyListPlot.stream().mapToDouble(e -> e.doubleValue(SI.JOULE)).toArray())
					);
			yList1.add(MyArrayUtils.convertFromDoubleToPrimitive(
					batteryEnergyListPlot.stream().mapToDouble(e -> e.doubleValue(SI.JOULE)).toArray())
					);
			
			List<Double[]> yList2 = new ArrayList<>();
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					fuelEnergyListPlot.stream().mapToDouble(e -> e.doubleValue(MyUnits.KILOWATT_HOUR)).toArray())
					);
			yList2.add(MyArrayUtils.convertFromDoubleToPrimitive(
					batteryEnergyListPlot.stream().mapToDouble(e -> e.doubleValue(MyUnits.HORSEPOWER_HOUR)).toArray())
					);
			
			List<String> legend = new ArrayList<>();
			legend.add("Fuel Energy");
			legend.add("Battery Energy");
			
			try {
				MyChartToFileUtils.plot(
						xList, yList1, 
						"Energy Profile", "Time", "Energies", 
						0.0, null, null, null, 
						"hr", "kW*hr", 
						true, legend, 
						_missionProfilesFolderPath, "Energy_profile_SI", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						xList, yList2, 
						"Energy Profile", "Time", "Energies", 
						0.0, null, null, null, 
						"hr", "hp*hr", 
						true, legend, 
						_missionProfilesFolderPath, "Power_profile_IMPERIAL", 
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		
		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\t\tMission distance = " + missionRange.to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tTotal mission distance (plus alternate) = " + totalRange.to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tBlock time = " + blockTime + "\n")
				.append("\t\tTotal mission duration = " + totalTime + "\n")
				.append("\t\tAircraft mass at mission start = " + initialMissionMass + "\n")
				.append("\t\tAircraft mass at mission end = " + endMissionMass + "\n")
				.append("\t\tInitial fuel mass for the assigned mission = " + initialFuelMass + "\n")
				.append("\t\tBlock fuel = " + blockFuel + "\n")
				.append("\t\tTotal fuel = " + totalFuel + "\n")
				.append("\t\tFuel reserve = " + fuelReserve*100 + " %\n")
				.append("\t\tTotal Fuel Power = " + totalFuelPower + "\n")
				.append("\t\tTotal Battery Power = " + totalBatteryPower + "\n")
				.append("\t\tTotal Fuel Energy = " + totalFuelEnergy.doubleValue(MyUnits.KILOWATT_HOUR) + " kW*h\n")
				.append("\t\tTotal Battery Energy = " + totalBatteryEnergy.doubleValue(MyUnits.KILOWATT_HOUR) + " kW*h\n")
				.append("\t\tDesign passengers number = " + theAircraft.getCabinConfiguration().getDesignPassengerNumber() + "\n")
				.append("\t\tPassengers number for this mission = " + deisgnPassengersNumber + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTAKE-OFF\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
				.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
		for(int i=0; i<timeMap.get(MissionPhasesEnum.TAKE_OFF).size(); i++)
			sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.MINUTE))  
					+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.NAUTICAL_MILE))
					+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.FOOT))
					+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.KILOGRAM))
					+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.KILOGRAM))
					+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.GRAM))
					+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.KNOT))
					+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.KNOT))
					+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.POUND_FORCE))
					+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.POUND_FORCE))
					+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.POUND_FORCE))
					+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.POUND_FORCE))
					+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i))
					+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
					+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.RADIAN)*100.0)
					+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(NonSI.DEGREE_ANGLE))
					+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.KILO(SI.WATT)))
					+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(SI.KILO(SI.WATT)))
					+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
					+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.TAKE_OFF).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
					+ "\n");
		sb.append("\t\t.....................................\n")
		.append("\t\tCLIMB\n")
		.append("\t\t.....................................\n")
		.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
		.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
		for(int i=0; i<timeMap.get(MissionPhasesEnum.CLIMB).size(); i++)
			sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.MINUTE))  
			+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.NAUTICAL_MILE))
			+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.FOOT))
			+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.CLIMB).get(i))
			+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.RADIAN)*100.0)
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(NonSI.DEGREE_ANGLE))
			+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.CLIMB).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\n");
		sb.append("\t\t.....................................\n")
		.append("\t\tCRUISE\n")
		.append("\t\t.....................................\n")
		.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
		.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
		for(int i=0; i<timeMap.get(MissionPhasesEnum.CRUISE).size(); i++)
			sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.MINUTE))  
			+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.NAUTICAL_MILE))
			+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.FOOT))
			+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.CRUISE).get(i))
			+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.RADIAN)*100.0)
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(NonSI.DEGREE_ANGLE))
			+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.CRUISE).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\n");
		sb.append("\t\t.....................................\n")
		.append("\t\tFIRST DESCENT\n")
		.append("\t\t.....................................\n")
		.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
		.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
		for(int i=0; i<timeMap.get(MissionPhasesEnum.FIRST_DESCENT).size(); i++)
			sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.MINUTE))  
			+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.NAUTICAL_MILE))
			+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.FOOT))
			+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i))
			+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.RADIAN)*100.0)
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(NonSI.DEGREE_ANGLE))
			+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.FIRST_DESCENT).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\n");

		if(alternateCruiseAltitude.doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
			sb.append("\t\t.....................................\n")
			.append("\t\tSECOND CLIMB\n")
			.append("\t\t.....................................\n")
			.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
			.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
			for(int i=0; i<timeMap.get(MissionPhasesEnum.SECOND_CLIMB).size(); i++)
				sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.MINUTE))  
				+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.NAUTICAL_MILE))
				+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.FOOT))
				+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i))
				+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.RADIAN)*100.0)
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(NonSI.DEGREE_ANGLE))
				+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.SECOND_CLIMB).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\n");
			sb.append("\t\t.....................................\n")
			.append("\t\tALTERNATE CRUISE\n")
			.append("\t\t.....................................\n")
			.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
			.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
			for(int i=0; i<timeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).size(); i++)
				sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.MINUTE))  
				+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.NAUTICAL_MILE))
				+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.FOOT))
				+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i))
				+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.RADIAN)*100.0)
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(NonSI.DEGREE_ANGLE))
				+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.ALTERNATE_CRUISE).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\n");
			sb.append("\t\t.....................................\n")
			.append("\t\tSECOND DESCENT\n")
			.append("\t\t.....................................\n")
			.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
			.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
			for(int i=0; i<timeMap.get(MissionPhasesEnum.SECOND_DESCENT).size(); i++)
				sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.MINUTE))  
				+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.NAUTICAL_MILE))
				+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.FOOT))
				+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i))
				+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.RADIAN)*100.0)
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(NonSI.DEGREE_ANGLE))
				+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.SECOND_DESCENT).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\n");
		}
		if(holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
			sb.append("\t\t.....................................\n")
			.append("\t\tHOLDING\n")
			.append("\t\t.....................................\n")
			.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
			.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
			for(int i=0; i<timeMap.get(MissionPhasesEnum.HOLDING).size(); i++)
				sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.MINUTE))  
				+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.NAUTICAL_MILE))
				+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.FOOT))
				+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.KILOGRAM))
				+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.GRAM))
				+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.KNOT))
				+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.POUND_FORCE))
				+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.HOLDING).get(i))
				+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.RADIAN)*100.0)
				+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(NonSI.DEGREE_ANGLE))
				+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(SI.KILO(SI.WATT)))
				+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.HOLDING).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
				+ "\n");
		}

		sb.append("\t\t.....................................\n")
		.append("\t\tLANDING\n")
		.append("\t\t.....................................\n")
		.append("\t\t\tTime\tRange\tAltitude\tFuel\tMass\tNOx\tCO\tHC\tSoot\tCO2\tSOx\tH2O\tTAS\tCAS\tMach\tCL\tCD\tE\tDrag\tTot. Thrust\tThermic Thrust\tElectric Thrust\tFuel Flow\tSFC\tRate of Climb\tClimb Gradient\tClimb Angle\tFuel Power\tBattery Power\tFuel Energy\tBattery Energy\n")
		.append("\t\t\tmin\tnm\tft\tkg\tkg\tg\tg\tg\tg\tg\tg\tg\tkts\tkts\t\t\t\t\tlbf\tlbf\tlbf\tlbf\tkg/min\tlb/lb*hr\tft/min\t%\tdeg\tkW\tkW\tkW*h\tkW*h\n");
		for(int i=0; i<timeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).size(); i++)
			sb.append("\t\t\t" + numberFormat.format(timeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.MINUTE))  
			+ "\t" + numberFormat.format(rangeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.NAUTICAL_MILE))
			+ "\t" + numberFormat.format(altitudeMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.FOOT))
			+ "\t" + numberFormat.format(fuelUsedMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(massMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.KILOGRAM))
			+ "\t" + numberFormat.format(emissionNOxMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCOMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionHCMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSootMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionCO2Map.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionSOxMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(emissionH2OMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.GRAM))
			+ "\t" + numberFormat.format(speedTASMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(speedCASMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.KNOT))
			+ "\t" + numberFormat.format(machMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(liftingCoefficientMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(dragCoefficientMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(efficiencyMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(dragMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(totalThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(thermicThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(electricThrustMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.POUND_FORCE))
			+ "\t" + numberFormat.format(fuelFlowMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(sfcMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i))
			+ "\t" + numberFormat.format(rateOfClimbMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.RADIAN)*100.0)
			+ "\t" + numberFormat.format(climbAngleMissionMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(NonSI.DEGREE_ANGLE))
			+ "\t" + numberFormat.format(fuelPowerMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(batteryPowerMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(SI.KILO(SI.WATT)))
			+ "\t" + numberFormat.format(fuelEnergyMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\t" + numberFormat.format(batteryEnergyMap.get(MissionPhasesEnum.APPROACH_AND_LANDING).get(i).doubleValue(MyUnits.KILOWATT_HOUR))
			+ "\n");
		
		sb.append("\t-------------------------------------\n")
		;

		return sb.toString();

	}

	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public Aircraft getTheAircraft() {
		return theAircraft;
	}

	public void setTheAircraft(Aircraft theAircraft) {
		this.theAircraft = theAircraft;
	}

	public Amount<Length> getMissionRange() {
		return missionRange;
	}

	public void setMissionRange(Amount<Length> missionRange) {
		this.missionRange = missionRange;
	}

	public Amount<Length> getAlternateCruiseRange() {
		return alternateCruiseRange;
	}

	public void setAlternateCruiseRange(Amount<Length> alternateCruiseRange) {
		this.alternateCruiseRange = alternateCruiseRange;
	}

	public Amount<Duration> getHoldingDuration() {
		return holdingDuration;
	}

	public void setHoldingDuration(Amount<Duration> holdingDuration) {
		this.holdingDuration = holdingDuration;
	}

	public double getFuelReserve() {
		return fuelReserve;
	}

	public void setFuelReserve(double fuelReserve) {
		this.fuelReserve = fuelReserve;
	}

	public Amount<Mass> getFirstGuessInitialFuelMass() {
		return firstGuessInitialFuelMass;
	}

	public void setFirstGuessInitialFuelMass(Amount<Mass> firstGuessInitialFuelMass) {
		this.firstGuessInitialFuelMass = firstGuessInitialFuelMass;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
		this.maximumTakeOffMass = maximumTakeOffMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> operatingEmptyMass) {
		this.operatingEmptyMass = operatingEmptyMass;
	}

	public Amount<Mass> getSinglePassengerMass() {
		return singlePassengerMass;
	}

	public void setSinglePassengerMass(Amount<Mass> singlePassengerMass) {
		this.singlePassengerMass = singlePassengerMass;
	}

	public int getDeisngPassengersNumber() {
		return deisgnPassengersNumber;
	}

	public void setDeisngPassengersNumber(int deisngPassengersNumber) {
		this.deisgnPassengersNumber = deisngPassengersNumber;
	}

	public double getcLmaxClean() {
		return cLmaxClean;
	}

	public void setcLmaxClean(double cLmaxClean) {
		this.cLmaxClean = cLmaxClean;
	}

	public double getcLmaxTakeOff() {
		return cLmaxTakeOff;
	}

	public void setcLmaxTakeOff(double cLmaxTakeOff) {
		this.cLmaxTakeOff = cLmaxTakeOff;
	}

	public Amount<?> getcLAlphaTakeOff() {
		return cLAlphaTakeOff;
	}

	public void setcLAlphaTakeOff(Amount<?> cLAlphaTakeOff) {
		this.cLAlphaTakeOff = cLAlphaTakeOff;
	}

	public double getcLZeroTakeOff() {
		return cLZeroTakeOff;
	}

	public void setcLZeroTakeOff(double cLZeroTakeOff) {
		this.cLZeroTakeOff = cLZeroTakeOff;
	}

	public double getcLmaxLanding() {
		return cLmaxLanding;
	}

	public void setcLmaxLanding(double cLmaxLanding) {
		this.cLmaxLanding = cLmaxLanding;
	}

	public Amount<?> getcLAlphaLanding() {
		return cLAlphaLanding;
	}

	public void setcLAlphaLanding(Amount<?> cLAlphaLanding) {
		this.cLAlphaLanding = cLAlphaLanding;
	}

	public double getcLZeroLanding() {
		return cLZeroLanding;
	}

	public void setcLZeroLanding(double cLZeroLanding) {
		this.cLZeroLanding = cLZeroLanding;
	}

	public double[] getPolarCLTakeOff() {
		return polarCLTakeOff;
	}

	public void setPolarCLTakeOff(double[] polarCLTakeOff) {
		this.polarCLTakeOff = polarCLTakeOff;
	}

	public double[] getPolarCDTakeOff() {
		return polarCDTakeOff;
	}

	public void setPolarCDTakeOff(double[] polarCDTakeOff) {
		this.polarCDTakeOff = polarCDTakeOff;
	}

	public double[] getPolarCLClimb() {
		return polarCLClimb;
	}

	public void setPolarCLClimb(double[] polarCLClimb) {
		this.polarCLClimb = polarCLClimb;
	}

	public double[] getPolarCDClimb() {
		return polarCDClimb;
	}

	public void setPolarCDClimb(double[] polarCDClimb) {
		this.polarCDClimb = polarCDClimb;
	}

	public double[] getPolarCLCruise() {
		return polarCLCruise;
	}

	public void setPolarCLCruise(double[] polarCLCruise) {
		this.polarCLCruise = polarCLCruise;
	}

	public double[] getPolarCDCruise() {
		return polarCDCruise;
	}

	public void setPolarCDCruise(double[] polarCDCruise) {
		this.polarCDCruise = polarCDCruise;
	}

	public double[] getPolarCLLanding() {
		return polarCLLanding;
	}

	public void setPolarCLLanding(double[] polarCLLanding) {
		this.polarCLLanding = polarCLLanding;
	}

	public double[] getPolarCDLanding() {
		return polarCDLanding;
	}

	public void setPolarCDLanding(double[] polarCDLanding) {
		this.polarCDLanding = polarCDLanding;
	}

	public Amount<Velocity> getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(Amount<Velocity> windSpeed) {
		this.windSpeed = windSpeed;
	}

	public MyInterpolatingFunction getMu() {
		return mu;
	}

	public void setMu(MyInterpolatingFunction mu) {
		this.mu = mu;
	}

	public MyInterpolatingFunction getMuBrake() {
		return muBrake;
	}

	public void setMuBrake(MyInterpolatingFunction muBrake) {
		this.muBrake = muBrake;
	}

	public Amount<Duration> getDtHold() {
		return dtHold;
	}

	public void setDtHold(Amount<Duration> dtHold) {
		this.dtHold = dtHold;
	}

	public Amount<Angle> getAlphaGround() {
		return alphaGround;
	}

	public void setAlphaGround(Amount<Angle> alphaGround) {
		this.alphaGround = alphaGround;
	}

	public Amount<Length> getObstacleTakeOff() {
		return obstacleTakeOff;
	}

	public void setObstacleTakeOff(Amount<Length> obstacleTakeOff) {
		this.obstacleTakeOff = obstacleTakeOff;
	}

	public double getkRotation() {
		return kRotation;
	}

	public void setkRotation(double kRotation) {
		this.kRotation = kRotation;
	}

	public double getkCLmaxTakeOff() {
		return kCLmaxTakeOff;
	}

	public void setkCLmaxTakeOff(double kCLmaxTakeOff) {
		this.kCLmaxTakeOff = kCLmaxTakeOff;
	}

	public double getDragDueToEnigneFailure() {
		return dragDueToEnigneFailure;
	}

	public void setDragDueToEnigneFailure(double dragDueToEnigneFailure) {
		this.dragDueToEnigneFailure = dragDueToEnigneFailure;
	}

	public double getkAlphaDot() {
		return kAlphaDot;
	}

	public void setkAlphaDot(double kAlphaDot) {
		this.kAlphaDot = kAlphaDot;
	}

	public double getAlphaDotInitial() {
		return alphaDotInitial;
	}

	public void setAlphaDotInitial(double alphaDotInitial) {
		this.alphaDotInitial = alphaDotInitial;
	}

	public Amount<Length> getObstacleLanding() {
		return obstacleLanding;
	}

	public void setObstacleLanding(Amount<Length> obstacleLanding) {
		this.obstacleLanding = obstacleLanding;
	}

	public Amount<Angle> getApproachAngle() {
		return approachAngle;
	}

	public void setApproachAngle(Amount<Angle> approachAngle) {
		this.approachAngle = approachAngle;
	}

	public double getkCLmaxLanding() {
		return kCLmaxLanding;
	}

	public void setkCLmaxLanding(double kCLmaxLanding) {
		this.kCLmaxLanding = kCLmaxLanding;
	}

	public double getkApproach() {
		return kApproach;
	}

	public void setkApproach(double kApproach) {
		this.kApproach = kApproach;
	}

	public double getkFlare() {
		return kFlare;
	}

	public void setkFlare(double kFlare) {
		this.kFlare = kFlare;
	}

	public double getkTouchDown() {
		return kTouchDown;
	}

	public void setkTouchDown(double kTouchDown) {
		this.kTouchDown = kTouchDown;
	}

	public Amount<Duration> getFreeRollDuration() {
		return freeRollDuration;
	}

	public void setFreeRollDuration(Amount<Duration> freeRollDuration) {
		this.freeRollDuration = freeRollDuration;
	}

	public Amount<Velocity> getClimbSpeed() {
		return climbSpeed;
	}

	public void setClimbSpeed(Amount<Velocity> climbSpeed) {
		this.climbSpeed = climbSpeed;
	}

	public Amount<Velocity> getSpeedDescentCAS() {
		return speedDescentCAS;
	}

	public void setSpeedDescentCAS(Amount<Velocity> speedDescentCAS) {
		this.speedDescentCAS = speedDescentCAS;
	}

	public Amount<Velocity> getRateOfDescent() {
		return rateOfDescent;
	}

	public void setRateOfDescent(Amount<Velocity> rateOfDescent) {
		this.rateOfDescent = rateOfDescent;
	}

	public double getTakeOffCalibrationFactorThrust() {
		return takeOffCalibrationFactorThrust;
	}

	public void setTakeOffCalibrationFactorThrust(double takeOffCalibrationFactorThrust) {
		this.takeOffCalibrationFactorThrust = takeOffCalibrationFactorThrust;
	}

	public double getAprCalibrationFactorThrust() {
		return aprCalibrationFactorThrust;
	}

	public void setAprCalibrationFactorThrust(double aprCalibrationFactorThrust) {
		this.aprCalibrationFactorThrust = aprCalibrationFactorThrust;
	}

	public double getClimbCalibrationFactorThrust() {
		return climbCalibrationFactorThrust;
	}

	public void setClimbCalibrationFactorThrust(double climbCalibrationFactorThrust) {
		this.climbCalibrationFactorThrust = climbCalibrationFactorThrust;
	}

	public double getContinuousCalibrationFactorThrust() {
		return continuousCalibrationFactorThrust;
	}

	public void setContinuousCalibrationFactorThrust(double continuousCalibrationFactorThrust) {
		this.continuousCalibrationFactorThrust = continuousCalibrationFactorThrust;
	}

	public double getCruiseCalibrationFactorThrust() {
		return cruiseCalibrationFactorThrust;
	}

	public void setCruiseCalibrationFactorThrust(double cruiseCalibrationFactorThrust) {
		this.cruiseCalibrationFactorThrust = cruiseCalibrationFactorThrust;
	}

	public double getFlightIdleCalibrationFactorThrust() {
		return flightIdleCalibrationFactorThrust;
	}

	public void setFlightIdleCalibrationFactorThrust(double flightIdleCalibrationFactorThrust) {
		this.flightIdleCalibrationFactorThrust = flightIdleCalibrationFactorThrust;
	}

	public double getGroundIdleCalibrationFactorThrust() {
		return groundIdleCalibrationFactorThrust;
	}

	public void setGroundIdleCalibrationFactorThrust(double groundIdleCalibrationFactorThrust) {
		this.groundIdleCalibrationFactorThrust = groundIdleCalibrationFactorThrust;
	}

	public double getTakeOffCalibrationFactorSFC() {
		return takeOffCalibrationFactorSFC;
	}

	public void setTakeOffCalibrationFactorSFC(double takeOffCalibrationFactorSFC) {
		this.takeOffCalibrationFactorSFC = takeOffCalibrationFactorSFC;
	}

	public double getAprCalibrationFactorSFC() {
		return aprCalibrationFactorSFC;
	}

	public void setAprCalibrationFactorSFC(double aprCalibrationFactorSFC) {
		this.aprCalibrationFactorSFC = aprCalibrationFactorSFC;
	}

	public double getClimbCalibrationFactorSFC() {
		return climbCalibrationFactorSFC;
	}

	public void setClimbCalibrationFactorSFC(double climbCalibrationFactorSFC) {
		this.climbCalibrationFactorSFC = climbCalibrationFactorSFC;
	}

	public double getCruiseCalibrationFactorSFC() {
		return cruiseCalibrationFactorSFC;
	}

	public void setCruiseCalibrationFactorSFC(double cruiseCalibrationFactorSFC) {
		this.cruiseCalibrationFactorSFC = cruiseCalibrationFactorSFC;
	}

	public double getFlightIdleCalibrationFactorSFC() {
		return flightIdleCalibrationFactorSFC;
	}

	public void setFlightIdleCalibrationFactorSFC(double flightIdleCalibrationFactorSFC) {
		this.flightIdleCalibrationFactorSFC = flightIdleCalibrationFactorSFC;
	}

	public double getGroundIdleCalibrationFactorSFC() {
		return groundIdleCalibrationFactorSFC;
	}

	public void setGroundIdleCalibrationFactorSFC(double groundIdleCalibrationFactorSFC) {
		this.groundIdleCalibrationFactorSFC = groundIdleCalibrationFactorSFC;
	}

	public double getTakeOffCalibrationFactorEmissionIndexNOx() {
		return takeOffCalibrationFactorEmissionIndexNOx;
	}

	public void setTakeOffCalibrationFactorEmissionIndexNOx(double takeOffCalibrationFactorEmissionIndexNOx) {
		this.takeOffCalibrationFactorEmissionIndexNOx = takeOffCalibrationFactorEmissionIndexNOx;
	}

	public double getAprCalibrationFactorEmissionIndexNOx() {
		return aprCalibrationFactorEmissionIndexNOx;
	}

	public void setAprCalibrationFactorEmissionIndexNOx(double aprCalibrationFactorEmissionIndexNOx) {
		this.aprCalibrationFactorEmissionIndexNOx = aprCalibrationFactorEmissionIndexNOx;
	}

	public double getClimbCalibrationFactorEmissionIndexNOx() {
		return climbCalibrationFactorEmissionIndexNOx;
	}

	public void setClimbCalibrationFactorEmissionIndexNOx(double climbCalibrationFactorEmissionIndexNOx) {
		this.climbCalibrationFactorEmissionIndexNOx = climbCalibrationFactorEmissionIndexNOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexNOx() {
		return cruiseCalibrationFactorEmissionIndexNOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexNOx(double cruiseCalibrationFactorEmissionIndexNOx) {
		this.cruiseCalibrationFactorEmissionIndexNOx = cruiseCalibrationFactorEmissionIndexNOx;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexNOx() {
		return flightIdleCalibrationFactorEmissionIndexNOx;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexNOx(double flightIdleCalibrationFactorEmissionIndexNOx) {
		this.flightIdleCalibrationFactorEmissionIndexNOx = flightIdleCalibrationFactorEmissionIndexNOx;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexNOx() {
		return groundIdleCalibrationFactorEmissionIndexNOx;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexNOx(double groundIdleCalibrationFactorEmissionIndexNOx) {
		this.groundIdleCalibrationFactorEmissionIndexNOx = groundIdleCalibrationFactorEmissionIndexNOx;
	}

	public double getTakeOffCalibrationFactorEmissionIndexCO() {
		return takeOffCalibrationFactorEmissionIndexCO;
	}

	public void setTakeOffCalibrationFactorEmissionIndexCO(double takeOffCalibrationFactorEmissionIndexCO) {
		this.takeOffCalibrationFactorEmissionIndexCO = takeOffCalibrationFactorEmissionIndexCO;
	}

	public double getAprCalibrationFactorEmissionIndexCO() {
		return aprCalibrationFactorEmissionIndexCO;
	}

	public void setAprCalibrationFactorEmissionIndexCO(double aprCalibrationFactorEmissionIndexCO) {
		this.aprCalibrationFactorEmissionIndexCO = aprCalibrationFactorEmissionIndexCO;
	}

	public double getClimbCalibrationFactorEmissionIndexCO() {
		return climbCalibrationFactorEmissionIndexCO;
	}

	public void setClimbCalibrationFactorEmissionIndexCO(double climbCalibrationFactorEmissionIndexCO) {
		this.climbCalibrationFactorEmissionIndexCO = climbCalibrationFactorEmissionIndexCO;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO() {
		return cruiseCalibrationFactorEmissionIndexCO;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO(double cruiseCalibrationFactorEmissionIndexCO) {
		this.cruiseCalibrationFactorEmissionIndexCO = cruiseCalibrationFactorEmissionIndexCO;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexCO() {
		return flightIdleCalibrationFactorEmissionIndexCO;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexCO(double flightIdleCalibrationFactorEmissionIndexCO) {
		this.flightIdleCalibrationFactorEmissionIndexCO = flightIdleCalibrationFactorEmissionIndexCO;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexCO() {
		return groundIdleCalibrationFactorEmissionIndexCO;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexCO(double groundIdleCalibrationFactorEmissionIndexCO) {
		this.groundIdleCalibrationFactorEmissionIndexCO = groundIdleCalibrationFactorEmissionIndexCO;
	}

	public double getTakeOffCalibrationFactorEmissionIndexHC() {
		return takeOffCalibrationFactorEmissionIndexHC;
	}

	public void setTakeOffCalibrationFactorEmissionIndexHC(double takeOffCalibrationFactorEmissionIndexHC) {
		this.takeOffCalibrationFactorEmissionIndexHC = takeOffCalibrationFactorEmissionIndexHC;
	}

	public double getAprCalibrationFactorEmissionIndexHC() {
		return aprCalibrationFactorEmissionIndexHC;
	}

	public void setAprCalibrationFactorEmissionIndexHC(double aprCalibrationFactorEmissionIndexHC) {
		this.aprCalibrationFactorEmissionIndexHC = aprCalibrationFactorEmissionIndexHC;
	}

	public double getClimbCalibrationFactorEmissionIndexHC() {
		return climbCalibrationFactorEmissionIndexHC;
	}

	public void setClimbCalibrationFactorEmissionIndexHC(double climbCalibrationFactorEmissionIndexHC) {
		this.climbCalibrationFactorEmissionIndexHC = climbCalibrationFactorEmissionIndexHC;
	}

	public double getCruiseCalibrationFactorEmissionIndexHC() {
		return cruiseCalibrationFactorEmissionIndexHC;
	}

	public void setCruiseCalibrationFactorEmissionIndexHC(double cruiseCalibrationFactorEmissionIndexHC) {
		this.cruiseCalibrationFactorEmissionIndexHC = cruiseCalibrationFactorEmissionIndexHC;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexHC() {
		return flightIdleCalibrationFactorEmissionIndexHC;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexHC(double flightIdleCalibrationFactorEmissionIndexHC) {
		this.flightIdleCalibrationFactorEmissionIndexHC = flightIdleCalibrationFactorEmissionIndexHC;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexHC() {
		return groundIdleCalibrationFactorEmissionIndexHC;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexHC(double groundIdleCalibrationFactorEmissionIndexHC) {
		this.groundIdleCalibrationFactorEmissionIndexHC = groundIdleCalibrationFactorEmissionIndexHC;
	}

	public double getTakeOffCalibrationFactorEmissionIndexSoot() {
		return takeOffCalibrationFactorEmissionIndexSoot;
	}

	public void setTakeOffCalibrationFactorEmissionIndexSoot(double takeOffCalibrationFactorEmissionIndexSoot) {
		this.takeOffCalibrationFactorEmissionIndexSoot = takeOffCalibrationFactorEmissionIndexSoot;
	}

	public double getAprCalibrationFactorEmissionIndexSoot() {
		return aprCalibrationFactorEmissionIndexSoot;
	}

	public void setAprCalibrationFactorEmissionIndexSoot(double aprCalibrationFactorEmissionIndexSoot) {
		this.aprCalibrationFactorEmissionIndexSoot = aprCalibrationFactorEmissionIndexSoot;
	}

	public double getClimbCalibrationFactorEmissionIndexSoot() {
		return climbCalibrationFactorEmissionIndexSoot;
	}

	public void setClimbCalibrationFactorEmissionIndexSoot(double climbCalibrationFactorEmissionIndexSoot) {
		this.climbCalibrationFactorEmissionIndexSoot = climbCalibrationFactorEmissionIndexSoot;
	}

	public double getCruiseCalibrationFactorEmissionIndexSoot() {
		return cruiseCalibrationFactorEmissionIndexSoot;
	}

	public void setCruiseCalibrationFactorEmissionIndexSoot(double cruiseCalibrationFactorEmissionIndexSoot) {
		this.cruiseCalibrationFactorEmissionIndexSoot = cruiseCalibrationFactorEmissionIndexSoot;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexSoot() {
		return flightIdleCalibrationFactorEmissionIndexSoot;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexSoot(double flightIdleCalibrationFactorEmissionIndexSoot) {
		this.flightIdleCalibrationFactorEmissionIndexSoot = flightIdleCalibrationFactorEmissionIndexSoot;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexSoot() {
		return groundIdleCalibrationFactorEmissionIndexSoot;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexSoot(double groundIdleCalibrationFactorEmissionIndexSoot) {
		this.groundIdleCalibrationFactorEmissionIndexSoot = groundIdleCalibrationFactorEmissionIndexSoot;
	}

	public double getTakeOffCalibrationFactorEmissionIndexCO2() {
		return takeOffCalibrationFactorEmissionIndexCO2;
	}

	public void setTakeOffCalibrationFactorEmissionIndexCO2(double takeOffCalibrationFactorEmissionIndexCO2) {
		this.takeOffCalibrationFactorEmissionIndexCO2 = takeOffCalibrationFactorEmissionIndexCO2;
	}

	public double getAprCalibrationFactorEmissionIndexCO2() {
		return aprCalibrationFactorEmissionIndexCO2;
	}

	public void setAprCalibrationFactorEmissionIndexCO2(double aprCalibrationFactorEmissionIndexCO2) {
		this.aprCalibrationFactorEmissionIndexCO2 = aprCalibrationFactorEmissionIndexCO2;
	}

	public double getClimbCalibrationFactorEmissionIndexCO2() {
		return climbCalibrationFactorEmissionIndexCO2;
	}

	public void setClimbCalibrationFactorEmissionIndexCO2(double climbCalibrationFactorEmissionIndexCO2) {
		this.climbCalibrationFactorEmissionIndexCO2 = climbCalibrationFactorEmissionIndexCO2;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO2() {
		return cruiseCalibrationFactorEmissionIndexCO2;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO2(double cruiseCalibrationFactorEmissionIndexCO2) {
		this.cruiseCalibrationFactorEmissionIndexCO2 = cruiseCalibrationFactorEmissionIndexCO2;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexCO2() {
		return flightIdleCalibrationFactorEmissionIndexCO2;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexCO2(double flightIdleCalibrationFactorEmissionIndexCO2) {
		this.flightIdleCalibrationFactorEmissionIndexCO2 = flightIdleCalibrationFactorEmissionIndexCO2;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexCO2() {
		return groundIdleCalibrationFactorEmissionIndexCO2;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexCO2(double groundIdleCalibrationFactorEmissionIndexCO2) {
		this.groundIdleCalibrationFactorEmissionIndexCO2 = groundIdleCalibrationFactorEmissionIndexCO2;
	}

	public double getTakeOffCalibrationFactorEmissionIndexSOx() {
		return takeOffCalibrationFactorEmissionIndexSOx;
	}

	public void setTakeOffCalibrationFactorEmissionIndexSOx(double takeOffCalibrationFactorEmissionIndexSOx) {
		this.takeOffCalibrationFactorEmissionIndexSOx = takeOffCalibrationFactorEmissionIndexSOx;
	}

	public double getAprCalibrationFactorEmissionIndexSOx() {
		return aprCalibrationFactorEmissionIndexSOx;
	}

	public void setAprCalibrationFactorEmissionIndexSOx(double aprCalibrationFactorEmissionIndexSOx) {
		this.aprCalibrationFactorEmissionIndexSOx = aprCalibrationFactorEmissionIndexSOx;
	}

	public double getClimbCalibrationFactorEmissionIndexSOx() {
		return climbCalibrationFactorEmissionIndexSOx;
	}

	public void setClimbCalibrationFactorEmissionIndexSOx(double climbCalibrationFactorEmissionIndexSOx) {
		this.climbCalibrationFactorEmissionIndexSOx = climbCalibrationFactorEmissionIndexSOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexSOx() {
		return cruiseCalibrationFactorEmissionIndexSOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexSOx(double cruiseCalibrationFactorEmissionIndexSOx) {
		this.cruiseCalibrationFactorEmissionIndexSOx = cruiseCalibrationFactorEmissionIndexSOx;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexSOx() {
		return flightIdleCalibrationFactorEmissionIndexSOx;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexSOx(double flightIdleCalibrationFactorEmissionIndexSOx) {
		this.flightIdleCalibrationFactorEmissionIndexSOx = flightIdleCalibrationFactorEmissionIndexSOx;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexSOx() {
		return groundIdleCalibrationFactorEmissionIndexSOx;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexSOx(double groundIdleCalibrationFactorEmissionIndexSOx) {
		this.groundIdleCalibrationFactorEmissionIndexSOx = groundIdleCalibrationFactorEmissionIndexSOx;
	}

	public double getTakeOffCalibrationFactorEmissionIndexH2O() {
		return takeOffCalibrationFactorEmissionIndexH2O;
	}

	public void setTakeOffCalibrationFactorEmissionIndexH2O(double takeOffCalibrationFactorEmissionIndexH2O) {
		this.takeOffCalibrationFactorEmissionIndexH2O = takeOffCalibrationFactorEmissionIndexH2O;
	}

	public double getAprCalibrationFactorEmissionIndexH2O() {
		return aprCalibrationFactorEmissionIndexH2O;
	}

	public void setAprCalibrationFactorEmissionIndexH2O(double aprCalibrationFactorEmissionIndexH2O) {
		this.aprCalibrationFactorEmissionIndexH2O = aprCalibrationFactorEmissionIndexH2O;
	}

	public double getClimbCalibrationFactorEmissionIndexH2O() {
		return climbCalibrationFactorEmissionIndexH2O;
	}

	public void setClimbCalibrationFactorEmissionIndexH2O(double climbCalibrationFactorEmissionIndexH2O) {
		this.climbCalibrationFactorEmissionIndexH2O = climbCalibrationFactorEmissionIndexH2O;
	}

	public double getCruiseCalibrationFactorEmissionIndexH2O() {
		return cruiseCalibrationFactorEmissionIndexH2O;
	}

	public void setCruiseCalibrationFactorEmissionIndexH2O(double cruiseCalibrationFactorEmissionIndexH2O) {
		this.cruiseCalibrationFactorEmissionIndexH2O = cruiseCalibrationFactorEmissionIndexH2O;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexH2O() {
		return flightIdleCalibrationFactorEmissionIndexH2O;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexH2O(double flightIdleCalibrationFactorEmissionIndexH2O) {
		this.flightIdleCalibrationFactorEmissionIndexH2O = flightIdleCalibrationFactorEmissionIndexH2O;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexH2O() {
		return groundIdleCalibrationFactorEmissionIndexH2O;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexH2O(double groundIdleCalibrationFactorEmissionIndexH2O) {
		this.groundIdleCalibrationFactorEmissionIndexH2O = groundIdleCalibrationFactorEmissionIndexH2O;
	}

	public Boolean getMissionProfileStopped() {
		return missionProfileStopped;
	}

	public void setMissionProfileStopped(Boolean missionProfileStopped) {
		this.missionProfileStopped = missionProfileStopped;
	}

	public Map<MissionPhasesEnum, List<Amount<Length>>> getRangeMap() {
		return rangeMap;
	}

	public void setRangeMap(Map<MissionPhasesEnum, List<Amount<Length>>> rangeMap) {
		this.rangeMap = rangeMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Length>>> getAltitudeMap() {
		return altitudeMap;
	}

	public void setAltitudeMap(Map<MissionPhasesEnum, List<Amount<Length>>> altitudeMap) {
		this.altitudeMap = altitudeMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Duration>>> getTimeMap() {
		return timeMap;
	}

	public void setTimeMap(Map<MissionPhasesEnum, List<Amount<Duration>>> timeMap) {
		this.timeMap = timeMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getFuelUsedMap() {
		return fuelUsedMap;
	}

	public void setFuelUsedMap(Map<MissionPhasesEnum, List<Amount<Mass>>> fuelUsedMap) {
		this.fuelUsedMap = fuelUsedMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionNOxMap() {
		return emissionNOxMap;
	}

	public void setEmissionNOxMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionNOxMap) {
		this.emissionNOxMap = emissionNOxMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionCOMap() {
		return emissionCOMap;
	}

	public void setEmissionCOMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionCOMap) {
		this.emissionCOMap = emissionCOMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionHCMap() {
		return emissionHCMap;
	}

	public void setEmissionHCMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionHCMap) {
		this.emissionHCMap = emissionHCMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionSootMap() {
		return emissionSootMap;
	}

	public void setEmissionSootMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionSootMap) {
		this.emissionSootMap = emissionSootMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionCO2Map() {
		return emissionCO2Map;
	}

	public void setEmissionCO2Map(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionCO2Map) {
		this.emissionCO2Map = emissionCO2Map;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionSOxMap() {
		return emissionSOxMap;
	}

	public void setEmissionSOxMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionSOxMap) {
		this.emissionSOxMap = emissionSOxMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getEmissionH2OMap() {
		return emissionH2OMap;
	}

	public void setEmissionH2OMap(Map<MissionPhasesEnum, List<Amount<Mass>>> emissionH2OMap) {
		this.emissionH2OMap = emissionH2OMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Mass>>> getMassMap() {
		return massMap;
	}

	public void setMassMap(Map<MissionPhasesEnum, List<Amount<Mass>>> massMap) {
		this.massMap = massMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Velocity>>> getSpeedCASMissionMap() {
		return speedCASMissionMap;
	}

	public void setSpeedCASMissionMap(Map<MissionPhasesEnum, List<Amount<Velocity>>> speedCASMissionMap) {
		this.speedCASMissionMap = speedCASMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Velocity>>> getSpeedTASMissionMap() {
		return speedTASMissionMap;
	}

	public void setSpeedTASMissionMap(Map<MissionPhasesEnum, List<Amount<Velocity>>> speedTASMissionMap) {
		this.speedTASMissionMap = speedTASMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getMachMissionMap() {
		return machMissionMap;
	}

	public void setMachMissionMap(Map<MissionPhasesEnum, List<Double>> machMissionMap) {
		this.machMissionMap = machMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getLiftingCoefficientMissionMap() {
		return liftingCoefficientMissionMap;
	}

	public void setLiftingCoefficientMissionMap(Map<MissionPhasesEnum, List<Double>> liftingCoefficientMissionMap) {
		this.liftingCoefficientMissionMap = liftingCoefficientMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getDragCoefficientMissionMap() {
		return dragCoefficientMissionMap;
	}

	public void setDragCoefficientMissionMap(Map<MissionPhasesEnum, List<Double>> dragCoefficientMissionMap) {
		this.dragCoefficientMissionMap = dragCoefficientMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getEfficiencyMissionMap() {
		return efficiencyMissionMap;
	}

	public void setEfficiencyMissionMap(Map<MissionPhasesEnum, List<Double>> efficiencyMissionMap) {
		this.efficiencyMissionMap = efficiencyMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Force>>> getDragMissionMap() {
		return dragMissionMap;
	}

	public void setDragMissionMap(Map<MissionPhasesEnum, List<Amount<Force>>> dragMissionMap) {
		this.dragMissionMap = dragMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Force>>> getTotalThrustMissionMap() {
		return totalThrustMissionMap;
	}

	public void setTotalThrustMissionMap(Map<MissionPhasesEnum, List<Amount<Force>>> totalThrustMissionMap) {
		this.totalThrustMissionMap = totalThrustMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getSfcMissionMap() {
		return sfcMissionMap;
	}

	public void setSfcMissionMap(Map<MissionPhasesEnum, List<Double>> sfcMissionMap) {
		this.sfcMissionMap = sfcMissionMap;
	}

	public Map<MissionPhasesEnum, List<Double>> getFuelFlowMissionMap() {
		return fuelFlowMissionMap;
	}

	public void setFuelFlowMissionMap(Map<MissionPhasesEnum, List<Double>> fuelFlowMissionMap) {
		this.fuelFlowMissionMap = fuelFlowMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Velocity>>> getRateOfClimbMissionMap() {
		return rateOfClimbMissionMap;
	}

	public void setRateOfClimbMissionMap(Map<MissionPhasesEnum, List<Amount<Velocity>>> rateOfClimbMissionMap) {
		this.rateOfClimbMissionMap = rateOfClimbMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Angle>>> getClimbAngleMissionMap() {
		return climbAngleMissionMap;
	}

	public void setClimbAngleMissionMap(Map<MissionPhasesEnum, List<Amount<Angle>>> climbAngleMissionMap) {
		this.climbAngleMissionMap = climbAngleMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Power>>> getFuelPowerMap() {
		return fuelPowerMap;
	}

	public void setFuelPowerMap(Map<MissionPhasesEnum, List<Amount<Power>>> fuelPowerMap) {
		this.fuelPowerMap = fuelPowerMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Power>>> getBatteryPowerMap() {
		return batteryPowerMap;
	}

	public void setBatteryPowerMap(Map<MissionPhasesEnum, List<Amount<Power>>> batteryPowerMap) {
		this.batteryPowerMap = batteryPowerMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Energy>>> getFuelEnergyMap() {
		return fuelEnergyMap;
	}

	public void setFuelEnergyMap(Map<MissionPhasesEnum, List<Amount<Energy>>> fuelEnergyMap) {
		this.fuelEnergyMap = fuelEnergyMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Energy>>> getBatteryEnergyMap() {
		return batteryEnergyMap;
	}

	public void setBatteryEnergyMap(Map<MissionPhasesEnum, List<Amount<Energy>>> batteryEnergyMap) {
		this.batteryEnergyMap = batteryEnergyMap;
	}

	public Amount<Mass> getInitialFuelMass() {
		return initialFuelMass;
	}

	public void setInitialFuelMass(Amount<Mass> initialFuelMass) {
		this.initialFuelMass = initialFuelMass;
	}

	public Amount<Mass> getInitialMissionMass() {
		return initialMissionMass;
	}

	public void setInitialMissionMass(Amount<Mass> initialMissionMass) {
		this.initialMissionMass = initialMissionMass;
	}

	public Amount<Mass> getEndMissionMass() {
		return endMissionMass;
	}

	public void setEndMissionMass(Amount<Mass> endMissionMass) {
		this.endMissionMass = endMissionMass;
	}

	public Amount<Mass> getTotalFuel() {
		return totalFuel;
	}

	public void setTotalFuel(Amount<Mass> totalFuel) {
		this.totalFuel = totalFuel;
	}

	public Amount<Mass> getBlockFuel() {
		return blockFuel;
	}

	public void setBlockFuel(Amount<Mass> blockFuel) {
		this.blockFuel = blockFuel;
	}

	public Amount<Duration> getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Amount<Duration> totalTime) {
		this.totalTime = totalTime;
	}

	public Amount<Duration> getBlockTime() {
		return blockTime;
	}

	public void setBlockTime(Amount<Duration> blockTime) {
		this.blockTime = blockTime;
	}

	public Amount<Length> getTotalRange() {
		return totalRange;
	}

	public void setTotalRange(Amount<Length> totalRange) {
		this.totalRange = totalRange;
	}

	public Amount<Power> getTotalFuelPower() {
		return totalFuelPower;
	}

	public void setTotalFuelPower(Amount<Power> totalFuelPower) {
		this.totalFuelPower = totalFuelPower;
	}

	public Amount<Power> getTotalBatteryPower() {
		return totalBatteryPower;
	}

	public void setTotalBatteryPower(Amount<Power> totalBatteryPower) {
		this.totalBatteryPower = totalBatteryPower;
	}

	public Amount<Energy> getTotalFuelEnergy() {
		return totalFuelEnergy;
	}

	public void setTotalFuelEnergy(Amount<Energy> totalFuelEnergy) {
		this.totalFuelEnergy = totalFuelEnergy;
	}

	public Amount<Energy> getTotalBatteryEnergy() {
		return totalBatteryEnergy;
	}

	public void setTotalBatteryEnergy(Amount<Energy> totalBatteryEnergy) {
		this.totalBatteryEnergy = totalBatteryEnergy;
	}

	public Map<MissionPhasesEnum, List<Amount<Force>>> getThermicThrustMissionMap() {
		return thermicThrustMissionMap;
	}

	public void setThermicThrustMissionMap(Map<MissionPhasesEnum, List<Amount<Force>>> thermicThrustMissionMap) {
		this.thermicThrustMissionMap = thermicThrustMissionMap;
	}

	public Map<MissionPhasesEnum, List<Amount<Force>>> getElectricThrustMissionMap() {
		return electricThrustMissionMap;
	}

	public void setElectricThrustMissionMap(Map<MissionPhasesEnum, List<Amount<Force>>> electricThrustMissionMap) {
		this.electricThrustMissionMap = electricThrustMissionMap;
	}
	
}
