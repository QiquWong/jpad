package calculators.performance.customdata;

import configuration.enumerations.EngineOperatingConditionEnum;


public class CeilingMap extends PerformanceMap{

	private double absoluteCeiling, serviceCeiling;

	public CeilingMap(double absoluteCeiling, double serviceCeiling, double weight, 
			double phi, double bpr, EngineOperatingConditionEnum flightCondition
			) {
		this.setAbsoluteCeiling(absoluteCeiling);
		this.phi = phi;
		this.bpr = bpr;
		this.flightCondition = flightCondition;
		this.setServiceCeiling(serviceCeiling);
		this.weight = weight;
	}

	public double getAbsoluteCeiling() {
		return absoluteCeiling;
	}

	public void setAbsoluteCeiling(double absoluteCeiling) {
		this.absoluteCeiling = absoluteCeiling;
	}

	public double getServiceCeiling() {
		return serviceCeiling;
	}

	public void setServiceCeiling(double serviceCeiling) {
		this.serviceCeiling = serviceCeiling;
	}

}

