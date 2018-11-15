package analyses;

import java.util.ArrayList;
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

import standaloneutils.JPADXmlReader;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.aerotools.aero.StdAtmos1976;
import standaloneutils.atmosphere.PressureCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.atmosphere.TemperatureCalc;

/**
 * Set the aircraft current operating conditions
 *   
 * @author Vittorio Trifari
 */
public class OperatingConditions {

	//-------------------------------------------------------------------------------
	//	VARIABLES DECLARATION
	//-------------------------------------------------------------------------------
	private IOperatingConditions theOperatingConditionsInterface;

	// Global data
	private double machTransonicThreshold = 0.7;
	
	// CLIMB
	private StdAtmos1976 atmosphereClimb; 
	private Double pressureCoefficientClimb;
	private Amount<Velocity> tasClimb;
	private Amount<Velocity> casClimb;
	private Amount<Velocity> easClimb;
	private Amount<VolumetricDensity> densityClimb;
	private Amount<Pressure> staticPressureClimb;
	private Amount<Pressure> dynamicPressureClimb;
	private Amount<Pressure> stagnationPressureClimb; 
	private Amount<Pressure> maxDeltaPressureClimb;
	private Amount<Pressure> maxDynamicPressureClimb;
	private Amount<DynamicViscosity> muClimb;
	private Amount<Temperature> staticTemperatureClimb;
	private Amount<Temperature> stagnationTemperatureClimb;
	
	// CRUISE
	private StdAtmos1976 atmosphereCruise; 
	private Double pressureCoefficientCruise;
	private Amount<Velocity> tasCruise;
	private Amount<Velocity> casCruise;
	private Amount<Velocity> easCruise;
	private Amount<VolumetricDensity> densityCruise;
	private Amount<Pressure> staticPressureCruise;
	private Amount<Pressure> dynamicPressureCruise;
	private Amount<Pressure> stagnationPressureCruise; 
	private Amount<Pressure> maxDeltaPressureCruise;
	private Amount<Pressure> maxDynamicPressureCruise;
	private Amount<DynamicViscosity> muCruise;
	private Amount<Temperature> staticTemperatureCruise;
	private Amount<Temperature> stagnationTemperatureCruise;

	// TAKE-OFF
	private StdAtmos1976 atmosphereTakeOff;
	private Double pressureCoefficientTakeOff;
	private Amount<Velocity> tasTakeOff;
	private Amount<Velocity> casTakeOff;
	private Amount<Velocity> easTakeOff;
	private Amount<VolumetricDensity> densityTakeOff;
	private Amount<Pressure> staticPressureTakeOff;
	private Amount<Pressure> dynamicPressureTakeOff;
	private Amount<Pressure> stagnationPressureTakeOff; 
	private Amount<Pressure> maxDeltaPressureTakeOff;
	private Amount<Pressure> maxDynamicPressureTakeOff;
	private Amount<DynamicViscosity> muTakeOff;
	private Amount<Temperature> staticTemperatureTakeOff;
	private Amount<Temperature> stagnationTemperatureTakeOff;

	// LANDING
	private StdAtmos1976 atmosphereLanding;
	private Double pressureCoefficientLanding;
	private Amount<Velocity> tasLanding;
	private Amount<Velocity> casLanding;
	private Amount<Velocity> easLanding;
	private Amount<VolumetricDensity> densityLanding;
	private Amount<Pressure> staticPressureLanding;
	private Amount<Pressure> dynamicPressureLanding;
	private Amount<Pressure> stagnationPressureLanding; 
	private Amount<Pressure> maxDeltaPressureLanding;
	private Amount<Pressure> maxDynamicPressureLanding;
	private Amount<DynamicViscosity> muLanding;
	private Amount<Temperature> staticTemperatureLanding;
	private Amount<Temperature> stagnationTemperatureLanding;

	//-------------------------------------------------------------------------------
	//	BUILDER
	//-------------------------------------------------------------------------------
	public OperatingConditions(IOperatingConditions theOperatingConditionsInterface) {
		
		this.theOperatingConditionsInterface = theOperatingConditionsInterface;
		calculate();
		
	}
	
	//-------------------------------------------------------------------------------
	//	METHODS
	//-------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public static OperatingConditions importFromXML(String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading operating conditions data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		
		///////////////////////////////////////////////////////////////
		// CLIMB DATA:
		Amount<Angle> alphaCurrentClimb = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> betaCurrentClimb = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double machClimb = 0.0;
		Amount<Length> altitudeClimb = Amount.valueOf(0.0, SI.METER);
		Amount<Temperature> deltaTemperatureClimb = Amount.valueOf(0.0, SI.CELSIUS);
		double throttleClimb = 0.0;
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
		String betaCurrentClimbProperty = reader.getXMLPropertyByPath("//climb/beta_current");
		if(betaCurrentClimbProperty != null)
			betaCurrentClimb = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//climb/beta_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machClimbProperty = reader.getXMLPropertyByPath("//climb/mach");
		if(machClimbProperty != null)
			machClimb = Double.valueOf(machClimbProperty);
		//.............................................................
		String altitudeClimbProperty = reader.getXMLPropertyByPath("//climb/altitude");
		if(altitudeClimbProperty != null)
			altitudeClimb = reader.getXMLAmountLengthByPath("//climb/altitude");
		//.............................................................
		String deltaTemperatureClimbProperty = reader.getXMLPropertyByPath("//climb/ISA_deviation");
		if(deltaTemperatureClimbProperty != null)
			deltaTemperatureClimb = (Amount<Temperature>) reader.getXMLAmountWithUnitByPath("//climb/ISA_deviation");
		//.............................................................
		String throttleClimbProperty = reader.getXMLPropertyByPath("//climb/throttle");
		if(throttleClimbProperty != null)
			throttleClimb= Double.valueOf(throttleClimbProperty);
		
		///////////////////////////////////////////////////////////////
		// CRUISE DATA:
		Amount<Angle> alphaCurrentCruise = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> betaCurrentCruise = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double machCruise = 0.0;
		Amount<Length> altitudeCruise = Amount.valueOf(0.0, SI.METER);
		Amount<Temperature> deltaTemperatureCruise = Amount.valueOf(0.0, SI.CELSIUS);
		double throttleCruise = 0.0;
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
		String betaCurrentCruiseProperty = reader.getXMLPropertyByPath("//cruise/beta_current");
		if(betaCurrentCruiseProperty != null)
			betaCurrentCruise = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//cruise/beta_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machCruiseProperty = reader.getXMLPropertyByPath("//cruise/mach");
		if(machCruiseProperty != null)
			machCruise = Double.valueOf(machCruiseProperty);
		//.............................................................
		String altitudeCruiseProperty = reader.getXMLPropertyByPath("//cruise/altitude");
		if(altitudeCruiseProperty != null)
			altitudeCruise = reader.getXMLAmountLengthByPath("//cruise/altitude");
		//.............................................................
		String deltaTemperatureCruiseProperty = reader.getXMLPropertyByPath("//cruise/ISA_deviation");
		if(deltaTemperatureCruiseProperty != null)
			deltaTemperatureCruise = (Amount<Temperature>) reader.getXMLAmountWithUnitByPath("//cruise/ISA_deviation");
		//.............................................................
		String throttleCruiseProperty = reader.getXMLPropertyByPath("//cruise/throttle");
		if(throttleCruiseProperty != null)
			throttleCruise= Double.valueOf(throttleCruiseProperty);
		
