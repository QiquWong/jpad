package calculators.performance;

import java.io.File;
import java.util.ArrayList;
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
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseManager;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

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

public class PayloadRangeCalcBreguet{
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	
	// INPUT DATA:
	private Aircraft theAircraft;
	private OperatingConditions theOperatingConditions;
	private Amount<Mass> paxSingleMass;
	private Amount<Mass> maxTakeOffMass;
	private Amount<Mass> operatingEmptyMass; 
	private Amount<Angle> sweepHalfChordEquivalent;
	private Amount<Area> surface;
	private Amount<Length> altitude;
	private FuelFractionDatabaseReader fuelFractionDatabase;
	private double cD0;
	private Double[] polarCL;
	private Double[] polarCD;
	private double oswald;
	private double ar;
	private double tcMax;
	private double byPassRatio;
	private double cruiseMach;
	private double nPassMax;
	private double etaPropeller;

	private AirfoilTypeEnum airfoilType;	
	private AircraftTypeEnum aircraftType;
	private EngineTypeEnum engineType;
	private double[][] fuelFractionTable;

	// TO EVALUATE:
	private Amount<Mass> fuelMass;
	private Amount<Mass> maxFuelMass;
	private Amount<Mass> takeOffMass;
	private Amount<Mass> payloadMaxFuel;
	private double mffRatio;
	
	private double bestRangeMach;
	private double criticalMach;
	private double nPassActual;
	
	private Amount<Length> rangeAtMaxPayloadBestRange;
	private Amount<Length> rangeAtMaxFuelBestRange;
	private Amount<Length> rangeAtZeroPayloadBestRange;
	private Amount<Length> rangeAtMaxPayloadCurrentMach;
	private Amount<Length> rangeAtMaxFuelCurrentMach;
	private Amount<Length> rangeAtZeroPayloadCurrentMach;
	
	private double cLAtMaxPayloadBestRange;
	private double cLAtMaxFuelBestRange;
	private double cLAtZeroPayloadBestRange;
	private double cLAtMaxPayloadCurrentMach;
	private double cLAtMaxFuelCurrentMach;
	private double cLAtZeroPayloadCurrentMach;
	
	private double cDAtMaxPayloadBestRange;
	private double cDAtMaxFuelBestRange;
	private double cDAtZeroPayloadBestRange;
	private double cDAtMaxPayloadCurrentMach;
	private double cDAtMaxFuelCurrentMach;
	private double cDAtZeroPayloadCurrentMach;
	
	private double efficiencyAtMaxPayloadBestRange;
	private double efficiencyAtMaxFuelBestRange;
	private double efficiencyAtZeroPayloadBestRange;
	private double efficiencyAtMaxPayloadCurrentMach;
	private double efficiencyAtMaxFuelCurrentMach;
	private double efficiencyAtZeroPayloadCurrentMach;
	
	private double sfcAtMaxPayloadBestRange;
	private double sfcAtMaxFuelBestRange;	
	private double sfcAtZeroPayloadBestRange;
	private double sfcAtMaxPayloadCurrentMach;
	private double sfcAtMaxFuelCurrentMach;	
	private double sfcAtZeroPayloadCurrentMach;
	
	private List<Amount<Length>> rangeArrayBestRange;
	private List<Amount<Length>> rangeArrayCurrentMach;
	private List<Double> payloadArray;
	
	private double[][] rangeMatrix;
	private double[][] payloadMatrix;

	//-------------------------------------------------------------------------------------
	// BUILDER
	
