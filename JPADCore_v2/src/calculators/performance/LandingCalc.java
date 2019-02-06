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
 * This class have the purpose of calculating the landing trajectory (approach up to grond roll)
 * of a given aircraft assuming:
 *
 * - 3° of glide path
 * - V= 1.23*VsLND
 * - full flaps configuration and landing gear down
 * - Maximum landing weight 
 *
 * for each of them a step by step integration is used in solving the dynamic equation.
 *
 * @author Vittorio Trifari
 *
 */

public class LandingCalc {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private double aspectRatio;
	private Amount<Area> surface; 
	private Amount<Length> span;
	private PowerPlant thePowerPlant;
	private double[] polarCLLanding;
	private double[] polarCDLanding;
	private Amount<Duration> dtFlare, dtFreeRoll,
	tObstacle = Amount.valueOf(10000.0, SI.SECOND),  	  // initialization to an impossible time
	tFlareAltitude = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
	tTouchDown = Amount.valueOf(10000.0, SI.SECOND), 	  // initialization to an impossible time
	tZeroGamma = Amount.valueOf(10000.0, SI.SECOND); 	  // initialization to an impossible time
	private Amount<Mass> maxLandingMass; 
	private Amount<Velocity> vSLanding, vApproach, vFlare, vTouchDown, vWind, rateOfDescentAtFlareEnding;
	private Amount<Length> wingToGroundDistance, obstacle, intialAltitude, altitudeAtFlareEnding, hFlare, fieldAltitude;
	private Amount<Temperature> deltaTemperature;
	private Amount<Angle> gammaDescent, alphaGround;
	private Amount<Force> thrustAtFlareStart, thrustAtDescentStart;
	private List<Amount<Angle>> alpha;
	private List<Double> cL, cD;
	private List<Amount<Duration>> time;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> weight;
	private List<Double> timeBreakPoints, loadFactor;
	private double alphaDotFlare, cL0LND, cLmaxLND, kGround, phi, kCLmax, 
	cruiseThrustCorrectionFactor, fidlThrustCorrectionFactor, gidlThrustCorrectionFactor, 
	cruiseSfcCorrectionFactor, fidlSfcCorrectionFactor, gidlSfcCorrectionFactor,
	cruiseCalibrationFactorEmissionIndexNOx, cruiseCalibrationFactorEmissionIndexCO, cruiseCalibrationFactorEmissionIndexHC,
	cruiseCalibrationFactorEmissionIndexSoot, cruiseCalibrationFactorEmissionIndexCO2, cruiseCalibrationFactorEmissionIndexSOx,
	cruiseCalibrationFactorEmissionIndexH2O, flightIdleCalibrationFactorEmissionIndexNOx, flightIdleCalibrationFactorEmissionIndexCO,
	flightIdleCalibrationFactorEmissionIndexHC, flightIdleCalibrationFactorEmissionIndexSoot, flightIdleCalibrationFactorEmissionIndexCO2,
	flightIdleCalibrationFactorEmissionIndexSOx, flightIdleCalibrationFactorEmissionIndexH2O, groundIdleCalibrationFactorEmissionIndexNOx,
	groundIdleCalibrationFactorEmissionIndexCO, groundIdleCalibrationFactorEmissionIndexHC, groundIdleCalibrationFactorEmissionIndexSoot,
	groundIdleCalibrationFactorEmissionIndexCO2, groundIdleCalibrationFactorEmissionIndexSOx, groundIdleCalibrationFactorEmissionIndexH2O;
	private Amount<?> cLalphaLND;
	private MyInterpolatingFunction mu, muBrake, thrustFlareFunction;
	private boolean targetRDandAltitudeFlag, maximumFlareCLFlag;
	private boolean createCSV;

	private FirstOrderIntegrator theIntegrator;
	private FirstOrderDifferentialEquations ode;

	//OUTPUT:
	private List<Amount<Velocity>> speedTASList, speedCASList, rateOfClimbList;
	private List<Amount<Force>> thrustList, thrustHorizontalList, thrustVerticalList,
	liftList, dragList, totalForceList, frictionList;
	private List<Amount<Angle>> alphaList, gammaList, thetaList;
	private List<Double> alphaDotList, gammaDotList, cLList, loadFactorList, cDList, machList, fuelFlowList;
	private List<Amount<Acceleration>> accelerationList;
	private List<Amount<Length>> groundDistanceList, verticalDistanceList;
	private List<Amount<Duration>> timeList;
	private List<Amount<Mass>> fuelUsedList, emissionNOxList, emissionCOList, emissionHCList, emissionSootList, 
	emissionCO2List, emissionSOxList, emissionH2OList;
	private List<Amount<Force>> weightList;
	private Amount<Length> sDescent, sApproach, sFlare, sGround, sLanding, sTotal;
	private Amount<Velocity> vFlareEffective, vTouchDownEffective;
	private Amount<Duration> totalTime, landingTime;

	private final PrintStream originalOut = System.out;
	private PrintStream filterStream = new PrintStream(new OutputStream() {
		public void write(int b) {
			// write nothing
		}
	});

	//-------------------------------------------------------------------------------------
	// BUILDER:

