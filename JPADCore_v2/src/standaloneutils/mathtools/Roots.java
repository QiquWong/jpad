/*
*   Roots  -- A collection of static methods for finding the roots of functions.
*
*   Copyright (C) 1999-2014 by Joseph A. Huwaldt.
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
**/
package standaloneutils.mathtools;

import static java.lang.Math.*;


/**
*  A collection of static routines to find the roots
*  of functions or sets of functions.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  October 8, 1997
*  @version  January 23, 2014
**/
public class Roots {

	// Debug flag.
	private static final boolean DEBUG = false;
	
	/**
	*  Machine floating point precision.
	**/
	private static final double	EPS = MathTools.EPS;
	
	/**
	*  Maximum number of allowed iterations.
	**/
	private static final int  MAXIT = 500;
	
	//	Constants used by newtonND.
	private static final double TOLF = 1.0e-8, TOLMIN = 1.0e-12, STPMX = 100.0;
	private static final double TINY = 1.0e-20;
	private static final double ALF = 1.0e-4;
	
	
	//-----------------------------------------------------------------------------------
	/**
	*  Given a function, <code>func</code>, defined on the interval <code>x1</code> to <code>x2</code>,
	*  this routine subdivides the interval into <code>n</code> equally spaced segments,
	*  and searches for zero crossings of the function.  Brackets around any zero crossings found
	*  are returned.
	*
	*  @param func  The function that is being search for zero crossings.
	*  @param x1    The start of the interval to be searched.
	*  @param x2    The end of the interval to be searched.
	*  @param n     The number segments to divide the interval into.
	*  @param xb1   Lower value of each bracketing pair found (number of pairs is returned by function).
	*               The size of the array determines the maximum number of bracketing pairs that will be found.
	*  @param xb2   Upper value of each bracketing pair found (number of pairs is returned by function).
	*               The size of this array must match the size of <code>xb1</code>.
	*  @return The number of bracketing pairs found.
	**/
	public static int bracket(Evaluatable1D func, double x1, double x2, int n, double[] xb1, double[] xb2) throws RootException {
		int nb = xb1.length;
		int nbb = 0;
		double dx = (x2 - x1)/n;
		if (dx == 0)	return 0;
		double x = x1;
		double fp = func.function(x);
		for (int i=0; i < n; ++i) {
			x += dx;
			if (x > x2) x = x2;
			double fc = func.function(x);
			if (fc*fp <= 0.0) {
				xb1[nbb] = x - dx;
				xb2[nbb++] = x;
				if(nb == nbb) return nb;
			}
			fp = fc;
		}
		return nbb;
	}
	
	/**
	*  <p>  Find the root of a general 1D function f(x) = 0 known
	*       to lie between x1 and x2.  The root will be refined
	*       until it's accuracy is "tol".  </p>
	*
	*  <p>  Have your Evaluatable1D derivative() function return
	*       Double.NaN when you want this routine
	*       to use Brent's method.  Newton-Raphson will be used if a
	*       derivative function is provided that returns something other
	*       than Double.NaN. The function() method will always
    *       be called before the derivative() method. The value returned
    *       from the 1st call to the function() method is ignored.</p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  bracket The upper and lower bracket surrounding the root (assumed that
	*                  root lies inside the bracket).
	*  @param  tol     The root will be refined until it's accuracy is
	*                  better than this.
	*  @return The value of x that solves the equation f(x) = 0.
	*  @exception  RootException  Unable to find a root of the function.
	**/
	public static double findRoot1D( Evaluatable1D eval, BracketRoot1D bracket, double tol ) throws RootException  {
		return findRoot1D(eval, bracket.x1, bracket.x2, tol);
	}
	
	
	/**
	*  <p>  Find the root of a general 1D function f(x) = 0 known
	*       to lie between x1 and x2.  The root will be refined
	*       until it's accuracy is "tol".  </p>
	*
	*  <p>  Have your Evaluatable1D derivative() function return
	*       Double.NaN when you want this routine
	*       to use Brent's method.  Newton-Raphson will be used if a
	*       derivative function is provided that returns something other
	*       than Double.NaN. The function() method will always
    *       be called before the derivative() method. The value returned
    *       from the 1st call to the function() method is ignored.</p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  x1      The lower bracket surrounding the root (assumed that
	*                  root lies between x1 and x2).
	*  @param  x2      The upper bracket surrounding the root (assumed that
	*                  root lies between x1 and x2).
	*  @param  tol     The root will be refined until it's accuracy is
	*                  better than this.
	*  @return The value of x that solves the equation f(x) = 0.
	*  @exception  RootException  Unable to find a root of the function.
	**/
	public static double findRoot1D( Evaluatable1D eval, double x1,
									double x2, double tol ) throws RootException  {
		if (tol < EPS)  tol = EPS;
        eval.function(x1);
		double value = eval.derivative(x1);
		
		if (Double.isNaN(value))
			//	No derivative information available, use Brent's method.
			value = zeroin(eval, x1, x2, tol);
			
		else
			//	Derivative info is available, use Newton-Raphson.
			value = newton(eval, x1, x2, tol);

		return value;
	}


