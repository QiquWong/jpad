package standaloneutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.customdata.MyArray;

/** 
 * Utility math functions.
 * This class cannot be instantiated
 * 
 * @author Lorenzo Attanasio
 */
public final class MyMathUtils {
	
	private MyMathUtils () {}
	
	public static double interpolateLinear(double x1, double y1, double x2, double y2, double x) {
		return y1 + (y2-y1)/(x2-x1) * (x-x1);
	}
	
	public static List<Double> intersectList(double[] d1, double[] d2) {

		if (d1.length != d2.length) {
			System.out.println("Input arrays must have the same length");
			return null;
		}

		double[] diff = new MyArray(d1).minus(d2);
		List<Integer> signChangeList = new ArrayList<Integer>();
		List<Double> intersectionValuesList = new ArrayList<Double>();

		for (int i=1; i < diff.length; i++){
			if (Math.signum(diff[i]) != Math.signum(diff[i-1])) {
				signChangeList.add(i);
				intersectionValuesList.add((d1[i] + d2[i])/4. + (d1[i-1] + d2[i-1])/4.);
			}
		}
		
		return intersectionValuesList;

	}
	
	public static List<Tuple2<Double, Double>> getIntersectionXAndY(double[]x,  double[] y1, double[] y2) {

		if (y1.length != y2.length || x.length != y1.length || x.length != y2.length) {
			System.out.println("Input arrays must have the same length");
			return null;
		}

		List<Tuple2<Double, Double>> results = new ArrayList<>();
		
		MyInterpolatingFunction y1Function = new MyInterpolatingFunction();
		y1Function.interpolateLinear(x, y1);
		
		MyInterpolatingFunction y2Function = new MyInterpolatingFunction();
		y2Function.interpolateLinear(x, y2);
		
		double[] xFitted = MyArrayUtils.linspace(x[0], x[x.length-1], 2000);
		List<Double> y1Fitted = new ArrayList<>();
		List<Double> y2Fitted = new ArrayList<>();
		
		Arrays.stream(xFitted).forEach(xElemet -> {
			y1Fitted.add(y1Function.value(xElemet));
			y2Fitted.add(y2Function.value(xElemet));
		});  
		
		List<Double> diff = new ArrayList<>();
		for(int i=0; i<xFitted.length; i++) 
			diff.add(y1Fitted.get(i)-y2Fitted.get(i));

		for (int i=1; i < diff.size(); i++){
			if (Math.signum(diff.get(i)) != Math.signum(diff.get(i-1))) {
				results.add(
						Tuple.of(
								xFitted[i],
								(y1Fitted.get(i) + y2Fitted.get(i))/4. + (y1Fitted.get(i-1) + y2Fitted.get(i-1))/4.
								)
						);
			}
		}
		
		return results;

	}
	
	/**
	 * Compute RMS
	 * 
	 * @author Lorenzo Attanasio
	 * @param doubles
	 * @return
	 */
	public static Double rootMeanSquare(Double ... doubles) {
		return Math.sqrt((1/doubles.length)*MyArrayUtils.productArray(doubles));
	}

	/**Compute Arithmetic mean
	 * 
	 * @author Lorenzo Attanasio
	 * @param doubles
	 * @return
	 */
	public static Double arithmeticMean(Double ... doubles) {
		return MyArrayUtils.sumArrayElements(doubles)/doubles.length;
	}

	/** Compute Geometric mean
	 * 
	 * @author Lorenzo Attanasio
	 * @param doubles
	 * @return
	 */	
	public static Double geometricMean(Double ... doubles) {
		return Math.pow(MyArrayUtils.productArray(doubles),1/doubles.length);
	}

	/**
	 * Solve the Ax=b linear system using
	 * LUDecomposition. Other methods are
	 * QRDecomposition and SingularValueDecomposition.
	 * 
	 * @param a
	 * @param b
	 */
	public static double[] solveLinearSystem(RealMatrix a, double[] b) {

		return new LUDecomposition(a).getSolver().solve(MatrixUtils.createRealVector(b)).toArray();

		//								System.out.println("Gamma: " + _gammaSigned.getRealVector());
		//								RealVector residual = influenceMatrix.operate(_gammaSigned.getRealVector()).subtract(alpha.getRealVector());
		//								double rnorm = residual.getLInfNorm();
		//								System.out.println("Residual: " + rnorm);
	}
	
	public static RealVector solveLinearSystem(RealMatrix a, RealVector b) {
		return new LUDecomposition(a).getSolver().solve(b);
	}
	
	public static double[] solveLinearSystem(RealMatrix a, MyArray b) {
		return new LUDecomposition(a).getSolver().solve(b.getRealVector()).toArray();
	}
	
