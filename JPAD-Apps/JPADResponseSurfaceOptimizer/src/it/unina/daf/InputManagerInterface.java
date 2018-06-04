package it.unina.daf;

import java.util.List;

import javax.annotation.Nullable;

import org.inferred.freebuilder.FreeBuilder;

import configuration.enumerations.ConstraintsViolationConditionEnum;
import javaslang.Tuple2;

@FreeBuilder
public interface InputManagerInterface {

	int getNumberOfVariables();
	int getNumberOfObjectives();
	int getNumberOfConstraints();
	Boolean[] getMaximizationProblemConditionArray();
	double[] getVariablesLowerBounds();
	double[] getVariablesUpperBounds();
	
	@Nullable
	List<Tuple2<ConstraintsViolationConditionEnum, List<Double>>> getConstraintsDictionary();
	
	String[] getAlgorithms();
	int getMaximumNumberOfEvaluations();
	int getPopulationSize();
	
	class Builder extends InputManagerInterface_Builder {
		public Builder() {
			
		}
	}
}
