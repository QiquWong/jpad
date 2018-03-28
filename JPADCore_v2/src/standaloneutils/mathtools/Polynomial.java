/*
 *   Polynomial  -- A class that represents a polynomial equation.
 *
 *   Copyright (C) 2000-2014 by Joseph A. Huwaldt
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

import static java.lang.Math.*;


/**
 *  <p> Represents a polynomial equation of the form
 *      p(x) = A + B*x + C*x^2 + D*x^3 + ...
 *      where A, B, C, etc are either real or complex coefficients.
 *  </p>
 *  <p> Includes a method for finding the zeros of a complex polynomial
 *      by the three stage complex algorithm of Jenkins and Traub.  The
 *      method finds the zeros one at a time in roughly increasing
 *      order of modulus and deflates the polynomial to one of lower
 *      degree.  This method is extremely fast and timing is quite
 *      insensitive to distribution of zeros.
 *  </p>
 *
 *  <p> Zero finder ported from FORTRAN version of algorithm 419
 *      courtesy <a href="http://www.netlib.org/">Netlib Repository</a>.
 *  </p>
 *  
 *  <p> This class is not thread-safe. </p>
 *
 *  <p>  Modified by:  Joseph A. Huwaldt </p>
 *
 *  @author  Joseph A. Huwaldt	Date:	July 15, 2000
 *  @version April 3, 2014
 */
public class Polynomial implements Cloneable, java.io.Serializable {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -2679960976162995156L;

	//  The base of the number system being used.
	private static final double BASE = 2;
	
	//  The number of base digits in each floating-point number (double precision)
	private static final double kT = 53;
	
	//  The largest exponent in the number system.
//	private static final double kM = 1024;
	
	//  The maximum relative representation error.  Fortran code:  BASE**(1-T)
	private static final double ETA = pow(BASE, 1 - kT);
	
	//  Infinity.  FORTRAN code:  BASE*(1.0D0-BASE**(-T))*BASE**(M-1)
	private static final double INFIN = Double.MAX_VALUE;
	
	//  The smallest number that can be represented.  Fortran code:  (BASE**(N+3))/BASE**3
	private static final double SMALNO = Double.MIN_VALUE;
	
	//  Error bounds on complex addition.
	private static final double ARE = ETA;
	
	//  Error bounds on complex multiplication.
	private static final double MRE = 2*sqrt(2.)*ETA;
	
	private static final double COSR = cos(94*PI/180);
	private static final double SINR = sin(94*PI/180);
	
	// Array of complex coefficients in order of increasing power.
	private Complex[] coef;
	
	
    // ****   The following are used only by the root finding routines.   ****
    // Temporary storage space for complex numbers with real and imaginary parts.
    private transient double PVr, PVi, Tr, Ti, Sr, Si, Zr, Zi;

    //-----------------------------------------------------------------------------------
    /**
     * Default constructor that creates the polynomial p(x) = 0.
     */
    public Polynomial() {
    }

    /**
     * Constructor that takes an array of complex coefficients. Coefficients are supplied
     * in order of increasing power. Example: p(x) = A + B*x + C*x^2 + D*x^3 + E*x^4 gives
     * coefficients[] = {A, B, C, D, E}.
     *
     * @param coefficients An array of complex coefficients in order of increasing power.
     */
    public Polynomial(Complex[] coefficients) {
        coef = (Complex[]) coefficients.clone();
    }

    /**
     * Constructor that takes an array of real coefficients. Coefficients are supplied in
     * order of increasing power. Example: p(x) = A + B*x + C*x^2 + D*x^3 + E*x^4 gives
     * coefficients[] = {A, B, C, D, E}.
     *
     * @param coefficients An array of real coefficients in order of increasing power.
     */
    public Polynomial(double[] coefficients) {

        int length = coefficients.length;

        // Allocate memory for complex representation of coefficients.
        coef = new Complex[length];

        // Create a complex coefficient with only a real part for each coefficient.
        for (int i = 0; i < length; ++i) {
            coef[i] = new Complex(coefficients[i]);
        }

    }

    //-----------------------------------------------------------------------------------
    /**
     * Sets the complex coefficients to the given array of values. Coefficients are
     * supplied in order of increasing power. Example: p(x) = A + B*x + C*x^2 + D*x^3 +
     * E*x^4 gives coefficients[] = {A, B, C, D, E}.
     *
     * @param coefficients An array of complex coefficients in order of increasing power.
     */
    public void setCoefficients(Complex[] coefficients) {
        coef = (Complex[]) coefficients.clone();
    }

    /**
     * Sets the real coefficients to the given array of values. Coefficients are supplied
     * in order of increasing power. Example: p(x) = A + B*x + C*x^2 + D*x^3 + E*x^4 gives
     * coefficients[] = {A, B, C, D, E}.
     *
     * @param coefficients An array of real coefficients in order of increasing power.
     */
    public void setCoefficients(double[] coefficients) {

        int length = coefficients.length;

        // Allocate memory for complex representation of coefficients.
        coef = new Complex[length];

        // Create a complex coefficient with only a real part for each coefficient.
        for (int i = 0; i < length; ++i) {
            coef[i] = new Complex(coefficients[i]);
        }

    }

