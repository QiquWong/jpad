package analyses;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

/**
 * All the computations are managed by this class.
 * Do not use directly the methods contained in each component; instead, invoke always the methods 
 * contained in this class in order to be sure that each quantity is evaluated correctly. 
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 *
 */
public class ACAnalysisManager implements IACAnalysisManager {

	private String _id;
	private Aircraft _theAircraft;

	private ACWeightsManager _theWeights;
	private ACBalanceManager _theBalance;
	private Map<ConditionEnum, ACAerodynamicCalculator> _theAerodynamicAndStability;
	private ACPerformanceManager _thePerformance;
	private ACCostsManager _theCosts;
	
	// INPUT DATA: 
	private Double _positiveLimitLoadFactor;
	private Double _negativeLimitLoadFactor;
	private Double _cruiseCL;
	private Amount<Length> _maxAltitudeAtMaxSpeed;
	private Double _machMaxCruise;
	private Amount<Length> _altitudeOptimumCruise;
	private Double _machOptimumCruise;
	private Amount<Duration> _blockTime;
	private Amount<Duration> _flightTime;
	private Amount<Length> _referenceRange;
	
	// DEPENDENT VARIABLES: 
	private Double _nUltimate;
	private Amount<Velocity> _vDive;
	private Amount<Velocity> _vDiveEAS;
	private Double _machDive0;
	private Amount<Velocity> _vMaxCruise;
	private Amount<Velocity> _vMaxCruiseEAS;
	private Amount<Velocity> _vOptimumCruise;
	private Amount<Pressure> _maxDynamicPressure;
	
	private Map <ComponentEnum, MethodEnum> _methodsMapWeights;
	private Map <ComponentEnum, MethodEnum> _methodsMapBalance;
	private List<PerformanceEnum> _taskListPerformance;
	private List<ConditionEnum> _taskListAerodynamicAndStability;
	private Map<CostsEnum, MethodEnum> _taskListCosts;
	private Map <AnalysisTypeEnum, Boolean> _executedAnalysesMap;
	private List<AnalysisTypeEnum> _analysisList;
	private Boolean _plotBalance;
	private Boolean _plotAerodynamicAndStability;
	private Boolean _plotPerformance;
	private Boolean _plotCosts;
	
	private static File _weightsFileComplete;
	private static File _balanceFileComplete;
	private static File _aerodynamicAndStabilityTakeOffFileComplete;
	private static File _aerodynamicAndStabilityClimbFileComplete;
	private static File _aerodynamicAndStabilityCruiseFileComplete;
	private static File _aerodynamicAndStabilityLandingFileComplete;
	private static File _performanceFileComplete;
	private static File _costsFileComplete;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACAnalysisManagerBuilder {

		// required parameters
		private String __id;
		private Aircraft __theAircraft;

		// optional parameters ... defaults
		// ...
		private Double __positiveLimitLoadFactor;
		private Double __negativeLimitLoadFactor;
		private Double __cruiseCL;
		private Amount<Length> __maxAltitudeAtMaxSpeed;
		private Double __machMaxCruise;
		private Amount<Length> __altitudeOptimumCruise;
		private Double __machOptimumCruise;
		private Amount<Duration> __blockTime;
		private Amount<Duration> __flightTime;
		private Amount<Length> __referenceRange;
		
		private Map <ComponentEnum, MethodEnum> __methodsMapWeights = new HashMap<ComponentEnum, MethodEnum>();
		private Map <ComponentEnum, MethodEnum> __methodsMapBalance = new HashMap<ComponentEnum, MethodEnum>();
		private List<PerformanceEnum> __taskListPerfromance = new ArrayList<PerformanceEnum>();
		private List<ConditionEnum> __taskListAerodynamicAndStability = new ArrayList<ConditionEnum>();
		private Map <CostsEnum, MethodEnum> __taskListCosts = new HashMap<>();
		private Map <AnalysisTypeEnum, Boolean> __executedAnalysesMap = new HashMap<AnalysisTypeEnum, Boolean>();
		private List<AnalysisTypeEnum> __analysisList = new ArrayList<AnalysisTypeEnum>();
		
		private Boolean __plotBalance = Boolean.FALSE;
		private Boolean __plotAerodynamicAndStability = Boolean.FALSE;
		private Boolean __plotPerformance = Boolean.FALSE;
		private Boolean __plotCosts = Boolean.FALSE;
		
		public ACAnalysisManagerBuilder id (String id) {
			this.__id = id;
			return this;
		}
		
		public ACAnalysisManagerBuilder aircraft (Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
			return this;
		}
		
		public ACAnalysisManagerBuilder analysisList (List<AnalysisTypeEnum> analysisList) {
			this.__analysisList = analysisList;
			return this;
		}
		
		public ACAnalysisManagerBuilder plotBalance (Boolean plotBalance){
			this.__plotBalance = plotBalance;
			return this;
		}
		
		public ACAnalysisManagerBuilder plotAerodynamicAndStability (Boolean plotAerodynamicAndStability){
			this.__plotAerodynamicAndStability = plotAerodynamicAndStability;
			return this;
		}
		
		public ACAnalysisManagerBuilder plotPerformance (Boolean plotPerformance){
			this.__plotPerformance = plotPerformance;
			return this;
		}
		
		public ACAnalysisManagerBuilder plotCosts (Boolean plotCosts){
			this.__plotCosts = plotCosts;
			return this;
		}
		
		public ACAnalysisManagerBuilder positiveLimitLoadFactor (Double nLimit) {
			this.__positiveLimitLoadFactor = nLimit;
			return this;
		}
		
		public ACAnalysisManagerBuilder negativeLimitLoadFactor (Double nLimitNeg) {
			this.__negativeLimitLoadFactor = nLimitNeg;
			return this;
		}
		
		public ACAnalysisManagerBuilder referenceRange (Amount<Length> refernceRange) {
			this.__referenceRange = refernceRange;
			return this;
		}
 		
		public ACAnalysisManagerBuilder cruiseCL (Double cruiseCL) {
			this.__cruiseCL = cruiseCL;
			return this;
		}
		
		public ACAnalysisManagerBuilder maxAltitudeAtMaxSpeed (Amount<Length> maxAltitudeAtMaxSpeed) {
			this.__maxAltitudeAtMaxSpeed = maxAltitudeAtMaxSpeed;
			return this;
		}
		
		public ACAnalysisManagerBuilder machMaxCruise (Double machMaxCruise) {
			this.__machMaxCruise = machMaxCruise;
			return this;
		}
		
		public ACAnalysisManagerBuilder altitudeOptimumCruise (Amount<Length> altitudeOptimumCruise) {
			this.__altitudeOptimumCruise = altitudeOptimumCruise;
			return this;
		}
		
		public ACAnalysisManagerBuilder machOptimumCruise (Double machOptimumCruise) {
			this.__machOptimumCruise = machOptimumCruise;
			return this;
		}
		
		public ACAnalysisManagerBuilder blockTime (Amount<Duration> blockTime) {
			this.__blockTime = blockTime;
			return this;
		}
		
		public ACAnalysisManagerBuilder flightTime (Amount<Duration> flightTime) {
			this.__flightTime = flightTime;
			return this;
		}
		
		public ACAnalysisManagerBuilder methodsMapWeights (Map<ComponentEnum, MethodEnum> methodsMapWeights) {
			this.__methodsMapWeights = methodsMapWeights;
			return this;
		}
		
		public ACAnalysisManagerBuilder methodsMapBalance (Map<ComponentEnum, MethodEnum> methodsMapBalance) {
			this.__methodsMapBalance = methodsMapBalance;
			return this;
		}
		
		public ACAnalysisManagerBuilder taskListPerfromance (List<PerformanceEnum> taskListPerfromance) {
			this.__taskListPerfromance = taskListPerfromance;
			return this;
		}
		
		public ACAnalysisManagerBuilder taskListAerodynamicAndStability (List<ConditionEnum> taskListAerodynamicAndStability) {
			this.__taskListAerodynamicAndStability = taskListAerodynamicAndStability;
			return this;
		}
		
		public ACAnalysisManagerBuilder taskListCosts (Map<CostsEnum, MethodEnum> taskListCosts) {
			this.__taskListCosts = taskListCosts;
			return this;
		}
		
		public ACAnalysisManagerBuilder(String id, Aircraft theAircraft) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			initializeDefaultData(AircraftEnum.ATR72);
		}
		
