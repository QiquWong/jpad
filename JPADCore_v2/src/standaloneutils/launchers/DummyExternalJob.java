package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/

/*
 * Use case:
 * 
 * DummyExternalJob job = new DummyExternalJob();
 * String commandLine = job.formCommand();
 * int status = job.execute();
 * 
 */
public class DummyExternalJob extends AbstractExternalJob {

	private DatcomOutputFileReader datcomOutputFileReader;
	
	/*
	 *  TODO modify this function as appropriate
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
		this.setOutputFile(
				new File(binDirPath + File.separator 
					+ this.getInputFile().getName().replaceFirst(".dcm", ".out")
				)
			);
		
		System.out.println("Output file full path: " + this.getOutputFile());
		System.out.println("Output file name: " + this.getOutputFile().getName());
		
		commandInformation.add(
				"cd " + binDirPath
				);
		commandInformation.add(
				"& "
				+ "datcom.bat " + this.getInputFile().getName()
				);
		
		return this.getCommandLine();
	}

	@Override
	public void setEnvironmentVariable(String varName, String value) {
		// systemCommandExecutor.setEnvironmentVariable(varName, value);
		additionalEnvironment.put(varName, value);
	}
	
	@Override
	public int execute() throws IOException, InterruptedException {
		
		System.out.println("DummyExternalJob::execute --> launching external process");
	    
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
	public boolean generateInputFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parseOutputFile() {
		
		datcomOutputFileReader = new DatcomOutputFileReader(this.outputFile);
		System.out.println("The Datcom output file is available? " + datcomOutputFileReader.isFileAvailable());
		System.out.println("The Datcom output file to read: " + datcomOutputFileReader.getTheFile());
		
		return datcomOutputFileReader.parse();
		
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		
		// Instantiate the job executor object
		DummyExternalJob job = new DummyExternalJob();
		
		System.out.println("--------------------------------------------- Launch job in a separate process.");
		
		// Set the DATCOMROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "Datcom" + File.separator 
				+ "bin" 				
				;
		job.setEnvironmentVariable("DATCOMROOT", binDirPath);
		
		// Establish the path to the executable file 
		job.setBinDirectory(new File(binDirPath));
		System.out.println("Binary directory: " + job.getBinDirectory());

//		// Assign the input file
//		job.setInputFile(new File(binDirPath + File.separator 
//				// + "B-737.dcm" // OK
//				+ "X-airplane.dcm" // OK
//				));

//		// Generate the input file
//
//		// Assign the input file
//		job.setInputFile(new File(binDirPath + File.separator 
//				+ "X-airplane.dcm"
//				));
		
		// Assign the input file
		job.setInputFile(new File(binDirPath + File.separator 
				+ "X-airplane-2.dcm"
				));
		
		//-------------------------------------------------------------------------
		// Generate data
		//
		// NOTE: these data might come from the outside, generated by the 
		//       aircraft design process
		DatcomInputData inputData = new DatcomInputData
				.Builder()
				/*
				 * set a new description wrt default
				 */
				.setDescription("(C) Agostino De Marco, agodemar")
				/*
				 *  set a new list of Mach numbers  wrt default
				 */
				.mutateFltcon_MACH(machNumbers -> machNumbers.clear())
				//.addAllFltcon_MACH(Arrays.asList(0.3, 0.4))
				.addAllFltcon_MACH(Arrays.asList(0.4))
				/*
				 *  wing position
				 */
				.setSynths_XW(29.3)
				.setSynths_ZW(-1.2)
				/*
				 *  build object, finally
				 */
				.build(); // validate for all fields to be set; Optional fields are empty	
		
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
	    System.out.println("DATCOMROOT=" + env.get("DATCOMROOT"));
	    System.out.println("windir=" + env.get("windir"));
	    System.out.println("---------------------------------------------");
	    
	    // Parse the Datcom output file
	    job.parseOutputFile();

		// print the map of variables
		Map<String, List<Number>> variables = job.getDatcomOutputReader().getVariables();
		// Print the map of variables
		variables.forEach((key, value) -> {
		    System.out.println(key + " = " + value);
		});
		System.out.println("Number of alpha's = " + job.getDatcomOutputReader().getNAlphas());
	    
	    System.out.println("---------------------------------------------");
		System.out.println("Job terminated.");
		
	}

	public DatcomOutputFileReader getDatcomOutputReader() {
		return datcomOutputFileReader;
	}

}
