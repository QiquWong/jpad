package it.unina.daf.test.Costs;

import java.util.List;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.costs.MyCosts;
import aircraft.components.Aircraft;
import standaloneutils.database.io.DatabaseFileReader;
import standaloneutils.database.io.DatabaseFileWriter;
import standaloneutils.database.io.DatabaseIOmanager;
import configuration.MyConfiguration;
import configuration.enumerations.CostsEnumOLD;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.FoldersEnum;

public class CostsCalc {
	
	/**
	 * @author Vincenzo Cusati
	 * @return  
	 */
	
	public static DatabaseIOmanager<CostsEnumOLD> initializeInputTree() {

		DatabaseIOmanager<CostsEnumOLD> ioManager = new DatabaseIOmanager<CostsEnumOLD>();
		
		// -------------------- Input --------------------
		// Fixed Charges
		ioManager.addElement(CostsEnumOLD.Residual_Value, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Total_Investiment, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Depreciation_period, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Interest, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Insurance, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.num_cabin_crew_members, Amount.valueOf(0., Unit.ONE), "");
		// Trip Charges
		ioManager.addElement(CostsEnumOLD.MTOW, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Block_Time, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Range, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Payload, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Manufacturer_Empty_Weight, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Labor_Manhour_Rate, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Price, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.OAPR, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.T0, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.BPR, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.num_compressor_stages, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.K, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Block_Fuel, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.Fuel_costs, Amount.valueOf(0., Unit.ONE), "");

		return ioManager;
	}
	
	/**
	 * 
	 * @param totTripCharges
	 * @param totFixedCharges
	 * @param dOC
	 * @return
	 */
	public static DatabaseIOmanager<CostsEnumOLD> initializeOutputTree(double totTripCharges,
			double totFixedCharges, double dOC) {

		DatabaseIOmanager<CostsEnumOLD> ioManager = new DatabaseIOmanager<CostsEnumOLD>();

		
		ioManager.addElement(CostsEnumOLD.total_TripCharges, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.total_FixedCharges, Amount.valueOf(0., Unit.ONE), "");
		ioManager.addElement(CostsEnumOLD.DOC, Amount.valueOf(0., Unit.ONE), "");

		return ioManager;  
	} 
	
	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @return
	 */
	public static DatabaseIOmanager<CostsEnumOLD> readFromFile(String filenamewithPathAndExt) {

		DatabaseIOmanager<CostsEnumOLD> inputManager = initializeInputTree();

		DatabaseFileReader<CostsEnumOLD> _costsEnumFileReader = 
				new DatabaseFileReader<CostsEnumOLD>(
						filenamewithPathAndExt, inputManager.getTagList());

		// System.out.println("--> File: " + filenamewithPathAndExt);

		List<Amount> valueList = _costsEnumFileReader.readDatabase();
		inputManager.setValueList(valueList);

		return inputManager;
	}
	
