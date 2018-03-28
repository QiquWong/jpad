/*
 *   IsentropicMeEvaluator -- Used by root finder to solve for Mach number in Isentropic.Me().
 *   
 *   Copyright (C) 2000-2014 by Joseph A. Huwaldt.
 *   All rights reserved.
 *   
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
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
package standaloneutils.aerotools.aero;

import standaloneutils.mathtools.AbstractEvaluatable1D;

/**
 * Used by Isentropic.Me() and a root solver to iteratively determine the local Mach
 * number at the entrance or exit of a duct with a sonic condition at the throat. Contains
 * a function that returns the difference between the A_Astar corresponding to the current
 * guess at Mach number and the A_Astar requested. When the Mach number is found, this
 * difference will be zero.
 * 
 * <p> Written by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: June 21, 2000
 * @version April 1, 2014
 */
public class IsentropicMeEvaluator extends AbstractEvaluatable1D {

    /**
     * The specific heat ratio for the gas the shock wave is in. Use 1.4 for air.
     */
    double gamma = 1.4;

    /**
     * Function that computes the difference between the area ratio that corresponds with
     * the supplied guess at Mach number with the target area ratio (supplied using
     * setInitialValue().
     *
     * @param mach The current guess at the Mach number.
     * @return The error between the current area ratio and the target area ratio.
     */
    @Override
    public double function(double mach) {
        double value = Isentropic.A_Astar(mach, gamma) - getInitialValue();
        return value;
    }
}
