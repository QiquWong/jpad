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
import standaloneutils.JPADXmlReader;

public class Test_Template {
	
	@Option(name = "-d", aliases = { "--database-path" }, required = false, // change ti true if needed
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

		//=================================================================
		// Get config file settings
		//=================================================================
		try {
			
			AppIO.parseConfig();
			
			System.out.println("Inputs read from dir: " + AppIO.getInputDirectory());
			System.out.println("Outputs written in dir: " + AppIO.getOutputDirectory());
			System.out.println("Output file name: " + AppIO.getOutputFilename());
			System.out.println("Output file full path: " + AppIO.getOutputFilenameFullPath());
			System.out.println("Output charts written in dir: " + AppIO.getOutputChartDirectory());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Test_Template theTestObject = new Test_Template();
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		// TODO use setter
		// AppIO.setDatabaseDirectory(databaseDirectoryAbsolutePath);
	}

}
