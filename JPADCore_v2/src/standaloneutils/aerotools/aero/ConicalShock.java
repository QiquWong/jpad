/*
 *   ConicalShock -- Class that comptues the properties of a semi-infinite cone in supersonic flow.
 *   
 *   Copyright (C) 2004-2014 by Joseph A. Huwaldt.
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

import standaloneutils.mathtools.AbstractEvaluatable1D;
import standaloneutils.mathtools.Derivatives;
import standaloneutils.mathtools.IntegratorException;
import standaloneutils.mathtools.MathTools;
import standaloneutils.mathtools.RootException;
import standaloneutils.mathtools.Roots;
import standaloneutils.mathtools.RungeKutta45;
import standaloneutils.mathtools.StoppingCondition;


/**
*  A class that computes the properties of a semi-infinite cone in
*  supersonic flow.  Uses the technique of numerically integrating the Taylor-Maccoll
*  equation for supersonic flow past a semi-infinite cone as described in:
*  Anderson, J., "Modern Compressible Flow With Historical Perspective",
*  McGraw-Hill Publishing Co., 1990, pg. 301.
*
*  <p>  Written by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 24, 2004
*  @version April 1, 2014
*/
public class ConicalShock {

	/**
	*  Constant indicating that a strong shock solution should
	*  be returned from shockAngle().
	*/
	public static final boolean STRONG = true;
	
	/**
	*  Constant indicating that a weak shock solution should
	*  be returned from shockAngle();
	*/
	public static final boolean WEAK = false;
	
	/**
	*  Error Messages.
	*/
	private static final String kMachSubsonicErr = "Mach number must be supersonic.";
	private static final String kThetaToLargeErr = "Cone half-angle indicates detatched shock.";

	//  The step size (in radians) used in the ODE integrator.
	private static final double STEPSIZE = 1e-6;
	
	//  Conversion from degrees to radians.
	private static final double D2R = PI/180.;
	
	//  PI/2.
	private static final double HALF_PI = PI*0.5;
	
	//  One degree in radians.
	private static final double RAD1DEG = PI/180.;
	
	//  Tolerance on solvers.
	private static final double TOL = 1e-9;

	/**
	*  The maximum Mach number used in freeStreamMach().
	*/
	private static final double MAXMACH = 100.;
	
	//  Interface for communicating with ODE Integrator.
	private final IntegratorInterface rkInt = new IntegratorInterface();
	
	//  The ODE integrator we are using.
	private final RungeKutta45 rk = new RungeKutta45();
	
	//  Used by shockAngle() root solver.
	private final ThetaSEvaluator thetaSfn = new ThetaSEvaluator(this);
	
	//  Used by freeStreamMach() root solver.
	private final MachEvaluator machfn = new MachEvaluator(this);
	
	//  Specific heat ratio for the gas we are analyzing.
	private double gam = 1.4;
	
	//  Store input state to determine if re-calculations are needed.
	private double oldM1, oldThetaS;
	
	//  Cone angle corresponding to oldM1 and oldThetaS.
	private double oldThetaC;
	
	//  The Mach number on the surface of the cone corresponding to oldM1 & oldThetaS.
	private double Mc = 1;
	
	//  The ratio of the total pressure downstream of the oblique shock to the total pressure
	//  upstream of the oblique shock.
	private double PTc_PT0 = 1;

	//  Storage for initial conditions for the integrator.
	private double[] y0 = new double[2];
	
	//  Storage for intermediate results.
	private double[] xout;			//  xout[0..nSteps-1]
	private double[][] yout;		//  yout[0..nSteps-1][0..nVars-1]
	
	
	/**
	*  Construct a conical shock solver that assumes the gas properties
	*  for air (gamma = 1.4).
	*/
	public ConicalShock() { }
	