	/**
	*  <p>  Find the root of a function f(x) = 0 known to lie between
	*       x1 and x2.  The root will be refined until it's accuracy
	*       is "tol".  This is the method of choice to find a bracketed
	*       root of a general 1D function when you can not easily compute
	*       the function's derivative.  </p>
	*
	*  <p>  Uses Van Wijngaarden-Dekker-Brent method (Brent's method).
	*       Algorithm:
	*           G.Forsythe, M.Malcolm, C.Moler, Computer methods for mathematical
	*           computations. M., Mir, 1980, p.180 of the Russian edition. </p>
	*
	*  <p>  Ported to Java from Netlib C version by:
	*           Joseph A. Huwaldt, July 10, 2000      </p>
	*
	*  <p>  This function makes use of the bisection procedure combined with
	*       linear or inverse quadric interpolation.
	*       At every step the program operates on three abscissae - a, b, and c.
	*       b - the last and the best approximation to the root,
	*       a - the last but one approximation,
	*       c - the last but one or even earlier approximation than a that
	*           1) |f(b)| <= |f(c)|,
	*           2) f(b) and f(c) have opposite signs, i.e. b and c confine
	*              the root.
	*       At every step zeroin() selects one of the two new approximations, the
	*       former being obtained by the bisection procedure and the latter
	*       resulting in the interpolation (if a,b, and c are all different
	*       the quadric interpolation is utilized, otherwise the linear one).
	*       If the latter (i.e. obtained by the interpolation) point is 
	*       reasonable (i.e. lies within the current interval [b,c] not being
	*       too close to the boundaries) it is accepted. The bisection result
	*       is used otherwise. Therefore, the range of uncertainty is
	*       ensured to be reduced at least by the factor 1.6 on each iteration.
	*  </p>
	*
	*  @param   eval    An evaluatable 1D function that returns the
	*                   function value at x.
	*  @param   x1, x2  The bracket surrounding the root (assumed that
	*                   root lies between x1 and x2).
	*  @param   tol     The root will be refined until it's accuracy is
	*                   better than this.
	*  @return  The value x that solves the equation f(x) = 0.
	*  @exception  RootException  Unable to find a root of the function.
	**/
	private static double zeroin(Evaluatable1D eval, double x1, double x2, double tol)
															throws RootException {
		double a=x1, b=x2, c=x1;		//	Abscissae, descr. see above.
		double fa = eval.function(a);	//	f(a)
		double fb = eval.function(b);	//	f(b)
		double fc = fa;					//	f(c)

		if (DEBUG)
			System.out.println("Using zeroin()...");
			
		// Check for bracketing.
		if ( (fa > 0.0 && fb > 0.0) || (fa < 0.0 & fb < 0.0) )
			throw new RootException("Root must be bracketed");
		
		//	Main iteration loop
		for(int iter=0; iter < MAXIT; ++iter) {
			if (DEBUG)
				System.out.println("Iteration #" + iter);

			double prev_step = b - a;	//	Distance from the last but one to the last approx.
			double tol_act;				//	Actual tolerance
    		double p;      				//	Interp step is calculated in the form p/q;
    		double q;					//		division is delayed until the last moment.
			double new_step;      		//	Step at this iteration
   
   			if( abs(fc) < abs(fb) ) {
				a = b;					//	Swap data for b to be the best approximation.
				b = c;
				c = a;
				fa=fb;
				fb=fc;
				fc=fa;
			}
			
			// Convergence check.
    		tol_act = 2.*EPS*abs(b) + tol*0.5;
			new_step = 0.5*(c - b);

			if( abs(new_step) <= tol_act || fb == 0.0 )
				return b;				//	An acceptable approximation was found.

			//	Decide if the interpolation can be tried.
			if( abs(prev_step) >= tol_act && abs(fa) > abs(fb) ) {
				// If prev_step was large enough and was in true direction,
				// inverse quadratic interpolation may be tried.
				double s = fb/fa;
				double cb = c - b;
				if( a == c ) {			//	If we have only two distinct points then only
					p = cb*s;			//	linear interpolation can be applied.
					q = 1.0 - s;
				} else {
					//	Quadric inverse interpolation.
					double t = fa/fc;
					double r = fb/fc;
					p = s*( cb*t*(t - r) - (b - a)*(r - 1.0) );
					q = (t - 1.0)*(r - 1.0)*(s - 1.0);
				}
				//  Check wether in bounds
				if( p > 0.0 )			//	p was calculated with the opposite sign;
					q = -q;				//		make p positive and assign possible minus to q.
				else
					p = -p;

				double min1 = (0.75*cb*q - abs(tol_act*q*0.5));
				double min2 = abs(prev_step*q*0.5);
				if (p < min1 && p < min2) {
					/*	If b+p/q falls in [b,c] and isn't too large it is accepted.
						If p/q is too large then use the bisection procedure which
						will reduce the [b,c] range to a greater extent.	*/
					new_step = p/q;
				}
			}

			if( abs(new_step) < tol_act )	//	Adjust the step to be not less than
				if( new_step > 0.0 )			//		tolerance.
					new_step = tol_act;
				else
					new_step = -tol_act;

			a = b;						//	Save the previous approximation.
			fa = fb;
			b += new_step;
			fb = eval.function(b);		//	Do step to a new approxim.
			if( (fb > 0. && fc > 0.) || (fb < 0. && fc < 0.) ) {
				c = a;					//	Adjust c for it to have a sign opp. to that of b.
				fc = fa;
			}
		}
		
		// Max iterations exceeded.
		throw new RootException("Maximun number of iterations exceeded");
		
	}


