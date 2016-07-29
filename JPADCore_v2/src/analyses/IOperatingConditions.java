package analyses;

import javax.measure.quantity.Angle;
import javax.measure.quantity.DynamicViscosity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

public interface IOperatingConditions {

	public void calculate();
	public double calculateReCutOff(double lenght, double k);
	public Double calculateRe(double lenght, double roughness);
	
	public Amount<Pressure> getMaxDeltaPressure();

	public Double getMachCurrent();
	public void setMachCurrent(Double _mach);

	public Double getMachTransonicThreshold();
	public void setMachTransonicThreshold(Double _machTransonicThreshold);

	public Amount<VolumetricDensity> getDensityCurrent();
	
	public void setRho(Amount<VolumetricDensity> _rho);

	public Amount<DynamicViscosity> getMu();
	public void setMu(Amount<DynamicViscosity> _mu);

	public Double[] getAlpha();
	public void setAlpha(Double _alpha[]);
	
	public Amount<Length> getAltitude();
	public void setAltitude(Amount<Length> _altitude);

	public Double[] getCL();

	public Amount<Temperature> getStaticTemperature();
	public void setTemperature(Amount<Temperature> _temperature);
	public void setDensity(Amount<VolumetricDensity> _density);
	public void setPressure(Amount<Pressure> _pressure);


	public Amount<Angle> getAlphaCurrent();
	public void setAlphaCurrent(Amount<Angle> _alphaCurrent);

	public void setTAS(Amount<Velocity> _speed);
	public Amount<Velocity> getTAS();
	public Amount<Velocity> getEAS();
	public Amount<Velocity> getCAS();
	public Amount<Pressure> getMaxDynamicPressure();
	public Double getPressureCoefficientCurrent();
	public Amount<Temperature> getStagnationTemperature();
	public Amount<Pressure> getStaticPressure();
	public Amount<Pressure> getStagnationPressure();
	public Amount<Pressure> getDynamicPressure();

	public String getId();
	public void setId(String id);
	
}
