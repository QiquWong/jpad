package analyses.fuselage;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import calculators.weights.FuselageWeightsCalc;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class FuselageWeightsManager {

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
	public FuselageWeightsManager () {
		
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
	
	public void calculateMass(Aircraft aircraft, OperatingConditions operatingConditions, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateMass(aircraft, operatingConditions, MethodEnum.JENKINSON);
		calculateMass(aircraft, operatingConditions, MethodEnum.NICOLAI_1984);
		calculateMass(aircraft, operatingConditions, MethodEnum.ROSKAM);
		calculateMass(aircraft, operatingConditions, MethodEnum.RAYMER);
		calculateMass(aircraft, operatingConditions, MethodEnum.SADRAY);
		calculateMass(aircraft, operatingConditions, MethodEnum.KROO);
		calculateMass(aircraft, operatingConditions, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, operatingConditions, MethodEnum.TORENBEEK_2013);
		
		if(!methodsMapWeights.get(ComponentEnum.FUSELAGE).equals(MethodEnum.AVERAGE)) { 
			_massEstimated = _massMap.get(methodsMapWeights.get(ComponentEnum.FUSELAGE));
			_percentDifference =  new double[_massMap.size()];
		}
		else {
			_percentDifference =  new double[_massMap.size()];
			_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMap,
					_percentDifference,
					100.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateMass(Aircraft aircraft, OperatingConditions operatingConditions, MethodEnum method) {

		switch (method){
		
		case JENKINSON : { 
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassJenkinson(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		
		case NICOLAI_1984 : {
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassNicolai(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
	
		case ROSKAM : { 
			_methodsList.add(method);
			FuselageWeightsCalc.calculateFuselageMassRoskam(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), _mass.getUnit()));
		} break;
		
		case RAYMER : { 
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassRaymer(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		
		case SADRAY : { 
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassSadray(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		 
		case KROO : { 
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassKroo(aircraft, operatingConditions);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		
		case TORENBEEK_2013 : {
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassTorenbeek2013(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1976 : { 
			_methodsList.add(method);
			_mass = FuselageWeightsCalc.calculateFuselageMassTorenbeek1976(aircraft);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		default : { } break;

		}
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
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
