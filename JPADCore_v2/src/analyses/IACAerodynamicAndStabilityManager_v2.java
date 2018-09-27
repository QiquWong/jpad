package analyses;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.customdata.MyArray;

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
	String getID();
	
	// balance
	List<Double> getXCGAircraft(); //in MAC perc.
	List<Double> getZCGAircraft(); //in MAC perc.
	Amount<Length> getXCGFuselage(); //in BRF.
	Amount<Length> getZCGFuselage(); //in BRF.
	Amount<Length> getXCGLandingGear(); //in BRF.
	Amount<Length> getZCGLandingGear(); //in BRF.
	Amount<Length> getXCGNacelles(); //in BRF.
	Amount<Length> getZCGNacelles(); //in BRF.
	
	// global data
	Amount<Angle> getAlphaBodyInitial();
	Amount<Angle> getAlphaBodyFinal();
	int getNumberOfAlphasBody();
	Amount<Angle> getBetaInitial();
	Amount<Angle> getBetaFinal();
	int getNumberOfBeta();
	@Nullable List<Amount<Angle>> getAlphaWingForDistribution();
	@Nullable List<Amount<Angle>> getAlphaHorizontalTailForDistribution();
	@Nullable List<Amount<Angle>> getBetaVerticalTailForDistribution();
	@Nullable List<Amount<Angle>> getAlphaCanardForDistribution();
	int getWingNumberOfPointSemiSpanWise();
	int getHTailNumberOfPointSemiSpanWise();
	int getVTailNumberOfPointSemiSpanWise();
	int getCanardNumberOfPointSemiSpanWise();
	List<Amount<Angle>> getDeltaElevatorList();
	List<Amount<Angle>> getDeltaRudderList();
	List<Amount<Angle>> getDeltaCanardControlSurfaceList();
	double getAdimensionalWingMomentumPole();
	double getAdimensionalHTailMomentumPole();
	double getAdimensionalVTailMomentumPole();
	double getAdimensionalCanardMomentumPole();
	double getAdimensionalFuselageMomentumPole();
	double getWingDynamicPressureRatio();
	double getHTailDynamicPressureRatio();
	double getVTailDynamicPressureRatio();
	MyInterpolatingFunction getTauElevatorFunction();
	MyInterpolatingFunction getTauRudderFunction();
	MyInterpolatingFunction getTauCanardFunction();
	
	// analysis options
	boolean isCanardWingDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	boolean isWingHTailDownwashConstant(); // if TRUE--> constant, if FALSE--> variable
	boolean isFuselageEffectOnWingLiftCurve(); // if TRUE--> included, if FALSE--> not included
	double  getTotalLiftCalibrationAlphaScaleFactor();
	double  getTotalLiftCalibrationCLScaleFactor();
	boolean isCalculateMiscellaneousDeltaDragCoefficient(); // if TRUE--> calculated, if FALSE--> not calculated
	double getLandingGearDeltaDragCoefficient();
	boolean isCalculateLandingGearDeltaDragCoefficient(); // if TRUE--> calculated, if FALSE--> not calculated
	double getMiscellaneousDeltaDragCoefficient();
	double  getTotalDragCalibrationCLScaleFactor();
	double  getTotalDragCalibrationCDScaleFactor();
	double  getTotalMomentCalibrationAlphaScaleFactor();
	double  getTotalMomentCalibrationCMScaleFactor();
	boolean isCalculateWingPendularStability();  // if TRUE--> calculated, if FALSE--> not calculated
	
	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACAerodynamicAndStabilityManager_v2_Builder { 
		public Builder() {
			
			}
		}
}
