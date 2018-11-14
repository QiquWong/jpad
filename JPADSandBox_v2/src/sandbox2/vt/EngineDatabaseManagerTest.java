package sandbox2.vt;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;

public class EngineDatabaseManagerTest {

	public static void main(String[] args) {
	
		System.out.println("-----------------------------");
		System.out.println("Engine Database Manager Test");
		System.out.println("-----------------------------");
		
		System.out.println("\nInitializing folders ...");
		MyConfiguration.initWorkingDirectoryTree();
		
		String engineDatabaseFileDirectory = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String engineDatabaseFileName = "EngineDatabaseTP.xlsx";
		System.out.println("Engine database file --> " + engineDatabaseFileDirectory + engineDatabaseFileName);
		System.out.println("Done!");
		
		System.out.println("\nCreating the Database Manager ...");
		EngineDatabaseManager engineDatabaseManager = DatabaseManager.initializeEngineDatabase(
				new EngineDatabaseManager(), 
				engineDatabaseFileDirectory, 
				engineDatabaseFileName
				);
		System.out.println("\n\nDone!");
		
		System.out.println("\nTesting getters ...");
		
		Map<EngineOperatingConditionEnum, List<Double[]>> engineDeckInput = new HashMap<>();
		engineDeckInput.put(
				EngineOperatingConditionEnum.TAKE_OFF, 
				createInputList(
						0.0, 9000.0, 25,
						0.0, 0.25, 10,
						0.0, 10.0, 3,
						0, 1, 2
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.APR, 
				createInputList(
						0.0, 8000.0, 25,
						0.0, 0.25, 10,
						0.0, 10.0, 3,
						0, 1, 2
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.CLIMB, 
				createInputList(
						0.0, 30000.0, 25,
						0.2, 0.7, 10,
						0.0, 10.0, 3,
						0, 1, 2
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.CONTINUOUS, 
				createInputList(
						0.0, 20000.0, 25,
						0.2, 0.5, 10,
						0.0, 10.0, 3,
						0, 1, 2
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.CRUISE, 
				createInputList(
						0.0, 30000.0, 25,
						0.3, 0.7, 10,
						0.0, 10.0, 3,
						0.4, 1, 10
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.FIDL, 
				createInputList(
						0.0, 30000.0, 25,
						0.3, 0.7, 10,
						0.0, 10.0, 3,
						0, 1, 2
						)
				);
		
		engineDeckInput.put(
				EngineOperatingConditionEnum.GIDL, 
				createInputList(
						0.0, 1000.0, 2,
						0.0, 0.3, 10,
						0.0, 10.0, 2,
						0, 1, 2
						)
				);
		
		Map<EngineOperatingConditionEnum, List<Double[]>> engineDeckOutput = new HashMap<>();
		System.out.println("\n TAKE-OFF");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.TAKE_OFF, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.TAKE_OFF),
						engineDatabaseManager,
						EngineOperatingConditionEnum.TAKE_OFF
						)
				);
		System.out.println("\n APR");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.APR, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.APR),
						engineDatabaseManager,
						EngineOperatingConditionEnum.APR
						)
				);
		System.out.println("\n CLIMB");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.CLIMB, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.CLIMB),
						engineDatabaseManager,
						EngineOperatingConditionEnum.CLIMB
						)
				);
		System.out.println("\n CONTINUOUS");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.CONTINUOUS, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.CONTINUOUS),
						engineDatabaseManager,
						EngineOperatingConditionEnum.CONTINUOUS
						)
				);
		System.out.println("\n CRUISE");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.CRUISE, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.CRUISE),
						engineDatabaseManager,
						EngineOperatingConditionEnum.CRUISE
						)
				);
		System.out.println("\n FLIGHT IDLE");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.FIDL, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.FIDL),
						engineDatabaseManager,
						EngineOperatingConditionEnum.FIDL
						)
				);
		System.out.println("\n GROUND IDLE");
		engineDeckOutput.put(
				EngineOperatingConditionEnum.GIDL, 
				createOutputList(
						engineDeckInput.get(EngineOperatingConditionEnum.GIDL),
						engineDatabaseManager,
						EngineOperatingConditionEnum.GIDL
						)
				);
		
		System.out.println("Done!");
		
	}
	
	public static List<Double[]> createInputList( 
			double minAltitude,
			double maxAltitude,
			int nAltitude,
			double minMach,
			double maxMach,
			int nMach,
			double minDeltaTemp,
			double maxDeltaTemp,
			int nDeltaTemp,
			double minThrottle,
			double maxThrottle,
			int nThrottle
			) {
		List<Double[]> inputList = new ArrayList<>();
		Double[] altitudeArray = MyArrayUtils.linspaceDouble(minAltitude, maxAltitude, nAltitude);
		Double[] machArray = MyArrayUtils.linspaceDouble(minMach, maxMach, nMach);
		Double[] deltaTemperatureArray = MyArrayUtils.linspaceDouble(minDeltaTemp, maxDeltaTemp, nDeltaTemp);
		Double[] throttleArray = MyArrayUtils.linspaceDouble(minThrottle, maxThrottle, nThrottle);
		
		inputList.add(altitudeArray);
		inputList.add(machArray);
		inputList.add(deltaTemperatureArray);
		inputList.add(throttleArray);
		
		return inputList;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Double[]> createOutputList(List<Double[]> inputList, EngineDatabaseManager engineDatabaseManager, EngineOperatingConditionEnum engineOperatingCondition) {
		
		List<Double[]> outputList = new ArrayList<>();
		List<Double> thrustRatioArray = new ArrayList<>();
		List<Double> sfcArray = new ArrayList<>();
		List<Double> emissionIndexNOxArray = new ArrayList<>();
		List<Double> emissionIndexCOArray = new ArrayList<>();
		List<Double> emissionIndexHCArray = new ArrayList<>();
		List<Double> emissionIndexSootArray = new ArrayList<>();
		List<Double> emissionIndexCO2Array = new ArrayList<>();
		List<Double> emissionIndexSOxArray = new ArrayList<>();
		List<Double> emissionIndexH2OArray = new ArrayList<>();
		
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		numberFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		System.out.println("\th (" + engineDatabaseManager.getAltitudeUnit() + ")"
				+ "\tM"
				+ "\tdT (" + engineDatabaseManager.getDeltaTemperatureUnit() + ")"
				+ "\tphi"
				+ "\tT/T0"
				+ "\tSFC"
				+ "\tNOx_EI"
				+ "\tCO_EI"
				+ "\tHC_EI"
				+ "\tSoot_EI"
				+ "\tCO2_EI"
				+ "\tSOx_EI"
				+ "\tH2O_EI"
				);
		for(int iAltitude=0; iAltitude<inputList.get(0).length; iAltitude++) {
			for (int iMach=0; iMach<inputList.get(1).length; iMach++) {
				for (int iDeltaTemp=0; iDeltaTemp<inputList.get(2).length; iDeltaTemp++) {
					for (int iThrottle=0; iThrottle<inputList.get(3).length; iThrottle++) {
						
						System.out.println("\t" +
								numberFormat.format(inputList.get(0)[iAltitude]) + "\t"
										+ numberFormat.format(inputList.get(1)[iMach]) + "\t"
										+ numberFormat.format(inputList.get(2)[iDeltaTemp]) + "\t"
										+ numberFormat.format(inputList.get(3)[iThrottle]) + "\t"
										+ numberFormat.format(
												engineDatabaseManager.getThrustRatio(
														inputList.get(1)[iMach],
														Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
														Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
														inputList.get(3)[iThrottle],
														engineOperatingCondition
														)
												) + "\t"
												+ numberFormat.format(
														engineDatabaseManager.getSfc(
																inputList.get(1)[iMach],
																Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																inputList.get(3)[iThrottle],
																engineOperatingCondition
																)
														) + "\t"
														+ numberFormat.format(
																engineDatabaseManager.getNOxEmissionIndex(
																		inputList.get(1)[iMach],
																		Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																		Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																		inputList.get(3)[iThrottle],
																		engineOperatingCondition
																		)
																) + "\t"
																+ numberFormat.format(
																		engineDatabaseManager.getCOEmissionIndex(
																				inputList.get(1)[iMach],
																				Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																				Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																				inputList.get(3)[iThrottle],
																				engineOperatingCondition
																				)
																		) + "\t"
																		+ numberFormat.format(
																				engineDatabaseManager.getHCEmissionIndex(
																						inputList.get(1)[iMach],
																						Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																						Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																						inputList.get(3)[iThrottle],
																						engineOperatingCondition
																						)
																				) + "\t"
																				+ numberFormat.format(
																						engineDatabaseManager.getSootEmissionIndex(
																								inputList.get(1)[iMach],
																								Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																								Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																								inputList.get(3)[iThrottle],
																								engineOperatingCondition
																								)
																						) + "\t"
																						+ numberFormat.format(
																								engineDatabaseManager.getCO2EmissionIndex(
																										inputList.get(1)[iMach],
																										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																										inputList.get(3)[iThrottle],
																										engineOperatingCondition
																										)
																								) + "\t"
																								+ numberFormat.format(
																										engineDatabaseManager.getSOxEmissionIndex(
																												inputList.get(1)[iMach],
																												Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																												Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																												inputList.get(3)[iThrottle],
																												engineOperatingCondition
																												)
																										) + "\t"
																										+ numberFormat.format(
																												engineDatabaseManager.getH2OEmissionIndex(
																														inputList.get(1)[iMach],
																														Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
																														Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
																														inputList.get(3)[iThrottle],
																														engineOperatingCondition
																														)
																												) + "\t"
								);

						thrustRatioArray.add(
								engineDatabaseManager.getThrustRatio(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						sfcArray.add(
								engineDatabaseManager.getSfc(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexNOxArray.add(
								engineDatabaseManager.getNOxEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexCOArray.add(
								engineDatabaseManager.getCOEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexHCArray.add(
								engineDatabaseManager.getHCEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexSootArray.add(
								engineDatabaseManager.getSootEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexCO2Array.add(
								engineDatabaseManager.getCO2EmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexSOxArray.add(
								engineDatabaseManager.getSOxEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
						emissionIndexH2OArray.add(
								engineDatabaseManager.getH2OEmissionIndex(
										inputList.get(1)[iMach],
										Amount.valueOf(inputList.get(0)[iAltitude], (Unit<Length>) Unit.valueOf(engineDatabaseManager.getAltitudeUnit())),
										Amount.valueOf(inputList.get(2)[iDeltaTemp], (Unit<Temperature>) Unit.valueOf(engineDatabaseManager.getDeltaTemperatureUnit())),
										inputList.get(3)[iThrottle],
										engineOperatingCondition
										)
								);
					}
				}
			}
		}
					
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(thrustRatioArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(sfcArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexNOxArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexCOArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexHCArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexSootArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexCO2Array));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexSOxArray));
		outputList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(emissionIndexH2OArray));
		
		return outputList;
	}
	
}
