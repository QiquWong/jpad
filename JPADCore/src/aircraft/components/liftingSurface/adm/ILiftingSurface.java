package aircraft.components.liftingSurface.adm;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public interface ILiftingSurface {

	public void calculateGeometry();

	Amount<Length>[] getXYZ0();
	Amount<Length> getX0();
	Amount<Length> getY0();
	Amount<Length> getZ0();
	void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0);

	Amount<Length>[] getXYZPole();
	Amount<Length> getXPole();
	Amount<Length> getYPole();
	Amount<Length> getZPole();
	void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp);

	public Amount<Length> getMeanAerodChord();
	Amount<Length>[] getMeanAerodChordLeadingEdge();
	Amount<Length> getMeanAerodChordLeadingEdgeX();
	Amount<Length> getMeanAerodChordLeadingEdgeY();
	Amount<Length> getMeanAerodChordLeadingEdgeZ();

	public Amount<Area> getSurfacePlanform();
	public Amount<Area> getSurfaceWetted();

	public Double getAspectRatio();
	public Double getTaperRatio();

	public LiftingSurface getEquivalentWing();

}
