package calculators.performance.customdata;

import configuration.enumerations.EngineOperatingConditionEnum;

public class SpecificRangeMap extends PerformanceMap{

	private Double[] specificRange;
	private Double[] mach;
	private Double[] sfc;
	private Double[] efficiency;

	public SpecificRangeMap (
			double altitude,
			double phi,
			double weight,
			EngineOperatingConditionEnum fligthCondition,
			Double[] specificRange,
			Double[] mach,
			Double[] efficiency,
			Double[] sfc
			) {
		
		this.altitude = altitude;
		this.phi = phi;
		this.weight = weight;
		this.flightCondition = fligthCondition;
		this.specificRange = specificRange;
		this.mach = mach;
		this.efficiency = efficiency;
		this.sfc = sfc;
	}

	/**
	 * @return the specificRange
	 */
	public Double[] getSpecificRange() {
		return specificRange;
	}

	/**
	 * @param specificRange the specificRange to set
	 */
	public void setSpecificRange(Double[] specificRange) {
		this.specificRange = specificRange;
	}

	/**
	 * @return the mach
	 */
	public Double[] getMach() {
		return mach;
	}

	/**
	 * @param mach the mach to set
	 */
	public void setMach(Double[] mach) {
		this.mach = mach;
	}

	/**
	 * @return the sfc
	 */
	public Double[] getSfc() {
		return sfc;
	}

	/**
	 * @param sfc the sfc to set
	 */
	public void setSfc(Double[] sfc) {
		this.sfc = sfc;
	}

	/**
	 * @return the efficiency
	 */
	public Double[] getEfficiency() {
		return efficiency;
	}

	/**
	 * @param efficiency the efficiency to set
	 */
	public void setEfficiency(Double[] efficiency) {
		this.efficiency = efficiency;
	};
	
}

