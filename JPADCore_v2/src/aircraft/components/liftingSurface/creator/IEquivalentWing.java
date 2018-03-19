package aircraft.components.liftingSurface.creator;

import java.util.List;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.airfoils.Airfoil;

@FreeBuilder
public interface IEquivalentWing {

	double getRealWingDimensionlessKinkPosition();
	Amount<Angle> getRealWingTwistAtKink();
	double getRealWingDimensionlessXOffsetRootChordLE();
	double getRealWingDimensionlessXOffsetRootChordTE();
	Airfoil getEquivalentWingAirfoilKink();
	List<LiftingSurfacePanelCreator> getPanels();
	
	class Builder extends IEquivalentWing_Builder {
		public Builder() {
			
		}
	}
}
