/*
 *   AeroMethods -- A collection of utility methods related to aerodynamic analysis.
 *   
 *   Copyright (C) 2002-2014 by Joseph A. Huwaldt
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
package standaloneutils.aerotools.aero;

import java.text.NumberFormat;

import standaloneutils.mathtools.AbstractEvaluatable1D;
import standaloneutils.mathtools.Evaluatable1D;
import standaloneutils.mathtools.RootException;
import standaloneutils.mathtools.Roots;

import java.text.DecimalFormat;


/**
*  A set of utility methods for calculating various
*  aerodynamic analysis related properties.
*
*  <p>  Written by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 25, 2000
*  @version April 1, 2014
*/
public final class AeroMethods {

	/**
	*  The natural logarithm of 10.
	*/
	private static final double LOG10 = Math.log(10);

	/**
	*  Maximum number of allowed iterations.
	*/
	private static final int MAXIT = 100;
	

    /**
    *  Prevent anyone from instantiating this class.
    */
    private AeroMethods() { }

    
	/**
	*  <p> Prandtl-Glauert compressibility correction.
	*      A compressibility correction that relates incompressible
	*      flow over a given 2D profile to subsonic
	*      compressible flow over the same profile.  It approximately
	*      corrects incompressible data for compressibility effects.
	*  </p>
	*
	*  <p> Generally, the Karman-Tsien correction is considered
	*      more accurate, but this method is provided for
	*      historical reasons.
	*  </p>
	*
	*  @param  coef0 Any 2D coefficient (pressure, lift, drag, etc)
	*                that is valid at M=0 (incompressible flow)
	*                which you want to scale to a subsonic
	*                compressible Mach number.
	*  @param  M     The subsonic Mach number you want to scale
	*                the coef0 to.
	*  @return The coefficient input corrected to the given
	*          subsonic Mach number.
	*  @throws IllegalArgumentException if the Mach number is not
	*          subsonic.
	*/
	public static double prandtlGlauert(double coef0, double M) {
		if (M >= 1.0)
			throw new IllegalArgumentException("Mach number must be subsonic.");
		
		return coef0/Math.sqrt(1 - M*M);
	}
	
	/**
	*  Karman-Tsien compressibility correction.
	*  A compressibility correction that relates incompressible
	*  flow over a given 2D profile to subsonic
	*  compressible flow over the same profile.  It approximately
	*  corrects incompressible data for compressibility effects.
	*
	*  @param  coef0 Any 2D coefficient (pressure, lift, drag, etc)
	*                that is valid at M=0 (incompressible flow)
	*                which you want to scale to a subsonic
	*                compressible Mach number.
	*  @param  M     The subsonic Mach number you want to scale
	*                the coef0 to.
	*  @return The coefficient input corrected to the given
	*          subsonic Mach number.
	*  @throws IllegalArgumentException if the Mach number is not
	*          subsonic.
	*/
	public static double karmenTsien(double coef0, double M) {
		if (M >= 1.0)
			throw new IllegalArgumentException("Mach number must be subsonic.");
		
		double M2 = M*M;
		double machF = Math.sqrt(1 - M2);
		double denom = machF + coef0/2*M2/(1 + machF);
		
		return coef0/denom;
	}
	
