package sandbox2.cavas;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import writers.JPADStaticWriteUtils;

public class TestCompleteAircraft {
	
	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	public static void main(String[] args) throws CmdLineException, IOException, HDF5LibraryException {
		
		System.out.println("-------------------");
		System.out.println("CAVAS TEST");
		System.out.println("-------------------"); 
		
		ArgumentsCavasSandbox va = new ArgumentsCavasSandbox();
		TestCompleteAircraft.theCmdLineParser = new CmdLineParser(va);
		
		TestCompleteAircraft.theCmdLineParser.parseArgument(args);
		
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

		final PrintStream originalOut = System.out;
		
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
		// Analyzing the aircraft
		System.setOut(originalOut);
		System.out.println("\n\n\tRunning requested analyses ... \n\n");
		theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft, theOperatingConditions));
		theAircraft.getTheAnalysisManager().calculateDependentVariables();
		System.setOut(originalOut);
		theAircraft.getTheAnalysisManager().doAnalysis(theAircraft, theOperatingConditions, subfolderPath);
		System.setOut(originalOut);
		System.out.println("\n\n\tDone!! \n\n");
		
		
		////////////////////////////////////////////////////////////////////////
		System.out.println(" FINE TEST ");
	}


}
