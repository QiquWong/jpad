/*
*   NACA4Uncambered -- An arbitrary uncambered NACA 4 digit airfoil.
*
*   Copyright (C) 2000-2013, by Joseph A. Huwaldt
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
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
*  <p> This class represents an arbitrary uncambered NACA 4
*      digit airfoil section such as a NACA 0012 airfoil.
*      The 1st two digits indicate a symmetric airfoil,
*      the 2nd two, the thickness to chord ratio.  All NACA 4
*      and 5 digit (and 16 series and mod-4 digit) airfoils
*      inherit from this class the generic code for generating
*      airfoil ordinates.
*  </p>
*
*  <p> Ported from FORTRAN "NACA4.FOR" to Java by:
*                Joseph A. Huwaldt, October 8, 2000     </p>
*
*  <p> Original FORTRAN "NACA4" code had the following note:  </p>
*
*  <pre>
*         AUTHORS - Charles L.Ladson and Cuyler W. Brooks, NASA Langley
*                   Liam Hardy, NASA Ames
*                   Ralph Carmichael, Public Domain Aeronautical Software
*         Last FORTRAN version:  8Aug95  1.7   RLC
*
*         NOTES - This program has been known by the names ANALIN, FOURDIGIT and NACA4.
*         REFERENCES-  NASA Technical Memorandum TM X-3284 (November, 1975),
*                      "Development of a Computer Program to Obtain Ordinates for
*                      NACA 4-Digit, 4-Digit Modified, 5-Digit and 16-Digit Airfoils",
*                      by Charles L. Ladson and Cuyler W. Brooks, Jr.,
*                      NASA Langley Research Center.
*
*                      NASA Technical Memorandum TM 4741 (December 1996),
*                      "Computer Program to Obtain Ordinates for NACA Airfoils",
*                      by Charles L. Ladson, Cuyler W. Brooks, Jr., and Acquilla S. Hill,
*                      NASA Langley Research Center and
*                      Darrell W. Sproles, Computer Sciences Corporation, Hampton, VA.
*
*                      "Theory of Wing Sections", by Ira Abbott and Albert Von Doenhoff.
*  </pre>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 8, 2000
*  @version March 7, 2013
**/
public class NACA4Uncambered implements Airfoil {

	// Constants
	protected static final double EPS = 1.E-10;
	protected static final double BIG = 1./EPS;
	private static final double kDX = 0.01;
	
	// Ordinate equation coefficients.
	private static final double a0=0.2969, a1=-0.1260, a2=-0.3516;
	private static final double a3=0.2843, a4=-0.1015;

	/**
	*  Chord location of maximum thickness.
	**/
	protected double xMaxT = 0.50F;
	
	
	// Inputs required by an uncambered NACA 4 digit airfoil.
	/**
	*  Thickness-to-Chord Ratio
	**/
	protected double TOC = 0.20F;
	
	/**
	*  Chord length.  Ordinates are scaled to this chord length.
	**/
	protected double chord = 1;
	
	
	// Buffers for the output data.
	protected transient List<Point2D> upper, lower;
	protected transient List<Point2D> camberLine;
	protected transient List<Double> yUp, yLp;
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Creates an uncambered NACA 4 digit airfoil with a
	*  thickness to chord ratio of 20% and a chord length of 1.0.
	**/
	public NACA4Uncambered() { }
	
	/**
	*  Create an uncambered NACA 4 digit airfoil with the
	*  specified parameters.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA4Uncambered(double thickness, double length) {
		chord = length;
		TOC = thickness;
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
		StringBuilder buffer = new StringBuilder("NACA 00");
		
		if (TOC < 0.1F)
			buffer.append("0");
		buffer.append((int)(TOC*100));
		
		if (chord != 1.0F) {
			buffer.append(" c=");
			buffer.append(chord);
		}
		return buffer.toString();
	}
	

	//-----------------------------------------------------------------------------------

	/**
	*  Method to determine the local slope of the airfoil at
	*  the leading edge.  This implementation sets the leading
	*  edge slope to a very large number (essentially infinity)
	*  as is appropriate for uncambered NACA 4 airfoils.  Sub-
	*  classes should override this method to provide an appropriate
	*  leading edge slope.
	*
	*  @param  o  The ordinate data structure.  The following are set:  tanth, yp, ypp.
	**/
	protected void calcLESlope(Ordinate o) {
		o.tanth = EPS;
		o.yp = BIG;
		o.ypp = BIG;
	}
	
