package sandbox.vc;

import java.util.HashMap;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public class Test_VC_complete_AC {
	
	private static String aircraftName = "ATR72		";
	private static AerodynamicDatabaseReader aeroDatabaseReader;
	private static Object importFile;

	public static void main(String[] args) {
		
	// Initilize working directories
	MyConfiguration.initWorkingDirectoryTree();	
	
	// Define the aircraft
	Aircraft aircraft = Aircraft.createDefaultAircraft();
	aircraft.set_name("ATR-72");
	
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
	
	// Database(s)
	String aerodynamicDatabaseName = "Aerodynamic_Database_Ultimate.h5"; 
	aeroDatabaseReader = new AerodynamicDatabaseReader(MyConfiguration.databaseFolderName, aerodynamicDatabaseName);
	aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);

	// Define the analysis
	theAnalysis.doAnalysis(aircraft, 
						AnalysisTypeEnum.AERODYNAMIC, 
						AnalysisTypeEnum.BALANCE,
						AnalysisTypeEnum.WEIGHTS,
						AnalysisTypeEnum.PERFORMANCES, 
						AnalysisTypeEnum.COSTS
						);		
	
	
	

	} // end Main method

}
