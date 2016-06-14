package aircraft.components;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

public interface ILandingGear {

	public void calculateMass(Aircraft aircraft, OperatingConditions conditions);
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions, MethodEnum method);
	public void calculateCG(Aircraft aircraft, OperatingConditions conditions);
	public void calculateCG(Aircraft aircraft, OperatingConditions conditions, MethodEnum method);
	
	public Amount<Mass> getMass();
	public void setMass(Amount<Mass> mass);
	
	public Map<MethodEnum, Amount<Mass>> getMassMap();
	public void setMassMap(Map<MethodEnum, Amount<Mass>> massMap);
	
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap();
	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> methodsMap);
	
	public Double[] getPercentDifference();
	public void setPercentDifference(Double[] percentDifference);
	
	public Amount<Mass> getMassEstimated();
	
	public Map<MethodEnum, Amount<Length>> getXCGMap();
	public void setXCGMap(Map<MethodEnum, Amount<Length>> xCGMap);
	
	public CenterOfGravity getCg();
	public void setCg(CenterOfGravity cg);
	
	public String getId();
	public void setId (String id);
	
	public Amount<Length> getMainLegsLenght();
	public void setMainLegsLenght(Amount<Length> lenght);
	
	public int getNumberOfFrontalWheels();
	public void setNumberOfFrontalWheels(int _numberOfFrontalWheels);
	
	public int getNumberOfRearWheels();
	public void setNumberOfRearWheels(int _numberOfRearWheels);
	
	public Amount<Length> getFrontalWheelsHeight();
	public void setFrontalWheelsHeight(Amount<Length> _frontalWheelsHeight);
	
	public Amount<Length> getFrontalWheelsWidth();
	public void setFrontalWheelsWidth(Amount<Length> _frontalWheelsWidth);
	
	public Amount<Length> getRearWheelsHeight();
	public void setRearWheelsHeight(Amount<Length> _rearWheelsHeight);
	
	public Amount<Length> getRearWheelsWidth();
	public void setRearWheelsWidth(Amount<Length> _rearWheelsWidth);
	
	public Amount<Mass> getReferenceMass();
	public void setReferenceMass(Amount<Mass> referenceMass);
	
}
