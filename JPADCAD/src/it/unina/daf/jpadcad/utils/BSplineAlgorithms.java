package it.unina.daf.jpadcad.utils;

import opencascade.TColgp_HArray1OfPnt;

public class BSplineAlgorithms {

	public static final double REL_TOL_CLOSED = 1.0e-8;

	public static double[] computeParamsBSplineCurve(TColgp_HArray1OfPnt points, double umin, double umax, double alpha) {


		if ( umax <= umin ) {
			System.err.println("The specified start parameter is larger than the specified end parameter");
			// TODO: throw exception ??
		}

		double[] parameters = new double[points.Length()];
		parameters[0] = 0.;

		for (int i = 1; i < parameters.length; ++i) {
			//	        int iArray = static_cast<int>(i) + points->Lower();
			//	        double length = pow(points->Value(iArray).SquareDistance(points->Value(iArray - 1)), alpha / 2.);
			//	        parameters[i] = parameters[i - 1] + length;
		}

		return null; // TODO

	}

}
