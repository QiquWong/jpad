package standaloneutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import javaslang.Tuple;
import javaslang.Tuple2;

public final class MyArrayUtils {

	private MyArrayUtils() {}

	/**
	 * Concatenate arrays
	 * @param d
	 * @return
	 */
	public static double[] concat(double[] ... d) {

		List<Double> list = new ArrayList<Double>();
		for (int i=0; i< d.length; i++)
			for (int j=0; j<d[i].length; j++)
				list.add(d[i][j]);
		return ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
	}
	

	public static Double average(Double[] x) {
		
		int numberOfElements = x.length;
		double arraySum = sumArrayElements(x);
		
		return arraySum/(double) numberOfElements;
		
	}
	
	/**
	 * Concatenate two matrices a,b to get a new matrix:
	 * |a|
	 * |b|
	 *
	 * a and b must have the same number of columns
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[][] concatMatricesVertically(double[][] a, double[][] b) {

		if (a[0].length != b[0].length){
			System.out.println("Input matrices have different number of columns");
			return null;
		}

		double[][] result = new double[a.length+b.length][a[0].length];
		for (int i=0; i<a.length; i++) {
			for (int j=0; j<result[0].length; j++) {
				result[i][j] = a[i][j];
				result[a.length+i][j] = b[i][j];
			}
		}
		return result;
	}

	/**
	 * Get an array with duplicate elements
	 * @param d an array (e.g., [x, y, z])
	 * @return an array (e.g., [x, x, y, y, z, z])
	 */
	public static double[] doMitosis(double[] d) {
		double[] dd = new double[2*d.length];

		for (int i=0; i<d.length; i++) {
			dd[i] = d[i];
			dd[i+1] = d[i];
		}

		return dd;
	}

	/**
	 *
	 * @param d an array [x, y, z]
	 * @return an array [x, y, z, x, y, z]
	 */
	public static double[] duplicateArray(double[] d) {
		double[] dd = new double[2*d.length];

		for (int i=0; i<d.length; i++) {
			dd[i] = d[i];
			dd[d.length + i] = d[i];
		}

		return dd;
	}

	/**
	 * Remove zeros from the end of an array
	 * @param d an array [x, y, z, 0., 0., 0., ...]
	 * @return an array [x, y, z]
	 */
	public static double[] stripTrailingZeros(double[] d) {

		int len = d.length;
		for (int i=d.length-1; i==0; i--) {
			if (d[i] == 0.) len = i+1;
			else break;
		}

		double[] dd = new double[len];
		for (int i=0; i<len; i++) {
			dd[i] = d[i];
		}

		return dd;
	}

	public static String[] capitalizeFullyArray(String[] strings) {

		String[] newString = new String[strings.length];

		for(int i=0; i < strings.length; i++) {
			newString[i] = WordUtils.capitalizeFully(strings[i]);
		}
		return newString;
	}

	public static String[] enumToStringArray(Enum[] e) {

		String[] s = new String[e.length];

		for (int i=0; i < e.length; i++) {
			s[i] = e[i].name();
		}

		return s;
	}

	public static double[] removeDuplicates(double[] x) {
		Set<Double> set = new TreeSet<Double>();
		set.addAll(Arrays.asList(ArrayUtils.toObject(x)));
		return ArrayUtils.toPrimitive(set.toArray(new Double[set.size()]));
	}

	/**
	 * Search for maximum value in a list of Double[]
	 */
	public static double searchMax(List<Double[]> dList) {

		double max = Double.MIN_VALUE;

		for(Double[] x : dList){
			for (Double xx : x) {
				if (xx > max) {
					max = xx;
				}
			}
		}
		return max;
	}
	
	public static int getIndexOfMin(double[] d) {
		// http://stackoverflow.com/questions/31116190/java-8-find-index-of-minimum-value-from-a-list
		int minIdx = IntStream.range(0,d.length)
	            .reduce((i,j) -> d[i] > d[j] ? j : i)
	            .getAsInt();  // or throw
		return minIdx;
	}

	public static int getIndexOfMin(Double[] d) {
		// http://stackoverflow.com/questions/31116190/java-8-find-index-of-minimum-value-from-a-list
		int minIdx = IntStream.range(0,d.length)
	            .reduce((i,j) -> d[i] > d[j] ? j : i)
	            .getAsInt();  // or throw
		return minIdx;
	}

