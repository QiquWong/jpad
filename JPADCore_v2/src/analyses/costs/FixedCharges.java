package analyses.costs;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.analysismodel.InnerCalculator;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;

public class FixedCharges {

	private Aircraft _theAircraft;

	private double _utilization, _totalInvestments, _aircraftCost, _singleEngineCost,
	_residualValue,	_lifeSpan, _annualInterestRate, _annualInsurancePremiumRate, _singleCabinCrewHrCost,
	_singleflightCrewHrCost;	

	private int _cabinCrewNumber,	_flightCrewNumber, _numberOfEngines, _typeOfAircraft;

	private Amount<Mass> _MTOM;

	private CalcDepreciation _calcDepreciation;
	private CalcInterest _calcInterest;
	private CalcInsurance _calcInsurance;
	private CalcCrewCosts _calcCrewCosts;

	private Map<MethodEnum, Double> _totalFixedChargesMap;

	/**
	 * MyFixedCharges constructor
	 * @param aircraft object of MyAircraft class
	 * @param utilization aircraft utilization in block hour per year
	 * @param totalInvestments price of airframe + engines + spares in USD
	 * @param lifeSpan life span on year
	 * @author AC
	 */
	public FixedCharges(Aircraft aircraft, Costs costs) {
		_theAircraft = aircraft;

		_calcDepreciation = new CalcDepreciation();
		_calcInterest = new CalcInterest();
		_calcInsurance = new CalcInsurance();
		_calcCrewCosts = new CalcCrewCosts();
		_totalFixedChargesMap = new TreeMap<MethodEnum, Double>();
	}

	public void initialize(Aircraft aircraft, Costs costs) {
		initialize(costs.getUtilization(), costs.getTotalInvestments(), costs.getAircraftCost(),
				costs.getSingleEngineCost(),
				aircraft.getLifeSpan(), costs.getResidualValue(), costs.getAnnualInterestRate(),
				costs.getAnnualInsurancePremiumRate(), aircraft.getCabinConfiguration().getCabinCrewNumber().intValue(),
				aircraft.getCabinConfiguration().getFlightCrewNumber().intValue(), aircraft.getPowerPlant().getEngineNumber(), 
				costs.getSingleCabinCrewHrCost(),
				costs.getSingleflightCrewHrCost(), aircraft.getTheWeights().get_MTOM());		
	}
	
	/**
	 * Initialize an object of MyFixedCharges class
	 * 
	 * @param utilization annual utilization in block hours
	 * @param totalInvestments the total investment airframe + engines + spares costs (USD)
	 * @param aircraftCost aircraft manufacturer airframe + engines costs (USD)
	 * @param aircraftCostSforza Cost of a single unit, calculated trough the Sforza statistical formula
	 * @param singleEngineCost Cost of a single engine
	 * @param lifeSpan operating life in years
	 * @param residualValue rate of total investment after the life span (0.1 typical value after 14/16 years)
	 * @param annualInterestRate (non-dimensional) current base national interest (typical 0.53/0.54)
	 * @param annualInsurancePremiumRate (non dimensional) typical Kundu: 0.005; Jenkinson: 0.01/0.03.
	 * @param cabinCrewNumber Number of cabin crew member
	 * @param flightCrewNumber Number of flight crew member
	 * @param numberOfEngines 
	 * @param singleCabinCrewHrCost Hour cost of a single cabin crew member
	 * @param singleflightCrewHrCost Hour cost of a single flight crew member
	 * @param MTOM
	 * @author AC
	 */
	public void initialize(double utilization,
			double totalInvestments,
			double aircraftCost,
			double singleEngineCost,
			double lifeSpan,
			double residualValue,
			double annualInterestRate,
			double annualInsurancePremiumRate,
			int cabinCrewNumber,
			int flightCrewNumber,
			int numberOfEngines,
			double singleCabinCrewHrCost,
			double singleflightCrewHrCost,
			Amount <Mass> MTOM
			){		
		_utilization = utilization;
		_totalInvestments = totalInvestments;
		_aircraftCost = aircraftCost;
		_singleEngineCost = singleEngineCost;
		_residualValue = residualValue; 
		_lifeSpan = lifeSpan; 
		_annualInterestRate = annualInterestRate;
		_annualInsurancePremiumRate = annualInsurancePremiumRate;
		_cabinCrewNumber = cabinCrewNumber;
		_flightCrewNumber = flightCrewNumber;
		_numberOfEngines = numberOfEngines;
		_singleCabinCrewHrCost = singleCabinCrewHrCost;
		_singleflightCrewHrCost = singleflightCrewHrCost;
		_MTOM = MTOM;
	}