	public PayloadRangeCalcBreguet (
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> maxFuelMass,
			Double[] polarCL,
			Double[] polarCD,
			double cruiseMach,
			Amount<Length> altitude,
			Amount<Mass> passengerSingleMass
			) {
		
		this.theAircraft = theAircraft;
		this.theOperatingConditions = theOperatingConditions; 
		this.maxTakeOffMass = maxTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.maxFuelMass = maxFuelMass;
		this.nPassMax = theAircraft.getCabinConfiguration().getMaxPax();
		this.paxSingleMass = passengerSingleMass;				
		
		this.surface = theAircraft.getWing().getSurface();
		this.ar = theAircraft.getWing().getAspectRatio();
		this.cruiseMach = cruiseMach;
		this.altitude = altitude;
		this.sweepHalfChordEquivalent = theAircraft.getWing().getSweepHalfChordEquivalent(false);
		this.polarCL = polarCL;
		this.polarCD = polarCD;
		this.cD0 = MyArrayUtils.getMin(this.polarCD);
		this.etaPropeller = theAircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller();
		
		Airfoil meanAirfoil = new Airfoil(
				LiftingSurface.calculateMeanAirfoil(theAircraft.getWing()),
				theAircraft.getWing().getAerodynamicDatabaseReader()
				);
		this.tcMax = meanAirfoil.getAirfoilCreator().getThicknessToChordRatio();
		
		this.aircraftType = theAircraft.getTypeVehicle();
		this.engineType = theAircraft.getPowerPlant().getEngineType();
		this.airfoilType = theAircraft.getWing().getAirfoilList().get(0).getType();
		
		///////////////////////////////////////////////////////////////////////////////////////
		// Defining the fuel fraction ratio as function of the aircraft type:
		this.fuelFractionDatabase = new FuelFractionDatabaseReader(
				System.getProperty("user.dir")
					+ File.separator 
					+ MyConfiguration.databaseFolderPath, 
				"FuelFractions.h5"
				);
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
		for (int i=0; i<5; i++) {
			System.out.println("PRODOTTO = " + fuelFractionTable[indexTurboprop][i]);
			mffRatio *= fuelFractionTable[indexTurboprop][i];
		}
		break;

		case JET: int indexJet = 6;
		for (int i=0; i<5; i++)
			mffRatio *= fuelFractionTable[indexJet][i]; 
		break;

		case BUSINESS_JET: int indexBusinessJet = 4;
		for (int i=0; i<5; i++)
			mffRatio *= fuelFractionTable[indexBusinessJet][i]; 
		break;

		case FIGHTER: int indexFighter = 8;
		for (int i=0; i<5; i++)
			mffRatio *= fuelFractionTable[indexFighter][i]; 
		break;

		case ACROBATIC: int indexAcrobatic = 7;
		for (int i=0; i<5; i++)
			mffRatio *= fuelFractionTable[indexAcrobatic][i]; 
		break;

		case COMMUTER: int indexCommuter = 5;
		for (int i=0; i<5; i++)
			mffRatio *= fuelFractionTable[indexCommuter][i]; 
		break;

		case GENERAL_AVIATION:
		if(theAircraft.getPowerPlant().getEngineNumber() == 1) {
			int indexSingleEngine = 1;	
			for (int i=0; i<5; i++)
				mffRatio *= fuelFractionTable[indexSingleEngine][i];
		}
		else if(theAircraft.getPowerPlant().getEngineNumber() == 2) {
			int indexTwinEngine = 2;
			for (int i=0; i<5; i++)
				mffRatio *= fuelFractionTable[indexTwinEngine][i];
		}
		break;
		}
		
