package standaloneutils.launchers;

import java.io.File;
import java.util.List;

public abstract class AbstractExternalJob extends SystemCommandExecutor {
	

	File configFile;
	File inputFile;
	File binDirectory;
	File cacheDirectory;
	String environmentVariables;
	File outputFile;
	
	List<String> commandInformation;
	
	public AbstractExternalJob(
			List<String> cmdInfo
			) {
		super(cmdInfo);
		this.commandInformation = cmdInfo;
	}
	
	public abstract void formCommandLine();

	public abstract void execute();
	
	public List<String> getCommandLineInformation() {
		return commandInformation;
	} 

	public String getCommandLine() {
		return String.join(" ", commandInformation);
	} 
	
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
