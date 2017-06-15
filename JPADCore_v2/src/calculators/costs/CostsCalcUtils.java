package calculators.costs;

import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import parser.ExprParser.start_return;
import standaloneutils.MyUnits;

public class CostsCalcUtils {

	/**
	 * Calculates the aircraft total investment as the sum of airframe price, engines 
	 * price and relative spares costs.
	 * 
	 * @author AC
	 * @param airframeCost (USD)
	 * @param singleEngineCost (USD)
	 * @param numberOfEngines
	 * @param sparesAirframePerCosts airframe relative spares costs w.r.t. the airframe cost (0.1 typical) 
	 * @param sparesEnginesPerCosts engines relative spares costs w.r.t. the engines cost (0.3 typical) 
	 * @return _totalInvestments
	 */
	public static Amount<Money> calcTotalInvestments(
			Amount<Money> airframeCost,
			Amount<Money> singleEngineCost,
			int numberOfEngines,
			double sparesAirframePerCosts,
			double sparesEnginesPerCosts){

		return Amount.valueOf(
				airframeCost.doubleValue(Currency.USD) * (1 + sparesAirframePerCosts) + 
				numberOfEngines * singleEngineCost.doubleValue(Currency.USD) * (1 + sparesEnginesPerCosts),
				Currency.USD)
				;
	}
	
	/**
	 * Method that calculates the aircraft manufacturer cost... TODO: remove it!!!
	 * 
	 * @author AC
	 * @param airframeCost
	 * @param singleEngineCost
	 * @param numberOfEngines
	 * @return _aircraftCost
	 */
	public static double calcAircraftCost(double airframeCost,
			double singleEngineCost,
			int numberOfEngines){

		return airframeCost + numberOfEngines * singleEngineCost;
	}
	
	/**
	 * Methods that implements Sforza statistical formula that relates the aircraft operative 
	 * empty weight (in pounds) with the cost in USD2012.
	 * 
	 * @author AC
	 * @param aircraft is the MyAircraft object that contains the data of the considered aircraft
	 * @return _aircraftCostSforza aircraft cost according to Sforza statistical law
	 */
	public static double calcAircraftCostSforza(double OEM){
		return 425.0 * Erf.erf((OEM - (10000.)) / (450000.)) * 1000000.;
	}
	
