package sandbox2.bsp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountException;

import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import igeo.IDoubleR.Rad;

public class Calculator {
	
	
	/**
	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1444.
	 * 		
	 * @author Bruno Spoti
	 * 
	 * @param alphaZeroLift
	 * @param alphaStar
	 * @param endOfLinearityLiftCoefficient
	 * @param alphaStall
	 * @param maximumLiftCoefficient
     * @param aspectRatio
	 * @param rootChord
	 * @param wingSpan
	 * @param deltaX
	 * @param height 
	 * @param liftCurveSlope
	 * @param heightOfRootChord
	 * @param sweepAngleQuarterChord
	 * @param deltaFlap
	 * @return liftCoefficientCurveWithGroundEffect
	 */
	public static List<Double> calculateWingLiftCurveWithGroundEffect(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Amount<Angle> alphaZeroLift,
			Amount<Angle> alphaStar,
			double endOfLinearityLiftCoefficient,
			Amount<Angle> alphaStall,
			double maximumLiftCoefficient,
			double aspectRatio,
			Amount<Length> rootChord,
			Amount<Length> wingSpan,
			Amount<Length> deltaX,
			Amount<Length> height,
			double liftCurveSlope,
			Amount<Length> heightOfRootChord,
			Amount<Angle> sweepAngleQuarterChord,
			Amount<Angle> deltaFlap
			) {
		
		List<Double> liftCoefficientCurveWithGroundEffect = new ArrayList();
		
		return liftCoefficientCurveWithGroundEffect;
		
	}
	
	//---------------------------------------------------------------------------	
	
	/**
	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1444.
	 * 		
	 * @author Bruno Spoti
	 * 
	 * @param alphaZeroLift
	 * @param alphaStar
	 * @param endOfLinearityLiftCoefficient
	 * @param alphaStall
	 * @param maximumLiftCoefficient
	 * @param aspectRatio
	 * @param rootChord
	 * @param wingSpan
	 * @param deltaX
	 * @param height 
	 * @param liftCurveSlope
	 * @param heightOfRootChord
	 * @param sweepAngleQuarterChord
	 * @param deltaFlap
	 * @return deltaAlphaGroundEffect
	 */
	public static Amount<Angle> calculateDeltaAlphaGroundEffectFirstMethodDatcom(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Amount<Angle> alphaZeroLift,
			Amount<Angle> alphaStar,
			double endOfLinearityLiftCoefficient,
			Amount<Angle> alphaStall,
			double maximumLiftCoefficient,
			double aspectRatio,
			Amount<Length> rootChord,
			Amount<Length> wingSpan,
			Amount<Length> deltaX,
			Amount<Length> height,
			Amount<?> liftCurveSlope,
			Amount<Length> heightOfRootChord,
			Amount<Angle> sweepAngleQuarterChord,
			Amount<Angle> deltaFlap,
			Amount<Length> heightOfThreeQuarterSemiSpanChord,
			double flappedLiftCoefficient,
			double liftCoefficient
			) {
		
		Amount<Angle> deltaAlphaGroundEffect = null;
		
		double cLParameter = 57.3*liftCoefficient/(2*Math.PI*Math.pow(Math.cos(sweepAngleQuarterChord.doubleValue(SI.RADIAN)), 2));
		double hCr4Cr = heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER)/4*rootChord.doubleValue(SI.METER);
		double hFracb = (heightOfRootChord.doubleValue(SI.METER)
				+heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER))/wingSpan.doubleValue(SI.METER);
				
		double xValue = aeroDatabaseReader.getDeltaAlphaCLGroundEffectXVs2hfracbDeltax(
				deltaX.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2),
				height.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2));
		double imageBoundVortexParameter = aeroDatabaseReader.getDeltaAlphaCLGroundEffectLL0minus1vshcr4cr(
				cLParameter, 
				heightOfRootChord.doubleValue(SI.METER)/(4*rootChord.doubleValue(SI.METER)));
		double rValue = Math.sqrt(1+Math.pow(hFracb, 2))-hFracb;
		double deltaDeltaLiftCoefficientWithFlap = aeroDatabaseReader.getDeltaAlphaCLGroundEffectDeltaDeltaCLflapVshCr4Cr(hCr4Cr);
		
		deltaAlphaGroundEffect=Amount.valueOf(
				(-(9.12/aspectRatio+7.16*(
				rootChord.doubleValue(SI.METER)/(
						wingSpan.doubleValue(SI.METER))))
				*flappedLiftCoefficient*xValue-
				((aspectRatio/(2*liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()))*
				(rootChord.doubleValue(SI.METER)/(
						wingSpan.doubleValue(SI.METER)))*
				imageBoundVortexParameter*flappedLiftCoefficient*rValue)-
				((Math.pow(deltaFlap.doubleValue(NonSI.DEGREE_ANGLE)/50, 2))/
						liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())*
						deltaDeltaLiftCoefficientWithFlap),
				NonSI.DEGREE_ANGLE);
						  
		return deltaAlphaGroundEffect;
		
	}	
	
	
	
	//------------------------------------------------------------------------------------------
	
	/**
	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1445.
	 * 		
	 * @author Bruno Spoti
	 * 
	 * @param downwashAngle
	 * @param effectiveSpan
	 * @param wingBodyLiftCoefficient
	 * @param deltaFlapLiftCoefficient
	 * @param taperRatio
     * @param aspectRatio
	 * @param flapSpan
	 * @param wingSpan
	 * @param quarterChordHeight
	 * @param horizontalTailquarterChordHeight
	 */

