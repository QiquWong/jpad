package database.databasefunctions.aerodynamics;

import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;

public class HighLiftDatabaseReader extends DatabaseReader{
	
	private MyInterpolatingFunction deltaC_Cf_vs_delta_flap,
									dCl_dDelta_vs_cs_c,
									deltaCLmaxBase_vs_tc,
									eta_delta_vs_delta_flap,
									eta_delta_vs_delta_flap_plain,
									eta_delta_vs_delta_slat,
									etaMax_vs_LEradius_tickness_ratio,
									K1_vs_flapChordRatio,
									K2_vs_delta_flap,
									K3_vs_df_dfRef,
									Kb_vs_flapSpanRatio,
									Kc_vs_AR,
									deltaAlphaMax_vs_DeltaFlap,
									mu_1_vs_cf_c_First_Slotted_Fowler,
									mu_1_vs_cf_c_First_Plain,
									mu_2_vs_bf_b,
									mu_3_vs_bf_b;

	public HighLiftDatabaseReader(String databaseFolderPath, String databaseFileName) {
		super(databaseFolderPath, databaseFileName);
		
		deltaC_Cf_vs_delta_flap = database.interpolate2DFromDatasetFunction("DeltacCf_vs_deltaf");
		dCl_dDelta_vs_cs_c = database.interpolate1DFromDatasetFunction("DClsuDdelta_vs_SlatChordRatio");
		deltaCLmaxBase_vs_tc = database.interpolate2DFromDatasetFunction("DeltaClmaxBase_vs_airfoilThickness");
		eta_delta_vs_delta_flap = database.interpolate2DFromDatasetFunction("EtaDelta_vs_DeltaFlap");
		eta_delta_vs_delta_flap_plain = database.interpolate2DFromDatasetFunction("EtaDelta_vs_DeltaFlap_Plain");
		eta_delta_vs_delta_slat = database.interpolate1DFromDatasetFunction("EtaDelta_vs_DeltaSlat");
		etaMax_vs_LEradius_tickness_ratio = database.interpolate1DFromDatasetFunction("EtaDeltaMax_vs_LEradius_thickness");
		K1_vs_flapChordRatio = database.interpolate2DFromDatasetFunction("K1_vs_FlapChordRatio");
		K2_vs_delta_flap = database.interpolate2DFromDatasetFunction("K2_vs_deltaf");
		K3_vs_df_dfRef = database.interpolate2DFromDatasetFunction("K3_vs_df_dfRef");
		Kb_vs_flapSpanRatio = database.interpolate1DFromDatasetFunction("Kb_vs_flapSpanRatio");
		Kc_vs_AR = database.interpolate2DFromDatasetFunction("Kc_vs_AR_vs_alphaDelta");
		deltaAlphaMax_vs_DeltaFlap = database.interpolate1DFromDatasetFunction("DeltaAlphaMax_vs_DeltaFlap");
		mu_1_vs_cf_c_First_Slotted_Fowler = database.interpolate1DFromDatasetFunction("Mu_1_pitching_moment_Slotted_Fowler");
		mu_1_vs_cf_c_First_Plain = database.interpolate2DFromDatasetFunction("Mu_1_pitching_moment_Plain");
		mu_2_vs_bf_b = database.interpolate2DFromDatasetFunction("Mu_2_pitching_moment");
		mu_3_vs_bf_b = database.interpolate2DFromDatasetFunction("Mu_3_pitching_moment");
	}
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param delta_flap from 0° to 60°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double get_deltaC_Cf_vs_delta_flap(double delta_flap, double flapTypeIndex) {
		return deltaC_Cf_vs_delta_flap.value(delta_flap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param slatChordRatio
	 * @return the interpolated value of the curve at that Slat chord ratio
	 */
	public double get_dCl_dDelta_vs_cs_c(double slatChordRatio) {
		return dCl_dDelta_vs_cs_c.value(slatChordRatio);
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
	public double get_deltaCLmaxBase_vs_tc(double tc, double flapTypeIndex) {
		return deltaCLmaxBase_vs_tc.value(tc*100, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param delta_flap from 0° to 80°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * 		  without 3 because Plain Flap is handled separately.
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double get_eta_delta_vs_delta_flap(double delta_flap, double flapTypeIndex) {
		return eta_delta_vs_delta_flap.value(delta_flap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param delta_flap from 0° to 60°
	 * @param cf_c the flap chord ratio
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double get_eta_delta_vs_delta_flap_plain(double delta_flap, double cf_c) {
		return eta_delta_vs_delta_flap_plain.value(delta_flap, cf_c);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param delta_slat from 0° to 35°
	 * @return the interpolated value of the curve at that slat deflection
	 */
	public double get_eta_delta_vs_delta_slat(double delta_slat) {
		return eta_delta_vs_delta_slat.value(delta_slat);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param LEradiusRatio LEradius of the airfoil divided by the airfoil chord
	 * @param tc airfoil thickness ratio 
	 * @return the interpolated value of the curve at that LEradius/t value
	 */
	public double get_etaMax_vs_LEradius_tickness_ratio(double LEradiusRatio, double tc) {
		return etaMax_vs_LEradius_tickness_ratio.value(LEradiusRatio/tc);
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
	public double get_K1_vs_flapChordRatio(double flapChordRatio, double flapTypeIndex) {
		return K1_vs_flapChordRatio.value(flapChordRatio*100, flapTypeIndex);
	}

	/**
	 * @author Vittorio Trifari
	 * @param delta_flap from 0° to 60°
	 * @param flapTypeIndex from 1.0 to 5.0 for the required flap type:
	 * 		  1 = Single Slotted Flap
	 * 		  2 = Double Slotted Flap
	 * 		  3 = Plain Flap
	 * 		  4 = Fowler Flap
	 * 		  5 = Triple Slotted Flap
	 * @return the interpolated value of the specific curve at that flap deflection
	 */
	public double get_K2_vs_delta_flap(double delta_flap, double flapTypeIndex) {
		return K2_vs_delta_flap.value(delta_flap, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param delta_flap
	 * @param delta_flap_ref the reference flap type deflection:
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
	public double get_K3_vs_df_dfRef(double delta_flap, double delta_flap_ref, double flapTypeIndex) {
		return K3_vs_df_dfRef.value(delta_flap/delta_flap_ref, flapTypeIndex);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param b wing span
	 * @param eta_in adimensional position of the flap inner station
	 * @param eta_out adimensional position of the flap inner station
	 * @return the interpolated value of the curve at that flap span ratio
	 */
	public double get_Kb_vs_flapSpanRatio(double eta_in, double eta_out) {
		return Kb_vs_flapSpanRatio.value(eta_out)-Kb_vs_flapSpanRatio.value(eta_in);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param ar wing aspect ratio
	 * @param alphaDelta see Baia-De Rosa thesis pag.16
	 * @return the interpolated value of the curve at that AR and that alphaDelta values
	 */
	public double get_Kc_vs_AR(double ar, double alphaDelta) {
		return Kc_vs_AR.value(ar, alphaDelta);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param delta_flap from 0° to 60°
	 * @return the interpolated value of the curve at that flap span ratio
	 */
	public double get_DeltaAlphaMax_vs_DeltaFlap(double delta_flap) {
		return deltaAlphaMax_vs_DeltaFlap.value(delta_flap);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cf_c_first 
	 * @return the interpolated value of the curve at that cf/c'
	 */
	public double get_mu_1_vs_cf_c_First_Slotted_Fowler(double cf_c_first) {
		return mu_1_vs_cf_c_First_Slotted_Fowler.value(cf_c_first);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cf_c_first
	 * @param deltaFlap from 10° to 60°
	 * @return the interpolated value of the curve at that cf/c' and at that flap deflection
	 */
	public double get_mu_1_vs_cf_c_First_Plain(double cf_c_first, double deltaFlap) {
		return mu_1_vs_cf_c_First_Plain.value(cf_c_first, deltaFlap);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param eta_in adimensional position of the flap inner station
	 * @param eta_out adimensional position of the flap inner station
	 * @param taperRatio
	 * @return the interpolated value of the curve at that flap span ratio for that taper ratio
	 */
	public double get_mu_2_vs_bf_b(double eta_in, double eta_out, double taperRatio) {
		return mu_2_vs_bf_b.value(eta_out-eta_in, taperRatio);
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param eta_in adimensional position of the flap inner station
	 * @param eta_out adimensional position of the flap inner station
	 * @param taperRatio
	 * @return he interpolated value of the curve at that flap span ratio for that taper ratio
	 */
	public double get_mu_3_vs_bf_b(double eta_in, double eta_out, double taperRatio) {
		return mu_3_vs_bf_b.value(eta_out-eta_in, taperRatio);
	}
		}