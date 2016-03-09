package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;


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

	
	public static double[] calculateCLvsAlphaArrayNasaBlackwell(LiftingSurface theLiftingSurface, MyArray alphaArray, int nValue){
		
		LSAerodynamicsManager theLsManager = theLiftingSurface.getAerodynamics();
		double [] cLActualArray = new double[nValue];
		LSAerodynamicsManager.CalcCLAtAlpha theClatAlphaCalculator =theLsManager.new CalcCLAtAlpha();
		double cLStar, cLTemp, qValue, a ,b ,c ,d;
		Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator =theLsManager.new MeanAirfoil();
		MyAirfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theLiftingSurface);
		double alphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
		Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);
		double alphaActual = 0;
		Amount<Angle> alphaMax;
		double cLStarWing, cLLinearSlope, cLAlphaZero, alphaZeroLiftWingClean;
		for (int i=0; i<nValue; i++ ){
		alphaActual = alphaArray.get(i);
		
		cLStarWing = theClatAlphaCalculator.nasaBlackwell(alphaStarAmount);
		cLTemp = theClatAlphaCalculator.nasaBlackwell(alphaTemp);
		if (alphaActual < alphaStar){    //linear trait
			cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
			//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
			qValue = cLStarWing - cLLinearSlope*alphaStar;
			cLAlphaZero = qValue;
			alphaZeroLiftWingClean = -qValue/cLLinearSlope;
			cLActualArray[i] = cLLinearSlope * alphaActual+ qValue;
			//System.out.println(" CL Actual = " + cLActual );
		}

		else {  // non linear trait

			theLsManager.calcAlphaAndCLMax(meanAirfoil);
			double cLMax = theLsManager.get_cLMaxClean();
			alphaMax = theLsManager.get_alphaMaxClean();	
			double alphaMaxDouble = alphaMax.getEstimatedValue();

			cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
			//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
			double[][] matrixData = { {Math.pow(alphaMaxDouble, 3), Math.pow(alphaMaxDouble, 2), alphaMaxDouble,1.0},
					{3* Math.pow(alphaMaxDouble, 2), 2*alphaMaxDouble, 1.0, 0.0},
					{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
					{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),alphaStar,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {cLMax, 0,cLLinearSlope, cLStarWing};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			a = solSystem[0];
			b = solSystem[1];
			c = solSystem[2];
			d = solSystem[3];

			cLActualArray[i] = a * Math.pow(alphaActual, 3) + 
					b * Math.pow(alphaActual, 2) + 
					c * alphaActual + d;
		}

		}
		return cLActualArray;
	}
	
}
