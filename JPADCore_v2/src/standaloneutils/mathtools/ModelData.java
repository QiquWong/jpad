/*
 *   ModelData  -- A utility class used to model experimental data.
 *
 *   Copyright (C) 2002-2006 by Joseph A. Huwaldt
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

import java.util.Arrays;

/**
 * Methods in this class are used to create functions that model experimental data.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt	Date:	October 21, 2002
 * @version February 13, 2014
 */
public class ModelData {

    //	Tolerance used in single-value-decomposition to remove singular values.
    private static final double TOL = 1E-13;

    /**
     * Method that returns the coefficients of a polynomial of the specified degree that
     * best models or "fits" the supplied data values in a minimization of chi-squared
     * sense. This method assumes that each data point is normally distributed and has a
     * standard deviation of 1.0.
     *
     * @param xarr Array of independent parameter data values to fit.
     * @param yarr Array of dependent data values (associated with each X value) to be modeled.
     * @param coef An existing array that will be filled in with the coefficients of the
     *  polynomial, in decreasing power that best fits the sample data. Example: p(x) = A +
     *  B*x + C*x^2 + D*x^3 + E*x^4 corresponds to coef[] = {A, B, C, D, E}. The number of
     *  elements in the coef array determines the order of the polynomial created. Number
     *  of elements be greater than 1.
     * @return The chi-squared value of the polynomial returned. A value near zero
     *  indicates a perfect fit.
     */
    public static double polynomial(double[] xarr, double[] yarr, double[] coef) throws RootException {
        if (xarr == null)
            throw new NullPointerException("xarr == null");
        double[] sig = new double[xarr.length];
        Arrays.fill(sig, 1.0);
        BasisFunction bf = new PolynomialFit();
        return fit(xarr, yarr, sig, bf, coef);
    }

    /**
     * Method that returns the coefficients of a polynomial of the specified degree that
     * best models or "fits" the supplied data values in a minimization of chi-squared
     * sense.
     *
     * @param xarr Array of independent parameter data values to fit.
     * @param yarr Array of dependent data values (associated with each X value) to be modeled.
     * @param sig Array of individual standard deviations for each data point.
     * @param coef An existing array that will be filled in with the coefficients of the
     *  polynomial, in decreasing power that best fits the sample data. Example: p(x) = A +
     *  B*x + C*x^2 + D*x^3 + E*x^4 corresponds to coef[] = {A, B, C, D, E}. The number of
     *  elements in the coef array determines the order of the polynomial created. Number
     *  of elements be greater than 1.
     * @return The chi-squared value of the polynomial returned. A value near zero
     *  indicates a perfect fit.
     */
    public static double polynomial(double[] xarr, double[] yarr, double[] sig, double[] coef) throws RootException {
        BasisFunction bf = new PolynomialFit();
        return fit(xarr, yarr, sig, bf, coef);
    }

    /**
     * Method that returns the coefficients of an arbitrary basis function that best
     * models or "fits" the supplied data values in a minimization of chi-squared sense.
     *
     * @param xarr Array of independent parameter data values to fit.
     * @param yarr Array of dependent data values (associated with each X value) to be
     * modeled.
     * @param sig Array of individual standard deviations for each data point.
     * @param func The basis function used to generate the data fit.
     * @param coef An existing array that will be filled in with the coefficients of the
     * basis function that best fits the data.
     * @return The chi-squared value of the function is returned. A value near zero
     * indicates a perfect fit.
     */
    public static double fit(double[] xarr, double[] yarr, double[] sig, BasisFunction func, double[] coef) throws RootException {
        if (xarr == null)
            throw new NullPointerException("xarr == null");
        if (yarr == null)
            throw new NullPointerException("yarr == null");
        if (sig == null)
            throw new NullPointerException("sig == null");
        if (func == null)
            throw new NullPointerException("func == null");
        if (coef == null)
            throw new NullPointerException("coef == null");

        int ma = coef.length;
        if (ma < func.getMinNumCoef())
            throw new IllegalArgumentException("Number of coefficients must be greater than " + (func.getMinNumCoef() - 1));

        int ndata = xarr.length;
        if (ndata != yarr.length)
            throw new IllegalArgumentException("xarr and yarr must be the same length.");
        if (ndata != sig.length)
            throw new IllegalArgumentException("sig must be the same length as xarr and yarr.");

        //	Allocate the arrays that we are going to need.
        double[][] u = new double[ndata][ma];
        double[][] v = new double[ma][ma];
        double[] w = new double[ma];

        //	Use Singular Value Decomposition to find the parameters of the basis function.
        double chisq = svdfit(xarr, yarr, sig, coef, u, v, w, func);

        return chisq;
    }

