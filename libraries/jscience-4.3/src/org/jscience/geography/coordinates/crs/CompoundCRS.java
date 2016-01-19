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

import org.jscience.geography.coordinates.CompoundCoordinates;
import org.jscience.geography.coordinates.Coordinates;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.InternationalString;

/**
 * This class represents a coordinate reference system combining two or more 
 * distinct reference systems.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public class CompoundCRS<C1 extends Coordinates<?>, C2 extends Coordinates<?>>
        extends CoordinateReferenceSystem<CompoundCoordinates<C1, C2>> {
    
    final CoordinateReferenceSystem<C1> _first;
    
    final CoordinateReferenceSystem<C2> _next;

    final CoordinateSystem _coordinateSystem = new CoordinateSystem() {

        public int getDimension() {
            return _first.getCoordinateSystem().getDimension() + 
            _next.getCoordinateSystem().getDimension();
        }

        public CoordinateSystemAxis getAxis(int dimension) throws IndexOutOfBoundsException {
            int firstDimension = _first.getCoordinateSystem().getDimension();
            return (dimension < firstDimension) ? _first.getCoordinateSystem().getAxis(dimension) :
                _next.getCoordinateSystem().getAxis(dimension - firstDimension);
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
        }};


    public CompoundCRS(CoordinateReferenceSystem<C1> first, CoordinateReferenceSystem<C2> next) {
        _first = first;
        _next = next;        
    }

    @Override
    protected CompoundCoordinates<C1, C2> coordinatesOf(AbsolutePosition position) {
        C1 c1 = _first.coordinatesOf(position);
        C2 c2 = _next.coordinatesOf(position);
        return CompoundCoordinates.valueOf(c1, c2);
    }

    @Override
    protected AbsolutePosition positionOf(CompoundCoordinates<C1, C2> coordinates, AbsolutePosition position) {
        AbsolutePosition firstPosition = _first.positionOf(coordinates.getFirst(), position);
        return _next.positionOf(coordinates.getNext(), firstPosition);
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return _coordinateSystem;
    }
   
}