package sandbox2.mr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import configuration.MyConfiguration;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;


public class Test_Stability{

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;
	
	@Argument
	private List<String> arguments = new ArrayList<String>();
	
	public Test_Stability (){
		theCmdLineParser = new CmdLineParser(this);
	}

	//------------------------------------------------------------------------------------------
	// MAIN:
	public static void main(String[] args) throws CmdLineException, ParserConfigurationException, InstantiationException, IllegalAccessException {
		
		System.out.println("--------------");
		System.out.println("Stability analysis.");
		System.out.println("--------------");
		
		Test_Stability theTestObject = new Test_Stability();
		theTestObject.theCmdLineParser.parseArgument(args);
		
		// Set the folders tree
				MyConfiguration.initWorkingDirectoryTree();

				String pathToXML = theTestObject.get_inputFile().getAbsolutePath();
				String filenameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)+"Stability_Output"+ "Stability_Output.xml"; 
				

		System.out.println("INPUT FILE ===> " + pathToXML);
		
		System.out.println("--------------");
		
		Amount<Angle> alphaTry = Amount.valueOf(13, NonSI.DEGREE_ANGLE);
		System.out.println(" alpha " + alphaTry);
		System.out.println(alphaTry.opposite());
		System.out.println(alphaTry);
		
		Amount<Angle> alphadeg = alphaTry.to(NonSI.DEGREE_ANGLE);
		System.out.println(" alpha " + alphadeg);
		double alphaDeg = alphaTry.doubleValue(SI.RADIAN);
		System.out.println(" alpha " + alphaDeg);
		
		System.out.println("alpha " + alphaTry);
		
		List<Amount<Angle>> metri = new ArrayList<>();
		metri.add(0, alphaTry);
		
		metri.add(1, alphadeg);
		
		List<Double> metrid = new ArrayList<>();
		metrid.add(0,1.0);
		
		metrid.add(1, 2.1);
		
		
		System.out.println(" array " + metri);
		
		System.out.println(" array " + metrid);
		
		StabilityExecutableManager theCalculator = new StabilityExecutableManager();
		ReaderWriter theReader = new ReaderWriter();
		theReader.importFromXML(
				pathToXML, 
				theCalculator
				);

		// output folder
		String outputPathPartial = JPADStaticWriteUtils.createNewFolder(
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + 
				File.separator +
				"Longitudinal Static Stability Output"+
			    File.separator 
				);
		
		String outputPath = JPADStaticWriteUtils.createNewFolder(
				outputPathPartial + 
				File.separator +
				theCalculator.getAircraftName()+
			    File.separator 
				);
		
		String outputChartPath = JPADStaticWriteUtils.createNewFolder(
				outputPath +
				File.separator +
				"Wing Charts"+
				File.separator
				);
		
		String outputChartPathDist = JPADStaticWriteUtils.createNewFolder(
				outputChartPath +
				File.separator +
				"Distributions"+
				File.separator
				);
		
		theCalculator.initializeData();
		theCalculator.initializeCalculators();
		theCalculator.calculateWingLiftCharacteristics();
		if (theCalculator.getTheCondition() == ConditionEnum.TAKE_OFF || theCalculator.getTheCondition() == ConditionEnum.LANDING){
			theCalculator.calculateWingHighLiftCharacteristics();
		}
		theCalculator.initializeDownwashArray();
		theCalculator.initializeHTailArray();
		theCalculator.calculateHTailLiftCharacteristics();
		theCalculator.calculateHTailLiftCharacteristicsWithElevatorDeflection();
		theCalculator.calculateWingFuselageLiftCharacterstics();
		theCalculator.initializeDragArray();
		theCalculator.calculateWingDragCharacterstics();
		theCalculator.calculateHTailDragCharacterstics();
		theCalculator.calculateWingXAC();
		theCalculator.calculateHTailXAC();
		theCalculator.calculateFuselageMomentCharacterstics();
		theCalculator.calculateWingBodyXAC();
		theCalculator.calculateWingMomentCharacterstics();
		theCalculator.calculateHtailMomentCharacterstics();
		//...
		theCalculator.calculateDistributions();
		theCalculator.printAllData();
		String sb = theCalculator.printAllResults();
		System.out.println(sb);
		theCalculator.plot(outputChartPath, outputChartPathDist);
		// read
		// initialize
		// print
		// calculator

//		InputOutputTree input = new InputOutputTree();
//		
//		ReaderWriterWing theReader = new ReaderWriterWing();
//		theReader.importFromXML(pathToXML,databaseDirectoryAbsolutePath , "Aerodynamic_Database_Ultimate.h5", input);
//		Calculator.calculateModifiedStallPath(input);
		
		//WingAerodynamicCalc.calculateAll(theReader.getInput());
		//theReader.writeToXML(filenameWithPathAndExt, input);

	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	public File get_inputFile() {
		return _inputFile;
	}

	public void set_inputFile(File _inputFile) {
		this._inputFile = _inputFile;
	}
	
}

