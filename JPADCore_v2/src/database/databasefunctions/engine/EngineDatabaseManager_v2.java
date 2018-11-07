package database.databasefunctions.engine;

import java.io.File;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

/**
 * 
 * @author Vittorio Trifari
 *
 */
public class EngineDatabaseManager_v2 extends EngineDatabaseReader_v2 {

	//-------------------------------------------------------------------
	// BUILDER
	//-------------------------------------------------------------------
	public EngineDatabaseManager_v2(String databaseFolderPath, String engineDatabaseFileName) {
		
		readDatabaseFromFile(databaseFolderPath, engineDatabaseFileName);
		serializeDatabase(databaseFolderPath, engineDatabaseFileName);
		
	}

	//-------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------
	public void readDatabaseFromFile(String databaseFolderPath, String engineDatabaseFileName) {
		
		// TODO: ADD READ DATA FROM FILE (CHOOSE THE BEST INPUT FILE FORMAT)
		
	}
	
	public void serializeDatabase(String databaseFolderPath, String engineDatabaseFileName) {
		
		String databaseNameXML = null;
		
		// TODO: CHANGE FILE EXTENSION
		if(engineDatabaseFileName.endsWith(".h5"))
			databaseNameXML = engineDatabaseFileName.replace(".h5", ".xml");
		
		String interpolatedEngineDatabaseSerializedDirectory = databaseFolderPath + File.separator + "serializedDatabase" 
				+ File.separator; 
		String interpolatedEngineDatabaseSerializedFullName = interpolatedEngineDatabaseSerializedDirectory +  
				File.separator + databaseNameXML;

		File serializedDatabaseFile = new File(interpolatedEngineDatabaseSerializedFullName);

		if(!serializedDatabaseFile.exists()){
			System.out.println(	"Serializing file " + "==> " + engineDatabaseFileName + "  ==> "+ 
					serializedDatabaseFile.getAbsolutePath() + " ...");

			File dir = new File(interpolatedEngineDatabaseSerializedDirectory);
			if(!dir.exists()){
				dir.mkdirs(); 
			} 
			else {
				JPADStaticWriteUtils.serializeObject(
						this, 
						interpolatedEngineDatabaseSerializedDirectory,
						databaseNameXML
						);
			}
		}
	}
	
	@Override
	public double getThrustRatio(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double thrustRatio = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			thrustRatio = getThrustRatioTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			thrustRatio = getThrustRatioAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			thrustRatio = getThrustRatioClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			thrustRatio = getThrustRatioContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			thrustRatio = getThrustRatioCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			thrustRatio = getThrustRatioFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			thrustRatio = getThrustRatioGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return thrustRatio;
	}

	@Override
	public double getThrustRatioTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getThrustRatioGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfc(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {
		
		double sfc = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			sfc = getSfcTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			sfc = getSfcAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			sfc = getSfcClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			sfc = getSfcContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			sfc = getSfcCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			sfc = getSfcFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			sfc = getSfcGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return sfc;
	}

	@Override
	public double getSfcTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSfcGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexNOx = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexNOx = getNOxEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexNOx = getNOxEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexNOx = getNOxEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexNOx = getNOxEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexNOx = getNOxEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			emissionIndexNOx = getNOxEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexNOx = getNOxEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexNOx;
	}

	@Override
	public double getNOxEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNOxEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexCO = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexCO = getCOEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexCO = getCOEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexCO = getCOEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexCO = getCOEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexCO = getCOEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			emissionIndexCO = getCOEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexCO = getCOEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexCO;
	}

	@Override
	public double getCOEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCOEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexHC = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexHC = getHCEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexHC = getHCEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexHC = getHCEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexHC = getHCEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexHC = getHCEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			emissionIndexHC = getHCEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexHC = getHCEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexHC;
	}

	@Override
	public double getHCEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHCEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexCO2 = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexCO2 = getCO2EmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexCO2 = getCO2EmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexCO2 = getCO2EmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexCO2 = getCO2EmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexCO2 = getCO2EmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			emissionIndexCO2 = getCO2EmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexCO2 = getCO2EmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexCO2;
		
	}

	@Override
	public double getCO2EmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCO2EmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexH2O = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexH2O = getH2OEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexH2O = getH2OEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexH2O = getH2OEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexH2O = getH2OEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexH2O = getH2OEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FILD:
			emissionIndexH2O = getH2OEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexH2O = getH2OEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexH2O;
	}

	@Override
	public double getH2OEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getH2OEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		// TODO Auto-generated method stub
		return 0;
	}

}