	public LandingCalc(
			Amount<Length> initialAltitude,
			Amount<Length> fieldAltitude,
			Amount<Temperature> deltaTemperature,
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
			Amount<Length> wingToGroundDistance,
			double kCLmax,
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
			double cruiseCalibrationFactorEmissionIndexNOx,
			double cruiseCalibrationFactorEmissionIndexCO,
			double cruiseCalibrationFactorEmissionIndexHC,
			double cruiseCalibrationFactorEmissionIndexSoot,
			double cruiseCalibrationFactorEmissionIndexCO2,
			double cruiseCalibrationFactorEmissionIndexSOx,
			double cruiseCalibrationFactorEmissionIndexH2O,
			double flightIdleCalibrationFactorEmissionIndexNOx,
			double flightIdleCalibrationFactorEmissionIndexCO,
			double flightIdleCalibrationFactorEmissionIndexHC,
			double flightIdleCalibrationFactorEmissionIndexSoot,
			double flightIdleCalibrationFactorEmissionIndexCO2,
			double flightIdleCalibrationFactorEmissionIndexSOx,
			double flightIdleCalibrationFactorEmissionIndexH2O,
			double groundIdleCalibrationFactorEmissionIndexNOx,
			double groundIdleCalibrationFactorEmissionIndexCO,
			double groundIdleCalibrationFactorEmissionIndexHC,
			double groundIdleCalibrationFactorEmissionIndexSoot,
			double groundIdleCalibrationFactorEmissionIndexCO2,
			double groundIdleCalibrationFactorEmissionIndexSOx,
			double groundIdleCalibrationFactorEmissionIndexH2O,
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
		this.intialAltitude = initialAltitude.to(NonSI.FOOT).plus(fieldAltitude.to(NonSI.FOOT));
		this.fieldAltitude = fieldAltitude;
		this.deltaTemperature = deltaTemperature;
		this.gammaDescent = gammaDescent;
		this.dtFreeRoll = dtFreeRoll;
		this.mu = mu;
		this.muBrake = muBrake;
		this.obstacle = Amount.valueOf(50 + fieldAltitude.doubleValue(NonSI.FOOT), NonSI.FOOT);
		this.wingToGroundDistance = wingToGroundDistance;
		this.vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		this.alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		this.kCLmax = kCLmax;
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
		this.cruiseCalibrationFactorEmissionIndexNOx = cruiseCalibrationFactorEmissionIndexNOx;
		this.cruiseCalibrationFactorEmissionIndexCO = cruiseCalibrationFactorEmissionIndexCO;
		this.cruiseCalibrationFactorEmissionIndexHC = cruiseCalibrationFactorEmissionIndexHC;
		this.cruiseCalibrationFactorEmissionIndexSoot = cruiseCalibrationFactorEmissionIndexSoot;
		this.cruiseCalibrationFactorEmissionIndexCO2 = cruiseCalibrationFactorEmissionIndexCO2;
		this.cruiseCalibrationFactorEmissionIndexSOx = cruiseCalibrationFactorEmissionIndexSOx;
		this.cruiseCalibrationFactorEmissionIndexH2O = cruiseCalibrationFactorEmissionIndexH2O;
		this.flightIdleCalibrationFactorEmissionIndexNOx = flightIdleCalibrationFactorEmissionIndexNOx;
		this.flightIdleCalibrationFactorEmissionIndexCO = flightIdleCalibrationFactorEmissionIndexCO;
		this.flightIdleCalibrationFactorEmissionIndexHC = flightIdleCalibrationFactorEmissionIndexHC;
		this.flightIdleCalibrationFactorEmissionIndexSoot = flightIdleCalibrationFactorEmissionIndexSoot;
		this.flightIdleCalibrationFactorEmissionIndexCO2 = flightIdleCalibrationFactorEmissionIndexCO2;
		this.flightIdleCalibrationFactorEmissionIndexSOx = flightIdleCalibrationFactorEmissionIndexSOx;
		this.flightIdleCalibrationFactorEmissionIndexH2O = flightIdleCalibrationFactorEmissionIndexH2O;
		this.groundIdleCalibrationFactorEmissionIndexNOx = groundIdleCalibrationFactorEmissionIndexNOx;
		this.groundIdleCalibrationFactorEmissionIndexCO = groundIdleCalibrationFactorEmissionIndexCO;
		this.groundIdleCalibrationFactorEmissionIndexHC = groundIdleCalibrationFactorEmissionIndexHC;
		this.groundIdleCalibrationFactorEmissionIndexSoot = groundIdleCalibrationFactorEmissionIndexSoot;
		this.groundIdleCalibrationFactorEmissionIndexCO2 = groundIdleCalibrationFactorEmissionIndexCO2;
		this.groundIdleCalibrationFactorEmissionIndexSOx = groundIdleCalibrationFactorEmissionIndexSOx;
		this.groundIdleCalibrationFactorEmissionIndexH2O = groundIdleCalibrationFactorEmissionIndexH2O;
		
		// Reference velocities definition
		vSLanding = SpeedCalc.calculateSpeedStall(
				fieldAltitude,
				deltaTemperature, 
				maxLandingMass,
				surface,
				cLmaxLND
				);
		vApproach = vSLanding.times(1.23);
		vFlare = vSLanding.times(1.2);
		vTouchDown = vSLanding.times(1.15);

		/*
		 *  Averaged value of hFlare
		 *  @see https://www.flightliteracy.com/normal-approach-and-landing-part-four-round-out-flare/ 
		 */
		hFlare = Amount.valueOf(20.0 + fieldAltitude.doubleValue(NonSI.FOOT), NonSI.FOOT); 
		
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("CLmaxLND = " + cLmaxLND);
		System.out.println("CL0 = " + cLZeroLND);
		System.out.println("VsLND = " + vSLanding);
		System.out.println("VApproach = " + vApproach);
		System.out.println("Initial Descent Altitude = " + initialAltitude.to(NonSI.FOOT));
		System.out.println("Approach Altitude = " + obstacle.to(NonSI.FOOT));
		System.out.println("Flare Rotation Altitude = " + hFlare.to(NonSI.FOOT));
		System.out.println("Field Altitude = " + fieldAltitude.to(NonSI.FOOT));
		System.out.println("-----------------------------------------------------------\n");

		// List initialization
		this.time = new ArrayList<Amount<Duration>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.timeBreakPoints = new ArrayList<Double>();
		this.cL = new ArrayList<>();
		this.cD = new ArrayList<>();
		this.weight = new ArrayList<>();
		this.acceleration = new ArrayList<>();
		this.loadFactor = new ArrayList<>();

		// Output maps initialization
		this.speedTASList = new ArrayList<>();
		this.speedCASList = new ArrayList<>(); 
		this.rateOfClimbList = new ArrayList<>();
		this.thrustList = new ArrayList<>();
		this.thrustHorizontalList  = new ArrayList<>();
		this.thrustVerticalList = new ArrayList<>();
		this.liftList = new ArrayList<>();
		this.dragList = new ArrayList<>(); 
		this.totalForceList = new ArrayList<>();
		this.frictionList = new ArrayList<>();
		this.alphaList = new ArrayList<>(); 
		this.gammaList = new ArrayList<>();
		this.thetaList = new ArrayList<>();
		this.alphaDotList = new ArrayList<>();
		this.gammaDotList = new ArrayList<>();
		this.cLList = new ArrayList<>();
		this.loadFactorList = new ArrayList<>();
		this.cDList = new ArrayList<>();
		this.machList = new ArrayList<>();
		this.fuelFlowList = new ArrayList<>();
		this.accelerationList = new ArrayList<>();
		this.groundDistanceList = new ArrayList<>();
		this.verticalDistanceList = new ArrayList<>();
		this.timeList = new ArrayList<>();
		this.fuelUsedList = new ArrayList<>();
		this.emissionNOxList = new ArrayList<>();
		this.emissionCOList = new ArrayList<>();
		this.emissionHCList = new ArrayList<>();
		this.emissionSootList = new ArrayList<>(); 
		this.emissionCO2List = new ArrayList<>();
		this.emissionSOxList = new ArrayList<>();
		this.emissionH2OList = new ArrayList<>();
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
		cL.clear();
		cD.clear();
		loadFactor.clear();
		acceleration.clear();
		weight.clear();
		timeBreakPoints.clear();

		// values initialization
		double cLInitial = LiftCalc.calculateLiftCoeff(
				Amount.valueOf(
						maxLandingMass.doubleValue(SI.KILOGRAM)
						*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)
						*Math.cos(gammaDescent.doubleValue(SI.RADIAN)),
						SI.NEWTON
						),
				vApproach,
				LandingCalc.this.getSurface(),
				intialAltitude,
				deltaTemperature
				); 
		double cDInitial = MyMathUtils.getInterpolatedValue1DLinear(polarCLLanding, polarCDLanding, cLInitial);
		alpha.add(
				Amount.valueOf(
						((cLInitial - cL0LND)
								/ cLalphaLND.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()),
						NonSI.DEGREE_ANGLE
						)
				);
		cL.add(cLInitial);
		cD.add(cDInitial);
		acceleration.add(Amount.valueOf(0.0, SI.METERS_PER_SQUARE_SECOND));
		weight.add(
				Amount.valueOf(
						maxLandingMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
						SI.NEWTON
						)
				);
		loadFactor.add(1.0);
		time.add(Amount.valueOf(0.0, SI.SECOND));
		
