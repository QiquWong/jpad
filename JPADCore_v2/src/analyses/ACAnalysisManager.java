package analyses;

import java.io.File;
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

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.costs.ACCostsManager;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

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
	private ACAerodynamicsManager _theAerodynamics;
	private ACPerformanceManager _thePerformance;
	private ACCostsManager _theCosts;
	
	// INPUT DATA: 
	private Double _nLimit;
	private Double _cruiseCL;
	private Amount<Length> _referenceRange;
	private Amount<Length> _maxAltitudeAtMaxSpeed;
	private Double _machMaxCruise;
	private Amount<Length> _altitudeOptimumCruise;
	private Double _machOptimumCruise;
	private Amount<Duration> _blockTime;
	private Amount<Duration> _flightTime;
	
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
	private Map <AnalysisTypeEnum, Boolean> _executedAnalysesMap;
	private List<ACCalculatorManager> _theCalculatorsList;
	private List<AnalysisTypeEnum> _analysisList;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACAnalysisManagerBuilder {

		// required parameters
		private String __id;
		private Aircraft __theAircraft;

		// optional parameters ... defaults
		// ...
		private ACWeightsManager __theWeights;
		private ACBalanceManager __theBalance;
		private ACAerodynamicsManager __theAerodynamics;
		private ACPerformanceManager __thePerformance;
		private ACCostsManager __theCosts;
		
		private Double __nLimit;
		private Double __cruiseCL;
		private Amount<Length> __referenceRange;
		private Amount<Length> __maxAltitudeAtMaxSpeed;
		private Double __machMaxCruise;
		private Amount<Length> __altitudeOptimumCruise;
		private Double __machOptimumCruise;
		private Amount<Duration> __blockTime;
		private Amount<Duration> __flightTime;
		
		private Map <ComponentEnum, MethodEnum> __methodsMapWeights = new HashMap<ComponentEnum, MethodEnum>();
		private Map <AnalysisTypeEnum, Boolean> __executedAnalysesMap = new HashMap<AnalysisTypeEnum, Boolean>();
		private List<ACCalculatorManager> __theCalculatorsList = new ArrayList<ACCalculatorManager>();
		private List<AnalysisTypeEnum> __analysisList = new ArrayList<AnalysisTypeEnum>();
		
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
		
		public ACAnalysisManagerBuilder theWeights (ACWeightsManager theWeights) {
			this.__theWeights = theWeights;
			return this;
		}
		
		public ACAnalysisManagerBuilder theBalance (ACBalanceManager theBalance) {
			this.__theBalance = theBalance;
			return this;
		}
		
		public ACAnalysisManagerBuilder theAerodynamics (ACAerodynamicsManager theAerodynamics) {
			this.__theAerodynamics = theAerodynamics;
			return this;
		}
		
		public ACAnalysisManagerBuilder thePerformance (ACPerformanceManager thePerformance) {
			this.__thePerformance = thePerformance;
			return this;
		}
		
		public ACAnalysisManagerBuilder theCosts (ACCostsManager theCosts) {
			this.__theCosts = theCosts;
			return this;
		}
		
		public ACAnalysisManagerBuilder nLimit (Double nLimit) {
			this.__nLimit = nLimit;
			return this;
		}
		
		public ACAnalysisManagerBuilder cruiseCL (Double cruiseCL) {
			this.__cruiseCL = cruiseCL;
			return this;
		}
		
		public ACAnalysisManagerBuilder referenceRange (Amount<Length> referenceRange) {
			this.__referenceRange = referenceRange;
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
		
		public ACAnalysisManagerBuilder methodsMap (Map<ComponentEnum, MethodEnum> methodsMap) {
			this.__methodsMapWeights = methodsMap;
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

		private void initializeDefaultData(AircraftEnum aircraftName) {
			
			switch(aircraftName) {
			case ATR72:
				__nLimit = 2.5;
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
				__nLimit = 2.5;
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
				__nLimit = 2.5;
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
		this._nLimit = builder.__nLimit;
		this._cruiseCL = builder.__cruiseCL;
		this._referenceRange = builder.__referenceRange;
		this._maxAltitudeAtMaxSpeed = builder.__maxAltitudeAtMaxSpeed;
		this._machMaxCruise = builder.__machMaxCruise;
		this._altitudeOptimumCruise = builder.__altitudeOptimumCruise;
		this._machOptimumCruise = builder.__machOptimumCruise;
		this._blockTime = builder.__blockTime;
		this._flightTime = builder.__flightTime;
		
		this._methodsMapWeights = builder.__methodsMapWeights;
		this._executedAnalysesMap = builder.__executedAnalysesMap;
		this._theCalculatorsList = builder.__theCalculatorsList;
		this._analysisList = builder.__analysisList;
		
		this._theWeights = builder.__theWeights;
		this._theBalance = builder.__theBalance;
		this._theAerodynamics = builder.__theAerodynamics;
		this._thePerformance = builder.__thePerformance;
		this._theCosts = builder.__theCosts;
		
		calculateDependentVariables();

	}

	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	public static ACAnalysisManager importFromXML (String pathToXML, Aircraft theAircraft) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		List<AnalysisTypeEnum> analysisList = new ArrayList<>();
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		Double nLimit = Double.valueOf(reader.getXMLPropertyByPath("//global_data/n_limit"));
		Double cruiseCL = Double.valueOf(reader.getXMLPropertyByPath("//global_data/cruise_lift_coefficient"));
		Amount<Length> referenceRange = reader.getXMLAmountLengthByPath("//global_data/reference_range");
		Amount<Length> maxAltitudeMaxSpeed = reader.getXMLAmountLengthByPath("//global_data/maximum_altitude_at_maximum_speed");
		Double maxCruiseMach = Double.valueOf(reader.getXMLPropertyByPath("//global_data/maximum_cruise_mach_number"));
		Amount<Length> optimumCruiseAltitude = reader.getXMLAmountLengthByPath("//global_data/optimum_cruise_altitude");
		Double optimumCruiseMach = Double.valueOf(reader.getXMLPropertyByPath("//global_data/optimum_cruise_mach_number"));
		Amount<Duration> blockTime = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//global_data/block_time")), NonSI.HOUR);
		Amount<Duration> flightTime = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//global_data/flight_time")), NonSI.HOUR);

		//-------------------------------------------------------------------------------------------
		// WEIGHTS ANALYSIS:
		Map<ComponentEnum, MethodEnum> methodsMap = new HashMap<>();
		
		String weightsFile = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//weights/@file");
		
		if(weightsFile != null) {
		
			analysisList.add(AnalysisTypeEnum.WEIGHTS);
			
			String fuselageMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_fuselage");
			if (fuselageMethod != null) {
				if(fuselageMethod.equalsIgnoreCase("RAYMER")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.RAYMER);
				}
				else if(fuselageMethod.equalsIgnoreCase("TORENBEEK_1976")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.TORENBEEK_1976);
				}
				else if(fuselageMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.TORENBEEK_2013);
				}
				else if(fuselageMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.JENKINSON);
				}
				else if(fuselageMethod.equalsIgnoreCase("KROO")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.KROO);
				}
				else if(fuselageMethod.equalsIgnoreCase("SADRAY")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.SADRAY);
				}
				else if(fuselageMethod.equalsIgnoreCase("NICOLAI_1984")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.NICOLAI_1984);
				}
				else if(fuselageMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.FUSELAGE, MethodEnum.ROSKAM);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE FUSELAGE ANALYSIS !!");
					return null;
				}
			}

			String wingMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_wing");
			if(wingMethod != null) {
				if(wingMethod.equalsIgnoreCase("KROO")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.KROO);
				}
				else if(wingMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.JENKINSON);
				}
				else if(wingMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.TORENBEEK_2013);
				}
				else if(wingMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.TORENBEEK_1982);
				}
				else if(wingMethod.equalsIgnoreCase("RAYMER")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.RAYMER);
				}
				else if(wingMethod.equalsIgnoreCase("SADRAY")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.SADRAY);
				}
				else if(wingMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.WING, MethodEnum.ROSKAM);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE WING ANALYSIS !!");
					return null;
				}
			}
			
			String hTailMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_htail");
			if(hTailMethod != null) {
				if(hTailMethod.equalsIgnoreCase("KROO")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.KROO);
				}
				else if(hTailMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.JENKINSON);
				}
				else if(hTailMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_2013);
				}
				else if(hTailMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_1982);
				}
				else if(hTailMethod.equalsIgnoreCase("RAYMER")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.RAYMER);
				}
				else if(hTailMethod.equalsIgnoreCase("SADRAY")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.SADRAY);
				}
				else if(hTailMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.ROSKAM);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE HORIZONTAL TAIL ANALYSIS !!");
					return null;
				}
			}
			
			String vTailMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_vtail");
			if(vTailMethod != null) {
				if(vTailMethod.equalsIgnoreCase("KROO")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.KROO);
				}
				else if(vTailMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.JENKINSON);
				}
				else if(vTailMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_2013);
				}
				else if(vTailMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_1982);
				}
				else if(vTailMethod.equalsIgnoreCase("RAYMER")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.RAYMER);
				}
				else if(vTailMethod.equalsIgnoreCase("SADRAY")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.SADRAY);
				}
				else if(vTailMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.ROSKAM);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE VERTICAL TAIL ANALYSIS !!");
					return null;
				}
			}
			
			String canardMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_canard");
			if(canardMethod != null) {
				if(canardMethod.equalsIgnoreCase("KROO")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.KROO);
				}
				else if(canardMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.JENKINSON);
				}
				else if(canardMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_2013);
				}
				else if(canardMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_1982);
				}
				else if(canardMethod.equalsIgnoreCase("RAYMER")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.RAYMER);
				}
				else if(canardMethod.equalsIgnoreCase("SADRAY")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.SADRAY);
				}
				else if(canardMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.CANARD, MethodEnum.ROSKAM);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE CANARD ANALYSIS !!");
					return null;
				}
			}
			
			String nacellesMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_nacelles");
			if(nacellesMethod != null) {
				if(nacellesMethod.equalsIgnoreCase("JENKINSON")) {
					methodsMap.put(ComponentEnum.NACELLE, MethodEnum.JENKINSON);
				}
				else if(nacellesMethod.equalsIgnoreCase("TORENBEEK_1976")) {
					methodsMap.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1976);
				}
				else if(nacellesMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1982);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE NACELLES ANALYSIS !!");
					return null;
				}
			}
			
			String landingGearsMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_landing_gears");
			if(landingGearsMethod != null) {
				if(landingGearsMethod.equalsIgnoreCase("ROSKAM")) {
					methodsMap.put(ComponentEnum.LANDING_GEAR, MethodEnum.ROSKAM);
				}
				else if(landingGearsMethod.equalsIgnoreCase("STANFORD")) {
					methodsMap.put(ComponentEnum.LANDING_GEAR, MethodEnum.STANFORD);
				}
				else if(landingGearsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
					methodsMap.put(ComponentEnum.LANDING_GEAR, MethodEnum.TORENBEEK_1982);
				}
				else if(landingGearsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.LANDING_GEAR, MethodEnum.TORENBEEK_2013);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE LANDING GEARS ANALYSIS !!");
					return null;
				}
			}
			
			String systemsMethod = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@method_systems");
			if(systemsMethod != null) {
				if(systemsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
					methodsMap.put(ComponentEnum.SYSTEMS, MethodEnum.TORENBEEK_2013);
				}
				else {
					System.err.println("NO VALID METHODS FOR THE SYSTEMS ANALYSIS !!");
					return null;
				}
			}
		}
		
		File weightsFileComplete = new File(
				MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
				+ File.separator 
				+ "Template_Analyses"
				+ File.separator
				+ weightsFile
				);
		
		ACWeightsManager theWeights = ACWeightsManager.importFromXML(
				weightsFileComplete.getAbsolutePath(),
				theAircraft
				);
		
		//-------------------------------------------------------------------------------------------
		// BALANCE ANALYSIS:

		
		
		
		
		//-------------------------------------------------------------------------------------------
		// AERODYNAMIC ANALYSIS:
		
		// TODO: IMPLEMENT THIS!
		
		//-------------------------------------------------------------------------------------------
		// PERFORMANCE ANALYSIS:
		
		// TODO: IMPLEMENT THIS!
		
		//-------------------------------------------------------------------------------------------
		// COSTS ANALYSIS:
		
		// TODO: IMPLEMENT THIS!
		
		//-------------------------------------------------------------------------------------------
		ACAnalysisManager theAnalysisManager = new ACAnalysisManager.ACAnalysisManagerBuilder(
				id,
				theAircraft
				)
				.analysisList(analysisList)
				.nLimit(nLimit)
				.cruiseCL(cruiseCL)
				.referenceRange(referenceRange)
				.maxAltitudeAtMaxSpeed(maxAltitudeMaxSpeed)
				.machMaxCruise(maxCruiseMach)
				.altitudeOptimumCruise(optimumCruiseAltitude)
				.machOptimumCruise(optimumCruiseMach)
				.blockTime(blockTime)
				.flightTime(flightTime)
				.theWeights(theWeights)
				.methodsMap(methodsMap)
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
				.append("\tn Limit: " + _nLimit + "\n")
				.append("\tCruise CL: " + _cruiseCL + "\n")
				.append("\tReference range: " + _referenceRange + "\n")
				.append("\tMaximum altitude at maximum speed: " + _maxAltitudeAtMaxSpeed + "\n")
				.append("\tMaximum cruise Mach number: " + _machMaxCruise + "\n")
				.append("\tOptimum cruise altitude: " + _altitudeOptimumCruise + "\n")
				.append("\tOptimum cruise Mach number: " + _machOptimumCruise + "\n")
				.append("\tBlock time: " + _blockTime + "\n")
				.append("\tFlight time: " + _flightTime + "\n")
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
		
		return sb.toString();
	}
	
	/**
	 * Evaluate dependent data
	 */
	public void calculateDependentVariables() {

		_nUltimate = 1.5 * _nLimit;
		
		// Maximum cruise TAS
		_vMaxCruise = Amount.valueOf(
				_machMaxCruise * 
				OperatingConditions.getAtmosphere(_maxAltitudeAtMaxSpeed.getEstimatedValue()).getSpeedOfSound(), 
				SI.METERS_PER_SECOND);
		_vMaxCruiseEAS = _vMaxCruise.
				times(Math.sqrt(
						OperatingConditions.getAtmosphere(_maxAltitudeAtMaxSpeed.getEstimatedValue()).getDensityRatio()));

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
			OperatingConditions theOperatingConditions
			) {

		if (aircraft == null) return;

// 		TODO : THIS MAY BE NECESSARY FOR THE AERODYNAMIC ANALYSIS
//		
//		//it's possible to define a method setDatabase
//		if (aircraft.getWing().getAerodynamicDatabaseReader() != null){
//				aircraft.getTheAnalysisManager().getTheAerodynamics().setAerodynamicDatabaseReader(
//						aircraft
//						.getWing()
//						.getAerodynamicDatabaseReader()
//						);
//				}
//		
//		if (aircraft.getWing().getHighLiftDatabaseReader() != null){
//				aircraft.getTheAnalysisManager().getTheAerodynamics().setHighLiftDatabaseReader(
//						aircraft
//						.getWing()
//						.getHighLiftDatabaseReader()
//						);
//				}
				
		if (this._analysisList.contains(AnalysisTypeEnum.WEIGHTS)) {
			calculateWeights(aircraft); 
			_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, true);
		}

		if (this._analysisList.contains(AnalysisTypeEnum.BALANCE)) {
			calculateBalance(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, true);
		}

		if (this._analysisList.contains(AnalysisTypeEnum.AERODYNAMIC)) {
			calculateAerodynamics(theOperatingConditions, aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC, true);
		}
		
		if (this._analysisList.contains(AnalysisTypeEnum.PERFORMANCE)) {
			calculatePerformances(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, true);
		}
		
		if (this._analysisList.contains(AnalysisTypeEnum.COSTS)) {
			calculateCosts(aircraft);
			_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, true);
		}
		
	} // end of constructor

	public void calculateWeights(Aircraft aircraft) {

		aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);
		aircraft.getCabinConfiguration().calculateDependentVariables();

		// Evaluate aircraft masses
		aircraft.getTheAnalysisManager().getTheWeights().calculateAllMasses(aircraft, _methodsMapWeights);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.getTheAnalysisManager().getTheWeights());

	}

	public void calculateBalance(Aircraft aircraft) {

		// Estimate center of gravity location
		aircraft.getTheAnalysisManager().getTheBalance().calculateBalance(_methodsMapWeights);

		// Evaluate arms again with the new CG estimate
		aircraft.calculateArms(aircraft.getHTail());
		aircraft.calculateArms(aircraft.getVTail());

		_theCalculatorsList.add(aircraft.getTheAnalysisManager().getTheBalance());
	}
	
	public void calculateAerodynamics(OperatingConditions theOperatingConditions, Aircraft aircraft) {

		aircraft.getTheAnalysisManager().getTheAerodynamics().initialize(theOperatingConditions);
		aircraft.getTheAnalysisManager().getTheAerodynamics().calculateAll(theOperatingConditions);

		// populate _theCalculatorsList
		_theCalculatorsList.add(aircraft.getTheAnalysisManager().getTheAerodynamics());

	}
	
	public void calculatePerformances(Aircraft aircraft) {
		aircraft.getTheAnalysisManager().getThePerformance().calculateAllPerformance();
	}
	
	public void calculateCosts(Aircraft aircraft) {
		aircraft.getTheAnalysisManager().getTheCosts().calculateAll();
	}

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

	public Double getNLimit() {
		return _nLimit;
	}

	public void setNLimit(Double _nLimit) {
		this._nLimit = _nLimit;
	}

	public Double getCruiseCL() {
		return _cruiseCL;
	}

	public void setCruiseCL(Double _cruiseCL) {
		this._cruiseCL = _cruiseCL;
	}

	public Amount<Length> getReferenceRange() {
		return _referenceRange;
	}

	public void setReferenceRange(Amount<Length> _referenceRange) {
		this._referenceRange = _referenceRange;
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

	public Map <ComponentEnum, MethodEnum> getMethodsMapBalance() {
		return _methodsMapBalance;
	}

	public void setMethodsMapBalance(Map <ComponentEnum, MethodEnum> _methodsMapBalance) {
		this._methodsMapBalance = _methodsMapBalance;
	}

	public Map<AnalysisTypeEnum, Boolean> getExecutedAnalysesMap() {
		return _executedAnalysesMap;
	}

	public void setExecutedAnalysesMap(Map<AnalysisTypeEnum, Boolean> _executedAnalysesMap) {
		this._executedAnalysesMap = _executedAnalysesMap;
	}

	public List<ACCalculatorManager> getTheCalculatorsList() {
		return _theCalculatorsList;
	}

	public void setTheCalculatorsList(List<ACCalculatorManager> _theCalculatorsList) {
		this._theCalculatorsList = _theCalculatorsList;
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

	public ACAerodynamicsManager getTheAerodynamics() {
		return _theAerodynamics;
	}

	public void setTheAerodynamics(ACAerodynamicsManager theAerodynamics) {
		this._theAerodynamics = theAerodynamics;
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
}