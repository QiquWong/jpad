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
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;
import calculators.performance.LandingNoiseTrajectoryCalc;
import calculators.performance.TakeOffNoiseTrajectoryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.UnitFormatEnum;
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

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

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

			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader,
//					fusDesDatabaseReader,
//					veDSCDatabaseReader
//					)
//					.build();

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
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());

			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
			
			//======================================================================
			// TAKE-OFF NOISE TRAJECTORIES
			System.out.println("\n\n\tTAKE-OFF NOISE TRAJECTORIES ... \n\n");
			//======================================================================
			// INPUT DATA TO BE ASSIGNED FROM FILE
			boolean timeHistories = true;
			UnitFormatEnum unitFormat = UnitFormatEnum.IMPERIAL;
			boolean takeOffSimulation = true;
			boolean landingSimulation = true;
			
			//......................................................................
			Amount<Length> xEndSimulation = Amount.valueOf(8000, SI.METER);
			Amount<Length> cutbackAltitude = Amount.valueOf(984, NonSI.FOOT); //  also to be done at 1000ft and 2000ft
			int numberOfThrustSettingCutback = 3;
			Amount<Mass> maxTakeOffMass = Amount.valueOf(54500, SI.KILOGRAM);
			Double[] polarCLTakeOff = new Double[] {-1.024064237,-0.882750413,-0.741378289,-0.599943427,-0.458441424,-0.316867914,-0.175218573,-0.033489115,0.108324705,0.250227087,0.392222187,0.534314117,0.676640473,0.818668509,0.960722621,1.102937968,1.245474454,1.388346414,1.531385114,1.674589688,1.818061748,1.961717376,2.105583653,2.249660071,2.392645609,2.512647424,2.592665502,2.616980286};
			Double[] polarCDTakeOff = new Double[] {0.103605564,0.096027653,0.08961484,0.084368382,0.080289963,0.077837014,0.076557364,0.076448505,0.077512477,0.079787184,0.083642651,0.088665524,0.094790411,0.101527726,0.109174381,0.118449777,0.129601151,0.142544953,0.156942866,0.172739892,0.190092273,0.208722674,0.228618565,0.249874304,0.272157975,0.291786295,0.306250433,0.314765984};
			Double deltaCD0LandingGear = 0.015;
			Double deltaCD0OEI = 0.0050;
			Amount<Duration> dtRot = Amount.valueOf(2, SI.SECOND);
			Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
			Amount<Duration> dtLandingGearRetraction = Amount.valueOf(12, SI.SECOND);
			Amount<Duration> dtThrustCutback = Amount.valueOf(4, SI.SECOND);
			Double phi = 1.0;
			Double kcLMax = 0.85;
			Double kRot = 1.05;
			Double alphaDotInitial = 4.0; // (deg/s)
			Double kAlphaDot = 0.04; // (1/deg)
			Double cLmaxTO = 2.61;
			Double cLZeroTO = 1.19;
			Amount<?> cLalphaFlap = Amount.valueOf(0.1082, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction mu = new MyInterpolatingFunction();
			mu.interpolateLinear(
					new double[]{0, 10000},
					new double[]{0.025, 0.025}
					);
			
			Boolean createCSV = Boolean.TRUE;
			
			//======================================================================
			
			Amount<Length> wingToGroundDistance = 
					theAircraft.getFuselage().getHeightFromGround()
					.plus(theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(theAircraft.getWing().getZApexConstructionAxes()
							.plus(theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											theAircraft.getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			if(takeOffSimulation) {
				TakeOffNoiseTrajectoryCalc theTakeOffNoiseTrajectoryCalculator = new TakeOffNoiseTrajectoryCalc(
						xEndSimulation,
						cutbackAltitude,
						maxTakeOffMass,
						theAircraft.getPowerPlant(),
						polarCLTakeOff,
						polarCDTakeOff, 
						deltaCD0LandingGear,
						deltaCD0OEI,
						theAircraft.getWing().getAspectRatio(),
						theAircraft.getWing().getSurface(), 
						dtRot, 
						dtHold, 
						dtLandingGearRetraction,
						dtThrustCutback,
						phi,
						kcLMax,
						kRot, 
						alphaDotInitial,
						kAlphaDot,
						mu,
						wingToGroundDistance, 
						theAircraft.getWing().getRiggingAngle(), 
						cLmaxTO, 
						cLZeroTO, 
						cLalphaFlap,
						createCSV
						);

				theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(false, null, timeHistories);
				theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(true, null, timeHistories);

				double lowestPhiCutback = theTakeOffNoiseTrajectoryCalculator.getPhiCutback();
				double[] phiArray = MyArrayUtils.linspace(lowestPhiCutback*1.1, 0.9, numberOfThrustSettingCutback);

				Arrays.stream(phiArray).forEach(throttle -> theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(true, throttle, timeHistories));

				try {
					theTakeOffNoiseTrajectoryCalculator.createOutputCharts(outputFolderTakeOff, timeHistories, unitFormat);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			//======================================================================
			// LANDING NOISE TRAJECTORY
			System.out.println("\n\n\tLANDING NOISE TRAJECTORY ... \n\n");
			//======================================================================
			// INPUT DATA TO BE ASSIGNED FROM FILE
			Amount<Length> initialAltitude = Amount.valueOf(4000, NonSI.FOOT);
			Amount<Angle> gammaDescent = Amount.valueOf(-3, NonSI.DEGREE_ANGLE);
			Amount<Mass> maxLandingMass = Amount.valueOf(52865, SI.KILOGRAM);
			Double[] polarCLLanding = new Double[] {-0.031750302,0.100923075,0.233596452,0.366269828,0.498943205,0.631616582,0.764289959,0.896963335,1.029636712,1.162310089,1.294983465,1.427656842,1.560330219,1.693003595,1.825676972,1.958350349,2.091023726,2.223697102,2.356370479,2.489043856,2.621717232,2.754390609,2.887063986,3.017145378,3.114191869,3.176564974,3.213398273,3.233825349,3.246979785};
			Double[] polarCDLanding = new Double[] {0.135400883,0.133582443,0.132848483,0.133199003,0.134634042,0.137153575,0.140757223,0.145445151,0.151217648,0.158074035,0.166014619,0.175038985,0.185146827,0.196198286,0.207603396,0.219873902,0.233549812,0.248933639,0.265937067,0.284287369,0.303883008,0.324826902,0.346694324,0.369645716,0.392268833,0.409403904,0.419468091,0.423103066,0.420313086};
			Amount<Duration> dtFlare = Amount.valueOf(3, SI.SECOND);
			Amount<Duration> dtFreeRoll = Amount.valueOf(2, SI.SECOND);
			double cLmaxLND = 3.125;
			double cLZeroLND = 2.263;
			Amount<?> cLalphaLND = Amount.valueOf(0.1131, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction muBrake = new MyInterpolatingFunction();
			muBrake.interpolateLinear(
					new double[]{0,5.144,10.228,15.432,20.576,25.72,30.864,36.008,41.152},
					new double[]{0.94,0.89,0.85,0.8,0.74,0.68,0.62,0.57,0.52}
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
						theAircraft.getWing().getSurface(),
						dtFlare, 
						dtFreeRoll,
						mu, 
						muBrake,
						theOperatingConditions.getThrottleGroundIdleLanding(),
						wingToGroundDistance,
						theAircraft.getWing().getRiggingAngle(),
						cLmaxLND,
						cLZeroLND,
						cLalphaLND,
						createCSV
						);

				theLandingNoiseTrajectoryCalculator.calculateNoiseLandingTrajectory(timeHistories);

				try {
					theLandingNoiseTrajectoryCalculator.createOutputCharts(outputFolderLanding, timeHistories, unitFormat);
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

}
