/*
*   NACA5Cambered -- An arbitrary cambered NACA 5 digit unreflexed airfoil.
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
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
*  <p>  This class represents an arbitrary cambered unreflexed
*       NACA 5 digit airfoil section with 3 digit camber such
*       as a NACA 23012 airfoil.
*       The 1st digit is defined as 2/3 of the design lift
*       coefficient (in tenths, i.e.: 2 denotes cl = 0.3).
*       The 2nd digit is twice the chordwise location of maximum
*       camber in tenths of chord.  The 3rd digit of zero
*       indicates a non-reflexed trailing edge and the
*       last two digits indicate the thickness ratio in
*       percent chord.
*  </p>
*
*  <p> Ported from FORTRAN "NACA4.FOR" to Java by:
*                Joseph A. Huwaldt, October 9, 2000     </p>
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
*  @author  Joseph A. Huwaldt   Date:  October 9, 2000
*  @version March 7, 2013
**/
public class NACA5Cambered extends NACA4Uncambered {

	/**
	*  The camber function K factor.
	**/
	private double k1;
	
	/**
	*  The camber function "r" factor.
	**/
	private double rFactor;
	
	/**
	*  The position in x/c of the maximum camber.
	**/
	private double xCM;
	
	
	/**
	*  Create a cambered unreflexed NACA 5 digit airfoil with the
	*  specified parameters.  For example:  a NACA 23012 airfoil
	*  translates to:  thickness = 0.12, cl*2/3 = 2, xcamber*2 = 0.30.
	*  The 3rd digit must be 0 for an unreflexed airfoil.
	*  This constructor requires the user to specify the 5 digit airfoil
	*  k and r factors and position of max camber explicitly.
	*  See NASA TM X-3284.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  k1         The airfoil camber k factor as described in
	*                     NASA TM X-3284.
	*  @param  r          The airfoil camber r factor as described in
	*                     NASA TM X-3284.
	*  @param  xcamber    The position of maximum camber in tenths of chord
	*                     (e.g.:  0.40 ==> max camber at 4% chord).
	*  @param  length     The chord length.
	**/
	public NACA5Cambered(double thickness, double k1, double r, double xcamber, double length) {
		super(thickness, length);
		
		this.k1 = k1;
		rFactor = r;
		xCM = xcamber;
	}
	
	/**
	*  Create a cambered unreflexed NACA 5 digit airfoil with the
	*  specified parameters.  For example:  a NACA 23012 airfoil
	*  translates to:  thickness = 0.12, cl*2/3 = 2, xcamber*2 = 0.30.
	*  The 3rd digit must be 0 for an unreflexed airfoil.
	*  This constructor requires the thickness and camber code (the
	*  1st 2 digits of the airfoil designation).
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  code       The 1st 2 digits of the airfoil designation.
	*                     Acceptable values are:  21, 22, 23, 24, and 25.
	*  @param  length     The chord length.
	**/
	public NACA5Cambered(double thickness, int code, double length) {
		super(thickness, length);
		
		switch (code) {
			case 21:
				xCM = 0.05;
				rFactor = 0.0580;
				k1 = 361.400;
				break;
			
			case 22:
				xCM = 0.10;
				rFactor = 0.1260;
				k1 = 51.640;
				break;
			
			case 23:
				xCM = 0.15;
				rFactor = 0.2025;
				k1 = 15.957;
				break;
			
			case 24:
				xCM = 0.20;
				rFactor = 0.2900;
				k1 = 6.643;
				break;
			
			case 25:
				xCM = 0.25;
				rFactor = 0.3910;
				k1 = 3.230;
				break;
			
			default:
				throw new IllegalArgumentException("Unknown camber code.");
		}
		
		
	}
	

	/**
	*  Returns a string representation of this airfoil
	*  (the NACA designation of this airfoil).
	**/
    @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("NACA 2");
		
		buffer.append((int)(xCM*20));
		buffer.append("0");
		
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
	*  the leading edge.  This implementation sets the LE
	*  slope to a very large number and sets the value for
	*  tanth to k1*rFactor*rFactor*(3 - rFactor)/6.
	*  This method sets the class variables:  tanth, yp, ypp.
	*
	*  @param  o  The ordinate data structure.  The following are set:  tanth, yp, ypp.
	**/
    @Override
	protected void calcLESlope(Ordinate o) {
		if (k1 < EPS)
			o.tanth = EPS;
		else
			o.tanth = k1*rFactor*rFactor*(3 - rFactor)/6;
		o.yp = BIG;
		o.ypp = BIG;
	}
	
	/**
	*  Method to calculate the camber distribution for the airfoil.
	*  This implementation computes a camber distribution that is
	*  appropriate for an unreflexed NACA 5 digit cambered airfoil.
	*  This method sets the class variables:  yCMB, tanth, thp.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: yCMB, tanth, thp.
	*  @return The following function value is returned: sqrt(1 + tanth^2).
	**/
    @Override
	protected double calcCamber(double x, Ordinate o) {
	
		double r2 = rFactor*rFactor;
		
		if (x > rFactor) {
			double r3 = r2*rFactor;
			o.yCMB = k1*r3*(1 - x)/6;
			o.tanth = -k1*r3/6;
			
		} else {
			double x2 = x*x;
			double x3 = x2*x;
			o.yCMB = k1*(x3 - 3*rFactor*x2 + r2*(3 - rFactor)*x)/6;
			o.tanth = k1*(3*x2 - 6*rFactor*x + r2*(3 - rFactor))/6;
		}
		
		double func = Math.sqrt(1 + o.tanth*o.tanth);
		
		if (x > rFactor) {
			o.thp = 0;
			
		} else 
			o.thp = k1*(x - rFactor)/(func*func);
		
		return func;
	}
	
	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA5Cambered...");
		
		System.out.println("Creating a NACA 23012 airfoil...");
		Airfoil af = new NACA5Cambered(0.12, 23, 1);
		
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


