package sandbox.vt;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseManager;

public class Test_ThrustDatabase {

	public static void main(String[] args) {

		System.out.println("----------------------------------------------------------------------");
		System.out.println("TEST THRUST RATIO :: TAKE-OFF CONDITION");
		System.out.println("----------------------------------------------------------------------");
		
		double[] mach = new double[] {0.10, 0.15, 0.20, 0.25};
		double[] bpr = new double[] {3.0, 6.5, 9.0, 13.0};
		double altitude = 0.0;
		EngineTypeEnum engineType = EngineTypeEnum.TURBOFAN;
		EngineOperatingConditionEnum engineOperatingCondition = EngineOperatingConditionEnum.TAKE_OFF;
		double[][] thrustRatio = new double[bpr.length][mach.length];
		
		for (int i=0; i<bpr.length; i++) {
			for(int j=0; j<mach.length; j++) {
				thrustRatio[i][j] = EngineDatabaseManager.getThrustRatio(
						mach[j],
						altitude,
						bpr[i],
						engineType,
						engineOperatingCondition
						);
			}
		}

		System.out.println("RESULTS @ BPR = 3.0" +
				"\n\tMach = " + mach[0] + " --> T/T0 = " + thrustRatio[0][0] + 
				"\n\tMach = " + mach[1] + " --> T/T0 = " + thrustRatio[0][1] +
				"\n\tMach = " + mach[2] + " --> T/T0 = " + thrustRatio[0][2] +
				"\n\tMach = " + mach[3] + " --> T/T0 = " + thrustRatio[0][3] +
				"\n\n");
		System.out.println("RESULTS @ BPR = 6.5" +
				"\n\tMach = " + mach[0] + " --> T/T0 = " + thrustRatio[1][0] + 
				"\n\tMach = " + mach[1] + " --> T/T0 = " + thrustRatio[1][1] +
				"\n\tMach = " + mach[2] + " --> T/T0 = " + thrustRatio[1][2] +
				"\n\tMach = " + mach[3] + " --> T/T0 = " + thrustRatio[1][3] +
				"\n\n");
		System.out.println("RESULTS @ BPR = 9.0" +
				"\n\tMach = " + mach[0] + " --> T/T0 = " + thrustRatio[2][0] + 
				"\n\tMach = " + mach[1] + " --> T/T0 = " + thrustRatio[2][1] +
				"\n\tMach = " + mach[2] + " --> T/T0 = " + thrustRatio[2][2] +
				"\n\tMach = " + mach[3] + " --> T/T0 = " + thrustRatio[2][3] +
				"\n\n");
		System.out.println("RESULTS @ BPR = 13.0" +
				"\n\tMach = " + mach[0] + " --> T/T0 = " + thrustRatio[3][0] + 
				"\n\tMach = " + mach[1] + " --> T/T0 = " + thrustRatio[3][1] +
				"\n\tMach = " + mach[2] + " --> T/T0 = " + thrustRatio[3][2] +
				"\n\tMach = " + mach[3] + " --> T/T0 = " + thrustRatio[3][3] +
				"\n\n");
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("TEST THRUST RATIO :: MAX CRUISE CONDITION");
		System.out.println("----------------------------------------------------------------------");
		
		double[] machCruise1 = new double[] {0.74, 0.78};
		double[] machCruise2 = new double[] {0.78, 0.80};
		double altitudeCruise1 = Amount.valueOf(30000, NonSI.FOOT).to(SI.METER).getEstimatedValue();
		double altitudeCruise2 = Amount.valueOf(35000, NonSI.FOOT).to(SI.METER).getEstimatedValue();
		EngineOperatingConditionEnum engineOperatingConditionCruise = EngineOperatingConditionEnum.CRUISE;
		double[] thrustRatioCruise1 = new double[2];
		double[] thrustRatioCruise2 = new double[2];
		
		for (int i=0; i<2; i++) 
				thrustRatioCruise1[i] = EngineDatabaseManager.getThrustRatio(
						machCruise1[i],
						altitudeCruise1,
						bpr[3],
						engineType,
						engineOperatingConditionCruise
						);
		for (int i=0; i<2; i++) 
			thrustRatioCruise2[i] = EngineDatabaseManager.getThrustRatio(
					machCruise2[i],
					altitudeCruise2,
					bpr[3],
					engineType,
					engineOperatingConditionCruise
					);

		System.out.println("RESULTS @ BPR = 9.0 AND @ 30000 ft" +
				"\n\tMach = " + machCruise1[0] + " --> T/T0 = " + thrustRatioCruise1[0] + 
				"\n\tMach = " + machCruise1[1] + " --> T/T0 = " + thrustRatioCruise1[1] +
				"\n\n");
		System.out.println("RESULTS @ BPR = 9.0 AND @ 35000 ft" +
				"\n\tMach = " + machCruise2[0] + " --> T/T0 = " + thrustRatioCruise2[0] + 
				"\n\tMach = " + machCruise2[1] + " --> T/T0 = " + thrustRatioCruise2[1] +
				"\n\n");
	}
}
