package it.unina.daf.test.highliftdeviceseffects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;

public class TestHighLiftDevicesCalc_v2 {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-d", aliases = { "--database-path" }, required = true,
			usage = "path for database files")
	private File _databasePath;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;
	
	@Argument
	private List<String> arguments = new ArrayList<String>();
	
	public TestHighLiftDevicesCalc_v2 (){
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException {
		
		System.out.println("--------------");
		System.out.println("High lift devices executable :: test");
		System.out.println("--------------");
		
		
		TestHighLiftDevicesCalc_v2 theTestObject = new TestHighLiftDevicesCalc_v2();
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String databaseDirectoryAbsolutePath = theTestObject.getDatabasePath().getAbsolutePath();
		
		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
				MyConfiguration.inputDirectory, 
				MyConfiguration.outputDirectory,
				databaseDirectoryAbsolutePath); // coming from main arguments

		String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
		String filenameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + 
				"HighLift_Output.xml"; 
		System.out.println("INPUT ===> " + pathToXML);
		
		System.out.println("--------------");
		
		HighLiftDevicesCalc.importFromXML(pathToXML);
		HighLiftDevicesCalc.executeStandAloneHighLiftDevicesCalc(
				databaseDirectoryAbsolutePath,
				"HighLiftDatabase.h5",
				"Aerodynamic_Database_Ultimate.h5"
				);
		HighLiftDevicesCalc.writeToXML(HighLiftDevicesCalc.getOutput(), filenameWithPathAndExt);
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	public File get_inputFile() {
		return _inputFile;
	}

	public void set_inputFile(File _inputFile) {
		this._inputFile = _inputFile;
	}

	public File getDatabasePath() {
		return _databasePath;
	}

	public void set_databasePath(File _databasePath) {
		this._databasePath = _databasePath;
	}
}
