/*
 *   ShockMethods -- Utility methods for calculating the properties across shock waves.
 *   
 *   Copyright (C) 2000-2014 by Joseph A. Huwaldt.
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

import static java.lang.Math.*;

import standaloneutils.mathtools.RootException;
import standaloneutils.mathtools.Roots;


/**
*  A set of utility methods for calculating the properties
*  across shock waves.
*
*  <p>  Written by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  June 21, 2000
*  @version April 1, 2014
*/
public final class ShockMethods {

	/**
	*  Constant indicating that a strong shock solution should
	*  be returned from os_beta().
	*/
	public static final boolean STRONG = true;
	
	/**
	*  Constant indicating that a weak shock solution should
	*  be returned from os_beta();
	*/
	public static final boolean WEAK = false;
	
	/**
	*  The maximum Mach number used in os_Mach() and pm_Mach().
	*/
	private static final double MAXMACH = 100.;
	
	/**
	*  Error Messages.
	*/
	private static final String kMachSubsonicErr = "Mach number must be supersonic.";
	private static final String kThetaToLargeErr = "Incline angle indicates a detatched shock.";


	/**
	*  Prevent anyone from instantiating this class.
	*/
	private ShockMethods() { }

	
	/**
	*  Calculates the Mach number downstream of a normal shock.
	*  Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The Mach number downstream of the normal shock.
	*/
	public static double ns_M2(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double M12 = M1*M1;
		double gm1o2 = (gam - 1)*0.5;
		double TT_T1 = 1 + gm1o2*M12;
		double denominator = gam*M12 - gm1o2;
		double M2 = sqrt(TT_T1/denominator);
		return M2;
	}
	
	/**
	*  Calculates the density ratio across a normal shock.
	*  Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The density ratio (rho2/rho1) across the
	*          normal shock.
	*/
	public static double ns_rho2rho1(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double M12 = M1*M1;
		return ( (gam + 1)*M12/(2 + (gam - 1)*M12) );
	}
	
	/**
	*  Calculates the pressure ratio across a normal shock.
	*  Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The pressure ratio (p2/p1) across the
	*          normal shock.
	*/
	public static double ns_P2P1(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		return ( 1 + 2*gam/(gam + 1)*(M1*M1 - 1) );
	}
	
	/**
	*  Calculates the temperature ratio across a normal shock.
	*  Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The temperature ratio (T2/T1) across the
	*          normal shock.
	*/
	public static double ns_T2T1(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		return ( ns_P2P1(M1, gam) / ns_rho2rho1(M1, gam) );
	}
	
	/**
	*  Calculates the total or stagnation pressure ratio across
	*  a normal shock. Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The total pressure ratio (PT2/PT1) across the
	*          normal shock.
	*/
	public static double ns_PT2PT1(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double M2 = ns_M2(M1, gam);
		return ns_PT2PT1(M1, M2, gam);
	}
	
	/**
	*  Calculates the total or stagnation pressure ratio across
	*  a normal shock. Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  M2  The Mach number downstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The total pressure ratio (PT2/PT1) across the
	*          normal shock.
	*/
	public static double ns_PT2PT1(double M1, double M2, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		if (M2 > M1)	throw new IllegalArgumentException("M2 must be <= M1.");
		double gamM1 = gam - 1;
		double TT_T1 = 1 + 0.5*gamM1*M1*M1;
		double TT_T2 = 1 + 0.5*gamM1*M2*M2;
		double P2P1 = ns_P2P1(M1, gam);
		double PT2PT1 = P2P1*pow(TT_T2/TT_T1, gam/gamM1);
		return PT2PT1;
	}
	
	/**
	*  Calculates the ratio of the total or stagnation pressure
	*  behind a normal shock divided by the static pressure
	*  before the shock.  This is what a supersonic pitot tube
	*  would measure. Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The ratio of total pressure downstream of a normal
	*          shock to the static pressure upstream (PT2/P1).
	*/
	public static double ns_PT2P1(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double M12 = M1*M1;
		double gamP1 = gam + 1;
		double gamm1 = gam - 1;
		double term1 = (-gamm1 + 2*gam*M12)/gamP1;
		double term2 = gamP1*gamP1*M12/(4*gam*M12 - 2*gamm1);
		double PT2P1 = term1*pow(term2, gam/gamm1);
		return PT2P1;
	}
	
	/**
	*  Calculates the maximum pressure coefficient behind a
	*  normal shock.  This is the maximum surface pressure that
	*  can occur at the stagnation point on a configuration in
	*  supersonic flight. Assumes a calorically perfect gas.
	*
	*  @param  M1  The Mach number upstream of a normal shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The maximum pressure (stagnation point pressure)
	*          that can occur on a configuration in supersonic flight.
	*/
	public static double ns_CpMax(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double PT2P1 = ns_PT2P1(M1, gam);
		double CpMax = 2/(gam*M1*M1)*(PT2P1 - 1);
		return CpMax;
	}
	
