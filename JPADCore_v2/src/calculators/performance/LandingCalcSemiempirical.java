package calculators.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.apache.commons.math3.ode.ContinuousOutputModel;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import calculators.performance.TakeOffCalc.DynamicsEquationsTakeOff;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * This class have the purpose of calculating the required landing field length
 * of a given aircraft by evaluating three main phases:
 *
 * - the airborne distance (from the obstacle altitude until the aircraft rotate the nose)
 * - the flare distance (from the rotation until the aircraft touches the ground)
 * - the ground roll distance (until the aircraft stops)
 *
 * the first two distances are evaluated using a simplified method which assumes that, in 
 * the approach phase, the trajectory evolves with constant attitude following a line; while 
 * the flare distance is calculated assuming that the aircraft follows a trajectory that is an
 * arc of circle. The last distance is, instead, calculated solving the equation of motion during
 * the decelerated ground roll (like the take-off the solution is obtained using an ODE integration
 * method). 
 *
 * @author Vittorio Trifari
 *
 */

public class LandingCalcSemiempirical {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION

	private Aircraft aircraft;
	private OperatingConditions theConditions;
	private Amount<Mass> maxLandingMass;
	private double[] polarCLLanding;
	private double[] polarCDLanding;
	private Amount<Duration> nFreeRoll;
	private Amount<Velocity> vSLanding, vA, vFlare, vTD, vWind;
	private Amount<Length> wingToGroundDistance, obstacle, sApproach, sFlare, sGround, sTotal;
	private Amount<Angle> alphaGround, iw, thetaApproach;
	private List<Double> loadFactor, timeBreakPoint;
	private List<Amount<Duration>> time;
	private List<Amount<Velocity>> speed;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> thrust, lift, drag, friction, totalForce;
	private List<Amount<Length>> landingDistance, verticalDistance;
	private List<Amount<Mass>> fuelUsed;
	private List<Amount<Force>> weight; 
	private double cLmaxLanding, kGround, cL0Landing, cLground, kA, kFlare, kTD, thrustCorrectionFactor, sfcCorrectionFactor;
	private MyInterpolatingFunction mu, muBrake;
	private ContinuousOutputModel continuousOutputModel;
	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;
	
	//-------------------------------------------------------------------------------------
	// BUILDER:

	/********************************************************************************************
	 * This builder is an overload of the previous one designed to allow the user 
	 * to perform the landing distance calculation without doing all flaps analysis.
	 * This may come in handy when only few data are available.
	 * 
	 * @author Vittorio Trifari
	 * @param aircraft
	 * @param theConditions
	 * @param kA percentage of the stall speed in landing which defines the approach speed
	 * @param kFlare percentage of the stall speed in landing which defines the flare speed
	 * @param kTD percentage of the stall speed in landing which defines the touch-down speed
	 * @param phiApproach throttle setting in approach phase
	 * @param phiGroundIdle throttle setting for the reverse thrust
	 * @param mu friction coefficient without brakes action
	 * @param muBrake friction coefficient with brakes activated
	 * @param wingToGroundDistance
	 * @param obstacle
	 * @param vWind
	 * @param alphaGround
	 * @param iw
	 * @param cD0
	 * @param oswald
	 * @param cLmaxLanding
	 * @param cL0Landing
	 * @param cLalphaFlap
	 * @param deltaCD0FlapLandingGearsAndSpoilers 
	 */
	public LandingCalcSemiempirical(
			Aircraft aircraft,
			OperatingConditions theConditions,
			Amount<Mass> maxLandingMass,
			double kA,
			double kFlare, 
			double kTD,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Length> obstacle,
			Amount<Length> wingToGroundDistance,
			Amount<Velocity> vWind,
			Amount<Angle> alphaGround,
			Amount<Angle> iw,
			Amount<Angle> thetaApproach,
			double cLmaxLanding,
			double cL0Landing,
			double cLalphaFlap,
			Amount<Duration> nFreeRoll,
			double[] polarCLLanding,
			double[] polarCDLanding,
			double thrustCorrectionFactor,
			double sfcCorrectionFactor
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.maxLandingMass = maxLandingMass;
		this.polarCLLanding = polarCLLanding;
		this.polarCDLanding = polarCDLanding;
		this.kA = kA;
		this.kFlare = kFlare;
		this.kTD = kTD;
		this.mu = mu;
		this.muBrake = muBrake;
		this.obstacle = obstacle;
		this.wingToGroundDistance = wingToGroundDistance;
		this.vWind = vWind;
		this.alphaGround = alphaGround;
		this.iw = iw;
		this.thetaApproach = thetaApproach;
		this.cLmaxLanding = cLmaxLanding;
		this.cL0Landing = cL0Landing; 
		this.nFreeRoll = nFreeRoll;
		this.thrustCorrectionFactor = thrustCorrectionFactor;
		this.sfcCorrectionFactor = sfcCorrectionFactor;
		
		this.cLground = cL0Landing + (cLalphaFlap*iw.getEstimatedValue());

		// Reference velocities definition
		vSLanding = SpeedCalc.calculateSpeedStall(
						theConditions.getAltitudeLanding(),
						theConditions.getDeltaTemperatureLanding(),
						maxLandingMass,
						aircraft.getWing().getSurfacePlanform(),
						cLmaxLanding
						);
		vA = vSLanding.times(kA);
		vFlare = vSLanding.times(kFlare);
		vTD = vSLanding.times(kTD);

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxLanding = " + cLmaxLanding);
		System.out.println("CL0 = " + cL0Landing);
		System.out.println("CLground = " + cLground);
		System.out.println("VsLanding = " + vSLanding);
		System.out.println("V_Approach = " + vA);
		System.out.println("V_Flare = " + vFlare);
		System.out.println("V_TouchDown = " + vTD);
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.time = new ArrayList<>();
		this.speed = new ArrayList<>();
		this.thrust = new ArrayList<>();
		this.lift = new ArrayList<>();
		this.loadFactor = new ArrayList<>();
		this.drag = new ArrayList<>();
		this.friction = new ArrayList<>();
		this.totalForce = new ArrayList<>();
		this.acceleration = new ArrayList<>();
		this.landingDistance = new ArrayList<>();
		this.verticalDistance = new ArrayList<>();
		this.fuelUsed = new ArrayList<>();
		this.weight = new ArrayList<>();
		this.timeBreakPoint = new ArrayList<>();
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
		lift.clear();
		loadFactor.clear();
		drag.clear();
		friction.clear();
		totalForce.clear();
		acceleration.clear();
		landingDistance.clear();
		verticalDistance.clear();
		fuelUsed.clear();
		weight.clear();
		timeBreakPoint.clear();
	}

