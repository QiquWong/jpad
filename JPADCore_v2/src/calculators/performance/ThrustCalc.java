package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import calculators.aerodynamics.DragCalc;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class ThrustCalc {

	public static double compareThrustAvailableToDrag(double speed, double weight, double cl, double cd0, double oswald, double surface, 
			double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, double t0, int nEngine, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double phi, EngineOperatingConditionEnum flightCondition, PowerPlant thePowerPlant) {
		
		double mach = SpeedCalc.calculateMach(altitude.doubleValue(SI.METER), speed);
		double thrust = ThrustCalc.calculateThrustDatabase(t0, nEngine, flightCondition, thePowerPlant, altitude, mach, deltaTemperature, phi);
		double drag = DragCalc.calculateDragAtSpeed(weight, altitude.doubleValue(SI.METER), surface, speed, cd0, cl, ar, oswald, sweepHalfChord, tcMax, airfoilType);
		
		if (thrust >= drag) return thrust;
		else return -1.;
	}
	
	public static double compareThrustAvailableToDragGivenDensity(double speed, double weight, double cl, double cd0, double oswald, double surface, 
			double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, double t0, int nEngine, Amount<Length> altitude,
			Amount<Temperature> deltaTemperature, double phi, EngineOperatingConditionEnum flightCondition, PowerPlant thePowerPlant, double density) {
		return compareThrustAvailableToDrag(speed, weight, cl, cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, t0, nEngine, altitude, deltaTemperature, phi, flightCondition, thePowerPlant);
	}
	
	public static List<ThrustMap> calculateThrustAndPowerAvailable(
			Amount<Length>[] altitude, double[] phi, Amount<Mass>[] weight,
			double[] speed, Amount<Temperature>[] deltaTemperature,
			EngineOperatingConditionEnum[] flightCondition,
			PowerPlant thePowerPlant,
			double t0, int nEngine) {
	
		List<ThrustMap> list = new ArrayList<ThrustMap>();
	
		for(int f=0; f<flightCondition.length; f++) {
			for(int k=0; k<weight.length; k++) {
				for(int j=0; j<phi.length; j++) {
					for(int i=0; i<altitude.length; i++) {
						for(int dt=0; dt<deltaTemperature.length; dt++) {
						list.add(
								new ThrustMap(
										weight[k],
										altitude[i], 
										deltaTemperature[dt], 
										phi[j], 
										ThrustCalc.calculateThrustVsSpeed(
												t0, 
												flightCondition[f], 
												thePowerPlant, 
												nEngine, 
												speed, 
												altitude[i], 
												deltaTemperature[dt], 
												phi[j]), 
										speed, 
										flightCondition[f]
										)
								);
						}
					}
				}
			}
		}
	
		System.out.println("------ Thrust and power available evaluated ------");
		return list;
	}

	public static ThrustMap calculateThrustAndPowerAvailable(
			Amount<Length> altitude, double phi, Amount<Mass> weight,
			double[] speed, Amount<Temperature> deltaTemperature,
			EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant,
			double t0, int nEngine) {
	
		return new ThrustMap(
				weight,
				altitude,
				deltaTemperature,
				phi, 
				ThrustCalc.calculateThrustVsSpeed(
						t0,
						flightCondition,
						thePowerPlant,
						nEngine, 
						speed, 
						altitude, 
						deltaTemperature, 
						phi
						),
				speed,
				flightCondition
				);
	}

	public static double calculateThrustDatabase(
			double t0, int nEngine,  
			EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant,
			Amount<Length> altitude, double mach, Amount<Temperature> deltaTemperature, double phi) {
		
		List<Double> thrustRatio = new ArrayList<>();
		List<Double> tDisp = new ArrayList<>();
		for (int i=0; i<nEngine; i++) {
			thrustRatio.add(
					thePowerPlant.getEngineDatabaseReaderList().get(i).getThrustRatio(
							mach,
							altitude,
							deltaTemperature,
							phi,
							flightCondition
							)
					);
			tDisp.add(thrustRatio.get(i)*t0*nEngine*phi);
		}

		return tDisp.stream().mapToDouble(t -> t).sum();
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @see Howe page 66 (94 pdf)
	 * 
	 * @param t0 (N)
	 * @param nEngine
	 * @param bpr
	 * @param phi
	 * @param altitude (m)
	 * @param mach
	 * @return
	 */
	public static double calculateThrustHowe(double t0, double nEngine, double bpr, 
			double phi, double altitude, double mach) {
	
		double ft = 1., k1=1., k2=0., k3=-0.6, k4=0., s = 0.8, sigmas = 1.;
	
		if (bpr<1.) {
			if (mach<0.4) {k1 = 1.; k2 = 0.; k3=-0.2; k4 = 0.07;}
			if (mach>=0.4 && mach<=0.9) {k1 = 0.856; k2 = 0.062; k3=0.16; k4 = -0.23;}
			if (mach>0.9) {k1 = 1.; k2 = -0.145; k3=0.5; k4 = -0.05;}
		}
		if (bpr>=1. && bpr<=6.) {
			s = 0.7;
			if (mach<0.4) {k1 = 1.; k2 = 0.; k3=-0.6; k4 = -0.04;}
			if (mach>=0.4 && mach<=0.9) {k1 = 0.88; k2 = -0.016; k3=-0.3; k4 = 0.;}
		}
		if (bpr>=8) {
			s = 0.7;
			if (mach<0.4) {k1 = 1.; k2 = 0.; k3=-0.595; k4 = -0.03;}
			if (mach>=0.4 && mach<=0.9) {k1 = 0.89; k2 = -0.014; k3=-0.3; k4 = 0.005;}
		}
	
		sigmas = Math.pow(AtmosphereCalc.getDensity(altitude),s);
		if (mach<=0.9)
			return ft*( k1 + k2*bpr + (k3 + k4*bpr)*mach)*sigmas*nEngine*t0*phi;
		else
			return ft*(k1 + k2*bpr + (k3 + k4*bpr)*(mach - 0.9))*sigmas*nEngine*t0*phi;
	}

	public static double[] calculateThrustVsSpeed(
			double t0, EngineOperatingConditionEnum flightCondition,  
			PowerPlant thePowerPlant, int nEngine,
			double speed[], Amount<Length> altitude, Amount<Temperature> deltaTemperature, double phi) {
	
		double[] thrust = new double[speed.length];
	
		for (int i=0; i< speed.length; i++){
			double mach = SpeedCalc.calculateMach(altitude.doubleValue(SI.METER), speed[i]);
				thrust[i] = calculateThrustDatabase(t0, nEngine, flightCondition, thePowerPlant, altitude, mach, deltaTemperature, phi);
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
