package sandbox.mr.ExecutableWing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import standaloneutils.JPADXmlReader;

public class Test_Executable_Wing {

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
	
	public Test_Executable_Wing (){
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException {
		
		System.out.println("--------------");
		System.out.println("Wing aerodynamic Analysis");
		System.out.println("--------------");
		
		Test_Executable_Wing theTestObject = new Test_Executable_Wing();
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);
		
		System.out.println("--------------");
		
		ReaderWriterWing.importFromXML(pathToXML);
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
