package aircraft.components.nacelles;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.nacelles.NacelleCreator.MountingPosition;
import aircraft.components.powerplant.Engine;
import analyses.nacelles.NacelleAerodynamicsManager;
import analyses.nacelles.NacelleBalanceManager;
import analyses.nacelles.NacelleWeightsManager;

public interface INacelleCreator {

	public void calculateGeometry();
	public void calculateGeometry(int nPoints);
	public void calculateAll(Aircraft theAircraft);
	public void estimateDimensions (Engine theEngine);
	public Double calculateFormFactor();
	
	public void initializeWeights(Aircraft theAircraft);
	public void initializeBalance();
	
	public Amount<Length> getLength();
	public void setLength(Amount<Length> _lenght);

	public Amount<Area> getSurfaceWetted();
	public void setSurfaceWetted(Amount<Area> _sWet);

	public Amount<Length> getDiameterInlet();
	public void setDiameterInlet(Amount<Length> _diameterInlet);
	
	public Amount<Length> getDiameterMax();
	public void setDiameterMean(Amount<Length> _diameter);

	public Amount<Length> getDiameterOutlet();
	public void setDiameterOutlet(Amount<Length> _exitDiameter);

	public Double getKInlet();
	public void setKInlet(Double _kInlet);

	public Double getKOutlet();
	public void setKOutlet(Double _kOutlet);

	public Double getKLength();
	public void setKLength(Double _kLength);
	
	public Double getKDiameterOutlet();
	public void setKDiameterOutlet(Double _kDiameterOutlet);
	
	public Amount<Length> getXPositionMaximumDiameterLRF();
	public void setXPositionMaximumDiameterLRF(Amount<Length> _xPositionMaximumDiameterLRF);
	
	public Amount<Length> getZPositionOutletDiameterLRF();
	public void setZPositionOutletDiameterLRF(Amount<Length> _zPositionOutletDiameterLRF);
	
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
	
	public NacelleWeightsManager getWeights();
	
	public NacelleAerodynamicsManager getAerodynamics();
	
	public NacelleBalanceManager getBalance();
	
	public String getId();
	public void setId(String id);
	
	public Amount<Mass> getMassReference();
	public void setMassReference(Amount<Mass> _massReference);
	
	public double[] getXCoordinatesOutlineDouble();
	public void setXCoordinatesOutlineDouble(double[] xCoordinatesOutlineDouble);

	public double[] getZCoordinatesOutlineXZUpperDouble();
	public void setZCoordinatesOutlineXZUpperDouble(double[] zCoordinatesOutlineXZUpperDouble);

	public double[] getZCoordinatesOutlineXZLowerDouble();
	public void setZCoordinatesOutlineXZLowerDouble(double[] zCoordinatesOutlineXZLowerDouble);
		
	public double[] getYCoordinatesOutlineXYRightDouble();
	public void setYCoordinatesOutlineXYRightDouble(double[] _yCoordinatesOutlineXYRightDouble);

	public double[] getYCoordinatesOutlineXYLeftDouble();
	public void setYCoordinatesOutlineXYLeftDouble(double[] _yCoordinatesOutlineXYLeftDouble);
	
	public List<Amount<Length>> getXCoordinatesOutline();
	public void setXCoordinatesOutline(List<Amount<Length>> xCoordinatesOutlineXY);

	public List<Amount<Length>> getZCoordinatesOutlineXZUpper();
	public void setZCoordinatesOutlineXZUpper(List<Amount<Length>> zCoordinatesOutlineXZUpper);

	public List<Amount<Length>> getZCoordinatesOutlineXZLower();
	public void setZCoordinatesOutlineXZLower(List<Amount<Length>> zCoordinatesOutlineXZLower);
	
	public List<Amount<Length>> getYCoordinatesOutlineXYRight();
	public void setYCoordinatesOutlineXYRight(List<Amount<Length>> _yCoordinatesOutlineXYRight);

	public List<Amount<Length>> getYCoordinatesOutlineXYLeft();
	public void setYCoordinatesOutlineXYLeft(List<Amount<Length>> _yCoordinatesOutlineXYLeft);
}
