package writers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.azeckoski.reflectutils.ReflectUtils;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACAnalysisManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.Costs;
import aircraft.components.Aircraft;
import aircraft.components.CabinConfiguration;
import aircraft.components.FuelTank;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelle;
import aircraft.components.nacelles.NacellesManager;
import aircraft.components.powerPlant.Engine;
import aircraft.components.powerPlant.PowerPlant;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADGlobalData;
import standaloneutils.MyXLSWriteUtils;
import standaloneutils.customdata.DragPolarPoint;
import standaloneutils.customdata.MyArray;

public class JPADDataWriter {

	private OperatingConditions _theOperatingConditions;
	private Aircraft _theAircraft;
	private ACAerodynamicsManager _theAeroCalculator;

	private Element _operatingConditions,
	_fuselageInitiator,
	_wingInitiator,
	_nacelleInitiator,
	_hTailInitiator,
	_vTailInitiator,
	_powerPlantInitiator,
	_fuelTankInitiator,
	_landingGearInitiator,
	_systemsInitiator,
	_costsInitiator,
	_analysisInitiator,
	Adjust_Criterion, 
	_configurationInit,
	_weightsInit,
	_balanceInit,
	_performancesInit;

	private Element Equivalent_Wing_parameters,
	Actual_Wing_parameters,
	Equivalent_HTail_parameters,
	Actual_HTail_parameters,
	Equivalent_VTail_parameters,
	Actual_VTail_parameters;

	private Element _element;

	private	Map<String, Object> _tempMap;
	private Multimap<Object, String> _variablesMap = ArrayListMultimap.create();
	private Object _fatherObject;
	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private Element _rootElement;

	//  create a document builder using DocumentBuilderFactory class
	public DocumentBuilderFactory _factoryImport = DocumentBuilderFactory.newInstance();

	private Workbook _workbookExport;
	private CreationHelper _createHelper;
	private Sheet _sheet;
	private ACAnalysisManager _theAnalysis;
	private Element _whole_aircraft;

	private ReflectUtils _reflectUtilsInstance = ReflectUtils.getInstance();
	public String _xmlFileImport = "";
	public Document _parserDoc;
	private String _exportFileName;
	private int _columnIndexForWritingArray = 0;

	private List<MyArray> _xlsArraysList = new ArrayList<MyArray>();
	private List<String> _xlsArraysDescription = new ArrayList<String>();
	private List<String> _xlsArraysUnit = new ArrayList<String>();
	private SchemaFactory schemaFactory;
	private Schema schema;
	private Validator validator;
	private Element currentFather;


	public JPADDataWriter(
			OperatingConditions conditions, 
			Aircraft aircraft, 
			ACAnalysisManager analysis) {

		_theOperatingConditions = conditions;
		_theAircraft = aircraft;
		_theAnalysis = analysis;

		//		buildXmlTree();
		initializeDirectories();
	}


	public JPADDataWriter(
			OperatingConditions conditions, 
			Aircraft aircraft) {

		_theOperatingConditions = conditions;
		_theAircraft = aircraft;

		//		buildXmlTree();
		initializeDirectories();
	}


	public JPADDataWriter(Aircraft aircraft, ACAnalysisManager analysis) {

		_theOperatingConditions = analysis.get_theOperatingConditions();
		_theAircraft = aircraft;
		_theAnalysis = analysis;

		//		buildXmlTree();
		initializeDirectories();
	}

	public JPADDataWriter(Aircraft aircraft) {

		_theAircraft = aircraft;
		//		buildXmlTree();
		initializeDirectories();
	}

