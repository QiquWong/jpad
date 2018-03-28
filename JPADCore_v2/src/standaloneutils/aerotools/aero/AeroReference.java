/*
 *   AeroReference -- Container for aerodynamic reference quantities and their units.
 *   
 *   Copyright (C) 2002-2014 by Joseph A. Huwaldt
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

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.*;
import javax.measure.unit.Unit;


/**
 * Class that serves as a container for aerodynamic reference quantities such as reference
 * area, span, chord length, and moment reference center. Except for the units, instances
 * of this class are immutable.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 8, 1999
 * @version April 3, 2014
 */
public class AeroReference implements Cloneable, java.io.Serializable {

    /**
     * Reference area in reference units (meters).
     */
    private double Sref = 1;
    /**
     * Reference chord length in reference units (meters).
     */
    private double cref = 1;
    /**
     * Reference span length in reference units (meters).
     */
    private double bref = 1;
    /**
     * Moment reference center (MRC) in reference units (meters).
     */
    private double Xref, Yref, Zref;
    /**
     * The current area unit used by the reference area.
     */
    private Unit<Area> areaUnit;
    /**
     * The current length unit used by reference lengths and MRC.
     */
    private Unit<Length> lengthUnit;

    /**
     * Creates an AeroReference object with the given reference quantities and units.
     *
     * @param area Reference area. Normally the projected wing planform area.
     * @param areaUnits Units used by the reference area parameter.  If <code>null</code>
     *              is passed, standard SI area units are used (m^2).
     * @param length Reference chord length. Normally the wing planform mean aerodynamic
     * chord (MAC) length.
     * @param span Reference span length. Normally the tip-to-tip distance on the wing
     * planform.
     * @param Xmrc Moment reference center location X coordinate.
     * @param Ymrc Moment reference center location Y coordinate.
     * @param Zmrc Moment reference center location Z coordinate.
     * @param lengthUnits Units used by all the length and position parameters.
     *      If <code>null</code> is passed, standard SI units are used (m).
     */
    public AeroReference(double area, Unit<Area> areaUnits, double length, double span,
            double Xmrc, double Ymrc, double Zmrc, Unit<Length> lengthUnits) {
        
        if (areaUnits == null)
            areaUnits = Area.UNIT;
        areaUnit = areaUnits;
        if (lengthUnits == null)
            lengthUnits = Length.UNIT;
        lengthUnit = lengthUnits;

        //  Store the values in reference (SI) units.
        Sref = areaUnits.toStandardUnit().convert(area);

        UnitConverter toStandardUnit = lengthUnits.toStandardUnit();
        cref = toStandardUnit.convert(length);
        bref = toStandardUnit.convert(span);
        Xref = toStandardUnit.convert(Xmrc);
        Yref = toStandardUnit.convert(Ymrc);
        Zref = toStandardUnit.convert(Zmrc);

    }

    /**
     * Return the area units currently being used by this aero reference object.
     */
    public Unit<Area> getAreaUnits() {
        return areaUnit;
    }

    /**
     * Change the current area units used by this reference object. The reference area
     * will be converted from it's current value to the equivalent value in the new units.
     *
     * @param newUnit The new area units to convert reference area to.
     */
    public void changeAreaUnits(Unit<Area> newUnit) {
        if (newUnit == null)
            newUnit = Area.UNIT;
        
        // Since we store values in reference units, we do not need
        // to change any values.  Conversion occures on output.

        // Keep a reference to the new area units.
        areaUnit = newUnit;
    }

    /**
     * Return the length units currently being used by this aero reference object.
     */
    public Unit<Length> getLengthUnits() {
        return lengthUnit;
    }

    /**
     * Change the current length units used by this reference object. The reference chord,
     * span and moment reference location values will be converted from their current
     * values to the equivalent values in the new units.
     *
     * @param newUnit The new length units to convert reference lengths and moment
     *          reference position to.
     */
    public void changeLengthUnits(Unit<Length> newUnit) {
        if (newUnit == null)
            newUnit = Length.UNIT;
        
        // Since we store values in reference units, we do not need
        // to change any values.  Conversion occures on output.

        // Keep a reference to the new length units.
        lengthUnit = newUnit;
    }

    /**
     * Return the X coordinate of the moment reference center location in the current
     * length units used by this AeroReference object.
     *
     * @return X coordinate of moment reference center location.
     */
    public double getMomentRefX() {
        UnitConverter convertFromRef = Length.UNIT.getConverterTo(lengthUnit);
        double value = convertFromRef.convert(Xref);

        return value;
    }

    /**
     * Return the X coordinate of the moment reference center location. in reference
     * length units (meters).
     *
     * @return X coordinate of moment reference center location.
     */
    public double getMomentRefXRU() {
        return Xref;
    }

