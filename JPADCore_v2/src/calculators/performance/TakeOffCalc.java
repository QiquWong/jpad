package calculators.performance;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.performance.customdata.TakeOffResultsMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
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

	private Aircraft aircraft;
	private Amount<Duration> dtRot, dtHold,	
	dtRec = Amount.valueOf(3, SI.SECOND),
	tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tFaiulre = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
	private Amount<Mass> maxTakeOffMass; 
	private Amount<Velocity> vSTakeOff, vRot, vLO, vWind, v1, v2;
	private Amount<Length> altitude, wingToGroundDistance, obstacle, balancedFieldLength;
	private Amount<Angle> alphaGround, iw;
	private List<Double> alphaDot, gammaDot, cL, cD, loadFactor, sfc;
	private List<Amount<Angle>> alpha, theta, gamma;
	private List<Amount<Duration>> time;
	private List<Amount<Velocity>> speed, rateOfClimb;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> thrust, thrustHorizontal, thrustVertical, lift, drag, friction, totalForce;
	private List<Amount<Length>> groundDistance, verticalDistance;
	private double kAlphaDot, kcLMax, kRot, phi, cLmaxTO, kGround, alphaDotInitial, 
	alphaRed, cL0, cLground, kFailure;
	private Double vFailure;
	private boolean isAborted;
	
	private double cLalphaFlap, mach;

	// Statistics to be collected at every phase: (initialization of the lists through the builder
	private TakeOffResultsMap takeOffResults = new TakeOffResultsMap();
	// Interpolated function for balanced field length calculation
	MyInterpolatingFunction continuedTakeOffFitted = new MyInterpolatingFunction();
	MyInterpolatingFunction abortedTakeOffFitted = new MyInterpolatingFunction();
	MyInterpolatingFunction mu;
	MyInterpolatingFunction muBrake;
	MyInterpolatingFunction groundIdlePhi;
	
	// integration index
	private double[] failureSpeedArray, continuedTakeOffArray, abortedTakeOffArray,
	failureSpeedArrayFitted, continuedTakeOffArrayFitted, abortedTakeOffArrayFitted;

	//-------------------------------------------------------------------------------------
	// BUILDER:
	
	/**
	 * This builder is an overload of the previous one designed to allow the user 
	 * to perform the take-off distance calculation without doing all flaps analysis.
	 * This may come in handy when only few data are available.
	 */
	public TakeOffCalc(
			Aircraft aircraft,
			Amount<Length> altitude,
			double mach,
			Amount<Mass> maxTakeOffMass,
			Amount<Duration> dtRot,
			Amount<Duration> dtHold,
			double kcLMax,
			double kRot,
			double alphaDotInitial,
			double kFailure,
			MyInterpolatingFunction groundIdlePhi,
			double phi,
			double kAlphaDot,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Length> wingToGroundDistance,
			Amount<Length> obstacle,
			Amount<Velocity> vWind,
			Amount<Angle> alphaGround,
			Amount<Angle> iw,
			double cLmaxTO,
			double cLZeroTO,
			double cLalphaFlap
			) {

		// Required data
		this.aircraft = aircraft;
		this.altitude = altitude;
		this.mach = mach;
		this.maxTakeOffMass = maxTakeOffMass;
		this.dtRot = dtRot;
		this.dtHold = dtHold;
		this.kcLMax = kcLMax;
		this.kRot = kRot;
		this.alphaDotInitial = alphaDotInitial;
		this.kFailure = kFailure;
		this.groundIdlePhi = groundIdlePhi;
		this.phi = phi;
		this.kAlphaDot = kAlphaDot;
		this.mu = mu;
		this.muBrake = muBrake;
		this.wingToGroundDistance = wingToGroundDistance;
		this.obstacle = obstacle;
		this.vWind = vWind;
		this.alphaGround = alphaGround;
		this.iw = iw;
		this.cLmaxTO = cLmaxTO;
		this.cLalphaFlap = cLalphaFlap;
		this.cL0 = cLZeroTO;
		this.cLground = cLZeroTO + (cLalphaFlap*iw.getEstimatedValue());
		
		// Reference velocities definition
		vSTakeOff = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						getAltitude().doubleValue(SI.METER),
						maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue(),
						aircraft.getWing().getSurface().getEstimatedValue(),
						cLmaxTO
						),
				SI.METERS_PER_SECOND);
		vRot = vSTakeOff.times(kRot);
		
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxTO = " + cLmaxTO);
		System.out.println("CL0 = " + cLZeroTO);
		System.out.println("CLground = " + cLground);
		System.out.println("VsTO = " + vSTakeOff);
		System.out.println("VRot = " + vRot);
		System.out.println("-----------------------------------------------------------\n");

		// McCormick interpolated function --> See the excel file into JPAD DOCS
		double hb = wingToGroundDistance.divide(aircraft.getWing().getSpan().times(Math.PI/4)).getEstimatedValue();
		kGround = - 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
				+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055;
		
		// List initialization
		this.time = new ArrayList<Amount<Duration>>();
		this.speed = new ArrayList<Amount<Velocity>>();
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
		this.sfc = new ArrayList<Double>();
		
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
		time.clear();
		speed.clear();
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
		sfc.clear();
		
		tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tFaiulre = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		
		vFailure = null;
		isAborted = false;
		
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
	public void calculateTakeOffDistanceODE(Double vFailure, boolean isAborted) {

		System.out.println("---------------------------------------------------");
		System.out.println("CalcTakeOff :: ODE integration\n\n");

		int i=0;
		double newAlphaRed = 0.0;
		alphaRed = 0.0;
		
		v2 = Amount.valueOf(10000.0, SI.METERS_PER_SECOND); // initialization to an impossible speed
		
		while (Math.abs(((v2.divide(vSTakeOff).getEstimatedValue()) - 1.2)) >= 0.009) {

			if(i >= 1) {
				if(newAlphaRed <= 0.0)
					alphaRed = newAlphaRed;
				else
					break;
			}
			
			initialize();
			
			this.isAborted = isAborted;
			// failure check
			if(vFailure == null)
				this.vFailure = 10000.0; // speed impossible to reach --> no failure!!
			else
				this.vFailure = vFailure;

			FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
					1e-8,
					1,
					1e-16,
					1e-16
					);
			FirstOrderDifferentialEquations ode = new DynamicsEquationsTakeOff();

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

					if(t < tRec.getEstimatedValue())
						return x[1] - TakeOffCalc.this.vFailure;
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

					if(t < tRec.getEstimatedValue()) {
						return speed - vRot.getEstimatedValue();
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
									"\n\tx[2] = gamma = " + x[2] + " �" +
									"\n\tx[3] = altitude = " + x[3] + " m"
							);

					tRot = Amount.valueOf(t, SI.SECOND);

					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
					takeOffResults.collectResults(
							time.get(time.size()-1),
							thrust.get(thrust.size()-1),
							thrustHorizontal.get(thrustHorizontal.size()-1),
							thrustVertical.get(thrustVertical.size()-1),
							friction.get(friction.size()-1),
							lift.get(lift.size()-1),
							drag.get(drag.size()-1),
							totalForce.get(totalForce.size()-1),
							loadFactor.get(loadFactor.size()-1),
							speed.get(speed.size()-1),
							rateOfClimb.get(rateOfClimb.size()-1),
							acceleration.get(acceleration.size()-1),
							groundDistance.get(groundDistance.size()-1),
							verticalDistance.get(verticalDistance.size()-1),
							alpha.get(alpha.size()-1),
							alphaDot.get(alphaDot.size()-1),
							gamma.get(gamma.size()-1),
							gammaDot.get(gammaDot.size()-1),
							theta.get(theta.size()-1),
							cL.get(cL.size()-1),
							cD.get(cD.size()-1)
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

					return t - (tHold.plus(dtHold).getEstimatedValue());
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND BAR HOLDING");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tEndHold = Amount.valueOf(t, SI.SECOND);

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

					return x[3] - obstacle.getEstimatedValue();
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF AIRBORNE PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " �" +
									"\n\tx[3] = altitude = " + x[3] + " m"
							);

					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF AIRBORNE PHASE ...");
					takeOffResults.collectResults(
							time.get(time.size()-1),
							thrust.get(thrust.size()-1),
							thrustHorizontal.get(thrustHorizontal.size()-1),
							thrustVertical.get(thrustVertical.size()-1),
							friction.get(friction.size()-1),
							lift.get(lift.size()-1),
							drag.get(drag.size()-1),
							totalForce.get(totalForce.size()-1),
							loadFactor.get(loadFactor.size()-1),
							speed.get(speed.size()-1),
							rateOfClimb.get(rateOfClimb.size()-1),
							acceleration.get(acceleration.size()-1),
							groundDistance.get(groundDistance.size()-1),
							verticalDistance.get(verticalDistance.size()-1),
							alpha.get(alpha.size()-1),
							alphaDot.get(alphaDot.size()-1),
							gamma.get(gamma.size()-1),
							gammaDot.get(gammaDot.size()-1),
							theta.get(theta.size()-1),
							cL.get(cL.size()-1),
							cD.get(cD.size()-1)
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

					return t - (tFaiulre.plus(dtRec).getEstimatedValue());
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFAILURE RECOGNITION --> BRAKES ACTIVATED");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tRec = Amount.valueOf(t, SI.SECOND);

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

					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};

			if(!isAborted) {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehEndConstantCL, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-7, 50);
			}
			else {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckBrakes, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-7, 50);
			}

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] x = interpolator.getInterpolatedState();

					// CHECK TO BE DONE ONLY IF isAborted IS FALSE!!
					if(!isAborted) {

						// CHECK ON LOAD FACTOR --> END ROTATION WHEN n=1
						if((t > tRot.getEstimatedValue()) && (tEndRot.getEstimatedValue() == 10000.0) &&
								(TakeOffCalc.this.getLoadFactor().get(TakeOffCalc.this.getLoadFactor().size()-1) > 1) &&
								(TakeOffCalc.this.getLoadFactor().get(TakeOffCalc.this.getLoadFactor().size()-2) < 1)) {
							System.out.println("\n\t\tEND OF ROTATION PHASE");
							System.out.println(
									"\n\tx[0] = s = " + x[0] + " m" +
											"\n\tx[1] = V = " + x[1] + " m/s" + 
											"\n\tx[2] = gamma = " + x[2] + " �" +
											"\n\tx[3] = altitude = " + x[3] + " m"+
											"\n\tt = " + t + " s"
									);
							// COLLECTING DATA IN TakeOffResultsMap
							System.out.println("\n\tCOLLECTING DATA AT THE END OF ROTATION PHASE ...");
							takeOffResults.collectResults(
									time.get(time.size()-1),
									thrust.get(thrust.size()-1),
									thrustHorizontal.get(thrustHorizontal.size()-1),
									thrustVertical.get(thrustVertical.size()-1),
									friction.get(friction.size()-1),
									lift.get(lift.size()-1),
									drag.get(drag.size()-1),
									totalForce.get(totalForce.size()-1),
									loadFactor.get(loadFactor.size()-1),
									speed.get(speed.size()-1),
									rateOfClimb.get(rateOfClimb.size()-1),
									acceleration.get(acceleration.size()-1),
									groundDistance.get(groundDistance.size()-1),
									verticalDistance.get(verticalDistance.size()-1),
									alpha.get(alpha.size()-1),
									alphaDot.get(alphaDot.size()-1),
									gamma.get(gamma.size()-1),
									gammaDot.get(gammaDot.size()-1),
									theta.get(theta.size()-1),
									cL.get(cL.size()-1),
									cD.get(cD.size()-1)
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tEndRot = Amount.valueOf(t, SI.SECOND);
							vLO = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
						}
						// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
						if((t > tEndRot.getEstimatedValue()) && 
								(TakeOffCalc.this.getcL().get(TakeOffCalc.this.getcL().size()-1) > kcLMax*cLmaxTO) &&
								((TakeOffCalc.this.getcL().get(TakeOffCalc.this.getcL().size()-2) < kcLMax*cLmaxTO))) {
							System.out.println("\n\t\tBEGIN BAR HOLDING");
							System.out.println(
									"\n\tCL = " + ((DynamicsEquationsTakeOff)ode).cL(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t
											) + 
									"\n\tAlpha Body = " + ((DynamicsEquationsTakeOff)ode).alpha + " �" + 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tHold = Amount.valueOf(t, SI.SECOND);
						}
						// CHECK ON LOAD FACTOR TO ENSTABLISH WHEN n=1 WHILE DECREASING ALPHA AND CL
						if((t > tEndHold.getEstimatedValue()) && (tClimb.getEstimatedValue() == 10000.0) &&
								(TakeOffCalc.this.getLoadFactor().get(TakeOffCalc.this.getLoadFactor().size()-1) < 1) &&
								(TakeOffCalc.this.getLoadFactor().get(TakeOffCalc.this.getLoadFactor().size()-2) > 1)) {
							System.out.println("\n\t\tLOAD FACTOR = 1 IN CLIMB");
							System.out.println( 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tClimb = Amount.valueOf(t, SI.SECOND);
						}
					}

					// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					// TIME:
					TakeOffCalc.this.getTime().add(Amount.valueOf(t, SI.SECOND));
					//----------------------------------------------------------------------------------------
					// SPEED:
					TakeOffCalc.this.getSpeed().add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
					//----------------------------------------------------------------------------------------
					// THRUST:
					TakeOffCalc.this.getThrust().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3]),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// THRUST HORIZONTAL:
					TakeOffCalc.this.getThrustHorizontal().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.cos(
									Amount.valueOf(
											((DynamicsEquationsTakeOff)ode).alpha,
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// THRUST VERTICAL:
					TakeOffCalc.this.getThrustVertical().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.sin(
									Amount.valueOf(
											((DynamicsEquationsTakeOff)ode).alpha,
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									),
							SI.NEWTON)
							);
					//--------------------------------------------------------------------------------
					// SFC:
					TakeOffCalc.this.getSfc().add(
							(TakeOffCalc.this.getThrust().get(
									TakeOffCalc.this.getThrust().size()-1
									)
									.doubleValue(SI.NEWTON))
							*(0.224809)*(0.454/60)
							*EngineDatabaseManager.getSFC(
									SpeedCalc.calculateMach(
											x[3],
											x[1]
											),
									x[3],
									EngineDatabaseManager.getThrustRatio(
											SpeedCalc.calculateMach(
													x[3],
													x[1]
													),
											x[3],
											TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
											TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.TAKE_OFF,
											TakeOffCalc.this.getAircraft().getPowerPlant()
											),
									TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
									TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									TakeOffCalc.this.getAircraft().getPowerPlant()
									)
							);

					//--------------------------------------------------------------------------------
					// FRICTION:
					if(!isAborted) {
						if(t < tEndRot.getEstimatedValue())
							TakeOffCalc.this.getFriction().add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).mu(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
						else
							TakeOffCalc.this.getFriction().add(Amount.valueOf(0.0, SI.NEWTON));
					}
					else {
						if(t < tRec.getEstimatedValue())
							TakeOffCalc.this.getFriction().add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).mu(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
						else
							TakeOffCalc.this.getFriction().add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).muBrake(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// LIFT:
					TakeOffCalc.this.getLift().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).lift(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// DRAG:
					TakeOffCalc.this.getDrag().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).drag(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// TOTAL FORCE:
					if(!isAborted) {
						TakeOffCalc.this.getTotalForce().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										)
								- ((DynamicsEquationsTakeOff)ode).drag(
										x[1],
										((DynamicsEquationsTakeOff)ode).alpha,
										x[2],
										t)
								- ((DynamicsEquationsTakeOff)ode).mu(x[1])
								*(((DynamicsEquationsTakeOff)ode).weight
										- ((DynamicsEquationsTakeOff)ode).lift(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t))
								- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
										Amount.valueOf(
												x[2],
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
								SI.NEWTON)
								);
					}
					else {
						if(t < tRec.getEstimatedValue())
							TakeOffCalc.this.getTotalForce().add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.cos(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											)
									- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
									- ((DynamicsEquationsTakeOff)ode).mu(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t))
									- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
											Amount.valueOf(
													x[2],
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
									SI.NEWTON)
									);
						else
							TakeOffCalc.this.getTotalForce().add(Amount.valueOf(
									- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
									- ((DynamicsEquationsTakeOff)ode).muBrake(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t))
									- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
											Amount.valueOf(
													x[2],
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					TakeOffCalc.this.getLoadFactor().add(
							((DynamicsEquationsTakeOff)ode).lift(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t)
							/(((DynamicsEquationsTakeOff)ode).weight*Math.cos(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
							);
					//----------------------------------------------------------------------------------------
					// RATE OF CLIMB:
					TakeOffCalc.this.getRateOfClimb().add(Amount.valueOf(
							x[1]*Math.sin(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
							SI.METERS_PER_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					if(!isAborted)
						TakeOffCalc.this.getAcceleration().add(Amount.valueOf(
								(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
								*(((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										)
										- ((DynamicsEquationsTakeOff)ode).drag(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t)
										- ((DynamicsEquationsTakeOff)ode).mu(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
												- ((DynamicsEquationsTakeOff)ode).lift(
														x[1],
														((DynamicsEquationsTakeOff)ode).alpha,
														x[2],
														t))
										- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
												Amount.valueOf(
														x[2],
														NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
								SI.METERS_PER_SQUARE_SECOND)
								);
					else {
						if(t < tRec.getEstimatedValue())
							TakeOffCalc.this.getAcceleration().add(Amount.valueOf(
									(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
									*(((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.cos(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											)
											- ((DynamicsEquationsTakeOff)ode).drag(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)
											- ((DynamicsEquationsTakeOff)ode).mu(x[1])
											*(((DynamicsEquationsTakeOff)ode).weight
													- ((DynamicsEquationsTakeOff)ode).lift(
															x[1],
															((DynamicsEquationsTakeOff)ode).alpha,
															x[2],
															t))
											- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
													Amount.valueOf(
															x[2],
															NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
									SI.METERS_PER_SQUARE_SECOND)
									);
						else
							TakeOffCalc.this.getAcceleration().add(Amount.valueOf(
									(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
									*(- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
											- ((DynamicsEquationsTakeOff)ode).muBrake(x[1])
											*(((DynamicsEquationsTakeOff)ode).weight
													- ((DynamicsEquationsTakeOff)ode).lift(
															x[1],
															((DynamicsEquationsTakeOff)ode).alpha,
															x[2],
															t))
											- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
													Amount.valueOf(
															x[2],
															NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
									SI.METERS_PER_SQUARE_SECOND)
									);
					}
					//----------------------------------------------------------------------------------------
					// GROUND DISTANCE:
					TakeOffCalc.this.getGroundDistance().add(Amount.valueOf(
							x[0],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// VERTICAL DISTANCE:
					TakeOffCalc.this.getVerticalDistance().add(Amount.valueOf(
							x[3],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					TakeOffCalc.this.getAlpha().add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).alpha,
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					TakeOffCalc.this.getGamma().add(Amount.valueOf(
							x[2],
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					TakeOffCalc.this.getAlphaDot().add(
							((DynamicsEquationsTakeOff)ode).alphaDot(t)
							); 
					//----------------------------------------------------------------------------------------
					// GAMMA DOT:
					if(t <= tEndRot.getEstimatedValue())
						TakeOffCalc.this.getGammaDot().add(0.0);
					else
						TakeOffCalc.this.getGammaDot().add(57.3*(AtmosphereCalc.g0.getEstimatedValue()/
								(((DynamicsEquationsTakeOff)ode).weight*x[1]))*(
										((DynamicsEquationsTakeOff)ode).lift(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t) 
										+ (((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t, x[3])*Math.sin(Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())
												)
										- ((DynamicsEquationsTakeOff)ode).weight*Math.cos(Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).gamma,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
												))
								);
					//----------------------------------------------------------------------------------------
					// THETA:
					TakeOffCalc.this.getTheta().add(Amount.valueOf(
							x[2] + ((DynamicsEquationsTakeOff)ode).alpha,
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					TakeOffCalc.this.getcL().add(
							((DynamicsEquationsTakeOff)ode).cL(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t
									)
							);
					//----------------------------------------------------------------------------------------
					// CD:
					TakeOffCalc.this.getcD().add(
							((DynamicsEquationsTakeOff)ode).cD(
									TakeOffCalc.this.getcL().get(
											TakeOffCalc.this.getcL().size()-1)
									)
							);
					//----------------------------------------------------------------------------------------
				}
			};
			theIntegrator.addStepHandler(stepHandler);

			double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0}; // initial state
			theIntegrator.integrate(ode, 0.0, xAt0, 100, xAt0); // now xAt0 contains final state

			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

			if(isAborted) {
					break;
			}
			
			//--------------------------------------------------------------------------------
			// NEW ALPHA REDUCTION RATE 
			if(((v2.divide(vSTakeOff).getEstimatedValue()) - 1.2) >= 0.0)
				newAlphaRed = alphaRed + 0.1;
			else
				newAlphaRed = alphaRed - 0.1;
			
			i++;
		}
		
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
	public void calculateBalancedFieldLength() {

//		final PrintStream originalOut = System.out;
//		PrintStream filterStream = new PrintStream(new OutputStream() {
//		    public void write(int b) {
//		         // write nothing
//		    }
//		});
//		System.setOut(filterStream);
		
		// failure speed array
		failureSpeedArray = MyArrayUtils.linspace(
				vSTakeOff.times(0.5).getEstimatedValue(),
				vRot.getEstimatedValue(),
				4);
		// continued take-off array
		continuedTakeOffArray = new double[failureSpeedArray.length]; 
		// aborted take-off array
		abortedTakeOffArray = new double[failureSpeedArray.length];

		// iterative take-off distance calculation for both conditions
		for(int i=0; i<failureSpeedArray.length; i++) {
			calculateTakeOffDistanceODE(failureSpeedArray[i], false);
			continuedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
			calculateTakeOffDistanceODE(failureSpeedArray[i], true);
			abortedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
		}

		MyInterpolatingFunction continuedTakeOffFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction abortedTakeOffFunction = new MyInterpolatingFunction();
		
		continuedTakeOffFunction.interpolateLinear(failureSpeedArray, continuedTakeOffArray);
		abortedTakeOffFunction.interpolateLinear(failureSpeedArray, abortedTakeOffArray);
		
		failureSpeedArrayFitted = MyArrayUtils.linspace(
				vSTakeOff.times(0.5).getEstimatedValue(),
				vRot.getEstimatedValue(),
				1000);
		continuedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
		abortedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
		
		for(int i=0; i<failureSpeedArrayFitted.length; i++) {
			
			continuedTakeOffArrayFitted[i] = continuedTakeOffFunction.value(failureSpeedArrayFitted[i]);
			abortedTakeOffArrayFitted[i] = abortedTakeOffFunction.value(failureSpeedArrayFitted[i]);
			
		}
		
		// arrays intersection
		double[] intersection = MyArrayUtils.intersectArraysSimple(
				continuedTakeOffArrayFitted,
				abortedTakeOffArrayFitted);
		for(int i=0; i<intersection.length; i++)
			if(intersection[i] != 0.0) {
				balancedFieldLength = Amount.valueOf(intersection[i], SI.METER);
				v1 = Amount.valueOf(failureSpeedArrayFitted[i], SI.METERS_PER_SECOND);
			}
		
		// write again
//		System.setOut(originalOut);
	}

	/**************************************************************************************
	 * This method allows users to plot all take-off performance producing several output charts
	 * which have time or ground distance as independent variables.
	 *
	 * @author Vittorio Trifari
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createTakeOffCharts(String takeOffFolderPath) throws InstantiationException, IllegalAccessException {

		System.out.println("\n---------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE-----------");

		// data setup
		double[] time = new double[getTime().size()];
		for(int i=0; i<time.length; i++)
			time[i] = getTime().get(i).getEstimatedValue();

		double[] verticalDistance = new double[getVerticalDistance().size()];
		for(int i=0; i<verticalDistance.length; i++)
			verticalDistance[i] = getVerticalDistance().get(i).getEstimatedValue();

		double[] groundDistance = new double[getGroundDistance().size()];
		for(int i=0; i<groundDistance.length; i++)
			groundDistance[i] = getGroundDistance().get(i).getEstimatedValue();

		double[] thrust = new double[getThrust().size()];
		for(int i=0; i<thrust.length; i++)
			thrust[i] = getThrust().get(i).getEstimatedValue();

		double[] thrustHorizontal = new double[getThrustHorizontal().size()];
		for(int i=0; i<thrustHorizontal.length; i++)
			thrustHorizontal[i] = getThrustHorizontal().get(i).getEstimatedValue();

		double[] thrustVertical = new double[getThrustVertical().size()];
		for(int i=0; i<thrustVertical.length; i++)
			thrustVertical[i] = getThrustVertical().get(i).getEstimatedValue();

		double[] lift = new double[getLift().size()];
		for(int i=0; i<lift.length; i++)
			lift[i] = getLift().get(i).getEstimatedValue();

		double[] drag = new double[getDrag().size()];
		for(int i=0; i<drag.length; i++)
			drag[i] = getDrag().get(i).getEstimatedValue();

		double[] friction = new double[getFriction().size()];
		for(int i=0; i<friction.length; i++)
			friction[i] = getFriction().get(i).getEstimatedValue();

		double[] totalForce = new double[getTotalForce().size()];
		for(int i=0; i<totalForce.length; i++)
			totalForce[i] = getTotalForce().get(i).getEstimatedValue();

		double[] loadFactor = new double[getLoadFactor().size()];
		for(int i=0; i<loadFactor.length; i++)
			loadFactor[i] = getLoadFactor().get(i);

		double[] acceleration = new double[getAcceleration().size()];
		for(int i=0; i<acceleration.length; i++)
			acceleration[i] = getAcceleration().get(i).getEstimatedValue();

		double[] speed = new double[getSpeed().size()];
		for(int i=0; i<speed.length; i++)
			speed[i] = getSpeed().get(i).getEstimatedValue();

		double[] alpha = new double[getAlpha().size()];
		for(int i=0; i<alpha.length; i++)
			alpha[i] = getAlpha().get(i).getEstimatedValue();

		double[] gamma = new double[getGamma().size()];
		for(int i=0; i<gamma.length; i++)
			gamma[i] = getGamma().get(i).getEstimatedValue();

		double[] theta = new double[getTime().size()];
		for(int i=0; i<theta.length; i++)
			theta[i] = getTheta().get(i).getEstimatedValue();

		double[] alphaDot = new double[getAlphaDot().size()];
		for(int i=0; i<alphaDot.length; i++)
			alphaDot[i] = getAlphaDot().get(i);

		double[] gammaDot = new double[getGammaDot().size()];
		for(int i=0; i<gammaDot.length; i++)
			gammaDot[i] = getGammaDot().get(i);

		double[] cL = new double[getcL().size()];
		for(int i=0; i<cL.length; i++)
			cL[i] = getcL().get(i);

		double[] rateOfClimb = new double[getRateOfClimb().size()];
		for(int i=0; i<rateOfClimb.length; i++)
			rateOfClimb[i] = getRateOfClimb().get(i).getEstimatedValue();

		double[] weightVertical = new double[getTime().size()];
		for(int i=0; i<weightVertical.length; i++)
			weightVertical[i] = maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue()
			*Math.cos(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

		double[] weightHorizontal = new double[getTime().size()];
		for(int i=0; i<weightHorizontal.length; i++)
			weightHorizontal[i] = maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue()
			*Math.sin(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

		if(!isAborted) {
			// take-off trajectory
			MyChartToFileUtils.plotNoLegend(
					groundDistance, verticalDistance,
					0.0, null, 0.0, null,
					"Ground Distance", "Altitude", "m", "m",
					takeOffFolderPath, "TakeOff_Trajectory");

			// vertical distance v.s. time
			MyChartToFileUtils.plotNoLegend(
					time, verticalDistance,
					0.0, null, 0.0, null,
					"Time", "Altitude", "s", "m",
					takeOffFolderPath, "Altitude_evolution");
		}
		
		// speed v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, speed,
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				takeOffFolderPath, "Speed_evolution");

		// speed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, speed,
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				takeOffFolderPath, "Speed_vs_GroundDistance");

		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, acceleration,
				0.0, null, null, null,
				"Time", "Acceleration", "s", "m/(s^2)",
				takeOffFolderPath, "Acceleration_evolution");

		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				groundDistance, acceleration,
				0.0, null, null, null,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				takeOffFolderPath, "Acceleration_vs_GroundDistance");

		// load factor v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, loadFactor,
				0.0, null, 0.0, null,
				"Time", "Load Factor", "s", "",
				takeOffFolderPath, "LoadFactor_evolution");

		// load factor v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, loadFactor,
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				takeOffFolderPath, "LoadFactor_vs_GroundDistance");

		if(!isAborted) {
			// Rate of Climb v.s. Time
			MyChartToFileUtils.plotNoLegend(
					time, rateOfClimb,
					0.0, null, 0.0, null,
					"Time", "Rate of Climb", "s", "m/s",
					takeOffFolderPath, "RateOfClimb_evolution");

			// Rate of Climb v.s. Ground distance
			MyChartToFileUtils.plotNoLegend(
					groundDistance, rateOfClimb,
					0.0, null, 0.0, null,
					"Ground distance", "Rate of Climb", "m", "m/s",
					takeOffFolderPath, "RateOfClimb_vs_GroundDistance");
		}
		
		// CL v.s. Time
		MyChartToFileUtils.plotNoLegend(
				time, cL,
				0.0, null, 0.0, null,
				"Time", "CL", "s", "",
				takeOffFolderPath, "CL_evolution");

		// CL v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, cL,
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				takeOffFolderPath, "CL_vs_GroundDistance");

		// Horizontal Forces v.s. Time
		double[][] xMatrix1 = new double[5][totalForce.length];
		for(int i=0; i<xMatrix1.length; i++)
			xMatrix1[i] = time;

		double[][] yMatrix1 = new double[5][totalForce.length];
		yMatrix1[0] = totalForce;
		yMatrix1[1] = thrustHorizontal;
		yMatrix1[2] = drag;
		yMatrix1[3] = friction;
		yMatrix1[4] = weightHorizontal;

		MyChartToFileUtils.plot(
				xMatrix1, yMatrix1,
				0.0, null, null, null,
				"Time", "Horizontal Forces", "s", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
				takeOffFolderPath, "HorizontalForces_evolution");

		// Horizontal Forces v.s. Ground Distance
		double[][] xMatrix2 = new double[5][totalForce.length];
		for(int i=0; i<xMatrix2.length; i++)
			xMatrix2[i] = groundDistance;

		double[][] yMatrix2 = new double[5][totalForce.length];
		yMatrix2[0] = totalForce;
		yMatrix2[1] = thrustHorizontal;
		yMatrix2[2] = drag;
		yMatrix2[3] = friction;
		yMatrix2[4] = weightHorizontal;

		MyChartToFileUtils.plot(
				xMatrix2, yMatrix2,
				0.0, null, null, null,
				"Ground Distance", "Horizontal Forces", "m", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
				takeOffFolderPath, "HorizontalForces_vs_GroundDistance");

		// Vertical Forces v.s. Time
		double[][] xMatrix3 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix3.length; i++)
			xMatrix3[i] = time;

		double[][] yMatrix3 = new double[3][totalForce.length];
		yMatrix3[0] = lift;
		yMatrix3[1] = thrustVertical;
		yMatrix3[2] = weightVertical;

		MyChartToFileUtils.plot(
				xMatrix3, yMatrix3,
				0.0, null, null, null,
				"Time", "Vertical Forces", "s", "N",
				new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
				takeOffFolderPath, "VerticalForces_evolution");

		// Vertical Forces v.s. ground distance
		double[][] xMatrix4 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix4.length; i++)
			xMatrix4[i] = groundDistance;

		double[][] yMatrix4 = new double[3][totalForce.length];
		yMatrix4[0] = lift;
		yMatrix4[1] = thrustVertical;
		yMatrix4[2] = weightVertical;

		MyChartToFileUtils.plot(
				xMatrix4, yMatrix4,
				0.0, null, null, null,
				"Ground distance", "Vertical Forces", "m", "N",
				new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
				takeOffFolderPath, "VerticalForces_vs_GroundDistance");

		if(!isAborted) {
			// Angles v.s. time
			double[][] xMatrix5 = new double[3][totalForce.length];
			for(int i=0; i<xMatrix5.length; i++)
				xMatrix5[i] = time;

			double[][] yMatrix5 = new double[3][totalForce.length];
			yMatrix5[0] = alpha;
			yMatrix5[1] = theta;
			yMatrix5[2] = gamma;

			MyChartToFileUtils.plot(
					xMatrix5, yMatrix5,
					0.0, null, null, null,
					"Time", "Angles", "s", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					takeOffFolderPath, "Angles_evolution");

			// Angles v.s. Ground Distance
			double[][] xMatrix6 = new double[3][totalForce.length];
			for(int i=0; i<xMatrix6.length; i++)
				xMatrix6[i] = groundDistance;

			double[][] yMatrix6 = new double[3][totalForce.length];
			yMatrix6[0] = alpha;
			yMatrix6[1] = theta;
			yMatrix6[2] = gamma;

			MyChartToFileUtils.plot(
					xMatrix6, yMatrix6,
					0.0, null, null, null,
					"Ground Distance", "Angles", "m", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					takeOffFolderPath, "Angles_vs_GroundDistance");

			// Angular velocity v.s. time
			double[][] xMatrix7 = new double[2][totalForce.length];
			for(int i=0; i<xMatrix7.length; i++)
				xMatrix7[i] = time;

			double[][] yMatrix7 = new double[2][totalForce.length];
			yMatrix7[0] = alphaDot;
			yMatrix7[1] = gammaDot;

			MyChartToFileUtils.plot(
					xMatrix7, yMatrix7,
					0.0, null, null, null,
					"Time", "Angular Velocity", "s", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					takeOffFolderPath, "AngularVelocity_evolution");

			// Angular velocity v.s. Ground Distance
			double[][] xMatrix8 = new double[2][totalForce.length];
			for(int i=0; i<xMatrix8.length; i++)
				xMatrix8[i] = groundDistance;

			double[][] yMatrix8 = new double[2][totalForce.length];
			yMatrix8[0] = alphaDot;
			yMatrix8[1] = gammaDot;

			MyChartToFileUtils.plot(
					xMatrix8, yMatrix8,
					0.0, null, null, null,
					"Ground Distance", "Angular Velocity", "m", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					takeOffFolderPath, "AngularVelocity_vs_GroundDistance");
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
	public void createBalancedFieldLengthChart(String takeOffFolderPath) {

		System.out.println("\n-------WRITING BALANCED TAKE-OFF DISTANCE CHART TO FILE--------");

		for(int i=0; i<failureSpeedArrayFitted.length; i++)
			failureSpeedArrayFitted[i] = failureSpeedArrayFitted[i]/vSTakeOff.getEstimatedValue();

		double[][] xArray = new double[][]
				{failureSpeedArrayFitted, failureSpeedArrayFitted};
		double[][] yArray = new double[][]
				{continuedTakeOffArrayFitted, abortedTakeOffArrayFitted};

		MyChartToFileUtils.plot(
				xArray, yArray,
				null, null, null, null,
				"Vfailure/VsTO", "Distance", "", "m",
				new String[] {"OEI Take-Off", "Aborted Take-Off"},
				takeOffFolderPath, "BalancedTakeOffLength");

		System.out.println("\n---------------------------DONE!-------------------------------");
	}

	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsTakeOff implements FirstOrderDifferentialEquations {

		double weight, altitude, g0, kAlpha, cD0, deltaCD0, oswald, ar, kGround, vWind, alphaDotInitial;
		MyInterpolatingFunction mu, muBrake;
		
		// visible variables
		public double alpha, gamma;

		public DynamicsEquationsTakeOff() {

			// constants and known values
			weight = maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue();
			g0 = AtmosphereCalc.g0.getEstimatedValue();
			mu = TakeOffCalc.this.mu;
			muBrake = TakeOffCalc.this.muBrake;
			kAlpha = TakeOffCalc.this.kAlphaDot;
			ar = aircraft.getWing().getAspectRatio();
			kGround = TakeOffCalc.this.getkGround();
			vWind = TakeOffCalc.this.getvWind().getEstimatedValue();
			altitude = TakeOffCalc.this.getAltitude().getEstimatedValue();
			alphaDotInitial = TakeOffCalc.this.getAlphaDotInitial();
		

		}

		@Override
		public int getDimension() {
			return 4;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			alpha = alpha(t);
			double speed = x[1];
			double altitude = x[3];
			gamma = x[2];
			
			if(!isAborted) {
				if( t < tEndRot.getEstimatedValue()) {
					xDot[0] = speed;
					xDot[1] = (g0/weight)*(thrust(speed, gamma, t, altitude) - drag(speed, alpha, gamma, t)
							- (mu(speed)*(weight - lift(speed, alpha, gamma, t))));
					xDot[2] = 0.0;
					xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
				}
				else {
					xDot[0] = speed;
					xDot[1] = (g0/weight)*(
							thrust(speed, gamma,t, altitude)*Math.cos(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()) 
							- drag(speed, alpha, gamma, t) 
							- weight*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
					xDot[2] = 57.3*(g0/(weight*speed))*(
							lift(speed, alpha, gamma, t) 
							+ (thrust(speed, gamma, t, altitude)*Math.sin(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
							- weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
					xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
				}
			}
			else {
				if( t < tRec.getEstimatedValue()) {
					xDot[0] = speed;
					xDot[1] = (g0/weight)*(thrust(speed, gamma, t, altitude) - drag(speed, alpha, gamma, t)
							- (mu.value(speed)*(weight - lift(speed, alpha, gamma, t))));
					xDot[2] = 0.0;
					xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
				}
				else {
					xDot[0] = speed;
					xDot[1] = (g0/weight)*(thrust(speed, gamma, t, altitude) - drag(speed, alpha, gamma, t)
							- (muBrake.value(speed)*(weight-lift(speed, alpha, gamma, t))));
					xDot[2] = 0.0;
					xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
				}
			}
		}

		public double thrust(double speed, double gamma, double time, double altitude) {

			double theThrust = 0.0;

			if (time < tFaiulre.getEstimatedValue())
				theThrust =	ThrustCalc.calculateThrustDatabase(
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineNumber(),
						TakeOffCalc.this.getPhi(),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
						EngineOperatingConditionEnum.TAKE_OFF,
						TakeOffCalc.this.getAircraft().getPowerPlant(),
						altitude,
						SpeedCalc.calculateMach(
								altitude,
								speed + 
								(TakeOffCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
										gamma,
										NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
								)
						);
			else {
				if(!isAborted) {
				// CHECK ON THE APR SETTING, if present use this ...
				if ((TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP) 
						&& (Double.valueOf(
								TakeOffCalc.this.getAircraft()
								.getPowerPlant()
								.getTurbopropEngineDatabaseReader()
								.getThrustAPR(
										TakeOffCalc.this.getMach(),
										TakeOffCalc.this.getAltitude().doubleValue(SI.METER),
										TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR()
										)
								) != 0.0
							)
						)
				theThrust =	ThrustCalc.calculateThrustDatabase(
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineNumber() - 1,
						TakeOffCalc.this.getPhi(),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
						TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
						EngineOperatingConditionEnum.APR,
						TakeOffCalc.this.getAircraft().getPowerPlant(),
						altitude,
						SpeedCalc.calculateMach(
								altitude,
								speed + 
								(TakeOffCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
										gamma,
										NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
								)
						);
				else
					// ... otherwise use TAKE-OFF
					theThrust =	ThrustCalc.calculateThrustDatabase(
							TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							TakeOffCalc.this.getAircraft().getPowerPlant().getEngineNumber() - 1,
							TakeOffCalc.this.getPhi(),
							TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
							TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
							EngineOperatingConditionEnum.TAKE_OFF,
							TakeOffCalc.this.getAircraft().getPowerPlant(),
							altitude,
							SpeedCalc.calculateMach(
									altitude,
									speed + 
									(TakeOffCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
											gamma,
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
									)
							);
				}
				else {
					if(time < tRec.doubleValue(SI.SECOND))
						theThrust =	ThrustCalc.calculateThrustDatabase(
								TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								TakeOffCalc.this.getAircraft().getPowerPlant().getEngineNumber() - 1,
								TakeOffCalc.this.getPhi(),
								TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
								TakeOffCalc.this.getAircraft().getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.TAKE_OFF,
								TakeOffCalc.this.getAircraft().getPowerPlant(),
								altitude,
								SpeedCalc.calculateMach(
										altitude,
										speed + 
										(TakeOffCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
												gamma,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
										)
								);
					else
						theThrust =	
								TakeOffCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON)
								*throttleGrounIdle(speed)
								*(TakeOffCalc.this.getAircraft().getPowerPlant().getEngineNumber() - 1)
								;
				}
			}
			
			return theThrust;
		}

		public double cD(double cL) {

			double cD = MyMathUtils
					.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(
									aircraft.getTheAnalysisManager().getThePerformance().getPolarCLTakeOff()
									),
							MyArrayUtils.convertToDoublePrimitive(
									aircraft.getTheAnalysisManager().getThePerformance().getPolarCDTakeOff()
									),
							cL
							);
			
			double cD0 = MyArrayUtils.getMin(
					MyArrayUtils.convertToDoublePrimitive(
							aircraft.getTheAnalysisManager().getThePerformance().getPolarCDTakeOff()
							)
					);
			double cDi = (cD-cD0)*kGround;

			double cDnew = cD0 + cDi;
			
			return cDnew;
		}

		public double drag(double speed, double alpha, double gamma, double time) {

			double cD = 0;
			
			if (time < tRec.getEstimatedValue())
				cD = cD(cL(speed, alpha, gamma, time));
			else
				cD = kFailure + cD(cL(speed, alpha, gamma, time));

			return 	0.5
					*aircraft.getWing().getSurface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							altitude)
					*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
							gamma,
							NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
					*cD;
		}

		public double cL(double speed, double alpha, double gamma ,double time) {

			if (time < tClimb.getEstimatedValue()) {
				double cL0 = TakeOffCalc.this.cL0;
				double cLalpha = TakeOffCalc.this.getcLalphaFlap();
				double alphaWing = alpha + TakeOffCalc.this.getIw().getEstimatedValue();

				return cL0 + (cLalpha*alphaWing);
				
			}
			else
				return (2*weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))/
						(TakeOffCalc.this.getAircraft().getWing().getSurface().getEstimatedValue()*
								AtmosphereCalc.getDensity(
										altitude)*
								Math.pow(speed, 2));
		}

		public double lift(double speed, double alpha, double gamma, double time) {

			double cL = cL(speed, alpha, gamma, time);

			return 	0.5
					*aircraft.getWing().getSurface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							altitude)
					*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
							gamma,
							NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
					*cL;
		}

		public double mu(double speed) {
			return mu.value(speed);
		}
		
		public double muBrake(double speed) {
			return muBrake.value(speed);
		}
		
		public double throttleGrounIdle(double speed) {
			return groundIdlePhi.value(speed);
		}
		
		public double alphaDot(double time) {

			double alphaDot = 0.0;
			
			if(isAborted)
				alphaDot = 0.0;
			else {
				if((time > tRot.getEstimatedValue()) && (time < tHold.getEstimatedValue())) {
					alphaDot = alphaDotInitial*(1-(kAlpha*(TakeOffCalc.this.getAlpha().get(
							TakeOffCalc.this.getAlpha().size()-1).getEstimatedValue()))
							);
				}
				else if((time > tEndHold.getEstimatedValue()) && (time < tClimb.getEstimatedValue())) {
					alphaDot = alphaRed;
				}
			}
			return alphaDot;
		}

		public double alpha(double time) {

			double alpha = TakeOffCalc.this.getAlphaGround().getEstimatedValue();

			if(time > tRot.getEstimatedValue())
				alpha = TakeOffCalc.this.getAlpha().get(
						TakeOffCalc.this.getAlpha().size()-1).getEstimatedValue()
				+(alphaDot(time)*(TakeOffCalc.this.getTime().get(
						TakeOffCalc.this.getTime().size()-1).getEstimatedValue()
						- TakeOffCalc.this.getTime().get(
								TakeOffCalc.this.getTime().size()-2).getEstimatedValue()));

			return alpha;
		}
	}
	//-------------------------------------------------------------------------------------
	//									END NESTED CLASS	
	//-------------------------------------------------------------------------------------


	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:

	public Aircraft getAircraft() {
		return aircraft;
	}

	public void setAircraft(Aircraft aircraft) {
		this.aircraft = aircraft;
	}

	public Amount<Duration> getDtRot() {
		return dtRot;
	}

	public void setDtRot(Amount<Duration> dtRot) {
		this.dtRot = dtRot;
	}

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

	public Amount<Length> getWingToGroundDistance() {
		return wingToGroundDistance;
	}

	public void setWingToGroundDistance(Amount<Length> wingToGroundDistance) {
		this.wingToGroundDistance = wingToGroundDistance;
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

	public Amount<Angle> getIw() {
		return iw;
	}

	public void setIw(Amount<Angle> iw) {
		this.iw = iw;
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

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
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

	public Double getvFailure() {
		return vFailure;
	}

	public void setvFailure(Double vFailure) {
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

	public double getMach() {
		return mach;
	}

	public void setMach(double mach) {
		this.mach = mach;
	}

	public List<Double> getSfc() {
		return sfc;
	}

	public void setSfc(List<Double> sfc) {
		this.sfc = sfc;
	}

	public MyInterpolatingFunction getGroundIdlePhi() {
		return groundIdlePhi;
	}

	public void setGroundIdlePhi(MyInterpolatingFunction groundIdlePhi) {
		this.groundIdlePhi = groundIdlePhi;
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
}