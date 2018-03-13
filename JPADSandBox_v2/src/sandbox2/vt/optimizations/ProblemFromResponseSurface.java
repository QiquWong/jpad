package sandbox2.vt.optimizations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import com.google.common.collect.Lists;

import flanagan.interpolation.PolyCubicSpline;
import standaloneutils.MyArrayUtils;

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
	@SuppressWarnings("unchecked")
	public void importResponseSurface (String filePath) throws IOException {
		
		File inputFile = new File(filePath);
		if(!inputFile.exists()) {
			System.err.println("\tThe input file does not exist ... terminating");
			System.exit(1);
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

		for(int i=0; i<inputList.size(); i++ ) {
			for(int j=numberOfVariables; j<numberOfVariables + numberOfObjectives; j++) {
				yArrays[i][j-numberOfVariables] = Double.valueOf(inputList.get(i)[j]);
			}
		}
		
		RealMatrix xMatrix = MatrixUtils.createRealMatrix(xArrays);
		List<double[]> variableArrayList = new ArrayList<>();
		for (int i=0; i<xMatrix.getColumnDimension(); i++) {
			variableArrayList.add(Arrays.stream(xMatrix.getColumn(i)).distinct().toArray());
		}

		RealMatrix yMatrix = MatrixUtils.createRealMatrix(yArrays);
		List<double[]> objectiveArrayList = new ArrayList<>();
		for (int i=0; i<yMatrix.getColumnDimension(); i++) {
			objectiveArrayList.add(Arrays.stream(yMatrix.getColumn(i)).distinct().toArray());
		}
		
		List<List<List<Integer>>> columnIndexList = new ArrayList<>();
		List<List<Integer>> currentVariableElementList = new ArrayList<>();
		List<Integer> currentIndexElementList = new ArrayList<>();
		for(int i=0; i<numberOfVariables; i++) {
			currentVariableElementList = new ArrayList<>();
			for(int j=0; j<variableArrayList.get(i).length; j++) {
				currentIndexElementList = new ArrayList<>();
				for(int k=0; k<xArrays.length; k++) {
					if(xArrays[k][i] == variableArrayList.get(i)[j])
						currentIndexElementList.add(k);
				}
				currentVariableElementList.add(currentIndexElementList);
			}
			columnIndexList.add(currentVariableElementList);
		}

		List<Integer> arrayDimensionList = variableArrayList.stream().map(x -> x.length).collect(Collectors.toList());
		
		List<List<Integer>> inputPermutationList = new ArrayList<>();
		for(int i=0; i<numberOfVariables; i++) {
			inputPermutationList.add(
					MyArrayUtils.convertIntArrayToListInteger(
							MyArrayUtils.linspaceInt(
									0,
									arrayDimensionList.get(i)-1, 
									arrayDimensionList.get(i)
									)
							)
					);
		}
		List<List<Double>> variablePermutationList = new ArrayList<>();
		for(int i=0; i<numberOfVariables; i++) {
			variablePermutationList.add(
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									variableArrayList.get(i)
									)
							)
					);
		}
		
		Collection<List<Integer>> permutationResults = permutations(inputPermutationList);
		Collection<List<Double>> variableResults = permutations(variablePermutationList);
		
		System.out.println("\tInterpolating Response Surface ... \n");
				
		Map<List<Integer>, Integer> interpolatingMatrixIndexes = new HashMap<>();
		for(int i=0; i<permutationResults.size(); i++) {
			interpolatingMatrixIndexes = buildInterpolatingMatrixIndexes(
					columnIndexList, 
					(List<Integer>) permutationResults.toArray()[i], 
					interpolatingMatrixIndexes
					);
			System.out.println("\tInput variable combination: " 
					+ (List<Double>) variableResults.toArray()[i] + " --> Objectives: " 
					+ Arrays.toString(yArrays[interpolatingMatrixIndexes.get((List<Integer>) permutationResults.toArray()[i])])
					);
		}
		
		for(int i=0; i<objectiveArrayList.size(); i++)
			createObjectiveMultiDimensionalMatrix(
					numberOfVariables,
					i,
					variableArrayList,
					objectiveArrayList.get(i),
					interpolatingMatrixIndexes
					);
		
	}
	
	private Map<List<Integer>, Integer> buildInterpolatingMatrixIndexes (
			List<List<List<Integer>>> columnIndexList,
			List<Integer> indexList,
			Map<List<Integer>, Integer> interpolatingMatrixIndexes
			) {
		
		for(int i=0; i<columnIndexList.get(columnIndexList.size()-1).size(); i++) {
			
			List<List<Integer>> columnIndexListArrays = new ArrayList<>();
			for(int j=0; j<numberOfVariables; j++) 
				columnIndexListArrays.add(columnIndexList.get(j).get(indexList.get(j)));
			
			List<Integer> valueList = new ArrayList<>();
			valueList.addAll(columnIndexListArrays.get(0));
			for(int j=1; j<columnIndexListArrays.size(); j++) {
				valueList.retainAll(columnIndexListArrays.get(j));
			}
			
			interpolatingMatrixIndexes.put(
					indexList, 
					valueList.get(0)
					);
		}
		
		return interpolatingMatrixIndexes;
	}
	
	/**
	 * Combines several collections of elements and create permutations of all of them, taking one element from each
	 * collection, and keeping the same order in resultant lists as the one in original list of collections.
	 * 
	 * <ul>Example
	 * <li>Input  = { {a,b,c} , {1,2,3,4} }</li>
	 * <li>Output = { {a,1} , {a,2} , {a,3} , {a,4} , {b,1} , {b,2} , {b,3} , {b,4} , {c,1} , {c,2} , {c,3} , {c,4} }</li>
	 * </ul>
	 * 
	 * @param inputPermutationList Original list of collections which elements have to be combined.
	 * @return Resultant collection of lists with all permutations of original list.
	 */
	public static <T> Collection<List<T>> permutations(List<List<T>> inputPermutationList) {
		if (inputPermutationList == null || inputPermutationList.isEmpty()) {
			return Collections.emptyList();
		} else {
			Collection<List<T>> res = Lists.newLinkedList();
			permutationsImpl(inputPermutationList, res, 0, new LinkedList<T>());
			return res;
		}
	}

	/** Recursive implementation for {@link #permutations(List, Collection)} */
	private static <T> void permutationsImpl(List<List<T>> inputPermutationList, Collection<List<T>> res, int d, List<T> copy2) {
		// if depth equals number of original collections, final reached, add and return
		if (d == inputPermutationList.size()) {
			res.add(copy2);
			return;
		}

		// iterate from current collection and copy 'current' element N times, one for each element
		Collection<T> currentCollection = (Collection<T>) inputPermutationList.get(d);
		for (T element : currentCollection) {
			List<T> copy = Lists.newLinkedList(copy2);
			copy.add(element);
			permutationsImpl(inputPermutationList, res, d + 1, copy);
		}
	}

	private void createObjectiveMultiDimensionalMatrix (
			int numberOfVariable, 
			int indexOfobjective,
			List<double[]> variableArrayList,
			double[] objectiveValues, 
			Map<List<Integer>, Integer> interpolatingMatrixIndexes ) {

		if(numberOfVariable != variableArrayList.size()) {
			System.err.println("\tERROR: The number of variables does not match the size of the variable array! ... terminating");
			return;
		}
		
		if(objectiveValues.length != interpolatingMatrixIndexes.size()) {
			System.err.println("\tERROR: The number of objective values does not match the size of the indexes matrix! ... terminating");
			return;
		}
		
		
		double[][] variableMatrix= new double[numberOfVariable][];
		for(int i=0; i<numberOfVariable; i++)
			variableMatrix[i] = variableArrayList.get(i);
		
		List<List<Integer>> matrixIndexSet = new ArrayList<>(interpolatingMatrixIndexes.keySet());
		List<Integer> matrixIndexValues = new ArrayList<>(interpolatingMatrixIndexes.values());
		
		switch (numberOfVariable) {
		case 1:
			System.err.println("\tERROR: Only one parameter has been assigned! ... terminating");
			return;
		case 2:
			double[][] objectiveMultiDimensionalMatrix2 = new double	
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix2
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix2
					);
			
			break;
		case 3:
			double[][][] objectiveMultiDimensionalMatrix3 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix3
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix3
					);
			
			break;
		case 4:
			double[][][][] objectiveMultiDimensionalMatrix4 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix4
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix4
					);
			
			break;
		case 5:
			double[][][][][] objectiveMultiDimensionalMatrix5 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix5
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix5
					);
			
			break;
		case 6:
			double[][][][][][] objectiveMultiDimensionalMatrix6 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix6
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix6
					);
			
			break;
		case 7:
			double[][][][][][][] objectiveMultiDimensionalMatrix7 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix7
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix7
					);
			
			break;
		case 8:
			double[][][][][][][][] objectiveMultiDimensionalMatrix8 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix8
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix8
					);
			
			break;
		case 9:
			double[][][][][][][][][] objectiveMultiDimensionalMatrix9 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix9
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix9
					);
			
			break;
		case 10:
			double[][][][][][][][][][] objectiveMultiDimensionalMatrix10 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix10
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix10
					);
			
			break;
		case 11:
			double[][][][][][][][][][][] objectiveMultiDimensionalMatrix11 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix11
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix11
					);
			
			break;
		case 12:
			double[][][][][][][][][][][][] objectiveMultiDimensionalMatrix12 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix12
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix12
					);
			
			break;
		case 13:
			double[][][][][][][][][][][][][] objectiveMultiDimensionalMatrix13 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix13
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix13
					);
			
			break;
		case 14:
			double[][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix14 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix14
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix14
					);
			
			break;
		case 15:
			double[][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix15 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix15
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix15
					);
			
			break;
		case 16:
			double[][][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix16 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length]
					[variableArrayList.get(15).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix16
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)]
					[matrixIndexSet.get(i).get(15)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix16
					);
			
			break;
		case 17:
			double[][][][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix17 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length]
					[variableArrayList.get(15).length]
					[variableArrayList.get(16).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix17
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)]
					[matrixIndexSet.get(i).get(15)]
					[matrixIndexSet.get(i).get(16)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix17
					);
			
			break;
		case 18:
			double[][][][][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix18 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length]
					[variableArrayList.get(15).length]
					[variableArrayList.get(16).length]
					[variableArrayList.get(17).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix18
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)]
					[matrixIndexSet.get(i).get(15)]
					[matrixIndexSet.get(i).get(16)]
					[matrixIndexSet.get(i).get(17)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix18
					);
			
			break;
		case 19:
			double[][][][][][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix19 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length]
					[variableArrayList.get(15).length]
					[variableArrayList.get(16).length]
					[variableArrayList.get(17).length]
					[variableArrayList.get(18).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix19
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)]
					[matrixIndexSet.get(i).get(15)]
					[matrixIndexSet.get(i).get(16)]
					[matrixIndexSet.get(i).get(17)]
					[matrixIndexSet.get(i).get(18)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix19
					);
			
			break;
		case 20:
			double[][][][][][][][][][][][][][][][][][][][] objectiveMultiDimensionalMatrix20 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length]
					[variableArrayList.get(8).length]
					[variableArrayList.get(9).length]
					[variableArrayList.get(10).length]
					[variableArrayList.get(11).length]
					[variableArrayList.get(12).length]
					[variableArrayList.get(13).length]
					[variableArrayList.get(14).length]
					[variableArrayList.get(15).length]
					[variableArrayList.get(16).length]
					[variableArrayList.get(17).length]
					[variableArrayList.get(18).length]
					[variableArrayList.get(19).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				objectiveMultiDimensionalMatrix20
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)]
					[matrixIndexSet.get(i).get(10)]
					[matrixIndexSet.get(i).get(11)]
					[matrixIndexSet.get(i).get(12)]
					[matrixIndexSet.get(i).get(13)]
					[matrixIndexSet.get(i).get(14)]
					[matrixIndexSet.get(i).get(15)]
					[matrixIndexSet.get(i).get(16)]
					[matrixIndexSet.get(i).get(17)]
					[matrixIndexSet.get(i).get(18)]
					[matrixIndexSet.get(i).get(19)] = objectiveValues[matrixIndexValues.get(i)];
			
			interpolateResponseSurface(
					indexOfobjective, 
					variableMatrix, 
					objectiveMultiDimensionalMatrix20
					);
			
			break;
		default:
			break;
		}
		
		return;
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
