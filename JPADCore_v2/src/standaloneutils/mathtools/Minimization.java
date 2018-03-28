/*
*   Minimization  -- A collection of static methods for finding the minima or maxima of functions.
*
*   Copyright (C) 2006-2014 by Joseph A. Huwaldt
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
*  A collection of static routines to find the minima or maxima
*  of functions or sets of functions.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  July 22, 2006
*  @version  January 23, 2014
**/
public class Minimization {

	/**
	*  Machine floating point precision.
	**/
	private static final double ZEPS = 1e-10;
	
	/**
	*  Maximum number of allowed iterations.
	**/
	private static final int  MAXIT = 1000;
	
	/**
	*  The default ratio by which successive intervals are magnified.
	**/
	private static final double GOLD = 1.618034;

	/**
	*  The golden ratio.
	**/
	private static final double CGOLD = 0.3819660;
	
	private static final double GLIMIT = 100;
	private static final double TINY = 1.0e-20;
	private static final double TINY2 = 1.0e-25;
	private static final double TOL = 1.0e-8;
	
	
	//-----------------------------------------------------------------------------------
	/**
	*  <p>  Method that isolates the minimum of a 1D function to a fractional precision
	*       of about "tol". A version of Brent's method is used with derivative
	*       information if it is available or a version without if it is not.</p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  x1      The lower bracket surrounding the minimum (assumed that
	*                  minimum lies between x1 and x2).
	*  @param  x2      The upper bracket surrounding the root (assumed that
	*                  minimum lies between x1 and x2).
	*  @param  tol     The root will be refined until it's accuracy is
	*                  better than this.  This should generally not be smaller
	*                  than Math.sqrt(EPS)!
	*  @param  output  An optional 2-element array that will be filled in with the abscissa (x) and
	*                  function minimum ordinate ( f(x) ) on output.  output[xmin,f(xmin)].
	*                  Pass "null" if this is not required.
	*  @return The abscissa (x) of the minimum value of f(x).
	*  @exception  RootException  Unable to find a minimum of the function.
	**/
	public static double find( Evaluatable1D eval, double x1, double x2, double tol, double[] output ) throws RootException  {
		
		//  First find an initial triplet that brackets the minimum.
		double[] triplet = new double[3];
		bracket1D(x1, x2, triplet, null, eval);
		
		//	Now find the minimum.
		return find(eval, triplet[0], triplet[1], triplet[2], tol, output);
	}


