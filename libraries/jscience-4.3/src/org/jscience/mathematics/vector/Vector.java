/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.Comparator;
import javolution.lang.Realtime;
import javolution.lang.ValueType;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.util.FastTable;

import org.jscience.mathematics.structure.Field;
import org.jscience.mathematics.structure.VectorSpace;

/**
 * <p> This class represents an immutable element of a vector space.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 * @see <a href="http://en.wikipedia.org/wiki/Vector_space">
 *      Wikipedia: Vector Space</a>
 */
public abstract class Vector<F extends Field<F>> 
        implements VectorSpace<Vector<F>, F>, ValueType, Realtime {

    /**
     * Default constructor (for sub-classes).
     */
    protected Vector() {
    }

    /**
     * Returns the number of elements  held by this vector.
     *
     * @return this vector dimension.
     */
    public abstract int getDimension();

    /**
     * Returns a single element from this vector.
     *
     * @param  i the element index (range [0..n[).
     * @return the element at <code>i</code>.
     * @throws IndexOutOfBoundsException <code>(i < 0) || (i >= size())</code>
     */
    public abstract F get(int i);

    /**
     * Returns the negation of this vector.
     *
     * @return <code>-this</code>.
     */
    public abstract Vector<F> opposite();

    /**
     * Returns the sum of this vector with the one specified.
     *
     * @param   that the vector to be added.
     * @return  <code>this + that</code>.
     * @throws  DimensionException is vectors dimensions are different.
     */
    public abstract Vector<F> plus(Vector<F> that);

    /**
     * Returns the difference between this vector and the one specified.
     *
     * @param  that the vector to be subtracted.
     * @return <code>this - that</code>.
     */
    public Vector<F> minus(Vector<F> that) {
        return this.plus(that.opposite());
    }

    /**
     * Returns the product of this vector with the specified coefficient.
     *
     * @param  k the coefficient multiplier.
     * @return <code>this · k</code>
     */
    public abstract Vector<F> times(F k);
    
    /**
     * Returns the dot product of this vector with the one specified.
     *
     * @param  that the vector multiplier.
     * @return <code>this · that</code>
     * @throws DimensionException if <code>this.dimension() != that.dimension()</code>
     * @see <a href="http://en.wikipedia.org/wiki/Dot_product">
     *      Wikipedia: Dot Product</a>
     */
    public abstract F times(Vector<F> that);
    
    /**
     * Returns the cross product of two 3-dimensional vectors.
     *
     * @param  that the vector multiplier.
     * @return <code>this x that</code>
     * @throws DimensionException if 
     *         <code>(this.getDimension() != 3) && (that.getDimension() != 3)</code> 
     */
    public Vector<F> cross(Vector<F> that) {
        if ((this.getDimension() != 3) || (that.getDimension() != 3))
            throw new DimensionException(
                    "The cross product of two vectors requires "
                            + "3-dimensional vectors");
        FastTable<F> elements = FastTable.newInstance();
        elements.add((this.get(1).times(that.get(2))).plus((this.get(2).times(that
                .get(1))).opposite()));
        elements.add((this.get(2).times(that.get(0))).plus((this.get(0).times(that
                .get(2))).opposite()));
        elements.add((this.get(0).times(that.get(1))).plus((this.get(1).times(that
                .get(0))).opposite()));
        DenseVector<F> V = DenseVector.valueOf(elements);
        FastTable.recycle(elements);
        return V;
    }

    /**
     * Returns the text representation of this vector.
     *
     * @return the text representation of this vector.
     */
    public Text toText() {
        final int dimension = this.getDimension();
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append('{');
        for (int i = 0; i < dimension; i++) {
            tmp.append(get(i));
            if (i != dimension - 1) {
                tmp.append(", ");
            }
        }
        tmp.append('}');
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp); 
        return txt;
    }

    /**
     * Returns the text representation of this vector as a 
     * <code>java.lang.String</code>.
     * 
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

    /**
     * Indicates if this vector can be considered equals to the one 
     * specified using the specified comparator when testing for 
     * element equality. The specified comparator may allow for some 
     * tolerance in the difference between the vector elements.
     *
     * @param  that the vector to compare for equality.
     * @param  cmp the comparator to use when testing for element equality.
     * @return <code>true</code> if this vector and the specified matrix are
     *         both vector with equal elements according to the specified
     *         comparator; <code>false</code> otherwise.
     */
    public boolean equals(Vector<F> that, Comparator<F> cmp) {
        if (this == that)
            return true;
        final int dimension = this.getDimension();
        if (that.getDimension() != dimension)
            return false;
        for (int i = dimension; --i >= 0;) {
            if (cmp.compare(this.get(i), that.get(i)) != 0)
                return false;
        }
        return true;
    }

    /**
     * Indicates if this vector is equal to the object specified.
     *
     * @param  that the object to compare for equality.
     * @return <code>true</code> if this vector and the specified object are
     *         both vectors with equal elements; <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Vector))
            return false;
        final int dimension = this.getDimension();
        Vector v = (Vector) that;
        if (v.getDimension() != dimension)
            return false;
        for (int i = dimension; --i >= 0;) {
            if (!this.get(i).equals(v.get(i)))
                return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for this vector.
     * Equals objects have equal hash codes.
     *
     * @return this vector hash code value.
     * @see    #equals
     */
    public int hashCode() {
        final int dimension = this.getDimension();
        int code = 0;
        for (int i = dimension; --i >= 0;) {
            code += get(i).hashCode();
        }
        return code;
    }
    
    /**
     * Returns a copy of this vector 
     * {@link javolution.context.AllocatorContext allocated} 
     * by the calling thread (possibly on the stack).
     *     
     * @return an identical and independant copy of this matrix.
     */
    public abstract Vector<F> copy();
    
}