	/**
	 * Calculates triangle area from points P1, P2, P3 ordered counter-clockwise
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return area
	 */
	public static double areaTriangle(double[] p1, double[] p2, double[] p3) {
		
		double area = 0.0;		
		area = 0.5*(p1[0]*p2[1] + p2[0]*p3[1] + p3[0]*p1[1] - p2[0]*p1[1] - p3[0]*p2[1] - p1[0]*p3[1]);
		
		return area;	
	}
	
	/**
	 * Calculate polygon area from an array of points ordered counter-clockwise
	 * @param pts
	 * @return area
	 */
	public static double areaPolygon(double[] ... pts) {
		double area = 0.0;
		int numPts = pts.length;
		
		if(numPts < 3) {
			throw new IllegalArgumentException();
		}
		
		if(numPts == 3) {
			return areaTriangle(pts[0], pts[1], pts[2]);
		}
			
		List<double[]> ptsList = new ArrayList<>();
		ptsList.addAll(Arrays.asList(pts));
		ptsList.add(pts[0]);
		
		for(int i = 0; i < ptsList.size()-1; i++) {
			area += ptsList.get(i)[0] * ptsList.get(i+1)[1] - 
					ptsList.get(i+1)[0] * ptsList.get(i)[1];
		}
		
		area = 0.5 * Math.abs(area);
		
		return area;
	}

	/**
	 * Numerical trapezoidal integration of y(x) function
	 * A linear interpolation is made to build y(x) function
	 * that has to be integrated
	 * 
	 * @param x independent variable values
	 * @param y dependent variable values
	 * @param a lower value for integral evaluation
	 * @param b upper value for integral evaluation
	 * @return
	 */
	public static Double integrate1DTrapezoidLinear(double[] x, double[] y, double a, double b) {

		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
		
		return integrator.integrate(
				Integer.MAX_VALUE, 
				interpolate1DLinear(x,y), 
				a, b);
	}

	/**
	 * Numerical Simpson integration of y(x) function
	 * A linear interpolation is made to build y(x) function
	 * that has to be integrated
	 * 
	 * @param x independent variable values
	 * @param y dependent variable values
	 * @param a lower value for integral evaluation
	 * @param b upper value for integral evaluation
	 * @return
	 */
	public static Double integrate1DSimpsonSpline(double[] x, double[] y, double a, double b) {

		try {
		return new SimpsonIntegrator().integrate(
				Integer.MAX_VALUE, 
				interpolate1DSpline(x, y), 
				a, b);
		
		} catch (NonMonotonicSequenceException e) {
			return 0.;
		}
	}
	
	public static Double integrate1DSimpsonSpline(double[] x, double[] y) {
		return integrate1DSimpsonSpline(x, y, x[0], x[x.length-1]);
	}


	/**
	 * Numerical Legendre-Gauss integration of y(x) function
	 * A linear interpolation is made to build y(x) function
	 * that has to be integrated
	 * 
	 * @param x independent variable values
	 * @param y dependent variable values
	 * @param a lower value for integral evaluation
	 * @param b upper value for integral evaluation
	 * @return
	 */
	public static Double integrate1DIterativeLegendreGaussSpline(double[] x, double[] y, double a, double b) {

		IterativeLegendreGaussIntegrator integrator = 
				new IterativeLegendreGaussIntegrator(x.length*4, 1e-5, 1e-5);
		
		return integrator.integrate(
				Integer.MAX_VALUE, 
				interpolate1DSpline(x, y), 
				a, b);
	}

	public static Double integrate1DIterativeLegendreGaussSpline(double[] x, double[] y) {
		return integrate1DIterativeLegendreGaussSpline(x, y, x[0], x[x.length-1]);
	}


	/** 
	 * Extract a two-dimensional array ('page') from a three-dimensional array. 
	 * Returns a null if it fails.
	 * The given 3D array must be an array of 2D arrays of the same rank.  
	 * 
	 * @param mat, the 3D array; page_idx, the page index .
	 * @return the 2D array.
	 */
	public static double[][] extract2DArrayFrom3DArray(double[][][] mat, int page_idx) {

		if (mat == null) return null;

		int nRows = mat.length;
		int nCols = mat[0].length;
		int nPages = mat[0][0].length;

		if ( (page_idx < 0) || (page_idx >= nPages) ) return null;

		double[][] result = new double[nRows][nCols];

		for(int i = 0; i < nRows; i++) {
			for(int j = 0; j < nCols; j++) {
				result[i][j] = mat[i][j][page_idx];
				// System.out.println(i + "," + j + "," + k + ": " + dset[index]);
			}
		}
		return result;
	}

