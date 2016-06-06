package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import calculators.aerodynamics.DragCalc;
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
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;

/**
 * Handle aircraft performance evaluation and provides
 * access to the results
 * 
 * @author Lorenzo Attanasio
 *
 */
public class PerformanceCalcManager {

	private RangeCalc rangeManager;

	private List<DragMap> dragList = new ArrayList<DragMap>();
	private List<ThrustMap> thrustList = new ArrayList<ThrustMap>();
	private List<DragThrustIntersectionMap> intersectionList = new ArrayList<DragThrustIntersectionMap>();
	private List<RCMap> rcList = new ArrayList<RCMap>();
	private List<CeilingMap> ceilingList = new ArrayList<CeilingMap>();
	private List<FlightEnvelopeMap> envelopeList = new ArrayList<FlightEnvelopeMap>();

	private double altitudeMin, altitudeMean, altitudeCruise, altitudeMax, 
	speedMin, speedMean, speedCruise, speedMax,
	phiMin, phiMean, phiMax,
	altitudeMinShort, altitudeMeanShort, altitudeMaxShort, 
	speedMinShort, speedMeanShort, speedMaxShort,
	phiMinShort, phiMeanShort, phiMaxShort,
	weightMin, weightMean, weightPercentMax, weightMax;

	private final int nWeights = 4, nAltitudes = 50, nSpeeds = 100, nPhi = 4; 

	private MyArray altitude = new MyArray(SI.METER),
			altitudeShort = new MyArray(SI.METER),
			altitudeEnvelope = new MyArray(SI.METER),
			speed = new MyArray(SI.METERS_PER_SECOND),
			speedShort = new MyArray(SI.METERS_PER_SECOND),
			mach = new MyArray(),
			weight = new MyArray(SI.NEWTON),
			weightShort = new MyArray(SI.NEWTON), 
			bPR = new MyArray(), 
			phi = new MyArray(),
			phiShort = new MyArray();

	// 1=take-off, 2=climb, 3=cruise, 4=descent
	private final EngineOperatingConditionEnum[] flightCondition = {
			EngineOperatingConditionEnum.TAKE_OFF,
			EngineOperatingConditionEnum.CLIMB,
			EngineOperatingConditionEnum.CRUISE,
			EngineOperatingConditionEnum.DESCENT};

	private EngineTypeEnum engineType;

	private final int nFlightCond = flightCondition.length;

	private double t0, bpr, surface, ar,
	sweepHalfChord, tcMax, cLmax, cD0, oswald;

	private AirfoilTypeEnum airfoilType;
	private int nEngine;
	private double weightWeightMaxRatio = 0.87;

	/**
	 * 
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param speed
	 * @param t0
	 * @param nEngine
	 * @param engineType TODO
	 * @param bpr
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cD0
	 * @param oswald
	 * @param flightCondition
	 */
	public PerformanceCalcManager(
			double[] altitude, double[] phi, double[] weight, double[] speed,
			double t0, int nEngine, EngineTypeEnum engineType,
			double bpr, double surface,
			double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double cLmax, double cD0, double oswald) {

		initializeAircraftData(t0, nEngine, engineType, 
				bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, cLmax, cD0, oswald);
		initializeArrays(altitude, phi, weight, speed);
		initializeCalculators();
	}

	/**
	 * Use default altitudes, speed and phi values
	 * @param t0
	 * @param nEngine
	 * @param engineType TODO
	 * @param bpr
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cd0
	 * @param oswald
	 * @param weight
	 * @param flightCondition
	 */
	public PerformanceCalcManager(
			double weightMin, double weightMax,
			double t0, int nEngine, EngineTypeEnum engineType,
			double bpr, double surface,
			double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cd0, double oswald) {

		initializeAircraftData(weightMin, weightMax, t0, nEngine, engineType, 
				bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, cLmax, cd0, oswald);
		initializeDefaultArrays();
		initializeCalculators();
	}

	public void initializeCalculators() {
		rangeManager = new RangeCalc(
				cD0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType,
				t0, nEngine, engineType, bpr);
		rangeManager.setArrays(speed.toArray(), altitudeShort.toArray(), weight.toArray(), phiShort.toArray());
		//TODO remove hard coded value
		rangeManager.setCurrentConditions(weightMax, weightMin, speedCruise, 0.4, altitudeCruise, phiMax, EngineOperatingConditionEnum.CRUISE);
	}

	public void initializeDefaultArrays() {
		setMinAndMaxValues(0., 11000., 50., 340., 0.7, 1.);

		//TODO remove hard-coded values
		speedCruise = 210.;
		altitudeCruise = 10000.;
		initializeArraysWithMinAndMaxValues();
	}

