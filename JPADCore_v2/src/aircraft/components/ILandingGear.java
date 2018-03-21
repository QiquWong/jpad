package aircraft.components;

import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

@FreeBuilder
public interface ILandingGear {

	String getId();
	int getNumberOfFrontalWheels();
	int getNumberOfRearWheels();
	Amount<Length> getMainLegsLenght();
	Amount<Length> getDistanceBetweenWheels();
	Amount<Length> getFrontalWheelsHeight();
	Amount<Length> getFrontalWheelsWidth();
	Amount<Length> getRearWheelsHeight();
	Amount<Length> getRearWheelsWidth();

	class Builder extends ILandingGear_Builder {
		public Builder () {
			
		}
	}
}
