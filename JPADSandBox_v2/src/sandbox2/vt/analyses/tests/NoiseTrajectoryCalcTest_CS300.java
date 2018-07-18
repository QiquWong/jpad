package sandbox2.vt.analyses.tests;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

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

public class NoiseTrajectoryCalcTest_CS300 extends Application {

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
			FileUtils.cleanDirectory(new File(outputFolderTakeOff));
			FileUtils.cleanDirectory(new File(outputFolderLanding));
			
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
			Amount<Mass> maxTakeOffMass = Amount.valueOf(65183, SI.KILOGRAM);
			Double[] polarCLTakeOff = new Double[] {-0.053138218,0.014516736,0.082153097,0.149771045,0.217370763,0.284952436,0.352516252,0.420062402,0.48759108,0.555102482,0.622596806,0.690074253,0.757535029,0.824979338,0.89240739,0.959819396,1.027215572,1.094596132,1.161961296,1.229311287};
			Double[] polarCDTakeOff = new Double[] {0.020646444,0.020729981,0.021112629,0.021794316,0.022774969,0.024054516,0.02563289,0.027510021,0.029685844,0.032160293,0.034933305,0.038004817,0.041374769,0.0450431,0.049009753,0.053274672,0.0578378,0.062699083,0.06785847,0.073315908};
			Double deltaCD0LandingGear = 0.015;
			Double deltaCD0OEI = 0.0050;
			Amount<Duration> dtRot = Amount.valueOf(2, SI.SECOND);
			Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
			Amount<Duration> dtLandingGearRetraction = Amount.valueOf(12, SI.SECOND);
			Amount<Duration> dtThrustCutback = Amount.valueOf(4, SI.SECOND);
			Double phi = 1.0;
			Double kcLMax = 0.85;
			Double kRot = 1.05;
			Double alphaDotInitial = 3.0; // (deg/s)
			Double kAlphaDot = 0.06; // (1/deg)
			Double cLmaxTO = 2.36;
			Double cLZeroTO = 1.2407;
			Amount<?> cLalphaFlap = Amount.valueOf(0.0868, NonSI.DEGREE_ANGLE.inverse());
			
			MyInterpolatingFunction mu = new MyInterpolatingFunction();
			mu.interpolateLinear(
					new double[]{0, 10000},
					new double[]{0.025, 0.025}
					);
			
			Boolean createCSV = Boolean.TRUE;
			
			//======================================================================
			
			Amount<Length> wingToGroundDistance = 
					theAircraft.getFuselage().getHeightFromGround()
					.plus(theAircraft.getFuselage().getSectionCylinderHeight().divide(2))
					.plus(theAircraft.getWing().getZApexConstructionAxes()
							.plus(theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											theAircraft.getWing()	
												
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
						theAircraft.getWing().getSurfacePlanform(), 
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
				double[] phiArray = MyArrayUtils.linspace( (lowestPhiCutback + 0.1), 0.9, numberOfThrustSettingCutback);

				Arrays.stream(phiArray).forEach(
						throttle -> theTakeOffNoiseTrajectoryCalculator.calculateNoiseTakeOffTrajectory(
								true,
								throttle, 
								timeHistories
								)
						);

				if(theTakeOffNoiseTrajectoryCalculator.isTargetSpeedFlag() == true)
					try {
						theTakeOffNoiseTrajectoryCalculator.createOutputCharts(outputFolderTakeOff, timeHistories, unitFormat);
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
			Amount<Mass> maxLandingMass = Amount.valueOf(58740, SI.KILOGRAM);
			Double[] polarCLLanding = new Double[] {1.477793265,1.567919335,1.657905336,1.74775273,1.83746302,1.927037748,2.016478496,2.105786885,2.194964573,2.284013261,2.372934685,2.462027739,2.550700002,2.639146707,2.715395409,2.780296468,2.833875056,2.876157804,2.907172657,2.926948714,2.935516057};
			Double[] polarCDLanding = new Double[] {0.098974994,0.098751866,0.099236527,0.10042851,0.10232738,0.104932744,0.10824424,0.112261543,0.116984358,0.122412424,0.12854551,0.135362785,0.142905345,0.151192962,0.158794304,0.165599319,0.171406943,0.176052624,0.179408394,0.18138296,0.181921793};
			Amount<Duration> dtFlare = Amount.valueOf(3, SI.SECOND);
			Amount<Duration> dtFreeRoll = Amount.valueOf(2, SI.SECOND);
			double cLmaxLND = 2.94;
			double cLZeroLND = 1.8291;
			Amount<?> cLalphaLND = Amount.valueOf(0.0869, NonSI.DEGREE_ANGLE.inverse());
			
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
