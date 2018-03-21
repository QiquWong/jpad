package aircraft.components.nacelles;

import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

@FreeBuilder
public interface INacelleCreator {

	String getId();
	Amount<Length> getRoughness();
	Amount<Length> getLength();
	Amount<Length> getDiameterMax();
	double getKInlet();
	double getKOutlet();
	double getKLength();
	double getKDiameterOutlet();
	
	class Builder extends INacelleCreator_Builder {
		public Builder() {
			
		}
	}
}
