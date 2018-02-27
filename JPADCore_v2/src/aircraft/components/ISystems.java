package aircraft.components;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.PrimaryElectricSystemsEnum;


@FreeBuilder
public interface ISystems {

	String getId();
	PrimaryElectricSystemsEnum getPrimaryElectricSystemsType();
	
	//-------------------------------------------------------------------------------------------
	// APU
	Amount<Length> getXApexConstructionAxesAPU(); 
	Amount<Length> getYApexConstructionAxesAPU(); 
	Amount<Length> getZApexConstructionAxesAPU();
	//-------------------------------------------------------------------------------------------
	// AIR CONDITIONING AND ANTI-ICING
	Amount<Length> getXApexConstructionAxesAirConditioningAndAntiIcing(); 
	Amount<Length> getYApexConstructionAxesAirConditioningAndAntiIcing(); 
	Amount<Length> getZApexConstructionAxesAirConditioningAndAntiIcing();
	//-------------------------------------------------------------------------------------------
	// ELECTRICAL SYSTEMS
	Amount<Length> getXApexConstructionAxesElectricalSystems(); 
	Amount<Length> getYApexConstructionAxesElectricalSystems(); 
	Amount<Length> getZApexConstructionAxesElectricalSystems();
	//-------------------------------------------------------------------------------------------
	// INSTRUMENT AND NAVIGATION
	Amount<Length> getXApexConstructionAxesInstrumentsAndNavigation(); 
	Amount<Length> getYApexConstructionAxesInstrumentsAndNavigation(); 
	Amount<Length> getZApexConstructionAxesInstrumentsAndNavigation();
	//-------------------------------------------------------------------------------------------
	// CONTROL SURFACES 
	Amount<Length> getXApexConstructionAxesControlSurface(); 
	Amount<Length> getYApexConstructionAxesControlSurface(); 
	Amount<Length> getZApexConstructionAxesControlSurface();
	//-------------------------------------------------------------------------------------------
	// FURNISHINGS AND EQUIPMENTS
	Amount<Length> getXApexConstructionAxesFurnishingsAndEquipment(); 
	Amount<Length> getYApexConstructionAxesFurnishingsAndEquipment(); 
	Amount<Length> getZApexConstructionAxesFurnishingsAndEquipment();
	//-------------------------------------------------------------------------------------------
	// HYDRAULIC AND PNEUMATIC
	Amount<Length> getXApexConstructionAxesHydraulicAndPneumatic(); 
	Amount<Length> getYApexConstructionAxesHydraulicAndPneumatic(); 
	Amount<Length> getZApexConstructionAxesHydraulicAndPneumatic();
	
	class Builder extends ISystems_Builder  { 
		 public Builder() {

				//-------------------------------------------------------------------------------------------
				// APU
				setXApexConstructionAxesAPU(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesAPU(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesAPU(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// AIR CONDITIONING AND ANTI-ICING
				setXApexConstructionAxesAirConditioningAndAntiIcing(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesAirConditioningAndAntiIcing(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesAirConditioningAndAntiIcing(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// ELECTRICAL SYSTEMS
				setXApexConstructionAxesElectricalSystems(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesElectricalSystems(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesElectricalSystems(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// INSTRUMENT AND NAVIGATION
				setXApexConstructionAxesInstrumentsAndNavigation(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesInstrumentsAndNavigation(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesInstrumentsAndNavigation(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// CONTROL SURFACES 
				setXApexConstructionAxesControlSurface(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesControlSurface(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesControlSurface(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// FURNISHINGS AND EQUIPMENTS
				setXApexConstructionAxesFurnishingsAndEquipment(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesFurnishingsAndEquipment(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesFurnishingsAndEquipment(Amount.valueOf(0.0, SI.METER));
				//-------------------------------------------------------------------------------------------
				// HYDRAULIC AND PNEUMATIC
				setXApexConstructionAxesHydraulicAndPneumatic(Amount.valueOf(0.0, SI.METER)); 
				setYApexConstructionAxesHydraulicAndPneumatic(Amount.valueOf(0.0, SI.METER)); 
				setZApexConstructionAxesHydraulicAndPneumatic(Amount.valueOf(0.0, SI.METER));
				
			 
			}
	 }
	
}
