package calculators.aerodynamics;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

/**
 * A group of static functions for evaluating aerodynamic side force/side force coefficients.
 * 
 * @author cavas
 *
 */
public class SideForceCalc {
	
	private SideForceCalc() {}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param dihedralW
	 * @return
	 */
	public static Amount<?> calcCYBetaWing(Amount<Angle> dihedralW) {
		
		// CY_beta_W
		Amount<?> cYBetaW = Amount.valueOf(
				-0.0001*dihedralW.abs().doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE.inverse()
				).to(SI.RADIAN.inverse());
		
		return cYBetaW;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param surfacePArrowV
	 * @param heightFuselage
	 * @param zW
	 * @return
	 */
	public static Amount<?> calcCYBetaBody(
			Amount<Area> surfaceW,
			Amount<Area> surfacePArrowV, Amount<Length> heightFuselage, Amount<Length> zW
			) {
		
		// K_int
		double cYKappaInt = 0.0;
		double zWOverSemiHeightFuselage = 2*zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		
		if(zWOverSemiHeightFuselage > 0)
			cYKappaInt = 1.50*zWOverSemiHeightFuselage;
		else
			cYKappaInt = -1.88*zWOverSemiHeightFuselage;
		
		// CY_beta_B
		Amount<?> cYBetaB = Amount.valueOf(
				-2*cYKappaInt*surfacePArrowV.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE),
				SI.RADIAN.inverse()
				);
		
		return cYBetaB;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param aspectRatioW
	 * @param sweepC4W
	 * @param dihedralW
	 * @param surfaceH
	 * @param heightFuselage
	 * @param zW
	 * @return
	 */
	public static Amount<?> calcCYBetaHorizontalTail(
			Amount<Area> surfaceW, double aspectRatioW, Amount<Angle> sweepC4W, Amount<Angle> dihedralW,
			Amount<Area> surfaceH,
			Amount<Length> heightFuselage, Amount<Length> zW
			) {
		
		// CY_beta_H
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaHTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceH.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepC4W.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioW;
		
		Amount<?> cYBetaH = Amount.valueOf(
				-0.0001*dihedralW.abs().doubleValue(NonSI.DEGREE_ANGLE)*etaHTimes1MinusdSigmaOverdBeta*surfaceH.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE),
				NonSI.DEGREE_ANGLE.inverse()
				).to(SI.RADIAN.inverse());
		
		return cYBetaH;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param aspectRatioW
	 * @param sweepC4W
	 * @param surfaceV
	 * @param spanV
	 * @param cLAlphaV
	 * @param heightFuselage
	 * @param r1
	 * @param zW
	 * @param databaseReader
	 * @return
	 */
	public static Amount<?> calcCYBetaVerticalTail(
			Amount<Area> surfaceW, double aspectRatioW, Amount<Angle> sweepC4W,
			Amount<Area> surfaceV, Amount<Length> spanV, Amount<?> cLAlphaV,
			Amount<Length> heightFuselage, Amount<Length> r1, Amount<Length> zW,
			AerodynamicDatabaseReader databaseReader
			) {

		// K_Y_V
		double bVOver2TimesR1 = spanV.doubleValue(SI.METER)/(2*r1.doubleValue(SI.METER));

		double cYKappaYV = databaseReader.getCyBetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);

		// CY_beta_V
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaVTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceV.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepC4W.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioW;

		Amount<?> cYBetaV =
				cLAlphaV.abs().times(surfaceV).divide(surfaceW)
				.times(-cYKappaYV*etaVTimes1MinusdSigmaOverdBeta)
				.to(SI.RADIAN.inverse());

		return cYBetaV;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param calcCYBetaWing
	 * @param calcCYBetaBody
	 * @param calcCYBetaHorizontalTail
	 * @param calcCYBetaVerticalTail
	 * @return
	 */
	public static Amount<?> calcCYBetaTotal(Amount<?> calcCYBetaWing, Amount<?> calcCYBetaBody, Amount<?> calcCYBetaHorizontalTail, Amount<?> calcCYBetaVerticalTail) {

		// CY_beta
		Amount<?>  cYBeta = calcCYBetaWing.plus(calcCYBetaBody).plus(calcCYBetaHorizontalTail).plus(calcCYBetaVerticalTail);

		return cYBeta;
	}
	
	// Assuming that CY_delta_A = 0
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param aspectRatioW
	 * @param sweepC4W
	 * @param surfaceV
	 * @param taperRatioV
	 * @param cLAlphaV
	 * @param etaInR
	 * @param etaOutR
	 * @param rudderChordRatio
	 * @param heightFuselage
	 * @param zW
	 * @param databaseReader
	 * @return
	 */
	public static Amount<?> calcCYDeltaR(
			Amount<Area> surfaceW, double aspectRatioW, Amount<Angle> sweepC4W,
			Amount<Area> surfaceV, double taperRatioV, Amount<?> cLAlphaV,
			double etaInR, double etaOutR, double rudderChordRatio,
			Amount<Length> heightFuselage, Amount<Length> zW,
			AerodynamicDatabaseReader databaseReader
			) {
		
		// tau_R
		double tauR = databaseReader.getControlSurfaceTauEVsCControlSurfaceOverCHorizontalTail(rudderChordRatio);
		
		// Delta K_R
		double innerKR = databaseReader.getCYDeltaRKRVsEtaLambdaV(taperRatioV, etaInR);
		double outerKR = databaseReader.getCYDeltaRKRVsEtaLambdaV(taperRatioV, etaOutR);
		double deltaKR = outerKR - innerKR;
		
		// CY_delta_r
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaV =
				0.724
				+ 3.06*(surfaceV.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepC4W.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioW;
		
		Amount <?> cYDeltaR = cLAlphaV.abs().times(surfaceV).divide(surfaceW).times(etaV*deltaKR*tauR);
		
		return cYDeltaR;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param spanW
	 * @param aspectRatioW
	 * @param sweepC4W
	 * @param surfaceV
	 * @param spanV
	 * @param xV
	 * @param zV
	 * @param cLAlphaV
	 * @param heightFuselage
	 * @param r1
	 * @param zW
	 * @param angleOfAttack
	 * @param databaseReader
	 * @return
	 */
	public static Amount<?> calcCYp(
			Amount<Area> surfaceW, Amount<Length> spanW, double aspectRatioW, Amount<Angle> sweepC4W,
			Amount<Area> surfaceV, Amount<Length> spanV, Amount<Length> xV, Amount<Length> zV, Amount<?> cLAlphaV,
			Amount<Length> heightFuselage, Amount<Length> r1, Amount<Length> zW,
			Amount<Angle> angleOfAttack,
			AerodynamicDatabaseReader databaseReader
			) {

		// K_Y_V
		double bVOver2TimesR1 = spanV.doubleValue(SI.METER)/(2*r1.doubleValue(SI.METER));

		double cYKappaYV = databaseReader.getCyBetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);

		// CY_beta_V
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaVTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceV.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepC4W.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioW;

		Amount<?> cYBetaV =
				cLAlphaV.abs().times(surfaceV).divide(surfaceW)
				.times(-cYKappaYV*etaVTimes1MinusdSigmaOverdBeta)
				.to(SI.RADIAN.inverse());
		
		// CY_p
		Amount<?> cYp =
				cYBetaV
				.times(
						zV.times(
								Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
						).minus(
								xV.times(
										Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanW).times(2);

		return cYp;
	}
	
	/**
	 *
	 * @author cavas
	 * 
	 * @param surfaceW
	 * @param spanW
	 * @param aspectRatioW
	 * @param sweepC4W
	 * @param surfaceV
	 * @param spanV
	 * @param xV
	 * @param zV
	 * @param cLAlphaV
	 * @param heightFuselage
	 * @param r1
	 * @param zW
	 * @param angleOfAttack
	 * @param databaseReader
	 * @return
	 */
	public static Amount<?> calcCYr(
			Amount<Area> surfaceW, Amount<Length> spanW, double aspectRatioW, Amount<Angle> sweepC4W,
			Amount<Area> surfaceV, Amount<Length> spanV, Amount<Length> xV, Amount<Length> zV, Amount<?> cLAlphaV,
			Amount<Length> heightFuselage, Amount<Length> r1, Amount<Length> zW,
			Amount<Angle> angleOfAttack,
			AerodynamicDatabaseReader databaseReader
			) {

		// K_Y_V
		double bVOver2TimesR1 = spanV.doubleValue(SI.METER)/(2*r1.doubleValue(SI.METER));

		double cYKappaYV = databaseReader.getCyBetaVKYVVsBVOver2TimesR1(bVOver2TimesR1);

		// CY_beta_V
		double zWOverHeightFuselage = zW.doubleValue(SI.METER)/heightFuselage.doubleValue(SI.METER);
		double etaVTimes1MinusdSigmaOverdBeta =
				0.724
				+ 3.06*(surfaceV.doubleValue(SI.SQUARE_METRE)/surfaceW.doubleValue(SI.SQUARE_METRE))/(1 + Math.cos(sweepC4W.doubleValue(SI.RADIAN)))
				+ 0.4*zWOverHeightFuselage
				+ 0.009*aspectRatioW;

		Amount<?> cYBetaV =
				cLAlphaV.abs().times(surfaceV).divide(surfaceW)
				.times(-cYKappaYV*etaVTimes1MinusdSigmaOverdBeta)
				.to(SI.RADIAN.inverse());
		
		// CY_r
		Amount<?> cYr =
				cYBetaV
				.times(
						zV.times(
								Math.sin(angleOfAttack.doubleValue(SI.RADIAN))
						).plus(
								xV.times(
										Math.cos(angleOfAttack.doubleValue(SI.RADIAN))
								)
						)
				).divide(spanW).times(-2);

		return cYr;
	}
	
}
