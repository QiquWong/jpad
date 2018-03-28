/*
*   RungeKutta4  -- A 4th Order Runge-Kutta ODE Integrator.
*
*   Copyright (C) 2004 by Joseph A. Huwaldt.
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
**/
package standaloneutils.mathtools;


/**
*  This class represents a fixed step size 4th-Order
*  Runge-Kutta Ordinary Differential Equation integrator.
*  Based on "RungeKutta4.java" as found in JAT
*  (http://jat.sourceforge.net) with many modifications
*  to suit my needs.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  March 19, 2004
*  @version October 30, 2004
**/
public class RungeKutta4 {

	//	Arrays used as temporary storage for this ODE integrator.
	private double[][] f;
	private double[] yt;
	private double[] state;
	
	private int nfe;
	
	
	/**
	*  Method that returns the actual number of derivative function evaluations
	*  completed during the last integration.
	**/
	public double functionEvaluations() {
		return nfe;
	}
	
	/**
	*  Take a single integration step, advancing the solution over
	*  the step size interval and returning the incremented variables
	*  as output.
	*
	*  @param  x        The independent variable (such as time).
	*  @param  y        The current state of the variables at x [0..nVars-1].
	*  @param  derivs   The object that will calculate the derivatives.
	*  @param  yout     Pre-existing storage space for the output.  Must
	*                   have the same number of elements as "y", and it may
	*                   be a reference to the same array as "y".
	*  @param  stepSize The size of the step to take.
	*  @return The value of the independent variable after the step has been taken:
	*          output = x + stepSize.
	**/
	public double step(double x, double[] y, Derivatives derivs, double[] yout, double stepSize) {
	
		int n = y.length;
		if (yout == null || yout.length != n)
			throw new IllegalArgumentException("yout must be same size as y");
		
		if (f == null || f[0].length != n) {
			//	Allocate arrays only once, re-use them after that.
			f = new double[3][n];
			yt = new double[n];
		}
		
		double h = stepSize;
		double hh = h*0.5;
		double h6 = h/6.0;
		double xhh = x + hh;
		double xh = x + h;
		
		//	First step.
		f[0] = derivs.derivs(f[0], x,y);
		for (int i=0; i < n; ++i)
			yt[i] = y[i] + hh*f[0][i];
		
		//	Second step.
		f[1] = derivs.derivs(f[1], xhh, yt);
		for (int i=0; i < n; ++i)
			yt[i] = y[i] + hh*f[1][i];
		
		//	Third step.
		f[2] = derivs.derivs(f[2], xhh, yt);
		for (int i=0; i < n; ++i) {
			yt[i] = y[i] + h*f[2][i];
			f[2][i] += f[1][i];
		}
		
		//	Fourth step.
		f[1] = derivs.derivs(f[1], xh, yt);
		
		//	Accumulate the solution.
		for (int i=0; i < n; ++i)
			yout[i] = y[i] + h6*(f[0][i] + f[1][i] + 2*f[2][i]);
		
		return xh;
	}
	
	
	/**
	*  Method that integrates the ODE from x0 to xf in equal
	*  step size increments.  The maximum number of steps is determined from
	*  the size of the input xout array (if provided).
	*
	*  @param  x0       The initial indepenent variable value.
	*  @param  y0       The initial state of the variables at x0 [0..nVars-1].
	*                   Will be overwritten with the final state of the variables
	*                   at the end of the integration.
	*  @param  xf       The final independent variable value.
	*  @param  dx       The fixed step size to be used for integration.
	*  @param  derivs   The object that will calculate the derivatives for the ODEs.
	*  @param  xout     Existing array that will be filled with the independent values
	*                   that were integrated at each step.  Defined as [0..maxSteps-1].
	*                   If null is passed, no independent data will be stored.
	*  @param  yout     Existing matrix that will be filled with the output of the integration
	*                   at each step.  Defined as [0..maxSteps-1][0..nVars-1].
	*                   If null is passed, no intermediate variables will be stored.
	*  @return  Returns the number of steps actually carried out.
	**/
	public int integrate(double x0, double[] y0, double xf, double dx, Derivatives derivs,
									double[] xout, double[][] yout) {
	
		nfe = 0;
		int neqns = y0.length;
		double x = x0;
		if ( (x+dx) == x )
			throw new IllegalArgumentException("Step size to small.");
		
		dx = (xf-x0 >= 0 ? Math.abs(dx): -Math.abs(dx));
		int nSteps = (int)((xf - x0)/dx);
		
		if (xout != null) {
			xout[0] = x0;

			//  Make sure we don't exceed the size of the input data arrays.
			if (xout.length < nSteps)
				nSteps = xout.length;
		}
		
		//	Allocate memory as needed.
		if (state == null || state.length != neqns)
			//  Allocate state array only once and re-use, if possible, after that.
			state = new double[neqns];
		
		//	Load starting values.
		System.arraycopy(y0,0, state,0, neqns);
		if (yout != null)
			System.arraycopy(y0,0, yout[0],0, neqns);
		
		
		//	Main integration loop.
		for (int k=1; k < nSteps; ++k) {
			x = step(x, state, derivs, state, dx);
			nfe += 4;
			
			//	Copy state into output array.
			if (yout != null)
				System.arraycopy(state,0, yout[k],0, neqns);
			
			if (xout != null)
				xout[k] = x;
			
			//  Check for bad numerics.
			if (Double.isNaN(state[0]) || Double.isInfinite(state[0]))
				break;
		}

		//  Copy final state into the y0 array.
		System.arraycopy(state,0, y0,0, neqns);

		return nSteps;
	}
	