	/**
	 * Calculate the total sum of the calculated fixed charges, then put them in a Map
	 * @author AC
	 */
	private void calcTotalFixedCharges(){

		_totalFixedChargesMap.put(MethodEnum.KUNDU,
				_calcDepreciation.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcInterest.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcInsurance.get_methodsMap().get(MethodEnum.KUNDU)+
				_calcCrewCosts.get_methodsMap().get(MethodEnum.KUNDU)
				);

		_totalFixedChargesMap.put(MethodEnum.JENKINSON,
				_calcDepreciation.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcInterest.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcInsurance.get_methodsMap().get(MethodEnum.JENKINSON)+
				_calcCrewCosts.get_methodsMap().get(MethodEnum.JENKINSON)
				);

		_totalFixedChargesMap.put(MethodEnum.SFORZA,
				_calcDepreciation.get_methodsMap().get(MethodEnum.SFORZA)+
				_calcInsurance.get_methodsMap().get(MethodEnum.SFORZA)+
				_calcCrewCosts.get_methodsMap().get(MethodEnum.SFORZA)
				);

	}

	public void calculateAll() {

		_calcDepreciation.allMethods();
		_calcInterest.allMethods();
		_calcInsurance.allMethods();
		_calcCrewCosts.allMethods();
		calcTotalFixedCharges();
	}

	/**
	 * Models the depreciation of aircraft according to Kundu, Jenkinson, Sforza.
	 *  
	 * @author AC
	 *
	 */
	public class CalcDepreciation extends InnerCalculator{

		/**
		 * Method that calculates the aircraft depreciation per block hour, according to Kundu.
		 * 
		 * @param totalInvestments USD
		 * @param utilization block hr/year
		 * @param lifeSpan year (operative life)
		 * @param residualValue rate of totalInvestments (non-dimensional)
		 * @return depreciation in USD/block hour
		 * @author AC
		 */
		private double kundu(double totalInvestments, 
				double utilization, double lifeSpan, double residualValue) {

			double depreciation = (1.0 - residualValue)*totalInvestments
					/(lifeSpan*utilization);

			_methodsMap.put(MethodEnum.KUNDU, depreciation);

			return depreciation;
		}


		public double kundu() {
			return kundu(
					_totalInvestments, 
					_utilization, 
					_lifeSpan,
					_residualValue
					);
		}

		/**
		 * Method that calculates the aircraft depreciation per block hour, according to Jenkinson.
		 * Actually it is equal to Kundu.
		 * 
		 * @param totalInvestments
		 * @param utilization
		 * @param lifeSpan
		 * @param residualValue
		 * @return depreciation in USD/block hour
		 * @author AC
		 */
		private double jenkinson(double totalInvestments, 
				double utilization, double lifeSpan, double residualValue) {

			double depreciation = (1.0 - residualValue)*totalInvestments
					/(lifeSpan*utilization);

			_methodsMap.put(MethodEnum.JENKINSON, depreciation);

			return depreciation;
		}

		public double jenkinson() {

			return jenkinson(
					_totalInvestments, 
					_utilization, 
					_lifeSpan,
					_residualValue
					);

		}

		/**
		 * Method that calculates the depreciation according to the equation suggested by Sforza
		 * 
		 * @param totalCost the total cost of the single aircraft calculated through the Sforza statistical
		 * 			equation.
		 * @param utilization
		 * @param residualValue
		 * @param singleEngineCost cost of a single engine
		 * @param numberOfEngine
		 * @return depreciation in USD/hr
		 * @author AC
		 */
		private double sforza(double totalCost,	double utilization,	double residualValue,
				double singleEngineCost, int numberOfEngine, double lifeSpan) {

			double depreciation = (totalCost*(1-residualValue)-0.3*numberOfEngine*singleEngineCost)/
					(utilization*lifeSpan);

			_methodsMap.put(MethodEnum.SFORZA, depreciation);

			return depreciation;
		}
		
		public double sforza() {
			return sforza(_aircraftCost,
					_utilization,
					_residualValue,
					_singleEngineCost,
					_numberOfEngines,
					_lifeSpan);
		}

