package sandbox2.gt.aerolibrary;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;

import configuration.MyConfiguration;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.database.hdf.MyHDFReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public class DatabaseReader {

	public static final String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";

	private static URL aerodynamicDatabaseFileNameWithPath = AerodynamicDatabaseReader.class.getResource(MyConfiguration.databaseFolderPath);
	private static final MyHDFReader aeroDatabase = new MyHDFReader(aerodynamicDatabaseFileNameWithPath.getPath() + File.separator + aerodynamicDatabaseFileName);

	private static final MyInterpolatingFunction cmqwKqVSAr = aeroDatabase.interpolate1DFromDatasetFunction("(C_m_q_w)_k_q_vs_AR");
	private static final MyInterpolatingFunction kyv_bvOver2r1 = aeroDatabase.interpolate1DFromDatasetFunction("(C_y_beta_v)_K_Y_V_vs_b_v_over_2_times_r1");
	private static final MyInterpolatingFunction c1_bvOver2r1 = aeroDatabase.interpolate2DFromDatasetFunction("(AR_v_eff)_c1_vs_b_v_over_2_times_r1_(lambda_v)");
	private static final MyInterpolatingFunction ar_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v = aeroDatabase.interpolate2DFromDatasetFunction("(AR_v_eff)_c2_vs_Z_h_over_b_v_(x_ac_h--v_over_c_bar_v)");
	private static final MyInterpolatingFunction kHV_ShOverSv = aeroDatabase.interpolate1DFromDatasetFunction("(C_y_beta_v)_K_Y_V_vs_b_v_over_2_times_r1");
	private static final MyInterpolatingFunction K_Rudder_vs_ChordRudderOverMAC_VT = aeroDatabase.interpolate2DFromDatasetFunction("(C_y_delta_r)_K_R_vs_eta_(lambda_v)");
	private static final MyInterpolatingFunction tau_ControlSurface_VS_chordCSOverMACVT = aeroDatabase.interpolate1DFromDatasetFunction("(control_surface)_tau_e_vs_c_control_surface_over_c_horizontal_tail");
	private static final MyInterpolatingFunction c_l_beta_w_b_over_gamma_vs_AR_TaperRatio = aeroDatabase.interpolate3DFromDatasetFunction("(C_l_beta_w_b)_C_l_beta_over_Gamma_w_vs_AR_(L_c2)_(lambda)");
	private static final MyInterpolatingFunction K_M_Gamma_vs_mach_cos_lambdamean_and_AR_over_coslambdamean = aeroDatabase.interpolate2DFromDatasetFunction("(C_l_beta_w_b)_k_M_Gamma_vs_Mach_times_cos(L_c2)_(AR_over_cos(L_c2))");
	private static final MyInterpolatingFunction clbeta_over_cl = aeroDatabase.interpolate3DFromDatasetFunction("(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(L_c2)_vs_L_c2_(AR)_(lambda)");
	private static final MyInterpolatingFunction K_M_machcoslambda_and_ARcoslambda = aeroDatabase.interpolate2DFromDatasetFunction("(C_l_beta_w_b)_k_M_L_vs_Mach_times_cos(L_c2)_(AR_over_cos(L_c2))");
	private static final MyInterpolatingFunction K_F_vs_AOverb_ARoverCos= aeroDatabase.interpolate2DFromDatasetFunction("(C_l_beta_w_b)_k_f_vs_A_over_b_(AR_over_cos(L_c2))");
	private static final MyInterpolatingFunction cl_Beta_over_Lift_vs_ar_and_taper_ratio= aeroDatabase.interpolate2DFromDatasetFunction("(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(AR)_vs_AR_(lambda)");
	private static final MyInterpolatingFunction deltaCL_Beta_over_eps_tan_VS_AR_and_taper_ratio= aeroDatabase.interpolate2DFromDatasetFunction("(C_l_beta_w_b)_dC_l_beta_over_eps_w_times_tan_(L_c4)_vs_AR_(lambda)");
	private static final MyInterpolatingFunction kRE_RE = aeroDatabase.interpolate1DFromDatasetFunction("(C_n_beta_b)_K_Re_b_vs_Re_l_b_times_1e-6");
	private static final MyInterpolatingFunction kN_VS_eta_taper_ratio_and_ar = aeroDatabase.interpolate3DFromDatasetFunction("(C_n_delta_a)_k_n_a_vs_eta_(AR)_(lambda)");
	private static final MyInterpolatingFunction RDP_vs_lambdaBeta_taperRatio_betaAROverKappa = aeroDatabase.interpolate3DFromDatasetFunction("(C_l_p_w)_RDP_vs_Lambda_beta_(beta_times_AR_over_k)_(lambda)");
	private static final MyInterpolatingFunction deltaClROverTwist_vs_AR_TaperRatio= aeroDatabase.interpolate2DFromDatasetFunction("(C_l_r_w)_dC_l_r_over_eps_w_vs_AR_(lambda)");
	private static final MyInterpolatingFunction Cnr_over_Cdo_vs_AR_LambdaQuarter = aeroDatabase.interpolate3DFromDatasetFunction("(C_n_r_w)_C_n_r_over_C_D0_bar_vs_AR_(L_c4)_(x_bar_ac_minus_x_bar_cg)");
	private static final MyInterpolatingFunction kN_Lb_quadro_Over_Side_Surface_first_passage = aeroDatabase.interpolate2DFromDatasetFunction("(C_n_beta_b)_K_N_vs_x_cg_over_l_b_(squared_l_b_over_S_b_s)_(square_root_(h1_over_h2))_(h_b_over_w_b)","data_0","var_0_0","var_0_1");
	private static final MyInterpolatingFunction kN_Root_Square_z1_Over_z2_second_passage = aeroDatabase.interpolate2DFromDatasetFunction("(C_n_beta_b)_K_N_vs_x_cg_over_l_b_(squared_l_b_over_S_b_s)_(square_root_(h1_over_h2))_(h_b_over_w_b)","data_1","var_1_0","var_1_1");
	private static final MyInterpolatingFunction kN_zMax_Over_wMax_third_passage = aeroDatabase.interpolate2DFromDatasetFunction("(C_n_beta_b)_K_N_vs_x_cg_over_l_b_(squared_l_b_over_S_b_s)_(square_root_(h1_over_h2))_(h_b_over_w_b)","data_2","var_2_0","var_2_1");
	private static final MyInterpolatingFunction clrOverCl_vs_AR_TaperRatio_first_passage = aeroDatabase.interpolate2DFromDatasetFunction("(C_l_r_w)_C_l_r_over_C_Lift1_vs_AR_(lambda)_(L_c2)","data_0","var_0_0","var_0_1");
	private static final MyInterpolatingFunction clrOverCl_vs_LambdaQuarter_second_passage = aeroDatabase.interpolate2DFromDatasetFunction("(C_l_r_w)_C_l_r_over_C_Lift1_vs_AR_(lambda)_(L_c2)","data_1","var_1_0","var_1_1");
	private static final MyInterpolatingFunction cnrOverClQuadro_vs_AR_TaperRatio_first_passage = aeroDatabase.interpolate3DFromDatasetFunction("(C_n_r_w)_C_n_r_over_squared_(C_Lift1)_vs_AR_(lambda)_(L_c4)_(x_bar_ac_minus_x_bar_cg)","data_0","var_0_0","var_0_1","var_0_2");
	private static final MyInterpolatingFunction get_DeltacpOverTwistAngle = aeroDatabase.interpolate2DFromDatasetFunction("(C_n_p_w)_dC_n_p_over_eps_w_vs_AR_(lambda)");

	public static double get_C_m_q_w_kp_VS_AR(double ar) { 
		return cmqwKqVSAr.value(ar);
	}

	public static double getkyvCoefficient(double bv, double rOne){
		double a = (2.0)*rOne;
		return kyv_bvOver2r1.value(bv/a);
	}

	public static double get_C1_bvOver2R1(double bv,double rone, double taperRatio) {
		double a = (2.0)*rone;
		double b = bv/a;
		return c1_bvOver2r1.value(b,taperRatio);
	}

	public static double get_DeltaCnpOverTwist(double ar,double taperRatio) {
		return get_DeltacpOverTwistAngle.value(ar,taperRatio);
	}

	/**
	 * 
	 * @param zH height of horizontal tail from the top of fuselage cone
	 * @param bV span (effective) of vertical tail
	 * @param xACHV distance of horizontal tail aerodynamic center from h-tail apex
	 * @param cV mean aerodynamic chord of vertical tail
	 * @return c2 factor for estimating vertical tail effective aspect-ratio
	 */
	public static double get_AR_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v(double zH, double bV, double xACHV, double cV) {
		return ar_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v.value( zH/bV,xACHV/cV);
	}

	public static double getkhvCoefficient(double sh, double sv){
		return kHV_ShOverSv.value(sh/sv);
	}

	public static double get_k_Rudder_vs_ChordRudderOverMAC_VT(double etainner,double etaouter,double taperRatioVT ) {
		double a = K_Rudder_vs_ChordRudderOverMAC_VT.value(etainner,taperRatioVT);
		double b = K_Rudder_vs_ChordRudderOverMAC_VT.value(etaouter,taperRatioVT);
		System.out.println("DELTAKRinner = "+a+" DeltaKappaROuter = "+b);
		return b-a;
	}

	public static double tau_ControlSurface_VS_chordCSOverMACVT(double chordCS,double mac) {
		return tau_ControlSurface_VS_chordCSOverMACVT.value(chordCS/mac);
	}

	public static double get_cl_beta_over_gamma_vs_AR_and_TaperRatio(double ar, double lambdahalf, double taperRatio){
		return c_l_beta_w_b_over_gamma_vs_AR_TaperRatio.value(ar, lambdahalf, taperRatio);
	}

	public static double get_kMGamma_vs_Mach_and_AR(double ar, double mach, double lambdamean ) {
		double cos = Math.cos(lambdamean);
		double x = ar/cos;
		double y = mach*cos;
		return K_M_Gamma_vs_mach_cos_lambdamean_and_AR_over_coslambdamean.value(y, x);
	}

	public static double get_clbetaOverCl(double taperRatio, double ar, double lambdamean) {
		return clbeta_over_cl.value(lambdamean , ar, taperRatio);
	}

	public static double get_KM_due_to_sweep_angle(double ar, double lambdamean, double mach) {
		double cos = Math.cos(lambdamean);
		double x = ar/cos;
		double y = mach*cos;
		return K_M_machcoslambda_and_ARcoslambda.value(y, x);
	}

	public static double get_KF_due_to_Sweep_Angle ( double a, double span, double ar, double lambdamean ) {
		double cos = Math.cos(lambdamean);
		double b = a/span;
		double c = ar/cos;
		return K_F_vs_AOverb_ARoverCos.value(b, c);
	}

	public static double get_cL_Beta_over_cl_Sweep_angle(double ar, double taperRatio) {	
		return cl_Beta_over_Lift_vs_ar_and_taper_ratio.value(ar, taperRatio);		
	}

	public static double get_deltaCL_Beta_over_eps_tan_VS_AR_and_taper_ratio(double ar, double taperRatio) {
		return deltaCL_Beta_over_eps_tan_VS_AR_and_taper_ratio.value(ar, taperRatio);
	}

	public static double get_RME(double etain, double etaout, double lambdaBeta, double ar, double k, double beta, double taperRatio) {		
		double a = (beta*ar)/k;
		System.out.println("\n beta = "+beta+"\n ar = "+ar+"\n k = "+k);
		System.out.println("\n quoziente = "+a);
		double RME_Inner = aeroDatabase.interpolate4DFromDatasetFunction("(C_l_delta_a)_RME_vs_eta_(Lambda_beta)_(beta_times_AR_over_k)_(lambda)",
				Arrays.asList("data_0", "data_1", "data_2"), "var_0" , "var_1", "var_2", "var_3",etain,lambdaBeta,a,taperRatio);
		double RME_Outer = aeroDatabase.interpolate4DFromDatasetFunction("(C_l_delta_a)_RME_vs_eta_(Lambda_beta)_(beta_times_AR_over_k)_(lambda)",
				Arrays.asList("data_0", "data_1", "data_2"), "var_0" , "var_1", "var_2", "var_3",etaout,lambdaBeta,a,taperRatio);
		System.out.println("\n RME_Inner = "+RME_Inner+"\n RME_Outer = "+RME_Outer);		 
		return RME_Outer-RME_Inner;
	}

	public static double get_kRE_vs_RE(double reynolds) {		
		return kRE_RE.value(reynolds);
	}

	public static double get_kN_VS_eta_taper_ratio_ar(double etain, double etaout, double ar, double taperRatio) {
		double a = kN_VS_eta_taper_ratio_and_ar.value(etain, ar, taperRatio);
		double b = kN_VS_eta_taper_ratio_and_ar.value(etaout, ar, taperRatio);
		System.out.println("\n a = "+a+"\n b= "+b);
		double c = b-a;
		return c;
	}

	public static double get_RDP_vs_lambdaBeta_taperRatio_betaAROverKappa(double lambdabeta, double taperRatio,double beta, double kappa, double ar) {
		double a = (beta*ar)/kappa;
		System.out.println("\n beta*ar/kappa = "+a);
		return RDP_vs_lambdaBeta_taperRatio_betaAROverKappa.value(lambdabeta, a, taperRatio);

	}

	public static double get_deltaClROverTwist_vs_ar_taperRatio(double ar, double taperRaio) {
		return deltaClROverTwist_vs_AR_TaperRatio.value(ar, taperRaio);
	}

	public static double get_Cnr_over_Cdo_vs_AR_and_LambdaQuarter_arm(double ar, double lambdaquarter, double xac,double xcg) {	
		double a = xac - xcg;	
		return Cnr_over_Cdo_vs_AR_LambdaQuarter.value(ar, lambdaquarter, a);		
	}

	public static double get_kn_body( double lb,double sb, double lcg,double zone,double ztwo, double zmax,double wmax) {		
		double var0_0 = lb*lb/sb; 			
		double var0_1 = lcg/lb;

		double var1_1 = kN_Lb_quadro_Over_Side_Surface_first_passage.value(var0_1,var0_0);
		double var1_0 = Math.sqrt(zone/ztwo);
		double var2_1 = kN_Root_Square_z1_Over_z2_second_passage.value(var1_1,var1_0);
		double var2_0 = zmax*wmax; 
		System.out.println("l*l/sb = "+var0_0+" lcg/lb = "+var0_1+" z1/z2 = "+var1_0+" zmax/wmax = "+var2_0);
		System.out.println("\n First passage = "+var1_1+"\n Second passage = " + var2_1);
		return kN_zMax_Over_wMax_third_passage.value( var2_1,var2_0);
	}

	public static double get_Clr_over_Cl_Mach0_cl0_vs_AR_TaperRatio_LambdaQuarter(double ar, double taperRatio, double lambdaquarter) {
		double var1_1 = clrOverCl_vs_AR_TaperRatio_first_passage.value(ar,taperRatio); 
		System.out.println("\n First Passage = "+var1_1);
		double lambda = lambdaquarter;
		return clrOverCl_vs_LambdaQuarter_second_passage.value( var1_1,lambda);
	}

	public static double get_Cnr_over_Cl_Square_vs_lambda_cl0_vs_AR_TaperRatio_LambdaQuarter(double ar, double taperRatio, double lambdaquarter, double xac, double xcg) {
		double lambda = 57.3*lambdaquarter;
		double x = xac - xcg;
		System.out.println("x = "+x);
		double var1_1 = cnrOverClQuadro_vs_AR_TaperRatio_first_passage.value(ar, lambda, x);
		System.out.println("\n Lambdaquarter get_Cnr_over_Cl_Square_vs_lambda_cl0_vs_AR_TaperRatio_LambdaQuarter = "+lambda+
				"\n 1st passage = "+ var1_1+"\n ar = "+ar+"\n taperratio = "+taperRatio);
		double dsaa = aeroDatabase.interpolate2DFrom3DDatasetFunction("(C_n_r_w)_C_n_r_over_squared_(C_Lift1)_vs_AR_(lambda)_(L_c4)_(x_bar_ac_minus_x_bar_cg)",
				"data_0", "data_1", "var_0_0", "var_0_1", "var_0_2", "var_1_0",
				"var_1_1", x, lambda, ar, taperRatio);		 				
		return dsaa;
	}



	public static double get_RME1(double product, double taperRatio, double lambdabeta,double etaInner, double etaOuter) {
		double RME_Inner = aeroDatabase.interpolate4DFromDatasetFunction("(C_l_delta_a)_RME_vs_eta_(Lambda_beta)_(beta_times_AR_over_k)_(lambda)",
				Arrays.asList("data_0", "data_1", "data_2"), "var_0" , "var_1", "var_2", "var_3",etaInner,lambdabeta,product,taperRatio);
		double RME_Outer = aeroDatabase.interpolate4DFromDatasetFunction("(C_l_delta_a)_RME_vs_eta_(Lambda_beta)_(beta_times_AR_over_k)_(lambda)",
				Arrays.asList("data_0", "data_1", "data_2"), "var_0" , "var_1", "var_2", "var_3",etaOuter,lambdabeta,product,taperRatio);
		System.out.println("\n RME_Inner = "+RME_Inner+"\n RME_Outer = "+RME_Outer); 
		return RME_Outer-RME_Inner;

	}

	public static double get_Cnr_over_Cl_Square_vs_lambda_cl0_vs_AR_TaperRatio_LambdaQuarter1(double ar, double taperRatio, double lambda, double xac, double xcg) {

		double x = xac - xcg;

		double var1_1 = cnrOverClQuadro_vs_AR_TaperRatio_first_passage.value(ar, lambda, x);

		System.out.println("\n Lambdaquarter get_Cnr_over_Cl_Square_vs_lambda_cl0_vs_AR_TaperRatio_LambdaQuarter = "+lambda+"\n 1st passage = "+ var1_1+
				"\n ar = "+ar+"\n taperratio = "+taperRatio);		
		double value = aeroDatabase.interpolate2DFrom3DDatasetFunction("(C_n_r_w)_C_n_r_over_squared_(C_Lift1)_vs_AR_(lambda)_(L_c4)_(x_bar_ac_minus_x_bar_cg)",
				"data_0", "data_1", "var_0_0", "var_0_1", "var_0_2", "var_1_0",
				"var_1_1", x, lambda, ar, taperRatio);
		return value;
	}	
}