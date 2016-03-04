package aircraft.components.liftingSurface.adm;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public abstract class AbstractLiftingSurface implements ILiftingSurface {

	protected String id;

	protected List<LiftingSurfacePanel> panels;
	protected List<SymmetricFlaps> symmetricFlaps;
	protected List<Slats> slats;
	protected List<AsymmetricFlaps> asymmetricFlaps;
	protected List<Spoilers> spoilers;

	// in BRF
	protected Amount<Length> x0;
	protected Amount<Length> y0;
	protected Amount<Length> z0;

	// in LRF
	protected Amount<Length> xPole;
	protected Amount<Length> yPole;
	protected Amount<Length> zPole;

	protected Amount<Length> meanAerodChord;
	protected Amount<Length> meanAerodChordLeadingEdgeX;
	protected Amount<Length> meanAerodChordLeadingEdgeY;
	protected Amount<Length> meanAerodChordLeadingEdgeZ;

	protected Amount<Area> surfacePlanform;
	protected Amount<Area> surfaceWetted;

	protected Amount<Length> semiSpan, span;
	protected Double aspectRatio;
	protected Double taperRatio;

	protected LiftingSurfacePanel equivalentWing;

	public List<LiftingSurfacePanel> getPanels() {
		return panels;
	}

	public List<SymmetricFlaps> getSymmetricFlaps() {
		return symmetricFlaps;
	}

	public List<Slats> getSlats() {
		return slats;
	}

	public List<AsymmetricFlaps> getAsymmetricFlaps() {
		return asymmetricFlaps;
	}

	public List<Spoilers> getSpoilers() {
		return spoilers;
	}

}
