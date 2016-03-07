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

	public Amount<Length> getMeanAerodynamicChord(boolean recalculate);
	public Amount<Length> getMeanAerodynamicChord();

	Amount<Length>[] getMeanAerodynamicChordLeadingEdge(boolean recalculate);
	Amount<Length>[] getMeanAerodynamicChordLeadingEdge();

	Amount<Length> getMeanAerodynamicChordLeadingEdgeX(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeX();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeY(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeY();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeZ(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeZ();

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
