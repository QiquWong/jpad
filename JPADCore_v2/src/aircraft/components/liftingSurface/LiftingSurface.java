package aircraft.components.liftingSurface;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import analyses.liftingsurface.LSAerodynamicsManager;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

/**
 * The LiftingSurface class is in charge of handling each lifting surface parameters (Wing, Horizontal Tail, Vertical Tail and Canard).
 * It contains the LiftingSurface object which manages all the geometrical parameters. 
 * It manages the components position in BRF (body reference frame) as well as the mass and center of gravity position.
 * 
 * @author Vittorio Trifari
 *
 */
public class LiftingSurface {

	//----------------------------------------------------------------------
	// VARIABLE DECLARATIONS
	//----------------------------------------------------------------------
	private Double _positionRelativeToAttachment;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _riggingAngle = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
	
	private AerodynamicDatabaseReader _aeroDatabaseReader;
	private HighLiftDatabaseReader _highLiftDatabaseReader;
	private VeDSCDatabaseReader _veDSCDatabaseReader;
	
	private LiftingSurface _exposedLiftingSurface;
	private LiftingSurfaceCreator _liftingSurfaceCreator;
	
	private int _numberOfEngineOverTheWing = 0; 
	
	private Map<ConditionEnum, LSAerodynamicsManager> _theAerodynamicsCalculatorMap;
	
	private Double _massCorrectionFactor = 1.0;
	private Amount<Mass> _mass, _massReference, _massEstimated;
	Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	Double[] _percentDifference;
	
	private CenterOfGravity _cg;
	private Amount<Length> _xCG, _yCG, _zCG;
	Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Double[] _percentDifferenceXCG;
	Double[] _percentDifferenceYCG;
	
	//------------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------------
	public LiftingSurface (LiftingSurfaceCreator theLiftingSurfaceCreator) {
		
		this._liftingSurfaceCreator = theLiftingSurfaceCreator;
		
	}
	
	//------------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------------
	public void calculateMass(Aircraft aircraft, ComponentEnum liftingSurfaceType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		calculateMass(aircraft, MethodEnum.KROO);
		calculateMass(aircraft, MethodEnum.JENKINSON);
		calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, MethodEnum.RAYMER);
//		calculateMass(aircraft, MethodEnum.NICOLAI_2013);
//		calculateMass(aircraft, MethodEnum.HOWE);
//		calculateMass(aircraft, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, MethodEnum.SADRAY);
		calculateMass(aircraft, MethodEnum.ROSKAM);
		
