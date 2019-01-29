package calculators.performance.customdata;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class RCMap extends PerformanceMap{

	private Amount<Velocity> rcMax, rcMaxSpeed; 
	private double rcMaxMach;
	private Amount<Angle> climbAngle;
	private List<Amount<Power>> powerRequired, powerAvailable;
	private List<Amount<Velocity>> rcList, speedList;
	private List<Amount<Angle>> climbAngleList;

	public RCMap(Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, List<Amount<Power>> powerRequired, List<Amount<Power>> powerAvailable,
			List<Amount<Velocity>> rc, Amount<Velocity> rcMax, Amount<Mass> weight, EngineOperatingConditionEnum flightCondition,
			List<Amount<Velocity>> speed, Amount<Velocity> rcMaxSpeed) {
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.phi = phi;
		this.powerRequired = powerRequired;
		this.powerAvailable = powerAvailable;
		this.flightCondition = flightCondition;
		this.rcList = rc;
		this.rcMax = rcMax;
		this.weight = weight;
		this.speedList = speed;
		this.rcMaxSpeed = rcMaxSpeed;
		this.rcMaxMach = SpeedCalc.calculateMach(altitude, deltaTemperature, rcMaxSpeed);
		
		this.climbAngle = Amount.valueOf(Math.asin(rcMax.doubleValue(SI.METERS_PER_SECOND)/rcMaxSpeed.doubleValue(SI.METERS_PER_SECOND)), SI.RADIAN).to(NonSI.DEGREE_ANGLE);
		this.climbAngleList = new ArrayList<>();
		for (int i=0; i<rc.size(); i++) {
			climbAngleList.add(
					Amount.valueOf(
							Math.asin(rc.get(i).doubleValue(SI.METERS_PER_SECOND)/speed.get(i).doubleValue(SI.METERS_PER_SECOND)),
							SI.RADIAN)
					.to(NonSI.DEGREE_ANGLE)
					);
		}
	}

	public Amount<Velocity> getRCMax() {
		return rcMax;
	}

	public void setRCMax(Amount<Velocity> rcMax) {
		this.rcMax = rcMax;
	}

	public Amount<Velocity> getRCMaxSpeed() {
		return rcMaxSpeed;
	}

	public void setRCMaxSpeed(Amount<Velocity> rcMaxSpeed) {
		this.rcMaxSpeed = rcMaxSpeed;
	}

	public double getRCMaxMach() {
		return rcMaxMach;
	}

	public void setRCMaxMach(double rcMaxMach) {
		this.rcMaxMach = rcMaxMach;
	}

	public Amount<Angle> getClimbAngle() {
		return climbAngle;
	}

	public void setClimbAngle(Amount<Angle> theta) {
		this.climbAngle = theta;
	}

	public List<Amount<Power>> getPowerRequired() {
		return powerRequired;
	}

	public void setPowerRequired(List<Amount<Power>> powerRequired) {
		this.powerRequired = powerRequired;
	}

	public List<Amount<Power>> getPowerAvailable() {
		return powerAvailable;
	}

	public void setPowerAvailable(List<Amount<Power>> powerAvailable) {
		this.powerAvailable = powerAvailable;
	}

	public List<Amount<Velocity>> getRCList() {
		return rcList;
	}

	public void setRCList(List<Amount<Velocity>> rc) {
		this.rcList = rc;
	}

	public List<Amount<Velocity>> getSpeedList() {
		return speedList;
	}

	public void setSpeedList(List<Amount<Velocity>> speed) {
		this.speedList = speed;
	}

	public List<Amount<Angle>> getClimbAngleList() {
		return climbAngleList;
	}

	public void setClimbAngleList(List<Amount<Angle>> gamma) {
		this.climbAngleList = gamma;
	}

}
