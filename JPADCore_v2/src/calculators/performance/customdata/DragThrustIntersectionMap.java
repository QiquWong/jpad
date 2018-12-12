package calculators.performance.customdata;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class DragThrustIntersectionMap extends PerformanceMap{

	private Amount<Velocity> minSpeed, maxSpeed;
	private double minMach, maxMach;
	List<Amount<Velocity>> speed;

	public DragThrustIntersectionMap(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, Amount<Mass> weight,
			EngineOperatingConditionEnum fligthCondition,
			List<Amount<Velocity>> speed, Amount<Velocity> speedMin, Amount<Velocity> speedMax,
			Amount<Area> surface, double cLmax
			) {
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.phi = phi;
		this.weight = weight;
		this.flightCondition = fligthCondition;
		this.speed = speed;
		this.minSpeed = speedMin;
		this.maxSpeed = speedMax;
		this.minMach = SpeedCalc.calculateMach(altitude, deltaTemperature, minSpeed);
		this.maxMach = SpeedCalc.calculateMach(altitude, deltaTemperature, maxSpeed);

	}

	public Amount<Velocity> getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(Amount<Velocity> minSpeed) {
		this.minSpeed = minSpeed;
	}

	public Amount<Velocity> getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Amount<Velocity> maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMinMach() {
		return minMach;
	}

	public void setMinMach(double minMach) {
		this.minMach = minMach;
	}

	public double getMaxMach() {
		return maxMach;
	}

	public void setMaxMach(double maxMach) {
		this.maxMach = maxMach;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}
	
}
