/*
 *   BracketRoot1D  -- A bracket around a root in a 1D function.
 *
 *   Copyright (C) 2010-2014 by Joseph A. Huwaldt.
 *   All rights reserved.
 *   
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
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

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the bracket around a root in a 1D function.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: October 8, 1997
 * @version April 3, 2014
 */
public class BracketRoot1D implements Comparable<BracketRoot1D> {

    /**
     * The lower bound of the bracket.
     */
    public double x1 = 0;
    
    /**
     * The upper bound of the bracket.
     */
    public double x2 = 0;

    //-----------------------------------------------------------------------------------
    /**
     * Construct a bracket around a root in a 1D function.
     *
     * @param x1 The lower bound of the bracket.
     * @param x2 The upper bound of the bracket.
     */
    public BracketRoot1D(double x1, double x2) {
        if (x1 < x2) {
            this.x1 = x1;
            this.x2 = x2;
        } else {
            this.x2 = x1;
            this.x1 = x2;
        }
    }

    /**
     * Given a function,
     * <code>func</code>, defined on the interval
     * <code>x1</code> to
     * <code>x2</code>, this routine subdivides the interval into
     * <code>n</code> equally spaced segments, and searches for zero crossings of the
     * function. Brackets around any zero crossings found are returned.
     *
     * @param func The function that is being search for zero crossings.
     * @param x1 The start of the interval to be searched.
     * @param x2 The end of the interval to be searched.
     * @param n The number segments to divide the interval into.
     * @return A list containing the brackets that were found. Could be an empty list if
     * no brackets are found.
     * @throws RootException if the Evaluatable1D throws a exception.
     */
    public static List<BracketRoot1D> findBrackets(Evaluatable1D func, double x1, double x2, int n) throws RootException {
        List<BracketRoot1D> list = new ArrayList<BracketRoot1D>();

        //	Make sure that x2 is > x1.
        if (x1 > x2) {
            double temp = x1;
            x1 = x2;
            x2 = temp;
        }

        double dx = (x2 - x1) / n;
        if (dx == 0)
            return list;

        double x = x1;
        double fp = func.function(x);
        for (int i = 0; i < n; ++i) {
            x += dx;
            if (x > x2)
                x = x2;
            double fc = func.function(x);
            if (fc * fp <= 0.0) {
                list.add(new BracketRoot1D(x - dx, x));
            }
            fp = fc;
        }

        return list;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object. This implementation compares the start of the
     * bracket (x1) only.
     */
    @Override
    public int compareTo(BracketRoot1D o) {
        return (this.x1 < o.x1 ? -1 : (this.x1 > o.x1 ? 1 : 0));
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        buffer.append(x1);
        buffer.append(",");
        buffer.append(x2);
        buffer.append('}');
        return buffer.toString();
    }
}
