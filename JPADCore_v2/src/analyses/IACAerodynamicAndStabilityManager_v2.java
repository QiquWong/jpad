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
public interface IACAerodynamicAndStabilityManager_v2 {

	//..............................................................................
	// FROM INPUT (Passed from ACAnalysisManager)
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	ConditionEnum getCurrentCondition();
	
	//..............................................................................
	// CHECKS
	boolean isPerformWingAnalyses();  
	boolean isPerformHTailAnalyses(); 
	boolean isPerformVTailAnalyses();  
	boolean isPerformCanardAnalyses(); 
	boolean isPerformFuselageAnalyses();  
	boolean isPerformNacelleAnalyses();
	
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> getComponentTaskList();
	Map<ComponentEnum, List<AerodynamicAndStabilityPlotEnum>> getPlotList();
	List<Double> getXCGAircraft(); //in MAC perc.
	List<Double> getZCGAircraft(); //in MAC perc.
	@Nullable Amount<Length> getXCGFuselage(); //in BRF.
	@Nullable Amount<Length> getZCGFuselage(); //in BRF.
	@Nullable Amount<Length> getXCGLandingGear(); //in BRF.
	@Nullable Amount<Length> getZCGLandingGear(); //in BRF.
	@Nullable Amount<Length> getXCGNacelles(); //in BRF.
	@Nullable Amount<Length> getZCGNacelles(); //in BRF.
	@Nullable double getLandingGearDeltaDragCoefficient();
	@Nullable double getMiscellaneousDeltaDragCoefficient();
	Amount<Angle> getAlphaBodyInitial();
	Amount<Angle> getAlphaBodyFinal();
	int getNumberOfAlphasBody();
	Amount<Angle> getBetaInitial();
	Amount<Angle> getBetaFinal();
	int getNumberOfBeta();
	int getWingNumberOfPointSemiSpanWise();
	int getHTailNumberOfPointSemiSpanWise();
	int getVTailNumberOfPointSemiSpanWise();
	int getCanardNumberOfPointSemiSpanWise();
	@Nullable List<Amount<Angle>> getAlphaWingForDistribution();
	@Nullable List<Amount<Angle>> getAlphaHorizontalTailForDistribution();
	@Nullable List<Amount<Angle>> getBetaVerticalTailForDistribution();
	@Nullable List<Amount<Angle>> getAlphaCanardForDistribution();
	@Nullable boolean getDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	@Nullable double getHTailDynamicPressureRatio();
	@Nullable double getVTailDynamicPressureRatio();
	@Nullable MyInterpolatingFunction getTauElevatorFunction();
	@Nullable MyInterpolatingFunction getTauRudderFunction();
	
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
	class Builder extends IACAerodynamicAndStabilityManager_v2_Builder { }
	
}
