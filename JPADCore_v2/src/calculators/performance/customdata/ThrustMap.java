package calculators.performance.customdata;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;

import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;

public class ThrustMap extends PerformanceMap{

	private double[] thrust, speed, power;

	/**
	 * A custom object used to manage the drag vs speed
	 * curves at several aircraft weights, altitudes, 
	 * power settings
	 * 
	 * @author Lorenzo Attanasio
	 * @param weight current aircraft weight (N)
	 * @param altitude current altitude (m)
	 * @param phi power setting (0<=phi<=1)
	 * @param drag (N)
	 * @param speed (m/s)
	 */
	public ThrustMap(
			Amount<Mass> weight,
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			double phi, 
			double[] thrust,
			double[] speed,
			EngineOperatingConditionEnum flightCondition
			) {
		this.weight = weight;
		this.altitude = altitude;
		this.phi = phi;
		this.thrust = thrust;
		this.speed = speed;
		this.flightCondition = flightCondition;
		this.deltaTemperature = deltaTemperature;
		this.power = MathArrays.ebeMultiply(speed,thrust);
	}

	public double[] getPower() {
		return power;
	}

	public double[] getThrust() {
		return thrust;
	}

	public void setThrust(double[] thrust) {
		this.thrust = thrust;
	}

	public double[] getSpeed() {
		return speed;
	}

	public void setSpeed(double[] speed) {
		this.speed = speed;
	}

}
