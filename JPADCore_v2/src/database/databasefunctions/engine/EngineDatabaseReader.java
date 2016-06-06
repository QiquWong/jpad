package database.databasefunctions.engine;

import configuration.enumerations.EngineOperatingConditionEnum;
import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.database.hdf.MyHDFReader;

/**
 * @author Lorenzo Attanasio
 *
 */
public abstract class EngineDatabaseReader extends DatabaseReader{

	protected MyInterpolatingFunction 
	takeOffThrustFunction, continuousThrustFunction, climbThrustFunction, cruiseThrustFunction, 
	descentThrustFunction, sfcFunction, descentFuelFlowFunction; 

	public EngineDatabaseReader(String databaseFolderPath, String engineDatabaseFileName) {
		super(databaseFolderPath, engineDatabaseFileName);
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param mach Mach number
	 * @param altitude (m)
	 * @param bpr ByPass Ratio
	 * @param flightCondition take-off, climb, cruise, descent
	 * @return
	 */
	public abstract double getThrustRatio(double mach, double altitude, double bpr, 
			EngineOperatingConditionEnum flightCondition);

	/**
	 * 
	 * @param mach
	 * @param altitude (m)
	 * @param bpr
	 * @return
	 */
	public abstract double getThrustTakeOff(double mach, double altitude, double bpr);

	/**
	 * 
	 * @param mach
	 * @param altitude (m)
	 * @param bpr
	 * @return
	 */
	public abstract double getThrustMaximumClimb(double mach, double altitude, double bpr);

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mach
	 * @param altitude
	 * @param bpr
	 * @return
	 */
	public abstract double getThrustContinuous(double mach, double altitude, double bpr);

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	public abstract double getThrustMaximumCruise(double mach, double altitude, double bpr);

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	public abstract double getThrustDescent(double mach, double altitude, double bpr);

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @param engineOperatingCondition
	 * @return Specific Fuel Consumption Jet (lb/(lb*h))
	 */
	public abstract double getSFC(double mach, double altitude, double tT0Ratio, double bpr, EngineOperatingConditionEnum engineOperatingCondition);

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	public abstract double getDescentFuelFlow(double mach, double altitude, double bpr);

	public String getEngineDatabaseFileName() {
		return databaseFileName;
	}

	public MyHDFReader getEngineDatabase() {
		return database;
	}

	public String getDatabaseFolderName() {
		return databaseFolderPath;
	}

}
