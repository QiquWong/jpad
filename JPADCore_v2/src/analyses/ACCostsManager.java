package analyses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;

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
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyUnits;

public class ACCostsManager {
	
	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW ACCostsManager (WORK IN PROGRESS)
	 * 
	 * @author Vincenzo Cusati & Vittorio Trifari, Agostino De Marco
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

		public void calculateCrewDOC() {


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
