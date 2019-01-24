package calculators.performance.customdata;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;

public class SpecificRangeMap extends PerformanceMap{

	private double[] specificRange, mach, sfc, efficiency;

	public SpecificRangeMap (
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			Amount<Mass> weight,
			EngineOperatingConditionEnum fligthCondition,
			double[] specificRange,
			double[] mach,
			double[] efficiency,
			double[] sfc
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

	public double[] getSpecificRange() {
		return specificRange;
	}

	public void setSpecificRange(double[] specificRange) {
		this.specificRange = specificRange;
	}

	public double[] getMach() {
		return mach;
	}

	public void setMach(double[] mach) {
		this.mach = mach;
	}

	public double[] getSfc() {
		return sfc;
	}

	public void setSfc(double[] sfc) {
		this.sfc = sfc;
	}

	public double[] getEfficiency() {
		return efficiency;
	}

	public void setEfficiency(double[] efficiency) {
		this.efficiency = efficiency;
	}

	
}

