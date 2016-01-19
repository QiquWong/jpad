/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import java.util.Date;

import javax.measure.Measurable;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Duration;
import static javax.measure.unit.SI.*;
import javax.measure.unit.Unit;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.TemporalCRS;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the {@link TemporalCRS temporal} UTC time coordinates.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 6, 2006
 */
public final class Time extends Coordinates<TemporalCRS<?>> implements Measurable<Duration> {

    /**
     * Holds the coordinate reference system for all instances of this class. 
     */
    public static final TemporalCRS<Time> CRS = new TemporalCRS<Time>() {

        @Override
        protected Time coordinatesOf(AbsolutePosition position) {
            if (position.timeUTC instanceof Time)
                return (Time) position.timeUTC;
            return Time.valueOf(position.timeUTC.doubleValue(SECOND),
                    SECOND);
        }

        @Override
        protected AbsolutePosition positionOf(Time coordinates, AbsolutePosition position) {
            position.timeUTC = coordinates;
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return TemporalCRS.TIME_CS;
        }
        
    };

    /**
     * Holds the time in second since midnight, January 1, 1970 UTC. 
     */
    private double _seconds;

    /**
     * Returns the temporal position corresponding to the specified coordinates.
     * 
     * @param value the time since midnight, January 1, 1970 UTC stated in the  
     *        specified unit.
     * @param unit the duration unit in which the time value is stated.
     * @return the corresponding temporal position.
     */
    public static Time valueOf(double value, Unit<Duration> unit) {
        Time time = FACTORY.object();
        if (unit == SECOND) {
            time._seconds = value;
        } else {
            UnitConverter toSecond = unit.getConverterTo(SECOND);
            time._seconds = toSecond.convert(value);
        }
        return time;
    }

    private static final ObjectFactory<Time> FACTORY = new ObjectFactory<Time>() {

        @Override
        protected Time create() {
            return new Time();
        }
    };
   
    private Time() {    
    }
   
        
    /**
     * Returns the temporal position corresponding to the specified date.
     * 
     * @param date the date.
     * @return the corresponding temporal position.
     */
    public static Time valueOf(Date date) {
        return Time.valueOf(date.getTime(), MILLI(SECOND));
    }

    /**
     * Creates the temporal position corresponding to the specified coordinates.
     * 
     * @param value the time since midnight, January 1, 1970 UTC stated in the  
     *        specified unit.
     * @param unit the duration unit in which the time value is stated.
     */
    public Time(double value, Unit<Duration> unit) {
    }

    @Override
    public TemporalCRS<?> getCoordinateReferenceSystem() {
        return CRS;
    }

    // OpenGIS Interface.
    public int getDimension() {
        return 1;
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            Unit<?> u = TemporalCRS.TIME_CS.getAxis(0).getUnit();
            return SECOND.getConverterTo(u).convert(_seconds);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // Implements Scalar<Duration>
    public final double doubleValue(Unit<Duration> unit) {
        return unit.equals(SECOND) ? _seconds : SECOND
                .getConverterTo(unit).convert(_seconds);
    }

    // Implements Scalar<Duration>
    public final long longValue(Unit<Duration> unit) {
        return Math.round(doubleValue(unit));
    }

    // Implements Scalar<Duration>
    public int compareTo(Measurable<Duration> arg0) {
        double arg0InSecond = arg0.doubleValue(SECOND);
        return (_seconds > arg0InSecond) ? 1
                : (_seconds < arg0InSecond) ? -1 : 0;
    }

    // Implements Realtime.
    public Time copy() {
        return Time.valueOf(_seconds, SECOND);
    }
    
    // Default serialization.
    //
    
    static final XMLFormat<Time> XML = new XMLFormat<Time>(Time.class) {
        
        @Override
        public Time newInstance(Class<Time> cls, InputElement xml) throws XMLStreamException {
            return FACTORY.object();
        }
        
        public void write(Time time, OutputElement xml) throws XMLStreamException {
             xml.setAttribute("seconds", time._seconds);
         }

         public void read(InputElement xml, Time time) throws XMLStreamException {
             time._seconds = xml.getAttribute("seconds", 0.0);
         }
     };

    private static final long serialVersionUID = 1L;

}