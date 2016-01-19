package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import calculators.aerodynamics.DragCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class RateOfClimbCalc {

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param flightCondition
	 * @param t0
	 * @param nEngine
	 * @param bpr
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cD0
	 * @param oswald
	 * @param listDrag
	 * @param listThrust
	 * @return
	 */
	public static List<RCMap> calculateRC(
			double[] altitude, double[] phi, double[] weight,
			EngineOperatingConditionEnum[] flightCondition,
			double t0, int nEngine, double bpr,
			double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cD0, double oswald,
			List<DragMap> listDrag, List<ThrustMap> listThrust
			) {

		double[] speed = null;
		double[] powerAvailable, powerRequired;
		double RCMax = 0., RCMaxSpeed = 0.;
		List<RCMap> list = new ArrayList<RCMap>();

		for(int f=0; f<flightCondition.length; f++) {
			for (int p=0; p<phi.length; p++) {
				for (int w=0; w<weight.length; w++) { 
					for(int i=0; i<altitude.length; i++) {

						powerAvailable = PerformanceDataManager.getPowerAvailable(altitude[i], phi[p],
								flightCondition[f], bpr, listThrust);
						powerRequired = PerformanceDataManager.getPowerRequired(altitude[i],
								weight[w], listDrag);
						speed = PerformanceDataManager.getSpeed(altitude[i], phi[p],
								flightCondition[f], bpr, listThrust);

						double[] RC = new double[powerAvailable.length];

						for (int pa=0; pa<powerAvailable.length; pa++) {
							RC[pa] = (powerAvailable[pa] - powerRequired[pa])/weight[w]; 
							//							gamma[pa] = Math.asin(RC[pa]/speed[pa]);

							if( RC[pa] > RCMax) { 
								RCMax = RC[pa];
								RCMaxSpeed = speed[pa];
								//								theta = Math.asin(RCMax/RCMaxSpeed);
							}
						}

						list.add(new RCMap(altitude[i], phi[p], powerRequired, 
								powerAvailable, RC, RCMax, bpr, weight[w], 
								flightCondition[f], speed, RCMaxSpeed));

					}
				}
			}
		}

		System.out.println("------ Rate of Climb evaluation terminated ------");
		return list;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param flightCondition
	 * @param t0
	 * @param nEngine
	 * @param bpr
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cD0
	 * @param oswald
	 * @param listDrag
	 * @param listThrust
	 * @return
	 */
	public static RCMap calculateRC(
			double altitude, double phi, double weight,
			EngineOperatingConditionEnum flightCondition, EngineTypeEnum engineType,
			double t0, int nEngine, double bpr,
			double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cD0, double oswald,
			List<DragMap> listDrag, List<ThrustMap> listThrust
			) {

		double[] speed = null;
		double RCMax = 0.;
		double RCMaxSpeed = 0.;

		double[] powerAvailable = PerformanceDataManager.getPowerAvailable(altitude, phi,
				flightCondition, bpr, listThrust);
		double[] powerRequired = PerformanceDataManager.getPowerRequired(altitude,
				weight, listDrag);
		speed = PerformanceDataManager.getSpeed(altitude, phi,
				flightCondition, bpr, listThrust);

		double[] RC = new double[powerAvailable.length];

		for (int pa=0; pa<powerAvailable.length; pa++) {
			RC[pa] = (powerAvailable[pa] - powerRequired[pa])/weight; 
			//			gamma[pa] = Math.asin(RC[pa]/speed[pa]);

			if( RC[pa] > RCMax) { 
				RCMax = RC[pa];
				RCMaxSpeed = speed[pa];
				//				theta = Math.asin(RCMax/RCMaxSpeed);
			}
		}

		return new RCMap(altitude, phi, powerRequired, 
				powerAvailable, RC, RCMax, bpr, weight, 
				flightCondition, speed, RCMaxSpeed);
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
			double t0, int nEngine, double bpr,
			double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cd0, double oswald) {
		
		double powerAvailable = speed*ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, altitude, SpeedCalc.calculateMach(altitude, speed));
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
			EngineOperatingConditionEnum flightCondition) {

		double thrust = ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, altitude, mach);
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
