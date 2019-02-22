package calculators.stability;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class DynamicStabilityCalculator {
	
	// TODO use IDynamicStabilityCalculator and builder pattern to encaplsulate
	//      low lever operations, making cleaner code in ACDynamicStabilityManager.calculate()
	
	/**
	 *   calculates EigenValues from a square matrix 4x4 (such as [A_Lon] and [A_LD])
	 *   and puts them in a matrix 4x2 (a single line contains a specific eigenvalue
	 *   in the form [ lambda(i)_Re , lambda(i)_Img ]) 
	 * @param array2D
	 * @return lambda_Matrix
	 */
	public static double[][] buildEigenValuesMatrix (double array2D[][]) {
		
		RealMatrix realMatrix = MatrixUtils.createRealMatrix(array2D);
		EigenDecomposition eigenDecomposition = new EigenDecomposition(realMatrix);
		double[] eigenRe = eigenDecomposition.getRealEigenvalues();
		double[] eigenIm = eigenDecomposition.getImagEigenvalues();
		
		double [][] eigenvaluesMatrix = new double [4][2]; 
		
		for (int i=0 ; i < 4 ; i++) {
			eigenvaluesMatrix[i][0] = eigenRe [i];
			eigenvaluesMatrix[i][1] = eigenIm [i];
		}
		
		return eigenvaluesMatrix;
	}
	
	/**
	 *   calculates EigenVectors from a square matrix 4x4 (such as [A_Lon] and [A_LD])
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
	public static double calcDampingCoefficient (double sigma, double omega) {
		
		return Math.sqrt( 1 / ( 1 + Math.pow( omega/sigma , 2 )));
	}
	
	/**
	 *   calculates Natural Frequency
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Natural Frequency [s^(-1)]
	 */
	public static double calcNaturalPulsation (double sigma, double omega) {
		
		return -(sigma)/calcDampingCoefficient(sigma,omega);
	}

	/**
	 *   calculates Period
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Period [s]
	 */
	public static double calcPeriod (double sigma, double omega) {
		
		return 2*Math.PI / ( calcNaturalPulsation (sigma,omega) * 
				Math.sqrt( 1 - Math.pow(calcDampingCoefficient (sigma,omega) , 2)));
	}
	
	/**
	 *   calculates Halving Time
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return Halving Time [s]
	 */
	public static double calcTimeToHalf (double sigma, double omega) {
		
		return Math.log(2) / (calcNaturalPulsation (sigma,omega) * calcDampingCoefficient (sigma,omega));
	}

	/**
	 *   calculates number of cycles to Halving Time
	 * @param sigma - lambda(i)_Re
     * @param omega - lambda(i)_Img
	 * @return number of cycles to Halving Time
	 */
	public static double calcNCyclesToHalf (double sigma, double omega) {
		
		return calcTimeToHalf (sigma,omega) / calcPeriod (sigma,omega);
	}

}
