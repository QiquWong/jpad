/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.Iterator;
import java.util.List;
import javolution.context.ArrayFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.structure.VectorSpaceNormed;

/**
 * <p> This class represents an optimized {@link Vector vector} implementation
 *     for {@link Complex complex} numbers elements.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class ComplexVector extends Vector<Complex> implements
        VectorSpaceNormed<Vector<Complex>, Complex> {

    /**
     * Holds the default XML representation. For example:
     * [code]
     *    <ComplexVector dimension="2">
     *        <Complex real="1.0" imaginary="-3.0" />
     *        <Complex real="0.0" imaginary="2.0" />
     *    </ComplexVector>[/code]
     */
    protected static final XMLFormat<ComplexVector> XML = new XMLFormat<ComplexVector>(
            ComplexVector.class) {

        @Override
        public ComplexVector newInstance(Class<ComplexVector> cls,
                InputElement xml) throws XMLStreamException {
            int dimension = xml.getAttribute("dimension", 0);
            ComplexVector V = FACTORY.array(dimension);
            V._dimension = dimension;
            return V;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void read(InputElement xml, ComplexVector V)
                throws XMLStreamException {
            for (int i = 0, n = V._dimension; i < n;) {
                V._reals[i++] = ((Complex) xml.getNext()).doubleValue();
            }
            if (xml.hasNext())
                throw new XMLStreamException("Too many elements");
        }

        @Override
        public void write(ComplexVector V, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("dimension", V._dimension);
            for (int i = 0, n = V._dimension; i < n;) {
                xml.add(V.get(i++));
            }
        }
    };

    /**
     * Holds factory for vectors with variable size arrays.
     */
    private static final ArrayFactory<ComplexVector> FACTORY 
         = new ArrayFactory<ComplexVector>() {

        @Override
        protected ComplexVector create(int capacity) {
            return new ComplexVector(capacity);
        }

    };
    /**
     * Holds the dimension.
     */
    private int _dimension;

    /**
     * Holds the real values.
     */
    private final double[] _reals;

    /**
     * Holds the imaginary values.
     */
    private double[] _imags;

    /**
     * Creates a vector of specified capacity.
     */
    private ComplexVector(int capacity) {
        _reals = new double[capacity];
        _imags = new double[capacity];
    }    
    
    /**
     * Returns a new vector holding the specified complex numbers.
     *
     * @param elements the complex numbers elements.
     * @return the vector having the specified complex numbers.
     */
    public static ComplexVector valueOf(Complex... elements) {
        int n = elements.length;
        ComplexVector V = FACTORY.array(n);
        V._dimension = n;
        for (int i = 0; i < n; i++) {
            Complex complex = elements[i];
            V._reals[i] = complex.getReal();
            V._imags[i] = complex.getImaginary();
        }
        return V;
    }

    /**
     * Returns a new vector holding the elements from the specified 
     * collection.
     *
     * @param elements the collection of floating-points numbers.
     * @return the vector having the specified elements.
     */
    public static ComplexVector valueOf(List<Complex> elements) {
        int n = elements.size();
        ComplexVector V = FACTORY.array(n);
        V._dimension = n;
        Iterator<Complex> iterator = elements.iterator();
        for (int i = 0; i < n; i++) {
            Complex complex = iterator.next();
            V._reals[i] = complex.getReal();
            V._imags[i] = complex.getImaginary();
        }
        return V;
    }

    /**
     * Returns a {@link ComplexVector} instance equivalent to the 
     * specified vector.
     *
     * @param that the vector to convert. 
     * @return <code>that</code> or new equivalent ComplexVector.
     */
    public static ComplexVector valueOf(Vector<Complex> that) {
        if (that instanceof ComplexVector)
            return (ComplexVector) that;
        int n = that.getDimension();
        ComplexVector V = FACTORY.array(n);
        V._dimension = n;
        for (int i = 0; i < n; i++) {
            Complex complex = that.get(i);
            V._reals[i] = complex.getReal();
            V._imags[i] = complex.getImaginary();
        }
        return V;
    }

    /**
     * Returns the real value of a complex number from this vector (fast).
     *
     * @param  i the complex number index.
     * @return the real value of complex at <code>i</code>.
     * @throws IndexOutOfBoundsException <code>(i < 0) || (i >= dimension())</code>
     */
    public double getReal(int i) {
        if (i >= _dimension)
            throw new ArrayIndexOutOfBoundsException();
        return _reals[i];
    }

    /**
     * Returns the imaginary value of a complex number from this vector (fast).
     *
     * @param  i the complex number index.
     * @return the real value of complex at <code>i</code>.
     * @throws IndexOutOfBoundsException <code>(i < 0) || (i >= dimension())</code>
     */
    public double getImaginary(int i) {
        if (i >= _dimension)
            throw new ArrayIndexOutOfBoundsException();
        return _imags[i];
    }

    /**
     * Returns the Euclidian norm of this vector (square root of the 
     * dot product of this vector and itself).
     *
     * @return <code>sqrt(this · this)</code>.
     */
    public Complex norm() {
        double normSquaredReal = 0;
        double normSquaredImag = 0;
        for (int i = _dimension; --i >= 0;) {
            double real = _reals[i];
            double imag = _imags[i];
            normSquaredReal += real * real - imag * imag;
            normSquaredImag += real * imag * 2.0;
        }
        return Complex.valueOf(normSquaredReal, normSquaredImag).sqrt();
    }

    @Override
    public int getDimension() {
        return _dimension;
    }

    @Override
    public Complex get(int i) {
        if (i >= _dimension)
            throw new IndexOutOfBoundsException();
        return Complex.valueOf(_reals[i], _imags[i]);
    }

    @Override
    public ComplexVector opposite() {
        ComplexVector V = FACTORY.array(_dimension);
        V._dimension = _dimension;
        for (int i = 0; i < _dimension; i++) {
            V._reals[i] = -_reals[i];
            V._imags[i] = -_imags[i];
        }
        return V;
    }

    @Override
    public ComplexVector plus(Vector<Complex> that) {
        ComplexVector T = ComplexVector.valueOf(that);
        if (T._dimension != _dimension)
            throw new DimensionException();
        ComplexVector V = FACTORY.array(_dimension);
        V._dimension = _dimension;
        for (int i = 0; i < _dimension; i++) {
            V._reals[i] = _reals[i] + T._reals[i];
            V._imags[i] = _imags[i] + T._imags[i];
        }
        return V;
    }

    @Override
    public ComplexVector minus(Vector<Complex> that) {
        ComplexVector T = ComplexVector.valueOf(that);
        if (T._dimension != _dimension)
            throw new DimensionException();
        ComplexVector V = FACTORY.array(_dimension);
        V._dimension = _dimension;
        for (int i = 0; i < _dimension; i++) {
            V._reals[i] = _reals[i] - T._reals[i];
            V._imags[i] = _imags[i] - T._imags[i];
        }
        return V;
    }

    @Override
    public ComplexVector times(Complex k) {
        ComplexVector V = FACTORY.array(_dimension);
        V._dimension = _dimension;
        for (int i = 0; i < _dimension; i++) {
            double real = _reals[i];
            double imag = _imags[i];
            V._reals[i] = real * k.getReal() - imag * k.getImaginary();
            V._imags[i] = real * k.getImaginary() + imag * k.getReal();
        }
        return V;
    }

    @Override
    public Complex times(Vector<Complex> that) {
        ComplexVector T = ComplexVector.valueOf(that);
        if (T._dimension != _dimension)
            throw new DimensionException();
        double sumReal = _reals[0] * T._reals[0] - _imags[0] * T._imags[0];
        double sumImag = _reals[0] * T._imags[0] + _imags[0] * T._reals[0];
        for (int i = 1; i < _dimension; i++) {
            sumReal += _reals[i] * T._reals[i] - _imags[i] * T._imags[i];
            sumImag += _reals[i] * T._imags[i] + _imags[i] * T._reals[i];
        }
        return Complex.valueOf(sumReal, sumImag);
    }

    @Override
    public ComplexVector copy() {
        ComplexVector V = FACTORY.array(_dimension);
        V._dimension = _dimension;
        for (int i = 0; i < _dimension; i++) {
            V._reals[i] = _reals[i];
            V._imags[i] = _imags[i];
        }
        return V;
    }
    ///////////////////////////////
    // Package Private Utilities //
    ///////////////////////////////
    
    void set(int i, Complex c) {
         _reals[i] = c.getReal();
         _imags[i] = c.getImaginary();
    }    

    static ComplexVector newInstance(int n) {
        ComplexVector V = FACTORY.array(n);
        V._dimension = n;
        return V;
    }
    
    private static final long serialVersionUID = 1L;

}