/*
 *   RungeKutta5  -- A 4th-5th Order Runge-Kutta ODE Integrator with variable step size.
 *
 *   Copyright (C) 2004-2014 by Joseph A. Huwaldt.
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
 * This class represents a variable step size 4th-5th Order Runge-Kutta Ordinary
 * Differential Equation integrator. Uses a Cash-Karp step and monitoring of the
 * truncation error.
 *
 * Reference: Press, et al, "Numerical Recipes in C", 2nd Edition, 1992, pg. 714.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt Date: March 19, 2004
 * @version April 3, 2014
 */
public class RungeKutta45 {

    //  Define constants.
    private static final double SAFETY = 0.9, PSHRNK = -0.25, PGROW = -0.2;
    private static final double ERRCON = 1.89e-4;   //  = pow(5/SAFETY, 1/PGROW)
    private static final double TINY = 1.0e-30;
    private static final double a2 = 0.2, a3 = 0.3, a4 = 0.6, a5 = 1.0, a6 = 0.875, b21 = 0.2;
    private static final double b31 = 3.0 / 40.0, b32 = 9.0 / 40.0, b41 = 0.3, b42 = -0.9, b43 = 1.2;
    private static final double b51 = -11.0 / 54.0, b52 = 2.5, b53 = -70.0 / 27.0, b54 = 35.0 / 27.0;
    private static final double b61 = 1631.0 / 55296.0, b62 = 175.0 / 512.0, b63 = 575.0 / 13824.0;
    private static final double b64 = 44275.0 / 110592.0, b65 = 253.0 / 4096.0, c1 = 37.0 / 378.0;
    private static final double c3 = 250.0 / 621.0, c4 = 125.0 / 594.0, c6 = 512.0 / 1771.0;
    private static final double dc5 = -277.0 / 14336.0;
    private static final double dc1 = c1 - 2825.0 / 27648.0, dc3 = c3 - 18575.0 / 48384.0;
    private static final double dc4 = c4 - 13525.0 / 55296.0, dc6 = c6 - 0.25;
    
    //	Arrays used as temporary storage for this ODE integrator.
    private double[] yerr;
    private double[] ytemp;
    private double[] yt, ak2, ak3, ak4, ak5, ak6;
    private double[] yscal, state, dydx;
    
    //  Class variables.
    private double hdid, hnext;
    private int numSteps, nfe;

    /**
     * Method that returns the size of the last step taken.
     */
    public double lastStepSize() {
        return hdid;
    }

    /**
     * Method that returns the estimated next step size. Output is only valid after a call
     * to "step".
     */
    public double nextStepSize() {
        return hnext;
    }

    /**
     * Method that returns the actual number of integration steps computed during the last
     * integration.
     */
    public double steps() {
        return numSteps;
    }

    /**
     * Method that returns the actual number of derivative function evaluations completed
     * during the last integration.
     */
    public double functionEvaluations() {
        return nfe;
    }

    /**
     * Take a single integration step, with monitoring of local truncation error to
     * monitor accuracy and adjust step size, advancing the solution over the step size
     * interval and returning the incremented variables as output. The last step size
     * taken by this method is returned using the "lastStepSize()" method. The estimated
     * next step size desired by this method is returned using the "nextStepSize()"
     * method.
     *
     * @param x The independent variable (such as time).
     * @param y The current state of the variables at x [0..nVars-1]. Overwritten with the
     *      final state of the variables at x+lastStepSize().
     * @param dydx Array containing the derivatives at x [0..nVars-1].
     * @param htry The step size to be attempted.
     * @param eps The required accuracy.
     * @param yscal The array against which the error will be scaled: [0..nVars-1].
     * @param derivs The object that will calculate the derivatives.
     * @return The value of the independent variable after the step is complete
     *      (x+lastStepSize()). Use "lastStepSize()" to return the last step size used and
     *      "nextStepSize()" to return an estimate of the next step size to be used.
     * @see #lastStepSize
     * @see #nextStepSize
     */
    public double step(double x, double[] y, double[] dydx, double htry, double eps, double[] yscal, Derivatives derivs)
            throws IntegratorException {

        int n = y.length;
        if (yerr == null || yerr.length != n) {
            //	Allocate arrays only once, re-use them after that.
            yerr = new double[n];
            ytemp = new double[n];
        }

        double h = htry;

        while (true) {
            //  Take a step.
            rkck(x, y, dydx, derivs, ytemp, yerr, h);

            //  Evaluate accuracy.
            double errmax = 0;
            for (int i = 0; i < n; ++i) {
                errmax = Math.max(errmax, Math.abs(yerr[i] / yscal[i]));
            }
            errmax /= eps;	  //  Scale relative to tolerance.
            if (errmax > 1.0) {
                //  Truncation to large, reduce step size.
                double htemp = SAFETY * h * Math.pow(errmax, PSHRNK);

                //  No more than a factor of 10.
                h = (h >= 0.0 ? Math.max(htemp, 0.1 * h) : Math.min(htemp, 0.1 * h));

                if ((x + h) == x)
                    throw new IntegratorException("Step size underflow.");

                continue;

            } else {
                //  Step succeeded.  Compute size of next step.
                if (errmax > ERRCON)
                    hnext = SAFETY * h * Math.pow(errmax, PGROW);
                else
                    hnext = 5.0 * h;	  //  More than a factor of 5 increase.
                hdid = h;
                x += h;

                //  Copy results into output array.
                System.arraycopy(ytemp, 0, y, 0, n);
                break;
            }

        }

        return x;
    }

