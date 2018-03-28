/*
 *   Fraction  -- Represents a fractional number in rational form.
 *
 *   Copyright (C) 1999-2014 by Joseph A. Huwaldt
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
 * A rational representation of a fractional number: Whole + Numerator/Denominator.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt	Date:	December 9, 1997
 * @version April 3, 2014
 */
public class Fraction implements Cloneable {

    private long whole;		//	The whole number portion of the faction.
    private long num;		//	The numerator of the fraction.
    private long den;		//	The denominator of the fraction.
    private int sign = 1;		//	The sign of the fraction (positive, or negative) (defaults positive).

    //-----------------------------------------------------------------------------------
    /**
     * Default constructor that creates the fraction 0 0/0.
     */
    public Fraction() {
        whole = 0;
        num = 0;
        den = 0;
    }

    /**
     * Constructor that takes direct values for whole number, numerator and denominator.
     *
     * @param whole Whole number portion of the fraction.
     * @param numerator Numerator portion of the fraction.
     * @param denominator Denominator portion of the fraction.
     */
    public Fraction(long whole, long numerator, long denominator) {
        sign = 1;
        if (whole < 0) {
            sign *= -1;
            whole *= -1;
        }
        if (numerator < 0) {
            sign *= -1;
            numerator *= -1;
        }
        if (denominator < 0) {
            sign *= -1;
            denominator *= -1;
        }

        this.whole = whole;
        num = numerator;
        den = denominator;
    }

    /**
     * Constructor that takes direct values for whole number, numerator and denominator.
     *
     * @param whole Whole number portion of the fraction.
     * @param numerator Numerator portion of the fraction.
     * @param denominator Denominator portion of the fraction.
     */
    public Fraction(int whole, int numerator, int denominator) {
        sign = 1;
        if (whole < 0) {
            sign *= -1;
            whole *= -1;
        }
        if (numerator < 0) {
            sign *= -1;
            numerator *= -1;
        }
        if (denominator < 0) {
            sign *= -1;
            denominator *= -1;
        }

        this.whole = whole;
        num = numerator;
        den = denominator;
    }

    /**
     * Constructor that takes a floating point value and converts it to a mixed fraction.
     * The fraction then, at worst, approximates the floating point value to within
     * tolerance and may exactly equal it exactly.
     *
     * @param theFloat A floating point number to have converted to a rational fraction.
     * @param tolerance Tolerance to be met by the fraction relative to the float.
     */
    public Fraction(double theFloat, double tolerance) {
        if (theFloat < 0) {
            sign = -1;
            theFloat *= -1;
        } else
            sign = 1;

        floatToFraction(theFloat, tolerance);
    }

    //-----------------------------------------------------------------------------------
    /**
     * Sets the fraction to the given fractional part values.
     *
     * @param whole Whole number portion of the fraction.
     * @param numerator Numerator portion of the fraction.
     * @param denominator Denominator portion of the fraction.
     */
    public void setValue(long whole, long numerator, long denominator) {
        sign = 1;
        if (whole < 0) {
            sign *= -1;
            whole *= -1;
        }
        if (numerator < 0) {
            sign *= -1;
            numerator *= -1;
        }
        if (denominator < 0) {
            sign *= -1;
            denominator *= -1;
        }

        this.whole = whole;
        num = numerator;
        den = denominator;
    }

    /**
     * Sets this object to a mixed fraction that, at worst, approximates the input
     * floating point value to within tolerance and may exactly equal it.
     *
     * @param theFloat A floating point number to have converted to a rational fraction.
     * @param tolerance Tolerance to be met by the fraction relative to the float.
     */
    public void setValue(double theFloat, double tolerance) {
        if (theFloat < 0) {
            sign = -1;
            theFloat *= -1;
        } else
            sign = 1;

        floatToFraction(theFloat, tolerance);
    }

