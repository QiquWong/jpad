package calculators.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * This class have the purpose of calculating the take-off trajectories for the noise certification
 * of a given aircraft by evaluating the following cases:
 *
 * - take-off in ISA+10°C with MAX TAKE-OFF, Landing Gear retraction and a V2=1.2VsTO + (10 or 20 kts)
 * - take-off in ISA+10°C with MAX TAKE-OFF, Landing Gear retraction, a V2=1.2VsTO + (10 or 20 kts) and 
 *   a reduction of the thrust starting from 984ft assuming the lowest throttle setting between:
 *   	
 *   	- the setting which leads to a constant CGR of 4%
 *   	- the setting which in OEI (considering a DeltaCD0OEI) leads to a leveled flight
 *   
 * - take-off in ISA+10°C with MAX TAKE-OFF setting, Landing Gear retraction, a V2=1.2VsTO + (10 or 20 kts) 
 *   and a reduction of the thrust starting from 984ft assuming a set of throttle setting between 1.0 and 
 *   the one found at the previous step
 *
 * for each of them a step by step integration is used in solving the dynamic equation.
 *
 * @author Vittorio Trifari
 *
 */

public class TakeOffNoiseTrajectoryCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private static final Amount<Temperature> deltaTemperature = Amount.valueOf(10.0, SI.CELSIUS);
	
	private boolean createCSV;
	private boolean targetSpeedFlag;
	private double aspectRatio;
	private Amount<Area> surface; 
	private Amount<Length> span;
	private PowerPlant thePowerPlant;
	private double[] polarCLTakeOff;
	private double[] polarCDTakeOff;
	private Amount<Duration> dtHold, dtLandingGearRetraction, dtThrustCutback,
	tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
	tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tLandingGearRetractionStart = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tLandingGearRetractionEnd = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tObstacle = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tZeroAcceleration = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tCutback = Amount.valueOf(10000, SI.SECOND); // initialization to an impossible time
	private Amount<Mass> maxTakeOffMass; 
	private Amount<Velocity> vSTakeOff, vRot, vLO, vWind, v1, v2, vClimb;
	private Amount<Length> wingToGroundDistance, obstacle, xEndSimulation, cutbackAltitude;
	private Amount<Angle> alphaGround, fuselageUpsweepAngle;
	private double kAlphaDot, kcLMax, kRot, phi, cLmaxTO, alphaDotInitial, deltaCD0LandingGear, deltaCD0OEI, 
	alphaRed, cL0, thrustCorrectionFactor, sfcCorrectionFactor;
	private Double phiCutback;
	private Amount<?> cLalphaFlap;
	private MyInterpolatingFunction mu, deltaCD0LandingGearRetractionSlope, deltaThrustCutbackSlope;
	
	private boolean isTailStrike;
	private boolean rotationSpeedWarningFlag;
	
	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;

	// STEP HANDLERS LISTS
	private List<Amount<Velocity>> speedPerStep, rateOfClimbPerStep;
	private List<Amount<Force>> thrustPerStep, thrustHorizontalPerStep, thrustVerticalPerStep,
	liftPerStep, dragPerStep, totalForcePerStep, frictionPerStep;
	private List<Amount<Angle>> alphaPerStep, gammaPerStep, thetaPerStep;
	private List<Double> alphaDotPerStep, gammaDotPerStep, cLPerStep, loadFactorPerStep, cDPerStep, timeBreakPoints;
	private List<Amount<Acceleration>> accelerationPerStep;
	private List<Amount<Length>> groundDistancePerStep, verticalDistancePerStep;
	private List<Amount<Duration>> timePerStep;
	private List<Amount<Mass>> fuelUsedPerStep;
	private List<Amount<Force>> weightPerStep;
	
	// OUTPUTS MAPS:
	private Map<Double, List<Amount<Velocity>>> speedMap, rateOfClimbMap;
	private Map<Double, List<Amount<Force>>> thrustMap, thrustHorizontalMap, thrustVerticalMap,
	liftMap, dragMap, totalForceMap, frictionMap;
	private Map<Double, List<Amount<Angle>>> alphaMap, gammaMap, thetaMap;
	private Map<Double, List<Double>> alphaDotMap, gammaDotMap, cLMap, loadFactorMap, cDMap;
	private Map<Double, List<Amount<Acceleration>>> accelerationMap;
	private Map<Double, List<Amount<Length>>> groundDistanceMap, verticalDistanceMap;
	private Map<Double, List<Amount<Duration>>> timeMap;
	private Map<Double, List<Amount<Mass>>> fuelUsedMap;
	private Map<Double, List<Amount<Force>>> weightMap;
	
	private Map<Double, Amount<Length>> certificationPointsGroundDistanceMap;
	private Map<Double, Amount<Length>> certificationPointsAltitudeMap;
	private Map<Double, Amount<Velocity>> certificationPointsSpeedTASMap;
	private Map<Double, Amount<Velocity>> certificationPointsSpeedCASMap;
	private Map<Double, Amount<Angle>> certificationPointsAlphaMap;
	private Map<Double, Amount<Angle>> certificationPointsGammaMap;
	private Map<Double, Amount<Angle>> certificationPointsThetaMap;	
	private Map<Double, Amount<Force>> certificationPointsThrustMap; 
	
	//-------------------------------------------------------------------------------------
	// BUILDER:

	public TakeOffNoiseTrajectoryCalc(
			Amount<Length> xEndSimulation,
			Amount<Length> cutbackAltitude,
			Amount<Mass> maxTakeOffMass,
			PowerPlant thePowerPlant,
			double[] polarCLTakeOff,
			double[] polarCDTakeOff,
			Amount<Length> wingToGroundDistance,
			double deltaCD0LandingGear,
			double deltaCD0OEI,
			double aspectRatio,
			Amount<Area> surface,
			Amount<Angle> fuselageUpsweepAngle,
			Amount<Duration> dtHold,
			Amount<Duration> dtLandingGearRetraction,
			Amount<Duration> dtThrustCutback,
			double kcLMax,
			double kRot,
			double alphaDotInitial,
			double kAlphaDot,
			MyInterpolatingFunction mu,
			double cLmaxTO,
			double cLZeroTO,
			Amount<?> cLalphaFlap,
			double thrustCorrectionFactor,
			double sfcCorrectionFactor,
			boolean createCSV
			) {

		this.createCSV = createCSV;

		// Required data
		this.xEndSimulation = xEndSimulation;
		this.cutbackAltitude = cutbackAltitude;
		this.aspectRatio = aspectRatio;
		this.surface = surface;
		this.span = Amount.valueOf(
				Math.sqrt(aspectRatio*surface.doubleValue(SI.SQUARE_METRE)),
				SI.METER
				);
		this.fuselageUpsweepAngle = fuselageUpsweepAngle;
		this.thePowerPlant = thePowerPlant;
		this.polarCLTakeOff = polarCLTakeOff;
		this.polarCDTakeOff = polarCDTakeOff;
		this.deltaCD0LandingGear = deltaCD0LandingGear;
		this.wingToGroundDistance = wingToGroundDistance;
		this.deltaCD0OEI = deltaCD0OEI;
		this.maxTakeOffMass = maxTakeOffMass;
		this.dtHold = dtHold;
		this.dtLandingGearRetraction = dtLandingGearRetraction;
		this.dtThrustCutback = dtThrustCutback;
		this.kcLMax = kcLMax;
		this.kRot = kRot;
		this.alphaDotInitial = alphaDotInitial;
		this.kAlphaDot = kAlphaDot;
		this.phi = 1.0;
		this.mu = mu;
		this.obstacle = Amount.valueOf(35, NonSI.FOOT);
		this.vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		this.alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		this.cLmaxTO = cLmaxTO;
		this.cLalphaFlap = cLalphaFlap;
		this.cL0 = cLZeroTO;
		this.deltaCD0LandingGearRetractionSlope = new MyInterpolatingFunction();
		this.deltaThrustCutbackSlope = new MyInterpolatingFunction();
		this.thrustCorrectionFactor = thrustCorrectionFactor;
		this.sfcCorrectionFactor = sfcCorrectionFactor;

		// Reference velocities definition
		vSTakeOff = SpeedCalc.calculateSpeedStall(
				Amount.valueOf(0.0, SI.METER),  // SEA LEVEL
				Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
				maxTakeOffMass,
				surface,
				cLmaxTO
				);
		vRot = vSTakeOff.times(kRot);

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxTO = " + cLmaxTO);
		System.out.println("CL0 = " + cLZeroTO);
		System.out.println("VsTO = " + vSTakeOff);
		System.out.println("VRot = " + vRot);
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.speedPerStep = new ArrayList<>();
		this.rateOfClimbPerStep = new ArrayList<>();
		this.thrustPerStep = new ArrayList<>();
		this.thrustHorizontalPerStep = new ArrayList<>();
		this.thrustVerticalPerStep = new ArrayList<>();
		this.liftPerStep = new ArrayList<>();
		this.dragPerStep = new ArrayList<>();
		this.totalForcePerStep = new ArrayList<>();
		this.frictionPerStep = new ArrayList<>();
		this.alphaPerStep = new ArrayList<>();
		this.gammaPerStep = new ArrayList<>();
		this.thetaPerStep = new ArrayList<>();
		this.alphaDotPerStep = new ArrayList<>();
		this.gammaDotPerStep = new ArrayList<>();
		this.thetaPerStep = new ArrayList<>();
		this.alphaDotPerStep = new ArrayList<>();
		this.gammaDotPerStep = new ArrayList<>();
		this.cLPerStep = new ArrayList<>();
		this.loadFactorPerStep = new ArrayList<>();
		this.cDPerStep = new ArrayList<>();
		this.accelerationPerStep = new ArrayList<>();
		this.groundDistancePerStep = new ArrayList<>();
		this.verticalDistancePerStep = new ArrayList<>();
		this.timePerStep = new ArrayList<>();
		this.fuelUsedPerStep = new ArrayList<>();
		this.weightPerStep = new ArrayList<>();
		this.timeBreakPoints = new ArrayList<>();
		
		// Output maps initialization
		this.timeMap = new HashMap<>();
		this.speedMap = new HashMap<>();
		this.thrustMap = new HashMap<>();
		this.thrustHorizontalMap = new HashMap<>();
		this.thrustVerticalMap = new HashMap<>();
		this.alphaMap = new HashMap<>();
		this.alphaDotMap = new HashMap<>();
		this.gammaMap = new HashMap<>();
		this.gammaDotMap = new HashMap<>();
		this.thetaMap = new HashMap<>();
		this.cLMap =  new HashMap<>();
		this.liftMap = new HashMap<>();
		this.loadFactorMap = new HashMap<>();
		this.cDMap = new HashMap<>();
		this.dragMap = new HashMap<>();
		this.frictionMap = new HashMap<>();
		this.totalForceMap = new HashMap<>();
		this.accelerationMap = new HashMap<>();
		this.rateOfClimbMap = new HashMap<>();
		this.groundDistanceMap = new HashMap<>();
		this.verticalDistanceMap = new HashMap<>();
		this.fuelUsedMap = new HashMap<>();
		this.weightMap = new HashMap<>();
		
		this.certificationPointsGroundDistanceMap = new HashMap<>();
		this.certificationPointsAltitudeMap = new HashMap<>();
		this.certificationPointsSpeedTASMap = new HashMap<>();
		this.certificationPointsSpeedCASMap = new HashMap<>();
		this.certificationPointsAlphaMap = new HashMap<>();
		this.certificationPointsGammaMap = new HashMap<>();
		this.certificationPointsThetaMap = new HashMap<>();
		this.certificationPointsThrustMap = new HashMap<>();
		
		

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
		speedPerStep.clear();
		rateOfClimbPerStep.clear();
		thrustPerStep.clear();
		thrustHorizontalPerStep.clear();
		thrustVerticalPerStep.clear();
		liftPerStep.clear();
		dragPerStep.clear();
		totalForcePerStep.clear();
		frictionPerStep.clear();
		alphaPerStep.clear();
		gammaPerStep.clear();
		thetaPerStep.clear();
		alphaDotPerStep.clear();
		gammaDotPerStep.clear();
		thetaPerStep.clear();
		alphaDotPerStep.clear();
		gammaDotPerStep.clear();
		cLPerStep.clear();
		loadFactorPerStep.clear();
		cDPerStep.clear();
		accelerationPerStep.clear();
		groundDistancePerStep.clear();
		verticalDistancePerStep.clear();
		timePerStep.clear();
		fuelUsedPerStep.clear();
		weightPerStep.clear();
		timeBreakPoints.clear();

		tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tLandingGearRetractionStart = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tLandingGearRetractionEnd = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
		tCutback = Amount.valueOf(10000, SI.SECOND);	// initialization to an impossible time
		tObstacle = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
		tZeroAcceleration = Amount.valueOf(10000.0, SI.SECOND);	// initialization to an impossible time
		
		isTailStrike = false;
		phiCutback = null;
		
	}

	/***************************************************************************************
	 * This method performs the integration of the equation of motion by solving a set of
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateNoiseTakeOffTrajectory(boolean cutback, Double phiCutback, boolean timeHistories, Amount<Velocity> vMC) {

		System.out.println("---------------------------------------------------");
		System.out.println("NoiseTrajectoryCalc :: TAKE-OFF ODE integration :: cutback = " + cutback + ":: throttle cutback = " + phiCutback + "\n\n");
		System.out.println("\tRUNNING SIMULATION ...\n\n");

		int i=0;
		double newAlphaRed = 0.0;
		alphaRed = 0.0;

		targetSpeedFlag = false;
		StepHandler continuousOutputModel = null;
		rotationSpeedWarningFlag = false;

		if(vMC != null) {
			if(1.05*vMC.doubleValue(SI.METERS_PER_SECOND) > (kRot*vSTakeOff.doubleValue(SI.METERS_PER_SECOND))
					) {
				rotationSpeedWarningFlag = true;
				vRot = vMC.to(SI.METERS_PER_SECOND).times(1.05);
			}
			else
				vRot = vSTakeOff.to(SI.METERS_PER_SECOND).times(kRot);
		}
		else
			vRot = vSTakeOff.to(SI.METERS_PER_SECOND).times(kRot);
		
		vClimb = Amount.valueOf(10000, SI.METERS_PER_SECOND); // initialization to impossible values
		
		while (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
				> (1.13 
						+ Amount.valueOf(20, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
						)
				|| vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
				< (1.13 
						+ Amount.valueOf(10, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
						)
				) {

			if(i >= 1) {
				if(newAlphaRed <= 0.0)
					alphaRed = newAlphaRed;
				else
					break;
			}

			if(i > 25) {
				System.err.println("WARNING: (SIMULATION - NOISE TRAJECTORY TAKE-OFF) MAXIMUM NUMBER OF ITERATION REACHED. THE LAST VALUE OF V2 WILL BE CONSIDERED. "
						+ "(V2 = " + v2.to(SI.METERS_PER_SECOND) + "; V2/VsTO = " + v2.to(SI.METERS_PER_SECOND).divide(vSTakeOff.to(SI.METERS_PER_SECOND)));
				break;
			}

			initialize();

			theIntegrator = new HighamHall54Integrator(
					1e-10,
					1,
					1e-3,
					1e-3
					);
			ode = new DynamicsEquationsTakeOffNoiseTrajectory();

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
						return speed - vRot.doubleValue(SI.METERS_PER_SECOND);
					else
						return 10; // a generic positive value used to make the event triggers only once
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
					tRot = Amount.valueOf(t, SI.SECOND);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return Action.CONTINUE;
				}
			};
			EventHandler ehCheckSidelineCertificationPoint = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					return x[3] - 650.0; // 650m sideline altitude
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tSIDELINE CERTIFICATION POINT REACHED :: COLLECTING RESULTS ");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					
					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Length> distance = Amount.valueOf(x[0], SI.METER);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf( 
							(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(
							time, 
							speed,
							altitude, 
							Amount.valueOf(10.0, SI.CELSIUS), 
							gamma, 
							weight
							);
					Amount<Force> thrust = Amount.valueOf( 
							((DynamicsEquationsTakeOffNoiseTrajectory)ode).thrust(
									speed, 
									time,
									gamma,
									altitude, 
									Amount.valueOf(10.0, SI.CELSIUS)
									).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON
							);
					
					certificationPointsGroundDistanceMap.put(1.0, distance);
					certificationPointsAltitudeMap.put(1.0, altitude);
					certificationPointsSpeedTASMap.put(1.0, speed);
					certificationPointsSpeedCASMap.put(1.0, speed.times(AtmosphereCalc.getDensity(650.0, 10.0)/1.225));  // density at 650m and ISA+10°C
					certificationPointsAlphaMap.put(1.0, alpha);
					certificationPointsGammaMap.put(1.0, gamma);
					certificationPointsThetaMap.put(1.0, alpha.to(NonSI.DEGREE_ANGLE).plus(gamma.to(NonSI.DEGREE_ANGLE)));
					certificationPointsThrustMap.put(1.0, thrust);
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return Action.CONTINUE;
				}
			};
			EventHandler ehCheckFlyoverCertificationPoint = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					return x[0] - 6500.0; // 6500m flyover microphone position
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFLYOVER CERTIFICATION POINT REACHED :: COLLECTING RESULTS ");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					
					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Length> distance = Amount.valueOf(x[0], SI.METER);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf( 
							(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(
							time, 
							speed,
							altitude, 
							Amount.valueOf(10.0, SI.CELSIUS), 
							gamma, 
							weight
							);
					Amount<Force> thrust = Amount.valueOf( 
							((DynamicsEquationsTakeOffNoiseTrajectory)ode).thrust(
									speed, 
									time,
									gamma,
									altitude, 
									Amount.valueOf(10.0, SI.CELSIUS)
									).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON
							);
					
					certificationPointsGroundDistanceMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, distance);
					certificationPointsAltitudeMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, altitude);
					certificationPointsSpeedTASMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, speed);
					certificationPointsSpeedCASMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, speed.times(AtmosphereCalc.getDensity(650.0, 10.0)/1.225));  // density at 650m and ISA+10°C
					certificationPointsAlphaMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, alpha);
					certificationPointsGammaMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, gamma);
					certificationPointsThetaMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, alpha.to(NonSI.DEGREE_ANGLE).plus(gamma.to(NonSI.DEGREE_ANGLE)));
					certificationPointsThrustMap.put(TakeOffNoiseTrajectoryCalc.this.phiCutback, thrust);
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return Action.CONTINUE;
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

					return Action.CONTINUE;
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
						return 10.0; /* Generic positive value to trigger the event only one time */
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
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					v2 = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					System.out.println("\n\tV2/VsTO = " + v2.divide(vSTakeOff));
					tObstacle = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return Action.CONTINUE;
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
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
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
					if(t < tLandingGearRetractionStart.doubleValue(SI.SECOND))
						return x[3] - obstacle.doubleValue(SI.METER);
					else
						return 10.0; /* Generic positive value to trigger the event only one time */
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					System.out.println("\n\t\t LANDING GEAR RETRACTION :: START");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" + 
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
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
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);
					timeBreakPoints.add(t);
					tLandingGearRetractionEnd = Amount.valueOf(t, SI.SECOND);
					System.out.println("\n---------------------------DONE!-------------------------------\n");
					return Action.CONTINUE;
				}

				@Override
				public void resetState(double t, double[] x) {
				}

			};
			EventHandler ehCheckCutbackAltitude = new EventHandler() {

				@Override
				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public double g(double t, double[] x) {
					if(t < tCutback.doubleValue(SI.SECOND))
						return x[3] - cutbackAltitude.doubleValue(SI.METER);
					else
						return 10.0; /* Generic positive value to trigger the event only one time */
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					System.out.println("\n\t\t CUTBACK ALTITUDE REACHED :: DECREASING THRUST ... ");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);
					timeBreakPoints.add(t);
					tCutback = Amount.valueOf(t, SI.SECOND);

					if(cutback == true && phiCutback == null){
						/* 
						 * CHECKING THE GREATEST THRUST SETTING BETWEEN:
						 *  - THE ONE REQUIRED FOR A CLIMB GRADIENT OF 4%
						 *  - THE ONE REQUIRED FOR A LEVEL FLIGHT IN OEI CONDITION
						 */
						/////////////////////////////////////////////////////////////////////////////
						// CGR = 4%
						Amount<Force> thrustRequiredCGR4Percent = Amount.valueOf(
								0.04 // climb gradient, i.e. flight path angle gamma in radians
								* ((maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
								+ ((DynamicsEquationsTakeOffNoiseTrajectory)ode).drag(
										Amount.valueOf(x[1], SI.METERS_PER_SECOND),
										((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(
												Amount.valueOf(t, SI.SECOND),
												Amount.valueOf(x[1], SI.METERS_PER_SECOND),
												Amount.valueOf(x[3], SI.METER),
												Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
												Amount.valueOf(x[2], NonSI.DEGREE_ANGLE), 
												Amount.valueOf(
														((maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])
																*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)),
														SI.NEWTON)
												),
										Amount.valueOf(x[2], NonSI.DEGREE_ANGLE),
										Amount.valueOf(t, SI.SECOND),
										Amount.valueOf(x[3], SI.METER),
										Amount.valueOf(10, SI.CELSIUS) // ISA+10°C
										).doubleValue(SI.NEWTON),
								SI.NEWTON
								);
						Amount<Force> totalT0 = Amount.valueOf(0.0, SI.NEWTON);
						for(int i=0; i<thePowerPlant.getEngineNumber(); i++)
							totalT0 = Amount.valueOf(
									totalT0.doubleValue(SI.NEWTON) 
									+ ThrustCalc.calculateThrustDatabase(
											TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
											TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i),
											EngineOperatingConditionEnum.TAKE_OFF,
											Amount.valueOf(x[3], SI.METER),
											SpeedCalc.calculateMach(
													Amount.valueOf(x[3], SI.METER),
													Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
													Amount.valueOf(
															x[1] + 
															(TakeOffNoiseTrajectoryCalc.this.getvWind().getEstimatedValue()*Math.cos(Amount.valueOf(
																	x[2],
																	NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 
															SI.METERS_PER_SECOND
															)
													),
											Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
											TakeOffNoiseTrajectoryCalc.this.getPhi(),
											TakeOffNoiseTrajectoryCalc.this.getThrustCorrectionFactor(),
											thePowerPlant.getEngineList().get(i).getEngineType(),
											thePowerPlant.getEngineList().get(i).getEtaPropeller()
											).doubleValue(SI.NEWTON),
									SI.NEWTON
									);
						double phiCutback1 = thrustRequiredCGR4Percent.doubleValue(SI.NEWTON) / totalT0.doubleValue(SI.NEWTON);
						System.out.println("\n\tThrottle setting for CGR=4% = " + phiCutback1);
						/////////////////////////////////////////////////////////////////////////////
						// LEVEL FLIGHT OEI
						double cDOEI = 
								((DynamicsEquationsTakeOffNoiseTrajectory)ode).cD(
										((DynamicsEquationsTakeOffNoiseTrajectory)ode).cL(
												((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(
														Amount.valueOf(t, SI.SECOND),
														Amount.valueOf(x[1], SI.METERS_PER_SECOND),
														Amount.valueOf(x[3], SI.METER),
														Amount.valueOf(10, SI.CELSIUS), // ISA+10°C
														Amount.valueOf(x[2], NonSI.DEGREE_ANGLE), 
														Amount.valueOf(
																((maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])
																		*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)),
																SI.NEWTON)
														)
												),
										Amount.valueOf(t, SI.SECOND),
										Amount.valueOf(x[1], SI.METERS_PER_SECOND), 
										Amount.valueOf(x[3], SI.METER)
										)
								+ deltaCD0OEI;
						double dragOEI = 
								0.5
								*AtmosphereCalc.getDensity(x[3], 10 /* ISA+10°C according to FAR36 */)
								*Math.pow(vClimb.doubleValue(SI.METERS_PER_SECOND),2)
								*surface.doubleValue(SI.SQUARE_METRE)
								*cDOEI;

						// dragOEI is the A/C aerodynamic drag with a OEI 
						// (includes Delta drag for the inoperative engine nacelle).
						// dragOEI is also the total required thrust for a level flight in OEI conditions.
						// Hence: thrustOEI := dragOEI 
						// Regulations say: apply the thrustOEI to all engines (all operative). 
						// Hence:						
						double thrustRequiredOEI = thePowerPlant.getEngineNumber()*dragOEI;

						double phiCutback2 = thrustRequiredOEI / totalT0.doubleValue(SI.NEWTON);
						System.out.println("\tThrottle setting for level flight OEI = " + phiCutback2);
						/////////////////////////////////////////////////////////////////////////////
						TakeOffNoiseTrajectoryCalc.this.setPhiCutback(Math.max(phiCutback1, phiCutback2)); // as per FAR36
						System.out.println("\n\tThrottle setting for cutback trajectory = " + TakeOffNoiseTrajectoryCalc.this.getPhiCutback());
					}
					if(cutback == true && phiCutback != null) {
						TakeOffNoiseTrajectoryCalc.this.setPhiCutback(phiCutback);
						System.out.println("\n\tThrottle setting for cutback trajectory = " + TakeOffNoiseTrajectoryCalc.this.getPhiCutback());
					}
					deltaThrustCutbackSlope.interpolateLinear(
							new double[] {
									tCutback.doubleValue(SI.SECOND), 
									tCutback.plus(dtThrustCutback).doubleValue(SI.SECOND)
							}, 
							new double[] {1, TakeOffNoiseTrajectoryCalc.this.getPhiCutback()}
							);
					System.out.println("\n---------------------------DONE!-------------------------------\n");
					return Action.CONTINUE;
				}

				@Override
				public void resetState(double t, double[] x) {
				}

			};

			if(!cutback) {
				theIntegrator.addEventHandler(ehCheckVRot, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehEndConstantCL, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckObstacle, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehLandingGearRetractionStart, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehLandingGearRetractionEnd, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckXEndSimulation, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckSidelineCertificationPoint, 1e-1, 1e-3, 20);
			}
			else {
				theIntegrator.addEventHandler(ehCheckVRot, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehEndConstantCL, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckObstacle, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehLandingGearRetractionStart, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehLandingGearRetractionEnd, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckXEndSimulation, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckCutbackAltitude, 1e-1, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFlyoverCertificationPoint, 1e-1, 1e-3, 20);
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

					if(TakeOffNoiseTrajectoryCalc.this.timePerStep.isEmpty()) 
						handleStepImplementation(t, x, xDot);

					if(t > TakeOffNoiseTrajectoryCalc.this.timePerStep.get(TakeOffNoiseTrajectoryCalc.this.timePerStep.size()-1).doubleValue(SI.SECOND)) 
						handleStepImplementation(t, x, xDot);
				}
			};
			theIntegrator.addStepHandler(stepHandler);

			//##############################################################################################
			// Use this handler for post-processing

			System.out.println("=================================================");
			System.out.println("Integration " + (i+1) + " - AlphaRed = " + alphaRed + "°/s\n\n");
			continuousOutputModel = new ContinuousOutputModel();
			theIntegrator.addStepHandler(continuousOutputModel);

			//##############################################################################################

			double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0, 0.0}; // initial state
			theIntegrator.integrate(ode, 0.0, xAt0, 1000, xAt0); // now xAt0 contains final state

			if (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
					<= (1.13 
							+ Amount.valueOf(20, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
							)
					&& vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
					>= (1.13 
							+ Amount.valueOf(10, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
							)
					) 
				targetSpeedFlag = true;
			else if (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
					> (1.13 
							+ Amount.valueOf(20, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
							))
				newAlphaRed = alphaRed + 0.25;
			else if (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
					< (1.13 
							+ Amount.valueOf(10, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
							))
				newAlphaRed = alphaRed - 0.25;

			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

			i++;

		} 

		if (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
				> (1.13 
						+ Amount.valueOf(20, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
						))
			System.err.println("ERROR: THE FINAL CLIMB SPEED EXCEEDS THE MAXIMUM LIMITATION OF V2 + 20 knots --> " 
					+ " vClimb current = "
					+ vClimb.to(NonSI.KNOT)
					+ " != "
					+ " Max Limit = "
					+ vSTakeOff.to(NonSI.KNOT).times(1.13).plus(Amount.valueOf(20, NonSI.KNOT))
					);
		if (vClimb.doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND) 
				< (1.13 
						+ Amount.valueOf(10, NonSI.KNOT).doubleValue(SI.METERS_PER_SECOND)/vSTakeOff.doubleValue(SI.METERS_PER_SECOND)
						))
			System.err.println("ERROR: THE FINAL CLIMB SPEED IS LOWER THAN THE MINIMUM LIMITATION OF V2 + 10 knots --> " 
					+ " vClimb current = "
					+ vClimb.to(NonSI.KNOT)
					+ " != "
					+ " Min Limit = "
					+ vSTakeOff.to(NonSI.KNOT).times(1.13).plus(Amount.valueOf(10, NonSI.KNOT))
					);

		if (targetSpeedFlag == true)
			if(cutback==false && phiCutback==null)
				manageOutputData(0.5, 1.0, timeHistories, continuousOutputModel);
			else if(cutback==true && phiCutback==null)
				manageOutputData(0.5, TakeOffNoiseTrajectoryCalc.this.getPhiCutback(), timeHistories, continuousOutputModel);
			else if(cutback==true && phiCutback!=null)
				manageOutputData(0.5, phiCutback, timeHistories, continuousOutputModel);

		System.out.println("\n---------------------------END!!-------------------------------\n\n");
	}

	private void handleStepImplementation (double t, double[] x, double[] xDot) {
		//========================================================================================
		// PICKING UP ALL VARIABLES AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
		//----------------------------------------------------------------------------------------
		Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
		Amount<Length> groundDistance = Amount.valueOf(x[0], SI.METER);
		Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
		Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
		Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
		Amount<Force> weight = Amount.valueOf( 
				(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		Amount<Angle> alpha = ((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);

//		System.out.println("\n\tTime = " + time);
//		System.out.println("\tDistance = " + groundDistance);
//		System.out.println("\tSpeed = " + speed);
//		System.out.println("\tAcceleration = " + xDot[2]);
//		System.out.println("\tGamma = " + gamma);
//		System.out.println("\tAlpha = " + alpha);
//		System.out.println("\tAltitude = " + altitude);
		
		List<Amount<Force>> totalThrustList = ((DynamicsEquationsTakeOffNoiseTrajectory)ode).thrust(speed, time, gamma, altitude, deltaTemperature);
		List<Amount<Force>> totalThrustListHorizontal = new ArrayList<>();
		List<Amount<Force>> totalThrustListVertical = new ArrayList<>();
		for(int j=0; j<totalThrustList.size(); j++) 
			totalThrustListHorizontal.add(totalThrustList.get(j).times(Math.cos(thePowerPlant.getEngineList().get(j).getTiltingAngle().doubleValue(SI.RADIAN))));
		for(int j=0; j<totalThrustList.size(); j++) 
			totalThrustListVertical.add(totalThrustList.get(j).times(Math.sin(thePowerPlant.getEngineList().get(j).getTiltingAngle().doubleValue(SI.RADIAN))));

		//----------------------------------------------------------------------------------------
		// TIME:
		TakeOffNoiseTrajectoryCalc.this.timePerStep.add(time);
		//----------------------------------------------------------------------------------------
		// GROUND DISTANCE:
		TakeOffNoiseTrajectoryCalc.this.groundDistancePerStep.add(groundDistance);
		//----------------------------------------------------------------------------------------
		// VERTICAL DISTANCE:
		TakeOffNoiseTrajectoryCalc.this.verticalDistancePerStep.add(altitude);
		//----------------------------------------------------------------------------------------
		// THRUST:
		TakeOffNoiseTrajectoryCalc.this.thrustPerStep.add(
				Amount.valueOf(
						totalThrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON)
				);
		//--------------------------------------------------------------------------------
		// FUEL USED (kg):
		TakeOffNoiseTrajectoryCalc.this.fuelUsedPerStep.add(Amount.valueOf(x[4], SI.KILOGRAM));
		//----------------------------------------------------------------------------------------
		// WEIGHT:
		TakeOffNoiseTrajectoryCalc.this.weightPerStep.add(weight);
		//----------------------------------------------------------------------------------------
		// SPEED:
		TakeOffNoiseTrajectoryCalc.this.speedPerStep.add(speed);
		//----------------------------------------------------------------------------------------
		// THRUST HORIZONTAL:
		TakeOffNoiseTrajectoryCalc.this.thrustHorizontalPerStep.add(
				Amount.valueOf(
						totalThrustListHorizontal
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						*Math.cos(alpha.doubleValue(SI.RADIAN)),
						SI.NEWTON)
				);
		//----------------------------------------------------------------------------------------
		// THRUST VERTICAL:
		TakeOffNoiseTrajectoryCalc.this.thrustVerticalPerStep.add(
				Amount.valueOf(
						totalThrustListVertical
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						*Math.sin(alpha.doubleValue(SI.RADIAN)),
						SI.NEWTON)
				);
		//--------------------------------------------------------------------------------
		// FRICTION:
		if(time.doubleValue(SI.SECOND) < tEndRot.doubleValue(SI.SECOND))
			TakeOffNoiseTrajectoryCalc.this.frictionPerStep.add(Amount.valueOf(
					((DynamicsEquationsTakeOffNoiseTrajectory)ode).mu(speed)
					*(weight.doubleValue(SI.NEWTON)
							- ((DynamicsEquationsTakeOffNoiseTrajectory)ode).lift(
									speed,
									alpha,
									gamma,
									altitude,
									deltaTemperature
									).doubleValue(SI.NEWTON)
							),
					SI.NEWTON)
					);
		else
			TakeOffNoiseTrajectoryCalc.this.frictionPerStep.add(Amount.valueOf(0.0, SI.NEWTON));
		//----------------------------------------------------------------------------------------
		// LIFT:
		TakeOffNoiseTrajectoryCalc.this.liftPerStep.add( 
				((DynamicsEquationsTakeOffNoiseTrajectory)ode).lift(
						speed,
						alpha,
						gamma,
						altitude,
						deltaTemperature
						)
				);
		//----------------------------------------------------------------------------------------
		// DRAG:
		TakeOffNoiseTrajectoryCalc.this.dragPerStep.add(
				((DynamicsEquationsTakeOffNoiseTrajectory)ode).drag(
						speed,
						alpha,
						gamma,
						time,
						altitude,
						deltaTemperature
						)
				);
		//----------------------------------------------------------------------------------------
		// TOTAL FORCE:
		TakeOffNoiseTrajectoryCalc.this.totalForcePerStep.add(
				Amount.valueOf(
						( totalThrustListHorizontal.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.cos(alpha.doubleValue(SI.RADIAN))
								)
						- ((DynamicsEquationsTakeOffNoiseTrajectory)ode).drag(
								speed,
								alpha,
								gamma,
								time,
								altitude,
								deltaTemperature
								).doubleValue(SI.NEWTON)
						- ( ((DynamicsEquationsTakeOffNoiseTrajectory)ode).mu(speed)
								*(weight.doubleValue(SI.NEWTON)
										- ((DynamicsEquationsTakeOffNoiseTrajectory)ode).lift(
												speed,
												alpha,
												gamma,
												altitude,
												deltaTemperature
												).doubleValue(SI.NEWTON)
										)
								)
						- (weight.doubleValue(SI.NEWTON)
								*Math.sin(gamma.doubleValue(SI.RADIAN))
								),
						SI.NEWTON
						)
				);
		//----------------------------------------------------------------------------------------
		// LOAD FACTOR:
		TakeOffNoiseTrajectoryCalc.this.loadFactorPerStep.add(
				(  ((DynamicsEquationsTakeOffNoiseTrajectory)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
						+ (  ((DynamicsEquationsTakeOffNoiseTrajectory)ode).thrust(speed, time, gamma, altitude, deltaTemperature)
								.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.sin(alpha.doubleValue(SI.RADIAN))
								)
						)
				/ ( weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN))	)
				);
		//----------------------------------------------------------------------------------------
		// RATE OF CLIMB:
		TakeOffNoiseTrajectoryCalc.this.rateOfClimbPerStep.add(Amount.valueOf(
				xDot[3],
				SI.METERS_PER_SECOND)
				);
		//----------------------------------------------------------------------------------------
		// ACCELERATION:
		TakeOffNoiseTrajectoryCalc.this.accelerationPerStep.add(
				Amount.valueOf(xDot[1], SI.METERS_PER_SQUARE_SECOND)
				);
		//----------------------------------------------------------------------------------------
		// ALPHA:
		TakeOffNoiseTrajectoryCalc.this.alphaPerStep.add(alpha);
		//----------------------------------------------------------------------------------------
		// GAMMA:
		TakeOffNoiseTrajectoryCalc.this.gammaPerStep.add(gamma);
		//----------------------------------------------------------------------------------------
		// ALPHA DOT:
		if((time.doubleValue(SI.SECOND) > tRot.doubleValue(SI.SECOND)) 
				&& (time.doubleValue(SI.SECOND) < tHold.doubleValue(SI.SECOND))) {
			TakeOffNoiseTrajectoryCalc.this.alphaDotPerStep.add(
					TakeOffNoiseTrajectoryCalc.this.getAlphaDotInitial()
					*(1-(TakeOffNoiseTrajectoryCalc.this.getkAlphaDot()
							*alpha.doubleValue(NonSI.DEGREE_ANGLE)
							)
							)
					);
		}
		else if((time.doubleValue(SI.SECOND) > tEndHold.doubleValue(SI.SECOND))
				&& (time.doubleValue(SI.SECOND) < tZeroAcceleration.doubleValue(SI.SECOND))) {
			TakeOffNoiseTrajectoryCalc.this.alphaDotPerStep.add(alphaRed);
		}
		else if(time.doubleValue(SI.SECOND) > tZeroAcceleration.doubleValue(SI.SECOND)) {
			double deltaAlpha = 
					TakeOffNoiseTrajectoryCalc.this.alphaPerStep.get(
							TakeOffNoiseTrajectoryCalc.this.alphaPerStep.size()-1
							).doubleValue(NonSI.DEGREE_ANGLE)
					- TakeOffNoiseTrajectoryCalc.this.alphaPerStep.get(
							TakeOffNoiseTrajectoryCalc.this.alphaPerStep.size()-2
							).doubleValue(NonSI.DEGREE_ANGLE);
			double deltaTime = 
					TakeOffNoiseTrajectoryCalc.this.timePerStep.get(
							TakeOffNoiseTrajectoryCalc.this.timePerStep.size()-1
							).doubleValue(SI.SECOND)
					- TakeOffNoiseTrajectoryCalc.this.timePerStep.get(
							TakeOffNoiseTrajectoryCalc.this.timePerStep.size()-2
							).doubleValue(SI.SECOND);
			TakeOffNoiseTrajectoryCalc.this.alphaDotPerStep.add(deltaAlpha/deltaTime);
		}
		else
			TakeOffNoiseTrajectoryCalc.this.alphaDotPerStep.add(0.0);
		//----------------------------------------------------------------------------------------
		// GAMMA DOT:
		TakeOffNoiseTrajectoryCalc.this.gammaDotPerStep.add(xDot[2]);
		//----------------------------------------------------------------------------------------
		// THETA:
		TakeOffNoiseTrajectoryCalc.this.thetaPerStep.add(Amount.valueOf(
				gamma.doubleValue(NonSI.DEGREE_ANGLE) + alpha.doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE)
				);
		//----------------------------------------------------------------------------------------
		// CL:				
		TakeOffNoiseTrajectoryCalc.this.cLPerStep.add(
				((DynamicsEquationsTakeOffNoiseTrajectory)ode).cL(alpha)
				);
		//----------------------------------------------------------------------------------------
		// CD:
		TakeOffNoiseTrajectoryCalc.this.cDPerStep.add(
				((DynamicsEquationsTakeOffNoiseTrajectory)ode).cD(
						((DynamicsEquationsTakeOffNoiseTrajectory)ode).cL(alpha),
						time,
						speed,
						altitude)
				);

		//========================================================================================
		// CHECK ON LOAD FACTOR --> END ROTATION WHEN n=1
		if((t > tRot.doubleValue(SI.SECOND)) && (tEndRot.doubleValue(SI.SECOND) == 10000.0) &&
				(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().get(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().size()-1) > 1.0) &&
				(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().get(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().size()-2) < 1.0)) {
			System.out.println("\n\t\tEND OF ROTATION PHASE");
			System.out.println(
					"\n\tx[0] = s = " + x[0] + " m" +
							"\n\tx[1] = V = " + x[1] + " m/s" + 
							"\n\tx[2] = gamma = " + x[2] + " °" +
							"\n\tx[3] = altitude = " + x[3] + " m" +
							"\n\tx[4] = fuel used = " + x[4] + " kg" +
							"\n\tt = " + t + " s"
					);
			System.out.println("\n---------------------------DONE!-------------------------------");

			tEndRot = Amount.valueOf(t, SI.SECOND);
			timeBreakPoints.add(t);
			vLO = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
		}

		//========================================================================================
		// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
		if((t > tRot.doubleValue(SI.SECOND)) && (t <= tObstacle.doubleValue(SI.SECOND)) && 
				(TakeOffNoiseTrajectoryCalc.this.getcLPerStep().get(TakeOffNoiseTrajectoryCalc.this.getcLPerStep().size()-1) - (kcLMax*cLmaxTO) >= 0.0) &&
				((TakeOffNoiseTrajectoryCalc.this.getcLPerStep().get(TakeOffNoiseTrajectoryCalc.this.getcLPerStep().size()-2) - (kcLMax*cLmaxTO)) < 0.0)) {
			System.out.println("\n\t\tBEGIN BAR HOLDING");
			System.out.println(
					"\n\tCL = " + ((DynamicsEquationsTakeOffNoiseTrajectory)ode).cL(alpha) + 
					"\n\tAlpha Body = " + ((DynamicsEquationsTakeOffNoiseTrajectory)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight) + " °" + 
					"\n\tt = " + t + " s"
					);
			System.out.println("\n---------------------------DONE!-------------------------------");

			tHold = Amount.valueOf(t, SI.SECOND);
			timeBreakPoints.add(t);
		}

		//========================================================================================
		// CHECK ON LOAD FACTOR TO ENSTABLISH WHEN n=1 WHILE DECREASING ALPHA AND CL
		if((t > tEndHold.doubleValue(SI.SECOND)) && (tClimb.doubleValue(SI.SECOND) == 10000.0) &&
				(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().get(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().size()-1) < 1.0) &&
				(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().get(TakeOffNoiseTrajectoryCalc.this.getLoadFactorPerStep().size()-2) > 1.0) ) {
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
		if(t > tEndRot.doubleValue(SI.SECOND) && tZeroAcceleration.doubleValue(SI.SECOND) == 10000 &&
				(TakeOffNoiseTrajectoryCalc.this.getAccelerationPerStep().get(TakeOffNoiseTrajectoryCalc.this.getAccelerationPerStep().size()-1).doubleValue(SI.METERS_PER_SQUARE_SECOND) < 0.0) &&
				(TakeOffNoiseTrajectoryCalc.this.getAccelerationPerStep().get(TakeOffNoiseTrajectoryCalc.this.getAccelerationPerStep().size()-2).doubleValue(SI.METERS_PER_SQUARE_SECOND) > 0.0)
				) {

			System.out.println("\n\t\tZERO ACCELERATION REACHED ... ");
			System.out.println( 
					"\n\tt = " + t + " s"
					);
			System.out.println(
					"\n\tx[0] = s = " + x[0] + " m" +
							"\n\tx[1] = V = " + x[1] + " m/s" + 
							"\n\tx[2] = gamma = " + x[2] + " °" +
							"\n\tx[3] = altitude = " + x[3] + " m" +
							"\n\tx[4] = fuel used = " + x[4] + " kg"
					);
			System.out.println("\n---------------------------DONE!-------------------------------");

			tZeroAcceleration = Amount.valueOf(t, SI.SECOND);
			vClimb = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			timeBreakPoints.add(t);

		}
	}
	
	/********************************************************************************************
	 * This method allows users to fill all the maps of results related to each throttle setting.
	 * @param dt, time discretization provided by the user
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void manageOutputData(double dt, double phi, boolean timeHistories, StepHandler handler) {

		// temporary lists
		List<Amount<Velocity>> speedList = new ArrayList<>();
		List<Amount<Velocity>> rateOfClimbList = new ArrayList<>();
		List<Amount<Force>> thrustList = new ArrayList<>();
		List<Amount<Force>> thrustHorizontalList = new ArrayList<>();
		List<Amount<Force>> thrustVerticalList = new ArrayList<>();
		List<Amount<Force>> liftList = new ArrayList<>();
		List<Amount<Force>> dragList = new ArrayList<>();
		List<Amount<Force>> totalForceList = new ArrayList<>();
		List<Amount<Force>> frictionList = new ArrayList<>();
		List<Amount<Angle>> alphaList = new ArrayList<>();
		List<Amount<Angle>> gammaList = new ArrayList<>();
		List<Amount<Angle>> thetaList = new ArrayList<>();
		List<Double> alphaDotList = new ArrayList<>();
		List<Double> gammaDotList = new ArrayList<>();
		List<Double> cLList = new ArrayList<>();
		List<Double> loadFactorList = new ArrayList<>();
		List<Double> cDList = new ArrayList<>();
		List<Amount<Acceleration>> accelerationList = new ArrayList<>();
		List<Amount<Length>> groundDistanceList = new ArrayList<>();
		List<Amount<Length>> verticalDistanceList = new ArrayList<>();
		List<Amount<Duration>> timeList = new ArrayList<>();
		List<Amount<Mass>> fuelUsedList = new ArrayList<>();
		List<Amount<Force>> weightList = new ArrayList<>();
		
		// step handler interpolating functions
		MyInterpolatingFunction speedFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction rateOfClimbFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction thrustFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction thrustHorizontalFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction thrustVerticalFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction liftFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction dragFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction totalForceFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction frictionFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction alphaFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction gammaFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction thetaFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction alphaDotFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction gammaDotFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction cLFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction loadFactorFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction cDFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction accelerationFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction groundDistanceFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction verticalDistanceFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction fuelUsedFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction weightFunction = new MyInterpolatingFunction();
		
		alphaFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.alphaPerStep)
				);

		if(timeHistories) {

			speedFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.speedPerStep))
					);
			rateOfClimbFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.rateOfClimbPerStep))
					);
			thrustFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.thrustPerStep))
					);
			thrustHorizontalFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.thrustHorizontalPerStep))
					);
			thrustVerticalFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.thrustVerticalPerStep))
					);
			liftFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.liftPerStep))
					);
			dragFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.dragPerStep))
					);
			frictionFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.frictionPerStep))
					);
			totalForceFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.totalForcePerStep))
					);
			gammaFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.gammaPerStep))
					);
			thetaFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this.thetaPerStep))
					);
			alphaDotFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.alphaDotPerStep))
					);
			gammaDotFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.gammaDotPerStep))
					);
			loadFactorFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.loadFactorPerStep))
					);
			accelerationFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.accelerationPerStep)
					);
			cLFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.cLPerStep))
					);
			cDFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.cDPerStep))
					);
			weightFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.weightPerStep)
					);
			fuelUsedFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.fuelUsedPerStep)
					);
			groundDistanceFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.groundDistancePerStep)
					);
			verticalDistanceFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.timePerStep), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.verticalDistancePerStep)
					);
		}

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
					if ((t-dt < t_) && (t > t_)) 
						if(Math.abs(times.get(times.size()-1).doubleValue(SI.SECOND) - t_) >= 1e-3)
							// set back time to breakpoint-time
							t = t_;
				}

			} while (t <= cm.getFinalTime());

			//--------------------------------------------------------------------------------
			// Reconstruct the auxiliary/derived variables
			for(int i = 0; i < times.size(); i++) {

				Amount<Duration> time = times.get(i);
				
				//========================================================================================
				// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				// TIME:
				timeList.add(time);
				//----------------------------------------------------------------------------------------
				// GROUND DISTANCE:
				groundDistanceList.add(
						Amount.valueOf(
								groundDistanceFunction.value(time.doubleValue(SI.SECOND)),
								SI.METER
								)
						);
				//----------------------------------------------------------------------------------------
				// VERTICAL DISTANCE:
				verticalDistanceList.add(
						Amount.valueOf(
								verticalDistanceFunction.value(time.doubleValue(SI.SECOND)),
								SI.METER
								)
						);
				//----------------------------------------------------------------------------------------
				// THRUST:
				thrustList.add(
						Amount.valueOf(
								thrustFunction.value(time.doubleValue(SI.SECOND)),
								SI.NEWTON
								)
						);
				//--------------------------------------------------------------------------------
				// FUEL USED (kg):
				fuelUsedList.add(
						Amount.valueOf(
								fuelUsedFunction.value(time.doubleValue(SI.SECOND)),
								SI.KILOGRAM
								)
						);
				//----------------------------------------------------------------------------------------
				if(timeHistories) {
					//----------------------------------------------------------------------------------------
					// WEIGHT:
					weightList.add(
							Amount.valueOf(
									weightFunction.value(times.get(i).doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//----------------------------------------------------------------------------------------
					// SPEED:
					speedList.add(
							Amount.valueOf(
									speedFunction.value(time.doubleValue(SI.SECOND)),
									SI.METERS_PER_SECOND
									)
							);
					//----------------------------------------------------------------------------------------
					// THRUST HORIZONTAL:
					thrustHorizontalList.add(
							Amount.valueOf(
									thrustHorizontalFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//----------------------------------------------------------------------------------------
					// THRUST VERTICAL:
					thrustVerticalList.add(
							Amount.valueOf(
									thrustVerticalFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//--------------------------------------------------------------------------------
					// FRICTION:
					frictionList.add(
							Amount.valueOf(
									frictionFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
								);
					//----------------------------------------------------------------------------------------
					// LIFT:
					liftList.add( 
							Amount.valueOf(
									liftFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//----------------------------------------------------------------------------------------
					// DRAG:
					dragList.add(
							Amount.valueOf(
									dragFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//----------------------------------------------------------------------------------------
					// TOTAL FORCE:
					totalForceList.add(
							Amount.valueOf(
									totalForceFunction.value(time.doubleValue(SI.SECOND)),
									SI.NEWTON
									)
							);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					loadFactorList.add(loadFactorFunction.value(times.get(i).doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// RATE OF CLIMB:
					rateOfClimbList.add(
							Amount.valueOf(
									rateOfClimbFunction.value(time.doubleValue(SI.SECOND)),
									SI.METERS_PER_SECOND
									)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					accelerationList.add(
							Amount.valueOf(
									accelerationFunction.value(times.get(i).doubleValue(SI.SECOND)),
									SI.METERS_PER_SQUARE_SECOND
									)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					alphaList.add(
							Amount.valueOf(
									alphaFunction.value(times.get(i).doubleValue(SI.SECOND)),
									NonSI.DEGREE_ANGLE
									)
							);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					gammaList.add(
							Amount.valueOf(
									gammaFunction.value(time.doubleValue(SI.SECOND)),
									NonSI.DEGREE_ANGLE
									)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					alphaDotList.add(alphaDotFunction.value(time.doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// GAMMA DOT:
					gammaDotList.add(gammaDotFunction.value(time.doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// THETA:
					thetaList.add(
							Amount.valueOf(
									thetaFunction.value(time.doubleValue(SI.SECOND)),
									NonSI.DEGREE_ANGLE
									)
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					cLList.add(cLFunction.value(times.get(i).doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// CD:
					cDList.add(cDFunction.value(times.get(i).doubleValue(SI.SECOND)));

					//----------------------------------------------------------------------------------------
				}
			}
		}

		timeMap.put(phi, times);
		groundDistanceMap.put(phi, groundDistanceList);
		verticalDistanceMap.put(phi, verticalDistanceList);

		if(timeHistories) {
			speedMap.put(phi, speedList);
			thrustMap.put(phi, thrustList);
			thrustHorizontalMap.put(phi, thrustHorizontalList);
			thrustVerticalMap.put(phi, thrustVerticalList);
			alphaMap.put(phi, alphaList);
			alphaDotMap.put(phi, alphaDotList);
			gammaMap.put(phi, gammaList);
			gammaDotMap.put(phi, gammaDotList);
			thetaMap.put(phi, thetaList);
			cLMap.put(phi, cLList);
			liftMap.put(phi, liftList);
			loadFactorMap.put(phi, loadFactorList);
			cDMap.put(phi, cDList);
			dragMap.put(phi, dragList);
			frictionMap.put(phi, frictionList);
			totalForceMap.put(phi, totalForceList);
			accelerationMap.put(phi, accelerationList);
			rateOfClimbMap.put(phi, rateOfClimbList);
			fuelUsedMap.put(phi, fuelUsedList);
			weightMap.put(phi, weightList);
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

		speedMap.keySet().stream().forEach(
				phi -> {
					if(timeHistories) {
						String currentOutputFolder = JPADStaticWriteUtils.createNewFolder(
								outputFolderPath + ("PHI=" + String.format( "%.2f", phi)) + File.separator
								);

						System.out.println("\tPRINTING CHARTS FOR PHI="+ String.format( "%.2f", phi ) +" TO FILE ...");
						//.................................................................................
						// speed v.s. time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(speedMap.get(phi)),
								0.0, null, 0.0, null,
								"Time", "Speed", "s", "m/s",
								currentOutputFolder, "Speed_evolution_SI", createCSV);


						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(
										speedMap.get(phi).stream()
										.map(x -> x.to(NonSI.KNOT))
										.collect(Collectors.toList())
										),
								0.0, null, null, null,
								"Time", "Speed", "s", "kn",
								currentOutputFolder, "Speed_evolution_IMPERIAL", createCSV);

						//.................................................................................
						// speed v.s. ground distance
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(speedMap.get(phi)),
								0.0, null, null, null,
								"Ground Distance", "Speed", "m", "m/s",
								currentOutputFolder, "Speed_vs_GroundDistance_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertListOfAmountTodoubleArray(
										speedMap.get(phi).stream()
										.map(x -> x.to(NonSI.KNOT))
										.collect(Collectors.toList())
										),
								0.0, null, 0.0, null,
								"Ground Distance", "Speed", "ft", "kn",
								currentOutputFolder, "Speed_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// acceleration v.s. time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(accelerationMap.get(phi)),
								0.0, null, null, null,
								"Time", "Acceleration", "s", "m/(s^2)",
								currentOutputFolder, "Acceleration_evolution_SI", createCSV);

						//.................................................................................
						// acceleration v.s. ground distance
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(accelerationMap.get(phi)),
								0.0, null, null, null,
								"Ground Distance", "Acceleration", "m", "m/(s^2)",
								currentOutputFolder, "Acceleration_vs_GroundDistance_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertListOfAmountTodoubleArray(accelerationMap.get(phi)),
								0.0, null, null, null,
								"Ground Distance", "Acceleration", "ft", "m/(s^2)",
								currentOutputFolder, "Acceleration_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// load factor v.s. time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertToDoublePrimitive(loadFactorMap.get(phi)),
								0.0, null, 0.0, null,
								"Time", "Load Factor", "s", "",
								currentOutputFolder, "LoadFactor_evolution", createCSV);

						//.................................................................................
						// load factor v.s. ground distance
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)), 
								MyArrayUtils.convertToDoublePrimitive(loadFactorMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "Load Factor", "m", "",
								currentOutputFolder, "LoadFactor_vs_GroundDistance_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										), 
								MyArrayUtils.convertToDoublePrimitive(loadFactorMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "Load Factor", "ft", "",
								currentOutputFolder, "LoadFactor_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// Rate of Climb v.s. Time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbMap.get(phi)),
								0.0, null, 0.0, null,
								"Time", "Rate of Climb", "s", "m/s",
								currentOutputFolder, "RateOfClimb_evolution_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertListOfAmountTodoubleArray(
										rateOfClimbMap.get(phi).stream()
										.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
										.collect(Collectors.toList())
										),
								0.0, null, 0.0, null,
								"Time", "Rate of Climb", "s", "ft/min",
								currentOutputFolder, "RateOfClimb_evolution_IMPERIAL", createCSV);

						//.................................................................................
						// Rate of Climb v.s. Ground distance
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)), 
								MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "Rate of Climb", "m", "m/s",
								currentOutputFolder, "RateOfClimb_vs_GroundDistance_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										), 
								MyArrayUtils.convertListOfAmountTodoubleArray(
										rateOfClimbMap.get(phi).stream()
										.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
										.collect(Collectors.toList())
										),
								0.0, null, 0.0, null,
								"Ground distance", "Rate of Climb", "ft", "ft/min",
								currentOutputFolder, "RateOfClimb_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// CL v.s. Time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertToDoublePrimitive(cLMap.get(phi)),
								0.0, null, 0.0, null,
								"Time", "CL", "s", "",
								currentOutputFolder, "CL_evolution", createCSV);

						//.................................................................................
						// CL v.s. Ground distance
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)),
								MyArrayUtils.convertToDoublePrimitive(cLMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "CL", "m", "",
								currentOutputFolder, "CL_vs_GroundDistance_SI", createCSV);

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertToDoublePrimitive(cLMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "CL", "ft", "",
								currentOutputFolder, "CL_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// CD v.s. Time
						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi)),
								MyArrayUtils.convertToDoublePrimitive(cDMap.get(phi)),
								0.0, null, 0.0, null,
								"Time", "CD", "s", "",
								currentOutputFolder, "CD_evolution", createCSV);

						//.................................................................................
						// CD v.s. Ground distance

						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi)),
								MyArrayUtils.convertToDoublePrimitive(cDMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "CD", "m", "",
								currentOutputFolder, "CD_vs_GroundDistance_SI", createCSV);


						MyChartToFileUtils.plotNoLegend(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertToDoublePrimitive(cDMap.get(phi)),
								0.0, null, 0.0, null,
								"Ground distance", "CD", "ft", "",
								currentOutputFolder, "CD_vs_GroundDistance_IMPERIAL", createCSV);

						//.................................................................................
						// Horizontal Forces v.s. Time
						{
							double[][] xMatrix1SI = new double[5][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix1SI.length; i++)
								xMatrix1SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

							double[][] yMatrix1SI = new double[5][totalForceMap.get(phi).size()];
							yMatrix1SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForceMap.get(phi));
							yMatrix1SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontalMap.get(phi));
							yMatrix1SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(dragMap.get(phi));
							yMatrix1SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(frictionMap.get(phi));
							yMatrix1SI[4] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											MyArrayUtils.convertDoubleArrayToListDouble(
													MyArrayUtils.convertListOfAmountToDoubleArray(weightMap.get(phi))
													)
											)
									); 

							MyChartToFileUtils.plot(
									xMatrix1SI, yMatrix1SI,
									0.0, null, null, null,
									"Time", "Horizontal Forces", "s", "N",
									new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
									currentOutputFolder, "HorizontalForces_evolution_SI",
									createCSV);
						}

						{
							double[][] xMatrix1IMPERIAL = new double[5][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix1IMPERIAL.length; i++)
								xMatrix1IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

							double[][] yMatrix1IMPERIAL = new double[5][totalForceMap.get(phi).size()];
							yMatrix1IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									totalForceMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix1IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thrustHorizontalMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix1IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
									dragMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix1IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
									frictionMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix1IMPERIAL[4] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											weightMap.get(phi).stream()
											.map(x -> x.doubleValue(NonSI.POUND_FORCE))
											.collect(Collectors.toList())
											)
									); 

							MyChartToFileUtils.plot(
									xMatrix1IMPERIAL, yMatrix1IMPERIAL,
									0.0, null, null, null,
									"Time", "Horizontal Forces", "s", "lb",
									new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
									currentOutputFolder, "HorizontalForces_evolution_IMPERIAL",
									createCSV);
						}

						//.................................................................................
						// Horizontal Forces v.s. Ground Distance
						{
							double[][] xMatrix2SI = new double[5][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix2SI.length; i++)
								xMatrix2SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi));

							double[][] yMatrix2SI = new double[5][totalForceMap.get(phi).size()];
							yMatrix2SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForceMap.get(phi));
							yMatrix2SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontalMap.get(phi));
							yMatrix2SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(dragMap.get(phi));
							yMatrix2SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(frictionMap.get(phi));
							yMatrix2SI[4] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											MyArrayUtils.convertDoubleArrayToListDouble(
													MyArrayUtils.convertListOfAmountToDoubleArray(weightMap.get(phi))
													)
											)
									); 

							MyChartToFileUtils.plot(
									xMatrix2SI, yMatrix2SI,
									0.0, null, null, null,
									"Ground Distance", "Horizontal Forces", "m", "N",
									new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
									currentOutputFolder, "HorizontalForces_vs_GroundDistance_SI",
									createCSV);
						}

						{
							double[][] xMatrix2IMPERIAL = new double[5][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix2IMPERIAL.length; i++)
								xMatrix2IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi));

							double[][] yMatrix2IMPERIAL = new double[5][totalForceMap.get(phi).size()];
							yMatrix2IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									totalForceMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix2IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thrustHorizontalMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix2IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
									dragMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix2IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
									frictionMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix2IMPERIAL[4] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											weightMap.get(phi).stream()
											.map(x -> x.doubleValue(NonSI.POUND_FORCE))
											.collect(Collectors.toList())
											)
									);    

							MyChartToFileUtils.plot(
									xMatrix2IMPERIAL, yMatrix2IMPERIAL,
									0.0, null, null, null,
									"Ground Distance", "Horizontal Forces", "ft", "lb",
									new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "Wsin(gamma)"},
									currentOutputFolder, "HorizontalForces_vs_GroundDistance_IMPERIAL",
									createCSV);
						}

						//.................................................................................
						// Vertical Forces v.s. Time
						{
							double[][] xMatrix3SI = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix3SI.length; i++)
								xMatrix3SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

							double[][] yMatrix3SI = new double[3][totalForceMap.get(phi).size()];
							yMatrix3SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(liftMap.get(phi));
							yMatrix3SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVerticalMap.get(phi));
							yMatrix3SI[2] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											MyArrayUtils.convertDoubleArrayToListDouble(
													MyArrayUtils.convertListOfAmountToDoubleArray(weightMap.get(phi))
													)
											)
									);  

							MyChartToFileUtils.plot(
									xMatrix3SI, yMatrix3SI,
									0.0, null, null, null,
									"Time", "Vertical Forces", "s", "N",
									new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
									currentOutputFolder, "VerticalForces_evolution_SI",
									createCSV);
						}

						{
							double[][] xMatrix3IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix3IMPERIAL.length; i++)
								xMatrix3IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

							double[][] yMatrix3IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							yMatrix3IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									liftMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix3IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thrustVerticalMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix3IMPERIAL[2] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											weightMap.get(phi).stream()
											.map(x -> x.doubleValue(NonSI.POUND_FORCE))
											.collect(Collectors.toList())
											)
									); 

							MyChartToFileUtils.plot(
									xMatrix3IMPERIAL, yMatrix3IMPERIAL,
									0.0, null, null, null,
									"Time", "Vertical Forces", "s", "lb",
									new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
									currentOutputFolder, "VerticalForces_evolution_IMPERIAL",
									createCSV);
						}

						//.................................................................................
						// Vertical Forces v.s. ground distance
						{
							double[][] xMatrix4SI = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix4SI.length; i++)
								xMatrix4SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi));

							double[][] yMatrix4SI = new double[3][totalForceMap.get(phi).size()];
							yMatrix4SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(liftMap.get(phi));
							yMatrix4SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVerticalMap.get(phi));
							yMatrix4SI[2] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											MyArrayUtils.convertDoubleArrayToListDouble(
													MyArrayUtils.convertListOfAmountToDoubleArray(weightMap.get(phi))
													)
											)
									);  

							MyChartToFileUtils.plot(
									xMatrix4SI, yMatrix4SI,
									0.0, null, null, null,
									"Ground distance", "Vertical Forces", "m", "N",
									new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
									currentOutputFolder, "VerticalForces_vs_GroundDistance_SI",
									createCSV);
						}

						{
							double[][] xMatrix4IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix4IMPERIAL.length; i++)
								xMatrix4IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										);

							double[][] yMatrix4IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							yMatrix4IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									liftMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix4IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thrustVerticalMap.get(phi).stream()
									.map(x -> x.to(NonSI.POUND_FORCE))
									.collect(Collectors.toList())
									);
							yMatrix4IMPERIAL[2] = MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.multiplyListEbE(
											gammaMap.get(phi).stream()
											.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
											.collect(Collectors.toList()),
											weightMap.get(phi).stream()
											.map(x -> x.doubleValue(NonSI.POUND_FORCE))
											.collect(Collectors.toList())
											)
									); 

							MyChartToFileUtils.plot(
									xMatrix4IMPERIAL, yMatrix4IMPERIAL,
									0.0, null, null, null,
									"Ground distance", "Vertical Forces", "ft", "lb",
									new String[] {"Lift", "Thrust Vertical", "Wcos(gamma)"},
									currentOutputFolder, "VerticalForces_vs_GroundDistance_IMPERIAL",
									createCSV);
						}

						//.................................................................................
						// Angles v.s. time
						double[][] xMatrix5 = new double[3][totalForceMap.get(phi).size()];
						for(int i=0; i<xMatrix5.length; i++)
							xMatrix5[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

						double[][] yMatrix5 = new double[3][totalForceMap.get(phi).size()];
						yMatrix5[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
								alphaMap.get(phi).stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								);
						yMatrix5[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
								thetaMap.get(phi).stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								);
						yMatrix5[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
								gammaMap.get(phi).stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								);

						MyChartToFileUtils.plot(
								xMatrix5, yMatrix5,
								0.0, null, null, null,
								"Time", "Angles", "s", "deg",
								new String[] {"Alpha Body", "Theta", "Gamma"},
								currentOutputFolder, "Angles_evolution",
								createCSV);

						//.................................................................................
						// Angles v.s. Ground Distance
						{
							double[][] xMatrix6SI = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix6SI.length; i++)
								xMatrix6SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi));

							double[][] yMatrix6SI = new double[3][totalForceMap.get(phi).size()];
							yMatrix6SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									alphaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);
							yMatrix6SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thetaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);
							yMatrix6SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
									gammaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);

							MyChartToFileUtils.plot(
									xMatrix6SI, yMatrix6SI,
									0.0, null, null, null,
									"Ground Distance", "Angles", "m", "deg",
									new String[] {"Alpha Body", "Theta", "Gamma"},
									currentOutputFolder, "Angles_vs_GroundDistance_SI",
									createCSV);
						}

						{
							double[][] xMatrix6IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix6IMPERIAL.length; i++)
								xMatrix6IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										);

							double[][] yMatrix6IMPERIAL = new double[3][totalForceMap.get(phi).size()];
							yMatrix6IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
									alphaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);
							yMatrix6IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
									thetaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);
							yMatrix6IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
									gammaMap.get(phi).stream()
									.map(x -> x.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									);

							MyChartToFileUtils.plot(
									xMatrix6IMPERIAL, yMatrix6IMPERIAL,
									0.0, null, null, null,
									"Ground Distance", "Angles", "ft", "deg",
									new String[] {"Alpha Body", "Theta", "Gamma"},
									currentOutputFolder, "Angles_vs_GroundDistance_IMPERIAL",
									createCSV);
						}
						//.................................................................................
						// Angular velocity v.s. time
						double[][] xMatrix7 = new double[2][totalForceMap.get(phi).size()];
						for(int i=0; i<xMatrix7.length; i++)
							xMatrix7[i] = MyArrayUtils.convertListOfAmountTodoubleArray(timeMap.get(phi));

						double[][] yMatrix7 = new double[2][totalForceMap.get(phi).size()];
						yMatrix7[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotMap.get(phi));
						yMatrix7[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotMap.get(phi));

						MyChartToFileUtils.plot(
								xMatrix7, yMatrix7,
								0.0, null, null, null,
								"Time", "Angular Velocity", "s", "deg/s",
								new String[] {"Alpha_dot", "Gamma_dot"},
								currentOutputFolder, "AngularVelocity_evolution",
								createCSV);

						//.................................................................................
						// Angular velocity v.s. Ground Distance
						{
							double[][] xMatrix8SI = new double[2][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix8SI.length; i++)
								xMatrix8SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceMap.get(phi));

							double[][] yMatrix8SI = new double[2][totalForceMap.get(phi).size()];
							yMatrix8SI[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotMap.get(phi));
							yMatrix8SI[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotMap.get(phi));

							MyChartToFileUtils.plot(
									xMatrix8SI, yMatrix8SI,
									0.0, null, null, null,
									"Ground Distance", "Angular Velocity", "m", "deg/s",
									new String[] {"Alpha_dot", "Gamma_dot"},
									currentOutputFolder, "AngularVelocity_vs_GroundDistance_SI",
									createCSV);
						}

						{
							double[][] xMatrix8IMPERIAL = new double[2][totalForceMap.get(phi).size()];
							for(int i=0; i<xMatrix8IMPERIAL.length; i++)
								xMatrix8IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
										groundDistanceMap.get(phi).stream()
										.map(x -> x.to(NonSI.FOOT))
										.collect(Collectors.toList())
										);

							double[][] yMatrix8SIMPERIAL = new double[2][totalForceMap.get(phi).size()];
							yMatrix8SIMPERIAL[0] = MyArrayUtils.convertToDoublePrimitive(alphaDotMap.get(phi));
							yMatrix8SIMPERIAL[1] = MyArrayUtils.convertToDoublePrimitive(gammaDotMap.get(phi));

							MyChartToFileUtils.plot(
									xMatrix8IMPERIAL, yMatrix8SIMPERIAL,
									0.0, null, null, null,
									"Ground Distance", "Angular Velocity", "ft", "deg/s",
									new String[] {"Alpha_dot", "Gamma_dot"},
									currentOutputFolder, "AngularVelocity_vs_GroundDistance_SI",
									createCSV);
						}

						System.out.println("\n---------------------------DONE!-------------------------------");
					}
				});

		String trajectoryOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "Trajectories" + File.separator
				);
		String thrustOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "Thrust" + File.separator
				);
		String fuelUsedOutputFolder = JPADStaticWriteUtils.createNewFolder(
				outputFolderPath + "FuelUsed" + File.separator
				);

		System.out.println("\tPRINTING TRAJECTORIES CHARTS TO FILE ...");

		//.................................................................................
		// take-off trajectory

		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) getVerticalDistanceMap().values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Take-off noise certification trajectories", "Ground distance", "Altitude",
				0.0, null, 0.0, null,
				"m", "m",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				trajectoryOutputFolder, "Trajectories_SI", createCSV
				);


		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(
						x.stream().map(xe -> xe.to(NonSI.FOOT)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				(List<Double[]>) getVerticalDistanceMap().values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(
						y.stream().map(ye -> ye.to(NonSI.FOOT)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				"Take-off noise certification trajectories", "Ground distance", "Altitude",
				0.0, null, 0.0, null,
				"ft", "ft",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				trajectoryOutputFolder, "Trajectories_IMPERIAL", createCSV
				);

		//.................................................................................
		// vertical distance v.s. time

		MyChartToFileUtils.plot(
				(List<Double[]>) timeMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) getVerticalDistanceMap().values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Take-off noise certification trajectories", "Time", "Altitude",
				0.0, null, 0.0, null,
				"s", "m",
				true, (List<String>) timeMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				trajectoryOutputFolder, "Altitude_Evolution_SI", createCSV
				);


		MyChartToFileUtils.plot(
				(List<Double[]>) timeMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) getVerticalDistanceMap().values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(
						y.stream().map(ye -> ye.to(NonSI.FOOT)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				"Take-off noise certification trajectories", "Time", "Altitude",
				0.0, null, 0.0, null,
				"s", "ft",
				true, (List<String>) timeMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				trajectoryOutputFolder, "Altitude_Evolution_IMPERIAL", createCSV
				);

		System.out.println("\tPRINTING THRUST CHARTS TO FILE ...");

		//.................................................................................
		// thrust v.s. time

		MyChartToFileUtils.plot(
				(List<Double[]>) timeMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) thrustMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Thrust for each take-off trajectory", "Time", "Thrust",
				0.0, null, 0.0, null,
				"s", "N",
				true, (List<String>) timeMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				thrustOutputFolder, "Thrust_evolution_SI", createCSV
				);

		MyChartToFileUtils.plot(
				(List<Double[]>) timeMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) thrustMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(
						y.stream().map(ye -> ye.to(NonSI.POUND_FORCE)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				"Thrust for each take-off trajectory", "Time", "Thrust",
				0.0, null, 0.0, null,
				"s", "lbf",
				true, (List<String>) timeMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				thrustOutputFolder, "Thrust_evolution_IMPERIAL", createCSV
				);

		//.................................................................................
		// thrust v.s. ground distance

		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) thrustMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Thrust for each take-off trajectory", "Ground distance", "Thrust",
				0.0, null, 0.0, null,
				"m", "N",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				thrustOutputFolder, "Thrust_vs_GroundDistance_SI", createCSV
				);


		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(
						x.stream().map(xe -> xe.to(NonSI.FOOT)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				(List<Double[]>) thrustMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(
						y.stream().map(ye -> ye.to(NonSI.POUND_FORCE)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				"Thrust for each take-off trajectory", "Ground distance", "Thrust",
				0.0, null, 0.0, null,
				"ft", "lbf",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				thrustOutputFolder, "Thrust_vs_GroundDistance_IMPERIAL", createCSV
				);

		System.out.println("\tPRINTING FUEL USED CHARTS TO FILE ...");

		//.................................................................................
		// fuelUsed v.s. time
		MyChartToFileUtils.plot(
				(List<Double[]>) timeMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) fuelUsedMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Fuel used for each take-off trajectory", "Time", "Fuel used",
				0.0, null, 0.0, null,
				"s", "kg",
				true, (List<String>) timeMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				fuelUsedOutputFolder, "FuelUsed_evolution", createCSV
				);

		//.................................................................................
		// fuelUsed v.s. ground distance

		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(x)).collect(Collectors.toList()),
				(List<Double[]>) fuelUsedMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(y)).collect(Collectors.toList()),
				"Fuel used for each take-off trajectory", "Ground distance", "Fuel used",
				0.0, null, 0.0, null,
				"m", "kg",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_SI", createCSV
				);


		MyChartToFileUtils.plot(
				(List<Double[]>) groundDistanceMap.values().stream().map(x -> MyArrayUtils.convertListOfAmountToDoubleArray(
						x.stream().map(xe -> xe.to(NonSI.FOOT)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				(List<Double[]>) fuelUsedMap.values().stream().map(y -> MyArrayUtils.convertListOfAmountToDoubleArray(
						y.stream().map(ye -> ye.to(NonSI.POUND)).collect(Collectors.toList())
						)).collect(Collectors.toList()),
				"Fuel used for each take-off trajectory", "Ground distance", "Fuel used",
				0.0, null, 0.0, null,
				"ft", "lb",
				true, (List<String>) groundDistanceMap.keySet().stream().map(phi -> "PHI = " + String.format( "%.2f", phi)).collect(Collectors.toList()),
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_IMPERIAL", createCSV
				);

	}

	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsTakeOffNoiseTrajectory implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);

		public DynamicsEquationsTakeOffNoiseTrajectory() { }

		@Override
		public int getDimension() {
			return 5;
		}
		
		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
			Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
			Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
			Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			Amount<Force> weight = Amount.valueOf( 
					(maxTakeOffMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
					SI.NEWTON
					);
			Amount<Angle> alpha = alpha(time, speed, altitude, deltaTemperature, gamma, weight);

			List<Amount<Force>> thrustList = thrust(speed, time, gamma, altitude, deltaTemperature);
			List<Amount<Force>> thrustListHorizontal = new ArrayList<>();
			List<Amount<Force>> thrustListVertical = new ArrayList<>();
			for(int i=0; i<thrustList.size(); i++) 
				thrustListHorizontal.add(thrustList.get(i).times(Math.cos(thePowerPlant.getEngineList().get(i).getTiltingAngle().doubleValue(SI.RADIAN))));
			for(int i=0; i<thrustList.size(); i++) 
				thrustListVertical.add(thrustList.get(i).times(Math.sin(thePowerPlant.getEngineList().get(i).getTiltingAngle().doubleValue(SI.RADIAN))));
			
			if( t < tEndRot.doubleValue(SI.SECOND)) {
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*( thrustListHorizontal.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum() 
								- drag(speed, alpha, gamma, time, altitude, deltaTemperature).doubleValue(SI.NEWTON)
								- (mu(speed)
										*(weight.doubleValue(SI.NEWTON) 
												- lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
												)
										)
								);
				xDot[2] = 0.0;
				xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
				xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
			}
			else {
				xDot[0] = speed.doubleValue(SI.METERS_PER_SECOND);
				xDot[1] = (g0/weight.doubleValue(SI.NEWTON))
						*( (thrustListHorizontal.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								*Math.cos(alpha.doubleValue(SI.RADIAN))
								) 
								- drag(speed, alpha, gamma, time, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
								- ( weight.doubleValue(SI.NEWTON)
										*Math.sin(gamma.doubleValue(SI.RADIAN))
										)
								);
				xDot[2] = 57.3*(g0/(weight.doubleValue(SI.NEWTON)*speed.doubleValue(SI.METERS_PER_SECOND)))
						*( lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
								+ ( thrustListVertical.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
										*Math.sin(alpha.doubleValue(SI.RADIAN))
										)
								- ( weight.doubleValue(SI.NEWTON)
										*Math.cos(gamma.doubleValue(SI.RADIAN))
										)
								);
				xDot[3] = speed.doubleValue(SI.METERS_PER_SECOND)*Math.sin(gamma.doubleValue(SI.RADIAN));
				xDot[4] = fuelFlow(speed, time, gamma, altitude, deltaTemperature);
			}
			
		}

		public List<Amount<Force>> thrust(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			List<Amount<Force>> theThrustList = new ArrayList<>();

			if(time.doubleValue(SI.SECOND) <= tCutback.doubleValue(SI.SECOND))
				for (int i=0; i<TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i),
									EngineOperatingConditionEnum.TAKE_OFF, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									TakeOffNoiseTrajectoryCalc.this.getPhi(),
									TakeOffNoiseTrajectoryCalc.this.getThrustCorrectionFactor(),
									thePowerPlant.getEngineList().get(i).getEngineType(),
									thePowerPlant.getEngineList().get(i).getEtaPropeller()
									)
							);
			else if(time.doubleValue(SI.SECOND) > tCutback.doubleValue(SI.SECOND) && time.doubleValue(SI.SECOND) <= tCutback.doubleValue(SI.SECOND)+dtThrustCutback.doubleValue(SI.SECOND) ) 
				for (int i=0; i<TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i), 
									EngineOperatingConditionEnum.TAKE_OFF, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									TakeOffNoiseTrajectoryCalc.this.getPhi(),
									TakeOffNoiseTrajectoryCalc.this.getThrustCorrectionFactor(),
									thePowerPlant.getEngineList().get(i).getEngineType(),
									thePowerPlant.getEngineList().get(i).getEtaPropeller()
									).times(deltaThrustCutbackSlope.value(time.doubleValue(SI.SECOND)))
							);
			else if(time.doubleValue(SI.SECOND) > tCutback.doubleValue(SI.SECOND)+dtThrustCutback.doubleValue(SI.SECOND)) 
				for (int i=0; i<TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineList().get(i).getT0(),
									TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(i),
									EngineOperatingConditionEnum.TAKE_OFF, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ TakeOffNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									TakeOffNoiseTrajectoryCalc.this.getPhi(),
									TakeOffNoiseTrajectoryCalc.this.getThrustCorrectionFactor(),
									thePowerPlant.getEngineList().get(i).getEngineType(),
									thePowerPlant.getEngineList().get(i).getEtaPropeller()
									).times(phiCutback)
							);

			return theThrustList;

		}

		public double fuelFlow(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			List<Double> fuelFlowList = new ArrayList<>();
			List<Amount<Force>> thrustList = thrust(speed, time, gamma, altitude, deltaTemperature); 
			
			for (int i=0; i<TakeOffNoiseTrajectoryCalc.this.getThePowerPlant().getEngineNumber(); i++) 
				fuelFlowList.add(
						thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
								SpeedCalc.calculateMach(
										altitude,
										deltaTemperature,
										Amount.valueOf(
												speed.doubleValue(SI.METERS_PER_SECOND) 
												+ TakeOffNoiseTrajectoryCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
												SI.METERS_PER_SECOND
												)
										),
								altitude,
								deltaTemperature,
								TakeOffNoiseTrajectoryCalc.this.getPhi(),
								EngineOperatingConditionEnum.TAKE_OFF,
								TakeOffNoiseTrajectoryCalc.this.getSfcCorrectionFactor()
								)
						*(0.224809)*(0.454/3600) // Conversion from lb/lb*hr to kg/N*s 
						*thrustList.get(i).doubleValue(SI.NEWTON)
						);

			return fuelFlowList.stream().mapToDouble(ff -> ff).sum();

		}
		
		public double cD(double cL, Amount<Duration> time, Amount<Velocity> speed, Amount<Length> altitude) {

			double cD = MyMathUtils.getInterpolatedValue1DLinear(polarCLTakeOff, polarCDTakeOff, cL);

			double hb = (TakeOffNoiseTrajectoryCalc.this.getWingToGroundDistance().doubleValue(SI.METER) / TakeOffNoiseTrajectoryCalc.this.getSpan().doubleValue(SI.METER)) + altitude.doubleValue(SI.METER);
			// Aerodynamics For Naval Aviators: (Hurt)
			double kGround = 1.0;
			if(hb < 1.1)
				kGround = 1- (-4.48276577 * Math.pow(hb, 5) 
						+ 15.61174376 * Math.pow(hb, 4)
						- 21.20171050 * Math.pow(hb, 3)
						+ 14.39438721 * Math.pow(hb, 2)
						- 5.20913465 * hb
						+ 0.90793397);

			double cD0 = MyArrayUtils.getMin(polarCDTakeOff);
			double cDi = (cD-cD0)*kGround;

			double cDnew = cD0 + cDi;
			
			if((time.doubleValue(SI.SECOND) >= tLandingGearRetractionStart.doubleValue(SI.SECOND))
					&& (time.doubleValue(SI.SECOND) < tLandingGearRetractionEnd.doubleValue(SI.SECOND))) 
				cD = cDnew - deltaCD0LandingGear*deltaCD0LandingGearRetractionSlope.value(time.doubleValue(SI.SECOND));
			else if((time.doubleValue(SI.SECOND) >= tLandingGearRetractionEnd.doubleValue(SI.SECOND)))
				cD = cDnew - deltaCD0LandingGear;

			return cDnew;
		}

		public Amount<Force> drag(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Duration> time, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			double cD = cD(cL(alpha), time, speed, altitude);
			Amount<Force> drag = Amount.valueOf(
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(
							speed.doubleValue(SI.METERS_PER_SECOND) 
							+ (TakeOffNoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))),
							2)
							)
					*cD,
					SI.NEWTON
					);

			return drag;
		}

		public double cL(Amount<Angle> alpha) {
			
			double cL0 = TakeOffNoiseTrajectoryCalc.this.cL0;
			double cLalpha = TakeOffNoiseTrajectoryCalc.this.getcLalphaFlap().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			double alphaBody = alpha.doubleValue(NonSI.DEGREE_ANGLE);

			return cL0 + (cLalpha*alphaBody);

		}

		public Amount<Force> lift(Amount<Velocity> speed, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {

			double cL = cL(alpha);
			Amount<Force> lift = Amount.valueOf( 
					0.5
					*surface.doubleValue(SI.SQUARE_METRE)
					*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + 
							(TakeOffNoiseTrajectoryCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))),
							2)
							)
					*cL,
					SI.NEWTON
					);

			return lift;
		}

		public double mu(Amount<Velocity> speed) {
			return mu.value(speed.doubleValue(SI.METERS_PER_SECOND));
		}

		public double alphaDot(Amount<Duration> time) {

			double alphaDot = 0.0;

			if((time.doubleValue(SI.SECOND) > tRot.doubleValue(SI.SECOND)) && (time.doubleValue(SI.SECOND) < tHold.doubleValue(SI.SECOND))) {
				alphaDot = TakeOffNoiseTrajectoryCalc.this.getAlphaDotInitial()
						*(1-(TakeOffNoiseTrajectoryCalc.this.getkAlphaDot()*(TakeOffNoiseTrajectoryCalc.this.getAlphaPerStep().get(
								TakeOffNoiseTrajectoryCalc.this.getAlphaPerStep().size()-1).getEstimatedValue()))
								);
			}
			else if((time.doubleValue(SI.SECOND) > tEndHold.doubleValue(SI.SECOND)) && (time.doubleValue(SI.SECOND) <= tClimb.doubleValue(SI.SECOND))) 
				alphaDot = alphaRed;
			else if((time.doubleValue(SI.SECOND) > tZeroAcceleration.doubleValue(SI.SECOND)))
				alphaDot = (TakeOffNoiseTrajectoryCalc.this.alphaPerStep.get(
						TakeOffNoiseTrajectoryCalc.this.alphaPerStep.size()-1).doubleValue(NonSI.DEGREE_ANGLE)
						- TakeOffNoiseTrajectoryCalc.this.alphaPerStep.get(
								TakeOffNoiseTrajectoryCalc.this.alphaPerStep.size()-2).doubleValue(NonSI.DEGREE_ANGLE)
						) / (TakeOffNoiseTrajectoryCalc.this.timePerStep.get(TakeOffNoiseTrajectoryCalc.this.timePerStep.size()-1).doubleValue(SI.SECOND)
								- TakeOffNoiseTrajectoryCalc.this.timePerStep.get(TakeOffNoiseTrajectoryCalc.this.timePerStep.size()-2).doubleValue(SI.SECOND)
								);
			
			return alphaDot;
		}

		public Amount<Angle> alpha(Amount<Duration> time, Amount<Velocity> speed, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Angle> gamma, Amount<Force> weight) {

			Amount<Angle> alpha = TakeOffNoiseTrajectoryCalc.this.getAlphaGround().to(NonSI.DEGREE_ANGLE);

			if( time.doubleValue(SI.SECOND) > tRot.doubleValue(SI.SECOND) 
					&& time.doubleValue(SI.SECOND) <= tZeroAcceleration.doubleValue(SI.SECOND) 
					)
				alpha = Amount.valueOf(
						TakeOffNoiseTrajectoryCalc.this.getAlphaPerStep().get(
								TakeOffNoiseTrajectoryCalc.this.getAlphaPerStep().size()-1).doubleValue(NonSI.DEGREE_ANGLE)
						+(alphaDot(time)*(TakeOffNoiseTrajectoryCalc.this.getTimePerStep().get(
								TakeOffNoiseTrajectoryCalc.this.getTimePerStep().size()-1).doubleValue(SI.SECOND)
								- TakeOffNoiseTrajectoryCalc.this.getTimePerStep().get(
										TakeOffNoiseTrajectoryCalc.this.getTimePerStep().size()-2).doubleValue(SI.SECOND))),
						NonSI.DEGREE_ANGLE
						);
			else if(time.doubleValue(SI.SECOND) > tZeroAcceleration.doubleValue(SI.SECOND)) {

				int j=0;
				
				alpha = TakeOffNoiseTrajectoryCalc.this.alphaPerStep.get(
						TakeOffNoiseTrajectoryCalc.this.alphaPerStep.size()-1);
				
				List<Amount<Angle>> alphaList = new ArrayList<>();
				alphaList.add(alpha);
				double acceleration = 0.0; /* First guess value */
				
				int maxIterAlpha = 2000; /* max alpha excursion +-20° */
				do {
					
					Amount<Angle> alphaTemp = alphaList.get(alphaList.size()-1);
					
					acceleration = (g0/weight.doubleValue(SI.NEWTON))
							*( (thrust(speed, time, gamma, altitude, deltaTemperature).stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.cos(alphaTemp.doubleValue(SI.RADIAN))
									) 
									- drag(speed, alphaTemp, gamma, time, altitude, deltaTemperature).doubleValue(SI.NEWTON) 
									- ( weight.doubleValue(SI.NEWTON)
											*Math.sin(gamma.doubleValue(SI.RADIAN))
											)
									);
					if(alphaTemp.doubleValue(NonSI.DEGREE_ANGLE) > 0.0 ) {
						if (acceleration > 0) 
							alphaTemp = alphaTemp.plus(Amount.valueOf(0.01, NonSI.DEGREE_ANGLE));
						else if (acceleration < 0) 
							alphaTemp = alphaTemp.minus(Amount.valueOf(0.01, NonSI.DEGREE_ANGLE));
					}
					else if(alphaTemp.doubleValue(NonSI.DEGREE_ANGLE) < 0.0 ) {
						if (acceleration > 0) 
							alphaTemp = alphaTemp.minus(Amount.valueOf(0.01, NonSI.DEGREE_ANGLE));
						else if (acceleration < 0) 
							alphaTemp = alphaTemp.plus(Amount.valueOf(0.01, NonSI.DEGREE_ANGLE));
					}
					alphaList.add(alphaTemp);
					
					if(j > maxIterAlpha)
						break;
					
					j++;

				} while (Math.abs(acceleration) >= 1e-3);

				alpha = alphaList.get(alphaList.size()-1);
				
			}
			
			if(time.doubleValue(SI.SECOND) <= tEndRot.doubleValue(SI.SECOND))
				if(alpha.doubleValue(NonSI.DEGREE_ANGLE) >= fuselageUpsweepAngle.doubleValue(NonSI.DEGREE_ANGLE)) 
					isTailStrike = true;
			
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

	public FirstOrderIntegrator getTheIntegrator() {
		return theIntegrator;
	}

	public void setTheIntegrator(FirstOrderIntegrator theIntegrator) {
		this.theIntegrator = theIntegrator;
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

	public Amount<Duration> gettObstacle() {
		return tObstacle;
	}

	public void settObstacle(Amount<Duration> tObstacle) {
		this.tObstacle = tObstacle;
	}

	public Amount<Duration> gettZeroAccelration() {
		return tZeroAcceleration;
	}

	public void settZeroAccelration(Amount<Duration> tZeroAccelration) {
		this.tZeroAcceleration = tZeroAccelration;
	}

	public Amount<Length> getCutbackAltitude() {
		return cutbackAltitude;
	}

	public void setCutbackAltitude(Amount<Length> cutbackAltitude) {
		this.cutbackAltitude = cutbackAltitude;
	}

	public Amount<Duration> getDtThrustCutback() {
		return dtThrustCutback;
	}

	public void setDtThrustCutback(Amount<Duration> dtThrustCutback) {
		this.dtThrustCutback = dtThrustCutback;
	}

	public Amount<Duration> gettCutback() {
		return tCutback;
	}

	public void settCutback(Amount<Duration> tCutback) {
		this.tCutback = tCutback;
	}

	public MyInterpolatingFunction getDeltaThrustCutbackSlope() {
		return deltaThrustCutbackSlope;
	}

	public void setDeltaThrustCutbackSlope(MyInterpolatingFunction deltaThrustCutbackSlope) {
		this.deltaThrustCutbackSlope = deltaThrustCutbackSlope;
	}

	public Double getPhiCutback() {
		return phiCutback;
	}

	public void setPhiCutback(Double phiCutback) {
		this.phiCutback = phiCutback;
	}

	public Map<Double, List<Amount<Velocity>>> getSpeedMap() {
		return speedMap;
	}

	public void setSpeedMap(Map<Double, List<Amount<Velocity>>> speedMap) {
		this.speedMap = speedMap;
	}

	public Map<Double, List<Amount<Force>>> getThrustMap() {
		return thrustMap;
	}

	public void setThrustMap(Map<Double, List<Amount<Force>>> thrustMap) {
		this.thrustMap = thrustMap;
	}

	public Map<Double, List<Amount<Force>>> getThrustHorizontalMap() {
		return thrustHorizontalMap;
	}

	public void setThrustHorizontalMap(Map<Double, List<Amount<Force>>> thrustHorizontalMap) {
		this.thrustHorizontalMap = thrustHorizontalMap;
	}

	public Map<Double, List<Amount<Force>>> getThrustVerticalMap() {
		return thrustVerticalMap;
	}

	public void setThrustVerticalMap(Map<Double, List<Amount<Force>>> thrustVerticalMap) {
		this.thrustVerticalMap = thrustVerticalMap;
	}

	public Map<Double, List<Amount<Angle>>> getAlphaMap() {
		return alphaMap;
	}

	public void setAlphaMap(Map<Double, List<Amount<Angle>>> alphaMap) {
		this.alphaMap = alphaMap;
	}

	public Map<Double, List<Double>> getAlphaDotMap() {
		return alphaDotMap;
	}

	public void setAlphaDotMap(Map<Double, List<Double>> alphaDotMap) {
		this.alphaDotMap = alphaDotMap;
	}

	public Map<Double, List<Amount<Angle>>> getGammaMap() {
		return gammaMap;
	}

	public void setGammaMap(Map<Double, List<Amount<Angle>>> gammaMap) {
		this.gammaMap = gammaMap;
	}

	public Map<Double, List<Double>> getGammaDotMap() {
		return gammaDotMap;
	}

	public void setGammaDotMap(Map<Double, List<Double>> gammaDotMap) {
		this.gammaDotMap = gammaDotMap;
	}

	public Map<Double, List<Amount<Angle>>> getThetaMap() {
		return thetaMap;
	}

	public void setThetaMap(Map<Double, List<Amount<Angle>>> thetaMap) {
		this.thetaMap = thetaMap;
	}

	public Map<Double, List<Double>> getcLMap() {
		return cLMap;
	}

	public void setcLMap(Map<Double, List<Double>> cLMap) {
		this.cLMap = cLMap;
	}

	public Map<Double, List<Amount<Force>>> getLiftMap() {
		return liftMap;
	}

	public void setLiftMap(Map<Double, List<Amount<Force>>> liftMap) {
		this.liftMap = liftMap;
	}

	public Map<Double, List<Double>> getLoadFactorMap() {
		return loadFactorMap;
	}

	public void setLoadFactorMap(Map<Double, List<Double>> loadFactorMap) {
		this.loadFactorMap = loadFactorMap;
	}

	public Map<Double, List<Double>> getcDMap() {
		return cDMap;
	}

	public void setcDMap(Map<Double, List<Double>> cDMap) {
		this.cDMap = cDMap;
	}

	public Map<Double, List<Amount<Force>>> getDragMap() {
		return dragMap;
	}

	public void setDragMap(Map<Double, List<Amount<Force>>> dragMap) {
		this.dragMap = dragMap;
	}

	public Map<Double, List<Amount<Force>>> getFrictionMap() {
		return frictionMap;
	}

	public void setFrictionMap(Map<Double, List<Amount<Force>>> frictionMap) {
		this.frictionMap = frictionMap;
	}

	public Map<Double, List<Amount<Force>>> getTotalForceMap() {
		return totalForceMap;
	}

	public void setTotalForceMap(Map<Double, List<Amount<Force>>> totalForceMap) {
		this.totalForceMap = totalForceMap;
	}

	public Map<Double, List<Amount<Acceleration>>> getAccelerationMap() {
		return accelerationMap;
	}

	public void setAccelerationMap(Map<Double, List<Amount<Acceleration>>> accelerationMap) {
		this.accelerationMap = accelerationMap;
	}

	public Map<Double, List<Amount<Velocity>>> getRateOfClimbMap() {
		return rateOfClimbMap;
	}

	public void setRateOfClimbMap(Map<Double, List<Amount<Velocity>>> rateOfClimbMap) {
		this.rateOfClimbMap = rateOfClimbMap;
	}

	public Map<Double, List<Amount<Length>>> getGroundDistanceMap() {
		return groundDistanceMap;
	}

	public void setGroundDistanceMap(Map<Double, List<Amount<Length>>> groundDistanceMap) {
		this.groundDistanceMap = groundDistanceMap;
	}

	public Map<Double, List<Amount<Length>>> getVerticalDistance() {
		return getVerticalDistanceMap();
	}

	public void setVerticalDistance(Map<Double, List<Amount<Length>>> verticalDistanceMap) {
		this.setVerticalDistanceMap(verticalDistanceMap);
	}

	public Map<Double, List<Amount<Mass>>> getFuelUsed() {
		return fuelUsedMap;
	}

	public void setFuelUsed(Map<Double, List<Amount<Mass>>> fuelUsedMap) {
		this.fuelUsedMap = fuelUsedMap;
	}

	public Map<Double, List<Amount<Duration>>> getTimeMap() {
		return timeMap;
	}

	public void setTimeMap(Map<Double, List<Amount<Duration>>> timeMap) {
		this.timeMap = timeMap;
	}

	public Map<Double, List<Amount<Force>>> getWeightMap() {
		return weightMap;
	}

	public void setWeightMap(Map<Double, List<Amount<Force>>> weightMap) {
		this.weightMap = weightMap;
	}

	public boolean isTargetSpeedFlag() {
		return targetSpeedFlag;
	}

	public void setTargetSpeedFlag(boolean targetSpeedFlag) {
		this.targetSpeedFlag = targetSpeedFlag;
	}
	
	public boolean isCreateCSV() {
		return createCSV;
	}

	public void setCreateCSV(boolean createCSV) {
		this.createCSV = createCSV;
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

	public Map<Double, List<Amount<Length>>> getVerticalDistanceMap() {
		return verticalDistanceMap;
	}

	public void setVerticalDistanceMap(Map<Double, List<Amount<Length>>> verticalDistanceMap) {
		this.verticalDistanceMap = verticalDistanceMap;
	}

	public Map<Double, Amount<Length>> getCertificationPointsGroundDistanceMap() {
		return certificationPointsGroundDistanceMap;
	}

	public void setCertificationPointsGroundDistanceMap(Map<Double, Amount<Length>> certificationPointsGroundDistanceMap) {
		this.certificationPointsGroundDistanceMap = certificationPointsGroundDistanceMap;
	}

	public Map<Double, Amount<Length>> getCertificationPointsAltitudeMap() {
		return certificationPointsAltitudeMap;
	}

	public void setCertificationPointsAltitudeMap(Map<Double, Amount<Length>> certificationPointsAltitudeMap) {
		this.certificationPointsAltitudeMap = certificationPointsAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getCertificationPointsSpeedTASMap() {
		return certificationPointsSpeedTASMap;
	}

	public void setCertificationPointsSpeedTASMap(Map<Double, Amount<Velocity>> certificationPointsSpeedTASMap) {
		this.certificationPointsSpeedTASMap = certificationPointsSpeedTASMap;
	}

	public Map<Double, Amount<Velocity>> getCertificationPointsSpeedCASMap() {
		return certificationPointsSpeedCASMap;
	}

	public void setCertificationPointsSpeedCASMap(Map<Double, Amount<Velocity>> certificationPointsSpeedCASMap) {
		this.certificationPointsSpeedCASMap = certificationPointsSpeedCASMap;
	}

	public Map<Double, Amount<Angle>> getCertificationPointsAlphaMap() {
		return certificationPointsAlphaMap;
	}

	public void setCertificationPointsAlphaMap(Map<Double, Amount<Angle>> certificationPointsAlphaMap) {
		this.certificationPointsAlphaMap = certificationPointsAlphaMap;
	}

	public Map<Double, Amount<Angle>> getCertificationPointsGammaMap() {
		return certificationPointsGammaMap;
	}

	public void setCertificationPointsGammaMap(Map<Double, Amount<Angle>> certificationPointsGammaMap) {
		this.certificationPointsGammaMap = certificationPointsGammaMap;
	}

	public Map<Double, Amount<Angle>> getCertificationPointsThetaMap() {
		return certificationPointsThetaMap;
	}

	public void setCertificationPointsThetaMap(Map<Double, Amount<Angle>> certificationPointsThetaMap) {
		this.certificationPointsThetaMap = certificationPointsThetaMap;
	}

	public Map<Double, Amount<Force>> getCertificationPointsThrustMap() {
		return certificationPointsThrustMap;
	}

	public void setCertificationPointsThrustMap(Map<Double, Amount<Force>> certificationPointsThrustMap) {
		this.certificationPointsThrustMap = certificationPointsThrustMap;
	}

	public boolean isTailStrike() {
		return isTailStrike;
	}

	public void setTailStrike(boolean isTailStrike) {
		this.isTailStrike = isTailStrike;
	}

	public boolean isRotationSpeedWarningFlag() {
		return rotationSpeedWarningFlag;
	}

	public void setRotationSpeedWarningFlag(boolean rotationSpeedWarningFlag) {
		this.rotationSpeedWarningFlag = rotationSpeedWarningFlag;
	}

	public List<Amount<Velocity>> getSpeedPerStep() {
		return speedPerStep;
	}

	public void setSpeedPerStep(List<Amount<Velocity>> speedPerStep) {
		this.speedPerStep = speedPerStep;
	}

	public List<Amount<Velocity>> getRateOfClimbPerStep() {
		return rateOfClimbPerStep;
	}

	public void setRateOfClimbPerStep(List<Amount<Velocity>> rateOfClimbPerStep) {
		this.rateOfClimbPerStep = rateOfClimbPerStep;
	}

	public List<Amount<Force>> getThrustPerStep() {
		return thrustPerStep;
	}

	public void setThrustPerStep(List<Amount<Force>> thrustPerStep) {
		this.thrustPerStep = thrustPerStep;
	}

	public List<Amount<Force>> getThrustHorizontalPerStep() {
		return thrustHorizontalPerStep;
	}

	public void setThrustHorizontalPerStep(List<Amount<Force>> thrustHorizontalPerStep) {
		this.thrustHorizontalPerStep = thrustHorizontalPerStep;
	}

	public List<Amount<Force>> getThrustVerticalPerStep() {
		return thrustVerticalPerStep;
	}

	public void setThrustVerticalPerStep(List<Amount<Force>> thrustVerticalPerStep) {
		this.thrustVerticalPerStep = thrustVerticalPerStep;
	}

	public List<Amount<Force>> getLiftPerStep() {
		return liftPerStep;
	}

	public void setLiftPerStep(List<Amount<Force>> liftPerStep) {
		this.liftPerStep = liftPerStep;
	}

	public List<Amount<Force>> getDragPerStep() {
		return dragPerStep;
	}

	public void setDragPerStep(List<Amount<Force>> dragPerStep) {
		this.dragPerStep = dragPerStep;
	}

	public List<Amount<Force>> getTotalForcePerStep() {
		return totalForcePerStep;
	}

	public void setTotalForcePerStep(List<Amount<Force>> totalForcePerStep) {
		this.totalForcePerStep = totalForcePerStep;
	}

	public List<Amount<Force>> getFrictionPerStep() {
		return frictionPerStep;
	}

	public void setFrictionPerStep(List<Amount<Force>> frictionPerStep) {
		this.frictionPerStep = frictionPerStep;
	}

	public List<Amount<Angle>> getAlphaPerStep() {
		return alphaPerStep;
	}

	public void setAlphaPerStep(List<Amount<Angle>> alphaPerStep) {
		this.alphaPerStep = alphaPerStep;
	}

	public List<Amount<Angle>> getGammaPerStep() {
		return gammaPerStep;
	}

	public void setGammaPerStep(List<Amount<Angle>> gammaPerStep) {
		this.gammaPerStep = gammaPerStep;
	}

	public List<Amount<Angle>> getThetaPerStep() {
		return thetaPerStep;
	}

	public void setThetaPerStep(List<Amount<Angle>> thetaPerStep) {
		this.thetaPerStep = thetaPerStep;
	}

	public List<Double> getAlphaDotPerStep() {
		return alphaDotPerStep;
	}

	public void setAlphaDotPerStep(List<Double> alphaDotPerStep) {
		this.alphaDotPerStep = alphaDotPerStep;
	}

	public List<Double> getGammaDotPerStep() {
		return gammaDotPerStep;
	}

	public void setGammaDotPerStep(List<Double> gammaDotPerStep) {
		this.gammaDotPerStep = gammaDotPerStep;
	}

	public List<Double> getcLPerStep() {
		return cLPerStep;
	}

	public void setcLPerStep(List<Double> cLPerStep) {
		this.cLPerStep = cLPerStep;
	}

	public List<Double> getLoadFactorPerStep() {
		return loadFactorPerStep;
	}

	public void setLoadFactorPerStep(List<Double> loadFactorPerStep) {
		this.loadFactorPerStep = loadFactorPerStep;
	}

	public List<Double> getcDPerStep() {
		return cDPerStep;
	}

	public void setcDPerStep(List<Double> cDPerStep) {
		this.cDPerStep = cDPerStep;
	}

	public List<Amount<Acceleration>> getAccelerationPerStep() {
		return accelerationPerStep;
	}

	public void setAccelerationPerStep(List<Amount<Acceleration>> accelerationPerStep) {
		this.accelerationPerStep = accelerationPerStep;
	}

	public List<Amount<Length>> getGroundDistancePerStep() {
		return groundDistancePerStep;
	}

	public void setGroundDistancePerStep(List<Amount<Length>> groundDistancePerStep) {
		this.groundDistancePerStep = groundDistancePerStep;
	}

	public List<Amount<Length>> getVerticalDistancePerStep() {
		return verticalDistancePerStep;
	}

	public void setVerticalDistancePerStep(List<Amount<Length>> verticalDistancePerStep) {
		this.verticalDistancePerStep = verticalDistancePerStep;
	}

	public List<Amount<Duration>> getTimePerStep() {
		return timePerStep;
	}

	public void setTimePerStep(List<Amount<Duration>> timePerStep) {
		this.timePerStep = timePerStep;
	}

	public List<Amount<Mass>> getFuelUsedPerStep() {
		return fuelUsedPerStep;
	}

	public void setFuelUsedPerStep(List<Amount<Mass>> fuelUsedPerStep) {
		this.fuelUsedPerStep = fuelUsedPerStep;
	}

	public List<Amount<Force>> getWeightPerStep() {
		return weightPerStep;
	}

	public void setWeightPerStep(List<Amount<Force>> weightPerStep) {
		this.weightPerStep = weightPerStep;
	}

}