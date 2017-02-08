package calculators.performance;

import org.inferred.freebuilder.FreeBuilder;

import configuration.enumerations.EngineOperatingConditionEnum;

@FreeBuilder
public interface MissionEvent {

	String getDescription();
	Double getTime();
	Double getCoefficientLiftMax();
	Double getCommandedSpeed();
	Double getCommandedFlightpathAngle();
	Double getCommandedHeadingAngle();
	Double getWindSpeedXE();
	Double getWindSpeedYE();
	Double getWindSpeedZE();
	EngineOperatingConditionEnum getEngineCondition();

	/** Builder of MissionEvent instances. */
	class Builder extends MissionEvent_Builder {
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("Mission event - untitled");
			setEngineCondition(EngineOperatingConditionEnum.UNKNOWN);
			setCoefficientLiftMax(1.1);
			setCommandedFlightpathAngle(0.0);
			setCommandedHeadingAngle(0.0);
			setWindSpeedXE(0.0);
			setWindSpeedYE(0.0);
			setWindSpeedZE(0.0);
		}
	}
	
}	

