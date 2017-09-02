package aircraft.components.nacelles;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.powerplant.Engine;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

public interface INacelles {

	public void initializeWeights(Aircraft theAircraft);
	public void initializeBalance();
	public void calculateSurfaceWetted();
	public void calculateMass(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights);
	public CenterOfGravity calculateCG();

	public String getId();
	public void setId(String _id);

	public int getNacellesNumber();
	public void setNacellesNumber(int _nacellesNumber);

	public List<NacelleCreator> getNacellesList();
	public void setNacellesList(List<NacelleCreator> _nacellesList);
	
	public Map<NacelleCreator, Engine> getNacelleEngineMap();
	public void setNacelleEngineMap(Map<NacelleCreator, Engine> _nacelleEngineMap);

	public Amount<Mass> getTotalMass();
	public void setTotalMass(Amount<Mass> _totalMass);
	
	public List<Amount<Mass>> getMassList();
	public void setMassList(List<Amount<Mass>> _massList);

	public CenterOfGravity getTotalCG();
	public void setTotalCG(CenterOfGravity _totalCG);

	public List<CenterOfGravity> getCGList();
	public void setCGList(List<CenterOfGravity> _cgList);

	public Double getPercentTotalDifference();

	public Double getCD0Total();

	public Double getCD0Parasite();

	public Double getCD0Base();

	public Amount<Mass> getMassReference();
	public void setMassReference(Amount<Mass> _massReference);

	public Amount<Area> getSurfaceWetted();
	public void setSurfaceWetted(Amount<Area> _surfaceWetted);
}
