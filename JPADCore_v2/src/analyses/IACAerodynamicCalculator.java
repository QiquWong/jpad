package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACAerodynamicCalculator {

	//..............................................................................
	// FROM INPUT (Passed from ACAnalysisManager)
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	ConditionEnum getCurrentCondition();
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> getComponentTaskList();
	List<AerodynamicAndStabilityPlotEnum> getPlotList();
	List<Double> getXCGAircraft(); //in MAC perc.
	List<Double> getZCGAircraft();
	Amount<Length> getZCGLandingGear(); // TODO: IN IMPORT FROM XML, READ OR CALCULATE THIS
	Double getLandingGearDragCoefficient();
	Amount<Angle> getAlphaBodyInitial();
	Amount<Angle> getAlphaBodyFinal();
	int getNumberOfAlphasBody();
	Amount<Angle> getBetaInitial();
	Amount<Angle> getBetaFinal();
	int getNumberOfBeta();
	int getWingNumberOfPointSemiSpanWise();
	int getHTailNumberOfPointSemiSpanWise();
	int getVTailNumberOfPointSemiSpanWise();
	List<Amount<Angle>> getAlphaWingForDistribution();
	List<Amount<Angle>> getAlphaHorizontalTailForDistribution();
	List<Amount<Angle>> getBetaVerticalTailForDistribution();
	Boolean getDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	Double getDynamicPressureRatio();
	MyInterpolatingFunction getTauElevatorFunction();
	MyInterpolatingFunction getTauRudderFunction();
	List<Amount<Angle>> getDeltaElevatorList();
	List<Amount<Angle>> getDeltaRudderList();
	Boolean getFuselageEffectOnWingLiftCurve();
	Boolean getWingPendularStability();
	Double getDeltaCD0Miscellaneous();
	Amount<Length> getWingMomentumPole();
	Amount<Length> getHTailMomentumPole();
	Amount<Length> getVTailMomentumPole();
	Double getAdimensionalFuselageMomentumPole();
	
	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACAerodynamicCalculator_Builder { }
	
}
