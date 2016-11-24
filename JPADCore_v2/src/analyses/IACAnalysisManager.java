package analyses;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.costs.ACCostsManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;

public interface IACAnalysisManager {

	public void calculateDependentVariables();
	public void doAnalysis(
			Aircraft aircraft, 
			OperatingConditions theOperatingConditions,
			String resultsFolderPath
			) throws IOException;
	public void calculateWeights(Aircraft aircraft, String resultsFolderPath);
	public void calculateBalance(Aircraft aircraft, String resultsFolderPath);
	public void calculateAerodynamics(
			OperatingConditions theOperatingConditions,
			Aircraft aircraft,
			String resultsFolderPath
			);
	public void calculatePerformances(Aircraft aircraft, String resultsFolderPath);
	public void calculateCosts(Aircraft aircraft, String resultsFolderPath);
	
	public String getId();
	public void setId(String _id);

	public Aircraft getTheAircraft();
	public void setTheAircraft(Aircraft _theAircraft);

	public Double getPositiveLimitLoadFactor();
	public void setPositiveLimitLoadFactor(Double _nLimit);

	public Double getNegativeLimitLoadFactor();
	public void setNegativeLimitLoadFactor(Double _nLimit);
	
	public Double getCruiseCL();
	public void setCruiseCL(Double _cruiseCL);

	public Amount<Length> getMaxAltitudeAtMaxSpeed();
	public void setMaxAltitudeAtMaxSpeed(Amount<Length> _maxAltitudeAtMaxSpeed);

	public Double getMachMaxCruise();
	public void setMachMaxCruise(Double _machMaxCruise);

	public Amount<Length> getAltitudeOptimumCruise();
	public void setAltitudeOptimumCruise(Amount<Length> _altitudeOptimumCruise);

	public Double getMachOptimumCruise();
	public void setMachOptimumCruise(Double _machOptimumCruise);

	public Amount<Duration> getBlockTime();
	public void setBlockTime(Amount<Duration> _blockTime);

	public Amount<Duration> getFlightTime();
	public void setFlightTime(Amount<Duration> _flightTime);

	public Double getNUltimate();
	public void setNUltimate(Double _nUltimate);

	public Amount<Velocity> getVDive();
	public void setVDive(Amount<Velocity> _vDive);

	public Amount<Velocity> getVDiveEAS();
	public void setVDiveEAS(Amount<Velocity> _vDiveEAS);

	public Double getMachDive0();
	public void setMachDive0(Double _machDive0);

	public Amount<Velocity> getVMaxCruise();
	public void setVMaxCruise(Amount<Velocity> _vMaxCruise);

	public Amount<Velocity> getVMaxCruiseEAS();
	public void setVMaxCruiseEAS(Amount<Velocity> _vMaxCruiseEAS);

	public Amount<Velocity> getVOptimumCruise();
	public void setVOptimumCruise(Amount<Velocity> _vOptimumCruise);

	public Amount<Pressure> getMaxDynamicPressure();
	public void setMaxDynamicPressure(Amount<Pressure> _maxDynamicPressure);

	public Map<ComponentEnum, MethodEnum> getMethodsMapWeights();
	public void setMethodsMapWeights(Map<ComponentEnum, MethodEnum> _methodsMap);
	
	public Map<AnalysisTypeEnum, Boolean> getExecutedAnalysesMap();
	public void setExecutedAnalysesMap(Map<AnalysisTypeEnum, Boolean> _executedAnalysesMap);

	public List<ACCalculatorManager> getTheCalculatorsList();
	public void setTheCalculatorsList(List<ACCalculatorManager> _theCalculatorsList);
	
	public ACWeightsManager getTheWeights();
	public void setTheWeights(ACWeightsManager theWeights);

	public ACBalanceManager getTheBalance();
	public void setTheBalance(ACBalanceManager theBalance);

	public ACAerodynamicsManager getTheAerodynamics();
	public void setTheAerodynamics(ACAerodynamicsManager theAerodynamics);

	public ACPerformanceCalculator getThePerformance();
	public void setThePerformance(ACPerformanceCalculator thePerformance);

	public ACCostsManager getTheCosts();
	public void setTheCosts(ACCostsManager theCosts);
}
