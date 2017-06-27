package analyses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.costs.CostsCalcUtils;
import configuration.MyConfiguration;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.CostsDerivedDataEnum;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.CostsPlotEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyUnits;

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
	private Map<MethodEnum, Amount<?>> _emissionsChargesNOx;
	private Map<MethodEnum, Amount<?>> _emissionsChargesCO;
	private Map<MethodEnum, Amount<?>> _emissionsChargesCO2;
	private Map<MethodEnum, Amount<?>> _emissionsChargesHC;
	private Map<MethodEnum, Amount<?>> _maintenanceCharges;
	private Map<MethodEnum, Amount<?>> _labourAirframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _materialAirframeMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _labourEngineMaintenanceCharges;
	private Map<MethodEnum, Amount<?>> _materialEngineMaintenanceCharges;
	private Amount<Mass> _airframeMass;
	private Amount<Duration> _blockTime;
	
	private Amount<?> _airframMaterialCost;
	private Amount<Money> _airframeMaterialCostPerFlightCycle;
	
	private Double  _airframeLabourManHourPerFlightHour;
	private Amount<Duration>  _airframeLabourManHourPerCycle; 
	
	private Amount<?> _engineMaterialCost;
	private Amount<Money> _engineMaterialCostPerFlightCycle;
	
	private Double  _engineLabourManHourPerFlightHour;
	private Amount<Duration>  _engineLabourManHourPerCycle; 
	//..............................................................................
	// OUTPUT
	private Map<MethodEnum, Amount<?>> _depreciation;
	private Map<MethodEnum, Amount<?>> _interest;
	private Map<MethodEnum, Amount<?>> _insurance;
	private Map<MethodEnum, Amount<?>> _capitalDOC;
	private Map<MethodEnum, Amount<?>> _cockpitCrewCost;
	private Map<MethodEnum, Amount<?>> _cabinCrewCost;
	private Map<MethodEnum, Amount<?>> _crewDOC;
	private Map<MethodEnum, Amount<?>> _fuelDOC;
	private Map<MethodEnum, Amount<?>> _landingCharges;
	private Map<MethodEnum, Amount<?>> _navigationCharges;
	private Map<MethodEnum, Amount<?>> _groundHandilingCharges;
	private Map<MethodEnum, Amount<?>> _noiseCharges;
	private Map<MethodEnum, Amount<?>> _emissionsCharges;
	private Map<MethodEnum, Amount<?>> _chargesDOC;
	private Map<MethodEnum, Amount<?>> _groundHandlingCharges;
	
	private Map<MethodEnum, Amount<?>> _labourAirframeManHoursPerFlightCycle;
	private Map<MethodEnum, Amount<?>> _labourAirframeManHoursPerFlightHour;

	// TODO: all derived data are maps
	// Only six items of DOC (Capital, etc)

	
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	public static IACCostsManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions
			) throws IOException {
		
		// TODO : FILL ME !!
		
		return null;
		
	}
	
	private void initializeAnalysis() {
		
		initializeData();
		initializeArrays();
		
	}
	
	private void initializeArrays() {
		// TODO Define all the derived data
		
	}

	private void initializeData() {
		// TODO Initialize all the arrays (eventually)
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
							_theCostsBuilderInterface.getInsuranceValue()
							)
					);
			
			_interest.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcInterestAEA(
							_totalInvestment,
							_theCostsBuilderInterface.getUtilization().get(_theCostsBuilderInterface.getDerivedDataMethodMap().get(CostsDerivedDataEnum.UTILIZATION)), 
							_theCostsBuilderInterface.getInterestValue()
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

		public void calculateFuelDOC() {
			
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

		public void calculateChargesDOC() {

			
			_landingCharges.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCLandingCharges(
							_landingChargeConstant,
							_theCostsBuilderInterface.getMaximumTakeOffMass())
					);
			
			_navigationCharges.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCNavigationCharges(
							_navigationChargeConstant,
							_theCostsBuilderInterface.getRange(),
							_theCostsBuilderInterface.getMaximumTakeOffMass())
					);
			
			_groundHandlingCharges.put(
					MethodEnum.AEA, 
					CostsCalcUtils.calcDOCGroundHandlingCharges(
							_groundHandlingChargeConstant,
							_theCostsBuilderInterface.getPayload())
					);
			
			_noiseCharges.put(
					MethodEnum.TNAC, 
					CostsCalcUtils.calcDOCNoiseCharges(
							_theCostsBuilderInterface.getApproachCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getLateralCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getFlyoverCertifiedNoiseLevel(),
							_theCostsBuilderInterface.getNoiseConstant(),
							_theCostsBuilderInterface.getNoiseDepartureThreshold(),
							_theCostsBuilderInterface.getNoiseArrivalThreshold())
					);
			
			// - start EMISSIONS
			_emissionsChargesNOx.put(
					MethodEnum.TNAC, 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantNOx(),
							_theCostsBuilderInterface.getMassNOx(),
							_theCostsBuilderInterface.getDpHCFooNOx(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
			);
			
			_emissionsChargesCO.put(
					MethodEnum.TNAC, 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO(),
							_theCostsBuilderInterface.getMassCO(),
							_theCostsBuilderInterface.getDpHCFooCO(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
			);
			
			_emissionsChargesCO2.put(
					MethodEnum.TNAC, 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantCO2(),
							_theCostsBuilderInterface.getMassCO2(),
							_theCostsBuilderInterface.getDpHCFooCO2(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
			);
			
			_emissionsChargesHC.put(
					MethodEnum.TNAC, 
					CostsCalcUtils.calcDOCEmissionsCharges(
							_theCostsBuilderInterface.getEmissionsConstantHC(),
							_theCostsBuilderInterface.getMassHC(),
							_theCostsBuilderInterface.getDpHCFooHC(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineType(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getP0Total(),
							_theCostsBuilderInterface.getAircraft().getPowerPlant().getEngineNumber())
			);
			
			_emissionsCharges.put(
					MethodEnum.TNAC,
					_emissionsChargesHC.get(MethodEnum.AEA).plus(
							_emissionsChargesCO2.get(MethodEnum.AEA)).plus(
									_emissionsChargesCO.get(MethodEnum.AEA)).plus(
												_emissionsChargesNOx.get(MethodEnum.AEA))
					);
			// -end EMISSIONS 
			
			
			
			_chargesDOC.put(
					MethodEnum.AEA,
					CostsCalcUtils.calcDOCCharges(
							_landingCharges.get(MethodEnum.AEA),
							_navigationCharges.get(MethodEnum.AEA),
							_groundHandilingCharges.get(MethodEnum.AEA),
							_noiseCharges.get(MethodEnum.AEA),
							_emissionsCharges.get(MethodEnum.AEA)
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

		public void calculateMaintenanceDOC() {

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
			
			// TOTAL MAINTENANCE COST ($/nm)
			_maintenanceCharges.put(
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
