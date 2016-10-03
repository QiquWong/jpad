package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.OperatingConditions;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class FlightManeuveringEnvelopeCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLES DECLARATION:

	//assigned:
	double _positiveLimitLoadFactor;
	double _negativeLimitLoadFactor;
	double _cLMaxClean;
	double _cLMaxFullFlap;
	double _cLMaxInverted;
	Amount<?> _cLAlpha;
	Amount<Length> _meanAerodynamicChord;
	Amount<Length> _altitude;
	Amount<Velocity> _cruisingSpeed;
	Amount<Velocity> _diveSpeed;
	Amount<Mass> _aircraftMass;
	Amount<Area> _wingSurface;
	
	//calculated:
	Amount<Velocity> _stallSpeedFullFlap;
	Amount<Velocity> _stallSpeedClean;
	Amount<Velocity> _stallSpeedInverted;
	Amount<Velocity> _maneuveringSpeed;
	Amount<Velocity> _maneuveringFlapSpeed;
	Amount<Velocity> _designFlapSpeed;
	List<Amount<Velocity>> _gustSpeeds;
	double _positiveLoadFactorManeuveringSpeed;
	double _positiveLoadFactorCruisingSpeed;
	double _positiveLoadFactorDiveSpeed;
	double _positiveLoadFactorDesignFlapSpeed;
	double _negativeLoadFactorManeuveringSpeed;
	double _negativeLoadFactorCruisingSpeed;
	double _negativeLoadFactorDiveSpeed;
	
	//--------------------------------------------------------------------------------------------
	// CONSTRUCTOR:
	public FlightManeuveringEnvelopeCalc(
			double cLMaxClean,
			double cLMaxFullFlap,
			double cLMaxInverted,
			double positiveLimitLoadFactor,
			double negativeLimitLoadFactor,
			Amount<Velocity> cruisingSpeed,
			Amount<Velocity> diveSpeed,
			Amount<?> cLAlpha,
			Amount<Length> meanAerodynamicChord,
			Amount<Length> altitude,
			Amount<Mass> aircraftMass,
			Amount<Area> wingSurface
			) {
		
		this._cLMaxClean = cLMaxClean;
		this._cLMaxFullFlap = cLMaxFullFlap;
		this._cLMaxInverted = cLMaxInverted;
		this._positiveLimitLoadFactor = positiveLimitLoadFactor;
		this._negativeLimitLoadFactor = negativeLimitLoadFactor;
		this._cruisingSpeed = cruisingSpeed;
		this._diveSpeed = diveSpeed;
		this._cLAlpha = cLAlpha;
		this._meanAerodynamicChord = meanAerodynamicChord;
		this._altitude = altitude;
		this._aircraftMass = aircraftMass;
		this._wingSurface = wingSurface;

		this._positiveLoadFactorDesignFlapSpeed = 2.0;
		
		this._gustSpeeds = new ArrayList<Amount<Velocity>>();
		if(this._altitude.doubleValue(NonSI.FOOT) >= 20000) {
			_gustSpeeds.add(
					Amount.valueOf(20.1168, SI.METERS_PER_SECOND) // 66 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(15.24, SI.METERS_PER_SECOND)   // 50 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(7.62, SI.METERS_PER_SECOND)    // 25 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-7.62, SI.METERS_PER_SECOND)   // -25 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-15.24, SI.METERS_PER_SECOND)   // -50 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-20.1168, SI.METERS_PER_SECOND) // -66 ft/sec
					);
		}
		else {
			_gustSpeeds.add(
					Amount.valueOf(11.5824, SI.METERS_PER_SECOND) // 38 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(7.62, SI.METERS_PER_SECOND)   // 25 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(3,81, SI.METERS_PER_SECOND)    // 12.5 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-3,81, SI.METERS_PER_SECOND)   // -12.5 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-7.62, SI.METERS_PER_SECOND)   // -25 ft/sec
					);
			_gustSpeeds.add(
					Amount.valueOf(-11.5824, SI.METERS_PER_SECOND) // -38 ft/sec
					);
		}
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	public void calculateManeuveringEnvelope() {
		
		this._stallSpeedClean = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						this._altitude.doubleValue(SI.METER),
						this._aircraftMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						this._cLMaxClean
						),
				SI.METERS_PER_SECOND
				);
		
		this._stallSpeedFullFlap = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						this._altitude.doubleValue(SI.METER),
						this._aircraftMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						this._cLMaxFullFlap
						),
				SI.METERS_PER_SECOND
				);
		
		this._stallSpeedInverted = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						this._altitude.doubleValue(SI.METER),
						this._aircraftMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						Math.abs(this._cLMaxInverted)
						),
				SI.METERS_PER_SECOND
				);
		
		this._maneuveringSpeed = this._stallSpeedClean.times(
				Math.sqrt(
						this._positiveLimitLoadFactor
						)
				);
		this._maneuveringFlapSpeed = this.getStallSpeedFullFlap().times(
				Math.sqrt(
						this._positiveLoadFactorDesignFlapSpeed
						)
				);
		this._designFlapSpeed = this._stallSpeedFullFlap.times(1.8);
		
		//---------------------------------------------------------------------------------------
		// gust modification:
		double density = OperatingConditions.getAtmosphere(
				this._altitude.doubleValue(SI.METER)
				).getDensity()*1000;
		double muGust = (2*this._aircraftMass.doubleValue(SI.KILOGRAM))
				/(density
						*this._meanAerodynamicChord.doubleValue(SI.METER)
						*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
						*this._wingSurface.doubleValue(SI.SQUARE_METRE)
						);
		double kGust = (0.88*muGust)/(5.3 + muGust);
		
		this._positiveLoadFactorManeuveringSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(0).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._maneuveringSpeed.doubleValue(SI.METERS_PER_SECOND)
				);

		this._positiveLoadFactorCruisingSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(1).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._cruisingSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
		this._positiveLoadFactorDiveSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(2).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._diveSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
		this._negativeLoadFactorDiveSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(3).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._diveSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
		this._negativeLoadFactorCruisingSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(4).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._cruisingSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
		this._negativeLoadFactorManeuveringSpeed = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(5).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*density
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._aircraftMass.doubleValue(SI.KILOGRAM))
						)
				*this._maneuveringSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
	}
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public double getPositiveLimitLoadFactor() {
		return _positiveLimitLoadFactor;
	}

	public void setPositiveLimitLoadFactor(double _positiveLimitLoadFactor) {
		this._positiveLimitLoadFactor = _positiveLimitLoadFactor;
	}

	public double getNegativeLimitLoadFactor() {
		return _negativeLimitLoadFactor;
	}

	public void setNegativeLimitLoadFactor(double _negativeLimitLoadFactor) {
		this._negativeLimitLoadFactor = _negativeLimitLoadFactor;
	}

	public double getCLMaxClean() {
		return _cLMaxClean;
	}

	public void setCLMaxClean(double _cLMaxClean) {
		this._cLMaxClean = _cLMaxClean;
	}

	public double getCLMaxFullFlap() {
		return _cLMaxFullFlap;
	}

	public void setCLMaxFullFlap(double _cLMaxFullFlap) {
		this._cLMaxFullFlap = _cLMaxFullFlap;
	}

	public double getCLMaxInverted() {
		return _cLMaxInverted;
	}

	public void setCLMaxInverted(double _cLMaxInverted) {
		this._cLMaxInverted = _cLMaxInverted;
	}

	public Amount<?> getCLAlpha() {
		return _cLAlpha;
	}

	public void setCLAlpha(Amount<?> _cLAlpha) {
		this._cLAlpha = _cLAlpha;
	}

	public Amount<Length> getMeanAerodynamicChord() {
		return _meanAerodynamicChord;
	}

	public void setMeanAerodynamicChord(Amount<Length> _meanAerodynamicChord) {
		this._meanAerodynamicChord = _meanAerodynamicChord;
	}

	public Amount<Length> getAltitude() {
		return _altitude;
	}

	public void setAltitude(Amount<Length> _altitude) {
		this._altitude = _altitude;
	}

	public Amount<Velocity> getStallSpeedFullFlap() {
		return _stallSpeedFullFlap;
	}

	public void setStallSpeedFullFlap(Amount<Velocity> _stallSpeedFullFlap) {
		this._stallSpeedFullFlap = _stallSpeedFullFlap;
	}

	public Amount<Velocity> getStallSpeedClean() {
		return _stallSpeedClean;
	}

	public void setStallSpeedClean(Amount<Velocity> _stallSpeedClean) {
		this._stallSpeedClean = _stallSpeedClean;
	}

	public Amount<Velocity> getStallSpeedInverted() {
		return _stallSpeedInverted;
	}

	public void setStallSpeedInverted(Amount<Velocity> _stallSpeedInverted) {
		this._stallSpeedInverted = _stallSpeedInverted;
	}

	public Amount<Velocity> getManeuveringSpeed() {
		return _maneuveringSpeed;
	}

	public void setManeuveringSpeed(Amount<Velocity> _maneuveringSpeed) {
		this._maneuveringSpeed = _maneuveringSpeed;
	}

	public Amount<Velocity> getManeuveringFlapSpeed() {
		return _maneuveringFlapSpeed;
	}

	public void setManeuveringFlapSpeed(Amount<Velocity> _maneuveringFlapSpeed) {
		this._maneuveringFlapSpeed = _maneuveringFlapSpeed;
	}

	public Amount<Velocity> getCruisingSpeed() {
		return _cruisingSpeed;
	}

	public void setCruisingSpeed(Amount<Velocity> _cruisingSpeed) {
		this._cruisingSpeed = _cruisingSpeed;
	}

	public Amount<Velocity> getDiveSpeed() {
		return _diveSpeed;
	}

	public void setDiveSpeed(Amount<Velocity> _diveSpeed) {
		this._diveSpeed = _diveSpeed;
	}

	public Amount<Velocity> getDesignFlapSpeed() {
		return _designFlapSpeed;
	}

	public void setDesignFlapSpeed(Amount<Velocity> _designFlapSpeed) {
		this._designFlapSpeed = _designFlapSpeed;
	}

	public Amount<Mass> getAircraftMass() {
		return _aircraftMass;
	}

	public void setAircraftMass(Amount<Mass> _aircraftMass) {
		this._aircraftMass = _aircraftMass;
	}

	public Amount<Area> getWingSurface() {
		return _wingSurface;
	}

	public void setWingSurface(Amount<Area> _wingSurface) {
		this._wingSurface = _wingSurface;
	}

	public List<Amount<Velocity>> getGustSpeeds() {
		return _gustSpeeds;
	}

	public void setGustSpeeds(List<Amount<Velocity>> _gustSpeeds) {
		this._gustSpeeds = _gustSpeeds;
	}

	public double getPositiveLoadFactorManeuveringSpeed() {
		return _positiveLoadFactorManeuveringSpeed;
	}

	public void setPositiveLoadFactorManeuveringSpeed(double _positiveLoadFactorManeuveringSpeed) {
		this._positiveLoadFactorManeuveringSpeed = _positiveLoadFactorManeuveringSpeed;
	}

	public double getPositiveLoadFactorCruisingSpeed() {
		return _positiveLoadFactorCruisingSpeed;
	}

	public void setPositiveLoadFactorCruisingSpeed(double _positiveLoadFactorCruisingSpeed) {
		this._positiveLoadFactorCruisingSpeed = _positiveLoadFactorCruisingSpeed;
	}

	public double getPositiveLoadFactorDiveSpeed() {
		return _positiveLoadFactorDiveSpeed;
	}

	public void setPositiveLoadFactorDiveSpeed(double _positiveLoadFactorDiveSpeed) {
		this._positiveLoadFactorDiveSpeed = _positiveLoadFactorDiveSpeed;
	}

	public double getPositiveLoadFactorDesignFlapSpeed() {
		return _positiveLoadFactorDesignFlapSpeed;
	}

	public void setPositiveLoadFactorDesignFlapSpeed(double _positiveLoadFactorDesignFlapSpeed) {
		this._positiveLoadFactorDesignFlapSpeed = _positiveLoadFactorDesignFlapSpeed;
	}

	public double getNegativeLoadFactorManeuveringSpeed() {
		return _negativeLoadFactorManeuveringSpeed;
	}

	public void setNegativeLoadFactorManeuveringSpeed(double _negativeLoadFactorManeuveringSpeed) {
		this._negativeLoadFactorManeuveringSpeed = _negativeLoadFactorManeuveringSpeed;
	}

	public double getNegativeLoadFactorCruisingSpeed() {
		return _negativeLoadFactorCruisingSpeed;
	}

	public void setNegativeLoadFactorCruisingSpeed(double _negativeLoadFactorCruisingSpeed) {
		this._negativeLoadFactorCruisingSpeed = _negativeLoadFactorCruisingSpeed;
	}

	public double getNegativeLoadFactorDiveSpeed() {
		return _negativeLoadFactorDiveSpeed;
	}

	public void setNegativeLoadFactorDiveSpeed(double _negativeLoadFactorDiveSpeed) {
		this._negativeLoadFactorDiveSpeed = _negativeLoadFactorDiveSpeed;
	}
	
}
