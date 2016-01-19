/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import static org.jscience.geography.coordinates.crs.ReferenceEllipsoid.WGS84;

import javax.measure.Measure;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.*;
import javax.measure.unit.*;
import static javax.measure.unit.SI.*;
import static javax.measure.unit.NonSI.*;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.*;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * This class represents the {@link ProjectedCRS projected}
 * Universal Transverse Mercator (UTM) coordinates onto the WGS84 ellipsoid.
 *
 * <p>The UTM system is limited to values between -80 and +84 degrees latitude.
 * Values beyond these limits (i.e., the polar regions) are projected
 * using the Universal Polar Stereographic (UPS) projection. Although
 * mathematically distinct, the two projections are represented identically.
 * This class returns correct results for both UTM and UPS projections.
 *
 * The conversion routines for this class were derived from formulas described
 * in the
 * <a href="http://earth-info.nga.mil/GandG/publications/tm8358.2/TM8358_2.pdf">
 * Defense Mapping Agency Technical Manual 8358.2.</a>
 *
 * @author Paul D. Anderson
 * @version 3.0, February 25, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system">
 *      Wikipedia: Universal Transverse Mercator Coordinate System</a>
 */
public final class UTM extends Coordinates<ProjectedCRS<?>> {

    /**
     * The UTM scale factor. This the exact scale factor only on a pair of
     * lines lying either side of the central meridian, but the effect is to
     * reduce overall distortion within the UTM zone to less than one part per
     * thousand.
     */
    public static final double UTM_SCALE_FACTOR = 0.9996;

    /**
     * The UTM "false easting" value. This quantity is added to the true
     * easting to avoid using negative numbers in the coordinates.
     */
    public static Measure<Integer, Length> UTM_FALSE_EASTING = Measure.valueOf(
            500000, METRE);

    /**
     * The UTM "false northing" value. This quantity is added to the true
     * northing for coordinates <b>in the southern hemisphere only</b>
     * to avoid using negative numbers in the coordinates.
     */
    public static Measure<Integer, Length> UTM_FALSE_NORTHING = Measure
            .valueOf(10000000, METRE);

    /**
     * The northern limit of the UTM grid. Beyond this limit the distortion
     * introduced by the transverse Mercator projection is impractically
     * large, and the UPS grid is used instead.
     */
    public static final Measure<Integer, Angle> UTM_NORTHERN_LIMIT = Measure
            .valueOf(84, DEGREE_ANGLE);

    /**
     * The southern limit of the UTM grid. Beyond this limit the distortion
     * introduced by the transverse Mercator projection is impractically
     * large, and the UPS grid is used instead.
     */
    public static final Measure<Integer, Angle> UTM_SOUTHERN_LIMIT = Measure
            .valueOf(-80, DEGREE_ANGLE);

    /**
     * The UPS scale factor.
     */
    public static final double UPS_SCALE_FACTOR = 0.994;

    /**
     * The UPS "false easting" value. This quantity is added to the true
     * easting to avoid using negative numbers in the coordinates.
     */
    public static Measure<Integer, Length> UPS_FALSE_EASTING = Measure.valueOf(
            2000000, METRE);

    /**
     * The UPS "false northing" value. This quantity is added to the true
     * northing to avoid using negative numbers in the coordinates.
     * The UPS system, unlike the UTM system, always includes the false northing.
     */
    public static Measure<Integer, Length> UPS_FALSE_NORTHING = Measure
            .valueOf(2000000, METRE);

    /*
     * NOTE: The calculations in this class use power series expansions.
     * The naming convention is to include the power in the name
     * of the term, so that the square of K0 is 'K02', the cube
     * is 'K03', etc.
     */
    private static final double K0 = UTM_SCALE_FACTOR;

    private static final double K02 = K0 * K0;

    private static final double K03 = K02 * K0;

    private static final double K04 = K03 * K0;

    private static final double K05 = K04 * K0;

    private static final double K06 = K05 * K0;

    private static final double K07 = K06 * K0;

    private static final double K08 = K07 * K0;

