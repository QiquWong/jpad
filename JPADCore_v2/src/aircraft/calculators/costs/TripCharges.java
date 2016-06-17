package aircraft.calculators.costs;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.componentmodel.InnerCalculator;
import aircraft.components.Aircraft;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;


public class TripCharges {

	private Aircraft _theAircraft;
	//	private MyCost _theCost; // TODO: control how organize the classes and the calling
	//	      to _utilization, _totalInvestments and _residualValue

	private double _utilization, _landingFeesPerTon, _jenkinsonNavigationalCharges, _groundHandlingCostXPax,
	_airframeCost, _manHourLaborRate, _byPassRatio, _overallPressureRatio, _aircraftCost, 
	_singleEngineCost, _fuelVolumetricCost, _hourVolumetricFuelConsumption, _oilMassCost,
	_engineMaintLaborCost, //This values are needed if we decide to use the jenkinsonCostsAvailable method
	_engineMaintMaterialCost, _airframeMaintLaborCost,	_airframeMaintMaterialCost;	

	private int _numberOfPax, _numberOfCompressorStage, _numberOfShaft, _numberOfEngines;

	private Amount<Mass> _MTOM, _OEM, _payload, _airframeMass;

	private Amount<Length> _range;

	private Amount<Duration> _blockTime, _flightTime;

	private Amount<Force> _seaLevelStaticThrust;

	private Amount<Velocity> _cruiseSpeed;

	private Amount<Volume> _blockFuelVolume;

	private Amount _engineTOFactor;

	//	private double _totalFixedCharges;

	private CalcLandingFees _calcLandingFees;
	private CalcNavigationalCharges _calcNavigationalCharges;
	private CalcGroundHandlingCharges _calcGroundHandlingCharges;
	private CalcMaintenanceCosts _calcMaintenanceCosts;
	private CalcFuelAndOilCharges _calcFuelAndOilCharges;

	private Map<MethodEnum, Double> _totalTripChargesMap;

	/**
	 * MyTripCharge constructor
	 * 
	 * @param aircraft
	 * @param costs
	 * @author AC
	 */
	public TripCharges(Aircraft aircraft, Costs costs) {
		_theAircraft = aircraft;

		_calcLandingFees = new CalcLandingFees();
		_calcNavigationalCharges = new CalcNavigationalCharges();
		_calcGroundHandlingCharges = new CalcGroundHandlingCharges();
		_calcMaintenanceCosts = new CalcMaintenanceCosts();
		_calcFuelAndOilCharges = new CalcFuelAndOilCharges();
		_totalTripChargesMap = new TreeMap<MethodEnum, Double>();
	}

	public void initialize(Aircraft aircraft, Costs costs) {
		initialize(costs.get_landingFeesPerTon(), 
				aircraft.getTheWeights().get_MTOM(), 
				aircraft.getThePerformance().get_blockTime(),
				aircraft.getThePerformance().get_range(), costs.get_jenkinsonNavigationalCharges(),
				aircraft.getTheWeights().get_paxMassMax(), aircraft.getCabinConfiguration().getMaxPax().intValue(),
				costs.get_groundHandlingCostXPax(),
				aircraft.getTheWeights().get_manufacturerEmptyMass().minus(aircraft.getPowerPlant().get_totalMass()),
				costs.get_manHourLaborRate(), costs.get_airframeCost(), 
				aircraft.getThePerformance().get_flightTime(),
				aircraft.getPowerPlant().get_engineList().get(0).get_bpr(), 
				aircraft.getPowerPlant().get_engineList().get(0).get_overallPressureRatio(),
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfCompressorStages(), 
				aircraft.getPowerPlant().get_engineList().get(0).get_numberOfShafts(),
				aircraft.getPowerPlant().get_engineList().get(0).get_t0(), aircraft.getPowerPlant().get_engineNumber(),
				costs.get_engineMaintLaborCost(), costs.get_engineMaintMaterialCost(),
				costs.get_airframeMaintLaborCost(),	costs.get_airframeMaintMaterialCost(),
				aircraft.getTheWeights().get_OEM(), 
				aircraft.getThePerformance().get_vOptimumCruise(), costs.get_aircraftCost(),
				costs.get_singleEngineCost(), aircraft.getPowerPlant().get_P0Total(), costs.get_blockFuelVolume(), 
				costs.get_fuelVolumetricCost(), costs.get_hourVolumetricFuelConsumption(), 
				costs.get_oilMassCost());		
	}

