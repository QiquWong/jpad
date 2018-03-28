/*
*   EvaluatableNumOpt  -- Defines interface for a multivariable cost function for numerical optimization.
*
*   Copyright (C) 1999-2004 by Joseph A. Huwaldt
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
*  A class that defines a multivariable cost function named
*  "cost" that can be used by numerical optimization routines
*  to return the cost associated with a particular set of
*  design variables (x[]).   This class also optionally defines
*  a penalty function named "penalty" that can be used to
*  return the penalty associated with violating a constraint
*  in a numerical optimization problem.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  May 24, 1999
*  @version  November 19, 2004
**/
public interface EvaluatableNumOpt extends java.io.Serializable {

	/** 
	*  Method that calculates the multivariable
	*  cost function y = fn(x[]).
	*
	*  @param   x  Array (vector) of design variables to be used to calculate
	*              the cost of this design solution.
	*  @return  Return the cost of this set of design variables.
	**/
	public float cost( float[] x );

	/**
	*  Calculates the penalty associated with violating the
	*  multivariable design constraints of the problem being
	*  optimized.  If a penalty function is not required, just
	*  return 0.
	*
	*  @param   x  Array (vector) of design variables to be used to
	*              calculate the penalty associated this design solution.
	*  @return  Return the penalty associated with this set of design
	*           variables.
	**/
	public float penalty( float[] x );

}