    /**
     * Returns a reference to the generally complex coefficients for this
     * polynomial.Coefficients are provided in order of increasing power. Example: p(x) =
     * A + B*x + C*x^2 + D*x^3 + E*x^4 gives coefficients[] = {A, B, C, D, E}.
     *
     * @return Reference to an array of complex coefficients in order of decreasing power.
     */
    public Complex[] getCoefficients() {
        return coef;
    }

    /**
     * Evaluate this polynomial at a complex x and return the generally complex result.
     *
     * @param x The generally Complex point at which to evaluate this polynomial.
     * @return The evaluation of this polynomial at x.
     */
    public Complex evaluate(Complex x) {
        return evaluate(x.re(), x.im());
    }

    /**
     * Evaluate this polynomial at a real x and return the generally complex result.
     *
     * @param x The real point at which to evaluate this polynomial.
     * @return The evaluation of this polynomial at x.
     */
    public Complex evaluate(double x) {
        return evaluate(x, 0.);
    }

    /**
     * Evaluate this polynomial at a complex x and returns the generally complex result.
     * Uses the method of Horner Recurrence.
     *
     * @param xr The real component of the point at which to evaluate this polynomial.
     * @param xi The imaginary component of the point at which to evaluate this
     * polynomial.
     * @return The evaluation of this polynomial at the complex x.
     */
    public Complex evaluate(double xr, double xi) {
        if (coef == null)
            return new Complex(0., 0.);

        int NN = coef.length;

        // Begin evaluation.
        double pvr = coef[NN - 1].re();
        double pvi = coef[NN - 1].im();
        for (int i = NN - 2; i >= 0; --i) {
            // real:     pv = pv*x + c[i]
            // Complex:  pv = pv.mul(x).add(coef[i]);
            double temp = pvr;
            pvr = pvr * xr - pvi * xi + coef[i].re();
            pvi = temp * xi + pvi * xr + coef[i].im();
        }

        return new Complex(pvr, pvi);
    }

    /**
     * Evaluate the slope "dP(x)/dx" of this polynomial at a complex x and return the
     * generally complex result.
     *
     * @param x The generally Complex point at which to evaluate this polynomial.
     * @return The slope "dP(x)/dx" of this polynomial at the complex x.
     */
    public Complex slope(Complex x) {
        return slope(x.re(), x.im());
    }

    /**
     * Evaluate the slope "dP(x)/dx" of this polynomial at a real x and return the
     * generally complex result.
     *
     * @param x The real point at which to evaluate this polynomial.
     * @return The slope "dP(x)/dx" of this polynomial at the complex x.
     */
    public Complex slope(double x) {
        return slope(x, 0.);
    }

    /**
     * Evaluate the slope "dP(x)/dx" of this polynomial at a complex x and return the
     * generally complex result. Uses the method of Horner Recurrence with added slope
     * calculation.
     *
     * @param xr The real component of the point at which to evaluate this polynomial.
     * @param xi The imaginary component of the point at which to evaluate this
     * polynomial.
     * @return The slope "dP(x)/dx" of this polynomial at the complex x.
     */
    public Complex slope(double xr, double xi) {
        if (coef == null)
            return new Complex(0., 0.);

        int NN = coef.length;

        // Begin evaluation.
        double dpr = coef[NN - 1].re() * (NN - 1);
        double dpi = coef[NN - 1].im() * (NN - 1);
        for (int i = NN - 2; i >= 1; --i) {
            // real:     dp = dp*x + p
            double temp = dpr;
            dpr = dpr * xr - dpi * xi + i * coef[i].re();
            dpi = temp * xi + dpi * xr + i * coef[i].im();
        }

        return new Complex(dpr, dpi);
    }

	/**
	*  Finds all the roots or zeros of this polynomial.  Zeros may be
	*  real or complex.
	*
	*  Uses the standard black-box method of Jenkins & Traub.
	*  This method finds the polynomial zeros one
	*  at a time in roughly increasing order of modulus.  The algorithm
	*  is extremely fast and is relatively insensitive to the
	*  distribution of zeros.  The algorithm is also known from
	*  historical applications to be very reliable.
	*
	*  @return  An array of all the generally complex zeros
	*           or roots of this polynomial.
	*  @throws RootException if there is a problem finding the zeros of the
	*         polynomial.
	*/
    public Complex[] zeros() throws RootException {
        Complex[] zeros = null;

        if (coef != null && coef.length > 0) {
            //	Create arrays of coefficients required by the root finder.
            int NN = coef.length;
            double[] Pr = new double[NN];
            double[] Pi = new double[NN];
            for (int i = 0; i < NN; ++i) {
                Pr[i] = coef[NN - 1 - i].re();
                Pi[i] = coef[NN - 1 - i].im();
            }

            //  Call the Jacob & Traub zero finder.
            zeros = cpoly(Pr, Pi);
        }

        return zeros;
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = 7;

        //  Use the 1st 3 terms (if there are that many) of the polynomial.
        int length = coef.length;
        for (int i = 0; i < length; ++i) {
            hash = hash * 31 + coef[i].hashCode();
        }

        return hash;
    }

