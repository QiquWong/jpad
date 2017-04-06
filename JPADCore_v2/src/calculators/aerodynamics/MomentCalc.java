package calculators.aerodynamics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.geom.transform.BaseTransform.Degree;

import analyses.liftingsurface.LSAerodynamicsManager.CalcAlpha0L;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.DatabaseManager;
import jahuwaldt.tools.units.Degrees;
//import databasesIO.vedscdatabase.VeDSCDatabaseCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

/**
 * A group of static functions for evaluating aerodynamic moment/moment coefficients.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MomentCalc {

	private MomentCalc() {}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mach
	 * @param ar
	 * @param semispan
	 * @param sweepHalfChordEq
	 * @param acToCgDistance
	 * @param wingMAC
	 * @param alpha0L
	 * @param cLalpha
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCM0LS(
			double mach, double ar, double semispan, double sweepHalfChordEq, 
			double acToCgDistance, double wingMAC, double alpha0L, double cLalpha,
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		return cLalpha
				+ LiftCalc.calculateLiftCoefficientAtAlpha0(alpha0L, cLalpha)
				* (acToCgDistance/wingMAC);
	}

	
	public static double calcCM0LS(
			double acToCgDistance, double wingMAC, double cL0, double cLalpha) {

		return cLalpha
				+ cL0
				* (acToCgDistance/wingMAC);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param cLalpha
	 * @param aircraftXcg
	 * @param xACpercentMAC
	 * @param xLEMacBRF x-coordinate of the Leading Edge of the Mean Aerodynamic Chord 
	 * in the Body Reference Frame
	 * @param meanAerodChord
	 * @return
	 */
	public static double calcCMalphaLS(double cLalpha, double aircraftXcg, 
			double xACpercentMAC, double xLEMacBRF, double meanAerodChord) {

		return cLalpha * (aircraftXcg - (xACpercentMAC + xLEMacBRF)) / meanAerodChord;
	}

	public static double calculateCMACAdditional(
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Double> cmACDistribution
			) {
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				*MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								yDistribution
								), 
						MyArrayUtils.convertToDoublePrimitive(
								cmACDistribution.stream()
								.map(cm -> cm*Math.pow(chordDistribution.get(chordDistribution.indexOf(cm)).doubleValue(SI.METER),2))
								.collect(Collectors.toList())
								),
						0., 
						semiSpan.doubleValue(SI.METER)
						);
	}
	
	public static double calculateCMACBasic(
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Amount<Length>> xLEDistribution,
			List<Amount<Angle>> dihedralDistribution,
			List<Amount<Angle>> twistDistribution,
			List<Amount<Angle>> alphaZeroLiftDistribution,
			List<Amount<Length>> airfoilACToWingACDistribution,
			Double vortexSemiSpanToSemiSpanRatio,
			Double mach,
			Amount<Length> altitude,
			Amount<Angle> alphaZeroLift
			) {
		
		NasaBlackwell theNasaBlackwellCalculatorAlphaZeroLift = new NasaBlackwell(
				semiSpan.doubleValue(SI.METER),
				surface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(yDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(dihedralDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(twistDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaZeroLiftDistribution),
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				mach,
				altitude.doubleValue(SI.METER)
				);
		
		theNasaBlackwellCalculatorAlphaZeroLift.calculate(alphaZeroLift);
		
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				*MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								yDistribution
								),
						theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution()
						.times(new MyArray(MyArrayUtils.convertListOfAmountToDoubleArray(chordDistribution)))
						.times(new MyArray(MyArrayUtils.convertListOfAmountTodoubleArray(airfoilACToWingACDistribution)))
						.toArray(),
						0., 
						semiSpan.doubleValue(SI.METER)
						);
	}
	
	public static double calculateCMACIntegralMean (
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Double> cmACDistribution
			) {
		
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				* MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(yDistribution),
						MyArrayUtils.convertToDoublePrimitive(
								cmACDistribution.stream()
								.map(cmac -> cmac
										*chordDistribution.get(chordDistribution.indexOf(cmac)).doubleValue(SI.METER)
										*chordDistribution.get(chordDistribution.indexOf(cmac)).doubleValue(SI.METER)
										)
								.collect(Collectors.toList())
								),
						0.,
						semiSpan.doubleValue(SI.METER)
						);
		
	}
	/**
	 * 
	 * @param cL_alpha_v
	 * @param kFv
	 * @param kWv
	 * @param kHv
	 * @param armVertical distance of the AC of the vertical tail MAC from the MAC/4 point of the wing
	 * @param sVertical
	 * @param sWing
	 * @param wingSpan
	 * @return
	 */
	public static double calcCNbetaVerticalTail(double cL_alpha_v, double kFv, double kWv, double kHv, 
			double armVertical, double sVertical, double sWing, double wingSpan) {

		if (wingSpan == 0. || sWing == 0.) return 0.; 

		return cL_alpha_v * kFv * kWv * kHv * (sVertical/sWing)  * (armVertical/wingSpan);
	}

	/**
	 * 
	 * @param wingAr
	 * @param verticalAr
	 * @param surfaceWing
	 * @param surfaceVertical
	 * @param sweepC2vertical
	 * @param clAlphaVertical
	 * @param mach
	 * @param kFv
	 * @param kWv
	 * @param kHv
	 * @return
	 */
	public static double calcCNbetaVerticalTail(
			double wingAr, double verticalAr, 
			double armVertical, double wingSpan,
			double surfaceWing, double surfaceVertical, 
			double sweepC2vertical, double clAlphaVertical, 
			double mach, double kFv, double kWv, double kHv) {


		return calcCNbetaVerticalTail(
				LiftCalc.calculateCLalphaHelmboldDiederich(verticalAr, clAlphaVertical, sweepC2vertical,mach), 
				kFv, kWv, kHv, armVertical, surfaceVertical, surfaceWing, wingSpan);
	}

	//	public static double calcCNbetaVerticalTail(
	//			double wingAr, double verticalAr, 
	//			double armVertical, double wingSpan,
	//			double wingPosition,
	//			double surfaceWing, double surfaceVertical, 
	//			double sweepC2vertical, double clAlphaVertical,
	//			double tailconeShape, double horizPosOverVertical,
	//			double fuselageDiameterAtVerticalMAC,
	//			double mach) {
	//		
	//		return calcCNbetaVerticalTail(
	//				LiftCalc.calculateCLalphaHelmboldDiederich(wingAr, clAlphaVertical, sweepC2vertical,mach), 
	//				AerodynamicsDatabaseManager.veDSCDatabaseReader.get_KFv_vs_bv_over_dfv(
	//						LSGeometryCalc.calculateSpan(surfaceVertical, wingAr), fuselageDiameterAtVerticalMAC, tailconeShape), 
	//				AerodynamicsDatabaseManager.veDSCDatabaseReader.get_KWv_vs_zw_over_rf(wingPosition, wingAr, tailconeShape), 
	//				AerodynamicsDatabaseManager.veDSCDatabaseReader.get_KHv_vs_zh_over_bv1(horizPosOverVertical, verticalAr, tailconeShape, wingPosition), 
	//				armVertical, surfaceVertical, surfaceWing, wingSpan);
	//		
	//	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageMainMomentContributionCoefficient
	 * @param fuselageNoseMomentCorrectionCoefficient
	 * @param fuselageTailMomentCorrectionCoefficient
	 * @return the fuselage contribution of pitching moment estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCM0Fuselage(double fuselageMainMomentContributionCoefficient, double fuselageNoseMomentCorrectionCoefficient,
			double fuselageTailMomentCorrectionCoefficient){

		double cM0FR = fuselageMainMomentContributionCoefficient;
		double dCMn = fuselageNoseMomentCorrectionCoefficient;
		double dCMt = fuselageTailMomentCorrectionCoefficient;

		return cM0FR + dCMn + dCMt;
	}

	/**
	 * @author Vincenzo Cusati
	 *  
	 * @param fuselageMainMomentDerivativeContributionCoefficient
	 * @param fuselageNoseMomentDerivativeCorrectionCoefficient
	 * @param fuselageTailMomentDerivativeCorrectionCoefficient
	 * @return the fuselage contribution of pitching moment derivative estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCMAlphaFuselage(double fuselageMainMomentDerivativeContributionCoefficient, 
			double fuselageNoseMomentDerivativeCorrectionCoefficient,
			double fuselageTailMomentDerivativeCorrectionCoefficient){

		double cMalphaFR = fuselageMainMomentDerivativeContributionCoefficient;
		double dCMalphaNose = fuselageNoseMomentDerivativeCorrectionCoefficient;
		double dCMalphaTail = fuselageTailMomentDerivativeCorrectionCoefficient;

		return cMalphaFR  + dCMalphaNose + dCMalphaTail;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageMainYawingMomentDerivativeCoefficient
	 * @param fuselageNoseYawingMomentDerivativeCoefficient
	 * @param fuselageTailYawingMomentDerivativeCoefficient
	 * @return the fuselage contribution of yawing moment estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCNBetaFuselage(double fuselageMainYawingMomentDerivativeCoefficient, 
			double fuselageNoseYawingMomentDerivativeCoefficient,
			double fuselageTailYawingMomentDerivativeCoefficient){

		double cNbetaFR = fuselageMainYawingMomentDerivativeCoefficient;
		double dCNbetaNose = fuselageNoseYawingMomentDerivativeCoefficient;
		double dCNbetaTail = fuselageTailYawingMomentDerivativeCoefficient;

		return cNbetaFR  + dCNbetaNose + dCNbetaTail;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param sweepAngle (referred to c/4 line)
	 * @return the yawing moment coefficient derivative (CN_beta) of the wing 
	 */

	public static double calcCNBetaWing(double sweepAngle){

		double sA = sweepAngle;
		double cNbWing = 0.00006*Math.sqrt(sA);

		return cNbWing; 
	}	


	/**
	 * 
	 * @param liftCoeff is the lift coefficient at a specific flight condition
	 * @param sweepAngle computed with respect to c/4 line
	 * @param aspectRatio
	 * @param xACwMACratio is the ratio between the x-position of the wing aerodynamic center and the mean aerodynamic chord
	 * @param xCGMACratio is the ratio between the x-position of the center of gravity and the mean aerodynamic chord
	 * @return the contribution of the wing to yawing moment derivative coefficent [1/deg]
	 * 
	 * see USAF Stability and Control DATCOM (Design Reference) - Finck 1978, 5.1.3 (pdf pag. 1576-1581)
	 * 
	 * @author Vincenzo Cusati
	 */

	public static double calcCNBetaWing(double liftCoeff, double sweepAngle, double aspectRatio,
														double xACwMACratio,  double xCGMACratio){

		double cL = liftCoeff;
		double sA = sweepAngle;
		double aR = aspectRatio;
		double xACw = xACwMACratio;
		double xCG = xCGMACratio;
		
		double cNbWing = Math.pow(cL, 2)*( ( 1/(4*Math.PI*aR) ) - ( Math.tan(Math.toRadians(sA))/(Math.PI*aR*(aR + Math.cos(Math.toRadians(sA)))) ) *
																  ( Math.cos(Math.toRadians(sA)) - aR/2 - Math.pow(aR, 2)/(8*Math.cos(Math.toRadians(sA))) +
																		  						   (6*(xACw-xCG)*Math.sin(Math.toRadians(sA))/aR) )
										 )/(180/Math.PI);

		return cNbWing; 
	}


	/**
	 * 
	 * This method calculates the yawing moment coefficient derivative (CN_beta) of 
	 * an aircraft as the sum of vertical tail, fuselage, wing contributions. 
	 * 
	 * @param verticalYawingMomDerCoeff
	 * @param fuselageYawingMomDerCoeff
	 * @param wingYawingMomentDerCoeff
	 * @return
	 */

	public static double calcCNBetaAC(double verticalYawingMomDerCoeff,
			double fuselageYawingMomDerCoeff, double wingYawingMomentDerCoeff){

		double cNbetaV    = verticalYawingMomDerCoeff;
		double cNbetaFus  = fuselageYawingMomDerCoeff;
		double cNbetaWing = wingYawingMomentDerCoeff;

		return cNbetaV + cNbetaFus + cNbetaWing;
	}

