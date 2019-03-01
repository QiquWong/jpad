package sandbox2.atsi.analyses.test0;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.measure.unit.SI;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

// Needs library: args4j
class MyArgumentsTestFuselageData {
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

public class TestFuselageData {

	public static CmdLineParser theCmdLineParser;

	public static void main(String[] args) {

		MyArgumentsTestFuselageData va = new MyArgumentsTestFuselageData();

		TestFuselageData.theCmdLineParser = new CmdLineParser(va);
		
		try {
			
			TestFuselageData.theCmdLineParser.parseArgument(args);
			
			//Path to XML mi da tutto il percorso 
			String pathToXML = va.getInputFile().getAbsolutePath();


			JPADXmlReader reader =new JPADXmlReader(pathToXML);

			//How to acquire the fuselage file
			String FuselageFile = reader.getXMLAttributeByPath(
					"/jpad_config/aircraft/fuselages/fuselage", 
					"file" );

			System.out.println("The file found is:" + FuselageFile);

			String fuselagePath = "in/Template_Aircraft/fuselages/" + FuselageFile;	

			File fuselageFile = new File(fuselagePath);

			Fuselage fuselage = Fuselage.importFromXML(fuselageFile.getAbsolutePath());

			System.out.println(fuselage /* .toString() */ );
			System.out.println("N° of decks: " + fuselage.getDeckNumber());

		} catch (CmdLineException e) {
			e.printStackTrace();
		}

	}

}
