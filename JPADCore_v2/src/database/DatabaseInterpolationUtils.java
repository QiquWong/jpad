package database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import flanagan.interpolation.PolyCubicSpline;

public class DatabaseInterpolationUtils {

	public static void createMultiDimensionalMatrix (
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
	public static void interpolateMultidimensionalMatrix(int objectiveIndex, Object xValues, Object yValues, Map<Integer, PolyCubicSpline> resultsMap) {
		
		/*
		 * Creates an instance of the PolyCubicSpline object with its internal data arrays 
		 */
		PolyCubicSpline interpolatedResponseSurface = new PolyCubicSpline(
				xValues,
				yValues
				);
		
		resultsMap.put(objectiveIndex, interpolatedResponseSurface);
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
