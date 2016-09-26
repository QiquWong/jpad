package standaloneutils.launchers;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/

public class DummyExternalJob extends AbstractExternalJob {

	public DummyExternalJob(List<String> commandInformation) {
		super(commandInformation);
	}


	@Override
	public void execute() {
		// TODO Auto-generated method stub
		System.out.println("Hello from DummyExternalJob::run");
		
	}

	@Override
	public void formCommandLine() {
		// TODO Auto-generated method stub
		commandLine = "dir";
		
	}

	public static void main(String[] args) 
			throws InterruptedException {
		DummyExternalJob dummyJob = new DummyExternalJob();
		System.out.println("Launch job in a separate thread.");
		dummyJob.formCommandLine();
		
		
		System.out.println("Job terminated.");
		
	}
}
