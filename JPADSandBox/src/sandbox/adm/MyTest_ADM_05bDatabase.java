package sandbox.adm;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import standaloneutils.MyMathUtils;
import standaloneutils.database.hdf.MyHDFReader;

public class MyTest_ADM_05bDatabase {

	public MyTest_ADM_05bDatabase() {

		String fileName ="test/Aerodynamic_Database_Ultimate.h5";
//		String fileName ="test/h5ex_g_visit.h5";
//		String fileName ="test/pippo.h5";
		
		try {
			MyHDFReader hdfReader = new MyHDFReader(fileName);
			
			// print all group starting from root
//			hdfReader.printGroup(hdfReader.getRootGroup(), "\t");
			
			System.out.println("Search for a group ...");
			String groupFullName = 
					// "(AR_v_eff)_k_h_v_vs_S_h_over_S_v";
					// "group1/group3/group4/pippo1"
					"g2/g3/pippo1"; 
			
			System.out.println(
					"------ Searching: " + groupFullName
					);
			
			int g_id = hdfReader.findGroupByName(groupFullName);
			if (g_id > 0)
			{
				System.out.println(
						"------ found (group id: " + g_id + ")"
						);
			}
			else 
			{
				System.out.println(
						"------ not found"
						);
			}
			
			System.out.println("Search for a dataset 1D ...");
			String dataset1DFullName = 
					"(AR_v_eff)_k_h_v_vs_S_h_over_S_v/data"; 
			
			System.out.println(
					"------ Searching: " + dataset1DFullName
					);
			
			double [] dset1D = hdfReader.getDataset1DFloatByName(dataset1DFullName);
			if (dset1D != null)
			{
				System.out.println(
						"------ found"
						);
				
				System.out.println(
						Arrays.toString(dset1D)
						);
				
				System.out.println("INTERPOLATE in 1D ...");
				
				String var01DFullName = "(AR_v_eff)_k_h_v_vs_S_h_over_S_v/var_0";
				double [] var01D = hdfReader.getDataset1DFloatByName(var01DFullName);
				
				if (var01D != null) {
					double v0 = 2.6;
					Double f1D_v0 = MyMathUtils.getInterpolatedValue1DLinear(var01D, dset1D, v0);
					System.out.println(
							"x = " + v0 + " --> f(x) = " + f1D_v0
							);
				} else {
					System.out.println(
							"------ var0 not found"
							);
				}
			}
			else 
			{
				System.out.println(
						"------ not found"
						);
			}
			
			System.out.println("Search for a dataset 2D ...");
			String dataset2DFullName = "(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(AR)_vs_AR_(lambda)/data";
			double [][] dset2D = hdfReader.getDataset2DFloatByName(dataset2DFullName);
			if (dset2D != null)
			{
				System.out.println(
						"------ found\n"
						+ "Rows: " + dset2D.length + "\n"
						+ "Columns: " + dset2D[0].length
						);
				
				System.out.println(
						"Full array:\n"
						+ Arrays.deepToString(dset2D)
						+ "\n---------"
						);
				
				RealMatrix matrix2D = new Array2DRowRealMatrix(
						dset2D
						);
				
				System.out.println(
						"Single columns:"
						);
				for (int col=0; col<dset2D[0].length; col++) {
					System.out.println(
							"Column " + col + "\n"
									+ Arrays.toString(matrix2D.getColumn( col ))
							);
				}
				
			}
			else 
			{
				System.out.println(
						"------ not found"
						);
			}
			
			System.out.println("Search for a dataset 3D ...");
			String dataset3DFullName = "(C_l_beta_w_b)_C_l_beta_over_C_Lift1_(L_c2)_vs_L_c2_(AR)_(lambda)/data";
			double [][][] dset3D = hdfReader.getDataset3DFloatByName(dataset3DFullName);
			if (dset3D != null)
			{
				
				System.out.println(
						"------ found\n"
						+ "Rows: " + dset3D.length + "\n"
						+ "Columns: " + dset3D[0].length
						+ "Pages: " + dset3D[0][0].length
						);

				System.out.println(
						"Full array:\n"
						+ Arrays.deepToString(dset3D)
						+ "\n---------"
						);
				
				List<RealMatrix> matrix3D = MyMathUtils.extractAll2DMatricesFrom3DArray(dset3D);
				
				int nPages = matrix3D.size();
				System.out.println(
						"Pages: " + nPages
						);
				for ( RealMatrix m2D : matrix3D ) {
					System.out.println(
							"---------------------\n"
							+ m2D.toString()
							);
				}
			}
			else 
			{
				System.out.println(
						"------ not found"
						);
			}
			
			
			
			hdfReader.close();

			// TO DO: play around with finding groups and using contents ...

			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

}
