package database.databasefunctions.engine;


import org.apache.commons.math3.exception.OutOfRangeException;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * 
 * @author Lorenzo Attanasio
 *
 */
public class TurbofanEngineDatabaseReader extends EngineDatabaseReader {

	private double t_T0Ratio = 0.00, bPR = 0., machN = 0.0, alt = 0., res = 0.;

	public TurbofanEngineDatabaseReader(String databaseFolderName, String engineDatabaseFileName) {
		
		super(databaseFolderName, engineDatabaseFileName);

		takeOffThrustFunction = database.interpolate3DFromDatasetFunction("TakeOffThrust");
		climbThrustFunction = database.interpolate3DFromDatasetFunction("MaximumClimbThrust");
		cruiseThrustFunction = database.interpolate3DFromDatasetFunction("MaximumCruiseThrust");
		descentThrustFunction = database.interpolate3DFromDatasetFunction("DescentThrust");
		sfcFunction = database.interpolate3DFromDatasetFunction("SFCloops");
		descentFuelFlowFunction = database.interpolate3DFromDatasetFunction("DescentFuelFlow");
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param mach Mach number
	 * @param altitude (m)
	 * @param bpr ByPass Ratio
	 * @param flightCondition take-off, climb, cruise, descent
	 * @return
	 */
	@Override
	public double getThrustRatio(
			double mach, 
			double altitude, 
			double bpr, 
			EngineOperatingConditionEnum flightCondition) {

		if (flightCondition.equals(EngineOperatingConditionEnum.TAKE_OFF)){
			return getThrustTakeOff(mach, altitude,bpr);
		}
		
		else if (flightCondition.equals(EngineOperatingConditionEnum.CONTINUOUS)) {
			return getThrustContinuous(mach, altitude, bpr);
		}

		else if (flightCondition.equals(EngineOperatingConditionEnum.CLIMB)){
			return getThrustMaximumClimb(mach, altitude, bpr);
		}

		else if (flightCondition.equals(EngineOperatingConditionEnum.CRUISE)){
			return getThrustMaximumCruise(mach, altitude, bpr);
		}

		else {
			return getThrustDescent(mach, altitude, bpr);
		}
	}

	/**
	 * Return the value corresponding to the last 
	 * successful reading from the database
	 * 
	 * @param mach
	 * @param altitude (m)
	 * @param bpr
	 * @param f
	 * @return
	 */
	private double manageOutOfRangeException(
			double mach, 
			double altitude, 
			double bpr, MyInterpolatingFunction f) {

		double result;

		try {
			result = f.value(mach, altitude/0.3048, bpr);
			machN = mach;
			alt = altitude;
			bPR = bpr;

		} catch(OutOfRangeException e) {
			result = f.value(machN, alt/0.3048, bPR);
		}

		return result;
	}

	/**
	 * 
	 * @param tT0Ratio
	 * @param altitude (m)
	 * @param mach
	 * @param bpr
	 * @param f
	 * @return
	 */
	private double manageOutOfRangeException(
			double tT0Ratio, 
			double altitude, double mach,
			double bpr, MyInterpolatingFunction f) {

		double thrustNonDimensionalRatio;
		if (tT0Ratio < 0.) tT0Ratio = 0.;
		if (tT0Ratio > 1.) tT0Ratio = 1.;
		if (altitude < 0.) altitude = 0.;
		if (mach < 0.) mach = 0.;
		if (mach > 0.95) mach = 0.949;
		if (bpr < 0.) bpr = 0.;
		if (bpr > 13.) bpr = 13.;
		thrustNonDimensionalRatio = tT0Ratio/AtmosphereCalc.getAtmosphere(altitude).getPressureRatio();
		if (thrustNonDimensionalRatio < 0.) thrustNonDimensionalRatio = 0.;
		if (thrustNonDimensionalRatio > 1.2) thrustNonDimensionalRatio = 1.19999999; 

		return f.value(thrustNonDimensionalRatio, mach, bpr);
	}

	/**
	 * 
	 * @param mach
	 * @param altitude (m)
	 * @param bpr
	 * @return
	 */
	@Override
	public double getThrustTakeOff(
			double mach, 
			double altitude, 
			double bpr) {
		return takeOffThrustFunction.valueTrilinear(bpr, mach, altitude/0.3048);
	}
	
	@Override
	public double getThrustContinuous(double mach, double altitude, double bpr) {
		return getThrustTakeOff(mach, altitude, bpr);
	}

	/**
	 * 
	 * @param mach
	 * @param altitude (m)
	 * @param bpr
	 * @return
	 */
	@Override
	public double getThrustMaximumClimb(
			double mach, 
			double altitude, 
			double bpr) {
		return climbThrustFunction.valueTrilinear(bpr, mach, altitude/0.3048);
	}

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	@Override
	public double getThrustMaximumCruise(
			double mach, 
			double altitude, 
			double bpr) {
		return cruiseThrustFunction.valueTrilinear(bpr, mach, altitude/0.3048);
	}

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	@Override
	public double getThrustDescent(
			double mach, 
			double altitude, 
			double bpr) {
		return descentThrustFunction.valueTrilinear(bpr, mach, altitude/0.3048);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return Specific Fuel Consumption Jet (lb/(lb*h))
	 */
	@Override
	public double getSFC(
			double mach,
			double altitude, 
			double tT0Ratio, 
			double bpr,
			EngineOperatingConditionEnum engineOperatingCondition) {
		
		return Math.pow(AtmosphereCalc.getAtmosphere(altitude).getTemperatureRatio(), 0.616)
				* sfcFunction.valueTrilinear(
						bpr,
						tT0Ratio/AtmosphereCalc.getAtmosphere(altitude).getPressureRatio(),
						mach);
	}

	/**
	 * 
	 * @param mach
	 * @param altitude meters
	 * @param bpr
	 * @return
	 */
	public double getDescentFuelFlow(
			double mach, 
			double altitude, 
			double bpr) {
		return manageOutOfRangeException(mach, altitude, bpr, descentFuelFlowFunction);
	}

}


