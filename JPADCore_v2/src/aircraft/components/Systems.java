package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PrimaryElectricSystemsEnum;
import writers.JPADStaticWriteUtils;


public class Systems {

	ISystems _theSystemsInterface;
	
	//------------------------------------------------------------------------------------------
	// OUTPUT DATA
	private Amount<Mass> _apuMass;
	private Amount<Mass> _airConditioningAndAntiIcingMass;
	private Amount<Mass> _electricalSystemsMass;
	private Amount<Mass> _instrumentsAndNavigationMass;
	private Amount<Mass> _controlSurfaceMass;
	private Amount<Mass> _furnishingsAndEquipmentMass;
	private Amount<Mass> _hydraulicAndPneumaticMass;
	private Amount<Mass> _overallMass;

	private Amount<Mass> _referenceMass;
	private Amount<Mass> _meanMass;
	private double[] _percentDifference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

	//------------------------------------------------------------------------------------------
	// BUILDER
	public Systems(ISystems theSystemsInterface) {
		
		this._theSystemsInterface = theSystemsInterface;
		
	}
	
	//------------------------------------------------------------------------------------------
	// METHODS
	public void calculateMass(Aircraft aircraft, MethodEnum method) {

		calculateAPUMass(aircraft, method);
		calculateAirConditionAndAntiIcing(aircraft, method);
		calculateInstrumentAndNavigationMass(aircraft, method);
		calculateFurnishingsAndEquipmentsMass(aircraft, method);
		calculateHydraulicAndPneumaticMass(aircraft, method);
		calculateElectricalSystemsMass(aircraft, method);
		calculateControlSurfaceMass(aircraft, method);
		
		_overallMass = _apuMass.to(SI.KILOGRAM)
				.plus(_airConditioningAndAntiIcingMass).to(SI.KILOGRAM)
				.plus(_instrumentsAndNavigationMass.to(SI.KILOGRAM))
				.plus(_furnishingsAndEquipmentMass).to(SI.KILOGRAM)
				.plus(_electricalSystemsMass.to(SI.KILOGRAM))
				.plus(_hydraulicAndPneumaticMass.to(SI.KILOGRAM))
				.plus(_controlSurfaceMass.to(SI.KILOGRAM));
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_massMap.put(
				MethodEnum.TORENBEEK_1982, 
				Amount.valueOf(round(_overallMass.doubleValue(SI.KILOGRAM)), SI.KILOGRAM)
				);
		_percentDifference =  new double[_massMap.size()]; 

		_meanMass = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_referenceMass, 
				_massMap,
				_percentDifference,
				30.).getFilteredMean(), SI.KILOGRAM);
		
	}

	public void calculateControlSurfaceMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case JENKINSON : {
			_controlSurfaceMass = Amount.valueOf(
					Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().times(0.04).getEstimatedValue(), 0.684),
					SI.KILOGRAM);
		} break;

		case TORENBEEK_1982 : {
			_controlSurfaceMass = Amount.valueOf(
					0.4915*Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue(), 2/3), 
					SI.KILOGRAM);
		} break;

		default : {} break;
		}

	}
	
	public void calculateAPUMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_apuMass = Amount.valueOf(
					2.25*1.17*Math.pow(
							aircraft.getCabinConfiguration().getActualPassengerNumber()*0.5, 
							3/5
							), 
					SI.KILOGRAM);
		} break;

		default : {} break;
		}
		
	}

	public void calculateInstrumentAndNavigationMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case TORENBEEK_1982 : {
			_instrumentsAndNavigationMass = Amount.valueOf(
					0.347*Math.pow(
							aircraft.getTheAnalysisManager().getTheWeights().getManufacturerEmptyMass().doubleValue(SI.KILOGRAM), 
							5/9
							)*Math.pow(
									aircraft.getTheAnalysisManager().getTheWeights().getRange().doubleValue(SI.KILOMETER), 
									0.25
									), 
					SI.KILOGRAM);
		} break;

		default : {} break;
		}
		
	}

	public void calculateElectricalSystemsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			if(_theSystemsInterface.getPrimaryElectricSystemsType().equals(PrimaryElectricSystemsEnum.DC)) {
				_electricalSystemsMass = Amount.valueOf(
						0.02*aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM) + 181,
						SI.KILOGRAM);
			}
			else if(_theSystemsInterface.getPrimaryElectricSystemsType().equals(PrimaryElectricSystemsEnum.AC)) {
				
				double pel = 0.0;
				Amount<Volume> fuselageCabinVolume = Amount.valueOf(
						aircraft.getFuselage().getFuselageCreator().getCylinderSectionArea().doubleValue(SI.SQUARE_METRE)
						*aircraft.getFuselage().getFuselageCreator().getCylinderLength().doubleValue(SI.METER), 
						SI.CUBIC_METRE
						);
				if(fuselageCabinVolume.doubleValue(SI.CUBIC_METRE) < 227)
					pel = fuselageCabinVolume.doubleValue(SI.CUBIC_METRE)*0.565;
				else
					pel = Math.pow(fuselageCabinVolume.doubleValue(SI.CUBIC_METRE), 0.7)*3.64;
				
				_electricalSystemsMass = Amount.valueOf(
						16.3*pel*(1-0.033*Math.sqrt(pel)), 
						SI.KILOGRAM);
			}
		} break;

		default : {} break;
		}
		
	}

	public void calculateAirConditionAndAntiIcing(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_airConditioningAndAntiIcingMass = Amount.valueOf(
					14.0*Math.pow(aircraft.getFuselage().getFuselageCreator().getCylinderLength().doubleValue(SI.METER), 1.28), 
					SI.KILOGRAM);
		} break;

		default : {} break;
		}
		
	}

	public void calculateFurnishingsAndEquipmentsMass(Aircraft aircraft, MethodEnum method) {
		
		switch (method) {
		case TORENBEEK_1982 : {
			_furnishingsAndEquipmentMass = Amount.valueOf(
					0.196*Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM), 0.91), 
					SI.KILOGRAM);
		} break;

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
	
	public void calculateAbsorbedPower(Aircraft aircraft, MethodEnum method) {
		// TODO
	}

	//-------------------------------------------------------------------------------------
	// 
	public ISystems getTheSystemsInterface() {
		return _theSystemsInterface;
	}

	public void setTheSystemsInterface(ISystems _theSystemsInterface) {
		this._theSystemsInterface = _theSystemsInterface;
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

	public Amount<Mass> getOverallMass() {
		return _overallMass;
	}

	public void setOverallMass(Amount<Mass> _overallMass) {
		this._overallMass = _overallMass;
	}

	public Amount<Mass> getMeanMass() {
		return _meanMass;
	}

	public void setMeanMass(Amount<Mass> _meanMass) {
		this._meanMass = _meanMass;
	}

	public double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(double[] _percentDifference) {
		this._percentDifference = _percentDifference;
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

	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	public Amount<Mass> getReferenceMass() {
		return _referenceMass;
	}

	public void setReferenceMass(Amount<Mass> _referenceMass) {
		this._referenceMass = _referenceMass;
	}
	
}