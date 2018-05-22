package analyses.landinggears;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.weights.LandingGearsWeightCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class LandingGearsWeightManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Amount<Mass> _mass;
	private Amount<Mass> _massEstimated;
	private Amount<Mass> _mainGearMassEstimated;
	private Amount<Mass> _frontGearMassEstimated;
	private Amount<Mass> _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <MethodEnum, Amount<Mass>> _mainGearMassMap;
	private Map <MethodEnum, Amount<Mass>> _frontGearMassMap;
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;       
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public LandingGearsWeightManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._massMap = new HashMap<>();
		this._mainGearMassMap = new HashMap<>();
		this._frontGearMassMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateMass(aircraft, MethodEnum.TORENBEEK_1976);
		
		if(!methodsMapWeights.get(ComponentEnum.LANDING_GEAR).equals(MethodEnum.AVERAGE)) { 
			_percentDifference =  new double[_massMap.size()];
			_massEstimated = _massMap.get(methodsMapWeights.get(ComponentEnum.LANDING_GEAR));
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
	
	public void calculateMass(Aircraft aircraft, MethodEnum method) {

		switch (method){
		
		case TORENBEEK_2013 : {
			_methodsList.add(method);
			_mainGearMassEstimated = LandingGearsWeightCalc.calculateMainGearMassTorenbeek1976(aircraft);
			_frontGearMassEstimated = LandingGearsWeightCalc.calculateFrontGearMassTorenbeek1976(aircraft);
			_mass = _mainGearMassEstimated.to(SI.KILOGRAM).plus(_frontGearMassEstimated.to(SI.KILOGRAM));
			_massMap.put(method, Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			_mainGearMassMap.put(method, Amount.valueOf(round(_mainGearMassEstimated.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
			_frontGearMassMap.put(method, Amount.valueOf(round(_frontGearMassEstimated.doubleValue(SI.KILOGRAM)), SI.KILOGRAM));
		} break;

		default : { } break;

		}
		
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

	public Amount<Mass> getMainGearMassEstimated() {
		return _mainGearMassEstimated;
	}

	public void setMainGearMassEstimated(Amount<Mass> _mainGearMassEstimated) {
		this._mainGearMassEstimated = _mainGearMassEstimated;
	}

	public Amount<Mass> getFrontGearMassEstimated() {
		return _frontGearMassEstimated;
	}

	public void setFrontGearMassEstimated(Amount<Mass> _frontGearMassEstimated) {
		this._frontGearMassEstimated = _frontGearMassEstimated;
	}

	public Map <MethodEnum, Amount<Mass>> getMainGearMassMap() {
		return _mainGearMassMap;
	}

	public void setMainGearMassMap(Map <MethodEnum, Amount<Mass>> _mainGearMassMap) {
		this._mainGearMassMap = _mainGearMassMap;
	}

	public Map <MethodEnum, Amount<Mass>> getFrontGearMassMap() {
		return _frontGearMassMap;
	}

	public void setFrontGearMassMap(Map <MethodEnum, Amount<Mass>> _frontGearMassMap) {
		this._frontGearMassMap = _frontGearMassMap;
	}
	
}