	/**
	*  Estimates the critical Mach number given the minimum
	*  incompressible pressure coefficient.  The critical
	*  Mach number is defined as that free stream Mach number
	*  where local flow speed on a configuration first becomes
	*  sonic (M=1).  If Cp0min is more positive than about
	*  -0.1327, then a maximum Mcr of 0.86 is returned.
	*
	*  @param  Cp0min The measured or calculated value of the
	*                 incompressible pressure coefficient at the
	*                 minimum pressure point.
	*  @param  gam    The specific heat ratio of the gas.  For
	*                 air, the value is 1.4.
	*  @param  tol    Tolerance to use when calculating Mach
	*                 number.
	*  @return The critical Mach number.
	*  @throws RootException if there is a problem in the root
	*          solver with converging on Mcr.
	*/
	public static double Mcr(double Cp0min, double gam, double tol)
												throws RootException {
		
		//	Do a bounds check on Cp.
		if (Cp0min > -0.132743943)
			return 0.86;
			
		//	Create an evaluatable function for our root solver.
		McrEvaluator machfn = new McrEvaluator();
		machfn.setInitialValue(Cp0min);
		machfn.gam = gam;
		
		// Use a root solver to find Mach number.
		double mach = Roots.findRoot1D(machfn, 0.1, 0.86, tol);
		
		return mach;
	}
	
	
    /**
    *  Used by AeroMethods.Mcr() and a root solver to iteratively
    *  determine the critical Mach number for a given incompressible
    *  minimum (most negative) pressure coefficient.
    *
    *  The critical Mach number is that Mach number where the
    *  Karmen-Tsien compressibility correction as a fn(M) and
    *  the critical pressure coefficient function as a fn(M)
    *  cross.
    */
    private static class McrEvaluator extends AbstractEvaluatable1D {
    	double gam = 1.4;
	
        @Override
    	public double function (double mach) {
	
    		//	Determine the Cp at this Mach number given the Cp at M=0.
    		double Cp = AeroMethods.karmenTsien(getInitialValue(), mach);
		
    		//	Determine the critical Cp at this Mach number.
    		double gamM1 = gam - 1;
    		double M2 = mach*mach;
    		double TT_T = 1 + 0.5*(gam - 1)*M2;
    		double term1 = TT_T/(1 + gamM1/2);
    		double term2 = Math.pow(term1, gam/gamM1) - 1;
    		double Cpcr = 2/(gam*M2)*term2;
		
    		//	When the critical Cp equals the compressible Cp, we are done.
    		return Cpcr - Cp;
    	}
    }


