package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

	private String formCommand() {
		// build the system command we want to run
		
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
		
		/*
		commandInformation.add("dir");
		commandInformation.add(
				"." + File.separator
				+ "src" + File.separator
				+ "standaloneutils" + File.separator
				+ "launchers"
				);
		*/
		
		// Set the DATCOMROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "Datcom" + File.separator 
				+ "bin" 				
				;
		this.setEnvironmentVariable("DATCOMROOT", binDirPath);
		
		// Assign the Binary dir
		this.setBinDirectory(new File(binDirPath));
		System.out.println("Binary directory: " + this.getBinDirectory());

		// Assign the input file
		this.setInputFile(new File(binDirPath + File.separator + "B-737.dcm"));
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
		
		DatcomOutputFileReader reader = new DatcomOutputFileReader(this.outputFile);
		System.out.println("The Datcom output file is available? " + reader.isFileAvailable());
		System.out.println("The Datcom output file to read: " + reader.getTheFile());
		
		return reader.parse();
		
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		
		DummyExternalJob job = new DummyExternalJob();
		
		System.out.println("--------------------------------------------- Launch job in a separate process.");
		
		// Form the final command -- adjust as appropriate
		String commandLine = job.formCommand();

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
	    
	    job.parseOutputFile();
	    
	    System.out.println("---------------------------------------------");
		System.out.println("Job terminated.");
		
	}

}