    /**
     * Sets the whole number portion of this fraction to the given value.
     *
     * @param whole Whole number portion of the fraction.
     */
    public void setValue(long whole) {
        sign = 1;
        if (whole < 0) {
            sign *= -1;
            whole *= -1;
        }
        this.whole = whole;
    }

    /**
     * Sets the numerator portion of this fraction to the given value.
     *
     * @param numerator The numerator portion of the fraction.
     */
    public void setNumerator(long numerator) {
        if (numerator < 0) {
            sign *= -1;
            numerator *= -1;
        }

        this.num = numerator;
    }

    /**
     * Sets the denominator portion of this fraction to the given value.
     *
     * @param denominator The denominator portion of the fraction.
     */
    public void setDenominator(long denominator) {
        if (denominator < 0) {
            sign *= -1;
            denominator *= -1;
        }

        this.den = denominator;
    }

    /**
     * Converts this fraction to a decimal fraction and returns it as a double.
     *
     * @return The value of this fraction object as a decimal fraction is returned.
     */
    public double getValue() {
        double theWhole = this.whole;
        double theNum = this.num;
        double theDen = this.den;

        return (sign * (theWhole + theNum / theDen));
    }

    /**
     * Get the whole number part of this Fraction object.
     *
     * @return Returns the whole number part of this Fraction object.
     */
    public long getWhole() {
        return sign * whole;
    }

    /**
     * Get the numerator part of this Fraction object.
     *
     * @return Returns the numerator part of this Fraction object.
     */
    public long getNumerator() {
        if (whole == 0)
            return sign * num;
        else
            return num;
    }

    /**
     * Get the denominator part of this Fraction object.
     *
     * @return Returns the denominator part of this Fraction object.
     */
    public long getDenominator() {
        return den;
    }

    /**
     * Converts this fraction to a simplified whole + num/den representation with the
     * smallest numerator and denominator as possible.
     */
    public void simplify() {
        if (num == 0 || den == 0)
            return;

        long tmp = num / den;				//	Reduce fractional part.

        whole += tmp;					//	Add any whole part created to "whole".

        long theNum = num - tmp * den;	//	Find remainder.
        long theDen = den;

        if (theNum != 0) {
            //	Find GCD
            long GCD = MathTools.greatestCommonDivisor(theNum, theDen);

            //	Reduce fraction using GCD
            num = theNum / GCD;
            den = theDen / GCD;
        } else {
            num = 0;
            den = theDen;
        }

    }

    /**
     * Converts this fraction to a mixed representation with the whole number mixed in
     * with the fraction (whole is set to 0).
     */
    public void mix() {

        long theNum = num + den * whole;
        long GCD = MathTools.greatestCommonDivisor(theNum, den);	//	Find GCD

        //	Reduce fraction using GCD
        num = theNum / GCD;
        den = den / GCD;
        whole = 0;

    }

    /**
     * Adds the input Fraction object to this Fraction object. This fraction is then
     * simplified after addition.
     *
     * @param theFraction The fraction to be added to this fraction.
     */
    public void add(Fraction theFraction) {

        //	Get a copy of the additive fraction (so that we don't mess with the original).
        Fraction addCopy = (Fraction) theFraction.clone();

        //	Mix the additive fraction.
        addCopy.mix();

        //	Mix this fraction.
        this.mix();

        //	Extract the numerators and denominators.
        long theDen = addCopy.den;
        long theNum = addCopy.num;
        long thisNum = this.num;
        long thisDen = this.den;

        //	Find a common denominator.
        thisNum *= theDen;
        theNum *= thisDen;
        thisDen *= theDen;

        //	Get the signs right.
        thisNum *= this.sign;
        theNum *= addCopy.sign;

        //	Add the two numerators (since they have a common denominator).
        thisNum += theNum;

        //	Deal with the sign.
        if (thisNum < 0) {
            sign = -1;
            thisNum *= -1;
        } else
            sign = 1;

        //	Save off the new values.
        num = thisNum;
        den = thisDen;

        this.simplify();		//	Simplify the resulting fraction.

    }

