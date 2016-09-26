package standaloneutils.launchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

	@Override
	public String formCommand() {
		// build the system command we want to run
		commandInformation.add("dir");
		commandInformation.add(".\\src\\standaloneutils\\launchers");
		return this.getCommandLine();
	}

	@Override
	public int execute() throws IOException, InterruptedException {
		
		System.out.println("DummyExternalJob::execute --> launching external process");
	    
		// execute the command
	    this.systemCommandExecutor = new SystemCommandExecutor(commandInformation);
	    int result = systemCommandExecutor.executeCommand();
	    
	    // get the stdout and stderr from the command that was run
	    stdOut = systemCommandExecutor.getStandardOutputFromCommand();
	    stdErr = systemCommandExecutor.getStandardErrorFromCommand();
		
	    return result;
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		DummyExternalJob dummyJob = new DummyExternalJob();
		System.out.println("--------------------------------------------- Launch job in a separate process.");
		String commandLine = dummyJob.formCommand();
		System.out.println("Command line: " + commandLine);
		int status = dummyJob.execute();

	    // print the stdout and stderr
	    System.out.println("The numeric result of the command was: " + status);
	    System.out.println("---------------------------------------------");
	    System.out.println("STDOUT:");
	    System.out.println(dummyJob.getStdOut());
	    System.out.println("---------------------------------------------");
	    System.out.println("STDERR:");
	    System.out.println(dummyJob.getStdErr());
	    System.out.println("---------------------------------------------");
		
		System.out.println("Job terminated.");
		
	}
}
