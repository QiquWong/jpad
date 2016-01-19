package calculators.performance;

import java.util.List;

import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.FlightEnvelopeMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.EngineOperatingConditionEnum;

/**
 * A collection of static methods to 
 * retrieve from each list the data 
 * stored in it 
 * 
 * @author Lorenzo Attanasio
 *
 */
public class PerformanceDataManager {

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param list
	 * @return
	 */
	public static double[] getDrag(double altitude, double weight, List<DragMap> list) {

		for (DragMap x : list) {
			if (x.getWeight() == weight 
					&& x.getAltitude() == altitude) return x.getDrag();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param weight
	 * @param altitude
	 * @param phi
	 * @param list
	 * @return
	 */
	public static double[] getSpeed(double weight, double altitude, double phi, List<DragMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragMap x = list.get(i);
			if (x.getWeight() == weight 
					&& x.getAltitude() == altitude 
					&& x.getPhi() == phi) return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param list
	 * @param phi
	 * @return
	 */
	public static double[] getPowerRequired(double altitude, double weight, List<DragMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragMap x = list.get(i);
			if (x.getWeight() == weight
					&& x.getAltitude() == altitude) return x.getPower();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param BPR
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static double[] getThrust( double altitude, double phi,
			double BPR, EngineOperatingConditionEnum flightCondition,
			List<ThrustMap> list) {

		for (int i=0; i < list.size(); i++) {
			ThrustMap x = list.get(i);
			if (x.getAltitude() == altitude
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getThrust();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param flightCondition
	 * @param BPR
	 * @param list
	 * @return
	 */
	public static double[] getSpeed(double altitude, double phi,
			EngineOperatingConditionEnum flightCondition, double BPR,
			List<ThrustMap> list) {

		for (int i=0; i < list.size(); i++) {			
			ThrustMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param flightCondition
	 * @param BPR
	 * @param list
	 * @return
	 */
	public static double[] getPowerAvailable( double altitude, double phi,
			EngineOperatingConditionEnum flightCondition, double BPR,
			List<ThrustMap> list) {

		for (int i=0; i < list.size(); i++) {
			ThrustMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getPower();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param phi
	 * @param BPR
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Double getAbsoluteCeiling(double phi, double BPR, 
			EngineOperatingConditionEnum fligthCondition, double weight,
			List<CeilingMap> list) {

		for (int i=0; i < list.size(); i++) {
			CeilingMap x = list.get(i);
			if (x.getFlightCondition().equals(fligthCondition)
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getAbsoluteCeiling();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param phi
	 * @param BPR
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Double getServiceCeiling(double phi, double BPR,
			EngineOperatingConditionEnum fligthCondition, double weight,
			List<CeilingMap> list) {

		for (int i=0; i < list.size(); i++) {			
			CeilingMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getServiceCeiling();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param bpr
	 * @param weight
	 * @param flightCondition
	 * @param list
	 * @return
	 */
//	public static double[] getIntersectionPoints(double altitude, double phi,
//			double bpr, double weight, FlightConditionEnum flightCondition,
//			List<DragThrustIntersectionMap> list) {
//
//		for (int i=0; i < list.size(); i++) {
//			DragThrustIntersectionMap x = list.get(i);
//			if (x.getAltitude() == altitude 
//					&& x.getFlightCondition().equals(flightCondition)
//					&& x.getBpr() == bpr
//					&& x.getPhi() == phi
//					&& x.getWeight() == weight) return x.getIntersectionPoints();
//		}
//
//		return null;
//	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param bpr
	 * @param weight
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static double[] getSpeed(double altitude, double phi,
			double bpr, double weight, EngineOperatingConditionEnum flightCondition,
			List<DragThrustIntersectionMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getPhi() == phi
					&& x.getWeight() == weight) return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param list
	 * @return
	 */
	public static Double getMaxSpeed(double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<DragThrustIntersectionMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getPhi() == phi
					&& x.getWeight() == weight) return x.getMaxSpeed();
		}

		return 0.;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param list
	 * @return
	 */
	public static Double getMinSpeed(double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<DragThrustIntersectionMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getPhi() == phi
					&& x.getWeight() == weight) return x.getMinSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param list
	 * @return
	 */
	public static Double getMaxMach(double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<DragThrustIntersectionMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getPhi() == phi
					&& x.getWeight() == weight) return x.getMaxMach();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param list
	 * @return
	 */
	public static Double getMinMach(double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<DragThrustIntersectionMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getPhi() == phi
					&& x.getWeight() == weight) return x.getMinMach();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param phi
	 * @param BPR
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Double getAltitude(double phi, double BPR, 
			EngineOperatingConditionEnum fligthCondition, double weight,
			List<FlightEnvelopeMap> list) {

		for (int i=0; i < list.size(); i++) {
			FlightEnvelopeMap x = list.get(i);
			if (x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight() == weight
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getAltitude();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param phi
	 * @param BPR
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Double getMaxSpeed(double phi, double BPR, 
			EngineOperatingConditionEnum fligthCondition, double weight,
			List<FlightEnvelopeMap> list) {

		for (int i=0; i < list.size(); i++) {			
			FlightEnvelopeMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight() == weight
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getMaxSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param phi
	 * @param BPR
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Double getMinSpeed(double phi, double BPR, 
			EngineOperatingConditionEnum fligthCondition, double weight,
			List<FlightEnvelopeMap> list) {

		for (int i=0; i < list.size(); i++) {			
			FlightEnvelopeMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight() == weight
					&& x.getBpr() == BPR
					&& x.getPhi() == phi) return x.getMinSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param rcList
	 * @return
	 */
	public static double[] getDrag( double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<RCMap> rcList) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getWeight() == weight
					&& x.getPhi() == phi) return x.getRC();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param BPR
	 * @param flightCondition
	 * @param weight
	 * @param rcList
	 * @return
	 */
	public static double[] getRCSpeed( double altitude, double phi,
			double BPR, EngineOperatingConditionEnum flightCondition, double weight,
			List<RCMap> rcList) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == BPR
					&& x.getWeight() == weight
					&& x.getPhi() == phi) return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param bpr
	 * @param rcList
	 * @return
	 */
	public static Double getRCmax(double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<RCMap> rcList) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getWeight() == weight
					&& x.getPhi() == phi) return x.getRCmax();
		}

		return null;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param BPR
	 * @param flightCondition
	 * @param weight
	 * @param rcList
	 * @return
	 */
	public static double[] getGamma( double altitude, double weight,
			double phi, EngineOperatingConditionEnum flightCondition, double bpr,
			List<RCMap> rcList) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getBpr() == bpr
					&& x.getWeight() == weight
					&& x.getPhi() == phi) return x.getGamma();
		}

		return null;
	}


}
