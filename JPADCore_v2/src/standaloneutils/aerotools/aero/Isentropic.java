/*
 *   Isentropic -- Utility methods for calculating isentropic flow properties of a gas.
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

import static java.lang.Math.*;

import standaloneutils.mathtools.RootException;
import standaloneutils.mathtools.Roots;


/**
 * A set of utility methods for calculating isentropic flow properties in a gas. All the
 * methods in this class assume that the flow processes are isentropic (adiabatic and
 * reversible). Includes methods for calculating isentropic stagnation properties.
 * 
 * <p> Written by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: October 23, 2000
 * @version April 1, 2014
 */
public final class Isentropic {

    /**
     * Constant indicating that a subsonic solution should be returned in Me().
     */
    public static final boolean SUBSONIC = false;
    
    /**
     * Constant indicating that a supersonic solution should be returned in Me().
     */
    public static final boolean SUPERSONIC = true;
    
    /**
     * The maximum Mach number used in Me().
     */
    private static final double MAXMACH = 100.;
    
    //	Error messages.
    private static final String kInvalidAreaRatio =
            "The area ratio A_Astar must be greater than 1.";

    /**
     * Prevent anyone from instantiating this class.
     */
    private Isentropic() {
    }

    /**
     * Calculate the isentropic temperature ratio between two points in the flow given the
     * pressure ratio.
     *
     * @param P2_P1 The pressure ratio between two points in isentropic flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return T2_T1 The temperature ratio between two points in isentropic flow.
     */
    public static double T2_T1(double P2_P1, double gam) {
        return pow(P2_P1, (gam - 1) / gam);
    }

    /**
     * Calculate the isentropic pressure ratio between two points in the flow given the
     * temperature ratio.
     *
     * @param T2_T1 The temperature ratio between two points in isentropic flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return P2_P1 The pressure ratio between two points in isentropic flow.
     */
    public static double P2_P1(double T2_T1, double gam) {
        return pow(T2_T1, gam / (gam - 1));
    }

    /**
     * Calculate the isentropic density ratio between two points in the flow given the
     * temperature ratio.
     *
     * @param T2_T1 The temperature ratio between two points in isentropic flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return rho2_rho1 The density ratio between two points in isentropic flow.
     */
    public static double rho2_rho1(double T2_T1, double gam) {
        return pow(T2_T1, 1. / (gam - 1));
    }

    /**
     * Calculates the ratio of total to static temperature at a point in a flow as a
     * function of Mach number at that point.
     *
     * @param M The Mach number at a point in the flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return The ratio of total to static temperature TT/T at a point in the flow.
     */
    public static double TT_T(double M, double gam) {
        return (1 + 0.5 * (gam - 1) * M * M);
    }

    /**
     * Calculates the ratio of total to static pressure at a point in a flow as a function
     * of Mach number at that point.
     *
     * @param M The Mach number at a point in the flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return The ratio of total to static pressure PT/P at a point in the flow.
     */
    public static double PT_P(double M, double gam) {
        return pow(TT_T(M, gam), gam / (gam - 1));
    }

    /**
     * Calculates the ratio of total to static density at a point in a flow as a function
     * of Mach number at that point.
     *
     * @param M The Mach number at a point in the flow.
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return The ratio of total to static density rhoT/rho at a point in the flow.
     */
    public static double rhoT_rho(double M, double gam) {
        return pow(TT_T(M, gam), 1 / (gam - 1));
    }

    /**
     * Calculates the duct area ratio (A/A*) necessary to create sonic flow (M=1) at the
     * throat of the duct. Assumes a calorically perfect gas.
     *
     * @param M The Mach number at a point in the flow corresponding to the area "A".
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @return The area ratio (A/A*) required to create sonic flow at the throat of the
     *          duct. The throat area is "A*".
     */
    public static double A_Astar(double M, double gam) {
        double gamP1 = gam + 1;
        double term1 = 2 * TT_T(M, gam) / gamP1;
        return pow(term1, gamP1 / (gam - 1)) / (M * M);
    }

