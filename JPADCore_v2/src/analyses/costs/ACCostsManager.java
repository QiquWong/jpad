package analyses.costs;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.ACWeightsManager;
import calculators.costs.CostsCalcUtils;
import calculators.performance.PerformanceCalcUtils;
import calculators.performance.ThrustCalc;
import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class ACCostsManager implements ICosts {

	private String _id;
	private Aircraft _theAircraft;

	private FixedCharges _theFixedCharges;
	private TripCharges _theTripCharges;

	private double _totalInvestments, _utilization, _residualValue,  _annualInterestRate, 
	_annualInsurancePremiumRate, _singleCabinCrewHrCost, _singleFlightCrewHrCost, _groundHandlingCostXPax,
	_manHourLaborRate, _byPassRatio, _overallPressureRatio, _fuelVolumetricCost, _cruiseSpecificFuelConsumption,
	_hourVolumetricFuelConsumption, _oilMassCost;	

	private double _airframeCost, _singleEngineCost, _sparesAirframePerCosts,
	_sparesEnginesPerCosts, _aircraftCost, _engineMaintLaborCost,
	_engineMaintMaterialCost, _airframeMaintLaborCost, _airframeMaintMaterialCost;

	private double _landingFeesPerTon, _jenkinsonNavigationalCharges;

	private Amount<Duration> _blockTime, _flightTime, _groundManoeuvreTime, _cruiseTime, _climbDescentTime,
	_startupTaxiTOTime, _holdPriorToLandTime, _landingTaxiToStopTime;

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
		private Aircraft __theAircraft;

		// optional parameters
		private double 	__residualValue,
		__annualInterestRate, 
		__annualInsurancePremiumRate,  
		__utilization, 
		__sparesAirframePerCosts, 
		__sparesEnginesPerCosts,
		__singleCabinCrewHrCost,
		__singleFlightCrewHrCost, 
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
		__startupTaxiTOTime,
		__holdPriorToLandTime, 
		__landingTaxiToStopTime;

		
		
		public CostsBuilder (String id, Aircraft theAircraft) {
			this.__id = id;
			this.__theAircraft = theAircraft;
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
	
		public CostsBuilder singleFlightCrewHrCost(double singleFlightCrewHrCost) {
			__singleFlightCrewHrCost = singleFlightCrewHrCost;
			return this;
		}
		
		public CostsBuilder climbDescentTime(Amount<Duration> climbDescentTime) {
			__climbDescentTime = climbDescentTime;
			return this;
		}
		
		public CostsBuilder startupTaxiTOTime(Amount<Duration> startupTaxiTOTime){
			__startupTaxiTOTime = startupTaxiTOTime;
			return this;
		}
		
		public CostsBuilder holdPriorToLandTime(Amount<Duration> holdPriorToLandTime){
			__holdPriorToLandTime = holdPriorToLandTime;
			return this;
		}
		
		public CostsBuilder landingTaxiToStopTime(Amount<Duration> landingTaxiToStopTime){
			__landingTaxiToStopTime = landingTaxiToStopTime;
			return this;
		}
		
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
		
		public CostsBuilder airframeMaintLaborCost(double airframeMaintLaborCost) {
			__airframeMaintLaborCost = airframeMaintLaborCost;
			return this;
		}
		
		public CostsBuilder airframeMaintMaterialCost(double airframeMaintMaterialCost) {
			__airframeMaintMaterialCost = airframeMaintMaterialCost;
			return this;
		}
		
		public CostsBuilder fuelVolumetricCost(double fuelVolumetricCost){
			__fuelVolumetricCost = fuelVolumetricCost;
			return this;
		}
		
		public  CostsBuilder hourVolumetricFuelConsumption(double hourVolumetricFuelConsumption){
			__hourVolumetricFuelConsumption = hourVolumetricFuelConsumption;
			return this;
		}
		
		public CostsBuilder oilMassCost(double oilMassCost){
			__oilMassCost = oilMassCost;
			return this;
		}
		
		public ACCostsManager build() {
			return new ACCostsManager(this);
		}
	}
	
	// Constructor of Costs class
	private ACCostsManager (CostsBuilder builder) { 
		
		this._id = builder.__id;
		this._theAircraft = builder.__theAircraft;
		this._residualValue = builder.__residualValue;
		this._annualInterestRate = builder.__annualInterestRate;
		this._annualInsurancePremiumRate = builder.__annualInsurancePremiumRate;
		this._utilization = builder.__utilization;
		this._sparesAirframePerCosts = builder.__sparesAirframePerCosts;
		this._sparesEnginesPerCosts = builder.__sparesEnginesPerCosts;
		this._singleCabinCrewHrCost = builder.__singleCabinCrewHrCost;
		this._singleFlightCrewHrCost = builder.__singleFlightCrewHrCost;
		this._climbDescentTime = builder.__climbDescentTime;
		this._startupTaxiTOTime = builder.__startupTaxiTOTime;
		this._holdPriorToLandTime = builder.__holdPriorToLandTime;
		this._landingTaxiToStopTime = builder.__landingTaxiToStopTime;
		this._landingFeesPerTon = builder.__landingFeesPerTon;
		this._jenkinsonNavigationalCharges = builder.__jenkinsonNavigationalCharges;
		this._groundHandlingCostXPax = builder.__groundHandlingCostXPax;
		this._manHourLaborRate = builder.__manHourLaborRate;
		this._overallPressureRatio = builder.__overallPressureRatio;
		this._engineMaintLaborCost = builder.__engineMaintLaborCost;
		this._engineMaintMaterialCost = builder.__engineMaintMaterialCost;
		this._airframeMaintLaborCost = builder.__airframeMaintLaborCost;
		this._airframeMaintMaterialCost = builder.__airframeMaintMaterialCost;
		this._fuelVolumetricCost = builder.__fuelVolumetricCost;
		this._hourVolumetricFuelConsumption = builder.__hourVolumetricFuelConsumption;
		this._oilMassCost = builder.__oilMassCost;

	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	public static ACCostsManager importFromXML(String pathToXML, Aircraft _theAircraft){
	
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading costs data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(reader.getXmlDoc(), reader.getXpath(),"//@id");
		
		// Fixed Charges
		double residualValue = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Residual_Value"));
		double annualInterestRate = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Annual_Interest_Rate"));
		double annualInsurancePremiumRate = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Annual_Insurance_Premium_Rate"));
		double utilization = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Utilization"));
		double sparesAirframePerCosts = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Spares_Costs_As_Airframe_Cost_Percentage"));
		double sparesEnginesPerCosts = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Spares_Engines_Costs_As_Airframe_Cost_Percentage"));
		double singleFlightCrewHrCost = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Single_Flight_Crew_Hr_Cost"));
		double singleCabinCrewHrCost = Double.valueOf(reader.getXMLPropertyByPath("//Fixed_Charges/Single_Cabin_Crew_Hr_Cost"));
		
		// Trip Charges
		Amount<Duration> climbDescentTime = Amount.valueOf(
														Double.valueOf(
																reader.getXMLPropertyByPath("//Trip_Charges/Climb_and_Descent_Time")),
														NonSI.MINUTE); 
		
		Amount<Duration> startupTaxiTOTime = Amount.valueOf(
													Double.valueOf(
															reader.getXMLPropertyByPath("//Trip_Charges/Sturtup_Taxi_and_Take_Off_time")),
											  NonSI.MINUTE); 
		
		Amount<Duration> holdPriorToLandTime = Amount.valueOf(
															Double.valueOf(
																	reader.getXMLPropertyByPath("//Trip_Charges/Hold_Prior_To_Land_Time")),
															NonSI.MINUTE);
		
		Amount<Duration> landingTaxiToStopTime = Amount.valueOf(
															Double.valueOf(
																	reader.getXMLPropertyByPath("//Trip_Charges/Landing_and_Taxi_To_Stop_Time")),
															NonSI.MINUTE); 
				
		double landingFeesPerTon = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Landing_Fees_Per_Ton")); 
		double jenkinsonNavigationalCharges = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Navigational_Charges")); 
		double groundHandlingCostXPax = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Ground_Handling_Cost_Per_Pax")); 
		double manHourLaborRate = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Labor_Manhour_Rate")); 
		double overallPressureRatio = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/OAPR")); 
		double engineMaintLaborCost  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Engine_Maint_Labor_Cost")); 
		double engineMaintMaterialCost   = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Engine_Maint_Material_Cost")); 
		double airframeMaintLaborCost  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Airframe_Maint_Labor_Cost")); 
		double airframeMaintMaterialCost  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Airframe_Maint_Material_Cost")); 
		double fuelVolumetricCost  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Fuel_Volumetric_Cost")); 
		double hourVolumetricFuelConsumption  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Hour_Volumetric_Fuel_Consumption")); 
		double oilMassCost  = Double.valueOf(reader.getXMLPropertyByPath("//Trip_Charges/Oil_Mass_Cost")); 
		
		ACCostsManager costs = new CostsBuilder(id,_theAircraft)
				.residualValue(residualValue)
				.annualInterestRate(annualInterestRate)
				.annualInsurancePremiumRate(annualInsurancePremiumRate)
				.utilization(utilization)
				.sparesAirframePerCosts(sparesAirframePerCosts)
				.sparesEnginesPerCosts(sparesEnginesPerCosts)
				.singleCabinCrewHrCost(singleCabinCrewHrCost)
				.singleFlightCrewHrCost(singleFlightCrewHrCost)
				.climbDescentTime(climbDescentTime)
				.startupTaxiTOTime(startupTaxiTOTime)
				.holdPriorToLandTime(holdPriorToLandTime)
				.landingTaxiToStopTime(landingTaxiToStopTime)
				.landingFeesPerTon(landingFeesPerTon)
				.jenkinsonNavigationalCharges(jenkinsonNavigationalCharges)
				.groundHandlingCostXPax(groundHandlingCostXPax)
				.manHourLaborRate(manHourLaborRate)
				.overallPressureRatio(overallPressureRatio)
				.engineMaintLaborCost(engineMaintLaborCost)
				.engineMaintMaterialCost(engineMaintMaterialCost)
				.airframeMaintLaborCost(airframeMaintLaborCost)
				.airframeMaintMaterialCost(airframeMaintMaterialCost)
				.fuelVolumetricCost(fuelVolumetricCost)
				.hourVolumetricFuelConsumption(hourVolumetricFuelConsumption)
				.oilMassCost(oilMassCost)
				.build();
		
		return costs;
	}
		
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tCosts\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				.append("\tResidual Value: " + _residualValue + "\n")
				.append("\t·····································\n")
				.append("\tAnnual InterestRate: " + _annualInterestRate + "\n")
				.append("\t·····································\n")
				.append("\tAnnual Insurance Premium Rate: "+_annualInsurancePremiumRate + "\n")
				.append("\t·····································\n")
				.append("\tUtilization: " + _utilization + " hr/year \n")
				.append("\t·····································\n")		
				.append("\tSpares Airframe Per Costs: " + _sparesAirframePerCosts + "\n")
				.append("\t·····································\n")
				.append("\tSpares Engines Per Costs: " + _sparesEnginesPerCosts + "\n")
				.append("\t·····································\n")
				.append("\tSingle Cabin Crew Hr Cost: + " + _singleCabinCrewHrCost + " USD \n")
				.append("\t·····································\n")
				.append("\tSingle Flight Crew Hr Cost: " + _singleFlightCrewHrCost + " USD \n")
				.append("\t·····································\n")
				.append("\tClimb Descent Time: " + _climbDescentTime + "\n")
				.append("\t·····································\n")
				.append("\tStartup Taxi TO Time: " + _startupTaxiTOTime + "\n")
				.append("\t·····································\n")
				.append("\tHold Prior To Land Time: " + _holdPriorToLandTime + "\n")
				.append("\t·····································\n")
				.append("\tLanding Taxi To Stop Time: " + _landingTaxiToStopTime + "\n")
				.append("\t·····································\n")
				.append("\tLanding Fees Per Ton: " + _landingFeesPerTon + " USD \n")
				.append("\t·····································\n")
				.append("\tJenkinson Navigational Charges: " +  _jenkinsonNavigationalCharges + " USD \n")
				.append("\t·····································\n")
				.append("\tGround Handling Cost Per Pax: " + _groundHandlingCostXPax + " USD \n")
				.append("\t·····································\n")
				.append("\tMan Hour Labor Rate: " + _manHourLaborRate + " USD/hr \n")
				.append("\t·····································\n")
				.append("\tOverall Pressure Ratio: " + _overallPressureRatio + "\n")
				.append("\t·····································\n")
				.append("\tEngine Maint Labor Cost: " + _engineMaintLaborCost + " USD/hr/engine \n")
				.append("\t·····································\n")
				.append("\tEngine Maint Material Cost: " + _engineMaintMaterialCost + " USD/hr/engine \n")
				.append("\t·····································\n")
				.append("\tAirframe Maint Labor Cost: " + _airframeMaintLaborCost + " USD/hr \n")
				.append("\t·····································\n")
				.append("\tAirframe Maint Material Cost: " + _airframeMaintMaterialCost + " USD/hr \n") 
				.append("\t·····································\n")
				.append("\tFuel Volumetric Cost: " + _fuelVolumetricCost + " USD/USGal \n")
				.append("\t·····································\n")
				.append("\tHour Volumetric Fuel Consumption: " + _hourVolumetricFuelConsumption + " USGal/hr \n")
				.append("\t·····································\n")
				.append("\tOil Mass Cost: " + _oilMassCost + " USD/lb \n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	
	@Override
	public void initializeAll(Aircraft aircraft) {
		
		_theFixedCharges = new FixedCharges(aircraft, this);
		_theTripCharges= new TripCharges(aircraft, this); 
		
		initializeAll(
				CostsCalcUtils.singleEngineCostSforza(Amount.valueOf(0., SI.NEWTON), 0.),	// singleEngineCost
				aircraft.getPowerPlant().getEngineNumber().intValue(),			// numberOfEngines
				aircraft.getCabinConfiguration().getCabinCrewNumber().intValue(),			// cabinCrewNumber,
				aircraft.getCabinConfiguration().getFlightCrewNumber().intValue(),			// flightCrewNumber,
				aircraft.getThePerformance().getRange(),	// range (nm)
				aircraft.getThePerformance().getVOptimumCruise(),	// cruiseSpeed, This default value is taken from the Jenkinson's Example
				aircraft.getTheWeights().getOperatingEmptyMass(),			// OEM, 
				aircraft.getTheWeights().getMaximumTakeOffMass(),			// MTOM,
//				aircraft.get_weights().get_paxMassMax(),		// payload,
				aircraft.getTheWeights().getPaxSingleMass().times(aircraft.getCabinConfiguration().getMaxPax()),		// payload,
//				aircraft.get_weights().get_manufacturerEmptyMass().minus(aircraft.get_powerPlant().get_totalMass()),	// airframeMass,
				aircraft.getTheWeights().getManufacturerEmptyMass().minus(aircraft.getPowerPlant().getEngineList().get(0).getTotalMass()),	// airframeMass,
				aircraft.getCabinConfiguration().getMaxPax().intValue(),// numberOfPax, Data from Jenkinson's example.
				aircraft.getPowerPlant().getEngineList().get(0).getBPR(),		// byPassRatio, Kundu's example value
				aircraft.getPowerPlant().getEngineList().get(0).getNumberOfCompressorStages(),			// numberOfCompressorStage, Kundu's example value
				aircraft.getPowerPlant().getEngineList().get(0).getNumberOfShafts(),			// numberOfShaft
				aircraft.getPowerPlant().getEngineList().get(0).getT0(),	// seaLevelStaticThrust (single engine), 
				aircraft.getPowerPlant().getEngineList().get(0).getT0(),	// thrustTO (single engine), 
				aircraft.getPowerPlant().getEngineList().get(0).getP0(),	// powerTO (single engine),
				//				aircraft.get_powerPlant().get_engineList().get(0).get_specificFuelConsumption //TODO: Substitute the value below whit this raw
				0.5 // Specific fuel consumption in (lb/(lb*hr))
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
	public void initializeAll(
			double singleEngineCost,
			int numberOfEngines,
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
			int numberOfCompressorStage,
			int numberOfShaft,
			Amount<Force> seaLevelStaticThrust,	
			Amount<Force> thrustTO,
			Amount<Power> powerTO,
			double cruiseSpecificFuelConsumption){

		initializeAircractMasses(OEM, 
				MTOM,
				payload,
				airframeMass);

		initializeMaintAndEngineVariable(getManHourLaborRate(),
				byPassRatio,
				getOverallPressureRatio(),
				numberOfCompressorStage,
				numberOfShaft,
				seaLevelStaticThrust,	
				thrustTO,
				powerTO,
				cruiseSpecificFuelConsumption);

		initializeFinacialCostVariables(getResidualValue(),
				getAnnualInterestRate(),
				getAnnualInsurancePremiumRate(),
				getUtilization(),
				singleEngineCost,
				numberOfEngines,
				getSparesAirframePerCosts(),
				getSparesEnginesPerCosts());

		initializeFlightDataVariables(cabinCrewNumber,
				flightCrewNumber,
				getSingleCabinCrewHrCost(),
				getSingleflightCrewHrCost(),
				range,
				cruiseSpeed,
				getClimbDescentTime(),
				getStartupTaxiTOTime(),
				getHoldPriorToLandTime(),
				getLandingTaxiToStopTime());

		initializeTripChargesVariables(getLandingFeesPerTon(),
				getJenkinsonNavigationalCharges(),
				numberOfPax,
				getGroundHandlingCostXPax());

		initializeAvailableMaintenanceCost(getEngineMaintLaborCost(), 
				getEngineMaintMaterialCost(),  
				getAirframeMaintLaborCost(),		  
				getAirframeMaintMaterialCost());

		initializeFuelAndOilCunsumptionVariables(getFuelVolumetricCost(),
				getHourVolumetricFuelConsumption(),
				getOilMassCost());
	}

	@Override
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
	
	@Override
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
		_singleFlightCrewHrCost = singleflightCrewHrCost;

		_range = range; // (nm)

		_cruiseSpeed = cruiseSpeed;

		_climbDescentTime = climbDescentTime;
		_startupTaxiTOTime = sturtupTaxiTOTime;
		_holdPriorToLandTime = holdPriorToLandTime;
		_landingTaxiToStopTime = landingTaxiToStopTime;

		_groundManoeuvreTime = _startupTaxiTOTime.plus(_landingTaxiToStopTime);
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


	@Override
	public void initializeTripChargesVariables(double landingFeesPerTon,
			double jenkinsonNavigationalCharges,
			int numberOfPax,
			double groundHandlingCostXPax){

		_landingFeesPerTon = landingFeesPerTon; //(USD/Ton)
		_jenkinsonNavigationalCharges = jenkinsonNavigationalCharges; // (USD)

		_numberOfPax = numberOfPax;
		_groundHandlingCostXPax = groundHandlingCostXPax;//(USD/Pax)

	}

	@Override
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
	
	@Override
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

	@Override
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
	@Override
	public void calculateAll(Aircraft aircraft) {

		JPADStaticWriteUtils.logToConsole("STARTING COSTS EVALUATION");
		initializeAll(aircraft);
		_totalInvestments = calcTotalInvestments();
		//		calcAircraftCost();
		_aircraftCost = calcAircraftCostSforza();

		_theFixedCharges.initialize(_utilization, _totalInvestments, _aircraftCost,
				_singleEngineCost, aircraft.getLifeSpan(),	_residualValue, _annualInterestRate,
				_annualInsurancePremiumRate, _cabinCrewNumber, _flightCrewNumber, _numberOfEngines,
				_singleCabinCrewHrCost, _singleFlightCrewHrCost, _MTOM);
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

	@Override
	public double calcTotalInvestments(){

		return CostsCalcUtils.calcTotalInvestments(_airframeCost,
				_singleEngineCost,
				_numberOfEngines,
				_sparesAirframePerCosts,
				_sparesEnginesPerCosts);
	}

	@Override
	public double calcAircraftCost(){

		return CostsCalcUtils.calcAircraftCost(_airframeCost,
				_singleEngineCost,
				_numberOfEngines);
	}

	@Override
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

	@Override
	public Amount<Duration> calcBlockTime(){
		return PerformanceCalcUtils.calcBlockTime(_cruiseTime,
				_climbDescentTime,
				_startupTaxiTOTime,
				_holdPriorToLandTime,
				_landingTaxiToStopTime);
	}

	@Override
	public Amount<Duration> calcCruiseTime(){
		return PerformanceCalcUtils.calcCruiseTime(_range,
				_climbDescentTime, _cruiseSpeed);
	}	

	@Override
	public FixedCharges getTheFixedCharges() {
		return _theFixedCharges;
	}

	@Override
	public TripCharges getTheTripCharges() {
		return _theTripCharges;
	}

	@Override
	public double getTotalInvestments() {
		return _totalInvestments;
	}

	public void setTotalInvestments(double _totalInvestments) {
		this._totalInvestments = _totalInvestments;
	}

	@Override
	public double getAircraftCost() {
		return _aircraftCost;
	}

	public void setAircraftCost(double _aircraftCost) {
		this._aircraftCost = _aircraftCost;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

	@Override
	public double getUtilization() {
		return _utilization;
	}

	public void setUtilization(double utilization) {
		this._utilization = utilization;
	}

	public double getAnnualInterestRate() {
		return _annualInterestRate;
	}


	public void setAnnualInterestRate(double _annualInterestRate) {
		this._annualInterestRate = _annualInterestRate;
	}


	public double getResidualValue() {
		return _residualValue;
	}

	public void setResidualValue(double _residualValue) {
		this._residualValue = _residualValue;
	}

	@Override
	public double getAirframeCost() {
		return _airframeCost;
	}

	public void setAirframeCost(double _airframeCost) {
		this._airframeCost = _airframeCost;
	}

	public double getSingleEngineCost() {
		return _singleEngineCost;
	}

	public void setSingleEngineCost(double _singleEngineCost) {
		this._singleEngineCost = _singleEngineCost;
	}

	public double getSparesAirframePerCosts() {
		return _sparesAirframePerCosts;
	}

	public void setSparesAirframePerCosts(double _sparesAirframePerCosts) {
		this._sparesAirframePerCosts = _sparesAirframePerCosts;
	}

	public double getSparesEnginesPerCosts() {
		return _sparesEnginesPerCosts;
	}

	public double getAnnualInsurancePremiumRate() {
		return _annualInsurancePremiumRate;
	}

	public void setAnnualInsurancePremiumRate(double _annualInsurancePremiumRate) {
		this._annualInsurancePremiumRate = _annualInsurancePremiumRate;
	}

	public void setSparesEnginesPerCosts(double _sparesEnginesPerCosts) {
		this._sparesEnginesPerCosts = _sparesEnginesPerCosts;
	}

	public void setNumberOfEngines(int _numberOfEngines) {
		this._numberOfEngines = _numberOfEngines;
	}

	public double getSingleCabinCrewHrCost() {
		return _singleCabinCrewHrCost;
	}

	public void setSingleCabinCrewHrCost(double _singleCabinCrewHrCost) {
		this._singleCabinCrewHrCost = _singleCabinCrewHrCost;
	}

	public double getSingleflightCrewHrCost() {
		return _singleFlightCrewHrCost;
	}

	public void setSingleflightCrewHrCost(double _singleflightCrewHrCost) {
		this._singleFlightCrewHrCost = _singleflightCrewHrCost;
	}

	public void setCabinCrewNumber(int _cabinCrewNumber) {
		this._cabinCrewNumber = _cabinCrewNumber;
	}

	public void setFlightCrewNumber(int _flightCrewNumber) {
		this._flightCrewNumber = _flightCrewNumber;
	}

	public void setRange(Amount<Length> _range) {
		this._range = _range;
	}

	public void setBlockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}

	public void setBlockSpeed(Amount<Velocity> _blockSpeed) {
	}

	public void setFlightTime(Amount<Duration> _flightTime) {
		this._flightTime = _flightTime;
	}

	public double getLandingFeesPerTon() {
		return _landingFeesPerTon;
	}

	public void setLandingFeesPerTon(double _landingFeesPerTon) {
		this._landingFeesPerTon = _landingFeesPerTon;
	}

	public double getJenkinsonNavigationalCharges() {
		return _jenkinsonNavigationalCharges;
	}

	public void setJenkinsonNavigationalCharges(
			double _jenkinsonNavigationalCharges) {
		this._jenkinsonNavigationalCharges = _jenkinsonNavigationalCharges;
	}

	public double getGroundHandlingCostXPax() {
		return _groundHandlingCostXPax;
	}

	public void setGroundHandlingCostXPax(double _groundHandlingCostXPax) {
		this._groundHandlingCostXPax = _groundHandlingCostXPax;
	}

	public double getManHourLaborRate() {
		return _manHourLaborRate;
	}

	public void setManHourLaborRate(double _manHourLaborRate) {
		this._manHourLaborRate = _manHourLaborRate;
	}

	public void setByPassRatio(double _byPassRatio) {
		this._byPassRatio = _byPassRatio;
	}

	public double getOverallPressureRatio() {
		return _overallPressureRatio;
	}
	
	public void setOverallPressureRatio(double _overallPressureRatio) {
		this._overallPressureRatio = _overallPressureRatio;
	}

	public void setNumberOfCompressorStage(int _numberOfCompressorStage) {
		this._numberOfCompressorStage = _numberOfCompressorStage;
	}

	public void setNumberOfShaft(int _numberOfShaft) {
		this._numberOfShaft = _numberOfShaft;
	}

	public void setSeaLevelStaticThrust(Amount<Force> _seaLevelStaticThrust) {
		this._seaLevelStaticThrust = _seaLevelStaticThrust;
	}

	public double getEngineMaintLaborCost() {
		return _engineMaintLaborCost;
	}

	public void setEngineMaintLaborCost(double _engineMaintLaborCost) {
		this._engineMaintLaborCost = _engineMaintLaborCost;
	}

	public double getEngineMaintMaterialCost() {
		return _engineMaintMaterialCost;
	}

	public void setEngineMaintMaterialCost(double _engineMaintMaterialCost) {
		this._engineMaintMaterialCost = _engineMaintMaterialCost;
	}

	public double getAirframeMaintLaborCost() {
		return _airframeMaintLaborCost;
	}

	public void setAirframeMaintLaborCost(double _airframeMaintLaborCost) {
		this._airframeMaintLaborCost = _airframeMaintLaborCost;
	}

	public double getAirframeMaintMaterialCost() {
		return _airframeMaintMaterialCost;
	}

	public void setAirframeMaintMaterialCost(double _airframeMaintMaterialCost) {
		this._airframeMaintMaterialCost = _airframeMaintMaterialCost;
	}

	public void setCruiseSpeed(Amount<Velocity> _cruiseSpeed) {
		this._cruiseSpeed = _cruiseSpeed;
	}

	public void setThrustTO(Amount<Force> _thrustTO) {
		this._thrustTO = _thrustTO;
	}

	public void setPowerTO(Amount<Power> _powerTO) {
	}

	public Amount<Volume> getBlockFuelVolume() {
		return _blockFuelVolume;
	}

	public void setBlockFuelVolume(Amount<Volume> _blockFuelVolume) {
		this._blockFuelVolume = _blockFuelVolume;
	}

	public double getFuelVolumetricCost() {
		return _fuelVolumetricCost;
	}

	public void setFuelVolumetricCost(double _fuelVolumetricCost) {
		this._fuelVolumetricCost = _fuelVolumetricCost;
	}

	public double getHourVolumetricFuelConsumption() {
		return _hourVolumetricFuelConsumption;
	}

	public void setHourVolumetricFuelConsumption(
			double _hourVolumetricFuelConsumption) {
		this._hourVolumetricFuelConsumption = _hourVolumetricFuelConsumption;
	}

	public double getOilMassCost() {
		return _oilMassCost;
	}

	public void setOilMassCost(double _oilMassCost) {
		this._oilMassCost = _oilMassCost;
	}

	public void setGroundManoeuvreTime(Amount<Duration> _groundManoeuvreTime) {
		this._groundManoeuvreTime = _groundManoeuvreTime;
	}

	public void setCruiseTime(Amount<Duration> _cruiseTime) {
		this._cruiseTime = _cruiseTime;
	}

	public void setClimbDescentTime(Amount<Duration> _climbDescentTime) {
		this._climbDescentTime = _climbDescentTime;
	}

	public void setSturtupTaxiTOTime(Amount<Duration> _sturtupTaxiTOTime) {
		this._startupTaxiTOTime = _sturtupTaxiTOTime;
	}

	public void setHoldPriorToLandTime(Amount<Duration> _holdPriorToLandTime) {
		this._holdPriorToLandTime = _holdPriorToLandTime;
	}

	public void setLandingTaxiToStopTime(Amount<Duration> _landingTaxiToStopTime) {
		this._landingTaxiToStopTime = _landingTaxiToStopTime;
	}
	public static String getId() {
		return "25";
	}

	public Amount<Duration> getStartupTaxiTOTime() {
		return _startupTaxiTOTime;
	}

	public void setStartupTaxiTOTime(Amount<Duration> _startupTaxiTOTime) {
		this._startupTaxiTOTime = _startupTaxiTOTime;
	}

	public Amount<Duration> getClimbDescentTime() {
		return _climbDescentTime;
	}

	public Amount<Duration> getHoldPriorToLandTime() {
		return _holdPriorToLandTime;
	}

	public Amount<Duration> getLandingTaxiToStopTime() {
		return _landingTaxiToStopTime;
	}


} // end of class MyCost