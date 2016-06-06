package database.databasefunctions.engine;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyInterpolatingFunction;

public class TurbopropEngineDatabaseReader extends EngineDatabaseReader{

	private MyInterpolatingFunction takeOffSFCFunction, continuousSFCFunction, climbSFCFunction,
	cruiseSFCFunction, descentSFCFunction;

	public TurbopropEngineDatabaseReader(String databaseFolderName, String engineDatabaseFileName) {

		super(databaseFolderName, engineDatabaseFileName);

		takeOffThrustFunction = database.interpolate2DFromDatasetFunction("TakeOffThrust");
		continuousThrustFunction = database.interpolate2DFromDatasetFunction("MaximumContinuousThrust");
		climbThrustFunction = database.interpolate2DFromDatasetFunction("MaximumClimbThrust");
		cruiseThrustFunction = database.interpolate2DFromDatasetFunction("MaximumCruiseThrust");
		descentThrustFunction = database.interpolate2DFromDatasetFunction("IdleThrust");

		takeOffSFCFunction = database.interpolate2DFromDatasetFunction("TakeOffSFC");
		continuousSFCFunction = database.interpolate2DFromDatasetFunction("MaximumContinuousSFC");
		climbSFCFunction = database.interpolate2DFromDatasetFunction("MaximumClimbSFC");
		cruiseSFCFunction = database.interpolate2DFromDatasetFunction("MaximumCruiseSFC");
		descentSFCFunction = database.interpolate2DFromDatasetFunction("IdleSFC");
	}

	@Override
	public double getThrustRatio(double mach, double altitude, double bpr, EngineOperatingConditionEnum flightCondition) {
		
		if (flightCondition.equals(EngineOperatingConditionEnum.TAKE_OFF)){
			return getThrustTakeOff(mach, altitude, bpr);
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

	@Override
	public double getThrustTakeOff(double mach, double altitude, double bpr) {
		return takeOffThrustFunction.value(mach, altitude/0.3048);
	}

	@Override
	public double getThrustContinuous(double mach, double altitude, double bpr) {
		return continuousThrustFunction.value(mach, altitude);
	}
	
	@Override
	public double getThrustMaximumClimb(double mach, double altitude, double bpr) {
		return climbThrustFunction.value(mach, altitude/0.3048);
	}

	@Override
	public double getThrustMaximumCruise(double mach, double altitude, double bpr) {
		return cruiseThrustFunction.value(mach, altitude/0.3048);
	}

	@Override
	public double getThrustDescent(double mach, double altitude, double bpr) {
		return descentThrustFunction.value(mach, altitude/0.3048);
	}

	@Override
	public double getSFC(double mach, double altitude, double tT0Ratio, double bpr, EngineOperatingConditionEnum flightCondition) {
		
		if (flightCondition.equals(EngineOperatingConditionEnum.TAKE_OFF)){
			return getSFCTakeOff(mach, altitude, bpr);
		}
		
		else if (flightCondition.equals(EngineOperatingConditionEnum.CONTINUOUS)) {
			return getSFCContinuous(mach, altitude, bpr);
		}

		else if (flightCondition.equals(EngineOperatingConditionEnum.CLIMB)){
			return getSFCMaximumClimb(mach, altitude, bpr);
		}

		else if (flightCondition.equals(EngineOperatingConditionEnum.CRUISE)){
			return getSFCMaximumCruise(mach, altitude, bpr);
		}

		else {
			return getSFCDescent(mach, altitude, bpr);
		}
	}
	
	public double getSFCTakeOff(double mach, double altitude, double bpr) {
		return takeOffSFCFunction.value(mach, altitude/0.3048);
	}
	
	public double getSFCContinuous(double mach, double altitude, double bpr) {
		return continuousSFCFunction.value(mach, altitude/0.3048);
	}

	public double getSFCMaximumClimb(double mach, double altitude, double bpr) {
		return climbSFCFunction.value(mach, altitude/0.3048);
	}

	public double getSFCMaximumCruise(double mach, double altitude, double bpr) {
		return cruiseSFCFunction.value(mach, altitude/0.3048);
	}

	public double getSFCDescent(double mach, double altitude, double bpr) {
		return descentSFCFunction.value(mach, altitude/0.3048);
	}

	@Override
	public double getDescentFuelFlow(double mach, double altitude, double bpr) {
		// TODO Auto-generated method stub
		return 0;
	}

}
