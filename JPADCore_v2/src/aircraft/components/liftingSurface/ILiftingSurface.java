package aircraft.components.liftingSurface;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;

public interface ILiftingSurface {

	public Amount<Area> getSurface();
	public double getAspectRatio();
	public Amount<Length> getSpan();
	public Amount<Length> getSemiSpan();
	public double getTaperRatio();
	public double getTaperRatioEquivalent(Boolean recalculate);
	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate);
	public Amount<Length> getChordRootEquivalent(Boolean recalculate);
	public Amount<Length> getChordRoot();
	public Amount<Length> getChordTip();
	public Amount<Angle> getSweepLEEquivalent(Boolean recalculate);
	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate);
	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate);
	public Amount<Angle> getDihedralEquivalent(Boolean recalculate);
	public LiftingSurfaceCreator getLiftingSurfaceCreator();
	
	public void calculateGeometry();
	public void calculateGeometry(int nSections);
	
	public double getChordAtYActual(Double y);
	
	public List<Airfoil> getAirfoilList();
		
}