	/** Extract all two-dimensional arrays ('pages') from a three-dimensional array. Returns a null if it fails.
	 *  The given 3D array must be an array of 2D arrays of the same rank.  
	 * 
	 * @param mat, the 3D array. 
	 * @return the List of 2D arrays.
	 */	
	public static List<double[][]> extractAll2DArraysFrom3DArray(double[][][] mat) {

		if (mat == null) return null;

		//		int nRows = mat.length;
		//		int nCols = mat[0].length;
		int nPages = mat[0][0].length;

		List<double[][]> result = new ArrayList<double[][]>();

		for(int kPage = 0; kPage < nPages; kPage++) {
			result.add( extract2DArrayFrom3DArray(mat, kPage) ); 
		}
		return result;
	}

	/** 
	 * Extract all two-dimensional arrays ('pages') from a three-dimensional array.
	 * Returns a null if it fails.
	 * The given 3D array must be an array of 2D arrays of the same rank.  
	 * 
	 * @param mat, the 3D array. 
	 * @return the List of RealMatrix.
	 */	
	public static List<RealMatrix> extractAll2DMatricesFrom3DArray(double[][][] mat) {

		if (mat == null) return null;

		//		int nRows = mat.length;
		//		int nCols = mat[0].length;
		int nPages = mat[0][0].length;

		List<RealMatrix> result = new ArrayList<RealMatrix>();

		for(int kPage = 0; kPage < nPages; kPage++) {
			result.add( 
					new Array2DRowRealMatrix(
							extract2DArrayFrom3DArray(mat, kPage)
							)  
					); 
		}
		return result;
	}

	/**
	 * Get a linear interpolation univariate function
	 * 
	 * @param vx
	 * @param vy
	 * @return
	 */
	public static UnivariateFunction interpolate1DLinear(double[] vx, double[] vy) {

		if ( (vx == null) || (vy == null) ) return null;
		if ( vx.length != vy.length ) { 
			System.out.println("vx.lenght != vy.length");
			return null;
		}
		
//		double[] xx = MyUtilities.removeDuplicates(vx);
//		double[] yy = MyUtilities.removeDuplicates(vy);

		return new LinearInterpolator().interpolate(vx,vy);
	}

	/**
	 * Get a spline interpolation univariate function
	 * 
	 * @param vx
	 * @param vy
	 * @return
	 */
	public static UnivariateFunction interpolate1DSpline(double[] vx, double[] vy) {

		if ( (vx == null) || (vy == null) ) return null;
		if ( vx.length != vy.length ) return null;

//		double[] xx = MyUtilities.removeDuplicates(vx);
//		double[] yy = MyUtilities.removeDuplicates(vy);

		if (vx.length > 3) 
			return  new SplineInterpolator().interpolate(vx,vy);
		else 
			return  new LinearInterpolator().interpolate(vx,vy);

	}

	/** 
	 * Interpolate a set of y values as function of x values
	 * 
	 * @param vx vector of x's in ascending ordered 
	 * @param vy vector of y's
	 * @param x0 value of x
	 * @return value of y
	 */
	public static Double getInterpolatedValue1DLinear(double[] vx, double[] vy, double x0) {

		UnivariateFunction myInterpolationFunction = interpolate1DLinear(vx, vy);

		Double result;
		if (x0 <= vx[0]) {
			result = vy[0];
		} else if ((x0 >= vx[vx.length - 1])) {
			result = vy[vy.length - 1];
		} else {
			result = myInterpolationFunction.value(x0);
		}
		return result;
	}

	/** 
	 * Perform an interpolation using vy as known values, where 
	 * vy = f(vx). The method returns a doubleInterpolated array 
	 * which contains f(x0) values.
	 * 
	 * @param vx vector of x's in ascending ordered 
	 * @param vy vector of y's
	 * @param x0 the x values where the user wants to estimate the function
	 * @return value of y
	 */
	public static Double[] getInterpolatedValue1DLinear(double[] vx, double[] vy, double[] x0) {

		UnivariateFunction myInterpolationFunction = interpolate1DLinear(vx, vy);
		
		Double[] result = new Double[x0.length];

		for (int i=0; i < x0.length; i++) {
			if (x0[i] <= vx[0]) {
				result[i] = vy[0];
			} else if ((x0[i] >= vx[vx.length - 1])) {
				result[i] = vy[vy.length - 1];
			} else {
				result[i] = myInterpolationFunction.value(x0[i]);
			}
		}
		return result;
	}

