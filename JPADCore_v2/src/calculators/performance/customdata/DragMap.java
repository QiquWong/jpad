package calculators.performance.customdata;

import org.apache.commons.math3.util.MathArrays;

public class DragMap extends PerformanceMap{

	private double[] drag, speed, power;

	/**
	 * A custom "map" used to manage the drag vs speed
	 * curves at several aircraft weights, altitudes, 
	 * power settings
	 * 
	 * @author Lorenzo Attanasio
	 * @param weight current aircraft weight (N)
	 * @param altitude current altitude (m)
	 * @param drag (N)
	 * @param speed (m/s)
	 */
	public DragMap(double weight, double altitude, double[] drag, double[] speed) {
		this.weight = weight;
		this.altitude = altitude;
		this.setDrag(drag);
		this.setSpeed(speed);
		this.setPower(MathArrays.ebeMultiply(speed,drag));
	}

	public double[] getDrag() {
		return drag;
	}

	public void setDrag(double[] drag) {
		this.drag = drag;
	}

	public double[] getSpeed() {
		return speed;
	}

	public void setSpeed(double[] speed) {
		this.speed = speed;
	}

	public double[] getPower() {
		return power;
	}

	public void setPower(double[] power) {
		this.power = power;
	}
	
}

