/*
*   StoppingCondition  -- Specifies stopping conditions for ODE Integrators.
*
*   Copyright (C) 2004 by Joseph A. Huwaldt.
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
*  Interface for objects that can tell an ODE integrator when
*  it should stop integrating.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 27, 2004
*  @version October 27, 2004
**/
public interface StoppingCondition {

	/**
	*  Return true if the current conditions passed to this
	*  method indicate that the integration should stop.
	*  Return false otherwise.
	*
	*  @param  x    The current independent variable value.
	*  @param  y    The array of outputs of the ODE at x.
	*  @param  step The current step count (step >= 1).
	**/
	public boolean stop(double x, double[] y, int step);

}

