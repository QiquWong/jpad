package calculators.geometry;

import static java.lang.Math.pow;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.airfoils.IAirfoil;
import calculators.aerodynamics.NasaBlackwell;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;


public class LSGeometryCalc {

	/**
	 * Get the Lifting Surface surface given its span and aspect ratio
	 *  
	 * @param span
	 * @param ar
	 * @return
	 */
	public static double calculateSurface(double span, double ar) {
		return Math.pow(span, 2)/ar;
	}

	public static double calculateSpan(double surface, double ar) {
		return Math.sqrt(ar*surface);
	}

	public static double calculateAR(double span, double surface) {
		return Math.pow(span, 2)/surface;
	}

	/**
	 * 
	 * @param ar
	 * @param taperRatioEquivalent
	 * @param sweepAtY
	 * @param x
	 * @param y
	 * @return
	 */
	public static Amount<Angle> calculateSweep(
			double ar, double taperRatioEquivalent, 
			double sweepAtY, double x, double y) {

		return Amount.valueOf(
				Math.atan(
						Math.tan(sweepAtY) -
						(4./ar)*
						((x - y)*(1 - taperRatioEquivalent)/(1 + taperRatioEquivalent))),
				SI.RADIAN);
	}

	public static List<Double> calculateInfluenceCoefficients (
			List<Amount<Length>> chordsBreakPoints,
			List<Amount<Length>> yBreakPoints,
			Amount<Area> surface,
			Boolean isMirrored
			) {

		int multiply = 1;
		if(isMirrored)
			multiply = 2;
		
		List<Amount<Area>> influenceAreas = new ArrayList<>();
		List<Double> influenceCoefficients = new ArrayList<>();
		
		//----------------------------------------------------------------------------------------------
		// calculation of the first influence area ...
		influenceAreas.add(
				Amount.valueOf(
						0.5
						*chordsBreakPoints.get(0).doubleValue(SI.METER)
						*(yBreakPoints.get(1).to(SI.METER)
								.minus(yBreakPoints.get(0).to(SI.METER))
								.doubleValue(SI.METER)),
						SI.SQUARE_METRE)
				);

		influenceCoefficients.add(
				influenceAreas.get(0).to(SI.SQUARE_METRE)
				.times(multiply)
				.divide(surface.to(SI.SQUARE_METRE))
				.getEstimatedValue()
				);

		//----------------------------------------------------------------------------------------------
		// calculation of the inner influence areas ... 
		for(int i=1; i<yBreakPoints.size()-1; i++) {

			influenceAreas.add(
					Amount.valueOf(
							(0.5
									*chordsBreakPoints.get(i).doubleValue(SI.METER)
									*(yBreakPoints.get(i).to(SI.METER)
											.minus(yBreakPoints.get(i-1).to(SI.METER))
											.doubleValue(SI.METER))
									)
							+(0.5
									*chordsBreakPoints.get(i).doubleValue(SI.METER)
									*(yBreakPoints.get(i+1).to(SI.METER)
											.minus(yBreakPoints.get(i).to(SI.METER))
											.doubleValue(SI.METER))
									),
							SI.SQUARE_METRE)
					);

			influenceCoefficients.add(
					influenceAreas.get(i).to(SI.SQUARE_METRE)
					.times(multiply)
					.divide(surface.to(SI.SQUARE_METRE))
					.getEstimatedValue()
					);

		}

		//----------------------------------------------------------------------------------------------
		// calculation of the last influence area ...
		influenceAreas.add(
				Amount.valueOf(
						0.5
						*chordsBreakPoints.get(chordsBreakPoints.size()-1).doubleValue(SI.METER)
						*(yBreakPoints.get(yBreakPoints.size()-1).to(SI.METER)
								.minus(yBreakPoints.get(yBreakPoints.size()-2).to(SI.METER))
								.doubleValue(SI.METER)),
						SI.SQUARE_METRE)
				);

		influenceCoefficients.add(
				influenceAreas.get(yBreakPoints.size()-1).to(SI.SQUARE_METRE)
				.times(multiply)
				.divide(surface.to(SI.SQUARE_METRE))
				.getEstimatedValue()
				);
		
		return influenceCoefficients;
	}	
	
	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mac
	 * @return dimensional distance of the aerodynamic center (AC) from the MAC
	 * leading edge assuming that the AC is at 0.25% of the MAC
	 */
	public static double calcXacFromLEMacQuarter(double mac) {
		return mac*0.25;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param xACfromLEMac
	 * @param xLEMacGRF
	 * @return dimensional distance of the aerodynamic center (AC) from a 
	 * Generic Reference Frame (GRF) origin
	 */
	public static double calcXacInGRF(double xACfromLEMac, double xLEMacGRF) {
		return xACfromLEMac + xLEMacGRF;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mac
	 * @param xLEMacGRF
	 * @return dimensional distance of the aerodynamic center (AC) from a 
	 * Generic Reference Frame (GRF) origin assuming that the AC is at 0.25% of the MAC
	 */
	public static double calcXacInGRFQuarterMAC(double mac, double xLEMacGRF) {
		return calcXacInGRF(calcXacFromLEMacQuarter(mac), xLEMacGRF);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 555 Sforza
	 * 
	 * @param ar
	 * @param mac
	 * @param taperRatioEquivalent
	 * @param sweepQuarterChordEq
	 * @return dimensional distance of the aerodynamic center (AC) from the MAC
	 * leading edge using De Young - Harper method
	 */
	public static double calcXacFromLEMacDeYoungHarper(
			double ar, double mac, double taperRatioEquivalent,
			double sweepQuarterChordEq) {

		return mac*(0.25 + ar * tan(sweepQuarterChordEq)
		* (0.342 - 0.567*taperRatioEquivalent 
				- 0.908*pow(taperRatioEquivalent,2))
		/(10*(1 + taperRatioEquivalent + pow(taperRatioEquivalent,2))));
	}
	
	public static double calcXacFromNapolitanoDatcom(double mac,
			double taperRatio, double sweepAngleLE, double aspectRatio,  
			double mach,AerodynamicDatabaseReader adbr) {
		double k1= adbr.getX_bar_ac_w_k1_vs_lambda(taperRatio);
		double k2=adbr.getX_bar_ac_w_k2_vs_L_LE_AR_lambda(taperRatio, sweepAngleLE, aspectRatio);
		double xBarAc=adbr.getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(taperRatio, sweepAngleLE, mach, aspectRatio);
		
		double xACNapolitano =  mac*k1*(xBarAc-k2);
		return xACNapolitano;
	}
	
	public static double calcXacPointAtCmConstant(
			NasaBlackwell theNasaBlackwellCalculator,
			Amount<Length> liftingSurfaceMAC,
			Amount<Length> xleMAC,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<Amount<Length>> liftingSurfaceXACDistribution,
			Amount<Area> liftingSurfaceArea
			) {
		double adimensionalXACwithRespectToMAC = 0.0 ;
		
		double xACIterative = 0.25;
		
		Double liftingSurfaceMomentCoefficientAlphaOne = 0.0;
		Double liftingSurfaceMomentCoefficientAlphaTwo = 1.0;
		double[] distancesArrayAC, clDistributionOne, clDistributionTwo,  cmDistributionFromClOne, cmDistributionFromClTwo,
		cCmFromClOne, cCmFromClTwo,  cCmFromCM;

		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

			Amount<Angle> alphaOne = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			Amount<Angle> alphaTwo = Amount.valueOf(6.0, NonSI.DEGREE_ANGLE);
			Amount<Length> dimensionalMomentumPole = Amount.valueOf(
					xleMAC.doubleValue(SI.METER) + xACIterative*liftingSurfaceMAC.doubleValue(SI.METER)
					, SI.METER);
			
			while(Math.abs(liftingSurfaceMomentCoefficientAlphaTwo-liftingSurfaceMomentCoefficientAlphaOne) > 0.0005) {
			clDistributionOne = new double[numberOfPointSemiSpanWise];
			clDistributionTwo = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];
			cmDistributionFromClOne = new double[numberOfPointSemiSpanWise];
			cmDistributionFromClTwo = new double[numberOfPointSemiSpanWise];
			cCmFromClOne = new double[numberOfPointSemiSpanWise];
			cCmFromClTwo = new double[numberOfPointSemiSpanWise];
			cCmFromCM = new double[numberOfPointSemiSpanWise];
					
			theNasaBlackwellCalculator.calculate(alphaOne);
			clDistributionOne = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			theNasaBlackwellCalculator.calculate(alphaTwo);
			clDistributionTwo = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				
//				if(Double.isNaN(clDistribution[ii]))
//					clDistribution[ii] = 0.0;
//				
//				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
//						liftingSurfaceCLAlphaDegDistribution.get(ii);
//
//				if (alphaDistribution[ii]<anglesOfAttack.get(i).doubleValue(NonSI.DEGREE_ANGLE)){
//					clInducedDistributionAtAlphaNew[ii] =
//							liftingSurfaceCLAlphaDegDistribution.get(ii)*
//							alphaDistribution[ii]+
//							liftingSurfaceCl0Distribution.get(ii);
//				}
//				else{
//					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
//							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
//							MyArrayUtils.convertToDoublePrimitive(
//									MyArrayUtils.convertListOfDoubleToDoubleArray(
//											airfoilClMatrix.get(ii))),
//							alphaDistribution[ii]
//							);
//				}

				//TODO modify here 
				distancesArrayAC[ii] =
						dimensionalMomentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceXACDistribution.get(ii).doubleValue(SI.METER)));

				cmDistributionFromClOne [ii] = clDistributionOne[ii] *  
						(distancesArrayAC[ii]);
				
				cmDistributionFromClTwo [ii] = clDistributionTwo[ii] *  
						(distancesArrayAC[ii]);

				cCmFromClOne[ii] = cmDistributionFromClOne [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER);
				cCmFromClTwo[ii] = cmDistributionFromClTwo [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER);
			
				cCmFromCM[ii] = liftingSurfaceCmC4Distribution.get(ii) * 
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) *
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER);
			}
			
			cCmFromClOne[numberOfPointSemiSpanWise-1] = 0;
			cCmFromClTwo[numberOfPointSemiSpanWise-1] = 0;
			cCmFromCM[numberOfPointSemiSpanWise-1] = 0;

			liftingSurfaceMomentCoefficientAlphaOne =
					(((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER))))
					* 
					(MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCmFromClOne)
					+
					MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCmFromCM)));
			
			liftingSurfaceMomentCoefficientAlphaTwo =
					(((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER))))
					* 
					(MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCmFromClTwo)
					+
					MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCmFromCM)));
			
			if(liftingSurfaceMomentCoefficientAlphaTwo < liftingSurfaceMomentCoefficientAlphaOne) {
			dimensionalMomentumPole = Amount.valueOf(
	     			xleMAC.doubleValue(SI.METER) + (xACIterative+0.005)*liftingSurfaceMAC.doubleValue(SI.METER)
	     			, SI.METER);
			}
			else {
				dimensionalMomentumPole = Amount.valueOf(
		     			xleMAC.doubleValue(SI.METER) + (xACIterative-0.005)*liftingSurfaceMAC.doubleValue(SI.METER)
		     			, SI.METER);
			}
				
			
			xACIterative = (dimensionalMomentumPole.doubleValue(SI.METER) - xleMAC.doubleValue(SI.METER))/liftingSurfaceMAC.doubleValue(SI.METER);
			}
			adimensionalXACwithRespectToMAC = xACIterative;
			
		return adimensionalXACwithRespectToMAC;
	}
	
	public static Amount<Length> calcZacFromIntegral(Amount<Area> liftingSurfaceArea,
			List<Amount<Length>> yLE, List<Amount<Length>> chordDistribution, List<Amount<Length>> liftingSurfaceDimensionalY) {

		double [] cY = new double [yLE.size()];
		
		for (int i=0; i<yLE.size(); i++){
		 cY[i] = yLE.get(i).doubleValue(SI.METER) * chordDistribution.get(i).doubleValue(SI.METER);
		}
		
		Amount<Length> yACIntegral =  Amount.valueOf((2/liftingSurfaceArea.doubleValue(SI.SQUARE_METRE))* MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
				cY),
				SI.METER);
		
		return yACIntegral;
	}
	
	public static Amount<Length> calcYacFromIntegral(Amount<Area> liftingSurfaceArea,
			List<Amount<Length>> yDimensionalStation, List<Amount<Length>> chordDistribution) {

		double [] cY = new double [yDimensionalStation.size()];
		
		for (int i=0; i<yDimensionalStation.size(); i++){
		 cY[i] = yDimensionalStation.get(i).doubleValue(SI.METER) * chordDistribution.get(i).doubleValue(SI.METER);
		}
		
		Amount<Length> yACIntegral =  Amount.valueOf((2/liftingSurfaceArea.doubleValue(SI.SQUARE_METRE))* MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalStation),
				cY),
				SI.METER);
		
		return yACIntegral;
	}

	public static Airfoil calculateAirfoilAtY (LiftingSurface theWing, double yLoc) {

		// initializing variables ... 
		AirfoilTypeEnum type = null;
		AirfoilFamilyEnum family = null;
		Double yInner = 0.0;
		Double yOuter = 0.0;
		Double thicknessRatioInner = 0.0;
		Double thicknessRatioOuter = 0.0;
		Double leadingEdgeRadiusInner = 0.0;
		Double leadingEdgeRadiusOuter = 0.0;
		Amount<Angle> alphaZeroLiftInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaZeroLiftOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaEndLinearityInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaEndLinearityOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStallInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStallOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Double clAlphaInner = 0.0;
		Double clAlphaOuter = 0.0;
		Double cdMinInner = 0.0;
		Double cdMinOuter = 0.0;
		Double clAtCdMinInner = 0.0;
		Double clAtCdMinOuter = 0.0;
		Double cl0Inner = 0.0;
		Double cl0Outer = 0.0;
		Double clEndLinearityInner = 0.0;
		Double clEndLinearityOuter = 0.0;
		Double clMaxInner = 0.0;
		Double clMaxOuter = 0.0;
		Double kFactorDragPolarInner = 0.0;
		Double kFactorDragPolarOuter = 0.0;
		Double laminarBucketSemiExtensionInner = 0.0;
		Double laminarBucketSemiExtensionOuter = 0.0;
		Double laminarBucketDepthInner = 0.0;
		Double laminarBucketDepthOuter = 0.0;
		Double cmAlphaQuarterChordInner = 0.0;
		Double cmAlphaQuarterChordOuter = 0.0;
		Double normalizedXacInner = 0.0;
		Double normalizedXacOuter = 0.0;
		Double cmACInner = 0.0;
		Double cmACOuter = 0.0;
		Double cmACStallInner = 0.0;
		Double cmACStallOuter = 0.0;
		Double criticalMachInner = 0.0;
		Double criticalMachOuter = 0.0;
		Double xTransitionUpperInner = 0.0;
		Double xTransitionUpperOuter = 0.0;
		Double xTransitionLowerInner = 0.0;
		Double xTransitionLowerOuter = 0.0;

		if(yLoc < 0.0) {
			System.err.println("\n\tINVALID Y STATION FOR THE INTERMEDIATE AIRFOIL!!");
			return null;
		}

		for(int i=1; i<theWing.getYBreakPoints().size(); i++) {

			if((yLoc > theWing.getYBreakPoints().get(i-1).doubleValue(SI.METER))
					&& (yLoc < theWing.getYBreakPoints().get(i).doubleValue(SI.METER))) {

				type = theWing.getPanels().get(i-1).getAirfoilRoot().getType();
				family = theWing.getPanels().get(i-1).getAirfoilRoot().getFamily();
				yInner = theWing.getYBreakPoints().get(i-1).doubleValue(SI.METER);
				yOuter = theWing.getYBreakPoints().get(i).doubleValue(SI.METER);
				thicknessRatioInner = theWing.getPanels().get(i-1).getAirfoilRoot().getThicknessToChordRatio();
				thicknessRatioOuter = theWing.getPanels().get(i-1).getAirfoilTip().getThicknessToChordRatio();
				leadingEdgeRadiusInner = theWing.getPanels().get(i-1).getAirfoilRoot().getRadiusLeadingEdge();
				leadingEdgeRadiusOuter = theWing.getPanels().get(i-1).getAirfoilTip().getRadiusLeadingEdge();
				alphaZeroLiftInner = theWing.getPanels().get(i-1).getAirfoilRoot().getAlphaZeroLift();
				alphaZeroLiftOuter = theWing.getPanels().get(i-1).getAirfoilTip().getAlphaZeroLift();
				alphaEndLinearityInner = theWing.getPanels().get(i-1).getAirfoilRoot().getAlphaEndLinearTrait();
				alphaEndLinearityOuter = theWing.getPanels().get(i-1).getAirfoilTip().getAlphaEndLinearTrait();
				alphaStallInner = theWing.getPanels().get(i-1).getAirfoilRoot().getAlphaStall();
				alphaStallOuter = theWing.getPanels().get(i-1).getAirfoilTip().getAlphaStall();
				clAlphaInner = theWing.getPanels().get(i-1).getAirfoilRoot().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(); 
				clAlphaOuter = theWing.getPanels().get(i-1).getAirfoilTip().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cdMinInner = theWing.getPanels().get(i-1).getAirfoilRoot().getCdMin();
				cdMinOuter = theWing.getPanels().get(i-1).getAirfoilTip().getCdMin();
				clAtCdMinInner = theWing.getPanels().get(i-1).getAirfoilRoot().getClAtCdMin();
				clAtCdMinOuter = theWing.getPanels().get(i-1).getAirfoilTip().getClAtCdMin();
				cl0Inner = theWing.getPanels().get(i-1).getAirfoilRoot().getClAtAlphaZero();
				cl0Outer = theWing.getPanels().get(i-1).getAirfoilTip().getClAtAlphaZero();
				clEndLinearityInner = theWing.getPanels().get(i-1).getAirfoilRoot().getClEndLinearTrait(); 
				clEndLinearityOuter = theWing.getPanels().get(i-1).getAirfoilTip().getClEndLinearTrait();
				clMaxInner = theWing.getPanels().get(i-1).getAirfoilRoot().getClMax();
				clMaxOuter = theWing.getPanels().get(i-1).getAirfoilTip().getClMax();
				kFactorDragPolarInner = theWing.getPanels().get(i-1).getAirfoilRoot().getKFactorDragPolar();
				kFactorDragPolarOuter = theWing.getPanels().get(i-1).getAirfoilTip().getKFactorDragPolar();
				laminarBucketSemiExtensionInner = theWing.getPanels().get(i-1).getAirfoilRoot().getLaminarBucketSemiExtension();
				laminarBucketSemiExtensionOuter = theWing.getPanels().get(i-1).getAirfoilTip().getLaminarBucketSemiExtension();
				laminarBucketDepthInner = theWing.getPanels().get(i-1).getAirfoilRoot().getLaminarBucketDepth();
				laminarBucketDepthOuter = theWing.getPanels().get(i-1).getAirfoilTip().getLaminarBucketDepth();
				cmAlphaQuarterChordInner = theWing.getPanels().get(i-1).getAirfoilRoot().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cmAlphaQuarterChordOuter = theWing.getPanels().get(i-1).getAirfoilTip().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				normalizedXacInner = theWing.getPanels().get(i-1).getAirfoilRoot().getXACNormalized();
				normalizedXacOuter = theWing.getPanels().get(i-1).getAirfoilTip().getXACNormalized();
				cmACInner = theWing.getPanels().get(i-1).getAirfoilRoot().getCmAC();
				cmACOuter = theWing.getPanels().get(i-1).getAirfoilTip().getCmAC();
				cmACStallInner = theWing.getPanels().get(i-1).getAirfoilRoot().getCmACAtStall();
				cmACStallOuter = theWing.getPanels().get(i-1).getAirfoilTip().getCmACAtStall();
				criticalMachInner = theWing.getPanels().get(i-1).getAirfoilRoot().getMachCritical();
				criticalMachOuter = theWing.getPanels().get(i-1).getAirfoilTip().getMachCritical();
				xTransitionUpperInner = theWing.getPanels().get(i-1).getAirfoilRoot().getXTransitionUpper();
				xTransitionUpperOuter = theWing.getPanels().get(i-1).getAirfoilTip().getXTransitionUpper();
				xTransitionLowerInner = theWing.getPanels().get(i-1).getAirfoilRoot().getXTransitionLower();
				xTransitionLowerOuter = theWing.getPanels().get(i-1).getAirfoilTip().getXTransitionLower();

			}	
		}

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE THICKNESS RATIO
		Double intermediateAirfoilThicknessRatio = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {thicknessRatioInner, thicknessRatioOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LEADING EDGE RADIUS
		double intermediateAirfoilLeadingEdgeRadius =MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {leadingEdgeRadiusInner, leadingEdgeRadiusOuter},
				yLoc
				);
				

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA ZERO LIFT
		Amount<Angle> intermediateAirfoilAlphaZeroLift = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaZeroLiftInner.doubleValue(NonSI.DEGREE_ANGLE), alphaZeroLiftOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA STAR
		Amount<Angle> intermediateAirfoilAlphaEndLinearity = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaEndLinearityInner.doubleValue(NonSI.DEGREE_ANGLE), alphaEndLinearityOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA STALL
		Amount<Angle> intermediateAirfoilAlphaStall = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaStallInner.doubleValue(NonSI.DEGREE_ANGLE), alphaStallOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl ALPHA
		Amount<?> intermediateAirfoilClAlpha = Amount.valueOf( 
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {clAlphaInner, clAlphaOuter},
						yLoc
						),
				NonSI.DEGREE_ANGLE.inverse()
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cd MIN
		Double intermediateAirfoilCdMin = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cdMinInner, cdMinOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl AT Cd MIN
		Double intermediateAirfoilClAtCdMin = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clAtCdMinInner, clAtCdMinOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl0
		Double intermediateAirfoilCl0 = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cl0Inner, cl0Outer},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl END LINEARITY
		Double intermediateAirfoilClEndLinearity = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clEndLinearityInner, clEndLinearityOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl MAX
		Double intermediateAirfoilClMax = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clMaxInner, clMaxOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE K FACTOR DRAG POLAR
		Double intermediateAirfoilKFactorDragPolar = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {kFactorDragPolarInner, kFactorDragPolarOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LAMINAR BUCKET SEMI-EXTENSION
		Double intermediateLaminarBucketSemiExtension = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {laminarBucketSemiExtensionInner, laminarBucketSemiExtensionOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LAMINAR BUCKET DEPTH
		Double intermediateLaminarBucketDepth = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {laminarBucketDepthInner, laminarBucketDepthOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm ALPHA c/4
		Amount<?> intermediateAirfoilCmAlphaQuaterChord = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								new double[] {yInner, yOuter},
								new double[] {cmAlphaQuarterChordInner, cmAlphaQuarterChordOuter},
								yLoc
								),
						NonSI.DEGREE_ANGLE.inverse()
						);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Xac
		Double intermediateAirfoilXac = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {normalizedXacInner, normalizedXacOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac
		Double intermediateAirfoilCmAC = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cmACInner, cmACOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac STALL
		Double intermediateAirfoilCmACStall = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cmACStallInner, cmACStallOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac STALL
		Double intermediateAirfoilCriticalMach = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {criticalMachInner, criticalMachOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE xTransition UPPER
		Double intermediateAirfoilTransitionXUpper = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {xTransitionUpperInner, xTransitionUpperOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE xTransition LOWER
		Double intermediateAirfoilTransitionXLower = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {xTransitionLowerInner, xTransitionLowerOuter},
				yLoc
				);

		//------------------------------------------------------------------------------------------------
		// AIRFOIL CREATION
		Airfoil intermediateAirfoilCreator = new Airfoil(
				new IAirfoil.Builder()
				.setName("Intermediate Airfoil")
				.setType(type)
				.setFamily(family)
				.setThicknessToChordRatio(intermediateAirfoilThicknessRatio)
				.setRadiusLeadingEdgeNormalized(intermediateAirfoilLeadingEdgeRadius)
				.setAlphaZeroLift(intermediateAirfoilAlphaZeroLift)
				.setAlphaEndLinearTrait(intermediateAirfoilAlphaEndLinearity)
				.setAlphaStall(intermediateAirfoilAlphaStall)
				.setClAlphaLinearTrait(intermediateAirfoilClAlpha)
				.setCdMin(intermediateAirfoilCdMin)
				.setClAtCdMin(intermediateAirfoilClAtCdMin)
				.setClAtAlphaZero(intermediateAirfoilCl0)
				.setClEndLinearTrait(intermediateAirfoilClEndLinearity)
				.setClMax(intermediateAirfoilClMax)
				.setKFactorDragPolar(intermediateAirfoilKFactorDragPolar)
				.setLaminarBucketSemiExtension(intermediateLaminarBucketSemiExtension)
				.setLaminarBucketDepth(intermediateLaminarBucketDepth)
				.setCmAlphaQuarterChord(intermediateAirfoilCmAlphaQuaterChord)
				.setXACNormalized(intermediateAirfoilXac)
				.setCmAC(intermediateAirfoilCmAC)
				.setCmACAtStall(intermediateAirfoilCmACStall)
				.setCriticalMach(intermediateAirfoilCriticalMach)
				.setXTransitionUpper(intermediateAirfoilTransitionXUpper)
				.setXTransitionLower(intermediateAirfoilTransitionXLower)
				.buildPartial()
				);

		return intermediateAirfoilCreator;

	}

	public static Airfoil calculateMeanAirfoil (
			LiftingSurface theLiftingSurface
			) {

		List<Double> influenceCoefficients = LSGeometryCalc.calculateInfluenceCoefficients(
				theLiftingSurface.getChordsBreakPoints(),
				theLiftingSurface.getYBreakPoints(), 
				theLiftingSurface.getSurfacePlanform(),
				theLiftingSurface.isMirrored()
				);

		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL DATA CALCULATION:

		//----------------------------------------------------------------------------------------------
		// Maximum thickness:
		double maximumThicknessMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			maximumThicknessMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getThicknessToChordRatio();

		//----------------------------------------------------------------------------------------------
		// Leading edge radius:
		double leadingEdgeRadiusMeanAirfoil = 0.0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			leadingEdgeRadiusMeanAirfoil = leadingEdgeRadiusMeanAirfoil + 
			(theLiftingSurface.getAirfoilList().get(i).getRadiusLeadingEdge()
					*influenceCoefficients.get(i));

		//----------------------------------------------------------------------------------------------
		// Alpha zero lift:
		Amount<Angle> alphaZeroLiftMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaZeroLiftMeanAirfoil = alphaZeroLiftMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAlphaZeroLift()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha star lift:
		Amount<Angle> alphaStarMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStarMeanAirfoil = alphaStarMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAlphaEndLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha stall lift:
		Amount<Angle> alphaStallMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStallMeanAirfoil = alphaStallMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAlphaStall()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cl alpha:
		Amount<?> clAlphaMeanAirfoil = Amount.valueOf(0.0, SI.RADIAN.inverse());

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAlphaMeanAirfoil = clAlphaMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getClAlphaLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cd min:
		double cdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cdMinMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl at Cd min:
		double clAtCdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAtCdMinMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getClAtCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl0:
		double cl0MeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cl0MeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getClAtAlphaZero();	

		//----------------------------------------------------------------------------------------------
		// Cl star:
		double clStarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clStarMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getClEndLinearTrait();	

		//----------------------------------------------------------------------------------------------
		// Cl max:
		double clMaxMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clMaxMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getClMax();	

		//----------------------------------------------------------------------------------------------
		// K factor drag polar:
		double kFactorDragPolarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			kFactorDragPolarMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getKFactorDragPolar();	

		//----------------------------------------------------------------------------------------------
		// Cm quarter chord:
		double cmAlphaQuarteChordMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmAlphaQuarteChordMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getCmAlphaQuarterChord().getEstimatedValue();	

		//----------------------------------------------------------------------------------------------
		// x ac:
		double xACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			xACMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getXACNormalized();	

		//----------------------------------------------------------------------------------------------
		// cm ac:
		double cmACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getCmAC();	

		//----------------------------------------------------------------------------------------------
		// cm ac stall:
		double cmACStallMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACStallMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getCmACAtStall();	

		//----------------------------------------------------------------------------------------------
		// critical Mach number:
		double criticalMachMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			criticalMachMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getMachCritical();	

		//----------------------------------------------------------------------------------------------
		// laminar bucket semi-extension:
		double laminarBucketSemiExtension= 0;
		for(int i=0; i<influenceCoefficients.size(); i++)
			laminarBucketSemiExtension += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getLaminarBucketSemiExtension();

		//----------------------------------------------------------------------------------------------
		// laminar bucket depth:
		double laminarBucketDepth= 0;
		for(int i=0; i<influenceCoefficients.size(); i++)
			laminarBucketDepth += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getLaminarBucketDepth();

		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL CREATION:

		Airfoil meanAirfoilCreator = new Airfoil(new IAirfoil.Builder()
				.setName("Mean Airfoil")
				.setType(theLiftingSurface.getAirfoilList().get(0).getType())
				.setFamily(theLiftingSurface.getAirfoilList().get(0).getFamily())
				.setThicknessToChordRatio(maximumThicknessMeanAirfoil)
				.setRadiusLeadingEdgeNormalized(leadingEdgeRadiusMeanAirfoil)
				.setAlphaZeroLift(alphaZeroLiftMeanAirfoil)
				.setAlphaEndLinearTrait(alphaStarMeanAirfoil)
				.setAlphaStall(alphaStallMeanAirfoil)
				.setClAlphaLinearTrait(clAlphaMeanAirfoil)
				.setCdMin(cdMinMeanAirfoil)
				.setClAtCdMin(clAtCdMinMeanAirfoil)
				.setClAtAlphaZero(cl0MeanAirfoil)
				.setClEndLinearTrait(clStarMeanAirfoil)
				.setClMax(clMaxMeanAirfoil)
				.setKFactorDragPolar(kFactorDragPolarMeanAirfoil)
				.setCmAlphaQuarterChord(Amount.valueOf(cmAlphaQuarteChordMeanAirfoil, NonSI.DEGREE_ANGLE.inverse()))
				.setXACNormalized(xACMeanAirfoil)
				.setCmAC(cmACMeanAirfoil)
				.setCmACAtStall(cmACStallMeanAirfoil)
				.setCriticalMach(criticalMachMeanAirfoil)
				.setLaminarBucketSemiExtension(laminarBucketSemiExtension)
				.setLaminarBucketDepth(laminarBucketDepth)
				.buildPartial()
				);
		
		return meanAirfoilCreator;

	}

	public static double[] calculateInfluenceFactorsMeanAirfoilFlap(
			double etaIn,
			double etaOut,
			List<Double> etaBreakPoints,
			List<Amount<Length>> chordBreakPoints,
			Amount<Length> semiSpan
			) throws InstantiationException, IllegalAccessException{

		double [] influenceAreas = new double [2];
		double [] influenceFactors = new double [2];

		double chordIn = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						etaBreakPoints
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
						),
				etaIn
				);

		double chordOut = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						etaBreakPoints
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
						),
				etaOut
				);

		influenceAreas[0] = (chordIn * ((etaOut - etaIn)*semiSpan.doubleValue(SI.METER)))/2;
		influenceAreas[1] = (chordOut * ((etaOut - etaIn)*semiSpan.doubleValue(SI.METER)))/2;

		// it returns the influence coefficient

		influenceFactors[0] = influenceAreas[0]/(influenceAreas[0] + influenceAreas[1]);
		influenceFactors[1] = influenceAreas[1]/(influenceAreas[0] + influenceAreas[1]);

		return influenceFactors;
	}
	
}
