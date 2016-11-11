package calculators.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.ACAerodynamicsManager;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AirplaneType;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseManager;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

/**
 * Class that allows users to evaluate the range in 3 different condition:
 * 
 * - Maximum TO Weight and Max Payload
 * - Maximum TO Weight and Max Fuel allowed
 * - TO Weight and Max Fuel allowed with Zero Payload
 * 
 * Moreover it allows to confront current Mach condition with the best range one, parametrize 
 * the analysis at different Max Take Off Mass and to generate Payload and Range arrays
 * in order to be plot in the Payload-Range chart.
 * 
 * @author Vittorio Trifari
 */

public class PayloadRangeCalc{
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	
	// INPUT DATA:
	private Aircraft theAircraft;
	private Amount<Mass> paxSingleMass;
	private Amount<Mass> maxTakeOffMass;
	private Amount<Mass> operatingEmptyMass; 
	private Amount<Angle> sweepLEEquivalent;
	private Amount<Angle> sweepHalfChordEquivalent;
	private Amount<Area> surface;
	private Amount<Length> altitude;
	private FuelFractionDatabaseReader fuelFractionDatabase;
	private double cD0;
	private double oswald;
	private double cL;
	private double ar;
	private double tcMax;
	private double byPassRatio;
	private double cruiseMach;
	private double nPassMax;
	private double etaPropeller;
	private double sfc;
	private AirfoilTypeEnum airfoilType;	
	private AircraftTypeEnum aircraftType;
	private EngineTypeEnum engineType;
	private double[][] fuelFractionTable;

	// TO EVALUATE:
	private Map<String,Double> pointE = new HashMap<String, Double>();
	private Map<String,Double> pointP = new HashMap<String, Double>();
	private Map<String,Double> pointA = new HashMap<String, Double>();
	
	private Amount<Mass> fuelMass, maxFuelMass, takeOffMass, payloadMaxFuel,
						 maxTakeOffMass_current;
	private Amount<Length> rangeAtMaxPayload, rangeAtMaxFuel, rangeAtZeroPayload;
	private double rangeBreguet, bestRangeMach, nPassActual, cD, criticalMach, mffRatio;
	private double[][] rangeMatrix, payloadMatrix;

	//-------------------------------------------------------------------------------------
	// BUILDER
	
