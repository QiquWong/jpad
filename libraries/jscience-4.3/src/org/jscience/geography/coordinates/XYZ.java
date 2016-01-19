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
import javax.measure.quantity.*;
import static javax.measure.unit.SI.*;
import javax.measure.unit.Unit;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import static org.jscience.geography.coordinates.crs.ReferenceEllipsoid.WGS84;

import org.jscience.geography.coordinates.crs.GeocentricCRS;
import org.jscience.mathematics.vector.DimensionException;
import org.jscience.mathematics.vector.Float64Vector;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the {@link GeocentricCRS geocentric} Earth-Centered, 
 * Earth-Fixed (ECEF) cartesian coordinates used in GPS/GLONASS.
 *
 * @author Paul D. Anderson
 * @version 3.0, February 18, 2006
 */
public final class XYZ extends Coordinates<GeocentricCRS<XYZ>> {

    /**
     * Holds the coordinate reference system for all instances of this class.
     */
    public static final GeocentricCRS<XYZ> CRS = new GeocentricCRS<XYZ>() {

        @Override
        protected XYZ coordinatesOf(AbsolutePosition position) {
            double latitude = position.latitudeWGS84.doubleValue(RADIAN);
            double longitude = position.longitudeWGS84.doubleValue(RADIAN);
            double height = (position.heightWGS84 != null) ?
                position.heightWGS84.doubleValue(METRE) : 0.0;

            double cosLat = Math.cos(latitude);
            double sinLat = Math.sin(latitude);
            double cosLon = Math.cos(longitude);
            double sinLon = Math.sin(longitude);

            double roc = WGS84.verticalRadiusOfCurvature(latitude);
            double x = (roc + height) * cosLat * cosLon;
            double y = (roc + height) * cosLat * sinLon;
            double z = ((1.0 - WGS84.getEccentricitySquared()) * roc + height)
                    * sinLat;

            return XYZ.valueOf(x, y, z, METRE);
        }

        @Override
        protected AbsolutePosition positionOf(XYZ coordinates,
                AbsolutePosition position) {
            final double x = coordinates._x;
            final double y = coordinates._y;
            final double z = coordinates._z;

            final double longitude = Math.atan2(y, x);

            final double latitude;
            final double xy = Math.hypot(x, y);
            // conventional result if xy == 0.0...
            if (xy == 0.0) {
                latitude = (z >= 0.0) ? Math.PI / 2.0 : -Math.PI / 2.0;
            } else {
                final double a = WGS84.getSemimajorAxis().doubleValue(METRE);
                final double b = WGS84.getsSemiminorAxis().doubleValue(METRE);
                final double ea2 = WGS84.getEccentricitySquared();
                final double eb2 = WGS84.getSecondEccentricitySquared();
                final double beta = Math.atan2(a * z, b * xy);
                double numerator = z + b * eb2 * cube(Math.sin(beta));
                double denominator = xy - a * ea2 * cube(Math.cos(beta));
                latitude = Math.atan2(numerator, denominator);
            }

            final double height = xy / Math.cos(latitude)
                    - WGS84.verticalRadiusOfCurvature(latitude);
            position.latitudeWGS84 = Measure.valueOf(latitude, RADIAN);
            position.longitudeWGS84 = Measure.valueOf(longitude, RADIAN);
            position.heightWGS84 = Measure.valueOf(height, METRE);
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return GeocentricCRS.XYZ_CS;
        }
    };

    private static double cube(final double x) {
        return x * x * x;
    }

    /**
     * Holds the x position in meters.
     */
    private double _x;

    /**
     * Holds the y position in meters.
     */
    private double _y;

    /**
     * Holds the z position in meters.
     */
    private double _z;

    /**
     * Returns the spatial position corresponding to the specified coordinates.
     *
     * @param x the x value stated in the specified unit.
     * @param y the y value stated in the specified unit.
     * @param z the z value stated in the specified unit.
     * @param unit the length unit in which the coordinates are stated.
     * @return the corresponding 3D position.
     */
    public static XYZ valueOf(double x, double y, double z, Unit<Length> unit) {
        XYZ xyz = FACTORY.object();
        if (unit == METRE) {
            xyz._x = x;
            xyz._y = y;
            xyz._z = z;
        } else {
            UnitConverter toMeter = unit.getConverterTo(METRE);
            xyz._x = toMeter.convert(x);
            xyz._y = toMeter.convert(y);
            xyz._z = toMeter.convert(z);
        }
        return xyz;
    }

