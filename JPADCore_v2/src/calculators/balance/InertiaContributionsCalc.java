package calculators.balance;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import standaloneutils.MyUnits;

public class InertiaContributionsCalc {

	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaMomentIxx (
			Amount<Mass> componentMass, 
			Amount<Length> componentYCG, 
			Amount<Length> aircraftYCG,
			Amount<Length> componentZCG, 
			Amount<Length> aircraftZCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (
						Math.pow(
								(componentYCG.doubleValue(SI.METER) - aircraftYCG.doubleValue(SI.METER)),
								2
								)
						+ Math.pow(
								(componentZCG.doubleValue(SI.METER) - aircraftZCG.doubleValue(SI.METER)),
								2
								)
						),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaMomentIyy (
			Amount<Mass> componentMass, 
			Amount<Length> componentZCG, 
			Amount<Length> aircraftZCG,
			Amount<Length> componentXCG, 
			Amount<Length> aircraftXCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (
						Math.pow(
								(componentZCG.doubleValue(SI.METER) - aircraftZCG.doubleValue(SI.METER)),
								2
								)
						+ Math.pow(
								(componentXCG.doubleValue(SI.METER) - aircraftXCG.doubleValue(SI.METER)),
								2
								)
						),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaMomentIzz (
			Amount<Mass> componentMass, 
			Amount<Length> componentXCG, 
			Amount<Length> aircraftXCG,
			Amount<Length> componentYCG, 
			Amount<Length> aircraftYCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (
						Math.pow(
								(componentXCG.doubleValue(SI.METER) - aircraftXCG.doubleValue(SI.METER)),
								2
								)
						+ Math.pow(
								(componentYCG.doubleValue(SI.METER) - aircraftYCG.doubleValue(SI.METER)),
								2
								)
						),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaProductIxy (
			Amount<Mass> componentMass, 
			Amount<Length> componentXCG, 
			Amount<Length> aircraftXCG,
			Amount<Length> componentYCG, 
			Amount<Length> aircraftYCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (componentXCG.doubleValue(SI.METER) - aircraftXCG.doubleValue(SI.METER))
				* (componentYCG.doubleValue(SI.METER) - aircraftYCG.doubleValue(SI.METER)),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaProductIyz (
			Amount<Mass> componentMass, 
			Amount<Length> componentYCG, 
			Amount<Length> aircraftYCG,
			Amount<Length> componentZCG, 
			Amount<Length> aircraftZCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (componentYCG.doubleValue(SI.METER) - aircraftYCG.doubleValue(SI.METER))
				* (componentZCG.doubleValue(SI.METER) - aircraftZCG.doubleValue(SI.METER)),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calculateComponentInertiaProductIxz (
			Amount<Mass> componentMass, 
			Amount<Length> componentXCG, 
			Amount<Length> aircraftXCG,
			Amount<Length> componentZCG, 
			Amount<Length> aircraftZCG
			) {
		
		return Amount.valueOf(
				componentMass.doubleValue(SI.KILOGRAM)
				* (componentXCG.doubleValue(SI.METER) - aircraftXCG.doubleValue(SI.METER))
				* (componentZCG.doubleValue(SI.METER) - aircraftZCG.doubleValue(SI.METER)),
				MyUnits.KILOGRAM_METER_SQUARED
				);
		
	}
	
}