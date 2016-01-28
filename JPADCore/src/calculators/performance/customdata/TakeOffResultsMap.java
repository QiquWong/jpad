package calculators.performance.customdata;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import org.jscience.physics.amount.Amount;

/**
 * A custom map used to collect all output data of take-off phases calculation. It uses Lists
 * to store all data and populates them by using the method collectResults. 
 * 
 * @author Vittorio Trifari
 *
 */

//-------------------------------------------------------------------------------------
// VARIABLE DECLARATION
public class TakeOffResultsMap extends PerformanceMap{

	private List<Amount<Duration>> time;
	private List<Amount<Force>> thrust, thrustHorizontal, thrustVertical,
								friction, lift, drag, totalForce;
	private List<Amount<Velocity>> speed, meanSpeed, rateOfClimb, meanRateOfClimb;
	private List<Amount<Acceleration>> acceleration, meanAcceleration;
	private List<Amount<Length>> deltaGruondDistance, groundDistance,
								 deltaVerticalDistance, verticalDistance;
	private List<Amount<Angle>> alpha, gamma, theta;
	private List<Double> alphaDot, gammaDot, cL, cD, loadFactor;

	//-------------------------------------------------------------------------------------
	// BUILDER:
	/*
	 * The builder initializes all Lists
	 */
	public TakeOffResultsMap() {

		this.time = new ArrayList<Amount<Duration>>();
		this.thrust = new ArrayList<Amount<Force>>();
		this.thrustHorizontal = new ArrayList<Amount<Force>>();
		this.thrustVertical = new ArrayList<Amount<Force>>();
		this.friction = new ArrayList<Amount<Force>>();
		this.lift = new ArrayList<Amount<Force>>();
		this.drag = new ArrayList<Amount<Force>>();
		this.totalForce = new ArrayList<Amount<Force>>();
		this.loadFactor = new ArrayList<Double>();
		this.speed = new ArrayList<Amount<Velocity>>();
		this.meanSpeed  = new ArrayList<Amount<Velocity>>();
		this.rateOfClimb = new ArrayList<Amount<Velocity>>();
		this.meanRateOfClimb = new ArrayList<Amount<Velocity>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.meanAcceleration = new ArrayList<Amount<Acceleration>>();
		this.deltaGruondDistance = new ArrayList<Amount<Length>>();
		this.groundDistance = new ArrayList<Amount<Length>>();
		this.deltaVerticalDistance = new ArrayList<Amount<Length>>();
		this.verticalDistance = new ArrayList<Amount<Length>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.alphaDot = new ArrayList<Double>();
		this.gamma = new ArrayList<Amount<Angle>>();
		this.gammaDot = new ArrayList<Double>();
		this.theta = new ArrayList<Amount<Angle>>();
		this.cL = new ArrayList<Double>();
		this.cD = new ArrayList<Double>();
	}

	//-------------------------------------------------------------------------------------
	// METHODS:
	
