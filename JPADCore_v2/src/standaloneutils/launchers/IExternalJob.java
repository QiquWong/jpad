package standaloneutils.launchers;

import java.io.IOException;

/**
 * Describe the public interface of an abstract external job
 * @author Agostino De Marco
 *
 */
public interface IExternalJob {

	/**
	 * Form the internal list of strings making the commandInformation
	 * Return the joined string.
	 */    
	public String formCommand();

	/**
	 * Execute the external process using a ProcessBuilder object
	 */    
	public int execute() throws IOException, InterruptedException;

}
