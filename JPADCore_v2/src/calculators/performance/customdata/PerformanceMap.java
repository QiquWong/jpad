package calculators.performance.customdata;

import java.util.ArrayList;
import java.util.List;

import configuration.enumerations.EngineOperatingConditionEnum;

public abstract class PerformanceMap{
	
	protected static List<? extends PerformanceMap> list = new ArrayList<>();
	protected double weight, bpr, phi, altitude;
	protected EngineOperatingConditionEnum flightCondition;

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getBpr() {
		return bpr;
	}

	public void setBPR(double bPR) {
		bpr = bPR;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public EngineOperatingConditionEnum getFlightCondition() {
		return flightCondition;
	}

	public void setFlightCondition(EngineOperatingConditionEnum flightCondition) {
		this.flightCondition = flightCondition;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	// TODO: this method should be removed if not used in the future
	public static List<? extends PerformanceMap> getList() {
		return list;
	}
}
