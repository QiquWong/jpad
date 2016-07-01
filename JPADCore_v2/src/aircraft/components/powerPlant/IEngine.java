package aircraft.components.powerPlant;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineTypeEnum;

public interface IEngine {
	
	public String getId();
	public void setId(String id);

	public EngineTypeEnum getEngineType();
	public void setEngineType(EngineTypeEnum _engineType);

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
	
}
