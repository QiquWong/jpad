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

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
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
	
	/*
	 * TO DO: implement me
	 * see Aerodynamic_Database_Ultimate.h5 
	 *   --> (x_bar_ac_w)_k1_vs_lambda
	 *   --> (x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)
	 *   -->(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)
	 *   see sandbox adm 
	 */
	public static double calcXacFromNapolitanoDatcom(double mac,
			double taperRatio, double sweepAngleLE, double aspectRatio,  
			double mach,AerodynamicDatabaseReader adbr) {
		double k1= adbr.getX_bar_ac_w_k1_vs_lambda(taperRatio);
		double k2=adbr.getX_bar_ac_w_k2_vs_L_LE_AR_lambda(taperRatio, sweepAngleLE, aspectRatio);
		double xBarAc=adbr.getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(taperRatio, sweepAngleLE, mach, aspectRatio);
		
		double xACNapolitano =  mac*k1*(xBarAc-k2);
		return xACNapolitano;
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
			List<Amount<Length>> yDimensionalStation, List<Amount<Length>> chordDistribution, List<Amount<Length>> liftingSurfaceDimensionalY) {

		double [] cY = new double [yDimensionalStation.size()];
		
		for (int i=0; i<yDimensionalStation.size(); i++){
		 cY[i] = yDimensionalStation.get(i).doubleValue(SI.METER) * chordDistribution.get(i).doubleValue(SI.METER);
		}
		
		Amount<Length> yACIntegral =  Amount.valueOf((2/liftingSurfaceArea.doubleValue(SI.SQUARE_METRE))* MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
				cY),
				SI.METER);
		
		return yACIntegral;
	}

	public static AirfoilCreator calculateAirfoilAtY (LiftingSurface theWing, double yLoc) {

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

		for(int i=1; i<theWing.getLiftingSurfaceCreator().getYBreakPoints().size(); i++) {

			if((yLoc > theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER))
					&& (yLoc < theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER))) {

				type = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getType();
				family = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getFamily();
				yInner = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER);
				yOuter = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER);
				thicknessRatioInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getThicknessToChordRatio();
				thicknessRatioOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getThicknessToChordRatio();
				leadingEdgeRadiusInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getRadiusLeadingEdge().doubleValue(SI.METER);
				leadingEdgeRadiusOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getRadiusLeadingEdge().doubleValue(SI.METER);
				alphaZeroLiftInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaZeroLift();
				alphaZeroLiftOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaZeroLift();
				alphaEndLinearityInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaEndLinearTrait();
				alphaEndLinearityOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaEndLinearTrait();
				alphaStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaStall();
				alphaStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaStall();
				clAlphaInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(); 
				clAlphaOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cdMinInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCdMin();
				cdMinOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCdMin();
				clAtCdMinInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAtCdMin();
				clAtCdMinOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAtCdMin();
				cl0Inner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAtAlphaZero();
				cl0Outer = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAtAlphaZero();
				clEndLinearityInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClEndLinearTrait(); 
				clEndLinearityOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClEndLinearTrait();
				clMaxInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClMax();
				clMaxOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClMax();
				kFactorDragPolarInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getKFactorDragPolar();
				kFactorDragPolarOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getKFactorDragPolar();
				laminarBucketSemiExtensionInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getLaminarBucketSemiExtension();
				laminarBucketSemiExtensionOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getLaminarBucketSemiExtension();
				laminarBucketDepthInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getLaminarBucketDepth();
				laminarBucketDepthOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getLaminarBucketDepth();
				cmAlphaQuarterChordInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cmAlphaQuarterChordOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				normalizedXacInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXACNormalized();
				normalizedXacOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXACNormalized();
				cmACInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAC();
				cmACOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAC();
				cmACStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmACAtStall();
				cmACStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmACAtStall();
				criticalMachInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getMachCritical();
				criticalMachOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getMachCritical();
				xTransitionUpperInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXTransitionUpper();
				xTransitionUpperOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXTransitionUpper();
				xTransitionLowerInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXTransitionLower();
				xTransitionLowerOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXTransitionLower();

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
		Amount<Length> intermediateAirfoilLeadingEdgeRadius = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {leadingEdgeRadiusInner, leadingEdgeRadiusOuter},
						yLoc
						),
				SI.METER
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
		AirfoilCreator intermediateAirfoilCreator = new AirfoilCreator.AirfoilBuilder()
				.name("Intermediate Airfoil")
				.type(type)
				.family(family)
				.thicknessToChordRatio(intermediateAirfoilThicknessRatio)
				.radiusLeadingEdge(intermediateAirfoilLeadingEdgeRadius)
				.alphaZeroLift(intermediateAirfoilAlphaZeroLift)
				.alphaEndLinearTrait(intermediateAirfoilAlphaEndLinearity)
				.alphaStall(intermediateAirfoilAlphaStall)
				.clAlphaLinearTrait(intermediateAirfoilClAlpha)
				.cdMin(intermediateAirfoilCdMin)
				.clAtCdMin(intermediateAirfoilClAtCdMin)
				.clAtAlphaZero(intermediateAirfoilCl0)
				.clEndLinearTrait(intermediateAirfoilClEndLinearity)
				.clMax(intermediateAirfoilClMax)
				.kFactorDragPolar(intermediateAirfoilKFactorDragPolar)
				.laminarBucketSemiExtension(intermediateLaminarBucketSemiExtension)
				.laminarBucketDepth(intermediateLaminarBucketDepth)
				.cmAlphaQuarterChord(intermediateAirfoilCmAlphaQuaterChord)
				.xACNormalized(intermediateAirfoilXac)
				.cmAC(intermediateAirfoilCmAC)
				.cmACAtStall(intermediateAirfoilCmACStall)
				.machCritical(intermediateAirfoilCriticalMach)
				.xTransitionUpper(intermediateAirfoilTransitionXUpper)
				.xTransitionLower(intermediateAirfoilTransitionXLower)
				.build();

		return intermediateAirfoilCreator;

	}

	public static AirfoilCreator calculateMeanAirfoil (
			LiftingSurfaceCreator theLiftingSurface
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
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getThicknessToChordRatio();

		//----------------------------------------------------------------------------------------------
		// Leading edge radius:
		Amount<Length> leadingEdgeRadiusMeanAirfoil = Amount.valueOf(0.0, SI.METER);

		for(int i=0; i<influenceCoefficients.size(); i++)
			leadingEdgeRadiusMeanAirfoil = leadingEdgeRadiusMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getRadiusLeadingEdge()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha zero lift:
		Amount<Angle> alphaZeroLiftMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaZeroLiftMeanAirfoil = alphaZeroLiftMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getAlphaZeroLift()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha star lift:
		Amount<Angle> alphaStarMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStarMeanAirfoil = alphaStarMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getAlphaEndLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha stall lift:
		Amount<Angle> alphaStallMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStallMeanAirfoil = alphaStallMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getAlphaStall()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cl alpha:
		Amount<?> clAlphaMeanAirfoil = Amount.valueOf(0.0, SI.RADIAN.inverse());

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAlphaMeanAirfoil = clAlphaMeanAirfoil
			.plus(theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getClAlphaLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cd min:
		double cdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cdMinMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl at Cd min:
		double clAtCdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAtCdMinMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getClAtCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl0:
		double cl0MeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cl0MeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getClAtAlphaZero();	

		//----------------------------------------------------------------------------------------------
		// Cl star:
		double clStarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clStarMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getClEndLinearTrait();	

		//----------------------------------------------------------------------------------------------
		// Cl max:
		double clMaxMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clMaxMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getClMax();	

		//----------------------------------------------------------------------------------------------
		// K factor drag polar:
		double kFactorDragPolarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			kFactorDragPolarMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getKFactorDragPolar();	

		//----------------------------------------------------------------------------------------------
		// Cm quarter chord:
		double cmAlphaQuarteChordMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmAlphaQuarteChordMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getCmAlphaQuarterChord().getEstimatedValue();	

		//----------------------------------------------------------------------------------------------
		// x ac:
		double xACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			xACMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getXACNormalized();	

		//----------------------------------------------------------------------------------------------
		// cm ac:
		double cmACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getCmAC();	

		//----------------------------------------------------------------------------------------------
		// cm ac stall:
		double cmACStallMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACStallMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getCmACAtStall();	

		//----------------------------------------------------------------------------------------------
		// critical Mach number:
		double criticalMachMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			criticalMachMeanAirfoil += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getMachCritical();	

		//----------------------------------------------------------------------------------------------
		// laminar bucket semi-extension:
		double laminarBucketSemiExtension= 0;
		for(int i=0; i<influenceCoefficients.size(); i++)
			laminarBucketSemiExtension += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getLaminarBucketSemiExtension();

		//----------------------------------------------------------------------------------------------
		// laminar bucket depth:
		double laminarBucketDepth= 0;
		for(int i=0; i<influenceCoefficients.size(); i++)
			laminarBucketDepth += influenceCoefficients.get(i)
			*theLiftingSurface.getAirfoilList().get(i).getAirfoilCreator().getLaminarBucketDepth();

		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL CREATION:

		AirfoilCreator meanAirfoilCreator = new AirfoilCreator.AirfoilBuilder()
				.name("Mean Airfoil")
				.type(theLiftingSurface.getAirfoilList().get(0).getAirfoilCreator().getType())
				.family(theLiftingSurface.getAirfoilList().get(0).getAirfoilCreator().getFamily())
				.thicknessToChordRatio(maximumThicknessMeanAirfoil)
				.radiusLeadingEdge(leadingEdgeRadiusMeanAirfoil)
				.alphaZeroLift(alphaZeroLiftMeanAirfoil)
				.alphaEndLinearTrait(alphaStarMeanAirfoil)
				.alphaStall(alphaStallMeanAirfoil)
				.clAlphaLinearTrait(clAlphaMeanAirfoil)
				.cdMin(cdMinMeanAirfoil)
				.clAtCdMin(clAtCdMinMeanAirfoil)
				.clAtAlphaZero(cl0MeanAirfoil)
				.clEndLinearTrait(clStarMeanAirfoil)
				.clMax(clMaxMeanAirfoil)
				.kFactorDragPolar(kFactorDragPolarMeanAirfoil)
				.cmAlphaQuarterChord(Amount.valueOf(cmAlphaQuarteChordMeanAirfoil, NonSI.DEGREE_ANGLE.inverse()))
				.xACNormalized(xACMeanAirfoil)
				.cmAC(cmACMeanAirfoil)
				.cmACAtStall(cmACStallMeanAirfoil)
				.machCritical(criticalMachMeanAirfoil)
				.laminarBucketSemiExtension(laminarBucketSemiExtension)
				.laminarBucketDepth(laminarBucketDepth)
				.build();

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
