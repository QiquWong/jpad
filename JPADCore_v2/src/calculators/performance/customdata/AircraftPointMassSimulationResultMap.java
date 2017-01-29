package calculators.performance.customdata;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

/**
 * A custom map collecting all output data of mission simulator. It uses Lists
 * to store all data and populates them by using the method collectResults. 
 * 
 * @author Agostino De Marco
 *
 */
public class AircraftPointMassSimulationResultMap {

	private List<Amount<Duration>> time;
	private List<Amount<Mass>> mass;
	private List<Amount<Force>> thrust, lift, drag, totalForce;
	private List<Amount<Velocity>> speedInertial, airspeed, rateOfClimb;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Length>> altitude, groundDistanceX, groundDistanceY;
	private List<Amount<Angle>> angleOfAttack, flightpathAngle, headingAngle, bankAngle;
	private List<Double> cL, cD, loadFactor;

	public AircraftPointMassSimulationResultMap() {
		this.time = new ArrayList<Amount<Duration>>();
		this.mass = new ArrayList<Amount<Mass>>();
		this.thrust = new ArrayList<Amount<Force>>();
		this.lift = new ArrayList<Amount<Force>>();
		this.drag = new ArrayList<Amount<Force>>();
		this.totalForce = new ArrayList<Amount<Force>>();
		this.speedInertial = new ArrayList<Amount<Velocity>>();
		this.airspeed = new ArrayList<Amount<Velocity>>();
		this.rateOfClimb = new ArrayList<Amount<Velocity>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.altitude = new ArrayList<Amount<Length>>();
		this.groundDistanceX = new ArrayList<Amount<Length>>();
		this.groundDistanceY = new ArrayList<Amount<Length>>();
		this.angleOfAttack = new ArrayList<Amount<Angle>>();
		this.flightpathAngle = new ArrayList<Amount<Angle>>();
		this.headingAngle = new ArrayList<Amount<Angle>>();
		this.bankAngle = new ArrayList<Amount<Angle>>();
		this.cL = new ArrayList<Double>();
		this.cD = new ArrayList<Double>();
		this.loadFactor = new ArrayList<Double>();
	}
	
	public void initialize() {
		this.time.clear();
		this.mass.clear();
		this.thrust.clear();
		this.lift.clear();
		this.drag.clear();
		this.totalForce.clear();
		this.speedInertial.clear();
		this.airspeed.clear();
		this.rateOfClimb.clear();
		this.acceleration.clear();
		this.altitude.clear();
		this.groundDistanceX.clear();
		this.groundDistanceY.clear();
		this.angleOfAttack.clear();
		this.flightpathAngle.clear();
		this.headingAngle.clear();
		this.bankAngle.clear();
		this.cL.clear();
		this.cD.clear();
		this.loadFactor.clear();
	}

	public void collectResults(
			Amount<Duration> timeVal,
			Amount<Mass> massVal,
			Amount<Force> thrustVal, Amount<Force> liftVal, Amount<Force> dragVal, Amount<Force> totalForceVal,
			Amount<Velocity> speedInertialVal, Amount<Velocity> airspeedVal, Amount<Velocity> rateOfClimbVal,
			Amount<Acceleration> accelerationVal,
			Amount<Length> altitudeVal, Amount<Length> groundDistanceXVal, Amount<Length> groundDistanceYVal,
			Amount<Angle> angleOfAttackVal, Amount<Angle> flightpathAngleVal, Amount<Angle> headingAngleVal, 
				Amount<Angle> bankAngleVal,
			double cLVal, double cDVal, double loadFactorVal) {
		this.time.add(timeVal);
		this.mass.add(massVal);
		this.thrust.add(thrustVal);
		this.lift.add(liftVal);
		this.drag.add(dragVal);
		this.totalForce.add(totalForceVal);
		this.speedInertial.add(speedInertialVal);
		this.airspeed.add(airspeedVal);
		this.rateOfClimb.add(rateOfClimbVal);
		this.acceleration.add(accelerationVal);
		this.altitude.add(altitudeVal);
		this.groundDistanceX.add(groundDistanceXVal);
		this.groundDistanceY.add(groundDistanceYVal);
		this.angleOfAttack.add(angleOfAttackVal);
		this.flightpathAngle.add(flightpathAngleVal);
		this.headingAngle.add(headingAngleVal);
		this.bankAngle.add(bankAngleVal);
		this.cL.add(cLVal);
		this.cD.add(cDVal);
		this.loadFactor.add(loadFactorVal);
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public List<Amount<Mass>> getMass() {
		return mass;
	}

	public void setMass(List<Amount<Mass>> mass) {
		this.mass = mass;
	}
	
	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getLift() {
		return lift;
	}

	public void setLift(List<Amount<Force>> lift) {
		this.lift = lift;
	}

	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Force>> getTotalForce() {
		return totalForce;
	}

	public void setTotalForce(List<Amount<Force>> totalForce) {
		this.totalForce = totalForce;
	}

	public List<Amount<Velocity>> getSpeedInertial() {
		return speedInertial;
	}

	public void setSpeedInertial(List<Amount<Velocity>> speedInertial) {
		this.speedInertial = speedInertial;
	}

	public List<Amount<Velocity>> getAirspeed() {
		return airspeed;
	}

	public void setAirspeed(List<Amount<Velocity>> airspeed) {
		this.airspeed = airspeed;
	}

	public List<Amount<Velocity>> getRateOfClimb() {
		return rateOfClimb;
	}

	public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
		this.rateOfClimb = rateOfClimb;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public List<Amount<Length>> getAltitude() {
		return altitude;
	}

	public void setAltitude(List<Amount<Length>> altitude) {
		this.altitude = altitude;
	}

	public List<Amount<Length>> getGroundDistanceX() {
		return groundDistanceX;
	}

	public void setGroundDistanceX(List<Amount<Length>> groundDistanceX) {
		this.groundDistanceX = groundDistanceX;
	}

	public List<Amount<Length>> getGroundDistanceY() {
		return groundDistanceY;
	}

	public void setGroundDistanceY(List<Amount<Length>> groundDistanceY) {
		this.groundDistanceY = groundDistanceY;
	}

	public List<Amount<Angle>> getAngleOfAttack() {
		return angleOfAttack;
	}

	public void setAngleOfAttack(List<Amount<Angle>> angleOfAttack) {
		this.angleOfAttack = angleOfAttack;
	}

	public List<Amount<Angle>> getFlightpathAngle() {
		return flightpathAngle;
	}

	public void setFlightpathAngle(List<Amount<Angle>> flightpathAngle) {
		this.flightpathAngle = flightpathAngle;
	}

	public List<Amount<Angle>> getHeadingAngle() {
		return headingAngle;
	}

	public void setHeadingAngle(List<Amount<Angle>> headingAngle) {
		this.headingAngle = headingAngle;
	}

	public List<Amount<Angle>> getBankAngle() {
		return bankAngle;
	}

	public void setBankAngle(List<Amount<Angle>> bankAngle) {
		this.bankAngle = bankAngle;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public List<Double> getcD() {
		return cD;
	}

	public void setcD(List<Double> cD) {
		this.cD = cD;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}
	
}