	/**
	 * Initialize an object of MyFixedCharges class
	 * @param utilization annual utilization in block hours
	 * @param totalInvestments (USD)
	 * @param lifeSpan operating life in years
	 * @param residualValue rate of total investment after the life span (0.1 typical value after 14/16 years)
	 * @param annualInterestRate (non-dimensional) current base national interest (typical 0.53/0.54)
	 * @param annualInsurancePremiumRate (non dimensional) typical Kundu: 0.005; Jenkinson: 0.01/0.03.
	 * @author AC
	 */
	public void initialize(double landingFeesPerTon,
			Amount<Mass> MTOM,
			Amount<Duration> blockTime,
			Amount<Length> range,
			double jenkinsonNavigationalCharges,
			Amount<Mass> payload,
			int numberOfPax,
			double groundHandlingCostXPax,
			Amount<Mass> airframeMass,
			double manHourLaborRate,
			double airframeCost,
			Amount<Duration> flightTime,
			double byPassRatio,
			double overallPressureRatio,
			int numberOfCompressorStage,
			int numberOfShaft,
			Amount<Force> seaLevelStaticThrust,
			int numberOfEngines,
			double engineMaintLaborCost,
			double engineMaintMaterialCost,
			double airframeMaintLaborCost,
			double airframeMaintMaterialCost,
			Amount<Mass> OEM,
			Amount<Velocity> cruiseSpeed,
			double aircraftCost,
			double singleEngineCost,
			Amount engineTOFactor, // TODO Create a method which handles different engine type (Turbofan (Thrust)/Turboprop (Power)). Maybe already exixts.
			Amount<Volume> blockFuelVolume,
			double fuelVolumetricCost,
			double hourVolumetricFuelConsumption,
			double oilMassCost){

		_landingFeesPerTon = landingFeesPerTon;
		_MTOM = MTOM;
		_blockTime = blockTime;
		_range = range;
		_jenkinsonNavigationalCharges = jenkinsonNavigationalCharges;
		_payload = payload;
		_numberOfPax = numberOfPax;
		_groundHandlingCostXPax = groundHandlingCostXPax;
		_airframeMass = airframeMass;
		_manHourLaborRate = manHourLaborRate;
		_airframeCost = airframeCost;
		_flightTime = flightTime;
		_byPassRatio = byPassRatio;
		_overallPressureRatio = overallPressureRatio;
		_numberOfCompressorStage = numberOfCompressorStage;
		_numberOfShaft = numberOfShaft;
		_seaLevelStaticThrust = seaLevelStaticThrust;
		_numberOfEngines = numberOfEngines;
		_engineMaintLaborCost = engineMaintLaborCost;
		_engineMaintMaterialCost = engineMaintMaterialCost;
		_airframeMaintLaborCost = airframeMaintLaborCost;
		_airframeMaintMaterialCost = airframeMaintMaterialCost;
		_OEM = OEM;
		_cruiseSpeed = cruiseSpeed;
		_aircraftCost = aircraftCost;
		_singleEngineCost = singleEngineCost;
		_engineTOFactor = engineTOFactor;
		_blockFuelVolume = blockFuelVolume;
		_fuelVolumetricCost = fuelVolumetricCost;
		_hourVolumetricFuelConsumption = hourVolumetricFuelConsumption;
		_oilMassCost = oilMassCost;
		//		_utilization = utilization;

	}

	/**
	 * Calculate the total sum of the calculated fixed charges, then put them in a Map
	 * @author AC
	 */
	private void calcTotalFixedCharges(){

		_totalTripChargesMap.put(MethodEnum.KUNDU,
				_calcLandingFees.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcNavigationalCharges.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcGroundHandlingCharges.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcMaintenanceCosts.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcFuelAndOilCharges.get_methodsMap().get(MethodEnum.KUNDU)
				);

		_totalTripChargesMap.put(MethodEnum.JENKINSON,
				_calcLandingFees.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcNavigationalCharges.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcGroundHandlingCharges.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcMaintenanceCosts.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcFuelAndOilCharges.get_methodsMap().get(MethodEnum.JENKINSON)
				);

		_totalTripChargesMap.put(MethodEnum.SFORZA,
				_calcMaintenanceCosts.get_methodsMap().get(MethodEnum.SFORZA)+
				_calcFuelAndOilCharges.get_methodsMap().get(MethodEnum.SFORZA)
				);

	}

	public void calculateAll() {
		_calcLandingFees.allMethods();
		_calcNavigationalCharges.allMethods();
		_calcGroundHandlingCharges.allMethods();
		_calcMaintenanceCosts.allMethods();
		_calcFuelAndOilCharges.allMethods();
		calcTotalFixedCharges();
	}

	public class CalcLandingFees extends InnerCalculator{

		/**
		 * Method that calculates the landing fees hour cost, as suggested by Kundu.
		 * 
		 * @param landingFeesPerTon USD per ton of MTOW. Kundu suggest a typical value of 7.8.
		 * @param MTOM Maximum Take-Off Mass in metric tons.
		 * @param blockTime It is the time that elapses between the switching on and off of the motors
		 * 			for a mission.
		 * @return landingFees Hour cost due to landing fees in USD/hr
		 * @author AC
		 */
		private double kundu(double landingFeesPerTon, double MTOM, double blockTime ){

			double landingFees = landingFeesPerTon*MTOM/blockTime;

			_methodsMap.put(MethodEnum.KUNDU, landingFees);

			return landingFees;
		}

