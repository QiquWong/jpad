package analyses;

import java.util.List;

import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.ConditionEnum;

@FreeBuilder
public interface IACDynamicStabilityManager {
	String getId();
	Aircraft getTheAircraft();

	// WEIGHTS DATA
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getDesignFuelMass();

	List<ConditionEnum> getTaskListDynamicStability();
	
	// TODO
	
	class Builder extends IACDynamicStabilityManager_Builder {
		public Builder() {
			
		}
	}
	
}
