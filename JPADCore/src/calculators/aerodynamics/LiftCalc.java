package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;


public class LiftCalc {

	private LiftCalc() {}

	/**
	 * Helmbold-Diederich formula
	 * 
	 * @param arVertical
	 * @param sweepV_c2 (deg)
	 * @param eff3D
	 * @param effMach
	 * @return vertical tail lift coefficient slope (1/rad)
	 */
	public static double calculateCLalphaHelmboldDiederich(double ar, double cLalpha2D, double sweepV_c2, double mach) {
		
		if (cLalpha2D == 0.) return 0.;
		
		double effMach = AerodynamicCalc.calculatePrandtlGlauertCorrection(mach);
		double eff3D = effMach * ar / (Math.toDegrees(cLalpha2D) / 2*Math.PI*effMach);
        
//		return 2. * Math.PI * ar / 
//        		(2 + Math.sqrt( Math.pow(eff3D,2) * (1 + Math.tan(Math.toRadians(sweepV_c2) / Math.pow(effMach, 2)) + 4) ));
		return 2. * Math.PI * ar / 
        		(2 + Math.sqrt(Math.pow(eff3D,2) * (1 + Math.pow(Math.tan(Math.toRadians(sweepV_c2)),2) / Math.pow(effMach, 2)) + 4 ));
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param lift
	 * @param speed
	 * @param surface
	 * @param altitude
	 * @return
	 */
	public static double calculateLiftCoeff(double lift, double speed, double surface, double altitude) {
		return 2.*lift/(speed*speed*AtmosphereCalc.getDensity(altitude)*surface);
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param alpha0L
	 * @param cLalpha
	 * @return
	 */
	public static double calculateLiftCoefficientAtAlpha0(double alpha0L, double cLalpha) {
		return - alpha0L*cLalpha;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param ar
	 * @param machCurrent
	 * @param sweepLEEquivalent
	 * @param taperRatioEquivalent
	 * @return
	 */
	public static double kFactorPolhamus(double ar, double machCurrent, 
			Amount<Angle> sweepLEEquivalent, double taperRatioEquivalent) {

		double kPolhamus = 0.;
		double aR = ar;

		if (machCurrent < 0.7 
				&& sweepLEEquivalent.getEstimatedValue() < Amount.valueOf(32., NonSI.DEGREE_ANGLE).getEstimatedValue()
				&& taperRatioEquivalent > 0.4
				&& taperRatioEquivalent < 1.
				&& aR > 3 && aR < 8) {

			if(aR < 4) {
				kPolhamus = 1 + 
						aR
						*(1.87 - 0.000233 * sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						/100;
			} else  {
				kPolhamus = 1 + (
						(8.2 - 2.3*sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						- aR * (0.22 - 0.153 *sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						)/100;
			} 
		}

		return kPolhamus;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param ar
	 * @param machCurrent
	 * @param sweepLEEquivalent
	 * @param taperRatioEquivalent
	 * @return
	 */
	public static double calculateCLalphaPolhamus(double ar, double machCurrent, 
			Amount<Angle> sweepLEEquivalent, double taperRatioEquivalent) {

		double cLAlpha = 0.;
		double kPolhamus = LiftCalc.kFactorPolhamus(
				ar, machCurrent, 
				sweepLEEquivalent, taperRatioEquivalent);

		if (kPolhamus != 0.) {

			double sweepHalfEq = LSGeometryCalc.calculateSweep(ar, taperRatioEquivalent, sweepLEEquivalent.doubleValue(SI.RADIAN), 0.5, 0.).doubleValue(SI.RADIAN);

			cLAlpha = 2*Math.PI*ar
					/(2 + sqrt(
							( (pow(ar, 2)*(1 - pow(machCurrent,2))
									/pow(kPolhamus, 2))
									*(1 + pow(tan(sweepHalfEq), 2)
									/(1 - pow(machCurrent,2))
											) + 4) ));
		}	

		return cLAlpha;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param machCurrent
	 * @param ar
	 * @param semispan
	 * @param sweepHalfChordEq
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCLalphaAndersonSweptCompressibleSubsonic(
			double machCurrent, double ar, double semispan, double sweepHalfChordEq,
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		double cLAlpha = 0.;
		double cLAlphaMean2D = calcCLalphaIntegralMean2D(Math.pow(semispan, 2)/ar, semispan, yStations, clAlphaVsY, chordsVsY);

		if (machCurrent < 1) {
			cLAlpha = cLAlphaMean2D * cos(sweepHalfChordEq) 
					/(sqrt(1 - pow(machCurrent,2)
							* pow(cos(sweepHalfChordEq), 2) 
							+ pow(cLAlphaMean2D 
									* cos(sweepHalfChordEq)
									/(Math.PI*ar), 2)) 
							+ cLAlphaMean2D 
							* cos(sweepHalfChordEq)/(Math.PI*ar)
							);

		} else {
			System.out.println("This method can be used in subsonic regime only");
		}

		return cLAlpha;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * 
	 * @param surface
	 * @param semispan
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCLalphaIntegralMean2D(double surface, double semispan, 
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		return (2./surface) * MyMathUtils.integrate1DSimpsonSpline(
						yStations, 
						MathArrays.ebeMultiply(clAlphaVsY, chordsVsY),
						0., semispan*0.9995);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 3 DLR pdf
	 * 
	 * @param alpha
	 */
	public static double calcCLatAlphaLinearDLR(double alpha, double ar) {
		return alpha * 5.53 * ar / (ar + 1.76);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 185 Sforza (2014)
	 * 
	 * @param clMax airfoil mean maximum lift coefficient
	 * @param cLAlpha
	 * @param tr taper ratio
	 * @param sweepLE LE sweep
	 * @param ar aspect ratio
	 * @param twist radians
	 * @param engineType
	 */
	public static double calculateCLmaxPhillipsAndAlley(
			double clMax, double cLAlpha, 
			double tr, double sweepLE, 
			double ar, double twist,
			EngineTypeEnum engineType) {

		double cLclMax = (0.952 - 0.45*pow(tr - 0.5, 2)) * pow(ar/12., 0.03);
		double kLambda1 = 0.15 + 18.5*(tr - 0.4)/ar;
		double kLambda2 = 0.55 + 12.*(tr - 0.275)/ar;
		double kLambda = 1 + kLambda1*sweepLE - kLambda2*pow(sweepLE,1.2);
		double kLS = 1 + (0.0042*ar - 0.068)*(1 + 2.3*cLAlpha*twist/clMax);
		double kOmega = 0.;

		if (engineType.equals(EngineTypeEnum.TURBOPROP)) {
			kOmega = 0.1;
		} else if(engineType.equals(EngineTypeEnum.TURBOFAN)) {
			kOmega = -0.2;
		}

		return cLclMax*kLS*kLambda*clMax*(1 - kOmega*cLAlpha*(-twist)/clMax);
	}

}