		public ACAnalysisManagerBuilder(String id, Aircraft theAircraft, AircraftEnum aircraftName) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			initializeDefaultData(aircraftName);
		}

		@SuppressWarnings("incomplete-switch")
		private void initializeDefaultData(AircraftEnum aircraftName) {
			
			switch(aircraftName) {
			case ATR72:
				__positiveLimitLoadFactor = 2.5;
				__negativeLimitLoadFactor = -1.0;
				__maxAltitudeAtMaxSpeed = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);
				__machMaxCruise = 0.45;
				__altitudeOptimumCruise = Amount.valueOf(16000., NonSI.FOOT).to(SI.METER);
				__machOptimumCruise = 0.43;
				__blockTime = Amount.valueOf(1.5, NonSI.HOUR);
				__flightTime = Amount.valueOf(1.35, NonSI.HOUR);
				__cruiseCL = 0.45;
				__referenceRange = Amount.valueOf(1528., SI.KILOMETER); 
				break;
				
			case B747_100B:
				__positiveLimitLoadFactor = 2.5;
				__negativeLimitLoadFactor = -1.0;
				__maxAltitudeAtMaxSpeed = Amount.valueOf(35000., NonSI.FOOT).to(SI.METER);
				__machMaxCruise = 0.89;
				__altitudeOptimumCruise = Amount.valueOf(35000., NonSI.FOOT).to(SI.METER);
				__machOptimumCruise = 0.84;
				__blockTime = Amount.valueOf(1.5, NonSI.HOUR);
				__flightTime = Amount.valueOf(1.35, NonSI.HOUR);
				__cruiseCL = 0.58;
				__referenceRange = Amount.valueOf(9800., SI.KILOMETER);
				break;
				
			case AGILE_DC1:
				__positiveLimitLoadFactor = 2.5;
				__negativeLimitLoadFactor = -1.0;
				__maxAltitudeAtMaxSpeed = Amount.valueOf(36000., NonSI.FOOT).to(SI.METER);
				__machMaxCruise = 0.82;
				__altitudeOptimumCruise = Amount.valueOf(36000., NonSI.FOOT).to(SI.METER);
				__machOptimumCruise = 0.78;
				__blockTime = Amount.valueOf(2.6, NonSI.HOUR);
				__flightTime = Amount.valueOf(2.33, NonSI.HOUR);
				__cruiseCL = 0.5;
				__referenceRange = Amount.valueOf(3500., SI.KILOMETER);
				break;
			}
		}
		
		public ACAnalysisManager build() {
			return new ACAnalysisManager(this); 
		}
	}

	private ACAnalysisManager(ACAnalysisManagerBuilder builder) {
		
		this._id = builder.__id;
		this._theAircraft = builder.__theAircraft;
		this._positiveLimitLoadFactor = builder.__positiveLimitLoadFactor;
		this._negativeLimitLoadFactor = builder.__negativeLimitLoadFactor;
		this._cruiseCL = builder.__cruiseCL;
		this._maxAltitudeAtMaxSpeed = builder.__maxAltitudeAtMaxSpeed;
		this._machMaxCruise = builder.__machMaxCruise;
		this._altitudeOptimumCruise = builder.__altitudeOptimumCruise;
		this._machOptimumCruise = builder.__machOptimumCruise;
		this._blockTime = builder.__blockTime;
		this._flightTime = builder.__flightTime;
		this._referenceRange = builder.__referenceRange;
		
		this._methodsMapWeights = builder.__methodsMapWeights;
		this._methodsMapBalance = builder.__methodsMapBalance;
		this._taskListPerformance = builder.__taskListPerfromance;
		this._taskListAerodynamicAndStability = builder.__taskListAerodynamicAndStability;
		this._taskListCosts = builder.__taskListCosts;
		this._executedAnalysesMap = builder.__executedAnalysesMap;
		this._analysisList = builder.__analysisList;
		
		this._plotBalance = builder.__plotBalance;
		this._plotAerodynamicAndStability = builder.__plotAerodynamicAndStability;
		this._plotPerformance = builder.__plotPerformance;
		this._plotCosts = builder.__plotCosts;
		
		calculateDependentVariables();
		
		//-------------------------------------------------
		// EXECUTED ANALYSIS MAP INITIALIZATION
		this._executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, false);
		this._executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, false);
		this._executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, false);
		this._executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, false);
		this._executedAnalysesMap.put(AnalysisTypeEnum.COSTS, false);
	}

	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
		
	@SuppressWarnings("unchecked")
	public static ACAnalysisManager importFromXML (String pathToXML, Aircraft theAircraft) throws IOException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		List<AnalysisTypeEnum> analysisList = new ArrayList<>();
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		Double positiveLimitLoadFactor = null;
		Double negativeLimitLoadFactor = null;
		Double cruiseCL = null; 
		Amount<Length> maxAltitudeMaxSpeed = null;
		Double maxCruiseMach = null;
		Amount<Length> optimumCruiseAltitude = null;
		Double optimumCruiseMach = null;
		Amount<Duration> blockTime = null;
		Amount<Duration> flightTime = null;
		Amount<Length> referenceRange = null;

		//-------------------------------------------------------------------------------------
		// POSITIVE LIMIT LOAD FACTOR
		String positiveLimitLoadFactorProperty = reader.getXMLPropertyByPath("//global_data/positive_limit_load_factor");
		if(positiveLimitLoadFactorProperty != null)
			positiveLimitLoadFactor = Double.valueOf(reader.getXMLPropertyByPath("//global_data/positive_limit_load_factor"));
		//-------------------------------------------------------------------------------------
		// NEGATIVE LIMIT LOAD FACTOR
		String negativeLimitLoadFactorProperty = reader.getXMLPropertyByPath("//global_data/negative_limit_load_factor");
		if(negativeLimitLoadFactorProperty != null)
			negativeLimitLoadFactor = Double.valueOf(reader.getXMLPropertyByPath("//global_data/negative_limit_load_factor"));
		//-------------------------------------------------------------------------------------
		// CRUISE LIFT COEFFICIENT
		String cruiseLiftCoefficientProperty = reader.getXMLPropertyByPath("//global_data/cruise_lift_coefficient");
		if(cruiseLiftCoefficientProperty != null)
			cruiseCL = Double.valueOf(reader.getXMLPropertyByPath("//global_data/cruise_lift_coefficient"));
		//-------------------------------------------------------------------------------------
		// MAX ALTITUDE AT MAX SPEED
		String maxAltitudeMaxSpeedProperty = reader.getXMLPropertyByPath("//global_data/maximum_altitude_at_maximum_speed");
		if(maxAltitudeMaxSpeedProperty != null)
			maxAltitudeMaxSpeed = reader.getXMLAmountLengthByPath("//global_data/maximum_altitude_at_maximum_speed");
		//-------------------------------------------------------------------------------------
		// MAX CRUISE MACH NUMBER
		String maxCruiseMachProperty = reader.getXMLPropertyByPath("//global_data/maximum_cruise_mach_number");
		if(maxCruiseMachProperty != null)
			maxCruiseMach = Double.valueOf(reader.getXMLPropertyByPath("//global_data/maximum_cruise_mach_number"));
		//-------------------------------------------------------------------------------------
		// OPTIMUM CRUISE ALTITUDE
		String optimumCruiseAltitudeProperty = reader.getXMLPropertyByPath("//global_data/optimum_cruise_altitude");
		if(optimumCruiseAltitudeProperty != null)
			optimumCruiseAltitude = reader.getXMLAmountLengthByPath("//global_data/optimum_cruise_altitude");
		//-------------------------------------------------------------------------------------
		// OPTIMUM CRUISE MACH NUMBER
		String optimumCruiseMachProperty = reader.getXMLPropertyByPath("//global_data/optimum_cruise_mach_number");
		if(optimumCruiseMachProperty != null)
			optimumCruiseMach = Double.valueOf(reader.getXMLPropertyByPath("//global_data/optimum_cruise_mach_number"));
		//-------------------------------------------------------------------------------------
		// BLOCK TIME
		String blockTimeProperty = reader.getXMLPropertyByPath("//global_data/block_time");
		if(blockTimeProperty != null)
			blockTime = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//global_data/block_time");
		//-------------------------------------------------------------------------------------
		// FLIGHT TIME
		String flightTimeProperty = reader.getXMLPropertyByPath("//global_data/flight_time");
		if(flightTimeProperty != null)
			flightTime = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//global_data/flight_time");
		//-------------------------------------------------------------------------------------
		// REFERENCE RANGE
		String referenceRangeProperty = reader.getXMLPropertyByPath("//global_data/block_time");
		if(referenceRangeProperty != null)
			referenceRange = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//global_data/reference_range")), NonSI.NAUTICAL_MILE);
		
		//-------------------------------------------------------------------------------------------
		// WEIGHTS ANALYSIS:
		Map<ComponentEnum, MethodEnum> methodsMapWeights = new HashMap<>();
		
		NodeList weightsTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//weights");
		
		if(weightsTag != null) {

			String weightsFile = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@file");

			if(weightsFile != null) {

				analysisList.add(AnalysisTypeEnum.WEIGHTS);

				////////////////////////////////////////////////////////////////////////////////////
				String fuselageWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_fuselage");
				if (fuselageWeightsMethod != null) {
					if(fuselageWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.RAYMER);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.TORENBEEK_1976);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.TORENBEEK_2013);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.JENKINSON);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.KROO);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("SADRAY")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.SADRAY);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("NICOLAI_1984")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.NICOLAI_1984);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.ROSKAM);
					}
					else 
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String wingWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_wing");
				if(wingWeightsMethod != null) {
					if(wingWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.KROO);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.JENKINSON);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.TORENBEEK_2013);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.TORENBEEK_1982);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.RAYMER);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("SADRAY")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.SADRAY);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.ROSKAM);
					}
					else 
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////			
				String hTailWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_htail");
				if(hTailWeightsMethod != null) {
					if(hTailWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.KROO);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.JENKINSON);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_2013);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_1982);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.RAYMER);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("SADRAY")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.SADRAY);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.ROSKAM);
					}
					else 
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String vTailWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_vtail");
				if(vTailWeightsMethod != null) {
					if(vTailWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.KROO);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.JENKINSON);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_2013);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_1982);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.RAYMER);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("SADRAY")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.SADRAY);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.ROSKAM);
					}
					else 
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String canardWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_canard");
				if(canardWeightsMethod != null) {
					if(canardWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.KROO);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.JENKINSON);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_2013);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_1982);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.RAYMER);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("SADRAY")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.SADRAY);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.ROSKAM);
					}
					else 
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String nacellesWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_nacelles");
				if(nacellesWeightsMethod != null) {
					if(nacellesWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.JENKINSON);
					}
					else if(nacellesWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1976);
					}
					else if(nacellesWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String landingGearsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_landing_gears");
				if(landingGearsWeightsMethod != null) {
					if(landingGearsWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.ROSKAM);
					}
					else if(landingGearsWeightsMethod.equalsIgnoreCase("STANFORD")) {
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.STANFORD);
					}
					else if(landingGearsWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.TORENBEEK_1982);
					}
					else if(landingGearsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.TORENBEEK_2013);
					}
					else 
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String systemsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_systems");
				if(systemsWeightsMethod != null) {
					if(systemsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.SYSTEMS, MethodEnum.TORENBEEK_2013);
					}
					else 
						methodsMapWeights.put(ComponentEnum.SYSTEMS, MethodEnum.AVERAGE);
				}
			}

			_weightsFileComplete = new File(
					MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
					+ File.separator 
					+ "Template_Analyses"
					+ File.separator
					+ weightsFile
					);
		}
		//-------------------------------------------------------------------------------------------
		// BALANCE ANALYSIS:
		Map<ComponentEnum, MethodEnum> methodsMapBalance = new HashMap<>();
		Boolean plotBalance = null;

		NodeList balanceTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//balance");
		Node balanceNode = balanceTag.item(0);

		if(balanceNode != null) {

			String balanceFile = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//balance/@file");

			String plotBalanceString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//balance/@plot");
			if(plotBalanceString.equalsIgnoreCase("FALSE") || plotBalanceString == null)
				plotBalance = Boolean.FALSE;
			else if(plotBalanceString.equalsIgnoreCase("TRUE"))
				plotBalance = Boolean.TRUE;

			if(balanceFile != null)  {		
				analysisList.add(AnalysisTypeEnum.BALANCE);

				////////////////////////////////////////////////////////////////////////////////////
				String fuselageBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_fuselage");
				if (fuselageBalanceMethod != null) {
					if(fuselageBalanceMethod.equalsIgnoreCase("SFORZA")) {
						methodsMapBalance.put(ComponentEnum.FUSELAGE, MethodEnum.SFORZA);
					}
					else if(fuselageBalanceMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapBalance.put(ComponentEnum.FUSELAGE, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapBalance.put(ComponentEnum.FUSELAGE, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String wingBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_wing");
				if(wingBalanceMethod != null) {
					if(wingBalanceMethod.equalsIgnoreCase("SFORZA")) {
						methodsMapBalance.put(ComponentEnum.WING, MethodEnum.SFORZA);
					}
					else if(wingBalanceMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapBalance.put(ComponentEnum.WING, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapBalance.put(ComponentEnum.WING, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String hTailBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_htail");
				if(hTailBalanceMethod != null) {
					if(hTailBalanceMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapBalance.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapBalance.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String vTailBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_vtail");
				if(vTailBalanceMethod != null) {
					if(vTailBalanceMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapBalance.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapBalance.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.AVERAGE);
				}

			}

			_balanceFileComplete = new File(
					MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
					+ File.separator 
					+ "Template_Analyses"
					+ File.separator
					+ balanceFile
					);

		}
		//-------------------------------------------------------------------------------------------
		// AERODYNAMIC ANALYSIS:
		List<ConditionEnum> taskListAerodynamicAndStability = new ArrayList<ConditionEnum>();
		Boolean plotAerodynamicAndStability = Boolean.FALSE;

		NodeList aerodynamicAndStabilityTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//aerodynamic_and_stability");
		Node aerodynamicAndStabilityNode = aerodynamicAndStabilityTag.item(0);

		if(aerodynamicAndStabilityNode != null) {

			analysisList.add(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY);
			
			String plotAerodynamicAndStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@plot");
			if(plotAerodynamicAndStabilityString.equalsIgnoreCase("FALSE")
					|| plotAerodynamicAndStabilityString == null)
				plotAerodynamicAndStability = Boolean.FALSE;
			else if(plotAerodynamicAndStabilityString.equalsIgnoreCase("TRUE"))
				plotAerodynamicAndStability = Boolean.TRUE;

			//---------------------------------------------------------------------------------------------------------
			// TAKE-OFF CONDITION
			String aerodynamicAndStabilityTakeOff = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@take_off_condition");
			
			if(aerodynamicAndStabilityTakeOff != null)  {		
				
				Boolean takeOffConditionFlag = null;
				if(aerodynamicAndStabilityTakeOff.equalsIgnoreCase("TRUE")) {
					takeOffConditionFlag = Boolean.TRUE;
				}
				else if(aerodynamicAndStabilityTakeOff.equalsIgnoreCase("FALSE") 
						|| aerodynamicAndStabilityTakeOff == null) {
					takeOffConditionFlag = Boolean.FALSE;
				}
				
				if(takeOffConditionFlag == Boolean.TRUE) 
					taskListAerodynamicAndStability.add(ConditionEnum.TAKE_OFF);

				String aerodynamicAndStabilityTakeOffFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aerodynamic_and_stability/@file_take_off_condition");
				
				_aerodynamicAndStabilityTakeOffFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ aerodynamicAndStabilityTakeOffFile
						);
			}
			
			//---------------------------------------------------------------------------------------------------------
			// CLIMB CONDITION
			String aerodynamicAndStabilityClimb = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@climb_condition");
			
			if(aerodynamicAndStabilityClimb != null)  {		
				
				Boolean climbConditionFlag = null;
				if(aerodynamicAndStabilityClimb.equalsIgnoreCase("TRUE")) {
					climbConditionFlag = Boolean.TRUE;
				}
				else if(aerodynamicAndStabilityClimb.equalsIgnoreCase("FALSE") 
						|| aerodynamicAndStabilityClimb == null) {
					climbConditionFlag = Boolean.FALSE;
				}
				
				if(climbConditionFlag == Boolean.TRUE) 
					taskListAerodynamicAndStability.add(ConditionEnum.CLIMB);

				String aerodynamicAndStabilityClimbFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aerodynamic_and_stability/@file_climb_condition");
				
				_aerodynamicAndStabilityClimbFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ aerodynamicAndStabilityClimbFile
						);
			}
			
			//---------------------------------------------------------------------------------------------------------
			// CRUISE CONDITION
			String aerodynamicAndStabilityCruise = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@cruise_condition");
			
			if(aerodynamicAndStabilityCruise != null)  {		
				
				Boolean cruiseConditionFlag = null;
				if(aerodynamicAndStabilityCruise.equalsIgnoreCase("TRUE")) {
					cruiseConditionFlag = Boolean.TRUE;
				}
				else if(aerodynamicAndStabilityCruise.equalsIgnoreCase("FALSE") 
						|| aerodynamicAndStabilityCruise == null) {
					cruiseConditionFlag = Boolean.FALSE;
				}
				
				if(cruiseConditionFlag == Boolean.TRUE) 
					taskListAerodynamicAndStability.add(ConditionEnum.CRUISE);

				String aerodynamicAndStabilityCruiseFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aerodynamic_and_stability/@file_cruise_condition");
				
				_aerodynamicAndStabilityCruiseFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ aerodynamicAndStabilityCruiseFile
						);
			}
			
			//---------------------------------------------------------------------------------------------------------
			// LANDING CONDITION
			String aerodynamicAndStabilityLanding = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@landing_condition");
			
			if(aerodynamicAndStabilityLanding != null)  {		
				
				Boolean landingConditionFlag = null;
				if(aerodynamicAndStabilityLanding.equalsIgnoreCase("TRUE")) {
					landingConditionFlag = Boolean.TRUE;
				}
				else if(aerodynamicAndStabilityLanding.equalsIgnoreCase("FALSE") 
						|| aerodynamicAndStabilityLanding == null) {
					landingConditionFlag = Boolean.FALSE;
				}
				
				if(landingConditionFlag == Boolean.TRUE) 
					taskListAerodynamicAndStability.add(ConditionEnum.LANDING);

				String aerodynamicAndStabilityLandingFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aerodynamic_and_stability/@file_landing_condition");
				
				_aerodynamicAndStabilityLandingFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ aerodynamicAndStabilityLandingFile
						);
			}
			
		}

		//-------------------------------------------------------------------------------------------
		// PERFORMANCE ANALYSIS:

		List<PerformanceEnum> taskListPerformance = new ArrayList<PerformanceEnum>();
		Boolean plotPerformance = Boolean.FALSE;

		NodeList performanceTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//performance");
		Node performanceNode = performanceTag.item(0);

		if(performanceNode != null) {

			String performanceFile = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//performance/@file");

			String plotPerfromanceString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//performance/@plot");
			if(plotPerfromanceString.equalsIgnoreCase("FALSE") 
					|| plotPerfromanceString == null)
				plotPerformance = Boolean.FALSE;
			else if(plotPerfromanceString.equalsIgnoreCase("TRUE"))
				plotPerformance = Boolean.TRUE;

			if(performanceFile != null)  {		
				analysisList.add(AnalysisTypeEnum.PERFORMANCE);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean takeOffFlag = Boolean.FALSE;
				String takeOffFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@take_off");
				if (takeOffFlagProperty != null) {
					if(takeOffFlagProperty.equalsIgnoreCase("TRUE")) {
						takeOffFlag = Boolean.TRUE;
					}
					else if(takeOffFlagProperty.equalsIgnoreCase("FALSE")) {
						takeOffFlag = Boolean.FALSE;
					}
				}
				if(takeOffFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.TAKE_OFF);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean climbFlag = Boolean.FALSE;
				String climbFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@climb");
				if (climbFlagProperty != null) {
					if(climbFlagProperty.equalsIgnoreCase("TRUE")) {
						climbFlag = Boolean.TRUE;
					}
					else if(climbFlagProperty.equalsIgnoreCase("FALSE")) {
						climbFlag = Boolean.FALSE;
					}
				}
				if(climbFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.CLIMB);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean cruiseFlag = Boolean.FALSE;
				String cruiseFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@cruise");
				if (cruiseFlagProperty != null) {
					if(cruiseFlagProperty.equalsIgnoreCase("TRUE")) {
						cruiseFlag = Boolean.TRUE;
					}
					else if(cruiseFlagProperty.equalsIgnoreCase("FALSE")) {
						cruiseFlag = Boolean.FALSE;
					}
				}
				if(cruiseFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.CRUISE);


				////////////////////////////////////////////////////////////////////////////////////
				Boolean descentFlag = Boolean.FALSE;
				String descentFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@descent");
				if (descentFlagProperty != null) {
					if(descentFlagProperty.equalsIgnoreCase("TRUE")) {
						descentFlag = Boolean.TRUE;
					}
					else if(descentFlagProperty.equalsIgnoreCase("FALSE")) {
						descentFlag = Boolean.FALSE;
					}
				}
				if(descentFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.DESCENT);
				
				////////////////////////////////////////////////////////////////////////////////////
				Boolean landingFlag = Boolean.FALSE;
				String landingFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@landing");
				if (landingFlagProperty != null) {
					if(landingFlagProperty.equalsIgnoreCase("TRUE")) {
						landingFlag = Boolean.TRUE;
					}
					else if(landingFlagProperty.equalsIgnoreCase("FALSE")) {
						landingFlag = Boolean.FALSE;
					}
				}
				if(landingFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.LANDING);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean payloadRangeFlag = Boolean.FALSE;
				String payloadRangeFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@payload_range");
				if (payloadRangeFlagProperty != null) {
					if(payloadRangeFlagProperty.equalsIgnoreCase("TRUE")) {
						payloadRangeFlag = Boolean.TRUE;
					}
					else if(payloadRangeFlagProperty.equalsIgnoreCase("FALSE")) {
						payloadRangeFlag = Boolean.FALSE;
					}
				}
				if(payloadRangeFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.PAYLOAD_RANGE);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean VnDiagramFlag = Boolean.FALSE;
				String VnDiagramFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@V_n_diagram");
				if (VnDiagramFlagProperty != null) {
					if(VnDiagramFlagProperty.equalsIgnoreCase("TRUE")) {
						VnDiagramFlag = Boolean.TRUE;
					}
					else if(VnDiagramFlagProperty.equalsIgnoreCase("FALSE")) {
						VnDiagramFlag = Boolean.FALSE;
					}
				}
				if(VnDiagramFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.V_n_DIAGRAM);
				
				////////////////////////////////////////////////////////////////////////////////////
				Boolean missionProfileFlag = Boolean.FALSE;
				String missionProfileFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@mission_profile");
				if (missionProfileFlagProperty != null) {
					if(missionProfileFlagProperty.equalsIgnoreCase("TRUE")) {
						missionProfileFlag = Boolean.TRUE;
					}
					else if(missionProfileFlagProperty.equalsIgnoreCase("FALSE")) {
						missionProfileFlag = Boolean.FALSE;
					}
				}
				if(missionProfileFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.MISSION_PROFILE);
			}
		
			_performanceFileComplete = new File(
					MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
					+ File.separator 
					+ "Template_Analyses"
					+ File.separator
					+ performanceFile
					);
		}
		
		//-------------------------------------------------------------------------------------------
		// COSTS ANALYSIS:
		Map<CostsEnum, MethodEnum> taskListCosts = new HashMap<>();
		Boolean plotCosts = Boolean.FALSE;

		NodeList costsTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//costs");
		Node costsNode = costsTag.item(0);

		if(costsNode != null) {

			String costsFile = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//costs/@file");

			String plotCostsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//costs/@plot");
			if(plotCostsString.equalsIgnoreCase("FALSE") 
					|| plotCostsString == null)
				plotCosts = Boolean.FALSE;
			else if(plotCostsString.equalsIgnoreCase("TRUE"))
				plotCosts = Boolean.TRUE;

			if(costsFile != null)  {		
				analysisList.add(AnalysisTypeEnum.COSTS);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean capitalDOCFlag = Boolean.FALSE;
				MethodEnum capitalDOCMethod = null;
				String capitalDOCFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//costs/@doc_capital");
				if (capitalDOCFlagProperty != null) {
					if(capitalDOCFlagProperty.equalsIgnoreCase("TRUE")) {
						capitalDOCFlag = Boolean.TRUE;
					}
					else if(capitalDOCFlagProperty.equalsIgnoreCase("FALSE")) {
						capitalDOCFlag = Boolean.FALSE;
					}

					String capitalDOCMethodProperty = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//costs/@doc_capital_method");
					if(capitalDOCMethodProperty != null)
						capitalDOCMethod = MethodEnum.valueOf(capitalDOCMethodProperty);
				}
				if(capitalDOCFlag == Boolean.TRUE) 
					taskListCosts.put(CostsEnum.DOC_CAPITAL, capitalDOCMethod);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean crewDOCFlag = Boolean.FALSE;
				MethodEnum crewDOCMethod = null;
				String crewDOCFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//costs/@doc_crew");
				if (crewDOCFlagProperty != null) {
					if(crewDOCFlagProperty.equalsIgnoreCase("TRUE")) {
						crewDOCFlag = Boolean.TRUE;
					}
					else if(crewDOCFlagProperty.equalsIgnoreCase("FALSE")) {
						crewDOCFlag = Boolean.FALSE;
					}

					String crewDOCMethodProperty = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//costs/@doc_crew_method");
					if(crewDOCMethodProperty != null)
						crewDOCMethod = MethodEnum.valueOf(crewDOCMethodProperty);
				}
				if(crewDOCFlag == Boolean.TRUE) 
					taskListCosts.put(CostsEnum.DOC_CREW, crewDOCMethod);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean fuelDOCFlag = Boolean.FALSE;
				MethodEnum fuelDOCMethod = null;
				String fuelDOCFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//costs/@doc_fuel");
				if (fuelDOCFlagProperty != null) {
					if(fuelDOCFlagProperty.equalsIgnoreCase("TRUE")) {
						fuelDOCFlag = Boolean.TRUE;
					}
					else if(fuelDOCFlagProperty.equalsIgnoreCase("FALSE")) {
						fuelDOCFlag = Boolean.FALSE;
					}

					String fuelDOCMethodProperty = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//costs/@doc_fuel_method");
					if(fuelDOCMethodProperty != null)
						fuelDOCMethod = MethodEnum.valueOf(fuelDOCMethodProperty);
				}
				if(fuelDOCFlag == Boolean.TRUE) 
					taskListCosts.put(CostsEnum.DOC_FUEL, fuelDOCMethod);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean chargesDOCFlag = Boolean.FALSE;
				MethodEnum chargesDOCMethod = null;
				String chargesDOCFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//costs/@doc_charges");
				if (chargesDOCFlagProperty != null) {
					if(chargesDOCFlagProperty.equalsIgnoreCase("TRUE")) {
						chargesDOCFlag = Boolean.TRUE;
					}
					else if(chargesDOCFlagProperty.equalsIgnoreCase("FALSE")) {
						chargesDOCFlag = Boolean.FALSE;
					}

					String chargesDOCMethodProperty = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//costs/@doc_charges_method");
					if(chargesDOCMethodProperty != null)
						chargesDOCMethod = MethodEnum.valueOf(chargesDOCMethodProperty);
				}
				if(chargesDOCFlag == Boolean.TRUE) 
					taskListCosts.put(CostsEnum.DOC_CHARGES, chargesDOCMethod);

				////////////////////////////////////////////////////////////////////////////////////
				Boolean maintenanceDOCFlag = Boolean.FALSE;
				MethodEnum maintenanceDOCMethod = null;
				String maintenanceDOCFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//costs/@doc_maintenance");
				if (maintenanceDOCFlagProperty != null) {
					if(maintenanceDOCFlagProperty.equalsIgnoreCase("TRUE")) {
						maintenanceDOCFlag = Boolean.TRUE;
					}
					else if(maintenanceDOCFlagProperty.equalsIgnoreCase("FALSE")) {
						maintenanceDOCFlag = Boolean.FALSE;
					}

					String maintenanceDOCMethodProperty = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//costs/@doc_maintenance_method");
					if(maintenanceDOCMethodProperty != null)
						maintenanceDOCMethod = MethodEnum.valueOf(maintenanceDOCMethodProperty);
				}
				if(maintenanceDOCFlag == Boolean.TRUE) 
					taskListCosts.put(CostsEnum.DOC_MAINTENANCE, maintenanceDOCMethod);

			}

			_costsFileComplete = new File(
					MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
					+ File.separator 
					+ "Template_Analyses"
					+ File.separator
					+ costsFile
					);
		}

		//-------------------------------------------------------------------------------------------
		ACAnalysisManager theAnalysisManager = new ACAnalysisManager.ACAnalysisManagerBuilder(
				id,
				theAircraft
				)
				.analysisList(analysisList)
				.positiveLimitLoadFactor(positiveLimitLoadFactor)
				.negativeLimitLoadFactor(negativeLimitLoadFactor)
				.cruiseCL(cruiseCL)
				.maxAltitudeAtMaxSpeed(maxAltitudeMaxSpeed)
				.machMaxCruise(maxCruiseMach)
				.altitudeOptimumCruise(optimumCruiseAltitude)
				.machOptimumCruise(optimumCruiseMach)
				.blockTime(blockTime)
				.flightTime(flightTime)
				.referenceRange(referenceRange)
				.methodsMapWeights(methodsMapWeights)
				.methodsMapBalance(methodsMapBalance)
				.taskListPerfromance(taskListPerformance)
				.taskListAerodynamicAndStability(taskListAerodynamicAndStability)
				.taskListCosts(taskListCosts)
				.plotBalance(plotBalance)
				.plotAerodynamicAndStability(plotAerodynamicAndStability)
				.plotPerformance(plotPerformance)
				.plotCosts(plotCosts)
				.build();
	
		return theAnalysisManager;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tAircraft Analysis Manager\n")
				.append("\t-------------------------------------\n")
				.append("\tAircraft in exam: " + _theAircraft.getId() + "\n")
				.append("\t·····································\n")
				.append("\tPositive limit load factor: " + _positiveLimitLoadFactor + "\n")
				.append("\tNegative limit load factor: " + _negativeLimitLoadFactor + "\n")
				.append("\tCruise CL: " + _cruiseCL + "\n")
				.append("\tMaximum altitude at maximum speed: " + _maxAltitudeAtMaxSpeed + "\n")
				.append("\tMaximum cruise Mach number: " + _machMaxCruise + "\n")
				.append("\tOptimum cruise altitude: " + _altitudeOptimumCruise + "\n")
				.append("\tOptimum cruise Mach number: " + _machOptimumCruise + "\n")
				.append("\tBlock time: " + _blockTime + "\n")
				.append("\tFlight time: " + _flightTime + "\n")
				.append("\tReference range: " + _referenceRange + "\n")
				.append("\t·····································\n")
				.append("\tn Ultimate " + _nUltimate + "\n")
				.append("\tV dive (TAS): " + _vDive + "\n")
				.append("\tV dive (EAS): " + _vDiveEAS + "\n")
				.append("\tV dive: " + _vDive + "\n")
				.append("\tMach dive at zero altitude: " + _machDive0 + "\n")
				.append("\tV max cruise (TAS): " + _vMaxCruise + "\n")
				.append("\tV max cruise (EAS): " + _vMaxCruiseEAS + "\n")
				.append("\tV optimum cruise: " + _vOptimumCruise + "\n")
				.append("\tMax dynamic pressure: " + _maxDynamicPressure + "\n")
				.append("\t·····································\n")
				;
		if(_executedAnalysesMap.get(AnalysisTypeEnum.WEIGHTS) == true)
			sb.append(_theAircraft.getTheAnalysisManager().getTheWeights().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.BALANCE) == true)
			sb.append(_theAircraft.getTheAnalysisManager().getTheBalance().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY) == true) {
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.TAKE_OFF))
				sb.append(_theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).toString());
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.CLIMB))
				sb.append(_theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB).toString());
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.CRUISE))
				sb.append(_theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).toString());
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.LANDING))
				sb.append(_theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).toString());
		}
		if(_executedAnalysesMap.get(AnalysisTypeEnum.PERFORMANCE) == true)
			sb.append(_theAircraft.getTheAnalysisManager().getThePerformance().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.COSTS) == true)
			sb.append(_theAircraft.getTheAnalysisManager().getTheCosts().toString());
		
		return sb.toString();
	}
	
	/**
	 * Evaluate dependent data
	 */
	public void calculateDependentVariables() {

		_nUltimate = 1.5 * _positiveLimitLoadFactor;
		
		// Maximum cruise TAS
		_vMaxCruise = Amount.valueOf(
				_machMaxCruise * 
				OperatingConditions.getAtmosphere(_maxAltitudeAtMaxSpeed.doubleValue(SI.METER)).getSpeedOfSound(), 
				SI.METERS_PER_SECOND);
		_vMaxCruiseEAS = _vMaxCruise.
				times(Math.sqrt(
						OperatingConditions.getAtmosphere(_maxAltitudeAtMaxSpeed.doubleValue(SI.METER)).getDensityRatio()));

		_vOptimumCruise = Amount.valueOf(_machOptimumCruise*AtmosphereCalc.getSpeedOfSound(_altitudeOptimumCruise.doubleValue(SI.METER)), SI.METERS_PER_SECOND);
		
		// FAR Part 25 paragraph 25.335
		_vDive = _vMaxCruise.times(1.25); 
		_vDiveEAS = _vMaxCruiseEAS.times(1.25); 

		_machDive0 = _vDiveEAS.divide(AtmosphereCalc.a0).getEstimatedValue();
		_maxDynamicPressure = Amount.valueOf(0.5 * 
				AtmosphereCalc.rho0.getEstimatedValue()*
				Math.pow(_vDiveEAS.getEstimatedValue(), 2), SI.PASCAL); 
	}
	
	public void doAnalysis(
			Aircraft aircraft, 
			OperatingConditions theOperatingConditions,
			String resultsFolderPath
			) throws IOException {

		if (aircraft == null) return;
		
		////////////////////////////////////////////////////////////////
		if (this._analysisList.contains(AnalysisTypeEnum.WEIGHTS)) {
			_theWeights = ACWeightsManager.importFromXML(
					_weightsFileComplete.getAbsolutePath(),
					aircraft
					);
			calculateWeights(aircraft, resultsFolderPath); 
			_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, true);
		}
		////////////////////////////////////////////////////////////////
		if (this._analysisList.contains(AnalysisTypeEnum.BALANCE)) {
			_theBalance = ACBalanceManager.importFromXML(
					_balanceFileComplete.getAbsolutePath(),
					aircraft
					);
			calculateBalance(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, true);
		}
		////////////////////////////////////////////////////////////////
		if (this._analysisList.contains(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY)) {
			
			_theAerodynamicAndStability.put(ConditionEnum.TAKE_OFF, new ACAerodynamicCalculator());
			_theAerodynamicAndStability.put(ConditionEnum.CLIMB, new ACAerodynamicCalculator());
			_theAerodynamicAndStability.put(ConditionEnum.CRUISE, new ACAerodynamicCalculator());
			_theAerodynamicAndStability.put(ConditionEnum.LANDING, new ACAerodynamicCalculator());
			
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.TAKE_OFF)) {
				_theAerodynamicAndStability.remove(ConditionEnum.TAKE_OFF);
				_theAerodynamicAndStability.put(
						ConditionEnum.TAKE_OFF,
						ACAerodynamicCalculator.importFromXML(
								_aerodynamicAndStabilityTakeOffFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.TAKE_OFF
								)
						);
			}
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.CLIMB)) {
				_theAerodynamicAndStability.remove(ConditionEnum.CLIMB);
				_theAerodynamicAndStability.put(
						ConditionEnum.CLIMB,
						ACAerodynamicCalculator.importFromXML(
								_aerodynamicAndStabilityClimbFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CLIMB
								)
						);
			}
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.CRUISE)) {
				_theAerodynamicAndStability.remove(ConditionEnum.CRUISE);
				_theAerodynamicAndStability.put(
						ConditionEnum.CRUISE,
						ACAerodynamicCalculator.importFromXML(
								_aerodynamicAndStabilityCruiseFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CRUISE
								)
						);
			}
			if(_taskListAerodynamicAndStability.contains(ConditionEnum.LANDING)) {
				_theAerodynamicAndStability.remove(ConditionEnum.LANDING);
				_theAerodynamicAndStability.put(
						ConditionEnum.LANDING,
						ACAerodynamicCalculator.importFromXML(
								_aerodynamicAndStabilityLandingFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.LANDING
								)
						);
			}
			
			calculateAerodynamicAndStability(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, true);
		}
		////////////////////////////////////////////////////////////////
		if (this._analysisList.contains(AnalysisTypeEnum.PERFORMANCE)) {
			_thePerformance = ACPerformanceManager.importFromXML(
					_performanceFileComplete.getAbsolutePath(), 
					aircraft,
					theOperatingConditions
					);
			calculatePerformances(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, true);
		}
		////////////////////////////////////////////////////////////////
		if (this._analysisList.contains(AnalysisTypeEnum.COSTS)) {
			_theCosts = ACCostsManager.importFromXML(
					_costsFileComplete.getAbsolutePath(), 
					aircraft, 
					theOperatingConditions, 
					_taskListCosts
					);
			calculateCosts(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, true);
		}
				
	} // end of constructor

	public void calculateWeights(Aircraft aircraft, String resultsFolderPath) {

		aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);
		aircraft.getCabinConfiguration().calculateDependentVariables();

		// Evaluate aircraft masses
		aircraft.getTheAnalysisManager().getTheWeights().calculateAllMasses(aircraft, _methodsMapWeights);

		// Plot
		try {
			String weightsFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "WEIGHTS"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheWeights().toXLSFile(
					weightsFolderPath
					+ "Weights");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	public void calculateBalance(Aircraft aircraft, String resultsFolderPath) {

		// Estimate center of gravity location
		aircraft.getTheAnalysisManager().getTheBalance().calculate(_methodsMapBalance);
		
		// Plot
		try {
			String balanceFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "BALANCE"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheBalance().toXLSFile(
					balanceFolderPath
					+ "Balance");
			if(_plotBalance == Boolean.TRUE)
				aircraft.getTheAnalysisManager().getTheBalance().createCharts(balanceFolderPath);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Evaluate arms again with the new CG estimate
		aircraft.calculateArms(
				aircraft.getHTail(),
				_theAircraft.getTheAnalysisManager().getTheBalance().getCGMTOM().getXBRF()
				);
		aircraft.calculateArms(
				aircraft.getVTail(),
				_theAircraft.getTheAnalysisManager().getTheBalance().getCGMTOM().getXBRF()
				);

	}
	
	public void calculateAerodynamicAndStability(Aircraft aircraft, String resultsFolderPath) {

		if(_taskListAerodynamicAndStability.contains(ConditionEnum.TAKE_OFF)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).calculate();
		if(_taskListAerodynamicAndStability.contains(ConditionEnum.CLIMB)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB).calculate();
		if(_taskListAerodynamicAndStability.contains(ConditionEnum.CRUISE)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).calculate();
		if(_taskListAerodynamicAndStability.contains(ConditionEnum.LANDING)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).calculate();

	}
	
	public void calculatePerformances(Aircraft aircraft, String resultsFolderPath) {

		// Execute analysis
		aircraft.getTheAnalysisManager().getThePerformance().calculate(resultsFolderPath);
		
	}
	
	public void calculateCosts(Aircraft aircraft, String resultsFolderPath) {
		
		aircraft.getTheAnalysisManager().getTheCosts().calculate(resultsFolderPath);
		
	}

	//////////////////////////////////////////////////////////////////////////
	// GETTERS & SETTERS:
	
	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

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

	public Double getCruiseCL() {
		return _cruiseCL;
	}

	public void setCruiseCL(Double _cruiseCL) {
		this._cruiseCL = _cruiseCL;
	}

	public Amount<Length> getMaxAltitudeAtMaxSpeed() {
		return _maxAltitudeAtMaxSpeed;
	}

	public void setMaxAltitudeAtMaxSpeed(Amount<Length> _maxAltitudeAtMaxSpeed) {
		this._maxAltitudeAtMaxSpeed = _maxAltitudeAtMaxSpeed;
	}

	public Double getMachMaxCruise() {
		return _machMaxCruise;
	}

	public void setMachMaxCruise(Double _machMaxCruise) {
		this._machMaxCruise = _machMaxCruise;
	}

	public Amount<Length> getAltitudeOptimumCruise() {
		return _altitudeOptimumCruise;
	}

	public void setAltitudeOptimumCruise(Amount<Length> _altitudeOptimumCruise) {
		this._altitudeOptimumCruise = _altitudeOptimumCruise;
	}

	public Double getMachOptimumCruise() {
		return _machOptimumCruise;
	}

	public void setMachOptimumCruise(Double _machOptimumCruise) {
		this._machOptimumCruise = _machOptimumCruise;
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

	public Double getNUltimate() {
		return _nUltimate;
	}

	public void setNUltimate(Double _nUltimate) {
		this._nUltimate = _nUltimate;
	}

	public Amount<Velocity> getVDive() {
		return _vDive;
	}

	public void setVDive(Amount<Velocity> _vDive) {
		this._vDive = _vDive;
	}

	public Amount<Velocity> getVDiveEAS() {
		return _vDiveEAS;
	}

	public void setVDiveEAS(Amount<Velocity> _vDiveEAS) {
		this._vDiveEAS = _vDiveEAS;
	}

	public Double getMachDive0() {
		return _machDive0;
	}

	public void setMachDive0(Double _machDive0) {
		this._machDive0 = _machDive0;
	}

	public Amount<Velocity> getVMaxCruise() {
		return _vMaxCruise;
	}

	public void setVMaxCruise(Amount<Velocity> _vMaxCruise) {
		this._vMaxCruise = _vMaxCruise;
	}

	public Amount<Velocity> getVMaxCruiseEAS() {
		return _vMaxCruiseEAS;
	}

	public void setVMaxCruiseEAS(Amount<Velocity> _vMaxCruiseEAS) {
		this._vMaxCruiseEAS = _vMaxCruiseEAS;
	}

	public Amount<Velocity> getVOptimumCruise() {
		return _vOptimumCruise;
	}

	public void setVOptimumCruise(Amount<Velocity> _vOptimumCruise) {
		this._vOptimumCruise = _vOptimumCruise;
	}

	public Amount<Pressure> getMaxDynamicPressure() {
		return _maxDynamicPressure;
	}

	public void setMaxDynamicPressure(Amount<Pressure> _maxDynamicPressure) {
		this._maxDynamicPressure = _maxDynamicPressure;
	}

	public Map<ComponentEnum, MethodEnum> getMethodsMapWeights() {
		return _methodsMapWeights;
	}

	public void setMethodsMapWeights(Map<ComponentEnum, MethodEnum> _methodsMap) {
		this._methodsMapWeights = _methodsMap;
	}

	public Map<ComponentEnum, MethodEnum> getMethodsMapBalance() {
		return _methodsMapBalance;
	}

	public void setMethodsMapBalance(Map<ComponentEnum, MethodEnum> _methodsMapBalance) {
		this._methodsMapBalance = _methodsMapBalance;
	}

	public Map<AnalysisTypeEnum, Boolean> getExecutedAnalysesMap() {
		return _executedAnalysesMap;
	}

	public void setExecutedAnalysesMap(Map<AnalysisTypeEnum, Boolean> _executedAnalysesMap) {
		this._executedAnalysesMap = _executedAnalysesMap;
	}

	public ACWeightsManager getTheWeights() {
		return _theWeights;
	}

	public void setTheWeights(ACWeightsManager theWeights) {
		this._theWeights = theWeights;
	}

	public ACBalanceManager getTheBalance() {
		return _theBalance;
	}

	public void setTheBalance(ACBalanceManager theBalance) {
		this._theBalance = theBalance;
	}

	public Map<ConditionEnum, ACAerodynamicCalculator> getTheAerodynamicAndStability() {
		return _theAerodynamicAndStability;
	}

	public void setTheAerodynamicAndStability(Map<ConditionEnum, ACAerodynamicCalculator> theAerodynamicAndStability) {
		this._theAerodynamicAndStability = theAerodynamicAndStability;
	}

	public ACPerformanceManager getThePerformance() {
		return _thePerformance;
	}

	public void setThePerformance(ACPerformanceManager thePerformance) {
		this._thePerformance = thePerformance;
	}

	public ACCostsManager getTheCosts() {
		return _theCosts;
	}

	public void setTheCosts(ACCostsManager theCosts) {
		this._theCosts = theCosts;
	}

	public List<AnalysisTypeEnum> getAnalysisList() {
		return _analysisList;
	}

	public void setAnalysisList(List<AnalysisTypeEnum> _analysisList) {
		this._analysisList = _analysisList;
	}

	public File getWeightsFileComplete() {
		return _weightsFileComplete;
	}

	public void setWeightsFileComplete(File _weightsFileComplete) {
		ACAnalysisManager._weightsFileComplete = _weightsFileComplete;
	}

	public File getBalanceFileComplete() {
		return _balanceFileComplete;
	}

	public void setBalanceFileComplete(File _balanceFileComplete) {
		ACAnalysisManager._balanceFileComplete = _balanceFileComplete;
	}

	public File getAerodynamicAndStabilityTakeOffFileComplete() {
		return _aerodynamicAndStabilityTakeOffFileComplete;
	}

	public void setAerodynamicAndStabilityTakeOffFileComplete(File _aerodynamicAndStabilityTakeOffFileComplete) {
		ACAnalysisManager._aerodynamicAndStabilityTakeOffFileComplete = _aerodynamicAndStabilityTakeOffFileComplete;
	}
	
	public File getAerodynamicAndStabilityClimbFileComplete() {
		return _aerodynamicAndStabilityClimbFileComplete;
	}

	public void setAerodynamicAndStabilityClimbFileComplete(File _aerodynamicAndStabilityClimbFileComplete) {
		ACAnalysisManager._aerodynamicAndStabilityClimbFileComplete = _aerodynamicAndStabilityClimbFileComplete;
	}

	public File getAerodynamicAndStabilityCruiseFileComplete() {
		return _aerodynamicAndStabilityCruiseFileComplete;
	}

	public void setAerodynamicAndStabilityCruiseFileComplete(File _aerodynamicAndStabilityCruiseFileComplete) {
		ACAnalysisManager._aerodynamicAndStabilityCruiseFileComplete = _aerodynamicAndStabilityCruiseFileComplete;
	}
	
	public File getAerodynamicAndStabilityLandingFileComplete() {
		return _aerodynamicAndStabilityLandingFileComplete;
	}

	public void setAerodynamicAndStabilityLandingFileComplete(File _aerodynamicAndStabilityLandingFileComplete) {
		ACAnalysisManager._aerodynamicAndStabilityLandingFileComplete = _aerodynamicAndStabilityLandingFileComplete;
	}
	
	public File getPerformanceFileComplete() {
		return _performanceFileComplete;
	}

	public void setPerformanceFileComplete(File _performanceFileComplete) {
		ACAnalysisManager._performanceFileComplete = _performanceFileComplete;
	}

	public File getCostsFileComplete() {
		return _costsFileComplete;
	}

	public void setCostsFileComplete(File _costsFileComplete) {
		ACAnalysisManager._costsFileComplete = _costsFileComplete;
	}

	public Boolean getPlotBalance() {
		return _plotBalance;
	}

	public void setPlotBalance(Boolean _plotBalance) {
		this._plotBalance = _plotBalance;
	}

	public Boolean getPlotAerodynamicAndStability() {
		return _plotAerodynamicAndStability;
	}

	public void setPlotAerodynamicAndStability(Boolean _plotAerodynamicAndStability) {
		this._plotAerodynamicAndStability = _plotAerodynamicAndStability;
	}

	public Boolean getPlotPerformance() {
		return _plotPerformance;
	}

	public void setPlotPerformance(Boolean _plotPerformance) {
		this._plotPerformance = _plotPerformance;
	}

	public Boolean getPlotCosts() {
		return _plotCosts;
	}

	public void setPlotCosts(Boolean _plotCosts) {
		this._plotCosts = _plotCosts;
	}

	public List<PerformanceEnum> getTaskListPerformance() {
		return _taskListPerformance;
	}

	public void setTaskListPerformance(List<PerformanceEnum> _taskListPerformance) {
		this._taskListPerformance = _taskListPerformance;
	}

	public Amount<Length> getReferenceRange() {
		return _referenceRange;
	}

	public List<ConditionEnum> getTaskListAerodynamicAndStability() {
		return _taskListAerodynamicAndStability;
	}

	public void setTaskListAerodynamicAndStability(List<ConditionEnum> _taskListAerodynamicAndStability) {
		this._taskListAerodynamicAndStability = _taskListAerodynamicAndStability;
	}

	public void setReferenceRange(Amount<Length> _referenceRange) {
		this._referenceRange = _referenceRange;
	}

	public Map<CostsEnum, MethodEnum> getTaskListCosts() {
		return _taskListCosts;
	}

	public void setTaskListCosts(Map<CostsEnum, MethodEnum> _taskListCosts) {
		this._taskListCosts = _taskListCosts;
	}

}