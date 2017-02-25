package sandbox2.vt.ExecutableTakeOff_v2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public class Test_TakeOffCalc_01 {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public Test_TakeOffCalc_01 (){
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException, InstantiationException, IllegalAccessException, InvalidFormatException, IOException {

		System.out.println("--------------");
		System.out.println("Take-off executable :: test");
		System.out.println("--------------");


		Test_TakeOffCalc_01 theTestObject = new Test_TakeOffCalc_01();
		theTestObject.theCmdLineParser.parseArgument(args);

		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(
				MyConfiguration.currentDirectoryString,
				MyConfiguration.inputDirectory, 
				MyConfiguration.outputDirectory);
		
		String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) 
				+ "Take-Off_executable_v2"
				+ File.separator
				);
		String filenameWithPathAndExt = performanceFolderPath + "TakeOff_Output"; 
		
		System.out.println("INPUT ===> " + pathToXML);

		System.out.println("--------------");

		TakeOffManager.importFromXML(pathToXML);
		TakeOffManager.executeStandAloneTakeOffCalculator(filenameWithPathAndExt);
		TakeOffManager.writeAllOutput(TakeOffManager.getOutput(), filenameWithPathAndExt);
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public File get_inputFile() {
		return _inputFile;
	}

	public void set_inputFile(File _inputFile) {
		this._inputFile = _inputFile;
	}

}
