package it.unina.daf.jpad.testtemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
			
//			StringBuilder message = new StringBuilder()
//					//.append("-------------------------------------\n")
//					.append("Inputs read from dir: " + AppIO.getInputDirectory() +"\n")
//					.append("Databases read from dir: " 
//							+ AppIO.getDirectory(AppIO.FoldersEnum.DATABASE_DIR) +"\n")
//					.append("Outputs written in dir: " + AppIO.getOutputDirectory() +"\n")
//					.append("Output file name: " + AppIO.getOutputFilename() +"\n")
//					.append("Output file full path: " + AppIO.getOutputFilenameFullPath() +"\n")
//					.append("Output charts written in dir: " + AppIO.getOutputChartDirectory() +"\n")
//					.append("-------------------------------------\n")
//					;
//			System.out.println(message);
			
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
	}

}
