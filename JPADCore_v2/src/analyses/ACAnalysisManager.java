package analyses;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
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
public class ACAnalysisManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private IACAnalysisManager _theAnalysisManagerInterface;

	private ACWeightsManager _theWeights;
	private ACBalanceManager _theBalance;
	private Map<ConditionEnum, ACAerodynamicAndStabilityManager> _theAerodynamicAndStability;
	private ACPerformanceManager _thePerformance;
	private ACCostsManager _theCosts;
	
	// DEPENDENT VARIABLES: 
	private Double _nUltimate;
	private Amount<Velocity> _vDive;
	private Amount<Velocity> _vDiveEAS;
	private Double _machDive0;
	private Amount<Velocity> _vMaxCruise;
	private Amount<Velocity> _vMaxCruiseEAS;
	private Amount<Velocity> _vOptimumCruise;
	private Amount<Pressure> _maxDynamicPressure;
	
	private Map <AnalysisTypeEnum, Boolean> _executedAnalysesMap;
	
	private static File _weightsFileComplete;
	private static File _balanceFileComplete;
	private static File _aerodynamicAndStabilityTakeOffFileComplete;
	private static File _aerodynamicAndStabilityClimbFileComplete;
	private static File _aerodynamicAndStabilityCruiseFileComplete;
	private static File _aerodynamicAndStabilityLandingFileComplete;
	private static File _performanceFileComplete;
	private static File _costsFileComplete;

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	public static ACAnalysisManager importFromXML (String pathToXML, Aircraft theAircraft, OperatingConditions operatingConditions) throws IOException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		List<AnalysisTypeEnum> analysisList = new ArrayList<>();
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		String iterativeLoop = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@iterative_loop");
		
		Double positiveLimitLoadFactor = null;
		Double negativeLimitLoadFactor = null;

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
		
		//-------------------------------------------------------------------------------------------
		// WEIGHTS ANALYSIS:
		Map<ComponentEnum, MethodEnum> methodsMapWeights = new HashMap<>();
		Boolean plotWeights = false;
		Boolean createCSVWeights = false;
		
		NodeList weightsTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//weights");
		Node weightsNode = weightsTag.item(0);
		
		if(weightsNode != null) {

			String weightsFile = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@file");
			
			String plotWeightsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@plot");
			if(plotWeightsString == null || plotWeightsString.equalsIgnoreCase("FALSE"))
				plotWeights = Boolean.FALSE;
			else if(plotWeightsString.equalsIgnoreCase("TRUE"))
				plotWeights = Boolean.TRUE;
			
			String createCSVWeightsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//weights/@create_CSV");
			if(createCSVWeightsString == null || createCSVWeightsString.equalsIgnoreCase("FALSE"))
				createCSVWeights = Boolean.FALSE;
			else if(createCSVWeightsString.equalsIgnoreCase("TRUE"))
				createCSVWeights = Boolean.TRUE;

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
					else if(fuselageWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodsMapWeights.put(ComponentEnum.FUSELAGE, MethodEnum.SADRAEY);
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
					else if(wingWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodsMapWeights.put(ComponentEnum.WING, MethodEnum.SADRAEY);
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
					if(hTailWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.HOWE);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.JENKINSON);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("NICOLAI_2013")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.NICOLAI_2013);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.KROO);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.RAYMER);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.SADRAEY);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.ROSKAM);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, MethodEnum.TORENBEEK_1976);
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
					else if(vTailWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.HOWE);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.RAYMER);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.SADRAEY);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.ROSKAM);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, MethodEnum.TORENBEEK_1976);
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
					if(canardWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.HOWE);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.JENKINSON);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("NICOLAI_2013")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.NICOLAI_2013);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("KROO")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.KROO);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.RAYMER);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.SADRAEY);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.ROSKAM);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_1976);
					}
					else 
						methodsMapWeights.put(ComponentEnum.CANARD, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String nacellesWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_nacelles");
				if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) 
						|| theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET)) {

					if(nacellesWeightsMethod != null) {
						if(nacellesWeightsMethod.equalsIgnoreCase("JENKINSON")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.JENKINSON);
						}
						else if(nacellesWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.KUNDU);
						}
						else if(nacellesWeightsMethod.equalsIgnoreCase("ROSKAM")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.ROSKAM);
						}
						else 
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.AVERAGE);
					}
					
				}
				else if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					if(nacellesWeightsMethod != null) {
						if(nacellesWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.KUNDU);
						}
						else if(nacellesWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1976);
						}
						else 
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.AVERAGE);
					}
					
				}
				else if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)) {
					
					if(nacellesWeightsMethod != null) {
						if(nacellesWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.KUNDU);
						}
						else 
							methodsMapWeights.put(ComponentEnum.NACELLE, MethodEnum.AVERAGE);
					}
					
				}

				////////////////////////////////////////////////////////////////////////////////////
				String powerPlantWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_power_plant");
				if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) 
						|| theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET)) {

					if(powerPlantWeightsMethod != null) {
						if(powerPlantWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.KUNDU);
						}
						else if(powerPlantWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.TORENBEEK_1976);
						}
						else if(powerPlantWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.TORENBEEK_2013);
						}
						else 
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.AVERAGE);
					}
					
				}
				else if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					if(powerPlantWeightsMethod != null) {
						if(nacellesWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.KUNDU);
						}
						else if(powerPlantWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.TORENBEEK_1976);
						}
						else 
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.AVERAGE);
					}
					
				}
				else if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)) {
					
					if(powerPlantWeightsMethod != null) {
						if(nacellesWeightsMethod.equalsIgnoreCase("KUNDU")) {
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.KUNDU);
						}
						else 
							methodsMapWeights.put(ComponentEnum.POWER_PLANT, MethodEnum.AVERAGE);
					}
					
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String landingGearsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_landing_gears");
				if(landingGearsWeightsMethod != null) {
					if(landingGearsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.TORENBEEK_2013);
					}
					else 
						methodsMapWeights.put(ComponentEnum.LANDING_GEAR, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String apuWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_APU");
				if(apuWeightsMethod != null) {
					if(apuWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.APU, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.APU, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String airConditioningAndAntiIcingWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_air_conditioning_and_anti_icing");
				if(airConditioningAndAntiIcingWeightsMethod != null) {
					if(airConditioningAndAntiIcingWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String instrumentsAndNavigationWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_instruments_and_navigation_system");
				if(instrumentsAndNavigationWeightsMethod != null) {
					if(instrumentsAndNavigationWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String hydraulicAndPneumaticWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_hydraulic_and_pneumatic_systems");
				if(hydraulicAndPneumaticWeightsMethod != null) {
					if(hydraulicAndPneumaticWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String electricalSystemsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_electrical_systems");
				if(electricalSystemsWeightsMethod != null) {
					if(electricalSystemsWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.ELECTRICAL_SYSTEMS, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.ELECTRICAL_SYSTEMS, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String furnishingsAnsEquipmentsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_furnishings_and_equipments");
				if(furnishingsAnsEquipmentsWeightsMethod != null) {
					if(furnishingsAnsEquipmentsWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, MethodEnum.TORENBEEK_1982);
					}
					else if(furnishingsAnsEquipmentsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodsMapWeights.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, MethodEnum.TORENBEEK_2013);
					}
					else 
						methodsMapWeights.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, MethodEnum.AVERAGE);
				}

				////////////////////////////////////////////////////////////////////////////////////
				String controlSurfacesWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_control_surfaces");
				if(controlSurfacesWeightsMethod != null) {
					if(controlSurfacesWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodsMapWeights.put(ComponentEnum.CONTROL_SURFACES, MethodEnum.JENKINSON);
					}
					else if(controlSurfacesWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapWeights.put(ComponentEnum.CONTROL_SURFACES, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapWeights.put(ComponentEnum.CONTROL_SURFACES, MethodEnum.AVERAGE);
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
		Boolean plotBalance = false;
		Boolean createCSVBalance = false;

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
			if(plotBalanceString == null || plotBalanceString.equalsIgnoreCase("FALSE"))
				plotBalance = Boolean.FALSE;
			else if(plotBalanceString.equalsIgnoreCase("TRUE"))
				plotBalance = Boolean.TRUE;
			
			String createCSVBalanceString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//balance/@create_CSV");
			if(createCSVBalanceString == null || createCSVBalanceString.equalsIgnoreCase("FALSE"))
				createCSVBalance = Boolean.FALSE;
			else if(createCSVBalanceString.equalsIgnoreCase("TRUE"))
				createCSVBalance = Boolean.TRUE;

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
				
				////////////////////////////////////////////////////////////////////////////////////
				String canardBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_canard");
				if(canardBalanceMethod != null) {
					if(canardBalanceMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodsMapBalance.put(ComponentEnum.CANARD, MethodEnum.TORENBEEK_1982);
					}
					else 
						methodsMapBalance.put(ComponentEnum.CANARD, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String nacellesBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_nacelles");
				if(nacellesBalanceMethod != null) {
					if(nacellesBalanceMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapBalance.put(ComponentEnum.NACELLE, MethodEnum.TORENBEEK_1976);
					}
					else 
						methodsMapBalance.put(ComponentEnum.NACELLE, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String powerPlantBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_power_plant");
				if(powerPlantBalanceMethod != null) {
					if(powerPlantBalanceMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodsMapBalance.put(ComponentEnum.POWER_PLANT, MethodEnum.TORENBEEK_1976);
					}
					else 
						methodsMapBalance.put(ComponentEnum.POWER_PLANT, MethodEnum.AVERAGE);
				}
				
				////////////////////////////////////////////////////////////////////////////////////
				String landingGearsBalanceMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//balance/@method_landing_gears");
				if(landingGearsBalanceMethod != null) {
					if(landingGearsBalanceMethod.equalsIgnoreCase("SFORZA")) {
						methodsMapBalance.put(ComponentEnum.LANDING_GEAR, MethodEnum.SFORZA);
					}
					else 
						methodsMapBalance.put(ComponentEnum.LANDING_GEAR, MethodEnum.SFORZA);
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
		Boolean createCSVAerodynamicAndStability = Boolean.FALSE;

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
			
			String createCSVAerodynamicAndStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aerodynamic_and_stability/@create_CSV");
			if(createCSVAerodynamicAndStabilityString.equalsIgnoreCase("FALSE")
					|| createCSVAerodynamicAndStabilityString == null)
				createCSVAerodynamicAndStability = Boolean.FALSE;
			else if(createCSVAerodynamicAndStabilityString.equalsIgnoreCase("TRUE"))
				createCSVAerodynamicAndStability = Boolean.TRUE;

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
		Boolean createCSVPerformance = Boolean.FALSE;

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
			
			String createCSVPerformanceString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//performance/@create_CSV");
			if(createCSVPerformanceString.equalsIgnoreCase("FALSE") 
					|| createCSVPerformanceString == null)
				createCSVPerformance = Boolean.FALSE;
			else if(createCSVPerformanceString.equalsIgnoreCase("TRUE"))
				createCSVPerformance = Boolean.TRUE;

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
		Boolean createCSVCosts = Boolean.FALSE;

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
			
			String createCSVCostsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//costs/@create_CSV");
			if(createCSVCostsString.equalsIgnoreCase("FALSE") 
					|| createCSVCostsString == null)
				createCSVCosts = Boolean.FALSE;
			else if(createCSVCostsString.equalsIgnoreCase("TRUE"))
				createCSVCosts = Boolean.TRUE;

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
		IACAnalysisManager theAnalysisManagerInterface = new IACAnalysisManager.Builder()
				.setId(id)
				.setIterativeLoop(Boolean.valueOf(iterativeLoop))
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(operatingConditions)
				.addAllAnalysisList(analysisList)
				.setPositiveLimitLoadFactor(positiveLimitLoadFactor)
				.setNegativeLimitLoadFactor(negativeLimitLoadFactor)
				.putAllMethodsMapWeights(methodsMapWeights)
				.putAllMethodsMapBalance(methodsMapBalance)
				.addAllTaskListPerfromance(taskListPerformance)
				.addAllTaskListAerodynamicAndStability(taskListAerodynamicAndStability)
				.putAllTaskListCosts(taskListCosts)
				.setPlotWeights(plotWeights)
				.setPlotBalance(plotBalance)
				.setPlotAerodynamicAndStability(plotAerodynamicAndStability)
				.setPlotPerformance(plotPerformance)
				.setPlotCosts(plotCosts)
				.setCreateCSVWeights(createCSVWeights)
				.setCreateCSVBalance(createCSVBalance)
				.setCreateCSVAerodynamicAndStability(createCSVAerodynamicAndStability)
				.setCreateCSVPerformance(createCSVPerformance)
				.setCreateCSVCosts(createCSVCosts)
				.build();
		
		
	
		ACAnalysisManager theAnalysisManager = new ACAnalysisManager();
		theAnalysisManager.setTheAnalysisManagerInterface(theAnalysisManagerInterface);
		
		return theAnalysisManager;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tAircraft Analysis Manager\n")
				.append("\t-------------------------------------\n")
				.append("\tAircraft in exam: " + _theAnalysisManagerInterface.getTheAircraft().getId() + "\n")
				.append("\t·····································\n")
				.append("\tPositive limit load factor: " + _theAnalysisManagerInterface.getPositiveLimitLoadFactor() + "\n")
				.append("\tNegative limit load factor: " + _theAnalysisManagerInterface.getNegativeLimitLoadFactor() + "\n")
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
			sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheWeights().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.BALANCE) == true)
			sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheBalance().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY) == true) {
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.TAKE_OFF))
				sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).toString());
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CLIMB))
				sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB).toString());
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CRUISE))
				sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).toString());
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.LANDING))
				sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).toString());
		}
		if(_executedAnalysesMap.get(AnalysisTypeEnum.PERFORMANCE) == true)
			sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getThePerformance().toString());
		if(_executedAnalysesMap.get(AnalysisTypeEnum.COSTS) == true)
			sb.append(_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheCosts().toString());
		
		return sb.toString();
	}
	
	/**
	 * Evaluate dependent data
	 */
	public void calculateDependentVariables() {

		//-------------------------------------------------------------------
		_executedAnalysesMap = new HashMap<>();
		_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, false);
		_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, false);
		_executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, false);
		_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, false);
		_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, false);
		//-------------------------------------------------------------------
		
		_nUltimate = 1.5 * _theAnalysisManagerInterface.getPositiveLimitLoadFactor();
		
		// Maximum cruise TAS
		_vMaxCruise = Amount.valueOf(
				_theAnalysisManagerInterface.getTheOperatingConditions().getMachCruise() * 
				OperatingConditions.getAtmosphere(
						_theAnalysisManagerInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)).getSpeedOfSound(), 
				SI.METERS_PER_SECOND);
		_vMaxCruiseEAS = _vMaxCruise.
				divide(Math.sqrt(
						OperatingConditions.getAtmosphere(
								_theAnalysisManagerInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)).getDensityRatio()));

		_vOptimumCruise = Amount.valueOf(_theAnalysisManagerInterface.getTheOperatingConditions().getMachCruise()
				*AtmosphereCalc.getSpeedOfSound(
						_theAnalysisManagerInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)), SI.METERS_PER_SECOND);
		
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
			) throws IOException, HDF5LibraryException {

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		if (aircraft == null) return;
		////////////////////////////////////////////////////////////////
		// ITERATIVE LOOP 
		if(_theAnalysisManagerInterface.isIterativeLoop() == true)
			executeAnalysisIterativeLoop(aircraft, theOperatingConditions);
		
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.WEIGHTS)) {
			System.setOut(originalOut);
			System.out.println("\t\tWeights Analysis :: START");
			System.setOut(filterStream);
			_theWeights = ACWeightsManager.importFromXML(
					_weightsFileComplete.getAbsolutePath(),
					aircraft,
					theOperatingConditions
					);
			calculateWeights(aircraft, theOperatingConditions, resultsFolderPath); 
			_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, true);
			System.setOut(originalOut);
			System.out.println("\t\tWeights Analysis :: COMPLETE\n");
			System.setOut(filterStream);
		}
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.BALANCE)) {
			System.setOut(originalOut);
			System.out.println("\t\tBalance Analysis :: START");
			System.setOut(filterStream);
			_theBalance = ACBalanceManager.importFromXML(
					_balanceFileComplete.getAbsolutePath(),
					aircraft
					);
			calculateBalance(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, true);
			System.setOut(originalOut);
			System.out.println("\t\tBalance Analysis :: COMPLETE \n");
			System.setOut(filterStream);
		}
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY)) {
			
			_theAerodynamicAndStability = new HashMap<>();
			
			_theAerodynamicAndStability.put(ConditionEnum.TAKE_OFF, new ACAerodynamicAndStabilityManager());
			_theAerodynamicAndStability.put(ConditionEnum.CLIMB, new ACAerodynamicAndStabilityManager());
			_theAerodynamicAndStability.put(ConditionEnum.CRUISE, new ACAerodynamicAndStabilityManager());
			_theAerodynamicAndStability.put(ConditionEnum.LANDING, new ACAerodynamicAndStabilityManager());
			
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.TAKE_OFF)) {
				System.setOut(originalOut);
				System.out.println("\t\tAerodynamic and Stability Analysis (TAKE-OFF) :: START");
				System.setOut(filterStream);
				_theAerodynamicAndStability.remove(ConditionEnum.TAKE_OFF);
				_theAerodynamicAndStability.put(
						ConditionEnum.TAKE_OFF,
						ACAerodynamicAndStabilityManager.importFromXML(
								_aerodynamicAndStabilityTakeOffFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.TAKE_OFF
								)
						);
			}
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CLIMB)) {
				System.setOut(originalOut);
				System.out.println("\t\tAerodynamic and Stability Analysis (CLIMB) :: START");
				System.setOut(filterStream);
				_theAerodynamicAndStability.remove(ConditionEnum.CLIMB);
				_theAerodynamicAndStability.put(
						ConditionEnum.CLIMB,
						ACAerodynamicAndStabilityManager.importFromXML(
								_aerodynamicAndStabilityClimbFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CLIMB
								)
						);
			}
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CRUISE)) {
				System.setOut(originalOut);
				System.out.println("\t\tAerodynamic and Stability Analysis (CRUISE) :: START");
				System.setOut(filterStream);
				_theAerodynamicAndStability.remove(ConditionEnum.CRUISE);
				_theAerodynamicAndStability.put(
						ConditionEnum.CRUISE,
						ACAerodynamicAndStabilityManager.importFromXML(
								_aerodynamicAndStabilityCruiseFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CRUISE
								)
						);
			}
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.LANDING)) {
				System.setOut(originalOut);
				System.out.println("\t\tAerodynamic and Stability Analysis (LANDING) :: START");
				System.setOut(filterStream);
				_theAerodynamicAndStability.remove(ConditionEnum.LANDING);
				_theAerodynamicAndStability.put(
						ConditionEnum.LANDING,
						ACAerodynamicAndStabilityManager.importFromXML(
								_aerodynamicAndStabilityLandingFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.LANDING
								)
						);
			}
			
			calculateAerodynamicAndStability(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY, true);
			System.setOut(originalOut);
			System.out.println("\t\tAerodynamic and Stability Analysis :: COMPLETE\n");
			System.setOut(filterStream);
		}
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.PERFORMANCE)) {
			System.setOut(originalOut);
			System.out.println("\t\tPerformance Analysis :: START");
			System.setOut(filterStream);
			_thePerformance = ACPerformanceManager.importFromXML(
					_performanceFileComplete.getAbsolutePath(), 
					aircraft,
					theOperatingConditions
					);
			calculatePerformances(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, true);
			System.setOut(originalOut);
			System.out.println("\t\tPerformance Analysis :: COMPLETE \n");
			System.setOut(filterStream);
		}
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.COSTS)) {
			System.setOut(originalOut);
			System.out.println("\t\tCosts Analysis :: START");
			System.setOut(filterStream);
			_theCosts = ACCostsManager.importFromXML(
					_costsFileComplete.getAbsolutePath(), 
					aircraft, 
					theOperatingConditions, 
					_theAnalysisManagerInterface.getTaskListCosts()
					);
			calculateCosts(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, true);
			System.setOut(originalOut);
			System.out.println("\t\tCosts Analysis :: COMPLETE \n");
			System.setOut(filterStream);
		}
				
	} // end of constructor

	/*
	 * This method execute a preliminary iterative loop until the calculated mission fuel mass is equal
	 * to the initial fuel mass from the weights analysis.
	 */
	private Amount<Mass> executeAnalysisIterativeLoop(Aircraft aircraft, OperatingConditions operatingConditions) throws HDF5LibraryException, IOException {
		
		/*
		 * 1) ASSIGN DEFAULT TASK_LISTS AND METHODS MAP FOR EACH ANALYSIS AND BUILD TEMPORARY MANAGERS
		 * 2) PERFORM ALL REQUIRED ANALYSES
		 * 3) RETURN THE FINAL FUEL WEIGHTS
		 * 
		 * N.B.: MODIFY THE WEIGHTS IMPORT FROM XML ALLOWING USERS TO ASSIGN THE WANTED FUEL WEIGHT. 
		 *       THIS WILL BE UPDATED FOR THE CALCULATION AFTER THE ITERATIVE LOOP.
		 */
		Amount<Mass> finalFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);		
		
		// TODO: COMPLETE ME!
		
		return finalFuelMass;
		
	}
	
	public void calculateWeights(Aircraft aircraft, OperatingConditions operatingConditions, String resultsFolderPath) {

		// Evaluate aircraft masses
		aircraft.getTheAnalysisManager().getTheWeights().calculateAllMasses(aircraft, operatingConditions, _theAnalysisManagerInterface.getMethodsMapWeights());

		// Plot and print
		try {
			String weightsFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "WEIGHTS"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheWeights().toXLSFile(
					weightsFolderPath
					+ "Weights");
			if(_theAnalysisManagerInterface.isPlotWeights() == true)
				aircraft.getTheAnalysisManager().getTheWeights().plotWeightBreakdown(weightsFolderPath);
			
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	public void calculateBalance(Aircraft aircraft, String resultsFolderPath) {

		// Estimate center of gravity location
		aircraft.getTheAnalysisManager().getTheBalance().calculate(_theAnalysisManagerInterface.getMethodsMapBalance());
		
		// Plot
		try {
			String balanceFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "BALANCE"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheBalance().toXLSFile(
					balanceFolderPath
					+ "Balance");
			if(_theAnalysisManagerInterface.isPlotBalance() == Boolean.TRUE)
				aircraft.getTheAnalysisManager().getTheBalance().createCharts(balanceFolderPath);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Evaluate arms again with the new CG estimate
		aircraft.calculateArms(
				aircraft.getHTail(),
				_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getXBRF()
				);
		aircraft.calculateArms(
				aircraft.getVTail(),
				_theAnalysisManagerInterface.getTheAircraft().getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getXBRF()
				);

	}
	
	public void calculateAerodynamicAndStability(Aircraft aircraft, String resultsFolderPath) {

		if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.TAKE_OFF)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CLIMB)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.CRUISE)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.LANDING)) 
			aircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).calculate(resultsFolderPath);

	}
	
	public void calculatePerformances(Aircraft aircraft, String resultsFolderPath) {

		// Execute analysis
		aircraft.getTheAnalysisManager().getThePerformance().calculate(resultsFolderPath);
		
	}
	
	public void calculateCosts(Aircraft aircraft, String resultsFolderPath) {
		
		// Execute analysis
		aircraft.getTheAnalysisManager().getTheCosts().calculate(resultsFolderPath);
		
	}

	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	public IACAnalysisManager getTheAnalysisManagerInterface() {
		return _theAnalysisManagerInterface;
	}
	
	public void setTheAnalysisManagerInterface (IACAnalysisManager theAnalysisManagerInterface) {
		this._theAnalysisManagerInterface = theAnalysisManagerInterface;
	}
	
	public String getId() {
		return _theAnalysisManagerInterface.getId();
	}

	public void setId(String _id) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setId(_id).build());
	}

	public Aircraft getTheAircraft() {
		return _theAnalysisManagerInterface.getTheAircraft();
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setTheAircraft(_theAircraft).build());
	}

	public Double getPositiveLimitLoadFactor() {
		return _theAnalysisManagerInterface.getPositiveLimitLoadFactor();
	}

	public void setPositiveLimitLoadFactor(Double _positiveLimitLoadFactor) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPositiveLimitLoadFactor(_positiveLimitLoadFactor).build());
	}

	public Double getNegativeLimitLoadFactor() {
		return _theAnalysisManagerInterface.getNegativeLimitLoadFactor();
	}
	
	public void setNegativeLimitLoadFactor(Double _negativeLimitLoadFactor) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setNegativeLimitLoadFactor(_negativeLimitLoadFactor).build());
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
		return _theAnalysisManagerInterface.getMethodsMapWeights();
	}

	public void setMethodsMapWeights(Map<ComponentEnum, MethodEnum> _methodsMap) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).putAllMethodsMapWeights(_methodsMap).build());
	}

	public Map<ComponentEnum, MethodEnum> getMethodsMapBalance() {
		return _theAnalysisManagerInterface.getMethodsMapBalance();
	}

	public void setMethodsMapBalance(Map<ComponentEnum, MethodEnum> _methodsMapBalance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).putAllMethodsMapBalance(_methodsMapBalance).build());
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

	public Map<ConditionEnum, ACAerodynamicAndStabilityManager> getTheAerodynamicAndStability() {
		return _theAerodynamicAndStability;
	}

	public void setTheAerodynamicAndStability(Map<ConditionEnum, ACAerodynamicAndStabilityManager> theAerodynamicAndStability) {
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
		return _theAnalysisManagerInterface.getAnalysisList();
	}

	public void setAnalysisList(List<AnalysisTypeEnum> _analysisList) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).addAllAnalysisList(_analysisList).build());
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
		return _theAnalysisManagerInterface.isPlotBalance();
	}

	public void setPlotBalance(Boolean _plotBalance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotBalance(_plotBalance).build());
	}

	public Boolean getPlotAerodynamicAndStability() {
		return _theAnalysisManagerInterface.isPlotAerodynamicAndStability();
	}

	public void setPlotAerodynamicAndStability(Boolean _plotAerodynamicAndStability) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotAerodynamicAndStability(_plotAerodynamicAndStability).build());
	}

	public Boolean getPlotPerformance() {
		return _theAnalysisManagerInterface.isPlotPerformance();
	}

	public void setPlotPerformance(Boolean _plotPerformance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotPerformance(_plotPerformance).build());
	}

	public Boolean getPlotCosts() {
		return _theAnalysisManagerInterface.isPlotCosts();
	}

	public void setPlotCosts(Boolean _plotCosts) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotCosts(_plotCosts).build());
	}

	public List<PerformanceEnum> getTaskListPerformance() {
		return _theAnalysisManagerInterface.getTaskListPerfromance();
	}

	public void setTaskListPerformance(List<PerformanceEnum> _taskListPerformance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).addAllTaskListPerfromance(_taskListPerformance).build());
	}

	public List<ConditionEnum> getTaskListAerodynamicAndStability() {
		return _theAnalysisManagerInterface.getTaskListAerodynamicAndStability();
	}

	public void setTaskListAerodynamicAndStability(List<ConditionEnum> _taskListAerodynamicAndStability) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).addAllTaskListAerodynamicAndStability(_taskListAerodynamicAndStability).build());
	}

	public Map<CostsEnum, MethodEnum> getTaskListCosts() {
		return _theAnalysisManagerInterface.getTaskListCosts();
	}

	public void setTaskListCosts(Map<CostsEnum, MethodEnum> _taskListCosts) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).putAllTaskListCosts(_taskListCosts).build());
	}

	public Boolean getPlotWeights() {
		return _theAnalysisManagerInterface.isPlotWeights();
	}

	public void setPlotWeights(Boolean _plotWeights) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotWeights(_plotWeights).build());
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theAnalysisManagerInterface.getTheOperatingConditions();
	}

	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setTheOperatingConditions(_theOperatingConditions).build());
	}

	public Boolean getCreateCSVWeights() {
		return _theAnalysisManagerInterface.isCreateCSVWeights();
	}

	public void setCreateCSVWeights(Boolean _createCSVWeights) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVWeights(_createCSVWeights).build());
	}

	public Boolean getCreateCSVBalance() {
		return _theAnalysisManagerInterface.isCreateCSVBalance();
	}

	public void setCreateCSVBalance(Boolean _createCSVBalance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVBalance(_createCSVBalance).build());
	}

	public Boolean getCreateCSVAerodynamicAndStability() {
		return _theAnalysisManagerInterface.isCreateCSVAerodynamicAndStability();
	}

	public void setCreateCSVAerodynamicAndStability(Boolean _createCSVAerodynamicAndStability) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVAerodynamicAndStability(_createCSVAerodynamicAndStability).build());
	}

	public Boolean getCreateCSVPerformance() {
		return _theAnalysisManagerInterface.isCreateCSVPerformance();
	}

	public void setCreateCSVPerformance(Boolean _createCSVPerformance) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVPerformance(_createCSVPerformance).build());
	}

	public Boolean getCreateCSVCosts() {
		return _theAnalysisManagerInterface.isCreateCSVCosts();
	}

	public void setCreateCSVCosts(Boolean _createCSVCosts) {
		setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVCosts(_createCSVCosts).build());
	}

}