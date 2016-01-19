/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2007 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.Map;

import javolution.context.ObjectFactory;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.Index;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.mathematics.structure.Field;

/**
 * <p> This class represents a sparse vector.</p>
 * <p> Sparse vectors can be created using an index-to-element mapping or 
 *     by adding single elements sparse vectors together.</p>
 *         
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class SparseVector<F extends Field<F>> extends Vector<F> {

    /**
     * Holds the default XML representation for sparse vectors.
     * For example:[code]
     *    <SparseVector dimension="16">
     *        <Zero class="Complex" real="0.0" imaginary="0.0" />
     *        <Elements>
     *            <Index value="4" />
     *            <Complex real="1.0" imaginary="0.0" />
     *            <Index value="6" />
     *            <Complex real="0.0" imaginary="1.0" />
     *        </Elements>
     *    </SparseVector>[/code]
     */
    protected static final XMLFormat<SparseVector> XML = new XMLFormat<SparseVector>(
            SparseVector.class) {

        @Override
        public SparseVector newInstance(Class<SparseVector> cls, InputElement xml)
                throws XMLStreamException {
            return FACTORY.object();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void read(InputElement xml, SparseVector V)
                throws XMLStreamException {
            V._dimension = xml.getAttribute("dimension", 0);
            V._zero = xml.get("Zero");
            V._elements.putAll(xml.get("Elements", FastMap.class));
        }

        @Override
        public void write(SparseVector V, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("dimension", V._dimension);
            xml.add(V._zero, "Zero");
            xml.add(V._elements, "Elements", FastMap.class);
        }
    };
    
    /**
     * Holds this vector dimension.
     */
    int _dimension;

    /**
     * Holds zero.
     */
    F _zero;

    /**
     * Holds the index to element mapping.
     */
    final FastMap<Index, F> _elements = new FastMap<Index, F>();

    /**
     * Returns a sparse vector having a single element at the specified index.
     *
     * @param dimension this vector dimension.
     * @param zero the element representing zero.
     * @param i the index value of this vector single element.
     * @param element the element at the specified index.
     * @return the corresponding vector.
     */
    public static <F extends Field<F>> SparseVector<F> valueOf(int dimension,
            F zero, int i, F element) {
        SparseVector<F> V = SparseVector.newInstance(dimension, zero);
        V._elements.put(Index.valueOf(i), element);
        return V;
    }

    /**
     * Returns a sparse vector from the specified index to element mapping.
     *
     * @param dimension this vector dimension.
     * @param zero the element representing zero.
     * @param elements the index to element mapping.
     * @return the corresponding vector.
     */
    public static <F extends Field<F>> SparseVector<F> valueOf(int dimension,
            F zero, Map<Index, F> elements) {
        SparseVector<F> V = SparseVector.newInstance(dimension, zero);
        V._elements.putAll(elements);
        return V;
    }

    /**
     * Returns a sparse vector equivalent to the specified vector but with 
     * the zero elements removed removed using a default object equality 
     * comparator.
     * 
     * @param that the vector to convert.
     * @param zero the zero element for the sparse vector to return.
     * @return <code>SparseVector.valueOf(that, zero, FastComparator.DEFAULT)</code>
     */
    public static <F extends Field<F>> SparseVector<F> valueOf(
            Vector<F> that, F zero) {
        return SparseVector.valueOf(that, zero, FastComparator.DEFAULT);
    }

    /**
     * Returns a sparse vector equivalent to the specified vector but with 
     * the zero elements removed using the specified object equality comparator.
     * This method can be used to clean up sparse vectors (to remove elements
     * close to zero).
     * 
     * @param that the vector to convert.
     * @param zero the zero element for the sparse vector to return.
     * @param comparator the comparator used to determinate zero equality. 
     * @return a sparse vector with zero elements removed.
     */
    public static <F extends Field<F>> SparseVector<F> valueOf(
            Vector<F> that, F zero, FastComparator<? super F> comparator) {
        if (that instanceof SparseVector) 
            return SparseVector.valueOf((SparseVector<F>) that, zero, comparator);
        int n = that.getDimension();
        SparseVector<F> V = SparseVector.newInstance(n, zero);
        for (int i=0; i < n; i++) {
            F element = that.get(i);
            if (!comparator.areEqual(zero, element)) {
                V._elements.put(Index.valueOf(i), element);
            }
        }
        return V;
    }
    private static <F extends Field<F>> SparseVector<F> valueOf(
            SparseVector<F> that, F zero, FastComparator<? super F> comparator) {
        SparseVector<F> V = SparseVector.newInstance(that._dimension, zero);
        for (FastMap.Entry<Index, F> e = that._elements.head(), n = that._elements.tail(); (e = e
                .getNext()) != n;) {
            if (!comparator.areEqual(e.getValue(), zero)) {
                V._elements.put(e.getKey(), e.getValue());
            }
        }
        return V;
    } 

    /**
     * Returns the value of the non-set elements for this sparse vector.
     * 
     * @return the element corresponding to zero.
     */
    public F getZero() {
        return _zero;
    }

    @Override
    public int getDimension() {
        return _dimension;
    }

    @Override
    public F get(int i) {
        if ((i < 0) || (i >= _dimension))
            throw new IndexOutOfBoundsException();
        F element = _elements.get(Index.valueOf(i));
        return (element == null) ? _zero : element;
    }

    @Override
    public SparseVector<F> opposite() {
        SparseVector<F> V = SparseVector.newInstance(_dimension, _zero);
        for (FastMap.Entry<Index, F> e = _elements.head(), n = _elements.tail(); (e = e
                .getNext()) != n;) {
            V._elements.put(e.getKey(), e.getValue().opposite());
        }
        return V;
    }

    @Override
    public SparseVector<F> plus(Vector<F> that) {
        if (that instanceof SparseVector) 
            return plus((SparseVector<F>) that);
        return plus(SparseVector.valueOf(that, _zero, FastComparator.DEFAULT));
    }

    private SparseVector<F> plus(SparseVector<F> that) {
        if (this._dimension != that._dimension) throw new DimensionException();
        SparseVector<F> V = SparseVector.newInstance(_dimension, _zero);
        V._elements.putAll(this._elements);
        for (FastMap.Entry<Index, F> e = that._elements.head(), n = that._elements.tail();
                (e = e.getNext()) != n;) {
            Index index = e.getKey();
            FastMap.Entry<Index, F> entry = V._elements.getEntry(index);
            if (entry == null) {
                V._elements.put(index, e.getValue());
            } else {
                entry.setValue(entry.getValue().plus(e.getValue()));
            }
        }
        return V;
    }

    @Override
    public SparseVector<F> times(F k) {
        SparseVector<F> V = SparseVector.newInstance(_dimension, _zero);
        for (FastMap.Entry<Index, F> e = _elements.head(), n = _elements.tail(); (e = e
                .getNext()) != n;) {
            V._elements.put(e.getKey(), e.getValue().times(k));
        }
        return V;
    }

    @Override
    public F times(Vector<F> that) {
        if (that.getDimension() != _dimension)
            throw new DimensionException();
        F sum = null;
        for (FastMap.Entry<Index, F> e = _elements.head(), n = _elements.tail(); (e = e
                .getNext()) != n;) {
            F f = e.getValue().times(that.get(e.getKey().intValue()));
            sum = (sum == null) ? f : sum.plus(f);
        }
        return (sum != null) ? sum : _zero;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SparseVector<F> copy() {
        SparseVector<F> V = SparseVector.newInstance(_dimension, (F)_zero.copy());
        for (Map.Entry<Index, F> e : _elements.entrySet()) {
            V._elements.put(e.getKey(), (F) e.getValue().copy());            
        }
        return V;
    }
    
    ///////////////////////
    // Factory creation. //
    ///////////////////////

    @SuppressWarnings("unchecked")
    static <F extends Field<F>> SparseVector<F> newInstance(int dimension, F zero) {
        SparseVector<F> V = FACTORY.object();
        V._dimension = dimension;
        V._zero = zero;
        return V;
    }

    private static final ObjectFactory<SparseVector> FACTORY = new ObjectFactory<SparseVector>() {
        @Override
        protected SparseVector create() {
            return new SparseVector();
        }

        @Override
        protected void cleanup(SparseVector vector) {
            vector._elements.reset();
        }
    };

    private SparseVector() {   
    }
    
    private static final long serialVersionUID = 1L;

}