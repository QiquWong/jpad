/*
 *   PolynomialFit  -- A basis function for generating polynomial curve fits.
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
 * A basis function for generating a polynomial curve fits of degree p[].length-1 with
 * coefficients in the array p[]. This is intended for use with ModelData.fit().
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt	Date:	October 21, 2002
 * @version February 12, 2014
 */
public class PolynomialFit implements BasisFunction {

    /**
     * Method that returns the minimum number of coefficients allowed by a polynomial
     * (method returns a value of 2 corresponding to a degree=1 polynomial).
     */
    @Override
    public int getMinNumCoef() {
        return 2;
    }

    /**
     * Basis function that takes an input x and calculates the parameters of the
     * polynomial function placing them in p[].
     */
    @Override
    public void func(double x, double[] p) {
        p[0] = 1;
        int np = p.length;
        for (int j = 1; j < np; ++j) {
            p[j] = p[j - 1] * x;
        }
    }
}