    /**
     * Holds the coordinate reference system for all instances of this class.
     */
    public static final ProjectedCRS<UTM> CRS = new ProjectedCRS<UTM>() {

        private final double NORTHERN_LIMIT_IN_DEGREES = UTM_NORTHERN_LIMIT
                .doubleValue(NonSI.DEGREE_ANGLE);

        private final double SOUTHERN_LIMIT_IN_DEGREES = UTM_SOUTHERN_LIMIT
                .doubleValue(NonSI.DEGREE_ANGLE);

        @Override
        protected UTM coordinatesOf(AbsolutePosition position) {
            LatLong latLong = LatLong.valueOf(position.latitudeWGS84
                    .doubleValue(SI.RADIAN), position.longitudeWGS84
                    .doubleValue(SI.RADIAN), SI.RADIAN);
            // UTM or UPS?
            final double latitude = position.latitudeWGS84
                    .doubleValue(NonSI.DEGREE_ANGLE);
            if (latitude > SOUTHERN_LIMIT_IN_DEGREES
                    && latitude < NORTHERN_LIMIT_IN_DEGREES) {
                return latLongToUtm(latLong, WGS84);
            } else {
                return latLongToUps(latLong, WGS84);
            }
        }

        @Override
        protected AbsolutePosition positionOf(UTM coordinates,
                AbsolutePosition position) {
            final LatLong latLong;
            if (coordinates.latitudeZone() < 'C'
                    || coordinates.latitudeZone() > 'X') {
                latLong = upsToLatLong(coordinates, WGS84);
            } else {
                latLong = utmToLatLong(coordinates, WGS84);
            }
            position.latitudeWGS84 = Measure.valueOf(latLong
                    .latitudeValue(SI.RADIAN), SI.RADIAN);
            position.longitudeWGS84 = Measure.valueOf(latLong
                    .longitudeValue(SI.RADIAN), SI.RADIAN);
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return ProjectedCRS.EASTING_NORTHING_CS;
        }
    };

    /**
     * Holds the longitude zone identifier.
     */
    private int _longitudeZone;

    /**
     * Holds the latitude zone identifier.
     */
    private char _latitudeZone;

    /**
     * Holds the easting in meters.
     */
    private double _easting;

    /**
     * Holds the northing in meters.
     */
    private double _northing;

    /**
     * Returns the projected UTM position corresponding to the specified
     * coordinates.
     *
     * @param longitudeZone the longitude zone number.
     * @param latitudeZone  the longitude zone character.
     * @param easting       the easting value stated in the specified unit.
     * @param northing      the northing value stated in the specified unit.
     * @param unit          the easting/northing length unit.
     * @return the corresponding surface position.
     */
    public static UTM valueOf(int longitudeZone, char latitudeZone,
            double easting, double northing, Unit<Length> unit) {
        UTM utm = FACTORY.object();
        utm._longitudeZone = longitudeZone;
        utm._latitudeZone = latitudeZone;
        if (unit == METRE) {
            utm._easting = easting;
            utm._northing = northing;
        } else {
            UnitConverter toMeter = unit.getConverterTo(METRE);
            utm._easting = toMeter.convert(easting);
            utm._northing = toMeter.convert(northing);
        }
        return utm;
    }

    private static final ObjectFactory<UTM> FACTORY = new ObjectFactory<UTM>() {

        @Override
        protected UTM create() {
            return new UTM();
        }
    };

    private UTM() {
    }

    /**
     * Returns the longitude zone identifier.
     *
     * @return the longitude zone number.
     */
    public final int longitudeZone() {
        return _longitudeZone;
    }

    /**
     * Returns the latitude zone identifier.
     *
     * @return the latitude zone character.
     */
    public final char latitudeZone() {
        return _latitudeZone;
    }

    /**
     * Returns the projected distance of the position from the central meridian.
     *
     * @param unit the length unit of the easting to return.
     * @return the easting stated in the specified unit.
     */
    public final double eastingValue(Unit<Length> unit) {
        return unit.equals(METRE) ? _easting : METRE.getConverterTo(unit)
                .convert(_easting);
    }

    /**
     * Returns the projected distance of the point from the equator.
     *
     * @param unit the length unit of the northing to return.
     * @return the northing stated in the specified unit.
     */
    public final double northingValue(Unit<Length> unit) {
        return unit.equals(METRE) ? _northing : METRE.getConverterTo(unit)
                .convert(_northing);
    }

