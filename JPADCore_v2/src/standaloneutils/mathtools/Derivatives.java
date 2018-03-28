/*
 *   Derivatives  -- Interface for objects that compute derivatives for an integrator.
 *
 *   Copyright (C) 2004-2014 by Joseph A. Huwaldt.
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
 */
package standaloneutils.mathtools;

 /**
 *  <p> The Derivatives interface provides a mechanism for passing a method
 *      that computes the derivatives of a system of equations for an integrator. </p>
 *
 *  <p> Based on "Derivatives.java", version 1.0 as found in JAT
 *      (http://jat.sourceforge.net). </p>
 *
 *  <p>  Modified by:  Joseph A. Huwaldt  </p>
 *
 *  @author <a href="mailto:dgaylor@users.sourceforge.net">Dave Gaylor
 *  @version April 3, 2014
 */
public interface Derivatives {

    /**
     * Compute the derivatives, at independent variable "x", with state variables "y[]",
     * of a set of coupled equations modeled by this class.
     *
     * @param ydot Existing array to be filled with derivatives.
     * @param x double containing time or the independent variable.
     * @param y Array containing the state of the variables.
     * @return Returns reference to the input array "ydot" containing the derivatives.
     */
    public double[] derivs(double[] ydot, double x, double[] y);
}
