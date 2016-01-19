/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates.crs;

import javax.measure.Measure;
import javax.measure.Measurable;
import javax.measure.quantity.*;
import javax.measure.unit.SI;

/**
 * <p> The ReferenceEllipsoid class defines a geodetic reference ellipsoid
 *     used as a standard for geodetic measurements. The World Geodetic System
 *     1984 (WGS84) ellipsoid is the current standard for most geographic and
 *     geodetic coordinate systems, including GPS. The WGS84 ellipsoid is
 *     provided as a static instance of this class.</p>
 *
 * <p> The ellipsoid (actually an oblate spheroid) is uniquely specified by
 *     two parameters, the semimajor (or equatorial) radius and the ellipticity
 *     or flattening. In practice, the reciprocal of the flattening is
 *     specified.</p>
 *
 * <p> The ellipsoid is an approximation of the shape of the earth. Although
 *     not exact, the ellipsoid is much more accurate than a spherical
 *     approximation and is still mathematically simple. The <i>geoid</i> is
 *     a still closer approximation of the shape of the earth (intended to
 *     represent the mean sea level), and is generally specified by it's
 *     deviation from the ellipsoid.</p>
 *
 * <p> Different reference ellipsoids give more or less accurate results at
 *     different locations, so it was previously common for different nations
 *     to use ellipsoids that were more accurate for their areas. More recent
 *     efforts have provided ellipsoids with better overall global accuracy,
 *     such as the WGS84 ellipsiod, and these have now largely supplanted
 *     the others.</p>
 *
 * @author Paul D. Anderson
 * @version 3.0, February 18, 2006
 */
public class ReferenceEllipsoid {

    /**
     * The World Geodetic System 1984 reference ellipsoid.
     */
    public static final ReferenceEllipsoid WGS84
        = new ReferenceEllipsoid(6378137.0, 298.257223563);
    /**
     * Geodetic Reference System 1980 ellipsoid.
     */
    public static final ReferenceEllipsoid GRS80
        = new ReferenceEllipsoid(6378137.0, 298.257222101);
    /**
     * The World Geodetic System 1972 reference ellipsoid.
     */
    public static final ReferenceEllipsoid WGS72
        = new ReferenceEllipsoid(6378135.0, 298.26);
    /**
     * The International 1924 reference ellipsoid, one of the earliest
     * "global" ellipsoids.
     */
    public static final ReferenceEllipsoid INTERNATIONAL1924
        = new ReferenceEllipsoid(6378388.0, 297.0);

    private final double a;

    private final double b;

    private final double f;

    private final double ea2;

    private final double e;

    private final double eb2;

    private Measurable<Length> _semimajorAxis;

    private Measurable<Length> _semiminorAxis;

    /**
     *  Constructs an instance of a reference ellipsoid.
     *
     * @param semimajorAxis The semimajor or equatorial radius of this
     * reference ellipsoid, in meters.
     * @param inverseFlattening The reciprocal of the ellipticity or flattening
     * of this reference ellipsoid (dimensionless).
     */
    public ReferenceEllipsoid(double semimajorAxis, double inverseFlattening) {
        this.a = semimajorAxis;
        f = 1.0 / inverseFlattening;
        b = semimajorAxis * (1.0 - f);
        ea2 = f * (2.0 - f);
        e = Math.sqrt(ea2);
        eb2 = ea2 / (1.0 - ea2);
    }

    private static double sqr(final double x) {
        return x * x;
    }

    /**
     * Returns the semimajor or equatorial radius of this reference ellipsoid.
     *
     * @return The semimajor radius.
     */
    public Measurable<Length> getSemimajorAxis() {
        if (_semimajorAxis == null) {
            _semimajorAxis = Measure.valueOf(a, SI.METRE);
        }
        return _semimajorAxis;
    }

    /**
     * Returns the semiminor or polar radius of this reference ellipsoid.
     *
     * @return  The semiminor radius.
     */
    public Measurable<Length> getsSemiminorAxis() {
        if (_semiminorAxis == null) {
            _semiminorAxis = Measure.valueOf(b, SI.METRE);
        }
        return _semiminorAxis;
    }

    /**
     * Returns the flattening or ellipticity of this reference ellipsoid.
     *
     * @return The flattening.
     */
    public double getFlattening() {
        return f;
    }

