package calculators.weights;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import calculators.geometry.FusNacGeometryCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;

public class LiftingSurfaceWeightCalc {

	/*
	 *  Roskam page 85 (pdf) part V
	 *  This equation is valid only for the following parameters range:
	 *  
	 *    - Mach dive0 = 0.4 - 0.8
	 *    - t/c_max = 0.08 - 0.15
	 *    - AR = 4 - 12 
	 */
	public static Amount<Mass> calculateWingMassRoskam (Aircraft aircraft) {
		
		return Amount.valueOf(
				2 * ( 0.00428*
						Math.pow(aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.48)
						* aircraft.getWing().getAspectRatio()
						* Math.pow(aircraft.getTheAnalysisManager().getMachDive0(), 0.43)
						* Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(NonSI.POUND_FORCE)
								* aircraft.getTheAnalysisManager().getNUltimate(), 0.84)
						* Math.pow(aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.14)
						) 
						/ (Math.pow(100*aircraft.getWing().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(), 0.76)
								* Math.pow(Math.cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN)), 1.54)
								),
						NonSI.POUND
						).to(SI.KILOGRAM);
		
	}
	
	/*
	 * page 430 Aircraft design synthesis
	 */
	public static Amount<Mass> calculateWingMassKroo (Aircraft aircraft) {
		
		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getWing());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		
		Amount<Angle> sweepStructuralAxis = Amount.valueOf(
					Math.atan(
							Math.tan(
									aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN)
									)
							- (4./aircraft.getWing().getAspectRatio())
							* (aircraft.getWing().getTheLiftingSurfaceInterface().getMainSparDimensionlessPosition()
									* (1 - aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
									/ (1 + aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
									)
							),
					1e-9, // precision
					SI.RADIAN);
			
		
		return Amount.valueOf(
				(4.22 * aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2) 
						+ 1.642e-6
						* (aircraft.getTheAnalysisManager().getNUltimate()
								* Math.pow(aircraft.getWing().getSpan().doubleValue(NonSI.FOOT), 3)
								* Math.sqrt(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)
										* aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND)
										)
								* (1 + (2 * aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
										)
								)
						/ (thicknessMean
								* Math.pow(
										Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),
										2
										)
								* aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2)
								* (1 + aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
								)
						),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
	}
	
	/*
	 * page 134 Jenkinson - Civil Jet Aircraft Design
	 */
	public static Amount<Mass> calculateWingMassJenkinson (Aircraft aircraft) {

		Amount<Mass> wingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		double R = 0.0;

		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getWing());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		List<Amount<Mass>> wingMassList = new ArrayList<>();
		wingMassList.add(wingMass);
		
		int i=1;
		int maxIteration = 30;
		while ((Math.abs((wingMassList.get(i).minus(wingMassList.get(i-1)))
				.divide(wingMassList.get(i))
				.getEstimatedValue())
				>= 0.01)
				|| (maxIteration >= i)
				) {

			try {
				if(aircraft.getPowerPlant().getMountingPosition().equals(EngineMountingPositionEnum.WING)) 
					R = wingMass.doubleValue(SI.KILOGRAM) 
					+ aircraft.getTheAnalysisManager().getTheWeights().getFuelMass().doubleValue(SI.KILOGRAM) 
					+ ((2*(aircraft.getNacelles().getTotalMass().doubleValue(SI.KILOGRAM) 
							+ aircraft.getPowerPlant().getTotalMass().doubleValue(SI.KILOGRAM))
							* aircraft.getNacelles().getDistanceBetweenInboardNacellesY().doubleValue(SI.METER)
							)
							/ (0.4*aircraft.getWing().getSpan().doubleValue(SI.METER))
							)
					+ ((2*(aircraft.getNacelles().getTotalMass().doubleValue(SI.KILOGRAM) 
							+ aircraft.getPowerPlant().getTotalMass().doubleValue(SI.KILOGRAM))
							* aircraft.getNacelles().getDistanceBetweenOutboardNacellesY().doubleValue(SI.METER)
							)
							/ (0.4*aircraft.getWing().getSpan().doubleValue(SI.METER))
							);
			} 
			catch(NullPointerException e) {
				e.printStackTrace();
			}

			wingMass = Amount.valueOf(0.021265 *
					( pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
							* aircraft.getTheAnalysisManager().getNUltimate(), 0.4843
							)
							* pow(aircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 0.7819)
							* pow(aircraft.getWing().getAspectRatio(), 0.993)
							* pow(1 + aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.4)
							* pow(1 - (R/aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)),0.4)
							)
					/ (aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(NonSI.DEGREE_ANGLE)
							* pow(thicknessMean,0.4)
							), 
					SI.KILOGRAM);

		}

		return wingMass;
	}
		
	/*
	 * Raymer - Aircraft Design a Conceptual Approach - page 403 
	 */
	public static Amount<Mass> calculateWingMassRaymer (Aircraft aircraft) {
		
		return  Amount.valueOf(
				0.0051 
				*pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND) 
						*aircraft.getTheAnalysisManager().getNUltimate(),
						0.557
						)
				*pow(aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2),0.649)
				*pow(aircraft.getWing().getAspectRatio(), 0.5)
				*pow(aircraft.getWing().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(), (-0.4))
				*pow(1 + aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.1)
				*pow(cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1)
				*pow(aircraft.getWing().getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2), 0.1),
				NonSI.POUND)
				.to(SI.KILOGRAM);
	}
	
	/*
	 * page 583 pdf Sadraey Aircraft Design System Engineering Approach
	 * (results very similar to Jenkinson) 
	 */
	public static Amount<Mass> calculateWingMassSadraey (Aircraft aircraft) {
		
		double _kRho = 1.0;
		if(aircraft.getPowerPlant().getMountingPosition().equals(EngineMountingPositionEnum.WING))
			_kRho = 0.0375;
		else
			_kRho = 0.0275;
		
		return Amount.valueOf(
				aircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
				* aircraft.getWing().getMeanAerodynamicChord().doubleValue(SI.METER)
				* aircraft.getWing().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
				* aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
				* _kRho
				* pow(aircraft.getWing().getAspectRatio()
						*aircraft.getTheAnalysisManager().getNUltimate()
						/cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), 
						0.6
						)
				* pow(aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.04),
				SI.KILOGRAM
				);
				
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag.280-281
	 */
	public static Amount<Mass> calculateWingMassTorenbeek1982 (Aircraft aircraft) {
		
		double kSpoilers = 1.0;
		double kEngine = 1.0;
		double kLandingGears = 1.0;
		
		if(!aircraft.getWing().getSpoilers().isEmpty())
			kSpoilers = 1.02;
		if(aircraft.getWing().getNumberOfEngineOverTheWing() > 0 && aircraft.getWing().getNumberOfEngineOverTheWing() <=2)
			kEngine = 0.95;
		if(aircraft.getWing().getNumberOfEngineOverTheWing() > 2 && aircraft.getWing().getNumberOfEngineOverTheWing() <=4)
			kEngine = 0.90;
		if(!aircraft.getLandingGears().getMountingPosition().equals(LandingGearsMountingPositionEnum.WING))
			kLandingGears = 0.95;
		
			
		return Amount.valueOf(
				0.0017
				* kEngine
				* kLandingGears
				* kSpoilers
				* aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND)
				* Math.pow(aircraft.getWing().getSpan().to(NonSI.FOOT).getEstimatedValue()
						/ Math.cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN)),
						0.75)
				* (1 + Math.pow(
						6.25*Math.cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))
						/aircraft.getWing().getSpan().doubleValue(NonSI.FOOT), 
						0.5
						)
				)
				* Math.pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.55)
				* Math.pow(
						(aircraft.getWing().getSpan().doubleValue(NonSI.FOOT)
						* aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2))
						/ (aircraft.getWing().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
								* aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(NonSI.FOOT)
								* aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND)
								* Math.cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))
								),
						0.3),
				NonSI.POUND)
				.to(SI.KILOGRAM);
		
	}
	
	/*
	 * Torenbeek: Advanced Aircraft Design (2013) page 235 
	 */
	public static Amount<Mass> calculateWingMassTorenbeek2013 (Aircraft aircraft) {
		
		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getWing());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		
		return Amount.valueOf( 
				(0.0013 * aircraft.getTheAnalysisManager().getNUltimate()
						* Math.pow(
								aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(SI.NEWTON)
								* aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().doubleValue(SI.NEWTON), 
								0.5)
						* 0.36 * Math.pow( 1 + aircraft.getWing().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.5)
						* (aircraft.getWing().getSpan().doubleValue(SI.METER)/100)
						* (aircraft.getWing().getAspectRatio()
								/ (thicknessMean
										* Math.pow(
												Math.cos(aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN)),
												2
												)
										)
								)
						+ 210*aircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
				/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 381 Howe Aircraft Conceptual Design Synthesis
	 */
	public static Amount<Mass> calculateHTailMassHowe (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.047
				* aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND)
				* pow(aircraft.getHTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 1.24),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 135 Jenkinson - Civil Jet Aircraft Design
	 */
	public static Amount<Mass> calculateHTailMassJenkinson (Aircraft aircraft) {
		
		return Amount.valueOf(
				25*aircraft.getHTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * Fundamentals of Aircraft and Airship Design, Volume I - pag. 554
	 */
	public static Amount<Mass> calculateHTailMassNicolai (Aircraft aircraft) {
		
		double gamma = pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()
				* aircraft.getTheAnalysisManager().getNUltimate(), 
				0.813
				)
				* pow(aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.584)
				* pow(aircraft.getHTail().getSpan().doubleValue(NonSI.FOOT)
						/ (aircraft.getHTail().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
								* aircraft.getHTail().getPanels().get(0).getChordRoot().doubleValue(NonSI.FOOT)
								), 0.033
						)
				* pow(aircraft.getHTail().getMeanAerodynamicChord().doubleValue(NonSI.FOOT)
						/ aircraft.getHTail().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT),
						0.28
						);
		
		return Amount.valueOf(
				0.0034 * pow(gamma, 0.915),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
	}
	
	/*
	 * Raymer - Aircraft Design a Conceptual Approach - page 403
	 */
	public static Amount<Mass> calculateHTailMassRaymer (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.0379 
				* pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.639)
				* pow(1 + 
						(Amount.valueOf(
								FusNacGeometryCalc.getWidthAtX(
										aircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER), 
										aircraft.getFuselage().getOutlineXYSideRCurveX(), 
										aircraft.getFuselage().getOutlineXYSideRCurveY()
										),
								SI.METER
								).doubleValue(NonSI.FOOT)
						/aircraft.getHTail().getSpan().doubleValue(NonSI.FOOT)
						), 
						-0.25
						) 
				* pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.1) 
				* pow(aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.75) 
				* pow(aircraft.getHTail().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), -1.) 
				* pow(0.3 * aircraft.getHTail().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), 0.704) 
				* pow( cos( aircraft.getHTail().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1) 
				* pow(aircraft.getHTail().getAspectRatio(), 0.166) 
				* pow(1 + aircraft.getHTail().getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2)
						/aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.1),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
	}
	
	/*
	 * page 431 Aircraft design synthesis
	 */
	public static Amount<Mass> calculateHTailKroo (Aircraft aircraft) {

		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getHTail());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		
		Amount<Angle> sweepStructuralAxis = Amount.valueOf(
				Math.atan(
						Math.tan(aircraft.getHTail().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN))
						- (4./aircraft.getWing().getAspectRatio())
						* (0.25
								*(1 - aircraft.getHTail().getPanels().get(0).getTaperRatio())
								/(1 + aircraft.getHTail().getPanels().get(0).getTaperRatio()))
						),
				1e-9, // precision
				SI.RADIAN);
		
		return Amount.valueOf(
				(5.25*aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2)) 
						+ (0.8e-6
								* (aircraft.getTheAnalysisManager().getNUltimate()
										* Math.pow(aircraft.getHTail().getSpan().doubleValue(NonSI.FOOT), 3)
										* aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)
										* aircraft.getHTail().getMeanAerodynamicChord().doubleValue(NonSI.FOOT)
										* Math.sqrt(aircraft.getHTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
										)
								)
						/ (thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)
								* aircraft.getHTail().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT)
								* Math.pow(aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 1.5)
								),
				NonSI.POUND
				).to(SI.KILOGRAM);

	}

	/*
	 * Sadraey Aircraft Design System Engineering Approach - page 561
	 */
	public static Amount<Mass> calculateHTailMassSadraey (Aircraft aircraft) {
		
		double[] _kRhoArray = new double[] {0.025, 0.0275};
		double[] _positionRelativeToAttachmentArray = new double[] {0.0, 1.0};
		
		double _kRhoHTailPosition = 1.0;
		if(aircraft.getHTail().getPositionRelativeToAttachment() >= 0.0 && aircraft.getHTail().getPositionRelativeToAttachment() <= 1.0)
			_kRhoHTailPosition = MyMathUtils.getInterpolatedValue1DLinear(
					_positionRelativeToAttachmentArray,
					_kRhoArray, 
					aircraft.getHTail().getPositionRelativeToAttachment()
					);
		
		return Amount.valueOf(
				aircraft.getHTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
				* aircraft.getHTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				* aircraft.getHTail().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
				* aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().getEstimatedValue()
				* _kRhoHTailPosition
				* pow(aircraft.getHTail().getAspectRatio()
						/ cos(aircraft.getHTail().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), 
						0.6
						)
				* pow(aircraft.getHTail().getPanels().get(0).getTaperRatio(), 0.04)
				* pow(aircraft.getHTail().getVolumetricRatio(), 0.3)
				* pow(aircraft.getHTail().getSymmetricFlaps().get(0).getMeanChordRatio(), 0.4),
				SI.KILOGRAM);
		
	}

	/*
	 * Roskam page 90 (pdf) part V
	 */
	public static Amount<Mass> calcuateHTailMassRoskam (Aircraft aircraft) {
		
		return Amount.valueOf(
				aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2)
				* (((3.81
						* aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(NonSI.KNOT)
						* pow(aircraft.getHTail().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.2)
						) 
						/ (1000*sqrt(cos(aircraft.getHTail().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))))  
						)
						- 0.287
						),
				NonSI.POUND).to(SI.KILOGRAM);
		
	}
	
	/*
	 * page 381 Howe Aircraft Conceptual Design Synthesis
	 */
	public static Amount<Mass> calculateVTailMassHowe (Aircraft aircraft) {
		
		double[] _kArray = new double[] {1.0, 1.5};
		double[] _positionRelativeToAttachmentArray = new double[] {0.0, 1.0};
		
		double k = MyMathUtils.getInterpolatedValue1DLinear(
				_positionRelativeToAttachmentArray,
				_kArray,
				aircraft.getHTail().getPositionRelativeToAttachment()
				);
		
		return Amount.valueOf(
				0.065
				* k
				* aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND)
				* pow(aircraft.getVTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 1.15),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 135 Jenkinson - Civil Jet Aircraft Design
	 */
	public static Amount<Mass> calculateVTailMassJenkinson (Aircraft aircraft) {
		
		return Amount.valueOf(
				22*
				aircraft.getVTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calculateCanardMassHowe (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.047
				* aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND)
				* pow(aircraft.getCanard().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 1.24),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calculateCanardMassJenkinson (Aircraft aircraft) {
		
		return Amount.valueOf(
				25*aircraft.getCanard().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calculateCanardMassNicolai (Aircraft aircraft) {
		
		double gamma = pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()
				* aircraft.getTheAnalysisManager().getNUltimate(), 
				0.813
				)
				* pow(aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.584)
				* pow(aircraft.getCanard().getSpan().doubleValue(NonSI.FOOT)
						/ (aircraft.getCanard().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
								* aircraft.getCanard().getPanels().get(0).getChordRoot().doubleValue(NonSI.FOOT)
								), 0.033
						)
				* pow(aircraft.getCanard().getMeanAerodynamicChord().doubleValue(NonSI.FOOT)
						/ aircraft.getCanard().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT),
						0.28
						);
		
		return Amount.valueOf(
				0.0034 * pow(gamma, 0.915),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calculateCanardMassRaymer (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.0379 
				* pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.639)
				* pow(1 + 
						(Amount.valueOf(
								FusNacGeometryCalc.getWidthAtX(
										aircraft.getCanard().getXApexConstructionAxes().doubleValue(SI.METER), 
										aircraft.getFuselage().getOutlineXYSideRCurveX(), 
										aircraft.getFuselage().getOutlineXYSideRCurveY()
										),
								SI.METER
								).doubleValue(NonSI.FOOT)
						/aircraft.getCanard().getSpan().doubleValue(NonSI.FOOT)
						), 
						-0.25
						) 
				* pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.1) 
				* pow(aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.75) 
				* pow(aircraft.getCanard().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), -1.) 
				* pow(0.3 * aircraft.getCanard().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), 0.704) 
				* pow( cos( aircraft.getCanard().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1) 
				* pow(aircraft.getCanard().getAspectRatio(), 0.166) 
				* pow(1 + aircraft.getCanard().getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2)
						/aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.1),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calculateCanardKroo (Aircraft aircraft) {

		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getCanard());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		
		Amount<Angle> sweepStructuralAxis = Amount.valueOf(
				Math.atan(
						Math.tan(aircraft.getCanard().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN))
						- (4./aircraft.getWing().getAspectRatio())
						* (0.25
								*(1 - aircraft.getCanard().getPanels().get(0).getTaperRatio())
								/(1 + aircraft.getCanard().getPanels().get(0).getTaperRatio()))
						),
				1e-9, // precision
				SI.RADIAN);
		
		return Amount.valueOf(
				(5.25*aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2)) 
						+ (0.8e-6
								* (aircraft.getTheAnalysisManager().getNUltimate()
										* Math.pow(aircraft.getCanard().getSpan().doubleValue(NonSI.FOOT), 3)
										* aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)
										* aircraft.getCanard().getMeanAerodynamicChord().doubleValue(NonSI.FOOT)
										* Math.sqrt(aircraft.getCanard().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
										)
								)
						/ (thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)
								* aircraft.getCanard().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT)
								* Math.pow(aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 1.5)
								),
				NonSI.POUND
				).to(SI.KILOGRAM);

	}

	/*
	 * Same as HTail without _kRhoArray and _positionRelativeToAttachmentArray
	 */
	public static Amount<Mass> calculateCanardMassSadraey (Aircraft aircraft) {
		
		return Amount.valueOf(
				aircraft.getCanard().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
				* aircraft.getCanard().getMeanAerodynamicChord().doubleValue(SI.METER)
				* aircraft.getCanard().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
				* aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().getEstimatedValue()
				* pow(aircraft.getCanard().getAspectRatio()
						/ cos(aircraft.getCanard().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), 
						0.6
						)
				* pow(aircraft.getCanard().getPanels().get(0).getTaperRatio(), 0.04)
				* pow(aircraft.getCanard().getVolumetricRatio(), 0.3)
				* pow(aircraft.getCanard().getSymmetricFlaps().get(0).getMeanChordRatio(), 0.4),
				SI.KILOGRAM);
		
	}

	/*
	 * Same as HTail
	 */
	public static Amount<Mass> calcuateCanardMassRoskam (Aircraft aircraft) {
		
		return Amount.valueOf(
				aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2)
				* (((3.81
						* aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(NonSI.KNOT)
						* pow(aircraft.getCanard().getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.2)
						) 
						/ (1000*sqrt(cos(aircraft.getCanard().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))))  
						)
						- 0.287
						),
				NonSI.POUND).to(SI.KILOGRAM);
		
	}
	
}

