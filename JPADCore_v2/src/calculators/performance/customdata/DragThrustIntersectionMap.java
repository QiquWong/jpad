package calculators.performance.customdata;

import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class DragThrustIntersectionMap extends PerformanceMap{

	private double minSpeed, maxSpeed, minMach, maxMach;
	double[] speed;

	public DragThrustIntersectionMap(
			double altitude, double phi, double weight,
			double bpr, EngineOperatingConditionEnum fligthCondition,
			double[] speed, double speedMin, double speedMax,
			double surface, double cLmax
			) {
		this.altitude = altitude;
		this.phi = phi;
//		this.intersectionPoints = intersectionPoints;
		this.bpr = bpr;
		this.weight = weight;
		this.flightCondition = fligthCondition;

		this.speed = speed;
//		double[] cmp = MyArrayUtils.sumArraysElementByElement(this.speed, intersectionPoints);
//for (int i=0; i<cmp.length; i++)
//	if (speed[i] != cmp[i])
//		
//		int[] idx = MyArrayUtils.getNonZeroValuesIndex(intersectionPoints);
//		
//		if (idx.length == 2) {
//			this.minSpeed = speed[idx[0]];
//			double stallSpeed = MySpeedCalc.calculateSpeedStall(altitude, weight, surface, cLmax);
//			if (stallSpeed > this.minSpeed) this.minSpeed = stallSpeed;
//			
//			this.maxSpeed = speed[idx[1]]; 
//		}
//		if (idx.length == 1) {
//			this.minSpeed = MySpeedCalc.calculateSpeedStall(altitude, weight, surface, cLmax);
//			this.maxSpeed = speed[idx[0]];
//		}
//		if (idx.length == 0) {
//			this.minSpeed = 0.;
//			this.maxSpeed = 0.;
//		}
		this.minSpeed = speedMin;
		this.maxSpeed = speedMax;
		this.minMach = SpeedCalc.calculateMach(altitude, minSpeed);
		this.maxMach = SpeedCalc.calculateMach(altitude, maxSpeed);

	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMinMach() {
		return minMach;
	}

	public void setMinMach(double minMach) {
		this.minMach = minMach;
	}

	public double getMaxMach() {
		return maxMach;
	}

	public void setMaxMach(double maxMach) {
		this.maxMach = maxMach;
	}

//	public double[] getIntersectionPoints() {
//		return intersectionPoints;
//	}
//
//	public void setIntersectionPoints(double[] intersectionPoints) {
//		this.intersectionPoints = intersectionPoints;
//	}

	public double[] getSpeed() {
		return speed;
	}

	public void setSpeed(double[] speed) {
		this.speed = speed;
	}
	
}