    /**
     * Take a single 5th order Cash-Karp Runge-Kutta integration step, advancing the
     * solution over the step size interval and returning the incremented variables as
     * output. Also returns an estimate of the local truncation error in yout using the
     * embedded 4th order method.
     *
     * @param x The independent variable (such as time).
     * @param y The current state of the variables at x [0..nVars-1].
     * @param dydx Array containing the derivatives at x [0..nVars-1].
     * @param derivs The object that will calculate the derivatives.
     * @param yout Pre-existing storage space for the output. Must have the same number of
     *      elements as "y", and it may NOT be a reference to the same array as "y".
     * @param yerr Pre-existing storage for the truncation error estimate. Must have the
     *      same number of elements as y.
     * @param h The size of the step to take.
     * @return The state after the step is complete [0..nVars-1].
     */
    private void rkck(double x, double[] y, double[] dydx, Derivatives derivs,
            double[] yout, double[] yerr, double h) {

        int n = y.length;

        if (ak2 == null || ak2.length != n) {
            //	Allocate arrays only once, re-use them after that.
            ak2 = new double[n];
            ak3 = new double[n];
            ak4 = new double[n];
            ak5 = new double[n];
            ak6 = new double[n];
            yt = new double[n];
        }

        //	First step.
        for (int i = 0; i < n; ++i) {
            yt[i] = y[i] + b21 * h * dydx[i];
        }

        //	Second step.
        derivs.derivs(ak2, x + a2 * h, yt);
        for (int i = 0; i < n; ++i) {
            yt[i] = y[i] + h * (b31 * dydx[i] + b32 * ak2[i]);
        }

        //	Third step.
        derivs.derivs(ak3, x + a3 * h, yt);
        for (int i = 0; i < n; ++i) {
            yt[i] = y[i] + h * (b41 * dydx[i] + b42 * ak2[i] + b43 * ak3[i]);
        }

        //	Fourth step.
        derivs.derivs(ak4, x + a4 * h, yt);
        for (int i = 0; i < n; ++i) {
            yt[i] = y[i] + h * (b51 * dydx[i] + b52 * ak2[i] + b53 * ak3[i] + b54 * ak4[i]);
        }

        //	Fifth step.
        derivs.derivs(ak5, x + a5 * h, yt);
        for (int i = 0; i < n; ++i) {
            yt[i] = y[i] + h * (b61 * dydx[i] + b62 * ak2[i] + b63 * ak3[i] + b64 * ak4[i] + b65 * ak5[i]);
        }

        //	Sixth step.
        derivs.derivs(ak6, x + a6 * h, yt);

        //  Accumulate increments with proper weights.
        for (int i = 0; i < n; ++i) {
            yout[i] = y[i] + h * (c1 * dydx[i] + c3 * ak3[i] + c4 * ak4[i] + c6 * ak6[i]);
        }

        //	Estimate error as difference between fourth and fifth order methods.
        for (int i = 0; i < n; ++i) {
            yerr[i] = h * (dc1 * dydx[i] + dc3 * ak3[i] + dc4 * ak4[i] + dc5 * ak5[i] + dc6 * ak6[i]);
        }

    }

