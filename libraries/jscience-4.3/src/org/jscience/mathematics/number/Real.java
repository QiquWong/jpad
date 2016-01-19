/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.number;

import org.jscience.mathematics.structure.Field;

import javolution.lang.MathLib;
import javolution.context.HeapContext;
import javolution.context.LocalContext;
import javolution.context.ObjectFactory;
import javolution.text.Text;
import javolution.text.TypeFormat;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a real number of arbitrary precision with 
 *     known/guaranteed uncertainty. A real number consists of a 
 *     {@link #getSignificand significand}, a maximum {@link #getError error} 
 *     (on the significand value) and a decimal {@link #getExponent exponent}: 
 *     (<code>(significand ± error) · 10<sup>exponent</sup></code>).</p>
 *     
 * <p> Reals number can be {@link #isExact exact} (e.g. integer values 
 *     scaled by a power of ten). Exactness is maintained for
 *     {@link org.jscience.mathematics.structure.Ring Ring} operations
 *     (e.g. addition, multiplication), but typically lost when a 
 *     multiplicative {@link #inverse() inverse} is calculated. The minimum 
 *     precision used for exact numbers is set by 
 *     {@link #setExactPrecision(int)} ({@link 
 *     javolution.context.LocalContext context local} setting, default
 *     <code>19</code> digits).<p>
 * 
 * <p> The actual {@link #getPrecision precision} and {@link #getAccuracy 
 *     accuracy} of any real number is available and <b>guaranteed</b> 
 *     (the true/exact value is always within the precision/accuracy range).</p>
 * 
 * <p> Operations on instances of this class are quite fast   
 *     as information substantially below the precision level (aka noise)
 *     is not processed/stored. There is no limit on a real precision
 *     but precision degenerates (due to numeric errors) and calculations 
 *     accelerate as more and more operations are performed.</p>
 * 
 * <p> Instances of this class can be utilized to find approximate 
 *     solutions to linear equations using the 
 *     {@link org.jscience.mathematics.vector.Matrix Matrix} class for which
 *     high-precision reals is often required, the primitive type
 *     <code>double</code> being not accurate enough to resolve equations 
 *     when the matrix's size exceeds 100x100. Furthermore, even for small 
 *     matrices the "qualified" result is indicative of possible system 
 *     singularities.</p>
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 8, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Real_number">
 *      Wikipedia: Real number</a>
 */
public final class Real extends Number<Real> implements Field<Real> {

    /**
     * Holds the default XML representation for real numbers.
     * This representation consists of a simple <code>value</code> attribute
     * holding the {@link #toText() textual} representation.
     */
    static final XMLFormat<Real> XML = new XMLFormat<Real>(Real.class) {

        @Override
        public Real newInstance(Class<Real> cls, InputElement xml)
                throws XMLStreamException {
            return Real.valueOf(xml.getAttribute("value"));
        }

        public void write(Real real, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", real.toText());
        }

        public void read(InputElement xml, Real real) {
            // Nothing to do, immutable.
        }
    };

    /** 
     * Holds a Not-a-Number instance (infinite error). 
     */
    public static final Real NaN = new Real(); // Unique (0 ± 1E2147483647)
    static {
        NaN._significand = LargeInteger.ZERO;
        NaN._error = LargeInteger.ONE;
        NaN._exponent = Integer.MAX_VALUE;
    }

    /** 
     * Holds the exact ZERO instance. 
     */
    public static final Real ZERO;

    /** 
     * Holds the exact ONE instance. 
     */
    public static final Real ONE;

    /**
     * Holds local precision for exact number.
     */
    private static final LocalContext.Reference<Integer> EXACT_PRECISION = new LocalContext.Reference<Integer>(
            new Integer(19));

    /**
     * The significand value.
     */
    private LargeInteger _significand;

    /**
     * The significand error (0 for exact number).
     */
    private LargeInteger _error;

    /**
     * The decimal exponent.
     */
    private int _exponent;

    /**
     * Default constructor.
     */
    private Real() {
    }

    /**
     * Returns the {@link javolution.context.LocalContext local} minimum 
     * precision (number of exact digits) when exact numbers have to be
     * approximated.
     * 
     * @return the minimum number of digits assumed exact for {@link #isExact 
     *         exact} real numbers.
     */
    public static int getExactPrecision() {
        return EXACT_PRECISION.get();
    }

    /**
     * Sets the {@link javolution.context.LocalContext local} minimum precision
     * (number of exact digits) when exact numbers have to be approximated.
     * 
     * @param precision the minimum number of digits assumed exact for 
     *        {@link #isExact exact} numbers.
     */
    public static void setExactPrecision(int precision) {
        EXACT_PRECISION.set(precision);
    }

    /**
     * Returns a real having the specified significand, error and exponent values.
     * If the error is <code>0</code>, the real is assumed exact. 
     * For example:[code]
     * 
     *     // x = 0.0 ± 0.01 
     *     Real x = Real.valueOf(LargeInteger.ZERO, 1, -2);
     *                           
     *      // y = -12.3 exact 
     *     Real y = Real.valueOf(LargeInteger.valueOf(-123), 0, -1);
     * 
     * [/code]
     * 
     * @param significand this real significand.
     * @param error the maximum error on the significand.
     * @param exponent the decimal exponent.
     * @return <code>(significand ± error)·10<sup>exponent</sup>)</code>
     * @throws IllegalArgumentException if <code>error < 0</code>
     */
    public static Real valueOf(LargeInteger significand, int error,
            int exponent) {
        if (error < 0)
            throw new IllegalArgumentException("Error cannot be negative");
        Real real = FACTORY.object();
        real._significand = significand;
        real._error = LargeInteger.valueOf(error);
        real._exponent = exponent;
        return real;
    }

    /**
     * Returns the real number (inexact except for <code>0.0</code>) 
     * corresponding to the specified <code>double</code> value. 
     * The error is derived from the inexact representation of 
     * <code>double</code> values intrinsic to the 64 bits IEEE 754 format.
     * 
     * @param doubleValue the <code>double</code> value to convert.
     * @return the corresponding real number.
     */
    public static Real valueOf(double doubleValue) {
        if (doubleValue == 0.0)
            return Real.ZERO;
        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))
            return Real.NaN;
        // Find the exponent e such as: value == x.xxx * 10^e
        int e = MathLib.floorLog10(MathLib.abs(doubleValue)) - 18 + 1; // 18 digits significand.
        long significand = MathLib.toLongPow10(doubleValue, -e);
        int error = (int) MathLib.toLongPow10(Math.ulp(doubleValue), -e) + 1;
        return Real.valueOf(LargeInteger.valueOf(significand), error, e);
    }

    /**
     * Returns the exact real number corresponding to the specified 
     * <code>long</code> value (convenience method).  
     * 
     * @param longValue the exact long value.
     * @return <code>Real.valueOf(LargeInteger.valueOf(longValue), 0, 0)</code>
     */
    public static Real valueOf(long longValue) {
        return Real.valueOf(LargeInteger.valueOf(longValue), 0, 0);
    }

    /**
     * Returns the real for the specified character sequence.
     * If the precision is not specified (using the <code>±</code> symbol), 
     * the real is supposed exact. Example of valid character sequences:
     * <li>"1.2E3" (1200 exact)</li>
     * <li>"1.2E3±1E-2" (1200 ± 0.01)</li></ul>
     * 
     * @param  chars the character sequence.
     * @return the corresponding real number.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable real.
     */
    public static Real valueOf(CharSequence chars) throws NumberFormatException {
        if ('-' == chars.charAt(0)) {
            return valueOf(chars.subSequence(1, chars.length())).opposite();
        }
        Text txt = Text.valueOf(chars); // TODO Use TextFormat...
        if ((txt.length() == 3) && (txt.indexOf("NaN", 0) == 0))
            return NaN;
        if (txt.equals("0"))
            return ZERO;
        int exponentIndex = txt.indexOf("E", 0);
        if (exponentIndex >= 0) {
            int exponent = TypeFormat.parseInt(txt.subtext(exponentIndex + 1,
                    txt.length()));
            Real r = valueOf(txt.subtext(0, exponentIndex));
            if (r == ZERO)
                return valueOf(LargeInteger.ZERO, 1, exponent);
            r._exponent += exponent;
            return r;
        }
        Real real = FACTORY.object();
        int errorIndex = txt.indexOf("±", 0);
        if (errorIndex >= 0) {
            real._significand = LargeInteger.valueOf(txt.subtext(0, errorIndex));
            real._error = LargeInteger.valueOf(txt.subtext(errorIndex + 1, txt
                    .length()));
            if (real._error.isNegative())
                throw new NumberFormatException(chars
                        + " not parsable (error cannot be negative)");
            real._exponent = 0;
            return real;
        }
        int decimalPointIndex = txt.indexOf(".", 0);
        if (decimalPointIndex >= 0) {
            LargeInteger integer = LargeInteger.valueOf(txt.subtext(0,
                    decimalPointIndex));
            LargeInteger fraction = LargeInteger.valueOf(txt.subtext(
                    decimalPointIndex + 1, txt.length()));
            int fractionDigits = chars.length() - decimalPointIndex - 1;
            real._significand = integer.isNegative() ? integer.times10pow(
                    fractionDigits).minus(fraction) : integer.times10pow(
                    fractionDigits).plus(fraction);
            real._error = LargeInteger.ZERO;
            real._exponent = -fractionDigits;
            return real;
        } else {
            real._significand = LargeInteger.valueOf(chars);
            real._error = LargeInteger.ZERO;
            real._exponent = 0;
            return real;
        }
    }

    /**
     * Returns this real <a href="http://en.wikipedia.org/wiki/Significand">
     * significand</a> value.
     * 
     * @return the significand.
     */
    public LargeInteger getSignificand() {
        return _significand;
    }

    /**
     * Returns the maximum error (positive) on this real significand.
     * 
     * @return the maximum error on the significand.
     */
    public int getError() {
        return _error.intValue();
    }

    /**
     * Returns the exponent of the power of 10 multiplier.
     * 
     * @return the decimal exponent.
     */
    public int getExponent() {
        return _exponent;
    }

    /**
     * Indicates if this real number is exact (<code>{@link #getError() error} 
     * == 0</code>).
     *
     * @return <code>getError() == 0</code>
     */
    public boolean isExact() {
        return _error.isZero();
    }

    /**
     * Returns the number of decimal digits guaranteed exact which appear to
     * the right of the decimal point (absolute error).
     *
     * @return a measure of the absolute error of this real number.
     */
    public int getAccuracy() {
        if (_error.isZero())
            return Integer.MAX_VALUE;
        if (this == NaN)
            return Integer.MIN_VALUE;
        return -_exponent - _error.digitLength();
    }

    /**
     * Returns the total number of decimal digits guaranteed exact
     * (relative error).
     *
     * @return a measure of the relative error of this real number.
     */
    public final int getPrecision() {
        if (_error.isZero())
            return Integer.MAX_VALUE;
        if (this == NaN)
            return Integer.MIN_VALUE;
        return _significand.digitLength() - _error.digitLength();
    }

    /**
     * Indicates if this real is greater than zero.
     * 
     * @return <code>this > 0</code>
     */
    public boolean isPositive() {
        return _significand.isPositive();
    }

    /**
     * Indicates if this real is less than zero.
     * 
     * @return <code>this < 0</code>
     */
    public boolean isNegative() {
        return _significand.isNegative();
    }

    /**
     * Indicates if this real is Not-a-Number (unbounded value interval).
     * 
     * @return <code>true</code> if this number has unbounded value interval;
     *         <code>false</code> otherwise.
     */
    public boolean isNaN() {
        return this == NaN;
    }

    /**
     * Indicates if this real approximates the one specified. 
     * This method takes into account possible errors (e.g. numeric
     * errors) to make this determination.
     *  
     * <p>Note: This method returns <code>true</code> if <code>this</code> or 
     *          <code>that</code> {@link #isNaN} (basically Not-A-Number 
     *          approximates anything).</p>
     *
     * @param  that the real to compare with.
     * @return <code>this &asymp; that</code>
     */
    public boolean approximates(Real that) {
        Real diff = this.minus(that);
        if (diff == NaN)
            return false;
        return diff._error.isLargerThan(diff._significand);
    }

    /**
     * Returns the closest integer value to this rational number.
     * 
     * @return this real rounded to the nearest integer.
     * @throws ArithmeticException if <code>this.isNaN()</code>
     */
    public LargeInteger round() {
        if (this == NaN)
            throw new ArithmeticException("Cannot convert NaN to integer value");
        LargeInteger half = LargeInteger.FIVE.times10pow(-_exponent - 1);
        return isNegative() ? _significand.minus(half).times10pow(_exponent) :
            _significand.plus(half).times10pow(_exponent);
    }

    /**
     * Returns the negation of this real number.
     * 
     * @return <code>-this</code>.
     */
    public Real opposite() {
        if (this == NaN)
            return NaN;
        Real real = FACTORY.object();
        real._significand = _significand.opposite();
        real._exponent = _exponent;
        real._error = _error;
        return real;
    }

    /**
     * Returns the sum of this real number with the one specified.
     * 
     * @param that the real to be added.
     * @return <code>this + that</code>.
     */
    public Real plus(Real that) {
        if ((this == NaN) || (that == NaN))
            return NaN;
        if (this._exponent > that._exponent)
            return that.plus(this); // Adds to the real with smallest exponent. 
        int scale = that._exponent - this._exponent; // >= 0
        Real real = FACTORY.object();
        real._exponent = _exponent;
        real._significand = this._significand.plus(that._significand.times10pow(scale));
        real._error = this._error.plus(that._error.times10pow(scale));
        return real.normalize();
    }

    /**
     * Returns the difference between this real number and the one
     * specified.
     * 
     * @param that the real to be subtracted.
     * @return <code>this - that</code>.
     */
    public Real minus(Real that) {
        return this.plus(that.opposite());
    }

    /**
     * Returns the product of this real number with the specified 
     * <code>long</code> multiplier.
     * 
     * @param multiplier the <code>long</code> multiplier.
     * @return <code>this · multiplier</code>.
     */
    public Real times(long multiplier) {
        if (this == NaN)
            return NaN;
        Real real = FACTORY.object();
        real._exponent = this._exponent;
        real._significand = this._significand.times(multiplier);
        real._error = this._error.times(multiplier);
        return real.normalize();
    }

    /**
     * Returns the product of this real number with the one specified.
     * 
     * @param that the real multiplier.
     * @return <code>this · that</code>.
     */
    public Real times(Real that) {
        if ((this == NaN) || (that == NaN))
            return NaN;
        long exp = ((long) this._exponent) + that._exponent;
        if (exp > Integer.MAX_VALUE || (exp < Integer.MIN_VALUE))
            return NaN; // Exponent overflow.
        LargeInteger thisMin = this._significand.minus(this._error);
        LargeInteger thisMax = this._significand.plus(this._error);
        LargeInteger thatMin = that._significand.minus(that._error);
        LargeInteger thatMax = that._significand.plus(that._error);
        LargeInteger min, max;
        if (thisMin.compareTo(thisMax.opposite()) > 0) {
            if (thatMin.compareTo(thatMax.opposite()) > 0) {
                min = thisMin.times(thatMin);
                max = thisMax.times(thatMax);
            } else {
                min = thisMax.times(thatMin);
                max = thisMin.times(thatMax);
            }
        } else {
            if (thatMin.compareTo(thatMax.opposite()) > 0) {
                min = thisMin.times(thatMax);
                max = thisMax.times(thatMin);
            } else {
                min = thisMax.times(thatMax);
                max = thisMin.times(thatMin);
            }
        }
        Real real = FACTORY.object();
        real._exponent = (int) exp;
        real._significand = min.plus(max).shiftRight(1);
        real._error = max.minus(min);
        return real.normalize();
    }

    /**
     * Returns this real number divided by the specified <code>int</code>
     * divisor.  
     * 
     * @param divisor the <code>int</code> divisor.
     * @return <code>this / divisor</code>
     */
    public Real divide(long divisor) {
        return this.divide(Real.valueOf(divisor));
    }

    /**
     * Returns this real number divided by the one specified.
     * 
     * @param that the real divisor.
     * @return <code>this / that</code>.
     * @throws ArithmeticException if <code>that.equals(ZERO)</code>
     */
    public Real divide(Real that) {
        return this.times(that.inverse());
    }

    /**
     * Returns the reciprocal (or inverse) of this real number.
     *
     * @return <code>1 / this</code>.
     */
    public Real inverse() {
        if ((this == NaN) || (this == ZERO))
            return NaN;
        if (this.isExact())
            return this.toInexact().inverse();
        LargeInteger thisMin = this._significand.minus(this._error);
        LargeInteger thisMax = this._significand.plus(this._error);
        if (thisMin.isNegative() && thisMax.isPositive()) // Encompasses 0
            return NaN;
        int digits = MathLib.max(thisMin.digitLength(), thisMax.digitLength());
        long exp = ((long) -this._exponent) - digits - digits;
        if ((exp > Integer.MAX_VALUE || (exp < Integer.MIN_VALUE)))
            return NaN; // Exponent overflow.
        LargeInteger min = div(2 * digits, thisMax);
        LargeInteger max = div(2 * digits, thisMin);
        Real real = FACTORY.object();
        real._exponent = (int) exp;
        real._significand = min.plus(max).shiftRight(1);
        real._error = max.minus(min).plus(LargeInteger.ONE);
        return real.normalize();
    }

    private static LargeInteger div(int exp, LargeInteger significand) {
        int expBitLength = (int) (exp * DIGITS_TO_BITS);
        int precision = expBitLength - significand.bitLength() + 1;
        LargeInteger reciprocal = significand.inverseScaled(precision);
        LargeInteger result = reciprocal.times10pow(exp);
        return result.shiftRight(expBitLength + 1);
    }

    private static final double DIGITS_TO_BITS = MathLib.LOG10 / MathLib.LOG2;

    private Real toInexact() {
        int digits = _significand.digitLength();
        int scale = Real.getExactPrecision() - digits + 1;
        Real z = FACTORY.object();
        z._significand = _significand.times10pow(scale);
        z._error = LargeInteger.ONE;
        z._exponent = _exponent - scale;
        return z;
    }

    /**
     * Returns the absolute value of this real number.
     * 
     * @return <code>|this|</code>.
     */
    public Real abs() {
        return _significand.isNegative() ? this.opposite() : this;
    }

    /**
     * Compares the absolute value of two real numbers.
     *
     * @param that the real number to be compared with.
     * @return <code>|this| > |that|</code>
     */
    public boolean isLargerThan(Real that) {
        return this.abs().compareTo(that.abs()) > 0;
    }

    /**
     * Returns the square root of this real number, the more accurate is this 
     * real number, the more accurate the square root. 
     * 
     * @return the positive square root of this real number.
     */
    public Real sqrt() {
        if (this == NaN)
            return NaN;
        if (this == ZERO) return ZERO;
        if (this.isExact())
            return this.toInexact().sqrt();
        LargeInteger thisMin = this._significand.minus(this._error);
        LargeInteger thisMax = this._significand.plus(this._error);
        if (thisMin.isNegative())
            return NaN;
        int exponent = _exponent >> 1;
        if ((_exponent & 1) == 1) { // Odd exponent.
            thisMin = thisMin.times10pow(1);
            thisMax = thisMax.times10pow(1);
        }
        LargeInteger minSqrt = thisMin.sqrt();
        LargeInteger maxSqrt = thisMax.sqrt().plus(LargeInteger.ONE);
        LargeInteger sqrt = minSqrt.plus(maxSqrt).shiftRight(1);
        Real z = FACTORY.object();
        z._significand = sqrt;
        z._error = maxSqrt.minus(sqrt);
        z._exponent = exponent;
        return z.normalize();
    }

    /**
     * Returns the decimal text representation of this number.
     *
     * @return the text representation of this number.
     */
    public Text toText() {
        if (this == NaN)
            return Text.valueOf("NaN");
        if (isExact()) {
            return (_exponent == 0) ? _significand.toText() : 
                _significand.toText().plus("E").plus(Text.valueOf(_exponent));
        }
        int errorDigits = _error.digitLength();
        LargeInteger m = (_significand.isPositive()) ? _significand.plus(FIVE
                .times10pow(errorDigits - 1)) : _significand.plus(MINUS_FIVE
                .times10pow(errorDigits - 1));
        m = m.times10pow(-errorDigits);
        int exp = _exponent + errorDigits;
        Text txt = m.toText();
        int digits = (m.isNegative()) ? txt.length() - 1 : txt.length();
        if (digits > 1) {
            if ((exp < 0) && (-exp < digits)) {
                txt = txt.insert(txt.length() + exp, Text.valueOf('.'));
            } else { // Scientific notation.
                txt = txt.insert(txt.length() - digits + 1, Text.valueOf('.'));
                txt = txt.concat(Text.valueOf('E')).concat(
                        Text.valueOf(exp + digits - 1));
            }
        } else {
            txt = txt.concat(Text.valueOf('E')).concat(Text.valueOf(exp));
        }
        return txt;
    }

    /**
     * Compares this real number against the specified object.
     * 
     * <p>Note: This method returns <code>true</code> if <code>this</code> or 
     *          <code>that</code> {@link #isNaN is Not-A-Number}, even though
     *          <code>Double.NaN == Double.NaN</code> has the value
     *          <code>false</code>.</p>
     *
     * @param that the object to compare with.
     * @return <code>true</code> if the objects are two reals with same 
     *        significand, error and exponent;<code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Real))
            return false;
        Real thatReal = (Real) that;
        return this._significand.equals(thatReal._significand)
                && this._error.equals(thatReal._error)
                && (this._exponent == thatReal._exponent);
    }

    /**
     * Returns the hash code for this real number.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        return _significand.hashCode() + _error.hashCode() + _exponent * 31;
    }

    /**
     * Returns the value of this real number as a <code>long</code>.
     * 
     * @return the numeric value represented by this real after conversion
     *         to type <code>long</code>.
     */
    public long longValue() {
        return (long) doubleValue();
    }

    /**
     * Returns the value of this real number as a <code>double</code>.
     * 
     * @return the numeric value represented by this real after conversion
     *         to type <code>double</code>.
     */
    public double doubleValue() {
        if (this == NaN)
            return Double.NaN;
        if (this == ZERO)
            return 0.0;
        // Shift the significand to a >18 digits integer (long compatible).
        int nbrDigits = _significand.digitLength();
        int digitShift = nbrDigits - 18;
        long reducedSignificand = _significand.times10pow(-digitShift).longValue();
        int exponent = _exponent + digitShift;
        return MathLib.toDoublePow10(reducedSignificand, exponent);
    }

    /**
     * Compares two real numbers numerically.
     * 
     * @param that the real to compare with.
     * @return -1, 0 or 1 as this real is numerically less than, equal to,
     *         or greater than <code>that</code>.
     * @throws ClassCastException <code>that</code> is not a {@link Real}.
     */
    public int compareTo(Real that) {
        Real diff = this.minus(that);
        if (diff.isPositive()) {
            return 1;
        } else if (diff.isNegative()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Normalizes this real (maintains error less than 31 bits).
     * 
     * @return the normalized real.
     */
    private Real normalize() {
        int digitError = this._error.digitLength();
        int scale = 9 - digitError;
        if (scale >= 0) return this; // Small error.
        Real z = FACTORY.object();
        z._significand = _significand.times10pow(scale);
        z._error = _error.times10pow(scale).plus(LargeInteger.ONE);
        z._exponent = _exponent - scale;
        return z;
    }
                
    @Override
    public Real copy() {
        if (this == NaN) return NaN; // Maintains unicity.
        return Real.valueOf(_significand.copy(), getError(), _exponent);
    }

    /**
     * Holds the factory constructing real instances.
     */
    private static final ObjectFactory<Real> FACTORY = new ObjectFactory<Real>() {

        protected Real create() {
            return new Real();
        }
    };

    private static final LargeInteger FIVE;

    private static final LargeInteger MINUS_FIVE;

    static { // Immortal memory allocation.
        HeapContext.enter();
        try {
            ZERO = Real.valueOf(0);
            ONE = Real.valueOf(1);
            FIVE = LargeInteger.valueOf(5);
            MINUS_FIVE = LargeInteger.valueOf(-5);
        } finally {
            HeapContext.exit();
        }
    }

    private static final long serialVersionUID = 1L;
}