	public void initializeArraysWithMinAndMaxValues() {
		weight.linspace(weightMin, weightMax, nWeights);
		weightShort.setDouble(new double[]{weightMin, weightMean, weightPercentMax, weightMax});
		altitude.linspace(altitudeMin, altitudeMax, nAltitudes);
		altitudeEnvelope.setDouble(MyArrayUtils.concat(MyArrayUtils.linspace(altitudeMin, altitudeMax, nAltitudes), 
				MyArrayUtils.linspace(altitudeMax, altitudeMin, nAltitudes)));
		speed.linspace(speedMin, speedMax, nSpeeds);
		mach.setDouble(speed.divide(AtmosphereCalc.a0.getEstimatedValue()));
		phi.linspace(phiMin, phiMax, nPhi);
		initializeShortArrays();
	}

	public void initializeShortArrays() {
		altitudeShort.setDouble(new double[]{altitudeMinShort, altitudeMeanShort, altitudeMaxShort});
		speedShort.setDouble(new double[]{speedMinShort, speedMeanShort, speedMaxShort});
		phiShort.setDouble(new double[]{phiMinShort, phiMeanShort, phiMaxShort});		
	}

	/**
	 * Initialize the current object arrays together with the ones
	 * in the sub-calculators
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param speed
	 */
	public void initializeArrays(double[] altitude, double[] phi, double[] weight, double[] speed) {
		this.altitude.setDouble(altitude);
		this.phi.setDouble(phi);
		this.weight.setDouble(weight);
		this.speed.setDouble(speed);
	}

	/**
	 * Set data required to carry out computations
	 * 
	 * @param t0 static thrust, single engine (N)
	 * @param nEngine number of engines
	 * @param engineType
	 * @param bpr by pass ratio
	 * @param surface wing reference surface
	 * @param ar wing aspect ratio
	 * @param sweepHalfChord wing sweep at half chord (rad)
	 * @param tcMax wing maximum thickness ratio
	 * @param airfoilType
	 * @param cLmax wing maximum lift coefficient 
	 * @param cD0 aircraft CD0
	 * @param oswald Oswald factor
	 */
	public void initializeAircraftData(
			double t0, int nEngine, EngineTypeEnum engineType,
			double bpr, double surface,
			double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cD0, double oswald) {
		this.t0 = t0;
		this.nEngine = nEngine;
		this.bpr = bpr;
		this.surface = surface;
		this.ar = ar;
		this.sweepHalfChord = sweepHalfChord;
		this.tcMax = tcMax;
		this.cLmax = cLmax; 
		this.cD0 = cD0;
		this.oswald = oswald;
		this.airfoilType = airfoilType;
		this.engineType = engineType;
	}