	/**
	*  <p>  Find the root of a function f(x) = 0 known to lie between
	*       x1 and x2.  The root will be refined until it's accuracy
	*       is known within "±tol".  This is the method of choice to find a
	*       bracketed root of a general 1D function when you are able to
	*       supply the derivative of the function with x.  </p>
	*
	*  <p>  Uses a combination of Newton-Raphson and bisection. </p>
	*
	*  <p>  Original FORTRAN program, obtained from Netlib, bore this message:
	*       NUMERICAL METHODS: FORTRAN Programs, (c) John H. Mathews 1994.
	*       NUMERICAL METHODS for Mathematics, Science and Engineering, 2nd Ed, 1992,
	*       Prentice Hall, Englewood Cliffs, New Jersey, 07632, U.S.A.
	*       This free software is complements of the author.
	*       Algorithm 2.5 (Newton-Raphson Iteration).
	*       Section 2.4, Newton-Raphson and Secant Methods, Page 84.  </p>
	*
	*  <p>  Ported from FORTRAN to Java by:  Joseph A. Huwaldt, July 11, 2000.
	*       Root bracketing and bisection added to avoid pathologies as suggested by:
	*       Numerical Recipes in C, 2nd Edition, pg. 366.  </p>
	*
	*  @param   eval    An evaluatable 1D function that returns the function
	*                   value at x and optionally returns the function
	*                   derivative value (d(fx)/dx) at x.  It is guaranteed
	*                   that "function()" will always be called before
	*                   "derivative()".
	*  @param   x1, x2  The bracket surrounding the root (assumed that root
	*                   lies between x1 and x2).
	*  @param   tol     The root will be refined until it's accuracy is better
	*                   than this.
	*  @return  The value x that solves the equation f(x) = 0.
	*  @exception  RootException  Unable to find a root of the function.
	**/
	private static double newton(Evaluatable1D eval, double x1,
									double x2, double tol) throws RootException {
		
		if (DEBUG)
			System.out.println("Using newton()...");
			
		// Evaluate the function low and high.
		double fl = eval.function(x1);
		double fh = eval.function(x2);
		
		// Check for bracketing.
		if((fl > 0.0 && fh > 0.0) || (fl < 0.0 && fh < 0.0))
			throw new RootException("Root must be bracketed");
		
		// If either end of the bracket is the root, we are done.
		if (fl == 0.0) return x1;
		if (fh == 0.0) return x2;
		
		double xh, xl;
		if (fl < 0.0) {
			//  Orient the search so that fn(x1) < 0.
			xl = x1;
			xh = x2;
		} else {
			xh = x1;
			xl = x2;
		}
		
		double p0 = 0.5*(x1 + x2);				//	Initialize the guess for root.
		double y0 = eval.function(p0);			//	Do initial function evaluation.
		
		double dp_old = abs(x2 - x1);		//	Step size before last.
		double dp = dp_old;						//	Last step size.
		
		// Loop over allowed iterations.
		for (int k=0; k < MAXIT; ++k) {
			if (DEBUG)
				System.out.println("Iteration #" + k);
				
			double df = eval.derivative(p0);	// Evaluate the derivative.

			if ( (((p0 - xh)*df - y0)*((p0 - xl)*df - y0) >= 0.0)
						|| (abs(2.0*y0) > abs(dp_old*df)) ) {
				// Use bisection if Newton is out of range or not decreasing fast enough.
				dp_old = dp;
				dp = 0.5*(xh - xl);
				p0 = xl + dp;
				if (xl == p0) return p0;		//	Change in root is negligable.
				
			} else {
				//	Do a regular Newton step.
				dp_old = dp;
				dp = y0/df;
				double p1 = p0;
				p0 = p0 - dp;
				if (p1 == p0)	return p0;		//	Change in root is negligable.
			}
			
			// Do a convergence check.
			if (abs(dp) < tol) return p0;
			
			// Do a function evaluation.
			y0 = eval.function(p0);
			if (y0 < 0.0)						//  Maintain the bracket on the root.
				xl = p0;
			else
				xh = p0;
		}
		
		throw new RootException("Maximun number of iterations exceeded");
		
	}
	
