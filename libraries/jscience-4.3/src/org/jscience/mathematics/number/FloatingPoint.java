/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2007 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.number;

import org.jscience.mathematics.structure.Field;

import javolution.context.LocalContext;
import javolution.context.ObjectFactory;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a floating point number of arbitrary precision.
 *     A floating point number consists of a {@link #getSignificand significand}
 *     and a decimal {@link #getExponent exponent}: 
 *     (<code>significand · 10<sup>exponent</sup></code>).</p>
 *     
 * <p> Unlike {@link Real} numbers, no calculation error is performed on 
 *     floating point instances but the number of digits used during 
 *     calculations can be specified (see {@link #setDigits(int)}). The 
 *     largest the number of digits, the smallest the numeric error.
 *     For example:[code]
 *         FloatingPoint two = FloatingPoint.valueOf(2); 
 *         FloatingPoint.setDigits(30); // 30 digits calculations.
 *         System.out.println(two.sqrt());
 *     >   0.141421356237309504880168872420E1
 *     [/code]</p>
 * 
 * <p> Instances of this class are immutable and can be used to find  
 *     accurate solutions to linear equations with the {@link 
 *     org.jscience.mathematics.vector.Matrix Matrix} class.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.1, June 8, 2007
 * @see <a href="http://en.wikipedia.org/wiki/Floating_point">
 *      Wikipedia: Floating point</a>
 */
public final class FloatingPoint extends Number<FloatingPoint> implements
        Field<FloatingPoint> {

    /**
     * Holds the default XML representation for floating point numbers.
     * This representation consists of a simple <code>value</code> attribute
     * holding the {@link #toText() textual} representation.
     */
    static final XMLFormat<FloatingPoint> XML = new XMLFormat<FloatingPoint>(
            FloatingPoint.class) {

        @Override
        public FloatingPoint newInstance(Class<FloatingPoint> cls,
                InputElement xml) throws XMLStreamException {
            return FloatingPoint.valueOf(xml.getAttribute("value"));
        }

        public void write(FloatingPoint FloatingPoint, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", FloatingPoint.toText());
        }

        public void read(InputElement xml, FloatingPoint FloatingPoint) {
            // Nothing to do, immutable.
        }
    };

    /**
     * Holds the factory constructing floating point instances.
     */
    private static final ObjectFactory<FloatingPoint> FACTORY = new ObjectFactory<FloatingPoint>() {

        protected FloatingPoint create() {
            return new FloatingPoint();
        }
    };

    /**
     * The floating point instance representing the additive identity.
     */
    public static final FloatingPoint ZERO = new FloatingPoint(
            LargeInteger.ZERO, 0);

    /**
     * The floating point instance representing the multiplicative identity.
     */
    public static final FloatingPoint ONE = new FloatingPoint(LargeInteger.ONE,
            0);

    /** 
     * The Not-a-Number instance (unique). 
     */
    public static final FloatingPoint NaN = new FloatingPoint(
            LargeInteger.ZERO, Integer.MAX_VALUE);

    /**
     * Holds the number of digits to be used (default 20 digits).
     */
    private static final LocalContext.Reference<Integer> DIGITS = new LocalContext.Reference<Integer>(
            20);

    /**
     * Holds the significand value.
     */
    private LargeInteger _significand;

    /**
     * Holds the power of 10 exponent.
     */
    private int _exponent;

    /**
     * Default constructor. 
     */
    private FloatingPoint() {
    }

    /**
     * Creates a floating point number for the specified significand and 
     * exponent.
     * 
     * @param significand the significand.
     * @param exponent the power of two exponent.
     */
    private FloatingPoint(LargeInteger significand, int exponent) {
        _significand = significand;
        _exponent = exponent;
    }

    /**
     * Returns the floating point number for the specified {@link 
     * LargeInteger} significand and power of two exponent. 
     * 
     * @param significand the significand value.
     * @param exponent the power of two exponent.
     * @return <code>(significand · 2<sup>exponent</sup></code>
     */
    public static FloatingPoint valueOf(LargeInteger significand, int exponent) {
        FloatingPoint fp = FACTORY.object();
        fp._significand = significand;
        fp._exponent = exponent;
        return fp;
    }

    /**
     * Returns the floating point number for the specified <code>long</code>
     * significand and power of two exponent (convenience method). 
     * 
     * @param significand the significand value.
     * @param exponent the power of two exponent.
     * @return <code>(significand · 2<sup>exponent</sup></code>
     */
    public static FloatingPoint valueOf(long significand, int exponent) {
        FloatingPoint fp = FACTORY.object();
        fp._significand = LargeInteger.valueOf(significand);
        fp._exponent = exponent;
        return fp;
    }

    /**
     * Returns the floating point number for the specified <code>long</code>
     * value (convenience method). 
     * 
     * @param longValue the <code>long</code> value.
     * @return <code>FloatingPoint.valueOf(longValue, 0)</code>
     */
    public static FloatingPoint valueOf(long longValue) {
        return FloatingPoint.valueOf(longValue, 0);
    }

    /**
     * Returns the floating point number for the specified <code>double</code>
     * value (convenience method). 
     * 
     * @param doubleValue the <code>double</code> value.
     * @return <code>FloatingPoint.valueOf(longValue, 0)</code>
     */
    public static FloatingPoint valueOf(double doubleValue) {
        if (doubleValue == 0.0)
            return FloatingPoint.ZERO;
        if (doubleValue == 1.0)
            return FloatingPoint.ONE;
        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))
            return FloatingPoint.NaN;

        // Find the exponent e such as: value == x.xxx * 10^e
        int e = MathLib.floorLog10(MathLib.abs(doubleValue)) - 18 + 1; // 18 digits significand.
        long significand = MathLib.toLongPow10(doubleValue, -e);
        return FloatingPoint.valueOf(significand, e);
    }

    /**
     * Returns the floating point number for the specified character sequence.
     * The number of digits
     * 
     * @param  chars the character sequence.
     * @return the corresponding FloatingPoint number.
     */
    public static FloatingPoint valueOf(CharSequence chars) {
        // Use same format as Real.
        Real real = Real.valueOf(chars);
        if (real.getError() != 0)
            throw new IllegalArgumentException("No error allowed");
        return FloatingPoint.valueOf(real.getSignificand(), real.getExponent());
    }

    /**
     * Returns the {@link javolution.context.LocalContext local} number of 
     * digits used during calculations (default 20 digits). 
     * 
     * @return the number of digits.
     */
    public static int getDigits() {
        return DIGITS.get();
    }

    /**
     * Sets the {@link javolution.context.LocalContext local} number of digits
     * to be used during calculations.
     * 
     * @param digits the number of digits.
     * @throws IllegalArgumentException if <code>digits &lt;= 0</code>
     */
    public static void setDigits(int digits) {
        if (digits <= 0)
            throw new IllegalArgumentException("digits: " + digits
                    + " has to be greater than 0");
        DIGITS.set(digits);
    }

    /**
     * Returns the significand value.
     * 
     * @return this floating point significand.
     */
    public LargeInteger getSignificand() {
        return _significand;
    }

    /**
     * Returns the decimal exponent.
     * 
     * @return this floating point decimal exponent.
     */
    public int getExponent() {
        return _exponent;
    }

    /**
     * Returns the closest integer to this floating point number.
     * 
     * @return this floating point rounded to the nearest integer.
     */
    public LargeInteger round() {
        if (this == NaN)
            throw new ArithmeticException("Cannot convert NaN to integer value");
        LargeInteger half = LargeInteger.FIVE.times10pow(-_exponent-1);
        return isNegative() ? _significand.minus(half).times10pow(_exponent)
                : _significand.plus(half).times10pow(_exponent);
    }

    /**
     * Returns the opposite of this floating point number.
     * 
     * @return <code>-this</code>.
     */
    public FloatingPoint opposite() {
        return FloatingPoint.valueOf(_significand.opposite(), _exponent);
    }

    /**
     * Returns the sum of this floating point number with the one specified.
     * 
     * @param that the floating point number to be added.
     * @return <code>this + that</code>.
     */
    public FloatingPoint plus(FloatingPoint that) {
        if (this._exponent > that._exponent)
            return that.plus(this);
        int pow10Scaling = that._exponent - this._exponent;
        LargeInteger thatScaled = that._significand.times10pow(pow10Scaling);
        return FloatingPoint.valueOf(_significand.plus(thatScaled), _exponent)
                .normalize();
    }

    /**
     * Returns the difference between this FloatingPoint number and the one
     * specified.
     * 
     * @param that the floating point number to be subtracted.
     * @return <code>this - that</code>.
     */
    public FloatingPoint minus(FloatingPoint that) {
        if (this._exponent > that._exponent)
            return that.opposite().plus(this);
        int pow10Scaling = that._exponent - this._exponent;
        LargeInteger thatScaled = that._significand.times10pow(pow10Scaling);
        return FloatingPoint.valueOf(_significand.minus(thatScaled), _exponent)
                .normalize();
    }

    /**
     * Returns the product of this floating point number with the specified 
     * <code>long</code> multiplier.
     * 
     * @param multiplier the <code>long</code> multiplier.
     * @return <code>this · multiplier</code>.
     */
    public FloatingPoint times(long multiplier) {
        return this.times(FloatingPoint.valueOf(multiplier));
    }

    /**
     * Returns the product of this floating point number with the one specified.
     * 
     * @param that the floating point number multiplier.
     * @return <code>this · that</code>.
     */
    public FloatingPoint times(FloatingPoint that) {
        return FloatingPoint.valueOf(
                this._significand.times(that._significand),
                this._exponent + that._exponent).normalize();
    }

    /**
     * Returns the inverse of this floating point number.
     * 
     * @return <code>1 / this</code>.
     * @throws ArithmeticException if <code>dividend.isZero()</code>
     */
    public FloatingPoint inverse() {
        if (_significand.isZero())
            return NaN;
        int pow10 = DIGITS.get() + _significand.digitLength();
        LargeInteger dividend = LargeInteger.ONE.times10pow(pow10);
        return FloatingPoint.valueOf(dividend.divide(_significand),
                -pow10 - _exponent).normalize();
    }

    /**
     * Returns this floating point number divided by the one specified.
     * 
     * @param that the FloatingPoint number divisor.
     * @return <code>this / that</code>.
     * @throws ArithmeticException if <code>that.equals(ZERO)</code>
     */
    public FloatingPoint divide(FloatingPoint that) {
        if (that._significand.isZero())
            return NaN;
        int pow10 = DIGITS.get() + that._significand.digitLength();
        LargeInteger dividend = _significand.times10pow(pow10);
        return FloatingPoint.valueOf(dividend.divide(that._significand),
                this._exponent - pow10 - that._exponent).normalize();
    }

    /**
     * Returns the absolute value of this floating point  number.
     * 
     * @return <code>|this|</code>.
     */
    public FloatingPoint abs() {
        return FloatingPoint.valueOf(_significand.abs(), _exponent);
    }

    /**
     * Returns the square root of this floating point number.
     * 
     * @return the positive square root of this floating point number.
     */
    public FloatingPoint sqrt() {
        if (this == NaN)
            return NaN;
        if (isZero()) return ZERO;
        int pow10 = DIGITS.get() * 2 - _significand.digitLength();
        int exp = _exponent - pow10;
        if ((exp & 1) == 1) { // Ensures that exp is even.
            pow10++;
            exp--;
        }
        LargeInteger scaledValue = _significand.times10pow(pow10);
        return FloatingPoint.valueOf(scaledValue.sqrt(), exp >> 1)
                .normalize();
    }

    /**
     * Indicates if this floating point number is equal to zero.
     * 
     * @return <code>this == 0</code>
     */
    public boolean isZero() {
        return _significand.isZero() && (this != NaN);
    }

    /**
     * Indicates if this floating point number is greater than zero.
     * 
     * @return <code>this > 0</code>
     */
    public boolean isPositive() {
        return _significand.isPositive();
    }

    /**
     * Indicates if this rational number is less than zero.
     * 
     * @return <code>this < 0</code>
     */
    public boolean isNegative() {
        return _significand.isNegative();
    }

    /**
     * Indicates if this floating point is Not-a-Number.
     * 
     * @return <code>true</code> if this number has unbounded value;
     *         <code>false</code> otherwise.
     */
    public boolean isNaN() {
        return this == NaN;
    }

    /**
     * Compares the absolute value of two FloatingPoint numbers.
     *
     * @param that the FloatingPoint number to be compared with.
     * @return <code>|this| > |that|</code>
     */
    public boolean isLargerThan(FloatingPoint that) {
        return this.abs().compareTo(that.abs()) > 0;
    }

    /**
     * Returns the decimal text representation of this number.
     *
     * @return the text representation of this number.
     */
    public Text toText() {
        if (this == NaN)
            return Text.valueOf("NaN");
        if (this._significand.isZero())
            return Text.valueOf("0.0");
        TextBuilder tb = TextBuilder.newInstance();
        LargeInteger m = _significand;
        if (isNegative()) {
            tb.append('-');
            m = m.opposite();
        }
        tb.append("0.");
        LargeInteger.DECIMAL_FORMAT.format(m, tb);
        int exp = _exponent + m.digitLength();
        if (exp != 0) {
            tb.append("E");
            tb.append(_exponent + m.digitLength());
        }
        Text txt = tb.toText();
        TextBuilder.recycle(tb);
        return txt;
    }

    /**
     * Compares this floating point number against the specified object.
     * 
     * @param that the object to compare with.
     * @return <code>true</code> if the objects are the same;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (that instanceof FloatingPoint) {
            return this.minus((FloatingPoint) that).isZero();
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code for this floating point number.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        if (isZero()) return 0;
        if (isNaN()) return 483929293; // some random number
        // This is a random prime - the same as in LargeInteger.hashCode()
        // We return _significand.mod(p).times(10.pow(-exp) mod p)
        final long p = 1327144033;  
        long code = _significand.hashCode();
        int exp = _exponent;
        long mult;
        if (0 > exp) {
            mult = 398143210; // modInverse of 10 mod p
            exp = -exp;
        } else {
            mult = 10;
        }
        while (0 != exp) {
            if (1 == exp % 2) {
                code = (code * mult) % p;                
            }
            mult = (mult * mult) % p;
            exp = exp / 2;
        }
        return (int) code;
    }

    /**
     * Returns the value of this floating point number as a <code>long</code>.
     * 
     * @return the numeric value represented by this floating point 
     *         after conversion to type <code>long</code>.
     */
    public long longValue() {
        Real real = Real.valueOf(_significand, 0, _exponent);
        return real.longValue();
    }

    /**
     * Returns the value of this floating point number as a <code>double</code>.
     * 
     * @return the numeric value represented by this FloatingPoint after conversion
     *         to type <code>double</code>.
     */
    public double doubleValue() {
        Real real = Real.valueOf(_significand, 0, _exponent);
        return real.doubleValue();
    }

    /**
     * Compares two floating point number numerically.
     * 
     * @param that the floating point number to compare with.
     * @return -1, 0 or 1 as this FloatingPoint number is numerically less than, 
     *         equal to, or greater than <code>that</code>.
     */
    public int compareTo(FloatingPoint that) {
        FloatingPoint diff = this.minus(that);
        if (diff.isPositive()) {
            return 1;
        } else if (diff.isNegative()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Returns this floating point number after normalization based upon 
     * the number of digits.
     * 
     * @return <code>this</code>
     */
    private FloatingPoint normalize() {
        int digits = FloatingPoint.getDigits();
        int thisDigits = this._significand.digitLength();
        if (thisDigits > digits) { // Scale down.
            int pow10 = digits - thisDigits; // Negative.
            _significand = _significand.times10pow(pow10);
            long exponent = ((long) _exponent) - pow10;
            if (exponent > Integer.MAX_VALUE)
                return NaN;
            if (exponent < Integer.MIN_VALUE)
                return ZERO;
            _exponent = (int) exponent;
        }
        return this;
    }

    @Override
    public FloatingPoint copy() {
        if (this == NaN)
            return NaN; // Maintains unicity.
        FloatingPoint r = FACTORY.object();
        r._significand = _significand.copy();
        r._exponent = _exponent;
        return r;
    }

    private static final long serialVersionUID = 1L;
}