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
												(initialMassCruise.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))),
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
				sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());
				
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
							Amount.valueOf(
									(cruiseSteps.get(j).doubleValue(SI.METER)
											- cruiseSteps.get(j-1).doubleValue(SI.METER))
									/ cruiseSpeedList.get(j-1).doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									).to(NonSI.MINUTE)
							);
					
					fuelUsedPerStep.add(
							Amount.valueOf(
									fuelFlows.get(j-1)
									*times.get(j).doubleValue(NonSI.MINUTE),
									SI.KILOGRAM
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
							Amount.valueOf(
									emissionIndexNOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionCOCruise.add(
							Amount.valueOf(
									emissionIndexCOList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionHCCruise.add(
							Amount.valueOf(
									emissionIndexHCList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionSootCruise.add(
							Amount.valueOf(
									emissionIndexSootList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionCO2Cruise.add(
							Amount.valueOf(
									emissionIndexCO2List.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionSOxCruise.add(
							Amount.valueOf(
									emissionIndexSOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					emissionH2OCruise.add(
							Amount.valueOf(
									emissionIndexH2OList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
									*fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM),
									SI.GRAM)
							);
					
					aircraftMassPerStep.add(
							aircraftMassPerStep.get(j-1).to(SI.KILOGRAM)
							.minus(fuelUsedPerStep.get(j).to(SI.KILOGRAM))
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
					
					sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());
					
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
				theSecondClimbCalculator = new ClimbCalc(
						_theAircraft,
						_theOperatingConditions,
						_cLmaxClean, 
						_polarCLClimb,
						_polarCDClimb,
						_climbSpeed, 
						_dragDueToEnigneFailure 
						);

				aircraftMassAtSecondClimbStart = _initialMissionMass
						.minus(fuelTakeOff.to(SI.KILOGRAM))
						.minus(fuelClimb.to(SI.KILOGRAM))
						.minus(fuelCruise.to(SI.KILOGRAM))
						.minus(fuelFirstDescent.to(SI.KILOGRAM));

				theSecondClimbCalculator.calculateClimbPerformance(
						aircraftMassAtSecondClimbStart,
						aircraftMassAtSecondClimbStart,
						_holdingAltitude.to(SI.METER),
						_alternateCruiseAltitude.to(SI.METER),
						false,
						false
						);

				rangeSecondClimb = theSecondClimbCalculator.getClimbTotalRange();
				timeSecondClimb = null;
				if(_climbSpeed != null)
					timeSecondClimb = theSecondClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
				else
					timeSecondClimb = theSecondClimbCalculator.getMinimumClimbTimeAEO();
				fuelSecondClimb = theSecondClimbCalculator.getClimbTotalFuelUsed();

				aircraftMassAtSecondClimbEnding = aircraftMassAtSecondClimbStart.to(SI.KILOGRAM).minus(fuelSecondClimb.to(SI.KILOGRAM));

				if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
					speedTASAtSecondClimbStart = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(_obstacleLanding.doubleValue(SI.METER))
											.getDensityRatio()
											)
									);
					speedCASAtSecondClimbStart = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT);
					speedTASAtSecondClimbEnding = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(
													_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
													)
											.getDensityRatio()
											)
									);
					speedCASAtSecondClimbEnding = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT);

					cLAtSecondClimbStart = LiftCalc.calculateLiftCoeff(
							aircraftMassAtSecondClimbStart.times(AtmosphereCalc.g0).getEstimatedValue(),
							speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							0.0
							);
					cLAtSecondClimbEnding = LiftCalc.calculateLiftCoeff(
							aircraftMassAtSecondClimbEnding
							.times(AtmosphereCalc.g0)
							.getEstimatedValue(),
							speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
							);
					cDAtSecondClimbStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondClimbStart
							);
					cDAtSecondClimbEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondClimbEnding
							);
					thrustAtSecondClimbStart = theSecondClimbCalculator.getThrustAtClimbStart().to(NonSI.POUND_FORCE);
					thrustAtSecondClimbEnding = theSecondClimbCalculator.getThrustAtClimbEnding().to(NonSI.POUND_FORCE);
					dragAtSecondClimbStart = theSecondClimbCalculator.getDragAtClimbStart().to(NonSI.POUND_FORCE);
					dragAtSecondClimbEnding = theSecondClimbCalculator.getDragAtClimbEnding().to(NonSI.POUND_FORCE);
					rateOfClimbAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(0).getRC(),
									speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtSecondClimbEnding = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getRC(),
									speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(0).getClimbAngleList(),
									speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.RADIAN).to(NonSI.DEGREE_ANGLE);
					climbAngleAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getClimbAngleList(),
									speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.RADIAN).to(NonSI.DEGREE_ANGLE);
					fuelFlowAtSecondClimbStart = theSecondClimbCalculator.getFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtSecondClimbEnding = theSecondClimbCalculator.getFuelFlowList().get(theSecondClimbCalculator.getFuelFlowList().size()-1)*2.20462/0.016667;
					sfcAtSecondClimbStart = theSecondClimbCalculator.getSFCList().get(0);
					sfcAtSecondClimbEnding = theSecondClimbCalculator.getSFCList().get(theSecondClimbCalculator.getSFCList().size()-1);
				}

				//--------------------------------------------------------------------
				// ALTERNATE CRUISE (AT MAX EFFICIENCY)
				aircraftMassAtAlternateCruiseStart = 
						_initialMissionMass
						.minus(fuelTakeOff.to(SI.KILOGRAM))
						.minus(fuelClimb.to(SI.KILOGRAM))
						.minus(fuelCruise.to(SI.KILOGRAM))
						.minus(fuelFirstDescent.to(SI.KILOGRAM))
						.minus(fuelSecondClimb.to(SI.KILOGRAM));

				rangeAlternateCruise = _alternateCruiseLength;

				for (int iAlternate=0; iAlternate < 5; iAlternate++) {
					double[] speedArrayAlternate = MyArrayUtils.linspace(
							SpeedCalc.calculateSpeedStall(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb)
									),
							SpeedCalc.calculateTAS(
									1.0,
									_alternateCruiseAltitude.doubleValue(SI.METER)
									),
							nPointSpeed
							);

					List<DragMap> dragListAlternate = new ArrayList<>();
					dragListAlternate.add(
							DragCalc.calculateDragAndPowerRequired(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									speedArrayAlternate,
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListAlternate = new ArrayList<>();
					thrustListAlternate.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									1.0,
									speedArrayAlternate,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineType(), 
									_theAircraft.getPowerPlant(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							);

					List<DragThrustIntersectionMap> intersectionListAlternate = new ArrayList<>();
					intersectionListAlternate.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									speedArrayAlternate,
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									1.0,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									dragListAlternate,
									thrustListAlternate
									)
							);

					if(intersectionListAlternate.get(0).getMaxSpeed() < 0.01) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Amount<Mass>> aircraftMassPerStepAlternateCruise = new ArrayList<>();
					aircraftMassPerStepAlternateCruise.add(aircraftMassAtAlternateCruiseStart);

					List<Double> rangeFactorAlternateCruiseList = new ArrayList<>();
					double[] cLRangeAlternateCruiseArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(_polarCLCruise),
							50
							); 
					for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							rangeFactorAlternateCruiseList.add(
									Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
											MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
											cLRangeAlternateCruiseArray[iCL]
											)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

							rangeFactorAlternateCruiseList.add(
									cLRangeAlternateCruiseArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLRangeAlternateCruiseArray[iCL]
													)
									);

						}
					}

					int iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
					double bestMachAlternateCruise = SpeedCalc.calculateMach(
							_alternateCruiseAltitude.doubleValue(SI.METER), 
							SpeedCalc.calculateSpeedAtCL(
									aircraftMassPerStepAlternateCruise.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
									AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER)),
									cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
									)
							);

					List<Double> alternateCruiseMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> alternateCruiseSpeedList = new ArrayList<>();
					if(bestMachAlternateCruise <= intersectionListAlternate.get(0).getMaxMach()) {
						alternateCruiseMachNumberList.add(bestMachAlternateCruise);
						alternateCruiseSpeedList.add(
								Amount.valueOf(
										SpeedCalc.calculateTAS(
												bestMachAlternateCruise,
												_alternateCruiseAltitude.doubleValue(SI.METER)
												),
										SI.METERS_PER_SECOND).to(NonSI.KNOT)
								);
					}
					else {
						alternateCruiseMachNumberList.add(intersectionListAlternate.get(0).getMaxMach());
						alternateCruiseSpeedList.add(
								Amount.valueOf(
										intersectionListAlternate.get(0).getMaxSpeed(),
										SI.METERS_PER_SECOND
										).to(NonSI.KNOT)
								);
						if(alternateCruiseBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							alternateCruiseBestMachNumberErrorFlag = true;
						}
					}

					double[] alternateCruiseSteps = MyArrayUtils.linspace(
							0.0,
							rangeAlternateCruise.doubleValue(SI.METER),
							5
							);

					List<Double> cLStepsAlternateCruise = new ArrayList<>();
					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepAlternateCruise.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_alternateCruiseAltitude.doubleValue(SI.METER)
									)
							);

					List<Amount<Force>> dragPerStepAlternateCruise = new ArrayList<>();
					dragPerStepAlternateCruise.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStepAlternateCruise.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_alternateCruiseAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLStepsAlternateCruise.get(0))
