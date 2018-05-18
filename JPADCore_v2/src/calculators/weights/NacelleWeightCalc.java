package calculators.weights;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import standaloneutils.atmosphere.AtmosphereCalc;

public class NacelleWeightCalc {

	/*
	 * page 150 Jenkinson - Civil Jet Aircraft Design
	 */
	public static Amount<Mass> calculateTurbofanNacelleMassJenkinson (Aircraft aircraft) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if (aircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON) < 600000.) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)/1000.*6.8, 
					SI.KILOGRAM);
		
		} else {
			mass = Amount.valueOf(
					2760 + (2.2*aircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON)), 
					SI.KILOGRAM);
		}
		
		return mass;
		
	}
	
	/*
	 * page 287 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculateTurbofanNacelleMassKundu (Aircraft aircraft) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getBPR() > 4.) {
			mass = aircraft.getPowerPlant().getT0Total().divide(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).to(SI.KILOGRAM).times(6.7);
		} else {
			mass = aircraft.getPowerPlant().getT0Total().divide(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).to(SI.KILOGRAM).times(6.2);
		}
		
		return mass;
		
	}
	
	public static Amount<Mass> calculateTurbofanNacelleMassTorenbeek1982 (Aircraft aircraft) {
		
		return aircraft.getPowerPlant().getT0Total().to(NonSI.POUND_FORCE).times(0.055).to(SI.KILOGRAM);
		
	}
	
}
