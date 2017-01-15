package database.databasefunctions.aerodynamics;


import configuration.enumerations.AirfoilFamilyEnum;
import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;

public class AerodynamicDatabaseReader extends DatabaseReader {

	private MyInterpolatingFunction c_m0_b_k2_minus_k1_vs_FFR, 
	ar_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v,
	 x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda,
	 x_bar_ac_w_k1_vs_lambda, x_bar_ac_w_k2_vs_L_LE_AR_lambda,
	 d_Alpha_Vs_LambdaLE_VsDy, d_Alpha_d_Delta_2d_VS_cf_c, d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio,
	 d_epsilon_d_alpha_VS_position_aspectRatio, deltaYvsThicknessRatio, kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR;
	
	double cM0_b_k2_minus_k1, ar_v_eff_c2, x_bar_ac_w_k1, x_bar_ac_w_k2, x_bar_ac_w_xac_cr, d_Alpha_Vs_LambdaLE, deltaYvsThickness;
 
	public AerodynamicDatabaseReader(String databaseFolderPath, String databaseFileName) {

		super(databaseFolderPath, databaseFileName);

		c_m0_b_k2_minus_k1_vs_FFR = database.interpolate1DFromDatasetFunction("(C_m0_b)_k2_minus_k1_vs_FFR");
		ar_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v = database.interpolate2DFromDatasetFunction("(AR_v_eff)_c2_vs_Z_h_over_b_v_(x_ac_h--v_over_c_bar_v)");
		
		x_bar_ac_w_k1_vs_lambda=database.interpolate1DFromDatasetFunction("(x_bar_ac_w)_k1_vs_lambda");
		x_bar_ac_w_k2_vs_L_LE_AR_lambda=database.interpolate3DFromDatasetFunction ("(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)");
		x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda= database.interpolate3DFromDatasetFunction("(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)");
		  
		
		d_Alpha_Vs_LambdaLE_VsDy = database.interpolate2DFromDatasetFunction("DAlphaVsLambdaLEVsDy");
		
		d_Alpha_d_Delta_2d_VS_cf_c = database.interpolate1DFromDatasetFunction("c_c1_deltaalpha2d");
		
		d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio = database.interpolate2DFromDatasetFunction("DAlphaDdeltavsAspectratio");
		
		d_epsilon_d_alpha_VS_position_aspectRatio = database.interpolate2DFromDatasetFunction("upwashvsposition");
		deltaYvsThicknessRatio  =database.interpolate2DFromDatasetFunction("DeltaYvsThicknessRatio");
		
		kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR = database.interpolate3DFromDatasetFunction("K_Omega_vs_CLalphaOmegaFracClmax_(taperRatio)_(AR)");
		
		//TODO Insert other aerodynamic functions (see "Aerodynamic_Database_Ultimate.h5")
	}
	
	


	public void runAnalysis(double diameter, double length, double zH, double bV, double xACHV, double cV){
		cM0_b_k2_minus_k1 = get_C_m0_b_k2_minus_k1_vs_FFR(length, diameter);
		ar_v_eff_c2 = get_AR_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v(zH, bV, xACHV, cV);
	}
	
	public void runAnalysisXac(double taperRatio, double sweepAngleLE, double aspectRatio, double mach ){
		x_bar_ac_w_k1 = getX_bar_ac_w_k1_vs_lambda(taperRatio);
	    x_bar_ac_w_k2 =	getX_bar_ac_w_k2_vs_L_LE_AR_lambda(taperRatio,sweepAngleLE, aspectRatio);	
	    x_bar_ac_w_xac_cr = getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(taperRatio , sweepAngleLE, mach , aspectRatio);
	   
	}
	
	public void runAnalysisDeltaAlphaCLMax(double sweepAngleLE , double sharpnessParameterLE){
		d_Alpha_Vs_LambdaLE =  getDAlphaVsLambdaLEVsDy(sweepAngleLE, sharpnessParameterLE);
	}
	
	
	/**
	 * 
	 * @param length
	 * @param diameter
	 * @return
	 */
	public double get_C_m0_b_k2_minus_k1_vs_FFR(double length, double diameter) { 
		return c_m0_b_k2_minus_k1_vs_FFR.value(length/diameter);
	}

