/*
*   TaylorSeries  -- Represents a Taylor series expansion of an arbitrary function.
*
*   Copyright (C) 2000-2004 by Joseph A. Huwaldt
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


/**
*  <p>  This class represents a Taylor series expansion of an arbitrary
*       function f(x) centered at x0.  The nth (degree) partial sum is
*       computed by the "evaluate" method.
*  </p>
*  <code>
*       Partial Sum:  P(x) = A0 + A1*(x - x0) + A2*(x - x0)^2 + ...
*                            + Ak*(x - x0)^k + ... + An*(x - x0)^n
*       Each coefficient:  Ak = df(x0)^k/dx^k/k! = deriv[k]/k!
*  </code>
*
*  <p>  This program was ported from FORTRAN to Java
*       by Joseph A. Huwaldt on October 11, 2000
*  </p>
*
*  <p>  The following note was attached to the original FORTRAN code.
*       NUMERICAL METHODS: FORTRAN Programs, (c) John H. Mathews 1994.
*       NUMERICAL METHODS for Mathematics, Science and Engineering, 2nd Ed, 1992.
*       Prentice Hall, Englewood Cliffs, New Jersey, 07632, USA.
*       Algorithm 4.1 (Evaluation of a Taylor Series).
*       Section 4.1, Taylor Series and Calculation of Functions, Page 203.
*       This free software is complements of the author.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 11, 2000
*  @version October 13, 2000
**/
public class TaylorSeries implements Cloneable, java.io.Serializable {

	/**
	*  Tolerance used for evaluating the Taylor series.
	**/
	private double tol=0.00000001;
	
	/**
	*  The center of the series.
	**/
	private double x0;
	
	/**
	*  The degree + 1 derivatives of f(x) evaluated at 
	*  the center value, x0.
	**/
	private double[] deriv;
	
	/**
	*  The value of the last term evaluated in the series.
	**/
	private transient double lastTerm = Double.MAX_VALUE;
	
	/**
	*  The number of terms evaluated.
	**/
	private transient int numTerms;
	
	
	/**
	*  Create a Taylor Series for an arbitrary function 
	*  with the given center and function derivatives
	*  about that center.
	*
	*  @param  x0    The center of the series (the point about which
	*                the function derivatives are calculated).
	*  @param  deriv An array containing the degree + 1 derivatives of the
	*                function evaluated about x0 (i.e.:  f(x), df(x)/dx,
	*                df(x)^2/dx^2, ..., df(x)^N/dx^N evaluated at x0).
	**/
	public TaylorSeries(double x0, double[] deriv) {
		this.x0 = x0;
		this.deriv = deriv;
	}
	
	/**
	*  Create a Taylor Series expansion for the trigonometric
	*  function "sine".
	*
	*  @param  degree  The degree (highest exponent or number of terms
	*                  minus 1) of the Taylor series.
	*  @return A Taylor series for the sine function.
	**/
	public static TaylorSeries createSinSeries(int degree) {
	
		//	Create the list of derivatives of sine.
		double[] deriv = new double[degree+1];
		int der=0;
		for (int k=0; k <= degree; ++k) {
		
			if (der > 3)	der = 0;
			switch(der) {
				case 0:
				case 2:
					deriv[k] = 0;
					break;
				
				case 1:
					deriv[k] = 1;
					break;
				
				case 3:
					deriv[k] = -1;
					break;
			}
			++der;
		}
		
		return new TaylorSeries(0, deriv);
	}
	
	/**
	*  Create a Taylor Series expansion for the trigonometric
	*  function "cosine".
	*
	*  @param  degree  The degree (highest exponent or number of terms
	*                  minus 1) of the Taylor series.
	*  @return A Taylor series for the cosine function.
	**/
	public static TaylorSeries createCosSeries(int degree) {
	
		//	Create the list of derivatives of cosine.
		double[] deriv = new double[degree+1];
		int der=0;
		for (int k=0; k <= degree; ++k) {
		
			if (der > 3)	der = 0;
			switch(der) {
				case 0:
					deriv[k] = 1;
					break;
				
				case 1:
				case 3:
					deriv[k] = 0;
					break;
				
				case 2:
					deriv[k] = -1;
					break;
			}
			++der;
		}
		
		return new TaylorSeries(0, deriv);
	}
	
	/**
	*  Create a Taylor Series expansion for the exponential
	*  function "exp" (i.e.:  e^x or 2.718...^x).
	*
	*  @param  degree  The degree (highest exponent or number of terms
	*                  minus 1) of the Taylor series.
	*  @return A Taylor series for the natural exponent function.
	**/
	public static TaylorSeries createExpSeries(int degree) {
	
		//	Create the list of derivatives of e^x.
		double[] deriv = new double[degree+1];
		for (int k=0; k <= degree; ++k) {
			deriv[k] = 1;
		}
		
		return new TaylorSeries(0, deriv);
	}
	
	
	/**
	*  Evaluate this Taylor series for the supplied value.
	**/
	public double evaluate(double x) {
		return ts_evaluate(x0, deriv, x, tol);
	}
	
	/**
	*  Returns true if the last series evaluation converged.
	**/
	public boolean converged() {
		if (lastTerm <= tol && numTerms < deriv.length)
			return true;
		return false;
	}
	
