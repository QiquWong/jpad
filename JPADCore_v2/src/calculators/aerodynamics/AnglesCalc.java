package calculators.aerodynamics;

import java.util.Arrays;

import org.apache.commons.math3.util.MathArrays;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

/**
 * A collection of static methods which evaluate angles
 * (e.g. angle of attack distribution over the wing,
 * lifting surface zero lift angle of attack and). 
 * 
 * UNLESS SPECIFIED ALL METHODS REQUIRE SI UNITS
 * (i.e., angles are in radians)
 * 
 * @author Lorenzo Attanasio
 *
 */
public class AnglesCalc {

	
	/**
	 * Evaluate the zero lift angle of attack of a lifting surface
	 * which is not geometrically twisted
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param surface
	 * @param semispan
	 * @param yStations
	 * @param chordVsY
	 * @param alpha0lVsY
	 * @return
	 */
	public static double alpha0LintegralMeanNoTwist(double surface, double semispan, 
			double[] yStations, double[] chordVsY, double[] alpha0lVsY) {
		return (2/surface) * MyMathUtils.integrate1DSimpsonSpline(
				yStations, 
				MathArrays.ebeMultiply(alpha0lVsY, chordVsY),
				0., semispan*0.999999);
	}

	/**
	 * Evaluate the zero lift angle of attack of a lifting surface
	 * which is geometrically twisted
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param surface
	 * @param semispan
	 * @param yStations
	 * @param chordVsY
	 * @param alpha0lVsY
	 * @param twistVsY
	 * @return
	 */
	public static double alpha0LintegralMeanWithTwist(double surface, double semispan, 
			double[] yStations, double[] chordVsY, double[] alpha0lVsY,
			double[] twistVsY) {
//		System.out.println(" y stat " + yStations.length);
//		System.out.println(" chord vs y " + chordVsY.length);
//		System.out.println(" twist " + twistVsY.length);
//		System.out.println("alpha zero lift " + alpha0lVsY.length);
//		System.out.println(" chord " + Arrays.toString(chordVsY));

		
		return (2/surface) * MyMathUtils.integrate1DSimpsonSpline(
				yStations,
				MathArrays.ebeMultiply(
						(MathArrays.ebeSubtract(alpha0lVsY, twistVsY)),
						chordVsY), 
						0., semispan*0.999999);
	}

	/**
	 * The angle of attack distribution over the lifting surface span
	 * given the root chord AoA, the geometric twist and the zero lift
	 * AoA of the airfoils over the LS span 
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param alphaRoot
	 * @param twistVsY
	 * @param alpha0lVsY
	 * @return
	 */
	public static double[] getAlphaDistribution(double alphaRoot, double[] twistVsY, double[] alpha0lVsY) {
		return MathArrays.ebeAdd(
				MyArrayUtils.fill(alphaRoot, twistVsY.length),
				MathArrays.ebeSubtract(twistVsY, alpha0lVsY));
	}

}
