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
import calculators.aerodynamics.AerodynamicCalc;
import configuration.enumerations.EngineOperatingConditionEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * This class have the purpose of calculating the trajectories for the noise certification
 * of a given aircraft by evaluating the following cases:
 *
 * - take-off in ISA+10°C with MAX TAKE-OFF, Landing Gear retraction and a V2=1.2VsTO + (10 or 20 kts)
 * - take-off in ISA+10°C with MAX TAKE-OFF, Landing Gear retraction, a V2=1.2VsTO + (10 or 20 kts) and 
 *   a reduction of the thrust starting from 984ft assuming the lowest throttle setting between:
 *   	
 *   	- the setting which leads to a constant CGR of 4%
 *   	- the setting which in OEI (considering a DeltaCD0OEI) leads to a leveled flight
 *   
 * - take-off in ISA+10°C with MAX TAKE-OFF, Landing Gear retraction, a V2=1.2VsTO + (10 or 20 kts) and 
 *   a reduction of the thrust starting from 984ft assuming a set of throttle setting between 1.0 and the one 
 *   found at the previous step
 *
 * for each of them a step by step integration is used in solving the dynamic equation.
 *
 * @author Vittorio Trifari
 *
 */

	//////////////////////////////////////////////
    //								            //
	// FIXME: LOOP ON VClimb UNTIL AN           // 
    //        ACCELERATION OF ZERO IS REACHED   //
    //        WITH A SPEED = V2+10kst           //
	//								            //
	//////////////////////////////////////////////

