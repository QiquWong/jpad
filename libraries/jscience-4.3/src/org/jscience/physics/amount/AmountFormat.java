/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.physics.amount;

import java.io.IOException;
import java.text.ParseException;

import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;

import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;
import javolution.context.LocalContext;

//@RETROWEAVER import javolution.text.Appendable;

/**
 * <p> This class provides the interface for formatting and parsing {@link 
 *     Amount measures} instances. For example:[code]
 *     // Display measurements using unscaled units (e.g. base units or alternate units).
 *     AmountFormat.setInstance(new AmountFormat() { // Context local.
 *         public Appendable format(Amount m, Appendable a) throws IOException {
 *             Unit u = m.getUnit();
 *             if (u instanceof TransformedUnit)
 *                   u = ((TransformedUnit)u).getParentUnit();
 *             return AmountFormat.getPlusMinusErrorInstance(2).format(m.to(u), a);
 *         }
 *         public Amount parse(CharSequence csq, Cursor c) {
 *             return AmountFormat.getPlusMinusErrorInstance(2).parse(csq, c);
 *         }
 *     });[/code]
 *     </p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 21, 2006
 */
public abstract class AmountFormat extends TextFormat<Amount<?>> {

    /**
     * Holds current format.
     */
    private static final LocalContext.Reference<AmountFormat> CURRENT = new LocalContext.Reference<AmountFormat>(
            new PlusMinusError(2));

    /**
     * Default constructor.
     */
    protected AmountFormat() {
    }

    /**
     * Returns the current {@link javolution.context.LocalContext local}  
     * format (default <code>AmountFormat.getPlusMinusErrorInstance(2)</code>).
     *
     * @return the context local format.
     * @see #getPlusMinusErrorInstance(int)
     */
    public static AmountFormat getInstance() {
        return CURRENT.get();
    }

    /**
     * Sets the current {@link javolution.context.LocalContext local} format.
     *
     * @param format the new format.
     */
    public static void setInstance(AmountFormat format) {
        CURRENT.set(format);
    }

    /**
     * Returns a format for which the error (if present) is stated using 
     * the '±' character; for example <code>"(1.34 ± 0.01) m"</code>.
     * This format can be used for formatting as well as for parsing.
     * 
     * @param digitsInError the maximum number of digits in error.
     */
    public static AmountFormat getPlusMinusErrorInstance(int digitsInError) {
        return new PlusMinusError(digitsInError);
    }

    /**
     * Returns a format for which the error is represented by an integer 
     * value in brackets; for example <code>"1.3456[20] m"</code> 
     * is equivalent to <code>"1.3456 ± 0.0020 m"</code>. 
     * This format can be used for formatting as well as for parsing.
     * 
     * @param digitsInError the maximum number of digits in error.
     */
    public static AmountFormat getBracketErrorInstance(int digitsInError) {
        return new BracketError(digitsInError);
    }

    /**
     * Returns a format for which only digits guaranteed to be exact are 
     * written out. In other words, the error is always on the
     * last digit and less than the last digit weight. For example,
     * <code>"1.34 m"</code> means a length between <code>1.32 m</code> and
     * <code>1.35 m</code>. This format can be used for formatting only.
     */
    public static AmountFormat getExactDigitsInstance() {
        return new ExactDigitsOnly();
    }

    /**
     * This class represents the plus minus error format.
     */
    private static class PlusMinusError extends AmountFormat {

        /**
         * Holds the number of digits in error.
         */
        private int _errorDigits;

        /**
         * Creates a plus-minus error format having the specified 
         * number of digits in error.
         * 
         * @param errorDigits the number of digits in error.
         */
        private PlusMinusError(int errorDigits) {
            _errorDigits = errorDigits;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Appendable format(Amount arg0, Appendable arg1)
                throws IOException {
            if (arg0.getUnit() instanceof Currency)
                return formatMoney(arg0, arg1);
            if (arg0.isExact()) {
                TypeFormat.format(arg0.getExactValue(), arg1);
                arg1.append(' ');
                return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
            }
            double value = arg0.getEstimatedValue();
            double error = arg0.getAbsoluteError();
            int log10Value = (int) MathLib.floor(MathLib.log10(MathLib
                    .abs(value)));
            int log10Error = (int) MathLib.floor(MathLib.log10(error));
            int digits = log10Value - log10Error - 1; // Exact digits.
            digits = MathLib.max(1, digits + _errorDigits);

            boolean scientific = (MathLib.abs(value) >= 1E6)
                    || (MathLib.abs(value) < 1E-6);
            boolean showZeros = false;
            arg1.append('(');
            TypeFormat.format(value, digits, scientific, showZeros, arg1);
            arg1.append(" ± ");
            scientific = (MathLib.abs(error) >= 1E6)
                    || (MathLib.abs(error) < 1E-6);
            showZeros = true;
            TypeFormat.format(error, _errorDigits, scientific, showZeros, arg1);
            arg1.append(") ");
            return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
        }

        @Override
        public Amount<?> parse(CharSequence arg0, Cursor arg1) {
            int start = arg1.getIndex();
            try {
                arg1.skip('(', arg0);
                long value = TypeFormat.parseLong(arg0, 10, arg1);
                if (arg0.charAt(arg1.getIndex()) == ' ') { // Exact! 
                    arg1.skip(' ', arg0);
                    Unit<?> unit = UnitFormat.getInstance().parseProductUnit(
                            arg0, arg1);
                    return Amount.valueOf(value, unit);
                }
                arg1.setIndex(start);
                double amount = TypeFormat.parseDouble(arg0, arg1);
                arg1.skip(' ', arg0);
                double error = 0;
                if (arg0.charAt(arg1.getIndex()) == '±') { // Error specified. 
                    arg1.skip('±', arg0);
                    arg1.skip(' ', arg0);
                    error = TypeFormat.parseDouble(arg0, arg1);
                }
                arg1.skip(')', arg0);
                arg1.skip(' ', arg0);
                Unit<?> unit = UnitFormat.getInstance().parseProductUnit(arg0,
                        arg1);
                return Amount.valueOf(amount, error, unit);
            } catch (ParseException e) {
                arg1.setIndex(start);
                arg1.setErrorIndex(e.getErrorOffset());
                return null;
            }
        }
    }

