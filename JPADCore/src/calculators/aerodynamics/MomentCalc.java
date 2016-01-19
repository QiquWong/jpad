package calculators.aerodynamics;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.geom.transform.BaseTransform.Degree;

import calculators.geometry.LSGeometryCalc;
import database.databasefunctions.aerodynamics.AerodynamicsDatabaseManager;
import jahuwaldt.tools.units.Degrees;
//import databasesIO.vedscdatabase.VeDSCDatabaseCalc;

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

	public static double calcCM0Fusalage(double fuselageMainMomentContributionCoefficient, double fuselageNoseMomentCorrectionCoefficient,
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

	public static double calcCMAlphaFusalage(double fuselageMainMomentDerivativeContributionCoefficient, 
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

	public static double calcCNBetaFusalage(double fuselageMainYawingMomentDerivativeCoefficient, 
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

}
