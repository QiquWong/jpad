package aircraft.components.liftingSurface;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;

public interface ILiftingSurface {

	public Amount<Area> getSurface();
	public double getAspectRatio();
	public Amount<Length> getSpan();
	public Amount<Length> getSemiSpan();
	public double getTaperRatio();
	public double getTaperRatioEquivalent();
	public LiftingSurface getEquivalentWing();
	public Amount<Length> getChordRootEquivalent();
	public Amount<Length> getChordRoot();
	public Amount<Length> getChordTip();
	public Amount<Angle> getSweepLEEquivalent();
	public Amount<Angle> getSweepHalfChordEquivalent();
	public Amount<Angle> getSweepQuarterChordEquivalent();
	public double getDihedralEquivalent();
	public LiftingSurfaceCreator getLiftingSurfaceCreator();
	
	public void calculateGeometry();
	public void calculateGeometry(int nSections);
	
	// TODO: COMPLETE !!
	
		
}

