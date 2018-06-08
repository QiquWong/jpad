package analyses;

import javax.annotation.Nullable;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

@FreeBuilder
public interface IACBalanceManager {

	String getId();
	Aircraft getTheAircraft();
	
	// WEIGHTS DATA
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getDesignFuelMass();
	Amount<Mass> getSinglePassengerMass();
	@Nullable
	Amount<Mass> getFuselageMass();
	@Nullable
	Amount<Mass> getWingMass();
	@Nullable
	Amount<Mass> getHTailMass();
	@Nullable
	Amount<Mass> getVTailMass();
	@Nullable
	Amount<Mass> getCanardMass(); 
	@Nullable
	Amount<Mass> getNacellesMass();
	@Nullable
	Amount<Mass> getPowerPlantMass();
	@Nullable
	Amount<Mass> getLandingGearMass();
	@Nullable
	Amount<Mass> getAPUMass();
	@Nullable
	Amount<Mass> getAirConditioningAndAntiIcingMass();
	@Nullable
	Amount<Mass> getInstrumentsAndNavigationSystemMass();
	@Nullable
	Amount<Mass> getHydraulicAndPneumaticSystemsMass();
	@Nullable
	Amount<Mass> getElectricalSystemsMass();
	@Nullable
	Amount<Mass> getControlSurfacesMass();
	@Nullable
	Amount<Mass> getFurnishingsAndEquipmentsMass();
	
	// SYSTEMS POSITION DATA
	boolean getStandardSystemsPositionFlag();
	@Nullable
	Amount<Length> getAPUPositionX();
	@Nullable
	Amount<Length> getAPUPositionZ();
	@Nullable
	Amount<Length> getAirConditioningAndAntiIcingSystemPositionX();
	@Nullable
	Amount<Length> getAirConditioningAndAntiIcingSystemPositionZ();
	@Nullable
	Amount<Length> getInstrumentsAndNavigationSystemPositionX();
	@Nullable
	Amount<Length> getInstrumentsAndNavigationSystemPositionZ();
	@Nullable
	Amount<Length> getHydraulicAndPneumaticSystemsPositionX();
	@Nullable
	Amount<Length> getHydraulicAndPneumaticSystemsPositionZ();
	@Nullable
	Amount<Length> getElectricalSystemsPositionX();
	@Nullable
	Amount<Length> getElectricalSystemsPositionZ();
	@Nullable
	Amount<Length> getControlSurfacesPositionX();
	@Nullable
	Amount<Length> getControlSurfacesPositionZ();
	@Nullable
	Amount<Length> getFurnishingsAndEquipmentsPositionX();
	@Nullable
	Amount<Length> getFurnishingsAndEquipmentsPositionZ();
	
	class Builder extends IACBalanceManager_Builder {
		public Builder() {
			
		}
	}
}