	/** 
	 * Interpolate a set of y values as function of x values
	 * with a spline
	 * 
	 * @param vx vector of x's in ascending ordered 
	 * @param vy vector of y's
	 * @param x0 value of x
	 * @return value of y
	 */
	public static Double getInterpolatedValue1DSpline(double[] vx, double[] vy, double x0) {

		UnivariateFunction myInterpolationFunction = interpolate1DSpline(vx, vy);

		Double result;
		if (x0 <= vx[0]) {
			result = vy[0];
		} else if ((x0 >= vx[vx.length - 1])) {
			result = vy[vy.length - 1];
		} else {
			result = myInterpolationFunction.value(x0);
		}
		return result;
	}

	/** 
	 * Interpolates among a set of curves in a plane (v1, y), where curves parametrized in v0
	 * 
	 * @param var0 vector of discrete values of v0
	 * @param var1 vector of discrete values of v1
	 * @param data matrix (2D) of discrete values of y-data
	 * @param v0 first parameter
	 * @param v1 second parameter (along x-axis)
	 * @return interpolated value f(v0, v1)
	 */
	public static Double interpolate2DLinear(double[] var0, double[] var1, double[][] data, double v0, double v1) {

		if ( (var0 == null) || (var1 == null) || (data == null) ) return null;
		if ( 
				( data[0].length == 1 ) // data has only 1 column
//				|| (var0.length != data[0].length) // data columns 
//				|| (var1.length != data.length) // data rows
				) return null;

		Double result;
		RealMatrix dataMatrix2D = new Array2DRowRealMatrix(data);

		// Bracketing the external variable, v0
		// NOTE: elements of v0 must be ordered (possibly with no duplicates)

		// Double v0L = Double.NaN, v0R = Double.NaN;
		Double v0L = null, v0R = null;
		int j0L = -1, j0R = -1;
		Double resL = null, resR = null;

		if (v0 <= var0[0]) {
			Double v0LL = var0[0];
			result = getInterpolatedValue1DLinear(var1, dataMatrix2D.getColumn(0), v1);	
			
		}  else if ((v0 >= var0[var0.length - 1])) {
			Double v0RR =  var0[var0.length - 1];
			result = getInterpolatedValue1DLinear(var1, dataMatrix2D.getColumn(var0.length - 1), v1);
			
		} else {

			// get the bracketing values (lower and upper values) in var0
			for (int j = 1; j < var0.length; j++) {
				if ( v0 <= var0[j] ) {
					j0L = j - 1;
					v0L = var0[j0L];
					j0R = j;
					v0R = var0[j0R];
					break;
				}
			}
			
			if (v0R == v0L) return null; // defense from division by zero
			resL = getInterpolatedValue1DLinear(var1, dataMatrix2D.getColumn(j0L), v1);
			resR = getInterpolatedValue1DLinear(var1, dataMatrix2D.getColumn(j0R), v1);
			
			// interpolate results
			result = resL + (v0 - v0L)*(resR - resL)/(v0R - v0L);
		}
		
		return result;
	}

	/** Interpolates among a list of curve sets, each set in a plane (v2, y), where curves parametrized in v1;
	 *  each set of curves is associated to parameter v0
	 * 
	 * @param var0 vector of discrete values of v0
	 * @param var1 vector of discrete values of v1
	 * @param var2 vector of discrete values of v2
	 * @param data 3D array of discrete values of y-data, i.e. a list of 2D matrices (pages)
	 * @param v0 first parameter
	 * @param v1 second parameter
	 * @param v2 third parameter
	 * @return interpolated value f(v0, v1, v2)
	 */
	public static Double interpolate3DLinear(
			double[] var0, double[] var1, double[] var2, 
			double[][][] data, double v0, double v1, double v2) {

		if ( (var0 == null) || (var1 == null) || (var2 == null) || (data == null) ) return null;
		if ( 
				( data[0][0].length == 1 ) || // data has only 1 page
				( data[0].length == 1 ) // data pages have only 1 column 
//				|| (var0.length != data[0][0].length) // data pages 
//				|| (var1.length != data[0].length) // data columns
//				|| (var2.length != data.length) // data rows
				) return null;

		Double result;

		int nPages = var0.length;
		List<RealMatrix> dataMatrices2D = extractAll2DMatricesFrom3DArray(data);

		// Bracketing the external variable, v0
		// NOTE: elements of v0 must be ordered (possibly with no duplicates)

		// Double v0L = Double.NaN, v0R = Double.NaN;
		Double v0L = null, v0R = null;
		int j0L = -1, j0R = -1;
		Double resL = null, resR = null;

		if (v0 <= var0[0]) {
			Double v0LL = var0[0];
			result = interpolate2DLinear(var1, var2, dataMatrices2D.get(0).getData(), v1, v2);
			
		}  else if ((v0 >= var0[var0.length - 1])) {
			Double v0RR =  var0[var0.length - 1];
			result = interpolate2DLinear(var1, var2, dataMatrices2D.get(var0.length - 1).getData(), v1, v2);	
			
		} else {

			// get the bracketing values (lower and upper values) in var0
			for (int j = 1; j < var0.length; j++) {
				if ( v0 <= var0[j] ) {
					j0L = j - 1;
					v0L = var0[j0L];
					j0R = j;
					v0R = var0[j0R];
					break;
				}
			}
			
			if (v0R == v0L) return null; // defense from division by zero
			resL = interpolate2DLinear(var1, var2, dataMatrices2D.get(j0L).getData(), v1, v2);	
			resR = interpolate2DLinear(var1, var2, dataMatrices2D.get(j0R).getData(), v1, v2);	
			
			// interpolate results
			result = resL + (v0 - v0L)*(resR - resL)/(v0R - v0L);
		}
		
		return result;
	}

