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
	private Amount<Angle> theta;
	private List<Amount<Power>> powerRequired, powerAvailable;
	private List<Amount<Velocity>> rc, speed;
	private List<Amount<Angle>> gamma;

	public RCMap(Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, List<Amount<Power>> powerRequired, List<Amount<Power>> powerAvailable,
			List<Amount<Velocity>> rc, Amount<Velocity> rcMax, Amount<Mass> weight, EngineOperatingConditionEnum flightCondition,
			List<Amount<Velocity>> speed, Amount<Velocity> rcMaxSpeed) {
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
		this.phi = phi;
		this.powerRequired = powerRequired;
		this.powerAvailable = powerAvailable;
		this.flightCondition = flightCondition;
		this.rc = rc;
		this.rcMax = rcMax;
		this.weight = weight;
		this.speed = speed;
		this.rcMaxSpeed = rcMaxSpeed;
		this.rcMaxMach = SpeedCalc.calculateMach(altitude, deltaTemperature, rcMaxSpeed);
		
		this.theta = Amount.valueOf(Math.asin(rcMax.doubleValue(SI.METERS_PER_SECOND)/rcMaxSpeed.doubleValue(SI.METERS_PER_SECOND)), SI.RADIAN).to(NonSI.DEGREE_ANGLE);
		this.gamma = new ArrayList<>();
		for (int i=0; i<rc.size(); i++) {
			gamma.add(
					Amount.valueOf(
							Math.asin(rc.get(i).doubleValue(SI.METERS_PER_SECOND)/speed.get(i).doubleValue(SI.METERS_PER_SECOND)),
							SI.RADIAN)
					.to(NonSI.DEGREE_ANGLE)
					);
		}
	}

	public Amount<Velocity> getRcMax() {
		return rcMax;
	}

	public void setRcMax(Amount<Velocity> rcMax) {
		this.rcMax = rcMax;
	}

	public Amount<Velocity> getRcMaxSpeed() {
		return rcMaxSpeed;
	}

	public void setRcMaxSpeed(Amount<Velocity> rcMaxSpeed) {
		this.rcMaxSpeed = rcMaxSpeed;
	}

	public double getRcMaxMach() {
		return rcMaxMach;
	}

	public void setRcMaxMach(double rcMaxMach) {
		this.rcMaxMach = rcMaxMach;
	}

	public Amount<Angle> getTheta() {
		return theta;
	}

	public void setTheta(Amount<Angle> theta) {
		this.theta = theta;
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

	public List<Amount<Velocity>> getRc() {
		return rc;
	}

	public void setRc(List<Amount<Velocity>> rc) {
		this.rc = rc;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}

	public List<Amount<Angle>> getGamma() {
		return gamma;
	}

	public void setGamma(List<Amount<Angle>> gamma) {
		this.gamma = gamma;
	}

}
