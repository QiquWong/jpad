package sandbox2.vt.analyses;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Aircraft;
import analyses.ACBalanceManager;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

class MyArgumentsBalanceAnalysis {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

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
	
	@Option(name = "-ds", aliases = { "--dir-systems" }, required = true,
			usage = "systems directory path")
	private File _systemsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	@Option(name = "-dc", aliases = { "--dir-costs" }, required = true,
			usage = "costs directory path")
	private File _costsDirectory;
	
	@Option(name = "-ib", aliases = { "--input-balance" }, required = true,
			usage = "balance input path")
	private File _balanceInputFile;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
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

	public File getSystemsDirectory() {
		return _systemsDirectory;
	}
	
	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
	
	public File getCostsDirectory() {
		return _costsDirectory;
	}
	
	public File getBalanceInputFile() {
		return _balanceInputFile;
	}
}

public class BalanceTest extends Application {

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

		Aircraft aircraft = WeightsTest.theAircraft;
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

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("-------------------");
		System.out.println("Balance test");
		System.out.println("-------------------");
		
		MyArgumentsBalanceAnalysis va = new MyArgumentsBalanceAnalysis();
		BalanceTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			BalanceTest.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String pathToXMLBalance = va.getBalanceInputFile().getAbsolutePath();
			System.out.println("BALANCE INPUT ===> " + pathToXMLBalance);
			
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
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			String dirCosts = va.getCostsDirectory().getCanonicalPath();
			System.out.println("COSTS ===> " + dirCosts);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			
			// default Aircraft ATR-72 ...
			theAircraft = new Aircraft.AircraftBuilder(
					"ATR-72",
					AircraftEnum.ATR72,
					aeroDatabaseReader,
					highLiftDatabaseReader
					)
					.build();

			// reading aircraft from xml ... 
			// TODO : THE ANALYSIS UPON THE IMPORTED AIRCRAFT REQUIRES THAT ACAnalysisManager WORKS !!
//			theAircraft = Aircraft.importFromXML(
//					pathToXML,
//					dirLiftingSurfaces,
//					dirFuselages,
//					dirEngines,
//					dirNacelles,
//					dirLandingGears,
//					dirSystems,
//					dirCabinConfiguration,
//					dirAirfoil,
//					dirCosts,
//					aeroDatabaseReader,
//					highLiftDatabaseReader);
			
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder + "BALANCE" + File.separator);

			///////////////////////////////////////////////////////////////////////////////////
			// TODO : THE METHODS MAP WILL COME FROM ANALYSIS MANAGER
			// Choose methods to use for each component
			// All methods are used for weight estimation and for CG estimation
			List<MethodEnum> _methodsList = new ArrayList<MethodEnum>(); 
			Map <ComponentEnum, List<MethodEnum>> _methodsMap = 
					new HashMap<ComponentEnum, List<MethodEnum>>();
			
			_methodsList.clear();
			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.FUSELAGE, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.WING, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.VERTICAL_TAIL, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.POWER_PLANT, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.FUEL_TANK, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.NACELLE, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.LANDING_GEAR, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			_methodsList.add(MethodEnum.ALL);
			_methodsMap.put(ComponentEnum.SYSTEMS, _methodsList);
			_methodsList = new ArrayList<MethodEnum>();

			// Evaluate aircraft balance
			theAircraft.setTheBalance(ACBalanceManager.importFromXML(pathToXMLBalance, theAircraft));
			
			theAircraft.getTheBalance().calculateBalance(_methodsMap);
			
			System.out.println(BalanceTest.theAircraft.getTheBalance().toString());
			theAircraft.getTheBalance().toXLSFile(subfolderPath + "Balance");
			theAircraft.getTheBalance().createBalanceCharts(subfolderPath);
			///////////////////////////////////////////////////////////////////////////////////
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			BalanceTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ...
		launch(args);
	}

}