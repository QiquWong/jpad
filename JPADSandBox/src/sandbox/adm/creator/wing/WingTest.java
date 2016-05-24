package sandbox.adm.creator.wing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.LiftingSurface2Panels;
import aircraft.components.liftingSurface.Wing;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import configuration.enumerations.ComponentEnum;
import standaloneutils.JPADXmlReader;

public class WingTest {

	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public WingTest() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public File getInputFile() {
		return _inputFile;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	public static void main(String[] args) throws CmdLineException, IOException {
		System.out.println("--------------");
		System.out.println("Wing test");
		System.out.println("--------------");

		WingTest theTestObject = new WingTest();

		theTestObject.theCmdLineParser.parseArgument(args);

		String pathToXML = theTestObject.getInputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);

		String dirAirfoil = theTestObject.getAirfoilDirectory().getCanonicalPath();
		System.out.println("AIRFOILS ===> " + dirAirfoil);

		System.out.println("--------------");

		LiftingSurface2Panels wing = new LiftingSurface2Panels(
				pathToXML, dirAirfoil,
				"Wing", // name
				"Test wing ", // description
				11.0, 0.0, 1.6,
				ComponentEnum.WING
				);
		
		wing.getTheLiftingSurfaceCreator().calculateGeometry(30);
		System.out.println("The wing ...");
		System.out.println(wing.getTheLiftingSurfaceCreator());
		
		System.out.println("Details on panel discretization ...");
		wing.getTheLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();
		

	}

}
