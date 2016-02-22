package sandbox.vc;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.DatabaseReaderEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import javafx.util.Pair;


public class Test_VC_complete_AC {
	
	private static AerodynamicDatabaseReader aeroDatabaseReader;
	private static Object importFile;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
	// Initilize working directories
	MyConfiguration.initWorkingDirectoryTree();	
	
	// Define the aircraft
	// TODO: Have to deprecate Aircraft.createDefaultAircraft() ->  Aircraft.createDefaultAircraft(String aircraftName)
	Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72);
	LiftingSurface theWing = aircraft.get_wing();
	
	// Import aircraft from .xml file
//	importFile = MyConfiguration.inputDirectory + aircraftName;
//	importUsingCustomXML(aircraftName);
	
	// Set the operating condition
	OperatingConditions theCondition = new OperatingConditions();
	
	theCondition.set_machCurrent(0.4);
	theCondition.set_altitude(Amount.valueOf(15000, NonSI.FOOT));
	
	// Define the Analysis Manager
	ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
	theAnalysis.updateGeometry(aircraft);
	
	// --------------------------------------------------------------
	// Define an LSAerodynamicsManager Object
	// --------------------------------------------------------------
	LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager (
			theCondition,
			theWing,
			aircraft
			);
	
//	// Database(s)
//	String aerodynamicDatabaseName = "Aerodynamic_Database_Ultimate.h5"; 
//	aeroDatabaseReader = new AerodynamicDatabaseReader(MyConfiguration.databaseFolderName, aerodynamicDatabaseName);
//	aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
	
	// --------------------------------------------------------------
	// Setup database(s)
	// --------------------------------------------------------------
	theLSAnalysis.setDatabaseReaders(
			new Pair(DatabaseReaderEnum.AERODYNAMIC,
					"Aerodynamic_Database_Ultimate.h5"),
			new Pair(DatabaseReaderEnum.HIGHLIFT,
					"HighLiftDatabase.h5")
			);

	// Define the analysis
	theAnalysis.doAnalysis(aircraft, 
//						AnalysisTypeEnum.AERODYNAMIC, 
//						AnalysisTypeEnum.BALANCE,
						AnalysisTypeEnum.WEIGHTS //,
//						AnalysisTypeEnum.PERFORMANCES //, 
//						AnalysisTypeEnum.COSTS
						);		
	// Write results
	/* Lorenzo sandbox:
	  		_theWriteUtilities = new MyDataWriter(
	 
			GlobalData.getTheCurrentOperatingConditions(),
			GlobalData.getTheCurrentAircraft(), _theAnalysis);

	_theWriteCharts = new MyChartWriter(GlobalData.getTheCurrentAircraft());
	_theWriteCharts.createCharts();

	// +++++++++++++++++++++++++++++++++++++++++++++++
	// STATIC FUNCTIONS - TO BE CALLED BEFORE WRITING CUSTOM XML FILES
	MyWriteUtils.buildXmlTree();

	// Export everything to file
	_theWriteUtilities.exportToXMLfile(exportFile + ".xml");
	_theWriteUtilities.exportToXLSfile(exportFile + ".xls");
	*/	
	
	} // end Main method

}
