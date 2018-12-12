package calculators.performance.customdata;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;

public abstract class PerformanceMap{
	
	protected Amount<Mass> weight; 
	protected double phi;
	protected Amount<Length> altitude;
	protected Amount<Temperature> deltaTemperature;
	protected EngineOperatingConditionEnum flightCondition;

	public Amount<Mass> getWeight() {
		return weight;
	}

	public void setWeight(Amount<Mass> weight) {
		this.weight = weight;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public EngineOperatingConditionEnum getFlightCondition() {
		return flightCondition;
	}

	public void setFlightCondition(EngineOperatingConditionEnum flightCondition) {
		this.flightCondition = flightCondition;
	}

	public Amount<Length> getAltitude() {
		return altitude;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public Amount<Temperature> getDeltaTemperature() {
		return deltaTemperature;
	}

	public void setDeltaTemperature(Amount<Temperature> deltaTemperature) {
		this.deltaTemperature = deltaTemperature;
	}

}
