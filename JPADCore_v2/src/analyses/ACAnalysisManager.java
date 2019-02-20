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
import configuration.enumerations.AerodynamicAndStabilityEnum;
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
	private Map<ConditionEnum, ACAerodynamicAndStabilityManager_v2> _theAerodynamicAndStability;
	private Map<ConditionEnum, ACDynamicStabilityManager> _theDynamicStability;
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
	private static File _dynamicStabilityTakeOffFileComplete;
	private static File _dynamicStabilityClimbFileComplete;
	private static File _dynamicStabilityCruiseFileComplete;
	private static File _dynamicStabilityLandingFileComplete;
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
		
		boolean iterativeLoop = false;
		String iterativeLoopString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@iterative_loop");
		if(iterativeLoopString != null)
			if(iterativeLoopString.equalsIgnoreCase("FALSE"))
				iterativeLoop = false;
			else
				iterativeLoop = true;
		
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
		Map<ComponentEnum, List<MethodEnum>> methodsMapWeights = new HashMap<>();
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
				List<MethodEnum> methodEnumListFuselage = new ArrayList<>();
				if (fuselageWeightsMethod != null) {
					if(fuselageWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodEnumListFuselage.add(MethodEnum.RAYMER);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodEnumListFuselage.add(MethodEnum.TORENBEEK_1976);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodEnumListFuselage.add(MethodEnum.TORENBEEK_2013);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListFuselage.add(MethodEnum.JENKINSON);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("KROO")) {
						methodEnumListFuselage.add(MethodEnum.KROO);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodEnumListFuselage.add(MethodEnum.SADRAEY);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("NICOLAI_1984")) {
						methodEnumListFuselage.add(MethodEnum.NICOLAI_1984);
					}
					else if(fuselageWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodEnumListFuselage.add(MethodEnum.ROSKAM);
					}
					else 
						methodEnumListFuselage.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.FUSELAGE, methodEnumListFuselage);
				
				////////////////////////////////////////////////////////////////////////////////////
				String wingWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_wing");
				List<MethodEnum> methodEnumListWing = new ArrayList<>();
				if(wingWeightsMethod != null) {
					if(wingWeightsMethod.equalsIgnoreCase("KROO")) {
						methodEnumListWing.add(MethodEnum.KROO);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListWing.add(MethodEnum.JENKINSON);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodEnumListWing.add(MethodEnum.TORENBEEK_2013);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListWing.add(MethodEnum.TORENBEEK_1982);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodEnumListWing.add(MethodEnum.RAYMER);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodEnumListWing.add(MethodEnum.SADRAEY);
					}
					else if(wingWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodEnumListWing.add(MethodEnum.ROSKAM);
					}
					else 
						methodEnumListWing.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.WING, methodEnumListWing);
				
				////////////////////////////////////////////////////////////////////////////////////			
				String hTailWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_htail");
				List<MethodEnum> methodEnumListHTail = new ArrayList<>();
				if(hTailWeightsMethod != null) {
					if(hTailWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodEnumListHTail.add(MethodEnum.HOWE);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListHTail.add(MethodEnum.JENKINSON);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("NICOLAI_2013")) {
						methodEnumListHTail.add(MethodEnum.NICOLAI_2013);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("KROO")) {
						methodEnumListHTail.add(MethodEnum.KROO);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodEnumListHTail.add(MethodEnum.RAYMER);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodEnumListHTail.add(MethodEnum.SADRAEY);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodEnumListHTail.add(MethodEnum.ROSKAM);
					}
					else if(hTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodEnumListHTail.add(MethodEnum.TORENBEEK_1976);
					}
					else 
						methodEnumListHTail.add(MethodEnum.AVERAGE);
				}

				methodsMapWeights.put(ComponentEnum.HORIZONTAL_TAIL, methodEnumListHTail);
				
				////////////////////////////////////////////////////////////////////////////////////
				String vTailWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_vtail");
				List<MethodEnum> methodEnumListVTail = new ArrayList<>();
				if(vTailWeightsMethod != null) {
					if(vTailWeightsMethod.equalsIgnoreCase("KROO")) {
						methodEnumListVTail.add(MethodEnum.KROO);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListVTail.add(MethodEnum.JENKINSON);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodEnumListVTail.add(MethodEnum.HOWE);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodEnumListVTail.add(MethodEnum.RAYMER);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodEnumListVTail.add(MethodEnum.SADRAEY);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodEnumListVTail.add(MethodEnum.ROSKAM);
					}
					else if(vTailWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodEnumListVTail.add(MethodEnum.TORENBEEK_1976);
					}
					else 
						methodEnumListVTail.add(MethodEnum.AVERAGE);
				}

				methodsMapWeights.put(ComponentEnum.VERTICAL_TAIL, methodEnumListVTail);
				
				////////////////////////////////////////////////////////////////////////////////////
				String canardWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_canard");
				List<MethodEnum> methodEnumListCanard = new ArrayList<>();
				if(canardWeightsMethod != null) {
					if(canardWeightsMethod.equalsIgnoreCase("HOWE")) {
						methodEnumListCanard.add(MethodEnum.HOWE);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListCanard.add(MethodEnum.JENKINSON);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("NICOLAI_2013")) {
						methodEnumListCanard.add(MethodEnum.NICOLAI_2013);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("KROO")) {
						methodEnumListCanard.add(MethodEnum.KROO);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("RAYMER")) {
						methodEnumListCanard.add(MethodEnum.RAYMER);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("SADRAEY")) {
						methodEnumListCanard.add(MethodEnum.SADRAEY);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("ROSKAM")) {
						methodEnumListCanard.add(MethodEnum.ROSKAM);
					}
					else if(canardWeightsMethod.equalsIgnoreCase("TORENBEEK_1976")) {
						methodEnumListCanard.add(MethodEnum.TORENBEEK_1976);
					}
					else 
						methodEnumListCanard.add(MethodEnum.AVERAGE);
				}

				methodsMapWeights.put(ComponentEnum.CANARD, methodEnumListCanard);
				
				////////////////////////////////////////////////////////////////////////////////////
				String nacellesWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_nacelles");
				String[] nacellesWeightsMethodArray = nacellesWeightsMethod.split(",");
				List<MethodEnum> methodEnumListNacelles = new ArrayList<>();
				
				if(!nacellesWeightsMethod.isEmpty() && nacellesWeightsMethodArray.length != theAircraft.getNacelles().getNacellesNumber()) {
					System.err.println("WARNING (IMPORT ANALYSIS DATA - NACELLES WEIGTHS METHODS): THE NUMBER OF NACELLE METHODS MUST BE EQUAL TO THE NUMBER OF NACELLES. OTHERWISE LEAVE BLANCK TO CALCULATE AN AVERAGED VALUE. TERMINATING ...");
					System.exit(1);
				}
				
				for (int i=0; i<theAircraft.getNacelles().getNacellesNumber(); i++) {
					if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOFAN) 
							|| theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOJET)) {

						if(nacellesWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(nacellesWeightsMethodArray[i].equalsIgnoreCase("JENKINSON")) {
								methodEnumListNacelles.add(MethodEnum.JENKINSON);
							}
							else if(nacellesWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListNacelles.add(MethodEnum.KUNDU);
							}
							else if(nacellesWeightsMethodArray[i].equalsIgnoreCase("ROSKAM")) {
								methodEnumListNacelles.add(MethodEnum.ROSKAM);
							}
						}
						else 
							methodEnumListNacelles.add(MethodEnum.AVERAGE);

					}
					else if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOPROP)) {

						if(nacellesWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(nacellesWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListNacelles.add(MethodEnum.KUNDU);
							}
							else if(nacellesWeightsMethodArray[i].equalsIgnoreCase("TORENBEEK_1976")) {
								methodEnumListNacelles.add(MethodEnum.TORENBEEK_1976);
							}
						}
						else 
							methodEnumListNacelles.add(MethodEnum.AVERAGE);

					}
					else if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.PISTON)) {

						if(nacellesWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(nacellesWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListNacelles.add(MethodEnum.KUNDU);
							}
						}
						else 
							methodEnumListNacelles.add(MethodEnum.AVERAGE);
					}
				}

				methodsMapWeights.put(ComponentEnum.NACELLE, methodEnumListNacelles);

				////////////////////////////////////////////////////////////////////////////////////
				String powerPlantWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_power_plant");
				String[] powerPlantWeightsMethodArray = nacellesWeightsMethod.split(",");
				List<MethodEnum> methodEnumListPowerPlant = new ArrayList<>();
				
				if(!powerPlantWeightsMethod.isEmpty() && powerPlantWeightsMethodArray.length != theAircraft.getPowerPlant().getEngineNumber()) {
					System.err.println("WARNING (IMPORT ANALYSIS DATA - POWER PLANT WEIGTHS METHODS): THE NUMBER OF ENGINE METHODS MUST BE EQUAL TO THE NUMBER OF ENGINES. OTHERWISE LEAVE BLANCK TO CALCULATE AN AVERAGED VALUE. TERMINATING ...");
					System.exit(1);
				}
				
				for (int i=0; i<theAircraft.getPowerPlant().getEngineNumber(); i++) {
					if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOFAN) 
							|| theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOJET)) {

						if(powerPlantWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListPowerPlant.add(MethodEnum.KUNDU);
							}
							else if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("TORENBEEK_1976")) {
								methodEnumListPowerPlant.add(MethodEnum.TORENBEEK_1976);
							}
							else if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("TORENBEEK_2013")) {
								methodEnumListPowerPlant.add(MethodEnum.TORENBEEK_2013);
							}
						}
						else 
							methodEnumListPowerPlant.add(MethodEnum.AVERAGE);

					}
					else if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.TURBOPROP)) {

						if(powerPlantWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListPowerPlant.add(MethodEnum.KUNDU);
							}
							else if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("TORENBEEK_1976")) {
								methodEnumListPowerPlant.add(MethodEnum.TORENBEEK_1976);
							}
						}
						else 
							methodEnumListPowerPlant.add(MethodEnum.AVERAGE);

					}
					else if(theAircraft.getPowerPlant().getEngineType().get(i).equals(EngineTypeEnum.PISTON)) {

						if(powerPlantWeightsMethod != null && !nacellesWeightsMethod.isEmpty()) {
							if(powerPlantWeightsMethodArray[i].equalsIgnoreCase("KUNDU")) {
								methodEnumListPowerPlant.add(MethodEnum.KUNDU);
							}
						}
						else 
							methodEnumListPowerPlant.add(MethodEnum.AVERAGE);
					}
				}
				
				methodsMapWeights.put(ComponentEnum.POWER_PLANT, methodEnumListPowerPlant);
				
				////////////////////////////////////////////////////////////////////////////////////
				String landingGearsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_landing_gears");
				List<MethodEnum> methodEnumListLandingGears = new ArrayList<>();
				if(landingGearsWeightsMethod != null) {
					if(landingGearsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodEnumListLandingGears.add(MethodEnum.TORENBEEK_2013);
					}
					else 
						methodEnumListLandingGears.add(MethodEnum.AVERAGE);
				}

				methodsMapWeights.put(ComponentEnum.LANDING_GEAR, methodEnumListLandingGears);
				
				////////////////////////////////////////////////////////////////////////////////////
				String apuWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_APU");
				List<MethodEnum> methodEnumListAPU = new ArrayList<>();
				if(apuWeightsMethod != null) {
					if(apuWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListAPU.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListAPU.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.APU, methodEnumListAPU);
				
				////////////////////////////////////////////////////////////////////////////////////
				String airConditioningAndAntiIcingWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_air_conditioning_and_anti_icing");
				List<MethodEnum> methodEnumListAirConditioningAndAntiIcingWeights = new ArrayList<>();
				if(airConditioningAndAntiIcingWeightsMethod != null) {
					if(airConditioningAndAntiIcingWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListAirConditioningAndAntiIcingWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListAirConditioningAndAntiIcingWeights.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, methodEnumListAirConditioningAndAntiIcingWeights);
				
				////////////////////////////////////////////////////////////////////////////////////
				String instrumentsAndNavigationWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_instruments_and_navigation_system");
				List<MethodEnum> methodEnumListInstrumentsAndNavigationWeights = new ArrayList<>();
				if(instrumentsAndNavigationWeightsMethod != null) {
					if(instrumentsAndNavigationWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListInstrumentsAndNavigationWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListInstrumentsAndNavigationWeights.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, methodEnumListInstrumentsAndNavigationWeights);

				////////////////////////////////////////////////////////////////////////////////////
				String hydraulicAndPneumaticWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_hydraulic_and_pneumatic_systems");
				List<MethodEnum> methodEnumListHydraulicAndPneumaticWeights = new ArrayList<>();
				if(hydraulicAndPneumaticWeightsMethod != null) {
					if(hydraulicAndPneumaticWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListHydraulicAndPneumaticWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListHydraulicAndPneumaticWeights.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, methodEnumListHydraulicAndPneumaticWeights);
				
				////////////////////////////////////////////////////////////////////////////////////
				String electricalSystemsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_electrical_systems");
				List<MethodEnum> methodEnumListElectricalSystemsWeights = new ArrayList<>();
				if(electricalSystemsWeightsMethod != null) {
					if(electricalSystemsWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListElectricalSystemsWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListElectricalSystemsWeights.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.ELECTRICAL_SYSTEMS, methodEnumListElectricalSystemsWeights);
				
				////////////////////////////////////////////////////////////////////////////////////
				String furnishingsAnsEquipmentsWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_furnishings_and_equipments");
				List<MethodEnum> methodEnumListFurnishingsAnsEquipmentsWeights = new ArrayList<>();
				if(furnishingsAnsEquipmentsWeightsMethod != null) {
					if(furnishingsAnsEquipmentsWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListFurnishingsAnsEquipmentsWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else if(furnishingsAnsEquipmentsWeightsMethod.equalsIgnoreCase("TORENBEEK_2013")) {
						methodEnumListFurnishingsAnsEquipmentsWeights.add(MethodEnum.TORENBEEK_2013);
					}
					else 
						methodEnumListFurnishingsAnsEquipmentsWeights.add(MethodEnum.AVERAGE);
				}

				methodsMapWeights.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, methodEnumListFurnishingsAnsEquipmentsWeights);
				
				////////////////////////////////////////////////////////////////////////////////////
				String controlSurfacesWeightsMethod = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//weights/@method_control_surfaces");
				List<MethodEnum> methodEnumListControlSurfacesWeights = new ArrayList<>();
				if(controlSurfacesWeightsMethod != null) {
					if(controlSurfacesWeightsMethod.equalsIgnoreCase("JENKINSON")) {
						methodEnumListControlSurfacesWeights.add(MethodEnum.JENKINSON);
					}
					else if(controlSurfacesWeightsMethod.equalsIgnoreCase("TORENBEEK_1982")) {
						methodEnumListControlSurfacesWeights.add(MethodEnum.TORENBEEK_1982);
					}
					else 
						methodEnumListControlSurfacesWeights.add(MethodEnum.AVERAGE);
				}
				
				methodsMapWeights.put(ComponentEnum.CONTROL_SURFACES, methodEnumListControlSurfacesWeights);
				
			}

			
			_weightsFileComplete = new File(
					MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
					+ File.separator 
					+ "Template_Analyses"
					+ File.separator
					+ weightsFile
					);
		}
		else {
			if (iterativeLoop == true) {
				System.err.println("WARNING (IMPORT ANALYSIS DATA - WEIGHTS): ITERATIVE LOOP CANNOT BE PERFORMED IF THE WEIGHTS ANALYSIS IS NOT DEFINED. TERMINATING ...");
				System.exit(1);
			}
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
		else {
			if (iterativeLoop == true) {
				System.err.println("WARNING (IMPORT ANALYSIS DATA - BALANCE): ITERATIVE LOOP CANNOT BE PERFORMED IF THE BALANCE ANALYSIS IS NOT DEFINED. TERMINATING ...");
				System.exit(1);
			}
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
		else {
			if (iterativeLoop == true) {
				System.err.println("WARNING (IMPORT ANALYSIS DATA - AERODYNAMIC AND STABILITY): ITERATIVE LOOP CANNOT BE PERFORMED IF AERODYNAMIC AND STABILITY ANALYSIS IS NOT DEFINED. TERMINATING ...");
				System.exit(1);
			}
		}

		//-------------------------------------------------------------------------------------------
		// DYNAMIC STABILITY ANALYSIS:
		List<ConditionEnum> taskListDynamicStability = new ArrayList<ConditionEnum>();
		Boolean plotDynamicStability = Boolean.FALSE;
		Boolean createCSVDynamicStability = Boolean.FALSE;

		NodeList dynamicStabilityTag = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//dynamic_stability");
		Node dynamicStabilityNode = dynamicStabilityTag.item(0);

		if(dynamicStabilityNode != null) {

			analysisList.add(AnalysisTypeEnum.DYNAMIC_STABILITY);

			String plotDynamicStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@plot");
			if(plotDynamicStabilityString.equalsIgnoreCase("FALSE")
					|| plotDynamicStabilityString == null)
				plotDynamicStability = Boolean.FALSE;
			else if(plotDynamicStabilityString.equalsIgnoreCase("TRUE"))
				plotDynamicStability = Boolean.TRUE;

			String createCSVDynamicStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@create_CSV");
			if(createCSVDynamicStabilityString.equalsIgnoreCase("FALSE")
					|| createCSVDynamicStabilityString == null)
				createCSVDynamicStability = Boolean.FALSE;
			else if(createCSVDynamicStabilityString.equalsIgnoreCase("TRUE"))
				createCSVDynamicStability = Boolean.TRUE;

			//---------------------------------------------------------------------------------------------------------
			// TAKE-OFF CONDITION
			String dynamicStabilityTakeOff = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@take_off_condition");

			if(dynamicStabilityTakeOff != null)  {		

				Boolean takeOffConditionFlag = null;
				if(dynamicStabilityTakeOff.equalsIgnoreCase("TRUE")) {
					takeOffConditionFlag = Boolean.TRUE;
				}
				else if(dynamicStabilityTakeOff.equalsIgnoreCase("FALSE") 
						|| dynamicStabilityTakeOff == null) {
					takeOffConditionFlag = Boolean.FALSE;
				}

				if(takeOffConditionFlag == Boolean.TRUE) 
					taskListDynamicStability.add(ConditionEnum.TAKE_OFF);

				String dynamicStabilityTakeOffFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//dynamic_stability/@file_take_off_condition");

				_dynamicStabilityTakeOffFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ dynamicStabilityTakeOffFile
						);
			}

			//---------------------------------------------------------------------------------------------------------
			// CLIMB CONDITION
			String dynamicStabilityClimb = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@climb_condition");

			if(dynamicStabilityClimb != null)  {		

				Boolean climbConditionFlag = null;
				if(dynamicStabilityClimb.equalsIgnoreCase("TRUE")) {
					climbConditionFlag = Boolean.TRUE;
				}
				else if(dynamicStabilityClimb.equalsIgnoreCase("FALSE") 
						|| dynamicStabilityClimb == null) {
					climbConditionFlag = Boolean.FALSE;
				}

				if(climbConditionFlag == Boolean.TRUE) 
					taskListDynamicStability.add(ConditionEnum.CLIMB);

				String dynamicStabilityClimbFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//dynamic_stability/@file_climb_condition");

				_dynamicStabilityClimbFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ dynamicStabilityClimbFile
						);
			}

			//---------------------------------------------------------------------------------------------------------
			// CRUISE CONDITION
			String dynamicStabilityCruise = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@cruise_condition");

			if(dynamicStabilityCruise != null)  {		

				Boolean cruiseConditionFlag = null;
				if(dynamicStabilityCruise.equalsIgnoreCase("TRUE")) {
					cruiseConditionFlag = Boolean.TRUE;
				}
				else if(dynamicStabilityCruise.equalsIgnoreCase("FALSE") 
						|| dynamicStabilityCruise == null) {
					cruiseConditionFlag = Boolean.FALSE;
				}

				if(cruiseConditionFlag == Boolean.TRUE) 
					taskListDynamicStability.add(ConditionEnum.CRUISE);

				String dynamicStabilityCruiseFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//dynamic_stability/@file_cruise_condition");

				_dynamicStabilityCruiseFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ dynamicStabilityCruiseFile
						);
			}

			//---------------------------------------------------------------------------------------------------------
			// LANDING CONDITION
			String dynamicStabilityLanding = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/@landing_condition");

			if(dynamicStabilityLanding != null)  {		

				Boolean landingConditionFlag = null;
				if(dynamicStabilityLanding.equalsIgnoreCase("TRUE")) {
					landingConditionFlag = Boolean.TRUE;
				}
				else if(dynamicStabilityLanding.equalsIgnoreCase("FALSE") 
						|| dynamicStabilityLanding == null) {
					landingConditionFlag = Boolean.FALSE;
				}

				if(landingConditionFlag == Boolean.TRUE) 
					taskListDynamicStability.add(ConditionEnum.CRUISE);

				String dynamicStabilityLandingFile = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//dynamic_stability/@file_landing_condition");

				_dynamicStabilityLandingFileComplete = new File(
						MyConfiguration.getDir(FoldersEnum.INPUT_DIR)
						+ File.separator 
						+ "Template_Analyses"
						+ File.separator
						+ dynamicStabilityLandingFile
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
				Boolean noiseTrajectoriesFlag = Boolean.FALSE;
				String noiseTrajectoriesFlagProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//performance/@noise_trajectories");
				if (noiseTrajectoriesFlagProperty != null) {
					if(noiseTrajectoriesFlagProperty.equalsIgnoreCase("TRUE")) {
						noiseTrajectoriesFlag = Boolean.TRUE;
					}
					else if(noiseTrajectoriesFlagProperty.equalsIgnoreCase("FALSE")) {
						noiseTrajectoriesFlag = Boolean.FALSE;
					}
				}
				if(noiseTrajectoriesFlag == Boolean.TRUE) 
					taskListPerformance.add(PerformanceEnum.NOISE_TRAJECTORIES);

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
		else {
			if (iterativeLoop == true) {
				System.err.println("WARNING (IMPORT ANALYSIS DATA - PERFORMANCE): ITERATIVE LOOP CANNOT BE PERFORMED IF THE PERFORMANCE ANALYSIS IS NOT DEFINED. TERMINATING ...");
				System.exit(1);
			}
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
				.setIterativeLoop(iterativeLoop)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(operatingConditions)
				.addAllAnalysisList(analysisList)
				.setPositiveLimitLoadFactor(positiveLimitLoadFactor)
				.setNegativeLimitLoadFactor(negativeLimitLoadFactor)
				.putAllMethodsMapWeights(methodsMapWeights)
				.putAllMethodsMapBalance(methodsMapBalance)
				.addAllTaskListPerfromance(taskListPerformance)
				.addAllTaskListAerodynamicAndStability(taskListAerodynamicAndStability)
				.addAllTaskListDynamicStability(taskListDynamicStability)
				.putAllTaskListCosts(taskListCosts)
				.setPlotWeights(plotWeights)
				.setPlotBalance(plotBalance)
				.setPlotAerodynamicAndStability(plotAerodynamicAndStability)
				.setPlotDynamicStability(plotDynamicStability)
				.setPlotPerformance(plotPerformance)
				.setPlotCosts(plotCosts)
				.setCreateCSVWeights(createCSVWeights)
				.setCreateCSVBalance(createCSVBalance)
				.setCreateCSVAerodynamicAndStability(createCSVAerodynamicAndStability)
				.setCreateCSVDynamicStability(createCSVDynamicStability)
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
				.append("\t...............................................................................................................\n")
				.append("\tPositive limit load factor: " + _theAnalysisManagerInterface.getPositiveLimitLoadFactor() + "\n")
				.append("\tNegative limit load factor: " + _theAnalysisManagerInterface.getNegativeLimitLoadFactor() + "\n")
				.append("\t...............................................................................................................\n")
				.append("\tn Ultimate " + _nUltimate + "\n")
				.append("\tV dive (TAS): " + _vDive + "\n")
				.append("\tV dive (EAS): " + _vDiveEAS + "\n")
				.append("\tV dive: " + _vDive + "\n")
				.append("\tMach dive at zero altitude: " + _machDive0 + "\n")
				.append("\tV max cruise (TAS): " + _vMaxCruise + "\n")
				.append("\tV max cruise (EAS): " + _vMaxCruiseEAS + "\n")
				.append("\tV optimum cruise: " + _vOptimumCruise + "\n")
				.append("\tMax dynamic pressure: " + _maxDynamicPressure + "\n")
				.append("\t...............................................................................................................\n")
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
				_theAnalysisManagerInterface.getTheOperatingConditions().getAtmosphereCruise().getSpeedOfSound(), 
				SI.METERS_PER_SECOND);
		_vMaxCruiseEAS = _vMaxCruise.
				divide(Math.sqrt(_theAnalysisManagerInterface.getTheOperatingConditions().getAtmosphereCruise().getDensityRatio()));

		_vOptimumCruise = Amount.valueOf(_theAnalysisManagerInterface.getTheOperatingConditions().getMachCruise()
				*AtmosphereCalc.getSpeedOfSound(
						_theAnalysisManagerInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER),
						_theAnalysisManagerInterface.getTheOperatingConditions().getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
						), SI.METERS_PER_SECOND);
		
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
		
		if (aircraft == null) {
			System.err.println("WARNING (DO ANALYSIS) - NO AIRCRAFT HAS BEEN DEFINED. TERMINATING ...");
			System.exit(1);
		}
		
		////////////////////////////////////////////////////////////////
		// ITERATIVE LOOP 
		if(_theAnalysisManagerInterface.isIterativeLoop() == true)
			executeAnalysisIterativeLoop(aircraft, theOperatingConditions, resultsFolderPath);
		////////////////////////////////////////////////////////////////
		
		
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
			System.setOut(originalOut);
			calculateWeights(aircraft, theOperatingConditions, resultsFolderPath); 
			_executedAnalysesMap.put(AnalysisTypeEnum.WEIGHTS, true);
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
			System.setOut(originalOut);
			calculateBalance(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.BALANCE, true);
			System.out.println("\t\tBalance Analysis :: COMPLETE \n");
			System.setOut(filterStream);
		}
		////////////////////////////////////////////////////////////////
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY)) {
			
			_theAerodynamicAndStability = new HashMap<>();
			
			_theAerodynamicAndStability.put(ConditionEnum.TAKE_OFF, new ACAerodynamicAndStabilityManager_v2());
			_theAerodynamicAndStability.put(ConditionEnum.CLIMB, new ACAerodynamicAndStabilityManager_v2());
			_theAerodynamicAndStability.put(ConditionEnum.CRUISE, new ACAerodynamicAndStabilityManager_v2());
			_theAerodynamicAndStability.put(ConditionEnum.LANDING, new ACAerodynamicAndStabilityManager_v2());
			
			if(_theAnalysisManagerInterface.getTaskListAerodynamicAndStability().contains(ConditionEnum.TAKE_OFF)) {
				System.setOut(originalOut);
				System.out.println("\t\tAerodynamic and Stability Analysis (TAKE-OFF) :: START");
				System.setOut(filterStream);
				_theAerodynamicAndStability.remove(ConditionEnum.TAKE_OFF);
				_theAerodynamicAndStability.put(
						ConditionEnum.TAKE_OFF,
						ACAerodynamicAndStabilityManager_v2.importFromXML(
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
						ACAerodynamicAndStabilityManager_v2.importFromXML(
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
						ACAerodynamicAndStabilityManager_v2.importFromXML(
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
						ACAerodynamicAndStabilityManager_v2.importFromXML(
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
		if (this._theAnalysisManagerInterface.getAnalysisList().contains(AnalysisTypeEnum.DYNAMIC_STABILITY)) {
			
			_theDynamicStability = new HashMap<>();
			
			_theDynamicStability.put(ConditionEnum.TAKE_OFF, new ACDynamicStabilityManager());
			_theDynamicStability.put(ConditionEnum.CLIMB, new ACDynamicStabilityManager());
			_theDynamicStability.put(ConditionEnum.CRUISE, new ACDynamicStabilityManager());
			_theDynamicStability.put(ConditionEnum.LANDING, new ACDynamicStabilityManager());
			
			if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.TAKE_OFF)) {
				System.setOut(originalOut);
				System.out.println("\t\tDynamic Stability Analysis (TAKE-OFF) :: START");
				System.setOut(filterStream);
				_theDynamicStability.remove(ConditionEnum.TAKE_OFF);
				_theDynamicStability.put(
						ConditionEnum.TAKE_OFF,
						ACDynamicStabilityManager.importFromXML(
								_dynamicStabilityTakeOffFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.TAKE_OFF
								)
						);
			}			
			if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.CLIMB)) {
				System.setOut(originalOut);
				System.out.println("\t\tDynamic Stability Analysis (CLIMB) :: START");
				System.setOut(filterStream);
				_theDynamicStability.remove(ConditionEnum.CLIMB);
				_theDynamicStability.put(
						ConditionEnum.CLIMB,
						ACDynamicStabilityManager.importFromXML(
								_dynamicStabilityClimbFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CLIMB
								)
						);
			}			
			if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.CRUISE)) {
				System.setOut(originalOut);
				System.out.println("\t\tDynamic Stability Analysis (CRUISE) :: START");
				//System.setOut(filterStream); // TODO
				_theDynamicStability.remove(ConditionEnum.CRUISE);
				_theDynamicStability.put(
						ConditionEnum.CRUISE,
						ACDynamicStabilityManager.importFromXML(
								_dynamicStabilityCruiseFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.CRUISE
								)
						);
			}			
			if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.LANDING)) {
				System.setOut(originalOut);
				System.out.println("\t\tDynamic Stability Analysis (LANDING) :: START");
				System.setOut(filterStream);
				_theDynamicStability.remove(ConditionEnum.LANDING);
				_theDynamicStability.put(
						ConditionEnum.LANDING,
						ACDynamicStabilityManager.importFromXML(
								_dynamicStabilityLandingFileComplete.getAbsolutePath(),
								aircraft,
								theOperatingConditions,
								ConditionEnum.LANDING
								)
						);
			}			
			calculateDynamicStability(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.DYNAMIC_STABILITY, true);
			System.setOut(originalOut);
			System.out.println("\t\t\tUNCOMPLETE - UNDER DEVELOPMENT...\n");
			System.out.println("\t\tDynamic Stability Analysis :: COMPLETE\n");
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
			System.setOut(originalOut);
			calculatePerformances(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.PERFORMANCE, true);
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
			System.setOut(originalOut);
			calculateCosts(aircraft, resultsFolderPath);
			_executedAnalysesMap.put(AnalysisTypeEnum.COSTS, true);
			System.out.println("\t\tCosts Analysis :: COMPLETE \n");
			System.setOut(filterStream);
		}
				
	} // end of constructor

	/*
	 * This method execute a preliminary iterative loop until the calculated mission fuel mass is equal
	 * to the one given as input to the weights analysis.
	 */
	private Amount<Mass> executeAnalysisIterativeLoop(
			Aircraft aircraft,
			OperatingConditions operatingConditions,
			String resultsFolderPath
			) throws HDF5LibraryException, IOException {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		System.setOut(filterStream);
		//------------------------------------------------------------------
		// ANALYSIS ITERATIVE LOOP
		/* 
		 * 1) MISSION FUEL (READ OR CALCULATED)
		 * 2) WEIGHTS 
		 * 3) BALANCE
		 * 4) AERODYNAMICS AND STABILITY EVERY CONDITION (ONLY TRIMMED POLARS AND LIFT CURVES AT OPERATIVE MAX FWD CG)
		 * 5) PERFORMANCE (ONLY MISSION PROFILE) -> MISSION FUEL (FEEDBACK)
		 * 
		 */

		System.setOut(originalOut);
		Amount<Mass> currentFuelMass = Amount.valueOf(Double.MIN_VALUE, SI.KILOGRAM);
		Amount<Mass> finalFuelMass = Amount.valueOf(Double.MAX_VALUE, SI.KILOGRAM);
		int i=1; 
		
		while ( Math.abs(currentFuelMass.doubleValue(SI.KILOGRAM) - finalFuelMass.doubleValue(SI.KILOGRAM))
				/ currentFuelMass.doubleValue(SI.KILOGRAM) >= 0.005) {
			
			System.out.println("\t\tIterative Loop :: Iteration " + i + " -> " 
					+ "Fuel Mass Ratio = " 
					+ Math.abs(currentFuelMass.doubleValue(SI.KILOGRAM) - finalFuelMass.doubleValue(SI.KILOGRAM))
					/ currentFuelMass.doubleValue(SI.KILOGRAM)
			);
			
			System.setOut(filterStream);
			//..................................................................
			_theWeights = ACWeightsManager.importFromXML(
					_weightsFileComplete.getAbsolutePath(),
					aircraft,
					operatingConditions
					);
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotWeights(false).build());
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVWeights(false).build());
			calculateWeights(aircraft, operatingConditions, resultsFolderPath);
			currentFuelMass = _theWeights.getFuelMass().to(SI.KILOGRAM);
			System.setOut(originalOut);
			System.out.println("\t\t\tWeights Analysis :: DONE");
			System.setOut(filterStream);
			
			//..................................................................
			_theBalance = ACBalanceManager.importFromXML(
					_balanceFileComplete.getAbsolutePath(),
					aircraft
					);
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotBalance(false).build());
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVBalance(false).build());
			calculateBalance(aircraft, resultsFolderPath);
			System.setOut(originalOut);
			System.out.println("\t\t\tBalance Analysis :: DONE");
			System.setOut(filterStream);
		
			//..................................................................
			_theAerodynamicAndStability = new HashMap<>();
			
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotAerodynamicAndStability(false).build());
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVAerodynamicAndStability(false).build());
			
			_theAerodynamicAndStability.put(
					ConditionEnum.TAKE_OFF, 
					ACAerodynamicAndStabilityManager_v2.importFromXML(
							_aerodynamicAndStabilityTakeOffFileComplete.getAbsolutePath(),
							aircraft,
							operatingConditions,
							ConditionEnum.TAKE_OFF
							));
			_theAerodynamicAndStability.put(
					ConditionEnum.CLIMB,
					ACAerodynamicAndStabilityManager_v2.importFromXML(
							_aerodynamicAndStabilityClimbFileComplete.getAbsolutePath(),
							aircraft,
							operatingConditions,
							ConditionEnum.CLIMB
							)
					);
			_theAerodynamicAndStability.put(
					ConditionEnum.CRUISE,
					ACAerodynamicAndStabilityManager_v2.importFromXML(
							_aerodynamicAndStabilityCruiseFileComplete.getAbsolutePath(),
							aircraft,
							operatingConditions,
							ConditionEnum.CRUISE
							)
					);
			_theAerodynamicAndStability.put(
					ConditionEnum.LANDING,
					ACAerodynamicAndStabilityManager_v2.importFromXML(
							_aerodynamicAndStabilityLandingFileComplete.getAbsolutePath(),
							aircraft,
							operatingConditions,
							ConditionEnum.LANDING
							)
					);
			
			Map<AerodynamicAndStabilityEnum, MethodEnum> aircraftTaskList = new HashMap<>();
			aircraftTaskList.put(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY, MethodEnum.FROM_BALANCE_EQUATION);
			Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> aerodynamicAndStabilityTaskList = new HashMap<>();
			aerodynamicAndStabilityTaskList.put(ComponentEnum.AIRCRAFT, aircraftTaskList);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.WING, null);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.HORIZONTAL_TAIL, null);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.VERTICAL_TAIL, null);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.CANARD, null);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.FUSELAGE, null);
			aerodynamicAndStabilityTaskList.put(ComponentEnum.NACELLE, null);
			
			_theAerodynamicAndStability.get(ConditionEnum.TAKE_OFF).setTheAerodynamicBuilderInterface(
					IACAerodynamicAndStabilityManager_v2.Builder.from(
							_theAerodynamicAndStability.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface()
							)
					.clearComponentTaskList()
					.putAllComponentTaskList(aerodynamicAndStabilityTaskList)
					.build()
					);
			_theAerodynamicAndStability.get(ConditionEnum.CLIMB).setTheAerodynamicBuilderInterface(
					IACAerodynamicAndStabilityManager_v2.Builder.from(
							_theAerodynamicAndStability.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface()
							)
					.clearComponentTaskList()
					.putAllComponentTaskList(aerodynamicAndStabilityTaskList)
					.build()
					);
			_theAerodynamicAndStability.get(ConditionEnum.CRUISE).setTheAerodynamicBuilderInterface(
					IACAerodynamicAndStabilityManager_v2.Builder.from(
							_theAerodynamicAndStability.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface()
							)
					.clearComponentTaskList()
					.putAllComponentTaskList(aerodynamicAndStabilityTaskList)
					.build()
					);
			_theAerodynamicAndStability.get(ConditionEnum.LANDING).setTheAerodynamicBuilderInterface(
					IACAerodynamicAndStabilityManager_v2.Builder.from(
							_theAerodynamicAndStability.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface()
							)
					.clearComponentTaskList()
					.putAllComponentTaskList(aerodynamicAndStabilityTaskList)
					.build()
					);
			calculateAerodynamicAndStability(aircraft, resultsFolderPath);
			System.setOut(originalOut);
			System.out.println("\t\t\tLongitudinal Stability And Equilibrium Analysis :: DONE");
			System.setOut(filterStream);
			
			//..................................................................
			_thePerformance = ACPerformanceManager.importFromXML(
					_performanceFileComplete.getAbsolutePath(), 
					aircraft,
					operatingConditions
					);
			List<PerformanceEnum> performanceTaskList = new ArrayList<>();
			performanceTaskList.add(PerformanceEnum.MISSION_PROFILE);
			_thePerformance.setThePerformanceInterface(
					IACPerformanceManager.Builder.from(
							_thePerformance.getThePerformanceInterface()
							)
					.clearTaskList()
					.addAllTaskList(performanceTaskList)
					.build()
					);
			
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setPlotPerformance(false).build());
			setTheAnalysisManagerInterface(IACAnalysisManager.Builder.from(_theAnalysisManagerInterface).setCreateCSVPerformance(false).build());			
			calculatePerformances(aircraft, resultsFolderPath);
			System.setOut(originalOut);
			System.out.println("\t\t\tMission Profile Simulation :: DONE\n\n");
			System.setOut(filterStream);
			finalFuelMass = _thePerformance.getInitialFuelMassMap().get(
					_thePerformance.getThePerformanceInterface().getXcgPositionList().get(0)
					).to(SI.KILOGRAM);

		}
		
		//------------------------------------------------------------------
		
		return finalFuelMass;
		
	}
	
	public void calculateWeights(Aircraft aircraft, OperatingConditions operatingConditions, String resultsFolderPath) {

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		// Evaluate aircraft masses
		System.setOut(originalOut);
		System.out.println("\t\t\tClass II mass breakdown :: START");
		System.setOut(filterStream);
		aircraft.getTheAnalysisManager().getTheWeights().calculateAllMasses(aircraft, operatingConditions, _theAnalysisManagerInterface.getMethodsMapWeights());
		System.setOut(originalOut);
		System.out.println("\t\t\tClass II mass breakdown :: COMPLETE");
		System.setOut(filterStream);

		// Plot and print
		try {
			String weightsFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "WEIGHTS"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheWeights().toXLSFile(
					weightsFolderPath
					+ "Weights");
			if(_theAnalysisManagerInterface.isPlotWeights() == true) {
				System.setOut(originalOut);
				System.out.println("\t\t\tClass II mass breakdown plot :: START");
				System.setOut(filterStream);
				aircraft.getTheAnalysisManager().getTheWeights().plotWeightBreakdown(weightsFolderPath);
				System.setOut(originalOut);
				System.out.println("\t\t\tClass II mass breakdown plot :: COMPLETE");
				System.setOut(filterStream);
			}
			
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	public void calculateBalance(Aircraft aircraft, String resultsFolderPath) {

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		// Estimate center of gravity location
		System.setOut(originalOut);
		System.out.println("\t\t\tCenter of gravity positions and inertias estimation :: START");
		System.setOut(filterStream);
		aircraft.getTheAnalysisManager().getTheBalance().calculate(_theAnalysisManagerInterface.getMethodsMapBalance());
		System.setOut(originalOut);
		System.out.println("\t\t\tCenter of gravity positions and inertias estimation :: COMPLETE");
		System.setOut(filterStream);
		
		// Plot
		try {
			String balanceFolderPath = JPADStaticWriteUtils.createNewFolder(
					resultsFolderPath 
					+ "BALANCE"
					+ File.separator);
			aircraft.getTheAnalysisManager().getTheBalance().toXLSFile(
					balanceFolderPath
					+ "Balance");
			if(_theAnalysisManagerInterface.isPlotBalance() == Boolean.TRUE) {
				System.setOut(originalOut);
				System.out.println("\t\t\tBalance analysis plot :: START");
				System.setOut(filterStream);
				aircraft.getTheAnalysisManager().getTheBalance().createCharts(balanceFolderPath);
				System.setOut(originalOut);
				System.out.println("\t\t\tBalance analysis plot :: COMPLETE");
				System.setOut(filterStream);
			}
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

	public void calculateDynamicStability(Aircraft aircraft, String resultsFolderPath) {

		if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.TAKE_OFF)) 
			aircraft.getTheAnalysisManager().getTheDynamicStability().get(ConditionEnum.TAKE_OFF).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.CLIMB)) 
			aircraft.getTheAnalysisManager().getTheDynamicStability().get(ConditionEnum.CLIMB).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.CRUISE)) 
			aircraft.getTheAnalysisManager().getTheDynamicStability().get(ConditionEnum.CRUISE).calculate(resultsFolderPath);
		if(_theAnalysisManagerInterface.getTaskListDynamicStability().contains(ConditionEnum.LANDING)) 
			aircraft.getTheAnalysisManager().getTheDynamicStability().get(ConditionEnum.LANDING).calculate(resultsFolderPath);

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

	public Map<ComponentEnum, List<MethodEnum>> getMethodsMapWeights() {
		return _theAnalysisManagerInterface.getMethodsMapWeights();
	}

	public void setMethodsMapWeights(Map<ComponentEnum, List<MethodEnum>> _methodsMap) {
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

	public Map<ConditionEnum, ACAerodynamicAndStabilityManager_v2> getTheAerodynamicAndStability() {
		return _theAerodynamicAndStability;
	}

	public void setTheAerodynamicAndStability(Map<ConditionEnum, ACAerodynamicAndStabilityManager_v2> theAerodynamicAndStability) {
		this._theAerodynamicAndStability = theAerodynamicAndStability;
	}

	public Map<ConditionEnum, ACDynamicStabilityManager> getTheDynamicStability() {
		return _theDynamicStability;
	}

	public void setTheDynamicStability(Map<ConditionEnum, ACDynamicStabilityManager> theDynamicStability) {
		this._theDynamicStability = theDynamicStability;
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

	public List<ConditionEnum> getTaskListDynamicStability() {
		return _theAnalysisManagerInterface.getTaskListDynamicStability();
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