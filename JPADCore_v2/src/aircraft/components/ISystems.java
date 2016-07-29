package aircraft.components;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import analyses.OperatingConditions;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;

public interface ISystems {

	public void calculateMass(Aircraft aircraft, MethodEnum method);
	
	public void calculateOverallMass(Aircraft aircraft, MethodEnum method);
	
	public void calculateControlSurfaceMass(
			Aircraft aircraft,
			MethodEnum method); 
	
	public void calculateAPUMass();
	public void calculateInstrumentationMass();
	public void calculateElectricalMass();
	public void calculateAntiIceAirCond();
	public void calculateElectronicsMass();
	public void calculateHydraulicPneumaticMass();
	public void calculateAbsorbedPower();
	
	public String getId();
	public void setId (String id);
	
	public Amount<Mass> getControlSurfaceMass();
	public void setControlSurfaceMass (Amount<Mass> csMass);

	public Map<MethodEnum, Amount<Mass>> getMassMap();
	public void setMassMap(Map<MethodEnum, Amount<Mass>> massMap);
	
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap();
	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> methodsMap);
	
	public Double[] getPercentDifference();
	public void setPercentDifference(Double[] percentDifference);
	
	public Amount<Mass> getReferenceMass();
	public void setReferenceMass(Amount<Mass> massReference);
	
	public Amount<Mass> getMeanMass();
	
	public Amount<Mass> getOverallMass();
	public void setOverallMass(Amount<Mass> mass);
	
	public Amount<Length> getXApexConstructionAxes();
	public void setXApexConstructionAxes (Amount<Length> xApexConstructionAxes);
	public Amount<Length> getYApexConstructionAxes();
	public void setYApexConstructionAxes (Amount<Length> yApexConstructionAxes);
	public Amount<Length> getZApexConstructionAxes();
	public void setZApexConstructionAxes (Amount<Length> zApexConstructionAxes);
}