    /**
     * The result is true if and only if the argument is not null and is a Polynomial
     * object that has exactly the same coefficients as this object.
     *
     * @param obj The object to be compared with this Polynomial object for equality.
     * @return True if the argument is not null and is a Polynomial object that has
     * exactly the same coefficients as this object.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        Polynomial that = (Polynomial) obj;
        if (this.coef == that.coef)
            return true;
        if (this.coef == null) {
            if (that.coef == null)
                return true;
            else
                return false;
        }
        if (that.coef == null)
            return false;
        if (this.coef.length != that.coef.length)
            return false;

        // Compare each coefficient.
        int length = this.coef.length;
        for (int i = 0; i < length; ++i) {
            if (!coef[i].equals(that.coef[i]))
                return false;
        }

        return true;
    }

    /**
     * Creates a String representation of this polynomial.
     *
     * @return The String representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        if (coef != null) {
            int NN = coef.length;

            //  Output constant.
            if (coef[0].abs() > 0.) {
                if (coef[0].im() != 0.)
                    buffer.append(coef[0].toString());
                else
                    buffer.append(coef[0].re());
            }

            if (NN > 1) {
                //  Output term times X.
                if (coef[1].abs() > 0.) {
                    buffer.append(" + ");
                    if (coef[1].im() != 0.)
                        buffer.append(coef[1].toString());
                    else
                        buffer.append(coef[1].re());
                    buffer.append("*x");
                }

                //  Output higher terms.
                for (int i = 2; i < NN; ++i) {
                    if (coef[i].abs() > 0.) {
                        buffer.append(" + ");
                        if (coef[i].im() != 0.)
                            buffer.append(coef[i].toString());
                        else
                            buffer.append(coef[i].re());
                        buffer.append("*x^");
                        buffer.append(i);
                    }
                }
            }

        } else
            // No coefficients, so polynomial == 0.
            buffer.append("0.");

        return buffer.toString();
    }

    /**
     * Make a copy of this Polynomial object.
     *
     * @return Returns a clone of this Polynomial object.
     */
    @Override
    public Object clone() {
        Polynomial newObject = null;

        try {
            // Make a shallow copy of this object.
            newObject = (Polynomial) super.clone();

            // Now make a deep copy of the data contained in this object.
            // The class Complex does not have a publicly accessible clone()
            // method, so we have to create new Complex objects from scratch.
            int length = coef.length;
            newObject.coef = new Complex[length];
            for (int i = 0; i < length; ++i) {
                newObject.coef[i] = new Complex(coef[i].re(), coef[i].im());
            }

        } catch (CloneNotSupportedException e) {
            // Can't happen.
            e.printStackTrace();
        }

        // Output the newly cloned object.
        return newObject;
    }

