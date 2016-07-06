package aircraft.components.powerPlant;

import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.customdata.CenterOfGravity;

public interface IPowerPlant {

	public void calculateMass();

	public CenterOfGravity calculateCG();

	public String getId();
	public void setId(String _id);

	public Integer getEngineNumber();
	public void setEngineNumber(Integer _engineNumber);
	
	public EngineTypeEnum getEngineType();
	public void setEngineType(EngineTypeEnum engineType);
	
	public EngineMountingPositionEnum getMountingPosition();
	
	public List<Engine> getEngineList();
	public void setEngineList(List<Engine> _engineList);

	public Amount<Force> getT0Total();
	
	public Amount<Power> getP0Total();
	
	public List<CenterOfGravity> getCGList();
	
	public Amount<Mass> getTotalMass();
	public void setTotalMass(Amount<Mass> totalMass);
	
	public Amount<Mass> getDryMassPublicDomainTotal();
	
	public Double getPercentTotalDifference();

	public CenterOfGravity getTotalCG();
	
}
