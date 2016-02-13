package database.databasefunctions.aerodynamics;

import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;

public class HighLiftDatabaseReader extends DatabaseReader{
	
	private MyInterpolatingFunction deltaCCfVsDeltaFlap,
									dCldDeltaVsCsC,
									deltaCLmaxBaseVsTc,
									etaDeltaVsDeltaFlap,
									etaDeltaVsDeltaFlapPlain,
									etaDeltaVsDeltaSlat,
									etaMaxVsLEradiusTicknessRatio,
									k1VsFlapChordRatio,
									k2VsDeltaFlap,
									k3VsDfDfRef,
									kbVsFlapSpanRatio,
									kcVsAR,
									deltaAlphaMaxVsDeltaFlap,
									mu1VsCfCFirstSlottedFowler,
									mu1VsCfCFirstPlain,
									mu2VsBfB,
									mu3VsBfB,
									delta1VsCfCPlain,
									delta1VsCfCSlotted,
									delta2VsDeltaFlapPlain,
									delta2VsDeltaFlapSlotted,
									delta3VsBfB;

	public HighLiftDatabaseReader(String databaseFolderPath, String databaseFileName) {
		super(databaseFolderPath, databaseFileName);
		
		deltaCCfVsDeltaFlap = database.interpolate2DFromDatasetFunction("DeltacCf_vs_deltaf");
		dCldDeltaVsCsC = database.interpolate1DFromDatasetFunction("DClsuDdelta_vs_SlatChordRatio");
		deltaCLmaxBaseVsTc = database.interpolate2DFromDatasetFunction("DeltaClmaxBase_vs_airfoilThickness");
		etaDeltaVsDeltaFlap = database.interpolate2DFromDatasetFunction("EtaDelta_vs_DeltaFlap");
		etaDeltaVsDeltaFlapPlain = database.interpolate2DFromDatasetFunction("EtaDelta_vs_DeltaFlap_Plain");
		etaDeltaVsDeltaSlat = database.interpolate1DFromDatasetFunction("EtaDelta_vs_DeltaSlat");
		etaMaxVsLEradiusTicknessRatio = database.interpolate1DFromDatasetFunction("EtaDeltaMax_vs_LEradius_thickness");
		k1VsFlapChordRatio = database.interpolate2DFromDatasetFunction("K1_vs_FlapChordRatio");
		k2VsDeltaFlap = database.interpolate2DFromDatasetFunction("K2_vs_deltaf");
		k3VsDfDfRef = database.interpolate2DFromDatasetFunction("K3_vs_df_dfRef");
		kbVsFlapSpanRatio = database.interpolate1DFromDatasetFunction("Kb_vs_flapSpanRatio");
		kcVsAR = database.interpolate2DFromDatasetFunction("Kc_vs_AR_vs_alphaDelta");
		deltaAlphaMaxVsDeltaFlap = database.interpolate1DFromDatasetFunction("DeltaAlphaMax_vs_DeltaFlap");
		mu1VsCfCFirstSlottedFowler = database.interpolate1DFromDatasetFunction("Mu_1_pitching_moment_Slotted_Fowler");
		mu1VsCfCFirstPlain = database.interpolate2DFromDatasetFunction("Mu_1_pitching_moment_Plain");
		mu2VsBfB = database.interpolate2DFromDatasetFunction("Mu_2_pitching_moment");
		mu3VsBfB = database.interpolate2DFromDatasetFunction("Mu_3_pitching_moment");
		delta1VsCfCPlain = database.interpolate2DFromDatasetFunction("Delta1_vs_cf_c_Plain");
		delta1VsCfCSlotted = database.interpolate2DFromDatasetFunction("Delta1_vs_cf_c_Slotted");
		delta2VsDeltaFlapPlain = database.interpolate1DFromDatasetFunction("Delta2_vs_DeltaFlap_Plain");
		delta2VsDeltaFlapSlotted = database.interpolate2DFromDatasetFunction("Delta2_vs_delta_flap_Slotted");
		delta3VsBfB = database.interpolate2DFromDatasetFunction("Delta3_vs_bf_b");
	}
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param deltaFlap from 0° to 60°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double getDeltaCCfVsDeltaFlap(double deltaFlap, double flapTypeIndex) {
		return deltaCCfVsDeltaFlap.value(deltaFlap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param slatChordRatio
	 * @return the interpolated value of the curve at that Slat chord ratio
	 */
	public double getDCldDeltaVsCsC(double slatChordRatio) {
		return dCldDeltaVsCsC.value(slatChordRatio);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param tc airfoil thickness ratio 
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that thickness ratio (%)
	 */
	public double getDeltaCLmaxBaseVsTc(double tc, double flapTypeIndex) {
		return deltaCLmaxBaseVsTc.value(tc*100, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 80°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * 		  without 3 because Plain Flap is handled separately.
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double getEtaDeltaVsDeltaFlap(double deltaFlap, double flapTypeIndex) {
		return etaDeltaVsDeltaFlap.value(deltaFlap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 60°
	 * @param cfc the flap chord ratio
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double getEtaDeltaVsDeltaFlapPlain(double deltaFlap, double cfc) {
		return etaDeltaVsDeltaFlapPlain.value(deltaFlap, cfc);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaSlat from 0° to 35°
	 * @return the interpolated value of the curve at that slat deflection
	 */
	public double getEtaDeltaVsDeltaSlat(double deltaSlat) {
		return etaDeltaVsDeltaSlat.value(deltaSlat);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param LEradiusRatio LEradius of the airfoil divided by the airfoil chord
	 * @param tc airfoil thickness ratio 
	 * @return the interpolated value of the curve at that LEradius/t value
	 */
	public double getEtaMaxVsLEradiusTicknessRatio(double LEradiusRatio, double tc) {
		return etaMaxVsLEradiusTicknessRatio.value(LEradiusRatio/tc);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param flapChordRatio
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap chord ratio (%)
	 */
	public double getK1vsFlapChordRatio(double flapChordRatio, double flapTypeIndex) {
		return k1VsFlapChordRatio.value(flapChordRatio*100, flapTypeIndex);
	}

	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 60°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double getK2VsDeltaFlap(double deltaFlap, double flapTypeIndex) {
		return k2VsDeltaFlap.value(deltaFlap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap
	 * @param deltaFlapRef the reference flap type deflection:
	 * 		  40° = Single Slotted Flap
	 * 		  45° = Double Slotted Flap
	 * 		  60° = Split Flap
	 * 		  60° = Plain Flap
	 * 		  40° = Fowler Flap
	 * 		  50° = Triple Slotted Flap
	 * @param flapTypeIndex from 1.0 to 6.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Split Flap
	 * 		  4 = Plain Flap
	 * 		  5 = Fowler Flap
	 * 		  6 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap deflection ratio
	 */
	public double getK3VsDfDfRef(double deltaFlap, double deltaFlapRef, double flapTypeIndex) {
		return k3VsDfDfRef.value(deltaFlap/deltaFlapRef, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param b wing span
	 * @param etaIn adimensional position of the flap inner station
	 * @param etaOut adimensional position of the flap inner station
	 * @return the interpolated value of the curve at that flap span ratio
	 */
	public double getKbVsFlapSpanRatio(double etaIn, double etaOut) {
		return kbVsFlapSpanRatio.value(etaOut)-kbVsFlapSpanRatio.value(etaIn);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param ar wing aspect ratio
	 * @param alphaDelta see Baia-De Rosa thesis pag.16
	 * @return the interpolated value of the curve at that AR and that alphaDelta values
	 */
	public double getKcVsAR(double ar, double alphaDelta) {
		return kcVsAR.value(ar, alphaDelta);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 60°
	 * @return the interpolated value of the curve at that flap span ratio
	 */
	public double getDeltaAlphaMaxVsDeltaFlap(double deltaFlap) {
		return deltaAlphaMaxVsDeltaFlap.value(deltaFlap);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cfcFirst 
	 * @return the interpolated value of the curve at that cf/c'
	 */
	public double getMu1VsCfCFirstSlottedFowler(double cfcFirst) {
		return mu1VsCfCFirstSlottedFowler.value(cfcFirst);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cfcFirst
	 * @param deltaFlap from 10° to 60°
	 * @return the interpolated value of the curve at that cf/c' and at that flap deflection
	 */
	public double getMu1VsCfCFirstPlain(double cfcFirst, double deltaFlap) {
		return mu1VsCfCFirstPlain.value(cfcFirst, deltaFlap);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param etaIn adimensional position of the flap inner station
	 * @param etaOut adimensional position of the flap inner station
	 * @param taperRatio
	 * @return the interpolated value of the curve at that flap span ratio for that taper ratio
	 */
	public double getMu2VsBfB(double etaIn, double etaOut, double taperRatio) {
		return mu2VsBfB.value(etaOut-etaIn, taperRatio);
	//	return mu_2_vs_bf_b.value(eta_out, taperRatio)-mu_2_vs_bf_b.value(eta_in, taperRatio);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param etaIn adimensional position of the flap inner station
	 * @param etaOut adimensional position of the flap inner station
	 * @param taperRatio
	 * @return the interpolated value of the curve at that flap span ratio for that taper ratio
	 */
	public double getMu3VsBfB(double etaIn, double etaOut, double taperRatio) {
		return mu3VsBfB.value(etaOut-etaIn, taperRatio);
	//  return mu_3_vs_bf_b.value(eta_out, taperRatio)-mu_3_vs_bf_b.value(eta_in, taperRatio);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cfc flap chord to wing chord ratio
	 * @param tc thickness to chord ratio
	 * @return the interpolated value of the curve at that cf_c for that tc
	 */
	public double getDelta1VsCfCPlain(double cfc, double tc) {
		return delta1VsCfCPlain.value(cfc, tc);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cfc flap chord to wing chord ratio
	 * @param tc thickness to chord ratio
	 * @return the interpolated value of the curve at that cf_c for that tc
	 */
	public double getDelta1VsCfCSlotted(double cfc, double tc) {
		return delta1VsCfCSlotted.value(cfc, tc);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 63°
	 * @return the interpolated value of the curve at that flap deflection
	 */
	public double getDelta2VsDeltaFlapPlain(double deltaFlap) {
		return delta2VsDeltaFlapPlain.value(deltaFlap);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param deltaFlap from 0° to 100°
	 * @param tc thickness to chord ratio
	 * @return the interpolated value of the curve at that flap deflection for that tc
	 */
	public double getDelta2VsDeltaFlapSlotted(double deltaFlap, double tc) {
		return delta2VsDeltaFlapSlotted.value(deltaFlap, tc);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param etaIn adimensional position of the flap inner station 
	 * @param etaOut adimensional position of the flap inner station
	 * @param taperRatio
	 * @return
	 */
	public double getDelta3VsBfB(double etaIn, double etaOut, double taperRatio) {
		return delta3VsBfB.value(etaOut-etaIn, taperRatio);
	}
}