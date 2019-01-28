package it.unina.daf.jpadcadsandbox.utils;

import java.io.File;
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

import it.unina.daf.jpadcadsandbox.CreateCasesGB;

import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public final class CaWiXUtils {
	
	public CmdLineParser theCmdLineParser;
	public ArgumentsCaWiX va;
	public JPADXmlReader reader;

	public void importData(String[] args, CreateCasesGB createCasesGB) {
		
		this.va = new ArgumentsCaWiX();
		this.theCmdLineParser = new CmdLineParser(this.va);
		
		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			System.out.println(args.length);
			this.theCmdLineParser.parseArgument(args);
			
			String pathToXML = this.va.getCaWiXFile().getAbsolutePath();
			System.out.println("CaWiX Run Config File ===> " + pathToXML);
			
			////////////////////////////////////////////////////////////////////////
			// Reading Config File
			System.out.println("Reading Config File ... ");
			
			JPADXmlReader reader = new JPADXmlReader(pathToXML);
			
			createCasesGB.setWorkingFolderPath(reader.getXMLAttributeByPath("//working_folder_path", "value"));
			createCasesGB.setJPADCADFolderPath(reader.getXMLAttributeByPath("//jpad_CAD_folder_path", "value"));
			createCasesGB.setStarTempFolderPath(reader.getXMLAttributeByPath("//star_temp_folder_path", "value"));
			createCasesGB.setMacroPath(reader.getXMLAttributeByPath("//macro_path", "value"));
			createCasesGB.setMacroName(reader.getXMLAttributeByPath("//macro_name", "value"));
			createCasesGB.setStarExePath(reader.getXMLAttributeByPath("//star_exe_path", "value"));
			createCasesGB.setStarOptions(reader.getXMLAttributeByPath("//star_options", "value"));
			
			createCasesGB.setAlphaList(reader.readArrayofAmountFromXML("//cases/alpha"));
			createCasesGB.setMachList(reader.readArrayDoubleFromXML("//cases/mach"));
			
			//createCasesGB.setComponentList(reader.readArrayFromXML("//component_to_be_modified"));
					
			createCasesGB.setRiggingAngleCanardList(reader.readArrayofAmountFromXML("//cases/rigging_canard_angle"));
			createCasesGB.setDihedralCanardList(reader.readArrayofAmountFromXML("//cases/dihedral_canard_angle"));
			createCasesGB.setSweepCanardList(reader.readArrayofAmountFromXML("//cases/sweep_canard_angle"));
			createCasesGB.setSpanCanardPcntVarList(reader.readArrayDoubleFromXML("//cases/span_canard_pct_var"));
			createCasesGB.setZPosCanardList(reader.readArrayofAmountFromXML("//cases/z_canard_pos"));
			createCasesGB.setXPosCanardPcntVarList(reader.readArrayDoubleFromXML("//cases/x_canard_pos_pct_var"));
			
//			createCasesGB.setRiggingAngleWingList(reader.readArrayofAmountFromXML("//cases/rigging_wing_angle"));
//			createCasesGB.setDihedralWingList(reader.readArrayofAmountFromXML("//cases/dihedral_wing_angle"));
//			createCasesGB.setSweepWingList(reader.readArrayofAmountFromXML("//cases/sweep_wing_angle"));
//			createCasesGB.setSpanWingPcntVarList(reader.readArrayDoubleFromXML("//cases/span_wing_pct_var"));
//			createCasesGB.setZPosWingList(reader.readArrayofAmountFromXML("//cases/z_wing_pos"));
//			createCasesGB.setXPosWingPcntVarList(reader.readArrayDoubleFromXML("//cases/x_wing_pos_pct_var"));
			
			
			System.out.println(args.length);
			createCasesGB.setAircraft(importAircraft(this.va));
			
		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
		}			
	}

	public Aircraft importAircraft(ArgumentsCaWiX va) {
		
		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
				
		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

//			String pathToAnalysesXML = va.getInputFileAnalyses().getAbsolutePath();
//			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);
//			
//			String pathToOperatingConditionsXML = va.getOperatingConditionsInputFile().getAbsolutePath();
//			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);
			
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
//					MyConfiguration.databaseDirectory,  // Used only for default location.
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);
			
			// Overriding default database directory path
			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, va.getDatabaseDirectory().getAbsolutePath());
			
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
//			System.setOut(originalOut);			
//			System.out.println(aircraft.toString());
//			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + aircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);
	
			
			System.setOut(originalOut);
			return aircraft;
			
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			filterStream.close();
			return null;
		}			
	}


	public CmdLineParser getTheCmdLineParser() {
		return theCmdLineParser;
	}


	public void setTheCmdLineParser(CmdLineParser theCmdLineParser) {
		this.theCmdLineParser = theCmdLineParser;
	}


	public ArgumentsCaWiX getVa() {
		return va;
	}


	public void setVa(ArgumentsCaWiX va) {
		this.va = va;
	}


	public JPADXmlReader getReader() {
		return reader;
	}


	public void setReader(JPADXmlReader reader) {
		this.reader = reader;
	}



}
