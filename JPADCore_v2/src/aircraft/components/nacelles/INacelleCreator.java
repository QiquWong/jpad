package aircraft.components.nacelles;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.nacelles.NacelleCreator.MountingPosition;
import aircraft.components.powerPlant.Engine;

public interface INacelleCreator {

	public void calculateAll();
	public void estimateDimensions (Engine theEngine);
	public Double calculateFormFactor();
	
	public Amount<Length> getLength();
	public void setLength(Amount<Length> _lenght);

	public Amount<Area> getSurfaceWetted();
	public void setSurfaceWetted(Amount<Area> _sWet);

	public Amount<Length> getDiameterMean();
	public void setDiameterMean(Amount<Length> _diameter);

	public Amount<Length> getDiameterOutlet();
	public void setDiameterOutlet(Amount<Length> _exitDiameter);

	public Amount<Length> getWidth();
	public void setWidth(Amount<Length> _width);

	public Amount<Length> getHeight();
	public void setHeight(Amount<Length> _height);
	
	public Amount<Length> getRoughness();
	public void setRoughness(Amount<Length> _roughness);

	public MountingPosition getMountingPosition();
	public void setMountingPosition(MountingPosition _mounting);

	public Amount<Length> getXApexConstructionAxes();
	public void setXApexConstructionAxes(Amount<Length> _X0);

	public Amount<Length> getYApexConstructionAxes();
	public void setYApexConstructionAxes(Amount<Length> _Y0);

	public Amount<Length> getZApexConstructionAxes();
	public void setZApexConstructionAxes(Amount<Length> _Z0);

	public Engine getTheEngine();
	public void setTheEngine(Engine _theEngine);
	
	public NacellesWeightsManager getWeights();
	
	public NacellesAerodynamicsManager getAerodynamics();
	
	public NacellesBalanceManager getBalance();
	
	public String getId();
	public void setId(String id);
	
	public Amount<Mass> getMassReference();
	public void setMassReference(Amount<Mass> _massReference);
	
}