		private double kundu(double landingFeesPerTon, Amount<Mass> MTOM, Amount<Duration> blockTime) {
			return kundu(landingFeesPerTon,
					MTOM.doubleValue(NonSI.METRIC_TON),
					blockTime.doubleValue(NonSI.HOUR));				
		}

		public double kundu() {
			return kundu(_landingFeesPerTon,
					_MTOM,
					_blockTime);				
		}	

		/**
		 * Method that calculates the landing fees hour cost, as suggested by Jenkinson.
		 * Actually it is equal to Kundu, except for the value suggested for landingFeesPerTon.
		 * 
		 * @param landingFeesPerTon USD per ton of MTOW. Jenkinson suggest a typical value of 6.0.
		 * @param MTOM Maximum Take-Off Mass in metric tons.
		 * @param blockTime It is the time that elapses between the switching on and off of the motors
		 * 			for a mission.
		 * @return landingFees Hour cost due to landing fees in USD/hr
		 * @author AC
		 */
		private double jenkinson(double landingFeesPerTon, double MTOM, double blockTime ){

			double landingFees = landingFeesPerTon*MTOM/blockTime;

			_methodsMap.put(MethodEnum.JENKINSON, landingFees);

			return landingFees;
		}

		private double jenkinson(double landingFeesPerTon, Amount<Mass> MTOM, Amount<Duration> blockTime) {
			return jenkinson(landingFeesPerTon,
					MTOM.doubleValue(NonSI.METRIC_TON),
					blockTime.doubleValue(NonSI.HOUR));				
		}

		public double jenkinson(){
			return jenkinson(_landingFeesPerTon,
					_MTOM,
					_blockTime);				
		}

		public void allMethods(){
			kundu();
			jenkinson();
		}

	}//end_of_class CalcLandingFees

	public class CalcNavigationalCharges extends InnerCalculator{

		/**
		 * Method that calculates the navigational charges hour cost, according to Kundu.
		 * 
		 * @param range operative range in Km
		 * @param MTOM Max Take-Off Mass in Metric tons
		 * @param blockTime 
		 * @return navigationalCharges (USD/hr)
		 * @author AC
		 */
		private double kundu(double range, double MTOM, double blockTime){

			double navigationalCharges = ((0.5*range)/blockTime)*Math.sqrt(MTOM/50.0);

			_methodsMap.put(MethodEnum.KUNDU, navigationalCharges);

			return navigationalCharges;
		}

		private double kundu(Amount<Length> range, Amount<Mass> MTOM, Amount<Duration> blockTime){
			return kundu(range.doubleValue(SI.KILOMETER),
					MTOM.doubleValue(NonSI.METRIC_TON),
					blockTime.doubleValue(NonSI.HOUR));
		}

		public double kundu(){
			return kundu(_range,
					_MTOM,
					_blockTime);
		}

		//TODO: find a better way
		/**
		 * Actually this method only took the value contained in costs, given by Jenkinson, of the total
		 * navigational charges and divides it by block hour to obtain the hourly cost.
		 * 
		 * @param jenkinsonNavigationalCharges (USD)
		 * @param blockTime (hr)
		 * @return jenkinsonHourNavigationalCharges (USD/hr)
		 * @author AC
		 */
		private double jenkinson(double jenkinsonNavigationalCharges, double blockTime){
			double jenkinsonHourNavigationalCharges = jenkinsonNavigationalCharges/blockTime;

			_methodsMap.put(MethodEnum.JENKINSON, jenkinsonHourNavigationalCharges);

			return jenkinsonHourNavigationalCharges;
		}

		private double jenkinson(double jenkinsonNavigationalCharges, Amount<Duration> blockTime){
			return jenkinson(jenkinsonNavigationalCharges,
					blockTime.doubleValue(NonSI.HOUR));			
		}
		
		public double jenkinson(){
			return jenkinson(_jenkinsonNavigationalCharges, _blockTime);
		}

		public void allMethods(){
			kundu();
			jenkinson();
		}

	}//end-of-class CalcNavigationalCharges

	public class CalcGroundHandlingCharges extends InnerCalculator{

		/**
		 * Method that calculates the ground handling charges per hour (USD/hr) according to what suggested
		 * by Kundu
		 * 
		 * @param tonsPayLoad payload in metric tons
		 * @param blockTime block time in hour
		 * @return groundHandlingCharges (USD/hr)
		 * @author AC
		 */
		private double kundu(double tonsPayLoad, double blockTime){

			double groundHandlingCharges = 100.*tonsPayLoad/blockTime;

			_methodsMap.put(MethodEnum.KUNDU, groundHandlingCharges);

			return groundHandlingCharges;
		}

