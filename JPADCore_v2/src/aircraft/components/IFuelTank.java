package aircraft.components;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import database.databasefunctions.FuelFractionDatabaseReader;
import standaloneutils.customdata.CenterOfGravity;

public interface IFuelTank {

	public void calculateGeometry(LiftingSurface theWing);
	
	public void calculateFuelMass();
	
	public void calculateCG();
	
	public FuelFractionDatabaseReader getFuelFractionDatabase();
	public void setFuelFractionDatabase(FuelFractionDatabaseReader fuelFractionDatabase);
	
	public String getId();
	public void setId(String _id);
	
	public Amount<Length> getXApexConstructionAxes();
	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes);
	
	public Amount<Length> getYApexConstructionAxes();
	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes);
	
	public Amount<Length> getZApexConstructionAxes();
	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes);
	
	public Double getMainSparNormalizedStation();
	public void setMainSparNormalizedStation(Double _mainSparNormalizedStation);
	
	public Double getSecondarySparNormalizedStation();
	public void setSecondarySparNormalizedStation(Double _secondarySparNormalizedStation);
	
	public List<Amount<Length>> getDistanceBetweenSpars();
	public void setDistanceBetweenSpars(List<Amount<Length>> _distanceBetweenSpars);
	
	public List<Amount<Length>> getPrismoidsLength();
	public void setPrismoidsLength(List<Amount<Length>> _prismoidsLength);
	
	public Amount<Length> getXCG();
	public void setXCG(Amount<Length> _xCG);
	
	public Amount<Length> getYCG();
	public void setYCG(Amount<Length> _yCG);
	
	public Amount<Length> getZCG();
	public void setZCG(Amount<Length> _zCG);
	
	public Amount<VolumetricDensity> getFuelDensity();
	public void setFuelDensity(Amount<VolumetricDensity> _fuelDensity);
	
	public Amount<Mass> getMassEstimated();
	
	public List<Amount<Length>> getThicknessAtMainSpar();
	
	public List<Amount<Length>> getThicknessAtSecondarySpar();
	
	public List<Amount<Area>> getPrismoidsSectionsAreas();
	
	public List<Amount<Volume>> getPrismoidsVolumes();
	
	public Amount<Volume> getFuelVolume();
	
	public Amount<Mass> getFuelMass();
	
	public Amount<Force> getFuelWeight();
}
