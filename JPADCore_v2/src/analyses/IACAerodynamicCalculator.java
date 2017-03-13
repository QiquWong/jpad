package analyses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACAerodynamicCalculator {

	// FROM INPUT (Passed from ACAnalysisManager)
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	List<ConditionEnum> getTheConditions();
	//..............................................................................
	// FROM INPUT (Passed from File)
	Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> getComponentTaskList();
	List<Double> getXCGAircraft();
	List<Double> getZCGAircraft();
	Amount<Angle> getAlphaBodyInitial();
	Amount<Angle> getAlphaBodyFinal();
	int getNumberOfAlphasBody();
	int getWingNumberOfPointSemiSpanWise();
	int getHTailNumberOfPointSemiSpanWise();
	List<Amount<Angle>> getAlphaWingForDistribution();
	List<Amount<Angle>> getAlphaHorizontalTailForDistribution();
	boolean isDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	Double getDynamicPressureRatio();
	MyInterpolatingFunction getTauElevatorFunction();
	List<Amount<Angle>> getDeltaElevatorList();
	
	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACAerodynamicCalculator_Builder {
		@SuppressWarnings("serial")
		public Builder() {
			// Set defaults in the builder constructor.
			putComponentTaskList(
					ComponentEnum.WING, 
					new HashMap<AerodynamicAndStabilityEnum, MethodEnum>()	{{
						put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.DEYOUNG_HARPER);
						put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CL_ZERO, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CL_STAR, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CL_MAX, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, MethodEnum.INTEGRAL_MEAN_TWIST);
						put(AerodynamicAndStabilityEnum.ALPHA_STAR, MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS);
						put(AerodynamicAndStabilityEnum.ALPHA_STALL, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CD0, MethodEnum.AIRFOIL_DISTRIBUTION);
						put(AerodynamicAndStabilityEnum.CD_INDUCED, MethodEnum.RAYMER); // which is the best method?
						put(AerodynamicAndStabilityEnum.CD_WAVE, MethodEnum.LOCK_KORN_WITH_KROO);
						put(AerodynamicAndStabilityEnum.OSWALD_FACTOR, MethodEnum.RAYMER); // which is the best method?
						put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D, MethodEnum.AIRFOIL_DISTRIBUTION);
						put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						put(AerodynamicAndStabilityEnum.CD_AT_ALPHA, MethodEnum.AIRFOIL_DISTRIBUTION);
						put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, MethodEnum.SEMPIEMPIRICAL);
						put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMPIEMPIRICAL);
						put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMPIEMPIRICAL);
						put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMPIEMPIRICAL);
						put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMPIEMPIRICAL);
						
						// TODO : put the moment contributions
						
					}}
					);
//			putComponentTaskList(
//					ComponentEnum.HORIZONTAL_TAIL, 
//					value
//					);
//			putComponentTaskList(
//					ComponentEnum.VERTICAL_TAIL,
//					value
//					);
//			putComponentTaskList(
//					ComponentEnum.FUSELAGE, 
//					value
//					);
//			putComponentTaskList(
//					ComponentEnum.NACELLE, 
//					value
//					);
		}
	}
	
}
