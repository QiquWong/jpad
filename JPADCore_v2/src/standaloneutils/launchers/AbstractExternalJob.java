package standaloneutils.launchers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExternalJob implements IExternalJob {
	
	protected File configFile;
	protected File inputFile;
	protected File binDirectory;
	protected File cacheDirectory;
	protected String environmentVariables;
	protected File outputFile;
	
	protected List<String> commandInformation = new ArrayList<String>();
	protected SystemCommandExecutor systemCommandExecutor;
	protected StringBuilder stdOut, stdErr;
	

	public AbstractExternalJob() {
	}

	public AbstractExternalJob(List<String> cmdInfo) {
		this.commandInformation.addAll(cmdInfo);
	}

	public AbstractExternalJob(String cmdLine) {
		this.commandInformation.add(cmdLine);
	}
	
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
	
	public SystemCommandExecutor getSystemCommandExecutor() {
		return systemCommandExecutor;
	}

	public StringBuilder getStdOut() {
		return stdOut;
	}

	public StringBuilder getStdErr() {
		return stdErr;
	}

}
