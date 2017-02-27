package analyses;

import java.util.List;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.ConditionEnum;
import standaloneutils.MyInterpolatingFunction;

@FreeBuilder
public interface IACAerodynamicCalculator {

	// FROM INPUT (Passed from ACAnalysisManager)
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	List<ConditionEnum> getTheConditions();
	//..............................................................................
	// FROM INPUT (Passed from File)
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
		public Builder() {
			// Set defaults in the builder constructor.
			// ... eventually
		}
	}
	
}
