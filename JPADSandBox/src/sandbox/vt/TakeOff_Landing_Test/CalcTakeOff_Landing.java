package sandbox.vt.TakeOff_Landing_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.TakeOffResultsMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

/**
 * This class have the purpose of calculating the required take-off/landing field length
 * of a given aircraft by evaluating two main phases:
 *
 * Take-Off:
 * - the ground roll distance (rotation included)
 * - the airborne distance (until the reach of a given obstacle altitude)
 *
 * Landing:
 * - the airborne distance
 * - the flare distance
 * - the ground roll distance
 *
 * for each of them a step by step integration is used in solving the dynamic equation;
 * furthermore the class allows to calculate the balanced field length by evaluating every
 * failure possibility and comparing the required field length in OEI condition with the
 * aborted take-off distance until they are equal.
 *
 * @author Vittorio Trifari
 *
 */

public class CalcTakeOff_Landing {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION

	private Aircraft aircraft;
	private OperatingConditions theConditions;
	private CalcHighLiftDevices highLiftCalculator;
	private Amount<Duration> dt, dtRot, dtHold,	dtRec = Amount.valueOf(1.5, SI.SECOND);
	private Amount<Velocity> vSTakeOff, vRot, vLO, vWind, v1;
	private Amount<Length> wingToGroundDistance, obstacle, balancedFieldLength;
	private Amount<Angle> alphaGround, iw;
	private List<Double> alphaDot, gammaDot, cL, cLAborted, cD, cDAborted, loadFactor, loadFactorAborted;
	private List<Amount<Angle>> alpha, alphaAborted, theta, gamma;
	private List<Amount<Duration>> time, timeAborted;
	private List<Amount<Velocity>> speed, speedAborted, rateOfClimb;
	private List<Amount<Acceleration>> acceleration, accelerationAborted;
	private List<Amount<Force>> thrust, thrustAborted, thrustHorizontal, thrustVertical,
								lift, liftAborted, drag, dragAborted, friction, frictionAborted,
								totalForce, totalForceAborted;
	private List<Amount<Length>> deltaGroundDistance, deltaGroundDistanceAborted, groundDistance,
								 groundDistanceAborted, deltaVerticalDistance, verticalDistance;
	private double kAlphaDot, kcLMax, kRot, kLO, phi, mu, muBrake, cLmaxTO, kGround, alphaDotInitial,
				   alphaRed, cL0, cLground, kFailure;
	private final double k1 = 0.078, k2 = 0.365;

	// Statistics to be collected at every phase: (initialization of the lists through the builder
	private TakeOffResultsMap takeOffResults = new TakeOffResultsMap();
	// Interpolated function for balanced field length calculation
	MyInterpolatingFunction continuedTakeOffFitted = new MyInterpolatingFunction();
	MyInterpolatingFunction abortedTakeOffFitted = new MyInterpolatingFunction();
	// integration index
	private int idxCurrent, idxAborted;
	private double[] failureSpeedArray, continuedTakeOffArray, abortedTakeOffArray,
					 failureSpeedArrayFitted, continuedTakeOffSplineValues,
					 abortedTakeOffSplineValues;

	//-------------------------------------------------------------------------------------
	// BUILDER:

