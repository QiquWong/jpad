package sandbox.adm;

import standaloneutils.database.hdf.MyHDFReader;


public class MyTest_ADM_05c_HDF_Interpolation {
	
	public MyTest_ADM_05c_HDF_Interpolation() {
		
		String fileName ="test/Aerodynamic_Database_Ultimate.h5";

		try {
			MyHDFReader hdfReader = new MyHDFReader(fileName);
			
			System.out.println("Interpolate over a dataset 1D ...");
			
			String group1DFullName = 
					"(AR_v_eff)_k_h_v_vs_S_h_over_S_v"; 
			
			double v0 = 0.5;
			Double f1D_v0 = hdfReader.interpolate1DFromDataset(group1DFullName, v0);
			
			if (f1D_v0 != null) {
				System.out.println(
						"x = " + v0 + " --> f(x) = " + f1D_v0
						);

			} else {
				System.out.println(
						"------ interpolation went wrong"
						);
			
			}

			System.out.println("Interpolate over a dataset 2D ...");
			
			String group2DFullName = 
					"(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(AR)_vs_AR_(lambda)";
			
			double v0_2d = 1.7; // ---> column-wise 
			double v1 = 7.0;    // ---> row-wise
			Double f2D_v0_v1 = hdfReader.interpolate2DFromDataset(group2DFullName, v0_2d, v1);
			
			if (f2D_v0_v1 != null) {
				System.out.println(
						"(x,y) = (" + v0_2d + "," + v1 + ") --> f(x,y) = " + f2D_v0_v1
						);
			} else {
				System.out.println(
						"------ interpolation went wrong"
						);
			}
			
			System.out.println("Interpolate over a dataset 3D ...");
			
			String group3DFullName = 
					"(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(L_c2)_vs_L_c2_(AR)_(lambda)";
			
			double v0_3d = 0.25; // ---> page-wise 
			double v1_3d = 8.0; // ---> column-wise 
			double v2 = -20.0;    // ---> row-wise
			Double f3D_v0_v1_v2 = hdfReader.interpolate3DFromDataset(group3DFullName, v0_3d, v1_3d, v2);
			
			if (f3D_v0_v1_v2 != null) {
				System.out.println(
						"(x,y,z) = (" + v0_3d + "," + v1_3d + "," + v2 + ") --> f(x,y,z) = " + f3D_v0_v1_v2
						);
			} else {
				System.out.println(
						"------ interpolation went wrong"
						);
			}
			
//			MyInterpolationCheckDialog _dialogInterpolationCheck = new MyInterpolationCheckDialog(
//					Display.getCurrent(), hdfReader, group1DFullName, 1);
			
			// release resources
			hdfReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		
	}

}
