package it.unina.daf;

import javax.annotation.Nullable;

import org.inferred.freebuilder.FreeBuilder;
import configuration.enumerations.ConstraintsViolationConditionEnum;

@FreeBuilder
public interface InputManagerInterface {

	int getNumberOfVariables();
	int getNumberOfObjectives();
	int getNumberOfConstraints();
	boolean[] getMaximizationProblemConditionArray();
	double[] getVariablesLowerBounds();
	double[] getVariablesUpperBounds();
	
	@Nullable
	double[] getConstraintsValues();
	@Nullable
	ConstraintsViolationConditionEnum[] getConstraintsViolationConditions();
	
	String[] getAlgorithms();
	int getMaximumNumberOfEvaluations();
	int getPopulationSize();
	
	class Builder extends InputManagerInterface_Builder {
		public Builder() {
			
			// Default Values
			setPopulationSize(50);
			setMaximumNumberOfEvaluations(10000);
		}
	}
}