    /**
     * This class represents the bracket error format.
     */
    private static class BracketError extends AmountFormat {

        /**
         * Holds the number of digits in error.
         */
        private int _errorDigits;

        /**
         * Creates a bracket error format having the specified 
         * number of digits in error.
         * 
         * @param bracket the number of digits in error.
         */
        private BracketError(int errorDigits) {
            _errorDigits = errorDigits;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Appendable format(Amount arg0, Appendable arg1)
                throws IOException {
            if (arg0.getUnit() instanceof Currency)
                return formatMoney(arg0, arg1);
            if (arg0.isExact()) {
                TypeFormat.format(arg0.getExactValue(), arg1);
                arg1.append(' ');
                return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
            }
            double value = arg0.getEstimatedValue();
            double error = arg0.getAbsoluteError();
            int log10Value = (int) MathLib.floor(MathLib.log10(MathLib
                    .abs(value)));
            int log10Error = (int) MathLib.floor(MathLib.log10(error));
            int digits = log10Value - log10Error - 1; // Exact digits.
            digits = MathLib.max(1, digits + _errorDigits);

            boolean scientific = (MathLib.abs(value) >= 1E6)
                    || (MathLib.abs(value) < 1E-6);
            boolean showZeros = true;
            TextBuilder tb = TextBuilder.newInstance();
            TypeFormat.format(value, digits, scientific, showZeros, tb);
            int endMantissa = 0;
            for (; endMantissa < tb.length(); endMantissa++) {
                if (tb.charAt(endMantissa) == 'E')
                    break;
            }
            int bracketError = (int) (error * MathLib.toDoublePow10(1,
                    -log10Error + _errorDigits - 1));
            tb.insert(endMantissa, Text.valueOf('[').plus(
                    Text.valueOf(bracketError)).plus(']'));
            arg1.append(tb);
            arg1.append(' ');
            return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
        }

        @Override
        public Amount<?> parse(CharSequence arg0, Cursor arg1) {
            // TBD
            throw new UnsupportedOperationException("Not supported yet");
        }
    }

    /**
     * This class represents the exact digits only format.
     */
    private static class ExactDigitsOnly extends AmountFormat {

        /**
         * Default constructor.
         */
        private ExactDigitsOnly() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public Appendable format(Amount arg0, Appendable arg1)
                throws IOException {
            if (arg0.getUnit() instanceof Currency)
                return formatMoney(arg0, arg1);
            if (arg0.isExact()) {
                TypeFormat.format(arg0.getExactValue(), arg1);
                arg1.append(' ');
                return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
            }
            double value = arg0.getEstimatedValue();
            double error = arg0.getAbsoluteError();
            int log10Value = (int) MathLib.floor(MathLib.log10(MathLib
                    .abs(value)));
            int log10Error = (int) MathLib.floor(MathLib.log10(error));
            int digits = log10Value - log10Error - 1; // Exact digits.

            boolean scientific = (MathLib.abs(value) >= 1E6)
                    || (MathLib.abs(value) < 1E-6);
            boolean showZeros = true;
            TypeFormat.format(value, digits, scientific, showZeros, arg1);
            arg1.append(' ');
            return UnitFormat.getInstance().format(arg0.getUnit(), arg1);
        }

        @Override
        public Amount<?> parse(CharSequence arg0, Cursor arg1) {
            throw new UnsupportedOperationException(
                    "This format should not be used for parsing "
                            + "(not enough information on the error");
        }
    }

    /**
     * Provides custom formatting for money measurements.
     */
    private static Appendable formatMoney(Amount<Money> arg0, Appendable arg1)
            throws IOException {
        Currency currency = (Currency) arg0.getUnit();
        int fraction = currency.getDefaultFractionDigits();
        if (fraction == 0) {
            long amount = arg0.longValue(currency);
            TypeFormat.format(amount, arg1);
        } else if (fraction == 2) {
            long amount = MathLib.round(arg0.doubleValue(arg0.getUnit()) * 100);
            TypeFormat.format(amount / 100, arg1);
            arg1.append('.');
            arg1.append((char) ('0' + (amount % 100) / 10));
            arg1.append((char) ('0' + (amount % 10)));
        } else {
            throw new UnsupportedOperationException();
        }
        arg1.append(' ');
        return UnitFormat.getInstance().format(currency, arg1);
    }

}