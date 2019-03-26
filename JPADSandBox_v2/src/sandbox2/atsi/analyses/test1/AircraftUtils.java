package sandbox2.atsi.analyses.test1;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import calculators.performance.AircraftPointMassPropagator;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.aircraft.AircraftAndComponentsViewPlotUtils;
import writers.JPADStaticWriteUtils;

public final class AircraftUtils {

	public static String pathToXML;
	public static String pathToOperatingConditionsXML;
	public static String pathToAnalysesXML;

	public static Aircraft importAircraft(String[] args) {

		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});

		CmdLineUtils.va = new MyArgumentsForTesting();
		CmdLineUtils.theCmdLineParser = new CmdLineParser(CmdLineUtils.va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			CmdLineUtils.theCmdLineParser.parseArgument(args);

			pathToXML = CmdLineUtils.va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			pathToAnalysesXML = CmdLineUtils.va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);

			String dirAirfoil = CmdLineUtils.va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = CmdLineUtils.va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);

			String dirLiftingSurfaces = CmdLineUtils.va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);

			String dirEngines = CmdLineUtils.va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);

			String dirNacelles = CmdLineUtils.va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);

			String dirLandingGears = CmdLineUtils.va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);

			String dirCabinConfiguration = CmdLineUtils.va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);

			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree(
					//					MyConfiguration.databaseDirectory,  // Used only for default location.
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);

			// Overriding default database directory path
			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, CmdLineUtils.va.getDatabaseDirectory().getAbsolutePath());

			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";

			AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
					new AerodynamicDatabaseReader(

							databaseFolderPath,	aerodynamicDatabaseFileName
							),
					databaseFolderPath
					);
			HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
					new HighLiftDatabaseReader(
							databaseFolderPath,	highLiftDatabaseFileName
							),
					databaseFolderPath
					);
			FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
					new FusDesDatabaseReader(
							databaseFolderPath,	fusDesDatabaseFilename
							),
					databaseFolderPath
					);
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(
							databaseFolderPath,	vedscDatabaseFilename
							),
					databaseFolderPath
					);

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("Creating the Aircraft ... ");

			// deactivating system.out
			System.setOut(filterStream);

			// reading aircraft from xml ... 
			Aircraft aircraft = Aircraft.importFromXML(
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
			System.out.println(aircraft.toString());
			System.setOut(filterStream);

			System.setOut(originalOut);
			return aircraft;

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			CmdLineUtils.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return null;
		}			
	}

	@SuppressWarnings("resource")
	public static OperatingConditions importOperatingCondition(String[] args) {

		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});

		CmdLineUtils.va = new MyArgumentsForTesting();
		CmdLineUtils.theCmdLineParser = new CmdLineParser(CmdLineUtils.va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			CmdLineUtils.theCmdLineParser.parseArgument(args);

			String pathToOperatingConditionsXML = CmdLineUtils.va.getOperatingConditionsInputFile().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);

			System.out.println("--------------");

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("Defining the operating conditions ... ");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
			System.setOut(filterStream);			

			System.setOut(originalOut);
			return theOperatingConditions;

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			CmdLineUtils.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return null;
		}			
	}

	public static void createViews(Aircraft aircraft, String aircraftFolder) {
		String subfolderViewPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder + "VIEWS" + File.separator);
		String subfolderViewComponentsPath = JPADStaticWriteUtils.createNewFolder(subfolderViewPath + "COMPONENTS");
		if(aircraft != null) {
			AircraftAndComponentsViewPlotUtils.createAircraftTopView(aircraft, subfolderViewPath);
			AircraftAndComponentsViewPlotUtils.createAircraftSideView(aircraft, subfolderViewPath);
			AircraftAndComponentsViewPlotUtils.createAircraftFrontView(aircraft, subfolderViewPath);
		}
		if(aircraft.getFuselage() != null) {
			AircraftAndComponentsViewPlotUtils.createFuselageTopView(aircraft, subfolderViewComponentsPath);
			AircraftAndComponentsViewPlotUtils.createFuselageSideView(aircraft, subfolderViewComponentsPath);
			AircraftAndComponentsViewPlotUtils.createFuselageFrontView(aircraft, subfolderViewComponentsPath);
		}
		if(aircraft.getWing() != null) {
			AircraftAndComponentsViewPlotUtils.createWingPlanformView(aircraft, subfolderViewComponentsPath);
			AircraftAndComponentsViewPlotUtils.createEquivalentWingView(aircraft, subfolderViewComponentsPath);
		}
		if(aircraft.getHTail() != null)
			AircraftAndComponentsViewPlotUtils.createHTailPlanformView(aircraft, subfolderViewComponentsPath);
		if(aircraft.getVTail() != null)
			AircraftAndComponentsViewPlotUtils.createVTailPlanformView(aircraft, subfolderViewComponentsPath);
		if(aircraft.getCanard() != null)
			AircraftAndComponentsViewPlotUtils.createCanardPlanformView(aircraft, subfolderViewComponentsPath);
		if(aircraft.getNacelles() != null) {
			AircraftAndComponentsViewPlotUtils.createNacelleTopView(aircraft, subfolderViewComponentsPath);
			AircraftAndComponentsViewPlotUtils.createNacelleSideView(aircraft, subfolderViewComponentsPath);
			AircraftAndComponentsViewPlotUtils.createNacelleFrontView(aircraft, subfolderViewComponentsPath);
		}
	}

	public static void performAnalyses(Aircraft theAircraft, OperatingConditions theOperatingConditions, String pathToAnalysesXML, String subfolderPath) {

		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});

		try {
			// Analyzing the aircraft
			System.setOut(originalOut);
			System.out.println("\n\n\tRunning requested analyses ... \n\n");
			System.setOut(filterStream);
			theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft, theOperatingConditions));
			System.setOut(originalOut);
			theAircraft.getTheAnalysisManager().calculateDependentVariables();
			theAircraft.getTheAnalysisManager().doAnalysis(theAircraft, theOperatingConditions, subfolderPath);
			System.setOut(originalOut);
			System.out.println("\n\n\tDone!! \n\n");
			System.setOut(filterStream);
		} catch (IOException | HDF5LibraryException e) {
			System.err.println("Error: " + e.getMessage());
			System.err.println();
			System.err.println("  Check analysis files and directives.");
			return;
		}	

	}

	public static AircraftPointMassPropagator importMissionScriptFromXML (String pathToXML, Aircraft theAircraft) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		String missionScriptFileName = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//point_mass_simulation/@file");

		File missionScriptFile = new File(
				MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
				+ File.separator 
				+ "Template_Analyses"
				+ File.separator
				+ missionScriptFileName
				);


		AircraftPointMassPropagator aircraftPointMassPropagator = new AircraftPointMassPropagator(theAircraft);
		if(missionScriptFile.exists())
			aircraftPointMassPropagator.readMissionScript(missionScriptFile.getAbsolutePath());
		else
			return null;

		return aircraftPointMassPropagator;

	}

}
