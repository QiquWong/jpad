package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import calculators.aerodynamics.DragCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.SpeedCalc;

public class RateOfClimbCalc {

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public static List<RCMap> calculateRC(
			List<Amount<Length>> altitude,
			Amount<Temperature> deltaTemperature,
			List<Double> phi,
			List<Amount<Mass>> weight,
			List<EngineOperatingConditionEnum> flightCondition,
			List<DragMap> listDrag,
			List<ThrustMap> listThrust
			) {

		List<Amount<Velocity>> speed = new ArrayList<>();
		List<Amount<Power>> powerAvailable, powerRequired;
		List<RCMap> list = new ArrayList<RCMap>();

		for(int f=0; f<flightCondition.size(); f++) {
			for (int p=0; p<phi.size(); p++) {
				for (int w=0; w<weight.size(); w++) { 
					for(int i=0; i<altitude.size(); i++) {
						Amount<Velocity> rcMax, rcMaxSpeed;

						powerAvailable = PerformanceDataManager.getPowerAvailable(altitude.get(i), deltaTemperature, phi.get(p), flightCondition.get(f), listThrust);
						powerRequired = PerformanceDataManager.getPowerRequired(altitude.get(i), deltaTemperature, weight.get(w), listDrag);
						speed = PerformanceDataManager.getSpeed(altitude.get(i), deltaTemperature, phi.get(p), flightCondition.get(f), listThrust);

						List<Amount<Velocity>> rc = new ArrayList<>();

						for (int pa=0; pa<powerAvailable.size(); pa++) {
							rc.add(Amount.valueOf(
											(powerAvailable.get(pa).doubleValue(SI.WATT) - powerRequired.get(pa).doubleValue(SI.WATT))/(weight.get(w).doubleValue(SI.KILOGRAM)*9.81),
											SI.METERS_PER_SECOND
											)
									); 
						}
						
						rcMax = Amount.valueOf(
								rc.stream().mapToDouble(r -> r.doubleValue(SI.METERS_PER_SECOND)).max().getAsDouble(),
								SI.METERS_PER_SECOND
								);
						int rcMaxSpeedIndex = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertListOfAmountTodoubleArray(rc));
						rcMaxSpeed = speed.get(rcMaxSpeedIndex);

						list.add(
								new RCMap(
										altitude.get(i),
										deltaTemperature,
										phi.get(p),
										powerRequired,
										powerAvailable, 
										rc,
										rcMax,
										weight.get(w), 
										flightCondition.get(f), 
										speed, 
										rcMaxSpeed
										)
								);
					}
				}
			}
		}

		System.out.println("------ Rate of Climb evaluation terminated ------");
		return list;
	}

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public static RCMap calculateRC(
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition,
			List<DragMap> listDrag,
			List<ThrustMap> listThrust
			) {

		List<Amount<Velocity>> speed = new ArrayList<>();
		Amount<Velocity> rcMax = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Velocity> rcMaxSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);

		List<Amount<Power>> powerAvailable = PerformanceDataManager.getPowerAvailable(altitude, deltaTemperature, phi, flightCondition, listThrust);
		List<Amount<Power>> powerRequired = PerformanceDataManager.getPowerRequired(altitude, deltaTemperature, weight, listDrag);
		speed = PerformanceDataManager.getSpeed(altitude, deltaTemperature, phi, flightCondition, listThrust);

		List<Amount<Velocity>> rc = new ArrayList<>();

		for (int pa=0; pa<powerAvailable.size(); pa++) {
			rc.add(Amount.valueOf(
							(powerAvailable.get(pa).doubleValue(SI.WATT) - powerRequired.get(pa).doubleValue(SI.WATT))/(weight.doubleValue(SI.KILOGRAM)*9.81),
							SI.METERS_PER_SECOND
							)
					); 

			if( rc.get(pa).doubleValue(SI.METERS_PER_SECOND) > rcMax.doubleValue(SI.METERS_PER_SECOND)) { 
				rcMax = rc.get(pa);
				rcMaxSpeed = speed.get(pa);
			}
		}

		return new RCMap(altitude, deltaTemperature, phi, powerRequired, powerAvailable, rc, rcMax, weight, flightCondition, speed, rcMaxSpeed);
				
	}

	/**
	 * Calculate the rate of climb supposing that the lift equals the weight
	 */
	public static Amount<Velocity> calculateRC(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Velocity> speed, double phi, Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition, EngineTypeEnum engineType,
			PowerPlant thePowerPlant,
			Amount<Force> t0, 
			EngineDatabaseReader engineDatabaseReader,
			Amount<Area> surface, double ar, Amount<Angle> sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cd0, double oswald) {
		
		Amount<Force> currentTotalThrust = Amount.valueOf(0.0, SI.NEWTON);
		for(int i=0; i<thePowerPlant.getEngineNumber(); i++)
			currentTotalThrust = Amount.valueOf(
					currentTotalThrust.doubleValue(SI.NEWTON)
					+ ThrustCalc.calculateThrustDatabase(
							t0, 
							engineDatabaseReader,
							flightCondition,
							altitude,
							SpeedCalc.calculateMach(altitude, deltaTemperature, speed), 
							deltaTemperature, 
							phi
							).doubleValue(SI.NEWTON),
					SI.NEWTON
					);
		
		Amount<Power> powerAvailable = 
				Amount.valueOf(
						speed.doubleValue(SI.METERS_PER_SECOND)
						*currentTotalThrust.doubleValue(SI.NEWTON), 
						SI.WATT
						);
		Amount<Power> powerRequired = 
				Amount.valueOf( 
						speed.doubleValue(SI.METERS_PER_SECOND)
						*DragCalc.calculateDragAtSpeedLevelFlight(
								weight, 
								altitude, 
								deltaTemperature,
								surface, 
								speed, 
								cd0, 
								ar,
								oswald, 
								sweepHalfChord,
								tcMax, 
								airfoilType
								).doubleValue(SI.NEWTON),
						SI.WATT
						);
		return Amount.valueOf(
				(powerAvailable.doubleValue(SI.WATT) - powerRequired.doubleValue(SI.WATT))/(weight.doubleValue(SI.KILOGRAM)*9.81),
				SI.METERS_PER_SECOND
				);
	}
}