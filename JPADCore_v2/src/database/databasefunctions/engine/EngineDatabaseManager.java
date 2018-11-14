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
public class EngineDatabaseManager extends EngineDatabaseReader {

	//-------------------------------------------------------------------
	// VARIABLE DECLARATION
	//-------------------------------------------------------------------
	public static final int numberOfInput = 4;
	public static final int numberOfOutput = 9;
	
	private double byPassRatio;
	private String altitudeUnit;
	private String deltaTemperatureUnit;
	private Map<String, List<Boolean>> interpolationInputBooleanListMap; 
	
	//-------------------------------------------------------------------
	// BUILDER
	//-------------------------------------------------------------------
	public EngineDatabaseManager() {
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
		 * 5 = Soot Emission Index
		 * 6 = CO2 Emission Index
		 * 7 = SOx Emission Index
		 * 8 = H2O Emission Index
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
		super.takeOffSootEmissionIndexFunction = interpolatedTakeOffDataMap.get(5);
		super.takeOffCO2EmissionIndexFunction = interpolatedTakeOffDataMap.get(6);
		super.takeOffSOxEmissionIndexFunction = interpolatedTakeOffDataMap.get(7);
		super.takeOffH2OEmissionIndexFunction = interpolatedTakeOffDataMap.get(8);
		
		/* APR */
		super.aprThrustRatioFunction = interpolatedAPRDataMap.get(0);
		super.aprSFCFunction = interpolatedAPRDataMap.get(1);
		super.aprNOxEmissionIndexFunction = interpolatedAPRDataMap.get(2);
		super.aprCOEmissionIndexFunction = interpolatedAPRDataMap.get(3);
		super.aprHCEmissionIndexFunction = interpolatedAPRDataMap.get(4);
		super.aprSootEmissionIndexFunction = interpolatedAPRDataMap.get(5);
		super.aprCO2EmissionIndexFunction = interpolatedAPRDataMap.get(6);
		super.aprSOxEmissionIndexFunction = interpolatedAPRDataMap.get(7);
		super.aprH2OEmissionIndexFunction = interpolatedAPRDataMap.get(8);
		
		/* CLIMB */
		super.climbThrustRatioFunction = interpolatedClimbDataMap.get(0);
		super.climbSFCFunction = interpolatedClimbDataMap.get(1);
		super.climbNOxEmissionIndexFunction = interpolatedClimbDataMap.get(2);
		super.climbCOEmissionIndexFunction = interpolatedClimbDataMap.get(3);
		super.climbHCEmissionIndexFunction = interpolatedClimbDataMap.get(4);
		super.climbSootEmissionIndexFunction = interpolatedClimbDataMap.get(5);
		super.climbCO2EmissionIndexFunction = interpolatedClimbDataMap.get(6);
		super.climbSOxEmissionIndexFunction = interpolatedClimbDataMap.get(7);
		super.climbH2OEmissionIndexFunction = interpolatedClimbDataMap.get(8);
		
		/* CONTINUOUS */
		super.continuousThrustRatioFunction = interpolatedContinuousDataMap.get(0);
		super.continuousSFCFunction = interpolatedContinuousDataMap.get(1);
		super.continuousNOxEmissionIndexFunction = interpolatedContinuousDataMap.get(2);
		super.continuousCOEmissionIndexFunction = interpolatedContinuousDataMap.get(3);
		super.continuousHCEmissionIndexFunction = interpolatedContinuousDataMap.get(4);
		super.continuousSootEmissionIndexFunction = interpolatedContinuousDataMap.get(5);
		super.continuousCO2EmissionIndexFunction = interpolatedContinuousDataMap.get(6);
		super.continuousSOxEmissionIndexFunction = interpolatedContinuousDataMap.get(7);
		super.continuousH2OEmissionIndexFunction = interpolatedContinuousDataMap.get(8);
		
		/* CRUISE */
		super.cruiseThrustRatioFunction = interpolatedCruiseDataMap.get(0);
		super.cruiseSFCFunction = interpolatedCruiseDataMap.get(1);
		super.cruiseNOxEmissionIndexFunction = interpolatedCruiseDataMap.get(2);
		super.cruiseCOEmissionIndexFunction = interpolatedCruiseDataMap.get(3);
		super.cruiseHCEmissionIndexFunction = interpolatedCruiseDataMap.get(4);
		super.cruiseSootEmissionIndexFunction = interpolatedCruiseDataMap.get(5);
		super.cruiseCO2EmissionIndexFunction = interpolatedCruiseDataMap.get(6);
		super.cruiseSOxEmissionIndexFunction = interpolatedCruiseDataMap.get(7);
		super.cruiseH2OEmissionIndexFunction = interpolatedCruiseDataMap.get(8);
		
		/* FLIGHT IDLE */
		super.flightIdleThrustRatioFunction = interpolatedFlightIdleDataMap.get(0);
		super.flightIdleSFCFunction = interpolatedFlightIdleDataMap.get(1);
		super.flightIdleNOxEmissionIndexFunction = interpolatedFlightIdleDataMap.get(2);
		super.flightIdleCOEmissionIndexFunction = interpolatedFlightIdleDataMap.get(3);
		super.flightIdleHCEmissionIndexFunction = interpolatedFlightIdleDataMap.get(4);
		super.flightIdleSootEmissionIndexFunction = interpolatedFlightIdleDataMap.get(5);
		super.flightIdleCO2EmissionIndexFunction = interpolatedFlightIdleDataMap.get(6);
		super.flightIdleSOxEmissionIndexFunction = interpolatedFlightIdleDataMap.get(7);
		super.flightIdleH2OEmissionIndexFunction = interpolatedFlightIdleDataMap.get(8);
		
		/* GROUND IDLE */
		super.groundIdleThrustRatioFunction = interpolatedGroundIdleDataMap.get(0);
		super.groundIdleSFCFunction = interpolatedGroundIdleDataMap.get(1);
		super.groundIdleNOxEmissionIndexFunction = interpolatedGroundIdleDataMap.get(2);
		super.groundIdleCOEmissionIndexFunction = interpolatedGroundIdleDataMap.get(3);
		super.groundIdleHCEmissionIndexFunction = interpolatedGroundIdleDataMap.get(4);
		super.groundIdleSootEmissionIndexFunction = interpolatedGroundIdleDataMap.get(5);
		super.groundIdleCO2EmissionIndexFunction = interpolatedGroundIdleDataMap.get(6);
		super.groundIdleSOxEmissionIndexFunction = interpolatedGroundIdleDataMap.get(7);
		super.groundIdleH2OEmissionIndexFunction = interpolatedGroundIdleDataMap.get(8);
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<Integer, MyInterpolatingFunction> createInterpolatedDataMap (
			Map<String, List<List<String>>> dataMap, 
			String engineSetting
			) {
		
		Map<Integer, MyInterpolatingFunction> outputMap = new HashMap<>();
		
	    List<List<String>> sheetData = dataMap.get(engineSetting);
	    setAltitudeUnit(sheetData.get(1).get(0));
	    setDeltaTemperatureUnit(sheetData.get(1).get(2));
	    List<Amount<Length>> altitudeList = new ArrayList<>();
	    List<Double> machList = new ArrayList<>();
	    List<Amount<Temperature>> deltaTemperatureList = new ArrayList<>();
	    List<Double> throttleList = new ArrayList<>();
	    List<Double> thrustRatioList = new ArrayList<>();
	    List<Double> sfcList = new ArrayList<>();
	    List<Double> emissionIndexNOxList = new ArrayList<>();
	    List<Double> emissionIndexCOList = new ArrayList<>();
	    List<Double> emissionIndexHCList = new ArrayList<>();
	    List<Double> emissionIndexSootList = new ArrayList<>();
	    List<Double> emissionIndexCO2List = new ArrayList<>();
	    List<Double> emissionIndexSOxList = new ArrayList<>();
	    List<Double> emissionIndexH2OList = new ArrayList<>();
	    for (int i = 2; i < sheetData.size(); i++) {
	    	if(sheetData.get(i).size() >= 1)
	    		altitudeList.add(
	    				(Amount<Length>) Amount.valueOf(
	    						Double.valueOf(sheetData.get(i).get(0).replace(',', '.')),
	    						Unit.valueOf(getAltitudeUnit())
	    						)
	    				);
	    	if(sheetData.get(i).size() >= 2)
	    		machList.add(Double.valueOf(sheetData.get(i).get(1).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 3)
	    		deltaTemperatureList.add(
	    				(Amount<Temperature>) Amount.valueOf(
	    						Double.valueOf(sheetData.get(i).get(2).replace(',', '.')),
	    						Unit.valueOf(getDeltaTemperatureUnit())
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
	    		emissionIndexSootList.add(Double.valueOf(sheetData.get(i).get(9).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 11)
	    		emissionIndexCO2List.add(Double.valueOf(sheetData.get(i).get(10).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 12)	    	
	    		emissionIndexSOxList.add(Double.valueOf(sheetData.get(i).get(11).replace(',', '.')));
	    	if(sheetData.get(i).size() >= 13)	    	
	    		emissionIndexH2OList.add(Double.valueOf(sheetData.get(i).get(12).replace(',', '.')));
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
	    
	    double[] inputUpperBounds = new double[4];
	    inputUpperBounds[0] = MyArrayUtils.getMax(inputDoubleArrayList.get(0));
	    inputUpperBounds[1] = MyArrayUtils.getMax(inputDoubleArrayList.get(1));
	    inputUpperBounds[2] = MyArrayUtils.getMax(inputDoubleArrayList.get(2));
	    inputUpperBounds[3] = MyArrayUtils.getMax(inputDoubleArrayList.get(3));
		
		double[] inputLowerBounds = new double[4];
		inputLowerBounds[0] = MyArrayUtils.getMin(inputDoubleArrayList.get(0));
		inputLowerBounds[1] = MyArrayUtils.getMin(inputDoubleArrayList.get(1));
		inputLowerBounds[2] = MyArrayUtils.getMin(inputDoubleArrayList.get(2));
		inputLowerBounds[3] = MyArrayUtils.getMin(inputDoubleArrayList.get(3));
	    
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
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexSootList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexCO2List));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexSOxList));
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
	    				outputMap,
	    				inputLowerBounds,
	    				inputUpperBounds
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
	    		interpolatedResponseSurface.setxMin(inputLowerBounds[0]);
	    		interpolatedResponseSurface.setyMin(inputLowerBounds[1]);
	    		interpolatedResponseSurface.setzMin(inputLowerBounds[2]);
	    		interpolatedResponseSurface.setkMin(inputLowerBounds[3]);
	    		interpolatedResponseSurface.setxMax(inputUpperBounds[0]);
	    		interpolatedResponseSurface.setyMax(inputUpperBounds[1]);
	    		interpolatedResponseSurface.setzMax(inputUpperBounds[2]);
	    		interpolatedResponseSurface.setkMax(inputUpperBounds[3]);
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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.takeOffThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.takeOffThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3)  
				thrustRatio = super.takeOffThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4)  
				thrustRatio = super.takeOffThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.aprThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.aprThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.aprThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.aprThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		}
		
		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.climbThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.climbThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.climbThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.climbThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.continuousThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.continuousThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.continuousThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.continuousThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.cruiseThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.cruiseThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.cruiseThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.cruiseThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return thrustRatio;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.flightIdleThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.flightIdleThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.flightIdleThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.flightIdleThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return thrustRatio;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getThrustRatioGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double thrustRatio = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleThrustRatioFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleThrustRatioFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleThrustRatioFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleThrustRatioFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleThrustRatioFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleThrustRatioFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleThrustRatioFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleThrustRatioFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				thrustRatio = super.groundIdleThrustRatioFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				thrustRatio = super.groundIdleThrustRatioFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				thrustRatio = super.groundIdleThrustRatioFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				thrustRatio = super.groundIdleThrustRatioFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				sfc = super.takeOffSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.takeOffSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.takeOffSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.takeOffSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				sfc = super.aprSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.aprSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.aprSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.aprSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				sfc = super.climbSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.climbSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.climbSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.climbSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcContinuous(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				sfc = super.continuousSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.continuousSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.continuousSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.continuousSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				sfc = super.cruiseSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.cruiseSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.cruiseSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.cruiseSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcFlightIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				sfc = super.flightIdleSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.flightIdleSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.flightIdleSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.flightIdleSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return sfc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSfcGroundIdle(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double sfc = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleSFCFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleSFCFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleSFCFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleSFCFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleSFCFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleSFCFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleSFCFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleSFCFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

		
			if(numberOfReducedInput == 1) 
				sfc = super.groundIdleSFCFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				sfc = super.groundIdleSFCFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				sfc = super.groundIdleSFCFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				sfc = super.groundIdleSFCFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.takeOffNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.aprNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.aprNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.climbNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.climbNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.continuousNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.continuousNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.cruiseNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleNOxEmissionIndexFunction.getkMin();
		
		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.flightIdleNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexNOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getNOxEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexNOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleNOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleNOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleNOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleNOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleNOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleNOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleNOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleNOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexNOx = super.groundIdleNOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.takeOffCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.takeOffCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.aprCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.aprCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.aprCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.aprCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.climbCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.climbCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.climbCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.climbCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);
		
		}
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.continuousCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.continuousCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.continuousCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.continuousCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.cruiseCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.cruiseCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.flightIdleCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.flightIdleCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCOEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexCO = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleCOEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleCOEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleCOEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleCOEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleCOEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleCOEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleCOEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleCOEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexCO = super.groundIdleCOEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO = super.groundIdleCOEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.takeOffHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.takeOffHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.aprHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.aprHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.aprHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.aprHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.climbHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.climbHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.climbHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.climbHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.continuousHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.continuousHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.continuousHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.continuousHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.cruiseHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.cruiseHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.flightIdleHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.flightIdleHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		return emissionIndexHC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getHCEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexHC = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleHCEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleHCEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleHCEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleHCEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleHCEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleHCEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleHCEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleHCEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexHC = super.groundIdleHCEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexHC = super.groundIdleHCEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexHC;
	}