    /**
     * Used to test out the methods in this class.
     */
    public static void main(String args[]) {

        System.out.println();
        System.out.println("Testing Polynomial...");

        try {
            // Evaluate p(x) = x^2 + 2*x + 1.
            double[] coef = new double[3];
            coef[0] = 1.;
            coef[1] = 2.;
            coef[2] = 1.;
            Polynomial poly = new Polynomial(coef);

            System.out.println("    EXAMPLE 0:  A VERY SIMPLE POLYNOMIAL.");
            System.out.println("    p(x) = " + poly);
            System.out.println("    p(5) = " + poly.evaluate(5.));
            System.out.println("    Zeros of p(x) are:");
            Complex[] zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("    EXAMPLE 1.  POLYNOMIAL WITH ZEROS 1,2,...,10.");
            coef = new double[11];
            coef[10] = 1;
            coef[9] = -55;
            coef[8] = 1320;
            coef[7] = -18150;
            coef[6] = 157773;
            coef[5] = -902055;
            coef[4] = 3416930;
            coef[3] = -8409500;
            coef[2] = 12753576;
            coef[1] = -10628640;
            coef[0] = 3628800;
            poly = new Polynomial(coef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("    EXAMPLE 2. ZEROS ON IMAGINARY AXIS DEGREE 3.");
            Complex[] ccoef = new Complex[4];
            ccoef[3] = new Complex(1, 0);
            ccoef[2] = new Complex(0, -10001.0001);
            ccoef[1] = new Complex(-10001.0001, 0);
            ccoef[0] = new Complex(0, 1);
            poly = new Polynomial(ccoef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("    EXAMPLE 3. ZEROS AT 1+I,1/2*(1+I)....1/(2**-9)*(1+I).");
            ccoef = new Complex[11];
            ccoef[10] = new Complex(1., 0.);
            ccoef[9] = new Complex(-1.998046875, -1.998046875);
            ccoef[8] = new Complex(0., 2.658859252929688);
            ccoef[7] = new Complex(0.7567065954208374, -7.567065954208374E-1);
            ccoef[6] = new Complex(-0.2002119533717632, 0.);
            ccoef[5] = new Complex(1.271507365163416E-2, 1.271507365163416E-2);
            ccoef[4] = new Complex(0., -7.820779428584501E-4);
            ccoef[3] = new Complex(-1.154642632172909E-5, 1.154642632172909E-5);
            ccoef[2] = new Complex(1.584803612786345E-7, 0.);
            ccoef[1] = new Complex(-4.652065399568528E-10, -4.652065399568528E-10);
            ccoef[0] = new Complex(0., 9.094947017729282E-13);
            poly = new Polynomial(ccoef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("    EXAMPLE 4. MULTIPLE ZEROS.");
            ccoef[10] = new Complex(1, 0);
            ccoef[9] = new Complex(-10, -10);
            ccoef[8] = new Complex(3, 100);
            ccoef[7] = new Complex(284, -334);
            ccoef[6] = new Complex(-1293, 200);
            ccoef[5] = new Complex(2374, 1394);
            ccoef[4] = new Complex(-1587, -3836);
            ccoef[3] = new Complex(-920, 4334);
            ccoef[2] = new Complex(2204, -2352);
            ccoef[1] = new Complex(-1344, 504);
            ccoef[0] = new Complex(288, 0.);
            poly = new Polynomial(ccoef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("    EXAMPLE 5. 12 ZEROS EVENLY DISTRIBUTED ON A CIRCLE OF RADIUS"
                    + " 1 CENTERED AT 0+2I.");
            ccoef = new Complex[13];
            ccoef[12] = new Complex(1, 0);
            ccoef[11] = new Complex(0, -24);
            ccoef[10] = new Complex(-264, 0);
            ccoef[9] = new Complex(0, 1760);
            ccoef[8] = new Complex(7920, 0);
            ccoef[7] = new Complex(0, -25344);
            ccoef[6] = new Complex(-59136, 0);
            ccoef[5] = new Complex(0, 101376);
            ccoef[4] = new Complex(126720, 0);
            ccoef[3] = new Complex(0, -112640);
            ccoef[2] = new Complex(-67584, 0);
            ccoef[1] = new Complex(0, 24576);
            ccoef[0] = new Complex(4095, 0);
            poly = new Polynomial(ccoef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

            System.out.println("  Another simple example:  fx = -1 + 1*x^4:");
            coef = new double[5];
            coef[0] = -1;
            coef[4] = 1;
            poly = new Polynomial(coef);
            System.out.println("    p(x) = " + poly);
            zeros = poly.zeros();
            for (int i = 0; i < zeros.length; ++i) {
                System.out.println("        " + zeros[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

	//-----------------------------------------------------------------------------------
	/**
	*  <p> Finds all the zeros of a complex polynomial.  </p>
	*
	*  <p> This program finds all the zeros of a complex polynomial by the
	*      three stage complex algorithm of Jenkins and Traub.  The
	*      program finds the zeros one at a time in roughly increasing
	*      order of modulus and deflates the polynomial to one of lower
	*      degree.  The program is extremely fast and timing is quite
	*      insensitive to distribution of zeros.
	*  </p>
	*
	*  <p> Ported from FORTRAN to Java by Joseph A. Huwaldt, July 20, 2000  </p>
	*
	*  <p> FORTRAN version from <a href="http://www.netlib.org/">Netlib Repository</a>
	*      where it is listed as "419.f".  A PDF file describing the algorithm
	*      and it's history with references is also available from Netlib.
	*  </p>
	*
	*  <p> FORTRAN version had the following note at the top.
	*      ALGORITHM 419 COLLECTED ALGORITHMS FROM ACM.
	*      ALGORITHM APPEARED IN COMM. ACM, VOL. 15, NO. 02, P. 097.
	*      Original Algol 60 zpolyzerofinder version by Jenkins, 1969.
	*      (as noted in PDF scan of original ACM document).
	*  </p>
	*
	*  @param  Pr  Array of real part of the polynomial coefficients
	*              in order of decreasing powers.  Will contain garbage
	*              after calling this method!
	*  @param  Pi  Array of imaginary part of the polynomial coefficients
	*              in order of decreasing powers.  Will contain garbage
	*              after calling this method!
	*  @return An array of generally complex zeros of the polynomial.
	*  @throws RootException if this routine was unable to locate the
	*          polynomial's zeros.
	*/
    protected Complex[] cpoly(double[] Pr, double[] Pi) throws RootException {
        int NN = Pr.length;
        int degree = NN - 1;

        //  Algorithm fails if the leading coefficient is zero.
        if (Pr[0] == 0. && Pi[0] == 0.)
            throw new RootException("Leading coefficient is zero.");

        //  Allocate memory for arrays used by this method.
        double[] Hr = new double[NN];
        double[] Hi = new double[NN];
        double[] QPr = new double[NN];
        double[] QPi = new double[NN];
        double[] QHr = new double[NN];
        double[] QHi = new double[NN];
        double[] SHr = new double[NN];
        double[] SHi = new double[NN];
        Complex[] zeros = new Complex[degree];

        //  Initialization of variables.
        double XX = sqrt(2.) / 2.;
        double YY = -XX;
        int idNN2;

        //  Remove zeros at the origin, if any.
        while (Pr[NN - 1] == 0. && Pi[NN - 1] == 0.) {
            idNN2 = degree - NN + 1;
            zeros[idNN2] = new Complex(0., 0.);
            --NN;
        }

        //  Calculate the modulus of the coefficients.
        for (int i = 0; i < NN; ++i) {
            SHr[i] = cmod(Pr[i], Pi[i]);
        }

        //  Scale the polynomial if needed.
        double bound = scale(NN, SHr);
        if (bound != 1.0) {
            for (int i = 0; i < NN; ++i) {
                Pr[i] *= bound;
                Pi[i] *= bound;
            }
        }

        //  Start the algorithm for one zero.
        out:
        while (true) {
            if (NN <= 2) {
                // Calculate the final zero and return.
                cdiv(-Pr[1], -Pi[1], Pr[0], Pi[0]);			// Outputs Tr, Ti.
                zeros[degree - 1] = new Complex(Tr, Ti);
                return zeros;
            }

            //  Calculate a lower bound on the modulus of the zeros.
            for (int i = 0; i < NN; ++i) {
                SHr[i] = cmod(Pr[i], Pi[i]);
            }

            bound = cauchy(NN, SHr, SHi);

            // Outer loop to control 2 major passes with different sequences of shifts.
            for (int cnt1 = 0; cnt1 < 2; ++cnt1) {

                //	First stage calculation, no shift.
                noShift(NN, 5, Pr, Pi, Hr, Hi);

                //  Inner loop to select a shift.
                for (int cnt2 = 0; cnt2 < 9; ++cnt2) {
                    //  Shift is chosen with a modulus bound and amplitude rotated
                    //  by 94 degrees from the previous shift.
                    double XXX = COSR * XX - SINR * YY;
                    YY = SINR * XX - COSR * YY;
                    XX = XXX;
                    Sr = bound * XX;
                    Si = bound * YY;

                    //  Second stage calculation, fixed shift.
                    boolean conv = fxShift(NN, 10 * cnt2, Pr, Pi, QPr, QPi, Hr, Hi,
                            QHr, QHi, SHr, SHi);		// Outputs Zr, Zi.
                    if (conv == true) {
                        //  If successful the zero is stored and the polynomial deflated.
                        idNN2 = degree - NN + 1;
                        zeros[idNN2] = new Complex(Zr, Zi);
                        --NN;
                        for (int i = 0; i < NN; ++i) {
                            Pr[i] = QPr[i];
                            Pi[i] = QPi[i];
                        }

                        // The 2nd stage jumps directly back to 3rd stage iteration.
                        continue out;
                    }

                    //  If iteration is unsuccessful, another shift is chosen.
                }

                //  If 9 shifts fail, the outer loop is repeated with another
                //  sequence of shifts.
            }

            //  The zero finder has failed on two major passes.  Return empty handed.
            throw new RootException("Found fewer than " + degree + " zeros.");
        }

        // We can never get here.
//		return null;
    }

    /**
     * Evaluate this polynomial at a complex x and returns the generally complex result as
     * a pair of class variables. Class variables are used to avoid the overhead of
     * creating a "Complex" object during root finding. Sets the class variables PVr, and
     * PVi with the real and imaginary parts of the result. Uses the method of Horner
     * Recurrence.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param Sr The real component of the point at which to evaluate this polynomial.
     * @param Si The imaginary component of the point at which to evaluate this
     * polynomial.
     * @param Pr, Pi Real & Imaginary coefficients of the polynomial.
     * @param Qr, Qi Arrays to contain partial sums.
     */
    private void polyEv(int NN, double Sr, double Si, double[] Pr, double[] Pi,
            double[] Qr, double[] Qi) {
        // Begin evaluation.
        double pvr = Qr[0] = Pr[0];
        double pvi = Qi[0] = Pi[0];

        for (int i = 1; i < NN; ++i) {
            double temp = pvr;
            pvr = pvr * Sr - pvi * Si + Pr[i];
            pvi = temp * Si + pvi * Sr + Pi[i];

            Qr[i] = pvr;
            Qi[i] = pvi;
        }

        // Use a class variable to pass results back when doing root finding.
        PVr = pvr;
        PVi = pvi;
    }

    /**
     * Bounds the error in evaluating the polynomial by the method of Horner Recurrence.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param Qr Real part of partial sum from evaluate().
     * @param Qi Imaginary part of partial sum from evaluate().
     * @param MS Modulus of the point being evaluated.
     * @param MP Modulus of the polynomial value.
     * @param ARE Error bounds on complex addition.
     * @param MRE Error bounds on complex multiplication.
     */
    private static double errEv(int NN, double[] Qr, double[] Qi, double MS, double MP,
            double ARE, double MRE) {
        double E = cmod(Qr[0], Qi[0]) * MRE / (ARE + MRE);
        for (int i = 0; i < NN; ++i) {
            E = E * MS + cmod(Qr[i], Qi[i]);
        }

        return (E * (ARE + MRE) - MP * MRE);
    }

    /**
     * Computes a lower bound on the moduli of the zeros of a polynomial.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param PT The modulus of the coefficients of the polynomial.
     * @param Q Array filled in on output.
     */
    private static double cauchy(int NN, double[] PT, double[] Q) {
        int NNm1 = NN - 1;

        PT[NNm1] = -PT[NNm1];

        //	Compute the upper estimate of bound.
        int N = NN - 1;
        int Nm1 = N - 1;
        double X = exp((log(-PT[NNm1]) - log(PT[0])) / N);
        if (PT[Nm1] != 0.) {
            // If newton step at the origin is better, use it.
            double XM = -PT[NNm1] / PT[Nm1];
            if (XM < X)
                X = XM;
        }
        // Chop the interval (0,X) until F <= 0.
        while (true) {
            double XM = X * 0.1;
            double F = PT[0];
            for (int i = 1; i < NN; ++i) {
                F = F * XM + PT[i];
            }
            if (F <= 0.)
                break;
            X = XM;
        }
        double DX = X;

        // Do Newton iterations until X converges to two decimal places.
        while (abs(DX / X) > 0.005) {
            Q[0] = PT[0];
            for (int i = 1; i < NN; ++i) {
                Q[i] = Q[i - 1] * X + PT[i];
            }
            double F = Q[NNm1];
            double DF = Q[0];
            for (int i = 1; i < N; ++i) {
                DF = DF * X + Q[i];
            }
            DX = F / DF;
            X = X - DX;
        }

        return X;
    }

    /**
     * Returns a scale factor to multiply the coefficients of the polynomial. The scaling
     * is done to avoid overflow and to avoid undetected underflow interfering with the
     * convergence criterion. The factor is a power of the BASE.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param PT The modulus of the coefficients of the polynomial.
     */
    private static double scale(int NN, double[] PT) {
        // Find the largest and the smallest moduli of coefficients.
        double hi = sqrt(INFIN);
        double lo = SMALNO / ETA;
        double max = 0.;
        double min = INFIN;
        double X, sc;
        for (int i = 0; i < NN; ++i) {
            X = PT[i];
            if (X > max)
                max = X;
            if (X != 0. && X < min)
                min = X;
        }

        // Scale only if there are very large or very small components.
        double scale = 1.;
        if (min >= lo && max <= hi)
            return scale;

        X = lo / min;
        if (X > 1.) {
            sc = X;
            if (INFIN / sc > max)
                sc = 1.;
        } else
            sc = 1. / sqrt(max * min);

        double L = log(sc) / log(BASE) + 0.5;
        scale = pow(BASE, L);

        return scale;
    }

    /**
     * Complex division C = A/B, avoiding overflow. Results are stored in class variables
     * Tr and Ti to avoid overhead of creating a Complex object during root finding.
     * Results are stored in class variables Tr and Ti.
     *
     * @param Ar The real part of the complex numerator.
     * @param Ai The imaginary part of the complex numerator.
     * @param Br The real part of the complex denominator.
     * @param Bi The imaginary part of the complex denominator.
     */
    private void cdiv(double Ar, double Ai, double Br, double Bi) {
        if (Br == 0. && Bi == 0.) {
            // Division by zero, result = infinity.
            Tr = INFIN;
            Ti = INFIN;
            return;
        }

        if (abs(Br) >= abs(Bi)) {
            double R = Bi / Br;
            double D = Br + R * Bi;
            Tr = (Ar + Ai * R) / D;
            Ti = (Ai - Ar * R) / D;

        } else {
            double R = Br / Bi;
            double D = Bi + R * Br;
            Tr = (Ar * R + Ai) / D;
            Ti = (Ai * R - Ar) / D;
        }

    }

    /**
     * Calculates the modulus or magnitude of a complex number avoiding overflow.
     *
     * Adapted from "Numerical Recipes in C: The Art of Scientific Computing" 2nd Edition,
     * pg 949, ISBN 0-521-43108-5. The NR algorithm is only slightly different from the
     * ACM algorithm, but the NR version appears to be slightly more robust.
     *
     * @param re The real part of the complex number.
     * @param im The imaginary part of the complex number.
     */
    private static double cmod(double re, double im) {
        double ans;
        re = abs(re);
        im = abs(im);

        if (re == 0.0)
            ans = im;
        else if (im == 0.0)
            ans = re;
        else if (re > im) {
            double temp = im / re;
            ans = re * sqrt(1. + temp * temp);

        } else {
            double temp = re / im;
            ans = im * sqrt(1. + temp * temp);
        }

        return ans;
    }

    /**
     * Computes the derivative polynomial as the initial H polynomial and computes L1
     * no-shift H polynomials.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param L1 Number of Level 1 shifts to make.
     * @param Pr, Pi The coefficients of the polynomial.
     * @param Hr, Hi Arrays containing output ?
     */
    private void noShift(int NN, int L1, double[] Pr, double[] Pi, double[] Hr, double[] Hi) {
        int N = NN - 1;
        int Nm1 = N - 1;
        int NNm1 = NN - 1;

        for (int i = 0; i < N; ++i) {
            double XNi = NNm1 - i;
            Hr[i] = XNi * Pr[i] / N;
            Hi[i] = XNi * Pi[i] / N;
        }

        for (int jj = 0; jj < L1; ++jj) {
            if (cmod(Hr[Nm1], Hi[Nm1]) > ETA * 10. * cmod(Pr[Nm1], Pi[Nm1])) {
                // Divide the negative coefficient by the derivative.
                cdiv(-Pr[NNm1], -Pi[NNm1], Hr[Nm1], Hi[Nm1]);	// Outputs Tr, Ti.
                for (int i = 1; i <= Nm1; ++i) {
                    int j = NNm1 - i;
                    double T1 = Hr[j - 1];
                    double T2 = Hi[j - 1];
                    Hr[j] = Tr * T1 - Ti * T2 + Pr[j];
                    Hi[j] = Tr * T2 + Ti * T1 + Pi[j];
                }
                Hr[0] = Pr[0];
                Hi[0] = Pi[0];

            } else {
                // If the constant term is essentially zero, shift H coefficients.
                for (int i = 1; i <= Nm1; ++i) {
                    int j = NNm1 - i;
                    Hr[j] = Hr[j - 1];
                    Hi[j] = Hi[j - 1];
                }
                Hr[0] = 0.;
                Hi[0] = 0.;
            }
        }

    }

    /**
     * Computes T = -P(S)/H(S). Sets class variables Tr, Ti.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param Sr The real part of the point we are evaluating the polynomial at.
     * @param Si The imaginary part of the point we are evaluating the polynomial at.
     * @param Hr, Hi Arrays containing ?
     * @param QHr, QHi Arrays containing partial sums of H(S) polynomial.
     * @return True if H(S) is essentially zero.
     */
    private boolean calcT(int NN, double Sr, double Si, double[] Hr, double[] Hi,
            double[] QHr, double[] QHi) {
        int N = NN - 1;
        int Nm1 = N - 1;

        //	Evaluate H(S).
        double tempR = PVr;
        double tempI = PVi;
        polyEv(N, Sr, Si, Hr, Hi, QHr, QHi);
        double HVr = PVr;
        double HVi = PVi;
        PVr = tempR;
        PVi = tempI;

        // Is H(S) essentially zero?
        boolean bool = cmod(HVr, HVi) <= ARE * 10. * cmod(Hr[Nm1], Hi[Nm1]);
        if (bool) {
            Tr = 0.;
            Ti = 0.;

        } else
            cdiv(-PVr, -PVi, HVr, HVi);		// Outputs Tr, Ti.

        return bool;
    }

    /**
     * Calculates the next shifted H polynomial.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param bool Set to true if H(S) is essentially zero.
     * @param Hr, Hi Arrays containing ?
     * @param QPr, QPi
     * @param QHr, QHi
     */
    private void nextH(int NN, boolean bool, double[] Hr, double[] Hi,
            double[] QPr, double[] QPi, double[] QHr, double[] QHi) {
        int N = NN - 1;

        if (!bool) {
            for (int j = 1; j < N; ++j) {
                double T1 = QHr[j - 1];
                double T2 = QHi[j - 1];
                Hr[j] = Tr * T1 - Ti * T2 + QPr[j];
                Hi[j] = Tr * T2 + Ti * T1 + QPi[j];
            }
            Hr[0] = QPr[0];
            Hi[0] = QPi[0];

        } else {
            // If H(S) is zero, replace H with QH.
            for (int j = 1; j < N; ++j) {
                Hr[j] = QHr[j - 1];
                Hi[j] = QHi[j - 1];
            }
            Hr[0] = 0.;
            Hi[0] = 0.;
        }

    }

    /**
     * Carries out the third stage iteration. On entry Zr, Zi contains the initial
     * iteration. If the iteration converges it contains the final iteration on exit. Also
     * uses and sets class variables Sr, Si.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param L3 Limit of steps in stage 3.
     * @param Pr, Pi The coefficients of the polynomial.
     * @param QPr, QPi
     * @param Hr, Hi Arrays containing ?
     * @param QHr, QHi
     * @return True if iteration converges.
     */
    private boolean vrShift(int NN, int L3, double[] Pr, double[] Pi,
            double QPr[], double QPi[],
            double Hr[], double Hi[], double QHr[], double QHi[]) {
        boolean conv = false;
        boolean B = false;
        double OMP = 0., RelSTP = 0.;

        Sr = Zr;
        Si = Zi;

        // Main loop for stage three.
        for (int i = 0; i < L3; ++i) {
            //	Evaluate P at S and test for convergence.
            polyEv(NN, Sr, Si, Pr, Pi, QPr, QPi);		//	Outputs PVr, PVi.

            double MP = cmod(PVr, PVi);
            double MS = cmod(Sr, Si);
            if (MP <= 20 * errEv(NN, QPr, QPi, MS, MP, ARE, MRE)) {
                //	Polynomial value is smaller in value than a bound on the error
                //	in evaluating P, terminate the iteration.
                Zr = Sr;
                Zi = Si;
                return true;

            }

            if (i == 0) {
                OMP = MP;

            } else {
                if (!B && MP >= OMP && RelSTP < 0.05) {
                    //	Iteration has stalled.  Probably a cluster of zeros.  Do 5 fixed
                    //	shift steps into the cluster to force one zero to dominate.
                    double TP = RelSTP;
                    B = true;
                    if (RelSTP < ETA)
                        TP = ETA;
                    double R1 = sqrt(TP);
                    double R2 = Sr * (1. + R1) - Si * R1;
                    Si = Sr * R1 + Si * (1. + R1);
                    Sr = R2;
                    polyEv(NN, Sr, Si, Pr, Pi, QPr, QPi);		//	Outputs PVr, PVi.

                    for (int j = 0; j < 5; ++j) {
                        boolean bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);	// Outputs Tr, Ti.
                        nextH(NN, bool, Hr, Hi, QPr, QPi, QHr, QHi);
                    }

                    OMP = INFIN;

                } else {
                    //	Exit if polynomial value increases significantly.
                    if (MP * 0.1 > OMP)
                        return conv;
                    OMP = MP;
                }
            }

            // Calculate next iteration.
            boolean bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);			// Outputs Tr, Ti.
            nextH(NN, bool, Hr, Hi, QPr, QPi, QHr, QHi);
            bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);			// Outputs Tr, Ti.
            if (!bool) {
                RelSTP = cmod(Tr, Ti) / cmod(Sr, Si);
                Sr = Sr + Tr;
                Si = Si + Ti;
            }
        }

        return conv;
    }

    /**
     * Computes L2 fixed-shift H polynomials and tests for convergence. Initiates a
     * variable-shift iteration and returns with the approximate zero if successful. Uses
     * and sets the class variables Sr and Si. Sets class variables Zr, Zi to approximate
     * zero if convergence is true.
     *
     * @param NN The number of coefficients to use in the evaluation.
     * @param L2 Limit of fixed shift steps.
     * @param Pr, Pi The coefficients of the polynomial.
     * @param QPr, QPi
     * @param Hr, Hi Arrays containing ?
     * @param QHr, QHi
     * @return True if convergence of stage 3 iteration is successful.
     */
    private boolean fxShift(int NN, int L2, double[] Pr, double[] Pi,
            double QPr[], double QPi[],
            double Hr[], double Hi[], double QHr[], double QHi[],
            double SHr[], double SHi[]) {
        int N = NN - 1;

        // Evaluate Polynomial at S.
        polyEv(NN, Sr, Si, Pr, Pi, QPr, QPi);	// Outputs PVr, PVi.
        boolean test = true;
        boolean pasd = false;

        // Calculate 1st T = -P(S)/H(S).
        boolean bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);	// Outputs Tr, Ti.

        // Main loop for one 2nd stage step.
        for (int j = 0; j < L2; ++j) {
            double OTr = Tr;
            double OTi = Ti;

            // Compute next H polynomial and new T.
            nextH(NN, bool, Hr, Hi, QPr, QPi, QHr, QHi);
            bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);		// Outputs Tr, Ti.
            Zr = Sr + Tr;
            Zi = Si + Ti;

            // Test for convergence unless stage 3 has failed once or
            // this is the last H polynomial.
            if (!bool && test && j != L2 - 1) {
                if (cmod(Tr - OTr, Ti - OTi) < 0.5 * cmod(Zr, Zi)) {
                    if (pasd) {
                        //	The weak convergence test has been passed twice, start
                        //	the third stage iteration, after saving the current H
                        //	polynomial and shift.
                        for (int i = 0; i < N; ++i) {
                            SHr[i] = Hr[i];
                            SHi[i] = Hi[i];
                        }
                        double SVSr = Sr;
                        double SVSi = Si;
                        boolean conv = vrShift(NN, 10, Pr, Pi, QPr, QPi, Hr, Hi, QHr, QHi);	// Outputs Zr, Zi.
                        if (conv)
                            return conv;

                        //	The iteration failed to converge.  Turn off testing and
                        //	restore H, S, PV and T.
                        test = false;
                        for (int i = 0; i < N; ++i) {
                            Hr[i] = SHr[i];
                            Hi[i] = SHi[i];
                        }
                        Sr = SVSr;
                        Si = SVSi;

                        polyEv(NN, Sr, Si, Pr, Pi, QPr, QPi);			// Outputs PVr, PVi.
                        bool = calcT(NN, Sr, Si, Hr, Hi, QHr, QHi);		// Outputs Tr, Ti.

                    } else {
                        pasd = true;
                    }
                } else {
                    pasd = false;
                }
            }
        }

        //	Attempt an iteration with final H polynomial from second stage.
        boolean conv = vrShift(NN, 10, Pr, Pi, QPr, QPi, Hr, Hi, QHr, QHi);		// Outputs Zr, Zi.

        return conv;
    }
}
