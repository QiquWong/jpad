package sandbox2.vt.analyses;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.aircraft.AircraftAndComponentsViewPlotUtils;
import writers.JPADStaticWriteUtils;

class MyArgumentsAnalysis {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-ia", aliases = { "--input-analyses" }, required = true,
			usage = "analyses input file")
	private File _inputFileAnalyses;
	
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

	public File getInputFileAnalyses() {
		return _inputFileAnalyses;
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

public class CompleteAnalysisTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

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
	 * @throws HDF5LibraryException 
	 */
	public static void main(String[] args) throws InvalidFormatException, HDF5LibraryException {

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
		System.out.println("Complete Analysis Test");
		System.out.println("-------------------");
		
		MyArgumentsAnalysis va = new MyArgumentsAnalysis();
		CompleteAnalysisTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class-> start ...)
		try {
		// before launching the JavaFX application thread (launch -
			CompleteAnalysisTest.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			String pathToAnalysesXML = va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);
			
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
			long databaseAndFoldersStartTime = System.currentTimeMillis();
			
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.databaseDirectory,
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);
			
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
			
			long databaseAndFoldersEndTime = System.currentTimeMillis();
			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("\n\n\tCreating the Aircraft ... \n\n");
			
			// deactivating system.out
			System.setOut(filterStream);
			
			long aircraftStartTime = System.currentTimeMillis();
			
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
			
			long aircraftEndTime = System.currentTimeMillis();
			
			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			long folderCleaningStartTime = System.currentTimeMillis();
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);
			FileUtils.cleanDirectory(new File(subfolderPath)); 
			long folderCleaningEndTime = System.currentTimeMillis();
			
			long aircraftViewsStartTime = System.currentTimeMillis();
			String subfolderViewPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder + "VIEWS" + File.separator);
			String subfolderViewComponentsPath = JPADStaticWriteUtils.createNewFolder(subfolderViewPath + "COMPONENTS");
			if(theAircraft != null) {
				AircraftAndComponentsViewPlotUtils.createAircraftTopView(theAircraft, subfolderViewPath);
				AircraftAndComponentsViewPlotUtils.createAircraftSideView(theAircraft, subfolderViewPath);
				AircraftAndComponentsViewPlotUtils.createAircraftFrontView(theAircraft, subfolderViewPath);
			}
			if(theAircraft.getFuselage() != null) {
				AircraftAndComponentsViewPlotUtils.createFuselageTopView(theAircraft, subfolderViewComponentsPath);
				AircraftAndComponentsViewPlotUtils.createFuselageSideView(theAircraft, subfolderViewComponentsPath);
				AircraftAndComponentsViewPlotUtils.createFuselageFrontView(theAircraft, subfolderViewComponentsPath);
			}
			if(theAircraft.getWing() != null) {
				AircraftAndComponentsViewPlotUtils.createWingPlanformView(theAircraft, subfolderViewComponentsPath);
				AircraftAndComponentsViewPlotUtils.createEquivalentWingView(theAircraft, subfolderViewComponentsPath);
			}
			if(theAircraft.getHTail() != null)
				AircraftAndComponentsViewPlotUtils.createHTailPlanformView(theAircraft, subfolderViewComponentsPath);
			if(theAircraft.getVTail() != null)
				AircraftAndComponentsViewPlotUtils.createVTailPlanformView(theAircraft, subfolderViewComponentsPath);
			if(theAircraft.getCanard() != null)
				AircraftAndComponentsViewPlotUtils.createCanardPlanformView(theAircraft, subfolderViewComponentsPath);
			if(theAircraft.getNacelles() != null) {
				AircraftAndComponentsViewPlotUtils.createNacelleTopView(theAircraft, subfolderViewComponentsPath);
				AircraftAndComponentsViewPlotUtils.createNacelleSideView(theAircraft, subfolderViewComponentsPath);
				AircraftAndComponentsViewPlotUtils.createNacelleFrontView(theAircraft, subfolderViewComponentsPath);
			}
			long aircraftViewsEndTime = System.currentTimeMillis();
			
			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			long operatingConditionsStartTime = System.currentTimeMillis();
			
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
			System.setOut(filterStream);
			
			long operatingConditionsEndTime = System.currentTimeMillis();
			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
			long analysisStartTime = System.currentTimeMillis();
			
			System.setOut(originalOut);
			System.out.println("\n\n\tRunning requested analyses ... \n\n");
			System.setOut(filterStream);
			theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft, theOperatingConditions));
			theAircraft.getTheAnalysisManager().calculateDependentVariables();
			System.setOut(originalOut);
			theAircraft.getTheAnalysisManager().doAnalysis(theAircraft, theOperatingConditions, subfolderPath);
			System.setOut(originalOut);
			System.out.println("\n\n\tDone!! \n\n");
			System.setOut(filterStream);
			
			long analysisEndTime = System.currentTimeMillis();
			////////////////////////////////////////////////////////////////////////
			// Printing results (activating system.out)
			System.setOut(originalOut);
			System.out.println("\n\n\tPrinting results ... \n\n");
			System.out.println(theAircraft.getTheAnalysisManager().toString());
			System.out.println("\n\n\tDone!! \n\n");
			
			long databaseAndFoldersEstimatedTime = databaseAndFoldersEndTime - databaseAndFoldersStartTime;
			long aircraftEstimatedTime = aircraftEndTime - aircraftStartTime;
			long foldersCleaningEstimatedTime = folderCleaningEndTime - folderCleaningStartTime;
			long aircraftViewsEstimatedTime = aircraftViewsEndTime - aircraftViewsStartTime;
			long operatingConditionsEstimatedTime = operatingConditionsEndTime - operatingConditionsStartTime;
			long analysisEstimatedTime = analysisEndTime - analysisStartTime;
			long estimatedTime = System.currentTimeMillis() - startTime;
			
			System.out.println("\n\t TIME ESTIMATED FOR DATABASE AND FOLDERS CREATION = " + TimeUnit.MILLISECONDS.toSeconds(databaseAndFoldersEstimatedTime) + " seconds");
			System.out.println("\n\t TIME ESTIMATED FOR AIRCRAFT CREATION = " + TimeUnit.MILLISECONDS.toSeconds(aircraftEstimatedTime) + " seconds");
			System.out.println("\n\t TIME ESTIMATED FOR FOLDERS CLEANING = " + TimeUnit.MILLISECONDS.toSeconds(foldersCleaningEstimatedTime) + " seconds");
			System.out.println("\n\t TIME ESTIMATED FOR AIRCRAFT VIEWS CREATION = " + TimeUnit.MILLISECONDS.toSeconds(aircraftViewsEstimatedTime) + " seconds");
			System.out.println("\n\t TIME ESTIMATED FOR OPERATING CONDITIONS = " + TimeUnit.MILLISECONDS.toSeconds(operatingConditionsEstimatedTime) + " seconds");
			System.out.println("\n\t TIME ESTIMATED FOR ANALYSIS = " + TimeUnit.MILLISECONDS.toSeconds(analysisEstimatedTime) + " seconds");
			System.out.println("\n\t TOTAL TIME ESTIMATED = " + TimeUnit.MILLISECONDS.toSeconds(estimatedTime) + " seconds");
			
			System.setOut(filterStream);
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			CompleteAnalysisTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			System.exit(1);
		}	
		
		System.exit(1);
	}
}
