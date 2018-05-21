package calculators.weights;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.NacelleMountingPositionEnum;
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
	 * page 243 Kundu - Aircraft Design.
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
	
	/*
	 * Roskam - Airplane Design - Part V (pag. 80)
	 */
	public static Amount<Mass> calculateTurbofanNacelleMassRoskam (Aircraft aircraft) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getPowerPlant().getEngineList().get(0).getBPR() > 4.) {
			mass = aircraft.getPowerPlant().getT0Total().to(NonSI.POUND_FORCE).times(0.065).to(SI.KILOGRAM);
		} else {
			mass = aircraft.getPowerPlant().getT0Total().to(NonSI.POUND_FORCE).times(0.055).to(SI.KILOGRAM);
		}
		
		return mass;
		
	}
	
	/*
	 * page 243 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculateTurbopropNacelleMassKundu (Aircraft aircraft) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.WING)
				|| aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.HTAIL)) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getP0Total().to(NonSI.HORSEPOWER).times(6.5).getEstimatedValue(),
					SI.KILOGRAM
					);
		} 
		else if(aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.UNDERCARRIAGE_HOUSING)) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getP0Total().to(NonSI.HORSEPOWER).times(8).getEstimatedValue(),
					SI.KILOGRAM
					);
		} 
		else if(aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.FUSELAGE)) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getP0Total().to(NonSI.HORSEPOWER).times(28).getEstimatedValue(),
					SI.KILOGRAM
					);
		}
		
		return mass;
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag.284
	 */
	public static Amount<Mass> calculateTurbopropNacelleMassTorenbeek1976 (Aircraft aircraft) {
		
		double jetThrustRatio = 0.10;
		Amount<Power> engineESPH = Amount.valueOf(
				aircraft.getPowerPlant().getP0Total().doubleValue(SI.WATT) 
				+ ((jetThrustRatio*aircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON))/14.92),
				SI.WATT
				).to(NonSI.HORSEPOWER);
		
		return Amount.valueOf(
				engineESPH.to(NonSI.HORSEPOWER).times(0.0635).getEstimatedValue(),
				SI.KILOGRAM
				);
		
	}
	
	/*
	 * page 243 Kundu - Aircraft Design.
	 */
	public static Amount<Mass> calculatePistonNacelleMassKundu (Aircraft aircraft) {
		
		Amount<Mass> mass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		if(aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.WING)
				|| aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.HTAIL)) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getP0Total().to(NonSI.HORSEPOWER).times(0.5).getEstimatedValue(),
					SI.KILOGRAM
					);
		} 
		else if(aircraft.getNacelles().getNacellesList().get(0).getMountingPosition().equals(NacelleMountingPositionEnum.FUSELAGE)) {
			mass = Amount.valueOf(
					aircraft.getPowerPlant().getP0Total().to(NonSI.HORSEPOWER).times(0.4).getEstimatedValue(),
					SI.KILOGRAM
					);
		}
		
		return mass;
		
	}
	
}
