package aircraft;

import java.util.ArrayList;
import java.util.List;

public class OperatingConditionsManager {

	private List<OperatingConditions> _operatingPoints = new ArrayList<OperatingConditions>();	

	public OperatingConditionsManager() {
	}
	public OperatingConditionsManager(List<OperatingConditions> opl) {
		_operatingPoints = opl;
	}
	
	public void addOperatingPoint(OperatingConditions op)
	{
		_operatingPoints.add(op);
	}
	public void removeOperatingPoint(OperatingConditions op)
	{
		_operatingPoints.remove(op);
	}
	public void resetOperatingPoint()
	{
		_operatingPoints.clear();
	}
	
	public List<OperatingConditions> getOperatingPointsList()
	{
		return _operatingPoints;
	}
	public Object[] getOperatingPointsArray()
	{
		return _operatingPoints.toArray();
	}
}