		private double kundu(Amount<Mass> payLoad, Amount<Duration> blockTime){
			return kundu(payLoad.doubleValue(NonSI.METRIC_TON),
					blockTime.doubleValue(NonSI.HOUR)
					);
		}

		public double kundu(){
			return kundu(_payload,
					_blockTime);
		}

		/**
		 * Method that calculates the ground handling charges per hour (USD/hr) according to Jenkinson
		 *  
		 * @param groundHandlingCostXPax ground handling cost per passenger (USD)
		 * @param numberOfPax number of passenger
		 * @param blockTime (hour)
		 * @return groundHandlingCharges (USD/hr)
		 * @author AC
		 */
		private double jenkinson(double groundHandlingCostXPax, int numberOfPax, double blockTime){

			double groundHandlingCharges = groundHandlingCostXPax*numberOfPax/blockTime;

			_methodsMap.put(MethodEnum.JENKINSON, groundHandlingCharges);

			return groundHandlingCharges;			
		}

		private double jenkinson(double groundHandlingCostXPax, int numberOfPax,
				Amount<Duration> blockTime){

			return jenkinson(groundHandlingCostXPax,
					numberOfPax,
					blockTime.doubleValue(NonSI.HOUR));
		}
		public double jenkinson(){
			return jenkinson(_groundHandlingCostXPax,
					_numberOfPax,
					_blockTime);
		}

		public void allMethods(){
			kundu();
			jenkinson();
		}

	}//end-of-class CalcGroundHandlingCharges

	public class CalcMaintenanceCosts extends InnerCalculator{

		/**
		 * Method that calculates the airframe maintenance labor's hour cost, according 
		 * to Kundu (AEA method)
		 *  
		 * @param airframeMass (M_Tons)
		 * @param manHourLaborRate (USD/hr)
		 * @param blockTime (hr)
		 * @param flightTime (hr)
		 * @return (USD/hr) The airframe maintenance labor's hour cost
		 * @author AC
		 */
		private double airframeLaborKundu(double airframeMass, double manHourLaborRate,
				double blockTime, double flightTime){
			return (0.09*airframeMass + 6.7 - (350/(airframeMass+75))) * 
					((0.8 + 0.68*flightTime) / blockTime ) * manHourLaborRate;
		}

		private double airframeLaborKundu(Amount<Mass> airframeMass, double manHourLaborRate,
				Amount<Duration> blockTime, Amount<Duration> flightTime){
			return airframeLaborKundu(airframeMass.doubleValue(NonSI.METRIC_TON),
					manHourLaborRate,
					blockTime.doubleValue(NonSI.HOUR),
					flightTime.doubleValue(NonSI.HOUR));
		}

		public double airframeLaborKundu(){
			return airframeLaborKundu(_airframeMass,
					_manHourLaborRate,
					_blockTime,
					_flightTime);
		}

		/**
		 * Method that calculates the airframe material maintenance's hour cost, according
		 * to Kundu (AEA Method)
		 * 
		 * @param airframeCost (USD)
		 * @param blockTime (hr)
		 * @param flightTime (hr)
		 * @return (USD/hr) The airframe material maintenance's hour cost
		 * @author AC
		 */
		private double airframeMaterialKundu(double airframeCost, double blockTime, double flightTime){
			return ((4.2 + 2.2*flightTime)/blockTime) * airframeCost/1000000.0;	
		}

		private double airframeMaterialKundu(double airframeCost, Amount<Duration> blockTime,
				Amount<Duration> flightTime){

			return airframeMaterialKundu(airframeCost,
					blockTime.doubleValue(NonSI.HOUR),
					flightTime.doubleValue(NonSI.HOUR));
		}

		public double airframeMaterialKundu(){
			return airframeMaterialKundu(_airframeCost,
					_blockTime,
					_flightTime);
		}

		/**
		 * Method that calculates the C1 coefficient, introduced by Kundu (actually in the AEA Method)
		 * to compute the maintenance costs
		 * 
		 * @param byPassRatio
		 * @return C1 Kundu coefficient
		 * @author AC
		 */
		private double engineKunduCoefficient1(double byPassRatio){
			return 1.27 - 0.2*(Math.pow(byPassRatio, 0.2));			
		}

		public double engineKunduCoefficient1(){
			return engineKunduCoefficient1(_byPassRatio);
		}

		/**
		 * Method that calculates the C2 coefficient, introduced by Kundu (actually in the AEA Method)
		 * to compute the maintenance costs
		 * 
		 * @param overallPressureRatio
		 * @return C2 Kundu coefficient
		 * @author AC
		 */
		private double engineKunduCoefficient2(double overallPressureRatio){
			return 0.4 * (Math.pow(overallPressureRatio/20, 1.3)) + 0.4;
		}

		public double engineKunduCoefficient2(){
			return engineKunduCoefficient2(_overallPressureRatio);
		}

