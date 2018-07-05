package analyses;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACAerodynamicAndStabilityManager {

	//..............................................................................
	// FROM INPUT (Passed from ACAnalysisManager)
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	ConditionEnum getCurrentCondition();
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> getComponentTaskList();
	Map<ComponentEnum, List<AerodynamicAndStabilityPlotEnum>> getPlotList();
	String getId();
	List<Double> getXCGAircraft(); //in MAC perc.
	List<Double> getZCGAircraft();
	Amount<Length> getXCGFuselage();
	Amount<Length> getZCGFuselage();
	@Nullable Amount<Length> getXCGLandingGear();
	@Nullable Amount<Length> getZCGLandingGear();
	@Nullable Amount<Length> getXCGNacelles();
	@Nullable
	Amount<Length> getZCGNacelles();
	@Nullable
	Double getLandingGearDragCoefficient();
	Amount<Angle> getAlphaBodyInitial();
	Amount<Angle> getAlphaBodyFinal();
	int getNumberOfAlphasBody();
	Amount<Angle> getBetaInitial();
	Amount<Angle> getBetaFinal();
	int getNumberOfBeta();
	int getWingNumberOfPointSemiSpanWise();
	@Nullable
	int getHTailNumberOfPointSemiSpanWise();
	@Nullable
	int getVTailNumberOfPointSemiSpanWise();
	@Nullable
	int getCanardNumberOfPointSemiSpanWise();
	@Nullable
	Amount<Angle> getMaximumElevatorDeflection();
	List<Amount<Angle>> getAlphaWingForDistribution();
	List<Amount<Angle>> getAlphaHorizontalTailForDistribution();
	List<Amount<Angle>> getBetaVerticalTailForDistribution();
	List<Amount<Angle>> getAlphaCanardForDistribution();
	@Nullable
	Boolean getDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	@Nullable
	Double getHTailDynamicPressureRatio();
	@Nullable
	Double getVTailDynamicPressureRatio();
	@Nullable
	MyInterpolatingFunction getTauElevatorFunction();
	@Nullable
	MyInterpolatingFunction getTauRudderFunction();

	List<Amount<Angle>> getDeltaElevatorList();

	Amount<Angle> getElevatorDeflectionForAnalysis();
	List<Amount<Angle>> getDeltaRudderList();
	@Nullable
	Amount<Angle> getRudderDeflectionForAnalysis();
	Boolean getFuselageEffectOnWingLiftCurve();
	Boolean getWingPendularStability();
	@Nullable
	Double getDeltaCD0Miscellaneous();
	Amount<Length> getWingMomentumPole();
	@Nullable
	Amount<Length> getHTailMomentumPole();
	@Nullable
	Amount<Length> getVTailMomentumPole();
	@Nullable
	Amount<Length> getCanardMomentumPole();
	Double getAdimensionalFuselageMomentumPole();
	//..............................................................................
	// AUXILIARY DATA
	MyInterpolatingFunction getWingLiftCurveFunction();
	MyInterpolatingFunction getWingPolarCurveFunction();
	MyInterpolatingFunction getWingHighLiftCurveFunction();
	MyInterpolatingFunction getWingHighLiftPolarCurveFunction();
	MyInterpolatingFunction getWingHighLiftMomentCurveFunction();
	MyInterpolatingFunction getWingMomentCurveFunction();
	
	@Nullable
	MyInterpolatingFunction getHTailLiftCurveFunction();
	@Nullable
	MyInterpolatingFunction getHTailPolarCurveFunction();
	@Nullable
	MyInterpolatingFunction getHTailMomentCurveFunction();
	
	@Nullable
	MyInterpolatingFunction getVTailLiftCurveFunction();
	@Nullable
	MyInterpolatingFunction getVTailPolarCurveFunction();
	@Nullable
	MyInterpolatingFunction getVTailMomentCurveFunction();
	
	@Nullable
	MyInterpolatingFunction getCanardLiftCurveFunction();
	@Nullable
	MyInterpolatingFunction getCanardPolarCurveFunction();
	@Nullable
	MyInterpolatingFunction getCanardMomentCurveFunction();
	
	MyInterpolatingFunction getFuselagePolarCurveFunction();
	MyInterpolatingFunction getFuselageMomentCurveFunction();
	
	@Nullable
	MyInterpolatingFunction getNacellePolarCurveFunction();
	@Nullable
	MyInterpolatingFunction getNacelleMomentCurveFunction();
	
	@Nullable
	MyInterpolatingFunction getAircraftDownwashGradientFunction();
	
	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACAerodynamicAndStabilityManager_Builder { }
	
}
