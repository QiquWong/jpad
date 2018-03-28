/*
*   NACA6Series -- An arbitrary NACA 6 or 6*A series airfoil.
*
*   Copyright (C) 2010-2012 by Joseph A. Huwaldt
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
package standaloneutils.aerotools.aero.airfoils;

import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;

import static java.lang.Math.*;


/**
*  <p> This class represents an arbitrary NACA 6 or 6*A series
*      airfoil section such as a NACA 63-020 airfoil.
*  </p>
*
*  <p> Ported from FORTRAN "NACA6.FOR" to Java by:
*                Joseph A. Huwaldt, June 3, 2010     </p>
*
*  <p> Original FORTRAN "NACA4" code had the following note:  </p>
*
*  <pre>
*         AUTHORS - Charles L.Ladson and Cuyler W. Brooks, NASA Langley
*                   Liam Hardy, NASA Ames
*                   Ralph Carmichael, Public Domain Aeronautical Software
*         Last FORTRAN version:  23Nov96  2.0   RLC
*
*         NOTES - This program has also been known as LADSON and SIXSERIES and
*                 as SIXSERIE on systems with a 8-letter name limit.
*         REFERENCES-  NASA Technical Memorandum TM X-3069 (September, 1974),
*                      by Charles L. Ladson and Cuyler W. Brooks, Jr., NASA Langley Research Center.
*
*                      "Theory of Wing Sections", by Ira Abbott and Albert Von Doenhoff.
*  </pre>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  June 3, 2010
*  @version September 15, 2012
**/
public abstract class NACA6Series implements Airfoil {
	
	// Constants
	private static final int NX = 200;
	private static final int MXCOMB = 10;
	private static final double TINY = 1.0e-10;
	private static final double EPS = 0.1e-10;
	private static final double DX = 0.01;
	
	// Ordinate equation coefficients.

	/**
	*  Thickness-to-Chord Ratio
	**/
	private double toc = 0.20;
	
	/**
	*  Chord length.  Ordinates are scaled to this chord length.
	**/
	private double chord = 1;
	
	private double cli = 0;		//	Design lift coefficient.
	
	private double aa = 1;		//	Mean line chordwise loading (use 0.8 for 6A-series).
	
	
	// Buffers for the output data.
	private transient List<Point2D> upper, lower;
	private transient List<Point2D> camberLine;
	private transient List<Double> yUp, yLp;
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Create a NACA 6 series airfoil with the specified parameters.
	*
	*  @param  CLi        Design lift coefficient (e.g.: 63-206 has CLi = 0.2).
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA6Series(double CLi, double thickness, double length) {
		cli = CLi;
		chord = length;
		toc = thickness;
	}
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Returns a list of points containing the abscissas (X coordinate) and
	*  ordinates (Y coordinate) of the points defining the upper surface of the airfoil.
	**/
    @Override
	public List<Point2D> getUpper() {
		if (upper == null)
			calcOrdinatesSlopes();

		return upper;
	}
	
	/**
	*  Returns a list of points containing the abscissas (X coordinate) and
	*  ordinates (Y coordinate) of the points defining the lower surface of the airfoil.
	**/
    @Override
	public List<Point2D> getLower() {
		if (lower == null)
			calcOrdinatesSlopes();

		return lower;
	}
	
	/**
	*  Returns a list of points containing the camber line of the airfoil.
	**/
    @Override
	public List<Point2D> getCamber() {
		if (camberLine == null)
			calcOrdinatesSlopes();

		return camberLine;
	}
	
	/**
	*  Returns a list containing the slope (dy/dx) of the upper
	*  surface of the airfoil at each ordinate.
	**/
    @Override
	public List<Double> getUpperYp() {
		if (yUp == null)
			calcOrdinatesSlopes();

		return yUp;
	}
	
	/**
	*  Returns a list containing the slope (dy/dx) of the lower
	*  surface of the airfoil at each ordinate.
	**/
    @Override
	public List<Double> getLowerYp() {
		if (yLp == null)
			calcOrdinatesSlopes();

		return yLp;
	}
	
