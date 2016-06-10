package sandbox2.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.Configuration;
import javafx.application.Application;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

class MyArgumentConfiguration {
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

public class ConfigurationTest extends Application {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static Configuration theConfiguration;

	//-------------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception {

		//--------------------------------------------------
		// get the wing object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the configuration object ...");

		Configuration configuration = ConfigurationTest.theConfiguration;
		if (configuration == null) {
			System.out.println("configuration object null, returning.");
			return;
		}

		System.out.println("The configuration ...");
		System.out.println(configuration);

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
		System.out.println("Configuration test");
		System.out.println("--------------");

		MyArgumentConfiguration va = new MyArgumentConfiguration();
		ConfigurationTest.theCmdLineParser = new CmdLineParser(va);

		// populate the configuration static object in the class
		// before launching the application thread (launch --> start ...)
		try {
			ConfigurationTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			// This configuration static object is available in the scope of
			// the Application.start method
				
			// read Configuration from xml ...
			theConfiguration = Configuration.importFromXML(pathToXML);

			System.out.println("The Configuration ...");
			System.out.println(ConfigurationTest.theConfiguration.toString());

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			HorizontalTailTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    
	}
}