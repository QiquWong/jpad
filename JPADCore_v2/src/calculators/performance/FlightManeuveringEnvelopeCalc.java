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
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.RegulationsEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class FlightManeuveringEnvelopeCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLES DECLARATION:

	//assigned:
	RegulationsEnum _regulations;
	AircraftTypeEnum _aircraftType;
	Double _positiveLimitLoadFactor;
	Double _negativeLimitLoadFactor;
	Double _cLMaxClean;
	Double _cLMaxFullFlap;
	Double _cLMaxInverted;
	Double _cLMin;
	Amount<?> _cLAlpha;
	Amount<Length> _meanAerodynamicChord;
	Amount<Length> _altitude;
	Amount<Velocity> _cruisingSpeed;
	Amount<Velocity> _diveSpeed;
	Amount<Mass> _maxTakeOffMass;
	Amount<Mass> _maxLandingMass;
	Amount<Area> _wingSurface;
	
	//calculated:
	Amount<Velocity> _stallSpeedFullFlap;
	Amount<Velocity> _stallSpeedClean;
	Amount<Velocity> _stallSpeedInverted;
	Amount<Velocity> _maneuveringSpeed;
	Amount<Velocity> _maneuveringFlapSpeed;
	Amount<Velocity> _maneuveringSpeedInverted;
	Amount<Velocity> _designFlapSpeed;
	List<Amount<Velocity>> _gustSpeeds;
	List<Amount<Velocity>> _gustSpeedsFlap;
	Double _positiveLoadFactorManeuveringSpeed;
	Double _positiveLoadFactorCruisingSpeed;
	Double _positiveLoadFactorDiveSpeed;
	Double _positiveLoadFactorDesignFlapSpeed;
	Double _negativeLoadFactorManeuveringSpeedInverted;
	Double _negativeLoadFactorCruisingSpeed;
	Double _negativeLoadFactorDiveSpeed;
	Double _positiveLoadFactorManeuveringSpeedWithGust;
	Double _positiveLoadFactorCruisingSpeedWithGust;
	Double _positiveLoadFactorDiveSpeedWithGust;
	Double _positiveLoadFactorDesignFlapSpeedWithGust;
	Double _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	Double _negativeLoadFactorCruisingSpeedWithGust;
	Double _negativeLoadFactorDiveSpeedWithGust;
	Double _negativeLoadFactorDesignFlapSpeedWithGust;
	
	// plot:
	List<Double> _basicManeuveringDiagramLoadFactors;
	List<Amount<Velocity>> _basicManeuveringDiagramSpeedEAS;
	List<Double> _gustCurvesLoadFactors;
	List<Amount<Velocity>> _gustCurvesSpeedEAS;
	List<Double> _envelopeLoadFactors;
	List<Amount<Velocity>> _envelopeSpeedEAS;
	
	List<Double> _flapManeuveringDiagramLoadFactors;
	List<Amount<Velocity>> _flapManeuveringDiagramSpeedEAS;
	List<Double> _gustCurvesLoadFactorsWithFlaps;
	List<Amount<Velocity>> _gustCurvesSpeedEASWithFlaps;
	List<Double> _envelopeLoadFactorsWithFlaps;
	List<Amount<Velocity>> _envelopeSpeedEASWithFlaps;
	
	//--------------------------------------------------------------------------------------------
	// CONSTRUCTOR:
	public FlightManeuveringEnvelopeCalc(
			RegulationsEnum regulations,
			AircraftTypeEnum aircraftType,
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
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> maxLandingMass,
			Amount<Area> wingSurface
			) {
		
		this._regulations = regulations;
		this._aircraftType = aircraftType;
		
		this._cLMaxClean = cLMaxClean;
		this._cLMaxFullFlap = cLMaxFullFlap;
		this._cLMaxInverted = cLMaxInverted;
		this._positiveLimitLoadFactor = positiveLimitLoadFactor;
		this._negativeLimitLoadFactor = negativeLimitLoadFactor;
		this._cLAlpha = cLAlpha;
		this._meanAerodynamicChord = meanAerodynamicChord;
		this._altitude = altitude;
		this._maxTakeOffMass = maxTakeOffMass;
		this._maxLandingMass = maxLandingMass;
		this._wingSurface = wingSurface;
		
		if(this._regulations == RegulationsEnum.FAR_23) {
			if(this._aircraftType == AircraftTypeEnum.ACROBATIC) {
				double wingLoadingAtMaxTakeOffMass = 
						this._maxTakeOffMass.doubleValue(NonSI.POUND)
						/(this._wingSurface.doubleValue(SI.SQUARE_METRE)*10.7639);
				if(wingLoadingAtMaxTakeOffMass <= 20) { 
					this._cruisingSpeed = Amount.valueOf(
							36*Math.sqrt(wingLoadingAtMaxTakeOffMass),
							NonSI.KNOT
							).to(SI.METERS_PER_SECOND);
					this._diveSpeed = this._cruisingSpeed.times(1.55);
				}
				else {
					double kCruisingSpeed = (28.6*wingLoadingAtMaxTakeOffMass)/100;
					double kDiveSpeed = (1.35*wingLoadingAtMaxTakeOffMass)/100;
					this._cruisingSpeed = Amount.valueOf(
							kCruisingSpeed*Math.sqrt(wingLoadingAtMaxTakeOffMass),
							NonSI.KNOT
							).to(SI.METERS_PER_SECOND);
					this._diveSpeed = this._cruisingSpeed.times(kDiveSpeed);
				}
			}
			else {
				double wingLoadingAtMaxTakeOffMass = 
						this._maxTakeOffMass.doubleValue(NonSI.POUND)
						/(this._wingSurface.doubleValue(SI.SQUARE_METRE)*10.7639);
				if(wingLoadingAtMaxTakeOffMass <= 20) { 
					this._cruisingSpeed = Amount.valueOf(
							33*Math.sqrt(wingLoadingAtMaxTakeOffMass),
							NonSI.KNOT
							).to(SI.METERS_PER_SECOND);
					this._diveSpeed = this._cruisingSpeed.times(1.4);
				}
				else {
					double kCruisingSpeed = (-0.055*wingLoadingAtMaxTakeOffMass) + 34.1;
					double kDiveSpeed = (-0.000625*wingLoadingAtMaxTakeOffMass) + 1.4125;
					this._cruisingSpeed = Amount.valueOf(
							kCruisingSpeed*Math.sqrt(wingLoadingAtMaxTakeOffMass),
							NonSI.KNOT
							).to(SI.METERS_PER_SECOND);
					this._diveSpeed = this._cruisingSpeed.times(kDiveSpeed);
				}
			}
		}
		else {
			this._cruisingSpeed = cruisingSpeed;
			this._diveSpeed = diveSpeed;
		}
		
		if((this._regulations == RegulationsEnum.FAR_25)
				&& (cruisingSpeed == null)
				&& (diveSpeed == null)) {
			System.err.println("WARNING : CRUISING SPEED AND DIVE SPEED HAVE TO BE ASSIGNED FOR THE FAR-25");
			return;
		}

		this._gustSpeedsFlap = new ArrayList<Amount<Velocity>>();
		_gustSpeedsFlap.add(
				Amount.valueOf(7.62, SI.METERS_PER_SECOND)    // 25 ft/sec
				);
		_gustSpeedsFlap.add(
				Amount.valueOf(-7.62, SI.METERS_PER_SECOND)   // -25 ft/sec
				);
		
		this._gustSpeeds = new ArrayList<Amount<Velocity>>();
		if(regulations == RegulationsEnum.FAR_23) {
			if(this._altitude.doubleValue(NonSI.FOOT) < 20000) {
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
				double vAFactor = (-0.0009*this._altitude.doubleValue(NonSI.FOOT)) + 84.667;
				double vCFactor = (-0.0008*this._altitude.doubleValue(NonSI.FOOT)) + 66.667;
				double vDFactor = (-0.0004*this._altitude.doubleValue(NonSI.FOOT)) + 33.333;
				
				_gustSpeeds.add(
						Amount.valueOf(vAFactor*0.3048, SI.METERS_PER_SECOND) // 38 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vCFactor*0.3048, SI.METERS_PER_SECOND)   // 25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vDFactor*0.3048, SI.METERS_PER_SECOND)    // 12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vDFactor*0.3048, SI.METERS_PER_SECOND)   // -12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vCFactor*0.3048, SI.METERS_PER_SECOND)   // -25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vDFactor*0.3048, SI.METERS_PER_SECOND) // -38 ft/sec
						);
			}
		}
		else {
			if(this._altitude.doubleValue(NonSI.FOOT) == 0.0) {
				_gustSpeeds.add(
						Amount.valueOf(17.0688, SI.METERS_PER_SECOND)   // 56 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(17.0688, SI.METERS_PER_SECOND)   // 56 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(8.5344, SI.METERS_PER_SECOND)    // 28 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-8.5344, SI.METERS_PER_SECOND)   // -28 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-17.0688, SI.METERS_PER_SECOND)  // -56 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-17.0688, SI.METERS_PER_SECOND)  // -56 ft/sec
						);
			}
			else if((this._altitude.doubleValue(NonSI.FOOT) > 0.0)
					&& (this._altitude.doubleValue(NonSI.FOOT) < 15000)) {
				double vAFactor = (-0.0008*this._altitude.doubleValue(NonSI.FOOT)) + 56;
				double vCFactor = vAFactor;
				double vDFactor = (-0.0004*this._altitude.doubleValue(NonSI.FOOT)) + 28;
				
				_gustSpeeds.add(
						Amount.valueOf(vAFactor*0.3048, SI.METERS_PER_SECOND) // 38 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vCFactor*0.3048, SI.METERS_PER_SECOND)   // 25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vDFactor*0.3048, SI.METERS_PER_SECOND)    // 12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vDFactor*0.3048, SI.METERS_PER_SECOND)   // -12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vCFactor*0.3048, SI.METERS_PER_SECOND)   // -25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vAFactor*0.3048, SI.METERS_PER_SECOND) // -38 ft/sec
						);
			}
			else {
				double vAFactor = (-0.0005*this._altitude.doubleValue(NonSI.FOOT)) + 51.713;
				double vCFactor = vAFactor;
				double vDFactor = (-0.0003*this._altitude.doubleValue(NonSI.FOOT)) + 25.857;
				
				_gustSpeeds.add(
						Amount.valueOf(vAFactor*0.3048, SI.METERS_PER_SECOND)    // 38 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vCFactor*0.3048, SI.METERS_PER_SECOND)    // 25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(vDFactor*0.3048, SI.METERS_PER_SECOND)    // 12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vDFactor*0.3048, SI.METERS_PER_SECOND)   // -12.5 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vCFactor*0.3048, SI.METERS_PER_SECOND)   // -25 ft/sec
						);
				_gustSpeeds.add(
						Amount.valueOf(-vAFactor*0.3048, SI.METERS_PER_SECOND)   // -38 ft/sec
						);
			}
		}
			
	}

	//--------------------------------------------------------------------------------------------
	// METHODS:
	public void calculateManeuveringEnvelope() {

		//.......................................................................................
		// BASIC MANEUVERING DIAGRAM
		//.......................................................................................
		// STALL POINT (n=1)
		this._stallSpeedClean = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						0.0, // altitude = 0 -> EAS
						this._maxTakeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						this._cLMaxClean
						),
				SI.METERS_PER_SECOND
				);
		
		// POINT A
		this._maneuveringSpeed = this._stallSpeedClean.times(
				Math.sqrt(this._positiveLimitLoadFactor)
				);
		this._positiveLoadFactorManeuveringSpeed = this._positiveLimitLoadFactor;
		
		// POINT C 
		this._positiveLoadFactorCruisingSpeed = this._positiveLimitLoadFactor;
		
		// POINT D
		this._positiveLoadFactorDiveSpeed = this._positiveLimitLoadFactor;
		
		// POINT E
		this._negativeLoadFactorDiveSpeed = 0.0;
		
		// POINT F
		this._negativeLoadFactorCruisingSpeed = this._negativeLimitLoadFactor;
		
		// POINT H and STALL POINT (n = -1)
		this._cLMin = -(this._negativeLimitLoadFactor*this._cLMaxInverted);
		this._stallSpeedInverted = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						0.0, // altitude = 0 -> EAS
						this._maxTakeOffMass.doubleValue(SI.KILOGRAM)
						*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						Math.abs(this._cLMin)
						),
				SI.METERS_PER_SECOND
				);
		this._maneuveringSpeedInverted = this._stallSpeedInverted.times(
				Math.sqrt(Math.abs(_negativeLimitLoadFactor))
				);
		this._negativeLoadFactorManeuveringSpeedInverted = this._negativeLimitLoadFactor;
		
		//.......................................................................................
		// FLAP MANEUVERING DIAGRAM
		//.......................................................................................
		this._stallSpeedFullFlap = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						0.0, // altitude = 0 -> EAS
						this._maxTakeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						this._cLMaxFullFlap
						),
				SI.METERS_PER_SECOND
				);

		// from regulations
		this._positiveLoadFactorDesignFlapSpeed = 2.0;
		
		this._maneuveringFlapSpeed = this.getStallSpeedFullFlap().times(
				Math.sqrt(
						this._positiveLoadFactorDesignFlapSpeed
						)
				);
		
		this._designFlapSpeed = Amount.valueOf(
				1.8*SpeedCalc.calculateSpeedStall(
						0.0, // altitude = 0 -> EAS 
						this._maxLandingMass.doubleValue(SI.KILOGRAM)
						*AtmosphereCalc.g0.getEstimatedValue(),
						this._wingSurface.doubleValue(SI.SQUARE_METRE),
						this._cLMaxFullFlap
						),
				SI.METERS_PER_SECOND
				);
		
		//.......................................................................................
		// GUST MODIFICATIONS (NO FLAP)
		//.......................................................................................
		double density = OperatingConditions.getAtmosphere(
				_altitude.doubleValue(SI.METER)
				).getDensity()*1000;
		double muGust = (2*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
				/(density
						*this._meanAerodynamicChord.doubleValue(SI.METER)
						*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
						*this._wingSurface.doubleValue(SI.SQUARE_METRE)
						);
		double kGust = (0.88*muGust)/(5.3 + muGust);

		if((_regulations != RegulationsEnum.FAR_23)
				&& (_aircraftType != AircraftTypeEnum.COMMUTER)) {
			
			this._positiveLoadFactorManeuveringSpeedWithGust = 1 + (
					(
							(kGust
									*this._gustSpeeds.get(0).doubleValue(SI.METERS_PER_SECOND)
									*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
									*1.225
									*this._wingSurface.doubleValue(SI.SQUARE_METRE))
							/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
							)
					*this._maneuveringSpeed.doubleValue(SI.METERS_PER_SECOND)
					);
			
			this._negativeLoadFactorManeuveringSpeedInvertedWithGust = 1 + (
					(
							(kGust
									*this._gustSpeeds.get(5).doubleValue(SI.METERS_PER_SECOND)
									*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
									*1.225
									*this._wingSurface.doubleValue(SI.SQUARE_METRE))
							/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
							)
					*this._maneuveringSpeed.doubleValue(SI.METERS_PER_SECOND)
					);
		}
		
		this._positiveLoadFactorCruisingSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(1).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._cruisingSpeed.doubleValue(SI.METERS_PER_SECOND)
				);

		this._positiveLoadFactorDiveSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(2).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._diveSpeed.doubleValue(SI.METERS_PER_SECOND)
				);

		this._negativeLoadFactorDiveSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(3).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._diveSpeed.doubleValue(SI.METERS_PER_SECOND)
				);

		this._negativeLoadFactorCruisingSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeeds.get(4).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._cruisingSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
		
		//.......................................................................................
		// GUST MODIFICATIONS (WITH FLAP)
		//.......................................................................................
		this._positiveLoadFactorDesignFlapSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeedsFlap.get(0).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._designFlapSpeed.doubleValue(SI.METERS_PER_SECOND)
				);

		this._negativeLoadFactorDesignFlapSpeedWithGust = 1 + (
				(
						(kGust
								*this._gustSpeedsFlap.get(1).doubleValue(SI.METERS_PER_SECOND)
								*this._cLAlpha.to(SI.RADIAN).inverse().getEstimatedValue()
								*1.225
								*this._wingSurface.doubleValue(SI.SQUARE_METRE))
						/(2*AtmosphereCalc.g0.getEstimatedValue()*this._maxTakeOffMass.doubleValue(SI.KILOGRAM))
						)
				*this._designFlapSpeed.doubleValue(SI.METERS_PER_SECOND)
				);
	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		
		if(_regulations == RegulationsEnum.FAR_23) {
			sb.append("-----------------------------------------------------------------------\n")
			.append(" REGULATION : " + _regulations + "\n")
			.append(" AIRCRAFT TYPE : " + _aircraftType + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" BASIC MANEUVERING DIAGRAM" + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Stall speed clean = " + getStallSpeedClean() + "\n")
			.append("Stall speed inverted = " + getStallSpeedInverted() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed = " + getManeuveringSpeed() + "\n")
			.append("Positive limit load factor maneuvering speed = " + getPositiveLoadFactorManeuveringSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Positive limit load factor cruising speed = " + getPositiveLoadFactorCruisingSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Positive limit load factor dive speed = " + getPositiveLoadFactorDiveSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Negative limit load factor dive speed = " + getNegativeLoadFactorDiveSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Negative limit load factor cruising speed = " + getNegativeLoadFactorCruisingSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed inverted = " + getManeuveringSpeedInverted() + "\n")
			.append("Negative limit load factor maneuvering speed inverted = " + getNegativeLoadFactorManeuveringSpeedInverted() + "\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" FLAP MANEUVERING DIAGRAM " + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Stall speed full flap = " + getStallSpeedFullFlap() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed full flap = " + getManeuveringFlapSpeed() + "\n")
			.append("Positive limit load factor full flap maneuvering speed = " + getPositiveLoadFactorDesignFlapSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Design speed full flap = " + getDesignFlapSpeed() + "\n")
			.append("Positive limit load factor full flap maneuvering speed = " + getPositiveLoadFactorDesignFlapSpeed() + "\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" GUST MODIFICATION (NO FLAP)" + "\n")
			.append("-----------------------------------------------------------------------\n");

			if (_aircraftType != AircraftTypeEnum.COMMUTER)
				sb.append("Maneuvering speed = " + getManeuveringSpeed() + "\n")
				.append("Positive limit load factor maneuvering speed (GUST) = " + getPositiveLoadFactorManeuveringSpeedWithGust() + "\n")
				.append(".......................................................................\n");		

			sb.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Positive limit load factor cruising speed (GUST) = " + getPositiveLoadFactorCruisingSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Positive limit load factor dive speed (GUST) = " + getPositiveLoadFactorDiveSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Negative limit load factor dive speed (GUST) = " + getNegativeLoadFactorDiveSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Negative limit load factor cruising speed (GUST) = " + getNegativeLoadFactorCruisingSpeedWithGust() + "\n");

			if (_aircraftType != AircraftTypeEnum.COMMUTER)
				sb.append(".......................................................................\n")
				.append("Maneuvering speed = " + getManeuveringSpeedInverted() + "\n")
				.append("Negative limit load factor maneuvering speed (GUST) = " + getNegativeLoadFactorManeuveringSpeedInvertedWithGust()+ "\n");
			
			sb.append("\n-----------------------------------------------------------------------\n")
			.append(" GUST MODIFICATION (WITH FLAP)" + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Design flap speed = " + getDesignFlapSpeed() + "\n")
			.append("Positive limit load factor design flap speed (GUST) = " + getPositiveLoadFactorDesignFlapSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Design flap speed = " + getDesignFlapSpeed() + "\n")
			.append("Negative limit load factor design flap speed (GUST) = " + getNegativeLoadFactorDesignFlapSpeedWithGust()+ "\n");
		}
		else {
			sb.append("-----------------------------------------------------------------------\n")
			.append(" REGULATION : " + _regulations + "\n")
			.append(" AIRCRAFT TYPE : " + _aircraftType + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" BASIC MANEUVERING DIAGRAM" + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Stall speed clean = " + getStallSpeedClean() + "\n")
			.append("Stall speed inverted = " + getStallSpeedInverted() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed = " + getManeuveringSpeed() + "\n")
			.append("Positive limit load factor maneuvering speed = " + getPositiveLoadFactorManeuveringSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Positive limit load factor cruising speed = " + getPositiveLoadFactorCruisingSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Positive limit load factor dive speed = " + getPositiveLoadFactorDiveSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Negative limit load factor dive speed = " + getNegativeLoadFactorDiveSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Negative limit load factor cruising speed = " + getNegativeLoadFactorCruisingSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed inverted = " + getManeuveringSpeedInverted() + "\n")
			.append("Negative limit load factor maneuvering speed inverted = " + getNegativeLoadFactorManeuveringSpeedInverted() + "\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" FLAP MANEUVERING DIAGRAM " + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Stall speed full flap = " + getStallSpeedFullFlap() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed full flap = " + getManeuveringFlapSpeed() + "\n")
			.append("Positive limit load factor full flap maneuvering speed = " + getPositiveLoadFactorDesignFlapSpeed() + "\n")
			.append(".......................................................................\n")
			.append("Design speed full flap = " + getDesignFlapSpeed() + "\n")
			.append("Positive limit load factor full flap maneuvering speed = " + getPositiveLoadFactorDesignFlapSpeed() + "\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" GUST MODIFICATION (NO FLAP)" + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Maneuvering speed = " + getManeuveringSpeed() + "\n")
			.append("Positive limit load factor maneuvering speed (GUST) = " + getPositiveLoadFactorManeuveringSpeedWithGust() + "\n")
			.append(".......................................................................\n")		
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Positive limit load factor cruising speed (GUST) = " + getPositiveLoadFactorCruisingSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Positive limit load factor dive speed (GUST) = " + getPositiveLoadFactorDiveSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Dive speed = " + getDiveSpeed() + "\n")
			.append("Negative limit load factor dive speed (GUST) = " + getNegativeLoadFactorDiveSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Cruising speed = " + getCruisingSpeed() + "\n")
			.append("Negative limit load factor cruising speed (GUST) = " + getNegativeLoadFactorCruisingSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Maneuvering speed = " + getManeuveringSpeedInverted() + "\n")
			.append("Negative limit load factor maneuvering speed (GUST) = " + getNegativeLoadFactorManeuveringSpeedInvertedWithGust()+ "\n")
			.append("\n-----------------------------------------------------------------------\n")
			.append(" GUST MODIFICATION (WITH FLAP)" + "\n")
			.append("-----------------------------------------------------------------------\n")
			.append("Design flap speed = " + getDesignFlapSpeed() + "\n")
			.append("Positive limit load factor design flap speed (GUST) = " + getPositiveLoadFactorDesignFlapSpeedWithGust() + "\n")
			.append(".......................................................................\n")
			.append("Design flap speed = " + getDesignFlapSpeed() + "\n")
			.append("Negative limit load factor design flap speed (GUST) = " + getNegativeLoadFactorDesignFlapSpeedWithGust()+ "\n");
			
		}
		return sb.toString();
	}
	
	public void plotManeuveringEnvelope(String folderPathName) {
		
		//--------------------------------------------------------------------------------------
		// BASIC MANEUVERING DIAGRAM
		//--------------------------------------------------------------------------------------
		this._basicManeuveringDiagramSpeedEAS = new ArrayList<Amount<Velocity>>();
		this._basicManeuveringDiagramLoadFactors = new ArrayList<Double>();
		this._gustCurvesSpeedEAS = new ArrayList<Amount<Velocity>>();
		this._gustCurvesLoadFactors = new ArrayList<Double>();
		
		this._flapManeuveringDiagramSpeedEAS = new ArrayList<Amount<Velocity>>();
		this._flapManeuveringDiagramLoadFactors = new ArrayList<Double>();
		this._gustCurvesSpeedEASWithFlaps = new ArrayList<Amount<Velocity>>();
		this._gustCurvesLoadFactorsWithFlaps = new ArrayList<Double>();
		
		// STALL CONDITIONS:
		this._basicManeuveringDiagramSpeedEAS.add(_stallSpeedClean);
		this._basicManeuveringDiagramLoadFactors.add(0.0);
		this._basicManeuveringDiagramSpeedEAS.add(_stallSpeedClean);
		this._basicManeuveringDiagramLoadFactors.add(1.0);
		
		// POSITIVE PARABOLIC TRIAT:
		for(int i=2; i<10; i++) {
			this._basicManeuveringDiagramSpeedEAS.add(
					this._basicManeuveringDiagramSpeedEAS.get(i-1)
						.plus((_maneuveringSpeed.minus(_stallSpeedClean)).divide(9))
						);
			this._basicManeuveringDiagramLoadFactors.add(
					(_cLMaxClean
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._basicManeuveringDiagramSpeedEAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// POINT A:
		this._basicManeuveringDiagramSpeedEAS.add(_maneuveringSpeed);
		this._basicManeuveringDiagramLoadFactors.add(_positiveLoadFactorManeuveringSpeed);
		
		// POINT C:
		this._basicManeuveringDiagramSpeedEAS.add(_cruisingSpeed);
		this._basicManeuveringDiagramLoadFactors.add(_positiveLoadFactorCruisingSpeed);
		
		// POINT D:
		this._basicManeuveringDiagramSpeedEAS.add(_diveSpeed);
		this._basicManeuveringDiagramLoadFactors.add(_positiveLoadFactorDiveSpeed);
		
		// POINT E:
		this._basicManeuveringDiagramSpeedEAS.add(_diveSpeed);
		this._basicManeuveringDiagramLoadFactors.add(_negativeLoadFactorDiveSpeed);
		
		// POINT F:
		this._basicManeuveringDiagramSpeedEAS.add(_cruisingSpeed);
		this._basicManeuveringDiagramLoadFactors.add(_negativeLoadFactorCruisingSpeed);
		
		// POINT H:
		this._basicManeuveringDiagramSpeedEAS.add(_maneuveringSpeedInverted);
		this._basicManeuveringDiagramLoadFactors.add(_negativeLoadFactorManeuveringSpeedInverted);
		
		// NEGATIVE PARABOLIC TRAIT:
		for(int i=16; i<25; i++) {
			this._basicManeuveringDiagramSpeedEAS.add(
					this._basicManeuveringDiagramSpeedEAS.get(i-1)
						.minus((_maneuveringSpeedInverted.minus(_stallSpeedInverted)).divide(9))
						);
			this._basicManeuveringDiagramLoadFactors.add(
					(_cLMin
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._basicManeuveringDiagramSpeedEAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// INVERTED FLIGHT STALL CONDITIONS:
		this._basicManeuveringDiagramSpeedEAS.add(_stallSpeedInverted);
		this._basicManeuveringDiagramLoadFactors.add(-1.0);
		this._basicManeuveringDiagramSpeedEAS.add(_stallSpeedInverted);
		this._basicManeuveringDiagramLoadFactors.add(0.0);
		
		//--------------------------------------------------------------------------------------
		// GUST CURVES:
		//--------------------------------------------------------------------------------------
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		// point A (eventually)
		if(this._positiveLoadFactorManeuveringSpeedWithGust != null) {
			this._gustCurvesSpeedEAS.add(_maneuveringSpeed);
			this._gustCurvesLoadFactors.add(_positiveLoadFactorManeuveringSpeedWithGust);
		}
			
		// point C
		this._gustCurvesSpeedEAS.add(_cruisingSpeed);
		this._gustCurvesLoadFactors.add(_positiveLoadFactorCruisingSpeedWithGust);
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		// point C
		this._gustCurvesSpeedEAS.add(_cruisingSpeed);
		this._gustCurvesLoadFactors.add(_positiveLoadFactorCruisingSpeedWithGust);
		
		// point D
		this._gustCurvesSpeedEAS.add(_diveSpeed);
		this._gustCurvesLoadFactors.add(_positiveLoadFactorDiveSpeedWithGust);
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		// point D
		this._gustCurvesSpeedEAS.add(_diveSpeed);
		this._gustCurvesLoadFactors.add(_positiveLoadFactorDiveSpeedWithGust);
		
		// point E
		this._gustCurvesSpeedEAS.add(_diveSpeed);
		this._gustCurvesLoadFactors.add(_negativeLoadFactorDiveSpeedWithGust);
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		// point E
		this._gustCurvesSpeedEAS.add(_diveSpeed);
		this._gustCurvesLoadFactors.add(_negativeLoadFactorDiveSpeedWithGust);
		
		// point F
		this._gustCurvesSpeedEAS.add(_cruisingSpeed);
		this._gustCurvesLoadFactors.add(_negativeLoadFactorCruisingSpeedWithGust);
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		// point F
		this._gustCurvesSpeedEAS.add(_cruisingSpeed);
		this._gustCurvesLoadFactors.add(_negativeLoadFactorCruisingSpeedWithGust);
		
		// point F
		this._gustCurvesSpeedEAS.add(_cruisingSpeed);
		this._gustCurvesLoadFactors.add(_negativeLoadFactorCruisingSpeedWithGust);
		
		// point H (eventually)
		if(this._negativeLoadFactorManeuveringSpeedInvertedWithGust != null) {
			this._gustCurvesSpeedEAS.add(_maneuveringSpeedInverted);
			this._gustCurvesLoadFactors.add(_negativeLoadFactorManeuveringSpeedInvertedWithGust);
		}
		
		// initial point
		this._gustCurvesSpeedEAS.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactors.add(1.0);
		
		//--------------------------------------------------------------------------------------
		// ENVELOPE:
		//--------------------------------------------------------------------------------------
		this._envelopeSpeedEAS = new ArrayList<Amount<Velocity>>();
		this._envelopeLoadFactors = new ArrayList<Double>();
		
		// STALL CONDITIONS:
		this._envelopeSpeedEAS.add(_stallSpeedClean);
		this._envelopeLoadFactors.add(0.0);
		this._envelopeSpeedEAS.add(_stallSpeedClean);
		this._envelopeLoadFactors.add(1.0);
		
		// POSITIVE PARABOLIC TRIAT:
		for(int i=2; i<10; i++) {
			this._envelopeSpeedEAS.add(
					this._envelopeSpeedEAS.get(i-1)
						.plus((_maneuveringSpeed.minus(_stallSpeedClean)).divide(9))
						);
			this._envelopeLoadFactors.add(
					(_cLMaxClean
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._envelopeSpeedEAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// POINT A:
		if(this._positiveLoadFactorManeuveringSpeedWithGust != null) {
			this._envelopeSpeedEAS.add(_maneuveringSpeed);
			this._envelopeLoadFactors.add(
					Math.max(
							_positiveLoadFactorManeuveringSpeed,
							_positiveLoadFactorManeuveringSpeedWithGust
							)
					);
		}
		else {
			this._envelopeSpeedEAS.add(_maneuveringSpeed);
			this._envelopeLoadFactors.add(_positiveLoadFactorManeuveringSpeed);
		}
		
		// POINT C:
		this._envelopeSpeedEAS.add(_cruisingSpeed);
		this._envelopeLoadFactors.add(
				Math.max(
						_positiveLoadFactorCruisingSpeed,
						_positiveLoadFactorCruisingSpeedWithGust
						)
				);
		
		// POINT D:
		this._envelopeSpeedEAS.add(_diveSpeed);
		this._envelopeLoadFactors.add(
				Math.max(
						_positiveLoadFactorDiveSpeed,
						_positiveLoadFactorDiveSpeedWithGust
						)
				);
		
		// POINT E:
		this._envelopeSpeedEAS.add(_diveSpeed);
		this._envelopeLoadFactors.add(
				Math.min(
						_negativeLoadFactorDiveSpeed,
						_negativeLoadFactorDiveSpeedWithGust
						)
				);
		
		// POINT F:
		this._envelopeSpeedEAS.add(_cruisingSpeed);
		this._envelopeLoadFactors.add(
				Math.min(
						_negativeLoadFactorCruisingSpeed,
						_negativeLoadFactorCruisingSpeedWithGust
						)
				);
		
		// POINT H:
		if(this._negativeLoadFactorManeuveringSpeedInvertedWithGust != null) {
			this._envelopeSpeedEAS.add(_maneuveringSpeedInverted);
			this._envelopeLoadFactors.add(
					Math.min(
							_negativeLoadFactorManeuveringSpeedInverted,
							_negativeLoadFactorManeuveringSpeedInvertedWithGust
							)
					);
		}
		else {
			this._envelopeSpeedEAS.add(_maneuveringSpeedInverted);
			this._envelopeLoadFactors.add(_negativeLoadFactorManeuveringSpeedInverted);
		}
		
		// NEGATIVE PARABOLIC TRAIT:
		for(int i=16; i<25; i++) {
			this._envelopeSpeedEAS.add(
					this._envelopeSpeedEAS.get(i-1)
						.minus((_maneuveringSpeedInverted.minus(_stallSpeedInverted)).divide(9))
						);
			this._envelopeLoadFactors.add(
					(_cLMin
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._envelopeSpeedEAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// INVERTED FLIGHT STALL CONDITIONS:
		this._envelopeSpeedEAS.add(_stallSpeedInverted);
		this._envelopeLoadFactors.add(-1.0);
		this._envelopeSpeedEAS.add(_stallSpeedInverted);
		this._envelopeLoadFactors.add(0.0);
		
		//--------------------------------------------------------------------------------------
		// BASIC MANEUVERING DIAGRAM (WITH FLPAS)
		//--------------------------------------------------------------------------------------
		this._flapManeuveringDiagramSpeedEAS = new ArrayList<Amount<Velocity>>();
		this._flapManeuveringDiagramLoadFactors = new ArrayList<Double>();
		
		// STALL CONDITIONS:
		this._flapManeuveringDiagramSpeedEAS.add(_stallSpeedFullFlap);
		this._flapManeuveringDiagramLoadFactors.add(0.0);
		this._flapManeuveringDiagramSpeedEAS.add(_stallSpeedFullFlap);
		this._flapManeuveringDiagramLoadFactors.add(1.0);
		
		// POSITIVE PARABOLIC TRIAT:
		for(int i=2; i<10; i++) {
			this._flapManeuveringDiagramSpeedEAS.add(
					this._flapManeuveringDiagramSpeedEAS.get(i-1)
						.plus((_maneuveringFlapSpeed.minus(_stallSpeedFullFlap)).divide(9))
						);
			this._flapManeuveringDiagramLoadFactors.add(
					(_cLMaxFullFlap
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._flapManeuveringDiagramSpeedEAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// POINT A:
		this._flapManeuveringDiagramSpeedEAS.add(_maneuveringFlapSpeed);
		this._flapManeuveringDiagramLoadFactors.add(_positiveLoadFactorDesignFlapSpeed);
		
		// POINT D:
		this._flapManeuveringDiagramSpeedEAS.add(_designFlapSpeed);
		this._flapManeuveringDiagramLoadFactors.add(_positiveLoadFactorDesignFlapSpeed);
		
		// POINT D0:
		this._flapManeuveringDiagramSpeedEAS.add(_designFlapSpeed);
		this._flapManeuveringDiagramLoadFactors.add(0.0);
		
		//--------------------------------------------------------------------------------------
		// GUST CURVES:
		//--------------------------------------------------------------------------------------
		this._gustCurvesSpeedEASWithFlaps = new ArrayList<Amount<Velocity>>();
		this._gustCurvesLoadFactorsWithFlaps = new ArrayList<Double>();
		
		// initial point
		this._gustCurvesSpeedEASWithFlaps.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactorsWithFlaps.add(1.0);
					
		// point D
		this._gustCurvesSpeedEASWithFlaps.add(_designFlapSpeed);
		this._gustCurvesLoadFactorsWithFlaps.add(_positiveLoadFactorDesignFlapSpeedWithGust);
		
		// point -D
		this._gustCurvesSpeedEASWithFlaps.add(_designFlapSpeed);
		this._gustCurvesLoadFactorsWithFlaps.add(_negativeLoadFactorDesignFlapSpeedWithGust);
		
		// initial point
		this._gustCurvesSpeedEASWithFlaps.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this._gustCurvesLoadFactorsWithFlaps.add(1.0);		
		
		//--------------------------------------------------------------------------------------
		// ENVELOPE:
		//--------------------------------------------------------------------------------------
		this._envelopeSpeedEASWithFlaps = new ArrayList<Amount<Velocity>>();
		this._envelopeLoadFactorsWithFlaps = new ArrayList<Double>();
		
		// STALL CONDITIONS:
		this._envelopeSpeedEASWithFlaps.add(_stallSpeedFullFlap);
		this._envelopeLoadFactorsWithFlaps.add(0.0);
		this._envelopeSpeedEASWithFlaps.add(_stallSpeedFullFlap);
		this._envelopeLoadFactorsWithFlaps.add(1.0);
		
		// POSITIVE PARABOLIC TRIAT:
		for(int i=2; i<10; i++) {
			this._envelopeSpeedEASWithFlaps.add(
					this._envelopeSpeedEASWithFlaps.get(i-1)
						.plus((_maneuveringFlapSpeed.minus(_stallSpeedFullFlap)).divide(9))
						);
			this._envelopeLoadFactorsWithFlaps.add(
					(_cLMaxFullFlap
							*_wingSurface.doubleValue(SI.SQUARE_METRE)
							*1.225
							*Math.pow(
									this._envelopeSpeedEASWithFlaps.get(i).doubleValue(SI.METERS_PER_SECOND),
									2
									)
							)
					/(2*_maxTakeOffMass.doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.getEstimatedValue())
					);
		}
		
		// POINT A:
		this._envelopeSpeedEASWithFlaps.add(_maneuveringFlapSpeed);
		this._envelopeLoadFactorsWithFlaps.add(_positiveLoadFactorDesignFlapSpeed);
		
		// POINT D:
		this._envelopeSpeedEASWithFlaps.add(_designFlapSpeed);
		this._envelopeLoadFactorsWithFlaps.add(
				Math.max(
						_positiveLoadFactorDesignFlapSpeed,
						_positiveLoadFactorDesignFlapSpeedWithGust
						)
				);
		
		// POINT -D:
		this._envelopeSpeedEASWithFlaps.add(_designFlapSpeed);
		this._envelopeLoadFactorsWithFlaps.add(
				Math.min(
						0.0,
						_negativeLoadFactorDesignFlapSpeedWithGust
						)
				);
		
		List<Double[]> xList = new ArrayList<>();
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_basicManeuveringDiagramSpeedEAS));
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_envelopeSpeedEAS));
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_gustCurvesSpeedEAS));
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_flapManeuveringDiagramSpeedEAS));
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_envelopeSpeedEASWithFlaps));
		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_gustCurvesSpeedEASWithFlaps));
		
		List<Double[]> yList = new ArrayList<>();
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_basicManeuveringDiagramLoadFactors));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_envelopeLoadFactors));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_gustCurvesLoadFactors));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_flapManeuveringDiagramLoadFactors));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_envelopeLoadFactorsWithFlaps));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_gustCurvesLoadFactorsWithFlaps));
		
		List<String> legend = new ArrayList<>();
		legend.add("Maneuvering diagram");
		legend.add("Envelope");
		legend.add("Gust curves");
		legend.add("Maneuvering diagram (FLAP)");
		legend.add("Envelope (FLAPS)");
		legend.add("Gust curves (FLAPS)");
		
		try {
			MyChartToFileUtils.plot(
					xList, yList,
					"Flight Maneuvering Envelope", "V (EAS)", "",
					0.0, null, null, null, "m/s", "",
					true, legend, 
					folderPathName, "Flight Maneuvering Envelope"
					);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public Double getPositiveLimitLoadFactor() {
		return _positiveLimitLoadFactor;
	}

	public void setPositiveLimitLoadFactor(Double _positiveLimitLoadFactor) {
		this._positiveLimitLoadFactor = _positiveLimitLoadFactor;
	}

	public Double getNegativeLimitLoadFactor() {
		return _negativeLimitLoadFactor;
	}

	public void setNegativeLimitLoadFactor(Double _negativeLimitLoadFactor) {
		this._negativeLimitLoadFactor = _negativeLimitLoadFactor;
	}

	public Double getCLMaxClean() {
		return _cLMaxClean;
	}

	public void setCLMaxClean(Double _cLMaxClean) {
		this._cLMaxClean = _cLMaxClean;
	}

	public Double getCLMaxFullFlap() {
		return _cLMaxFullFlap;
	}

	public void setCLMaxFullFlap(Double _cLMaxFullFlap) {
		this._cLMaxFullFlap = _cLMaxFullFlap;
	}

	public Double getCLMaxInverted() {
		return _cLMaxInverted;
	}

	public void setCLMaxInverted(Double _cLMaxInverted) {
		this._cLMaxInverted = _cLMaxInverted;
	}

	public Double getCLMin() {
		return _cLMin;
	}

	public void setCLMin(Double _cLMin) {
		this._cLMin = _cLMin;
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

	public Amount<Velocity> getManeuveringSpeedInverted() {
		return _maneuveringSpeedInverted;
	}

	public void setManeuveringSpeedInverted(Amount<Velocity> _maneuveringSpeedInverted) {
		this._maneuveringSpeedInverted = _maneuveringSpeedInverted;
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

	public Amount<Mass> getMaxTakeOffMass() {
		return _maxTakeOffMass;
	}

	public void setMaxTakeOffMass(Amount<Mass> _maxTakeOffMass) {
		this._maxTakeOffMass = _maxTakeOffMass;
	}

	public Amount<Mass> getMaxLandingMass() {
		return _maxLandingMass;
	}

	public void setMaxLandingMass(Amount<Mass> _maxLandingMass) {
		this._maxLandingMass = _maxLandingMass;
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

	public List<Amount<Velocity>> getGustSpeedsFlap() {
		return _gustSpeedsFlap;
	}

	public void setGustSpeedsFlap(List<Amount<Velocity>> _gustSpeedsFlap) {
		this._gustSpeedsFlap = _gustSpeedsFlap;
	}

	public Double getPositiveLoadFactorManeuveringSpeed() {
		return _positiveLoadFactorManeuveringSpeed;
	}

	public void setPositiveLoadFactorManeuveringSpeed(Double _positiveLoadFactorManeuveringSpeed) {
		this._positiveLoadFactorManeuveringSpeed = _positiveLoadFactorManeuveringSpeed;
	}

	public Double getPositiveLoadFactorCruisingSpeed() {
		return _positiveLoadFactorCruisingSpeed;
	}

	public void setPositiveLoadFactorCruisingSpeed(Double _positiveLoadFactorCruisingSpeed) {
		this._positiveLoadFactorCruisingSpeed = _positiveLoadFactorCruisingSpeed;
	}

	public Double getPositiveLoadFactorDiveSpeed() {
		return _positiveLoadFactorDiveSpeed;
	}

	public void setPositiveLoadFactorDiveSpeed(Double _positiveLoadFactorDiveSpeed) {
		this._positiveLoadFactorDiveSpeed = _positiveLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorDesignFlapSpeed() {
		return _positiveLoadFactorDesignFlapSpeed;
	}

	public void setPositiveLoadFactorDesignFlapSpeed(Double _positiveLoadFactorDesignFlapSpeed) {
		this._positiveLoadFactorDesignFlapSpeed = _positiveLoadFactorDesignFlapSpeed;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInverted() {
		return _negativeLoadFactorManeuveringSpeedInverted;
	}

	public void setNegativeLoadFactorManeuveringSpeed(Double _negativeLoadFactorManeuveringSpeed) {
		this._negativeLoadFactorManeuveringSpeedInverted = _negativeLoadFactorManeuveringSpeed;
	}

	public Double getNegativeLoadFactorCruisingSpeed() {
		return _negativeLoadFactorCruisingSpeed;
	}

	public void setNegativeLoadFactorCruisingSpeed(Double _negativeLoadFactorCruisingSpeed) {
		this._negativeLoadFactorCruisingSpeed = _negativeLoadFactorCruisingSpeed;
	}

	public Double getNegativeLoadFactorDiveSpeed() {
		return _negativeLoadFactorDiveSpeed;
	}

	public void setNegativeLoadFactorDiveSpeed(Double _negativeLoadFactorDiveSpeed) {
		this._negativeLoadFactorDiveSpeed = _negativeLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorManeuveringSpeedWithGust() {
		return _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public void setPositiveLoadFactorManeuveringSpeedWithGust(Double _positiveLoadFactorManeuveringSpeedWithGust) {
		this._positiveLoadFactorManeuveringSpeedWithGust = _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public Double getPositiveLoadFactorCruisingSpeedWithGust() {
		return _positiveLoadFactorCruisingSpeedWithGust;
	}

	public void setPositiveLoadFactorCruisingSpeedWithGust(Double _positiveLoadFactorCruisingSpeedWithGust) {
		this._positiveLoadFactorCruisingSpeedWithGust = _positiveLoadFactorCruisingSpeedWithGust;
	}

	public Double getPositiveLoadFactorDiveSpeedWithGust() {
		return _positiveLoadFactorDiveSpeedWithGust;
	}

	public void setPositiveLoadFactorDiveSpeedWithGust(Double _positiveLoadFactorDiveSpeedWithGust) {
		this._positiveLoadFactorDiveSpeedWithGust = _positiveLoadFactorDiveSpeedWithGust;
	}

	public Double getPositiveLoadFactorDesignFlapSpeedWithGust() {
		return _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public void setPositiveLoadFactorDesignFlapSpeedWithGust(Double _positiveLoadFactorDesignFlapSpeedWithGust) {
		this._positiveLoadFactorDesignFlapSpeedWithGust = _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInvertedWithGust() {
		return _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGust(Double _negativeLoadFactorManeuveringSpeedWithGust) {
		this._negativeLoadFactorManeuveringSpeedInvertedWithGust = _negativeLoadFactorManeuveringSpeedWithGust;
	}

	public Double getNegativeLoadFactorCruisingSpeedWithGust() {
		return _negativeLoadFactorCruisingSpeedWithGust;
	}

	public void setNegativeLoadFactorCruisingSpeedWithGust(Double _negativeLoadFactorCruisingSpeedWithGust) {
		this._negativeLoadFactorCruisingSpeedWithGust = _negativeLoadFactorCruisingSpeedWithGust;
	}

	public Double getNegativeLoadFactorDiveSpeedWithGust() {
		return _negativeLoadFactorDiveSpeedWithGust;
	}

	public void setNegativeLoadFactorDiveSpeedWithGust(Double _negativeLoadFactorDiveSpeedWithGust) {
		this._negativeLoadFactorDiveSpeedWithGust = _negativeLoadFactorDiveSpeedWithGust;
	}

	public Double getNegativeLoadFactorDesignFlapSpeedWithGust() {
		return _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public void setNegativeLoadFactorDesignFlapSpeedWithGust(Double _negativeLoadFactorDesignFlapSpeedWithGust) {
		this._negativeLoadFactorDesignFlapSpeedWithGust = _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public List<Double> getGustCurvesLoadFactors() {
		return _gustCurvesLoadFactors;
	}

	public void setGustCurvesLoadFactors(List<Double> _gustCurvesLoadFactors) {
		this._gustCurvesLoadFactors = _gustCurvesLoadFactors;
	}

	public List<Amount<Velocity>> getGustCurvesSpeedEAS() {
		return _gustCurvesSpeedEAS;
	}

	public void setGustCurvesSpeedEAS(List<Amount<Velocity>> _gustCurvesSpeedEAS) {
		this._gustCurvesSpeedEAS = _gustCurvesSpeedEAS;
	}

	public List<Double> getGustCurvesLoadFactorsWithFlaps() {
		return _gustCurvesLoadFactorsWithFlaps;
	}

	public void setGustCurvesLoadFactorsWithFlaps(List<Double> _gustCurvesLoadFactorsWithFlaps) {
		this._gustCurvesLoadFactorsWithFlaps = _gustCurvesLoadFactorsWithFlaps;
	}

	public List<Amount<Velocity>> getGustCurvesSpeedEASWithFlaps() {
		return _gustCurvesSpeedEASWithFlaps;
	}

	public void setGustCurvesSpeedEASWithFlaps(List<Amount<Velocity>> _gustCurvesSpeedEASWithFlaps) {
		this._gustCurvesSpeedEASWithFlaps = _gustCurvesSpeedEASWithFlaps;
	}

	public List<Double> getEnvelopeLoadFactors() {
		return _envelopeLoadFactors;
	}

	public void setEnvelopeLoadFactors(List<Double> _envelopeLoadFactors) {
		this._envelopeLoadFactors = _envelopeLoadFactors;
	}

	public List<Amount<Velocity>> getEnvelopeSpeedEAS() {
		return _envelopeSpeedEAS;
	}

	public void setEnvelopeSpeedEAS(List<Amount<Velocity>> _envelopeSpeedEAS) {
		this._envelopeSpeedEAS = _envelopeSpeedEAS;
	}

	public List<Double> getEnvelopeLoadFactorsWithFlaps() {
		return _envelopeLoadFactorsWithFlaps;
	}

	public void setEnvelopeLoadFactorsWithFlaps(List<Double> _envelopeLoadFactorsWithFlaps) {
		this._envelopeLoadFactorsWithFlaps = _envelopeLoadFactorsWithFlaps;
	}

	public List<Amount<Velocity>> getEnvelopeSpeedEASWithFlaps() {
		return _envelopeSpeedEASWithFlaps;
	}

	public void setEnvelopeSpeedEASWithFlaps(List<Amount<Velocity>> _envelopeSpeedEASWithFlaps) {
		this._envelopeSpeedEASWithFlaps = _envelopeSpeedEASWithFlaps;
	}

	public RegulationsEnum getRegulations() {
		return _regulations;
	}

	public void setRegulations(RegulationsEnum _regulations) {
		this._regulations = _regulations;
	}

	public AircraftTypeEnum getAircraftType() {
		return _aircraftType;
	}

	public void setAircraftType(AircraftTypeEnum _aircraftType) {
		this._aircraftType = _aircraftType;
	}
	
}