	/**
	 * 
	 * @author AC
	 *
	 * @param cruiseThrust
	 * @param cruiseSpecificFuelConsumption
	 * @return
	 */
	public static double singleEngineCostSforza(double cruiseThrust, double cruiseSpecificFuelConsumption){
		//TODO: test this equation on some known engine
		return 1.2* (1 + (FastMath.pow(cruiseThrust, 0.088) /
				FastMath.pow(cruiseSpecificFuelConsumption, 2.58)));
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param OEM
	 * @return
	 */
	public static double calcAircraftCostSforza(Amount<Mass> OEM){
		return CostsCalcUtils.calcAircraftCostSforza(OEM.doubleValue(NonSI.POUND));
	}
	
	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param cruiseThrust
	 * @param cruiseSpecificFuelConsumption
	 * @return
	 */
	public static double singleEngineCostSforza(Amount<Force> cruiseThrust,
			double cruiseSpecificFuelConsumption){
		return singleEngineCostSforza(cruiseThrust.doubleValue(NonSI.POUND_FORCE),
				cruiseSpecificFuelConsumption);
	}
	
	/**
	 * Method that returns the inflaction factor w.r.t. the $2012
	 * 
	 * @author AC
	 * @param actualCPIFactor CPI factor of the year that is taken into account
	 * @return 
	 */
	public static double calcInflationFactor(double actualCPIFactor){
		//TODO: Control how and when to use this method
		double cpi2012factor = 117.6;
		return actualCPIFactor/cpi2012factor;
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
	@SuppressWarnings("unchecked")
	public static Amount<?> calcDepreciationAEA(
			Amount<Money> totalInvestments, 
			Amount<?> utilization,
			Amount<Duration> lifeSpan, 
			double residualValue
			) {

		return Amount.valueOf(
				(1.0 - residualValue)*totalInvestments.doubleValue(Currency.USD)
					/(lifeSpan.doubleValue(NonSI.YEAR)*utilization.doubleValue(MyUnits.HOURS_PER_YEAR)),
				MyUnits.USD_PER_HOUR
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
	@SuppressWarnings("unchecked")
	public static Amount<?> calcDepreciationATA(
			Amount<Money> totalCost,
			Amount<?> utilization,	
			double residualValue,
			Amount<Money> singleEngineCost, 
			int numberOfEngine,
			Amount<Duration> lifeSpan
			) {

		return Amount.valueOf(
				(totalCost.doubleValue(Currency.USD)*(1-residualValue)-0.3*numberOfEngine*singleEngineCost.doubleValue(Currency.USD))
				/(utilization.doubleValue(MyUnits.HOURS_PER_YEAR)*lifeSpan.doubleValue(NonSI.YEAR)),
				MyUnits.USD_PER_HOUR
				);
	}
	
	/**
	 * 
	 * @param range (<= 3000nmi == SHORT / MEDIUM) (>3000 == LONG)
	 * @return landingChargeConstant (USD/ton)
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcLandingChargeConstant(Amount<Length> range){
		if (range.doubleValue(NonSI.NAUTICAL_MILE)<= 3000) 
			return Amount.valueOf(7.8, MyUnits.USD_PER_TON);
		else
			return Amount.valueOf(6.0, MyUnits.USD_PER_TON);
	}

	/**
	 * 
	 * @param range (<= 3000nmi == SHORT / MEDIUM) (>3000 == LONG)
	 * @return NavigationChargeConstant (USD/(Km*sqrt(ton)))
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcNavigationChargeConstant(Amount<Length> range){
		if (range.doubleValue(NonSI.NAUTICAL_MILE)<= 3000) 
			return Amount.valueOf(0.50, MyUnits.USD_PER_KM_SQRT_TON);
		else
			return Amount.valueOf(0.17, MyUnits.USD_PER_KM_SQRT_TON);
	}
	

	/**
	 * 
	 * @param range (<= 3000nmi == SHORT / MEDIUM) (>3000 == LONG)
	 * @return groundHandlingChargeConstant (USD/ton)
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcGroundHandlingChargeConstant(Amount<Length> range){
		if (range.doubleValue(NonSI.NAUTICAL_MILE)<= 3000) 
			return Amount.valueOf(100, MyUnits.USD_PER_TON);
		else
			return Amount.valueOf(103, MyUnits.USD_PER_TON);
	}
	
	/**
	 * Method that calculates the insurance premium per block hour according to AEA. 
	 * 
	 * @param aircraftCost aircraft manufacturer cost in USD (airframe + engine)
	 * @param utilization annual utilization in block hour
	 * @param annualInsurancePremiumRate (non-dimensional) Default 0.005. Other typical values
	 * 									range from 0.01 to 0.03
	 * @return insurance (USD/block hr)
	 * @author AC
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcInsuranceAEA(Amount<Money> aircraftCost, 
			Amount<?> utilization, double annualInsurancePremiumRate) {

		return Amount.valueOf(
				annualInsurancePremiumRate*aircraftCost.doubleValue(Currency.USD)/utilization.doubleValue(MyUnits.HOURS_PER_YEAR),
				MyUnits.USD_PER_HOUR
				);

	}
	
	/**
	 * Method that calculates the insurance premium per block hour according to ATA. 
	 * 
	 * @param aircraftCost aircraft manufacturer cost in USD (airframe + engine)
	 * @param utilization annual utilization in block hour
	 * @param annualInsurancePremiumRate (non-dimensional) Default 0.005. Other typical values
	 * 									range from 0.01 to 0.03
	 * @return insurance (USD/statute mile)
	 * @author AC
	 */
	public static double calcInsuranceATA(double aircraftCost, 
			double utilization, double annualInsurancePremiumRate, Amount<Velocity> blockSpeed) {

		return annualInsurancePremiumRate*aircraftCost/(utilization*blockSpeed.doubleValue(NonSI.MILES_PER_HOUR));

	}
	
	/**
	 * Method that calculate the loan interest repayments per block hour according to AEA (Jenkinson) 
	 * 
	 * @param totalInvestments
	 * @param utilization
	 * @param annualInterestRate
	 * @return interest (USD/hr)
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcInterestAEA(Amount<Money> totalInvestments, 
			Amount<?> utilization, double annualInterestRate) {

		return Amount.valueOf(
				annualInterestRate*totalInvestments.doubleValue(Currency.USD)/utilization.doubleValue(MyUnits.HOURS_PER_YEAR), 
				MyUnits.USD_PER_HOUR
				);

	}
	
	

	@SuppressWarnings("unchecked")
	public static Amount<?> calcDOCCapitalAEA(Amount<?> _depreciation, Amount<?>_insurance,
			Amount<?> _interest){
		
		return Amount.valueOf(
				_depreciation.doubleValue(MyUnits.USD_PER_HOUR) + _interest.doubleValue(MyUnits.USD_PER_HOUR) + _insurance.doubleValue(MyUnits.USD_PER_HOUR),
				MyUnits.USD_PER_HOUR
				);
	}
	
	/**
	 * Method that calculate the total crew hour cost according to AEA
	 * 
	 *@param cockpitCrewNumber
	 *@param cockpitLabourRate (USD/hr)
	 * 
	 * @return cockpitCrewCost (USD/hr)
	 */
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calcCockpitCrewCostAEA(int cockpitCrewNumber, Amount<?> cockpitLabourRate) {

		return  Amount.valueOf(
				cockpitLabourRate.doubleValue(MyUnits.USD_PER_HOUR)*cockpitCrewNumber,
				MyUnits.USD_PER_HOUR
				);

	}
	
	
	/**
	 * Method that calculate the total crew hour cost according to AEA
	 * 
	 *@param cabinCrewNumber
	 *@param cabinLabourRate (USD/hr)
	 * 
	 * @return cabinCrewCost (USD/hr)
	 */
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calcCabinCrewCostAEA(int cabinCrewNumber, Amount<?> cabinLabourRate) {

		return  Amount.valueOf(
				cabinLabourRate.doubleValue(MyUnits.USD_PER_HOUR)*cabinCrewNumber,
				MyUnits.USD_PER_HOUR
				);

	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calcDOCCrew(Amount<?> _cabinCrewCost, Amount<?>_cockpitCrewCost){
		
		return Amount.valueOf(
				_cabinCrewCost.doubleValue(MyUnits.USD_PER_HOUR) + _cockpitCrewCost.doubleValue(MyUnits.USD_PER_HOUR),
				MyUnits.USD_PER_HOUR
				);
	}
	
	
	/**
	 * Method that calculate the cockpit crew hour cost according to Sforza. It varies with the crew 
	 * element number (2 or 3) and the type of engines, through the use of a constant (aircraftTypeConst),
	 * as it is indicated below:
	 * 			1: Turbofan 2-man crew 		---> aircraftTypeConst = 697					
	 * 			2: Turboprop 2-man crew;	---> aircraftTypeConst = 439
	 * 			3: Turbofan 3-man crew;		---> aircraftTypeConst = 836.4
	 * 
	 * @param cabinCrewNumber
	 * @param MTOM
	 * @param _theAircraft
	 * @return crewCost (USD/hr)
 
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcCockpitCrewCostATA(int cabinCrewNumber, Amount<Mass> MTOM, Aircraft _theAircraft) {

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

		return Amount.valueOf(
					(0.349*MTOM.doubleValue(NonSI.POUND)/1000.0) + aircraftTypeConst,
					MyUnits.USD_PER_HOUR);

	}
	
	/**
	 * Method that calculate the total crew hour cost according to ATA method (see "DOC+I method")
	 * 
	 * @param numberOfSeats
	 * @return crewCost (USD/hr)
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcCabinCrewCostATA(int numberOfSeats){

		return Amount.valueOf(
				60*numberOfSeats/35,
				MyUnits.USD_PER_HOUR);

	}
	
	/**
	 * 
	 * @param fuelPrice   ($/barrel) 
	 * @param fuelMass    (Kg)  
	 * @return Fuel Cost ($/flight)
	 */
	
	@SuppressWarnings("unchecked")
	public static Amount<?> calcDOCFuel(Amount<?> fuelPrice, Amount<Mass> fuelMass) {

		// Jet fuel density = 0.810 kg/l
		Amount<Volume> fuelVolume = fuelMass.divide(0.810).to(MyUnits.BARREL);
		
		return  Amount.valueOf(
				fuelPrice.doubleValue(MyUnits.USD_PER_BARREL)*fuelVolume.doubleValue(MyUnits.BARREL),
				MyUnits.USD_PER_FLIGHT
				);

	}
	
	
}
