/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.physics.model;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.BaseUnit;
import javax.measure.unit.Dimension;

/**
 * This class represents the standard model. 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public class StandardModel extends PhysicalModel {

    /**
     * Holds the single instance of this class.
     */
    final static StandardModel INSTANCE = new StandardModel();

    /**
     * Default constructor (allows for derivation).
     */
    protected StandardModel() {
    }

    /**
     * Selects the standard model as the current model.
     */
    public static void select() {        
        PhysicalModel.setCurrent(INSTANCE);
    }

    // Implements Dimension.Model
    public Dimension getDimension(BaseUnit<?> unit) {
        return Dimension.Model.STANDARD.getDimension(unit);
    }

    // Implements Dimension.Model
    public UnitConverter getTransform(BaseUnit<?> unit) {
        return Dimension.Model.STANDARD.getTransform(unit);
    }

}