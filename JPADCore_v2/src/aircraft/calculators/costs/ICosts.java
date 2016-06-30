package aircraft.calculators.costs;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;

public interface ICosts {

	void initializeAll(Aircraft aircraft);

	void initializeFinacialCostVariables(double residualValue, double annualInterestRate,
			double annualInsurancePremiumRate, double utilization, double singleEngineCost, int numberOfEngines,
			double sparesAirframePerCosts, double sparesEnginesPerCosts);

	void initializeFlightDataVariables(int cabinCrewNumber, int flightCrewNumber, double singleCabinCrewHrCost,
			double singleflightCrewHrCost, Amount<Length> range, Amount<Velocity> cruiseSpeed,
			Amount<Duration> climbDescentTime, Amount<Duration> sturtupTaxiTOTime, Amount<Duration> holdPriorToLandTime,
			Amount<Duration> landingTaxiToStopTime);

	void initializeTripChargesVariables(double landingFeesPerTon, double jenkinsonNavigationalCharges, int numberOfPax,
			double groundHandlingCostXPax);

	void initializeMaintAndEngineVariable(double manHourLaborRate, double byPassRatio, double overallPressureRatio,
			int numberOfCompressorStage, int numberOfShaft, Amount<Force> seaLevelStaticThrust, Amount<Force> thrustTO,
			Amount<Power> powerTO, double cruiseSpecificFuelConsumption);

	void initializeAvailableMaintenanceCost(double engineMaintLaborCost, double engineMaintMaterialCost,
			double airframeMaintLaborCost, double airframeMaintMaterialCost);

	void initializeFuelAndOilCunsumptionVariables(double fuelVolumetricCost, double hourVolumetricFuelConsumption,
			double oilMassCost);

	void calculateAll(Aircraft aircraft);

	double calcTotalInvestments();

	double calcAircraftCost();

	double calcAircraftCostSforza();

	Amount<Duration> calcBlockTime();

	Amount<Duration> calcCruiseTime();

	FixedCharges getTheFixedCharges();

	TripCharges getTheTripCharges();

	double getTotalInvestments();

	double getAircraftCost();

	double getUtilization();

	double getAirframeCost();

}
