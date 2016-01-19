package standaloneutils;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class MyInterpolatingFunction {

	private PolynomialSplineFunction psf;
	private BicubicInterpolatingFunction bi;
	private TricubicInterpolatingFunction ti;
	private double[] x, y, z;
	private double xMin, xMax, yMin, yMax, zMin, zMax;

	public MyInterpolatingFunction() {

	}

	public PolynomialSplineFunction interpolate(double[] x, double[] data) {
		this.x = x;
		xMin = MyArrayUtils.getMin(this.x);
		xMax = MyArrayUtils.getMax(this.x);
		psf = new SplineInterpolator().interpolate(x, data);
		return psf;
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

	public double value(double x, double y) {
		if (x < xMin) x = xMin;
		if (x > xMax) x = xMax;
		if (y < yMin) y = yMin;
		if (y > yMax) y = yMax;
		return bi.value(x,y);
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

}