    /**
     * Adds the input whole integer value to this Fraction object. This fraction is then
     * simplified after addition.
     *
     * @param theValue The integer to be added to this fraction.
     */
    public void add(long theValue) {

        //	Mix this fraction.
        this.mix();

        //	Extract the numerator and denominator.
        long theDen = 1;
        long theNum = (theValue < 0 ? -theValue : theValue);
        long thisNum = this.num;
        long thisDen = this.den;

        //	Find a common denominator.
        thisNum *= theDen;
        theNum *= thisDen;
        thisDen *= theDen;

        //	Get the signs right.
        thisNum *= this.sign;
        if (theValue < 0)
            theNum = -theNum;

        //	Add the two numerators (since they have a common denominator).
        thisNum += theNum;

        //	Deal with the sign.
        if (thisNum < 0) {
            sign = -1;
            thisNum *= -1;
        } else
            sign = 1;

        //	Save off the new values.
        num = thisNum;
        den = thisDen;

        this.simplify();		//	Simplify the resulting fraction.

    }

    /**
     * Adds the input double value to this Fraction object. The double is at worst
     * approximated by a rational fraction to within tol. The resulting fraction is then
     * simplified after addition.
     *
     * @param theValue The double float to be added to this fraction.
     * @param tol Tolerance to be met by the fraction relative to the double.
     */
    public void add(double theValue, double tol) {
        this.add(new Fraction(theValue, tol));
    }

    /**
     * Subtracts the input Fraction object from this Fraction object. This fraction is
     * then simplified after subtraction.
     *
     * @param theFraction The fraction to be subtracted from this fraction.
     */
    public void subtract(Fraction theFraction) {

        //	Get a copy of the operator fraction (so that we don't mess with it).
        Fraction objCopy = (Fraction) theFraction.clone();

        //	Mix the operator fraction.
        objCopy.mix();

        //	Mix this fraction.
        this.mix();

        //	Extract the numerators and denominators.
        long theDen = objCopy.den;
        long theNum = objCopy.num;
        long thisNum = this.num;
        long thisDen = this.den;

        //	Find a common denominator.
        thisNum *= theDen;
        theNum *= thisDen;
        thisDen *= theDen;

        //	Get the signs right.
        thisNum *= this.sign;
        theNum *= objCopy.sign;

        //	Subtract the two numerators (since they have a common denominator).
        thisNum -= theNum;

        //	Deal with the sign.
        if (thisNum < 0) {
            sign = -1;
            thisNum *= -1;
        } else
            sign = 1;

        //	Save off the new values.
        num = thisNum;
        den = thisDen;

        this.simplify();		//	Simplify the resulting fraction.

    }

    /**
     * Multiplies this Fraction object by the input Fraction object. This fraction is then
     * simplified after subtraction.
     *
     * @param theFraction	The fraction to multiply this fraction by.
     */
    public void multiply(Fraction theFraction) {

        //	Get a copy of the multiplying fraction (so that we don't mess with it).
        Fraction objCopy = (Fraction) theFraction.clone();

        //	Mix the multiplying fraction.
        objCopy.mix();

        //	Mix this fraction.
        this.mix();

        //	Extract the numerators and denominators.
        long theDen = objCopy.den;
        long theNum = objCopy.num;
        long thisNum = this.num;
        long thisDen = this.den;

        //	Get the signs right.
        thisNum *= this.sign;
        theNum *= objCopy.sign;

        //	Multiply the two numerators.
        thisNum *= theNum;
        thisDen *= theDen;

        //	Deal with the sign.
        if (thisNum < 0) {
            sign = -1;
            thisNum *= -1;
        } else
            sign = 1;

        //	Save off the new values.
        num = thisNum;
        den = thisDen;

        this.simplify();		//	Simplify the resulting fraction.

    }

