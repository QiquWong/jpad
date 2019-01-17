package calculators.performance;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
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
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

/**
 * This class have the purpose of calculating the landing trajectories for the noise certification
 * of a given aircraft assuming:
 *
 * - 3° of glide path
 * - V= 1.3*VsLND + 10kts
 * - full flaps configuration and landing gear down
 * - Maximum landing weight 
 * - ISA+10°C
 *
 * for each of them a step by step integration is used in solving the dynamic equation.
 *
 * @author Vittorio Trifari
 *
 */

public class LandingNoiseTrajectoryCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private double aspectRatio;
	private Amount<Area> surface; 
	private Amount<Length> span;
	private PowerPlant thePowerPlant;
	private double[] polarCLLanding;
	private double[] polarCDLanding;
	private Amount<Duration> dtFlare, dtFreeRoll,
	tObstacle = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tTouchDown = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tZeroGamma = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
	private Amount<Mass> maxLandingMass; 
	private Amount<Velocity> vSLanding, vApproach, vTouchDown, vWind, vDescent;
	private Amount<Length> wingToGroundDistance, obstacle, intialAltitude, altitudeAtFlareEnding;
	private Amount<Angle> gammaDescent, iw, alphaGround, gammaAtFlareEnding;
	private Amount<Force> thrustAtFlareStart;
	private List<Amount<Angle>> alpha;
	private List<Double> gammaDot;
	private List<Amount<Duration>> time;
	private List<Amount<Force>> thrust;
	private List<Double> timeBreakPoints;
	private double alphaDotFlare, cL0LND, cLmaxLND, kGround, phi, 
	cruiseThrustCorrectionFactor, fidlThrustCorrectionFactor, gidlThrustCorrectionFactor, 
	cruiseSfcCorrectionFactor, fidlSfcCorrectionFactor, gidlSfcCorrectionFactor;
	private Amount<?> cLalphaLND;
	private MyInterpolatingFunction mu, muBrake, thrustFlareFunction;
	private boolean createCSV;

	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;

	//OUTPUT:
	private List<Amount<Velocity>> speedList, rateOfClimbList;
	private List<Amount<Force>> thrustList, thrustHorizontalList, thrustVerticalList,
	liftList, dragList, totalForceList, frictionList;
	private List<Amount<Angle>> alphaList, gammaList, thetaList;
	private List<Double> alphaDotList, gammaDotList, cLList, loadFactorList, cDList;
	private List<Amount<Acceleration>> accelerationList;
	private List<Amount<Length>> groundDistanceList, verticalDistanceList;
	private List<Amount<Duration>> timeList;
	private List<Amount<Mass>> fuelUsedList;
	private List<Amount<Force>> weightList;

	private final PrintStream originalOut = System.out;
	private PrintStream filterStream = new PrintStream(new OutputStream() {
		public void write(int b) {
			// write nothing
		}
	});

	//-------------------------------------------------------------------------------------
	// BUILDER:

	public LandingNoiseTrajectoryCalc(
			Amount<Length> initialAltitude,
			Amount<Angle> gammaDescent,
			Amount<Mass> maxLandingMass,
			PowerPlant thePowerPlant,
			double[] polarCLLanding,
			double[] polarCDLanding,
			double aspectRatio,
			Amount<Area> surface,
			Amount<Duration> dtFreeRoll,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Angle> iw,
			Amount<Length> wingToGroundDistance,
			double cLmaxLND,
			double cLZeroLND,
			Amount<?> cLalphaLND,
			double phi,
			double cruiseThrustCorrectionFactor,
			double fidlThrustCorrectionFactor,
			double gidlThrustCorrectionFactor,
			double cruiseSfcCorrectionFactor,
			double fidlSfcCorrectionFactor,
			double gidlSfcCorrectionFactor,
			boolean createCSV
			) {

		this.createCSV = createCSV;

		// Required data
		this.aspectRatio = aspectRatio;
		this.surface = surface;
		this.span = Amount.valueOf(
				Math.sqrt(aspectRatio*surface.doubleValue(SI.SQUARE_METRE)),
				SI.METER
				);
		this.thePowerPlant = thePowerPlant;
		this.polarCLLanding = polarCLLanding;
		this.polarCDLanding = polarCDLanding;
		this.maxLandingMass = maxLandingMass;
		this.intialAltitude = initialAltitude;
		this.gammaDescent = gammaDescent;
		this.dtFreeRoll = dtFreeRoll;
		this.mu = mu;
		this.muBrake = muBrake;
		this.obstacle = Amount.valueOf(50, NonSI.FOOT);
		this.wingToGroundDistance = wingToGroundDistance;
		this.vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		this.iw = iw;
		this.alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		this.cLmaxLND = cLmaxLND;
		this.cLalphaLND = cLalphaLND;
		this.cL0LND = cLZeroLND;
		this.phi = phi;
		this.cruiseThrustCorrectionFactor = cruiseThrustCorrectionFactor;
		this.fidlThrustCorrectionFactor = fidlThrustCorrectionFactor;
		this.gidlThrustCorrectionFactor = gidlThrustCorrectionFactor;
		this.cruiseSfcCorrectionFactor = cruiseSfcCorrectionFactor;
		this.fidlSfcCorrectionFactor = fidlSfcCorrectionFactor;
		this.gidlSfcCorrectionFactor = gidlSfcCorrectionFactor;

		// Reference velocities definition
		vSLanding = SpeedCalc.calculateSpeedStall(
				Amount.valueOf(0.0, SI.METER), // SEA LEVEL
				Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
				maxLandingMass,
				surface,
				cLmaxLND
				);
		vApproach = vSLanding.times(1.23);
		vDescent = vApproach.plus(Amount.valueOf(10, NonSI.KNOT).to(SI.METERS_PER_SECOND)); 
		vTouchDown = vSLanding.times(1.09);

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxLND = " + cLmaxLND);
		System.out.println("CL0 = " + cLZeroLND);
		System.out.println("VsLND = " + vSLanding);
		System.out.println("VTouchDown = " + vTouchDown);
		System.out.println("VApproach = " + vApproach);
		System.out.println("VDescent = " + vDescent);
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.time = new ArrayList<Amount<Duration>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.timeBreakPoints = new ArrayList<Double>();
		this.gammaDot = new ArrayList<Double>();
		this.thrust = new ArrayList<Amount<Force>>();

		// Output maps initialization
		this.timeList = new ArrayList<>();
		this.speedList = new ArrayList<>();
		this.thrustList = new ArrayList<>();
		this.thrustHorizontalList = new ArrayList<>();
		this.thrustVerticalList = new ArrayList<>();
		this.alphaList = new ArrayList<>();
		this.alphaDotList = new ArrayList<>();
		this.gammaList = new ArrayList<>();
		this.gammaDotList = new ArrayList<>();
		this.thetaList = new ArrayList<>();
		this.cLList = new ArrayList<>();
		this.liftList = new ArrayList<>();
		this.loadFactorList = new ArrayList<>();
		this.cDList = new ArrayList<>();
		this.dragList = new ArrayList<>();
		this.frictionList = new ArrayList<>();
		this.totalForceList = new ArrayList<>();
		this.accelerationList = new ArrayList<>();
		this.rateOfClimbList = new ArrayList<>();
		this.groundDistanceList = new ArrayList<>();
		this.verticalDistanceList = new ArrayList<>();
		this.fuelUsedList = new ArrayList<>();
		this.weightList = new ArrayList<>();

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
		alpha.clear();
		timeBreakPoints.clear();
		gammaDot.clear();
		thrust.clear();

		// values initialization
		double cLInitial = LiftCalc.calculateLiftCoeff(
				Amount.valueOf(
						maxLandingMass.doubleValue(SI.KILOGRAM)
						*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)
						*Math.cos(gammaDescent.doubleValue(SI.RADIAN)),
						SI.NEWTON
						),
				vDescent,
				LandingNoiseTrajectoryCalc.this.getSurface(),
				intialAltitude,
				Amount.valueOf(10, SI.CELSIUS) // ISA+10°C
				); 
		alpha.add(
				Amount.valueOf(
						((cLInitial - cL0LND)
								/ cLalphaLND.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()),
						NonSI.DEGREE_ANGLE
						)
				);
		gammaDot.add(0.0);
		gammaAtFlareEnding = Amount.valueOf(10000, NonSI.DEGREE_ANGLE);	// initialization to an impossible time
		time.add(Amount.valueOf(0.0, SI.SECOND));

		tObstacle = Amount.valueOf(10000.0, SI.SECOND);		// initialization to an impossible time
		tTouchDown = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
		tZeroGamma = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
	}

	/***************************************************************************************
	 * This method performs the integration of the equation of motion by solving a set of
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateNoiseLandingTrajectory(boolean timeHistories) {

		System.out.println("---------------------------------------------------");
		System.out.println("NoiseTrajectoryCalc :: LANDING ODE integration\n\n");
		System.out.println("\tRUNNING SIMULATION ...\n\n");


		int i=0;
		int maxIter = 5;
		Amount<Duration> newFlareDuration = Amount.valueOf(0.0, SI.SECOND);
		dtFlare = Amount.valueOf(3.0, SI.SECOND); // First guess value

		List<Amount<Length>> residualAltitudeList = new ArrayList<>();
		residualAltitudeList.add(obstacle);
		List<Amount<Duration>> dtFlareDuration = new ArrayList<>();
		dtFlareDuration.add(Amount.valueOf(0.0, SI.SECOND));
		dtFlareDuration.add(dtFlare);
		
		altitudeAtFlareEnding = intialAltitude;  // Initialization at an impossible value
		gammaAtFlareEnding = Amount.valueOf(10000, NonSI.DEGREE_ANGLE);	// Initialization to an impossible time

		while (Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER)) >= 1.0 
				|| Math.abs(gammaAtFlareEnding.doubleValue(NonSI.DEGREE_ANGLE)) > 1e-3) {

			if(i > 0) { 
				dtFlare = newFlareDuration;
				dtFlareDuration.add(dtFlare);
			}
			
			initialize();

			theIntegrator = new HighamHall54Integrator(
					1e-10,
					1,
					1e-3,
					1e-3
					);
			ode = new DynamicsEquationsLandingNoiseTrajectory();

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
					System.out.println("\n\t\tEND OF GROUND ROLL PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					timeBreakPoints.add(t);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
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
					if(t < tObstacle.doubleValue(SI.SECOND))
						return x[3] - obstacle.doubleValue(SI.METER);
					else
						return -10.0; /* Generic negative value to trigger the event only one time */

				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF DESCENT PHASE :: FLARE ROTATION");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					Amount<Temperature> deltaTemperature = Amount.valueOf(10, SI.CELSIUS); // ISA+10°C
					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf(
							(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsLandingNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);

					tObstacle = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					thrustAtFlareStart = 
							Amount.valueOf( 
									((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(
											speed,
											time,
											alpha,
											gamma,
											altitude,
											deltaTemperature, 
											weight
											).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
									SI.NEWTON
									);
					Amount<Force> thrustAtGammaZero = ((DynamicsEquationsLandingNoiseTrajectory)ode).drag(
							vTouchDown, 
							Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), 
							Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), 
							Amount.valueOf(0.0, SI.METER), 
							deltaTemperature
							);

					thrustFlareFunction = new MyInterpolatingFunction();
					thrustFlareFunction.interpolateLinear(
							new double[] {
									tObstacle.doubleValue(SI.SECOND),
									tObstacle.plus(dtFlare).doubleValue(SI.SECOND) 
							},
							new double[] {
									thrustAtFlareStart.doubleValue(SI.NEWTON),
									thrustAtGammaZero.doubleValue(SI.NEWTON)
							}
							);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.CONTINUE;
				}
			};
			EventHandler ehCheckTouchDown = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					if(t < tTouchDown.doubleValue(SI.SECOND))
						return x[3] - 1.0;
					else
						return -10.0; /* Generic negative value to trigger the event only one time */
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF FLARE PHASE :: TOUCH-DOWN");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);
					
					if(x[2] < 0.0) {
						altitudeAtFlareEnding = Amount.valueOf(x[3], SI.METER);
						gammaAtFlareEnding = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
						residualAltitudeList.add(altitudeAtFlareEnding);
						System.out.println("\nAltitude @ Flare Ending = " + altitudeAtFlareEnding);
						System.out.println("Gamma @ Flare Ending = " + gammaAtFlareEnding);
						System.out.println("\nFlare duration = " + dtFlare);
					}
						
					tTouchDown = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};
			EventHandler ehCheckZeroGamma = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					if(t < tZeroGamma.doubleValue(SI.SECOND))
						return x[2] - 0.0;
					else
						return 10.0; /* Generic positive value to trigger the event only one time */
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tGAMMA = 0.0 DURING FLARE ROTATION");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					tZeroGamma = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					gammaAtFlareEnding = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					altitudeAtFlareEnding = Amount.valueOf(x[3], SI.METER);
					residualAltitudeList.add(altitudeAtFlareEnding);
					System.out.println("\nAltitude @ Flare Ending = " + altitudeAtFlareEnding);
					System.out.println("Gamma @ Flare Ending = " + gammaAtFlareEnding);
					System.out.println("\nFlare duration = " + dtFlare);

					System.out.println("\n---------------------------DONE!-------------------------------");
					Action action = Action.CONTINUE;
					if( Math.abs(gammaAtFlareEnding.doubleValue(NonSI.DEGREE_ANGLE)) > 1e-3
							|| Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER)) >= 1.0 )
						action = Action.STOP;
					
					return  action;
				}
			};

			theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckZeroGamma, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckTouchDown, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-3, 20);

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] xDot = interpolator.getInterpolatedDerivatives();
					double[] x = interpolator.getInterpolatedState();			

					Amount<Temperature> deltaTemperature = Amount.valueOf(10, SI.CELSIUS); // ISA+10°C
					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf(
							(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsLandingNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);

					//----------------------------------------------------------------------------------------
					// TIME:
					LandingNoiseTrajectoryCalc.this.getTime().add(Amount.valueOf(t, SI.SECOND));
					//----------------------------------------------------------------------------------------
					// ALPHA:
					LandingNoiseTrajectoryCalc.this.getAlpha().add(((DynamicsEquationsLandingNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight));
					//----------------------------------------------------------------------------------------
					// GAMMA_DOT:
					LandingNoiseTrajectoryCalc.this.getGammaDot().add(xDot[2]);
					//----------------------------------------------------------------------------------------
					// THRUST:
					LandingNoiseTrajectoryCalc.this.getThrust().add(
							Amount.valueOf(
									((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
									.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(), 
									SI.NEWTON)
							);

				}
			};
			theIntegrator.addStepHandler(stepHandler);

			//##############################################################################################
			// Use this handler for post-processing
			theIntegrator.addStepHandler(new ContinuousOutputModel());
			System.out.println("=================================================");
			System.out.println("Integration #" + (i+1) + "\n\n");
			//##############################################################################################

			// initial state
			double[] xAt0 = new double[] {
					0.0,
					vDescent.doubleValue(SI.METERS_PER_SECOND),
					gammaDescent.doubleValue(NonSI.DEGREE_ANGLE),
					intialAltitude.doubleValue(SI.METER),
					0.0
			}; 
			theIntegrator.integrate(ode, 0.0, xAt0, 10000, xAt0); // now xAt0 contains final state

			i++;
			if(altitudeAtFlareEnding.doubleValue(SI.METER) > 2.0)
				newFlareDuration = Amount.valueOf( 
						-residualAltitudeList.get(0).doubleValue(SI.METER)
						/( (residualAltitudeList.get(i).doubleValue(SI.METER) - residualAltitudeList.get(i-1).doubleValue(SI.METER)) 
								/ (dtFlareDuration.get(i).doubleValue(SI.SECOND) - dtFlareDuration.get(i-1).doubleValue(SI.SECOND)) ),
						SI.SECOND
						);
			else {
				if(altitudeAtFlareEnding.doubleValue(SI.METER) > 0.0)
					newFlareDuration = dtFlare.to(SI.SECOND).plus(Amount.valueOf(0.01, SI.SECOND));
				else
					newFlareDuration = dtFlare.to(SI.SECOND).minus(Amount.valueOf(0.01, SI.SECOND));
			}

			if(Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER)) <= 1.0
					&& Math.abs(gammaAtFlareEnding.doubleValue(NonSI.DEGREE_ANGLE)) <= 1e-3)
				manageOutputData(1.0, timeHistories);
			
			if(i > maxIter) {
				manageOutputData(1.0, timeHistories);
				break;
			}
			
			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

		}

		
		System.out.println("\n---------------------------END!!-------------------------------\n\n");
	}

	/********************************************************************************************
	 * This method allows users to fill all the maps of results related to each throttle setting.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void manageOutputData(double dt, boolean timeHistories) {

		//#############################################################################
		// Collect the array of times and associated state vector values according
		// to the given dt and keeping the the discrete event-times (breakpoints)

		List<double[]> states = new ArrayList<double[]>();
		List<double[]> stateDerivatives = new ArrayList<double[]>();
		for (  StepHandler handler : this.theIntegrator.getStepHandlers() ) {

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
					timeList.add(Amount.valueOf(t, SI.SECOND));
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
				for(int i = 0; i < timeList.size(); i++) {

					double[] x = states.get(i);
					double[] xDot = stateDerivatives.get(i);

					Amount<Temperature> deltaTemperature = Amount.valueOf(10, SI.CELSIUS); // ISA+10°C
					Amount<Duration> time = timeList.get(i);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf(
							(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsLandingNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);
					
					//========================================================================================
					// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					// GROUND DISTANCE:
					groundDistanceList.add(Amount.valueOf(
							x[0],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// VERTICAL DISTANCE:
					verticalDistanceList.add(Amount.valueOf(
							x[3],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// THRUST:
					thrustList.add(Amount.valueOf(
							((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
							.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON)
							);
					//--------------------------------------------------------------------------------
					// FUEL USED (kg/s):
					fuelUsedList.add(Amount.valueOf(x[4], SI.KILOGRAM));
					//----------------------------------------------------------------------------------------
					if(timeHistories) {
						//----------------------------------------------------------------------------------------
						// WEIGHT:
						weightList.add(weight);
						//----------------------------------------------------------------------------------------
						// SPEED:
						speedList.add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
						//----------------------------------------------------------------------------------------
						// THRUST HORIZONTAL:
						thrustHorizontalList.add(Amount.valueOf(
								((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
								.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.cos(alpha.doubleValue(SI.RADIAN)),
								SI.NEWTON)
								);
						//----------------------------------------------------------------------------------------
						// THRUST VERTICAL:
						thrustVerticalList.add(Amount.valueOf(
								((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
								.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.sin(alpha.doubleValue(SI.RADIAN)),
								SI.NEWTON)
								);
						//--------------------------------------------------------------------------------
						// FRICTION:
						if(timeList.get(i).doubleValue(SI.SECOND) >= tTouchDown.doubleValue(SI.SECOND))
							frictionList.add(Amount.valueOf(
									((DynamicsEquationsLandingNoiseTrajectory)ode).mu(speed)
									*(weight.doubleValue(SI.NEWTON)
											- ((DynamicsEquationsLandingNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
											),
									SI.NEWTON)
									);
						else if(timeList.get(i).doubleValue(SI.SECOND) >= 
								tTouchDown.to(SI.SECOND).plus(dtFreeRoll.to(SI.SECOND)).doubleValue(SI.SECOND)
								)
							frictionList.add(Amount.valueOf(
									((DynamicsEquationsLandingNoiseTrajectory)ode).muBrake(speed)
									*(weight.doubleValue(SI.NEWTON)
											- ((DynamicsEquationsLandingNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
											),
									SI.NEWTON)
									);
						else
							frictionList.add(Amount.valueOf(0.0, SI.NEWTON));
						//----------------------------------------------------------------------------------------
						// LIFT:
						liftList.add(((DynamicsEquationsLandingNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature));
						//----------------------------------------------------------------------------------------
						// DRAG:
						dragList.add(((DynamicsEquationsLandingNoiseTrajectory)ode).drag(speed, alpha, gamma, altitude, deltaTemperature));
						//----------------------------------------------------------------------------------------
						// TOTAL FORCE:
						totalForceList.add(Amount.valueOf(
								(((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
										.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
										*Math.cos(alpha.doubleValue(SI.RADIAN))
										)
								- ((DynamicsEquationsLandingNoiseTrajectory)ode).drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
								- (((DynamicsEquationsLandingNoiseTrajectory)ode).mu(speed)
										*(weight.doubleValue(SI.NEWTON)
												- ((DynamicsEquationsLandingNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
												)
										)
								- (weight.doubleValue(SI.NEWTON)*Math.sin(gamma.doubleValue(SI.RADIAN))
										),
								SI.NEWTON)
								);
						//----------------------------------------------------------------------------------------
						// LOAD FACTOR:
						loadFactorList.add(
								(((DynamicsEquationsLandingNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
										+ (((DynamicsEquationsLandingNoiseTrajectory)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
												.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
												*Math.sin(alpha.doubleValue(SI.RADIAN))
												)
										)
								/(weight.doubleValue(SI.NEWTON)
										*Math.cos(gamma.doubleValue(SI.RADIAN))
										)
								);
						//----------------------------------------------------------------------------------------
						// RATE OF CLIMB:
						rateOfClimbList.add(Amount.valueOf(
								xDot[3],
								SI.METERS_PER_SECOND)
								);
						//----------------------------------------------------------------------------------------
						// ACCELERATION:
						accelerationList.add(Amount.valueOf(xDot[1], SI.METERS_PER_SQUARE_SECOND));
						//----------------------------------------------------------------------------------------
						// ALPHA:
						alphaList.add(alpha);
						//----------------------------------------------------------------------------------------
						// GAMMA:
						gammaList.add(gamma);
						//----------------------------------------------------------------------------------------
						// ALPHA DOT:
						if(timeList.get(i).doubleValue(SI.SECOND) > tObstacle.doubleValue(SI.SECOND)) {
							double deltaAlpha = 
									alphaList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
									- alphaList.get(i-1).doubleValue(NonSI.DEGREE_ANGLE);
							double deltaTime = 
									timeList.get(i).doubleValue(SI.SECOND)
									- timeList.get(i-1).doubleValue(SI.SECOND);
							alphaDotList.add(deltaAlpha/deltaTime);
						}
						else
							alphaDotList.add(0.0);
						//----------------------------------------------------------------------------------------
						// GAMMA DOT:
						gammaDotList.add(xDot[2]);
						//----------------------------------------------------------------------------------------
						// THETA:
						thetaList.add(Amount.valueOf(
								alpha.doubleValue(NonSI.DEGREE_ANGLE) + gamma.doubleValue(NonSI.DEGREE_ANGLE),
								NonSI.DEGREE_ANGLE)
								);
						//----------------------------------------------------------------------------------------
						// CL:				
						cLList.add(((DynamicsEquationsLandingNoiseTrajectory)ode).cL(alpha));
						//----------------------------------------------------------------------------------------
						// CD:
						cDList.add(
								((DynamicsEquationsLandingNoiseTrajectory)ode).cD(
										((DynamicsEquationsLandingNoiseTrajectory)ode).cL(alpha),
										altitude
										)
								);

						//----------------------------------------------------------------------------------------
					}
				}
			}
		}
	}

	/**************************************************************************************
	 * This method allows users to plot all simulation results producing several output charts
	 * which have time as independent variables.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco, Vittorio Trifari
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createOutputCharts(String outputFolderPath, boolean timeHistories) throws InstantiationException, IllegalAccessException {

		if(timeHistories) {
			String simulationDetailsOutputFolder = JPADStaticWriteUtils.createNewFolder(
					outputFolderPath + ("SIMULATION_DETAILS") + File.separator
					);

			System.setOut(originalOut);
			System.out.println("\tPRINTING SIMULATION DETAILS CHARTS TO FILE ...");
			System.setOut(filterStream);

			//.................................................................................
			// speed v.s. time

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(speedList),
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "m/s",
					simulationDetailsOutputFolder, "Speed_evolution_SI",true);



			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							speedList.stream()
							.map(x -> x.to(NonSI.KNOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "kn",
					simulationDetailsOutputFolder, "Speed_evolution_IMPERIAL",true);

			//.................................................................................
			// speed v.s. ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
					MyArrayUtils.convertListOfAmountTodoubleArray(speedList),
					0.0, null, 0.0, null,
					"Ground Distance", "Speed", "m", "m/s",
					simulationDetailsOutputFolder, "Speed_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							speedList.stream()
							.map(x -> x.to(NonSI.KNOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Ground Distance", "Speed", "ft", "kn",
					simulationDetailsOutputFolder, "Speed_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// acceleration v.s. time

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
					0.0, null, -10.0, 10.0,
					"Time", "Acceleration", "s", "m/(s^2)",
					simulationDetailsOutputFolder, "Acceleration_evolution_SI",true);


			//.................................................................................
			// acceleration v.s. ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
					MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
					0.0, null, -10.0, 10.0,
					"Ground Distance", "Acceleration", "m", "m/(s^2)",
					simulationDetailsOutputFolder, "Acceleration_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
					0.0, null, -10.0, 10.0,
					"Ground Distance", "Acceleration", "ft", "m/(s^2)",
					simulationDetailsOutputFolder, "Acceleration_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// load factor v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertToDoublePrimitive(loadFactorList),
					0.0, null, 0.0, null,
					"Time", "Load Factor", "s", "",
					simulationDetailsOutputFolder, "LoadFactor_evolution",true);

			//.................................................................................
			// load factor v.s. ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList), 
					MyArrayUtils.convertToDoublePrimitive(loadFactorList),
					0.0, null, 0.0, null,
					"Ground distance", "Load Factor", "m", "",
					simulationDetailsOutputFolder, "LoadFactor_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertToDoublePrimitive(loadFactorList),
					0.0, null, 0.0, null,
					"Ground distance", "Load Factor", "ft", "",
					simulationDetailsOutputFolder, "LoadFactor_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// Rate of Climb v.s. Time

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbList),
					0.0, null, null, 0.0,
					"Time", "Rate of Climb", "s", "m/s",
					simulationDetailsOutputFolder, "RateOfClimb_evolution_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimbList.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, null, 0.0,
					"Time", "Rate of Climb", "s", "ft/min",
					simulationDetailsOutputFolder, "RateOfClimb_evolution_IMPERIAL",true);

			//.................................................................................
			// Rate of Climb v.s. Ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList), 
					MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbList),
					0.0, null, null, 0.0,
					"Ground distance", "Rate of Climb", "m", "m/s",
					simulationDetailsOutputFolder, "RateOfClimb_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertListOfAmountTodoubleArray(
							rateOfClimbList.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, null, 0.0,
					"Ground distance", "Rate of Climb", "ft", "ft/min",
					simulationDetailsOutputFolder, "RateOfClimb_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// CL v.s. Time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertToDoublePrimitive(cLList),
					0.0, null, 0.0, null,
					"Time", "CL", "s", "",
					simulationDetailsOutputFolder, "CL_evolution",true);

			//.................................................................................
			// CL v.s. Ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
					MyArrayUtils.convertToDoublePrimitive(cLList),
					0.0, null, 0.0, null,
					"Ground distance", "CL", "m", "",
					simulationDetailsOutputFolder, "CL_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(cLList),
					0.0, null, 0.0, null,
					"Ground distance", "CL", "ft", "",
					simulationDetailsOutputFolder, "CL_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// CD v.s. Time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
					MyArrayUtils.convertToDoublePrimitive(cDList),
					0.0, null, 0.0, null,
					"Time", "CD", "s", "",
					simulationDetailsOutputFolder, "CD_evolution",true);

			//.................................................................................
			// CD v.s. Ground distance

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
					MyArrayUtils.convertToDoublePrimitive(cDList),
					0.0, null, 0.0, null,
					"Ground distance", "CD", "m", "",
					simulationDetailsOutputFolder, "CD_vs_GroundDistance_SI",true);


			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(cDList),
					0.0, null, 0.0, null,
					"Ground distance", "CD", "ft", "",
					simulationDetailsOutputFolder, "CD_vs_GroundDistance_IMPERIAL",true);

			//.................................................................................
			// Horizontal Forces v.s. Time
			{
				double[][] xMatrix1SI = new double[5][totalForceList.size()];
				for(int i=0; i<xMatrix1SI.length; i++)
					xMatrix1SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

				double[][] yMatrix1SI = new double[5][totalForceList.size()];
				yMatrix1SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForceList);
				yMatrix1SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontalList);
				yMatrix1SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(dragList);
				yMatrix1SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(frictionList);
				yMatrix1SI[4] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(weightList)
										)
								)
						); 

				MyChartToFileUtils.plot(
						xMatrix1SI, yMatrix1SI,
						0.0, null, null, null,
						"Time", "Horizontal Forces", "s", "N",
						new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
						simulationDetailsOutputFolder, "HorizontalForces_evolution_SI",
						createCSV
						);
			}

			{
				double[][] xMatrix1IMPERIAL = new double[5][totalForceList.size()];
				for(int i=0; i<xMatrix1IMPERIAL.length; i++)
					xMatrix1IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

				double[][] yMatrix1IMPERIAL = new double[5][totalForceList.size()];
				yMatrix1IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						totalForceList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix1IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustHorizontalList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix1IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						dragList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix1IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
						frictionList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix1IMPERIAL[4] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								weightList.stream()
								.map(x -> x.doubleValue(NonSI.POUND_FORCE))
								.collect(Collectors.toList())
								)
						); 

				MyChartToFileUtils.plot(
						xMatrix1IMPERIAL, yMatrix1IMPERIAL,
						0.0, null, null, null,
						"Time", "Horizontal Forces", "s", "lb",
						new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
						simulationDetailsOutputFolder, "HorizontalForces_evolution_IMPERIAL",
						createCSV );
			}

			//.................................................................................
			// Horizontal Forces v.s. Ground Distance
			{
				double[][] xMatrix2SI = new double[5][totalForceList.size()];
				for(int i=0; i<xMatrix2SI.length; i++)
					xMatrix2SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList);

				double[][] yMatrix2SI = new double[5][totalForceList.size()];
				yMatrix2SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForceList);
				yMatrix2SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontalList);
				yMatrix2SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(dragList);
				yMatrix2SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(frictionList);
				yMatrix2SI[4] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(weightList)
										)
								)
						); 

				MyChartToFileUtils.plot(
						xMatrix2SI, yMatrix2SI,
						0.0, null, null, null,
						"Ground Distance", "Horizontal Forces", "m", "N",
						new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
						simulationDetailsOutputFolder, "HorizontalForces_vs_GroundDistance_SI",
						createCSV);
			}

			{
				double[][] xMatrix2IMPERIAL = new double[5][totalForceList.size()];
				for(int i=0; i<xMatrix2IMPERIAL.length; i++)
					xMatrix2IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList);

				double[][] yMatrix2IMPERIAL = new double[5][totalForceList.size()];
				yMatrix2IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						totalForceList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix2IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustHorizontalList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix2IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						dragList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix2IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
						frictionList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix2IMPERIAL[4] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								weightList.stream()
								.map(x -> x.doubleValue(NonSI.POUND_FORCE))
								.collect(Collectors.toList())
								)
						);    

				MyChartToFileUtils.plot(
						xMatrix2IMPERIAL, yMatrix2IMPERIAL,
						0.0, null, null, null,
						"Ground Distance", "Horizontal Forces", "ft", "lb",
						new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
						simulationDetailsOutputFolder, "HorizontalForces_vs_GroundDistance_IMPERIAL",
						createCSV);
			}

			//.................................................................................
			// Vertical Forces v.s. Time
			{
				double[][] xMatrix3SI = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix3SI.length; i++)
					xMatrix3SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

				double[][] yMatrix3SI = new double[3][totalForceList.size()];
				yMatrix3SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(liftList);
				yMatrix3SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVerticalList);
				yMatrix3SI[2] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(weightList)
										)
								)
						);  

				MyChartToFileUtils.plot(
						xMatrix3SI, yMatrix3SI,
						0.0, null, null, null,
						"Time", "Vertical Forces", "s", "N",
						new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
						simulationDetailsOutputFolder, "VerticalForces_evolution_SI",
						createCSV);
			}

			{
				double[][] xMatrix3IMPERIAL = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix3IMPERIAL.length; i++)
					xMatrix3IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

				double[][] yMatrix3IMPERIAL = new double[3][totalForceList.size()];
				yMatrix3IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						liftList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix3IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustVerticalList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix3IMPERIAL[2] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								weightList.stream()
								.map(x -> x.doubleValue(NonSI.POUND_FORCE))
								.collect(Collectors.toList())
								)
						); 

				MyChartToFileUtils.plot(
						xMatrix3IMPERIAL, yMatrix3IMPERIAL,
						0.0, null, null, null,
						"Time", "Vertical Forces", "s", "lb",
						new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
						simulationDetailsOutputFolder, "VerticalForces_evolution_IMPERIAL",
						createCSV);
			}

			//.................................................................................
			// Vertical Forces v.s. ground distance
			{
				double[][] xMatrix4SI = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix4SI.length; i++)
					xMatrix4SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList);

				double[][] yMatrix4SI = new double[3][totalForceList.size()];
				yMatrix4SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(liftList);
				yMatrix4SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVerticalList);
				yMatrix4SI[2] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertListOfAmountToDoubleArray(weightList)
										)
								)
						);  

				MyChartToFileUtils.plot(
						xMatrix4SI, yMatrix4SI,
						0.0, null, null, null,
						"Ground distance", "Vertical Forces", "m", "N",
						new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
						simulationDetailsOutputFolder, "VerticalForces_vs_GroundDistance_SI",
						createCSV);
			}

			{
				double[][] xMatrix4IMPERIAL = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix4IMPERIAL.length; i++)
					xMatrix4IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							);

				double[][] yMatrix4IMPERIAL = new double[3][totalForceList.size()];
				yMatrix4IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						liftList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix4IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustVerticalList.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						);
				yMatrix4IMPERIAL[2] = MyArrayUtils.convertToDoublePrimitive(
						MyArrayUtils.multiplyListEbE(
								gammaList.stream()
								.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
								.collect(Collectors.toList()),
								weightList.stream()
								.map(x -> x.doubleValue(NonSI.POUND_FORCE))
								.collect(Collectors.toList())
								)
						); 

				MyChartToFileUtils.plot(
						xMatrix4IMPERIAL, yMatrix4IMPERIAL,
						0.0, null, null, null,
						"Ground distance", "Vertical Forces", "ft", "lb",
						new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
						simulationDetailsOutputFolder, "VerticalForces_vs_GroundDistance_IMPERIAL",
						createCSV);
			}

			//.................................................................................
			// Angles v.s. time
			double[][] xMatrix5 = new double[3][totalForceList.size()];
			for(int i=0; i<xMatrix5.length; i++)
				xMatrix5[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

			double[][] yMatrix5 = new double[3][totalForceList.size()];
			yMatrix5[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					alphaList.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix5[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					thetaList.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);
			yMatrix5[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gammaList.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix5, yMatrix5,
					0.0, null, null, null,
					"Time", "Angles", "s", "deg",
					new String[] {"Alpha Body", "Theta", "Gamma"},
					simulationDetailsOutputFolder, "Angles_evolution",
					createCSV);

			//.................................................................................
			// Angles v.s. Ground Distance
			{
				double[][] xMatrix6SI = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix6SI.length; i++)
					xMatrix6SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList);

				double[][] yMatrix6SI = new double[3][totalForceList.size()];
				yMatrix6SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						alphaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thetaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						gammaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);

				MyChartToFileUtils.plot(
						xMatrix6SI, yMatrix6SI,
						0.0, null, null, null,
						"Ground Distance", "Angles", "m", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						simulationDetailsOutputFolder, "Angles_vs_GroundDistance_SI",
						createCSV);
			}

			{
				double[][] xMatrix6IMPERIAL = new double[3][totalForceList.size()];
				for(int i=0; i<xMatrix6IMPERIAL.length; i++)
					xMatrix6IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							);

				double[][] yMatrix6IMPERIAL = new double[3][totalForceList.size()];
				yMatrix6IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						alphaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						thetaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						gammaList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);

				MyChartToFileUtils.plot(
						xMatrix6IMPERIAL, yMatrix6IMPERIAL,
						0.0, null, null, null,
						"Ground Distance", "Angles", "ft", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						simulationDetailsOutputFolder, "Angles_vs_GroundDistance_IMPERIAL",
						createCSV);
			}
			//.................................................................................
			// Angular velocity v.s. time
			double[][] xMatrix7 = new double[2][totalForceList.size()];
			for(int i=0; i<xMatrix7.length; i++)
				xMatrix7[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeList);

			double[][] yMatrix7 = new double[2][totalForceList.size()];
			yMatrix7[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotList);
			yMatrix7[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotList);

			MyChartToFileUtils.plot(
					xMatrix7, yMatrix7,
					0.0, null, -5.0, 5.0,
					"Time", "Angular Velocity", "s", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					simulationDetailsOutputFolder, "AngularVelocity_evolution",
					createCSV);

			//.................................................................................
			// Angular velocity v.s. Ground Distance
			{
				double[][] xMatrix8SI = new double[2][totalForceList.size()];
				for(int i=0; i<xMatrix8SI.length; i++)
					xMatrix8SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList);

				double[][] yMatrix8SI = new double[2][totalForceList.size()];
				yMatrix8SI[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotList);
				yMatrix8SI[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotList);

				MyChartToFileUtils.plot(
						xMatrix8SI, yMatrix8SI,
						0.0, null, -5.0, 5.0,
						"Ground Distance", "Angular Velocity", "m", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						simulationDetailsOutputFolder, "AngularVelocity_vs_GroundDistance_SI",
						createCSV);
			}

			{
				double[][] xMatrix8IMPERIAL = new double[2][totalForceList.size()];
				for(int i=0; i<xMatrix8IMPERIAL.length; i++)
					xMatrix8IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistanceList.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							);

				double[][] yMatrix8SIMPERIAL = new double[2][totalForceList.size()];
				yMatrix8SIMPERIAL[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotList);
				yMatrix8SIMPERIAL[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotList);

				MyChartToFileUtils.plot(
						xMatrix8IMPERIAL, yMatrix8SIMPERIAL,
						0.0, null, -5.0, 5.0,
						"Ground Distance", "Angular Velocity", "ft", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						simulationDetailsOutputFolder, "AngularVelocity_vs_GroundDistance_SI",
						createCSV);
			}

			System.out.println("\n---------------------------DONE!-------------------------------");
		}

		String trajectoryOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "Trajectories" + File.separator
				);
		String thrustOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "Thrust" + File.separator
				);
		String fuelUsedOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "FuelUsed" + File.separator
				);

		System.setOut(originalOut);
		System.out.println("\tPRINTING TRAJECTORY CHARTS TO FILE ...");
		System.setOut(filterStream);

		//.................................................................................
		// take-off trajectory

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, 0.0, null, 
				"Ground distance", "Altitude",
				"m", "m",
				trajectoryOutputFolder, "Trajectory_SI",true
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						verticalDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				0.0, null, 0.0, null, 
				"Ground distance", "Altitude",
				"ft", "ft",
				trajectoryOutputFolder, "Trajectory_IMPERIAL",true
				);

		//.................................................................................
		// vertical distance v.s. time

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, 0.0, null, 
				"Time", "Altitude",
				"s", "m",
				trajectoryOutputFolder, "Altitude_evolution_SI",true
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						verticalDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				0.0, null, 0.0, null, 
				"Time", "Altitude",
				"s", "ft",
				trajectoryOutputFolder, "Altitude_evolution_IMPERIAL",true
				);

		System.setOut(originalOut);
		System.out.println("\tPRINTING THRUST CHARTS TO FILE ...");
		System.setOut(filterStream);

		//.................................................................................
		// thrust v.s. time
		Double yMaxThrust = null;
		if(MyArrayUtils.getMin(thrustList.stream().mapToDouble(ff -> ff.doubleValue(SI.NEWTON)).toArray()) == 0.0
				&& MyArrayUtils.getMax(thrustList.stream().mapToDouble(ff -> ff.doubleValue(SI.NEWTON)).toArray()) == 0.0) 
			yMaxThrust = 1.0; /* Generic positive value */ 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(thrustList),
				0.0, null, 0.0, yMaxThrust, 
				"Time", "Thrust",
				"s", "N",
				thrustOutputFolder, "Thrust_evolution_SI",true
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustList.stream().map(x -> x.to(NonSI.POUND_FORCE)).collect(Collectors.toList())
						),
				0.0, null, 0.0, yMaxThrust, 
				"Time", "Thrust",
				"s", "lbf",
				thrustOutputFolder, "Thrust_evolution_IMPERIAL",true
				);

		//.................................................................................
		// thrust v.s. ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(thrustList),
				0.0, null, 0.0, yMaxThrust, 
				"Ground distance", "Thrust",
				"m", "N",
				thrustOutputFolder, "Thrust_vs_GroundDistance_SI",true
				);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream().map(dist -> dist.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustList.stream().map(thr -> thr.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, yMaxThrust, 
				"Ground distance", "Thrust",
				"ft", "lbf",
				thrustOutputFolder, "Thrust_vs_GroundDistance_IMPERIAL",true
				);

		//.................................................................................
		// fuelUsed v.s. time
		Double yMaxFuel = null;
		if(MyArrayUtils.getMin(fuelUsedList.stream().mapToDouble(ff -> ff.doubleValue(SI.KILOGRAM)).toArray()) == 0.0
				&& MyArrayUtils.getMax(fuelUsedList.stream().mapToDouble(ff -> ff.doubleValue(SI.KILOGRAM)).toArray()) == 0.0) 
			yMaxFuel = 10.0; /* Generic positive value */ 
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(fuelUsedList),
				0.0, null, 0.0, yMaxFuel, 
				"Time", "Fuel Used",
				"s", "kg",
				fuelUsedOutputFolder, "FuelUsed_evolution_SI",true
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						fuelUsedList.stream().map(x -> x.to(NonSI.POUND)).collect(Collectors.toList())
						),
				0.0, null, 0.0, yMaxFuel, 
				"Time", "Fuel used",
				"s", "lb",
				fuelUsedOutputFolder, "FuelUsed_evolution_IMPERIAL",true
				);

		//.................................................................................
		// fuelUsed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(fuelUsedList),
				0.0, null, 0.0, yMaxFuel, 
				"Ground distance", "Fuel Used",
				"m", "kg",
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_SI",true
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream().map(dist -> dist.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						fuelUsedList.stream().map(x -> x.to(NonSI.POUND)).collect(Collectors.toList())
						),
				0.0, null, 0.0, yMaxFuel, 
				"Ground distance", "Fuel used",
				"ft", "lb",
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_IMPERIAL",true
				);

		System.setOut(originalOut);

	}

	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsLandingNoiseTrajectory implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);

		public DynamicsEquationsLandingNoiseTrajectory() {

		}

		@Override
		public int getDimension() {
			return 5;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			Amount<Temperature> deltaTemperature = Amount.valueOf(10.0, SI.CELSIUS); // ISA+10°C
			Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
			Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
			Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
			Amount<Force> weight = Amount.valueOf(
					(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*g0,
					SI.NEWTON
					);
			Amount<Angle> alpha = alpha(time, speed, altitude, deltaTemperature, gamma, weight);

//			if(time.doubleValue(SI.SECOND) > tZeroGamma.doubleValue(SI.SECOND)) {
//				System.out.println("\tTime = " + time);
//				System.out.println("\tSpeed = " + speed);
//				System.out.println("\tAltitude = " + altitude);
//				System.out.println("\tGamma = " + gamma);
//				System.out.println("\tAlpha = " + alpha);
//				System.out.println("\tThrust = " + thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()*0.224809 + " lbf");
//				System.out.println("\tFuel Flow = " + fuelFlow(speed, time, alpha, gamma, altitude, deltaTemperature, weight) + " kg/s");
//				System.out.println("\tAcceleration = " + xDot[1] + " m/s^2");
//				System.out.println("\tGammaDot = " + xDot[2] + " °/s");
//				System.out.println("\tCL = " + cL(alpha));
//				System.out.println("\tCD = " + cD(cL(alpha), altitude));
//				System.out.println("\n");
//			}
			
			if( t < tTouchDown.doubleValue(SI.SECOND)) {
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))*(
						(thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.cos(alpha.doubleValue(SI.RADIAN))
								) 
						- drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
						- (weight.doubleValue(SI.NEWTON)*Math.sin(gamma.doubleValue(SI.RADIAN)))
						); 
				xDot[2] = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))*(
						lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
						+ (thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.sin(alpha.doubleValue(SI.RADIAN))
								)
						- (weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN)))
						);
				xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
				xDot[4] = fuelFlow(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
			}
			else if( t >= tTouchDown.doubleValue(SI.SECOND)  
					&&  t < tTouchDown.to(SI.SECOND).plus(dtFreeRoll.to(SI.SECOND)).doubleValue(SI.SECOND)) {
				
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*(thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum() 
								- drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
								- (mu(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)))
								);
				xDot[2] = 0.0;
				xDot[3] = 0.0;
				xDot[4] = fuelFlow(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
			}
			else if(t >= tTouchDown.to(SI.SECOND).plus(dtFreeRoll.to(SI.SECOND)).doubleValue(SI.SECOND)) {
				
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*(thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum() 
								- drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
								- (muBrake(speed)*(weight.doubleValue(SI.NEWTON) - lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)))
								);
				xDot[2] = 0.0;
				xDot[3] = 0.0;
				xDot[4] = fuelFlow(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
			}
		}

		public List<Amount<Force>> thrust(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight) {

			List<Amount<Force>> theThrustList = new ArrayList<>();

			if (time.doubleValue(SI.SECOND) <= tObstacle.doubleValue(SI.SECOND)) {

				Amount<Force> totalThrust = Amount.valueOf( 
						gammaDescent.doubleValue(SI.RADIAN)*weight.doubleValue(SI.NEWTON) 
						+ drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON),
						SI.NEWTON
						);
				
				for (int i=0; i<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(totalThrust.divide(LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber()));
				
			}
			else if(time.doubleValue(SI.SECOND) > tObstacle.doubleValue(SI.SECOND) && time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {

				Amount<Force> totalThrust = Amount.valueOf(
						thrustFlareFunction.value(time.doubleValue(SI.SECOND)),
						SI.NEWTON
						);

				for (int i=0; i<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(totalThrust.divide(LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber()));

			}
			else if(time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND)) {

				for (int i=0; i<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									thePowerPlant.getEngineList().get(i).getT0(),
									thePowerPlant.getEngineDatabaseReaderList().get(i),
									EngineOperatingConditionEnum.GIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									LandingNoiseTrajectoryCalc.this.getPhi(),
									LandingNoiseTrajectoryCalc.this.getGidlThrustCorrectionFactor()
									)
							);

			}

			return theThrustList;
		}

		public double fuelFlow(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight) {

			double fuelFlow = 0.0;
			List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight); 

			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				for(int ieng=0; ieng<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingNoiseTrajectoryCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingNoiseTrajectoryCalc.this.getFidlThrustCorrectionFactor()
									)
							);
				}
				
				Amount<Force> cruiseThrustFromDatabase = Amount.valueOf(
								cruiseThrustDatabaseTemp.stream().mapToDouble(cthr -> cthr.doubleValue(SI.NEWTON)).sum(),
								SI.NEWTON
								);
				Amount<Force> flightIdleThrustFromDatabase = Amount.valueOf(
								flightIdleThrustDatabaseTemp.stream().mapToDouble(cthr -> cthr.doubleValue(SI.NEWTON)).sum(),
								SI.NEWTON
								);
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										-interpolatedThrust.doubleValue(SI.NEWTON))
								) 
								/ totalThrust.doubleValue(SI.NEWTON)
								)
						> 0.01
						) {
					
					if(iter > maxIter) 
						break;
					
					double thrustRatio = interpolatedThrust.doubleValue(SI.NEWTON)/totalThrust.doubleValue(SI.NEWTON);
					
					/* Increase or decrease flight idle weight to make the interpolatedThrust similar to the target totalThrust */
					double weightFlightIdleTemp = weightFlightIdle;
					weightFlightIdle = weightFlightIdleTemp*thrustRatio;
					weightCruise = 1-weightFlightIdle;
					
					/* Calculate new interpolatedThrust */
					interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
					
					iter++;
					
				}
				
				List<Double> fuelFlowCruiseList = new ArrayList<>();
				List<Double> fuelFlowFlightIdleList = new ArrayList<>();
				for(int ieng=0; ieng<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					fuelFlowCruiseList.add(
							LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									1.0, /* Throttle setting */
									EngineOperatingConditionEnum.CRUISE,
									cruiseSfcCorrectionFactor
									)
							*0.454
							*0.224809
							/60
							*cruiseThrustFromDatabase.doubleValue(SI.NEWTON)
							);
					fuelFlowFlightIdleList.add(
							LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									1.0, /* Throttle setting */
									EngineOperatingConditionEnum.FIDL,
									fidlSfcCorrectionFactor
									)
							*0.454
							*0.224809
							/60
							*flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)
							);
				}
				
				double fuelFlowCruise = fuelFlowCruiseList.stream().mapToDouble(s -> s).sum();
				double fuelFlowFlightIdle = fuelFlowFlightIdleList.stream().mapToDouble(s -> s).sum();
				
				fuelFlow = (fuelFlowCruise*weightCruise)
						+ (fuelFlowFlightIdle*weightFlightIdle);
				
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> fuelFlowList = new ArrayList<>();
				for (int i=0; i<LandingNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					fuelFlowList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingNoiseTrajectoryCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingNoiseTrajectoryCalc.this.getGidlSfcCorrectionFactor()
									)
							*(0.224809)*(0.454/3600)
							*thrustList.get(i).doubleValue(SI.NEWTON)
							);

				fuelFlow = fuelFlowList.stream().mapToDouble(ff -> ff).sum();
			}

			return fuelFlow;
			
		}

		public double cD(double cL, Amount<Length> altitude) {

			double hb = (LandingNoiseTrajectoryCalc.this.getWingToGroundDistance().doubleValue(SI.METER) / LandingNoiseTrajectoryCalc.this.getSpan().doubleValue(SI.METER)) + altitude.doubleValue(SI.METER);
			// Aerodynamics For Naval Aviators: (Hurt)
			double kGround = 1.0;
			if(hb < 1.1)
				kGround = 1- (-4.48276577 * Math.pow(hb, 5) 
						+ 15.61174376 * Math.pow(hb, 4)
						- 21.20171050 * Math.pow(hb, 3)
						+ 14.39438721 * Math.pow(hb, 2)
						- 5.20913465 * hb
						+ 0.90793397);
			
			double cD = MyMathUtils.getInterpolatedValue1DLinear(
					polarCLLanding,
					polarCDLanding, 
					cL);

			double cD0 = MyArrayUtils.getMin(polarCDLanding);
			double cDi = (cD-cD0)*kGround;

			double cDnew = cD0 + cDi;

			return cDnew;
			
		}

		public Amount<Force> drag(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			if(altitude.doubleValue(SI.METER) < 0.0)
				altitude = Amount.valueOf(0.0, SI.METER);

			double cD = cD(cL(alpha), altitude);

			return 	Amount.valueOf(
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (LandingNoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
					*cD,
					SI.NEWTON
					);
			
		}

		public double cL(Amount<Angle> alpha) {

			double cL0 = LandingNoiseTrajectoryCalc.this.cL0LND;
			double cLalpha = LandingNoiseTrajectoryCalc.this.getcLalphaLND().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			double alphaWing = alpha.doubleValue(NonSI.DEGREE_ANGLE) + LandingNoiseTrajectoryCalc.this.getIw().doubleValue(NonSI.DEGREE_ANGLE);
			double cL = cL0 + cLalpha*alphaWing;

			return cL;

		}

		public Amount<Force> lift(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			if(altitude.doubleValue(SI.METER) < 0.0)
				altitude = Amount.valueOf(0.0, SI.METER);

			double cL = cL(alpha);
			return 	Amount.valueOf(
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (LandingNoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
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

		public Amount<Angle> alpha(Amount<Duration> time, Amount<Velocity> speed, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Angle> gamma, Amount<Force> weight) {

			Amount<Angle> alpha = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			
			int maxIterAlpha = 500; /* max alpha excursion +-5° */
			if(time.doubleValue(SI.SECOND) <= tObstacle.doubleValue(SI.SECOND)) {

				int j=0;

				alpha = LandingNoiseTrajectoryCalc.this.getAlpha().get(
						LandingNoiseTrajectoryCalc.this.getAlpha().size()-1
						).to(NonSI.DEGREE_ANGLE);
				double gammaDot = 0.0;
				
				do {
					
					gammaDot = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))*(
							lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
							+ (thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.sin(alpha.doubleValue(SI.RADIAN))
									)
							- (weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN)))
							);

					if (gammaDot > 0) 
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) - 0.01, NonSI.DEGREE_ANGLE);
					else
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) + 0.01, NonSI.DEGREE_ANGLE);
					
					if(j > maxIterAlpha)
						break;
					
					j++;
					
				} while (Math.abs(gammaDot) >= 1e-3);
				
			}
			else if( time.doubleValue(SI.SECOND) > tObstacle.doubleValue(SI.SECOND) && time.doubleValue(SI.SECOND) <= tZeroGamma.doubleValue(SI.SECOND)) {

				int j=0;

				double gammaDot = 0.0;

				alpha = LandingNoiseTrajectoryCalc.this.getAlpha().get(
						LandingNoiseTrajectoryCalc.this.getAlpha().size()-1
						).to(NonSI.DEGREE_ANGLE);

				double gammaDotTarget = 
						(0 - LandingNoiseTrajectoryCalc.this.getGammaDescent().doubleValue(NonSI.DEGREE_ANGLE))
						/ LandingNoiseTrajectoryCalc.this.getDtFlare().doubleValue(SI.SECOND);

				do {
					gammaDot = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))*(
							lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
							+ (thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.sin(alpha.doubleValue(SI.RADIAN))
									)
							- (weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN)))
							);

					if (gammaDot > gammaDotTarget) 
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) - 0.01, NonSI.DEGREE_ANGLE);
					else
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) + 0.01, NonSI.DEGREE_ANGLE);

					if(j > maxIterAlpha)
						break;
					
					j++;

				} while (Math.abs(gammaDot - gammaDotTarget) >= 1e-3);

			}
			else if( time.doubleValue(SI.SECOND) > tZeroGamma.doubleValue(SI.SECOND) ) {

				int j=0;

				double gammaDot = 0.0;

				alpha = LandingNoiseTrajectoryCalc.this.getAlpha().get(
						LandingNoiseTrajectoryCalc.this.getAlpha().size()-1
						).to(NonSI.DEGREE_ANGLE);


				do {
					gammaDot = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))*(
							lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
							+ (thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.sin(alpha.doubleValue(SI.RADIAN))
									)
							- (weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN)))
							);

					if (gammaDot > 0.0) 
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) - 0.01, NonSI.DEGREE_ANGLE);
					else
						alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) + 0.01, NonSI.DEGREE_ANGLE);

					if(j > maxIterAlpha)
						break;
					
					j++;

				} while (Math.abs(gammaDot - 0) >= 1e-3);

			}
			else if( time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))
				alpha = LandingNoiseTrajectoryCalc.this.getAlphaGround().to(NonSI.DEGREE_ANGLE);

			return alpha;
		}
	}
	//-------------------------------------------------------------------------------------
	//									END NESTED CLASS	
	//-------------------------------------------------------------------------------------


	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:

	public Double getAspectRatio() {
		return aspectRatio;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public Amount<Length> getSpan() {
		return span;
	}

	public PowerPlant getThePowerPlant() {
		return thePowerPlant;
	}

	public double[] getPolarCLLanding() {
		return polarCLLanding;
	}

	public double[] getPolarCDLanding() {
		return polarCDLanding;
	}

	public Amount<Duration> getDtFlare() {
		return dtFlare;
	}

	public Amount<Duration> getDtFreeRoll() {
		return dtFreeRoll;
	}

	public Amount<Duration> gettObstacle() {
		return tObstacle;
	}

	public Amount<Duration> gettTouchDown() {
		return tTouchDown;
	}

	public Amount<Mass> getMaxLandingMass() {
		return maxLandingMass;
	}

	public Amount<Velocity> getvSLanding() {
		return vSLanding;
	}

	public Amount<Velocity> getvApproach() {
		return vApproach;
	}

	public Amount<Velocity> getvTouchDown() {
		return vTouchDown;
	}

	public Amount<Velocity> getvWind() {
		return vWind;
	}

	public Amount<Velocity> getvDescent() {
		return vDescent;
	}

	public Amount<Length> getObstacle() {
		return obstacle;
	}

	public Amount<Length> getIntialAltitude() {
		return intialAltitude;
	}

	public Amount<Angle> getGammaDescent() {
		return gammaDescent;
	}

	public Amount<Angle> getIw() {
		return iw;
	}

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public List<Double> getTimeBreakPoints() {
		return timeBreakPoints;
	}

	public Double getAlphaDotFlare() {
		return alphaDotFlare;
	}

	public Double getcL0LND() {
		return cL0LND;
	}

	public Double getcLmaxLND() {
		return cLmaxLND;
	}

	public Amount<?> getcLalphaLND() {
		return cLalphaLND;
	}

	public MyInterpolatingFunction getMu() {
		return mu;
	}

	public MyInterpolatingFunction getMuBrake() {
		return muBrake;
	}

	public FirstOrderIntegrator getTheIntegrator() {
		return theIntegrator;
	}

	public FirstOrderDifferentialEquations getOde() {
		return ode;
	}

	public List<Amount<Velocity>> getSpeedList() {
		return speedList;
	}

	public List<Amount<Velocity>> getRateOfClimbList() {
		return rateOfClimbList;
	}

	public List<Amount<Force>> getThrustList() {
		return thrustList;
	}

	public List<Amount<Force>> getThrustHorizontalList() {
		return thrustHorizontalList;
	}

	public List<Amount<Force>> getThrustVerticalList() {
		return thrustVerticalList;
	}

	public List<Amount<Force>> getLiftList() {
		return liftList;
	}

	public List<Amount<Force>> getDragList() {
		return dragList;
	}

	public List<Amount<Force>> getTotalForceList() {
		return totalForceList;
	}

	public List<Amount<Force>> getFrictionList() {
		return frictionList;
	}

	public List<Amount<Angle>> getAlphaList() {
		return alphaList;
	}

	public List<Amount<Angle>> getGammaList() {
		return gammaList;
	}

	public List<Amount<Angle>> getThetaList() {
		return thetaList;
	}

	public List<Double> getAlphaDotList() {
		return alphaDotList;
	}

	public List<Double> getGammaDotList() {
		return gammaDotList;
	}

	public List<Double> getcLList() {
		return cLList;
	}

	public List<Double> getLoadFactorList() {
		return loadFactorList;
	}

	public List<Double> getcDList() {
		return cDList;
	}

	public List<Amount<Acceleration>> getAccelerationList() {
		return accelerationList;
	}

	public List<Amount<Length>> getGroundDistanceList() {
		return groundDistanceList;
	}

	public List<Amount<Length>> getVerticalDistanceList() {
		return verticalDistanceList;
	}

	public List<Amount<Duration>> getTimeList() {
		return timeList;
	}

	public List<Amount<Mass>> getFuelUsedList() {
		return fuelUsedList;
	}

	public List<Amount<Force>> getWeightList() {
		return weightList;
	}

	public PrintStream getOriginalOut() {
		return originalOut;
	}

	public PrintStream getFilterStream() {
		return filterStream;
	}

	public void setAspectRatio(Double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public void setSpan(Amount<Length> span) {
		this.span = span;
	}

	public void setThePowerPlant(PowerPlant thePowerPlant) {
		this.thePowerPlant = thePowerPlant;
	}

	public void setPolarCLLanding(double[] polarCLLanding) {
		this.polarCLLanding = polarCLLanding;
	}

	public void setPolarCDLanding(double[] polarCDLanding) {
		this.polarCDLanding = polarCDLanding;
	}

	public void setDtFlare(Amount<Duration> dtFlare) {
		this.dtFlare = dtFlare;
	}

	public void setDtFreeRoll(Amount<Duration> dtFreeRoll) {
		this.dtFreeRoll = dtFreeRoll;
	}

	public void settObstacle(Amount<Duration> tObstacle) {
		this.tObstacle = tObstacle;
	}

	public void settTouchDown(Amount<Duration> tTouchDown) {
		this.tTouchDown = tTouchDown;
	}

	public void setMaxLandingMass(Amount<Mass> maxLandingMass) {
		this.maxLandingMass = maxLandingMass;
	}

	public void setvSLanding(Amount<Velocity> vSLanding) {
		this.vSLanding = vSLanding;
	}

	public void setvApproach(Amount<Velocity> vApproach) {
		this.vApproach = vApproach;
	}

	public void setvTouchDown(Amount<Velocity> vTouchDown) {
		this.vTouchDown = vTouchDown;
	}

	public void setvWind(Amount<Velocity> vWind) {
		this.vWind = vWind;
	}

	public void setvDescent(Amount<Velocity> vDescent) {
		this.vDescent = vDescent;
	}

	public void setObstacle(Amount<Length> obstacle) {
		this.obstacle = obstacle;
	}

	public void setIntialAltitude(Amount<Length> intialAltitude) {
		this.intialAltitude = intialAltitude;
	}

	public void setGammaDescent(Amount<Angle> gammaDescent) {
		this.gammaDescent = gammaDescent;
	}

	public void setIw(Amount<Angle> iw) {
		this.iw = iw;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public void setTimeBreakPoints(List<Double> timeBreakPoints) {
		this.timeBreakPoints = timeBreakPoints;
	}

	public void setAlphaDotFlare(Double alphaDotFlare) {
		this.alphaDotFlare = alphaDotFlare;
	}

	public void setcL0LND(Double cL0LND) {
		this.cL0LND = cL0LND;
	}

	public void setcLmaxLND(Double cLmaxLND) {
		this.cLmaxLND = cLmaxLND;
	}

	public void setcLalphaLND(Amount<?> cLalphaLND) {
		this.cLalphaLND = cLalphaLND;
	}

	public void setMu(MyInterpolatingFunction mu) {
		this.mu = mu;
	}

	public void setMuBrake(MyInterpolatingFunction muBrake) {
		this.muBrake = muBrake;
	}

	public void setTheIntegrator(FirstOrderIntegrator theIntegrator) {
		this.theIntegrator = theIntegrator;
	}

	public void setOde(FirstOrderDifferentialEquations ode) {
		this.ode = ode;
	}

	public void setSpeedList(List<Amount<Velocity>> speedList) {
		this.speedList = speedList;
	}

	public void setRateOfClimbList(List<Amount<Velocity>> rateOfClimbList) {
		this.rateOfClimbList = rateOfClimbList;
	}

	public void setThrustList(List<Amount<Force>> thrustList) {
		this.thrustList = thrustList;
	}

	public void setThrustHorizontalList(List<Amount<Force>> thrustHorizontalList) {
		this.thrustHorizontalList = thrustHorizontalList;
	}

	public void setThrustVerticalList(List<Amount<Force>> thrustVerticalList) {
		this.thrustVerticalList = thrustVerticalList;
	}

	public void setLiftList(List<Amount<Force>> liftList) {
		this.liftList = liftList;
	}

	public void setDragList(List<Amount<Force>> dragList) {
		this.dragList = dragList;
	}

	public void setTotalForceList(List<Amount<Force>> totalForceList) {
		this.totalForceList = totalForceList;
	}

	public void setFrictionList(List<Amount<Force>> frictionList) {
		this.frictionList = frictionList;
	}

	public void setAlphaList(List<Amount<Angle>> alphaList) {
		this.alphaList = alphaList;
	}

	public void setGammaList(List<Amount<Angle>> gammaList) {
		this.gammaList = gammaList;
	}

	public void setThetaList(List<Amount<Angle>> thetaList) {
		this.thetaList = thetaList;
	}

	public void setAlphaDotList(List<Double> alphaDotList) {
		this.alphaDotList = alphaDotList;
	}

	public void setGammaDotList(List<Double> gammaDotList) {
		this.gammaDotList = gammaDotList;
	}

	public void setcLList(List<Double> cLList) {
		this.cLList = cLList;
	}

	public void setLoadFactorList(List<Double> loadFactorList) {
		this.loadFactorList = loadFactorList;
	}

	public void setcDList(List<Double> cDList) {
		this.cDList = cDList;
	}

	public void setAccelerationList(List<Amount<Acceleration>> accelerationList) {
		this.accelerationList = accelerationList;
	}

	public void setGroundDistanceList(List<Amount<Length>> groundDistanceList) {
		this.groundDistanceList = groundDistanceList;
	}

	public void setVerticalDistanceList(List<Amount<Length>> verticalDistanceList) {
		this.verticalDistanceList = verticalDistanceList;
	}

	public void setTimeList(List<Amount<Duration>> timeList) {
		this.timeList = timeList;
	}

	public void setFuelUsedList(List<Amount<Mass>> fuelUsedList) {
		this.fuelUsedList = fuelUsedList;
	}

	public void setWeightList(List<Amount<Force>> weightList) {
		this.weightList = weightList;
	}

	public void setFilterStream(PrintStream filterStream) {
		this.filterStream = filterStream;
	}

	public Amount<Angle> getAlphaGround() {
		return alphaGround;
	}

	public void setAlphaGround(Amount<Angle> alphaGround) {
		this.alphaGround = alphaGround;
	}

	public Double getkGround() {
		return kGround;
	}

	public void setkGround(Double kGround) {
		this.kGround = kGround;
	}

	public Amount<Force> getThrustAtFlareStart() {
		return thrustAtFlareStart;
	}

	public void setThrustAtFlareStart(Amount<Force> thrustAtFlareStart) {
		this.thrustAtFlareStart = thrustAtFlareStart;
	}

	public List<Double> getGammaDot() {
		return gammaDot;
	}

	public void setGammaDot(List<Double> gammaDot) {
		this.gammaDot = gammaDot;
	}

	public Amount<Duration> gettZeroGamma() {
		return tZeroGamma;
	}

	public void settZeroGamma(Amount<Duration> tZeroGamma) {
		this.tZeroGamma = tZeroGamma;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public Amount<Length> getAltitudeAtFlareEnding() {
		return altitudeAtFlareEnding;
	}

	public void setAltitudeAtFlareEnding(Amount<Length> altitudeAtFlareEnding) {
		this.altitudeAtFlareEnding = altitudeAtFlareEnding;
	}

	public MyInterpolatingFunction getThrustFlareFunction() {
		return thrustFlareFunction;
	}

	public void setThrustFlareFunction(MyInterpolatingFunction thrustFlareFunction) {
		this.thrustFlareFunction = thrustFlareFunction;
	}

	public boolean getCreateCSV() {
		return createCSV;
	}

	public void setCreateCSV(boolean createCSV) {
		this.createCSV = createCSV;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public Amount<Length> getWingToGroundDistance() {
		return wingToGroundDistance;
	}

	public void setWingToGroundDistance(Amount<Length> wingToGroundDistance) {
		this.wingToGroundDistance = wingToGroundDistance;
	}

	public double getCruiseThrustCorrectionFactor() {
		return cruiseThrustCorrectionFactor;
	}

	public void setCruiseThrustCorrectionFactor(double cruiseThrustCorrectionFactor) {
		this.cruiseThrustCorrectionFactor = cruiseThrustCorrectionFactor;
	}

	public double getCruiseSfcCorrectionFactor() {
		return cruiseSfcCorrectionFactor;
	}

	public void setCruiseSfcCorrectionFactor(double cruiseSfcCorrectionFactor) {
		this.cruiseSfcCorrectionFactor = cruiseSfcCorrectionFactor;
	}

	public double getFidlThrustCorrectionFactor() {
		return fidlThrustCorrectionFactor;
	}

	public void setFidlThrustCorrectionFactor(double fidlThrustCorrectionFactor) {
		this.fidlThrustCorrectionFactor = fidlThrustCorrectionFactor;
	}

	public double getFidlSfcCorrectionFactor() {
		return fidlSfcCorrectionFactor;
	}

	public void setFidlSfcCorrectionFactor(double fidlSfcCorrectionFactor) {
		this.fidlSfcCorrectionFactor = fidlSfcCorrectionFactor;
	}

	public double getGidlThrustCorrectionFactor() {
		return gidlThrustCorrectionFactor;
	}

	public void setGidlThrustCorrectionFactor(double gidlThrustCorrectionFactor) {
		this.gidlThrustCorrectionFactor = gidlThrustCorrectionFactor;
	}

	public double getGidlSfcCorrectionFactor() {
		return gidlSfcCorrectionFactor;
	}

	public void setGidlSfcCorrectionFactor(double gidlSfcCorrectionFactor) {
		this.gidlSfcCorrectionFactor = gidlSfcCorrectionFactor;
	}

	public Amount<Angle> getGammaAtFlareEnding() {
		return gammaAtFlareEnding;
	}

	public void setGammaAtFlareEnding(Amount<Angle> gammaAtFlareEnding) {
		this.gammaAtFlareEnding = gammaAtFlareEnding;
	}

}