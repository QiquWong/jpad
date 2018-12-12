package calculators.performance.customdata;


import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class FlightEnvelopeMap extends PerformanceMap{

	private Amount<Velocity> maxSpeed, minSpeed;
	private double maxMach, minMach;

	public FlightEnvelopeMap( Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, Amount<Mass> weight, 
			EngineOperatingConditionEnum flightCondition, Amount<Velocity> maxSpeed, Amount<Velocity> minSpeed) {
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.phi = phi;
		this.flightCondition = flightCondition;
		this.maxSpeed = maxSpeed;
		this.minSpeed = minSpeed;
		this.weight = weight;
		this.maxMach = SpeedCalc.calculateMach(altitude, deltaTemperature, maxSpeed);
		this.minMach = SpeedCalc.calculateMach(altitude, deltaTemperature, minSpeed);
	}

	public Amount<Velocity> getMaxSpeed() {
		return maxSpeed;
	}

	public Amount<Velocity> getMinSpeed() {
		return minSpeed;
	}

	public double getMaxMach() {
		return maxMach;
	}

	public double getMinMach() {
		return minMach;
	}

}