    /**
     * Method that integrates an ODE from x0 to xf using adaptive step size increments. If
     * the output arrays are filled before xf is reached, recording of intermediate steps
     * will stop. The actual number of integration steps carried out during an integration
     * is returned by the "steps()" method. The number of function evaluations carried out
     * during a integration is returned by the "functionEvaluations()" method.
     *
     * @param x0 The initial independent variable value.
     * @param y0 The initial state of the variables at x0 [0..nVars-1]. Will be
     *      overwritten with the final state of the variables at the end of the integration.
     * @param xf The final independent variable value.
     * @param eps Desired accuracy for the integration.
     * @param dx0 Guessed first step size.
     * @param dxmin Minimum allowed step size (may be 0).
     * @param derivs The object that will calculate the derivatives for the ODEs.
     * @param xout Existing array that will be filled with the independent values that
     *      were integrated at each step. Defined as [0..maxSteps-1]. If null is passed, no
     *      independent data will be stored.
     * @param yout Existing matrix that will be filled with the output of the integration
     *      at each step. Defined as [0..maxSteps-1][0..nVars-1]. If null is passed, no
     *      intermediate variables will be stored.
     * @param dxsav Step interval at which results should be stored in output arrays.
     * @return Returns the number of data elements written to the output array.
     * @see #steps
     * @see #functionEvaluations
     */
    public int integrate(double x0, double[] y0, double xf, double eps, double dx0, double dxmin,
            Derivatives derivs, double[] xout, double[][] yout, double dxsav)
            throws IntegratorException {

        nfe = 0;
        int nvar = y0.length;
        dxmin = Math.abs(dxmin);
        eps = Math.abs(eps);
        double h = (xf - x0 >= 0 ? Math.abs(dx0) : -Math.abs(dx0));
        hnext = h;
        double x = x0;
        if ((x + h) == x)
            throw new IllegalArgumentException("Initial step size to small.");

        int kmax = 0;

        if (xout != null) {
            xout[0] = x0;

            //  Make sure we don't exceed the size of the input data arrays.
            kmax = xout.length;
        }

        //	Allocate memory as needed.
        if (yscal == null || yscal.length != nvar) {
            //  Allocate state array only once and re-use, if possible, after that.
            yscal = new double[nvar];
            state = new double[nvar];
            dydx = new double[nvar];
        }

        //	Load starting values.
        System.arraycopy(y0, 0, state, 0, nvar);
        if (yout != null)
            System.arraycopy(y0, 0, yout[0], 0, nvar);

        //  Assures storage of the 1st point.
        double xsav = x - dxsav * 2;

        //	Main integration loop.
        int kount = 0;
        int nstep = 0;
        do {
            ++nstep;

            if (Math.abs(hnext) <= dxmin)
                throw new IntegratorException("Step size too small: h = " + (float) (hnext) + ".");
            h = hnext;

            derivs.derivs(dydx, x, state);
            ++nfe;

            //  Scaling used to monitor accuracy.
            for (int i = 0; i < nvar; ++i) {
                yscal[i] = Math.abs(state[i]) + Math.abs(dydx[i] * h) + TINY;
            }

            //  Store intermediate results.
            if (kmax > 0 && kount < kmax - 3 && Math.abs(x - xsav) > Math.abs(dxsav)) {
                ++kount;
                if (xout != null)
                    xout[kount] = x;
                if (yout != null)
                    System.arraycopy(state, 0, yout[kount], 0, nvar);
                xsav = x;
            }

            //  If stepsize can overshoot, decrease.
            if ((x + h - xf) * (x + h - x0) > 0.0)
                h = xf - x;

            //  Take a single step.
            x = step(x, state, dydx, h, eps, yscal, derivs);
            nfe += 5;

            //  Are we done?
        } while ((x - xf) * (xf - x0) < 0.0 && (kmax > 0 && kount < kmax - 2) && nstep < Integer.MAX_VALUE
                && !(Double.isNaN(state[0]) || Double.isInfinite(state[0])));


        //  Copy final state into the y0 array.
        System.arraycopy(state, 0, y0, 0, nvar);

        //  Save final step.
        if (kmax > 0) {
            ++kount;
            if (xout != null)
                xout[kount] = x;
            if (yout != null)
                System.arraycopy(state, 0, yout[kount], 0, nvar);
        }

        numSteps = nstep + 1;

        return kount + 1;
    }

