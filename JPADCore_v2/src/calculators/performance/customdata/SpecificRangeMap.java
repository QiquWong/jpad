package calculators.performance.customdata;

import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;

public class SpecificRangeMap extends PerformanceMap{

	private List<Double> specificRange, mach, sfc, efficiency;

	public SpecificRangeMap (
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			Amount<Mass> weight,
			EngineOperatingConditionEnum fligthCondition,
			List<Double> specificRange,
			List<Double> mach,
			List<Double> efficiency,
			List<Double> sfc
			) {
		
		this.altitude = altitude;
		this.deltaTemperature = deltaTemperature;
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
	public List<Double> getSpecificRange() {
		return specificRange;
	}

	/**
	 * @param specificRange the specificRange to set
	 */
	public void setSpecificRange(List<Double> specificRange) {
		this.specificRange = specificRange;
	}

	/**
	 * @return the mach
	 */
	public List<Double> getMach() {
		return mach;
	}

	/**
	 * @param mach the mach to set
	 */
	public void setMach(List<Double> mach) {
		this.mach = mach;
	}

	/**
	 * @return the sfc
	 */
	public List<Double> getSfc() {
		return sfc;
	}

	/**
	 * @param sfc the sfc to set
	 */
	public void setSfc(List<Double> sfc) {
		this.sfc = sfc;
	}

	/**
	 * @return the efficiency
	 */
	public List<Double> getEfficiency() {
		return efficiency;
	}

	/**
	 * @param efficiency the efficiency to set
	 */
	public void setEfficiency(List<Double> efficiency) {
		this.efficiency = efficiency;
	};
	
}

