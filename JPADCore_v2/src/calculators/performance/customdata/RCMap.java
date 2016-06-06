package calculators.performance.customdata;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class RCMap extends PerformanceMap{

	private double RCmax, theta, RCMaxSpeed, RCMaxMach;
	double[] powerRequired, powerAvailable, RC, speed, gamma;

	public RCMap(double altitude, double phi, double[] powerRequired, double[] powerAvailable,
			double[] RC, double RCMax, double BPR, double weight, EngineOperatingConditionEnum flightCondition,
			double[] speed, double RCMaxSpeed) {
		this.altitude = altitude;
		this.phi = phi;
		this.powerRequired = powerRequired;
		this.powerAvailable = powerAvailable;
		this.bpr = BPR;
		this.flightCondition = flightCondition;
		this.RC = RC;
		this.RCmax = RCMax;
		this.weight = weight;
		this.speed = speed;
		this.RCMaxSpeed = RCMaxSpeed;
		this.RCMaxMach = SpeedCalc.calculateMach(altitude, RCMaxSpeed);
		
		this.theta = Math.asin(RCMax/RCMaxSpeed);
		this.gamma = new double[RC.length];
		for (int i=0; i<RC.length; i++) {
			this.gamma[i] = Math.asin(RC[i]/speed[i]);
		}
	}

	public double getRCmax() {
		return RCmax;
	}

	public void setRCmax(double rCmax) {
		RCmax = rCmax;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public double getRCMaxMach() {
		return RCMaxMach;
	}

	public void setRCMaxMach(double rCMaxMach) {
		RCMaxMach = rCMaxMach;
	}

	public double[] getPowerRequired() {
		return powerRequired;
	}

	public void setPowerRequired(double[] powerRequired) {
		this.powerRequired = powerRequired;
	}

	public double[] getPowerAvailable() {
		return powerAvailable;
	}

	public void setPowerAvailable(double[] powerAvailable) {
		this.powerAvailable = powerAvailable;
	}

	public double[] getRC() {
		return RC;
	}

	public void setRC(double[] rC) {
		RC = rC;
	}

	public double[] getSpeed() {
		return speed;
	}

	public void setSpeed(double[] speed) {
		this.speed = speed;
	}

	public double[] getGamma() {
		return gamma;
	}

	public void setGamma(double[] gamma) {
		this.gamma = gamma;
	}

	public double getRCMaxSpeed() {
		return RCMaxSpeed;
	}

}