	/**
	*  <p>  Method that isolates the minimum of a 1D function to a fractional precision
	*       of about "tol".  A version of Brent's method is used with derivative
	*       information if it is available or a version without if it is not.</p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".  The return value from the 1st
    *                  call to "function()" is ignored.
	*  @param  ax      The lower bracket known to surround the minimum (assumed that
	*                  minimum lies between ax and cx).
	*  @param  bx      Abscissa point that lies between ax and cx where f(bx) is less than
	*                  both f(ax) and f(cx).
	*  @param  cx      The upper bracket known to surround the root (assumed that
	*                  minimum lies between ax and cx).
	*  @param  tol     The root will be refined until it's accuracy is
	*                  better than this.  This should generally not be smaller
	*                  than Math.sqrt(EPS)!
	*  @param  output  An optional 2-element array that will be filled in with the abscissa (x) and
	*                  function minimum ordinate ( f(x) ) on output.  output[xmin,f(xmin)].
	*                  Pass "null" if this is not required.
	*  @return The abscissa (x) of the minimum value of f(x).
	*  @exception  RootException  Unable to find a minimum of the function.
	**/
	public static double find( Evaluatable1D eval, double ax, double bx, double cx, double tol, double[] output ) throws RootException  {
		if (tol < MathTools.EPS)  tol = MathTools.EPS;
		double f = eval.function(bx);
		double d = eval.derivative(bx);
		if (Double.isNaN(d))
			return brent(eval, ax, bx, cx, tol, output);
		return dbrent(eval, ax, bx, cx, tol, output);
	}
	
	
	/**
	*  Method that searches in the downhill direction (defined by the function as
	*  evaluated at the initial points) and returns new points (in triple) that bracket
	*  a minimum of the function.
	*
	*  @param  ax      The lower bracket surrounding the minimum (assumed that
	*                  minimum lies between ax and bx).
	*  @param  bx      The upper bracket surrounding the root (assumed that
	*                  minimum lies between ax and bx).
	*  @param  triple  A 3-element array that will be filled in with the abcissa of the
	*                  three points that bracket the minium triple[ax,bx,cx].
	*  @param  values  A 3-element array that contains the function values that correspond
	*                  with the points ax, bx and cx in triple.  Null may be passed for this
	*                  if the function values are not required.
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	**/
	public static void bracket1D(double ax, double bx, double[] triple, double[] values, Evaluatable1D eval)
						throws RootException {
		double fa = eval.function(ax);
		double fb = eval.function(bx);
		if (fb > fa) {
			//  Switch roles of a and b so that we can go downhill in the direction from a to b.
			double dum = ax;
			ax = bx;
			bx = dum;
			dum = fb;
			fb = fa;
			fa = dum;
		}
		
		double cx = bx+GOLD*(bx-ax);
		double fc = eval.function(cx);
		double fu;
		while (fb > fc) {
			//  Keep returning here until we bracket.
			double r = (bx - ax)*(fb - fc);		//  Compute u by parabolic interpolation from a,b,c.
			double q = (bx - cx)*(fb - fa);
			double u = bx - ((bx - cx)*q - (bx - ax)*r)/
							(2.0*MathTools.sign(max(abs(q-r),TINY), q-r));
			double ulim = bx + GLIMIT*(cx - bx);
			
			if ((bx - u)*(u - cx) > 0.0) {
				//  Parabolic u is between b and c.
				fu = eval.function(u);
				if (fu < fc) {
					//  Got a minimum between b and c.
					ax = bx;
					bx = u;
					fa = fb;
					fb = fu;
					triple[0] = ax;
					triple[1] = bx;
					triple[2] = cx;
					if (values != null) {
						values[0] = fa;
						values[1] = fb;
						values[2] = fc;
					}
					return;
				
				} else if (fu > fb) {
					//  Got a minimum between a and u.
					cx = u;
					fc = fu;
					triple[0] = ax;
					triple[1] = bx;
					triple[2] = cx;
					if (values != null) {
						values[0] = fa;
						values[1] = fb;
						values[2] = fc;
					}
					return;
				}
				u = cx + GOLD*(cx - bx);	//  Parabolic fit was no use.  Use default magnification.
				fu = eval.function(u);
				
			} else if ((cx - u)*(u - ulim) > 0.0) {
				//  Parabolic fit is between c and it's allowed limit.
				fu = eval.function(u);
				if (fu < fc) {
					bx = cx;
					cx = u;
					u = u + GOLD*(u - bx);
					fb = fc;
					fc = fu;
					fu = eval.function(u);
				}
				
			} else if ((u - ulim)*(ulim - cx) >= 0.0) {
				//  Limit parabolic u to maximum allowed value.
				u = ulim;
				fu = eval.function(u);
			
			} else {
				//  Reject parabolic u, use default magnification.
				u = cx + GOLD*(cx - bx);
				fu = eval.function(u);
			}
			
			//  Eliminate oldest point and continue.
			ax = bx;
			bx = cx;
			cx = u;
			fa = fb;
			fb = fc;
			fc = fu;
		}

		triple[0] = ax;
		triple[1] = bx;
		triple[2] = cx;
		if (values != null) {
			values[0] = fa;
			values[1] = fb;
			values[2] = fc;
		}

	}
	
	
	/**
	*  <p>  Method that isolates the minimum of a 1D function to a fractional precision
	*       of about "tol" using Brent's method.  </p>
	*
	*  <p> Ported from brent() code found in _Numerical_Recipes_in_C:_The_Art_of_Scientific_Computing_,
	*      2nd Edition, 1992.  </p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and optionally returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  ax      The lower bracket surrounding the minimum (assumed that
	*                  minimum lies between ax and cx).
	*  @param  bx      Abscissa point that lies between ax and cx where f(bx) is less than
	*                  both f(ax) and f(cx).
	*  @param  cx      The upper bracket surrounding the root (assumed that
	*                  minimum lies between ax and cx).
	*  @param  tol     The minimum will be refined until it's accuracy is
	*                  better than this.  This should generally not be smaller
	*                  than Math.sqrt(EPS)!
	*  @param  output  An optional 2-element array that will be filled in with the abscissa (x) and
	*                  function minimum ordinate ( f(x) ) on output.  output[xmin,f(xmin)].
	*                  Pass <code>null</code> if this is not required.
	*  @return The abscissa (x) of the minimum value of f(x).
	*  @exception  RootException  Unable to find a minimum of the function.
	**/
	private static double brent( Evaluatable1D eval, double ax, double bx, double cx, double tol, double[] output ) throws RootException  {
		
		double e = 0;		//  This is the distance moved on the step before last.
		
		//  a and b must be in ascending order, but the input abscissas need not be.
		double a = (ax < cx ? ax : cx);
		double b = (ax > cx ? ax : cx);
		double x = bx, w = bx, v = bx;
		double fx = eval.function(x);
		double fw = fx, fv = fx;
		double u, fu, d = 0;
		
		//  Main program loop.
		for (int iter = 0; iter < MAXIT; ++iter) {
			double xm = 0.5*(a + b);
			double tol1 = tol*abs(x) + ZEPS;
			double tol2 = 2.0*tol1;
			
			//  Test for done here.
			if (abs(x-xm) <= (tol2 - 0.5*(b-a))) {
				if (output != null) {
					output[0] = x;
					output[1] = fx;
				}
				return x;
			}
			
			if (abs(e) > tol1) {
				//  Construct a trial parabolic fit.
				double r = (x - w)*(fx - fv);
				double q = (x - v)*(fx - fw);
				double p = (x - v)*q - (x - w)*r;
				q = 2.0*(q - r);
				if (q > 0.0)	p = -p;
				q = abs(q);
				double etemp = e;
				e = d;
				if (abs(p) >= abs(0.5*q*etemp) || p <= q*(a-x) || p >= q*(b-x)) {
					//  Take a golden ratio step.
					e = (x >= xm ? a - x : b - x);
					d = CGOLD*e;
					
				} else {
					//  Take a parabolic step.
					d = p/q;
					u = x + d;
					if (u - a < tol2 || b - u < tol2)
						d = MathTools.sign(tol1, xm - x);
				}
			
			} else {
				//  Take a golden ratio step.
				e = (x >= xm ? a - x : b - x);
				d = CGOLD*e;
			}
			
			u = (abs(d) >= tol1 ? x + d : x + MathTools.sign(tol1, d));
			fu = eval.function(u);			//  This is the one function eval per iteration.
			
			if (fu <= fx) {
				if (u >= x)
					a = x;
				else
					b = x;
				v = w;
				w = x;
				x = u;
				fv = fw;
				fw = fx;
				fx = fu;
				
			} else {
				if (u < x)
					a = u;
				else
					b = u;
				if (fu <= fw || w == x) {
					v = w;
					w = u;
					fv = fw;
					fw = fu;
					
				} else if (fu <= fv || v == x || v == w) {
					v = u;
					fv = fu;
				}
			}
			
		}   //  Next iter
		
		// Max iterations exceeded.
		throw new RootException("Maximum number of iterations exceeded");
	
	}
	
	
	/**
	*  <p>  Method that isolates the minimum of a 1D function to a fractional precision
	*       of about "tol" using Brent's method plus derivatives.  </p>
	*
	*  <p> Ported from dbrent() code found in _Numerical_Recipes_in_C:_The_Art_of_Scientific_Computing_,
	*      2nd Edition, 1992, pg 406.  </p>
	*
	*  @param  eval    An evaluatable 1D function that returns the
	*                  function value at x and returns the
	*                  function derivative value (d(fx)/dx) at x. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivative()".
	*  @param  ax      The lower bracket surrounding the minimum (assumed that
	*                  minimum lies between ax and cx).
	*  @param  bx      Abscissa point that lies between ax and cx where f(bx) is less than
	*                  both f(ax) and f(cx).
	*  @param  cx      The upper bracket surrounding the root (assumed that
	*                  minimum lies between ax and cx).
	*  @param  tol     The minimum will be refined until it's accuracy is
	*                  better than this.  This should generally not be smaller
	*                  than Math.sqrt(EPS)!
	*  @param  output  An optional 2-element array that will be filled in with the abscissa (x) and
	*                  function minimum ordinate ( f(x) ) on output.  output[xmin,f(xmin)].
	*                  Pass <code>null</code> if this is not required.
	*  @return The abscissa (x) of the minimum value of f(x).
	*  @exception  RootException  Unable to find a minimum of the function.
	**/
	private static double dbrent( Evaluatable1D eval, double ax, double bx, double cx, double tol, double[] output )
							throws RootException  {
		double a=(ax < cx ? ax : cx);
		double b=(ax > cx ? ax : cx);
		double x=bx;
		double w=bx;
		double v=bx;
		double fw=eval.function(x);
		double fv=fw;
		double fx=fw;
		double fu;
		double dw=eval.derivative(x);
		double dv=dw;
		double dx=dw;
		double d=0.0;
		double e=0.0;
		double u;
		
		for (int iter=0;iter < MAXIT;iter++) {
			double xm=0.5*(a+b);
			double tol1 = tol*abs(x)+ZEPS;
			double tol2 = 2.0*tol1;
			if (abs(x-xm) <= (tol2-0.5*(b-a))) {
				if (output != null) {
					output[0] = x;
					output[1] = fx;
				}
				return x;
			}
			if (abs(e) > tol1) {
				double d1=2.0*(b-a);
				double d2=d1;
				if (dw != dx) d1=(w-x)*dx/(dx-dw);
				if (dv != dx) d2=(v-x)*dx/(dx-dv);
				double u1=x+d1;
				double u2=x+d2;
				boolean ok1 = (a-u1)*(u1-b) > 0.0 && dx*d1 <= 0.0;
				boolean ok2 = (a-u2)*(u2-b) > 0.0 && dx*d2 <= 0.0;
				double olde=e;
				e=d;
				if (ok1 || ok2) {
					if (ok1 && ok2)
						d = (abs(d1) < abs(d2) ? d1 : d2);
					else if (ok1)
						d = d1;
					else
						d = d2;
					if (abs(d) <= abs(0.5*olde)) {
						u = x+d;
						if (u-a < tol2 || b-u < tol2)
							d = MathTools.sign(tol1,xm-x);
					} else {
						d = 0.5*(e=(dx >= 0.0 ? a-x : b-x));
					}
				} else {
					d = 0.5*(e=(dx >= 0.0 ? a-x : b-x));
				}
			} else {
				d = 0.5*(e=(dx >= 0.0 ? a-x : b-x));
			}
			if (abs(d) >= tol1) {
				u = x+d;
				fu = eval.function(u);
			} else {
				u = x + MathTools.sign(tol1,d);
				fu=eval.function(u);
				if (fu > fx) {
					if (output != null) {
						output[0] = x;
						output[1] = fx;
					}
					return x;
				}
			}
			double du = eval.derivative(u);
			if (fu <= fx) {
				if (u >= x) a=x; else b=x;
				v = w;
				fv = fw;
				dv = dw;
				w = x;
				fw = fx;
				dw = dx;
				x = u;
				fx = fu;
				dx = du;
			} else {
				if (u < x) a=u; else b=u;
				if (fu <= fw || w == x) {
					v = w;
					fv = fw;
					dv = dw;
					w = u;
					fw = fu;
					dw = du;
				} else if (fu < fv || v == x || v == w) {
					v = u;
					fv = fu;
					dv = du;
				}
			}
		}
		
		// Max iterations exceeded.
		throw new RootException("Maximum number of iterations exceeded");
	}
	
	
	/**
	*  <p>  Method that isolates the minimum of an n-Dimensional scalar function
	*       to a fractional precision of about "tol".  If no derivatives are available
	*       then Powell's method is used.  If derivatives are available, then a
	*       Polak-Reibiere minimization is used.</p>
	*
	*  @param  func    An evaluatable n-D scalar function that returns the
	*                  function value at a point, p[1..n], and optionally returns the
	*                  function derivatives (d(fx[1..n])/dx) at p[1..n]. It is
	*                  guaranteed that "function()" will always be called
	*                  before "derivatives()".
	*  @param p        On input, this is the initial guess at the minimum point.  On output, this
	*                  is filled in with the values that minimize the function.
	*  @param n        The number of variables in the array p.
	*  @param tol      The convergence tolerance on the function value.
	*  @return The minimum value of f(p[1..n]).
	*  @exception  RootException if unable to find a minimum of the function.
	**/
	public static double findND( ScalarFunctionND func, double[] p, int n, double tol )
							throws RootException  {
		if (tol < MathTools.EPS)  tol = MathTools.EPS;
		double[] df = new double[n];
		double f = func.function(p);
		boolean hasDerivatives = func.derivatives(p, df);
		if (hasDerivatives)
			//	We have derivatives, so use Polak-Reibiere method.
			return frprmn(p, n, tol, func);
		//df = null;
		
		//	No derivatives, so use Powell's method.
		double[][] xi = new double[n][n];
        for (int i=0;i < n;i++)
          for (int j=0;j < n;j++)
            xi[i][j]=(i == j ? 1.0 : 0.0);
		
		return powell(p, n, xi, tol, func);
	}
	
	
	/**
	*  Given a starting point, p[1..n], a Powell's method minimization is
	*  performed on a function, func.
	*
	*  Reference:  Numerical Recipes in C, 2nd Edition, pg 417.
	*
	*  @param p  On input, this is the initial guess at the minimum point.  On output, this
	*            is filled in with the values that minimize the function.
	*  @param n  The number of variables in the array p.
	*  @param xi An existing nxn matrix who's columns contain the initial set of directions
	*            (usually the n unit vectors).
	*  @param ftol The convergence tolerance on the function value.
	*  @param func The function being minimized and it's derivative.
	*  @return The minimum function value.
	**/
	private static double powell(double[] p, int n, double[][] xi, double ftol, ScalarFunctionND func)
							throws RootException {
		double fptt;
		double[] pt = new double[n];
		double[] ptt = new double[n];
		double[] xit = new double[n];
		double fret = func.function(p);
        System.arraycopy(p, 0, pt, 0, n);   // for (int j=0;j < n;j++) pt[j]=p[j];	//	Save the initial point.
		for (int iter=0;;++iter) {
			double fp=fret;
			int ibig=0;
			double del=0.0;		//	Will be the biggest function decrease.
			
			//	Loop over all the directions in the set.
			for (int i=0;i < n;i++) {
				for (int j=0;j < n;j++) xit[j]=xi[j][i];	//	Copy the direction.
				fptt=fret;
				fret = linmin(n, p,xit,func);	//	Minimize along direction.
				
				//	Record if the largest decrease so far.
				if (abs(fptt-fret) > del) {
					del=abs(fptt-fret);
					ibig=i;
				}
			}
			
			//	Termination criteria.
            double ferr = 2.0*abs(fp-fret);
            double fcrit = ftol*(abs(fp)+abs(fret))+TINY2;
			if (ferr <= fcrit) {
				return fret;
			}
			if (iter == MAXIT) throw new RootException("Exceeded maximum iterations.");
			
			//	Construct extrapolated point and average direction moved and save old starting point.
			for (int j=0;j < n;j++) {
                double pj = p[j];
                double ptj = pt[j];
				ptt[j]=2.0*pj-ptj;
				xit[j]=pj-ptj;
				pt[j]=pj;
			}
			
			fptt = func.function(ptt);	//	Function value at extrapolated point.
			if (fptt < fp) {
				double tmp1 = fp-fret-del;
				double tmp2 = fp-fptt;
				double t = 2.0*(fp-2.0*fret+fptt)*tmp1*tmp1-del*tmp2*tmp2;
				if (t < 0.0) {
					//	Move to the minimum of the new direction, and save the new direciton.
					fret = linmin(n, p,xit,func);
					for (int j=0;j < n;j++) {
						xi[j][ibig] = xi[j][n-1];
						xi[j][n-1] = xit[j];
					}
				}
			}
		}	//	Next iteration
	}
	
	
	/**
	*  Given a starting point, p[1..n], a Polak-Reibiere minimization is
	*  performed on a function, func, using it's gradient.
	*
	*  Reference:  Numerical Recipes in C, 2nd Edition, pg 423.
	*
	*  @param p  On input, this is the initial guess at the minimum point.  On output, this
	*            is filled in with the variables that minimize the function.
	*  @param n  The number of variables in the array p.
	*  @param ftol The convergence tolerance on the function value.
	*  @param func The function being minimized and it's derivative.
	*  @return The minimum function value.
	**/
	private static double frprmn(double[] p, int n, double ftol, ScalarFunctionND func)
						throws RootException {

		double[] g = new double[n];
		double[] h = new double[n];
		double[] xi = new double[n];
		
		//	Initializations
		double fp = func.function(p);
		func.derivatives(p,xi);
		
		for (int j=0;j < n;j++) {
			g[j] = -xi[j];
		}
        System.arraycopy(g, 0, h, 0, n);
        System.arraycopy(g, 0, xi, 0, n);
		
		//	Loop over the iterations.
		for (int its=0;its < MAXIT;its++) {
			double fret = linmin(n, p,xi,func);
			if (2.0*abs(fret-fp) <= ftol*(abs(fret)+abs(fp)+TINY))
				return fret;	//	Normal return.
			fp=fret;
			func.derivatives(p,xi);
			double dgg = 0.0;
			double gg = 0.0;
			for (int j=0;j < n;j++) {
                double gj = g[j];
				gg += gj*gj;
                double xij = xi[j];
        //		dgg += xij*xij];				//	Statement for Fletcher-Reeves.
				dgg += (xij+gj)*xij;            //	Statement for Polak-Ribiere.
			}
			if (gg == 0.0)			//	Unlikely, if gradient is exactly 0, then we are already done.
				return fret;
			
			double gam = dgg/gg;
			for (int j=0;j < n;j++) {
				g[j] = -xi[j];
				xi[j]=h[j]=g[j]+gam*h[j];
			}
		}
		throw new RootException("Too many iterations required for minimization.");
	}
	
