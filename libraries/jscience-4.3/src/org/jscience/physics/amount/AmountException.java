/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.physics.amount;

/**
 * Signals that an illegal measure operation has been performed.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, March 2, 2006
 */
public class AmountException extends RuntimeException {

    /**
     * Constructs a measure exception with no detail message.
     */
    public AmountException() {
        super();
    }

    /**
     * Constructs a measure exception with the specified message.
     * 
     * @param message the error message.
     */
    public AmountException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}