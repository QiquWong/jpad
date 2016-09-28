package standaloneutils.launchers;

import java.io.IOException;
import java.util.Map;

/**
 * Describe the public interface of an abstract external job
 * @author Agostino De Marco
 *
 */
public interface IExternalJob {

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

	/**
	 * Generate the input file(s)
	 */    
	public boolean generateInputFile();
	
	/**
	 * Parse the results of job execution.
	 * Return true if output file parsed successfully  
	 */    
	public boolean parseOutputFile();

}
