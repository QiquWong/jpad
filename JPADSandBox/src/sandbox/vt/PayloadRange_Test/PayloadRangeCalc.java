package sandbox.vt.PayloadRange_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.calculators.ACAerodynamicsManager;
import aircraft.components.Aircraft;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.performance.RangeCalc;
import configuration.MyConfiguration;
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
	// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
	private Amount<Mass> paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);
	private Amount<Mass> maxTakeOffMass;
	private Amount<Mass> operatingEmptyMass; 
	private Amount<Angle> sweepLEEquivalent;
	private Amount<Angle> sweepHalfChordEquivalent;
	private Amount<Area> surface;
	private ACAerodynamicsManager aerodynamicAnalysis;
	private FuelFractionDatabaseReader fuelFractionDatabase;
	private double cd0;
	private double oswald;
	private double cl;
	private double ar;
	private double tcMax;
	private double byPassRatio;
	private double altitude;
	private double currentMach;
	private double nPassMax;
	private double eta = 0.85;
	private double sfc;
	private AirfoilTypeEnum airfoilType;	
	private AirplaneType airplaneType;
	private EngineTypeEnum engineType;

	// TO EVALUATE: 
	private Amount<Mass> fuelMass, maxFuelMass, takeOffMass, payloadMaxFuel,
						 maxTakeOffMass_current;
	private Amount<Length> range;
	private double rangeBreguet, bestRangeMach, nPassActual, cd, criticalMach;
	private double[][] rangeMatrix, payloadMatrix;
 

	//-------------------------------------------------------------------------------------
	// BUILDER
	
	// Builder used only for StandAlone version 
	public PayloadRangeCalc(
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> maxFuelMass,
			Amount<Angle> sweepHalfChordEquivalent,
			double nPassMax,
			double cl,
			double tcMax,
			AirplaneType airplaneType,
			EngineTypeEnum engineType,
			AirfoilTypeEnum airfoilType,
			ACAerodynamicsManager analysis,
			FuelFractionDatabaseReader fuelFraction){
		
		this.maxTakeOffMass = maxTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.maxFuelMass = maxFuelMass;
		this.sweepHalfChordEquivalent = sweepHalfChordEquivalent;
		this.nPassMax = nPassMax;
		this.cl = cl;
		this.tcMax = tcMax;
		this.airplaneType = airplaneType;
		this.engineType = engineType;
		this.airfoilType = airfoilType;
		this.aerodynamicAnalysis = analysis;
		this.fuelFractionDatabase = fuelFraction;
	};
	
	// Builder used in case of a given aircraft object
	PayloadRangeCalc(
			OperatingConditions theConditions,
			Aircraft theAircraft,
			AirplaneType type){
		
		airplaneType = type;
		
		maxTakeOffMass = theAircraft.get_weights().get_MTOM();
		operatingEmptyMass = theAircraft.get_weights().get_OEM();
		maxFuelMass = theAircraft.get_theFuelTank().get_fuelMass();
		nPassMax = theAircraft.get_configuration().get_maxPax();
		airfoilType = theAircraft.get_wing().get_theAirfoilsList().get(0).get_type();
		engineType = theAircraft.get_powerPlant().get_engineType();

		surface = theAircraft.get_wing().get_surface();
		ar = theAircraft.get_wing().get_aspectRatio();
		currentMach = theConditions.get_machCurrent();
		altitude = theConditions.get_altitude().getEstimatedValue();
		sweepLEEquivalent = theAircraft.get_wing().get_sweepLEEquivalent();
		sweepHalfChordEquivalent = theAircraft.get_wing().calculateSweep(
				sweepLEEquivalent.getEstimatedValue(), 0.5, 0.0
				);
		
		/* TODO: WHEN OTHER ENGINE DATA WILL BE AVAIABLE FIX THIS
		 * this works for turbofan and turboprop engine only (which are what we're analyzing). 
		 */
		if (engineType == EngineTypeEnum.TURBOFAN)
			byPassRatio = theAircraft.get_powerPlant().get_engineList().get(0).get_bpr();
		else
			byPassRatio = 0.0;
		
		tcMax = theAircraft.get_wing().get_thicknessMean();
		cd0 = theAircraft.get_theAerodynamics().calculateCD0Total();
		oswald = theAircraft.get_theAerodynamics().calculateOswald(currentMach, MethodEnum.HOWE);
		cl = LiftCalc.calcCLatAlphaLinearDLR(
				theConditions.get_alphaCurrent().getEstimatedValue(),
				ar
				);
	
		
		aerodynamicAnalysis = theAircraft.get_theAerodynamics();
		fuelFractionDatabase = theAircraft.get_theFuelTank().getFuelFractionDatabase();
	}

	//-------------------------------------------------------------------------------------
	// METHODS
	
	/********************************************************************************************
	 * Method that calculate, for a given aircraft with a given engine,
	 * the number of Mach related to the Best Range condition.
	 * 
	 * @author Vittorio Trifari
	 * @param engineType
	 * @param surface Wing surface in [m^2] 
	 * @param ar Wing aspect ratio
	 * @param oswald Oswald factor of the wing
	 * @param cd0 
	 * @param altitude
	 * @return bestRangeMach the Mach number relative to the Best Range condition.
	 */
	public double calculateBestRangeMach(
			EngineTypeEnum engineType,
			Amount<Area> surface,
			double ar,
			double oswald,
			double cd0,
			double altitude){
		
		if (engineType == EngineTypeEnum.PISTON  ||
				engineType == EngineTypeEnum.TURBOPROP) {
			
			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue());
			
			bestRangeMach = SpeedCalc.calculateMach(
					altitude,
					aerodynamicAnalysis.getvE());
		}
		else if (engineType == EngineTypeEnum.TURBOJET ||
				engineType == EngineTypeEnum.TURBOFAN ||
				engineType == EngineTypeEnum.RAMJET   ||
				engineType == EngineTypeEnum.PROPFAN)  {
			
			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue());
			
			bestRangeMach = SpeedCalc.calculateMach(
					altitude,
					aerodynamicAnalysis.getvA());
		}
		return bestRangeMach;
	}

	/**************************************************************************************
	 * This method allow user to check if the Mach number chosen for the calculation it's lower 
	 * than the critical Mach number in that flight condition or not.
	 * 
	 * @author Vittorio Trifari
	 * @param currentMach
	 * @return check boolean that is true if the current Mach is lower than the critical one
	 */
	public boolean checkCriticalMach(double currentMach){
		
		criticalMach = AerodynamicCalc.calculateMachCriticalKroo(
				cl,
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
	 * @return range the range value in [nmi]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public Amount<Length> calcRangeAtMaxPayload(
			Amount<Mass> maxTakeOffMass,
			Amount<Angle> sweepHalfChordEquivalent,
			Amount<Area> surface,
			double cd0, double oswald,
			double cl, double ar,
			double tcMax, double byPassRatio, double eta,
			double altitude, double currentMach,
			boolean isBestRange)
					throws HDF5LibraryException, NullPointerException {	
		
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

		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Roskam");
		double ratio = 1.0;

		//TODO: WHEN UNKNOWN ARICRAFTS DATA WILL BE AVAIABLE, MODIFY THE ERROR MESSAGE.

		switch (airplaneType){

		case PISTON_HOMEBUILT: int index1 = 1;
		for (int i=0; i<fuelFractionTable[0].length; i++){
			ratio *= fuelFractionTable[index1][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_SINGLE_ENGINE: int index2 = 2;
		for (int i=0; i<fuelFractionTable[1].length; i++){
			ratio *= fuelFractionTable[index2][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_TWIN_ENGINE: int index3 = 3;
		for (int i=0; i<fuelFractionTable[2].length; i++){
			ratio *= fuelFractionTable[index3][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_AGRICULTURAL: int index4 = 4;
		for (int i=0; i<fuelFractionTable[3].length; i++){
			ratio *= fuelFractionTable[index4][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOFAN_BUSINESS_JETS: int index5 = 5;
		for (int i=0; i<fuelFractionTable[4].length; i++){
			ratio *= fuelFractionTable[index5][i]; 
		} 
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOPROP_REGIONAL: int index6 = 6;
		for (int i=0; i<fuelFractionTable[5].length; i++){
			ratio *= fuelFractionTable[index6][i]; 
		}
		break;

		case TURBOFAN_TRANSPORT_JETS: int index7 = 7;
		for (int i=0; i<fuelFractionTable[6].length; i++){
			ratio *= fuelFractionTable[index7][i]; 
		}
		break;

		case MILITARY_TRAINERS: int index8 = 8;
		for (int i=0; i<fuelFractionTable[7].length; i++){
			ratio *= fuelFractionTable[index8][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_FIGHTERS: int index9 = 9;
		for (int i=0; i<fuelFractionTable[8].length; i++){
			ratio *= fuelFractionTable[index9][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_PATROL_BOMB_TRANSPORT: int index10 = 10;
		for (int i=0; i<fuelFractionTable[9].length; i++){
			ratio *= fuelFractionTable[index10][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case FLYING_BOATS_AMPHIBIOUS_FLOATAIRPLANES: int index11 = 11;
		for (int i=0; i<fuelFractionTable[10].length; i++){
			ratio *= fuelFractionTable[index11][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case SUPERSONIC_CRUISE: int index12 = 12;
		for (int i=0; i<fuelFractionTable[11].length; i++){
			ratio *= fuelFractionTable[index12][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;
		}

		double breguetRatio = ratio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue()
					);

			sfc = EngineDatabaseManager.getSFC(
					calculateBestRangeMach(
							engineType,
							surface,
							ar,
							oswald,
							cd0,
							altitude
							),
					altitude,
					EngineDatabaseManager.getThrustRatio(
							calculateBestRangeMach(
									engineType,
									surface,
									ar,
									oswald,
									cd0,
									altitude
									),
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cl = aerodynamicAnalysis.getcLE();
			cd = aerodynamicAnalysis.getcDE();
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);
			
			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue());

			bestRangeMach = calculateBestRangeMach(
					engineType,
					surface,
					ar,
					oswald,
					cd0,
					altitude
					);
			
			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);
			
			cl = aerodynamicAnalysis.getcLA();
			cd = aerodynamicAnalysis.getcDA();
			
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);
			
			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);
						
			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									currentMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}

		range = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + range.getEstimatedValue() + " " +
				range.getUnit() + " \n\t@ Max TO Mass of " + 
				maxTakeOffMass.getEstimatedValue() + " " +
				maxTakeOffMass.getUnit() + "\n\t@ Max Payload Mass of " + 
				paxSingleMass.times(nPassMax).getEstimatedValue() + " " +
				paxSingleMass.getUnit() + " \n\t@ Fuel Mass of " + 
				fuelMass.getEstimatedValue() + " " +
				fuelMass.getUnit() + "\n\twith " + nPassMax + " passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cl + "\n\t\t cd = " + cd +
				"\n\t\t\t Efficiency = " + (cl/cd));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
		return range;
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
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
	 * @return range the range value in [nmi]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public Amount<Length> calcRangeAtMaxFuel(
			Amount<Mass> maxTakeOffMass,
			Amount<Angle> sweepHalfChordEquivalent,
			Amount<Area> surface,
			double cd0, double oswald,
			double cl, double ar,
			double tcMax, double byPassRatio, double eta,
			double altitude, double currentMach,
			boolean isBestRange) 
					throws HDF5LibraryException, NullPointerException{	
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

		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Roskam");
		double ratio = 1.0;

		//TODO: WHEN UNKNOWN ARICRAFTS DATA WILL BE AVAIABLE, MODIFY THE ERROR MESSAGE.

		switch (airplaneType){

		case PISTON_HOMEBUILT: int index1 = 1;
		for (int i=0; i<fuelFractionTable[0].length; i++){
			ratio *= fuelFractionTable[index1][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_SINGLE_ENGINE: int index2 = 2;
		for (int i=0; i<fuelFractionTable[1].length; i++){
			ratio *= fuelFractionTable[index2][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_TWIN_ENGINE: int index3 = 3;
		for (int i=0; i<fuelFractionTable[2].length; i++){
			ratio *= fuelFractionTable[index3][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_AGRICULTURAL: int index4 = 4;
		for (int i=0; i<fuelFractionTable[3].length; i++){
			ratio *= fuelFractionTable[index4][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOFAN_BUSINESS_JETS: int index5 = 5;
		for (int i=0; i<fuelFractionTable[4].length; i++){
			ratio *= fuelFractionTable[index5][i]; 
		} 
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOPROP_REGIONAL: int index6 = 6;
		for (int i=0; i<fuelFractionTable[5].length; i++){
			ratio *= fuelFractionTable[index6][i]; 
		}
		break;

		case TURBOFAN_TRANSPORT_JETS: int index7 = 7;
		for (int i=0; i<fuelFractionTable[6].length; i++){
			ratio *= fuelFractionTable[index7][i]; 
		}
		break;

		case MILITARY_TRAINERS: int index8 = 8;
		for (int i=0; i<fuelFractionTable[7].length; i++){
			ratio *= fuelFractionTable[index8][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_FIGHTERS: int index9 = 9;
		for (int i=0; i<fuelFractionTable[8].length; i++){
			ratio *= fuelFractionTable[index9][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_PATROL_BOMB_TRANSPORT: int index10 = 10;
		for (int i=0; i<fuelFractionTable[9].length; i++){
			ratio *= fuelFractionTable[index10][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case FLYING_BOATS_AMPHIBIOUS_FLOATAIRPLANES: int index11 = 11;
		for (int i=0; i<fuelFractionTable[10].length; i++){
			ratio *= fuelFractionTable[index11][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case SUPERSONIC_CRUISE: int index12 = 12;
		for (int i=0; i<fuelFractionTable[11].length; i++){
			ratio *= fuelFractionTable[index12][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;
		}

		double breguetRatio = ratio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue()
					);

			sfc = EngineDatabaseManager.getSFC(
					calculateBestRangeMach(
							engineType,
							surface,
							ar,
							oswald,
							cd0,
							altitude
							),
					altitude,
					EngineDatabaseManager.getThrustRatio(
							calculateBestRangeMach(
									engineType,
									surface,
									ar,
									oswald,
									cd0,
									altitude
									),
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cl = aerodynamicAnalysis.getcLE();
			cd = aerodynamicAnalysis.getcDE();

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue());

			bestRangeMach = calculateBestRangeMach(
					engineType,
					surface,
					ar,
					oswald,
					cd0,
					altitude
					);

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cl = aerodynamicAnalysis.getcLA();
			cd = aerodynamicAnalysis.getcDA();

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);

			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									currentMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}

		range = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX FUEL");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + range.getEstimatedValue() + " " +
				range.getUnit() + " \n\t@ Max TO Mass of " + 
				maxTakeOffMass.getEstimatedValue() + " " +
				maxTakeOffMass.getUnit() + "\n\t@ Payload Mass of " + 
				paxSingleMass.times(nPassActual).getEstimatedValue() + " " +
				paxSingleMass.getUnit() + " \n\t@ Max Fuel Mass of " + 
				maxFuelMass.getEstimatedValue() + " " +
				maxFuelMass.getUnit() + "\n\twith " + Math.ceil(nPassActual) + " passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cl + "\n\t\t cd = " + cd +
				"\n\t\t\t Efficiency = " + (cl/cd));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
		return range;
	}

	/******************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Fuel.
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
	 * @return range the range value in [nmi]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public Amount<Length> calcRangeAtZeroPayload(
			Amount<Mass> maxTakeOffMass,
			Amount<Angle> sweepHalfChordEquivalent,
			Amount<Area> surface,
			double cd0, double oswald,
			double cl, double ar,
			double tcMax, double byPassRatio, double eta,
			double altitude, double currentMach,
			boolean isBestRange) 
					throws HDF5LibraryException, NullPointerException
	{		
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
		
		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Roskam");
		double ratio = 1.0;

		//TODO: WHEN UNKNOWN ARICRAFTS DATA WILL BE AVAIABLE, MODIFY THE ERROR MESSAGE.

		switch (airplaneType){

		case PISTON_HOMEBUILT: int index1 = 1;
		for (int i=0; i<fuelFractionTable[0].length; i++){
			ratio *= fuelFractionTable[index1][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_SINGLE_ENGINE: int index2 = 2;
		for (int i=0; i<fuelFractionTable[1].length; i++){
			ratio *= fuelFractionTable[index2][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_TWIN_ENGINE: int index3 = 3;
		for (int i=0; i<fuelFractionTable[2].length; i++){
			ratio *= fuelFractionTable[index3][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case PISTON_AGRICULTURAL: int index4 = 4;
		for (int i=0; i<fuelFractionTable[3].length; i++){
			ratio *= fuelFractionTable[index4][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOFAN_BUSINESS_JETS: int index5 = 5;
		for (int i=0; i<fuelFractionTable[4].length; i++){
			ratio *= fuelFractionTable[index5][i]; 
		} 
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case TURBOPROP_REGIONAL: int index6 = 6;
		for (int i=0; i<fuelFractionTable[5].length; i++){
			ratio *= fuelFractionTable[index6][i]; 
		}
		break;

		case TURBOFAN_TRANSPORT_JETS: int index7 = 7;
		for (int i=0; i<fuelFractionTable[6].length; i++){
			ratio *= fuelFractionTable[index7][i]; 
		}
		break;

		case MILITARY_TRAINERS: int index8 = 8;
		for (int i=0; i<fuelFractionTable[7].length; i++){
			ratio *= fuelFractionTable[index8][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_FIGHTERS: int index9 = 9;
		for (int i=0; i<fuelFractionTable[8].length; i++){
			ratio *= fuelFractionTable[index9][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case MILITARY_PATROL_BOMB_TRANSPORT: int index10 = 10;
		for (int i=0; i<fuelFractionTable[9].length; i++){
			ratio *= fuelFractionTable[index10][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case FLYING_BOATS_AMPHIBIOUS_FLOATAIRPLANES: int index11 = 11;
		for (int i=0; i<fuelFractionTable[10].length; i++){
			ratio *= fuelFractionTable[index11][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;

		case SUPERSONIC_CRUISE: int index12 = 12;
		for (int i=0; i<fuelFractionTable[11].length; i++){
			ratio *= fuelFractionTable[index12][i]; 
		}
		System.err.println("-------------------------------------------------------");
		System.err.println("CAN'T PROCEED - DATA NOT AVAIABLE FOR THIS AIRCRAFT YET");
		System.err.println("NULL POINTER EXCEPTION WILL BE SHOWN");
		System.err.println("-------------------------------------------------------");
		System.err.println(" ");
		return null;
		}

		double breguetRatio = ratio/mff; 

		if (isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue()
					);

			sfc = EngineDatabaseManager.getSFC(
					calculateBestRangeMach(
							engineType,
							surface,
							ar,
							oswald,
							cd0,
							altitude
							),
					altitude,
					EngineDatabaseManager.getThrustRatio(
							calculateBestRangeMach(
									engineType,
									surface,
									ar,
									oswald,
									cd0,
									altitude
									),
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cl = aerodynamicAnalysis.getcLE();
			cd = aerodynamicAnalysis.getcDE();

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (!isBestRange &&
				(engineType==EngineTypeEnum.PISTON ||
				 engineType==EngineTypeEnum.TURBOPROP)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType
					);

			rangeBreguet = RangeCalc.calculateRangeBreguetPropellerSFC(
					eta,
					sfc,
					cl,
					cd,
					breguetRatio
					);
		}
		else if (isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			aerodynamicAnalysis.calculateDragPolarPoints(
					ar,
					oswald,
					cd0,
					AtmosphereCalc.getDensity(altitude),
					maxTakeOffMass.getEstimatedValue()*9.81,
					surface.getEstimatedValue());

			bestRangeMach = calculateBestRangeMach(
					engineType,
					surface,
					ar,
					oswald,
					cd0,
					altitude
					);

			sfc = EngineDatabaseManager.getSFC(
					bestRangeMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							bestRangeMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE
							),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE
					);

			cl = aerodynamicAnalysis.getcLA();
			cd = aerodynamicAnalysis.getcDA();

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									bestRangeMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}
		else if (!isBestRange &&
				(engineType == EngineTypeEnum.TURBOFAN ||
				 engineType == EngineTypeEnum.TURBOJET ||
				 engineType == EngineTypeEnum.PROPFAN  ||
				 engineType == EngineTypeEnum.RAMJET)) {

			sfc = EngineDatabaseManager.getSFC(
					currentMach,
					altitude,
					EngineDatabaseManager.getThrustRatio(
							currentMach,
							altitude,
							byPassRatio,
							engineType,
							EngineOperatingConditionEnum.CRUISE),
					byPassRatio,
					engineType,
					EngineOperatingConditionEnum.CRUISE);

			cd = DragCalc.calculateCDTotal(
					cd0,
					cl,
					ar,
					oswald,
					currentMach,
					sweepHalfChordEquivalent.getEstimatedValue(),
					tcMax,
					airfoilType);

			rangeBreguet = RangeCalc.calculateRangeBreguetJetSFCJ(
					Amount.valueOf(
							SpeedCalc.calculateTAS(
									currentMach,
									altitude
									),
							SI.METERS_PER_SECOND).to(NonSI.KILOMETERS_PER_HOUR),
					sfc,
					cl,
					cd,
					breguetRatio);
		}

		range = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ ZERO PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + range.getEstimatedValue() + " " +
				range.getUnit() + " \n\t@ TO Mass of " + 
				takeOffMass.getEstimatedValue() + " " +
				takeOffMass.getUnit() + "\n\t@ Payload Mass of 0 " + 
				paxSingleMass.getUnit() + " \n\t@ Max Fuel Mass of " + 
				maxFuelMass.getEstimatedValue() + " " +
				maxFuelMass.getUnit() + "\n\twith 0 passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cl + "\n\t\t cd = " + cd +
				"\n\t\t\t Efficiency = " + (cl/cd));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
		return range;
	}

	/******************************************************************************************
	 * Method that allows users to generate the Range array to be used in 
	 * Payload-Range plot.
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
	 * @return range the range value in [nmi]
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 * 
	 * @return vRange the List of Range values in [nmi]
	 */
	public List<Amount<Length>> createRangeArray(
			Amount<Mass> maxTakeOffMass,
			Amount<Angle> sweepHalfChordEquivalent,
			Amount<Area> surface,
			double cd0, double oswald,
			double cl, double ar,
			double tcMax, double byPassRatio, double eta,
			double altitude, double currentMach,
			boolean isBestRange)
					throws HDF5LibraryException, NullPointerException{

		List<Amount<Length>> vRange = new ArrayList<Amount<Length>>();

		System.out.println("-----------BUILDING RANGE ARRAY COMPONENTS-----------------");
		System.out.println();
		// POINT 1
		vRange.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		vRange.add(calcRangeAtMaxPayload(
				maxTakeOffMass,
				sweepHalfChordEquivalent,
				surface,
				cd0,
				oswald,
				cl,
				ar,
				tcMax,
				byPassRatio,
				eta,
				altitude,
				currentMach,
				isBestRange));
		// POINT 3
		vRange.add(calcRangeAtMaxFuel(
				maxTakeOffMass,
				sweepHalfChordEquivalent,
				surface,
				cd0,
				oswald,
				cl,
				ar,
				tcMax,
				byPassRatio,
				eta,
				altitude,
				currentMach,
				isBestRange));
		// POINT 4
		vRange.add(calcRangeAtZeroPayload(
				maxTakeOffMass,
				sweepHalfChordEquivalent,
				surface,
				cd0,
				oswald,
				cl,
				ar,
				tcMax,
				byPassRatio,
				eta,
				altitude,
				currentMach,
				isBestRange
				)
				);

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
	public void createPayloadRangeMatrices(
			Amount<Angle> sweepHalfChordEquivalent,
			Amount<Area> surface,
			double cd0, double oswald,
			double cl, double ar,
			double tcMax, double byPassRatio, double eta,
			double altitude, double currentMach,
			boolean isBestRange)
					throws HDF5LibraryException, NullPointerException{
		
		System.out.println("-----------BUILDING RANGE MATRIX COMPONENTS-----------------");
		System.out.println();
		
		double[] massArray_MTOM = new double[5];
		rangeMatrix = new double [5][4];
		payloadMatrix = new double [5][4];
		
		// generating variation of mass of 5% until -20% of maxTakeOffMass
		for (int i=0; i<5; i++){
			massArray_MTOM[i] = maxTakeOffMass.getEstimatedValue()*(1-0.05*(4-i));
		}

		// setting the i-value of the mass array to the current maxTakeOffMass
		for (int i=0; i<5; i++){
			for (int j=0; j<4; j++){
				maxTakeOffMass_current = Amount.valueOf(massArray_MTOM[i], SI.KILOGRAM);
				switch (j){
				case 0:
					rangeMatrix[i][j] = 0.0;
					payloadMatrix[i][j] = nPassMax;
					break;
				case 1:
					rangeMatrix[i][j] =	calcRangeAtMaxPayload(
							maxTakeOffMass_current,
							sweepHalfChordEquivalent,
							surface,
							cd0,
							oswald,
							cl,
							ar,
							tcMax,
							byPassRatio,
							eta,
							altitude,
							currentMach,
							isBestRange
							).getEstimatedValue();	
					payloadMatrix[i][j] = nPassMax;
					break;
				case 2:
					rangeMatrix[i][j] =	calcRangeAtMaxFuel(
							maxTakeOffMass_current,
							sweepHalfChordEquivalent,
							surface,
							cd0,
							oswald,
							cl,
							ar,
							tcMax,
							byPassRatio,
							eta,
							altitude,
							currentMach,
							isBestRange
							).getEstimatedValue();
					payloadMatrix[i][j] = Math.round(getnPassActual());
					break;
				case 3:
					rangeMatrix[i][j] =	calcRangeAtZeroPayload(
							maxTakeOffMass_current,
							sweepHalfChordEquivalent,
							surface,
							cd0,
							oswald,
							cl,
							ar,
							tcMax,
							byPassRatio,
							eta,
							altitude,
							currentMach,
							isBestRange
							).getEstimatedValue();
					payloadMatrix[i][j] = 0;
					break;
				}
			}
		}
		
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

		double bestRange_double_Array[] = MyArrayUtils.convertListOfAmountTodoubleArray(vRange_BR);
		double currentRange_double_Array[] = MyArrayUtils.convertListOfAmountTodoubleArray(vRange_CM);
		double payload_double_Array[]= MyArrayUtils.convertToDoublePrimitive(vPayload);

		double[][] range_double_Arrays = new double[2][4];
		for (int i=0; i<4; i++){
			range_double_Arrays[0][i] = bestRange_double_Array[i];
			range_double_Arrays[1][i] = currentRange_double_Array[i];
		}
		double[][] payload_double_Arrays = new double[2][4];
		for (int i=0; i<4; i++){
			payload_double_Arrays[0][i] = payload_double_Array[i];
			payload_double_Arrays[1][i] = payload_double_Array[i];
		}

		String[] legendValue = new String[2];
		legendValue[0] = "Best Range at Mach = " + bestRangeMach;
		legendValue[1] = "Current Condition at Mach = " + currentMach;

		MyChartToFileUtils.plot(
				range_double_Arrays, payload_double_Arrays,	// array to plot
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
	 * @param currentMach the actual Mach number
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

	public Amount<Mass> getMaxTakeOffMass() {
		return maxTakeOffMass;
	}

	public void setMaxTakeOffMass(Amount<Mass> maxTakeOffMass) {
		this.maxTakeOffMass = maxTakeOffMass;
	}

	public Amount<Mass> getMaxTakeOffMass_current() {
		return maxTakeOffMass_current;
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

	public double getCd0() {
		return cd0;
	}

	public void setCd0(double cd0) {
		this.cd0 = cd0;
	}

	public double getOswald() {
		return oswald;
	}

	public void setOswald(double oswald) {
		this.oswald = oswald;
	}

	public double getCl() {
		return cl;
	}

	public void setCl(double cl) {
		this.cl = cl;
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

	public double setByPassRatio(double byPassRatio) {
		this.byPassRatio = byPassRatio;
		return this.byPassRatio;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getCurrentMach() {
		return currentMach;
	}

	public void setCurrentMach(double currentMach) {
		this.currentMach = currentMach;
	}

	public double getCriticalMach() {
		return criticalMach;
	}

	public double getnPassMax() {
		return nPassMax;
	}

	public void setnPassMax(double nPassMax) {
		this.nPassMax = nPassMax;
	}

	public AirfoilTypeEnum getAirfoilType() {
		return airfoilType;
	}

	public void setAirfoilType(AirfoilTypeEnum airfoilType) {
		this.airfoilType = airfoilType;
	}

	public EngineTypeEnum getEngineType() {
		return engineType;
	}

	public void setEngineType(EngineTypeEnum engineType) {
		this.engineType = engineType;
	}

	public Amount<Mass> getMaxFuelMass() {
		return maxFuelMass;
	}

	public void setMaxFuelMass(Amount<Mass> maxFuelMass) {
		this.maxFuelMass = maxFuelMass;
	}

	public Amount<Mass> getPaxSingleMass() {
		return paxSingleMass;
	}

	public double getnPassActual() {
		return nPassActual;
	}

	public double getEta() {
		return eta;
	}

	public void setEta(double eta) {
		this.eta = eta;
	}

	public double getBestRangeMach() {
		return bestRangeMach;
	}

	public double getSfc_current_mach() {
		return sfc;
	}

	public double[][] getRangeMatrix() {
		return rangeMatrix;
	}

	public double[][] getPayloadMatrix() {
		return payloadMatrix;
	}
}