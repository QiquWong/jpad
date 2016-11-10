package sandbox2.vt.analyses.tests;

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
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsCalculator;
import analyses.liftingsurface.LSAerodynamicsCalculator.CalcCLAlpha;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentsHighLiftTest {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

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
	
	@Option(name = "-ds", aliases = { "--dir-systems" }, required = true,
			usage = "systems directory path")
	private File _systemsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
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

	public File getSystemsDirectory() {
		return _systemsDirectory;
	}
	
	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}
	
}

public class HighLiftDevicesEffectsTest extends Application {

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

		Aircraft aircraft = HighLiftDevicesEffectsTest.theAircraft;
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
		System.out.println("Complete Analysis Test");
		System.out.println("-------------------");

		MyArgumentsHighLiftTest va = new MyArgumentsHighLiftTest();
		HighLiftDevicesEffectsTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			HighLiftDevicesEffectsTest.theCmdLineParser.parseArgument(args);
			
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
			
			String dirSystems = va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
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
//					aeroDatabaseReader,
//					highLiftDatabaseReader);
			
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			
			// Defining the operating conditions ...
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);

			// Defining the LSAerodynamicsCalculator object
			Map <String, List<MethodEnum>> taskMap = new HashMap<>();
			Map <String, List<MethodEnum>> plotMap = new HashMap<>();
			LSAerodynamicsCalculator theAerodynamicCalculator = new LSAerodynamicsCalculator(
					theAircraft.getWing(),
					theOperatingConditions,
					taskMap,
					plotMap
					);
			CalcCLAlpha theCLAlphaCalculator = theAerodynamicCalculator.new CalcCLAlpha();
			theCLAlphaCalculator.nasaBlackwell();
			
			theAircraft.getWing().setTheAerodynamicsCalculator(theAerodynamicCalculator);

//			// TAKE-OFF
//			LiftCalc.calculateHighLiftDevicesEffects(
//					theAircraft.getWing(),
//					theOperatingConditions.getFlapDeflectionTakeOff(),
//					theOperatingConditions.getSlatDeflectionTakeOff(),
//					theAerodynamicCalculator.getCurrentLiftCoefficient()
//					);
			
			// LANDING
			LiftCalc.calculateHighLiftDevicesEffects(
					theAircraft.getWing(),
					theOperatingConditions.getFlapDeflectionLanding(),
					theOperatingConditions.getSlatDeflectionLanding(),
					theAerodynamicCalculator.getCurrentLiftCoefficient()
					);
			
			//----------------------------------------------------------------------------------
			// Results print
			System.out.println("\ndeltaCl0_flap_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaCl0FlapList().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaCl0FlapList().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaCl0_flap = \n" + theAerodynamicCalculator.getDeltaCl0Flap().get(MethodEnum.EMPIRICAL));

			System.out.println("\ndeltaCL0_flap_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaCL0FlapList().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaCL0FlapList().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaCL0_flap = \n" + theAerodynamicCalculator.getDeltaCL0Flap().get(MethodEnum.EMPIRICAL));
			
			System.out.println("\ndeltaClmax_flap_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaClmaxFlapList().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaClmaxFlapList().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaClmax_flap = \n" + theAerodynamicCalculator.getDeltaClmaxFlap().get(MethodEnum.EMPIRICAL));
			
			System.out.println("\ndeltaCLmax_flap_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaCLmaxFlapList().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaCLmaxFlapList().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaCLmax_flap = \n" + theAerodynamicCalculator.getDeltaCLmaxFlap().get(MethodEnum.EMPIRICAL));

			if(!theAircraft.getWing().getLiftingSurfaceCreator().getSlats().isEmpty()) {
				System.out.println("\ndeltaClmax_slat_list = ");
				for(int i=0; i<theAerodynamicCalculator.getDeltaClmaxSlatList().get(MethodEnum.EMPIRICAL).size(); i++)
					System.out.print(theAerodynamicCalculator.getDeltaClmaxSlatList().get(MethodEnum.EMPIRICAL).get(i) + " ");

				System.out.println("\n\ndeltaClmax_slat = \n" + theAerodynamicCalculator.getDeltaClmaxSlat().get(MethodEnum.EMPIRICAL));

				System.out.println("\ndeltaCLmax_slat_list = ");
				for(int i=0; i<theAerodynamicCalculator.getDeltaCLmaxSlatList().get(MethodEnum.EMPIRICAL).size(); i++)
					System.out.print(theAerodynamicCalculator.getDeltaCLmaxSlatList().get(MethodEnum.EMPIRICAL).get(i) + " ");

				System.out.println("\n\ndeltaCLmax_slat = \n" + theAerodynamicCalculator.getDeltaCLmaxSlat().get(MethodEnum.EMPIRICAL));
			}
			
			System.out.println("\ncLalpha_flap = \n" + theAerodynamicCalculator.getCLAlphaHighLift().get(MethodEnum.EMPIRICAL));

			System.out.println("\ndeltaCD_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaCDList().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaCDList().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaCD = \n" + theAerodynamicCalculator.getDeltaCD().get(MethodEnum.EMPIRICAL));
			
			System.out.println("\ndeltaCM_c4_list = ");
			for(int i=0; i<theAerodynamicCalculator.getDeltaCMc4List().get(MethodEnum.EMPIRICAL).size(); i++)
				System.out.print(theAerodynamicCalculator.getDeltaCMc4List().get(MethodEnum.EMPIRICAL).get(i) + " ");

			System.out.println("\n\ndeltaCM_c4 = \n" + theAerodynamicCalculator.getDeltaCMc4().get(MethodEnum.EMPIRICAL));
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			HighLiftDevicesEffectsTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ... (if needed)
		launch(args);
	}

}

