package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.costs.CostsCalcUtils;
import configuration.MyConfiguration;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.CostsPlotEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyUnits;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;

public class ACCostsManager {
	
	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW ACCostsManager (WORK IN PROGRESS)
	 * 
	 * @author Vincenzo Cusati & Vittorio Trifari
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	public IACCostsManager _theCostsBuilderInterface;
	
	//..............................................................................
	// DERIVED INPUT	
	private Amount<Money> _totalInvestment;
	private Amount<Money> _airframeCost;
	private Amount<?> _landingChargeConstant;
	private Amount<?> _navigationChargeConstant;
	private Amount<?> _groundHandlingChargeConstant;
	private Map<MethodEnum, Amount<?>> _cockpitCrewCost;
	private Map<MethodEnum, Amount<?>> _cabinCrewCost;
	private Map<MethodEnum, Amount<?>> _labourAirframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _materialAirframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _labourEngineMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _materialEngineMaintenanceCharges;
	private Amount<Mass> _airframeMass;
	private Amount<Duration> _blockTime;
	
	//..............................................................................
	// OUTPUT
	private Map<MethodEnum, Amount<?>> _depreciation;
	private Map<MethodEnum, Amount<?>> _interest;
	private Map<MethodEnum, Amount<?>> _insurance;
	private Map<MethodEnum, Amount<?>> _capitalDOC;
	
	private Map<MethodEnum, Amount<?>> _crewDOC;
	
	private Map<MethodEnum, Amount<?>> _fuelDOC;
	
	private Amount<?> _landingCharges;
	private Amount<?> _navigationCharges;
	private Amount<?> _groundHandlingCharges;
	private Amount<?> _emissionsCharges;
	private Map<MethodEnum, Amount<?>> _chargesDOC;
	
	private Map<MethodEnum, Amount<?>> _airframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _engineMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _maintenanceChargesDOC;
	
