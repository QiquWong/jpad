/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.economics.money;

import javolution.context.LocalContext;
import javolution.util.LocalMap;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.DerivedUnit;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

/**
 * <p> This class represents a currency {@link javax.measure.unit.Unit Unit}.
 *     Currencies are a special form of {@link DerivedUnit}, conversions
 *     between currencies is possible if their respective exchange rates 
 *     have been set and the conversion factor can be changed dynamically.</p>
 *     
 * <p> Quantities stated in {@link Currency} are usually instances of 
 *     {@link Money}.</p>
 * 
 * <p> By default, the label associated to a currency is its ISO-4217 code
 *     (see the <a href="http://www.bsi-global.com/iso4217currency"> ISO 4217
 *     maintenance agency</a> for a table of currency codes). An application may
 *     change this default using the {@link javax.measure.unit.UnitFormat#label
 *     UnitFormat.label(String)} method.
 *     For example:[code]
 *     UnitFormat.getStandardInstance().label(Currency.EUR, "€");
 *     UnitFormat.getStandardInstance().label(Currency.GBP, "£");
 *     UnitFormat.getStandardInstance().label(Currency.JPY, "¥");
 *     UnitFormat.getStandardInstance().label(Currency.USD, "$");
 *     [/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see     #setExchangeRate
 */
public class Currency extends DerivedUnit<Money> {

    /**
     * The Australian Dollar currency unit.
     */
    public static final Currency AUD = new Currency("AUD");

    /**
     * The Canadian Dollar currency unit.
     */
    public static final Currency CAD = new Currency("CAD");

    /**
     * The China Yan currency.
     */
    public static final Currency CNY = new Currency("CNY");

    /**
     * The Euro currency.
     */
    public static final Currency EUR = new Currency("EUR");

    /**
     * The British Pound currency.
     */
    public static final Currency GBP = new Currency("GBP");

    /**
     * The Japanese Yen currency.
     */
    public static final Currency JPY = new Currency("JPY");

    /**
     * The Korean Republic Won currency.
     */
    public static final Currency KRW = new Currency("KRW");

    /**
     * The Taiwanese dollar currency.
     */
    public static final Currency TWD = new Currency("TWD");

    /**
     * The United State dollar currency.
     */
    public static final Currency USD = new Currency("USD");

    /**
     * Holds the reference currency.
     */
    private static final LocalContext.Reference<Currency> 
        REFERENCE = new LocalContext.Reference<Currency>();
    
    /**
     * Holds the exchanges rate to the reference currency.
     */
    private static final LocalMap<String, Double> TO_REFERENCE = 
        new LocalMap<String, Double>();
    
    /**
     * Holds the converter to the {@link Money#BASE_UNIT money base unit}.
     */
    private final Converter _toBaseUnit;

    /**
     * Creates the currency unit for the given currency code.
     * See the <a href="http://www.bsi-global.com/iso4217currency"> ISO 4217
     * maintenance agency</a> for more information, including a table of
     * currency codes.
     *
     * @param  code the ISO-4217 code of the currency (e.g.
     *         <code>"EUR", "USD", "JPY"</code>).
     * @throws IllegalArgumentException if the specified code is not an ISO-4217
     *         code.
     */
    public Currency(String code) {
        _toBaseUnit = new Converter(code, false);
        UnitFormat.getInstance().label(this, code);
    }

    /**
     * Returns the currency code for this currency.
     *
     * @return the ISO-4217 code of the currency 
     *         (e.g. <code>"EUR", "USD", "JPY"</code>).
     */
    public String getCode() {
        return _toBaseUnit._code;
    }
    
    /**
     * Returns the default number of fraction digits used with this currency 
     * unit. For example, the default number of fraction digits for
     * the {@link Currency#EUR} is 2, while for the {@link Currency#JPY} (Yen)
     * it's 0. This method can be overriden for custom currencies returning 
     * values different from <code>2</code>.  
     *
     * @return the default number of fraction digits for this currency.
     */
    public int getDefaultFractionDigits() {
        return (this.equals(JPY) || (this.equals(KRW))) ?
                0 : 2;
    }

    /**
     * Sets the reference currency (context-local). Changing the
     * reference currency clears all the exchange rates previously set.
     *
     * @param  currency the new reference currency.
     * @see    javolution.context.LocalContext
     */
    public static void setReferenceCurrency(Currency currency) {
        REFERENCE.set(currency);
        TO_REFERENCE.clear();
        TO_REFERENCE.put(currency.getCode(), 1.0);
    }
    
    /**
     * Returns the currency used as reference when setting the exchange rate.
     * By default, the reference currency is the currency for the default
     * country locale.
     *
     * @return the reference currency.
     * @see    #setExchangeRate
     */
    public static Currency getReferenceCurrency() {
        return REFERENCE.get();
    }
    
    /**
     * Sets the exchange rate of this {@link Currency} relatively to
     * the reference currency. Setting the exchange rate allows
     * for conversion between {@link Money} stated in different currencies.
     * For example:<pre>
     *     Currency.setReferenceCurrency(Currency.USD);
     *     Currency.EUR.setExchangeRate(1.17); // 1.0 € = 1.17 $
     * </pre>
     *
     * @param  refAmount the amount stated in the {@link #getReferenceCurrency}
     *         equals to one unit of this {@link Currency}.
     * @see    #getReferenceCurrency
     */
    public void setExchangeRate(double refAmount) {
        TO_REFERENCE.put(this.getCode(), refAmount);
    }

    /**
     * Returns the exchange rate for this {@link Currency}.
     *
     * @return the amount stated in the {@link #getReferenceCurrency}
     *         equals to one unit of this {@link Currency}.
     * @throws ConversionException if the exchange rate has not be set for
     *         this {@link Currency}.
     */
    public double getExchangeRate() {
        Double refAmount = TO_REFERENCE.get(this.getCode());
        if (refAmount == null) 
              throw new ConversionException("Exchange rate not set for " + this.getCode());
        return refAmount.doubleValue();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency))
           return false;
        Currency that = (Currency) obj;
        return this._toBaseUnit.equals(that._toBaseUnit);
    }

    @Override
    public int hashCode() {
        return _toBaseUnit.hashCode();
    }    
    
    @Override
    public Unit<? super Money> getStandardUnit() {
        return Money.BASE_UNIT;
    }

    @Override
    public UnitConverter toStandardUnit() {
        return _toBaseUnit;
    }

    /**
     * This class represents the currency converters.
     */
    private static class Converter  extends UnitConverter {
        
        String _code;
        
        boolean _invert;
        
        private Converter(String code, boolean invert) {
            _code = code;
            _invert = invert;
        }

        @Override
        public UnitConverter inverse() {
            return new Converter(_code, !_invert);
        }

        @Override
        public double convert(double x) throws ConversionException {
            Double refAmount = TO_REFERENCE.get(_code);
            if (refAmount == null) 
                  throw new ConversionException("Exchange rate not set for " + _code);
            return _invert ? x / refAmount.doubleValue() : x * refAmount.doubleValue();
        }

        @Override
        public boolean isLinear() {
            return true;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Converter))
               return false;
            Converter that = (Converter) obj;
            return this._code.equals(that._code) && (this._invert == that._invert);
        }

        @Override
        public int hashCode() {
            return _invert ? _code.hashCode() : - _code.hashCode();
        }

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}