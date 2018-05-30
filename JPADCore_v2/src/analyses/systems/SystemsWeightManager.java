package analyses.systems;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.weights.SystemsWeightCalc;
import configuration.enumerations.ComponentEnum;
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
	private Amount<Mass> _massEstimatedAPU;
	private Amount<Mass> _massEstimatedAirConditioningAndAntiIcing;
	private Amount<Mass> _massEstimatedElectricalSystems;
	private Amount<Mass> _massEstimatedInstrumentsAndNavigation;
	private Amount<Mass> _massEstimatedHydraulicAndPneumatic;
	private Amount<Mass> _massEstimatedFurnishingsAndEquipment;
	private Amount<Mass> _massEstimatedControlSurface;
	private Amount<Mass> _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <MethodEnum, Amount<Mass>> _massMapAPU;
	private Map <MethodEnum, Amount<Mass>> _massMapAirConditioningAndAntiIcing;
	private Map <MethodEnum, Amount<Mass>> _massMapInstrumentsAndNavigationSystem;
	private Map <MethodEnum, Amount<Mass>> _massMapHydraulicAndPneumaticSystems;
	private Map <MethodEnum, Amount<Mass>> _massMapElectricalSystems;
	private Map <MethodEnum, Amount<Mass>> _massMapFurnishingsAndEquipments;
	private Map <MethodEnum, Amount<Mass>> _massMapControlSurfaces;	
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;
	private double[] _percentDifferenceAPU;
	private double[] _percentDifferenceAirConditioningAndAntiIcing;
	private double[] _percentDifferenceInstrumentsAndNavigationSystem;
	private double[] _percentDifferenceHydraulicAndPneumaticSystems;
	private double[] _percentDifferenceElectricalSystems;
	private double[] _percentDifferenceFurnishingsAndEquipments;
	private double[] _percentDifferenceControlSurfaces;
	
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
		this._massMapAPU = new HashMap<>();
		this._massMapAirConditioningAndAntiIcing = new HashMap<>();
		this._massMapInstrumentsAndNavigationSystem = new HashMap<>();
		this._massMapHydraulicAndPneumaticSystems = new HashMap<>();
		this._massMapElectricalSystems = new HashMap<>();
		this._massMapFurnishingsAndEquipments = new HashMap<>();
		this._massMapControlSurfaces = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		calculateAPUMass(aircraft, methodsMapWeights);
		calculateAirConditionAndAntiIcing(aircraft, methodsMapWeights);
		calculateInstrumentAndNavigationMass(aircraft, methodsMapWeights);
		calculateFurnishingsAndEquipmentsMass(aircraft, methodsMapWeights);
		calculateHydraulicAndPneumaticMass(aircraft, methodsMapWeights);
		calculateElectricalSystemsMass(aircraft, methodsMapWeights);
		calculateControlSurfaceMass(aircraft, methodsMapWeights);
		
		_mass = _massEstimatedAPU.to(SI.KILOGRAM)
				.plus(_massEstimatedAirConditioningAndAntiIcing).to(SI.KILOGRAM)
				.plus(_massEstimatedInstrumentsAndNavigation.to(SI.KILOGRAM))
				.plus(_massEstimatedHydraulicAndPneumatic).to(SI.KILOGRAM)
				.plus(_massEstimatedElectricalSystems.to(SI.KILOGRAM))
				.plus(_massEstimatedFurnishingsAndEquipment.to(SI.KILOGRAM))
				.plus(_massEstimatedControlSurface.to(SI.KILOGRAM));
		
		_massMap.put(
				MethodEnum.ALL, 
				_mass.to(SI.KILOGRAM)
				);
		_percentDifference =  new double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				1000.).getFilteredMean(), SI.KILOGRAM);
		
	}
	
	public void calculateControlSurfaceMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateControlSurfaceMass(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.CONTROL_SURFACES).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceControlSurfaces =  new double[_massMapControlSurfaces.size()];
			_massEstimatedControlSurface = _massMapControlSurfaces.get(methodsMapWeights.get(ComponentEnum.CONTROL_SURFACES));
		}
		else {
			_percentDifferenceControlSurfaces =  new double[_massMapControlSurfaces.size()];
			_massEstimatedControlSurface = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapControlSurfaces,
					_percentDifferenceControlSurfaces,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateControlSurfaceMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_controlSurfaceMass = SystemsWeightCalc.calculateControlSurfaceMassTorenbeek1982(aircraft);
			_massMapControlSurfaces.put(method, _controlSurfaceMass.to(SI.KILOGRAM));
		} break;

		default : {} break;
		}

	}
	
	public void calculateAPUMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		calculateAPUMass(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.APU).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceAPU =  new double[_massMapAPU.size()];
			_massEstimatedAPU = _massMapAPU.get(methodsMapWeights.get(ComponentEnum.APU));
		}
		else {
			_percentDifferenceAPU =  new double[_massMapAPU.size()];
			_massEstimatedAPU = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapAPU,
					_percentDifferenceAPU,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateAPUMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_apuMass = SystemsWeightCalc.calculateAPUMassTorenbeek1982(aircraft);
			_massMapAPU.put(method, _apuMass.to(SI.KILOGRAM));
		} break;

		default : {} break;
		}
		
	}

	public void calculateInstrumentAndNavigationMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		calculateInstrumentAndNavigationMass(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceInstrumentsAndNavigationSystem =  new double[_massMapInstrumentsAndNavigationSystem.size()];
			_massEstimatedInstrumentsAndNavigation = _massMapInstrumentsAndNavigationSystem.get(methodsMapWeights.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION));
		}
		else {
			_percentDifferenceInstrumentsAndNavigationSystem =  new double[_massMapInstrumentsAndNavigationSystem.size()];
			_massEstimatedInstrumentsAndNavigation = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapInstrumentsAndNavigationSystem,
					_percentDifferenceInstrumentsAndNavigationSystem,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateInstrumentAndNavigationMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_instrumentsAndNavigationMass = SystemsWeightCalc.calculateInstrumentsAndNavigationMassTorenbeek1982(aircraft);
			_massMapInstrumentsAndNavigationSystem.put(method, _instrumentsAndNavigationMass.to(SI.KILOGRAM));
		} break;

		default : {} break;
		}
		
	}

	public void calculateElectricalSystemsMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateElectricalSystemsMass(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.ELECTRICAL_SYSTEMS).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceElectricalSystems =  new double[_massMapElectricalSystems.size()];
			_massEstimatedElectricalSystems = _massMapElectricalSystems.get(methodsMapWeights.get(ComponentEnum.ELECTRICAL_SYSTEMS));
		}
		else {
			_percentDifferenceElectricalSystems =  new double[_massMapElectricalSystems.size()];
			_massEstimatedElectricalSystems = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapElectricalSystems,
					_percentDifferenceElectricalSystems,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateElectricalSystemsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_electricalSystemsMass = SystemsWeightCalc.calculateElectricalSystemsMassTorenbeek1982(aircraft);
			_massMapElectricalSystems.put(method, _electricalSystemsMass.to(SI.KILOGRAM));
		} break;
		
		default : {} break;
		}
		
	}

	public void calculateAirConditionAndAntiIcing(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		calculateAirConditionAndAntiIcing(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceAirConditioningAndAntiIcing =  new double[_massMapAirConditioningAndAntiIcing.size()];
			_massEstimatedAirConditioningAndAntiIcing = _massMapAirConditioningAndAntiIcing.get(methodsMapWeights.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING));
		}
		else {
			_percentDifferenceAirConditioningAndAntiIcing =  new double[_massMapAirConditioningAndAntiIcing.size()];
			_massEstimatedAirConditioningAndAntiIcing = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapAirConditioningAndAntiIcing,
					_percentDifferenceAirConditioningAndAntiIcing,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateAirConditionAndAntiIcing(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_airConditioningAndAntiIcingMass = SystemsWeightCalc.calculateAirConditionAndAntiIcingTorenbeek1982(aircraft);
			_massMapAirConditioningAndAntiIcing.put(method, _airConditioningAndAntiIcingMass.to(SI.KILOGRAM));
		} break;

		default : {} break;
		}
		
	}

	public void calculateFurnishingsAndEquipmentsMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateFurnishingsAndEquipmentsMass(aircraft, MethodEnum.TORENBEEK_1982);
		calculateFurnishingsAndEquipmentsMass(aircraft, MethodEnum.TORENBEEK_2013);
		
		if(!methodsMapWeights.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceFurnishingsAndEquipments =  new double[_massMapFurnishingsAndEquipments.size()];
			_massEstimatedFurnishingsAndEquipment = _massMapFurnishingsAndEquipments.get(methodsMapWeights.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS));
		}
		else {
			_percentDifferenceFurnishingsAndEquipments =  new double[_massMapFurnishingsAndEquipments.size()];
			_massEstimatedFurnishingsAndEquipment = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapFurnishingsAndEquipments,
					_percentDifferenceFurnishingsAndEquipments,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
	public void calculateFurnishingsAndEquipmentsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_furnishingsAndEquipmentMass = SystemsWeightCalc.calculateFurnishingsAndEquipmentsMassTorenbeek1982(aircraft);
			_massMapFurnishingsAndEquipments.put(method, _furnishingsAndEquipmentMass.to(SI.KILOGRAM));
		} break;
		case TORENBEEK_2013:
			_methodsList.add(method);
			_furnishingsAndEquipmentMass = SystemsWeightCalc.calculateFurnishingsAndEquipmentsMassTorenbeek2013(aircraft);
			_massMapFurnishingsAndEquipments.put(method, _furnishingsAndEquipmentMass.to(SI.KILOGRAM));
		default : {} break;
		}
		
	}

	public void calculateHydraulicAndPneumaticMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		calculateHydraulicAndPneumaticMass(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMapWeights.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).equals(MethodEnum.AVERAGE)) { 
			_percentDifferenceHydraulicAndPneumaticSystems =  new double[_massMapHydraulicAndPneumaticSystems.size()];
			_massEstimatedHydraulicAndPneumatic = _massMapHydraulicAndPneumaticSystems.get(methodsMapWeights.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS));
		}
		else {
			_percentDifferenceHydraulicAndPneumaticSystems =  new double[_massMapHydraulicAndPneumaticSystems.size()];
			_massEstimatedHydraulicAndPneumatic = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_massReference, 
					_massMapHydraulicAndPneumaticSystems,
					_percentDifferenceHydraulicAndPneumaticSystems,
					1000.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}

	public void calculateHydraulicAndPneumaticMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_hydraulicAndPneumaticMass = SystemsWeightCalc.calculateHydraulicAndPneumaticMassTorenbeek1982(aircraft);
			_massMapHydraulicAndPneumaticSystems.put(method, _hydraulicAndPneumaticMass.to(SI.KILOGRAM));
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

	public Map <MethodEnum, Amount<Mass>> getMassMapAPU() {
		return _massMapAPU;
	}

	public void setMassMapAPU(Map <MethodEnum, Amount<Mass>> _massMapAPU) {
		this._massMapAPU = _massMapAPU;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapAirConditioningAndAntiIcing() {
		return _massMapAirConditioningAndAntiIcing;
	}

	public void setMassMapAirConditioningAndAntiIcing(Map <MethodEnum, Amount<Mass>> _massMapAirConditioningAndAntiIcing) {
		this._massMapAirConditioningAndAntiIcing = _massMapAirConditioningAndAntiIcing;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapInstrumentsAndNavigationSystem() {
		return _massMapInstrumentsAndNavigationSystem;
	}

	public void setMassMapInstrumentsAndNavigationSystem(Map <MethodEnum, Amount<Mass>> _massMapInstrumentsAndNavigationSystem) {
		this._massMapInstrumentsAndNavigationSystem = _massMapInstrumentsAndNavigationSystem;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapHydraulicAndPneumaticSystems() {
		return _massMapHydraulicAndPneumaticSystems;
	}

	public void setMassMapHydraulicAndPneumaticSystems(Map <MethodEnum, Amount<Mass>> _massMapHydraulicAndPneumaticSystems) {
		this._massMapHydraulicAndPneumaticSystems = _massMapHydraulicAndPneumaticSystems;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapElectricalSystems() {
		return _massMapElectricalSystems;
	}

	public void setMassMapElectricalSystems(Map <MethodEnum, Amount<Mass>> _massMapElectricalSystems) {
		this._massMapElectricalSystems = _massMapElectricalSystems;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapFurnishingsAndEquipments() {
		return _massMapFurnishingsAndEquipments;
	}

	public void setMassMapFurnishingsAndEquipments(Map <MethodEnum, Amount<Mass>> _massMapFurnishingsAndEquipments) {
		this._massMapFurnishingsAndEquipments = _massMapFurnishingsAndEquipments;
	}

	public Map <MethodEnum, Amount<Mass>> getMassMapControlSurfaces() {
		return _massMapControlSurfaces;
	}

	public void setMassMapControlSurfaces(Map <MethodEnum, Amount<Mass>> _massMapControlSurfaces) {
		this._massMapControlSurfaces = _massMapControlSurfaces;
	}

	public double[] getPercentDifferenceAPU() {
		return _percentDifferenceAPU;
	}

	public void setPercentDifferenceAPU(double[] _percentDifferenceAPU) {
		this._percentDifferenceAPU = _percentDifferenceAPU;
	}

	public double[] getPercentDifferenceAirConditioningAndAntiIcing() {
		return _percentDifferenceAirConditioningAndAntiIcing;
	}

	public void setPercentDifferenceAirConditioningAndAntiIcing(
			double[] _percentDifferenceAirConditioningAndAntiIcing) {
		this._percentDifferenceAirConditioningAndAntiIcing = _percentDifferenceAirConditioningAndAntiIcing;
	}

	public double[] getPercentDifferenceInstrumentsAndNavigationSystem() {
		return _percentDifferenceInstrumentsAndNavigationSystem;
	}

	public void setPercentDifferenceInstrumentsAndNavigationSystem(
			double[] _percentDifferenceInstrumentsAndNavigationSystem) {
		this._percentDifferenceInstrumentsAndNavigationSystem = _percentDifferenceInstrumentsAndNavigationSystem;
	}

	public double[] getPercentDifferenceHydraulicAndPneumaticSystems() {
		return _percentDifferenceHydraulicAndPneumaticSystems;
	}

	public void setPercentDifferenceHydraulicAndPneumaticSystems(
			double[] _percentDifferenceHydraulicAndPneumaticSystems) {
		this._percentDifferenceHydraulicAndPneumaticSystems = _percentDifferenceHydraulicAndPneumaticSystems;
	}

	public double[] getPercentDifferenceElectricalSystems() {
		return _percentDifferenceElectricalSystems;
	}

	public void setPercentDifferenceElectricalSystems(double[] _percentDifferenceElectricalSystems) {
		this._percentDifferenceElectricalSystems = _percentDifferenceElectricalSystems;
	}

	public double[] getPercentDifferenceFurnishingsAndEquipments() {
		return _percentDifferenceFurnishingsAndEquipments;
	}

	public void setPercentDifferenceFurnishingsAndEquipments(double[] _percentDifferenceFurnishingsAndEquipments) {
		this._percentDifferenceFurnishingsAndEquipments = _percentDifferenceFurnishingsAndEquipments;
	}

	public double[] getPercentDifferenceControlSurfaces() {
		return _percentDifferenceControlSurfaces;
	}

	public void setPercentDifferenceControlSurfaces(double[] _percentDifferenceControlSurfaces) {
		this._percentDifferenceControlSurfaces = _percentDifferenceControlSurfaces;
	}

	public Amount<Mass> getMassEstimatedAPU() {
		return _massEstimatedAPU;
	}

	public void setMassEstimatedAPU(Amount<Mass> _massEstimatedAPU) {
		this._massEstimatedAPU = _massEstimatedAPU;
	}

	public Amount<Mass> getMassEstimatedAirConditioningAndAntiIcing() {
		return _massEstimatedAirConditioningAndAntiIcing;
	}

	public void setMassEstimatedAirConditioningAndAntiIcing(Amount<Mass> _massEstimatedAirConditioningAndAntiIcing) {
		this._massEstimatedAirConditioningAndAntiIcing = _massEstimatedAirConditioningAndAntiIcing;
	}

	public Amount<Mass> getMassEstimatedElectricalSystems() {
		return _massEstimatedElectricalSystems;
	}

	public void setMassEstimatedElectricalSystems(Amount<Mass> _massEstimatedElectricalSystems) {
		this._massEstimatedElectricalSystems = _massEstimatedElectricalSystems;
	}

	public Amount<Mass> getMassEstimatedInstrumentsAndNavigation() {
		return _massEstimatedInstrumentsAndNavigation;
	}

	public void setMassEstimatedInstrumentsAndNavigation(Amount<Mass> _massEstimatedInstrumentsAndNavigation) {
		this._massEstimatedInstrumentsAndNavigation = _massEstimatedInstrumentsAndNavigation;
	}

	public Amount<Mass> getMassEstimatedHydraulicAndPneumatic() {
		return _massEstimatedHydraulicAndPneumatic;
	}

	public void setMassEstimatedHydraulicAndPneumatic(Amount<Mass> _massEstimatedHydraulicAndPneumatic) {
		this._massEstimatedHydraulicAndPneumatic = _massEstimatedHydraulicAndPneumatic;
	}

	public Amount<Mass> getMassEstimatedFurnishingsAndEquipment() {
		return _massEstimatedFurnishingsAndEquipment;
	}

	public void setMassEstimatedFurnishingsAndEquipment(Amount<Mass> _massEstimatedFurnishingsAndEquipment) {
		this._massEstimatedFurnishingsAndEquipment = _massEstimatedFurnishingsAndEquipment;
	}

	public Amount<Mass> getMassEstimatedControlSurface() {
		return _massEstimatedControlSurface;
	}

	public void setMassEstimatedControlSurface(Amount<Mass> _massEstimatedControlSurface) {
		this._massEstimatedControlSurface = _massEstimatedControlSurface;
	}
	
}
