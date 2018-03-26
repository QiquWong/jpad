package analyses.systems;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.weights.SystemsWeightCalc;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class SystemsWeightManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Amount<Mass> _mass;
	private Amount<Mass> _apuMass;
	private Amount<Mass> _airConditioningAndAntiIcingMass;
	private Amount<Mass> _electricalSystemsMass;
	private Amount<Mass> _instrumentsAndNavigationMass;
	private Amount<Mass> _controlSurfaceMass;
	private Amount<Mass> _furnishingsAndEquipmentMass;
	private Amount<Mass> _hydraulicAndPneumaticMass;
	private Amount<Mass> _massEstimated;
	private Amount<Mass> _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap; 
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;       
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public SystemsWeightManager () {
		
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
	
	public void calculateMass(Aircraft aircraft, MethodEnum method) {

		calculateAPUMass(aircraft, method);
		calculateAirConditionAndAntiIcing(aircraft, method);
		calculateInstrumentAndNavigationMass(aircraft, method);
		calculateFurnishingsAndEquipmentsMass(aircraft, method);
		calculateHydraulicAndPneumaticMass(aircraft, method);
		calculateElectricalSystemsMass(aircraft, method);
		calculateControlSurfaceMass(aircraft, method);
		
		_mass = _apuMass.to(SI.KILOGRAM)
				.plus(_airConditioningAndAntiIcingMass).to(SI.KILOGRAM)
				.plus(_instrumentsAndNavigationMass.to(SI.KILOGRAM))
				.plus(_furnishingsAndEquipmentMass).to(SI.KILOGRAM)
				.plus(_electricalSystemsMass.to(SI.KILOGRAM))
				.plus(_hydraulicAndPneumaticMass.to(SI.KILOGRAM))
				.plus(_controlSurfaceMass.to(SI.KILOGRAM));
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_massMap.put(
				MethodEnum.TORENBEEK_1982, 
				Amount.valueOf(round(_mass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM)
				);
		_percentDifference =  new double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				30.).getFilteredMean(), SI.KILOGRAM);
		
	}
	
	public void calculateControlSurfaceMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case JENKINSON : {
			_controlSurfaceMass = SystemsWeightCalc.calculateControlSurfaceMassJenkinson(aircraft);
		} break;

		case TORENBEEK_1982 : {
			_controlSurfaceMass = SystemsWeightCalc.calculateControlSurfaceMassTorenbeek1982(aircraft);
		} break;

		default : {} break;
		}

	}
	
	public void calculateAPUMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_apuMass = SystemsWeightCalc.calculateAPUMassTorenbeek1982(aircraft);
		} break;

		default : {} break;
		}
		
	}

	public void calculateInstrumentAndNavigationMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_instrumentsAndNavigationMass = SystemsWeightCalc.calculateInstrumentsAndNavigationMassTorenbeek1982(aircraft);
		} break;

		default : {} break;
		}
		
	}

	public void calculateElectricalSystemsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_electricalSystemsMass = SystemsWeightCalc.calculateElectricalSystemsMassTorenbeek1982(aircraft);
		} break;
		
		default : {} break;
		}
		
	}

	public void calculateAirConditionAndAntiIcing(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_airConditioningAndAntiIcingMass = SystemsWeightCalc.calculateAirConditionAndAntiIcingTorenbeek1982(aircraft);
		} break;

		default : {} break;
		}
		
	}

	public void calculateFurnishingsAndEquipmentsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_furnishingsAndEquipmentMass = SystemsWeightCalc.calculateFurnishingsAndEquipmentsMassTorenbeek1982(aircraft);
		} break;
		case TORENBEEK_2013:  // page 257 Torenbeek 2013
			_furnishingsAndEquipmentMass = SystemsWeightCalc.calculateFurnishingsAndEquipmentsMassTorenbeek2013(aircraft);
		default : {} break;
		}
		
	}

	public void calculateHydraulicAndPneumaticMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_hydraulicAndPneumaticMass = Amount.valueOf(
					0.015*aircraft.getTheAnalysisManager().getTheWeights().getManufacturerEmptyMass().doubleValue(SI.KILOGRAM), 
					SI.KILOGRAM);
		} break;

		default : {} break;
		}
		
	}

	//------------------------------------------------------------------------------
	// GETTER AND SETTERS:
	//------------------------------------------------------------------------------
	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Amount<Mass> getAPUMass() {
		return _apuMass;
	}

	public void setAPUMass(Amount<Mass> _apuMass) {
		this._apuMass = _apuMass;
	}

	public Amount<Mass> getAirConditioningAndAntiIcingMass() {
		return _airConditioningAndAntiIcingMass;
	}

	public void setAirConditioningAndAntiIcingMass(Amount<Mass> _airConditioningAndAntiIcingMass) {
		this._airConditioningAndAntiIcingMass = _airConditioningAndAntiIcingMass;
	}

	public Amount<Mass> getElectricalSystemsMass() {
		return _electricalSystemsMass;
	}

	public void setElectricalSystemsMass(Amount<Mass> _electricalSystemsMass) {
		this._electricalSystemsMass = _electricalSystemsMass;
	}

	public Amount<Mass> getInstrumentsAndNavigationMass() {
		return _instrumentsAndNavigationMass;
	}

	public void setInstrumentsAndNavigationMass(Amount<Mass> _instrumentsAndNavigationMass) {
		this._instrumentsAndNavigationMass = _instrumentsAndNavigationMass;
	}

	public Amount<Mass> getControlSurfaceMass() {
		return _controlSurfaceMass;
	}

	public void setControlSurfaceMass(Amount<Mass> _controlSurfaceMass) {
		this._controlSurfaceMass = _controlSurfaceMass;
	}

	public Amount<Mass> getFurnishingsAndEquipmentMass() {
		return _furnishingsAndEquipmentMass;
	}

	public void setFurnishingsAndEquipmentMass(Amount<Mass> _furnishingsAndEquipmentMass) {
		this._furnishingsAndEquipmentMass = _furnishingsAndEquipmentMass;
	}

	public Amount<Mass> getHydraulicAndPneumaticMass() {
		return _hydraulicAndPneumaticMass;
	}

	public void setHydraulicAndPneumaticMass(Amount<Mass> _hydraulicAndPneumaticMass) {
		this._hydraulicAndPneumaticMass = _hydraulicAndPneumaticMass;
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
