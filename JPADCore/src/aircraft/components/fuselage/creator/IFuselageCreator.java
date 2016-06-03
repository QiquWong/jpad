package aircraft.components.fuselage.creator;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.creator.SpoilerCreator;

public interface IFuselageCreator {

	public void calculateGeometry(
			int np_N, int np_C, int np_T, // no. points @ Nose/Cabin/Tail
			int np_SecUp, int np_SecLow   // no. points @ Upper/Lower section
			);
	public void calculateGeometry();

	List<Amount<Length>> getXYZ0();
	Amount<Length> getX0();
	Amount<Length> getY0();
	Amount<Length> getZ0();
	void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0);

	List<Amount<Length>> getXYZPole();
	Amount<Length> getXPole();
	Amount<Length> getYPole();
	Amount<Length> getZPole();
	void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp);

	int getDeckNumber();
	void setDeckNumber(int dn);

	Amount<Length> getLength();
	void setLength(Amount<Length> len);

	Amount<Mass> getMassReference();
	void setMassReference(Amount<Mass> massRef);

	void discretizeGeometry(int numberSpanwiseStations);

	public Amount<Area> getSurfaceWetted(boolean recalculate);
	public Amount<Area> getSurfaceWetted();

	public List<Amount<Length>> getDiscretizedYs();
	
}
