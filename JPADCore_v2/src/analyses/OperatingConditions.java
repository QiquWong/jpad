package analyses;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
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
	
	// Global data
	private Double _machTransonicThreshold = 0.7;
	
	// Climb data
	private Amount<Angle> _alphaCurrentClimb;
	private Double _machClimb;
	private Amount<Length> _altitudeClimb;
	
	// Cruise data
	private Amount<Angle> _alphaCurrentCruise;
	private Amount<Length> _altitudeCruise;
	private Double _machCruise;
	private Double _throttleCruise;

	// Take-off data
	private Amount<Angle> _alphaCurrentTakeOff;
	private Amount<Length> _altitudeTakeOff;
	private Double _machTakeOff;
	private Double _throttleTakeOff;
	private MyInterpolatingFunction _throttleGroundIdleTakeOff;
	private List<Amount<Angle>> _flapDeflectionTakeOff;
	private List<Amount<Angle>> _slatDeflectionTakeOff;

	// Landing data
	private Amount<Angle> _alphaCurrentLanding;
	private Amount<Length> _altitudeLanding;
	private Double _machLanding;
	private MyInterpolatingFunction _throttleGroundIdleLanding;
	private List<Amount<Angle>> _flapDeflectionLanding;
	private List<Amount<Angle>> _slatDeflectionLanding;
	
	// TO BE CALCULATED:
	
	// CRUISE
	private StdAtmos1976 _atmosphereCruise; 
	private Double _pressureCoefficientCruise;
	private Amount<Velocity> _tasCruise;
	private Amount<Velocity> _casCruise;
	private Amount<Velocity> _easCruise;
	private Amount<VolumetricDensity> _densityCruise;
	private Amount<Pressure> _staticPressureCruise;
	private Amount<Pressure> _dynamicPressureCruise;
	private Amount<Pressure> _stagnationPressureCruise; 
	private Amount<Pressure> _maxDeltaPressureCruise;
	private Amount<Pressure> _maxDynamicPressureCruise;
	private Amount<DynamicViscosity> _muCruise;
	private Amount<Temperature> _staticTemperatureCruise;
	private Amount<Temperature> _stagnationTemperatureCruise;

	// TAKE-OFF
	private StdAtmos1976 _atmosphereTakeOff;
	private Double _pressureCoefficientTakeOff;
	private Amount<Velocity> _tasTakeOff;
	private Amount<Velocity> _casTakeOff;
	private Amount<Velocity> _easTakeOff;
	private Amount<VolumetricDensity> _densityTakeOff;
	private Amount<Pressure> _staticPressureTakeOff;
	private Amount<Pressure> _dynamicPressureTakeOff;
	private Amount<Pressure> _stagnationPressureTakeOff; 
	private Amount<Pressure> _maxDeltaPressureTakeOff;
	private Amount<Pressure> _maxDynamicPressureTakeOff;
	private Amount<DynamicViscosity> _muTakeOff;
	private Amount<Temperature> _staticTemperatureTakeOff;
	private Amount<Temperature> _stagnationTemperatureTakeOff;

	// LANDING
	private StdAtmos1976 _atmosphereLanding;
	private Double _pressureCoefficientLanding;
	private Amount<Velocity> _tasLanding;
	private Amount<Velocity> _casLanding;
	private Amount<Velocity> _easLanding;
	private Amount<VolumetricDensity> _densityLanding;
	private Amount<Pressure> _staticPressureLanding;
	private Amount<Pressure> _dynamicPressureLanding;
	private Amount<Pressure> _stagnationPressureLanding; 
	private Amount<Pressure> _maxDeltaPressureLanding;
	private Amount<Pressure> _maxDynamicPressureLanding;
	private Amount<DynamicViscosity> _muLanding;
	private Amount<Temperature> _staticTemperatureLanding;
	private Amount<Temperature> _stagnationTemperatureLanding;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class OperatingConditionsBuilder {
		
		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		
		// Climb data
		private Amount<Angle> __alphaCurrentClimb;
		private Double __machClimb;
		private Amount<Length> __altitudeClimb;
		
		// Cruise data
		private Amount<Angle> __alphaCurrentCruise;
		private Amount<Length> __altitudeCruise;
		private Double __machCruise;
		private Double __throttleCruise;

		// Take-off data
		private Amount<Angle> __alphaCurrentTakeOff;
		private Amount<Length> __altitudeTakeOff;
		private Double __machTakeOff;
		private Double __throttleTakeOff;
		private MyInterpolatingFunction __throttleGroundIdleTakeOff;
		private List<Amount<Angle>> __flapDeflectionTakeOff;
		private List<Amount<Angle>> __slatDeflectionTakeOff;

		// Landing data
		private Amount<Angle> __alphaCurrentLanding;
		private Amount<Length> __altitudeLanding;
		private Double __machLanding;
		private MyInterpolatingFunction __throttleGroundIdleLanding;
		private List<Amount<Angle>> __flapDeflectionLanding;
		private List<Amount<Angle>> __slatDeflectionLanding;
		
		public OperatingConditionsBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public OperatingConditionsBuilder alphaCurrentClimb(Amount<Angle> alphaCurrentClimb) {
			this.__alphaCurrentClimb = alphaCurrentClimb; 
			return this;
		}
		
		public OperatingConditionsBuilder alphaCurrentCruise(Amount<Angle> alphaCurrentCruise) {
			this.__alphaCurrentCruise = alphaCurrentCruise; 
			return this;
		}
		
		public OperatingConditionsBuilder alphaCurrentTakeOff(Amount<Angle> alphaCurrentTakeOff) {
			this.__alphaCurrentTakeOff = alphaCurrentTakeOff; 
			return this;
		}
		
		public OperatingConditionsBuilder alphaCurrentLanding(Amount<Angle> alphaCurrentLanding) {
			this.__alphaCurrentLanding = alphaCurrentLanding; 
			return this;
		}
		
		public OperatingConditionsBuilder machCruise (Double machCruise) {
			this.__machCruise = machCruise;
			return this;
		}
		
		public OperatingConditionsBuilder machClimb (Double machClimb) {
			this.__machClimb = machClimb;
			return this;
		}
		
		public OperatingConditionsBuilder altitudeClimb (Amount<Length> altitudeClimb) {
			this.__altitudeClimb = altitudeClimb;
			return this;
		}
		
		public OperatingConditionsBuilder altitudeCruise (Amount<Length> altitudeCruise) {
			this.__altitudeCruise = altitudeCruise;
			return this;
		}
		
		public OperatingConditionsBuilder throttleCruise (Double throttleCruise) {
			this.__throttleCruise = throttleCruise;
			return this;
		}
		
		public OperatingConditionsBuilder machTakeOff (Double machTakeOff) {
			this.__machTakeOff = machTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder altitudeTakeOff (Amount<Length> altitudeTakeOff) {
			this.__altitudeTakeOff = altitudeTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder throttleTakeOff (Double throttleTakeOff) {
			this.__throttleTakeOff = throttleTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder throttleGroundIdleTakeOff (MyInterpolatingFunction throttleGroundIdleTakeOff) {
			this.__throttleGroundIdleTakeOff = throttleGroundIdleTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder flapDeflectionTakeOff (List<Amount<Angle>> deltaFlapTakeOff) {
			this.__flapDeflectionTakeOff = deltaFlapTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder slatDeflectionTakeOff (List<Amount<Angle>> deltaSlatTakeOff) {
			this.__slatDeflectionTakeOff = deltaSlatTakeOff;
			return this;
		}
		
		public OperatingConditionsBuilder machLanding (Double machLanding) {
			this.__machLanding = machLanding;
			return this;
		}
		
		public OperatingConditionsBuilder altitudeLanding (Amount<Length> altitudeLanding) {
			this.__altitudeLanding = altitudeLanding;
			return this;
		}
		
		public OperatingConditionsBuilder throttleGroundIdleLanding (MyInterpolatingFunction throttleGroundIdleLanding) {
			this.__throttleGroundIdleLanding = throttleGroundIdleLanding;
			return this;
		}
		
		public OperatingConditionsBuilder flapDeflectionLanding (List<Amount<Angle>> deltaFlapLanding) {
			this.__flapDeflectionLanding = deltaFlapLanding;
			return this;
		}
		
		public OperatingConditionsBuilder slatDeflectionLanding (List<Amount<Angle>> deltaSlatLanding) {
			this.__slatDeflectionLanding = deltaSlatLanding;
			return this;
		}
		
		public OperatingConditionsBuilder(String id) {
			this.__id = id;
			initializeDefaultVariables();
		}
		
		private void initializeDefaultVariables() {
			
			// DEFAULT DATA ATR-72	
			this.__alphaCurrentClimb = Amount.valueOf(15.0, NonSI.DEGREE_ANGLE);
			this.__machClimb = 0.3;
			this.__altitudeClimb = Amount.valueOf(6000.0, SI.METER);
			
			this.__alphaCurrentCruise = Amount.valueOf(1.0, NonSI.DEGREE_ANGLE);
			this.__machCruise = 0.6;
			this.__altitudeCruise = Amount.valueOf(6000.0, SI.METER);
			this.__throttleCruise = 1.0;
			
			this.__alphaCurrentTakeOff = Amount.valueOf(10.0, NonSI.DEGREE_ANGLE);
			this.__machTakeOff = 0.2;
			this.__altitudeTakeOff = Amount.valueOf(0.0, SI.METER);
			this.__throttleGroundIdleTakeOff = null; 
			this.__flapDeflectionTakeOff = new ArrayList<>();
			this.__slatDeflectionTakeOff = new ArrayList<>();
			
			this.__alphaCurrentLanding = Amount.valueOf(8.0, NonSI.DEGREE_ANGLE);
			this.__machLanding = 0.2;
			this.__altitudeLanding = Amount.valueOf(0.0, SI.METER);
			this.__throttleGroundIdleLanding = null;
			this.__flapDeflectionLanding = new ArrayList<>();
			this.__slatDeflectionLanding = new ArrayList<>();
			
		}
		
		public OperatingConditions build() {
			return new OperatingConditions(this);
		}
		
	}
	
	private OperatingConditions(OperatingConditionsBuilder builder) {
		
		this._id = builder.__id;
		
		this._alphaCurrentClimb = builder.__alphaCurrentClimb;
		this._alphaCurrentCruise = builder.__alphaCurrentCruise;
		this._alphaCurrentTakeOff = builder.__alphaCurrentTakeOff;
		this._alphaCurrentLanding = builder.__alphaCurrentLanding;
		
		this._machClimb = builder.__machClimb;
		this._altitudeClimb = builder.__altitudeClimb;
		
		this._machCruise = builder.__machCruise;
		this._altitudeCruise = builder.__altitudeCruise;
		this._throttleCruise = builder.__throttleCruise;
		
		this._machTakeOff = builder.__machTakeOff;
		this._altitudeTakeOff = builder.__altitudeTakeOff;
		this._throttleTakeOff = builder.__throttleTakeOff;
		this._throttleGroundIdleTakeOff = builder.__throttleGroundIdleTakeOff;
		this._flapDeflectionTakeOff = builder.__flapDeflectionTakeOff;
		this._slatDeflectionTakeOff = builder.__slatDeflectionTakeOff;
		
		this._machLanding = builder.__machLanding;
		this._altitudeLanding = builder.__altitudeLanding;
		this._throttleGroundIdleLanding = builder.__throttleGroundIdleLanding;
		this._flapDeflectionLanding = builder.__flapDeflectionLanding;
		this._slatDeflectionLanding = builder.__slatDeflectionLanding;
		
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
		
		
		///////////////////////////////////////////////////////////////
		// CLIMB DATA:
		Double machClimb = null;
		Amount<Length> altitudeClimb = null;
		Amount<Angle> alphaCurrentClimb = null;
		//.............................................................
		String alphaCurrentClimbProperty = reader.getXMLPropertyByPath("//climb/alpha_current");
		if(alphaCurrentClimbProperty != null)
			alphaCurrentClimb = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//climb/alpha_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machClimbProperty = reader.getXMLPropertyByPath("//climb/mach");
		if(machClimbProperty != null)
			machClimb = Double.valueOf(reader.getXMLPropertyByPath("//climb/mach"));
		//.............................................................
		String altitudeClimbProperty = reader.getXMLPropertyByPath("//climb/altitude");
		if(altitudeClimbProperty != null)
			altitudeClimb = reader.getXMLAmountLengthByPath("//climb/altitude");
		
		///////////////////////////////////////////////////////////////
		// CRUISE DATA:
		Double machCruise = null;
		Amount<Length> altitudeCruise = null;
		Double throttleCruise = null;
		Amount<Angle> alphaCurrentCruise = null;
		//.............................................................
		String alphaCurrentCruiseProperty = reader.getXMLPropertyByPath("//cruise/alpha_current");
		if(alphaCurrentCruiseProperty != null)
			alphaCurrentCruise = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//cruise/alpha_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machCruiseProperty = reader.getXMLPropertyByPath("//cruise/mach");
		if(machCruiseProperty != null)
			machCruise = Double.valueOf(reader.getXMLPropertyByPath("//cruise/mach"));
		//.............................................................
		String altitudeCruiseProperty = reader.getXMLPropertyByPath("//cruise/altitude");
		if(altitudeCruiseProperty != null)
			altitudeCruise = reader.getXMLAmountLengthByPath("//cruise/altitude").to(SI.METER);
		//.............................................................
		String throttleCruiseProperty = reader.getXMLPropertyByPath("//cruise/throttle");
		if(throttleCruiseProperty != null)
			throttleCruise = Double.valueOf(reader.getXMLPropertyByPath("//cruise/throttle"));
		
		///////////////////////////////////////////////////////////////
		// TAKE-OFF DATA:
		Double machTakeOff = 0.2;
		Amount<Length> altitudeTakeOff = Amount.valueOf(0.0, SI.METER);
		Double throttleTakeOff = 1.0;
		List<Double> throttleGroundIdleTakeOffFunction = new ArrayList<>();
		List<Amount<Velocity>> speedThrottleGroundIdleTakeOffFunction = new ArrayList<>();
		List<Amount<Angle>> deltaFlapTakeOff = new ArrayList<>();
		List<Amount<Angle>> deltaSlatTakeOff = new ArrayList<>();
		Amount<Angle> alphaCurrentTakeOff = null;
		//.............................................................
		String alphaCurrentTakeOffProperty = reader.getXMLPropertyByPath("//take_off/alpha_current");
		if(alphaCurrentTakeOffProperty != null)
			alphaCurrentTakeOff = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//take_off/alpha_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................		
		String machTakeOffProperty = reader.getXMLPropertyByPath("//take_off/mach");
		if(machTakeOffProperty != null)
			machTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//take_off/mach"));
		//.............................................................
		String altitudeTakeOffProperty = reader.getXMLPropertyByPath("//take_off/altitude");
		if(altitudeTakeOffProperty != null)
			altitudeTakeOff = reader.getXMLAmountLengthByPath("//take_off/altitude").to(SI.METER);
		//.............................................................
		String throttleTakeOffProperty = reader.getXMLPropertyByPath("//take_off/throttle");
		if(throttleTakeOffProperty != null)
			throttleTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//take_off/throttle"));
		//.............................................................
		String throttleGroundIdleTakeOffFunctionProperty = reader.getXMLPropertyByPath("//landing/throttle_ground_idle/throttle");
		if(throttleGroundIdleTakeOffFunctionProperty != null)
			throttleGroundIdleTakeOffFunction = reader.readArrayDoubleFromXML("//landing/throttle_ground_idle/throttle"); 
		String speedThrottleGroundIdleTakeOffFunctionProperty = reader.getXMLPropertyByPath("//landing/throttle_ground_idle/speed");
		if(speedThrottleGroundIdleTakeOffFunctionProperty != null) 
			speedThrottleGroundIdleTakeOffFunction = reader.readArrayofAmountFromXML("//landing/throttle_ground_idle/speed");
		
		List<Amount<Velocity>> speedMeterPerSecondThrottleGroundIdleTakeOffFunction = 
				speedThrottleGroundIdleTakeOffFunction.stream()
				.map(s -> s.to(SI.METERS_PER_SECOND))
				.collect(Collectors.toList());
		
		if(throttleGroundIdleTakeOffFunction.size() > 1)
			if(throttleGroundIdleTakeOffFunction.size() != throttleGroundIdleTakeOffFunction.size())
			{
				System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(throttleGroundIdleTakeOffFunction.size() == 1) {
			throttleGroundIdleTakeOffFunction.add(throttleGroundIdleTakeOffFunction.get(0));
			speedMeterPerSecondThrottleGroundIdleTakeOffFunction.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			speedMeterPerSecondThrottleGroundIdleTakeOffFunction.add(Amount.valueOf(1.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction throttleGroundIdleTakeOffInterpolatingFunction = new MyInterpolatingFunction();
		throttleGroundIdleTakeOffInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(speedMeterPerSecondThrottleGroundIdleTakeOffFunction),
				MyArrayUtils.convertToDoublePrimitive(throttleGroundIdleTakeOffFunction)
				);
		//.............................................................
		List<String> deltaFlapTakeOffCheck = reader.getXMLPropertiesByPath(
				"//take_off/delta_flap"
				);
		if(!deltaFlapTakeOffCheck.isEmpty()) {
			List<String> deltaFlapTakeOffProperty = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath(
							"//take_off/delta_flap"
							).get(0)
					);
			for(int i=0; i<deltaFlapTakeOffProperty.size(); i++)
				deltaFlapTakeOff.add(Amount.valueOf(Double.valueOf(deltaFlapTakeOffProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaFlapTakeOff.clear();
		//.............................................................
		List<String> deltaSlatTakeOffCheck = reader.getXMLPropertiesByPath(
				"//take_off/delta_slat"
				);
		if(!deltaSlatTakeOffCheck.isEmpty()) {
			List<String> deltaSlatTakeOffProperty = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath(
							"//take_off/delta_slat"
							).get(0)
					);
			for(int i=0; i<deltaSlatTakeOffProperty.size(); i++)
				deltaSlatTakeOff.add(Amount.valueOf(Double.valueOf(deltaSlatTakeOffProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaSlatTakeOff.clear();
		
		///////////////////////////////////////////////////////////////
		// LANDING DATA:
		Double machLanding = 0.2;
		Amount<Length> altitudeLanding = Amount.valueOf(0.0, SI.METER);
		List<Double> throttleGroundIdleLandingFunction = new ArrayList<>();
		List<Amount<Velocity>> speedThrottleGroundIdleLanidngFunction = new ArrayList<>();
		List<Amount<Angle>> deltaFlapLanding = new ArrayList<>();
		List<Amount<Angle>> deltaSlatLanding = new ArrayList<>();
		Amount<Angle> alphaCurrentLanding = null;
		//.............................................................
		String alphaCurrentLandingProperty = reader.getXMLPropertyByPath("//landing/alpha_current");
		if(alphaCurrentLandingProperty != null)
			alphaCurrentLanding = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//landing/alpha_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................		
		String machLandingProperty = reader.getXMLPropertyByPath("//landing/mach");
		if(machLandingProperty != null)
			machLanding = Double.valueOf(reader.getXMLPropertyByPath("//landing/mach"));
		//.............................................................
		String altitudeLandingProperty = reader.getXMLPropertyByPath("//landing/altitude");
		if(altitudeLandingProperty != null)
			altitudeLanding = reader.getXMLAmountLengthByPath("//landing/altitude").to(SI.METER);
		//.............................................................
		String throttleGroundIdleLandingFunctionProperty = reader.getXMLPropertyByPath("//landing/throttle_ground_idle/throttle");
		if(throttleGroundIdleLandingFunctionProperty != null)
			throttleGroundIdleLandingFunction = reader.readArrayDoubleFromXML("//landing/throttle_ground_idle/throttle"); 
		String speedThrottleGroundIdleLanidngFunctionProperty = reader.getXMLPropertyByPath("//landing/throttle_ground_idle/speed");
		if(speedThrottleGroundIdleLanidngFunctionProperty != null) {
			speedThrottleGroundIdleLanidngFunction = reader.readArrayofAmountFromXML("//landing/throttle_ground_idle/speed");
		}
		
		List<Amount<Velocity>> speedMeterPerSecondThrottleGroundIdleLanidngFunction = 
				speedThrottleGroundIdleLanidngFunction.stream()
				.map(s -> s.to(SI.METERS_PER_SECOND))
				.collect(Collectors.toList());
		
		if(throttleGroundIdleLandingFunction.size() > 1)
			if(throttleGroundIdleLandingFunction.size() != throttleGroundIdleLandingFunction.size())
			{
				System.err.println("SFC ARRAY AND THE RELATED THROTTLE ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(throttleGroundIdleLandingFunction.size() == 1) {
			throttleGroundIdleLandingFunction.add(throttleGroundIdleLandingFunction.get(0));
			speedMeterPerSecondThrottleGroundIdleLanidngFunction.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			speedMeterPerSecondThrottleGroundIdleLanidngFunction.add(Amount.valueOf(1.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction throttleGroundIdleLandingInterpolatingFunction = new MyInterpolatingFunction();
		throttleGroundIdleLandingInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(speedMeterPerSecondThrottleGroundIdleLanidngFunction),
				MyArrayUtils.convertToDoublePrimitive(throttleGroundIdleLandingFunction)
				);
		//.............................................................
		List<String> deltaFlapLandingCheck = reader.getXMLPropertiesByPath(
				"//landing/delta_flap"
				);
		if(!deltaFlapLandingCheck.isEmpty()) {
			List<String> deltaFlapLandingProperty = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath(
							"//landing/delta_flap"
							).get(0)
					);
			for(int i=0; i<deltaFlapLandingProperty.size(); i++)
				deltaFlapLanding.add(Amount.valueOf(Double.valueOf(deltaFlapLandingProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaFlapLanding.clear();
		//.............................................................
		List<String> deltaSlatLandingCheck = reader.getXMLPropertiesByPath(
				"//landing/delta_slat"
				);
		if(!deltaSlatLandingCheck.isEmpty()) {
			List<String> deltaSlatLandingProperty = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath(
							"//landing/delta_slat"
							).get(0)
					);
			for(int i=0; i<deltaSlatLandingProperty.size(); i++)
				deltaSlatLanding.add(Amount.valueOf(Double.valueOf(deltaSlatLandingProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaSlatLanding.clear();

		///////////////////////////////////////////////////////////////
		OperatingConditions theConditions = new OperatingConditionsBuilder(id)
				.alphaCurrentClimb(alphaCurrentClimb)
				.alphaCurrentCruise(alphaCurrentCruise)
				.alphaCurrentTakeOff(alphaCurrentTakeOff)
				.alphaCurrentLanding(alphaCurrentLanding)
				.machClimb(machClimb)
				.altitudeClimb(altitudeClimb)
				.machCruise(machCruise)
				.altitudeCruise(altitudeCruise)
				.throttleCruise(throttleCruise)
				.machTakeOff(machTakeOff)
				.altitudeTakeOff(altitudeTakeOff)
				.throttleTakeOff(throttleTakeOff)
				.throttleGroundIdleTakeOff(throttleGroundIdleTakeOffInterpolatingFunction)
				.flapDeflectionTakeOff(deltaFlapTakeOff)
				.slatDeflectionTakeOff(deltaSlatTakeOff)
				.machLanding(machLanding)
				.altitudeLanding(altitudeLanding)
				.throttleGroundIdleLanding(throttleGroundIdleLandingInterpolatingFunction)
				.flapDeflectionLanding(deltaFlapLanding)
				.slatDeflectionLanding(deltaSlatLanding)
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
				.append("\tAlpha current Climb: " + _alphaCurrentClimb.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Climb: " + _machClimb + "\n")
				.append("\tAltitude Climb: " + _altitudeClimb.to(NonSI.FOOT) + "\n")
				.append("\t.....................................\n")
				.append("\tAlpha current Cruise: " + _alphaCurrentCruise.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Cruise: " + _machCruise + "\n")
				.append("\tAltitude Cruise: " + _altitudeCruise.to(NonSI.FOOT) + "\n")
				.append("\tThrottle Cruise (phi): " + _throttleCruise + "\n")
				.append("\t.....................................\n")
				.append("\tAlpha current Take-off: " + _alphaCurrentTakeOff.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Take-off: " + _machTakeOff + "\n")
				.append("\tAltitude Take-off: " + _altitudeTakeOff.to(NonSI.FOOT) + "\n")
				.append("\tThrottle Take-off (phi): " + _throttleTakeOff + "\n");
		if(!_flapDeflectionTakeOff.isEmpty())
				sb.append("\tFlap deflections Take-off: " + _flapDeflectionTakeOff + "\n");
		if(!_slatDeflectionTakeOff.isEmpty())
				sb.append("\tSlat deflections Take-off: " + _slatDeflectionTakeOff + "\n");
				sb.append("\t.....................................\n")
				.append("\tAlpha current Landing: " + _alphaCurrentLanding.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Landing: " + _machLanding + "\n")
				.append("\tAltitude Landing: " + _altitudeLanding.to(NonSI.FOOT) + "\n");
		if(!_flapDeflectionLanding.isEmpty())
				sb.append("\tFlap deflections Landing: " + _flapDeflectionLanding + "\n");
		if(!_slatDeflectionLanding.isEmpty())
				sb.append("\tSlat deflections Landing: " + _slatDeflectionLanding + "\n");
		
				sb.append("\n\t.....................................\n")
				.append("\tCRUISE\n")
				.append("\tCruise pressure coefficient: " + _pressureCoefficientCruise + "\n")
				.append("\tCruise density: " + _densityCruise + "\n")
				.append("\tCruise static pressure: " + _staticPressureCruise + "\n")
				.append("\tCruise dynamic pressure: " + _dynamicPressureCruise + "\n")
				.append("\tCruise stagnation pressure: " + _stagnationPressureCruise + "\n")
				.append("\tCruise maximum delta pressure outside-inside: " + _maxDeltaPressureCruise + "\n")
				.append("\tCruise static temperature: " + _staticTemperatureCruise + "\n")
				.append("\tCruise stagnation temperature: " + _stagnationTemperatureCruise + "\n")
				.append("\tCruise speed (TAS): " + _tasCruise + "\n")
				.append("\tCruise speed (CAS): " + _casCruise + "\n")
				.append("\tCruise speed (EAS): " + _easCruise + "\n")
				.append("\tCruise dynamic viscosity: " + _muCruise + "\n")
				.append("\t.....................................\n")
				.append("\tTAKE-OFF\n")
				.append("\tTake-off pressure coefficient: " + _pressureCoefficientTakeOff + "\n")
				.append("\tTake-off density: " + _densityTakeOff + "\n")
				.append("\tTake-off static pressure: " + _staticPressureTakeOff + "\n")
				.append("\tTake-off dynamic pressure: " + _dynamicPressureTakeOff + "\n")
				.append("\tTake-off stagnation pressure: " + _stagnationPressureTakeOff + "\n")
				.append("\tTake-off maximum delta pressure outside-inside: " + _maxDeltaPressureTakeOff + "\n")
				.append("\tTake-off static temperature: " + _staticTemperatureTakeOff + "\n")
				.append("\tTake-off stagnation temperature: " + _stagnationTemperatureTakeOff + "\n")
				.append("\tTake-off speed (TAS): " + _tasTakeOff + "\n")
				.append("\tTake-off speed (CAS): " + _casTakeOff + "\n")
				.append("\tTake-off speed (EAS): " + _easTakeOff + "\n")
				.append("\tTake-off dynamic viscosity: " + _muTakeOff + "\n")
				.append("\t.....................................\n")
				.append("\tLANDING\n")
				.append("\tLanding pressure coefficient: " + _pressureCoefficientLanding + "\n")
				.append("\tLanding density: " + _densityLanding + "\n")
				.append("\tLanding static pressure: " + _staticPressureLanding + "\n")
				.append("\tLanding dynamic pressure: " + _dynamicPressureLanding + "\n")
				.append("\tLanding stagnation pressure: " + _stagnationPressureLanding + "\n")
				.append("\tLanding maximum delta pressure outside-inside: " + _maxDeltaPressureLanding + "\n")
				.append("\tLanding static temperature: " + _staticTemperatureLanding + "\n")
				.append("\tLanding stagnation temperature: " + _stagnationTemperatureLanding + "\n")
				.append("\tLanding speed (TAS): " + _tasLanding + "\n")
				.append("\tLanding speed (CAS): " + _casLanding + "\n")
				.append("\tLanding speed (EAS): " + _easLanding + "\n")
				.append("\tLanding dynamic viscosity: " + _muLanding + "\n")
				.append("\t.....................................\n")
				;
				
		return sb.toString();
	}
	
	/** 
	 * Evaluate all dependent parameters
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public void calculate() {

		// CRUISE
		_atmosphereCruise = new StdAtmos1976(_altitudeCruise.getEstimatedValue());
		_pressureCoefficientCruise = PressureCalc.calculatePressureCoefficient(_machCruise);
		_densityCruise = Amount.valueOf(_atmosphereCruise.getDensity()*1000, VolumetricDensity.UNIT);
		_staticPressureCruise = Amount.valueOf(_atmosphereCruise.getPressure(), SI.PASCAL);
		_dynamicPressureCruise = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(_machCruise, _staticPressureCruise.getEstimatedValue())
				, SI.PASCAL);
		_stagnationPressureCruise = _dynamicPressureCruise.times(_pressureCoefficientCruise).plus(_staticPressureCruise);
		_tasCruise = Amount.valueOf(_machCruise * _atmosphereCruise.getSpeedOfSound(), SI.METERS_PER_SECOND);
		_casCruise = Amount.valueOf(
				SpeedCalc.calculateCAS(_stagnationPressureCruise.getEstimatedValue(), _staticPressureCruise.getEstimatedValue())
				, SI.METERS_PER_SECOND);
		_easCruise = Amount.valueOf(Math.sqrt(_atmosphereCruise.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(_stagnationPressureCruise.getEstimatedValue(), 
						_staticPressureCruise.getEstimatedValue(), _densityCruise.getEstimatedValue())
						, SI.METERS_PER_SECOND);
		_staticTemperatureCruise = Amount.valueOf(_atmosphereCruise.getTemperature(), SI.KELVIN);
		_stagnationTemperatureCruise = _staticTemperatureCruise
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(_machCruise));

		// Maximum pressure difference between outside and inside
		_maxDeltaPressureCruise = Amount.valueOf(
				Math.abs(
						_staticPressureCruise.getEstimatedValue() 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		double mu0 = 17.33e-6; // Pa*s, grc.nasa.gov/WWW/BGH/Viscosity.html
		double T0 = 288.166667; // K
		double C = 110.4; // K

		_muCruise = Amount.valueOf(
				mu0*((T0 + C)
						/ (_atmosphereCruise.getTemperature() + C))
						* Math.pow(
								_atmosphereCruise.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);

		// TAKE-OFF
		_atmosphereTakeOff = new StdAtmos1976(_altitudeTakeOff.getEstimatedValue());
		_pressureCoefficientTakeOff = PressureCalc.calculatePressureCoefficient(_machTakeOff);
		_densityTakeOff = Amount.valueOf(_atmosphereTakeOff.getDensity()*1000, VolumetricDensity.UNIT);
		_staticPressureTakeOff = Amount.valueOf(_atmosphereTakeOff.getPressure(), SI.PASCAL);
		_dynamicPressureTakeOff = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(_machTakeOff, _staticPressureTakeOff.getEstimatedValue())
				, SI.PASCAL);
		_stagnationPressureTakeOff = _dynamicPressureTakeOff.times(_pressureCoefficientTakeOff).plus(_staticPressureTakeOff);
		_tasTakeOff = Amount.valueOf(_machTakeOff * _atmosphereTakeOff.getSpeedOfSound(), SI.METERS_PER_SECOND);
		_casTakeOff = Amount.valueOf(
				SpeedCalc.calculateCAS(_stagnationPressureTakeOff.getEstimatedValue(), _staticPressureTakeOff.getEstimatedValue())
				, SI.METERS_PER_SECOND);
		_easTakeOff = Amount.valueOf(Math.sqrt(_atmosphereTakeOff.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(_stagnationPressureTakeOff.getEstimatedValue(), 
						_staticPressureTakeOff.getEstimatedValue(), _densityTakeOff.getEstimatedValue())
						, SI.METERS_PER_SECOND);
		_staticTemperatureTakeOff = Amount.valueOf(_atmosphereTakeOff.getTemperature(), SI.KELVIN);
		_stagnationTemperatureTakeOff = _staticTemperatureTakeOff
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(_machTakeOff));

		// Maximum pressure difference between outside and inside
		_maxDeltaPressureTakeOff = Amount.valueOf(
				Math.abs(
						_staticPressureTakeOff.getEstimatedValue() 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		_muTakeOff = Amount.valueOf(
				mu0*((T0 + C)
						/ (_atmosphereTakeOff.getTemperature() + C))
						* Math.pow(
								_atmosphereTakeOff.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);
		
		// LANDING
		_atmosphereLanding = new StdAtmos1976(_altitudeLanding.getEstimatedValue());
		_pressureCoefficientLanding = PressureCalc.calculatePressureCoefficient(_machLanding);
		_densityLanding = Amount.valueOf(_atmosphereLanding.getDensity()*1000, VolumetricDensity.UNIT);
		_staticPressureLanding = Amount.valueOf(_atmosphereLanding.getPressure(), SI.PASCAL);
		_dynamicPressureLanding = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(_machLanding, _staticPressureLanding.getEstimatedValue())
				, SI.PASCAL);
		_stagnationPressureLanding = _dynamicPressureLanding.times(_pressureCoefficientLanding).plus(_staticPressureLanding);
		_tasLanding = Amount.valueOf(_machLanding * _atmosphereLanding.getSpeedOfSound(), SI.METERS_PER_SECOND);
		_casLanding = Amount.valueOf(
				SpeedCalc.calculateCAS(_stagnationPressureLanding.getEstimatedValue(), _staticPressureLanding.getEstimatedValue())
				, SI.METERS_PER_SECOND);
		_easLanding = Amount.valueOf(Math.sqrt(_atmosphereLanding.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(_stagnationPressureLanding.getEstimatedValue(), 
						_staticPressureLanding.getEstimatedValue(), _densityLanding.getEstimatedValue())
						, SI.METERS_PER_SECOND);
		_staticTemperatureLanding = Amount.valueOf(_atmosphereLanding.getTemperature(), SI.KELVIN);
		_stagnationTemperatureLanding = _staticTemperatureLanding
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(_machLanding));

		// Maximum pressure difference between outside and inside
		_maxDeltaPressureLanding = Amount.valueOf(
				Math.abs(
						_staticPressureLanding.getEstimatedValue() 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		_muLanding = Amount.valueOf(
				mu0*((T0 + C)
						/ (_atmosphereLanding.getTemperature() + C))
						* Math.pow(
								_atmosphereLanding.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);
		
	}

	// GETTERS AND SETTERS ---------------------------------------------------------

	public Double getMachCruise() {
		return _machCruise;
	}

	public void setMachCurrent(Double _mach) {
		this._machCruise = _mach;
	}

	public Double getMachTransonicThreshold() {
		return _machTransonicThreshold;
	}

	public void setMachTransonicThreshold(Double _machTransonicThreshold) {
		this._machTransonicThreshold = _machTransonicThreshold;
	}

	public Amount<Length> getAltitudeCruise() {
		return _altitudeCruise;
	}

	public void setAltitude(Amount<Length> _altitude) {
		this._altitudeCruise = _altitude;
	}

	public static StdAtmos1976 getAtmosphere(double altitude) {
		return new StdAtmos1976(altitude);
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	/**
	 * @return the _machClimb
	 */
	public Double getMachClimb() {
		return _machClimb;
	}

	/**
	 * @param _machClimb the _machClimb to set
	 */
	public void setMachClimb(Double _machClimb) {
		this._machClimb = _machClimb;
	}

	public Double getThrottleCruise() {
		return _throttleCruise;
	}

	public void setThrottleCruise(Double _throttleCruise) {
		this._throttleCruise = _throttleCruise;
	}

	public Amount<Length> getAltitudeTakeOff() {
		return _altitudeTakeOff;
	}

	public void setAltitudeTakeOff(Amount<Length> _altitudeTakeOff) {
		this._altitudeTakeOff = _altitudeTakeOff;
	}

	public Double getMachTakeOff() {
		return _machTakeOff;
	}

	public void setMachTakeOff(Double _machTakeOff) {
		this._machTakeOff = _machTakeOff;
	}

	public Double getThrottleTakeOff() {
		return _throttleTakeOff;
	}

	public void setThrottleTakeOff(Double _throttleTakeOff) {
		this._throttleTakeOff = _throttleTakeOff;
	}

	public List<Amount<Angle>> getFlapDeflectionTakeOff() {
		return _flapDeflectionTakeOff;
	}

	public void setFlapDeflectionTakeOff(List<Amount<Angle>> _flapDeflectionTakeOff) {
		this._flapDeflectionTakeOff = _flapDeflectionTakeOff;
	}

	public List<Amount<Angle>> getSlatDeflectionTakeOff() {
		return _slatDeflectionTakeOff;
	}

	public void setSlatDeflectionTakeOff(List<Amount<Angle>> _slatDeflectionTakeOff) {
		this._slatDeflectionTakeOff = _slatDeflectionTakeOff;
	}

	public Amount<Length> getAltitudeLanding() {
		return _altitudeLanding;
	}

	public void setAltitudeLanding(Amount<Length> _altitudeLanding) {
		this._altitudeLanding = _altitudeLanding;
	}

	public Double getMachLanding() {
		return _machLanding;
	}

	public void setMachLanding(Double _machLanding) {
		this._machLanding = _machLanding;
	}

	public List<Amount<Angle>> getFlapDeflectionLanding() {
		return _flapDeflectionLanding;
	}

	public void setFlapDeflectionLanding(List<Amount<Angle>> _flapDeflectionLanding) {
		this._flapDeflectionLanding = _flapDeflectionLanding;
	}

	public List<Amount<Angle>> getSlatDeflectionLanding() {
		return _slatDeflectionLanding;
	}

	public void setSlatDeflectionLanding(List<Amount<Angle>> _slatDeflectionLanding) {
		this._slatDeflectionLanding = _slatDeflectionLanding;
	}

	public Double getPpressureCoefficientTakeOff() {
		return _pressureCoefficientTakeOff;
	}

	public void setPressureCoefficientTakeOff(Double _pressureCoefficientTakeOff) {
		this._pressureCoefficientTakeOff = _pressureCoefficientTakeOff;
	}

	public Amount<Velocity> getTASTakeOff() {
		return _tasTakeOff;
	}

	public void setTASTakeOff(Amount<Velocity> _tasTakeOff) {
		this._tasTakeOff = _tasTakeOff;
	}

	public Amount<Velocity> getCASTakeOff() {
		return _casTakeOff;
	}

	public void setCASTakeOff(Amount<Velocity> _casTakeOff) {
		this._casTakeOff = _casTakeOff;
	}

	public Amount<Velocity> getEASTakeOff() {
		return _easTakeOff;
	}

	public void setEASTakeOff(Amount<Velocity> _easTakeOff) {
		this._easTakeOff = _easTakeOff;
	}

	public Amount<VolumetricDensity> getDensityTakeOff() {
		return _densityTakeOff;
	}

	public void setDensityTakeOff(Amount<VolumetricDensity> _densityTakeOff) {
		this._densityTakeOff = _densityTakeOff;
	}

	public Amount<Pressure> getStaticPressureTakeOff() {
		return _staticPressureTakeOff;
	}

	public void setStaticPressureTakeOff(Amount<Pressure> _staticPressureTakeOff) {
		this._staticPressureTakeOff = _staticPressureTakeOff;
	}

	public Amount<Pressure> getDynamicPressureTakeOff() {
		return _dynamicPressureTakeOff;
	}

	public void setDynamicPressureTakeOff(Amount<Pressure> _dynamicPressureTakeOff) {
		this._dynamicPressureTakeOff = _dynamicPressureTakeOff;
	}

	public Amount<Pressure> getStagnationPressureTakeOff() {
		return _stagnationPressureTakeOff;
	}

	public void setStagnationPressureTakeOff(Amount<Pressure> _stagnationPressureTakeOff) {
		this._stagnationPressureTakeOff = _stagnationPressureTakeOff;
	}

	public Amount<Pressure> getMaxDeltaPressureTakeOff() {
		return _maxDeltaPressureTakeOff;
	}

	public void setMaxDeltaPressureTakeOff(Amount<Pressure> _maxDeltaPressureTakeOff) {
		this._maxDeltaPressureTakeOff = _maxDeltaPressureTakeOff;
	}

	public Amount<Pressure> getMaxDynamicPressureTakeOff() {
		return _maxDynamicPressureTakeOff;
	}

	public void setMaxDynamicPressureTakeOff(Amount<Pressure> _maxDynamicPressureTakeOff) {
		this._maxDynamicPressureTakeOff = _maxDynamicPressureTakeOff;
	}

	public Amount<DynamicViscosity> getMuTakeOff() {
		return _muTakeOff;
	}

	public void setMuTakeOff(Amount<DynamicViscosity> _muTakeOff) {
		this._muTakeOff = _muTakeOff;
	}

	public Amount<Temperature> getStaticTemperatureTakeOff() {
		return _staticTemperatureTakeOff;
	}

	public void setStaticTemperatureTakeOff(Amount<Temperature> _staticTemperatureTakeOff) {
		this._staticTemperatureTakeOff = _staticTemperatureTakeOff;
	}

	public Amount<Temperature> getStagnationTemperatureTakeOff() {
		return _stagnationTemperatureTakeOff;
	}

	public void setStagnationTemperatureTakeOff(Amount<Temperature> _stagnationTemperatureTakeOff) {
		this._stagnationTemperatureTakeOff = _stagnationTemperatureTakeOff;
	}

	public Double getPressureCoefficientLanding() {
		return _pressureCoefficientLanding;
	}

	public void setPressureCoefficientLanding(Double _pressureCoefficientLanding) {
		this._pressureCoefficientLanding = _pressureCoefficientLanding;
	}

	public Amount<Velocity> getTASLanding() {
		return _tasLanding;
	}

	public void setTASLanding(Amount<Velocity> _tasLanding) {
		this._tasLanding = _tasLanding;
	}

	public Amount<Velocity> getCASLanding() {
		return _casLanding;
	}

	public void setCASLanding(Amount<Velocity> _casLanding) {
		this._casLanding = _casLanding;
	}

	public Amount<Velocity> getEASLanding() {
		return _easLanding;
	}

	public void setEASLanding(Amount<Velocity> _easLanding) {
		this._easLanding = _easLanding;
	}

	public Amount<VolumetricDensity> getDensityLanding() {
		return _densityLanding;
	}

	public void setDensityLanding(Amount<VolumetricDensity> _densityLanding) {
		this._densityLanding = _densityLanding;
	}

	public Amount<Pressure> getStaticPressureLanding() {
		return _staticPressureLanding;
	}

	public void setStaticPressureLanding(Amount<Pressure> _staticPressureLanding) {
		this._staticPressureLanding = _staticPressureLanding;
	}

	public Amount<Pressure> getDynamicPressureLanding() {
		return _dynamicPressureLanding;
	}

	public void setDynamicPressureLanding(Amount<Pressure> _dynamicPressureLanding) {
		this._dynamicPressureLanding = _dynamicPressureLanding;
	}

	public Amount<Pressure> getStagnationPressureLanding() {
		return _stagnationPressureLanding;
	}

	public void setStagnationPressureLanding(Amount<Pressure> _stagnationPressureLanding) {
		this._stagnationPressureLanding = _stagnationPressureLanding;
	}

	public Amount<Pressure> getMaxDeltaPressureLanding() {
		return _maxDeltaPressureLanding;
	}

	public void setMaxDeltaPressureLanding(Amount<Pressure> _maxDeltaPressureLanding) {
		this._maxDeltaPressureLanding = _maxDeltaPressureLanding;
	}

	public Amount<Pressure> getMaxDynamicPressureLanding() {
		return _maxDynamicPressureLanding;
	}

	public void setMaxDynamicPressureLanding(Amount<Pressure> _maxDynamicPressureLanding) {
		this._maxDynamicPressureLanding = _maxDynamicPressureLanding;
	}

	public Amount<DynamicViscosity> getMuLanding() {
		return _muLanding;
	}

	public void setMuLanding(Amount<DynamicViscosity> _muLanding) {
		this._muLanding = _muLanding;
	}

	public Amount<Temperature> getStaticTemperatureLanding() {
		return _staticTemperatureLanding;
	}

	public void setStaticTemperatureLanding(Amount<Temperature> _staticTemperatureLanding) {
		this._staticTemperatureLanding = _staticTemperatureLanding;
	}

	public Amount<Temperature> getStagnationTemperatureLanding() {
		return _stagnationTemperatureLanding;
	}

	public void setStagnationTemperatureLanding(Amount<Temperature> _stagnationTemperatureLanding) {
		this._stagnationTemperatureLanding = _stagnationTemperatureLanding;
	}

	public Double getPressureCoefficientCruise() {
		return _pressureCoefficientCruise;
	}

	public void setPressureCoefficientCruise(Double _pressureCoefficientCruise) {
		this._pressureCoefficientCruise = _pressureCoefficientCruise;
	}

	public Amount<Velocity> getTASCruise() {
		return _tasCruise;
	}

	public void setTASCruise(Amount<Velocity> _tasCruise) {
		this._tasCruise = _tasCruise;
	}

	public Amount<Velocity> getCASCruise() {
		return _casCruise;
	}

	public void setCASCruise(Amount<Velocity> _casCruise) {
		this._casCruise = _casCruise;
	}

	public Amount<Velocity> getEASCruise() {
		return _easCruise;
	}

	public void setEASCruise(Amount<Velocity> _easCruise) {
		this._easCruise = _easCruise;
	}

	public Amount<VolumetricDensity> getDensityCruise() {
		return _densityCruise;
	}

	public void setDensityCruise(Amount<VolumetricDensity> _densityCruise) {
		this._densityCruise = _densityCruise;
	}

	public Amount<Pressure> getStaticPressureCruise() {
		return _staticPressureCruise;
	}

	public void setStaticPressureCruise(Amount<Pressure> _staticPressureCruise) {
		this._staticPressureCruise = _staticPressureCruise;
	}

	public Amount<Pressure> getDynamicPressureCruise() {
		return _dynamicPressureCruise;
	}

	public void setDynamicPressureCruise(Amount<Pressure> _dynamicPressureCruise) {
		this._dynamicPressureCruise = _dynamicPressureCruise;
	}

	public Amount<Pressure> getStagnationPressureCruise() {
		return _stagnationPressureCruise;
	}

	public void setStagnationPressureCruise(Amount<Pressure> _stagnationPressureCruise) {
		this._stagnationPressureCruise = _stagnationPressureCruise;
	}

	public Amount<Pressure> getMaxDeltaPressureCruise() {
		return _maxDeltaPressureCruise;
	}

	public void setMaxDeltaPressureCruise(Amount<Pressure> _maxDeltaPressureCruise) {
		this._maxDeltaPressureCruise = _maxDeltaPressureCruise;
	}

	public Amount<Pressure> getMaxDynamicPressureCruise() {
		return _maxDynamicPressureCruise;
	}

	public void setMaxDynamicPressureCruise(Amount<Pressure> _maxDynamicPressureCruise) {
		this._maxDynamicPressureCruise = _maxDynamicPressureCruise;
	}

	public Amount<DynamicViscosity> getMuCruise() {
		return _muCruise;
	}

	public void setMuCruise(Amount<DynamicViscosity> _muCruise) {
		this._muCruise = _muCruise;
	}

	public Amount<Temperature> getStaticTemperatureCruise() {
		return _staticTemperatureCruise;
	}

	public void setStaticTemperatureCruise(Amount<Temperature> _staticTemperatureCruise) {
		this._staticTemperatureCruise = _staticTemperatureCruise;
	}

	public Amount<Temperature> getStagnationTemperatureCruise() {
		return _stagnationTemperatureCruise;
	}

	public void setStagnationTemperatureCruise(Amount<Temperature> _stagnationTemperatureCruise) {
		this._stagnationTemperatureCruise = _stagnationTemperatureCruise;
	}

	public StdAtmos1976 getAtmosphereCruise() {
		return _atmosphereCruise;
	}

	public void setAtmosphereCruise(StdAtmos1976 _atmoshpereCruise) {
		this._atmosphereCruise = _atmoshpereCruise;
	}

	public StdAtmos1976 getAtmosphereTakeOff() {
		return _atmosphereTakeOff;
	}

	public void setAtmosphereTakeOff(StdAtmos1976 _atmoshpereTakeOff) {
		this._atmosphereTakeOff = _atmoshpereTakeOff;
	}

	public StdAtmos1976 getAtmosphereLanding() {
		return _atmosphereLanding;
	}

	public void setAtmosphereLanding(StdAtmos1976 _atmoshpereLanding) {
		this._atmosphereLanding = _atmoshpereLanding;
	}

	public MyInterpolatingFunction getThrottleGroundIdleTakeOff() {
		return _throttleGroundIdleTakeOff;
	}

	public void setThrottleGroundIdleTakeOff(MyInterpolatingFunction _throttleGroundIdleTakeOff) {
		this._throttleGroundIdleTakeOff = _throttleGroundIdleTakeOff;
	}

	public MyInterpolatingFunction getThrottleGroundIdleLanding() {
		return _throttleGroundIdleLanding;
	}

	public void setThrottleGroundIdleLanding(MyInterpolatingFunction _throttleGroundIdleLanding) {
		this._throttleGroundIdleLanding = _throttleGroundIdleLanding;
	}

	public Amount<Length> getAltitudeClimb() {
		return _altitudeClimb;
	}

	public void setAltitudeClimb(Amount<Length> _altitudeClimb) {
		this._altitudeClimb = _altitudeClimb;
	}

	public Amount<Angle> getAlphaCurrentClimb() {
		return _alphaCurrentClimb;
	}

	public void setAlphaCurrentClimb(Amount<Angle> _alphaCurrentClimb) {
		this._alphaCurrentClimb = _alphaCurrentClimb;
	}

	public Amount<Angle> getAlphaCurrentCruise() {
		return _alphaCurrentCruise;
	}

	public void setAlphaCurrentCruise(Amount<Angle> _alphaCurrentCruise) {
		this._alphaCurrentCruise = _alphaCurrentCruise;
	}

	public Amount<Angle> getAlphaCurrentTakeOff() {
		return _alphaCurrentTakeOff;
	}

	public void setAlphaCurrentTakeOff(Amount<Angle> _alphaCurrentTakeOff) {
		this._alphaCurrentTakeOff = _alphaCurrentTakeOff;
	}

	public Amount<Angle> getAlphaCurrentLanding() {
		return _alphaCurrentLanding;
	}

	public void setAlphaCurrentLanding(Amount<Angle> _alphaCurrentLanding) {
		this._alphaCurrentLanding = _alphaCurrentLanding;
	}
	
} // end of class