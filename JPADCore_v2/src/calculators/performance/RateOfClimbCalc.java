package calculators.performance;

import java.util.ArrayList;
import java.util.List;

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
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class RateOfClimbCalc {

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param flightCondition
	 * @param phi
	 * @param listDrag
	 * @param listThrust
	 * @return
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
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param flightCondition
	 * @param phi
	 * @param listDrag
	 * @param listThrust
	 * @return
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
	 * 
	 * @param altitude (m)
	 * @param speed (m/s)
	 * @param phi
	 * @param weight (N)
	 * @param flightCondition
	 * @param t0 (N)
	 * @param nEngine
	 * @param bpr
	 * @param surface (m2)
	 * @param ar 
	 * @param sweepHalfChord (radian)
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cd0
	 * @param oswald
	 * @return rate of climb (m/s)
	 */
	public static double calculateRC(
			double altitude, double speed, double phi, double weight,
			EngineOperatingConditionEnum flightCondition, EngineTypeEnum engineType,
			PowerPlant thePowerPlant,
			double t0, int nEngine, double bpr,
			double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cd0, double oswald) {
		
		double powerAvailable = speed*ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, altitude, SpeedCalc.calculateMach(altitude, speed));
		double powerRequired = speed*DragCalc.calculateDragAtSpeedLevelFlight(weight, altitude, surface, speed, cd0, ar, oswald, sweepHalfChord, tcMax, airfoilType);
		return (powerAvailable - powerRequired)/weight;
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param w weight (N)
	 * @param s surface (m2)
	 * @param gamma gamma function
	 * @param rho density (kg/m3)
	 * @param cd0 zero lift drag coefficient
	 * @param t thrust (N)
	 * @param emax maximum efficiency L/D
	 * @return
	 */
	public static double calculateRCmax(double w, double s, 
			double gamma, double rho, double cd0, 
			double t, double emax) {
		return Math.pow((w/s)*gamma/(3.*rho*cd0), 0.5) 
				* Math.pow(t/w, 1.5)
				* (1. - gamma/6. - 3./(2*(t*t/(w*w))*emax*emax*gamma));
	}

	/**
	 * Evaluate maximum rate of climb
	 * 
	 * @author Lorenzo Attanasio
	 * @param w weight (N)
	 * @param s wing reference surface (m2)
	 * @param rho density (kg/m3)
	 * @param cd0 zero lift drag coefficient
	 * @param t thrust(N) 
	 * @param emax maximum efficiency L/D
	 * @return
	 */
	public static double calculateRCmax(double w, double s, 
			double rho, double cd0, double t, double emax) {
		return calculateRCmax(w, s, calculateGamma(emax, t, w), rho, cd0, t, emax);
	}

	/**
	 * 
	 * @param w
	 * @param s
	 * @param altitude
	 * @param cd0
	 * @param emax
	 * @param t0
	 * @param nEngine
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return
	 */
	public static double calculateRCmax(double w, double s, 
			double altitude, double mach, double cd0, double emax,
			double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType,
			EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant
			) {

		double thrust = ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, altitude, mach);
		return calculateRCmax(w, s, calculateGamma(emax, thrust, w), AtmosphereCalc.getDensity(altitude), cd0, 
				thrust, emax);
	}

	/**
	 * Evaluate maximum rate of climb, propeller engine
	 * 
	 * @author Lorenzo Attanasio
	 * @param etap propeller efficiency
	 * @param pa power available
	 * @param w weight (N)
	 * @param sigma density ratio
	 * @param ar aspect ratio
	 * @param cD0 zero lift drag coefficient
	 * @param s wing surface (m2)
	 * @return
	 */
	public static double calculateRCmaxProp(double etap, double pa, double w, 
			double sigma, double ar, double cD0, double s) {
		return 76.*etap*pa/w 
				- 2.97*Math.sqrt(w/sigma)*Math.pow(cD0, 0.25)/(Math.pow(ar, 0.75)*Math.pow(s, 0.5));
	}

	/**
	 * Gamma function used in RCmax evaluation
	 * 
	 * @author Lorenzo Attanasio
	 * @param emax maximum efficiency L/D
	 * @param t thrust (N)
	 * @param w weight (N)
	 * @return
	 */
	public static double calculateGamma(double emax, double t, double w) {
		return 1. + Math.sqrt(1. + 3./(emax*emax*t*t/(w*w)));
	}

}
