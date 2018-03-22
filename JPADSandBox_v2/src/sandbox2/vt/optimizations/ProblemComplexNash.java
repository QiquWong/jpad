package sandbox2.vt.optimizations;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class ProblemComplexNash extends AbstractProblem{

	public ProblemComplexNash(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
	}

	@Override
	public void evaluate(Solution solution) {
		
		double x = ((RealVariable)solution.getVariable(0)).getValue();
		double y = ((RealVariable)solution.getVariable(1)).getValue();
		
		double f1 = 0.0;
		
		if(x <= y) {
			f1 = -x;
		}
		else {		
			f1 = -y;
		}
		
		solution.setObjective(0, f1);
		
	}

	@Override
	public Solution newSolution() {
		
		Solution solution = new Solution(numberOfVariables, numberOfObjectives);

		for (int i = 0; i < numberOfVariables; i++) 
			solution.setVariable(i, new RealVariable(0.0, 1.0)); // <-- insert here bounds
		
		return solution;
	}

}
