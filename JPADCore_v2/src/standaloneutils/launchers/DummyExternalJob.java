package standaloneutils.launchers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// https://examples.javacodegeeks.com/core-java/lang/processbuilder/java-lang-processbuilder-example/

public class DummyExternalJob extends AbstractExternalJob {

	@Override
	public void run() {
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
		
//		Thread t = new Thread(new DummyExternalJob());
//		t.start();
//		t.join(); // wait until thread t ends
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);		
		executor.execute(dummyJob);
		executor.shutdown();
		
		System.out.println("Thread terminated.");
		
		
	}

}
