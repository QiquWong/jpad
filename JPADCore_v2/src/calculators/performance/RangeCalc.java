package calculators.performance;

import javax.measure.quantity.Velocity;
import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.PowerPlant;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import database.databasefunctions.engine.EngineDatabaseManager_old;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

public class RangeCalc {

	private double w0, wf, speed, cl, altitude, cd0, oswald, surface, ar, sweepHalfChord, tcMax, t0, phi, bpr,
	mach, sfcBreguet, thrustBreguet, rangeBreguetJet, rangeSpeedAndClConstant, rangeSpeedAndAltitudeConstant,
	rangeClAndAltitudeConstant;

	private double[] speedA, altitudeA, phiA, wfA;
	
	private PowerPlant thePowerPlant;

	private double[][] rangeSpeedAndClConstantM, rangeSpeedAndAltitudeConstantM;

	private int nEngine;
	private EngineTypeEnum engineType;
	private AirfoilTypeEnum airfoilType;
	private EngineOperatingConditionEnum flightCondition;

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param cd0
	 * @param oswald
	 * @param surface
	 * @param ar
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @param t0
	 * @param nEngine
	 * @param engineType
	 * @param bpr
	 */
	public RangeCalc(
			double cd0, double oswald, double surface, double ar, double sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType, double t0, int nEngine,
			EngineTypeEnum engineType, double bpr, PowerPlant thePowerPlant) {
		this.cd0 = cd0;
		this.oswald = oswald;
		this.surface = surface;
		this.ar = ar;
		this.sweepHalfChord = sweepHalfChord;
		this.tcMax = tcMax;
		this.t0 = t0;
		this.bpr = bpr;
		this.nEngine = nEngine;
		this.thePowerPlant = thePowerPlant;
		this.airfoilType = airfoilType;
		this.engineType = engineType;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param w0
	 * @param wf
	 * @param speed
	 * @param cl
	 * @param altitude
	 * @param phi
	 * @param flightCondition
	 */
	public void setCurrentConditions(
			double w0, double wf, 
			double speed, double cl, double altitude, 
			double phi, EngineOperatingConditionEnum flightCondition) {
		this.w0 = w0;
		this.wf = wf;
		this.speed = speed;
		this.cl = cl;
		this.altitude = altitude;
		this.phi = phi;
		this.flightCondition = flightCondition;	
	}

	public void setArrays(double[] speedA, double[] altitudeA, double[] wfA, double[] phiA) {
		this.speedA = speedA;
		this.altitudeA = altitudeA;
		this.phiA = phiA;
		this.wfA = wfA;
	}

	/**
	 * @author Lorenzo Attanasio
	 *
	 */
	public void calculateDependentVariables() {
		mach = SpeedCalc.calculateMach(altitude, speed);		
	}

	/**
	 * @author Lorenzo Attanasio
	 *
	 */
	public void calculateAll() {
		calculateDependentVariables();
		calculateAllBreguet();
		calculateAllAtGivenCondition();
		calculateAllSweep();
	}

	/**
	 * @author Lorenzo Attanasio
	 *
	 */
	public void calculateAllBreguet() {
		thrustBreguet = ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, altitude, mach);
		double tT0Ratio = thrustBreguet/(t0*nEngine);

		sfcBreguet = EngineDatabaseManager_old.getSFC(
				mach,
				altitude,
				tT0Ratio,
				bpr,
				engineType,
				EngineOperatingConditionEnum.CRUISE,
				thePowerPlant
				);
		rangeBreguetJet = calculateRangeBreguetSFC(sfcBreguet, altitude, surface, cl, w0, wf, cd0, ar, oswald, sweepHalfChord, tcMax, airfoilType);
	}