    /**
     * Return the Y coordinate of the moment reference center location in the current
     * length units used by this AeroReference object.
     *
     * @return Y coordinate of moment reference center location.
     */
    public double getMomentRefY() {
        UnitConverter convertFromRef = Length.UNIT.getConverterTo(lengthUnit);
        double value = convertFromRef.convert(Yref);

        return value;
    }

    /**
     * Return the Y coordinate of the moment reference center location. in reference
     * length units (meters).
     *
     * @return Y coordinate of moment reference center location.
     */
    public double getMomentRefYRU() {
        return Yref;
    }

    /**
     * Return the Z coordinate of the moment reference center location in the current
     * length units used by this AeroReference object.
     *
     * @return Z coordinate of moment reference center location.
     */
    public double getMomentRefZ() {
        UnitConverter convertFromRef = Length.UNIT.getConverterTo(lengthUnit);
        double value = convertFromRef.convert(Zref);

        return value;
    }

    /**
     * Return the Z coordinate of the moment reference center location. in reference
     * length units (meters).
     *
     * @return Z coordinate of moment reference center location.
     */
    public double getMomentRefZRU() {
        return Zref;
    }

    /**
     * Return the reference area in the current area units used by this AeroReference
     * object.
     *
     * @return The reference area in current units.
     */
    public double getRefArea() {
        UnitConverter convertFromRef = Area.UNIT.getConverterTo(areaUnit);
        double value = convertFromRef.convert(Sref);

        return value;
    }

    /**
     * Return the reference area in the reference units (sq. meters).
     *
     * @return The reference area in reference units.
     */
    public double getRefAreaRU() {
        return Sref;
    }

    /**
     * Return the reference length in the current length units used by this AeroReference
     * object.
     *
     * @return The reference length in the current length units.
     */
    public double getRefLength() {
        UnitConverter convertFromRef = Length.UNIT.getConverterTo(lengthUnit);
        double value = convertFromRef.convert(cref);

        return value;
    }

    /**
     * Return the reference length in the reference units (meters).
     *
     * @return The reference length in reference units.
     */
    public double getRefLengthRU() {
        return cref;
    }

    /**
     * Return the reference span in the current length units used by this AeroReference
     * object.
     *
     * @return The reference span in the current units.
     */
    public double getRefSpan() {
        UnitConverter convertFromRef = Length.UNIT.getConverterTo(lengthUnit);
        double value = convertFromRef.convert(bref);

        return value;
    }

    /**
     * Return the reference span in reference length units (meters).
     *
     * @return The reference span in reference units.
     */
    public double getRefSpanRU() {
        return bref;
    }

    /**
     * Returns true if the input AeroReference object has the same numerical values (same
     * reference area, same span, etc) as this one.
     *
     * @param obj The AeroReference object we are comparing this one to.
     * @return True if the given AeroReference object is equal to this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        AeroReference that = (AeroReference) obj;
        if (this.Sref != that.Sref)
            return false;
        if (this.cref != that.cref)
            return false;
        if (this.bref != that.bref)
            return false;
        if (this.Xref != that.Xref)
            return false;
        if (this.Yref != that.Yref)
            return false;

        return this.Zref == that.Zref;
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = hash * 31 + makeVarCode(Sref);
        hash = hash * 31 + makeVarCode(cref);
        hash = hash * 31 + makeVarCode(bref);
        hash = hash * 31 + makeVarCode(Xref);
        hash = hash * 31 + makeVarCode(Yref);
        hash = hash * 31 + makeVarCode(Zref);

        return hash;
    }

    private static int makeVarCode(double value) {
        long bits = Double.doubleToLongBits(value);
        int var_code = (int)(bits ^ (bits >>> 32));
        return var_code;
    }
    
    /**
     * Make a copy of this AeroReference object.
     *
     * @return A copy of this AeroReference object.
     */
    @Override
    public Object clone() {
        Object result = null;

        try {
            result = super.clone();

        } catch (Exception e) {
            // This shouldn't happen if this object extends "Cloneable".
            System.err.println("Can not clone this object!");
        }

        return result;
    }

    /**
     * Method that returns a string representations of the reference quantities contained
     * in this object.
     */
    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder("Sref = ");
        buffer.append(getRefArea());
        buffer.append(" ");
        buffer.append(areaUnit);

        buffer.append(", cref = ");
        buffer.append(getRefLength());
        buffer.append(" ");
        buffer.append(lengthUnit);

        buffer.append(", bref = ");
        buffer.append(getRefSpan());
        buffer.append(" ");
        buffer.append(lengthUnit);

        buffer.append(", Xmrc = ");
        buffer.append(getMomentRefX());
        buffer.append(" ");
        buffer.append(lengthUnit);

        buffer.append(", Ymrc = ");
        buffer.append(getMomentRefY());
        buffer.append(" ");
        buffer.append(lengthUnit);

        buffer.append(", Zmrc = ");
        buffer.append(getMomentRefZ());
        buffer.append(" ");
        buffer.append(lengthUnit);

        return buffer.toString();
    }
}
