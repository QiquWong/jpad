package sandbox2.atsi.analyses.test0;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

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
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.aircraft.AircraftAndComponentsViewPlotUtils;
import writers.JPADStaticWriteUtils;

public class ATsiTestAircraft {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static Aircraft theAircraft;

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
		
		MyArgumentsAtsiTestAircraft va = new MyArgumentsAtsiTestAircraft();
		ATsiTestAircraft.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class-> start ...)
		try {
		// before launching the JavaFX application thread (launch -
			ATsiTestAircraft.theCmdLineParser.parseArgument(args);
			
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
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);
			FileUtils.cleanDirectory(new File(subfolderPath)); 

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
			
			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
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
			
			////////////////////////////////////////////////////////////////////////
			// Printing results (activating system.out)
			System.setOut(originalOut);
			System.out.println("\n\n\tPrinting results ... \n\n");
			System.out.println(theAircraft.getTheAnalysisManager().toString());
			System.out.println("\n\n\tDone!! \n\n");
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			
			System.setOut(filterStream);
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			ATsiTestAircraft.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			System.exit(1);
		}	
		
		System.exit(1);
	}

}
