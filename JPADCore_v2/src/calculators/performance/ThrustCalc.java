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
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseReader;
import standaloneutils.atmosphere.SpeedCalc;

public class ThrustCalc {

	public static List<ThrustMap> calculateThrustAndPowerAvailable(
			List<Amount<Length>> altitude, Amount<Temperature> deltaTemperature, List<Double> phi, List<Amount<Mass>> weight,
			List<Amount<Velocity>> speed,
			List<EngineOperatingConditionEnum> flightCondition,
			PowerPlant thePowerPlant,
			boolean isOEI,
			double thrustCorrectionFactor
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
												isOEI,
												thrustCorrectionFactor
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
			boolean isOEI, double thrustCorrectionFactor
			) {
	
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
						isOEI,
						thrustCorrectionFactor
						),
				speed,
				flightCondition
				);
	}

	public static Amount<Force> calculateThrustDatabase(
			Amount<Force> t0, 
			EngineDatabaseReader engineDatabaseReader,
			EngineOperatingConditionEnum flightCondition,
			Amount<Length> altitude, double mach, Amount<Temperature> deltaTemperature, double phi,
			double thrustCorrectionFactor, EngineTypeEnum type, double etaPropeller
			) {
		
		double thrustRatio = engineDatabaseReader.getThrustRatio(
				mach,
				altitude,
				deltaTemperature,
				phi,
				flightCondition,
				thrustCorrectionFactor
				); 
		
		Amount<Force> thrust = Amount.valueOf(0.0, SI.NEWTON);
		if(type == EngineTypeEnum.TURBOPROP || type == EngineTypeEnum.PISTON) {
			if(Double.valueOf(etaPropeller) != null) {
				thrust = Amount.valueOf(
						thrustRatio*t0.doubleValue(SI.NEWTON)*phi*etaPropeller,
						SI.NEWTON
						);
			}
		}
		else if(type == EngineTypeEnum.TURBOFAN || type == EngineTypeEnum.TURBOJET) {
			thrust = Amount.valueOf(
					thrustRatio*t0.doubleValue(SI.NEWTON)*phi,
					SI.NEWTON
					);
		}
		
		return thrust;
	}
	
	public static List<Amount<Force>> calculateThrustVsSpeed(
			EngineOperatingConditionEnum flightCondition,  
			PowerPlant thePowerPlant, 
			List<Amount<Velocity>> speed, Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi, 
			boolean isOEI, double thrustCorrectionFactor
			) {
	
		List<Amount<Force>> thrust = new ArrayList<>();
	
		int engineNumber = thePowerPlant.getEngineNumber();
		if(isOEI == true) 
			engineNumber -= 1;
		
		for (int i=0; i< speed.size(); i++){
			List<Amount<Force>> thrustTemp = new ArrayList<>();
			double mach = SpeedCalc.calculateMach(altitude, deltaTemperature, speed.get(i));
			for (int ieng=0; ieng < engineNumber; ieng++)
				thrustTemp.add(
						calculateThrustDatabase(
								thePowerPlant.getEngineList().get(ieng).getT0(), 
								thePowerPlant.getEngineDatabaseReaderList().get(ieng),
								flightCondition, 
								altitude,
								mach,
								deltaTemperature,
								phi,
								thrustCorrectionFactor,
								thePowerPlant.getEngineList().get(ieng).getEngineType(), 
								thePowerPlant.getEngineList().get(ieng).getEtaPropeller()
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