	/**
	*  Method that integrates the ODE starting with x0 and going to the stopping condition
	*  in equal step size increments.  The maximum number of steps is determined from
	*  the size of the input xout array (if provided).  The actual number of steps will be
	*  determined by the supplied stopping condition.
	*
	*  @param  x0       The initial indepenent variable value.
	*  @param  y0       The initial state of the variables at x0. Defined as [0..nVars-1].
	*                   Will be overwritten with the final state of the variables
	*                   at the end of the integration.
	*  @param  stopping An object that tells the integrator to stop integrating when
	*                   certain conditions are met.  One integration step will always
	*                   be taken, no matter what the stopping condition is.
	*  @param  dx       The fixed step size to be used for integration.
	*  @param  derivs   The object that will calculate the derivatives for the ODEs.
	*  @param  xout     Existing array that will be filled with the independent values
	*                   that were integrated at each step.  Defined as [0..maxSteps-1].
	*                   If null is passed, no independent data will be stored.
	*  @param  yout     Existing matrix that will be filled with the output of the integration
	*                   at each step.  Defined as [0..maxSteps-1][0..nVars-1].
	*                   If null is passed, no intermediate variables will be stored.
	*  @return  Returns the number of steps actually carried out.
	**/
	public int integrate(double x0, double[] y0, StoppingCondition stopping, double dx, Derivatives derivs,
									double[] xout, double[][] yout) {
	
		int neqns = y0.length;
		double x = x0;
		if ( (x+dx) == x )
			throw new IllegalArgumentException("Step size to small.");
		
		
		int maxSteps = Integer.MAX_VALUE;
		if (xout != null) {
			xout[0] = x0;
			maxSteps = xout.length;
		}
		
		//	Allocate memory as needed.
		if (state == null || state.length != neqns)
			//  Allocate state array only once and re-use, if possible, after that.
			state = new double[neqns];
		
		//	Load starting values.
		System.arraycopy(y0,0, state,0, neqns);
		if (yout != null)
			System.arraycopy(y0,0, yout[0],0, neqns);

		
		//	Main integration loop.
		int k=0;
		do {
			//  Advance the solution one time step.
			x = step(x, state, derivs, state, dx);

			//  Advance the step counter.
			++k;
			
			//	Copy output state into output array.
			if (yout != null)
				System.arraycopy(state,0, yout[k],0, neqns);

			if (xout != null)
				xout[k] = x;

		} while( k < maxSteps && !stopping.stop(x, state, k) &&
						!(Double.isNaN(state[0])  || Double.isInfinite(state[0])) );

		//  Copy final state into the y0 array.
		System.arraycopy(state,0, y0,0, neqns);
		
		return k;
	}
	
}
