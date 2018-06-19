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
			double flappedLiftCoefficient
			) {
		
		Amount<Angle> deltaAlphaGroundEffect = null;
		
		double xValue = aeroDatabaseReader.getDeltaAlphaCLGroundEffectXVs2hfracbDeltax(
				deltaX.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2),
				height.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2)
				);
		
		double imageBoundVortexParameter = 0;
		double rValue = 0;
		double deltaDeltaLiftCoefficientWithFlap = 0;
		
		
		
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
	
	
	
	
	
	
	
	
	/**
	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1444.
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
	
	Amount<Length> effectiveWingSpan=null;
	//double xValue = aeroDatabaseReader.getDeltaAlphaCLGroundEffectXVs2hfracbDeltax(
	//		deltaX.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2),
	//		height.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2)
	//		);
	//
	//double imageBoundVortexParameter = 0;
	//double rValue = 0;
	//double deltaDeltaLiftCoefficientWithFlap = 0;
	
	
	
	deltaEpsilonGroundEffect = Amount.valueOf(
			downwashAngle.doubleValue(NonSI.DEGREE_ANGLE)*((Math.pow(effectiveWingSpan.doubleValue(SI.METER),2))
			+4*(Math.pow((horizontalTailquarterChordHeight.doubleValue(SI.METER)-quarterChordHeight.doubleValue(SI.METER)),2)))/(
			Math.pow(effectiveWingSpan.doubleValue(SI.METER),2)
			+4*(horizontalTailquarterChordHeight.doubleValue(SI.METER)+quarterChordHeight.doubleValue(SI.METER))),NonSI.DEGREE_ANGLE);
			
			
//			Amount.valueOf(
//			(-(9.12/aspectRatio+7.16*(
//			rootChord.doubleValue(SI.METER)/(
//					wingSpan.doubleValue(SI.METER))))
//			*flappedLiftCoefficient*xValue-
//			((aspectRatio/(2*liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()))*
//			(rootChord.doubleValue(SI.METER)/(
//					wingSpan.doubleValue(SI.METER)))*
//			imageBoundVortexParameter*flappedLiftCoefficient*rValue)-
//			((Math.pow(deltaFlap.doubleValue(NonSI.DEGREE_ANGLE)/50, 2))/
//					liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())*
//					deltaDeltaLiftCoefficientWithFlap),
//			NonSI.DEGREE_ANGLE);
					  
					
			
	
	return deltaEpsilonGroundEffect;
	
}	








}