		public void allMethods() {
			kundu();
			jenkinson();
			sforza();
		}

	}// end-of-class CalcDepreciation

	public class CalcInterest extends InnerCalculator{

		/**
		 * Method that calculate the loan interest repayments per block hour. Suggested by Kundu.
		 * 
		 * @param totalInvestments is the total investment in USD (airframe + engine + spares prices)
		 * @param utilization is the annual utilization in block hour
		 * @param annualInterestRate current national base interest (non-dimensional). 
		 * 							 Default 0.053 suggested by Kundu.
		 * @return interest (USD/block hr)
		 * @author AC
		 */
		private double kundu(double totalInvestments, 
				double utilization, double annualInterestRate) {

			double interest = annualInterestRate*totalInvestments/utilization;

			_methodsMap.put(MethodEnum.KUNDU, interest);

			return interest;

		}

		public double kundu() {
			return kundu(
					_totalInvestments, 
					_utilization, 
					_annualInterestRate
					);
		}

		/**
		 * Method that calculate the loan interest repayments per block hour according to Jenkinson 
		 * (Actually is equal to Kundu)
		 * 
		 * @param totalInvestments
		 * @param utilization
		 * @param annualInterestRate
		 * @return interest (USD/hr)
		 */
		private double jenkinson(double totalInvestments, 
				double utilization, double annualInterestRate) {

			double interest = annualInterestRate*totalInvestments/utilization;

			_methodsMap.put(MethodEnum.JENKINSON, interest);

			return interest;

		}

		public double jenkinson() {
			return jenkinson(
					_totalInvestments, 
					_utilization, 
					_annualInterestRate
					);
		}


		public void allMethods() {
			kundu();
			jenkinson();
		}

	}// end-of-class CalcInterest

	public class CalcInsurance extends InnerCalculator{

		/**
		 * Method that calculates the insurance premium per block hour according to Kundu.
		 * 
		 * @param aircraftCost is the aircraft manufacturer cost in USD (airframe + engine)
		 * @param utilization is the annual utilization in block hour
		 * @param annualInsurancePremiumRate (non-dimensional) Default 0.005 suggested by Kundu.
		 * @return insurance (USD/block hr)
		 * @author AC
		 */
		private double kundu(double aircraftCost, 
				double utilization, double annualInsurancePremiumRate) {

			double insurance = annualInsurancePremiumRate*aircraftCost/utilization;

			_methodsMap.put(MethodEnum.KUNDU, insurance);

			return insurance;

		}

		public double kundu() {
			return kundu(
					_aircraftCost, 
					_utilization, 
					_annualInsurancePremiumRate
					);
		}

		/**
		 * Method that calculates the insurance premium per block hour according to Jenkinson. 
		 * Actually is equal to what already suggested by Kundu.
		 * 
		 * @param aircraftCost aircraft manufacturer cost in USD (airframe + engine)
		 * @param utilization annual utilization in block hour
		 * @param annualInsurancePremiumRate (non-dimensional) Default 0.005. Other typical values
		 * 									range from 0.01 to 0.03
		 * @return insurance (USD/block hr)
		 * @author AC
		 */
		private double jenkinson(double aircraftCost, 
				double utilization, double annualInsurancePremiumRate) {

			double insurance = annualInsurancePremiumRate*aircraftCost/utilization;

			_methodsMap.put(MethodEnum.JENKINSON, insurance);

			return insurance;

		}

		public double jenkinson() {
			return jenkinson(
					_aircraftCost, 
					_utilization, 
					_annualInsurancePremiumRate
					);
		}

		/**
		 * Method that calculates the insurance premium per block hour according to Jenkinson. 
		 * Actually is equal to what already suggested by Kundu.
		 * 
		 * @param totalCost the total cost of the single aircraft calculated through the Sforza statistical
		 * 			equation.
		 * @param utilization is the annual utilization in block hour
		 * @param annualInsurancePremiumRate (non-dimensional) Default 0.005. Other typical values
		 * 									range from 0.01 to 0.03
		 * @return insurance (USD/block hr)
		 * @author AC
		 */
		private double sforza(double totalCost, 
				double utilization, double annualInsurancePremiumRate) {

			double insurance = annualInsurancePremiumRate*totalCost/utilization;

			_methodsMap.put(MethodEnum.SFORZA, insurance);

			return insurance;

		}

		public double sforza() {
			return sforza(
					_aircraftCost, 
					_utilization, 
					_annualInsurancePremiumRate
					);
		}

