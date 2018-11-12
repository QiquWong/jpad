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
import flanagan.interpolation.PolyCubicSpline;
import standaloneutils.MyArrayUtils;

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
	
	/*
	 * Index legend:
	 * 0 = Thrust Ratio
	 * 1 = TSFC [lb/lb*hr]
	 * 2 = NOx Emission Index
	 * 3 = CO Emission Index
	 * 4 = HC Emission Index
	 * 5 = CO2 Emission Index
	 * 6 = H2O Emission Index
	 */
	private Map<Integer, PolyCubicSpline> interpolatedTakeOffDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedAPRDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedClimbDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedContinuousDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedCruiseDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedFlightIdleDataMap;
	private Map<Integer, PolyCubicSpline> interpolatedGroundIdleDataMap;

	//-------------------------------------------------------------------
	// BUILDER
	//-------------------------------------------------------------------
	public EngineDatabaseManager_v2(String databaseFolderPath, String engineDatabaseFileName) {

		importDatabaseFromFile(databaseFolderPath, engineDatabaseFileName);
		
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
				System.out.println("\n" + workbook.getSheetName(i));
				System.out.println("Iterating over Rows and Columns ...");
				List<List<String>> sheetData = new ArrayList<>();
				workbook.getSheetAt(i).forEach(row -> {
					List<String> sheetRowData = new ArrayList<>();
					row.forEach(cell -> {
						sheetRowData.add(dataFormatter.formatCellValue(cell));
					});
					sheetData.add(sheetRowData.stream().filter(data -> !data.isEmpty()).collect(Collectors.toList()));
				});
				dataMap.put(workbook.getSheetName(i), sheetData);
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * CREATING INTERPOLATING FUNCTIONS ...
		 */
		createInterpolatedDataMap(dataMap, "TAKE-OFF", interpolatedTakeOffDataMap);
		createInterpolatedDataMap(dataMap, "APR", interpolatedAPRDataMap);
		createInterpolatedDataMap(dataMap, "CLIMB", interpolatedClimbDataMap);
		createInterpolatedDataMap(dataMap, "CONTINUOUS", interpolatedContinuousDataMap);
		createInterpolatedDataMap(dataMap, "CRUISE", interpolatedCruiseDataMap);
		createInterpolatedDataMap(dataMap, "FLIGHT IDLE", interpolatedFlightIdleDataMap);
		createInterpolatedDataMap(dataMap, "GROUND IDLE", interpolatedGroundIdleDataMap);
	}
	
	@SuppressWarnings("unchecked")
	private void createInterpolatedDataMap (
			Map<String, List<List<String>>> dataMap, 
			String engineSetting, 
			Map<Integer, PolyCubicSpline> interpolatedTakeOffDataMap
			) {
		
	    List<List<String>> sheetData = dataMap.get(engineSetting);
	    String altitudeUnit = sheetData.get(1).get(0);
	    String deltaTemperatureUnit = sheetData.get(1).get(2);
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
	    
	    List<double[]> inputDoubleArrayList = new ArrayList<>();
	    inputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(altitudeList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(machList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertListOfAmountTodoubleArray(deltaTemperatureList.stream().distinct().collect(Collectors.toList())));
	    inputDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(throttleList.stream().distinct().collect(Collectors.toList())));
	    
	    List<double[]> engineDataDoubleArrayList = new ArrayList<>();
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(thrustRatioList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(sfcList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexNOxList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexCOList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexHCList));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexCO2List));
	    engineDataDoubleArrayList.add(MyArrayUtils.convertToDoublePrimitive(emissionIndexH2OList));
	    
		List<List<List<Integer>>> columnIndexList = new ArrayList<>();
		List<List<Integer>> currentVariableElementList = new ArrayList<>();
		List<Integer> currentIndexElementList = new ArrayList<>();
		for(int i=0; i<numberOfInput; i++) {
			currentVariableElementList = new ArrayList<>();
			for(int j=0; j<inputDoubleArrayList.get(i).length; j++) {
				currentIndexElementList = new ArrayList<>();
				for(int k=2; k<sheetData.size(); k++) {
					if(Double.valueOf(sheetData.get(k).get(i).replace(',', '.')) == inputDoubleArrayList.get(i)[j])
						currentIndexElementList.add(k);
				}
				currentVariableElementList.add(currentIndexElementList);
			}
			columnIndexList.add(currentVariableElementList);
		}

		List<Integer> arrayDimensionList = inputDoubleArrayList.stream().map(x -> x.length).collect(Collectors.toList());
		
		List<List<Integer>> inputPermutationList = new ArrayList<>();
		for(int i=0; i<numberOfInput; i++) {
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
		for(int i=0; i<numberOfInput; i++) {
			variablePermutationList.add(
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									inputDoubleArrayList.get(i)
									)
							)
					);
		}
		
		Collection<List<Integer>> permutationResults = DatabaseInterpolationUtils.permutations(inputPermutationList);
		
		Map<List<Integer>, Integer> interpolatingMatrixIndexes = new HashMap<>();
		for(int i=0; i<permutationResults.size(); i++) 
			interpolatingMatrixIndexes = DatabaseInterpolationUtils.buildInterpolatingMatrixIndexes(
					columnIndexList, 
					(List<Integer>) permutationResults.toArray()[i], 
					interpolatingMatrixIndexes,
					numberOfInput
					);
		
		for(int i=0; i<numberOfOutput; i++)
			DatabaseInterpolationUtils.createMultiDimensionalMatrix(
					numberOfInput,
					i,
					inputDoubleArrayList,
					engineDataDoubleArrayList.get(i),
					interpolatingMatrixIndexes,
					interpolatedTakeOffDataMap
					);
		
		System.out.println(engineSetting + " data have been interpolated!");
		
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

	public double getByPassRatio() {
		return byPassRatio;
	}

	public void setByPassRatio(double byPassRatio) {
		this.byPassRatio = byPassRatio;
	}

}