	public static int getIndexOfMax(double[] d) {
		// http://stackoverflow.com/questions/31116190/java-8-find-index-of-minimum-value-from-a-list
		int maxIdx = IntStream.range(0,d.length)
	            .reduce((i,j) -> d[i] < d[j] ? j : i)
	            .getAsInt();  // or throw
		return maxIdx;
	}

	public static int getIndexOfMax(Double[] d) {
		// http://stackoverflow.com/questions/31116190/java-8-find-index-of-minimum-value-from-a-list
		int maxIdx = IntStream.range(0,d.length)
	            .reduce((i,j) -> d[i] < d[j] ? j : i)
	            .getAsInt();  // or throw
		return maxIdx;
	}

	public static double getMax(List<Double> d) {
		return Collections.max(d);
	}

	public static double getMin(List<Double> d) {
		return Collections.min(d);
	}

	public static double getMin(Double[] d) {
		return Collections.min(Arrays.asList(d));
	}

	public static double getMax(Double[] d) {
		return Collections.max(Arrays.asList(d));
	}

	public static double getMax(double[] ... d) {

		List<Double> dd = new ArrayList<Double>();
		for (double[] x:d)
			if (x!=null) {
				for (double xx:x)
					dd.add(xx);
			}

		return getMax(dd);
	}

	public static double getMin(double[] ... d) {

		List<Double> dd = new ArrayList<Double>();
		for (double[] x:d)
			if (x!=null) {
				for (double xx:x)
					dd.add(xx);
			}

		return getMin(dd);
	}

	/**
	 * Search for minimum value in a list of Double[]
	 */
	public static double searchMin(List<Double[]> dList) {

		double min = Double.MAX_VALUE;

		for(Double[] x : dList){
			for (Double xx : x) {
				if (xx < min){
					min = xx;
				}
			}
		}
		return min;
	}

	public static double getMean(double[] data) {
		double sum = 0.;
		for (double d : data) sum += d;
		return sum/(double)data.length;
	}

	// Function to find the index of an element in a primitive array in Java
	public static int getIndexOf(int[] a, int target) {
		return Arrays.stream(a) 					// IntStream
					.boxed()						// Stream<Integer>
					.collect(Collectors.toList())   // List<Integer>
					.indexOf(target);
	}	
	// Function to find the index of an element in a primitive array in Java
	public static int getIndexOf(double[] a, double target) {
		return Arrays.stream(a) 					// IntStream
					.boxed()						// Stream<Integer>
					.collect(Collectors.toList())   // List<Integer>
					.indexOf(target);
	}
	
	// Generic function to find the index of an element in an object array in Java
	public static<T> int getIndexOf(T[] a, T target) {
		return Arrays.asList(a).indexOf(target);
	}	

	// list of values MUST be sorted
//	public static Tuple2<Integer, Integer> getBracketingIndicesOf(List<Double> a, Double target) {
//		int i0, i1;
//		i0 = -1;
//		i1 = -1;
//		
//		i0 = a.indexOf(target);
//		
//		if (i0 == -1) {
//			if (target < a.get(0))
//				return Tuple.of(-1, 1);
//			if (target > a.get(a.size()))
//				return Tuple.of(a.size(), -1);
//			if ( 
//				(target > a.get(0))
//				&& 
//				(target < a.get(a.size()))
//				) {
//					// TODO: check!!!
//					for (int i=0; i < a.size(); i++) {
//						if (target > a.get(i)) {
//							i0 = i;
//							i1 = i0+1;
//							return Tuple.of(i0, i1);
//						}
//					}			
//				}
//		}
//	}	
	
	
	// http://stackoverflow.com/questions/11447780/convert-two-dimensional-array-to-list-in-java
	public static <T> List<List<T>> convert2DArrayToList(T[][] twoDArray) {
	    List<List<T>> list = new ArrayList<>(twoDArray.length);
	    for (T[] subarray : twoDArray) {
	    	List<T> sublist = new ArrayList<>(subarray.length);
	    	Collections.addAll(sublist, subarray);
	    	list.add(sublist);
	    }
	    return list;
	}

	public static List<Double[]> convertTwoDimensionArrayToListDoubleArray(double[][] matrix) {
		
		List<Double[]> resultList = new ArrayList<>();
		
		for(int i=0; i<matrix.length; i++) {
			
			resultList.add(MyArrayUtils.convertFromDoubleToPrimitive(matrix[i]));
			
		}
		
		return resultList;
	}
	