	/**
	*  Given an n-dimensional point, p[1..n], and an n-dimensional direction, xi[1..n],
	*  moves and resets p to where the function, func(p), takes on a minimum along the
	*  direction xi from p, and replaces xi by the actual vector displacement that p was
	*  moved.  Also returns the function value at p.
	*
	*  Reference:  Numerical Recipes in C, 2nd Edition, pg 419.
	**/
	private static double linmin(int n, double[] p, double[] xi, ScalarFunctionND func)
							throws RootException {
		
		Evaluatable1D f1dim = new F1DimFunction(p, xi, n, func);
		double[] triple = new double[3];
		double[] values = new double[3];
		bracket1D(0, 1, triple, values, f1dim);
		double ax = triple[0];
		double xx = triple[1];
		double bx = triple[2];
		
		double[] outputs = new double[2];
		double xmin = find(f1dim,ax,xx,bx,TOL,outputs);
		double fret = outputs[1];
		for (int j=0;j < n;j++) {
			xi[j] *= xmin;
			p[j] += xi[j];
		}
		
		return fret;
	}
	
	/**
	*  This is the value of the function, nrfunc, along the line going through the
	*  point p in the direction of xi.  This is used by linmin().
	**/
	private static class F1DimFunction extends AbstractEvaluatable1D {
		private double[] _pcom;
		private double[] _xicom;
		private int _ncom;
		private double[] _xt;
		private double[] _df;
		private ScalarFunctionND _nrfunc;
		
