package it.unina.daf.jpad.testtemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javaslang.Tuple;
import standaloneutils.JPADXmlReader;

public class Test_Template {
	
	@Option(name = "-i", aliases = { "--input-dir" }, required = false, // if given, overrides the default & config
			usage = "my input file")
	private File _inputPath;

	@Option(name = "-d", aliases = { "--database-dir" }, required = false, // if given, overrides the default & config
			usage = "path for database files")
	private File _databasePath;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();
	
	// Constructor
	public Test_Template() {
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public File get_databasePath() {
		return _databasePath;
	}
	
	public static void main(String[] args) throws CmdLineException {

		StringBuffer logMessage = new StringBuffer()
				.append("-------------------------------------\n")
				.append("Test application template\n")
				.append("-------------------------------------\n")
				;
		System.out.println(logMessage);
		
		//=================================================================
		// FIRST, get config file settings
		//=================================================================
		try {
			
			AppIO.parseConfig();
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		//=================================================================
		// SECOND, parse command line and override configs
		//=================================================================
		Test_Template theTestObject = new Test_Template();
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		
		AppIO.putWorkingDirectoryTree(
				Tuple.of(AppIO.FoldersEnum.DATABASE_DIR,databaseDirectoryAbsolutePath));
		
		logMessage.setLength(0); // clearing the buffer
		logMessage
			.append("\n~ REPORT ~\n\n")
			.append(AppIO.reportAll());
		System.out.println(logMessage);
		
		//=================================================================
		// THIRD, initialize the directory tree, i.e. create all the folders
		//=================================================================
		AppIO.initWorkingDirectoryTree();
		
		AppIO.customizeAmountOutput();
		//=================================================================
		// Read data
		//=================================================================
		try {

			logMessage.setLength(0); // clearing the buffer
			logMessage
				.append("\nReading data from file ")
					.append(AppIO.getInputFilenameFullPath())
				;
			System.out.println(logMessage);
			
			AppIO.getInputOutputTree().importFromXML(
					AppIO.getInputFilenameFullPath(), "", ""
					);
			
			logMessage.setLength(0); // clearing the buffer
			logMessage
				.append("\nAltitude: ")
					.append(AppIO.getInputOutputTree().getAltitude())
				.append("\nMach number: ")
					.append(AppIO.getInputOutputTree().getMachNumber())
				.append("\nAlpha: ")
					.append(AppIO.getInputOutputTree().getAlpha())
				.append("\nSweep LE: ")
					.append(AppIO.getInputOutputTree().getSweepLE())
				.append("\nNo. points in semispan: ")
					.append(AppIO.getInputOutputTree().getNumberOfPointSemispan())
				;
			
			System.out.println(logMessage);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		//=================================================================
		// execute an external command, and pass parameters
		//=================================================================
		// http://stackoverflow.com/questions/13991007/execute-external-program-in-java
		// http://www.rgagnon.com/javadetails/java-0014.html
		
		try {
		    List<String> command = new ArrayList<String>();
		    command.add(System.getenv("windir") +"\\system32\\"+"tree.com");
		    command.add("/A");
//		    command.add("dir");
//		    command.add("/D");

		    ProcessBuilder builder = new ProcessBuilder(command);
		    Map<String, String> environ = builder.environment();
		    builder.directory(
		    		new File(
		    				// System.getenv("temp")
		    				AppIO.getDirectory(AppIO.FoldersEnum.CURRENT_DIR)
		    			));

		    System.out.println("Directory: " + 
		    		// System.getenv("temp") 
		    		AppIO.getDirectory(AppIO.FoldersEnum.CURRENT_DIR)
		    );
		    System.out.println(
		    		"Command>" +
		    			// command.stream().collect(Collectors.joining())
		    			String.join(" ", command)
		    );
		    
		    final Process process = builder.start();
		    InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line;
		    while ((line = br.readLine()) != null) {
		      System.out.println(line);
		    }
		    System.out.println("Program terminated!");	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	} // end-of-main

}
