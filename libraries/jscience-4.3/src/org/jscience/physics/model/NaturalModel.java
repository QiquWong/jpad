/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.physics.model;

import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.BaseUnit;
import javax.measure.unit.Dimension;
import javax.measure.unit.SI;

/**
 * This class represents the natural model.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Planck_units">
 *      Wikipedia: Planck units</a>
 */
public  class NaturalModel extends PhysicalModel {

	/**
	 * Holds the single instance of this class.
	 */
	final static NaturalModel INSTANCE = new NaturalModel();
    
    /**
     * Holds the meter to time transform.
     */
    private static RationalConverter METRE_TO_TIME 
        = new RationalConverter(1, 299792458);
    
    /**
     * Selects the relativistic model as the current model.
     */
    public static void select() {
        throw new UnsupportedOperationException("Not implemented");
    }

    // Implements Dimension.Model
    public Dimension getDimension(BaseUnit<?> unit) {
        if (unit.equals(SI.METRE)) return Dimension.TIME;
        return Dimension.Model.STANDARD.getDimension(unit);
    }

    // Implements Dimension.Model
    public UnitConverter getTransform(BaseUnit<?> unit) {
        if (unit.equals(SI.METRE)) return METRE_TO_TIME;
        return Dimension.Model.STANDARD.getTransform(unit);
    }
//		// H_BAR (SECOND * JOULE = SECOND * (KILOGRAM / C^2 )) = 1
//		// SPEED_OF_LIGHT (METRE / SECOND) = 1
//		// BOLTZMANN (JOULE / KELVIN = (KILOGRAM / C^2 ) / KELVIN) = 1
//		// MAGNETIC CONSTANT (NEWTON / AMPERE^2) = 1
//		// GRAVITATIONAL CONSTANT (METRE^3 / KILOGRAM / SECOND^2) = 1
//		SI.SECOND.setDimension(NONE, new MultiplyConverter((c * c)
//				* MathLib.sqrt(c / (hBar * G))));
//		SI.METRE.setDimension(NONE, new MultiplyConverter(c
//				* MathLib.sqrt(c / (hBar * G))));
//		SI.KILOGRAM.setDimension(NONE, new MultiplyConverter(MathLib.sqrt(G
//				/ (hBar * c))));
//		SI.KELVIN.setDimension(NONE, new MultiplyConverter(k
//				* MathLib.sqrt(G / (hBar * c)) / (c * c)));
//		SI.AMPERE.setDimension(NONE, new MultiplyConverter(MathLib.sqrt(µ0 * G)
//				/ (c * c)));
//		SI.MOLE.setDimension(AMOUNT_OF_SUBSTANCE, Converter.IDENTITY);
//		SI.CANDELA.setDimension(LUMINOUS_INTENSITY, Converter.IDENTITY);
}
