package calculators.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
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
import database.databasefunctions.engine.EngineDatabaseManager;
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
	private int deisngPassengersNumber;
	
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
	private Map<MissionPhasesEnum, List<Double>> throttleMissionMap;
	private Map<MissionPhasesEnum, List<Double>> sfcMissionMap;
	private Map<MissionPhasesEnum, List<Double>> fuelFlowMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Velocity>>> rateOfClimbMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Angle>>> climbAngleMissionMap;
	private Map<MissionPhasesEnum, List<Amount<Power>>> fuelPowerMap;
	private Map<MissionPhasesEnum, List<Amount<Power>>> batteryPowerMap;
	private Map<MissionPhasesEnum, List<Amount<Energy>>> fuelEnergyMap;
	private Map<MissionPhasesEnum, List<Amount<Energy>>> batteryEnergyMap;	
	
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
			Amount<Duration> holdingDuration,
			double fuelReserve,
			Amount<Mass> firstGuessInitialFuelMass,
			Amount<Mass> maximumTakeOffMass,
			Amount<Mass> operatingEmptyMass, 
			Amount<Mass> singlePassengerMass, 
			int deisngPassengersNumber,
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
		this.missionRange = missionRange;
		this.alternateCruiseRange = alternateCruiseRange;
		this.holdingDuration = holdingDuration;
		this.fuelReserve = fuelReserve;
		this.firstGuessInitialFuelMass = firstGuessInitialFuelMass;
		this.maximumTakeOffMass = maximumTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.singlePassengerMass = singlePassengerMass;
		this.deisngPassengersNumber = deisngPassengersNumber;
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
		
		this.rangeMap = new HashMap<>();
		this.altitudeMap = new HashMap<>();
		this.timeMap = new HashMap<>();
		this.fuelUsedMap = new HashMap<>();
		this.massMap = new HashMap<>();
		this.emissionNOxMap = new HashMap<>();
		this.emissionCOMap = new HashMap<>();
		this.emissionHCMap = new HashMap<>();
		this.emissionSootMap = new HashMap<>();
		this.emissionCO2Map = new HashMap<>();
		this.emissionSOxMap = new HashMap<>();
		this.emissionH2OMap = new HashMap<>();
		this.speedCASMissionMap = new HashMap<>();
		this.speedTASMissionMap = new HashMap<>();
		this.machMissionMap = new HashMap<>();
		this.liftingCoefficientMissionMap = new HashMap<>();
		this.dragCoefficientMissionMap = new HashMap<>();
		this.efficiencyMissionMap = new HashMap<>();
		this.dragMissionMap = new HashMap<>();
		this.totalThrustMissionMap = new HashMap<>();
		this.thermicThrustMissionMap = new HashMap<>();
		this.electricThrustMissionMap = new HashMap<>();
		this.throttleMissionMap = new HashMap<>();
		this.sfcMissionMap = new HashMap<>();
		this.fuelFlowMissionMap = new HashMap<>();
		this.rateOfClimbMissionMap = new HashMap<>();
		this.climbAngleMissionMap = new HashMap<>();
		this.fuelPowerMap = new HashMap<>();
		this.batteryPowerMap = new HashMap<>();
		this.fuelEnergyMap = new HashMap<>();
		this.batteryEnergyMap = new HashMap<>();	
		
	}

	//--------------------------------------------------------------------------------------------
	// METHODS:

	public void calculateProfiles(Amount<Velocity> vMC) {

		initialMissionMass = operatingEmptyMass
				.plus(singlePassengerMass.times(deisngPassengersNumber))
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
		// QUANTITES TO BE ADDED IN MAPS PER PHASE AT THE END OF THE ITERATION
		//----------------------------------------------------------------------
		// TAKE-OFF
		List<Amount<Length>> rangeTakeOff = new ArrayList<>();
		List<Amount<Length>> altitudeTakeOff = new ArrayList<>();
		List<Amount<Duration>> timeTakeOff = new ArrayList<>();
		List<Amount<Mass>> fuelUsedTakeOff = new ArrayList<>();
		List<Amount<Mass>> aircraftMassTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionNOxTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionCOTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionHCTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionSootTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionCO2TakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionSOxTakeOff = new ArrayList<>();
		List<Amount<Mass>> emissionH2OTakeOff = new ArrayList<>();
		List<Amount<Velocity>> speedTASTakeOff = new ArrayList<>();
		List<Amount<Velocity>> speedCASTakeOff = new ArrayList<>();
		List<Double> machTakeOff = new ArrayList<>();
		List<Double> cLTakeOff = new ArrayList<>();
		List<Double> cDTakeOff = new ArrayList<>();
		List<Double> efficiencyTakeOff = new ArrayList<>();		
		List<Amount<Force>> dragTakeOff = new ArrayList<>();
		List<Amount<Force>> totalThrustTakeOff = new ArrayList<>();
		List<Amount<Force>> thermicThrustTakeOff = new ArrayList<>();
		List<Amount<Force>> electricThrustTakeOff = new ArrayList<>();
		List<Double> throttleTakeOff = new ArrayList<>();
		List<Double> sfcTakeOff = new ArrayList<>();
		List<Double> fuelFlowTakeOff = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbTakeOff = new ArrayList<>();
		List<Amount<Angle>> climbAngleTakeOff = new ArrayList<>();
		List<Amount<Power>> fuelPowerTakeOff = new ArrayList<>();
		List<Amount<Power>> batteryPowerTakeOff = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyTakeOff = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyTakeOff = new ArrayList<>();
		
		//......................................................................
		// CLIMB
		List<Amount<Length>> rangeClimb = new ArrayList<>();
		List<Amount<Length>> altitudeClimb = new ArrayList<>();
		List<Amount<Duration>> timeClimb = new ArrayList<>();
		List<Amount<Mass>> fuelUsedClimb = new ArrayList<>();
		List<Amount<Mass>> aircraftMassClimb = new ArrayList<>();
		List<Amount<Mass>> emissionNOxClimb = new ArrayList<>();
		List<Amount<Mass>> emissionCOClimb = new ArrayList<>();
		List<Amount<Mass>> emissionHCClimb = new ArrayList<>();
		List<Amount<Mass>> emissionSootClimb = new ArrayList<>();
		List<Amount<Mass>> emissionCO2Climb = new ArrayList<>();
		List<Amount<Mass>> emissionSOxClimb = new ArrayList<>();
		List<Amount<Mass>> emissionH2OClimb = new ArrayList<>();
		List<Amount<Velocity>> speedTASClimb = new ArrayList<>();
		List<Amount<Velocity>> speedCASClimb = new ArrayList<>();
		List<Double> machClimb = new ArrayList<>();
		List<Double> cLClimb = new ArrayList<>();
		List<Double> cDClimb = new ArrayList<>();
		List<Double> efficiencyClimb = new ArrayList<>();		
		List<Amount<Force>> dragClimb = new ArrayList<>();
		List<Amount<Force>> totalThrustClimb = new ArrayList<>();
		List<Amount<Force>> thermicThrustClimb = new ArrayList<>();
		List<Amount<Force>> electricThrustClimb = new ArrayList<>();
		List<Double> throttleClimb = new ArrayList<>();
		List<Double> sfcClimb = new ArrayList<>();
		List<Double> fuelFlowClimb = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbClimb = new ArrayList<>();
		List<Amount<Angle>> climbAngleClimb = new ArrayList<>();
		List<Amount<Power>> fuelPowerClimb = new ArrayList<>();
		List<Amount<Power>> batteryPowerClimb = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyClimb = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyClimb = new ArrayList<>();
		
		//......................................................................
		// CRUISE
		List<Amount<Length>> rangeCruise = new ArrayList<>();
		List<Amount<Length>> altitudeCruise = new ArrayList<>();
		List<Amount<Duration>> timeCruise = new ArrayList<>();
		List<Amount<Mass>> fuelUsedCruise = new ArrayList<>();
		List<Amount<Mass>> aircraftMassCruise = new ArrayList<>();
		List<Amount<Mass>> emissionNOxCruise = new ArrayList<>();
		List<Amount<Mass>> emissionCOCruise = new ArrayList<>();
		List<Amount<Mass>> emissionHCCruise = new ArrayList<>();
		List<Amount<Mass>> emissionSootCruise = new ArrayList<>();
		List<Amount<Mass>> emissionCO2Cruise = new ArrayList<>();
		List<Amount<Mass>> emissionSOxCruise = new ArrayList<>();
		List<Amount<Mass>> emissionH2OCruise = new ArrayList<>();
		List<Amount<Velocity>> speedTASCruise = new ArrayList<>();
		List<Amount<Velocity>> speedCASCruise = new ArrayList<>();
		List<Double> machCruise = new ArrayList<>();
		List<Double> cLCruise = new ArrayList<>();
		List<Double> cDCruise = new ArrayList<>();
		List<Double> efficiencyCruise = new ArrayList<>();		
		List<Amount<Force>> dragCruise = new ArrayList<>();
		List<Amount<Force>> totalThrustCruise = new ArrayList<>();
		List<Amount<Force>> thermicThrustCruise = new ArrayList<>();
		List<Amount<Force>> electricThrustCruise = new ArrayList<>();
		List<Double> throttleCruise = new ArrayList<>();
		List<Double> sfcCruise = new ArrayList<>();
		List<Double> fuelFlowCruise = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbCruise = new ArrayList<>();
		List<Amount<Angle>> climbAngleCruise = new ArrayList<>();
		List<Amount<Power>> fuelPowerCruise = new ArrayList<>();
		List<Amount<Power>> batteryPowerCruise = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyCruise = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyCruise = new ArrayList<>();
		
		//......................................................................
		// FIRST DESCENT
		List<Amount<Length>> rangeFirstDescent = new ArrayList<>();
		List<Amount<Length>> altitudeFirstDescent = new ArrayList<>();
		List<Amount<Duration>> timeFirstDescent = new ArrayList<>();
		List<Amount<Mass>> fuelUsedFirstDescent = new ArrayList<>();
		List<Amount<Mass>> aircraftMassFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionNOxFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionCOFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionHCFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionSootFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionCO2FirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionSOxFirstDescent = new ArrayList<>();
		List<Amount<Mass>> emissionH2OFirstDescent = new ArrayList<>();
		List<Amount<Velocity>> speedTASFirstDescent = new ArrayList<>();
		List<Amount<Velocity>> speedCASFirstDescent = new ArrayList<>();
		List<Double> machFirstDescent = new ArrayList<>();
		List<Double> cLFirstDescent = new ArrayList<>();
		List<Double> cDFirstDescent = new ArrayList<>();
		List<Double> efficiencyFirstDescent = new ArrayList<>();		
		List<Amount<Force>> dragFirstDescent = new ArrayList<>();
		List<Amount<Force>> totalThrustFirstDescent = new ArrayList<>();
		List<Amount<Force>> thermicThrustFirstDescent = new ArrayList<>();
		List<Amount<Force>> electricThrustFirstDescent = new ArrayList<>();
		List<Double> throttleFirstDescent = new ArrayList<>();
		List<Double> sfcFirstDescent = new ArrayList<>();
		List<Double> fuelFlowFirstDescent = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbFirstDescent = new ArrayList<>();
		List<Amount<Angle>> climbAngleFirstDescent = new ArrayList<>();
		List<Amount<Power>> fuelPowerFirstDescent = new ArrayList<>();
		List<Amount<Power>> batteryPowerFirstDescent = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyFirstDescent = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyFirstDescent = new ArrayList<>();
		
		//......................................................................
		// SECOND CLIMB
		List<Amount<Length>> rangeSecondClimb = new ArrayList<>();
		List<Amount<Length>> altitudeSecondClimb = new ArrayList<>();
		List<Amount<Duration>> timeSecondClimb = new ArrayList<>();
		List<Amount<Mass>> fuelUsedSecondClimb = new ArrayList<>();
		List<Amount<Mass>> aircraftMassSecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionNOxSecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionCOSecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionHCSecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionSootSecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionCO2SecondClimb = new ArrayList<>();
		List<Amount<Mass>> emissionSOxSecondClimb  = new ArrayList<>();
		List<Amount<Mass>> emissionH2OSecondClimb  = new ArrayList<>();
		List<Amount<Velocity>> speedTASSecondClimb = new ArrayList<>();
		List<Amount<Velocity>> speedCASSecondClimb = new ArrayList<>();
		List<Double> machSecondClimb = new ArrayList<>();
		List<Double> cLSecondClimb = new ArrayList<>();
		List<Double> cDSecondClimb = new ArrayList<>();
		List<Double> efficiencySecondClimb = new ArrayList<>();		
		List<Amount<Force>> dragSecondClimb = new ArrayList<>();
		List<Amount<Force>> totalThrustSecondClimb = new ArrayList<>();
		List<Amount<Force>> thermicThrustSecondClimb = new ArrayList<>();
		List<Amount<Force>> electricThrustSecondClimb = new ArrayList<>();
		List<Double> throttleSecondClimb = new ArrayList<>();
		List<Double> sfcSecondClimb = new ArrayList<>();
		List<Double> fuelFlowSecondClimb = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbSecondClimb = new ArrayList<>();
		List<Amount<Angle>> climbAngleSecondClimb = new ArrayList<>();
		List<Amount<Power>> fuelPowerSecondClimb = new ArrayList<>();
		List<Amount<Power>> batteryPowerSecondClimb = new ArrayList<>();
		List<Amount<Energy>> fuelEnergySecondClimb = new ArrayList<>();
		List<Amount<Energy>> batteryEnergySecondClimb = new ArrayList<>();
		
		//......................................................................
		// ALTERNATE CRUISE
		List<Amount<Length>> rangeAlternateCruise = new ArrayList<>();
		List<Amount<Length>> altitudeAlternateCruise = new ArrayList<>();
		List<Amount<Duration>> timeAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> fuelUsedAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> aircraftMassAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionNOxAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionCOAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionHCAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionSootAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionCO2AlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionSOxAlternateCruise = new ArrayList<>();
		List<Amount<Mass>> emissionH2OAlternateCruise = new ArrayList<>();
		List<Amount<Velocity>> speedTASAlternateCruise = new ArrayList<>();
		List<Amount<Velocity>> speedCASAlternateCruise = new ArrayList<>();
		List<Double> machAlternateCruise = new ArrayList<>();
		List<Double> cLAlternateCruise = new ArrayList<>();
		List<Double> cDAlternateCruise = new ArrayList<>();
		List<Double> efficiencyAlternateCruise = new ArrayList<>();		
		List<Amount<Force>> dragAlternateCruise = new ArrayList<>();
		List<Amount<Force>> totalThrustAlternateCruise = new ArrayList<>();
		List<Amount<Force>> thermicThrustAlternateCruise = new ArrayList<>();
		List<Amount<Force>> electricThrustAlternateCruise = new ArrayList<>();
		List<Double> throttleAlternateCruise = new ArrayList<>();
		List<Double> sfcAlternateCruise = new ArrayList<>();
		List<Double> fuelFlowAlternateCruise = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbAlternateCruise = new ArrayList<>();
		List<Amount<Angle>> climbAngleAlternateCruise = new ArrayList<>();
		List<Amount<Power>> fuelPowerAlternateCruise = new ArrayList<>();
		List<Amount<Power>> batteryPowerAlternateCruise = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyAlternateCruise = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyAlternateCruise = new ArrayList<>();
		
		//......................................................................
		// SECOND DESCENT
		List<Amount<Length>> rangeSecondDescent = new ArrayList<>();
		List<Amount<Length>> altitudeSecondDescent = new ArrayList<>();
		List<Amount<Duration>> timeSecondDescent = new ArrayList<>();
		List<Amount<Mass>> fuelUsedSecondDescent = new ArrayList<>();
		List<Amount<Mass>> aircraftMassSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionNOxSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionCOSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionHCSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionSootSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionCO2SecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionSOxSecondDescent = new ArrayList<>();
		List<Amount<Mass>> emissionH2OSecondDescent = new ArrayList<>();
		List<Amount<Velocity>> speedTASSecondDescent = new ArrayList<>();
		List<Amount<Velocity>> speedCASSecondDescent = new ArrayList<>();
		List<Double> machSecondDescent = new ArrayList<>();
		List<Double> cLSecondDescent = new ArrayList<>();
		List<Double> cDSecondDescent = new ArrayList<>();
		List<Double> efficiencySecondDescent = new ArrayList<>();		
		List<Amount<Force>> dragSecondDescent = new ArrayList<>();
		List<Amount<Force>> totalThrustSecondDescent = new ArrayList<>();
		List<Amount<Force>> thermicThrustSecondDescent = new ArrayList<>();
		List<Amount<Force>> electricThrustSecondDescent = new ArrayList<>();
		List<Double> throttleSecondDescent = new ArrayList<>();
		List<Double> sfcSecondDescent = new ArrayList<>();
		List<Double> fuelFlowSecondDescent = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbSecondDescent = new ArrayList<>();
		List<Amount<Angle>> climbAngleSecondDescent = new ArrayList<>();
		List<Amount<Power>> fuelPowerSecondDescent = new ArrayList<>();
		List<Amount<Power>> batteryPowerSecondDescent = new ArrayList<>();
		List<Amount<Energy>> fuelEnergySecondDescent = new ArrayList<>();
		List<Amount<Energy>> batteryEnergySecondDescent = new ArrayList<>();
		
		//......................................................................
		// HOLDING
		List<Amount<Length>> rangeHolding = new ArrayList<>();
		List<Amount<Length>> altitudeHolding = new ArrayList<>();
		List<Amount<Duration>> timeHolding = new ArrayList<>();
		List<Amount<Mass>> fuelUsedHolding = new ArrayList<>();
		List<Amount<Mass>> aircraftMassHolding = new ArrayList<>();
		List<Amount<Mass>> emissionNOxHolding = new ArrayList<>();
		List<Amount<Mass>> emissionCOHolding = new ArrayList<>();
		List<Amount<Mass>> emissionHCHolding = new ArrayList<>();
		List<Amount<Mass>> emissionSootHolding = new ArrayList<>();
		List<Amount<Mass>> emissionCO2Holding = new ArrayList<>();
		List<Amount<Mass>> emissionSOxHolding = new ArrayList<>();
		List<Amount<Mass>> emissionH2OHolding = new ArrayList<>();
		List<Amount<Velocity>> speedTASHolding = new ArrayList<>();
		List<Amount<Velocity>> speedCASHolding = new ArrayList<>();
		List<Double> machHolding = new ArrayList<>();
		List<Double> cLHolding = new ArrayList<>();
		List<Double> cDHolding = new ArrayList<>();
		List<Double> efficiencyHolding = new ArrayList<>();		
		List<Amount<Force>> dragHolding = new ArrayList<>();
		List<Amount<Force>> totalThrustHolding = new ArrayList<>();
		List<Amount<Force>> thermicThrustHolding = new ArrayList<>();
		List<Amount<Force>> electricThrustHolding = new ArrayList<>();
		List<Double> throttleHolding = new ArrayList<>();
		List<Double> sfcHolding = new ArrayList<>();
		List<Double> fuelFlowHolding = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbHolding = new ArrayList<>();
		List<Amount<Angle>> climbAngleHolding = new ArrayList<>();
		List<Amount<Power>> fuelPowerHolding = new ArrayList<>();
		List<Amount<Power>> batteryPowerHolding = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyHolding = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyHolding = new ArrayList<>();
		
		//......................................................................
		// LANDING
		List<Amount<Length>> rangeLanding = new ArrayList<>();
		List<Amount<Length>> altitudeLanding = new ArrayList<>();
		List<Amount<Duration>> timeLanding = new ArrayList<>();
		List<Amount<Mass>> fuelUsedLanding = new ArrayList<>();
		List<Amount<Mass>> aircraftMassLanding = new ArrayList<>();
		List<Amount<Mass>> emissionNOxLanding = new ArrayList<>();
		List<Amount<Mass>> emissionCOLanding = new ArrayList<>();
		List<Amount<Mass>> emissionHCLanding = new ArrayList<>();
		List<Amount<Mass>> emissionSootLanding = new ArrayList<>();
		List<Amount<Mass>> emissionCO2Landing = new ArrayList<>();
		List<Amount<Mass>> emissionSOxLanding = new ArrayList<>();
		List<Amount<Mass>> emissionH2OLanding = new ArrayList<>();
		List<Amount<Velocity>> speedTASLanding = new ArrayList<>();
		List<Amount<Velocity>> speedCASLanding = new ArrayList<>();
		List<Double> machLanding = new ArrayList<>();
		List<Double> cLLanding = new ArrayList<>();
		List<Double> cDLanding = new ArrayList<>();
		List<Double> efficiencyLanding = new ArrayList<>();		
		List<Amount<Force>> dragLanding = new ArrayList<>();
		List<Amount<Force>> totalThrustLanding = new ArrayList<>();
		List<Amount<Force>> thermicThrustLanding = new ArrayList<>();
		List<Amount<Force>> electricThrustLanding = new ArrayList<>();
		List<Double> throttleLanding = new ArrayList<>();
		List<Double> sfcLanding = new ArrayList<>();
		List<Double> fuelFlowLanding = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbLanding = new ArrayList<>();
		List<Amount<Angle>> climbAngleLanding = new ArrayList<>();
		List<Amount<Power>> fuelPowerLanding = new ArrayList<>();
		List<Amount<Power>> batteryPowerLanding = new ArrayList<>();
		List<Amount<Energy>> fuelEnergyLanding = new ArrayList<>();
		List<Amount<Energy>> batteryEnergyLanding = new ArrayList<>();

		//----------------------------------------------------------------------
		// ITERATION START ...
		//----------------------------------------------------------------------
		if(initialMissionMass.doubleValue(SI.KILOGRAM) > maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {
			initialMissionMass = maximumTakeOffMass;
			initialFuelMass = maximumTakeOffMass
					.minus(operatingEmptyMass)
					.minus(singlePassengerMass.times(deisngPassengersNumber)); 
		}

		Amount<Length> currentCruiseRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Length> currentAlternateCruiseRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> newInitialFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		totalFuel = Amount.valueOf(0.0, SI.KILOGRAM);
		int i = 0;

		do {

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
			throttleTakeOff.addAll(timeTakeOff.stream().map(t -> theOperatingConditions.getThrottleTakeOff()).collect(Collectors.toList()));
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
								totalThrustTakeOff.get(iTakeOff).doubleValue(SI.NEWTON)
								* speedTASTakeOff.get(iTakeOff).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				batteryPowerTakeOff.add(Amount.valueOf(0.0, SI.WATT));
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
					- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
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
			throttleClimb.addAll(timeClimb.stream().map(t -> theOperatingConditions.getThrottleClimb()).collect(Collectors.toList()));
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
								totalThrustClimb.get(iClimb).doubleValue(SI.NEWTON)
								* speedTASClimb.get(iClimb).doubleValue(SI.METERS_PER_SECOND),
								SI.WATT
								)
						);
				batteryPowerClimb.add(Amount.valueOf(0.0, SI.WATT));
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
					- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
					- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
					SI.KILOGRAM
					);

			currentCruiseRange = missionRange;
			totalRange = Amount.valueOf(0.0, SI.METER);

			for (int iCruise=0; iCruise < 5; iCruise++) {
				List<Amount<Length>> cruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
						MyArrayUtils.linspace(
								0.0,
								currentCruiseRange.doubleValue(SI.METER),
								50
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
						/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
					emissionNOxCruise.add(
							emissionNOxCruise.get(emissionNOxCruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexNOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionCOCruise.add(
							emissionCOCruise.get(emissionCOCruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexCOList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionHCCruise.add(
							emissionHCCruise.get(emissionHCCruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexHCList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionSootCruise.add(
							emissionSootCruise.get(emissionSootCruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexSootList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionCO2Cruise.add(
							emissionCO2Cruise.get(emissionCO2Cruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexCO2List.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionSOxCruise.add(
							emissionSOxCruise.get(emissionSOxCruise.size()-1)
							.plus(
									Amount.valueOf(
											emissionIndexSOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
											*(fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM)),
											SI.GRAM)
									)
							);
					emissionH2OCruise.add(
							emissionH2OCruise.get(emissionH2OCruise.size()-1)
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
								/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
				throttleCruise.addAll(phi);
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
									totalThrustCruise.get(iCr).doubleValue(SI.NEWTON)
									* speedTASCruise.get(iCr).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerCruise.add(Amount.valueOf(0.0, SI.WATT));
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
						- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
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
				throttleFirstDescent.addAll(theFirstDescentCalculator.getThrottlePerStep());
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
									totalThrustFirstDescent.get(iFirstDescent).doubleValue(SI.NEWTON)
									* speedTASFirstDescent.get(iFirstDescent).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerFirstDescent.add(Amount.valueOf(0.0, SI.WATT));
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
						- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theFirstDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
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
				throttleSecondClimb.addAll(timeClimb.stream().map(t -> theOperatingConditions.getThrottleClimb()).collect(Collectors.toList()));
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
									totalThrustSecondClimb.get(iSecondClimb).doubleValue(SI.NEWTON)
									* speedTASSecondClimb.get(iSecondClimb).doubleValue(SI.METERS_PER_SECOND),
									SI.WATT
									)
							);
					batteryPowerSecondClimb.add(Amount.valueOf(0.0, SI.WATT));
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
						- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theFirstDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
						- theSecondClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
						SI.KILOGRAM
						);

				currentAlternateCruiseRange = alternateCruiseRange;

				for (int iAlternate=0; iAlternate < 5; iAlternate++) {
					List<Amount<Length>> alternateCruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
							MyArrayUtils.linspace(
									0.0,
									currentAlternateCruiseRange.doubleValue(SI.METER),
									10
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

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							rangeFactorAlternateCruiseList.add(
									Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLCruise,
											polarCDCruise,
											cLRangeAlternateCruiseArray[iCL]
											)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

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
									cDAlternateCruise.get(0)
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
								/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
						emissionNOxAlternateCruise.add(
								emissionNOxAlternateCruise.get(emissionNOxAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexNOxListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCOAlternateCruise.add(
								emissionCOAlternateCruise.get(emissionCOAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCOListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);		
						emissionHCAlternateCruise.add(
								emissionHCAlternateCruise.get(emissionHCAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexHCListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSootAlternateCruise.add(
								emissionSootAlternateCruise.get(emissionSootAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSootListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCO2AlternateCruise.add(
								emissionCO2AlternateCruise.get(emissionCO2AlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCO2ListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSOxAlternateCruise.add(
								emissionSOxAlternateCruise.get(emissionSOxAlternateCruise.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSOxListAlternateCruise.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionH2OAlternateCruise.add(
								emissionH2OAlternateCruise.get(emissionH2OAlternateCruise.size()-1)
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

							if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

								rangeFactorAlternateCruiseList.add(
										Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLCruise,
												polarCDCruise,
												cLRangeAlternateCruiseArray[iCL]
												)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

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
									/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
					throttleAlternateCruise.addAll(phiAlternateCruise);
					sfcAlternateCruise.addAll(sfcAlternateCruiseList);
					fuelFlowAlternateCruise.addAll(fuelFlowsAlternateCruise);
					
					for(int iAltCr=0; iAltCr<timeCruise.size(); iAltCr++) {
						/* WHEN THE HYBRIDAZION FACTOR WILL BE AVAILABLE USE IT TO CALCULATE THERMIC AND ELECTRIC THRUSTS FROM THE TOTAL */ 
						thermicThrustAlternateCruise.add(totalThrustAlternateCruise.get(iAltCr));
						electricThrustAlternateCruise.add(Amount.valueOf(0.0, SI.NEWTON));
						altitudeAlternateCruise.add(alternateCruiseAltitude);
						climbAngleAlternateCruise.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
						rateOfClimbAlternateCruise.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
						efficiencyAlternateCruise.add(cLAlternateCruise.get(iAltCr)/cDAlternateCruise.get(iAltCr));
						fuelPowerAlternateCruise.add(
								Amount.valueOf(
										totalThrustAlternateCruise.get(iAltCr).doubleValue(SI.NEWTON)
										* speedTASAlternateCruise.get(iAltCr).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerAlternateCruise.add(Amount.valueOf(0.0, SI.WATT));
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
							- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theFirstDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theSecondClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStepAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
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
					throttleSecondDescent.addAll(theSecondDescentCalculator.getThrottlePerStep());
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
										totalThrustSecondDescent.get(iSecondDescent).doubleValue(SI.NEWTON)
										* speedTASSecondDescent.get(iSecondDescent).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerSecondDescent.add(Amount.valueOf(0.0, SI.WATT));
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
							- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theFirstDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theSecondClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theSecondDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
							SI.KILOGRAM
							);

					List<Amount<Duration>> timeHoldingArray = MyArrayUtils.convertDoubleArrayToListOfAmount( 
							MyArrayUtils.linspace(
									0.0,
									holdingDuration.doubleValue(NonSI.MINUTE),
									10
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

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							enduranceFactorHoldingList.add(
									cLEnduranceHoldingArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													polarCLClimb,
													polarCDClimb,
													cLEnduranceHoldingArray[iCL]
													)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

							enduranceFactorHoldingList.add(
									Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLClimb,
											polarCDClimb,
											cLEnduranceHoldingArray[iCL]
											)
									);

						}
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
									cDHolding.get(0)
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
								/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
						emissionNOxHolding.add(
								emissionNOxHolding.get(emissionNOxHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexNOxListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCOHolding.add(
								emissionCOHolding.get(emissionCOHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCOListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionHCHolding.add(
								emissionHCHolding.get(emissionHCHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexHCListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSootHolding.add(
								emissionSootHolding.get(emissionSootHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSootListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionCO2Holding.add(
								emissionCO2Holding.get(emissionCO2Holding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexCO2ListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionSOxHolding.add(
								emissionSOxHolding.get(emissionSOxHolding.size()-1)
								.plus(
										Amount.valueOf(
												emissionIndexSOxListHolding.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
												*(fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM)),
												SI.GRAM)
										)
								);
						emissionH2OHolding.add(
								emissionH2OHolding.get(emissionH2OHolding.size()-1)
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

							if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

								enduranceFactorHoldingList.add(
										cLEnduranceHoldingArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														polarCLClimb,
														polarCDClimb,
														cLEnduranceHoldingArray[iCL]
														)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

								enduranceFactorHoldingList.add(
										Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLClimb,
												polarCDClimb,
												cLEnduranceHoldingArray[iCL]
												)
										);

							}
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
									/ theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)
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
					throttleHolding.addAll(phiHolding);
					sfcHolding.addAll(sfcHoldingList);
					fuelFlowHolding.addAll(fuelFlowsHolding);
					
					for(int iHold=0; iHold<timeCruise.size(); iHold++) {
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
										totalThrustHolding.get(iHold).doubleValue(SI.NEWTON)
										* speedTASHolding.get(iHold).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerHolding.add(Amount.valueOf(0.0, SI.WATT));
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
							- theTakeOffCalculator.getFuelUsed().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theFirstDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theSecondClimbCalculator.getFuelUsedClimb().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStepAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- theSecondDescentCalculator.getFuelUsedPerStep().stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
							- fuelUsedPerStepHolding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
							SI.KILOGRAM
							);

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
					throttleLanding.addAll(timeLanding.stream().map(t -> theOperatingConditions.getThrottleLanding()).collect(Collectors.toList()));
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
										totalThrustLanding.get(iLanding).doubleValue(SI.NEWTON)
										* speedTASLanding.get(iLanding).doubleValue(SI.METERS_PER_SECOND),
										SI.WATT
										)
								);
						batteryPowerLanding.add(Amount.valueOf(0.0, SI.WATT));
						fuelEnergyLanding.add(
								Amount.valueOf(
										fuelPowerLanding.get(iLanding).doubleValue(SI.WATT)
										* timeLanding.get(iLanding).doubleValue(SI.SECOND),
										SI.JOULE
										)
								);
						batteryEnergyLanding.add(Amount.valueOf(0.0, SI.JOULE));
					}
					
					//.....................................................................
					// NEW ITERATION ALTERNATE CRUISE LENGTH
					currentAlternateCruiseRange = Amount.valueOf( 
							currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
							+ ( alternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
									- rangeSecondClimb.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
									- rangeAlternateCruise.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
									- rangeHolding.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
									),
							NonSI.NAUTICAL_MILE
							);
					if(currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
						return;
					}
				}
				
				//.....................................................................
				// NEW ITERATION CRUISE LENGTH
				currentCruiseRange = Amount.valueOf( 
						currentCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
						+ ( missionRange.doubleValue(NonSI.NAUTICAL_MILE)
								- rangeTakeOff.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
								- rangeClimb.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
								- rangeCruise.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
								- rangeFirstDescent.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
								- rangeLanding.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
								),
						NonSI.NAUTICAL_MILE
						);
				if(currentCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (NEW CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
					return;
				}
				
			} 
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			newInitialFuelMass = totalFuel.to(SI.KILOGRAM).divide(1-fuelReserve); 
			initialMissionMass = operatingEmptyMass
					.plus(singlePassengerMass.times(deisngPassengersNumber))
					.plus(newInitialFuelMass); 
			
			if(initialMissionMass.doubleValue(SI.KILOGRAM) > maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {

				System.err.println("MAXIMUM TAKE-OFF MASS SURPASSED !! REDUCING PASSENGERS NUMBER TO INCREASE THE FUEL ... ");
				
				deisngPassengersNumber += (int) Math.ceil(
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
		
		if(theFirstDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - FIRST DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theSecondDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - SECOND DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		
		//-------------------------------------------------------------------------------------------------
		// MANAGING OUTPUT DATA:
		this.initialFuelMass = initialFuelMass;
		this.initialMissionMass = initialMissionMass;
		this.totalFuel = Amount.valueOf(
				fuelUsedTakeOff.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedClimb.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedFirstDescent.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedSecondClimb.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedSecondDescent.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedHolding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedLanding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
				SI.KILOGRAM
				);
		this.blockFuel = Amount.valueOf(
				fuelUsedTakeOff.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedClimb.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedFirstDescent.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum()
				+ fuelUsedLanding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
				SI.KILOGRAM
				);
		this.endMissionMass = this.initialMissionMass.to(SI.KILOGRAM).minus(this.totalFuel.to(SI.KILOGRAM));
		this.totalTime = Amount.valueOf(
				timeTakeOff.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeClimb.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeCruise.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeFirstDescent.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeSecondClimb.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeAlternateCruise.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeSecondDescent.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeHolding.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeLanding.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum(),
				NonSI.MINUTE
				);
		this.blockTime = Amount.valueOf(
				timeTakeOff.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeClimb.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeCruise.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeFirstDescent.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum()
				+ timeLanding.stream().mapToDouble(t -> t.doubleValue(NonSI.MINUTE)).sum(),
				NonSI.MINUTE
				);
		this.totalRange = Amount.valueOf(
				rangeTakeOff.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeClimb.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeCruise.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeFirstDescent.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeSecondClimb.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeAlternateCruise.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeSecondDescent.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeHolding.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum()
				+ rangeLanding.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).sum(),
				NonSI.NAUTICAL_MILE
				);
		this.totalFuelPower = Amount.valueOf(
				fuelPowerTakeOff.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerClimb.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerCruise.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerFirstDescent.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerSecondClimb.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerAlternateCruise.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerSecondDescent.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerHolding.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ fuelPowerLanding.stream().mapToDouble(fp -> fp.doubleValue(SI.KILO(SI.WATT))).sum(),
				SI.KILO(SI.WATT)
				);
		this.totalBatteryPower = Amount.valueOf(
				batteryPowerTakeOff.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerClimb.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerCruise.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerFirstDescent.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerSecondClimb.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerAlternateCruise.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerSecondDescent.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerHolding.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum()
				+ batteryPowerLanding.stream().mapToDouble(bp -> bp.doubleValue(SI.KILO(SI.WATT))).sum(),
				SI.KILO(SI.WATT)
				);
		this.totalFuelEnergy = Amount.valueOf(
				fuelEnergyTakeOff.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyClimb.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyCruise.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyFirstDescent.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergySecondClimb.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyAlternateCruise.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergySecondDescent.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyHolding.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum()
				+ fuelEnergyLanding.stream().mapToDouble(fe -> fe.doubleValue(SI.JOULE)).sum(),
				SI.JOULE
				);
		this.totalBatteryEnergy = Amount.valueOf(
				batteryEnergyTakeOff.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyClimb.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyCruise.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyFirstDescent.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergySecondClimb.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyAlternateCruise.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergySecondDescent.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyHolding.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum()
				+ batteryEnergyLanding.stream().mapToDouble(be -> be.doubleValue(SI.JOULE)).sum(),
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
		this.rangeMap.put(MissionPhasesEnum.LANDING, rangeLanding.stream().map(e -> e.to(NonSI.NAUTICAL_MILE)).collect(Collectors.toList()));
		
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
		this.altitudeMap.put(MissionPhasesEnum.LANDING, altitudeLanding.stream().map(e -> e.to(NonSI.FOOT)).collect(Collectors.toList()));
		
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
		this.timeMap.put(MissionPhasesEnum.LANDING, timeLanding.stream().map(e -> e.to(NonSI.MINUTE)).collect(Collectors.toList()));
		
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
		this.fuelUsedMap.put(MissionPhasesEnum.LANDING, fuelUsedLanding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		
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
		this.massMap.put(MissionPhasesEnum.LANDING, aircraftMassLanding.stream().map(e -> e.to(SI.KILOGRAM)).collect(Collectors.toList()));
		
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
		this.emissionNOxMap.put(MissionPhasesEnum.LANDING, emissionNOxLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionCOMap.put(MissionPhasesEnum.LANDING, emissionCOLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionHCMap.put(MissionPhasesEnum.LANDING, emissionHCLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionSootMap.put(MissionPhasesEnum.LANDING, emissionSootLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionCO2Map.put(MissionPhasesEnum.LANDING, emissionCO2Landing.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionSOxMap.put(MissionPhasesEnum.LANDING, emissionSOxLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.emissionH2OMap.put(MissionPhasesEnum.LANDING, emissionH2OLanding.stream().map(e -> e.to(SI.GRAM)).collect(Collectors.toList()));
		
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
		this.speedTASMissionMap.put(MissionPhasesEnum.LANDING, speedTASLanding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		
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
		this.speedCASMissionMap.put(MissionPhasesEnum.LANDING, speedCASLanding.stream().map(e -> e.to(NonSI.KNOT)).collect(Collectors.toList()));
		
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
		this.machMissionMap.put(MissionPhasesEnum.LANDING, machLanding);
		
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
		this.liftingCoefficientMissionMap.put(MissionPhasesEnum.LANDING, cLLanding);
		
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
		this.dragCoefficientMissionMap.put(MissionPhasesEnum.LANDING, cDLanding);
		
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
		this.efficiencyMissionMap.put(MissionPhasesEnum.LANDING, efficiencyLanding);
		
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
		this.dragMissionMap.put(MissionPhasesEnum.LANDING, dragLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
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
		this.totalThrustMissionMap.put(MissionPhasesEnum.LANDING, totalThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
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
		this.thermicThrustMissionMap.put(MissionPhasesEnum.LANDING, thermicThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
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
		this.electricThrustMissionMap.put(MissionPhasesEnum.LANDING, electricThrustLanding.stream().map(e -> e.to(NonSI.POUND_FORCE)).collect(Collectors.toList()));
		
		//.................................................................................................
		// THROTTLE
		this.throttleMissionMap.put(MissionPhasesEnum.TAKE_OFF, throttleTakeOff);
		this.throttleMissionMap.put(MissionPhasesEnum.CLIMB, throttleClimb);
		this.throttleMissionMap.put(MissionPhasesEnum.CRUISE, throttleCruise);
		this.throttleMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, throttleFirstDescent);
		this.throttleMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, throttleSecondClimb);
		this.throttleMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, throttleAlternateCruise);
		this.throttleMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, throttleSecondDescent);
		this.throttleMissionMap.put(MissionPhasesEnum.HOLDING, throttleHolding);
		this.throttleMissionMap.put(MissionPhasesEnum.LANDING, throttleLanding);
		
		//.................................................................................................
		// SFC
		this.sfcMissionMap.put(MissionPhasesEnum.TAKE_OFF, sfcTakeOff); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.CLIMB, sfcClimb); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.CRUISE, sfcCruise); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.FIRST_DESCENT, sfcFirstDescent); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.SECOND_CLIMB, sfcSecondClimb); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.ALTERNATE_CRUISE, sfcAlternateCruise); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.SECOND_DESCENT, sfcSecondDescent); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.HOLDING, sfcHolding); /* lb/lb*hr */
		this.sfcMissionMap.put(MissionPhasesEnum.LANDING, sfcLanding); /* lb/lb*hr */
		
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
		this.fuelFlowMissionMap.put(MissionPhasesEnum.LANDING, fuelFlowLanding.stream().map(e -> e*60.0).collect(Collectors.toList())); /* kg/min */
		
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
		this.rateOfClimbMissionMap.put(MissionPhasesEnum.LANDING, rateOfClimbLanding.stream().map(e -> e.to(MyUnits.FOOT_PER_MINUTE)).collect(Collectors.toList()));
		
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
		this.climbAngleMissionMap.put(MissionPhasesEnum.LANDING, climbAngleLanding.stream().map(e -> e.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
		
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
		this.fuelPowerMap.put(MissionPhasesEnum.LANDING, fuelPowerLanding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		
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
		this.batteryPowerMap.put(MissionPhasesEnum.LANDING, batteryPowerLanding.stream().map(e -> e.to(SI.KILO(SI.WATT))).collect(Collectors.toList()));
		
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
		this.fuelEnergyMap.put(MissionPhasesEnum.LANDING, fuelEnergyLanding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		
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
		this.batteryEnergyMap.put(MissionPhasesEnum.LANDING, batteryEnergyLanding.stream().map(e -> e.to(SI.JOULE)).collect(Collectors.toList()));
		
	}

	public void plotProfiles(
			List<PerformancePlotEnum> _plotList,
			String _missionProfilesFolderPath) {
		
		if(_plotList.contains(PerformancePlotEnum.RANGE_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_rangeList.stream()
							.map(r -> r.to(SI.KILOMETER))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"km", "m",
					_missionProfilesFolderPath, "Range_Profile_SI",true
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_rangeList.stream()
							.map(r -> r.to(NonSI.NAUTICAL_MILE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"nmi", "ft",
					_missionProfilesFolderPath, "Range_Profile_IMPERIAL",true
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.TIME_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "m",
					_missionProfilesFolderPath, "Time_Profile_(min)_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "ft",
					_missionProfilesFolderPath, "Time_Profile_(min)_IMPERIAL",true
					);
			
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "m",
					_missionProfilesFolderPath, "Time_Profile_(hours)_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "ft",
					_missionProfilesFolderPath, "Time_Profile_(hours)_IMPERIAL",true
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_USED_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_fuelUsedList.stream()
							.map(f -> f.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"kg", "m",
					_missionProfilesFolderPath, "Fuel_used_Profile_SI",true
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_fuelUsedList.stream()
							.map(f -> f.to(NonSI.POUND))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"lb", "ft",
					_missionProfilesFolderPath, "Fuel_used_Profile_IMPERIAL",true
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.WEIGHT_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_massList.stream()
							.map(m -> m.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					0.0, null, null, null,
					"Time", "Aircraft mass",
					"min", "kg",
					_missionProfilesFolderPath, "Mass_Profile",true
					);
		}
		
	}
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

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
				.append("\t\tDesign passengers number = " + theAircraft.getCabinConfiguration().getDesignPassengerNumber() + "\n")
				.append("\t\tPassengers number for this mission = " + deisngPassengersNumber + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off range = " + _rangeList.get(1).to(NonSI.NAUTICAL_MILE) + " \n")
				.append("\t\tClimb range = " + _rangeList.get(2).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(1).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tCruise range = " + _rangeList.get(3).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(2).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tFirst descent range = " + _rangeList.get(4).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(3).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tSecond climb range = " + _rangeList.get(5).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(4).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tAlternate cruise range = " + _rangeList.get(6).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(5).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tSecond descent range = " + _rangeList.get(7).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(6).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tHolding range = " + _rangeList.get(8).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(7).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tThird descent range = " + _rangeList.get(9).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(8).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tLanding range = " + _rangeList.get(10).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(9).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tAltitude at take-off ending = " + _altitudeList.get(1).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at climb ending = " + _altitudeList.get(2).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at cruise ending = " + _altitudeList.get(3).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at first descent ending = " + _altitudeList.get(4).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at second climb ending = " + _altitudeList.get(5).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at alternate cruise ending = " + _altitudeList.get(6).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at second descent ending = " + _altitudeList.get(7).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at holding ending = " + _altitudeList.get(8).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at third descent ending = " + _altitudeList.get(9).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at landing ending = " + _altitudeList.get(10).to(NonSI.FOOT) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off duration = " + _timeList.get(1).to(NonSI.MINUTE) + " \n")
				.append("\t\tClimb duration = " + _timeList.get(2).to(NonSI.MINUTE).minus(_timeList.get(1).to(NonSI.MINUTE)) + " \n")
				.append("\t\tCruise duration = " + _timeList.get(3).to(NonSI.MINUTE).minus(_timeList.get(2).to(NonSI.MINUTE))+ " \n")
				.append("\t\tFirst descent duration = " + _timeList.get(4).to(NonSI.MINUTE).minus(_timeList.get(3).to(NonSI.MINUTE)) + " \n")
				.append("\t\tSecond climb duration = " + _timeList.get(5).to(NonSI.MINUTE).minus(_timeList.get(4).to(NonSI.MINUTE)) + " \n")
				.append("\t\tAlternate cruise duration = " + _timeList.get(6).to(NonSI.MINUTE).minus(_timeList.get(5).to(NonSI.MINUTE)) + " \n")
				.append("\t\tSecond descent duration = " + _timeList.get(7).to(NonSI.MINUTE).minus(_timeList.get(6).to(NonSI.MINUTE)) + " \n")
				.append("\t\tHolding duration = " + _timeList.get(8).to(NonSI.MINUTE).minus(_timeList.get(7).to(NonSI.MINUTE)) + " \n")
				.append("\t\tThird descent duration = " + _timeList.get(9).to(NonSI.MINUTE).minus(_timeList.get(8).to(NonSI.MINUTE)) + " \n")
				.append("\t\tLanding duration = " + _timeList.get(10).to(NonSI.MINUTE).minus(_timeList.get(9).to(NonSI.MINUTE)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off used fuel = " + _fuelUsedList.get(1).to(SI.KILOGRAM) + " \n")
				.append("\t\tClimb used fuel = " + _fuelUsedList.get(2).to(SI.KILOGRAM).minus(_fuelUsedList.get(1).to(SI.KILOGRAM)) + " \n")
				.append("\t\tCruise used fuel = " + _fuelUsedList.get(3).to(SI.KILOGRAM).minus(_fuelUsedList.get(2).to(SI.KILOGRAM)) + "\n")
				.append("\t\tFirst descent used fuel = " + _fuelUsedList.get(4).to(SI.KILOGRAM).minus(_fuelUsedList.get(3).to(SI.KILOGRAM)) + " \n")
				.append("\t\tSecond climb used fuel = " + _fuelUsedList.get(5).to(SI.KILOGRAM).minus(_fuelUsedList.get(4).to(SI.KILOGRAM)) + " \n")
				.append("\t\tAlternate cruise used fuel = " + _fuelUsedList.get(6).to(SI.KILOGRAM).minus(_fuelUsedList.get(5).to(SI.KILOGRAM)) + "\n")
				.append("\t\tSecond descent used fuel = " + _fuelUsedList.get(7).to(SI.KILOGRAM).minus(_fuelUsedList.get(6).to(SI.KILOGRAM)) + "\n")
				.append("\t\tHolding used fuel = " + _fuelUsedList.get(8).to(SI.KILOGRAM).minus(_fuelUsedList.get(7).to(SI.KILOGRAM)) + " \n")
				.append("\t\tThird descent used fuel = " + _fuelUsedList.get(9).to(SI.KILOGRAM).minus(_fuelUsedList.get(8).to(SI.KILOGRAM)) + " \n")
				.append("\t\tLanding used fuel = " + _fuelUsedList.get(10).to(SI.KILOGRAM).minus(_fuelUsedList.get(9).to(SI.KILOGRAM)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tAircraft weight at take-off start  = " + _massList.get(1).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at climb start = " + _massList.get(2).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at cruise start = " + _massList.get(3).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at first descent start = " + _massList.get(4).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at second climb start = " + _massList.get(5).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at alternate cruise start = " + _massList.get(6).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at second descent start = " + _massList.get(7).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at holding start = " + _massList.get(8).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at third descent start = " + _massList.get(9).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at landing start = " + _massList.get(10).to(SI.KILOGRAM) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTAKE-OFF\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at take-off start  = " + _speedTASMissionList.get(0).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at take-off ending  = " + _speedTASMissionList.get(1).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at take-off start  = " + _speedCASMissionList.get(0).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at take-off ending  = " + _speedCASMissionList.get(1).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at take-off start  = " + _machMissionList.get(0) + " \n")
				.append("\t\tMach at take-off ending  = " + _machMissionList.get(1) + " \n")
				.append("\t\tCL at take-off start  = " + _liftingCoefficientMissionList.get(0) + " \n")
				.append("\t\tCL at take-off ending  = " + _liftingCoefficientMissionList.get(1) + " \n")
				.append("\t\tCD at take-off start  = " + _dragCoefficientMissionList.get(0) + " \n")
				.append("\t\tCD at take-off ending  = " + _dragCoefficientMissionList.get(1) + " \n")
				.append("\t\tEfficiency at take-off start  = " + _efficiencyMissionList.get(0) + " \n")
				.append("\t\tEfficiency at take-off ending  = " + _efficiencyMissionList.get(1) + " \n")
				.append("\t\tThrust at take-off start  = " + _thrustMissionList.get(0).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at take-off ending  = " + _thrustMissionList.get(1).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at take-off start  = " + _dragMissionList.get(0).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at take-off ending  = " + _dragMissionList.get(1).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at take-off start  = " + _rateOfClimbMissionList.get(0).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at take-off ending  = " + _rateOfClimbMissionList.get(1).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at take-off start  = " + _climbAngleMissionList.get(0).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at take-off ending  = " + _climbAngleMissionList.get(1).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at take-off start  = " + _fuelFlowMissionList.get(0) + " lb/hr\n")
				.append("\t\tFuel flow at take-off ending  = " + _fuelFlowMissionList.get(1) + " lb/hr\n")
				.append("\t\tSFC at take-off start  = " + _sfcMissionList.get(0) + " lb/(lb*hr)\n")
				.append("\t\tSFC at take-off ending  = " + _sfcMissionList.get(1) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tCLIMB\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at climb start  = " + _speedTASMissionList.get(2).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at climb ending  = " + _speedTASMissionList.get(3).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at climb start  = " + _speedCASMissionList.get(2).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at climb ending  = " + _speedCASMissionList.get(3).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at climb start  = " + _machMissionList.get(2) + " \n")
				.append("\t\tMach at climb ending  = " + _machMissionList.get(3) + " \n")
				.append("\t\tCL at climb start  = " + _liftingCoefficientMissionList.get(2) + " \n")
				.append("\t\tCL at climb ending  = " + _liftingCoefficientMissionList.get(3) + " \n")
				.append("\t\tCD at climb start  = " + _dragCoefficientMissionList.get(2) + " \n")
				.append("\t\tCD at climb ending  = " + _dragCoefficientMissionList.get(3) + " \n")
				.append("\t\tEfficiency at climb start  = " + _efficiencyMissionList.get(2) + " \n")
				.append("\t\tEfficiency at climb ending  = " + _efficiencyMissionList.get(3) + " \n")
				.append("\t\tThrust at climb start  = " + _thrustMissionList.get(2).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at climb ending  = " + _thrustMissionList.get(3).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at climb start  = " + _dragMissionList.get(2).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at climb ending  = " + _dragMissionList.get(3).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at climb start  = " + _rateOfClimbMissionList.get(2).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at climb ending  = " + _rateOfClimbMissionList.get(3).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at climb start  = " + _climbAngleMissionList.get(2).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at climb ending  = " + _climbAngleMissionList.get(3).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at climb start  = " + _fuelFlowMissionList.get(2) + " lb/hr\n")
				.append("\t\tFuel flow at climb ending  = " + _fuelFlowMissionList.get(3) + " lb/hr\n")
				.append("\t\tSFC at climb start  = " + _sfcMissionList.get(2) + " lb/(lb*hr)\n")
				.append("\t\tSFC at climb ending  = " + _sfcMissionList.get(3) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tCRUISE\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at cruise start  = " + _speedTASMissionList.get(4).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at cruise ending  = " + _speedTASMissionList.get(5).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at cruise start  = " + _speedCASMissionList.get(4).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at cruise ending  = " + _speedCASMissionList.get(5).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at cruise start  = " + _machMissionList.get(4) + " \n")
				.append("\t\tMach at cruise ending  = " + _machMissionList.get(5) + " \n")
				.append("\t\tCL at cruise start  = " + _liftingCoefficientMissionList.get(4) + " \n")
				.append("\t\tCL at cruise ending  = " + _liftingCoefficientMissionList.get(5) + " \n")
				.append("\t\tCD at cruise start  = " + _dragCoefficientMissionList.get(4) + " \n")
				.append("\t\tCD at cruise ending  = " + _dragCoefficientMissionList.get(5) + " \n")
				.append("\t\tEfficiency at cruise start  = " + _efficiencyMissionList.get(4) + " \n")
				.append("\t\tEfficiency at cruise ending  = " + _efficiencyMissionList.get(5) + " \n")
				.append("\t\tThrottle at cruise start  = " + _throttleMissionList.get(0) + " \n")
				.append("\t\tThrottle at cruise ending  = " + _throttleMissionList.get(1) + " \n")
				.append("\t\tThrust at cruise start  = " + _thrustMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at cruise ending  = " + _thrustMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise start  = " + _dragMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise ending  = " + _dragMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at cruise start  = " + _rateOfClimbMissionList.get(4).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at cruise ending  = " + _rateOfClimbMissionList.get(5).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at cruise start  = " + _climbAngleMissionList.get(4).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at cruise ending  = " + _climbAngleMissionList.get(5).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at cruise start  = " + _fuelFlowMissionList.get(4) + " lb/hr\n")
				.append("\t\tFuel flow at cruise ending  = " + _fuelFlowMissionList.get(5) + " lb/hr\n")
				.append("\t\tSFC at cruise start  = " + _sfcMissionList.get(4) + " lb/(lb*hr)\n")
				.append("\t\tSFC at cruise ending  = " + _sfcMissionList.get(5) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tFIRST DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at first descent start  = " + _speedTASMissionList.get(6).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at first descent ending  = " + _speedTASMissionList.get(7).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at first descent start  = " + _speedCASMissionList.get(6).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at first descent ending  = " + _speedCASMissionList.get(7).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at first descent start  = " + _machMissionList.get(6) + " \n")
				.append("\t\tMach at first descent ending  = " + _machMissionList.get(7) + " \n")
				.append("\t\tCL at first descent start  = " + _liftingCoefficientMissionList.get(6) + " \n")
				.append("\t\tCL at first descent ending  = " + _liftingCoefficientMissionList.get(7) + " \n")
				.append("\t\tCD at first descent start  = " + _dragCoefficientMissionList.get(6) + " \n")
				.append("\t\tCD at first descent ending  = " + _dragCoefficientMissionList.get(7) + " \n")
				.append("\t\tEfficiency at first descent start  = " + _efficiencyMissionList.get(6) + " \n")
				.append("\t\tEfficiency at first descent ending  = " + _efficiencyMissionList.get(7) + " \n")
				.append("\t\tThrust at first descent start  = " + _thrustMissionList.get(6).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at first descent ending  = " + _thrustMissionList.get(7).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at first descent start  = " + _dragMissionList.get(6).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at first descent ending  = " + _dragMissionList.get(7).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at first descent start  = " + _rateOfClimbMissionList.get(6).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at first descent ending  = " + _rateOfClimbMissionList.get(7).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at first descent start  = " + _climbAngleMissionList.get(6).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at first descent ending  = " + _climbAngleMissionList.get(7).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at first descent start  = " + _fuelFlowMissionList.get(6) + " lb/hr\n")
				.append("\t\tFuel flow at first descent ending  = " + _fuelFlowMissionList.get(7) + " lb/hr\n")
				.append("\t\tSFC at first descent start  = " + _sfcMissionList.get(6) + " lb/(lb*hr)\n")
				.append("\t\tSFC at first desecnt ending  = " + _sfcMissionList.get(7) + " lb/(lb*hr)\n");
		
		if(_alternateCruiseAltitude.doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
			sb.append("\t\t.....................................\n")
			.append("\t\tSECOND CLIMB\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second climb start  = " + _speedTASMissionList.get(8).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second climb ending  = " + _speedTASMissionList.get(9).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second climb start  = " + _speedCASMissionList.get(8).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second climb ending  = " + _speedCASMissionList.get(9).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at second climb start  = " + _machMissionList.get(8) + " \n")
			.append("\t\tMach at second climb ending  = " + _machMissionList.get(9) + " \n")
			.append("\t\tCL at second climb start  = " + _liftingCoefficientMissionList.get(8) + " \n")
			.append("\t\tCL at second climb ending  = " + _liftingCoefficientMissionList.get(9) + " \n")
			.append("\t\tCD at second climb start  = " + _dragCoefficientMissionList.get(8) + " \n")
			.append("\t\tCD at second climb ending  = " + _dragCoefficientMissionList.get(9) + " \n")
			.append("\t\tEfficiency at second climb start  = " + _efficiencyMissionList.get(8) + " \n")
			.append("\t\tEfficiency at second climb ending  = " + _efficiencyMissionList.get(9) + " \n")
			.append("\t\tThrust at second climb start  = " + _thrustMissionList.get(8).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at second climb ending  = " + _thrustMissionList.get(9).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second climb start  = " + _dragMissionList.get(8).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second climb ending  = " + _dragMissionList.get(9).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at second climb start  = " + _rateOfClimbMissionList.get(8).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at second climb ending  = " + _rateOfClimbMissionList.get(9).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at second climb start  = " + _climbAngleMissionList.get(8).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at second climb ending  = " + _climbAngleMissionList.get(9).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at second climb start  = " + _fuelFlowMissionList.get(8) + " lb/hr\n")
			.append("\t\tFuel flow at second climb ending  = " + _fuelFlowMissionList.get(9) + " lb/hr\n")
			.append("\t\tSFC at second climb start  = " + _sfcMissionList.get(8) + " lb/(lb*hr)\n")
			.append("\t\tSFC at second climb ending  = " + _sfcMissionList.get(9) + " lb/(lb*hr)\n")
			.append("\t\t.....................................\n")
			.append("\t\tALTERNATE CRUISE\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at alternate cruise start  = " + _speedTASMissionList.get(10).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at alternate cruise ending  = " + _speedTASMissionList.get(11).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at alternate cruise start  = " + _speedCASMissionList.get(10).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at alternate cruise ending  = " + _speedCASMissionList.get(11).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at alternate cruise start  = " + _machMissionList.get(10) + " \n")
			.append("\t\tMach at alternate cruise ending  = " + _machMissionList.get(11) + " \n")
			.append("\t\tCL at alternate cruise start  = " + _liftingCoefficientMissionList.get(10) + " \n")
			.append("\t\tCL at alternate cruise ending  = " + _liftingCoefficientMissionList.get(11) + " \n")
			.append("\t\tCD at alternate cruise start  = " + _dragCoefficientMissionList.get(10) + " \n")
			.append("\t\tCD at alternate cruise ending  = " + _dragCoefficientMissionList.get(11) + " \n")
			.append("\t\tEfficiency at alternate cruise start  = " + _efficiencyMissionList.get(10) + " \n")
			.append("\t\tEfficiency at alternate cruise ending  = " + _efficiencyMissionList.get(11) + " \n")
			.append("\t\tThrottle at alternate cruise start  = " + _throttleMissionList.get(2) + " \n")
			.append("\t\tThrottle at alternate cruise ending  = " + _throttleMissionList.get(3) + " \n")
			.append("\t\tThrust at alternate cruise start  = " + _thrustMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at alternate cruise ending  = " + _thrustMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise start  = " + _dragMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise ending  = " + _dragMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at alternate cruise start  = " + _rateOfClimbMissionList.get(10).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at alternate cruise ending  = " + _rateOfClimbMissionList.get(11).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at alternate cruise start  = " + _climbAngleMissionList.get(10).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at alternate cruise ending  = " + _climbAngleMissionList.get(11).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at alternate cruise start  = " + _fuelFlowMissionList.get(10) + " lb/hr\n")
			.append("\t\tFuel flow at alternate cruise ending  = " + _fuelFlowMissionList.get(11) + " lb/hr\n")
			.append("\t\tSFC at alternate cruise start  = " + _sfcMissionList.get(10) + " lb/(lb*hr)\n")
			.append("\t\tSFC at alternate cruise ending  = " + _sfcMissionList.get(11) + " lb/(lb*hr)\n")
			.append("\t\t.....................................\n")
			.append("\t\tSECOND DESCENT\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second descent start  = " + _speedTASMissionList.get(12).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second descent ending  = " + _speedTASMissionList.get(13).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second descent start  = " + _speedCASMissionList.get(12).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second descent ending  = " + _speedCASMissionList.get(13).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at second descent start  = " + _machMissionList.get(12) + " \n")
			.append("\t\tMach at second descent ending  = " + _machMissionList.get(13) + " \n")
			.append("\t\tCL at second descent start  = " + _liftingCoefficientMissionList.get(12) + " \n")
			.append("\t\tCL at second descent ending  = " + _liftingCoefficientMissionList.get(13) + " \n")
			.append("\t\tCD at second descent start  = " + _dragCoefficientMissionList.get(12) + " \n")
			.append("\t\tCD at second descent ending  = " + _dragCoefficientMissionList.get(13) + " \n")
			.append("\t\tEfficiency at second descent start  = " + _efficiencyMissionList.get(12) + " \n")
			.append("\t\tEfficiency at second descent ending  = " + _efficiencyMissionList.get(13) + " \n")
			.append("\t\tThrust at second descent start  = " + _thrustMissionList.get(12).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at second descent ending  = " + _thrustMissionList.get(13).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second descent start  = " + _dragMissionList.get(12).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second descent ending  = " + _dragMissionList.get(13).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at second descent start = " + _rateOfClimbMissionList.get(12).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at second descent ending  = " + _rateOfClimbMissionList.get(13).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at second descent start  = " + _climbAngleMissionList.get(12).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at second descent ending  = " + _climbAngleMissionList.get(13).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at second descent start  = " + _fuelFlowMissionList.get(12) + " lb/hr\n")
			.append("\t\tFuel flow at second descent ending  = " + _fuelFlowMissionList.get(13) + " lb/hr\n")
			.append("\t\tSFC at second descent start  = " + _sfcMissionList.get(12) + " lb/(lb*hr)\n")
			.append("\t\tSFC at second descent ending  = " + _sfcMissionList.get(13) + " lb/(lb*hr)\n");
		}
		if(_holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
				sb.append("\t\t.....................................\n")
				.append("\t\tHOLDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at holding start  = " + _speedTASMissionList.get(14).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at holding ending  = " + _speedTASMissionList.get(15).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at holding start  = " + _speedCASMissionList.get(14).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at holding ending  = " + _speedCASMissionList.get(15).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at holding start  = " + _machMissionList.get(14) + " \n")
				.append("\t\tMach at holding ending  = " + _machMissionList.get(15) + " \n")
				.append("\t\tCL at holding start  = " + _liftingCoefficientMissionList.get(14) + " \n")
				.append("\t\tCL at holding ending  = " + _liftingCoefficientMissionList.get(15) + " \n")
				.append("\t\tCD at holding start  = " + _dragCoefficientMissionList.get(14) + " \n")
				.append("\t\tCD at holding ending  = " + _dragCoefficientMissionList.get(15) + " \n")
				.append("\t\tEfficiency at holding start  = " + _efficiencyMissionList.get(14) + " \n")
				.append("\t\tEfficiency at holding ending  = " + _efficiencyMissionList.get(15) + " \n")
				.append("\t\tThrottle at holding start  = " + _throttleMissionList.get(4) + " \n")
				.append("\t\tThrottle at holding ending  = " + _throttleMissionList.get(5) + " \n")
				.append("\t\tThrust at holding start  = " + _thrustMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at holding ending  = " + _thrustMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding start  = " + _dragMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding ending  = " + _dragMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at holding start = " + _rateOfClimbMissionList.get(14).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at holding ending  = " + _rateOfClimbMissionList.get(15).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at holding start  = " + _climbAngleMissionList.get(14).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at holding ending  = " + _climbAngleMissionList.get(15).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at holding start  = " + _fuelFlowMissionList.get(14) + " lb/hr\n")
				.append("\t\tFuel flow at holding ending  = " + _fuelFlowMissionList.get(15) + " lb/hr\n")
				.append("\t\tSFC at holding start  = " + _sfcMissionList.get(14) + " lb/(lb*hr)\n")
				.append("\t\tSFC at holding ending  = " + _sfcMissionList.get(15) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tTHIRD DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at third descent start  = " + _speedTASMissionList.get(16).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at third descent ending  = " + _speedTASMissionList.get(17).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at third descent start  = " + _speedCASMissionList.get(16).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at third descent ending  = " + _speedCASMissionList.get(17).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at third descent start  = " + _machMissionList.get(16) + " \n")
				.append("\t\tMach at third descent ending  = " + _machMissionList.get(17) + " \n")
				.append("\t\tCL at third descent start  = " + _liftingCoefficientMissionList.get(16) + " \n")
				.append("\t\tCL at third descent ending  = " + _liftingCoefficientMissionList.get(17) + " \n")
				.append("\t\tCD at third descent start  = " + _dragCoefficientMissionList.get(16) + " \n")
				.append("\t\tCD at third descent ending  = " + _dragCoefficientMissionList.get(17) + " \n")
				.append("\t\tEfficiency at third descent start  = " + _efficiencyMissionList.get(16) + " \n")
				.append("\t\tEfficiency at third descent ending  = " + _efficiencyMissionList.get(17) + " \n")
				.append("\t\tThrust at third descent start  = " + _thrustMissionList.get(16).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at third descent ending  = " + _thrustMissionList.get(17).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at third descent start  = " + _dragMissionList.get(16).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at third descent ending  = " + _dragMissionList.get(17).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at third descent start = " + _rateOfClimbMissionList.get(16).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at third descent ending  = " + _rateOfClimbMissionList.get(17).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at third descent start  = " + _climbAngleMissionList.get(16).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at third descent ending  = " + _climbAngleMissionList.get(17).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at third descent start  = " + _fuelFlowMissionList.get(16) + " lb/hr\n")
				.append("\t\tFuel flow at third descent ending  = " + _fuelFlowMissionList.get(17) + " lb/hr\n")
				.append("\t\tSFC at third descent start  = " + _sfcMissionList.get(16) + " lb/(lb*hr)\n")
				.append("\t\tSFC at third descent ending  = " + _sfcMissionList.get(17) + " lb/(lb*hr)\n");
		}
		
				sb.append("\t\t.....................................\n")
				.append("\t\tLANDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at landing start  = " + _speedTASMissionList.get(18).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at landing ending  = " + _speedTASMissionList.get(19).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at landing start  = " + _speedCASMissionList.get(18).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at landing ending  = " + _speedCASMissionList.get(19).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at landing start  = " + _machMissionList.get(18) + " \n")
				.append("\t\tMach at landing ending  = " + _machMissionList.get(19) + " \n")
				.append("\t\tCL at landing start  = " + _liftingCoefficientMissionList.get(18) + " \n")
				.append("\t\tCL at landing ending  = " + _liftingCoefficientMissionList.get(19) + " \n")
				.append("\t\tCD at landing start  = " + _dragCoefficientMissionList.get(18) + " \n")
				.append("\t\tCD at landing ending  = " + _dragCoefficientMissionList.get(19) + " \n")
				.append("\t\tEfficiency at landing start  = " + _efficiencyMissionList.get(18) + " \n")
				.append("\t\tEfficiency at landing ending  = " + _efficiencyMissionList.get(19) + " \n")
				.append("\t\tThrust at landing start  = " + _thrustMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at landing ending  = " + _thrustMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing start  = " + _dragMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing ending  = " + _dragMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at landing start = " + _rateOfClimbMissionList.get(18).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at landing ending  = " + _rateOfClimbMissionList.get(19).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at landing start  = " + _climbAngleMissionList.get(18).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at landing ending  = " + _climbAngleMissionList.get(19).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at landing start  = " + _fuelFlowMissionList.get(18) + " lb/hr\n")
				.append("\t\tFuel flow at landing ending  = " + _fuelFlowMissionList.get(19) + " lb/hr\n")
				.append("\t\tSFC at landing start  = " + _sfcMissionList.get(18) + " lb/(lb*hr)\n")
				.append("\t\tSFC at landing ending  = " + _sfcMissionList.get(19) + " lb/(lb*hr)\n")
				.append("\t-------------------------------------\n")
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
		return deisngPassengersNumber;
	}

	public void setDeisngPassengersNumber(int deisngPassengersNumber) {
		this.deisngPassengersNumber = deisngPassengersNumber;
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

	public Map<MissionPhasesEnum, List<Double>> getThrottleMissionMap() {
		return throttleMissionMap;
	}

	public void setThrottleMissionMap(Map<MissionPhasesEnum, List<Double>> throttleMissionMap) {
		this.throttleMissionMap = throttleMissionMap;
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
