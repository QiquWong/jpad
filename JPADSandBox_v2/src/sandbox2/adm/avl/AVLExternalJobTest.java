package sandbox2.adm.avl;

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
import org.kohsuke.args4j.CmdLineParser;

import aircraft.Aircraft;
import analyses.ACAerodynamicAndStabilityManager_v2;
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import sandbox2.adm.avl.utils.AircraftUtils;
import standaloneutils.JPADXmlReader;
import standaloneutils.launchers.avl.AVLAircraft;
import standaloneutils.launchers.avl.AVLExternalJob;
import standaloneutils.launchers.avl.AVLMacro;
import standaloneutils.launchers.avl.AVLMainInputData;
import standaloneutils.launchers.avl.AVLMassInputData;
import standaloneutils.launchers.avl.AVLOutputStabilityDerivativesFileReader;
import writers.JPADStaticWriteUtils;


public class AVLExternalJobTest {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;
	public static OperatingConditions theOperatingConditions;
	
	public static AVLExternalJob job = new AVLExternalJob();


	//-------------------------------------------------------------

	public static void main(String[] args) throws InvalidFormatException, IOException, InterruptedException, HDF5LibraryException {

		long startTime = System.currentTimeMillis();        
		
		System.out.println("---------------------");
		System.out.println("AVL External Job Test");
		System.out.println("---------------------");
		
		
		
		try {
			
			theAircraft = AircraftUtils.importAircraft(args);
			theOperatingConditions = AircraftUtils.importOperatingConditions(args);
			AircraftUtils.performAnalyses(theAircraft, theOperatingConditions);
			
			System.out.println("--------------------------------------------- Prepared AVL job:");

			System.out.println(theAircraft.toString());
			System.out.println(theOperatingConditions.toString());
			

			

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
			
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			AVLExternalJobTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

	}

}
