/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.number;

import java.io.IOException;

import org.jscience.mathematics.structure.Field;

import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;
import javolution.context.ObjectFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

//@RETROWEAVER import javolution.text.Appendable;

/**
 * <p> This class represents an immutable complex number.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Complex_number">
 *      Wikipedia: Complex number</a>
 */
public final class Complex extends Number<Complex> implements Field<Complex> {

    /**
     * Holds the local text format for complex numbers (cartesian form 
     * by default, e.g.<code> "2.34 - 0.4i"</code>).
     */
    private static final TextFormat<Complex> CARTESIAN_FORMAT = new TextFormat<Complex>() {
        public Appendable format(Complex complex, Appendable appendable)
                throws IOException {
            TypeFormat.format(complex._real, appendable);
            if (complex._imaginary < 0.0) {
                appendable.append(" - ");
                TypeFormat.format(-complex._imaginary, appendable);
            } else {
                appendable.append(" + ");
                TypeFormat.format(complex._imaginary, appendable);
            }
            return appendable.append('i');
        }

        public Complex parse(CharSequence csq, Cursor cursor) {
            // Reads real part.
            double real = TypeFormat.parseDouble(csq, cursor);

            // Reads separator.
            cursor.skip(' ', csq);
            char op = cursor.next(csq);
            if ((op != '+') && (op != '-'))
                throw new NumberFormatException("'+' or '-' expected");
            cursor.skip(' ', csq);

            // Reads imaginary part.
            double imaginary = TypeFormat.parseDouble(csq, cursor);
            char i = cursor.next(csq);
            if (i != 'i')
                throw new NumberFormatException("'i' expected");
            return Complex.valueOf(real, op == '-' ? -imaginary : imaginary);
        }
    };
    static { // Sets default format to cartesian, users can always change it to polar.
        TextFormat.setInstance(Complex.class, CARTESIAN_FORMAT);
    }

    /**
     * The complex number zero.
     */
    public static final Complex ZERO = new Complex(0.0, 0.0);

    /**
     * The complex number one.
     */
    public static final Complex ONE = new Complex(1.0, 0.0);

    /**
     * The imaginary unit <i><b>i</b></i>.
     */
    public static final Complex I = new Complex(0.0, 1.0);

    /**
     * Holds the default XML representation for complex numbers.
     * This representation consists of <code>real</code> and  
     * <code>imaginary</code> attributes (e.g. 
     * <code>&lt;Complex real="2.34" imaginary="-0.4"/&gt;</code>).
     */
    static final XMLFormat<Complex> XML = new XMLFormat<Complex>(Complex.class) {

        @Override
        public Complex newInstance(Class<Complex> cls, InputElement xml)
                throws XMLStreamException {
            return Complex.valueOf(xml.getAttribute("real", 0.0), xml
                    .getAttribute("imaginary", 0.0));
        }

        public void write(Complex complex, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("real", complex._real);
            xml.setAttribute("imaginary", complex._imaginary);
        }

        public void read(InputElement xml, Complex complex) {
            // Nothing to do, immutable.
        }
    };

    /**
     * Holds the factory constructing complex instances.
     */
    private static final ObjectFactory<Complex> FACTORY = new ObjectFactory<Complex>() {
        protected Complex create() {
            return new Complex();
        }
    };

    /**
     * Holds the real component.
     */
    private double _real;

    /**
     * Holds the imaginary component.
     */
    private double _imaginary;

    /**
     * Default constructor.
     */
    private Complex() {
    }

    /**
     * Creates a complex number having the specified real and imaginary
     * components.
     * 
     * @param  real the real component of this complex number.
     * @param  imaginary the imaginary component of this complex number.
     */
    private Complex(double real, double imaginary) {
        _real = real;
        _imaginary = imaginary;
    }

    /**
     * Returns the complex number having the specified real and imaginary
     * components.
     *
     * @param  real the real component of this complex number.
     * @param  imaginary the imaginary component of this complex number.
     * @return the corresponding complex number.
     * @see    #getReal
     * @see    #getImaginary
     */
    public static Complex valueOf(double real, double imaginary) {
        Complex c = FACTORY.object();
        c._real = real;
        c._imaginary = imaginary;
        return c;
    }

    /**
     * Returns the complex number for the specified character sequence.
     *
     * @param  csq the character sequence.
     * @return <code>TextFormat.getInstance(Complex.class).parse(csq)</code>
     * @throws IllegalArgumentException if the character sequence does not 
     *         contain a parsable complex number.
     * @see TextFormat#getInstance(Class)
     */
    public static Complex valueOf(CharSequence csq) {
        return TextFormat.getInstance(Complex.class).parse(csq);
    }

    /**
     * Indicates if either the real or imaginary component of this complex
     * is infinite.
     *
     * @return  <code>true</code> if this complex is infinite;
     *          <code>false</code> otherwise.
     */
    public boolean isInfinite() {
        return Double.isInfinite(_real) | Double.isInfinite(_imaginary);
    }

