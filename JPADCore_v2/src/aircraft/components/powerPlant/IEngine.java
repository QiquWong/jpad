package aircraft.components.powerPlant;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;

public interface IEngine {
	
	public String getId();
	public void setId(String id);

	public EngineTypeEnum getEngineType();
	public void setEngineType(EngineTypeEnum _engineType);

	public Amount<Length> getXApexConstructionAxes();
	public void setXApexConstructionAxes(Amount<Length> _X0);

	public Amount<Length> getYApexConstructionAxes();
	public void setYApexConstructionAxes(Amount<Length> _Y0);

	public Amount<Length> getZApexConstructionAxes();
	public void setZApexConstructionAxes(Amount<Length> _Z0);

	public Amount<Angle> getTiltingAngle();
	public void setTiltingAngle(Amount<Angle> _muT);
	
	public EngineMountingPositionEnum getMountingPosition();
	public void setMountingPosition (EngineMountingPositionEnum position);
	
	public Amount<Power> getP0();
	public void setP0(Amount<Power> _p0);

	public Amount<Force> getT0();
	public void setT0(Amount<Force> _t0);

	public Double getBPR();
	public void setBPR(Double _BPR);
	
	public Amount<Mass> getDryMassPublicDomain();
	public void setDryMassPublicDomain(Amount<Mass> dryMassPublicDomain);
	
	public Amount<Length> getLength();
	public void setLength(Amount<Length> _length);
	
	public Amount<Length> getDiameter();
	public void setDiameter(Amount<Length> _diameter);
	
	public Amount<Length> getWidth();
	public void setWidth(Amount<Length> _width);
	
	public Amount<Length> getHeight();
	public void setHeight(Amount<Length> _height);
	
	public Amount<Length> getPropellerDiameter();
	public void setPropellerDiameter(Amount<Length> _propellerDiameter);
	
	public int getNumberOfBlades();
	public void setNumberOfBlades(int _nBlades);
	
	public EngineWeightsManager getTheWeights();
	public void setTheWeights(EngineWeightsManager _theWeights);

	public EngineBalanceManager getTheBalance();
	public void setTheBalance(EngineBalanceManager _theBalance);
	
	public Amount<Mass> getTotalMass();
	public void setTotalMass(Amount<Mass> _totalMass);
	
	public int getNumberOfCompressorStages();
	public void setNumberOfCompressorStages(int _numberOfCompressorStages);

	public int getNumberOfShafts();
	public void setNumberOfShafts(int _numberOfShafts);
 
	public double getOverallPressureRatio();
	public void setOverallPressureRatio(double _overallPressureRatio);
}
