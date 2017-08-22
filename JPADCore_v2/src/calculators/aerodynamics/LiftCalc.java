package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.HighLiftDeviceEffectEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;


public class LiftCalc {

	private LiftCalc() {}

	public static List<Double> calculateLiftingCoefficientVsMachPrandtlGlauert(
			List<Double> machList,
			double cLAtMachZero
			) {
		
		return machList.stream()
				.map(m -> AerodynamicCalc.calculatePrandtlGlauertCorrection(m))
					.map(m -> cLAtMachZero/m)
						.collect(Collectors.toList());
		
	}
	
	/**
	 * @see S. Ciornei. 
	 * 		Mach number, relative thickness, sweep and lift coefficient of the wing 
	 * 			- An empirical investigation of parameters and equations. 
	 * 		Paper. 
	 * 		31.05.2005.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param machList
	 * @param tcMeanAirfoil
	 * @param sweepQuarterChord
	 * @param deltaCriticalMach depends on the airfoil type (0 = peaky; -0.04 = NACA; 0.04 = SUPERCRITICAL; 0.06 = MODERN SUPERCRITICAL) 
	 * @return the list of CL
	 */
	public static List<Double> calculateLiftingCoefficientFromCriticalMachKroo(
			List<Double> machList,
			double tcMeanAirfoil,
			Amount<Angle> sweepQuarterChord,
			double deltaCriticalMach
			) {
		
		double x = tcMeanAirfoil/Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN));
		double k1 = 2.8355*Math.pow(x, 2)-1.9072*x+0.9499;
		double k2 = 0.2*(1-2.131*x);
		
		return machList.stream()
				.map(m -> ((k1*Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),2))
						-((m-deltaCriticalMach)*Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),3)))
						/k2)
					.collect(Collectors.toList());
		
	}
	
	/**
	 * @see S. Ciornei. 
	 * 		Mach number, relative thickness, sweep and lift coefficient of the wing 
	 * 			- An empirical investigation of parameters and equations. 
	 * 		Paper. 
	 * 		31.05.2005.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param machList
	 * @param tcMeanAirfoil
	 * @param sweepQuarterChord
	 * @param deltaCriticalMach depends on the airfoil type (0 = peaky; -0.04 = NACA; 0.04 = SUPERCRITICAL; 0.06 = MODERN SUPERCRITICAL) 
	 * @return the list of CL
	 */
	public static List<Double> calculateLiftingCoefficientFromDivergenceMachKroo(
			List<Double> machList,
			double tcMeanAirfoil,
			Amount<Angle> sweepQuarterChord,
			double deltaCriticalMach
			) {
		
		double x = tcMeanAirfoil/Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN));
		double k1 = 2.8355*Math.pow(x, 2)-1.9072*x+0.9499;
		double k2 = 0.2*(1-2.131*x);
		
		return machList.stream()
				.map(m -> ((k1*Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),2))
						-(((m-deltaCriticalMach)/(1.02+0.08*(1-Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)))))*Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),3)))
						/k2)
					.collect(Collectors.toList());
		
	}
	
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
		//double eff3D = effMach * ar / (Math.toDegrees(cLalpha2D) / 2*Math.PI*effMach);
		double eff3D = effMach * ar / (cLalpha2D / (2*Math.PI*effMach));

		//		return 2. * Math.PI * ar / 
		//        		(2 + Math.sqrt( Math.pow(eff3D,2) * (1 + Math.tan(Math.toRadians(sweepV_c2) / Math.pow(effMach, 2)) + 4) ));
		
		return 2. * Math.PI * ar / 
				(2 + Math.sqrt(Math.pow(eff3D,2) * (1 + Math.pow(Math.tan(Math.toRadians(sweepV_c2)),2) / Math.pow(effMach, 2)) + 4 ));
	}

	/**
	 * This method allows the user to evaluate the lift with an assigned CL, assuming a leveled
	 * flight at which L=W;
	 * 
	 * @author Vittorio Trifari
	 * @param speed
	 * @param surface
	 * @param altitude
	 * @param cL
	 * @return
	 */
	public static double calculateLift(double speed, double surface, double altitude, double cL) {
		return 0.5*AtmosphereCalc.getDensity(altitude)*surface*Math.pow(speed, 2)*cL;
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

	public static double calculateCLAlphaAtMachNasaBlackwell (
			Amount<Length> semiSpan,
			Amount<Area> surface,
			List<Amount<Length>> yStationDistribution,
			List<Amount<Length>> chordDistribution,
			List<Amount<Length>> xLEDistribution,
			List<Amount<Angle>> dihedralDistribution,
			List<Amount<Angle>> twistDistribution,
			List<Amount<Angle>> alphaZeroLiftDistribution,
			double vortexSemiSpanToSemiSpanRatio,
			double currentMach,
			Amount<Length> altitude
			) {
		
		NasaBlackwell theNasaBlackwellCalculator = new NasaBlackwell(
				semiSpan.doubleValue(SI.METER),
				surface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(dihedralDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(twistDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaZeroLiftDistribution),
				vortexSemiSpanToSemiSpanRatio,
				0.0, // alpha 
				currentMach,
				altitude.doubleValue(SI.METER)
				);
		
		Amount<Angle> alphaOne = Amount.valueOf(toRadians(0.), SI.RADIAN);
		theNasaBlackwellCalculator.calculate(alphaOne);
		double clOne = theNasaBlackwellCalculator.getCLCurrent();

		Amount<Angle>alphaTwo = Amount.valueOf(toRadians(4.), SI.RADIAN);
		theNasaBlackwellCalculator.calculate(alphaTwo);
		double clTwo = theNasaBlackwellCalculator.getCLCurrent();

		return (clTwo-clOne)/alphaTwo.getEstimatedValue();
		
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
			AerodynamicDatabaseReader aeroDatabaseReader
			) {

		double cLclMax = (0.952 - 0.45*pow(tr - 0.5, 2)) * pow(ar/12., 0.03);
		double kLambda1 = 0.15 + 18.5*(tr - 0.4)/ar;
		double kLambda2 = 0.55 + 12.*(tr - 0.275)/ar;
		double kLambda = 1 + kLambda1*sweepLE - kLambda2*pow(sweepLE,1.2);
		double kLS = 1 + (0.0042*ar - 0.068)*(1 + 2.3*cLAlpha*twist/clMax);
		double kOmega = aeroDatabaseReader.getKOmegePhillipsAndAlley(
				cLAlpha,
				twist,
				clMax,
				tr,
				ar
				);

		return cLclMax*kLS*kLambda*clMax*(1 - kOmega*cLAlpha*(-twist)/clMax);
	}

	public static double calculateCLAtAlphaNonLinearTrait(
			Amount<Angle> alphaActual,
			Amount<?> cLAlpha,
			double cLStar,
			Amount<Angle> alphaStar,
			double cLmax,
			Amount<Angle> alphaStall
			) {
		
		double cLActual = 0.0;
			
		double[][] matrixData = { 
				{
					Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
					Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
					alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
					1.0
				},
				{
					3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
					2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
					1.0,
					0.0
				},
				{
					3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
					2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
					1.0,
					0.0
				},
				{
					Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
					Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
					alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
					1.0
				}
		};
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		double [] vector = {
				cLmax,
				0,
				cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
				cLStar
				};

		double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

		double a = solSystem[0];
		double b = solSystem[1];
		double c = solSystem[2];
		double d = solSystem[3];

		cLActual = 
				a * Math.pow(alphaActual.doubleValue(NonSI.DEGREE_ANGLE), 3) + 
				b * Math.pow(alphaActual.doubleValue(NonSI.DEGREE_ANGLE), 2) + 
				c * alphaActual.doubleValue(NonSI.DEGREE_ANGLE) + 
				d;
		
		return cLActual;
		
	}

	public static Double[] calculateCLAlphaArray( //1/deg
		Double[] cL3DCurve,
		List<Amount<Angle>> alphaArray
			) {
		
	Double [] cLAlphaArray = new Double [cL3DCurve.length];
	

	for (int i=0 ; i<alphaArray.size()-1; i++){
			cLAlphaArray[i] = (cL3DCurve[i+1] - cL3DCurve[i])/
					(alphaArray.get(i+1).doubleValue(SI.RADIAN) - alphaArray.get(i).doubleValue(SI.RADIAN));			
		
	}
	cLAlphaArray[cLAlphaArray.length-1] = cLAlphaArray[cLAlphaArray.length-2];
	return cLAlphaArray; // 1/deg
	}
	
	
	
	public static Double[] calculateCLvsAlphaArray(
			double cL0,
			double cLmax,
			Amount<Angle> alphaStar,
			Amount<Angle> alphaStall,
			Amount<?> cLAlpha,
			Double[] alphaArray
			) {
	
		Double[] cLArray = new Double[alphaArray.length];
		
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		double e = 0.0;
		

		double cLStar = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				* alphaStar.doubleValue(NonSI.DEGREE_ANGLE))
				+ cL0;
		for(int i=0; i<alphaArray.length; i++) {
			if(alphaArray[i] <= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)) {
				cLArray[i] = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
						* alphaArray[i])
						+ cL0;
			}
			else {
				double[][] matrixData = { 
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 4),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{4* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
								3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 4),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
									alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									1.0},
						{4* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
										3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
										2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0,
										0.0},
						{12* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
											6*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
											2.0,
											0.0,
											0.0},
									};

				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {
						cLmax,
						0,
						cLStar,
						cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
						0
						};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];
				e = solSystem[4];

				cLArray[i] = a * Math.pow(alphaArray[i], 4) + 
						b * Math.pow(alphaArray[i], 3) + 
						c* Math.pow(alphaArray[i], 2) +
						d * alphaArray[i]+
						e;
			}
		}

		return cLArray;
	}

