package analyses.liftingsurface;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.weights.LiftingSurfaceWeightCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class LiftingSurfaceWeightManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Amount<Mass> _mass;
	private Amount<Mass> _massEstimated;
	private Amount<Mass> _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;       
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public LiftingSurfaceWeightManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._massMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateMass(Aircraft aircraft, ComponentEnum liftingSurfaceType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		if (liftingSurfaceType.equals(ComponentEnum.WING)) {
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.ROSKAM);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.KROO);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.JENKINSON);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.RAYMER);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.SADRAEY);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.TORENBEEK_1982);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.TORENBEEK_2013);
		}
		else if (liftingSurfaceType.equals(ComponentEnum.HORIZONTAL_TAIL)) {
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.HOWE);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.JENKINSON);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.NICOLAI_2013);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.RAYMER);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.KROO);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.SADRAEY);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.ROSKAM);
		}
		else if (liftingSurfaceType.equals(ComponentEnum.VERTICAL_TAIL)) {
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.HOWE);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.JENKINSON);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.RAYMER);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.ROSKAM);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.KROO);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.SADRAEY);
		}
		else if (liftingSurfaceType.equals(ComponentEnum.CANARD)) {
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.HOWE);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.JENKINSON);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.NICOLAI_2013);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.RAYMER);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.KROO);
			calculateMass(aircraft, liftingSurfaceType, MethodEnum.ROSKAM);
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
					100.).getMean(), SI.KILOGRAM);
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
			ComponentEnum liftingSurfaceType,
			MethodEnum method) {

		switch(liftingSurfaceType) {
		
		//---------------------------------------------------------------------------------------------------
		case WING : {
			switch (method) {

			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;
			
			case KROO : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassKroo(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;
			
			case JENKINSON : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassJenkinson(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case RAYMER : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassRaymer(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case SADRAEY : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassSadraey(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case TORENBEEK_1982 : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassTorenbeek1982(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case TORENBEEK_2013 : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateWingMassTorenbeek2013(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));

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
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
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
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;
			
			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calcuateHTailMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
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

			case RAYMER : {  
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassRaymer(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case ROSKAM : {  
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case KROO : {
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassKroo(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;

			case SADRAEY : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calculateVTailMassSadraey(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
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
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
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

			case ROSKAM : { 
				_methodsList.add(method);
				_mass = LiftingSurfaceWeightCalc.calcuateCanardMassRoskam(aircraft);
				_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			} break;
			
			default:
				break;
			
			}
		}
		
		default:
			break;
		}

		_percentDifference =  new double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				1000.).getFilteredMean(), SI.KILOGRAM);

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