    private static final ObjectFactory<XYZ> FACTORY = new ObjectFactory<XYZ>() {

        @Override
        protected XYZ create() {
            return new XYZ();
        }
    };

    private XYZ() {
    }

    /**
     * Returns the spatial position corresponding to the specified 
     * 3-dimensional vector.
     *
     * @param vector the 3-dimensional vector holding the x/y/z coordinates.
     * @param unit the length unit in which the coordinates are stated.
     * @return the corresponding 3D position.
     */
    public static XYZ valueOf(Float64Vector vector, Unit<Length> unit) {
        if (vector.getDimension() != 3)
            throw new DimensionException("3-dimensional vector expected");
        return XYZ.valueOf(vector.getValue(0), vector.getValue(1), vector
                .getValue(2), unit);
    }

    /**
     * Returns the x coordinate value as <code>double</code>
     *
     * @param unit the length unit of the x coordinate value to return.
     * @return the x coordinate stated in the specified unit.
     */
    public double xValue(Unit<Length> unit) {
        return (unit == METRE) ? _x : METRE.getConverterTo(unit).convert(_x);
    }

    /**
     * Returns the y coordinate value as <code>double</code>
     *
     * @param unit the length unit of the x coordinate value to return.
     * @return the z coordinate stated in the specified unit.
     */
    public double yValue(Unit<Length> unit) {
        return (unit == METRE) ? _y : METRE.getConverterTo(unit).convert(_y);
    }

    /**
     * Returns the z coordinate value as <code>double</code>
     *
     * @param unit the length unit of the x coordinate value to return.
     * @return the z coordinate stated in the specified unit.
     */
    public double zValue(Unit<Length> unit) {
        return (unit == METRE) ? _z : METRE.getConverterTo(unit).convert(_z);
    }

    /**
     * Returns the x/y/z coordinates value as a 3-dimensional vector.
     *
     * @param unit the length unit of the vector coordinates.
     * @return a vector holding the x/y/z coordinates stated in the 
     *         specified unit.
     */
    public Float64Vector toVector(Unit<Length> unit) {
        if (unit == METRE)
            return Float64Vector.valueOf(_x, _y, _z);
        UnitConverter cvtr = METRE.getConverterTo(unit);
        return Float64Vector.valueOf(cvtr.convert(_x), cvtr.convert(_y), cvtr
                .convert(_z));
    }

    @Override
    public GeocentricCRS<XYZ> getCoordinateReferenceSystem() {
        return CRS;
    }

    // OpenGIS Interface.
    public int getDimension() {
        return 3;
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            Unit<?> u = GeocentricCRS.XYZ_CS.getAxis(0).getUnit();
            return METRE.getConverterTo(u).convert(_x);
        } else if (dimension == 1) {
            Unit<?> u = GeocentricCRS.XYZ_CS.getAxis(1).getUnit();
            return METRE.getConverterTo(u).convert(_y);
        } else if (dimension == 2) {
            Unit<?> u = GeocentricCRS.XYZ_CS.getAxis(2).getUnit();
            return METRE.getConverterTo(u).convert(_z);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public XYZ copy() {
        return XYZ.valueOf(_x, _y, _z, METRE);
    }

    // Default serialization.
    //

    static final XMLFormat<XYZ> XML = new XMLFormat<XYZ>(XYZ.class) {

        @Override
        public XYZ newInstance(Class<XYZ> cls, InputElement xml)
                throws XMLStreamException {
            return FACTORY.object();
        }

        public void write(XYZ xyz, OutputElement xml) throws XMLStreamException {
            xml.setAttribute("x", xyz._x);
            xml.setAttribute("y", xyz._y);
            xml.setAttribute("z", xyz._z);
        }

        public void read(InputElement xml, XYZ xyz) throws XMLStreamException {
            xyz._x = xml.getAttribute("x", 0.0);
            xyz._y = xml.getAttribute("y", 0.0);
            xyz._z = xml.getAttribute("z", 0.0);
        }
    };

    private static final long serialVersionUID = 1L;

}