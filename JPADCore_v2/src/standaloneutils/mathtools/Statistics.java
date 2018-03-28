/*
*   Statistics  -- A collection of useful statistical methods.
*
*   Copyright (C) 2001-2004 by Joseph A. Huwaldt
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*   Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package standaloneutils.mathtools;

import java.util.Arrays;


/**
*  A utility class containing collection of useful static routines for calculating
*  certain statistical properties.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  March 7, 2001
*  @version  November 19, 2004
**/
public class Statistics {
	
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Prevent the user from instantiating this class.
	**/
	private Statistics() { }
	
	
	/**
	*  Returns the minimum value in an array of sample data.
	*  The minimum value is defined as the most negative value.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The minimum value in the sample data array.
	**/
	public static final double min( double[] arr ) {
		double min = Double.MAX_VALUE;
		int length = arr.length;
		
		for (int i=0; i < length; ++i)
			if (arr[i] < min)
				min = arr[i];
		
		return min;
	}

	/**
	*  Returns the minimum value in an array of sample data.
	*  The minimum value is defined as the most negative value.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The minimum value in the sample data array.
	**/
	public static final float min( float[] arr ) {
		float min = Float.MAX_VALUE;
		int length = arr.length;
		
		for (int i=0; i < length; ++i)
			if (arr[i] < min)
				min = arr[i];
		
		return min;
	}

	/**
	*  Returns the maximum value in an array of sample data.
	*  The maximum value is defined as the most positive value.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The maximum value in the sample data array.
	**/
	public static final double max( double[] arr ) {
		double max = -Double.MAX_VALUE;
		int length = arr.length;
		
		for (int i=0; i < length; ++i)
			if (arr[i] > max)
				max = arr[i];
		
		return max;
	}

	/**
	*  Returns the maximum value in an array of sample data.
	*  The maximum value is defined as the most positive value.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The maximum value in the sample data array.
	**/
	public static final float max( float[] arr ) {
		float max = -Float.MAX_VALUE;
		int length = arr.length;
		
		for (int i=0; i < length; ++i)
			if (arr[i] > max)
				max = arr[i];
		
		return max;
	}

	/**
	*  Returns the range of the data in the specified array.
	*  Range is the difference between the maximum and minimum
	*  values in the data set.
	*
	*  @param  arr  An array of sample data values.
	*  @return The range of the data in the input array.
	**/
	public static final double range(double[] arr) {
		return max(arr) - min(arr);
	}
	
	/**
	*  Returns the range of the data in the specified array.
	*  Range is the difference between the maximum and minimum
	*  values in the data set.
	*
	*  @param  arr  An array of sample data values.
	*  @return The range of the data in the input array.
	**/
	public static final float range(float[] arr) {
		return max(arr) - min(arr);
	}
	
	/**
	*  Returns the sum of all the elements in the specified data array.
	*
	*  @param  arr  An array of sample data values to be added together.
	*  @return The sum of all the sample data values in arr.
	**/
	public static final double sum(double[] arr) {
		double sum = 0;
		int size = arr.length;
		
		for (int i=0; i < size; ++i)
			sum += arr[i];
		
		return sum;
	}
	
	/**
	*  Returns the sum of all the elements in the specified data array.
	*
	*  @param  arr  An array of sample data values to be added together.
	*  @return The sum of all the sample data values in arr.
	**/
	public static final float sum(float[] arr) {
		float sum = 0;
		int size = arr.length;
		
		for (int i=0; i < size; ++i)
			sum += arr[i];
		
		return sum;
	}
	
	/**
	*  Returns the mean or average of an array of data values.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The mean or average of the input data values.
	**/
	public static final double mean( double[] arr ) {
		int size = arr.length;
		double sum = sum(arr);
		return sum/size;
	}

	/**
	*  Returns the mean or average of an array of data values.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The mean or average of the input data values.
	**/
	public static final float mean( float[] arr ) {
		int size = arr.length;
		float sum = sum(arr);
		return sum/size;
	}

	/**
	*  Returns the p-th percentile of values in an array. You can use this
	*  function to establish a threshold of acceptance. For example, you can
	*  decide to examine candidates who score above the 90th percentile (0.9).
	*  The elements of the input array are modified (sorted) by this method.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @param   p    The percentile value in the range 0..1, inclusive.
	*  @return  The p-th percentile of values in an array.  If p is not a multiple
	*           of 1/(n - 1), this method interpolates to determine the value at
	*           the p-th percentile.
	**/
	public static final double percentile( double[] arr, double p ) {
		
		if (p < 0 || p > 1)
			throw new IllegalArgumentException("Percentile out of range.");
		
		//	Sort the array in ascending order.
		Arrays.sort(arr);
		
		//	Calculate the percentile.
		double t = p*(arr.length - 1);
		int i = (int)t;
		
		return ((i + 1 - t)*arr[i] + (t - i)*arr[i + 1]);
	}

