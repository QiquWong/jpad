package sandbox2.vt;

import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;

public class DatabaseTest {

	public static void main(String[] args) {

		MyConfiguration.initWorkingDirectoryTree();
		
		//----------------------------------------------------------------------------------------
		// AERODYNAMIC DATABASE
		AerodynamicDatabaseReader aeroReader = new AerodynamicDatabaseReader(
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
				"Aerodynamic_Database_Ultimate.h5");

		System.out.println("----------------------------------------------------------------------");
		double cm0b2 = aeroReader.get_C_m0_b_k2_minus_k1_vs_FFR(9.87,1); 
		System.out.println("cm02b: " + cm0b2 + " m"); // expected = 0.9282
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double arEffc2 = aeroReader.get_AR_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v(-1, 1, 0.5, 1);
		System.out.println("arEffc2: " + arEffc2 + " m"); // expected = 1.7
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double xBarAcWK1vsLambda = aeroReader.getX_bar_ac_w_k1_vs_lambda(0.7); 
		System.out.println("xBarAcWK1vsLambda: " + xBarAcWK1vsLambda + " m"); // expected = 1.1556
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double x_bar_ac_w_k2_vs_L_LE_AR_lambda = aeroReader.getX_bar_ac_w_k2_vs_L_LE_AR_lambda(0.4, 40.0, 4); 
		System.out.println("x_bar_ac_w_k2_vs_L_LE_AR_lambda: " + x_bar_ac_w_k2_vs_L_LE_AR_lambda + " m"); // expected = 0.5248
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda = 
				aeroReader.getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(0.2, 25, 0.3, 8); 
		System.out.println("x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda: " 
				+ x_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda + " m"); // expected = 0.6027
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double d_Alpha_Vs_LambdaLE_VsDy = aeroReader.getDAlphaVsLambdaLEVsDy(11.03448275862069, 2); 
		System.out.println("d_Alpha_Vs_LambdaLE_VsDy: " + d_Alpha_Vs_LambdaLE_VsDy + " m"); // expected = 1.1300829729651274
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double d_Alpha_d_Delta_2d_VS_cf_c = aeroReader.getD_Alpha_d_Delta_2d_VS_cf_c(0.1694915254237288); 
		System.out.println("d_Alpha_d_Delta_2d_VS_cf_c: " + d_Alpha_d_Delta_2d_VS_cf_c + " m"); // expected = 0.500949476945679
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio = aeroReader.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(10, 0.6); 
		System.out.println("d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio: " 
				+ d_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio + " m"); // expected = 1.0372299886481917
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double d_epsilon_d_alpha_VS_position_aspectRatio = aeroReader.getD_epsilon_d_alpha_VS_position_aspectRatio(-1.4, 9); 
		System.out.println("d_epsilon_d_alpha_VS_position_aspectRatio: " 
				+ d_epsilon_d_alpha_VS_position_aspectRatio + " m"); // expected = 1.0524526743120663
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double deltaYvsThicknessRatio = aeroReader.getDeltaYvsThickness(0.04081632653061224, AirfoilFamilyEnum.NACA_64_Series); 
		System.out.println("deltaYvsThicknessRatio: " 
				+ deltaYvsThicknessRatio + " m"); // expected = 0.856252254910293
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR = aeroReader.getKOmegePhillipsAndAlley(0.275, 2, 1.8, 0.6, 8); 
		System.out.println("kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR: " 
				+ kOmega_vs_CLalphaOmegaClmax_vs_taperRatio_vs_AR + " m"); // expected = 0.13311090108121684
		System.out.println("----------------------------------------------------------------------\n");


		//----------------------------------------------------------------------------------------
		// VeDSC DATABASE
		VeDSCDatabaseReader vedscReader = new VeDSCDatabaseReader(
				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
				"VeDSC_database.h5");
		
		System.out.println("\n\n\n\n----------------------------------------------------------------------");
		double kFv_vs_bv_over_dfv = vedscReader.get_KFv_vs_bv_over_dfv(1.06, 1, 0.5); 
		System.out.println("kWv_vs_zw_over_rf: " + kFv_vs_bv_over_dfv + " m"); // expected = 1.055245134
		System.out.println("----------------------------------------------------------------------\n");

		System.out.println("----------------------------------------------------------------------");
		double kVf_vs_zw_over_dfv = vedscReader.get_KVf_vs_zw_over_dfv(1.06, 1, 0.5); 
		System.out.println("kVf_vs_zw_over_dfv: " + kVf_vs_zw_over_dfv + " m"); // expected = 0.8993491483797981
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kWv_vs_zw_over_rf = vedscReader.get_KWv_vs_zw_over_rf(0.0, 10, 1.0); 
		System.out.println("kWv_vs_zw_over_rf: " + kWv_vs_zw_over_rf + " m"); // expected = 0.992752141
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kWf_vs_zw_over_rf = vedscReader.get_KWf_vs_zw_over_rf(0.0, 10, 1.0); 
		System.out.println("kWf_vs_zw_over_rf: " + kWf_vs_zw_over_rf + " m"); // expected = 0.974073557
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHv_vs_zh_over_bv1_high_wing = vedscReader.get_KHv_vs_zh_over_bv1_high_wing(0.4, 2.0, 0.5); 
		System.out.println("kHv_vs_zh_over_bv1_high_wing: " + kHv_vs_zh_over_bv1_high_wing + " m"); // expected = 0.9908725496178525
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHv_vs_zh_over_bv1_mid_wing = vedscReader.get_KHv_vs_zh_over_bv1_mid_wing(0.4, 2.0, 0.5); 
		System.out.println("kHv_vs_zh_over_bv1_mid_wing: " + kHv_vs_zh_over_bv1_mid_wing + " m"); // expected = 0.9866052201076049
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHv_vs_zh_over_bv1_low_wing = vedscReader.get_KHv_vs_zh_over_bv1_low_wing(0.4, 2.0, 0.5); 
		System.out.println("kHv_vs_zh_over_bv1_low_wing: " + kHv_vs_zh_over_bv1_low_wing + " m"); // expected = 0.9724395821005023
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHf_vs_zh_over_bv1_high_wing = vedscReader.get_KHf_vs_zh_over_bv1_high_wing(0.4, 2.0, 0.5); 
		System.out.println("kHf_vs_zh_over_bv1_high_wing: " + kHf_vs_zh_over_bv1_high_wing + " m"); // expected = 0.9929830739108546
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHf_vs_zh_over_bv1_mid_wing = vedscReader.get_KHf_vs_zh_over_bv1_mid_wing(0.4, 2.0, 0.5); 
		System.out.println("kHf_vs_zh_over_bv1_mid_wing: " + kHf_vs_zh_over_bv1_mid_wing + " m"); // expected = 0.9842275725844947
		System.out.println("----------------------------------------------------------------------\n");
		
		System.out.println("----------------------------------------------------------------------");
		double kHf_vs_zh_over_bv1_low_wing = vedscReader.get_KHf_vs_zh_over_bv1_low_wing(0.4, 2.0, 0.5); 
		System.out.println("kHf_vs_zh_over_bv1_low_wing: " + kHf_vs_zh_over_bv1_low_wing + " m"); // expected = 0.9996310358631683
		System.out.println("----------------------------------------------------------------------\n");
		
		
	}

}