	/**
	*  Construct a conical shock solver object using the specified
	*  specific heat ratio for the gas that the shock is in
	*  (use 1.4 for air).
	*/
	public ConicalShock(double gamma) {
		gam = gamma;
	}
	
	
	/**
	*  Method that returns the specific heat ratio for the gas used in
	*  the calculation of the conical shock properties.
	*/
	public double getGamma() {
		return gam;
	}
	
	/**
	*  Method used to set the specific heat ratio for the gas used in the
	*  calculation of the conical shock properties.  Use 1.4 for air.
	*/
	public void setGamma(double gamma) {
		if (Math.abs(gam - gamma) > MathTools.EPS) {
			//	Value changed, so clear stored values to force re-calculation.
			oldM1 = Double.NaN;
		}
		
		gam = gamma;
	}
	
	/**
	*  Method that computes the ratio of total and static temperature
	*  at the surface of the cone given the free-stream Mach number and
	*  the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of total and static temperature TT/T at the surface of the cone.
	*/
	public double coneTT_T(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		return Isentropic.TT_T(Mc, gam);
	}
	
	/**
	*  Method that computes the ratio of static temperature at the surface of the
	*  cone to the static temperature upstream of the oblique shock forward of the cone
	*  given the free-stream Mach number and the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of static temperature at the surface of the cone to
	*          the free-stream static temperature (Tc/T0).
	*/
	public double coneT_T0(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		double TT0_T0 = Isentropic.TT_T(M1, gam);	//	Total temperature ratio upstream of shock.
		double TTc_Tc = Isentropic.TT_T(Mc, gam);	//	Total temperature ratio at the cone surface.
		
		return (TT0_T0/TTc_Tc);			//	Total temperature (TT) is constant across shock and cancels.
	}
	
	/**
	*  Method that computes the ratio of total and static pressure
	*  at the surface of the cone given the free-stream Mach number and
	*  the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of total and static pressure PT/P at the surface of the cone.
	*/
	public double conePT_P(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		return Isentropic.PT_P(Mc, gam);
	}
	
	/**
	*  Method that computes the ratio of static pressure at the surface of the
	*  cone to the static pressure upstream of the oblique shock forward of the cone
	*  given the free-stream Mach number and the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of static pressure at the surface of the cone to
	*          the free-stream static pressure (Pc/P0).
	*/
	public double coneP_P0(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else including PTc_PT0).
		coneAngle(M1, thetaS);
		
		double PT0_P0 = Isentropic.PT_P(M1, gam);	//	Total pressure ratio upstream of shock.
		double PTc_Pc = Isentropic.PT_P(Mc, gam);	//	Total pressure ratio at cone surface.
		
