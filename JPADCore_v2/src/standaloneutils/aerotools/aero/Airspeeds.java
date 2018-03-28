/*
 *   Airspeeds -- Utility class containing airspeed calculation methods.
 *   
 *   Copyright (C) 2006-2014 by Joseph A. Huwaldt
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

import static java.lang.Math.*;

/**
 * A collection of static routines for calculating various airspeeds.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: May 28, 2006
 * @version April 2, 2014
 */
public final class Airspeeds {

    /**
     * Calculates the ratio of the speed of sound to the standard sea level speed of sound
     * as a function of the temperature ratio.
     *
     * @param theta Temperature ratio with altitude (T/T0, where T0 = sea level standard
     * temp).
     */
    public static double a_a0_fn_theta(double theta) {
        double a_a0 = sqrt(theta);
        return a_a0;
    }

    /**
     * Calculates the speed of sound as a function of the true airspeed and Mach number.
     *
     * @param VT True airspeed.
     * @param Mach Mach number
     */
    public static double a_fn_VT_M(double VT, double Mach) {
        double a = VT / Mach;
        return a;
    }

    /**
     * Calculates the Mach number as a function of the true airspeed and local speed of
     * sound.
     *
     * @param VT True airspeed (in same units as "a").
     * @param a Local speed of sound (in same units as VT).
     */
    public static double M_fn_VT_a(double VT, double a) {
        double Mach = VT / a;
        return Mach;
    }

    /**
     * Method that calculates Mach number as a function of pressure ratio, and calibrated
     * airspeed.
     *
     * @param delta Pressure ratio with altitude (P/P0).
     * @param VC_a0 Ratio of calibrated airspeed to standard sea level speed of sound.
     * @param gam The specific heat ratio of the gas. For air, the value is 1.4.
     */
    public static double M_fn_delta_VC(double delta, double VC_a0, double gam) {
        double gratio = gam / (gam - 1);
        double gm1 = gam - 1;
        double M = sqrt(2 / gm1 * (pow(1 / delta * (pow(1 + gm1 / 2 * VC_a0 * VC_a0, gratio) - 1) + 1, gratio) - 1));
        return M;
    }

    /**
     * Method that calculates calibrated airspeed divided by sea level standard day speed
     * of sound. Multiply the output of this function by the sea level standard speed of
     * sound to get calibrated airspeed.
     *
     * @param delta Pressure ratio with altitude (P/P0).
     * @param Mach Mach number.
     * @param gam The specific heat ratio of the gas. For air, the value is 1.4.
     */
    public static double VC_a0_fn_delta_M(double delta, double Mach, double gam) {
        double gratio = gam / (gam - 1);
        double gm1 = gam - 1;
        double VC_a0 = sqrt(2 / gm1 * (pow((delta * (pow((1 + gm1 / 2 * Mach * Mach), gratio) - 1) + 1), 1 / gratio) - 1));
        return VC_a0;
    }
}
