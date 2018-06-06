package it.unina.daf;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import configuration.enumerations.ConstraintsViolationConditionEnum;
import flanagan.interpolation.PolyCubicSpline;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

/** 
 * This class generates an optimization problem for the MOEA Framework library starting from a given response surface. 
 * To use this class the user have follow these steps in the main class:
 * 
 *  - Create an instance of ProblemFromResponseSurface
 *  - Set the maximization flag to TRUE (maximize) or FALS (minimize)
 *  - Invoke the "importResponseSurface" method. This will populate the "interpolatedResponseSurfaceMap" and the "interpolatedConstraintsMap".
 *  - set upper and lower bounds arrays (if needed)
 *  - set constraints values array (if needed)
 *  - set the constraints violation condition array
 * 
 * @see: Example of customized problem --> http://keyboardscientist.weebly.com/blog/moea-framework-defining-new-problems
 * @author Vittorio Trifari
 *
 */
public class MOEAProblemResponseSurface extends AbstractProblem {

	//--------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private Map<Integer, PolyCubicSpline> interpolatedResponseSurfaceMap;
	private Map<Integer, PolyCubicSpline> interpolatedConstraintsMap;
	private double[] variablesUpperBounds;
	private double[] variablesLowerBounds;
	private List<Tuple2<ConstraintsViolationConditionEnum, List<Double>>> constraintsDictionary;
	private Boolean[] maximizationProblemConditionArray;
	private String[] objectivesLabelArray;
	private String[] variablesLabelArray;
	
	//--------------------------------------------------------------------------------------
	// BUILDER
	public MOEAProblemResponseSurface(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
		
		super(numberOfVariables, numberOfObjectives, numberOfConstraints);
		
		interpolatedResponseSurfaceMap = new HashMap<>();
		interpolatedConstraintsMap = new HashMap<>();
		variablesLabelArray = new String[numberOfVariables];
		objectivesLabelArray = new String[numberOfObjectives];
		
	}

	//--------------------------------------------------------------------------------------
	// METHODS
	
