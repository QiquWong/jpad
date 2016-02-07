package aircraft.components.liftingSurface.adm;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public interface ILiftingSurfacePanel {

	public void calculateGeometry();

	Amount<Length> getChordRoot();
	void setChordRoot(Amount<Length> cr);

	Amount<Length> getChordTip();
	void setChordTip(Amount<Length> ct);
	
	Amount<Length> getSpan();
	void setSpan(Amount<Length> b);
	
	Amount<Angle> getSweepAtLeadingEdge();
	Amount<Angle> getSweepAtQuarterChord();
	Amount<Angle> getSweepAtHalfChord();
	Amount<Angle> getSweepAtTrailingEdge();
	void setSweepAtLeadingEdge(Amount<Angle> lambda);
	
	Amount<Angle> getDihedral();
	void setDihedral(Amount<Angle> gamma);
	
	public Amount<Length> getMeanAerodChord();
	Amount<Length>[] getMeanAerodChordLeadingEdge();
	Amount<Length> getMeanAerodChordLeadingEdgeX();
	Amount<Length> getMeanAerodChordLeadingEdgeY();
	Amount<Length> getMeanAerodChordLeadingEdgeZ();

	public Amount<Area> getSurfacePlanform();
	public Amount<Area> getSurfaceWetted();

	public Double getAspectRatio();
	public Double getTaperRatio();

}