//-----------------------------------------------------------------------------------	
	public static List<Double> calcCMLiftingSurfaceWithIntegral(
			NasaBlackwell theNasaBlackwellCalculator,
			List<Amount<Angle>> anglesOfAttack,
			Amount<Length> liftingSurfaceMAC,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Area> liftingSurfaceArea,
			Amount<Length> momentumPole  //referred to the origin of LRF
			){

		List<Double> liftingSurfaceMomentCoefficient = new ArrayList<>();

		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, cmDistribution, cCm,
		xcPfracC;

		int numberOfAlphas = anglesOfAttack.size();
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

		for (int i=0; i<numberOfAlphas; i++){

			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];
			cmDistribution = new double[numberOfPointSemiSpanWise];
			cCm = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(anglesOfAttack.get(i));
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<anglesOfAttack.get(0).doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}

				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution [ii] = clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+ liftingSurfaceCmC4Distribution.get(ii);

				cCm[ii] = cmDistribution [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) *
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) ;
			}
			
//			System.out.println(" distance " +  Arrays.toString(distancesArrayAC));
//			System.out.println(" xcp " +  Arrays.toString(xcPfracC));
//			System.out.println(" cl " +  Arrays.toString(clDistribution));
//			System.out.println(" cl new " +  Arrays.toString(clInducedDistributionAtAlphaNew));
//			System.out.println(" cm " + Arrays.toString(cmDistribution) );
//			System.out.println(" ccm " + Arrays.toString(cCm));
			
			cCm[numberOfPointSemiSpanWise-1] = 0;

			liftingSurfaceMomentCoefficient.add(
					i,
					((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER)))
					* MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCm))
					);
			
		}
		return liftingSurfaceMomentCoefficient;
	}
	
	public static List<Double> calcCMLiftingSurfaceWithIntegralACVariable(
			NasaBlackwell theNasaBlackwellCalculator,
			List<Amount<Angle>> anglesOfAttack,
			Amount<Length> liftingSurfaceMAC,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCLforCMMatrix,
			List<List<Double>> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Area> liftingSurfaceArea,
			Amount<Length> momentumPole  //referred to the origin of LRF
			){

		List<Double> liftingSurfaceMomentCoefficient = new ArrayList<>();

		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, cmDistribution, cCm,
		xcPfracC;
		double cmActual;
		int numberOfAlphas = anglesOfAttack.size();
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

		for (int i=0; i<numberOfAlphas; i++){

			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];
			cmDistribution = new double[numberOfPointSemiSpanWise];
			cCm = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(anglesOfAttack.get(i));
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<anglesOfAttack.get(0).doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}

				cmActual = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCLforCMMatrix),
						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCmC4Distribution.get(ii)),
						clInducedDistributionAtAlphaNew[ii]
						);
				

				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution [ii] = clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+cmActual;

				cCm[ii] = cmDistribution [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) *
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) ;
			}
