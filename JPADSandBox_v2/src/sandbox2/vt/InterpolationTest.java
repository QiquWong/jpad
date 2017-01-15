package sandbox2.vt;

import standaloneutils.MyInterpolatingFunction.BilinearInterpolatingFunction;
import standaloneutils.MyInterpolatingFunction.TrilinearInterpolatingFunction;

public class InterpolationTest {

	public static void main(String[] args) {

		//--------------------------------------------------------------------------------------
		// BILINEAR
		double[] x = new double[] {1,2,3,4,5}; // var_1
		double[] y = new double[] {0.1,0.2,0.3,0.4}; // var_0
		double[][] dataBilinear = new double[][] {
			{2,3,4,5},
			{6,7,8,9},
			{10,11,12,13},
			{14,15,16,17},
			{18,19,20,21}
		};

		BilinearInterpolatingFunction function1 = new BilinearInterpolatingFunction(x, y, dataBilinear);
		double result1 = function1.value(2.5,0.15);
		
		System.out.println("RESULT = " + result1);
		
		//--------------------------------------------------------------------------------------
		// TRILINEAR
		double[] a = new double[] {1,2,3}; // var_0
		double[] b = new double[] {8,9}; // var_2
		double[] c = new double[] {0.1,0.2,0.3,0.4}; // var_1
		double[][][] dataTrilinear = new double[][][] {
			// Each line is a page. In each page there is a matrix of two arrays in brackets
			{{1,2,3,4},{13,14,15,16}},
			{{5,6,7,8},{17,18,19,20}},
			{{9,10,11,12},{21,22,23,24}}
		};

		TrilinearInterpolatingFunction function2 = new TrilinearInterpolatingFunction(a, b, c, dataTrilinear);
		double result2 = function2.value(2.5,8.5,0.25);
		
		System.out.println("RESULT = " + result2);

		
	}

}
