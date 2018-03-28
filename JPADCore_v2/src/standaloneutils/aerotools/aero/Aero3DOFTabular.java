/*
 *   Aero3DOFTabular -- Tabulated 3-DOF Static aerodynamics model.
 *   
 *   Copyright (C) 2005-2014 by Joseph A. Huwaldt
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

import standaloneutils.aerotools.tools.tables.FloatTable;


/**
 * A 3DOF aerodynamic model that makes use of data tabulated as a function of angle of
 * attack and Mach number. No ground effect.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: October 2, 2005
 * @version April 2, 2014
 */
public class Aero3DOFTabular implements Aero3DOF {

    //  The tabulated data.
    private FloatTable CL = null;
    private FloatTable CD = null;
    private FloatTable CM = null;
    
    //  The independent index for AOA.
    private int AOAi = 0;
    
    //  the independent index for Mach.
    private int Machi = 1;
    
    //  Storage space for independent variables.
    private float[] independents = new float[2];
    
    //  Used to transform from wind to body axes.
    private static final double D2R = PI / 180.;
    
    /**
     * The cosine of angle of attack.
     */
    protected float cosA = 1;
    
    /**
     * The sine of angle of attack.
     */
    private float sinA = 0;

    /**
     * Construct a tabular 3-DOF aerodynamics model.
     *
     * @param liftTable The wind axis lift coefficient tabulated as a function of AOA and Mach.
     * @param dragTable The wind axis drag coefficient tabulated as a function of AOA and Mach.
     * @param pmTable The wind axis pitching moment coef. tabulated as a fn. of AOA and Mach.
     * @param AOAName The name of the independent variable that represents AOA in degrees.
     * @param MachName The name of the independent variable that represents Mach number.
     */
    public Aero3DOFTabular(FloatTable liftTable, FloatTable dragTable,
            FloatTable pmTable, String AOAName, String MachName) throws IllegalArgumentException {

        if (liftTable.dimensions() != 2 || dragTable.dimensions() != 2 || pmTable.dimensions() != 2)
            throw new IllegalArgumentException("Aerodynamic coefficient tables must have 2 independent variables!");

        this.CL = liftTable;
        this.CD = dragTable;
        this.CM = pmTable;

        //  Determine which independent is AOA and which is Mach number.
        String[] names = liftTable.getIndepNames();
        if (names[0].equals(AOAName)) {
            AOAi = 0;
            Machi = 1;

        } else if (names[0].equals(MachName)) {
            Machi = 0;
            AOAi = 1;

        } else
            throw new IllegalArgumentException("Supplied independent parameter names not found in tables!");

        //  Check to make sure other tables have same independents.
        names = dragTable.getIndepNames();
        if (!names[AOAi].equals(AOAName) || !names[Machi].equals(MachName))
            throw new IllegalArgumentException("Drag table's independent variables are not the same as the lift table's.");

        names = pmTable.getIndepNames();
        if (!names[AOAi].equals(AOAName) || !names[Machi].equals(MachName))
            throw new IllegalArgumentException("PM table's independent variables are not the same as the lift table's.");
    }

    /**
     * Method that sets the angle of attack in degrees. The angle of attack independent
     * variable in the table must be defined in degrees.
     *
     * @param AOA Angle of attack in degrees.
     */
    @Override
    public void setAOA(float AOA) {
        independents[AOAi] = AOA;
        AOA *= D2R;
        cosA = (float) cos(AOA);
        sinA = (float) sin(AOA);
    }

    /**
     * Returns the current angle of attack value.
     *
     * @return Angle of attack in degrees.
     */
    public float getAOA() {
        return independents[AOAi];
    }

    /**
     * Returns the minimum AOA breakpoint in the table in degrees.
     */
    public float getMinAOA() {
        return CL.getBreakpoint(AOAi, 0);
    }

    /**
     * Returns the maximum AOA breakpoint in the table in degrees.
     */
    public float getMaxAOA() {
        int size = CL.getNumBreakpoints(AOAi);
        return CL.getBreakpoint(AOAi, size - 1);
    }

    /**
     * Method that sets the Mach number.
     *
     * @param Mach The Mach number.
     */
    @Override
    public void setMach(float Mach) {
        independents[Machi] = Mach;
    }

    /**
     * Returns the current Mach number value.
     *
     * @return Mach number.
     */
    public float getMach() {
        return independents[Machi];
    }

    /**
     * Returns the minimum Mach breakpoint in the table.
     */
    public float getMinMach() {
        return CL.getBreakpoint(Machi, 0);
    }

    /**
     * Returns the maximum Mach breakpoint in the table.
     */
    public float getMaxMach() {
        int size = CL.getNumBreakpoints(Machi);
        return CL.getBreakpoint(Machi, size - 1);
    }

    /**
     * Sets the height above ground level (for ground effect modeling). This method is
     * ignored by this implementation which does not account for ground effect.
     *
     * @param hob The height to the aero reference point divided by the reference span
     * "bref".
     */
    @Override
    public void setHOB(float hob) {
    }

    /**
     * Returns the wind axis lift coefficient. WARNING: This method will extrapolate
     * beyond the edges of the table if AOA or Mach exceed table limits!
     */
    @Override
    public float getCL() {
        return CL.lookup(independents);
    }

    /**
     * Method that returns the wind axis drag coefficient. WARNING: This method will
     * extrapolate beyond the edges of the table if AOA or Mach exceed table limits!
     */
    @Override
    public float getCD() {
        return CD.lookup(independents);
    }

    /**
     * Method that returns the wind axis pitching moment coefficient. WARNING: This method
     * will extrapolate beyond the edges of the table if AOA or Mach exceed table limits!
     */
    @Override
    public float getCM() {
        return CM.lookup(independents);
    }

    /**
     * Returns the body axis normal force coefficient.
     */
    @Override
    public float getCN() {
        return getCL() * cosA + getCD() * sinA;
    }

    /**
     * Method that returns the body axis axial force coefficient.
     */
    @Override
    public float getCA() {
        return getCD() * cosA - getCL() * sinA;
    }

    /**
     * Method that returns the body axis pitching moment coefficient. (Which should be
     * identical to the wind-axis pitching moment for a 3-DOF model with no sideslip).
     */
    @Override
    public float getCMb() {
        return getCM();
    }

    /**
     * Return a string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(CL.toString());
        buf.append(", ");
        buf.append(CD.toString());
        buf.append(", ");
        buf.append(CM.toString());
        return buf.toString();
    }
}
