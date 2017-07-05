package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.ACBalanceManager.ACBalanceManagerBuilder;
import calculators.costs.CostsCalcUtils;
import configuration.MyConfiguration;
import configuration.enumerations.CostsDerivedDataEnum;
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
	private Amount<?> _emissionsChargesNOx;
	private Amount<?> _emissionsChargesCO;
	private Amount<?> _emissionsChargesCO2;
	private Amount<?> _emissionsChargesHC;
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
	private Amount<?> _noiseCharges;
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
	@SuppressWarnings("unchecked")
	public static IACCostsManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions
			) throws IOException {
		
		// TODO : FILL ME !!
		
		// Preliminary operation 
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
		// end of preliminary operation
		
		// Initializing weights data
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
				}


				//---------------------------------------------------------------
				// OPERATING EMPTY MASS
				Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(2);
				if(operatingEmptyMassCell != null)
					operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// Payload
				Cell payloadMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Passengers Mass").get(0)).getCell(2);
				if(payloadMassCell != null)
					payloadMass = Amount.valueOf(payloadMassCell.getNumericCellValue(), SI.KILOGRAM);

				
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
		
		// end of Initializing weights data
		
		
		// Initializing weights data
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
				}


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
		
		
		// Initialize costs data
		//---------------------------------------------------------------
		Amount<?> utilization = null;
		if(range.doubleValue(NonSI.NAUTICAL_MILE) <=2200){
			utilization = Amount.valueOf(3750, MyUnits.HOURS_PER_YEAR);
		}
		else{
			utilization = Amount.valueOf(4800, MyUnits.HOURS_PER_YEAR);
		}
		
		Amount<Duration> lifeSpan = Amount.valueOf(16, NonSI.YEAR);
		Double residualValue = 0.1; 
		Amount<Money> aircraft_price = null;
		Double airframeRelativeSparesCosts = 0.1;
		Double engineRelativeSparesCosts = 0.3;
		Double interestRate = 0.054;
		Double insuranceRate = 0.005;
		Amount<?> cockpitLabourRate = Amount.valueOf(360, MyUnits.USD_PER_HOUR);
		Amount<?> cabinLabourRate = Amount.valueOf(90, MyUnits.USD_PER_HOUR);
		Amount<?> fuelUnitPrice = Amount.valueOf(59.2, MyUnits.USD_PER_BARREL);

		Amount<?> noiseCharges = null;
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
			
			
			String flyoverCertifiedNoiseLevel = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@flyover_Certified_Noise_Level");
			
			String lateralCertifiedNoiseLevel = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@lateral_Certified_Noise_Level");
			
			String approachCertifiedNoiseLevel = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@approach_Certified_Noise_Level");
			
			String departureThreshold = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@departure_threshold");
			
			String arrivalThreshold = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@arrival_threshold");
			
		}
		
		
		
		// RANGE
		String rangeProperty = reader.getXMLPropertyByPath("//performance/range");
		if(rangeProperty != null)
			range = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//performance/range");
		
		
		
		
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the ACBalanceManager object can be created
		 * using the builder pattern.
		 */
		ACBalanceManager theBalance = new ACBalanceManagerBuilder(id, theAircraft)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.passengersTotalMass(payloadMass)
				.passengersSingleMass(passengersSingleMass)
				.fuselageMass(fuselageMass)
				.wingMass(wingMass)
				.horizontalTailMass(horizontalTailMass)
				.verticalTailMass(verticalTailMass)
				.canardMass(canardMass)
				.nacellesMass(nacellesMassList)
				.enginesMass(enginesMassList)
				.landingGearsMass(landingGearsMass)
				.build();
		
		return theBalance;
		
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
				_theCostsBuilderInterface.getAircraftPrice().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.AIRCRAFT_PRICE))
				.minus(_theCostsBuilderInterface.getEnginePrice().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.ENGINE_PRICE))
						.times(_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
						);
		
		_totalInvestment = CostsCalcUtils.calcTotalInvestments(
				_airframeCost,
				_theCostsBuilderInterface.getEnginePrice().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.ENGINE_PRICE)),
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
							_theCostsBuilderInterface.getAircraftPrice().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.AIRCRAFT_PRICE)),
							_theCostsBuilderInterface.getUtilization().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.UTILIZATION)), 
							_theCostsBuilderInterface.getInsuranceRate()
							)
					);
			
			_interest.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcInterestAEA(
							_totalInvestment,
							_theCostsBuilderInterface.getUtilization().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.UTILIZATION)), 
							_theCostsBuilderInterface.getInterestRate()
							)
					);
			
			_depreciation.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDepreciationAEA(
							_totalInvestment,
							_theCostsBuilderInterface.getUtilization().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.UTILIZATION)), 
							_theCostsBuilderInterface.getLifeSpan(),
							_theCostsBuilderInterface.getResidualValue()
							)
					);
			
			_capitalDOC.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCCapitalAEA(
							_depreciation.get(_depreciation),
							_insurance.get(_insurance),
							_interest.get(_interest)
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
			
			_noiseCharges=
					CostsCalcUtils.calcDOCNoiseCharges(
							_theCostsBuilderInterface.getApproachCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getLateralCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getFlyoverCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getNoiseConstant(),
							_theCostsBuilderInterface.getNoiseDepartureThreshold(),
							_theCostsBuilderInterface.getNoiseArrivalThreshold());
			
			_emissionsChargesNOx=
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantNOx(),
							_theCostsBuilderInterface.getMassNOx(),
							_theCostsBuilderInterface.getDpHCFooNOx(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
			
			_emissionsChargesCO=
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO(),
							_theCostsBuilderInterface.getMassCO(),
							_theCostsBuilderInterface.getDpHCFooCO(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
			
			_emissionsChargesCO2=
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO2(),
							_theCostsBuilderInterface.getMassCO2(),
							_theCostsBuilderInterface.getDpHCFooCO2(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
			
			_emissionsChargesHC= 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantHC(),
							_theCostsBuilderInterface.getMassHC(),
							_theCostsBuilderInterface.getDpHCFooHC(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());
			
			_emissionsCharges=
					_emissionsChargesHC.plus(
							_emissionsChargesCO2).plus(
									_emissionsChargesCO).plus(
												_emissionsChargesNOx);
			
			
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
		public void calculateDOCChargesFRANZ() {
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

			_noiseCharges=
					CostsCalcUtils.calcDOCNoiseCharges(
							_theCostsBuilderInterface.getApproachCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getLateralCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getFlyoverCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getNoiseConstant(),
							_theCostsBuilderInterface.getNoiseDepartureThreshold(),
							_theCostsBuilderInterface.getNoiseArrivalThreshold());

			_emissionsChargesNOx= 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantNOx(),
							_theCostsBuilderInterface.getMassNOx(),
							_theCostsBuilderInterface.getDpHCFooNOx(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());

			_emissionsChargesCO=
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO(),
							_theCostsBuilderInterface.getMassCO(),
							_theCostsBuilderInterface.getDpHCFooCO(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());

			_emissionsChargesCO2=
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO2(),
							_theCostsBuilderInterface.getMassCO2(),
							_theCostsBuilderInterface.getDpHCFooCO2(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());

			_emissionsChargesHC= 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantHC(),
							_theCostsBuilderInterface.getMassHC(),
							_theCostsBuilderInterface.getDpHCFooHC(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber());

			_emissionsCharges=
					_emissionsChargesHC.plus(
							_emissionsChargesCO2).plus(
									_emissionsChargesCO).plus(
											_emissionsChargesNOx);


			_chargesDOC.put(
					MethodEnum.AEA,
					CostsCalcUtils.calcDOCChargesILRAachen(
							_landingCharges,
							_navigationCharges,
							_groundHandlingCharges,
							_noiseCharges,
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
							_theCostsBuilderInterface.getAircraftPrice().get(MethodEnum.SFORZA),
							_theCostsBuilderInterface.getEnginePrice().get(MethodEnum.SFORZA),
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
							_theCostsBuilderInterface.getEnginePrice().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.ENGINE_PRICE)),
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