	/**
	 * This method reads from the .xml input file and store the problem parameter in the dedicated interface 
	 * 'InputManagerInterface'
	 * 
	 * @author Vittorio Trifari
	 * @param filePath
	 * @throws IOException 
	 */
	public static InputManagerInterface importInputFile(String filePath) {
		
		JPADXmlReader reader = new JPADXmlReader(filePath);
		
		// Values initialization:
		int numberOfVariables = 0;
		int numberOfObjectives = 0;
		int numberOfConstraints = 0;
		int maxNumberOfEvaluations = 10000;
		int populationSize = 50;
		List<Boolean> maximizationConditions = new ArrayList<>();
		double[] variableLowerBounds = null;
		double[] variableUpperBounds = null;
		
		List<Tuple2<ConstraintsViolationConditionEnum, List<Double>>> constraintsDictionary = new ArrayList<>();
		String[] algorithms = null;
		
		// Reading all data ...
		String numberOfVarialbleProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/number_of_variables");
		if(numberOfVarialbleProperty != null)
			numberOfVariables = Integer.valueOf(numberOfVarialbleProperty);

		String numberOfObjectiveProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/number_of_objectives");
		if(numberOfObjectiveProperty != null)
			numberOfObjectives = Integer.valueOf(numberOfObjectiveProperty);
		
		String numberOfConstraintsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/number_of_constraints");
		if(numberOfConstraintsProperty != null)
			numberOfConstraints = Integer.valueOf(numberOfConstraintsProperty);
		
		String maxNumberOfEvaluationsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/max_number_of_evaluations");
		if(maxNumberOfEvaluationsProperty != null)
			maxNumberOfEvaluations = Integer.valueOf(maxNumberOfEvaluationsProperty);
		
		String populationSizeProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/population_size");
		if(populationSizeProperty != null)
			populationSize = Integer.valueOf(populationSizeProperty);
		
		String maximizationConditionsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/maximization_conditions");
		String[] maximizationConditionsStringArray = null;
		if(maximizationConditionsProperty != null) {
			maximizationConditionsStringArray = maximizationConditionsProperty.split(";");
			maximizationConditions = Arrays.stream(maximizationConditionsStringArray)
			.map(s -> Boolean.valueOf(s))
			.collect(Collectors.toList());
		}
		
		String variableLowerBoundsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/variable_lower_bounds");
		if(variableLowerBoundsProperty != null)
			variableLowerBounds = MyArrayUtils.convertToDoublePrimitive(
					reader.readArrayDoubleFromXML("//JPAD_Optimizer/variable_lower_bounds")
					);
		
		String variableUpperBoundsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/variable_upper_bounds");
		if(variableUpperBoundsProperty != null)
			variableUpperBounds = MyArrayUtils.convertToDoublePrimitive(
					reader.readArrayDoubleFromXML("//JPAD_Optimizer/variable_upper_bounds")
					);
		
		
		List<List<Double>> constraintsValueList = new ArrayList<>();
		List<String> constraintsViolatingConditionList = new ArrayList<>();
		NodeList constraintsViolatingConditionNodeList = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//constraints/value");
		for (int i = 0; i < constraintsViolatingConditionNodeList.getLength(); i++) {
			Node constraintsViolatingConditionNode  = constraintsViolatingConditionNodeList.item(i); 
			Element constraintsViolatingConditionElement = (Element) constraintsViolatingConditionNode;
			constraintsViolatingConditionList.add(constraintsViolatingConditionElement.getAttribute("violating_condition"));
			constraintsValueList.add(MyXMLReaderUtils.importFromValueNodeListDouble(constraintsViolatingConditionNode));
		}
		for(int i=0; i<numberOfConstraints; i++) {
			constraintsDictionary.add(
					Tuple.of(
							ConstraintsViolationConditionEnum.valueOf(constraintsViolatingConditionList.get(i)),
							constraintsValueList.get(i)
							)
					);
		}
		
		String algorithmsProperty = reader.getXMLPropertyByPath("//JPAD_Optimizer/algorithms");
		if(algorithmsProperty != null) {
			algorithms = algorithmsProperty.split(";");
		}
		
		// Performing dimension checks ...
		if(maximizationConditions.size() != numberOfObjectives) {
			System.err.println("\n\tERROR: The number of MAXIMIZARTION CONDITIONS does not match the declared NUMBER OF OBJECTIVES! ... terminating");
			System.exit(1);
		}
		
		if(variableLowerBounds.length != numberOfVariables) {
			System.err.println("\n\tERROR: The number of VARIABLE LOWER BOUNDS does not match the declared NUMBER OF VARIABLES! ... terminating");
			System.exit(1);
		}
		
		if(variableUpperBounds.length != numberOfVariables) {
			System.err.println("\n\tERROR: The number of VARIABLE UPPER BOUNDS does not match the declared NUMBER OF VARIABLES! ... terminating");
			System.exit(1);
		}
		
		if(numberOfConstraints > 0 ) 
			if(constraintsDictionary.size() != numberOfConstraints) {
				System.err.println("\n\tERROR: The number of CONSTRAINTS VALUES does not match the declared NUMBER OF CONSTRAINTS! ... terminating");
				System.exit(1);
			}

		// Creating the input manager interface
		return new InputManagerInterface.Builder()
				.setNumberOfVariables(numberOfVariables)
				.setNumberOfObjectives(numberOfObjectives)
				.setNumberOfConstraints(numberOfConstraints)
				.setMaximumNumberOfEvaluations(maxNumberOfEvaluations)
				.setPopulationSize(populationSize)
				.setMaximizationProblemConditionArray(maximizationConditions.toArray(new Boolean[numberOfObjectives]))
				.setVariablesLowerBounds(variableLowerBounds)
				.setVariablesUpperBounds(variableUpperBounds)
				.setConstraintsDictionary(constraintsDictionary)
				.setAlgorithms(algorithms)
				.build();
		
	}
	
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
		String[] headerList = new String[numberOfVariables+numberOfObjectives+numberOfConstraints];
		InputStream inputFS = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
		// skip the header of the csv
		headerList = br.readLine().split(";");
		inputList = br.lines()
				.map(s -> s.split(";"))
				.collect(Collectors.toList());
		br.close(); 
		
		if(inputList.get(0).length < (numberOfVariables + numberOfObjectives + numberOfConstraints)) {
			System.err.println("\tERROR: The number of input file colums does not match the declared number of variables, objectives and constraints! ... terminating");
			System.exit(1);
		}
		
		double[][] xArrays = new double[inputList.size()][numberOfVariables];
		double[][] yArrays = new double[inputList.size()][numberOfObjectives];
		double[][] constraintArrays = new double[inputList.size()][numberOfConstraints];
		