	/**
	*  Returns the p-th percentile of values in an array. You can use this
	*  function to establish a threshold of acceptance. For example, you can
	*  decide to examine candidates who score above the 90th percentile (0.9).
	*  The elements of the input array are modified (sorted) by this method.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @param   p    The percentile value in the range 0..1, inclusive.
	*  @return  The p-th percentile of values in an array.  If p is not a multiple
	*           of 1/(n - 1), this method interpolates to determine the value at
	*           the p-th percentile.
	**/
	public static final float percentile( float[] arr, float p ) {
		
		if (p < 0 || p > 1)
			throw new IllegalArgumentException("Percentile out of range.");
		
		//	Sort the array in ascending order.
		Arrays.sort(arr);
		
		//	Calculate the percentile.
		float t = p*(arr.length - 1);
		int i = (int)t;
		
		return ((i + 1 - t)*arr[i] + (t - i)*arr[i + 1]);
	}

	/**
	*  Returns the median of the values in an array. The median is the same
	*  as the 50th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The median (50th percentile) of values in an array.
	*           Interpolation is used if the number of array elements is odd.
	**/
	public static final double median( double[] arr ) {
		return percentile(arr, 0.5);
	}

	/**
	*  Returns the median of the values in an array. The median is the same
	*  as the 50th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The median (50th percentile) of values in an array.
	*           Interpolation is used if the number of array elements is odd.
	**/
	public static final float median( float[] arr ) {
		return percentile(arr, 0.5f);
	}

	/**
	*  Returns the first quartile of the values in an array. The 1st quartile
	*   is the same as the 25th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The first quartile (25th percentile) of values in an array.
	*           Interpolation is used if necissary.
	**/
	public static final double quartile1( double[] arr ) {
		return percentile(arr, 0.25);
	}

	/**
	*  Returns the first quartile of the values in an array. The 1st quartile
	*   is the same as the 25th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The first quartile (25th percentile) of values in an array.
	*           Interpolation is used if necissary.
	**/
	public static final float quartile1( float[] arr ) {
		return percentile(arr, 0.25f);
	}

	/**
	*  Returns the third quartile of the values in an array. The 3rd quartile
	*   is the same as the 75th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The third quartile (75th percentile) of values in an array.
	*           Interpolation is used if necissary.
	**/
	public static final double quartile3( double[] arr ) {
		return percentile(arr, 0.75);
	}

	/**
	*  Returns the third quartile of the values in an array. The 3rd quartile
	*   is the same as the 75th percentile.
	*
	*  @param   arr  An array of sample data values that define relative standing.
	*                The contents of the input array are sorted by this method.
	*  @return  The third quartile (75th percentile) of values in an array.
	*           Interpolation is used if necissary.
	**/
	public static final float quartile3( float[] arr ) {
		return percentile(arr, 0.75f);
	}

	/**
	*  Returns the root mean square of an array of sample data.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The root mean square of the sample data.
	**/
	public static final double rms( double[] arr ) {
		int size = arr.length;
		double sum = 0;
		for (int i=0; i < size; ++i)
			sum += arr[i]*arr[i];
		
		return Math.sqrt(sum/size);
	}

	/**
	*  Returns the root mean square of an array of sample data.
	*
	*  @param   arr  An array of sample data values.
	*  @return  The root mean square of the sample data.
	**/
	public static final float rms( float[] arr ) {
		int size = arr.length;
		float sum = 0;
		for (int i=0; i < size; ++i)
			sum += arr[i]*arr[i];
		
		return (float)Math.sqrt(sum/size);
	}

	/**
	*  Returns the variance of an array of sample data.
	*
	*  @param  arr  An array of sample data values.
	*  @return The variance of the sample data.
	**/
	public static final double variance(double[] arr) {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		double ave = mean(arr);
		
		double var = 0;
		double ep = 0;
		for (int i=0; i < n; ++i) {
			double s = arr[i] - ave;
			ep += s;
			var += s*s;
		}
		
		var = (var - ep*ep/n)/(n-1);
		
		return var;
	}
	
	/**
	*  Returns the variance of an array of sample data.
	*
	*  @param  arr  An array of sample data values.
	*  @return The variance of the sample data.
	**/
	public static final float variance(float[] arr) {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		float ave = mean(arr);
		
		float var = 0;
		float ep = 0;
		for (int i=0; i < n; ++i) {
			float s = arr[i] - ave;
			ep += s;
			var += s*s;
		}
		
		var = (var - ep*ep/n)/(n-1);
		
		return var;
	}
	
	/**
	*  Returns the standard deviation of an array of sample data.
	*
	*  @param  arr  An array of sample data values.
	*  @return The standard deviation of the sample data.
	**/
	public static final double sdev(double[] arr) {
		return Math.sqrt(variance(arr));
	}
	
	/**
	*  Returns the standard deviation of an array of sample data.
	*
	*  @param  arr  An array of sample data values.
	*  @return The standard deviation of the sample data.
	**/
	public static final float sdev(float[] arr) {
		return (float)Math.sqrt(variance(arr));
	}
	
