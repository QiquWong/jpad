package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACPerformanceManager {

	//..............................................................................
	// FROM INPUT (Passed from ACAnalysisManager)
	String getId();
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	//..............................................................................
	// Weights
	Amount<Mass> getMaximumTakeOffMass();
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getMaximumFuelMass();
	Amount<Mass> getSinglePassengerMass();
	//..............................................................................
	// Aerodynamics
	Map<Double, MyInterpolatingFunction> getTauRudderMap();
	List<Double> getXcgPositionList();
	Map<Double, Double> getCLmaxClean();
	Map<Double, Amount<?>> getCLAlphaClean();
	Map<Double, Double> getCLmaxTakeOff();
	Map<Double, Amount<?>> getCLAlphaTakeOff();
	Map<Double, Double> getCLZeroTakeOff();
	Map<Double, Double> getCLmaxLanding();
	Map<Double, Amount<?>> getCLAlphaLanding();
	Map<Double, Double> getCLZeroLanding();
	Map<Double, Double> getDeltaCD0FlapTakeOff();
	Map<Double, Double> getDeltaCD0FlapLanding();
	Map<Double, Double> getDeltaCD0LandingGears();
	Map<Double, double[]> getPolarCLCruise();
	Map<Double, double[]> getPolarCDCruise();
	Map<Double, double[]> getPolarCLClimb();
	Map<Double, double[]> getPolarCDClimb();
	Map<Double, double[]> getPolarCLTakeOff();
	Map<Double, double[]> getPolarCDTakeOff();
	Map<Double, double[]> getPolarCLLanding();
	Map<Double, double[]> getPolarCDLanding();
	//..............................................................................
	// Take-off & Landing
	Amount<Velocity> getWindSpeed();
	MyInterpolatingFunction getMuFunction();
	MyInterpolatingFunction getMuBrakeFunction();

	Amount<Duration> getDtHold();
	Amount<Angle> getAlphaGround();
	Amount<Length> getObstacleTakeOff();
	Double getKRotation();
	Double getAlphaDotRotation();
	Double getKCLmaxTakeOff();
	Double getDragDueToEngineFailure();
	Double getKAlphaDot();

	Double getKLandingWeight();
	Amount<Length> getInitialALtitudeLanding();
	Amount<Length> getObstacleLanding();
	Amount<Angle> getApproachAngle();
	Double getKCLmaxLanding();
	Double getKApproach();
	Double getKFlare();
	Double getKTouchDown();
	Amount<Duration> getFreeRollDuration();
	//..............................................................................
	// Climb
	Double getKClimbWeightAEO();
	Double getKClimbWeightOEI();
	Amount<Velocity> getClimbSpeedCAS();
	Amount<Length> getInitialClimbAltitude();
	Amount<Length> getFinalClimbAltitude();
	//..............................................................................
	// Cruise
	List<Amount<Length>> getAltitudeListCruise();
	Double getKCruiseWeight();
	//..............................................................................
	// Descent
	Amount<Velocity> getSpeedDescentCAS();
	Amount<Velocity> getRateOfDescent();
	double getKDescentWeight();
	Amount<Length> getInitialDescentAltitude();
	Amount<Length> getFinalDescentAltitude();
	//..............................................................................
	// Flight maneuvering and gust envelope
	double getCLmaxInverted();
	//..............................................................................
	// Noise Trajectories
	Amount<Length> getTakeOffNoiseTrajectoryXEndSimulation();
	Amount<Length> getTakeOffNoiseTrajectoryCutbackAltitude();
	int getTakeOffNoiseTrajectoryNumberOfThrustSettingCutback();
	Amount<Duration> getTakeOffNoiseTrajectoryLandingGearRetractionTimeInterval();
	Amount<Duration> getTakeOffNoiseTrajectoryThrustReductionCutbackTimeInterval();
	Amount<Length> getLandingNoiseTrajectoryInitialAltitude();
	Amount<Angle> getLandingNoiseTrajectoryTrajectoryAngle();
	//..............................................................................
	// Mission Profile:
	Amount<Length> getMissionRange();
	Amount<Length> getAlternateCruiseLength();
	Amount<Length> getAlternateCruiseAltitude();
	Amount<Duration> getHoldingDuration();
	Amount<Length> getHoldingAltitude();
	double getHoldingMachNumber();
	double getFuelReserve();
	Amount<Length> getFirstGuessCruiseLength();
	Amount<Mass> getFirstGuessInitialMissionFuelMass();
	//..............................................................................
	// Calibration factors:
	
	// Thrust
	double getTakeOffCalibrationFactorThrust();
	double getAprCalibrationFactorThrust();
	double getClimbCalibrationFactorThrust();
	double getContinuousCalibrationFactorThrust();
	double getCruiseCalibrationFactorThrust();
	double getFlightIdleCalibrationFactorThrust();
	double getGroundIdleCalibrationFactorThrust();
	
	// SFC
	double getTakeOffCalibrationFactorSFC();
	double getAprCalibrationFactorSFC();
	double getClimbCalibrationFactorSFC();
	double getContinuousCalibrationFactorSFC();
	double getCruiseCalibrationFactorSFC();
	double getFlightIdleCalibrationFactorSFC();
	double getGroundIdleCalibrationFactorSFC();
	
	// EmissionIndexNOx
	double getTakeOffCalibrationFactorEmissionIndexNOx();
	double getAprCalibrationFactorEmissionIndexNOx();
	double getClimbCalibrationFactorEmissionIndexNOx();
	double getContinuousCalibrationFactorEmissionIndexNOx();
	double getCruiseCalibrationFactorEmissionIndexNOx();
	double getFlightIdleCalibrationFactorEmissionIndexNOx();
	double getGroundIdleCalibrationFactorEmissionIndexNOx();
	
	// EmissionIndexCO
	double getTakeOffCalibrationFactorEmissionIndexCO();
	double getAprCalibrationFactorEmissionIndexCO();
	double getClimbCalibrationFactorEmissionIndexCO();
	double getContinuousCalibrationFactorEmissionIndexCO();
	double getCruiseCalibrationFactorEmissionIndexCO();
	double getFlightIdleCalibrationFactorEmissionIndexCO();
	double getGroundIdleCalibrationFactorEmissionIndexCO();
	
	// EmissionIndexHC
	double getTakeOffCalibrationFactorEmissionIndexHC();
	double getAprCalibrationFactorEmissionIndexHC();
	double getClimbCalibrationFactorEmissionIndexHC();
	double getContinuousCalibrationFactorEmissionIndexHC();
	double getCruiseCalibrationFactorEmissionIndexHC();
	double getFlightIdleCalibrationFactorEmissionIndexHC();
	double getGroundIdleCalibrationFactorEmissionIndexHC();
	
	// EmissionIndexSoot
	double getTakeOffCalibrationFactorEmissionIndexSoot();
	double getAprCalibrationFactorEmissionIndexSoot();
	double getClimbCalibrationFactorEmissionIndexSoot();
	double getContinuousCalibrationFactorEmissionIndexSoot();
	double getCruiseCalibrationFactorEmissionIndexSoot();
	double getFlightIdleCalibrationFactorEmissionIndexSoot();
	double getGroundIdleCalibrationFactorEmissionIndexSoot();
	
	// EmissionIndexCO2
	double getTakeOffCalibrationFactorEmissionIndexCO2();
	double getAprCalibrationFactorEmissionIndexCO2();
	double getClimbCalibrationFactorEmissionIndexCO2();
	double getContinuousCalibrationFactorEmissionIndexCO2();
	double getCruiseCalibrationFactorEmissionIndexCO2();
	double getFlightIdleCalibrationFactorEmissionIndexCO2();
	double getGroundIdleCalibrationFactorEmissionIndexCO2();
	
	// EmissionIndexSOx
	double getTakeOffCalibrationFactorEmissionIndexSOx();
	double getAprCalibrationFactorEmissionIndexSOx();
	double getClimbCalibrationFactorEmissionIndexSOx();
	double getContinuousCalibrationFactorEmissionIndexSOx();
	double getCruiseCalibrationFactorEmissionIndexSOx();
	double getFlightIdleCalibrationFactorEmissionIndexSOx();
	double getGroundIdleCalibrationFactorEmissionIndexSOx();
	
	// EmissionIndexCO
	double getTakeOffCalibrationFactorEmissionIndexH2O();
	double getAprCalibrationFactorEmissionIndexH2O();
	double getClimbCalibrationFactorEmissionIndexH2O();
	double getContinuousCalibrationFactorEmissionIndexH2O();
	double getCruiseCalibrationFactorEmissionIndexH2O();
	double getFlightIdleCalibrationFactorEmissionIndexH2O();
	double getGroundIdleCalibrationFactorEmissionIndexH2O();
	
	//..............................................................................
	// Plot and Task Maps
	List<PerformanceEnum> getTaskList();
	List<PerformancePlotEnum> getPlotList();

	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACPerformanceManager_Builder { }

}
