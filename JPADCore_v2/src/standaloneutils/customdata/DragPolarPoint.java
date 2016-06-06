package standaloneutils.customdata;

import javax.measure.quantity.Force;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class DragPolarPoint {

	public enum DragPolarPointEnum {
		SPEED,
		DRAG,
		DRAG_COEFFICIENT,
		LIFT_COEFFICIENT,
		POWER,
		EFFICIENCY;
	}
	
	private double efficiency, cL, cD;
	private Amount<Velocity> speed;
	private Amount<Power> power;
	private Amount<Force> drag;
	
	/**
	 * 
	 * @param efficiency
	 * @param cL
	 * @param cD
	 * @param speed
	 * @param power
	 * @param drag
	 */
	public DragPolarPoint(double efficiency, double cL, double cD, 
			double speed, double power, double drag) {
		this.efficiency = efficiency;
		this.cL = cL;
		this.cD = cD;
		setSpeed(speed);
		setPower(power);
		setDrag(drag);
	}
	
	public double getEfficiency() {
		return efficiency;
	}

	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}

	public double getcL() {
		return cL;
	}

	public void setcL(double cL) {
		this.cL = cL;
	}

	public double getcD() {
		return cD;
	}

	public void setcD(double cD) {
		this.cD = cD;
	}

	public Amount<Velocity> getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = Amount.valueOf(speed, SI.METERS_PER_SECOND);
	}

	public Amount<Power> getPower() {
		return power;
	}

	public void setPower(double power) {
		this.power = Amount.valueOf(power, SI.WATT);
	}

	public Amount<Force> getDrag() {
		return drag;
	}

	public void setDrag(double drag) {
		this.drag = Amount.valueOf(drag, SI.NEWTON);
	}
	
}
