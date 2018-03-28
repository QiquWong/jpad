/*
 *   BasisFunction  -- Interface that defines a basis function as used by ModelData.
 *
 *   Copyright (C) 2002-2014 by Joseph A. Huwaldt
 *   All rights reserved.
 *   
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *   
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *   Or visit:  http://www.gnu.org/licenses/lgpl.html
 */
package standaloneutils.mathtools;

/**
 * Defines the interface for a basis function as required by the ModelData class.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt	Date:	October 21, 2002
 * @version February 14, 2014
 */
public interface BasisFunction {

    /**
     * Method that returns the minimum number of coefficients allowed by this basis
     * function.
     */
    public int getMinNumCoef();

    /**
     * Basis function that takes an input x and calculates the parameters of the function
     * placing them in p[].
     */
    public void func(double x, double[] p);
}
