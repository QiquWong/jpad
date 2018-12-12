package it.unina.daf.jpadcadsandbox.cfdworkflows;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcadsandbox.utils.CmdLineUtils;

public class CFDSimUtils {
	
//	public static Aircraft importAircraft(String[] args) {
//		
//		Aircraft aircraft = null;
//		//--------------------------------------------------------------------
//		// Redirect console output
//		final PrintStream originalOut = System.out;
//		PrintStream filterStream = new PrintStream(new OutputStream() {
//		    public void write(int b) {}
//		});
//		
//		CFDCmdLineUtils.va = new ArgumentsForCFDSim();
//		CmdLineUtils.theCmdLineParser = new CmdLineParser(CmdLineUtils.va);
//		
//		//--------------------------------------------------------------------
//		// Populate the aircraft object
//		try {
//			CFDCmdLineUtils.theCmdLineParser.parseArgument(args);
//			
//			String pathToXML = CFDCmdLineUtils.va.getInputFile().getAbsolutePath();
//			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);
//			
//			String dirAirfoil = CFDCmdLineUtils.va.getAirfoilDirectory().getCanonicalPath();
//			System.out.println("AIRFOILS ===> " + dirAirfoil);
//
//			String dirFuselages = CFDCmdLineUtils.va.getFuselagesDirectory().getCanonicalPath();
//			System.out.println("FUSELAGES ===> " + dirFuselages);
//			
//			String dirLiftingSurfaces = CFDCmdLineUtils.va.getLiftingSurfacesDirectory().getCanonicalPath();
//			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
//			
//			String dirEngines = CFDCmdLineUtils.va.getEnginesDirectory().getCanonicalPath();
//			System.out.println("ENGINES ===> " + dirEngines);
//			
//			String dirNacelles = CFDCmdLineUtils.va.getNacellesDirectory().getCanonicalPath();
//			System.out.println("NACELLES ===> " + dirNacelles);
//			
//			String dirLandingGears = CFDCmdLineUtils.va.getLandingGearsDirectory().getCanonicalPath();
//			System.out.println("LANDING GEARS ===> " + dirLandingGears);
//			
//			String dirCabinConfiguration = CFDCmdLineUtils.va.getCabinConfigurationDirectory().getCanonicalPath();
//			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
//			
//			System.out.println("--------------");
//			
//			//------------------------------------------------------------------------------------
//			// Setup database(s)
//			MyConfiguration.initWorkingDirectoryTree(
//					MyConfiguration.inputDirectory,
//					MyConfiguration.outputDirectory
//					);
//			
//			//------------------------------------------------------------------------------------
//			// Overriding default database directory path
//			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, CFDCmdLineUtils.va.getDatabaseDirectory().getAbsolutePath());
//			
//			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
//			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
//			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
//			String fusDesDatabaseFilename = "FusDes_database.h5";
//			String vedscDatabaseFilename = "VeDSC_database.h5";
//			
//			AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
//					new AerodynamicDatabaseReader(
//							databaseFolderPath,	aerodynamicDatabaseFileName
//							),
//					databaseFolderPath
//					);
//			HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
//					new HighLiftDatabaseReader(
//							databaseFolderPath,	highLiftDatabaseFileName
//							),
//					databaseFolderPath
//					);
//			FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
//					new FusDesDatabaseReader(
//							databaseFolderPath,	fusDesDatabaseFilename
//							),
//					databaseFolderPath
//					);
//			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
//					new VeDSCDatabaseReader(
//							databaseFolderPath,	vedscDatabaseFilename
//							),
//					databaseFolderPath
//					);
//			
//			//------------------------------------------------------------------------------------
//			// Aircraft creation
//			System.out.println("Creating the Aircraft ... ");
//
//			// deactivating system.out
//			System.setOut(filterStream);
//
//			Aircraft aircraft = Aircraft.importFromXML(
//					pathToXML,
//					dirLiftingSurfaces,
//					dirFuselages,
//					dirEngines,
//					dirNacelles,
//					dirLandingGears,
//					dirCabinConfiguration,
//					dirAirfoil,
//					aeroDatabaseReader,
//					highLiftDatabaseReader,
//					fusDesDatabaseReader,
//					veDSCDatabaseReader);
//			
//		} catch (CmdLineException | IOException e) {
//			System.err.println("Error: " + e.getMessage());
//			CFDCmdLineUtils.theCmdLineParser.printUsage(System.err);
//			System.err.println();
//			System.err.println("Must launch this app with proper command line arguments.");
//			return null;
//		}
//		return aircraft;
//	}

}
