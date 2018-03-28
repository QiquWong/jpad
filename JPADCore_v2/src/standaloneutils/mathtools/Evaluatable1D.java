/*
*   Evaluatable1D  -- Interface for a 1D function y=fn(x) and it's slope dy/dx=d_fn(x)/dx.
*
*   Copyright (C) 1999 - 2011 by Joseph A. Huwaldt.
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
*  An interface that defines a 1D function y = fn(x) (named
*  "function") that can be called by math tools such as root
*  finders.  Also defines the derivative of the function.
*  dy/dx = d fn(x)/dx (oddly enough, named "derivative").
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  October 7, 1998
*  @version  November 12, 2011
**/
public interface Evaluatable1D {
	
	/**
	*  User supplied method that calculates the function y = fn(x).
	*  Classes implementing this interface must define this function.
	*
	*  @param   x  Independent parameter to the function.
	*  @return  The function value at x.
	**/
	public double function(double x) throws RootException;
	
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
	public double derivative(double x) throws RootException;
	
}