    /**
     * Indicates if either the real or imaginary component of this complex
     * is not a number.
     *
     * @return  <code>true</code> if this complex is NaN;
     *          <code>false</code> otherwise.
     */
    public boolean isNaN() {
        return Double.isNaN(_real) | Double.isNaN(_imaginary);
    }

    /**
     * Returns the real component of this complex number.
     *
     * @return the real component.
     */
    public double getReal() {
        return _real;
    }

    /**
     * Returns the imaginary component of this complex number.
     *
     * @return the imaginary component.
     */
    public double getImaginary() {
        return _imaginary;
    }

    /**
     * Returns the negation of this complex.
     *
     * @return <code>-this</code>.
     */
    public Complex opposite() {
        Complex c = FACTORY.object();
        c._real = -this._real;
        c._imaginary = -this._imaginary;
        return c;
    }

    /**
     * Returns the sum of this complex with the one specified.
     *
     * @param  that the complex to be added.
     * @return <code>this + that</code>.
     */
    public Complex plus(Complex that) {
        Complex c = FACTORY.object();
        c._real = this._real + that._real;
        c._imaginary = this._imaginary + that._imaginary;
        return c;
    }

    /**
     * Returns the difference between this complex and the one specified.
     *
     * @param  that the complex to be subtracted.
     * @return <code>this - that</code>.
     */
    public Complex minus(Complex that) {
        Complex c = FACTORY.object();
        c._real = this._real - that._real;
        c._imaginary = this._imaginary - that._imaginary;
        return c;
    }

    /**
     * Returns this complex multiplied by the specified factor.
     *
     * @param  k the factor multiplier.
     * @return <code>this * k</code>.
     */
    public Complex times(double k) {
        Complex c = FACTORY.object();
        c._real = this._real * k;
        c._imaginary = this._imaginary * k;
        return c;
    }

    /**
     * Returns the product of this complex with the one specified.
     *
     * @param  that the complex multiplier.
     * @return <code>this * that</code>.
     */
    public Complex times(Complex that) {
        Complex c = FACTORY.object();
        c._real = this._real * that._real - this._imaginary * that._imaginary;
        c._imaginary = this._real * that._imaginary + this._imaginary
                * that._real;
        return c;
    }

    /**
     * Returns the inverse of this complex.
     *
     * @return <code>1 / this</code>.
     */
    public Complex inverse() {
        Complex c = FACTORY.object();
        double tmp = (this._real * this._real)
                + (this._imaginary * this._imaginary);
        c._real = this._real / tmp;
        c._imaginary = -this._imaginary / tmp;
        return c;
    }

    /**
     * Returns this complex divided by the specified factor.
     *
     * @param  k the factor divisor.
     * @return <code>this / k</code>.
     */
    public Complex divide(double k) {
        Complex c = FACTORY.object();
        c._real = this._real / k;
        c._imaginary = this._imaginary / k;
        return c;
    }

    /**
     * Returns this complex divided by the specified complex.
     *
     * @param  that the complex divisor.
     * @return <code>this / that</code>.
     */
    public Complex divide(Complex that) {
        double tmp = (that._real * that._real)
                + (that._imaginary * that._imaginary);
        double thatInvReal = that._real / tmp;
        double thatInvImaginary = -that._imaginary / tmp;
        Complex c = FACTORY.object();
        c._real = this._real * thatInvReal - this._imaginary * thatInvImaginary;
        c._imaginary = this._real * thatInvImaginary + this._imaginary
                * thatInvReal;
        return c;
    }

    /**
     * Returns the conjugate of this complex number.
     *
     * @return <code>(this.real(), - this.imaginary())</code>.
     */
    public Complex conjugate() {
        Complex c = FACTORY.object();
        c._real = this._real;
        c._imaginary = -this._imaginary;
        return c;
    }

    /**
     * Returns the magnitude of this complex number, also referred to
     * as the "modulus" or "length".
     *
     * @return the magnitude of this complex number.
     */
    public double magnitude() {
        return MathLib.sqrt(_real * _real + _imaginary * _imaginary);
    }

    /**
     * Returns the argument of this complex number. It is the angle
     * in radians, measured counter-clockwise from the real axis.
     *
     * @return argument of this complex number.
     */
    public double argument() {
        return MathLib.atan2(_imaginary, _real);
    }

    /**
     * Returns one of the two square root of this complex number.
     *
     * @return <code>sqrt(this)</code>.
     */
    public Complex sqrt() {
        Complex c = FACTORY.object();
        double m = MathLib.sqrt(this.magnitude());
        double a = this.argument() / 2.0;
        c._real = m * MathLib.cos(a);
        c._imaginary = m * MathLib.sin(a);
        return c;
    }