	@Override
	public double getSootEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexSoot = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexSoot = getSootEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexSoot = getSootEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexSoot = getSootEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexSoot = getSootEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexSoot = getSootEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FIDL:
			emissionIndexSoot = getSootEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexSoot = getSootEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.takeOffSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.takeOffSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.takeOffSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.takeOffSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.aprSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.aprSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.aprSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.aprSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
			
			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.climbSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.climbSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.climbSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.climbSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.continuousSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.continuousSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.continuousSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.continuousSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.cruiseSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.cruiseSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.cruiseSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.cruiseSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {

		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.flightIdleSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.flightIdleSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.flightIdleSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.flightIdleSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		return emissionIndexSoot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSootEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexSoot = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleSootEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleSootEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleSootEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleSootEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleSootEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleSootEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleSootEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleSootEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSoot = super.groundIdleSootEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSoot = super.groundIdleSootEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSoot = super.groundIdleSootEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSoot = super.groundIdleSootEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSoot;
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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.takeOffCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {


			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.aprCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.aprCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {

		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {
		
			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.climbCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.climbCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.continuousCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.cruiseCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.flightIdleCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexCO2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getCO2EmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexCO2 = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleCO2EmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleCO2EmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleCO2EmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleCO2EmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleCO2EmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleCO2EmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleCO2EmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleCO2EmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexCO2 = super.groundIdleCO2EmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

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
		case FIDL:
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
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.takeOffH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.aprH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.aprH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.climbH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.climbH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
	
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.continuousH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.continuousH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.cruiseH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.flightIdleH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getH2OEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexH2O = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleH2OEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleH2OEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleH2OEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleH2OEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleH2OEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleH2OEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleH2OEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleH2OEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexH2O = super.groundIdleH2OEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexH2O;
	}

	@Override
	public double getSOxEmissionIndex(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting, EngineOperatingConditionEnum flightCondition) {

		double emissionIndexSOx = 0.0;
		
		switch (flightCondition) {
		case TAKE_OFF:
			emissionIndexSOx = getSOxEmissionIndexTakeOff(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case APR:
			emissionIndexSOx = getSOxEmissionIndexAPR(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CLIMB:
			emissionIndexSOx = getSOxEmissionIndexClimb(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CONTINUOUS:
			emissionIndexSOx = getSOxEmissionIndexContinuous(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case CRUISE:
			emissionIndexSOx = getSOxEmissionIndexCruise(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case FIDL:
			emissionIndexSOx = getSOxEmissionIndexFlightIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		case GIDL:
			emissionIndexSOx = getSOxEmissionIndexGroundIdle(mach, altitude, deltaTemperature, throttleSetting);
			break;
		default:
			break;
		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexTakeOff(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.takeOffSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.takeOffSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.takeOffSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.takeOffSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.takeOffSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.takeOffSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.takeOffSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.takeOffSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.takeOffSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.takeOffSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.takeOffSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.takeOffSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexAPR(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.aprSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.aprSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.aprSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.aprSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.aprSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.aprSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.aprSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.aprSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.aprSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.aprSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.aprSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.aprSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexClimb(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.climbSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.climbSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.climbSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.climbSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.climbSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.climbSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.climbSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.climbSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.climbSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.climbSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.climbSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.climbSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}
		
		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexContinuous(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
	
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.continuousSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.continuousSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.continuousSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.continuousSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.continuousSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.continuousSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.continuousSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.continuousSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.continuousSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.continuousSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.continuousSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.continuousSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexCruise(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.cruiseSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.cruiseSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.cruiseSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.cruiseSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.cruiseSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.cruiseSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.cruiseSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.cruiseSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.cruiseSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.cruiseSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.cruiseSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.cruiseSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexFlightIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.flightIdleSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.flightIdleSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.flightIdleSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.flightIdleSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.flightIdleSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.flightIdleSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.flightIdleSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.flightIdleSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.flightIdleSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.flightIdleSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.flightIdleSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.flightIdleSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getSOxEmissionIndexGroundIdle(double mach, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double throttleSetting) {
		
		double emissionIndexSOx = 0.0;
		double[] inputArray = new double[] {
				altitude.doubleValue((Unit<Length>) Unit.valueOf(getAltitudeUnit())),
				mach,
				deltaTemperature.doubleValue((Unit<Temperature>) Unit.valueOf(getDeltaTemperatureUnit())),
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
		
		double[] interpolatingFunctionVariableUpperBounds = new double[4];
		interpolatingFunctionVariableUpperBounds[0] = super.groundIdleSOxEmissionIndexFunction.getxMax();
		interpolatingFunctionVariableUpperBounds[1] = super.groundIdleSOxEmissionIndexFunction.getyMax();
		interpolatingFunctionVariableUpperBounds[2] = super.groundIdleSOxEmissionIndexFunction.getzMax();
		interpolatingFunctionVariableUpperBounds[3] = super.groundIdleSOxEmissionIndexFunction.getkMax();
		
		double[] interpolatingFunctionVariableLowerBounds = new double[4];
		interpolatingFunctionVariableLowerBounds[0] = super.groundIdleSOxEmissionIndexFunction.getxMin();
		interpolatingFunctionVariableLowerBounds[1] = super.groundIdleSOxEmissionIndexFunction.getyMin();
		interpolatingFunctionVariableLowerBounds[2] = super.groundIdleSOxEmissionIndexFunction.getzMin();
		interpolatingFunctionVariableLowerBounds[3] = super.groundIdleSOxEmissionIndexFunction.getkMin();

		if( (inputArray[0] >= interpolatingFunctionVariableLowerBounds[0]
				&& inputArray[0] <= interpolatingFunctionVariableUpperBounds[0] )
				&& (inputArray[1] >= interpolatingFunctionVariableLowerBounds[1]
						&& inputArray[1] <= interpolatingFunctionVariableUpperBounds[1] )
				&& (inputArray[2] >= interpolatingFunctionVariableLowerBounds[2]
						&& inputArray[2] <= interpolatingFunctionVariableUpperBounds[2] )
				&& (inputArray[3] >= interpolatingFunctionVariableLowerBounds[3]
						&& inputArray[3] <= interpolatingFunctionVariableUpperBounds[3] )  ) {

			if(numberOfReducedInput == 1) 
				emissionIndexSOx = super.groundIdleSOxEmissionIndexFunction.value(inputArray[indexList.get(0)]);
			else if(numberOfReducedInput == 2) 
				emissionIndexSOx = super.groundIdleSOxEmissionIndexFunction.valueBilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)]);
			else if(numberOfReducedInput == 3) 
				emissionIndexSOx = super.groundIdleSOxEmissionIndexFunction.valueTrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)]);
			else if(numberOfReducedInput == 4) 
				emissionIndexSOx = super.groundIdleSOxEmissionIndexFunction.valueQuadrilinear(inputArray[indexList.get(0)], inputArray[indexList.get(1)], inputArray[indexList.get(2)], inputArray[indexList.get(3)]);

		}

		return emissionIndexSOx;
	}
	
	public double getByPassRatio() {
		return byPassRatio;
	}

	public void setByPassRatio(double byPassRatio) {
		this.byPassRatio = byPassRatio;
	}

	public String getAltitudeUnit() {
		return altitudeUnit;
	}

	public void setAltitudeUnit(String altitudeUnit) {
		this.altitudeUnit = altitudeUnit;
	}

	public String getDeltaTemperatureUnit() {
		return deltaTemperatureUnit;
	}

	public void setDeltaTemperatureUnit(String deltaTemperatureUnit) {
		this.deltaTemperatureUnit = deltaTemperatureUnit;
	}

}