    /**
     * Returns the (first) eccentricity of this reference ellipsoid.
     *
     * @return The eccentricity.
     */
    public double getEccentricity() {
        return e;
    }

    /**
     * Returns the square of the (first) eccentricity. This number is frequently
     * used in ellipsoidal calculations.
     *
     * @return The square of the eccentricity.
     */
    public double getEccentricitySquared() {
        return ea2;
    }

    /**
     * Returns the square of the second eccentricity of this reference ellipsoid.
     * This number is frequently used in ellipsoidal calculations.
     *
     * @return The square of the second eccentricity.
     */
    public double getSecondEccentricitySquared() {
        return eb2;
    }

    /**
      * Returns the <i>radius of curvature in the prime vertical</i>
      * for this reference ellipsoid at the specified latitude.
      *
      * @param phi The local latitude (radians).
      * @return The radius of curvature in the prime vertical (meters).
      */
     public double verticalRadiusOfCurvature(final double phi) {
         return a / Math.sqrt(1.0 - (ea2 * sqr(Math.sin(phi))));
     }

    /**
      * Returns the <i>radius of curvature in the prime vertical</i>
      * for this reference ellipsoid at the specified latitude.
      *
      * @param latitude The local latitude.
      * @return The radius of curvature in the prime vertical.
      */
     public Measurable<Length> verticalRadiusOfCurvature(final Measurable<Angle> latitude) {
         return Measure.valueOf(verticalRadiusOfCurvature(latitude.doubleValue(SI.RADIAN)), SI.METRE);
     }

    /**
     *  Returns the <i>radius of curvature in the meridian<i>
     *  for this reference ellipsoid at the specified latitude.
     *
     * @param phi The local latitude (in radians).
     * @return  The radius of curvature in the meridian (in meters).
     */
    public double meridionalRadiusOfCurvature(final double phi) {
        return verticalRadiusOfCurvature(phi)
               / (1.0 + eb2 * sqr(Math.cos(phi)));
    }

    /**
     *  Returns the <i>radius of curvature in the meridian<i>
     *  for this reference ellipsoid at the specified latitude.
     *
     * @param latitude The local latitude (in radians).
     * @return  The radius of curvature in the meridian (in meters).
     */
    public Measurable<Length> meridionalRadiusOfCurvature(final Measurable<Angle> latitude) {
        return Measure.valueOf(meridionalRadiusOfCurvature(latitude.doubleValue(SI.RADIAN)), SI.METRE);
    }

    /**
     *  Returns the meridional arc, the true meridional distance on the
     * ellipsoid from the equator to the specified latitude, in meters.
     *
     * @param phi   The local latitude (in radians).
     * @return  The meridional arc (in meters).
     */
    public double meridionalArc(final double phi) {
        final double sin2Phi = Math.sin(2.0 * phi);
        final double sin4Phi = Math.sin(4.0 * phi);
        final double sin6Phi = Math.sin(6.0 * phi);
        final double sin8Phi = Math.sin(8.0 * phi);
        final double n = f / (2.0 - f);
        final double n2 = n * n;
        final double n3 = n2 * n;
        final double n4 = n3 * n;
        final double n5 = n4 * n;
        final double n1n2 = n - n2;
        final double n2n3 = n2 - n3;
        final double n3n4 = n3 - n4;
        final double n4n5 = n4 - n5;
        final double ap = a * (1.0 - n + (5.0 / 4.0) * (n2n3) + (81.0 / 64.0) * (n4n5));
        final double bp = (3.0 / 2.0) * a * (n1n2 + (7.0 / 8.0) * (n3n4) + (55.0 / 64.0) * n5);
        final double cp = (15.0 / 16.0) * a * (n2n3 + (3.0 / 4.0) * (n4n5));
        final double dp = (35.0 / 48.0) * a * (n3n4 + (11.0 / 16.0) * n5);
        final double ep = (315.0 / 512.0) * a * (n4n5);
        return ap * phi - bp * sin2Phi + cp * sin4Phi - dp * sin6Phi + ep * sin8Phi;
    }

    /**
     *  Returns the meridional arc, the true meridional distance on the
     * ellipsoid from the equator to the specified latitude.
     *
     * @param latitude   The local latitude.
     * @return  The meridional arc.
     */
    public Measurable<Length> meridionalArc(final Measurable<Angle> latitude) {
        return Measure.valueOf(meridionalArc(latitude.doubleValue(SI.RADIAN)), SI.METRE);
    }

}