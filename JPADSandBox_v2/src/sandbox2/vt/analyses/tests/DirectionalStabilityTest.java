package sandbox2.vt.analyses.tests;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.unit.NonSI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Aircraft;
import analyses.ACAerodynamicCalculator;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import analyses.ACAerodynamicCalculator.CalcDirectionalStability;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import sandbox2.vt.analyses.CompleteAnalysisTest;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import writers.JPADStaticWriteUtils;

class MyArgumentsDirectionalStability {
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

public class DirectionalStabilityTest extends Application {

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
	 */
	public static void main(String[] args) throws InvalidFormatException {

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
		System.out.println("Directional Stability Test");
		System.out.println("-------------------");

		MyArgumentsDirectionalStability va = new MyArgumentsDirectionalStability();
		CompleteAnalysisTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			CompleteAnalysisTest.theCmdLineParser.parseArgument(args);

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
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
			VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("\n\n\tCreating the Aircraft ... \n\n");

			// deactivating system.out
			System.setOut(filterStream);

			// default Aircraft ATR-72 ...
			//				theAircraft = new Aircraft.AircraftBuilder(
			//						"ATR-72",
			//						AircraftEnum.ATR72,
			//						aeroDatabaseReader,
			//						highLiftDatabaseReader,
			//				        fusDesDatabaseReader,
			//						veDSCDatabaseReader,
			//						)
			//						.build();

			// reading aircraft from xml ... 
			theAircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirSystems,
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
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

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
			System.setOut(filterStream);
			
			ACAerodynamicCalculator theAerodynamicCalculator = new ACAerodynamicCalculator();
			theAerodynamicCalculator.setTheAircraft(theAircraft);
			theAerodynamicCalculator.setBetaList(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(0.0, 25, 50),
							NonSI.DEGREE_ANGLE
							)
					);
			theAerodynamicCalculator.setDeltaRudderList(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(0.0, 25, 50),
							NonSI.DEGREE_ANGLE
							)
					);
			theAerodynamicCalculator.setXCGAircraft(
					MyArrayUtils.convertDoubleArrayToListDouble(
							new Double[] {0.25}
							)
					);
			
			// Defining VTail analysis of the Xac ...
			Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> componentTaskList = new HashMap<>();
			Map<AerodynamicAndStabilityEnum, MethodEnum> vTailAnalysisList = new HashMap<>();
			vTailAnalysisList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.QUARTER);
			vTailAnalysisList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
			componentTaskList.put(ComponentEnum.VERTICAL_TAIL, vTailAnalysisList);
			
			Map<ComponentEnum, LSAerodynamicsManager> liftingSurfaceAerodynamicManagers = new HashMap<>();
			liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.VERTICAL_TAIL, 
					new LSAerodynamicsManager(
							theAircraft.getVTail(),
							theOperatingConditions,
							componentTaskList,
							null,
							ConditionEnum.TAKE_OFF,
							50, 
							null, 
							null, 
							null)
					);
			CalcXAC calcXAC = liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcXAC();
			calcXAC.atQuarterMAC();
			theAerodynamicCalculator.setLiftingSurfaceAerodynamicManagers(liftingSurfaceAerodynamicManagers);
			theAerodynamicCalculator.setComponentTaskList(componentTaskList);
			
			CalcDirectionalStability calcDirectionalStability = theAerodynamicCalculator.new CalcDirectionalStability();
			calcDirectionalStability.vedscSimplifiedWing(theOperatingConditions.getMachTakeOff());
			
			System.setOut(originalOut);
			
			////////////////////////////////////////////////////////////////////////
			// Printing results (activating system.out)
			System.setOut(originalOut);
			System.out.println("\n\n\tPrinting results ... \n\n");
			System.out.println("\tStability Derivatives ... \n\n");
			System.out.println("\t\tCNb Fuselgae @Xcg/c " + theAerodynamicCalculator.getCNbFuselage().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNbFuselage().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCNb Wing @Xcg/c " + theAerodynamicCalculator.getCNbWing().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNbWing().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCNb VTail @Xcg/c " + theAerodynamicCalculator.getCNbVertical().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNbVertical().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCNb Total @Xcg/c " + theAerodynamicCalculator.getCNbTotal().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNbTotal().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\n\tControl Derivative ... \n\n");
			System.out.println("\t\tCNdr @Xcg/c " 
					+ theAerodynamicCalculator.getCNdr()
						.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
							.get(theAerodynamicCalculator.getDeltaRudderList()
								.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
								).get(0)._1() 
					+ " and dr "
					+ theAerodynamicCalculator.getDeltaRudderList()
						.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
					+ " = "
					+ theAerodynamicCalculator.getCNdr()
						.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
							.get(theAerodynamicCalculator.getDeltaRudderList()
								.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
								).get(0)._2()
			);
			System.out.println("\n\tYawing moment curves ... \n\n");
			System.out.println("\t\tCN(b) Fuselgae @Xcg/c " + theAerodynamicCalculator.getCNFuselage().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNFuselage().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCN(b) Wing @Xcg/c " + theAerodynamicCalculator.getCNWing().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNWing().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCN(b) VTail @Xcg/c " + theAerodynamicCalculator.getCNVertical().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNVertical().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCN(b) Total @Xcg/c " + theAerodynamicCalculator.getCNTotal().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._1() + " = " + 
					theAerodynamicCalculator.getCNTotal().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0)._2()
			);
			System.out.println("\t\tCNdr @Xcg/c " 
					+ theAerodynamicCalculator.getCNDueToDeltaRudder()
						.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
							.get(theAerodynamicCalculator.getDeltaRudderList()
								.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
								).get(0)._1() 
					+ " and dr "
					+ theAerodynamicCalculator.getDeltaRudderList()
						.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
					+ " = "
					+ theAerodynamicCalculator.getCNDueToDeltaRudder()
						.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
							.get(theAerodynamicCalculator.getDeltaRudderList()
								.get(theAerodynamicCalculator.getDeltaRudderList().size()-1)
								).get(0)._2()
			);
			System.out.println("\n\tEquilibrium dr-beta ... (deg)\n\n");
			System.out.println("\t\tdelta_r = " + theAerodynamicCalculator.getBetaOfEquilibrium().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0.25).stream().map(tpl -> tpl._1.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
			System.out.println("\t\tbeta = " + theAerodynamicCalculator.getBetaOfEquilibrium().get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(0.25).stream().map(tpl -> tpl._2.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()));
			System.out.println("\n\n\tDone!! \n\n");

			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");

			System.setOut(filterStream);

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			DirectionalStabilityTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ... (if needed)
		launch(args);
	}

}