    /**
     * Method that integrates an ODE from x0 until a specified stopping condition is met
     * using adaptive step size increments. If the output arrays are filled before the
     * stopping condition is reached, recording of intermediate steps will stop. The
     * actual number of integration steps carried out during an integration is returned by
     * the "steps()" method. The number of function evaluations carried out during a
     * integration is returned by the "functionEvaluations()" method.
     *
     * @param x0 The initial independent variable value.
     * @param y0 The initial state of the variables at x0 [0..nVars-1]. Will be
     *      overwritten with the final state of the variables at the end of the integration.
     * @param stopping An object that tells the integrator to stop integrating when
     *      certain conditions are met. One integration step will always be taken, no matter
     *      what the stopping condition is.
     * @param eps Desired accuracy for the integration.
     * @param dx0 Guessed first step size.
     * @param dxmin Minimum allowed step size (may be 0).
     * @param derivs The object that will calculate the derivatives for the ODEs.
     * @param xout Existing array that will be filled with the independent values that
     *      were integrated at each step. Defined as [0..maxSteps-1]. If null is passed, no
     *      independent data will be stored.
     * @param yout Existing matrix that will be filled with the output of the integration
     *      at each step. Defined as [0..maxSteps-1][0..nVars-1]. If null is passed, no
     *      intermediate variables will be stored.
     * @param dxsav Step interval at which results should be stored in output arrays.
     * @return Returns the number of data elements written to the output array.
     * @see #steps
     * @see #functionEvaluations
     */
    public int integrate(double x0, double[] y0, StoppingCondition stopping, double eps, double dx0, double dxmin,
            Derivatives derivs, double[] xout, double[][] yout, double dxsav)
            throws IntegratorException {

        nfe = 0;
        int nvar = y0.length;
        dxmin = Math.abs(dxmin);
        eps = Math.abs(eps);
        double h = dx0;
        hnext = h;
        double x = x0;
        if ((x + h) == x)
            throw new IllegalArgumentException("Initial step size to small.");

        int kmax = 0;

        if (xout != null) {
            xout[0] = x0;

            //  Make sure we don't exceed the size of the input data arrays.
            kmax = xout.length;
        }

        //	Allocate memory as needed.
        if (yscal == null || yscal.length != nvar) {
            //  Allocate state array only once and re-use, if possible, after that.
            yscal = new double[nvar];
            state = new double[nvar];
            dydx = new double[nvar];
        }

        //	Load starting values.
        System.arraycopy(y0, 0, state, 0, nvar);
        if (yout != null)
            System.arraycopy(y0, 0, yout[0], 0, nvar);

        //  Assures storage of the 1st point.
        double xsav = x - dxsav * 2;

        //	Main integration loop.
        int kount = 0;
        int nstep = 0;
        do {
            ++nstep;

            if (Math.abs(hnext) <= dxmin)
                hnext = dxmin;
            h = hnext;

            derivs.derivs(dydx, x, state);
            ++nfe;

            //  Scaling used to monitor accuracy.
            for (int i = 0; i < nvar; ++i) {
                yscal[i] = Math.abs(state[i]) + Math.abs(dydx[i] * h) + TINY;
            }

            //  Store intermediate results.
            if (kmax > 0 && kount < kmax - 3 && Math.abs(x - xsav) > Math.abs(dxsav)) {
                ++kount;
                if (xout != null)
                    xout[kount] = x;
                if (yout != null)
                    System.arraycopy(state, 0, yout[kount], 0, nvar);
                xsav = x;
            }

            //  If stepsize can overshoot, decrease.
//			if ((x + h - xf)*(x + h - x0) > 0.0)
//				h = xf - x;

            //  Take a single step.
            x = step(x, state, dydx, h, eps, yscal, derivs);
            nfe += 5;

            //  Are we done?
        } while (!stopping.stop(x, state, nstep) && (kmax > 0 && kount < kmax - 2) && nstep < Integer.MAX_VALUE
                && !(Double.isNaN(state[0]) || Double.isInfinite(state[0])));


        //  Copy final state into the y0 array.
        System.arraycopy(state, 0, y0, 0, nvar);

        //  Save final step.
        if (kmax > 0) {
            ++kount;
            if (xout != null)
                xout[kount] = x;
            if (yout != null)
                System.arraycopy(state, 0, yout[kount], 0, nvar);
        }

        numSteps = nstep + 1;

        return kount + 1;
    }
}
