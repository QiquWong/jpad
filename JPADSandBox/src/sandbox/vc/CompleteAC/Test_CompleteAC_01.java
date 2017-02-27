package sandbox.vc.CompleteAC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.io.Files;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FoldersEnum;
import javafx.util.Pair;
import standaloneutils.JPADXmlReader;
import writers.JPADDataWriter;

public class Test_CompleteAC_01 {
	
	//------------------------------------------------------------------------------------------
		// VARIABLE DECLARATION:
		@Option(name = "-i", aliases = { "--input" }, required = false,
				usage = "my input file")
		private File _inputFile;
		
		@Option(name = "-d", aliases = { "--database-path" }, required = false,
				usage = "path for database files")
		private File _databasePath;

		// declaration necessary for Concrete Object usage
		public CmdLineParser theCmdLineParser;
		public JPADXmlReader reader;

		@Argument
		private List<String> arguments = new ArrayList<String>();

		public Test_CompleteAC_01 (){
			theCmdLineParser = new CmdLineParser(this);
		}

	public static void main(String[] args) throws CmdLineException {

		Test_CompleteAC_01 theTestObject = new Test_CompleteAC_01();
		
		theTestObject.theCmdLineParser.parseArgument(args);
		String fileNameWithPathAndExt = theTestObject.get_inputFile().getAbsolutePath();
		String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		
		System.out.println("File name: " + fileNameWithPathAndExt);
		System.out.println("Database path: " + databaseDirectoryAbsolutePath);
		
		
		// Set the folders tree
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
											     MyConfiguration.inputDirectory, 
												 MyConfiguration.outputDirectory,
												 databaseDirectoryAbsolutePath); 
		
		File inputFile = new File(fileNameWithPathAndExt);
		String inputFileName = new String(inputFile.getName());

		if (!inputFile.exists()) {
			System.out.println("Input file " + fileNameWithPathAndExt + " not found! Terminating.");
			return;
		}
		if (inputFile.isDirectory()) {
			System.out.println("Input string " + fileNameWithPathAndExt + " is not a file. Terminating.");
			return;
		}

		System.out.println("Input file found. Running ...");

		String outputFileNameWithPathAndExt = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator + 
				Files.getNameWithoutExtension(inputFileName) + "Out.xml";
		
		
		//--------------------- Set geometry -----------------------------------
		
		// Fuselage
		
		// Wing
		
		// [...]
		
		//--------------------- Analysis -----------------------------------------
		
		// Define the aircraft
//		JPADXmlReader aircraft = new JPADXmlReader(inputFileName) ;
//		Aircraft aircraft = new Aircraft(AircraftEnum.ATR72);
//		aircraft.setName("ATR72");
		Aircraft aircraft = new Aircraft(AircraftEnum.IRON);
		aircraft.setName("IRON");
		LiftingSurface2Panels theWing = aircraft.get_wing();

		// Set the operating conditions
		OperatingConditions operatingConditions = new OperatingConditions();
		operatingConditions.set_altitude(Amount.valueOf(9144., SI.METER));
		operatingConditions.set_machCurrent(0.62);
		
		// Define the Analysis Manager
		ACAnalysisManager theAnalysis = new ACAnalysisManager(operatingConditions);
		theAnalysis.updateGeometry(aircraft);
		
		// --------------------------------------------------------------
		// Define an LSAerodynamicsManager Object
		// --------------------------------------------------------------
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager (
				operatingConditions,
				theWing,
				aircraft
				);
		
		
		// --------------------------------------------------------------
		// Setup database(s)
		// --------------------------------------------------------------
		theLSAnalysis.setDatabaseReaders(
				new Pair<DatabaseReaderEnum, String>(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair<DatabaseReaderEnum, String>(DatabaseReaderEnum.HIGHLIFT,
						"HighLiftDatabase.h5")
				);

		// Define the analysis
		theAnalysis.doAnalysis(aircraft, 
							AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, 
							AnalysisTypeEnum.BALANCE,
							AnalysisTypeEnum.WEIGHTS,
							AnalysisTypeEnum.PERFORMANCE, 
							AnalysisTypeEnum.COSTS
							);
		
		
		JPADDataWriter _theWriteUtilities = new JPADDataWriter(
				operatingConditions,
				aircraft, 
				theAnalysis);
		
	}
	
	
	

	public File get_inputFile() {
		return _inputFile;
	}

	public File get_databasePath() {
		return _databasePath;
	}
	
	
	
	

}