	public PayloadRangeCalc (
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> maxFuelMass,
			double cD0,
			double oswald,
			double cruiseMach,
			Amount<Length> altitude,
			Amount<Mass> passengerSingleMass
			) {
		
		this.theAircraft = theAircraft;
		this.maxTakeOffMass = maxTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.maxFuelMass = maxFuelMass;
		this.nPassMax = theAircraft.getCabinConfiguration().getMaxPax();
		this.paxSingleMass = passengerSingleMass;				
		
		this.surface = theAircraft.getWing().getSurface();
		this.ar = theAircraft.getWing().getAspectRatio();
		this.cruiseMach = cruiseMach;
		this.altitude = altitude;
		this.sweepLEEquivalent = theAircraft.getWing().getSweepLEEquivalent(false);
		this.sweepHalfChordEquivalent = theAircraft.getWing().getSweepHalfChordEquivalent(false);
		
		this.aircraftType = theAircraft.getTypeVehicle();
		this.engineType = theAircraft.getPowerPlant().getEngineType();
		this.airfoilType = theAircraft.getWing().getAirfoilList().get(0).getType();
		
		///////////////////////////////////////////////////////////////////////////////////////
		// Defining the fuel fraction ratio as function of the aircraft type:
		this.fuelFractionDatabase = theAircraft.getFuelTank().getFuelFractionDatabase();
		try {
			this.fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Roskam");
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		this.mffRatio = 1.0;
		
		switch (aircraftType){

		case TURBOPROP: int indexTurboprop = 5;
		for (int i=0; i<fuelFractionTable[indexTurboprop].length; i++)
			mffRatio *= fuelFractionTable[indexTurboprop][i]; 
		break;

		case JET: int indexJet = 6;
		for (int i=0; i<fuelFractionTable[indexJet].length; i++)
			mffRatio *= fuelFractionTable[indexJet][i]; 
		break;

		case BUSINESS_JET: int indexBusinessJet = 4;
		for (int i=0; i<fuelFractionTable[indexBusinessJet].length; i++)
			mffRatio *= fuelFractionTable[indexBusinessJet][i]; 
		break;

		case FIGHTER: int indexFighter = 8;
		for (int i=0; i<fuelFractionTable[indexFighter].length; i++)
			mffRatio *= fuelFractionTable[indexFighter][i]; 
		break;

		case ACROBATIC: int indexAcrobatic = 7;
		for (int i=0; i<fuelFractionTable[indexAcrobatic].length; i++)
			mffRatio *= fuelFractionTable[indexAcrobatic][i]; 
		break;

		case COMMUTER: int indexCommuter = 5;
		for (int i=0; i<fuelFractionTable[indexCommuter].length; i++)
			mffRatio *= fuelFractionTable[indexCommuter][i]; 
		break;

		case GENERAL_AVIATION:
		if(theAircraft.getPowerPlant().getEngineNumber() == 1) {
			int indexSingleEngine = 1;	
			for (int i=0; i<fuelFractionTable[indexSingleEngine].length; i++)
				mffRatio *= fuelFractionTable[indexSingleEngine][i];
		}
		else if(theAircraft.getPowerPlant().getEngineNumber() == 2) {
			int indexTwinEngine = 2;
			for (int i=0; i<fuelFractionTable[indexTwinEngine].length; i++)
				mffRatio *= fuelFractionTable[indexTwinEngine][i];
		}
		break;
		}
		
		///////////////////////////////////////////////////////////////////////////////////////
		// Check if cruise Mach number is lower than the critical Mach number
		checkCriticalMach(cruiseMach);
		
		///////////////////////////////////////////////////////////////////////////////////////
		// Calculating the drag polar characteristic points of interest
		pointE = DragCalc.calculateMaximumEfficiency(
				cruiseMach,
				oswald,
				cD0,
				theOperatingConditions.getDensityCruise().getEstimatedValue(),
				maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
				theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
				);
		pointP = DragCalc.calculateMinimumPower(
				cruiseMach,
				oswald,
				cD0,
				theOperatingConditions.getDensityCruise().getEstimatedValue(),
				maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
				theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
				);
		pointA = DragCalc.calculateMaximumRange(
				cruiseMach,
				oswald,
				cD0,
				theOperatingConditions.getDensityCruise().getEstimatedValue(),
				maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
				theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
				);
		///////////////////////////////////////////////////////////////////////////////////////
		// Calculating best range Mach number
		calculateBestRangeMach();
	}

	//-------------------------------------------------------------------------------------
	// METHODS
	
	/********************************************************************************************
	 * Method that calculate, for a given aircraft with a given engine,
	 * the number of Mach related to the Best Range condition.
	 * 
	 * @author Vittorio Trifari
	 * @return bestRangeMach the Mach number relative to the Best Range condition.
	 */
	public void calculateBestRangeMach(){
		
		if (engineType == EngineTypeEnum.PISTON  ||
				engineType == EngineTypeEnum.TURBOPROP) {
				
			bestRangeMach = SpeedCalc.calculateMach(
					altitude.doubleValue(SI.METER),
					pointE.get("Speed_E")
					);
		}
		else if (engineType == EngineTypeEnum.TURBOJET ||
				engineType == EngineTypeEnum.TURBOFAN ||
				engineType == EngineTypeEnum.RAMJET   ||
				engineType == EngineTypeEnum.PROPFAN)  {
			
			bestRangeMach = SpeedCalc.calculateMach(
					altitude.doubleValue(SI.METER),
					pointA.get("Speed_A"));
		}
	}

	/**************************************************************************************
	 * This method allow user to check if the Mach number chosen for the calculation it's lower 
	 * than the critical Mach number in that flight condition or not.
	 * 
	 * @author Vittorio Trifari
	 * @param currentMach
	 * @return check boolean that is true if the current Mach is lower than the critical one
	 */
	private boolean checkCriticalMach(double currentMach){
		
		criticalMach = AerodynamicCalc.calculateMachCriticalKroo(
				cL,
				sweepHalfChordEquivalent,
				tcMax,
				airfoilType);
		boolean check = false;
		
		if (currentMach < criticalMach)
			check = true;
		
		return check;
	}
	
	/**************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Payload.
	 *
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @return range the range value in [nmi]
	 */
	public void calcRangeAtMaxPayload(boolean isBestRange) {	
		
		/* 
		 * first of all it's necessary to calculate the fuel weight the airplane 
		 * can take on board @ MTOW and max Payload.
		 */
		fuelMass = maxTakeOffMass.minus(operatingEmptyMass).minus(paxSingleMass.times(nPassMax));
		Amount<? extends Quantity> fuelFraction  = fuelMass.divide(maxTakeOffMass);
		double mff = 1-fuelFraction.getEstimatedValue();

		/*
		 * now it's necessary to evaluate the w0/wf ratio to be used in breguet
		 * formula in order to determine the range. This ratio isn't mff but
		 * the cruise (eventually loiter and diversion) ratio which has to be 
		 * derived from mff dividing by all other phases ratios.
		 */
		double breguetRatio = mffRatio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				engineType==EngineTypeEnum.TURBOPROP)) {

						sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cL = pointE.get("CL_E");
			cD = pointE.get("CD_E");
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {
			
			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cL = pointA.get("CL_A");
			cD = pointA.get("CD_A");
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);
			
			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);
						
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}

		rangeAtMaxPayload = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeAtMaxPayload.getEstimatedValue() + " " +
				rangeAtMaxPayload.getUnit() + " \n\t@ Max TO Mass of " + 
				maxTakeOffMass.getEstimatedValue() + " " +
				maxTakeOffMass.getUnit() + "\n\t@ Max Payload Mass of " + 
				paxSingleMass.times(nPassMax).getEstimatedValue() + " " +
				paxSingleMass.getUnit() + " \n\t@ Fuel Mass of " + 
				fuelMass.getEstimatedValue() + " " +
				fuelMass.getUnit() + "\n\twith " + nPassMax + " passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cL + "\n\t\t cd = " + cD +
				"\n\t\t\t Efficiency = " + (cL/cD));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
	 * 
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @return range the range value in [nmi]
	 */
	public void calcRangeAtMaxFuel(boolean isBestRange) {	
		/* 
		 * first of all it's necessary to calculate the max fuel weight the airplane 
		 * can take on board @ MTOW.
		 */
		payloadMaxFuel = maxTakeOffMass.minus(operatingEmptyMass).minus(maxFuelMass);
		nPassActual = payloadMaxFuel.divide(paxSingleMass).getEstimatedValue();
		Amount<? extends Quantity> fuelFraction  = maxFuelMass.divide(maxTakeOffMass);
		double mff = 1-fuelFraction.getEstimatedValue();

		/*
		 * now it's necessary to evaluate the w0/wf ratio to be used in breguet
		 * formula in order to determine the range. This ratio isn't mff but
		 * the cruise (eventually loiter and diversion) ratio which has to be 
		 * derived from mff dividing by all other phases ratios.
		 */
		double breguetRatio = mffRatio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cL = pointE.get("CL_E");
			cD = pointE.get("CD_E");

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cL = pointA.get("CL_A");
			cD = pointA.get("CD_A");

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);

			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}

		rangeAtMaxFuel = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX FUEL");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeAtMaxPayload.getEstimatedValue() + " " +
				rangeAtMaxPayload.getUnit() + " \n\t@ Max TO Mass of " + 
				maxTakeOffMass.getEstimatedValue() + " " +
				maxTakeOffMass.getUnit() + "\n\t@ Payload Mass of " + 
				paxSingleMass.times(nPassActual).getEstimatedValue() + " " +
				paxSingleMass.getUnit() + " \n\t@ Max Fuel Mass of " + 
				maxFuelMass.getEstimatedValue() + " " +
				maxFuelMass.getUnit() + "\n\twith " + Math.ceil(nPassActual) + " passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cL + "\n\t\t cd = " + cD +
				"\n\t\t\t Efficiency = " + (cL/cD));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
	 * 
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @return range the range value in [nmi]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public void calcRangeAtZeroPayload(boolean isBestRange) {		
		/* 
		 * first of all it's necessary to calculate the max fuel weight the airplane 
		 * can take on board @ zero Payload.
		 */
		takeOffMass = maxFuelMass.plus(operatingEmptyMass);
		Amount<? extends Quantity> fuelFraction  = maxFuelMass.divide(takeOffMass);
		double mff = 1-fuelFraction.getEstimatedValue();

		/*
		 * now it's necessary to evaluate the w0/wf ratio to be used in breguet
		 * formula in order to determine the range. This ratio isn't mff but
		 * the cruise (eventually loiter and diversion) ratio which has to be 
		 * derived from mff dividing by all other phases ratios.
		 */
		double breguetRatio = mffRatio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cL = pointE.get("CL_E");
			cD = pointE.get("CD_E");

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfc,
					cL,
					cD,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cL = pointA.get("CL_A");
			cD = pointA.get("CD_A");

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);

			cD = DragCalc.calculateCDTotal(
					cD0,
					cL,
					ar,
					oswald,
					cruiseMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cL,
					cD,
					breguetRatio);
		}

		rangeAtZeroPayload = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ ZERO PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeAtMaxPayload.getEstimatedValue() + " " +
				rangeAtMaxPayload.getUnit() + " \n\t@ TO Mass of " + 
				takeOffMass.getEstimatedValue() + " " +
				takeOffMass.getUnit() + "\n\t@ Payload Mass of 0 " + 
				paxSingleMass.getUnit() + " \n\t@ Max Fuel Mass of " + 
				maxFuelMass.getEstimatedValue() + " " +
				maxFuelMass.getUnit() + "\n\twith 0 passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cL + "\n\t\t cd = " + cD +
				"\n\t\t\t Efficiency = " + (cL/cD));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
	}

	/******************************************************************************************
	 * Method that allows users to generate the Range array to be used in 
	 * Payload-Range plot.
	 * 
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * 
	 * @return vRange the List of Range values in [nmi]
	 */
	public List<Amount<Length>> createRangeArray(boolean isBestRange)
					throws HDF5LibraryException, NullPointerException{

		List<Amount<Length>> vRange = new ArrayList<Amount<Length>>();

		System.out.println("-----------BUILDING RANGE ARRAY COMPONENTS-----------------");
		System.out.println();
		
		calcRangeAtMaxPayload(isBestRange);
		calcRangeAtMaxFuel(isBestRange);
		calcRangeAtZeroPayload(isBestRange);
		
		// POINT 1
		vRange.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		vRange.add(rangeAtMaxPayload);
		// POINT 3
		vRange.add(rangeAtMaxFuel);
		// POINT 4
		vRange.add(rangeAtZeroPayload);

		return vRange;
	}

	/******************************************************************************************
	 * Method that allows users to generate Range and Payload matrices to be used in 
	 * Payload-Range plot parameterized in maxTakeOffMass.
	 * 
	 * @author Vittorio Trifari
	 * @param maxTakeOffMass
	 * @param sweepHalfChordEquivalent Sweep angle at 50% of the equivalent wing chord
	 * @param surface Wing surface in [m^2]
	 * @param cd0
	 * @param oswald Oswald factor of the wing
	 * @param cl
	 * @param ar Aspect ratio of the wing
	 * @param tcMax mean maximum thickness of the wing
	 * @param byPassRatio By Pass Ratio
	 * @param eta Propeller efficiency
	 * @param altitude 
	 * @param currentMach
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public void createPayloadRangeMatrices(boolean isBestRange)
					throws HDF5LibraryException, NullPointerException{
		
		System.out.println("-----------BUILDING RANGE MATRIX COMPONENTS-----------------");
		System.out.println();
		
		double[] massArrayMTOM = new double[5];
		rangeMatrix = new double [5][4];
		payloadMatrix = new double [5][4];
		
		// generating variation of mass of 5% until -20% of maxTakeOffMass
		for (int i=0; i<5; i++){
			massArrayMTOM[i] = maxTakeOffMass.getEstimatedValue()*(1-0.05*(4-i));
		}

		// setting the i-value of the mass array to the current maxTakeOffMass
		for (int i=0; i<5; i++){
			for (int j=0; j<4; j++){
				maxTakeOffMass_current = Amount.valueOf(massArrayMTOM[i], SI.KILOGRAM);
				switch (j){
				case 0:
					rangeMatrix[i][j] = 0.0;
					payloadMatrix[i][j] = nPassMax;
					break;
				case 1:
					rangeMatrix[i][j] =	calcRangeAtMaxPayload(
							isBestRange
							).getEstimatedValue();	
					payloadMatrix[i][j] = nPassMax;
					break;
				case 2:
					rangeMatrix[i][j] =	calcRangeAtMaxFuel(
							isBestRange
							).getEstimatedValue();
					payloadMatrix[i][j] = Math.round(getnPassActual());
					break;
				case 3:
					rangeMatrix[i][j] =	calcRangeAtZeroPayload(
							isBestRange
							).getEstimatedValue();
					payloadMatrix[i][j] = 0;
					break;
				}
			}
		}
		
		//////////////////////////////////////////
		//										//
		// TODO:  FIX ERRORS. ADD toString()	//
		//									    //
		//////////////////////////////////////////
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE MATRIX [nmi]");
		for (int i=0; i<rangeMatrix.length; i++){
			for (int j=0; j<rangeMatrix[0].length; j++){
				System.out.print(rangeMatrix[i][j] + " ");
			}
			System.out.println(" ");
		}
		System.out.println("-----------------------------------------------------------");
		System.out.println("PAYLOAD MATRIX [No. Pass]");
		for (int i=0; i<payloadMatrix.length; i++){
			for (int j=0; j<payloadMatrix[0].length; j++){
				System.out.print(payloadMatrix[i][j] + " ");
			}
			System.out.println(" ");
		}
		System.out.println("-----------------------------------------------------------");

		return;
	}
	
	/*******************************************************************************************
	 * Method that allows users to generate the Payload array to be used in 
	 * Payload-Range plot.
	 * 
	 * @author Vittorio Trifari
	 * @return vPayload the List of Payload values in [No. Pass]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public List<Double> createPayloadArray(){

		List<Double> vPayload = new ArrayList<Double>();

		System.out.println("-------------BUILDING PAYLOAD ARRAY COMPONENTS-------------");
		// POINT 1
		vPayload.add(nPassMax);
		// POINT 2
		vPayload.add(nPassMax);
		// POINT 3
		vPayload.add(Double.valueOf(Math.round(getnPassActual())));
		// POINT 4
		vPayload.add(0.);

		System.out.println("-----------------------------------------------------------");
		System.out.println("PAYLOAD [No. Pass]" + "\nPayload = " + vPayload);
		System.out.println("-----------------------------------------------------------");

		return vPayload;
	}
	
	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, for the best range
	 * Mach and the current one, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 * @param vRange_BR array of range values to plot (Best Range condition)
	 * @param vRange_CM array of range values to plot (Current Mach condition)
	 * @param vPayload array of payload (in No. Pass) to plot
	 * @param bestRangeMach the evaluated Mach number for Best Range condition
	 * @param currentMach the actual Mach number
	 */
	public void createPayloadRangeCharts_Mach(
			List<Amount<Length>> vRange_BR,
			List<Amount<Length>> vRange_CM,
			List<Double> vPayload,
			double bestRangeMach,
			double currentMach){

		System.out.println();
		System.out.println("------------WRITING PERFORMANCE CHART TO FILE--------------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "PayloadRange" + File.separator);

		double bestRangeDoubleArray[] = MyArrayUtils.convertListOfAmountTodoubleArray(vRange_BR);
		double currentRangeDoubleArray[] = MyArrayUtils.convertListOfAmountTodoubleArray(vRange_CM);
		double payloadDoubleArray[]= MyArrayUtils.convertToDoublePrimitive(vPayload);

		double[][] rangeDoubleArrays = new double[2][4];
		for (int i=0; i<4; i++){
			rangeDoubleArrays[0][i] = bestRangeDoubleArray[i];
			rangeDoubleArrays[1][i] = currentRangeDoubleArray[i];
		}
		double[][] payloadDoubleArrays = new double[2][4];
		for (int i=0; i<4; i++){
			payloadDoubleArrays[0][i] = payloadDoubleArray[i];
			payloadDoubleArrays[1][i] = payloadDoubleArray[i];
		}

		String[] legendValue = new String[2];
		legendValue[0] = "Best Range at Mach = " + bestRangeMach;
		legendValue[1] = "Current Condition at Mach = " + currentMach;

		MyChartToFileUtils.plot(
				rangeDoubleArrays, payloadDoubleArrays,	// array to plot
				null, null, 0.0, null,					    // axis with limits
				"Range", "Payload", "nmi", "No. Pass",	    // label with unit
				legendValue,								// legend
				subfolderPath, "PayloadRange_Mach");		// output informations
	}

	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, parameterized in
	 * maxTakeOffMass, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 * @param vRange_BR array of range values to plot (Best Range condition)
	 * @param vRange_CM array of range values to plot (Current Mach condition)
	 * @param vPayload array of payload (in No. Pass) to plot
	 * @param bestRangeMach the evaluated Mach number for Best Range condition
	 * @param cruiseMach the actual Mach number
	 */
	public void createPayloadRangeCharts_MaxTakeOffMass(
			double[][] rangeMatrix,
			double[][] payloadMatrix
			){

		System.out.println();
		System.out.println("------------WRITING PERFORMANCE CHART TO FILE--------------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "PayloadRange" + File.separator);

		double[] massArray = new double[11];
		
		// generating variation of mass of 5% until -20% of maxTakeOffMass
		for (int i=0; i<5; i++){
			massArray[i] = maxTakeOffMass.getEstimatedValue()*(1-0.05*(4-i));
		}
		
		MyChartToFileUtils.plot(
				rangeMatrix, payloadMatrix,						// array to plot
				0.0, null, 0.0, null,					    	// axis with limits
				"Range", "Payload", "nmi", "No. Pass",	    	// label with unit
				"MTOM = ", massArray, " Kg ",										// legend
				subfolderPath, "PayloadRange_MaxTakeOffMass");  // output informations
	}

	
	//----------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public Aircraft getTheAircraft() {
		return theAircraft;
	}

	public void setTheAircraft(Aircraft theAircraft) {
		this.theAircraft = theAircraft;
	}

	public Amount<Mass> getPaxSingleMass() {
		return paxSingleMass;
	}

	public void setPaxSingleMass(Amount<Mass> paxSingleMass) {
		this.paxSingleMass = paxSingleMass;
	}

	public Amount<Mass> getMaxTakeOffMass() {
		return maxTakeOffMass;
	}

	public void setMaxTakeOffMass(Amount<Mass> maxTakeOffMass) {
		this.maxTakeOffMass = maxTakeOffMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> operatingEmptyMass) {
		this.operatingEmptyMass = operatingEmptyMass;
	}

	public Amount<Angle> getSweepLEEquivalent() {
		return sweepLEEquivalent;
	}

	public void setSweepLEEquivalent(Amount<Angle> sweepLEEquivalent) {
		this.sweepLEEquivalent = sweepLEEquivalent;
	}

	public Amount<Angle> getSweepHalfChordEquivalent() {
		return sweepHalfChordEquivalent;
	}

	public void setSweepHalfChordEquivalent(Amount<Angle> sweepHalfChordEquivalent) {
		this.sweepHalfChordEquivalent = sweepHalfChordEquivalent;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public Amount<Length> getAltitude() {
		return altitude;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public FuelFractionDatabaseReader getFuelFractionDatabase() {
		return fuelFractionDatabase;
	}

	public void setFuelFractionDatabase(FuelFractionDatabaseReader fuelFractionDatabase) {
		this.fuelFractionDatabase = fuelFractionDatabase;
	}

	public double getcD0() {
		return cD0;
	}

	public void setcD0(double cD0) {
		this.cD0 = cD0;
	}

	public double getOswald() {
		return oswald;
	}

	public void setOswald(double oswald) {
		this.oswald = oswald;
	}

	public double getcL() {
		return cL;
	}

	public void setcL(double cL) {
		this.cL = cL;
	}

	public double getAr() {
		return ar;
	}

	public void setAr(double ar) {
		this.ar = ar;
	}

	public double getTcMax() {
		return tcMax;
	}

	public void setTcMax(double tcMax) {
		this.tcMax = tcMax;
	}

	public double getByPassRatio() {
		return byPassRatio;
	}

	public void setByPassRatio(double byPassRatio) {
		this.byPassRatio = byPassRatio;
	}

	public double getCruiseMach() {
		return cruiseMach;
	}

	public void setCruiseMach(double cruiseMach) {
		this.cruiseMach = cruiseMach;
	}

	public double getnPassMax() {
		return nPassMax;
	}

	public void setnPassMax(double nPassMax) {
		this.nPassMax = nPassMax;
	}

	public double getEtaPropeller() {
		return etaPropeller;
	}

	public void setEtaPropeller(double etaPropeller) {
		this.etaPropeller = etaPropeller;
	}

	public double getSfc() {
		return sfc;
	}

	public void setSfc(double sfc) {
		this.sfc = sfc;
	}

	public AirfoilTypeEnum getAirfoilType() {
		return airfoilType;
	}

	public void setAirfoilType(AirfoilTypeEnum airfoilType) {
		this.airfoilType = airfoilType;
	}

	public AircraftTypeEnum getAircraftType() {
		return aircraftType;
	}

	public void setAircraftType(AircraftTypeEnum aircraftType) {
		this.aircraftType = aircraftType;
	}

	public EngineTypeEnum getEngineType() {
		return engineType;
	}

	public void setEngineType(EngineTypeEnum engineType) {
		this.engineType = engineType;
	}

	public double[][] getFuelFractionTable() {
		return fuelFractionTable;
	}

	public void setFuelFractionTable(double[][] fuelFractionTable) {
		this.fuelFractionTable = fuelFractionTable;
	}

	public Amount<Mass> getFuelMass() {
		return fuelMass;
	}

	public void setFuelMass(Amount<Mass> fuelMass) {
		this.fuelMass = fuelMass;
	}

	public Amount<Mass> getMaxFuelMass() {
		return maxFuelMass;
	}

	public void setMaxFuelMass(Amount<Mass> maxFuelMass) {
		this.maxFuelMass = maxFuelMass;
	}

	public Amount<Mass> getTakeOffMass() {
		return takeOffMass;
	}

	public void setTakeOffMass(Amount<Mass> takeOffMass) {
		this.takeOffMass = takeOffMass;
	}

	public Amount<Mass> getPayloadMaxFuel() {
		return payloadMaxFuel;
	}

	public void setPayloadMaxFuel(Amount<Mass> payloadMaxFuel) {
		this.payloadMaxFuel = payloadMaxFuel;
	}

	public Amount<Mass> getMaxTakeOffMass_current() {
		return maxTakeOffMass_current;
	}

	public void setMaxTakeOffMass_current(Amount<Mass> maxTakeOffMass_current) {
		this.maxTakeOffMass_current = maxTakeOffMass_current;
	}

	public Amount<Length> getRange() {
		return rangeAtMaxPayload;
	}

	public void setRange(Amount<Length> range) {
		this.rangeAtMaxPayload = range;
	}

	public double getRangeBreguet() {
		return rangeBreguet;
	}

	public void setRangeBreguet(double rangeBreguet) {
		this.rangeBreguet = rangeBreguet;
	}

	public double getBestRangeMach() {
		return bestRangeMach;
	}

	public void setBestRangeMach(double bestRangeMach) {
		this.bestRangeMach = bestRangeMach;
	}

	public double getnPassActual() {
		return nPassActual;
	}

	public void setnPassActual(double nPassActual) {
		this.nPassActual = nPassActual;
	}

	public double getcD() {
		return cD;
	}

	public void setcD(double cD) {
		this.cD = cD;
	}

	public double getCriticalMach() {
		return criticalMach;
	}

	public void setCriticalMach(double criticalMach) {
		this.criticalMach = criticalMach;
	}

	public double getMffRatio() {
		return mffRatio;
	}

	public void setMffRatio(double mffRatio) {
		this.mffRatio = mffRatio;
	}

	public double[][] getRangeMatrix() {
		return rangeMatrix;
	}

	public void setRangeMatrix(double[][] rangeMatrix) {
		this.rangeMatrix = rangeMatrix;
	}

	public double[][] getPayloadMatrix() {
		return payloadMatrix;
	}

	public void setPayloadMatrix(double[][] payloadMatrix) {
		this.payloadMatrix = payloadMatrix;
	}

	public Map<String,Double> getPointE() {
		return pointE;
	}

	public void setPointE(Map<String,Double> pointE) {
		this.pointE = pointE;
	}

	public Map<String,Double> getPointP() {
		return pointP;
	}

	public void setPointP(Map<String,Double> pointP) {
		this.pointP = pointP;
	}

	public Map<String,Double> getPointA() {
		return pointA;
	}

	public void setPointA(Map<String,Double> pointA) {
		this.pointA = pointA;
	}

	public Amount<Length> getRangeAtMaxFuel() {
		return rangeAtMaxFuel;
	}

	public void setRangeAtMaxFuel(Amount<Length> rangeAtMaxFuel) {
		this.rangeAtMaxFuel = rangeAtMaxFuel;
	}

	public Amount<Length> getRangeAtZeroPayload() {
		return rangeAtZeroPayload;
	}

	public void setRangeAtZeroPayload(Amount<Length> rangeAtZeroPayload) {
		this.rangeAtZeroPayload = rangeAtZeroPayload;
	}
}