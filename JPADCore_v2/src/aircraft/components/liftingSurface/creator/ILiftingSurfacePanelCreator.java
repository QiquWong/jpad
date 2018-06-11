package aircraft.components.liftingSurface.creator;

import javax.annotation.Nullable;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.airfoils.Airfoil;

@FreeBuilder
public interface ILiftingSurfacePanelCreator {

	String getId();
	boolean isLinkedTo();
	Amount<Length> getChordRoot();
	Amount<Length> getChordTip();
	Airfoil getAirfoilRoot();
	@Nullable
	String getAirfoilRootFilePath();
	Airfoil getAirfoilTip();
	@Nullable
	String getAirfoilTipFilePath();
	Amount<Angle> getTwistGeometricAtRoot();
	Amount<Angle> getTwistGeometricAtTip();
	Amount<Length> getSpan();
	Amount<Angle> getSweepLeadingEdge();
	Amount<Angle> getDihedral();

	class Builder extends ILiftingSurfacePanelCreator_Builder {
		public Builder() {
			
		}
	}
}
