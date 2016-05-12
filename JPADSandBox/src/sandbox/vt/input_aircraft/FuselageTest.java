package sandbox.vt.input_aircraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.fuselage.vt.Fuselage;
import aircraft.components.liftingSurface.adm.LiftingSurface;
import sandbox.adm.liftingsurface2.WingTest;
import standaloneutils.JPADXmlReader;

public class FuselageTest {

	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public FuselageTest() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public File getInputFile() {
		return _inputFile;
	}

	public static void main(String[] args) throws CmdLineException, IOException {
		System.out.println("--------------");
		System.out.println("Fuselage test");
		System.out.println("--------------");

		FuselageTest theTestObject = new FuselageTest();

		theTestObject.theCmdLineParser.parseArgument(args);

		String pathToXML = theTestObject.getInputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);

		System.out.println("--------------");

		Fuselage fuselage = Fuselage.importFromXML(pathToXML);
		fuselage.calculateGeometry(30);

		System.out.println("The fuselage ...");

		System.out.println(fuselage);

	}
}
