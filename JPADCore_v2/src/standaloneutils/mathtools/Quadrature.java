/*
*   Quadrature  -- Methods for integrating a 1D function: I=int_a^b{f(x) dx}.
*
*   Copyright (C) 2009-2014 by Joseph A. Huwaldt.
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

import static java.lang.Math.*;


/**
*  A collection of methods for integrating a 1D function within a range:
*  <code>I=int_a^b{f(x) dx}</code>.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  June 8, 2009
*  @version  January 23, 2014
*/
public class Quadrature {
	
	//	2^(JMAX-1) is the maximum number of steps in simpsonsRule.
	private static final int JMAX = 20;
	
    /**
     * Returns the integral of the supplied function from <code>a</code> to
     * <code>b</code> using Simpson's Rule. Reference: Numerical Recipes in C, pg. 139.
     *
     * @param func The function being integrated (the "derivative" method is never called).
     * @param a The lower integration limit (must be < b).
     * @param b The upper integration limit (must be > a).
     * @param tol The desired relative error.
     * @return The integral of the supplied function from <code>a</code> to <code>b</code>.
     * @throws IntegratorException if to many iterations are attempted.
     * @throws RootException thrown by the function itself.
     */
    public static double simpsonsRule(Evaluatable1D func, double a, double b, double tol) throws IntegratorException, RootException {
        if (tol < MathTools.EPS)    tol = MathTools.EPS;
        
        double ost = 0;
        double os = 0;
        for (int j = 1; j <= JMAX; ++j) {
            double st = trapzd(func, a, b, j, ost);
            double s = (4 * st - ost) / 3.;
            if (j > 5)
                if (abs(s - os) < tol * abs(os) || (s == 0.0 && os == 0.0))
                    return s;
            os = s;
            ost = st;
        }
        throw new IntegratorException("To many steps in simpsonsRule.");
    }

	/**
	*  This routine computes the nth stage of refinement of an extended
	*  trapezoidal rule.  When called with n=1, the routine returns the
	*  crudest estimate of int_a^b{f(x)dx}.  Subsequent calls with
	*  n=2,3,... (in sequential order) will improve the accuracy by adding
	*  2^(n-2) additional interior points.
	*  Reference:  Numerical Recipes in C, pg. 137.
	*
	*  @param func  The function being integrated (the "derivative" function is never called).
	*  @param a The lower integration limit (must be < b).
	*  @param b The upper integration limit (must be > a).
	*  @param n Desired stage of refinement (n=1, 2, 3, ... in sequential order).
	*  @param s The estimate of the integral from the last call to this method (ignored when n==1).
	*  @return The estimate of the function's integral.
	*  @throws RootException thrown by the function itself.
	*/
	private static double trapzd(Evaluatable1D func, double a, double b, int n, double s) throws RootException {
		if (n == 1)	return 0.5*(b-a)*(func.function(a)+func.function(b));
		
		int it = 1;
		for (int j=1; j < n-1; j++) it <<= 1;
		
		double tnm = it;
		double del = (b-a)/tnm;
		double x = a + 0.5*del;
		double sum = 0.;
		for (int j=0; j < it; j++, x += del)
			sum += func.function(x);
		
		s = 0.5*(s+(b-a)*sum/tnm);
		
		return s;
	}
		

	/**
	*  Returns the integral of the supplied function from <code>a</code> to <code>b</code>
	*  using Gauss-Legendre integration (W(x) = 1).
	*  Reference: Numerical Recipes in C, pg. 148.
	*
	*  @param func  The function being integrated (the "derivative" function is never called).
	*  @param a The lower integration limit (must be < b).
	*  @param b The upper integration limit (must be > a).
	*  @param x The abscissas for the quadrature.  Number of points determined by length of x.
	*  @param w The weights for the quadrature.  Must be same length as x.
	*  @return The integral of the supplied function from <code>a</code> to <code>b</code>.
	*  @throws RootException may be thrown by the function itself.
	*/
	private static double gaussLegendre_Wx1(Evaluatable1D func, double a, double b, double[] x, double[] w) throws RootException {
		
		double xm = 0.5*(b+a);
		double xr = 0.5*(b-a);
		double s = 0;
		int size = x.length;
		for (int j=0; j < size; j++) {
			double dx = xr*x[j];
			s += w[j]*(func.function(xm+dx) + func.function(xm-dx));
		}
		return s *= xr;
	}
	
