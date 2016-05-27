package sandbox.mr.ExecutableMeanAirfoil;

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

public class Test_Mean_Airfoil {

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

	public Test_Mean_Airfoil (){
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException, InstantiationException, IllegalAccessException {

		System.out.println("--------------");
		System.out.println("Mean Airfoil Calculator");
		System.out.println("--------------");

		Test_Mean_Airfoil theTestObject = new Test_Mean_Airfoil();
		theTestObject.theCmdLineParser.parseArgument(args);

		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(
				MyConfiguration.currentDirectoryString,
				MyConfiguration.inputDirectory, 
				MyConfiguration.outputDirectory);

		String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
		String filenameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) 
										+ "Mean_Airfoil_Output.xml"; 

		System.out.println("INPUT ===> " + pathToXML);

		System.out.println("--------------");

				ReaderWriter.importFromXML(pathToXML);
				MeanAirfoilCalc.executeStandAlone(ReaderWriter.getInputOutput());
				ReaderWriter.writeToXML(filenameWithPathAndExt);

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