		if(!methodsMapWeights.get(liftingSurfaceType).equals(MethodEnum.AVERAGE))
			_massEstimated = _massMap.get(methodsMapWeights.get(liftingSurfaceType));
		else
			_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					this.getMassReference(), 
					_massMap,
					_percentDifference,
					20.).getMean(), SI.KILOGRAM);
		
	}
	
	/** 
	 * Calculate mass of the generic lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 * @param method
	 */
	@SuppressWarnings("unused")
	private void calculateMass(
			Aircraft aircraft, 
			MethodEnum method) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		double surface = _liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2);
		double surfaceExposed = aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(MyUnits.FOOT2);

		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(aircraft.getWing().getLiftingSurfaceCreator());
		double thicknessMean = meanAirfoil.getThicknessToChordRatio();
		
		Amount<Angle> sweepStructuralAxis;
		if(_liftingSurfaceCreator.getTheLiftingSurfaceInterface().getType() == ComponentEnum.WING)
			sweepStructuralAxis = Amount.valueOf(
					Math.atan(
							Math.tan(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN))
							- (4./_liftingSurfaceCreator.getAspectRatio())*
							(_liftingSurfaceCreator.getTheLiftingSurfaceInterface().getMainSparDimensionlessPosition()
									*(1 - _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio())
									/(1 + _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio()))
							),
					1e-9, // precision
					SI.RADIAN);
		else
			sweepStructuralAxis = Amount.valueOf(
					Math.atan(
							Math.tan(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN))
							- (4./_liftingSurfaceCreator.getAspectRatio())*
							(0.25
									*(1 - _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio())
									/(1 + _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio()))
							),
					1e-9, // precision
					SI.RADIAN);
		
		switch(_liftingSurfaceCreator.getTheLiftingSurfaceInterface().getType()) {
		case WING : {
			switch (method) {

			/* This method poor results
			 * */
			case ROSKAM : { // Roskam page 85 (pdf) part V
				methodsList.add(method);

				_mass = Amount.valueOf(
						Amount.valueOf(2*(0.00428*
								Math.pow(surface, 0.48)*_liftingSurfaceCreator.getAspectRatio()*
								Math.pow(aircraft.getTheAnalysisManager().getMachDive0(), 0.43)*
								Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(NonSI.POUND_FORCE)
										*aircraft.getTheAnalysisManager().getNUltimate(), 0.84)*
								Math.pow(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.14))/
								(Math.pow(100*_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(),0.76)*
										Math.pow(Math.cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().doubleValue(SI.RADIAN)), 1.54)),
								NonSI.POUND_FORCE).to(NonSI.KILOGRAM_FORCE).getEstimatedValue(),
						SI.KILOGRAM);

				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			//			
			case KROO : { // page 430 Aircraft design synthesis
				methodsList.add(method);

				_mass = Amount.valueOf((4.22*surface +
						1.642e-6*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(_liftingSurfaceCreator.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*
								Math.sqrt(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)*
										aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(NonSI.POUND))*
								(1 + 2*this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio()))/
						(thicknessMean*Math.pow(Math.cos(this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)),2)*
								surface*(1 + this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio()))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				
			} break;

			case JENKINSON : { // page 134 Jenkinson - Civil Jet Aircraft Design

				methodsList.add(method);

				if (!aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) { 

					// TODO : FIND A WAY TO SEE THE EFFECT OF NACELLE POSITION ALSO FOR TURBOPROP

					double R, kComp;

					if (_massCorrectionFactor != null) {
						kComp = _massCorrectionFactor;
					} else {
						kComp = 0.;					
					}

					for (int i = 0; i < 10; i++) {

						try {
							if(aircraft.getPowerPlant().getMountingPosition().equals(EngineMountingPositionEnum.WING)) 
								R = _mass.doubleValue(SI.KILOGRAM) + aircraft.getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM) +
								((2*(aircraft.getNacelles().getTotalMass().doubleValue(SI.KILOGRAM) + 
										aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().doubleValue(SI.KILOGRAM))*
										aircraft.getNacelles().getDistanceBetweenInboardNacellesY().doubleValue(SI.METER))/
										(0.4*_liftingSurfaceCreator.getSpan().doubleValue(SI.METER))) + 
								((2*(aircraft.getNacelles().getTotalMass().doubleValue(SI.KILOGRAM) + 
										aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().doubleValue(SI.KILOGRAM))*
										aircraft.getNacelles().getDistanceBetweenOutboardNacellesY().doubleValue(SI.METER))/
										(0.4*_liftingSurfaceCreator.getSpan().getEstimatedValue()));
							else
								R = 0.0;
						} catch(NullPointerException e) {R = 0.;}

						_mass = Amount.valueOf(
								(1 - kComp) * 0.021265*
								(pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)*
										aircraft.getTheAnalysisManager().getNUltimate(), 0.4843)*
										pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 0.7819)*
										pow(_liftingSurfaceCreator.getAspectRatio(), 0.993)*
										pow(1 + _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio(),0.4)*
										pow(1 - R/aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM),0.4))/
								(cos(this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN))*
										pow(thicknessMean,0.4)), 
								SI.KILOGRAM);
					}
					_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				} else {
					_mass = null;
					_massMap.put(method, null);
				}
			} break;

			/* This method gives poor results
			 * 
			 * */
			case RAYMER : { // page 403 (211 pdf) Raymer 
				methodsList.add(method);
				_mass = Amount.valueOf(0.0051 * pow(aircraft.getTheAnalysisManager().getTheWeights().
						getMaximumTakeOffWeight().doubleValue(NonSI.POUND_FORCE) * aircraft.getTheAnalysisManager().
								getNUltimate(),
						0.557)*
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2),0.649)*
						pow(_liftingSurfaceCreator.getAspectRatio(), 0.5)*
						pow(_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(), -0.4)*
						pow(1 + _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.1)*
						pow(cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1)*
						pow(_liftingSurfaceCreator.getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2), 0.1), NonSI.POUND).
						to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case SADRAY : { // page 583 pdf Sadray Aircraft Design System Engineering Approach
				// results very similar to Jenkinson
				methodsList.add(method);
				Double _kRho = 0.0035;
				_mass = Amount.valueOf(
						_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*
						_liftingSurfaceCreator.getMeanAerodynamicChord().doubleValue(SI.METER)* //
						(_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio())*
						aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)*
						_kRho*
						pow((_liftingSurfaceCreator.getAspectRatio()*aircraft.getTheAnalysisManager().getNUltimate())/
								cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)),0.6)*
						pow(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.04), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* The method gives an average 20 percent difference from real value 
			 */
			case TORENBEEK_1982 : {
				methodsList.add(method);
				_mass = Amount.valueOf(
						0.0017*
						aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().doubleValue(NonSI.POUND_FORCE)*
						Math.pow(_liftingSurfaceCreator.getSpan().to(NonSI.FOOT).getEstimatedValue()/
								Math.cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN)),0.75)*
						(1 + Math.pow(6.3*Math.cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))/
								_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), 0.5))*
						Math.pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.55)*
						Math.pow(
								_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT)*surface/
								(_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*_liftingSurfaceCreator.getPanels().get(0).getChordRoot().doubleValue(NonSI.FOOT)*
										aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().doubleValue(NonSI.POUND_FORCE)*
										Math.cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))), 0.3)
						, NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case TORENBEEK_2013 : { // page 253 pdf
				methodsList.add(method);

				_mass = Amount.valueOf(
						(0.0013*
								aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(SI.NEWTON)*
										(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().doubleValue(SI.NEWTON)), 
										0.5)*
								0.36*Math.pow(1 + this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(), 0.5)*
								(_liftingSurfaceCreator.getSpan().doubleValue(SI.METER)/100)*
								(_liftingSurfaceCreator.getAspectRatio()/
										(thicknessMean
												*Math.pow(
														Math.cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN))
														, 2))) +
								210*_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE))/
						AtmosphereCalc.g0.getEstimatedValue()
						, SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));

			}

			default : { }
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case HORIZONTAL_TAIL : {
			switch (method) {

			/*
			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				_methodsList.add(method);
				_mass = Amount.valueOf(0.047*
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue()*
						pow(_surface.getEstimatedValue(), 1.24), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case JENKINSON : { // Jenkinson page 149 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(22*_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				methodsList.add(method);
				double gamma = pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()*
						aircraft.getTheAnalysisManager().getNUltimate(), 0.813)*
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.584)*
						pow(_liftingSurfaceCreator.getSpan().doubleValue(SI.METER)/
								(_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*_liftingSurfaceCreator.getPanels().get(0).getChordRoot().doubleValue(SI.METER)), 0.033) * 
						pow(_liftingSurfaceCreator.getMeanAerodynamicChord().doubleValue(SI.METER)/
								_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(SI.METER), 0.28);

				_mass = Amount.valueOf(0.0034 * 
						pow(gamma, 0.915), NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case RAYMER : { // Raymer page 211 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(0.0379 * 
						pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.639)*
						pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.1) * 
						pow(_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), -1.) *
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.75) * 
						pow(0.3*_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), 0.704) * 
						pow(cos(_liftingSurfaceCreator.getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1) *
						pow(_liftingSurfaceCreator.getAspectRatio(), 0.166) * 
						pow(1 + aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().doubleValue(NonSI.FOOT)/
								_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), -0.25) * 
						pow(1 + _liftingSurfaceCreator.getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2)/
								_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.1),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((5.25*aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE) +
						0.8e-6*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), 3)*
								aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)*
								_liftingSurfaceCreator.getMeanAerodynamicChord().doubleValue(NonSI.FOOT)*
								Math.sqrt(aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)))/
						(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)*
								_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT)*Math.pow(surface,1.5))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.0275;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.3)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			/*
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				_methodsList.add(method);
				double kh = 1.;
				if (_variableIncidence == true) { kh = 1.1;}

				_mass = Amount.valueOf(kh*3.81*
						aircraft.get_performances().get_vDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			default : { } break;
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case VERTICAL_TAIL : {
			switch (method) {

			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				methodsList.add(method);
				double k = 0.;
				if (_positionRelativeToAttachment == 1.0) {
					k = 1.5;
				} else {
					k = 1.;
				}
				_mass = Amount.valueOf(0.065*k*
						aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(SI.METERS_PER_SECOND)*
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 1.15), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;


			case JENKINSON : {
				methodsList.add(method);
				_mass = Amount.valueOf(22*_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case RAYMER : { // Raymer page 211 pdf
				_methodsList.add(method);
				_mass = Amount.valueOf(0.0026 * 
						pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.556)*
						pow(aircraft.get_performances().get_nUltimate(), 0.536) * 
						pow(_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), -0.5) *
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.5) * 
						pow(0.3*_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), 0.875) * 
						pow(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()), -1.) *
						pow(_aspectRatio, 0.35) * 
						pow(_tc_root, -0.5) *
						pow(1 + _positionRelativeToAttachment, 0.225),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				methodsList.add(method);
				double kv = 1.;
				if (_positionRelativeToAttachment == 1.) { 
					kv = 1 + 0.15*
							(aircraft.getHTail().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)/
									_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE));}
				_mass = Amount.valueOf(kv*3.81*
						aircraft.getTheAnalysisManager().getVDiveEAS().doubleValue(NonSI.KNOT)*
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 1.2)/
						(1000*sqrt(cos(_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN)))) 
								- 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((2.62*surface +
						1.5e-5*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), 3)*(
										8.0 + 0.44*aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(NonSI.POUND_FORCE)/
										aircraft.getWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(MyUnits.FOOT2))/
								(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.05;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.2)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			}break;
			 */

			default : { } break;
			}
		} break;
		case CANARD : {
			switch (method) {

			/*
			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				_methodsList.add(method);
				_mass = Amount.valueOf(0.047*
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue()*
						pow(_surface.getEstimatedValue(), 1.24), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case JENKINSON : { // Jenkinson page 149 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(22*_liftingSurfaceCreator.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				methodsList.add(method);
				double gamma = pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)*
						aircraft.getTheAnalysisManager().getNUltimate(), 0.813)*
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.584)*
						pow(_liftingSurfaceCreator.getSpan().doubleValue(SI.METER)/
								(_liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*_liftingSurfaceCreator.getPanels().get(0).getChordRoot().doubleValue(SI.METER)), 0.033) * 
						pow(_liftingSurfaceCreator.getMeanAerodynamicChord().doubleValue(SI.METER)/
								_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(SI.METER), 0.28);

				_mass = Amount.valueOf(0.0034 * 
						pow(gamma, 0.915), NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case RAYMER : { // Raymer page 211 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(0.0379 * 
						pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND), 0.639)*
						pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.1) * 
						pow(_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), -1.) *
						pow(_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.75) * 
						pow(0.3*_liftingSurfaceCreator.getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT), 0.704) * 
						pow(cos(_liftingSurfaceCreator.getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)), -1) *
						pow(_liftingSurfaceCreator.getAspectRatio(), 0.166) * 
						pow(1 + aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().doubleValue(NonSI.FOOT)/
								_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), -0.25) * 
						pow(1 +_liftingSurfaceCreator.getTotalControlSurfaceArea().doubleValue(MyUnits.FOOT2)/
								_liftingSurfaceCreator.getSurfacePlanform().doubleValue(MyUnits.FOOT2), 0.1),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((5.25*aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE) +
						0.8e-6*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT),3)*
								aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(NonSI.POUND)*
								this.getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(NonSI.FOOT)*
								Math.sqrt(aircraft.getExposedWing().getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)))/
						(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)*
								this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().doubleValue(NonSI.FOOT)*Math.pow(surface,1.5))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.0275;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.3)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			/*
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				_methodsList.add(method);
				double kh = 1.;
				if (_variableIncidence == true) { kh = 1.1;}

				_mass = Amount.valueOf(kh*3.81*
						aircraft.get_performances().get_vDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			default : { } break;
			}
		} break;
		default:
			break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				20.).getFilteredMean(), SI.KILOGRAM);

		if ((_massCorrectionFactor != null) && (_massEstimated != null)) {
			_massEstimated = _massEstimated.times(_massCorrectionFactor);
		}

		_mass = _massEstimated.to(SI.KILOGRAM);

	}

	public void calculateCG(ComponentEnum type, Map<ComponentEnum, MethodEnum> methodsMap) {
//		calculateCG(MethodEnum.SFORZA, type);
		calculateCG(MethodEnum.TORENBEEK_1982, type);
		
		if(!methodsMap.get(type).equals(MethodEnum.AVERAGE)) { 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(type)));
			_cg.setYLRF(_yCGMap.get(methodsMap.get(type)));
		}
		else {
			_percentDifferenceXCG = new Double[_xCGMap.size()];
			_percentDifferenceYCG = new Double[_yCGMap.size()];

			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					100.).getFilteredMean(), SI.METER));

			_cg.setYLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getYLRFref(), 
					_yCGMap,
					_percentDifferenceYCG,
					100.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(type);
	}
	
	private void calculateCG(MethodEnum method, ComponentEnum type) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes,
						 _yApexConstructionAxes,
						 _zApexConstructionAxes
				);

		_cg.set_xLRFref(_liftingSurfaceCreator.getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
		_cg.set_yLRFref(_liftingSurfaceCreator.getSpan().to(SI.METER).times(0.5*0.4));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		@SuppressWarnings("unused")
		Double lambda = 0.0;
		if(type.equals(ComponentEnum.WING))
			lambda = _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio();
		else
			lambda = _liftingSurfaceCreator.getPanels().get(0).getTaperRatio();
		Double span = _liftingSurfaceCreator.getSpan().doubleValue(SI.METER);
		Double xRearSpar = _liftingSurfaceCreator.getSecondarySparDimensionlessPosition();
		Double xFrontSpar = _liftingSurfaceCreator.getMainSparDimensionlessPosition();

		switch (type) {
		case WING : {
			switch(method) {

//			//		 Bad results ...
//			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
//				methodsList.add(method);
//				_yCG = Amount.valueOf(
//						(span/6) * 
//						((1+2*lambda)/(1-lambda)),
//						SI.METER);
//
//				_xCG = Amount.valueOf(
//						(_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())/2)
//						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
//						, SI.METER);
//				_xCGMap.put(method, _xCG);
//				_yCGMap.put(method, _yCG);
//			} break;

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.35*(span/2) 
						, SI.METER);

				xRearSpar = 0.6*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER));
				xFrontSpar = 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER));

				_xCG = Amount.valueOf(
						0.7*(xRearSpar - xFrontSpar)
						+ 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
						, SI.METER);

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;

			}

		} break;

		case HORIZONTAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.38*(span/2) 
						, SI.METER);

				_xCG = Amount.valueOf(
						(0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER)))
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
						, SI.METER);

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case VERTICAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);

				if (_positionRelativeToAttachment > 0.8) {
					_yCG = Amount.valueOf(
							0.55*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
							, SI.METER);
				} else {
					_yCG = Amount.valueOf(
							0.38*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
							, SI.METER);
				}

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case CANARD : {

		} break;

		default : {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, methodsList);

	}

	//------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------------
	
	public Double getPositionRelativeToAttachment() {
		return _positionRelativeToAttachment;
	}

	public void setPositionRelativeToAttachment(Double _positionRelativeToAttachment) {
		this._positionRelativeToAttachment = _positionRelativeToAttachment;
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public Amount<Angle> getRiggingAngle() {
		return _riggingAngle;
	}

	public void setRiggingAngle(Amount<Angle> _riggingAngle) {
		this._riggingAngle = _riggingAngle;
	}

	public AerodynamicDatabaseReader getAeroDatabaseReader() {
		return _aeroDatabaseReader;
	}

	public void setAeroDatabaseReader(AerodynamicDatabaseReader _aeroDatabaseReader) {
		this._aeroDatabaseReader = _aeroDatabaseReader;
	}

	public HighLiftDatabaseReader getHighLiftDatabaseReader() {
		return _highLiftDatabaseReader;
	}

	public void setHighLiftDatabaseReader(HighLiftDatabaseReader _highLiftDatabaseReader) {
		this._highLiftDatabaseReader = _highLiftDatabaseReader;
	}

	public VeDSCDatabaseReader getVeDSCDatabaseReader() {
		return _veDSCDatabaseReader;
	}

	public void setVeDSCDatabaseReader(VeDSCDatabaseReader _veDSCDatabaseReader) {
		this._veDSCDatabaseReader = _veDSCDatabaseReader;
	}

	public LiftingSurfaceCreator getLiftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	public void setLiftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}

	public LiftingSurface getExposedLiftingSurface() {
		return _exposedLiftingSurface;
	}

	public void setExposedLiftingSurface(LiftingSurface _exposedLiftingSurface) {
		this._exposedLiftingSurface = _exposedLiftingSurface;
	}

	public int getNumberOfEngineOverTheWing() {
		return _numberOfEngineOverTheWing;
	}

	public void setNumberOfEngineOverTheWing(int _numberOfEngineOverTheWing) {
		this._numberOfEngineOverTheWing = _numberOfEngineOverTheWing;
	}

	public Map<ConditionEnum, LSAerodynamicsManager> getTheAerodynamicsCalculatorMap() {
		return _theAerodynamicsCalculatorMap;
	}

	public void setTheAerodynamicsCalculatorMap(Map<ConditionEnum, LSAerodynamicsManager> _theAerodynamicsCalculatorMap) {
		this._theAerodynamicsCalculatorMap = _theAerodynamicsCalculatorMap;
	}

	public Double getMassCorrectionFactor() {
		return _massCorrectionFactor;
	}

	public void setMassCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> _massEstimated) {
		this._massEstimated = _massEstimated;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public Amount<Length> getXCG() {
		return _xCG;
	}

	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	public Amount<Length> getYCG() {
		return _yCG;
	}

	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	public Amount<Length> getZCG() {
		return _zCG;
	}

	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<MethodEnum, Amount<Length>> getYCGMap() {
		return _yCGMap;
	}

	public void setYCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}

	public Double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void setPercentDifferenceXCG(Double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}

	public Double[] getPercentDifferenceYCG() {
		return _percentDifferenceYCG;
	}

	public void setPercentDifferenceYCG(Double[] _percentDifferenceYCG) {
		this._percentDifferenceYCG = _percentDifferenceYCG;
	}

}
