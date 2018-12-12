package calculators.performance.customdata;

import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;

public class ThrustMap extends PerformanceMap{

	private List<Amount<Force>> thrust;
	private List<Amount<Velocity>> speed;
	private List<Amount<Power>> power;

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
			List<Amount<Force>> thrust,
			List<Amount<Velocity>> speed,
			EngineOperatingConditionEnum flightCondition
			) {
		this.weight = weight;
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.phi = phi;
		this.thrust = thrust;
		this.speed = speed;
		this.flightCondition = flightCondition;
		this.deltaTemperature = deltaTemperature;
		this.power = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MathArrays.ebeMultiply(
						MyArrayUtils.convertListOfAmountTodoubleArray(speed),
						MyArrayUtils.convertListOfAmountTodoubleArray(thrust)
						),
				SI.WATT);
	}

	public List<Amount<Power>> getPower() {
		return power;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}

}
