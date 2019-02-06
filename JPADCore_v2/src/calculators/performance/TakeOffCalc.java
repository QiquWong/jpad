package calculators.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.ContinuousOutputModel;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import calculators.performance.customdata.TakeOffResultsMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * This class have the purpose of calculating the required take-off field length
 * of a given aircraft by evaluating two main phases:
 *
 * - the ground roll distance (rotation included)
 * - the airborne distance (until the aircraft reaches a given obstacle altitude)
 *
 * for each of them a step by step integration is used in solving the dynamic equation;
 * furthermore the class allows to calculate the balanced field length by evaluating every
 * failure possibility and comparing the required field length in OEI condition with the
 * aborted take-off distance until they are equal.
 *
 * @author Vittorio Trifari
 *
 */

public class TakeOffCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION

	private double aspectRatio;
	private Amount<Area> surface; 
	private Amount<Length> span;
	private Amount<Angle> fuselageUpsweepAngle;
	private PowerPlant thePowerPlant;
	private double[] polarCLTakeOff;
	private double[] polarCDTakeOff;
	private Amount<Duration> dtHold,
	dtRec = Amount.valueOf(3, SI.SECOND),
	tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tFaiulre = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
	private Amount<Mass> maxTakeOffMass; 
	private Amount<Velocity> vSTakeOff, vRot, vMC, vLO, vWind, v1, v2;
	private Amount<Length> wingToGroundDistance, altitude, obstacle, balancedFieldLength;
	private Amount<Temperature> deltaTemperature;
	private Amount<Angle> alphaGround;
	private List<Double> cLStepHandler, cDStepHandler, loadFactorStepHandler, alphaDotStepHandler, timeBreakPoints;
	private List<Amount<Angle>> alphaStepHandler, gammaStepHandler, thetaStepHandler;
	private List<Amount<Duration>> timeStepHandler;
	private List<Amount<Acceleration>> accelerationStepHandler;
	private List<Amount<Force>> weightStepHandler;
	private List<Amount<Velocity>> speedTASStepHandler;
	private List<Amount<Length>> groundDistanceStepHandler;
	private List<Amount<Length>> verticalDistanceStepHandler;
	private double kAlphaDot, kcLMax, kRot, phi, cLmaxTO, kGround, alphaDotInitial, 
	alphaRed, cL0, cLground, kFailure, takeOffThrustCorrectionFactor, aprThrustCorrectionFactor, gidlThrustCorrectionFactor, 
	takeOffSfcCorrectionFactor, aprSfcCorrectionFactor, gidlSfcCorrectionFactor, takeOffCalibrationFactorEmissionIndexNOx,
	takeOffCalibrationFactorEmissionIndexCO, takeOffCalibrationFactorEmissionIndexHC, takeOffCalibrationFactorEmissionIndexSoot, 
	takeOffCalibrationFactorEmissionIndexCO2, takeOffCalibrationFactorEmissionIndexSOx, takeOffCalibrationFactorEmissionIndexH2O, 
	aprCalibrationFactorEmissionIndexNOx, aprCalibrationFactorEmissionIndexCO, aprCalibrationFactorEmissionIndexHC, 
	aprCalibrationFactorEmissionIndexSoot,	aprCalibrationFactorEmissionIndexCO2, aprCalibrationFactorEmissionIndexSOx, 
	aprCalibrationFactorEmissionIndexH2O, gidlCalibrationFactorEmissionIndexNOx, gidlCalibrationFactorEmissionIndexCO,	
	gidlCalibrationFactorEmissionIndexHC, gidlCalibrationFactorEmissionIndexSoot, gidlCalibrationFactorEmissionIndexCO2, 
	gidlCalibrationFactorEmissionIndexSOx, gidlCalibrationFactorEmissionIndexH2O;
	private Amount<Velocity> vFailure;
	private boolean isAborted;
	private boolean isTailStrike;
	private double cLalphaFlap;

	// output lists
	private List<Double> alphaDot, gammaDot, cL, cD, loadFactor, fuelFlow, mach;
	private List<Amount<Angle>> alpha, theta, gamma;
	private List<Amount<Duration>> time;
	private List<Amount<Mass>> fuelUsed, emissionsNOx, emissionsCO, emissionsHC, emissionsSoot, emissionsCO2, emissionsSOx, emissionsH2O;
	private List<Amount<Velocity>> speedTAS, speedCAS, rateOfClimb;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> thrust, thrustHorizontal, thrustVertical, lift, drag, friction, totalForce;
	private List<Amount<Length>> groundDistance, verticalDistance;
	private List<Amount<Force>> weight;
	
	// Statistics to be collected at every phase: (initialization of the lists through the builder
	private TakeOffResultsMap takeOffResults = new TakeOffResultsMap();
	private ContinuousOutputModel continuousOutputModel;
	// Interpolated function for balanced field length calculation
	MyInterpolatingFunction continuedTakeOffFitted = new MyInterpolatingFunction();
	MyInterpolatingFunction abortedTakeOffFitted = new MyInterpolatingFunction();
	MyInterpolatingFunction mu;
	MyInterpolatingFunction muBrake;
	
	// integration index
	private double[] failureSpeedArray, continuedTakeOffArray, abortedTakeOffArray,
	failureSpeedArrayFitted, continuedTakeOffArrayFitted, abortedTakeOffArrayFitted;

	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;
	
	//-------------------------------------------------------------------------------------
	// BUILDER:
	
	/**
	 * This builder is an overload of the previous one designed to allow the user 
	 * to perform the take-off distance calculation without doing all flaps analysis.
	 * This may come in handy when only few data are available.
	 */
	public TakeOffCalc(
			double aspectRatio,
			Amount<Area> surface,
			Amount<Angle> fuselageUpsweepAngle,
			PowerPlant thePowerPlant,
			double[] polarCLTakeOff,
			double[] polarCDTakeOff,
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			Amount<Mass> maxTakeOffMass,
			Amount<Duration> dtHold,
			double kcLMax,
			double kRot,
			double alphaDotInitial,
			double kFailure,
			double phi,
			double kAlphaDot,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Length> obstacle,
			Amount<Length> wingToGroundDistance,
			Amount<Velocity> vWind,
			Amount<Angle> alphaGround,
			double cLmaxTO,
			double cLZeroTO,
			double cLalphaFlap,
			double takeOffThrustCorrectionFactor,
			double aprThrustCorrectionFactor,
			double gidlThrustCorrectionFactor,
			double takeOffSfcCorrectionFactor,
			double aprSfcCorrectionFactor,
			double gidlSfcCorrectionFactor,
			double takeOffCalibrationFactorEmissionIndexNOx,
			double takeOffCalibrationFactorEmissionIndexCO,
			double takeOffCalibrationFactorEmissionIndexHC,
			double takeOffCalibrationFactorEmissionIndexSoot,
			double takeOffCalibrationFactorEmissionIndexCO2,
			double takeOffCalibrationFactorEmissionIndexSOx,
			double takeOffCalibrationFactorEmissionIndexH2O,
			double aprCalibrationFactorEmissionIndexNOx,
			double aprCalibrationFactorEmissionIndexCO,
			double aprCalibrationFactorEmissionIndexHC,
			double aprCalibrationFactorEmissionIndexSoot,
			double aprCalibrationFactorEmissionIndexCO2,
			double aprCalibrationFactorEmissionIndexSOx,
			double aprCalibrationFactorEmissionIndexH2O,
			double groundIdleCalibrationFactorEmissionIndexNOx,
			double groundIdleCalibrationFactorEmissionIndexCO,
			double groundIdleCalibrationFactorEmissionIndexHC,
			double groundIdleCalibrationFactorEmissionIndexSoot,
			double groundIdleCalibrationFactorEmissionIndexCO2,
			double groundIdleCalibrationFactorEmissionIndexSOx,
			double groundIdleCalibrationFactorEmissionIndexH2O
			) {

		// Required data
		this.aspectRatio = aspectRatio;
		this.fuselageUpsweepAngle = fuselageUpsweepAngle;
		this.surface = surface;
		this.span = Amount.valueOf(
				Math.sqrt(aspectRatio*surface.doubleValue(SI.SQUARE_METRE)),
				SI.METER
				);
		this.thePowerPlant = thePowerPlant;
		this.polarCLTakeOff = polarCLTakeOff;
		this.polarCDTakeOff = polarCDTakeOff;
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.maxTakeOffMass = maxTakeOffMass;
		this.dtHold = dtHold;
		this.kcLMax = kcLMax;
		this.kRot = kRot;
		this.alphaDotInitial = alphaDotInitial;
		this.kFailure = kFailure;
		this.phi = phi;
		this.kAlphaDot = kAlphaDot;
		this.mu = mu;
		this.muBrake = muBrake;
		this.obstacle = obstacle;
		this.wingToGroundDistance = wingToGroundDistance;
		this.vWind = vWind;
		this.alphaGround = alphaGround;
		this.cLmaxTO = cLmaxTO;
		this.cLalphaFlap = cLalphaFlap;
		this.cL0 = cLZeroTO;
		this.cLground = cLZeroTO;
		this.takeOffThrustCorrectionFactor = takeOffThrustCorrectionFactor;
		this.aprThrustCorrectionFactor = aprThrustCorrectionFactor;
		this.gidlThrustCorrectionFactor = gidlThrustCorrectionFactor;
		this.takeOffSfcCorrectionFactor = takeOffSfcCorrectionFactor;
		this.aprSfcCorrectionFactor = aprSfcCorrectionFactor;
		this.gidlSfcCorrectionFactor = gidlSfcCorrectionFactor;
		this.takeOffCalibrationFactorEmissionIndexNOx = takeOffCalibrationFactorEmissionIndexNOx;
		this.takeOffCalibrationFactorEmissionIndexCO = takeOffCalibrationFactorEmissionIndexCO; 
		this.takeOffCalibrationFactorEmissionIndexHC = takeOffCalibrationFactorEmissionIndexHC;
		this.takeOffCalibrationFactorEmissionIndexSoot = takeOffCalibrationFactorEmissionIndexSoot;
		this.takeOffCalibrationFactorEmissionIndexCO2 = takeOffCalibrationFactorEmissionIndexCO2; 
		this.takeOffCalibrationFactorEmissionIndexSOx = takeOffCalibrationFactorEmissionIndexSOx;
		this.takeOffCalibrationFactorEmissionIndexH2O = takeOffCalibrationFactorEmissionIndexH2O;
		this.aprCalibrationFactorEmissionIndexNOx = aprCalibrationFactorEmissionIndexNOx;
		this.aprCalibrationFactorEmissionIndexCO = aprCalibrationFactorEmissionIndexCO;
		this.aprCalibrationFactorEmissionIndexHC = aprCalibrationFactorEmissionIndexHC;
		this.aprCalibrationFactorEmissionIndexSoot = aprCalibrationFactorEmissionIndexSoot;
		this.aprCalibrationFactorEmissionIndexCO2 = aprCalibrationFactorEmissionIndexCO2;
		this.aprCalibrationFactorEmissionIndexSOx = aprCalibrationFactorEmissionIndexSOx;
		this.aprCalibrationFactorEmissionIndexH2O = aprCalibrationFactorEmissionIndexH2O;
		this.gidlCalibrationFactorEmissionIndexNOx = groundIdleCalibrationFactorEmissionIndexNOx;
		this.gidlCalibrationFactorEmissionIndexCO = groundIdleCalibrationFactorEmissionIndexCO;	
		this.gidlCalibrationFactorEmissionIndexHC = groundIdleCalibrationFactorEmissionIndexHC;
		this.gidlCalibrationFactorEmissionIndexSoot = groundIdleCalibrationFactorEmissionIndexSoot; 
		this.gidlCalibrationFactorEmissionIndexCO2 = groundIdleCalibrationFactorEmissionIndexCO2; 
		this.gidlCalibrationFactorEmissionIndexSOx = groundIdleCalibrationFactorEmissionIndexSOx;
		this.gidlCalibrationFactorEmissionIndexH2O = groundIdleCalibrationFactorEmissionIndexH2O;
		
		// Reference velocities definition
		vSTakeOff = SpeedCalc.calculateSpeedStall(
						altitude,
						deltaTemperature,
						maxTakeOffMass,
						surface,
						cLmaxTO
						);
		
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxTO = " + cLmaxTO);
		System.out.println("CL0 = " + cLZeroTO);
		System.out.println("VsTO = " + vSTakeOff);
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.timeStepHandler = new ArrayList<Amount<Duration>>();
		this.alphaStepHandler = new ArrayList<Amount<Angle>>();
		this.gammaStepHandler = new ArrayList<Amount<Angle>>();
		this.thetaStepHandler = new ArrayList<Amount<Angle>>();
		this.alphaDotStepHandler = new ArrayList<Double>();
		this.accelerationStepHandler = new ArrayList<Amount<Acceleration>>();
		this.loadFactorStepHandler = new ArrayList<Double>();
		this.cLStepHandler = new ArrayList<Double>();
		this.cDStepHandler = new ArrayList<Double>();
		this.weightStepHandler = new ArrayList<Amount<Force>>();
		this.speedTASStepHandler = new ArrayList<Amount<Velocity>>();
		this.groundDistanceStepHandler = new ArrayList<Amount<Length>>();
		this.verticalDistanceStepHandler = new ArrayList<Amount<Length>>();
		this.timeBreakPoints = new ArrayList<Double>();
		
		this.time = new ArrayList<Amount<Duration>>();
		this.speedTAS = new ArrayList<Amount<Velocity>>();
		this.speedCAS = new ArrayList<Amount<Velocity>>();
		this.mach = new ArrayList<Double>();
		this.thrust = new ArrayList<Amount<Force>>();
		this.thrustHorizontal = new ArrayList<Amount<Force>>();
		this.thrustVertical = new ArrayList<Amount<Force>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.alphaDot = new ArrayList<Double>();
		this.gamma = new ArrayList<Amount<Angle>>();
		this.gammaDot = new ArrayList<Double>();
		this.theta = new ArrayList<Amount<Angle>>();
		this.cL = new ArrayList<Double>();
		this.lift = new ArrayList<Amount<Force>>();
		this.loadFactor = new ArrayList<Double>();
		this.cD = new ArrayList<Double>();
		this.drag = new ArrayList<Amount<Force>>();
		this.friction = new ArrayList<Amount<Force>>();
		this.totalForce = new ArrayList<Amount<Force>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.rateOfClimb = new ArrayList<Amount<Velocity>>();
		this.groundDistance = new ArrayList<Amount<Length>>();
		this.verticalDistance = new ArrayList<Amount<Length>>();
		this.fuelFlow = new ArrayList<Double>();
		this.fuelUsed = new ArrayList<>();
		this.weight = new ArrayList<Amount<Force>>();
		this.emissionsNOx = new ArrayList<Amount<Mass>>();
		this.emissionsCO = new ArrayList<Amount<Mass>>();
		this.emissionsHC = new ArrayList<Amount<Mass>>();
		this.emissionsSoot = new ArrayList<Amount<Mass>>();
		this.emissionsCO2 = new ArrayList<Amount<Mass>>();
		this.emissionsSOx = new ArrayList<Amount<Mass>>();
		this.emissionsH2O = new ArrayList<Amount<Mass>>();
		
		takeOffResults.initialize();
	}
	//-------------------------------------------------------------------------------------
	// METHODS:

	/**************************************************************************************
	 * This method is used to initialize all lists in order to perform a new calculation or
	 * to setup the first one
	 *
	 * @author Vittorio Trifari
	 */
	public void initialize() {

		// lists cleaning
		timeStepHandler.clear();
		alphaStepHandler.clear();
		gammaStepHandler.clear();
		thetaStepHandler.clear();
		alphaDotStepHandler.clear();
		speedTASStepHandler.clear();
		accelerationStepHandler.clear();
		loadFactorStepHandler.clear();
		cLStepHandler.clear();
		cDStepHandler.clear();
		weightStepHandler.clear();
		groundDistanceStepHandler.clear();
		verticalDistanceStepHandler.clear();
		timeBreakPoints.clear();
		
		time.clear();
		speedTAS.clear();
		speedCAS.clear();
		mach.clear();
		thrust.clear();
		thrustHorizontal.clear();
		thrustVertical.clear();
		alpha.clear();
		alphaDot.clear();
		gamma.clear();
		gammaDot.clear();
		theta.clear();
		cL.clear();
		lift.clear();
		loadFactor.clear();
		cD.clear();
		drag.clear();
		friction.clear();
		totalForce.clear();
		acceleration.clear();
		rateOfClimb.clear();
		groundDistance.clear();
		verticalDistance.clear();
		fuelFlow.clear();
		weight.clear();
		fuelUsed.clear();
		emissionsNOx.clear();
		emissionsCO.clear();
		emissionsHC.clear();
		emissionsSoot.clear();
		emissionsCO2.clear();
		emissionsSOx.clear();
		emissionsH2O.clear();
		
		tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tFaiulre = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		
		vFailure = null;
		isAborted = false;
		isTailStrike = false;
		
		takeOffResults.initialize();
	}
	
	/***************************************************************************************
	 * This method performs the integration of the total take-off distance by solving a set of
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateTakeOffDistanceODE(Amount<Velocity> vFailure, boolean isAborted, boolean iterativeLoopOverV2, Amount<Velocity> vMC) {

		System.out.println("---------------------------------------------------");
		System.out.println("CalcTakeOff :: ODE integration\n\n");

		if(vMC != null) {
			if(1.05*vMC.doubleValue(SI.METERS_PER_SECOND) > (kRot*vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
					) {
				System.err.println("WARNING: (SIMULATION - TAKE-OFF) THE CHOSEN VRot IS LESS THAN 1.05*VMC. THIS LATTER WILL BE USED ...");
				vRot = vMC.to(SI.METERS_PER_SECOND).times(1.05);
			}
			else
				vRot = vSTakeOff.to(SI.METERS_PER_SECOND).times(kRot);
		}
		else
			vRot = vSTakeOff.to(SI.METERS_PER_SECOND).times(kRot);
		
		int i=0;
		double newAlphaRed = 0.0;
		alphaRed = 0.0;
		
		v2 = Amount.valueOf(10000.0, SI.METERS_PER_SECOND); // initialization to an impossible speed
		
		while (Math.abs((v2.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)) - 1.13) >= 0.001) {

			if(i >= 1) {
				if(newAlphaRed <= 0.0)
					alphaRed = newAlphaRed;
				else
					break;
			}
			
			if(i > 100) {
				System.err.println("WARNING: (SIMULATION - TAKE-OFF) MAXIMUM NUMBER OF ITERATION REACHED. THE LAST VALUE OF V2 WILL BE CONSIDERED. "
						+ "(V2 = " + v2.to(SI.METERS_PER_SECOND) + "; V2/VsTO = " + v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)));
				break;
			}
			
			initialize();
			
			this.isAborted = isAborted;
			// failure check
			if(vFailure == null)
				this.vFailure = Amount.valueOf(10000.0, SI.METERS_PER_SECOND); // speed impossible to reach --> no failure!!
			else
				this.vFailure = vFailure;

			theIntegrator = new HighamHall54Integrator(
					1e-20,
					1,
					1e-13,
					1e-13
					);
			ode = new DynamicsEquationsTakeOff();

			EventHandler ehCheckFailure = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					if(t < tFaiulre.doubleValue(SI.SECOND))
						return x[1] - TakeOffCalc.this.vFailure.doubleValue(SI.METERS_PER_SECOND);
					else
						return 10; // a generic positive value used to make the event trigger once
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFAILURE OCCURRED !!");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tFaiulre = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					
					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckVRot = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					double speed = x[1];

					if(t < tRec.doubleValue(SI.SECOND)) {
						return speed - vRot.doubleValue(SI.METERS_PER_SECOND);
					}
					else
						return 10; // a generic positive value used to make the event trigger once
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF GROUND ROLL PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m"+
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					tRot = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					
					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
					takeOffResults.collectResults(
							timeStepHandler.get(timeStepHandler.size()-1),
							speedTASStepHandler.get(speedTASStepHandler.size()-1),
							groundDistanceStepHandler.get(groundDistanceStepHandler.size()-1),
							verticalDistanceStepHandler.get(verticalDistanceStepHandler.size()-1),
							alphaStepHandler.get(alphaStepHandler.size()-1),
							gammaStepHandler.get(gammaStepHandler.size()-1),
							thetaStepHandler.get(thetaStepHandler.size()-1)
							);
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.CONTINUE;
				}
			};
			EventHandler ehEndConstantCL = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return t - (tHold.to(SI.SECOND).plus(dtHold.to(SI.SECOND)).doubleValue(SI.SECOND));
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND BAR HOLDING");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tEndHold = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					
					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckObstacle = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return x[3] - obstacle.doubleValue(SI.METER);
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF AIRBORNE PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m"+
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF AIRBORNE PHASE ...");
					takeOffResults.collectResults(
							timeStepHandler.get(timeStepHandler.size()-1),
							speedTASStepHandler.get(speedTASStepHandler.size()-1),
							groundDistanceStepHandler.get(groundDistanceStepHandler.size()-1),
							verticalDistanceStepHandler.get(verticalDistanceStepHandler.size()-1),
							alphaStepHandler.get(alphaStepHandler.size()-1),
							gammaStepHandler.get(gammaStepHandler.size()-1),
							thetaStepHandler.get(thetaStepHandler.size()-1)
							);
					
					v2 = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					System.out.println("V2/VsTO = " + v2.divide(vSTakeOff));
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};
			EventHandler ehCheckBrakes = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return t - (tFaiulre.to(SI.SECOND).plus(dtRec.to(SI.SECOND)).doubleValue(SI.SECOND));
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFAILURE RECOGNITION --> BRAKES ACTIVATED");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tRec = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					
					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckStop = new EventHandler() {
				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					double speed = x[1];
					return speed - 0.0;
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND ABORTED TAKE OFF RUN");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s"
							);
					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE AIRCRAFT STOP ...");
					takeOffResults.collectResults(
							timeStepHandler.get(timeStepHandler.size()-1),
							speedTASStepHandler.get(speedTASStepHandler.size()-1),
							groundDistanceStepHandler.get(groundDistanceStepHandler.size()-1),
							verticalDistanceStepHandler.get(verticalDistanceStepHandler.size()-1),
							alphaStepHandler.get(alphaStepHandler.size()-1),
							gammaStepHandler.get(gammaStepHandler.size()-1),
							thetaStepHandler.get(thetaStepHandler.size()-1)
							);
					System.out.println("\n---------------------------DONE!-------------------------------");
					timeBreakPoints.add(t);
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};

			if(isAborted == false) {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehEndConstantCL, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-3, 20);
			}
			else {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckBrakes, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-10, 50);
			}

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] x = interpolator.getInterpolatedState();
					double[] xDot = interpolator.getInterpolatedDerivatives();
					
					//========================================================================================
					// PICKING UP ALL VARIABLES AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					Amount<Duration> currentTime = Amount.valueOf(t, SI.SECOND);
					Amount<Length> currentGroundDistance = Amount.valueOf(x[0], SI.METER);
					Amount<Velocity> currentSpeed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> currentGamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> currentAltitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> currentWeight = Amount.valueOf( 
							(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> currentAlpha = ((DynamicsEquationsTakeOff)ode).alpha(currentTime);
					double currentAlphaDot = ((DynamicsEquationsTakeOff)ode).alphaDot(currentTime);

					// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					// TIME:
					TakeOffCalc.this.getTimeStepHandler().add(currentTime);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					TakeOffCalc.this.getAlphaStepHandler().add(currentAlpha);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					TakeOffCalc.this.getAlphaDotStepHandler().add(currentAlphaDot);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					TakeOffCalc.this.getGammaStepHandler().add(currentGamma);
					//----------------------------------------------------------------------------------------
					// THETA:
					TakeOffCalc.this.getThetaStepHandler().add(currentAlpha.to(NonSI.DEGREE_ANGLE).plus(currentGamma.to(NonSI.DEGREE_ANGLE)));
					//----------------------------------------------------------------------------------------
					// SPEED TAS:
					TakeOffCalc.this.getSpeedTASStepHandler().add(currentSpeed);
					//----------------------------------------------------------------------------------------
					// GROUND DISTANCE:
					TakeOffCalc.this.getGroundDistanceStepHandler().add(currentGroundDistance);
					//----------------------------------------------------------------------------------------
					// VERTICAL DISTANCE:
					TakeOffCalc.this.getVerticalDistanceStepHandler().add(currentAltitude);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					TakeOffCalc.this.getLoadFactorStepHandler().add(
							(  ((DynamicsEquationsTakeOff)ode).lift(currentSpeed, currentAlpha, currentGamma, currentTime, currentAltitude, deltaTemperature, currentWeight).doubleValue(SI.NEWTON)
									+ (  ((DynamicsEquationsTakeOff)ode).thrust(currentSpeed, currentTime, currentGamma, currentAltitude, deltaTemperature)
											.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
											*Math.sin(currentAlpha.doubleValue(SI.RADIAN))
											)
									)
							/ ( currentWeight.doubleValue(SI.NEWTON)*Math.cos(currentGamma.doubleValue(SI.RADIAN))	)
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					TakeOffCalc.this.getCLStepHandler().add(
							((DynamicsEquationsTakeOff)ode).cL(currentSpeed, currentAlpha, currentGamma, currentTime, currentWeight, currentAltitude, deltaTemperature)
							);
					//----------------------------------------------------------------------------------------
					// CD:				
					TakeOffCalc.this.getCDStepHandler().add(
							((DynamicsEquationsTakeOff)ode).cD(
									((DynamicsEquationsTakeOff)ode).cL(currentSpeed, currentAlpha, currentGamma, currentTime, currentWeight, currentAltitude, deltaTemperature),
									currentAltitude
									)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					TakeOffCalc.this.getAccelerationStepHandler().add(
							Amount.valueOf(xDot[1], SI.METERS_PER_SQUARE_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// WEIGHT:
					TakeOffCalc.this.getWeightStepHandler().add(currentWeight);

					// CHECK TO BE DONE ONLY IF isAborted IS FALSE!!
					if(isAborted == false) {						
						// CHECK ON LOAD FACTOR --> END ROTATION WHEN n=1
						if((t > tRot.doubleValue(SI.SECOND)) && (tEndRot.doubleValue(SI.SECOND) == 10000.0) &&
								(TakeOffCalc.this.getLoadFactorStepHandler().get(TakeOffCalc.this.getLoadFactorStepHandler().size()-1) > 1.0) &&
								(TakeOffCalc.this.getLoadFactorStepHandler().get(TakeOffCalc.this.getLoadFactorStepHandler().size()-2) < 1.0)) {
							System.out.println("\n\t\tEND OF ROTATION PHASE");
							System.out.println(
									"\n\tx[0] = s = " + x[0] + " m" +
											"\n\tx[1] = V = " + x[1] + " m/s" + 
											"\n\tx[2] = gamma = " + x[2] + " °" +
											"\n\tx[3] = altitude = " + x[3] + " m"+
											"\n\tx[4] = fuel used = " + x[4] + " kg" +
											"\n\tt = " + t + " s"
									);
							// COLLECTING DATA IN TakeOffResultsMap
							System.out.println("\n\tCOLLECTING DATA AT THE END OF ROTATION PHASE ...");
							takeOffResults.collectResults(
									timeStepHandler.get(timeStepHandler.size()-1),
									speedTASStepHandler.get(speedTASStepHandler.size()-1),
									groundDistanceStepHandler.get(groundDistanceStepHandler.size()-1),
									verticalDistanceStepHandler.get(verticalDistanceStepHandler.size()-1),
									alphaStepHandler.get(alphaStepHandler.size()-1),
									gammaStepHandler.get(gammaStepHandler.size()-1),
									thetaStepHandler.get(thetaStepHandler.size()-1)
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tEndRot = Amount.valueOf(t, SI.SECOND);
							timeBreakPoints.add(t);
							vLO = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
						}
						// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
						if((t > tRot.doubleValue(SI.SECOND)) && 
								(TakeOffCalc.this.getCLStepHandler().get(TakeOffCalc.this.getCLStepHandler().size()-1) - (kcLMax*cLmaxTO) >= 0.0) &&
								((TakeOffCalc.this.getCLStepHandler().get(TakeOffCalc.this.getCLStepHandler().size()-2) - (kcLMax*cLmaxTO)) < 0.0)) {
							System.out.println("\n\t\tBEGIN BAR HOLDING");
							System.out.println(
									"\n\tCL = " + ((DynamicsEquationsTakeOff)ode).cL(
											currentSpeed,
											((DynamicsEquationsTakeOff)ode).alpha(currentTime),
											currentGamma,
											currentTime,
											Amount.valueOf(
													(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])
													*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
													SI.NEWTON
													),
											currentAltitude,
											deltaTemperature
											) + 
									"\n\tAlpha Body = " + ((DynamicsEquationsTakeOff)ode).alpha(currentTime) + " °" + 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tHold = Amount.valueOf(t, SI.SECOND);
							timeBreakPoints.add(t);
						}
						// CHECK ON LOAD FACTOR TO ENSTABLISH WHEN n=1 WHILE DECREASING ALPHA AND CL
						if((t > tEndHold.doubleValue(SI.SECOND)) && (tClimb.doubleValue(SI.SECOND) == 10000.0) &&
								(TakeOffCalc.this.getLoadFactorStepHandler().get(TakeOffCalc.this.getLoadFactorStepHandler().size()-1) < 1) &&
								(TakeOffCalc.this.getLoadFactorStepHandler().get(TakeOffCalc.this.getLoadFactorStepHandler().size()-2) > 1)) {
							System.out.println("\n\t\tLOAD FACTOR = 1 IN CLIMB");
							System.out.println( 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tClimb = Amount.valueOf(t, SI.SECOND);
							timeBreakPoints.add(t);
						}
					}
				}
			};
			theIntegrator.addStepHandler(stepHandler);

			continuousOutputModel = new ContinuousOutputModel();
			theIntegrator.addStepHandler(continuousOutputModel);
			
			double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0, 0.0}; // initial state
			theIntegrator.integrate(ode, 0.0, xAt0, 100, xAt0); // now xAt0 contains final state

			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

			if( (isAborted == true && iterativeLoopOverV2 == false) )
				break;
			
			//--------------------------------------------------------------------------------
			// NEW ALPHA REDUCTION RATE 
			if(((v2.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)) - 1.13) >= 0.0)
				newAlphaRed = alphaRed + 0.1;
			else
				newAlphaRed = alphaRed - 0.1;
			
			i++;
		}
		
		if(isTailStrike)
			System.err.println("WARNING: (SIMULATION - TAKE-OFF) TAIL STRIKE !! ");
		
		if(vFailure == null)
			manageOutputData(0.75, continuousOutputModel);
		
		
		System.out.println("\n---------------------------END!!-------------------------------");
	}

	/**************************************************************************************
	 * This method is used to evaluate the balanced take-off field length. It calculates the
	 * total take-off distance and the aborted take-off distance in case of engine failure at
	 * several failure speeds; after that the points made by the failure speed and the related
	 * distance are interpolated in order to have more fitted curves of aborted and continued
	 * take-off conditions. At this point the intersection of these curves is evaluated giving
	 * back the balanced take-off field length and the related failure speed which is the decision
	 * speed (V1)
	 *
	 * @author Vittorio Trifari
	 */
	public void calculateBalancedFieldLength(Amount<Velocity> vMC) {

		// failure speed array
		failureSpeedArray = MyArrayUtils.linspace(
				vSTakeOff.times(0.5).doubleValue(SI.METERS_PER_SECOND),
				vRot.doubleValue(SI.METERS_PER_SECOND),
				5);
		// continued take-off array
		continuedTakeOffArray = new double[failureSpeedArray.length]; 
		// aborted take-off array
		abortedTakeOffArray = new double[failureSpeedArray.length];

		// iterative take-off distance calculation for both conditions
		for(int i=0; i<failureSpeedArray.length; i++) {
			calculateTakeOffDistanceODE(Amount.valueOf(failureSpeedArray[i], SI.METERS_PER_SECOND), false, true, vMC);
			if(!getTakeOffResults().getGroundDistance().isEmpty())
				continuedTakeOffArray[i] = getTakeOffResults().getGroundDistance().get(getTakeOffResults().getGroundDistance().size()-1).doubleValue(SI.METER);
			else {
				failureSpeedArray[i] = 0.0;
				continuedTakeOffArray[i] = 0.0;
			}
			
			calculateTakeOffDistanceODE(Amount.valueOf(failureSpeedArray[i], SI.METERS_PER_SECOND), true, false, vMC);
			if(!getTakeOffResults().getGroundDistance().isEmpty() 
					&& getTakeOffResults().getGroundDistance().get(getTakeOffResults().getGroundDistance().size()-1).doubleValue(SI.METER) >= 0.0)
				abortedTakeOffArray[i] = getTakeOffResults().getGroundDistance().get(getTakeOffResults().getGroundDistance().size()-1).doubleValue(SI.METER);
			else {
				failureSpeedArray[i] = 0.0;
				abortedTakeOffArray[i] = 0.0;
			}
		}

		MyInterpolatingFunction continuedTakeOffFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction abortedTakeOffFunction = new MyInterpolatingFunction();
		
		continuedTakeOffFunction.interpolateLinear(
				Arrays.stream(failureSpeedArray).filter(x -> x != 0.0).toArray(), 
				Arrays.stream(continuedTakeOffArray).filter(x -> x != 0.0).toArray()
				);
		abortedTakeOffFunction.interpolateLinear(failureSpeedArray, abortedTakeOffArray);
		
		failureSpeedArrayFitted = MyArrayUtils.linspace(
				vSTakeOff.times(0.5).doubleValue(SI.METERS_PER_SECOND),
				vRot.doubleValue(SI.METERS_PER_SECOND),
				1000);
		continuedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
		abortedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
		
		for(int i=0; i<failureSpeedArrayFitted.length; i++) {
			
			continuedTakeOffArrayFitted[i] = continuedTakeOffFunction.value(failureSpeedArrayFitted[i]);
			abortedTakeOffArrayFitted[i] = abortedTakeOffFunction.value(failureSpeedArrayFitted[i]);
			
		}
		
		// arrays intersection
		double[] intersection = new double[continuedTakeOffArrayFitted.length]; 
				
		try {
			intersection = MyArrayUtils.intersectArraysSimple(
				continuedTakeOffArrayFitted,
				abortedTakeOffArrayFitted);
			for(int i=0; i<intersection.length; i++)
				if(intersection[i] != 0.0) {
					balancedFieldLength = Amount.valueOf(intersection[i], SI.METER);
					v1 = Amount.valueOf(failureSpeedArrayFitted[i], SI.METERS_PER_SECOND);
				}
		}
		catch (NullPointerException e) {
			System.err.println("WARNING: (BALANCED FIELD LENGTH - TAKE-OFF) NO INTERSECTION FOUND ...");
			balancedFieldLength = Amount.valueOf(0.0, SI.METER);
			v1 = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		}
		
	}

	/********************************************************************************************
	 * This method allows users to fill all the maps of results related to each throttle setting.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void manageOutputData(double dt, StepHandler handler) {

		MyInterpolatingFunction alphaFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction alphaDotFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction loadFactorFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction accelerationFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction cLFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction weightFunction = new MyInterpolatingFunction();

		alphaFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.alphaStepHandler)
				);
		alphaDotFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.alphaDotStepHandler))
				);
		loadFactorFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.loadFactorStepHandler))
				);
		accelerationFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.accelerationStepHandler)
				);
		cLFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.cLStepHandler))
				);
		weightFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timeStepHandler), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.weightStepHandler)
				);

		//#############################################################################
		// Collect the array of times and associated state vector values according
		// to the given dt and keeping the the discrete event-times (breakpoints)

		List<Amount<Duration>> times = new ArrayList<Amount<Duration>>();
		List<double[]> states = new ArrayList<double[]>();
		List<double[]> stateDerivatives = new ArrayList<double[]>();

		// There is only ONE ContinuousOutputModel handler, get it
		if (handler instanceof ContinuousOutputModel) {
			System.out.println("Found handler instanceof ContinuousOutputModel");
			System.out.println("=== Stored state variables ===");
			ContinuousOutputModel cm = (ContinuousOutputModel) handler;
			System.out.println("Initial time: " + cm.getInitialTime());
			System.out.println("Final time: " + cm.getFinalTime());

			// build time vector keeping event-times as breakpoints
			double t = cm.getInitialTime();
			do {
				times.add(Amount.valueOf(t, SI.SECOND));
				cm.setInterpolatedTime(t);
				states.add(cm.getInterpolatedState());
				stateDerivatives.add(cm.getInterpolatedDerivatives());

				t += dt;
				// System.out.println("Current time: " + t);
				// detect breakpoints adjusting time as appropriate
				for(int i=0; i<timeBreakPoints.size(); i++) {
					double t_ = timeBreakPoints.get(i);
					//  bracketing
					if ((t-dt < t_) && (t > t_)) {
						// set back time to breakpoint-time
						t = t_;
					}
				}

			} while (t <= cm.getFinalTime());

			//--------------------------------------------------------------------------------
			// Reconstruct the auxiliary/derived variables
			for(int i = 0; i < times.size(); i++) {

				double[] x = states.get(i);
				double[] xDot = stateDerivatives.get(i);
				//========================================================================================
				// PICKING UP ALL VARIABLES AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				Amount<Duration> time = times.get(i);
				Amount<Length> groundDistance = Amount.valueOf(x[0], SI.METER);
				Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
				Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
				Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
				
				//========================================================================================
				// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				// TIME:
				this.time.add(time);
				//----------------------------------------------------------------------------------------
				// GROUND DISTANCE:
				this.groundDistance.add(groundDistance);
				//----------------------------------------------------------------------------------------
				// ALTITUDE:
				this.verticalDistance.add(altitude);
				//----------------------------------------------------------------------------------------
				// THRUST:
				this.thrust.add(Amount.valueOf(
						((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON)
						);
				//--------------------------------------------------------------------------------
				// FUEL USED (kg):
				this.fuelUsed.add(Amount.valueOf(x[4], SI.KILOGRAM));
				//--------------------------------------------------------------------------------
				// FUEL FLOW (kg/s):
				this.fuelFlow.add(xDot[4]);
				//----------------------------------------------------------------------------------------
				// WEIGHT:
				this.weight.add(
						Amount.valueOf(
								weightFunction.value(times.get(i).doubleValue(SI.SECOND)),
								SI.NEWTON
								)
						);
				//----------------------------------------------------------------------------------------
				// SPEED TAS:
				this.speedTAS.add(speed);
				//----------------------------------------------------------------------------------------
				// SPEED CAS:
				double sigma = AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)/1.225);
				this.speedCAS.add(speed.times(Math.sqrt(sigma)));
				//----------------------------------------------------------------------------------------
				// MACH:
				double speedOfSound = AtmosphereCalc.getSpeedOfSound(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS));
				this.mach.add(speed.doubleValue(SI.METERS_PER_SECOND) / speedOfSound);
				//----------------------------------------------------------------------------------------
				// THRUST HORIZONTAL:
				this.thrustHorizontal.add(Amount.valueOf(
						((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						*Math.cos(
								Amount.valueOf(
										alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
										NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN)
								),
						SI.NEWTON)
						);
				//----------------------------------------------------------------------------------------
				// THRUST VERTICAL:
				this.thrustVertical.add(Amount.valueOf(
						((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						*Math.sin(
								Amount.valueOf(
										alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
										NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN)
								),
						SI.NEWTON)
						);
				//--------------------------------------------------------------------------------
				// FRICTION:
				if(isAborted == false) {
					if(times.get(i).doubleValue(SI.SECOND) < tEndRot.doubleValue(SI.SECOND))
						this.friction.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).mu(speed)
								* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
										- ((DynamicsEquationsTakeOff)ode).lift(
												speed,
												Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
												gamma,
												times.get(i),
												altitude,
												deltaTemperature,
												Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
												).doubleValue(SI.NEWTON)
										),
								SI.NEWTON)
								);
					else
						this.friction.add(Amount.valueOf(0.0, SI.NEWTON));
				}
				else {
					if(t < tRec.doubleValue(SI.SECOND))
						this.friction.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).mu(speed)
								* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
										- ((DynamicsEquationsTakeOff)ode).lift(
												speed,
												Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
												gamma,
												times.get(i),
												altitude,
												deltaTemperature,
												Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
												).doubleValue(SI.NEWTON)
										),
								SI.NEWTON)
								);
					else
						this.friction.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).muBrake(speed)
								* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
										- ((DynamicsEquationsTakeOff)ode).lift(
												speed,
												Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
												gamma,
												times.get(i),
												altitude,
												deltaTemperature,
												Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
												).doubleValue(SI.NEWTON)
										),
								SI.NEWTON)
								);
				}
				//----------------------------------------------------------------------------------------
				// LIFT:
				this.lift.add(
						((DynamicsEquationsTakeOff)ode).lift(
								speed,
								Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
								gamma,
								times.get(i),
								altitude,
								deltaTemperature,
								Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
								)
						);
				//----------------------------------------------------------------------------------------
				// DRAG:
				this.drag.add(
						((DynamicsEquationsTakeOff)ode).drag(
								speed,
								Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
								gamma,
								times.get(i),
								altitude,
								deltaTemperature,
								Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
								)
						);
				//----------------------------------------------------------------------------------------
				// TOTAL FORCE:
				if(isAborted == false) {
					this.totalForce.add(Amount.valueOf(
							(((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
									.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.cos(Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN))
									)
							- ((DynamicsEquationsTakeOff)ode).drag(
									speed,
									Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
									gamma,
									times.get(i),
									altitude,
									deltaTemperature,
									Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
									).doubleValue(SI.NEWTON)
							- (((DynamicsEquationsTakeOff)ode).mu(speed)
									* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
											- ((DynamicsEquationsTakeOff)ode).lift(
													speed,
													Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
													gamma,
													times.get(i),
													altitude,
													deltaTemperature,
													Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
													).doubleValue(SI.NEWTON)
											)
									)
							- (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
									* Math.sin(gamma.doubleValue(SI.RADIAN))
									),
							SI.NEWTON)
							);
				}
				else {
					if(t < tRec.doubleValue(SI.SECOND))
						this.totalForce.add(Amount.valueOf(
								(((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
										.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
										*Math.cos(Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN))
										)
								- ((DynamicsEquationsTakeOff)ode).drag(
										speed,
										Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
										gamma,
										times.get(i),
										altitude,
										deltaTemperature,
										Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
										).doubleValue(SI.NEWTON)
								- (((DynamicsEquationsTakeOff)ode).mu(speed)
										* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
												- ((DynamicsEquationsTakeOff)ode).lift(
														speed,
														Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
														gamma,
														times.get(i),
														altitude,
														deltaTemperature,
														Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
														).doubleValue(SI.NEWTON)
												)
										)
								- (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
										* Math.sin(gamma.doubleValue(SI.RADIAN))
										),
								SI.NEWTON)
								);
					else
						this.totalForce.add(Amount.valueOf(
								(((DynamicsEquationsTakeOff)ode).thrust(speed, times.get(i), gamma, altitude, deltaTemperature)
										.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
										*Math.cos(Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN))
										)
								- ((DynamicsEquationsTakeOff)ode).drag(
										speed,
										Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
										gamma,
										times.get(i),
										altitude,
										deltaTemperature,
										Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
										).doubleValue(SI.NEWTON)
								- (((DynamicsEquationsTakeOff)ode).muBrake(speed)
										* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
												- ((DynamicsEquationsTakeOff)ode).lift(
														speed,
														Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
														gamma,
														times.get(i),
														altitude,
														deltaTemperature,
														Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON)
														).doubleValue(SI.NEWTON)
												)
										)
								- (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
										* Math.sin(gamma.doubleValue(SI.RADIAN))
										),
								SI.NEWTON)
								);
				}
				//----------------------------------------------------------------------------------------
				// LOAD FACTOR:
				this.loadFactor.add(loadFactorFunction.value(times.get(i).doubleValue(SI.SECOND)));
				//----------------------------------------------------------------------------------------
				// RATE OF CLIMB:
				this.rateOfClimb.add(Amount.valueOf(
						xDot[3],
						SI.METERS_PER_SECOND)
						);
				//----------------------------------------------------------------------------------------
				// ACCELERATION:
				this.acceleration.add(Amount.valueOf(
						accelerationFunction.value(times.get(i).doubleValue(SI.SECOND)),
						SI.METERS_PER_SQUARE_SECOND)
						);
				//----------------------------------------------------------------------------------------
				// ALPHA:
				this.alpha.add(Amount.valueOf(
						alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
						NonSI.DEGREE_ANGLE)
						);
				//----------------------------------------------------------------------------------------
				// GAMMA:
				this.gamma.add(gamma);
				//----------------------------------------------------------------------------------------
				// ALPHA DOT:
				this.alphaDot.add(alphaDotFunction.value(times.get(i).doubleValue(SI.SECOND)));
				//----------------------------------------------------------------------------------------
				// GAMMA DOT:
				this.gammaDot.add(xDot[2]);
				//----------------------------------------------------------------------------------------
				// THETA:
				this.theta.add(Amount.valueOf(
						gamma.doubleValue(NonSI.DEGREE_ANGLE) + alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
						NonSI.DEGREE_ANGLE)
						);
				//----------------------------------------------------------------------------------------
				// CL:				
				this.cL.add(cLFunction.value(times.get(i).doubleValue(SI.SECOND)));
				//----------------------------------------------------------------------------------------
				// CD:
				this.cD.add(
						((DynamicsEquationsTakeOff)ode).cD(
								((DynamicsEquationsTakeOff)ode).cL(
										speed,
										Amount.valueOf(alphaFunction.value(times.get(i).doubleValue(SI.SECOND)), NonSI.DEGREE_ANGLE),
										gamma,
										times.get(i),
										Amount.valueOf(weightFunction.value(times.get(i).doubleValue(SI.SECOND)), SI.NEWTON),
										altitude,
										deltaTemperature
										),
								altitude
								)
						);
				//----------------------------------------------------------------------------------------
				// EMISSIONS NOx:
				this.emissionsNOx.add(((DynamicsEquationsTakeOff)ode).emissionNOx(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS CO:
				this.emissionsCO.add(((DynamicsEquationsTakeOff)ode).emissionCO(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS HC:
				this.emissionsHC.add(((DynamicsEquationsTakeOff)ode).emissionHC(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS Soot:
				this.emissionsSoot.add(((DynamicsEquationsTakeOff)ode).emissionSoot(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS CO2:
				this.emissionsCO2.add(((DynamicsEquationsTakeOff)ode).emissionCO2(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS SOx:
				this.emissionsSOx.add(((DynamicsEquationsTakeOff)ode).emissionSOx(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
				//----------------------------------------------------------------------------------------
				// EMISSIONS H2O:
				this.emissionsH2O.add(((DynamicsEquationsTakeOff)ode).emissionH2O(speed, time, gamma, altitude, deltaTemperature, fuelUsed.get(i)));
			}
		}
	}
	
	/**************************************************************************************
	 * This method allows users to plot all take-off performance producing several output charts
	 * which have time or ground distance as independent variables.
	 *
	 * @author Vittorio Trifari
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createTakeOffCharts(String takeOffFolderPath, boolean createCSV) throws InstantiationException, IllegalAccessException {

		System.out.println("\n---------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE-----------");

		if(isAborted == false) {
			//.................................................................................
			// take-off trajectory
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
					MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
					0.0, null, 0.0, null,
					"Ground Distance", "Altitude", "m", "m",
					takeOffFolderPath, "TakeOff_Trajectory_SI", createCSV);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertListOfAmountTodoubleArray(
							verticalDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Ground Distance", "Altitude", "ft", "ft",
					takeOffFolderPath, "TakeOff_Trajectory_IMPERIAL", createCSV);

			//.................................................................................
			// vertical distance v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
					0.0, null, 0.0, null,
					"Time", "Altitude", "s", "m",
					takeOffFolderPath, "Altitude_evolution_SI", createCSV);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							verticalDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Time", "Altitude", "s", "ft",
					takeOffFolderPath, "Altitude_evolution_IMPERIAL", createCSV);
			
		}
		
		//.................................................................................
		// speed TAS v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(speedTAS),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				takeOffFolderPath, "Speed_TAS_evolution_SI", createCSV);
		
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedTAS.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "kn",
				takeOffFolderPath, "Speed_TAS_evolution_IMPERIAL", createCSV);
		
		//.................................................................................
		// speed TAS v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(speedTAS),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				takeOffFolderPath, "Speed_TAS_vs_GroundDistance_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedTAS.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "ft", "kn",
				takeOffFolderPath, "Speed_TAS_vs_GroundDistance_IMPERIAL", createCSV);

		//.................................................................................
		// speed CAS v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(speedCAS),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				takeOffFolderPath, "Speed_CAS_evolution_SI", createCSV);
		
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedCAS.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "kn",
				takeOffFolderPath, "Speed_CAS_evolution_IMPERIAL", createCSV);
		
		//.................................................................................
		// speed CAS v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(speedCAS),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				takeOffFolderPath, "Speed_CAS_vs_GroundDistance_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedCAS.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "ft", "kn",
				takeOffFolderPath, "Speed_CAS_vs_GroundDistance_IMPERIAL", createCSV);
		
		//.................................................................................
		// mach v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertToDoublePrimitive(mach),
				0.0, null, 0.0, null,
				"Time", "Mach number", "s", "",
				takeOffFolderPath, "Mach_evolution_SI", createCSV);
		
		
		//.................................................................................
		// mach v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertToDoublePrimitive(mach),
				0.0, null, 0.0, null,
				"Ground Distance", "Mach number", "m", "",
				takeOffFolderPath, "Mach_vs_GroundDistance_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(mach),
				0.0, null, 0.0, null,
				"Ground Distance", "Mach number", "ft", "",
				takeOffFolderPath, "Mach_vs_GroundDistance_IMPERIAL", createCSV);
		
		//.................................................................................
		// Emission NOx v.s. time
		Double yMaxNOx = null;
		Double yMinNOx = null;
		if(MyArrayUtils.getMin(emissionsNOx.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsNOx.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxNOx = 10.0; /* Generic positive value */ 
			yMinNOx = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsNOx),
				0.0, null, yMinNOx, yMaxNOx,
				"Time", "Emission NOx", "s", "g",
				takeOffFolderPath, "Emission_NOx_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission NOx v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsNOx),
				0.0, null, yMinNOx, yMaxNOx,
				"Ground Distance", "Emission NOx", "m", "g",
				takeOffFolderPath, "Emission_NOx_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission CO v.s. time
		Double yMaxCO = null;
		Double yMinCO = null;
		if(MyArrayUtils.getMin(emissionsCO.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsCO.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxCO = 10.0; /* Generic positive value */ 
			yMinCO = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsCO),
				0.0, null, yMinCO, yMaxCO,
				"Time", "Emission CO", "s", "g",
				takeOffFolderPath, "Emission_CO_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission CO v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsCO),
				0.0, null, yMinCO, yMaxCO,
				"Ground Distance", "Emission CO", "m", "g",
				takeOffFolderPath, "Emission_CO_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission HC v.s. time
		Double yMaxHC = null;
		Double yMinHC = null;
		if(MyArrayUtils.getMin(emissionsHC.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsHC.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxHC = 10.0; /* Generic positive value */ 
			yMinHC = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsHC),
				0.0, null, yMinHC, yMaxHC,
				"Time", "Emission HC", "s", "g",
				takeOffFolderPath, "Emission_HC_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission HC v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsHC),
				0.0, null, yMinHC, yMaxHC,
				"Ground Distance", "Emission HC", "m", "g",
				takeOffFolderPath, "Emission_HC_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission Soot v.s. time
		Double yMaxSoot = null;
		Double yMinSoot = null;
		if(MyArrayUtils.getMin(emissionsSoot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsSoot.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxSoot = 10.0; /* Generic positive value */ 
			yMinSoot = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsSoot),
				0.0, null, yMinSoot, yMaxSoot,
				"Time", "Emission Soot", "s", "g",
				takeOffFolderPath, "Emission_Soot_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission Soot v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsSoot),
				0.0, null, yMinSoot, yMaxSoot,
				"Ground Distance", "Emission Soot", "m", "g",
				takeOffFolderPath, "Emission_Soot_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission CO2 v.s. time
		Double yMaxCO2 = null;
		Double yMinCO2 = null;
		if(MyArrayUtils.getMin(emissionsCO2.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsCO2.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxCO2 = 10.0; /* Generic positive value */ 
			yMinCO2 = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsCO2),
				0.0, null, yMinCO2, yMaxCO2,
				"Time", "Emission CO2", "s", "g",
				takeOffFolderPath, "Emission_CO2_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission CO2 v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsCO2),
				0.0, null, yMinCO2, yMaxCO2,
				"Ground Distance", "Emission CO2", "m", "g",
				takeOffFolderPath, "Emission_CO2_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission SOx v.s. time
		Double yMaxSOx = null;
		Double yMinSOx = null;
		if(MyArrayUtils.getMin(emissionsSOx.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsSOx.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxSOx = 10.0; /* Generic positive value */ 
			yMinSOx = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsSOx),
				0.0, null, yMinSOx, yMaxSOx,
				"Time", "Emission SOx", "s", "g",
				takeOffFolderPath, "Emission_SOx_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission SOx v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsSOx),
				0.0, null, yMinSOx, yMaxSOx,
				"Ground Distance", "Emission SOx", "m", "g",
				takeOffFolderPath, "Emission_SOx_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// Emission H2O v.s. time
		Double yMaxH2O = null;
		Double yMinH2O = null;
		if(MyArrayUtils.getMin(emissionsH2O.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(emissionsH2O.stream().mapToDouble(e -> e.doubleValue(SI.GRAM)).toArray()) == 0.0) { 
			yMaxH2O = 10.0; /* Generic positive value */ 
			yMinH2O = -10.0; /* Generic positive value */
		} 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsH2O),
				0.0, null, yMinH2O, yMaxH2O,
				"Time", "Emission H2O", "s", "g",
				takeOffFolderPath, "Emission_H2O_evolution_SI", createCSV);
		
		//.................................................................................
		// Emission H2O v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(emissionsH2O),
				0.0, null, yMinH2O, yMaxH2O,
				"Ground Distance", "Emission H2O", "m", "g",
				takeOffFolderPath, "Emission_H2O_vs_GroundDistance_SI", createCSV);
		
		//.................................................................................
		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
				0.0, null, null, null,
				"Time", "Acceleration", "s", "m/(s^2)",
				takeOffFolderPath, "Acceleration_evolution_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						acceleration.stream()
						.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
						.collect(Collectors.toList())
						),
				0.0, null, null, null,
				"Time", "Acceleration", "s", "ft/(min^2)",
				takeOffFolderPath, "Acceleration_evolution_IMPERIAL", createCSV);
		
		//.................................................................................
		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
				0.0, null, null, null,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				takeOffFolderPath, "Acceleration_vs_GroundDistance_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						acceleration.stream()
						.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
						.collect(Collectors.toList())
						),
				0.0, null, null, null,
				"Ground Distance", "Acceleration", "ft", "ft/(min^2)",
				takeOffFolderPath, "Acceleration_vs_GroundDistance_IMPERIAL", createCSV);

		//.................................................................................
		// load factor v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				0.0, null, 0.0, null,
				"Time", "Load Factor", "s", "",
				takeOffFolderPath, "LoadFactor_evolution", createCSV);

		//.................................................................................
		// load factor v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				takeOffFolderPath, "LoadFactor_vs_GroundDistance_SI", createCSV);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						), 
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				takeOffFolderPath, "LoadFactor_vs_GroundDistance_IMPERIAL", createCSV);

		if(isAborted == false) {
			//.................................................................................
			// Rate of Climb v.s. Time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
					0.0, null, 0.0, null,
					"Time", "Rate of Climb", "s", "m/s",
					takeOffFolderPath, "RateOfClimb_evolution_SI", createCSV);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimb.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Time", "Rate of Climb", "s", "ft/min",
					takeOffFolderPath, "RateOfClimb_evolution_IMPERIAL", createCSV);

			//.................................................................................
			// Rate of Climb v.s. Ground distance
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
					MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
					0.0, null, 0.0, null,
					"Ground distance", "Rate of Climb", "m", "m/s",
					takeOffFolderPath, "RateOfClimb_vs_GroundDistance_SI", createCSV);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimb.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Ground distance", "Rate of Climb", "ft", "ft/min",
					takeOffFolderPath, "RateOfClimb_vs_GroundDistance_IMPERIAL", createCSV);
		}
		
		//.................................................................................
		// CL v.s. Time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(time),
				MyArrayUtils.convertToDoublePrimitive(cL),
				0.0, null, 0.0, null,
				"Time", "CL", "s", "",
				takeOffFolderPath, "CL_evolution", createCSV);

		//.................................................................................
		// CL v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertToDoublePrimitive(cL),
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				takeOffFolderPath, "CL_vs_GroundDistance_SI", createCSV);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(cL),
				0.0, null, 0.0, null,
				"Ground distance", "CL", "ft", "",
				takeOffFolderPath, "CL_vs_GroundDistance_IMPERIAL", createCSV);
		
		//.................................................................................
		// Horizontal Forces v.s. Time
		double[][] xMatrix1SI = new double[5][totalForce.size()];
		for(int i=0; i<xMatrix1SI.length; i++)
			xMatrix1SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

		double[][] yMatrix1SI = new double[5][totalForce.size()];
		yMatrix1SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
		yMatrix1SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontal);
		yMatrix1SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
		yMatrix1SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);
		yMatrix1SI[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix1SI, yMatrix1SI,
				0.0, null, null, null,
				"Time", "Horizontal Forces", "s", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
				takeOffFolderPath, "HorizontalForces_evolution_SI",
				createCSV
				);

		double[][] xMatrix1IMPERIAL = new double[5][totalForce.size()];
		for(int i=0; i<xMatrix1IMPERIAL.length; i++)
			xMatrix1IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

		double[][] yMatrix1IMPERIAL = new double[5][totalForce.size()];
		yMatrix1IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				totalForce.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix1IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
				thrustHorizontal.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix1IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				drag.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix1IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
				friction.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix1IMPERIAL[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.map(x -> x.times(0.224809))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix1IMPERIAL, yMatrix1IMPERIAL,
				0.0, null, null, null,
				"Time", "Horizontal Forces", "s", "lb",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
				takeOffFolderPath, "HorizontalForces_evolution_IMPERIAL",
				createCSV);
		
		//.................................................................................
		// Horizontal Forces v.s. Ground Distance
		double[][] xMatrix2SI = new double[5][totalForce.size()];
		for(int i=0; i<xMatrix2SI.length; i++)
			xMatrix2SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

		double[][] yMatrix2SI = new double[5][totalForce.size()];
		yMatrix2SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
		yMatrix2SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontal);
		yMatrix2SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
		yMatrix2SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);
		yMatrix2SI[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix2SI, yMatrix2SI,
				0.0, null, null, null,
				"Ground Distance", "Horizontal Forces", "m", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
				takeOffFolderPath, "HorizontalForces_vs_GroundDistance_SI",
				createCSV);

		double[][] xMatrix2IMPERIAL = new double[5][totalForce.size()];
		for(int i=0; i<xMatrix2IMPERIAL.length; i++)
			xMatrix2IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

		double[][] yMatrix2IMPERIAL = new double[5][totalForce.size()];
		yMatrix2IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				totalForce.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
				thrustHorizontal.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				drag.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
				friction.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2IMPERIAL[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.map(x -> x.times(0.224809))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix2IMPERIAL, yMatrix2IMPERIAL,
				0.0, null, null, null,
				"Ground Distance", "Horizontal Forces", "ft", "lb",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
				takeOffFolderPath, "HorizontalForces_vs_GroundDistance_IMPERIAL",
				createCSV);

		//.................................................................................
		// Vertical Forces v.s. Time
		double[][] xMatrix3SI = new double[3][totalForce.size()];
		for(int i=0; i<xMatrix3SI.length; i++)
			xMatrix3SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

		double[][] yMatrix3SI = new double[3][totalForce.size()];
		yMatrix3SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
		yMatrix3SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVertical);
		yMatrix3SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix3SI, yMatrix3SI,
				0.0, null, null, null,
				"Time", "Vertical Forces", "s", "N",
				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
				takeOffFolderPath, "VerticalForces_evolution",
				createCSV);

		double[][] xMatrix3IMPERIAL = new double[3][totalForce.size()];
		for(int i=0; i<xMatrix3IMPERIAL.length; i++)
			xMatrix3IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

		double[][] yMatrix3IMPERIAL = new double[3][totalForce.size()];
		yMatrix3IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				lift.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix3IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
				thrustVertical.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix3IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.map(x -> x.times(0.224809))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix3IMPERIAL, yMatrix3IMPERIAL,
				0.0, null, null, null,
				"Time", "Vertical Forces", "s", "lb",
				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
				takeOffFolderPath, "VerticalForces_evolution_IMPERIAL",
				createCSV);
		
		//.................................................................................
		// Vertical Forces v.s. ground distance
		double[][] xMatrix4SI = new double[3][totalForce.size()];
		for(int i=0; i<xMatrix4SI.length; i++)
			xMatrix4SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

		double[][] yMatrix4SI = new double[3][totalForce.size()];
		yMatrix4SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
		yMatrix4SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVertical);
		yMatrix4SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix4SI, yMatrix4SI,
				0.0, null, null, null,
				"Ground distance", "Vertical Forces", "m", "N",
				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
				takeOffFolderPath, "VerticalForces_vs_GroundDistance_SI",
				createCSV);

		double[][] xMatrix4IMPERIAL = new double[3][totalForce.size()];
		for(int i=0; i<xMatrix4IMPERIAL.length; i++)
			xMatrix4IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
					groundDistance.stream()
					.map(x -> x.to(NonSI.FOOT))
					.collect(Collectors.toList())
					);

		double[][] yMatrix4IMPERIAL = new double[3][totalForce.size()];
		yMatrix4IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				lift.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix4IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
				thrustVertical.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix4IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				gamma.stream()
				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
				.map(x -> x.times(0.224809))
				.collect(Collectors.toList())
				);

		MyChartToFileUtils.plot(
				xMatrix4IMPERIAL, yMatrix4IMPERIAL,
				0.0, null, null, null,
				"Ground distance", "Vertical Forces", "ft", "lb",
				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
				takeOffFolderPath, "VerticalForces_vs_GroundDistance_IMPERIAL", createCSV);
		
		if(isAborted == false) {
			//.................................................................................
			// Angles v.s. time
			double[][] xMatrix5 = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix5.length; i++)
				xMatrix5[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix5 = new double[3][totalForce.size()];
			yMatrix5[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					alpha.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix5[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					theta.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix5[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix5, yMatrix5,
					0.0, null, null, null,
					"Time", "Angles", "s", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					takeOffFolderPath, "Angles_evolution",createCSV);

			//.................................................................................
			// Angles v.s. Ground Distance
			double[][] xMatrix6SI = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix6SI.length; i++)
				xMatrix6SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

			double[][] yMatrix6SI = new double[3][totalForce.size()];
			yMatrix6SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					alpha.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix6SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					theta.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix6SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix6SI, yMatrix6SI,
					0.0, null, null, null,
					"Ground Distance", "Angles", "m", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					takeOffFolderPath, "Angles_vs_GroundDistance_SI", createCSV);

			double[][] xMatrix6IMPERIAL = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix6IMPERIAL.length; i++)
				xMatrix6IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						);

			double[][] yMatrix6IMPERIAL = new double[3][totalForce.size()];
			yMatrix6IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					alpha.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix6IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					theta.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix6IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix6IMPERIAL, yMatrix6IMPERIAL,
					0.0, null, null, null,
					"Ground Distance", "Angles", "ft", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					takeOffFolderPath, "Angles_vs_GroundDistance_IMPERIAL", createCSV);
			
			//.................................................................................
			// Angular velocity v.s. time
			double[][] xMatrix7 = new double[2][totalForce.size()];
			for(int i=0; i<xMatrix7.length; i++)
				xMatrix7[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix7 = new double[2][totalForce.size()];
			yMatrix7[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
			yMatrix7[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

			MyChartToFileUtils.plot(
					xMatrix7, yMatrix7,
					0.0, null, null, null,
					"Time", "Angular Velocity", "s", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					takeOffFolderPath, "AngularVelocity_evolution", createCSV);

			//.................................................................................
			// Angular velocity v.s. Ground Distance
			double[][] xMatrix8SI = new double[2][totalForce.size()];
			for(int i=0; i<xMatrix8SI.length; i++)
				xMatrix8SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

			double[][] yMatrix8SI = new double[2][totalForce.size()];
			yMatrix8SI[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
			yMatrix8SI[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

			MyChartToFileUtils.plot(
					xMatrix8SI, yMatrix8SI,
					0.0, null, null, null,
					"Ground Distance", "Angular Velocity", "m", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					takeOffFolderPath, "AngularVelocity_vs_GroundDistance_SI",createCSV);
			
			double[][] xMatrix8IMPERIAL = new double[2][totalForce.size()];
			for(int i=0; i<xMatrix8IMPERIAL.length; i++)
				xMatrix8IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						);

			double[][] yMatrix8SIMPERIAL = new double[2][totalForce.size()];
			yMatrix8SIMPERIAL[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
			yMatrix8SIMPERIAL[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

			MyChartToFileUtils.plot(
					xMatrix8IMPERIAL, yMatrix8SIMPERIAL,
					0.0, null, null, null,
					"Ground Distance", "Angular Velocity", "ft", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					takeOffFolderPath, "AngularVelocity_vs_GroundDistance_SI", createCSV);
		}
		
		System.out.println("\n---------------------------DONE!-------------------------------");
	}

	/**************************************************************************************
	 * This method allows users to plot the OEI continued take-off distance and the aborted
	 * take-off distance as function of the failure speed compared with the take-off stalling
	 * speed. From the intersection of the two curves it's possible to identify the decision
	 * speed (V1) and the balanced field length.
	 *
	 * @author Vittorio Trifari
	 */
	public void createBalancedFieldLengthChart(String takeOffFolderPath, boolean createCSV) {

		System.out.println("\n-------WRITING BALANCED TAKE-OFF DISTANCE CHART TO FILE--------");

		for(int i=0; i<failureSpeedArrayFitted.length; i++)
			failureSpeedArrayFitted[i] = failureSpeedArrayFitted[i]/vSTakeOff.doubleValue(SI.METERS_PER_SECOND);

		double[][] xArray = new double[][]
				{failureSpeedArrayFitted, failureSpeedArrayFitted};
		double[][] yArraySI = new double[][]
				{continuedTakeOffArrayFitted, abortedTakeOffArrayFitted};

		MyChartToFileUtils.plot(
				xArray, yArraySI,
				null, null, null, null,
				"Vfailure/VsTO", "Distance", "", "m",
				new String[] {"OEI Take-Off", "Aborted Take-Off"},
				takeOffFolderPath, "BalancedTakeOffLength_SI", createCSV);
		
		double[][] yArrayIMPERIAL = new double[][]	{
			Arrays.stream(continuedTakeOffArrayFitted)
			.map(x -> x*3.28084)
			.toArray(), 
			Arrays.stream(abortedTakeOffArrayFitted)
			.map(x -> x*3.28084)
			.toArray()
			};

		MyChartToFileUtils.plot(
				xArray, yArrayIMPERIAL,
				null, null, null, null,
				"Vfailure/VsTO", "Distance", "", "ft",
				new String[] {"OEI Take-Off", "Aborted Take-Off"},
				takeOffFolderPath, "BalancedTakeOffLength_IMPERIAL", createCSV);

		System.out.println("\n---------------------------DONE!-------------------------------");
	}

	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsTakeOff implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);
		
		public DynamicsEquationsTakeOff() {

		}

		@Override
		public int getDimension() {
			return 5;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
			Amount<Angle> alpha = alpha(time);
			Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
			Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
			Amount<Force> weight = Amount.valueOf(
					(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*g0,
					SI.NEWTON
					);
			
//			if(time.doubleValue(SI.SECOND) > tEndHold.doubleValue(SI.SECOND)) {
//				System.out.println("\n\tTime: " + time);
//				System.out.println("\tDistance: " + x[0] + " m");
//				System.out.println("\tSpeed: " + speed);
//				System.out.println("\tGamma: " + gamma);
//				System.out.println("\tAltitude: " + altitude);
//				System.out.println("\tAlpha: " + alpha);
//				System.out.println("\tCL: " + cL(speed, alpha, gamma, time, weight, altitude, deltaTemperature));
//				System.out.println("\tLoad Factor: " + 
//						(  ((DynamicsEquationsTakeOff)ode).lift(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)
//								+ (  ((DynamicsEquationsTakeOff)ode).thrust(speed, time, gamma, altitude, deltaTemperature)
//										.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
//										*Math.sin(alpha.doubleValue(SI.RADIAN))
//										)
//								)
//						/ ( weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN))	)
//						);
//			}
			
			if(isAborted == false) {
				if( time.doubleValue(SI.SECOND) < tEndRot.doubleValue(SI.SECOND)) {
					xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
					xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
							*(thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									- drag(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)
									- (mu(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)))
									);
					xDot[2] = 0.0;
					xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
					xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
				}
				else {
					xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
					xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
							*(thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()* Math.cos(alpha.doubleValue(SI.RADIAN)) 
									- drag(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON) 
									- weight.doubleValue(SI.NEWTON)*Math.sin(gamma.doubleValue(SI.RADIAN))
									);
					xDot[2] = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))
							*(lift(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON) 
									+ (thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
											*Math.sin(alpha.doubleValue(SI.RADIAN))
											)
									- weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN))
									);
					xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
					xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
				}
			}
			else {
				if( t < tRec.doubleValue(SI.SECOND)) {
					xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
					xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
							*(thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									- drag(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)
									- (mu(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)))
									);
					xDot[2] = 0.0;
					xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
					xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
				}
				else {
					xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
					xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
							*(thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									- drag(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)
									- (muBrake(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed, alpha, gamma, time, altitude, deltaTemperature, weight).doubleValue(SI.NEWTON)))
									);
					xDot[2] = 0.0;
					xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
					xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
				}
			}
		}

		public List<Amount<Force>> thrust(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			List<Amount<Force>> theThrustList = new ArrayList<>();

			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									TakeOffCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
									TakeOffCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i),
									EngineOperatingConditionEnum.TAKE_OFF, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									TakeOffCalc.this.getPhi(),
									TakeOffCalc.this.getTakeOffThrustCorrectionFactor()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							theThrustList.add(
									ThrustCalc.calculateThrustDatabase(
											TakeOffCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
											TakeOffCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i), 
											EngineOperatingConditionEnum.TAKE_OFF, 
											altitude,
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											deltaTemperature, 
											TakeOffCalc.this.getPhi(),
											TakeOffCalc.this.getTakeOffThrustCorrectionFactor()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							theThrustList.add(
									ThrustCalc.calculateThrustDatabase(
											TakeOffCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
											TakeOffCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i),  
											EngineOperatingConditionEnum.APR, 
											altitude, 
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											deltaTemperature, 
											TakeOffCalc.this.getPhi(),
											TakeOffCalc.this.getAprThrustCorrectionFactor()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							theThrustList.add(
									ThrustCalc.calculateThrustDatabase(
											TakeOffCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
											TakeOffCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i), 
											EngineOperatingConditionEnum.TAKE_OFF, 
											altitude,
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											deltaTemperature, 
											TakeOffCalc.this.getPhi(),
											TakeOffCalc.this.getTakeOffThrustCorrectionFactor()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							theThrustList.add(
									ThrustCalc.calculateThrustDatabase(
											TakeOffCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
											TakeOffCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i), 
											EngineOperatingConditionEnum.GIDL, 
											altitude,
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											deltaTemperature, 
											TakeOffCalc.this.getPhi(),
											TakeOffCalc.this.getGidlThrustCorrectionFactor()
											)
									);
				}
			}

			return theThrustList;
		}

		public double fuelFlow(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			List<Double> fuelFlowList = new ArrayList<>();
			List<Amount<Force>> thrustList = thrust(speed, time, gamma, altitude, deltaTemperature); 
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					fuelFlowList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffSfcCorrectionFactor()
									)
							*(0.224809)*(0.454/3600)
							*thrustList.get(i).doubleValue(SI.NEWTON)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							fuelFlowList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffSfcCorrectionFactor()
											)
									*(0.224809)*(0.454/3600)
									*thrustList.get(i).doubleValue(SI.NEWTON)
									)
							;
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							fuelFlowList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprSfcCorrectionFactor()
											)
									*(0.224809)*(0.454/3600)
									*thrustList.get(i).doubleValue(SI.NEWTON)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							fuelFlowList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffSfcCorrectionFactor()
											)
									*(0.224809)*(0.454/3600)
									*thrustList.get(i).doubleValue(SI.NEWTON)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							fuelFlowList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlSfcCorrectionFactor()
											)
									*(0.224809)*(0.454/3600)
									*thrustList.get(i).doubleValue(SI.NEWTON)
									);
				}
			}
			
			return fuelFlowList.stream().mapToDouble(ff -> ff).sum();

		}
		
		public Amount<Mass> emissionNOx(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexNOxList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexNOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexNOx()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexNOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexNOx()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexNOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexNOx()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexNOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexNOx()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexNOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexNOx()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexNOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public Amount<Mass> emissionCO(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexCOList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexCOList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexCOList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCOList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexCO()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCOList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCOList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexCO()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexCOList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}

		public Amount<Mass> emissionHC(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexHCList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexHCList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexHC()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexHCList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexHC()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexHCList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexHC()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexHCList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexHC()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexHCList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexHC()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexHCList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public Amount<Mass> emissionSoot(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexSootList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexSootList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSoot()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexSootList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSoot()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSootList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexSoot()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSootList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSoot()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSootList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexSoot()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexSootList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public Amount<Mass> emissionCO2(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexCO2List = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexCO2List.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO2()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexCO2List.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO2()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCO2List.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexCO2()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCO2List.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexCO2()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexCO2List.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexCO2()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexCO2List.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public Amount<Mass> emissionSOx(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexSOxList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexSOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSOx()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexSOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSOx()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexSOx()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexSOx()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexSOxList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexSOx()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexSOxList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public Amount<Mass> emissionH2O(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> fuelUsed) {

			List<Double> emissionIndexH2OList = new ArrayList<>();
			
			if (time.doubleValue(SI.SECOND) <= tFaiulre.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexH2OList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									TakeOffCalc.this.getPhi(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexH2O()
									)
							);
			else {
				if(isAborted == false) {
					if ( (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)) 
							&& (time.doubleValue(SI.SECOND) > tFaiulre.doubleValue(SI.SECOND)+1) )
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++) 
							emissionIndexH2OList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexH2O()
											)
									);
					else {
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexH2OList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.APR,
											TakeOffCalc.this.getAprCalibrationFactorEmissionIndexH2O()
											)
									);
					}
				}
				else {
					if(time.doubleValue(SI.SECOND) < tRec.doubleValue(SI.SECOND))
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexH2OList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getTakeOffCalibrationFactorEmissionIndexH2O()
											)
									);
					else
						for (int i=0; i<TakeOffCalc.this.getThePowerPlant().getEngineNumber()-1; i++)
							emissionIndexH2OList.add(
									thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
											SpeedCalc.calculateMach(
													altitude,
													deltaTemperature,
													Amount.valueOf(
															speed.doubleValue(SI.METERS_PER_SECOND) 
															+ TakeOffCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
															SI.METERS_PER_SECOND
															)
													),
											altitude,
											deltaTemperature,
											TakeOffCalc.this.getPhi(),
											EngineOperatingConditionEnum.GIDL,
											TakeOffCalc.this.getGidlCalibrationFactorEmissionIndexH2O()
											)
									);
				}
			}
			
			return Amount.valueOf(
					emissionIndexH2OList.stream().mapToDouble(e -> e.doubleValue()).average().getAsDouble()
					*fuelUsed.doubleValue(SI.KILOGRAM),
					SI.GRAM
					);

		}
		
		public double cD(double cL, Amount<Length> altitude) {

			double hb = (TakeOffCalc.this.getWingToGroundDistance().doubleValue(SI.METER) / TakeOffCalc.this.getSpan().doubleValue(SI.METER)) + altitude.doubleValue(SI.METER);
			// Aerodynamics For Naval Aviators: (Hurt)
			double kGround = 1.0;
			if(hb < 1.1)
				kGround = 1- (-4.48276577 * Math.pow(hb, 5) 
						+ 15.61174376 * Math.pow(hb, 4)
						- 21.20171050 * Math.pow(hb, 3)
						+ 14.39438721 * Math.pow(hb, 2)
						- 5.20913465 * hb
						+ 0.90793397);
			
			double cD = MyMathUtils
					.getInterpolatedValue1DLinear(polarCLTakeOff, polarCDTakeOff, cL);

			double cD0 = MyArrayUtils.getMin(polarCDTakeOff);
			double cDi = (cD-cD0)*kGround;

			double cDnew = cD0 + cDi;

			return cDnew;
			
		}

		public Amount<Force> drag(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Duration> time, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight) {

			double cD = 0;

			if (time.doubleValue(SI.SECOND) < tFaiulre.doubleValue(SI.SECOND))
				cD = cD(cL(speed, alpha, gamma, time, weight, altitude, deltaTemperature), altitude);
			else
				cD = kFailure + cD(cL(speed, alpha, gamma, time, weight, altitude, deltaTemperature), altitude);

			return 	Amount.valueOf(
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (TakeOffCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
					*cD,
					SI.NEWTON
					);
		}

		public double cL(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Duration> time, Amount<Force> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			if (time.doubleValue(SI.SECOND) < tClimb.doubleValue(SI.SECOND)) {
				double cL0 = TakeOffCalc.this.cL0;
				double cLalpha = TakeOffCalc.this.getcLalphaFlap();
				double alphaWing = alpha.doubleValue(NonSI.DEGREE_ANGLE);

				return cL0 + (cLalpha*alphaWing);
				
			}
			else
				return (2*weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN)))
						/ (TakeOffCalc.this.getSurface().doubleValue(SI.SQUARE_METRE)
								*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
								*Math.pow(speed.doubleValue(SI.METERS_PER_SECOND), 2));
		}

		public Amount<Force> lift(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Duration> time, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight) {

			double cL = cL(speed, alpha, gamma, time, weight, altitude, deltaTemperature);

			return 	Amount.valueOf(
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (TakeOffCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
					*cL,
					SI.NEWTON
					);
		}

		public double mu(Amount<Velocity> speed) {
			return mu.value(speed.doubleValue(SI.METERS_PER_SECOND));
		}
		
		public double muBrake(Amount<Velocity> speed) {
			return muBrake.value(speed.doubleValue(SI.METERS_PER_SECOND));
		}
		
		public double alphaDot(Amount<Duration> time) {

			double alphaDot = 0.0;
			
			if(isAborted == true)
				alphaDot = 0.0;
			else {
				if((time.doubleValue(SI.SECOND) > tRot.doubleValue(SI.SECOND)) && (time.doubleValue(SI.SECOND) < tHold.doubleValue(SI.SECOND))) {
					alphaDot = alphaDotInitial*(1-(TakeOffCalc.this.getkAlphaDot()*(TakeOffCalc.this.getAlphaStepHandler().get(
							TakeOffCalc.this.getAlphaStepHandler().size()-1).doubleValue(NonSI.DEGREE_ANGLE)))
							);
				}
				else if((time.doubleValue(SI.SECOND) > tEndHold.doubleValue(SI.SECOND)) && (time.doubleValue(SI.SECOND) < tClimb.doubleValue(SI.SECOND))) {
					alphaDot = alphaRed;
				}
			}
			return alphaDot;
		}

		public Amount<Angle> alpha(Amount<Duration> time) {

			Amount<Angle> alpha = TakeOffCalc.this.getAlphaGround();

			if(time.doubleValue(SI.SECOND) > tRot.doubleValue(SI.SECOND))
				alpha = Amount.valueOf( 
						TakeOffCalc.this.getAlphaStepHandler().get(
								TakeOffCalc.this.getAlphaStepHandler().size()-1).doubleValue(NonSI.DEGREE_ANGLE)
						+(alphaDot(time)*(TakeOffCalc.this.getTimeStepHandler().get(
								TakeOffCalc.this.getTimeStepHandler().size()-1).doubleValue(SI.SECOND)
								- TakeOffCalc.this.getTimeStepHandler().get(
										TakeOffCalc.this.getTimeStepHandler().size()-2).doubleValue(SI.SECOND))),
						NonSI.DEGREE_ANGLE
						);

			if(alpha.doubleValue(NonSI.DEGREE_ANGLE) >= fuselageUpsweepAngle.doubleValue(NonSI.DEGREE_ANGLE)) {
				isTailStrike = true;
			}
			
			return alpha;
		}
	}
	//-------------------------------------------------------------------------------------
	//									END NESTED CLASS	
	//-------------------------------------------------------------------------------------


	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:

	public Amount<Duration> getDtHold() {
		return dtHold;
	}

	public void setDtHold(Amount<Duration> dtHold) {
		this.dtHold = dtHold;
	}

	public Amount<Duration> getDtRec() {
		return dtRec;
	}

	public void setDtRec(Amount<Duration> dtRec) {
		this.dtRec = dtRec;
	}

	public Amount<Velocity> getvSTakeOff() {
		return vSTakeOff;
	}

	public void setvSTakeOff(Amount<Velocity> vSTakeOff) {
		this.vSTakeOff = vSTakeOff;
	}

	public Amount<Velocity> getvRot() {
		return vRot;
	}

	public void setvRot(Amount<Velocity> vRot) {
		this.vRot = vRot;
	}

	public Amount<Velocity> getvLO() {
		return vLO;
	}

	public void setvLO(Amount<Velocity> vLO) {
		this.vLO = vLO;
	}

	public Amount<Velocity> getvWind() {
		return vWind;
	}

	public void setvWind(Amount<Velocity> vWind) {
		this.vWind = vWind;
	}

	public Amount<Length> getObstacle() {
		return obstacle;
	}

	public void setObstacle(Amount<Length> obstacle) {
		this.obstacle = obstacle;
	}

	public Amount<Angle> getAlphaGround() {
		return alphaGround;
	}

	public void setAlphaGround(Amount<Angle> alphaGround) {
		this.alphaGround = alphaGround;
	}

	public List<Double> getAlphaDot() {
		return alphaDot;
	}

	public void setAlphaDot(List<Double> alphaDot) {
		this.alphaDot = alphaDot;
	}

	public List<Double> getGammaDot() {
		return gammaDot;
	}

	public void setGammaDot(List<Double> gammaDot) {
		this.gammaDot = gammaDot;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public List<Double> getcD() {
		return cD;
	}

	public void setcD(List<Double> cD) {
		this.cD = cD;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public List<Amount<Angle>> getTheta() {
		return theta;
	}

	public void setTheta(List<Amount<Angle>> theta) {
		this.theta = theta;
	}

	public List<Amount<Angle>> getGamma() {
		return gamma;
	}

	public void setGamma(List<Amount<Angle>> gamma) {
		this.gamma = gamma;
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public List<Amount<Velocity>> getSpeedTAS() {
		return speedTAS;
	}

	public void setSpeedTAS(List<Amount<Velocity>> speed) {
		this.speedTAS = speed;
	}

	public List<Amount<Velocity>> getRateOfClimb() {
		return rateOfClimb;
	}

	public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
		this.rateOfClimb = rateOfClimb;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getThrustHorizontal() {
		return thrustHorizontal;
	}

	public void setThrustHorizontal(List<Amount<Force>> thrustHorizontal) {
		this.thrustHorizontal = thrustHorizontal;
	}

	public List<Amount<Force>> getThrustVertical() {
		return thrustVertical;
	}

	public void setThrustVertical(List<Amount<Force>> thrustVertical) {
		this.thrustVertical = thrustVertical;
	}

	public List<Amount<Force>> getLift() {
		return lift;
	}

	public void setLift(List<Amount<Force>> lift) {
		this.lift = lift;
	}

	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Force>> getFriction() {
		return friction;
	}

	public void setFriction(List<Amount<Force>> friction) {
		this.friction = friction;
	}

	public List<Amount<Force>> getTotalForce() {
		return totalForce;
	}

	public void setTotalForce(List<Amount<Force>> totalForce) {
		this.totalForce = totalForce;
	}

	public List<Amount<Length>> getGroundDistance() {
		return groundDistance;
	}

	public void setGroundDistance(List<Amount<Length>> groundDistance) {
		this.groundDistance = groundDistance;
	}

	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
	}

	public double getkAlphaDot() {
		return kAlphaDot;
	}

	public void setkAlphaDot(double kAlphaDot) {
		this.kAlphaDot = kAlphaDot;
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

	public double getcLmaxTO() {
		return cLmaxTO;
	}

	public void setcLmaxTO(double cLmaxTO) {
		this.cLmaxTO = cLmaxTO;
	}

	public double getkGround() {
		return kGround;
	}

	public void setkGround(double kGround) {
		this.kGround = kGround;
	}

	public double getAlphaDotInitial() {
		return alphaDotInitial;
	}

	public void setAlphaDotInitial(double alphaDotInitial) {
		this.alphaDotInitial = alphaDotInitial;
	}

	public double getAlphaRed() {
		return alphaRed;
	}

	public void setAlphaRed(double alphaRed) {
		this.alphaRed = alphaRed;
	}

	public double getcL0() {
		return cL0;
	}

	public void setcL0(double cL0) {
		this.cL0 = cL0;
	}

	public double getcLground() {
		return cLground;
	}

	public void setcLground(double cLground) {
		this.cLground = cLground;
	}

	public double getkFailure() {
		return kFailure;
	}

	public void setkFailure(double kFailure) {
		this.kFailure = kFailure;
	}

	public TakeOffResultsMap getTakeOffResults() {
		return takeOffResults;
	}

	public void setTakeOffResults(TakeOffResultsMap takeOffResults) {
		this.takeOffResults = takeOffResults;
	}

	public double getkRot() {
		return kRot;
	}

	public double getKclMax() {
		return kcLMax;
	}

	public double getPhi() {
		return phi;
	}

	public MyInterpolatingFunction getContinuedTakeOffFitted() {
		return continuedTakeOffFitted;
	}

	public void setContinuedTakeOffFitted(MyInterpolatingFunction continuedTakeOffFitted) {
		this.continuedTakeOffFitted = continuedTakeOffFitted;
	}

	public MyInterpolatingFunction getAbortedTakeOffFitted() {
		return abortedTakeOffFitted;
	}

	public void setAbortedTakeOffFitted(MyInterpolatingFunction abortedTakeOffFitted) {
		this.abortedTakeOffFitted = abortedTakeOffFitted;
	}

	public Amount<Velocity> getV1() {
		return v1;
	}

	public void setV1(Amount<Velocity> v1) {
		this.v1 = v1;
	}

	public Amount<Length> getBalancedFieldLength() {
		return balancedFieldLength;
	}

	public void setBalancedFieldLength(Amount<Length> balancedFieldLength) {
		this.balancedFieldLength = balancedFieldLength;
	}

	public double[] getFailureSpeedArray() {
		return failureSpeedArray;
	}

	public void setFailureSpeedArray(double[] failureSpeedArray) {
		this.failureSpeedArray = failureSpeedArray;
	}

	public double[] getContinuedTakeOffArray() {
		return continuedTakeOffArray;
	}

	public void setContinuedTakeOffArray(double[] continuedTakeOffArray) {
		this.continuedTakeOffArray = continuedTakeOffArray;
	}

	public double[] getAbortedTakeOffArray() {
		return abortedTakeOffArray;
	}

	public void setAbortedTakeOffArray(double[] abortedTakeOffArray) {
		this.abortedTakeOffArray = abortedTakeOffArray;
	}

	public Amount<Duration> gettHold() {
		return tHold;
	}

	public void settHold(Amount<Duration> tHold) {
		this.tHold = tHold;
	}

	public Amount<Duration> gettEndHold() {
		return tEndHold;
	}

	public void settEndHold(Amount<Duration> tEndHold) {
		this.tEndHold = tEndHold;
	}

	public Amount<Duration> gettRot() {
		return tRot;
	}

	public void settRot(Amount<Duration> tRot) {
		this.tRot = tRot;
	}

	public Amount<Duration> gettEndRot() {
		return tEndRot;
	}

	public void settEndRot(Amount<Duration> tEndRot) {
		this.tEndRot = tEndRot;
	}

	public Amount<Duration> gettAlphaCost() {
		return tClimb;
	}

	public void settAlphaCost(Amount<Duration> tAlphaCost) {
		this.tClimb = tAlphaCost;
	}

	public double getKcLMax() {
		return kcLMax;
	}

	public void setKcLMax(double kcLMax) {
		this.kcLMax = kcLMax;
	}

	public Amount<Velocity> getvFailure() {
		return vFailure;
	}

	public void setvFailure(Amount<Velocity> vFailure) {
		this.vFailure = vFailure;
	}

	public boolean isAborted() {
		return isAborted;
	}

	public void setAborted(boolean isAborted) {
		this.isAborted = isAborted;
	}

	public Amount<Duration> gettClimb() {
		return tClimb;
	}

	public Amount<Duration> gettFaiulre() {
		return tFaiulre;
	}

	public void setkRot(double kRot) {
		this.kRot = kRot;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public Amount<Duration> gettRec() {
		return tRec;
	}

	public void settRec(Amount<Duration> tRec) {
		this.tRec = tRec;
	}

	public double getcLalphaFlap() {
		return cLalphaFlap;
	}

	public void setcLalphaFlap(double cLalphaFlap) {
		this.cLalphaFlap = cLalphaFlap;
	}

	public Amount<Mass> getMaxTakeOffMass() {
		return maxTakeOffMass;
	}

	public void setMaxTakeOffMass(Amount<Mass> maxTakeOffMass) {
		this.maxTakeOffMass = maxTakeOffMass;
	}

	public Amount<Length> getAltitude() {
		return altitude;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public List<Double> getFuelFlow() {
		return fuelFlow;
	}

	public void setFuelFlow(List<Double> fuelFlow) {
		this.fuelFlow = fuelFlow;
	}

	public Amount<Velocity> getV2() {
		return v2;
	}

	public void setV2(Amount<Velocity> v2) {
		this.v2 = v2;
	}

	public double[] getContinuedTakeOffArrayFitted() {
		return continuedTakeOffArrayFitted;
	}

	public void setContinuedTakeOffArrayFitted(double[] continuedTakeOffArrayFitted) {
		this.continuedTakeOffArrayFitted = continuedTakeOffArrayFitted;
	}

	public double[] getAbortedTakeOffArrayFitted() {
		return abortedTakeOffArrayFitted;
	}

	public void setAbortedTakeOffArrayFitted(double[] abortedTakeOffArrayFitted) {
		this.abortedTakeOffArrayFitted = abortedTakeOffArrayFitted;
	}

	public Double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(Double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public Amount<Length> getSpan() {
		return span;
	}

	public void setSpan(Amount<Length> span) {
		this.span = span;
	}

	public PowerPlant getThePowerPlant() {
		return thePowerPlant;
	}

	public void setThePowerPlant(PowerPlant thePowerPlant) {
		this.thePowerPlant = thePowerPlant;
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

	public Amount<Velocity> getVMC() {
		return vMC;
	}

	public void setVMC(Amount<Velocity> vMC) {
		this.vMC = vMC;
	}

	public boolean isTailStrike() {
		return isTailStrike;
	}

	public void setTailStrike(boolean isTailStrike) {
		this.isTailStrike = isTailStrike;
	}

	public Amount<Angle> getFuselageUpsweepAngle() {
		return fuselageUpsweepAngle;
	}

	public void setFuselageUpsweepAngle(Amount<Angle> fuselageUpsweepAngle) {
		this.fuselageUpsweepAngle = fuselageUpsweepAngle;
	}

	public ContinuousOutputModel getContinuousOutputModel() {
		return continuousOutputModel;
	}

	public void setContinuousOutputModel(ContinuousOutputModel continuousOutputModel) {
		this.continuousOutputModel = continuousOutputModel;
	}

	public List<Double> getTimeBreakPoints() {
		return timeBreakPoints;
	}

	public void setTimeBreakPoints(List<Double> timeBreakPoints) {
		this.timeBreakPoints = timeBreakPoints;
	}

	public List<Amount<Force>> getWeight() {
		return weight;
	}

	public void setWeight(List<Amount<Force>> weight) {
		this.weight = weight;
	}

	public FirstOrderIntegrator getTheIntegrator() {
		return theIntegrator;
	}

	public void setTheIntegrator(FirstOrderIntegrator theIntegrator) {
		this.theIntegrator = theIntegrator;
	}

	public FirstOrderDifferentialEquations getOde() {
		return ode;
	}

	public void setOde(FirstOrderDifferentialEquations ode) {
		this.ode = ode;
	}

	public Amount<Temperature> getDeltaTemperature() {
		return deltaTemperature;
	}

	public void setDeltaTemperature(Amount<Temperature> deltaTemperature) {
		this.deltaTemperature = deltaTemperature;
	}

	public List<Amount<Mass>> getFuelUsed() {
		return fuelUsed;
	}

	public void setFuelUsed(List<Amount<Mass>> fuelUsed) {
		this.fuelUsed = fuelUsed;
	}

	public Amount<Length> getWingToGroundDistance() {
		return wingToGroundDistance;
	}

	public void setWingToGroundDistance(Amount<Length> wingToGroundDistance) {
		this.wingToGroundDistance = wingToGroundDistance;
	}

	public double getTakeOffThrustCorrectionFactor() {
		return takeOffThrustCorrectionFactor;
	}

	public void setTakeOffThrustCorrectionFactor(double takeOffThrustCorrectionFactor) {
		this.takeOffThrustCorrectionFactor = takeOffThrustCorrectionFactor;
	}

	public double getAprThrustCorrectionFactor() {
		return aprThrustCorrectionFactor;
	}

	public void setAprThrustCorrectionFactor(double aprThrustCorrectionFactor) {
		this.aprThrustCorrectionFactor = aprThrustCorrectionFactor;
	}

	public double getGidlThrustCorrectionFactor() {
		return gidlThrustCorrectionFactor;
	}

	public void setGidlThrustCorrectionFactor(double gidlThrustCorrectionFactor) {
		this.gidlThrustCorrectionFactor = gidlThrustCorrectionFactor;
	}

	public double getTakeOffSfcCorrectionFactor() {
		return takeOffSfcCorrectionFactor;
	}

	public void setTakeOffSfcCorrectionFactor(double takeOffSfcCorrectionFactor) {
		this.takeOffSfcCorrectionFactor = takeOffSfcCorrectionFactor;
	}

	public double getAprSfcCorrectionFactor() {
		return aprSfcCorrectionFactor;
	}

	public void setAprSfcCorrectionFactor(double aprSfcCorrectionFactor) {
		this.aprSfcCorrectionFactor = aprSfcCorrectionFactor;
	}

	public double getGidlSfcCorrectionFactor() {
		return gidlSfcCorrectionFactor;
	}

	public void setGidlSfcCorrectionFactor(double gidlSfcCorrectionFactor) {
		this.gidlSfcCorrectionFactor = gidlSfcCorrectionFactor;
	}

	public double getTakeOffCalibrationFactorEmissionIndexNOx() {
		return takeOffCalibrationFactorEmissionIndexNOx;
	}

	public void setTakeOffCalibrationFactorEmissionIndexNOx(double takeOffCalibrationFactorEmissionIndexNOx) {
		this.takeOffCalibrationFactorEmissionIndexNOx = takeOffCalibrationFactorEmissionIndexNOx;
	}

	public double getTakeOffCalibrationFactorEmissionIndexCO() {
		return takeOffCalibrationFactorEmissionIndexCO;
	}

	public void setTakeOffCalibrationFactorEmissionIndexCO(double takeOffCalibrationFactorEmissionIndexCO) {
		this.takeOffCalibrationFactorEmissionIndexCO = takeOffCalibrationFactorEmissionIndexCO;
	}

	public double getTakeOffCalibrationFactorEmissionIndexHC() {
		return takeOffCalibrationFactorEmissionIndexHC;
	}

	public void setTakeOffCalibrationFactorEmissionIndexHC(double takeOffCalibrationFactorEmissionIndexHC) {
		this.takeOffCalibrationFactorEmissionIndexHC = takeOffCalibrationFactorEmissionIndexHC;
	}

	public double getTakeOffCalibrationFactorEmissionIndexSoot() {
		return takeOffCalibrationFactorEmissionIndexSoot;
	}

	public void setTakeOffCalibrationFactorEmissionIndexSoot(double takeOffCalibrationFactorEmissionIndexSoot) {
		this.takeOffCalibrationFactorEmissionIndexSoot = takeOffCalibrationFactorEmissionIndexSoot;
	}

	public double getTakeOffCalibrationFactorEmissionIndexCO2() {
		return takeOffCalibrationFactorEmissionIndexCO2;
	}

	public void setTakeOffCalibrationFactorEmissionIndexCO2(double takeOffCalibrationFactorEmissionIndexCO2) {
		this.takeOffCalibrationFactorEmissionIndexCO2 = takeOffCalibrationFactorEmissionIndexCO2;
	}

	public double getTakeOffCalibrationFactorEmissionIndexSOx() {
		return takeOffCalibrationFactorEmissionIndexSOx;
	}

	public void setTakeOffCalibrationFactorEmissionIndexSOx(double takeOffCalibrationFactorEmissionIndexSOx) {
		this.takeOffCalibrationFactorEmissionIndexSOx = takeOffCalibrationFactorEmissionIndexSOx;
	}

	public double getTakeOffCalibrationFactorEmissionIndexH2O() {
		return takeOffCalibrationFactorEmissionIndexH2O;
	}

	public void setTakeOffCalibrationFactorEmissionIndexH2O(double takeOffCalibrationFactorEmissionIndexH2O) {
		this.takeOffCalibrationFactorEmissionIndexH2O = takeOffCalibrationFactorEmissionIndexH2O;
	}

	public double getAprCalibrationFactorEmissionIndexNOx() {
		return aprCalibrationFactorEmissionIndexNOx;
	}

	public void setAprCalibrationFactorEmissionIndexNOx(double aprCalibrationFactorEmissionIndexNOx) {
		this.aprCalibrationFactorEmissionIndexNOx = aprCalibrationFactorEmissionIndexNOx;
	}

	public double getAprCalibrationFactorEmissionIndexCO() {
		return aprCalibrationFactorEmissionIndexCO;
	}

	public void setAprCalibrationFactorEmissionIndexCO(double aprCalibrationFactorEmissionIndexCO) {
		this.aprCalibrationFactorEmissionIndexCO = aprCalibrationFactorEmissionIndexCO;
	}

	public double getAprCalibrationFactorEmissionIndexHC() {
		return aprCalibrationFactorEmissionIndexHC;
	}

	public void setAprCalibrationFactorEmissionIndexHC(double aprCalibrationFactorEmissionIndexHC) {
		this.aprCalibrationFactorEmissionIndexHC = aprCalibrationFactorEmissionIndexHC;
	}

	public double getAprCalibrationFactorEmissionIndexSoot() {
		return aprCalibrationFactorEmissionIndexSoot;
	}

	public void setAprCalibrationFactorEmissionIndexSoot(double aprCalibrationFactorEmissionIndexSoot) {
		this.aprCalibrationFactorEmissionIndexSoot = aprCalibrationFactorEmissionIndexSoot;
	}

	public double getAprCalibrationFactorEmissionIndexCO2() {
		return aprCalibrationFactorEmissionIndexCO2;
	}

	public void setAprCalibrationFactorEmissionIndexCO2(double aprCalibrationFactorEmissionIndexCO2) {
		this.aprCalibrationFactorEmissionIndexCO2 = aprCalibrationFactorEmissionIndexCO2;
	}

	public double getAprCalibrationFactorEmissionIndexSOx() {
		return aprCalibrationFactorEmissionIndexSOx;
	}

	public void setAprCalibrationFactorEmissionIndexSOx(double aprCalibrationFactorEmissionIndexSOx) {
		this.aprCalibrationFactorEmissionIndexSOx = aprCalibrationFactorEmissionIndexSOx;
	}

	public double getAprCalibrationFactorEmissionIndexH2O() {
		return aprCalibrationFactorEmissionIndexH2O;
	}

	public void setAprCalibrationFactorEmissionIndexH2O(double aprCalibrationFactorEmissionIndexH2O) {
		this.aprCalibrationFactorEmissionIndexH2O = aprCalibrationFactorEmissionIndexH2O;
	}

	public double getGidlCalibrationFactorEmissionIndexNOx() {
		return gidlCalibrationFactorEmissionIndexNOx;
	}

	public void setGidlCalibrationFactorEmissionIndexNOx(double gidlCalibrationFactorEmissionIndexNOx) {
		this.gidlCalibrationFactorEmissionIndexNOx = gidlCalibrationFactorEmissionIndexNOx;
	}

	public double getGidlCalibrationFactorEmissionIndexCO() {
		return gidlCalibrationFactorEmissionIndexCO;
	}

	public void setGidlCalibrationFactorEmissionIndexCO(double gidlCalibrationFactorEmissionIndexCO) {
		this.gidlCalibrationFactorEmissionIndexCO = gidlCalibrationFactorEmissionIndexCO;
	}

	public double getGidlCalibrationFactorEmissionIndexHC() {
		return gidlCalibrationFactorEmissionIndexHC;
	}

	public void setGidlCalibrationFactorEmissionIndexHC(double gidlCalibrationFactorEmissionIndexHC) {
		this.gidlCalibrationFactorEmissionIndexHC = gidlCalibrationFactorEmissionIndexHC;
	}

	public double getGidlCalibrationFactorEmissionIndexSoot() {
		return gidlCalibrationFactorEmissionIndexSoot;
	}

	public void setGidlCalibrationFactorEmissionIndexSoot(double gidlCalibrationFactorEmissionIndexSoot) {
		this.gidlCalibrationFactorEmissionIndexSoot = gidlCalibrationFactorEmissionIndexSoot;
	}

	public double getGidlCalibrationFactorEmissionIndexCO2() {
		return gidlCalibrationFactorEmissionIndexCO2;
	}

	public void setGidlCalibrationFactorEmissionIndexCO2(double gidlCalibrationFactorEmissionIndexCO2) {
		this.gidlCalibrationFactorEmissionIndexCO2 = gidlCalibrationFactorEmissionIndexCO2;
	}

	public double getGidlCalibrationFactorEmissionIndexSOx() {
		return gidlCalibrationFactorEmissionIndexSOx;
	}

	public void setGidlCalibrationFactorEmissionIndexSOx(double gidlCalibrationFactorEmissionIndexSOx) {
		this.gidlCalibrationFactorEmissionIndexSOx = gidlCalibrationFactorEmissionIndexSOx;
	}

	public double getGidlCalibrationFactorEmissionIndexH2O() {
		return gidlCalibrationFactorEmissionIndexH2O;
	}

	public void setGidlCalibrationFactorEmissionIndexH2O(double gidlCalibrationFactorEmissionIndexH2O) {
		this.gidlCalibrationFactorEmissionIndexH2O = gidlCalibrationFactorEmissionIndexH2O;
	}

	public List<Amount<Mass>> getEmissionsNOx() {
		return emissionsNOx;
	}

	public void setEmissionsNOx(List<Amount<Mass>> emissionsNOx) {
		this.emissionsNOx = emissionsNOx;
	}

	public List<Amount<Mass>> getEmissionsCO() {
		return emissionsCO;
	}

	public void setEmissionsCO(List<Amount<Mass>> emissionsCO) {
		this.emissionsCO = emissionsCO;
	}

	public List<Amount<Mass>> getEmissionsHC() {
		return emissionsHC;
	}

	public void setEmissionsHC(List<Amount<Mass>> emissionsHC) {
		this.emissionsHC = emissionsHC;
	}

	public List<Amount<Mass>> getEmissionsSoot() {
		return emissionsSoot;
	}

	public void setEmissionsSoot(List<Amount<Mass>> emissionsSoot) {
		this.emissionsSoot = emissionsSoot;
	}

	public List<Amount<Mass>> getEmissionsCO2() {
		return emissionsCO2;
	}

	public void setEmissionsCO2(List<Amount<Mass>> emissionsCO2) {
		this.emissionsCO2 = emissionsCO2;
	}

	public List<Amount<Mass>> getEmissionsSOx() {
		return emissionsSOx;
	}

	public void setEmissionsSOx(List<Amount<Mass>> emissionsSOx) {
		this.emissionsSOx = emissionsSOx;
	}

	public List<Amount<Mass>> getEmissionsH2O() {
		return emissionsH2O;
	}

	public void setEmissionsH2O(List<Amount<Mass>> emissionsH2O) {
		this.emissionsH2O = emissionsH2O;
	}

	public List<Double> getMach() {
		return mach;
	}

	public void setMach(List<Double> mach) {
		this.mach = mach;
	}

	public List<Amount<Velocity>> getSpeedCAS() {
		return speedCAS;
	}

	public void setSpeedCAS(List<Amount<Velocity>> speedCAS) {
		this.speedCAS = speedCAS;
	}

	public List<Double> getCLStepHandler() {
		return cLStepHandler;
	}

	public void setCLStepHandler(List<Double> cLStepHandler) {
		this.cLStepHandler = cLStepHandler;
	}

	public List<Double> getCDStepHandler() {
		return cDStepHandler;
	}

	public void setCDStepHandler(List<Double> cDStepHandler) {
		this.cDStepHandler = cDStepHandler;
	}

	public List<Double> getLoadFactorStepHandler() {
		return loadFactorStepHandler;
	}

	public void setLoadFactorStepHandler(List<Double> loadFactorStepHandler) {
		this.loadFactorStepHandler = loadFactorStepHandler;
	}

	public List<Amount<Angle>> getAlphaStepHandler() {
		return alphaStepHandler;
	}

	public void setAlphaStepHandler(List<Amount<Angle>> alphaStepHandler) {
		this.alphaStepHandler = alphaStepHandler;
	}

	public List<Amount<Duration>> getTimeStepHandler() {
		return timeStepHandler;
	}

	public void setTimeStepHandler(List<Amount<Duration>> timeStepHandler) {
		this.timeStepHandler = timeStepHandler;
	}

	public List<Amount<Acceleration>> getAccelerationStepHandler() {
		return accelerationStepHandler;
	}

	public void setAccelerationStepHandler(List<Amount<Acceleration>> accelerationStepHandler) {
		this.accelerationStepHandler = accelerationStepHandler;
	}

	public List<Amount<Force>> getWeightStepHandler() {
		return weightStepHandler;
	}

	public void setWeightStepHandler(List<Amount<Force>> weightStepHandler) {
		this.weightStepHandler = weightStepHandler;
	}

	public List<Amount<Angle>> getGammaStepHandler() {
		return gammaStepHandler;
	}

	public void setGammaStepHandler(List<Amount<Angle>> gammaStepHandler) {
		this.gammaStepHandler = gammaStepHandler;
	}

	public List<Amount<Angle>> getThetaStepHandler() {
		return thetaStepHandler;
	}

	public void setThetaStepHandler(List<Amount<Angle>> thetaStepHandler) {
		this.thetaStepHandler = thetaStepHandler;
	}

	public List<Amount<Velocity>> getSpeedTASStepHandler() {
		return speedTASStepHandler;
	}

	public void setSpeedTASStepHandler(List<Amount<Velocity>> speedTASStepHandler) {
		this.speedTASStepHandler = speedTASStepHandler;
	}

	public List<Amount<Length>> getGroundDistanceStepHandler() {
		return groundDistanceStepHandler;
	}

	public void setGroundDistanceStepHandler(List<Amount<Length>> groundDistanceStepHandler) {
		this.groundDistanceStepHandler = groundDistanceStepHandler;
	}

	public List<Amount<Length>> getVerticalDistanceStepHandler() {
		return verticalDistanceStepHandler;
	}

	public void setVerticalDistanceStepHandler(List<Amount<Length>> verticalDistanceStepHandler) {
		this.verticalDistanceStepHandler = verticalDistanceStepHandler;
	}

	public List<Double> getAlphaDotStepHandler() {
		return alphaDotStepHandler;
	}

	public void setAlphaDotStepHandler(List<Double> alphaDotStepHandler) {
		this.alphaDotStepHandler = alphaDotStepHandler;
	}

}