	/**
	 * @author Lorenzo Attanasio
	 *
	 */
	public void calculateAllSweep() {
		rangeSpeedAndClConstantM = calculateRangeAtConstantSpeedAndLiftCoefficient(
				w0, wfA, speedA, 
				cl, cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		rangeSpeedAndAltitudeConstantM = calculateRangeAtConstantSpeedAndAltitude(
				w0, wf, speedA, altitudeA, 
				cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
	}
	
	/**
	 * @author Lorenzo Attanasio
	 *
	 */
	public void calculateAllAtGivenCondition() {
		rangeSpeedAndClConstant = calculateRangeAtConstantSpeedAndLiftCoefficient(
				w0, wf, speed, cl, 
				cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, true);
		rangeSpeedAndAltitudeConstant = calculateRangeAtConstantSpeedAndAltitude(
				w0, wf, speed, altitude, 
				cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, true);
		rangeClAndAltitudeConstant = calculateRangeAtConstantLiftCoefficientAndAltitude(
				w0, wf, cl, altitude,
				cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, true);		
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param eta
	 * @param c Fuel consumption (N/(J/s)/s)
	 * @param cl
	 * @param cd
	 * @param w0 Initial weight (kg)
	 * @param wf Final weight (kg)
	 * @return range (m)
	 */
	public static double calculateRangeBreguetPropeller(double eta, double c, 
			double cl, double cd, double w0, double wf) {
		return (eta/c) * cl/cd * Math.log(w0/wf);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param eta propeller efficiency
	 * @param sfc Specific Fuel Consumption (lb/(hp*h))
	 * @param cl
	 * @param cd
	 * @param w0 Initial weight (kg)
	 * @param wf Final weight (kg)
	 * @return range (km)
	 */
	public static double calculateRangeBreguetPropellerSFC(double eta, double sfc, 
			double cl, double cd, double w0, double wf) {
		return 603.5*calculateRangeBreguetPropellerSFC(eta, sfc, cl, cd, w0, wf);
	}

	/**
	 * This method overload the method in RangeCalc with the same name allowing 
	 * the user to evaluate the range from breguet formula giving in input just the
	 * weight ratio. 
	 * 
	 * @author Vittorio Trifari
	 * @param eta Propeller Efficiency 
	 * @param sfc Specific Fuel Consumption (lb/(hp*h))
	 * @param cl Lift Coefficient
	 * @param cd Drag Coefficient
	 * @param weightRatio w0/wf 
	 * @return Range [km]
	 */
	public static double calculateRangeBreguetPropellerSFC(double eta, double sfc, 
			double cl, double cd, double weightRatio)
	{
		return 603.5*(eta/sfc) * cl/cd * Math.log(weightRatio);
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param ct
	 * @param density (kg/m3)
	 * @param surface
	 * @param cl
	 * @param cd
	 * @param w0
	 * @param wf
	 * @return range (m)
	 */
	public static double calculateRangeBreguetJet(double ct, double density, double surface, 
			double cl, double cd, double w0, double wf) {
		double range = (2./ct) * Math.sqrt(2./(density*surface)) * (Math.sqrt(cl)/cd) * (Math.sqrt(w0) - Math.sqrt(wf));
		JPADStaticWriteUtils.logToConsole("Range (Breguet) at CL= " + cl + " and wf/w0= " + wf/w0 + " is: " + range + " m");
		return range;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param sfcj Specific Fuel Consumption Jet (lb/(lb*h))
	 * @param density (kg/m3)
	 * @param surface
	 * @param cl
	 * @param cd
	 * @param w0 (kg)
	 * @param wf (kg)
	 * @return range (m)
	 */
	public static double calculateRangeBreguetJetSFCJ(
			double sfcj, double density, double surface, 
			double cl, double cd, double w0, double wf) {
		return calculateRangeBreguetJet(sfcj/3600., density, surface, cl, cd, w0, wf);
	}

	/**
	 * This method overload the method in RangeCalc with the same name allowing 
	 * the user to evaluate the range from breguet formula giving in input just the
	 * weight ratio. Using this method CL and V are supposed constant. 
	 * 
	 * @author Vittorio Trifari
	 * @param speed Aircraft speed 
	 * @param sfcj Specific Fuel Consumption Jet (lb/(lb*h))
	 * @param Lift Coefficient
	 * @param cd Drag Coefficient
	 * @param weightRatio w0/wf 
	 * @return Range [km]
	 */
	public static double calculateRangeBreguetJetSFCJ(Amount<Velocity> speed, double sfcj, 
			double cl, double cd, double weightRatio) {
		return (speed.getEstimatedValue()/sfcj) * (cl/cd) * Math.log(weightRatio);
	}
	
	public static double calculateRangeBreguetJetSFCJAtAltitude(
			double sfcj, double altitude, double surface, 
			double cl, double cd, double w0, double wf) {
		return calculateRangeBreguetJetSFCJ(sfcj, AtmosphereCalc.getDensity(altitude), surface, cl, cd, w0, wf);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param sfcj
	 * @param altitude
	 * @param surface
	 * @param cl
	 * @param w0
	 * @param wf
	 * @param cd0
	 * @param ar
	 * @param e
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double calculateRangeBreguetSFC(
			double sfcj, double altitude, double surface, 
			double cl, double w0, double wf,
			double cd0, double ar, double e, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {

		double density = AtmosphereCalc.getDensity(altitude);
		return calculateRangeBreguetJetSFCJ(sfcj, density, surface, 
				cl, DragCalc.calculateCDTotal(cd0, cl, ar, e, 
						SpeedCalc.calculateMach(altitude, Math.sqrt((w0+wf)/(density*surface*cl))), 
						sweepHalfChord, tcMax, airfoilType), 
						w0, wf);
	}

	/**
	 * 
	 * @param ct
	 * @param speed
	 * @param cl
	 * @param cd
	 * @param w
	 * @param dw
	 * @return delta range (m)
	 */
	public static double calculateDeltaRange(double ct, double speed, double cl, double cd, double w) {
		return (speed/ct) * (cl/(cd*w));
	}

	/**
	 * 
	 * @param sfc
	 * @param speed
	 * @param cl
	 * @param cd
	 * @param w (N)
	 * @return delta range (m)
	 */
	public static double calculateDeltaRangeJetSFC(double sfc, double speed, double cl, double cd, double w) {
		return calculateDeltaRange(sfc/3600., speed, cl, cd, w);
	}

	/**
	 * Calculate range assuming that the available thrust equals the required thrust
	 * 
	 * @author Lorenzo Attanasio
	 * @param ct Fuel Consumption (N/N/s)
	 * @param rho
	 * @param surface
	 * @param cl
	 * @param cd
	 * @param w
	 * @return delta range (m)
	 */
	public static double calculateDeltaRangeJet(double ct, double rho, double surface, double cl, double cd, double w) {
		return (1./ct) * (Math.sqrt(2*cl/(rho*surface)) / (cd * Math.sqrt(w)));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param speed (m/s)
	 * @param cl
	 * @param w weight (N)
	 * @param altitude (m)
	 * @param cd0
	 * @param e oswald factor
	 * @param surface wing surface (m2)
	 * @param ar
	 * @param sweepHalfChord wing sweep at half chord (radian)
	 * @param tcMax wing maximum t/c
	 * @param airfoilType
	 * @param t0 single engine sea level thrust (N)
	 * @param nEngine number of engines
	 * @param phi engine throttle setting
	 * @param bpr
	 * @param flightCondition
	 * @return delta range (m)
	 */
	public static double calculateDeltaRange(double speed, double cl, double w, 
			double altitude, double cd0, double e, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, EngineTypeEnum engineType, 
			EngineOperatingConditionEnum flightCondition, PowerPlant thePowerPlant) {

		double mach = SpeedCalc.calculateMach(altitude, speed);
		double thrust = ThrustCalc.calculateThrustDatabase(t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, altitude, mach);
		double cd = DragCalc.calculateCDTotal(cd0, cl, ar, e, mach, sweepHalfChord, tcMax, airfoilType);
		double drag = DragCalc.calculateDragAtSpeed(w, altitude, surface, speed, cd);
		double tT0Ratio = thrust/(t0*nEngine);
		
		if (thrust >= drag) 
			return calculateDeltaRangeJetSFC(EngineDatabaseManager_old.getSFC(mach, altitude, tT0Ratio, bpr, engineType, flightCondition, thePowerPlant), 
					speed, cl, cd, w);
		else 
			return 0.;
	}
	
	public static double calculateDeltaRangeGivenDensity(double speed, double cl, double w, 
			double density, double cd0, double e, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, EngineTypeEnum engineType,
			EngineOperatingConditionEnum flightCondition, PowerPlant thePowerPlant) {
		return calculateDeltaRange(speed, cl, w, AtmosphereCalc.getAltitude(density), 
				cd0, e, surface, ar, sweepHalfChord, tcMax, airfoilType, 
				t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
	}

	/**
	 * Evaluate range for a jet aircraft, supposing that the lift equals the weight.
	 * The method checks if the aircraft is above or below the ceiling; if it is above 
	 * the method sets the altitude at a percentage (<1) of the ceiling altitude
	 *  
	 * @author Lorenzo Attanasio
	 * @param w0 Initial weight (N)
	 * @param wf Final weight (N)
	 * @param speed (m/s)
	 * @param cl
	 * @param cd0
	 * @param oswald
	 * @param surface wing surface (m2)
	 * @param ar
	 * @param sweepHalfChord wing sweep at half chord (radian)
	 * @param tcMax wing maximum t/c
	 * @param airfoilType
	 * @param t0 single engine sea level thrust (N)
	 * @param nEngine
	 * @param phi engine throttle
	 * @param bpr
	 * @param flightCondition
	 * @param printResult TODO
	 * @param altitude (m)
	 * @return range (m)
	 */
	public static double calculateRangeAtConstantSpeedAndLiftCoefficient(
			double w0, double wf, double speed, double cl,
			double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double t0, int nEngine, double phi, double bpr, EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant, boolean printResult) {

		int nWeights = 100;
		double density = 0., ceiling = 0.;
		double[] weight = MyArrayUtils.linspace(wf, w0, nWeights);
		double[] deltaRange = new double[nWeights];
		
		for (int i=0; i<nWeights; i++) {

			// We suppose that the lift equals the weight for estimating current altitude
			density = 2.*weight[i]/(speed*speed*surface*cl);
			if (density < 0) density = 0.;
			if (density > 1.225) density = 1.225;
//			ceiling = MyPerformanceCalcUtils.calculateCeiling(speed, phi, weight[i], flightCondition, 
//					t0, nEngine, bpr, surface, ar, sweepHalfChord, tcMax, airfoilType, cd0, oswald);
//			if (altitude > ceiling) altitude = 0.95*ceiling;
			
			if (ThrustCalc.compareThrustAvailableToDrag(
					speed, weight[i], cl, AtmosphereCalc.getAltitude(density), 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant) <= 0.) {
				if (printResult)
					JPADStaticWriteUtils.logToConsole("The aircraft cannot fly at constant speed= " + speed + " m/s and constant CL= " + cl + " m");
				return 0.;
			}
			
			deltaRange[i] = calculateDeltaRange(speed, cl, weight[i], AtmosphereCalc.getAltitude(density), 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		}

		double range = MyMathUtils.integrate1DSimpsonSpline(weight, deltaRange, wf*1.0001, w0*0.9999);
		if (printResult)
			JPADStaticWriteUtils.logToConsole("Range at constant speed= " + speed + " m/s and constant CL= " + cl + " m is: " + range + " m");
		return range;
	}

	/**
	 * 
	 * @param w0
	 * @param wf
	 * @param speed
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
	 * @param printResult TODO
	 * @return
	 */
	public static double calculateRangeAtConstantSpeedAndAltitude(double w0, double wf, double speed,
			double altitude, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition, 
			PowerPlant thePowerPlant, boolean printResult) {

		int nWeights = 100;
		double[] weight = MyArrayUtils.linspace(wf, w0, nWeights);
		double[] deltaRange = new double[nWeights];
		double[] cl = new double[nWeights];

		for (int i=0; i<nWeights; i++) {

			cl[i] = LiftCalc.calculateLiftCoeff(weight[i], speed, surface, altitude);
			
			if (ThrustCalc.compareThrustAvailableToDrag(
					speed, weight[i], cl[i], altitude, 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant) <= 0.) { 
				if (printResult)
					JPADStaticWriteUtils.logToConsole("The aircraft cannot fly at constant speed= " + speed + " m/s and h= " + altitude + " m");
				return 0.;
			}
			
			deltaRange[i] = calculateDeltaRange(speed, cl[i], weight[i], altitude, 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		}

		double range = MyMathUtils.integrate1DSimpsonSpline(weight, deltaRange, wf*1.0001, w0*0.9999);
		if (printResult)
			JPADStaticWriteUtils.logToConsole("Range at constant speed= " + speed + " m/s and constant h= " + altitude + " m is: " + range + " m");
		return range;
	}

	/**
	 * 
	 * @param w0
	 * @param wf
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
	 * @param printResult TODO
	 * @return
	 */
	public static double calculateRangeAtConstantLiftCoefficientAndAltitude(double w0, double wf, double cl,
			double altitude, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition, 
			PowerPlant thePowerPlant, boolean printResult) {

		int nWeights = 100;
		double[] weight = MyArrayUtils.linspace(wf, w0, nWeights);
		double[] deltaRange = new double[nWeights];
		double[] speed = new double[nWeights];

		for (int i=0; i<nWeights; i++) {

			speed[i] = SpeedCalc.calculateSpeedAtCL(weight[i], surface, AtmosphereCalc.getDensity(altitude), cl);

			if (ThrustCalc.compareThrustAvailableToDrag(
					speed[i], weight[i], cl, altitude, 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant) <= 0.) {
				if (printResult)
					JPADStaticWriteUtils.logToConsole("The aircraft cannot fly at constant CL= " + cl + " and constant h= " + altitude + " m");
				return 0.;
			}
			
			deltaRange[i] = calculateDeltaRange(speed[i], cl, weight[i], altitude, 
					cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		}

		double range = MyMathUtils.integrate1DSimpsonSpline(weight, deltaRange, wf*1.0001, w0*0.9999);
		if(printResult)
			JPADStaticWriteUtils.logToConsole("Range at constant CL= " + cl + " and constant h= " + altitude + " m is: " + range + " m");
		return range;
	}

	/**
	 * Evaluate range at several speeds
	 * @param w0
	 * @param wf
	 * @param speed
	 * @param cl
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
	public static double[] calculateRangeAtConstantSpeedAndLiftCoefficientJet(
			double w0, double wf, double[] speed, double cl,
			double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition, 
			PowerPlant thePowerPlant) {

		double[] range = new double[speed.length];

		for(int i=0; i<speed.length; i++) {
			range[i] = calculateRangeAtConstantSpeedAndLiftCoefficient(w0, wf, speed[i], 
					cl, cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, false);
		}

		return range;
	}

	/**
	 * Evaluate range at several speeds and final weight
	 * @param w0
	 * @param wf
	 * @param speed
	 * @param cl
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
	public static double[][] calculateRangeAtConstantSpeedAndLiftCoefficient(
			double w0, double[] wf, double[] speed, double cl,
			double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType, 
			double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant) {
		
		double[][] range = new double[wf.length][speed.length];

		for(int i=0; i<wf.length; i++) {
			range[i] = calculateRangeAtConstantSpeedAndLiftCoefficientJet(w0, wf[i], speed, 
					cl, cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		}

		return range;
	}

	/**
	 * Evaluate range at several speeds and fixed altitude
	 * @param w0
	 * @param wf
	 * @param speed
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
	public static double[] calculateRangeAtConstantSpeedAndAltitudeJet(double w0, double wf, double[] speed,
			double altitude, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition,
			PowerPlant thePowerPlant) {

		double[] range = new double[speed.length];

		for(int i=0; i<speed.length; i++) {
			range[i] = calculateRangeAtConstantSpeedAndAltitude(w0, wf, speed[i], 
					altitude, cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant, false);
		}

		return range;
	}

	/**
	 * Evaluate range at several speeds and altitudes
	 * @param w0
	 * @param wf
	 * @param speed
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
	public static double[][] calculateRangeAtConstantSpeedAndAltitude(double w0, double wf, double[] speed,
			double[] altitude, double cd0, double oswald, double surface, double ar, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType, double t0, int nEngine, double phi, double bpr, 
			EngineTypeEnum engineType, EngineOperatingConditionEnum flightCondition, 
			PowerPlant thePowerPlant) {

		double[][] range = new double[altitude.length][speed.length];

		for(int i=0; i<altitude.length; i++) {
			range[i] = calculateRangeAtConstantSpeedAndAltitudeJet(w0, wf, speed, 
					altitude[i], cd0, oswald, surface, ar, sweepHalfChord, tcMax, airfoilType, 
					t0, nEngine, phi, bpr, engineType, flightCondition, thePowerPlant);
		}

		return range;
	}


	public double getSfcBreguet() {
		return sfcBreguet;
	}

	public double getRangeBreguetJet() {
		return rangeBreguetJet;
	}

	public double getRangeSpeedAndClConstant() {
		return rangeSpeedAndClConstant;
	}

	public double getRangeSpeedAndAltitudeConstant() {
		return rangeSpeedAndAltitudeConstant;
	}

	public double getRangeClAndAltitudeConstant() {
		return rangeClAndAltitudeConstant;
	}

	public double[][] getRangeSpeedAndClConstantM() {
		return rangeSpeedAndClConstantM;
	}

	public double[][] getRangeSpeedAndAltitudeConstantM() {
		return rangeSpeedAndAltitudeConstantM;
	}

}
