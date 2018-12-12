package calculators.performance.customdata;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.EngineOperatingConditionEnum;


public class CeilingMap extends PerformanceMap{

	private Amount<Length> absoluteCeiling, serviceCeiling;

	public CeilingMap(Amount<Length> absoluteCeiling, Amount<Length> serviceCeiling, Amount<Mass> weight, 
			double phi, EngineOperatingConditionEnum flightCondition ) {
		this.absoluteCeiling = absoluteCeiling;
		this.phi = phi;
		this.flightCondition = flightCondition;
		this.serviceCeiling = serviceCeiling;
		this.weight = weight;
	}

	public Amount<Length> getAbsoluteCeiling() {
		return absoluteCeiling;
	}

	public void setAbsoluteCeiling(Amount<Length> absoluteCeiling) {
		this.absoluteCeiling = absoluteCeiling;
	}

	public Amount<Length> getServiceCeiling() {
		return serviceCeiling;
	}

	public void setServiceCeiling(Amount<Length> serviceCeiling) {
		this.serviceCeiling = serviceCeiling;
	}

}