	/**
	*  Return the last term in the series that was last evaluated.
	*  This can be used to determine how well the series has
	*  converged.
	**/
	public double lastTerm() {
		return lastTerm;
	}
	
	/**
	*  Set the tolerance used to determine if the Taylor series
	*  has converged during evaluation.
	**/
	public void setTolerance(double value) {
		tol = value;
	}
	
	/**
	*  Return the tolerance value used to determine if the series
	*  has converged during evaluation.
	**/
	public double getTolerance() {
		return tol;
	}
	
	/**
	*  Returns a string representation of this Taylor series.
	**/
	public String toString() {
		StringBuffer buffer = new StringBuffer("P(x) = ");
		
		//	Add 1st term.
		buffer.append(deriv[0]);
		
		//	Add remaining terms.
		int length = deriv.length;
		for (int k=1; k < length; ++k) {
			if (deriv[k] != 0.) {
				buffer.append(" + ");
				buffer.append(deriv[k]);
				if (x0 != 0.) {
					buffer.append("*(x - ");
					buffer.append(x0);
					buffer.append(")^");
				} else
					buffer.append("*x^");
				
				buffer.append(k);
				buffer.append("/");
				buffer.append(k);
				buffer.append("!");
			}
		}
		
		return buffer.toString();
	}
	
	/**
	*  Make a copy of this Taylor series object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		TaylorSeries newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (TaylorSeries) super.clone();

			// Copy over the array of derivatives.
			int size = deriv.length;
			double[] newArr = new double[size];
			System.arraycopy(deriv, 0, newArr, 0, size);
			newObject.deriv = newArr;

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}


	/**
	*  <p>  Method that actually does the evaluation of a Taylor series.  </p>
	*
	*  <p>  The following note was attached to the original FORTRAN code:
	*       NUMERICAL METHODS: FORTRAN Programs, (c) John H. Mathews 1994.
	*       NUMERICAL METHODS for Mathematics, Science and Engineering, 2nd Ed, 1992.
	*       Prentice Hall, Englewood Cliffs, New Jersey, 07632, USA.
	*       This free software is complements of the author.
	*       Algorithm 4.1 (Evaluation of a Taylor Series).
	*       Section 4.1, Taylor Series and Calculation of Functions, Page 203.
	*  </p>
	*
	*  @param  x0    The center of the series (the point about which
	*                the function derivatives are calculated).
	*  @param  deriv The Taylor series degree + 1 derivatives of the
	*                function evaluated about x0 (i.e.:  f(x), df(x)/dx,
	*                df(x)^2/dx^2, ..., df(x)^N/dx^N).
	*  @param  x     The value to evaluate the Taylor series at.
	*  @param  tol   The tolerance for the Taylor series evaluation.
	*  @return The value of the Taylor series after either converging or 
	*          reaching the maximum degree specified by the number of
	*          derivatives supplied.
	**/
	private double ts_evaluate(double x0, double[] deriv, double x, double tol) {
		int k = 0;
		double sum = deriv[0];
		double prod = 1;
		int N = deriv.length - 1;	//	The degree of the series.
		double close = 1;

		if (x == x0)	close = 0;
		while ( close >= tol && k < N ) {
			++k;
			prod *= (x - x0)/k;
			while ( deriv[k] == 0. && k < N ) {
				++k;
				prod *= (x - x0)/k;
			}
			double term = deriv[k]*prod;
			if (term != 0.) {
				close = Math.abs(term);
				sum += term;
			}
		}
		
		//	Save off convergence information.
		numTerms = k;
		lastTerm = close;
		
		return sum;
	}

	/**
	*  A simple method to test this class.
	**/
	public static void main(String[] args) {
	
		System.out.println();
		System.out.println();
		System.out.println("Testing TaylorSeries...");
		
		System.out.println("    Creating a 11 term series for sin(x)...");
		TaylorSeries series = TaylorSeries.createSinSeries(10);
		System.out.println("    " + series);
		
		System.out.println("    sin(30) = " + Math.sin(30*Math.PI/180));
		System.out.println("    series sin(30) = " + series.evaluate(30*Math.PI/180));
		System.out.println("    Series tolerance = " + series.getTolerance());
		System.out.println("    " + (series.converged() ? "converged" : "did not converge")
								+ " ==> last term = " + series.lastTerm());
		System.out.println();
		
		System.out.println("    Creating a 6 term series for cos(x)...");
		series = TaylorSeries.createCosSeries(5);
		System.out.println("    " + series);
		
		System.out.println("    cos(-30) = " + Math.cos(-30*Math.PI/180));
		System.out.println("    series cos(-30) = " + series.evaluate(-30*Math.PI/180));
		System.out.println("    " + (series.converged() ? "converged" : "did not converge")
								+ " ==> last term = " + series.lastTerm());
		System.out.println();
		
		System.out.println("    Creating a 21 term series for exp(x)...");
		series = TaylorSeries.createExpSeries(20);
		System.out.println("    " + series);
		
		System.out.println("    exp(2.53) = " + Math.exp(2.53));
		System.out.println("    series exp(2.53) = " + series.evaluate(2.53));
		System.out.println("    " + (series.converged() ? "converged" : "did not converge")
								+ " ==> last term = " + series.lastTerm());
		System.out.println();
		
		
		System.out.println("Done!");
	}
}
