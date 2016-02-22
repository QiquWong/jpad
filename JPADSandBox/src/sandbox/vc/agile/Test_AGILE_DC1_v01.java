package sandbox.vc.agile;

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

public class Test_AGILE_DC1_v01 {

	
	public static void main(String[] args) {

		// Initilize working directories
		MyConfiguration.initWorkingDirectoryTree();
//		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
//												MyConfiguration.inputDirectory,
//												MyConfiguration.outputDirectory);	
		
		// Define the aircraft
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		LiftingSurface theWing = aircraft.get_wing();
		// Set the operating conditions
		OperatingConditions operatingConditions = new OperatingConditions();
//		operatingConditions.set_altitude(Amount.valueOf(0.000, SI.METER));
		
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
		
		// Database(s)
//		String aerodynamicDatabaseName = "Aerodynamic_Database_Ultimate.h5"; 
//		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(MyConfiguration.databaseFolderName, aerodynamicDatabaseName);
//		aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
//		String veDSCDatabaseFileName = "VeDSC_database.h5";
//		String fusDesDatabaseFileName = "FusDes_database.h5";
		
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
							AnalysisTypeEnum.AERODYNAMIC, 
							AnalysisTypeEnum.BALANCE,
							AnalysisTypeEnum.WEIGHTS,
							AnalysisTypeEnum.PERFORMANCES, 
							AnalysisTypeEnum.COSTS
							);
		

	}

}
