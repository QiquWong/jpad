package aircraft.componentmodel.componentcalcmanager;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.MethodEnum;

public abstract class WeightsManager extends ComponentCalculator{

	protected Amount<Mass> _mass, _massReference, _massEstimated;
	
	protected Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	protected Double _compositeCorretionFactor, _massCorrectionFactor;
	
	
}