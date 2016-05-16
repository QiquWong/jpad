package sandbox.vt.ExecutableTakeOff;

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
import sandbox.vt.ExecutableHighLiftDevices.HighLiftDevicesCalc;
import sandbox.vt.ExecutableHighLiftDevices.Test_HighLiftDevicesCalc_01;
import standaloneutils.JPADXmlReader;

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
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException {

		System.out.println("--------------");
		System.out.println("Take-off executable :: test");
		System.out.println("--------------");


		Test_TakeOffCalc_01 theTestObject = new Test_TakeOffCalc_01();
		theTestObject.theCmdLineParser.parseArgument(args);

		String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);

		System.out.println("--------------");

		TakeOffManager.importFromXML(pathToXML);
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