		public F1DimFunction(double[] p, double[] xi, int n, ScalarFunctionND nrfunc) {
			_pcom = p;
			_xicom = xi;
			_ncom = n;
			_xt = new double[n];
			_df = new double[n];
			_nrfunc = nrfunc;
		}
		
        @Override
		public double function(double x) throws RootException {
			for (int j=0;j < _ncom;j++)
				_xt[j] = _pcom[j] + x*_xicom[j];
			return _nrfunc.function(_xt);
		}
		
        @Override
		public double derivative(double x) throws RootException {
			boolean hasDerivatives = _nrfunc.derivatives(_xt, _df);
			if (hasDerivatives) {
				double df1 = 0;
				for (int j=0;j < _ncom;j++)
					df1 += _df[j]*_xicom[j];
				return df1;
			}
			return Double.NaN;
		}
	}

	/**
	*  Used to test out the methods in this class.
	**/
	public static void main(String args[]) {
	
		System.out.println();
		System.out.println("Testing Minimization...");
		
		try {
			// Find the minimum of f(x) = x^2 - 3*x + 2.
			
			// First create an instance of our evaluatable function.
			Evaluatable1D function = new DemoMinFunction();
			
			//  Create some temporary storage.
			double[] output = new double[2];
			
			// Then call the minimizer with brackets on the minima and a tolerance.
			// The brackets can be used to isolate which minima you want (for instance).
			double xmin = find( function, -10, 10, 0.0001, output );
			
			System.out.println("    Minimum of f(x) = x^2 - 3*x + 2 is x = " + (float)xmin +
										", f(x) = " + (float)output[1]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	*  <p>  A simple demonstration class that shows how to
	*       use the Evaluatable1D class to find the minimum
	*       of a 1D equation.  </p>
	**/
	private static class DemoMinFunction extends AbstractEvaluatable1D {

		/**
		*  User supplied method that calculates the function y = fn(x).
		*  This demonstration returns the value of y = x^2 - 3*x + 2.
		*
		*  @param   x  Independent parameter to the function.
		*  @return  The x^2 - 3*x + 2 evaluated at x.
		**/
        @Override
		public double function(double x) {
			return (x*x - 3.*x + 2.);
		}
		
		/**
		*  Calculates the derivative of the function dy/dx = d fn(x)/dx.
		*  This demonstration returns dy/dx = 2*x - 3.
		*
		*  @param   x  Independent parameter to the function.
		*  @return  The 2*x - 3 evaluated at x.
		**/
        @Override
		public double derivative(double x) {
			return (2.*x - 3.);
		}

	}

}