		return (PT0_P0*PTc_PT0/PTc_Pc);	//	Total pressure (PT) is NOT constant across shock, but calculated by coneAngle().
	}
	
	/**
	*  Method that computes the ratio of total and static density
	*  at the surface of the cone given the free-stream Mach number and
	*  the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of total and static density rhoT/rhoc at the surface of the cone.
	*/
	public double coneRhoT_Rho(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		return Isentropic.rhoT_rho(Mc, gam);	//	rhoT is constant across shock, so this works.
	}
	
	/**
	*  Method that computes the ratio of static density at the surface of the
	*  cone to the static density upstream of the oblique shock forward of the cone
	*  given the free-stream Mach number and the conical shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The ratio of static density at the surface of the cone to
	*          the free-stream static density (rhoc/rho0).
	*/
	public double coneRho_Rho0(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		double rhoT0_rho0 = Isentropic.rhoT_rho(M1, gam);	//	Total density ratio upstream of shock.
		double rhoTc_rhoc = Isentropic.rhoT_rho(Mc, gam);	//	Total density ratio at cone surface.
		
		return (rhoT0_rho0/rhoTc_rhoc);		//	Total density (rhoT) is constant across shock and cancels.
	}
	
	/**
	*  Method that computes the Mach number at the surface of a cone
	*  in supersonic flow given the Mach number upstream of the oblique
	*  shock and the shock angle.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The Mach number on the surface of the cone.
	*/
	public double coneMach(double M1, double thetaS) {
	
		//  Compute the cone angle (and everything else).
		coneAngle(M1, thetaS);
		
		return Mc;
	}
	
	/**
	*  Method that computes the cone half-angle (thetaC) given the
	*  shock angle and the Mach number upstream of the oblique shock.
	*
	*  @param  M1         The Mach number upstream of the oblique shock.
	*  @param  thetaS     The shock angle in radians (0 < thetaS <= PI/2).
	*  @return The cone half-angle (thetaC) in radians (the conical incline angle).
	*/
	public double coneAngle(double M1, double thetaS) {
		
		//  If the same values have been passed in twice in a row, don't re-calculate.
		if (abs(M1 - oldM1) < TOL && abs(thetaS - oldThetaS) < TOL)
			return oldThetaC;
		
		//  Check for illegal inputs.
		if (M1 < 1.0)
			throw new IllegalArgumentException(kMachSubsonicErr);
		if (thetaS-TOL > HALF_PI)
			throw new IllegalArgumentException("Shock angle can not be greater than 90 degrees");
			
		double mu = ShockMethods.machAngle(M1);
		if (thetaS+TOL < mu)
			throw new IllegalArgumentException("Shock angle can not be less than Mach wave angle (" +
						(float)toDegrees(mu) + " deg for M1 = " + (float)M1 + ").");
			
		//  Check for special cases.
		//  Normal Shock = Pi/2
		if (abs(thetaS - HALF_PI) < TOL) {
			oldM1 = M1;
			oldThetaS = thetaS;
			oldThetaC = 0;
			Mc = ShockMethods.ns_M2(M1, gam);
			PTc_PT0 = ShockMethods.ns_PT2PT1(M1, Mc, gam);
			return 0;
		}
		//  Mach wave
		if (abs(thetaS - mu) < TOL) {
			oldM1 = M1;
			oldThetaS = thetaS;
			oldThetaC = 0;
			Mc = M1;
			PTc_PT0 = 1;
			return 0;
		}
		
		//  Find conical "turning" angle from oblique shock relations.
		double delta = ShockMethods.os_theta(M1, thetaS, gam);
		
		//  Find the Mach number downstream of the oblique shock.
		double Mn1 = ShockMethods.os_Mn1(M1,thetaS);            //  Component of M1 normal to shock.
		double Mn2 = ShockMethods.ns_M2(Mn1, gam);              //  Cross normal-shock, get component of M2 normal to shock.
		double M2 = ShockMethods.os_M2(Mn2, thetaS, delta);     //  Total Mach number after shock.
		PTc_PT0 = ShockMethods.ns_PT2PT1(Mn1,Mn2,gam);          //  Total pressure ratio across normal shock.
		
		//  Obtain V2', Vr2', Vtheta2'
		double V2p = 1.0/sqrt(1 + 2./(M2*M2)/(gam - 1));
		double Vr2p = V2p*cos(thetaS - delta);
		double Vt2p = -V2p*sin(thetaS - delta);
		
		//  Set initial (start) values of variables.
		y0[0] = Vr2p;	y0[1] = Vt2p;
		
		//	Allocate storage for intermediate results.
		if (xout == null) {
			xout = new double[50];
			yout = new double[50][2];
		}
		
		//  Integrate the Taylor-Maccoll equations from thetaS to thetaC.
		int nstep = 0;
		try {
			nstep = rk.integrate(thetaS, y0, rkInt, TOL/10, -STEPSIZE, STEPSIZE/1000, rkInt, xout, yout, STEPSIZE);
		} catch (IntegratorException e) {
			e.printStackTrace();
		}
		//System.out.println("nstep = " + nstep + ", thetaS = " + (float)toDegrees(thetaS));
		
		//  Find where zero was crossed.
		int j=0;
		while ((j < nstep-1) && (yout[j][1] < 0.0))	++j;

		//  Interpolate linearly back to Vtp=zero to get output values.
		double x1 = xout[j-1];
		double x2 = xout[j];
		double y1 = yout[j-1][1];
		double y2 = yout[j][1];
		double thetaC = (x2 - x1)/(y2 - y1)*(-y1)+x1;

		//  Also compute the velocity at the cone from the Vrp component (interpolated to where Vtp=0).
		x1 = yout[j-1][0];
		x2 = yout[j][0];
		double Vp = (x2 - x1)/(y2 - y1)*(-y1)+x1;
		
		//  Compute the Mach number at the cone.
		double Vp2 = Vp*Vp;
		this.Mc = sqrt(2*Vp2/(gam-1)/(1-Vp2));
		
		//  Store the current values for next time.
		oldM1 = M1;
		oldThetaS = thetaS;
		oldThetaC = thetaC;
		
		return thetaC;
	}
	
	/**
	*  Method that computes the oblique shock wave angle given the
	*  cone half-angle and the Mach number upstream of the oblique shock.
	*  The cone half-angle must be less than that angle where the shock
	*  becomes separated and a bow shock is formed (can this be determined
	*  analytically?).
	*
	*  @param  M1      The Mach number upstream of the oblique shock.
	*  @param  thetaC  The cone half-angle (conical incline angle) in radians
	*                  (0 <= thetaC < thetaMax).
	*  @param  type    Indicates if you want the Strong (true) or
	*                  weak (false) solution to be returned.
	*  @return The oblique shock angle (thetaS) in radians.
	*  @throws RootException if there is a problem finding the shock angle.
	*/
	public double shockAngle(double M1, double thetaC, boolean type) throws RootException {
	
		if (M1 < 1.0)
			throw new IllegalArgumentException(kMachSubsonicErr);
		
		// Is theta == 0?
		if (abs(thetaC) <= TOL/10) {
			if (type == WEAK) {
				// We have a Mach wave (beta == mu).
				oldM1 = M1;
				Mc = M1;
				PTc_PT0 = 1;
				oldThetaC = thetaC;
				oldThetaS = ShockMethods.machAngle(M1);
				return oldThetaS;
			} else {
				// We have a normal shock.
				oldM1 = M1;
				oldThetaC = thetaC;
				oldThetaS = HALF_PI;
				Mc = ShockMethods.ns_M2(M1, gam);
				PTc_PT0 = ShockMethods.ns_PT2PT1(M1, Mc, gam);
				return HALF_PI;
			}
		}
		
		// Initialize the evaluatable function for our root solver.
		thetaSfn.M = M1;
		thetaSfn.setInitialValue(thetaC);
		
		// Use a root solver to find thetaS.
		double thetaSCrit = thetaSCrit(M1);
		double thetaS;
		if (type == WEAK)
			// Find weak solution.
			thetaS = Roots.findRoot1D(thetaSfn, ShockMethods.machAngle(M1), thetaSCrit, TOL);
		
		else
			// Find strong solution.
			thetaS = Roots.findRoot1D(thetaSfn, thetaSCrit, HALF_PI, TOL);
		
		return thetaS;
	}
	
	/**
	*  Estimates the critical shock angle which divides
	*  strong shocks from weak shocks.  This is a two piece polynomial
	*  curve fit to a set of critical angles found by running coneAngle()
	*  over a range of Mach numbers and thetaS's.
	*
	*  @param  M1  The Mach number upstream of the shock.
	*  @return The approximate critical shock angle in radians.
	*/
	private static double thetaSCrit(double M1) {
		if (M1 <= 1.5)
			return (47.984*M1*M1 - 145.41*M1 + 178.86)*D2R;
		
		return (-0.4583*M1*M1 + 4.1509*M1 + 63.222)*D2R;
	}
	
	/**
	*  Calculate the Mach number upstream of a conical shock
	*  given the cone half-angle and the shock angle.
	*
	*  @param  thetaC The cone half-angle in radians.
	*  @param  thetaS The conical shock angle in radians.
	*
	*  @return The Mach number upstream of the conical oblique shock (M0).
	*  @throws RootException if there is a problem finding Mach number.
	*/
	public double freeStreamMach(double thetaC, double thetaS) throws RootException {
	
		// First, deal with special cases.
		
		// Is thetaC == 0?
		if (abs(thetaC) <= TOL/10) {
			// We have a Mach wave (beta == mu).
			oldM1 = 1.0/sin(thetaS);
			Mc = oldM1;
			oldThetaC = thetaC;
			oldThetaS = thetaS;
			return oldM1;
		}
		
		// Is theta > theta max?
		double thetaMax = coneAngle(MAXMACH, thetaS);
		if (thetaC > thetaMax)
			throw new IllegalArgumentException(kThetaToLargeErr);

		// Initialize the evaluatable function for our root solver.
		machfn.thetaS = thetaS;
		machfn.setInitialValue(thetaC);
		
		// Use a root solver to find Mach number.
		double mach = Roots.findRoot1D(machfn, 1.0/sin(thetaS), MAXMACH, TOL);
		
		return mach;
	}

	
	/**
	*  Class that serves as the interface for communicating with the ODE Integrator.
	*/
	private class IntegratorInterface implements Derivatives, StoppingCondition {
	
		/************** Required by Derivatives interface *******************
		/**
		*  Calculates the derivatives of the Taylor-Maccoll equation
		*  for supersonic flow around a semi-infinite cone.  Reference:
		*  Anderson, J., "Modern Compressible Flow With Historical Perspective",
		*  McGraw-Hill Publishing Co., 1990, pg. 301.
		*
		*  Compute the derivatives, "ydot", at independent variable "theta" (x), with variables
		*  "y[]", of a set of coupled equations modeled by this class.
		*
		* @params ydot Existing array to be filled with derivatives.
		* @params x    double containing the independent variable (theta).
		* @params y    Array containing the state of the variables (Vr', Vt').
		* @return      Returns reference to the input array "ydot" containing the derivatives.
		*/
        @Override
		public double[] derivs(double[] ydot, double x, double[] y) {
		
			double Vrp = y[0];			/*	y(0) = Vr'	*/
			double Vtp = y[1];			/*	y(1) = Vt'	*/
			
			ydot[0] = Vtp;				/*	ydot(0) = ydot1 = Vtp	*/
			
			/*	ydot(1) = ydot2 = ∆í(x, Vrp, Vtp, gam)		*/
			double gamm1o2 = (gam-1)*0.5;
			double Vtp2 = Vtp*Vtp;
			double term = 1 - Vrp*Vrp - Vtp2;
			double g1 = Vrp*Vtp2 - gamm1o2*(2*Vrp + Vtp/tan(x))*term;
			double g2 = gamm1o2*term - Vtp2;
			ydot[1] = g1/g2;

			return ydot;
		}

		/************** Required by StoppingCondition interface *******************
		/**
		*  This method tells the ODE integrator to stop integrating if theta becomes negative
		*  (theta must be > 0), or the output becomes positive (the output = 0 at the surface
		*  of the cone).
		*
		*  @param  x    The current independent variable value.
		*  @param  y    The array of outputs of the ODE at x.
		*  @param  step The current step count (step >= 1).
		*/
        @Override
		public boolean stop(double x, double[] y, int step) {
			double theta = y[1];
			if (theta >= 0 || x <= 0)
				return true;
			return false;
		}
	}

    /**
    *  Used by shockAngle() and a root solver to iteratively
    *  determine thetaS.  Contains a function that returns the
    *  difference between the thetaC corresponding to the
    *  current guess at thetaS and the thetaC requested.  When
    *  thetaS is found, this difference will be zero.
    */
    private static class ThetaSEvaluator extends AbstractEvaluatable1D {
    	public double M;
		private ConicalShock cs;
		
		public ThetaSEvaluator(ConicalShock cs) {
			this.cs = cs;
		}
		
        @Override
    	public double function (double thetaS) {
			double thetaC = cs.coneAngle(M, thetaS);
    		double value = thetaC - getInitialValue();
    		return value;
    	}
	
    }

    /**
    *  Used by freeStreamMach() and a root solver to iteratively
    *  determine Mach number for a conical shock when given the
    *  cone half-angle and shock angles.  Contains a function that returns the
    *  difference between the thetaC corresponding to the
    *  current guess at Mach number and the thetaC requested.  When
    *  Mach number is found, this difference will be zero.
    */
    private static class MachEvaluator extends AbstractEvaluatable1D {
    	public double thetaS;
		private ConicalShock cs;
		
		public MachEvaluator(ConicalShock cs) {
			this.cs = cs;
		}
		
        @Override
    	public double function (double mach) {
			double thetaC = cs.coneAngle(mach, thetaS);
    		double value = thetaC - getInitialValue();
			//System.out.println("thetaS = " + (float)toDegrees(thetaS) + ", mach = " + mach + ", thetaC = " + (float)toDegrees(thetaC));
    		return value;
    	}
	
    }
	

	/**
	*  A simple method to test the methods in this class.
	*/
	public static void main(String args[]) {
	
		System.out.println("\nTesting ConicalShock class:");
		System.out.println("Create a table of cone angles vs. shock angles for a range of Mach numbers.");
		
		double gamma = 1.4;
		//  Create a ConicalShock solver.
		ConicalShock cs = new ConicalShock(gamma);
		
		//  Run a table of angles for a range of Mach numbers.
		double[] MTable = {1.05, 1.5, 2.0, 5.0};
		
		//  Loop over the Mach numbers.
		for (int i=0; i < MTable.length; ++i) {
			//  Get the Mach numbe we are running.
			double M1 = MTable[i];
		
			//  Use Mach wave as initial value of the shock angle (it can't be less than this).
			double thetaS = ShockMethods.machAngle(M1);
			
			//  Output data.
			System.out.println("\n\tM1\t\tmu (deg)");
			System.out.println("\t" + M1 + "\t\t" + (float)toDegrees(thetaS));
			System.out.println("\tthetaS\t\tthetaC\t\tMc");
			
			//  Begin looping through conical shock angles.
			double thetaC;
			while (thetaS < HALF_PI) {
				//  Compute the required conical angle.
				thetaC = cs.coneAngle(M1, thetaS);
				
				//  Output data.
				System.out.println("\t" + (float)toDegrees(thetaS) + "\t" + (float)toDegrees(thetaC) +
										"\t" + (float)(cs.coneMach(M1, thetaS)));
				
				//	thetaS is incremented by 1 degree in radians.
				thetaS += RAD1DEG;
			}
		}
		
		try {
			System.out.println("\n    Test shock angle given cone half-angle and Mach number."); 
			double M1 = 2.0;
			double thetaC = toRadians(20);
			double thetaS = cs.shockAngle(M1, thetaC, false);
			System.out.println("    M1 = " + (float)M1 + ", thetaC = " +
									(float)toDegrees(thetaC) +
									", thetaS = " + (float)toDegrees(thetaS));
			
			System.out.println("\n    Test free-stream Mach number given cone half-angle and shock angle."); 
			thetaC = toRadians(20);
			thetaS = toRadians(30);
			M1 = cs.freeStreamMach(thetaC, thetaS);
			System.out.println("    M1 = " + (float)M1 + ", thetaC = " +
									(float)toDegrees(thetaC) +
									", thetaS = " + (float)toDegrees(thetaS));
			
		
		} catch (RootException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
	
	
}



