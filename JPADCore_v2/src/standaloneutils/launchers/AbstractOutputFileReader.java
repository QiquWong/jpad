package standaloneutils.launchers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOutputFileReader {
	
	protected File theFile;
	protected Map<String, Number> variables = new HashMap<String, Number>();
	
	public AbstractOutputFileReader() {
		
	}

	public AbstractOutputFileReader(String fileFullPath) {
		File f = new File(fileFullPath);
		if(f.exists() && !f.isDirectory()) { 
		    theFile = f;
		}
	}

	public AbstractOutputFileReader(File file) {
		theFile = file; 
	}

	public File getTheFile() {
		return theFile;
	}

	public Map<String, Number> getVariables() {
		return variables;
	}
	
}