//	public static double[] calculateCLvsAlphaHighLiftArrayNasaBlackwell(
//			LiftingSurface theLiftingSurface,
//			MyArray alphaArray, 
//			int nValue,
//			double cLalphaNew,
//			double deltaCL0Flap,
//			double deltaAlphaMaxFlap,
//			double cLMaxFlap,
//			double deltaClmaxSlat
//			)
//	{
//		double alphaActual = 0;
//		LSAerodynamicsManager theLsManager = theLiftingSurface.getAerodynamics();
//		double [] cLActualArray = new double[nValue];
//		double cLAlphaFlap = cLalphaNew*57.3; // need it in 1/rad
//
//		Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(theLiftingSurface));
//		double alphaStarClean = meanAirfoil.getAirfoilCreator().getAlphaEndLinearTrait().getEstimatedValue();
//
//		Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);
//
//		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLvsAlphaCurve = theLsManager.new CalcCLvsAlphaCurve();
//		LSAerodynamicsManager.CalcCLAtAlpha theCLCleanCalculator = theLsManager.new CalcCLAtAlpha();
//		double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurveValue(alphaStarCleanAmount);
//
//		double cL0Clean =  theCLCleanCalculator.nasaBlackwellCompleteCurveValue(Amount.valueOf(0.0, SI.RADIAN));
//		double cL0HighLift = cL0Clean + deltaCL0Flap;
//		double qValue = cL0HighLift;
//		double alphaStar = (cLStarClean - qValue)/cLAlphaFlap;
//		theLsManager.calcAlphaAndCLMax(meanAirfoil);
//		Amount<Angle> alphaMax = theLsManager.getAlphaMaxClean();	
//
//		double alphaMaxHighLift;
//		double deltaYPercent = AirfoilCalc.calculateDeltaYPercent(
//				meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
//				meanAirfoil.getAirfoilCreator().getFamily(),
//				theLiftingSurface.getAerodynamicDatabaseReader()
//				);
//
//		if(deltaClmaxSlat == 0)
//			alphaMaxHighLift = alphaMax.getEstimatedValue() + deltaAlphaMaxFlap/57.3;
//		else
//			alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLalphaNew) 
//			+ theLsManager.getAerodynamicDatabaseReader().getDAlphaVsLambdaLEVsDy(
//					theLiftingSurface
//					.getSweepLEEquivalent(false).to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
//					deltaYPercent);
//
//		alphaMaxHighLift = Amount.valueOf(alphaMaxHighLift, SI.RADIAN).getEstimatedValue();
//
//		double alphaStarFlap; 
//
//		if(deltaClmaxSlat == 0)
//			alphaStarFlap = (alphaStar + alphaStarClean)/2;
//		else
//			alphaStarFlap = alphaMaxHighLift-(alphaMax.to(SI.RADIAN).getEstimatedValue()-alphaStarClean);
//
//		double cLStarFlap = cLAlphaFlap * alphaStarFlap + qValue;	
//		for (int i=0; i<nValue; i++ ){
//			alphaActual = alphaArray.get(i);
//
//			if (alphaActual < alphaStarFlap ){ 
//				cLActualArray[i] = cLAlphaFlap * alphaActual + qValue;	
//			}
//			else{
//				double[][] matrixData = { {Math.pow(alphaMaxHighLift, 3), Math.pow(alphaMaxHighLift, 2)
//					, alphaMaxHighLift,1.0},
//						{3* Math.pow(alphaMaxHighLift, 2), 2*alphaMaxHighLift, 1.0, 0.0},
//						{3* Math.pow(alphaStarFlap, 2), 2*alphaStarFlap, 1.0, 0.0},
//						{Math.pow(alphaStarFlap, 3), Math.pow(alphaStarFlap, 2),alphaStarFlap,1.0}};
//				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
//
//
//				double [] vector = {cLMaxFlap, 0,cLAlphaFlap, cLStarFlap};
//
//				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);
//
//				double a = solSystem[0];
//				double b = solSystem[1];
//				double c = solSystem[2];
//				double d = solSystem[3];
//
//				cLActualArray[i] = a * Math.pow(alphaActual, 3) + 
//						b * Math.pow(alphaActual, 2) + 
//						c * alphaActual + d;
//			}
//
//		}
//		return cLActualArray;
//	}


//	// TO DO move here the cl wing body calculator
//	public static double[] calculateCLvsAlphaArrayWingBody(
//			LiftingSurface theLiftingSurface,
//			MyArray alphaArray,
//			int nValue,
//			boolean printResults
//			)
//	{
//		double[] xArray = {0.0, 0.0};
//		return xArray;
//	}

	/**
	 * 
	 *This method evaluates the CL vs Alpha array for given alpha array. In order to evaluate the curve
	 *for each angle of attack the load distribution using Nasa Blackwell method is evaluated. At 50 station spemi-span wise 
	 *is calculated the local lift coefficient. It's important to remember that Nasa Blackwell method is inviscid, so the
	 *obtained values of cl are non viscous. With these value of cl it's calculated the angle of attack in the linear 
	 *curve of cl vs alpha for the airfoils and the obtained value of alpha is used in order to evaluate the viscous cl of
	 *the airfoils. Starting from this new distribution of cl it's evaluated the viscous cL of the wing with an integral.
	 *
	 *@param alpha array in rad
	 *
	 *@author Manuela Ruocco
	 *
	 */
