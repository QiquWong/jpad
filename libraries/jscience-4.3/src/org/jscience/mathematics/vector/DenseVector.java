/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.List;

import javolution.context.ObjectFactory;
import javolution.context.StackContext;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.mathematics.structure.Field;

/**
 * <p> This class represents a dense vector.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class DenseVector<F extends Field<F>> extends Vector<F> {

    /**
     * Holds the default XML representation for dense vectors. For example:
     * [code]
     *    <DenseVector dimension="2">
     *        <Rational value="1/3" />
     *        <Rational value="3/5" />
     *    </DenseVector>[/code]
     */
    protected static final XMLFormat<DenseVector> XML = new XMLFormat<DenseVector>(
            DenseVector.class) {

        @Override
        public DenseVector newInstance(Class<DenseVector> cls, InputElement xml)
                throws XMLStreamException {
            return FACTORY.object();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void read(InputElement xml, DenseVector V)
                throws XMLStreamException {
            int dimension = xml.getAttribute("dimension", 0);
            for (int i=0; i < dimension; i++) {
                V._elements.add(xml.getNext());
            }
            if (xml.hasNext()) 
                throw new XMLStreamException("Too many elements");
        }

        @Override
        public void write(DenseVector V, OutputElement xml)
                throws XMLStreamException {
            int dimension = V._elements.size();
            xml.setAttribute("dimension", dimension);
            for (int i = 0; i < dimension;) {
                xml.add(V._elements.get(i++));
            }
        }
    };

    /**
     * Holds the elements.
     */
    final FastTable<F> _elements = new FastTable<F>();

    /**
     * Returns a dense vector holding the specified elements.
     *
     * @param elements the vector elements.
     * @return the vector having the specified elements.
     */
    public static <F extends Field<F>> DenseVector<F> valueOf(F... elements) {
        DenseVector<F> V = DenseVector.newInstance();
        for (int i=0, n=elements.length; i < n;) {
            V._elements.add(elements[i++]);
        }
        return V;
    }

    /**
     * Returns a dense vector holding the elements from the specified 
     * collection.
     *
     * @param elements the collection of vector elements.
     * @return the vector having the specified elements.
     */
    public static <F extends Field<F>> DenseVector<F> valueOf(List<F> elements) {
        DenseVector<F> V = DenseVector.newInstance();
        V._elements.addAll(elements);
        return V;
    }

    /**
     * Returns a dense vector equivalent to the specified vector.
     *
     * @param that the vector to convert.
     * @return <code>that</code> or a dense vector holding the same elements
     *         as the specified vector.
     */
    public static <F extends Field<F>> DenseVector<F> valueOf(Vector<F> that) {
        if (that instanceof DenseVector) return (DenseVector<F>) that;
        DenseVector<F> V = DenseVector.newInstance();
        for (int i=0, n=that.getDimension(); i < n;) {
            V. _elements.add(that.get(i++));
         }
         return V;
    }
    
    @Override
    public int getDimension() {
        return _elements.size();
    }

    @Override
    public F get(int i) {
        return _elements.get(i);
    }

    @Override
    public DenseVector<F> opposite() {
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0, n = _elements.size(); i < n;) {
            V._elements.add(_elements.get(i++).opposite());
        }
        return V;
    }

    @Override
    public DenseVector<F> plus(Vector<F> that) {
        final int n = _elements.size();
        if (that.getDimension() != n)
            throw new DimensionException();
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0; i < n; i++) {
            V._elements.add(_elements.get(i).plus(that.get(i)));
        }
        return V;
    }

    @Override
    public DenseVector<F> minus(Vector<F> that) { // Returns more specialized type.
        return this.plus(that.opposite());
    }
    
    @Override
    public DenseVector<F> times(F k) {
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0, n = _elements.size(); i < n;) {
            V._elements.add(_elements.get(i++).times(k));
        }
        return V;
    }

    @Override
    public F times(Vector<F> that) {
        final int n = _elements.size();
        if (that.getDimension() != n)
            throw new DimensionException();
        StackContext.enter();
        try { // Reduces memory allocation / garbage collection.
            F sum = _elements.get(0).times(that.get(0));
            for (int i = 1; i < n; i++) {
                sum = sum.plus(_elements.get(i).times(that.get(i)));
            }
            return StackContext.outerCopy(sum);
        } finally {
            StackContext.exit();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DenseVector<F> copy() {
        DenseVector<F> V = DenseVector.newInstance();
        for (F e : _elements) {
            V._elements.add((F)e.copy());            
        }        
        return V;
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////

    @SuppressWarnings("unchecked")
    static <F extends Field<F>> DenseVector<F> newInstance() {
        return FACTORY.object();
    }

    private static final ObjectFactory<DenseVector> FACTORY = new ObjectFactory<DenseVector>() {
        @Override
        protected DenseVector create() {
            return new DenseVector();
        }

        @Override
        protected void cleanup(DenseVector vector) {
            vector._elements.reset();
        }
    };

    private DenseVector() {
    }

    private static final long serialVersionUID = 1L;

 
}