package calculators.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
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
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftDevices;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

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

public class LandingCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION

	private Aircraft aircraft;
	private OperatingConditions theConditions;
	private CalcHighLiftDevices highLiftCalculator;
	private Amount<Mass> maxLandingMass;
	private Amount<Duration> nFreeRoll;
	private Amount<Velocity> vSLanding, vA, vFlare, vTD, vWind;
	private Amount<Length> wingToGroundDistance, obstacle, sApproach, sFlare, sGround, sTotal;
	private Amount<Angle> alphaGround, iw, thetaApproach;
	private List<Double> loadFactor;
	private List<Amount<Duration>> time;
	private List<Amount<Velocity>> speed;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> thrust, lift, drag, friction, totalForce;
	private List<Amount<Length>> landingDistance, verticalDistance;
	private double mu, muBrake, cLmaxLanding, kGround, cL0Landing, cLground, kA, kFlare, kTD, phiRev;
	private double oswald, cD0, cLalphaFlap, deltaCD0Spoiler, deltaCD0LandignGear, deltaCD0FlapLandinGearsSpoilers;

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
	 * @param phiRev throttle setting for the reverse thrust
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
	 * @param deltaCD0FlapLandingGears 
	 */
	public LandingCalc(
			Aircraft aircraft,
			OperatingConditions theConditions,
			Amount<Mass> maxLandingMass,
			double kA,
			double kFlare, 
			double kTD,
			double mu,
			double muBrake,
			Amount<Length> wingToGroundDistance,
			Amount<Length> obstacle,
			Amount<Velocity> vWind,
			Amount<Angle> alphaGround,
			Amount<Angle> iw,
			Amount<Angle> thetaApproach,
			double cD0,
			double oswald,
			double cLmaxLanding,
			double cL0Landing,
			double cLalphaFlap,
			double deltaCD0FlapLandingGears,
			Amount<Duration> nFreeRoll
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.maxLandingMass = maxLandingMass;
		this.kA = kA;
		this.kFlare = kFlare;
		this.kTD = kTD;
		this.mu = mu;
		this.muBrake = muBrake;
		this.wingToGroundDistance = wingToGroundDistance;
		this.obstacle = obstacle;
		this.vWind = vWind;
		this.alphaGround = alphaGround;
		this.iw = iw;
		this.thetaApproach = thetaApproach;
		this.cD0 = cD0;
		this.oswald = oswald;
		this.cLmaxLanding = cLmaxLanding;
		this.deltaCD0FlapLandinGearsSpoilers = deltaCD0FlapLandingGears;
		this.cL0Landing = cL0Landing; 
		this.cLalphaFlap = cLalphaFlap;
		this.nFreeRoll = nFreeRoll;
		
		this.cLground = cL0Landing + (cLalphaFlap*iw.getEstimatedValue());

		// Reference velocities definition
		vSLanding = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						theConditions.getAltitude().getEstimatedValue(),
						maxLandingMass.times(AtmosphereCalc.g0).getEstimatedValue(),
						aircraft.getWing().getSurface().getEstimatedValue(),
						cLmaxLanding
						),
				SI.METERS_PER_SECOND);
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

		// McCormick interpolated function --> See the excel file into JPAD DOCS
		double hb = wingToGroundDistance.divide(aircraft.getWing().getSpan().times(Math.PI/4)).getEstimatedValue();
		kGround = - 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
				+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055;

		// List initialization
		this.time = new ArrayList<Amount<Duration>>();
		this.speed = new ArrayList<Amount<Velocity>>();
		this.thrust = new ArrayList<Amount<Force>>();
		this.lift = new ArrayList<Amount<Force>>();
		this.loadFactor = new ArrayList<Double>();
		this.drag = new ArrayList<Amount<Force>>();
		this.friction = new ArrayList<Amount<Force>>();
		this.totalForce = new ArrayList<Amount<Force>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.landingDistance = new ArrayList<Amount<Length>>();
		this.verticalDistance = new ArrayList<Amount<Length>>();
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
		
		// DATA TO PLOT AT THE BEGINNING OF THE APPROACH PHASE
		landingDistance.add(Amount.valueOf(0.0, SI.METER));
		verticalDistance.add(obstacle);
		speed.add(vA);

		// DATA RELATED TO FLARE PHASE TO BE PLOT
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
	public void calculateGroundRollLandingODE(double phiRev) {

		this.phiRev = phiRev;
		
		initialize();
		
		System.out.println("---------------------------------------------------");
		System.out.println("CalcLanding :: Ground Roll ODE integration\n\n");

		FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
				1e-8,
				1,
				1e-8,
				1e-8
				);
		FirstOrderDifferentialEquations ode = new DynamicsEquationsLanding();

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
							
				//----------------------------------------------------------------------------------------
				// PICKING UP ALL DATA AT EVERY STEP 
				//----------------------------------------------------------------------------------------
				// TIME:
				LandingCalc.this.getTime().add(Amount.valueOf(t, SI.SECOND));
				//----------------------------------------------------------------------------------------
				// SPEED:
				LandingCalc.this.getSpeed().add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// THRUST:
				LandingCalc.this.getThrust().add(Amount.valueOf(
							-((DynamicsEquationsLanding)ode).thrust(x[1]),
							SI.NEWTON)
							);
				//--------------------------------------------------------------------------------
				// FRICTION:
				if(t < nFreeRoll.getEstimatedValue()) 
					LandingCalc.this.getFriction().add(Amount.valueOf(
							LandingCalc.this.getMu()
							*(((DynamicsEquationsLanding)ode).weight
									- ((DynamicsEquationsLanding)ode).lift(
											x[1])
									),
							SI.NEWTON)
							);
				else 
					LandingCalc.this.getFriction().add(Amount.valueOf(
							LandingCalc.this.getMuBrake()
							*(((DynamicsEquationsLanding)ode).weight
									- ((DynamicsEquationsLanding)ode).lift(
											x[1])
									),
							SI.NEWTON)
							);
				//----------------------------------------------------------------------------------------
				// LIFT:
				LandingCalc.this.getLift().add(Amount.valueOf(
						((DynamicsEquationsLanding)ode).lift(x[1]),
						SI.NEWTON)
						);
				//----------------------------------------------------------------------------------------
				// DRAG:
				LandingCalc.this.getDrag().add(Amount.valueOf(
						((DynamicsEquationsLanding)ode).drag(x[1]),
						SI.NEWTON)
						);
				//----------------------------------------------------------------------------------------
				// TOTAL FORCE:
				if(t < nFreeRoll.getEstimatedValue())
					LandingCalc.this.getTotalForce().add(Amount.valueOf(
							 - ((DynamicsEquationsLanding)ode).thrust(x[1])
							 - ((DynamicsEquationsLanding)ode).drag(x[1])
							 - LandingCalc.this.getMu()*(((DynamicsEquationsLanding)ode).weight
										- ((DynamicsEquationsLanding)ode).lift(x[1])),
								SI.NEWTON)
								);
				else
					LandingCalc.this.getTotalForce().add(Amount.valueOf(
							 -((DynamicsEquationsLanding)ode).thrust(x[1])
							 - ((DynamicsEquationsLanding)ode).drag(x[1])
							 - LandingCalc.this.getMuBrake()*(((DynamicsEquationsLanding)ode).weight
										- ((DynamicsEquationsLanding)ode).lift(x[1])),
								SI.NEWTON)
								);
				//----------------------------------------------------------------------------------------
				// LOAD FACTOR:
				LandingCalc.this.getLoadFactor().add(
						((DynamicsEquationsLanding)ode).lift(x[1])
						/(((DynamicsEquationsLanding)ode).weight));
				//----------------------------------------------------------------------------------------
				// ACCELERATION:
				if(t < nFreeRoll.getEstimatedValue())
					LandingCalc.this.getAcceleration().add(	
							Amount.valueOf((AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsLanding)ode).weight)
									*(- ((DynamicsEquationsLanding)ode).thrust(x[1])
											- ((DynamicsEquationsLanding)ode).drag(x[1])
											- LandingCalc.this.getMu()*(((DynamicsEquationsLanding)ode).weight
													- ((DynamicsEquationsLanding)ode).lift(x[1]))),
									SI.METERS_PER_SQUARE_SECOND)
							);
				else
					LandingCalc.this.getAcceleration().add(	
							Amount.valueOf((AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsLanding)ode).weight)
									*(- ((DynamicsEquationsLanding)ode).thrust(x[1])
											- ((DynamicsEquationsLanding)ode).drag(x[1])
											- LandingCalc.this.getMuBrake()*(((DynamicsEquationsLanding)ode).weight
													- ((DynamicsEquationsLanding)ode).lift(x[1]))),
									SI.METERS_PER_SQUARE_SECOND)
							);
				
				//----------------------------------------------------------------------------------------
				// LANDING DISTANCE:
				LandingCalc.this.getLandingDistance().add(Amount.valueOf(x[0],
						SI.METER)
						);
				
				//----------------------------------------------------------------------------------------
				// LANDING DISTANCE:
				LandingCalc.this.getVerticalDistance().add(Amount.valueOf(0.0, SI.METER));
				
				//----------------------------------------------------------------------------------------
			}
		};
		theIntegrator.addStepHandler(stepHandler);
		
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

		// data setup
		double[] groundDistance = new double[getLandingDistance().size()];
		for(int i=0; i<groundDistance.length; i++)
			groundDistance[i] = getLandingDistance().get(i).getEstimatedValue();
		
		double[] groundRollDistance = new double[getLandingDistance().size()-11];
		for(int i=0; i<groundRollDistance.length; i++)
			groundRollDistance[i] = groundDistance[i+11];

		double[] verticalDistance = new double[getVerticalDistance().size()];
		for(int i=0; i<verticalDistance.length; i++)
			verticalDistance[i] = getVerticalDistance().get(i).getEstimatedValue();
		
		double[] thrust = new double[getThrust().size()];
		for(int i=0; i<thrust.length; i++)
			thrust[i] = getThrust().get(i).getEstimatedValue();

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

		double[] weight = new double[getTime().size()];
		for(int i=0; i<weight.length; i++)
			weight[i] = maxLandingMass.times(AtmosphereCalc.g0).getEstimatedValue();

		// landing trajectory and speed
		double[][] xMatrix1 = new double[2][groundDistance.length];
		for(int i=0; i<xMatrix1.length; i++)
			xMatrix1[i] = groundDistance;

		double[][] yMatrix1 = new double[2][groundDistance.length];
		yMatrix1[0] = verticalDistance;
		yMatrix1[1] = speed;

		MyChartToFileUtils.plot(
				xMatrix1, yMatrix1,
				0.0, null, -1.0, null,
				"Ground Distance", "", "m", "",
				new String[] {"Landing Trajectory", "Speed (m/s)"},
				landingFolderPath, "TrajectoryAndSpeed_vs_GroundDistance");
		
		// acceleration v.s. ground roll distance
		MyChartToFileUtils.plotNoLegend(
				groundRollDistance, acceleration,
				sApproach.plus(sFlare).getEstimatedValue(), null, null, null,
				"Ground Roll Distance", "Acceleration", "m", "m/(s^2)",
				landingFolderPath, "Acceleration_vs_GroundDistance");

		// load factor v.s. ground roll distance
		MyChartToFileUtils.plotNoLegend(
				groundRollDistance, loadFactor,
				sApproach.plus(sFlare).getEstimatedValue(), null, 0.0, null,
				"Ground Roll distance", "Load Factor", "m", "",
				landingFolderPath, "LoadFactor_vs_GroundDistance");

		// Horizontal Forces v.s. ground roll distance
		double[][] xMatrix2 = new double[4][totalForce.length];
		for(int i=0; i<xMatrix2.length; i++)
			xMatrix2[i] = groundRollDistance;

		double[][] yMatrix2 = new double[4][totalForce.length];
		yMatrix2[0] = totalForce;
		yMatrix2[1] = thrust;
		yMatrix2[2] = drag;
		yMatrix2[3] = friction;

		MyChartToFileUtils.plot(
				xMatrix2, yMatrix2,
				sApproach.plus(sFlare).getEstimatedValue(), null, null, null,
				"Ground Roll Distance", "Horizontal Forces", "m", "N",
				new String[] {"Total Force", "Thrust", "Drag", "Friction"},
				landingFolderPath, "HorizontalForces_vs_GroundDistance");

		// Vertical Forces v.s. ground roll distance
		double[][] xMatrix3 = new double[2][totalForce.length];
		for(int i=0; i<xMatrix3.length; i++)
			xMatrix3[i] = groundRollDistance;

		double[][] yMatrix3 = new double[2][totalForce.length];
		yMatrix3[0] = lift;
		yMatrix3[1] = weight;

		MyChartToFileUtils.plot(
				xMatrix3, yMatrix3,
				sApproach.plus(sFlare).getEstimatedValue(), null, null, null,
				"Ground Roll distance", "Vertical Forces", "m", "N",
				new String[] {"Lift", "Weight"},
				landingFolderPath, "VerticalForces_vs_GroundDistance");
		
		System.out.println("\n---------------------------DONE!-------------------------------");
	}
	
	/**************************************************************************************
	 * This method perform the calculation of the total landing distance by calling the 
	 * previous methods
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateLandingDistance(double phiRev) {
		calculateAirborneDistance();
		calculateGroundRollLandingODE(phiRev);
	}
	
	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsLanding implements FirstOrderDifferentialEquations {

		double weight, g0, mu, muBrake, cD0, deltaCD0, oswald, ar, kGround, cLground, vWind, alphaGround;

		public DynamicsEquationsLanding() {

			// constants and known values
			weight = maxLandingMass.times(AtmosphereCalc.g0).getEstimatedValue();
			g0 = AtmosphereCalc.g0.getEstimatedValue();
			mu = LandingCalc.this.mu;
			muBrake = LandingCalc.this.muBrake;
			cD0 = LandingCalc.this.getcD0();
			deltaCD0 = LandingCalc.this.getDeltaCD0FlapLandinGearsSpoilers();
			oswald = LandingCalc.this.getOswald();
			ar = aircraft.getWing().getAspectRatio();
			kGround = LandingCalc.this.getkGround();
			cLground = LandingCalc.this.getcLground();
			vWind = LandingCalc.this.getvWind().getEstimatedValue();
			alphaGround = LandingCalc.this.getAlphaGround().getEstimatedValue();
		}

		@Override
		public int getDimension() {
			return 2;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			double speed = x[1];

			if(t < LandingCalc.this.getnFreeRoll().getEstimatedValue()) {
				xDot[0] = speed;
				xDot[1] = (g0/weight)*(-thrust(speed) - drag(speed)
						- (mu*(weight - lift(speed))));
			}
			else {
				xDot[0] = speed;
				xDot[1] = (g0/weight)*(-thrust(speed) - drag(speed)
						- (muBrake*(weight - lift(speed))));
			}
		}

		public double thrust(double speed) {

			double theThrust = 0.0;

			theThrust =	ThrustCalc.calculateThrustDatabase(
					LandingCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getT0().getEstimatedValue(),
					LandingCalc.this.getAircraft().getPowerPlant().getEngineNumber(),
					LandingCalc.this.getPhiRev(),
					LandingCalc.this.getAircraft().getPowerPlant().getEngineList().get(0).getBPR(),
					LandingCalc.this.getAircraft().getPowerPlant().getEngineType(),
					EngineOperatingConditionEnum.TAKE_OFF,
					LandingCalc.this.getTheConditions().getAltitude().getEstimatedValue(),
					SpeedCalc.calculateMach(
							LandingCalc.this.getTheConditions().getAltitude().getEstimatedValue(),
							speed + 
							LandingCalc.this.getvWind().getEstimatedValue()
							)
					);

			return theThrust;
		}

		public double drag(double speed) {

			double cD = MyMathUtils
					.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(
									aircraft.getTheAnalysisManager().getThePerformance().getPolarCLLanding()
									),
							MyArrayUtils.convertToDoublePrimitive(
									aircraft.getTheAnalysisManager().getThePerformance().getPolarCDLanding()
									),
							LandingCalc.this.getcLground()
							);

			return 	0.5
					*aircraft.getWing().getSurface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.getAltitude().getEstimatedValue())
					*(Math.pow((speed + vWind), 2))
					*cD;
		}

		public double lift(double speed) {

			return 	0.5
					*aircraft.getWing().getSurface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.getAltitude().getEstimatedValue())
					*(Math.pow((speed + vWind), 2))
					*LandingCalc.this.getcLground();
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
	public CalcHighLiftDevices getHighLiftCalculator() {
		return highLiftCalculator;
	}
	public void setHighLiftCalculator(CalcHighLiftDevices highLiftCalculator) {
		this.highLiftCalculator = highLiftCalculator;
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
	public double getPhiRev() {
		return phiRev;
	}
	public void setPhiRev(double phiRev) {
		this.phiRev = phiRev;
	}
	public double getOswald() {
		return oswald;
	}
	public void setOswald(double oswald) {
		this.oswald = oswald;
	}
	public double getcD0() {
		return cD0;
	}
	public void setcD0(double cD0) {
		this.cD0 = cD0;
	}
	public double getcLalphaFlap() {
		return cLalphaFlap;
	}
	public void setcLalphaFlap(double cLalphaFlap) {
		this.cLalphaFlap = cLalphaFlap;
	}
	public double getDeltaCD0FlapLandinGearsSpoilers() {
		return deltaCD0FlapLandinGearsSpoilers;
	}
	public void setDeltaCD0FlapLandinGearsSpoliers(double deltaCD0FlapLandinGearsSpoilers) {
		this.deltaCD0FlapLandinGearsSpoilers = deltaCD0FlapLandinGearsSpoilers;
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

	public double getDeltaCD0LandignGear() {
		return deltaCD0LandignGear;
	}

	public void setDeltaCD0LandignGear(double deltaCD0LandignGear) {
		this.deltaCD0LandignGear = deltaCD0LandignGear;
	}

	public double getDeltaCD0Spoiler() {
		return deltaCD0Spoiler;
	}

	public void setDeltaCD0Spoiler(double deltaCD0Spoiler) {
		this.deltaCD0Spoiler = deltaCD0Spoiler;
	}

	public Amount<Mass> getMaxLandingMass() {
		return maxLandingMass;
	}

	public void setMaxLandingMass(Amount<Mass> maxLandingMass) {
		this.maxLandingMass = maxLandingMass;
	}
}