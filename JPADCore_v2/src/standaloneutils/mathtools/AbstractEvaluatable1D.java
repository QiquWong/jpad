/*
*   AbstractEvaluatable1D  -- A partial implementation of a 1D function y=fn(x) and it's slope dy/dx=d_fn(x)/dx.
*
*   Copyright (C) 1999 - 2012 by Joseph A. Huwaldt.
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
*  A class that defines a 1D function y = fn(x) (named
*  "function") that can be called by math tools such as root
*  finders.  Also defines the derivative of the function.
*  dy/dx = d fn(x)/dx (oddly enough, named "derivative").
*  An optional capability of setting an initial function value
*  is also provided.  This aides the calculating of some
*  functions and is a common enough problem that I've included
*  it in the basic Evaluatable1D class definition.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  October 7, 1998
*  @version  September 16, 2012
**/
public abstract class AbstractEvaluatable1D implements Evaluatable1D {
	private	double value0;
	
	
	/**
	*  Calculates the derivative of the function dy/dx = d fn(x)/dx.
	*  Classes wanting to return the derivative of the function at x
	*  should override this function.
	*  If the method using this class doesn't require the
	*  derivative, do nothing and Double.NaN is returned.
	*
	*  @param   x  Independent parameter to the function.
	*  @return  The function value at x or Double.NaN if the derivative is not defined.
	**/
    @Override
	public double derivative(double x) throws RootException {
		return Double.NaN;
	}
	
	
	/**
	*  Used to set an initial function value (or any other initial
	*  value the user wants to use).  This function is provided as
	*  an optional service.  The programmer may use this to aide in
	*  solving problems where you want to find the point where
	*  f(x) - f0 = 0.  Use this function to set f0 and use
	*  "getInitialValue()" to retrieve f0 and calculate f(x) - f0 in
	*  your "function()" definition.	</p>
	*
	*  @param  initialValue  Initial function value, f0.
	**/
	public void setInitialValue(double initialValue) {
		value0 = initialValue;
	}
	

	/**
	*  Returns the initial function value set with "setInitialValue()".
	*
	*  @return  The initial value set with "setInitialValue()".
	**/
	public final double getInitialValue() {
		return value0;
	}
	
}



