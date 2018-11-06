package it.unina.daf.jpadcad.utils;

import javaslang.Tuple2;
import opencascade.TColgp_Array2OfPnt;
import opencascade.TColgp_HArray1OfPnt;

public class BSplineAlgorithms {

	public static final double REL_TOL_CLOSED = 1.0e-8;

	/**
	 * Computes the parameters of a Geom_BSplineCurve at the given points
	 * @param points	Given points where new parameters are computed at
	 * @param umin		First parameter of the curve
	 * @param umax		Last parameter of the curve
	 * @param alpha		Exponent for the computation of the parameters; alpha=0.5 means, that this method uses the centripetal method
	 * @return			Array of parameters
	 */
	public static double[] computeParamsBSplineCurve(TColgp_HArray1OfPnt points, double umin, double umax, double alpha) {

		if ( umax <= umin ) {
			System.err.println("The specified start parameter is larger than the specified end parameter");
			throw new IllegalArgumentException("Bad (umin, umax)");
		}

		double[] parameters = new double[points.Length()];
		parameters[0] = 0.;

		for (int i = 1; i < parameters.length; i++) {
			int iArray = i + points.Lower(); 
			double length = Math.pow(points.Value(iArray).SquareDistance(points.Value(iArray - 1)), alpha / 2.);
			parameters[i] = parameters[i - 1] + length;
		}

		double totalLength = parameters[parameters.length - 1];

		for (int i = 0; i < parameters.length; i++) {
			double ratio = 0.;
			if (totalLength < 1e-10) {
				ratio = i / (double)(parameters.length - 1);
			}
			else {
				ratio = parameters[i] / totalLength;
			}
			parameters[i] = (umax - umin) * ratio + umin;
		}

		return parameters;		
	}
	
	/**
	 * Computes the parameters of a Geom_BSplineCurve at the given points, 
	 * assuming	umin=0 (First parameter of the curve), and umax=1 (Last parameter of the curve)
	 * @param points	Given points where new parameters are computed at
	 * @param alpha		Exponent for the computation of the parameters; alpha=0.5 means, that this method uses the centripetal method
	 * @return			Array of parameters
	 */
	public static double[] computeParamsBSplineCurve(TColgp_HArray1OfPnt points, double alpha) {
		return computeParamsBSplineCurve(points, 0.0, 1.0, alpha);
	}

	/**
	 * Computes the parameters of a Geom_BSplineCurve at the given points, 
	 * assuming	umin=0 (First parameter of the curve), and umax=1 (Last parameter of the curve),
	 * and alpha=0.5 (Exponent for the computation of the parameters; i.e. uses the centripetal method)
	 * @param points	Given points where new parameters are computed at
	 * @return			Array of parameters
	 */
	public static double[] computeParamsBSplineCurve(TColgp_HArray1OfPnt points) {
		return computeParamsBSplineCurve(points, 0.0, 1.0, 0.5);
	}
	
//	/**
//	 * Computes the parameters of a Geom_BSplineSurface at the given points
//	 * @param points	Given points where new parameters are computed at
//	 * @param alpha		Exponent for the computation of the parameters; alpha=0.5 means that this method uses the centripetal method
//	 * @return			A Tuple2 of arrays
//	 */
//	public static Tuple2<double[], double[]> computeParamsBSplineSurf(TColgp_Array2OfPnt points, double alpha) {
//		return null; // TODO
//	}

}
