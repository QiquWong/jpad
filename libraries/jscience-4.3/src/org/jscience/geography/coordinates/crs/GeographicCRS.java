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

import javax.measure.unit.NonSI;

import org.jscience.geography.coordinates.Coordinates;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.InternationalString;

/**
 * This class represents a 2 dimensional surface reference system
 * based on an ellipsoidal approximation of a geoid.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public abstract class GeographicCRS<C extends Coordinates<?>> extends CoordinateReferenceSystem<C> {

    /**
     * Holds the Geodetic Latitude/Longitude coordinate system.
     */
    public static final CoordinateSystem LATITUDE_LONGITUDE_CS = new CoordinateSystem() {

        Axis latitudeAxis = new Axis("Geodetic Latitude", "Lat", NonSI.DEGREE_ANGLE,
                AxisDirection.NORTH);

        Axis longitudeAxis = new Axis("Geodetic Longitude", "Long", NonSI.DEGREE_ANGLE,
                AxisDirection.EAST);
        
        public int getDimension() {
            return 2;
        }

        public CoordinateSystemAxis getAxis(int dimension)
                throws IndexOutOfBoundsException {
            if (dimension == 0) {
                return latitudeAxis;
            } else if (dimension == 1) {
                return longitudeAxis;
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