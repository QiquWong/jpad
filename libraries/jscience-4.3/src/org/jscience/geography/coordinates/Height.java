/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import static javax.measure.unit.SI.METRE;

import javax.measure.quantity.Length;
import javax.measure.Measurable;
import javax.measure.unit.Unit;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.VerticalCRS;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the {@link VerticalCRS vertical} height above the 
 * WGS84 ellipsoid.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 6, 2006
 */
public final class Height extends Coordinates<VerticalCRS<?>> implements
        Measurable<Length> {

    /**
     * Holds the coordinate reference system for all instances of this class. 
     */
    public static final VerticalCRS<Height> CRS = new VerticalCRS<Height>() {

        @Override
        protected Height coordinatesOf(AbsolutePosition position) {
            if (position.heightWGS84 instanceof Height)
                return (Height) position.heightWGS84;
            return Height.valueOf(position.heightWGS84.doubleValue(METRE),
                    METRE);
        }

        @Override
        protected AbsolutePosition positionOf(Height coordinates,
                AbsolutePosition position) {
            position.heightWGS84 = coordinates;
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return VerticalCRS.HEIGHT_CS;
        }
    };

    /**
     * Holds the height in meters. 
     */
    private double _meters;

    /**
     * Returns the vertical position corresponding to the specified coordinates.
     * 
     * @param value the height above the WGS84 ellipsoid stated in the 
     *        specified unit.
     * @param unit the length unit in which the height is stated.
     * @return the corresponding vertical position.
     */
    public static Height valueOf(double value, Unit<Length> unit) {
        Height height = FACTORY.object();
        height._meters = (unit == METRE) ? value : 
            unit.getConverterTo(METRE).convert(value);
        return height;
    }
    
    private static final ObjectFactory<Height> FACTORY = new ObjectFactory<Height>() {

        @Override
        protected Height create() {
            return new Height();
        } };
    
    private Height() {
    }

    @Override
    public VerticalCRS<?> getCoordinateReferenceSystem() {
        return Height.CRS;
    }

    // OpenGIS Interface.
    public int getDimension() {
        return 1;
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            Unit<?> u = VerticalCRS.HEIGHT_CS.getAxis(0).getUnit();
            return METRE.getConverterTo(u).convert(_meters);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // Implements Scalar<Length>
    public final double doubleValue(Unit<Length> unit) {
        return unit.equals(METRE) ? _meters : METRE
                .getConverterTo(unit).convert(_meters);
    }

    // Implements Scalar<Length>
    public final long longValue(Unit<Length> unit) {
        return Math.round(doubleValue(unit));
    }

    // Implements Scalar<Length>
    public int compareTo(Measurable<Length> arg0) {
        double arg0InMeter = arg0.doubleValue(METRE);
        return (_meters > arg0InMeter) ? 1
                : (_meters < arg0InMeter) ? -1 : 0;
    }
    
    
    @Override
    public Height copy() {
        return Height.valueOf(_meters, METRE);
    }

    // Default serialization.
    //
    
    static final XMLFormat<Height> XML = new XMLFormat<Height>(Height.class) {
        
        @Override
        public Height newInstance(Class<Height> cls, InputElement xml) throws XMLStreamException {
            return FACTORY.object();
        }
        
        public void write(Height height, OutputElement xml) throws XMLStreamException {
             xml.setAttribute("meters", height._meters);
         }

         public void read(InputElement xml, Height height) throws XMLStreamException {
             height._meters = xml.getAttribute("meters", 0.0);
         }
     };

    private static final long serialVersionUID = 1L;
}