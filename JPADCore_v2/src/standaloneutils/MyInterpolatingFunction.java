package standaloneutils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;

public class MyInterpolatingFunction {

	private PolynomialSplineFunction psf;
	private BicubicInterpolatingFunction bi;
	private TricubicInterpolatingFunction ti;
	private BilinearInterpolatingFunction bif;
	private TrilinearInterpolatingFunction tif;
	private double[] x, y, z;
	private double xMin, xMax, yMin, yMax, zMin, zMax;

	public MyInterpolatingFunction() {

	}

	public PolynomialSplineFunction interpolateLinear(double[] x, double[] data) {
		this.x = x;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		psf = new LinearInterpolator().interpolate(x, data);
		return psf;
	} 
	
	public PolynomialSplineFunction interpolate(double[] x, double[] data) {
		this.x = x;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		psf = new SplineInterpolator().interpolate(x, data);
		return psf;
	}

	public BilinearInterpolatingFunction interpolateBilinear (double[] x, double[] y, double[][] data) {
		
		this.x = x; 
		this.y = y;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		yMin = MyArrayUtils.getMin(this.y);
		yMax = MyArrayUtils.getMax(this.y);
		
		bif = new BilinearInterpolatingFunction(x, y, data);
		return bif;
		
	}
	
	public BicubicInterpolatingFunction interpolate(double[] x, double[] y, double[][] data) {
		this.x = x; 
		this.y = y;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		yMin = MyArrayUtils.getMin(this.y);
		yMax = MyArrayUtils.getMax(this.y);
		bi = new BicubicInterpolator().interpolate(x, y, data);
		return bi;
	}

	public TrilinearInterpolatingFunction interpolateTrilinear(double[] x, double[] y, double[] z, double[][][] data) {
		
		this.x = x; 
		this.y = y;
		this.z = z;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		yMin = MyArrayUtils.getMin(this.y);
		yMax = MyArrayUtils.getMax(this.y);
		zMin = MyArrayUtils.getMin(this.z);
		zMax = MyArrayUtils.getMax(this.z);
		
		tif = new TrilinearInterpolatingFunction(x, y, z, data);
		return tif;
		
	}
	
	public TricubicInterpolatingFunction interpolate(double[] x, double[] y, double[] z, double[][][] data) {
		this.x = x; 
		this.y = y;
		this.z = z;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		yMin = MyArrayUtils.getMin(this.y);
		yMax = MyArrayUtils.getMax(this.y);
		zMin = MyArrayUtils.getMin(this.z);
		zMax = MyArrayUtils.getMax(this.z);
		ti = new TricubicInterpolator().interpolate(x, y, z, data);
		return ti;
	}

