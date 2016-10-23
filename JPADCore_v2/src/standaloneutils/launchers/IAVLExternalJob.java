package standaloneutils.launchers;

import java.io.File;

public interface IAVLExternalJob extends IExternalJob {
	
	public File getOutputStabilityDerivativesFile();
	public void setOutputStabilityDerivativesFile(File outputFile);
	
}
