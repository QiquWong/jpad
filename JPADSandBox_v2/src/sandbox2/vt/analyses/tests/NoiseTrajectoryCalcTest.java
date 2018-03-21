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

import aircraft.Aircraft;
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
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
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
			UnitFormatEnum unitFormat = UnitFormatEnum.SI;
			boolean takeOffSimulation = true;
			boolean landingSimulation = true;
			
			//......................................................................
			Amount<Length> xEndSimulation = Amount.valueOf(8000, SI.METER);
			Amount<Length> cutbackAltitude = Amount.valueOf(984, NonSI.FOOT); //  also to be done at 1000ft and 2000ft
			int numberOfThrustSettingCutback = 3;
			Amount<Mass> maxTakeOffMass = Amount.valueOf(55032, SI.KILOGRAM);
			Double[] polarCLTakeOff = new Double[] {0.57825266,0.696589758,0.814590688,0.932780557,1.051150342,1.169691,1.288393466,1.407248652,1.526247451,1.645380738,1.764639367,1.884132045,2.003613843,2.123075559,2.242743719,2.362491216,2.482308794,2.602187184,2.722117101,2.842089242,2.851016568};
			Double[] polarCDTakeOff = new Double[] {0.120319713,0.119352237,0.118990827,0.119228811,0.120067629,0.121508644,0.123553144,0.126202339,0.129457353,0.133319228,0.137788921,0.142868765,0.148556915,0.154853121,0.161761843,0.169281799,0.177413389,0.186156914,0.195512577,0.205480483,0.204259518};
			Double deltaCD0LandingGear = 0.015;
			Double deltaCD0OEI = 0.0050;
			Amount<Duration> dtRot = Amount.valueOf(2, SI.SECOND);
			Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
			Amount<Duration> dtLandingGearRetraction = Amount.valueOf(12, SI.SECOND);
			Amount<Duration> dtThrustCutback = Amount.valueOf(4, SI.SECOND);
			Double phi = 1.0;
			Double kcLMax = 0.88;
			Double kRot = 1.05;
			Double alphaDotInitial = 3.0; // (deg/s)
			Double kAlphaDot = 0.04; // (1/deg)
			Double cLmaxTO = 2.92;
			Double cLZeroTO = 1.333;
			Amount<?> cLalphaFlap = Amount.valueOf(0.1303, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction mu = new MyInterpolatingFunction();
			mu.interpolateLinear(
					new double[]{0, 10000},
					new double[]{0.025, 0.025}
					);
			
			Boolean createCSV = Boolean.TRUE;
			
			//======================================================================
			
			Amount<Length> wingToGroundDistance = 
					theAircraft.getFuselage().getFuselageCreator().getHeightFromGround()
					.plus(theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().divide(2))
					.plus(theAircraft.getWing().getZApexConstructionAxes()
							.plus(theAircraft.getWing().getLiftingSurfaceCreator().getSemiSpan()
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
						theAircraft.getWing().getLiftingSurfaceCreator().getAspectRatio(),
						theAircraft.getWing().getLiftingSurfaceCreator().getSurfacePlanform(), 
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

				Arrays.stream(phiArray).forEach(
						throttle -> theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(
								true,
								throttle, 
								timeHistories
								)
						);

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
			Amount<Mass> maxLandingMass = Amount.valueOf(53381, SI.KILOGRAM);
			Double[] polarCLLanding = new Double[] {1.094267779,1.258884296,1.376716801,1.494747857,1.612968439,1.731369507,1.849941995,1.968676819,2.087564876,2.206597043,2.325764178,2.445174996,2.564584571,2.68398371,2.803598946,2.923303179,3.043087164,3.162941642,3.282857337,3.40282496,3.411757482};
			Double[] polarCDLanding = new Double[] {0.12629755,0.121981303,0.121518064,0.121653573,0.122389314,0.123726701,0.125667072,0.128211686,0.131361726,0.135118289,0.139482389,0.144456549,0.150038726,0.156228739,0.163031363,0.170445256,0.178470883,0.187108615,0.196358721,0.206221376,0.204882932};
			Amount<Duration> dtFlare = Amount.valueOf(3, SI.SECOND);
			Amount<Duration> dtFreeRoll = Amount.valueOf(2, SI.SECOND);
			double cLmaxLND = 3.52;
			double cLZeroLND = 1.89;
			Amount<?> cLalphaLND = Amount.valueOf(0.1306, NonSI.DEGREE_ANGLE.inverse());
			
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
						theAircraft.getWing().getLiftingSurfaceCreator().getAspectRatio(), 
						theAircraft.getWing().getLiftingSurfaceCreator().getSurfacePlanform(),
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
