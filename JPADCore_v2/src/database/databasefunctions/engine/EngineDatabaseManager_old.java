package database.databasefunctions.engine;

import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;


public class EngineDatabaseManager_old {

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
			EngineOperatingConditionEnum engineOperatingCondition,
			PowerPlant thePowerPlant
			) {

		if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
			return thePowerPlant.getTurbofanEngineDatabaseReader().getThrustRatio(mach, altitude, bpr, engineOperatingCondition);

		} else {
			return thePowerPlant.getTurbopropEngineDatabaseReader().getThrustRatio(mach, altitude, bpr, engineOperatingCondition);
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
			EngineOperatingConditionEnum engineOperatingCondition,
			PowerPlant thePowerPlant
			) {

		if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
			return thePowerPlant.getTurbofanEngineDatabaseReader().getSFC(mach, altitude, tT0Ratio, bpr, engineOperatingCondition); 

		} else {
			return thePowerPlant.getTurbopropEngineDatabaseReader().getSFC(mach, altitude, tT0Ratio, bpr, engineOperatingCondition);
		}
	}

}

