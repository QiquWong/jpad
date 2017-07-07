package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
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
	Amount<Money> getNoiseConstant();
	Amount<Dimensionless> getNoiseDepartureThreshold();
	Amount<Dimensionless> getNoiseArrivalThreshold();
	Amount<Dimensionless> getApproachCertifiedNoiseLevel();
	Amount<Dimensionless> getLateralCertifiedNoiseLevel();
	Amount<Dimensionless> getFlyoverCertifiedNoiseLevel();
	
	Amount<?> getEmissionsChargesNOx();
	Amount<Money> getEmissionsConstantNOx();
	Amount<Mass> getMassNOx();
	Amount<?> getDpHCFooNOx();
	
	Amount<?> getEmissionsChargesCO();
	Amount<Money> getEmissionsConstantCO();
	Amount<Mass> getMassCO();
	Amount<?> getDpHCFooCO();
	
	Amount<?> getEmissionsChargesCO2();
	Amount<Money> getEmissionsConstantCO2();
	Amount<Mass> getMassCO2();
	Amount<?> getDpHCFooCO2();
	
	Amount<?> getEmissionsChargesHC();
	Amount<Money> getEmissionsConstantHC();
	Amount<Mass> getMassHC();
	Amount<?> getDpHCFooHC();
	
	Amount<?> getAirframeLabourRate();
	Amount<?> getEngineLabourRate();
	Amount<Money> getEnginePrice();
	
	Map<CostsEnum, MethodEnum> getTaskList();
	List<CostsPlotEnum> getPlotList();
	
	/** Builder of IACCostsManager instances. */
	 class Builder extends IACCostsManager_Builder  { 
		 public Builder() {
				// Set defaults in the builder constructor.
			 	setAirframeRelativeSparesCosts(0.1);
			 	setEnginesRelativeSparesCosts(0.3);
			}
	 }
}
