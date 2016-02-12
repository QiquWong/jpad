package sandbox.vc.costs;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.costs.MyCosts;
import aircraft.components.Aircraft;
import calculators.costs.CostsCalcUtils;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;

public class Test_sandboxVC_Costs {
		
		public static void main(String[] args) {
			
			// Initialize Aircraft with default parameters
			Aircraft aircraft = Aircraft.createDefaultAircraft(); //("ATR-72")
					
			OperatingConditions operatingConditions = new OperatingConditions();
			operatingConditions.set_altitude(Amount.valueOf(11000, SI.METER));
			operatingConditions.set_tas(Amount.valueOf(473, NonSI.KNOT));
			Amount<Mass> OEM = Amount.valueOf(141056, SI.KILOGRAM);
			Amount<Mass> MTOM = Amount.valueOf(536164.22, NonSI.POUND);
			aircraft.get_weights().set_OEM(OEM);
			aircraft.get_weights().set_MTOM(MTOM);
			aircraft.get_weights().set_manufacturerEmptyMass(OEM);
			
			MyCosts theCost = new MyCosts(aircraft);
			aircraft.set_lifeSpan(16);
			theCost.set_annualInterestRate(0.054);
			CostsCalcUtils.calcAircraftCostSforza(OEM);
//			theCost.calcAircraftCostSforza();
			Amount<Duration> flightTime = Amount.valueOf(15.22, NonSI.HOUR);
//			Amount<Velocity> blockSpeed = Amount.valueOf(243.0, SI.METERS_PER_SECOND); // Value according to Sforza
			theCost.set_flightTime(flightTime);
//			theCost.set_manHourLaborRate(40); // Value according to Sforza
//			theCost.set_blockSpeed(blockSpeed);// Value according to Sforza
//			theCost.calcUtilizationKundu(theCost.get_blockTime().doubleValue(NonSI.HOUR));
			theCost.set_utilization(4750);
//			theCost.calcTotalInvestments(98400000.0, 9800000.0, 2, 0.1, 0.3);
//			theCost.get_theFixedCharges().set_residualValue(0.2);
			
			aircraft.get_powerPlant().set_engineType(EngineTypeEnum.TURBOFAN);
			
//			Amount<Duration> tb = theCost.calcBlockTime();
//			theCost.set_blockTime(Amount.valueOf(15.94, NonSI.HOUR));;
			
			theCost.calculateAll(aircraft);
			
			Map<MethodEnum, Double> depreciationMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> interestMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> insuranceMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> crewCostsMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> totalFixedChargesMap = 
					new TreeMap<MethodEnum, Double>();

			Map<MethodEnum, Double> landingFeesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> navigationalChargesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> groundHandlingChargesMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> maintenanceMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> fuelAndOilMap = 
					new TreeMap<MethodEnum, Double>();
			Map<MethodEnum, Double> totalTripChargesMap = 
					new TreeMap<MethodEnum, Double>();
			
			depreciationMap = theCost.get_theFixedCharges().get_calcDepreciation().get_methodsMap();
			interestMap = theCost.get_theFixedCharges().get_calcInterest().get_methodsMap();
			insuranceMap = theCost.get_theFixedCharges().get_calcInsurance().get_methodsMap();
			crewCostsMap = theCost.get_theFixedCharges().get_calcCrewCosts().get_methodsMap();
			totalFixedChargesMap = theCost.get_theFixedCharges().get_totalFixedChargesMap();
			
			landingFeesMap = theCost.get_theTripCharges().get_calcLandingFees().get_methodsMap();
			navigationalChargesMap = theCost.get_theTripCharges().get_calcNavigationalCharges().get_methodsMap();
			groundHandlingChargesMap = theCost.get_theTripCharges().get_calcGroundHandlingCharges().
					get_methodsMap();
			maintenanceMap = theCost.get_theTripCharges().get_calcMaintenanceCosts().get_methodsMap();
			fuelAndOilMap = theCost.get_theTripCharges().get_calcFuelAndOilCharges().get_methodsMap();
			totalTripChargesMap = theCost.get_theTripCharges().get_totalTripChargesMap();
			
			// DOC = Fixed + Trip Charge
			Map<MethodEnum, Double> DOC = new HashMap<>(totalFixedChargesMap);
			totalTripChargesMap.forEach((k,v) -> DOC.merge(k,v, Double::sum));
			
			
//			System.out.println("The aircraft total investment is " +  theCost.get_totalInvestments());
////			System.out.println("The aircraft depreciation per block hour is " + depreciation  );
////			System.out.println("The residual value rate is " + theFixedCharges.get_residualValue() );
//			System.out.println("The test depreciation methodMap is " + depreciationMap );
//			System.out.println("The test interest methodMap is " + interestMap );
//			System.out.println("The test insurance methodMap is " + insuranceMap );
//			System.out.println("The test crew cost methodMap is " + crewCostsMap );
//			System.out.println("The test total fixed charges methodMap is " + totalFixedChargesMap );
//			System.out.println();
//			
//			System.out.println("The test landing fees methodMap is " + landingFeesMap );
//			System.out.println("The test navigational charges methodMap is " + navigationalChargesMap );
//			System.out.println("The test ground handling charges methodMap is " + groundHandlingChargesMap );
//			System.out.println("The test maintenance methodMap is " + maintenanceMap );
//			System.out.println("The test fuel and oil methodMap is " + fuelAndOilMap );
//			System.out.println("The test total trip charges methodMap is " + totalTripChargesMap );		
			
//			aircraft.getCost().calculateAll();
			System.out.println("\n The test total Fixed Charge methodMap is " + totalFixedChargesMap);
			System.out.println("\n The test total Trip Charges methodMap is " + totalTripChargesMap);
			System.out.println("\n The test DOC methodMap is " + DOC);
			

		}

	}
