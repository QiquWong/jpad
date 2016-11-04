package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// see: http://www.uavs.us/2011/12/02/matlab-avl-control/

public class AVLExternalJob implements IAVLExternalJob {
	
	protected File configFile;
	protected File inputFile;
	protected File binDirectory;
	protected File cacheDirectory;
	protected File outputFile;
	protected File outputStabilityDerivativesFile;
	
	Map<String, String> additionalEnvironment = new HashMap<String, String>();
	
	protected List<String> commandInformation = new ArrayList<String>();
	protected SystemCommandExecutor systemCommandExecutor;
	protected StringBuilder stdOut, stdErr;
	
	private AVLOutputStabilityDerivativesFileReader outputStabilityDerivativesFileReader;

	public static void main(String[] args) throws IOException, InterruptedException {
		// Instantiate the job executor object
		AVLExternalJob job = new AVLExternalJob();

		System.out.println("--------------------------------------------- Launch AVL job in a separate process.");

		// Set the AVLROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "AVL" + File.separator 
				+ "bin" 				
				;
		job.setEnvironmentVariable("AVLROOT", binDirPath);

		// Establish the path to dir where the executable file resides 
		job.setBinDirectory(new File(binDirPath));
		System.out.println("Binary directory: " + job.getBinDirectory());

		// Assign the input file
		job.setInputFile(new File(binDirPath + File.separator 
				+ "newData1.run"
				));
		
		// Clean previously generated outputs
		Stream<String> fileNames = Stream.of("newData1.st", "newData1.sb", "newData1.eig");
		fileNames.forEach(name -> {
			Path path = FileSystems.getDefault().getPath(
					binDirPath + File.separator + name);
			try {
				System.out.println("Deleting file: " + path);
				Files.delete(path);
			} catch (NoSuchFileException e) {
			    System.err.format("%s: no such" + " file or directory%n", path);
			} catch (DirectoryNotEmptyException e) {
			    System.err.format("%s not empty%n", path);
			} catch (IOException e) {
				System.err.println(e);
			}
		});


		//-------------------------------------------------------------------------
		// Generate data
		//
		// NOTE: these data might come from the outside, generated by the 
		//       aircraft design process
		AVLInputData inputData = new AVLInputData
				.Builder()
				/*
				 *    Description
				 */
				.setDescription("(C) Agostino De Marco, agodemar")
				/*
				 *    Mach number
				 */
				.setMain_Mach(0.3) // only one Mach number at time permitted
				/*
				 *   Wing position
				 */
				// ...
				/*
				 *   Build object, finally 
				 *   Validate for all fields to be set, Optional fields are empty	
				 *   
				 */
				.build();

		//-------------------------------------------------------------------------
		// Form the final command to launch the external process
		String commandLine = job.formCommand(binDirPath, inputData);

		// Print out the command line
		System.out.println("Command line: " + commandLine);

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

/*

		// Parse the AVL output file
		job.parseOutputFile();

		// print the map of variables
		Map<String, List<Number>> variables = job.getAVLOutputReader().getVariables();
		// Print the map of variables
		variables.forEach((key, value) -> {
			System.out.println(key + " = " + value);
		});
		System.out.println("Number of alpha's = " + job.getAVLOutputReader().getNAlphas());
		System.out.println("---------------------------------------------");

*/

		System.out.println("Job terminated.");

	}

	/*
	 *  TODO modify this function as appropriate
	 */
	private String formCommand(String binDirPath, AVLInputData inputData) {

		// build the system command we want to run
		// TODO: handle Win32 and Win64 with separate tags,
		//       handle Linux and Mac iOS as well
		String binShellWin32 = System.getenv("WINDIR") + File.separator
				+ "syswow64"  + File.separator
				+ "cmd.exe"
				;
		System.out.println("Shell Win32 launcher: " + binShellWin32);

		// the Win32 shell cmd.exe
		commandInformation.add(binShellWin32);
		// option /C to cmd.exe
		commandInformation.add("/C");
		// command line to pass to the shell prompt

		//			commandInformation.add("dir"); // must be on Windows
		//			commandInformation.add(
		//					"." + File.separator
		//					+ "src" + File.separator
		//					+ "standaloneutils" + File.separator
		//					+ "launchers"
		//					);

		//			// The following writes a file similar to B-737.dcm
		//			DatcomPlusInputGenerator.writeTemplate(this.getInputFile().getAbsolutePath()); // Ok

		// Write out the input file
		AVLInputGenerator.writeDataToFile(inputData, this.getInputFile().getAbsolutePath());

		System.out.println("Input file full path: " + this.getInputFile());
		System.out.println("Input file name: " + this.getInputFile().getName());

		// Assign the output file
		this.setOutputFile(
				new File(binDirPath + File.separator 
						+ this.getInputFile().getName().replaceFirst(".avl", ".st")
						)
				);

		System.out.println("Output file full path: " + this.getOutputFile());
		System.out.println("Output file name: " + this.getOutputFile().getName());

		commandInformation.add(
				"cd " + binDirPath
				);
		commandInformation.add(
				"& "
						+ "avl.exe < " + this.getInputFile().getAbsolutePath() // .getName()
				);

		return this.getCommandLine();
	}