	/**
	*  Returns the skewness of an array of sample data.
	*  Skewness characterises the degree of asymmetry of a distribution
	*  of data around it's mean.
	*
	*  @param  arr  An array of sample data values.
	*  @return The skewness of the sample data.
	**/
	public static final double skew(double[] arr) throws Exception {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		double ave = mean(arr);
		
		double var = 0;
		double skew = 0;
		double ep = 0;
		for (int i=0; i < n; ++i) {
			double s = arr[i] - ave;
			ep += s;
			double p = s*s;
			var += p;
			p *= s;
			skew += p;
		}
		
		var = (var - ep*ep/n)/(n-1);
		double sdev = Math.sqrt(var);
		
		if (var == 0)
			throw new Exception("No skew when variance = 0.");
		
		skew /= n*var*sdev;
		
		return skew;
	}
	
	/**
	*  Returns the skewness of an array of sample data.
	*  Skewness characterises the degree of asymmetry of a distribution
	*  of data around it's mean.
	*
	*  @param  arr  An array of sample data values.
	*  @return The skewness of the sample data.
	**/
	public static final float skew(float[] arr) throws Exception {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		float ave = mean(arr);
		
		float var = 0;
		float skew = 0;
		float ep = 0;
		for (int i=0; i < n; ++i) {
			float s = arr[i] - ave;
			ep += s;
			float p = s*s;
			var += p;
			p *= s;
			skew += p;
		}
		
		var = (var - ep*ep/n)/(n-1);
		float sdev = (float)Math.sqrt(var);
		
		if (var == 0)
			throw new Exception("No skew when variance = 0.");
		
		skew /= n*var*sdev;
		
		return skew;
	}
	
	
	/**
	*  Returns the kurtosis of an array of sample data.
	*  Kurtosis measures the relative peakedness or flatness of a distribution
	*  relative to a normal distribution.
	*
	*  @param  arr  An array of sample data values.
	*  @return The kurtosis of the sample data.
	**/
	public static final double kurtosis(double[] arr) throws Exception {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		double ave = mean(arr);
		
		double var = 0;
		double curt = 0;
		double ep = 0;
		for (int i=0; i < n; ++i) {
			double s = arr[i] - ave;
			ep += s;
			double p = s*s;
			var += p;
			curt = p*p;
		}
		
		var = (var - ep*ep/n)/(n-1);
		
		if (var == 0)
			throw new Exception("No kurtosis when the variance = 0.");
		
		curt = curt/(n*var*var) - 3.;
		
		return curt;
	}
	
	/**
	*  Returns the kurtosis of an array of sample data.
	*  Kurtosis measures the relative peakedness or flatness of a distribution
	*  relative to a normal distribution.
	*
	*  @param  arr  An array of sample data values.
	*  @return The kurtosis of the sample data.
	**/
	public static final float kurtosis(float[] arr) throws Exception {
		int n = arr.length;
		if (n < 2)
			throw new IllegalArgumentException("Must be at least 2 elements in array.");
		
		//	1st get the average of the data.
		float ave = mean(arr);
		
		float var = 0;
		float curt = 0;
		float ep = 0;
		for (int i=0; i < n; ++i) {
			float s = arr[i] - ave;
			ep += s;
			float p = s*s;
			var += p;
			curt = p*p;
		}
		
		var = (var - ep*ep/n)/(n-1);
		
		if (var == 0)
			throw new Exception("No kurtosis when the variance = 0.");
		
		curt = curt/(n*var*var) - 3;
		
		return curt;
	}
	

	/**
	*  Used to test out the methods in this class.
	**/
	public static void main(String args[]) {
	
		double[] arr = {1, 23, 2, 87, 56, 33, 10, 9, 25, 89, 99, 26, 43, 48, 55, 77, 15, 19 };
		
		try {
		
		System.out.println();
		System.out.println("Testing Statistics...");
		
		System.out.println("  min(arr) = " + min(arr) + ", should be 1.");
		System.out.println("  max(arr) = " + max(arr) + ", should be 99.");
		System.out.println("  range(arr) = " + range(arr) + ", should be 98.");
		System.out.println("  sum(arr) = " + sum(arr) + ", should be 717.");
		System.out.println("  mean(arr) = " + mean(arr) + ", should be 39.8333...");
		System.out.println("  percentile(arr, 0.95) = " + percentile(arr, 0.95) + ", should be 90.5");
		System.out.println("  median(arr) = " + median(arr) + ", should be 29.5.");
		System.out.println("  rms(arr) = " + rms(arr) + ", should be 50.1248...");
		System.out.println("  variance(arr) = " + variance(arr) + ", should be 980.2647...");
		System.out.println("  sdev(arr) = " + sdev(arr) + ", should be 31.309...");
		System.out.println("  skew(arr) = " + skew(arr) + ", should be 0.51879...");
		System.out.println("  kurtosis(arr) = " + kurtosis(arr) + ", should be -2.29148...");
		
		
		} catch (Exception e)  {
			e.printStackTrace();
		}
		
	}


}



