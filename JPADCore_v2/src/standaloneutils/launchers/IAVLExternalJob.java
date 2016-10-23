package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface IAVLExternalJob {
	
	/**
	 * Execute the external process using a ProcessBuilder object
	 */    
	public int execute() throws IOException, InterruptedException;
	
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
}
