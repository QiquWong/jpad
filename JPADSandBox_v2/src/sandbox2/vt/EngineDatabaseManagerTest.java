package sandbox2.vt;


import java.io.File;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
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
		EngineDatabaseManager_v2 engineDatabaseManager = new EngineDatabaseManager_v2(engineDatabaseFileDirectory, engineDatabaseFileName);
		System.out.println("Done!");
		
		System.out.println("\nTesting getters ...");
		// TODO
		System.out.println("Done!");
		
	}
}
