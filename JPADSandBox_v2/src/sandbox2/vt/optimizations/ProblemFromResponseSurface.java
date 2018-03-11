package sandbox2.vt.optimizations;

import java.util.HashMap;
import java.util.Map;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import flanagan.interpolation.PolyCubicSpline;

/** 
 * This class generates an optimization problem for the MOEA Framework library starting from a given response surface. 
 * To use this class the user have follow these steps in the main class:
 * 
 *  - Create an instance of ProblemFromResponseSurface
 *  - For each objective, if it has to be maximized, negate the yArrays. (MOEA Framework works only with minimization problems)
 *  - Invoke the "interpolateResponseSurface" method for each response surface (one per objective). This will populate the "interpolatedResponseSurfaceMap".
 *  - set upper and lower bounds arrays (if needed)
 * 
 * @see: http://keyboardscientist.weebly.com/blog/moea-framework-defining-new-problems
 * @author Vittorio Trifari
 *
 */
public class ProblemFromResponseSurface extends AbstractProblem {

	//--------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private Map<Integer, PolyCubicSpline> interpolatedResponseSurfaceMap;
	private double[] variablesUpperBounds;
	private double[] variablesLowerBounds;
	
	//--------------------------------------------------------------------------------------
	// BUILDER
	public ProblemFromResponseSurface(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		interpolatedResponseSurfaceMap = new HashMap<>();
		
		// TODO: initialize array if needed and perform arrays dimension check
		
	}

	//--------------------------------------------------------------------------------------
	// METHODS
	
	// TODO: ADD METHDO TO PARSE DATA FROM FILE (.csv) AND CREATE XArrays YArrays
	
	/**
	 * @see: Example --> https://www.ee.ucl.ac.uk/~mflanaga/java/PolyCubicSplineExample.java
	 * 
	 * @author Vittorio Trifari
	 * @param xValues is a two dimensional array of doubles in which:
	 * 			 - xValues[0] is the array of x1 values, i.e. {xValue[0][0] to xValue[0][p-1]},
	 * 		  	 - xValues[1] is the array of x2 values i.e. {xValue[1][0] to xValue[1][q-1]}, 
	 * 			 . . . 
	 * 			 - and xValues[n-1] is the array of xn i.e. {xValue[n-1][0] to xValue[n-1][r-1]} 
	 * 		  where there are p values x1, q values of x2, ..... r values of xn.
	 * @param yValues is an n-dimensional array of doubles containing the tabulated values of y = f(x1,x2,x3 . . . xn). 
	 * 		  The yValues elements are ordered as in the order of the following nested for loops if you were reading in or calculating the tabulated data:
					
					for(int i=0; i<x1.length; i++){
       					for(int j=0; j<x2.length; j++){
              				for(int k=0; k<x3.length; k++){
                     			for(int l=0; l<x4.length; l++){
                            		for(int m=0; m<x5.length; m++){
                                   		yValues[i][j][k][l][m] = 'read statement' or 'calculation of f(x1, x2, x3, x4, x5)';
                            		}
                     			}
              				}
       					}
					}
	 * @return the n-dimensional cubic spline
	 */
	public void interpolateResponseSurface(int objectiveIndex, Object xValues, Object yValues) {
		
		/*
		 * Creates an instance of the PolyCubicSpline object with its internal data arrays 
		 */
		PolyCubicSpline interpolatedResponseSurface = new PolyCubicSpline(
				xValues,
				yValues
				);
		
		interpolatedResponseSurfaceMap.put(objectiveIndex, interpolatedResponseSurface);
	}
	
	/**
	 * The evaluate method gives a candidate solution to the problem. 
	 */
	@Override
	public void evaluate(Solution solution) {
		
		double[] xArray = new double[solution.getNumberOfVariables()];
		for(int i=0; i<numberOfVariables; i++) 
			xArray[i] = ((RealVariable)solution.getVariable(i)).getValue(); 
		
		for(int i=0; i<numberOfObjectives; i++) 
			solution.setObjective(i, getInterpolatedValue(i, xArray));
		
	}

	/**
	 * The newSolution method is used to define a prototype solution to the problem.
	 * This prototype simply defines the number decision variables, their types 
	 * and if applicable the bounds on the decision variables.
	 */
	@Override
	public Solution newSolution() {
		
		Solution solution = new Solution(numberOfVariables, numberOfObjectives);

		for (int i = 0; i < numberOfVariables; i++) 
			solution.setVariable(i, new RealVariable(variablesLowerBounds[i], variablesUpperBounds[i])); // <-- insert here bounds

		return solution;
	}

	//--------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public Map<Integer, PolyCubicSpline> getInterpolatedResponseSurfaceMap() {
		return interpolatedResponseSurfaceMap;
	}

	public void setInterpolatedResponseSurfaceMap(Map<Integer, PolyCubicSpline> interpolatedResponseSurfaceMap) {
		this.interpolatedResponseSurfaceMap = interpolatedResponseSurfaceMap;
	};
	
	public double getInterpolatedValue (Integer objectiveIndex, double[] xArray) {
		return interpolatedResponseSurfaceMap.get(objectiveIndex).interpolate(xArray);
	}

	public double[] getVariablesUpperBounds() {
		return variablesUpperBounds;
	}

	public void setVariablesUpperBounds(double[] variablesUpperBounds) {
		this.variablesUpperBounds = variablesUpperBounds;
	}

	public double[] getVariablesLowerBounds() {
		return variablesLowerBounds;
	}

	public void setVariablesLowerBounds(double[] variablesLowerBounds) {
		this.variablesLowerBounds = variablesLowerBounds;
	}

}
