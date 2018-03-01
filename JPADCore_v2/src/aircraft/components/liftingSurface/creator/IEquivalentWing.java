package aircraft.components.liftingSurface.creator;

import java.util.List;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;

@FreeBuilder
public interface IEquivalentWing {

	double getRealWingDimensionlessKinkPosition();
	Amount<Angle> getRealWingTwistAtKink();
	double getRealWingDimensionlessXOffsetRootChordLE();
	double getRealWingDimensionlessXOffsetRootChordTE();
	AirfoilCreator getEquivalentWingAirfoilKink();
	List<LiftingSurfacePanelCreator> getPanels();
	
	class Builder extends IEquivalentWing_Builder {
		public Builder() {
			
		}
	}
}