	//	Abscissa used by gaussLegendre_Wx1N10().
	private static final double[] _gaussLx_Wx1N10 =
		{0.14887433898163122, 0.43339539412924716, 0.6794095682990244, 0.8650633666889845, 0.9739065285171717};
	//	Weights used by gaussLegendre_Wx1N10().
	private static final double[] _gaussLw_Wx1N10 =
		{0.2955242247147529, 0.26926671930999174, 0.21908636251598218, 0.14945134915058053, 0.06667134430868371};
	
	/**
	*  Returns the integral of the supplied function from <code>a</code> to <code>b</code>
	*  using Gauss-Legendre integration (W(x) = 1, N=10).
	*  Reference: Numerical Recipes in C, pg. 148.
	*
	*  @param func  The function being integrated (the "derivative" function is never called).
	*  @param a The lower integration limit (must be < b).
	*  @param b The upper integration limit (must be > a).
	*  @return The integral of the supplied function from <code>a</code> to <code>b</code>.
	*  @throws RootException may be thrown by the function itself.
	*/
	public static double gaussLegendre_Wx1N10(Evaluatable1D func, double a, double b) throws RootException {
		return gaussLegendre_Wx1(func, a, b, _gaussLx_Wx1N10, _gaussLw_Wx1N10);
	}
	
	//	Abscissa used by gaussLegendre_Wx1N20().
	private static final double[] _gaussLx_Wx1N20 =
		{0.07652652113349734, 0.2277858511416451, 0.37370608871541955, 0.5108670019508271, 0.636053680726515,
		 0.7463319064601508, 0.8391169718222189, 0.912234428251326, 0.9639719272779138, 0.9931285991850949};
	//	Weights used by gaussLegendre_Wx1N20().
	private static final double[] _gaussLw_Wx1N20 =
		{0.15275338713072598, 0.14917298647260382, 0.1420961093183819, 0.1316886384491766, 0.11819453196151775,
		 0.10193011981724048, 0.08327674157670474, 0.06267204833410904, 0.04060142980038705, 0.017614007139150577};
	
	/**
	*  Returns the integral of the supplied function from <code>a</code> to <code>b</code>
	*  using Gauss-Legendre integration (W(x) = 1, N=20).
	*  Reference: Numerical Recipes in C, pg. 148.
	*
	*  @param func  The function being integrated (the "derivative" function is never called).
	*  @param a The lower integration limit (must be < b).
	*  @param b The upper integration limit (must be > a).
	*  @return The integral of the supplied function from <code>a</code> to <code>b</code>.
	*  @throws RootException may be thrown by the function itself.
	*/
	public static double gaussLegendre_Wx1N20(Evaluatable1D func, double a, double b) throws RootException {
		return gaussLegendre_Wx1(func, a, b, _gaussLx_Wx1N20, _gaussLw_Wx1N20);
	}
	
	//	Abscissa used by gaussLegendre_Wx1N40().
	private static final double[] _gaussLx_Wx1N40 =
		{0.038772417506050816, 0.11608407067525521, 0.1926975807013711, 0.2681521850072, 0.3419940908257585,
		 0.413779204371605, 0.4830758016861787, 0.5494671250951282, 0.6125538896679802, 0.6719566846141796,
		 0.7273182551899271, 0.7783056514265194, 0.8246122308333117, 0.8659595032122595, 0.9020988069688743,
		 0.9328128082786765, 0.9579168192137917, 0.9772599499837743, 0.990726238699457, 0.9982377097105593};
	//	Weights used by gaussLegendre_Wx1N40().
	private static final double[] _gaussLw_Wx1N40 =
		{0.0775059479784248, 0.077039818164248, 0.07611036190062617, 0.07472316905796833, 0.07288658239580408,
		 0.07061164739128681, 0.06791204581523393, 0.06480401345660108, 0.06130624249292889, 0.05743976909939157,
		 0.05322784698393679, 0.04869580763507221, 0.04387090818567314, 0.03878216797447161, 0.033460195282546436,
		 0.02793700698001634, 0.02224584919416689, 0.016421058381907876, 0.010498284531152905, 0.004521277098533099};
	
