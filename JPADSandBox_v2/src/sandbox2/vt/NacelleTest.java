package sandbox2.vt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.nacelles.NacelleCreator;
import configuration.enumerations.AircraftEnum;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentNacelle {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-de", aliases = { "--dir-engines" }, required = true,
			usage = "engines directory path")
	private File _enginesDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getEnginesDirectory() {
		return _enginesDirectory;
	}
}

public class NacelleTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static NacelleCreator theNacelle;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the nacelle object ...");

		NacelleCreator nacelle = NacelleTest.theNacelle;
		if (nacelle == null) {
			System.out.println("nacelle object null, returning.");
			return;
		}

		System.out.println("The nacelle ...");
		System.out.println(nacelle);

		}; // end-of-Runnable

	/**
	 * Main
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("--------------");
		System.out.println("Nacelle test");
		System.out.println("--------------");

		MyArgumentNacelle va = new MyArgumentNacelle();
		NacelleTest.theCmdLineParser = new CmdLineParser(va);

		// populate the configuration static object in the class
		// before launching the application thread (launch --> start ...)
		try {
			NacelleTest.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String dirEngines = va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			System.out.println("--------------");

			// This configuration static object is available in the scope of
			// the Application.start method
				
			// read Engine from xml ...
			theNacelle = NacelleCreator.importFromXML(pathToXML, dirEngines);

			// default Engine ...
//			theNacelle = new NacelleCreator.NacelleCreatorBuilder(
//					"ATR-72 engine",
//					AircraftEnum.ATR72)
//					.build();
			
			System.out.println("The Nacelle ...");
			System.out.println(NacelleTest.theNacelle.toString());

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			NacelleTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    
	}
}