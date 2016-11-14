package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface IAVLExternalJob {
	
	/**
	 * Execute the external process using a ProcessBuilder object
	 */    
	public int execute() throws IOException, InterruptedException;

	public File getExecutableFile();
	public void setExecutableFile(File file);
	
	/**
	 * Get the map of environment variables and their values
	 */    
	Map<String, String> getEnvironment();
	
	/**
	 * Assign a value to an environment variable
	 */    
	public void setEnvironmentVariable(String varName, String value);

	public File getOutputStabilityDerivativesFile();
	public void setOutputStabilityDerivativesFile(File file);
	
	/**
	 * Parse the results of job execution.
	 * Return true if output file parsed successfully  
	 */    
	public boolean parseOutputStabilityDerivativesFile();

	StringBuilder getStdOut();

	StringBuilder getStdErr();

	SystemCommandExecutor getSystemCommandExecutor();

	void setCacheDirectory(File cacheDirectory);

	File getCacheDirectory();

	void setBinDirectory(File binDirectory);

	File getBinDirectory();

	String getCommandLine();

	File getInputRunFile();

	void setInputRunFile(File file);

	File getInputAVLFile();

	void setInputAVLFile(File inputFile);

	File getInputMassFile();

	void setInputMassFile(File massFile);
}