	/**************************************************************************************
	 * This builder has the purpose of pass input to the class fields and to initialize all
	 * lists with the correct initial values in order to setup the take-off length calculation
	 *
	 * @author Vittorio Trifari
	 * @param aircraft
	 * @param theConditions
	 * @param highLiftCalculator instance of LSAerodynamicManager inner class for managing
	 * 		  and slat effects
	 * @param dt temporal step
	 * @param k_alpha_dot decrease factor of alpha_dot
	 * @param mu friction coefficient at take-off
	 * @param mu_brake friction coefficient in landing or aborting
	 * @param wing_to_ground_distance
	 * @param v_wind negative is adverse
	 */
	public CalcTakeOff_Landing(
			Aircraft aircraft,
			OperatingConditions theConditions,
			CalcHighLiftDevices highLiftCalculator,
			Amount<Duration> dt,
			Amount<Duration> dtRot,
			Amount<Duration> dtHold,
			double kcLMax,
			double kRot,
			double kLO,
			double kFailure,
			double phi,
			double k_alpha_dot,
			double alphaRed,
			double mu,
			double mu_brake,
			Amount<Length> wing_to_ground_distance,
			Amount<Length> obstacle,
			Amount<Velocity> v_wind,
			Amount<Angle> alpha_ground,
			Amount<Angle> iw
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.highLiftCalculator = highLiftCalculator;
		this.dt = dt;
		this.dtRot = dtRot;
		this.dtHold = dtHold;
		this.kcLMax = kcLMax;
		this.kRot = kRot;
		this.kLO = kLO;
		this.kFailure = kFailure;
		this.phi = phi;
		this.kAlphaDot = k_alpha_dot;
		this.alphaRed = alphaRed;
		this.mu = mu;
		this.muBrake = mu_brake;
		this.wingToGroundDistance = wing_to_ground_distance;
		this.obstacle = obstacle;
		this.vWind = v_wind;
		this.alphaGround = alpha_ground;
		this.iw = iw;

		// CalcHighLiftDevices object to manage flap/slat effects
		highLiftCalculator.calculateHighLiftDevicesEffects();
		cLmaxTO = highLiftCalculator.getcL_Max_Flap();

		// Reference velocities definition
		vSTakeOff = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						theConditions.get_altitude().getEstimatedValue(),
						aircraft.get_weights().get_MTOW().getEstimatedValue(),
						aircraft.get_wing().get_surface().getEstimatedValue(),
						cLmaxTO
						),
				SI.METERS_PER_SECOND);
		vRot = vSTakeOff.times(kRot);
		vLO = vSTakeOff.times(kLO);

		// Ground effect coefficient (Roskam)
		//		kGround = (1 - (1.32*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())))
		//				/(1.05 + (7.4*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())));

		// McCormick interpolated function --> See the excel file into JPAD DOCS
		double hb = wing_to_ground_distance.divide(aircraft.get_wing().get_span().times(Math.PI/4)).getEstimatedValue();
		kGround = - 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
				+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055;

		cL0 = highLiftCalculator.calcCLatAlphaHighLiftDevice(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		cLground = highLiftCalculator.calcCLatAlphaHighLiftDevice(getAlphaGround().plus(iw));

		// List initialization with known values
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
		this.deltaGroundDistance = new ArrayList<Amount<Length>>();
		this.groundDistance = new ArrayList<Amount<Length>>();
		this.deltaVerticalDistance = new ArrayList<Amount<Length>>();
		this.verticalDistance = new ArrayList<Amount<Length>>();

		// Aborted distance list initialization
		this.timeAborted = new ArrayList<Amount<Duration>>();
		this.speedAborted = new ArrayList<Amount<Velocity>>();
		this.thrustAborted = new ArrayList<Amount<Force>>();
		this.alphaAborted = new ArrayList<Amount<Angle>>();
		this.cLAborted = new ArrayList<Double>();
		this.liftAborted = new ArrayList<Amount<Force>>();
		this.loadFactorAborted = new ArrayList<Double>();
		this.cDAborted = new ArrayList<Double>();
		this.dragAborted = new ArrayList<Amount<Force>>();
		this.frictionAborted = new ArrayList<Amount<Force>>();
		this.totalForceAborted = new ArrayList<Amount<Force>>();
		this.accelerationAborted = new ArrayList<Amount<Acceleration>>();
		this.deltaGroundDistanceAborted = new ArrayList<Amount<Length>>();
		this.groundDistanceAborted = new ArrayList<Amount<Length>>();

		takeOffResults.initialize();

		System.out.println("---------------------------------------------------");
		System.out.println("CalcTakeOff_Landing :: ODE integration\n\n");

		// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

		FirstOrderIntegrator theIntegrator = new DormandPrince853Integrator(
				1.0e-8,   // minStep
				100.0,    // maxStep
				1.0e-10,  // vecAbsoluteTolerance
				1.0e-10   // vecRelativeTolerance
				);

		FirstOrderDifferentialEquations ode = new DynamicsEquations();

		// handle detail infos
		StepHandler stepHandler = new StepHandler() {
			
			boolean doWrite = false;
			
			public void init(double t0, double[] x0, double t) {
				doWrite = true;
			}

			@Override
			public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {
				
				if(!doWrite)
					return;
				
				double   t = interpolator.getCurrentTime();
				double[] x = interpolator.getInterpolatedState();
				
				System.out.println(
						"\n\tt = " + t + " s" +
						"\n\tx[0] = s = " + x[0] + " m" +
						"\n\tx[1] = V = " + x[1] + " m/s" + 
						"\n\tx[2] = gamma = " + x[2] + " °" +
						"\n\tx[3] = quota = " + x[3] + " m"
						);
				
				CalcTakeOff_Landing.this.getTime().add(Amount.valueOf(t, SI.SECOND));
				CalcTakeOff_Landing.this.getSpeed().add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
				CalcTakeOff_Landing.this.getThrust().add(Amount.valueOf(
						((DynamicsEquations)ode).thrust(x[1]),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getThrustHorizontal().add(Amount.valueOf(
						((DynamicsEquations)ode).thrust(x[1])*Math.cos(((DynamicsEquations)ode).alpha),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getThrustVertical().add(Amount.valueOf(
						((DynamicsEquations)ode).thrust(x[1])*Math.sin(((DynamicsEquations)ode).alpha),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getFriction().add(Amount.valueOf(
						CalcTakeOff_Landing.this.getMu()
						*(((DynamicsEquations)ode).weight
								- ((DynamicsEquations)ode).lift(x[1],((DynamicsEquations)ode).alpha)),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getLift().add(Amount.valueOf(
						((DynamicsEquations)ode).lift(x[1],((DynamicsEquations)ode).alpha),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getDrag().add(Amount.valueOf(
						((DynamicsEquations)ode).drag(x[1],((DynamicsEquations)ode).alpha),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getTotalForce().add(Amount.valueOf(
						((DynamicsEquations)ode).thrust(x[1])*Math.cos(((DynamicsEquations)ode).alpha)
						- ((DynamicsEquations)ode).drag(x[1],((DynamicsEquations)ode).alpha)
						- CalcTakeOff_Landing.this.getMu()*(((DynamicsEquations)ode).weight
								- ((DynamicsEquations)ode).lift(x[1],((DynamicsEquations)ode).alpha))
						- ((DynamicsEquations)ode).weight*Math.sin(x[2]),
						SI.NEWTON)
						);
				CalcTakeOff_Landing.this.getLoadFactor().add(
						((DynamicsEquations)ode).lift(x[1], ((DynamicsEquations)ode).alpha)
						/(((DynamicsEquations)ode).weight*Math.cos(x[2]))
						);
				CalcTakeOff_Landing.this.getSpeed().add(Amount.valueOf(
						x[1],
						SI.METERS_PER_SECOND)
						);
				CalcTakeOff_Landing.this.getRateOfClimb().add(Amount.valueOf(
						x[1]*Math.sin(x[2]),
						SI.METERS_PER_SECOND)
						);
				CalcTakeOff_Landing.this.getAcceleration().add(Amount.valueOf(
						(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquations)ode).weight)
						*((DynamicsEquations)ode).thrust(x[1])*Math.cos(((DynamicsEquations)ode).alpha)
						- ((DynamicsEquations)ode).drag(x[1],((DynamicsEquations)ode).alpha)
						- CalcTakeOff_Landing.this.getMu()*(((DynamicsEquations)ode).weight
								- ((DynamicsEquations)ode).lift(x[1],((DynamicsEquations)ode).alpha))
						- ((DynamicsEquations)ode).weight*Math.sin(x[2]),
						SI.METERS_PER_SQUARE_SECOND)
						);
				CalcTakeOff_Landing.this.getGroundDistance().add(Amount.valueOf(
						x[0],
						SI.METER)
						);
				CalcTakeOff_Landing.this.getVerticalDistance().add(Amount.valueOf(
						x[3],
						SI.METER)
						);
				CalcTakeOff_Landing.this.getAlpha().add(Amount.valueOf(
						((DynamicsEquations)ode).alpha,
						NonSI.DEGREE_ANGLE)
						);
				CalcTakeOff_Landing.this.getGamma().add(Amount.valueOf(
						x[2],
						NonSI.DEGREE_ANGLE)
						);
				
				// TODO: ADD ALPHA_DOT AND GAMMA_DOT WHEN AVAILABLE
				CalcTakeOff_Landing.this.getAlphaDot().add(0.0);
				CalcTakeOff_Landing.this.getGammaDot().add(0.0);
				
				CalcTakeOff_Landing.this.getTheta().add(Amount.valueOf(
						x[2] + ((DynamicsEquations)ode).alpha,
						NonSI.DEGREE_ANGLE)
						);
				CalcTakeOff_Landing.this.getcL().add(
						((DynamicsEquations)ode).cL(((DynamicsEquations)ode).alpha)
						);
				CalcTakeOff_Landing.this.getcD().add(
						((DynamicsEquations)ode).cD0 
						+ ((DynamicsEquations)ode).deltaCD0 
						+ ((((DynamicsEquations)ode).cL(((DynamicsEquations)ode).alpha)
								/(Math.PI
										*((DynamicsEquations)ode).ar
										*((DynamicsEquations)ode).oswald))
								*kGround)
						);
				
				doWrite = false;
			}
		};
		theIntegrator.addStepHandler(stepHandler);

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
				return speed - vRot.getEstimatedValue();
			}

			@Override
			public Action eventOccurred(double t, double[] x, boolean increasing) {
				// Handle an event and choose what to do next.
				System.out.println("\n switching function changes sign at t = " + t);
				System.out.println(
						"\n\tx[0] = s = " + x[0] + " m" +
						"\n\tx[1] = V = " + x[1] + " m/s" + 
						"\n\tx[2] = gamma = " + x[2] + " °" +
						"\n\tx[3] = quota = " + x[3] + " m"
						);
				
				// COLLECTING DATA IN TakeOffResultsMap
				System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
				takeOffResults.initialize();
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
				return  Action.STOP;
			}

		};
		theIntegrator.addEventHandler(ehCheckVRot, 1.0e-5, 5e-2, 50);

		double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0}; // initial state
		theIntegrator.integrate(ode, 0.0, xAt0, 40, xAt0); // now xAt0 contains final state at time t=16.0

		System.out.println("\n\n---------------------------------------------------");
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

		// index initialization
		idxCurrent = 1;
		idxAborted = 1;

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
		deltaGroundDistance.clear();
		groundDistance.clear();
		deltaVerticalDistance.clear();
		verticalDistance.clear();

		timeAborted.clear();
		speedAborted.clear();
		thrustAborted.clear();
		alphaAborted.clear();
		cLAborted.clear();
		liftAborted.clear();
		loadFactorAborted.clear();
		cDAborted.clear();
		dragAborted.clear();
		frictionAborted.clear();
		totalForceAborted.clear();
		accelerationAborted.clear();
		deltaGroundDistanceAborted.clear();
		groundDistanceAborted.clear();

		// List initialization with known values
		time.add(Amount.valueOf(0.0, SI.SECOND));
		speed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
			thrust.add(Amount.valueOf(
					ThrustCalc.calculateThrustDatabase(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
							aircraft.get_powerPlant().get_engineType(),
							EngineOperatingConditionEnum.TAKE_OFF,
							theConditions.get_altitude().getEstimatedValue(),
							SpeedCalc.calculateMach(
									theConditions.get_altitude().getEstimatedValue(),
									speed.get(0).getEstimatedValue() + getvWind().getEstimatedValue())
							),
					SI.NEWTON)
					);
			thrustHorizontal.add(thrust.get(0));
			thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
		}
		else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
			thrust.add(Amount.valueOf(
					ThrustCalc.calculateThrust(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							theConditions.get_altitude().getEstimatedValue()),
					SI.NEWTON)
					);
			thrustHorizontal.add(thrust.get(0));
			thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
		}
		alpha.add(getAlphaGround());
		alphaDot.add(0.0);
		gamma.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		gammaDot.add(0.0);
		theta.add(alpha.get(0).plus(gamma.get(0)));
		cL.add(getcLground());
		lift.add(Amount.valueOf(0.0, SI.NEWTON));
		loadFactor.add(0.0);
		//		cD.add(aircraft.get_theAerodynamics().get_cD0()
		//				+ highLiftCalculator.getDeltaCD()
		//				+ aircraft.get_landingGear().get_deltaCD0()
		//				+ ((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
		//				- kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));

		cD.add(aircraft.get_theAerodynamics().get_cD0()
				+ highLiftCalculator.getDeltaCD()
				+ aircraft.get_landingGear().get_deltaCD0()
				+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
				);

		drag.add(Amount.valueOf(0.0, SI.NEWTON));
		friction.add(aircraft.get_weights().get_MTOW().times(mu));
		totalForce.add(thrustHorizontal.get(0).minus(friction.get(0)));
		acceleration.add(Amount.valueOf(
				AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForce.get(0)).getEstimatedValue(),
				SI.METERS_PER_SQUARE_SECOND));
		rateOfClimb.add(speed.get(0).plus(getvWind()).times(Math.sin(gamma.get(0).to(SI.RADIAN).getEstimatedValue())));
		deltaGroundDistance.add(Amount.valueOf(0.0, SI.METER));
		groundDistance.add(Amount.valueOf(0.0, SI.METER));
		deltaVerticalDistance.add(Amount.valueOf(0.0, SI.METER));
		verticalDistance.add(Amount.valueOf(0.0, SI.METER));

		// Aborted distance list initialization
		timeAborted.add(Amount.valueOf(0.0, SI.SECOND));
		speedAborted.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
			thrustAborted.add(Amount.valueOf(
					ThrustCalc.calculateThrustDatabase(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
							aircraft.get_powerPlant().get_engineType(),
							EngineOperatingConditionEnum.TAKE_OFF,
							theConditions.get_altitude().getEstimatedValue(),
							SpeedCalc.calculateMach(
									theConditions.get_altitude().getEstimatedValue(),
									speedAborted.get(0).getEstimatedValue() + getvWind().getEstimatedValue())
							),
					SI.NEWTON)
					);
		}
		else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
			thrustAborted.add(Amount.valueOf(
					ThrustCalc.calculateThrust(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							theConditions.get_altitude().getEstimatedValue()),
					SI.NEWTON)
					);
		}
		alphaAborted.add(getAlphaGround());
		cLAborted.add(getcLground());
		liftAborted.add(Amount.valueOf(0.0, SI.NEWTON));
		loadFactorAborted.add(0.0);
		cDAborted.add(cD.get(0));
		dragAborted.add(Amount.valueOf(0.0, SI.NEWTON));
		frictionAborted.add(aircraft.get_weights().get_MTOW().times(mu));
		totalForceAborted.add(thrustAborted.get(0).minus(frictionAborted.get(0)));
		accelerationAborted.add(Amount.valueOf(
				AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForceAborted.get(0)).getEstimatedValue(),
				SI.METERS_PER_SQUARE_SECOND));
		deltaGroundDistanceAborted.add(Amount.valueOf(0.0, SI.METER));
		groundDistanceAborted.add(Amount.valueOf(0.0, SI.METER));
	}

	/**************************************************************************************
	 * This method performs a step by step integration of horizontal equation of motion in
	 * order to calculate the ground roll and rotation distance of a given airplane take-off phase.
	 * Moreover it calculates all accessories quantity in terms of forces and velocities.
	 * If a failure speed is specified (0<Vfailure<VLO) it performs the ground distance
	 * calculation in OEI condition.
	 *
	 * @author Vittorio Trifari
	 */
	private void calculateGroundDistance(Double vFailure) {

		// failure check
		if(vFailure == null)
			vFailure = 10000.0; // speed impossible to reach --> no failure!!

		// temporal step setup (needed for iterative calls of this method)
		dt = Amount.valueOf(0.5, SI.SECOND);

		// Computation starts from the result of first step initialized in the builder
		System.out.println("\n---------------------GROUND ROLL PHASE-------------------------");

		while(speed.get(idxCurrent-1).isLessThan(vRot)) {

			System.out.println(" idx : " + idxCurrent);

			// update of all variables
			time.add(time.get(idxCurrent-1).plus(dt));
			// increment at actual speed
			speed.add(Amount.valueOf(
					speed.get(idxCurrent-1).getEstimatedValue() + (acceleration.get(idxCurrent-1).getEstimatedValue()*dt.getEstimatedValue()),
					SI.METERS_PER_SECOND));

			// speed check
			// AOE condition
			if (speed.get(idxCurrent).getEstimatedValue() < vFailure) {

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speed.get(idxCurrent).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent));
					thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent));
					thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
				}

				alpha.add(alpha.get(idxCurrent-1));
				alphaDot.add(alphaDot.get(idxCurrent-1));
				gammaDot.add(gammaDot.get(idxCurrent-1));
				gamma.add(gamma.get(idxCurrent-1));
				theta.add(alpha.get(idxCurrent).plus(gamma.get(idxCurrent)));

				cL.add(cL.get(idxCurrent-1));
				lift.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cL.get(idxCurrent),
						SI.NEWTON)
						);

				loadFactor.add(lift.get(idxCurrent).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());

				//				cD.add(aircraft.get_theAerodynamics().get_cD0()
				//						+ highLiftCalculator.getDeltaCD()
				//						+ aircraft.get_landingGear().get_deltaCD0()
				//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
				//						- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));

				cD.add(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
						);

				drag.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cD.get(idxCurrent),
						SI.NEWTON)
						);

				friction.add((aircraft.get_weights().get_MTOW().minus(lift.get(idxCurrent))).times(mu));
				totalForce.add(thrustHorizontal.get(idxCurrent).minus(friction.get(idxCurrent)).minus(drag.get(idxCurrent)));
				acceleration.add(Amount.valueOf(
						AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForce.get(idxCurrent)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				rateOfClimb.add(speed.get(idxCurrent).plus(vWind).times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				deltaGroundDistance.add(
						Amount.valueOf(
								speed.get(idxCurrent).getEstimatedValue()*dt.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistance.add(groundDistance.get(idxCurrent-1).plus(deltaGroundDistance.get(idxCurrent)));
				deltaVerticalDistance.add(Amount.valueOf(rateOfClimb.get(idxCurrent).times(dt).getEstimatedValue(), SI.METER));
				verticalDistance.add(verticalDistance.get(idxCurrent-1).plus(deltaVerticalDistance.get(idxCurrent)));
			}

			// OEI condition
			else {

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speed.get(idxCurrent).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent));
					thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent));
					thrustVertical.add(Amount.valueOf(0.0, SI.NEWTON));
				}

				alpha.add(alpha.get(idxCurrent-1));
				alphaDot.add(alphaDot.get(idxCurrent-1));
				gammaDot.add(gammaDot.get(idxCurrent-1));
				gamma.add(gamma.get(idxCurrent-1));
				theta.add(alpha.get(idxCurrent).plus(gamma.get(idxCurrent)));

				cL.add(cL.get(idxCurrent-1));
				lift.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cL.get(idxCurrent),
						SI.NEWTON)
						);

				loadFactor.add(lift.get(idxCurrent).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());

				//				cD.add(1.1*(aircraft.get_theAerodynamics().get_cD0()
				//						+ highLiftCalculator.getDeltaCD()
				//						+ aircraft.get_landingGear().get_deltaCD0()
				//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
				//						- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))));

				cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
						));

				drag.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cD.get(idxCurrent),
						SI.NEWTON)
						);

				friction.add((aircraft.get_weights().get_MTOW().minus(lift.get(idxCurrent))).times(mu));
				totalForce.add(thrustHorizontal.get(idxCurrent).minus(friction.get(idxCurrent)).minus(drag.get(idxCurrent)));
				acceleration.add(Amount.valueOf(
						AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForce.get(idxCurrent)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				rateOfClimb.add(speed.get(idxCurrent).plus(vWind).times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				deltaGroundDistance.add(
						Amount.valueOf(
								speed.get(idxCurrent).getEstimatedValue()*dt.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistance.add(groundDistance.get(idxCurrent-1).plus(deltaGroundDistance.get(idxCurrent)));
				deltaVerticalDistance.add(Amount.valueOf(rateOfClimb.get(idxCurrent).times(dt).getEstimatedValue(), SI.METER));
				verticalDistance.add(verticalDistance.get(idxCurrent-1).plus(deltaVerticalDistance.get(idxCurrent)));
			}

			// step increment
			idxCurrent += 1;
		}
		System.out.println("\n-------------------END GROUND ROLL PHASE-----------------------");

		// Collecting data at the end of ground roll phase
		System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
		takeOffResults.initialize();
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

		// dtRotation
		Amount<Duration> dtRotation = dt.times(0.5);

		// alpha_dot_initial calculation
		double cLatLiftOff = cLmaxTO/(Math.pow(kLO, 2));
		double alphaLiftOff = (cLatLiftOff - cL0)/highLiftCalculator.getcLalpha_new();
		alphaDotInitial = (((alphaLiftOff - iw.getEstimatedValue()) - alpha.get(0).getEstimatedValue())/(dtRot.getEstimatedValue()));

		System.out.println("\n\n-----------------------ROTATION PHASE--------------------------");

		while(loadFactor.get(idxCurrent-1) < 1.0) {

			System.out.println(" idx : " + idxCurrent);

			// temporal steps in rotation are smaller in order to track this short phase in a better way
			time.add(time.get(idxCurrent-1).plus(dtRotation));
			speed.add(Amount.valueOf(
					speed.get(idxCurrent-1).getEstimatedValue() + (acceleration.get(idxCurrent-1).getEstimatedValue()*dtRotation.getEstimatedValue()),
					SI.METERS_PER_SECOND));

			// speed check
			// AOE condition
			if (speed.get(idxCurrent).getEstimatedValue() < vFailure) {

				alphaDot.add(alphaDotInitial*(1-(kAlphaDot*(alpha.get(idxCurrent-1).getEstimatedValue() + iw.getEstimatedValue()))));

				if (alpha.get(idxCurrent-1).isLessThan(Amount.valueOf(alphaLiftOff, NonSI.DEGREE_ANGLE).minus(iw)))
					alpha.add(Amount.valueOf(
							alpha.get(idxCurrent-1).getEstimatedValue() + (alphaDot.get(idxCurrent)*dtRotation.getEstimatedValue()),
							NonSI.DEGREE_ANGLE)
							);
				else
					alpha.add(Amount.valueOf(alphaLiftOff, NonSI.DEGREE_ANGLE).minus(iw));

				gamma.add(gamma.get(idxCurrent-1));
				gammaDot.add(gammaDot.get(idxCurrent-1));
				theta.add(alpha.get(idxCurrent).plus(gamma.get(idxCurrent)));

				if(cL.get(idxCurrent-1) < cLatLiftOff)
					cL.add(cL0 + ((highLiftCalculator.getcLalpha_new())*(alpha.get(idxCurrent).getEstimatedValue() + iw.getEstimatedValue())));
				else
					cL.add(cLatLiftOff);
				lift.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cL.get(idxCurrent),
						SI.NEWTON)
						);

				// CD cases
				if(cL.get(idxCurrent) <= 1.2)
					//					cD.add(aircraft.get_theAerodynamics().get_cD0()
					//							+ highLiftCalculator.getDeltaCD()
					//							+ aircraft.get_landingGear().get_deltaCD0()
					//							+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
					//							- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));

					cD.add(aircraft.get_theAerodynamics().get_cD0()
							+ highLiftCalculator.getDeltaCD()
							+ aircraft.get_landingGear().get_deltaCD0()
							+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
							);
				else
					//					cD.add(aircraft.get_theAerodynamics().get_cD0()
					//							+ highLiftCalculator.getDeltaCD()
					//							+ aircraft.get_landingGear().get_deltaCD0()
					//							+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
					//							- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))
					//							+ k1*(cL.get(i)-1.2)
					//							+ k2*(Math.pow((cL.get(i)-1.2), 2))
					//							);

					cD.add(aircraft.get_theAerodynamics().get_cD0()
							+ highLiftCalculator.getDeltaCD()
							+ aircraft.get_landingGear().get_deltaCD0()
							+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
							+ k1*(cL.get(idxCurrent)-1.2)
							+ k2*(Math.pow((cL.get(idxCurrent)-1.2), 2))
							);

				drag.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cD.get(idxCurrent),
						SI.NEWTON)
						);

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speed.get(idxCurrent).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
					thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
					thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				}

				friction.add(
						(aircraft.get_weights().get_MTOW()
								.minus(lift.get(idxCurrent).plus(thrustVertical.get(idxCurrent))))
						.times(mu));
				if (friction.get(idxCurrent).isLessThan(Amount.valueOf(0.0, SI.NEWTON))) {
					friction.remove(idxCurrent);
					friction.add(Amount.valueOf(0.0, SI.NEWTON));
				}

				totalForce.add(thrustHorizontal.get(idxCurrent)
						.minus(friction.get(idxCurrent))
						.minus(drag.get(idxCurrent)));

				loadFactor.add(lift.get(idxCurrent).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());
				acceleration.add(Amount.valueOf(
						(AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW())).times(totalForce.get(idxCurrent)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				rateOfClimb.add(speed.get(idxCurrent).plus(vWind).times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				deltaGroundDistance.add(
						Amount.valueOf(
								speed.get(idxCurrent).getEstimatedValue()*dtRotation.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistance.add(groundDistance.get(idxCurrent-1).plus(deltaGroundDistance.get(idxCurrent)));
				deltaVerticalDistance.add(Amount.valueOf(rateOfClimb.get(idxCurrent).times(dtRotation).getEstimatedValue(), SI.METER));
				verticalDistance.add(verticalDistance.get(idxCurrent-1).plus(deltaVerticalDistance.get(idxCurrent)));
			}

			// OEI condition
			else {
				alphaDot.add(alphaDotInitial*(1-(kAlphaDot*(alpha.get(idxCurrent-1).getEstimatedValue() + iw.getEstimatedValue()))));

				if (alpha.get(idxCurrent-1).isLessThan(Amount.valueOf(alphaLiftOff, NonSI.DEGREE_ANGLE).minus(iw)))
					alpha.add(Amount.valueOf(
							alpha.get(idxCurrent-1).getEstimatedValue() + (alphaDot.get(idxCurrent)*dtRotation.getEstimatedValue()),
							NonSI.DEGREE_ANGLE)
							);
				else
					alpha.add(Amount.valueOf(alphaLiftOff, NonSI.DEGREE_ANGLE).minus(iw));

				gamma.add(gamma.get(idxCurrent-1));
				gammaDot.add(gammaDot.get(idxCurrent-1));
				theta.add(alpha.get(idxCurrent).plus(gamma.get(idxCurrent)));

				if(cL.get(idxCurrent-1) < cLatLiftOff)
					cL.add(cL0 + ((highLiftCalculator.getcLalpha_new())*(alpha.get(idxCurrent).getEstimatedValue() + iw.getEstimatedValue())));
				else
					cL.add(cLatLiftOff);

				lift.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cL.get(idxCurrent),
						SI.NEWTON)
						);

				// CD cases
				if(cL.get(idxCurrent) <= 1.2)
					//					cD.add(aircraft.get_theAerodynamics().get_cD0()
					//							+ highLiftCalculator.getDeltaCD()
					//							+ aircraft.get_landingGear().get_deltaCD0()
					//							+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
					//							- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));

					cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
							+ highLiftCalculator.getDeltaCD()
							+ aircraft.get_landingGear().get_deltaCD0()
							+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
							));
				else
					//					cD.add(aircraft.get_theAerodynamics().get_cD0()
					//							+ highLiftCalculator.getDeltaCD()
					//							+ aircraft.get_landingGear().get_deltaCD0()
					//							+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
					//							- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))
					//							+ k1*(cL.get(i)-1.2)
					//							+ k2*(Math.pow((cL.get(i)-1.2), 2))
					//							);

					cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
							+ highLiftCalculator.getDeltaCD()
							+ aircraft.get_landingGear().get_deltaCD0()
							+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
							+ k1*(cL.get(idxCurrent)-1.2)
							+ k2*(Math.pow((cL.get(idxCurrent)-1.2), 2))
							));

				drag.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cD.get(idxCurrent),
						SI.NEWTON)
						);

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speed.get(idxCurrent).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
					thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrust.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
					thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
					thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				}

				friction.add(
						(aircraft.get_weights().get_MTOW()
								.minus(lift.get(idxCurrent).plus(thrustVertical.get(idxCurrent))))
						.times(mu));
				if (friction.get(idxCurrent).isLessThan(Amount.valueOf(0.0, SI.NEWTON))) {
					friction.remove(idxCurrent);
					friction.add(Amount.valueOf(0.0, SI.NEWTON));
				}

				totalForce.add(thrustHorizontal.get(idxCurrent)
						.minus(friction.get(idxCurrent))
						.minus(drag.get(idxCurrent)));

				loadFactor.add(lift.get(idxCurrent).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());
				acceleration.add(Amount.valueOf(
						(AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW())).times(totalForce.get(idxCurrent)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				rateOfClimb.add(speed.get(idxCurrent).plus(vWind).times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				deltaGroundDistance.add(
						Amount.valueOf(
								speed.get(idxCurrent).getEstimatedValue()*dtRotation.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistance.add(groundDistance.get(idxCurrent-1).plus(deltaGroundDistance.get(idxCurrent)));
				deltaVerticalDistance.add(Amount.valueOf(rateOfClimb.get(idxCurrent).times(dtRotation).getEstimatedValue(), SI.METER));
				verticalDistance.add(verticalDistance.get(idxCurrent-1).plus(deltaVerticalDistance.get(idxCurrent)));
			}
			// step increment
			idxCurrent += 1;
		}
		System.out.println("\n--------------------END ROTATION PHASE-------------------------");

		// Collecting data at the end of ground roll phase
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
	}

	/**************************************************************************************
	 * This method performs a step by step integration of horizontal and vertical equation
	 * of motion in order to calculate the airborne distance of a given airplane take-off phase.
	 * Moreover it calculates all accessories quantity in terms of forces and velocities.
	 * If the boolean failure flag is true it calculates the airborne distance in OEI condition.
	 *
	 * @author Vittorio Trifari
	 */
	private void calculateAirborneDistance(boolean failure) {

		int nEngine;
		// failure check
		if (failure == false) {
			nEngine = aircraft.get_powerPlant().get_engineNumber();
			kFailure = 1.0;
		}
		else
			nEngine = aircraft.get_powerPlant().get_engineNumber()-1;
		// dtAirborne
		Amount<Duration> dtAirborne = dt.times(0.2);
		// definition of time steps necessary to cover dtHold
		double n = Math.ceil(dtHold.divide(dtAirborne).getEstimatedValue());
		// tHold initialization
		double tHold = 0.0;
		// dtFit for precise calculation of tHold
		Amount<Duration> dtFit = Amount.valueOf(0.005, SI.SECOND);

		System.out.println("\n\n-----------------------AIRBORNE PHASE--------------------------");

		while (verticalDistance.get(idxCurrent-1).isLessThan(obstacle)) {

			System.out.println(" idx : " + idxCurrent);

			// time step selection
			double checkValue = (((kcLMax*cLmaxTO) - (cL.get(idxCurrent-1)))/(kcLMax*cLmaxTO))*100;
			if ((checkValue <= 1) // 1% = threshold
					&& (((kcLMax*cLmaxTO) - (cL.get(idxCurrent-1))) > 0.0)
					&& ((cL.get(idxCurrent-1) - cL.get(idxCurrent-2))>0))
				dt = dtFit;
			else
				dt = dtAirborne;

			// new ground effects due to increasing altitude
			kGround = (1 - (1.32*((wingToGroundDistance.plus(verticalDistance.get(idxCurrent-1)).getEstimatedValue())
					/aircraft.get_wing().get_span().getEstimatedValue())))
					/(1.05 + (7.4*((wingToGroundDistance.plus(verticalDistance.get(idxCurrent-1)).getEstimatedValue())
							/aircraft.get_wing().get_span().getEstimatedValue())));

			time.add(time.get(idxCurrent-1).plus(dt));
			speed.add(Amount.valueOf(
					speed.get(idxCurrent-1).getEstimatedValue() + (acceleration.get(idxCurrent-1).getEstimatedValue()*dt.getEstimatedValue()),
					SI.METERS_PER_SECOND));

			// check on CL to obtain the time at which CL=0.9*CLmaxTO (a minor time step is required
			// for more precision)
			if ((checkValue < 0.05) && (checkValue > 0))
				tHold  = time.get(idxCurrent-1).getMaximumValue();

			// alpha_dot cases
			if ((cL.get(idxCurrent-1) < kcLMax*cLmaxTO)
					&& ((cL.get(idxCurrent-1) - cL.get(idxCurrent-2))>0)
					&& (
							(tHold == 0)
							|| (time.get(idxCurrent-1).getEstimatedValue() <= tHold)
							)
					)
				alphaDot.add(alphaDotInitial*(1-(kAlphaDot*(alpha.get(idxCurrent-1).getEstimatedValue() + iw.getEstimatedValue()))));
			else if ((cL.get(idxCurrent-1) >= kcLMax*cLmaxTO)
					&& (time.get(idxCurrent-1).getEstimatedValue() <= (tHold + (n*dt.getEstimatedValue()))))
				alphaDot.add(0.0);
			else if ((time.get(idxCurrent-1).getEstimatedValue() > (tHold + (n*dt.getEstimatedValue())))
					&& (lift.get(idxCurrent-1).isGreaterThan(
							aircraft.get_weights().get_MTOW()
							.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue())))))
				alphaDot.add(alphaRed);
			else if (lift.get(idxCurrent-1).getEstimatedValue() <= (aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue())).getEstimatedValue()))
				alphaDot.add(((alpha.get(idxCurrent-1).minus(alpha.get(idxCurrent-2))).divide(dt.getEstimatedValue())).getEstimatedValue());

			// alpha case
			if (lift.get(idxCurrent-1).isGreaterThan(
					aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue()))))
				alpha.add(Amount.valueOf(
						alpha.get(idxCurrent-1).getEstimatedValue() + (alphaDot.get(idxCurrent)*dt.getEstimatedValue()),
						NonSI.DEGREE_ANGLE)
						);
			else if (lift.get(idxCurrent-1).getEstimatedValue() <= (aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue())).getEstimatedValue()))
				alpha.add(alpha.get(idxCurrent-1));

			if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
				thrust.add(Amount.valueOf(
						ThrustCalc.calculateThrustDatabase(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								nEngine,
								phi,
								aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
								aircraft.get_powerPlant().get_engineType(),
								EngineOperatingConditionEnum.TAKE_OFF,
								theConditions.get_altitude().getEstimatedValue(),
								SpeedCalc.calculateMach(
										theConditions.get_altitude().getEstimatedValue(),
										speed.get(idxCurrent).getEstimatedValue() + vWind.getEstimatedValue())
								),
						SI.NEWTON)
						);
				thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
			}
			else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
				thrust.add(Amount.valueOf(
						ThrustCalc.calculateThrust(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								nEngine,
								phi,
								theConditions.get_altitude().getEstimatedValue()),
						SI.NEWTON)
						);
				thrustHorizontal.add(thrust.get(idxCurrent).times(Math.cos(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
				thrustVertical.add(thrust.get(idxCurrent).times(Math.sin(alpha.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
			}

			gammaDot.add((AtmosphereCalc.g0.getEstimatedValue()/(speed.get(idxCurrent-1).plus(vWind).getEstimatedValue()))
					*((lift.get(idxCurrent-1)
							.plus(thrustVertical.get(idxCurrent-1))
							.minus(aircraft.get_weights().get_MTOW().times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue()))).getEstimatedValue())
							/aircraft.get_weights().get_MTOW().getEstimatedValue())
					*57.3);
			gamma.add(Amount.valueOf(
					gamma.get(idxCurrent-1).getEstimatedValue() + (gammaDot.get(idxCurrent)*dt.getEstimatedValue()),
					NonSI.DEGREE_ANGLE)
					);
			theta.add(alpha.get(idxCurrent).plus(gamma.get(idxCurrent)));

			// CL cases
			if (lift.get(idxCurrent-1).isGreaterThan(
					aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue()))))
				cL.add(cL0 + ((highLiftCalculator.getcLalpha_new())*(alpha.get(idxCurrent).getEstimatedValue() + iw.getEstimatedValue())));
			else if (lift.get(idxCurrent-1).getEstimatedValue() <= (aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue())).getEstimatedValue()))
				cL.add((aircraft.get_weights().get_MTOW().times(2).times(Math.cos(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())).getEstimatedValue())
						/(aircraft.get_wing().get_surface()
								.times(theConditions.get_densityCurrent().getEstimatedValue())
								.times(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))).getEstimatedValue());

			// lift cases
			if (lift.get(idxCurrent-1).isGreaterThan(
					aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue()))))
				lift.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
						*cL.get(idxCurrent),
						SI.NEWTON)
						);
			else if (lift.get(idxCurrent-1).getEstimatedValue() <= (aircraft.get_weights().get_MTOW()
					.times(Math.cos(gamma.get(idxCurrent-1).to(SI.RADIAN).getEstimatedValue())).getEstimatedValue()))
				lift.add(aircraft.get_weights().get_MTOW().times(Math.cos(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));

			// CD cases
			if(cL.get(idxCurrent) <= 1.2)
				//				cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
				//						+ highLiftCalculator.getDeltaCD()
				//						+ aircraft.get_landingGear().get_deltaCD0()
				//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
				//						- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))));

				cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
						));
			else
				//				cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
				//						+ highLiftCalculator.getDeltaCD()
				//						+ aircraft.get_landingGear().get_deltaCD0()
				//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
				//						- kGround*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))
				//						+ k1*(cL.get(i)-1.2)
				//						+ k2*(Math.pow((cL.get(i)-1.2), 2))
				//						));

				cD.add(kFailure*(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))
						+ k1*(cL.get(idxCurrent)-1.2)
						+ k2*(Math.pow((cL.get(idxCurrent)-1.2), 2))
						));

			drag.add(Amount.valueOf(
					0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed.get(idxCurrent).getEstimatedValue(), 2))
					*cD.get(idxCurrent),
					SI.NEWTON)
					);

			friction.add(friction.get(idxCurrent-1));

			totalForce.add(
					thrustHorizontal.get(idxCurrent)
					.minus(drag.get(idxCurrent))
					.minus(aircraft.get_weights().get_MTOW()
							.times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue()))
							)
					);
			loadFactor.add(lift.get(idxCurrent)
					.divide(aircraft.get_weights().get_MTOW()
							.times(Math.cos(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue()))
							)
					.getEstimatedValue()
					);
			if(loadFactor.get(idxCurrent) < 1.0) {
				loadFactor.remove(idxCurrent);
				loadFactor.add(1.0);
			}

			acceleration.add(Amount.valueOf(
					AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForce.get(idxCurrent)).getEstimatedValue(),
					SI.METERS_PER_SQUARE_SECOND));
			rateOfClimb.add(speed.get(idxCurrent).plus(vWind).times(Math.sin(gamma.get(idxCurrent).to(SI.RADIAN).getEstimatedValue())));
			deltaGroundDistance.add(
					Amount.valueOf(
							speed.get(idxCurrent).getEstimatedValue()*dt.getEstimatedValue(),
							SI.METER
							)
					);
			groundDistance.add(groundDistance.get(idxCurrent-1).plus(deltaGroundDistance.get(idxCurrent)));
			deltaVerticalDistance.add(Amount.valueOf(rateOfClimb.get(idxCurrent).times(dt).getEstimatedValue(), SI.METER));
			verticalDistance.add(verticalDistance.get(idxCurrent-1).plus(deltaVerticalDistance.get(idxCurrent)));

			// altitude update
			theConditions.set_altitude(verticalDistance.get(idxCurrent));

			// step increment
			idxCurrent += 1;
		}

		System.out.println("\n--------------------END AIRBORNE PHASE-------------------------");

		// Collecting data at the end of ground roll phase
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
		System.out.println("\n---------------------------DONE!-------------------------------");
	}


	/*************************************************************************************
	 * This method evaluates the aborted take-off distance in case of engine failure by
	 * integrating the horizontal equation of motion until speed is zero.
	 *
	 * @author Vittorio Trifari
	 */
	private void calculateAbortedDistance(Double vFailure) {

		// Computation starts from the result of first step initialized in the builder
		System.out.println("\n------------------ABORTED TAKE OFF DISTANCE----------------------");

		// definition of time steps necessary to cover dtHold
		double n = Math.ceil(dtRec.divide(dt).getEstimatedValue());
		// index which indicates the failure time
		int index = 0;

		// step by step integration
		while(speedAborted.get(idxAborted-1).getEstimatedValue() >= 0.0) {

			// update of all variables
			timeAborted.add(timeAborted.get(idxAborted-1).plus(dt));
			// increment at actual speed
			speedAborted.add(Amount.valueOf(
					speedAborted.get(idxAborted-1).getEstimatedValue() + (accelerationAborted.get(idxAborted-1).getEstimatedValue()*dt.getEstimatedValue()),
					SI.METERS_PER_SECOND));

			// speed check
			// AOE condition
			if ((speedAborted.get(idxAborted).getEstimatedValue() < vFailure)
					&& (speedAborted.get(idxAborted).isGreaterThan(speedAborted.get(idxAborted-1)))){

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrustAborted.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speedAborted.get(idxAborted).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrustAborted.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber(),
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
				}

				alphaAborted.add(alphaAborted.get(idxAborted-1));
				cLAborted.add(cLAborted.get(idxAborted-1));
				liftAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cLAborted.get(idxAborted),
						SI.NEWTON)
						);

				loadFactorAborted.add(liftAborted.get(idxAborted).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());

				cDAborted.add(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))));
				dragAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cDAborted.get(idxAborted),
						SI.NEWTON)
						);

				frictionAborted.add((aircraft.get_weights().get_MTOW().minus(liftAborted.get(idxAborted))).times(mu));
				totalForceAborted.add(thrustAborted.get(idxAborted).minus(frictionAborted.get(idxAborted)).minus(dragAborted.get(idxAborted)));
				accelerationAborted.add(Amount.valueOf(
						AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForceAborted.get(idxAborted)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				deltaGroundDistanceAborted.add(
						Amount.valueOf(
								speedAborted.get(idxAborted).getEstimatedValue()*dt.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistanceAborted.add(groundDistanceAborted.get(idxAborted-1).plus(deltaGroundDistanceAborted.get(idxAborted)));

				// index is needed to acquire the time of failure which is the last time of AOE condition
				index += 1;
			}

			// OEI condition without brake (pilot action)
			else if((speedAborted.get(idxAborted).getEstimatedValue() >= vFailure)
					&& (timeAborted.get(idxAborted).getEstimatedValue()
							<= (timeAborted.get(index + 1).getEstimatedValue() + (n*dt.getEstimatedValue())))) {

				if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
					thrustAborted.add(Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
									aircraft.get_powerPlant().get_engineType(),
									EngineOperatingConditionEnum.TAKE_OFF,
									theConditions.get_altitude().getEstimatedValue(),
									SpeedCalc.calculateMach(
											theConditions.get_altitude().getEstimatedValue(),
											speedAborted.get(idxAborted).getEstimatedValue() + vWind.getEstimatedValue())
									),
							SI.NEWTON)
							);
				}
				else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
					thrustAborted.add(Amount.valueOf(
							ThrustCalc.calculateThrust(
									aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
									aircraft.get_powerPlant().get_engineNumber() - 1,
									phi,
									theConditions.get_altitude().getEstimatedValue()),
							SI.NEWTON)
							);
				}

				alphaAborted.add(alphaAborted.get(idxAborted-1));
				cLAborted.add(cLAborted.get(idxAborted-1));
				liftAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cLAborted.get(idxAborted),
						SI.NEWTON)
						);

				loadFactorAborted.add(liftAborted.get(idxAborted).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());

				cDAborted.add(1.1*(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))));
				dragAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cDAborted.get(idxAborted),
						SI.NEWTON)
						);

				frictionAborted.add((aircraft.get_weights().get_MTOW().minus(liftAborted.get(idxAborted))).times(mu));
				totalForceAborted.add(thrustAborted.get(idxAborted).minus(frictionAborted.get(idxAborted)).minus(dragAborted.get(idxAborted)));
				accelerationAborted.add(Amount.valueOf(
						AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForceAborted.get(idxAborted)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				deltaGroundDistanceAborted.add(
						Amount.valueOf(
								speedAborted.get(idxAborted).getEstimatedValue()*dt.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistanceAborted.add(groundDistanceAborted.get(idxAborted-1).plus(deltaGroundDistanceAborted.get(idxAborted)));
			}

			// OEI condition with brakes action
			else if(timeAborted.get(idxAborted).getEstimatedValue()
					> (timeAborted.get(index).getEstimatedValue() + (n*dt.getEstimatedValue()))) {

				thrustAborted.add(Amount.valueOf(0.0, SI.NEWTON));

				alphaAborted.add(alphaAborted.get(idxAborted-1));
				cLAborted.add(cLAborted.get(idxAborted-1));
				liftAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cLAborted.get(idxAborted),
						SI.NEWTON)
						);

				loadFactorAborted.add(liftAborted.get(idxAborted).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());

				cDAborted.add(1.1*(aircraft.get_theAerodynamics().get_cD0()
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0()
						+ (kGround*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald())))));
				dragAborted.add(Amount.valueOf(
						0.5
						*aircraft.get_wing().get_surface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								theConditions.get_altitude().getEstimatedValue())
						*(Math.pow(speedAborted.get(idxAborted).getEstimatedValue(), 2))
						*cDAborted.get(idxAborted),
						SI.NEWTON)
						);

				frictionAborted.add((aircraft.get_weights().get_MTOW().minus(liftAborted.get(idxAborted))).times(muBrake));
				totalForceAborted.add(thrustAborted.get(idxAborted).minus(frictionAborted.get(idxAborted)).minus(dragAborted.get(idxAborted)));
				accelerationAborted.add(Amount.valueOf(
						AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(totalForceAborted.get(idxAborted)).getEstimatedValue(),
						SI.METERS_PER_SQUARE_SECOND));
				deltaGroundDistanceAborted.add(
						Amount.valueOf(
								speedAborted.get(idxAborted).getEstimatedValue()*dt.getEstimatedValue(),
								SI.METER
								)
						);
				groundDistanceAborted.add(groundDistanceAborted.get(idxAborted-1).plus(deltaGroundDistanceAborted.get(idxAborted)));
			}
			// step increment
			idxAborted += 1;
		}
		System.out.println("\n----------------END ABORTED TAKE OFF DISTANCE--------------------");
	}

	/**************************************************************************************
	 * This method performs the total take-off distance calculation of a given airplane.
	 * This is the public method the user have to use in a test and it calls the two
	 * previous methods.
	 *
	 * @author Vittorio Trifari
	 */
	public void calculateTakeOffDistance(Double vFailure, boolean failure) {
		calculateGroundDistance(vFailure);
		calculateAirborneDistance(failure);
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

		// failure speed array
		failureSpeedArray = MyArrayUtils.linspace(
				0.0,
				vLO.getEstimatedValue(),
				4);
		// continued take-off array
		continuedTakeOffArray = new double[failureSpeedArray.length];
		// aborted take-off array
		abortedTakeOffArray = new double[failureSpeedArray.length];

		// iterative take-off distance calculation for both conditions
		for(int i=0; i<failureSpeedArray.length; i++) {
			initialize();
			calculateTakeOffDistance(failureSpeedArray[i], true);
			continuedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
			calculateAbortedDistance(failureSpeedArray[i]);
			abortedTakeOffArray[i] = getGroundDistanceAborted().get(groundDistanceAborted.size()-1).getEstimatedValue();
		}

		// interpolation of the two arrays
		failureSpeedArrayFitted = MyArrayUtils.linspace(
				0.0,
				vLO.getEstimatedValue(),
				250);
		continuedTakeOffFitted.interpolate(failureSpeedArray, continuedTakeOffArray);
		abortedTakeOffFitted.interpolate(failureSpeedArray, abortedTakeOffArray);

		// values extraction from the polynomial spline functions
		continuedTakeOffSplineValues = new double[failureSpeedArrayFitted.length];
		abortedTakeOffSplineValues = new double[failureSpeedArrayFitted.length];

		for(int i=0; i<failureSpeedArrayFitted.length; i++){
			continuedTakeOffSplineValues[i] = continuedTakeOffFitted.value(failureSpeedArrayFitted[i]);
			abortedTakeOffSplineValues[i] = abortedTakeOffFitted.value(failureSpeedArrayFitted[i]);
		}

		// arrays intersection
		double[] intersection = MyArrayUtils.intersectArraysSimple(
				continuedTakeOffSplineValues,
				abortedTakeOffSplineValues);
		for(int i=0; i<intersection.length; i++)
			if(intersection[i] != 0.0) {
				balancedFieldLength = Amount.valueOf(intersection[i], SI.METER);
				v1 = Amount.valueOf(failureSpeedArrayFitted[i], SI.METERS_PER_SECOND);
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
	public void createTakeOffCharts() throws InstantiationException, IllegalAccessException {

		System.out.println("\n----------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE------------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Take-Off_Performance" + File.separator);

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
			weightVertical[i] = aircraft.get_weights().get_MTOW().getEstimatedValue()
			*Math.cos(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

		double[] weightHorizontal = new double[getTime().size()];
		for(int i=0; i<weightHorizontal.length; i++)
			weightHorizontal[i] = aircraft.get_weights().get_MTOW().getEstimatedValue()
			*Math.sin(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

		// take-off trajectory
		MyChartToFileUtils.plotNoLegend(
				groundDistance, verticalDistance,
				0.0, null, 0.0, 12.0,
				"Ground Distance", "Altitude", "m", "m",
				subfolderPath, "TakeOff_Trajectory");

		// vertical distance v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, verticalDistance,
				0.0, null, 0.0, 12.0,
				"Time", "Altitude", "s", "m",
				subfolderPath, "Altitude_evolution");

		// speed v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, speed,
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				subfolderPath, "Speed_evolution");

		// speed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, speed,
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				subfolderPath, "Speed_vs_GroundDistance");

		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, acceleration,
				0.0, null, null, null,
				"Time", "Acceleration", "s", "m/(s^2)",
				subfolderPath, "Acceleration_evolution");

		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				groundDistance, acceleration,
				0.0, null, null, null,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				subfolderPath, "Acceleration_vs_GroundDistance");

		// load factor v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, loadFactor,
				0.0, null, 0.0, null,
				"Time", "Load Factor", "s", "",
				subfolderPath, "LoadFactor_evolution");

		// load factor v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, loadFactor,
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				subfolderPath, "LoadFactor_vs_GroundDistance");

		// Rate of Climb v.s. Time
		MyChartToFileUtils.plotNoLegend(
				time, rateOfClimb,
				0.0, null, 0.0, 10.0,
				"Time", "Rate of Climb", "s", "m/s",
				subfolderPath, "RateOfClimb_evolution");

		// Rate of Climb v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, rateOfClimb,
				0.0, null, 0.0, 10.0,
				"Ground distance", "Rate of Climb", "m", "m/s",
				subfolderPath, "RateOfClimb_vs_GroundDistance");

		// CL v.s. Time
		MyChartToFileUtils.plotNoLegend(
				time, cL,
				0.0, null, 0.0, null,
				"Time", "CL", "s", "",
				subfolderPath, "CL_evolution");

		// CL v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, cL,
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				subfolderPath, "CL_vs_GroundDistance");

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
				subfolderPath, "HorizontalForces_evolution");

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
				subfolderPath, "HorizontalForces_vs_GroundDistance");

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
				subfolderPath, "VerticalForces_evolution");

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
				subfolderPath, "VerticalForces_vs_GroundDistance");

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
				0.0, null, null, 5.0,
				"Time", "Angles", "s", "deg",
				new String[] {"Alpha", "Theta", "Gamma"},
				subfolderPath, "Angles_evolution");

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
				0.0, null, null, 5.0,
				"Ground Distance", "Angles", "m", "deg",
				new String[] {"Alpha", "Theta", "Gamma"},
				subfolderPath, "Angles_vs_GroundDistance");

		// Angular velocity v.s. time
		double[][] xMatrix7 = new double[2][totalForce.length];
		for(int i=0; i<xMatrix7.length; i++)
			xMatrix7[i] = time;

		double[][] yMatrix7 = new double[2][totalForce.length];
		yMatrix7[0] = alphaDot;
		yMatrix7[1] = gammaDot;

		MyChartToFileUtils.plot(
				xMatrix7, yMatrix7,
				0.0, null, null, 2.0,
				"Time", "Angular Velocity", "s", "deg/s",
				new String[] {"Alpha_dot", "Gamma_dot"},
				subfolderPath, "AngularVelocity_evolution");

		// Angular velocity v.s. Ground Distance
		double[][] xMatrix8 = new double[2][totalForce.length];
		for(int i=0; i<xMatrix8.length; i++)
			xMatrix8[i] = groundDistance;

		double[][] yMatrix8 = new double[2][totalForce.length];
		yMatrix8[0] = alphaDot;
		yMatrix8[1] = gammaDot;

		MyChartToFileUtils.plot(
				xMatrix8, yMatrix8,
				0.0, null, null, 11.0,
				"Ground Distance", "Angular Velocity", "m", "deg/s",
				new String[] {"Alpha_dot", "Gamma_dot"},
				subfolderPath, "AngularVelocity_vs_GroundDistance");

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
	public void createBalancedFieldLengthChart() {

		System.out.println("\n-------WRITING BALANCED TAKE-OFF DISTANCE CHART TO FILE--------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Take-Off_Performance" + File.separator);

		for(int i=0; i<failureSpeedArrayFitted.length; i++)
			failureSpeedArrayFitted[i] = failureSpeedArrayFitted[i]/vSTakeOff.getEstimatedValue();

		double[][] xArray = new double[][]
				{failureSpeedArrayFitted, failureSpeedArrayFitted};
				double[][] yArray = new double[][]
						{continuedTakeOffSplineValues, abortedTakeOffSplineValues};

						MyChartToFileUtils.plot(
								xArray, yArray,
								null, null, null, null,
								"Vfailure/VsTO", "Distance", "", "m",
								new String[] {"OEI Take-Off", "Aborted Take-Off"},
								subfolderPath, "BalancedTakeOffLength");

						System.out.println("\n---------------------------DONE!-------------------------------");
	}

	
	//-------------------------------------------------------------------------------------
    //										INNER CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquations implements FirstOrderDifferentialEquations {

		double weight, g0, mu, kAlpha, cD0, deltaCD0, oswald, ar, k1, k2, kGround, vWind;

		// interpolation of the two arrays
		public double[] timeBreakPoints;
		public double[] alphaBreakPoints;
		public LinearInterpolator alphaLinearInterpolator;
		public PolynomialSplineFunction alphaFunction;
		public double alpha;

		public DynamicsEquations() {

			// constants and known values
			weight = aircraft.get_weights().get_MTOW().getEstimatedValue();
			g0 = AtmosphereCalc.g0.getEstimatedValue();
			mu = CalcTakeOff_Landing.this.mu;
			kAlpha = CalcTakeOff_Landing.this.kAlphaDot;
			cD0 = aircraft.get_theAerodynamics().get_cD0();
			deltaCD0 = highLiftCalculator.getDeltaCD() + aircraft.get_landingGear().get_deltaCD0();
			oswald = aircraft.get_theAerodynamics().get_oswald();
			ar = aircraft.get_wing().get_aspectRatio();
			k1 = CalcTakeOff_Landing.this.getK1();
			k2 = CalcTakeOff_Landing.this.getK2();
			kGround = CalcTakeOff_Landing.this.getkGround();
			vWind = CalcTakeOff_Landing.this.getvWind().getEstimatedValue();

			// TODO : Add alpha method to evaluate alpha at different time
			
//			timeBreakPoints = MyArrayUtils.linspace(0.0, 100.0, 10);
//			alphaBreakPoints = MyArrayUtils.linspace(2.0, 5.0, 10);
//			alphaLinearInterpolator = new LinearInterpolator();
//			alphaFunction = alphaLinearInterpolator.interpolate(timeBreakPoints, alphaBreakPoints);
			alpha = CalcTakeOff_Landing.this.getAlphaGround().getEstimatedValue();
		}

		@Override
		public int getDimension() {
			return 4;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

//			double alpha = alphaFunction.value(t);
			double alpha = CalcTakeOff_Landing.this.getAlphaGround().getEstimatedValue();
//			double space = x[0];
			double speed = x[1];
			double gamma = x[2];
//			double altitude = x[3];

			xDot[0] = speed;
			xDot[1] = (g0/weight)*(thrust(speed) - drag(speed, alpha) - (mu*(weight - lift(speed, alpha))));
			xDot[2] = 0.0;
			xDot[3] = speed*Math.sin(gamma);
		}

		public double thrust(double speed) {

			double theThrust = 0.0;

			if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
				theThrust =	ThrustCalc.calculateThrustDatabase(
						aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
						aircraft.get_powerPlant().get_engineNumber(),
						phi,
						aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
						aircraft.get_powerPlant().get_engineType(),
						EngineOperatingConditionEnum.TAKE_OFF,
						theConditions.get_altitude().getEstimatedValue(),
						SpeedCalc.calculateMach(
								theConditions.get_altitude().getEstimatedValue(),
								speed + vWind)
						);
			}
			else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
				theThrust =	 ThrustCalc.calculateThrust(
						aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
						aircraft.get_powerPlant().get_engineNumber(),
						phi,
						theConditions.get_altitude().getEstimatedValue());
			}
			return theThrust;
		}

		public double drag(double speed, double alpha) {

			double cD = cD0 + deltaCD0 + ((cL(alpha)/(Math.PI*ar*oswald))*kGround); 

			return 	0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed + vWind, 2))
					*cD;

		}

		public double lift(double speed, double alpha) {

			double cL = cL(alpha);
		
			return 	0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed + vWind, 2))
					*cL;
		}
		
		public double cL(double alpha) {
			
			double cL0 = CalcTakeOff_Landing.this.cL0;
			double cLalpha = highLiftCalculator.getcLalpha_new();
			double alphaWing = alpha + CalcTakeOff_Landing.this.getIw().getEstimatedValue();
			
			return cL0 + (cLalpha*alphaWing);
		}
		
	}
	//-------------------------------------------------------------------------------------
    //									END INNER CLASS	
	//-------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:

	public Aircraft getAircraft() {
		return aircraft;
	}

	public void setAircraft(Aircraft aircraft) {
		this.aircraft = aircraft;
	}

	public OperatingConditions getTheConditions() {
		return theConditions;
	}

	public void setTheConditions(OperatingConditions theConditions) {
		this.theConditions = theConditions;
	}

	public CalcHighLiftDevices getHighLiftCalculator() {
		return highLiftCalculator;
	}

	public void setHighLiftCalculator(CalcHighLiftDevices highLiftCalculator) {
		this.highLiftCalculator = highLiftCalculator;
	}

	public Amount<Duration> getDt() {
		return dt;
	}

	public void setDt(Amount<Duration> dt) {
		this.dt = dt;
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

	public List<Double> getcLAborted() {
		return cLAborted;
	}

	public void setcLAborted(List<Double> cLAborted) {
		this.cLAborted = cLAborted;
	}

	public List<Double> getcD() {
		return cD;
	}

	public void setcD(List<Double> cD) {
		this.cD = cD;
	}

	public List<Double> getcDAborted() {
		return cDAborted;
	}

	public void setcDAborted(List<Double> cDAborted) {
		this.cDAborted = cDAborted;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}

	public List<Double> getLoadFactorAborted() {
		return loadFactorAborted;
	}

	public void setLoadFactorAborted(List<Double> loadFactorAborted) {
		this.loadFactorAborted = loadFactorAborted;
	}

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public List<Amount<Angle>> getAlphaAborted() {
		return alphaAborted;
	}

	public void setAlphaAborted(List<Amount<Angle>> alphaAborted) {
		this.alphaAborted = alphaAborted;
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

	public List<Amount<Duration>> getTimeAborted() {
		return timeAborted;
	}

	public void setTimeAborted(List<Amount<Duration>> timeAborted) {
		this.timeAborted = timeAborted;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}

	public List<Amount<Velocity>> getSpeedAborted() {
		return speedAborted;
	}

	public void setSpeedAborted(List<Amount<Velocity>> speedAborted) {
		this.speedAborted = speedAborted;
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

	public List<Amount<Acceleration>> getAccelerationAborted() {
		return accelerationAborted;
	}

	public void setAccelerationAborted(List<Amount<Acceleration>> accelerationAborted) {
		this.accelerationAborted = accelerationAborted;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getThrustAborted() {
		return thrustAborted;
	}

	public void setThrustAborted(List<Amount<Force>> thrustAborted) {
		this.thrustAborted = thrustAborted;
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

	public List<Amount<Force>> getLiftAborted() {
		return liftAborted;
	}

	public void setLiftAborted(List<Amount<Force>> liftAborted) {
		this.liftAborted = liftAborted;
	}

	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Force>> getDragAborted() {
		return dragAborted;
	}

	public void setDragAborted(List<Amount<Force>> dragAborted) {
		this.dragAborted = dragAborted;
	}

	public List<Amount<Force>> getFriction() {
		return friction;
	}

	public void setFriction(List<Amount<Force>> friction) {
		this.friction = friction;
	}

	public List<Amount<Force>> getFrictionAborted() {
		return frictionAborted;
	}

	public void setFrictionAborted(List<Amount<Force>> frictionAborted) {
		this.frictionAborted = frictionAborted;
	}

	public List<Amount<Force>> getTotalForce() {
		return totalForce;
	}

	public void setTotalForce(List<Amount<Force>> totalForce) {
		this.totalForce = totalForce;
	}

	public List<Amount<Force>> getTotalForceAborted() {
		return totalForceAborted;
	}

	public void setTotalForceAborted(List<Amount<Force>> totalForceAborted) {
		this.totalForceAborted = totalForceAborted;
	}

	public List<Amount<Length>> getDeltaGroundDistance() {
		return deltaGroundDistance;
	}

	public void setDeltaGroundDistance(List<Amount<Length>> deltaGroundDistance) {
		this.deltaGroundDistance = deltaGroundDistance;
	}

	public List<Amount<Length>> getDeltaGroundDistanceAborted() {
		return deltaGroundDistanceAborted;
	}

	public void setDeltaGroundDistanceAborted(List<Amount<Length>> deltaGroundDistanceAborted) {
		this.deltaGroundDistanceAborted = deltaGroundDistanceAborted;
	}

	public List<Amount<Length>> getGroundDistance() {
		return groundDistance;
	}

	public void setGroundDistance(List<Amount<Length>> groundDistance) {
		this.groundDistance = groundDistance;
	}

	public List<Amount<Length>> getGroundDistanceAborted() {
		return groundDistanceAborted;
	}

	public void setGroundDistanceAborted(List<Amount<Length>> groundDistanceAborted) {
		this.groundDistanceAborted = groundDistanceAborted;
	}

	public List<Amount<Length>> getDeltaVerticalDistance() {
		return deltaVerticalDistance;
	}

	public void setDeltaVerticalDistance(List<Amount<Length>> deltaVerticalDistance) {
		this.deltaVerticalDistance = deltaVerticalDistance;
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

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double getMuBrake() {
		return muBrake;
	}

	public void setMuBrake(double muBrake) {
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

	public double getkLO() {
		return kLO;
	}

	public double getKclMax() {
		return kcLMax;
	}

	public double getK1() {
		return k1;
	}

	public double getK2() {
		return k2;
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

	public double[] getContinuedTakeOffSplineValues() {
		return continuedTakeOffSplineValues;
	}

	public void setContinuedTakeOffSplineValues(double[] continuedTakeOffSplineValues) {
		this.continuedTakeOffSplineValues = continuedTakeOffSplineValues;
	}

	public double[] getAbortedTakeOffSplineValues() {
		return abortedTakeOffSplineValues;
	}

	public void setAbortedTakeOffSplineValues(double[] abortedTakeOffSplineValues) {
		this.abortedTakeOffSplineValues = abortedTakeOffSplineValues;
	}
}