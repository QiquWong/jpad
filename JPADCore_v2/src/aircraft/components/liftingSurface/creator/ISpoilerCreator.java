package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

@FreeBuilder
public interface ISpoilerCreator {

	String getId();
	double getInnerStationSpanwisePosition();
	double getOuterStationSpanwisePosition();
	double getInnerStationChordwisePosition();
	double getOuterStationChordwisePosition();
	Amount<Angle> getMinimumDeflection();
	Amount<Angle> getMaximumDeflection();
	
	class Builder extends ISpoilerCreator_Builder {
		public Builder() {
			
		}
	}
}
