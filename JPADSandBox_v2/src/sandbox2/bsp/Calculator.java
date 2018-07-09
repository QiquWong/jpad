//package sandbox2.bsp;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.measure.quantity.Angle;
//import javax.measure.quantity.Length;
//import javax.measure.unit.NonSI;
//import javax.measure.unit.SI;
//
//import org.jscience.physics.amount.Amount;
//import org.jscience.physics.amount.AmountException;
//import org.omg.PortableInterceptor.NON_EXISTENT;
//
//import configuration.enumerations.MethodEnum;
//import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
//import igeo.IDoubleR.Rad;
//
//public class Calculator {
//
//
//	/**
//	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1444.
//	 * 		
//	 * @author Bruno Spoti
//	 * 
//	 * @param alphaZeroLift
//	 * @param alphaStar
//	 * @param endOfLinearityLiftCoefficient
//	 * @param alphaStall
//	 * @param maximumLiftCoefficient
//	 * @param aspectRatio
//	 * @param rootChord
//	 * @param wingSpan
//	 * @param deltaX
//	 * @param height 
//	 * @param liftCurveSlope
//	 * @param heightOfRootChord
//	 * @param sweepAngleQuarterChord
//	 * @param deltaFlap
//	 * @param method
//	 * @return liftCoefficientCurveWithGroundEffect
//	 */
//	public static List<Double> calculateWingLiftCurveWithGroundEffect(
//			AerodynamicDatabaseReader aeroDatabaseReader,
//			Amount<Angle> alphaZeroLift,
//			Amount<Angle> alphaStar,
//			double endOfLinearityLiftCoefficient,
//			Amount<Angle> alphaStall,
//			double maximumLiftCoefficient,
//			double aspectRatio,
//			Amount<Length> rootChord,
//			Amount<Length> wingSpan,
//			Amount<Length> deltaX,
//			Amount<Length> height,
//			Amount<?> liftCurveSlope,
//			Amount<Length> heightOfRootChord,
//			Amount<Angle> sweepAngleQuarterChord,
//			Amount<Angle> deltaFlap,
//			Amount<Length> heightOfThreeQuarterSemiSpanChord,
//			double flappedLiftCoefficient,
//			List<Amount<Angle>> alphaArray,
//			double maximumThickness,
//			Amount<Length> meanAerodynamicChord,
//			MethodEnum method
//			) {
//
//		List<Double> liftCoefficientCurveWithGroundEffect = new ArrayList();
//
//		switch (method) {
//		case DATCOM_VMU_FIRST_METHOD:
//			
//			Amount<Angle> alphaZeroLiftWithGroundEffectFirstMethodDatcom = 
//			alphaZeroLift.minus(Calculator.calculateDeltaAlphaGroundEffectFirstMethodDatcom(
//					aeroDatabaseReader, 
//					aspectRatio, 
//					rootChord, 
//					wingSpan, 
//					deltaX, 
//					heightOfRootChord, 
//					liftCurveSlope, 
//					heightOfRootChord, 
//					sweepAngleQuarterChord, 
//					deltaFlap, 
//					heightOfThreeQuarterSemiSpanChord, 
//					flappedLiftCoefficient, 
//					0));
//
//			Amount<Angle> alphaStarWithGroundEffectFirstMethodDatcom = alphaStar.minus(
//					calculateDeltaAlphaGroundEffectFirstMethodDatcom(
//							aeroDatabaseReader, 
//							aspectRatio, 
//							rootChord, 
//							wingSpan, 
//							deltaX, 
//							heightOfRootChord, 
//							liftCurveSlope, 
//							heightOfRootChord, 
//							sweepAngleQuarterChord, 
//							deltaFlap, 
//							heightOfThreeQuarterSemiSpanChord, 
//							flappedLiftCoefficient, 
//							endOfLinearityLiftCoefficient));
//
//			Amount<Angle> alphaStallWithGroundEffectFirstMethodDatcom = alphaStall.minus(
//					calculateDeltaAlphaGroundEffectFirstMethodDatcom(
//							aeroDatabaseReader, 
//							aspectRatio, 
//							rootChord, 
//							wingSpan, 
//							deltaX, 
//							heightOfRootChord, 
//							liftCurveSlope, 
//							heightOfRootChord, 
//							sweepAngleQuarterChord, 
//							deltaFlap, 
//							heightOfThreeQuarterSemiSpanChord, 
//							flappedLiftCoefficient, 
//							endOfLinearityLiftCoefficient));
//
//			double clAlphaWithGroundEffectFirstMethodDatcom = endOfLinearityLiftCoefficient/
//					(alphaStarWithGroundEffectFirstMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE)-
//							alphaZeroLiftWithGroundEffectFirstMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE));
//
//			liftCoefficientCurveWithGroundEffect =
//					calculators.aerodynamics.AirfoilCalc.calculateClCurve(
//							alphaArray, 
//							-clAlphaWithGroundEffectFirstMethodDatcom*
//							alphaZeroLiftWithGroundEffectFirstMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE), 
//							maximumLiftCoefficient, 
//							alphaStarWithGroundEffectFirstMethodDatcom, 
//							alphaStallWithGroundEffectFirstMethodDatcom, 
//							Amount.valueOf(clAlphaWithGroundEffectFirstMethodDatcom, NonSI.DEGREE_ANGLE.inverse())
//							);
//			break;
//
//		case DATCOM_VMU_SECOND_METHOD:
//			
//			Amount<Angle> alphaZeroLiftWithGroundEffectSecondMethodDatcom = 
//			alphaZeroLift.minus(Calculator.calculateDeltaAlphaGroundEffectSecondMethodDatcom(
//					aeroDatabaseReader, 
//					aspectRatio, 
//					maximumThickness, 
//					rootChord, 
//					meanAerodynamicChord, 
//					wingSpan, 
//					height, 
//					liftCurveSlope, 
//					heightOfRootChord, 
//					heightOfThreeQuarterSemiSpanChord, 
//					flappedLiftCoefficient, 
//					0));
//
//			Amount<Angle> alphaStarWithGroundEffectSecondMethodDatcom = alphaStar.minus(
//					calculateDeltaAlphaGroundEffectSecondMethodDatcom(
//							aeroDatabaseReader, 
//							aspectRatio, 
//							maximumThickness, 
//							rootChord, 
//							meanAerodynamicChord, 
//							wingSpan, 
//							height, 
//							liftCurveSlope, 
//							heightOfRootChord, 
//							heightOfThreeQuarterSemiSpanChord, 
//							flappedLiftCoefficient,
//							endOfLinearityLiftCoefficient));
//
//			Amount<Angle> alphaStallWithGroundEffectSecondMethodDatcom = alphaStall.minus(
//					calculateDeltaAlphaGroundEffectSecondMethodDatcom(
//							aeroDatabaseReader, 
//							aspectRatio, 
//							maximumThickness, 
//							rootChord, 
//							meanAerodynamicChord, 
//							wingSpan, 
//							height, 
//							liftCurveSlope, 
//							heightOfRootChord, 
//							heightOfThreeQuarterSemiSpanChord, 
//							flappedLiftCoefficient,
//							endOfLinearityLiftCoefficient));
//
//			double clAlphaWithGroundEffectSecondMethodDatcom = endOfLinearityLiftCoefficient/
//					(alphaStarWithGroundEffectSecondMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE)-
//							alphaZeroLiftWithGroundEffectSecondMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE));
//
//			liftCoefficientCurveWithGroundEffect =
//					calculators.aerodynamics.AirfoilCalc.calculateClCurve(
//							alphaArray, 
//							-clAlphaWithGroundEffectSecondMethodDatcom*
//							alphaZeroLiftWithGroundEffectSecondMethodDatcom.doubleValue(NonSI.DEGREE_ANGLE), 
//							maximumLiftCoefficient, 
//							alphaStarWithGroundEffectSecondMethodDatcom, 
//							alphaStallWithGroundEffectSecondMethodDatcom, 
//							Amount.valueOf(clAlphaWithGroundEffectSecondMethodDatcom, NonSI.DEGREE_ANGLE.inverse())
//							);
//
//			break;
//		}
//
//
//
//		return liftCoefficientCurveWithGroundEffect;
//
//	}
//
//	//---------------------------------------------------------------------------	
//
//	/**
//	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1444.
//	 * 		
//	 * @author Bruno Spoti
//	 * 
//	 * @param alphaZeroLift
//	 * @param alphaStar
//	 * @param endOfLinearityLiftCoefficient
//	 * @param alphaStall
//	 * @param maximumLiftCoefficient
//	 * @param aspectRatio
//	 * @param rootChord
//	 * @param wingSpan
//	 * @param deltaX
//	 * @param height 
//	 * @param liftCurveSlope
//	 * @param heightOfRootChord
//	 * @param sweepAngleQuarterChord
//	 * @param deltaFlap
//	 * @return deltaAlphaGroundEffect
//	 */
//	public static Amount<Angle> calculateDeltaAlphaGroundEffectFirstMethodDatcom(
//			AerodynamicDatabaseReader aeroDatabaseReader,
//			double aspectRatio,
//			Amount<Length> rootChord,	
//			Amount<Length> wingSpan,
//			Amount<Length> deltaX,
//			Amount<Length> height,
//			Amount<?> liftCurveSlope,
//			Amount<Length> heightOfRootChord,
//			Amount<Angle> sweepAngleQuarterChord,
//			Amount<Angle> deltaFlap,
//			Amount<Length> heightOfThreeQuarterSemiSpanChord,
//			double flappedLiftCoefficient,
//			double liftCoefficient
//			) {
//
//		Amount<Angle> deltaAlphaGroundEffect = null;
//
//		double cLParameter = 57.3*liftCoefficient/(2*Math.PI*Math.pow(Math.cos(sweepAngleQuarterChord.doubleValue(SI.RADIAN)), 2));
//		double hCr4Cr = heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER)/4*rootChord.doubleValue(SI.METER);
//		double hFracb = (heightOfRootChord.doubleValue(SI.METER)
//				+heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER))/wingSpan.doubleValue(SI.METER);
//
//		double xValue = aeroDatabaseReader.getDeltaAlphaCLGroundEffectXVs2hfracbDeltax(
//				deltaX.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2),
//				height.doubleValue(SI.METER)/(wingSpan.doubleValue(SI.METER)/2));
//		double imageBoundVortexParameter = aeroDatabaseReader.getDeltaAlphaCLGroundEffectLL0minus1vshcr4cr(
//				cLParameter, 
//				heightOfRootChord.doubleValue(SI.METER)/(4*rootChord.doubleValue(SI.METER)));
//		double rValue = Math.sqrt(1+Math.pow(hFracb, 2))-hFracb;
//		double deltaDeltaLiftCoefficientWithFlap = aeroDatabaseReader.getDeltaAlphaCLGroundEffectDeltaDeltaCLflapVshCr4Cr(hCr4Cr);
//
//		deltaAlphaGroundEffect=Amount.valueOf(
//				(-(9.12/aspectRatio+7.16*(
//						rootChord.doubleValue(SI.METER)/(
//								wingSpan.doubleValue(SI.METER))))
//						*flappedLiftCoefficient*xValue-
//						((aspectRatio/(2*liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()))*
//								(rootChord.doubleValue(SI.METER)/(
//										wingSpan.doubleValue(SI.METER)))*
//								imageBoundVortexParameter*flappedLiftCoefficient*rValue)-
//						((Math.pow(deltaFlap.doubleValue(NonSI.DEGREE_ANGLE)/50, 2))/
//								liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())*
//						deltaDeltaLiftCoefficientWithFlap),
//				NonSI.DEGREE_ANGLE);
//
//		return deltaAlphaGroundEffect;
//
//	}	
//
//
//
//	//------------------------------------------------------------------------------------------
//
//	/**
//	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1445.
//	 * 		
//	 * @author Bruno Spoti
//	 * 
//	 * @param downwashAngle
//	 * @param effectiveSpan
//	 * @param wingBodyLiftCoefficient
//	 * @param deltaFlapLiftCoefficient
//	 * @param taperRatio
//	 * @param aspectRatio
//	 * @param flapSpan
//	 * @param wingSpan
//	 * @param quarterChordHeight
//	 * @param horizontalTailquarterChordHeight
//	 */
//
//	public static Amount<Angle> calculateDeltaEpsilonGroundEffect(
//			AerodynamicDatabaseReader aeroDatabaseReader,
//			Amount<Angle> downwashAngle,
//			Amount<Length> effectiveSpan,
//			double wingBodyLiftCoefficient,
//			double deltaFlapLiftCoefficient,
//			double taperRatio,
//			double aspectRatio,
//			Amount<Length> flapSpan,
//			Amount<Length> wingSpan,
//			Amount<Length> quarterChordHeight,
//			Amount<Length> horizontalTailquarterChordHeight
//			) {
//
//		Amount<Angle> deltaEpsilonGroundEffect = null;
//
//		double bApexfFracbApexw = aeroDatabaseReader.getDeltaEpsilonGbApexfFracbApexwVsbfFracb(
//				flapSpan.doubleValue(SI.METER)/wingSpan.doubleValue(SI.METER));
//		double bApexFracb = aeroDatabaseReader.getDeltaEpsilonGbApexFracbVsFracLambda(1/taperRatio);
//		double bApexW = bApexFracb*wingSpan.doubleValue(SI.METER);
//		double bApexf = bApexfFracbApexw*bApexFracb*wingSpan.doubleValue(SI.METER);
//
//		Amount<Length> effectiveWingSpan = Amount.valueOf((wingBodyLiftCoefficient + deltaFlapLiftCoefficient)/
//				((wingBodyLiftCoefficient/bApexW)+(deltaFlapLiftCoefficient/bApexf)), SI.METER);
//
//
//
//		deltaEpsilonGroundEffect = Amount.valueOf(
//				downwashAngle.doubleValue(NonSI.DEGREE_ANGLE)*((Math.pow(effectiveWingSpan.doubleValue(SI.METER),2))
//						+4*(Math.pow((horizontalTailquarterChordHeight.doubleValue(SI.METER)-quarterChordHeight.doubleValue(SI.METER)),2)))/(
//								Math.pow(effectiveWingSpan.doubleValue(SI.METER),2)
//								+4*(horizontalTailquarterChordHeight.doubleValue(SI.METER)+quarterChordHeight.doubleValue(SI.METER))),NonSI.DEGREE_ANGLE);
//
//
//		return deltaEpsilonGroundEffect;
//
//	}	
//
//	//---------------------------------------------------------------------------------
//
//	/**
//	 * @see USAF Stability and Control DATCOM (Design Reference) - Finck 1978_page1446.
//	 * 		
//	 * @author Bruno Spoti
//	 * 
//	 * @param aspectRatio
//	 * @param maximumWingThickness
//	 * @param chordOfMaximumWingThickness    
//	 * @param rootChord
//	 * @param meanAerodynamicChord
//	 * @param wingSpan
//	 * @param height
//	 * @param liftCurveSlope
//	 * @param heightOfRootChord  
//	 * @param heightOfThreeQuarterSemiSpanChord
//	 * @param flappedLiftCoefficient
//	 * @param liftCoefficient  
//	 * @return deltaAlphaGroundEffectSecondMethod
//	 */
//
//
//	public static Amount<Angle> calculateDeltaAlphaGroundEffectSecondMethodDatcom(
//			AerodynamicDatabaseReader aeroDatabaseReader,
//			double aspectRatio,  
//			double maximumThickness,     
//			Amount<Length> rootChord,
//			Amount<Length> meanAerodynamicChord,
//			Amount<Length> wingSpan,    
//			Amount<Length> height,     
//			Amount<?> liftCurveSlope,    
//			Amount<Length> heightOfRootChord,     
//			Amount<Length> heightOfThreeQuarterSemiSpanChord,  
//			double flappedLiftCoefficient,  
//			double liftCoefficient  
//			) {
//
//		Amount<Angle> calculateDeltaAlphaGroundEffectSecondMethod = null;
//
//		double hFracb = (heightOfRootChord.doubleValue(SI.METER)
//				+heightOfThreeQuarterSemiSpanChord.doubleValue(SI.METER))/wingSpan.doubleValue(SI.METER);
//
//		double sigmaParameter = aeroDatabaseReader.getDeltaAlphaGSigmaVshfracb(2*height.doubleValue(SI.METER)/wingSpan.doubleValue(SI.METER));
//		double tParameter = (57.3/(8*Math.PI))*((height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER))/
//				Math.pow((height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER)), 2)+(1/64));
//		double bParameter = aeroDatabaseReader.getDeltaAlphaGBVshFracOverlinecCLWB(
//				height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER), 
//				liftCoefficient);
//		double kParameter = 57.3*0.00300*(height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER))*
//				((Math.pow(Math.pow((height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER)), 2)+(1/64), -2))+
//						((Math.pow(Math.pow((height.doubleValue(SI.METER)/meanAerodynamicChord.doubleValue(SI.METER)), 2)+(9/64), -2))));
//		double rValue = Math.sqrt(1+Math.pow(hFracb, 2))-hFracb;  //-	
//
//		calculateDeltaAlphaGroundEffectSecondMethod = Amount.valueOf(
//				(-18.24*flappedLiftCoefficient*sigmaParameter/aspectRatio)+
//				(rValue*tParameter*Math.pow(flappedLiftCoefficient, 2)/
//						(57.3*liftCurveSlope.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()))-
//				rValue*bParameter+
//				kParameter*maximumThickness, 
//				NonSI.DEGREE_ANGLE);
//
//
//
//		return calculateDeltaAlphaGroundEffectSecondMethod;
//
//	}	
//
//
//}
//
