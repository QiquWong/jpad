package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

@FreeBuilder
public interface ISlatCreator {

	String getId();
	double getInnerStationSpanwisePosition();
	double getOuterStationSpanwisePosition();
	double getInnerChordRatio();
	double getOuterChordRatio();
	double getExtensionRatio();
	Amount<Angle> getMinimumDeflection();
	Amount<Angle> getMaximumDeflection();
	
	class Builder extends ISlatCreator_Builder {
		public Builder() {
			
		}
	}
}
