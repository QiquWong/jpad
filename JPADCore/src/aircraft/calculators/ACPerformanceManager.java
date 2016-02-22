package aircraft.calculators;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import calculators.performance.PerformanceCalcManager;
import configuration.enumerations.AircraftEnum;
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

		_nLimit = 2.5;
		_nLimitZFW = 2.5;
		_nUltimate = 1.5 * _nLimit;

		// Altitude at which TAS is maximum.
		_maxAltitudeAtMaxSpeed = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);

		_altitudeOptimumCruise = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);

		// Cruise Mach number
		_machOptimumCruise = 0.43;

		_vOptimumCruise = Amount.valueOf(_machOptimumCruise*AtmosphereCalc.getSpeedOfSound(_altitudeOptimumCruise.doubleValue(SI.METER)), SI.METERS_PER_SECOND);

		// Max Mach number
		_machMaxCruise = 0.45;

		_blockTime = Amount.valueOf(1.5, NonSI.HOUR);
		_flightTime = Amount.valueOf(1.35, NonSI.HOUR);

		_cruiseCL = 0.45;

		_range = Amount.valueOf(1528., SI.KILOMETER);

		calculateSpeeds();
	}
	
	/**
	 * Overload of the default builder that recognize aircraft name and sets the relative 
	 * data to be used in performances evaluation.
	 * 
	 * @author Vittorio Trifari
	 */
	public ACPerformanceManager(AircraftEnum aircraftName) {
		
		switch(aircraftName) {
		case ATR72:
			_nLimit = 2.5;
			_nLimitZFW = 2.5;
			_nUltimate = 1.5 * _nLimit;

			// Altitude at which TAS is maximum.
			_maxAltitudeAtMaxSpeed = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);

			_altitudeOptimumCruise = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);

			// Cruise Mach number
			_machOptimumCruise = 0.43;

			_vOptimumCruise = Amount.valueOf(_machOptimumCruise*AtmosphereCalc.getSpeedOfSound(_altitudeOptimumCruise.doubleValue(SI.METER)), SI.METERS_PER_SECOND);

			// Max Mach number
			_machMaxCruise = 0.45;

			_blockTime = Amount.valueOf(1.5, NonSI.HOUR);
			_flightTime = Amount.valueOf(1.35, NonSI.HOUR);

			_cruiseCL = 0.45;

			_range = Amount.valueOf(1528., SI.KILOMETER);

			calculateSpeeds();
			break;
			
		case B747_100B:
			_nLimit = 2.5;
			_nLimitZFW = 2.5;
			_nUltimate = 1.5 * _nLimit;

			// Altitude at which TAS is maximum.
			_maxAltitudeAtMaxSpeed = Amount.valueOf(35000., NonSI.FOOT).to(SI.METER);

			_altitudeOptimumCruise = Amount.valueOf(35000., NonSI.FOOT).to(SI.METER);

			// Cruise Mach number
			_machOptimumCruise = 0.84;

			_vOptimumCruise = Amount.valueOf(_machOptimumCruise*AtmosphereCalc.getSpeedOfSound(_altitudeOptimumCruise.doubleValue(SI.METER)), SI.METERS_PER_SECOND);

			// Max Mach number
			_machMaxCruise = 0.89;

			//used for costs --> TODO: need to be fixed with correct data.
			_blockTime = Amount.valueOf(1.5, NonSI.HOUR);
			_flightTime = Amount.valueOf(1.35, NonSI.HOUR);

			_cruiseCL = 0.58;

			_range = Amount.valueOf(9800., SI.KILOMETER);

			calculateSpeeds();
			break;
			
		case AGILE_DC1:
			_nLimit = 2.5;
			_nLimitZFW = 2.5;
			_nUltimate = 1.5 * _nLimit;

			// Altitude at which TAS is maximum.
			_maxAltitudeAtMaxSpeed = Amount.valueOf(36000., NonSI.FOOT).to(SI.METER);

			_altitudeOptimumCruise = Amount.valueOf(36000., NonSI.FOOT).to(SI.METER);

			// Cruise Mach number
			_machOptimumCruise = 0.78;

			_vOptimumCruise = Amount.valueOf(_machOptimumCruise*AtmosphereCalc.getSpeedOfSound(_altitudeOptimumCruise.doubleValue(SI.METER)), SI.METERS_PER_SECOND);

			// Max Mach number
			_machMaxCruise = 0.82;

			_blockTime = Amount.valueOf(2.6, NonSI.HOUR);
			_flightTime = Amount.valueOf(2.33, NonSI.HOUR);

			_cruiseCL = 0.5;

			_range = Amount.valueOf(3500., SI.KILOMETER);

			calculateSpeeds();
			break;
		}
	}

	public ACPerformanceManager(Aircraft aircraft) {
		this();
		_theAircraft = aircraft;
	}

	/**
	 * Evaluate relevant speeds
	 */
	public void calculateSpeeds() {

		// Maximum cruise TAS
		_vMaxCruise = Amount.valueOf(
				_machMaxCruise * 
				OperatingConditions.get_atmosphere(_maxAltitudeAtMaxSpeed.getEstimatedValue()).getSpeedOfSound(), 
				SI.METERS_PER_SECOND); // ATR72 max TAS (Jane's)
		_vMaxCruiseEAS = _vMaxCruise.
				times(Math.sqrt(
						OperatingConditions.get_atmosphere(_maxAltitudeAtMaxSpeed.getEstimatedValue()).getDensityRatio()));

		// FAR Part 25 paragraph 25.335
		_vDive = _vMaxCruise.times(1.25); 
		_vDiveEAS = _vMaxCruiseEAS.times(1.25); 

		_machDive0 = _vDiveEAS.divide(AtmosphereCalc.a0).getEstimatedValue();
		_maxDynamicPressure = Amount.valueOf(0.5 * 
				AtmosphereCalc.rho0.getEstimatedValue()*
				Math.pow(_vDiveEAS.getEstimatedValue(), 2), SI.PASCAL); //TODO ??? Is it correct ???

	}


	/**
	 * This method MUST be called only if the aircraft
	 * object is not null
	 */
	public void calculateAllPerformance() {

		if (_theAircraft != null) {
			//		try {
			performanceManager = new PerformanceCalcManager(
					_theAircraft.get_weights().get_MZFW().doubleValue(SI.NEWTON),
					_theAircraft.get_weights().get_MTOW().doubleValue(SI.NEWTON),
					_theAircraft.get_powerPlant().get_engineList().get(0).get_t0().doubleValue(SI.NEWTON),
					_theAircraft.get_powerPlant().get_engineNumber().intValue(), 
					_theAircraft.get_powerPlant().get_engineList().get(0).get_engineType(), 
					_theAircraft.get_powerPlant().get_engineList().get(0).get_bpr(), 
					_theAircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.get_wing().get_aspectRatio(), 
					_theAircraft.get_wing().get_sweepHalfChordEq().doubleValue(SI.RADIAN), 
					_theAircraft.get_wing().get_thicknessMax(), 
					_theAircraft.get_wing().get_theAirfoilsList().get(0).get_type(), 
					_theAircraft.get_wing().getAerodynamics().getCalculateCLMaxClean().phillipsAndAlley(), 
					_theAircraft.get_theAerodynamics().get_cD0(), 
					_theAircraft.get_theAerodynamics().get_oswald());
			// TODO
			//		performanceManager.setMinAndMaxValues(0., 11000., 0., _vDive.doubleValue(SI.METERS_PER_SECOND), 0.7, 1.);
			//		performanceManager.initializeArraysWithMinAndMaxValues();
			performanceManager.setMinAndMaxValuesShortArrays(0., 9000., 50., 340., 0.7, 1.);
			performanceManager.initializeShortArrays();
			performanceManager.calculateAllPerformance();

			performanceManagerOEI = new PerformanceCalcManager(
					_theAircraft.get_weights().get_MZFW().doubleValue(SI.NEWTON),
					_theAircraft.get_weights().get_MTOW().doubleValue(SI.NEWTON),
					_theAircraft.get_powerPlant().get_engineList().get(0).get_t0().doubleValue(SI.NEWTON),
					_theAircraft.get_powerPlant().get_engineNumber().intValue() - 1, 
					_theAircraft.get_powerPlant().get_engineList().get(0).get_engineType(),
					_theAircraft.get_powerPlant().get_engineList().get(0).get_bpr(), 
					_theAircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE),
					_theAircraft.get_wing().get_aspectRatio(), 
					_theAircraft.get_wing().get_sweepHalfChordEq().doubleValue(SI.RADIAN), 
					_theAircraft.get_wing().get_thicknessMax(), 
					_theAircraft.get_wing().get_theAirfoilsList().get(0).get_type(), 
					_theAircraft.get_wing().getAerodynamics().getCalculateCLMaxClean().phillipsAndAlley(), 
					_theAircraft.get_theAerodynamics().get_cD0(), 
					_theAircraft.get_theAerodynamics().get_oswald());

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