	/**
	*  Find a root of a 1D equation using the Aitken method.
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  x       A starting guess at the root value.
	*  @param  tol     The root will be refined until it's accuracy is better than this.
	*  @return The value of x that solves the equation f(x) = 0 to within the specified tolerance.
	*  @exception  RootException  Unable to find a root of the function.
	**/
	public static double aitken(Evaluatable1D eval, double x, double tol) throws RootException {
		//	Reference: http://www.tm.bi.ruhr-uni-bochum.de/profil/mitarbeiter/meyers/Aitken.html
		if (tol < EPS)  tol = EPS;
		double fx0 = 1e99;
		double fac = 1.0;
		int m = MAXIT;
		int n = 0;
		while (m > n) {
			double fx1 = eval.function(x);
			++n;
			double d = fx0 - fx1;
			if (abs(d) < tol) {
				x += fx1;
				return x;
			}
			fac *= fx0/d;
			x += fac*fx1;
			fx0 = fx1;
		}
		throw new RootException("Maximun number of iterations exceeded");
	}
	
	
	/**
	*  <p>  Find the roots of a set of N non-linear equations in N variables.  </p>
	*
	*  <p>  Uses a globally convergent Newton's method with either a Jacobian supplied by
	*       the VectorFunction or one computed by differencing.  </p>
	*
	*  <p>  Reference:  Numerical Recipes in C, 2nd Edition, pg 386. </p>
	*
	*  @param  vecfunc A VectorFunction that computes the values for each equation
	*                  using the input values in x[] and optionally the Jacobian.
	*  @param  x       A pre-existing array that contains the initial guesses at the x values.
	*                  On completion, it will contain the roots of the equations.
	*  @param  n       The number of equations to be solved (and the number of elements in x.
	*  @return <code>true</code> if the algorithm converges to a local minimum, otherwise <code>false</code>.
	*  @exception  RootException  Unable to find a roots of the functions.
	**/
	public static boolean findRootsND( VectorFunction vecfunc, double[] x, int n ) throws RootException  {
		return newtonND(vecfunc, x, n);
	}