	public void initializeAircraftData(
			double weightMin, double weightMax,
			double t0, int nEngine, EngineTypeEnum engineType,
			double bpr, double surface,
			double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double cLmax, double cD0, double oswald) {
		this.weightMin = weightMin;
		this.weightMax = weightMax;
		weightMean = (weightMin + weightMax)/2.;
		weightPercentMax = weightWeightMaxRatio * weightMax;
		initializeAircraftData(t0, nEngine, engineType, 
				bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, cLmax, cD0, oswald);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitudeMin (m)
	 * @param altitudeMax (m)
	 * @param speedMin (m/s)
	 * @param speedMax (m/s)
	 * @param phiMin
	 * @param phiMax
	 */
	public void setMinAndMaxValues(
			double altitudeMin, double altitudeMax, 
			double speedMin, double speedMax,
			double phiMin, double phiMax) {
		this.altitudeMin = altitudeMin;
		this.altitudeMax = altitudeMax;
		altitudeMean = (altitudeMin + altitudeMax)/2.;
		this.speedMin = speedMin;
		this.speedMax = speedMax;
		speedMean = (speedMin + speedMax)/2.;
		this.phiMin = phiMin;
		this.phiMax = phiMax;
		phiMean = (phiMin + phiMax)/2.;
	}

	public void setMinAndMaxValuesShortArrays(
			double altitudeMin, double altitudeMax, 
			double speedMin, double speedMax,
			double phiMin, double phiMax) {
		this.altitudeMinShort = altitudeMin;
		this.altitudeMaxShort = altitudeMax;
		altitudeMeanShort = (altitudeMin + altitudeMax)/2.;
		this.speedMinShort = speedMin;
		this.speedMaxShort = speedMax;
		speedMeanShort = (speedMin + speedMax)/2.;
		this.phiMinShort = phiMin;
		this.phiMaxShort = phiMax;
		phiMeanShort = (phiMin + phiMax)/2.;
	}


	/**
	 * Evaluate the aircraft performances at several altitudes, engine settings,
	 * weights and speeds. This method can be called without providing an aircraft
	 * object.
	 * 
	 * @author Lorenzo Attanasio
	 * @param altitude the altitudes at which you want to evaluate the performance (m)
	 * @param phi engine throttle values
	 * @param weight aircraft weights at which you want to evaluate the performance (N)
	 * @param speed (m/s)
	 * @param flightCondition take-off, climb, cruise or descent 
	 * @param t0 static thrust, sea level (N)
	 * @param bpr by pass ratio
	 * @param nEngine number of engines
	 * @param surface wing reference surface (m2)
	 * @param ar aspect ratio
	 * @param sweepHalfChord sweep angle of the equivalent wing at c/2
	 * @param tcMax maximum wing thickness ratio
	 * @param airfoilType conventional or supercritical
	 * @param cLmax whole aircraft maximum lift coefficient
	 * @param cD0 lift independent drag
	 * @param oswald Oswald factor
	 */
	public void calculateRCandFlightEnvelope(
			double[] altitude, double[] phi, double[] weight, double[] speed,
			EngineOperatingConditionEnum[] flightCondition,
			EngineTypeEnum engineType,
			double t0, double bpr, int nEngine,
			double surface, double ar,
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, double cLmax, 
			double cD0, double oswald) {

		dragList.addAll(DragCalc.calculateDragAndPowerRequired(
				altitude, phi, weight, speed, surface, 
				cLmax, cD0, ar, oswald, sweepHalfChord, 
				tcMax, airfoilType));

		thrustList.addAll(ThrustCalc.calculateThrustAndPowerAvailable(
				altitude, phi, weight, speed, flightCondition, engineType, 
				t0, nEngine, bpr, surface, ar, oswald, sweepHalfChord, tcMax, 
				airfoilType, cLmax, cD0));

		intersectionList.addAll(PerformanceCalcUtils.calculateDragThrustIntersection(
				altitude, speed, weight, phi, flightCondition, bpr, 
				surface, cLmax, dragList, thrustList));

		envelopeList.addAll(PerformanceCalcUtils.calculateEnvelope(
				altitude, weight, phi, bpr, surface, cLmax, flightCondition, intersectionList));

		rcList.addAll(RateOfClimbCalc.calculateRC(
				altitude, phi, weight, flightCondition, 
				t0, nEngine, bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				cLmax, cD0, oswald, dragList, thrustList));

		ceilingList.addAll(PerformanceCalcUtils.calculateCeiling(
				altitude, phi, weight, flightCondition, bpr, rcList));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param speed
	 */
	public void calculateAllPerformance(
			double[] altitude, double[] weight, double[] phi, EngineOperatingConditionEnum[] flightCondition,
			double[] speed) {
		calculateRCandFlightEnvelope(
				altitude, phi, weight, speed,
				flightCondition,
				engineType,
				t0, bpr, nEngine,
				surface, ar,
				sweepHalfChord, tcMax, airfoilType, cLmax, 
				cD0, oswald);

		rangeManager.setArrays(speed, altitudeShort.toArray(), weight, phi);
		rangeManager.calculateAll();

		System.out.println("---------- Performance evaluation terminated -------------");
	}

	/**
	 * Evaluate performance using default arrays
	 */
	public void calculateAllPerformance() {
		calculateAllPerformance(
				altitude.toArray(), weight.toArray(), phi.toArray(),
				flightCondition, speed.toArray());
	}

	/**
	 * Evaluate the aircraft performance at given altitude,
	 * power setting and weight at several speeds
	 * 
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param phi
	 * @param weight
	 * @param speed
	 * @param flightCondition
	 * @param t0
	 * @param bpr
	 * @param nEngine
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param cLmax
	 * @param cD0
	 * @param oswald
	 */
	public void calculateRCandFlightEnvelope(
			double altitude, double phi, double weight, double[] speed,
			EngineOperatingConditionEnum flightCondition, EngineTypeEnum engineType,
			double t0, double bpr, int nEngine,
			double surface, double ar,
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, double cLmax, 
			double cD0, double oswald) {

		dragList.add(DragCalc.calculateDragAndPowerRequired(
				altitude, weight, speed, surface, cLmax, 
				cD0, ar, oswald, sweepHalfChord, tcMax, 
				airfoilType));

		thrustList.add(ThrustCalc.calculateThrustAndPowerAvailable(
				altitude, phi, speed, flightCondition, engineType, t0, 
				nEngine, bpr));

		intersectionList.add(PerformanceCalcUtils.calculateDragThrustIntersection(
				altitude, speed, weight, phi, flightCondition, bpr, 
				surface, cLmax, dragList, thrustList));

		rcList.add(RateOfClimbCalc.calculateRC(
				altitude, phi, weight, flightCondition, engineType,
				t0, nEngine, bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				cLmax, cD0, oswald, dragList, thrustList));

		envelopeList.add(PerformanceCalcUtils.calculateEnvelope(
				altitude, weight, phi, bpr, surface, cLmax, flightCondition, intersectionList));
	}

	/**
	 * Evaluate performance with the aircraft data 
	 * stored in the current object
	 * 
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param flightCondition
	 * @param speed
	 */
	public void calculateAllPerformance(
			double altitude, double weight, double phi, EngineOperatingConditionEnum flightCondition,
			double[] speed) {
		calculateRCandFlightEnvelope(
				altitude, phi, weight, speed,
				flightCondition,
				engineType,
				t0, bpr, nEngine,
				surface, ar,
				sweepHalfChord, tcMax, airfoilType, cLmax, 
				cD0, oswald);
	}

	public double[] getThrust(double altitude, double phi, 
			double bpr, EngineOperatingConditionEnum flightCondition) {
		double[] thrust;

		thrust = PerformanceDataManager.getThrust(altitude, phi, bpr, flightCondition, thrustList);

		if (thrust == null) {
			thrustList.add(ThrustCalc.calculateThrustAndPowerAvailable(
					altitude, phi, speed.toArray(), 
					flightCondition, engineType, t0, nEngine, bpr));
			thrust = PerformanceDataManager.getThrust(altitude, phi, bpr, flightCondition, thrustList);
		}

		return thrust;
	}

	public double[] getThrust(double altitude, double phi, EngineOperatingConditionEnum condition) {
		return getThrust(altitude, phi, bpr, condition);
	}


	public double[][] getThrust(double[] altitude, double phi, EngineOperatingConditionEnum condition) {

		double[] thrustArray = null;
		double[][] thrust = new double[altitude.length][nSpeeds];

		for (int i=0; i<altitude.length; i++) {
			thrustArray = getThrust(altitude[i], phi, condition);
			for (int j=0; j<nSpeeds; j++) {
				thrust[i][j] = thrustArray[j];
			}
		}

		return thrust;
	}

	public double[][] getThrust(double altitude, double[] phi, EngineOperatingConditionEnum condition) {

		double[] thrustArray = null;
		double[][] thrust = new double[phi.length][nSpeeds];

		for (int i=0; i<phi.length; i++) {
			thrustArray = getThrust(altitude, phi[i], condition);
			for (int j=0; j<nSpeeds; j++) {
				thrust[i][j] = thrustArray[j];
			}
		}

		return thrust;
	}

	public double[][] getThrustvsAltitude(double phi, EngineOperatingConditionEnum condition) {
		return getThrust(altitudeShort.toArray(), phi, condition);
	}

	public double[] getDrag(double altitude, double weight) {
		double[] drag;

		drag = PerformanceDataManager.getDrag(altitude, weight, dragList);

		if (drag == null) {
			dragList.add(DragCalc.calculateDragAndPowerRequired(
					altitude, weight, speed.toArray(), surface, cLmax, 
					cD0, ar, oswald, sweepHalfChord, tcMax, 
					airfoilType));
			drag = PerformanceDataManager.getDrag(altitude, weight, dragList);
		}

		return drag;
	}

	public double[][] getDrag(double[] altitude, double weight) {

		double[] dragArray = null;
		double[][] drag = new double[altitude.length][nSpeeds];

		for (int i=0; i<altitude.length; i++) {
			dragArray = getDrag(altitude[i], weight);
			for (int j=0; j<nSpeeds; j++) {
				drag[i][j] = dragArray[j];
			}
		}

		return drag;
	}

	public double[][] getDrag(double altitude, double[] weight) {

		double[] dragArray = null;
		double[][] drag = new double[weight.length][nSpeeds];

		for (int i=0; i<weight.length; i++) {
			dragArray = getDrag(altitude, weight[i]);
			for (int j=0; j<nSpeeds; j++) {
				drag[i][j] = dragArray[j];
			}
		}

		return drag;
	}

	public double[][] getDragMinWeight(){
		return getDrag(altitudeShort.toArray(), weightMin);
	}

	public double[][] getDragMaxWeight(){
		return getDrag(altitudeShort.toArray(), weightMax);
	}


	/**
	 * Get the rate of climb vs speed at fixed altitude, weight, phi and bpr.
	 * The method reads it from the corresponding list if
	 * the value required has already been evaluated, otherwise
	 * it performs the required computation.
	 * 
	 * @param altitude (m)
	 * @param weight (N)
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return rate of climb (m/s)
	 */
	public double[] getRC(double altitude, double weight, double phi, 
			double bpr, EngineOperatingConditionEnum flightCondition) {
		double[] rc;

		rc = PerformanceDataManager.getDrag(altitude, weight, phi, flightCondition, bpr, rcList);

		if (rc == null) {
			calculateAllPerformance(altitude, weight, phi, flightCondition, speed.toArray());
			rc = PerformanceDataManager.getDrag(altitude, weight, phi, flightCondition, bpr, rcList);
		}

		return rc;
	}

	public double[] getRC(double altitude, double weight, double phi) {
		return getRC(altitude, weight, phi, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	public double[][] getRC(double[] altitude, double weight, double phi) {

		double[] rcArray = null;
		double[][] rc = new double[altitude.length][nSpeeds];

		for (int i=0; i<altitude.length; i++) {
			rcArray = getRC(altitude[i], weight, phi);
			for (int j=0; j<nSpeeds; j++) {
				rc[i][j] = rcArray[j];
			}
		}

		return rc;
	}

	public double[][] getRC(double altitude, double[] weight, double phi) {

		double[] rcArray = null;
		double[][] rc = new double[weight.length][nSpeeds];

		for (int i=0; i<weight.length; i++) {
			rcArray = getRC(altitude, weight[i], phi);
			for (int j=0; j<nSpeeds; j++) {
				rc[i][j] = rcArray[j];
			}
		}

		return rc;
	}


	public double[][] getRCvsAltitude(double weight, double phi) {
		return getRC(altitudeShort.toArray(), weight, phi);
	}

	public double[][] getRCvsAltitudeMinWeight(double phi) {
		return getRC(altitudeShort.toArray(), weightMin, phi);
	}

	public double[][] getRCvsAltitudeMaxWeight(double phi) {
		return getRC(altitudeShort.toArray(), weightMax, phi);
	}

	public double[][] getRCvsWeight(double altitude, double phi) {
		return getRC(altitude, weight.toArray(), phi);
	}

	public double[][] getRCvsWeightMinAltitude(double phi) {
		return getRC(altitudeMin, weight.toArray(), phi);
	}

	public double[][] getRCvsWeightMaxAltitude(double phi) {
		return getRC(altitudeMax, weight.toArray(), phi);
	}

	/**
	 * Get the maximum rate of climb.
	 * The method reads it from the corresponding list if
	 * the value required has already been evaluated, otherwise
	 * it performs the required computation.
	 * 
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return maximum rate of climb (m/s) at fixed altitude
	 */
	public double getRCmax(double altitude, double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {
		Double rcMax = 0.;

		rcMax = PerformanceDataManager.getRCmax(altitude, weight, phi, flightCondition, bpr, rcList);

		if (rcMax == null) {
			calculateAllPerformance(altitude, weight, phi, flightCondition, speed.toArray());
			rcMax = PerformanceDataManager.getRCmax(altitude, weight, phi, flightCondition, bpr, rcList);
		}

		return rcMax;
	}

	/**
	 * Get the maximum rate of climb at several altitudes
	 * 
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return maximum rate of climb (m/s) vs altitude
	 */
	public double[] getRCmax(double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		double[] rcMax = new double[altitude.size()];
		for (int i=0; i < altitude.size(); i++) {
			rcMax[i] = getRCmax(altitude.get(i), weight, phi, bpr, flightCondition);
		}
		return rcMax;
	}

	/**
	 * 
	 * @param weight
	 * @return
	 */
	public double[] getRCmax(double weight) {
		return getRCmax(weight, 1.0, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	public double[] getRCmax(double weightPercentMaximum, double phi, EngineOperatingConditionEnum condition) {
		return getRCmax(weightPercentMaximum*weightMax, phi, bpr, condition);
	}

	public double[] getRCmaxMaxWeight(double phi) {
		return getRCmax(weightMax, phi, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	public double[] getRCmaxMinWeight(double phi) {
		return getRCmax(weightMin, phi, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	public double[] getRCmaxMaxWeight(double weightPercentMaximum, double phi) {
		return getRCmax(weightPercentMaximum*weightMax, phi, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	/**
	 * Get the ramp angle vs speed at fixed altitude, weight, phi and bpr.
	 * The method reads it from the corresponding list if
	 * the value required has already been evaluated, otherwise
	 * it performs the required computation.
	 * 
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return ramp angle (rad)
	 */
	public double[] getGamma(double altitude, double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {
		double[] gamma = null;

		gamma = PerformanceDataManager.getGamma(altitude, weight, phi, flightCondition, bpr, rcList);
		if (gamma == null) {
			calculateAllPerformance(altitude, weight, phi, flightCondition, speed.toArray());
			gamma = PerformanceDataManager.getGamma(altitude, weight, phi, flightCondition, bpr, rcList);
		}

		return gamma;
	}

	public double[] getGamma(double altitude, double weight, double phi) {
		return getGamma(altitude, weight, phi, bpr, EngineOperatingConditionEnum.CLIMB);
	}

	public double[][] getGamma(double[] altitude, double weight, double phi) {

		double[] gammaArray = null;
		double[][] gamma = new double[altitude.length][nSpeeds];

		for (int i=0; i<altitude.length; i++) {
			gammaArray = getGamma(altitude[i], weight, phi);
			for (int j=0; j<nSpeeds; j++) {
				gamma[i][j] = gammaArray[j];
			}
		}

		return gamma;
	}

	public double[][] getGamma(double altitude, double[] weight, double phi) {

		double[] gammaArray = null;
		double[][] gamma = new double[weight.length][nSpeeds];

		for (int i=0; i<weight.length; i++) {
			gammaArray = getGamma(altitude, weight[i], phi);
			for (int j=0; j<nSpeeds; j++) {
				gamma[i][j] = gammaArray[j];
			}
		}

		return gamma;
	}

	public double[][] getGammaRadVsAltitude(double weight, double phi) {
		return getGamma(altitudeShort.toArray(), weight, phi);
	}

	public double[][] getGammaDegVsAltitude(double weight, double phi) {
		return new Array2DRowRealMatrix(getGammaRadVsAltitude(weight, phi)).scalarMultiply(57.2957795).getData();
	}

	public double[][] getGammaRadVsAltitudeMinWeight(double phi) {
		return getGamma(altitudeShort.toArray(), weightMin, phi);
	}

	public double[][] getGammaDegVsAltitudeMinWeight(double phi) {
		return getGammaDegVsAltitude(weightMin, phi);
	}

	public double[][] getGammaRadVsAltitudeMaxWeight(double phi) {
		return getGamma(altitudeShort.toArray(), weightMax, phi);
	}

	public double[][] getGammaDegVsAltitudeMaxWeight(double phi) {
		return getGammaDegVsAltitude(weightMax, phi);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return minimum speed (m/s) of the flight envelope at fixed altitude
	 */
	public double getMinimumSpeed(double altitude, double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		Double minSpeed;

		minSpeed = PerformanceDataManager.getMinSpeed(altitude, weight, phi, flightCondition, bpr, intersectionList);
		if (minSpeed == null) {
			calculateAllPerformance(altitude, weight, phi, flightCondition, speed.toArray());
			minSpeed = PerformanceDataManager.getMinSpeed(altitude, weight, phi, flightCondition, bpr, intersectionList);
		}

		return minSpeed;
	}

	/**
	 * 
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return minimum speed (m/s) vs altitude (m)
	 */
	public double[] getMinimumSpeed(double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		double[] minSpeed = new double[altitude.size()];
		for (int i=0; i < altitude.size(); i++) {
			minSpeed[i] = getMinimumSpeed(altitude.get(i), weight, phi, bpr, flightCondition);
		}

		return minSpeed;
	}

	public double[] getMinimumSpeedMinWeight(double phi, EngineOperatingConditionEnum condition) {
		return getMinimumSpeed(weightMin, phi, bpr, condition);
	}

	public double[] getMinimumSpeedMaxWeight(double phi, EngineOperatingConditionEnum condition) {
		return getMinimumSpeed(weightMax, phi, bpr, condition);
	}

	public double[][] getMinimumSpeed(double weight, EngineOperatingConditionEnum condition) {

		double[][] matrix = new double[phiShort.size()][nAltitudes];

		for (int i=0; i<phiShort.size(); i++) {
			matrix[i] = getMinimumSpeed(weight, phiShort.get(i), bpr, condition);
		}

		return matrix;
	}

	public double[][] getMinimumSpeedMinWeight(EngineOperatingConditionEnum condition) {
		return getMinimumSpeed(weightMin, condition);
	}

	public double[][] getMinimumSpeedMaxWeight(EngineOperatingConditionEnum condition) {
		return getMinimumSpeed(weightMax, condition);
	}

	public double[][] getMinimumSpeedPercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return getMinimumSpeed(weightPercentMax*weightMax, condition);
	}

	public double getMinimumSpeedAbsolutePercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return MyArrayUtils.getMin(getMinimumSpeed(weightPercentMax*weightMax, condition));
	}

	public double getAltitudeAtMinimumSpeedAbsolutePercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return altitude.get(MyArrayUtils.getIndexOfMin(speed.minus(getMinimumSpeedAbsolutePercentMaxWeight(weightPercentMax, condition)).toArray()));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return
	 */
	public double getMaximumSpeed(double altitude, double weight, 
			double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		Double maxSpeed = PerformanceDataManager.getMaxSpeed(altitude, weight, phi, flightCondition, bpr, intersectionList);
		if(maxSpeed == null) {
			calculateAllPerformance(altitude, weight, phi, flightCondition, speed.toArray());
			maxSpeed = PerformanceDataManager.getMaxSpeed(altitude, weight, phi, flightCondition, bpr, intersectionList);
		}

		return maxSpeed;
	}

	public double[] getMaximumSpeed(double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		double[] maxSpeed = new double[altitude.size()];
		for (int i=0; i < altitude.size(); i++) {
			maxSpeed[i] = getMaximumSpeed(altitude.get(i), weight, phi, bpr, flightCondition);
		}

		return maxSpeed;
	}

	public double[] getMaximumSpeedMinWeight(double phi, EngineOperatingConditionEnum condition) {
		return getMaximumSpeed(weightMin, phi, bpr, condition);
	}

	public double[] getMaximumSpeedMaxWeight(double phi, EngineOperatingConditionEnum condition) {
		return getMaximumSpeed(weightMax, phi, bpr, condition);
	}

	public double[][] getMaximumSpeed(double weight, EngineOperatingConditionEnum condition) {

		double[] array = null;
		double[][] matrix = new double[phiShort.size()][nAltitudes];

		for (int i=0; i<phiShort.size(); i++) {
			array = getMaximumSpeed(weight, phiShort.get(i), bpr, condition);
			for (int j=0; j<nAltitudes; j++) {
				matrix[i][j] = array[j];
			}
		}

		return matrix;
	}

	public double[][] getMaximumSpeedMinWeight(EngineOperatingConditionEnum condition) {
		return getMaximumSpeed(weightMin, condition);
	}

	public double[][] getMaximumSpeedMaxWeight(EngineOperatingConditionEnum condition) {
		return getMaximumSpeed(weightMax, condition);
	}

	public double[][] getMaximumSpeedPercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return getMaximumSpeed(weightPercentMax*weightMax, condition);
	}

	public double getMaximumSpeedAbsolutePercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return MyArrayUtils.getMax(getMaximumSpeed(weightPercentMax*weightMax, condition));
	}

	public double getAltitudeAtMaximumSpeedAbsolutePercentMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return altitude.get(MyArrayUtils.getIndexOfMin(speed.minus(getMaximumSpeedAbsolutePercentMaxWeight(weightPercentMax, condition)).toArray()));
	}

	/**
	 * 
	 * @param weight
	 * @param condition
	 * @return the minimum and maximum speed as separate arrays (one for each row)
	 */
	public double[][] getMinimumAndMaximumSpeed(double weight, EngineOperatingConditionEnum condition) {
		double[][] minimum = getMinimumSpeed(weight, condition);
		double[][] maximum = getMaximumSpeed(weight, condition);

		double[][] result = new double[minimum.length*2][minimum[0].length];

		for (int j=0; j<minimum.length; j++)
			for (int i=0; i<minimum[0].length; i++)
				result[j*2][i] = minimum[j][i];

		for (int j=0; j<maximum.length; j++)
			for (int i=0; i<maximum[0].length; i++)
				result[2*j+1][i] = maximum[j][i];

		return result;
	}

	/** 
	 * 
	 * @param weight
	 * @param condition
	 * @return the minimum and maximum speeds as a single array (minimum then maximum on the same row)
	 */
	public double[][] getMinimumAndMaximumSpeedAsSingleCurve(double weight, EngineOperatingConditionEnum condition) {
		double[][] minimum = getMinimumSpeed(weight, condition);
		double[][] maximum = getMaximumSpeed(weight, condition);

		double[][] result = new double[minimum.length][minimum[0].length*2];

		for (int j=0; j<minimum.length; j++)
			for (int i=0; i<minimum[0].length; i++) {
				result[j][i] = minimum[j][i];
				result[j][minimum[0].length + i] = maximum[j][maximum[0].length -1 -i];
			}

		return result;
	}

	public double[][] getMinimumAndMaximumSpeedMinWeight(EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeed(weightMin, condition);
	}

	public double[][] getMinimumAndMaximumSpeedMaxWeight(EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeed(weightMax, condition);
	}

	public double[][] getMinimumAndMaximumSpeedMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeed(weightPercentMax*weightMax, condition);
	}

	public double[][] getMinimumAndMaximumSpeedAsSingleCurveMinWeight(EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeedAsSingleCurve(weightMin, condition);
	}

	public double[][] getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeedAsSingleCurve(weightMax, condition);
	}

	public double[][] getMinimumAndMaximumSpeedAsSingleCurveMaxWeight(double weightPercentMax, EngineOperatingConditionEnum condition) {
		return getMinimumAndMaximumSpeedAsSingleCurve(weightPercentMax*weightMax, condition);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param phi
	 * @param bpr
	 * @param flightCondition
	 * @return absolute ceiling (m)
	 */
	public double getAbsoluteCeiling(double[] altitude, double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {

		Double absCeiling = PerformanceDataManager.getAbsoluteCeiling(phi, bpr, flightCondition, weight, ceilingList);

		if(absCeiling == null) {
			calculateAllPerformance(altitude, 
					new double[]{weight}, 
					new double[]{phi}, 
					new EngineOperatingConditionEnum[]{flightCondition}, 
					speed.toArray());

			absCeiling = PerformanceDataManager.getAbsoluteCeiling(phi, bpr, flightCondition, weight, ceilingList);
		}

		return absCeiling;
	}

	public double getAbsoluteCeiling(double weight, double phi, double bpr, EngineOperatingConditionEnum flightCondition) {
		return getAbsoluteCeiling(altitude.toArray(), weight, phi, bpr, flightCondition);
	}

	public double getAbsoluteCeiling(double weight, double phi, EngineOperatingConditionEnum condition) {
		return getAbsoluteCeiling(weight, phi, bpr, condition);
	}

	public double getAbsoluteCeilingMinWeight(EngineOperatingConditionEnum condition) {
		return getAbsoluteCeiling(weightMin, phiMax, condition);
	}

	public double getAbsoluteCeilingPercentMaxWeight(double weightWeightMaxPercent, double phi, EngineOperatingConditionEnum condition) {
		return getAbsoluteCeiling(weightWeightMaxPercent*weightMax, phi, condition);
	}

	public double getAbsoluteCeilingPercentMaxWeight(double weightWeightMaxPercent, EngineOperatingConditionEnum condition) {
		return getAbsoluteCeiling(weightWeightMaxPercent*weightMax, phiMax, condition);
	}

	public List<DragMap> getDragList() {
		return dragList;
	}

	public List<ThrustMap> getThrustList() {
		return thrustList;
	}

	public List<DragThrustIntersectionMap> getIntersectionList() {
		return intersectionList;
	}

	public List<RCMap> getRcList() {
		return rcList;
	}

	public List<CeilingMap> getCeilingList() {
		return ceilingList;
	}

	public List<FlightEnvelopeMap> getEnvelopeList() {
		return envelopeList;
	}

	public MyArray getAltitude() {
		return altitude;
	}

	public MyArray getSpeed() {
		return speed;
	}

	public MyArray getWeight() {
		return weight;
	}

	public MyArray getPhi() {
		return phi;
	}

	public EngineOperatingConditionEnum[] getFlightCondition() {
		return flightCondition;
	}

	public double getAltitudeMin() {
		return altitudeMin;
	}

	public void setAltitudeMin(double altitudeMin) {
		this.altitudeMin = altitudeMin;
	}

	public double getAltitudeMax() {
		return altitudeMax;
	}

	public void setAltitudeMax(double altitudeMax) {
		this.altitudeMax = altitudeMax;
	}

	public double getSpeedMin() {
		return speedMin;
	}

	public void setSpeedMin(double speedMin) {
		this.speedMin = speedMin;
	}

	public double getSpeedMax() {
		return speedMax;
	}

	public void setSpeedMax(double speedMax) {
		this.speedMax = speedMax;
	}

	public double getPhiMin() {
		return phiMin;
	}

	public void setPhiMin(double phiMin) {
		this.phiMin = phiMin;
	}

	public double getPhiMax() {
		return phiMax;
	}

	public void setPhiMax(double phiMax) {
		this.phiMax = phiMax;
	}

	public int getnFlightCond() {
		return nFlightCond;
	}

	public MyArray getMach() {
		return mach;
	}

	public MyArray getAltitudeShort() {
		return altitudeShort;
	}

	public MyArray getPhiShort() {
		return phiShort;
	}

	public MyArray getAltitudeEnvelope() {
		return altitudeEnvelope;
	}

	public double getWeightWeightMaxRatio() {
		return weightWeightMaxRatio;
	}

	public void setWeightWeightMaxRatio(double weightWeightMaxRatio) {
		this.weightWeightMaxRatio = weightWeightMaxRatio;
	}

	public RangeCalc getRangeManager() {
		return rangeManager;
	}

}


