package aircraft.components.liftingSurface.adm;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public class LiftingSurface extends AbstractLiftingSurface {


	public static LiftingSurface importFromXML(String pathToXML, String airfoilsDir) {
		// TODO


		return null;
	}

	@Override
	public void calculateGeometry() {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Length>[] getXYZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getX0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getY0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Length>[] getXYZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getXPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getYPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Length> getMeanAerodChord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length>[] getMeanAerodChordLeadingEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeZ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfacePlanform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getAspectRatio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getTaperRatio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiftingSurface getEquivalentWing() {
		// TODO Auto-generated method stub
		return null;
	}

}