	/**
	*  Calculates the angle of a Mach wave with respect
	*  to the direction of motion (Mach angle).
	*
	*  @param  M  The Mach number.
	*  @return The Mach angle in radians.
	*/
	public static double machAngle(double M) {
		if (M < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		return ( asin(1./M) );
	}
	
	/**
	*  Calculates the component of upstream Mach number
	*  normal to an oblique shock.  The normal component
	*  of Mach number establishes the strength of the shock.
	*
	*  @param  M1    The Mach number upstream of an oblique shock.
	*  @param  beta  The oblique shock angle in radians.
	*  @return The component of upstream Mach number normal
	*          to the shock.
	*/
	public static double os_Mn1(double M1, double beta) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		return ( M1*sin(beta) );
	}
	
	/**
	*  Calculates the Mach number downstream of an
	*  oblique shock given the normal component of
	*  Mach number downstream of the shock.
	*
	*  @param  Mn2   The normal component of Mach number
	*                downstream of the oblique shock.
	*  @param  beta  The oblique shock angle in radians.
	*  @param  theta The incline angle in radians.
	*  @return The Mach number downstream of an oblique shock.
	*/
	public static double os_M2(double Mn2, double beta, double theta) {
		return ( Mn2/sin(beta-theta) );
	}
	

	/**
	*  Calculate the incline angle (theta) given the oblique shock
	*  angle and Mach number upstream of the oblique shock.
	*
	*  @param  M1   The Mach number upstream of the shock.
	*  @param  beta The oblique shock angle in radians.
	*  @param  gam  The specific heat ratio of the gas. The
	*               value used for air is 1.4.
	*  @return Incline angle (theta) in radians.
	*/
	public static double os_theta(double M1, double beta, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double sinB = sin(beta);
		double Msq = M1*M1;
		double numerator = Msq*sinB*sinB - 1;
		double denominator = Msq*(gam + cos(2*beta)) + 2;
		double theta = atan(2/tan(beta)*numerator/denominator);
		return theta;
	}
	

	/**
	*  Calculate the change in incline angle (theta) with 
	*  a change in upstream Mach number: dtheta(M1)/dM1 for an oblique shock.
	*  This is the slope of the theta-beta-M equation, solved for theta,
	*  with respect to M.
	*
	*  @param  M1   The Mach number upstream of the shock.
	*  @param  beta The oblique shock angle in radians.
	*  @param  gam  The specific heat ratio of the gas. The
	*               value used for air is 1.4.
	*  @return Derivative of the incline angle (theta) with Mach number, dtheta/dM1, in radians.
	*/
	private static double os_dthetadM1(double M1, double beta, double gam) {
		double sinB = sin(beta);
		double sinB2 = sinB*sinB;
		double cos2B = cos(2*beta);
		double Msq = M1*M1;
		double gampc2b = gam + cos2B;
		double AA = Msq*gampc2b+2;
		double BB = Msq*sinB2 - 1;
		double numerator = sinB2 - BB/AA*gampc2b;
		double denominator = AA*tan(beta) + 4./AA*BB*BB;
		double dthetadM = 4*M1*numerator/denominator;
		return dthetadM;
	}
	
	/**
	*  Calculates the critical oblique shock angle which divides
	*  strong shocks from weak shocks.
	*
	*  @param  M1  The Mach number upstream of the oblique shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The critical oblique shock angle in radians.
	*/
	public static double os_betaCrit(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		
		double gamp1 = gam + 1;
		double Msq = M1*M1;
		double term1 = Msq*gamp1 - 4 + sqrt(gamp1*(Msq*Msq*gamp1 +
							8*Msq*(gam - 1) + 16));
		double beta = asin(sqrt(term1/gam)/2/M1);
		
		return beta;
	}
	
	
	/**
	*  Calculates the maximum incline angle for an attached
	*  oblique shock.  Incline angles greater than this result in
	*  detached shocks.
	*
	*  @param  M1  The Mach number upstream of the oblique shock.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The maximum attached shock incline in radians.
	*/
	public static double os_maxTheta(double M1, double gam) {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double betaCrit = os_betaCrit(M1, gam);
		double thetaMax = os_theta(M1, betaCrit, gam);
		return thetaMax;
	}
	
	
	/**
	*  Calculate the oblique shock angle (beta) given the incline
	*  angle and Mach number upstream of the oblique shock.
	*  If calling repeatedly, it is more efficient to call the version
	*  of this function where a beta evaluator is supplied on
	*  the parameter list.  This prevents that object from being created
	*  on every function call.
	*
	*  @param  M1    The Mach number upstream of the shock.
	*  @param  theta The incline angle in radians.
	*  @param  gam   The specific heat ratio of the gas. The
	*                value used for air is 1.4.
	*  @param  tol   Tolerance to use when calculating shock
	*                angle.
	*  @param  type  Indicates if you want the Strong (true) or
	*                weak (false) solution to be returned.
	*  @return Oblique shock angle (beta) in radians.
	*  @throws RootException if there is a problem finding beta.
	*/
	public static double os_beta(double M1, double theta, double gam,
									double tol, boolean type) throws RootException {
		return os_beta(M1,theta,gam,tol,type, new ShockBetaEvaluator());
	}
	
