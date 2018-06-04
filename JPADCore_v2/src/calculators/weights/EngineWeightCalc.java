package calculators.weights;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import standaloneutils.atmosphere.AtmosphereCalc;

public class EngineWeightCalc {

	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag.285
	 */
	public static Amount<Mass> calculateTurbofanEngineMassTorenbeek1976 (Aircraft aircraft) {

		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain() == null) {
			System.err.println("WARNING (WEIGHT ANALYSIS - ENGINE): THE ENGINE DRY MASS HAS NOT BEEN ASSIGNED OR CALCULATED. ENGINE MASS SET TO 0.0 kg ... ");
			return mass;
		}
		
		double kpg = 1.15;
		double kthr = 1.18;
		mass = aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().to(NonSI.POUND)
				.times(kpg)
				.times(kthr)
				.to(SI.KILOGRAM);
		
		return mass;
		
	}
	
	/*
	 * Torenbeek: Advanced Aircraft Design (2013) pag.238
	 */
	public static Amount<Mass> calculateTurbofanEngineMassTorenbeek2013 (Aircraft aircraft) {

		return Amount.valueOf(
				(aircraft.getPowerPlant().getEngineList().get(0).getT0().to(SI.NEWTON).times(0.25).doubleValue(SI.NEWTON) 
				+ 8000)
				/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 245 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculateTurbofanEngineMassKundu (Aircraft aircraft) {

		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain() == null) {
			System.err.println("WARNING (WEIGHT ANALYSIS - ENGINE): THE ENGINE DRY MASS HAS NOT BEEN ASSIGNED OR CALCULATED. ENGINE MASS SET TO 0.0 kg ... ");
			return mass;
		}
		
		mass = aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().to(SI.KILOGRAM)
				.times(1.5)
				.to(SI.KILOGRAM);
		
		return mass;
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag.285
	 */
	public static Amount<Mass> calculateTurbopropEngineMassTorenbeek1976 (Aircraft aircraft) {

		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain() == null) {
			System.err.println("WARNING (WEIGHT ANALYSIS - ENGINE): THE ENGINE DRY MASS HAS NOT BEEN ASSIGNED OR CALCULATED. ENGINE MASS SET TO 0.0 kg ... ");
			return mass;
		}
		
		double kpg = 1.35;
		mass = Amount.valueOf(
				kpg
				* (aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().doubleValue(NonSI.POUND)
						+ 0.24*aircraft.getPowerPlant().getEngineList().get(0).getP0().doubleValue(NonSI.HORSEPOWER)),
				NonSI.POUND
				).to(SI.KILOGRAM);
		
		return mass;
		
	}

	/*
	 * page 245 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculateTurbopropEngineMassKundu (Aircraft aircraft) {

		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain() == null) {
			System.err.println("WARNING (WEIGHT ANALYSIS - ENGINE): THE ENGINE DRY MASS HAS NOT BEEN ASSIGNED OR CALCULATED. ENGINE MASS SET TO 0.0 kg ... ");
			return mass;
		}
		
		mass = aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().to(NonSI.POUND)
				.times(1.45)
				.to(SI.KILOGRAM);
		
		return mass;
		
	}
	
	/*
	 * page 245 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculatePistonEngineMassKundu (Aircraft aircraft) {

		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain() == null) {
			System.err.println("WARNING (WEIGHT ANALYSIS - ENGINE): THE ENGINE DRY MASS HAS NOT BEEN ASSIGNED OR CALCULATED. ENGINE MASS SET TO 0.0 kg ... ");
			return mass;
		}
		
		mass = aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().to(NonSI.POUND)
				.times(1.45)
				.to(SI.KILOGRAM);
		
		return mass;
		
	}
	
}
