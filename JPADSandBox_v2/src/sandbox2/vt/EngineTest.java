package sandbox2.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.powerplant.Engine;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineTypeEnum;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentEngine {
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

public class EngineTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Engine theEngine;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the engine object ...");

		Engine engine = EngineTest.theEngine;
		if (engine == null) {
			System.out.println("engine object null, returning.");
			return;
		}

		System.out.println("The engine ...");
		System.out.println(engine);

		}; // end-of-Runnable

	/**
	 * Main
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("--------------");
		System.out.println("Engine test");
		System.out.println("--------------");

		MyArgumentEngine va = new MyArgumentEngine();
		EngineTest.theCmdLineParser = new CmdLineParser(va);

		// populate the configuration static object in the class
		// before launching the application thread (launch --> start ...)
		try {
			EngineTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			// This configuration static object is available in the scope of
			// the Application.start method
				
			// read Engine from xml ...
//			theEngine = Engine.importFromXML(pathToXML);

			// default Engine ...
			theEngine = new Engine.EngineBuilder(
					"ATR-72 engine",
					EngineTypeEnum.TURBOPROP,
					AircraftEnum.ATR72)
					.build();
			
			System.out.println("The Engine ...");
			System.out.println(EngineTest.theEngine.toString());

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			EngineTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    
	}
}
