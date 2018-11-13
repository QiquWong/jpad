package database.databasefunctions.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.unit.Unit;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import database.DatabaseInterpolationUtils;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;

/**
 * 
 * @author Vittorio Trifari
 *
 */
public class EngineDatabaseManager_v2 extends EngineDatabaseReader_v2 {

	//-------------------------------------------------------------------
	// VARIABLE DECLARATION
	//-------------------------------------------------------------------
	public static final int numberOfInput = 4;
	public static final int numberOfOutput = 7;
	
	private double byPassRatio;
	private String altitudeUnit;
	private String deltaTemperatureUnit;
	private Map<String, List<Boolean>> interpolationInputBooleanListMap; 
	
	//-------------------------------------------------------------------
	// BUILDER
	//-------------------------------------------------------------------
	public EngineDatabaseManager_v2() {
		super();
	}

	//-------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------
	public void importDatabaseFromFile(String databaseFolderPath, String engineDatabaseFileName) {
		
		/*
		 * READING DATA FROM EXCEL FILE ...
		 */
		File engineDatabaseFile = new File(databaseFolderPath + engineDatabaseFileName);
		if(!engineDatabaseFile.exists()) {
			System.err.println("WARNING (IMPORT ENGINE DATABASE): THE ENGINE DATABASE FILE DOES NOT EXIST!! TERMINATING...");
			System.exit(1);
		}

		Map<String, List<List<String>>> dataMap = new HashMap<>(); 
		DataFormatter dataFormatter = new DataFormatter();
		try {
			Workbook workbook = WorkbookFactory.create(engineDatabaseFile);
			System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets: ");
			String engineType = dataFormatter.formatCellValue(workbook.getSheetAt(0).getRow(0).getCell(1));
			System.out.println("Engine Type: " + engineType);
			if(!engineType.equalsIgnoreCase("TURBOPROP") && !engineType.equalsIgnoreCase("PISTON")) {
				byPassRatio = Double.valueOf(dataFormatter.formatCellValue(workbook.getSheetAt(0).getRow(1).getCell(1)));
				System.out.println("BPR: " + byPassRatio);
			}
				
			for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
				List<List<String>> sheetData = new ArrayList<>();
				workbook.getSheetAt(i).forEach(row -> {
					List<String> sheetRowData = new ArrayList<>();
					row.forEach(cell -> {
						sheetRowData.add(dataFormatter.formatCellValue(cell));
					});
					sheetData.add(sheetRowData.stream().filter(data -> !data.isEmpty()).collect(Collectors.toList()));
				});
				for(int j=0; j<sheetData.size(); j++)
					if(sheetData.get(j).isEmpty()) {
						sheetData.remove(j);
						j--;
					}
				dataMap.put(workbook.getSheetName(i), sheetData);
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		interpolationInputBooleanListMap = new HashMap<>();
		
		/*
		 * CREATING INTERPOLATING FUNCTIONS ...
		 * 
		 * Index legend:
		 * 0 = Thrust Ratio
		 * 1 = TSFC [lb/lb*hr]
		 * 2 = NOx Emission Index
		 * 3 = CO Emission Index
		 * 4 = HC Emission Index
		 * 5 = CO2 Emission Index
		 * 6 = H2O Emission Index
		 */
		Map<Integer, MyInterpolatingFunction> interpolatedTakeOffDataMap = createInterpolatedDataMap(dataMap, "TAKE-OFF");
		Map<Integer, MyInterpolatingFunction> interpolatedAPRDataMap = createInterpolatedDataMap(dataMap, "APR");
		Map<Integer, MyInterpolatingFunction> interpolatedClimbDataMap = createInterpolatedDataMap(dataMap, "CLIMB");
		Map<Integer, MyInterpolatingFunction> interpolatedContinuousDataMap = createInterpolatedDataMap(dataMap, "CONTINUOUS");
		Map<Integer, MyInterpolatingFunction> interpolatedCruiseDataMap = createInterpolatedDataMap(dataMap, "CRUISE");
		Map<Integer, MyInterpolatingFunction> interpolatedFlightIdleDataMap = createInterpolatedDataMap(dataMap, "FLIGHT IDLE");
		Map<Integer, MyInterpolatingFunction> interpolatedGroundIdleDataMap = createInterpolatedDataMap(dataMap, "GROUND IDLE");
		
		/*
		 * FILLING SUERCLASS INTERPO FUNCTIONS ...
		 */
		
		/* TAKE-OFF */
		super.takeOffThrustRatioFunction = interpolatedTakeOffDataMap.get(0);
		super.takeOffSFCFunction = interpolatedTakeOffDataMap.get(1);
		super.takeOffNOxEmissionIndexFunction = interpolatedTakeOffDataMap.get(2);
		super.takeOffCOEmissionIndexFunction = interpolatedTakeOffDataMap.get(3);
		super.takeOffHCEmissionIndexFunction = interpolatedTakeOffDataMap.get(4);
		super.takeOffCO2EmissionIndexFunction = interpolatedTakeOffDataMap.get(5);
		super.takeOffH2OEmissionIndexFunction = interpolatedTakeOffDataMap.get(6);
		
		/* APR */
		super.aprThrustRatioFunction = interpolatedAPRDataMap.get(0);
		super.aprSFCFunction = interpolatedAPRDataMap.get(1);
		super.aprNOxEmissionIndexFunction = interpolatedAPRDataMap.get(2);
		super.aprCOEmissionIndexFunction = interpolatedAPRDataMap.get(3);
		super.aprHCEmissionIndexFunction = interpolatedAPRDataMap.get(4);
		super.aprCO2EmissionIndexFunction = interpolatedAPRDataMap.get(5);
		super.aprH2OEmissionIndexFunction = interpolatedAPRDataMap.get(6);
		
		/* CLIMB */
		super.climbThrustRatioFunction = interpolatedClimbDataMap.get(0);
		super.climbSFCFunction = interpolatedClimbDataMap.get(1);
		super.climbNOxEmissionIndexFunction = interpolatedClimbDataMap.get(2);
		super.climbCOEmissionIndexFunction = interpolatedClimbDataMap.get(3);
		super.climbHCEmissionIndexFunction = interpolatedClimbDataMap.get(4);
		super.climbCO2EmissionIndexFunction = interpolatedClimbDataMap.get(5);
		super.climbH2OEmissionIndexFunction = interpolatedClimbDataMap.get(6);
		
		/* CONTINUOUS */
		super.continuousThrustRatioFunction = interpolatedContinuousDataMap.get(0);
		super.continuousSFCFunction = interpolatedContinuousDataMap.get(1);
		super.continuousNOxEmissionIndexFunction = interpolatedContinuousDataMap.get(2);
		super.continuousCOEmissionIndexFunction = interpolatedContinuousDataMap.get(3);
		super.continuousHCEmissionIndexFunction = interpolatedContinuousDataMap.get(4);
		super.continuousCO2EmissionIndexFunction = interpolatedContinuousDataMap.get(5);
		super.continuousH2OEmissionIndexFunction = interpolatedContinuousDataMap.get(6);
		
		/* CRUISE */
		super.cruiseThrustRatioFunction = interpolatedCruiseDataMap.get(0);
		super.cruiseSFCFunction = interpolatedCruiseDataMap.get(1);
		super.cruiseNOxEmissionIndexFunction = interpolatedCruiseDataMap.get(2);
		super.cruiseCOEmissionIndexFunction = interpolatedCruiseDataMap.get(3);
		super.cruiseHCEmissionIndexFunction = interpolatedCruiseDataMap.get(4);
		super.cruiseCO2EmissionIndexFunction = interpolatedCruiseDataMap.get(5);
		super.cruiseH2OEmissionIndexFunction = interpolatedCruiseDataMap.get(6);
		
		/* FLIGHT IDLE */
		super.flightIdleThrustRatioFunction = interpolatedFlightIdleDataMap.get(0);
		super.flightIdleSFCFunction = interpolatedFlightIdleDataMap.get(1);
		super.flightIdleNOxEmissionIndexFunction = interpolatedFlightIdleDataMap.get(2);
		super.flightIdleCOEmissionIndexFunction = interpolatedFlightIdleDataMap.get(3);
		super.flightIdleHCEmissionIndexFunction = interpolatedFlightIdleDataMap.get(4);
		super.flightIdleCO2EmissionIndexFunction = interpolatedFlightIdleDataMap.get(5);
		super.flightIdleH2OEmissionIndexFunction = interpolatedFlightIdleDataMap.get(6);
		
		/* FLIGHT IDLE */
		super.groundIdleThrustRatioFunction = interpolatedGroundIdleDataMap.get(0);
		super.groundIdleSFCFunction = interpolatedGroundIdleDataMap.get(1);
		super.groundIdleNOxEmissionIndexFunction = interpolatedGroundIdleDataMap.get(2);
		super.groundIdleCOEmissionIndexFunction = interpolatedGroundIdleDataMap.get(3);
		super.groundIdleHCEmissionIndexFunction = interpolatedGroundIdleDataMap.get(4);
		super.groundIdleCO2EmissionIndexFunction = interpolatedGroundIdleDataMap.get(5);
		super.groundIdleH2OEmissionIndexFunction = interpolatedGroundIdleDataMap.get(6);
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<Integer, MyInterpolatingFunction> createInterpolatedDataMap (
			Map<String, List<List<String>>> dataMap, 
			String engineSetting
			) {
		
		Map<Integer, MyInterpolatingFunction> outputMap = new HashMap<>();
		
	    List<List<String>> sheetData = dataMap.get(engineSetting);
	    altitudeUnit = sheetData.get(1).get(0);
	    deltaTemperatureUnit = sheetData.get(1).get(2);
	    List<Amount<Length>> altitudeList = new ArrayList<>();
	    List<Double> machList = new ArrayList<>();
	    List<Amount<Temperature>> deltaTemperatureList = new ArrayList<>();
	    List<Double> throttleList = new ArrayList<>();
	    List<Double> thrustRatioList = new ArrayList<>();
	    List<Double> sfcList = new ArrayList<>();
	    List<Double> emissionIndexNOxList = new ArrayList<>();
	    List<Double> emissionIndexCOList = new ArrayList<>();
	    List<Double> emissionIndexHCList = new ArrayList<>();
	    List<Double> emissionIndexCO2List = new ArrayList<>();
	    List<Double> emissionIndexH2OList = new ArrayList<>();
	    for (int i = 2; i < sheetData.size(); i++) {
	    	if(sheetData.get(i).size() >= 1)
	    		altitudeList.add(
	    				(Amount<Length>) Amount.valueOf(
	    						Double.valueOf(sheetData.get(i).get(0).replace(',', '.')),
	    						Unit.valueOf(altitudeUnit)
	    						)
	    				);
	    	if(sheetData.get(i).size() >= 2)
	    		machList.add(Double.valueOf(sheetData.get(i).get(1).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 3)
	    		deltaTemperatureList.add(
	    				(Amount<Temperature>) Amount.valueOf(
	    						Double.valueOf(sheetData.get(i).get(2).replace(',', '.')),
	    						Unit.valueOf(deltaTemperatureUnit)
	    						)
	    				);
	    	if(sheetData.get(i).size() >= 4)
	    		throttleList.add(Double.valueOf(sheetData.get(i).get(3).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 5)
	    		thrustRatioList.add(Double.valueOf(sheetData.get(i).get(4).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 6)
	    		sfcList.add(Double.valueOf(sheetData.get(i).get(5).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 7)
	    		emissionIndexNOxList.add(Double.valueOf(sheetData.get(i).get(6).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 8)
	    		emissionIndexCOList.add(Double.valueOf(sheetData.get(i).get(7).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 9)
	    		emissionIndexHCList.add(Double.valueOf(sheetData.get(i).get(8).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 10)
	    		emissionIndexCO2List.add(Double.valueOf(sheetData.get(i).get(9).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 11)	    	
	    		emissionIndexH2OList.add(Double.valueOf(sheetData.get(i).get(10).replace(',', '.')));
		}
	    
	    List<double[]> repeatedInputDoubleArrayList = new ArrayList<>();
	    repeatedInputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(altitudeList));
	    repeatedInputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(machList));
	    repeatedInputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(deltaTemperatureList));
	    repeatedInputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(throttleList));

	    List<double[]> inputDoubleArrayList = new ArrayList<>();
	    inputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(altitudeList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(machList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(deltaTemperatureList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(throttleList.stream().distinct().collect(Collectors.toList())));
	    
	    int interpolationNumberOfInput = 0;
	    List<double[]> interpolationInputDoubleArrayList = new ArrayList<>();
	    List<double[]> interpolationRepeatedInputDoubleArrayList = new ArrayList<>();
	    List<Boolean> interpolationInputBooleanList = new ArrayList<>();
	    for(int i=0; i<numberOfInput; i++) {
			if(inputDoubleArrayList.get(i).length > 1) {
				interpolationNumberOfInput += 1;
				interpolationInputDoubleArrayList.add(inputDoubleArrayList.get(i));
				interpolationRepeatedInputDoubleArrayList.add(repeatedInputDoubleArrayList.get(i));
				interpolationInputBooleanList.add(true);
			}
			else
				interpolationInputBooleanList.add(false);
	    }
	    
	    interpolationInputBooleanListMap.put(engineSetting, interpolationInputBooleanList);

	    List<double[]> engineDataDoubleArrayList = new ArrayList<>();
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(thrustRatioList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(sfcList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexNOxList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexCOList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexHCList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexCO2List));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexH2OList));

	    if(interpolationNumberOfInput > 1) {
	    	List<List<List<Integer>>> columnIndexList = new ArrayList<>();
	    	List<List<Integer>> currentVariableElementList = new ArrayList<>();
	    	List<Integer> currentIndexElementList = new ArrayList<>();
	    	for(int i=0; i<interpolationNumberOfInput; i++) {
	    		currentVariableElementList = new ArrayList<>();
	    		for(int j=0; j<interpolationInputDoubleArrayList.get(i).length; j++) {
	    			currentIndexElementList = new ArrayList<>();
	    			for(int k=0; k<interpolationRepeatedInputDoubleArrayList.get(i).length; k++) {
	    				if(interpolationRepeatedInputDoubleArrayList.get(i)[k] == interpolationInputDoubleArrayList.get(i)[j])
	    					currentIndexElementList.add(k);
	    			}
	    			currentVariableElementList.add(currentIndexElementList);
	    		}
	    		columnIndexList.add(currentVariableElementList);
	    	}


	    	List<Integer> arrayDimensionList = interpolationInputDoubleArrayList.stream().map(x -> x.length).collect(Collectors.toList());

	    	List<List<Integer>> inputPermutationList = new ArrayList<>();
	    	for(int i=0; i<interpolationNumberOfInput; i++) {
	    		if (arrayDimensionList.get(i) == 1) {
	    			List<Integer> permutationListIn = new ArrayList<>();
	    			permutationListIn.add(0);
	    			inputPermutationList.add(permutationListIn);
	    		}
	    		else
	    			inputPermutationList.add(
	    					MyArrayUtils.convertIntArrayToListInteger(
	    							MyArrayUtils.linspaceInt(
	    									0,
	    									arrayDimensionList.get(i)-1, 
	    									arrayDimensionList.get(i)
	    									)
	    							)
	    					);
	    	}
	    	List<List<Double>> variablePermutationList = new ArrayList<>();
	    	for(int i=0; i<interpolationNumberOfInput; i++) {
	    		variablePermutationList.add(
	    				MyArrayUtils.convertDoubleArrayToListDouble(
	    						MyArrayUtils.convertFromDoubleToPrimitive(
	    								interpolationInputDoubleArrayList.get(i)
	    								)
	    						)
	    				);
	    	}

	    	Collection<List<Integer>> permutationResults = DatabaseInterpolationUtils.permutations(inputPermutationList);

	    	Map<List<Integer>, Integer> interpolatingMatrixIndexes = new HashMap<>();
	    	System.out.println("\n\n" + engineSetting + " data: ");
	    	for(int i=0; i<permutationResults.size(); i++) {
	    		interpolatingMatrixIndexes = DatabaseInterpolationUtils.buildInterpolatingMatrixIndexes(
	    				columnIndexList, 
	    				(List<Integer>) permutationResults.toArray()[i], 
	    				interpolatingMatrixIndexes, 
	    				interpolationNumberOfInput
	    				);
	    	}

	    	for(int i=0; i<numberOfOutput; i++)
	    		DatabaseInterpolationUtils.createMultiDimensionalMatrix(
	    				interpolationNumberOfInput,
	    				i,
	    				interpolationInputDoubleArrayList,
	    				engineDataDoubleArrayList.get(i),
	    				interpolatingMatrixIndexes,
	    				outputMap
	    				);
	    }
	    else {
	    	System.out.println("\n\n" + engineSetting + " data: ");
	    	for(int i=0; i<numberOfOutput; i++) {
	    		
	    		while (engineDataDoubleArrayList.get(i).length != interpolationInputDoubleArrayList.get(0).length) {
	    			List<Double> interpolationEngineDataDoubleArrayList = MyArrayUtils.convertArrayDoublePrimitiveToList(engineDataDoubleArrayList.get(i));
	    			interpolationEngineDataDoubleArrayList.add(0.0);
	    			engineDataDoubleArrayList.remove(i);
	    			engineDataDoubleArrayList.add(i, MyArrayUtils.convertToDoublePrimitive(interpolationEngineDataDoubleArrayList));
	    		}
	    		
	    		MyInterpolatingFunction interpolatedResponseSurface = new MyInterpolatingFunction();
	    		interpolatedResponseSurface.interpolateLinear(interpolationInputDoubleArrayList.get(0), engineDataDoubleArrayList.get(i));
	    		outputMap.put(i, interpolatedResponseSurface);
	    	}
			
	    }
		
		System.out.println(engineSetting + " data has been interpolated!");
		return outputMap;
		
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

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.takeOffThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.takeOffThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.takeOffThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.takeOffThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.aprThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.aprThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.aprThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.aprThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.climbThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.climbThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.climbThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.climbThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.continuousThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.continuousThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.continuousThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.continuousThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.cruiseThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.cruiseThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.cruiseThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.cruiseThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.flightIdleThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.flightIdleThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.flightIdleThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.flightIdleThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			thrustRatio = super.groundIdleThrustRatioFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			thrustRatio = super.groundIdleThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			thrustRatio = super.groundIdleThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			thrustRatio = super.groundIdleThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return thrustRatio;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.takeOffSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.takeOffSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.takeOffSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.takeOffSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.aprSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.aprSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.aprSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.aprSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.climbSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.climbSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.climbSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.climbSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.continuousSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.continuousSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.continuousSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.continuousSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.cruiseSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.cruiseSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.cruiseSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.cruiseSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.flightIdleSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.flightIdleSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.flightIdleSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.flightIdleSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			sfc = super.groundIdleSFCFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			sfc = super.groundIdleSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			sfc = super.groundIdleSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			sfc = super.groundIdleSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return sfc;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.aprNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.climbNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.continuousNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexNOx;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.takeOffCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.aprCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.aprCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.aprCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.aprCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.climbCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.climbCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.climbCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.climbCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.continuousCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.continuousCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.continuousCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.continuousCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.cruiseCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.flightIdleCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO = super.groundIdleCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.takeOffHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.aprHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.aprHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.aprHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.aprHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.climbHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.climbHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.climbHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.climbHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.continuousHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.continuousHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.continuousHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.continuousHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.cruiseHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.flightIdleHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexHC = super.groundIdleHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexHC;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.aprCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.climbCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexCO2;
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

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("TAKE-OFF")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("TAKE-OFF").size(); i++)
			if (interpolationInputBooleanListMap.get("TAKE-OFF").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("APR")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("APR").size(); i++)
			if (interpolationInputBooleanListMap.get("APR").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.aprH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CLIMB")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CLIMB").size(); i++)
			if (interpolationInputBooleanListMap.get("CLIMB").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.climbH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
	
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CONTINUOUS")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CONTINUOUS").size(); i++)
			if (interpolationInputBooleanListMap.get("CONTINUOUS").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.continuousH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("CRUISE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("CRUISE").size(); i++)
			if (interpolationInputBooleanListMap.get("CRUISE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("FLIGHT IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("FLIGHT IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("FLIGHT IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(altitudeUnit)),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(deltaTemperatureUnit)),
				throttleSetting
		};
		
		int numberOfReducedInput = interpolationInputBooleanListMap
				.get("GROUND IDLE")
				.stream()
				.filter(bool -> bool.equals(true))
				.collect(Collectors.toList())
				.size(); 
		List<Integer> indexList = new ArrayList<>();
		for(int i=0; i<interpolationInputBooleanListMap.get("GROUND IDLE").size(); i++)
			if (interpolationInputBooleanListMap.get("GROUND IDLE").get(i) == true) 
				indexList.add(i);
		
		if(numberOfReducedInput == 1) 
			emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
		else if(numberOfReducedInput == 2) 
			emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
		else if(numberOfReducedInput == 3) 
			emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
		else if(numberOfReducedInput == 4) 
			emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		return emissionIndexH2O;
	}

	public double getByPassRatio() {
		return byPassRatio;
	}

	public void setByPassRatio(double byPassRatio) {
		this.byPassRatio = byPassRatio;
	}

}