	/**************************************************************************************
	 * This method performs the calculation of the airborne distance using a simplified
	 * method in which, known the angle thetaApproach in approach phase (about D/L-T/W), the 
	 * approach distance is calculated as sApproach=(obstacle-hFlare)/tan(thetaApproach); where
	 * hFlare is the altitude at which the aircraft start to rotate. On the other hand, the
	 * flare distance is calculated as sFlare=R*sin(thetaApproach), where R is the radius 
	 * of the trajectory measured with the flare speed (vFlare) and assuming a load factor of 1.2.
	 * 
	 * @author Vittorio Triari
	 */
	public void calculateAirborneDistance() {
		
		double radius = Math.pow(this.vFlare.getEstimatedValue(),2)/(0.2*AtmosphereCalc.g0.getEstimatedValue());
				
		double hFlare = radius*(1-Math.cos(this.thetaApproach.to(SI.RADIAN).getEstimatedValue()));
		
		sApproach = Amount.valueOf(
				(this.obstacle.getEstimatedValue()-hFlare)/Math.tan(this.thetaApproach.to(SI.RADIAN).getEstimatedValue()),
				SI.METER
				);
		sFlare = Amount.valueOf(
				radius*Math.sin(this.thetaApproach.to(SI.RADIAN).getEstimatedValue()),
				SI.METER
				);

		// DATA AT THE BEGINNING OF THE APPROACH PHASE
		landingDistance.add(Amount.valueOf(0.0, SI.METER));
		verticalDistance.add(obstacle);
		speed.add(vA);

		// DATA RELATED TO FLARE PHASE
		double[] speedArray = MyArrayUtils.linspace(
				vA.getEstimatedValue(),
				vTD.getEstimatedValue(),
				10);
		double[] xDistance = MyArrayUtils.linspace(
				sApproach.getEstimatedValue(),
				sApproach.plus(sFlare).getEstimatedValue(),
				10);
		double[] yDistance = new double[xDistance.length];

		double a = -2*sApproach.plus(sFlare).getEstimatedValue();
		double b = -2*radius;
		double c = Math.pow(sApproach.plus(sFlare).getEstimatedValue(), 2);

		for(int i=0; i<xDistance.length; i++) {
			yDistance[i] = -(Math.sqrt(
					((Math.pow(b, 2))/4)
					-c
					-(a*xDistance[i])
					-(Math.pow(xDistance[i], 2))))
					-(b/2);
			speed.add(Amount.valueOf(speedArray[i], SI.METERS_PER_SECOND));
			landingDistance.add(Amount.valueOf(xDistance[i], SI.METER));
			verticalDistance.add(Amount.valueOf(yDistance[i], SI.METER));
		}

		System.out.println("\n---------------------------END!!-------------------------------");
	}

