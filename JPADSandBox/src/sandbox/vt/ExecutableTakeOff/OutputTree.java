package sandbox.vt.ExecutableTakeOff;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class OutputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// global results:
	private Amount<Length> takeOffDistanceAEO,
						   takeOffDistanceFAR25,
						   balancedFieldLength,
						   groundRoll,
						   rotation,
						   airborne;
	private Amount<Velocity> vsT0, vRot, vLO, v1, v2;
	
	// physical quantities lists:
	private List<Double> alphaDot,
		                 gammaDot,
		                 cL,
		                 cD,
		                 loadFactor;
	private List<Amount<Angle>> alpha,
								theta,
								gamma;
	private List<Amount<Duration>> time;
	private List<Amount<Velocity>> speed,
								   rateOfClimb;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Force>> thrust,
								thrustHorizontal,
								thrustVertical,
								lift,
								drag,
								friction,
								totalForce;
	private List<Amount<Length>> groundDistance,
								 verticalDistance;
	private double[] failureSpeedArray,
		             continuedTakeOffArray,
		             abortedTakeOffArray;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	
	public OutputTree() {
		
		takeOffDistanceAEO = Amount.valueOf(0.0, SI.METER);
		takeOffDistanceFAR25 = Amount.valueOf(0.0, SI.METER);
		balancedFieldLength = Amount.valueOf(0.0, SI.METER);
		groundRoll = Amount.valueOf(0.0, SI.METER);
		rotation = Amount.valueOf(0.0, SI.METER);
		airborne = Amount.valueOf(0.0, SI.METER);
		
		vsT0 = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		vRot = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		vLO = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		v1 = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		v2 = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		
		alphaDot = new ArrayList<Double>();
		gammaDot = new ArrayList<Double>();
		cL = new ArrayList<Double>();
		cD = new ArrayList<Double>();
		loadFactor = new ArrayList<Double>();
		alpha = new ArrayList<Amount<Angle>>();
		theta = new ArrayList<Amount<Angle>>();
		gamma = new ArrayList<Amount<Angle>>();
		time = new ArrayList<Amount<Duration>>();
		speed = new ArrayList<Amount<Velocity>>();
		rateOfClimb = new ArrayList<Amount<Velocity>>();
		acceleration = new ArrayList<Amount<Acceleration>>();
		thrust = new ArrayList<Amount<Force>>();
		thrustHorizontal = new ArrayList<Amount<Force>>();
		thrustVertical = new ArrayList<Amount<Force>>();
		lift = new ArrayList<Amount<Force>>();
		drag = new ArrayList<Amount<Force>>();
		friction = new ArrayList<Amount<Force>>();
		totalForce = new ArrayList<Amount<Force>>();
		groundDistance = new ArrayList<Amount<Length>>();
		verticalDistance = new ArrayList<Amount<Length>>();
		
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	public Amount<Length> getTakeOffDistanceAEO() {
		return takeOffDistanceAEO;
	}

	public void setTakeOffDistanceAEO(Amount<Length> takeOffDistanceAEO) {
		this.takeOffDistanceAEO = takeOffDistanceAEO;
	}

	public Amount<Length> getTakeOffDistanceFAR25() {
		return takeOffDistanceFAR25;
	}

	public void setTakeOffDistanceFAR25(Amount<Length> takeOffDistanceFAR25) {
		this.takeOffDistanceFAR25 = takeOffDistanceFAR25;
	}

	public Amount<Length> getBalancedFieldLength() {
		return balancedFieldLength;
	}

	public void setBalancedFieldLength(Amount<Length> balancedFieldLength) {
		this.balancedFieldLength = balancedFieldLength;
	}

	public Amount<Length> getGroundRoll() {
		return groundRoll;
	}

	public void setGroundRoll(Amount<Length> groundRoll) {
		this.groundRoll = groundRoll;
	}

	public Amount<Length> getRotation() {
		return rotation;
	}

	public void setRotation(Amount<Length> rotation) {
		this.rotation = rotation;
	}

	public Amount<Length> getAirborne() {
		return airborne;
	}

	public void setAirborne(Amount<Length> airborne) {
		this.airborne = airborne;
	}

	public Amount<Velocity> getVsT0() {
		return vsT0;
	}

	public Amount<Velocity> getvRot() {
		return vRot;
	}

	public Amount<Velocity> getvLO() {
		return vLO;
	}

	public void setVsT0(Amount<Velocity> vsT0) {
		this.vsT0 = vsT0;
	}

	public void setvRot(Amount<Velocity> vRot) {
		this.vRot = vRot;
	}

	public void setvLO(Amount<Velocity> vLO) {
		this.vLO = vLO;
	}

	public Amount<Velocity> getV1() {
		return v1;
	}

	public void setV1(Amount<Velocity> v1) {
		this.v1 = v1;
	}

	public Amount<Velocity> getV2() {
		return v2;
	}

	public void setV2(Amount<Velocity> v2) {
		this.v2 = v2;
	}

	public List<Double> getAlphaDot() {
		return alphaDot;
	}

	public void setAlphaDot(List<Double> alphaDot) {
		this.alphaDot = alphaDot;
	}

	public List<Double> getGammaDot() {
		return gammaDot;
	}

	public void setGammaDot(List<Double> gammaDot) {
		this.gammaDot = gammaDot;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public double[] getFailureSpeedArray() {
		return failureSpeedArray;
	}

	public double[] getContinuedTakeOffArray() {
		return continuedTakeOffArray;
	}

	public double[] getAbortedTakeOffArray() {
		return abortedTakeOffArray;
	}

	public void setFailureSpeedArray(double[] failureSpeedArray) {
		this.failureSpeedArray = failureSpeedArray;
	}

	public void setContinuedTakeOffArray(double[] continuedTakeOffArray) {
		this.continuedTakeOffArray = continuedTakeOffArray;
	}

	public void setAbortedTakeOffArray(double[] abortedTakeOffArray) {
		this.abortedTakeOffArray = abortedTakeOffArray;
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

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public List<Amount<Angle>> getTheta() {
		return theta;
	}

	public void setTheta(List<Amount<Angle>> theta) {
		this.theta = theta;
	}

	public List<Amount<Angle>> getGamma() {
		return gamma;
	}

	public void setGamma(List<Amount<Angle>> gamma) {
		this.gamma = gamma;
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
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

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getThrustHorizontal() {
		return thrustHorizontal;
	}

	public void setThrustHorizontal(List<Amount<Force>> thrustHorizontal) {
		this.thrustHorizontal = thrustHorizontal;
	}

	public List<Amount<Force>> getThrustVertical() {
		return thrustVertical;
	}

	public void setThrustVertical(List<Amount<Force>> thrustVertical) {
		this.thrustVertical = thrustVertical;
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

	public List<Amount<Force>> getFriction() {
		return friction;
	}

	public void setFriction(List<Amount<Force>> friction) {
		this.friction = friction;
	}

	public List<Amount<Force>> getTotalForce() {
		return totalForce;
	}

	public void setTotalForce(List<Amount<Force>> totalForce) {
		this.totalForce = totalForce;
	}

	public List<Amount<Length>> getGroundDistance() {
		return groundDistance;
	}

	public void setGroundDistance(List<Amount<Length>> groundDistance) {
		this.groundDistance = groundDistance;
	}

	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
	}
	
}
