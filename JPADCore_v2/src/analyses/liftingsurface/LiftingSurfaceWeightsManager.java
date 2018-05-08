package analyses.liftingsurface;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import calculators.geometry.LSGeometryCalc;
import calculators.weights.LiftingSurfaceWeightCalc;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

public class LiftingSurfaceWeightsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Amount<Mass> _mass;
	private Amount<Mass> _massEstimated;
	private Amount<Mass> _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap; 
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;       
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public LiftingSurfaceWeightsManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._massMap = new HashMap<>();
		this._methodsMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateMass(Aircraft aircraft, ComponentEnum liftingSurfaceType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		if (liftingSurfaceType.equals(ComponentEnum.WING)) {
			calculateMass(aircraft, MethodEnum.ROSKAM);
			calculateMass(aircraft, MethodEnum.KROO);
			calculateMass(aircraft, MethodEnum.JENKINSON);
			calculateMass(aircraft, MethodEnum.RAYMER);
			calculateMass(aircraft, MethodEnum.SADRAEY);
			calculateMass(aircraft, MethodEnum.TORENBEEK_1982);
			calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		}
		else if (liftingSurfaceType.equals(ComponentEnum.HORIZONTAL_TAIL) || liftingSurfaceType.equals(ComponentEnum.CANARD)) {
			// TODO: DEFINE THE CORRECT METHODOLOGIES FOR EACH LIFTING SURFACE TYPE
		}
		else if (liftingSurfaceType.equals(ComponentEnum.VERTICAL_TAIL)) {
			// TODO: DEFINE THE CORRECT METHODOLOGIES FOR EACH LIFTING SURFACE TYPE
		}
		
		
		
		if(!methodsMapWeights.get(liftingSurfaceType).equals(MethodEnum.AVERAGE)) {
			_percentDifference =  new double[_massMap.size()];
			_massEstimated = _massMap.get(methodsMapWeights.get(liftingSurfaceType));
		}
		else {
			_percentDifference =  new double[_massMap.size()];
			_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference,
					_massMap,
					_percentDifference,
					20.).getMean(), SI.KILOGRAM);
		}
		
	}
	
	/** 
	 * Calculate mass of the generic lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 * @param method
	 */
	private void calculateMass(
			Aircraft aircraft, 
			MethodEnum method) {

//		double surfaceExposed = aircraft.getExposedWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2);
//
		switch(aircraft.getWing().getType()) {
		
		//---------------------------------------------------------------------------------------------------
		case WING : {
			switch (method) {

			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case KROO : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassKroo(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case JENKINSON : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassJenkinson(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case RAYMER : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassRaymer(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case SADRAEY : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassSadraey(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case TORENBEEK_1982 : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassTorenbeek1982(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case TORENBEEK_2013 : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassTorenbeek2013(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));

			}

			default : { }
			}
		} break;
		
		//---------------------------------------------------------------------------------------------------
		case HORIZONTAL_TAIL : {
			switch (method) {
			
			case HOWE : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailMassHowe(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case JENKINSON : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailMassJenkinson(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailMassNicolai(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case RAYMER : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailMassRaymer(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailKroo(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case SADRAEY : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateHTailMassSadraey(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calcuateHTailMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			default : { } break;
			}
		} break;
		
		//---------------------------------------------------------------------------------------------------
		case VERTICAL_TAIL : {
			switch (method) {

			case HOWE : { // 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassHowe(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;


			case JENKINSON : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassJenkinson(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

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

			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				_methodsList.add(method);
				double kv = 1.;
				if (_positionRelativeToAttachment == 1.) { 
					kv = 1 + 0.15*
							(aircraft.getHTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)/
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
				_methodsList.add(method);
				_mass = Amount.valueOf((2.62*surface +
						1.5e-5*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(_liftingSurfaceCreator.getSpan().doubleValue(NonSI.FOOT), 3)*(
										8.0 + 0.44*aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().doubleValue(NonSI.POUND_FORCE)/
										aircraft.getWing().getSurfacePlanform().doubleValue(MyUnits.FOOT2))/
								(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.doubleValue(SI.RADIAN)),2)))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case SADRAEY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
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

			default : { } break;
			}
		} break;
		
		//---------------------------------------------------------------------------------------------------
		case CANARD : {
			switch (method) {
			
			case HOWE : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardMassHowe(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case JENKINSON : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardMassJenkinson(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardMassNicolai(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case RAYMER : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardMassRaymer(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardKroo(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case SADRAEY : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateCanardMassSadraey(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calcuateCanardMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			
			default:
				break;
			
			}
		}
		
		default:
			break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				20.).getFilteredMean(), SI.KILOGRAM);

		_mass = _massEstimated.to(SI.KILOGRAM);

	}
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	
	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> _massEstimated) {
		this._massEstimated = _massEstimated;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
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

	public List<MethodEnum> getMethodsList() {
		return _methodsList;
	}

	public void setMethodsList(List<MethodEnum> _methodsList) {
		this._methodsList = _methodsList;
	}

	public double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}
	
}