    @Override
    public ProjectedCRS<UTM> getCoordinateReferenceSystem() {
        return CRS;
    }

    // OpenGIS Interface.
    public int getDimension() {
        return 2;
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            Unit<?> u = ProjectedCRS.EASTING_NORTHING_CS.getAxis(0).getUnit();
            return METRE.getConverterTo(u).convert(_easting);
        } else if (dimension == 1) {
            Unit<?> u = ProjectedCRS.EASTING_NORTHING_CS.getAxis(1).getUnit();
            return METRE.getConverterTo(u).convert(_northing);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns true if the position indicated by the coordinates is
     * north of the northern limit of the UTM grid (84 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is greater than 84 degrees.
     */
    public static boolean isNorthPolar(final LatLong latLong) {
        return latLong.latitudeValue(DEGREE_ANGLE) > 84.0;
    }

    /**
     * Returns true if the position indicated by the coordinates is
     * south of the southern limit of the UTM grid (-80 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is less than -80 degrees.
     */
    public static boolean isSouthPolar(final LatLong latLong) {
        return latLong.latitudeValue(DEGREE_ANGLE) < -80.0;
    }

    /**
     * Returns the UTM/UPS latitude zone identifier for the specified coordinates.
     *
     * @param latLong The coordinates.
     * @return the latitude zone character.
     */
    public static char getLatitudeZone(final LatLong latLong) {
        if (isNorthPolar(latLong)) {
            if (latLong.longitudeValue(RADIAN) < 0) {
                return 'Y';
            } else {
                return 'Z';
            }
        }
        if (isSouthPolar(latLong)) {
            if (latLong.longitudeValue(RADIAN) < 0) {
                return 'A';
            } else {
                return 'B';
            }
        }
        final int degreesLatitude = (int) latLong.latitudeValue(DEGREE_ANGLE);
        char zone = (char) ((degreesLatitude + 80) / 8 + 'C');
        if (zone > 'H') {
            zone++;
        }
        if (zone > 'N') {
            zone++;
        }
        if (zone > 'X') {
            zone = 'X';
        }
        return zone;
    }

    /**
     * Returns the UTM/UPS longitude zone number for the specified
     * coordinates.
     *
     * @param latLong  The coordinates.
     * @return the longitude zone number.
     */
    public static int getLongitudeZone(LatLong latLong) {

        final double degreesLongitude = latLong.longitudeValue(DEGREE_ANGLE);

        // UPS longitude zones
        if (isNorthPolar(latLong) || isSouthPolar(latLong)) {
            if (degreesLongitude < 0.0) {
                return 30;
            } else {
                return 31;
            }
        }

        final char latitudeZone = getLatitudeZone(latLong);
        // X latitude exceptions
        if (latitudeZone == 'X' && degreesLongitude > 0.0
                && degreesLongitude < 42.0) {
            if (degreesLongitude < 9.0) {
                return 31;
            }
            if (degreesLongitude < 21.0) {
                return 33;
            }
            if (degreesLongitude < 33.0) {
                return 35;
            } else {
                return 37;
            }
        }
        // V latitude exceptions
        if (latitudeZone == 'V' && degreesLongitude > 0.0
                && degreesLongitude < 12.0) {
            if (degreesLongitude < 3.0) {
                return 31;
            } else {
                return 32;
            }
        }

        return (int) ((degreesLongitude + 180) / 6) + 1;
    }

    /**
     * Returns the central meridian (in radians) for the specified
     * UTM/UPS zone.
     * @param longitudeZone The UTM/UPS longitude zone number.
     * @param latitudeZone  The UTM/UPS latitude zone character.
     * @return The central meridian for the specified zone.
     */
    public static double getCentralMeridian(int longitudeZone, char latitudeZone) {
        // polar zones
        if (latitudeZone < 'C' || latitudeZone > 'X') {
            return 0.0;
        }
        // X latitude zone exceptions
        if (latitudeZone == 'X' && longitudeZone > 31 && longitudeZone <= 37) {
            return Math.toRadians((longitudeZone - 1) * 6 - 180 + 4.5);
        }
        // V latitude zone exceptions
        if (longitudeZone == 'V') {
            if (latitudeZone == 31) {
                return Math.toRadians(1.5);
            } else if (latitudeZone == 32) {
                return Math.toRadians(7.5);
            }
        }
        return Math.toRadians((longitudeZone - 1) * 6 - 180 + 3);
    }

    /**
     * Converts latitude/longitude coordinates to UTM coordinates based on
     * the specified reference ellipsoid.
     *
     * @param latLong   The latitude/longitude coordinates.
     * @param ellipsoid The reference ellipsoid.
     * @return  The UTM coordinates.
     */
    public static UTM latLongToUtm(LatLong latLong, ReferenceEllipsoid ellipsoid) {
        final char latitudeZone = getLatitudeZone(latLong);
        final int longitudeZone = getLongitudeZone(latLong);

        final double phi = latLong.latitudeValue(RADIAN);

        final double cosPhi = Math.cos(phi);
        final double cos2Phi = cosPhi * cosPhi;
        final double cos3Phi = cos2Phi * cosPhi;
        final double cos4Phi = cos3Phi * cosPhi;
        final double cos5Phi = cos4Phi * cosPhi;
        final double cos6Phi = cos5Phi * cosPhi;
        final double cos7Phi = cos6Phi * cosPhi;
        final double cos8Phi = cos7Phi * cosPhi;

        final double tanPhi = Math.tan(phi);
        final double tan2Phi = tanPhi * tanPhi;
        final double tan4Phi = tan2Phi * tan2Phi;
        final double tan6Phi = tan4Phi * tan2Phi;

        final double eb2 = ellipsoid.getSecondEccentricitySquared();
        final double eb4 = eb2 * eb2;
        final double eb6 = eb4 * eb2;
        final double eb8 = eb6 * eb2;

        final double e2c2 = eb2 * cos2Phi;
        final double e4c4 = eb4 * cos4Phi;
        final double e6c6 = eb6 * cos6Phi;
        final double e8c8 = eb8 * cos8Phi;

        final double t2e2c2 = tan2Phi * e2c2;
        final double t2e4c4 = tan2Phi * e4c4;
        final double t2e6c6 = tan2Phi * e6c6;
        final double t2e8c8 = tan2Phi * e8c8;

        final double nu = ellipsoid.verticalRadiusOfCurvature(phi);
        final double kn1 = K0 * nu * Math.sin(phi);
        final double t1 = K0 * ellipsoid.meridionalArc(phi);
        final double t2 = kn1 * cosPhi / 2.0;
        final double t3 = (kn1 * cos3Phi / 24.0)
                * (5.0 - tan2Phi + 9.0 * e2c2 + 4.0 * e4c4);
        final double t4 = (kn1 * cos5Phi / 720.0)
                * (61.0 - 58.0 * tan2Phi + tan4Phi + 270.0 * e2c2 - 330.0
                        * t2e2c2 + 445.0 * e4c4 - 680.0 * t2e4c4 + 324.0 * e6c6
                        - 600.0 * t2e6c6 + 88.0 * e8c8 - 192.0 * t2e8c8);
        final double t5 = (kn1 * cos7Phi / 40320.0)
                * (1385.0 - 3111.0 * tan2Phi + 543.0 * tan4Phi - tan6Phi);

        final double kn2 = K0 * nu;
        final double t6 = kn2 * cosPhi;
        final double t7 = (kn2 * cos3Phi / 6.0) * (1.0 - tan2Phi + e2c2);
        final double t8 = (kn2 * cos5Phi / 120.0)
                * (5.0 - 18.0 * tan2Phi + tan4Phi + 14.0 * e2c2 - 58.0 * t2e2c2
                        + 13.0 * e4c4 - 64.0 * t2e4c4 + 4.0 * e6c6 - 24.0 * t2e6c6);
        final double t9 = (kn2 * cos7Phi / 50.40)
                * (61.0 - 479.0 * tan2Phi + 179.0 * tan4Phi - tan6Phi);

        final double lambda = latLong.longitudeValue(RADIAN);
        final double lambda0 = getCentralMeridian(longitudeZone, latitudeZone);
        final double dL = lambda - lambda0;
        final double dL2 = dL * dL;
        final double dL3 = dL2 * dL;
        final double dL4 = dL3 * dL;
        final double dL5 = dL4 * dL;
        final double dL6 = dL5 * dL;
        final double dL7 = dL6 * dL;
        final double dL8 = dL7 * dL;

        final double falseNorthing;
        if ((phi < 0.0)) {
            // southern hemisphere -- add false northing
            falseNorthing = UTM_FALSE_NORTHING.doubleValue(METRE);
        } else {
            // northern hemisphere -- no false northing
            falseNorthing = 0.0;
        }
        final double falseEasting = UTM_FALSE_EASTING.doubleValue(METRE);
        final double northing = falseNorthing + t1 + dL2 * t2 + dL4 * t3 + dL6
                * t4 + dL8 * t5;
        final double easting = falseEasting + dL * t6 + dL3 * t7 + dL5 * t8
                + dL7 * t9;

        return UTM.valueOf(longitudeZone, latitudeZone, easting, northing,
                METRE);
    }

    /**
     * Converts latitude/longitude coordinates to UPS coordinates based on
     * the specified reference ellipsoid.
     *
     * @param latLong   The latitude/longitude coordinates.
     * @param ellipsoid The reference ellipsoid.
     * @return  The UPS coordinates.
     */
    public static UTM latLongToUps(LatLong latLong, ReferenceEllipsoid ellipsoid) {

        final char latitudeZone = getLatitudeZone(latLong);
        final int longitudeZone = getLongitudeZone(latLong);

        final double latitude = latLong.latitudeValue(RADIAN);
        final double sign = Math.signum(latitude);
        final double phi = Math.abs(latitude);
        final double lambda = latLong.longitudeValue(RADIAN);

        final double a = ellipsoid.getSemimajorAxis().doubleValue(METRE);
        final double e = ellipsoid.getEccentricity();
        final double e2 = ellipsoid.getEccentricitySquared();

        final double c0 = ((2.0 * a) / Math.sqrt(1.0 - e2))
                * Math.pow((1.0 - e) / (1.0 + e), e / 2.0);
        final double eSinPhi = e * Math.sin(phi);
        final double tz = Math.pow((1 + eSinPhi) / (1 - eSinPhi), e / 2.0)
                * Math.tan(Math.PI / 4.0 - phi / 2.0);
        final double radius = UPS_SCALE_FACTOR * c0 * tz;
        final double falseNorthing = UPS_FALSE_NORTHING.doubleValue(METRE);
        final double northing;
        if (sign > 0) {
            northing = falseNorthing - radius * Math.cos(lambda);
        } else {
            northing = falseNorthing + radius * Math.cos(lambda);
        }
        final double falseEasting = UPS_FALSE_EASTING.doubleValue(METRE);
        final double easting = falseEasting + radius * Math.sin(lambda);

        return UTM.valueOf(longitudeZone, latitudeZone, easting, northing,
                METRE);
    }

    /**
     * Converts the UTM coordinates to latitude/longitude coordinates,
     * based on the specified reference ellipsoid.
     *
     * @param utm   The UTM coordinates.
     * @param ellipsoid The reference ellipsoid.
     * @return  The latitude/longitude coordinates.
     */
    public static LatLong utmToLatLong(UTM utm, ReferenceEllipsoid ellipsoid) {
        final double northing;
        if ((utm.latitudeZone() < 'N')) {
            // southern hemisphere
            northing = utm._northing - UTM_FALSE_NORTHING.doubleValue(METRE);
        } else {
            // northern hemisphere
            northing = utm._northing;
        }

        // footpoint latitude
        final double arc0 = northing / K0;
        double rho = ellipsoid.meridionalRadiusOfCurvature(0.0);
        double phi = arc0 / rho;
        for (int i = 0; i < 5; i++) {
            double arc = ellipsoid.meridionalArc(phi);
            rho = ellipsoid.meridionalRadiusOfCurvature(phi);
            double diff = (arc0 - arc) / rho;
            if (Math.abs(diff) < Math.ulp(phi)) {
                break;
            }
            phi += diff;
        }

        final double cosPhi = Math.cos(phi);
        final double cos2Phi = cosPhi * cosPhi;
        final double cos3Phi = cos2Phi * cosPhi;
        final double cos4Phi = cos3Phi * cosPhi;
        final double cos5Phi = cos4Phi * cosPhi;
        final double cos6Phi = cos5Phi * cosPhi;
        final double cos7Phi = cos6Phi * cosPhi;
        final double cos8Phi = cos7Phi * cosPhi;

        final double tanPhi = Math.tan(phi);
        final double tan2Phi = tanPhi * tanPhi;
        final double tan4Phi = tan2Phi * tan2Phi;
        final double tan6Phi = tan4Phi * tan2Phi;

        final double eb2 = ellipsoid.getSecondEccentricitySquared();
        final double eb4 = eb2 * eb2;
        final double eb6 = eb4 * eb2;
        final double eb8 = eb6 * eb2;
        final double e2c2 = eb2 * cos2Phi;
        final double e4c4 = eb4 * cos4Phi;
        final double e6c6 = eb6 * cos6Phi;
        final double e8c8 = eb8 * cos8Phi;

        final double t2e2c2 = tan2Phi * e2c2;
        final double t2e4c4 = tan2Phi * e4c4;
        final double t2e6c6 = tan2Phi * e6c6;
        final double t2e8c8 = tan2Phi * e8c8;
        final double t4e2c2 = tan4Phi * e2c2;
        final double t4e4c4 = tan4Phi * e4c4;

        final double nu = ellipsoid.verticalRadiusOfCurvature(phi);
        final double nu2 = nu * nu;
        final double nu3 = nu2 * nu;
        final double nu5 = nu3 * nu2;
        final double nu7 = nu5 * nu2;

        final double lambda0 = getCentralMeridian(utm.longitudeZone(), utm
                .latitudeZone());
        final double dE = utm._easting - UTM_FALSE_EASTING.doubleValue(METRE);
        final double dE2 = dE * dE;
        final double dE3 = dE2 * dE;
        final double dE4 = dE3 * dE;
        final double dE5 = dE4 * dE;
        final double dE6 = dE5 * dE;
        final double dE7 = dE6 * dE;
        final double dE8 = dE7 * dE;

        final double t10 = tanPhi / (2.0 * rho * nu * K02);
        final double t11 = tanPhi / (24.0 * rho * nu3 * K04)
                * (5.0 + 3.0 * tan2Phi + e2c2 - 9.0 * t2e2c2 - 4.0 * e4c4);
        final double t12 = tanPhi
                / (720.0 * rho * nu5 * K06)
                * (61.0 + 90.0 * tan2Phi + 45.0 * tan4Phi + 46.0 * e2c2 - 252.0
                        * t2e2c2 - 90.0 * t4e2c2 - 3.0 * e4c4 - 66.0 * t2e4c4
                        + 225.0 * t4e4c4 + 100.0 * e6c6 + 84.0 * t2e6c6 + 88.0
                        * e8c8 - 192.0 * t2e8c8);
        final double t13 = tanPhi
                / (40320.0 * rho * nu7 * K08)
                * (1385.0 + 3633.0 * tan2Phi + 4095.0 * tan4Phi + 1575.0 * tan6Phi);
        final double t14 = 1.0 / (cosPhi * nu * K0);
        final double t15 = 1.0 / (6.0 * cosPhi * nu3 * K03)
                * (1.0 + 2.0 * tan2Phi + e2c2);
        final double t16 = 1.0
                / (120.0 * cosPhi * nu5 * K05)
                * (5.0 + 28.0 * tan2Phi + 24.0 * tan4Phi + 6.0 * e2c2 + 8.0
                        * t2e2c2 - 3.0 * e4c4 + 4.0 * t2e4c4 - 4.0 * e6c6 + 24.0 * t2e6c6);
        final double t17 = 1.0 / (5040.0 * cosPhi * nu7 * K07)
                * (61.0 + 662.0 * tan2Phi + 1320.0 * tan4Phi + 720.0 * tan6Phi);

        final double latitude = phi - dE2 * t10 + dE4 * t11 - dE6 * t12 + dE8
                * t13;
        final double longitude = lambda0 + dE * t14 - dE3 * t15 + dE5 * t16
                - dE7 * t17;
        return LatLong.valueOf(latitude, longitude, RADIAN);
    }

    /**
     * Converts the UPS coordinates to latitude/longitude coordinates,
     * based on the specified reference ellipsoid.
     *
     * @param ups   The UPS coordinates.
     * @param ellipsoid The reference ellipsoid.
     * @return  The latitude/longitude coordinates.
     */
    public static LatLong upsToLatLong(UTM ups, ReferenceEllipsoid ellipsoid) {
        final boolean northernHemisphere = ups.latitudeZone() > 'N';
        final double dN = ups.northingValue(METRE)
                - UPS_FALSE_NORTHING.doubleValue(METRE);
        final double dE = ups.eastingValue(METRE)
                - UPS_FALSE_EASTING.doubleValue(METRE);
        // check for zeroes (the poles)
        if (dE == 0.0 && dN == 0.0) {
            if (northernHemisphere) {
                return LatLong.valueOf(90.0, 0.0, DEGREE_ANGLE);
            } else {
                return LatLong.valueOf(-90.0, 0.0, DEGREE_ANGLE);
            }
        }
        // compute longitude
        final double longitude;
        if (northernHemisphere) {
            longitude = Math.atan2(dE, -dN);
        } else {
            longitude = Math.atan2(dE, dN);
        }

        // compute latitude
        final double a = ellipsoid.getSemimajorAxis().doubleValue(METRE);
        final double e = ellipsoid.getEccentricity();
        final double e2 = ellipsoid.getEccentricitySquared();
        final double e4 = e2 * e2;
        final double e6 = e4 * e2;
        final double e8 = e6 * e2;
        final double aBar = e2 / 2.0 + 5.0 * e4 / 24.0 + e6 / 12.0 + 13 * e8
                / 360.0;
        final double bBar = 7.0 * e4 / 48.0 + 29.0 * e6 / 240.0 + 811.0 * e8
                / 11520.0;
        final double cBar = 7.0 * e6 / 120.0 + 81.0 * e8 / 1120.0;
        final double dBar = 4279 * e8 / 161280.0;
        final double c0 = ((2.0 * a) / Math.sqrt(1.0 - e2))
                * Math.pow((1.0 - e) / (1.0 + e), e / 2.0);
        final double r;
        if (dE == 0.0) {
            r = dN;
        } else if (dN == 0.0) {
            r = dE;
        } else if (dN < dE) {
            r = dE / Math.sin(longitude);
        } else {
            r = dN / Math.cos(longitude);
        }
        final double radius = Math.abs(r);

        final double chi = (Math.PI / 2.0) - 2.0
                * Math.atan2(radius, UPS_SCALE_FACTOR * c0);
        final double phi = chi + aBar * Math.sin(2.0 * chi) + bBar
                * Math.sin(4.0 * chi) + cBar * Math.sin(6.0 * chi) + dBar
                * Math.sin(8.0 * chi);
        final double latitude;
        if (northernHemisphere) {
            latitude = phi;
        } else {
            latitude = -phi;
        }
        return LatLong.valueOf(latitude, longitude, RADIAN);
    }

    @Override
    public UTM copy() {
        return UTM.valueOf(_longitudeZone, _latitudeZone, _easting, _northing,
                METRE);
    }

    // Default serialization.
    //

    static final XMLFormat<UTM> XML = new XMLFormat<UTM>(UTM.class) {

        @Override
        public UTM newInstance(Class<UTM> cls, InputElement xml)
                throws XMLStreamException {
            return FACTORY.object();
        }

        public void write(UTM utm, OutputElement xml) throws XMLStreamException {
            xml.setAttribute("latitudeZone", utm._latitudeZone);
            xml.setAttribute("longitudeZone", utm._longitudeZone);
            xml.setAttribute("easting", utm._easting);
            xml.setAttribute("northing", utm._northing);
        }

        public void read(InputElement xml, UTM UTM) throws XMLStreamException {
            UTM._latitudeZone = xml.getAttribute("latitudeZone", ' ');
            UTM._longitudeZone = xml.getAttribute("longitudeZone", 0);
            UTM._easting = xml.getAttribute("easting", 0.0);
            UTM._northing = xml.getAttribute("northing", 0.0);
        }
    };

    private static final long serialVersionUID = 1L;

}
