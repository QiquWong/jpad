package analyses;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.apache.commons.lang3.text.WordUtils;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.performance.PerformanceCalcManager;
import configuration.enumerations.AnalysisTypeEnum;

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

	private AnalysisTypeEnum _type;
	private String _name;
	
	public Double _nLimit = 2.5;
	public Double _nLimitZFW = 2.5;
	public Double _nUltimate = 1.5 * _nLimit;
	
	private Aircraft _theAircraft;
	private Double _cruiseCL;

	private Amount<Velocity> _vOptimumCruise, _vMaxCruise,
	_vDive, _vMaxCruiseEAS, _vDiveEAS;

	private Amount<Pressure> _maxDynamicPressure;
	private Amount<Length> _maxAltitudeAtMaxSpeed, _range;

	private Double _machDive0, _machOptimumCruise, _machMaxCruise;

	int nEngine;

	private PerformanceCalcManager performanceManager, performanceManagerOEI;

	private Amount<Duration> _blockTime, _flightTime;

	/** 
	 * This class holds all performances data inputs necessary to run the program.
	 * It also contains all the methods to estimate the aircraft's performances
	 */
	public ACPerformanceManager() {

		setType(AnalysisTypeEnum.PERFORMANCE);
		setName(WordUtils.capitalizeFully(AnalysisTypeEnum.PERFORMANCE.name()));
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
					_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().getEngineNumber().intValue(), 
					_theAircraft.getPowerPlant().getEngineList().get(0).getEngineType(), 
					_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(), 
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.getWing().getAspectRatio(), 
					_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN), 
					_theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(), 
					_theAircraft.getWing().getAirfoilList().get(0).getType(), 
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
					_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
					_theAircraft.getPowerPlant().getEngineNumber().intValue() - 1, 
					_theAircraft.getPowerPlant().getEngineList().get(0).getEngineType(),
					_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(), 
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.getWing().getAspectRatio(), 
					_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN), 
					_theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(), 
					_theAircraft.getWing().getAirfoilList().get(0).getType(), 
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


	public Double getNLimit() {
		return _nLimit;
	}

	public void setNLimit(Double _nLimit) {
		this._nLimit = _nLimit;
	}

	public Amount<Velocity> getVMaxCruise() {
		return _vMaxCruise;
	}

	public void setVMaxCruise(Amount<Velocity> _vMaxCruise) {
		this._vMaxCruise = _vMaxCruise;
	}


	public Amount<Velocity> getVDive() {
		return _vDive;
	}


	public void setVDive(Amount<Velocity> _vDive) {
		this._vDive = _vDive;
	}


	public Double getNUltimate() {
		return _nUltimate;
	}


	public void setNUltimate(Double _nUltimate) {
		this._nUltimate = _nUltimate;
	}


	public Double getNLimitZFW() {
		return _nLimitZFW;
	}


	public void setNLimitZFW(Double _nLimitZFW) {
		this._nLimitZFW = _nLimitZFW;
	}


	public Amount<Pressure> getMaxDynamicPressure() {
		return _maxDynamicPressure;
	}


	public Amount<Velocity> getVMaxCruiseEAS() {
		return _vMaxCruiseEAS;
	}


	public void setVMaxCruiseEAS(Amount<Velocity> _vMaxCruiseEAS) {
		this._vMaxCruiseEAS = _vMaxCruiseEAS;
	}


	public Amount<Velocity> getVDiveEAS() {
		return _vDiveEAS;
	}


	public void setVDiveEAS(Amount<Velocity> _vDiveEAS) {
		this._vDiveEAS = _vDiveEAS;
	}


	public Amount<Length> getMaxAltitudeAtMaxSpeed() {
		return _maxAltitudeAtMaxSpeed;
	}


	public void setMaxAltitudeAtMaxSpeed(Amount<Length> _maxAltitudeAtMaxSpeed) {
		this._maxAltitudeAtMaxSpeed = _maxAltitudeAtMaxSpeed;
	}


	public Double getMachDive0() {
		return _machDive0;
	}


	public Double getMachMaxCruise() {
		return _machMaxCruise;
	}


	public void setMachMaxCruise(Double _mach) {
		this._machMaxCruise = _mach;
	}


	public Double getMachOptimumCruise() {
		return _machOptimumCruise;
	}


	public void setMachOptimumCruise(Double _machCruise) {
		this._machOptimumCruise = _machCruise;
	}

	public Amount<Length> getRange() {
		return _range;
	}

	public void setRange(Amount<Length> _range) {
		this._range = _range;
	}

	public Double getCruiseCL() {
		return _cruiseCL;
	}

	public void setCruiseCL(Double _cruiseCL) {
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

	public Amount<Velocity> getVOptimumCruise() {
		return _vOptimumCruise;
	}

	public void setVDesignCruise(Amount<Velocity> _vDesignCruise) {
		this._vOptimumCruise = _vDesignCruise;
	}

	public Amount<Duration> getBlockTime() {
		return _blockTime;
	}

	public void setBlockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}

	public Amount<Duration> getFlightTime() {
		return _flightTime;
	}

	public void setFlightTime(Amount<Duration> _flightTime) {
		this._flightTime = _flightTime;
	}


	public AnalysisTypeEnum getType() {
		return _type;
	}


	public void setType(AnalysisTypeEnum _type) {
		this._type = _type;
	}


	public String getName() {
		return _name;
	}


	public void setName(String _name) {
		this._name = _name;
	}


}