		/**
		 * Method that calculates the C3 coefficient, introduced by Kundu (actually in the AEA Method)
		 * to compute the maintenance costs
		 * 
		 * @param numberOfCompressorStage
		 * @param numberOfShaft
		 * @return C3 Kundu coefficient
		 * @author AC
		 */
		private double engineKunduCoefficient3(int numberOfCompressorStage, int numberOfShaft){
			double kShaftConstant = 0.50 + (numberOfShaft-1) * 0.07;
			return 0.032*numberOfCompressorStage + kShaftConstant;
		}

		public double engineKunduCoefficient3(){
			return engineKunduCoefficient3(_numberOfCompressorStage,
					_numberOfShaft);
		}

		/**
		 * Method that returns the cost per block hour of engine maintenance's labor, according
		 * to Kundu.
		 * 
		 * @param seaLevelStaticThrust (M_Tons)
		 * @param manHourLaborRate (USD/hr)
		 * @return (USD/hr) Cost per block hour of engine maintenance's labor
		 * @author AC
		 */
		private double engineLaborKundu(double seaLevelStaticThrust, double manHourLaborRate){
			double C1 = engineKunduCoefficient1();
			double C3 = engineKunduCoefficient3();

			return 0.21*manHourLaborRate*C1*C3*(Math.pow( 1 + seaLevelStaticThrust, 0.4));
		}

		private double engineLaborKundu(Amount<Force> seaLevelStaticThrust, double manHourLaborRate){
			return engineLaborKundu((seaLevelStaticThrust.doubleValue(NonSI.KILOGRAM_FORCE))/1000.0,
					_manHourLaborRate);
		}

		public double engineLaborKundu(){
			return engineLaborKundu(_seaLevelStaticThrust,
					_manHourLaborRate);
		}

		/**
		 * Method that returns the cost per block hour of engine material's maintenance, according
		 * to Kundu.
		 * 
		 * @param seaLevelStaticThrust (M_Tons)
		 * @return (USD/hr)
		 * @author AC
		 */
		private double engineMaterialKundu(double seaLevelStaticThrust){
			double engineMaterialCost = 0.0;
			double C1 = engineKunduCoefficient1();
			double C2 = engineKunduCoefficient2();
			double C3 = engineKunduCoefficient3();

			return 2.56*(Math.pow( 1 + seaLevelStaticThrust, 0.8))*
					C1*(C2+C3);
		}

		private double engineMaterialKundu(Amount<Force> seaLevelStaticThrust){
			return engineMaterialKundu((seaLevelStaticThrust.doubleValue(NonSI.KILOGRAM_FORCE))/1000.0);
		}

		public double engineMaterialKundu(){
			return engineMaterialKundu(_seaLevelStaticThrust);
		}

		/**
		 * The method calculates the total hour maintenance cost according to Kundu.
		 *  
		 * @param numberOfEngines
		 * @param blockTime (hr)
		 * @param flightTime (hr)
		 * @return (USD/hr) Total hour maintenance cost
		 * @author AC
		 */
		private double kundu(int numberOfEngines, double blockTime, double flightTime){
			double totAirframeMaintenance = airframeLaborKundu() + airframeMaterialKundu();
			double totEngineMaintenance = numberOfEngines * 
					(engineLaborKundu() + engineMaterialKundu())*
					((blockTime + 1.3)/(flightTime));
			double totMaintenanceCost = totAirframeMaintenance + totEngineMaintenance;

			_methodsMap.put(MethodEnum.KUNDU, totMaintenanceCost);
			return totMaintenanceCost;
		}

		private double kundu(int numberOfEngines, Amount<Duration> blockTime, Amount<Duration> flightTime){
			return kundu(numberOfEngines,
					blockTime.doubleValue(NonSI.HOUR),
					flightTime.doubleValue(NonSI.HOUR));
		}

		public double kundu(){
			return kundu(_numberOfEngines,
					_blockTime,
					_flightTime);
		}

		private double jenkinsonCostsAvailable(int numberOfEngines, double engineMaintLaborCost,
				double engineMaintMaterialCost, double airframeMaintLaborCost,
				double airframeMaintMaterialCost){
			return numberOfEngines*(engineMaintLaborCost+engineMaintMaterialCost)
					+  airframeMaintLaborCost + airframeMaintMaterialCost;
		}

		public double jenkinsonCostsAvailable(){
			return jenkinsonCostsAvailable(_numberOfEngines,
					_engineMaintLaborCost,
					_engineMaintMaterialCost,
					_airframeMaintLaborCost,
					_airframeMaintMaterialCost);
		}

		/**
		 * The method calculates the hour airframe maintenance cost according to the statistical law
		 * suggested by Jenkinson
		 * 
		 * @param OEM (M_Tons) Operative Empty Mass
		 * @return (USD/hr) Hour airframe maintenance cost
		 * @author AC
		 */
		private double airframeMaintenanceCostJenkinson(double OEM){
			return 175. + 4.1 * OEM;
		}