	private static boolean newtonND(VectorFunction vecfunc, double[] x, int n) throws RootException {
		
		int[] indx = new int[n];
		double[] g = new double[n];
		double[] p = new double[n];
		double[] xold = new double[n];
		double[][] fjac = new double[n][n];
		double[] fvec = new double[n];
		
		double f = fmin(x, fvec, n, vecfunc);
		double test=0.0;
		for (int i=0;i < n;i++)
			if (abs(fvec[i]) > test) test=abs(fvec[i]);
		if (test < 0.01*TOLF) {
			return false;
		}
		double sum=0.0;
		for (int i=0;i < n;i++) {
            double xi = x[i];
            sum += xi*xi;
        }
		double stpmax=STPMX*max(sqrt(sum),n);
		for (int its=0;its < MAXIT;its++) {
			if (!vecfunc.jacobian(n, x, fjac))
				fdjac(x,fvec,fjac,n,vecfunc);
			for (int i=0;i < n;i++) {
				sum=0.0;
				for (int j=0;j < n;j++) sum += fjac[j][i]*fvec[j];
				g[i]=sum;
			}
            System.arraycopy(x, 0, xold, 0, n);     //  for (int i=0;i < n;i++) xold[i]=x[i];
			double fold=f;
			for (int i=0;i < n;i++) p[i] = -fvec[i];
			double d = ludcmp(fjac,indx,n);
			lubksb(fjac,indx,p,n);
			boolean check = lnsrch(xold,n,fold,g,p,x,f,stpmax, fvec,vecfunc);
			test=0.0;
			for (int i=0;i < n;i++)
				if (abs(fvec[i]) > test) test=abs(fvec[i]);
			if (test < TOLF) {
				return false;
			}
			if (check) {
				test=0.0;
				double den = max(f,0.5*n);
				for (int i=0;i < n;i++) {
					double temp = abs(g[i])*max(abs(x[i]),1.0)/den;
					if (temp > test) test=temp;
				}
				return (test < TOLMIN);
			}
			test=0.0;
			for (int i=0;i < n;i++) {
				double temp = (abs(x[i]-xold[i]))/max(abs(x[i]),1.0);
				if (temp > test) test=temp;
			}
			if (test < EPS) {
				return check;
			}
		}
		
		throw new RootException("MAXIT exceeded in newtonND");
	}
	
	
	private static boolean lnsrch(double[] xold, int n, double fold, double[] g, double[] p,
						double[] x, double f, double stpmax, double[] fvec, VectorFunction vecfunc) throws RootException {
		
		double sum = 0;
		for (int i=0;i < n;i++) {
            double pi = p[i];
            sum += pi*pi;
        }
		sum = sqrt(sum);
		if (sum > stpmax)
			for (int i=0;i < n;i++) p[i] *= stpmax/sum;
		double slope=0.0;
		for (int i=0;i < n;i++)
			slope += g[i]*p[i];
		if (slope >= 0.0) throw new RootException("Roundoff problem in lnsrch.");
		double test = 0;
		for (int i=0;i < n;i++) {
			double temp = abs(p[i])/max(abs(xold[i]),1.0);
			if (temp > test) test=temp;
		}
		double alamin = EPS/test;
		double alam = 1.0;
		double tmplam, alam2=0, f2=0;
		while (true) {
			for (int i=0;i < n;i++) x[i] = xold[i]+alam*p[i];
			f=fmin(x, fvec, n, vecfunc);
			if (alam < alamin) {
                System.arraycopy(xold, 0, x, 0, n);     //  for (int i=0;i < n;i++) x[i]=xold[i];
				return true;
			} else if (f <= fold+ALF*alam*slope) {
				return false;
			} else {
				if (alam == 1.0)
					tmplam = -slope/(2.0*(f-fold-slope));
				else {
					double rhs1 = f-fold-alam*slope;
					double rhs2 = f2-fold-alam2*slope;
					double a = (rhs1/(alam*alam)-rhs2/(alam2*alam2))/(alam-alam2);
					double b = (-alam2*rhs1/(alam*alam)+alam*rhs2/(alam2*alam2))/(alam-alam2);
					if (a == 0.0)
						tmplam = -slope/(2.0*b);
					else {
						double disc = b*b-3.0*a*slope;
						if (disc < 0.0) tmplam = 0.5*alam;
						else if (b <= 0.0) tmplam = (-b+sqrt(disc))/(3.0*a);
						else tmplam = -slope/(b+sqrt(disc));
					}
					if (tmplam > 0.5*alam)
						tmplam=0.5*alam;
				}

			}
			alam2=alam;
			f2 = f;
			alam=max(tmplam,0.1*alam);
		}
	}
	
