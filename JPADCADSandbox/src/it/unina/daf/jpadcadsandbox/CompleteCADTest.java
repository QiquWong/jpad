package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcad.CADManager;
import javafx.application.Application;
import javafx.scene.DepthTest;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

class CADArguments {
	@Option(name = "-aci", aliases = { "--ac-input" }, required = true,
			usage = "aircraft input file")
	private File _aircraftInputFile;
	
	@Option(name = "-cci", aliases = { "--cc-input" }, required = true,
			usage = "CAD configuration input file")
	private File _cadConfigurationInputFile;
	
	@Option(name = "-db", aliases = { "--dir-database" }, required = true,
			usage = "database directory")
	private File _databaseDirectory;
	
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

	public File getAircraftInputFile() {
		return _aircraftInputFile;
	}
	
	public File getCADConfigurationInputFile() {
		return _cadConfigurationInputFile;
	}
	
	public File getDatabaseDirectory() {
		return _databaseDirectory;
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

public class CompleteCADTest extends Application {
	
	// Declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;
	public static CADManager theCADManager;

	//-------------------------------------------------------------
	
	// Console output management
	public final static PrintStream originalOut = System.out;
	public final static PrintStream filterStream = new PrintStream(new OutputStream() {
		public void write(int b) {} // write nothing
	});
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		theCADManager.generateScene();
		Scene scene = theCADManager.getTheFXScene();
		
		primaryStage.setTitle("AIRCRAFT FX");
		primaryStage.setScene(theCADManager.getTheFXScene());
		primaryStage.show();
		
		scene.setCamera(theCADManager.getTheCamera());
	};

	public static void main(String[] args) {
		
		//---------------------------------------------------------------
		// IMPORT DATA FROM XMLS
		//---------------------------------------------------------------
		importFromXML(args);
		
		//---------------------------------------------------------------
		// PRINT CAD CONFIGURATION OPTIONS TO CONSOLE
		//---------------------------------------------------------------	
		System.out.println(theCADManager.toString());
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT CAD
		//---------------------------------------------------------------
		System.setOut(filterStream);
		theCADManager.generateCAD();
		System.setOut(originalOut);
		
		System.out.println("\tAIRCRAFT CAD SUCCESFULLY GENERATED ... \n");
		
		//---------------------------------------------------------------
		// EXPORT THE AIRCRAFT CAD TO FILE
		//---------------------------------------------------------------
		if (theCADManager.getTheCADBuilderInterface().getExportToFile()) {
			
			System.out.println("\tEXPORTING THE AIRCRAFT CAD TO FILE ... \n");

			MyConfiguration.setDir(FoldersEnum.OUTPUT_DIR, MyConfiguration.outputDirectory);			
			String outputFolderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
			
			System.setOut(filterStream);
			theCADManager.exportCAD(outputFolderPath);
			System.setOut(originalOut);
			
			System.out.println("\n\n\tTHE AIRCRAFT CAD FILE (." + 
						theCADManager.getTheCADBuilderInterface().getFileExtension().toString() + 
						" FORMAT) HAS BEEN SUCCESFULLY CREATED\n");
			
			System.out.println("\tTHE FILE HAS BEEN GENERATED AT: " + outputFolderPath + "\n");
		}
		
		launch();
//		System.exit(0);
	}
	
	public static void importFromXML(String[] args) {
		
		CADArguments cadArguments = new CADArguments();
		theCmdLineParser = new CmdLineParser(cadArguments);
		
		try {
			theCmdLineParser.parseArgument(args);
			
			String pathToAircraftXML = cadArguments.getAircraftInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToAircraftXML);
			
			String pathToCADConfigXML = cadArguments.getCADConfigurationInputFile().getAbsolutePath();
			System.out.println("CAD CONFIGURATION INPUT ===> " + pathToCADConfigXML);
			
			String dirAirfoil = cadArguments.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = cadArguments.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);
			
			String dirLiftingSurfaces = cadArguments.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
			
			String dirEngines = cadArguments.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			String dirNacelles = cadArguments.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);
			
			String dirLandingGears = cadArguments.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirCabinConfiguration = cadArguments.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");
			
			// Setup database(s)
			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, cadArguments.getDatabaseDirectory().getAbsolutePath());
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			
			AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
					new AerodynamicDatabaseReader(databaseFolderPath, aerodynamicDatabaseFileName),
					databaseFolderPath);
			
			HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
					new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName),
					databaseFolderPath);
			
			FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
					new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename),
					databaseFolderPath);
			
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(databaseFolderPath,	vedscDatabaseFilename),
					databaseFolderPath);
			
			// Creating the aircraft from the XML
			System.setOut(filterStream);
  
			theAircraft = Aircraft.importFromXML(
					pathToAircraftXML,
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
			
			theCADManager = CADManager.importFromXML(
					pathToCADConfigXML, 
					theAircraft);
			
		} catch (Exception e) {			
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("Must launch this app with proper command line arguments!");
			System.exit(1);
		}
		
		System.setOut(originalOut);
	}
	
}
