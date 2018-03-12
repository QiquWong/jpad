package sandbox2.vt.optimizations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import flanagan.interpolation.PolyCubicSpline;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

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
	}

	//--------------------------------------------------------------------------------------
	// METHODS
	
	/**
	 * This method reads from a .csv file and populate the "interpolatedResponseSurfaceMap" for each objective
	 * 
	 * @author Vittorio Trifari
	 * @param filePath
	 * @throws IOException 
	 */
	public void importResponseSurface (String filePath) throws IOException {
		
		File inputFile = new File(filePath);
		if(!inputFile.exists()) {
			System.err.println("\tThe input file does not exist ... terminating");
			System.exit(1);;
		}
		
		List<String[]> inputList = new ArrayList<>();
		InputStream inputFS = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
		// skip the header of the csv
		inputList = br.lines()
				.skip(1)
				.map(s -> s.split(";"))
				.collect(Collectors.toList());
		br.close();

		double[][] xArrays = new double[inputList.size()][numberOfVariables];
		double[][] yArrays = new double[inputList.size()][numberOfObjectives];
		
		for(int i=0; i<inputList.size(); i++ ) {
			for(int j=0; j<numberOfVariables; j++) {
				xArrays[i][j] = Double.valueOf(inputList.get(i)[j]);
			}
		}
		
		RealMatrix xArraysTransposed = MatrixUtils.createRealMatrix(xArrays).transpose();  
		// TODO: CONTINUE USING REAL MATRIX (easy to fetch columns)
		//
		//       From the transposed matrix, for each row distinct elements. This will provide the vairables array to be used for the interpolation.
		//
		//       For the yMatrix, search in the xArrays Matrix the line at which the specific x,y and z value are matched.ù
		//       That index have to be used to build the n-dimensional matrix to be interpolated (one per objective)
				
		for(int i=0; i<inputList.size(); i++ ) {
			for(int j=numberOfVariables; j<numberOfVariables + numberOfObjectives; j++) {
				yArrays[i][j-numberOfVariables] = Double.valueOf(inputList.get(i)[j]);
			}
		}
		
	}
	
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