	private static void lubksb(double[][] a, int[] indx, double[] b, int n) throws RootException {
		int ii = 0;
		for (int i=0;i < n;i++) {
			int ip=indx[i];
			double sum=b[ip];
			b[ip]=b[i];
			if (ii != 0)
				for (int j=ii-1;j < i;j++) sum -= a[i][j]*b[j];
			else if (sum != 0.0)
				ii=i+1;
			b[i]=sum;
		}
		for (int i=n-1;i >= 0;i--) {
			double sum=b[i];
			for (int j=i+1;j < n;j++) sum -= a[i][j]*b[j];
			b[i]=sum/a[i][i];
		}
	}
	
	
	private static double ludcmp(double[][] a, int[] indx, int n) throws RootException {
		
		double[] vv = new double[n];
		double d=1.0;
		for (int i=0;i < n;i++) {
			double big=0.0;
			double temp;
			for (int j=0;j < n;j++)
				if ((temp = abs(a[i][j])) > big) big=temp;
			if (big == 0.0) throw new RootException("Singular matrix in routine ludcmp");
			vv[i] = 1.0/big;
		}
		int imax = 0;
		for (int j=0;j < n;j++) {
			for (int i=0;i < j;i++) {
				double sum = a[i][j];
				for (int k=0;k < i;k++) sum -= a[i][k]*a[k][j];
				a[i][j] = sum;
			}
			double big=0.0;
			for (int i=j;i < n;i++) {
				double sum=a[i][j];
				for (int k=0;k < j;k++) sum -= a[i][k]*a[k][j];
				a[i][j]=sum;
				double dum;
				if ((dum=vv[i]*abs(sum)) >= big) {
					big=dum;
					imax=i;
				}
			}
			if (j != imax) {
				for (int k=0;k < n;k++) {
					double dum=a[imax][k];
					a[imax][k]=a[j][k];
					a[j][k]=dum;
				}
				d = -d;
				vv[imax]=vv[j];
			}
			indx[j]=imax;
			if (a[j][j] == 0.0) a[j][j]=TINY;
			if (j != n-1) {
				double dum=1.0/(a[j][j]);
				for (int i=j+1;i < n;i++) a[i][j] *= dum;
			}
		}
		
		return d;
	}
	
	private static void fdjac(double[] x, double[] fvec, double[][] df, int n, VectorFunction vecfunc) throws RootException {
		double[] f = new double[n];
		for (int j=0;j < n;j++) {
			double temp = x[j];
			double h = TOLF*abs(temp);
			if (h == 0.0) h = TOLF;
			x[j] = temp+h;
			h = x[j]-temp;
			vecfunc.function(n,x,f);
			x[j] = temp;
			for (int i=0;i < n;i++)
				df[i][j] = (f[i]-fvec[i])/h;
		}
	}
	
