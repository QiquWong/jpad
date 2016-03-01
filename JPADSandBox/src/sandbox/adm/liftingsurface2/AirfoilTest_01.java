package sandbox.adm.liftingsurface2;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.adm.Airfoil;
import aircraft.components.liftingSurface.adm.Airfoil.AirfoilBuilder;
import configuration.enumerations.AirfoilTypeEnum;
import sandbox.vt.TakeOff_Test.TakeOff_Landing_Test_TP;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class AirfoilTest_01 {

	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public AirfoilTest_01() {
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public File getInputFile() {
		return _inputFile;
	}
	
	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {

		System.out.println("--------------");
		System.out.println("Airfoil test");
		System.out.println("--------------");

		AirfoilTest_01 theTestObject = new AirfoilTest_01();
		
		theTestObject.theCmdLineParser.parseArgument(args);
		String pathToXML = theTestObject.getInputFile().getAbsolutePath();

		Airfoil airfoil = Airfoil.importFromXML(pathToXML);
		System.out.println(airfoil);
		
	}

}
