package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;

@FreeBuilder
public interface ISymmetricFlapCreator {

	String getId();
	FlapTypeEnum getType();
	Double getInnerStationSpanwisePosition();
	Double getOuterStationSpanwisePosition();
	Double getInnerChordRatio();
	Double getOuterChordRatio();
	Amount<Angle> getMinimumDeflection();
	Amount<Angle> getMaximumDeflection();
	
	class Builder extends ISymmetricFlapCreator_Builder {
		public Builder() {
			
		}
	}
}
