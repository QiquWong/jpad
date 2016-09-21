package analyses;

import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.DynamicViscosity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
//WARNING: Density is in g/cm3 ( = 1000 kg/m3)
import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.PressureCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.atmosphere.TemperatureCalc;

/**
 * Set the aircraft current operating conditions
 *   
 * @author Lorenzo Attanasio
 */
public class OperatingConditions implements IOperatingConditions {

	private String _id;
	
	// Flight parameters
	private Double[] _alpha;
	private Amount<Angle> _alphaCurrent;
	private Double _machCurrent;
	private Double _pressureCoefficientCurrent;
	private Amount<Velocity> _tas;
	private Amount<Velocity> _cas;
	private Amount<Velocity> _eas;
	private Amount<Length> _altitude;

	// Return density ratio, pressure ratio, temperature ratio, ecc ...
	private StdAtmos1976 _atmosphere;
	private Amount<VolumetricDensity> _densityCurrent;
	private Amount<Pressure> _staticPressure;
	private Amount<Pressure> _dynamicPressure;
	private Amount<Pressure> _stagnationPressure; 
	private Amount<Pressure> _maxDeltaPressure;
	private Amount<Pressure> _maxDynamicPressure;
	private Amount<DynamicViscosity> _mu;
	private Double _machTransonicThreshold = 0.7;
	private Amount<Temperature> _staticTemperature;
	private Amount<Temperature> _stagnationTemperature;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class OperatingConditionsBuilder {
		
		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Double[] __alpha;
		private Amount<Angle> __alphaCurrent;
		private Double __machCurrent;
		private Amount<Length> __altitude;
		
		public OperatingConditionsBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public OperatingConditionsBuilder alphaArray(Double[] alpha) {
			this.__alpha = alpha;
			return this;
		}
		
		public OperatingConditionsBuilder alphaCurrent(Amount<Angle> alphaCurrent) {
			this.__alphaCurrent = alphaCurrent; 
			return this;
		}
		
		public OperatingConditionsBuilder machCurrent (Double machCurrent) {
			this.__machCurrent = machCurrent;
			return this;
		}
		
		public OperatingConditionsBuilder altitude (Amount<Length> altitude) {
			this.__altitude = altitude;
			return this;
		}
		
		public OperatingConditionsBuilder(String id) {
			this.__id = id;
			initializeDefaultVariables();
		}
		
		private void initializeDefaultVariables() {
			
			this.__alphaCurrent = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
			this.__machCurrent = 0.0;
			this.__altitude = Amount.valueOf(0.0, SI.METER);
			this.__alpha = new Double[] {0.0,2.0,4.0,6.0,8.0,10.0,12.0,14.0,16.0,18.0,20.0,22.0,24.0};
		}
		
		public OperatingConditions build() {
			return new OperatingConditions(this);
		}
		
	}
	
	private OperatingConditions(OperatingConditionsBuilder builder) {
		
		this._id = builder.__id;
		this._alphaCurrent = builder.__alphaCurrent;
		this._machCurrent = builder.__machCurrent;
		this._altitude = builder.__altitude;
		this._alpha = builder.__alpha;
		
		calculate();
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================

	public static OperatingConditions importFromXML(String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading operating conditions data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		Amount<Angle> alphaCurrent = Amount.valueOf(
				Double.valueOf(
						reader.getXMLPropertyByPath(
								"//operating_conditions/alpha_current"
								)
						),
				NonSI.DEGREE_ANGLE)
				.to(SI.RADIAN);
		
		Double machCurrent = Double.valueOf(
				reader.getXMLPropertyByPath(
						"//operating_conditions/mach_current"
						)
				);
		
		Amount<Length> altitude = reader.getXMLAmountLengthByPath("//operating_conditions/altitude");
		
		List<String> alphaArrayList = JPADXmlReader.readArrayFromXML(
				reader.getXMLPropertiesByPath(
						"//operating_conditions/alpha_array"
						).get(0)
				);
		Double[] alphaArray = new Double[alphaArrayList.size()];
		for(int i=0; i<alphaArrayList.size(); i++)
			alphaArray[i] = Double.valueOf(alphaArrayList.get(i));
		
		OperatingConditions theConditions = new OperatingConditionsBuilder(id)
				.alphaCurrent(alphaCurrent)
				.machCurrent(machCurrent)
				.altitude(altitude)
				.alphaArray(alphaArray)
				.build();
				
		return theConditions;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tOperating Conditions\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "' \n")
				.append("\t.....................................\n")
				.append("\tAltitude: " + _altitude + "\n")
				.append("\tMach current: " + _machCurrent + "\n")
				.append("\tAlpha current: " + _alphaCurrent + "\n")
				.append("\tAlpha array: " + Arrays.toString(_alpha) + "\n")
				.append("\t.....................................\n")
				.append("\tCurrent pressure coefficient: " + _pressureCoefficientCurrent + "\n")
				.append("\tCurrent density: " + _densityCurrent + "\n")
				.append("\tCurrent static pressure: " + _staticPressure + "\n")
				.append("\tCurrent dynamic pressure: " + _dynamicPressure + "\n")
				.append("\tCurrent stagnation pressure: " + _stagnationPressure + "\n")
				.append("\tCurrent maximum delta pressure outside-inside: " + _maxDeltaPressure + "\n")
				.append("\tCurrent static temperature: " + _staticTemperature + "\n")
				.append("\tCurrent stagnation temperature: " + _stagnationTemperature + "\n")
				.append("\tCurrent speed (TAS): " + _tas + "\n")
				.append("\tCurrent speed (CAS): " + _cas + "\n")
				.append("\tCurrent speed (EAS): " + _eas + "\n")
				.append("\tCurrent dynamic viscosity: " + _mu + "\n")
				.append("\t.....................................\n")
				;
				
		return sb.toString();
	}
	