	/**
	*  Calculates the viscosity ratio (mu/muref) as a function of the temperature
	*  ratio (T/Tref).  This method uses the Sutherland Law which is an
	*  empirical viscosity-temperature relation which best fits experimental
	*  data.  This method is valid for air at temperatures from -250 degF (116.5 K)
	*  to 3250 degF (2060.9 K).
	*
	*  @param  tratio The temperature ratio (T/Tref) at which the viscosity
	*                 ratio (mu/muref) is to be evaluated.
	*  @return The viscosity ratio (mu/muref) corresponding to the input temperature ratio.
	*/
	public static double viscosityRatio(double tratio) {
	
		//	Ref:  Truitt, R.W., _Fundamentals_of_Aerodynamic_Heating_,
		//			The Ronald Press, 1960, pg.44.
		//	mu/muref = (T/Tref)^0.5*(1 + S/Tref)/(1 + (S/Tref)/(T/Tref))
		//	S/Tref is taken as 0.505.
		
		double denom = 1 + (0.505/tratio);
		double muratio = Math.sqrt(tratio)*1.505/denom;
		
		return muratio;
	}
	
	
	/**
	*  Function that calculates the turbulent skin friction coefficient in a
	*  boundary layer including the effects of compressibility, viscosity, and
	*  heat transfer.  This function uses the "T'" method of Sommer and
	*  Short (NACA-TN-3391, Appendix C, pg. 28).
	*
	*  @param  M1       The local Mach number just outside the boundary layer.
	*  @param  twratio  The local wall-temperature ratio (Tw/T1).  T1 is the
	*                   local temperature just outside of the boundary layer,
	*                   Tw is the temperature at the wall.
	*  @param  R1       The local Reynolds number just outside of the boundary layer.
	*  @return The turbulent skin friction coefficient including the effects of
	*          compressibility and heat transfer.  If a solution could not be found
	*          (which should only happen at very small Reynolds numbers (<1000) where
	*          this method probably doesn't apply anyway) this function will return Double.NaN.
	*/
	public static double turbSkinFriction(double M1, double twratio, double R1) {
	
		double cf = Double.NaN;
		
		try {
			double tpratio = 1.0 + 0.35*M1*M1 + (twratio - 1.0);
			double mupratio = viscosityRatio(tpratio);
			double rpratio = 1.0/(tpratio*mupratio);
			double cfp = karmanSch(rpratio*R1, 0.000001);
			
			cf = cfp/tpratio;
		
		} catch (RootException e) {
			//	Shouldn't happen except at very low Reynolds numbers.
			e.printStackTrace();
		}
		
		return cf;
	}
	
	
	/**
	*  Solves the Karman-Schoenherr equation for local skin friction coefficient in an
	*  incompressible boundary layer with no heat transfer.
	*  The Karman-Schoenherr equation is:  0.242/sqrt(cf) = log10(cf*Rey).
	*  This equation is solved iteratively using root solver.
	*
	*  @param   Rey		The Reynolds number at which the solution is to be found.
	*  @param   tol     The solution will be refined until it's accuracy is
	*                   better than this.
	*  @throws  RootException if a solution could not be found.  This should only happen
	*           at very low Reynolds number where this method probably doesn't apply anyway.
	*/
	private static double karmanSch(double Rey, double tol) throws RootException {
        //  Using Brent's method to find the root.
        
		//	Determine what the iteration limits (brackets) should be using the
		//	Prandtl-Schlicting approximation (curve fit).
		double cpguess = 0.455/Math.pow(log10(Rey), 2.58);
		double x1 = cpguess*0.95;
		double x2 = cpguess*1.05;
	
		double a=x1, b=x2, c=x1;
		double fa = karmanSchFunc(a, Rey);	//	f(a)
		double fb = karmanSchFunc(b, Rey);	//	f(b)
		double fc = fa;						//	f(c)

		// Check for bracketing.
		if ( (fa > 0.0 && fb > 0.0) || (fa < 0.0 & fb < 0.0) )
			throw new RootException("Root not bracketed.");
		
		//	Main iteration loop
		for(int iter=0; iter < MAXIT; ++iter) {
			double prev_step = b - a;	//	Distance from the last but one to the last approx.
			double tol_act;				//	Actual tolerance
    		double p;      				//	Interp step is calculated in the form p/q;
    		double q;					//		division is delayed until the last moment.
			double new_step;      		//	Step at this iteration
   
   			if( Math.abs(fc) < Math.abs(fb) ) {
				a = b;					//	Swap data for b to be the best approximation.
				b = c;
				c = a;
				fa=fb;
				fb=fc;
				fc=fa;
			}
			
			// Convergence check.
    		tol_act = 2.*Double.MIN_VALUE*Math.abs(b) + tol*0.5;
			new_step = 0.5*(c - b);

			if( Math.abs(new_step) <= tol_act || fb == 0.0 )
				return b;				//	An acceptable approximation was found.

			//	Decide if the interpolation can be tried.
			if( Math.abs(prev_step) >= tol_act && Math.abs(fa) > Math.abs(fb) ) {
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

				double min1 = (0.75*cb*q - Math.abs(tol_act*q*0.5));
				double min2 = Math.abs(prev_step*q*0.5);
				if (p < min1 && p < min2) {
					/*	If b+p/q falls in [b,c] and isn't too large it is accepted.
						If p/q is too large then use the bisection procedure which
						will reduce the [b,c] range to a greater extent.	*/
					new_step = p/q;
				}
			}

			if( Math.abs(new_step) < tol_act )	//	Adjust the step to be not less than
				if( new_step > 0.0 )			//		tolerance.
					new_step = tol_act;
				else
					new_step = -tol_act;

			a = b;						//	Save the previous approximation.
			fa = fb;
			b += new_step;
			fb = karmanSchFunc(b, Rey);		//	Do step to a new approximation.
			if( (fb > 0. && fc > 0.) || (fb < 0. && fc < 0.) ) {
				c = a;					//	Adjust c for it to have a sign opp. to that of b.
				fc = fa;
			}
		}
		
		// Max iterations exceeded.
		throw new RootException("Maximun number of iterations exceeded.");
		
	}


	/**
	*  The Karman-Schoenherr equation for an incompressible boundary layer without
	*  heat transfer.  Returns:  0.058564/(log(cf*R))^2 - cf.  The solution to
	*  the equation is found by iterating cf until this function returns a value
	*  near zero.
	*/
	private static double karmanSchFunc(double cf, double R) {
	
		double denom = log10(cf*R);
		denom *= denom;
		
		return 0.058564/denom - cf;
	}


