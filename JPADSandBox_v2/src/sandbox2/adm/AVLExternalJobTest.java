package sandbox2.adm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import analyses.ACAnalysisManager;
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import javafx.application.Application;
import javafx.stage.Stage;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.launchers.avl.AVLAircraft;
import standaloneutils.launchers.avl.AVLExternalJob;
import standaloneutils.launchers.avl.AVLInputGenerator;
import standaloneutils.launchers.avl.AVLMacro;
import standaloneutils.launchers.avl.AVLMainInputData;
import standaloneutils.launchers.avl.AVLMassInputData;
import standaloneutils.launchers.avl.AVLOutputStabilityDerivativesFileReader;
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

public class AVLExternalJobTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;
	
	public static AVLExternalJob job = new AVLExternalJob();


	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the aircraft object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the aircraft object ...");

		Aircraft aircraft = AVLExternalJobTest.theAircraft;
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
	public static void main(String[] args) throws InvalidFormatException, IOException, InterruptedException, HDF5LibraryException {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

//		final PrintStream originalOut = System.out;
//		PrintStream filterStream = new PrintStream(new OutputStream() {
//		    public void write(int b) {
//		         // write nothing
//		    }
//		});
		long startTime = System.currentTimeMillis();        
		
		System.out.println("-------------------");
		System.out.println("Complete Analysis Test");
		System.out.println("-------------------");
		
		MyArgumentsAnalysis va = new MyArgumentsAnalysis();
		AVLExternalJobTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			AVLExternalJobTest.theCmdLineParser.parseArgument(args);
			
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
			
//			// deactivating system.out
//			System.setOut(filterStream);
			
			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader,
//					fusDesDatabaseReader,
//					veDSCDatabaseReader
//					)
//					.build();

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
			
			// activating system.out
//			System.setOut(originalOut);			
			System.out.println(theAircraft.toString());
//			System.setOut(filterStream);
			

			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + theAircraft.getId() + "_AVL" + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
//			System.setOut(originalOut);
			System.out.println("\n\n\tDefining the operating conditions ... \n\n");
//			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
//			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
//			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Analyzing the aircraft
//			System.setOut(originalOut);
			System.out.println("\n\n\tRunning requested analyses ... \n\n");
//			System.setOut(filterStream);
			theAircraft.setTheAnalysisManager(ACAnalysisManager.importFromXML(pathToAnalysesXML, theAircraft, theOperatingConditions));
			theAircraft.getTheAnalysisManager().doAnalysis(theAircraft, theOperatingConditions, subfolderPath);
//			System.setOut(originalOut);
			System.out.println("\n\n\tDone!! \n\n");
//			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Printing results (activating system.out)
//			System.setOut(originalOut);
			System.out.println("\n\n\tPrinting results ... \n\n");
			System.out.println(theAircraft.getTheAnalysisManager().toString());
			System.out.println("\n\n\tDone!! \n\n");
			
			
			//---------------------------------------------------------------------------------------
			// Testing AVL with the current aircraft
			
			// Instantiate the job executor object
			AVLExternalJob job = AVLExternalJobTest.job;

			System.out.println("--------------------------------------------- Launch AVL job in a separate process.");

			// Set the AVLROOT environment variable
			String binDirPath = System.getProperty("user.dir") + File.separator					
					+ "apps" + File.separator 
					+ "AVL" + File.separator 
					+ "bin" 				
					;
			job.setEnvironmentVariable("AVLROOT", binDirPath);

			// Establish the path to dir where the executable file resides 
			job.setBinDirectory(new File(binDirPath));
			System.out.println("Binary directory: " + job.getBinDirectory());

			// Establish the path to executable file
			job.setExecutableFile(new File(binDirPath + File.separator + "avl.exe"));
			System.out.println("Executable file: " + job.getExecutableFile());
			
			// Establish the path to the cache directory - TODO: for now the same as bin dir
			job.setCacheDirectory(new File(binDirPath));
			System.out.println("Cache directory: " + job.getCacheDirectory());
			
			//-----------------------------------------------------------------------------------------------------
			// Handle file names according to a given base-name
			// Must assign this to avoid NullPointerException
			job.setBaseName("newData3");
			
			// gather files and clean up before execution
			List<String> fileNames = new ArrayList<>();
			Stream<String> fileExtensions = Stream.of(".run", ".avl", ".mass", ".st", ".sb", ".eig");
			fileExtensions.forEach(ext -> fileNames.add(job.getBaseName()+ext));

			fileNames.stream().forEach(name -> {
				Path path = FileSystems.getDefault().getPath(
						binDirPath + File.separator + name);
				try {
					System.out.println("Deleting file: " + path);
					Files.delete(path);
				} catch (NoSuchFileException e) {
					System.err.format("%s: no such" + " file or directory: %1$s\n", path);
				} catch (DirectoryNotEmptyException e) {
					System.err.format("%1$s not empty\n", path);
				} catch (IOException e) {
					System.err.println(e);
				}
			});
			// TODO delete all files like <base-name>_airfoil*.dat
			
			// Assign the main .avl input file
			job.setInputAVLFile(new File(binDirPath + File.separator + job.getBaseName()+".avl"));

			// Assign the .mass file
			job.setInputMassFile(new File(binDirPath + File.separator + job.getBaseName()+".mass"));
			
			// Assign the output stability derivatives file
			job.setOutputStabilityDerivativesFile(new File(binDirPath + File.separator + job.getBaseName()+".st"));

			// Assign the output stability derivatives file
			job.setOutputStabilityDerivativesBodyAxesFile(new File(binDirPath + File.separator + job.getBaseName()+".sb"));

			// Assign .run file with commands
			job.setInputRunFile(new File(binDirPath + File.separator + job.getBaseName()+".run"));
			
			//-------------------------------------------------------------------------
			// Generate data

			AVLMainInputData inputData = job.importToMainInputData(theOperatingConditions, theAircraft);
			
			AVLAircraft aircraft = job.importToAVLAircraft(theAircraft);
			
			AVLMassInputData massData = job.importToMassInputData(theAircraft);
			
			AVLMacro avlMacro = job.formRunMacro(theOperatingConditions); // TODO: modify this as appropriate
			
			/*
			 * ================================================================
			 * Form the final command to launch the external process
			 *
			 * Example, in Win32 shell:
			 *
			 * >$ cd <avl-executable-dir>
			 * >$ avl.exe < <base-name>.run
			 * 
			 * ================================================================
			 */
			String jobCommandLine = job.formCommand(inputData, aircraft, massData, avlMacro);

			// Print out the command line
			System.out.println("Command line: " + jobCommandLine);

			System.out.println("---------------------------------------------");
			System.out.println("EXECUTE JOB:\n");
			int status = job.execute();

			// print the stdout and stderr
			System.out.println("The numeric result of the command was: " + status);
			System.out.println("---------------------------------------------");
			System.out.println("STDOUT:");
			System.out.println(job.getStdOut());
			System.out.println("---------------------------------------------");
			System.out.println("STDERR:");
			System.out.println(job.getStdErr());
			System.out.println("---------------------------------------------");
			System.out.println("Environment variables:");
			Map<String, String> env = job.getEnvironment();
			// env.forEach((k,v)->System.out.println(k + "=" + v));
			System.out.println("AVLROOT=" + env.get("AVLROOT"));
			System.out.println("windir=" + env.get("windir"));
			System.out.println("---------------------------------------------");

			// Parse the AVL output file
			
			System.out.println("Output file full path: " + job.getOutputStabilityDerivativesFile());
			
			// Use AVLOutputStabilityDerivativesFileReader object
			AVLOutputStabilityDerivativesFileReader reader = new AVLOutputStabilityDerivativesFileReader(job.getOutputStabilityDerivativesFile());
			
			System.out.println("The Datcom output file is available? " + reader.isFileAvailable());
			System.out.println("The Datcom output file to read: " + reader.getTheFile());
			
			// parse the file and build map of variables & values
			reader.parse();
			
			// print the map
			System.out.println("------ Map of variables ------");
			Map<String, List<Number>> variables = reader.getVariables();
			// Print the map of variables
			variables.forEach((key, value) -> {
			    System.out.println(key + " = " + value);
			});		
			
			System.out.println("---------------------------------------------");

			System.out.println("External job terminated.");
			
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
			
//			System.setOut(filterStream);
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			AVLExternalJobTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

		// JavaFX ... (if needed)
		launch(args);
	}

}
