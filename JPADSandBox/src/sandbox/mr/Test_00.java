package sandbox.mr;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.DatabaseReaderEnum;
import javafx.util.Pair;

public class Test_00 {

	public static void main(String[] args) {

		// --------------------------------------------------------------
		// Define directory
		// --------------------------------------------------------------
		MyConfiguration.initWorkingDirectoryTree();


		// --------------------------------------------------------------
		// Generate default Aircraft
		// --------------------------------------------------------------
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.B747_100B);
		LiftingSurface theWing = aircraft.get_wing();

		// Default operating conditions
		OperatingConditions theConditions = new OperatingConditions();


		// --------------------------------------------------------------
		// Define an ACAnalysisManager Object
		// --------------------------------------------------------------
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theConditions);
		theAnalysis.updateGeometry(aircraft);


		// --------------------------------------------------------------
		// Define an LSAerodynamicsManager Object
		// --------------------------------------------------------------
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager (
				theConditions,
				theWing,
				aircraft
				);


		// --------------------------------------------------------------
		// Setup database(s)
		// --------------------------------------------------------------

		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC, "Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);


		// --------------------------------------------------------------
		// Do analysis
		// --------------------------------------------------------------
		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.AERODYNAMIC);


	}
}