	/**
	*  Find the base 10 logarithm of the given double.
	*
	*  @param   x  Value to find the base 10 logarithm of.
	*  @return  The base 10 logarithm of x.
	*/
	private static double log10( double x ) {
		return Math.log(x)/LOG10;
	}
	

	/**
	*  Format a number for output to a table file.
	*  This method formats the given number using the supplied
	*  NumberFormat object.  It then adds spaces to the start
	*  of the formatted number until the string reaches the
	*  specified length.
	*
	*  @param  size   The overall length of the formatted number including
	*                 the decimal point, minus sign, "E" notation, etc.
	*  @param  nf     The NumberFormat to use when formatting this number.
	*  @param  number The number to be formatted.
	*/
	private static String formatNumber( int size, NumberFormat nf, double number ) {
		StringBuilder buffer = new StringBuilder( nf.format( number ) );
		int length = buffer.length();
		while ( length < size ) {
			buffer.insert( 0, " " );
			length += 1;
		}
		return buffer.toString();
	}

	/**
	*  A simple method to test the methods in this class.
	*/
	public static void main(String args[]) {
	
		System.out.println("\nTesting the methods in AeroMethods class:");
		
		try {
			//	Test compressibility corrections.
			double Mach = 0.7;
			double Cp = -0.83;
			System.out.println("    Mach = " + Mach + ", Cp0 = " + Cp + ":");
			System.out.println("        Prandtl-Glauert = " +
									(float)prandtlGlauert(Cp, Mach) +
									", Karmen-Tsien = " + (float)karmenTsien(Cp, Mach)+".");
		
			//	Test critical Mach number.
			Cp = -2;
			System.out.println("    Cp0min = " + Cp + ":  Mcr = " +
									(float)Mcr(Cp, 1.4, 0.0001) + ".");
			
			//	Test viscosity ratio.
			double muref = 1.205e-5;	//	lb/(ft-sec)
			double tref = 518.688;		//	deg R
			double tratio = 389.984/tref;	//	1976 std atmosphere, h=50k ft.
			double muratio = viscosityRatio(tratio);
			System.out.println("    tratio = " + (float)tratio + ":  muratio = " +
									(float)muratio + ", mu = " + (float)(muratio*muref));
			
			System.out.println("\nTesting turbSkinFriction()...");
			NumberFormat nf = (DecimalFormat)NumberFormat.getInstance();
			nf.setMaximumIntegerDigits(1);
			nf.setMaximumFractionDigits(5);
			nf.setMinimumFractionDigits(5);
			NumberFormat rnf = (DecimalFormat)NumberFormat.getInstance();
			rnf.setMaximumFractionDigits(0);
			rnf.setGroupingUsed(false);
			
			double[] RnTab = { 1E5,2E5,5E5, 1E6,2E6,5E6, 1E7,2E7,5E7, 1E8,2E8,5E8, 1E9 };
			double[] MachTab = { 0,1,2,3,5,10 };
			int numRows = RnTab.length;
			int numCols = MachTab.length;
			System.out.println("Temp = 288.16 K, variation with Reynolds & Mach");
			System.out.println("               0.0     1.0     2.0     3.0     5.0    10.0 Mach");
			for (int j=0; j < numRows; ++j) {
				System.out.print(formatNumber(10, rnf, RnTab[j]) + " ");
				for (int i=0; i < numCols; ++i) {
					double cf = turbSkinFriction(MachTab[i], 1.0, RnTab[j]);
					System.out.print(nf.format(cf) + " ");
				}
				System.out.println();
			}
			System.out.println("\nMach = 1, variation with Reynolds & Temperature");
			tref = 288.16;	//	Kelvin
			System.out.println("                50     100     150     200     250     300 K");
			for (int j=0; j < numRows; ++j) {
				System.out.print(formatNumber(10, rnf, RnTab[j]) + " ");
				for (int i=0; i < numCols; ++i) {
					double cf = turbSkinFriction(1.0, 50*i/tref, RnTab[j]);
					System.out.print(nf.format(cf) + " ");
				}
				System.out.println();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
}

