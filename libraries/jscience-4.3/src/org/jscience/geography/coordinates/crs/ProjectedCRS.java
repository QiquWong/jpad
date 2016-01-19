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
 * This class represents a 2-dimensional projected reference system.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public abstract class ProjectedCRS<C extends Coordinates<?>> extends CoordinateReferenceSystem<C> {

    /**
     * Holds the Easting/Northing coordinate system.
     */
    public static final CoordinateSystem EASTING_NORTHING_CS = new CoordinateSystem() {

        Axis eastingAxis = new Axis("Easting", "E", SI.METRE,
                AxisDirection.EAST);

        Axis northingAxis = new Axis("Northing", "N", SI.METRE,
                AxisDirection.NORTH);
        
        public int getDimension() {
            return 2;
        }

        public CoordinateSystemAxis getAxis(int dimension)
                throws IndexOutOfBoundsException {
            if (dimension == 0) {
                return eastingAxis;
            } else if (dimension == 1) {
                return northingAxis;
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