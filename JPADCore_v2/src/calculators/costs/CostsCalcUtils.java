package calculators.costs;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

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
	public static Amount<?> calcDepreciationJenkinson(
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
	public static Amount<?> calcDepreciationSforza(
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
	
	
}