	public static double calculateSlopeLinear(
			double y2, double y1,
			double x2, double x1 ) {
		return (y2-y1)/(x2-x1);
	}

	/** This static function makes a summation of n element in a double vector
	 * 
	 * @param int loweLimit first element of vector to be added
	 * @param int upperLimit last element of vector to be added
	 * @param double [] the element of vector are the addends of the summation
	 * @return double 
	 * @author Manuela Ruocco
	 */	
	
	public static double summation (
			int lowerLimit, int upperLimit,
			double [] addend){
		double sum = 0.0;
		int numberOfIndexElements = upperLimit - lowerLimit;
		for (int i=0; i< numberOfIndexElements ; i++){
			sum += addend[i];
		}
		return sum;
	}

	public static double calculateFirstDerivative2Point (
			double[] xArray,
			double[] yArray
			) {
		
		return ((yArray[1]-yArray[0])/(xArray[1]-xArray[0]));
		
	}
	
	public static double calculateFirstDerivative3Points (
			double[] xArray,
			double[] yArray
			) {
		
		return (((yArray[2]-yArray[1])/(xArray[2]-xArray[1])) 
				+ ((yArray[1]-yArray[0])/(xArray[1]-xArray[0])))
				/2;
		
	};
	
	public static double[] calculateArrayFirstDerivative (
			double[] xArray,
			double[] yArray
			) {
		
		if(xArray.length != yArray.length) {
			System.err.println("THE LENGTH OF THE TWO ARRAYS HAS TO BE THE SAME !!!");
			return null;
		}
		
		double[] derivativeArray = new double[xArray.length];
		
		derivativeArray[0] = calculateFirstDerivative2Point(
				new double[] {xArray[0], xArray[1]},
				new double[] {yArray[0], yArray[1]}
				);
		
		for(int i=1; i<xArray.length-1; i++) {
			
			derivativeArray[i] = calculateFirstDerivative3Points(
					new double[] {xArray[i-1], xArray[i], xArray[i+1]},
					new double[] {yArray[i-1], yArray[i], yArray[i+1]}
					);
			
		}
		
		derivativeArray[derivativeArray.length-1] = calculateFirstDerivative2Point(
				new double[] {xArray[xArray.length-2], xArray[xArray.length-1]},
				new double[] {yArray[yArray.length-2], yArray[yArray.length-1]}
				);
			
		return derivativeArray;
		
	}
	
	public static double[] calculateArrayFirstDerivative2Point (
			double[] xArray,
			double[] yArray
			) {
		
		if(xArray.length != yArray.length) {
			System.err.println("THE LENGTH OF THE TWO ARRAYS HAS TO BE THE SAME !!!");
			return null;
		}
		
		double[] derivativeArray = new double[xArray.length];
	
		
		for(int i=1; i<xArray.length-1; i++) {
			
			derivativeArray[i-1] = calculateFirstDerivative2Point(
					new double[] {xArray[i-1], xArray[i]},
					new double[] {yArray[i-1], yArray[i]}
					);
			
		}
		derivativeArray[derivativeArray.length-1] = calculateFirstDerivative2Point(
				new double[] {xArray[xArray.length-2], xArray[xArray.length-1]},
				new double[] {yArray[yArray.length-2], yArray[yArray.length-1]}
				);
			
		
			
		return derivativeArray;
		
	}
	
	public static double getMaxMultiValue (double... value) {
		
		return Arrays.stream(value).max().getAsDouble();
		
	}
	
	public static double getMinMultiValue (double... value) {
		
		return Arrays.stream(value).min().getAsDouble();
		
	}
}
