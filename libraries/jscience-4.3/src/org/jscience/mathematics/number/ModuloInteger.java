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

import javolution.context.LocalContext;
import javolution.context.ObjectFactory;
import javolution.text.Text;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a modulo integer. It can be used in conjonction 
 *     with the {@link org.jscience.mathematics.vector.Matrix Matrix}
 *     class to resolve modulo equations (ref. number theory).</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Modular_arithmetic">
 *      Wikipedia: Modular Arithmetic</a>
 */
public final class ModuloInteger extends Number<ModuloInteger> implements Field<ModuloInteger> {
    
    /**
     * The modulo integer representing the additive identity.
     */
    public static final ModuloInteger ZERO = new ModuloInteger();
    static {
        ZERO._value = LargeInteger.ZERO;
    }

    /**
     * The modulo integer representing the multiplicative identity.
     */
    public static final ModuloInteger ONE = new ModuloInteger();
    static {
        ONE._value = LargeInteger.ONE;
    }

    /**
     * Holds the default XML representation for modulo integers.
     * This representation consists of a simple <code>value</code> attribute
     * holding the {@link #toText() textual} representation.
     */
    static final XMLFormat<ModuloInteger> XML = new XMLFormat<ModuloInteger>(ModuloInteger.class) {
        
        @Override
        public ModuloInteger newInstance(Class<ModuloInteger> cls, InputElement xml) throws XMLStreamException {
            return ModuloInteger.valueOf(xml.getAttribute("value"));
        }
        
        public void write(ModuloInteger mi, OutputElement xml) throws XMLStreamException {
            xml.setAttribute("value", mi._value.toText());
            }

         public void read(InputElement xml, ModuloInteger mi) {
             // Nothing to do, immutable.
         }
     };

    /**
     * Holds the local modulus (for modular arithmetic).
     */
    private static final LocalContext.Reference<LargeInteger> MODULUS
        = new LocalContext.Reference<LargeInteger>();

    /**
     * Holds the large integer value.
     */
    private LargeInteger _value;

    /**
     * Returns the modulo integer having the specified value (independently of
     * the current modulo).
     * 
     * @param  value the modulo integer intrinsic value.
     * @return the corresponding modulo number.
     */
    public static ModuloInteger valueOf(LargeInteger value) {
         return ModuloInteger.newInstance(value);
    }
    /**
     * Returns the modulo integer for the specified character sequence in
     * decimal number.
     * 
     * @param chars the character sequence.
     * @return the corresponding modulo number.
     */
    public static ModuloInteger valueOf(CharSequence chars) {
        return ModuloInteger.newInstance(LargeInteger.valueOf(chars));
    }
    
    /**
     * Returns the {@link javolution.context.LocalContext local} modulus 
     * for modular arithmetic or <code>null</code> if the arithmetic operations
     * are non-modular (default). 
     * 
     * @return the local modulus or <code>null</code> if none.
     * @see #setModulus
     */
    public static LargeInteger getModulus() {
        return MODULUS.get();
    }

    /**
     * Sets the {@link javolution.context.LocalContext local} modulus 
     * for modular arithmetic.
     * 
     * @param modulus the new modulus or <code>null</code> to unset the modulus.
     * @throws IllegalArgumentException if <code>modulus &lt;= 0</code>
     */
    public static void setModulus(LargeInteger modulus) {
        if ((modulus != null) && (!modulus.isPositive()))
            throw new IllegalArgumentException("modulus: " + modulus
                    + " has to be greater than 0");
        MODULUS.set(modulus);
    }

    /**
     * Returns the current modulo value of this number. If the modulus 
     * is {@link #setModulus set} to <code>null</code> the intrinsic value
     * (the creation value) is returned.
     * 
     * @return the positive number equals to this number modulo modulus or
     *         this modulo creation value.
     */
    public LargeInteger moduloValue() {
        LargeInteger modulus = MODULUS.get();
        return (modulus == null) ? _value : _value.mod(modulus);
    }

    /**
     * Returns the text representation of the current modulo value of 
     * this number.
     *
     * @return the representation of its modulo value.
     */
    public Text toText() {
        return moduloValue().toText();
    }

    /**
     * Compares this modulo integer against the specified object
     * independently of the current modulus.
     * 
     * @param that the object to compare with.
     * @return <code>true</code> if that is a modulo number with the same 
     *         intrinsic value; <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        return (that instanceof ModuloInteger) ?
            _value.equals(((ModuloInteger) that)._value) :
            false;
    }

    /**
     * Returns the hash code for this large integer number.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        return _value.hashCode();
    }

    @Override
    public boolean isLargerThan(ModuloInteger that) {
        return _value.isLargerThan(that._value);
    }

    @Override
    public long longValue() {
        return moduloValue().longValue();
    }

    @Override
    public double doubleValue() {
        return moduloValue().doubleValue();
    }

    @Override
    public int compareTo(ModuloInteger that) {
        return _value.compareTo(that._value);
    }

    public ModuloInteger times(ModuloInteger that) {
        LargeInteger value = moduloValue().times(that.moduloValue());
        LargeInteger modulus = MODULUS.get();
        return (modulus == null) ? ModuloInteger.valueOf(value)
                : ModuloInteger.valueOf(value.mod(modulus));
    }

    public ModuloInteger plus(ModuloInteger that) {
        LargeInteger value = moduloValue().plus(that.moduloValue());
        LargeInteger modulus = MODULUS.get();
        return (modulus == null) ? ModuloInteger.valueOf(value)
                : ModuloInteger.valueOf(value.mod(modulus));
    }

    public ModuloInteger opposite() {
        LargeInteger value = moduloValue().opposite();
        LargeInteger modulus = MODULUS.get();
        return (modulus == null) ? ModuloInteger.valueOf(value)
                : ModuloInteger.valueOf(value.mod(modulus));
    }

    public ModuloInteger inverse() {
        LargeInteger modulus = MODULUS.get();
        if (modulus == null) 
            throw new ArithmeticException("Modulus not set");
        return ModuloInteger.valueOf(_value.modInverse(modulus));
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////
    
    private static ModuloInteger newInstance(LargeInteger value) {
        ModuloInteger m = FACTORY.object();
        m._value = value;
        return m;
    }
    
    private static final ObjectFactory<ModuloInteger> FACTORY = new ObjectFactory<ModuloInteger>() {
        protected ModuloInteger create() {
            return new ModuloInteger();
        }
    };
    
    private ModuloInteger() {
    }

    @Override
    public ModuloInteger copy() {
        return newInstance(_value.copy());
    }

    private static final long serialVersionUID = 1L;

}