		public void allMethods() {
			kundu();
			jenkinson();
			sforza();
		}

	} // end-of-class CalcInsurance

	public class CalcCrewCosts extends InnerCalculator{

		/**
		 * Method that calculate the total crew hour cost according to Kundu
		 * 
		 * @param cabinCrewNumber
		 * @param flightCrewNumber
		 * @param singleCabinCrewHrCost
		 * @param singleflightCrewHrCost
		 * @return  crewCost (USD/hr)
		 * @author AC
		 */
		private double kundu(double cabinCrewNumber, 
				double flightCrewNumber,
				double singleCabinCrewHrCost,
				double singleflightCrewHrCost) {

			double crewCosts = cabinCrewNumber*singleCabinCrewHrCost + 
					flightCrewNumber*singleflightCrewHrCost;

			_methodsMap.put(MethodEnum.KUNDU, crewCosts);

			return crewCosts;

		}

		public double kundu() {
			return kundu(
					_cabinCrewNumber, 
					_flightCrewNumber, 
					_singleCabinCrewHrCost,
					_singleflightCrewHrCost
					);
		}


		/**
		 * Method that calculate the total crew hour cost according to Jenkinson
		 *  (actually is equal to Kundu).
		 * 
		 * @param cabinCrewNumber
		 * @param flightCrewNumber
		 * @param singleCabinCrewHrCost
		 * @param singleflightCrewHrCost
		 * @return crewCost (USD/hr)
		 * @author AC
		 */
		private double jenkinson(double cabinCrewNumber, 
				double flightCrewNumber,
				double singleCabinCrewHrCost,
				double singleflightCrewHrCost) {

			double crewCosts = cabinCrewNumber*singleCabinCrewHrCost + 
					flightCrewNumber*singleflightCrewHrCost;

			_methodsMap.put(MethodEnum.JENKINSON, crewCosts);

			return crewCosts;

		}

		public double jenkinson() {
			return jenkinson(
					_cabinCrewNumber, 
					_flightCrewNumber, 
					_singleCabinCrewHrCost,
					_singleflightCrewHrCost
					);
		}

		/**
		 * Method that calculate the total crew hour cost according to Sforza. It varies with the cabin crew 
		 * element number (2 or 3) and the type of engines, through the use of a constant (aircraftTypeConst),
		 * as it is indicated below:
		 * 			1: Turbofan 2-man crew 		---> aircraftTypeConst = 697					
		 * 			2: Turboprop 2-man crew;	---> aircraftTypeConst = 439
		 * 			3: Turbofan 3-man crew;		---> aircraftTypeConst = 836.4
		 * @param cabinCrewNumber Number of cabin crew members
		 * @param MTOM Maximum Take-Off Mass
		 * @return crewCost (USD/hr)
		 * @author AC
		 */
		private double sforza(int cabinCrewNumber, double MTOM) {

			double crewCosts = 0.0;
			double aircraftTypeConst = 0.0;

			if (_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) && cabinCrewNumber == 2){
				aircraftTypeConst = 697.0;
			}
			else if (_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) && cabinCrewNumber == 2){
				aircraftTypeConst = 439.0;
			}
			else if (_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) && cabinCrewNumber == 3){
				aircraftTypeConst = 836.4;
			}

			crewCosts = (0.349*MTOM/1000.0) + aircraftTypeConst;

			_methodsMap.put(MethodEnum.SFORZA, crewCosts);

			return crewCosts;

		}

		private double sforza(int cabinCrewNumber, Amount<Mass> MTOM) {
			return sforza(
					_cabinCrewNumber, 
					MTOM.doubleValue(NonSI.POUND)
					);			
		}

		public double sforza() {
			return sforza(
					_typeOfAircraft, 
					_MTOM
					);
		}

		public void allMethods() {
			kundu();
			jenkinson();
			sforza();
		}

	} // end-of-class CalcCrewCosts


	public Map<MethodEnum, Double> get_totalFixedChargesMap() {
		return _totalFixedChargesMap;
	}

	public CalcDepreciation get_calcDepreciation() {
		return _calcDepreciation;
	}

	public CalcInterest get_calcInterest() {
		return _calcInterest;
	}

	public CalcInsurance get_calcInsurance() {
		return _calcInsurance;
	}

	public CalcCrewCosts get_calcCrewCosts() {
		return _calcCrewCosts;
	}

}