	//TODO: duplicate of createNewFolder
	private void initializeDirectories() {

		MyConfiguration.currentImagesDirectory = MyConfiguration.imagesDirectory 
				+ File.separator
				+ _theAircraft.getId()
				+ File.separator;

		// Create folders for each aircraft
		File aircraftImagesFolder = new File(MyConfiguration.currentImagesDirectory);
		try{
			if(aircraftImagesFolder.mkdir() && !aircraftImagesFolder.exists()) { 
				//				System.out.println("Directory Created");
			} else {
				//				System.out.println("Directory is not created");
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/** 
	 * Report all data to xml file
	 * 
	 * @author LA
	 * @param filenameWithPathAndExt
	 */
	public void exportToXMLfile(String filenameWithPathAndExt) {

		_exportFileName = filenameWithPathAndExt;

		docFactory = DocumentBuilderFactory.newInstance();
		//						docFactory.setNamespaceAware(true);
		//						docFactory.setValidating(true);
		//						schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		//						schema = schemaFactory.newSchema();
		//						validator = schema.newValidator();
		//						validator.
		try {
			docBuilder = docFactory.newDocumentBuilder();
			// Using Apache POI to export xls file
			_workbookExport = new HSSFWorkbook();
			//_workbookExport = new XSSFWorkbook();
			_createHelper = _workbookExport.getCreationHelper();

			defineXmlTree();
			writeAllData();
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}


	/** 
	 * Export the xml file also as xls file
	 * to enhance redability
	 * 
	 * @author LA
	 * @param EXPORT_FILE_NAME
	 */
	public void exportToXLSfile(String EXPORT_FILE_NAME) {

		// Write the output to a xls file
		FileOutputStream fileOut;

		try {
			fileOut = new FileOutputStream(EXPORT_FILE_NAME);
			_workbookExport.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Define and build the xml tree structure
	 * 
	 * @author LA
	 */
	private void defineXmlTree(){

		// root elements
		doc = docBuilder.newDocument();
		doc.createAttribute("id");
		_rootElement = doc.createElement("ADOpT");
		doc.appendChild(_rootElement);

		_operatingConditions = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theOperatingConditions));
		_rootElement.appendChild(_operatingConditions);

		//		System.out.println(_theAircraft.get_name());
		_whole_aircraft = doc.createElement("AIRCRAFT");
		_rootElement.appendChild(_whole_aircraft);

		_performancesInit = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getThePerformance()));
		_whole_aircraft.appendChild(_performancesInit);

		_costsInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getTheCosts()));
		_whole_aircraft.appendChild(_costsInitiator);

		_configurationInit = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getCabinConfiguration()));
		_whole_aircraft.appendChild(_configurationInit);

		_weightsInit = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getTheWeights())); 
		_whole_aircraft.appendChild(_weightsInit);

		_balanceInit = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getTheBalance())); 
		_whole_aircraft.appendChild(_balanceInit);

		_fuselageInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getFuselage()));
		_whole_aircraft.appendChild(_fuselageInitiator);

		_wingInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getWing()));
		_whole_aircraft.appendChild(_wingInitiator);

		_hTailInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getHTail()));
		_whole_aircraft.appendChild(_hTailInitiator);

		_vTailInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getVTail()));
		_whole_aircraft.appendChild(_vTailInitiator);

		_nacelleInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getNacelles()));
		_whole_aircraft.appendChild(_nacelleInitiator);

		_fuelTankInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getFuelTank()));
		_whole_aircraft.appendChild(_fuelTankInitiator);

		_powerPlantInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getPowerPlant()));
		_whole_aircraft.appendChild(_powerPlantInitiator);

		_systemsInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getSystems()));
		_whole_aircraft.appendChild(_systemsInitiator);

		_landingGearInitiator = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(_theAircraft.getLandingGears()));
		_whole_aircraft.appendChild(_landingGearInitiator);

		_analysisInitiator = doc.createElement("ANALYSIS");
		_rootElement.appendChild(_analysisInitiator);

		// Adjust_Criterion element
		Adjust_Criterion = doc.createElement("Adjust_Criterion");
		Adjust_Criterion.appendChild(
				doc.createTextNode(
						_theAircraft.getFuselage().get_adjustCriterion().toString()
						)
				);
		_fuselageInitiator.appendChild(Adjust_Criterion);
	}


	/** 
	 * This method executes all writing mehtods needed
	 * 
	 * @author LA
	 */
	private void writeAllData() {

		// TODO: check if analysis is null and do not call some functions
		
		MyXLSWriteUtils.setXLSstyle(_workbookExport);

		// --- Whole aircraft data ---------------------------------------

		if (_theOperatingConditions != null)
			writeOperatingConditions(_theOperatingConditions);

		ACAerodynamicsManager am = _theAircraft.getTheAerodynamics();
		
		if (_theAircraft.getTheAerodynamics() != null)
			writeAircraftAerodynamics(doc, _analysisInitiator, _theAircraft.getTheAerodynamics());

		if (_theAircraft.getTheWeights() != null)
			writeWeights(_theAircraft.getTheWeights());

		if (_theAircraft.getTheBalance() != null)
			writeBalanceOutput(_theAircraft.getTheBalance());

		if (_theAircraft.getThePerformance() != null)
			writePerformances(_theAircraft.getThePerformance());

		if (_theAircraft.getCabinConfiguration() != null)
			writeConfiguration(_theAircraft.getCabinConfiguration());

		if (_theAircraft.getTheCosts() != null)
			writeCosts(_theAircraft.getTheCosts(), _analysisInitiator);

		// --- Components --------------------------------------------------

		// Write fuselage data (analysis results included)
		if (_theAircraft.getFuselage() != null)
			writeFuselage(_theAircraft.getFuselage());

		// Write wing data
		if (_theAircraft.getWing() != null)
			writeLiftingSurface(_wingInitiator, _theAircraft.getWing());

		// Write HTail data
		if (_theAircraft.getHTail() != null)
			writeLiftingSurface(_hTailInitiator, _theAircraft.getHTail());

		// Write VTail data
		if (_theAircraft.getVTail() != null)
			writeLiftingSurface(_vTailInitiator, _theAircraft.getVTail());

		// Write Propulsion system data
		if (_theAircraft.getPowerPlant() != null)
			writePowerPlant(_theAircraft.getPowerPlant());

		// Write fuel tank data
		if (_theAircraft.getFuelTank() != null)
			writeFuelTank(_theAircraft.getFuelTank());

		// Write Nacelle data
		if (_theAircraft.getNacelles() != null)
			writeNacelles(_theAircraft.getNacelles());

		// Write Landing Gear data
		if (_theAircraft.getLandingGears() != null)
			writeLandingGear(_theAircraft.getLandingGears());

		// Write Systems data
		if (_theAircraft.getSystems() != null)
			writeSystems(_theAircraft.getSystems());

	}


	/** 
	 * This method extracts variables name from the object (e.g, _theWing)
	 * and return the corresponding Map (which supports repeated entries).
	 * TO GET VARIABLE NAMES THE METHOD MUST RECEIVE THE OBJECT IN WHICH THE
	 * VARIABLES IS DEFINED; PRIMITIVE TYPES NAME CANNOT BE EXTRACTED
	 * 
	 * @author LA
	 * @return 
	 */
	private Multimap<Object, String> initializeVariableMap() {

		_xlsArraysList.clear();
		_xlsArraysDescription.clear();
		_xlsArraysUnit.clear();

		_variablesMap.clear();
		Multimap<Object, String> variables = ArrayListMultimap.create();
		//		Field[] field = _objectToWrite.getClass().getDeclaredFields();

		// Build a map where value is the key and variableName is the entry
		for (Entry<String, Object> entry : _tempMap.entrySet()) {
			variables.put(entry.getValue(), entry.getKey());
		}

		_tempMap.clear();
		return variables;

	}


	private void writeOperatingConditions(OperatingConditions conditions) {
		_sheet = commonOperations(conditions, _operatingConditions, true);
		writeOperatingConditionsInput(conditions, _operatingConditions);
		writeOperatingConditionsOutput(conditions, _analysisInitiator);
	}

	private void writeOperatingConditionsInput(OperatingConditions conditions, Element operatingNode) {
		writeInputNode("Altitude", conditions.get_altitude(), operatingNode, true);
		writeInputNode("Mach_number", conditions.get_machCurrent(), operatingNode, true);		
	}

	private void writeOperatingConditionsOutput(OperatingConditions conditions, Element analysisNode){
		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Operating_Conditions", analysisNode);
		writeOutputNode("Static_Pressure", conditions.get_staticPressure(),  analysis);
		writeOutputNode("Dynamic_Pressure", conditions.get_dynamicPressure(),  analysis);
		writeOutputNode("Stagnation_Pressure", conditions.get_stagnationPressure(),  analysis);
		writeOutputNode("Cabin_to_outside_pressure_differential", conditions.get_maxDeltaP(),  analysis);
		writeOutputNode("Density", conditions.get_densityCurrent(),  analysis);
		writeOutputNode("Static_Temperature", conditions.get_staticTemperature(),  analysis);
		writeOutputNode("Stagnation_Temperature", conditions.get_stagnationTemperature(),  analysis);
		writeOutputNode("Dynamic_Viscosity", conditions.get_mu(),  analysis);
		writeOutputNode("Equivalent_AirSpeed", conditions.get_eas(), analysis);
		writeOutputNode("Calibrated_AirSpeed", conditions.get_cas(), analysis);
		writeOutputNode("True_AirSpeed", conditions.get_tas(), analysis);
		//		writeBlock(Operating_Conditions, "Reynolds_Number_Fuselage_lenght", _theAircraft.get_fuselage().get_reynolds());
		//		writeBlock(Operating_Conditions, "Reynolds_Number_MAC", formatter.format(BigDecimal.valueOf(conditions.calculateRe(_theAircraft.get_wing().get_meanAerodChordCk().getEstimatedValue(), _theAircraft.get_wing().get_roughness().getEstimatedValue()))));
		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
	}

	private void writeConfiguration(CabinConfiguration configuration) {
		_sheet = commonOperations(configuration, _configurationInit, true);
		writeConfigurationInput(configuration, _configurationInit);
		writeConfigurationOutput(configuration, _analysisInitiator);
	}

	private void writeConfigurationInput(CabinConfiguration configuration, Element configurationNode) {
		writeInputNode("Number_of_passengers", configuration.getNPax(), configurationNode, true);
		writeInputNode("Maximum_number_of_passengers", configuration.getMaxPax(), configurationNode, true);
		writeInputNode("Number_of_aisles", configuration.getAislesNumber(), configurationNode, true);
		writeInputNode("Number_of_classes", configuration.getClassesNumber(), configurationNode, true);
		writeInputNode("Xcoordinate_of_first_row", configuration.getXCoordinateFirstRow(), configurationNode, true);

		Element cabinLayout = doc.createElement("Cabin_Layout");
		configurationNode.appendChild(cabinLayout);

		Element economy = doc.createElement(WordUtils.capitalizeFully(ClassTypeEnum.ECONOMY.name()));
		cabinLayout.appendChild(economy);

		writeInputNode("Pitch", configuration.getPitchEconomyClass(), economy, true);
		writeInputNode("Width", configuration.getWidthEconomyClass(), economy, true);
		writeInputNode("Abreasts", configuration.getNumberOfColumnsEconomyClass(), economy, true);
		writeInputNode("Number_of_rows", configuration.getNumberOfRowsEconomyClass(), economy, true);
		writeInputNode("Distance_from_wall", configuration.getDistanceFromWallEconomyClass(), economy, true);
		writeInputNode("Number_of_breaks", configuration.getNumberOfBreaksEconomyClass(), economy, true);

		Element business = doc.createElement(WordUtils.capitalizeFully(ClassTypeEnum.BUSINESS.name()));
		cabinLayout.appendChild(business);

		writeInputNode("Pitch", configuration.getPitchBusinessClass(), business, true);
		writeInputNode("Width", configuration.getWidthBusinessClass(), business, true);
		writeInputNode("Abreasts", configuration.getNumberOfColumnsBusinessClass(), business, true);
		writeInputNode("Number_of_rows", configuration.getNumberOfRowsBusinessClass(), business, true);
		writeInputNode("Distance_from_wall", configuration.getDistanceFromWallBusinessClass(), business, true);
		writeInputNode("Number_of_breaks", configuration.getNumberOfBreaksBusinessClass(), business, true);

		Element first = doc.createElement(WordUtils.capitalizeFully(ClassTypeEnum.FIRST.name()));
		cabinLayout.appendChild(first);

		writeInputNode("Pitch", configuration.getPitchFirstClass(), first, true);
		writeInputNode("Width", configuration.getWidthFirstClass(), first, true);
		writeInputNode("Abreasts", configuration.getNumberOfColumnsFirstClass(), first, true);
		writeInputNode("Number_of_rows", configuration.getNumberOfRowsFirstClass(), first, true);
		writeInputNode("Distance_from_wall", configuration.getDistanceFromWallFirstClass(), first, true);
		writeInputNode("Number_of_breaks", configuration.getNumberOfBreaksFirstClass(), first, true);
	}

	private void writeConfigurationOutput(CabinConfiguration configuration, Element analysisNode) {
		Element configurationAnalysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Configuration_Analysis", analysisNode);
		writeOutputNode("Number_of_crew_members", configuration.getNCrew(), configurationAnalysis);
		writeOutputNode("Furnishings_and_equipment_mass", configuration.getMassEstimatedFurnishingsAndEquipment(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X", configuration.getSeatsCoG(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_front_to_rear_window_seats", configuration.getSeatsCoGFrontToRearWindow(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_front_to_rear_aisle_seats", configuration.getSeatsCoGFrontToRearAisle(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_front_to_rear_other_seats", configuration.getSeatsCoGFrontToRearOther(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_rear_to_front_window_seats", configuration.getSeatsCoGrearToFrontWindow(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_rear_to_front_aisle_seats", configuration.getSeatsCoGrearToFrontAisle(), configurationAnalysis);
		writeOutputNode("Cabin_Center_of_Gravity_X_boarding_rear_to_front_other_seats", configuration.getSeatsCoGrearToFrontOther(), configurationAnalysis);
		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
	}


	private void writeWeights(ACWeightsManager weights) {
		_sheet = commonOperations(weights, _weightsInit, true);
		writeWeightsInput(weights);
		writeWeightsOutput(_analysisInitiator);
	}

	private void writeWeightsInput(ACWeightsManager weights) {
		writeInputNode("Material_density", weights.get_materialDensity(), _weightsInit, true);

		// TODO: this should be output only data
		writeInputNode("Maximum_zero_fuel_mass", weights.get_MZFM(), _weightsInit, true);
		writeInputNode("Maximum_landing_mass", weights.get_MLM(), _weightsInit, true);
		writeInputNode("Maximum_take_off_mass", weights.get_MTOM(), _weightsInit, true);

	}

	private void writeWeightsOutput(Element analysisNode) {
		if (_theAnalysis != null &&
			_theAnalysis.get_executedAnalysesMap() != null) {

			if (_theAnalysis.get_executedAnalysesMap().get(AnalysisTypeEnum.WEIGHTS) != null) {

				if (_theAnalysis.get_executedAnalysesMap().get(AnalysisTypeEnum.WEIGHTS) == true) {

					Element weightsAnalysis = doc.createElement("Weights_Breakdown");
					analysisNode.appendChild(weightsAnalysis);

					writeOutputNode("Fuselage_mass", _theAircraft.getFuselage().get_massEstimated(), weightsAnalysis);
					writeOutputNode("Wing_mass", _theAircraft.getWing().getMassEstimated(), weightsAnalysis);
					writeOutputNode("HTail_mass", _theAircraft.getHTail().getMassEstimated(), weightsAnalysis);
					writeOutputNode("VTail_mass", _theAircraft.getVTail().getMassEstimated(), weightsAnalysis);
					writeOutputNode("Nacelles_mass", _theAircraft.getNacelles().get_totalMass(), weightsAnalysis);
					writeOutputNode("Landing_gear_mass", _theAircraft.getLandingGears().getMassEstimated(), weightsAnalysis);
					writeOutputNode("Structure_mass", _theAircraft.getTheWeights().get_structuralMass(), weightsAnalysis);
					writeOutputNode("Power_plant_mass", _theAircraft.getPowerPlant().get_totalMass(), weightsAnalysis);
					writeOutputNode("Systems_mass", _theAircraft.getSystems().getOverallMass(), weightsAnalysis);
					writeOutputNode("Furnishings_and_Equipment_mass", _theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment(), weightsAnalysis);
					writeOutputNode("Manufacturer_empty_mass", _theAircraft.getTheWeights().get_manufacturerEmptyMass(), weightsAnalysis);
					writeOutputNode("Crew_mass", _theAircraft.getTheWeights().get_crewMass(), weightsAnalysis);
					writeOutputNode("Operating_Items_mass", _theAircraft.getTheWeights().get_OIM(), weightsAnalysis);
					writeOutputNode("Operating_empty_mass", _theAircraft.getTheWeights().get_OEM(), weightsAnalysis);
					writeOutputNode("Passengers_mass", _theAircraft.getTheWeights().get_paxMass(), weightsAnalysis);
					writeOutputNode("ZeroFuelMass", _theAircraft.getTheWeights().get_ZFM(), weightsAnalysis);
				}
			}
			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		}
	}


	private void writeBalanceOutput(ACBalanceManager balance) {

		_sheet = commonOperations(balance, _balanceInit, true);

		if (_theAnalysis != null &&
				_theAnalysis.get_executedAnalysesMap() != null) {

			if (_theAnalysis.get_executedAnalysesMap().get(AnalysisTypeEnum.WEIGHTS) != null) {

				if (_theAnalysis.get_executedAnalysesMap().get(AnalysisTypeEnum.WEIGHTS) == true) {

					Element balance_Analysis = doc.createElement("Balance");
					_analysisInitiator.appendChild(balance_Analysis);

					writeOutputNode("Xcg_structure_MAC", _theAircraft.getTheBalance().get_cgStructure().get_xMAC(), balance_Analysis);
					writeOutputNode("Xcg_structure_BRF", _theAircraft.getTheBalance().get_cgStructure().get_xBRF(), balance_Analysis);
					writeOutputNode("Xcg_structure_and_engines_MAC", _theAircraft.getTheBalance().get_cgStructureAndPower().get_xMAC(), balance_Analysis);
					writeOutputNode("Xcg_structure_and_engines_BRF", _theAircraft.getTheBalance().get_cgStructureAndPower().get_xBRF(), balance_Analysis);
					writeOutputNode("Xcg_MAC_MZFM", _theAircraft.getTheBalance().get_cgMZFM().get_xMAC(), balance_Analysis);
					writeOutputNode("Xcg_BRF_MZFM", _theAircraft.getTheBalance().get_cgMZFM().get_xBRF(), balance_Analysis);
					writeOutputNode("Xcg_MTOM_MAC", _theAircraft.getTheBalance().get_cgMTOM().get_xMAC(), balance_Analysis);
					writeOutputNode("Xcg_MTOM_BRF", _theAircraft.getTheBalance().get_cgMTOM().get_xBRF(), balance_Analysis);
				}
			}

			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		}
	}

	private void writePerformances(ACPerformanceManager performances) {
		_sheet = commonOperations(performances, _performancesInit, true);
		writePerformancesInput(performances, _performancesInit);
		writePerformancesOutput(performances, _analysisInitiator);
	}

	private void writePerformancesInput(ACPerformanceManager performances, Element performancesNode) {
		writeInputNode("Optimum_Cruise_Mach_Number", performances.getMachOptimumCruise(), performancesNode, true);
		writeInputNode("Maximum_Cruise_Mach_Number", performances.getMachMaxCruise(), performancesNode, true);
		writeInputNode("Limit_load_factor", performances.getNLimit(), performancesNode, true);
		writeInputNode("Limit_load_factor_at_MZFW", performances.getNLimitZFW(), performancesNode, true);
		writeInputNode("Ultimate_load_factor", performances.getNUltimate(), performancesNode, true);		
	}

	private void writePerformancesOutput(ACPerformanceManager performances, Element analysisNode) {
		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Performances_Analysis", analysisNode);
		Element speeds = addElementToSubElement("Speeds", analysis);

		if (performances != null) {
			if (performances.getVDiveEAS() != null)
				writeOutputNode("Dive_EAS", performances.getVDiveEAS(), speeds);
			if (performances.getVMaxCruiseEAS() != null)
				writeOutputNode("Max_cruise_EAS", performances.getVMaxCruiseEAS(), speeds);
			//		writeNode("Altitude_at_absolute_minimum_speed_and_MTOW", performances.getPerformanceManager().getAltitudeAtMinimumSpeedAbsolutePercentMaxWeight(1., FlightConditionEnum.CRUISE), speeds);
			if (performances.getPerformanceManager() != null) {
				writeOutputNode("Absolute_minimum_speed_at_MTOW", performances.getPerformanceManager().getMinimumSpeedAbsolutePercentMaxWeight(1., EngineOperatingConditionEnum.CRUISE), speeds);
				//		writeNode("Altitude_at_absolute_maximum_speed_and_MTOW", performances.getPerformanceManager().getAltitudeAtMaximumSpeedAbsolutePercentMaxWeight(1., FlightConditionEnum.CRUISE), speeds);
				writeOutputNode("Absolute_maximum_speed_at_MTOW", performances.getPerformanceManager().getMaximumSpeedAbsolutePercentMaxWeight(1., EngineOperatingConditionEnum.CRUISE), speeds);

				Element pressures = addElementToSubElement("Pressures", analysis);
				writeOutputNode("Maximum_dynamic_pressure", performances.getMaxDynamicPressure(), pressures);

				Element range = addElementToSubElement("Ranges", analysis);
				writeOutputNode("Range_constant_speed_and_cl", performances.getPerformanceManager().getRangeManager().getRangeSpeedAndClConstant(), range);
				writeOutputNode("Range_constant_speed_and_altitude", performances.getPerformanceManager().getRangeManager().getRangeSpeedAndAltitudeConstant(), range);
				writeOutputNode("Range_constant_altitude_and_cl", performances.getPerformanceManager().getRangeManager().getRangeClAndAltitudeConstant(), range);

				Element ceiling = addElementToSubElement("Ceiling", analysis);
				writeOutputNode("Absolute_ceiling_at_MTOW", performances.getPerformanceManager().getAbsoluteCeilingPercentMaxWeight(1., EngineOperatingConditionEnum.CRUISE), ceiling);
				writeOutputNode("Absolute_ceiling_at_MZFW", performances.getPerformanceManager().getAbsoluteCeilingMinWeight(EngineOperatingConditionEnum.CRUISE), ceiling);
			}

			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		}
	}

	private void writeFuselage(Fuselage fuselage){

		_sheet = commonOperations(fuselage, _fuselageInitiator, true);

		////////////////////////////////////////////////////////////////////////////
		// Fuse_Parms element
		////////////////////////////////////////////////////////////////////////////
		Element fuselageParameters = doc.createElement("Fuselage_Parameters");
		_fuselageInitiator.appendChild(fuselageParameters);

		fuselageParameters.appendChild(doc.createComment(
				"Main fuselage geometric parameters "));

		// --- INPUT DATA --------------------------------------------------------------
		// Fuselage length
		writeInputNode("Length", fuselage.get_len_F(), fuselageParameters, true);
		writeInputNode("Number_of_decks", fuselage.get_deckNumber(), fuselageParameters, true);
		writeInputNode("Nose_to_fuselage_lenght_ratio", fuselage.get_lenRatio_NF(), fuselageParameters, true);
		writeInputNode("Cylindrical_part_to_fuselage_lenght_ratio", fuselage.get_lenRatio_CF(), fuselageParameters, true);
		writeInputNode("Cylindrical_part_width", fuselage.get_sectionCylinderWidth(), fuselageParameters, true);
		writeInputNode("Cylindrical_part_height", fuselage.get_sectionCylinderHeight(), fuselageParameters, true);
		writeInputNode("Nose_fineness_ratio", fuselage.get_lambda_N(), fuselageParameters, true);
		writeInputNode("Minimum_height_from_ground", fuselage.get_heightFromGround(), fuselageParameters, true);
		writeInputNode("Surface_roughess", fuselage.get_roughness(), fuselageParameters, true);
		writeInputNode("Nose_furthermost_point_height", fuselage.get_height_N(), fuselageParameters, true);
		writeInputNode("Tail_rearmost_point_height", fuselage.get_height_T(), fuselageParameters, true);
		writeInputNode("Nose_cap_lenght", fuselage.get_dxNoseCap(), fuselageParameters, true);
		writeInputNode("Tail_cap_lenght", fuselage.get_dxTailCap(), fuselageParameters, true);
		writeInputNode("Windshield_type", fuselage.get_windshieldType(), fuselageParameters, true);
		writeInputNode("Windshield_height", fuselage.get_windshieldHeight(), fuselageParameters, true);
		writeInputNode("Windshield_width", fuselage.get_windshieldWidth(), fuselageParameters, true);
		writeInputNode("Cylinder_lower_to_total_height_ratio", fuselage.get_sectionCylinderLowerToTotalHeightRatio(), fuselageParameters, true);
		writeInputNode("Cylinder_Rho_upper", fuselage.get_sectionCylinderRhoUpper(), fuselageParameters, true);
		writeInputNode("Cylinder_Rho_lower", fuselage.get_sectionCylinderRhoLower(), fuselageParameters, true);
		writeInputNode("Pressurization", fuselage.is_pressurized(), fuselageParameters, true);
		writeInputNode("Reference_mass", fuselage.getMassReference(), fuselageParameters, true);
		writeInputNode("Mass_correction_factor", fuselage.get_massCorrectionFactor(), fuselageParameters, true);

		// --- END OF INPUT DATA --------------------------------------------------------------


		// --- OUTPUT DATA --------------------------------------------------------------------

		// TODO: these parameters can be considered as input or output!!!
		writeOutputNode("Nose_Length", fuselage.get_len_N(), fuselageParameters);
		writeOutputNode("Cylindrical_Length", fuselage.get_len_C(), fuselageParameters);
		writeOutputNode("TailCone_Length", fuselage.get_len_T(), fuselageParameters);
		writeOutputNode("Nose_Length_Ratio", fuselage.get_lenRatio_NF(), fuselageParameters);
		writeOutputNode("Cylindrical_Length_Ratio", fuselage.get_lenRatio_CF(), fuselageParameters);
		writeOutputNode("TailCone_Length_Ratio", fuselage.get_lenRatio_TF(), fuselageParameters);
		writeOutputNode("Nose_Fineness_Ratio", fuselage.get_lambda_N(),  fuselageParameters);
		writeOutputNode("Cylindrical_Fineness_Ratio", fuselage.get_lambda_C(), fuselageParameters);
		writeOutputNode("TailCone_Fineness_Ratio", fuselage.get_lambda_T(), fuselageParameters);

		////////////////////////////////////////////////////////////////////////////
		// Fuselage Cross Section element
		////////////////////////////////////////////////////////////////////////////

		Element crossSections = doc.createElement("Fuse_Section_List");
		_fuselageInitiator.appendChild(crossSections);

		crossSections.appendChild(doc.createComment(
				"Fuselage Sections parameters. Edit these to alter the fuselage shape"));

		// --- INPUT DATA --------------------------------------------------------------------

		writeInputNode("Fuse_Cylinder_Section_X_Location", fuselage.get_sectionsYZStations().get(fuselage.IDX_SECTION_YZ_CYLINDER_1), crossSections, true);
		writeInputNode("Fuse_Cylinder_Section_Rho_Upper", fuselage.get_sectionCylinderRhoUpper(), crossSections, true);
		writeInputNode("Fuse_Cylinder_Section_Rho_Lower", fuselage.get_sectionCylinderRhoLower(), crossSections, true);
		writeInputNode("Fuse_Cylinder_Lower_to_total_height_ratio", fuselage.get_sectionCylinderLowerToTotalHeightRatio(), crossSections, true);

		// --- END OF INPUT DATA -------------------------------------------------------------

		// TODO: these are input parameters!
		writeOutputNode("Cylinder_Base_Area", fuselage.get_area_C(), crossSections);		
		writeOutputNode("Section_stations", fuselage.get_sectionsYZStations().get(fuselage.IDX_SECTION_YZ_NOSE_CAP), crossSections);
		writeOutputNode("Nose_Cap_Section_Width", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_NOSE_CAP).get_w_f(), crossSections);
		writeOutputNode("Nose_Cap_Section_Height", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_NOSE_CAP).get_Len_Height(), crossSections);
		writeOutputNode("Nose_Cap_Section_Rho_Upper", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_NOSE_CAP).get_RhoUpper(), crossSections);
		writeOutputNode("Nose_Cap_Section_Rho_Lower", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_NOSE_CAP).get_RhoLower(), crossSections);
		writeOutputNode("Nose_Cap_Lower_Section_a_Control_Point", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_NOSE_CAP).get_LowerToTotalHeightRatio(), crossSections);
		writeOutputNode("Mid_Nose_Section_X_Location", fuselage.get_sectionsYZStations().get(fuselage.IDX_SECTION_YZ_MID_NOSE), crossSections);
		writeOutputNode("Mid_Nose_Section_Width", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_w_f(), crossSections);
		writeOutputNode("Mid_Nose_Section_Height", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_Len_Height(), crossSections);
		writeOutputNode("Mid_Nose_Section_Rho_Upper", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_RhoUpper(), crossSections);
		writeOutputNode("Mid_Nose_Section_Rho_Lower", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_RhoLower(), crossSections);
		writeOutputNode("Mid_Nose_Lower_Section_a_Control_Point", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_LowerToTotalHeightRatio(), crossSections);
		writeOutputNode("Mid_Tail_Section_X_Location", fuselage.get_sectionsYZStations().get(fuselage.IDX_SECTION_YZ_MID_TAIL), crossSections);
		writeOutputNode("Mid_Tail_Section_Width", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_TAIL).get_w_f(), crossSections);
		writeOutputNode("Mid_Tail_Section_Height", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_TAIL).get_Len_Height(), crossSections);
		writeOutputNode("Mid_Tail_Section_Rho_Upper", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_TAIL).get_RhoUpper(), crossSections);
		writeOutputNode("Mid_Tail_Section_Rho_Lower", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_TAIL).get_RhoLower(), crossSections);
		writeOutputNode("Mid_Tail_Lower_Section_a_Control_Point", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_TAIL).get_LowerToTotalHeightRatio(), crossSections);
		writeOutputNode("Tail_Cap_Section_X_Location", fuselage.get_sectionsYZStations().get(fuselage.IDX_SECTION_YZ_TAIL_CAP), crossSections);
		writeOutputNode("Tail_Cap_Section_Width", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_TAIL_CAP).get_w_f(), crossSections);
		writeOutputNode("Tail_Cap_Section_Height", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_MID_NOSE).get_Len_Height(), crossSections);
		writeOutputNode("Tail_Cap_Section_Rho_Upper", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_TAIL_CAP).get_RhoUpper(), crossSections);
		writeOutputNode("Tail_Cap_Section_Rho_Lower", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_TAIL_CAP).get_RhoLower(), crossSections);
		writeOutputNode("Tail_Cap_Lower_Section_a_Control_Point", fuselage.get_sectionsYZ().get(fuselage.IDX_SECTION_YZ_TAIL_CAP).get_LowerToTotalHeightRatio(), crossSections);

		writeFuselageOutput(fuselage, _analysisInitiator);
	}


	private void writeFuselageOutput(Fuselage fuselage, Element analysisNode) {

		if (fuselage != null) {

			Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Fuselage_Analysis", analysisNode);

			// --- Geometry ---------------------------
			Element geometry = addElementToSubElement("Geometry", analysis);

			writeOutputNode("Overall_Equivalent_diameter_GM", fuselage.get_equivalentDiameterGM(), geometry);
			writeOutputNode("Cylinder_Equivalent_diameter_GM", fuselage.get_equivalentDiameterCylinderGM(), geometry);
			writeOutputNode("Fineness_Ratio", fuselage.get_lambda_F(), geometry);
			writeOutputNode("Wetted_surface", fuselage.get_sWet().getEstimatedValue(), geometry);
			writeOutputNode("Form_Factor", fuselage.get_formFactor(), geometry);

			// --- Weights ---------------------------
			Element weights = addElementToSubElement("Weights", analysis);

			writeOutputNode("Reference_mass", fuselage.getMassReference(), weights);
			writeOutputNode("Mass_correction_factor", fuselage.get_massCorrectionFactor(), weights);
			writeMethodsComparison(doc, _sheet, "Weight_estimation_methods_comparison", fuselage.getMassMap(), fuselage.get_percentDifference(), weights);
			writeOutputNode("Estimated_mass", fuselage.get_massEstimated(), weights);

			// --- Balance ----------------------------
			Element balance = addElementToSubElement("Balance", analysis);
			writeMethodsComparison(				
					doc, 
					_sheet,
					"Xcg_estimation_method_comparison",
					fuselage.get_xCGMap(), fuselage.get_percentDifferenceXCG(), balance);

			writeOutputNode("Xcg_LRF", fuselage.get_cg().get_xLRF(), balance);
			writeOutputNode("Ycg_LRF", fuselage.get_cg().get_yLRF(), balance);
			writeOutputNode("Zcg_LRF", fuselage.get_cg().get_zLRF(), balance);
			writeOutputNode("Xcg_BRF", fuselage.get_cg().get_xBRF(), balance);
			writeOutputNode("Ycg_BRF", fuselage.get_cg().get_yBRF(), balance);
			writeOutputNode("Zcg_BRF", fuselage.get_cg().get_zBRF(), balance);

			// --- Aerodynamics -----------------------
			Element aerodynamics = addElementToSubElement("Aerodynamics", analysis);

			if (fuselage.getAerodynamics() != null) {
				writeOutputNode("BaseDiameter", fuselage.getAerodynamics().get_equivalentDiameterBase(), aerodynamics);
				writeOutputNode("FrictionCoefficient_Cf", fuselage.getAerodynamics().get_cF(), aerodynamics);
				writeOutputNode("Cd0Parasite", fuselage.getAerodynamics().get_cD0Parasite(), aerodynamics);
				writeOutputNode("Cd0Base", fuselage.getAerodynamics().get_cD0Base(), aerodynamics);
				writeOutputNode("Cd0Upsweep", fuselage.getAerodynamics().get_cD0Upsweep(), aerodynamics);
				writeOutputNode("Cd0Windshield", fuselage.getAerodynamics().get_cDWindshield(), aerodynamics);
				writeOutputNode("Cd0", fuselage.getAerodynamics().get_cD0Total(), aerodynamics);
				writeMethodsComparison("Cm0", fuselage.getAerodynamics().getCalculateCm0().get_methodMap(), aerodynamics);
				writeMethodsComparison("CmAlpha", fuselage.getAerodynamics().getCalculateCmAlpha().get_methodMap(), aerodynamics);
				writeMethodsComparison("CmCL", fuselage.getAerodynamics().getCalculateCmCL().get_methodMap(), aerodynamics);
			}

			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		}
	}


	private void writeLiftingSurface(Element initiator, LiftingSurface liftingSurface){

		_sheet = commonOperations(liftingSurface, initiator, true);

		writeLiftingSurfaceEquivalentInput(liftingSurface, initiator);
		writeLiftingSurfaceActualInput(liftingSurface, initiator);
		writeLiftingSurfaceOutput(liftingSurface, _analysisInitiator);

	}

	private void writeLiftingSurfaceEquivalentInput(LiftingSurface liftingSurface, Element initiator) {
		Element equivalent_parameters = doc.createElement("Equivalent_lifting_surface_parameters");
		initiator.appendChild(equivalent_parameters);

		// --- INPUT DATA-------------------------------------
		writeInputNode("Xcoordinate", liftingSurface.getX0(), equivalent_parameters, true);
		writeInputNode("Ycoordinate", liftingSurface.getY0(), equivalent_parameters, true);
		writeInputNode("Zcoordinate", liftingSurface.getZ0(), equivalent_parameters, true);

		writeInputNode("Planform_surface", liftingSurface.get_surface(), equivalent_parameters, true);
		writeInputNode("Control_surface_extension", liftingSurface.get_surfaceCS(), equivalent_parameters, true);
		writeInputNode("aspectRatio", liftingSurface.get_aspectRatio(), equivalent_parameters, true);
		writeInputNode("taperRatio", liftingSurface.get_taperRatioEquivalent(), equivalent_parameters, true);
		writeInputNode("Wing_position_in_percent_of_fuselage_height", liftingSurface.get_positionRelativeToAttachment(), equivalent_parameters, true);
		writeInputNode("kinkSpanStation", liftingSurface.get_spanStationKink(), equivalent_parameters, true);
		writeInputNode("Thickness_to_chord_ratio_root", liftingSurface.get_tc_root(), equivalent_parameters, true);
		writeInputNode("Thickness_to_chord_ratio_kink", liftingSurface.get_tc_kink(), equivalent_parameters, true);
		writeInputNode("Thickness_to_chord_ratio_tip", liftingSurface.get_tc_tip(), equivalent_parameters, true);
		writeInputNode("Root_chord_LE_extension", liftingSurface.get_extensionLERootChordLinPanel(), equivalent_parameters, true);
		writeInputNode("Root_chord_TE_extension", liftingSurface.get_extensionTERootChordLinPanel(), equivalent_parameters, true);
		writeInputNode("sweepc4", liftingSurface.get_sweepQuarterChordEq(), equivalent_parameters, true);
		writeInputNode("Incidence_relative_to_xBRF", liftingSurface.get_iw(), equivalent_parameters, true);
		writeInputNode("kinkStationTwist", liftingSurface.get_twistKink(), equivalent_parameters, true);
		writeInputNode("tipStationTwist", liftingSurface.get_twistTip(), equivalent_parameters, true);
		writeInputNode("Dihedral_inner_panel", liftingSurface.get_dihedralInnerPanel(), equivalent_parameters, true);
		writeInputNode("Dihedral_outer_panel", liftingSurface.get_dihedralOuterPanel(), equivalent_parameters, true);
		writeInputNode("Surface_roughness", liftingSurface.get_roughness(), equivalent_parameters, true);
		writeInputNode("Transition_point_in_percent_of_chord_upper_wing", liftingSurface.get_xTransitionU(), equivalent_parameters, true);
		writeInputNode("Transition_point_in_percent_of_chord_upper_wing", liftingSurface.get_xTransitionL(), equivalent_parameters, true);
		writeInputNode("Reference_mass", liftingSurface.getReferenceMass(), equivalent_parameters, true);
		writeInputNode("Composite_correction_factor", liftingSurface.get_compositeCorretionFactor(), equivalent_parameters, true);
		writeInputNode("Mass_correction_factor", liftingSurface.get_massCorrectionFactor(), equivalent_parameters, true);

		// ---OUTPUT DATA-------------------------------------
		writeOutputNode("sweepLE", liftingSurface.get_sweepLEEquivalent(), equivalent_parameters);
		writeOutputNode("Mean_aerodynamic_chord_MAC", liftingSurface.get_meanAerodChordEq(), equivalent_parameters);
		writeOutputNode("Root_chord", liftingSurface.get_chordRootEquivalentWing(), equivalent_parameters);
	}

	private void writeLiftingSurfaceActualInput(LiftingSurface liftingSurface, Element initiator) {

		Element actual_parameters = doc.createElement("Actual_lifting_surface_parameters");
		initiator.appendChild(actual_parameters);

		//		writeNode("Mach_number_transonic_threshold", liftingSurface.get_machTransonicThreshold(), actual_parameters, true);
		writeOutputNode("Planform_surface", liftingSurface.get_surfaceCranked(), actual_parameters);
		writeOutputNode("Exposed_surface", liftingSurface.get_surfaceExposed(), actual_parameters);
		writeOutputNode("Wetted_surface", liftingSurface.get_surfaceWetted(), actual_parameters);
		writeOutputNode("Exposed_Wetted_surface", liftingSurface.get_surfaceWettedExposed(), actual_parameters);
		writeOutputNode("span", liftingSurface.get_span(), actual_parameters);
		writeOutputNode("taperRatio", liftingSurface.get_taperRatioActual(), actual_parameters);
		writeOutputNode("semiSurfaceInnerPanel", liftingSurface.get_semiSurfaceInnerPanel(), actual_parameters);
		writeOutputNode("aspectRatioInnerPanel", liftingSurface.get_aspectRatioInnerPanel(), actual_parameters);
		writeOutputNode("semiSurfaceOuterPanel", liftingSurface.get_semiSurfaceOuterPanel(), actual_parameters);
		writeOutputNode("aspectRatioOuterPanel", liftingSurface.get_aspectRatioOuterPanel(), actual_parameters);
		writeOutputNode("taperRatioInnerPanel", liftingSurface.get_taperRatioInnerPanel(), actual_parameters);
		writeOutputNode("taperRatioOuterPanel", liftingSurface.get_taperRatioOuterPanel(), actual_parameters);
		writeOutputNode("sweepLEInnerPanel", liftingSurface.get_sweepLEInnerPanel(), actual_parameters);
		writeOutputNode("sweepc4InnerPanel", liftingSurface.get_sweepQuarterChordInnerPanel(), actual_parameters);
		writeOutputNode("sweepLEOuterPanel", liftingSurface.get_sweepLEOuterPanel(), actual_parameters);
		writeOutputNode("sweepc4OuterPanel", liftingSurface.get_sweepQuarterChordOuterPanel(), actual_parameters);
		writeOutputNode("rootChord", liftingSurface.get_chordRoot(), actual_parameters);
		writeOutputNode("rootChordXle", liftingSurface.get_xLERoot(), actual_parameters);
		writeOutputNode("kinkChord", liftingSurface.get_chordKink(), actual_parameters);
		writeOutputNode("kinkChordXle", liftingSurface.get_xLEKink(), actual_parameters);
		writeOutputNode("tipChord", liftingSurface.get_chordTip(), actual_parameters);
		writeOutputNode("tipChordXle", liftingSurface.get_xLETip(), actual_parameters);
		writeOutputNode("Mean_aerodynamic_chord_MAC", liftingSurface.get_meanAerodChordActual(), actual_parameters);
		writeOutputNode("Mean_aerodynamic_chord_xLE_LRF", liftingSurface.get_xLEMacActualLRF(), actual_parameters);
		writeOutputNode("Mean_aerodynamic_chord_yLE_LRF", liftingSurface.get_yLEMacActualLRF(), actual_parameters);
		writeOutputNode("Mean_aerodynamic_chord_xLE_BRF", liftingSurface.get_xLEMacActualBRF(), actual_parameters);
		writeOutputNode("Mean_aerodynamic_chord_yLE_BRF", liftingSurface.get_yLEMacActualBRF(), actual_parameters);
		writeOutputNode("Mean_dihedral_angle", liftingSurface.get_dihedralMean(), actual_parameters);
		writeOutputNode("AC_to_CG_distance", liftingSurface.get_AC_CGdistance(), actual_parameters);
		writeOutputNode("AC_to_Wing_AC_distance", liftingSurface.get_ACw_ACdistance(), actual_parameters);
		writeOutputNode("Volumetric_ratio", liftingSurface.get_volumetricRatio(), actual_parameters);

		if (liftingSurface.getGeometry().getCalculateThickness() != null) {
			writeMethodsComparison(
					"Mean_maximum_thickness", 
					liftingSurface.getGeometry().getCalculateThickness().getMethodsMap(),
					actual_parameters);
		}
		
		writeInputNode("Number_of_airfoils", liftingSurface.get_numberOfAirfoils(), actual_parameters, true);

		for (int k=0; k < liftingSurface.get_theAirfoilsList().size(); k++){
			writeAirfoil(liftingSurface.get_theAirfoilsList().get(k), liftingSurface, actual_parameters);
		}
	}

	/**
	 * Write lifting surface output parameters
	 * 
	 * @author LA
	 * @param liftingSurface
	 * @param analysisNode
	 */
	private void writeLiftingSurfaceOutput(LiftingSurface liftingSurface, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, WordUtils.capitalizeFully(liftingSurface.getType().name()) + "_Analysis", analysisNode);

		// --- Weights -----------------------

		Element weights = addElementToSubElement("Weights", analysis);

		writeOutputNode("Reference_mass", liftingSurface.getReferenceMass(), weights);
		writeOutputNode("Composite_correction_factor", liftingSurface.get_compositeCorretionFactor(), weights);
		writeOutputNode("Mass_correction_factor", liftingSurface.get_massCorrectionFactor(), weights);

		writeMethodsComparison(
				doc, _sheet,
				"Weight_estimation_method_comparison",
				liftingSurface.getMassMap(), liftingSurface.getPercentDifference(), weights);

		writeOutputNode("Estimated_mass", liftingSurface.getMassEstimated(), weights);

		// --- Balance ----------------------------

		Element balance = addElementToSubElement("Balance", analysis);

		writeMethodsComparison(				
				doc, _sheet,
				"Xcg_estimation_method_comparison",
				liftingSurface.getXCGMap(), liftingSurface.getPercentDifferenceXCG(), balance);

		writeMethodsComparison(
				doc, _sheet,
				"Ycg_estimation_method_comparison",
				liftingSurface.getYCGMap(), liftingSurface.getPercentDifferenceYCG(), balance);

		writeOutputNode("Xcg_LRF", liftingSurface.getCg().get_xLRF(), balance);
		writeOutputNode("Ycg_LRF_half_wing", liftingSurface.getCg().get_yLRF(), balance);
		writeOutputNode("Zcg_LRF", liftingSurface.getCg().get_zLRF(), balance);
		writeOutputNode("Xcg_BRF", liftingSurface.getCg().get_xBRF(), balance);
		writeOutputNode("Ycg_BRF_half_wing", liftingSurface.getCg().get_yBRF(), balance);
		writeOutputNode("Zcg_BRF", liftingSurface.getCg().get_zBRF(), balance);

		// --- Aerodynamics -------------------------

		Element aerodynamics = addElementToSubElement("Aerodynamics", analysis);

		if (liftingSurface.getAerodynamics() != null) {
			writeMethodsComparison("Aerodynamic_center_x_coordinate_MRF", 
					liftingSurface.getAerodynamics().getCalculateXAC().get_methodMapMRF(), 
					aerodynamics);
			writeMethodsComparison("Aerodynamic_center_x_coordinate_LRF", 
					liftingSurface.getAerodynamics().getCalculateXAC().get_methodMapLRF(), 
					aerodynamics);
			writeMethodsComparison("Critical_Mach_number", 
					liftingSurface.getAerodynamics().getCalculateMachCr().getMethodsMap(), 
					aerodynamics);

			if (liftingSurface.getType() == ComponentEnum.WING){

				writeOutputNode("FrictionCoefficient_Cf", liftingSurface.getAerodynamics().get_cF(), aerodynamics);
				writeOutputNode("CompressibilityFactor", liftingSurface.getAerodynamics().get_compressibilityFactor(), aerodynamics);
				writeOutputNode("FormFactor", liftingSurface.get_formFactor(), aerodynamics);
				writeOutputNode("Cd0Parasite", liftingSurface.getAerodynamics().get_cD0Parasite(), aerodynamics);
				writeOutputNode("CdWingFuselageInterference", liftingSurface.getAerodynamics().get_cdWFInterf(), aerodynamics);
				writeOutputNode("CdWingNacelleInterference", liftingSurface.getAerodynamics().get_cdWNInterf(), aerodynamics);
				writeOutputNode("CdGaps", liftingSurface.getAerodynamics().get_cdGap(), aerodynamics);
				writeMethodsComparison(				
						"CdWaveAtCurrentCL", 
						liftingSurface.getAerodynamics().getCalculateCdWaveDrag().getMethodsMap(),
						aerodynamics);
				writeOutputNode("Cd0", liftingSurface.getAerodynamics().get_cD0Total(), aerodynamics);
			}

			else if (liftingSurface.getType() == ComponentEnum.HORIZONTAL_TAIL){

				writeOutputNode("CG-ACtail_distance", liftingSurface.get_AC_CGdistance(), aerodynamics);
				writeOutputNode("ACwing-ACtail_distance", liftingSurface.get_ACw_ACdistance(), aerodynamics);
				writeOutputNode("VolumetricRatio_Vh", liftingSurface.get_volumetricRatio(), aerodynamics);
				writeOutputNode("FrictionCoefficient_Cf", liftingSurface.getAerodynamics().get_cF(), aerodynamics);
				writeOutputNode("CompressibilityFactor", liftingSurface.getAerodynamics().get_compressibilityFactor(), aerodynamics);
				writeOutputNode("FormFactor", liftingSurface.get_formFactor(), aerodynamics);
				writeOutputNode("Cd0Parasite", liftingSurface.getAerodynamics().get_cD0Parasite(), aerodynamics);
				writeOutputNode("CdGaps", liftingSurface.getAerodynamics().get_cdGap(), aerodynamics);
				writeMethodsComparison(				
						"CdWaveAtCurrentCL", 
						liftingSurface.getAerodynamics().getCalculateCdWaveDrag().getMethodsMap(),
						aerodynamics);
				writeOutputNode("Cd0", liftingSurface.getAerodynamics().get_cD0Total(), aerodynamics);
			}

			else if (liftingSurface.getType() == ComponentEnum.VERTICAL_TAIL){

				writeOutputNode("CG-ACtail_distance", liftingSurface.get_AC_CGdistance(), aerodynamics);
				writeOutputNode("ACwing-ACtail_distance", liftingSurface.get_ACw_ACdistance(), aerodynamics);
				writeOutputNode("VolumetricRatio_Vt", liftingSurface.get_volumetricRatio(), aerodynamics);
				writeOutputNode("FrictionCoefficient_Cf", liftingSurface.getAerodynamics().get_cF(), aerodynamics);
				writeOutputNode("CompressibilityFactor", liftingSurface.getAerodynamics().get_compressibilityFactor(), aerodynamics);
				writeOutputNode("FormFactor", liftingSurface.get_formFactor(), aerodynamics);
				writeOutputNode("Cd0Parasite", liftingSurface.getAerodynamics().get_cD0Parasite(), aerodynamics);
				writeOutputNode("CdGaps", liftingSurface.getAerodynamics().get_cdGap(), aerodynamics);
				writeMethodsComparison(				
						"CdWaveAtCurrentCL", 
						liftingSurface.getAerodynamics().getCalculateCdWaveDrag().getMethodsMap(),
						aerodynamics);
				writeOutputNode("Cd0", liftingSurface.getAerodynamics().get_cD0Total(), aerodynamics);

			} else {

				writeOutputNode("CG-ACtail_distance", liftingSurface.get_AC_CGdistance(), aerodynamics);
				writeOutputNode("ACwing-ACtail_distance", liftingSurface.get_ACw_ACdistance(), aerodynamics);
				writeOutputNode("VolumetricRatio_Vt", liftingSurface.get_volumetricRatio(), aerodynamics);
				writeOutputNode("FrictionCoefficient_Cf", liftingSurface.getAerodynamics().get_cF(), aerodynamics);
				writeOutputNode("CompressibilityFactor", liftingSurface.getAerodynamics().get_compressibilityFactor(), aerodynamics);
				writeOutputNode("FormFactor", liftingSurface.get_formFactor(), aerodynamics);
				writeOutputNode("Cd0Parasite", liftingSurface.getAerodynamics().get_cD0Parasite(), aerodynamics);
				writeOutputNode("CdGaps", liftingSurface.getAerodynamics().get_cdGap(), aerodynamics);
				writeMethodsComparison(				
						"CdWaveAtCurrentCL", 
						liftingSurface.getAerodynamics().getCalculateCdWaveDrag().getMethodsMap(),
						aerodynamics);
				writeOutputNode("Cd0", liftingSurface.getAerodynamics().get_cD0Total(), aerodynamics);
			}

			writeOutputNode("Current_lift_coefficient", liftingSurface.getAerodynamics().get_cLCurrent(), aerodynamics);

			writeMethodsComparison(				
					"Alpha_zero_lift", 
					liftingSurface.getAerodynamics().getCalculateAlpha0L().getMethodsMap(),
					aerodynamics);
			writeMethodsComparison(				
					"CL_alpha", 
					liftingSurface.getAerodynamics().getCalculateCLAlpha().getMethodsMap(),
					aerodynamics);
			writeMethodsComparison(				
					"Maximum_lift_coefficient",
					liftingSurface.getAerodynamics().getcLMap().getcXMaxMap(),
					aerodynamics);
			writeOutputNode("CL_basic_along_span", liftingSurface.get_clBasic_y().toArray(), aerodynamics);

			writeMethodsComparison(				
					"CMac", 
					liftingSurface.getAerodynamics().getCalculateCmAC().getMethodsMap(),
					aerodynamics);
			writeMethodsComparison(				
					"CMalpha", 
					liftingSurface.getAerodynamics().getCalculateCmAlpha().getMethodsMap(),
					aerodynamics);

			writeOutputNode("Alpha", liftingSurface.getAerodynamics().getAlphaArray().to(NonSI.DEGREE_ANGLE), aerodynamics);
			writeMethodsComparison(				
					"CLvsAlpha_curve", 
					liftingSurface.getAerodynamics().getCalculateCLvsAlphaCurve().get_cLMap(),
					aerodynamics);

			writeOutputNode("Non_dimensional_stations", liftingSurface.get_eta(), aerodynamics);
			writeOutputNode("Elliptical_load_distribution", liftingSurface.get_ellChordVsY().toArray(), aerodynamics);

			writeTable(				
					"Actual_load_distribution", "Alpha", "Load",
					liftingSurface.getAerodynamics().getcLMap().getCcxyVsAlphaTable(),
					aerodynamics);

			writeTable(				
					"Lift_coefficient_distribution", "Alpha", "Cl",
					liftingSurface.getAerodynamics().getcLMap().getCxyVsAlphaTable(),
					aerodynamics);

			writeTable(				
					"Total_lift_coefficient", "Alpha", "CL",
					liftingSurface.getAerodynamics().getcLMap().getcXVsAlphaAsArrayTable(),
					aerodynamics);

			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		} // getAerodynamics() != null

	}

	/** 
	 * Write airfoil parameters
	 * 
	 * @param airfoil
	 * @param liftingSurface
	 * @param parentInitiator
	 */
	private void writeAirfoil(
			Airfoil airfoil, 
			LiftingSurface liftingSurface, 
			Element parentInitiator) {

		Element airfoilParam = addElementToSubElement(JPADGlobalData.getTheXmlTree().getDescription(airfoil), parentInitiator);
		airfoilParam.setAttribute("level", JPADGlobalData.getTheXmlTree().getLevel(airfoil).toString());
		commonOperations(airfoil, airfoilParam);

		writeInputNode("Family", airfoil.get_family(), airfoilParam, true);
		writeInputNode("Type", airfoil.getType(), airfoilParam, true);


		Element geometry = addElementToSubElement(JPADGlobalData.getTheXmlTree().getDescription(airfoil.getGeometry()), airfoilParam);
		commonOperations(airfoil.getGeometry(), geometry);
		geometry.setAttribute("level", JPADGlobalData.getTheXmlTree().getLevel(airfoil.getGeometry()).toString());

		writeOutputNode("Position_along_semispan", airfoil.getGeometry().get_yStation(), geometry);
		writeInputNode("Twist_relative_to_root", airfoil.getGeometry().get_twist(), geometry, true);
		writeInputNode("Thickness_to_chord_ratio_max", airfoil.getGeometry().get_maximumThicknessOverChord(), geometry, true);
		writeInputNode("Xcoordinate", airfoil.getGeometry().get_xCoords(), geometry, true);
		//		writeNode("Ycoordinate", geometry, airfoil.getGeometry().get_yCoords(), true);
		writeInputNode("Zcoordinate", airfoil.getGeometry().get_zCoords(), geometry, true);


		Element aerodynamics = addElementToSubElement(JPADGlobalData.getTheXmlTree().getDescription(airfoil.getAerodynamics()), airfoilParam);
		commonOperations(airfoil.getAerodynamics(), aerodynamics);
		aerodynamics.setAttribute("level", JPADGlobalData.getTheXmlTree().getLevel(airfoil.getAerodynamics()).toString());

		writeInputNode("Alpha_zero_lift", airfoil.getAerodynamics().get_alphaZeroLift(), aerodynamics, true);
		writeInputNode("Alpha_end_linear", airfoil.getAerodynamics().get_alphaStar(), aerodynamics, true);
		writeInputNode("Alpha_stall", airfoil.getAerodynamics().get_alphaStall(), aerodynamics, true);
		writeInputNode("Cl_alpha", airfoil.getAerodynamics().get_clAlpha(), aerodynamics, true);
		writeInputNode("Cd_min", airfoil.getAerodynamics().get_cdMin(), aerodynamics, true);
		writeInputNode("Cl_at_Cdmin", airfoil.getAerodynamics().get_clAtCdMin(), aerodynamics, true);
		writeInputNode("Cl_end_linear", airfoil.getAerodynamics().get_clStar(), aerodynamics, true);
		writeInputNode("Cl_max", airfoil.getAerodynamics().get_clMax(), aerodynamics, true);
		writeInputNode("K_factor_drag", airfoil.getAerodynamics().get_kFactorDragPolar(), aerodynamics, true);
		writeInputNode("Cm_alpha", airfoil.getAerodynamics().get_cmAlphaAC(), aerodynamics, true);
		writeInputNode("Xac", airfoil.getAerodynamics().get_aerodynamicCenterX(), aerodynamics, true);
		writeInputNode("CmAC", airfoil.getAerodynamics().get_cmAC(), aerodynamics, true);
		writeInputNode("CmAC_at_stall", airfoil.getAerodynamics().get_cmACStall(), aerodynamics, true);

	}

	private void writeFuelTank(FuelTank fuelTank) {

		_sheet = commonOperations(fuelTank, _fuelTankInitiator, true);

		Element fuelTankParam = doc.createElement("Fuel_tank_parameters");
		_fuelTankInitiator.appendChild(fuelTankParam);

		writeFuelTankInput(fuelTank, fuelTankParam);
		writeFuelTankOutput(fuelTank, fuelTankParam, _analysisInitiator);
	}

	private void writeFuelTankInput(FuelTank fuelTank, Element fuelTankParam) {
		writeInputNode("Xcoordinate", fuelTank.getX0(), fuelTankParam, true);
		writeInputNode("Ycoordinate", fuelTank.getY0(), fuelTankParam, true);
		writeInputNode("Zcoordinate", fuelTank.getZ0(), fuelTankParam, true);
		writeInputNode("Fuel_density", fuelTank.getFuelDensity(), fuelTankParam, true);
		writeInputNode("Fuel_volume", fuelTank.getFuelVolume(), fuelTankParam, true);
		writeInputNode("Fuel_mass", fuelTank.getFuelMass(), fuelTankParam, true);
	}

	private void writeFuelTankOutput(FuelTank fuelTank, Element fuelTankParam, Element analysisNode) {

		writeOutputNode("LE_spanwise_extension", fuelTank.getA1(), fuelTankParam);
		writeOutputNode("TE_spanwise_extension", fuelTank.getA2(), fuelTankParam);
		writeOutputNode("Chordwise_mean_extension", fuelTank.getLength(), fuelTankParam);
		writeOutputNode("Root_height", fuelTank.getB1(), fuelTankParam);
		writeOutputNode("Tip_height", fuelTank.getB2(), fuelTankParam);
		writeOutputNode("LE_surface", fuelTank.getS1(), fuelTankParam);
		writeOutputNode("TE_surface", fuelTank.getS2(), fuelTankParam);
		writeOutputNode("Volume", fuelTank.getVolumeEstimated(), fuelTankParam);

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Fuel_tank_Analysis", analysisNode);

		// --- Weights -----------------------
		Element weights = addElementToSubElement("Weights", analysis);

		// --- Balance ----------------------------
		Element balance = addElementToSubElement("Balance", analysis);

		writeMethodsComparison(				
				doc, 
				_sheet,
				"Xcg_estimation_method_comparison",
				fuelTank.getXCGMap(), fuelTank.getPercentDifferenceXCG(), balance);

		writeOutputNode("Xcg_LRF", fuelTank.getCenterOfGravity().get_xLRF(), balance);
		writeOutputNode("Ycg_LRF", fuelTank.getCenterOfGravity().get_yLRF(), balance);
		writeOutputNode("Zcg_LRF", fuelTank.getCenterOfGravity().get_zLRF(), balance);
		writeOutputNode("Xcg_BRF", fuelTank.getCenterOfGravity().get_xBRF(), balance);
		writeOutputNode("Ycg_BRF", fuelTank.getCenterOfGravity().get_yBRF(), balance);
		writeOutputNode("Zcg_BRF", fuelTank.getCenterOfGravity().get_zBRF(), balance);

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);	
	}

	private void writePowerPlant(PowerPlant powerPlant) {

		_sheet = commonOperations(powerPlant, _powerPlantInitiator, true);

		Element powerPlantParameters = doc.createElement("Power_plant_parameters");
		_powerPlantInitiator.appendChild(powerPlantParameters);

		Element analysisNode = doc.createElement("Power_plant_analysis");
		_analysisInitiator.appendChild(analysisNode);

		writePowerPlantInput(powerPlant, powerPlantParameters);
		writePowerPlantOutput(powerPlant, analysisNode);

	}

	private void writePowerPlantInput(PowerPlant powerPlant, Element powerPlantParameters) {
		writeInputNode("Xcoordinate", powerPlant.get_X0(), powerPlantParameters, true);
		writeInputNode("Ycoordinate", powerPlant.get_Y0(), powerPlantParameters, true);
		writeInputNode("Zcoordinate", powerPlant.get_Z0(), powerPlantParameters, true);
		writeInputNode("Number_of_engines", powerPlant.get_engineNumber(), powerPlantParameters, true);
		writeInputNode("Engines_equal", powerPlant.is_engineEqual(),  powerPlantParameters, true);
		writeOutputNode("Maximum_total_power_output", powerPlant.get_P0Total(),  powerPlantParameters);
		writeOutputNode("Maximum_total_thrust", powerPlant.get_T0Total(), powerPlantParameters);
		writeInputNode("Total_Reference_mass", powerPlant.get_dryMassPublicDomain(), powerPlantParameters, true);

		int nEngines = 1;
		if (powerPlant.is_engineEqual() == false) nEngines = powerPlant.get_engineList().size();

		for (int k=0; k < nEngines; k++){
			writeEngineInput(powerPlant.get_engineList().get(k), _powerPlantInitiator);
		}
	}

	private void writePowerPlantOutput(PowerPlant powerPlant, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Power_plant_Analysis", analysisNode);

		// --- Weights -----------------------
		Element weights = addElementToSubElement("Weights", analysis);

		writeOutputNode("Total_Reference_mass", powerPlant.get_dryMassPublicDomain(), weights);
		writeOutputNode("Total_Estimated_mass", powerPlant.get_totalMass(), weights);
		writeOutputNode("Percent_difference_from_total_reference_mass", powerPlant.get_percentTotalDifference(), weights);

		// --- Balance --------------------------------------
		Element balance = addElementToSubElement("Balance", analysis);

		writeMethodsComparison(				
				doc, 
				_sheet,
				"Xcg_estimation_method_comparison",
				powerPlant.get_xCGMap(), powerPlant.get_percentDifferenceXCG(), balance);

		writeOutputNode("Xcg_LRF", powerPlant.get_cg().get_xLRF(), balance);
		writeOutputNode("Ycg_LRF", powerPlant.get_cg().get_yLRF(), balance);
		writeOutputNode("Zcg_LRF", powerPlant.get_cg().get_zLRF(), balance);
		writeOutputNode("Xcg_BRF", powerPlant.get_cg().get_xBRF(), balance);
		writeOutputNode("Ycg_BRF", powerPlant.get_cg().get_yBRF(), balance);
		writeOutputNode("Zcg_BRF", powerPlant.get_cg().get_zBRF(), balance);

		int size = 1;
		if (powerPlant.is_engineEqual() == false) size = powerPlant.get_engineList().size();

		for (int k=0; k < size; k++){
			writeEngineOutput(powerPlant.get_engineList().get(k), analysisNode);
		}

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
	}


	/**
	 * Write engine input parameters
	 * 
	 * @author LA
	 * @param engine
	 * @param powerPlantNode
	 */
	private void writeEngineInput(Engine engine, Element powerPlantNode) {

		Element engineParameters = JPADStaticWriteUtils.addElementToSubElement(
				doc, _sheet, 
				JPADGlobalData.getTheXmlTree().getDescription(engine), powerPlantNode);
		engineParameters.setAttribute("level", JPADGlobalData.getTheXmlTree().getLevel(engine).toString());
		commonOperations(engine, engineParameters);

		writeInputNode("Type", engine.get_engineType(), engineParameters, true);
		writeInputNode("Xcoordinate", engine.get_X0(), engineParameters, true);
		writeInputNode("Ycoordinate", engine.get_Y0(), engineParameters, true);
		writeInputNode("Zcoordinate", engine.get_Z0(), engineParameters, true);
		writeInputNode("Mounting_point", engine.get_mountingPoint(), engineParameters, true);
		writeInputNode("Maximum_thrust", engine.get_t0(), engineParameters, true);
		writeInputNode("Maximum_power_output", engine.get_p0(),  engineParameters, true);
		writeInputNode("BPR", engine.get_bpr(),  engineParameters, true);
		writeInputNode("Dry_engine_mass_from_public_domain", engine.get_dryMassPublicDomain(), engineParameters, true);	
	}

	/**
	 * Write engine output parameters
	 * 
	 * @author LA
	 * @param engine
	 * @param analysisNode
	 */
	private void writeEngineOutput(Engine engine, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Engine_Analysis", analysisNode);

		// --- Weights -----------------------
		Element weights = addElementToSubElement("Weights", analysis);

		writeMethodsComparison(				
				doc, _sheet,
				"Mass_estimation_method_comparison",
				engine.get_massMap(), engine.get_percentDifference(), weights);

		writeOutputNode("Dry_engine_mass", engine.get_dryMass(), weights);		
	}


	private void writeNacelles(NacellesManager nacelles) {

		_sheet = commonOperations(nacelles, _nacelleInitiator, true);
		Element analysisNode = doc.createElement("Nacelles_analysis");
		_analysisInitiator.appendChild(analysisNode);

		for (int i=0; i < nacelles.get_nacellesNumber(); i++) {
			writeNacelleInput(nacelles.get_nacellesList().get(i), _nacelleInitiator);
			writeNacelleOutput(nacelles.get_nacellesList().get(i), analysisNode);
		}
	}

	/** 
	 * Write nacelle input parameters
	 * 
	 * @author LA
	 * @param nacelle
	 * @param parentInitiator
	 */
	private void writeNacelleInput(Nacelle nacelle, Element parentInitiator) {

		Element nacelleParameters = doc.createElement(JPADGlobalData.getTheXmlTree().getDescription(nacelle));
		parentInitiator.appendChild(nacelleParameters);
		commonOperations(nacelle, nacelleParameters);

		writeInputNode("Xcoordinate", nacelle.get_X0(), nacelleParameters, true);
		writeInputNode("Ycoordinate", nacelle.get_Y0(), nacelleParameters, true);
		writeInputNode("Zcoordinate", nacelle.get_Z0(), nacelleParameters, true);
		writeInputNode("Lenght", nacelle.get_length(), nacelleParameters, true);
		writeInputNode("Mean_diameter", nacelle.get_diameterMean(), nacelleParameters, true);
		writeInputNode("Inlet_diameter", nacelle.get_diameterInlet(), nacelleParameters, true);
		writeInputNode("Outlet_diameter", nacelle.get_diameterOutlet(), nacelleParameters, true);
		writeInputNode("Reference_mass", nacelle.get_massReference(), nacelleParameters, true);
		writeOutputNode("Wetted_surface", nacelle.get_surfaceWetted(), nacelleParameters);
		writeOutputNode("FormFactor", nacelle.formFactor(), nacelleParameters);
	}

	/**
	 * Write nacelle output parameters
	 * 
	 * @author LA
	 * @param nacelle
	 * @param analysisNode
	 */
	private void writeNacelleOutput(Nacelle nacelle, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Nacelle_Analysis", analysisNode);

		// --- Weights -------------------------
		Element weights = addElementToSubElement("Weights", analysis);

		writeOutputNode("Reference_mass", nacelle.get_massReference(), weights);
		writeOutputNode("All_nacelles_mass", nacelle.get_totalMass(), weights);
//		writeMethodsComparison(doc, _sheet, "Weight_estimation_methods_comparison", nacelle.getWeights().get_massMap(), nacelle.get_percentDifference(), weights);
		writeMethodsComparison(doc, _sheet, "Weight_estimation_methods_comparison", nacelle.getWeights().get_massMap(), nacelle.getWeights().get_percentDifference(), weights);
		writeOutputNode("Estimated_mass", nacelle.get_massEstimated(), weights);

		// --- Balance -------------------------
		Element balance = addElementToSubElement("Balance", analysis);

		writeOutputNode("Xcg_LRF", nacelle.getCG().get_xLRF(), balance);
		writeOutputNode("Ycg_LRF", nacelle.getCG().get_yLRF(), balance);
		writeOutputNode("Zcg_LRF", nacelle.getCG().get_zLRF(), balance);
		writeOutputNode("Xcg_BRF", nacelle.getCG().get_xBRF(), balance);
		writeOutputNode("Ycg_BRF", nacelle.getCG().get_yBRF(), balance);
		writeOutputNode("Zcg_BRF", nacelle.getCG().get_zBRF(), balance);

		// --- Aerodynamics --------------------
		Element aerodynamics = addElementToSubElement("Aerodynamics", analysis);

		writeOutputNode("FrictionCoefficient_Cf", nacelle.getAerodynamics().get_cF(), aerodynamics);
		writeOutputNode("Cd0Parasite", nacelle.getAerodynamics().get_cd0Parasite(), aerodynamics);
		writeOutputNode("Cd0Base", nacelle.getAerodynamics().get_cd0Base(), aerodynamics);
		writeOutputNode("Cd0Nacelle", nacelle.getAerodynamics().get_cd0Total(), aerodynamics);

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);		
	}


	private void writeLandingGear(LandingGears landingGear) {
		_sheet = commonOperations(landingGear, _landingGearInitiator, true);
		writeLandingGearInput(landingGear, _landingGearInitiator);
		writeLandingGearOutput(landingGear, _analysisInitiator);
	}

	/** 
	 * Write landing gear input parameters
	 * 
	 * @author LA
	 * @param landingGear
	 * @param father
	 */
	private void writeLandingGearInput(LandingGears landingGear, Element father) {
		Element Landing_gear_parameters = doc.createElement("Landing_gear_parameters");
		father.appendChild(Landing_gear_parameters);

		writeInputNode("Xcoordinate", landingGear.getX0(), Landing_gear_parameters, true);
		writeInputNode("Ycoordinate", landingGear.getY0(), Landing_gear_parameters, true);
		writeInputNode("Zcoordinate", landingGear.getZ0(), Landing_gear_parameters, true);
		writeInputNode("Mounting_point", landingGear.get_mounting().name(), Landing_gear_parameters, true);
		writeInputNode("Lenght", landingGear.getLenght(), Landing_gear_parameters, true);
		writeInputNode("Reference_mass", landingGear.getReferenceMass(), Landing_gear_parameters, true);
	}

	/**
	 * Write landing gear output parameters
	 * 
	 * @author LA
	 * @param landingGear
	 * @param analysisNode
	 */
	private void writeLandingGearOutput(LandingGears landingGear, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Landing_gear_Analysis", analysisNode);

		// --- Weights -------------------------
		Element weights = addElementToSubElement("Weights", analysis);

		writeOutputNode("Reference_mass", landingGear.getReferenceMass(), weights);
		writeOutputNode("Mass", landingGear.getMass(), weights);

		writeMethodsComparison(
				doc, 
				_sheet, 
				"Weight_estimation_method_comparison",
				landingGear.getMassMap(), landingGear.getPercentDifference(), weights);

		writeOutputNode("Estimated_mass", landingGear.getMassEstimated(), weights);

		// --- Balance --------------------------------
		Element balance = addElementToSubElement("Balance", analysis);

		writeOutputNode("Xcg_LRF", landingGear.getCg().get_xLRF(), balance);
		writeOutputNode("Ycg_LRF", landingGear.getCg().get_yLRF(), balance);
		writeOutputNode("Zcg_LRF", landingGear.getCg().get_zLRF(), balance);
		writeOutputNode("Xcg_BRF", landingGear.getCg().get_xBRF(), balance);
		writeOutputNode("Ycg_BRF", landingGear.getCg().get_yBRF(), balance);
		writeOutputNode("Zcg_BRF", landingGear.getCg().get_zBRF(), balance);

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);

	}

	private void writeSystems(Systems systems) {

		_sheet = commonOperations(systems, _systemsInitiator, true);

		////////////////////////////////////////////////////////////////////////////
		// Systems Data
		////////////////////////////////////////////////////////////////////////////
		Element Systems = doc.createElement("Systems_data");
		_systemsInitiator.appendChild(Systems);

		writeInputNode("Reference_mass", systems.getReferenceMass(), Systems, true);

		////////////////////////////////////////////////////////////////////////////
		// Systems analysis results
		////////////////////////////////////////////////////////////////////////////

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Systems_Analysis", _analysisInitiator);

		// --- Weights -------------------------
		Element weights = addElementToSubElement("Weights", analysis);

		writeOutputNode("Reference_mass", systems.getReferenceMass(), weights);
		writeOutputNode("Overall_mass", systems.getOverallMass(), weights);

		int i=0;
		for (Entry<MethodEnum, Amount<Mass>> entry : systems.getMassMap().entrySet())
		{
			// Wing Mass estimation methods
			writeOutputNode("Mass_estimation_method", entry.getKey().toString(), weights);

			// Wing Mass
			writeOutputNode("Mass", entry.getValue(), weights);

			// Percent difference from reference value
			writeOutputNode("Percent_difference", systems.getPercentDifference()[i], weights);

			i++;
		}

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);

	}

	/**
	 * Write aerodynamics parameters associated with the entire aircraft
	 * 
	 * @author LA
	 * @param doc TODO
	 * @param father TODO
	 * @param aeroCalc
	 */
	private void writeAircraftAerodynamics(Document doc, Element father, ACAerodynamicsManager aeroCalc) {

		if (aeroCalc != null) {

			Element wholeConfigurationAerodynamics = doc.createElement("Whole_configuration_aerodynamics");
			father.appendChild(wholeConfigurationAerodynamics);
			_sheet = JPADStaticWriteUtils.commonOperations(_theAircraft.getId(), _workbookExport, "Whole_configuration_aerodynamics", true);

			Element dragPolar = addElementToSubElement("Drag_Polar", wholeConfigurationAerodynamics);
			if (aeroCalc.get_eWhole() != null)
				writeOutputNode("OswaldFactor", aeroCalc.get_eWhole()[0], dragPolar);
			if (aeroCalc.get_kExcr() != null)
				writeOutputNode("K_Excrescences", aeroCalc.get_kExcr(), dragPolar);
			if (aeroCalc.get_cDTotalCurrent() != null)
				writeOutputNode("CDTotalCurrent", aeroCalc.get_cDTotalCurrent(), dragPolar);
			if (aeroCalc.get_cDRough() != null)
				writeOutputNode("CDRoughness", aeroCalc.get_cDRough(), dragPolar);
			if (aeroCalc.get_cDCool() != null)
				writeOutputNode("CDCoolings", aeroCalc.get_cDCool(), dragPolar);
			
			if (aeroCalc.get_cD0Map() != null)
				writeMethodsComparison("CD0", aeroCalc.get_cD0Map(), dragPolar);
			//			writeNode("CD0Total", aeroCalc.get_cD()[0], dragPolar);
			
			if (aeroCalc.get_cDWaveList() != null)
				writeOutputNode("CDWave", aeroCalc.get_cDWaveList().toArray(), dragPolar);			
			if (aeroCalc.getMaxEfficiencyPoint() != null)
				writeDragPolarPoint("Maximum_efficiency_point", aeroCalc.getMaxEfficiencyPoint(), dragPolar);			
			if (aeroCalc.getMinPowerPoint() != null)
				writeDragPolarPoint("Minimum_power_required_point", aeroCalc.getMinPowerPoint(), dragPolar);
			if (aeroCalc.getMaxRangePoint() != null)
				writeDragPolarPoint("Maximum_range_point", aeroCalc.getMaxRangePoint(), dragPolar);
			if (aeroCalc.get_cD() != null)
				writeOutputNode("CD", aeroCalc.get_cD(), dragPolar);
			if (aeroCalc.get_cL() != null)
				writeOutputNode("CL", aeroCalc.get_cL(), dragPolar);

			Element staticStability = addElementToSubElement("Static_stability", wholeConfigurationAerodynamics);
			if (aeroCalc.getDepsdalpha() != null)
				writeOutputNode("dEpsilonDAlpha", aeroCalc.getDepsdalpha(), staticStability);
			if (aeroCalc.get_cLAlphaFixed() != null)
				writeOutputNode("CLAlphaFixed", aeroCalc.get_cLAlphaFixed(), staticStability);
			if (aeroCalc.get_cMAlphaFixed() != null)
				writeOutputNode("CMAlphaFixed", aeroCalc.get_cMAlphaFixed(), staticStability);
			if (aeroCalc.get_cMCLFixed() != null)
				writeOutputNode("CMCLFixed", aeroCalc.get_cMCLFixed(), staticStability);
			if (aeroCalc.get_neutralPointXCoordinateMRF() != null)
				writeOutputNode("Neutral_point_xMRF", aeroCalc.get_neutralPointXCoordinateMRF(), staticStability);

			JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
		}
	}

	/**
	 * 
	 * @author LA
	 *
	 * @param costs
	 * @param analysisNode
	 */
	private void writeCosts(Costs costs, Element analysisNode) {
		_sheet = commonOperations(costs, _costsInitiator, true);
		writeCostsInput(costs, _costsInitiator);
		writeCostsOutput(costs, analysisNode);
	}


	/**
	 * 
	 * @author LA
	 *
	 * @param costs
	 * @param costsInitiator
	 */
	private void writeCostsInput(Costs costs, Element costsInitiator) {

		writeInputNode("Airframe_cost", costs.get_airframeCost(), costsInitiator, true);
		writeInputNode("Total_investments", costs.get_totalInvestments(), costsInitiator, true);

		writeInputNode("Single_Engine_cost", costs.get_singleEngineCost(), costsInitiator, true);
		writeInputNode("Engine_maintenance_cost", costs.get_engineMaintLaborCost(), costsInitiator, true);
		writeInputNode("Fuel_volumetric_cost", costs.get_fuelVolumetricCost(), costsInitiator, true);
		writeInputNode("Hour_volumetric_fuel_consumption", costs.get_hourVolumetricFuelConsumption(), costsInitiator, true);
		writeInputNode("Oil_mass_cost", costs.get_oilMassCost(), costsInitiator, true);
		writeInputNode("Spares_cost", costs.get_sparesAirframePerCosts(), costsInitiator, true);

		writeInputNode("Airframe_Maintenance_Labor_Cost", costs.get_airframeMaintLaborCost(), costsInitiator, true);
		writeInputNode("Airframe_Maintenance_Material_Cost", costs.get_airframeMaintMaterialCost(), costsInitiator, true);
		writeInputNode("Residual_value", costs.get_residualValue(), costsInitiator, true);
		writeInputNode("Annual_insurance_premium_rate", costs.get_annualInsurancePremiumRate(), costsInitiator, true);
		writeInputNode("Annual_interest_rate", costs.get_annualInterestRate(), costsInitiator, true);
		writeInputNode("Ground_Handling_cost_per_passengers", costs.get_groundHandlingCostXPax(), costsInitiator, true);
		writeInputNode("Navigational_charges", costs.get_jenkinsonNavigationalCharges(), costsInitiator, true);
		writeInputNode("Landing_fees_per_ton", costs.get_landingFeesPerTon(), costsInitiator, true);
		writeInputNode("Man_hour_labor_rate", costs.get_manHourLaborRate(), costsInitiator, true);
		writeInputNode("Single_cabin_crew_cost_per_hour", costs.get_singleCabinCrewHrCost(), costsInitiator, true);
		writeInputNode("Single_flight_crew_cost_per_hour", costs.get_singleflightCrewHrCost(), costsInitiator, true);
		writeInputNode("Utilization", costs.get_utilization(), costsInitiator, true);
	}

	/**
	 * 
	 * @author LA
	 *
	 * @param costs
	 * @param analysisNode
	 */
	private void writeCostsOutput(Costs costs, Element analysisNode) {

		Element analysis = JPADStaticWriteUtils.addSubElement(doc, _sheet, "Costs_Analysis", analysisNode);

		Element fixedCharges = addElementToSubElement("Fixed_charges", analysis);
		writeMethodsComparison("Depreciation", costs.get_theFixedCharges().get_calcDepreciation().get_methodsMap(), fixedCharges);
		writeMethodsComparison("Interests", costs.get_theFixedCharges().get_calcInterest().get_methodsMap(), fixedCharges);
		writeMethodsComparison("Insurance", costs.get_theFixedCharges().get_calcInsurance().get_methodsMap(), fixedCharges);
		writeMethodsComparison("Crew", costs.get_theFixedCharges().get_calcCrewCosts().get_methodsMap(), fixedCharges);

		Element tripCharges = addElementToSubElement("Trip_charges", analysis);
		writeMethodsComparison("Landing_fees", costs.get_theTripCharges().get_calcLandingFees().get_methodsMap(), tripCharges);
		writeMethodsComparison("Navigational_charges", costs.get_theTripCharges().get_calcNavigationalCharges().get_methodsMap(), tripCharges);
		writeMethodsComparison("Ground_handling_charges", costs.get_theTripCharges().get_calcGroundHandlingCharges().get_methodsMap(), tripCharges);
		writeMethodsComparison("Maintenance_costs", costs.get_theTripCharges().get_calcMaintenanceCosts().get_methodsMap(), tripCharges);
		writeMethodsComparison("Fuel_and_oil_costs", costs.get_theTripCharges().get_calcFuelAndOilCharges().get_methodsMap(), tripCharges);

		JPADStaticWriteUtils.writeAllArraysToXls(_sheet, _xlsArraysDescription, _xlsArraysList, _xlsArraysUnit);
	}

	/**
	 * Write a drag polar point represented
	 * by its efficiency, CD, CL, drag, speed and power 
	 * 
	 * @author LA
	 * @param title
	 * @param dragPoint
	 * @param father
	 */
	private void writeDragPolarPoint(String title, DragPolarPoint dragPoint, Element father) {
		Element innerElement = doc.createElement(title);
		father.appendChild(innerElement);

		// --- The xls file must be treated separately -------------------------------------------
		Row row = _sheet.createRow(_sheet.getLastRowNum() + 1);

		row.createCell(0).setCellValue(_createHelper.createRichTextString(title.replace("_", " ")));
		row.getCell(0).setCellStyle(MyXLSWriteUtils.styleFirstColumn);
		// ---------------------------------------------------------------------------------------

		writeOutputNode(MyConfiguration.tabAsSpaces + "Efficiency", dragPoint.getEfficiency(), innerElement);
		writeOutputNode(MyConfiguration.tabAsSpaces + "CD", dragPoint.getcD(), innerElement);
		writeOutputNode(MyConfiguration.tabAsSpaces + "CL", dragPoint.getcL(), innerElement);
		writeOutputNode(MyConfiguration.tabAsSpaces + "Drag", dragPoint.getDrag(), innerElement);
		writeOutputNode(MyConfiguration.tabAsSpaces + "Speed", dragPoint.getSpeed(), innerElement);
		writeOutputNode(MyConfiguration.tabAsSpaces + "Power", dragPoint.getPower(), innerElement);
	}

	private void writeInputNode(String description, Object valueToWrite, Element father, boolean input) {
		JPADStaticWriteUtils.writeNode(
				description, valueToWrite,
				father, _fatherObject, doc, 
				_sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, 
				_reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, input);
	}

	private void writeOutputNode(String description, Object valueToWrite, Element father) {
		JPADStaticWriteUtils.writeNode(
				description, valueToWrite,
				father, _fatherObject, doc, 
				_sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, 
				_reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
	}

	/**
	 * 
	 * @param elementName
	 * @param father
	 * @return
	 */
	private Element addElementToSubElement(String elementName, Element father) {
		return JPADStaticWriteUtils.addElementToSubElement(doc, _sheet, elementName, father);
	}

	private Sheet commonOperations(Object obj, Element el, boolean createSheet) {
		el.setAttribute("id", JPADGlobalData.getTheXmlTree().getIdAsString(obj));
		el.setIdAttribute("id", true);

		_tempMap = _reflectUtilsInstance.getObjectValues(obj);
		_variablesMap = initializeVariableMap();
		_fatherObject = obj;

		return JPADStaticWriteUtils.commonOperations(_theAircraft.getId(), _workbookExport, JPADGlobalData.getTheXmlTree().getDescription(obj), createSheet);
	}

	private void commonOperations(Object obj, Element el) {
		commonOperations(obj, el, false);
	}

	/** 
	 * Utility function to write all estimation methods chosen
	 * 
	 * @author LA
	 * @param doc TODO
	 * @param _sheet TODO
	 * @param percentDifference
	 * @param entrySet
	 * @param innerElement
	 */
	private void writeMethodsComparison( 
			Document doc, 
			Sheet _sheet, 
			String title,
			Map mapToWrite, Double[] percentDifference, Element fatherElement) {

		Set<Entry<MethodEnum, Amount<?>>> entrySet = (Set<Entry<MethodEnum, Amount<?>>>) mapToWrite.entrySet();
		Element innerElement = doc.createElement(title);
		fatherElement.appendChild(innerElement);

		// --- The xls file must be treated separately -------------------------------------------
		Row row = _sheet.createRow(_sheet.getLastRowNum() + 1);

		row.createCell(0).setCellValue(title.replace("_", " "));
		row.getCell(0).setCellStyle(MyXLSWriteUtils.styleFirstColumn);
		// ---------------------------------------------------------------------------------------

		int i=0;
		for (Entry<MethodEnum, Amount<?>> entry : entrySet) {

			JPADStaticWriteUtils.writeNode(WordUtils.capitalizeFully(MyConfiguration.tabAsSpaces + entry.getKey().toString()), entry.getValue(), innerElement, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);

			// Percent difference from reference value
			JPADStaticWriteUtils.writeNode(MyConfiguration.tabAsSpaces + "Percent_error", percentDifference[i], innerElement, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);

			i++;
		}

	}

	private void writeMethodsComparison(String title, 
			Map map,
			Element parentInitiator) {

		Element innerElement = doc.createElement(title);
		parentInitiator.appendChild(innerElement);

		// --- The xls file must be treated separately -------------------------------------------
		Row row = _sheet.createRow(_sheet.getLastRowNum() + 1);

		row.createCell(0).setCellValue(title.replace("_", " "));
		row.getCell(0).setCellStyle(MyXLSWriteUtils.styleFirstColumn);

		// ---------------------------------------------------------------------------------------

		if (map.values().size() != 0) {
			if ((Iterables.get(map.values(), 0) instanceof Amount)) {
				Set<Entry<Enum, Amount<?>>> entrySet = (Set<Entry<Enum, Amount<?>>>) map.entrySet();
				for (Entry<Enum, Amount<?>> entry : entrySet) {
					JPADStaticWriteUtils.writeNode(
							WordUtils.capitalizeFully(MyConfiguration.tabAsSpaces + entry.getKey().toString()), 
							entry.getValue(), innerElement, _fatherObject, doc, _sheet, 
							_variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, 
							_reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
				}
			} else {
				Set<Entry<Enum, Double>> entrySet = (Set<Entry<Enum, Double>>) map.entrySet();
				for (Entry<Enum, Double> entry : entrySet) {
					JPADStaticWriteUtils.writeNode(
							WordUtils.capitalizeFully(MyConfiguration.tabAsSpaces + entry.getKey().toString()), 
							entry.getValue(), innerElement, _fatherObject, doc, _sheet, 
							_variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, 
							_reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
				}
			}
		}
	}

	private void writeTable(
			String title, 
			String xVariableName,
			String yVariableName,
			Object entries,
			Element parentInitiator) {

		Element innerElement = doc.createElement(title);
		parentInitiator.appendChild(innerElement);
		Table<Object, Object, Object> map;

		// --- The xls file must be treated separately -------------------------------------------
		Row row = _sheet.createRow(_sheet.getLastRowNum() + 1);

		row.createCell(0).setCellValue(_createHelper.createRichTextString(title.replace("_", " ")));
		row.getCell(0).setCellStyle(MyXLSWriteUtils.styleDefault);
		// ---------------------------------------------------------------------------------------

		if (entries instanceof TreeBasedTable) {
			map = (TreeBasedTable<Object, Object, Object>) entries;
		} else {
			map = (HashBasedTable<Object, Object, Object>) entries;
		}

		// Loop over methods
		for (Entry<Object, Map<Object, Object>> m : map.rowMap().entrySet()) {

			Map<Object, Object> innerMap = m.getValue();
			Element inner2Element = doc.createElement(WordUtils.capitalizeFully(m.getKey().toString()));
			innerElement.appendChild(inner2Element);

			// Loop over alphas
			for (Entry<Object, Object > mm : innerMap.entrySet()){
				if (mm.getKey() instanceof Amount) {
					JPADStaticWriteUtils.writeNode(xVariableName, mm.getKey(), inner2Element, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
					JPADStaticWriteUtils.writeNode(yVariableName + "_at_" + xVariableName + "_" + ((Amount)mm.getKey()).getEstimatedValue(), mm.getValue(), inner2Element, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);

				} else if (mm.getKey() instanceof MyArray) {
					JPADStaticWriteUtils.writeNode(xVariableName, mm.getKey(), inner2Element, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
					JPADStaticWriteUtils.writeNode(yVariableName, mm.getValue(), inner2Element, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);	

				} else {
					JPADStaticWriteUtils.writeNode(WordUtils.capitalizeFully(mm.getKey().toString()), mm.getValue(), inner2Element, _fatherObject, doc, _sheet, _variablesMap, _xlsArraysList, _xlsArraysDescription, _xlsArraysUnit, _reflectUtilsInstance, MyConfiguration.notInitializedWarning, true, false);
				}
			}
		}
	}


} // end of class