	/** 
	 * Evaluate all dependent parameters
	 * @author Lorenzo Attanasio
	 */
	public void calculate() {

		_atmosphere = new StdAtmos1976(_altitude.getEstimatedValue());
		_pressureCoefficientCurrent = PressureCalc.calculatePressureCoefficient(_machCurrent);

		_densityCurrent = Amount.valueOf(_atmosphere.getDensity()*1000, VolumetricDensity.UNIT);

		_staticPressure = Amount.valueOf(_atmosphere.getPressure(), SI.PASCAL);
		_dynamicPressure = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(_machCurrent, _staticPressure.getEstimatedValue())
				, SI.PASCAL);
		_stagnationPressure = _dynamicPressure.times(_pressureCoefficientCurrent).plus(_staticPressure);

		_tas = Amount.valueOf(_machCurrent * _atmosphere.getSpeedOfSound(), SI.METERS_PER_SECOND);
		_cas = Amount.valueOf(
				SpeedCalc.calculateCAS(_stagnationPressure.getEstimatedValue(), _staticPressure.getEstimatedValue())
				, SI.METERS_PER_SECOND);
		_eas = Amount.valueOf(Math.sqrt(_atmosphere.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(_stagnationPressure.getEstimatedValue(), 
						_staticPressure.getEstimatedValue(), _densityCurrent.getEstimatedValue())
						, SI.METERS_PER_SECOND);

		_staticTemperature = Amount.valueOf(_atmosphere.getTemperature(), SI.KELVIN);
		_stagnationTemperature = _staticTemperature
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(_machCurrent));

		// Maximum pressure difference between outside and inside
		_maxDeltaPressure = Amount.valueOf(
				Math.abs(
						_staticPressure.getEstimatedValue() 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		double mu0 = 17.33e-6; // Pa*s, grc.nasa.gov/WWW/BGH/Viscosity.html
		double T0 = 288.166667; // K
		double C = 110.4; // K

		_mu = Amount.valueOf(
				mu0*((T0 + C)
						/ (_atmosphere.getTemperature() + C))
						* Math.pow(
								_atmosphere.getTemperature()/T0, 
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

	public Amount<Pressure> getMaxDeltaPressure() {
		return _maxDeltaPressure;
	}

	public Double getMachCurrent() {
		return _machCurrent;
	}

	public void setMachCurrent(Double _mach) {
		this._machCurrent = _mach;
	}

	public Double getMachTransonicThreshold() {
		return _machTransonicThreshold;
	}

	public void setMachTransonicThreshold(Double _machTransonicThreshold) {
		this._machTransonicThreshold = _machTransonicThreshold;
	}

	public Amount<VolumetricDensity> getDensityCurrent() {
		return _densityCurrent;
	}

	public void setRho(Amount<VolumetricDensity> _rho) {
		this._densityCurrent = _rho;
	}

	public Amount<DynamicViscosity> getMu() {
		return _mu;
	}

	public void setMu(Amount<DynamicViscosity> _mu) {
		this._mu = _mu;
	}

	public Double[] getAlpha() {
		return _alpha;
	}

	public void setAlpha(Double _alpha[]) {
		this._alpha = _alpha;
	}

	public Amount<Length> getAltitude() {
		return _altitude;
	}

	public void setAltitude(Amount<Length> _altitude) {
		this._altitude = _altitude;
	}

	public Amount<Velocity> getTAS() {
		return _tas;
	}

	public void setTAS(Amount<Velocity> _speed) {
		this._tas = _speed;
	}

	public Amount<Pressure> getStaticPressure() {
		return _staticPressure;
	}


	public Amount<Temperature> getStaticTemperature() {
		return _staticTemperature;
	}


	public void setTemperature(Amount<Temperature> _temperature) {
		this._staticTemperature = _temperature;
	}


	public static StdAtmos1976 getAtmosphere(double altitude) {
		return new StdAtmos1976(altitude);
	}

	public void setDensity(Amount<VolumetricDensity> _density) {
		this._densityCurrent = _density;
	}

	public void setPressure(Amount<Pressure> _pressure) {
		this._staticPressure = _pressure;
	}

	public Amount<Pressure> getMaxDynamicPressure() {
		return _maxDynamicPressure;
	}

	public Amount<Angle> getAlphaCurrent() {
		return _alphaCurrent;
	}

	public void setAlphaCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaCurrent = _alphaCurrent;
	}

	public Amount<Velocity> getEAS() {
		return _eas;
	}

	public Double getPressureCoefficientCurrent() {
		return _pressureCoefficientCurrent;
	}

	public Amount<Temperature> getStagnationTemperature() {
		return _stagnationTemperature;
	}

	public Amount<Pressure> getStagnationPressure() {
		return _stagnationPressure;
	}

	public Amount<Velocity> getCAS() {
		return _cas;
	}

	public Amount<Pressure> getDynamicPressure() {
		return _dynamicPressure;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}
	
} // end of class