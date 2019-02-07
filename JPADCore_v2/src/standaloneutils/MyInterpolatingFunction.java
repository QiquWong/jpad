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
	private QuadrilinearInterpolatingFunction qif;
	private double[] x, y, z, k;
	private double xMin, xMax, yMin, yMax, zMin, zMax, kMin, kMax;

	public MyInterpolatingFunction() {

	}

	public PolynomialSplineFunction interpolateLinear(double[] x, double[] data) {
		this.x = x;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		psf = new LinearInterpolator().interpolate(x, data);
		return psf;
	} 
	
	public PolynomialSplineFunction interpolateLinearAtIndex(double[] input, int index, double[] data) {
		
		switch (index) {
		case 0:
			this.x = input; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		psf = new LinearInterpolator().interpolate(input, data);
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
	
	public BilinearInterpolatingFunction interpolateBilinearAtIndex (
			double[] input1, double[] input2,  
			int index1, int index2, 
			double[][] data
			) {
		
		switch (index1) {
		case 0:
			this.x = input1; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input1; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input1; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input1; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		
		switch (index2) {
		case 0:
			this.x = input2; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input2; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input2; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input2; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		
		bif = new BilinearInterpolatingFunction(input1, input2, data);
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
	
	public TrilinearInterpolatingFunction interpolateTrilinearAtIndex (
			double[] input1, double[] input2, double[] input3, 
			int index1, int index2, int index3,
			double[][][] data
			) {
		
		switch (index1) {
		case 0:
			this.x = input1; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input1; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input1; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input1; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		
		switch (index2) {
		case 0:
			this.x = input2; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input2; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input2; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input2; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		
		switch (index3) {
		case 0:
			this.x = input3; 
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			break;
		case 1:
			this.y = input3; 
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			break;
		case 2:
			this.z = input3; 
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			break;
		case 3:
			this.k = input3; 
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			break;
		default:
			break;
		}
		
		tif = new TrilinearInterpolatingFunction(input1, input2, input3, data);
		return tif;
		
	}
	
	public QuadrilinearInterpolatingFunction interpolateQuadrilinear(double[] x, double[] y, double[] z, double[] k, double[][][][] data) {
		
		this.x = x; 
		this.y = y;
		this.z = z;
		this.k = k;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		yMin = MyArrayUtils.getMin(this.y);
		yMax = MyArrayUtils.getMax(this.y);
		zMin = MyArrayUtils.getMin(this.z);
		zMax = MyArrayUtils.getMax(this.z);
		kMin = MyArrayUtils.getMin(this.k);
		kMax = MyArrayUtils.getMax(this.k);
		
		qif = new QuadrilinearInterpolatingFunction(x, y, z, k, data);
		return qif;
		
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

	public double valueAtIndex(double input, int index) {
		
		double value = 0.0;
		
		switch (index) {
		case 0:
			if (input < xMin) input = xMin;
			if (input > xMax) input = xMax;
			value = psf.value(input);
			break;
		case 1:
			if (input < yMin) input = yMin;
			if (input > yMax) input = yMax;
			value = psf.value(input);
			break;
		case 2:
			if (input < zMin) input = zMin;
			if (input > zMax) input = zMax;
			value = psf.value(input);
			break;
		case 3:
			if (input < kMin) input = kMin;
			if (input > kMax) input = kMax;
			value = psf.value(input);
			break;
		default:
			break;
		}
		
		return value;
	}
	
	public double valueBilinear(double x, double y) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		return bif.value(x,y);
	}
	
	public double valueBilinearAtIndex(double input1, double input2, int index1, int index2) {
		
		switch (index1) {
		case 0:
			if (input1 < xMin) input1 = xMin;
			if (input1 > xMax) input1 = xMax;
			break;
		case 1:
			if (input1 < yMin) input1 = yMin;
			if (input1 > yMax) input1 = yMax;
			break;
		case 2:
			if (input1 < zMin) input1 = zMin;
			if (input1 > zMax) input1 = zMax;
			break;
		case 3:
			if (input1 < kMin) input1 = kMin;
			if (input1 > kMax) input1 = kMax;
			break;
		default:
			break;
		}
		
		switch (index2) {
		case 0:
			if (input2 < xMin) input2 = xMin;
			if (input2 > xMax) input2 = xMax;
			break;
		case 1:
			if (input2 < yMin) input2 = yMin;
			if (input2 > yMax) input2 = yMax;
			break;
		case 2:
			if (input2 < zMin) input2 = zMin;
			if (input2 > zMax) input2 = zMax;
			break;
		case 3:
			if (input2 < kMin) input2 = kMin;
			if (input2 > kMax) input2 = kMax;
			break;
		default:
			break;
		}
		
		return bif.value(input1, input2);
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
	
	public double valueTrilinearAtIndex(double input1, double input2, double input3, int index1, int index2, int index3) {
		
		switch (index1) {
		case 0:
			if (input1 < xMin) input1 = xMin;
			if (input1 > xMax) input1 = xMax;
			break;
		case 1:
			if (input1 < yMin) input1 = yMin;
			if (input1 > yMax) input1 = yMax;
			break;
		case 2:
			if (input1 < zMin) input1 = zMin;
			if (input1 > zMax) input1 = zMax;
			break;
		case 3:
			if (input1 < kMin) input1 = kMin;
			if (input1 > kMax) input1 = kMax;
			break;
		default:
			break;
		}
		
		switch (index2) {
		case 0:
			if (input2 < xMin) input2 = xMin;
			if (input2 > xMax) input2 = xMax;
			break;
		case 1:
			if (input2 < yMin) input2 = yMin;
			if (input2 > yMax) input2 = yMax;
			break;
		case 2:
			if (input2 < zMin) input2 = zMin;
			if (input2 > zMax) input2 = zMax;
			break;
		case 3:
			if (input2 < kMin) input2 = kMin;
			if (input2 > kMax) input2 = kMax;
			break;
		default:
			break;
		}
		
		switch (index3) {
		case 0:
			if (input3 < xMin) input3 = xMin;
			if (input3 > xMax) input3 = xMax;
			break;
		case 1:
			if (input3 < yMin) input3 = yMin;
			if (input3 > yMax) input3 = yMax;
			break;
		case 2:
			if (input3 < zMin) input3 = zMin;
			if (input3 > zMax) input3 = zMax;
			break;
		case 3:
			if (input3 < kMin) input3 = kMin;
			if (input3 > kMax) input3 = kMax;
			break;
		default:
			break;
		}
		
		return tif.value(input1, input2, input3);
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

	public double valueQuadrilinear(double x, double y, double z, double k) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		if (z < zMin) z = zMin;
		if (z > zMax) z = zMax;
		if (k < getkMin()) k = getkMin();
		if (k > getkMax()) k = getkMax();
		return qif.value(x,y,z,k);
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

	public double[] getK() {
		return k;
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

	public double getkMin() {
		return kMin;
	}

	public double getkMax() {
		return kMax;
	}
	
	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}

	public void setzMin(double zMin) {
		this.zMin = zMin;
	}

	public void setzMax(double zMax) {
		this.zMax = zMax;
	}

	public void setkMin(double kMin) {
		this.kMin = kMin;
	}

	public void setkMax(double kMax) {
		this.kMax = kMax;
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
				throw new DimensionMismatchException(z.length, data[0][0].length);
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
	
	public static class QuadrilinearInterpolatingFunction {
		
		double[] x;
		double xMin;
		double xMax;
		double[] y;
		double yMin;
		double yMax;
		double[] z;
		double zMin;
		double zMax;
		double[] k;
		double kMin;
		double kMax;
		
		List<TrilinearInterpolatingFunction> interpolatedDataAlongYAndZAndK;
		
		public QuadrilinearInterpolatingFunction (double[] x, double[] y, double[] z, double [] k ,double[][][][] data) {

			// x = var_0 = number of pages
			if (x.length != data.length) {
				throw new DimensionMismatchException(x.length, data.length);
			}
			
			// y = var_3 = number of rows
			if (y.length != data[0].length) {
				throw new DimensionMismatchException(y.length, data[0].length);
			}
			
			// z = var_2 = number of columns
			if (z.length != data[0][0].length) {
				throw new DimensionMismatchException(z.length, data[0][0].length);
			}
			
			// k = var_1 = number of tables
			if (k.length != data[0][0][0].length) {
				throw new DimensionMismatchException(k.length, data[0][0][0].length);
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
			
			if (k.length < 2) {
				throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS,
						k.length, 2, true);
			}
			
			this.x = x; 
			this.y = y;
			this.z = z;
			this.k = k;
			xMin = MyArrayUtils.getMin(this.x);
			xMax = MyArrayUtils.getMax(this.x);
			yMin = MyArrayUtils.getMin(this.y);
			yMax = MyArrayUtils.getMax(this.y);
			zMin = MyArrayUtils.getMin(this.z);
			zMax = MyArrayUtils.getMax(this.z);
			kMin = MyArrayUtils.getMin(this.k);
			kMax = MyArrayUtils.getMax(this.k);
			
		    MathArrays.checkOrder(x);
		    MathArrays.checkOrder(y);
		    MathArrays.checkOrder(z);
		    MathArrays.checkOrder(k);
		    
		    // getting bilinear interpolating functions for each page (along var_0 = z)
		    interpolatedDataAlongYAndZAndK = new ArrayList<>();
		    for(int i=0; i<x.length; i++)
		    	interpolatedDataAlongYAndZAndK.add(
		    			new TrilinearInterpolatingFunction(
		    					y,
		    					z,
		    					k,
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
		 * @param k = var_3 = table value
		 * @return
		 */
		public double value(double x, double y, double z, double k) {
			
			if (x < xMin) x = xMin;
			if (x > xMax) x = xMax;
			if (y < yMin) y = yMin;
			if (y > yMax) y = yMax;
			if (z < zMin) z = zMin;
			if (z > zMax) z = zMax;
			if (k < kMin) k = kMin;
			if (k > kMax) k = kMax;
			
			List<Double> valuesAtVar0 = new ArrayList<>();
			for(int i=0; i<interpolatedDataAlongYAndZAndK.size(); i++)
				valuesAtVar0.add(interpolatedDataAlongYAndZAndK.get(i).value(y,z,k));
			
			return MyMathUtils.getInterpolatedValue1DLinear(
					this.x,
					MyArrayUtils.convertToDoublePrimitive(valuesAtVar0), 
					x
					);
			
		}
		
	}

}
