package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;

import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.jscience.physics.amount.Amount;

import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.FlightEnvelopeMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.SpeedCalc;

public class PerformanceCalcUtils {

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude (m)
	 * @param speed (m/s)
	 * @param weight (N)
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param surface (m2)
	 * @param cLmax
	 * @param listDrag
	 * @param listThrust
	 * @return
	 */
	public static List<DragThrustIntersectionMap> calculateDragThrustIntersection(
			double[] altitude, double[] speed, double[] weight,
			double[] phi, EngineOperatingConditionEnum[] flightCondition,
			double bpr,
			double surface, double cLmax,
			List<DragMap> listDrag, List<ThrustMap> listThrust) {

		List<DragThrustIntersectionMap> list = new ArrayList<DragThrustIntersectionMap>();

		for(int f=0; f<flightCondition.length; f++) {
			for (int j=0; j<phi.length; j++) {
				for (int w=0; w<weight.length; w++) {
					for (int i=0; i<altitude.length; i++){

						list.add(calculateDragThrustIntersection(
								altitude[i], speed, weight[w], phi[j], flightCondition[f], 
								bpr, surface, cLmax, listDrag, listThrust));
					}
				}
			}
		}

		System.out.println("------ calculateIntersection terminated ------");
		return list;
	}

	public static DragThrustIntersectionMap calculateDragThrustIntersection(
			double altitude, double[] speed, double weight,
			double phi, EngineOperatingConditionEnum flightCondition,
			double bpr, double surface, double cLmax,
			List<DragMap> listDrag, List<ThrustMap> listThrust) {

		double[] thrust = PerformanceDataManager.getThrust(altitude, phi,
				bpr, flightCondition, listThrust);
		double[] drag = PerformanceDataManager.getDrag(altitude, weight, listDrag);

		Double speedMin = null, speedMax = null;
		double stallSpeed = SpeedCalc.calculateSpeedStall(altitude, weight, surface, cLmax);

		//		double[] diff = MyArrayUtils.abs(MyArrayUtils.MathArrays.ebeSubtract(thrust, drag));
		//		double[] diff2 = diff;
		//		Arrays.sort(diff2);
		//		
		//		int min1 = ArrayUtils.indexOf(diff, diff2[0]);
		//		int min2 = ArrayUtils.indexOf(diff, diff2[1]);
		//		
		//		double lowerBound = speed[min1+1];
		//		double upperBound = speed[min2-1];
		//		
		//		if (lowerBound < speed[0] || upperBound < speed[0]) {
		//			lowerBound = speed[0];
		//			upperBound = speed[1];
		//		} else if (lowerBound > speed[speed.length - 1] || upperBound > speed[speed.length - 1]) {
		//			lowerBound = speed[speed.length - 2];
		//			upperBound = speed[speed.length - 1];
		//		} 

		speedMin = MyArrayUtils.intersectArraysBrent(speed, thrust, drag, stallSpeed, stallSpeed+30, AllowedSolution.BELOW_SIDE);
		speedMax = MyArrayUtils.intersectArraysBrent(speed, thrust, drag, stallSpeed+30, speed[speed.length-1], AllowedSolution.ABOVE_SIDE);

		if (speedMin != null && stallSpeed > speedMin) speedMin = stallSpeed;
		else if (speedMin == null && speedMax != null) speedMin = stallSpeed;
		else if (speedMax == null && speedMin == null) { 
			speedMin = 0.;
			speedMax = 0.;
		}

		if (speedMax == null) speedMax = 0.;

		return new DragThrustIntersectionMap(
				altitude, phi, weight,
				bpr, flightCondition,
				speed, speedMin.doubleValue(), speedMax.doubleValue(),
				surface, cLmax);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param flightCondition
	 * @param bpr
	 * @param listRC
	 * @return
	 */
	public static List<CeilingMap> calculateCeiling(
			double[] altitude,
			double[] phi, 
			double[] weight, 
			EngineOperatingConditionEnum[] flightCondition,
			double bpr,
			List<RCMap> listRC) {

		int nAlt = altitude.length;
		double slope, absoluteCeiling = 0., serviceCeiling = 0.;
		double[] RCMaxAtAltitude = new double[nAlt];
		List<CeilingMap> ceilingList = new ArrayList<CeilingMap>();

		for(int f=0; f<flightCondition.length; f++) {
			for (int p=0; p<phi.length; p++) {
				for (int w=0; w<weight.length; w++) { 
					for (int i=0; i<altitude.length; i++) {
						RCMaxAtAltitude[i] = PerformanceDataManager.getRCmax(altitude[i], weight[w],
								phi[p], flightCondition[f], bpr, listRC);
					}

					int M=0;
					for (int i=0; i<nAlt; i++){
						if (RCMaxAtAltitude[i] > 0.) M=M+1;
					}

					try {
						slope = MyMathUtils.calculateSlopeLinear(
								RCMaxAtAltitude[M-1], RCMaxAtAltitude[M-2], 
								altitude[M-1], altitude[M-2]);
						absoluteCeiling = calculateCeilingInterp( 0.0, altitude[M-2], 
								RCMaxAtAltitude[M-2], slope);
						serviceCeiling = calculateCeilingInterp( 0.5, altitude[M-2], 
								RCMaxAtAltitude[M-2], slope);

					} catch (ArrayIndexOutOfBoundsException e) { }

					ceilingList.add(new CeilingMap(absoluteCeiling, serviceCeiling, weight[w], phi[p],
							bpr, flightCondition[f]));

				}
			}
		}

		return ceilingList;
	}

	/**
	 * 
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param flightCondition
	 * @param bpr
	 * @param listRC
	 * @return
	 */
	public static CeilingMap calculateCeiling(
			double[] altitude,
			double phi, 
			double weight, 
			EngineOperatingConditionEnum flightCondition,
			double bpr,
			List<RCMap> listRC) {

		int nAlt = altitude.length;
		double[] RCMaxAtAltitude = new double[nAlt];

		for (int i=0; i < nAlt; i++) {
			RCMaxAtAltitude[i] = PerformanceDataManager.getRCmax(altitude[i], weight,
					phi, flightCondition, bpr, listRC);
		}

		int M=2;
		for (int i=0; i < nAlt; i++){
			if (RCMaxAtAltitude[i] != 0.) M=M+1;
		}

		double K = MyMathUtils.calculateSlopeLinear( RCMaxAtAltitude[M-1], RCMaxAtAltitude[M-2], 
				altitude[M-1], altitude[M-2]);
		double absoluteCeiling = calculateCeilingInterp( 0.0, altitude[M-2], 
				RCMaxAtAltitude[M-2], K);
		double serviceCeiling = calculateCeilingInterp( 0.5, altitude[M-2], 
				RCMaxAtAltitude[M-2], K);

		return new CeilingMap(absoluteCeiling, serviceCeiling, weight, phi,
				bpr, flightCondition);
	}

	/**FIXME: this method still needs to be tested
	 * @author Lorenzo Attanasio
	 * @deprecated
	 * @param speed
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
	 * @param cd0
	 * @param oswald
	 * @return
	 */
	public static double calculateCeiling(
			double speed, double phi, double weight,
			EngineOperatingConditionEnum flightCondition, EngineTypeEnum engineType,
			double t0, int nEngine, double bpr,
			double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, 
			double cd0, double oswald) {

		double rc = 0.001;
		double[] altitude = MyArrayUtils.linspace(0., 13000., 100);

		for (int i=altitude.length-1; i>0; i--) {
			rc = RateOfClimbCalc.calculateRC(altitude[i], speed, phi, weight, flightCondition, engineType,
					t0, nEngine, bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, cd0, oswald);
			if (Math.abs(rc) < 0.1) return altitude[i+1];
		}

		return altitude[altitude.length-1];
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude (m)
	 * @param weight (N)
	 * @param phi
	 * @param bpr
	 * @param surface (m2)
	 * @param cLmax
	 * @param flightCondition
	 * @param listDragThrust
	 * @return
	 */
	public static List<FlightEnvelopeMap> calculateEnvelope(
			double[] altitude, double[] weight,
			double[] phi, double bpr,
			double surface, double cLmax,
			EngineOperatingConditionEnum[] flightCondition,
			List<DragThrustIntersectionMap> listDragThrust){

		int nAlt=altitude.length;
		double[] maxSpeed = new double[nAlt];
		double[] minSpeed = new double[nAlt];
		double[] maxMach = new double[nAlt];
		double[] minMach = new double[nAlt];
		List<FlightEnvelopeMap> list = new ArrayList<FlightEnvelopeMap>();

		for(int f=0; f<flightCondition.length; f++) {
			for (int p=0; p<phi.length; p++) {
				for (int w=0; w<weight.length; w++) {
					for (int i=0; i<altitude.length; i++) {

						maxSpeed[i] = PerformanceDataManager.getMaxSpeed(altitude[i], weight[w], phi[p], flightCondition[f], bpr, listDragThrust);
						minSpeed[i] = PerformanceDataManager.getMinSpeed(altitude[i], weight[w], phi[p], flightCondition[f], bpr, listDragThrust);
						maxMach[i] = PerformanceDataManager.getMaxMach(altitude[i], weight[w], phi[p], flightCondition[f], bpr, listDragThrust);
						minMach[i] = PerformanceDataManager.getMinMach(altitude[i], weight[w], phi[p], flightCondition[f], bpr, listDragThrust);

						list.add(new FlightEnvelopeMap(
								altitude[i], phi[p], weight[w], bpr, flightCondition[f],
								maxSpeed[i], minSpeed[i]));
					}
				}
			}
		}

		return list;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param surface
	 * @param cLmax
	 * @param flightCondition
	 * @param listDragThrust
	 * @return
	 */
	public static FlightEnvelopeMap calculateEnvelope(
			double altitude, double weight,
			double phi, double bpr,
			double surface, double cLmax,
			EngineOperatingConditionEnum flightCondition,
			List<DragThrustIntersectionMap> listDragThrust){

		double maxSpeed = PerformanceDataManager.getMaxSpeed(altitude, weight, phi, flightCondition, bpr, listDragThrust);
		double minSpeed = PerformanceDataManager.getMinSpeed(altitude, weight, phi, flightCondition, bpr, listDragThrust);

		return new FlightEnvelopeMap(
				altitude, phi, weight, bpr, flightCondition,
				maxSpeed, minSpeed);

	}
	
	/**
	 * 
	 * @param range
	 * @param climbDescentTime
	 * @param cruiseSpeed
	 * @return
	 */
	public static Amount<Duration> calcCruiseTime(Amount<Length> range, Amount<Duration> climbDescentTime,
			Amount<Velocity> cruiseSpeed) {

		double ka = 0.0; // Airway distance increment is a coefficient defined by Jenkinson
		double tc = 0.0; // Cruise time, double value in hr
		Amount<Length> trasholdRange = Amount.valueOf(1400, NonSI.MILE);

		if(range.equals(trasholdRange) || range.isLessThan(trasholdRange))
			ka = (7.0 + 0.015 * range.doubleValue(NonSI.MILE));
		else 
			ka = 0.02 * range.doubleValue(NonSI.MILE);

		tc = ((range.doubleValue(NonSI.MILE) + (ka + 20.0)) / cruiseSpeed.doubleValue(NonSI.MILES_PER_HOUR)) -
				climbDescentTime.doubleValue(NonSI.HOUR);

		return Amount.valueOf(tc, NonSI.HOUR);
	}

	public static Amount<Duration> calcCruiseTime(double range, double climbDescentTime,
			double cruiseSpeed){

		return calcCruiseTime(Amount.valueOf(range, NonSI.NAUTICAL_MILE) ,
				Amount.valueOf(climbDescentTime, NonSI.HOUR),
				Amount.valueOf(cruiseSpeed, NonSI.MACH));
	}

	/**
	 * Method that calculates the block time (hr) through the sum of the times below
	 * 												
	 * @author AC
	 * @param cruiseTime Cruise time in (hr)
	 * @param climbDescentTime Climb and descent time in (min) suggested value = 10 min
	 * @param sturtupTaxiTOTime Start up,taxi and take off time	in (min) suggested value = 15 min
	 * @param holdPriorToLandTime Hold prior to land time in (min) suggested value = 5 min
	 * @param landingTaxiToStopTime Landing and taxi to stop time in (min) suggested value = 5 min
	 * @return _blockTime in (hr)
	 */
	public static Amount<Duration> calcBlockTime(Amount<Duration> cruiseTime,
			Amount<Duration> climbDescentTime,
			Amount<Duration> sturtupTaxiTOTime,
			Amount<Duration> holdPriorToLandTime,
			Amount<Duration> landingTaxiToStopTime){

		return cruiseTime.
				plus(climbDescentTime).
				plus(sturtupTaxiTOTime).
				plus(holdPriorToLandTime).
				plus(landingTaxiToStopTime);
	}

	public static Amount<Duration> calcBlockTime(double cruiseTime,
			double climbDescentTime,
			double sturtupTaxiTOTime,
			double holdPriorToLandTime,
			double landingTaxiToStopTime){

		return calcBlockTime(Amount.valueOf(cruiseTime, NonSI.HOUR),
				Amount.valueOf(climbDescentTime, NonSI.MINUTE),
				Amount.valueOf(sturtupTaxiTOTime, NonSI.MINUTE),
				Amount.valueOf(holdPriorToLandTime, NonSI.MINUTE),
				Amount.valueOf(landingTaxiToStopTime, NonSI.MINUTE));
	}
	
	private static double calculateCeilingInterp(
			double RCMax, double x1,
			double y1, double k
			) {
		return (RCMax - y1 + k*x1)/k;
	}

}
