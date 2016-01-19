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

import javax.measure.unit.SI;

import org.jscience.geography.coordinates.Coordinates;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.InternationalString;

/**
 * This class represents a 3 dimensional spatial reference system.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public abstract class GeocentricCRS<C extends Coordinates<?>> extends
        CoordinateReferenceSystem<C> {

    /**
     * Holds the XYZ coordinate system.
     */
    public static final CoordinateSystem XYZ_CS = new CoordinateSystem() {

        Axis xAxis = new Axis("Geocentric X", "X", SI.METRE,
                AxisDirection.GEOCENTRIC_X);

        Axis yAxis = new Axis("Geocentric Y", "Y", SI.METRE,
                AxisDirection.GEOCENTRIC_Y);

        Axis zAxis = new Axis("Geocentric Z", "Z", SI.METRE,
                AxisDirection.GEOCENTRIC_Z);

        public int getDimension() {
            return 3;
        }

        public CoordinateSystemAxis getAxis(int dimension)
                throws IndexOutOfBoundsException {
            if (dimension == 0) {
                return xAxis;
            } else if (dimension == 1) {
                return yAxis;
            } else if (dimension == 2) {
                return zAxis;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public Identifier getName() {
            throw new UnsupportedOperationException();
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
    };


}