	/**
	*  Returns the integral of the supplied function from <code>a</code> to <code>b</code>
	*  using Gauss-Legendre integration (W(x) = 1, N=40).
	*  Reference: Numerical Recipes in C, pg. 148.
	*
	*  @param func  The function being integrated (the "derivative" function is never called).
	*  @param a The lower integration limit (must be < b).
	*  @param b The upper integration limit (must be > a).
	*  @return The integral of the supplied function from <code>a</code> to <code>b</code>.
	*  @throws RootException may be thrown by the function itself.
	*/
	public static double gaussLegendre_Wx1N40(Evaluatable1D func, double a, double b) throws RootException {
		return gaussLegendre_Wx1(func, a, b, _gaussLx_Wx1N40, _gaussLw_Wx1N40);
	}
	
    
    //  Constants used by the adaptive Lobatto integration routines.
    private static final double ALPHA = sqrt(2./3.);
    private static final double BETA = 1./sqrt(5.);
    private static final double X1 = 0.942882415695480;
    private static final double X2 = 0.641853342345781;
    private static final double X3 = 0.236383199662150;
    
    /**
     * Returns the integral of the supplied function from <code>a</code> to <code>b</code>
     * using the adaptive Gauss-Lobatto method.<br>
     * Reference:
     *  Gander, W., Gautschi, W., "Adaptive Quadrature - Revisited", ETH Zürich, Aug. 1998.
     *
     * @param func The function being integrated (the "derivative" function is never called).
     * @param a The lower integration limit (must be < b).
     * @param b The upper integration limit (must be > a).
     * @param tol The desired relative error.
     * @return The integral of the supplied function from <code>a</code> to <code>b</code>.
     * @throws IntegratorException if to many iterations are attempted.
     * @throws RootException may be thrown by the function itself.
     */
    public static double adaptLobatto(Evaluatable1D func, double a, double b, double tol) throws IntegratorException, RootException {
        if (tol < MathTools.EPS)    tol = MathTools.EPS;
        
        double m = (a+b)/2;
        double h = (b-a)/2;
        double[] x = {a, m-X1*h, m-ALPHA*h, m-X2*h, m-BETA*h, m-X3*h, m, m+X3*h,
                    m+BETA*h, m+X2*h, m+ALPHA*h, m+X1*h, b};
        double[] y = new double[13];
        for (int i=0; i < 13; ++i)
            y[i] = func.function(x[i]);
        double fa = y[0];
        double fb = y[12];
        double i2 = h/6*(fa + fb + 5*(y[4] + y[8]));
        double i1 = h/1470*(77*(fa + fb) + 432*(y[2] + y[10]) + 625*(y[4] + y[8]) + 672*y[6]);
        double is = h*(0.01582719197343802*(fa+fb) + 0.0942738402188500*(y[1]+y[11])
                + 0.155071987336585*(y[2]+y[10]) + 0.188821573960182*(y[3]+y[9])
                + 0.199773405226859*(y[4]+y[8]) + 0.224926465333340*(y[5]+y[7])
                + 0.242611071901408*y[6]);
        double s = signum(is);
        if (s == 0) s = 1;
        double erri1 = abs(i1-is);
        double erri2 = abs(i2-is);
        double R = erri1/erri2;
        if (R > 0 && R < 1)
            tol = tol/R;
        is = s*abs(is)*tol/MathTools.EPS;
        if (is == 0)    is = b-a;
        
        return adaptlobstp(func,a,b,fa,fb,is);
    }
    
    /**
     * Recursion function used by adaptlob().
     */
    private static double adaptlobstp(Evaluatable1D func, double a, double b, double fa, double fb, double is) throws IntegratorException, RootException {
        double h = (b-a)/2;
        double m = (a+b)/2;
        double mll = m-ALPHA*h;
        double ml = m-BETA*h;
        double mr = m+BETA*h;
        double mrr = m+ALPHA*h;
        double fmll = func.function(mll);
        double fml = func.function(ml);
        double fm = func.function(m);
        double fmr = func.function(mr);
        double fmrr = func.function(mrr);
        double i2 = h/6*(fa+fb+5*(fml+fmr));
        double i1 = h/1470*(77*(fa + fb) + 432*(fmll + fmrr) + 625*(fml + fmr) + 672*fm);
        if (is + (i1-i2) == is || mll <= a || b <= mr) {
            if (m <= a || b <= m ) {
                throw new IntegratorException("Interval contains no more machine precision. Required tolerance not met.");
            }
            return i1;
            
        } else {
            double output = adaptlobstp(func,a,mll,fa,fmll,is);
            output += adaptlobstp(func,mll,ml,fmll,fml,is);
            output += adaptlobstp(func,ml,m,fml,fm,is);
            output += adaptlobstp(func,m,mr,fm,fmr,is);
            output += adaptlobstp(func,mr,mrr,fmr,fmrr,is);
            output += adaptlobstp(func,mrr,b,fmrr,fb,is);
            
            return output;
        }
    }
    
}



