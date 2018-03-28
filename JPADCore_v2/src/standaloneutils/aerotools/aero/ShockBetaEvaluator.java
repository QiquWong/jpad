/*
 *   ShockBetaEvaluator -- Used by root finder to solve for oblique shock angle in ShockMethods.os_beta().
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
 * Used by ShockMethods.os_beta() and a root solver to iteratively determine beta.
 * Contains a function that returns the difference between the theta corresponding to the
 * current guess at beta and the theta requested. When beta is found, this difference will
 * be zero.
 * 
 * <p> Written by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: June 21, 2000
 * @version April 1, 2014
 */
public class ShockBetaEvaluator extends AbstractEvaluatable1D {

    /**
     * The free-stream Mach number forward of the oblique shock that we are trying to find
     * the angle for.
     */
    double M;
    
    /**
     * The specific heat ratio for the gas the shock wave is in. Use 1.4 for air.
     */
    double gamma = 1.4;

    /**
     * Function that computes the difference between the incline angle that corresponds
     * with the supplied guess at shock angle with the taret incline angle (supplied using
     * setInitialValue().
     *
     * @param beta The current guess at the shock angle.
     * @return The error between the current incline angle and the target incline angle.
     */
    @Override
    public double function(double beta) {
        double value = ShockMethods.os_theta(M, beta, gamma) - getInitialValue();
        return value;
    }
}
