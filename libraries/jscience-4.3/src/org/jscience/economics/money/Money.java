/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.economics.money;

import javax.measure.quantity.Quantity;
import javax.measure.unit.BaseUnit;

/**
 * This interface represents something generally accepted as a medium of 
 * exchange, a measure of value, or a means of payment. The units for money
 * quantities is of type {@link Currency}.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public interface Money extends Quantity {

    /**
     * Holds the base unit for money quantities (symbol "¤", currency symbol).
     */
    public final static BaseUnit<Money> BASE_UNIT = new BaseUnit<Money>("¤");

}