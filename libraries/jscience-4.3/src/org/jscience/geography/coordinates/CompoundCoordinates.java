/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.geography.coordinates;

import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.geography.coordinates.crs.CompoundCRS;
import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem;

/**
 * This class represents a coordinates made up by combining 
 * two coordinates objects together.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public final class CompoundCoordinates<C1 extends Coordinates<?>, C2 extends Coordinates<?>>
        extends Coordinates<CompoundCRS<C1, C2>> {

    /**
     * Holds the first coordinates. 
     */
    private C1 _first;

    /**
     * Holds the next coordinates. 
     */
    private C2 _next;

    /**
     * Returns a compound coordinates made up of the specified coordinates.
     * 
     * @param first the first coordinates.
     * @param next the next coordinates. 
     */
    @SuppressWarnings("unchecked")
    public static <T1 extends Coordinates<?>, T2 extends Coordinates<?>> CompoundCoordinates<T1, T2> valueOf(
            T1 first, T2 next) {
        CompoundCoordinates coord = FACTORY.object();
        coord._first = first;
        coord._next = next;
        return coord;
    }

    @SuppressWarnings("unchecked")
    private static final ObjectFactory<CompoundCoordinates> FACTORY = new ObjectFactory<CompoundCoordinates>() {

        @Override
        protected CompoundCoordinates create() {
            return new CompoundCoordinates();
        }

    };

    private CompoundCoordinates() {
    }

    /**
     * Returns the first coordinates.
     * 
     * @return the first coordinates. 
     */
    public C1 getFirst() {
        return _first;
    }

    /**
     * Returns the next coordinates.
     * 
     * @return the next coordinates. 
     */
    public C2 getNext() {
        return _next;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompoundCRS<C1, C2> getCoordinateReferenceSystem() {
        return new CompoundCRS<C1, C2>((CoordinateReferenceSystem<C1>) _first.getCoordinateReferenceSystem(),
                (CoordinateReferenceSystem<C2>) _next.getCoordinateReferenceSystem());
    }

    // OpenGIS Interface.
    public int getDimension() {
        return _first.getDimension() + _next.getDimension();
    }

    // OpenGIS Interface.
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        final int firstDimension = _first.getDimension();
        if (dimension < firstDimension) {
            return _first.getOrdinate(dimension);
        } else {
            return _next.getOrdinate(dimension - firstDimension);
        }
    }

    @Override
    public CompoundCoordinates<?, ?> copy() {
        return CompoundCoordinates.valueOf(_first, _next);
    }

    // Default serialization.
    //

    @SuppressWarnings("unchecked")
    static final XMLFormat<CompoundCoordinates> XML = new XMLFormat<CompoundCoordinates>(
            CompoundCoordinates.class) {

        @Override
        public CompoundCoordinates newInstance(Class<CompoundCoordinates> cls,
                InputElement xml) throws XMLStreamException {
            return FACTORY.object();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void read(InputElement xml, CompoundCoordinates coord)
                throws XMLStreamException {
            coord._first = xml.getNext();
            coord._next = xml.getNext();
        }

        @Override
        public void write(CompoundCoordinates coord, OutputElement xml)
                throws XMLStreamException {
            xml.add(coord._first);
            xml.add(coord._next);
        }
    };

    private static final long serialVersionUID = 1L;

}