//	@SuppressWarnings("static-access")
//	public static double[] calculateCLArraymodifiedStallPath(MyArray alphaArray, LiftingSurface theLiftingSurface){
//
//
//		// VARIABLE DECLARATION
//		Amount<Angle> alphaActual;
//		double qValue, cLWingActual = 0;
//		double [] clNasaBlackwell = new double [alphaArray.size()];
//
//		List<Airfoil> airfoilList = new ArrayList<Airfoil>();
//
//		LSAerodynamicsManager theLSManager = theLiftingSurface.getAerodynamics();
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//
//		int nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
//		double [] yArray = MyArrayUtils.linspace(0., theLiftingSurface.getSpan().getEstimatedValue()/2, nPointSemiSpan);
//		double [] yArrayND = MyArrayUtils.linspace(0., 1, nPointSemiSpan);
//		double [] cLDistributionInviscid = new double [nPointSemiSpan];
//		double [] alphaLocalAirfoil = new double [nPointSemiSpan];
//		double [] clDisributionReal = new double [nPointSemiSpan];
//
//		double [] cLWingArray = new double [alphaArray.size()];
//
//
//		for (int j=0 ; j<nPointSemiSpan; j++){
//			airfoilList.add(j, new Airfoil(theLiftingSurface.calculateAirfoilAtY(theLiftingSurface, yArray[j])));
//
//			// iterations
//			for (int ii=0; ii<alphaArray.size(); ii++){
//				alphaActual = Amount.valueOf(alphaArray.get(ii),SI.RADIAN);
//
//				calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
//				clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().getClTotalDistribution().toArray();
//				clNasaBlackwell[clNasaBlackwell.length-1] = 0;
//
//				for (int i=0 ; i<nPointSemiSpan ;  i++){
//					cLDistributionInviscid[i] = clNasaBlackwell[i];
//					//			System.out.println( " cl local " + cLLocal);
//					qValue = MyMathUtils.getInterpolatedValue1DLinear(
//							MyArrayUtils.convertListOfAmountTodoubleArray(airfoilList.get(i).getAirfoilCreator().getAlphaForClCurve()),
//							MyArrayUtils.convertToDoublePrimitive(airfoilList.get(i).getAirfoilCreator().getClCurve()),
//							0.0
//							);
//					//			System.out.println(" qValue " + qValue );
//					alphaLocalAirfoil[i] = (cLDistributionInviscid[i]-qValue)/airfoilList.get(i).getAirfoilCreator().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
//					//			System.out.println(" alpha local airfoil " + alphaLocalAirfoil);
//					clDisributionReal[i] = MyMathUtils.getInterpolatedValue1DLinear(
//							MyArrayUtils.convertListOfAmountTodoubleArray(airfoilList.get(i).getAirfoilCreator().getAlphaForClCurve()),
//							MyArrayUtils.convertToDoublePrimitive(airfoilList.get(i).getAirfoilCreator().getClCurve()),
//							alphaLocalAirfoil[i]
//							); 
//				}
//				cLWingActual = MyMathUtils.integrate1DSimpsonSpline(yArrayND, clDisributionReal);
//				cLWingArray[ii] = cLWingActual;
//			}
//		}
//		return cLWingArray;
//	}

	@SuppressWarnings("unused")
	public static double calculateCLMax(
			double[] maximumLiftCoefficient, 
			double semispan, 
			double surface,
			double[] yStationsActual,
			double[] chordsVsYActual,
			double[] xLEvsYActual,
			double[] dihedral,
			double[] twist,
			double[] alpha0l,
			double vortexSemiSpanToSemiSpanRatio,
			double alpha,
			double mach,
			double altitude){

		// parameters definition

		double cLMax = 0;
		Amount<Angle> alphaAtCLMaX = null;

		int _nPointsSemispanWise = (int)(1./(2*vortexSemiSpanToSemiSpanRatio));
		int stepsToStallCounter = 0;
		double accuracy =0.0001;
		double diffCL = 0;
		double diffCLappOld = 0;
		double diffCLapp = 0;
		double deltaAlpha;
		double alphaNew = 0;
		double alphaOld;
		boolean _findStall = false;
		Amount<Angle> alphaNewAmount;
		boolean found = false;

		Amount<Angle> alphaStart = Amount.valueOf(toRadians(-2.), SI.RADIAN);
		Amount<Angle> alphaEnd = Amount.valueOf(toRadians(32.), SI.RADIAN);
		int _numberOfAlpha = 15; 
		MyArray alphaArray = new MyArray();
		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));

		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
				semispan, 
				surface,
				yStationsActual,
				chordsVsYActual,
				xLEvsYActual,
				dihedral,
				twist,
				alpha0l,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				mach,
				altitude);

		for (int j=0; j < _numberOfAlpha; j++) {
			if (found == false) {
				Amount<Angle> alphaInputAngle = Amount.valueOf(alphaArray.get(j), SI.RADIAN);
				MyArray clDistributionArray = new MyArray();
				
				theNasaBlackwellCalculator.calculate(alphaInputAngle);
				clDistributionArray = theNasaBlackwellCalculator.getClTotalDistribution();

				for(int i =0; i< _nPointsSemispanWise; i++) {
					if (found == false 
							&& clDistributionArray.get(i)
							> maximumLiftCoefficient[i] ) {	

						for (int k =i; k< _nPointsSemispanWise; k++) {
							diffCLapp = ( clDistributionArray.get(k) -  maximumLiftCoefficient[k]);
							diffCL = Math.max(diffCLapp, diffCLappOld);
							diffCLappOld = diffCL;
						}
						if( Math.abs(diffCL) < accuracy){
							cLMax = theNasaBlackwellCalculator.get_cLEvaluated();
							found = true;
							alphaAtCLMaX = alphaArray.getAsAmount(j); 
						}

						else{
							deltaAlpha = alphaArray.getAsAmount(j).getEstimatedValue()
									- alphaArray.getAsAmount(j-1).getEstimatedValue();
							alphaNew = alphaArray.getAsAmount(j).getEstimatedValue() - (deltaAlpha/2);
							alphaOld = alphaArray.getAsAmount(j).getEstimatedValue(); 
							alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
							diffCLappOld = 0;
							while ( diffCL > accuracy){
								theNasaBlackwellCalculator.calculate(alphaNewAmount);
								clDistributionArray = theNasaBlackwellCalculator.getClTotalDistribution();
								diffCL = 0;

								for (int m =0; m< _nPointsSemispanWise; m++) {
									diffCLapp = (clDistributionArray.get(m) -  maximumLiftCoefficient[m]);

									if ( diffCLapp > 0 ){
										diffCL = Math.max(diffCLapp,diffCLappOld);
										diffCLappOld = diffCL;
									}

								}
								deltaAlpha = Math.abs(alphaOld - alphaNew);
								alphaOld = alphaNew;
								if (diffCL == 0 ){
									alphaNew = alphaOld + (deltaAlpha/2);
									alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
									diffCL = 1;
									diffCLappOld = 0;
								}
								else { 
									if(deltaAlpha > 0.005){
										alphaNew = alphaOld - (deltaAlpha/2);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;
									}
									else {
										alphaNew = alphaOld - (deltaAlpha);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;}}

							}
							found = true;
						}
						alphaAtCLMaX = Amount.valueOf(alphaNew, SI.RADIAN);
				}
			}
		}
		}
			theNasaBlackwellCalculator.calculate(alphaAtCLMaX);
			double cLMaxActual = theNasaBlackwellCalculator.getCLCurrent();
			return cLMaxActual;
		}
	
	public static double calculateCLMaxHIGHLIFT(
			double[] maximumLiftCoefficient, 
			double semispan, 
			double surface,
			double[] yStationsActual,
			double[] chordsVsYActual,
			double[] xLEvsYActual,
			double[] dihedral,
			double[] twist,
			double[] alpha0l,
			double vortexSemiSpanToSemiSpanRatio,
			double alpha,
			double mach,
			double altitude,
			double[] chordsOld){

		// parameters definition

		double cLMax = 0;
		Amount<Angle> alphaAtCLMaX = null;

		int _nPointsSemispanWise = (int)(1./(2*vortexSemiSpanToSemiSpanRatio));
		int stepsToStallCounter = 0;
		double accuracy =0.0001;
		double diffCL = 0;
		double diffCLappOld = 0;
		double diffCLapp = 0;
		double deltaAlpha;
		double alphaNew = 0;
		double alphaOld;
		boolean _findStall = false;
		Amount<Angle> alphaNewAmount;
		boolean found = false;

		Amount<Angle> alphaStart = Amount.valueOf(toRadians(-2.), SI.RADIAN);
		Amount<Angle> alphaEnd = Amount.valueOf(toRadians(32.), SI.RADIAN);
		int _numberOfAlpha = 15; 
		MyArray alphaArray = new MyArray();
		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));

		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
				semispan, 
				surface,
				yStationsActual,
				chordsVsYActual,
				xLEvsYActual,
				dihedral,
				twist,
				alpha0l,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				mach,
				altitude);

		for (int j=0; j < _numberOfAlpha; j++) {
			if (found == false) {
				Amount<Angle> alphaInputAngle = Amount.valueOf(alphaArray.get(j), SI.RADIAN);
				List<Double> clDistributionArray = new ArrayList<>();
				
				theNasaBlackwellCalculator.calculate(alphaInputAngle);
				
				List<Double> clDistributionHighLift = MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								theNasaBlackwellCalculator.get_ccLDistribution().toArray()));
				for(int jj =0; jj<clDistributionHighLift.size(); jj++) {
					clDistributionHighLift.set(jj, clDistributionHighLift.get(jj)/chordsOld[jj]);
				}
				
				clDistributionArray = clDistributionHighLift;

				for(int i =0; i< _nPointsSemispanWise; i++) {
					if (found == false 
							&& clDistributionArray.get(i)
							> maximumLiftCoefficient[i] ) {	

						for (int k =i; k< _nPointsSemispanWise; k++) {
							diffCLapp = ( clDistributionArray.get(k) -  maximumLiftCoefficient[k]);
							diffCL = Math.max(diffCLapp, diffCLappOld);
							diffCLappOld = diffCL;
						}
						if( Math.abs(diffCL) < accuracy){
							cLMax = theNasaBlackwellCalculator.get_cLEvaluated();
							found = true;
							alphaAtCLMaX = alphaArray.getAsAmount(j); 
						}

						else{
							deltaAlpha = alphaArray.getAsAmount(j).getEstimatedValue()
									- alphaArray.getAsAmount(j-1).getEstimatedValue();
							alphaNew = alphaArray.getAsAmount(j).getEstimatedValue() - (deltaAlpha/2);
							alphaOld = alphaArray.getAsAmount(j).getEstimatedValue(); 
							alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
							diffCLappOld = 0;
							while ( diffCL > accuracy){
								theNasaBlackwellCalculator.calculate(alphaNewAmount);		
								clDistributionHighLift = new ArrayList<>();
								clDistributionHighLift = MyArrayUtils.convertDoubleArrayToListDouble(
										MyArrayUtils.convertFromDoubleToPrimitive(
												theNasaBlackwellCalculator.get_ccLDistribution().toArray()));
								for(int jj =0; jj<clDistributionHighLift.size(); jj++) {
									clDistributionHighLift.set(jj, clDistributionHighLift.get(jj)/chordsOld[jj]);
								}
								
								clDistributionArray = clDistributionHighLift;
								diffCL = 0;

								for (int m =0; m< _nPointsSemispanWise; m++) {
									diffCLapp = (clDistributionArray.get(m) -  maximumLiftCoefficient[m]);

									if ( diffCLapp > 0 ){
										diffCL = Math.max(diffCLapp,diffCLappOld);
										diffCLappOld = diffCL;
									}

								}
								deltaAlpha = Math.abs(alphaOld - alphaNew);
								alphaOld = alphaNew;
								if (diffCL == 0 ){
									alphaNew = alphaOld + (deltaAlpha/2);
									alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
									diffCL = 1;
									diffCLappOld = 0;
								}
								else { 
									if(deltaAlpha > 0.005){
										alphaNew = alphaOld - (deltaAlpha/2);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;
									}
									else {
										alphaNew = alphaOld - (deltaAlpha);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;}}

							}
							found = true;
						}
						alphaAtCLMaX = Amount.valueOf(alphaNew, SI.RADIAN);
				}
			}
		}
		}
			theNasaBlackwellCalculator.calculate(alphaAtCLMaX);
			double cLMaxActual = theNasaBlackwellCalculator.getCLCurrent();
			return cLMaxActual;
		}

	/*********************************************************************************************
	 * This method calculate high lift devices effects on lift coefficient curve of the 
	 * airfoil and wing throughout semi-empirical formulas; in particular DeltaCl0, DeltaCL0
	 * DeltaCLmax and DeltaClmax are calculated for flaps when only DeltaClmax and DeltaCLmax
	 * are calculated for slats. Moreover an evaluation of new CLapha slope and CD are performed
	 * for the wing. 
	 * 
	 * @author Vittorio Trifari
	 */
	public static Map<HighLiftDeviceEffectEnum, Object> calculateHighLiftDevicesEffects(
			AerodynamicDatabaseReader aeroDatabaseReader,
			HighLiftDatabaseReader highLiftDatabaseReader,
			List<SymmetricFlapCreator> flapList,
			List<SlatCreator> slatList,
			List<Double> etaBreakPoints,
			List<Amount<?>> clAlphaBreakPoints,
			List<Double> clZeroBreakPoints,
			List<Double> maxThicknessRatioBreakPoints,
			List<Amount<Length>> radiusLeadingEdgeBreakPoints,
			List<Amount<Length>> chordBreakPoints,
			List<Amount<Angle>> flapDeflections,
			List<Amount<Angle>> slatDeflections,
			Amount<Angle> currentAlpha,
			Amount<?> cLAlphaClean,
			Amount<Angle> sweepQuarterChordEquivalent,
			Double taperRatioEquivalent,
			Amount<Length> rootChordEquivalent,
			Double aspectRatio,
			Amount<Area> surface,
			Double meanAirfoilThickness,
			AirfoilFamilyEnum meanAirfoilFamily,
			Double cLZeroClean,
			Double cLMaxClean,
			Amount<Angle> alphaStarClean,
			Amount<Angle> alphaStallClean
			) {

		Map<HighLiftDeviceEffectEnum, Object> resultsMap = new HashMap<>();
		
		Amount<Length> span = 
				Amount.valueOf(
				Math.sqrt(
						surface.to(SI.SQUARE_METRE)
						.times(aspectRatio)
						.getEstimatedValue()
						),
				SI.METER
				);
		
		if(flapDeflections.size() != flapList.size()) {
			System.err.println("ERROR THE FLAP DEFLECTIONS MUST BE EQUAL TO THE NUMBER OF FLAPS!");
			return null;
		}
		
		if(!(slatList.isEmpty() || slatList == null)) 
				if(slatDeflections.size() != slatList.size()) {
			System.err.println("ERROR THE SLAT DEFLECTIONS MUST BE EQUAL TO THE NUMBER OF SLATS!");
			return null;
		}
		
		//--------------------------------------------
		// Managing flaps types:
		List<Double> flapTypeIndex = new ArrayList<Double>();
		List<Double> deltaFlapRef = new ArrayList<Double>();

		for(int i=0; i<flapList.size(); i++) {
			if(flapList.get(i).getType() == FlapTypeEnum.SINGLE_SLOTTED) {
				flapTypeIndex.add(1.0);
				deltaFlapRef.add(45.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapTypeIndex.add(2.0);
				deltaFlapRef.add(50.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.PLAIN) {
				flapTypeIndex.add(3.0);
				deltaFlapRef.add(60.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.FOWLER) {
				flapTypeIndex.add(4.0);
				deltaFlapRef.add(40.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.TRIPLE_SLOTTED) {
				flapTypeIndex.add(5.0);
				deltaFlapRef.add(50.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.OPTIMIZED_FOWLER) {
				flapTypeIndex.add(6.0);
				deltaFlapRef.add(40.0);
			}
		}
		//--------------------------------------------
		// Creating lists of flaps geometric parameters:
		List<Double> etaInFlap = new ArrayList<Double>();
		List<Double> etaOutFlap = new ArrayList<Double>();
		List<Double> cfc = new ArrayList<Double>();
		
		List<Double> etaInSlat = new ArrayList<Double>();
		List<Double> etaOutSlat = new ArrayList<Double>();
		List<Double> csc = new ArrayList<Double>();
		List<Double> cExtcSlat = new ArrayList<Double>();

		for(int i=0; i<flapList.size(); i++) {
			etaInFlap.add(flapList.get(i).getInnerStationSpanwisePosition());
			etaOutFlap.add(flapList.get(i).getOuterStationSpanwisePosition());
			cfc.add(flapList.get(i).getMeanChordRatio());
		}
		if(!slatList.isEmpty()) {
			for(int i=0; i<slatList.size(); i++) {
				etaInSlat.add(slatList.get(i).getInnerStationSpanwisePosition());
				etaOutSlat.add(slatList.get(i).getOuterStationSpanwisePosition());
				csc.add(slatList.get(i).getMeanChordRatio());
				cExtcSlat.add(slatList.get(i).getExtensionRatio());
			}
		}
		
		//--------------------------------------------
		// Creating arrays of the required parameters to be interpolated:
		double [] clAlphaMeanFlap = new double [flapList.size()];
		double [] clZeroMeanFlap = new double [flapList.size()];
		double [] maxTicknessMeanFlap = new double [flapList.size()];
		double [] maxTicknessMeanSlat = new double [slatList.size()];
		double [] maxTicknessFlapStations = new double [2*flapList.size()];
		double [] clAlphaFlapStations = new double [2*flapList.size()];
		double [] clZeroFlapStations = new double [2*flapList.size()];
		double [] leRadiusMeanSlat = new double [slatList.size()];
		double [] chordMeanSlat = new double [slatList.size()];
		double [] leadingEdgeRadiusSlatStations = new double [2*slatList.size()];
		double [] maxTicknessSlatStations = new double [2*slatList.size()];
		double [] chordSlatStations = new double [2*slatList.size()];
		
		double [] influenceFactor = new double [2];
		
		for ( int i=0; i< flapList.size(); i++){
			int kk = i*2;
			
			clAlphaFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertListOfAmountodoubleArray(
							clAlphaBreakPoints.stream().map(x -> x.to(NonSI.DEGREE_ANGLE.inverse())).collect(Collectors.toList())
							),
					etaOutFlap.get(i));
			
			clAlphaFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertListOfAmountodoubleArray(
							clAlphaBreakPoints.stream().map(x -> x.to(NonSI.DEGREE_ANGLE.inverse())).collect(Collectors.toList())
							),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertToDoublePrimitive(
							clZeroBreakPoints
							),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertToDoublePrimitive(
							clZeroBreakPoints
							),
					etaOutFlap.get(i));
			
			maxTicknessFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertToDoublePrimitive(
							maxThicknessRatioBreakPoints
							),
					etaInFlap.get(i));
			
			maxTicknessFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							etaBreakPoints
							),
					MyArrayUtils.convertToDoublePrimitive(
							maxThicknessRatioBreakPoints
							),
					etaOutFlap.get(i));
			
			try {
				influenceFactor = LiftingSurface.calculateInfluenceFactorsMeanAirfoilFlap(
						etaInFlap.get(i),
						etaOutFlap.get(i),
						etaBreakPoints,
						chordBreakPoints,
						span.divide(2)
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			clAlphaMeanFlap[i] = clAlphaFlapStations[kk] * influenceFactor[0] + clAlphaFlapStations[kk+1]*influenceFactor[1];
			clZeroMeanFlap[i] = clZeroFlapStations[kk] * influenceFactor[0] + clZeroFlapStations[kk+1]*influenceFactor[1];
			maxTicknessMeanFlap[i] = maxTicknessFlapStations[kk]* influenceFactor[0] + maxTicknessFlapStations[kk+1]*influenceFactor[1];
		}
		
		if(!slatList.isEmpty()) 
			for ( int i=0; i< slatList.size(); i++){
				int kk = i*2;

				leadingEdgeRadiusSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								radiusLeadingEdgeBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
								),
						etaOutSlat.get(i));

				leadingEdgeRadiusSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								radiusLeadingEdgeBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
								),
						etaInSlat.get(i));

				chordSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
								),
						etaInSlat.get(i));

				chordSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
								),
						etaOutSlat.get(i));


				maxTicknessSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertToDoublePrimitive(
								maxThicknessRatioBreakPoints
								),
						etaInSlat.get(i));

				maxTicknessSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								etaBreakPoints
								),
						MyArrayUtils.convertToDoublePrimitive(
								maxThicknessRatioBreakPoints
								),
						etaOutSlat.get(i));

				try {
					influenceFactor = LiftingSurface.calculateInfluenceFactorsMeanAirfoilFlap(
							etaInSlat.get(i),
							etaOutSlat.get(i),
							etaBreakPoints,
							chordBreakPoints,
							span.divide(2)
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				leRadiusMeanSlat[i] = leadingEdgeRadiusSlatStations[kk] * influenceFactor[0] + leadingEdgeRadiusSlatStations[kk+1]*influenceFactor[1];
				chordMeanSlat[i] = chordSlatStations[kk] * influenceFactor[0] + chordSlatStations[kk+1]*influenceFactor[1];
				maxTicknessMeanSlat[i] = maxTicknessSlatStations[kk] * influenceFactor[0] + maxTicknessSlatStations[kk+1]*influenceFactor[1];

			}

		//---------------------------------------------
		// deltaCl0 (flap)
		List<Double> thetaF = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) 
			thetaF.add(Math.acos((2*cfc.get(i))-1));

		List<Double> alphaDelta = new ArrayList<Double>();
		for(int i=0; i<thetaF.size(); i++)
			alphaDelta.add(1-((thetaF.get(i)-Math.sin(thetaF.get(i)))/Math.PI));

		List<Double> etaDeltaFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlapPlain(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								cfc.get(i)
								)
						);
			else
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								flapTypeIndex.get(i))
						);
		}

		List<Double> deltaCl0First = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0First.add(
					alphaDelta.get(i).doubleValue()
					*etaDeltaFlap.get(i).doubleValue()
					*flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE)
					*(clAlphaMeanFlap[i])
					);

		List<Double> deltaCCfFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) != 6.0)
				deltaCCfFlap.add(
						highLiftDatabaseReader
						.getDeltaCCfVsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								flapTypeIndex.get(i)
								)
						);
			else
				deltaCCfFlap.add(
						highLiftDatabaseReader
						.getDeltaCCfVsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								4.0 // Fowler
								)
						);
		

		List<Double> cFirstCFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*cfc.get(i).doubleValue()));

		List<Double> deltaCl0FlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0FlapList.add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(clZeroMeanFlap[i]*(cFirstCFlap.get(i).doubleValue()-1))
					);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP_LIST, 
				deltaCl0FlapList
				);
		
		double deltaCl0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0Flap += deltaCl0FlapList.get(i);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP,
				deltaCl0Flap
				);
		
		//---------------------------------------------------------------
		// deltaClmax (flap)
		List<Double> deltaClmaxBase = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) != 6.0)
				deltaClmaxBase.add(
						highLiftDatabaseReader
						.getDeltaCLmaxBaseVsTc(
								maxTicknessMeanFlap[i],
								flapTypeIndex.get(i)
								)
						);
			else
				deltaClmaxBase.add(
						highLiftDatabaseReader
						.getDeltaCLmaxBaseVsTc(
								maxTicknessMeanFlap[i],
								4.0 // FOWLER
								)
						);

		List<Double> k1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if (cfc.get(i) <= 0.30)
				if(flapTypeIndex.get(i) != 6.0)
					k1.add(highLiftDatabaseReader
							.getK1vsFlapChordRatio(cfc.get(i), flapTypeIndex.get(i))
							);
				else
					k1.add(highLiftDatabaseReader
							.getK1vsFlapChordRatio(cfc.get(i), 4)  // FOWLER
							);
			else if ((cfc.get(i) > 0.30) && ((flapTypeIndex.get(i) == 2) || (flapTypeIndex.get(i) == 4) || (flapTypeIndex.get(i) == 5) || (flapTypeIndex.get(i) == 6)))
				k1.add(0.04*(cfc.get(i)*100));
			else if ((cfc.get(i) > 0.30) && ((flapTypeIndex.get(i) == 1) || (flapTypeIndex.get(i) == 3) ))
				k1.add((608.31*Math.pow(cfc.get(i), 5))
						-(626.15*Math.pow(cfc.get(i), 4))
						+(263.4*Math.pow(cfc.get(i), 3))
						-(62.946*Math.pow(cfc.get(i), 2))
						+(10.638*cfc.get(i))
						+0.0064
						);
		
		List<Double> k2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) != 6.0)
				k2.add(highLiftDatabaseReader
						.getK2VsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								flapTypeIndex.get(i)
								)
						);
			else
				k2.add(highLiftDatabaseReader
						.getK2VsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								4.0 // FOWLER
								)
						);

		List<Double> k3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) != 6.0)
				k3.add(highLiftDatabaseReader
						.getK3VsDfDfRef(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								deltaFlapRef.get(i),
								flapTypeIndex.get(i)
								)
						);
			else
				k3.add(1.0);

		List<Double> deltaClmaxFlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxFlapList.add(
					k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmaxBase.get(i).doubleValue()
					);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP_LIST, 
				deltaClmaxFlapList
				);
		
		double deltaClmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxFlap += deltaClmaxFlapList.get(i);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP,
				deltaClmaxFlap
				);
		
		double deltaCLmaxSlat = 0.0;
		if(!slatDeflections.isEmpty()) {
			//---------------------------------------------------------------
			// deltaClmax (slat)
			List<Double> dCldDelta = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				dCldDelta.add(highLiftDatabaseReader
						.getDCldDeltaVsCsC(csc.get(i))
						);

			List<Double> etaMaxSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				etaMaxSlat.add(highLiftDatabaseReader
						.getEtaMaxVsLEradiusTicknessRatio(
								leRadiusMeanSlat[i]/(chordMeanSlat[i]),
								maxTicknessMeanSlat[i]
										)
						);

			List<Double> etaDeltaSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				etaDeltaSlat.add(highLiftDatabaseReader
						.getEtaDeltaVsDeltaSlat(slatDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);

			List<Double> deltaClmaxSlatList = new ArrayList<>();
			for(int i=0; i<slatDeflections.size(); i++)
				deltaClmaxSlatList.add(
						dCldDelta.get(i).doubleValue()
						*etaMaxSlat.get(i).doubleValue()
						*etaDeltaSlat.get(i).doubleValue()
						*slatDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE)
						*cExtcSlat.get(i).doubleValue()
						);
			resultsMap.put(
					HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST,
					deltaClmaxSlatList
					);
			
			double deltaClmaxSlat = 0.0;
			for(int i=0; i<slatDeflections.size(); i++)
				deltaClmaxSlat += deltaClmaxSlatList.get(i);
			resultsMap.put(
					HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT, 
					deltaClmaxSlat
					);
			
			//---------------------------------------------------------------
			// deltaCLmax (slat)
			List<Double> kLambdaSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				kLambdaSlat.add(
						Math.pow(Math.cos(sweepQuarterChordEquivalent.doubleValue(SI.RADIAN)),0.75)
						*(1-(0.08*Math.pow(Math.cos(sweepQuarterChordEquivalent.doubleValue(SI.RADIAN)), 2)))
						);

			List<Double> slatSurface = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				slatSurface.add(
						Math.abs(span.doubleValue(SI.METER)
								/2*rootChordEquivalent.doubleValue(SI.METER)
								*(2-(1-taperRatioEquivalent)*(etaInSlat.get(i)+etaOutSlat.get(i)))
								*(etaOutSlat.get(i)-etaInSlat.get(i))
								)
						);

			List<Double> deltaCLmaxSlatList = new ArrayList<>();
			for(int i=0; i<slatDeflections.size(); i++)
				deltaCLmaxSlatList.add(
						deltaClmaxSlatList.get(i)
						*(slatSurface.get(i)/surface.doubleValue(SI.SQUARE_METRE))
						*kLambdaSlat.get(i));
			resultsMap.put(
					HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST,
					deltaCLmaxSlatList
					);

			for(int i=0; i<slatDeflections.size(); i++)
				deltaCLmaxSlat += deltaCLmaxSlatList.get(i);
			resultsMap.put(
					HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT,
					deltaCLmaxSlat
					);
		}
		else {
			resultsMap.put(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST, null);
			resultsMap.put(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST, null);
			resultsMap.put(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT, null);
			resultsMap.put(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT, null);
		}
			
		//---------------------------------------------------------------
		// deltaCL0 (flap)
		List<Double> kc = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kc.add(highLiftDatabaseReader
					.getKcVsAR(
							aspectRatio,
							alphaDelta.get(i))	
					);

		List<Double> kb = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kb.add(highLiftDatabaseReader
					.getKbVsFlapSpanRatio(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							taperRatioEquivalent
							)	
					);

		List<Double> deltaCL0FlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCL0FlapList.add(
					kb.get(i).doubleValue()
					*kc.get(i).doubleValue()
					*deltaCl0FlapList.get(i)
					*((cLAlphaClean.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
							/(clAlphaMeanFlap[i]))
					);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CL0_FLAP_LIST,
				deltaCL0FlapList
				);

		double deltaCL0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCL0Flap += deltaCL0FlapList.get(i);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CL0_FLAP,
				deltaCL0Flap
				);
		
		//---------------------------------------------------------------
		// deltaCLmax (flap)
		List<Double> flapSurface = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			flapSurface.add(
					Math.abs(
							span.doubleValue(SI.METER)							
							/2*rootChordEquivalent.doubleValue(SI.METER)
							*(2-((1-taperRatioEquivalent)*(etaInFlap.get(i)+etaOutFlap.get(i))))
							*(etaOutFlap.get(i)-etaInFlap.get(i))
							)
					);

		List<Double> kLambdaFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kLambdaFlap.add(
					Math.pow(Math.cos(sweepQuarterChordEquivalent.doubleValue(SI.RADIAN)),0.75)
					*(1-(0.08*Math.pow(Math.cos(sweepQuarterChordEquivalent.doubleValue(SI.RADIAN)), 2)))
					);

		List<Double> deltaCLmaxFlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlapList.add(
					deltaClmaxFlapList.get(i)
					*(flapSurface.get(i)/surface.doubleValue(SI.SQUARE_METRE))
					*kLambdaFlap.get(i)
					);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP_LIST, 
				deltaCLmaxFlapList
				);
		
		double deltaCLmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlap += deltaCLmaxFlapList.get(i).doubleValue();
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP,
				deltaCLmaxFlap
				);

		//---------------------------------------------------------------
		// new CLalpha
		List<Double> cLalphaFlapList = new ArrayList<Double>();
		List<Double> swf = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			cLalphaFlapList.add(
					cLAlphaClean.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
					*(1+((deltaCL0FlapList.get(i)/
							deltaCl0FlapList.get(i))
							*(cFirstCFlap.get(i)*(1-((cfc.get(i))*(1/cFirstCFlap.get(i))
									*Math.pow(Math.sin(flapDeflections.get(i).doubleValue(SI.RADIAN)), 2)))-1))));
			swf.add(flapSurface.get(i)/surface.doubleValue(SI.SQUARE_METRE));
		}
		resultsMap.put(HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT_LIST, cLalphaFlapList);

		double swfTot = 0;
		for(int i=0; i<swf.size(); i++)
			swfTot += swf.get(i);

		double cLAlphaFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			cLAlphaFlap += cLalphaFlapList.get(i)*swf.get(i);

		cLAlphaFlap /= swfTot;
		resultsMap.put(
				HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT,
				Amount.valueOf(
						cLAlphaFlap,
						NonSI.DEGREE_ANGLE.inverse()
						)
				);
		
		//---------------------------------------------------------------
		// deltaCD
		List<Double> delta1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta1.add(
						highLiftDatabaseReader
						.getDelta1VsCfCPlain(
								cfc.get(i),
								maxTicknessMeanFlap[i]
								)
						);
			else
				delta1.add(
						highLiftDatabaseReader
						.getDelta1VsCfCSlotted(
								cfc.get(i),
								maxTicknessMeanFlap[i]
								)
						);
		}

		List<Double> delta2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta2.add(
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapPlain(flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);
			else
				delta2.add(
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapSlotted(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								maxTicknessMeanFlap[i]
								)
						);
		}

		List<Double> delta3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			delta3.add(
					highLiftDatabaseReader
					.getDelta3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							taperRatioEquivalent
							)
					);
		}

		List<Double> deltaCDList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			deltaCDList.add(
					delta1.get(i)*
					delta2.get(i)*
					delta3.get(i)
					);
		}
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CD_LIST,
				deltaCDList
				);
		
		
		double deltaCD = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCD += deltaCDList.get(i).doubleValue();
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CD,
				deltaCD
				);

		//---------------------------------------------------------------
		// deltaCM_c/4
		List<Double> mu1 = new ArrayList<Double>();
		for (int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) == 3.0)
				mu1.add(
						highLiftDatabaseReader
						.getMu1VsCfCFirstPlain(
								(cfc.get(i))*(1/cFirstCFlap.get(i)),
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								)
						);
			else
				mu1.add(
						highLiftDatabaseReader
						.getMu1VsCfCFirstSlottedFowler((cfc.get(i))*(1/cFirstCFlap.get(i)))
						);

		List<Double> mu2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu2.add(
					highLiftDatabaseReader
					.getMu2VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							taperRatioEquivalent
							)
					);

		List<Double> mu3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu3.add(
					highLiftDatabaseReader
					.getMu3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							taperRatioEquivalent
							)
					);
		
		double deltaYPercent = aeroDatabaseReader
				.getDeltaYvsThickness(
						meanAirfoilThickness,
						meanAirfoilFamily
						);
		
		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						sweepQuarterChordEquivalent.doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);
		
		Double cLmaxHighLift = cLMaxClean + deltaCLmaxFlap + deltaCLmaxSlat;
		Double cLZeroHighLift = cLZeroClean + deltaCL0Flap;
		
		Amount<Angle> alphaStallHighLift = Amount.valueOf(
				((cLmaxHighLift	- cLZeroHighLift)
						/ cLAlphaFlap)
				+ deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE);
		
		Double[] alphaArrayHighLift = MyArrayUtils.linspaceDouble(
				-(cLZeroHighLift/cLAlphaFlap) - 2,
				alphaStallClean.doubleValue(NonSI.DEGREE_ANGLE) + deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE) + 3,
				40
				);
		
		// evaluating CL of the high lift 3D curve ...
		Double[] liftCurve = LiftCalc.calculateCLvsAlphaArray(
				cLZeroHighLift,
				cLmaxHighLift,
				alphaStarClean,
				alphaStallHighLift,
				Amount.valueOf(cLAlphaFlap, NonSI.DEGREE_ANGLE.inverse()),
				alphaArrayHighLift
				);
		double currentLiftCoefficient = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(alphaArrayHighLift),
				MyArrayUtils.convertToDoublePrimitive(liftCurve),
				currentAlpha.doubleValue(NonSI.DEGREE_ANGLE)
				);
		
		List<Double> deltaCMc4List = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMc4List.add(
					(mu2.get(i)*(-(mu1.get(i)
							*deltaClmaxFlapList.get(i)
							*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
									*((cFirstCFlap.get(i))-1)
									*(currentLiftCoefficient + 
											(deltaClmaxFlapList.get(i)
											*(1-(flapSurface.get(i)
													/surface.doubleValue(SI.SQUARE_METRE)))))
									*(1/8)))) + (0.7*(aspectRatio
											/(1+(aspectRatio/2)))
											*mu3.get(i)
											*deltaClmaxFlapList.get(i)
											*Math.tan(sweepQuarterChordEquivalent.doubleValue(SI.RADIAN)))
					);
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CM_c4_LIST,
				deltaCMc4List
				);

		double deltaCMC4 = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMC4 += deltaCMc4List.get(i).doubleValue();
		resultsMap.put(
				HighLiftDeviceEffectEnum.DELTA_CM_c4,
				deltaCMC4
				);
		
		return resultsMap;
	}
	
	
	public static double calculateTauIndexElevator(
			double chordRatioElevator,
			double aspectRatioHorizontalTail,
			HighLiftDatabaseReader highLiftDatabaseReader,
			AerodynamicDatabaseReader aeroDatabaseReader,
			Amount<Angle> elevatorDeflection
			)
	{
		double deflectionAngleDeg;

		if(elevatorDeflection.doubleValue(NonSI.DEGREE_ANGLE)<0){
			deflectionAngleDeg = -elevatorDeflection.doubleValue(NonSI.DEGREE_ANGLE);
			}

		else{
			deflectionAngleDeg = elevatorDeflection.doubleValue(NonSI.DEGREE_ANGLE);
			}


		double etaDelta = highLiftDatabaseReader
				.getEtaDeltaVsDeltaFlapPlain(deflectionAngleDeg, chordRatioElevator);
		
		double deltaAlpha2D = aeroDatabaseReader
				.getD_Alpha_d_Delta_2d_VS_cf_c(chordRatioElevator);


		double deltaAlpha2D3D = aeroDatabaseReader
				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
						aspectRatioHorizontalTail,
						deltaAlpha2D
						);

		double tauIndex = deltaAlpha2D3D * deltaAlpha2D * etaDelta;


		return tauIndex;
	}
	
	// returns the cl alpha in the same unit of cl alpha wing
	
	public static Amount<?> calculateCLAlphaFuselage(
			Amount<?> cLAlphaWing, 
			Amount<Length> wingSpan,
			Amount<Length> equivalentDiameterAtWingStation
			){
		
		//Sforza p64
		double d = equivalentDiameterAtWingStation.doubleValue(SI.METER);
		double b = wingSpan.doubleValue(SI.METER);
		Amount<?> cLAlphaFuselage = Amount.valueOf(
				(1.0+((1/4.0)*(d/b))-((1/40.0)*Math.pow((d/b), 2)))
				*cLAlphaWing.to(SI.RADIAN.inverse()).getEstimatedValue(),
				SI.RADIAN.inverse());
	
		return cLAlphaFuselage;
	}
	
	public static double [] calculateNasaBlackwellDistributionFromAirfoil (
			Amount<Angle> alphaActual,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> alphaReferenceOfCdMatrix,// references Cl of the list of list airfoilCdMatrix
			List<Double> clZeroDistribution,
			List<Double> clAlphaDegDistribution,
			List<Amount<Length>> yDimensionalDistribution
			) 
			{
		
		int numberOfPointSemiSpanWise = clZeroDistribution.size();
		double [] clDistributionAtAlpha ;
		double [] alphaDistribution = new double[numberOfPointSemiSpanWise];
		double [] clDistributionAtAlphaModified = new double[numberOfPointSemiSpanWise];
		
		double minAlpha = MyArrayUtils.getMin(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(alphaReferenceOfCdMatrix)));
		
		// Classical nasa blackwell distribution
		theNasaBlackwellCalculator.calculate(alphaActual);
		clDistributionAtAlpha = theNasaBlackwellCalculator.getClTotalDistribution().toArray();

		for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
			alphaDistribution [ii] = (clDistributionAtAlpha[ii] - clZeroDistribution.get(ii))/
					clAlphaDegDistribution.get(ii);

			if(alphaDistribution [ii] < minAlpha){
				clDistributionAtAlphaModified[ii] = 
						clAlphaDegDistribution.get(ii)*
						alphaDistribution [ii] + 
						clZeroDistribution.get(ii);
			}
			
			else{
				clDistributionAtAlphaModified[ii] = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(alphaReferenceOfCdMatrix),
					MyArrayUtils.convertToDoublePrimitive(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									airfoilClMatrix.get(ii))),
					alphaDistribution[ii]
					);
			}
			
		}
		return clDistributionAtAlphaModified;
		
			}
	
	public static double[] calculate3DCLfromNasaBlacwellModified (
			List<Amount<Angle>> alphasArray,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> alphaReferenceOfCdMatrix,// references Cl of the list of list airfoilCdMatrix
			List<Amount<Length>> chordDistribution,
			Amount<Area> surface,
			List<Double> clZeroDistribution,
			List<Double> clAlphaDegDistribution,
			List<Amount<Length>> yDimensionalDistribution
			) 
			{
		
		double[] nasaBlackwellModifiedCLCurve = new double [alphasArray.size()] ;
		double[] clDistributionActual ;
		double[] cCl = new double [chordDistribution.size()] ;
		
		for (int i=0; i<alphasArray.size(); i++){

			clDistributionActual = 	
			calculateNasaBlackwellDistributionFromAirfoil(
					alphasArray.get(i),
					theNasaBlackwellCalculator,
					airfoilClMatrix, 
					alphaReferenceOfCdMatrix,
					clZeroDistribution,
					clAlphaDegDistribution, 
					yDimensionalDistribution
					);
			for(int ii=0; ii<chordDistribution.size(); ii++){
			cCl[ii] = clDistributionActual[ii] * chordDistribution.get(ii).doubleValue(SI.METER);
			}
			nasaBlackwellModifiedCLCurve[i] = (
					2/surface.doubleValue(SI.SQUARE_METRE)) * MyMathUtils.integrate1DSimpsonSpline(
					MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistribution),
					cCl);
		}
		
		return nasaBlackwellModifiedCLCurve;
			}
	
	
	public static List<Double> calculateCLTotalCurveWithEquation(
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			List<Double> wingFuselageLiftCoefficient,
			List<Double> horizontalTailLiftCoefficient,
			Double horizontalTailDynamicPressureRatio,
			List<Amount<Angle>> alphaBodyList
			) {
		
		List<Double>  totalLiftCoefficient = new ArrayList<>();
		
		//TOTAL LIFT CALCULATION
				alphaBodyList.stream().forEach( ab-> {

					int i = alphaBodyList.indexOf(ab);
					
					totalLiftCoefficient.add(
							wingFuselageLiftCoefficient.get(i)+
							horizontalTailLiftCoefficient.get(i)*(
							(horizontalTailSurface.doubleValue(SI.SQUARE_METRE)/wingSurface.doubleValue(SI.SQUARE_METRE))*
							horizontalTailDynamicPressureRatio)
							);
				}
				);
		
		return totalLiftCoefficient;
	}
	
	public static List<Double> calculateHorizontalTailEquilibriumLiftCoefficient(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xACWing,
			Amount<Length> zACWing,
			Amount<Length> xACHorizontalTail,
			Amount<Length> zLandingGear,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			List<Double> wingFuselageLiftCoefficient,
			List<Double> wingDragCoefficient,
			List<Double> wingMomentCoefficient,
			List<Double> fuselageMomentCoefficient,
			List<Double> fuselageDragCoefficient,
			Double landingGearDragCoefficient,
			Double horizontalTailDynamicPressureRatio,
			List<Amount<Angle>> alphaBodyList,
			boolean pendularStability
			) {
		
		List<Double>  horizontalTailEquilibriumLiftCoefficient = new ArrayList<>();

		List<Double> wingNormalCoefficient = new ArrayList<>();
		List<Double> wingHorizontalCoeffient = new ArrayList<>();
		List<Double> wingMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> fuselageMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> landingGearMomentCoefficientWithRespectToCG = new ArrayList<>();
		
		//DISTANCES--------
		//Wing	
		Amount<Length> wingHorizontalDistanceACtoCG = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xACWing.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalWingHorizontalDistance = 
				wingHorizontalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		Amount<Length> wingVerticalDistanceACtoCG = Amount.valueOf(
				zACWing.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalWingVerticalDistance = 
				wingVerticalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		
		// landing gear
		Double nonDimensionalLandingGearArm = (zLandingGear.doubleValue(SI.METER)-zCGPosition.doubleValue(SI.METER))/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		
		//Fuselage
		Amount<Length> fuselageVerticalDistanceACtoCG = Amount.valueOf(
				- zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalFuselageVerticalDistance = 
				fuselageVerticalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		
		//Horizontal tail
		Amount<Length> horizontalTailHorizontalDistanceACtoCG = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xACHorizontalTail.doubleValue(SI.METER),
				SI.METER);

		
		alphaBodyList.stream().forEach( ab-> {

		int i = alphaBodyList.indexOf(ab);
			
		// WING -----------------------------
		// forces
		wingNormalCoefficient.add(
				wingFuselageLiftCoefficient.get(i)*Math.cos(ab.doubleValue(SI.RADIAN))+
				wingDragCoefficient.get(i)*Math.sin(ab.doubleValue(SI.RADIAN))
				);

		wingHorizontalCoeffient.add(
				wingDragCoefficient.get(i)*Math.cos(ab.doubleValue(SI.RADIAN)) - 
				wingFuselageLiftCoefficient.get(i)*Math.sin(ab.doubleValue(SI.RADIAN)));		

		// moment with respect to CG
		if(pendularStability == true){
			wingMomentCoefficientWithRespectToCG.add(
					wingNormalCoefficient.get(i)* nondimensionalWingHorizontalDistance+
					wingHorizontalCoeffient.get(i)* nondimensionalWingVerticalDistance+
					wingMomentCoefficient.get(i)
					);
		}
		if(pendularStability == false){
			wingMomentCoefficientWithRespectToCG.add(
					wingNormalCoefficient.get(i)* nondimensionalWingHorizontalDistance+
					wingMomentCoefficient.get(i)
					);
		}
		
		//FUSELAGE----------------------------
		// moment with respect to CG
		fuselageMomentCoefficientWithRespectToCG.add(
				fuselageMomentCoefficient.get(i) + fuselageDragCoefficient.get(i)*nondimensionalFuselageVerticalDistance
				);

		//LANDING GEAR----------------------------
		// moment with respect to CG
		landingGearMomentCoefficientWithRespectToCG.add(
				landingGearDragCoefficient* nonDimensionalLandingGearArm
				);
		
		
		horizontalTailEquilibriumLiftCoefficient.add(i,
				(-wingMomentCoefficientWithRespectToCG.get(i)-
						fuselageMomentCoefficientWithRespectToCG.get(i) -
						landingGearMomentCoefficientWithRespectToCG.get(i) *
				(wingSurface.doubleValue(SI.SQUARE_METRE)/horizontalTailSurface.doubleValue(SI.SQUARE_METRE)) *
				(wingMeanAerodynamicChord.doubleValue(SI.METER)/horizontalTailHorizontalDistanceACtoCG.doubleValue(SI.METER))*
				(1/horizontalTailDynamicPressureRatio)
				));
		}
				);
		
		return horizontalTailEquilibriumLiftCoefficient;
	}
}