/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.number;

import javolution.context.ObjectFactory;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TypeFormat;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a 64 bits integer number.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Integer">
 *      Wikipedia: Integer</a>
 */
public final class Integer64 extends Number<Integer64> {

    /**
     * Holds the default XML representation for 64 bits integer numbers.
     * This representation consists of a simple <code>value</code> attribute
     * holding the {@link #toText() textual} representation.
     */
    static final XMLFormat<Integer64> XML = new XMLFormat<Integer64>(Integer64.class) {

        @Override
        public Integer64 newInstance(Class<Integer64> cls, InputElement xml)
                throws XMLStreamException {
            return Integer64.valueOf(xml.getAttribute("value", 0L));
        }

        public void write(Integer64 integer64, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", integer64._value);
        }

        public void read(InputElement xml, Integer64 integer64) {
            // Nothing to do, immutable.
        }
    };
 
    /**
     * Holds the factory used to produce 64 bits integer instances.
     */
    private static final ObjectFactory<Integer64> FACTORY = new ObjectFactory<Integer64>() {

        protected Integer64 create() {
            return new Integer64();
        }
    };

    /**
     * The 64 bits floating point representing zero.
     */
    public static final Integer64 ZERO = new Integer64(0L);

    /**
     * The 64 bits floating point representing one.
     */
    public static final Integer64 ONE = new Integer64(1L);

    /**
     * The associated long value.
     */
    private long _value;

    /**
     * Default constructor.
     */
    private Integer64() {
    }

    /**
     * Returns the 64 bits integer from the specified <code>long</code> value.
     *
     * @param  longValue the <code>long</code> value for this number.
     * @see    #longValue()
     */
    private Integer64(long longValue) {
         _value = longValue;
    }

    /**
     * Returns the 64 bits integer from the specified <code>long</code> value.
     *
     * @param  longValue the <code>long</code> value for this number.
     * @return the corresponding number.
     * @see    #longValue()
     */
    public static Integer64 valueOf(long longValue) {
        Integer64 r = FACTORY.object();
        r._value = longValue;
        return r;
    }

    /**
     * Returns the number for the specified character sequence.
     *
     * @param  chars the character sequence.
     * @return the corresponding number.
     */
    public static Integer64 valueOf(CharSequence chars) {
        Integer64 r = FACTORY.object();
        r._value = TypeFormat.parseLong(chars);
        return r;
    }

    /**
     * Returns the opposite of this number.
     *
     * @return <code>-this</code>.
     */
    public Integer64 opposite() {
        Integer64 r = FACTORY.object();
        r._value = -this._value;
        return r;
    }

    /**
     * Returns the sum of this number with the one specified.
     *
     * @param  that the number to be added.
     * @return <code>this + that</code>.
     */
    public Integer64 plus(Integer64 that) {
        Integer64 r = FACTORY.object();
        r._value = this._value + that._value;
        return r;
    }

    /**
     * Returns the sum of this number with the specifice value.
     *
     * @param  value the value to be added.
     * @return <code>this + value</code>.
     */
    public Integer64 plus(long value) {
        Integer64 r = FACTORY.object();
        r._value = this._value + value;
        return r;
    }

    /**
     * Returns the difference between this number and the one specified.
     *
     * @param  that the number to be subtracted.
     * @return <code>this - that</code>.
     */
    public Integer64 minus(Integer64 that) {
        Integer64 r = FACTORY.object();
        r._value = this._value - that._value;
        return r;
    }

    /**
     * Returns the difference between this number and the specified value
     *
     * @param  value the value to be subtracted.
     * @return <code>this - value</code>.
     */
    public Integer64 minus(long value) {
        Integer64 r = FACTORY.object();
        r._value = this._value - value;
        return r;
    }

    /**
     * Returns the product of this number with the one specified.
     *
     * @param  that the number multiplier.
     * @return <code>this · that</code>.
     */
    public Integer64 times(Integer64 that) {
        Integer64 r = FACTORY.object();
        r._value = this._value * that._value;
        return r;
    }

    /**
     * Returns the product of this number with the specified value.
     *
     * @param  value the value multiplier.
     * @return <code>this · value</code>.
     */
    public Integer64 times(long value) {
        Integer64 r = FACTORY.object();
        r._value = this._value * value;
        return r;
    }

    /**
     * Returns this number divided by the one specified.
     *
     * @param  that the number divisor.
     * @return <code>this / that</code>.
     */
    public Integer64 divide(Integer64 that) {
        Integer64 r = FACTORY.object();
        r._value = this._value / that._value;
        return r;
    }

    /**
     * Returns this number divided by the specified value.
     *
     * @param  value the value divisor.
     * @return <code>this / value</code>.
     */
    public Integer64 divide(long value) {
        Integer64 r = FACTORY.object();
        r._value = this._value / value;
        return r;
    }

    /**
     * Compares the magnitude of this number with that number.
     *
     * @return <code>|this| > |that|</code>
     */
    public boolean isLargerThan(Integer64 that) {
        return MathLib.abs(this._value) > MathLib.abs(that._value);
    }

    /**
     * Returns the absolute value of this number.
     *
     * @return <code>|this|</code>.
     */
    public Integer64 abs() {
        Integer64 r = FACTORY.object();
        r._value = MathLib.abs(this._value);
        return r;
    }

    /**
     * Returns the decimal text representation of this number.
     *
     * @return the text representation of this number.
     */
    public Text toText() {
        return Text.valueOf(_value);
    }

    /**
     * Compares this number against the specified object.
     *
     * @param  that the object to compare with.
     * @return <code>true</code> if the objects are the same;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        return (that instanceof Integer64)
                && (this._value == ((Integer64) that)._value);
    }

    /**
     * Compares this number against the specified value.
     *
     * @param  value the value to compare with.
     * @return <code>this.longValue() == value</code>
     */
    public boolean equals(long value) {
        return this._value == value;
    }

    /**
     * Compares this number with the specified value for order.
     * 
     * @param value the value to be compared with.
     * @return a negative integer, zero, or a positive integer as this number
     *        is less than, equal to, or greater than the specified value.
     */
    public int compareTo(long value) {
        if (this._value < value) {
            return -1;
        } else if (this._value > value) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /**
     * Returns the hash code for this number.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        int h = Float.floatToIntBits((float) _value);
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        return h ^ (h >>> 10);
    }

    @Override
    public long longValue() {
        return _value;
    }

    @Override
    public double doubleValue() {
        return _value;
    }

    @Override
    public int compareTo(Integer64 that) {
        return compareTo(that._value);
    }

    @Override
    public Integer64 copy() {
        return Integer64.valueOf(_value);
    }

    private static final long serialVersionUID = 1L;
}