		private double airframeMaintenanceCostJenkinson(Amount<Mass> OEM){
			return airframeMaintenanceCostJenkinson(OEM.doubleValue(NonSI.METRIC_TON));
		}

		public double airframeMaintenanceCostJenkinson(){
			return airframeMaintenanceCostJenkinson(_OEM);
		}

		/**
		 * The method calculates the hour engine maintenance cost according to the statistical law
		 * suggested by Jenkinson
		 *  
		 * @param seaLevelStaticThrust (KN)
		 * @return (USD/hr) Hour engine maintenance cost
		 * @author AC
		 */
		private double engineMaintenanceCostJenkinson(double seaLevelStaticThrust){
			return 0.29 * seaLevelStaticThrust;
		}

		private double engineMaintenanceCostJenkinson(Amount<Force> seaLevelStaticThrust){
			return engineMaintenanceCostJenkinson((seaLevelStaticThrust.doubleValue(SI.NEWTON))/1000);
		}

		public double engineMaintenanceCostJenkinson(){
			return engineMaintenanceCostJenkinson(_seaLevelStaticThrust);
		}

		/**
		 * Method that returns the maintenance total cost per block hour, according to Jenkinson.
		 * 
		 * @param numberOfEngine 
		 * @return (USD/hr) Maintenance total cost per block hour
		 * @author AC
		 */
		private double jenkinson(int numberOfEngine){
			double engineMaintenanceCost = engineMaintenanceCostJenkinson();
			double airframeMaintCost = airframeMaintenanceCostJenkinson();
			double maintenanceCost = numberOfEngine*engineMaintenanceCost + airframeMaintCost;

			_methodsMap.put(MethodEnum.JENKINSON, maintenanceCost);

			return maintenanceCost;
		}

		public double jenkinson(){
			return jenkinson(_numberOfEngines);
		}

		/**
		 * Method that calculates the number of labor manhour per flight cycle, according to Sforza.
		 * 
		 * @param OEM Operative Empty Mass in pounds
		 * @return Number of labor manhour per flight cycle.
		 * @author AC
		 */
		private double numberOfLabManHourPerFlightCycleSforza(double OEM){
			return 6. + 0.05 * OEM/1000. - 630./(OEM/1000.+120.);		
		}

		private double numberOfLabManHourPerFlightCycleSforza(Amount<Mass> OEM){
			return numberOfLabManHourPerFlightCycleSforza(OEM.doubleValue(NonSI.POUND));
		}

		public double numberOfLabManHourPerFlightCycleSforza(){
			return numberOfLabManHourPerFlightCycleSforza(_OEM);
		}

		/**
		 * Method that returns the cost per block hour of airframe maintenance's labor according
		 * to Sforza.
		 * 
		 * @param flightTime (hr)
		 * @param blockTime (hr)
		 * @param manHourLaborRate (USD/hr) Labor manhour rate
		 * @param cruiseSpeed (Mach)
		 * @return Cost per block hour of airframe maintenance's labor.
		 * @author AC
		 */
		private double airframeMaintLaborSforza(double flightTime, double blockTime,
				double manHourLaborRate, double cruiseSpeed){
			double numberOfLabManHourPerFlightCycle = numberOfLabManHourPerFlightCycleSforza();
			double numberOfLabManHourPerFlightHour = 0.59 * numberOfLabManHourPerFlightCycle;

			return (numberOfLabManHourPerFlightHour*flightTime +
					numberOfLabManHourPerFlightCycle) *
					manHourLaborRate * Math.sqrt(cruiseSpeed)/
					blockTime;
		}

		private double airframeMaintLaborSforza(Amount<Duration> flightTime, Amount<Duration> blockTime,
				double manHourLaborRate, Amount<Velocity> cruiseSpeed){

			return airframeMaintLaborSforza(flightTime.doubleValue(NonSI.HOUR),
					blockTime.doubleValue(NonSI.HOUR),
					manHourLaborRate,
					cruiseSpeed.doubleValue(NonSI.MACH)) ;
		}

		public double airframeMaintLaborSforza(){

			return airframeMaintLaborSforza(_flightTime,
					_blockTime,
					_manHourLaborRate,
					_cruiseSpeed);
		}

		/**
		 * Method that calculates the cost per block hour of the airframe material's maintenance according
		 * to Sforza.
		 * 
		 * @param aircraftCost (USD) Total aircraft cost
		 * @param numberOfEngines 
		 * @param singleEngineCost (USD)
		 * @param flightTime (hr)
		 * @param blockTime (hr)
		 * @return Cost per block hour of airframe material's maintenance.
		 * @author AC
		 */
		private double airframeMaintMaterialSforza(double aircraftCost, int numberOfEngines,
				double singleEngineCost, double flightTime, double blockTime){
			return (aircraftCost - numberOfEngines*singleEngineCost)*
					(3.08*flightTime + 6.24)/ (blockTime *(Math.pow(10.0, 6)));
		}

