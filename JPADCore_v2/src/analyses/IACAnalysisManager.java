package analyses;

import java.io.IOException;
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
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
	public void calculateAerodynamicAndStability(
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

	public ACWeightsManager getTheWeights();
	public void setTheWeights(ACWeightsManager theWeights);

	public ACBalanceManager getTheBalance();
	public void setTheBalance(ACBalanceManager theBalance);

	public Map<ConditionEnum, ACAerodynamicCalculator> getTheAerodynamicAndStability();
	public void setTheAerodynamicAndStability(Map<ConditionEnum, ACAerodynamicCalculator> theAerodynamics);

	public ACPerformanceManager getThePerformance();
	public void setThePerformance(ACPerformanceManager thePerformance);

	public ACCostsManager getTheCosts();
	public void setTheCosts(ACCostsManager theCosts);
}
