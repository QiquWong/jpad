package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.inferred.freebuilder.shaded.org.apache.commons.lang3.ArrayUtils;
import org.jscience.physics.amount.Amount;

import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.FlightEnvelopeMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class PerformanceCalcUtils {

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public static List<DragThrustIntersectionMap> calculateDragThrustIntersection(
			List<Amount<Length>> altitude, Amount<Temperature> deltaTemperature, List<Amount<Velocity>> speed, List<Amount<Mass>> weight,
			List<Double> phi, List<EngineOperatingConditionEnum> flightCondition,
			Amount<Area>surface, double cLmax,
			List<DragMap> listDrag, List<ThrustMap> listThrust) {

		List<DragThrustIntersectionMap> list = new ArrayList<DragThrustIntersectionMap>();

		for(int f=0; f<flightCondition.size(); f++) {
			for (int j=0; j<phi.size(); j++) {
				for (int w=0; w<weight.size(); w++) {
					for (int i=0; i<altitude.size(); i++){

						list.add(calculateDragThrustIntersection(
								altitude.get(i), deltaTemperature, speed, weight.get(w), phi.get(j), flightCondition.get(f), 
								surface, cLmax, listDrag, listThrust));
					}
				}
			}
		}

		System.out.println("------ calculateIntersection terminated ------");
		return list;
	}

	public static DragThrustIntersectionMap calculateDragThrustIntersection(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, List<Amount<Velocity>> speed, Amount<Mass> weight,
			double phi, EngineOperatingConditionEnum flightCondition,
			Amount<Area> surface, double cLmax,
			List<DragMap> listDrag, List<ThrustMap> listThrust) {

		List<Amount<Force>> thrust = PerformanceDataManager.getThrust(altitude, deltaTemperature, phi, flightCondition, listThrust);
		List<Amount<Force>> drag = PerformanceDataManager.getDrag(altitude, deltaTemperature, weight, listDrag);

		Amount<Velocity> speedMin = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Velocity> speedMax = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Velocity> stallSpeed = SpeedCalc.calculateSpeedStall(altitude, deltaTemperature, weight, surface, cLmax);

		double[] intersection = new double[thrust.size()]; 
			intersection = MyArrayUtils.intersectArraysSimple(
					MyArrayUtils.convertListOfAmountTodoubleArray(thrust), 
					MyArrayUtils.convertListOfAmountTodoubleArray(drag)
					);
		
		List<Amount<Velocity>> intersectionSpeed = new ArrayList<>();
		for(int i=0; i<intersection.length; i++)
			if(intersection[i] != 0.0)
				intersectionSpeed.add(speed.get(i));
		
		int min = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertListOfAmountTodoubleArray(drag));
		Amount<Velocity> speedOfIndexMin = speed.get(min);
		
		if(!intersectionSpeed.isEmpty()) {
			if(intersectionSpeed.size() == 1) {
				if(intersectionSpeed.get(0).doubleValue(SI.METERS_PER_SECOND) < speedOfIndexMin.doubleValue(SI.METERS_PER_SECOND))
					speedMin = intersectionSpeed.get(0);
				else
					speedMax = intersectionSpeed.get(0);
			}
			else {
				for(int i=0; i<intersectionSpeed.size(); i++) {
					if(intersectionSpeed.get(i).doubleValue(SI.METERS_PER_SECOND) < speedOfIndexMin.doubleValue(SI.METERS_PER_SECOND))
						speedMin = intersectionSpeed.get(i);
					else
						speedMax = intersectionSpeed.get(i);
				}
			}
		}
		else {
			System.err.println("WARNING: (THRUST-DRAG INTERSECTION) NO INTERSECTION FOUND BETWEEN THRUST AND DRAG...");
		}
		
		if (speedMin != null && stallSpeed.doubleValue(SI.METERS_PER_SECOND) > speedMin.doubleValue(SI.METERS_PER_SECOND)) 
			speedMin = stallSpeed;
		else if (speedMin == null && speedMax != null) speedMin = stallSpeed;
		else if (speedMax == null && speedMin == null) { 
			speedMin = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
			speedMax = Amount.valueOf(0.001, SI.METERS_PER_SECOND);
		}

		if (speedMax == null) speedMax = Amount.valueOf(0.0, SI.METERS_PER_SECOND);

		return new DragThrustIntersectionMap(
				altitude,
				deltaTemperature,
				phi,
				weight,
				flightCondition,
				intersectionSpeed,
				speedMin,
				speedMax,
				surface,
				cLmax
				);
	}

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public static List<CeilingMap> calculateCeiling(
			List<Amount<Length>> altitude,
			Amount<Temperature> deltaTemperature,
			List<Double> phi, 
			List<Amount<Mass>> weight, 
			List<EngineOperatingConditionEnum> flightCondition,
			List<RCMap> listRC) {

		int nAlt = altitude.size();
		double slope;
		Amount<Length> absoluteCeiling = Amount.valueOf(0.0, SI.METER);
		Amount<Length> serviceCeiling = Amount.valueOf(0.0, SI.METER);
		List<Amount<Velocity>> rcMaxAtAltitude = new ArrayList<>();
		List<CeilingMap> ceilingList = new ArrayList<CeilingMap>();

		for(int f=0; f<flightCondition.size(); f++) {
			for (int p=0; p<phi.size(); p++) {
				for (int w=0; w<weight.size(); w++) { 
					for (int i=0; i<altitude.size(); i++) {
						rcMaxAtAltitude.add(
								PerformanceDataManager.getRCmax(
										altitude.get(i),
										deltaTemperature,
										phi.get(p),
										weight.get(w),
										flightCondition.get(f), 
										listRC
										)
								);
					}

					int M=0;
					for (int i=0; i<nAlt; i++){
						if (rcMaxAtAltitude.get(i).doubleValue(SI.METERS_PER_SECOND) > 0.) M=M+1;
					}

					try {
						slope = MyMathUtils.calculateSlopeLinear(
								rcMaxAtAltitude.get(M-1).doubleValue(SI.METERS_PER_SECOND),
								rcMaxAtAltitude.get(M-2).doubleValue(SI.METERS_PER_SECOND), 
								altitude.get(M-1).doubleValue(SI.METER),
								altitude.get(M-2).doubleValue(SI.METER)
								);
						absoluteCeiling = calculateCeilingInterp( 
								Amount.valueOf(0.0, SI.METERS_PER_SECOND),
								altitude.get(M-2), 
								rcMaxAtAltitude.get(M-2), 
								slope
								);
						serviceCeiling =  calculateCeilingInterp( 
								Amount.valueOf(0.5, SI.METERS_PER_SECOND),
								altitude.get(M-2), 
								rcMaxAtAltitude.get(M-2), 
								slope
								);

					} catch (ArrayIndexOutOfBoundsException e) { }

					ceilingList.add(new CeilingMap(absoluteCeiling, serviceCeiling, weight.get(w), phi.get(p), flightCondition.get(f)));

				}
			}
		}

		return ceilingList;
	}

	public static CeilingMap calculateCeiling(List<RCMap> listRC) {
		
		int nAlt = listRC.size();
		double[] altitude = new double[nAlt];
		double[] altitudeFitted = MyArrayUtils.linspace(
				listRC.get(0).getAltitude().doubleValue(SI.METER),
				listRC.get(listRC.size()-1).getAltitude().doubleValue(SI.METER),
				500
				);
		double[] rcMaxAtAltitude = new double[nAlt];
		double[] rcMaxAtAltitudeFitted = new double[altitudeFitted.length];

		for (int i=0; i < nAlt; i++) {
			rcMaxAtAltitude[i] = listRC.get(i).getRCMax().doubleValue(SI.METERS_PER_SECOND);
			altitude[i] = listRC.get(i).getAltitude().doubleValue(SI.METER);
		}

		for(int i=0; i< altitudeFitted.length; i++) {
			rcMaxAtAltitudeFitted[i] = MyMathUtils.getInterpolatedValue1DLinear(
					altitude,
					rcMaxAtAltitude,
					altitudeFitted[i]
					);
		}
		
		int M=0;
		for (int i=0; i < altitudeFitted.length; i++){
			if (rcMaxAtAltitudeFitted[i] > 0.) 
				M=M+1;
		}

		Amount<Length> absoluteCeiling = Amount.valueOf(0.0, SI.METER);
		Amount<Length> serviceCeiling = Amount.valueOf(0.0, SI.METER);
		
		if (M < rcMaxAtAltitudeFitted.length) {

			double[] rcMaxArrayReverse = ArrayUtils.subarray(rcMaxAtAltitudeFitted, 0, M-1);
			double[] altitudeArrayReverse = ArrayUtils.subarray(altitudeFitted, 0, M-1);
			ArrayUtils.reverse(rcMaxArrayReverse);
			ArrayUtils.reverse(altitudeArrayReverse);
			
			MyInterpolatingFunction rcInterpolatingFunction = new MyInterpolatingFunction();
			rcInterpolatingFunction.interpolate(rcMaxArrayReverse, altitudeArrayReverse);
			
			absoluteCeiling = Amount.valueOf(rcInterpolatingFunction.value(0.0), SI.METER);
			serviceCeiling = Amount.valueOf(rcInterpolatingFunction.value(0.5), SI.METER);

		}
		else {
			System.err.println("WARNING: (CEILING CALCULATION - CLIMB) RCmax > 0.0 AT CLIMB MAX ALTITUDE. CEILINGS CALCULATION FAILED...");
		}

		return new CeilingMap(absoluteCeiling, serviceCeiling, listRC.get(0).getWeight(), listRC.get(0).getPhi(), listRC.get(0).getFlightCondition());
		
	}
	
	public static CeilingMap calculateCeiling(List<RCMap> listRC, boolean isOEI) {
		
		int nAlt = listRC.size();
		double[] altitude = new double[nAlt];
		double[] altitudeFitted = MyArrayUtils.linspace(
				0.0,
				listRC.get(listRC.size()-1).getAltitude().doubleValue(SI.METER),
				200
				);
		double[] rcMaxAtAltitude = new double[nAlt];
		double[] rcMaxAtAltitudeFitted = new double[altitudeFitted.length];

		for (int i=0; i < nAlt; i++) {
			rcMaxAtAltitude[i] = listRC.get(i).getRCMax().doubleValue(SI.METERS_PER_SECOND);
			altitude[i] = listRC.get(i).getAltitude().doubleValue(SI.METER);
		}

		for(int i=0; i< altitudeFitted.length; i++) {
			rcMaxAtAltitudeFitted[i] = MyMathUtils.getInterpolatedValue1DLinear(
					altitude,
					rcMaxAtAltitude,
					altitudeFitted[i]
					);
		}
		
		int M=0;
		for (int i=0; i < altitudeFitted.length; i++){
			if (isOEI == false) {
				if (rcMaxAtAltitudeFitted[i] > 0.) 
					M=M+1;
			}
			else {
				if (rcMaxAtAltitudeFitted[i] > 0.5) 
					M=M+1;
			}
		}

		double k = MyMathUtils.calculateSlopeLinear( rcMaxAtAltitudeFitted[M-1], rcMaxAtAltitudeFitted[M-2], 
				altitudeFitted[M-1], altitudeFitted[M-2]);
		Amount<Length> absoluteCeiling = calculateCeilingInterp( 
				Amount.valueOf(0.0, SI.METERS_PER_SECOND), 
				Amount.valueOf(altitudeFitted[M-2], SI.METER), 
				Amount.valueOf(rcMaxAtAltitudeFitted[M-2], SI.METERS_PER_SECOND),
				k
				);
		Amount<Length> serviceCeiling = calculateCeilingInterp(
				Amount.valueOf(0.5, SI.METERS_PER_SECOND), 
				Amount.valueOf(altitudeFitted[M-2], SI.METER), 
				Amount.valueOf(rcMaxAtAltitudeFitted[M-2], SI.METERS_PER_SECOND),
				k
				);

		return new CeilingMap(absoluteCeiling, serviceCeiling, listRC.get(0).getWeight(), listRC.get(0).getPhi(), listRC.get(0).getFlightCondition());
		
	}
	
	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
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
			List<Amount<Length>> altitude, Amount<Temperature> deltaTemperature, List<Amount<Mass>> weight,
			List<Double> phi, 
			Amount<Area> surface, double cLmax,
			List<EngineOperatingConditionEnum> flightCondition,
			List<DragThrustIntersectionMap> listDragThrust
			){

		List<Amount<Velocity>> maxSpeed = new ArrayList<>();
		List<Amount<Velocity>> minSpeed = new ArrayList<>();
		List<Double> maxMach = new ArrayList<>();
		List<Double> minMach = new ArrayList<>();
		List<FlightEnvelopeMap> list = new ArrayList<FlightEnvelopeMap>();

		for(int f=0; f<flightCondition.size(); f++) {
			for (int p=0; p<phi.size(); p++) {
				for (int w=0; w<weight.size(); w++) {
					for (int i=0; i<altitude.size(); i++) {

						maxSpeed.add(PerformanceDataManager.getMaxSpeed(altitude.get(i), deltaTemperature, weight.get(w), phi.get(p), flightCondition.get(f), listDragThrust));
						minSpeed.add(PerformanceDataManager.getMinSpeed(altitude.get(i), deltaTemperature, weight.get(w), phi.get(p), flightCondition.get(f), listDragThrust));
						maxMach.add(PerformanceDataManager.getMaxMach(altitude.get(i), deltaTemperature, weight.get(w), phi.get(p), flightCondition.get(f), listDragThrust));
						minMach.add(PerformanceDataManager.getMinMach(altitude.get(i), deltaTemperature, weight.get(w), phi.get(p), flightCondition.get(f), listDragThrust));

						list.add(new FlightEnvelopeMap(altitude.get(i), deltaTemperature, phi.get(p), weight.get(w), flightCondition.get(f), maxSpeed.get(i), minSpeed.get(i)));
					}
				}
			}
		}

		return list;
	}

	/**
	 * @author Lorenzo Attanasio, Vittorio Trifari
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
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> weight,
			double phi,
			Amount<Area> surface, double cLmax,
			EngineOperatingConditionEnum flightCondition,
			List<DragThrustIntersectionMap> listDragThrust
			){

		Amount<Velocity> maxSpeed = PerformanceDataManager.getMaxSpeed(altitude, deltaTemperature, weight, phi, flightCondition, listDragThrust);
		Amount<Velocity> minSpeed = PerformanceDataManager.getMinSpeed(altitude, deltaTemperature, weight, phi, flightCondition, listDragThrust);

		return new FlightEnvelopeMap(altitude, deltaTemperature, phi, weight, flightCondition, maxSpeed, minSpeed);

	}
	
	public static Amount<Duration> calculateClimbTime (
			List<RCMap> rcList,
			Amount<Velocity> speed
			) {
		
		double[] rcInverseArray = new double[rcList.size()];
		double[] altitudeArray = new double[rcList.size()];
		
		for(int i=0; i<rcInverseArray.length; i++) {
			
			double sigma = AtmosphereCalc.getDensity(
					rcList.get(i).getAltitude().doubleValue(SI.METER),
					rcList.get(i).getDeltaTemperature().doubleValue(SI.CELSIUS)
					)/1.225; 
			
			rcInverseArray[i] = 1/(MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(rcList.get(i).getSpeedList()),
						MyArrayUtils.convertListOfAmountTodoubleArray(rcList.get(i).getRCList()),
						speed.divide(Math.sqrt(sigma)).doubleValue(SI.METERS_PER_SECOND)
						)
					);
			altitudeArray[i] = rcList.get(i).getAltitude().doubleValue(SI.METER);
		}
		
		double time = MyMathUtils.integrate1DTrapezoidLinear(
				altitudeArray,
				rcInverseArray,
				altitudeArray[0],
				altitudeArray[altitudeArray.length-1]
				);
		
		return Amount.valueOf(time, SI.SECOND);
		
	}
	
	public static Amount<Duration> calculateMinimumClimbTime (List<RCMap> rcList) {
			
		List<Double> rcMaxInverseArray = new ArrayList<Double>();
		List<Double> altitudeList = new ArrayList<Double>();
		
		for(int i=0; i<rcList.size(); i++) {
			if(rcList.get(i).getRCMax().doubleValue(SI.METERS_PER_SECOND) > 0.0) {
				rcMaxInverseArray.add(1/rcList.get(i).getRCMax().doubleValue(SI.METERS_PER_SECOND));
				altitudeList.add(rcList.get(i).getAltitude().doubleValue(SI.METER));
			}
		}
		
		double time = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertToDoublePrimitive(altitudeList),
				MyArrayUtils.convertToDoublePrimitive(rcMaxInverseArray)
				);
		
		Amount<Duration> minimumClimbTime = Amount.valueOf(time, SI.SECOND);
		
		return minimumClimbTime;
	}
	
	private static Amount<Length> calculateCeilingInterp(
			Amount<Velocity> rcMax, Amount<Length> altitude,
			Amount<Velocity> rcAtAltitude, double slope
			) {
		return Amount.valueOf(
				( rcMax.doubleValue(SI.METERS_PER_SECOND) 
						- rcAtAltitude.doubleValue(SI.METERS_PER_SECOND) 
						+ (slope*altitude.doubleValue(SI.METER))
						)/slope,
				SI.METER);
	}

}