		for(int i=0; i<numberOfVariables; i++)
			variablesLabelArray[i] = headerList[i];
			
		for(int i=numberOfVariables; i<numberOfVariables+numberOfObjectives; i++)
			objectivesLabelArray[i-numberOfVariables] = headerList[i];
		
		for(int i=0; i<inputList.size(); i++ ) 
			for(int j=0; j<numberOfVariables; j++) 
				xArrays[i][j] = Double.valueOf(inputList.get(i)[j]);

		for(int i=0; i<inputList.size(); i++ ) 
			for(int j=numberOfVariables; j<numberOfVariables + numberOfObjectives; j++) 
				if(getMaximizationProblemConditionArray()[j-numberOfVariables])
					yArrays[i][j-numberOfVariables] = -Double.valueOf(inputList.get(i)[j]);
				else
					yArrays[i][j-numberOfVariables] = Double.valueOf(inputList.get(i)[j]);
		
		for(int i=0; i<inputList.size(); i++ ) {
			for(int j=numberOfVariables + numberOfObjectives; j<numberOfVariables + numberOfObjectives + numberOfConstraints; j++) {
				constraintArrays[i][j-numberOfVariables - numberOfObjectives] = Double.valueOf(inputList.get(i)[j]);
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
			objectiveArrayList.add(Arrays.stream(yMatrix.getColumn(i)).toArray());
		}
		
		List<double[]> constraintsArrayList = new ArrayList<>();
		if(numberOfConstraints > 0) {
			RealMatrix constraintsMatrix = MatrixUtils.createRealMatrix(constraintArrays);
			for (int i=0; i<constraintsMatrix.getColumnDimension(); i++) {
				constraintsArrayList.add(Arrays.stream(constraintsMatrix.getColumn(i)).toArray());
			}
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
		
		System.out.println("\n\tInterpolating Response Surface ... \n");
				
		Map<List<Integer>, Integer> interpolatingMatrixIndexes = new HashMap<>();
		for(int i=0; i<permutationResults.size(); i++) {
			interpolatingMatrixIndexes = buildInterpolatingMatrixIndexes(
					columnIndexList, 
					(List<Integer>) permutationResults.toArray()[i], 
					interpolatingMatrixIndexes
					);
			System.out.println("\tInput variable combination: " 
					+ (List<Double>) variableResults.toArray()[i] 
					+ " --> Objectives: " + Arrays.toString(yArrays[interpolatingMatrixIndexes.get((List<Integer>) permutationResults.toArray()[i])])
					+ " --> Constraints Values: " + Arrays.toString(constraintArrays[interpolatingMatrixIndexes.get((List<Integer>) permutationResults.toArray()[i])])
					);
		}
		
		for(int i=0; i<objectiveArrayList.size(); i++)
			createMultiDimensionalMatrix(
					numberOfVariables,
					i,
					variableArrayList,
					objectiveArrayList.get(i),
					interpolatingMatrixIndexes,
					interpolatedResponseSurfaceMap
					);
		
		for(int i=0; i<constraintsArrayList.size(); i++)
			createMultiDimensionalMatrix(
					numberOfVariables,
					i,
					variableArrayList,
					constraintsArrayList.get(i),
					interpolatingMatrixIndexes,
					interpolatedConstraintsMap
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

	private void createMultiDimensionalMatrix (
			int numberOfVariable, 
			int index,
			List<double[]> variableArrayList,
			double[] values, 
			Map<List<Integer>, Integer> interpolatingMatrixIndexes,
			Map<Integer, PolyCubicSpline> resultMap
			) {

		if(numberOfVariable != variableArrayList.size()) {
			System.err.println("\tERROR: The number of variables does not match the size of the variable array! ... terminating");
			return;
		}
		
		if(values.length != interpolatingMatrixIndexes.size()) {
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
			double[][] multiDimensionalMatrix2 = new double	
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix2
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix2,
					resultMap
					);
			
			break;
		case 3:
			double[][][] multiDimensionalMatrix3 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix3
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix3,
					resultMap
					);
			
			break;
		case 4:
			double[][][][] multiDimensionalMatrix4 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix4
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix4,
					resultMap
					);
			
			break;
		case 5:
			double[][][][][] multiDimensionalMatrix5 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix5
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix5,
					resultMap
					);
			
			break;
		case 6:
			double[][][][][][] multiDimensionalMatrix6 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix6
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix6,
					resultMap
					);
			
