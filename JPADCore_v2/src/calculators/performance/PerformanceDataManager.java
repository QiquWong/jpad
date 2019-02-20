package calculators.performance;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

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
 * @author Vittorio Trifari
 *
 */
public class PerformanceDataManager {

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param list
	 * @return
	 */
	public static List<Amount<Force>> getDrag(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			Amount<Mass> weight, 
			List<DragMap> list
			) {

		for (DragMap x : list) {
			if (x.getWeight().equals(weight) 
					&& x.getAltitude().equals(altitude)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					) 
				return x.getDrag();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param weight
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param list
	 * @return
	 */
	public static List<Amount<Velocity>> getSpeed(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			double phi,
			Amount<Mass> weight, 
			List<DragMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragMap x = list.get(i);
			if (x.getWeight().equals(weight) 
					&& x.getAltitude().equals(altitude) 
					&& x.getPhi() == phi
					&& x.getDeltaTemperature().equals(deltaTemperature)
					) return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param list
	 * @param phi
	 * @return
	 */
	public static List<Amount<Power>> getPowerRequired(Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> weight, List<DragMap> list) {

		for (int i=0; i < list.size(); i++) {
			DragMap x = list.get(i);
			if (x.getWeight().equals(weight)
					&& x.getAltitude().equals(altitude)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					)
				return x.getPower();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static List<Amount<Force>> getThrust( 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			EngineOperatingConditionEnum flightCondition,
			List<ThrustMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			ThrustMap x = list.get(i);
			if (x.getAltitude() == altitude
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					) 
				return x.getThrust();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static List<Amount<Velocity>> getSpeed(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			List<ThrustMap> list
			) {

		for (int i=0; i < list.size(); i++) {			
			ThrustMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					)
				return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static List<Amount<Power>> getPowerAvailable( 
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			double phi,
			EngineOperatingConditionEnum flightCondition,
			List<ThrustMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			ThrustMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					) 
				return x.getPower();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param phi
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Amount<Length> getAbsoluteCeiling(
			double phi, 
			EngineOperatingConditionEnum fligthCondition, 
			Amount<Mass> weight,
			List<CeilingMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			CeilingMap x = list.get(i);
			if (x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getAbsoluteCeiling();
		}

		return Amount.valueOf(0.0, SI.METER);
	}

	/**
	 * @author Vittorio Trifari
	 * @param phi
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Amount<Length> getServiceCeiling(
			double phi,
			EngineOperatingConditionEnum fligthCondition, 
			Amount<Mass> weight,
			List<CeilingMap> list) {

		for (int i=0; i < list.size(); i++) {			
			CeilingMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					)
				return x.getServiceCeiling();
		}

		return Amount.valueOf(0.0, SI.METER);
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param weight
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static List<Amount<Velocity>> getSpeed(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature, 
			double phi,
			Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition,
			List<DragThrustIntersectionMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					&& x.getWeight().equals(weight)
					)
				return x.getSpeed();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static Amount<Velocity> getMaxSpeed(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			Amount<Mass> weight,
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			List<DragThrustIntersectionMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					&& x.getWeight().equals(weight)
					)
				return x.getMaxSpeed();
		}

		return Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static Amount<Velocity> getMinSpeed(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			Amount<Mass> weight,
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			List<DragThrustIntersectionMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude() == altitude 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					&& x.getWeight().equals(weight)
					)
				return x.getMinSpeed();
		}

		return Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static double getMaxMach(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			Amount<Mass> weight,
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			List<DragThrustIntersectionMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					&& x.getWeight().equals(weight)
					)
				return x.getMaxMach();
		}

		return 0.0;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param list
	 * @return
	 */
	public static double getMinMach(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			Amount<Mass> weight,
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			List<DragThrustIntersectionMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			DragThrustIntersectionMap x = list.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getPhi() == phi
					&& x.getWeight().equals(weight)
					)
				return x.getMinMach();
		}

		return 0.0;
	}

	/**
	 * @author Vittorio Trifari
	 * @param phi
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Amount<Length> getAltitude(
			double phi, 
			EngineOperatingConditionEnum fligthCondition, 
			Amount<Mass> weight,
			List<FlightEnvelopeMap> list
			) {

		for (int i=0; i < list.size(); i++) {
			FlightEnvelopeMap x = list.get(i);
			if (x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					)
				return x.getAltitude();
		}

		return Amount.valueOf(0.0, SI.METER);
	}

	/**
	 * @author Vittorio Trifari
	 * @param phi
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Amount<Velocity> getMaxSpeed(
			double phi, 
			EngineOperatingConditionEnum fligthCondition, 
			Amount<Mass> weight,
			List<FlightEnvelopeMap> list
			) {

		for (int i=0; i < list.size(); i++) {			
			FlightEnvelopeMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getMaxSpeed();
		}

		return Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	}

	/**
	 * @author Vittorio Trifari
	 * @param phi
	 * @param fligthCondition
	 * @param weight
	 * @param list
	 * @return
	 */
	public static Amount<Velocity> getMinSpeed(
			double phi, 
			EngineOperatingConditionEnum fligthCondition, 
			Amount<Mass> weight,
			List<FlightEnvelopeMap> list
			) {

		for (int i=0; i < list.size(); i++) {			
			FlightEnvelopeMap x = list.get(i);
			if ( x.getFlightCondition().equals(fligthCondition)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getMinSpeed();
		}

		return Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param rcList
	 * @return
	 */
	public static List<Amount<Velocity>> getRC( 
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			Amount<Mass> weight,
			double phi, 
			EngineOperatingConditionEnum flightCondition,
			List<RCMap> rcList
			) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature) 
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getRCList();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param weight
	 * @param rcList
	 * @return
	 */
	public static List<Amount<Velocity>> getRCSpeed( 
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			double phi,
			EngineOperatingConditionEnum flightCondition, 
			Amount<Mass> weight,
			List<RCMap> rcList
			) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature) 
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getSpeedList();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param rcList
	 * @return
	 */
	public static Amount<Velocity> getRCmax(
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature,
			double phi,
			Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition, 
			List<RCMap> rcList
			) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getRCMax();
		}

		return Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param weight
	 * @param rcList
	 * @return
	 */
	public static List<Amount<Angle>> getGamma(
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			double phi, 
			Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition,
			List<RCMap> rcList
			) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getClimbAngleList();
		}

		return null;
	}

	/**
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param deltaTemperature
	 * @param phi
	 * @param flightCondition
	 * @param weight
	 * @param rcList
	 * @return
	 */
	public static Amount<Angle> getCGR (
			Amount<Length> altitude, 
			Amount<Temperature> deltaTemperature,
			double phi, 
			Amount<Mass> weight,
			EngineOperatingConditionEnum flightCondition,
			List<RCMap> rcList
			) {

		for (int i=0; i < rcList.size(); i++) {
			RCMap x = rcList.get(i);
			if (x.getAltitude().equals(altitude) 
					&& x.getFlightCondition().equals(flightCondition)
					&& x.getDeltaTemperature().equals(deltaTemperature)
					&& x.getWeight().equals(weight)
					&& x.getPhi() == phi
					) 
				return x.getClimbAngle();
		}

		return Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
	}
	
}
