/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.number;

import org.jscience.mathematics.structure.Ring;
import javolution.lang.Realtime;
import javolution.text.Text;
import javolution.xml.XMLSerializable;

/**
 * <p> This class represents a {@link javolution.lang.ValueType value-type}
 *     number.</p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Number">
 *      Wikipedia: Number</a>
 */
public abstract class Number<T extends Number<T>> extends java.lang.Number
        implements Ring<T>, Comparable<T>, Realtime, XMLSerializable {

    /**
     * Compares the magnitude of this number with that number.
     *
     * @return <code>|this| > |that|</code>
     */
    public abstract boolean isLargerThan(T that);

    /**
     * Returns the value of this number as a <code>long</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    public abstract long longValue();

    /**
     * Returns the value of this number as a <code>double</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    public abstract double doubleValue();

    /**
     * Compares this number with the specified number for order.  Returns a
     * negative integer, zero, or a positive integer as this number is less
     * than, equal to, or greater than the specified number. 
     * Implementation must ensure that this method is consistent with equals 
     * <code>(x.compareTo(y)==0) == (x.equals(y))</code>,  
     * 
     * @param that the number to be compared.
     * @return a negative integer, zero, or a positive integer as this number
     *        is less than, equal to, or greater than the specified number.
     */
    public abstract int compareTo(T that);

    /**
     * Indicates if this number is ordered before that number
     * (convenience method).
     *
     * @param that the number to compare with.
     * @return <code>this.compareTo(that) < 0</code>.
     */
    public final boolean isLessThan(T that) {
        return this.compareTo(that) < 0;
    }

    /**
     * Indicates if this number is ordered after that number
     * (convenience method).
     *
     * @param that the number to compare with.
     * @return <code>this.compareTo(that) > 0</code>.
     */
    public final boolean isGreaterThan(T that) {
        return this.compareTo(that) > 0;
    }

    /**
     * Returns the difference between this number and the one specified.
     *
     * @param  that the number to be subtracted.
     * @return <code>this - that</code>.
     */
    public T minus(T that) {
        return this.plus(that.opposite());
    }

    /**
     * Returns this number raised at the specified positive exponent.
     *
     * @param  exp the positive exponent.
     * @return <code>this<sup>exp</sup></code>
     * @throws IllegalArgumentException if <code>exp &lt;= 0</code> 
     */
    @SuppressWarnings("unchecked")
    public T pow(int exp) {
        if (exp <= 0)
            throw new IllegalArgumentException("exp: " + exp
                    + " should be a positive number");
        final T t = (T) this;
        if (exp == 1) return t;
        if (exp == 2) return t.times(t);
        if (exp == 3) return t.times(t).times(t);
        int halfExp = exp >> 1;
        return this.pow(halfExp).times(this.pow(exp - halfExp));
    }

    /**
     * Returns the value of this number as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     */
    public final byte byteValue() {
        return (byte) longValue();
    }

    /**
     * Returns the value of this number as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
     */
    public final short shortValue() {
        return (short) longValue();
    }

    /**
     * Returns the value of this number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    public final int intValue() {
        return (int) longValue();
    }

    /**
     * Returns the value of this number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public final float floatValue() {
        return (float) doubleValue();
    }
    /**
     * Indicates if this number is equals to the specified object.
     *
     * @param obj the object to be compared with.
     * @return <code>true</code> if this number and the specified argument
     *         represent the same number; <code>false</code> otherwise.
     */
    public abstract boolean equals(Object obj);

    /**
     * Returns the hash code for this number (consistent with 
     * {@link #equals(Object)}.
     *
     * @return this number hash code.
     */
    public abstract int hashCode();

    /**
     * Returns the textual representation of this real-time object
     * (equivalent to <code>toString</code> except that the returned value
     * can be allocated from the local context space).
     * 
     * @return this object's textual representation.
     */
    public abstract Text toText();

    /**
     * Returns a copy of this number 
     * {@link javolution.context.AllocatorContext allocated} 
     * by the calling thread (possibly on the stack).
     *     
     * @return an identical and independant copy of this number.
     */
    public abstract Number<T> copy();

    /**
     * Returns the text representation of this number as a 
     * <code>java.lang.String</code>.
     * 
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

}