	public double value(double x) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		return psf.value(x);
	}

	public double valueBilinear(double x, double y) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		return bif.value(x,y);
	}
	
	public double value(double x, double y) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		return bi.value(x,y);
	}

	public double valueTrilinear(double x, double y, double z) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		if (z < zMin) z = zMin;
		if (z > zMax) z = zMax;
		return tif.value(x,y,z);
	}
	
	public double value(double x, double y, double z) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		if (z < zMin) z = zMin;
		if (z > zMax) z = zMax;
		return ti.value(x,y,z);
	}

	public double[] getX() {
		return x;
	}

	public double[] getY() {
		return y;
	}

	public double[] getZ() {
		return z;
	}

	public double getxMin() {
		return xMin;
	}

	public double getxMax() {
		return xMax;
	}

	public double getyMin() {
		return yMin;
	}

	public double getyMax() {
		return yMax;
	}

	public double getzMin() {
		return zMin;
	}

	public double getzMax() {
		return zMax;
	}

	public static class BilinearInterpolatingFunction {
		
		double[] x;
		double xMin;
		double xMax;
		double[] y;
		double yMin;
		double yMax;
		
		List<PolynomialSplineFunction> interpolatedDataAlongX;
		
		public BilinearInterpolatingFunction (double[] x, double[] y, double[][] data) {

			// x = var_1 = number of rows
			if (x.length != data.length) {
				throw new DimensionMismatchException(x.length, data.length);
			}

			// y = var_0 = number of columns
			if (y.length != data[0].length) {
				throw new DimensionMismatchException(y.length, data[0].length);
			}
			
			if (x.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						x.length, 2, true);
			}
			
			if (y.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						y.length, 2, true);
			}
			
			this.x = x; 
			this.y = y;
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			
		    MathArrays.checkOrder(x);
		    MathArrays.checkOrder(y);
			
		    // getting linear interpolating functions for each row (along var_1 = x)
		    interpolatedDataAlongX = new ArrayList<>();
		    for(int i=0; i<x.length; i++)
		    	interpolatedDataAlongX.add(
		    			new LinearInterpolator().interpolate(y, data[i])
		    			);
		    
		}
		
		/**
		 *@author Vittorio Trifari
		 * 
		 * @param x = var_1 = row value
		 * @param y = var_0 = column value
		 * @return
		 */
		public double value(double x, double y) {
			
			// x = var_1
			// y = var_0
			
			if (x < xMin) x = xMin;
			if (x > xMax) x = xMax;
			if (y < yMin) y = yMin;
			if (y > yMax) y = yMax;
			
			List<Double> valuesAtVar0 = new ArrayList<>();
			for(int i=0; i<interpolatedDataAlongX.size(); i++)
				valuesAtVar0.add(interpolatedDataAlongX.get(i).value(y));
			
			return MyMathUtils.getInterpolatedValue1DLinear(
					this.x,
					MyArrayUtils.convertToDoublePrimitive(valuesAtVar0), 
					x
					);
			
		}
		
	}
	
	public static class TrilinearInterpolatingFunction {
		
		double[] x;
		double xMin;
		double xMax;
		double[] y;
		double yMin;
		double yMax;
		double[] z;
		double zMin;
		double zMax;
		
		List<BilinearInterpolatingFunction> interpolatedDataAlongYAndZ;
		
		public TrilinearInterpolatingFunction (double[] x, double[] y, double[] z ,double[][][] data) {

			// x = var_0 = number of pages
			if (x.length != data.length) {
				throw new DimensionMismatchException(x.length, data.length);
			}

			// y = var_2 = number of rows
			if (y.length != data[0].length) {
				throw new DimensionMismatchException(y.length, data[0].length);
			}
			
			// z = var_1 = number of columns
			if (z.length != data[0][0].length) {
				throw new DimensionMismatchException(y.length, data[0][0].length);
			}
			
			if (x.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						x.length, 2, true);
			}
			
			if (y.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						y.length, 2, true);
			}
			
			if (z.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						z.length, 2, true);
			}
			
			this.x = x; 
			this.y = y;
			this.z = z;
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			
		    MathArrays.checkOrder(x);
		    MathArrays.checkOrder(y);
		    MathArrays.checkOrder(z);
		    
		    // getting bilinear interpolating functions for each page (along var_0 = z)
		    interpolatedDataAlongYAndZ = new ArrayList<>();
		    for(int i=0; i<x.length; i++)
		    	interpolatedDataAlongYAndZ.add(
		    			new BilinearInterpolatingFunction(
		    					y,
		    					z,
		    					data[i]
		    					)
		    			);
		    
		}
		
		/**
		 *@author Vittorio Trifari
		 * 
		 * @param x = var_0 = page value
		 * @param y = var_2 = row value
		 * @param z = var_1 = column value
		 * @return
		 */
		public double value(double x, double y, double z) {
			
			if (x < xMin) x = xMin;
			if (x > xMax) x = xMax;
			if (y < yMin) y = yMin;
			if (y > yMax) y = yMax;
			if (z < zMin) z = zMin;
			if (z > zMax) z = zMax;
			
			List<Double> valuesAtVar0 = new ArrayList<>();
			for(int i=0; i<interpolatedDataAlongYAndZ.size(); i++)
				valuesAtVar0.add(interpolatedDataAlongYAndZ.get(i).value(y,z));
			
			return MyMathUtils.getInterpolatedValue1DLinear(
					this.x,
					MyArrayUtils.convertToDoublePrimitive(valuesAtVar0), 
					x
					);
			
		}
		
	}
	
}