    /**
     * Calculates the Mach number at the entrance or exit of a duct (nozzle) given the
     * ratio of the local duct area at the entrance or exit to the area where the sonic
     * (M=1) condition exists. Assumes a calorically perfect gas. If calling repeatedly,
     * it is more efficient to call the version of this function where a Mach evaluator is
     * supplied on the parameter list. This prevents that object from being created on
     * every function call.
     *
     * @param A_Astar The ratio of the local duct area at the entrance or exit to the area
     *              where the sonic condition exists (A/A*).
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @param tol Tolerance to use when calculating Mach number.
     * @param type A flag that indicates if you want the subsonic (false) or supersonic
     *              (true) solution returned.
     * @return The local Mach number at the entrance or exit of the duct.
     */
    public static double Me(double A_Astar, double gam, double tol, boolean type)
            throws RootException {
        return Me(A_Astar, gam, tol, type, new IsentropicMeEvaluator());
    }

    /**
     * Calculates the Mach number at the entrance or exit of a duct (nozzle) given the
     * ratio of the local duct area at the entrance or exit to the area where the sonic
     * (M=1) condition exists. Assumes a calorically perfect gas.
     *
     * @param A_Astar The ratio of the local duct area at the entrance or exit to the area
     *              where the sonic condition exists (A/A*).
     * @param gam The specific heat ratio of the gas. The value used for air is 1.4.
     * @param tol Tolerance to use when calculating Mach number.
     * @param type A flag that indicates if you want the subsonic (false) or supersonic
     *              (true) solution returned.
     * @param machfn An instance of IsentropicMeEvaluator.
     * @return The local Mach number at the entrance or exit of the duct.
     */
    public static double Me(double A_Astar, double gam, double tol, boolean type,
            IsentropicMeEvaluator machfn) throws RootException {

        double mach = 1.0;

        if (A_Astar < 1.0)
            throw new IllegalArgumentException(kInvalidAreaRatio);

        if (A_Astar > 1.0) {

            //	Initialize the evaluatable function for our root solver.
            machfn.setInitialValue(A_Astar);
            machfn.gamma = gam;

            // Use a root solver to find Mach number.
            if (type == SUBSONIC)
                //	Find the subsonic solution.
                mach = Roots.findRoot1D(machfn, 0., 1.0, tol);
            else
                //	Find the supersonic solution.
                mach = Roots.findRoot1D(machfn, 1.0, MAXMACH, tol);
        }

        return mach;
    }

    /**
     * A simple method to test the isentropic methods in this class.
     */
    public static void main(String args[]) {

        System.out.println("\nTesting the methods in Isentropic class:");

        double gamma = 1.4;

        //	Test basic isentropic relations.
        double T2_T1 = 0.85;
        System.out.println("    For T2/T1 = " + T2_T1 + ", P2/P1 = "
                + (float) P2_P1(T2_T1, gamma) + ", and rho2/rho1 = "
                + (float) rho2_rho1(T2_T1, gamma) + ".");

        //	Test stagnation condition relations.
        double Mach = 1.3;
        System.out.println("    For Mach = " + Mach
                + ", TT/T = " + (float) TT_T(Mach, gamma)
                + ", PT/P = " + (float) PT_P(Mach, gamma)
                + ", rhoT/rho = " + (float) rhoT_rho(Mach, gamma) + ".");

        //	Test area ratio relations.
        try {
            System.out.println("    For Mach = " + Mach + ", A/A* = "
                    + (float) A_Astar(Mach, gamma) + ".");
            System.out.println("    For A/A* = 1.7, Supersonic Mach = "
                    + (float) Me(1.7, gamma, 0.00001, true)
                    + ", Subsonic Mach = "
                    + (float) Me(1.7, gamma, 0.00001, false) + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done!");
    }
}