	// TODO: all derived data are maps
	// Only six items of DOC (Capital, etc)

	
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "resource" })
	public static IACCostsManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Map<CostsEnum, MethodEnum> taskList
			) throws IOException {
		
		//---------------------------------------------------------------
		// PRELIMINARY OPERATIONS
		//---------------------------------------------------------------
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading costs analysis data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		Boolean readWeightFromXLSFlag;
		Boolean readPerformanceFromXLSFlag;
		
		String readWeightFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_xls_file");
		
		if(readWeightFromXLSString.equalsIgnoreCase("true"))
			readWeightFromXLSFlag = Boolean.TRUE;
		else
			readWeightFromXLSFlag = Boolean.FALSE;
		
		String readPerformanceFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@performance_from_xls_file");
		
		if(readPerformanceFromXLSString.equalsIgnoreCase("true"))
			readPerformanceFromXLSFlag = Boolean.TRUE;
		else
			readPerformanceFromXLSFlag = Boolean.FALSE;

		String fileWeightsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_weights");
		
		String filePerformanceXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_performance");		
		
		//---------------------------------------------------------------
		// INITIALIZING WEIGHTS DATA
		//---------------------------------------------------------------
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> payloadMass = null;

		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		if(readWeightFromXLSFlag == Boolean.TRUE) {

			File weightsFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "WEIGHTS"
					+ File.separator
					+ fileWeightsXLS);
			if(weightsFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(weightsFile);
				Workbook workbook;
				if (weightsFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (weightsFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//---------------------------------------------------------------
				// MAXIMUM TAKE-OFF MASS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell maximumTakeOffMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Take-Off Mass").get(0)).getCell(2);
					if(maximumTakeOffMassCell != null)
						maximumTakeOffMass = Amount.valueOf(maximumTakeOffMassCell.getNumericCellValue(), SI.KILOGRAM);
					//---------------------------------------------------------------
					// OPERATING EMPTY MASS
					Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(2);
					if(operatingEmptyMassCell != null)
						operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

					//---------------------------------------------------------------
					// PAYLOAD MASS
					Cell payloadMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Passengers Mass").get(0)).getCell(2);
					if(payloadMassCell != null)
						payloadMass = Amount.valueOf(payloadMassCell.getNumericCellValue(), SI.KILOGRAM);
				}
			}
			else {
				System.err.println("FILE '" + weightsFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
			}
		}
		else {
		
			//---------------------------------------------------------------
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_take_off_mass");
			
			//---------------------------------------------------------------
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/operating_empty_mass");
			
			//---------------------------------------------------------------
			// PASSENGERS TOTAL MASS
			String payloadMassProperty = reader.getXMLPropertyByPath("//weights/payload");
			if(payloadMassProperty != null)
				payloadMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/payload");
		}
		
		//---------------------------------------------------------------
		// INITIALIZING PERFORMANCE DATA
		//---------------------------------------------------------------
		Amount<Length> range= null;
		Amount<Mass> blockFuel = null;
		Amount<Duration> flightTime = null;

		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		if(readPerformanceFromXLSFlag == Boolean.TRUE) {

			File performanceFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "PERFORMANCE"
					+ File.separator
					+ filePerformanceXLS);
			if(performanceFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(performanceFile);
				Workbook workbook;
				if (performanceFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (performanceFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//---------------------------------------------------------------
				// RANGE
				Sheet sheetMissionProfileData = MyXLSUtils.findSheet(workbook, "MISSION PROFILE");
				if(sheetMissionProfileData != null) {
					Cell rangeCell = sheetMissionProfileData.getRow(MyXLSUtils.findRowIndex(sheetMissionProfileData, "Total mission distance").get(0)).getCell(2);
					if(rangeCell != null)
						range = Amount.valueOf(rangeCell.getNumericCellValue(), NonSI.NAUTICAL_MILE);
					//---------------------------------------------------------------
					// BLOCK FUEL
					Cell blockFuelCell = sheetMissionProfileData.getRow(MyXLSUtils.findRowIndex(sheetMissionProfileData, "Total fuel used").get(0)).getCell(2);
					if(blockFuelCell != null)
						blockFuel = Amount.valueOf(blockFuelCell.getNumericCellValue(), SI.KILOGRAM);

					//---------------------------------------------------------------
					// FLIGHT TIME
					Cell flightTimeCell = sheetMissionProfileData.getRow(MyXLSUtils.findRowIndex(sheetMissionProfileData, "Total mission duration").get(0)).getCell(2);
					if(flightTimeCell != null)
						flightTime = Amount.valueOf(flightTimeCell.getNumericCellValue(), NonSI.MINUTE);
				}
			}
			else {
				System.err.println("FILE '" + performanceFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
			}
		}
		else {
		
			//---------------------------------------------------------------
			// RANGE
			String rangeProperty = reader.getXMLPropertyByPath("//performance/range");
			if(rangeProperty != null)
				range = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//performance/range");
			
			//---------------------------------------------------------------
			// BLOCK FUEL
			String blockFuelProperty = reader.getXMLPropertyByPath("//performance/block_fuel");
			if(blockFuelProperty != null)
				blockFuel = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/block_fuel");
			
			//---------------------------------------------------------------
			// FLIGHT TIME
			String flightTimeProperty = reader.getXMLPropertyByPath("//performance/flight_time");
			if(flightTimeProperty != null)
				flightTime = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/flight_time");

		}
		
		//---------------------------------------------------------------
		// INITIALIZE COSTS DATA
		//---------------------------------------------------------------
		Amount<?> utilization = null;
		Amount<Duration> lifeSpan = Amount.valueOf(16, NonSI.YEAR);
		Double residualValue = 0.1; 
		Amount<Money> aircraftPrice = null;
		Double airframeRelativeSparesCosts = 0.1;
		Double engineRelativeSparesCosts = 0.3;
		Double interestRate = 0.054;
		Double insuranceRate = 0.005;
		Amount<?> cockpitLabourRate = Amount.valueOf(360, MyUnits.USD_PER_HOUR);
		Amount<?> cabinLabourRate = Amount.valueOf(90, MyUnits.USD_PER_HOUR);
		Amount<?> fuelUnitPrice = Amount.valueOf(59.2, MyUnits.USD_PER_BARREL);
		Amount<?> landingCharges = null;
		Amount<?> navigationCharges = null;
		Amount<?> groundHandlingCharges = null;
		Amount<?> noiseCharges = null;
		Amount<?> emissionChargesNOx = null;
		Amount<?> emissionChargesCO = null;
		Amount<?> emissionChargesCO2 = null;
		Amount<?> emissionChargesHC = null;
		Amount<?> airframeLabourRate = Amount.valueOf(40, MyUnits.USD_PER_HOUR);
		Amount<?> engineLabourRate = Amount.valueOf(40, MyUnits.USD_PER_HOUR);
		Amount<Money> enginePrice = null;
		
		//---------------------------------------------------------------
		// UTILIZATION
		String calculateUtilizationString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/utilization/@calculate");
		
		if(calculateUtilizationString.equalsIgnoreCase("TRUE")){
			
			String calculateUtilizationMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//global_data/utilization/@method");
			
			if(calculateUtilizationMethodString != null) {
				
				if(calculateUtilizationMethodString.equalsIgnoreCase("AEA")) 
					utilization = CostsCalcUtils.calcUtilizationAEA(
									CostsCalcUtils.calcBlockTime(flightTime, range)
									);
				
				if(calculateUtilizationMethodString.equalsIgnoreCase("SFORZA")) 
					utilization = CostsCalcUtils.calcUtilizationSforza(
									CostsCalcUtils.calcBlockTime(flightTime, range)
									);
			}
		}
		else {
			String utilizationProperty = reader.getXMLPropertyByPath("//global_data/utilization");
			if(utilizationProperty != null)
				utilization = (Amount<?>) reader.getXMLAmountWithUnitByPath("//global_data/utilization");
		}
		
		//---------------------------------------------------------------
		// LIFE SPAN
		String lifeSpanProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/life_span");
		if(lifeSpanProperty != null)
			lifeSpan = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//global_data/doc/capital/life_span");
		
		//---------------------------------------------------------------
		// RESIDUAL VALUE
		String residualValueProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/residual_value");
		if(residualValueProperty != null)
			residualValue = Double.valueOf(residualValueProperty); 
		
		//---------------------------------------------------------------
		// AIRCRAFT PRICE
		String calculateAircraftPriceString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/doc/aircraft_price/@calculate");
		
		if(calculateAircraftPriceString.equalsIgnoreCase("TRUE")){
			
			String calculateAircraftPriceMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//global_data/utilization/@method");
			
			if(calculateAircraftPriceMethodString != null) {
				
				if(calculateAircraftPriceMethodString.equalsIgnoreCase("SFORZA")) 
					aircraftPrice = CostsCalcUtils.calcAircraftCostSforza(operatingEmptyMass);
			}
		}
		else {
			String aircraftPriceProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/aircraft_price");
			if(aircraftPriceProperty != null)
				aircraftPrice = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//global_data/doc/capital/aircraft_price");
		}
		
		//---------------------------------------------------------------
		// AIRFRAME RELATIVE SPARES COSTS
		String airframeRelativeSparesCostsProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/airframe_relative_spares_costs");
		if(airframeRelativeSparesCostsProperty != null)
			airframeRelativeSparesCosts = Double.valueOf(airframeRelativeSparesCostsProperty); 
		
		//---------------------------------------------------------------
		// ENGINES RELATIVE SPARES COSTS
		String engineRelativeSparesCostsProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/engines_relative_spares_costs");
		if(engineRelativeSparesCostsProperty != null)
			engineRelativeSparesCosts = Double.valueOf(engineRelativeSparesCostsProperty); 
			
		//---------------------------------------------------------------
		// INTEREST RATE
		String interestRateProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/interest");
		if(interestRateProperty != null)
			interestRate = Double.valueOf(interestRateProperty); 
		
		//---------------------------------------------------------------
		// INSURANCE RATE
		String insuranceRateProperty = reader.getXMLPropertyByPath("//global_data/doc/capital/insurance");
		if(insuranceRateProperty != null)
			insuranceRate = Double.valueOf(insuranceRateProperty); 
		
		//---------------------------------------------------------------
		// CABIN LABOUR RATE
		String cabinLabourRateProperty = reader.getXMLPropertyByPath("//global_data/doc/crew/cabin_labour_rate");
		if(cabinLabourRateProperty != null)
			cabinLabourRate = (Amount<?>) reader.getXMLAmountWithUnitByPath("//global_data/doc/crew/cabin_labour_rate"); 
		
		//---------------------------------------------------------------
		// COCKPIT LABOUR RATE
		String cockpitLabourRateProperty = reader.getXMLPropertyByPath("//global_data/doc/crew/cockpit_labour_rate");
		if(cockpitLabourRateProperty != null)
			cockpitLabourRate = (Amount<?>) reader.getXMLAmountWithUnitByPath("//global_data/doc/crew/cockpit_labour_rate"); 
		
		//---------------------------------------------------------------
		// FUEL UNIT PRICE
		String fuelUnitPriceProperty = reader.getXMLPropertyByPath("//global_data/doc/fuel/unit_price");
		if(fuelUnitPriceProperty != null)
			fuelUnitPrice = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//global_data/doc/fuel/unit_price"); 
		
		// TODO: CONTINUE READING ALL THE DATA
		
		
		//===================================================================================================
		// TODO : FOR VINCENZO --> EXAMPLE ON NOISE (DO THE SAME FOR OTHER DATA TO BE CALCULATED OR ASSINGED)
		//---------------------------------------------------------------
		// NOISE CHARGES
		
		String calculateNoiseChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/noise/@calculate");
		
		if(calculateNoiseChargesString.equalsIgnoreCase("TRUE")){
			
			Double noiseCostant = null;
			String noiseCostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@noise_constant");
			if(noiseCostantString != null)
				noiseCostant = Double.valueOf(noiseCostantString);
			
			Double flyoverCertifiedNoiseLevel = null;
			String flyoverCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@flyover_Certified_Noise_Level");
			if(flyoverCertifiedNoiseLevelString != null)
				flyoverCertifiedNoiseLevel = Double.valueOf(flyoverCertifiedNoiseLevelString);
			
			Double lateralCertifiedNoiseLevel = null;
			String lateralCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@lateral_Certified_Noise_Level");
			if(lateralCertifiedNoiseLevelString != null)
				lateralCertifiedNoiseLevel = Double.valueOf(lateralCertifiedNoiseLevelString);
			
			Double approachCertifiedNoiseLevel = null;
			String approachCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@approach_Certified_Noise_Level");
			if(approachCertifiedNoiseLevelString != null)
				approachCertifiedNoiseLevel = Double.valueOf(approachCertifiedNoiseLevelString);
			
			Double departureThreshold = null;
			String departureThresholdString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@departure_threshold");
			if(departureThresholdString != null)
				departureThreshold = Double.valueOf(departureThresholdString);
			
			Double arrivalThreshold = null;
			String arrivalThresholdString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@arrival_threshold");
			if(arrivalThresholdString != null)
				arrivalThreshold = Double.valueOf(arrivalThresholdString);
			
			noiseCharges = CostsCalcUtils.calcDOCNoiseCharges(
					Amount.valueOf(approachCertifiedNoiseLevel, NonSI.DECIBEL),
					Amount.valueOf(lateralCertifiedNoiseLevel, NonSI.DECIBEL),
					Amount.valueOf(flyoverCertifiedNoiseLevel, NonSI.DECIBEL),
					Amount.valueOf(noiseCostant, Currency.USD),
					Amount.valueOf(departureThreshold, NonSI.DECIBEL),
					Amount.valueOf(arrivalThreshold, NonSI.DECIBEL)
					);
		}
		else {
			String noiseChargesProperty = reader.getXMLPropertyByPath("//charges/noise");
			if(noiseChargesProperty != null)
				noiseCharges = (Amount<?>) reader.getXMLAmountWithUnitByPath("//charges/noise");
		}
		//===================================================================================================
		
		
		
		
		//---------------------------------------------------------------
		// READING PLOT LIST
		//---------------------------------------------------------------
		List<CostsPlotEnum> plotList = new ArrayList<CostsPlotEnum>();
		if(theAircraft.getTheAnalysisManager().getPlotCosts() == Boolean.TRUE) {
			
			/*
			 * TODO: CHECK WHICH ANALYSES HAVE TO BE PERFORMED TO OBTAIN EACH PLOT ... 
			 * (in DOC_vs_Range the check is only an example of how to do that)
			 */
			
			//...............................................................
			// DOC_vs_RANGE
			if(theAircraft.getTheAnalysisManager().getTaskListCosts().contains(CostsEnum.DOC_CAPITAL)) {
				String docVsRangeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/DOC_vs_Range/@perform");
				if (docVsRangeProperty != null) {
					if(docVsRangeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.DOC_RANGE);
				}
			}			
		}
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the ACBalanceManager object can be created
		 * using the builder pattern.
		 */
		IACCostsManager theCostsBuilderInterface = new IACCostsManager.Builder()
				.setId(id)
				.setAircraft(theAircraft)
				.setOperatingConditions(theOperatingConditions)
				.putAllTaskList(taskList)
				.addAllPlotList(plotList)
				.setMaximumTakeOffMass(maximumTakeOffMass)
				.setOperatingEmptyMass(operatingEmptyMass)
				.setPayload(payloadMass)
				.setRange(range)
				.setBlockFuelMass(blockFuel)
				.setFlightTime(flightTime)
				.setUtilization(utilization)
				.setLifeSpan(lifeSpan)
				.setResidualValue(residualValue)
				.setAircraftPrice(aircraftPrice)
				.setAirframeRelativeSparesCosts(airframeRelativeSparesCosts)
				.setEnginesRelativeSparesCosts(engineRelativeSparesCosts)
				.setInterestRate(interestRate)
				.setInsuranceRate(insuranceRate)
				.setCockpitLabourRate(cockpitLabourRate)
				.setCabinLabourRate(cabinLabourRate)
				.setFuelUnitPrice(fuelUnitPrice)
				// TODO: continue adding ...
				.setNoiseCharges(noiseCharges)
				.build();
		
		return theCostsBuilderInterface;
		
	}
	private void initializeAnalysis() {
	
		initializeData();
		initializeArrays();
		
	}
	
	private void initializeArrays() {
		
		_cockpitCrewCost = new HashMap<>();
		_cabinCrewCost = new HashMap<>();
		_labourAirframeMaintenanceCharges = new HashMap<>();
		_materialAirframeMaintenanceCharges = new HashMap<>();
		_labourEngineMaintenanceCharges = new HashMap<>();
		_materialEngineMaintenanceCharges = new HashMap<>();

		_depreciation = new HashMap<>();
		_interest= new HashMap<>();
		_insurance= new HashMap<>();
		_capitalDOC= new HashMap<>();

		_crewDOC= new HashMap<>();

		_fuelDOC= new HashMap<>();

		_chargesDOC= new HashMap<>();

		_airframeMaintenanceCharges= new HashMap<>();
		_engineMaintenanceCharges= new HashMap<>();
		_maintenanceChargesDOC= new HashMap<>();
		
	}

	private void initializeData() {

		_airframeMass = Amount.valueOf(
				_theCostsBuilderInterface.getOperatingEmptyMass().doubleValue(NonSI.POUND) -
				_theCostsBuilderInterface.getAircraft().getPowerPlant().getDryMassPublicDomainTotal().doubleValue(NonSI.POUND)*_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
				NonSI.POUND);
				
		_airframeCost = 
				_theCostsBuilderInterface.getAircraftPrice()
				.minus(_theCostsBuilderInterface.getEnginePrice()
						.times(_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
						);
		
		_totalInvestment = CostsCalcUtils.calcTotalInvestments(
				_airframeCost,
				_theCostsBuilderInterface.getEnginePrice(),
				_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
				_theCostsBuilderInterface.getAirframeRelativeSparesCosts(),
				_theCostsBuilderInterface.getEnginesRelativeSparesCosts()
				);
		
		
		_landingChargeConstant = CostsCalcUtils.calcLandingChargeConstant(_theCostsBuilderInterface.getRange());
		
		_navigationChargeConstant = CostsCalcUtils.calcNavigationChargeConstant(_theCostsBuilderInterface.getRange());
		
		_groundHandlingChargeConstant = CostsCalcUtils.calcGroundHandlingChargeConstant(_theCostsBuilderInterface.getRange());
		
		
		_blockTime = CostsCalcUtils.calcBlockTime(_theCostsBuilderInterface.getFlightTime(), _theCostsBuilderInterface.getRange()); 
		
		
	}

	public void calculate() {
		
		initializeAnalysis();
	
		// TODO : FILL ME !!
		/*
		 * CREATE INNER CLASSES FOR EACH "AIRCRAFT" ANALYSIS
		 * AND CALL THEM HERE IF REQUIRED BY THE TASK MAP 
		 */
		
		// TODO: RECOGNIZE WHICH CHART HAS TO BE PLOT (if on the plotList)
		
		try {
			toXLSFile("???");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		// TODO : FILL ME !!
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tCosts Analysis\n")
				.append("\t-------------------------------------\n")
				;
	
		// TODO : FILL ME !!
		
		return sb.toString();
		
	}
	
	//............................................................................
	// CAPITAL DOC INNER CLASS
	//............................................................................
	public class CalcCapitalDOC {

		public void calculateDOCCapitalAEA() {
			
			_insurance.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcInsuranceAEA(
							_theCostsBuilderInterface.getAircraftPrice(),
							_theCostsBuilderInterface.getUtilization(), 
							_theCostsBuilderInterface.getInsuranceRate()
							)
					);
			
			_interest.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcInterestAEA(
							_totalInvestment,
							_theCostsBuilderInterface.getUtilization(), 
							_theCostsBuilderInterface.getInterestRate()
							)
					);
			
			_depreciation.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDepreciationAEA(
							_totalInvestment,
							_theCostsBuilderInterface.getUtilization(), 
							_theCostsBuilderInterface.getLifeSpan(),
							_theCostsBuilderInterface.getResidualValue()
							)
					);
			
			_capitalDOC.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCCapitalAEA(
							_depreciation.get(MethodEnum.AEA),
							_insurance.get(MethodEnum.AEA),
							_interest.get(MethodEnum.AEA)
							)
					);
		}
		
	}
	//............................................................................
	// END CAPITAL DOC INNER CLASS
	//............................................................................
	
	//............................................................................
	// CREW DOC INNER CLASS
	//............................................................................
	public class CalcCrewDOC {

		public void calculateDOCCrewAEA() {

			_cockpitCrewCost.put(MethodEnum.AEA,
					CostsCalcUtils.calcCockpitCrewCostAEA(
							_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getFlightCrewNumber(), 
							_theCostsBuilderInterface.getCockpitLabourRate()
							)
					);


			_cabinCrewCost.put(MethodEnum.AEA,
					CostsCalcUtils.calcCabinCrewCostAEA(
							_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getCabinCrewNumber(), 
							_theCostsBuilderInterface.getCabinLabourRate()
							)
					);
			
			_crewDOC.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCCrew(
							_cabinCrewCost.get(MethodEnum.AEA),
							_cockpitCrewCost.get(MethodEnum.AEA)
							)
					);
		}
		
		
		public void calculateDOCCrewATA() {

			_cockpitCrewCost.put(MethodEnum.ATA,
					CostsCalcUtils.calcCockpitCrewCostATA(
							_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getFlightCrewNumber(),
							_theCostsBuilderInterface.getMaximumTakeOffMass(), 
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType()
							)
					);

			_cabinCrewCost.put(MethodEnum.ATA,
					CostsCalcUtils.calcCabinCrewCostATA(
							_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getNPax()
							)
					);
			
			_crewDOC.put(
					MethodEnum.ATA, 
					CostsCalcUtils.calcDOCCrew(
							_cabinCrewCost.get(MethodEnum.ATA),
							_cockpitCrewCost.get(MethodEnum.ATA)
							)
					);
		}
		
	}
	//............................................................................
	// END CREW DOC INNER CLASS
	//............................................................................
	
	//............................................................................
	// FUEL DOC INNER CLASS
	//............................................................................
	public class CalcFuelDOC {

		public void calculateDOCFuelAEA() {
			
			_fuelDOC.put(
					MethodEnum.AEA,
					CostsCalcUtils.calcDOCFuel(
							_theCostsBuilderInterface.getFuelUnitPrice(),
							_theCostsBuilderInterface.getAircraft().getFuelTank().getFuelDensity(),
							_theCostsBuilderInterface.getBlockFuelMass()
							)
					);

		}
		
	}
	//............................................................................
	// END FUEL DOC INNER CLASS
	//............................................................................
	
	//............................................................................
	// CHARGES DOC INNER CLASS
	//............................................................................
	public class CalcChargesDOC {

		public void calculateDOCChargesAEA() {

			
			_landingCharges = CostsCalcUtils.calcDOCLandingCharges(
							_landingChargeConstant,
							_theCostsBuilderInterface.getMaximumTakeOffMass()
					);
			
			_navigationCharges=
					CostsCalcUtils.calcDOCNavigationCharges(
							_navigationChargeConstant,
							_theCostsBuilderInterface.getRange(),
							_theCostsBuilderInterface.getMaximumTakeOffMass());
			
			_groundHandlingCharges=
					CostsCalcUtils.calcDOCGroundHandlingCharges(
							_groundHandlingChargeConstant,
							_theCostsBuilderInterface.getPayload());
			
			_chargesDOC.put(
					MethodEnum.AEA,
					CostsCalcUtils.calcDOCChargesAEA(
							_landingCharges,
							_navigationCharges,
							_groundHandlingCharges
							)
					);

		}
		
		
		// CHARGES with noise and emissions
		public void calculateDOCChargesILRAachen() {
			_landingCharges = CostsCalcUtils.calcDOCLandingCharges(
					_landingChargeConstant,
					_theCostsBuilderInterface.getMaximumTakeOffMass()
			);
	
			_navigationCharges=
					CostsCalcUtils.calcDOCNavigationCharges(
							_navigationChargeConstant,
							_theCostsBuilderInterface.getRange(),
							_theCostsBuilderInterface.getMaximumTakeOffMass());

			_groundHandlingCharges=
					CostsCalcUtils.calcDOCGroundHandlingCharges(
							_groundHandlingChargeConstant,
							_theCostsBuilderInterface.getPayload());

			/*
			 * FIXME: ALL THESE DATA MUST COME FROM INPUT (CALCULATED OR ASSIGNED) 
			 *        (compete ImportFromXML)
			 */
//			_noiseCharges=
//					CostsCalcUtils.calcDOCNoiseCharges(
//							_theCostsBuilderInterface.getApproachCertifiedNoiseLevel(),
//							_theCostsBuilderInterface.getLateralCertifiedNoiseLevel(),
//							_theCostsBuilderInterface.getFlyoverCertifiedNoiseLevel(),
//							_theCostsBuilderInterface.getNoiseConstant(),
//							_theCostsBuilderInterface.getNoiseDepartureThreshold(),
//							_theCostsBuilderInterface.getNoiseArrivalThreshold());
//
//			_emissionsChargesNOx= 
//					CostsCalcUtils.calcDOCEmissionsCharges(
//							_theCostsBuilderInterface.getEmissionsConstantNOx(),
//							_theCostsBuilderInterface.getMassNOx(),
//							_theCostsBuilderInterface.getDpHCFooNOx(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
//
//			_emissionsChargesCO=
//					CostsCalcUtils.calcDOCEmissionsCharges(
//							_theCostsBuilderInterface.getEmissionsConstantCO(),
//							_theCostsBuilderInterface.getMassCO(),
//							_theCostsBuilderInterface.getDpHCFooCO(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
//
//			_emissionsChargesCO2=
//					CostsCalcUtils.calcDOCEmissionsCharges(
//							_theCostsBuilderInterface.getEmissionsConstantCO2(),
//							_theCostsBuilderInterface.getMassCO2(),
//							_theCostsBuilderInterface.getDpHCFooCO2(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
//
//			_emissionsChargesHC= 
//					CostsCalcUtils.calcDOCEmissionsCharges(
//							_theCostsBuilderInterface.getEmissionsConstantHC(),
//							_theCostsBuilderInterface.getMassHC(),
//							_theCostsBuilderInterface.getDpHCFooHC(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
//							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());

			_emissionsCharges=
					_theCostsBuilderInterface.getEmissionsChargesHC().plus(
							_theCostsBuilderInterface.getEmissionsChargesCO2()).plus(
									_theCostsBuilderInterface.getEmissionsChargesCO()).plus(
											_theCostsBuilderInterface.getEmissionsChargesNOx());


			_chargesDOC.put(
					MethodEnum.AEA,
					CostsCalcUtils.calcDOCChargesILRAachen(
							_landingCharges,
							_navigationCharges,
							_groundHandlingCharges,
							_theCostsBuilderInterface.getNoiseCharges(),
							_emissionsCharges
							)
					);
		}
		
	}
	//............................................................................
	// END CHARGES DOC INNER CLASS
	//............................................................................
	
	//............................................................................
	// MAINTENANCE DOC INNER CLASS
	//............................................................................
	public class CalcMaintenanceDOC {

		@SuppressWarnings("unchecked")
		public void calculateDOCMaintenanceATA() {

			// AIRFRAME: Labour cost
			_labourAirframeMaintenanceCharges.put(MethodEnum.ATA, 
					CostsCalcUtils.calcDOCLabourAirframeMaintenanceATA(
							_theCostsBuilderInterface.getAirframeLabourRate(),
							_airframeMass,
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
							_theCostsBuilderInterface.getFlightTime(),
							_blockTime,
							_theCostsBuilderInterface.getRange())
							);
			
			// AIRFRAME: Material cost
			_materialAirframeMaintenanceCharges.put(MethodEnum.ATA, 
					CostsCalcUtils.calcDOCMaterialAirframeMaintenanceATA(
							_theCostsBuilderInterface.getAircraftPrice(),
							_theCostsBuilderInterface.getEnginePrice(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
							_theCostsBuilderInterface.getFlightTime(),
							_blockTime,
							_theCostsBuilderInterface.getRange())
							);
			
			// AIRFRAME maintenance cost
			_airframeMaintenanceCharges.put(MethodEnum.ATA, 
					Amount.valueOf(
							_labourAirframeMaintenanceCharges.get(MethodEnum.ATA).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)+
							_materialAirframeMaintenanceCharges.get(MethodEnum.ATA).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE),
							MyUnits.USD_PER_NAUTICAL_MILE)
							);
			
			// ENGINE: Labour cost
			if(_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType() == EngineTypeEnum.TURBOFAN){
			_labourEngineMaintenanceCharges.put(MethodEnum.ATA, 
					CostsCalcUtils.calcDOCLabourEngineMaintenanceATA(
							_theCostsBuilderInterface.getAirframeLabourRate(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getT0Total().doubleValue(NonSI.POUND_FORCE),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
							_theCostsBuilderInterface.getFlightTime(),
							_blockTime,
							_theCostsBuilderInterface.getRange())
							);
			}
			else if(_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP){
				_labourEngineMaintenanceCharges.put(MethodEnum.ATA, 
						CostsCalcUtils.calcDOCLabourEngineMaintenanceATA(
								_theCostsBuilderInterface.getAirframeLabourRate(),
								_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total().doubleValue(NonSI.HORSEPOWER),
								_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
								_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
								_theCostsBuilderInterface.getFlightTime(),
								_blockTime,
								_theCostsBuilderInterface.getRange())
								);
				
			}
			
			// ENGINE: Material cost
			_materialEngineMaintenanceCharges.put(MethodEnum.ATA, 
					CostsCalcUtils.calcDOCMaterialEngineMaintenanceATA(
							_theCostsBuilderInterface.getEnginePrice(),
							_theCostsBuilderInterface.getFlightTime(),
							_blockTime,
							_theCostsBuilderInterface.getRange(),
							_theCostsBuilderInterface.getOperatingConditions().getMachCruise(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
							);
			
			// ENGINE maintenance cost
			_engineMaintenanceCharges.put(MethodEnum.ATA, 
					Amount.valueOf(
							_labourEngineMaintenanceCharges.get(MethodEnum.ATA).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)+
							_materialEngineMaintenanceCharges.get(MethodEnum.ATA).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE),
							MyUnits.USD_PER_NAUTICAL_MILE)
							);
			
			
			
			// TOTAL MAINTENANCE COST ($/nm)
			_maintenanceChargesDOC.put(
					MethodEnum.ATA, 
					CostsCalcUtils.calcDOCMaintenanceCharges(
							_labourAirframeMaintenanceCharges.get(MethodEnum.ATA),
							_materialAirframeMaintenanceCharges.get(MethodEnum.ATA),
							_labourEngineMaintenanceCharges.get(MethodEnum.ATA),
							_materialEngineMaintenanceCharges.get(MethodEnum.ATA))
					);

		}
		
	}
	//............................................................................
	// END MAINTENANCE DOC INNER CLASS
	//............................................................................
	
	//............................................................................
	// PLOT INNER CLASS
	//............................................................................
	public class PlotDOC {

		public void plotDocVsRange() {


		}
		
		public void plotDocVsBlockTime() {


		}
		
		public void plotDocVsBlockFuel() {


		}
		
		public void plotProfitability() {


		}
		
		public void plotDocPieChart() {


		}
		
	}
	//............................................................................
	// END PLOT INNER CLASS
	//............................................................................
	
	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	
	
	
}