	/***************************************************************************************
	 * This method collects all given values into the lists created by the builder.
	 * 
	 * @author Vittorio Trifari
	 * @param timeValue
	 * @param thrustValue
	 * @param thrustHorizontalValue
	 * @param thrustVerticalValue
	 * @param frictionValue
	 * @param liftValue
	 * @param dragValue
	 * @param totalForceValue
	 * @param speedValue
	 * @param meanSpeedValue
	 * @param accelerationValue
	 * @param meanAccelerationValue
	 * @param deltaGruondDistanceValue
	 * @param groundDistanceValue
	 * @param alphaValue
	 * @param alphaDotValue
	 * @param cLValue
	 * @param cDValue
	 */
	public void collectResults(Amount<Duration> timeValue, Amount<Force> thrustValue,
			Amount<Force> thrustHorizontalValue, Amount<Force> thrustVerticalValue,
			Amount<Force> frictionValue, Amount<Force> liftValue, Amount<Force> dragValue,
			Amount<Force> totalForceValue, double loadFactorValue, Amount<Velocity> speedValue, 
			Amount<Velocity> meanSpeedValue, Amount<Velocity> rateOfClimbValue,
			Amount<Velocity> meanRateOfClimbValue, Amount<Acceleration> accelerationValue,
			Amount<Acceleration> meanAccelerationValue,	Amount<Length> deltaGruondDistanceValue,
			Amount<Length> groundDistanceValue, Amount<Length> deltaVerticalDistanceValue,
			Amount<Length> verticalDistanceValue, Amount<Angle> alphaValue, 
			double alphaDotValue, Amount<Angle> gammaValue, double gammaDotValue,
			Amount<Angle> thetaValue, double cLValue, double cDValue) {

		time.add(timeValue);
		thrust.add(thrustHorizontalValue);
		thrustHorizontal.add(thrustHorizontalValue);
		thrustVertical.add(thrustVerticalValue);
		friction.add(frictionValue);
		lift.add(liftValue);
		drag.add(dragValue);
		totalForce.add(totalForceValue);
		loadFactor.add(loadFactorValue);
		speed.add(speedValue);
		meanSpeed.add(meanSpeedValue);
		rateOfClimb.add(rateOfClimbValue);
		meanRateOfClimb.add(meanRateOfClimbValue);
		acceleration.add(accelerationValue);
		meanAcceleration.add(meanAccelerationValue);
		deltaGruondDistance.add(deltaGruondDistanceValue);
		groundDistance.add(groundDistanceValue);
		deltaVerticalDistance.add(deltaVerticalDistanceValue);
		verticalDistance.add(verticalDistanceValue);
		alpha.add(alphaValue);
		alphaDot.add(alphaDotValue);
		gamma.add(gammaValue);
		gammaDot.add(gammaDotValue);
		theta.add(thetaValue);
		cL.add(cLValue);
		cD.add(cDValue);
	}


	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
	public List<Amount<Duration>> getTime() {
		return time;
	}
	
	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
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
	
	public List<Amount<Force>> getFriction() {
		return friction;
	}
	
	public void setFriction(List<Amount<Force>> friction) {
		this.friction = friction;
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
	
	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}
	
	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}
	
	public List<Amount<Velocity>> getMeanSpeed() {
		return meanSpeed;
	}
	
	public void setMeanSpeed(List<Amount<Velocity>> meanSpeed) {
		this.meanSpeed = meanSpeed;
	}
	
	public List<Amount<Velocity>> getRateOfClimb() {
		return rateOfClimb;
	}
	
	public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
		this.rateOfClimb = rateOfClimb;
	}
	
	public List<Amount<Velocity>> getMeanRateOfClimb() {
		return meanRateOfClimb;
	}
	
	public void setMeanRateOfClimb(List<Amount<Velocity>> meanRateOfClimb) {
		this.meanRateOfClimb = meanRateOfClimb;
	}
	
	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}
	
	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}
	
	public List<Amount<Acceleration>> getMeanAcceleration() {
		return meanAcceleration;
	}
	
	public void setMeanAcceleration(List<Amount<Acceleration>> meanAcceleration) {
		this.meanAcceleration = meanAcceleration;
	}
	
	public List<Amount<Length>> getDeltaGruondDistance() {
		return deltaGruondDistance;
	}
	
	public void setDeltaGruondDistance(List<Amount<Length>> deltaGruondDistance) {
		this.deltaGruondDistance = deltaGruondDistance;
	}
	
	public List<Amount<Length>> getGroundDistance() {
		return groundDistance;
	}
	
	public void setGroundDistance(List<Amount<Length>> groundDistance) {
		this.groundDistance = groundDistance;
	}
	
	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}
	
	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}
	
	public List<Amount<Angle>> getGamma() {
		return gamma;
	}
	
	public void setGamma(List<Amount<Angle>> gamma) {
		this.gamma = gamma;
	}
	
	public List<Amount<Angle>> getTheta() {
		return theta;
	}
	
	public void setTheta(List<Amount<Angle>> theta) {
		this.theta = theta;
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

	public List<Amount<Length>> getDeltaVerticalDistance() {
		return deltaVerticalDistance;
	}

	public void setDeltaVerticalDistance(List<Amount<Length>> deltaVerticalDistance) {
		this.deltaVerticalDistance = deltaVerticalDistance;
	}

	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
	}

}