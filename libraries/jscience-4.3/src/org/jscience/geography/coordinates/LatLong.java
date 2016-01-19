/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import javax.measure.Measure;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.SI.RADIAN;
import javax.measure.unit.Unit;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.GeographicCRS;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the {@link GeographicCRS geographic} latitude/longitude
 * coordinates onto the WGS84 ellipsoid.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public final class LatLong extends Coordinates<GeographicCRS<?>> {

    /**
     * Holds the coordinate reference system for all instances of this class. 
     */
    public static final GeographicCRS<LatLong> CRS = new GeographicCRS<LatLong>() {

        @Override
        protected LatLong coordinatesOf(AbsolutePosition position) {
            return LatLong.valueOf(position.latitudeWGS84.doubleValue(DEGREE_ANGLE),
                    position.longitudeWGS84.doubleValue(DEGREE_ANGLE), DEGREE_ANGLE);
        }

        @Override
        protected AbsolutePosition positionOf(LatLong coordinates,
                AbsolutePosition position) {
            position.latitudeWGS84 = Measure.valueOf(coordinates._latitude,
                    DEGREE_ANGLE);
            position.longitudeWGS84 = Measure.valueOf(coordinates._longitude,
                    DEGREE_ANGLE);
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return GeographicCRS.LATITUDE_LONGITUDE_CS;
        }

    };

    /**
     * Holds converter from degree to radian. 
     */
    private static final UnitConverter DEGREE_TO_RADIAN = DEGREE_ANGLE
            .getConverterTo(RADIAN);

    /**
     * Holds converter from radian to degree. 
     */
    private static final UnitConverter RADIAN_TO_DEGREE = DEGREE_TO_RADIAN
            .inverse();

    /**
     * Holds the latitude in degrees. 
     */
    private double _latitude;

    /**
     * Holds the longitude in degrees. 
     */
    private double _longitude;

    /**
     * Returns the surface position corresponding to the specified coordinates.
     * 
     * @param latitude the latitude value stated in the specified unit.
     * @param longitude the longitude value stated in the specified unit.
     * @param unit the angle unit in which the coordinates are stated
     *        ({@link javax.measure.unit.NonSI#DEGREE_ANGLE Degree} typically).
     * @return the corresponding surface position.
     */
    public static LatLong valueOf(double latitude, double longitude,
            Unit<Angle> unit) {
        LatLong latLong = FACTORY.object();
        if (unit == DEGREE_ANGLE) {
            latLong._latitude = latitude;
            latLong._longitude = longitude;
        } else if (unit == RADIAN) {
            latLong._latitude = RADIAN_TO_DEGREE.convert(latitude);
            latLong._longitude = RADIAN_TO_DEGREE.convert(longitude);
        } else { // Other angle unit.
            UnitConverter toDegree = unit.getConverterTo(DEGREE_ANGLE);
            latLong._latitude = toDegree.convert(latitude);
            latLong._longitude = toDegree.convert(longitude);
        }
        return latLong;
    }

    private static final ObjectFactory<LatLong> FACTORY = new ObjectFactory<LatLong>() {

        @Override
        protected LatLong create() {
            return new LatLong();
        }
    };

    private LatLong() {
    }

    /**
     * Returns the latitude value as <code>double</code>
     * 
     * @param unit the angle unit of the latitude to return.
     * @return the latitude stated in the specified unit.
     */
    public final double latitudeValue(Unit<Angle> unit) {
        return (unit == DEGREE_ANGLE) ? _latitude
                : (unit == RADIAN) ? DEGREE_TO_RADIAN.convert(_latitude)
                        : DEGREE_ANGLE.getConverterTo(unit).convert(_latitude);
    }

    /**
     * Returns the longitude value as <code>double</code>
     * 
     * @param unit the angle unit of the longitude to return.
     * @return the longitude stated in the specified unit.
     */
    public final double longitudeValue(Unit<Angle> unit) {
        return (unit == DEGREE_ANGLE) ? _longitude
                : (unit == RADIAN) ? DEGREE_TO_RADIAN.convert(_longitude)
                        : DEGREE_ANGLE.getConverterTo(unit).convert(_longitude);
    }

    @Override
    public GeographicCRS<LatLong> getCoordinateReferenceSystem() {
        return CRS;
    }

    // OpenGIS Interface.
    public int getDimension() {
        return 2;
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            Unit<?> u = GeographicCRS.LATITUDE_LONGITUDE_CS.getAxis(0).getUnit();
            return DEGREE_ANGLE.getConverterTo(u).convert(_latitude);
        } else if (dimension == 1) {
            Unit<?> u = GeographicCRS.LATITUDE_LONGITUDE_CS.getAxis(1).getUnit();
            return DEGREE_ANGLE.getConverterTo(u).convert(_longitude);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // Implements Realtime.
    public LatLong copy() {
        return LatLong.valueOf(_latitude, _longitude, DEGREE_ANGLE);
    }

    // Default serialization.
    //

    static final XMLFormat<LatLong> XML = new XMLFormat<LatLong>(LatLong.class) {

        @Override
        public LatLong newInstance(Class<LatLong> cls, InputElement xml)
                throws XMLStreamException {
            return FACTORY.object();
        }

        public void write(LatLong latLong, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("latitude", latLong._latitude);
            xml.setAttribute("longitude", latLong._longitude);
        }

        public void read(InputElement xml, LatLong latLong)
                throws XMLStreamException {
            latLong._latitude = xml.getAttribute("latitude", 0.0);
            latLong._longitude = xml.getAttribute("longitude", 0.0);
        }
    };

    private static final long serialVersionUID = 1L;
}