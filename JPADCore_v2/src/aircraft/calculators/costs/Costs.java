package aircraft.calculators.costs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import aircraft.calculators.ACCalculatorManager;
import aircraft.components.Aircraft;
import aircraft.components.Systems;
import aircraft.components.Systems.SystemsBuilder;
import calculators.costs.CostsCalcUtils;
import calculators.performance.PerformanceCalcUtils;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class Costs extends ACCalculatorManager implements ICosts {

	private Aircraft _theAircraft;

	private FixedCharges _theFixedCharges;
	private TripCharges _theTripCharges;

	private double _totalInvestments, _utilization, _residualValue,  _annualInterestRate, 
	_annualInsurancePremiumRate, _singleCabinCrewHrCost, _singleflightCrewHrCost, _groundHandlingCostXPax,
	_manHourLaborRate, _byPassRatio, _overallPressureRatio, _fuelVolumetricCost, _cruiseSpecificFuelConsumption,
	_hourVolumetricFuelConsumption, _oilMassCost;	

	private double _airframeCost, _singleEngineCost, _sparesAirframePerCosts,
	_sparesEnginesPerCosts, _aircraftCost, _engineMaintLaborCost,
	_engineMaintMaterialCost, _airframeMaintLaborCost, _airframeMaintMaterialCost;

	private double _landingFeesPerTon, _jenkinsonNavigationalCharges;

	private Amount<Duration> _blockTime, _flightTime, _groundManoeuvreTime, _cruiseTime, _climbDescentTime,
	_sturtupTaxiTOTime, _holdPriorToLandTime, _landingTaxiToStopTime;

	private Amount<Velocity> _cruiseSpeed;

	private Amount<Mass> _OEM, _MTOM, _payload, _airframeMass;

	private Amount<Volume>  _blockFuelVolume;

	private Amount<Length> _range;

	private Amount<Force> _seaLevelStaticThrust, _thrustTO;

	private int _numberOfEngines, _cabinCrewNumber, _flightCrewNumber, _numberOfPax,
	_numberOfCompressorStage, _numberOfShaft;

	
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class CostsBuilder {

		// required parameters
		private String __id;

		// optional parameters

		private double 	__residualValue,
		__annualInterestRate, 
		__annualInsurancePremiumRate,  
		__utilization, 
		__sparesAirframePerCosts, 
		__sparesEnginesPerCosts,
		__singleCabinCrewHrCost,
		__singleflightCrewHrCost, 
		__landingFeesPerTon,
		__jenkinsonNavigationalCharges,
		__groundHandlingCostXPax,
		__manHourLaborRate, 
		__overallPressureRatio, 
		__engineMaintLaborCost,
		__engineMaintMaterialCost,
		__airframeMaintLaborCost, 
		__airframeMaintMaterialCost,
		__fuelVolumetricCost,
		__hourVolumetricFuelConsumption,
		__oilMassCost;	


		private Amount<Duration>  __climbDescentTime, 
		__sturtupTaxiTOTime,
		__holdPriorToLandTime, 
		__landingTaxiToStopTime;
		
		public CostsBuilder (String id) {
			this.__id = id;
		}
	
		public CostsBuilder residualValue (double residualValue){
			__residualValue = residualValue;
			return this;
		}
		
		public CostsBuilder annualInterestRate (double annualInterestRate) {
			__annualInterestRate = annualInterestRate;
			return this;
		}
		
		public CostsBuilder annualInsurancePremiumRate(double annualInsurancePremiumRate) {
			__annualInsurancePremiumRate = annualInsurancePremiumRate;
			return this;
		}
		
		public CostsBuilder utilization (double utilization) {
			__utilization = utilization;
			return this;
		}
		
		public CostsBuilder sparesAirframePerCosts(double sparesAirframePerCosts) {
			__sparesAirframePerCosts = sparesAirframePerCosts;
			return this;
		}
		
		public CostsBuilder sparesEnginesPerCosts (double sparesEnginesPerCosts) {
			__sparesEnginesPerCosts = sparesEnginesPerCosts;
			return this;
		}
		
		public CostsBuilder singleCabinCrewHrCost (double singleCabinCrewHrCost){
			__singleCabinCrewHrCost = singleCabinCrewHrCost;
			return this;
		}
	
		public CostsBuilder singleflightCrewHrCost(double singleflightCrewHrCost) {
			__singleflightCrewHrCost = singleflightCrewHrCost;
			return this;
		}
		
		
//		__climbDescentTime, 
//		__sturtupTaxiTOTime,
//		__holdPriorToLandTime, 
//		__landingTaxiToStopTime
		
		
		public CostsBuilder landingFeesPerTon(double landingFeesPerTon) {
			__landingFeesPerTon =landingFeesPerTon;
			return this;
		}
		
		public CostsBuilder jenkinsonNavigationalCharges(double jenkinsonNavigationalCharges) {
			__jenkinsonNavigationalCharges = jenkinsonNavigationalCharges;
			return this;
		}
		
		public CostsBuilder groundHandlingCostXPax(double groundHandlingCostXPax) {
			__groundHandlingCostXPax = groundHandlingCostXPax;
			return this;
		}
		
		public CostsBuilder manHourLaborRate(double manHourLaborRate) {
			__manHourLaborRate = manHourLaborRate;
			return this;
		}
		
		public CostsBuilder overallPressureRatio(double overallPressureRatio) {
			__overallPressureRatio = overallPressureRatio;
			return this;
		}
		
		public CostsBuilder engineMaintLaborCost(double engineMaintLaborCost) {
			__engineMaintLaborCost = engineMaintLaborCost;
			return this;
		}
		
		public CostsBuilder engineMaintMaterialCost(double engineMaintMaterialCost) {
			__engineMaintMaterialCost = engineMaintMaterialCost;
			return this;
		}
		
		
//		__engineMaintMaterialCost,
//		__airframeMaintLaborCost, 
//		__airframeMaintMaterialCost,
//		__fuelVolumetricCost,
//		__hourVolumetricFuelConsumption,
//		__oilMassCost;	
		
		
		
		
		
		
		public Costs build() {
			return new Costs(this);
		}
		

	}
	
	
	// Constructor 
	private Costs (CostsBuilder builder) { 
		
//		this._id = builder.__id;
		
		
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================

		
		
	public Costs(Aircraft aircraft) {

		_theAircraft = aircraft;

		_theFixedCharges = new FixedCharges(_theAircraft, this);
		_theTripCharges= new TripCharges(_theAircraft, this); 

		initializeIndependentVars(
				0.1,									// residualValue, Jenkinson's example value.
				0.053,									// annualInterestRate
				0.005,									// annualInsurancePremiumRate
				4200.,									// utilization (hr/year)
				0.1,									// sparesCostsAsAirframeCostPercentage
				0.3,									// sparesEnginesCostsAsAirframeCostPercentage
				246.5,									// singleCabinCrewHrCost,
				81.,									// singleflightCrewHrCost,
				Amount.valueOf(10, NonSI.MINUTE), 		// Climb and descent time (min),
				Amount.valueOf(20, NonSI.MINUTE), 		// Sturtup Taxi and Take-Off time (min),
				Amount.valueOf(8, NonSI.MINUTE),  		// Hold Prior To Land Time (min),
				Amount.valueOf(5, NonSI.MINUTE),  		// Landing and Taxi To Stop Time (min),
				7.8,									// landingFeesPerTon, As suggested by Kundu (USD per ton of MTOW); Jenkinson suggested instead a value of 6 USD per ton of MTOW.
				5640.,									// jenkinsonNavigationalCharges, (USD)this value is from the jenkinson example. Jenkinson doesn't give a statistic law, but suggest to find the desired value time by time in literature or else.
				72,
				11.,									// groundHandlingCostXPax, Jenkinson suggests this value in USD for the ground handling cost per passenger
				63.0,									// manHourLaborRate, (USD/hr) As suggested by Kundu. This is the cost of an hour man labor for the maintenance of the airframe.
				110.,									// engineMaintLaborCost, (USD/hr/engine) 
				80.,									// engineMaintMaterialCost, (USD/hr/engine)
				660.,									// airframeMaintLaborCost, (USD/hr)	  
				218.,									// airframeMaintMaterialCost, (USD/hr)
				0.75,									// fuelVolumetricCost, Volumetric cost of aeronautic fuel in USD/USGal, as suggested by Kundu
				8.99									// oilMassCost, Mass cost of aeronautic oil (USD/lb) according to Sforza
				);
	}

	/**
	 * Initialize the variable that are independent from aircraft or statistical assumed. //TODO: Complete Javadoc
	 * 
	 * @param residualValue
	 * @param annualInterestRate
	 * @param annualInsurancePremiumRate
	 * @param utilization
	 * @param sparesAirframePerCosts
	 * @param sparesEnginesPerCosts
	 * @param singleCabinCrewHrCost
	 * @param singleflightCrewHrCost
	 * @param climbDescentTime
	 * @param sturtupTaxiTOTime
	 * @param holdPriorToLandTime
	 * @param landingTaxiToStopTime
	 * @param landingFeesPerTon
	 * @param jenkinsonNavigationalCharges
	 * @param numberOfPax
	 * @param groundHandlingCostXPax
	 * @param manHourLaborRate
	 * @param engineMaintLaborCost
	 * @param engineMaintMaterialCost
	 * @param airframeMaintLaborCost
	 * @param airframeMaintMaterialCost
	 * @param fuelVolumetricCost
	 * @param oilMassCost
	 * @author AC
	 */
	public void initializeIndependentVars(double residualValue,
			double annualInterestRate,
			double annualInsurancePremiumRate,
			double utilization,
			double sparesAirframePerCosts,
			double sparesEnginesPerCosts,
			double singleCabinCrewHrCost,
			double singleflightCrewHrCost,
			Amount<Duration> climbDescentTime,
			Amount<Duration> sturtupTaxiTOTime,
			Amount<Duration> holdPriorToLandTime,
			Amount<Duration> landingTaxiToStopTime,
			double landingFeesPerTon,
			double jenkinsonNavigationalCharges,
			int numberOfPax,
			double groundHandlingCostXPax,
			double manHourLaborRate,
			double engineMaintLaborCost, 
			double engineMaintMaterialCost,  
			double airframeMaintLaborCost,		  
			double airframeMaintMaterialCost,
			double fuelVolumetricCost,
			double oilMassCost){

		_residualValue = residualValue;
		_annualInterestRate = annualInterestRate;
		_annualInsurancePremiumRate = annualInsurancePremiumRate;
		_utilization = utilization;
		_sparesAirframePerCosts = sparesAirframePerCosts;
		_sparesEnginesPerCosts = sparesEnginesPerCosts;
		_singleCabinCrewHrCost = singleCabinCrewHrCost;
		_singleflightCrewHrCost = singleflightCrewHrCost;
		_climbDescentTime = climbDescentTime;
		_sturtupTaxiTOTime = sturtupTaxiTOTime;
		_holdPriorToLandTime = holdPriorToLandTime;
		_landingTaxiToStopTime = landingTaxiToStopTime;
		_landingFeesPerTon = landingFeesPerTon;
		_jenkinsonNavigationalCharges = jenkinsonNavigationalCharges;
		_numberOfPax = numberOfPax;
		_groundHandlingCostXPax = groundHandlingCostXPax;
		_manHourLaborRate = manHourLaborRate;
		_engineMaintLaborCost = engineMaintLaborCost;
		_engineMaintMaterialCost = engineMaintMaterialCost;
		_airframeMaintLaborCost = airframeMaintLaborCost;
		_airframeMaintMaterialCost = airframeMaintMaterialCost;
		_fuelVolumetricCost = fuelVolumetricCost;
		_oilMassCost = oilMassCost;
	}

	public void initializeDependentVars(Aircraft aircraft){
		initializeDependentVars(aircraft.getPowerPlant().get_engineNumber().intValue(),			// numberOfEngines
				aircraft.getCabinConfiguration().getCabinCrewNumber().intValue(),			// cabinCrewNumber,
				aircraft.getCabinConfiguration().getFlightCrewNumber().intValue(),			// flightCrewNumber,
				aircraft.getThePerformance().getRange(),	// range (nm)
				aircraft.getThePerformance().getVOptimumCruise(),	// cruiseSpeed, This default value is taken from the Jenkinson's Example
				aircraft.getTheWeights().get_OEM(),			// OEM, 
				aircraft.getTheWeights().get_MTOM(),			// MTOM,
				aircraft.getTheWeights().get_paxMassMax(),		// payload, 
				aircraft.getTheWeights().get_manufacturerEmptyMass().minus(aircraft.getPowerPlant().get_totalMass()),	// airframeMass,
				aircraft.getCabinConfiguration().getMaxPax().intValue(),// numberOfPax, Data from Jenkinson's example.
				aircraft.getPowerPlant().get_engineList().get(0).get_bpr(),		// byPassRatio, Kundu's example value
				14.0,		// overallPressureRatio, Kundu's example value
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfCompressorStages(),			// numberOfCompressorStage, Kundu's example value
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfShafts(),			// numberOfShaft
				aircraft.getPowerPlant().get_engineList().get(0).get_t0(),	// seaLevelStaticThrust (single engine), 
				aircraft.getPowerPlant().get_engineList().get(0).get_t0(),	// thrustTO (single engine), 
				aircraft.getPowerPlant().get_engineList().get(0).get_p0(),	// powerTO (single engine),
				//				aircraft.get_powerPlant().get_engineList().get(0).get_specificFuelConsumption //TODO: Substitute the value below whit this raw
				0.5, // Specific fuel consumption in (lb/(lb*hr))
				1816.0	// hourVolumetricFuelConsumption, Hour fuel consumption in USGal/hr. The value is taken from Jenkinson's example
				);
	}

	/**
	 * @param numberOfEngines
	 * @param cabinCrewNumber
	 * @param flightCrewNumber
	 * @param range
	 * @param cruiseSpeed
	 * @param OEM
	 * @param MTOM
	 * @param payload
	 * @param airframeMass
	 * @param numberOfPax
	 * @param byPassRatio
	 * @param overallPressureRatio
	 * @param numberOfCompressorStage
	 * @param numberOfShaft
	 * @param seaLevelStaticThrust
	 * @param thrustTO
	 * @param powerTO
	 * @param cruiseSpecificFuelConsumption
	 * @param hourVolumetricFuelConsumption
	 * @author AC
	 */
	public void initializeDependentVars(int numberOfEngines,
			int cabinCrewNumber,
			int flightCrewNumber,
			Amount<Length> range,
			Amount<Velocity> cruiseSpeed,
			Amount<Mass> OEM, 
			Amount<Mass> MTOM,
			Amount<Mass> payload,
			Amount<Mass> airframeMass,
			int numberOfPax,
			double byPassRatio,
			double overallPressureRatio,
			int numberOfCompressorStage,
			int numberOfShaft,
			Amount<Force> seaLevelStaticThrust,	
			Amount<Force> thrustTO,
			Amount<Power> powerTO,
			double cruiseSpecificFuelConsumption,
			double hourVolumetricFuelConsumption){

		_numberOfEngines = numberOfEngines;
		_cabinCrewNumber = cabinCrewNumber;
		_flightCrewNumber = flightCrewNumber;
		_range = range;
		_cruiseSpeed =cruiseSpeed;
		_OEM = OEM;
		_MTOM = MTOM;
		_payload = payload;
		_airframeMass = airframeMass;
		_numberOfPax = numberOfPax;
		_byPassRatio = byPassRatio;
		_overallPressureRatio = overallPressureRatio;
		_numberOfCompressorStage = numberOfCompressorStage;
		_numberOfShaft = numberOfShaft;
		_seaLevelStaticThrust = seaLevelStaticThrust;	
		_thrustTO = thrustTO;
		_cruiseSpecificFuelConsumption = cruiseSpecificFuelConsumption;
		_hourVolumetricFuelConsumption = hourVolumetricFuelConsumption;

		_singleEngineCost = CostsCalcUtils.singleEngineCostSforza(_thrustTO, _cruiseSpecificFuelConsumption); 
		_aircraftCost = calcAircraftCostSforza();
		_totalInvestments = calcTotalInvestments(); 
		_airframeCost = _aircraftCost - _singleEngineCost; // TODO: seek for the price of ATR 72, meanwhile these values comes from Jenkinson

		_groundManoeuvreTime = _sturtupTaxiTOTime.plus(_landingTaxiToStopTime);
		_cruiseTime = calcCruiseTime();

		_blockTime = calcBlockTime(); //(hr)

		Amount.valueOf((_range.getEstimatedValue()
				/_blockTime.getEstimatedValue()), NonSI.KNOT);
		_flightTime = _blockTime.minus(_groundManoeuvreTime); //(hr) as suggested by Kundu and Jankinson

		_blockFuelVolume = Amount.valueOf(_hourVolumetricFuelConsumption*
				_blockTime.doubleValue(NonSI.HOUR), NonSI.GALLON_LIQUID_US);// Volumetric block fuel value (USGallons)

	}

	public void initializeAll(Aircraft aircraft) {
		initializeAll(0.1,			// residualValue, Jankinson's example value.
				0.053,		// annualInterestRate
				0.005,		// annualInsurancePremiumRate
				4200.,		// utilization (hr/year)
				CostsCalcUtils.singleEngineCostSforza(Amount.valueOf(0., SI.NEWTON), 0.),	// singleEngineCost
				aircraft.getPowerPlant().get_engineNumber().intValue(),			// numberOfEngines
				0.1,			// sparesCostsAsAirframeCostPercentage
				0.3,			// sparesEnginesCostsAsAirframeCostPercentage
				aircraft.getCabinConfiguration().getCabinCrewNumber().intValue(),			// cabinCrewNumber,
				aircraft.getCabinConfiguration().getFlightCrewNumber().intValue(),			// flightCrewNumber,
				246.5,		// singleCabinCrewHrCost,
				81,			// singleflightCrewHrCost,
				aircraft.getThePerformance().getRange(),	// range (nm)
				aircraft.getThePerformance().getVOptimumCruise(),	// cruiseSpeed, This default value is taken from the Jenkinson's Example
				Amount.valueOf(10, NonSI.MINUTE), // Climb and descent time (min),
				Amount.valueOf(20, NonSI.MINUTE), // Sturtup Taxi and Take-Off time (min),
				Amount.valueOf(8, NonSI.MINUTE),  // Hold Prior To Land Time (min),
				Amount.valueOf(5, NonSI.MINUTE),   // Landing and Taxi To Stop Time (min),
				aircraft.getTheWeights().get_OEM(),			// OEM, 
				aircraft.getTheWeights().get_MTOM(),			// MTOM,
//				aircraft.get_weights().get_paxMassMax(),		// payload,
				aircraft.getTheWeights().get_paxSingleMass().times(aircraft.getCabinConfiguration().getMaxPax()),		// payload,
//				aircraft.get_weights().get_manufacturerEmptyMass().minus(aircraft.get_powerPlant().get_totalMass()),	// airframeMass,
				aircraft.getTheWeights().get_manufacturerEmptyMass().minus(aircraft.getPowerPlant().get_engineList().get(0).get_totalMass()),	// airframeMass,
				7.8,			// landingFeesPerTon, As suggested by Kundu (USD per ton of MTOW); Jenkinson suggested instead a value of 6 USD per ton of MTOW.
				5640.,		// jenkinsonNavigationalCharges, (USD)this value is from the jenkinson example. Jenkinson doesn't give a statistic law, but suggest to find the desired value time by time in literature or else.
				aircraft.getCabinConfiguration().getMaxPax().intValue(),// numberOfPax, Data from Jenkinson's example.
				11,			// groundHandlingCostXPax, Jenkinson suggests this value in USD for the ground handling cost per passenger
				63.0,		// manHourLaborRate, (USD/hr) As suggested by Kundu. This is the cost of an hour man labor for the maintenance of the airframe.
				aircraft.getPowerPlant().get_engineList().get(0).get_bpr(),		// byPassRatio, Kundu's example value
				14.0,		// overallPressureRatio, Kundu's example value
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfCompressorStages(),			// numberOfCompressorStage, Kundu's example value
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfShafts(),			// numberOfShaft
				aircraft.getPowerPlant().get_engineList().get(0).get_t0(),	// seaLevelStaticThrust (single engine), 
				aircraft.getPowerPlant().get_engineList().get(0).get_t0(),	// thrustTO (single engine), 
				aircraft.getPowerPlant().get_engineList().get(0).get_p0(),	// powerTO (single engine),
				//				aircraft.get_powerPlant().get_engineList().get(0).get_specificFuelConsumption //TODO: Substitute the value below whit this raw
				0.5, // Specific fuel consumption in (lb/(lb*hr))
				110,		// engineMaintLaborCost, (USD/hr/engine) 
				80,		// engineMaintMaterialCost, (USD/hr/engine)
				660,		// airframeMaintLaborCost, (USD/hr)	  
				218,		// airframeMaintMaterialCost, (USD/hr)
				0.75,	// fuelVolumetricCost, Volumetric cost of aeronautic fuel in USD/USGal, as suggested by Kundu
				1816.0,	// hourVolumetricFuelConsumption, Hour fuel consumption in USGal/hr. The value is taken from Jenkinson's example
				8.99		// oilMassCost, Mass cost of aeronautic oil (USD/lb) according to Sforza
				);		
	}

	/**
	 * Method that initializes ALL variables of the cost calculation problem.
	 * 
	 * @param residualValue (0.1 typical) ratio between the actual value after an operative life (typical 14/16 years) and the initial value
	 * @param annualInterestRate (0.053 typical) annual interest rate 
	 * @param annualInsurancePremiumRate (0.005-0.03 typical) annual insurance premium rate
	 * @param utilization (hr) Annual utilization of the airplane in hours
	 * @param airframeCost (US$) Cost of the airframe in $US. Cost of the aircraft less the cost of the engines.
	 * @param singleEngineCost (US$) One engine cost in US$.
	 * @param numberOfEngines Number of engines mounted on the airplane
	 * @param sparesAirframePerCosts (0.1 typical) Cost of the airframe operative life spares, expressed as the
	 * 		  ratio between the cost of spares during the operative life and the cost of the airframe
	 * @param sparesEnginesPerCosts (0.3 typical) Cost of the engines operative life spares, expressed as the
	 * 		  ratio between the cost of spares during the operative life and the cost of the engines
	 * @param cabinCrewNumber Number of cabin crew elements (hostesses and stewards)
	 * @param flightCrewNumber Number of flight crew elements (pilots)
	 * @param singleCabinCrewHrCost (60-80 US$/hr typical) Hour cost of one cabin crew element
	 * @param singleflightCrewHrCost (360-500 US$/hr typical) Hour cost of one flight crew element
	 * @param range (nMi)
	 * @param climbDescentTime (10 min typical) time that the aircraft lose in climb and descend phases 
	 * @param sturtupTaxiTOTime (10-20 min typical) time that the aircraft lose in startup, taxi and take-off
	 * 		  phases
	 * @param holdPriorToLandTime (5-8 min typical) time that the aircraft lose in holding before the landing
	 * @param landingTaxiToStopTime (5 min typical) time that the aircraft lose in landing taxi and stopping 
	 * 		  phases
	 * @param blockTime (hr) The time between the turning on and off of the aircraft engines
	 * @param OEM (The unit depends on formula) Operative Empty Mass
	 * @param MTOM (The unit depends on formula) Maximum Take-Off Mass
	 * @param payload (The unit depends on formula) Payload mass
	 * @param airframeMass (The unit depends on formula) Airframe mass (Manufacturer Empty Mass less engines 
	 * 		  mass)
	 * @param landingFeesPerTon (US$/ton) Cost of the landing fees per aircraft's metric ton
	 * @param jenkinsonNavigationalCharges (US$) Value of navigational charges suggested by Jenkinson TODO: Clear this
	 * @param numberOfPax Number of available seats TODO: Modify the name of this variable
	 * @param groundHandlingCostXPax ($/PAX) 
	 * @param manHourLaborRate (40-63 $US/hr) Man-hour labor cost for maintenance
	 * @param byPassRatio 
	 * @param overallPressureRatio Engine data
	 * @param numberOfCompressorStage
	 * @param numberOfShaft
	 * @param seaLevelStaticThrust (The unit depends on formula)
	 * @param thrustTO (The unit depends on formula) Take-Off thrust (for turbofan)
	 * @param powerTO (The unit depends on formula) Take-Off power (for turboprop)
	 * @param engineMaintLaborCost 
	 * @param engineMaintMaterialCost
	 * @param airframeMaintLaborCost
	 * @param airframeMaintMaterialCost
	 * @param cruiseSpeed (The unit depends on formula) 
	 * @param blockFuelVolume (The unit depends on formula)
	 * @param fuelVolumetricCost (US$/USGal) Aeronautical fuel price (dollar) per volumetric unit(USGal). For
	 * 		  the actual price look at http://www.iata.org/publications/economics/fuel-monitor/Pages/price-analysis.aspx
	 * @param hourVolumetricFuelConsumption (USGal/hr) Hour fuel consumption
	 * @param oilMassCost (US$/lb) Aeronautical lubricant price (dollar) per mass unit (pound)
	 * 
	 * @author AC
	 */
	public void initializeAll(double residualValue,
			double annualInterestRate,
			double annualInsurancePremiumRate,
			double utilization,
			double singleEngineCost,
			int numberOfEngines,
			double sparesAirframePerCosts,
			double sparesEnginesPerCosts,
			int cabinCrewNumber,
			int flightCrewNumber,
			double singleCabinCrewHrCost,
			double singleflightCrewHrCost,
			Amount<Length> range,
			Amount<Velocity> cruiseSpeed,
			Amount<Duration> climbDescentTime,
			Amount<Duration> sturtupTaxiTOTime,
			Amount<Duration> holdPriorToLandTime,
			Amount<Duration> landingTaxiToStopTime,
			Amount<Mass> OEM, 
			Amount<Mass> MTOM,
			Amount<Mass> payload,
			Amount<Mass> airframeMass,
			double landingFeesPerTon,
			double jenkinsonNavigationalCharges,
			int numberOfPax,
			double groundHandlingCostXPax,
			double manHourLaborRate,
			double byPassRatio,
			double overallPressureRatio,
			int numberOfCompressorStage,
			int numberOfShaft,
			Amount<Force> seaLevelStaticThrust,	
			Amount<Force> thrustTO,
			Amount<Power> powerTO,
			double cruiseSpecificFuelConsumption,
			double engineMaintLaborCost, 
			double engineMaintMaterialCost,  
			double airframeMaintLaborCost,		  
			double airframeMaintMaterialCost,
			double fuelVolumetricCost,
			double hourVolumetricFuelConsumption,
			double oilMassCost){

		initializeAircractMasses(OEM, 
				MTOM,
				payload,
				airframeMass);

		initializeMaintAndEngineVariable(manHourLaborRate,
				byPassRatio,
				overallPressureRatio,
				numberOfCompressorStage,
				numberOfShaft,
				seaLevelStaticThrust,	
				thrustTO,
				powerTO,
				cruiseSpecificFuelConsumption);

		initializeFinacialCostVariables(residualValue,
				annualInterestRate,
				annualInsurancePremiumRate,
				utilization,
				singleEngineCost,
				numberOfEngines,
				sparesAirframePerCosts,
				sparesEnginesPerCosts);

		initializeFlightDataVariables(cabinCrewNumber,
				flightCrewNumber,
				singleCabinCrewHrCost,
				singleflightCrewHrCost,
				range,
				cruiseSpeed,
				climbDescentTime,
				sturtupTaxiTOTime,
				holdPriorToLandTime,
				landingTaxiToStopTime);

		initializeTripChargesVariables(landingFeesPerTon,
				jenkinsonNavigationalCharges,
				numberOfPax,
				groundHandlingCostXPax);

		initializeAvailableMaintenanceCost(engineMaintLaborCost, 
				engineMaintMaterialCost,  
				airframeMaintLaborCost,		  
				airframeMaintMaterialCost);

		initializeFuelAndOilCunsumptionVariables(fuelVolumetricCost,
				hourVolumetricFuelConsumption,
				oilMassCost);
	}

	public void initializeFinacialCostVariables(double residualValue,
			double annualInterestRate,
			double annualInsurancePremiumRate,
			double utilization,
			double singleEngineCost,
			int numberOfEngines,
			double sparesAirframePerCosts,
			double sparesEnginesPerCosts){

		_residualValue = residualValue;		  
		_annualInterestRate = annualInterestRate;
		_annualInsurancePremiumRate = annualInsurancePremiumRate;
		_utilization = utilization;
		_numberOfEngines = numberOfEngines;
		_sparesAirframePerCosts = sparesAirframePerCosts;
		_sparesEnginesPerCosts = sparesEnginesPerCosts;
		_singleEngineCost = CostsCalcUtils.singleEngineCostSforza(_thrustTO, _cruiseSpecificFuelConsumption); 
		_aircraftCost = calcAircraftCostSforza();
		_totalInvestments = calcTotalInvestments(); 
		_airframeCost = _aircraftCost - _singleEngineCost; // TODO: seek for the price of ATR 72, meanwhile these values comes from Jenkinson
	}

	public void initializeFlightDataVariables(int cabinCrewNumber,
			int flightCrewNumber,
			double singleCabinCrewHrCost,
			double singleflightCrewHrCost,
			Amount<Length> range,
			Amount<Velocity> cruiseSpeed,
			Amount<Duration> climbDescentTime,
			Amount<Duration> sturtupTaxiTOTime,
			Amount<Duration> holdPriorToLandTime,
			Amount<Duration> landingTaxiToStopTime){

		_cabinCrewNumber = cabinCrewNumber;
		_flightCrewNumber = flightCrewNumber;
		_singleCabinCrewHrCost = singleCabinCrewHrCost;
		_singleflightCrewHrCost = singleflightCrewHrCost;

		_range = range; // (nm)

		_cruiseSpeed = cruiseSpeed;

		_climbDescentTime = climbDescentTime;
		_sturtupTaxiTOTime = sturtupTaxiTOTime;
		_holdPriorToLandTime = holdPriorToLandTime;
		_landingTaxiToStopTime = landingTaxiToStopTime;

		_groundManoeuvreTime = _sturtupTaxiTOTime.plus(_landingTaxiToStopTime);
		_cruiseTime = calcCruiseTime();

		_blockTime = calcBlockTime(); //(hr)

		Amount.valueOf((_range.getEstimatedValue()
				/_blockTime.getEstimatedValue()), NonSI.KNOT);
		_flightTime = _blockTime.minus(_groundManoeuvreTime); //(hr) as suggested by Kundu and Jankinson
	}

	/**
	 * Flight data initialization (double version)
	 * 
	 * @param cabinCrewNumber
	 * @param flightCrewNumber
	 * @param singleCabinCrewHrCost
	 * @param singleflightCrewHrCost
	 * @param range (km)
	 * @param cruiseSpeed (m/s) TAS 
	 * @param climbDescentTime (min)
	 * @param sturtupTaxiTOTime (min)
	 * @param holdPriorToLandTime (min)
	 * @param landingTaxiToStopTime (min)
	 */
	public void initializeFlightDataVariables(int cabinCrewNumber,
			int flightCrewNumber,
			double singleCabinCrewHrCost,
			double singleflightCrewHrCost,
			double range,
			double cruiseSpeed,
			double climbDescentTime,
			double sturtupTaxiTOTime,
			double holdPriorToLandTime,
			double landingTaxiToStopTime){

		initializeFlightDataVariables(cabinCrewNumber,
				flightCrewNumber,
				singleCabinCrewHrCost,
				singleflightCrewHrCost,
				Amount.valueOf(range, SI.KILOMETER),
				Amount.valueOf(cruiseSpeed, SI.METERS_PER_SECOND),
				Amount.valueOf(climbDescentTime, NonSI.MINUTE),
				Amount.valueOf(sturtupTaxiTOTime, NonSI.MINUTE),
				Amount.valueOf(holdPriorToLandTime, NonSI.MINUTE),
				Amount.valueOf(landingTaxiToStopTime, NonSI.MINUTE));
	}


	public void initializeAircractMasses(Amount<Mass> OEM, 
			Amount<Mass> MTOM,
			Amount<Mass> payload,
			Amount<Mass> airframeMass){
		_OEM = OEM; 
		_MTOM = MTOM;		
		_payload =payload;
		_airframeMass = airframeMass;
	}

	/**
	 * All the parameter in this initialization are in Kilograms (SI)
	 * 
	 * @param OEM
	 * @param MTOM
	 * @param payload
	 * @param airframeMass
	 */
	public void initializeAircractMasses(double OEM, 
			double MTOM,
			double payload,
			double airframeMass){

		initializeAircractMasses(Amount.valueOf(OEM, SI.KILOGRAM), 
				Amount.valueOf(MTOM, SI.KILOGRAM),
				Amount.valueOf(payload, SI.KILOGRAM),
				Amount.valueOf(airframeMass, SI.KILOGRAM));
	}	


	public void initializeTripChargesVariables(double landingFeesPerTon,
			double jenkinsonNavigationalCharges,
			int numberOfPax,
			double groundHandlingCostXPax){

		_landingFeesPerTon = landingFeesPerTon; //(USD/Ton)
		_jenkinsonNavigationalCharges = jenkinsonNavigationalCharges; // (USD)

		_numberOfPax = numberOfPax;
		_groundHandlingCostXPax = groundHandlingCostXPax;//(USD/Pax)

	}

	public void initializeMaintAndEngineVariable(double manHourLaborRate,
			double byPassRatio,
			double overallPressureRatio,
			int numberOfCompressorStage,
			int numberOfShaft,
			Amount<Force> seaLevelStaticThrust,	
			Amount<Force> thrustTO,
			Amount<Power> powerTO,
			double cruiseSpecificFuelConsumption){

		_manHourLaborRate = manHourLaborRate;//(USD/hr)

		_byPassRatio = byPassRatio;
		_overallPressureRatio = overallPressureRatio;
		_numberOfCompressorStage = numberOfCompressorStage;
		_numberOfShaft = numberOfShaft;
		_seaLevelStaticThrust = seaLevelStaticThrust;//(N)		
		_thrustTO = thrustTO;//(N)
		_cruiseSpecificFuelConsumption = cruiseSpecificFuelConsumption;
	}

	/**
	 * @param manHourLaborRate
	 * @param byPassRatio
	 * @param overallPressureRatio
	 * @param numberOfCompressorStage
	 * @param numberOfShaft
	 * @param seaLevelStaticThrust (N)
	 * @param thrustTO (N)
	 * @param powerTO (KW)
	 * @param cruiseSpecificFuelConsumption
	 */
	public void initializeMaintAndEngineVariable(double manHourLaborRate,
			double byPassRatio,
			double overallPressureRatio,
			int numberOfCompressorStage,
			int numberOfShaft,
			double seaLevelStaticThrust,	
			double thrustTO,
			double powerTO,
			double cruiseSpecificFuelConsumption){

		initializeMaintAndEngineVariable(manHourLaborRate,
				byPassRatio,
				overallPressureRatio,
				numberOfCompressorStage,
				numberOfShaft,
				Amount.valueOf(seaLevelStaticThrust, SI.NEWTON),
				Amount.valueOf(thrustTO, SI.NEWTON),
				Amount.valueOf(powerTO*1000., SI.WATT),
				cruiseSpecificFuelConsumption);
	}

	public void initializeAvailableMaintenanceCost(double engineMaintLaborCost, 
			double engineMaintMaterialCost,  
			double airframeMaintLaborCost,		  
			double airframeMaintMaterialCost){		
		// These variables give the possibility to put
		// directly the costs, if the user already knows their amounts.
		_engineMaintLaborCost = engineMaintLaborCost;   // (USD/hr/engine) 
		_engineMaintMaterialCost = engineMaintMaterialCost; // (USD/hr/engine)  
		_airframeMaintLaborCost = airframeMaintLaborCost; // (USD/hr)		  
		_airframeMaintMaterialCost = airframeMaintMaterialCost; // (USD/hr)

	}

	public void initializeFuelAndOilCunsumptionVariables(double fuelVolumetricCost,
			double hourVolumetricFuelConsumption,
			double oilMassCost){

		_fuelVolumetricCost = fuelVolumetricCost;// Volumetric cost of aeronautic fuel in USD/USGal
		_hourVolumetricFuelConsumption = hourVolumetricFuelConsumption;// Hour fuel consumption in USGal/hr
		_oilMassCost = oilMassCost;// Mass cost of aeronautic oil (USD/lb)
		_blockFuelVolume = Amount.valueOf(_hourVolumetricFuelConsumption*
				_blockTime.doubleValue(NonSI.HOUR), NonSI.GALLON_LIQUID_US);// Volumetric block fuel value (USGallons)
	}

	/**
	 * Needs data from weights, engines, performance (fuel consumption),
	 * number of passenger.
	 * 
	 * @param aircraft
	 */
	public void calculateAll(Aircraft aircraft) {

		JPADStaticWriteUtils.logToConsole("STARTING COSTS EVALUATION");
		initializeAll(aircraft);
		_totalInvestments = calcTotalInvestments();
		//		calcAircraftCost();
		_aircraftCost = calcAircraftCostSforza();

		_theFixedCharges.initialize(_utilization, _totalInvestments, _aircraftCost,
				_singleEngineCost, aircraft.getLifeSpan(),	_residualValue, _annualInterestRate,
				_annualInsurancePremiumRate, _cabinCrewNumber, _flightCrewNumber, _numberOfEngines,
				_singleCabinCrewHrCost, _singleflightCrewHrCost, _MTOM);
		_theFixedCharges.calculateAll();

		_theTripCharges.initialize(_landingFeesPerTon, _MTOM, _blockTime, _range,
				_jenkinsonNavigationalCharges, _payload, _numberOfPax, _groundHandlingCostXPax,
				_airframeMass, _manHourLaborRate, _airframeCost, _flightTime, _byPassRatio,
				_overallPressureRatio, _numberOfCompressorStage, _numberOfShaft, _seaLevelStaticThrust,
				_numberOfEngines, _engineMaintLaborCost, _engineMaintMaterialCost, _airframeMaintLaborCost,
				_airframeMaintMaterialCost, _OEM, _cruiseSpeed, _aircraftCost, _singleEngineCost,
				_thrustTO, _blockFuelVolume, _fuelVolumetricCost, _hourVolumetricFuelConsumption,
				_oilMassCost);
		_theTripCharges.calculateAll();

		JPADStaticWriteUtils.logToConsole("COSTS EVALUATION DONE");
	}

	public void calculateAll() {
		calculateAll(_theAircraft);
	}

	public double calcTotalInvestments(){

		return CostsCalcUtils.calcTotalInvestments(_airframeCost,
				_singleEngineCost,
				_numberOfEngines,
				_sparesAirframePerCosts,
				_sparesEnginesPerCosts);
	}


	public double calcAircraftCost(){

		return CostsCalcUtils.calcAircraftCost(_airframeCost,
				_singleEngineCost,
				_numberOfEngines);
	}


	public double calcAircraftCostSforza(){
		return CostsCalcUtils.calcAircraftCostSforza(_OEM);
	}
	
	/**
	 * Calculate the aircraft utilization (in hours per year) with the method suggested
	 * by Kundu
	 * 
	 * @param blockTime (hours per trip)
	 * @return utilization (hours per year)
	 * @author AC
	 */
	public static double calcUtilizationKundu(double blockTime){
		return (3750./(blockTime+0.5))*blockTime;
	}


	public Amount<Duration> calcBlockTime(){
		return PerformanceCalcUtils.calcBlockTime(_cruiseTime,
				_climbDescentTime,
				_sturtupTaxiTOTime,
				_holdPriorToLandTime,
				_landingTaxiToStopTime);
	}

	public Amount<Duration> calcCruiseTime(){
		return PerformanceCalcUtils.calcCruiseTime(_range,
				_climbDescentTime, _cruiseSpeed);
	}	

	public FixedCharges get_theFixedCharges() {
		return _theFixedCharges;
	}

	public TripCharges get_theTripCharges() {
		return _theTripCharges;
	}

	public double get_totalInvestments() {
		return _totalInvestments;
	}

	public void set_totalInvestments(double _totalInvestments) {
		this._totalInvestments = _totalInvestments;
	}

	public double get_aircraftCost() {
		return _aircraftCost;
	}

	public void set_aircraftCost(double _aircraftCost) {
		this._aircraftCost = _aircraftCost;
	}


	public void set_theAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

	public double get_utilization() {
		return _utilization;
	}

	public void set_utilization(double utilization) {
		this._utilization = utilization;
	}

	public double get_annualInterestRate() {
		return _annualInterestRate;
	}


	public void set_annualInterestRate(double _annualInterestRate) {
		this._annualInterestRate = _annualInterestRate;
	}


	public double get_residualValue() {
		return _residualValue;
	}


	public void set_residualValue(double _residualValue) {
		this._residualValue = _residualValue;
	}

	public double get_airframeCost() {
		return _airframeCost;
	}

	public void set_airframeCost(double _airframeCost) {
		this._airframeCost = _airframeCost;
	}

	public double get_singleEngineCost() {
		return _singleEngineCost;
	}

	public void set_singleEngineCost(double _singleEngineCost) {
		this._singleEngineCost = _singleEngineCost;
	}

	public double get_sparesAirframePerCosts() {
		return _sparesAirframePerCosts;
	}

	public void set_sparesAirframePerCosts(double _sparesAirframePerCosts) {
		this._sparesAirframePerCosts = _sparesAirframePerCosts;
	}

	public double get_sparesEnginesPerCosts() {
		return _sparesEnginesPerCosts;
	}

	public double get_annualInsurancePremiumRate() {
		return _annualInsurancePremiumRate;
	}

	public void set_annualInsurancePremiumRate(double _annualInsurancePremiumRate) {
		this._annualInsurancePremiumRate = _annualInsurancePremiumRate;
	}

	public void set_sparesEnginesPerCosts(double _sparesEnginesPerCosts) {
		this._sparesEnginesPerCosts = _sparesEnginesPerCosts;
	}

	public void set_numberOfEngines(int _numberOfEngines) {
		this._numberOfEngines = _numberOfEngines;
	}

	public double get_singleCabinCrewHrCost() {
		return _singleCabinCrewHrCost;
	}

	public void set_singleCabinCrewHrCost(double _singleCabinCrewHrCost) {
		this._singleCabinCrewHrCost = _singleCabinCrewHrCost;
	}

	public double get_singleflightCrewHrCost() {
		return _singleflightCrewHrCost;
	}

	public void set_singleflightCrewHrCost(double _singleflightCrewHrCost) {
		this._singleflightCrewHrCost = _singleflightCrewHrCost;
	}

	public void set_cabinCrewNumber(int _cabinCrewNumber) {
		this._cabinCrewNumber = _cabinCrewNumber;
	}

	public void set_flightCrewNumber(int _flightCrewNumber) {
		this._flightCrewNumber = _flightCrewNumber;
	}

	public void set_range(Amount<Length> _range) {
		this._range = _range;
	}

	public void set_blockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}

	public void set_blockSpeed(Amount<Velocity> _blockSpeed) {
	}

	public void set_flightTime(Amount<Duration> _flightTime) {
		this._flightTime = _flightTime;
	}

	public double get_landingFeesPerTon() {
		return _landingFeesPerTon;
	}

	public void set_landingFeesPerTon(double _landingFeesPerTon) {
		this._landingFeesPerTon = _landingFeesPerTon;
	}

	public double get_jenkinsonNavigationalCharges() {
		return _jenkinsonNavigationalCharges;
	}

	public void set_jenkinsonNavigationalCharges(
			double _jenkinsonNavigationalCharges) {
		this._jenkinsonNavigationalCharges = _jenkinsonNavigationalCharges;
	}

	public double get_groundHandlingCostXPax() {
		return _groundHandlingCostXPax;
	}

	public void set_groundHandlingCostXPax(double _groundHandlingCostXPax) {
		this._groundHandlingCostXPax = _groundHandlingCostXPax;
	}

	public double get_manHourLaborRate() {
		return _manHourLaborRate;
	}

	public void set_manHourLaborRate(double _manHourLaborRate) {
		this._manHourLaborRate = _manHourLaborRate;
	}

	public void set_byPassRatio(double _byPassRatio) {
		this._byPassRatio = _byPassRatio;
	}

	public void set_overallPressureRatio(double _overallPressureRatio) {
		this._overallPressureRatio = _overallPressureRatio;
	}

	public void set_numberOfCompressorStage(int _numberOfCompressorStage) {
		this._numberOfCompressorStage = _numberOfCompressorStage;
	}

	public void set_numberOfShaft(int _numberOfShaft) {
		this._numberOfShaft = _numberOfShaft;
	}

	public void set_seaLevelStaticThrust(Amount<Force> _seaLevelStaticThrust) {
		this._seaLevelStaticThrust = _seaLevelStaticThrust;
	}

	public double get_engineMaintLaborCost() {
		return _engineMaintLaborCost;
	}

	public void set_engineMaintLaborCost(double _engineMaintLaborCost) {
		this._engineMaintLaborCost = _engineMaintLaborCost;
	}

	public double get_engineMaintMaterialCost() {
		return _engineMaintMaterialCost;
	}

	public void set_engineMaintMaterialCost(double _engineMaintMaterialCost) {
		this._engineMaintMaterialCost = _engineMaintMaterialCost;
	}

	public double get_airframeMaintLaborCost() {
		return _airframeMaintLaborCost;
	}

	public void set_airframeMaintLaborCost(double _airframeMaintLaborCost) {
		this._airframeMaintLaborCost = _airframeMaintLaborCost;
	}

	public double get_airframeMaintMaterialCost() {
		return _airframeMaintMaterialCost;
	}

	public void set_airframeMaintMaterialCost(double _airframeMaintMaterialCost) {
		this._airframeMaintMaterialCost = _airframeMaintMaterialCost;
	}

	public void set_cruiseSpeed(Amount<Velocity> _cruiseSpeed) {
		this._cruiseSpeed = _cruiseSpeed;
	}

	public void set_thrustTO(Amount<Force> _thrustTO) {
		this._thrustTO = _thrustTO;
	}

	public void set_powerTO(Amount<Power> _powerTO) {
	}

	public Amount<Volume> get_blockFuelVolume() {
		return _blockFuelVolume;
	}

	public void set_blockFuelVolume(Amount<Volume> _blockFuelVolume) {
		this._blockFuelVolume = _blockFuelVolume;
	}

	public double get_fuelVolumetricCost() {
		return _fuelVolumetricCost;
	}

	public void set_fuelVolumetricCost(double _fuelVolumetricCost) {
		this._fuelVolumetricCost = _fuelVolumetricCost;
	}

	public double get_hourVolumetricFuelConsumption() {
		return _hourVolumetricFuelConsumption;
	}

	public void set_hourVolumetricFuelConsumption(
			double _hourVolumetricFuelConsumption) {
		this._hourVolumetricFuelConsumption = _hourVolumetricFuelConsumption;
	}

	public double get_oilMassCost() {
		return _oilMassCost;
	}

	public void set_oilMassCost(double _oilMassCost) {
		this._oilMassCost = _oilMassCost;
	}

	public void set_groundManoeuvreTime(Amount<Duration> _groundManoeuvreTime) {
		this._groundManoeuvreTime = _groundManoeuvreTime;
	}

	public void set_cruiseTime(Amount<Duration> _cruiseTime) {
		this._cruiseTime = _cruiseTime;
	}

	public void set_climbDescentTime(Amount<Duration> _climbDescentTime) {
		this._climbDescentTime = _climbDescentTime;
	}

	public void set_sturtupTaxiTOTime(Amount<Duration> _sturtupTaxiTOTime) {
		this._sturtupTaxiTOTime = _sturtupTaxiTOTime;
	}

	public void set_holdPriorToLandTime(Amount<Duration> _holdPriorToLandTime) {
		this._holdPriorToLandTime = _holdPriorToLandTime;
	}

	public void set_landingTaxiToStopTime(Amount<Duration> _landingTaxiToStopTime) {
		this._landingTaxiToStopTime = _landingTaxiToStopTime;
	}

	public static String getId() {
		return "25";
	}

} // end of class MyCost