	/**
	*  Method to calculate the ordinate of the uncambered airfoil
	*  forward of the maximum thickness point.  This implementation
	*  is appropriate for uncambered NACA 4 airfoils.  Sub-classes
	*  should override this method to compute the ordinates forward
	*  of the max thickness point.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: y, yp, ypp.
	**/
	protected void calcOrdinateForward(double x, Ordinate o) {
		//	Uncambered ordinate.
		double sqrtx = Math.sqrt(x);
		double x2 = x*x;
		o.y = a0*sqrtx + a1*x + a2*x2 + a3*x2*x + a4*x2*x2;
		o.yp = 0.5*a0/sqrtx + a1 + 2*a2*x + 3*a3*x2 + 4*a4*x2*x;
		o.ypp = -0.5*0.5*a0/Math.sqrt(x2*x) + 2*a2 + 3*2*a3*x + 4*3*a4*x2;
	}
	
	/**
	*  Method to calculate the ordinate of the uncambered airfoil
	*  aft of the maximum thickness point.  This implementation
	*  simply calls calcOrdinateForward().  Sub-classes should
	*  override this method to compute the ordinates aft of the
	*  max thickness point.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: y, yp, ypp.
	**/
	protected void calcOrdinateAft(double x, Ordinate o) {
		calcOrdinateForward(x,o);
	}
	
	/**
	*  Method to calculate the camber distribution for the airfoil.
	*  This implementation simply sets camber ralated variables
	*  to zero (no camber).  Sub-classes should override this method
	*  to calculate camber.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: yCMB, tanth, thp.
	*  @return The following function value is returned: sqrt(1 + tanth^2).
	**/
	protected double calcCamber(double x, Ordinate o) {
		//	Uncambered airfoil.
		o.yCMB = 0;
		o.tanth = 0;
		o.thp = 0;
		
		double func = 1;
		return func;
	}

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
		
		//	Create an ordinate data structure for passing around data.
		Ordinate ord = new Ordinate();

		// Calculate the slope of the leading edge point.
		calcLESlope(ord);
		double tanth = ord.tanth;
		double y;
		double yp;
		double ypp = ord.ypp;
		
		// Start filling in the data arrays.
		int i = 0;
		upper.add(new Point2D.Double(0,0));
		lower.add(new Point2D.Double(0,0));
		yUp.add(-1./tanth);
		yLp.add(-1./tanth);
		
		// Generate airfoil upstream of max thickness point.
		double x = 0.00025;
		while (x < xMaxT && Math.abs(x-xMaxT) >= EPS) {
		
			// Calculate the ordinate of an uncambered airfoil.
			calcOrdinateForward(x,ord);
			y = ord.y*TOC/0.2;
			yp = ord.yp*TOC/0.2;
			ypp = ord.ypp*TOC/0.2;
			double xC = x*chord;
			double yC = y*chord;
			
			// Calculate the camber profile offset.
			double func = calcCamber(x, ord);
			tanth = ord.tanth;
			double yCMB = ord.yCMB;
			double thp = ord.thp;
			double sinth = tanth/func;
			double costh = 1./func;
			
			// Add camber to uncambered ordinate and output.
			++i;
			double xUi = (x - y*sinth)*chord;
			double yUi = (yCMB + y*costh)*chord;
			double xLi = (x + y*sinth)*chord;
			double yLi = (yCMB - y*costh)*chord;
			upper.add(new Point2D.Double(xUi, yUi));
			lower.add(new Point2D.Double(xLi, yLi));
			camberLine.add(new Point2D.Double(x*chord, yCMB*chord));
			
			// Calculate the slope of the airfoil upper and lower surface.
			double yUpr;
			double yLpr;
			if (Math.abs(tanth) >= EPS) {
				yUpr = (tanth*func + yp - tanth*y*thp)/(func - yp*tanth - y*thp);
				yLpr = (tanth*func - yp + tanth*y*thp)/(func + yp*tanth + y*thp);
			} else 
				yUpr = yLpr = yp;
			
			yUp.add(yUpr);
			yLp.add(yLpr);
			
			// Prepare to calculate the next point on the airfoil.
			double frac = 1;
			if (x <= 0.00225)
				frac = 0.025;
			else if (x <= 0.0124)
				frac = 0.025;
			else if (x <= 0.0975)
				frac = 0.25;
			
			x += frac*kDX;
		}
		
