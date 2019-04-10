package sandbox2.masc.avl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.CmdLineParser;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import sandbox2.adm.avl.AVLExternalJobTest;
import sandbox2.masc.avl.utils.AVLTestUtils;
import standaloneutils.JPADXmlReader;
import standaloneutils.launchers.avl.AVLAircraft;
import standaloneutils.launchers.avl.AVLExternalJob;
import standaloneutils.launchers.avl.AVLMacro;
import standaloneutils.launchers.avl.AVLMainInputData;
import standaloneutils.launchers.avl.AVLMassInputData;
import standaloneutils.launchers.avl.AVLOutputStabilityDerivativesFileReader;

public class AVLExternalJobTest2 {

	// declaration necessary for Concrete Object usage
		public static CmdLineParser theCmdLineParser;
		public static JPADXmlReader reader;

		//-------------------------------------------------------------

		public static Aircraft theAircraft;
		public static OperatingConditions theOperatingConditions;
		
		public static AVLExternalJob job = new AVLExternalJob();
		
		public static String baseName = "newData3";	// BaseName of AVL input files

		//-------------------------------------------------------------	
		public static void main(String[] args) throws InvalidFormatException, IOException, InterruptedException, HDF5LibraryException {

			long startTime = System.currentTimeMillis();        
			
			System.out.println("---------------------");
			System.out.println("AVL External Job Test");
			System.out.println("---------------------");

			try {
				
				System.out.println("\n--------------------------------------------- Reading the aircraft:\n");
				theAircraft = AVLTestUtils.importAircraft(args);
				
				System.out.println("\n--------------------------------------------- Prepare AVL job:\n");
				theOperatingConditions = AVLTestUtils.importOperatingConditions(args);
				AVLTestUtils.performAnalyses(theAircraft, theOperatingConditions);
	
				//System.out.println(theAircraft.toString());
				//System.out.println("Operating conditions:\n\n");
				//System.out.println(theOperatingConditions.toString());
				//System.out.println("\n\n\tDone!! \n\n");
				
				//---------------------------------------------------------------------------------------
				// Testing AVL with the current aircraft
				
				// Instantiate the job executor object
				AVLExternalJob job = AVLExternalJobTest.job;

				System.out.println("--------------------------------------------- Launch AVL (Athena Vortex Lattice) job in a separate process.");
				System.out.println("--------------------------------------------- Version: 3.37");
				// Establish the path to dir where the executable file resides 
				String binDirPath;
				File binDir = AVLTestUtils.getAVLBinDir(args); 
				if ( binDir != null) {
					binDirPath = binDir.getAbsolutePath();
					job.setBinDirectory(binDir);
					System.out.println("\n\n\tAVL bin dir given by user.");
					System.out.println("\tDoes dir exist? " + binDir.exists() + "\n\n");
					if (!binDir.exists()) {
						System.err.println("AVL bin dir not found!");
						System.err.println("TERMINATING");
					}
				} else {					
					// Set the AVLROOT environment variable
					binDirPath = System.getProperty("user.dir") + File.separator					
							+ "apps" + File.separator 
							+ "AVL" + File.separator 
							+ "bin" 				
							;
					job.setBinDirectory(new File(binDirPath));	
				}
				
				job.setEnvironmentVariable("AVLROOT", binDirPath);
					
				System.out.println("Binary directory: " + job.getBinDirectory());

				// Establish the path to executable file
				job.setExecutableFile(new File(binDirPath + File.separator + "avl.exe"));
				//job.setExecutableFile(AVLTestUtils.getAVLExecutable(args));
				
				System.out.println("Executable file: " + job.getExecutableFile());
				
				// Establish the path to the cache directory - TODO: for now the same as bin dir
				job.setCacheDirectory(new File(binDirPath));
				System.out.println("Cache directory: " + job.getCacheDirectory());
				
				//-----------------------------------------------------------------------------------------------------
				// Handle file names according to a given base-name
				// Must assign this to avoid NullPointerException
				job.setBaseName(baseName);
				
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
					}catch (IOException e) {
						System.err.println(e);
					}
				});
				// TODO delete all files like <base-name>_airfoil*.dat

				// Assign the main .avl input file
				job.setInputAVLFile(new File(binDirPath + File.separator + job.getBaseName()+".avl"));

				// Assign .run file with commands
				job.setInputRunFile(new File(binDirPath + File.separator + job.getBaseName()+".run"));
				
				//-------------------------------------------------------------------------
				// Generate data

				AVLMainInputData inputData = job.importToMainInputData(theOperatingConditions, theAircraft);

				AVLAircraft avlAircraft = job.importToAVLAircraft(theAircraft);

				AVLMassInputData massData = job.importToMassInputData(theAircraft);

				if (massData != null) { // true if massData != null
					
					job.setEnabledStabilityAnalysis(true);
					
					// Assign the .mass file
					job.setInputMassFile(new File(binDirPath + File.separator + job.getBaseName()+".mass"));
					
					// Assign the output stability derivatives file
					job.setOutputStabilityDerivativesFile(new File(binDirPath + File.separator + job.getBaseName()+".st"));

					// Assign the output stability derivatives file
					job.setOutputStabilityDerivativesBodyAxesFile(new File(binDirPath + File.separator + job.getBaseName()+".sb"));
				}				
				
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
				String jobCommandLine = job.formCommand(inputData, avlAircraft, massData, avlMacro);

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
				if (job.isEnabledStabilityAnalysis()) {
					
					System.out.println("Output file full path: " + job.getOutputStabilityDerivativesFile());
					// Use AVLOutputStabilityDerivativesFileReader object
					AVLOutputStabilityDerivativesFileReader reader = new AVLOutputStabilityDerivativesFileReader(job.getOutputStabilityDerivativesFile());
					
					System.out.println("Is the AVL output file available? " + reader.isFileAvailable());
					System.out.println("The AVL output file to read: " + reader.getTheFile());
					
					// parse the file and build map of variables & values
					reader.parse();
					
					// print the map
					System.out.println("------ Map of variables ------");
					Map<String, List<Number>> variables = reader.getVariables();
					// Print the map of variables
					variables.forEach((key, value) -> {
						System.out.println(key + " = " + value);
					});		
				}
				
				System.out.println("---------------------------------------------");

				System.out.println("External job terminated.");
				
				
				long estimatedTime = System.currentTimeMillis() - startTime;
				System.out.println("\n\n\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
				
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
				AVLExternalJobTest2.theCmdLineParser.printUsage(System.err);
				System.err.println();
				System.err.println("  Must launch this app with proper command line arguments.");
				System.err.println("  Make sure you point to a valid aircraft data set.");
				return;
			}	  			
		}		
}