	private static double fmin(double[] x, double[] fvec, int n, VectorFunction vecfunc) throws RootException {
		vecfunc.function(n,x,fvec);
		double sum = 0;
		for (int i=0;i < n;i++) {
            double fveci = fvec[i];
            sum += fveci*fveci;
        }
		return 0.5*sum;
	}
	
	
	/**
	*  Used to test out the methods in this class.
	**/
	public static void main(String args[]) {
	
		System.out.println();
		System.out.println("Testing Roots...");
		
		try {
			// Find a root of f(x) = x^3 - 3*x + 2.
			
			// First create an instance of our evaluatable function.
			Evaluatable1D function = new DemoFunction1D();
			
			// Then call the root finder with brackets on the root and a tolerance.
			// The brackets can be used to isolate which root you want (for instance).
			double root = findRoot1D( function, -10, 0, 0.0001 );
//			double root = aitken( function, 0, 0.0001 );
			
			System.out.println("    A root of f(x) = x^3 - 3*x + 2 is " + root);
			
			
			//	Find the roots to: x0^2 + x1^2 - 2; and e^(x0 - 1) + x1^3 - 2
			VectorFunction funcND = new DemoFunctionND();
			double[] f = new double[2];
			double[] x = new double[2];
			x[0]=2.0;
			x[1]=0.5;
			boolean check = findRootsND(funcND, x, 2);
			funcND.function(2,x,f);
			if (check) System.out.println("Convergence problems.");
			System.out.println("Roots of x0^2 + x1^2 - 2; and e^(x0 - 1) + x1^3 - 2 are:");
			System.out.printf("%7s %3s %12s\n","Index","x","f");
			for (int i=0;i < 2;i++) System.out.printf("%5d %12.6f %12.6f\n",i+1,x[i],f[i]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	*  <p>  A simple demonstration class that shows how to
	*       use the Evaluatable1D class to find the roots
	*       of a 1D equation.  Since this class returns
	*       a value for derivative(), we are signaling
	*       the root finder that we want it to use the
	*       Newton-Raphson technique to find the root of the
	*       equation.  </p>
	**/
	private static class DemoFunction1D implements Evaluatable1D {

		/**
		*  User supplied method that calculates the function y = fn(x).
		*  This demonstration returns the value of y = x^3 - 3*x + 2.
		*
		*  @param   x  Independent parameter to the function.
		*  @return  The x^3 - 3*x + 2 evaluated at x.
		**/
        @Override
		public double function(double x) {
			return (x*x*x - 3.*x + 2.);
		}
		
		/**
		*  Calculates the derivative of the function dy/dx = d fn(x)/dx.
		*  This demonstration returns dy/dx = 3*x^2 - 3.
		*
		*  @param   x  Independent parameter to the function.
		*  @return  The 3*x^2 - 3 evaluated at x.
		**/
        @Override
		public double derivative(double x) {
			return (3.*x*x - 3.);
		}

	}

	/**
	*  A demonstration function for use with the newtonND multi-dimensional
	*  root finder.
	**/
	private static class DemoFunctionND implements VectorFunction {
		
		/**
		*  User supplied method that calculates the function y[] = fn(x[]).
		*
		*  @param   n  The number of variables in the x & y arrays.
		*  @param   x  Independent parameters to the function, passed in as input.
		*  @param   y  An existing array that is filled in with the outputs of the function
		**/
        @Override
		public void function(int n, double x[], double[] y) throws RootException {
			double x0 = x[0];
			double x1 = x[1];
			double x12 = x1*x1;
			y[0] = x0*x0 + x12 - 2.0;
			y[1] = exp(x0 - 1.0) + x1*x12 - 2.0;
		}

		/**
		*  User supplied method that calculates the Jacobian of the function.
		*
		*  @param n The number of rows and columns in the Jacobian.
		*  @param jac The Jacobian array.
		*  @return True if the Jacobian was computed by this method, false if it was not.
		**/
        @Override
		public boolean jacobian(int n, double[] x, double[][] jac) {
			return false;
		}
	}
}