    /**
     * Returns the slope of the line formed by a linear regression through the specified
     * data arrays.
     *
     * @param x The independent array of data points.
     * @param y The dependent array of data points (must be the same size as the x array.
     * @return The slope of the line formed by a linear regression through the x and y
     *  arrays of data points.
     */
    public static double linearSlope(double[] x, double[] y) {

        //  Find the mean of the input arrays.
        int length = x.length;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < length; ++i) {
            sumX += x[i];
            sumY += y[i];
        }
        double xbar = sumX / length;
        double ybar = sumY / length;

        //  b = sum( (x - xbar)*(y - ybar) ) / sum ( (x - xbar)^2 )
        double denom = 0;
        double num = 0;
        for (int i = 0; i < 4; ++i) {
            double a = x[i] - xbar;
            num += a * (y[i] - ybar);
            denom += a * a;
        }
        double b = num / denom;

        return b;
    }

    /**
     * Used to test the methods in this class.
     */
    public static void main(String[] args) {

        System.out.println();
        System.out.println("Testing ModelData...");

        try {
            System.out.println("  Poly Curve Fit #1:  linear");
            double[] x = {0, 1, 2, 3, 4, 5};
            double[] y = {1, 3, 5, 7, 9, 11};
            double[] coef = new double[2];
            double chisq = polynomial(x, y, coef);
            System.out.println("    coef = " + (float)coef[0] + ", " + (float)coef[1] + ", chisq = " + (float)chisq);
            System.out.println("      should be:  p(x) = 1 + 2*x\n");

            System.out.println("  Poly Curve Fit #2:  quadratic");
            double[] x1 = {3, 4, 5};
            double[] y1 = {-13.87, -0.09, -13.95};
            double[] coef1 = new double[3];
            chisq = polynomial(x1, y1, coef1);
            System.out.println("    coef = " + (float)coef1[0] + ", " + (float)coef1[1] + ", " + (float)coef1[2] + ", chisq = " + (float)chisq);
            System.out.println("      should be:  p(x) = -221.05 + 110.52*x - 13.82*x^2");

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done.");

    }

    //-----------------------------------------------------------------------------------
    /**
     * Given a set of data points x,y with individual standard deviations sig, use chi^2
     * minimization to determine the coefficients "a" of the supplied basis fitting
     * function y = sumi(ai*funci(x)).
     */
    private static double svdfit(double[] x, double[] y, double[] sig, double[] a, double[][] u, double[][] v, double[] w,
            BasisFunction f) throws RootException {
        int ndata = x.length;
        int ma = a.length;

        double[] beta = new double[ndata];
        double[] afunc = new double[ma];

        //	Accumulate coefficients of the fitting matrix.
        for (int i = 0; i < ndata; ++i) {
            f.func(x[i], afunc);
            double sigi = 1.0 / sig[i];
            for (int j = 0; j < ma; ++j) {
                u[i][j] = afunc[j] * sigi;
            }
            beta[i] = y[i] * sigi;
        }

        //	Singular value decomposition.
        svdcmp(u, w, v);

        //	Edit the singular values.
        double wmax = 0;
        for (int j = 0; j < ma; ++j) {
            if (w[j] > wmax)
                wmax = w[j];
        }
        double thresh = TOL * wmax;
        for (int j = 0; j < ma; ++j) {
            if (w[j] < thresh)
                w[j] = 0;
        }

        svdksb(u, w, v, beta, a);

        //	Evaluate chi-square.
        double chisq = 0;
        for (int i = 0; i < ndata; ++i) {
            f.func(x[i], afunc);
            double sum = 0;
            for (int j = 0; j < ma; ++j) {
                sum += a[j] * afunc[j];
            }
            double tmp = (y[i] - sum) / sig[i];
            chisq += tmp * tmp;
        }

        return chisq;
    }

    /**
     * Solves A*X = B for a vector X, where A is specified by the arrays u[][], w[], and
     * v[][] as returned by svdcmp(). b is the right-hand side. x is the output solution
     * vector. No input quantities are destroyed, so the routine can be called
     * sequentially with different b's.
     */
    private static void svdksb(double[][] u, double[] w, double[][] v, double b[], double x[]) {
        int n = u[0].length;
        int m = u.length;

        double[] tmp = new double[n];
        for (int j = 0; j < n; ++j) {
            //	Calculate U^T*B.
            double s = 0;
            if (w[j] != 0) {
                //	Nonzero result only if w[j] is nonzero.
                for (int i = 0; i < m; ++i) {
                    s += u[i][j] * b[i];
                }
                s /= w[j];
            }
            tmp[j] = s;
        }

        //	Matrix multiply by V to get answer.
        for (int j = 0; j < n; ++j) {
            double s = 0;
            for (int jj = 0; jj < n; ++jj) {
                s += v[j][jj] * tmp[jj];
            }
            x[j] = s;
        }
    }

    /**
     * Given a matrix a[][], this routine computes its singular value decomposition. A =
     * U*W*V^T. The matrix U replaces "a" on output. The diagonal matrix of singular
     * values W is output as a vector w[]. The matrix V (not the transpose V^T) is output
     * as v[][].
     */
    private static void svdcmp(double[][] a, double[] w, double[][] v) throws RootException {
        int n = a[0].length;
        int m = a.length;
        int k, l = 0;

        double[] rv1 = new double[n];
        double g = 0;
        double scale = 0;
        double anorm = 0;
        double s = 0;

        //	Householder reduction to bidiagonal form.
        for (int i = 0; i < n; ++i) {
            l = i + 1;
            rv1[i] = scale * g;
            s = g = scale = 0;

            if (i < m) {
                for (k = i; k < m; ++k) {
                    scale += Math.abs(a[k][i]);
                }
                if (scale != 0) {
                    for (k = i; k < m; ++k) {
                        a[k][i] /= scale;
                        s += a[k][i] * a[k][i];
                    }
                    double f = a[i][i];
                    g = -MathTools.sign(Math.sqrt(s), f);
                    double h = f * g - s;
                    a[i][i] = f - g;
                    for (int j = l; j < n; ++j) {
                        for (s = 0, k = i; k < m; ++k) {
                            s += a[k][i] * a[k][j];
                        }
                        f = s / h;
                        for (k = i; k < m; ++k) {
                            a[k][j] += f * a[k][i];
                        }
                    }
                    for (k = i; k < m; ++k) {
                        a[k][i] *= scale;
                    }
                }

            }

            w[i] = scale * g;
            g = s = scale = 0;

            if (i < m && i + 1 != n) {
                for (k = l; k < n; ++k) {
                    scale += Math.abs(a[i][k]);
                }
                if (scale != 0) {
                    for (k = l; k < n; ++k) {
                        a[i][k] /= scale;
                        s += a[i][k] * a[i][k];
                    }
                    double f = a[i][l];
                    g = -MathTools.sign(Math.sqrt(s), f);
                    double h = f * g - s;
                    a[i][l] = f - g;
                    for (k = l; k < n; ++k) {
                        rv1[k] = a[i][k] / h;
                    }
                    for (int j = l; j < m; ++j) {
                        for (s = 0, k = l; k < n; ++k) {
                            s += a[j][k] * a[i][k];
                        }
                        for (k = l; k < n; ++k) {
                            a[j][k] += s * rv1[k];
                        }
                    }
                    for (k = l; k < n; ++k) {
                        a[i][k] *= scale;
                    }
                }

            }

            anorm = Math.max(anorm, (Math.abs(w[i]) + Math.abs(rv1[i])));
        }	//	Next i

        //	Accumulation of right-hand transformations.
        for (int i = n - 1; i >= 0; --i) {
            if (i + 1 < n) {
                if (g != 0) {
                    //	Double division to avoid possible underflow.
                    for (int j = l; j < n; ++j) {
                        v[j][i] = (a[i][j] / a[i][l]) / g;
                    }
                    for (int j = l; j < n; ++j) {
                        for (s = 0, k = l; k < n; ++k) {
                            s += a[i][k] * v[k][j];
                        }
                        for (k = l; k < n; ++k) {
                            v[k][j] += s * v[k][i];
                        }
                    }
                }
                for (int j = l; j < n; ++j) {
                    v[i][j] = v[j][i] = 0;
                }
            }

            v[i][i] = 1;
            g = rv1[i];
            l = i;
        }	//	Next i

        //	Accumulation of left-hand transformations.
        for (int i = Math.min(m, n) - 1; i >= 0; --i) {
            l = i + 1;
            g = w[i];
            for (int j = l; j < n; ++j) {
                a[i][j] = 0;
            }
            if (g != 0) {
                g = 1.0 / g;
                for (int j = l; j < n; ++j) {
                    for (s = 0, k = l; k < m; ++k) {
                        s += a[k][i] * a[k][j];
                    }
                    double f = (s / a[i][i]) * g;
                    for (k = i; k < m; ++k) {
                        a[k][j] += f * a[k][i];
                    }
                }
                for (int j = i; j < m; ++j) {
                    a[j][i] *= g;
                }

            } else
                for (int j = i; j < m; ++j) {
                    a[j][i] = 0;
                }

            ++a[i][i];
        }	//	Next i

        //	Diagonalization of the bidiagonal form:  Loop over singular values,
        //	and over allowed iterations.
        for (k = n - 1; k >= 0; --k) {
            for (int its = 0; its < 30; ++its) {
                boolean flag = true;
                double x, y, z, c, f, h;
                int nm = 0;

                //	Test for splitting.
                for (l = k; l >= 0; --l) {
                    nm = l - 1;				//	Note that rv1[1] is always zero.
                    if ((Math.abs(rv1[l]) + anorm) == anorm) {
                        flag = false;
                        break;
                    }
                    if ((Math.abs(w[nm]) + anorm) == anorm)
                        break;
                }

                //	Cancelation of rv1[l], if l > 1.
                if (flag) {
                    c = 0;
                    s = 1;
                    for (int i = l; i <= k; ++i) {
                        f = s * rv1[i];
                        rv1[i] = c * rv1[i];
                        if ((Math.abs(f) + anorm) == anorm)
                            break;
                        g = w[i];
                        h = pythag(f, g);
                        w[i] = h;
                        h = 1 / h;
                        c = g * h;
                        s = -f * h;
                        for (int j = 0; j < m; ++j) {
                            y = a[j][nm];
                            z = a[j][i];
                            a[j][nm] = y * c + z * s;
                            a[j][i] = z * c - y * s;
                        }
                    }
                }

                z = w[k];
                if (l == k) {
                    //	Convergence
                    //	Singular value is made nonnegative.
                    if (z < 0) {
                        w[k] = -z;
                        for (int j = 0; j < n; ++j) {
                            v[j][k] = -v[j][k];
                        }
                    }
                    break;
                }

                if (its == 30)
                    throw new RootException("No convergence in 30 svdcmp() iterations.");

                //	Shift from bottom 2-by-2 minor.
                x = w[l];
                nm = k - 1;
                y = w[nm];
                g = rv1[nm];
                h = rv1[k];
                f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
                g = pythag(f, 1);
                f = ((x - z) * (x + z) + h * ((y / (f + MathTools.sign(g, f))) - h)) / x;
                c = s = 1;

                //	Next QR transformation.
                for (int j = l; j <= nm; ++j) {
                    int i = j + 1;
                    g = rv1[i];
                    y = w[i];
                    h = s * g;
                    g = c * g;
                    z = pythag(f, h);
                    rv1[j] = z;
                    c = f / z;
                    s = h / z;
                    f = x * c + g * s;
                    g = g * c - x * s;
                    h = y * s;
                    y *= c;
                    for (int jj = 0; jj < n; ++jj) {
                        x = v[jj][j];
                        z = v[jj][i];
                        v[jj][j] = x * c + z * s;
                        v[jj][i] = z * c - x * s;
                    }
                    z = pythag(f, h);
                    w[j] = z;

                    //	Rotation can be arbitrary if z == 0.
                    if (z != 0) {
                        z = 1 / z;
                        c = f * z;
                        s = h * z;
                    }
                    f = c * g + s * y;
                    x = c * y - s * g;
                    for (int jj = 0; jj < m; ++jj) {
                        y = a[jj][j];
                        z = a[jj][i];
                        a[jj][j] = y * c + z * s;
                        a[jj][i] = z * c - y * s;
                    }

                }	//	Next j

                rv1[l] = 0;
                rv1[k] = f;
                w[k] = x;

            }	//	Next its
        }	//	Next k

    }

    /**
     * Method that calculates sqrt(a^2 + b^2) without destructive underflow or overflow.
     */
    private static double pythag(double a, double b) {
        double absa = Math.abs(a);
        double absb = Math.abs(b);

        if (absa > absb) {
            double ratio = absb / absa;
            return absa * Math.sqrt(1 + ratio * ratio);

        } else {
            if (absb == 0)
                return 0.0;
            else {
                double ratio = absa / absb;
                return absb * Math.sqrt(1 + ratio * ratio);
            }
        }
    }
}