//			System.out.println(" distance " +  Arrays.toString(distancesArrayAC));
//			System.out.println(" xcp " +  Arrays.toString(xcPfracC));
//			System.out.println(" cl " +  Arrays.toString(clDistribution));
//			System.out.println(" cl new " +  Arrays.toString(clInducedDistributionAtAlphaNew));
//			System.out.println(" cm " + Arrays.toString(cmDistribution) );
//			System.out.println(" ccm " + Arrays.toString(cCm));
			
			cCm[numberOfPointSemiSpanWise-1] = 0;

			liftingSurfaceMomentCoefficient.add(
					i,
					((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER)))
					* MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCm))
					);
			
		}
		return liftingSurfaceMomentCoefficient;
	}
	
	public static List<Double> calcCmDistributionLiftingSurfaceWithIntegral(
			NasaBlackwell theNasaBlackwellCalculator,
			Amount<Angle> angleOfAttack,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Length> momentumPole  //referred to the origin of LRF
			){
		
		List<Double> cmDistribution = new ArrayList<>();
		
		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, xcPfracC;
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(angleOfAttack);
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();

			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<angleOfAttack.doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}


				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution.add(ii, clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+ liftingSurfaceCmC4Distribution.get(ii));

			}
			cmDistribution.set(numberOfPointSemiSpanWise-1,0.);
		return cmDistribution;
	}
	
}