	public static List<Double[]> convertTwoDimensionArrayToListDoubleArray(Double[][] matrix) {
		
		List<Double[]> resultList = new ArrayList<>();
		
		for(int i=0; i<matrix.length; i++) {
			
			resultList.add(matrix[i]);
			
		}
		
		return resultList;
	}
	
	public static <T> List<T> extractColumnOf2DArrayToList(T[][] twoDArray, int c) {

		if (twoDArray.length == 0)
			return null;
		else {
			if (c >= twoDArray[0].length)
				return null;
		}
		if (c < 0)
			return null;

	    List<T> list = new ArrayList<>(twoDArray.length);
	    for (T[] subarray : twoDArray) {
	    	T element = subarray[c];
	    	list.add(element);
	    }
	    return list;
	}


	//	public int findFirstGreaterThan(double[] d1, double[] d2) {
	//
	//		if (search)
	//
	//		for (int i=)
	//	}

	public static double[] convertToDoublePrimitive(Double[] vec){

		double[] vec_d = ArrayUtils.toPrimitive(vec);

		return vec_d;
	}

	public static double[] convertToDoublePrimitive(List<Double> list){

		double[] vec_d = ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));

		return vec_d;
	}

	public static Integer[] convertListOfIntegerToIntegerArray (List<Integer> list){

		Integer[] vec_i = new Integer[list.size()];
		for(int i=0; i<list.size(); i++) 
			vec_i[i] = list.get(i);

		return vec_i;
	}
	
	public static String[] convertListStringToStringArray (List<String> list) {
		return
				list.toArray(new String[list.size()]);
	}
	
	public static List<Double[]> convertFromListOfDoublePrimitive(List<double[]> list){

		return 
				list.stream()
				.map(x -> convertFromDoubleToPrimitive(x))
				.collect(Collectors.toList());
		
	}
	
	public static Double[] convertFromDoubleToPrimitive(double[] vec) {
		
		Double[] vec_D = new Double[vec.length];
		
		for (int i=0; i<vec.length; i++)
			vec_D[i] = Double.valueOf(vec[i]);
		
		return vec_D;
		
	}
	
	public static Double[] convertListOfDoubleToDoubleArray(List<Double> list){

		Double[] vec_d = new Double[list.size()];

		for(int i=0; i<list.size(); i++)
			vec_d[i] = list.get(i);
		
		return vec_d;
	}
	
	public static List<Double> convertDoubleArrayToListDouble(Double[] vec){ 

		List<Double> list = new ArrayList<Double>();

		for(int i=0; i<vec.length; i++)
			list.add(vec[i]);
		
		return list;
	}
	
	public static List<Integer> convertIntArrayToListInteger(int[] vec){ 

		List<Integer> list = new ArrayList<Integer>();

		for(int i=0; i<vec.length; i++)
			list.add(vec[i]);
		
		return list;
	}
	
	public static <T extends Quantity> List<Amount<T>> convertDoubleArrayToListOfAmount(double[] d, Unit unit) {

		if ( d.length == 0 ) return null;

		List<Amount<T>> result = new ArrayList<Amount<T>>();

		for (int k = 0; k < d.length; k++) {
			result.add(Amount.valueOf(d[k], unit));
		}

		return result;
	}

	public static <T extends Quantity> List<Amount<T>> convertDoubleArrayToListOfAmount(Double[] d, Unit unit) {

		if ( d.length == 0 ) return null;

		List<Amount<T>> result = new ArrayList<Amount<T>>();

		for (int k = 0; k < d.length; k++) {
			result.add(Amount.valueOf(d[k], unit));
		}

		return result;
	}

	public static <T extends Quantity> double[] convertListOfAmountTodoubleArray(List<Amount<T>> theListOfObjects) {

		if ( theListOfObjects.size() == 0 ) return null;

		double[] result = new double[theListOfObjects.size()];

		for (int k = 0; k < theListOfObjects.size(); k++) {
			result[k] = theListOfObjects.get(k).getEstimatedValue();
		}
		return result;
	}
	
	public static double[] convertListOfAmountodoubleArray(List<Amount<?>> theListOfObjects) {

		if ( theListOfObjects.size() == 0 ) return null;

		double[] result = new double[theListOfObjects.size()];

		for (int k = 0; k < theListOfObjects.size(); k++) {
			result[k] = theListOfObjects.get(k).getEstimatedValue();
		}
		return result;
	}

	public static <T extends Quantity> Double[] convertListOfAmountToDoubleArray(List<Amount<T>> theListOfObjects) {

		if ( theListOfObjects.size() == 0 ) return null;

		Double[] result = new Double[theListOfObjects.size()];

		for (int k = 0; k < theListOfObjects.size(); k++) {
			result[k] = theListOfObjects.get(k).getEstimatedValue();
		}
		return result;
	}

	public static double[] ones(int dim) {
		double[] d = new double[dim];
		for (int i=0; i<dim; i++) {
			d[i] = 1.;
		}
		return d;
	}

	public static Double[] zeros(int dim) {
		Double[] d = new Double[dim];
		for (int i=0; i<dim; i++) {
			d[i] = 0.;
		}
		return d;
	}

	public static double[] fill(double d, int dim) {
		double[] dd = new double[dim];
		for (int i=0; i<dim; i++) dd[i] = d;
		return dd;
	}

	public static RealVector onesRV(int dim) {
		RealVector d = new ArrayRealVector(dim);
		for (int i=0; i<dim; i++) {
			d.setEntry(i, 1.);
		}
		return d;
	}

	public static double[] sqrt(double[] dd){
		double[] d = new double[dd.length];
		for (int i=0; i<dd.length; i++) {
			d[i] = Math.sqrt(dd[i]);
		}
		return d;
	}

	public static double[] sqrtRV(double[] dd){
		double[] d = new double[dd.length];
		for (int i=0; i<dd.length; i++) {
			d[i] = Math.sqrt(dd[i]);
		}
		return d;
	}


	/** Compute a cosine-spaced array of values
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return double[]
	 */
	public static double[] cosineSpace(double a, double b, int n){
		if (n <= 0) return null;
		if (b <= a) return null;
		double [] ret = new double[n];
		double r = 0.5*(b - a);
		double dth = Math.PI/((double)(n-1));
		double th;
		for(int i = 0; i < ret.length; i++){
			th = 0.0 + (double)i * dth;
			ret[i] = a + (1.0 - Math.cos(th))*r;
		}
		return ret;
	}

	/** Compute a cosine-spaced array of values (finer spacing near first and last boundary value)
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return Double[]
	 */
	public static Double[] cosineSpaceDouble(double a, double b, int n){
		return MyArrayUtils.convertFromDoubleToPrimitive(
				cosineSpace(a, b, n)
				);
	}

	/** Compute a half-cosine-spaced array of values (finer spacing near first boundary value)
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return double[]
	 */
	public static double[] halfCosine1Space(double a, double b, int n){
		if (n <= 0) return null;
		if (b <= a) return null;
		double [] ret = new double[n];
		double r = (b - a);
		double dth = 0.5*Math.PI/((double)(n-1));
		double th;
		for(int i = 0; i < ret.length; i++){
			th = 0.0 + (double)i * dth;
			ret[i] = a + (1.0 - Math.cos(th))*r;
		}
		return ret;
	}

	/** Compute a half-cosine-spaced array of values (finer spacing near first boundary value)
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return Double[]
	 */
	public static Double[] halfCosine1SpaceDouble(double a, double b, int n){
		return MyArrayUtils.convertFromDoubleToPrimitive(
				halfCosine1Space(a, b, n)
				);
	}
	

	/** Compute a half-cosine-spaced array of values (finer spacing near second boundary value)
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return double[]
	 */
	public static double[] halfCosine2Space(double a, double b, int n){
		if (n <= 0) return null;
		if (b <= a) return null;
		double [] ret = new double[n];
		double r = (b - a);
		double dth = 0.5*Math.PI/((double)(n-1));
		double th;
		for(int i = 0; i < ret.length; i++){
			th = 0.5*Math.PI - (double)i * dth;
			ret[i] = a + r*Math.cos(th);
		}
		return ret;
	}

	/** Compute a half-cosine-spaced array of values (finer spacing near last boundary value)
	 *
	 * @author Agodemar
	 * @param a, b, n
	 * @return Double[]
	 */
	public static Double[] halfCosine2SpaceDouble(double a, double b, int n){
		return MyArrayUtils.convertFromDoubleToPrimitive(
				halfCosine2Space(a, b, n)
				);
	}
	

	public static double[] sumNumberToArrayEBE(double[] a, double b) {
		return Arrays.stream(a).map(x -> x + b).toArray();
	}
	
	@Deprecated
	public static double[] sumArraysElementByElement(double[] a, double[] b) {

		if(a.length != b.length){
			System.out.println("The arrays must be the same length");
			return null;
		}
		double[] c = new double[a.length];
		for (int i=0; i<a.length; i++)
			c[i] = a[i]+b[i];

		return c;
	}

	@Deprecated
	public static double[] subtractArraysElementByElement(double[] a, double[] b) {

		if(a.length != b.length){
			System.out.println("The arrays must be the same length");
			return null;
		}
		double[] c = new double[a.length];
		for (int i=0; i<a.length; i++)
			c[i] = a[i]-b[i];

		return c;
	}

	public static Double[] subtractArrayEbE(Double[] a, Double[] b) {
		
		
		if(a.length < b.length) {
			System.err.println("A and B must have the same length");
			System.exit(1);
		}

		Double[] result = new Double[a.length];
		for(int i=0; i<a.length; i++)
			result[i] = a[i] - b[i];
		
		return result;
		
	}
	
	/**
	 * Compute product of the elements of an array
	 *
	 * @author Lorenzo Attanasio
	 * @param d
	 * @return
	 */
	public static Double productArray(Double[] d){

		Double product = 1.;
		for(int i=0; i<d.length; i++){
			product = product*d[i];
		}

		return product;
	}


	/**
	 * Compute sum of the elements of an array
	 *
	 * @author Lorenzo Attanasio
	 * @param d
	 * @return
	 */
	public static Double sumArrayElements(Double[] d){

		Double sum = 0.;
		for(int i=0; i<d.length; i++){
			sum = sum + d[i];
		}

		return sum;
	}

	@Deprecated
	public static double[] multiplyArrays(double[] a, double[] b) {
		double[] result = new double[a.length];
		for (int i=0; i < a.length; i++) {
			result[i] = a[i]*b[i];
		}

		return result;
	}

	@Deprecated
	public static double[] multiply(double[] d, double dd) {
		double[] result = new double[d.length];
		for (int i=0; i < d.length; i++) {
			result[i] = d[i]*dd;
		}
		return result;
	}

	/**
	 * This method accepts an array and multiply each element by the double value 
	 * given as second input.
	 * 
	 * @author Vittorio Trifari
	 * @param vec
	 * @return
	 */
	public static double[] scaleArray(double[] vec, double d) {
		return Arrays.stream(vec).map(i -> i*d).toArray();
	}
	
	/**
	 * This method accepts an array and multiply each element by the double value 
	 * given as second input.
	 * 
	 * @author Vittorio Trifari
	 * @param vec
	 * @return
	 */
	public static List<Double> multiplyListEbE(List<Double> list1, List<Double> list2) {
		return list1.stream().map(l1 -> l1*list2.get(list1.indexOf(l1))).collect(Collectors.toList());
	}
	
	
	public static double[] abs(double[] d) {

		double[] dd = new double[d.length];
		for (int i=0; i<d.length; i++)
			dd[i] = Math.abs(d[i]);

		return dd;
	}

	public static double[] intersectArraysSimple(double[] d1, double[] d2) {

		if (d1.length != d2.length) {
			System.out.println("Input arrays must have the same length");
			return null;
		}

		double[] diff = MathArrays.ebeSubtract(d1, d2);
		//		List<Integer> signChangeList = new ArrayList<Integer>();
		double[] intersectionArray = new double[d1.length];

		for (int i=1; i < diff.length; i++){
			if (Math.signum(diff[i]) != Math.signum(diff[i-1])) {
				//				signChangeList.add(i);
				intersectionArray[i] = (d1[i] + d2[i])/4. + (d1[i-1] + d2[i-1])/4.;
			}
		}

		return intersectionArray;
	}

	public static Double rootFindingBrent(double[] x, double[] y,
			double lowerBound, double upperBound, AllowedSolution solutionSide) {

		if (x.length != y.length) {
			System.out.println("rootFindingBrent: Input arrays must have the same length");
			return null;
		}

		UnivariateFunction function = MyMathUtils.interpolate1DLinear(x, y);
		try {
			return new BracketingNthOrderBrentSolver(1.0e-9, 1.0e-5, 5)
			.solve(100, function, lowerBound, upperBound, solutionSide);
		} catch (TooManyEvaluationsException | NumberIsTooLargeException | NoBracketingException e) {
			return null;
		}

	}


	public static Double intersectArraysBrent(double[] x, double[] d1, double[] d2,
			double lowerBound, double upperBound, AllowedSolution solutionSide) {
		if (d1.length != d2.length) {
			System.out.println("intersectArraysBrent: Input arrays must have the same length");
			return null;
		}

		return rootFindingBrent(x, MathArrays.ebeSubtract(d1, d2), lowerBound, upperBound, solutionSide);
	}


	public static double[] getNonZeroValues(double[] d) {

		List<Double> list = new ArrayList<Double>();

		for (int i=0; i<d.length; i++) {
			if (d[i] != 0.) list.add(d[i]);
		}

		return ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
	}

	public static int[] getNonZeroValuesIndex(double[] d) {

		List<Integer> list = new ArrayList<Integer>();

		for (int i=0; i<d.length; i++) {
			if (d[i] != 0.) list.add(i);
		}

		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}

	public static Integer sumArrayElements(Integer[] integer){

		Integer sum = 0;
		for(int i=0; i<integer.length; i++){
			sum = sum + integer[i];
		}

		return sum;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param d
	 * @return an array where each element is d[i+1]-d[i]
	 */
	public static double[] diff(double[] d) {

		double[] dd = new double[d.length-1];
		for (int i=0; i<dd.length; i++) {
			dd[i] = d[i+1] - d[i];
		}

		return dd;
	}

	/**
	 *
	 * @author Lorenzo Attanasio
	 *
	 * @param start
	 * @param end
	 * @param nPoints
	 * @return
	 */
	public static double[] linspace(double start, double end, int nPoints) {
		double[] d = new double[nPoints];
		double step = (end-start)/(nPoints-1);
		d[0] = start;
		d[d.length-1] = end;
		for (int i=1; i<nPoints-1; i++)
			d[i] = d[i-1] + step;

		return d;
	}

	/**
	 * Overload of the double[] linspace method for Double[]
	 *
	 * @author Vittorio Trifari
	 *
	 * @param start
	 * @param end
	 * @param nPoints
	 * @return
	 */
	public static Double[] linspaceDouble(double start, double end, int nPoints) {
		Double[] d = new Double[nPoints];
		Double step = (end-start)/(nPoints-1);
		d[0] = start;
		d[d.length-1] = end;
		for (int i=1; i<nPoints-1; i++)
			d[i] = d[i-1] + step;

		return d;
	}
	
	/**
	 * Overload of the double[] linspace method for Double[]
	 *
	 * @author Vittorio Trifari
	 *
	 * @param start
	 * @param end
	 * @param nPoints
	 * @return
	 */
	public static int[] linspaceInt(int start, int end, int nPoints) {
		int[] d = new int[nPoints];
		int step = (end-start)/(nPoints-1);
		d[0] = start;
		d[d.length-1] = end;
		for (int i=1; i<nPoints-1; i++)
			d[i] = d[i-1] + step;

		return d;
	}
	
	/**
	 * This method allows to print a list of amount in format name (unit) --> [ value1 (separator) value2...]. This is useful to copy array in excel
	 * @author Manuela Ruocco
	 *
	 * @param start
	 * @param end
	 * @param nPoints
	 * @return
	 */
public static <T extends Quantity> void printListOfAmountWithUnitsInEvidence( List<Amount<T>> list, String name, String separator) {
		
		System.out.print("\n" + name + " (" + list.get(0).getUnit() + ") --> [");
		for (int i=0; i<list.size()-1; i++){
		System.out.print(list.get(i).getEstimatedValue() + " " + separator + " ");
		}
		System.out.print(list.get(list.size()-1).getEstimatedValue() + " ] ");
	}

public static <T extends Quantity> String ListOfAmountWithUnitsInEvidenceString( List<Amount<T>> list, String name, String separator) {
	
	String st = null;
	st =  name + " (" + list.get(0).getUnit() + ") --> [";
			
	for (int i=0; i<list.size()-1; i++){
		st = st + list.get(i).getEstimatedValue() + " " + separator + " ";
	}
	st = st + list.get(list.size()-1).getEstimatedValue() + "] " + "\n";

	return st;
}

/**
 * This method returns the index of the LAST SORTED ELEMENT of the array if it isn't sorted. If the array input is already sorted, the method returns -1
 * @author Manuela Ruocco
 *
 * @param array input
 * @return index of last sorted element is the array isn't alseady sorted. Else -1
 */
public static int isSorted( Double[] arrayInput) {

	for(int i=1; i < arrayInput.length; i++) {
		if(arrayInput[i] < arrayInput[i-1]) {
			return i-1;
		}
	}
	return -1;
}
}

