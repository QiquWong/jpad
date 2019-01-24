package database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;

public class DatabaseInterpolationUtils {

	public static void createMultiDimensionalMatrix (
			int numberOfVariable, 
			int index,
			List<double[]> variableArrayList,
			double[] values, 
			Map<List<Integer>, Integer> interpolatingMatrixIndexes,
			Map<Integer, MyInterpolatingFunction> outputMap,
			double[] inputLowerBounds, double[] inputUpperBounds
			) {

		if(numberOfVariable != variableArrayList.size()) {
			System.err.println("\tWARNING: The number of variables does not match the size of the variable array! ... terminating");
			System.exit(1);
		}
		
		if(values.length < interpolatingMatrixIndexes.size()) {
			List<Double> newValues = MyArrayUtils.convertArrayDoublePrimitiveToList(values);
			while (values.length != interpolatingMatrixIndexes.size()) {
				newValues.add(0.0);
				values = MyArrayUtils.convertToDoublePrimitive(newValues);
			}
		}
		
		List<List<Integer>> matrixIndexSet = new ArrayList<>(interpolatingMatrixIndexes.keySet());
		List<Integer> matrixIndexValues = new ArrayList<>(interpolatingMatrixIndexes.values());
		MyInterpolatingFunction interpolatedResponseSurface = new MyInterpolatingFunction();
		interpolatedResponseSurface.setxMin(inputLowerBounds[0]);
		interpolatedResponseSurface.setyMin(inputLowerBounds[1]);
		interpolatedResponseSurface.setzMin(inputLowerBounds[2]);
		interpolatedResponseSurface.setkMin(inputLowerBounds[3]);
		interpolatedResponseSurface.setxMax(inputUpperBounds[0]);
		interpolatedResponseSurface.setyMax(inputUpperBounds[1]);
		interpolatedResponseSurface.setzMax(inputUpperBounds[2]);
		interpolatedResponseSurface.setkMax(inputUpperBounds[3]);
		double[] variableMatrixX1;
		double[] variableMatrixX2;
		double[] variableMatrixX3;
		double[] variableMatrixX4;
		
		switch (numberOfVariable) {
		case 2:
			variableMatrixX1 = variableArrayList.get(0);
			variableMatrixX2 = variableArrayList.get(1);
			double[][] dataMatrix2 = new double [variableArrayList.get(0).length][variableArrayList.get(1).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				dataMatrix2 [matrixIndexSet.get(i).get(0)][matrixIndexSet.get(i).get(1)] = values[matrixIndexValues.get(i)];
			
			interpolatedResponseSurface.interpolateBilinear(variableMatrixX1, variableMatrixX2, dataMatrix2);
			
			outputMap.put(index, interpolatedResponseSurface);
			
			break;
		case 3:
			variableMatrixX1 = variableArrayList.get(0);
			variableMatrixX2 = variableArrayList.get(1);
			variableMatrixX3 = variableArrayList.get(2);
			double[][][] dataMatrix3= new double [variableArrayList.get(0).length][variableArrayList.get(1).length][variableArrayList.get(2).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				dataMatrix3 [matrixIndexSet.get(i).get(0)][matrixIndexSet.get(i).get(1)][matrixIndexSet.get(i).get(2)] = values[matrixIndexValues.get(i)];
			
			interpolatedResponseSurface.interpolateTrilinear(variableMatrixX1, variableMatrixX2, variableMatrixX3, dataMatrix3);
			
			outputMap.put(index, interpolatedResponseSurface);
			
			break;
		case 4:
			variableMatrixX1 = variableArrayList.get(0);
			variableMatrixX2 = variableArrayList.get(1);
			variableMatrixX3 = variableArrayList.get(2);
			variableMatrixX4 = variableArrayList.get(3);
			double[][][][] dataMatrix4 = new double [variableArrayList.get(0).length][variableArrayList.get(1).length][variableArrayList.get(2).length][variableArrayList.get(3).length];
			
			for(int i=0; i<interpolatingMatrixIndexes.size(); i++)
				dataMatrix4 [matrixIndexSet.get(i).get(0)][matrixIndexSet.get(i).get(1)][matrixIndexSet.get(i).get(2)][matrixIndexSet.get(i).get(3)] = values[matrixIndexValues.get(i)];
			
			interpolatedResponseSurface.interpolateQuadrilinear(variableMatrixX1, variableMatrixX2, variableMatrixX3, variableMatrixX4, dataMatrix4);
			
			outputMap.put(index, interpolatedResponseSurface);
			
			break;
		default:
			break;
		}
		
		return;
	}
	
	public static Map<List<Integer>, Integer> buildInterpolatingMatrixIndexes (
			List<List<List<Integer>>> columnIndexList,
			List<Integer> indexList,
			Map<List<Integer>, Integer> interpolatingMatrixIndexes,
			int numberOfInput
			) {
		
		for(int i=0; i<columnIndexList.get(columnIndexList.size()-1).size(); i++) {
			
			List<List<Integer>> columnIndexListArrays = new ArrayList<>();
			for(int j=0; j<numberOfInput; j++) 
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
	public static <T> void permutationsImpl(List<List<T>> inputPermutationList, Collection<List<T>> res, int d, List<T> copy2) {
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
	
}
