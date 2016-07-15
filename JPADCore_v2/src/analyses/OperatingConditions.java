package analyses;

import javax.measure.quantity.Angle;
import javax.measure.quantity.DynamicViscosity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;
import javax.xml.bind.annotation.XmlRootElement;

import org.jscience.physics.amount.Amount;

//WARNING: Density is in g/cm3 ( = 1000 kg/m3)
import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.atmosphere.PressureCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.atmosphere.TemperatureCalc;

/**
 * Set the aircraft current operating conditions
 *   
 * @author Lorenzo Attanasio
 */
@XmlRootElement
public class OperatingConditions {

	private static final String _id = "10";
	// Flight parameters
	private Double[] _alpha;
	private Double[] _cL;
	private Amount<Angle> _alphaCurrent;
	private Double _machCurrent, _pressureCoefficientCurrent;
	private Amount<Velocity> _tas, _cas, _eas;
	private Amount<Length> _altitude;


	// Return density ratio, pressure ratio, temperature ratio, ecc ...
	private StdAtmos1976 atmosphere;
	private Amount<VolumetricDensity> _densityCurrent;
	private Amount<Pressure> 
	_staticPressure, _dynamicPressure, _stagnationPressure, 
	_maxDeltaPressure, _maxDynamicPressure;

	private Amount<DynamicViscosity> _mu;
	private Double _machTransonicThreshold = 0.7;
	private Amount<Temperature> _staticTemperature, _stagnationTemperature;



	public OperatingConditions() {

		_alphaCurrent = Amount.valueOf(Math.toRadians(5), SI.RADIAN);
		_machCurrent = 0.43;
		_altitude = Amount.valueOf(6000.0, SI.METER);
		
		_alpha = new Double[]{0., 1., 2., 3., 4., 5., 6.};
		_cL = new Double[]{0., 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4};

		calculate();
	}

	/** 
	 * Evaluate all dependent parameters
	 * @author Lorenzo Attanasio
	 */
	public void calculate() {

		atmosphere = new StdAtmos1976(_altitude.getEstimatedValue());
		_pressureCoefficientCurrent = PressureCalc.calculatePressureCoefficient(_machCurrent);

		_densityCurrent = Amount.valueOf(atmosphere.getDensity()*1000, VolumetricDensity.UNIT);

		_staticPressure = Amount.valueOf(atmosphere.getPressure(), SI.PASCAL);
		_dynamicPressure = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(_machCurrent, _staticPressure.getEstimatedValue())
				, SI.PASCAL);
		_stagnationPressure = _dynamicPressure.times(_pressureCoefficientCurrent).plus(_staticPressure);

		_tas = Amount.valueOf(_machCurrent * atmosphere.getSpeedOfSound(), SI.METERS_PER_SECOND);
		_cas = Amount.valueOf(
				SpeedCalc.calculateCAS(_stagnationPressure.getEstimatedValue(), _staticPressure.getEstimatedValue())
				, SI.METERS_PER_SECOND);
		_eas = Amount.valueOf(Math.sqrt(atmosphere.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(_stagnationPressure.getEstimatedValue(), 
						_staticPressure.getEstimatedValue(), _densityCurrent.getEstimatedValue())
						, SI.METERS_PER_SECOND);

		_staticTemperature = Amount.valueOf(atmosphere.getTemperature(), SI.KELVIN);
		_stagnationTemperature = _staticTemperature
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(_machCurrent));

