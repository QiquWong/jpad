package sandbox2.vc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import analyses.costs.Costs;
import standaloneutils.JPADXmlReader;

class MyArgumentSystems {
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

public class CostsTest {
	
	// declaration necessary for Concrete Object usage
		public static CmdLineParser theCmdLineParser;
		public static JPADXmlReader reader;

		//-------------------------------------------------------------

		public static Costs theCosts;

		//-------------------------------------------------------------

	public static void main(String[] args) {
		
		System.out.println("--------------");
		System.out.println("Costs test");
		System.out.println("--------------");

		MyArgumentSystems va = new MyArgumentSystems();
		CostsTest.theCmdLineParser = new CmdLineParser(va);

		// populate the configuration static object in the class
		// before launching the application thread (launch --> start ...)
		try {
			CostsTest.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			System.out.println("--------------");

			// read data from XML
			theCosts = Costs.importFromXML(pathToXML);
			
//			// default Configuration ...
//			theCosts = new CostsBuilder(
//					"JPAD Test Costs DAF - 2016")
//					.build();
			
			System.out.println("The Costs ...");
			System.out.println(CostsTest.theCosts.toString());

		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			CostsTest.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;

		}

	}

}
