package calculators.stability;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class DynamicStabilityCalculator {
	/**
	 *   calculates EigenValues from a square matrix 4x4 (such as [A_Lon] and [A_LD])
	 *   and puts them in a matrix 4x2 (a single line contains a specific eigenvalue
	 *   in the form [ lambda(i)_Re , lambda(i)_Img ]) 
	 * @param aMatrix
	 * @return lambda_Matrix
	 */
	public static double[][] buildEigenValuesMatrix (double aMatrix[][]) {
		
		RealMatrix aLonRM = MatrixUtils.createRealMatrix(aMatrix);
		EigenDecomposition aLonDecomposition = new EigenDecomposition(aLonRM);
		double[] reEigen = aLonDecomposition.getRealEigenvalues();
		double[] imgEigen = aLonDecomposition.getImagEigenvalues();
		
		double [][] lambda_Matrix = new double [4][2]; 
		
		for (int i=0 ; i < 4 ; i++) {
			lambda_Matrix[i][0] = reEigen [i];
			lambda_Matrix[i][1] = imgEigen [i];
		}
		
		return lambda_Matrix;
	}
	
	/**
	 *   calculates i� EigenVector from a square matrix 4x4 (such as [A_Lon] and [A_LD])
	 * @param aMatrix
	 * @param index
	 * @return
	 */
	public static RealVector buildEigenVector (double aMatrix[][], int index) {
		RealMatrix aRM = new Array2DRowRealMatrix(aMatrix);
		EigenDecomposition eigDec = new EigenDecomposition(aRM);
		
		RealVector eigVec = eigDec.getEigenvector(index);

		return eigVec;
	}

	/**
	 *   calculates Damping Coefficient
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Damping Coefficient
	 */
	public static double calcZeta (double sigma, double omega) {
		
		return Math.sqrt( 1 / ( 1 + Math.pow( omega/sigma , 2 )));
	}
	
	/**
	 *   calculates Natural Frequency
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Natural Frequency [s^(-1)]
	 */
	public static double calcOmega_n (double sigma, double omega) {
		
		return -(sigma)/calcZeta(sigma,omega);
	}

	/**
	 *   calculates Period
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Period [s]
	 */
	public static double calcT (double sigma, double omega) {
		
		return 2*Math.PI / ( calcOmega_n (sigma,omega) * 
				Math.sqrt( 1 - Math.pow(calcZeta (sigma,omega) , 2)));
	}
	
	/**
	 *   calculates Halving Time
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Halving Time [s]
	 */
	public static double calct_half (double sigma, double omega) {
		
		return Math.log(2) / (calcOmega_n (sigma,omega) * calcZeta (sigma,omega));
	}

	/**
	 *   calculates number of cycles to Halving Time
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return number of cycles to Halving Time
	 */
	public static double calcN_half (double sigma, double omega) {
		
		return calct_half (sigma,omega) / calcT (sigma,omega);
	}

}