	/**
	 * 
	 * @param zH height of horizontal tail from the top of fuselage cone
	 * @param bV span (effective) of vertical tail
	 * @param xACHV distance of horizontal tail aerodynamic center from h-tail apex
	 * @param cV mean aerodynamic chord of vertical tail
	 * @return c2 factor for estimating vertical tail effective aspect-ratio
	 */
	public double get_AR_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v(double zH, double bV, double xACHV, double cV) {
		return ar_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v.valueBilinear(zH/bV, xACHV/cV);
	}
	
	
	/**
	 * 
	 * @param taperRatio taper ratio of the wing
	 * @return k1 factor for estimating xac
	 */
	
	public double getX_bar_ac_w_k1_vs_lambda(double taperRatio) {
		return x_bar_ac_w_k1_vs_lambda.value(taperRatio);
	}


	
	/**
	 * 
	 * @param taperRatio taper ratio of the wing 
	 * @param sweepAngleLE sweep angle of the equivalent wing at leading edge
	 * @param aspectRatio aspect ratio of the wing
	 * @return k2 factor for estimating xac
	 */
	
	public double getX_bar_ac_w_k2_vs_L_LE_AR_lambda(double taperRatio, double sweepAngleLE, double aspectRatio) {
		return x_bar_ac_w_k2_vs_L_LE_AR_lambda.valueTrilinear(taperRatio, sweepAngleLE, aspectRatio);
	}
	

	/**
	 * 
	 * @param taperRatio taper ratio of the wing
	 * @param sweepAngleLE sweep angle at leading edge of the equivalent wing. in radians
	 * @param mach Mach number
	 * @param aspectRatio aspect ratio of the wing
	 * @return x'ac/cr for estimating xac for subsonic Mach numbers 
	 */
	
	public double getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(double taperRatio, double sweepAngleLE, double mach, double aspectRatio) {
		double tgSweepAngleLe = Math.tan(sweepAngleLE/57.3);
		double v3;
		if(mach>1)
		v3 = (Math.sqrt(1- Math.pow(mach,2)))/tgSweepAngleLe;
		else
		v3 = tgSweepAngleLe/(Math.sqrt(1- Math.pow(mach,2)));
	    return x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda.valueTrilinear( taperRatio, v3, aspectRatio*tgSweepAngleLe);
	}
	
	
	
	
	public double getDAlphaVsLambdaLEVsDy(double sweepAngleLE , double sharpnessParameterLE ) {
		return d_Alpha_Vs_LambdaLE_VsDy.valueBilinear(sweepAngleLE, sharpnessParameterLE);
	}
	
	public double getD_Alpha_d_Delta_2d_VS_cf_c(double cf_c  ) {
		return d_Alpha_d_Delta_2d_VS_cf_c.value(cf_c);
	}
	
	public double getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(double aspectRatio, double d_Alpha_d_delta2d ) {
		return d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio.valueBilinear(aspectRatio, d_Alpha_d_delta2d);
	}
	
	public double getD_epsilon_d_alpha_VS_position_aspectRatio(double position, double aspectRatio) {
		return d_epsilon_d_alpha_VS_position_aspectRatio.valueBilinear(position, aspectRatio);
	}
	
	public double getDeltaYvsThickness (double tc, AirfoilFamilyEnum airfoilFamily) {
		
		//recognizing airfoil family
		int airfoilFamilyIndex = 0;
		if(airfoilFamily == AirfoilFamilyEnum.NACA_4_Digit) 
			airfoilFamilyIndex = 1;
		else if(airfoilFamily == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamilyIndex = 2;
		else if(airfoilFamily == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamilyIndex = 3;
		else if(airfoilFamily == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamilyIndex = 4;
		else if(airfoilFamily == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamilyIndex = 5;
		else if(airfoilFamily == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamilyIndex = 6;
		else if(airfoilFamily == AirfoilFamilyEnum.BICONVEX)
			airfoilFamilyIndex = 7;
		else if(airfoilFamily == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamilyIndex = 8;
		
		return deltaYvsThicknessRatio.valueBilinear(tc, airfoilFamilyIndex);
	}

	public double getKOmegePhillipsAndAlley (
			double cLalpha,
			double twistEquivalent,
			double clmaxMeanAirfoil,
			double taperRatioEquivalent,
			double aspectRatio
			) {
		
		double cLAlphaOmegaFracClmax = (cLalpha*twistEquivalent)/clmaxMeanAirfoil;
		
		return kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR.valueTrilinear(
				aspectRatio,
				cLAlphaOmegaFracClmax,
				taperRatioEquivalent
				);
	}

}
