package sandbox.vt.IRON_Test;

import java.io.File;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.DatabaseReaderEnum;
import javafx.util.Pair;
import writers.JPADChartWriter;
import writers.JPADDataWriter;
import writers.JPADWriteUtils;

public class Test_IRON_01 {

	
	public static void main(String[] args) {

		// Initialize working directories
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
												MyConfiguration.inputDirectory,
												MyConfiguration.outputDirectory,
												MyConfiguration.databaseDirectory);	
		String folderName = "Test_IRON_01";
		
		// Define the aircraft
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.IRON);
		aircraft.setName("IRON");
		LiftingSurface2Panels theWing = aircraft.get_wing();
		
		String exportFile = MyConfiguration.outputDirectory + File.separator + 
				folderName + File.separator + aircraft.getName();

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
							AnalysisTypeEnum.WEIGHTS
//							AnalysisTypeEnum.PERFORMANCE, 
//							AnalysisTypeEnum.COSTS
							);
		
		
		JPADDataWriter _theWriteUtilities = new JPADDataWriter(
				operatingConditions,
				aircraft, 
				theAnalysis);

		JPADChartWriter _theWriteCharts = new JPADChartWriter(aircraft);
		_theWriteCharts.createCharts();

		// +++++++++++++++++++++++++++++++++++++++++++++++
		// STATIC FUNCTIONS - TO BE CALLED BEFORE WRITING CUSTOM XML FILES
		JPADWriteUtils.buildXmlTree(aircraft,operatingConditions);

		// Export everything to file
		_theWriteUtilities.exportToXMLfile(exportFile  + ".xml");
		_theWriteUtilities.exportToXLSfile(exportFile + ".xls");
	}

}
