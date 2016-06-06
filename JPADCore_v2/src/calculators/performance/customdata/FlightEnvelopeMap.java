package calculators.performance.customdata;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class FlightEnvelopeMap extends PerformanceMap{

	double maxSpeed, minSpeed, maxMach, minMach;

	public FlightEnvelopeMap( double altitude, double phi, double weight, 
			double BPR, EngineOperatingConditionEnum flightCondition, double maxSpeed, double minSpeed) {
		this.altitude = altitude;
		this.phi = phi;
		this.bpr = BPR;
		this.flightCondition = flightCondition;
		this.maxSpeed = maxSpeed;
		this.minSpeed = minSpeed;
		this.weight = weight;
		this.maxMach = SpeedCalc.calculateMach(altitude, maxSpeed);
		this.minMach = SpeedCalc.calculateMach(altitude, minSpeed);
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public double getMaxMach() {
		return maxMach;
	}

	public double getMinMach() {
		return minMach;
	}

}