	/**
	 * 
	 * @param filenamewithPathAndExt
	 * @param inputManager
	 * @param outputManager
	 */
	public static void writeToFile(
			String filenamewithPathAndExt,
			DatabaseIOmanager<DirStabEnum> inputManager,
			DatabaseIOmanager<DirStabEnum> outputManager) {

		DatabaseFileWriter<DirStabEnum> databaseWriter = new DatabaseFileWriter<DirStabEnum>(
				"Costs", // This string is that written in the <rootElement> in the output xml file 
				filenamewithPathAndExt, inputManager, outputManager);

		databaseWriter.writeDocument();
	}

	
	public static void executeStandaloneCosts(String inputFileNameWithPathAndExt, 
			String outputFileNameWithPathAndExt) {

		DatabaseIOmanager<CostsEnumOLD> inputManager = readFromFile(inputFileNameWithPathAndExt); 
		
		Aircraft aircraft = Aircraft.createDefaultAircraft("ATR-72"); 
		OperatingConditions operatingConditions = new OperatingConditions();
//		TODO: put Altitude and TAS (or Mach) in the input
//		operatingConditions.set_altitude(inputManager.getValue(CostsEnum.Altitude));//Amount.valueOf(11000, SI.METER)
//		operatingConditions.set_tas(inputManager.getValue(CostsEnum.TAS));// Amount.valueOf(473, NonSI.KNOT)
		
		Amount<Mass> OEM = inputManager.getValue(CostsEnumOLD.Manufacturer_Empty_Weight);
		Amount<Mass> MTOM = inputManager.getValue(CostsEnumOLD.MTOW);
		aircraft.get_weights().set_OEM(OEM);
		aircraft.get_weights().set_MTOM(MTOM);
		aircraft.get_weights().set_manufacturerEmptyMass(OEM);
		
		MyCosts theCost = new MyCosts(aircraft);
		
	}
	
	
	
	
//	MyCosts theCost = new MyCosts(aircraft);
//	aircraft.set_lifeSpan(16);
//	theCost.set_annualInterestRate(0.054);
//	CostsCalcUtils.calcAircraftCostSforza(OEM);
////	theCost.calcAircraftCostSforza();
//	Amount<Duration> flightTime = Amount.valueOf(15.22, NonSI.HOUR);
////	Amount<Velocity> blockSpeed = Amount.valueOf(243.0, SI.METERS_PER_SECOND); // Value according to Sforza
//	theCost.set_flightTime(flightTime);
////	theCost.set_manHourLaborRate(40); // Value according to Sforza
////	theCost.set_blockSpeed(blockSpeed);// Value according to Sforza
////	theCost.calcUtilizationKundu(theCost.get_blockTime().doubleValue(NonSI.HOUR));
//	theCost.set_utilization(4750);
////	theCost.calcTotalInvestments(98400000.0, 9800000.0, 2, 0.1, 0.3);
////	theCost.get_theFixedCharges().set_residualValue(0.2);
//	
//	aircraft.get_powerPlant().set_engineType(EngineTypeEnum.TURBOFAN);
//	
////	Amount<Duration> tb = theCost.calcBlockTime();
////	theCost.set_blockTime(Amount.valueOf(15.94, NonSI.HOUR));;
//	
//	theCost.calculateAll(aircraft);
//	
//	Map<MethodEnum, Double> depreciationMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> interestMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> insuranceMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> crewCostsMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> totalFixedChargesMap = 
//			new TreeMap<MethodEnum, Double>();
//
//	Map<MethodEnum, Double> landingFeesMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> navigationalChargesMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> groundHandlingChargesMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> maintenanceMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> fuelAndOilMap = 
//			new TreeMap<MethodEnum, Double>();
//	Map<MethodEnum, Double> totalTripChargesMap = 
//			new TreeMap<MethodEnum, Double>();
//	
//	depreciationMap = theCost.get_theFixedCharges().get_calcDepreciation().get_methodsMap();
//	interestMap = theCost.get_theFixedCharges().get_calcInterest().get_methodsMap();
//	insuranceMap = theCost.get_theFixedCharges().get_calcInsurance().get_methodsMap();
//	crewCostsMap = theCost.get_theFixedCharges().get_calcCrewCosts().get_methodsMap();
//	totalFixedChargesMap = theCost.get_theFixedCharges().get_totalFixedChargesMap();
//	
//	landingFeesMap = theCost.get_theTripCharges().get_calcLandingFees().get_methodsMap();
//	navigationalChargesMap = theCost.get_theTripCharges().get_calcNavigationalCharges().get_methodsMap();
//	groundHandlingChargesMap = theCost.get_theTripCharges().get_calcGroundHandlingCharges().
//			get_methodsMap();
//	maintenanceMap = theCost.get_theTripCharges().get_calcMaintenanceCosts().get_methodsMap();
//	fuelAndOilMap = theCost.get_theTripCharges().get_calcFuelAndOilCharges().get_methodsMap();
//	totalTripChargesMap = theCost.get_theTripCharges().get_totalTripChargesMap();
//
//	
//	System.out.println("The aircraft total investment is " +  theCost.get_totalInvestments());
////	System.out.println("The aircraft depreciation per block hour is " + depreciation  );
////	System.out.println("The residual value rate is " + theFixedCharges.get_residualValue() );
//	System.out.println("The test depreciation methodMap is " + depreciationMap );
//	System.out.println("The test interest methodMap is " + interestMap );
//	System.out.println("The test insurance methodMap is " + insuranceMap );
//	System.out.println("The test crew cost methodMap is " + crewCostsMap );
//	System.out.println("The test total fixed charges methodMap is " + totalFixedChargesMap );
//	System.out.println();
//	
//	System.out.println("The test landing fees methodMap is " + landingFeesMap );
//	System.out.println("The test navigational charges methodMap is " + navigationalChargesMap );
//	System.out.println("The test ground handling charges methodMap is " + groundHandlingChargesMap );
//	System.out.println("The test maintenance methodMap is " + maintenanceMap );
//	System.out.println("The test fuel and oil methodMap is " + fuelAndOilMap );
//	System.out.println("The test total trip charges methodMap is " + totalTripChargesMap );		
	

}
