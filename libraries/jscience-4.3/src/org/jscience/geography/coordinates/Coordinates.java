/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import javolution.lang.Realtime;
import javolution.lang.ValueType;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.xml.XMLSerializable;

import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

/**
 * This class designates the position that a point occupies in a given
 * n-dimensional reference frame or system.
 * This implementation is compatible with OpenGIS&reg; DirectPosition. 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, April 9, 2007
 * @see <a href="http://www.opengeospatial.org">Open Geospatial Consortium, Inc.</a>  
 */
public abstract class Coordinates<R extends CoordinateReferenceSystem<?>>
    implements DirectPosition, Realtime, ValueType, XMLSerializable {

    /**
     * Default constructor.
     */
    protected Coordinates() {
    }

    /**
     * Returns the reference system for this coordinates.
     * 
     * @return the associated coordinate reference system.
     */
    public abstract R getCoordinateReferenceSystem();

    /////////////
    // OpenGIS //
    /////////////

    /**
     * OpenGIS&reg; - The length of coordinate sequence (the number of entries). 
     * This is determined by the {@linkplain #getCoordinateReferenceSystem() 
     * coordinate reference system}.
     *
     * @return the dimensionality of this position.
     */
    public abstract int getDimension();

    /**
     * OpenGIS&reg; - Returns the ordinate at the specified dimension.
     *
     * @param  dimension The dimension in the range 0 to 
     *         {@linkplain #getDimension dimension}-1.
     * @return The coordinate at the specified dimension.
     * @throws IndexOutOfBoundsException if the specified dimension is out
     *         of bounds.
     */
    public abstract double getOrdinate(int dimension)
            throws IndexOutOfBoundsException;

    /**
     * OpenGIS&reg; - Throws <code>UnsupportedOperationException</code> as 
     * <b>J</b>Science coordinates are immutable.
     */
    public final void setOrdinate(int dimension, double value)
            throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Immutable coordinates");
    }

    /**
     * OpenGIS&reg; - Returns the sequence of numbers that hold the coordinate 
     * of this position in its reference system.
     * 
     * @return a copy of the coordinates. Changes in the returned array will 
     *         not be reflected back in this {@code DirectPosition} object.
     */
    public final double[] getCoordinates() {
        double[] coordinates = new double[getDimension()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = getOrdinate(i);
        }
        return coordinates;
    }

    /**
     * OpenGIS&reg; - Returns the direct position for this position.
     * 
     * @return <code>this</code>
     */
    public final DirectPosition getPosition() {
        return this;
    }

    /**
     * OpenGIS&reg; - Makes an exact copy of this coordinate.
     * 
     * @return the copy.
     */
    public final Coordinates<R> clone() {
        return this.clone();
    }

    /**
     * Returns the string representation of this coordinates.
     * 
     * @return the coordinates values/units.
     */
    public Text toText() {
        double[] coordinates = getCoordinates();
        CoordinateSystem cs = this.getCoordinateReferenceSystem().getCoordinateSystem();
        TextBuilder tb = TextBuilder.newInstance();
        tb.append('[');
        for (int i=0; i < coordinates.length; i++) {
            if (i != 0) {
                tb.append(", ");
            }
            tb.append(getOrdinate(i));
            tb.append(' ');
            tb.append(cs.getAxis(i).getUnit());
        }
        tb.append(']');
        return tb.toText();
    }

    /**
     * Returns the text representation of these coordinates as a 
     * <code>java.lang.String</code>.
     * 
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

    /**
     * Returns a copy of these coordinates 
     * {@link javolution.context.AllocatorContext allocated} 
     * by the calling thread (possibly on the stack).
     *     
     * @return an identical and independant copy of these coordinates .
     */
    public abstract Coordinates<?> copy();
    
}