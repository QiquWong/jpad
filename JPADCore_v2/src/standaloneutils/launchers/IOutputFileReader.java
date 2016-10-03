package standaloneutils.launchers;

/**
 * Describe the public interface of an abstract output file reader
 * @author Agostino De Marco
 *
 */
public interface IOutputFileReader {

	/**
	 * Parse the output file, fill the variables Map object
	 * Return true if parse is successful
	 */    
	public boolean parse();
	
	/**
	 * Check if file is ok
	 * Return true if parse is successful
	 */    
	public boolean isFileAvailable();
	
	
}