    /**
     * Returns the exponential number <i>e</i> raised to the power of
     * this complex.
     * Note: <code><i><b>e</b></i><sup><font size=+0><b>PI</b>*<i><b>i
     * </b></i></font></sup> = -1</code>
     *
     * @return  <code>exp(this)</code>.
     */
    public Complex exp() {
        Complex c = FACTORY.object();
        double m = MathLib.exp(this._real);
        c._real = m * MathLib.cos(this._imaginary);
        c._imaginary = m * MathLib.sin(this._imaginary);
        return c;
    }

    /**
     * Returns the principal natural logarithm (base e) of this complex.
     * Note: There are an infinity of solutions.
     *
     * @return  <code>log(this)</code>.
     */
    public Complex log() {
        Complex c = FACTORY.object();
        c._real = MathLib.log(this.magnitude());
        c._imaginary = this.argument();
        return c;
    }

    /**
     * Returns this complex raised to the specified power.
     *
     * @param   e the exponent.
     * @return  <code>this**e</code>.
     */
    public Complex pow(double e) {
        Complex c = FACTORY.object();
        double m = MathLib.pow(this.magnitude(), e);
        double a = this.argument() * e;
        c._real = m * MathLib.cos(a);
        c._imaginary = m * MathLib.sin(a);
        return c;
    }

    /**
     * Returns this complex raised to the power of the specified complex
     * exponent.
     *
     * @param   that the exponent.
     * @return  <code>this**that</code>.
     */
    public Complex pow(Complex that) {
        Complex c = FACTORY.object();
        double r1 = MathLib.log(this.magnitude());
        double i1 = this.argument();
        double r2 = (r1 * that._real) - (i1 * that._imaginary);
        double i2 = (r1 * that._imaginary) + (i1 * that._real);
        double m = MathLib.exp(r2);
        c._real = m * MathLib.cos(i2);
        c._imaginary = m * MathLib.sin(i2);
        return c;
    }

    /**
     * Indicates if two complexes are "sufficiently" alike to be considered
     * equal.
     *
     * @param  that the complex to compare with.
     * @param  tolerance the maximum magnitude of the difference between
     *         them before they are considered <i>not</i> equal.
     * @return <code>true</code> if they are considered equal;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Complex that, double tolerance) {
        return MathLib.abs(this.minus(that).magnitude()) <= tolerance;
    }

    /**
     * Compares this complex against the specified Object.
     *
     * @param  that the object to compare with.
     * @return <code>true</code> if the objects are the same;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        return (that instanceof Complex)
                && (this._real == ((Complex) that)._real)
                && (this._imaginary == ((Complex) that)._imaginary);
    }

    /**
     * Returns the hash code for this complex number.
     *
     * @return the hash code value.
     */
    public int hashCode() {
        int h = Float.floatToIntBits((float) _real)
                ^ Float.floatToIntBits((float) (_imaginary * MathLib.PI));
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        return h ^ (h >>> 10);
    }

    /**
     * Returns the text representation of this complex number.
     *
     * @return <code>TextFormat.getInstance(Complex.class).format(this)</code>
     * @see TextFormat#getInstance(Class)
     */
    public Text toText() {
        return TextFormat.getInstance(Complex.class).format(this);
    }

    /**
     * Returns the {@link #getReal real} component of this {@link Complex}
     * number as a <code>long</code>.
     *
     * @return <code>(long) this.getReal()</code>
     */
    public long longValue() {
        return (long) _real;
    }

    /**
     * Returns the {@link #getReal real} component of this {@link Complex}
     * number as a <code>double</code>.
     *
     * @return <code>(double) this.getReal()</code>
     */
    public double doubleValue() {
        return _real;
    }

    /**
     * Compares two complex numbers, the real components are compared first,
     * then if equal, the imaginary components.
     *
     * @param that the complex number to be compared with.
     * @return -1, 0, 1 based upon the ordering. 
     */
    public int compareTo(Complex that) {
        if (this._real < that._real)
            return -1;
        if (this._real > that._real)
            return 1;
        long l1 = Double.doubleToLongBits(this._real);
        long l2 = Double.doubleToLongBits(that._real);
        if (l1 < l2)
            return -1;
        if (l2 > l1)
            return 1;
        if (this._imaginary < that._imaginary)
            return -1;
        if (this._imaginary > that._imaginary)
            return 1;
        l1 = Double.doubleToLongBits(this._imaginary);
        l2 = Double.doubleToLongBits(that._imaginary);
        if (l1 < l2)
            return -1;
        if (l2 > l1)
            return 1;
        return 0;
    }

    /**
     * Compares the {@link #magnitude() magnitude} of this complex number
     * with the magnitude of the complex number specified.
     *
     * @param that the complex number to be compared with.
     * @return <code>|this| > |that|</code>
     */
    public boolean isLargerThan(Complex that) {
        return this.magnitude() > that.magnitude();
    }

    @Override
    public Complex copy() {
        return Complex.valueOf(_real, _imaginary);
    }

    private static final long serialVersionUID = 1L;

}