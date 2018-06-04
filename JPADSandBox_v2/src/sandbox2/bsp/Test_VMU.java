package sandbox2.bsp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
import sandbox2.bsp.MyArgumentsAnalysis;

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

public class Test_VMU {
	
	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	public static void main(String[] args) throws CmdLineException, IOException {
		
		System.out.println("-------------------");
		System.out.println("TEST VMU");
		System.out.println("-------------------"); 
		
		MyArgumentsAnalysis va = new MyArgumentsAnalysis();
		Test_VMU.theCmdLineParser = new CmdLineParser(va);
		
		Test_VMU.theCmdLineParser.parseArgument(args);
		
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
		System.out.println(theAircraft.toString());

		
		////////////////////////////////////////////////////////////////////////
		// Set the folders tree
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
		String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

		////////////////////////////////////////////////////////////////////////
		// Defining the operating conditions ...
		System.out.println("\n\n\tDefining the operating conditions ... \n\n");
		OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
		System.out.println(theOperatingConditions.toString());
		
		////////////////////////////////////////////////////////////////////////
		System.out.println(" FINE TEST VMU ");
	}


}
