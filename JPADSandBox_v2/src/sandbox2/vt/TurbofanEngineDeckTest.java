package sandbox2.vt;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import configuration.enumerations.EngineOperatingConditionEnum;
import database.databasefunctions.engine.TurbofanEngineDatabaseReader;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;

public class TurbofanEngineDeckTest {

	public static void main(String[] args) {

		TurbofanEngineDatabaseReader databaseReader = new TurbofanEngineDatabaseReader(
				System.getProperty("user.dir") + File.separator + "data", 
				"TurbofanEngineDatabase.h5"
				);

		EngineOperatingConditionEnum[] operatingCondition = new EngineOperatingConditionEnum[] {
				EngineOperatingConditionEnum.TAKE_OFF,
				EngineOperatingConditionEnum.CLIMB,
				EngineOperatingConditionEnum.CRUISE,
				EngineOperatingConditionEnum.DESCENT
		};
		double[] bpr = MyArrayUtils.linspace(8.0, 12.0, 3);
		double[] altitudes = MyArrayUtils.linspace(0.0, 13716, 9);
		double[] mach = MyArrayUtils.linspace(0.0, 0.9, 50);

		Map<EngineOperatingConditionEnum, Map<Double, Map<Double, Map<Double, Tuple2<Double, Double>>>>> engineDeck = new HashMap<>(); 

		for (int i=0; i<operatingCondition.length; i++) {

			Map<Double, Map<Double, Map<Double, Tuple2<Double, Double>>>> engineDeckBPRMap = new HashMap<>();

			for (int j=0; j<bpr.length; j++) {

				Map<Double, Map<Double, Tuple2<Double, Double>>> engineDeckAltitudeMap = new HashMap<>();

				for (int k=0; k<altitudes.length; k++) {

					Map<Double, Tuple2<Double, Double>> engineDeckMachMap = new HashMap<>();

					for (int m=0; m<mach.length; m++) {

						engineDeckMachMap.put(
								mach[m],
								Tuple.of(
										databaseReader.getThrustRatio(
												mach[m],
												altitudes[k], 
												bpr[j], 
												operatingCondition[i]
												),
										databaseReader.getSFC(
												mach[m],
												altitudes[k],
												databaseReader.getThrustRatio(
														mach[m],
														altitudes[k], 
														bpr[j], 
														operatingCondition[i]
														),
												bpr[j],
												operatingCondition[i]
												)
										)
								);

					}

					engineDeckAltitudeMap.put(
							altitudes[k], 
							engineDeckMachMap
							);

				}

				engineDeckBPRMap.put(
						bpr[j], 
						engineDeckAltitudeMap
						);

			}

			engineDeck.put(
					operatingCondition[i], 
					engineDeckBPRMap
					);
			
		}
		
		for (int i=0; i<operatingCondition.length; i++) {

			System.out.println("\n\n" + operatingCondition[i].toString());
			
			for (int j=0; j<bpr.length; j++) {

				System.out.println("\n\tBPR: " + String.format(Locale.ROOT, "%.0f", bpr[j]));

				for (int k=0; k<altitudes.length; k++) {

					System.out.println("\n\t\tALTITUDE: " + String.format(Locale.ROOT, "%.0f", altitudes[k]));
					System.out.println("\t\tMACH" + "\tT/T0" + "\tSFC");

					for (int m=0; m<mach.length; m++) {

						System.out.println(
								"\t\t" + String.format(Locale.ROOT, "%.02f", mach[m]) + 
								"\t" + String.format(Locale.ROOT, "%.004f", engineDeck.get(operatingCondition[i]).get(bpr[j]).get(altitudes[k]).get(mach[m])._1()) +
								"\t" + String.format(Locale.ROOT, "%.004f", engineDeck.get(operatingCondition[i]).get(bpr[j]).get(altitudes[k]).get(mach[m])._2()));

					}

				}

			}

		}

	}

}
