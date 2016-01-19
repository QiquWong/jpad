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

import javolution.context.LocalContext;

/**
 * <p> This abstract class represents a physical model. Instances of this
 *     class determinate the current quantities dimensions.</p>
 *     
 * <p> To select a model, one needs only to call the model <code>select</code>
 *     static method. For example:[code]
 *     public static void main(String[] args) {
 *          // Global (LocalContext should be used for thread-local settings).
 *          RelativisticModel.select();
 *          ...
 *     [/code]</p>
 *     
 * <p> Selecting a predefined model automatically sets the dimension of 
 *     the {@link javax.measure.unit.BaseUnit base units}.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public abstract class PhysicalModel implements Dimension.Model {

    /**
     * Holds the current physical model.
     */
    private static LocalContext.Reference<PhysicalModel> Current 
        = new LocalContext.Reference<PhysicalModel>();
    
    /**
     * Holds the dimensional model.
     */
    private static final Dimension.Model DIMENSIONAL_MODEL 
        = new Dimension.Model() {

            public Dimension getDimension(BaseUnit<?> unit) {
                return PhysicalModel.Current.get().getDimension(unit);
            }

            public UnitConverter getTransform(BaseUnit<?> unit) {
                return PhysicalModel.Current.get().getTransform(unit);
            }};
    
    /**
     * Default constructor (allows for derivation).
     */
    protected PhysicalModel() {
    }

    /**
     * Returns the current physical model (default: instance of 
     * {@link StandardModel}).
     *
     * @return the context-local physical model.
     */
    public static final PhysicalModel current() {
        PhysicalModel physicalModel = PhysicalModel.Current.get();
        return (physicalModel == null) ? StandardModel.INSTANCE : physicalModel;
    }

    /**
     * Sets the current model (this method is called when the a predefined 
     * model is selected).
     *
     * @param  model the context-local physical model.
     * @see    #current
     */
    protected static final void setCurrent(PhysicalModel model) {
        PhysicalModel.Current.set(model);
        Dimension.setModel(DIMENSIONAL_MODEL);
    }

}