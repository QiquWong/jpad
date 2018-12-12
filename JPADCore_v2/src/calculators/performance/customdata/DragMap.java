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

import standaloneutils.MyArrayUtils;

public class DragMap extends PerformanceMap{

	private List<Amount<Force>> drag;
	private List<Amount<Velocity>> speed;
	private List<Amount<Power>> power;

	/**
	 * A custom "map" used to manage the drag vs speed
	 * curves at several aircraft weights, altitudes, 
	 * power settings
	 * 
	 * @author Vittorio Trifari
	 * @param weight current aircraft weight (N)
	 * @param altitude current altitude (m)
	 * @param drag (N)
	 * @param speed (m/s)
	 */
	public DragMap(Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature, List<Amount<Force>> drag, List<Amount<Velocity>> speed) {
		this.weight = weight;
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.drag = drag;
		this.speed = speed;
		this.power = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MathArrays.ebeMultiply(
						MyArrayUtils.convertListOfAmountTodoubleArray(speed),
						MyArrayUtils.convertListOfAmountTodoubleArray(drag)
						), 
				SI.WATT
				);
	}

	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}

	public List<Amount<Power>> getPower() {
		return power;
	}

	public void setPower(List<Amount<Power>> power) {
		this.power = power;
	}
	
}