    /**
     * Multiplies this Fraction object by the input integer value. This fraction is then
     * simplified after subtraction.
     *
     * @param theValue The integer value to multiply this fraction by.
     */
    public void multiply(long theValue) {

        //	Mix this fraction.
        this.mix();

        //	Extract the numerator.
        long thisNum = this.num;

        //	Get the sign right.
        thisNum *= this.sign;

        //	Multiply the two numbers.
        thisNum *= theValue;

        //	Deal with the sign.
        if (thisNum < 0) {
            sign = -1;
            thisNum *= -1;
        } else
            sign = 1;

        //	Save off the new values.
        num = thisNum;

        this.simplify();		//	Simplify the resulting fraction.

    }

    /**
     * The result is true if and only if the argument is not null and is a Fraction object
     * that has exactly the same simple fraction value as this object.
     *
     * @param obj The object to be compared with this Fraction object for equality.
     * @return True if the argument is not null and is a Fraction object that has exactly
     * the same simple fraction value as this object.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        //	Make a copy of the two fractions so that we don't mess with the originals.
        Fraction that = (Fraction) ((Fraction) obj).clone();
        Fraction thisCopy = (Fraction) this.clone();

        //	Simplify both fractions.
        that.simplify();
        thisCopy.simplify();

        if (thisCopy.sign != that.sign)
            return false;
        if (thisCopy.whole != that.whole)
            return false;
        if (thisCopy.num != that.num)
            return false;

        return thisCopy.den == that.den;
    }

    /**
     * Returns the hash code value for this Fraction.
     *
     * @return The hash code for this fraction.
     */
    @Override
    public int hashCode() {
        Fraction thisCopy = (Fraction) this.clone();

        int hash = 5;
        hash = 47 * hash + (int) (thisCopy.whole ^ (thisCopy.whole >>> 32));
        hash = 47 * hash + (int) (thisCopy.num ^ (thisCopy.num >>> 32));
        hash = 47 * hash + (int) (thisCopy.den ^ (thisCopy.den >>> 32));
        hash = 47 * hash + thisCopy.sign;

        return hash;
    }

    /**
     * Creates a String representation of this object.
     *
     * @return The String representation of this Fraction object.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (whole != 0) {
            buf.append("");
            buf.append(sign * whole);
            if (num != 0) {
                buf.append(" ");
                buf.append(num);
                buf.append("/");
                buf.append(den);
            }
        } else {
            buf.append(sign * num);
            buf.append("/");
            buf.append(den);
        }

        return buf.toString();
    }

    /**
     * Make a copy of this Fraction.
     *
     * @return Returns a clone of this Fraction object.
     */
    @Override
    public Object clone() {
        Fraction newObject = null;

        try {
            // Make a shallow copy of this object.
            newObject = (Fraction) super.clone();

            // There are no "deep" data structures to be copied, so we are done.

        } catch (CloneNotSupportedException e) {
            // Can't happen.
            e.printStackTrace();
        }

        // Output the newly cloned object.
        return newObject;
    }

