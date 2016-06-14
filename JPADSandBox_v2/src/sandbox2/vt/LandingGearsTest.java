package sandbox2.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.LandingGears;
import aircraft.components.Systems;
import configuration.enumerations.AircraftEnum;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentLandingGears {
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

public class LandingGearsTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LandingGears theLandingGears;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the landing gears object ...");

		Systems systems = SystemsTest.theSystems;
		if (systems == null) {
			System.out.println("landing gears object null, returning.");
			return;
		}

		System.out.println("The landing gears ...");
		System.out.println(systems);

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
		System.out.println("Landing gears test");
		System.out.println("--------------");

		MyArgumentLandingGears va = new MyArgumentLandingGears();
		LandingGearsTest.theCmdLineParser = new CmdLineParser(va);

		// populate the configuration static object in the class
		// before launching the application thread (launch --> start ...)
		try {
			LandingGearsTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			// This configuration static object is available in the scope of
			// the Application.start method
				
			// read Configuration from xml ...
			theLandingGears = LandingGears.importFromXML(pathToXML);

//			// default Configuration ...
//			theLandingGears = new LandingGear.LandingGearBuilder(
//					"ATR-72 landing gears",
//					AircraftEnum.ATR72)
//					.build();
			
			System.out.println("The landing gears ...");
			System.out.println(LandingGearsTest.theLandingGears.toString());

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			LandingGearsTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    
	}
}