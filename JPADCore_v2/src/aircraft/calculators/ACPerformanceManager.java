package aircraft.calculators;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.lang3.text.WordUtils;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import calculators.performance.PerformanceCalcManager;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import standaloneutils.atmosphere.AtmosphereCalc;

/** 
 * Estimate the whole aircraft performances.
 * This class relies on the libraries contained in the JPADCalculators
 * project.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class ACPerformanceManager {

	private static final String id = "21";

	// TODO :  ADD GET /SET
	private AnalysisTypeEnum _type;
	private String _name;
	
	public static Double nLimit = 2.5;
	public static Double nLimitZFW = 2.5;
	public static Double nUltimate = 1.5 * nLimit;
	
	private Aircraft _theAircraft;
	private Double _nUltimate,
	_nLimit, _nLimitZFW,
	_cruiseCL;

	private Amount<Velocity> _vOptimumCruise, _vMaxCruise,
	_vDive, _vMaxCruiseEAS, _vDiveEAS;

	private Amount<Pressure> _maxDynamicPressure;
	private Amount<Length> _maxAltitudeAtMaxSpeed, _altitudeOptimumCruise, _range;

	private Double _machDive0, _machOptimumCruise, _machMaxCruise;

	int nEngine;

	private PerformanceCalcManager performanceManager, performanceManagerOEI;

	private Amount<Duration> _blockTime, _flightTime;

	/** 
	 * This class holds all performances data inputs necessary to run the program.
	 * It also contains all the methods to estimate the aircraft's performances
	 */
	public ACPerformanceManager() {

		_type = AnalysisTypeEnum.PERFORMANCE;
		_name = WordUtils.capitalizeFully(AnalysisTypeEnum.PERFORMANCE.name());
	}
	

	public ACPerformanceManager(Aircraft aircraft) {
		this();
		_theAircraft = aircraft;
	}

	/**
	 * This method MUST be called only if the aircraft
	 * object is not null
	 */
	public void calculateAllPerformance() {

		if (_theAircraft != null) {
			//		try {
			performanceManager = new PerformanceCalcManager(
					_theAircraft.getTheWeights().get_MZFW().doubleValue(SI.NEWTON),
					_theAircraft.getTheWeights().get_MTOW().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().get_engineList().get(0).get_t0().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().get_engineNumber().intValue(), 
					_theAircraft.getPowerPlant().get_engineList().get(0).get_engineType(), 
					_theAircraft.getPowerPlant().get_engineList().get(0).get_bpr(), 
					_theAircraft.getWing().get_surface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.getWing().get_aspectRatio(), 
					_theAircraft.getWing().get_sweepHalfChordEq().doubleValue(SI.RADIAN), 
					_theAircraft.getWing().get_thicknessMax(), 
					_theAircraft.getWing().get_theAirfoilsList().get(0).getType(), 
					_theAircraft.getWing().getAerodynamics().getCalculateCLMaxClean().phillipsAndAlley(), 
					_theAircraft.getTheAerodynamics().get_cD0(), 
					_theAircraft.getTheAerodynamics().get_oswald());
			// TODO
			//		performanceManager.setMinAndMaxValues(0., 11000., 0., _vDive.doubleValue(SI.METERS_PER_SECOND), 0.7, 1.);
			//		performanceManager.initializeArraysWithMinAndMaxValues();
			performanceManager.setMinAndMaxValuesShortArrays(0., 9000., 50., 340., 0.7, 1.);
			performanceManager.initializeShortArrays();
			performanceManager.calculateAllPerformance();

			performanceManagerOEI = new PerformanceCalcManager(
					_theAircraft.getTheWeights().get_MZFW().doubleValue(SI.NEWTON),
					_theAircraft.getTheWeights().get_MTOW().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().get_engineList().get(0).get_t0().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().get_engineNumber().intValue() - 1, 
					_theAircraft.getPowerPlant().get_engineList().get(0).get_engineType(),
					_theAircraft.getPowerPlant().get_engineList().get(0).get_bpr(), 
					_theAircraft.getWing().get_surface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.getWing().get_aspectRatio(), 
					_theAircraft.getWing().get_sweepHalfChordEq().doubleValue(SI.RADIAN), 
					_theAircraft.getWing().get_thicknessMax(), 
					_theAircraft.getWing().get_theAirfoilsList().get(0).getType(), 
					_theAircraft.getWing().getAerodynamics().getCalculateCLMaxClean().phillipsAndAlley(), 
					_theAircraft.getTheAerodynamics().get_cD0(), 
					_theAircraft.getTheAerodynamics().get_oswald());

			performanceManagerOEI.setMinAndMaxValuesShortArrays(0., 9000., 50., 340., 0.7, 1.);
			performanceManagerOEI.initializeShortArrays();
			performanceManagerOEI.calculateAllPerformance();

			//		} catch(NullPointerException e) {
			//			System.out.println("WARNING: The aircraft object has not been initialized."
			//					+ " Performances cannot be evaluated");
			//			e.printStackTrace();
			//		}
		}
	}


	public Double get_nLimit() {
		return _nLimit;
	}

	public void set_nLimit(Double _nLimit) {
		this._nLimit = _nLimit;
	}

	public Amount<Velocity> get_vMaxCruise() {
		return _vMaxCruise;
	}

	public void set_vMaxCruise(Amount<Velocity> _vMaxCruise) {
		this._vMaxCruise = _vMaxCruise;
	}


	public Amount<Velocity> get_vDive() {
		return _vDive;
	}


	public void set_vDive(Amount<Velocity> _vDive) {
		this._vDive = _vDive;
	}


	public Double get_nUltimate() {
		return _nUltimate;
	}


	public void set_nUltimate(Double _nUltimate) {
		this._nUltimate = _nUltimate;
	}


	public Double get_nLimitZFW() {
		return _nLimitZFW;
	}


	public void set_nLimitZFW(Double _nLimitZFW) {
		this._nLimitZFW = _nLimitZFW;
	}


	public Amount<Pressure> get_maxDynamicPressure() {
		return _maxDynamicPressure;
	}


	public Amount<Velocity> get_vMaxCruiseEAS() {
		return _vMaxCruiseEAS;
	}


	public void set_vMaxCruiseEAS(Amount<Velocity> _vMaxCruiseEAS) {
		this._vMaxCruiseEAS = _vMaxCruiseEAS;
	}


	public Amount<Velocity> get_vDiveEAS() {
		return _vDiveEAS;
	}


	public void set_vDiveEAS(Amount<Velocity> _vDiveEAS) {
		this._vDiveEAS = _vDiveEAS;
	}


	public Amount<Length> get_maxAltitudeAtMaxSpeed() {
		return _maxAltitudeAtMaxSpeed;
	}


	public void set_maxAltitudeAtMaxSpeed(Amount<Length> _maxAltitudeAtMaxSpeed) {
		this._maxAltitudeAtMaxSpeed = _maxAltitudeAtMaxSpeed;
	}


	public Double get_machDive0() {
		return _machDive0;
	}


	public Double get_machMaxCruise() {
		return _machMaxCruise;
	}


	public void set_machMaxCruise(Double _mach) {
		this._machMaxCruise = _mach;
	}


	public Double get_machOptimumCruise() {
		return _machOptimumCruise;
	}


	public void set_machOptimumCruise(Double _machCruise) {
		this._machOptimumCruise = _machCruise;
	}

	public Amount<Length> get_range() {
		return _range;
	}

	public void set_range(Amount<Length> _range) {
		this._range = _range;
	}

	public Double get_cruiseCL() {
		return _cruiseCL;
	}

	public void set_cruiseCL(Double _cruiseCL) {
		this._cruiseCL = _cruiseCL;
	}

	public static String getId() {
		return id;
	}

	public PerformanceCalcManager getPerformanceManager() {
		return performanceManager;
	}

	public PerformanceCalcManager getPerformanceManagerOEI() {
		return performanceManagerOEI;
	}

	public Amount<Velocity> get_vOptimumCruise() {
		return _vOptimumCruise;
	}

	public void set_vDesignCruise(Amount<Velocity> _vDesignCruise) {
		this._vOptimumCruise = _vDesignCruise;
	}

	public Amount<Duration> get_blockTime() {
		return _blockTime;
	}

	public void set_blockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}

	public Amount<Duration> get_flightTime() {
		return _flightTime;
	}

	public void set_flightTime(Amount<Duration> _flightTime) {
		this._flightTime = _flightTime;
	}


}
