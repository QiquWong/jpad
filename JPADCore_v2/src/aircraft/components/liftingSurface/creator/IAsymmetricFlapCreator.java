package aircraft.components.liftingSurface.creator;

import javax.annotation.Nullable;
import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;

@FreeBuilder
public interface IAsymmetricFlapCreator {

	@Nullable
	String getId();
	FlapTypeEnum getType();
	double getInnerStationSpanwisePosition();
	double getOuterStationSpanwisePosition();
	double getInnerChordRatio();
	double getOuterChordRatio();
	Amount<Angle> getMinimumDeflection();
	Amount<Angle> getMaximumDeflection();
	
	class Builder extends IAsymmetricFlapCreator_Builder {
		public Builder() {
			
		}
	}
}
