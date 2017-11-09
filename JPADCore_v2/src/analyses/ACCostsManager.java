package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
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
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class ACCostsManager {
	
	/*
	 *******************************************************************************
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
	private Amount<?> _noiseCharges;
	private Amount<?> _emissionsCharges;
	private Map<MethodEnum, Amount<?>> _chargesDOC;
	
	private Map<MethodEnum, Amount<?>> _airframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _engineMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _maintenanceChargesDOC;
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "resource" })
	public static ACCostsManager importFromXML (
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

		Boolean readWeightFromPreviousAnalysisFlag;
		Boolean readPerformanceFromPreviousAnalysisFlag;
		
		String readWeightFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_previous_analysis");
		
		if(readWeightFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readWeightFromPreviousAnalysisFlag = Boolean.TRUE;
		else
			readWeightFromPreviousAnalysisFlag = Boolean.FALSE;
		
		String readPerformanceFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@performance_from_previous_analysis");
		
		if(readPerformanceFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readPerformanceFromPreviousAnalysisFlag = Boolean.TRUE;
		else
			readPerformanceFromPreviousAnalysisFlag = Boolean.FALSE;

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
		if(readWeightFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheWeights() != null) {

					//---------------------------------------------------------------
					// MAXIMUM TAKE-OFF MASS
					maximumTakeOffMass = theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// OPERATING EMPTY MASS
					operatingEmptyMass = theAircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// PAYLOAD MASS
					payloadMass = theAircraft.getTheAnalysisManager().getTheWeights().getPaxMass().to(SI.KILOGRAM);
					
				}
				else {
					System.err.println("WARNING!! THE WEIGHTS ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
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
		if(readPerformanceFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getThePerformance() != null) {

					//---------------------------------------------------------------
					// RANGE
					range = theAircraft.getTheAnalysisManager().getThePerformance().getThePerformanceInterface().getMissionRange().to(NonSI.NAUTICAL_MILE);

//					//---------------------------------------------------------------
//					// BLOCK FUEL
//					blockFuel = theAircraft.getTheAnalysisManager().getThePerformance().getTotalFuelUsed().to(SI.KILOGRAM);
//
//					//---------------------------------------------------------------
//					// FLIGHT TIME
//					flightTime = theAircraft.getTheAnalysisManager().getThePerformance().getTotalMissionTime().to(NonSI.MINUTE);

				}
				else {
					System.err.println("WARNING!! THE PERFORMANCE ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
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
		Double noiseCostant = 4.12;
		Double flyoverCertifiedNoiseLevel = null;
		Double lateralCertifiedNoiseLevel = null;
		Double approachCertifiedNoiseLevel = null;
		Double departureThreshold = 91.0;
		Double arrivalThreshold = 86.0;
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
						"//global_data/doc/capital/aircraft_price/@calculate");
		
		if(calculateAircraftPriceString.equalsIgnoreCase("TRUE")){
			
			String calculateAircraftPriceMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//global_data/doc/capital/aircraft_price/@method");
			
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
		if(fuelUnitPriceProperty != null) {
			Amount<Money> fuelUnitPriceCurrency = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//global_data/doc/fuel/unit_price");
			fuelUnitPrice = Amount.valueOf(fuelUnitPriceCurrency.doubleValue(Currency.USD), MyUnits.USD_PER_BARREL); 
		}
		//---------------------------------------------------------------
		// LANDING CHARGES
		String calculateLandingChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/landing/@calculate");
		
		if(calculateLandingChargesString.equalsIgnoreCase("TRUE")){
			
			String calculateLandingChargesMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/landing/@method");
			
			if(calculateLandingChargesMethodString != null) {
				
				if(calculateLandingChargesMethodString.equalsIgnoreCase("AEA")) 
					landingCharges = CostsCalcUtils.calcDOCLandingCharges(
							CostsCalcUtils.calcLandingChargeConstant(range),
							maximumTakeOffMass
							);
			}
		}
		else {
			String landingChargesProperty = reader.getXMLPropertyByPath("//charges/landing");
			if(landingChargesProperty != null)
				landingCharges = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//charges/landing");
		}
		
		//---------------------------------------------------------------
		// NAVIGATION CHARGES
		String calculateNavigationChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/navigation/@calculate");
		
		if(calculateNavigationChargesString.equalsIgnoreCase("TRUE")){
			
			String calculateNavigationChargesMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/navigation/@method");
			
			if(calculateNavigationChargesMethodString != null) {
				
				if(calculateNavigationChargesMethodString.equalsIgnoreCase("AEA")) 
					navigationCharges = CostsCalcUtils.calcDOCNavigationCharges(
							CostsCalcUtils.calcNavigationChargeConstant(range), 
							range, 
							maximumTakeOffMass
							);
			}
		}
		else {
			String navigationChargesProperty = reader.getXMLPropertyByPath("//charges/navigation");
			if(navigationChargesProperty != null)
				navigationCharges = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//charges/navigation");
		}
		
		//---------------------------------------------------------------
		// GROUND HANDLING CHARGES
		String calculateGroundHandlingChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/ground_handling/@calculate");
		
		if(calculateGroundHandlingChargesString.equalsIgnoreCase("TRUE")){
			
			String calculateGroundHandlingChargesMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/ground_handling/@method");
			
			if(calculateGroundHandlingChargesMethodString != null) {
				
				if(calculateGroundHandlingChargesMethodString.equalsIgnoreCase("AEA")) 
					groundHandlingCharges = CostsCalcUtils.calcDOCGroundHandlingCharges(
							CostsCalcUtils.calcGroundHandlingChargeConstant(range), 
							payloadMass
							);
			}
		}
		else {
			String groundHandlingChargesProperty = reader.getXMLPropertyByPath("//charges/ground_handling");
			if(groundHandlingChargesProperty != null)
				groundHandlingCharges = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//charges/ground_handling");
		}
		
		//---------------------------------------------------------------
		// NOISE CHARGES
		String calculateNoiseChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/noise/@calculate");
		
		if(calculateNoiseChargesString.equalsIgnoreCase("TRUE")){
			
			String noiseCostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@noise_constant");
			if(!noiseCostantString.isEmpty())
				noiseCostant = Double.valueOf(noiseCostantString);
			
			String flyoverCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@flyover_Certified_Noise_Level");
			if(!flyoverCertifiedNoiseLevelString.isEmpty())
				flyoverCertifiedNoiseLevel = Double.valueOf(flyoverCertifiedNoiseLevelString);
			
			String lateralCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@lateral_Certified_Noise_Level");
			if(!lateralCertifiedNoiseLevelString.isEmpty())
				lateralCertifiedNoiseLevel = Double.valueOf(lateralCertifiedNoiseLevelString);
			
			String approachCertifiedNoiseLevelString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@approach_Certified_Noise_Level");
			if(!approachCertifiedNoiseLevelString.isEmpty())
				approachCertifiedNoiseLevel = Double.valueOf(approachCertifiedNoiseLevelString);
			
			String departureThresholdString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@departure_threshold");
			if(!departureThresholdString.isEmpty())
				departureThreshold = Double.valueOf(departureThresholdString);
			
			String arrivalThresholdString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/noise/@arrival_threshold");
			if(!arrivalThresholdString.isEmpty())
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
		
		//---------------------------------------------------------------
		// EMISSION NOx CHARGES
		String calculateEmissionNOxChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/emissions_NOx/@calculate");
		
		if(calculateEmissionNOxChargesString.equalsIgnoreCase("TRUE")){
			
			Double emissionNOxCostant = null;
			String emissionNOxCostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_NOx/@emission_constant");
			if(emissionNOxCostantString != null)
				emissionNOxCostant = Double.valueOf(emissionNOxCostantString);
			
			Double massNOx = null;
			String massNOxString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_NOx/@massNOx");
			if(massNOxString != null)
				massNOx = Double.valueOf(massNOxString);
			
			Double dpHCFooRatio = null;
			String dpHCFooRatioString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_NOx/@DpHC_Foo");
			if(dpHCFooRatioString != null)
				dpHCFooRatio = Double.valueOf(dpHCFooRatioString);
			
			emissionChargesNOx = CostsCalcUtils.calcDOCEmissionsCharges(
					Amount.valueOf(emissionNOxCostant, Currency.USD),
					Amount.valueOf(massNOx, SI.KILOGRAM),
					Amount.valueOf(dpHCFooRatio, MyUnits.G_PER_KN),
					theAircraft.getPowerPlant().getEngineType(),
					theAircraft.getPowerPlant().getP0Total(),
					theAircraft.getPowerPlant().getEngineNumber()
					);
		}
		else {
			String emissionChargesNOxProperty = reader.getXMLPropertyByPath("//charges/emissions_NOx");
			if(emissionChargesNOxProperty != null)
				emissionChargesNOx = (Amount<?>) reader.getXMLAmountWithUnitByPath("//charges/emissions_NOx");
		}
		
		//---------------------------------------------------------------
		// EMISSION CO CHARGES
		String calculateEmissionCOChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/emissions_CO/@calculate");
		
		if(calculateEmissionCOChargesString.equalsIgnoreCase("TRUE")){
			
			Double emissionCOCostant = null;
			String emissionCOCostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO/@emission_constant");
			if(emissionCOCostantString != null)
				emissionCOCostant = Double.valueOf(emissionCOCostantString);
			
			Double massCO = null;
			String massCOString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO/@massCO");
			if(massCOString != null)
				massCO = Double.valueOf(massCOString);
			
			Double dpHCFooRatio = null;
			String dpHCFooRatioString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO/@DpHC_Foo");
			if(dpHCFooRatioString != null)
				dpHCFooRatio = Double.valueOf(dpHCFooRatioString);
			
			emissionChargesCO = CostsCalcUtils.calcDOCEmissionsCharges(
					Amount.valueOf(emissionCOCostant, Currency.USD),
					Amount.valueOf(massCO, SI.KILOGRAM),
					Amount.valueOf(dpHCFooRatio, MyUnits.G_PER_KN),
					theAircraft.getPowerPlant().getEngineType(),
					theAircraft.getPowerPlant().getP0Total(),
					theAircraft.getPowerPlant().getEngineNumber()
					);
		}
		else {
			String emissionChargesCOProperty = reader.getXMLPropertyByPath("//charges/emissions_CO");
			if(emissionChargesCOProperty != null)
				emissionChargesCO = (Amount<?>) reader.getXMLAmountWithUnitByPath("//charges/emissions_CO");
		}
		
		//---------------------------------------------------------------
		// EMISSION CO2 CHARGES
		String calculateEmissionCO2ChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/emissions_CO2/@calculate");
		
		if(calculateEmissionCO2ChargesString.equalsIgnoreCase("TRUE")){
			
			Double emissionCO2Costant = null;
			String emissionCO2CostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO2/@emission_constant");
			if(emissionCO2CostantString != null)
				emissionCO2Costant = Double.valueOf(emissionCO2CostantString);
			
			Double massCO2 = null;
			String massCO2String = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO2/@massCO2");
			if(massCO2String != null)
				massCO2 = Double.valueOf(massCO2String);
			
			Double dpHCFooRatio = null;
			String dpHCFooRatioString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_CO2/@DpHC_Foo");
			if(dpHCFooRatioString != null)
				dpHCFooRatio = Double.valueOf(dpHCFooRatioString);
			
			emissionChargesCO2 = CostsCalcUtils.calcDOCEmissionsCharges(
					Amount.valueOf(emissionCO2Costant, Currency.USD),
					Amount.valueOf(massCO2, SI.KILOGRAM),
					Amount.valueOf(dpHCFooRatio, MyUnits.G_PER_KN),
					theAircraft.getPowerPlant().getEngineType(),
					theAircraft.getPowerPlant().getP0Total(),
					theAircraft.getPowerPlant().getEngineNumber()
					);
		}
		else {
			String emissionChargesCO2Property = reader.getXMLPropertyByPath("//charges/emissions_CO2");
			if(emissionChargesCO2Property != null)
				emissionChargesCO2 = (Amount<?>) reader.getXMLAmountWithUnitByPath("//charges/emissions_CO2");
		}
		
		//---------------------------------------------------------------
		// EMISSION HC CHARGES
		String calculateEmissionHCChargesString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//charges/emissions_HC/@calculate");
		
		if(calculateEmissionHCChargesString.equalsIgnoreCase("TRUE")){
			
			Double emissionHCCostant = null;
			String emissionHCCostantString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_HC/@emission_constant");
			if(emissionHCCostantString != null)
				emissionHCCostant = Double.valueOf(emissionHCCostantString);
			
			Double massHC = null;
			String massHCString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_HC/@massHC");
			if(massHCString != null)
				massHC = Double.valueOf(massHCString);
			
			Double dpHCFooRatio = null;
			String dpHCFooRatioString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//charges/emissions_HC/@DpHC_Foo");
			if(dpHCFooRatioString != null)
				dpHCFooRatio = Double.valueOf(dpHCFooRatioString);
			
			emissionChargesHC = CostsCalcUtils.calcDOCEmissionsCharges(
					Amount.valueOf(emissionHCCostant, Currency.USD),
					Amount.valueOf(massHC, SI.KILOGRAM),
					Amount.valueOf(dpHCFooRatio, MyUnits.G_PER_KN),
					theAircraft.getPowerPlant().getEngineType(),
					theAircraft.getPowerPlant().getP0Total(),
					theAircraft.getPowerPlant().getEngineNumber()
					);
		}
		else {
			String emissionChargesHCProperty = reader.getXMLPropertyByPath("//charges/emissions_HC");
			if(emissionChargesHCProperty != null)
				emissionChargesHC = (Amount<?>) reader.getXMLAmountWithUnitByPath("//charges/emissions_HC");
		}
		
		//---------------------------------------------------------------
		// AIRFRAME LABOUR RATE
		String airframeLabourRateProperty= reader.getXMLPropertyByPath("//global_data/doc/maintenance/airframe_labour_rate");
		if(airframeLabourRateProperty != null)
			airframeLabourRate = (Amount<?>) reader.getXMLAmountWithUnitByPath("//global_data/doc/maintenance/airframe_labour_rate");
		
		//---------------------------------------------------------------
		// ENGINE LABOUR RATE
		String engineLabourRateProperty= reader.getXMLPropertyByPath("//global_data/doc/maintenance/engine_labour_rate");
		if(engineLabourRateProperty != null)
			engineLabourRate = (Amount<?>) reader.getXMLAmountWithUnitByPath("//global_data/doc/maintenance/engine_labour_rate");
				
		//---------------------------------------------------------------
		// ENGINE PRICE
		String calculateEnginePriceString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//maintenance/engine_price/@calculate");
		
		if(calculateEnginePriceString.equalsIgnoreCase("TRUE")){
			if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) 
					|| theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET))
			enginePrice = Amount.valueOf(
					CostsCalcUtils.calcSingleEngineCostSforza(
					theAircraft.getPowerPlant().getT0Total().doubleValue(NonSI.POUND_FORCE), 
					theAircraft.getPowerPlant().getTurbofanEngineDatabaseReader().getSFC(
							theOperatingConditions.getMachCruise(), 
							theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
							theAircraft.getPowerPlant().getTurbofanEngineDatabaseReader().getThrustRatio(
														theOperatingConditions.getMachCruise(),
														theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
														theAircraft.getPowerPlant().getEngineList().get(0).getBPR(), 
														EngineOperatingConditionEnum.CRUISE
														), 
							theAircraft.getPowerPlant().getEngineList().get(0).getBPR(), 
							EngineOperatingConditionEnum.CRUISE)
							),
					Currency.USD
					);
			else if(theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
					|| theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)) {
				
				// TODO : (for Vincenzo) IMPLEMENT THE METHOD FOR TURBOPROP ... 
				
			}
					
		}
		else{
			String enginePriceProperty = reader.getXMLPropertyByPath("//maintenance/engine_price");
			if(enginePriceProperty != null)
				enginePrice = (Amount<Money>) reader.getXMLAmountWithUnitByPath("//maintenance/engine_price");
		}
		
		//---------------------------------------------------------------
		// READING PLOT LIST
		//---------------------------------------------------------------
		List<CostsPlotEnum> plotList = new ArrayList<CostsPlotEnum>();
		if(theAircraft.getTheAnalysisManager().getPlotCosts() == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager().getTaskListCosts().containsKey(CostsEnum.DOC_CAPITAL)
					|| theAircraft.getTheAnalysisManager().getTaskListCosts().containsKey(CostsEnum.DOC_CREW)
					|| theAircraft.getTheAnalysisManager().getTaskListCosts().containsKey(CostsEnum.DOC_FUEL)
					|| theAircraft.getTheAnalysisManager().getTaskListCosts().containsKey(CostsEnum.DOC_CHARGES)
					|| theAircraft.getTheAnalysisManager().getTaskListCosts().containsKey(CostsEnum.DOC_MAINTENANCE)
					) {
			
				//...............................................................
				// DOC_vs_RANGE
				String docVsRangeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/doc_vs_Range/@perform");
				if (docVsRangeProperty != null) {
					if(docVsRangeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.DOC_RANGE);
				}
				
				//...............................................................
				// DOC_vs_BLOCK FUEL
				String docVsBlockFuelProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/doc_vs_BlockFuel/@perform");
				if (docVsBlockFuelProperty != null) {
					if(docVsBlockFuelProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.DOC_BLOCK_FUEL);
				}
				
				//...............................................................
				// DOC_vs_BLOCK TIME
				String docVsBlockTimeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/doc_vs_BlockTime/@perform");
				if (docVsBlockTimeProperty != null) {
					if(docVsBlockTimeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.DOC_BLOCK_TIME);
				}

				//...............................................................
				// DOC BREAKDOWN
				String docBreakdownProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/doc_Breakdown/@perform");
				if (docBreakdownProperty != null) {
					if(docBreakdownProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.DOC_PIE_CHART);
				}
				
				//...............................................................
				// PROFITABILITY
				String profitabilityChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/profitability/@perform");
				if (profitabilityChartProperty != null) {
					if(profitabilityChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(CostsPlotEnum.PROFITABILITY);
				}
				
			}			
		}
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the ACCostManager object can be created
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
				.setLandingCharges(landingCharges)
				.setNavigationCharges(navigationCharges)
				.setGroundHandlingCharges(groundHandlingCharges)
				.setNoiseCharges(noiseCharges)
				.setEmissionsChargesNOx(emissionChargesNOx)
				.setEmissionsChargesCO(emissionChargesCO)
				.setEmissionsChargesCO2(emissionChargesCO2)
				.setEmissionsChargesHC(emissionChargesHC)
				.setAirframeLabourRate(airframeLabourRate)
				.setEngineLabourRate(engineLabourRate)
				.setEnginePrice(enginePrice)
				.build();
		
		ACCostsManager theCostsManager = new ACCostsManager();
		theCostsManager.setTheCostsBuilderInterface(theCostsBuilderInterface);
		
		return theCostsManager;
		
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
				_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().doubleValue(NonSI.POUND)
					*_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber(),
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
		
		
		_blockTime = CostsCalcUtils.calcBlockTime(_theCostsBuilderInterface.getFlightTime(), _theCostsBuilderInterface.getRange()); 
		
		
	}

	public void calculate(String resultsFolderPath) {
		
		initializeAnalysis();
	
		String costsFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "COSTS"
				+ File.separator
				);
		
		//=================================================================================================
		// DOC CALCULATION
		//=================================================================================================
		//-------------------------------------------------------------------------------------------------
		// DOC CAPITAL 
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)) {
			
			CalcCapitalDOC calcCapitalDOC = new CalcCapitalDOC();
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL).equals(MethodEnum.AEA)) 
				calcCapitalDOC.calculateDOCCapitalAEA();
			
		}
		
		//-------------------------------------------------------------------------------------------------
		// DOC CREW
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)) {
			
			CalcCrewDOC calcCrewDOC = new CalcCrewDOC();
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW).equals(MethodEnum.AEA)) 
				calcCrewDOC.calculateDOCCrewAEA();
			
			else if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW).equals(MethodEnum.ATA)) 
				calcCrewDOC.calculateDOCCrewATA();
			
		}
		
		//-------------------------------------------------------------------------------------------------
		// DOC FUEL
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)) {
			
			CalcFuelDOC calcFuelDOC = new CalcFuelDOC();
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL).equals(MethodEnum.AEA)) 
				calcFuelDOC.calculateDOCFuelAEA();
			
		}
		
		//-------------------------------------------------------------------------------------------------
		// DOC CHARGES
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)) {
			
			CalcChargesDOC calcChargesDOC = new CalcChargesDOC();
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.AEA)) 
				calcChargesDOC.calculateDOCChargesAEA();
			
			else if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) 
				calcChargesDOC.calculateDOCChargesILRAachen();
			
		}
		
		//-------------------------------------------------------------------------------------------------
		// DOC MAINTENANCE
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)) {
			
			CalcMaintenanceDOC calcMaintenanceDOC = new CalcMaintenanceDOC();
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE).equals(MethodEnum.ATA)) 
				calcMaintenanceDOC.calculateDOCMaintenanceATA();
			
		}
		
		//=================================================================================================
		// PLOTS
		//=================================================================================================
		if(_theCostsBuilderInterface.getAircraft().getTheAnalysisManager().getPlotCosts()) {
			
			PlotDOC plotDOC = new PlotDOC();

			//-------------------------------------------------------------------------------------------------
			// DOC BREAKDOWN 
			if(_theCostsBuilderInterface.getPlotList().contains(CostsPlotEnum.DOC_PIE_CHART)) 
				if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)
						|| _theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)
						|| _theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)
						|| _theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)
						|| _theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)
						)
					plotDOC.plotDocPieChart(costsFolderPath);
			
			//-------------------------------------------------------------------------------------------------
			// DOC vs RANGE 
			if(_theCostsBuilderInterface.getPlotList().contains(CostsPlotEnum.DOC_RANGE)) 
				plotDOC.plotDocVsRange(); // TODO
			
			//-------------------------------------------------------------------------------------------------
			// DOC vs BLOCK FUEL 
			if(_theCostsBuilderInterface.getPlotList().contains(CostsPlotEnum.DOC_BLOCK_FUEL)) 
				plotDOC.plotDocVsBlockFuel(); // TODO
			
			//-------------------------------------------------------------------------------------------------
			// DOC vs BLOCK TIME 
			if(_theCostsBuilderInterface.getPlotList().contains(CostsPlotEnum.DOC_BLOCK_TIME)) 
				plotDOC.plotDocVsBlockTime(); // TODO
			
			//-------------------------------------------------------------------------------------------------
			// PROFITABILITY
			if(_theCostsBuilderInterface.getPlotList().contains(CostsPlotEnum.PROFITABILITY)) 
				plotDOC.plotProfitability(); // TODO
			
		}
		
		try {
			toXLSFile(costsFolderPath + "Costs");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
			outputFile.delete();		
			System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		Sheet sheetDOC = wb.createSheet("DOC");
		List<Object[]> dataListDOC = new ArrayList<>();
		dataListDOC.add(new Object[] {"Description","$/flight","$/h","$/nmi","¢/(nmi*seat)"});
		
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)) {
			
			//--------------------------------------------------------------------------------
			// CAPITAL DOC:
			//--------------------------------------------------------------------------------
			dataListDOC.add(new Object[] {
					"Depreciation", 
					_depreciation.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_depreciation.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR),
					_depreciation.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_depreciation.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"Interest", 
					_interest.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_interest.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR),
					_interest.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_interest.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"Insurance", 
					_insurance.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_insurance.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR),
					_insurance.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_insurance.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"DOC Capital", 
					_capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR),
					_capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*100	
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] { });
		}
		
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)) {
			
			//--------------------------------------------------------------------------------
			// CREW DOC:
			//--------------------------------------------------------------------------------
			dataListDOC.add(new Object[] {
					"Cockpit Crew", 
					_cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR),
					_cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"Cabin Crew", 
					_cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR),
					_cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"DOC Crew", 
					_crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR),
					_crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR),
					_crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*100
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] { });
		}
		
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)) {
			
			//--------------------------------------------------------------------------------
			// FUEL DOC:
			//--------------------------------------------------------------------------------
			dataListDOC.add(new Object[] {
					"DOC Fuel", 
					_fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue(),
					_fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_blockTime.doubleValue(NonSI.HOUR),
					_fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] { });
		}
		
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)) {
			
			//--------------------------------------------------------------------------------
			// CHARGES DOC:
			//--------------------------------------------------------------------------------
			dataListDOC.add(new Object[] {
					"Landing charges", 
					_landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT),
					_landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
					_landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"Navigation charges", 
					_navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT),
					_navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
					_navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
						
			});
			dataListDOC.add(new Object[] {
					"Ground handling charges", 
					_groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT),
					_groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
					_groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			
			if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) {
			
				dataListDOC.add(new Object[] {
						"Noise charges", 
						_noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT),
						_noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
						_noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
						_noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
				});
				dataListDOC.add(new Object[] {
						"Emissions charges", 
						_emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT),
						_emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
						_emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
						_emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
				});
				
			}
			dataListDOC.add(new Object[] {
					"DOC Charges", 
					_chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT),
					_chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR),
					_chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] { });
		}
		
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)) {
			
			//--------------------------------------------------------------------------------
			// MAINTENANCE DOC:
			//--------------------------------------------------------------------------------
			dataListDOC.add(new Object[] {
					"Airframe Maintenance Charges", 
					_airframeMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_airframeMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
							/_blockTime.doubleValue(NonSI.HOUR),
					_airframeMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE),
					_airframeMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*100		
							/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"Engine Maintenance Charges", 
					_engineMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_engineMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
							/_blockTime.doubleValue(NonSI.HOUR),
					_engineMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE),
					_engineMaintenanceCharges.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*100		
							/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] {
					"DOC Maintenance", 
					_maintenanceChargesDOC.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
					_maintenanceChargesDOC.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
							/_blockTime.doubleValue(NonSI.HOUR),
					_maintenanceChargesDOC.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE),
					_maintenanceChargesDOC.get(
							_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
							*100		
							/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
			});
			dataListDOC.add(new Object[] { });
		}
		
		Double totalDOC = 0.0;
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL))
			totalDOC += _capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW))
			totalDOC += _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL))
			totalDOC += _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue();
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES))
			totalDOC += _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE))
			totalDOC += _maintenanceChargesDOC.get(
					_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
					*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE);
		
		
		dataListDOC.add(new Object[] { });
		dataListDOC.add(new Object[] {
				"Total DOC", 
				totalDOC,
				totalDOC/_blockTime.doubleValue(NonSI.HOUR),
				totalDOC/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
				totalDOC
					/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
					*100
					/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
		});
		
		Double cashDOC = 0.0;
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW))
			cashDOC += _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL))
			cashDOC += _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue();
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES))
			cashDOC += _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE))
			cashDOC += _maintenanceChargesDOC.get(
					_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
					*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE);
		
		
		dataListDOC.add(new Object[] { });
		dataListDOC.add(new Object[] {
				"Cash DOC", 
				cashDOC,
				cashDOC/_blockTime.doubleValue(NonSI.HOUR),
				cashDOC/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE),
				cashDOC
					/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
					*100
					/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax()
		});
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 20);
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleHead.setFont(font);
		
		Row row = sheetDOC.createRow(0);
		Object[] objArr = dataListDOC.get(0);
		int cellnum = 0;
		for (Object obj : objArr) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellStyle(styleHead);
			if (obj instanceof Date) {
				cell.setCellValue((Date) obj);
			} else if (obj instanceof Boolean) {
				cell.setCellValue((Boolean) obj);
			} else if (obj instanceof String) {
				cell.setCellValue((String) obj);
			} else if (obj instanceof Double) {
				cell.setCellValue((Double) obj);
			}
			sheetDOC.setDefaultColumnWidth(35);
			sheetDOC.setColumnWidth(1, 2048);
			sheetDOC.setColumnWidth(2, 3840);
		}

		int rownum = 1;
		for (int i = 1; i < dataListDOC.size(); i++) {
			objArr = dataListDOC.get(i);
			row = sheetDOC.createRow(rownum++);
			cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}
		}
			
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		Double totalDOC = 0.0;
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL))
			totalDOC += _capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW))
			totalDOC += _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL))
			totalDOC += _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue();
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES))
			totalDOC += _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE))
			totalDOC += _maintenanceChargesDOC.get(
					_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
					*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE);
		
		Double cashDOC = 0.0;
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW))
			cashDOC += _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
					*_blockTime.doubleValue(NonSI.HOUR);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL))
			cashDOC += _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue();
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES))
			cashDOC += _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT);
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE))
			cashDOC += _maintenanceChargesDOC.get(
					_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
					*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE);
		
		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tCosts Analysis\n")
				.append("\t-------------------------------------\n");
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)) {
				sb.append("\tCapital DOC\n")
				.append("\t\t$/flight\n")
				.append("\t\t\tDepreciation: " + _depreciation.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tInterest: " + _interest.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tInsurance: " + _insurance.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tDOC: " + _capitalDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t$/h\n")
				.append("\t\t\tDepreciation: " + _depreciation.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t\tInterest: " + _interest.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t\tInsurance: " + _insurance.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t\tDOC: " + _capitalDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t$/nmi\n")
				.append("\t\t\tDepreciation: " + _depreciation.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tInterest: " + _interest.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tInsurance: " + _insurance.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tDOC: " + _capitalDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tcent/(nmi*seat)\n")
				.append("\t\t\tDepreciation: " + _depreciation.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tInterest: " + _interest.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*100
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tInsurance: " + _insurance.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tDOC: " + _capitalDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t.....................................\n");
		}
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)) {
				sb.append("\tCrew DOC\n")
				.append("\t\t$/flight\n")
				.append("\t\t\tCockpit Crew: " + _cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tCabin Crew: " + _cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tDOC: " + _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t$/h\n")
				.append("\t\t\tCockpit Crew: " + _cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t\tCabin Crew: " + _cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t\tDOC: " + _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR) + "\n")
				.append("\t\t$/nmi\n")
				.append("\t\t\tCockpit Crew: " + _cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tCabin Crew: " + _cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tDOC: " + _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t¢/(nmi*seat)\n")
				.append("\t\t\tCockpit Crew: " + _cockpitCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*_blockTime.doubleValue(NonSI.HOUR) 
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tCabin Crew: " + _cabinCrewCost.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*100
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tDOC: " + _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
						*100
						*_blockTime.doubleValue(NonSI.HOUR)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t.....................................\n");
		}
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)) {
				sb.append("\tFuel DOC\n")
				.append("\t\t$/flight\n")
				.append("\t\t\t" + _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue() + "\n")
				.append("\t\t$/h\n")
				.append("\t\t\t" + _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t$/nmi\n")
				.append("\t\t\t" + _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t¢/(nmi*seat)\n")
				.append("\t\t\t" + _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue()
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t.....................................\n");
		}
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)) {
				sb.append("\tCharges DOC\n")
				.append("\t\t$/flight\n")
				.append("\t\t\tLanding charges: " + _landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT) + "\n")
				.append("\t\t\tNavigation charges: " + _navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT) + "\n")
				.append("\t\t\tGround handling charges: " + _groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT) + "\n");
				
				if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) {
					sb.append("\t\t\tNoise charges: " + _noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT) + "\n")
					.append("\t\t\tEmissions charges: " + _emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT) + "\n");
				}
				
				sb.append("\t\t\tDOC: " + _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT) + "  $/flight\n")
				.append("\t\t$/h\n")
				.append("\t\t\tLanding charges: " + _landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tNavigation charges: " + _navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tGround handling charges: " + _groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
						/_blockTime.doubleValue(NonSI.HOUR) + "\n");
				
				if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) {
					sb.append("\t\t\tNoise charges: " + _noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
					/_blockTime.doubleValue(NonSI.HOUR) + "\n")
					.append("\t\t\tEmissions charges: " + _emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
					/_blockTime.doubleValue(NonSI.HOUR) + "\n");
				}
				
				sb.append("\t\t\tDOC: " + _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t$/nmi\n")
				.append("\t\t\tLanding charges: " + _landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tNavigation charges: " + _navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tGround handling charges: " + _groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n");
				
				if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) {
					sb.append("\t\t\tNoise charges: " + _noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
					/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
					.append("\t\t\tEmissions charges: " + _emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
					/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n");
				}
				
				sb.append("\t\t\tDOC: " + _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t¢/(nmi*seat)\n")
				.append("\t\t\tLanding charges: " + _landingCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tNavigation charges: " + _navigationCharges.doubleValue(MyUnits.USD_PER_FLIGHT)
						*100		
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tGround handling charges: " + _groundHandlingCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
						*100		
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n");
				
				if(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES).equals(MethodEnum.ILR_AACHEN)) {
					sb.append("\t\t\tNoise charges: " + _noiseCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
						*100	
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
					.append("\t\t\tEmissions charges: " + _emissionsCharges.doubleValue(MyUnits.USD_PER_FLIGHT) 
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n");
				}
				
				sb.append("\t\t\tDOC: " + _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT)
						*100
						/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) 
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t.....................................\n");
		}
		if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)) {
				sb.append("\tMaintenance DOC\n")
				.append("\t\t$/flight\n")
				.append("\t\t\tAirframe Maintenance Charges: " + _airframeMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tEngine Maintenance Charges: " + _engineMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE) 
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t\tDOC: " + _maintenanceChargesDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\t$/h\n")
				.append("\t\t\tAirframe Maintenance Charges: " + _airframeMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tEngine Maintenance Charges: " + _engineMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE) 
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t\tDOC: " + _maintenanceChargesDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
						/_blockTime.doubleValue(NonSI.HOUR) + "\n")
				.append("\t\t$/nmi\n")
				.append("\t\t\tAirframe Maintenance Charges: " + _airframeMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE) + "\n")
				.append("\t\t\tEngine Maintenance Charges: " + _engineMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE) + "\n")
				.append("\t\t\tDOC: " + _maintenanceChargesDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE) + "\n")
				.append("\t\t¢/(nmi*seat)\n")
				.append("\t\t\tAirframe Maintenance Charges: " + _airframeMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tEngine Maintenance Charges: " + _engineMaintenanceCharges.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t\t\tDOC: " + _maintenanceChargesDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
						*100
						/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
				.append("\t.....................................\n");
		}
		
		sb.append("\tTotal DOC\n")
		.append("\t\t$/flight\n")
		.append("\t\t\t"+ totalDOC + "\n")
		.append("\t\t$/h\n")
		.append("\t\t\t" + totalDOC/_blockTime.doubleValue(NonSI.HOUR) + "\n")
		.append("\t\t$/nmi\n")
		.append("\t\t\t" + totalDOC/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
		.append("\t\t¢/(nmi*seat)\n")
		.append("\t\t\t" + totalDOC
				/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
				*100
				/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
		.append("\t.....................................\n")
		.append("\tCash DOC\n")
		.append("\t\t$/flight\n")
		.append("\t\t\t"+ cashDOC + "\n")
		.append("\t\t$/h\n")
		.append("\t\t\t" + cashDOC/_blockTime.doubleValue(NonSI.HOUR) + "\n")
		.append("\t\t$/nmi\n")
		.append("\t\t\t" + cashDOC/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE) + "\n")
		.append("\t\t¢/(nmi*seat)\n")
		.append("\t\t\t" + cashDOC
				/_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE)
				*100
				/_theCostsBuilderInterface.getAircraft().getCabinConfiguration().getMaxPax() + "\n")
		.append("\t.....................................\n");
		
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

			_landingCharges = _theCostsBuilderInterface.getLandingCharges();
			
			_navigationCharges = _theCostsBuilderInterface.getNavigationCharges();
			
			_groundHandlingCharges = _theCostsBuilderInterface.getGroundHandlingCharges();
			
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

			_landingCharges = _theCostsBuilderInterface.getLandingCharges();
			
			_navigationCharges = _theCostsBuilderInterface.getNavigationCharges();
			
			_groundHandlingCharges = _theCostsBuilderInterface.getGroundHandlingCharges();
			
			_noiseCharges = _theCostsBuilderInterface.getNoiseCharges();
			
			_emissionsCharges=
					_theCostsBuilderInterface.getEmissionsChargesHC().plus(
							_theCostsBuilderInterface.getEmissionsChargesCO2()).plus(
									_theCostsBuilderInterface.getEmissionsChargesCO()).plus(
											_theCostsBuilderInterface.getEmissionsChargesNOx());

			_chargesDOC.put(
					MethodEnum.ILR_AACHEN,
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

			// TODO: ONLY IF THERE IS A PERFORMANCE MANAGER

		}
		
		public void plotDocVsBlockTime() {

			// TODO: ONLY IF THERE IS A PERFORMANCE MANAGER

		}
		
		public void plotDocVsBlockFuel() {

			// TODO: ONLY IF THERE IS A PERFORMANCE MANAGER

		}
		
		public void plotProfitability() {

			// TODO: ONLY IF THERE IS A PERFORMANCE MANAGER

		}
		
		@SuppressWarnings("unchecked")
		public void plotDocPieChart(String costsFolderPath) {

			List<String> labels = new ArrayList<>();
			List<Double> values = new ArrayList<>();
			
			Double capitalDOC = 0.0;
			Double crewDOC = 0.0;
			Double fuelDOC = 0.0;
			Double chargesDOC = 0.0;
			Double maintenanceDOC = 0.0;
			Double cashDOC = 0.0;
			
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)) {
				labels.add("DOC Capital");
				 capitalDOC = _capitalDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CAPITAL)).doubleValue(MyUnits.USD_PER_HOUR)
				*_blockTime.doubleValue(NonSI.HOUR);
				 cashDOC += capitalDOC;
			}
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)) {
				labels.add("DOC Crew");
				crewDOC = _crewDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CREW)).doubleValue(MyUnits.USD_PER_HOUR)
				*_blockTime.doubleValue(NonSI.HOUR);
				cashDOC += crewDOC;
			}
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)) {
				labels.add("DOC Fuel");
				fuelDOC = _fuelDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_FUEL)).getEstimatedValue();
				cashDOC += fuelDOC;
			}
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)) {
				labels.add("DOC Charges");
				chargesDOC = _chargesDOC.get(_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_CHARGES)).doubleValue(MyUnits.USD_PER_FLIGHT);
				cashDOC += chargesDOC;
			}
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)) {
				labels.add("DOC Maintenance");
				maintenanceDOC = _maintenanceChargesDOC.get(
						_theCostsBuilderInterface.getTaskList().get(CostsEnum.DOC_MAINTENANCE)).doubleValue(MyUnits.USD_PER_NAUTICAL_MILE)
				*_theCostsBuilderInterface.getRange().doubleValue(NonSI.NAUTICAL_MILE);
				cashDOC += maintenanceDOC;
			}
			
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CAPITAL)) 
				values.add(capitalDOC/cashDOC*100.0);
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CREW)) 
				values.add(crewDOC/cashDOC*100.0);
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_FUEL)) 
				values.add(fuelDOC/cashDOC*100.0);
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_CHARGES)) 
				values.add(chargesDOC/cashDOC*100.0);
			if(_theCostsBuilderInterface.getTaskList().containsKey(CostsEnum.DOC_MAINTENANCE)) 
				values.add(maintenanceDOC/cashDOC*100.0);
			
			MyChartToFileUtils.plotPieChart(
					labels, 
					values, 
					"DOC Breakdown", 
					false, 
					costsFolderPath, 
					"DOC_Breakdown"
					);
			
		}
		
	}
	//............................................................................
	// END PLOT INNER CLASS
	//............................................................................

	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................

	public IACCostsManager getTheCostsBuilderInterface() {
		return _theCostsBuilderInterface;
	}
	public void setTheCostsBuilderInterface(IACCostsManager _theCostsBuilderInterface) {
		this._theCostsBuilderInterface = _theCostsBuilderInterface;
	}
	public Amount<Money> getTotalInvestment() {
		return _totalInvestment;
	}
	public void setTotalInvestment(Amount<Money> _totalInvestment) {
		this._totalInvestment = _totalInvestment;
	}
	public Amount<Money> getAirframeCost() {
		return _airframeCost;
	}
	public void setAirframeCost(Amount<Money> _airframeCost) {
		this._airframeCost = _airframeCost;
	}
	public Map<MethodEnum, Amount<?>> getCockpitCrewCost() {
		return _cockpitCrewCost;
	}
	public void setCockpitCrewCost(Map<MethodEnum, Amount<?>> _cockpitCrewCost) {
		this._cockpitCrewCost = _cockpitCrewCost;
	}
	public Map<MethodEnum, Amount<?>> getCabinCrewCost() {
		return _cabinCrewCost;
	}
	public void setCabinCrewCost(Map<MethodEnum, Amount<?>> _cabinCrewCost) {
		this._cabinCrewCost = _cabinCrewCost;
	}
	public Map<MethodEnum, Amount<?>> getLabourAirframeMaintenanceCharges() {
		return _labourAirframeMaintenanceCharges;
	}
	public void setLabourAirframeMaintenanceCharges(Map<MethodEnum, Amount<?>> _labourAirframeMaintenanceCharges) {
		this._labourAirframeMaintenanceCharges = _labourAirframeMaintenanceCharges;
	}
	public Map<MethodEnum, Amount<?>> getMaterialAirframeMaintenanceCharges() {
		return _materialAirframeMaintenanceCharges;
	}
	public void setMaterialAirframeMaintenanceCharges(Map<MethodEnum, Amount<?>> _materialAirframeMaintenanceCharges) {
		this._materialAirframeMaintenanceCharges = _materialAirframeMaintenanceCharges;
	}
	public Map<MethodEnum, Amount<?>> getLabourEngineMaintenanceCharges() {
		return _labourEngineMaintenanceCharges;
	}
	public void setLabourEngineMaintenanceCharges(Map<MethodEnum, Amount<?>> _labourEngineMaintenanceCharges) {
		this._labourEngineMaintenanceCharges = _labourEngineMaintenanceCharges;
	}
	public Map<MethodEnum, Amount<?>> getMaterialEngineMaintenanceCharges() {
		return _materialEngineMaintenanceCharges;
	}
	public void setMaterialEngineMaintenanceCharges(Map<MethodEnum, Amount<?>> _materialEngineMaintenanceCharges) {
		this._materialEngineMaintenanceCharges = _materialEngineMaintenanceCharges;
	}
	public Amount<Mass> getAirframeMass() {
		return _airframeMass;
	}
	public void setAirframeMass(Amount<Mass> _airframeMass) {
		this._airframeMass = _airframeMass;
	}
	public Amount<Duration> getBlockTime() {
		return _blockTime;
	}
	public void setBlockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}
	public Map<MethodEnum, Amount<?>> getDepreciation() {
		return _depreciation;
	}
	public void setDepreciation(Map<MethodEnum, Amount<?>> _depreciation) {
		this._depreciation = _depreciation;
	}
	public Map<MethodEnum, Amount<?>> getInterest() {
		return _interest;
	}
	public void setInterest(Map<MethodEnum, Amount<?>> _interest) {
		this._interest = _interest;
	}
	public Map<MethodEnum, Amount<?>> getInsurance() {
		return _insurance;
	}
	public void setInsurance(Map<MethodEnum, Amount<?>> _insurance) {
		this._insurance = _insurance;
	}
	public Map<MethodEnum, Amount<?>> getCapitalDOC() {
		return _capitalDOC;
	}
	public void setCapitalDOC(Map<MethodEnum, Amount<?>> _capitalDOC) {
		this._capitalDOC = _capitalDOC;
	}
	public Map<MethodEnum, Amount<?>> getCrewDOC() {
		return _crewDOC;
	}
	public void setCrewDOC(Map<MethodEnum, Amount<?>> _crewDOC) {
		this._crewDOC = _crewDOC;
	}
	public Map<MethodEnum, Amount<?>> getFuelDOC() {
		return _fuelDOC;
	}
	public void setFuelDOC(Map<MethodEnum, Amount<?>> _fuelDOC) {
		this._fuelDOC = _fuelDOC;
	}
	public Amount<?> getLandingCharges() {
		return _landingCharges;
	}
	public void setLandingCharges(Amount<?> _landingCharges) {
		this._landingCharges = _landingCharges;
	}
	public Amount<?> getNavigationCharges() {
		return _navigationCharges;
	}
	public void setNavigationCharges(Amount<?> _navigationCharges) {
		this._navigationCharges = _navigationCharges;
	}
	public Amount<?> getGroundHandlingCharges() {
		return _groundHandlingCharges;
	}
	public void setGroundHandlingCharges(Amount<?> _groundHandlingCharges) {
		this._groundHandlingCharges = _groundHandlingCharges;
	}
	public Amount<?> getEmissionsCharges() {
		return _emissionsCharges;
	}
	public void setEmissionsCharges(Amount<?> _emissionsCharges) {
		this._emissionsCharges = _emissionsCharges;
	}
	public Map<MethodEnum, Amount<?>> getChargesDOC() {
		return _chargesDOC;
	}
	public void setChargesDOC(Map<MethodEnum, Amount<?>> _chargesDOC) {
		this._chargesDOC = _chargesDOC;
	}
	public Map<MethodEnum, Amount<?>> getAirframeMaintenanceCharges() {
		return _airframeMaintenanceCharges;
	}
	public void setAirframeMaintenanceCharges(Map<MethodEnum, Amount<?>> _airframeMaintenanceCharges) {
		this._airframeMaintenanceCharges = _airframeMaintenanceCharges;
	}
	public Map<MethodEnum, Amount<?>> getEngineMaintenanceCharges() {
		return _engineMaintenanceCharges;
	}
	public void setEngineMaintenanceCharges(Map<MethodEnum, Amount<?>> _engineMaintenanceCharges) {
		this._engineMaintenanceCharges = _engineMaintenanceCharges;
	}
	public Map<MethodEnum, Amount<?>> getMaintenanceChargesDOC() {
		return _maintenanceChargesDOC;
	}
	public void setMaintenanceChargesDOC(Map<MethodEnum, Amount<?>> _maintenanceChargesDOC) {
		this._maintenanceChargesDOC = _maintenanceChargesDOC;
	}
	public Amount<?> getNoiseCharges() {
		return _noiseCharges;
	}
	public void setNoiseCharges(Amount<?> _noiseCharges) {
		this._noiseCharges = _noiseCharges;
	}
}