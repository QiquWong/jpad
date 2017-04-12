package calculators.costs;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;
import org.jscience.physics.amount.Amount;

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
	public static double calcTotalInvestments(double airframeCost,
			double singleEngineCost,
			int numberOfEngines,
			double sparesAirframePerCosts,
			double sparesEnginesPerCosts){

		return airframeCost * (1 + sparesAirframePerCosts) + 
				numberOfEngines * singleEngineCost * (1 + sparesEnginesPerCosts);
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
	
	
	
	
	
	
	
	
	
	
	
	
}
