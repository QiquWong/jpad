package sandbox.adm.liftingsurface2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.adm.LiftingSurfacePanel;
import standaloneutils.JPADXmlReader;

public class LiftingSurfacePanelTest_01 {

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

	public LiftingSurfacePanelTest_01() {
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
		System.out.println("Airfoil test");
		System.out.println("--------------");

		LiftingSurfacePanelTest_01 theTestObject = new LiftingSurfacePanelTest_01();
		
		theTestObject.theCmdLineParser.parseArgument(args);
		
		String pathToXML = theTestObject.getInputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);
		
		String dirAirfoil = theTestObject.getAirfoilDirectory().getCanonicalPath(); 
		System.out.println("AIRFOILS ===> " + dirAirfoil);

		System.out.println("--------------");
		
		LiftingSurfacePanel panel = LiftingSurfacePanel.importFromXML(pathToXML, dirAirfoil);
		System.out.println(panel);


	}

}