			break;
		case 7:
			double[][][][][][][] multiDimensionalMatrix7 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix7
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix7,
					resultMap
					);
			
			break;
		case 8:
			double[][][][][][][][] multiDimensionalMatrix8 = new double
					[variableArrayList.get(0).length]
					[variableArrayList.get(1).length]
					[variableArrayList.get(2).length]
					[variableArrayList.get(3).length]
					[variableArrayList.get(4).length]
					[variableArrayList.get(5).length]
					[variableArrayList.get(6).length]
					[variableArrayList.get(7).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				multiDimensionalMatrix8
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix8,
					resultMap
					);
			
			break;
		case 9:
			double[][][][][][][][][] multiDimensionalMatrix9 = new double
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
				multiDimensionalMatrix9
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix9,
					resultMap
					);			
			break;
		case 10:
			double[][][][][][][][][][] multiDimensionalMatrix10 = new double
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
				multiDimensionalMatrix10
					[matrixIndexSet.get(i).get(0)]
					[matrixIndexSet.get(i).get(1)]
					[matrixIndexSet.get(i).get(2)] 
					[matrixIndexSet.get(i).get(3)]
					[matrixIndexSet.get(i).get(4)]
					[matrixIndexSet.get(i).get(5)]
					[matrixIndexSet.get(i).get(6)]
					[matrixIndexSet.get(i).get(7)]
					[matrixIndexSet.get(i).get(8)]
					[matrixIndexSet.get(i).get(9)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix10,
					resultMap
					);
			
			break;
		case 11:
			double[][][][][][][][][][][] multiDimensionalMatrix11 = new double
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
				multiDimensionalMatrix11
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
					[matrixIndexSet.get(i).get(10)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix11,
					resultMap
					);
			
			break;
		case 12:
			double[][][][][][][][][][][][] multiDimensionalMatrix12 = new double
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
				multiDimensionalMatrix12
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
					[matrixIndexSet.get(i).get(11)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix12,
					resultMap
					);
			
			break;
		case 13:
			double[][][][][][][][][][][][][] multiDimensionalMatrix13 = new double
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
				multiDimensionalMatrix13
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
					[matrixIndexSet.get(i).get(12)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix13,
					resultMap
					);
			
			break;
		case 14:
			double[][][][][][][][][][][][][][] multiDimensionalMatrix14 = new double
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
				multiDimensionalMatrix14
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
					[matrixIndexSet.get(i).get(13)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix14,
					resultMap
					);
			
			break;
		case 15:
			double[][][][][][][][][][][][][][][] multiDimensionalMatrix15 = new double
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
				multiDimensionalMatrix15
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
					[matrixIndexSet.get(i).get(14)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix15,
					resultMap
					);
			
			break;
		case 16:
			double[][][][][][][][][][][][][][][][] multiDimensionalMatrix16 = new double
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
				multiDimensionalMatrix16
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
					[matrixIndexSet.get(i).get(15)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix16,
					resultMap
					);
			
			break;
		case 17:
			double[][][][][][][][][][][][][][][][][] multiDimensionalMatrix17 = new double
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
				multiDimensionalMatrix17
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
					[matrixIndexSet.get(i).get(16)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix17,
					resultMap
					);
			
			break;
		case 18:
			double[][][][][][][][][][][][][][][][][][] multiDimensionalMatrix18 = new double
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
				multiDimensionalMatrix18
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
					[matrixIndexSet.get(i).get(17)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix18,
					resultMap
					);
			
			break;
		case 19:
			double[][][][][][][][][][][][][][][][][][][] multiDimensionalMatrix19 = new double
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
				multiDimensionalMatrix19
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
					[matrixIndexSet.get(i).get(18)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix19,
					resultMap
					);
			
			break;
		case 20:
			double[][][][][][][][][][][][][][][][][][][][] multiDimensionalMatrix20 = new double
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
				multiDimensionalMatrix20
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
					[matrixIndexSet.get(i).get(19)] = values[matrixIndexValues.get(i)];
			
			interpolateMultidimensionalMatrix(
					index, 
					variableMatrix, 
					multiDimensionalMatrix20,
					resultMap
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
	public void interpolateMultidimensionalMatrix(int objectiveIndex, Object xValues, Object yValues, Map<Integer, PolyCubicSpline> resultsMap) {
		
		/*
		 * Creates an instance of the PolyCubicSpline object with its internal data arrays 
		 */
		PolyCubicSpline interpolatedResponseSurface = new PolyCubicSpline(
				xValues,
				yValues
				);
		
		resultsMap.put(objectiveIndex, interpolatedResponseSurface);
	}

	/**
	 * The evaluate method gives a candidate solution to the problem. 
	 */
	@Override
	public void evaluate(Solution solution) {

		double[] xArray = new double[solution.getNumberOfVariables()];
		for(int i=0; i<numberOfVariables; i++) 
			xArray[i] = ((RealVariable)solution.getVariable(i)).getValue(); 

		for(int obj=0; obj<numberOfObjectives; obj++) {

			if(numberOfConstraints <= 0) {
				solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
			}
			else {
				for(int i=0; i<numberOfConstraints; i++) {
					
					ConstraintsViolationConditionEnum violatingCondition = constraintsDictionary.get(i)._1();
					List<Double> constraintValues = constraintsDictionary.get(i)._2();
					
					if(violatingCondition.equals(ConstraintsViolationConditionEnum.LESS_THAN)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint >= constraintValues.get(0))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
					else if(violatingCondition.equals(ConstraintsViolationConditionEnum.LESS_EQUAL_THAN)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint > constraintValues.get(0))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
					else if(violatingCondition.equals(ConstraintsViolationConditionEnum.BIGGER_THAN)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint <= constraintValues.get(0))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
					else if(violatingCondition.equals(ConstraintsViolationConditionEnum.BIGGER_EQUAL_THAN)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint < constraintValues.get(0))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
					else if(violatingCondition.equals(ConstraintsViolationConditionEnum.WITHIN_INTERVAL)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint <= constraintValues.get(0)
								|| interpolatedConstraint <= constraintValues.get(1))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
					else if(violatingCondition.equals(ConstraintsViolationConditionEnum.OUTSIDE_INTERVAL)) {
						double interpolatedConstraint = getInterpolatedConstraintsValue(i, xArray);
						if(interpolatedConstraint >= constraintValues.get(0)
								&& interpolatedConstraint >= constraintValues.get(1))
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray));
						else {
							solution.setObjective(obj, getInterpolatedResponseSurfaceValue(obj, xArray) + Double.MAX_VALUE);
							break;
						}
					}
				}
			}
		}
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
	
	public Map<Integer, PolyCubicSpline> getInterpolatedConstraintsMap() {
		return interpolatedConstraintsMap;
	}

	public void setInterpolatedConstraintsMap(Map<Integer, PolyCubicSpline> interpolatedConstraintsMap) {
		this.interpolatedConstraintsMap = interpolatedConstraintsMap;
	}

	public double getInterpolatedResponseSurfaceValue (Integer index, double[] xArray) {
		return interpolatedResponseSurfaceMap.get(index).interpolate(xArray);
	}
	
	public double getInterpolatedConstraintsValue (Integer index, double[] xArray) {
		return interpolatedConstraintsMap.get(index).interpolate(xArray);
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

	public Boolean[] getMaximizationProblemConditionArray() {
		return maximizationProblemConditionArray;
	}

	public void setMaximizationProblemConditionArray(Boolean[] maximizationProblemConditionArray) {
		this.maximizationProblemConditionArray = maximizationProblemConditionArray;
	}

	public String[] getObjectivesLabelArray() {
		return objectivesLabelArray;
	}

	public void setObjectivesLabelArray(String[] objectivesLabelArray) {
		this.objectivesLabelArray = objectivesLabelArray;
	}

	public String[] getVariablesLabelArray() {
		return variablesLabelArray;
	}

	public void setVariablesLabelArray(String[] variablesLabelArray) {
		this.variablesLabelArray = variablesLabelArray;
	}

	public List<Tuple2<ConstraintsViolationConditionEnum, List<Double>>> getConstraintsDictionary() {
		return constraintsDictionary;
	}

	public void setConstraintsDictionary(List<Tuple2<ConstraintsViolationConditionEnum, List<Double>>> constraintsDictionary) {
		this.constraintsDictionary = constraintsDictionary;
	}

}