		thrustAtDescentStart = Amount.valueOf( 
				gammaDescent.doubleValue(SI.RADIAN)*(maxLandingMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)) 
				+ ((DynamicsEquationsLanding)ode).drag(
						vApproach, 
						alpha.get(0),
						gammaDescent,
						intialAltitude,
						deltaTemperature
						).doubleValue(SI.NEWTON),
				SI.NEWTON
				);
		
		rateOfDescentAtFlareEnding = Amount.valueOf(10000.0, SI.METERS_PER_SECOND);  // Initialization at an impossible value
		altitudeAtFlareEnding = Amount.valueOf(10000.0, SI.METER);                   // Initialization at an impossible value
		
		tObstacle = Amount.valueOf(10000.0, SI.SECOND);		        // initialization to an impossible time
		tFlareAltitude = Amount.valueOf(10000.0, SI.SECOND);		// initialization to an impossible time
		tTouchDown = Amount.valueOf(10000.0, SI.SECOND);	        // initialization to an impossible time
		tZeroGamma = Amount.valueOf(10000.0, SI.SECOND);	        // initialization to an impossible time
		
		maximumFlareCLFlag = false;
	}

	/***************************************************************************************
	 * This method performs the integration of the equation of motion by solving a set of
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateLanding(boolean timeHistories) {

		System.out.println("---------------------------------------------------");
		System.out.println("NoiseTrajectoryCalc :: LANDING ODE integration\n\n");
		System.out.println("\tRUNNING SIMULATION ...\n\n");

		StepHandler continuousOutputModel = null;

		int i=0;
		int maxIter = 200;
		dtFlare = Amount.valueOf(4.0, SI.SECOND); // First guess value
		alphaDotFlare = 1.0; /* deg/s - First guess value */
		double newAlphaDotFlare = 0.0;
		Amount<Velocity> targetRateOfDescent = Amount.valueOf(-100, MyUnits.FOOT_PER_MINUTE);

		rateOfDescentAtFlareEnding = Amount.valueOf(10000.0, SI.METERS_PER_SECOND);  // Initialization at an impossible value
		altitudeAtFlareEnding = Amount.valueOf(10000.0, SI.METER);  // Initialization at an impossible value

		while (Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER) - 1e-2) >= 1.0 
				|| Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE)) >= Math.abs(targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)) ) {

			if(i > 0) 
				alphaDotFlare = newAlphaDotFlare;
			
			if(i > maxIter)
				break;
			
			theIntegrator = new HighamHall54Integrator(
					1e-10,
					1,
					1e-10,
					1e-10
					);
			ode = new DynamicsEquationsLanding();
			
			initialize();

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
									"\n\tx[2] = gamma = " + 0.0 + " °" +
									"\n\tx[3] = altitude = " + 0.0 + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					timeBreakPoints.add(t);
					sTotal = Amount.valueOf(x[0], SI.METER);
					sLanding = sTotal.to(SI.METER).minus(sDescent.to(SI.METER));
					sGround = sTotal.to(SI.METER).minus(sDescent.to(SI.METER)).minus(sApproach.to(SI.METER)).minus(sFlare.to(SI.METER));
					totalTime = Amount.valueOf(t, SI.SECOND);
					landingTime = totalTime.to(SI.SECOND).minus(tObstacle.to(SI.SECOND));

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
					System.out.println("\n\t\tEND OF DESCENT PHASE :: APPROACH PHASE ");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					tObstacle = Amount.valueOf(t, SI.SECOND);
					sDescent = Amount.valueOf(x[0], SI.METER);
					timeBreakPoints.add(t);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.CONTINUE;
				}
			};
			EventHandler ehCheckFlareAltitude = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					
					if(t < tFlareAltitude.doubleValue(SI.SECOND))
						return x[3] - hFlare.doubleValue(SI.METER);
					else
						return -10.0; /* Generic negative value to trigger the event only one time */

				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF APPROACH PHASE :: FLARE ROTATION");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m" +
									"\n\tx[4] = fuel used = " + x[4] + " kg"
							);

					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf(
							(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsLanding)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);

					tFlareAltitude = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					sApproach = Amount.valueOf(x[0] - sDescent.doubleValue(SI.METER), SI.METER);
					vFlareEffective = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					thrustAtFlareStart = 
							Amount.valueOf( 
									((DynamicsEquationsLanding)ode).thrust(
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
					
					List<Amount<Force>> thrustList = new ArrayList<>();
					for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
						thrustList.add(
								ThrustCalc.calculateThrustDatabase(
										thePowerPlant.getEngineList().get(i).getT0(),
										thePowerPlant.getEngineDatabaseReaderList().get(i),
										EngineOperatingConditionEnum.GIDL, 
										Amount.valueOf(0.0, SI.METER), 
										SpeedCalc.calculateMach(
												Amount.valueOf(0.0, SI.METER),
												deltaTemperature,
												Amount.valueOf(
														vTouchDown.doubleValue(SI.METERS_PER_SECOND) 
														+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
														SI.METERS_PER_SECOND
														)
												),
										deltaTemperature, 
										LandingCalc.this.getPhi(),
										LandingCalc.this.getGidlThrustCorrectionFactor()
										)
								);
					Amount<Force> thrustAtTouchDown = Amount.valueOf( 
							thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON
							);

					thrustFlareFunction = new MyInterpolatingFunction();
					thrustFlareFunction.interpolateLinear(
							new double[] {
									tFlareAltitude.doubleValue(SI.SECOND),
									tFlareAltitude.plus(dtFlare).doubleValue(SI.SECOND) 
							},
							new double[] {
									thrustAtFlareStart.doubleValue(SI.NEWTON),
									thrustAtTouchDown.doubleValue(SI.NEWTON)
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
						return x[3] - 1e-2;
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
					
					altitudeAtFlareEnding = Amount.valueOf(x[3], SI.METER);
					sFlare = Amount.valueOf(x[0] - sDescent.doubleValue(SI.METER) - sApproach.doubleValue(SI.METER), SI.METER);
					vTouchDownEffective = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					rateOfDescentAtFlareEnding = Amount.valueOf(
							x[1]*Math.sin(x[2]/57.3),
							SI.METERS_PER_SECOND
							);
					System.out.println("\nAltitude @ Flare Ending = " + altitudeAtFlareEnding);
					System.out.println("\nRate of Descent @ Flare Ending = " + rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE) + " ft/min");
					System.out.println("\nFlare Angular Velocity = " + alphaDotFlare + " °/s");
						
					tTouchDown = Amount.valueOf(t, SI.SECOND);
					timeBreakPoints.add(t);
					System.out.println("\n---------------------------DONE!-------------------------------");
					Action action = Action.CONTINUE;
					if ( Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER) - 1.0) >= 1.0 
							|| Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE)) >= Math.abs(targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE))
							)
						action = Action.STOP;
					return  action;
				}
			};

			theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckFlareAltitude, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckTouchDown, 1.0, 1e-3, 20);
			theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-10, 50);

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] x = interpolator.getInterpolatedState();
					double[] xDot = interpolator.getInterpolatedDerivatives();

					Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
					Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
					Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
					Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
					Amount<Force> weight = Amount.valueOf(
							(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							SI.NEWTON
							);
					Amount<Angle> alpha = ((DynamicsEquationsLanding)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight);

					//----------------------------------------------------------------------------------------
					// TIME:
					LandingCalc.this.getTime().add(Amount.valueOf(t, SI.SECOND));
					//----------------------------------------------------------------------------------------
					// ALPHA:
					LandingCalc.this.getAlpha().add(((DynamicsEquationsLanding)ode).alpha(time, speed, altitude, deltaTemperature, gamma, weight));
					//----------------------------------------------------------------------------------------
					// CL:
					LandingCalc.this.getcL().add(((DynamicsEquationsLanding)ode).cL(alpha));
					if(cL.get(cL.size()-1) > (kCLmax*cLmaxLND) ) 
						maximumFlareCLFlag = true;
					//----------------------------------------------------------------------------------------
					// CD:
					LandingCalc.this.getcD().add(
							((DynamicsEquationsLanding)ode).cD(
									((DynamicsEquationsLanding)ode).cL(alpha),
									altitude
									)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					LandingCalc.this.getAcceleration().add(
							Amount.valueOf(xDot[1], SI.METERS_PER_SQUARE_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// WEIGHT:
					LandingCalc.this.getWeight().add(weight);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					LandingCalc.this.getLoadFactor().add(
							(  ((DynamicsEquationsLanding)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
									+ (  ((DynamicsEquationsLanding)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
											.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
											*Math.sin(alpha.doubleValue(SI.RADIAN))
											)
									)
							/ ( weight.doubleValue(SI.NEWTON)*Math.cos(gamma.doubleValue(SI.RADIAN))	)
							);

				}
			};
			theIntegrator.addStepHandler(stepHandler);

			//----------------------------------------------------------------------------------------------
			// Use this handler for post-processing
			System.out.println("=================================================");
			System.out.println("Integration " + (i+1) + "\n\n");
			continuousOutputModel = new ContinuousOutputModel();
			theIntegrator.addStepHandler(continuousOutputModel);
			//----------------------------------------------------------------------------------------------

			// initial state
			double[] xAt0 = new double[] {
					0.0,
					vApproach.doubleValue(SI.METERS_PER_SECOND),
					gammaDescent.doubleValue(NonSI.DEGREE_ANGLE),
					intialAltitude.doubleValue(SI.METER),
					0.0
			}; 
			theIntegrator.integrate(ode, 0.0, xAt0, 10000, xAt0); // now xAt0 contains final state

			if (maximumFlareCLFlag == true) {
					System.err.println("ERROR: MAXIMUM ALLOWED CL DURING FLARE REACHED. THE LAST FLARE ANGULAR VELOCITY WILL BE CONSIDERED.");
					break;
			}
			
			if(Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE)) > targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE))
				if(Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE) - targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)) < 50.0)
					newAlphaDotFlare = alphaDotFlare + 0.02;
				else
					newAlphaDotFlare = alphaDotFlare + 0.1;
			else
				if(Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE) - targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)) < 50.0)
					newAlphaDotFlare = alphaDotFlare - 0.02;
				else
					newAlphaDotFlare = alphaDotFlare - 0.1;

			if(Math.abs(altitudeAtFlareEnding.doubleValue(SI.METER) - 1e-2) < 1 
					&& Math.abs(rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE)) < Math.abs(targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE))
					)
				targetRDandAltitudeFlag = true;
			
			if(i > maxIter) {
				break;
			}
			
			i++;
			
			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

		}

		if(targetRDandAltitudeFlag == true)
			manageOutputData(1.0, timeHistories, continuousOutputModel);
		else {
			System.err.println("ERROR: TARGET RATE OF CLIMB AND/OR ALTITUDE ARE NOT REACHED " 
					+ "\nRate of Descent current = "
					+ rateOfDescentAtFlareEnding.doubleValue(MyUnits.FOOT_PER_MINUTE) + " ft/min"
					+ "\nRate of Descent Target = "
					+ targetRateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE) + " ft/min"
					+ "\nAltuitude current = "
					+ altitudeAtFlareEnding.to(SI.METER)
					+ "\nAltitude Target = "
					+ 0.01 + " m"
					);
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
	public void manageOutputData(double dt, boolean timeHistories, StepHandler handler) {

		//-----------------------------------------------------------------------------
		// Collect the array of times and associated state vector values according
		// to the given dt and keeping the the discrete event-times (breakpoints)

		List<double[]> states = new ArrayList<double[]>();
		List<double[]> stateDerivatives = new ArrayList<double[]>();
		
		MyInterpolatingFunction alphaFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction loadFactorFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction accelerationFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction cLFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction cDFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction weightFunction = new MyInterpolatingFunction();
		
		alphaFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
				MyArrayUtils.convertListOfAmountTodoubleArray(this.alpha)
				);
		
		if(timeHistories) {
			loadFactorFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.loadFactor))
					);
			accelerationFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.acceleration)
					);
			cLFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.cL))
					);
			cDFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(this.cD))
					);
			weightFunction.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(this.time), 
					MyArrayUtils.convertListOfAmountTodoubleArray(this.weight)
					);
		}
		
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

				Amount<Duration> time = timeList.get(i);
				Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
				Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
				Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
				Amount<Force> weight = Amount.valueOf(
						weightFunction.value(timeList.get(i).doubleValue(SI.SECOND)),
						SI.NEWTON
						);
				Amount<Angle> alpha = Amount.valueOf(
						alphaFunction.value(timeList.get(i).doubleValue(SI.SECOND)),
						NonSI.DEGREE_ANGLE
						);

				if(time.doubleValue(SI.SECOND) >= tTouchDown.doubleValue(SI.SECOND))
					gamma = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
				
				//========================================================================================
				// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
				//----------------------------------------------------------------------------------------
				// GROUND DISTANCE:
				this.groundDistanceList.add(Amount.valueOf(
						x[0],
						SI.METER)
						);
				//----------------------------------------------------------------------------------------
				// VERTICAL DISTANCE:
				this.verticalDistanceList.add(Amount.valueOf(
						x[3],
						SI.METER)
						);
				//----------------------------------------------------------------------------------------
				// THRUST:
				this.thrustList.add(Amount.valueOf(
						((DynamicsEquationsLanding)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
						.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON)
						);
				//--------------------------------------------------------------------------------
				// FUEL USED (kg):
				this.fuelUsedList.add(Amount.valueOf(x[4], SI.KILOGRAM));
				//-----------------------------------------------------------
				// FUEL FLOW (kg/s):
				this.fuelFlowList.add(xDot[4]);
				//----------------------------------------------------------------------------------------
				if(timeHistories) {
					//----------------------------------------------------------------------------------------
					// WEIGHT:
					this.weightList.add(weight);
					//----------------------------------------------------------------------------------------
					// SPEED TAS:
					this.speedTASList.add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
					//----------------------------------------------------------------------------------------
					// SPEED CAS:
					double sigma = AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)/1.225);
					this.speedCASList.add(speed.times(Math.sqrt(sigma)));
					//----------------------------------------------------------------------------------------
					// MACH:
					double speedOfSound = AtmosphereCalc.getSpeedOfSound(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS));
					this.machList.add(speed.doubleValue(SI.METERS_PER_SECOND) / speedOfSound);
					//----------------------------------------------------------------------------------------
					// THRUST HORIZONTAL:
					this.thrustHorizontalList.add(Amount.valueOf(
							((DynamicsEquationsLanding)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
							.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
							*Math.cos(alpha.doubleValue(SI.RADIAN)),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// THRUST VERTICAL:
					this.thrustVerticalList.add(Amount.valueOf(
							((DynamicsEquationsLanding)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
							.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
							*Math.sin(alpha.doubleValue(SI.RADIAN)),
							SI.NEWTON)
							);
					//--------------------------------------------------------------------------------
					// FRICTION:
					if(this.timeList.get(i).doubleValue(SI.SECOND) >= this.tTouchDown.doubleValue(SI.SECOND))
						this.frictionList.add(Amount.valueOf(
								((DynamicsEquationsLanding)ode).mu(speed)
								*(weight.doubleValue(SI.NEWTON)
										- ((DynamicsEquationsLanding)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
										),
								SI.NEWTON)
								);
					else if(this.timeList.get(i).doubleValue(SI.SECOND) >= 
							this.tTouchDown.to(SI.SECOND).plus(this.dtFreeRoll.to(SI.SECOND)).doubleValue(SI.SECOND)
							)
						this.frictionList.add(Amount.valueOf(
								((DynamicsEquationsLanding)ode).muBrake(speed)
								*(weight.doubleValue(SI.NEWTON)
										- ((DynamicsEquationsLanding)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
										),
								SI.NEWTON)
								);
					else
						this.frictionList.add(Amount.valueOf(0.0, SI.NEWTON));
					//----------------------------------------------------------------------------------------
					// LIFT:
					this.liftList.add(((DynamicsEquationsLanding)ode).lift(speed, alpha, gamma, altitude, deltaTemperature));
					//----------------------------------------------------------------------------------------
					// DRAG:
					this.dragList.add(((DynamicsEquationsLanding)ode).drag(speed, alpha, gamma, altitude, deltaTemperature));
					//----------------------------------------------------------------------------------------
					// TOTAL FORCE:
					this.totalForceList.add(Amount.valueOf(
							(((DynamicsEquationsLanding)ode).thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight)
									.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
									*Math.cos(alpha.doubleValue(SI.RADIAN))
									)
							- ((DynamicsEquationsLanding)ode).drag(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
							- (((DynamicsEquationsLanding)ode).mu(speed)
									*(weight.doubleValue(SI.NEWTON)
											- ((DynamicsEquationsLanding)ode).lift(speed, alpha, gamma, altitude, deltaTemperature).doubleValue(SI.NEWTON)
											)
									)
							- (weight.doubleValue(SI.NEWTON)*Math.sin(gamma.doubleValue(SI.RADIAN))
									),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					this.loadFactorList.add(loadFactorFunction.value(time.doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// RATE OF CLIMB:
					this.rateOfClimbList.add(Amount.valueOf(
							xDot[3],
							SI.METERS_PER_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					this.accelerationList.add(Amount.valueOf(
							accelerationFunction.value(time.doubleValue(SI.SECOND)),
							SI.METERS_PER_SQUARE_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					this.alphaList.add(alpha);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					this.gammaList.add(gamma);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					if(this.timeList.get(i).doubleValue(SI.SECOND) > this.tFlareAltitude.doubleValue(SI.SECOND) 
							&& this.timeList.get(i).doubleValue(SI.SECOND) <= this.tTouchDown.doubleValue(SI.SECOND)
							) 
						this.alphaDotList.add(this.alphaDotFlare);
					else
						this.alphaDotList.add(0.0);
					//----------------------------------------------------------------------------------------
					// GAMMA DOT:
					this.gammaDotList.add(xDot[2]);
					//----------------------------------------------------------------------------------------
					// THETA:
					this.thetaList.add(Amount.valueOf(
							alpha.doubleValue(NonSI.DEGREE_ANGLE) + gamma.doubleValue(NonSI.DEGREE_ANGLE),
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// CL:				
					this.cLList.add(cLFunction.value(time.doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// CD:
					this.cDList.add(cDFunction.value(time.doubleValue(SI.SECOND)));
					//----------------------------------------------------------------------------------------
					// EMISSIONS:
					for(int iEng=0; iEng < thePowerPlant.getEngineNumber(); i++) {
						
						//----------------------------------------------------------------------------------------
						// EMISSIONS NOx:
						this.emissionNOxList.add(((DynamicsEquationsLanding)ode).emissionNOx(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS CO:
						this.emissionCOList.add(((DynamicsEquationsLanding)ode).emissionCO(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS HC:
						this.emissionHCList.add(((DynamicsEquationsLanding)ode).emissionHC(speed, time, gamma, alpha, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS Soot:
						this.emissionSootList.add(((DynamicsEquationsLanding)ode).emissionSoot(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS CO2:
						this.emissionCO2List.add(((DynamicsEquationsLanding)ode).emissionCO2(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS SOx:
						this.emissionSOxList.add(((DynamicsEquationsLanding)ode).emissionSOx(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
						//----------------------------------------------------------------------------------------
						// EMISSIONS H2O:
						this.emissionH2OList.add(((DynamicsEquationsLanding)ode).emissionH2O(speed, time, alpha, gamma, altitude, deltaTemperature, weight, fuelUsedList.get(i)));
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
	public void createOutputCharts(String outputFolderPath) throws InstantiationException, IllegalAccessException {

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
				MyArrayUtils.convertListOfAmountTodoubleArray(speedTASList),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				simulationDetailsOutputFolder, "Speed_evolution_SI",
				createCSV
				);



		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedTASList.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "kn",
				simulationDetailsOutputFolder, "Speed_evolution_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// speed v.s. ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(speedTASList),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				simulationDetailsOutputFolder, "Speed_vs_GroundDistance_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						speedTASList.stream()
						.map(x -> x.to(NonSI.KNOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "ft", "kn",
				simulationDetailsOutputFolder, "Speed_vs_GroundDistance_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// acceleration v.s. time

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
				0.0, null, -10.0, 10.0,
				"Time", "Acceleration", "s", "m/(s^2)",
				simulationDetailsOutputFolder, "Acceleration_evolution_SI",
				createCSV
				);


		//.................................................................................
		// acceleration v.s. ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
				0.0, null, -10.0, 10.0,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				simulationDetailsOutputFolder, "Acceleration_vs_GroundDistance_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(accelerationList),
				0.0, null, -10.0, 10.0,
				"Ground Distance", "Acceleration", "ft", "m/(s^2)",
				simulationDetailsOutputFolder, "Acceleration_vs_GroundDistance_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// load factor v.s. time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertToDoublePrimitive(loadFactorList),
				0.0, null, 0.0, null,
				"Time", "Load Factor", "s", "",
				simulationDetailsOutputFolder, "LoadFactor_evolution",
				createCSV
				);

		//.................................................................................
		// load factor v.s. ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList), 
				MyArrayUtils.convertToDoublePrimitive(loadFactorList),
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				simulationDetailsOutputFolder, "LoadFactor_vs_GroundDistance_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						), 
				MyArrayUtils.convertToDoublePrimitive(loadFactorList),
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "ft", "",
				simulationDetailsOutputFolder, "LoadFactor_vs_GroundDistance_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// Rate of Climb v.s. Time

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbList),
				0.0, null, null, 0.0,
				"Time", "Rate of Climb", "s", "m/s",
				simulationDetailsOutputFolder, "RateOfClimb_evolution_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						rateOfClimbList.stream()
						.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
						.collect(Collectors.toList())
						),
				0.0, null, null, 0.0,
				"Time", "Rate of Climb", "s", "ft/min",
				simulationDetailsOutputFolder, "RateOfClimb_evolution_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// Rate of Climb v.s. Ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList), 
				MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimbList),
				0.0, null, null, 0.0,
				"Ground distance", "Rate of Climb", "m", "m/s",
				simulationDetailsOutputFolder, "RateOfClimb_vs_GroundDistance_SI",
				createCSV
				);


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
				simulationDetailsOutputFolder, "RateOfClimb_vs_GroundDistance_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// CL v.s. Time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertToDoublePrimitive(cLList),
				0.0, null, 0.0, null,
				"Time", "CL", "s", "",
				simulationDetailsOutputFolder, "CL_evolution",
				createCSV
				);

		//.................................................................................
		// CL v.s. Ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertToDoublePrimitive(cLList),
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				simulationDetailsOutputFolder, "CL_vs_GroundDistance_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(cLList),
				0.0, null, 0.0, null,
				"Ground distance", "CL", "ft", "",
				simulationDetailsOutputFolder, "CL_vs_GroundDistance_IMPERIAL",
				createCSV
				);

		//.................................................................................
		// CD v.s. Time
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertToDoublePrimitive(cDList),
				0.0, null, 0.0, null,
				"Time", "CD", "s", "",
				simulationDetailsOutputFolder, "CD_evolution",
				createCSV
				);

		//.................................................................................
		// CD v.s. Ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertToDoublePrimitive(cDList),
				0.0, null, 0.0, null,
				"Ground distance", "CD", "m", "",
				simulationDetailsOutputFolder, "CD_vs_GroundDistance_SI",
				createCSV
				);


		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistanceList.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(cDList),
				0.0, null, 0.0, null,
				"Ground distance", "CD", "ft", "",
				simulationDetailsOutputFolder, "CD_vs_GroundDistance_IMPERIAL",
				createCSV
				);

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
				0.0, null, -10.0, null,
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
					0.0, null, -10.0, null,
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
					0.0, null, -10.0, null,
					"Ground Distance", "Angular Velocity", "ft", "deg/s",
					new String[] {"Alpha_dot", "Gamma_dot"},
					simulationDetailsOutputFolder, "AngularVelocity_vs_GroundDistance_SI",
					createCSV);
		}

		System.out.println("\n---------------------------DONE!-------------------------------");

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
		List<Amount<Length>> landingGroundDistance = groundDistanceList.stream()
				.map(s -> s.to(SI.METER).minus(sDescent.to(SI.METER)))
				.collect(Collectors.toList());
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, 0.0, null, 
				"Ground distance", "Altitude",
				"m", "m",
				trajectoryOutputFolder, "Total_Trajectory_SI", createCSV
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(landingGroundDistance),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, null, obstacle.doubleValue(SI.METER), 
				"Ground distance", "Altitude",
				"m", "m",
				trajectoryOutputFolder, "Landing_Trajectory_SI", createCSV
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
				trajectoryOutputFolder, "Total_Trajectory_IMPERIAL", createCSV
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						landingGroundDistance.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						verticalDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				0.0, null, null, obstacle.doubleValue(NonSI.FOOT), 
				"Ground distance", "Altitude",
				"ft", "ft",
				trajectoryOutputFolder, "Landing_Trajectory_IMPERIAL", createCSV
				);

		//.................................................................................
		// vertical distance v.s. time
		List<Amount<Duration>> landingTime = timeList.stream()
				.map(t -> t.to(SI.SECOND).minus(tObstacle.to(SI.SECOND)))
				.collect(Collectors.toList());
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, 0.0, null, 
				"Time", "Altitude",
				"s", "m",
				trajectoryOutputFolder, "Total_Altitude_evolution_SI", createCSV
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(landingTime),
				MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistanceList),
				0.0, null, null, obstacle.doubleValue(SI.METER), 
				"Time", "Altitude",
				"s", "m",
				trajectoryOutputFolder, "Landing_Altitude_evolution_SI", createCSV
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						verticalDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				0.0, null, 0.0, null, 
				"Time", "Altitude",
				"s", "ft",
				trajectoryOutputFolder, "Total_Altitude_evolution_IMPERIAL", createCSV
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(landingTime),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						verticalDistanceList.stream().map(x -> x.to(NonSI.FOOT)).collect(Collectors.toList())
						),
				0.0, null, null, obstacle.doubleValue(NonSI.FOOT),  
				"Time", "Altitude",
				"s", "ft",
				trajectoryOutputFolder, "Landing_Altitude_evolution_IMPERIAL", createCSV
				);

		System.setOut(originalOut);
		System.out.println("\tPRINTING THRUST CHARTS TO FILE ...");
		System.setOut(filterStream);

		//.................................................................................
		// thrust v.s. time
		Double yMaxThrust = null;
		Double yMinThrust = null;
		if(MyArrayUtils.getMin(thrustList.stream().mapToDouble(ff -> ff.doubleValue(SI.NEWTON)).toArray()) == 0.0
				&& MyArrayUtils.getMax(thrustList.stream().mapToDouble(ff -> ff.doubleValue(SI.NEWTON)).toArray()) == 0.0) {
			yMaxThrust = 1.0; /* Generic positive value */
			yMinThrust = -1.0; /* Generic positive value */
		}
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(thrustList),
				0.0, null, yMinThrust, yMaxThrust, 
				"Time", "Thrust",
				"s", "N",
				thrustOutputFolder, "Thrust_evolution_SI", createCSV
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						thrustList.stream().map(x -> x.to(NonSI.POUND_FORCE)).collect(Collectors.toList())
						),
				0.0, null, yMinThrust, yMaxThrust, 
				"Time", "Thrust",
				"s", "lbf",
				thrustOutputFolder, "Thrust_evolution_IMPERIAL", createCSV
				);

		//.................................................................................
		// thrust v.s. ground distance

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(thrustList),
				0.0, null, yMinThrust, yMaxThrust, 
				"Ground distance", "Thrust",
				"m", "N",
				thrustOutputFolder, "Thrust_vs_GroundDistance_SI", createCSV
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
				0.0, null, yMinThrust, yMaxThrust,
				"Ground distance", "Thrust",
				"ft", "lbf",
				thrustOutputFolder, "Thrust_vs_GroundDistance_IMPERIAL", createCSV
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
				fuelUsedOutputFolder, "FuelUsed_evolution_SI", createCSV
				);

		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timeList),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						fuelUsedList.stream().map(x -> x.to(NonSI.POUND)).collect(Collectors.toList())
						),
				0.0, null, 0.0, yMaxFuel, 
				"Time", "Fuel used",
				"s", "lb",
				fuelUsedOutputFolder, "FuelUsed_evolution_IMPERIAL", createCSV
				);

		//.................................................................................
		// fuelUsed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(groundDistanceList),
				MyArrayUtils.convertListOfAmountTodoubleArray(fuelUsedList),
				0.0, null, 0.0, yMaxFuel, 
				"Ground distance", "Fuel Used",
				"m", "kg",
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_SI", createCSV
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
				fuelUsedOutputFolder, "FuelUsed_vs_GroundDistance_IMPERIAL", createCSV
				);

		System.setOut(originalOut);

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
			return 5;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {

			Amount<Duration> time = Amount.valueOf(t, SI.SECOND);
			Amount<Velocity> speed = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
			Amount<Angle> gamma = Amount.valueOf(x[2], NonSI.DEGREE_ANGLE);
			Amount<Length> altitude = Amount.valueOf(x[3], SI.METER);
			Amount<Force> weight = Amount.valueOf(
					(maxLandingMass.doubleValue(SI.KILOGRAM) - x[4])*g0,
					SI.NEWTON
					);
			Amount<Angle> alpha = alpha(time, speed, altitude, deltaTemperature, gamma, weight);

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

			if (time.doubleValue(SI.SECOND) <= tFlareAltitude.doubleValue(SI.SECOND)) {

				Amount<Force> totalThrust = thrustAtDescentStart;
				
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(totalThrust.divide(LandingCalc.this.getThePowerPlant().getEngineNumber()));
				
			}
			else if(time.doubleValue(SI.SECOND) > tFlareAltitude.doubleValue(SI.SECOND) && time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {

				Amount<Force> totalThrust = Amount.valueOf(
						thrustFlareFunction.value(time.doubleValue(SI.SECOND)),
						SI.NEWTON
						);

				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(totalThrust.divide(LandingCalc.this.getThePowerPlant().getEngineNumber()));

			}
			else if(time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND)) {

				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					theThrustList.add(
							ThrustCalc.calculateThrustDatabase(
									thePowerPlant.getEngineList().get(i).getT0(),
									thePowerPlant.getEngineDatabaseReaderList().get(i),
									EngineOperatingConditionEnum.GIDL, 
									Amount.valueOf(0.0, SI.METER), 
									SpeedCalc.calculateMach(
											Amount.valueOf(0.0, SI.METER),
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									deltaTemperature, 
									LandingCalc.this.getPhi(),
									LandingCalc.this.getGidlThrustCorrectionFactor()
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
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
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
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					fuelFlowCruiseList.add(
							LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
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
							LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
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
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					fuelFlowList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSfc(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGidlSfcCorrectionFactor()
									)
							*(0.224809)*(0.454/3600)
							*thrustList.get(i).doubleValue(SI.NEWTON)
							);

				fuelFlow = fuelFlowList.stream().mapToDouble(ff -> ff).sum();
			}

			return fuelFlow;
			
		}

		public Amount<Mass> emissionNOx(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexNOx = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexNOxList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexNOxList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexNOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexNOx()
									)
							);
					flightIdleEmissionIndexNOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexNOx()
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
				double cruiseEmissionIndexNOxFromDatabase = cruiseEmissionIndexNOxList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexNOxFromDatabase = flightIdleEmissionIndexNOxList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexNOx = (cruiseEmissionIndexNOxFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexNOxFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexNOx = (cruiseEmissionIndexNOxFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexNOxFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexNOxList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexNOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getNOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexNOx()
									)
							);

				emissionIndexNOx = emissionIndexNOxList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexNOx*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionCO(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexCO = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexCOList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexCOList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexCOList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexCO()
									)
							);
					flightIdleEmissionIndexCOList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexCO()
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
				double cruiseEmissionIndexCOFromDatabase = cruiseEmissionIndexCOList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexCOFromDatabase = flightIdleEmissionIndexCOList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexCO = (cruiseEmissionIndexCOFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexCOFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexCO = (cruiseEmissionIndexCOFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexCOFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexCOList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexCOList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getCOEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexCO()
									)
							);

				emissionIndexCO = emissionIndexCOList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexCO*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionHC(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexHC = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexHCList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexHCList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexHCList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexHC()
									)
							);
					flightIdleEmissionIndexHCList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexHC()
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
				double cruiseEmissionIndexHCFromDatabase = cruiseEmissionIndexHCList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexHCFromDatabase = flightIdleEmissionIndexHCList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexHC = (cruiseEmissionIndexHCFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexHCFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexHC = (cruiseEmissionIndexHCFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexHCFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexHCList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexHCList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getHCEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexHC()
									)
							);

				emissionIndexHC = emissionIndexHCList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexHC*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionSoot(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexSoot = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexSootList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexSootList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexSootList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexSoot()
									)
							);
					flightIdleEmissionIndexSootList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexSoot()
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
				double cruiseEmissionIndexSootFromDatabase = cruiseEmissionIndexSootList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexSootFromDatabase = flightIdleEmissionIndexSootList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexSoot = (cruiseEmissionIndexSootFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexSootFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexSoot = (cruiseEmissionIndexSootFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexSootFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexSootList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexSootList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSootEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexSoot()
									)
							);

				emissionIndexSoot = emissionIndexSootList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexSoot*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionCO2(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexCO2 = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexCO2List = new ArrayList<>();
				List<Double> flightIdleEmissionIndexCO2List = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexCO2List.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexCO2()
									)
							);
					flightIdleEmissionIndexCO2List.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexCO2()
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
				double cruiseEmissionIndexCO2FromDatabase = cruiseEmissionIndexCO2List.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexCO2FromDatabase = flightIdleEmissionIndexCO2List.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexCO2 = (cruiseEmissionIndexCO2FromDatabase*weightCruise)
						+ (flightIdleEmissionIndexCO2FromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexCO2 = (cruiseEmissionIndexCO2FromDatabase*weightCruise)
							+ (flightIdleEmissionIndexCO2FromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexCO2List = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexCO2List.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getCO2EmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexCO2()
									)
							);

				emissionIndexCO2 = emissionIndexCO2List.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexCO2*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionSOx(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexSOx = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexSOxList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexSOxList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexSOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexSOx()
									)
							);
					flightIdleEmissionIndexSOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexSOx()
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
				double cruiseEmissionIndexSOxFromDatabase = cruiseEmissionIndexSOxList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexSOxFromDatabase = flightIdleEmissionIndexSOxList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexSOx = (cruiseEmissionIndexSOxFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexSOxFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexSOx = (cruiseEmissionIndexSOxFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexSOxFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexSOxList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexSOxList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getSOxEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexSOx()
									)
							);

				emissionIndexSOx = emissionIndexSOxList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexSOx*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public Amount<Mass> emissionH2O(Amount<Velocity> speed, Amount<Duration> time, Amount<Angle> alpha, Amount<Angle> gamma, Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Force> weight, Amount<Mass> fuelUsed) {

			double emissionIndexH2O = 0.0;
			
			if (time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {
				
				List<Amount<Force>> thrustList = thrust(speed, time, alpha, gamma, altitude, deltaTemperature, weight);
				List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
				List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
				List<Double> cruiseEmissionIndexH2OList = new ArrayList<>();
				List<Double> flightIdleEmissionIndexH2OList = new ArrayList<>();
				
				Amount<Force> totalThrust = Amount.valueOf( 
						thrustList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						);
				
				for(int ieng=0; ieng<LandingCalc.this.getThePowerPlant().getEngineNumber(); ieng++) {
					cruiseThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.CRUISE, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getCruiseThrustCorrectionFactor()
									)
							);

					flightIdleThrustDatabaseTemp.add(
							ThrustCalc.calculateThrustDatabase(
									LandingCalc.this.getThePowerPlant().getEngineList().get(ieng).getT0(),
									LandingCalc.this.getThePowerPlant().getEngineDatabaseReaderList().get(ieng),
									EngineOperatingConditionEnum.FIDL, 
									altitude, 
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											), 
									deltaTemperature, 
									1.0, /* Throttle setting cruise */
									LandingCalc.this.getFidlThrustCorrectionFactor()
									)
							);
					cruiseEmissionIndexH2OList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.CRUISE,
									LandingCalc.this.getCruiseCalibrationFactorEmissionIndexH2O()
									)
							);
					flightIdleEmissionIndexH2OList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.FIDL,
									LandingCalc.this.getFlightIdleCalibrationFactorEmissionIndexH2O()
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
				double cruiseEmissionIndexH2OFromDatabase = cruiseEmissionIndexH2OList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				double flightIdleEmissionIndexH2OFromDatabase = flightIdleEmissionIndexH2OList.stream().mapToDouble(cthr -> cthr.doubleValue()).average().getAsDouble();
				
				// first guess values
				double weightCruise = 0.5;
				double weightFlightIdle = 0.5;

				Amount<Force> interpolatedThrust = Amount.valueOf(
								(cruiseThrustFromDatabase.doubleValue(SI.NEWTON)*weightCruise)
								+ (flightIdleThrustFromDatabase.doubleValue(SI.NEWTON)*weightFlightIdle),
								SI.NEWTON);
				emissionIndexH2O = (cruiseEmissionIndexH2OFromDatabase*weightCruise)
						+ (flightIdleEmissionIndexH2OFromDatabase*weightFlightIdle);
				
				int iter = 0;
				int maxIter = 50;
				// iterative loop for the definition of the cruise and flight idle weights
				while (
						(Math.abs(
								(totalThrust.doubleValue(SI.NEWTON)
										- interpolatedThrust.doubleValue(SI.NEWTON))
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
					emissionIndexH2O = (cruiseEmissionIndexH2OFromDatabase*weightCruise)
							+ (flightIdleEmissionIndexH2OFromDatabase*weightFlightIdle);
					
					iter++;
					
				}
			}
			else if (time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))  {
				
				List<Double> emissionIndexH2OList = new ArrayList<>();
				for (int i=0; i<LandingCalc.this.getThePowerPlant().getEngineNumber(); i++) 
					emissionIndexH2OList.add(
							thePowerPlant.getEngineDatabaseReaderList().get(i).getH2OEmissionIndex(
									SpeedCalc.calculateMach(
											altitude,
											deltaTemperature,
											Amount.valueOf(
													speed.doubleValue(SI.METERS_PER_SECOND) 
													+ LandingCalc.this.vWind.doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN)),
													SI.METERS_PER_SECOND
													)
											),
									altitude,
									deltaTemperature,
									LandingCalc.this.getPhi(),
									EngineOperatingConditionEnum.GIDL,
									LandingCalc.this.getGroundIdleCalibrationFactorEmissionIndexH2O()
									)
							);

				emissionIndexH2O = emissionIndexH2OList.stream().mapToDouble(ff -> ff).average().getAsDouble();
			}

			return Amount.valueOf(emissionIndexH2O*fuelUsed.doubleValue(SI.KILOGRAM), SI.GRAM);
			
		}
		
		public double cD(double cL, Amount<Length> altitude) {

			double hb = (LandingCalc.this.getWingToGroundDistance().doubleValue(SI.METER) / LandingCalc.this.getSpan().doubleValue(SI.METER)) + altitude.doubleValue(SI.METER);
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
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (LandingCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
					*cD,
					SI.NEWTON
					);
			
		}

		public double cL(Amount<Angle> alpha) {

			double cL0 = LandingCalc.this.cL0LND;
			double cLalpha = LandingCalc.this.getcLalphaLND().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			double alphaBody = alpha.doubleValue(NonSI.DEGREE_ANGLE);
			double cL = cL0 + cLalpha*alphaBody;

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
					*(Math.pow(speed.doubleValue(SI.METERS_PER_SECOND) + (LandingCalc.this.getvWind().doubleValue(SI.METERS_PER_SECOND)*Math.cos(gamma.doubleValue(SI.RADIAN))), 2))
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
			
			int maxIterAlpha = 100; /* max alpha excursion +-1° */
			if(time.doubleValue(SI.SECOND) <= tFlareAltitude.doubleValue(SI.SECOND)) {

				int j=0;

				alpha = LandingCalc.this.getAlpha().get(
						LandingCalc.this.getAlpha().size()-1
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

					if(Math.abs(gammaDot) >= 1e-3) {
						if (gammaDot > 0) 
							alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) - 0.01, NonSI.DEGREE_ANGLE);
						else
							alpha = Amount.valueOf(alpha.doubleValue(NonSI.DEGREE_ANGLE) + 0.01, NonSI.DEGREE_ANGLE);
					}
					
					if(j > maxIterAlpha)
						break;
					
					j++;
					
				} while (Math.abs(gammaDot) >= 1e-3);
				
			}
			else if( time.doubleValue(SI.SECOND) > tFlareAltitude.doubleValue(SI.SECOND) && time.doubleValue(SI.SECOND) <= tTouchDown.doubleValue(SI.SECOND)) {

				alpha = Amount.valueOf(
						LandingCalc.this.getAlpha().get(
								LandingCalc.this.getAlpha().size()-1).doubleValue(NonSI.DEGREE_ANGLE)
						+(alphaDotFlare*(LandingCalc.this.getTime().get(
								LandingCalc.this.getTime().size()-1).doubleValue(SI.SECOND)
								- LandingCalc.this.getTime().get(
										LandingCalc.this.getTime().size()-2).doubleValue(SI.SECOND))),
						NonSI.DEGREE_ANGLE
						);
				
			}
			else if( time.doubleValue(SI.SECOND) > tTouchDown.doubleValue(SI.SECOND))
				alpha = LandingCalc.this.getAlphaGround().to(NonSI.DEGREE_ANGLE);

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

	public Amount<Length> getObstacle() {
		return obstacle;
	}

	public Amount<Length> getIntialAltitude() {
		return intialAltitude;
	}

	public Amount<Angle> getGammaDescent() {
		return gammaDescent;
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

	public List<Amount<Velocity>> getSpeedTASList() {
		return speedTASList;
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

	public void setObstacle(Amount<Length> obstacle) {
		this.obstacle = obstacle;
	}

	public void setIntialAltitude(Amount<Length> intialAltitude) {
		this.intialAltitude = intialAltitude;
	}

	public void setGammaDescent(Amount<Angle> gammaDescent) {
		this.gammaDescent = gammaDescent;
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

	public void setSpeedTASList(List<Amount<Velocity>> speedList) {
		this.speedTASList = speedList;
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

	public Amount<Duration> gettZeroGamma() {
		return tZeroGamma;
	}

	public void settZeroGamma(Amount<Duration> tZeroGamma) {
		this.tZeroGamma = tZeroGamma;
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

	public Amount<Velocity> getRateOfDescentAtFlareEnding() {
		return rateOfDescentAtFlareEnding;
	}

	public void setRateOfDescentAtFlareEnding(Amount<Velocity> rateOfDescentAtFlareEnding) {
		this.rateOfDescentAtFlareEnding = rateOfDescentAtFlareEnding;
	}

	public boolean isTargetRDandAltitudeFlag() {
		return targetRDandAltitudeFlag;
	}

	public void setTargetRDandAltitudeFlag(boolean targetRDandAltitudeFlag) {
		this.targetRDandAltitudeFlag = targetRDandAltitudeFlag;
	}

	public Amount<Velocity> getvFlare() {
		return vFlare;
	}

	public void setvFlare(Amount<Velocity> vFlare) {
		this.vFlare = vFlare;
	}

	public Amount<Duration> gettFlareAltitude() {
		return tFlareAltitude;
	}

	public void settFlareAltitude(Amount<Duration> tFlareAltitude) {
		this.tFlareAltitude = tFlareAltitude;
	}

	public Amount<Length> gethFlare() {
		return hFlare;
	}

	public void sethFlare(Amount<Length> hFlare) {
		this.hFlare = hFlare;
	}

	public double getkCLmax() {
		return kCLmax;
	}

	public void setkCLmax(double kCLmax) {
		this.kCLmax = kCLmax;
	}

	public boolean isMaximumFlareCLFlag() {
		return maximumFlareCLFlag;
	}

	public void setMaximumFlareCLFlag(boolean maximumFlareCLFlag) {
		this.maximumFlareCLFlag = maximumFlareCLFlag;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public Amount<Force> getThrustAtDescentStart() {
		return thrustAtDescentStart;
	}

	public void setThrustAtDescentStart(Amount<Force> thrustAtDescentStart) {
		this.thrustAtDescentStart = thrustAtDescentStart;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public List<Amount<Force>> getWeight() {
		return weight;
	}

	public void setWeight(List<Amount<Force>> weight) {
		this.weight = weight;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}

	public List<Double> getcD() {
		return cD;
	}

	public void setcD(List<Double> cD) {
		this.cD = cD;
	}

	public Amount<Length> getsDescent() {
		return sDescent;
	}

	public void setsDescent(Amount<Length> sDescent) {
		this.sDescent = sDescent;
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

	public Amount<Temperature> getDeltaTemperature() {
		return deltaTemperature;
	}

	public void setDeltaTemperature(Amount<Temperature> deltaTemperature) {
		this.deltaTemperature = deltaTemperature;
	}

	public Amount<Length> getFieldAltitude() {
		return fieldAltitude;
	}

	public void setFieldAltitude(Amount<Length> fieldAltitude) {
		this.fieldAltitude = fieldAltitude;
	}

	public Amount<Velocity> getvFlareEffective() {
		return vFlareEffective;
	}

	public void setvFlareEffective(Amount<Velocity> vFlareEffective) {
		this.vFlareEffective = vFlareEffective;
	}

	public Amount<Velocity> getvTouchDownEffective() {
		return vTouchDownEffective;
	}

	public void setvTouchDownEffective(Amount<Velocity> vTouchDownEffective) {
		this.vTouchDownEffective = vTouchDownEffective;
	}

	public Amount<Duration> getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Amount<Duration> totalTime) {
		this.totalTime = totalTime;
	}

	public Amount<Duration> getLandingTime() {
		return landingTime;
	}

	public void setLandingTime(Amount<Duration> landingTime) {
		this.landingTime = landingTime;
	}

	public Amount<Length> getsLanding() {
		return sLanding;
	}

	public void setsLanding(Amount<Length> sLanding) {
		this.sLanding = sLanding;
	}

	public double getCruiseCalibrationFactorEmissionIndexNOx() {
		return cruiseCalibrationFactorEmissionIndexNOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexNOx(double cruiseCalibrationFactorEmissionIndexNOx) {
		this.cruiseCalibrationFactorEmissionIndexNOx = cruiseCalibrationFactorEmissionIndexNOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO() {
		return cruiseCalibrationFactorEmissionIndexCO;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO(double cruiseCalibrationFactorEmissionIndexCO) {
		this.cruiseCalibrationFactorEmissionIndexCO = cruiseCalibrationFactorEmissionIndexCO;
	}

	public double getCruiseCalibrationFactorEmissionIndexHC() {
		return cruiseCalibrationFactorEmissionIndexHC;
	}

	public void setCruiseCalibrationFactorEmissionIndexHC(double cruiseCalibrationFactorEmissionIndexHC) {
		this.cruiseCalibrationFactorEmissionIndexHC = cruiseCalibrationFactorEmissionIndexHC;
	}

	public double getCruiseCalibrationFactorEmissionIndexSoot() {
		return cruiseCalibrationFactorEmissionIndexSoot;
	}

	public void setCruiseCalibrationFactorEmissionIndexSoot(double cruiseCalibrationFactorEmissionIndexSoot) {
		this.cruiseCalibrationFactorEmissionIndexSoot = cruiseCalibrationFactorEmissionIndexSoot;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO2() {
		return cruiseCalibrationFactorEmissionIndexCO2;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO2(double cruiseCalibrationFactorEmissionIndexCO2) {
		this.cruiseCalibrationFactorEmissionIndexCO2 = cruiseCalibrationFactorEmissionIndexCO2;
	}

	public double getCruiseCalibrationFactorEmissionIndexSOx() {
		return cruiseCalibrationFactorEmissionIndexSOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexSOx(double cruiseCalibrationFactorEmissionIndexSOx) {
		this.cruiseCalibrationFactorEmissionIndexSOx = cruiseCalibrationFactorEmissionIndexSOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexH2O() {
		return cruiseCalibrationFactorEmissionIndexH2O;
	}

	public void setCruiseCalibrationFactorEmissionIndexH2O(double cruiseCalibrationFactorEmissionIndexH2O) {
		this.cruiseCalibrationFactorEmissionIndexH2O = cruiseCalibrationFactorEmissionIndexH2O;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexNOx() {
		return flightIdleCalibrationFactorEmissionIndexNOx;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexNOx(double flightIdleCalibrationFactorEmissionIndexNOx) {
		this.flightIdleCalibrationFactorEmissionIndexNOx = flightIdleCalibrationFactorEmissionIndexNOx;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexCO() {
		return flightIdleCalibrationFactorEmissionIndexCO;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexCO(double flightIdleCalibrationFactorEmissionIndexCO) {
		this.flightIdleCalibrationFactorEmissionIndexCO = flightIdleCalibrationFactorEmissionIndexCO;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexHC() {
		return flightIdleCalibrationFactorEmissionIndexHC;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexHC(double flightIdleCalibrationFactorEmissionIndexHC) {
		this.flightIdleCalibrationFactorEmissionIndexHC = flightIdleCalibrationFactorEmissionIndexHC;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexSoot() {
		return flightIdleCalibrationFactorEmissionIndexSoot;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexSoot(double flightIdleCalibrationFactorEmissionIndexSoot) {
		this.flightIdleCalibrationFactorEmissionIndexSoot = flightIdleCalibrationFactorEmissionIndexSoot;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexCO2() {
		return flightIdleCalibrationFactorEmissionIndexCO2;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexCO2(double flightIdleCalibrationFactorEmissionIndexCO2) {
		this.flightIdleCalibrationFactorEmissionIndexCO2 = flightIdleCalibrationFactorEmissionIndexCO2;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexSOx() {
		return flightIdleCalibrationFactorEmissionIndexSOx;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexSOx(double flightIdleCalibrationFactorEmissionIndexSOx) {
		this.flightIdleCalibrationFactorEmissionIndexSOx = flightIdleCalibrationFactorEmissionIndexSOx;
	}

	public double getFlightIdleCalibrationFactorEmissionIndexH2O() {
		return flightIdleCalibrationFactorEmissionIndexH2O;
	}

	public void setFlightIdleCalibrationFactorEmissionIndexH2O(double flightIdleCalibrationFactorEmissionIndexH2O) {
		this.flightIdleCalibrationFactorEmissionIndexH2O = flightIdleCalibrationFactorEmissionIndexH2O;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexNOx() {
		return groundIdleCalibrationFactorEmissionIndexNOx;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexNOx(double groundIdleCalibrationFactorEmissionIndexNOx) {
		this.groundIdleCalibrationFactorEmissionIndexNOx = groundIdleCalibrationFactorEmissionIndexNOx;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexCO() {
		return groundIdleCalibrationFactorEmissionIndexCO;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexCO(double groundIdleCalibrationFactorEmissionIndexCO) {
		this.groundIdleCalibrationFactorEmissionIndexCO = groundIdleCalibrationFactorEmissionIndexCO;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexHC() {
		return groundIdleCalibrationFactorEmissionIndexHC;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexHC(double groundIdleCalibrationFactorEmissionIndexHC) {
		this.groundIdleCalibrationFactorEmissionIndexHC = groundIdleCalibrationFactorEmissionIndexHC;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexSoot() {
		return groundIdleCalibrationFactorEmissionIndexSoot;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexSoot(double groundIdleCalibrationFactorEmissionIndexSoot) {
		this.groundIdleCalibrationFactorEmissionIndexSoot = groundIdleCalibrationFactorEmissionIndexSoot;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexCO2() {
		return groundIdleCalibrationFactorEmissionIndexCO2;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexCO2(double groundIdleCalibrationFactorEmissionIndexCO2) {
		this.groundIdleCalibrationFactorEmissionIndexCO2 = groundIdleCalibrationFactorEmissionIndexCO2;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexSOx() {
		return groundIdleCalibrationFactorEmissionIndexSOx;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexSOx(double groundIdleCalibrationFactorEmissionIndexSOx) {
		this.groundIdleCalibrationFactorEmissionIndexSOx = groundIdleCalibrationFactorEmissionIndexSOx;
	}

	public double getGroundIdleCalibrationFactorEmissionIndexH2O() {
		return groundIdleCalibrationFactorEmissionIndexH2O;
	}

	public void setGroundIdleCalibrationFactorEmissionIndexH2O(double groundIdleCalibrationFactorEmissionIndexH2O) {
		this.groundIdleCalibrationFactorEmissionIndexH2O = groundIdleCalibrationFactorEmissionIndexH2O;
	}

	public List<Amount<Velocity>> getSpeedCASList() {
		return speedCASList;
	}

	public void setSpeedCASList(List<Amount<Velocity>> speedCASList) {
		this.speedCASList = speedCASList;
	}

	public List<Double> getMachList() {
		return machList;
	}

	public void setMachList(List<Double> machList) {
		this.machList = machList;
	}

	public List<Double> getFuelFlowList() {
		return fuelFlowList;
	}

	public void setFuelFlowList(List<Double> fuelFlowList) {
		this.fuelFlowList = fuelFlowList;
	}

	public List<Amount<Mass>> getEmissionNOxList() {
		return emissionNOxList;
	}

	public void setEmissionNOxList(List<Amount<Mass>> emissionNOxList) {
		this.emissionNOxList = emissionNOxList;
	}

	public List<Amount<Mass>> getEmissionCOList() {
		return emissionCOList;
	}

	public void setEmissionCOList(List<Amount<Mass>> emissionCOList) {
		this.emissionCOList = emissionCOList;
	}

	public List<Amount<Mass>> getEmissionHCList() {
		return emissionHCList;
	}

	public void setEmissionHCList(List<Amount<Mass>> emissionHCList) {
		this.emissionHCList = emissionHCList;
	}

	public List<Amount<Mass>> getEmissionSootList() {
		return emissionSootList;
	}

	public void setEmissionSootList(List<Amount<Mass>> emissionSootList) {
		this.emissionSootList = emissionSootList;
	}

	public List<Amount<Mass>> getEmissionCO2List() {
		return emissionCO2List;
	}

	public void setEmissionCO2List(List<Amount<Mass>> emissionCO2List) {
		this.emissionCO2List = emissionCO2List;
	}

	public List<Amount<Mass>> getEmissionSOxList() {
		return emissionSOxList;
	}

	public void setEmissionSOxList(List<Amount<Mass>> emissionSOxList) {
		this.emissionSOxList = emissionSOxList;
	}

	public List<Amount<Mass>> getEmissionH2OList() {
		return emissionH2OList;
	}

	public void setEmissionH2OList(List<Amount<Mass>> emissionH2OList) {
		this.emissionH2OList = emissionH2OList;
	}

}