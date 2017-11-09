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

import aircraft.components.Aircraft;
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
	Map<Double, Double[]> getPolarCLCruise();
	Map<Double, Double[]> getPolarCDCruise();
	Map<Double, Double[]> getPolarCLClimb();
	Map<Double, Double[]> getPolarCDClimb();
	Map<Double, Double[]> getPolarCLTakeOff();
	Map<Double, Double[]> getPolarCDTakeOff();
	Map<Double, Double[]> getPolarCLLanding();
	Map<Double, Double[]> getPolarCDLanding();
	//..............................................................................
	// Take-off & Landing
	Amount<Velocity> getWindSpeed();
	MyInterpolatingFunction getMuFunction();
	MyInterpolatingFunction getMuBrakeFunction();

	Amount<Duration> getDtRotation();
	Amount<Duration> getDtHold();
	Amount<Angle> getAlphaGround();
	Amount<Length> getObstacleTakeOff();
	Double getKRotation();
	Double getAlphaDotRotation();
	Double getKCLmax();
	Double getDragDueToEngineFailure();
	Double getKAlphaDot();

	Double getKLandingWeight();
	Amount<Length> getObstacleLanding();
	Amount<Angle> getThetaApproach();
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
	// Flight maneuvering and gust envelope
	Double getCLmaxInverted();
	//..............................................................................
	// Descent
	Amount<Velocity> getSpeedDescentCAS();
	Amount<Velocity> getRateOfDescent();
	Double getKDescentWeight();
	Amount<Length> getInitialDescentAltitude();
	Amount<Length> getFinalDescentAltitude();
	//..............................................................................
	// Mission Profile:
	Amount<Length> getMissionRange();
	Amount<Length> getAlternateCruiseLength();
	Amount<Length> getAlternateCruiseAltitude();
	Amount<Duration> getHoldingDuration();
	Amount<Length> getHoldingAltitude();
	Double getHoldingMachNumber();
	Double getFuelReserve();
	Amount<Length> getFirstGuessCruiseLength();
	Boolean getCalculateSFCCruise();
	Boolean getCalculateSFCAlternateCruise();
	Boolean getCalculateSFCHolding();
	MyInterpolatingFunction getSfcFunctionCruise();
	MyInterpolatingFunction getSfcFunctionAlternateCruise();
	MyInterpolatingFunction getSfcFunctionHolding();
	Amount<Mass> getFirstGuessInitialMissionFuelMass();
	Amount<Length> getTakeOffMissionAltitude();
	Double getLandingFuelFlow();
	//..............................................................................
	// Plot and Task Maps
	List<PerformanceEnum> getTaskList();
	List<PerformancePlotEnum> getPlotList();

	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACPerformanceManager_Builder { }

}