		private double airframeMaintMaterialSforza(double aircraftCost, int numberOfEngines,
				double singleEngineCost, Amount<Duration> flightTime, Amount<Duration> blockTime){

			return airframeMaintMaterialSforza(aircraftCost,
					numberOfEngines,
					singleEngineCost,
					flightTime.doubleValue(NonSI.HOUR),
					blockTime.doubleValue(NonSI.HOUR));
		}

		public double airframeMaintMaterialSforza(){

			return airframeMaintMaterialSforza(_aircraftCost,
					_numberOfEngines,
					_singleEngineCost,
					_flightTime,
					_blockTime);
		}

		/**
		 * Method that returns the cost per block hour of engine maintenance's labor, according
		 * to Sforza.
		 * 
		 * @param manHourLaborRate (USD/hr) Labor manhour rate
		 * @param numberOfEngines
		 * @param blockTime
		 * @param engineTOFactor This parameter is:
		 * 											- Take Off thrust in pounds for turbofans
		 * 											- Take Off power in horsepower for turboprops
		 * @param jConst This parameter is a constant, its value is:
		 * 															- 0 for turbofans
		 * 															- 1 for turboprops
		 * @param flightTime (hr)
		 * @return Cost per block hour of the engine maintenance's labor (USD/hr) 
		 * @author AC
		 */
		private double engineMaintLaborSforza(double manHourLaborRate, int numberOfEngines,
				double blockTime, double engineTOFactor, int jConst, double flightTime){
			return manHourLaborRate * numberOfEngines / blockTime *
					((0.6 + 0.027 * (engineTOFactor/1000.0)) * Math.pow(1.08, jConst) * flightTime +
							(0.065 + 0.03 * (engineTOFactor/1000.0)));
		}

		private double engineMaintLaborSforza(double manHourLaborRate, int numberOfEngines,
				Amount<Duration> blockTime, Amount engineTOFactor,
				Amount<Duration> flightTime){

			double engineTOFactorValue = 0.0;
			int jConst = 0;

			if (_theAircraft.getPowerPlant().get_engineList().get(0).equals(EngineTypeEnum.TURBOFAN)){
				jConst = 0;
				engineTOFactorValue = engineTOFactor.doubleValue(NonSI.POUND_FORCE);
			}
			if (_theAircraft.getPowerPlant().get_engineList().get(0).equals(EngineTypeEnum.TURBOPROP)){
				jConst = 1;
				engineTOFactorValue = engineTOFactor.doubleValue(NonSI.HORSEPOWER);
			}

			return engineMaintLaborSforza(manHourLaborRate,
					numberOfEngines,
					blockTime.doubleValue(NonSI.HOUR),
					engineTOFactorValue,
					jConst,
					flightTime.doubleValue(NonSI.HOUR));
		}

		public double engineMaintLaborSforza(){

			return engineMaintLaborSforza(_manHourLaborRate,
					_numberOfEngines,
					_blockTime,
					_engineTOFactor,
					_flightTime);
		}

		/**
		 * Method that returns the cost per block hour of engine material's maintenance  according
		 * to Sforza.
		 * 
		 * @param numberOfEngines
		 * @param singleEngineCost (USD)
		 * @param flightTime (hr)
		 * @param blockTime (hr)
		 * @return Cost per block hour of the engine material's maintenance (USD/hr)
		 * @author AC
		 */
		private double engineMaintMaterialSforza(int numberOfEngines, double singleEngineCost, 
				double flightTime, double blockTime){
			double engineMaintMaterialCost = 0.0;

			engineMaintMaterialCost = 2 * numberOfEngines * (singleEngineCost/100000.0) * 
					(1.25*flightTime + 1) / blockTime;

			return engineMaintMaterialCost;
		}

		private double engineMaintMaterialSforza(int numberOfEngines, double singleEngineCost, 
				Amount<Duration> flightTime, Amount<Duration> blockTime){

			return engineMaintMaterialSforza(numberOfEngines,
					singleEngineCost, 
					flightTime.doubleValue(NonSI.HOUR), 
					blockTime.doubleValue(NonSI.HOUR)); 
		}

		public double engineMaintMaterialSforza(){

			return engineMaintMaterialSforza(_numberOfEngines,
					_singleEngineCost, 
					_flightTime, 
					_blockTime);
		}

		/**
		 * Method that returns the maintenance total cost per block hour, according to Sforza.
		 *  
		 * @return maintenanceCost (USD/hr)
		 * @author AC
		 */
		public double sforza(){
			double engineMaintenanceCost = 0.0;
			double airframeMaintCost = 0.0;
			double maintenanceCost = 0.0;

			engineMaintenanceCost = engineMaintLaborSforza() + engineMaintMaterialSforza();
			airframeMaintCost = airframeMaintLaborSforza() + airframeMaintMaterialSforza();

			maintenanceCost = engineMaintenanceCost + airframeMaintCost;

			_methodsMap.put(MethodEnum.SFORZA, maintenanceCost);

			return maintenanceCost;		
		}

