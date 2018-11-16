package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.CostsPlotEnum;
import configuration.enumerations.MethodEnum;

@FreeBuilder
public interface IACCostsManager {

	// All input from file
	String getId();
	Aircraft getAircraft();
	OperatingConditions getOperatingConditions();
	
	Amount<Mass> getMaximumTakeOffMass();
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getPayload();
	
	Amount<Length> getRange();
	Amount<Mass> getBlockFuelMass();
	Amount<Duration> getFlightTime();
	
	Amount<?> getUtilization();
	
	Amount<Duration> getLifeSpan();
	Double getResidualValue();
	Amount<Money> getAircraftPrice();
	Double getAirframeRelativeSparesCosts();
	Double getEnginesRelativeSparesCosts();
	Double getInterestRate();
	Double getInsuranceRate();
	
	Amount<?> getCabinLabourRate();
	Amount<?> getCockpitLabourRate();
	Amount<?> getFuelUnitPrice();
	
	Amount<?> getLandingCharges();
	Amount<?> getNavigationCharges();
	Amount<?> getGroundHandlingCharges();
	Amount<?> getNoiseCharges();
	Amount<?> getEmissionsChargesNOx();
	Amount<?> getEmissionsChargesCO();
	Amount<?> getEmissionsChargesCO2();
	Amount<?> getEmissionsChargesHC();
	
	Amount<?> getAirframeLabourRate();
	Amount<?> getEngineLabourRate();
	Amount<Money> getEnginesPrice();
	
	Map<CostsEnum, MethodEnum> getTaskList();
	List<CostsPlotEnum> getPlotList();
	
	/** Builder of IACCostsManager instances. */
	 class Builder extends IACCostsManager_Builder  { 
		 public Builder() {
				// Set defaults in the builder constructor.
			}
	 }
}