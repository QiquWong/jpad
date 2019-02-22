package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.ACDynamicStabilityManagerUtils.PropulsionTypeEnum;
import configuration.enumerations.ConditionEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACDynamicStabilityManager {

	//==============================================================================
	// FROM INPUT (Passed from ACAnalysisManager)
	//==============================================================================

	String getId();
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	ConditionEnum getCurrentCondition();

	//==============================================================================
	// FROM INPUT (Passed from XML file)
	//==============================================================================

	//..............................................................................
	// Weights
	Amount<Mass> getMaximumTakeOffMass();
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getMaximumFuelMass();
	Amount<Mass> getMaximumPayload();
	Amount<Mass> getSinglePassengerMass();

	PropulsionTypeEnum getPropulsionSystem();
	
	//..............................................................................
	// Aerodynamics
	List<Double> getXcgPositionList();
	// Maps are: <xcg, variable>
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

	Map<Double, Double> getIXX();
	Map<Double, Double> getIYY();
	Map<Double, Double> getIZZ();
	Map<Double, Double> getIXZ();

	Map<Double, Double> getCDrag0();
	Map<Double, Double> getCDragAlpha0();
	Map<Double, Double> getCDragMach0();
	
	Map<Double, Double> getCLift0();
	Map<Double, Double> getCLiftAlpha0();
	Map<Double, Double> getCLiftAlphaDot0();
	Map<Double, Double> getCLiftMach0();
	Map<Double, Double> getCLiftQ0();
	Map<Double, Double> getCLiftDeltaT();
	Map<Double, Double> getCLiftDeltaE();
	
	Map<Double, Double> getCPitchAlpha0();
	Map<Double, Double> getCPitchAlphaDot0();
	Map<Double, Double> getCPitchMach0();
	Map<Double, Double> getCPitchQ0();
	Map<Double, Double> getCPitchDeltaT();
	Map<Double, Double> getCPitchDeltaE();
	
	Map<Double, Double> getCThrustFix();
	Map<Double, Double> getKVThrust();
	
	Map<Double, Double> getCSideBeta();
	Map<Double, Double> getCSideP();
	Map<Double, Double> getCSideR();
	Map<Double, Double> getCSideDeltaA();
	Map<Double, Double> getCSideDeltaR();

	Map<Double, Double> getCRollBeta();
	Map<Double, Double> getCRollP();
	Map<Double, Double> getCRollR();
	Map<Double, Double> getCRollDeltaA();
	Map<Double, Double> getCRollDeltaR();
	
	Map<Double, Double> getCYawBeta();
	Map<Double, Double> getCYawP();
	Map<Double, Double> getCYawR();
	Map<Double, Double> getCYawDeltaA();
	Map<Double, Double> getCYawDeltaR();
	
	//..............................................................................
	// Plot and Task Maps
	List<ConditionEnum> getTaskList();
	List<ConditionEnum> getPlotList();
	
	class Builder extends IACDynamicStabilityManager_Builder {
		public Builder() {
			
		}
	}
	
}
