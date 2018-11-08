package database.databasefunctions.engine;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import flanagan.interpolation.PolyCubicSpline;

/**
 * @author Vittorio Trifari
 *
 */
public abstract class EngineDatabaseReader_v2 {

	//----------------------------------------------------------------------------------------
	// VARIABLES DECLARATION
	//----------------------------------------------------------------------------------------
	/*
	 * Thrust Ratio Interpolating Functions
	 */
	protected PolyCubicSpline 
	takeOffThrustRatioFunction,
	aprThrustRatioFunction,
	climbThrustRatioFunction,
	continuousThrustRatioFunction,
	cruiseThrustRatioFunction, 
	flightIdleThrustRatioFunction,
	groundIdleThrustRatioFunction;
	
	/*
	 * SFC Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffSFCFunction,
	aprSFCFunction,
	climbSFCFunction,
	continuousSFCFunction,
	cruiseSFCFunction, 
	flightIdleSFCFunction,
	groundIdleSFCFunction; 
	
	/*
	 * NOx Emission Index Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffNOxEmissionIndexFunction,
	aprNOxEmissionIndexFunction,
	climbNOxEmissionIndexFunction,
	continuousNOxEmissionIndexFunction,
	cruiseNOxEmissionIndexFunction, 
	flightIdleNOxEmissionIndexFunction,
	groundIdleNOxEmissionIndexFunction; 

	/*
	 * CO Emission Index Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffCOEmissionIndexFunction,
	aprCOEmissionIndexFunction,
	climbCOEmissionIndexFunction,
	continuousCOEmissionIndexFunction,
	cruiseCOEmissionIndexFunction, 
	flightIdleCOEmissionIndexFunction,
	groundIdleCOEmissionIndexFunction;
	
	/*
	 * HC Emission Index Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffHCEmissionIndexFunction,
	aprHCEmissionIndexFunction,
	climbHCEmissionIndexFunction,
	continuousHCEmissionIndexFunction,
	cruiseHCEmissionIndexFunction, 
	flightIdleHCEmissionIndexFunction,
	groundIdleHCEmissionIndexFunction;
	
	/*
	 * CO2 Emission Index Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffCO2EmissionIndexFunction,
	aprCO2EmissionIndexFunction,
	climbCO2EmissionIndexFunction,
	continuousCO2EmissionIndexFunction,
	cruiseCO2EmissionIndexFunction, 
	flightIdleCO2EmissionIndexFunction,
	groundIdleCO2EmissionIndexFunction;
	
	/*
	 * H2O Emission Index Interpolating Functions
	 */
	protected PolyCubicSpline
	takeOffH2OEmissionIndexFunction,
	aprH2OEmissionIndexFunction,
	climbH2OEmissionIndexFunction,
	continuousH2OEmissionIndexFunction,
	cruiseH2OEmissionIndexFunction, 
	flightIdleH2OEmissionIndexFunction,
	groundIdleH2OEmissionIndexFunction;

	//----------------------------------------------------------------------------------------
	// METHODS
	//----------------------------------------------------------------------------------------
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic thrust ratio
	 */
	public abstract double getThrustRatio(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);

	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the take-off thrust ratio
	 */
	public abstract double getThrustRatioTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR thrust ratio
	 */
	public abstract double getThrustRatioAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb thrust ratio
	 */
	public abstract double getThrustRatioClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);

	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous thrust ratio
	 */
	public abstract double getThrustRatioContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise thrust ratio
	 */
	public abstract double getThrustRatioCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle thrust ratio
	 */
	public abstract double getThrustRatioFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle Idle thrust ratio
	 */
	public abstract double getThrustRatioGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic SFC in [lb/lb*hr]
	 */
	public abstract double getSfc(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off SFC in [lb/lb*hr]
	 */
	public abstract double getSfcTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR SFC in [lb/lb*hr]
	 */
	public abstract double getSfcAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb SFC in [lb/lb*hr]
	 */
	public abstract double getSfcClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous SFC in [lb/lb*hr]
	 */
	public abstract double getSfcContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise SFC in [lb/lb*hr]
	 */
	public abstract double getSfcCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle SFC in [lb/lb*hr]
	 */
	public abstract double getSfcFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle SFC in [lb/lb*hr]
	 */
	public abstract double getSfcGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic NOx Emission Index
	 */
	public abstract double getNOxEmissionIndex(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle NOx Emission Index
	 */
	public abstract double getNOxEmissionIndexGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic CO Emission Index
	 */
	public abstract double getCOEmissionIndex(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off CO Emission Index
	 */
	public abstract double getCOEmissionIndexTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR CO Emission Index
	 */
	public abstract double getCOEmissionIndexAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb CO Emission Index
	 */
	public abstract double getCOEmissionIndexClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous CO Emission Index
	 */
	public abstract double getCOEmissionIndexContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise CO Emission Index
	 */
	public abstract double getCOEmissionIndexCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle CO Emission Index
	 */
	public abstract double getCOEmissionIndexFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle CO Emission Index
	 */
	public abstract double getCOEmissionIndexGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic HC Emission Index
	 */
	public abstract double getHCEmissionIndex(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off HC Emission Index
	 */
	public abstract double getHCEmissionIndexTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR HC Emission Index
	 */
	public abstract double getHCEmissionIndexAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb HC Emission Index
	 */
	public abstract double getHCEmissionIndexClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous HC Emission Index
	 */
	public abstract double getHCEmissionIndexContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise HC Emission Index
	 */
	public abstract double getHCEmissionIndexCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle HC Emission Index
	 */
	public abstract double getHCEmissionIndexFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle HC Emission Index
	 */
	public abstract double getHCEmissionIndexGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndex(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle CO2 Emission Index
	 */
	public abstract double getCO2EmissionIndexGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);

	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @param flightCondition the engine rating
	 * @return the generic H2O Emission Index
	 */
	public abstract double getH2OEmissionIndex(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting, 
			EngineOperatingConditionEnum flightCondition
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Take-Off H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexTakeOff(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the APR H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexAPR(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Climb H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexClimb(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Continuous H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexContinuous(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Cruise H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexCruise(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Flight Idle H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexFlightIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param mach the Mach number
	 * @param altitude an Amount containing the altitude
	 * @param deltaTemperature an Amount containing the temperature offset
	 * @param throttleSetting the throttle setting of the rating 
	 * @return the Ground Idle H2O Emission Index
	 */
	public abstract double getH2OEmissionIndexGroundIdle(
			double mach, 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double throttleSetting 
			);
}
