package sandbox.vc.agile;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

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
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import javafx.util.Pair;
import writers.JPADDataWriter;
import writers.JPADWriteUtils;

public class Test_AGILE_DC1_01 {

	
	public static void main(String[] args) {

		// Initialize working directories
//		MyConfiguration.initWorkingDirectoryTree();
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
												MyConfiguration.inputDirectory,
												MyConfiguration.outputDirectory,
												MyConfiguration.databaseDirectory);	
		
		// Define the aircraft
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		aircraft.setName("AGILE_DC1");
		LiftingSurface theWing = aircraft.get_wing();
		
		String exportFile = MyConfiguration.outputDirectory + File.separator + 
				 "Test_AGILE_DC1_01" + File.separator + aircraft.getName();

		// Set the operating conditions
		OperatingConditions operatingConditions = new OperatingConditions();
		operatingConditions.set_altitude(Amount.valueOf(11000., SI.METER));
		operatingConditions.set_machCurrent(0.78);
		
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
							AnalysisTypeEnum.AERODYNAMIC, 
							AnalysisTypeEnum.BALANCE,
							AnalysisTypeEnum.WEIGHTS,
							AnalysisTypeEnum.PERFORMANCES, 
							AnalysisTypeEnum.COSTS
							);
		
//		Map<MethodEnum, Amount<Mass>> massMap = aircraft.get_fuselage().get_massMap();
//		System.out.println("Mass map: " + massMap);
		 Amount<Mass> nacelleMass = aircraft.get_theNacelles().get_totalMass();
		System.out.println("Nacelles Mass: " + nacelleMass);
		
		JPADDataWriter _theWriteUtilities = new JPADDataWriter(
				operatingConditions,
				aircraft, 
				theAnalysis);

//		_theWriteCharts = new MyChartWriter(aircraft);
//		_theWriteCharts.createCharts();

		// +++++++++++++++++++++++++++++++++++++++++++++++
		// STATIC FUNCTIONS - TO BE CALLED BEFORE WRITING CUSTOM XML FILES
		JPADWriteUtils.buildXmlTree(aircraft,operatingConditions);

		// Export everything to file
		_theWriteUtilities.exportToXMLfile(exportFile  + ".xml");
		_theWriteUtilities.exportToXLSfile(exportFile + ".xls");
	}

}
