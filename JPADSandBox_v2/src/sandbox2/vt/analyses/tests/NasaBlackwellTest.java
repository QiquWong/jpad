package sandbox2.vt.analyses.tests;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsCalculator;
import analyses.liftingsurface.LSAerodynamicsCalculator.CalcCLmax;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;

public class NasaBlackwellTest {

	public static void main(String[] args) {
		
//		System.out.println("-------------------");
//		System.out.println("NASA Blackwell test");
//		System.out.println("-------------------");
//		
//		//------------------------------------------------------------------------------------
//		// Setup database(s)
//		MyConfiguration.initWorkingDirectoryTree();
//		
//		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
//		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
//		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
//		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
//		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
//		
//		//------------------------------------------------------------------------------------
//		// Define the Aircraft 
//		Aircraft theAircraft = new Aircraft.AircraftBuilder(
//				"ATR-72",
//				AircraftEnum.ATR72,
//				aeroDatabaseReader,
//				highLiftDatabaseReader
//				).build();
//		
//		OperatingConditions theOperatingConditions = new OperatingConditions.OperatingConditionsBuilder(
//				"ATR-72 Operating Conditions"
//				).build();
//		
//		Map <String, List<MethodEnum>> taskMap = new HashMap<>();
//		Map <String, List<MethodEnum>> plotMap = new HashMap<>();
//		LSAerodynamicsCalculator theLSAerodynamicsCalculator = new LSAerodynamicsCalculator(
//				theAircraft.getWing(),
//				theOperatingConditions,
//				taskMap,
//				plotMap,
//				ConditionEnum.CRUISE,
//				50
//				);
//		
//		CalcCLmax calcCLmax = theLSAerodynamicsCalculator.new CalcCLmax();
//		calcCLmax.nasaBlackwell();
//		
//		double[][] xArrays = new double[2][theLSAerodynamicsCalculator.getNumberOfPointSemiSpanWise()];
//		xArrays[0] = theLSAerodynamicsCalculator.getEtaStationDistribution();
//		xArrays[1] = theLSAerodynamicsCalculator.getEtaStationDistribution();
//		
//		double[][] yArrays = new double[2][theLSAerodynamicsCalculator.getNumberOfPointSemiSpanWise()];
//		yArrays[0] = MyArrayUtils.convertToDoublePrimitive(theLSAerodynamicsCalculator.getClMaxDistribution());
//		yArrays[1] = theLSAerodynamicsCalculator.getLiftCoefficientDistributionAtCLMax().get(MethodEnum.NASA_BLACKWELL);
//		
//		MyChartToFileUtils.plotNoLegend(
//				xArrays,
//				yArrays,
//				0.0,
//				1.0,
//				0.0,
//				2.2,
//				"eta",
//				"Cl",
//				"",
//				"",
//				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
//				"Stall Path"
//				);
	}
}
