package sandbox.vc.agile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
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
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.performance.PayloadRangeCalc;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.cNbetaContributionsEnum;
import javafx.util.Pair;
import sandbox.vc.dirstab.DirStabCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class Test_AC_AGILE_02 {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-d", aliases = { "--database-path" }, required = true,
			usage = "path for database files")
	private File _databasePath;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public Test_AC_AGILE_02(){
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public static void main(String[] args) throws CmdLineException {
		
		// Allocate the main object
		Test_AC_AGILE_02 theTestObject = new Test_AC_AGILE_02();

		theTestObject.theCmdLineParser.parseArgument(args);
		
		// Initialize Aircraft with default parameters
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.B747_100B); 
		LiftingSurface theWing = aircraft.get_wing();
		
		OperatingConditions operatingConditions = new OperatingConditions();
		operatingConditions.set_altitude(Amount.valueOf(0.000, SI.METER));
		operatingConditions.set_machCurrent(0.78);
		// Mass - Weight
		Amount<Mass> MTOM = Amount.valueOf(25330, NonSI.POUND);
		aircraft.get_weights().set_MTOM(MTOM);
		Amount<Force> _MLW = MTOM.times(0.9).times(AtmosphereCalc.g0).to(SI.NEWTON);
		aircraft.get_weights().set_MLW(_MLW);
		// -------------------------------------------------------------------
		Amount<Length> range = Amount.valueOf(1890, NonSI.MILE);
		aircraft.get_performances().set_range(range);
		Amount<Mass> paxSingleMass = Amount.valueOf(225, NonSI.POUND);
		aircraft.get_weights().set_paxSingleMass(paxSingleMass);
		//----------------------------------------------------------------------
		// Geometry		
		Amount<Area> _surface = Amount.valueOf(76.33,SI.SQUARE_METRE);
		aircraft.get_exposedWing().set_surface(_surface);
		Amount<Length> _span = Amount.valueOf(27.6, SI.METER);
		aircraft.get_exposedWing().set_span(_span);
		Amount<Length> diam_C_MAX = Amount.valueOf(3, SI.METER);
		aircraft.get_fuselage().set_diam_C_MAX(diam_C_MAX );
		
		
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
//		theLSAnalysis.setDatabaseReaders(
//				new Pair(DatabaseReaderEnum.AERODYNAMIC,
//						"Aerodynamic_Database_Ultimate.h5"),
//				new Pair(DatabaseReaderEnum.HIGHLIFT,
//						"HighLiftDatabase.h5")
//				);

		// Define the analysis
		// TODO: Problem with doAnalysis->It takes some values from default aircraft. 
		theAnalysis.doAnalysis(aircraft, 
							AnalysisTypeEnum.WEIGHTS
							);
	}
	
	public File get_inputFile() {
		return _inputFile;
	}

	public File get_databasePath() {
		return _databasePath;
	}

}
