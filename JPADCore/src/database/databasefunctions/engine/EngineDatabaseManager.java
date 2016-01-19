package database.databasefunctions.engine;

import java.io.File;

import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;


public class EngineDatabaseManager {

	private static EngineTypeEnum engineType = EngineTypeEnum.TURBOFAN;	
	private static final TurbofanEngineDatabaseReader turbofanDatabaseReader 
		= new TurbofanEngineDatabaseReader(
				System.getProperty("user.dir") + File.separator + MyConfiguration.databaseFolderPath, 
				"TurbofanEngineDatabase.h5");
	private static final TurbopropEngineDatabaseReader turbopropDatabaseReader 
		= new TurbopropEngineDatabaseReader(
				System.getProperty("user.dir") + File.separator + MyConfiguration.databaseFolderPath, 
				"TurbopropEngineDatabase.h5");


	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mach
	 * @param altitude
	 * @param bpr
	 * @param engineType
	 * @param engineOperatingCondition
	 * @return
	 */
	public static double getThrustRatio(
			double mach, 
			double altitude, 
			double bpr, 
			EngineTypeEnum engineType,
			EngineOperatingConditionEnum engineOperatingCondition) {

		if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
			return turbofanDatabaseReader.getThrustRatio(mach, altitude, bpr, engineOperatingCondition);

		} else {
			return turbopropDatabaseReader.getThrustRatio(mach, altitude, bpr, engineOperatingCondition);
		}
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mach
	 * @param altitude
	 * @param tT0Ratio
	 * @param bpr
	 * @param engineType
	 * @param engineOperatingCondition
	 * @return
	 */
	public static double getSFC(
			double mach,
			double altitude, 
			double tT0Ratio, 
			double bpr,
			EngineTypeEnum engineType,
			EngineOperatingConditionEnum engineOperatingCondition) {

		if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
			return turbofanDatabaseReader.getSFC(mach, altitude, tT0Ratio, bpr, engineOperatingCondition); 

		} else {
			return turbopropDatabaseReader.getSFC(mach, altitude, tT0Ratio, bpr, engineOperatingCondition);
		}
	}

	public static EngineTypeEnum getEngineType() {
		return engineType;
	}

	public static void setEngineType(EngineTypeEnum engineType) {
		EngineDatabaseManager.engineType = engineType;
	}

}

