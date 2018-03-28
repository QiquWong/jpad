/*
*   ScalarFunctionND  -- Defines an n-Dimensional sclar function y=fn(x[1..n]).
*
*   Copyright (C) 2010, by Joseph A. Huwaldt.
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
*  A class that defines an n-Dimensional scalar function y = fn(x[1..n]) (named
*  "function") that can be called by math tools such as a function minimizer.
*  Also defines the derivatives of the function in each dimension:
*  (dy/dx)[1..n] = d fn(x[1..n])/dx (oddly enough, named "derivative").
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  July 9, 2010
*  @version  July 9, 2010
**/
public interface ScalarFunctionND {
	
	/**
	*  User supplied method that calculates the function y = fn(x[1..n]).
	*
	*  @param  x  Independent parameters to the function, passed in as input.
	*  @return The scalar function value for the provided list of inputs.
	**/
	public double function(double x[]) throws RootException;
	
	/**
	*  User supplied method that calculates the derivatives of the function:
	*  (dy/dx)[1..n] = d( fn(x[1..n]))/dx.
	*
	*  @param x    Independent parameters to the function, passed in as input.
	*  @param dydx Existing array that is filled in with the derivatives of the function
	*              with respect to each variable x[i].
	*  @return <code>true</code> if the derivatives were computed by this method,
	*          <code>false</code> if they were not.
	**/
	public boolean derivatives(double[] x, double[] dydx);
	
}



