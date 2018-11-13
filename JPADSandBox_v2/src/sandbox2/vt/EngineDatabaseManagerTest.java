package sandbox2.vt;


import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.engine.EngineDatabaseManager_v2;

public class EngineDatabaseManagerTest {

	public static void main(String[] args) {
	
		System.out.println("-----------------------------");
		System.out.println("Engine Database Manager Test");
		System.out.println("-----------------------------");
		
		System.out.println("\nInitializing folders ...");
		MyConfiguration.initWorkingDirectoryTree();
		
		String engineDatabaseFileDirectory = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String engineDatabaseFileName = "EngineDatabaseTP.xlsx";
		System.out.println("Engine database file --> " + engineDatabaseFileDirectory + engineDatabaseFileName);
		System.out.println("Done!");
		
		System.out.println("\nCreating the Database Manager ...");
		EngineDatabaseManager_v2 engineDatabaseManager = DatabaseManager.initializeEngineDatabase(
				new EngineDatabaseManager_v2(), 
				engineDatabaseFileDirectory, 
				engineDatabaseFileName
				);
//		EngineDatabaseManager_v2 engineDatabaseManager = new EngineDatabaseManager_v2(engineDatabaseFileDirectory, engineDatabaseFileName);
		System.out.println("\n\nDone!");
		
		System.out.println("\nTesting getters ...");
		double thrustRatio = engineDatabaseManager.getThrustRatio(
				0.25,									// mach
				Amount.valueOf(1000, NonSI.FOOT),		// altitude
				Amount.valueOf(0.0, SI.CELSIUS),		// delta Temperature
				1.0, 									// throttle setting
				EngineOperatingConditionEnum.TAKE_OFF	// engine rating
				);
		System.out.println("TAKE-OFF THRUST RATIO AT : 1000ft, M=0.25, dT=0°C, PHI=1.0 --> " + thrustRatio);
		System.out.println("Done!");
		
	}
}