		///////////////////////////////////////////////////////////////
		// TAKE-OFF DATA:
		Amount<Angle> alphaCurrentTakeOff = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> betaCurrentTakeOff = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double machTakeOff = 0.0;
		Amount<Length> altitudeTakeOff = Amount.valueOf(0.0, SI.METER);
		Amount<Temperature> deltaTemperatureTakeOff = Amount.valueOf(0.0, SI.CELSIUS);
		double throttleTakeOff = 0.0;
		List<Amount<Angle>> deltaFlapListTakeOff = new ArrayList<>();
		List<Amount<Angle>> deltaSlatListTakeOff = new ArrayList<>();
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
		String betaCurrentTakeOffProperty = reader.getXMLPropertyByPath("//take_off/beta_current");
		if(betaCurrentTakeOffProperty != null)
			betaCurrentTakeOff = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//take_off/beta_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machTakeOffProperty = reader.getXMLPropertyByPath("//take_off/mach");
		if(machTakeOffProperty != null)
			machTakeOff = Double.valueOf(machTakeOffProperty);
		//.............................................................
		String altitudeTakeOffProperty = reader.getXMLPropertyByPath("//take_off/altitude");
		if(altitudeTakeOffProperty != null)
			altitudeTakeOff = reader.getXMLAmountLengthByPath("//take_off/altitude");
		//.............................................................
		String deltaTemperatureTakeOffProperty = reader.getXMLPropertyByPath("//take_off/ISA_deviation");
		if(deltaTemperatureTakeOffProperty != null)
			deltaTemperatureTakeOff = (Amount<Temperature>) reader.getXMLAmountWithUnitByPath("//take_off/ISA_deviation");
		//.............................................................
		String throttleTakeOffProperty = reader.getXMLPropertyByPath("//take_off/throttle");
		if(throttleTakeOffProperty != null)
			throttleTakeOff= Double.valueOf(throttleTakeOffProperty);
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
				deltaFlapListTakeOff.add(Amount.valueOf(Double.valueOf(deltaFlapTakeOffProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaFlapListTakeOff.clear();
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
				deltaSlatListTakeOff.add(Amount.valueOf(Double.valueOf(deltaSlatTakeOffProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaSlatListTakeOff.clear();

		///////////////////////////////////////////////////////////////
		// LANDING DATA:
		Amount<Angle> alphaCurrentLanding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> betaCurrentLanding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double machLanding = 0.0;
		Amount<Length> altitudeLanding = Amount.valueOf(0.0, SI.METER);
		Amount<Temperature> deltaTemperatureLanding = Amount.valueOf(0.0, SI.CELSIUS);
		double throttleLanding = 0.0;
		List<Amount<Angle>> deltaFlapListLanding = new ArrayList<>();
		List<Amount<Angle>> deltaSlatListLanding = new ArrayList<>();
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
		String betaCurrentLandingProperty = reader.getXMLPropertyByPath("//landing/beta_current");
		if(betaCurrentLandingProperty != null)
			betaCurrentLanding = Amount.valueOf(
					Double.valueOf(
							reader.getXMLPropertyByPath(
									"//landing/beta_current"
									)
							),
					NonSI.DEGREE_ANGLE)
			.to(SI.RADIAN);
		//.............................................................
		String machLandingProperty = reader.getXMLPropertyByPath("//landing/mach");
		if(machLandingProperty != null)
			machLanding = Double.valueOf(machLandingProperty);
		//.............................................................
		String altitudeLandingProperty = reader.getXMLPropertyByPath("//landing/altitude");
		if(altitudeLandingProperty != null)
			altitudeLanding = reader.getXMLAmountLengthByPath("//landing/altitude");
		//.............................................................
		String deltaTemperatureLandingProperty = reader.getXMLPropertyByPath("//landing/ISA_deviation");
		if(deltaTemperatureLandingProperty != null)
			deltaTemperatureLanding = (Amount<Temperature>) reader.getXMLAmountWithUnitByPath("//landing/ISA_deviation");
		//.............................................................
		String throttleLandingProperty = reader.getXMLPropertyByPath("//landing/throttle");
		if(throttleLandingProperty != null)
			throttleLanding= Double.valueOf(throttleLandingProperty);
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
				deltaFlapListLanding.add(Amount.valueOf(Double.valueOf(deltaFlapLandingProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaFlapListLanding.clear();
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
				deltaSlatListLanding.add(Amount.valueOf(Double.valueOf(deltaSlatLandingProperty.get(i)), NonSI.DEGREE_ANGLE));
		}
		else
			deltaSlatListLanding.clear();


		///////////////////////////////////////////////////////////////
		OperatingConditions theOperatingConditions = new OperatingConditions(
				new IOperatingConditions.Builder()
				.setID(id)
				.setAlphaClimb(alphaCurrentClimb)
				.setBetaClimb(betaCurrentClimb)
				.setMachClimb(machClimb)
				.setAltitudeClimb(altitudeClimb)
				.setDeltaTemperatureClimb(deltaTemperatureClimb)
				.setThrottleClimb(throttleClimb)
				.setAlphaCruise(alphaCurrentCruise)
				.setBetaCruise(betaCurrentCruise)
				.setMachCruise(machCruise)
				.setAltitudeCruise(altitudeCruise)
				.setDeltaTemperatureCruise(deltaTemperatureCruise)
				.setThrottleCruise(throttleCruise)
				.setAlphaTakeOff(alphaCurrentTakeOff)
				.setBetaTakeOff(betaCurrentTakeOff)
				.setMachTakeOff(machTakeOff)
				.setAltitudeTakeOff(altitudeTakeOff)
				.setDeltaTemperatureTakeOff(deltaTemperatureTakeOff)
				.setThrottleTakeOff(throttleTakeOff)
				.addAllTakeOffSlatDefletctionList(deltaFlapListTakeOff)
				.addAllTakeOffSlatDefletctionList(deltaSlatListTakeOff)
				.setAlphaLanding(alphaCurrentLanding)
				.setBetaLanding(betaCurrentLanding)
				.setMachLanding(machLanding)
				.setAltitudeLanding(altitudeLanding)
				.setDeltaTemperatureLanding(deltaTemperatureLanding)
				.setThrottleLanding(throttleLanding)
				.addAllLandingSlatDefletctionList(deltaFlapListLanding)
				.addAllLandingSlatDefletctionList(deltaSlatListLanding)
				.build()
				);
		
		return theOperatingConditions;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tOperating Conditions\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + theOperatingConditionsInterface.getID() + "' \n")
				.append("\t.....................................\n")
				.append("\tAlpha current Climb: " + theOperatingConditionsInterface.getAlphaClimb().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tBeta current Climb: " + theOperatingConditionsInterface.getBetaClimb().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Climb: " + theOperatingConditionsInterface.getMachClimb() + "\n")
				.append("\tAltitude Climb: " + theOperatingConditionsInterface.getAltitudeClimb().to(NonSI.FOOT) + "\n")
				.append("\tISA Deviation Climb: " + theOperatingConditionsInterface.getDeltaTemperatureClimb().to(SI.CELSIUS) + "\n")
				.append("\tThrottle Climb: " + theOperatingConditionsInterface.getThrottleClimb() + "\n")
				.append("\t.....................................\n")
				.append("\tAlpha current Cruise: " + theOperatingConditionsInterface.getAlphaCruise().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tBeta current Cruise: " + theOperatingConditionsInterface.getBetaCruise().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Cruise: " + theOperatingConditionsInterface.getMachCruise() + "\n")
				.append("\tAltitude Cruise: " + theOperatingConditionsInterface.getAltitudeCruise().to(NonSI.FOOT) + "\n")
				.append("\tISA Deviation Cruise: " + theOperatingConditionsInterface.getDeltaTemperatureCruise().to(SI.CELSIUS) + "\n")
				.append("\tThrottle Cruise: " + theOperatingConditionsInterface.getThrottleCruise() + "\n")
				.append("\t.....................................\n")
				.append("\tAlpha current TakeOff: " + theOperatingConditionsInterface.getAlphaTakeOff().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tBeta current TakeOff: " + theOperatingConditionsInterface.getBetaTakeOff().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach TakeOff: " + theOperatingConditionsInterface.getMachTakeOff() + "\n")
				.append("\tAltitude TakeOff: " + theOperatingConditionsInterface.getAltitudeTakeOff().to(NonSI.FOOT) + "\n")
				.append("\tISA Deviation TakeOff: " + theOperatingConditionsInterface.getDeltaTemperatureTakeOff().to(SI.CELSIUS) + "\n")
				.append("\tThrottle TakeOff: " + theOperatingConditionsInterface.getThrottleTakeOff() + "\n");
		if(!theOperatingConditionsInterface.getTakeOffFlapDefletctionList().isEmpty())
				sb.append("\tFlap deflections Take-off: " + theOperatingConditionsInterface.getTakeOffFlapDefletctionList() + "\n");
		if(!theOperatingConditionsInterface.getTakeOffSlatDefletctionList().isEmpty())
				sb.append("\tSlat deflections Take-off: " + theOperatingConditionsInterface.getTakeOffSlatDefletctionList() + "\n");
				sb.append("\t.....................................\n")
				.append("\tAlpha current Landing: " + theOperatingConditionsInterface.getAlphaLanding().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tBeta current Landing: " + theOperatingConditionsInterface.getBetaLanding().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tMach Landing: " + theOperatingConditionsInterface.getMachLanding() + "\n")
				.append("\tAltitude Landing: " + theOperatingConditionsInterface.getAltitudeLanding().to(NonSI.FOOT) + "\n")
				.append("\tISA Deviation Landing: " + theOperatingConditionsInterface.getDeltaTemperatureLanding().to(SI.CELSIUS) + "\n")
				.append("\tThrottle Landing: " + theOperatingConditionsInterface.getThrottleLanding() + "\n");
		if(!theOperatingConditionsInterface.getLandingFlapDefletctionList().isEmpty())
				sb.append("\tFlap deflections Landing: " + theOperatingConditionsInterface.getLandingFlapDefletctionList() + "\n");
		if(!theOperatingConditionsInterface.getLandingSlatDefletctionList().isEmpty())
				sb.append("\tSlat deflections Landing: " + theOperatingConditionsInterface.getLandingSlatDefletctionList() + "\n");
		
				sb.append("\n\t.....................................\n")
				.append("\tCLIMB\n")
				.append("\tClimb pressure coefficient: " + pressureCoefficientClimb + "\n")
				.append("\tClimb density: " + densityClimb + "\n")
				.append("\tClimb static pressure: " + staticPressureClimb + "\n")
				.append("\tClimb dynamic pressure: " + dynamicPressureClimb + "\n")
				.append("\tClimb stagnation pressure: " + stagnationPressureClimb + "\n")
				.append("\tClimb maximum delta pressure outside-inside: " + maxDeltaPressureClimb + "\n")
				.append("\tClimb static temperature: " + staticTemperatureClimb + "\n")
				.append("\tClimb stagnation temperature: " + stagnationTemperatureClimb + "\n")
				.append("\tClimb speed (TAS): " + tasClimb + "\n")
				.append("\tClimb speed (CAS): " + casClimb + "\n")
				.append("\tClimb speed (EAS): " + easClimb + "\n")
				.append("\tClimb dynamic viscosity: " + muClimb + "\n")
				.append("\t.....................................\n")
				.append("\tCRUISE\n")
				.append("\tCruise pressure coefficient: " + pressureCoefficientCruise + "\n")
				.append("\tCruise density: " + densityCruise + "\n")
				.append("\tCruise static pressure: " + staticPressureCruise + "\n")
				.append("\tCruise dynamic pressure: " + dynamicPressureCruise + "\n")
				.append("\tCruise stagnation pressure: " + stagnationPressureCruise + "\n")
				.append("\tCruise maximum delta pressure outside-inside: " + maxDeltaPressureCruise + "\n")
				.append("\tCruise static temperature: " + staticTemperatureCruise + "\n")
				.append("\tCruise stagnation temperature: " + stagnationTemperatureCruise + "\n")
				.append("\tCruise speed (TAS): " + tasCruise + "\n")
				.append("\tCruise speed (CAS): " + casCruise + "\n")
				.append("\tCruise speed (EAS): " + easCruise + "\n")
				.append("\tCruise dynamic viscosity: " + muCruise + "\n")
				.append("\t.....................................\n")
				.append("\tTAKE-OFF\n")
				.append("\tTake-off pressure coefficient: " + pressureCoefficientTakeOff + "\n")
				.append("\tTake-off density: " + densityTakeOff + "\n")
				.append("\tTake-off static pressure: " + staticPressureTakeOff + "\n")
				.append("\tTake-off dynamic pressure: " + dynamicPressureTakeOff + "\n")
				.append("\tTake-off stagnation pressure: " + stagnationPressureTakeOff + "\n")
				.append("\tTake-off maximum delta pressure outside-inside: " + maxDeltaPressureTakeOff + "\n")
				.append("\tTake-off static temperature: " + staticTemperatureTakeOff + "\n")
				.append("\tTake-off stagnation temperature: " + stagnationTemperatureTakeOff + "\n")
				.append("\tTake-off speed (TAS): " + tasTakeOff + "\n")
				.append("\tTake-off speed (CAS): " + casTakeOff + "\n")
				.append("\tTake-off speed (EAS): " + easTakeOff + "\n")
				.append("\tTake-off dynamic viscosity: " + muTakeOff + "\n")
				.append("\t.....................................\n")
				.append("\tLANDING\n")
				.append("\tLanding pressure coefficient: " + pressureCoefficientLanding + "\n")
				.append("\tLanding density: " + densityLanding + "\n")
				.append("\tLanding static pressure: " + staticPressureLanding + "\n")
				.append("\tLanding dynamic pressure: " + dynamicPressureLanding + "\n")
				.append("\tLanding stagnation pressure: " + stagnationPressureLanding + "\n")
				.append("\tLanding maximum delta pressure outside-inside: " + maxDeltaPressureLanding + "\n")
				.append("\tLanding static temperature: " + staticTemperatureLanding + "\n")
				.append("\tLanding stagnation temperature: " + stagnationTemperatureLanding + "\n")
				.append("\tLanding speed (TAS): " + tasLanding + "\n")
				.append("\tLanding speed (CAS): " + casLanding + "\n")
				.append("\tLanding speed (EAS): " + easLanding + "\n")
				.append("\tLanding dynamic viscosity: " + muLanding + "\n")
				.append("\t.....................................\n")
				;
				
		return sb.toString();
	}
	
	/** 
	 * Evaluate all dependent parameters
	 * @author Lorenzo Attanasio, Vittorio Trifari
	 */
	public void calculate() {

		// CLIMB
		atmosphereClimb = new StdAtmos1976(theOperatingConditionsInterface.getAltitudeClimb().doubleValue(SI.METER));
		pressureCoefficientClimb = PressureCalc.calculatePressureCoefficient(theOperatingConditionsInterface.getMachClimb());
		densityClimb = Amount.valueOf(atmosphereClimb.getDensity()*1000, VolumetricDensity.UNIT);
		staticPressureClimb = Amount.valueOf(atmosphereClimb.getPressure(), SI.PASCAL);
		dynamicPressureClimb = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(theOperatingConditionsInterface.getMachClimb(), staticPressureClimb.doubleValue(SI.PASCAL))
				, SI.PASCAL);
		stagnationPressureClimb = dynamicPressureClimb.times(pressureCoefficientClimb).plus(staticPressureClimb);
		tasClimb = Amount.valueOf(theOperatingConditionsInterface.getMachClimb() * atmosphereClimb.getSpeedOfSound(), SI.METERS_PER_SECOND);
		casClimb = Amount.valueOf(
				SpeedCalc.calculateCAS(stagnationPressureClimb.doubleValue(SI.PASCAL), staticPressureClimb.doubleValue(SI.PASCAL))
				, SI.METERS_PER_SECOND);
		easClimb = Amount.valueOf(Math.sqrt(atmosphereClimb.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(stagnationPressureClimb.doubleValue(SI.PASCAL), 
						staticPressureClimb.doubleValue(SI.PASCAL), densityClimb.doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER))
						, SI.METERS_PER_SECOND);
		staticTemperatureClimb = Amount.valueOf(atmosphereClimb.getTemperature(), SI.KELVIN);
		stagnationTemperatureClimb = staticTemperatureClimb
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(theOperatingConditionsInterface.getMachClimb()));

		// Maximum pressure difference between outside and inside
		maxDeltaPressureClimb = Amount.valueOf(
				Math.abs(
						staticPressureClimb.doubleValue(SI.PASCAL) 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		double mu0 = 17.33e-6; // Pa*s, grc.nasa.gov/WWW/BGH/Viscosity.html
		double T0 = 288.166667; // K
		double C = 110.4; // K

		muClimb = Amount.valueOf(
				mu0*((T0 + C)
						/ (atmosphereClimb.getTemperature() + C))
						* Math.pow(
								atmosphereClimb.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);
		
		// CRUISE
		atmosphereCruise = new StdAtmos1976(theOperatingConditionsInterface.getAltitudeCruise().doubleValue(SI.METER));
		pressureCoefficientCruise = PressureCalc.calculatePressureCoefficient(theOperatingConditionsInterface.getMachCruise());
		densityCruise = Amount.valueOf(atmosphereCruise.getDensity()*1000, VolumetricDensity.UNIT);
		staticPressureCruise = Amount.valueOf(atmosphereCruise.getPressure(), SI.PASCAL);
		dynamicPressureCruise = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(theOperatingConditionsInterface.getMachCruise(), staticPressureCruise.doubleValue(SI.PASCAL))
				, SI.PASCAL);
		stagnationPressureCruise = dynamicPressureCruise.times(pressureCoefficientCruise).plus(staticPressureCruise);
		tasCruise = Amount.valueOf(theOperatingConditionsInterface.getMachCruise() * atmosphereCruise.getSpeedOfSound(), SI.METERS_PER_SECOND);
		casCruise = Amount.valueOf(
				SpeedCalc.calculateCAS(stagnationPressureCruise.doubleValue(SI.PASCAL), staticPressureCruise.doubleValue(SI.PASCAL))
				, SI.METERS_PER_SECOND);
		easCruise = Amount.valueOf(Math.sqrt(atmosphereCruise.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(stagnationPressureCruise.doubleValue(SI.PASCAL), 
						staticPressureCruise.doubleValue(SI.PASCAL), densityCruise.doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER))
						, SI.METERS_PER_SECOND);
		staticTemperatureCruise = Amount.valueOf(atmosphereCruise.getTemperature(), SI.KELVIN);
		stagnationTemperatureCruise = staticTemperatureCruise
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(theOperatingConditionsInterface.getMachCruise()));

		// Maximum pressure difference between outside and inside
		maxDeltaPressureCruise = Amount.valueOf(
				Math.abs(
						staticPressureCruise.doubleValue(SI.PASCAL) 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		muCruise = Amount.valueOf(
				mu0*((T0 + C)
						/ (atmosphereCruise.getTemperature() + C))
						* Math.pow(
								atmosphereCruise.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);

		// TAKE-OFF
		atmosphereTakeOff = new StdAtmos1976(theOperatingConditionsInterface.getAltitudeTakeOff().doubleValue(SI.METER));
		pressureCoefficientTakeOff = PressureCalc.calculatePressureCoefficient(theOperatingConditionsInterface.getMachTakeOff());
		densityTakeOff = Amount.valueOf(atmosphereTakeOff.getDensity()*1000, VolumetricDensity.UNIT);
		staticPressureTakeOff = Amount.valueOf(atmosphereTakeOff.getPressure(), SI.PASCAL);
		dynamicPressureTakeOff = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(theOperatingConditionsInterface.getMachTakeOff(), staticPressureTakeOff.doubleValue(SI.PASCAL))
				, SI.PASCAL);
		stagnationPressureTakeOff = dynamicPressureTakeOff.times(pressureCoefficientTakeOff).plus(staticPressureTakeOff);
		tasTakeOff = Amount.valueOf(theOperatingConditionsInterface.getMachTakeOff() * atmosphereTakeOff.getSpeedOfSound(), SI.METERS_PER_SECOND);
		casTakeOff = Amount.valueOf(
				SpeedCalc.calculateCAS(stagnationPressureTakeOff.doubleValue(SI.PASCAL), staticPressureTakeOff.doubleValue(SI.PASCAL))
				, SI.METERS_PER_SECOND);
		easTakeOff = Amount.valueOf(Math.sqrt(atmosphereTakeOff.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(stagnationPressureTakeOff.doubleValue(SI.PASCAL), 
						staticPressureTakeOff.doubleValue(SI.PASCAL), densityTakeOff.doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER))
						, SI.METERS_PER_SECOND);
		staticTemperatureTakeOff = Amount.valueOf(atmosphereTakeOff.getTemperature(), SI.KELVIN);
		stagnationTemperatureTakeOff = staticTemperatureTakeOff
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(theOperatingConditionsInterface.getMachTakeOff()));

		// Maximum pressure difference between outside and inside
		maxDeltaPressureTakeOff = Amount.valueOf(
				Math.abs(
						staticPressureTakeOff.doubleValue(SI.PASCAL) 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		muTakeOff = Amount.valueOf(
				mu0*((T0 + C)
						/ (atmosphereTakeOff.getTemperature() + C))
						* Math.pow(
								atmosphereTakeOff.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);
		
		// LANDING
		atmosphereLanding = new StdAtmos1976(theOperatingConditionsInterface.getAltitudeLanding().doubleValue(SI.METER));
		pressureCoefficientLanding = PressureCalc.calculatePressureCoefficient(theOperatingConditionsInterface.getMachLanding());
		densityLanding = Amount.valueOf(atmosphereLanding.getDensity()*1000, VolumetricDensity.UNIT);
		staticPressureLanding = Amount.valueOf(atmosphereLanding.getPressure(), SI.PASCAL);
		dynamicPressureLanding = Amount.valueOf(
				PressureCalc.calculateDynamicPressure(theOperatingConditionsInterface.getMachLanding(), staticPressureLanding.doubleValue(SI.PASCAL))
				, SI.PASCAL);
		stagnationPressureLanding = dynamicPressureLanding.times(pressureCoefficientLanding).plus(staticPressureLanding);
		tasLanding = Amount.valueOf(theOperatingConditionsInterface.getMachLanding() * atmosphereLanding.getSpeedOfSound(), SI.METERS_PER_SECOND);
		casLanding = Amount.valueOf(
				SpeedCalc.calculateCAS(stagnationPressureLanding.doubleValue(SI.PASCAL), staticPressureLanding.doubleValue(SI.PASCAL))
				, SI.METERS_PER_SECOND);
		easLanding = Amount.valueOf(Math.sqrt(atmosphereLanding.getDensityRatio())
				* SpeedCalc.calculateIsentropicVelocity(stagnationPressureLanding.doubleValue(SI.PASCAL), 
						staticPressureLanding.doubleValue(SI.PASCAL), densityLanding.doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER))
						, SI.METERS_PER_SECOND);
		staticTemperatureLanding = Amount.valueOf(atmosphereLanding.getTemperature(), SI.KELVIN);
		stagnationTemperatureLanding = staticTemperatureLanding
				.times(TemperatureCalc.calculateStagnationTemperatureToStaticTemperatureRatio(theOperatingConditionsInterface.getMachLanding()));

		// Maximum pressure difference between outside and inside
		maxDeltaPressureLanding = Amount.valueOf(
				Math.abs(
						staticPressureLanding.doubleValue(SI.PASCAL) 
						- ((new StdAtmos1976(2000.)).getPressure()))
						, SI.PASCAL);

		muLanding = Amount.valueOf(
				mu0*((T0 + C)
						/ (atmosphereLanding.getTemperature() + C))
						* Math.pow(
								atmosphereLanding.getTemperature()/T0, 
								3/2), 
								DynamicViscosity.UNIT);
		
	}

	public static StdAtmos1976 getAtmosphere(double altitude) {
		return new StdAtmos1976(altitude);
	}
	
	//-------------------------------------------------------------------------------
	//	GETTERS & SETTERS
	//-------------------------------------------------------------------------------
	
	public IOperatingConditions getTheOperatingConditionsInterface() {
		return theOperatingConditionsInterface;
	}

	
	
	public void setTheOperatingConditionsInterface(IOperatingConditions theOperatingConditionsInterface) {
		this.theOperatingConditionsInterface = theOperatingConditionsInterface;
	}

	public double getMachTransonicThreshold() {
		return machTransonicThreshold;
	}

	public void setMachTransonicThreshold(double machTransonicThreshold) {
		this.machTransonicThreshold = machTransonicThreshold;
	}

	public StdAtmos1976 getAtmosphereClimb() {
		return atmosphereClimb;
	}

	public void setAtmosphereClimb(StdAtmos1976 atmosphereClimb) {
		this.atmosphereClimb = atmosphereClimb;
	}

	public Double getPressureCoefficientClimb() {
		return pressureCoefficientClimb;
	}

	public void setPressureCoefficientClimb(Double pressureCoefficientClimb) {
		this.pressureCoefficientClimb = pressureCoefficientClimb;
	}

	public Amount<Velocity> getTasClimb() {
		return tasClimb;
	}

	public void setTasClimb(Amount<Velocity> tasClimb) {
		this.tasClimb = tasClimb;
	}

	public Amount<Velocity> getCasClimb() {
		return casClimb;
	}

	public void setCasClimb(Amount<Velocity> casClimb) {
		this.casClimb = casClimb;
	}

	public Amount<Velocity> getEasClimb() {
		return easClimb;
	}

	public void setEasClimb(Amount<Velocity> easClimb) {
		this.easClimb = easClimb;
	}

	public Amount<VolumetricDensity> getDensityClimb() {
		return densityClimb;
	}

	public void setDensityClimb(Amount<VolumetricDensity> densityClimb) {
		this.densityClimb = densityClimb;
	}

	public Amount<Pressure> getStaticPressureClimb() {
		return staticPressureClimb;
	}

	public void setStaticPressureClimb(Amount<Pressure> staticPressureClimb) {
		this.staticPressureClimb = staticPressureClimb;
	}

	public Amount<Pressure> getDynamicPressureClimb() {
		return dynamicPressureClimb;
	}

	public void setDynamicPressureClimb(Amount<Pressure> dynamicPressureClimb) {
		this.dynamicPressureClimb = dynamicPressureClimb;
	}

	public Amount<Pressure> getStagnationPressureClimb() {
		return stagnationPressureClimb;
	}

	public void setStagnationPressureClimb(Amount<Pressure> stagnationPressureClimb) {
		this.stagnationPressureClimb = stagnationPressureClimb;
	}

	public Amount<Pressure> getMaxDeltaPressureClimb() {
		return maxDeltaPressureClimb;
	}

	public void setMaxDeltaPressureClimb(Amount<Pressure> maxDeltaPressureClimb) {
		this.maxDeltaPressureClimb = maxDeltaPressureClimb;
	}

	public Amount<Pressure> getMaxDynamicPressureClimb() {
		return maxDynamicPressureClimb;
	}

	public void setMaxDynamicPressureClimb(Amount<Pressure> maxDynamicPressureClimb) {
		this.maxDynamicPressureClimb = maxDynamicPressureClimb;
	}

	public Amount<DynamicViscosity> getMuClimb() {
		return muClimb;
	}

	public void setMuClimb(Amount<DynamicViscosity> muClimb) {
		this.muClimb = muClimb;
	}

	public Amount<Temperature> getStaticTemperatureClimb() {
		return staticTemperatureClimb;
	}

	public void setStaticTemperatureClimb(Amount<Temperature> staticTemperatureClimb) {
		this.staticTemperatureClimb = staticTemperatureClimb;
	}

	public Amount<Temperature> getStagnationTemperatureClimb() {
		return stagnationTemperatureClimb;
	}

	public void setStagnationTemperatureClimb(Amount<Temperature> stagnationTemperatureClimb) {
		this.stagnationTemperatureClimb = stagnationTemperatureClimb;
	}

	public StdAtmos1976 getAtmosphereCruise() {
		return atmosphereCruise;
	}

	public void setAtmosphereCruise(StdAtmos1976 atmosphereCruise) {
		this.atmosphereCruise = atmosphereCruise;
	}

	public Double getPressureCoefficientCruise() {
		return pressureCoefficientCruise;
	}

	public void setPressureCoefficientCruise(Double pressureCoefficientCruise) {
		this.pressureCoefficientCruise = pressureCoefficientCruise;
	}

	public Amount<Velocity> getTasCruise() {
		return tasCruise;
	}

	public void setTasCruise(Amount<Velocity> tasCruise) {
		this.tasCruise = tasCruise;
	}

	public Amount<Velocity> getCasCruise() {
		return casCruise;
	}

	public void setCasCruise(Amount<Velocity> casCruise) {
		this.casCruise = casCruise;
	}

	public Amount<Velocity> getEasCruise() {
		return easCruise;
	}

	public void setEasCruise(Amount<Velocity> easCruise) {
		this.easCruise = easCruise;
	}

	public Amount<VolumetricDensity> getDensityCruise() {
		return densityCruise;
	}

	public void setDensityCruise(Amount<VolumetricDensity> densityCruise) {
		this.densityCruise = densityCruise;
	}

	public Amount<Pressure> getStaticPressureCruise() {
		return staticPressureCruise;
	}

	public void setStaticPressureCruise(Amount<Pressure> staticPressureCruise) {
		this.staticPressureCruise = staticPressureCruise;
	}

	public Amount<Pressure> getDynamicPressureCruise() {
		return dynamicPressureCruise;
	}

	public void setDynamicPressureCruise(Amount<Pressure> dynamicPressureCruise) {
		this.dynamicPressureCruise = dynamicPressureCruise;
	}

	public Amount<Pressure> getStagnationPressureCruise() {
		return stagnationPressureCruise;
	}

	public void setStagnationPressureCruise(Amount<Pressure> stagnationPressureCruise) {
		this.stagnationPressureCruise = stagnationPressureCruise;
	}

	public Amount<Pressure> getMaxDeltaPressureCruise() {
		return maxDeltaPressureCruise;
	}

	public void setMaxDeltaPressureCruise(Amount<Pressure> maxDeltaPressureCruise) {
		this.maxDeltaPressureCruise = maxDeltaPressureCruise;
	}

	public Amount<Pressure> getMaxDynamicPressureCruise() {
		return maxDynamicPressureCruise;
	}

	public void setMaxDynamicPressureCruise(Amount<Pressure> maxDynamicPressureCruise) {
		this.maxDynamicPressureCruise = maxDynamicPressureCruise;
	}

	public Amount<DynamicViscosity> getMuCruise() {
		return muCruise;
	}

	public void setMuCruise(Amount<DynamicViscosity> muCruise) {
		this.muCruise = muCruise;
	}

	public Amount<Temperature> getStaticTemperatureCruise() {
		return staticTemperatureCruise;
	}

	public void setStaticTemperatureCruise(Amount<Temperature> staticTemperatureCruise) {
		this.staticTemperatureCruise = staticTemperatureCruise;
	}

	public Amount<Temperature> getStagnationTemperatureCruise() {
		return stagnationTemperatureCruise;
	}

	public void setStagnationTemperatureCruise(Amount<Temperature> stagnationTemperatureCruise) {
		this.stagnationTemperatureCruise = stagnationTemperatureCruise;
	}

	public StdAtmos1976 getAtmosphereTakeOff() {
		return atmosphereTakeOff;
	}

	public void setAtmosphereTakeOff(StdAtmos1976 atmosphereTakeOff) {
		this.atmosphereTakeOff = atmosphereTakeOff;
	}

	public Double getPressureCoefficientTakeOff() {
		return pressureCoefficientTakeOff;
	}

	public void setPressureCoefficientTakeOff(Double pressureCoefficientTakeOff) {
		this.pressureCoefficientTakeOff = pressureCoefficientTakeOff;
	}

	public Amount<Velocity> getTasTakeOff() {
		return tasTakeOff;
	}

	public void setTasTakeOff(Amount<Velocity> tasTakeOff) {
		this.tasTakeOff = tasTakeOff;
	}

	public Amount<Velocity> getCasTakeOff() {
		return casTakeOff;
	}

	public void setCasTakeOff(Amount<Velocity> casTakeOff) {
		this.casTakeOff = casTakeOff;
	}

	public Amount<Velocity> getEasTakeOff() {
		return easTakeOff;
	}

	public void setEasTakeOff(Amount<Velocity> easTakeOff) {
		this.easTakeOff = easTakeOff;
	}

	public Amount<VolumetricDensity> getDensityTakeOff() {
		return densityTakeOff;
	}

	public void setDensityTakeOff(Amount<VolumetricDensity> densityTakeOff) {
		this.densityTakeOff = densityTakeOff;
	}

	public Amount<Pressure> getStaticPressureTakeOff() {
		return staticPressureTakeOff;
	}

	public void setStaticPressureTakeOff(Amount<Pressure> staticPressureTakeOff) {
		this.staticPressureTakeOff = staticPressureTakeOff;
	}

	public Amount<Pressure> getDynamicPressureTakeOff() {
		return dynamicPressureTakeOff;
	}

	public void setDynamicPressureTakeOff(Amount<Pressure> dynamicPressureTakeOff) {
		this.dynamicPressureTakeOff = dynamicPressureTakeOff;
	}

	public Amount<Pressure> getStagnationPressureTakeOff() {
		return stagnationPressureTakeOff;
	}

	public void setStagnationPressureTakeOff(Amount<Pressure> stagnationPressureTakeOff) {
		this.stagnationPressureTakeOff = stagnationPressureTakeOff;
	}

	public Amount<Pressure> getMaxDeltaPressureTakeOff() {
		return maxDeltaPressureTakeOff;
	}

	public void setMaxDeltaPressureTakeOff(Amount<Pressure> maxDeltaPressureTakeOff) {
		this.maxDeltaPressureTakeOff = maxDeltaPressureTakeOff;
	}

	public Amount<Pressure> getMaxDynamicPressureTakeOff() {
		return maxDynamicPressureTakeOff;
	}

	public void setMaxDynamicPressureTakeOff(Amount<Pressure> maxDynamicPressureTakeOff) {
		this.maxDynamicPressureTakeOff = maxDynamicPressureTakeOff;
	}

	public Amount<DynamicViscosity> getMuTakeOff() {
		return muTakeOff;
	}

	public void setMuTakeOff(Amount<DynamicViscosity> muTakeOff) {
		this.muTakeOff = muTakeOff;
	}

	public Amount<Temperature> getStaticTemperatureTakeOff() {
		return staticTemperatureTakeOff;
	}

	public void setStaticTemperatureTakeOff(Amount<Temperature> staticTemperatureTakeOff) {
		this.staticTemperatureTakeOff = staticTemperatureTakeOff;
	}

	public Amount<Temperature> getStagnationTemperatureTakeOff() {
		return stagnationTemperatureTakeOff;
	}

	public void setStagnationTemperatureTakeOff(Amount<Temperature> stagnationTemperatureTakeOff) {
		this.stagnationTemperatureTakeOff = stagnationTemperatureTakeOff;
	}

	public StdAtmos1976 getAtmosphereLanding() {
		return atmosphereLanding;
	}

	public void setAtmosphereLanding(StdAtmos1976 atmosphereLanding) {
		this.atmosphereLanding = atmosphereLanding;
	}

	public Double getPressureCoefficientLanding() {
		return pressureCoefficientLanding;
	}

	public void setPressureCoefficientLanding(Double pressureCoefficientLanding) {
		this.pressureCoefficientLanding = pressureCoefficientLanding;
	}

	public Amount<Velocity> getTasLanding() {
		return tasLanding;
	}

	public void setTasLanding(Amount<Velocity> tasLanding) {
		this.tasLanding = tasLanding;
	}

	public Amount<Velocity> getCasLanding() {
		return casLanding;
	}

	public void setCasLanding(Amount<Velocity> casLanding) {
		this.casLanding = casLanding;
	}

	public Amount<Velocity> getEasLanding() {
		return easLanding;
	}

	public void setEasLanding(Amount<Velocity> easLanding) {
		this.easLanding = easLanding;
	}

	public Amount<VolumetricDensity> getDensityLanding() {
		return densityLanding;
	}

	public void setDensityLanding(Amount<VolumetricDensity> densityLanding) {
		this.densityLanding = densityLanding;
	}

	public Amount<Pressure> getStaticPressureLanding() {
		return staticPressureLanding;
	}

	public void setStaticPressureLanding(Amount<Pressure> staticPressureLanding) {
		this.staticPressureLanding = staticPressureLanding;
	}

	public Amount<Pressure> getDynamicPressureLanding() {
		return dynamicPressureLanding;
	}

	public void setDynamicPressureLanding(Amount<Pressure> dynamicPressureLanding) {
		this.dynamicPressureLanding = dynamicPressureLanding;
	}

	public Amount<Pressure> getStagnationPressureLanding() {
		return stagnationPressureLanding;
	}

	public void setStagnationPressureLanding(Amount<Pressure> stagnationPressureLanding) {
		this.stagnationPressureLanding = stagnationPressureLanding;
	}

	public Amount<Pressure> getMaxDeltaPressureLanding() {
		return maxDeltaPressureLanding;
	}

	public void setMaxDeltaPressureLanding(Amount<Pressure> maxDeltaPressureLanding) {
		this.maxDeltaPressureLanding = maxDeltaPressureLanding;
	}

	public Amount<Pressure> getMaxDynamicPressureLanding() {
		return maxDynamicPressureLanding;
	}

	public void setMaxDynamicPressureLanding(Amount<Pressure> maxDynamicPressureLanding) {
		this.maxDynamicPressureLanding = maxDynamicPressureLanding;
	}

	public Amount<DynamicViscosity> getMuLanding() {
		return muLanding;
	}

	public void setMuLanding(Amount<DynamicViscosity> muLanding) {
		this.muLanding = muLanding;
	}

	public Amount<Temperature> getStaticTemperatureLanding() {
		return staticTemperatureLanding;
	}

	public void setStaticTemperatureLanding(Amount<Temperature> staticTemperatureLanding) {
		this.staticTemperatureLanding = staticTemperatureLanding;
	}

	public Amount<Temperature> getStagnationTemperatureLanding() {
		return stagnationTemperatureLanding;
	}

	public void setStagnationTemperatureLanding(Amount<Temperature> stagnationTemperatureLanding) {
		this.stagnationTemperatureLanding = stagnationTemperatureLanding;
	}

	public Amount<Angle> getAlphaClimb() {
		return theOperatingConditionsInterface.getAlphaClimb();
	}

	public void setAlphaClimb(Amount<Angle> alphaClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAlphaClimb(alphaClimb).build());
	}

	public Amount<Angle> getBetaClimb() {
		return theOperatingConditionsInterface.getBetaClimb();
	}

	public void setBetaClimb(Amount<Angle> betaClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setBetaClimb(betaClimb).build());
	}

	public double getMachClimb() {
		return theOperatingConditionsInterface.getMachClimb();
	}

	public void setMachClimb(double machClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setMachClimb(machClimb).build());
	}

	public Amount<Length> getAltitudeClimb() {
		return theOperatingConditionsInterface.getAltitudeClimb();
	}

	public void setAltitudeClimb(Amount<Length> altitudeClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAltitudeClimb(altitudeClimb).build());
	}

	public Amount<Temperature> getDeltaTemperatureClimb() {
		return theOperatingConditionsInterface.getDeltaTemperatureClimb();
	}

	public void setDeltaTemperatureClimb(Amount<Temperature> deltaTemperatureClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setDeltaTemperatureClimb(deltaTemperatureClimb).build());
	}

	public double getThrottleClimb() {
		return theOperatingConditionsInterface.getThrottleClimb();
	}

	public void setThrottleClimb(double throttleClimb) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setThrottleClimb(throttleClimb).build());
	}

	public Amount<Angle> getAlphaCruise() {
		return theOperatingConditionsInterface.getAlphaCruise();
	}

	public void setAlphaCruise(Amount<Angle> alphaCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAlphaCruise(alphaCruise).build());
	}

