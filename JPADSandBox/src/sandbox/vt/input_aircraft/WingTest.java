package sandbox.vt.input_aircraft;

import static java.lang.Math.toRadians;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.OperatingConditions;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import aircraft.components.liftingSurface.LiftingSurface2Panels.LiftingSurface2PanelsBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.DatabaseReaderEnum;
import javafx.util.Pair;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;

public class WingTest {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION: 
	@Option(name = "-i", aliases = { "--input" }, required = false,
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

	//BUILDER:
	public WingTest() {
		theCmdLineParser = new CmdLineParser(this);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, CmdLineException, IOException {

		WingTest main = new WingTest();
		
		// Arguments check
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		main.theCmdLineParser.parseArgument(args);
		
		String pathToXML = main.getInputFile().getAbsolutePath();
		System.out.println("INPUT ===> " + pathToXML);

		String dirAirfoil = main.getAirfoilDirectory().getCanonicalPath();
		System.out.println("AIRFOILS ===> " + dirAirfoil);
		
		LiftingSurface2Panels theWing = new LiftingSurface2PanelsBuilder("MyWing", ComponentEnum.WING)
				.liftingSurface2PanelsCreator(
						LiftingSurfaceCreator.importFromXML(pathToXML, dirAirfoil)
						)
				.build();
		theWing.calculateGeometry(40);
		System.out.println("The wing ...");
		System.out.println(theWing.getLiftingSurfaceCreator().toString());
		System.out.println("Details on panel discretization ...");
		theWing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();
		
		// Center of Gravity
		double xCgLocal= 1.5; // meter 
		double yCgLocal= 0;
		double zCgLocal= 0;
		
		double xAw = 11.0; //meter 
		double yAw = 0.0;
		double zAw = 1.6;
		
		CenterOfGravity cg = new CenterOfGravity(
				Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
				Amount.valueOf(yCgLocal, SI.METER),
				Amount.valueOf(zCgLocal, SI.METER),
				Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
				Amount.valueOf(yAw, SI.METER),
				Amount.valueOf(zAw, SI.METER),
				Amount.valueOf(0.0, SI.METER),// origin of BRF
				Amount.valueOf(0.0, SI.METER),
				Amount.valueOf(0.0, SI.METER)
				);

		cg.calculateCGinBRF();
		theWing.set_cg(cg);
		theWing.set_aspectRatio(6.0);

		// Default operating conditions
		OperatingConditions theOperatingConditions = new OperatingConditions();		
		theOperatingConditions.set_alphaCurrent(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Operating condition");
		System.out.println("-----------------------------------------------------");
		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		System.out.println("\tAltitude: " + theOperatingConditions.get_altitude());
		System.out.println("\tAlpha " + theOperatingConditions.get_alphaCurrent().getEstimatedValue());
		System.out.println("----------------------");


		// allocate manager
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
				theOperatingConditions,
				theWing
				);

		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		// Setup database(s)	
		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC, 
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);
		
		// -----------------------------------------------------------------------
		// Calculate CL 
		// -----------------------------------------------------------------------

		System.out.println("---------------------------");
		System.out.println("\nEvaluating CL "); 
		System.out.println("\n---------------------------");

		Amount<Angle> alpha = Amount.valueOf(toRadians(14.), SI.RADIAN);
		LSAerodynamicsManager.CalcCLAtAlpha theCLCalculator = theLSAnalysis.new CalcCLAtAlpha();
		double CL = theCLCalculator.nasaBlackwellCompleteCurve(alpha);

		System.out.println(" At alpha " + alpha.to(NonSI.DEGREE_ANGLE) + " CL = " + CL);

		theLSAnalysis.plotCLvsAlphaCurve();

		System.out.println("\nDONE "); 

	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File getInputFile() {
		return _inputFile;
	}
	
	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}
}