	/**
	*  Returns a string representation of this airfoil
	*  (the NACA designation of this airfoil).
	**/
    @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("NACA ");
		buffer.append(getProfile());
		buffer.append("-");
		buffer.append((int)(cli*10));
		if (toc < 0.1F)
			buffer.append("0");
		buffer.append((int)(toc*100));
		
		if (chord != 1.0F) {
			buffer.append(" c=");
			buffer.append(chord);
		}
		return buffer.toString();
	}
	
	/**
	*  Returns a String that represents the profile type of this airfoil.
	**/
	public abstract String getProfile();
	
	//-----------------------------------------------------------------------------------

	//-----------------------------------------------------------------------------------
	
	/**
	*  A method that is called when a new airfoil is instantiated
	*  that calculates the ordinates and slopes of the airfoil.
	*  This method is general to all NACA 4 digit, 5 digit, 16 series
	*  and mod-4 digit airfoils.
	**/
	private void calcOrdinatesSlopes() {
	
		//	Allocate memory for buffer arrays.
		upper = new ArrayList();
		lower = new ArrayList();
		yUp = new ArrayList();
		yLp = new ArrayList();
		camberLine = new ArrayList();
		
		upper.add(new Point2D.Double(0,0));
		lower.add(new Point2D.Double(0,0));
		camberLine.add(new Point2D.Double(0,0));
		
		boolean if6xa = false;
		double frac = 1;
		double clis = cli;
		int l = 0;
		
//		l = l + 1;
		double y;
		double xc = 0;
		double yc = 0;
		double xuc;
		double yuc;
		double xlc;
		double ylc;
		double xl;
		
		double[] xau = new double[NX];
		double[] yau = new double[NX];
		double[] xal = new double[NX];
		double[] yal = new double[NX];
		double[] phi = new double[NX+1];
		double[] eps = new double[NX+1];
		double[] psi = new double[NX+1];
		double[] xt = new double[NX+1];
		double[] yt = new double[NX+1];
		double[] ytp = new double[NX+1];
		double[] bspline = new double[NX+1];
		double[] cspline = new double[NX+1];
		double[] dspline = new double[NX+1];
		double[] sout = new double[4];
		
		
		double u = 0.005;
		double v= -(aa-u)/abs(aa-u);
		double omxl = (1.-u)*log(1.-u);
		double amxl = (aa-u)*log(abs(aa-u));
		double omxl1 = -log(1.-u) - 1.;
		double amxl1 = -log(abs(aa-u)) + v;
		double omxl2 = 1./(1.-u);
		double amxl2 = -v/abs(aa-u);
		double ali = 0;
		
		double h=0, g, q=0, z, z1=0, z2;
		if ( aa >= EPS && abs(1.-aa) >= EPS ) {
			g = -(aa*aa*(.5*log(aa)-0.25)+0.25)/(1.-aa);
			q = 1;
			double omaa2 = 1.-aa;
			omaa2 *= omaa2;
			h = (0.5*omaa2*log(1.-aa)-0.25*omaa2)/(1.-aa) + g;
			double aamu = aa-u;
			double omu = 1-u;
			z = .5*aamu*amxl - .5*omu*omxl - .25*aamu*aamu + .25*omu*omu;
			z1 = .5*(aamu*amxl1-amxl-omu*omxl1+omxl+aamu-omu);
			z2 = .5*aamu*amxl2 - amxl1 - .5*omu*omxl2 + omxl1;
		}
		
		if ( aa < EPS ) {
			h = -0.5;
			q = 1;
			z1 = u*log(u) - .5*u - .5*(1.-u)*omxl1 + .5*omxl - .5;
			
		} else if (abs(aa - 1) < EPS) {
			h = 0;
			q = 0;
			z1 = -omxl1;
		}
		
		double tanth0 = cli*(z1/(1.-q*aa)-1.-log(u)-h)/PI/(aa+1.)/2.;
		
		//	Slope of profile at origin, Upper and Lower:
		double yp;
		double yup=0, ylp=0;
		if (tanth0 != 0.) {
			yup = -1./tanth0;
			ylp = -1./tanth0;
		} else {
			yup = ylp = 0;
		}
		
		//	First station aft of origin on uncambered profile:
		double x = 0.00025;
		int i = 0;
		
		//	Start loop for X increment:
		while ( x <= 1.0000000001) {
			
			//	Skip thickness computation after first pass:
			if ( i == 0) {
				phep(phi,eps);
				phps(phi,psi);
				
				double rat = 1;
				int it = 0;
				double acrat = 1;
				
				while (true) {
					//	Loop start for thickness iteration:
					++it;
					acrat = acrat*rat;
					double ymax=0, xym=0;
					
					for (int j=0; j < NX+1; ++j) {
						xt[j] = -2.0*cosh(psi[j]*acrat)*cos(phi[j]-eps[j]*acrat);
						yt[j] = 2.0*sinh(psi[j]*acrat)*sin(phi[j]-eps[j]*acrat);
						if ( yt[j] > ymax )	{
							xym = xt[j];
							ymax = yt[j];
						}
					}
					
					//	Estimate first,second and third derivatives by spline fit.
					spline(NX+1, xt, yt, bspline, cspline, dspline);
					for (int j=0; j < NX+1; ++j) {
						seval2(NX+1, xt[j], xt, yt, bspline, cspline, dspline, sout);
						ytp[j] = sout[1];
					}
					
					//	Estimate the location of maximum thickness. Look for the
					//	interval where dt/dx changes sign. XTP,YM are x,y of max thickness.
					double xtp = 1;
					for (int j=2; j < NX; ++j) {
						if ( ytp[j] < 0.0 && ytp[j-1] >= 0.0) {
							xtp = xt[j-1] + ytp[j-1]*(xt[j]-xt[j-1])/(ytp[j-1]-ytp[j]);
						}
					}
					
					double ym = seval(NX+1, xtp, xt, yt, bspline, cspline, dspline);
					double xo = xt[0];
					xl = xt[NX-1];
					double tr = 2.*ym/(xl-xo);
					rat = toc/tr;
					double sf = rat;
					
					if (toc <= EPS || abs(rat-1) <= 0.0001 || it > 10) {
						
						if ( i == 0 ) {
							for (int j = 0; j < NX+1; ++j) {
								xt[j] = (xt[j]-xo)/(xl-xo);
								//	Scale linearly to exact thickness:
								yt[j] = sf*yt[j]/(xl-xo);
								ytp[j] = sf*ytp[j];
							}
						}
						
						//	Now the XT,YT,YTP array are loaded and never change
						xtp = (xtp-xo)/(xl-xo);
						ymax = ymax*sf/(xl-xo);
						ym = ym*sf/(xl-xo);
						xym = (xym-xo)/(xl-xo);
						xl = 0;
						
						if (toc > EPS) {
							//	Fit tilted ellipse at eleventh profile point:
							double cn = 2.*ytp[10] - yt[10]/xt[10] + 0.1;
							double an = xt[10]*(ytp[10]*xt[10]-yt[10])/(xt[10]*(2.*ytp[10]-cn)-yt[10]);
							double ytmcnxt = yt[10] - cn*xt[10];
							double xtman = xt[10] - an;
							double an2 = an*an;
							double bn = sqrt(ytmcnxt*ytmcnxt/(1.-xtman*xtman/an2));
							double bn2 = bn*bn;
							for (int j = 0; j < 10; ++j) {
								xtman = xt[j] - an;
								yt[j] = bn*sqrt(1.-xtman*xtman/an2) + cn*xt[j];
								if ( xt[j] > EPS ) {
									ytmcnxt = yt[j]-cn*xt[j];
									ytp[j] = bn2*(an-xt[j])/an2/ytmcnxt + cn;
								}
							}
							
							//	Update spline.
							spline(NX+1, xt, yt, bspline, cspline, dspline);
						}
						
						x = 0.00025;
						ali = abs(cli);
						xl = 0;
						break;
					}
				}	//	End while(true)
			}	//	end if (i == 0)
			
			//	Interpolate for thickness and derivatives at desired values of X:
			spline(NX+1,xt,yt,bspline,cspline,dspline);
			seval2(NX+1,x,xt,yt,bspline,cspline,dspline,sout);
			y = sout[0];
			yp = sout[1];
			
			//	Compute camberline:
			cli = clis;
			l = 0;
			
			xc = x*chord;
			yc = y*chord;
			double xll = x*log(x);
			q = 1;
			if ( abs(1.-aa) < EPS && abs(1.-x) < EPS) {
				g = h = q = z = 0;
				z1 = z2 = -10.e10;
				
			} else if (aa < EPS && (1-x) < EPS ) {
				g = -0.25;
				h = -.5;
				q = 1;
				z = -.25;
				z1 = 0;
				z2 = -10.e10;
				
			} else if ( abs(aa - x) < EPS) {
//				System.out.println("Here 1");
				double omx = 1.-x;
				z = -.5*omx*omx*log(omx) + 0.25*omx*omx;
				z1 = -.5*omx*(-log(omx)-1.) + .5*omx*log(omx) - .5*(omx);
				z2 = -log(omx) - 0.5;
				g = -(aa*aa*(.5*log(aa)-0.25)+0.25)/(1.-aa);
				double omaa = 1.-aa;
				h = (0.5*omaa*omaa*log(omaa)-0.25*omaa*omaa)/omaa + g;
				
			} else if ( abs(1.-x) < EPS ) {
//				System.out.println("Here 2");
				g = -(aa*aa*(.5*log(aa)-0.25)+0.25)/(1.-aa);
				double omaa = 1.-aa;
				h = (0.5*omaa*omaa*log(omaa)-0.25*omaa*omaa)/omaa + g;
				double aam1 = aa-1.;
				z = .5*aam1*aam1*log(abs(aam1)) - 0.25*aam1*aam1;
				z1 = -aam1*log(abs(aam1));
				z2 = -10.e10;
				
			} else if ( abs(aa-1.) < EPS ) {
//				System.out.println("Here 3");
				g = h = q = 0;
				double omx = 1.-x;
				z = -omx*log(omx);
				z1 = log(omx) + 1.;
				z2 = -1./omx;
				
			} else {
//				System.out.println("Here 4");
				double aamx = aa-x;
				double omx = 1.-x;
				omxl = omx*log(omx);
				amxl = aamx*log(abs(aamx));
				omxl1 = -log(omx) - 1.;
				amxl1 = -log(abs(aamx)) - 1.;
				omxl2 = 1./omx;
				amxl2 = 1./aamx;
				z = .5*aamx*amxl - .5*omx*omxl - .25*aamx*aamx + .25*omx*omx;
				z1 = .5*(aamx*amxl1-amxl-omx*omxl1+omxl+aamx-omx);
				z2 = .5*aamx*amxl2 - amxl1 - .5*omx*omxl2 + omxl1;
				if ( aa <= EPS ) {
					g = -0.25;
					h = -.5;
					
				} else {
					double omaa = 1.-aa;
					g = -(aa*aa*(.5*log(aa)-0.25)+0.25)/omaa;
					h = (0.5*omaa*omaa*log(omaa)-0.25*omaa*omaa)/omaa + g;
				}
			}
			
			double ycmb = cli*(z/(1.-q*aa)-xll+g-h*x)/PI/(aa+1.)/2.;
			
			double xsv = x;
			if ( x < 0.005 )	x = 0.005;
			double tanth = cli*(z1/(1.-q*aa)-1.-log(x)-h)/PI/(aa+1.)/2.0;
			x = xsv;
			if (if6xa)	tanth = -2;
			double ycp2;
			if (x <= 0.005)
				ycp2 = 0;
			else if ( abs(1.-x) <= EPS )
				ycp2 = 1./EPS;
			else {
				double pia = PI*(aa+1)*2;
				ycp2 = cli*(z2/(1.-q*aa)-1./x)/pia;
			}
			
			//	Modified camberline option:
			if6xa = modCamberLine(x, if6xa, ycmb, tanth, ycp2, cli, sout);
			ycmb = sout[0];
			tanth = sout[1];
			ycp2 = sout[2];
			
			double f = sqrt(1.+tanth*tanth);
			double thp = ycp2/(f*f);
			double sinth = tanth/f;
			double costh = 1./f;
			
			//	Camberline and derivatives computed:
			
			//	Combine thickness distributuion and camberline:
			double xu = x - y*sinth;
			double yu = ycmb + y*costh;
			xl = x + y*sinth;
			double yl = ycmb - y*costh;
			modTrailingEdge(x, xu, yu, xl, yl, sout);
			yu = sout[0];
			yl = sout[1];
			
			//	Multiply by chord:
			xuc = xu*chord;
			yuc = yu*chord;
			xlc = xl*chord;
			ylc = yl*chord;
			
			if (ali > EPS) {
				//	Find local slope of cambered profile:
				yup = (tanth*f+yp-tanth*y*thp)/(f-yp*tanth-y*thp);
				ylp = (tanth*f-yp+tanth*y*thp)/(f+yp*tanth+y*thp);
			}
			
			++i;
			xau[i] = xuc;
			yau[i] = yuc;
			xal[i] = xlc;
			yal[i] = ylc;
			
			
			//	Store scaled profile in appropriate arrays
			upper.add(new Point2D.Double(xuc,yuc));
			if (ali >= TINY)
				lower.add(new Point2D.Double(xlc,ylc));
			else
				lower.add(new Point2D.Double(xuc,-yuc));
			camberLine.add(new Point2D.Double(x*chord,ycmb*chord));
			
			
			//	Find X increment:
			if ( x <= 0.012251)	frac = 0.025;
			else if ( x <= 0.09751 )	frac = 0.25;
			
			//	Increment X and return to start of X loop:
			x += frac*DX;
			frac = 1;
		}	//	end while( x <= 1.0 )
		
		//	Calculate derivatives of final coordinates; tabulate and save results.
		++i;
		if ( ali >= EPS ) {
			//	output for cambered airfoil
			spline(i,xau,yau,bspline,cspline,dspline);
			for (int j=0; j < i; ++j) {
				seval2(i,xau[j],xau,yau,bspline,cspline,dspline,sout);
				yUp.add(sout[1]);
			}
			spline(i,xal,yal,bspline,cspline,dspline);
			for (int j=0; j < i; ++j) {
				seval2(i,xal[j],xal,yal,bspline,cspline,dspline,sout);
				yLp.add(sout[1]);
			}
			
		} else {
			//	uncambered
			spline(i,xau,yau,bspline,cspline,dspline);
			for (int j=0; j < i; ++j) {
				seval2(i,xau[j],xau,yau,bspline,cspline,dspline,sout);
				yUp.add(sout[1]);
				yLp.add(-sout[1]);
			}
		}

	}
	
	
	/**
	*  Method used by 6*A series airfoils to modify the camber line.
	*  The default implementation simply returns the inputs as outputs
	*  and returns false.
	*
	*  @param x     The current x/c being calculated.
	*  @param if6xa Flag indicating if 6*A specific calculations have been enabled.
	*  @param ycmb  The y position of the camber line.
	*  @param tanth The slope of the camber line.
	*  @param ycp2  ?
	*  @param cli   The design lift coefficient.
	*  @param outputs  An existing 3-element array filled in with
	*                  outputs: outputs[ycmb, tanth, ycp2].
	*  @return true if this is a 6*A series airfoil, false if it is not.
	**/
	protected boolean modCamberLine(double x, boolean if6xa, double ycmb, double tanth, double ycp2, double cli, double[] outputs) {
		outputs[0] = ycmb;
		outputs[1] = tanth;
		outputs[2] = ycp2;
		return if6xa;
	}
	
	
	/**
	*  Method used by 6*A series airfoils to modify the airfoil trailing edge.
	*  The default implementation simply returns the input yu and yl values.
	*
	*  @param x  The x/c location being calculated.
	*  @param xu The x/c location of the upper surface ordinate.
	*  @param yu The upper surface ordinate.
	*  @param xl The x/c location of hte lower surface ordinate.
	*  @param yl The lower surface ordinate.
	*  @param outputs  An existing 2-element array filled in with
	*                  outputs: outputs[yu, yl].
	**/
	protected void modTrailingEdge(double x, double xu, double yu, double xl, double yl, double[] outputs) {
		outputs[0] = yu;
		outputs[1] = yl;
	}
	
	
	/**
	*  Fill in phi, eps vectors for 63 series airfoil.
	*
	*  @param phi  An existing array with 201 elements to be filled in
	*              by this method.
	*  @param eps  An existing array with 201 elements to be filled in
	*              by this method.
	**/
	protected abstract void phep(double[] phi, double[] eps);
	
	
	/**
	*  Fill in the psi vector for a 63 series airfoil.
	*
	*  @param phi  An array filled in by phep().
	*  @param psi  An existing array with 201 elements to be filled in
	*              by this method.
	**/
	protected abstract void phps(double[] phi, double[] psi);
	
	
	/**
	*  Compute the coefficients of a cubic spline
	*  NOTES - From "Computer Methods for Mathematical Computations", by
	*       Forsythe, Malcolm, and Moser. Prentice-Hall, 1977.
	*
	*  @param n  # of data points (n>1)
	*  @param x  abscissas of knots: x[0..n-1]
	*  @param y  ordinates of knots: x[0..n-1]
	*  @param b  Existing array filled with linear coeff.: b[0..n-1]
	*  @param c  Existing array filled with quadratic coeff.: c[0..n-1]
	*  @param d  Existing array filled with cubic coeff.: d[0..n-1]
	**/
	protected final void spline(int n, double[] x, double[] y, double[] b, double[] c, double[] d) {
		
		if (n < 3) {
			//	special treatment for n < 3
			b[0] = 0;
			if (n == 2)	b[0] = (y[1] - y[0])/(x[1] - x[0]);
			c[0] = 0;
			d[0] = 0;
			b[1] = b[0];
			c[1] = 0;
			d[1] = 0;
			return;
		}
		
		//	Set up tridiagonal system
		//	b=diagonal, d=offdiagonal, c=right-hand side
		d[0] = x[1] - x[0];
		c[1] = (y[1]-y[0])/d[0];
		for (int i=1; i < n-1; ++i) {
			d[i] = x[i+1] - x[i];
			b[i] = 2.0*(d[i-1]+d[i]);
			c[i+1] = (y[i+1]-y[i])/d[i];
			c[i] = c[i+1] - c[i];
		}
		
		//	End conditions.  third derivatives at x[1] and x[n] obtained
		//	from divided differences.
		b[0] = -d[0];
		b[n-1] = -d[n-2];
		c[0] = 0.0;
		c[n-1] = 0.0;
		if (n > 3) {
			c[0] = c[2]/(x[3]-x[1]) - c[1]/(x[2]-x[0]);
			c[n-1] = c[n-2]/(x[n-1]-x[n-3]) - c[n-3]/(x[n-2]-x[n-4]);
			c[0] = c[0]*d[0]*d[0]/(x[3]-x[0]);
			c[n-1] = -c[n-1]*d[n-2]*d[n-2]/(x[n-1]-x[n-4]);
		}
		
		for (int i=1; i < n; ++i) {
			//	forward elimination
			double t = d[i-1]/b[i-1];
			b[i] = b[i] - t*d[i-1];
			c[i] = c[i] - t*c[i-1];
		}
		
		c[n-1] = c[n-1]/b[n-1];	//	back substitution ( makes C the sigma of text)
		for (int i=n-2; i >= 0; --i) {
			c[i] = (c[i]-d[i]*c[i+1])/b[i];
		}
		
		//	Compute polynomial coefficients
		b[n-1] = (y[n-1]-y[n-2])/d[n-2] + d[n-2]*(c[n-2]+c[n-1]+c[n-1]);
		for (int i=0; i < n-1; ++i) {
			b[i] = (y[i+1]-y[i])/d[i] - d[i]*(c[i+1]+c[i]+c[i]);
			d[i] = (c[i+1]-c[i])/d[i];
			c[i] = 3.0*c[i];
		}
		c[n-1] = 3.0*c[n-1];
		d[n-1] = d[n-2];
		
	}
	
	
	/**
	*  Evaluate the cubic spline function.
	*  seval=y(i)+b(i)*(u-x(i))+c(i)*(u-x(i))**2+d(i)*(u-x(i))**3
	*  where:  x(i)=<u<x(i+1)
	*  NOTES - From "Computer Methods for Mathematical Computations", by
	*       Forsythe, Malcolm, and Moser. Prentice-Hall, 1977.
	*
	*  @param n  # of data points (n>1)
	*  @param u  abscissa at which the spline is to be evaluated.
	*  @param x  abscissas of knots: x[0..n-1]
	*  @param y  ordinates of knots: x[0..n-1]
	*  @param b  linear coeff.: b[0..n-1]
	*  @param c  quadratic coeff.: c[0..n-1]
	*  @param d  cubic coeff.: d[0..n-1]
	*  @return The spline function value evaluated at u.
	**/
	protected final double seval(int n, double u, double[] x, double[] y, double[] b, double[] c, double[] d) {
		
		if (n <= 1 )	return y[0];
		
		int i = 0;
		int j = n-1;
		while (true) {
			int k = (i+j)/2;
			if (u < x[k])
				j = k;
			else
				i = k;
			if ( j <= i+1) {
				double dx = u - x[i];
				return (y[i] + dx*(b[i] + dx*(c[i] + dx*d[i])));
			}
		}
	}
	
	/**
	*  Evaluate the cubic spline function and it's 1st, 2nd and 3rd derivatives.
	*  seval=y(i)+b(i)*(u-x(i))+c(i)*(u-x(i))**2+d(i)*(u-x(i))**3
	*  where:  x(i)=<u<x(i+1)
	*  NOTES- if u < x(1), i=0 is used;if u > x(n), i=n-1 is used
	*
	*  NOTES - From "Computer Methods for Mathematical Computations", by
	*       Forsythe, Malcolm, and Moser. Prentice-Hall, 1977.
	*
	*  @param n  # of data points (n>1)
	*  @param u  abscissa at which the spline is to be evaluated.
	*  @param x  abscissas of knots: x[0..n-1]
	*  @param y  ordinates of knots: x[0..n-1]
	*  @param b  linear coeff.: b[0..n-1]
	*  @param c  quadratic coeff.: c[0..n-1]
	*  @param d  cubic coeff.: d[0..n-1]
	*  @param outputs An existing 2-element array that will be filed with the
	*                 the spline function value and it's 1st derivative:
	*                 outputs[seval..deriv].
	**/
	private void seval2(int n, double u, double[] x, double[] y, double[] b, double[] c, double[] d,
							double[] outputs) {
		
		if (n <= 1) {
			outputs[0] = y[0];
			outputs[1] = 0;
			return;
		}
		
		int i = 0;
		int j = n-1;
		while (true) {
			int k = (i+j)/2;
			if (u < x[k])
				j = k;
			else
				i = k;
			if (j <= i+1) {
				double dx = u - x[i];
				outputs[0] = y[i] + dx*(b[i] + dx*(c[i] + dx*d[i]));
				outputs[1] = b[i] + dx*(c[i]+c[i]+dx*(d[i]+d[i]+d[i]));
				return;
			}
		}
	}
	
}


