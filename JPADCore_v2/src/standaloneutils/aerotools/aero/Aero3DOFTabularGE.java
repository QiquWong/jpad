/*
 *   Aero3DOFTabularGE -- Tabulated 3-DOF Static aerodynamics model including ground effect.
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

import standaloneutils.aerotools.tools.tables.FloatTable;

/**
 * A 3DOF aerodynamic model that makes use of data tabulated as a function of angle of
 * attack and Mach number. Includes a ground effect (GE) model that consists of increments
 * in the aero coefficients due to ground effect as a function of angle of attack and
 * h/bref.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: November 6, 2005
 * @version April 2, 2014
 */
public class Aero3DOFTabularGE extends Aero3DOFTabular {

    //  The tabulated GE data.
    private FloatTable dCLGE = null;
    private FloatTable dCDGE = null;
    private FloatTable dCMGE = null;
    
    //  The independent index for AOA.
    private int AOAGEi = 0;
    
    //  the independent index for h/bref.
    private int hobi = 1;
    
    //  Storage space for independent variables.
    private float[] independents = new float[2];

    /**
     * Construct a tabular 3-DOF aerodynamics model with an increment based ground effect
     * model.
     *
     * @param liftTable The wind axis lift coefficient tabulated as a function of AOA and Mach.
     * @param dragTable The wind axis drag coefficient tabulated as a function of AOA and Mach.
     * @param pmTable The wind axis pitching moment coef. tabulated as a fn. of AOA and Mach.
     * @param AOAName The name of the independent variable that represents AOA.
     * @param MachName The name of the independent variable that represents Mach number.
     * @param dCLGE The wind axis incr. in lift coef. due to ground effect as a function
     *              of alpha & h/bref.
     * @param dCDGE The wind axis incr. in drag coef. due to ground effect as a function
     *              of alpha & h/bref.
     * @param dCMGE The wind axis incr. in pitching moment due to ground effect as a
     *              function of alpha & h/bref.
     * @param AOAGEName The name of the independent variable that represents AOA in the
     *              ground effect tables.
     * @param hobName The name of the independent variable that represents h/bref.
     */
    public Aero3DOFTabularGE(FloatTable liftTable, FloatTable dragTable, FloatTable pmTable, String AOAName, String MachName,
            FloatTable dCLGE, FloatTable dCDGE, FloatTable dCMGE, String AOAGEName, String hobName) throws IllegalArgumentException {

        super(liftTable, dragTable, pmTable, AOAName, MachName);

        if (dCLGE.dimensions() != 2 || dCDGE.dimensions() != 2 || dCMGE.dimensions() != 2)
            throw new IllegalArgumentException("Ground effect increment tables must have 2 independent variable only!");

        this.dCLGE = dCLGE;
        this.dCDGE = dCDGE;
        this.dCMGE = dCMGE;

        //  Determine which independent is AOA and which is Mach number.
        String[] names = dCLGE.getIndepNames();
        if (names[0].equals(AOAGEName)) {
            AOAGEi = 0;
            hobi = 1;

        } else if (names[0].equals(hobName)) {
            hobi = 0;
            AOAGEi = 1;

        } else
            throw new IllegalArgumentException("Supplied independent parameter names not found in GE tables!");

        independents[hobi] = 2;
        independents[AOAGEi] = 0;

        //  Check to make sure other tables have same independents.
        names = dCDGE.getIndepNames();
        if (!names[AOAGEi].equals(AOAGEName) || !names[hobi].equals(hobName))
            throw new IllegalArgumentException("Drag GE incr. table's independent variables are not the same as the lift incr. table's.");

        names = dCMGE.getIndepNames();
        if (!names[AOAGEi].equals(AOAGEName) || !names[hobi].equals(hobName))
            throw new IllegalArgumentException("PM GE incr. table's independent variables are not the same as the lift incr. table's.");

    }

    /**
     * Sets the height above ground level (for ground effect modeling).
     *
     * @param hob The height to the aero reference point divided by the reference span
     * "bref" (h/bref).
     */
    @Override
    public void setHOB(float hob) {
        independents[hobi] = hob;
    }

    /**
     * Method that sets the angle of attack. Use units consistent with how the tabular
     * data is defined. Typically in degrees.
     *
     * @param AOA Angle of attack.
     */
    @Override
    public void setAOA(float AOA) {
        super.setAOA(AOA);
        independents[AOAGEi] = AOA;
    }

    /**
     * Returns the wind axis lift coefficient. WARNING: This method will extrapolate
     * beyond the edges of the table if AOA, Mach or hob exceed table limits!
     */
    @Override
    public float getCL() {
        float CL = super.getCL();
        float dCL = dCLGE.lookup(independents);
        return CL + dCL;
    }

    /**
     * Method that returns the wind axis drag coefficient. WARNING: This method will
     * extrapolate beyond the edges of the table if AOA, Mach, or hob exceed table limits!
     */
    @Override
    public float getCD() {
        float CD = super.getCD();
        float dCD = dCDGE.lookup(independents);
        return CD + dCD;
    }

    /**
     * Method that returns the wind axis pitching moment coefficient. WARNING: This method
     * will extrapolate beyond the edges of the table if AOA, Mach, or hob exceed table
     * limits!
     */
    @Override
    public float getCM() {
        float CM = super.getCM();
        float dCM = dCMGE.lookup(independents);
        return CM + dCM;
    }

    /**
     * Return a string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(", ");
        buf.append(dCLGE.toString());
        buf.append(", ");
        buf.append(dCDGE.toString());
        buf.append(", ");
        buf.append(dCMGE.toString());
        return buf.toString();
    }
}