public class NoiseTrajectoryCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private Double aspectRatio;
	private Amount<Area> surface; 
	private Amount<Length> span;
	private PowerPlant thePowerPlant;
	private Double[] polarCLTakeOff;
	private Double[] polarCDTakeOff;
	private Amount<Duration> dtRot, dtHold, dtLandingGearRetraction,
	tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tLandingGearRetractionStart = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tLandingGearRetractionEnd = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tObstacle = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tZeroAccelration = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tVClimb = Amount.valueOf(10000, SI.SECOND); // initialization to an impossible time
	private Amount<Mass> maxTakeOffMass; 
	private Amount<Velocity> vSTakeOff, vRot, vLO, vWind, v1, v2, vClimb;
	private Amount<Length> wingToGroundDistance, obstacle, xEndSimulation;
	private Amount<Angle> alphaGround, iw;
	private List<Amount<Angle>> alpha;
	private List<Amount<Duration>> time;
	private List<Amount<Acceleration>> acceleration;
	private List<Double> loadFactor, cL, timeBreakPoints;
	private Double kAlphaDot, kcLMax, kRot, phi, cLmaxTO, kGround, alphaDotInitial, deltaCD0LandingGear, deltaCD0OEI, 
	alphaRed, cL0;
	private Amount<?> cLalphaFlap;
	private MyInterpolatingFunction mu, deltaCD0LandingGearRetractionSlope;

	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;

	//-------------------------------------------------------------------------------------
	// BUILDER:

	public NoiseTrajectoryCalc(
			Amount<Length> xEndSimulation,
			Amount<Mass> maxTakeOffMass,
			PowerPlant thePowerPlant,
			Double[] polarCLTakeOff,
			Double[] polarCDTakeOff,
			Double deltaCD0LandingGear,
			Double deltaCD0OEI,
			Double aspectRatio,
			Amount<Area> surface,
			Amount<Duration> dtRot,
			Amount<Duration> dtHold,
			Amount<Duration> dtLandingGearRetraction,
			double throttleSetting,
			double kcLMax,
			double kRot,
			double alphaDotInitial,
			double kAlphaDot,
			MyInterpolatingFunction mu,
			Amount<Length> wingToGroundDistance,
			Amount<Angle> iw,
			double cLmaxTO,
			double cLZeroTO,
			Amount<?> cLalphaFlap
			) {

		// Required data
		this.xEndSimulation = xEndSimulation;
		this.aspectRatio = aspectRatio;
		this.surface = surface;
		this.span = Amount.valueOf(
				Math.sqrt(aspectRatio*surface.doubleValue(SI.SQUARE_METRE)),
				SI.METER
				);
		this.thePowerPlant = thePowerPlant;
		this.polarCLTakeOff = polarCLTakeOff;
		this.polarCDTakeOff = polarCDTakeOff;
		this.deltaCD0LandingGear = deltaCD0LandingGear;
		this.deltaCD0OEI = deltaCD0OEI;
		this.maxTakeOffMass = maxTakeOffMass;
		this.dtRot = dtRot;
		this.dtHold = dtHold;
		this.dtLandingGearRetraction = dtLandingGearRetraction;
		this.kcLMax = kcLMax;
		this.kRot = kRot;
		this.alphaDotInitial = alphaDotInitial;
		this.kAlphaDot = kAlphaDot;
		this.phi = throttleSetting;
		this.mu = mu;
		this.wingToGroundDistance = wingToGroundDistance;
		this.obstacle = Amount.valueOf(35, NonSI.FOOT);
		this.vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		this.alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		this.iw = iw;
		this.cLmaxTO = cLmaxTO;
		this.cLalphaFlap = cLalphaFlap;
		this.cL0 = cLZeroTO;
		this.deltaCD0LandingGearRetractionSlope = new MyInterpolatingFunction();
		
		// Reference velocities definition
		vSTakeOff = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						0.0, // SEA LEVEL
						maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
						surface.doubleValue(SI.SQUARE_METRE),
						cLmaxTO
						),
				SI.METERS_PER_SECOND);
		vRot = vSTakeOff.times(kRot);

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxTO = " + cLmaxTO);
		System.out.println("CL0 = " + cLZeroTO);
		System.out.println("VsTO = " + vSTakeOff);
		System.out.println("VRot = " + vRot);
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.time = new ArrayList<Amount<Duration>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.loadFactor = new ArrayList<Double>();
		this.cL = new ArrayList<Double>();
		this.timeBreakPoints = new ArrayList<Double>();
		
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
		acceleration.clear();
		loadFactor.clear();
		cL.clear();
		timeBreakPoints.clear();

		tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tLandingGearRetractionStart = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tLandingGearRetractionEnd = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tVClimb = Amount.valueOf(10000, SI.SECOND);	// initialization to an impossible time
		tObstacle = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
		tZeroAccelration = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
	}

	/***************************************************************************************
	 * This method performs the integration of the equation of motion by solving a set of
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateTakeOffTrajectoryAEOUpToObstacle(String outputFolderPath) {

		System.out.println("---------------------------------------------------");
		System.out.println("NoiseTrajectoryCalc :: ODE integration\n\n");

		int i=0;
		double newAlphaRed = 0.0;
		alphaRed = 0.0;

//		double tObstacleCheck = 10000.0; // initialization to impossible values
//		double tClimbCheck = 1.0; // initialization to impossible values
		
//		v2 = Amount.valueOf(10000, SI.METERS_PER_SECOND); // initialization to impossible values
//		while (Math.abs(
//				(v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
//				- 1.2 
//				- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
//				) >= 0.01) {
//
//			double check = (Math.abs(tObstacleCheck - tClimbCheck)/tObstacleCheck);
//			double check = (Math.abs(
//					(v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
//					- 1.2 
//					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
//					));
		
//		while ((Math.abs(tObstacleCheck - tClimbCheck)/tObstacleCheck) >= 0.01) {
			
		vClimb = Amount.valueOf(10000, SI.METERS_PER_SECOND); // initialization to impossible values
		while (Math.abs(
				(vClimb.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
				- 1.2 
				- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
				) >= 0.01) {

			double check = (Math.abs(
					(vClimb.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
					- 1.2 
					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
					));
		
			if(i >= 1) {
				if(newAlphaRed <= 0.0)
					alphaRed = newAlphaRed;
				else
					break;
			}

			initialize();

			theIntegrator = new HighamHall54Integrator(
					1e-15,
					1,
					1e-17,
					1e-17
					);
			ode = new DynamicsEquationsNoiseTrajectory();

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
					if(t < tRot.doubleValue(SI.SECOND)) 
						return speed - vRot.getEstimatedValue();
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
							"\n\tx[3] = altitude = " + x[3] + " m"
							);

					timeBreakPoints.add(t);
					tRot = Amount.valueOf(t, SI.SECOND);

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
					
					timeBreakPoints.add(t);
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
							"\n\tx[3] = altitude = " + x[3] + " m"
							);

					v2 = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					System.out.println("\n\tV2/VsTO = " + v2.divide(vSTakeOff));
					tObstacle = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.CONTINUE;
				}
			};
			EventHandler ehCheckXEndSimulation = new EventHandler() {

				@Override
				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public double g(double t, double[] x) {
					
					double position = x[0];
					return position - xEndSimulation.doubleValue(SI.METER);
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					System.out.println("\n\t\tEND OF THE SIMULATION");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
							"\n\tx[1] = V = " + x[1] + " m/s" + 
							"\n\tx[2] = gamma = " + x[2] + " °" +
							"\n\tx[3] = altitude = " + x[3] + " m"
							);
					System.out.println("\n---------------------------DONE!-------------------------------\n");
					System.out.println("\n==============================================================");
					System.out.println("\n==============================================================\n\n");
					timeBreakPoints.add(t);
					
					return Action.STOP;
				}

				@Override
				public void resetState(double t, double[] x) {
					
				}
				
			};
			EventHandler ehLandingGearRetractionStart = new EventHandler() {

				@Override
				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public double g(double t, double[] x) {
					return x[3] - obstacle.doubleValue(SI.METER);
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					System.out.println("\n\t\t LANDING GEAR RETRACTION :: START");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
							"\n\tx[1] = V = " + x[1] + " m/s" + 
							"\n\tx[2] = gamma = " + x[2] + " °" +
							"\n\tx[3] = altitude = " + x[3] + " m"
							);
					timeBreakPoints.add(t);
					tLandingGearRetractionStart = Amount.valueOf(t, SI.SECOND);
					deltaCD0LandingGearRetractionSlope.interpolateLinear(
							new double[] {
									tLandingGearRetractionStart.doubleValue(SI.SECOND), 
									tLandingGearRetractionStart.plus(dtLandingGearRetraction).doubleValue(SI.SECOND)
									}, 
							new double[] {0, 1}
							);
					
					System.out.println("\n---------------------------DONE!-------------------------------\n");
					return Action.CONTINUE;
				}

				@Override
				public void resetState(double t, double[] x) {
				}
				
			};
			EventHandler ehLandingGearRetractionEnd = new EventHandler() {

				@Override
				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public double g(double t, double[] x) {
					return t - tLandingGearRetractionStart.plus(dtLandingGearRetraction).doubleValue(SI.SECOND);					
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					System.out.println("\n\t\t LANDING GEAR RETRACTION :: END");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
							"\n\tx[1] = V = " + x[1] + " m/s" + 
							"\n\tx[2] = gamma = " + x[2] + " °" +
							"\n\tx[3] = altitude = " + x[3] + " m"
							);
					timeBreakPoints.add(t);
					tLandingGearRetractionEnd = Amount.valueOf(t, SI.SECOND);
					System.out.println("\n---------------------------DONE!-------------------------------\n");
					return Action.STOP;
				}

				@Override
				public void resetState(double t, double[] x) {
				}
				
			};

			theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 50);
			theIntegrator.addEventHandler(ehEndConstantCL, 1.0, 1e-3, 50);
			theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-3, 50);
//			theIntegrator.addEventHandler(ehLandingGearRetractionStart, 1.0, 1e-3, 50);
//			theIntegrator.addEventHandler(ehLandingGearRetractionEnd, 1.0, 1e-3, 50);

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] x = interpolator.getInterpolatedState();
					double[] xDot = interpolator.getInterpolatedDerivatives();

					//----------------------------------------------------------------------------------------
					// TIME:
					NoiseTrajectoryCalc.this.getTime().add(Amount.valueOf(t, SI.SECOND));
					//----------------------------------------------------------------------------------------
					// ALPHA:
					NoiseTrajectoryCalc.this.getAlpha().add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).alpha,
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					NoiseTrajectoryCalc.this.getLoadFactor().add(
							(((DynamicsEquationsNoiseTrajectory)ode).lift(
									x[1],
									((DynamicsEquationsNoiseTrajectory)ode).alpha,
									x[2],
									t,
									x[3])
									+ (((DynamicsEquationsNoiseTrajectory)ode).thrust(
											x[1],
											x[2],
											t,
											x[3]
											)*Math.sin(
													Amount.valueOf(
															((DynamicsEquationsNoiseTrajectory)ode).alpha,
															NonSI.DEGREE_ANGLE
															).to(SI.RADIAN).getEstimatedValue())
											)
									)
							/(((DynamicsEquationsNoiseTrajectory)ode).weight*Math.cos(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					NoiseTrajectoryCalc.this.getcL().add(
							((DynamicsEquationsNoiseTrajectory)ode).cL(
									x[1],
									((DynamicsEquationsNoiseTrajectory)ode).alpha,
									x[2],
									t,
									x[3]
									)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
//					double acceleration = xDot[1];
//					System.out.println(acceleration);
					
					NoiseTrajectoryCalc.this.getAcceleration().add(
							Amount.valueOf(xDot[1], SI.METERS_PER_SQUARE_SECOND)
							);
					
					//========================================================================================
					// CHECK ON LOAD FACTOR --> END ROTATION WHEN n=1
					if((t > tRot.getEstimatedValue()) && (tEndRot.getEstimatedValue() == 10000.0) &&
							(NoiseTrajectoryCalc.this.getLoadFactor().get(NoiseTrajectoryCalc.this.getLoadFactor().size()-1) > 1) &&
							(NoiseTrajectoryCalc.this.getLoadFactor().get(NoiseTrajectoryCalc.this.getLoadFactor().size()-2) < 1)) {
						System.out.println("\n\t\tEND OF ROTATION PHASE");
						System.out.println(
								"\n\tx[0] = s = " + x[0] + " m" +
										"\n\tx[1] = V = " + x[1] + " m/s" + 
										"\n\tx[2] = gamma = " + x[2] + " °" +
										"\n\tx[3] = altitude = " + x[3] + " m"+
										"\n\tt = " + t + " s"
								);
						System.out.println("\n---------------------------DONE!-------------------------------");

						tEndRot = Amount.valueOf(t, SI.SECOND);
						timeBreakPoints.add(t);
						vLO = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					}

					//========================================================================================
					// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
					if((t > tEndRot.getEstimatedValue()) && (t <= tObstacle.getEstimatedValue()) && 
							(NoiseTrajectoryCalc.this.getcL().get(NoiseTrajectoryCalc.this.getcL().size()-1) - (kcLMax*cLmaxTO) >= 0.0) &&
							((NoiseTrajectoryCalc.this.getcL().get(NoiseTrajectoryCalc.this.getcL().size()-2) - (kcLMax*cLmaxTO)) < 0.0)) {
						System.out.println("\n\t\tBEGIN BAR HOLDING");
						System.out.println(
								"\n\tCL = " + ((DynamicsEquationsNoiseTrajectory)ode).cL(
										x[1],
										((DynamicsEquationsNoiseTrajectory)ode).alpha,
										x[2],
										t,
										x[3]
										) + 
								"\n\tAlpha Body = " + ((DynamicsEquationsNoiseTrajectory)ode).alpha + " °" + 
								"\n\tt = " + t + " s"
								);
						System.out.println("\n---------------------------DONE!-------------------------------");

						tHold = Amount.valueOf(t, SI.SECOND);
						timeBreakPoints.add(t);
					}

					//========================================================================================
					// CHECK ON LOAD FACTOR TO ENSTABLISH WHEN n=1 WHILE DECREASING ALPHA AND CL
					if((t > tEndHold.getEstimatedValue()) && (tClimb.getEstimatedValue() == 10000.0) &&
							(NoiseTrajectoryCalc.this.getLoadFactor().get(NoiseTrajectoryCalc.this.getLoadFactor().size()-1) < 1) &&
							(NoiseTrajectoryCalc.this.getLoadFactor().get(NoiseTrajectoryCalc.this.getLoadFactor().size()-2) > 1) ) {
						System.out.println("\n\t\tLOAD FACTOR = 1 IN CLIMB");
						System.out.println( 
								"\n\tt = " + t + " s"
								);
						System.out.println("\n---------------------------DONE!-------------------------------");

						tClimb = Amount.valueOf(t, SI.SECOND);
						timeBreakPoints.add(t);
					}

					//========================================================================================
					// CHECK ON ACCELERATION --> DEFINING THE ISTANT AT WHICH THE SPEED MUST BE KEPT CONSTANT 
					if(t > tRot.doubleValue(SI.SECOND) && tZeroAccelration.doubleValue(SI.SECOND) == 10000 &&
							(NoiseTrajectoryCalc.this.getAcceleration().get(NoiseTrajectoryCalc.this.getAcceleration().size()-1).doubleValue(SI.METERS_PER_SQUARE_SECOND)< 0.0) &&
							(NoiseTrajectoryCalc.this.getAcceleration().get(NoiseTrajectoryCalc.this.getAcceleration().size()-2).doubleValue(SI.METERS_PER_SQUARE_SECOND) > 0.0)
							) {
						
						System.out.println("\n\t\tZERO ACCELERATION REACHED ... ");
						System.out.println( 
								"\n\tt = " + t + " s"
								);
						System.out.println(
								"\n\tx[0] = s = " + x[0] + " m" +
								"\n\tx[1] = V = " + x[1] + " m/s" + 
								"\n\tx[2] = gamma = " + x[2] + " °" +
								"\n\tx[3] = altitude = " + x[3] + " m"
								);
						System.out.println("\n---------------------------DONE!-------------------------------");

						tZeroAccelration = Amount.valueOf(t, SI.SECOND);
						vClimb = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
						timeBreakPoints.add(t);
						
					}
						
					
				}
			};
			theIntegrator.addStepHandler(stepHandler);

			//##############################################################################################
			// Use this handler for post-processing

			theIntegrator.addStepHandler(new ContinuousOutputModel());

			//##############################################################################################

			double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0}; // initial state
			theIntegrator.integrate(ode, 0.0, xAt0, 1000, xAt0); // now xAt0 contains final state

			//--------------------------------------------------------------------------------
			// UPDATING tClimb AND tObstacle
//			tClimbCheck = tClimb.doubleValue(SI.SECOND);
//			tObstacleCheck = tObstacle.doubleValue(SI.SECOND);
			
			//--------------------------------------------------------------------------------
			// NEW ALPHA REDUCTION RATE 
//			if(Math.abs(tClimb.doubleValue(SI.SECOND) - tObstacle.doubleValue(SI.SECOND)) >= 0.0)
//			if((v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
//					- 1.2 
//					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
//					>= 0.0)
			if((vClimb.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
					- 1.2 
					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
					>= 0.0)
//				newAlphaRed = alphaRed - 0.1;
				newAlphaRed = alphaRed + 0.1;
			else
//				newAlphaRed = alphaRed + 0.1;
			    newAlphaRed = alphaRed - 0.1;

//			if((Math.abs(tObstacle.doubleValue(SI.SECOND) - tClimb.doubleValue(SI.SECOND))
//					/tObstacle.doubleValue(SI.SECOND)) < 0.01)
//			if(Math.abs(
//					(v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
//					- 1.2 
//					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
//					) < 0.01)
			if(Math.abs(
					(vClimb.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)).getEstimatedValue()) 
					- 1.2 
					- (5.144/vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
					) < 0.01)
				try {
					createOutputCharts(0.25, outputFolderPath);
					break;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			
			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();
			
			i++;
		} 

		System.out.println("\n---------------------------END!!-------------------------------");
	}
		
	/**************************************************************************************
	 * This method allows users to plot all simulation results producing several output charts
	 * which have time as independent variables.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createOutputCharts(double dt, String outputFolderPath) throws InstantiationException, IllegalAccessException {

		List<Amount<Velocity>> speed = new ArrayList<Amount<Velocity>>();
		List<Amount<Force>> thrust = new ArrayList<Amount<Force>>();
		List<Amount<Force>> thrustHorizontal = new ArrayList<Amount<Force>>();
		List<Amount<Force>> thrustVertical = new ArrayList<Amount<Force>>();
		List<Amount<Angle>> alpha = new ArrayList<Amount<Angle>>();
		List<Double> alphaDot = new ArrayList<Double>();
		List<Amount<Angle>> gamma = new ArrayList<Amount<Angle>>();
		List<Double> gammaDot = new ArrayList<Double>();
		List<Amount<Angle>> theta = new ArrayList<Amount<Angle>>();
		List<Double> cL = new ArrayList<Double>();
		List<Amount<Force>> lift = new ArrayList<Amount<Force>>();
		List<Double> loadFactor = new ArrayList<Double>();
		List<Double> cD = new ArrayList<Double>();
		List<Amount<Force>> drag = new ArrayList<Amount<Force>>();
		List<Amount<Force>> friction = new ArrayList<Amount<Force>>();
		List<Amount<Force>> totalForce = new ArrayList<Amount<Force>>();
		List<Amount<Acceleration>> acceleration = new ArrayList<Amount<Acceleration>>();
		List<Amount<Velocity>> rateOfClimb = new ArrayList<Amount<Velocity>>();
		List<Amount<Length>> groundDistance = new ArrayList<Amount<Length>>();
		List<Amount<Length>> verticalDistance = new ArrayList<Amount<Length>>();
		List<Double> fuelUsed = new ArrayList<Double>();

		MyInterpolatingFunction alphaFunction = new MyInterpolatingFunction();
		alphaFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.alpha)
				);
		MyInterpolatingFunction loadFactorFunction = new MyInterpolatingFunction();
		loadFactorFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.loadFactor))
				);
		
		//#############################################################################
		// Collect the array of times and associated state vector values according
		// to the given dt and keeping the the discrete event-times (breakpoints)

		List<Amount<Duration>> times = new ArrayList<Amount<Duration>>();
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
					// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					// SPEED:
					speed.add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
					//----------------------------------------------------------------------------------------
					// THRUST:
					thrust.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).thrust(x[1], x[2], times.get(i).doubleValue(SI.SECOND), x[3]),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// THRUST HORIZONTAL:
					thrustHorizontal.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).thrust(x[1], x[2], times.get(i).doubleValue(SI.SECOND), x[3])*Math.cos(
									Amount.valueOf(
											alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// THRUST VERTICAL:
					thrustVertical.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).thrust(x[1], x[2], times.get(i).doubleValue(SI.SECOND), x[3])*Math.sin(
									Amount.valueOf(
											alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									),
							SI.NEWTON)
							);
					//--------------------------------------------------------------------------------
					// SFC:
					if(i==0)
						fuelUsed.add(0.0);
					else
						fuelUsed.add(
								fuelUsed.get(i-1) +
								(thrust.get(i-1).doubleValue(SI.NEWTON))
								*(0.224809)*(0.454/3600)
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
												NoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(0).getBPR(),
												NoiseTrajectoryCalc.this.getThePowerPlant().getEngineType(),
												EngineOperatingConditionEnum.TAKE_OFF,
												NoiseTrajectoryCalc.this.getThePowerPlant()
												),
										NoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(0).getBPR(),
										NoiseTrajectoryCalc.this.getThePowerPlant().getEngineType(),
										EngineOperatingConditionEnum.TAKE_OFF,
										NoiseTrajectoryCalc.this.getThePowerPlant()
										)
								);

					//--------------------------------------------------------------------------------
					// FRICTION:
					if(times.get(i).doubleValue(SI.SECOND) < tEndRot.getEstimatedValue())
						friction.add(Amount.valueOf(
								((DynamicsEquationsNoiseTrajectory)ode).mu(x[1])
								*(((DynamicsEquationsNoiseTrajectory)ode).weight
										- ((DynamicsEquationsNoiseTrajectory)ode).lift(
												x[1],
												alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
												x[2],
												times.get(i).doubleValue(SI.SECOND),
												x[3])),
								SI.NEWTON)
								);
					else
						friction.add(Amount.valueOf(0.0, SI.NEWTON));
					//----------------------------------------------------------------------------------------
					// LIFT:
					lift.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).lift(
									x[1],
									alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
									x[2],
									times.get(i).doubleValue(SI.SECOND),
									x[3]),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// DRAG:
					drag.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).drag(
									x[1],
									alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
									x[2],
									times.get(i).doubleValue(SI.SECOND),
									x[3]),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// TOTAL FORCE:
					totalForce.add(Amount.valueOf(
							((DynamicsEquationsNoiseTrajectory)ode).thrust(x[1], x[2], times.get(i).doubleValue(SI.SECOND), x[3])*Math.cos(
									Amount.valueOf(
											alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									)
							- ((DynamicsEquationsNoiseTrajectory)ode).drag(
									x[1],
									alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
									x[2],
									times.get(i).doubleValue(SI.SECOND),
									x[3])
							- ((DynamicsEquationsNoiseTrajectory)ode).mu(x[1])
							*(((DynamicsEquationsNoiseTrajectory)ode).weight
									- ((DynamicsEquationsNoiseTrajectory)ode).lift(
											x[1],
											alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
											x[2],
											times.get(i).doubleValue(SI.SECOND),
											x[3]))
							- ((DynamicsEquationsNoiseTrajectory)ode).weight*Math.sin(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					loadFactor.add(loadFactorFunction.value(times.get(i).doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// RATE OF CLIMB:
					rateOfClimb.add(Amount.valueOf(
							xDot[3],
							SI.METERS_PER_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					acceleration.add(Amount.valueOf(
							xDot[1],
							SI.METERS_PER_SQUARE_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// GROUND DISTANCE:
					groundDistance.add(Amount.valueOf(
							x[0],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// VERTICAL DISTANCE:
					verticalDistance.add(Amount.valueOf(
							x[3],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					alpha.add(Amount.valueOf(
							alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					gamma.add(Amount.valueOf(
							x[2],
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					if((times.get(i).doubleValue(SI.SECOND) > tRot.getEstimatedValue()) 
							&& (times.get(i).doubleValue(SI.SECOND) < tHold.getEstimatedValue())) {
						alphaDot.add(
								NoiseTrajectoryCalc.this.getAlphaDotInitial()
								*(1-(NoiseTrajectoryCalc.this.getkAlphaDot()
										*alphaFunction.value(times.get(i).doubleValue(SI.SECOND))
										)
									)
								);
					}
					else if((times.get(i).doubleValue(SI.SECOND) > tEndHold.getEstimatedValue())
							&& (times.get(i).doubleValue(SI.SECOND) < tZeroAccelration.getEstimatedValue())) {
						alphaDot.add(alphaRed);
					}
					else
						alphaDot.add(0.0);
					//----------------------------------------------------------------------------------------
					// GAMMA DOT:
					gammaDot.add(xDot[2]);
					//----------------------------------------------------------------------------------------
					// THETA:
					theta.add(Amount.valueOf(
							x[2] + alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					cL.add(
							((DynamicsEquationsNoiseTrajectory)ode).cL(
									x[1],
									alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
									x[2],
									times.get(i).doubleValue(SI.SECOND),
									x[3]
									)
							);
					//----------------------------------------------------------------------------------------
					// CD:
					cD.add(
							((DynamicsEquationsNoiseTrajectory)ode).cD(
									((DynamicsEquationsNoiseTrajectory)ode).cL(
											x[1],
											alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
											x[2],
											times.get(i).doubleValue(SI.SECOND),
											x[3]
											),
									times.get(i).doubleValue(SI.SECOND),
									x[1],
									x[3]
									)
							);

					//----------------------------------------------------------------------------------------
				}
			}
		}

		//.................................................................................
		// take-off trajectory
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
				0.0, null, 0.0, null,
				"Ground Distance", "Altitude", "m", "m",
				outputFolderPath, "Trajectory_SI");

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
				outputFolderPath, "Trajectory_IMPERIAL");

		//.................................................................................
		// vertical distance v.s. time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
//				0.0, null, 0.0, null,
//				"Time", "Altitude", "s", "m",
//				outputFolderPath, "Altitude_evolution_SI");
//
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						verticalDistance.stream()
//						.map(x -> x.to(NonSI.FOOT))
//						.collect(Collectors.toList())
//						),
//				0.0, null, 0.0, null,
//				"Time", "Altitude", "s", "ft",
//				outputFolderPath, "Altitude_evolution_IMPERIAL");

		//.................................................................................
		// speed v.s. time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(speed),
//				0.0, null, 0.0, null,
//				"Time", "Speed", "s", "m/s",
//				outputFolderPath, "Speed_evolution_SI");
//
//
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						speed.stream()
//						.map(x -> x.to(NonSI.KNOT))
//						.collect(Collectors.toList())
//						),
//				0.0, null, 0.0, null,
//				"Time", "Speed", "s", "kn",
//				outputFolderPath, "Speed_evolution_IMPERIAL");

		//.................................................................................
		// speed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(speed),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				outputFolderPath, "Speed_vs_GroundDistance_SI");

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speed.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "ft", "kn",
				outputFolderPath, "Speed_vs_GroundDistance_IMPERIAL");

		//.................................................................................
		// acceleration v.s. time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
//				0.0, null, null, null,
//				"Time", "Acceleration", "s", "m/(s^2)",
//				outputFolderPath, "Acceleration_evolution_SI");
//
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						acceleration.stream()
//						.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
//						.collect(Collectors.toList())
//						),
//				0.0, null, null, null,
//				"Time", "Acceleration", "s", "ft/(min^2)",
//				outputFolderPath, "Acceleration_evolution_IMPERIAL");

		//.................................................................................
		// acceleration v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
				0.0, null, null, null,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				outputFolderPath, "Acceleration_vs_GroundDistance_SI");

//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						groundDistance.stream()
//						.map(x -> x.to(NonSI.FOOT))
//						.collect(Collectors.toList())
//						),
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						acceleration.stream()
//						.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
//						.collect(Collectors.toList())
//						),
//				0.0, null, null, null,
//				"Ground Distance", "Acceleration", "ft", "ft/(min^2)",
//				outputFolderPath, "Acceleration_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// load factor v.s. time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertToDoublePrimitive(loadFactor),
//				0.0, null, 0.0, null,
//				"Time", "Load Factor", "s", "",
//				outputFolderPath, "LoadFactor_evolution");

		//.................................................................................
		// load factor v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
				MyArrayUtils.convertToDoublePrimitive(loadFactor),
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				outputFolderPath, "LoadFactor_vs_GroundDistance_SI");

//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						groundDistance.stream()
//						.map(x -> x.to(NonSI.FOOT))
//						.collect(Collectors.toList())
//						), 
//				MyArrayUtils.convertToDoublePrimitive(loadFactor),
//				0.0, null, 0.0, null,
//				"Ground distance", "Load Factor", "ft", "",
//				outputFolderPath, "LoadFactor_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// Rate of Climb v.s. Time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
//				0.0, null, 0.0, null,
//				"Time", "Rate of Climb", "s", "m/s",
//				outputFolderPath, "RateOfClimb_evolution_SI");
//
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						rateOfClimb.stream()
//						.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
//						.collect(Collectors.toList())
//						),
//				0.0, null, 0.0, null,
//				"Time", "Rate of Climb", "s", "ft/min",
//				outputFolderPath, "RateOfClimb_evolution_IMPERIAL");

		//.................................................................................
		// Rate of Climb v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
				MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
				0.0, null, 0.0, null,
				"Ground distance", "Rate of Climb", "m", "m/s",
				outputFolderPath, "RateOfClimb_vs_GroundDistance_SI");

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
				outputFolderPath, "RateOfClimb_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// CL v.s. Time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertToDoublePrimitive(cL),
//				0.0, null, 0.0, null,
//				"Time", "CL", "s", "",
//				outputFolderPath, "CL_evolution");

		//.................................................................................
		// CL v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertToDoublePrimitive(cL),
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				outputFolderPath, "CL_vs_GroundDistance_SI");

//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						groundDistance.stream()
//						.map(x -> x.to(NonSI.FOOT))
//						.collect(Collectors.toList())
//						),
//				MyArrayUtils.convertToDoublePrimitive(cL),
//				0.0, null, 0.0, null,
//				"Ground distance", "CL", "ft", "",
//				outputFolderPath, "CL_vs_GroundDistance_IMPERIAL");
		
//		//.................................................................................
//		// CD v.s. Time
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(times),
//				MyArrayUtils.convertToDoublePrimitive(cD),
//				0.0, null, 0.0, null,
//				"Time", "CL", "s", "",
//				outputFolderPath, "CD_evolution");

		//.................................................................................
		// CD v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
				MyArrayUtils.convertToDoublePrimitive(cD),
				0.0, null, 0.0, null,
				"Ground distance", "CD", "m", "",
				outputFolderPath, "CD_vs_GroundDistance_SI");
//
//		MyChartToFileUtils.plotNoLegend(
//				MyArrayUtils.convertListOfAmountTodoubleArray(
//						groundDistance.stream()
//						.map(x -> x.to(NonSI.FOOT))
//						.collect(Collectors.toList())
//						),
//				MyArrayUtils.convertToDoublePrimitive(cD),
//				0.0, null, 0.0, null,
//				"Ground distance", "CD", "ft", "",
//				outputFolderPath, "CD_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// Horizontal Forces v.s. Time
//		double[][] xMatrix1SI = new double[5][totalForce.size()];
//		for(int i=0; i<xMatrix1SI.length; i++)
//			xMatrix1SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix1SI = new double[5][totalForce.size()];
//		yMatrix1SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
//		yMatrix1SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontal);
//		yMatrix1SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
//		yMatrix1SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);
//		yMatrix1SI[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
//				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix1SI, yMatrix1SI,
//				0.0, null, null, null,
//				"Time", "Horizontal Forces", "s", "N",
//				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
//				outputFolderPath, "HorizontalForces_evolution_SI");
//
//		double[][] xMatrix1IMPERIAL = new double[5][totalForce.size()];
//		for(int i=0; i<xMatrix1IMPERIAL.length; i++)
//			xMatrix1IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix1IMPERIAL = new double[5][totalForce.size()];
//		yMatrix1IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				totalForce.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix1IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				thrustHorizontal.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix1IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				drag.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix1IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				friction.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix1IMPERIAL[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
//				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
//				.map(x -> x.times(0.224809))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix1IMPERIAL, yMatrix1IMPERIAL,
//				0.0, null, null, null,
//				"Time", "Horizontal Forces", "s", "lb",
//				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
//				outputFolderPath, "HorizontalForces_evolution_IMPERIAL");

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
				outputFolderPath, "HorizontalForces_vs_GroundDistance_SI");

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
				outputFolderPath, "HorizontalForces_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// Vertical Forces v.s. Time
//		double[][] xMatrix3SI = new double[3][totalForce.size()];
//		for(int i=0; i<xMatrix3SI.length; i++)
//			xMatrix3SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix3SI = new double[3][totalForce.size()];
//		yMatrix3SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
//		yMatrix3SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVertical);
//		yMatrix3SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
//				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix3SI, yMatrix3SI,
//				0.0, null, null, null,
//				"Time", "Vertical Forces", "s", "N",
//				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
//				outputFolderPath, "VerticalForces_evolution");
//
//		double[][] xMatrix3IMPERIAL = new double[3][totalForce.size()];
//		for(int i=0; i<xMatrix3IMPERIAL.length; i++)
//			xMatrix3IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix3IMPERIAL = new double[3][totalForce.size()];
//		yMatrix3IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				lift.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix3IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				thrustVertical.stream()
//				.map(x -> x.to(NonSI.POUND_FORCE))
//				.collect(Collectors.toList())
//				);
//		yMatrix3IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
//				.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
//				.map(x -> x.times(0.224809))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix3IMPERIAL, yMatrix3IMPERIAL,
//				0.0, null, null, null,
//				"Time", "Vertical Forces", "s", "lb",
//				new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
//				outputFolderPath, "VerticalForces_evolution_IMPERIAL");

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
				outputFolderPath, "VerticalForces_vs_GroundDistance_SI");

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
				outputFolderPath, "VerticalForces_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// Angles v.s. time
//		double[][] xMatrix5 = new double[3][totalForce.size()];
//		for(int i=0; i<xMatrix5.length; i++)
//			xMatrix5[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix5 = new double[3][totalForce.size()];
//		yMatrix5[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				alpha.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//		yMatrix5[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				theta.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//		yMatrix5[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix5, yMatrix5,
//				0.0, null, null, null,
//				"Time", "Angles", "s", "deg",
//				new String[] {"Alpha Body", "Theta", "Gamma"},
//				outputFolderPath, "Angles_evolution");

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
				outputFolderPath, "Angles_vs_GroundDistance_SI");
//
//		double[][] xMatrix6IMPERIAL = new double[3][totalForce.size()];
//		for(int i=0; i<xMatrix6IMPERIAL.length; i++)
//			xMatrix6IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
//					groundDistance.stream()
//					.map(x -> x.to(NonSI.FOOT))
//					.collect(Collectors.toList())
//					);
//
//		double[][] yMatrix6IMPERIAL = new double[3][totalForce.size()];
//		yMatrix6IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				alpha.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//		yMatrix6IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				theta.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//		yMatrix6IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
//				gamma.stream()
//				.map(x -> x.to(NonSI.DEGREE_ANGLE))
//				.collect(Collectors.toList())
//				);
//
//		MyChartToFileUtils.plot(
//				xMatrix6IMPERIAL, yMatrix6IMPERIAL,
//				0.0, null, null, null,
//				"Ground Distance", "Angles", "ft", "deg",
//				new String[] {"Alpha Body", "Theta", "Gamma"},
//				outputFolderPath, "Angles_vs_GroundDistance_IMPERIAL");

//		//.................................................................................
//		// Angular velocity v.s. time
//		double[][] xMatrix7 = new double[2][totalForce.size()];
//		for(int i=0; i<xMatrix7.length; i++)
//			xMatrix7[i] = MyArrayUtils.convertListOfAmountTodoubleArray(times);
//
//		double[][] yMatrix7 = new double[2][totalForce.size()];
//		yMatrix7[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
//		yMatrix7[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);
//
//		MyChartToFileUtils.plot(
//				xMatrix7, yMatrix7,
//				0.0, null, null, null,
//				"Time", "Angular Velocity", "s", "deg/s",
//				new String[] {"Alpha_dot", "Gamma_dot"},
//				outputFolderPath, "AngularVelocity_evolution");

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
				outputFolderPath, "AngularVelocity_vs_GroundDistance_SI");
//
//		double[][] xMatrix8IMPERIAL = new double[2][totalForce.size()];
//		for(int i=0; i<xMatrix8IMPERIAL.length; i++)
//			xMatrix8IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
//					groundDistance.stream()
//					.map(x -> x.to(NonSI.FOOT))
//					.collect(Collectors.toList())
//					);
//
//		double[][] yMatrix8SIMPERIAL = new double[2][totalForce.size()];
//		yMatrix8SIMPERIAL[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
//		yMatrix8SIMPERIAL[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);
//
//		MyChartToFileUtils.plot(
//				xMatrix8IMPERIAL, yMatrix8SIMPERIAL,
//				0.0, null, null, null,
//				"Ground Distance", "Angular Velocity", "ft", "deg/s",
//				new String[] {"Alpha_dot", "Gamma_dot"},
//				outputFolderPath, "AngularVelocity_vs_GroundDistance_SI");

		System.out.println("\n---------------------------DONE!-------------------------------");

	}

	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsNoiseTrajectory implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);

		// visible variables
		public double alpha, gamma, weight;

		public DynamicsEquationsNoiseTrajectory() {
			this.weight = NoiseTrajectoryCalc.this.getMaxTakeOffMass().doubleValue(SI.KILOGRAM)*this.g0;
		}

		@Override
		public int getDimension() {
			return 4;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			gamma = x[2];
			double altitude = x[3];
			double speed = x[1];
			alpha = alpha(t);

			if( t < tEndRot.getEstimatedValue()) {
				xDot[0] = speed;
				xDot[1] = (g0/weight)*(thrust(speed, gamma, t, altitude) - drag(speed, alpha, gamma, t, altitude)
						- (mu(speed)*(weight - lift(speed, alpha, gamma, t, altitude))));
				xDot[2] = 0.0;
				xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
			}
			else {
				xDot[0] = speed;
				xDot[1] = (g0/weight)*(
						thrust(speed, gamma,t, altitude)*Math.cos(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()) 
						- drag(speed, alpha, gamma, t, altitude) 
						- weight*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
				xDot[2] = 57.3*(g0/(weight*speed))*(
						lift(speed, alpha, gamma, t, altitude) 
						+ (thrust(speed, gamma, t, altitude)*Math.sin(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
						- weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
				xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
			}
		}

		public double thrust(double speed, double gamma, double time, double altitude) {

			double theThrust = ThrustCalc.calculateThrustDatabase(
					NoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
					NoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(),
					NoiseTrajectoryCalc.this.getPhi(),
					NoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(0).getBPR(),
					NoiseTrajectoryCalc.this.getThePowerPlant().getEngineType(),
					EngineOperatingConditionEnum.TAKE_OFF,
					NoiseTrajectoryCalc.this.getThePowerPlant(),
					altitude,
					SpeedCalc.calculateMach(
							altitude,
							speed + 
							(NoiseTrajectoryCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
									gamma,
									NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
							)
					);

			return theThrust;
		}

		public double cD(double cL, double time, double speed, double altitude) {

			double cD = 0.0;
			double cDi = Math.pow(cL, 2)
					/(Math.PI
							*NoiseTrajectoryCalc.this.getAspectRatio()
							*AerodynamicCalc.estimateOswaldFactorFormAircraftDragPolar(
									NoiseTrajectoryCalc.this.getPolarCLTakeOff(),
									NoiseTrajectoryCalc.this.getPolarCDTakeOff(),
									NoiseTrajectoryCalc.this.getAspectRatio())
							);
			
//			// McCormick interpolated function --> See the excel file into JPAD DOCS
//			double hb = altitude/(NoiseTrajectoryCalc.this.getSpan().times(Math.PI/4)).getEstimatedValue();
//			NoiseTrajectoryCalc.this.setkGround(- 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
//					+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055);
			
			// Biot-Savart law for the kGround (see McCormick pag.420)
			double hb = altitude/(NoiseTrajectoryCalc.this.getSpan().times(Math.PI/4)).getEstimatedValue();
			NoiseTrajectoryCalc.this.setkGround((Math.pow(16*hb, 2))/(1+(Math.pow(16*hb, 2))));
			
			if(time < tLandingGearRetractionStart.doubleValue(SI.SECOND)) {
				cD = MyMathUtils
						.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCLTakeOff()
										),
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCDTakeOff()
										),
								cL
								);
			}
			else if((time >= tLandingGearRetractionStart.doubleValue(SI.SECOND))
					&& (time < tLandingGearRetractionEnd.doubleValue(SI.SECOND))) {
				cD = MyMathUtils
						.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCLTakeOff()
										),
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCDTakeOff()
										),
								cL
								)
						- deltaCD0LandingGear*deltaCD0LandingGearRetractionSlope.value(time);
				
			}
			else {
				cD = MyMathUtils
						.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCLTakeOff()
										),
								MyArrayUtils.convertToDoublePrimitive(
										NoiseTrajectoryCalc.this.getPolarCDTakeOff()
										),
								cL
								)
						- deltaCD0LandingGear;
			}

			return cD - ((1 - NoiseTrajectoryCalc.this.getkGround())*cDi);
		}

		public double drag(double speed, double alpha, double gamma, double time, double altitude) {

			double cD = cD(cL(speed, alpha, gamma, time, altitude), time, speed, altitude);
			double drag = 0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude)
					*(Math.pow(speed 
							+ (NoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)
									*Math.cos(Amount.valueOf(
											gamma,
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											)
									),
							2)
							)
					*cD;
			
			return drag;
		}

		public double cL(double speed, double alpha, double gamma ,double time, double altitude) {

				double cL0 = NoiseTrajectoryCalc.this.cL0;
				double cLalpha = NoiseTrajectoryCalc.this.getcLalphaFlap().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				double alphaWing = alpha + NoiseTrajectoryCalc.this.getIw().getEstimatedValue();

				return cL0 + (cLalpha*alphaWing);

		}

		public double lift(double speed, double alpha, double gamma, double time, double altitude) {

			double cL = cL(speed, alpha, gamma, time, altitude);
			double lift = 0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude)
					*(Math.pow(speed + 
							(NoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(Amount.valueOf(
									gamma,
									NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
									)
									),
							2)
							)
					*cL;
			
			return lift;
		}

		public double mu(double speed) {
			return mu.value(speed);
		}

		public double alphaDot(double time) {

			double alphaDot = 0.0;

			if((time > tRot.doubleValue(SI.SECOND)) && (time < tHold.doubleValue(SI.SECOND))) {
				alphaDot = NoiseTrajectoryCalc.this.getAlphaDotInitial()
						*(1-(NoiseTrajectoryCalc.this.getkAlphaDot()*(NoiseTrajectoryCalc.this.getAlpha().get(
								NoiseTrajectoryCalc.this.getAlpha().size()-1).getEstimatedValue()))
								);
			}
			else if((time > tEndHold.doubleValue(SI.SECOND)) && (time <= tZeroAccelration.doubleValue(SI.SECOND))) 
				alphaDot = alphaRed;
			
			return alphaDot;
		}

		public double alpha(double time) {

			double alpha = NoiseTrajectoryCalc.this.getAlphaGround().getEstimatedValue();
			
			if(time > tRot.getEstimatedValue())
				alpha = NoiseTrajectoryCalc.this.getAlpha().get(
						NoiseTrajectoryCalc.this.getAlpha().size()-1).getEstimatedValue()
				+(alphaDot(time)*(NoiseTrajectoryCalc.this.getTime().get(
						NoiseTrajectoryCalc.this.getTime().size()-1).getEstimatedValue()
						- NoiseTrajectoryCalc.this.getTime().get(
								NoiseTrajectoryCalc.this.getTime().size()-2).getEstimatedValue()));

			return alpha;
		}
	}
	//-------------------------------------------------------------------------------------
	//									END NESTED CLASS	
	//-------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:

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

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
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

	public double getkRot() {
		return kRot;
	}

	public double getKclMax() {
		return kcLMax;
	}

	public double getPhi() {
		return phi;
	}

	public Amount<Velocity> getV1() {
		return v1;
	}

	public void setV1(Amount<Velocity> v1) {
		this.v1 = v1;
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

	public Amount<Duration> gettClimb() {
		return tClimb;
	}

	public void setkRot(double kRot) {
		this.kRot = kRot;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public Amount<?> getcLalphaFlap() {
		return cLalphaFlap;
	}

	public void setcLalphaFlap(Amount<?> cLalphaFlap) {
		this.cLalphaFlap = cLalphaFlap;
	}

	public Amount<Mass> getMaxTakeOffMass() {
		return maxTakeOffMass;
	}

	public void setMaxTakeOffMass(Amount<Mass> maxTakeOffMass) {
		this.maxTakeOffMass = maxTakeOffMass;
	}

	public Amount<Velocity> getV2() {
		return v2;
	}

	public void setV2(Amount<Velocity> v2) {
		this.v2 = v2;
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

	public Double[] getPolarCLTakeOff() {
		return polarCLTakeOff;
	}

	public void setPolarCLTakeOff(Double[] polarCLTakeOff) {
		this.polarCLTakeOff = polarCLTakeOff;
	}

	public Double[] getPolarCDTakeOff() {
		return polarCDTakeOff;
	}

	public void setPolarCDTakeOff(Double[] polarCDTakeOff) {
		this.polarCDTakeOff = polarCDTakeOff;
	}

	public FirstOrderIntegrator getTheIntegrator() {
		return theIntegrator;
	}

	public void setTheIntegrator(FirstOrderIntegrator theIntegrator) {
		this.theIntegrator = theIntegrator;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public List<Double> getTimeBreakPoints() {
		return timeBreakPoints;
	}

	public void setTimeBreakPoints(List<Double> timeBreakPoints) {
		this.timeBreakPoints = timeBreakPoints;
	}

	public Amount<Length> getxEndSimulation() {
		return xEndSimulation;
	}

	public void setxEndSimulation(Amount<Length> xEndSimulation) {
		this.xEndSimulation = xEndSimulation;
	}

	public Amount<Duration> gettLandingGearRetractionStart() {
		return tLandingGearRetractionStart;
	}

	public void settLandingGearRetractionStart(Amount<Duration> tLandingGearRetractionStart) {
		this.tLandingGearRetractionStart = tLandingGearRetractionStart;
	}

	public Amount<Duration> gettLandingGearRetractionEnd() {
		return tLandingGearRetractionEnd;
	}

	public void settLandingGearRetractionEnd(Amount<Duration> tLandingGearRetractionEnd) {
		this.tLandingGearRetractionEnd = tLandingGearRetractionEnd;
	}

	public Amount<Duration> getDtLandingGearRetraction() {
		return dtLandingGearRetraction;
	}

	public void setDtLandingGearRetraction(Amount<Duration> dtLandingGearRetraction) {
		this.dtLandingGearRetraction = dtLandingGearRetraction;
	}

	public Double getDeltaCD0LandingGear() {
		return deltaCD0LandingGear;
	}

	public void setDeltaCD0LandingGear(Double deltaCD0LandingGear) {
		this.deltaCD0LandingGear = deltaCD0LandingGear;
	}

	public Double getDeltaCD0OEI() {
		return deltaCD0OEI;
	}

	public void setDeltaCD0OEI(Double deltaCD0OEI) {
		this.deltaCD0OEI = deltaCD0OEI;
	}

	public MyInterpolatingFunction getDeltaCD0LandingGearRetractionSlope() {
		return deltaCD0LandingGearRetractionSlope;
	}

	public void setDeltaCD0LandingGearRetractionSlope(MyInterpolatingFunction deltaCD0LandingGearRetractionSlope) {
		this.deltaCD0LandingGearRetractionSlope = deltaCD0LandingGearRetractionSlope;
	}

	public Amount<Duration> gettVClimb() {
		return tVClimb;
	}

	public void settVClimb(Amount<Duration> tVClimb) {
		this.tVClimb = tVClimb;
	}

	public Amount<Duration> gettObstacle() {
		return tObstacle;
	}

	public void settObstacle(Amount<Duration> tObstacle) {
		this.tObstacle = tObstacle;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public Amount<Duration> gettZeroAccelration() {
		return tZeroAccelration;
	}

	public void settZeroAccelration(Amount<Duration> tZeroAccelration) {
		this.tZeroAccelration = tZeroAccelration;
	}
}