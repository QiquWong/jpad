package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.SpeedCalc;

public class ThrustCalc {

	public static List<ThrustMap> calculateThrustAndPowerAvailable(
			List<Amount<Length>> altitude, Amount<Temperature> deltaTemperature, List<Double> phi, List<Amount<Mass>> weight,
			List<Amount<Velocity>> speed,
			List<EngineOperatingConditionEnum> flightCondition,
			PowerPlant thePowerPlant,
			boolean isOEI
			) {

		List<ThrustMap> list = new ArrayList<ThrustMap>();

		for(int f=0; f<flightCondition.size(); f++) {
			for(int k=0; k<weight.size(); k++) {
				for(int j=0; j<phi.size(); j++) {
					for(int i=0; i<altitude.size(); i++) {
						list.add(
								new ThrustMap(
										weight.get(k),
										altitude.get(i), 
										deltaTemperature, 
										phi.get(j), 
										ThrustCalc.calculateThrustVsSpeed(
												flightCondition.get(f), 
												thePowerPlant, 
												speed, 
												altitude.get(i), 
												deltaTemperature, 
												phi.get(j),
												isOEI
												), 
										speed, 
										flightCondition.get(f)
										)
								);
					}
				}
			}
		}

		System.out.println("------ Thrust and power available evaluated ------");
		return list;
	}

	public static ThrustMap calculateThrustAndPowerAvailable(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, Amount<Mass> weight,
			List<Amount<Velocity>> speed,
			EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant,
			boolean isOEI) {
	
		return new ThrustMap(
				weight,
				altitude,
				deltaTemperature,
				phi, 
				ThrustCalc.calculateThrustVsSpeed(
						flightCondition,
						thePowerPlant,
						speed, 
						altitude, 
						deltaTemperature, 
						phi,
						isOEI
						),
				speed,
				flightCondition
				);
	}

	public static Amount<Force> calculateThrustDatabase(
			Amount<Force> t0, 
			EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant,
			Amount<Length> altitude, double mach, Amount<Temperature> deltaTemperature, double phi) {
		
		List<Double> thrustRatio = new ArrayList<>();
		List<Double> tDisp = new ArrayList<>();
		for (int i=0; i<thePowerPlant.getEngineNumber(); i++) {
			thrustRatio.add(
					thePowerPlant.getEngineDatabaseReaderList().get(i).getThrustRatio(
							mach,
							altitude,
							deltaTemperature,
							phi,
							flightCondition
							)
					);
			tDisp.add(thrustRatio.get(i)*t0.doubleValue(SI.NEWTON)*phi);
		}

		return Amount.valueOf(
				tDisp.stream().mapToDouble(t -> t).sum(),
				SI.NEWTON
				);
	}
	
	public static List<Amount<Force>> calculateThrustVsSpeed(
			EngineOperatingConditionEnum flightCondition,  
			PowerPlant thePowerPlant, 
			List<Amount<Velocity>> speed, Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, boolean isOEI) {
	
		List<Amount<Force>> thrust = new ArrayList<>();
		List<Amount<Force>> thrustTemp = new ArrayList<>();
	
		int engineNumber = thePowerPlant.getEngineNumber();
		if(isOEI == true) 
			engineNumber -= 1;
		
		for (int i=0; i< speed.size(); i++){
			double mach = SpeedCalc.calculateMach(altitude, deltaTemperature, speed.get(i));
			for (int ieng=0; ieng < engineNumber; i++)
				thrustTemp.add(
						calculateThrustDatabase(
								thePowerPlant.getEngineList().get(ieng).getT0(), 
								flightCondition, 
								thePowerPlant,
								altitude,
								mach,
								deltaTemperature,
								phi
								)
						);
			thrust.add(
					Amount.valueOf(
							thrustTemp.stream().mapToDouble(t -> t.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON
							)
					);
		}
	
		return thrust;
	}

	/***************************************************************************************
	 * This method accepts the T0/P0 ratio in order to obtain the T0 from a given P0 for a 
	 * propeller engine
	 * 
	 * @author Vittorio Trifari
	 * @param p0 the static power of the engine
	 * @param p0T0ratio the ratio P0/T0
	 * @return
	 */
	public static double calculateT0fromP0 (double p0, double p0T0ratio) {
		
		return p0*p0T0ratio;
	}

}