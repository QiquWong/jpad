package sandbox.vc.PayloadRange;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.sun.org.glassfish.gmbal.AMXMetadata;

import aircraft.calculators.ACAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.RangeCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AirplaneType;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PayloadRangeEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.engine.EngineDatabaseManager;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.database.io.InputFileReader;
import standaloneutils.database.io.DatabaseFileWriter;
import standaloneutils.database.io.DatabaseIOmanager;
import writers.JPADStaticWriteUtils;

public class PayloadRangeCalcSA {

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
	private static double sfc;
	private AirfoilTypeEnum airfoilType;	
	private AirplaneType airplaneType;
	private EngineTypeEnum engineType;

	private static Unit<? extends Quantity> sfcUnit = (NonSI.POUND).divide((NonSI.POUND_FORCE).times(NonSI.HOUR));
	private static HashMap<Double, AirplaneType> mapPLRangeAirplane = new HashMap<Double, AirplaneType>();
	private static HashMap<Double, EngineTypeEnum> mapPLRangeEngine = new HashMap<Double, EngineTypeEnum>();
	private static HashMap<Double, AirfoilTypeEnum> mapPLRangeAirfoil = new HashMap<Double, AirfoilTypeEnum>();

	// TO EVALUATE: 
	private Amount<Mass> fuelMass, maxFuelMass, takeOffMass, payloadMaxFuel,
	maxTakeOffMass_current;
	private Amount<Length> range;
	private Amount<Length> rangeMP;
	private Amount<Length> rangeMF;
	private Amount<Length> rangeZP;
	private double rangeBreguet, bestRangeMach, nPassActual, cd, criticalMach;
	private double[][] rangeMatrix, payloadMatrix;

	private Amount<Mass> maxPayloadMass;

	//-------------------------------------------------------------------------------------
	// BUILDER

