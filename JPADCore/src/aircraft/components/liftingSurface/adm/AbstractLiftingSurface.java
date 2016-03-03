package aircraft.components.liftingSurface.adm;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public abstract class AbstractLiftingSurface implements ILiftingSurface {

	private List<LiftingSurfacePanel> panels;

	Amount<Length> x0;
	Amount<Length> y0;
	Amount<Length> z0;

	Amount<Length> xPole;
	Amount<Length> yPole;
	Amount<Length> zPole;

	Amount<Length> meanAerodChord;
	Amount<Length> meanAerodChordLeadingEdgeX;
	Amount<Length> meanAerodChordLeadingEdgeY;
	Amount<Length> meanAerodChordLeadingEdgeZ;

	public Amount<Area> surfacePlanform;
	public Amount<Area> surfaceWetted;

	public Double aspectRatio;
	public Double taperRatio;

	private LiftingSurfacePanel equivalentWing;

	public List<LiftingSurfacePanel> getPanels() {
		return panels;
	}

}
