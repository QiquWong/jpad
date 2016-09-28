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
		
		String lenghtUnits = "";
		String derivUnits = "";
		
		System.out.println("Parsing...\n");
		try (Scanner scanner =  new Scanner(this.outputFile)) {
			// process line-by-line
			while (scanner.hasNextLine()){
				// processLine(scanner.nextLine());
				//System.out.println(scanner.nextLine());
				
				// regular expressions
				// http://www.vogella.com/tutorials/JavaRegularExpressions/article.html
				
				// datcom read
				// https://searchcode.com/codesearch/view/3176213/
				
				// http://www.dept.aoe.vt.edu/~mason/Mason_f/MRsoft.html
				// http://www.dept.aoe.vt.edu/~mason/Mason_f/747LDs.out
				
				String line = scanner.nextLine();

				//----------------------------------------------------
				// Find the default dimensions
				if (line
						.matches(".*DIM\\s.*")
					&&
					lenghtUnits.equals("")
					) {
					System.out.println(line);
					String[] splitString = (line.trim().split("\\s+"));
					System.out.println(Arrays.toString(splitString));
					lenghtUnits = splitString[1].toLowerCase();
					System.out.println("Length units: " + lenghtUnits);
				}				
				
				if (line
						.matches(".*DERIV\\s.*")
					&&
					derivUnits.equals("")
					) {
					System.out.println(line);
					String[] splitString = (line.trim().split("\\s+"));
					System.out.println(Arrays.toString(splitString));
					derivUnits = splitString[1].toLowerCase();
					System.out.println("Derivative units: " + derivUnits);
				}				

				
				//.matches("\\w.*")
				//.matches("0.*IDEAL ANGLE OF ATTACK =.*")
				//.matches("0.*MACH=.*LIFT-CURVE-SLOPE =.*XAC =.*")
				
				//----------------------------------------------------
				// Find the block with results
				if (line
						.matches("1.*AUTOMATED STABILITY AND CONTROL METHODS PER APRIL 1976 VERSION OF DATCOM.*")
					) {
					// advance and check if this is a block with results
					line = scanner.nextLine();
					if (line
							.matches(".*CHARACTERISTICS AT ANGLE OF ATTACK AND IN SIDESLIP.*")
						) {
						System.out.println(line);
						// advance 8 lines
						for(int i = 0; i < 8; i++) {
							line = scanner.nextLine();
							System.out.println(line);
						}
						// advance and get row of numbers
						line = scanner.nextLine();
						System.out.println(line);
						String[] splitString = (line.split("\\s+"));
						System.out.println("Values: " + Arrays.toString(splitString));
						
						Double mach = Double.valueOf(splitString[1]);
						System.out.println("Mach = " + mach);
						
						// TODO: handle -->  ALTITUDE   VELOCITY    PRESSURE    TEMPERATURE
						
					}
				}
			}
			System.out.println("\n...parsing done");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