	@Override
	public int execute() throws IOException, InterruptedException {
		
		System.out.println("AVLExternalJob::execute --> launching external process");
	    
		// allocate the executor
	    this.systemCommandExecutor = new SystemCommandExecutor(commandInformation);
	    
	    // fetch additional environment variables, before executing the process
	    additionalEnvironment.forEach(
	    		(k,v) -> systemCommandExecutor.setEnvironmentVariable(k, v)
	    );

	    // execute the process
	    int result = systemCommandExecutor.executeCommand();
	    
	    // get the stdout and stderr from the command that was run
	    stdOut = systemCommandExecutor.getStandardOutputFromCommand();
	    stdErr = systemCommandExecutor.getStandardErrorFromCommand();
		
	    return result;
	}

	@Override
	public Map<String, String> getEnvironment() {
		return systemCommandExecutor.getProcessBuilder().environment();
	}

	@Override
	public void setEnvironmentVariable(String varName, String value) {
		// systemCommandExecutor.setEnvironmentVariable(varName, value);
		additionalEnvironment.put(varName, value);
	}

	@Override
	public File getOutputStabilityDerivativesFile() {
		return this.outputStabilityDerivativesFile;
	}

	@Override
	public void setOutputStabilityDerivativesFile(File file) {
		this.outputStabilityDerivativesFile = file;
	}

	@Override
	public boolean parseOutputStabilityDerivativesFile() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String> getCommandLineInformation() {
		return commandInformation;
	} 

	public String getCommandLine() {
		return String.join(" ", commandInformation);
	} 
	
	public File getConfigFile() {
		return configFile;
	}
	
	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	public File getBinDirectory() {
		return binDirectory;
	}
	
	public void setBinDirectory(File binDirectory) {
		this.binDirectory = binDirectory;
	}
	
	public File getCacheDirectory() {
		return cacheDirectory;
	}
	
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public SystemCommandExecutor getSystemCommandExecutor() {
		return systemCommandExecutor;
	}

	public StringBuilder getStdOut() {
		return stdOut;
	}

	public StringBuilder getStdErr() {
		return stdErr;
	}
	
	/*
	 *  The most important function, forms the command to be launched 
	 */
	public String formCommand(String binDirPath, DatcomInputData inputData) {
		
		// build the system command we want to run
		// TODO: handle Win32 and Win64 with separate tags,
		//       handle Linux and Mac iOS as well
		String binShellWin32 = System.getenv("WINDIR") + File.separator
				+ "syswow64"  + File.separator
				+ "cmd.exe"
				;
		System.out.println("Shell Win32 launcher: " + binShellWin32);
		
		// the Win32 shell cmd.exe
		commandInformation.add(binShellWin32);
		// option /C to cmd.exe
		commandInformation.add("/C");
		// command line to pass to the shell prompt
		
//		commandInformation.add("dir"); // must be on Windows
//		commandInformation.add(
//				"." + File.separator
//				+ "src" + File.separator
//				+ "standaloneutils" + File.separator
//				+ "launchers"
//				);
		
//		// The following writes a file similar to B-737.dcm
//		DatcomPlusInputGenerator.writeTemplate(this.getInputFile().getAbsolutePath()); // Ok
		
		// Write out the input file
		DatcomPlusInputGenerator.writeDataToFile(inputData, this.getInputFile().getAbsolutePath());

		System.out.println("Input file full path: " + this.getInputFile());
		System.out.println("Input file name: " + this.getInputFile().getName());

		// Assign the output file
		this.setOutputStabilityDerivativesFile(
				new File(binDirPath + File.separator 
					+ this.getInputFile().getName().replaceFirst(".avl", ".st")
				)
			);
		
		System.out.println("Output file full path: " + this.getOutputFile());
		System.out.println("Output file name: " + this.getOutputFile().getName());
		
		commandInformation.add(
				"cd " + binDirPath
				);
		commandInformation.add(
				"& "
				+ "avl.exe < " + this.getInputFile().getAbsolutePath() // .getName()
				);
		
		return this.getCommandLine();
	}
}
