package sandbox2.vt.analyses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import analyses.OperatingConditions;
import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentsOperatingConditions {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
}

public class OperatingConditionsTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static OperatingConditions theOperatingConditions;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the aircraft object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the operating conditions object ...");

		OperatingConditions operatingConditions = OperatingConditionsTest.theOperatingConditions;
		if (operatingConditions == null) {
			System.out.println("operating conditions object null, returning.");
			return;
		}

		System.out.println("The Operating conditions ... ");
		theOperatingConditions.toString();
		
	}; // end-of-Runnable

	/**
	 * Main
	 *
	 * @param args
	 * @throws InvalidFormatException 
	 */
	public static void main(String[] args) throws InvalidFormatException {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("-------------------");
		System.out.println("Operating conditons test");
		System.out.println("-------------------");

		MyArgumentsOperatingConditions va = new MyArgumentsOperatingConditions();
		OperatingConditionsTest.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			OperatingConditionsTest.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			// reading from xml ... 
			theOperatingConditions = OperatingConditions.importFromXML(pathToXML);
			System.out.println(theOperatingConditions.toString());
			
		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			OperatingConditionsTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

	}

}

