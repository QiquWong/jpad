package standaloneutils.launchers;

import java.io.File;

public abstract class AbstractExternalJob implements Runnable {
	
	File configFile;
	File inputFile;
	File binDirectory;
	File cacheDirectory;
	String environmentVariables;
	String commandLine;
	File outputFile;
	
	public abstract void formCommandLine();
	
	public File getConfigFile() {
		return configFile;
	}
	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}
	public File getInputFile() {
		return inputFile;
	}
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	public File getBinDirectory() {
		return binDirectory;
	}
	public void setBinDirectory(File binDirectory) {
		this.binDirectory = binDirectory;
	}
	public File getCacheDirectory() {
		return cacheDirectory;
	}
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}
	public String getEnvironmentVariables() {
		return environmentVariables;
	}
	public void setEnvironmentVariables(String environmentVariables) {
		this.environmentVariables = environmentVariables;
	}
	public File getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	

}
