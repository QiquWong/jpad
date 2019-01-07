package sandbox2.vt.analyses.tests;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import calculators.aerodynamics.MomentCalc;
import calculators.performance.LandingNoiseTrajectoryCalc;
import calculators.performance.TakeOffNoiseTrajectoryCalc;
import calculators.performance.ThrustCalc;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import sandbox2.vt.analyses.CompleteAnalysisTest;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

class MyArgumentsNoiseTrajectory {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-ioc", aliases = { "--input-operating-condition" }, required = true,
			usage = "operating conditions input file")
	private File _inputFileOperatingCondition;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	@Option(name = "-df", aliases = { "--dir-fuselages" }, required = true,
			usage = "fuselages directory path")
	private File _fuselagesDirectory;

	@Option(name = "-dls", aliases = { "--dir-lifting-surfaces" }, required = true,
			usage = "lifting surfaces directory path")
	private File _liftingSurfacesDirectory;

	@Option(name = "-de", aliases = { "--dir-engines" }, required = true,
			usage = "engines directory path")
	private File _enginesDirectory;

	@Option(name = "-dn", aliases = { "--dir-nacelles" }, required = true,
			usage = "nacelles directory path")
	private File _nacellesDirectory;

	@Option(name = "-dlg", aliases = { "--dir-landing-gears" }, required = true,
			usage = "landing gears directory path")
	private File _landingGearsDirectory;

	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getOperatingConditionsInputFile() {
		return _inputFileOperatingCondition;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	public File getFuselagesDirectory() {
		return _fuselagesDirectory;
	}

	public File getLiftingSurfacesDirectory() {
		return _liftingSurfacesDirectory;
	}

	public File getEnginesDirectory() {
		return _enginesDirectory;
	}

	public File getNacellesDirectory() {
		return _nacellesDirectory;
	}

	public File getLandingGearsDirectory() {
		return _landingGearsDirectory;
	}

	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
}

public class NoiseTrajectoryCalcTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;
	public static OperatingConditions theOperatingConditions;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the aircraft object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the aircraft object ...");

		Aircraft aircraft = CompleteAnalysisTest.theAircraft;
		if (aircraft == null) {
			System.out.println("aircraft object null, returning.");
			return;
		}

	}; // end-of-Runnable

	/**
	 * Main
	 *
	 * @param args
	 * @throws InvalidFormatException 
	 */
	public static void main(String[] args) throws InvalidFormatException {

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		long startTime = System.currentTimeMillis();        

		System.out.println("-------------------");
		System.out.println("Noise Trajectory Test");
		System.out.println("-------------------");

		MyArgumentsNoiseTrajectory va = new MyArgumentsNoiseTrajectory();
		CompleteAnalysisTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			CompleteAnalysisTest.theCmdLineParser.parseArgument(args);

			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			String pathToOperatingConditionsXML = va.getOperatingConditionsInputFile().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);

			String dirLiftingSurfaces = va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);

			String dirEngines = va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);

			String dirNacelles = va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);

			String dirLandingGears = va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);

			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);

			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();

			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("\n\n\tCreating the Aircraft ... \n\n");

			// deactivating system.out
			System.setOut(filterStream);

			// reading aircraft from xml ... 
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader);

			// activating system.out
			System.setOut(originalOut);			
			System.out.println(theAircraft.toString());
			System.setOut(filterStream);

			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String outputFolderTakeOff = JPADStaticWriteUtils.createNewFolder(folderPath + "take_off_noise_trajectory" + File.separator);
			String outputFolderLanding = JPADStaticWriteUtils.createNewFolder(folderPath + "landing_noise_trajectory" + File.separator);

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());

			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
			
			//======================================================================
			// TAKE-OFF NOISE TRAJECTORIES (ISA+10°C)
			System.out.println("\n\n\tTAKE-OFF NOISE TRAJECTORIES ... \n\n");
			//======================================================================
			// INPUT DATA TO BE ASSIGNED FROM FILE
			boolean timeHistories = true;
			boolean takeOffSimulation = true;
			boolean landingSimulation = false;
			
			//......................................................................
			Amount<Length> xEndSimulation = Amount.valueOf(8000, SI.METER);
			Amount<Length> cutbackAltitude = Amount.valueOf(984, NonSI.FOOT); //  also to be done at 1500ft and 2000ft
			int numberOfThrustSettingCutback = 3;
			Amount<Mass> maxTakeOffMass = Amount.valueOf(54576, SI.KILOGRAM);
			double[] polarCLTakeOff = new double[] {1.683505301,1.727959984,1.774656575,1.823332257,1.873724213,1.925569626,1.978605681,2.032569559,2.087198445,2.142229523,2.197399974,2.252446983,2.307107733,2.361119408,2.41421919,2.466144263,2.516631811,2.565419016,2.612243063,2.656841134,2.698950412,2.738308082,2.774651327,2.807717329,2.837243272,2.86296634,2.884623715,2.901952582,2.914690124,2.922573523,2.925339964};
			double[] polarCDTakeOff = new double[] {0.134322844,0.134490111,0.134824043,0.135326555,0.135999805,0.136846218,0.137868518,0.139069743,0.14045327,0.142022824,0.143782489,0.145736715,0.147890322,0.150248492,0.15281677,0.155601049,0.158607558,0.161842847,0.165313759,0.16902741,0.172991157,0.177212561,0.181699357,0.186459405,0.191500649,0.196831062,0.202458601,0.20839114,0.214636415,0.221201956,0.228095016};
			double deltaCD0LandingGear = 0.015;
			double deltaCD0OEI = 0.0050;
			double xcgPosition = -0.22;
			Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
			Amount<Duration> dtLandingGearRetraction = Amount.valueOf(12, SI.SECOND);
			Amount<Duration> dtThrustCutback = Amount.valueOf(4, SI.SECOND);
			double kcLMax = 0.8;
			double kRot = 1.05;
			double alphaDotInitial = 3.0; // (deg/s)
			double kAlphaDot = 0.06; // (1/deg)
			double cLmaxTO = 2.86;
			double cLZeroTO = 1.6358;
			Amount<?> cLalphaFlap = Amount.valueOf(0.1031, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction mu = new MyInterpolatingFunction();
			mu.interpolateLinear(
					new double[]{0, 10000},
					new double[]{0.025, 0.025}
					);
			MyInterpolatingFunction tauRudder = new MyInterpolatingFunction();
			tauRudder.interpolateLinear(
					new double[]{0.0000, 0.5359, 0.5648, 0.5502, 0.5261},
					new double[]{0.0, 10.0, 20.0, 25.0, 30.0}
					);
			
			boolean createCSV = true;
			
			//======================================================================
			if(takeOffSimulation) {
				TakeOffNoiseTrajectoryCalc theTakeOffNoiseTrajectoryCalculator = new TakeOffNoiseTrajectoryCalc(
						xEndSimulation,
						cutbackAltitude,
						maxTakeOffMass,
						theAircraft.getPowerPlant(),
						polarCLTakeOff, 
						polarCDTakeOff, 
						theOperatingConditions.getAltitudeTakeOff(),
						deltaCD0LandingGear, 
						deltaCD0OEI,
						theAircraft.getWing().getAspectRatio(),
						theAircraft.getWing().getSurfacePlanform(),
						dtHold, 
						dtLandingGearRetraction, 
						dtThrustCutback,
						theOperatingConditions.getThrottleTakeOff(),
						kcLMax,
						kRot,
						alphaDotInitial,
						kAlphaDot,
						mu,
						theAircraft.getWing().getRiggingAngle(),
						cLmaxTO,
						cLZeroTO,
						cLalphaFlap,
						createCSV
						);

				
				Amount<Velocity> vMC = calculateVMC(xcgPosition, maxTakeOffMass, cLmaxTO, tauRudder);
				theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(false, null, timeHistories,vMC);
				theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(true, null, timeHistories, vMC);

				double lowestPhiCutback = theTakeOffNoiseTrajectoryCalculator.getPhiCutback();
				double[] phiArray = MyArrayUtils.linspace( (lowestPhiCutback + 0.1), 0.9, numberOfThrustSettingCutback);

				Arrays.stream(phiArray).forEach(
						throttle -> theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(
								true,
								throttle, 
								timeHistories,
								vMC
								)
						);

				if(theTakeOffNoiseTrajectoryCalculator.isTargetSpeedFlag() == true)
					try {
						theTakeOffNoiseTrajectoryCalculator.createOutputCharts(outputFolderTakeOff, timeHistories);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				else {
					System.err.println("TERMINATING ... ");
					System.exit(1);
				}
			}
			
			//======================================================================
			// LANDING NOISE TRAJECTORY
			System.out.println("\n\n\tLANDING NOISE TRAJECTORY ... \n\n");
			//======================================================================
			// INPUT DATA TO BE ASSIGNED FROM FILE
			Amount<Length> initialAltitude = Amount.valueOf(4000, NonSI.FOOT);
			Amount<Angle> gammaDescent = Amount.valueOf(-3, NonSI.DEGREE_ANGLE);
			Amount<Mass> maxLandingMass = Amount.valueOf(49118, SI.KILOGRAM);
			Double[] polarCLLanding = new Double[] {1.603339861,1.701665544,1.799865498,1.897942475,1.99589925,2.093738612,2.191463366,2.289076333,2.386580344,2.483978244,2.581272886,2.678331247,2.775427969,2.872565929,2.969476233,3.066297654,3.163033076,3.250850775,3.336353224,3.408798774};
			Double[] polarCDLanding = new Double[] {0.161176711,0.170918928,0.181227248,0.192099765,0.20353464,0.215530106,0.228084464,0.241196089,0.254863421,0.269084974,0.28385933,0.299163319,0.315038518,0.331486092,0.348458885,0.365978439,0.384043756,0.400929562,0.417814455,0.432463932};
			Amount<Duration> dtFlare = Amount.valueOf(3, SI.SECOND);
			Amount<Duration> dtFreeRoll = Amount.valueOf(2, SI.SECOND);
			double cLmaxLND = 3.34;
			double cLZeroLND = 1.9396;
			Amount<?> cLalphaLND = Amount.valueOf(0.1082, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction muBrake = new MyInterpolatingFunction();
//			muBrake.interpolateLinear(
//					new double[]{0,5.144,10.228,15.432,20.576,25.72,30.864,36.008,41.152},
//					new double[]{0.94,0.89,0.85,0.8,0.74,0.68,0.62,0.57,0.52}
//					);
			muBrake.interpolateLinear(
					new double[]{0, 10000},
					new double[]{0.4, 0.4}
					);

			
			//======================================================================
			if(landingSimulation) {
				LandingNoiseTrajectoryCalc theLandingNoiseTrajectoryCalculator = new LandingNoiseTrajectoryCalc(
						initialAltitude, 
						gammaDescent,
						maxLandingMass,
						theAircraft.getPowerPlant(), 
						polarCLLanding, 
						polarCDLanding, 
						theAircraft.getWing().getAspectRatio(),
						theAircraft.getWing().getSurfacePlanform(),
						dtFlare,
						dtFreeRoll, 
						mu, 
						muBrake,
						theAircraft.getWing().getRiggingAngle(),
						cLmaxLND,
						cLZeroLND,
						cLalphaLND,
						theOperatingConditions.getThrottleLanding(),
						createCSV
						);

				theLandingNoiseTrajectoryCalculator.calculateNoiseLandingTrajectory(timeHistories);

				try {
					theLandingNoiseTrajectoryCalculator.createOutputCharts(outputFolderLanding, timeHistories);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			//--------------------------------END-----------------------------------
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");

			System.setOut(filterStream);

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			DirectionalStabilityTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ... (if needed)
		launch(args);
	}

	public static Amount<Velocity> calculateVMC(
			double xcg,
			Amount<Mass> maxTakeOffMass,
			double cLMaxTakeOff,
			MyInterpolatingFunction tauRudder
			) {
		
		Amount<Length> dimensionalXcg = 
				theAircraft.getWing().getMeanAerodynamicChord().to(SI.METER).times(xcg)
				.plus(theAircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
				.plus(theAircraft.getWing().getXApexConstructionAxes().to(SI.METER));
		
		String veDSCDatabaseFileName = "VeDSC_database.h5";
		
		VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
				new VeDSCDatabaseReader(
						MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), veDSCDatabaseFileName
						),
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR)
				);

		// GETTING THE FUSELAGE HEGHT AR V-TAIL MAC (c/4)
		List<Amount<Length>> vX = theAircraft.getFuselage().getOutlineXZUpperCurveAmountX();
		List<Amount<Length>> vZUpper = theAircraft.getFuselage().getOutlineXZUpperCurveAmountZ();
		List<Amount<Length>> vZLower = theAircraft.getFuselage().getOutlineXZLowerCurveAmountZ();
		
		List<Amount<Length>> sectionHeightsList = new ArrayList<>();
		List<Amount<Length>> xListInterpolation = new ArrayList<>();
		for(int i=vX.size()-5; i<vX.size(); i++) {
			sectionHeightsList.add(
					vZUpper.get(i).minus(vZLower.get(i))
					);
			xListInterpolation.add(vX.get(i));
		}
		
		Amount<Length> diameterAtVTailQuarteMAC = 
				Amount.valueOf( 
						MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(xListInterpolation),
								MyArrayUtils.convertListOfAmountTodoubleArray(sectionHeightsList),
								theAircraft.getVTail().getMeanAerodynamicChordLeadingEdgeX()
								.plus(theAircraft.getVTail().getXApexConstructionAxes())
								.plus(theAircraft.getVTail().getMeanAerodynamicChord().times(0.25))
								.doubleValue(SI.METER)
								),
						SI.METER
						);
		
		double tailConeTipToFuselageRadiusRatio = 
				theAircraft.getFuselage().getTailTipOffset()
				.divide(theAircraft.getFuselage().getSectionCylinderHeight().divide(2))
				.getEstimatedValue();
		
		veDSCDatabaseReader.runAnalysis(
				theAircraft.getWing().getAspectRatio(), 
				theAircraft.getWing().getPositionRelativeToAttachment(), 
				theAircraft.getVTail().getAspectRatio(), 
				theAircraft.getVTail().getSpan().doubleValue(SI.METER), 
				theAircraft.getHTail().getPositionRelativeToAttachment(),
				diameterAtVTailQuarteMAC.doubleValue(SI.METER), 
				tailConeTipToFuselageRadiusRatio
				);

		if(theAircraft.getTheAnalysisManager().getTheBalance() == null)
			theAircraft.calculateArms(theAircraft.getVTail(), dimensionalXcg);
		
		// cNb vertical [1/deg]
		double cNbVertical = MomentCalc.calcCNbetaVerticalTailVEDSC(
				theAircraft.getWing().getAspectRatio(), 
				theAircraft.getVTail().getAspectRatio(),
				theAircraft.getVTail().getLiftingSurfaceArm().doubleValue(SI.METER),
				theAircraft.getWing().getSpan().doubleValue(SI.METER),
				theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				theAircraft.getVTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
				theAircraft.getVTail().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
				theAircraft.getVTail().getAirfoilList().get(0)
					.getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
				theOperatingConditions.getMachTakeOff(), 
				veDSCDatabaseReader.getkFv(),
				veDSCDatabaseReader.getkWv(),
				veDSCDatabaseReader.getkHv()
				);
		
		//..................................................................................
		// CALCULATING THE THRUST YAWING MOMENT
		double[] speed = MyArrayUtils.linspace(
				SpeedCalc.calculateTAS(
						0.05,
						theOperatingConditions.getAltitudeTakeOff(),
						Amount.valueOf(10, SI.CELSIUS)
						).doubleValue(SI.METERS_PER_SECOND),
				SpeedCalc.calculateSpeedStall(
						theOperatingConditions.getAltitudeTakeOff(),
						Amount.valueOf(10, SI.CELSIUS),
						maxTakeOffMass.to(SI.KILOGRAM),
						theAircraft.getWing().getSurfacePlanform(),
						cLMaxTakeOff
						).times(1.2).doubleValue(SI.METERS_PER_SECOND),
				250
				);

		List<Amount<Force>> thrust = ThrustCalc.calculateThrustVsSpeed(
				EngineOperatingConditionEnum.APR,
				theAircraft.getPowerPlant(), 
				MyArrayUtils.convertDoubleArrayToListOfAmount(speed, SI.METERS_PER_SECOND),
				theOperatingConditions.getAltitudeTakeOff(), 
				Amount.valueOf(10, SI.CELSIUS), 
				theOperatingConditions.getThrottleTakeOff(), 
				true
				);

		List<Amount<Length>> enginesArms = new ArrayList<>();
		for(int i=0; i<theAircraft.getPowerPlant().getEngineList().size(); i++)
			enginesArms.add(theAircraft.getPowerPlant().getEngineList().get(i).getYApexConstructionAxes());
		
		Amount<Length> maxEngineArm = 
				Amount.valueOf(
						MyArrayUtils.getMax(
								MyArrayUtils.convertListOfAmountToDoubleArray(
										enginesArms
										)
								),
						SI.METER
						);
		
		double[] thrustMomentOEI = new double[thrust.size()];
		
		for(int i=0; i < thrust.size(); i++){
			thrustMomentOEI[i] = thrust.get(i).doubleValue(SI.NEWTON)*maxEngineArm.doubleValue(SI.METER);
		}

		//..................................................................................
		// CALCULATING THE VERTICAL TAIL YAWING MOMENT
		double[] yawingMomentOEI = new double[thrustMomentOEI.length];
		
		for(int i=0; i < thrust.size(); i++){
			yawingMomentOEI[i] = cNbVertical*
				tauRudder.value(
						theAircraft.getVTail().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)
						)*
				theAircraft.getVTail().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)*
				0.5*
				theOperatingConditions.getDensityTakeOff().getEstimatedValue()*
				Math.pow(speed[i],2)*
				theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*
				theAircraft.getWing().getSpan().doubleValue(SI.METER);
		}
		
		//..................................................................................
		// CALCULATING THE VMC
		Amount<Velocity> vMC = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		
		double[] curvesIntersection = MyArrayUtils.intersectArraysSimple(
				thrustMomentOEI,
				yawingMomentOEI
				);
		int indexOfVMC = 0;
		for(int i=0; i<curvesIntersection.length; i++)
			if(curvesIntersection[i] != 0.0) {
				indexOfVMC = i;
			}			

		if(indexOfVMC != 0)
			vMC = Amount.valueOf(
					speed[indexOfVMC],
					SI.METERS_PER_SECOND
					).to(NonSI.KNOT);
		else {
			System.err.println("WARNING: (VMC - TAKE-OFF) NO INTERSECTION FOUND ...");
			vMC = Amount.valueOf(
					0.0,
					SI.METERS_PER_SECOND
					).to(NonSI.KNOT);
		}

		return vMC;
		
	}
	
}