		// Maximum pressure difference between outside and inside
		_maxDeltaPressure = Amount.valueOf(
				Math.abs(
						_staticPressure.getEstimatedValue() 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		double _mu0 = 17.33e-6; // Pa*s, grc.nasa.gov/WWW/BGH/Viscosity.html
		double T0 = 288.166667; // K
		double C = 110.4; // K

		_mu = Amount.valueOf(
				_mu0*((T0 + C)
						/ (atmosphere.getTemperature() + C))
						* Math.pow(
								atmosphere.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);

	}

	// file matlab Ing. Della Vecchia
	public double calculateReCutOff(double lenght, double k){
		if (_machCurrent <= _machTransonicThreshold) 
			return 38.21*Math.pow(lenght/k, 1.053);  //Reynolds  cut-off for wing   // ADAS pag 91. Subsonic case
		else 
			return 44.62*Math.pow(lenght/k,1.053)*Math.pow(_machCurrent, 1.16); // Transonic or supersonic case
	}

	public Double calculateRe(double lenght, double roughness){
		double re = _densityCurrent.getEstimatedValue()*_tas.getEstimatedValue()*lenght/_mu.getEstimatedValue();
//
//		if (calculateReCutOff(lenght, roughness) < re) {
//			re = calculateReCutOff(lenght, roughness);
//		}

		return re;
	}


	// GETTERS AND SETTERS ---------------------------------------------------------

	public Amount<Pressure> get_maxDeltaPressure() {
		return _maxDeltaPressure;
	}

	public Double get_machCurrent() {
		return _machCurrent;
	}

	public void set_machCurrent(Double _mach) {
		this._machCurrent = _mach;
	}

	public Double get_machTransonicThreshold() {
		return _machTransonicThreshold;
	}

	public void set_machTransonicThreshold(Double _machTransonicThreshold) {
		this._machTransonicThreshold = _machTransonicThreshold;
	}

	public Amount<VolumetricDensity> get_densityCurrent() {
		return _densityCurrent;
	}

	public void set_rho(Amount<VolumetricDensity> _rho) {
		this._densityCurrent = _rho;
	}

	public Amount<DynamicViscosity> get_mu() {
		return _mu;
	}

	public void set_mu(Amount<DynamicViscosity> _mu) {
		this._mu = _mu;
	}

	public Double[] get_alpha() {
		return _alpha;
	}

	public void set_alpha(Double _alpha[]) {
		this._alpha = _alpha;
	}

	public Amount<Length> get_altitude() {
		return _altitude;
	}

	public void set_altitude(Amount<Length> _altitude) {
		this._altitude = _altitude;
	}

	public Amount<Velocity> get_tas() {
		return _tas;
	}

	public void set_tas(Amount<Velocity> _speed) {
		this._tas = _speed;
	}

	public Double[] get_cL() {
		return _cL;
	}


	public Amount<Pressure> get_staticPressure() {
		return _staticPressure;
	}


	public Amount<Temperature> get_staticTemperature() {
		return _staticTemperature;
	}


	public void set_temperature(Amount<Temperature> _temperature) {
		this._staticTemperature = _temperature;
	}


	public static StdAtmos1976 get_atmosphere(double altitude) {
		return new StdAtmos1976(altitude);
	}

	public void set_density(Amount<VolumetricDensity> _density) {
		this._densityCurrent = _density;
	}

	public void set_pressure(Amount<Pressure> _pressure) {
		this._staticPressure = _pressure;
	}

	public Amount<Pressure> get_maxDynamicPressure() {
		return _maxDynamicPressure;
	}


	public Amount<Angle> get_alphaCurrent() {
		return _alphaCurrent;
	}


	public void set_alphaCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaCurrent = _alphaCurrent;
	}


	public Amount<Velocity> get_eas() {
		return _eas;
	}


	public Double get_pressureCoefficientCurrent() {
		return _pressureCoefficientCurrent;
	}


	public Amount<Temperature> get_stagnationTemperature() {
		return _stagnationTemperature;
	}


	public Amount<Pressure> get_stagnationPressure() {
		return _stagnationPressure;
	}


	public Amount<Velocity> get_cas() {
		return _cas;
	}


	public Amount<Pressure> get_dynamicPressure() {
		return _dynamicPressure;
	}

	public static String getId() {
		return _id;
	}



} // end of class