public static Amount<Angle> calculateDeltaEpsilonGroundEffect(
		AerodynamicDatabaseReader aeroDatabaseReader,
		Amount<Angle> downwashAngle,
		Amount<Length> effectiveSpan,
		double wingBodyLiftCoefficient,
		double deltaFlapLiftCoefficient,
		double taperRatio,
		double aspectRatio,
		Amount<Length> flapSpan,
		Amount<Length> wingSpan,
		Amount<Length> quarterChordHeight,
		Amount<Length> horizontalTailquarterChordHeight
		) {
	
	Amount<Angle> deltaEpsilonGroundEffect = null;
	
	double bApexfFracbApexw = aeroDatabaseReader.getDeltaEpsilonGbApexfFracbApexwVsbfFracb(
			flapSpan.doubleValue(SI.METER)/wingSpan.doubleValue(SI.METER));
	double bApexFracb = aeroDatabaseReader.getDeltaEpsilonGbApexFracbVsFracLambda(1/taperRatio);
	double bApexW = bApexFracb*wingSpan.doubleValue(SI.METER);
	double bApexf = bApexFracb*bApexFracb*wingSpan.doubleValue(SI.METER);
	
	Amount<Length> effectiveWingSpan = Amount.valueOf((wingBodyLiftCoefficient + deltaFlapLiftCoefficient)/
			((wingBodyLiftCoefficient/bApexW)+(deltaFlapLiftCoefficient/bApexf)), SI.METER);
			
	
	
	deltaEpsilonGroundEffect = Amount.valueOf(
			downwashAngle.doubleValue(NonSI.DEGREE_ANGLE)*((Math.pow(effectiveWingSpan.doubleValue(SI.METER),2))
			+4*(Math.pow((horizontalTailquarterChordHeight.doubleValue(SI.METER)-quarterChordHeight.doubleValue(SI.METER)),2)))/(
			Math.pow(effectiveWingSpan.doubleValue(SI.METER),2)
			+4*(horizontalTailquarterChordHeight.doubleValue(SI.METER)+quarterChordHeight.doubleValue(SI.METER))),NonSI.DEGREE_ANGLE);
			
	
	return deltaEpsilonGroundEffect;
	
}	

//---------------------------------------------------------------------------------

	/**
	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1446.
	 * 		
	 * @author Bruno Spoti
	 * 
	 * @param alphaZeroLift
	 * @param alphaStar
	 * @param endOfLinearityLiftCoefficient
	 * @param alphaStall
	 * @param maximumLiftCoefficient
	 * @param aspectRatio
	 * @param rootChord
	 * @param wingSpan
	 * @param deltaX
	 * @param height 
	 * @param liftCurveSlope
	 * @param heightOfRootChord
	 * @param sweepAngleQuarterChord
	 * @param deltaFlap
	 * @return deltaAlphaGroundEffect
	 */

	public static Amount<Angle> calculateDeltaAlphaGroundEffectSecondMethodDatcom(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Amount<Angle> alphaZeroLift,
			Amount<Angle> alphaStar,
			double endOfLinearityLiftCoefficient,
			Amount<Angle> alphaStall,
			double maximumLiftCoefficient,
			double aspectRatio,   
			Amount<Length> rootChord, 
			Amount<Length> wingSpan,  
			Amount<Length> deltaX,   
			Amount<Length> height,   
			Amount<?> liftCurveSlope,    
			Amount<Length> heightOfRootChord,   
			Amount<Angle> sweepAngleQuarterChord,  
			Amount<Angle> deltaFlap,    
			Amount<Length> heightOfThreeQuarterSemiSpanChord,
			double flappedLiftCoefficient,  
			double liftCoefficient  
			) {
		
		Amount<Angle> deltaAlphaGroundEffect = null;
		
		double cLParameter = 57.3*liftCoefficient/(2*Math.PI*Math.pow(Math.cos(sweepAngleQuarterChord.doubleValue(SI.RADIAN)), 2));
		double hCr4Cr = heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER)/4*rootChord.doubleValue(SI.METER);
		double hFracb = (heightOfRootChord.doubleValue(SI.METER)
				+heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER))/wingSpan.doubleValue(SI.METER);
				
		double xValue = aeroDatabaseReader.getDeltaAlphaCLGroundEffectXVs2hfracbDeltax(
				deltaX.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2),
				height.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2));
		double imageBoundVortexParameter = aeroDatabaseReader.getDeltaAlphaCLGroundEffectLL0minus1vshcr4cr(
				cLParameter, 
				heightOfRootChord.doubleValue(SI.METER)/(4*rootChord.doubleValue(SI.METER)));
		double rValue = Math.sqrt(1+Math.pow(hFracb, 2))-hFracb;
		double deltaDeltaLiftCoefficientWithFlap = aeroDatabaseReader.getDeltaAlphaCLGroundEffectDeltaDeltaCLflapVshCr4Cr(hCr4Cr);
		
		deltaAlphaGroundEffect=Amount.valueOf(
				(-(9.12/aspectRatio+7.16*(
				rootChord.doubleValue(SI.METER)/(
						wingSpan.doubleValue(SI.METER))))
				*flappedLiftCoefficient*xValue-
				((aspectRatio/(2*liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()))*
				(rootChord.doubleValue(SI.METER)/(
						wingSpan.doubleValue(SI.METER)))*
				imageBoundVortexParameter*flappedLiftCoefficient*rValue)-
				((Math.pow(deltaFlap.doubleValue(NonSI.DEGREE_ANGLE)/50, 2))/
						liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())*
						deltaDeltaLiftCoefficientWithFlap),
				NonSI.DEGREE_ANGLE);
						  
		return deltaAlphaGroundEffect;
		
	}	


}

