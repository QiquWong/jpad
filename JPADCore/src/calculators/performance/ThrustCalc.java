package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import calculators.aerodynamics.DragCalc;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class ThrustCalc {

	/**
	 * 
	 * @param speed
	 * @param cl
	 * @param altitude
	 * @param cd0
	 * @param oswald
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param t0
	 * @param nEngine
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return
	 */
	public static double compareThrustAvailableToDrag(double speed, double weight, double cl,
			double altitude, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition) {
		
		double mach = SpeedCalc.calculateMach(altitude, speed);
		double thrust = ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, altitude, mach);
		double drag = DragCalc.calculateDragAtSpeed(weight, altitude, surface, speed, cd0, cl, ar, oswald, sweepHalfChord, tcMax, airfoilType);
		
		if (thrust >= drag) return thrust;
		else return -1.;
	}
	
	public static double compareThrustAvailableToDragGivenDensity(double speed, double weight, double cl,
			double density, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition) {
		return compareThrustAvailableToDrag(speed, weight, cl, AtmosphereCalc.getAltitude(density), cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, t0, nEngine, phi, bpr, engineType, flightCondition);
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param altitude (m)
	 * @param phi engine setting
	 * @param weight aircraft weights (N)
	 * @param speed (m/s)
	 * @param flightCondition
	 * @param t0 static thrust, 1 engine (N)
	 * @param nEngine number of engines
	 * @param bpr by-pass ratio
	 * @param surface wing reference surface (m2)
	 * @param ar aspect ratio
	 * @param oswald Oswald factor
	 * @param sweepHalfChord half chord wing sweep (rad)
	 * @param tcMax wing maximum thickness ratio
	 * @param airfoilType
	 * @param cLmax wing maximum CL
	 * @param cD0 whole aircraft CD0
	 * @return
	 */
	public static List<ThrustMap> calculateThrustAndPowerAvailable(
			double[] altitude, double[] phi, double[] weight,
			double[] speed,
			EngineOperatingConditionEnum[] flightCondition,
			EngineTypeEnum engineType, 
			double t0, int nEngine, double bpr,
			double surface, double ar, double oswald,
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cD0) {
	
		List<ThrustMap> list = new ArrayList<ThrustMap>();
	
		for(int f=0; f<flightCondition.length; f++) {
			for(int k=0; k<weight.length; k++) {
				for(int j=0; j<phi.length; j++) {
					for(int i=0; i<altitude.length; i++) {
	
						list.add(new ThrustMap(altitude[i], phi[j], 
								ThrustCalc.calculateThrustVsSpeed(
										t0, phi[j], altitude[i],
										flightCondition[f], engineType, bpr, nEngine, speed),
										speed, bpr, flightCondition[f]));
	
					}
				}
			}
		}
	
		System.out.println("------ Thrust and power available evaluated ------");
		return list;
	}

	public static ThrustMap calculateThrustAndPowerAvailable(
			double altitude, double phi, double[] speed,
			EngineOperatingConditionEnum flightCondition,
			EngineTypeEnum engineType,
			double t0, int nEngine, double bpr) {
	
		return new ThrustMap(altitude, phi, 
				ThrustCalc.calculateThrustVsSpeed(t0, phi, altitude,
						flightCondition, engineType, bpr, nEngine, speed),
						speed, bpr, flightCondition);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param t0 static thrust (N)
	 * @param nEngine number of engines
	 * @param phi engine power setting
	 * @param bpr by-pass ratio
	 * @param flightCondition
	 * @param altitude (m)
	 * @param mach
	 * @return
	 */
	public static double calculateThrustDatabase(
			double t0, double nEngine, double phi,
			double bpr, EngineTypeEnum engineType, 
			EngineOperatingConditionEnum flightCondition, double altitude, double mach
			) {
		
		return EngineDatabaseManager.getThrustRatio(mach, altitude, bpr, engineType, flightCondition)*
				t0*nEngine*phi;
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param t0 static thrust (N) per single engine
	 * @param nEngine number of engines
	 * @param phi engine power setting
	 * @param altitude (m)
	 * @return
	 */
	public static double calculateThrust(
			double t0, double nEngine, double phi, double altitude
			) {
		
		return t0*nEngine*0.71*phi*(AtmosphereCalc.getDensity(altitude)/AtmosphereCalc.getDensity(0.0));
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

	/**
	 * @author Lorenzo Attanasio
	 * @param t0 static thrust (N)
	 * @param phi
	 * @param altitude (m)
	 * @param flightCondition
	 * @param bpr by-pass ratio
	 * @param nEngine number of engines
	 * @param speed (m/s)
	 * @return
	 */
	public static double[] calculateThrustVsSpeed(
			double t0, double phi, double altitude, 
			EngineOperatingConditionEnum flightCondition, 
			EngineTypeEnum engineType, 
			double bpr, double nEngine,
			double speed[]) {
	
		double[] thrust = new double[speed.length];
	
		for (int i=0; i< speed.length; i++){
			double mach = SpeedCalc.calculateMach(altitude, speed[i]);
			if (engineType == EngineTypeEnum.TURBOPROP)
				thrust[i] = calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, altitude, mach);
//						thrust[i] = calculateThrustHowe(t0, nEngine, bpr, phi, altitude, mach);
			else if (engineType == EngineTypeEnum.TURBOFAN)
//				thrust[i] = calculateThrust(t0, nEngine, phi, altitude);
				thrust[i] = calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, altitude, mach);
		}
	
		return thrust;
	}

	/**
	 * Overload of the previous method allowing the usage of Double[]
	 * 
	 * @author Vittorio Trifari
	 * @param t0 static thrust (N)
	 * @param phi
	 * @param altitude (m)
	 * @param flightCondition
	 * @param bpr by-pass ratio
	 * @param nEngine number of engines
	 * @param speed (m/s)
	 * @return
	 */
}