	public PayloadRangeCalcSA(
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


	//-------------------------------------------------------------------------------------
	// Methods

	/**
	 * 
	 * @author Vincenzo Cusati
	 * @return an object which manages the input   
	 */
	public static DatabaseIOmanager<PayloadRangeEnum> initializeInputTree() {

		DatabaseIOmanager<PayloadRangeEnum> IOmanager = new DatabaseIOmanager<PayloadRangeEnum>();

		IOmanager.addElement(PayloadRangeEnum.AirplaneType,  Amount.valueOf(0.0, Unit.ONE),	
					"It is possible to choose 0 which corresponds to TURBOFAN_TRANSPORT_JETS, or 1 for TURBOPROP_REGIONAL");
		IOmanager.addElement(PayloadRangeEnum.EngineType,  Amount.valueOf(0.0, Unit.ONE), "It is possible to choose 0 for TURBOFAN or 1 for TURBOPROP");
		IOmanager.addElement(PayloadRangeEnum.AirfoilType,  Amount.valueOf(0.0, Unit.ONE), 
					"It is possible to choose 0 for CONVENTIONAL, 1 for PEAKY, 2 for SUPERCRITICAL or 3 for MODERN_SUPERCRITICAL");
		IOmanager.addElement(PayloadRangeEnum.Altitude, Amount.valueOf(0.0, SI.METER), "Altitude in meters");
		IOmanager.addElement(PayloadRangeEnum.Mach_number, Amount.valueOf(0.0, Unit.ONE), "Mach number");
		IOmanager.addElement(PayloadRangeEnum.Planform_surface, Amount.valueOf(0.0, SI.SQUARE_METRE), "Planform wing surface");
		IOmanager.addElement(PayloadRangeEnum.TaperRatio, Amount.valueOf(0.0, Unit.ONE), "Wing taper ratio");
		IOmanager.addElement(PayloadRangeEnum.Maximum_take_off_mass, Amount.valueOf(0.0, SI.KILOGRAM), "Maximum take off mass (kg)");
		IOmanager.addElement(PayloadRangeEnum.Operating_empty_mass, Amount.valueOf(0.0, SI.KILOGRAM),  "Operating empty mass (kg)");
		IOmanager.addElement(PayloadRangeEnum.Maximum_fuel_mass, Amount.valueOf(0.0, SI.KILOGRAM), "Maximum fuel mass (kg)");
		IOmanager.addElement(PayloadRangeEnum.Maximum_number_of_passengers, Amount.valueOf(0.0, Unit.ONE), "Maximum number of passenger (maximum payload)");
		IOmanager.addElement(PayloadRangeEnum.SweepLE, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), "Wing sweep angle at leading edge (degree)");
		IOmanager.addElement(PayloadRangeEnum.OswaldFactor, Amount.valueOf(0.0, Unit.ONE), "Oswald factor");
		IOmanager.addElement(PayloadRangeEnum.ByPassRatio, Amount.valueOf(0.0, Unit.ONE), "Bypass ratio of the engine. If it has been chosen TURBOPROP type,"
				+ " set this value equal to 0.0");
		IOmanager.addElement(PayloadRangeEnum.Propeller_efficiency, Amount.valueOf(0.0, Unit.ONE),  "Propeller efficiency");
		IOmanager.addElement(PayloadRangeEnum.Mean_maximum_thickness, Amount.valueOf(0.0, Unit.ONE), "Mean maximum thickness of the wing airfoils");
		IOmanager.addElement(PayloadRangeEnum.Current_lift_coefficient, Amount.valueOf(0.0, Unit.ONE), "Total lift coefficient");
		IOmanager.addElement(PayloadRangeEnum.AspectRatio, Amount.valueOf(0.0, Unit.ONE),  "Wing aspect ratio");
		IOmanager.addElement(PayloadRangeEnum.CD0, Amount.valueOf(0.0, Unit.ONE), "Aircraft drag coefficient at zero lift");

		return IOmanager;

	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param rangeMP Range at Maximum Payload [nmi]
	 * @param rangeMF Range at Maximum Fuel [nmi]
	 * @param rangeZP Range at Zero Payload (maximum value of the range) [nmi]
	 * @param maxPayloadMass [kg] 
	 * @param sfc specific fuel consumption [lb/(hp*h)]
	 * @param E Aerodynamic Efficiency
	 * @return an object which manages the output
	 */

	public static DatabaseIOmanager<PayloadRangeEnum> initializeOutputManager(Amount<Length> rangeMP, Amount<Length>
	rangeMF, Amount<Length> rangeZP, Amount<Mass> maxPayloadMass, Double sfc, Double E){

		DatabaseIOmanager<PayloadRangeEnum> IOmanager = new DatabaseIOmanager<PayloadRangeEnum>(); 

		IOmanager.addElement(PayloadRangeEnum.Design_Range, rangeMP, "It's the range with maximum payload");
		IOmanager.addElement(PayloadRangeEnum.Max_Fuel_Range, rangeMF, "It's the range with maximum fuel mass");
		IOmanager.addElement(PayloadRangeEnum.Max_Range, rangeZP, "It's the range without payload");
		IOmanager.addElement(PayloadRangeEnum.Design_Payload, maxPayloadMass, "It's the value of design payload mass");
		IOmanager.addElement(PayloadRangeEnum.SFC, Amount.valueOf(sfc, sfcUnit), "Specific Fuel Consumption");
		IOmanager.addElement(PayloadRangeEnum.Efficiency, Amount.valueOf(E, Unit.ONE), "Aerodynamic efficiency");

		return IOmanager;
	}


	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */

	public static DatabaseIOmanager<PayloadRangeEnum> readFromFile(String filenamewithPathAndExt) {

		DatabaseIOmanager<PayloadRangeEnum> inputManager = initializeInputTree();

		InputFileReader<PayloadRangeEnum> payloadRangeFileReader = 
				new InputFileReader<PayloadRangeEnum>(filenamewithPathAndExt, inputManager.getTagList());

		List<Amount> valueList = payloadRangeFileReader.readAmounts();
		inputManager.setValueList(valueList);

		return inputManager;
	}


	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param filenamewithPathAndExt
	 * @param rootElement
	 * @param inputManager
	 * @param outputManager
	 */
	public static void writeToFile(
			String filenamewithPathAndExt,
//			String rootElement,
			DatabaseIOmanager<PayloadRangeEnum> inputManager,
			DatabaseIOmanager<PayloadRangeEnum> outputManager) {

		DatabaseFileWriter<PayloadRangeEnum> databaseWriter = new DatabaseFileWriter<PayloadRangeEnum>(
				"PayloadRange", // This string is that written in the <rootElement> in the output file for PayloadRange xml file 
				filenamewithPathAndExt, inputManager, outputManager);

		databaseWriter.writeDocument();
	}


	public static HashMap<Double, AirplaneType> pLRangeAirplaneTypes() {
		
		mapPLRangeAirplane.put(0.0,AirplaneType.TURBOFAN_TRANSPORT_JETS);
		mapPLRangeAirplane.put(1.0,AirplaneType.TURBOPROP_REGIONAL);
		// TODO: Insert here other airplane types
		
		return mapPLRangeAirplane;
	}
	
	public static HashMap<Double, EngineTypeEnum> pLRangeEngineType() {
		
		mapPLRangeEngine.put(0.0, EngineTypeEnum.TURBOFAN);
		mapPLRangeEngine.put(1.0, EngineTypeEnum.TURBOPROP);
		// TODO: Insert here other engine types
		
		return mapPLRangeEngine;
	}
	
	public static HashMap<Double, AirfoilTypeEnum> pLRangeAirfoilType() {
		
		mapPLRangeAirfoil.put(0.0, AirfoilTypeEnum.CONVENTIONAL);
		mapPLRangeAirfoil.put(1.0, AirfoilTypeEnum.MODERN_SUPERCRITICAL);
		mapPLRangeAirfoil.put(2.0, AirfoilTypeEnum.PEAKY);
		mapPLRangeAirfoil.put(3.0, AirfoilTypeEnum.SUPERCRITICAL);
				
		return mapPLRangeAirfoil;
	}
	
	
	
	/**
	 * @param inputFileNamewithPathAndExt
	 * @param outputFileWithPathAndExt
	 * @param fuelFractionReader
	 * @throws ClassNotFoundException
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 * @author Vincenzo Cusati
	 */
	public static void executeStandalonePayloadRange(String inputFileNamewithPathAndExt, String outputFileWithPathAndExt, 
			FuelFractionDatabaseReader fuelFractionReader) throws ClassNotFoundException, HDF5LibraryException, NullPointerException {

		DatabaseIOmanager<PayloadRangeEnum> inputManager = readFromFile(inputFileNamewithPathAndExt); 
				
		AirplaneType airplaneType = pLRangeAirplaneTypes().get(inputManager.getValue(PayloadRangeEnum.AirplaneType).getEstimatedValue());
		EngineTypeEnum engineType = pLRangeEngineType().get(inputManager.getValue(PayloadRangeEnum.EngineType).getEstimatedValue());
		AirfoilTypeEnum airfoilType = pLRangeAirfoilType().get(inputManager.getValue(PayloadRangeEnum.AirfoilType).getEstimatedValue());
		Amount<Angle> sweepLEEquivalent = inputManager.getValue(PayloadRangeEnum.SweepLE);
		Amount<Mass> maxTakeOffMass = (Amount<Mass>) inputManager.getValue(PayloadRangeEnum.Maximum_take_off_mass);
		Amount<Mass> operatingEmptyMass = (Amount<Mass>) inputManager.getValue(PayloadRangeEnum.Operating_empty_mass);
		Amount<Mass> maxFuelMass = (Amount<Mass>) inputManager.getValue(PayloadRangeEnum.Maximum_fuel_mass);
		Amount<Area> surface = (Amount<Area>) inputManager.getValue(PayloadRangeEnum.Planform_surface);
		double cd0 	  = inputManager.getValue(PayloadRangeEnum.CD0).getEstimatedValue();
		double oswald = inputManager.getValue(PayloadRangeEnum.OswaldFactor).getEstimatedValue();
		double ar 	  = inputManager.getValue(PayloadRangeEnum.AspectRatio).getEstimatedValue();
		double cl 	  = inputManager.getValue(PayloadRangeEnum.Current_lift_coefficient).getEstimatedValue();
		double tcMax = inputManager.getValue(PayloadRangeEnum.Mean_maximum_thickness).getEstimatedValue();
		double altitude 	= inputManager.getValue(PayloadRangeEnum.Altitude).getEstimatedValue();
		double eta 			= inputManager.getValue(PayloadRangeEnum.Propeller_efficiency).getEstimatedValue();
		double currentMach 	= inputManager.getValue(PayloadRangeEnum.Mach_number).getEstimatedValue();
		double byPassRatio 	= inputManager.getValue(PayloadRangeEnum.ByPassRatio).getEstimatedValue();
		double nPassMax		= inputManager.getValue(PayloadRangeEnum.Maximum_number_of_passengers).getEstimatedValue();
		double taperRatioEquivalent =  inputManager.getValue(PayloadRangeEnum.TaperRatio).getEstimatedValue();
		
//		String airplaneTypeString = inputManager.getValue(PayloadRangeEnum.AirplaneType).toString();
//		Class<?> type = Class.forName("AirplaneType");
//		AirplaneType airplaneType = AirplaneType.class.cast(airplaneTypeString);
//		
//		String engineTypeString = inputManager.getValue(PayloadRangeEnum.EngineType).toString();
////		Class<?> typeEngine = Class.forName("EngineTypeEnum");
//		EngineTypeEnum engineType = EngineTypeEnum.class.cast(engineTypeString);
//		
//		String airfoilTypeString = inputManager.getValue(PayloadRangeEnum.AirfoilType).toString();
////		Class<?> typeAirfoil = Class.forName("AirfoilTypeEnum");
//		AirfoilTypeEnum airfoilType = AirfoilTypeEnum.class.cast(airfoilTypeString);
		
		Amount<Angle> sweepHalfChordEquivalent = LSGeometryCalc.calculateSweep(
				ar,
				taperRatioEquivalent,
				sweepLEEquivalent.to(SI.RADIAN).getEstimatedValue(),
				0.5,
				0.0
				);

		//--------------------------------------------------------------------------------------
		// the next step is necessary to calculate CL and Speed at point E (or A) of the parabolic polar
		ACAerodynamicsManager analysis = new ACAerodynamicsManager();
		analysis.calculateDragPolarPoints(
				ar,
				oswald,
				cd0,
				AtmosphereCalc.getDensity(altitude),
				maxTakeOffMass.getEstimatedValue()*9.81,
				surface.getEstimatedValue()
				);

		//--------------------------------------------------------------------------------------
		// Creating calculator object

		PayloadRangeCalcSA test = new PayloadRangeCalcSA(
				maxTakeOffMass,
				operatingEmptyMass,
				maxFuelMass,
				sweepHalfChordEquivalent,
				nPassMax,
				cl,
				tcMax,
				airplaneType,
				engineType,
				airfoilType,
				analysis,
				fuelFractionReader
				);

		// -----------------------CRITICAL MACH NUMBER CHECK----------------------------

		boolean check = test.checkCriticalMach(currentMach);

		if (check){
			//		System.out.println("\n\n-----------------------------------------------------------"
			//				+ "\nCurrent Mach is lower then critical Mach number."
			//				+ "\nCurrent Mach = " + currentMach
			//				+ "\nCritical Mach = " + test.getCriticalMach() 
			//				+ "\n\n\t CHECK PASSED --> PROCEDING TO CALCULATION "
			//				+ "\n\n"
			//				+ "-----------------------------------------------------------");
		}
		else{
			System.err.println("\n\n-----------------------------------------------------------"
					+ "\nCurrent Mach is bigger then critical Mach number."
					+ "\nCurrent Mach = " + currentMach
					+ "\nCritical Mach = " + test.getCriticalMach() 
					+ "\n\n\t CHECK NOT PASSED --> WARNING!!! "
					+ "\n\n"
					+ "-----------------------------------------------------------");
		}

		// ------------------------MTOM PARAMETERIZATION---------------------------------

		test.createPayloadRangeMatrices(
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
				false
				);

		// ------------------------------PLOTTING-----------------------------------------------				
		// MTOM parameterization:

		test.createPayloadRangeCharts_MaxTakeOffMass(
				test.getRangeMatrix(),
				test.getPayloadMatrix()
				);
		

		DatabaseIOmanager<PayloadRangeEnum> outputManager = initializeOutputManager(
				Amount.valueOf(test.getRangeMatrix()[test.getRangeMatrix().length-1][1], NonSI.NAUTICAL_MILE),
				Amount.valueOf(test.getRangeMatrix()[test.getRangeMatrix().length-1][2], NonSI.NAUTICAL_MILE),
				Amount.valueOf(test.getRangeMatrix()[test.getRangeMatrix().length-1][3], NonSI.NAUTICAL_MILE), 
				test.getMaxPayloadMass(), 
				test.getSfc_current_mach(), 
				(test.getCl()/test.getCd()));
		
		writeToFile(outputFileWithPathAndExt, inputManager, outputManager);
				
	} //--end executeStandalonePayloadRange
	//
	//------------------------------------End Moved snippet-------------------------------------------------



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

//		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Perkins");
		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable();
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

		rangeMP = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		maxPayloadMass = Amount.valueOf(paxSingleMass.times(nPassMax).getEstimatedValue(), SI.KILOGRAM);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeMP.getEstimatedValue() + " " +
				rangeMP.getUnit() + " \n\t@ Max TO Mass of " + 
				maxTakeOffMass.getEstimatedValue() + " " +
				maxTakeOffMass.getUnit() + "\n\t@ Max Payload Mass of " + 
				maxPayloadMass + " " +
				paxSingleMass.getUnit() + " \n\t@ Fuel Mass of " + 
				fuelMass.getEstimatedValue() + " " +
				fuelMass.getUnit() + "\n\twith " + nPassMax + " passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cl + "\n\t\t cd = " + cd +
				"\n\t\t\t Efficiency = " + (cl/cd));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
		return rangeMP;
	}

	public double getCd() {
		return cd;
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

//		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Perkins");
		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable();
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

		rangeMF = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ MAX FUEL");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeMF.getEstimatedValue() + " " +
				rangeMF.getUnit() + " \n\t@ Max TO Mass of " + 
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
		return rangeMF;
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

//		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable("FuelFractions_Perkins");
		double[][] fuelFractionTable = fuelFractionDatabase.getFuelFractionTable();
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

		rangeZP = Amount.valueOf(rangeBreguet, SI.KILOMETER).to(NonSI.NAUTICAL_MILE);
		//---------------------------------------------------------------------------
		// RESULTS PRINT:
		System.out.println("-----------------------------------------------------------");
		System.out.println("RANGE @ ZERO PAYLOAD");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Range = " + rangeZP.getEstimatedValue() + " " +
				rangeZP.getUnit() + " \n\t@ TO Mass of " + 
				takeOffMass.getEstimatedValue() + " " +
				takeOffMass.getUnit() + "\n\t@ Payload Mass of 0 " + 
				paxSingleMass.getUnit() + " \n\t@ Max Fuel Mass of " + 
				maxFuelMass.getEstimatedValue() + " " +
				maxFuelMass.getUnit() + "\n\twith 0 passengers" +
				"\n\n\t\t SFC = " + sfc + "\n\n\t\t cL = " + cl + "\n\t\t cd = " + cd +
				"\n\t\t\t Efficiency = " + (cl/cd));
		System.out.println("-----------------------------------------------------------");
		System.out.println(" ");
		return rangeZP;
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


	public Amount<Mass> getMaxPayloadMass() {
		return maxPayloadMass;
	}


	public void setMaxPayloadMass(Amount<Mass> maxPayloadMass) {
		this.maxPayloadMass = maxPayloadMass;
	}

}