		///////////////////////////////////////////////////////////////////////////////////////
		// Check if cruise Mach number is lower than the critical Mach number
		boolean criticalMachCheck = checkCriticalMach(cruiseMach);
		if(criticalMachCheck == false) {
			System.err.println("WARNING CURRENT MACH NUMBER BIGGER THAN CRITICAL MACH NUMBER!! \n\t\t--> TERMINATING");
			return;
		}
		
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
	private void calculateBestRangeMach(){
		
		if (engineType == EngineTypeEnum.PISTON  ||
				engineType == EngineTypeEnum.TURBOPROP) {
			
			Map<String,Double> pointE = DragCalc.calculateMaximumEfficiency(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			if(oswald != 0.0)
				bestRangeMach = SpeedCalc.calculateMach(
						altitude.doubleValue(SI.METER),
						pointE.get("Speed_E")
						);
			else
				bestRangeMach = 0.0;
		}
		else if (engineType == EngineTypeEnum.TURBOJET ||
				engineType == EngineTypeEnum.TURBOFAN ||
				engineType == EngineTypeEnum.RAMJET   ||
				engineType == EngineTypeEnum.PROPFAN)  {
			
			Map<String,Double> pointA = DragCalc.calculateMaximumRange(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			if(oswald != 0.0)
				bestRangeMach = SpeedCalc.calculateMach(
						altitude.doubleValue(SI.METER),
						pointA.get("Speed_A"));
			else
				bestRangeMach = 0.0;
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
		
		double cL = LiftCalc.calculateLiftCoeff(
				maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
				SpeedCalc.calculateTAS(
						cruiseMach,
						altitude.doubleValue(SI.METER)
						),
				surface.doubleValue(SI.SQUARE_METRE),
				altitude.doubleValue(SI.METER)
				);
		
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
	private Amount<Length> calcRangeAtMaxPayload(
			Amount<Mass> maxTakeOffMassCurrent,
			boolean isBestRange
			) {	
		
		/* 
		 * first of all it's necessary to calculate the fuel weight the airplane 
		 * can take on board @ MTOW and max Payload.
		 */
		fuelMass = maxTakeOffMassCurrent.minus(operatingEmptyMass).minus(paxSingleMass.times(nPassMax));
		Amount<? extends Quantity> fuelFraction  = fuelMass.divide(maxTakeOffMassCurrent);
		double mff = 1-fuelFraction.getEstimatedValue();

		/*
		 * now it's necessary to evaluate the w0/wf ratio to be used in breguet
		 * formula in order to determine the range. This ratio isn't mff but
		 * the cruise (eventually loiter and diversion) ratio which has to be 
		 * derived from mff dividing by all other phases ratios.
		 */
		double breguetRatio = mffRatio/mff; 
		double rangeBreguet = 0.0;

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtMaxPayloadBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);
			
			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointE = DragCalc.calculateMaximumEfficiency(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtMaxPayloadBestRange = pointE.get("CL_E");
			cDAtMaxPayloadBestRange = pointE.get("CD_E");
			
			efficiencyAtMaxPayloadBestRange = cLAtMaxPayloadBestRange/cDAtMaxPayloadBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtMaxPayloadBestRange,
					cLAtMaxPayloadBestRange,
					cDAtMaxPayloadBestRange,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtMaxPayloadCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);
			
			cLAtMaxPayloadCurrentMach = LiftCalc.calculateLiftCoeff(
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtMaxPayloadCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtMaxPayloadCurrentMach
					);

			efficiencyAtMaxPayloadCurrentMach = cLAtMaxPayloadCurrentMach/cDAtMaxPayloadCurrentMach;
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtMaxPayloadCurrentMach,
					cLAtMaxPayloadCurrentMach,
					cDAtMaxPayloadCurrentMach,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {
			
			sfcAtMaxPayloadBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);
			
			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointA = DragCalc.calculateMaximumRange(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtMaxPayloadBestRange = pointA.get("CL_A");
			cDAtMaxPayloadBestRange = pointA.get("CD_A");
			
			efficiencyAtMaxPayloadBestRange = cLAtMaxPayloadBestRange/cDAtMaxPayloadBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtMaxPayloadBestRange,
					cLAtMaxPayloadBestRange,
					cDAtMaxPayloadBestRange,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfcAtMaxPayloadCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);
			
			cLAtMaxPayloadCurrentMach = LiftCalc.calculateLiftCoeff(
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtMaxPayloadCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtMaxPayloadCurrentMach
					);
						
			efficiencyAtMaxPayloadCurrentMach = cLAtMaxPayloadCurrentMach/cDAtMaxPayloadCurrentMach;
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtMaxPayloadCurrentMach,
					cLAtMaxPayloadCurrentMach,
					cDAtMaxPayloadCurrentMach,
					breguetRatio);
		}
	
		return Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
	 * 
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @return range the range value in [nmi]
	 */
	private Amount<Length> calcRangeAtMaxFuel(
			Amount<Mass> maxTakeOffMassCurrent,
			boolean isBestRange
			) {	
		/* 
		 * first of all it's necessary to calculate the max fuel weight the airplane 
		 * can take on board @ MTOW.
		 */
		payloadMaxFuel = maxTakeOffMassCurrent.minus(operatingEmptyMass).minus(maxFuelMass);
		nPassActual = payloadMaxFuel.divide(paxSingleMass).getEstimatedValue();
		Amount<? extends Quantity> fuelFraction  = maxFuelMass.divide(maxTakeOffMassCurrent);
		double mff = 1-fuelFraction.getEstimatedValue();

		/*
		 * now it's necessary to evaluate the w0/wf ratio to be used in breguet
		 * formula in order to determine the range. This ratio isn't mff but
		 * the cruise (eventually loiter and diversion) ratio which has to be 
		 * derived from mff dividing by all other phases ratios.
		 */
		double breguetRatio = mffRatio/mff; 
		double rangeBreguet = 0.0;
		
		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtMaxFuelBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointE = DragCalc.calculateMaximumEfficiency(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtMaxFuelBestRange = pointE.get("CL_E");
			cDAtMaxFuelBestRange = pointE.get("CD_E");
			
			efficiencyAtMaxFuelBestRange = cLAtMaxFuelBestRange/cDAtMaxFuelBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtMaxFuelBestRange,
					cLAtMaxFuelBestRange,
					cDAtMaxFuelBestRange,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtMaxFuelCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			cLAtMaxFuelCurrentMach = LiftCalc.calculateLiftCoeff(
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtMaxFuelCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtMaxFuelCurrentMach
					);

			efficiencyAtMaxFuelCurrentMach = cLAtMaxFuelCurrentMach/cDAtMaxFuelCurrentMach;
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtMaxFuelCurrentMach,
					cLAtMaxFuelCurrentMach,
					cDAtMaxFuelCurrentMach,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfcAtMaxFuelBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointA = DragCalc.calculateMaximumRange(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtMaxFuelBestRange = pointA.get("CL_A");
			cDAtMaxFuelBestRange = pointA.get("CD_A");
			
			efficiencyAtMaxFuelBestRange = cLAtMaxFuelBestRange/cDAtMaxFuelBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
					
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtMaxFuelBestRange,
					cLAtMaxFuelBestRange,
					cDAtMaxFuelBestRange,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfcAtMaxFuelCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			cLAtMaxFuelCurrentMach = LiftCalc.calculateLiftCoeff(
					maxTakeOffMassCurrent.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtMaxFuelCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtMaxFuelCurrentMach
					);

			efficiencyAtMaxFuelCurrentMach = cLAtMaxFuelCurrentMach/cDAtMaxFuelCurrentMach;
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtMaxFuelCurrentMach,
					cLAtMaxFuelCurrentMach,
					cDAtMaxFuelCurrentMach,
					breguetRatio);
		}

		return Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
	 * 
	 * @author Vittorio Trifari
	 * @param isBestRange This value is true if it's a best range condition, otherwise it's false
	 * @return range the range value in [nmi]
	 */
	private Amount<Length> calcRangeAtZeroPayload(boolean isBestRange) {		
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
		double rangeBreguet = 0.0;

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtZeroPayloadBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointE = DragCalc.calculateMaximumEfficiency(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					takeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtZeroPayloadBestRange = pointE.get("CL_E");
			cDAtZeroPayloadBestRange = pointE.get("CD_E");
			
			efficiencyAtZeroPayloadBestRange = cLAtZeroPayloadBestRange/cDAtZeroPayloadBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtZeroPayloadBestRange,
					cLAtZeroPayloadBestRange,
					cDAtZeroPayloadBestRange,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfcAtZeroPayloadCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			cLAtZeroPayloadCurrentMach = LiftCalc.calculateLiftCoeff(
					takeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtZeroPayloadCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtZeroPayloadCurrentMach
					);

			efficiencyAtZeroPayloadCurrentMach = cLAtZeroPayloadCurrentMach/cDAtZeroPayloadCurrentMach;
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					etaPropeller,
					sfcAtZeroPayloadCurrentMach,
					cLAtZeroPayloadCurrentMach,
					cDAtZeroPayloadCurrentMach,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfcAtZeroPayloadBestRange = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			////////////////////////////////////////////////////////////////////////////////////
			// Calculating the drag polar characteristic points of interest
			Map<String, Double> pointA = DragCalc.calculateMaximumRange(
					theAircraft.getWing().getAspectRatio(),
					oswald,
					cD0,
					theOperatingConditions.getDensityCruise().getEstimatedValue(),
					takeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE)
					);
			
			cLAtZeroPayloadBestRange = pointA.get("CL_A");
			cDAtZeroPayloadBestRange = pointA.get("CD_A");
			
			efficiencyAtZeroPayloadBestRange = cLAtZeroPayloadBestRange/cDAtZeroPayloadBestRange;
			
			////////////////////////////////////////////////////////////////////////////////////
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtZeroPayloadBestRange,
					cLAtZeroPayloadBestRange,
					cDAtZeroPayloadBestRange,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfcAtZeroPayloadCurrentMach = EngineDatabaseManager.getSFC(
					cruiseMach,
					altitude.doubleValue(SI.METER),
					EngineDatabaseManager.getThrustRatio(
							cruiseMach,
							altitude.doubleValue(SI.METER),
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE,
							theAircraft.getPowerPlant()
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE,
					theAircraft.getPowerPlant()
					);

			cLAtZeroPayloadCurrentMach = LiftCalc.calculateLiftCoeff(
					takeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
					SpeedCalc.calculateTAS(
							cruiseMach,
							altitude.doubleValue(SI.METER)
							),
					surface.doubleValue(SI.SQUARE_METRE),
					altitude.doubleValue(SI.METER)
					);
			
			cDAtZeroPayloadCurrentMach = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(polarCL),
					MyArrayUtils.convertToDoublePrimitive(polarCD),
					cLAtZeroPayloadCurrentMach
					);

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									cruiseMach,
									altitude.doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfcAtZeroPayloadCurrentMach,
					cLAtZeroPayloadCurrentMach,
					cDAtZeroPayloadCurrentMach,
					breguetRatio);
		}
		
		return Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
	}

	/******************************************************************************************
	 * Method that allows users to generate the Range array to be used in 
	 * Payload-Range plot.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeMachComparison() {
		
		//--------------------------------------------------------------------------------------
		// BEST RANGE CONDITION
		rangeArrayBestRange = new ArrayList<>();

		rangeAtMaxPayloadBestRange = calcRangeAtMaxPayload(maxTakeOffMass, true);
		rangeAtMaxFuelBestRange = calcRangeAtMaxFuel(maxTakeOffMass, true);
		rangeAtZeroPayloadBestRange = calcRangeAtZeroPayload(true);

		// POINT 1
		rangeArrayBestRange.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		rangeArrayBestRange.add(rangeAtMaxPayloadBestRange);
		// POINT 3
		rangeArrayBestRange.add(rangeAtMaxFuelBestRange);
		// POINT 4
		rangeArrayBestRange.add(rangeAtZeroPayloadBestRange);

		//--------------------------------------------------------------------------------------
		// CURRENT MACH CONDITION
		rangeArrayCurrentMach = new ArrayList<>();
		
		rangeAtMaxPayloadCurrentMach = calcRangeAtMaxPayload(maxTakeOffMass, false);
		rangeAtMaxFuelCurrentMach = calcRangeAtMaxFuel(maxTakeOffMass, false);
		rangeAtZeroPayloadCurrentMach = calcRangeAtZeroPayload(false);
		
		// POINT 1
		rangeArrayCurrentMach.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		rangeArrayCurrentMach.add(rangeAtMaxPayloadCurrentMach);
		// POINT 3
		rangeArrayCurrentMach.add(rangeAtMaxFuelCurrentMach);
		// POINT 4
		rangeArrayCurrentMach.add(rangeAtZeroPayloadCurrentMach);
		
		//--------------------------------------------------------------------------------------
		// PAYLOAD ARRAY (both conditions)
		payloadArray = new ArrayList<Double>();
		
		// POINT 1
		payloadArray.add(nPassMax);
		// POINT 2
		payloadArray.add(nPassMax);
		// POINT 3
		payloadArray.add(Double.valueOf(Math.round(getnPassActual())));
		// POINT 4
		payloadArray.add(0.);

	}

	/******************************************************************************************
	 * Method that allows users to generate Range and Payload matrices to be used in 
	 * Payload-Range plot parameterized in maxTakeOffMass.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeMaxTakeOffMassParameterization() {
		
		double[] massArrayMTOM = new double[5];
		Amount<Mass> maxTakeOffMassCurrent = Amount.valueOf(0.0, SI.KILOGRAM);
		rangeMatrix = new double [5][4];
		payloadMatrix = new double [5][4];
		
		// generating variation of mass of 5% until -20% of maxTakeOffMass
		for (int i=0; i<5; i++){
			massArrayMTOM[i] = maxTakeOffMass.getEstimatedValue()*(1-0.05*(4-i));
		}

		// setting the i-value of the mass array to the current maxTakeOffMass
		for (int i=0; i<5; i++){
			for (int j=0; j<4; j++){
				maxTakeOffMassCurrent = Amount.valueOf(massArrayMTOM[i], SI.KILOGRAM);
				switch (j){
				case 0:
					rangeMatrix[i][j] = 0.0;
					payloadMatrix[i][j] = nPassMax;
					break;
				case 1:
					rangeMatrix[i][j] =	calcRangeAtMaxPayload(
							maxTakeOffMassCurrent,
							false
							).getEstimatedValue();	
					payloadMatrix[i][j] = nPassMax;
					break;
				case 2:
					rangeMatrix[i][j] =	calcRangeAtMaxFuel(
							maxTakeOffMassCurrent,
							false
							).getEstimatedValue();
					payloadMatrix[i][j] = Math.round(getnPassActual());
					break;
				case 3:
					rangeMatrix[i][j] =	calcRangeAtZeroPayload(
							false
							).getEstimatedValue();
					payloadMatrix[i][j] = 0;
					break;
				}
			}
		}
	
		return;
	}
	
	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, for the best range
	 * Mach and the current one, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeChartsMachParameterization(String subFolderPath){

		double bestRangeDoubleArray[] = MyArrayUtils.convertListOfAmountTodoubleArray(rangeArrayBestRange);
		double currentRangeDoubleArray[] = MyArrayUtils.convertListOfAmountTodoubleArray(rangeArrayCurrentMach);
		double payloadDoubleArray[]= MyArrayUtils.convertToDoublePrimitive(payloadArray);

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
		legendValue[1] = "Current Condition at Mach = " + cruiseMach;

		MyChartToFileUtils.plot(
				rangeDoubleArrays, payloadDoubleArrays,		// array to plot
				null, null, 0.0, null,					    // axis with limits
				"Range", "Payload", "nmi", "No. Pass",	    // label with unit
				legendValue,								// legend
				subFolderPath, "Payload-Range_Mach");		// output informations
	}

	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, parameterized in
	 * maxTakeOffMass, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeChartsMaxTakeOffMassParameterization(String subFolderPath){
		
		double[] massArray = new double[11];
		
		// generating variation of mass of 5% until -20% of maxTakeOffMass
		for (int i=0; i<5; i++){
			massArray[i] = maxTakeOffMass.getEstimatedValue()*(1-0.05*(4-i));
		}
		
		MyChartToFileUtils.plot(
				rangeMatrix, payloadMatrix,						// array to plot
				0.0, null, 0.0, null,					    	// axis with limits
				"Range", "Payload", "nmi", "No. Pass",	    	// label with unit
				"MTOM = ", massArray, " Kg ",					// legend
				subFolderPath, "Payload-Range_MaxTakeOffMass"); // output informations
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\t\tRANGE AT MAX PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + rangeAtMaxPayloadCurrentMach + "\n")
				.append("\t\t\tMax take-off mass = " + maxTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + paxSingleMass.times(nPassMax) + "\n")
				.append("\t\t\tPassengers number = " + nPassMax + "\n")
				.append("\t\t\tFuel mass = " + fuelMass + "\n")
				.append("\t\t\tSFC = " + sfcAtMaxPayloadCurrentMach + "\n")
				.append("\t\t\tCL = " + cLAtMaxPayloadCurrentMach + "\n")
				.append("\t\t\tCD = " + cDAtMaxPayloadCurrentMach + "\n")
				.append("\t\t\tEfficiency = " + efficiencyAtMaxPayloadCurrentMach + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT MAX FUEL\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + rangeAtMaxFuelCurrentMach + "\n")
				.append("\t\t\tMax take-off mass = " + maxTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + paxSingleMass.times(nPassActual) + "\n")
				.append("\t\t\tPassengers number = " + nPassActual + "\n")
				.append("\t\t\tFuel mass = " + maxFuelMass + "\n")
				.append("\t\t\tSFC = " + sfcAtMaxFuelCurrentMach + "\n")
				.append("\t\t\tCL = " + cLAtMaxFuelCurrentMach + "\n")
				.append("\t\t\tCD = " + cDAtMaxFuelCurrentMach + "\n")
				.append("\t\t\tEfficiency = " + efficiencyAtMaxFuelCurrentMach + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT ZERO PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + rangeAtZeroPayloadCurrentMach + "\n")
				.append("\t\t\tMax take-off mass = " + takeOffMass + "\n")
				.append("\t\t\tPayload mass = " + Amount.valueOf(0.0, SI.KILOGRAM) + "\n")
				.append("\t\t\tPassengers number = " + 0 + "\n")
				.append("\t\t\tFuel mass = " + maxFuelMass + "\n")
				.append("\t\t\tSFC = " + sfcAtZeroPayloadCurrentMach + "\n")
				.append("\t\t\tCL = " + cLAtZeroPayloadCurrentMach + "\n")
				.append("\t\t\tCD = " + cDAtZeroPayloadCurrentMach + "\n")
				.append("\t\t\tEfficiency = " + efficiencyAtZeroPayloadCurrentMach + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE MATRIX (WEIGHT PARAMETERIZATION)\n")
				.append("\t\t.....................................\n");
		
		for (int i=0; i<rangeMatrix.length; i++){
			sb.append("\t\t\t");
			for (int j=0; j<rangeMatrix[0].length; j++)
				sb.append(rangeMatrix[i][j] + ", ");
			sb.append("\n");
		}
		
		sb.append("\t\t.....................................\n")
		.append("\t\tPAYLOAD MATRIX [passengers number] (WEIGHT PARAMETERIZATION)\n")
		.append("\t\t.....................................\n");
		
		for (int i=0; i<payloadMatrix.length; i++){
			sb.append("\t\t\t");
			for (int j=0; j<payloadMatrix[0].length; j++)
				sb.append(payloadMatrix[i][j] + ", ");
			sb.append("\n");
		}
		
		sb.append("\t-------------------------------------\n");
		
		return sb.toString();
	}
	//----------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public Aircraft getTheAircraft() {
		return theAircraft;
	}
	public void setTheAircraft(Aircraft theAircraft) {
		this.theAircraft = theAircraft;
	}
	public OperatingConditions getTheOperatingConditions() {
		return theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions theOperatingConditions) {
		this.theOperatingConditions = theOperatingConditions;
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
	public double getMffRatio() {
		return mffRatio;
	}
	public void setMffRatio(double mffRatio) {
		this.mffRatio = mffRatio;
	}
	public double getBestRangeMach() {
		return bestRangeMach;
	}
	public void setBestRangeMach(double bestRangeMach) {
		this.bestRangeMach = bestRangeMach;
	}
	public double getCriticalMach() {
		return criticalMach;
	}
	public void setCriticalMach(double criticalMach) {
		this.criticalMach = criticalMach;
	}
	public double getnPassActual() {
		return nPassActual;
	}
	public void setnPassActual(double nPassActual) {
		this.nPassActual = nPassActual;
	}
	public Amount<Length> getRangeAtMaxPayloadBestRange() {
		return rangeAtMaxPayloadBestRange;
	}
	public void setRangeAtMaxPayloadBestRange(Amount<Length> rangeAtMaxPayloadBestRange) {
		this.rangeAtMaxPayloadBestRange = rangeAtMaxPayloadBestRange;
	}
	public Amount<Length> getRangeAtMaxFuelBestRange() {
		return rangeAtMaxFuelBestRange;
	}
	public void setRangeAtMaxFuelBestRange(Amount<Length> rangeAtMaxFuelBestRange) {
		this.rangeAtMaxFuelBestRange = rangeAtMaxFuelBestRange;
	}
	public Amount<Length> getRangeAtZeroPayloadBestRange() {
		return rangeAtZeroPayloadBestRange;
	}
	public void setRangeAtZeroPayloadBestRange(Amount<Length> rangeAtZeroPayloadBestRange) {
		this.rangeAtZeroPayloadBestRange = rangeAtZeroPayloadBestRange;
	}
	public Amount<Length> getRangeAtMaxPayloadCurrentMach() {
		return rangeAtMaxPayloadCurrentMach;
	}
	public void setRangeAtMaxPayloadCurrentMach(Amount<Length> rangeAtMaxPayloadCurrentMach) {
		this.rangeAtMaxPayloadCurrentMach = rangeAtMaxPayloadCurrentMach;
	}
	public Amount<Length> getRangeAtMaxFuelCurrentMach() {
		return rangeAtMaxFuelCurrentMach;
	}
	public void setRangeAtMaxFuelCurrentMach(Amount<Length> rangeAtMaxFuelCurrentMach) {
		this.rangeAtMaxFuelCurrentMach = rangeAtMaxFuelCurrentMach;
	}
	public Amount<Length> getRangeAtZeroPayloadCurrentMach() {
		return rangeAtZeroPayloadCurrentMach;
	}
	public void setRangeAtZeroPayloadCurrentMach(Amount<Length> rangeAtZeroPayloadCurrentMach) {
		this.rangeAtZeroPayloadCurrentMach = rangeAtZeroPayloadCurrentMach;
	}
	public double getcLAtMaxPayloadBestRange() {
		return cLAtMaxPayloadBestRange;
	}
	public void setcLAtMaxPayloadBestRange(double cLAtMaxPayloadBestRange) {
		this.cLAtMaxPayloadBestRange = cLAtMaxPayloadBestRange;
	}
	public double getcLAtMaxFuelBestRange() {
		return cLAtMaxFuelBestRange;
	}
	public void setcLAtMaxFuelBestRange(double cLAtMaxFuelBestRange) {
		this.cLAtMaxFuelBestRange = cLAtMaxFuelBestRange;
	}
	public double getcLAtZeroPayloadBestRange() {
		return cLAtZeroPayloadBestRange;
	}
	public void setcLAtZeroPayloadBestRange(double cLAtZeroPayloadBestRange) {
		this.cLAtZeroPayloadBestRange = cLAtZeroPayloadBestRange;
	}
	public double getcLAtMaxPayloadCurrentMach() {
		return cLAtMaxPayloadCurrentMach;
	}
	public void setcLAtMaxPayloadCurrentMach(double cLAtMaxPayloadCurrentMach) {
		this.cLAtMaxPayloadCurrentMach = cLAtMaxPayloadCurrentMach;
	}
	public double getcLAtMaxFuelCurrentMach() {
		return cLAtMaxFuelCurrentMach;
	}
	public void setcLAtMaxFuelCurrentMach(double cLAtMaxFuelCurrentMach) {
		this.cLAtMaxFuelCurrentMach = cLAtMaxFuelCurrentMach;
	}
	public double getcLAtZeroPayloadCurrentMach() {
		return cLAtZeroPayloadCurrentMach;
	}
	public void setcLAtZeroPayloadCurrentMach(double cLAtZeroPayloadCurrentMach) {
		this.cLAtZeroPayloadCurrentMach = cLAtZeroPayloadCurrentMach;
	}
	public double getcDAtMaxPayloadBestRange() {
		return cDAtMaxPayloadBestRange;
	}
	public void setcDAtMaxPayloadBestRange(double cDAtMaxPayloadBestRange) {
		this.cDAtMaxPayloadBestRange = cDAtMaxPayloadBestRange;
	}
	public double getcDAtMaxFuelBestRange() {
		return cDAtMaxFuelBestRange;
	}
	public void setcDAtMaxFuelBestRange(double cDAtMaxFuelBestRange) {
		this.cDAtMaxFuelBestRange = cDAtMaxFuelBestRange;
	}
	public double getcDAtZeroPayloadBestRange() {
		return cDAtZeroPayloadBestRange;
	}
	public void setcDAtZeroPayloadBestRange(double cDAtZeroPayloadBestRange) {
		this.cDAtZeroPayloadBestRange = cDAtZeroPayloadBestRange;
	}
	public double getcDAtMaxPayloadCurrentMach() {
		return cDAtMaxPayloadCurrentMach;
	}
	public void setcDAtMaxPayloadCurrentMach(double cDAtMaxPayloadCurrentMach) {
		this.cDAtMaxPayloadCurrentMach = cDAtMaxPayloadCurrentMach;
	}
	public double getcDAtMaxFuelCurrentMach() {
		return cDAtMaxFuelCurrentMach;
	}
	public void setcDAtMaxFuelCurrentMach(double cDAtMaxFuelCurrentMach) {
		this.cDAtMaxFuelCurrentMach = cDAtMaxFuelCurrentMach;
	}
	public double getcDAtZeroPayloadCurrentMach() {
		return cDAtZeroPayloadCurrentMach;
	}
	public void setcDAtZeroPayloadCurrentMach(double cDAtZeroPayloadCurrentMach) {
		this.cDAtZeroPayloadCurrentMach = cDAtZeroPayloadCurrentMach;
	}
	public double getEfficiencyAtMaxPayloadBestRange() {
		return efficiencyAtMaxPayloadBestRange;
	}
	public void setEfficiencyAtMaxPayloadBestRange(double efficiencyAtMaxPayloadBestRange) {
		this.efficiencyAtMaxPayloadBestRange = efficiencyAtMaxPayloadBestRange;
	}
	public double getEfficiencyAtMaxFuelBestRange() {
		return efficiencyAtMaxFuelBestRange;
	}
	public void setEfficiencyAtMaxFuelBestRange(double efficiencyAtMaxFuelBestRange) {
		this.efficiencyAtMaxFuelBestRange = efficiencyAtMaxFuelBestRange;
	}
	public double getEfficiencyAtZeroPayloadBestRange() {
		return efficiencyAtZeroPayloadBestRange;
	}
	public void setEfficiencyAtZeroPayloadBestRange(double efficiencyAtZeroPayloadBestRange) {
		this.efficiencyAtZeroPayloadBestRange = efficiencyAtZeroPayloadBestRange;
	}
	public double getEfficiencyAtMaxPayloadCurrentMach() {
		return efficiencyAtMaxPayloadCurrentMach;
	}
	public void setEfficiencyAtMaxPayloadCurrentMach(double efficiencyAtMaxPayloadCurrentMach) {
		this.efficiencyAtMaxPayloadCurrentMach = efficiencyAtMaxPayloadCurrentMach;
	}
	public double getEfficiencyAtMaxFuelCurrentMach() {
		return efficiencyAtMaxFuelCurrentMach;
	}
	public void setEfficiencyAtMaxFuelCurrentMach(double efficiencyAtMaxFuelCurrentMach) {
		this.efficiencyAtMaxFuelCurrentMach = efficiencyAtMaxFuelCurrentMach;
	}
	public double getEfficiencyAtZeroPayloadCurrentMach() {
		return efficiencyAtZeroPayloadCurrentMach;
	}
	public void setEfficiencyAtZeroPayloadCurrentMach(double efficiencyAtZeroPayloadCurrentMach) {
		this.efficiencyAtZeroPayloadCurrentMach = efficiencyAtZeroPayloadCurrentMach;
	}
	public double getSfcAtMaxPayloadBestRange() {
		return sfcAtMaxPayloadBestRange;
	}
	public void setSfcAtMaxPayloadBestRange(double sfcAtMaxPayloadBestRange) {
		this.sfcAtMaxPayloadBestRange = sfcAtMaxPayloadBestRange;
	}
	public double getSfcAtMaxFuelBestRange() {
		return sfcAtMaxFuelBestRange;
	}
	public void setSfcAtMaxFuelBestRange(double sfcAtMaxFuelBestRange) {
		this.sfcAtMaxFuelBestRange = sfcAtMaxFuelBestRange;
	}
	public double getSfcAtZeroPayloadBestRange() {
		return sfcAtZeroPayloadBestRange;
	}
	public void setSfcAtZeroPayloadBestRange(double sfcAtZeroPayloadBestRange) {
		this.sfcAtZeroPayloadBestRange = sfcAtZeroPayloadBestRange;
	}
	public double getSfcAtMaxPayloadCurrentMach() {
		return sfcAtMaxPayloadCurrentMach;
	}
	public void setSfcAtMaxPayloadCurrentMach(double sfcAtMaxPayloadCurrentMach) {
		this.sfcAtMaxPayloadCurrentMach = sfcAtMaxPayloadCurrentMach;
	}
	public double getSfcAtMaxFuelCurrentMach() {
		return sfcAtMaxFuelCurrentMach;
	}
	public void setSfcAtMaxFuelCurrentMach(double sfcAtMaxFuelCurrentMach) {
		this.sfcAtMaxFuelCurrentMach = sfcAtMaxFuelCurrentMach;
	}
	public double getSfcAtZeroPayloadCurrentMach() {
		return sfcAtZeroPayloadCurrentMach;
	}
	public void setSfcAtZeroPayloadCurrentMach(double sfcAtZeroPayloadCurrentMach) {
		this.sfcAtZeroPayloadCurrentMach = sfcAtZeroPayloadCurrentMach;
	}
	public List<Amount<Length>> getRangeArrayBestRange() {
		return rangeArrayBestRange;
	}
	public void setRangeArrayBestRange(List<Amount<Length>> rangeArrayBestRange) {
		this.rangeArrayBestRange = rangeArrayBestRange;
	}
	public List<Amount<Length>> getRangeArrayCurrentMach() {
		return rangeArrayCurrentMach;
	}
	public void setRangeArrayCurrentMach(List<Amount<Length>> rangeArrayCurrentMach) {
		this.rangeArrayCurrentMach = rangeArrayCurrentMach;
	}
	public List<Double> getPayloadArray() {
		return payloadArray;
	}
	public void setPayloadArray(List<Double> payloadArray) {
		this.payloadArray = payloadArray;
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

	public Double[] getPolarCL() {
		return polarCL;
	}

	public void setPolarCL(Double[] polarCL) {
		this.polarCL = polarCL;
	}

	public Double[] getPolarCD() {
		return polarCD;
	}

	public void setPolarCD(Double[] polarCD) {
		this.polarCD = polarCD;
	}
	
}