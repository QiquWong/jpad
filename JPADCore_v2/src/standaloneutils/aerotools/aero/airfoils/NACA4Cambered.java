/*
*   NACA4Cambered -- An arbitrary cambered NACA 4 digit airfoil.
*
*   Copyright (C) 2000-2013 by Joseph A. Huwaldt
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
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
*  <p> This class represents an arbitrary cambered NACA 4
*      digit airfoil section with 2 digit camber such as a
*      NACA 6409 airfoil.
*      The 1st digit is the maximum camber in percent chord.
*      The 2nd digit is the chordwise location of maximum
*      camber in tenths of chord.  The last two digits are
*      the airfoil thickness in percent chord.
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
*         REFERENCES-  NASA Technical Memorandum TM X-3284 (November, 1975)
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
public class NACA4Cambered extends NACA4Uncambered {

	/**
	*  The amount of camber in terms of maximum ordinate-to-chord ratio.
	**/
	private double CMB;
	
	/**
	*  The position in x/c of the maximum camber.
	**/
	private double xCM;
	
	
	/**
	*  Create a cambered NACA 4 digit airfoil with the
	*  specified parameters.  For example:  a NACA 6409 airfoil
	*  translates to:  thickness = 0.09, camber = 0.06, xcamber = 0.40.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  camber     The maximum camber ordinate-to-chord ratio
	*                     (e.g.:  0.06 ==> 6% camber/c).
	*  @param  xcamber    The position of maximum camber in tenths of chord
	*                     (e.g.:  0.40 ==> max camber at 4% chord).
	*  @param  length     The chord length.
	**/
	public NACA4Cambered(double thickness, double camber, double xcamber, double length) {
		super(thickness, length);
		
		CMB = camber;
		xCM = xcamber;
	}
	

	/**
	*  Returns a string representation of this airfoil
	*  (the NACA designation of this airfoil).
	**/
    @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("NACA ");
		
		buffer.append((int)(CMB*100));
		buffer.append((int)(xCM*10));
		
		if (TOC < 0.1)
			buffer.append("0");
		buffer.append((int)(TOC*100));
		
		if (chord != 1.0) {
			buffer.append(" c=");
			buffer.append(chord);
		}
		return buffer.toString();
	}
	
	/**
	*  Method to determine the local slope of the airfoil at
	*  the leading edge.  This implementation sets the
	*  slope of the leading edge to a very large number and
	*  sets tanth to 2*CMB/xCM.
	*  This method sets the class variables:  tanth, yp, ypp.
	*
	*  @param  o  The ordinate data structure.  The following are set:  tanth, yp, ypp.
	**/
    @Override
	protected void calcLESlope(Ordinate o) {
		if (CMB < EPS)
			o.tanth = EPS;
		else
			o.tanth = 2*CMB/xCM;
		o.yp = BIG;
		o.ypp = BIG;
	}
	
	/**
	*  Method to calculate the camber distribution for the airfoil.
	*  This implementation computes a camber distribution that is
	*  appropriate for a cambered NACA 4 digit airfoil.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: yCMB, tanth, thp.
	*  @return The following function value is returned: sqrt(1 + tanth^2).
	**/
    @Override
	protected double calcCamber(double x, Ordinate o) {
		if (x > xCM) {
			double OneMxCM = 1 - xCM;
			double OneMxCM2 = OneMxCM*OneMxCM;
			o.yCMB = CMB*(1 - 2*xCM + 2*xCM*x - x*x)/OneMxCM2;
			o.tanth = (2*xCM - 2*x)*CMB/OneMxCM2;
			
		} else {
			o.yCMB = CMB*(2*xCM*x - x*x)/(xCM*xCM);
			o.tanth = 2*CMB*(1 - x/xCM)/xCM;
		}
		
		double func = Math.sqrt(1 + o.tanth*o.tanth);
		
		if (x > xCM) {
			double OneMxCM = 1 - xCM;
			o.thp = -2*CMB/(OneMxCM*OneMxCM)/(func*func);
			
		} else 
			o.thp = -2*CMB/(xCM*xCM)/(func*func);
		
		return func;
	}

	
	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA4Cambered...");
		
		System.out.println("Creating a NACA 6409 airfoil...");
		Airfoil af = new NACA4Cambered(0.09, 0.06, 0.4, 1);
		
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


