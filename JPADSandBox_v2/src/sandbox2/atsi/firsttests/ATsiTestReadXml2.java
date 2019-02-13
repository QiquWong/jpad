package sandbox2.atsi.firsttests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.measure.unit.SI;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

// Needs library: args4j
class MyArgumentsAtsiTestReadXml2 {
	@Option(name = "-f", aliases = { "--file" }, required = true,
			usage = "Aircraft file to be read")
	private File _inputFile;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
}

public class ATsiTestReadXml2 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	public static void main(String[] args) {

		// creates an object able to get values from a "variable argument" input string
		MyArgumentsAtsiTestReadXml2 va = new MyArgumentsAtsiTestReadXml2();
		// creates an object that parses the arguments (prepares va)
		ATsiTestReadXml2.theCmdLineParser = new CmdLineParser(va);

		try {
			// parse step, i.e. fills va
			ATsiTestReadXml2.theCmdLineParser.parseArgument(args);
			
			// now get the file name, i.e. the string from the _inputFile
			String pathToXML = va.getInputFile().getAbsolutePath();
			
			// creates the aircraft reader
			JPADXmlReader reader = new JPADXmlReader(pathToXML);
			
			// traverse XML structure (DOM) to acquire wing file name
			String wingFileName = reader.getXMLAttributeByPath(
					"/jpad_config/aircraft/lifting_surfaces/wing", 
					"file");
			
			System.out.println("File containing aircraft data: " + wingFileName);
			
			// now open wing file
			String wingFilePath = "in/Template_Aircraft/lifting_surfaces/"
					+ wingFileName;
			
			File wingFile = new File(wingFilePath);
			System.out.println("Wing file complete path: " + wingFile.getAbsolutePath());
			System.out.println("Wing file exists? " + wingFile.exists());
			
			System.out.println("***--------------------------------***");
			// let's read the file
			// JPADXmlReader wingReader = new JPADXmlReader(wingFile.getAbsolutePath());
			
			// let's instantiate a LiftingSurface object
			LiftingSurface wing = LiftingSurface.importFromXML(
					ComponentEnum.WING,
					wingFile.getAbsolutePath(),
					"in/Template_Aircraft/lifting_surfaces/airfoils");
			
			//System.out.println("Is wing null? " + (wing == null));
			
			System.out.println("N. of panels: " + wing.getPanels().size());

			System.out.println("***--------------------------------***");
			
			// print out the whole data structure
			
			System.out.println(wing /* .toString() */ );


		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		
	}
	
}
