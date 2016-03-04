package aircraft.components.liftingSurface.adm;

import java.util.List;

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

	public Amount<Length> getMeanAerodChord(boolean recalculate);
	public Amount<Length> getMeanAerodChord();

	Amount<Length>[] getMeanAerodChordLeadingEdge(boolean recalculate);
	Amount<Length>[] getMeanAerodChordLeadingEdge();

	Amount<Length> getMeanAerodChordLeadingEdgeX(boolean recalculate);
	Amount<Length> getMeanAerodChordLeadingEdgeX();
	Amount<Length> getMeanAerodChordLeadingEdgeY(boolean recalculate);
	Amount<Length> getMeanAerodChordLeadingEdgeY();
	Amount<Length> getMeanAerodChordLeadingEdgeZ(boolean recalculate);
	Amount<Length> getMeanAerodChordLeadingEdgeZ();

	public Amount<Area> getSurfacePlanform(boolean recalculate);
	public Amount<Area> getSurfacePlanform();

	public Amount<Area> getSurfaceWetted(boolean recalculate);
	public Amount<Area> getSurfaceWetted();

	public Amount<Length> getSemiSpan(boolean recalculate);
	public Amount<Length> getSemiSpan();
	public Amount<Length> getSpan(boolean recalculate);
	public Amount<Length> getSpan();

	public Double getAspectRatio(boolean recalculate);
	public Double getAspectRatio();

	public Double getTaperRatio(boolean recalculate);
	public Double getTaperRatio();

	public LiftingSurface getEquivalentWing(boolean recalculate);
	public LiftingSurface getEquivalentWing();

	public List<LiftingSurfacePanel> getPanels();
	public void addPanel(LiftingSurfacePanel panel);

	public List<SymmetricFlaps> getSymmetricFlaps();

	public List<Slats> getSlats();

	public List<AsymmetricFlaps> getAsymmetricFlaps();

	public List<Spoilers> getSpoilers();

}
