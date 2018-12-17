package calculators.performance.customdata;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
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
	private List<Amount<Velocity>> speed;
	private List<Amount<Length>> groundDistance, verticalDistance;
	private List<Amount<Angle>> alpha, gamma, theta;

	//-------------------------------------------------------------------------------------
	// BUILDER:
	/*
	 * The builder initializes all Lists
	 */
	public TakeOffResultsMap() {

		this.time = new ArrayList<Amount<Duration>>();
		this.speed = new ArrayList<Amount<Velocity>>();
		this.groundDistance = new ArrayList<Amount<Length>>();
		this.verticalDistance = new ArrayList<Amount<Length>>();
		this.alpha = new ArrayList<Amount<Angle>>();
		this.gamma = new ArrayList<Amount<Angle>>();
		this.theta = new ArrayList<Amount<Angle>>();
	}

	//-------------------------------------------------------------------------------------
	// METHODS:
	
	/**************************************************************************************
	 * This method initialize all lists in order to make reusable this map for other 
	 * calculations.
	 * 
	 * @author Vittorio Trifari
	 */
	public void initialize() {
		time.clear();
		speed.clear();
		groundDistance.clear();
		verticalDistance.clear();
		alpha.clear(); 
		gamma.clear(); 
		theta.clear();
	}
	
	/*******************************************************************************************
	 * This method collects all given values into the lists created by the builder.
	 * 
	 * @author Vittorio Trifari
	 * @param timeValue
	 * @param speedValue
	 * @param groundDistanceValue
	 * @param verticalDistanceValue
	 * @param alphaValue
	 * @param gammaValue
	 * @param thetaValue
	 */
	public void collectResults(
			Amount<Duration> timeValue,
			Amount<Velocity> speedValue, 
			Amount<Length> groundDistanceValue,	
			Amount<Length> verticalDistanceValue,
			Amount<Angle> alphaValue, 
			Amount<Angle> gammaValue,
			Amount<Angle> thetaValue
			) {

		time.add(timeValue);
		speed.add(speedValue);
		groundDistance.add(groundDistanceValue);
		verticalDistance.add(verticalDistanceValue);
		alpha.add(alphaValue);
		gamma.add(gammaValue);
		theta.add(thetaValue);
	}

	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
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
	
	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
	}

}