	/***************************************************************************************
	 * This method performs the integration of the ground roll distance in landing
	 * by solving a set of ODE with a HighamHall54Integrator.
	 * The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateGroundRollLandingODE() {

		System.out.println("---------------------------------------------------");
		System.out.println("CalcLanding :: Ground Roll ODE integration\n\n");

		theIntegrator = new HighamHall54Integrator(
				1e-8,
				1,
				1e-8,
				1e-8
				);
		ode = new DynamicsEquationsLanding();

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
				System.out.println("\n\t\tEND OF THE LANDING GROUND ROLL");
				System.out.println("\n\tswitching function changes sign at t = " + t);
				System.out.println(
						"\n\tx[0] = s = " + x[0] + " m" +
						"\n\tx[1] = V = " + x[1] + " m/s"
						);

				System.out.println("\n---------------------------DONE!-------------------------------");
				timeBreakPoint.add(t);
				return  Action.STOP;
			}
		};
		theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-3, 20);

		// handle detailed info
		StepHandler stepHandler = new StepHandler() {

			public void init(double t0, double[] x0, double t) {
			}

			@Override
			public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

				double   t = interpolator.getCurrentTime();
				double[] x = interpolator.getInterpolatedState();
						
				//========================================================================================
				// PICKING UP ALL VARIABLES AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
				Amount<Angle> gamma = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
				Amount<Length> altitude = theConditions.getAltitudeLanding();
				Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
				Amount<Force> weight = Amount.valueOf( 
						(maxLandingMass.doubleValue(SI.KILOGRAM) - x[2])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
						SI.NEWTON
						);
				
				//----------------------------------------------------------------------------------------
				// PICKING UP ALL DATA AT EVERY STEP 
				//----------------------------------------------------------------------------------------
				// TIME:
				LandingCalcSemiempirical.this.getTime().add(time);
				//----------------------------------------------------------------------------------------
				// WEIGHT:
				LandingCalcSemiempirical.this.getWeight().add(weight);
				//----------------------------------------------------------------------------------------
				// LOAD FACTOR:
				LandingCalcSemiempirical.this.getLoadFactor().add(
						((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON)
						/weight.doubleValue(SI.NEWTON)
						);
				//----------------------------------------------------------------------------------------
				// ACCELERATION:
				if(t < nFreeRoll.getEstimatedValue())
					LandingCalcSemiempirical.this.getAcceleration().add(	
							Amount.valueOf((AtmosphereCalc.g0.getEstimatedValue()/weight.doubleValue(SI.NEWTON))
									*(((DynamicsEquationsLanding)ode).thrust(speed, gamma, altitude).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
											- ((DynamicsEquationsLanding)ode).drag(speed, altitude).doubleValue(SI.NEWTON)
											- ((DynamicsEquationsLanding)ode).mu(speed)
											*(weight.doubleValue(SI.NEWTON)
													- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON))
											),
									SI.METERS_PER_SQUARE_SECOND)
							);
				else
					LandingCalcSemiempirical.this.getAcceleration().add(	
							Amount.valueOf((AtmosphereCalc.g0.getEstimatedValue()/weight.doubleValue(SI.NEWTON))
									*(((DynamicsEquationsLanding)ode).thrust(speed, gamma, altitude).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
											- ((DynamicsEquationsLanding)ode).drag(speed, altitude).doubleValue(SI.NEWTON)
											- ((DynamicsEquationsLanding)ode).muBrake(speed)
											*(weight.doubleValue(SI.NEWTON)
													- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON))
											),
									SI.METERS_PER_SQUARE_SECOND)
							);
			}
		};
		theIntegrator.addStepHandler(stepHandler);
		
		continuousOutputModel = new ContinuousOutputModel();
		theIntegrator.addStepHandler(continuousOutputModel);
		
		double[] xAt0 = new double[] {
				sApproach.plus(sFlare).getEstimatedValue(),
				vTD.getEstimatedValue()
				}; // initial state
		theIntegrator.integrate(ode, 0.0, xAt0, 100, xAt0); // now xAt0 contains final state
		
		theIntegrator.clearEventHandlers();
		theIntegrator.clearStepHandlers();
		
		this.sTotal = this.landingDistance.get(this.landingDistance.size()-1);
		this.sGround = this.sTotal.minus(this.sApproach).minus(this.sFlare);
		
		System.out.println("\n---------------------------END!!-------------------------------");
	}

	/********************************************************************************************
	 * This method allows users to fill all the maps of results related to each throttle setting.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void manageOutputData(double dt, StepHandler handler) {

		MyInterpolatingFunction loadFactorFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction accelerationFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction weightFunction = new MyInterpolatingFunction();

		loadFactorFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.loadFactor))
				);
		accelerationFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.acceleration)
				);
		weightFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.weight)
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
				for(int i=0; i<timeBreakPoint.size(); i++) {
					double t_ = timeBreakPoint.get(i);
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
				//========================================================================================
				// PICKING UP ALL VARIABLES AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
				Amount<Length> groundDistance = Amount.valueOf(x[0], SI.METER);
				Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
				Amount<Angle> gamma = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
				Amount<Length> altitude = theConditions.getAltitudeLanding();
				
				//========================================================================================
				// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				// GROUND DISTANCE:
				this.landingDistance.add(groundDistance);
				//----------------------------------------------------------------------------------------
				// VERTICAL DISTANCE:
				this.verticalDistance.add(altitude);
				//----------------------------------------------------------------------------------------
				// THRUST:
				this.thrust.add(Amount.valueOf(
						((DynamicsEquationsLanding)ode).thrust(speed, gamma, altitude)
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON)
						);
				//--------------------------------------------------------------------------------
				// FUEL USED (kg/s):
				this.fuelUsed.add(Amount.valueOf(x[4], SI.KILOGRAM));
				//----------------------------------------------------------------------------------------
				// WEIGHT:
				this.weight.add(
						Amount.valueOf(
								weightFunction.value(times.get(i).doubleValue(SI.SECOND)),
								SI.NEWTON
								)
						);
				//----------------------------------------------------------------------------------------
				// SPEED:
				this.speed.add(speed);
				//--------------------------------------------------------------------------------
				// FRICTION:
				if(times.get(i).doubleValue(SI.SECOND) < nFreeRoll.doubleValue(SI.SECOND))
					this.friction.add(Amount.valueOf(
							((DynamicsEquationsLanding)ode).mu(speed)
							* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
									- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON)
									),
							SI.NEWTON)
							);
				else
					this.friction.add(Amount.valueOf(
							((DynamicsEquationsLanding)ode).muBrake(speed)
							* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
									- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON)
									),
							SI.NEWTON)
							);
				//----------------------------------------------------------------------------------------
				// LIFT:
				this.lift.add(((DynamicsEquationsLanding)ode).lift(speed));
				//----------------------------------------------------------------------------------------
				// DRAG:
				this.drag.add(((DynamicsEquationsLanding)ode).drag(speed, altitude));
				//----------------------------------------------------------------------------------------
				// TOTAL FORCE:
				if(time.doubleValue(SI.SECOND) < nFreeRoll.doubleValue(SI.SECOND))
					this.totalForce.add(Amount.valueOf(
							(((DynamicsEquationsLanding)ode).thrust(speed, gamma, altitude)
									.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									)
							- ((DynamicsEquationsLanding)ode).drag(speed, altitude).doubleValue(SI.NEWTON)
							- (((DynamicsEquationsTakeOff)ode).mu(speed)
									* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
											- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON)
											)
									),
							SI.NEWTON)
							);
				else
					this.totalForce.add(Amount.valueOf(
							(((DynamicsEquationsLanding)ode).thrust(speed, gamma, altitude)
									.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									)
							- ((DynamicsEquationsLanding)ode).drag(speed, altitude).doubleValue(SI.NEWTON)
							- (((DynamicsEquationsTakeOff)ode).muBrake(speed)
									* (weightFunction.value(times.get(i).doubleValue(SI.SECOND))
											- ((DynamicsEquationsLanding)ode).lift(speed).doubleValue(SI.NEWTON)
											)
									),
							SI.NEWTON)
							);
				//----------------------------------------------------------------------------------------
				// LOAD FACTOR:
				this.loadFactor.add(loadFactorFunction.value(times.get(i).doubleValue(SI.SECOND)));
				//----------------------------------------------------------------------------------------
				// ACCELERATION:
				this.acceleration.add(Amount.valueOf(
						accelerationFunction.value(times.get(i).doubleValue(SI.SECOND)),
						SI.METERS_PER_SQUARE_SECOND)
						);
			}
		}
	}
	
	/**************************************************************************************
	 * This method allows users to plot all landing performance producing several output charts
	 * which have time or ground distance as independent variables.
	 *
	 * @author Vittorio Trifari
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createLandingCharts(String landingFolderPath) throws InstantiationException, IllegalAccessException {

		System.out.println("\n---------WRITING GROUND ROLL PERFORMANCE CHARTS TO FILE-----------");

		//.............................................................................
		// landing trajectory and speed
		double[][] xMatrix1_SI = new double[2][landingDistance.size()];
		double[][] xMatrix1_Imperial = new double[2][landingDistance.size()];
		xMatrix1_SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(landingDistance);
		xMatrix1_SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(landingDistance);
		
		xMatrix1_Imperial[0] = MyArrayUtils.convertToDoublePrimitive( 
				landingDistance.stream()
				.map(x -> x.doubleValue(NonSI.FOOT))
				.collect(Collectors.toList())
				);
		xMatrix1_Imperial[1] = MyArrayUtils.convertToDoublePrimitive( 
				landingDistance.stream()
				.map(x -> x.doubleValue(NonSI.FOOT))
				.collect(Collectors.toList())
				);

		double[][] yMatrix1_SI = new double[2][landingDistance.size()];
		double[][] yMatrix1_Imperial = new double[2][landingDistance.size()];
		yMatrix1_SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance);
		yMatrix1_SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(speed);

		yMatrix1_Imperial[0] = MyArrayUtils.convertToDoublePrimitive(
				verticalDistance.stream()
				.map(x -> x.doubleValue(NonSI.FOOT))
				.collect(Collectors.toList())
				);
		yMatrix1_Imperial[1] = MyArrayUtils.convertToDoublePrimitive(
				speed.stream()
				.map(x -> x.doubleValue(NonSI.KNOT))
				.collect(Collectors.toList())
				);
		
		MyChartToFileUtils.plot(
				xMatrix1_SI, yMatrix1_SI,
				0.0, null, -1.0, null,
				"Ground Distance", "", "m", "",
				new String[] {"Altitude (m)", "Speed (m/s)"},
				landingFolderPath, "TrajectoryAndSpeed_vs_GroundDistance_SI",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		MyChartToFileUtils.plot(
				xMatrix1_Imperial, yMatrix1_Imperial,
				0.0, null, -1.0, null,
				"Ground Distance", "", "ft", "",
				new String[] {"Altitude (ft)", "Speed (kn)"},
				landingFolderPath, "TrajectoryAndSpeed_vs_GroundDistance_IMPERIAL",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		//.............................................................................
		// acceleration v.s. ground roll distance
		double[] groundRollDistance_SI = new double[getLandingDistance().size()-11];
		double[] groundRollDistance_Imperial = new double[getLandingDistance().size()-11];
		for(int i=0; i<groundRollDistance_SI.length; i++) {
			groundRollDistance_SI[i] = landingDistance.get(i+11).doubleValue(SI.METER);
			groundRollDistance_Imperial[i] = landingDistance.get(i+11).doubleValue(NonSI.FOOT);
		}
		
		MyChartToFileUtils.plotNoLegend(
				groundRollDistance_SI, 
				MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
				sApproach.plus(sFlare).doubleValue(SI.METER), null, null, null,
				"Ground Roll Distance", "Acceleration", "m", "m/(s^2)",
				landingFolderPath, "Acceleration_vs_GroundDistance_SI",true);
		
		MyChartToFileUtils.plotNoLegend(
				groundRollDistance_Imperial,
				MyArrayUtils.convertToDoublePrimitive(
						acceleration.stream()
						.map(x -> x.doubleValue(MyUnits.FOOT_PER_SQUARE_MINUTE))
						.collect(Collectors.toList())
						),
				sApproach.plus(sFlare).doubleValue(NonSI.FOOT), null, null, null,
				"Ground Roll Distance", "Acceleration", "ft", "ft/(min^2)",
				landingFolderPath, "Acceleration_vs_GroundDistance_IMPERIAL",true);

		//.............................................................................
		// load factor v.s. ground roll distance
		MyChartToFileUtils.plotNoLegend(
				groundRollDistance_SI,
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				sApproach.plus(sFlare).doubleValue(SI.METER), null, 0.0, null,
				"Ground Roll distance", "Load Factor", "m", "",
				landingFolderPath, "LoadFactor_vs_GroundDistance_SI",true);

		MyChartToFileUtils.plotNoLegend(
				groundRollDistance_Imperial,
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				sApproach.plus(sFlare).doubleValue(NonSI.FOOT), null, 0.0, null,
				"Ground Roll distance", "Load Factor", "ft", "",
				landingFolderPath, "LoadFactor_vs_GroundDistance_IMPERIAL",true);
		//.............................................................................
		// Horizontal Forces v.s. ground roll distance
		double[][] xMatrix2_SI = new double[4][totalForce.size()];
		double[][] xMatrix2_Imperial = new double[4][totalForce.size()];
		xMatrix2_SI[0] = groundRollDistance_SI;
		xMatrix2_SI[1] = groundRollDistance_SI;
		xMatrix2_SI[2] = groundRollDistance_SI;
		xMatrix2_SI[3] = groundRollDistance_SI;
		xMatrix2_Imperial[0] = groundRollDistance_Imperial;
		xMatrix2_Imperial[1] = groundRollDistance_Imperial;
		xMatrix2_Imperial[2] = groundRollDistance_Imperial;
		xMatrix2_Imperial[3] = groundRollDistance_Imperial;

		double[][] yMatrix2_SI = new double[4][totalForce.size()];
		double[][] yMatrix2_Imperial = new double[4][totalForce.size()];
		yMatrix2_SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
		yMatrix2_SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrust);
		yMatrix2_SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
		yMatrix2_SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);

		yMatrix2_Imperial[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				totalForce.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2_Imperial[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
				thrust.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2_Imperial[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
				drag.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix2_Imperial[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
				friction.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		
		MyChartToFileUtils.plot(
				xMatrix2_SI, yMatrix2_SI,
				sApproach.plus(sFlare).doubleValue(SI.METER), null, null, null,
				"Ground Roll Distance", "Horizontal Forces", "m", "N",
				new String[] {"Total Force", "Thrust", "Drag", "Friction"},
				landingFolderPath, "HorizontalForces_vs_GroundDistance_SI",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance());
		
		MyChartToFileUtils.plot(
				xMatrix2_Imperial, yMatrix2_Imperial,
				sApproach.plus(sFlare).doubleValue(NonSI.FOOT), null, null, null,
				"Ground Roll Distance", "Horizontal Forces", "ft", "lb",
				new String[] {"Total Force", "Thrust", "Drag", "Friction"},
				landingFolderPath, "HorizontalForces_vs_GroundDistance_IMPERIAL",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance());

		//.............................................................................
		// Vertical Forces v.s. ground roll distance
		double[] weight_SI = new double[getTime().size()];
		double[] weight_Imperial = new double[getTime().size()];
		for(int i=0; i<weight_SI.length; i++) {
			weight_SI[i] = maxLandingMass.times(AtmosphereCalc.g0).getEstimatedValue();
			weight_Imperial[i] = maxLandingMass.times(AtmosphereCalc.g0).times(0.224809).getEstimatedValue();
		}
			
		double[][] xMatrix3_SI = new double[2][totalForce.size()];
		double[][] xMatrix3_Imperial = new double[2][totalForce.size()];
		for(int i=0; i<xMatrix3_SI.length; i++) {
			xMatrix3_SI[i] = groundRollDistance_SI;
			xMatrix3_Imperial[i] = groundRollDistance_Imperial;
		}
			
		double[][] yMatrix3_SI = new double[2][totalForce.size()];
		double[][] yMatrix3_Imperial = new double[2][totalForce.size()];
		yMatrix3_SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
		yMatrix3_SI[1] = weight_SI;

		yMatrix3_Imperial[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
				lift.stream()
				.map(x -> x.to(NonSI.POUND_FORCE))
				.collect(Collectors.toList())
				);
		yMatrix3_Imperial[1] = weight_Imperial;
		
		MyChartToFileUtils.plot(
				xMatrix3_SI, yMatrix3_SI,
				sApproach.plus(sFlare).doubleValue(SI.METER), null, null, null,
				"Ground Roll distance", "Vertical Forces", "m", "N",
				new String[] {"Lift", "Weight"},
				landingFolderPath, "VerticalForces_vs_GroundDistance_SI",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance());
		
		MyChartToFileUtils.plot(
				xMatrix3_Imperial, yMatrix3_Imperial,
				sApproach.plus(sFlare).doubleValue(NonSI.FOOT), null, null, null,
				"Ground Roll distance", "Vertical Forces", "ft", "lb",
				new String[] {"Lift", "Weight"},
				landingFolderPath, "VerticalForces_vs_GroundDistance_IMPERIAL",
				aircraft.getTheAnalysisManager().getCreateCSVPerformance());
		
		System.out.println("\n---------------------------DONE!-------------------------------");
	}
	
	/**************************************************************************************
	 * This method perform the calculation of the total landing distance by calling the 
	 * previous methods
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateLandingDistance() {
		calculateAirborneDistance();
		calculateGroundRollLandingODE();
	}
	
	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsLanding implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);
		
		public DynamicsEquationsLanding() {

		}

		@Override
		public int getDimension() {
			return 3;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
			Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			Amount<Angle> gamma = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			Amount<Length> altitude = LandingCalcSemiempirical.this.theConditions.getAltitudeLanding();
			Amount<Force> weight = Amount.valueOf(
					(maxLandingMass.doubleValue(SI.KILOGRAM) - x[2])*g0,
					SI.NEWTON
					);
			
			if(time.doubleValue(SI.SECOND) < LandingCalcSemiempirical.this.getnFreeRoll().doubleValue(SI.SECOND)) {
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*(thrust(speed, gamma, altitude).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum() 
								- drag(speed, altitude).doubleValue(SI.NEWTON)
								- (mu(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed).doubleValue(SI.NEWTON)))
								);
				xDot[2] = fuelFlow(speed, gamma, altitude);
			}
			else {
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*(thrust(speed, gamma, altitude).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								- drag(speed, altitude).doubleValue(SI.NEWTON)
								- (muBrake(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed).doubleValue(SI.NEWTON)))
								);
				xDot[2] = fuelFlow(speed, gamma, altitude);
			}
		}

		public List<Amount<Force>> thrust(Amount<Velocity> speed, Amount<Angle> gamma, Amount<Length> altitude) {

			List<Amount<Force>> theThrustList = new ArrayList<>();
			
			for (int i=0; i<aircraft.getPowerPlant().getEngineNumber(); i++)
			theThrustList.add(
					ThrustCalc.calculateThrustDatabase(
							LandingCalcSemiempirical.this.getAircraft().getPowerPlant().getEngineList().get(i).getT0(),
							LandingCalcSemiempirical.this.getAircraft().getPowerPlant().getEngineDatabaseReaderList().get(i),
							EngineOperatingConditionEnum.GIDL, 
							altitude, 
							SpeedCalc.calculateMach(
									altitude,
									LandingCalcSemiempirical.this.getTheConditions().getDeltaTemperatureLanding(),
									Amount.valueOf(
											speed.doubleValue(SI.METERS_PER_SECOND) 
											+ LandingCalcSemiempirical.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
											SI.METERS_PER_SECOND
											)
									),
							LandingCalcSemiempirical.this.getTheConditions().getDeltaTemperatureLanding(), 
							theConditions.getThrottleLanding(),
							LandingCalcSemiempirical.this.getThrustCorrectionFactor()
							)
					);

			return theThrustList;
		}

		public double fuelFlow(Amount<Velocity> speed, Amount<Angle> gamma, Amount<Length> altitude) {

			List<Double> fuelFlowList = new ArrayList<>();
			List<Amount<Force>> thrustList = thrust(speed, gamma, altitude); 
			
			for (int i=0; i<aircraft.getPowerPlant().getEngineNumber(); i++) 
				fuelFlowList.add(
						aircraft.getPowerPlant().getEngineDatabaseReaderList().get(i).getSfc(
								SpeedCalc.calculateMach(
										altitude,
										LandingCalcSemiempirical.this.getTheConditions().getDeltaTemperatureLanding(),
										Amount.valueOf(
												speed.doubleValue(SI.METERS_PER_SECOND) 
												+ LandingCalcSemiempirical.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
												SI.METERS_PER_SECOND
												)
										),
								altitude,
								LandingCalcSemiempirical.this.getTheConditions().getDeltaTemperatureLanding(),
								theConditions.getThrottleLanding(),
								EngineOperatingConditionEnum.GIDL,
								LandingCalcSemiempirical.this.getSfcCorrectionFactor()
								)
						*(0.224809)*(0.454/3600)
						*thrustList.get(i).doubleValue(SI.NEWTON)
						);

			return fuelFlowList.stream().mapToDouble(ff -> ff).sum();

		}
		
		public Amount<Force> drag(Amount<Velocity> speed, Amount<Length> altitude) {

			// Aerodynamics For Naval Aviators: (Hurt)
			double hb = (LandingCalcSemiempirical.this.getWingToGroundDistance().doubleValue(SI.METER) / aircraft.getWing().getSpan().doubleValue(SI.METER)) + altitude.doubleValue(SI.METER);
			kGround = 1- (-4.48276577 * Math.pow(hb, 5) 
					+ 15.61174376 * Math.pow(hb, 4)
					- 21.20171050 * Math.pow(hb, 3)
					+ 14.39438721 * Math.pow(hb, 2)
					- 5.20913465 * hb
					+ 0.90793397);
			
			double cD = MyMathUtils
					.getInterpolatedValue1DLinear(
							polarCLLanding,
							polarCDLanding,
							LandingCalcSemiempirical.this.getcLground()
							);

			double cD0 = MyArrayUtils.getMin(polarCDLanding);
			double cDi = (cD-cD0)*kGround;
			
			double cDnew = cD0 + cDi;

			return 	Amount.valueOf(
					0.5
					*aircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(
							theConditions.getAltitudeLanding().doubleValue(SI.METER),
							theConditions.getDeltaTemperatureLanding().doubleValue(SI.CELSIUS)
							)
					*(Math.pow((speed.doubleValue(SI.METERS_PER_SECOND) + vWind.doubleValue(SI.METERS_PER_SECOND)), 2))
					*cDnew,
					SI.NEWTON
					);
			
		}

		public Amount<Force> lift(Amount<Velocity> speed) {

			return 	Amount.valueOf(
					0.5
					*aircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(
							theConditions.getAltitudeLanding().doubleValue(SI.METER),
							theConditions.getDeltaTemperatureLanding().doubleValue(SI.CELSIUS)
							)
					*(Math.pow((speed.doubleValue(SI.METERS_PER_SECOND) + vWind.doubleValue(SI.METERS_PER_SECOND)), 2))
					*LandingCalcSemiempirical.this.getcLground(),
					SI.NEWTON
					);
		}
		
		public double mu(Amount<Velocity> speed) {
			return mu.value(speed.doubleValue(SI.METERS_PER_SECOND));
		}
		
		public double muBrake(Amount<Velocity> speed) {
			return muBrake.value(speed.doubleValue(SI.METERS_PER_SECOND));
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
	public OperatingConditions getTheConditions() {
		return theConditions;
	}
	public void setTheConditions(OperatingConditions theConditions) {
		this.theConditions = theConditions;
	}
	public Amount<Velocity> getvSLanding() {
		return vSLanding;
	}
	public void setvSLanding(Amount<Velocity> vSLanding) {
		this.vSLanding = vSLanding;
	}
	public Amount<Velocity> getvA() {
		return vA;
	}
	public void setvA(Amount<Velocity> vA) {
		this.vA = vA;
	}
	public Amount<Velocity> getvFlare() {
		return vFlare;
	}
	public void setvFlare(Amount<Velocity> vFlare) {
		this.vFlare = vFlare;
	}
	public Amount<Velocity> getvTD() {
		return vTD;
	}
	public void setvTD(Amount<Velocity> vTD) {
		this.vTD = vTD;
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
	public Amount<Angle> getIw() {
		return iw;
	}
	public void setIw(Amount<Angle> iw) {
		this.iw = iw;
	}
	public List<Double> getLoadFactor() {
		return loadFactor;
	}
	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
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
	public List<Amount<Length>> getLandingDistance() {
		return landingDistance;
	}
	public void setLandingDistance(List<Amount<Length>> landingDistance) {
		this.landingDistance = landingDistance;
	}
	/**
	 * @return the verticalDistance
	 */
	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	/**
	 * @param verticalDistance the verticalDistance to set
	 */
	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
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
	public double getcLmaxLanding() {
		return cLmaxLanding;
	}
	public void setcLmaxLanding(double cLmaxLanding) {
		this.cLmaxLanding = cLmaxLanding;
	}
	public double getkGround() {
		return kGround;
	}
	public void setkGround(double kGround) {
		this.kGround = kGround;
	}
	public double getcL0() {
		return cL0Landing;
	}
	public void setcL0(double cL0) {
		this.cL0Landing = cL0;
	}
	public double getcLground() {
		return cLground;
	}
	public void setcLground(double cLground) {
		this.cLground = cLground;
	}
	public double getkA() {
		return kA;
	}
	public void setkA(double kA) {
		this.kA = kA;
	}
	public double getkFlare() {
		return kFlare;
	}
	public void setkFlare(double kFlare) {
		this.kFlare = kFlare;
	}
	public double getkTD() {
		return kTD;
	}
	public void setkTD(double kTD) {
		this.kTD = kTD;
	}
	public Amount<Length> getsApproach() {
		return sApproach;
	}
	public void setsApproach(Amount<Length> sApproach) {
		this.sApproach = sApproach;
	}
	public Amount<Length> getsFlare() {
		return sFlare;
	}
	public void setsFlare(Amount<Length> sFlare) {
		this.sFlare = sFlare;
	}
	public Amount<Length> getsGround() {
		return sGround;
	}
	public void setsGround(Amount<Length> sGround) {
		this.sGround = sGround;
	}
	public Amount<Length> getsTotal() {
		return sTotal;
	}
	public void setsTotal(Amount<Length> sTotal) {
		this.sTotal = sTotal;
	}
	public Amount<Duration> getnFreeRoll() {
		return nFreeRoll;
	}
	public void setnFreeRoll(Amount<Duration> nFreeRoll) {
		this.nFreeRoll = nFreeRoll;
	}
	public Amount<Angle> getThetaApproach() {
		return thetaApproach;
	}
	public void setThetaApproach(Amount<Angle> thetaApproach) {
		this.thetaApproach = thetaApproach;
	}
	public Amount<Mass> getMaxLandingMass() {
		return maxLandingMass;
	}
	public void setMaxLandingMass(Amount<Mass> maxLandingMass) {
		this.maxLandingMass = maxLandingMass;
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

	public List<Amount<Force>> getWeight() {
		return weight;
	}

	public void setWeight(List<Amount<Force>> weight) {
		this.weight = weight;
	}

	public List<Double> getTimeBreakPoint() {
		return timeBreakPoint;
	}

	public void setTimeBreakPoint(List<Double> timeBreakPoint) {
		this.timeBreakPoint = timeBreakPoint;
	}

	public double getThrustCorrectionFactor() {
		return thrustCorrectionFactor;
	}

	public void setThrustCorrectionFactor(double thrustCorrectionFactor) {
		this.thrustCorrectionFactor = thrustCorrectionFactor;
	}

	public double getSfcCorrectionFactor() {
		return sfcCorrectionFactor;
	}

	public void setSfcCorrectionFactor(double sfcCorrectionFactor) {
		this.sfcCorrectionFactor = sfcCorrectionFactor;
	}
}