	/**
	*  Calculate the oblique shock angle (beta) given the incline
	*  angle and Mach number upstream of the oblique shock.
	*
	*  @param  M1      The Mach number upstream of the oblique shock.
	*  @param  theta   The incline angle in radians.
	*  @param  gam     The specific heat ratio of the gas. The
	*                  value used for air is 1.4.
	*  @param  tol     Tolerance to use when calculating shock
	*                  angle.
	*  @param  type    Indicates if you want the strong (true) or
	*                  weak (false) solution to be returned.  Weak is the more common solution.
	*  @param  betafn  An instance of ShockBetaEvaluator.
	*  @return Oblique shock angle (beta) in radians.
	*  @throws RootException if there is a problem finding beta.
	*/
	public static double os_beta(double M1, double theta, double gam,
									double tol, boolean type, ShockBetaEvaluator betafn) throws RootException {
		if (M1 < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
	
		// First, deal with special cases.
		
		// Is theta == 0?
		if (abs(theta) <= tol) {
			if (type == WEAK)
				// We have a Mach wave (beta == mu).
				return machAngle(M1);
				
			else
				// We have a normal shock.
				return PI/2;
		}
		
		// Is theta > theta max?
		double betaCrit = os_betaCrit(M1, gam);
		double thetaMax = os_theta(M1, betaCrit, gam);
		if (theta > thetaMax)
			throw new IllegalArgumentException(kThetaToLargeErr);
		
		// Does theta == theta max?
		else if (theta >= thetaMax - tol)
			return betaCrit;
		
		
		// Initialize the evaluatable function for our root solver.
		betafn.M = M1;
		betafn.gamma = gam;
		betafn.setInitialValue(theta);
		
		// Use a root solver to find beta.
		double beta;
		if (type == WEAK)
			// Find weak solution.
			beta = Roots.findRoot1D(betafn, machAngle(M1), betaCrit, tol);
		
		else
			// Find strong solution.
			beta = Roots.findRoot1D(betafn, betaCrit, PI/2, tol);
		
		return beta;
	}
	
	
	/**
	*  Calculate the Mach number upstream of an oblique shock
	*  given the incline and oblique shock angles.
	*  If calling repeatedly, it is more efficient to call the version
	*  of this function where a Mach evaluator is supplied on
	*  the parameter list.  This prevents that object from being created
	*  on every function call.
	*
	*  @param  theta The incline angle in radians.
	*  @param  beta  The oblique shock angle in radians.
	*  @param  gam   The specific heat ratio of the gas. The
	*                value used for air is 1.4.
	*  @param  tol   Tolerance to use when calculating Mach
	*                number.
	*  @return The Mach number upstream of the oblique shock.
	*  @throws RootException if there is a problem finding Mach
	*          number.
	*/
	public static double os_Mach(double theta, double beta, double gam, double tol) 
									throws RootException {
		return os_Mach(theta,beta,gam,tol, new ShockMachEvaluator());
	}
	
	
	/**
	*  Calculate the Mach number upstream of an oblique shock
	*  given the incline and oblique shock angles.
	*
	*  @param  theta   The incline angle in radians.
	*  @param  beta    The oblique shock angle in radians.
	*  @param  gam     The specific heat ratio of the gas. The
	*                  value used for air is 1.4.
	*  @param  tol     Tolerance to use when calculating Mach
	*                  number.
	*  @param  machfn  An instance of ShockMachEvaluator.
	*  @return The Mach number upstream of the oblique shock.
	*  @throws RootException if there is a problem finding Mach
	*          number.
	*/
	public static double os_Mach(double theta, double beta, double gam, double tol,
									ShockMachEvaluator machfn) throws RootException {
	
		// First, deal with special cases.
		
		// Is theta == 0?
		double Mmu = 1./sin(beta);
		double dthetadM = os_dthetadM1(Mmu, beta, gam);	//	Estimate the local theta slope with Mach number near Mmu.
		double tol_t = tol*dthetadM;					//	Come up with a tolerance for theta that is consistant with Mmu.
		if (abs(theta) <= tol_t) {
			// We have a Mach wave (beta == mu).
			return Mmu;
		}
		
		// Is theta > theta max?
		double thetaMax = os_theta(MAXMACH, beta, gam);
		if (theta > thetaMax)
			throw new IllegalArgumentException(kThetaToLargeErr);
		

		// Initialize an evaluatable function for our root solver.
		machfn.beta = beta;
		machfn.gamma = gam;
		machfn.setInitialValue(theta);
		
		// Use a root solver to find Mach number.
		double mach = Roots.findRoot1D(machfn, Mmu, MAXMACH, tol);
		
		return mach;
	}


	/**
	*  Calculates the Prandtl-Meyer function (nu) for a
	*  calorically perfect gas.
	*
	*  @param  M   The Mach number upstream of an expansion fan.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @return The Prandtl-Meyer function value in radians
	*          for the given M.
	*/
	public static double pm_func(double M, double gam) {
		if (M < 1.0)	throw new IllegalArgumentException(kMachSubsonicErr);
		double gamR = (gam+1)/(gam-1);
		double M2m1 = M*M - 1;
		double term1 = atan(sqrt(M2m1/gamR));
		double term2 = atan(sqrt(M2m1));
		return sqrt(gamR)*term1 - term2;
	}
	
	/**
	*  Back solves the Prandtl-Meyer function (nu) for Mach
	*  number.  Assumes a calorically perfect gas.
	*  If calling repeatedly, it is more efficient to call the version
	*  of this function where a Mach-Nu evaluator is supplied on
	*  the parameter list.  This prevents that object from being created
	*  on every function call.
	*
	*  @param  nu  Value of the Prandtl-Meyer function in radians.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @param  tol Tolerance to use when calculating the
	*              Mach number.
	*  @return The Mach number corresponding to the specified
	*          PM function value.
	*/
	public static double pm_Mach(double nu, double gam, double tol)
									throws RootException {
		return pm_Mach(nu,gam,tol, new ShockMachNuEvaluator());
	}
	
	/**
	*  Back solves the Prandtl-Meyer function (nu) for Mach
	*  number.  Assumes a calorically perfect gas.
	*
	*  @param  nu  Value of the Prandtl-Meyer function in radians.
	*  @param  gam The specific heat ratio of the gas. The
	*              value used for air is 1.4.
	*  @param  tol Tolerance to use when calculating the
	*              Mach number.
	*  @param  machNufn  An instance of a ShockMachNuEvalautor object.
	*  @return The Mach number corresponding to the specified
	*          PM function value.
	*/
	public static double pm_Mach(double nu, double gam, double tol, ShockMachNuEvaluator machNufn)
									throws RootException {
	
		// Initialize an evaluatable function for our root solver.
		machNufn.setInitialValue(nu);
		machNufn.gamma = gam;
		
		// Use a root solver to find beta.
		double mach = Roots.findRoot1D(machNufn, 1., MAXMACH, tol);
		
		return mach;
	}
	
	
	
	/**
	*  A simple method to test the shock methods in this
	*  class.
	*/
	public static void main(String args[]) {
	
		System.out.println("\nTesting ShockMethods class:");
		
		double gamma = 1.4;
		
		System.out.println("    Test Mach number downstream of a normal shock.");
		double M1 = 2.3;
		double M2 = ns_M2(M1, gamma);
		System.out.println("    M1 = " + M1 + ", M2 = " + (float)M2);
		
		System.out.println("    Test incline angle given shock angle and Mach number.");
		double beta = toRadians(30);
		double theta = os_theta(M1, beta, gamma);
		System.out.println("    M1 = " + M1 + ", beta = " + (float)toDegrees(beta) +
							", theta = " + (float)toDegrees(theta));
		System.out.println("    Test the maximum incline angle for this Mach number.");
		System.out.println("    thetaMax = " + (float)toDegrees(os_maxTheta(M1, gamma)));
		
		try {
			System.out.println("    Test shock angle given incline angle and Mach number."); 
			theta = toRadians(20);
			beta = os_beta(M1, theta, gamma, 0.0001, false);
			System.out.println("    M1 = " + M1 + ", beta = " + (float)toDegrees(beta) +
									", theta = " + (float)toDegrees(theta));
			
			System.out.println("    Test Mach number as function of incline & shock angle."); 
			M1 = os_Mach(theta, beta, gamma, 0.0001);
			System.out.println("    M1 = " + (float)M1 + ", beta = " + (float)toDegrees(beta) +
									", theta = " + (float)toDegrees(theta));
			
			System.out.println("    Test PM value as a function of Mach number."); 
			M1 = 1.5;
			double nu = pm_func(M1, gamma);
			System.out.println("    M1 = " + M1 + ", nu = " + (float)toDegrees(nu));
			
			System.out.println("    Test Mach number as function of PM value."); 
			M2 = pm_Mach(nu, gamma, 0.0001);
			System.out.println("    nu = " + (float)toDegrees(nu) +
									", M = " + (float)M2);
			
		} catch (RootException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
	
	
}