	public Amount<Angle> getBetaCruise() {
		return theOperatingConditionsInterface.getBetaCruise();
	}

	public void setBetaCruise(Amount<Angle> betaCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setBetaCruise(betaCruise).build());
	}

	public double getMachCruise() {
		return theOperatingConditionsInterface.getMachCruise();
	}

	public void setMachCruise(double machCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setMachCruise(machCruise).build());
	}

	public Amount<Length> getAltitudeCruise() {
		return theOperatingConditionsInterface.getAltitudeCruise();
	}

	public void setAltitudeCruise(Amount<Length> altitudeCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAltitudeCruise(altitudeCruise).build());
	}

	public Amount<Temperature> getDeltaTemperatureCruise() {
		return theOperatingConditionsInterface.getDeltaTemperatureCruise();
	}

	public void setDeltaTemperatureCruise(Amount<Temperature> deltaTemperatureCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setDeltaTemperatureCruise(deltaTemperatureCruise).build());
	}

	public double getThrottleCruise() {
		return theOperatingConditionsInterface.getThrottleCruise();
	}

	public void setThrottleCruise(double throttleCruise) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setThrottleCruise(throttleCruise).build());
	}

	public Amount<Angle> getAlphaTakeOff() {
		return theOperatingConditionsInterface.getAlphaTakeOff();
	}

	public void setAlphaTakeOff(Amount<Angle> alphaTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAlphaTakeOff(alphaTakeOff).build());
	}

	public Amount<Angle> getBetaTakeOff() {
		return theOperatingConditionsInterface.getBetaTakeOff();
	}

	public void setBetaTakeOff(Amount<Angle> betaTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setBetaTakeOff(betaTakeOff).build());
	}

	public double getMachTakeOff() {
		return theOperatingConditionsInterface.getMachTakeOff();
	}

	public void setMachTakeOff(double machTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setMachTakeOff(machTakeOff).build());
	}

	public Amount<Length> getAltitudeTakeOff() {
		return theOperatingConditionsInterface.getAltitudeTakeOff();
	}

	public void setAltitudeTakeOff(Amount<Length> altitudeTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAltitudeTakeOff(altitudeTakeOff).build());
	}

	public Amount<Temperature> getDeltaTemperatureTakeOff() {
		return theOperatingConditionsInterface.getDeltaTemperatureTakeOff();
	}

	public void setDeltaTemperatureTakeOff(Amount<Temperature> deltaTemperatureTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setDeltaTemperatureTakeOff(deltaTemperatureTakeOff).build());
	}

	public double getThrottleTakeOff() {
		return theOperatingConditionsInterface.getThrottleTakeOff();
	}

	public void setThrottleTakeOff(double throttleTakeOff) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setThrottleTakeOff(throttleTakeOff).build());
	}

	public List<Amount<Angle>> getTakeOffFlapDefletctionList() {
		return theOperatingConditionsInterface.getTakeOffFlapDefletctionList();
	}

	public void setTakeOffFlapDefletctionList(List<Amount<Angle>> takeOffFlapDefletctionList) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface)
				.clearTakeOffFlapDefletctionList()
				.addAllTakeOffFlapDefletctionList(takeOffFlapDefletctionList)
				.build());
	}

	public List<Amount<Angle>> getTakeOffSlatDefletctionList() {
		return theOperatingConditionsInterface.getTakeOffSlatDefletctionList();
	}

	public void setTakeOffSlatDefletctionList(List<Amount<Angle>> takeOffSlatDefletctionList) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface)
				.clearTakeOffSlatDefletctionList()
				.addAllTakeOffSlatDefletctionList(takeOffSlatDefletctionList)
				.build());
	}

	public Amount<Angle> getAlphaLanding() {
		return theOperatingConditionsInterface.getAlphaLanding();
	}

	public void setAlphaLanding(Amount<Angle> alphaLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAlphaLanding(alphaLanding).build());
	}

	public Amount<Angle> getBetaLanding() {
		return theOperatingConditionsInterface.getBetaLanding();
	}

	public void setBetaLanding(Amount<Angle> betaLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setBetaLanding(betaLanding).build());
	}

	public double getMachLanding() {
		return theOperatingConditionsInterface.getMachLanding();
	}

	public void setMachLanding(double machLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setMachLanding(machLanding).build());
	}

	public Amount<Length> getAltitudeLanding() {
		return theOperatingConditionsInterface.getAltitudeLanding();
	}

	public void setAltitudeLanding(Amount<Length> altitudeLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setAltitudeLanding(altitudeLanding).build());
	}

	public Amount<Temperature> getDeltaTemperatureLanding() {
		return theOperatingConditionsInterface.getDeltaTemperatureLanding();
	}

	public void setDeltaTemperatureLanding(Amount<Temperature> deltaTemperatureLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setDeltaTemperatureLanding(deltaTemperatureLanding).build());
	}

	public double getThrottleLanding() {
		return theOperatingConditionsInterface.getThrottleLanding();
	}

	public void setThrottleLanding(double throttleLanding) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface).setThrottleLanding(throttleLanding).build());
	}

	public List<Amount<Angle>> getLandingFlapDefletctionList() {
		return theOperatingConditionsInterface.getLandingFlapDefletctionList();
	}

	public void setLandingFlapDefletctionList(List<Amount<Angle>> landingFlapDefletctionList) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface)
				.clearLandingFlapDefletctionList()
				.addAllLandingFlapDefletctionList(landingFlapDefletctionList)
				.build());
	}

	public List<Amount<Angle>> getLandingSlatDefletctionList() {
		return theOperatingConditionsInterface.getLandingSlatDefletctionList();
	}

	public void setLandingSlatDefletctionList(List<Amount<Angle>> landingSlatDefletctionList) {
		setTheOperatingConditionsInterface(IOperatingConditions.Builder.from(theOperatingConditionsInterface)
				.clearLandingSlatDefletctionList()
				.addAllLandingSlatDefletctionList(landingSlatDefletctionList)
				.build());
	}

} 