    //-----------------------------------------------------------------------------------
	/**
	 *  <p>  Approximates an arbitrary floating point number with a
	 *       rational fraction in the form numerator/denominator.
	 *       The sign is attached to the numerator.  The fraction
	 *       returned is mixed (has no whole part).  </p>
	 *
	 *  <p>  Method: Uses the method of continued fractions.  An
	 *               arbitrary decimal number can be approximated by:
	 *               theFloat = a1 + __________1_________________
	 *                                a2 + ________1_____________
	 *                                      a3 + _______1________
	 *                                            a4 + ... + 1/ak
	 *               The complete fraction can be computed from the
	 *               partial fraction using matrix math:
	 *                   | a1  1 | * | a2  1 | * | a3  1 | * ...
	 *                   | 1   0 |   | 1   0 |   | 1   0 |
	 *
	 *               Rather than keep the sequence of partial fractions,
	 *               this routine keeps only the product of the last
	 *               two:
	 *                   | Num   m01 | * | ai  1 |
	 *                   | Denom m11 |   |  1  0 |
	 *  </p>
	 *
	 *  @param  theFloat  Floating point number to be made into a
	 *                    fraction.
	 *  @param  tol       Tolerance that fraction should be created to.
	 *                    The fraction generated will have at least
	 *                    this tolerance and could be much better
	 *                    (even exact).
	 */
    private void floatToFraction(double theFloat, double tol) {
        long ai;						//	"Current" partial fraction coefficient.
        double Denom;					//	Fraction denominator (double to eliminate conversions).
        double Num;					//	The fraction numerator.
        double error = Double.MAX_VALUE;	//	Error between fraction and float, set to a big number.
        double startx = theFloat;		//	Hold this value to use in error calculation.
        double m01, m11;				//	Matrix elements used in partial fraction calculation.

        //	This method calculates a mixed fraction only.
        whole = 0;

        //	If a zero, or number to large was input, just output a zero numerator.
        if (theFloat == 0.) {
            num = 0;
            den = 0;
            return;
        } else if (theFloat >= Long.MAX_VALUE) {
            num = 0;
            den = 0;
            return;
        }


        //	Initialize the partial fraction matrix.
        Num = m11 = 1.;
        Denom = m01 = 0.;


        //	Loop, finding terms, until fraction is better than tolerance.
        while (error > tol) {
            double dx;
            double oldNum, oldDenom;

            //	Pick off the integer portion of theFloat.
            ai = (long) theFloat;

            //	Calculate the new denominator by starting the matrix multiplication process.
            oldDenom = Denom;
            Denom = oldDenom * ai + m11;

            //	Now calculate the new numerator.
            oldNum = Num;
            Num = oldNum * ai + m01;

            //	Check for numerical overflow.
            if (Num > Long.MAX_VALUE || Denom > Long.MAX_VALUE) {
                num = (long) Num;
                den = (long) Denom;
                return;
            }

            //	Calculate the error in the current fraction.
            error = Math.abs(startx - Num / Denom);

            //	On last pass, calculate fraction using a rounded rather than truncated "ai".
            if (error <= tol) {
                long r_ai = Math.round(theFloat);
                double r_Denom = oldDenom * r_ai + m11;
                double r_Num = oldNum * r_ai + m01;
                if (r_Denom < Long.MAX_VALUE && r_Num < Long.MAX_VALUE) {
                    ai = r_ai;
                    Num = r_Num;
                    Denom = r_Denom;
                    error = Math.abs(startx - Num / Denom);
                }
            }

            //	Now finish up the matrix multiplication here.
            m01 = oldNum;
            m11 = oldDenom;

            //	Subtract the whole number from the float.
            dx = (theFloat - (double) ai);

            //	Invert the new float to prepare for the next iteration.
            if (!MathTools.isApproxZero(dx))            //	Don't try and do 1/0
                theFloat = 1. / dx;

        }


        //	Output the results.
        num = (long) Num;
        den = (long) Denom;

    }

    /**
     * Used to test out the methods in this class.
     */
    public static void main(String args[]) {

        System.out.println();
        System.out.println("Testing Fraction...");

        try {
            // Create a new fraction object.
            Fraction test = new Fraction(5, -52, 8);
            System.out.println("    Fraction = " + test);

            test.simplify();
            System.out.println("    Simplified fraction = " + test);

            test.setValue(Math.PI, 0.00001);
            System.out.println("    float = " + Math.PI + "==> Fraction = " + test);

            test.simplify();
            System.out.println("    Simplified fraction = " + test);

            test.mix();
            System.out.println("    Mixed fraction = " + test);

            Fraction test2 = (Fraction) test.clone();
            test2.simplify();
            System.out.println("    Clone of \"test\" simplified = " + test2);

            if (test2.equals(test))
                System.out.println("    test and test2 are equal.");
            else
                System.out.println("    test and test2 are NOT equal.");

            test.setValue(2, 3, 8);
            test2.setValue(0, 2, 3);

            System.out.println("    New test = " + test + "   New test2 = " + test2);
            test2.multiply(test);
            System.out.println("    test2 * test = " + test2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
