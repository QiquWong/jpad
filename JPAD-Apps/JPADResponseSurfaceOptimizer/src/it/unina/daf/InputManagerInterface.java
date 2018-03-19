package it.unina.daf;

import java.util.List;

import javax.annotation.Nullable;

import org.inferred.freebuilder.FreeBuilder;
import configuration.enumerations.ConstraintsViolationConditionEnum;

@FreeBuilder
public interface InputManagerInterface {

	int getNumberOfVariables();
	int getNumberOfObjectives();
	int getNumberOfConstraints();
	Boolean[] getMaximizationProblemConditionArray();
	double[] getVariablesLowerBounds();
	double[] getVariablesUpperBounds();
	
	@Nullable
	double[] getConstraintsValues();
	@Nullable
	List<ConstraintsViolationConditionEnum> getConstraintsViolationConditions();
	
	String[] getAlgorithms();
	int getMaximumNumberOfEvaluations();
	int getPopulationSize();
	
	class Builder extends InputManagerInterface_Builder {
		public Builder() {
			
		}
	}
}
