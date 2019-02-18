package analyses;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.ConditionEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACDynamicStabilityManager {

	//==============================================================================
	// FROM INPUT (Passed from ACAnalysisManager)
	//==============================================================================

	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	ConditionEnum getCurrentCondition();
	
	//==============================================================================
	// FROM INPUT (Passed from XML file)
	//==============================================================================
	
	String getId();

	//..............................................................................
	// Weights
	Amount<Mass> getOperatingEmptyMass();
	Amount<Mass> getDesignFuelMass();

	//..............................................................................
	// Aerodynamics
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
	// Plot and Task Maps
	List<ConditionEnum> getTaskList();
	List<ConditionEnum> getPlotList();
	
	// TODO
	
	class Builder extends IACDynamicStabilityManager_Builder {
		public Builder() {
			
		}
	}
	
}
