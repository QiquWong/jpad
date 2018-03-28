/*
*   NACA5Reflexed -- An arbitrary cambered NACA 5 digit reflexed airfoil.
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
*  <p> This class represents an arbitrary cambered, reflexed,
*      NACA 5 digit airfoil section with 3 digit camber such
*      as a NACA 23112 airfoil.
*      The 1st digit is defined as 2/3 of the design lift
*      coefficient (in tenths, i.e.: 2 denotes cl = 0.3).
*      The 2nd digit is twice the chordwise location of maximum
*      camber in tenths of chord.  The 3rd digit of one
*      indicates a reflexed trailing edge and the
*      last two digits indicate the thickness ratio in
*      percent chord.
*  </p>
*
*  <p> Ported from FORTRAN "NACA4.FOR" to Java by:
*                Joseph A. Huwaldt, October 11, 2000     </p>
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
*  @author  Joseph A. Huwaldt   Date:  October 11, 2000
*  @version March 7, 2013
**/
public class NACA5Reflexed extends NACA4Uncambered {

	/**
	*  The camber function K factor.
	**/
	private double k1;
	
	/**
	*  The camber function k2/k1.
	**/
	private double k2ok1;
	
	/**
	*  The camber function "r" factor.
	**/
	private double rFactor;
	
	/**
	*  The postion in x/c of the maximum camber.
	**/
	private double xCM;
	
	
	/**
	*  Create a cambered reflexed NACA 5 digit airfoil with the
	*  specified parameters.  For example:  a NACA 23112 airfoil
	*  translates to:  thickness = 0.12, cl*2/3 = 2, xcamber*2 = 0.30.
	*  The 3rd digit must be 1 for a reflexed airfoil.
	*  This constructor requires the user to specify the 5 digit airfoil
	*  k and r factors and position of max camber explicitly.
	*  See NASA TM X-3284.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  k1         The airfoil camber k1 factor as described in
	*                     NASA TM X-3284.
	*  @param  k2ok1      The ratio of the k2 to k1 camber factors as
	*                     described in NASA TM X-3284.
	*  @param  r          The airfoil camber r factor as described in
	*                     NASA TM X-3284.
	*  @param  xcamber    The position of maximum camber in tenths of chord
	*                     (e.g.:  0.40 ==> max camber at 4% chord).
	*  @param  length     The chord length.
	**/
	public NACA5Reflexed(double thickness, double k1, double k2ok1, double r,
							double xcamber, double length) {
		super(thickness, length);
		
		this.k1 = k1;
		this.k2ok1 = k2ok1;
		rFactor = r;
		xCM = xcamber;
	}
	
	/**
	*  Create a cambered reflexed NACA 5 digit airfoil with the
	*  specified parameters.  For example:  a NACA 23112 airfoil
	*  translates to:  thickness = 0.12, cl*2/3 = 2, xcamber*2 = 0.30.
	*  The 3rd digit must be 1 for a reflexed airfoil.
	*  This constructor requires the thickness and camber code (the
	*  1st 2 digits of the airfoil designation).
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  code       The 1st 2 digits of the airfoil designation.
	*                     Acceptable values are:  22, 23, 24, and 25.
	*  @param  length     The chord length.
	**/
	public NACA5Reflexed(double thickness, int code, double length) {
		super(thickness, length);
		
		switch (code) {
			case 22:
				xCM = 0.10;
				rFactor = 0.1300;
				k1 = 51.990;
				k2ok1 = 0.000764;
				break;
			
			case 23:
				xCM = 0.15;
				rFactor = 0.2170;
				k1 = 15.793;
				k2ok1 = 0.00677;
				break;
			
			case 24:
				xCM = 0.20;
				rFactor = 0.3180;
				k1 = 6.520;
				k2ok1 = 0.0303;
				break;
			
			case 25:
				xCM = 0.25;
				rFactor = 0.4410;
				k1 = 3.191;
				k2ok1 = 0.1355;
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
		buffer.append("1");
		
		if (TOC < 0.1)
			buffer.append("0");
		buffer.append((int)(TOC*100));
		
		if (chord != 1.0) {
			buffer.append(" c=");
			buffer.append(chord);
		}
		return buffer.toString();
	}
	
	//-----------------------------------------------------------------------------------

	/**
	*  Method to determine the local slope of the airfoil at
	*  the leading edge.  This implementation sets the LE
	*  slope to a very large number and computes a value for
	*  tanth that is appropriate for a NACA 5 digit reflexed
	*  airfoil.
	*  This method sets the class variables:  tanth, yp, ypp.
	*
	*  @param  o  The ordinate data structure.  The following are set:  tanth, yp, ypp.
	**/
    @Override
	protected void calcLESlope(Ordinate o) {
		if (k1 < EPS)
			o.tanth = EPS;
		else {
			double r2 = rFactor*rFactor;
			double oneMr = 1 - rFactor;
			double oneMr3 = oneMr*oneMr*oneMr;
			o.tanth = k1*(3*r2 - k2ok1*oneMr3 - r2*rFactor)/6;
		}
		o.yp = BIG;
		o.ypp = BIG;
	}
	
	/**
	*  Method to calculate the camber distribution for the airfoil.
	*  This method computes a camber distribution that is appropriate
	*  for a NACA 5 digit reflexed airfoil.
	*  This method sets the class variables:  yCMB, tanth, thp.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: yCMB, tanth, thp.
	*  @return The following function value is returned: sqrt(1 + tanth^2).
	**/
    @Override
	protected double calcCamber(double x, Ordinate o) {
	
		double r3 = rFactor*rFactor*rFactor;
		double oneMr = 1 - rFactor;
		double oneMr3 = oneMr*oneMr*oneMr;
		double xMr = x - rFactor;
		double xMr2 = xMr*xMr;
		
		if (x > rFactor) {
			o.yCMB = k1*(k2ok1*xMr2*xMr - k2ok1*oneMr3*x - r3*x + r3)/6;
			o.tanth = k1*(3*k2ok1*xMr2 - k2ok1*oneMr3 - r3)/6;
			
		} else {
			o.yCMB = k1*(xMr2*xMr - k2ok1*oneMr3*x - r3*x + r3)/6;
			o.tanth = k1*(3*xMr2 - k2ok1*oneMr3 - r3)/6;
		}
		
		double func = Math.sqrt(1 + o.tanth*o.tanth);
		
		if (x > rFactor) {
			o.thp = k2ok1*k1*xMr/(func*func);
			
		} else 
			o.thp = k1*xMr/(func*func);
		
		return func;
	}
	
	//-----------------------------------------------------------------------------------

	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA5Reflexed...");
		
		System.out.println("Creating a NACA 23112 airfoil...");
		Airfoil af = new NACA5Reflexed(0.12, 23, 1);
		
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


