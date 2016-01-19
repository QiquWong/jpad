/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates.crs;

import java.util.Collection;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.Measurable;
import javax.measure.unit.Unit;

import javolution.util.FastSet;

import org.jscience.geography.coordinates.Coordinates;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.util.InternationalString;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/**
 * This class represents an arbitrary system of reference for which 
 * {@link Coordinates coordinates} of same significance can be stated.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public abstract class CoordinateReferenceSystem<C extends Coordinates<?>>
        implements org.opengis.referencing.crs.CoordinateReferenceSystem {

    
    /**
     * This class represents an absolute position (can be be extended)
     */
    protected static class AbsolutePosition {

        /**
         * Holds the Geodetic Latitude (WGS84 Ellipsoid).
         */
        public Measurable<Angle> latitudeWGS84;

        /**
         * Holds the Geodetic Longitude (WGS84 Ellipsoid).
         */
        public Measurable<Angle> longitudeWGS84;

        /**
         * Holds the WGS84 Ellipsoidal Height.
         */
        public Measurable<Length> heightWGS84;

        /**
         * Holds the Time since midnight, January 1, 1970 UTC. 
         */
        public Measurable<Duration> timeUTC;
    }

    /**
     * Returns the converter between this coordinate reference system 
     * and the one specified.
     * 
     * @param  that the coordinate reference system to convert to.
     * @return the corresponding coordinates converter.
     * @throws ConversionException if the conversion is not possible
     *         (e.g. geographic to temporal).
     */
    public <T extends Coordinates<?>> CoordinatesConverter<C, T> getConverterTo(
            CoordinateReferenceSystem<T> that) {
        return new GeneralConverter<T>(that);
    }

    // General implementation using absolute position as intermediary.
    private class GeneralConverter<T extends Coordinates<?>> implements
            CoordinatesConverter<C, T> {
        private final CoordinateReferenceSystem<T> _toCRS;

        private GeneralConverter(CoordinateReferenceSystem<T> toCRS) {
            _toCRS = toCRS;
        }

        public T convert(C source) {
            AbsolutePosition position = positionOf(source,
                    new AbsolutePosition());
            return _toCRS.coordinatesOf(position);
        }
    }

    /**
     * Returns the coordinates in this reference system of the specified 
     * absolute position.
     * 
     * @param position the absolute position for which the coordinates  
     *        in this reference system is returned.
     * @return the coordinates for the specified absolute position.
     * @throws ConversionException if a conversion error occurs.
     */
    protected abstract C coordinatesOf(AbsolutePosition position);

    /**
     * Returns the absolute position from the coordinates in
     * this reference system. This update may require information already 
     * supplied by the position. For example, the height for a pressure 
     * altitude might depends upon the latitude/longitude and the time.
     * 
     * @param coordinates the coordinates for which the absolute position 
     *        is adjusted.
     * @param position the position object to update.
     * @return the corresponding absolute position. 
     * @throws ConversionException if a conversion error occurs.
     */
    protected abstract AbsolutePosition positionOf(C coordinates,
            AbsolutePosition position);

    /**
     * Returns the OpenGIS coordinate system associated to this 
     * coordinate reference system.
     * 
     * @return the corresponding coordinate system. 
     */
    public abstract CoordinateSystem getCoordinateSystem();

    /////////////
    // OpenGIS //
    /////////////
    
    /**
     * OpenGIS&reg; - Area for which the (coordinate) reference system is valid.
     *
     * @return coordinate reference system valid area, 
     *         or {@code null} (default) if not available.
     */
    public Extent getValidArea() {
        return null;
    }

    /**
     * OpenGIS&reg; - Description of domain of usage, or limitations of usage,
     * for which this (coordinate) reference system object is valid.
     */
    public InternationalString getScope() {
        throw new UnsupportedOperationException();
    }

    /**
     * OpenGIS&reg; - The primary name by which this object is identified.
     * 
     * @return an identifier holding the class name.
     */
    public Identifier getName() {
        return new Name(CoordinateReferenceSystem.this.getClass().getName());
    }

    /**
     * OpenGIS&reg; - An alternative name by which this object is identified.
     *
     * @return The aliases, or an empty collection if there is none.
     */
    public Collection<String> getAlias() {
        return EMPTY_SET;
    }

    /**
     * OpenGIS&reg;  - An identifier which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
     *
     * @return This object identifiers, or an empty set if there is none.
     */
    public Set<String> getIdentifiers() {
        return EMPTY_SET;
    }

    /**
     * OpenGIS&reg; - Comments on or information about this object, including 
     * data source information.
     * 
     * @return <code>null</code> (default).
     */
    public InternationalString getRemarks() {
        return null;
    }

    /**
     * OpenGIS&reg; - Returns a <cite>Well Known Text</cite> (WKT)</A> for 
     * this object. This operation may fails if an object is too complex for
     * the WKT format capability (for example an engineering CRS} with different
     * unit for each axis).
     *
     * @return The Well Know Text for this object.
     * @throws UnsupportedOperationException If this object can't be formatted 
     *         as WKT (default).
     */
    public String toWKT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    // Default coordinates axis.
    static class Axis implements CoordinateSystemAxis {

        private final Name _name;

        private final String _abbreviation;

        private final Unit<?> _unit;

        private final AxisDirection _direction;

        public Axis(final String name, String abbreviation, Unit<?> unit,
                AxisDirection direction) {
            _name = new Name(name);
            _abbreviation = abbreviation;
            _unit = unit;
            _direction = direction;
        }

        public final Identifier getName() {
            return _name;
        }

        public final String getAbbreviation() {
            return _abbreviation;
        }

        public final Unit<?> getUnit() {
            return _unit;
        }

        public final AxisDirection getDirection() {
            return _direction;
        }

        public Collection<String> getAlias() {
            return EMPTY_SET;
        }

        public Set<String> getIdentifiers() {
            return EMPTY_SET;
        }

        public InternationalString getRemarks() {
            throw new UnsupportedOperationException();
        }

        public String toWKT() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

    }

    // Default coordinates axis.
    static class Name implements Identifier {
        final String _value;

        public Name(String value) {
            _value = value;
        }

        public String getCode() {
            return _value;
        }

        public Citation getAuthority() {
            throw new UnsupportedOperationException();
        }

        public String getVersion() {
            throw new UnsupportedOperationException();
        }
    }

    static final FastSet<String> EMPTY_SET = new FastSet<String>();
}