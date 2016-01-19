/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import javax.measure.quantity.Length;
import javax.measure.Measurable;
import static javax.measure.unit.SI.METRE;
import javax.measure.unit.Unit;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.VerticalCRS;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the Mean-Sea-Level {@link VerticalCRS vertical} 
 * altitude (MSL).
 *  
 * <p> Note: The current implementation approximates the MSL altitude to 
 *           the WGS-86 Ellipsoid Height. Future implementations will use 
 *           lookup tables in order to correct for regional discrepencies.</p> 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 26, 2006
 */
public final class Altitude extends Coordinates<VerticalCRS<?>> implements
        Measurable<Length> {

    /**
     * Holds the coordinate reference system for all instances of this class. 
     */
    public static final VerticalCRS<Altitude> CRS = new VerticalCRS<Altitude>() {

        @Override
        protected Altitude coordinatesOf(AbsolutePosition position) {
            return Altitude.valueOf(position.heightWGS84.doubleValue(METRE),
                    METRE);
        }

        @Override
        protected AbsolutePosition positionOf(Altitude coordinates,
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
     * Holds the altitude value in meters. 
     */
    private double _meters;

    /**
     * Returns the vertical position corresponding to the specified coordinates.
     * 
     * @param value the mean sea level altitude stated in the specified unit.
     * @param unit the length unit in which the altitude is stated.
     * @return the corresponding vertical position.
     */
    public static Altitude valueOf(double value, Unit<Length> unit) {
        Altitude altitude = FACTORY.object();
        altitude._meters = (unit == METRE) ? value : 
            unit.getConverterTo(METRE).convert(value);
        return altitude;
    }
    
    private static final ObjectFactory<Altitude> FACTORY = new ObjectFactory<Altitude>() {

        @Override
        protected Altitude create() {
            return new Altitude();
        } };
    
    private Altitude() {
    }

    @Override
    public VerticalCRS<?> getCoordinateReferenceSystem() {
        return Altitude.CRS;
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
        return (unit == METRE) ? _meters : 
            METRE.getConverterTo(unit).convert(_meters);
    }

    // Implements Scalar<Length>
    public final long longValue(Unit<Length> unit) {
        return Math.round(doubleValue(unit));
    }

    // Implements Scalar<Length>
    public int compareTo(Measurable<Length> measure) {
        double meters = measure.doubleValue(METRE);
        return (_meters  > meters) ? 1
                : (_meters < meters) ? -1 : 0;
    }

    @Override
    public Altitude copy() {
        return Altitude.valueOf(_meters, METRE);
    }
         
    // Default serialization.
    //
    
    static final XMLFormat<Altitude> XML = new XMLFormat<Altitude>(Altitude.class) {
        
        @Override
        public Altitude newInstance(Class<Altitude> cls, InputElement xml) throws XMLStreamException {
            return FACTORY.object();
        }
        
        public void write(Altitude altitude, OutputElement xml) throws XMLStreamException {
             xml.setAttribute("meters", altitude._meters);
         }

         public void read(InputElement xml, Altitude altitude) throws XMLStreamException {
             altitude._meters = xml.getAttribute("meters", 0.0);
         }
     };

     private static final long serialVersionUID = 1L;

}