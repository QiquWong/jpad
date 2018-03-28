/*
*   VectorFunction  -- Defines an n-Dimensional vector function y[]=fn(x[]).
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
*  A class that defines an n-Dimensional vector function y[] = fn(x[]) (named
*  "function") that can be called by math tools such as root
*  finders.  Also defines the derivative of the function.
*  dy[]/dx = d fn(x[])/dx (oddly enough, named "derivative").
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  July 4, 2010
*  @version  July 8, 2010
**/
public interface VectorFunction {
	
	/**
	*  User supplied method that calculates the function y[] = fn(x[]).
	*
	*  @param   n  The number of variables in the x & y arrays.
	*  @param   x  Independent parameters to the function, passed in as input.
	*  @param   y  An existing array that is filled in with the outputs of the function
	**/
	public void function(int n, double x[], double[] y) throws RootException;
	
	/**
	*  User supplied method that calculates the Jacobian of the function.
	*
	*  @param n   The number of rows and columns in the Jacobian.
	*  @param x   Independent parameters to the function, passed in as input.
	*  @param jac The Jacobian array.
	*  @return <code>true</code> if the Jacobian was computed by this method,
	*          <code>false</code> if it was not.
	**/
	public boolean jacobian(int n, double[] x, double[][] jac);
	
}