		//	Ensure the x = xMaxT is a computed point.
		x = xMaxT;
		
		//	Generate airfoil downstream of max thickness point.
		while (x <= 1.0) {
			calcOrdinateAft(x,ord);
			y = ord.y*TOC/0.2;
			yp = ord.yp*TOC/0.2;
			ypp = ord.ypp*TOC/0.2;
			double xC = x*chord;
			double yC = y*chord;
		
			double func = calcCamber(x, ord);
			tanth = ord.tanth;
			double yCMB = ord.yCMB;
			double thp = ord.thp;
			double sinth = tanth/func;
			double costh = 1./func;
			
			++i;
			double xUi = (x - y*sinth)*chord;
			double yUi = (yCMB + y*costh)*chord;
			double xLi = (x + y*sinth)*chord;
			double yLi = (yCMB - y*costh)*chord;
			upper.add(new Point2D.Double(xUi, yUi));
			lower.add(new Point2D.Double(xLi, yLi));
			camberLine.add(new Point2D.Double(x*chord, yCMB*chord));
			
			double yUpr;
			double yLpr;
			if (Math.abs(tanth) >= EPS) {
				yUpr = tanth*(func + yp/tanth - y*thp)/(func - yp*tanth - y*thp);
				yLpr = tanth*(func - yp/tanth + y*thp)/(func + yp*tanth + y*thp);
			} else
				yUpr = yLpr = yp;
			
			yUp.add(yUpr);
			yLp.add(yLpr);
			
			x += kDX;
			
			//	Ensure that x=1 point is calculated.
			if (x > 1 && x < 1 + kDX)	x = 1;
		}
	}
	
	/**
	*  Class that serves as a simple container for airfoil ordinate data.
	**/
	protected class Ordinate {
		/**
		*  Ordinate (Y coordinate) of the uncambered airfoil.
		**/
		public double y;
		
		/**
		*  The local slope of the uncambered airfoil:  yp = dy/dx.
		**/
		public double yp;
		
		/**
		*  The rate of change of the local slope of the
		*  uncambered airfoil: ypp = dy^2/dx^2.
		**/
		public double ypp;
		
		/**
		*  The offset in the ordinate from the uncambered airfoil
		*  to the cambered airfoil.  This is the camber profile.
		**/
		public double yCMB;
		
		/**
		*  The tangent of the local surface angle due to camber.
		**/
		public double tanth;
		
		/**
		*  The local surface angle due to camber.
		**/
		public double thp;
	}

	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA4Uncambered...");
		
		System.out.println("Creating a NACA 0012 airfoil...");
		Airfoil af = new NACA4Uncambered(0.12, 1);
		
		System.out.println("Airfoil = " + af.toString());
		
		//	Output the upper surface of the airfoil.
		List<Point2D> upper = af.getUpper();
		List<Double> ypArr = af.getUpperYp();
		
		System.out.println("        X    \t    Y    \t    dy/dx");
		int length = upper.size();
		for (int i=0; i < length; ++i) {
			Point2D o = upper.get(i);
			System.out.println("    " + nf.format(o.getX()) + "\t" + nf.format(o.getY()) +
									"\t" + nf.format(ypArr.get(i)));
		}
		
		System.out.println("# ordinates = " + length);
		System.out.println("Done!");
	}
}