//											+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
//													+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
//													-0.003767654434394
//													)
											),
									SI.NEWTON
									)
							);

					List<Double> phiAlternateCruise = new ArrayList<>();
					phiAlternateCruise.add(dragPerStepAlternateCruise.get(0).to(SI.NEWTON)
							.divide(
									ThrustCalc.calculateThrustDatabase(
											_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
											_theAircraft.getPowerPlant().getEngineNumber(),
											1.0,
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.CRUISE,
											_theAircraft.getPowerPlant(),
											_alternateCruiseAltitude.doubleValue(SI.METER), 
											alternateCruiseMachNumberList.get(0)
											)
									)
							.getEstimatedValue()
							);

					List<Double> fuelFlowsAlternateCruise = new ArrayList<>();
					if(phiAlternateCruise.get(0) > 1.0) {
						phiAlternateCruise.remove(0);
						phiAlternateCruise.add(0, 1.0);
					}
					if(_calculateSFCAlternateCruise)
						fuelFlowsAlternateCruise.add(
								dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*(2-phiAlternateCruise.get(0))
								*EngineDatabaseManager_old.getSFC(
										alternateCruiseMachNumberList.get(0),
										_alternateCruiseAltitude.doubleValue(SI.METER), 
										EngineDatabaseManager_old.getThrustRatio(
												alternateCruiseMachNumberList.get(0),
												_alternateCruiseAltitude.doubleValue(SI.METER), 
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(), 
												EngineOperatingConditionEnum.CRUISE, 
												_theAircraft.getPowerPlant()
												),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(), 
										EngineOperatingConditionEnum.CRUISE, 
										_theAircraft.getPowerPlant()
										)
								);
					else
						fuelFlowsAlternateCruise.add(
								dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(0))
								);

					List<Amount<Duration>> timesAlternateCruise = new ArrayList<>(); 
					timesAlternateCruise.add(
							Amount.valueOf(
									(alternateCruiseSteps[1]-alternateCruiseSteps[0])
									/alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									).to(NonSI.MINUTE)
							);

					List<Amount<Mass>> fuelUsedPerStepAlternateCruise = new ArrayList<>();
					fuelUsedPerStepAlternateCruise.add(
							Amount.valueOf(
									fuelFlowsAlternateCruise.get(0)
									*timesAlternateCruise.get(0).doubleValue(NonSI.MINUTE),
									SI.KILOGRAM
									)
							);

					for (int j=1; j<alternateCruiseSteps.length-1; j++) {

						aircraftMassPerStepAlternateCruise.add(
								aircraftMassPerStepAlternateCruise.get(j-1)
								.minus(Amount.valueOf(
										fuelUsedPerStepAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
										SI.KILOGRAM)
										)
								);

						dragListAlternate.add(
								DragCalc.calculateDragAndPowerRequired(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										(aircraftMassPerStepAlternateCruise.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										speedArrayAlternate,
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
										MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
										_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListAlternate.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										1.0,
										speedArrayAlternate,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineType(), 
										_theAircraft.getPowerPlant(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
										_theAircraft.getPowerPlant().getEngineNumber(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
										)
								);

						intersectionListAlternate.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										speedArrayAlternate,
										(aircraftMassPerStepAlternateCruise.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										1.0,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										dragListAlternate,
										thrustListAlternate
										)
								);

						if(intersectionListAlternate.get(j).getMaxSpeed() < 0.01) {
							_missionProfileStopped = Boolean.TRUE;
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
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLRangeAlternateCruiseArray[iCL]
												)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

								rangeFactorAlternateCruiseList.add(
										cLRangeAlternateCruiseArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
														MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
														cLRangeAlternateCruiseArray[iCL]
														)
										);

							}
						}

						iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
						bestMachAlternateCruise = SpeedCalc.calculateMach(
								_alternateCruiseAltitude.doubleValue(SI.METER), 
								SpeedCalc.calculateSpeedAtCL(
										aircraftMassPerStepAlternateCruise.get(j).times(AtmosphereCalc.g0).getEstimatedValue(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER)),
										cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
										)
								);

						if(bestMachAlternateCruise <= intersectionListAlternate.get(j).getMaxMach()) {
							alternateCruiseMachNumberList.add(bestMachAlternateCruise);
							alternateCruiseSpeedList.add(
									Amount.valueOf(
											SpeedCalc.calculateTAS(
													bestMachAlternateCruise,
													_alternateCruiseAltitude.doubleValue(SI.METER)
													),
											SI.METERS_PER_SECOND).to(NonSI.KNOT)
									);
						}
						else {
							alternateCruiseMachNumberList.add(intersectionListAlternate.get(j).getMaxMach());
							alternateCruiseSpeedList.add(
									Amount.valueOf(
											intersectionListAlternate.get(j).getMaxSpeed(),
											SI.METERS_PER_SECOND
											).to(NonSI.KNOT)
									);
							if(alternateCruiseBestMachNumberErrorFlag == false) {
								System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
								alternateCruiseBestMachNumberErrorFlag = true;
							}
						}

						cLStepsAlternateCruise.add(
								LiftCalc.calculateLiftCoeff(
										aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										_alternateCruiseAltitude.doubleValue(SI.METER)
										)
								);
						dragPerStepAlternateCruise.add(
								Amount.valueOf(
										DragCalc.calculateDragAtSpeed(
												aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												_alternateCruiseAltitude.doubleValue(SI.METER),
												_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
												MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
														MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
														cLStepsAlternateCruise.get(j))
//												+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
//														+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
//														-0.003767654434394
//														)
												),
										SI.NEWTON
										)
								);

						phiAlternateCruise.add(dragPerStepAlternateCruise.get(j).to(SI.NEWTON)
								.divide(
										ThrustCalc.calculateThrustDatabase(
												_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
												_theAircraft.getPowerPlant().getEngineNumber(),
												1.0,
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(),
												EngineOperatingConditionEnum.CRUISE,
												_theAircraft.getPowerPlant(),
												_alternateCruiseAltitude.doubleValue(SI.METER), 
												alternateCruiseMachNumberList.get(j)
												)
										)
								.getEstimatedValue()
								);

						if(phiAlternateCruise.get(j) > 1.0) {
							phiAlternateCruise.remove(j);
							phiAlternateCruise.add(j, 1.0);
						}

						if(_calculateSFCAlternateCruise)
							fuelFlowsAlternateCruise.add(
									dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*(2-phiAlternateCruise.get(j))
									*EngineDatabaseManager_old.getSFC(
											alternateCruiseMachNumberList.get(j),
											_alternateCruiseAltitude.doubleValue(SI.METER), 
											EngineDatabaseManager_old.getThrustRatio(
													alternateCruiseMachNumberList.get(j),
													_alternateCruiseAltitude.doubleValue(SI.METER), 
													_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
													_theAircraft.getPowerPlant().getEngineType(), 
													EngineOperatingConditionEnum.CRUISE, 
													_theAircraft.getPowerPlant()
													),
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(), 
											EngineOperatingConditionEnum.CRUISE, 
											_theAircraft.getPowerPlant()
											)
									);
						else
							fuelFlowsAlternateCruise.add(
									dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(j))
									);

						timesAlternateCruise.add(
								Amount.valueOf(
										(alternateCruiseSteps[j]-alternateCruiseSteps[j-1])
										/alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										SI.SECOND
										).to(NonSI.MINUTE)
								);

						fuelUsedPerStepAlternateCruise.add(
								Amount.valueOf(
										fuelFlowsAlternateCruise.get(j)
										*timesAlternateCruise.get(j).doubleValue(NonSI.MINUTE),
										SI.KILOGRAM
										)
								);
					}

					timeAlternateCruise =
							Amount.valueOf(
									timesAlternateCruise.stream()
									.mapToDouble( t -> t.doubleValue(NonSI.MINUTE))
									.sum(),
									NonSI.MINUTE
									);
					fuelAlternateCruise =
							Amount.valueOf(
									fuelUsedPerStepAlternateCruise.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);
					aircraftMassAtAlternateCruiseEnding = aircraftMassAtAlternateCruiseStart.to(SI.KILOGRAM).minus(fuelAlternateCruise.to(SI.KILOGRAM));

					if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
						speedTASAtAlternateCruiseStart = alternateCruiseSpeedList.get(0).to(NonSI.KNOT);
						speedTASAtAlternateCruiseEnding = alternateCruiseSpeedList.get(alternateCruiseSpeedList.size()-1).to(NonSI.KNOT);
						speedCASAtAlternateCruiseStart = alternateCruiseSpeedList.get(0).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						speedCASAtAlternateCruiseEnding = alternateCruiseSpeedList.get(alternateCruiseSpeedList.size()-1).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						cLAtAlternateCruiseStart = cLStepsAlternateCruise.get(0);
						cLAtAlternateCruiseEnding = cLStepsAlternateCruise.get(cLStepsAlternateCruise.size()-1);
						cDAtAlternateCruiseStart = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								cLAtAlternateCruiseStart
								);
						cDAtAlternateCruiseEnding = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								cLAtAlternateCruiseEnding
								);
						dragAtAlternateCruiseStart = dragPerStepAlternateCruise.get(0).to(NonSI.POUND_FORCE);
						dragAtAlternateCruiseEnding = dragPerStepAlternateCruise.get(dragPerStepAlternateCruise.size()-1).to(NonSI.POUND_FORCE);
						thrustAtAlternateCruiseStart = dragAtAlternateCruiseStart;
						thrustAtAlternateCruiseEnding = dragAtAlternateCruiseEnding;
						throttleAlternateCruiseStart = phiAlternateCruise.get(0);
						throttleAlternateCruiseEnding = phiAlternateCruise.get(phiAlternateCruise.size()-1);
						rateOfClimbAtAlternateCruiseStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						rateOfClimbAtAlternateCruiseEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						climbAngleAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						climbAngleAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						fuelFlowAtAlternateCruiseStart = fuelFlowsAlternateCruise.get(0)*2.20462/0.016667;
						fuelFlowAtAlternateCruiseEnding = fuelFlowsAlternateCruise.get(fuelFlowsAlternateCruise.size()-1)*2.20462/0.016667;
						sfcAtAlternateCruiseStart = fuelFlowAtAlternateCruiseStart/thrustAtAlternateCruiseStart.doubleValue(NonSI.POUND_FORCE);
						sfcAtAlternateCruiseEnding = fuelFlowAtAlternateCruiseEnding/thrustAtAlternateCruiseEnding.doubleValue(NonSI.POUND_FORCE);
					}

					//--------------------------------------------------------------------
					// DESCENT (up to HOLDING altitude)
					aircraftMassAtSecondDescentStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM));

					theSecondDescentCalculator = new DescentCalc(
							_theAircraft,
							_speedDescentCAS,
							_rateOfDescent,
							_alternateCruiseAltitude.to(SI.METER),
							_holdingAltitude.to(SI.METER),
							aircraftMassAtSecondDescentStart,
							_polarCLClimb,
							_polarCDClimb
							);

					theSecondDescentCalculator.calculateDescentPerformance();

					rangeSecondDescent = theSecondDescentCalculator.getTotalDescentLength().to(NonSI.NAUTICAL_MILE);
					timeSecondDescent = theSecondDescentCalculator.getTotalDescentTime().to(NonSI.MINUTE);
					fuelSecondDescent = theSecondDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);
					aircraftMassAtSecondDescentEnding = aircraftMassAtSecondDescentStart.to(SI.KILOGRAM).minus(fuelSecondDescent.to(SI.KILOGRAM));

					speedTASAtSecondDescentStart = theSecondDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
					speedTASAtSecondDescentEnding = theSecondDescentCalculator.getSpeedListTAS()
							.get(theSecondDescentCalculator.getSpeedListTAS().size()-1)
							.to(NonSI.KNOT);
					speedCASAtSecondDescentStart = theSecondDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					speedCASAtSecondDescentEnding = theSecondDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					cLAtSecondDescentStart = theSecondDescentCalculator.getCLSteps().get(0);
					cLAtSecondDescentEnding = theSecondDescentCalculator.getCLSteps().get(theSecondDescentCalculator.getCLSteps().size()-1);
					cDAtSecondDescentStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondDescentStart
							);
					cDAtSecondDescentEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondDescentEnding
							);
					thrustAtSecondDescentStart = theSecondDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
					thrustAtSecondDescentEnding = theSecondDescentCalculator.getThrustPerStep()
							.get(theSecondDescentCalculator.getThrustPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					dragAtSecondDescentStart = theSecondDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
					dragAtSecondDescentEnding = theSecondDescentCalculator.getDragPerStep()
							.get(theSecondDescentCalculator.getDragPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					rateOfClimbAtSecondDescentStart = theSecondDescentCalculator.getRateOfDescentList().get(0).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtSecondDescentEnding = theSecondDescentCalculator.getRateOfDescentList()
							.get(theSecondDescentCalculator.getRateOfDescentList().size()-1)
							.to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtSecondDescentStart = theSecondDescentCalculator.getDescentAngles().get(0).to(NonSI.DEGREE_ANGLE);
					climbAngleAtSecondDescentEnding = theSecondDescentCalculator.getDescentAngles()
							.get(theSecondDescentCalculator.getDescentAngles().size()-1)
							.to(NonSI.DEGREE_ANGLE);
					fuelFlowAtSecondDescentStart = theSecondDescentCalculator.getInterpolatedFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtSecondDescentEnding = theSecondDescentCalculator.getInterpolatedFuelFlowList()
							.get(theSecondDescentCalculator.getInterpolatedFuelFlowList().size()-1)
							*2.20462/0.016667;
					sfcAtSecondDescentStart = fuelFlowAtSecondDescentStart/thrustAtSecondDescentStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtSecondDescentEnding = fuelFlowAtSecondDescentEnding/thrustAtSecondDescentEnding.doubleValue(NonSI.POUND_FORCE);

					//--------------------------------------------------------------------
					// HOLDING (BEST ENDURANCE)
					aircraftMassAtHoldingStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM));

					double[] timeHoldingArray = MyArrayUtils.linspace(
							0.0,
							_holdingDuration.doubleValue(NonSI.MINUTE),
							5
							);

					double[] speedArrayHolding = MyArrayUtils.linspace(
							SpeedCalc.calculateSpeedStall(
									_holdingAltitude.doubleValue(SI.METER),
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb)
									),
							SpeedCalc.calculateTAS(
									1.0,
									_holdingAltitude.doubleValue(SI.METER)
									),
							nPointSpeed
							);

					List<DragMap> dragListHolding = new ArrayList<>();
					dragListHolding.add(
							DragCalc.calculateDragAndPowerRequired(
									_holdingAltitude.doubleValue(SI.METER),
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									speedArrayHolding,
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListHolding = new ArrayList<>();
					thrustListHolding.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_holdingAltitude.doubleValue(SI.METER),
									1.0,
									speedArrayHolding,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineType(), 
									_theAircraft.getPowerPlant(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							);

					List<DragThrustIntersectionMap> intersectionListHolding = new ArrayList<>();
					intersectionListHolding.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									_holdingAltitude.doubleValue(SI.METER),
									speedArrayHolding,
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									1.0,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									dragListHolding,
									thrustListHolding
									)
							);

					if(intersectionListHolding.get(0).getMaxSpeed() < 0.01) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (HOLDING - MISSION PROFILE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Amount<Mass>> aircraftMassPerStepHolding = new ArrayList<>();
					aircraftMassPerStepHolding.add(aircraftMassAtHoldingStart);

					List<Double> enduranceFactorHoldingList = new ArrayList<>();
					double[] cLEnduranceHoldingArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(_polarCLClimb),
							50
							); 
					for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							enduranceFactorHoldingList.add(
									cLEnduranceHoldingArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
													MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
													cLEnduranceHoldingArray[iCL]
													)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

							enduranceFactorHoldingList.add(
									Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
											MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
											cLEnduranceHoldingArray[iCL]
											)
									);

						}
					}

					int iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
					double bestMachHolding = SpeedCalc.calculateMach(
							_holdingAltitude.doubleValue(SI.METER), 
							SpeedCalc.calculateSpeedAtCL(
									aircraftMassPerStepHolding.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
									AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER)),
									cLEnduranceHoldingArray[iBestSpeedHolding]
									)
							);

					List<Double> holdingMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> holdingSpeedList = new ArrayList<>();
					if(bestMachHolding <= intersectionListHolding.get(0).getMaxMach()) {
						holdingMachNumberList.add(bestMachHolding);
						holdingSpeedList.add(
								Amount.valueOf(
										SpeedCalc.calculateTAS(
												bestMachHolding,
												_holdingAltitude.doubleValue(SI.METER)
												),
										SI.METERS_PER_SECOND).to(NonSI.KNOT)
								);
					}
					else {
						holdingMachNumberList.add(intersectionListHolding.get(0).getMaxMach());
						holdingSpeedList.add(
								Amount.valueOf(
										intersectionListHolding.get(0).getMaxSpeed(),
										SI.METERS_PER_SECOND
										).to(NonSI.KNOT)
								);
						System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
					}

					List<Double> cLStepsHolding = new ArrayList<>();
					cLStepsHolding.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									holdingSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_holdingAltitude.doubleValue(SI.METER)
									)
							);

					List<Amount<Force>> dragPerStepHolding = new ArrayList<>();
					dragPerStepHolding.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_holdingAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											holdingSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
													MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
													cLStepsHolding.get(0))
											),
									SI.NEWTON
									)
							);

					List<Double> phiHolding = new ArrayList<>();
					phiHolding.add(dragPerStepHolding.get(0).to(SI.NEWTON)
							.divide(
									ThrustCalc.calculateThrustDatabase(
											_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
											_theAircraft.getPowerPlant().getEngineNumber(),
											1.0,
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.CRUISE,
											_theAircraft.getPowerPlant(),
											_holdingAltitude.doubleValue(SI.METER), 
											holdingMachNumberList.get(0)
											)
									)
							.getEstimatedValue()
							);

					List<Double> fuelFlowsHolding = new ArrayList<>();
					if(phiHolding.get(0) > 1.0) {
						phiHolding.remove(0);
						phiHolding.add(0, 1.0);
					}
					if(_calculateSFCHolding)
						fuelFlowsHolding.add(
								dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*(2-phiHolding.get(0))
								*EngineDatabaseManager_old.getSFC(
										holdingMachNumberList.get(0),
										_holdingAltitude.doubleValue(SI.METER), 
										EngineDatabaseManager_old.getThrustRatio(
												holdingMachNumberList.get(0),
												_holdingAltitude.doubleValue(SI.METER), 
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(), 
												EngineOperatingConditionEnum.CRUISE, 
												_theAircraft.getPowerPlant()
												),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(), 
										EngineOperatingConditionEnum.CRUISE, 
										_theAircraft.getPowerPlant()
										)
								);
					else
						fuelFlowsHolding.add(
								dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionHolding.value(phiHolding.get(0))
								);

					List<Amount<Mass>> fuelUsedPerStepHolding = new ArrayList<>();
					fuelUsedPerStepHolding.add(
							Amount.valueOf(
									fuelFlowsHolding.get(0)
									*(timeHoldingArray[1]-timeHoldingArray[0]),
									SI.KILOGRAM
									)
							);

					for(int j=1; j<timeHoldingArray.length-1; j++) {

						aircraftMassPerStepHolding.add(
								aircraftMassPerStepHolding.get(j-1)
								.minus(Amount.valueOf(
										fuelUsedPerStepHolding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
										SI.KILOGRAM)
										)
								);

						dragListHolding.add(
								DragCalc.calculateDragAndPowerRequired(
										_holdingAltitude.doubleValue(SI.METER),
										(aircraftMassPerStepHolding.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										speedArrayHolding,
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
										_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListHolding.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										_holdingAltitude.doubleValue(SI.METER),
										1.0,
										speedArrayHolding,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineType(), 
										_theAircraft.getPowerPlant(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
										_theAircraft.getPowerPlant().getEngineNumber(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
										)
								);

						intersectionListHolding.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										_holdingAltitude.doubleValue(SI.METER),
										speedArrayHolding,
										(aircraftMassPerStepHolding.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										1.0,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										dragListHolding,
										thrustListHolding
										)
								);

						if(intersectionListHolding.get(j).getMaxSpeed() < 0.01) {
							_missionProfileStopped = Boolean.TRUE;
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
														MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
														MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
														cLEnduranceHoldingArray[iCL]
														)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

								enduranceFactorHoldingList.add(
										Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
												MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
												cLEnduranceHoldingArray[iCL]
												)
										);

							}
						}

						iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
						bestMachHolding = SpeedCalc.calculateMach(
								_holdingAltitude.doubleValue(SI.METER), 
								SpeedCalc.calculateSpeedAtCL(
										aircraftMassPerStepHolding.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER)),
										cLEnduranceHoldingArray[iBestSpeedHolding]
										)
								);

						if(bestMachHolding <= intersectionListHolding.get(j).getMaxMach()) {
							holdingMachNumberList.add(bestMachHolding);
							holdingSpeedList.add(
									Amount.valueOf(
											SpeedCalc.calculateTAS(
													bestMachHolding,
													_holdingAltitude.doubleValue(SI.METER)
													),
											SI.METERS_PER_SECOND).to(NonSI.KNOT)
									);
						}
						else {
							holdingMachNumberList.add(intersectionListHolding.get(j).getMaxMach());
							holdingSpeedList.add(
									Amount.valueOf(
											intersectionListHolding.get(j).getMaxSpeed(),
											SI.METERS_PER_SECOND
											).to(NonSI.KNOT)
									);
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						}

						cLStepsHolding.add(
								LiftCalc.calculateLiftCoeff(
										aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										holdingSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										_holdingAltitude.doubleValue(SI.METER)
										)
								);
						dragPerStepHolding.add(
								Amount.valueOf(
										DragCalc.calculateDragAtSpeed(
												aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												_holdingAltitude.doubleValue(SI.METER),
												_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												holdingSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
												MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
														MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
														cLStepsHolding.get(j))
												),
										SI.NEWTON
										)
								);

						phiHolding.add(dragPerStepHolding.get(j).to(SI.NEWTON)
								.divide(
										ThrustCalc.calculateThrustDatabase(
												_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
												_theAircraft.getPowerPlant().getEngineNumber(),
												1.0,
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(),
												EngineOperatingConditionEnum.CRUISE,
												_theAircraft.getPowerPlant(),
												_holdingAltitude.doubleValue(SI.METER), 
												holdingMachNumberList.get(j)
												)
										)
								.getEstimatedValue()
								);

						if(phiHolding.get(j) > 1.0) {
							phiHolding.remove(j);
							phiHolding.add(j, 1.0);
						}

						if(_calculateSFCHolding)
							fuelFlowsHolding.add(
									dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*(2-phiHolding.get(j))
									*EngineDatabaseManager_old.getSFC(
											holdingMachNumberList.get(j),
											_holdingAltitude.doubleValue(SI.METER), 
											EngineDatabaseManager_old.getThrustRatio(
													holdingMachNumberList.get(j),
													_holdingAltitude.doubleValue(SI.METER), 
													_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
													_theAircraft.getPowerPlant().getEngineType(), 
													EngineOperatingConditionEnum.CRUISE, 
													_theAircraft.getPowerPlant()
													),
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(), 
											EngineOperatingConditionEnum.CRUISE, 
											_theAircraft.getPowerPlant()
											)
									);
						else
							fuelFlowsHolding.add(
									dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*_sfcFunctionHolding.value(phiHolding.get(j))
									);

						fuelUsedPerStepHolding.add(
								Amount.valueOf(
										fuelFlowsHolding.get(j)
										*(timeHoldingArray[j+1]-timeHoldingArray[j]),
										SI.KILOGRAM
										)
								);
					}

					rangeHolding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					timeHolding = _holdingDuration.to(NonSI.MINUTE);
					fuelHolding =
							Amount.valueOf(
									fuelUsedPerStepHolding.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);
					aircraftMassAtHoldingEnding = aircraftMassAtHoldingStart.to(SI.KILOGRAM).minus(fuelHolding.to(SI.KILOGRAM));

					if (_holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
						speedTASAtHoldingStart = holdingSpeedList.get(0).to(NonSI.KNOT);
						speedTASAtHoldingEnding = holdingSpeedList.get(holdingSpeedList.size()-1).to(NonSI.KNOT);
						speedCASAtHoldingStart = holdingSpeedList.get(0).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						speedCASAtHoldingEnding = holdingSpeedList.get(holdingSpeedList.size()-1).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						cLAtHoldingStart = cLStepsHolding.get(0);
						cLAtHoldingEnding = cLStepsHolding.get(cLStepsHolding.size()-1);
						cDAtHoldingStart = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
								cLAtHoldingStart
								);
						cDAtHoldingEnding = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
								cLAtHoldingEnding
								);
						dragAtHoldingStart = dragPerStepHolding.get(0).to(NonSI.POUND_FORCE);
						dragAtHoldingEnding = dragPerStepHolding.get(dragPerStepHolding.size()-1).to(NonSI.POUND_FORCE);
						thrustAtHoldingStart = dragAtHoldingStart;
						thrustAtHoldingEnding = dragAtHoldingEnding;
						throttleHoldingStart = phiHolding.get(0);
						throttleHoldingEnding = phiHolding.get(phiHolding.size()-1);
						rateOfClimbAtHoldingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						rateOfClimbAtHoldingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						climbAngleAtHoldingStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						climbAngleAtHoldingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						fuelFlowAtHoldingStart = fuelFlowsHolding.get(0)*2.20462/0.016667;
						fuelFlowAtHoldingEnding = fuelFlowsHolding.get(fuelFlowsHolding.size()-1)*2.20462/0.016667;
						sfcAtHoldingStart = fuelFlowAtHoldingStart/thrustAtHoldingStart.doubleValue(NonSI.POUND_FORCE);
						sfcAtHoldingEnding = fuelFlowAtHoldingEnding/thrustAtHoldingEnding.doubleValue(NonSI.POUND_FORCE);
					}

					//--------------------------------------------------------------------
					// DESCENT (up to LANDING altitude)
					aircraftMassAtThirdDescentStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM))
							.minus(fuelHolding.to(SI.KILOGRAM));

					theThirdDescentCalculator = new DescentCalc(
							_theAircraft,
							_speedDescentCAS,
							_rateOfDescent,
							_holdingAltitude.to(SI.METER),
							Amount.valueOf(15.24, SI.METER),
							aircraftMassAtThirdDescentStart,
							_polarCLClimb,
							_polarCDClimb
							);

					theThirdDescentCalculator.calculateDescentPerformance();

					rangeThirdDescent = theThirdDescentCalculator.getTotalDescentLength().to(NonSI.NAUTICAL_MILE);
					timeThirdDescent = theThirdDescentCalculator.getTotalDescentTime().to(NonSI.MINUTE);
					fuelThirdDescent = theThirdDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);
					aircraftMassAtThirdDescentEnding = aircraftMassAtThirdDescentStart.to(SI.KILOGRAM).minus(fuelThirdDescent.to(SI.KILOGRAM));

					speedTASAtThirdDescentStart = theThirdDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
					speedTASAtThirdDescentEnding = theThirdDescentCalculator.getSpeedListTAS()
							.get(theThirdDescentCalculator.getSpeedListTAS().size()-1)
							.to(NonSI.KNOT);
					speedCASAtThirdDescentStart = theThirdDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					speedCASAtThirdDescentEnding = theThirdDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					cLAtThirdDescentStart = theThirdDescentCalculator.getCLSteps().get(0);
					cLAtThirdDescentEnding = theThirdDescentCalculator.getCLSteps().get(theThirdDescentCalculator.getCLSteps().size()-1);
					cDAtThirdDescentStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtThirdDescentStart
							);
					cDAtThirdDescentEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtThirdDescentEnding
							);
					thrustAtThirdDescentStart = theThirdDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
					thrustAtThirdDescentEnding = theThirdDescentCalculator.getThrustPerStep()
							.get(theThirdDescentCalculator.getThrustPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					dragAtThirdDescentStart = theThirdDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
					dragAtThirdDescentEnding = theThirdDescentCalculator.getDragPerStep()
							.get(theThirdDescentCalculator.getDragPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					rateOfClimbAtThirdDescentStart = theThirdDescentCalculator.getRateOfDescentList().get(0).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtThirdDescentEnding = theThirdDescentCalculator.getRateOfDescentList()
							.get(theThirdDescentCalculator.getRateOfDescentList().size()-1)
							.to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtThirdDescentStart = theThirdDescentCalculator.getDescentAngles().get(0).to(NonSI.DEGREE_ANGLE);
					climbAngleAtThirdDescentEnding = theThirdDescentCalculator.getDescentAngles()
							.get(theThirdDescentCalculator.getDescentAngles().size()-1)
							.to(NonSI.DEGREE_ANGLE);
					fuelFlowAtThirdDescentStart = theThirdDescentCalculator.getInterpolatedFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtThirdDescentEnding = theThirdDescentCalculator.getInterpolatedFuelFlowList()
							.get(theThirdDescentCalculator.getInterpolatedFuelFlowList().size()-1)
							*2.20462/0.016667;
					sfcAtThirdDescentStart = fuelFlowAtThirdDescentStart/thrustAtThirdDescentStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtThirdDescentEnding = fuelFlowAtThirdDescentEnding/thrustAtThirdDescentEnding.doubleValue(NonSI.POUND_FORCE);

					//--------------------------------------------------------------------
					// LANDING
					aircraftMassAtLandingStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM))
							.minus(fuelHolding.to(SI.KILOGRAM))
							.minus(fuelThirdDescent.to(SI.KILOGRAM));

					theLandingCalculator = new LandingCalcSemiempirical(
							_theAircraft, 
							_theOperatingConditions,
							aircraftMassAtLandingStart,
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
							_theOperatingConditions.getThrottleGroundIdleLanding(),
							_freeRollDuration,
							_polarCLLanding,
							_polarCDLanding
							);

					theLandingCalculator.calculateLandingDistance();

					rangeLanding = theLandingCalculator.getsTotal().to(NonSI.NAUTICAL_MILE);			
					timeLanding = theLandingCalculator.getTime().get(theLandingCalculator.getTime().size()-1);
					fuelLanding = Amount.valueOf(
							timeLanding.to(NonSI.MINUTE).times(_landingFuelFlow).getEstimatedValue(),
							SI.KILOGRAM
							);
					aircraftMassAtLandingEnding = aircraftMassAtLandingStart.to(SI.KILOGRAM).minus(fuelLanding.to(SI.KILOGRAM));

					speedTASAtLandingStart = theLandingCalculator.getSpeed()
							.get(0)
							.to(NonSI.KNOT);
					speedCASAtLandingStart = theLandingCalculator.getSpeed()
							.get(0)
							.to(NonSI.KNOT)
							.times(
									Math.sqrt(
											AtmosphereCalc.getDensity(_obstacleLanding.doubleValue(SI.METER))
											/_theOperatingConditions.getDensityLanding().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
											)
									);
					speedTASAtLandingEnding = theLandingCalculator.getSpeed()
							.get(theLandingCalculator.getSpeed().size()-1)
							.to(NonSI.KNOT);
					speedCASAtLandingEnding = theLandingCalculator.getSpeed()
							.get(theLandingCalculator.getSpeed().size()-1)
							.to(NonSI.KNOT)
							.times(
									Math.sqrt(
											AtmosphereCalc.getDensity(_obstacleLanding.doubleValue(SI.METER))
											/_theOperatingConditions.getDensityLanding().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
											)
									);
					cLAtLandingStart = LiftCalc.calculateLiftCoeff(
							aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue(),
							theLandingCalculator.getvA().doubleValue(SI.METERS_PER_SECOND),
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_obstacleLanding.doubleValue(SI.METER)
							);
					cLAtLandingEnding = theLandingCalculator.getcLground();
					cDAtLandingStart = 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_polarCLLanding),
									MyArrayUtils.convertToDoublePrimitive(_polarCDLanding),
									cLAtLandingStart
									);
					cDAtLandingEnding = 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_polarCLLanding),
									MyArrayUtils.convertToDoublePrimitive(_polarCDLanding),
									cLAtLandingEnding
									);
					rateOfClimbAtLandingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtLandingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
					climbAngleAtLandingStart = Amount.valueOf(-3.0, NonSI.DEGREE_ANGLE);
					climbAngleAtLandingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
					dragAtLandingStart = Amount.valueOf( 
							DragCalc.calculateDragAtSpeed(
									aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue(), 
									_obstacleLanding.doubleValue(SI.METER),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									theLandingCalculator.getvA().doubleValue(SI.METERS_PER_SECOND),
									cDAtLandingStart
									),
							SI.NEWTON
							).to(NonSI.POUND_FORCE);
					dragAtLandingEnding = theLandingCalculator.getDrag()
							.get(theLandingCalculator.getDrag().size()-1)
							.to(NonSI.POUND_FORCE);
					thrustAtLandingStart = Amount.valueOf(
							(climbAngleAtLandingStart.doubleValue(SI.RADIAN)
									*aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue())
							+ (cDAtLandingStart/cLAtLandingStart),
							SI.NEWTON
							).to(NonSI.POUND_FORCE);
					thrustAtLandingEnding = theLandingCalculator.getThrust()
							.get(theLandingCalculator.getThrust().size()-1)
							.to(NonSI.POUND_FORCE);
					fuelFlowAtLandingStart = _landingFuelFlow*2.20462/0.016667;
					fuelFlowAtLandingEnding = _landingFuelFlow*2.20462/0.016667;
					sfcAtLandingStart = fuelFlowAtLandingStart/thrustAtLandingStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtLandingEnding = fuelFlowAtLandingEnding/thrustAtLandingEnding.doubleValue(NonSI.POUND_FORCE);

					//.....................................................................
					// NEW ITERATION ALTERNATE CRUISE LENGTH
					rangeAlternateCruise = rangeAlternateCruise.to(NonSI.NAUTICAL_MILE).plus( 
							_alternateCruiseLength.to(NonSI.NAUTICAL_MILE)
							.minus(rangeSecondClimb.to(NonSI.NAUTICAL_MILE)
									.plus(rangeAlternateCruise.to(NonSI.NAUTICAL_MILE))
									.plus(rangeSecondDescent.to(NonSI.NAUTICAL_MILE))
									.plus(rangeHolding.to(NonSI.NAUTICAL_MILE))
									.plus(rangeThirdDescent.to(NonSI.NAUTICAL_MILE))
									)
							);
					if(rangeAlternateCruise.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
						return;
					}
				}
				
				//.....................................................................
				// NEW ITERATION CRUISE LENGTH
				rangeCruise = rangeCruise.to(NonSI.NAUTICAL_MILE).plus( 
						_missionRange.to(NonSI.NAUTICAL_MILE)
						.minus(rangeTakeOff.to(NonSI.NAUTICAL_MILE)
								.plus(rangeClimb.to(NonSI.NAUTICAL_MILE))
								.plus(rangeCruise.to(NonSI.NAUTICAL_MILE))
								.plus(rangeFirstDescent.to(NonSI.NAUTICAL_MILE))
								.plus(rangeLanding.to(NonSI.NAUTICAL_MILE))
								)
						);
				if(rangeCruise.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
					_missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (NEW CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
					return;
				}
				
				//--------------------------------------------------------------------
				// ALTITUDE
				_altitudeList.clear();

				_altitudeList.add(Amount.valueOf(0.0, NonSI.FOOT));
				_altitudeList.add(_obstacleTakeOff.to(NonSI.FOOT));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(NonSI.FOOT));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_alternateCruiseAltitude.to(NonSI.FOOT));
				_altitudeList.add(_alternateCruiseAltitude.to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_obstacleLanding.to(NonSI.FOOT)); 
				_altitudeList.add(Amount.valueOf(0.0, SI.METER)); 

				//--------------------------------------------------------------------
				// RANGE
				_rangeList.clear();

				_rangeList.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
				_rangeList.add(_rangeList.get(0).plus(rangeTakeOff.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(1).plus(rangeClimb.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(2).plus(rangeCruise.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(3).plus(rangeFirstDescent.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(4).plus(rangeSecondClimb.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(5).plus(rangeAlternateCruise.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(6).plus(rangeSecondDescent.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(7).plus(rangeHolding.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(8).plus(rangeThirdDescent).to(NonSI.NAUTICAL_MILE));
				_rangeList.add(_rangeList.get(9).plus(rangeLanding).to(NonSI.NAUTICAL_MILE));

				_totalRange = _rangeList.get(_rangeList.size()-1);

				//--------------------------------------------------------------------
				// TIME
				_timeList.clear();

				_timeList.add(Amount.valueOf(0.0, NonSI.MINUTE));
				_timeList.add(_timeList.get(0).plus(timeTakeOff.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(1).plus(timeClimb.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(2).plus(timeCruise.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(3).plus(timeFirstDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(4).plus(timeSecondClimb.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(5).plus(timeAlternateCruise.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(6).plus(timeSecondDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(7).plus(timeHolding.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(8).plus(timeThirdDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(9).plus(timeLanding.to(NonSI.MINUTE)));

				_totalTime = _timeList.get(_timeList.size()-1);
				_blockTime = timeTakeOff.to(NonSI.MINUTE)
						.plus(timeClimb.to(NonSI.MINUTE))
						.plus(timeCruise.to(NonSI.MINUTE))
						.plus(timeFirstDescent.to(NonSI.MINUTE))
						.plus(timeLanding.to(NonSI.MINUTE));

				//--------------------------------------------------------------------
				// USED FUEL
				_fuelUsedList.clear();

				_fuelUsedList.add(Amount.valueOf(0.0, SI.KILOGRAM));
				_fuelUsedList.add(_fuelUsedList.get(0).plus(fuelTakeOff.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(1).plus(fuelClimb.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(2).plus(fuelCruise.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(3).plus(fuelFirstDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(4).plus(fuelSecondClimb.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(5).plus(fuelAlternateCruise.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(6).plus(fuelSecondDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(7).plus(fuelHolding.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(8).plus(fuelThirdDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(9).plus(fuelLanding.to(SI.KILOGRAM)));

				_totalFuel = _fuelUsedList.get(_fuelUsedList.size()-1);
				_blockFuel = fuelTakeOff.to(SI.KILOGRAM)
						.plus(fuelClimb.to(SI.KILOGRAM))
						.plus(fuelCruise.to(SI.KILOGRAM))
						.plus(fuelFirstDescent.to(SI.KILOGRAM))
						.plus(fuelLanding.to(SI.KILOGRAM));

				//--------------------------------------------------------------------
				// WEIGHT VARIATION
				_massList.clear();

				_massList.add(_initialMissionMass.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtTakeOffEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtClimbEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtCruiseEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtFirstDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtSecondClimbEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtAlternateCruiseEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtSecondDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtHoldingEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtThirdDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtLandingEnding.to(SI.KILOGRAM));

				_endMissionMass = _massList.get(_massList.size()-1);
				
			} 
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			newInitialFuelMass = _totalFuel.to(SI.KILOGRAM).divide(1-_fuelReserve); 
			_initialMissionMass = _operatingEmptyMass
					.plus(_singlePassengerMass.times(_passengersNumber))
					.plus(newInitialFuelMass); 
			
			if(_initialMissionMass.doubleValue(SI.KILOGRAM) > _maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {

				System.err.println("MAXIMUM TAKE-OFF MASS SURPASSED !! REDUCING PASSENGERS NUMBER TO INCREASE THE FUEL ... ");
				
				_passengersNumber += (int) Math.ceil(
						(_maximumTakeOffMass.minus(_initialMissionMass))
						.divide(_singlePassengerMass)
						.getEstimatedValue()
						)
						;
				_initialMissionMass = _maximumTakeOffMass;
				
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
		if(theThirdDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - THIRD DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		
		//----------------------------------------------------------------------
		// ITERATION ENDING ... collecting results
		//----------------------------------------------------------------------
		
		//......................................................................
		_speedCASMissionList.add(speedCASAtTakeOffStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtTakeOffEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtClimbStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtClimbEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtCruiseStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtCruiseEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtFirstDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtFirstDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondClimbStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondClimbEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtAlternateCruiseStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtAlternateCruiseEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtHoldingStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtHoldingEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtThirdDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtThirdDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtLandingStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtLandingEnding.to(NonSI.KNOT));
		
		//......................................................................
		_speedTASMissionList.add(speedTASAtTakeOffStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtTakeOffEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtFirstDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtFirstDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtAlternateCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtAlternateCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtHoldingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtHoldingEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtThirdDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtThirdDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtLandingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtLandingEnding.to(NonSI.KNOT));
		
		//......................................................................
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(0).doubleValue(SI.METER),
						_speedTASMissionList.get(0).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(1).doubleValue(SI.METER),
						_speedTASMissionList.get(1).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						0.0,
						_speedTASMissionList.get(2).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(3).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(4).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(5).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(6).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(7).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(8).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(9).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(10).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(11).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(12).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(13).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(14).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(15).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(16).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						Amount.valueOf(50, NonSI.FOOT).doubleValue(SI.METER),
						_speedTASMissionList.get(17).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						Amount.valueOf(50, NonSI.FOOT).doubleValue(SI.METER),
						_speedTASMissionList.get(18).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(_altitudeList.size()-1).doubleValue(SI.METER),
						_speedTASMissionList.get(19).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		
		//......................................................................
		_liftingCoefficientMissionList.add(cLAtTakeOffStart);
		_liftingCoefficientMissionList.add(cLAtTakeOffEnding);
		_liftingCoefficientMissionList.add(cLAtClimbStart);
		_liftingCoefficientMissionList.add(cLAtClimbEnding);
		_liftingCoefficientMissionList.add(cLAtCruiseStart);
		_liftingCoefficientMissionList.add(cLAtCruiseEnding);
		_liftingCoefficientMissionList.add(cLAtFirstDescentStart);
		_liftingCoefficientMissionList.add(cLAtFirstDescentEnding);
		_liftingCoefficientMissionList.add(cLAtSecondClimbStart);
		_liftingCoefficientMissionList.add(cLAtSecondClimbEnding);
		_liftingCoefficientMissionList.add(cLAtAlternateCruiseStart);
		_liftingCoefficientMissionList.add(cLAtAlternateCruiseEnding);
		_liftingCoefficientMissionList.add(cLAtSecondDescentStart);
		_liftingCoefficientMissionList.add(cLAtSecondDescentEnding);
		_liftingCoefficientMissionList.add(cLAtHoldingStart);
		_liftingCoefficientMissionList.add(cLAtHoldingEnding);
		_liftingCoefficientMissionList.add(cLAtThirdDescentStart);
		_liftingCoefficientMissionList.add(cLAtThirdDescentEnding);
		_liftingCoefficientMissionList.add(cLAtLandingStart);
		_liftingCoefficientMissionList.add(cLAtLandingEnding);
		
		//......................................................................
		_dragCoefficientMissionList.add(cDAtTakeOffStart);
		_dragCoefficientMissionList.add(cDAtTakeOffEnding);
		_dragCoefficientMissionList.add(cDAtClimbStart);
		_dragCoefficientMissionList.add(cDAtClimbEnding);
		_dragCoefficientMissionList.add(cDAtCruiseStart);
		_dragCoefficientMissionList.add(cDAtCruiseEnding);
		_dragCoefficientMissionList.add(cDAtFirstDescentStart);
		_dragCoefficientMissionList.add(cDAtFirstDescentEnding);
		_dragCoefficientMissionList.add(cDAtSecondClimbStart);
		_dragCoefficientMissionList.add(cDAtSecondClimbEnding);
		_dragCoefficientMissionList.add(cDAtAlternateCruiseStart);
		_dragCoefficientMissionList.add(cDAtAlternateCruiseEnding);
		_dragCoefficientMissionList.add(cDAtSecondDescentStart);
		_dragCoefficientMissionList.add(cDAtSecondDescentEnding);
		_dragCoefficientMissionList.add(cDAtHoldingStart);
		_dragCoefficientMissionList.add(cDAtHoldingEnding);
		_dragCoefficientMissionList.add(cDAtThirdDescentStart);
		_dragCoefficientMissionList.add(cDAtThirdDescentEnding);
		_dragCoefficientMissionList.add(cDAtLandingStart);
		_dragCoefficientMissionList.add(cDAtLandingEnding);

		//......................................................................
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(0)/_dragCoefficientMissionList.get(0));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(1)/_dragCoefficientMissionList.get(1));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(2)/_dragCoefficientMissionList.get(2));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(3)/_dragCoefficientMissionList.get(3));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(4)/_dragCoefficientMissionList.get(4));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(5)/_dragCoefficientMissionList.get(5));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(6)/_dragCoefficientMissionList.get(6));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(7)/_dragCoefficientMissionList.get(7));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(8)/_dragCoefficientMissionList.get(8));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(9)/_dragCoefficientMissionList.get(9));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(10)/_dragCoefficientMissionList.get(10));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(11)/_dragCoefficientMissionList.get(11));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(12)/_dragCoefficientMissionList.get(12));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(13)/_dragCoefficientMissionList.get(13));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(14)/_dragCoefficientMissionList.get(14));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(15)/_dragCoefficientMissionList.get(15));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(16)/_dragCoefficientMissionList.get(16));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(17)/_dragCoefficientMissionList.get(17));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(18)/_dragCoefficientMissionList.get(18));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(19)/_dragCoefficientMissionList.get(19));
		
		//......................................................................
		_throttleMissionList.add(throttleCruiseStart);
		_throttleMissionList.add(throttleCruiseEnding);
		_throttleMissionList.add(throttleAlternateCruiseStart);
		_throttleMissionList.add(throttleAlternateCruiseEnding);
		_throttleMissionList.add(throttleHoldingStart);
		_throttleMissionList.add(throttleHoldingEnding);
		
		//......................................................................
		_thrustMissionList.add(thrustAtTakeOffStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtTakeOffEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtClimbStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtClimbEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtCruiseStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtCruiseEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtFirstDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtFirstDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondClimbStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondClimbEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtAlternateCruiseStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtAlternateCruiseEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtHoldingStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtHoldingEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtThirdDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtThirdDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtLandingStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtLandingEnding.to(NonSI.POUND_FORCE));
		
		//......................................................................
		_dragMissionList.add(dragAtTakeOffStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtTakeOffEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtClimbStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtClimbEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtCruiseStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtCruiseEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtFirstDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtFirstDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondClimbStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondClimbEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtAlternateCruiseStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtAlternateCruiseEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtHoldingStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtHoldingEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtThirdDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtThirdDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtLandingStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtLandingEnding.to(NonSI.POUND_FORCE));
		
		//......................................................................
		_rateOfClimbMissionList.add(rateOfClimbAtTakeOffStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtTakeOffEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtClimbStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtClimbEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtCruiseStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtCruiseEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtFirstDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtFirstDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondClimbStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondClimbEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtAlternateCruiseStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtAlternateCruiseEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtHoldingStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtHoldingEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtThirdDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtThirdDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtLandingStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtLandingEnding.to(MyUnits.FOOT_PER_MINUTE));
		
		//......................................................................
		_climbAngleMissionList.add(climbAngleAtTakeOffStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtTakeOffEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtClimbStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtClimbEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtCruiseStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtCruiseEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtFirstDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtFirstDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondClimbStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondClimbEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtAlternateCruiseStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtAlternateCruiseEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtHoldingStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtHoldingEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtThirdDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtThirdDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtLandingStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtLandingEnding.to(NonSI.DEGREE_ANGLE));
		
		//......................................................................
		// in (lb/hr)
		_fuelFlowMissionList.add(fuelFlowAtTakeOffStart);
		_fuelFlowMissionList.add(fuelFlowAtTakeOffEnding);
		_fuelFlowMissionList.add(fuelFlowAtClimbStart);
		_fuelFlowMissionList.add(fuelFlowAtClimbEnding);
		_fuelFlowMissionList.add(fuelFlowAtCruiseStart);
		_fuelFlowMissionList.add(fuelFlowAtCruiseEnding);
		_fuelFlowMissionList.add(fuelFlowAtFirstDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtFirstDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtSecondClimbStart);
		_fuelFlowMissionList.add(fuelFlowAtSecondClimbEnding);
		_fuelFlowMissionList.add(fuelFlowAtAlternateCruiseStart);
		_fuelFlowMissionList.add(fuelFlowAtAlternateCruiseEnding);
		_fuelFlowMissionList.add(fuelFlowAtSecondDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtSecondDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtHoldingStart);
		_fuelFlowMissionList.add(fuelFlowAtHoldingEnding);
		_fuelFlowMissionList.add(fuelFlowAtThirdDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtThirdDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtLandingStart);
		_fuelFlowMissionList.add(fuelFlowAtLandingEnding);
		
		//......................................................................
		// in (lb/(lb*hr))
		_sfcMissionList.add(sfcAtTakeOffStart);
		_sfcMissionList.add(sfcAtTakeOffEnding);
		_sfcMissionList.add(sfcAtClimbStart);
		_sfcMissionList.add(sfcAtClimbEnding);
		_sfcMissionList.add(sfcAtCruiseStart);
		_sfcMissionList.add(sfcAtCruiseEnding);
		_sfcMissionList.add(sfcAtFirstDescentStart);
		_sfcMissionList.add(sfcAtFirstDescentEnding);
		_sfcMissionList.add(sfcAtSecondClimbStart);
		_sfcMissionList.add(sfcAtSecondClimbEnding);
		_sfcMissionList.add(sfcAtAlternateCruiseStart);
		_sfcMissionList.add(sfcAtAlternateCruiseEnding);
		_sfcMissionList.add(sfcAtSecondDescentStart);
		_sfcMissionList.add(sfcAtSecondDescentEnding);
		_sfcMissionList.add(sfcAtHoldingStart);
		_sfcMissionList.add(sfcAtHoldingEnding);
		_sfcMissionList.add(sfcAtThirdDescentStart);
		_sfcMissionList.add(sfcAtThirdDescentEnding);
		_sfcMissionList.add(sfcAtLandingStart);
		_sfcMissionList.add(sfcAtLandingEnding);
		
		_initialFuelMass = newInitialFuelMass;
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
	
}