		public void allMethods(){
			kundu();
			jenkinson();
			sforza();
		}

	}//end-of-class CalcMaintenanceCosts

	public class CalcFuelAndOilCharges extends InnerCalculator{

		/**
		 * Method that calculates the hourly cost of fuel according to Kundu.
		 * 
		 * @param blockFuelVolume Volume of fuel consumed during the trip (USGal)
		 * @param fuelVolumetricCost Cost of fuel per volume unit (USD/USGal)
		 * @param blockTime (hr)
		 * @return Hour cost of fuel (USD/hr)
		 * @author AC
		 */
		private double kundu(double blockFuelVolume, double fuelVolumetricCost, double blockTime){
			double fuelCharges = 0.0;

			fuelCharges = blockFuelVolume * fuelVolumetricCost / blockTime;

			_methodsMap.put(MethodEnum.KUNDU, fuelCharges);

			return fuelCharges;
		}

		private double kundu(Amount<Volume> blockFuelVolume, double fuelVolumetricCost, Amount<Duration> blockTime){

			return kundu(blockFuelVolume.doubleValue(NonSI.GALLON_LIQUID_US),
					fuelVolumetricCost,
					blockTime.doubleValue(NonSI.HOUR));
		}

		public double kundu(){

			return kundu(_blockFuelVolume,
					_fuelVolumetricCost,
					_blockTime);
		}

		/**
		 * Method that return  the hourly fuel cost according to Jenkinson.
		 * 
		 * @param hourVolumetricFuelConsumption Fuel consumption per hour in US Gallons (USGal/hr)
		 * @param fuelVolumetricCost Fuel cost per volume unit (USD/USGal)
		 * @return Hour cost of fuel
		 * @author AC
		 */
		private double jenkinson(double numberOfEngines, double hourVolumetricFuelConsumption, double fuelVolumetricCost){
			double fuelCharges = 0.0;

			fuelCharges = numberOfEngines * hourVolumetricFuelConsumption * fuelVolumetricCost;

			_methodsMap.put(MethodEnum.JENKINSON, fuelCharges);

			return fuelCharges;
		}

		public double jenkinson(){

			return jenkinson(_numberOfEngines,
					_hourVolumetricFuelConsumption,
					_fuelVolumetricCost);
		}

		/**
		 * Method that calculates the fuel and oil hourly cost, according to Sforza.
		 * 
		 * @param blockFuelVolume Volume of block fuel in US Gallons
		 * @param fuelVolumetricCost Fuel cost per US Gallon (USD/USGal)
		 * @param numberOfEngines 
		 * @param oilMassCost Cost of oil unit mass (USD/lb)
		 * @param blockTime (hr)
		 * @return Hourly cost of fuel and oil (USD/hr)
		 * @author AC
		 */
		private double sforza(double blockFuelVolume, double fuelVolumetricCost, int numberOfEngines,
				double oilMassCost, double blockTime){
			double fuelAndOilCharges = 0.0;

			fuelAndOilCharges = 1.02 * (blockFuelVolume*fuelVolumetricCost +
					0.135 * numberOfEngines * oilMassCost * blockTime) / blockTime;

			_methodsMap.put(MethodEnum.SFORZA, fuelAndOilCharges);

			return fuelAndOilCharges;
		}

		private double sforza(Amount<Volume> blockFuelVolume, double fuelVolumetricCost, int numberOfEngines,
				double oilMassCost, Amount<Duration> blockTime){

			return sforza(blockFuelVolume.doubleValue(NonSI.GALLON_LIQUID_US),
					fuelVolumetricCost, 
					numberOfEngines,
					oilMassCost, 
					blockTime.doubleValue(NonSI.HOUR));
		}

		public double sforza(){

			return sforza(_blockFuelVolume,
					_fuelVolumetricCost, 
					_numberOfEngines,
					_oilMassCost, 
					_blockTime);
		}

		public void allMethods(){
			kundu();
			jenkinson();
			sforza();
		}

	}//end-of-class CalcFuelAndOilCharges

	public Map<MethodEnum, Double> get_totalTripChargesMap() {
		return _totalTripChargesMap;
	}

	public CalcLandingFees get_calcLandingFees() {
		return _calcLandingFees;
	}

	public CalcNavigationalCharges get_calcNavigationalCharges() {
		return _calcNavigationalCharges;
	}

	public CalcGroundHandlingCharges get_calcGroundHandlingCharges() {
		return _calcGroundHandlingCharges;
	}

	public CalcMaintenanceCosts get_calcMaintenanceCosts() {
		return _calcMaintenanceCosts;
	}

	public CalcFuelAndOilCharges get_calcFuelAndOilCharges() {
		return _calcFuelAndOilCharges;
	}

}
