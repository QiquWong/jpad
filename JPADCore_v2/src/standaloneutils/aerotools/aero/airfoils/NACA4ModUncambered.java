/*
*   NACA4ModUncambered -- An arbitrary uncambered NACA 4 digit modified airfoil.
*
*   Copyright (C) 2000-2012, by Joseph A. Huwaldt
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
*  <p> This class represents an arbitrary uncambered NACA
*      modified 4 digit airfoil section such as a NACA 0012-34
*      airfoil.  The 1st two digits indicate a symmetric airfoil,
*      the 2nd two, the thickness-chord ratio.  The 1st digit
*      after the hyphen is the leading edge radius index and
*      the last digit after the hyphen is the position of
*      maximum thickness in tenths of chord.
*  </p>
*
*  <p> Ported from FORTRAN "NACA4.FOR" to Java by:
*                Joseph A. Huwaldt, October 19, 2000     </p>
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
*  @author  Joseph A. Huwaldt   Date:  October 19, 2000
*  @version September 15, 2012
**/
public class NACA4ModUncambered extends NACA4Uncambered {

	/**
	*  Table relating t/c ratio and LE index to LE radius.
	**/
	private static final double[][] tableRle = {
		{ 0.000077, 0.000306, 0.000689, 0.001224, 0.001913, 0.002755, 0.003749, 0.004897 },
		{ 0.000110, 0.000441, 0.000992, 0.001763, 0.002755, 0.003967, 0.005399, 0.007052 },
		{ 0.000150, 0.000600, 0.001350, 0.002400, 0.003749, 0.005399, 0.007349, 0.009599 },
		{ 0.000196, 0.000784, 0.001763, 0.003134, 0.004897, 0.007052, 0.009599, 0.012537 },
		{ 0.000248, 0.000992, 0.002231, 0.003967, 0.006198, 0.008925, 0.012148, 0.015867 },
		{ 0.000306, 0.001224, 0.002755, 0.004897, 0.007652, 0.011019, 0.014998, 0.019589 },
		{ 0.000370, 0.001481, 0.003333, 0.005925, 0.009259, 0.013333, 0.018147, 0.023703 },
		{ 0.000441, 0.001763, 0.003967, 0.007052, 0.011019, 0.015867, 0.021597, 0.028208 },
		{ 0.000517, 0.002069, 0.004655, 0.008276, 0.012932, 0.018622, 0.025346, 0.033105 },
		{ 0.000600, 0.002400, 0.005399, 0.009599, 0.014998, 0.021597, 0.029395, 0.038394 },
		{ 0.000689, 0.002755, 0.006198, 0.011019, 0.017217, 0.024792, 0.033745, 0.044075 },
		{ 0.000784, 0.003134, 0.007052, 0.012537, 0.019589, 0.028208, 0.038394, 0.050147 },
		{ 0.000885, 0.003438, 0.007961, 0.014153, 0.022114, 0.031844, 0.043343, 0.056612 },
		{ 0.000992, 0.003967, 0.008925, 0.015867, 0.024792, 0.035701, 0.048592, 0.063468 },
		{ 0.001105, 0.004420, 0.009944, 0.017679, 0.027623, 0.039778, 0.054142, 0.070716 },
		{ 0.001224, 0.004897, 0.011019, 0.019589, 0.030608, 0.044075, 0.059991, 0.078355 },
		{ 0.001350, 0.005399, 0.012148, 0.021597, 0.033745, 0.048592, 0.066140, 0.086387 } };
	
	private static final double[] toc = {0.05, 0.06, 0.07, 0.08, 0.09,
				0.10, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
				0.20, 0.21 };
	
	/**
	*  The leading edge radius in percent chord length.
	**/
	protected double RLE;
	
	/**
	*  The leading edge radius index.
	**/
	protected int leIndex = 0;
	
	/**
	*  The coefficients of the profile equation.
	**/
	private double a0, a1, a2, a3;
	private double d0, d1, d2, d3;			//	d1 = trailing edge slope.
	
	
	/**
	*  Create an uncambered NACA modified 4 digit airfoil with the
	*  specified parameters.  For example:  a NACA 0012-34 airfoil
	*  translates to:  thickness = 0.12, Rle = 0.003967, xThickness = 0.40.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  Rle        The radius of the leading edge of the airfoil in
	*                     percent chord.
	*  @param  xThickness The position of maximum thickness in percent chord
	*                     (e.g.:  0.40 => 40% t/c).
	*  @param  length     The chord length.
	**/
	public NACA4ModUncambered(double thickness, double Rle, double xThickness, double length) {
		super(thickness, length);
		
		xMaxT = xThickness;
		RLE = Rle;
		
		calcCoefficients();
		
	}
	
	/**
	*  Create an uncambered NACA modified 4 digit airfoil with the
	*  specified parameters.  For example:  a NACA 0012-34 airfoil
	*  translates to:  thickness = 0.12, LEindex = 3, xThickness = 0.40.
	*
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.09 ==> 9% t/c).
	*  @param  LEindex    The leading edge radius index for this airfoil.  Should
	*                     be a number from 1 to 8.
	*  @param  xThickness The position of maximum thickness in percent chord
	*                     (e.g.:  0.40 => 40% t/c).
	*  @param  length     The chord length.
	**/
	public NACA4ModUncambered(double thickness, int LEindex, double xThickness, double length) {
		super(thickness, length);
		
		xMaxT = xThickness;
		RLE = lookupRle(thickness, LEindex);
		leIndex = LEindex;
		
		calcCoefficients();
		
	}
	

	/**
	*  Returns a string representation of this airfoil
	*  (the NACA designation of this airfoil).
	**/
    @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("NACA 00");
		
		if (TOC < 0.1)
			buffer.append("0");
		buffer.append((int)(TOC*100));
		
		buffer.append("-");
		
		if (leIndex == 0)
			buffer.append("?");
		else
			buffer.append(leIndex);
		
		buffer.append((int)(xMaxT*10));
		
		if (chord != 1.0) {
			buffer.append(" c=");
			buffer.append(chord);
		}
		return buffer.toString();
	}
	
	/**
	*  Method to calculate the ordinates of the uncambered airfoil
	*  forward of the maximum thickness point.  This implementation
	*  computes a set of ordinates that are appropriate for a modified
	*  NACA 4 digit airfoil.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: y, yp, ypp.
	**/
    @Override
	protected void calcOrdinateForward(double x, Ordinate o) {
		//	Uncambered ordinate.
		double sqrtx = Math.sqrt(x);
		double x2 = x*x;
		o.y = a0*sqrtx + a1*x + a2*x2 + a3*x2*x;
		o.yp = 0.5*a0/sqrtx + a1 + 2*a2*x + 3*a3*x2;
		o.ypp = -0.5*0.5*a0/Math.sqrt(x2*x) + 2*a2 + 3*2*a3*x;
	}
	
	/**
	*  Method to calculate the ordinates of the uncambered airfoil
	*  aft of the maximum thickness point.  This implementation
	*  computes a set of ordinates that are appropriate for a modified
	*  NACA 4 digit airfoil.
	*
	*  @param  x  The x/c location currently being calculated.
	*  @param  o  The ordinate data structure. The following are set: y, yp, ypp.
	**/
    @Override
	protected void calcOrdinateAft(double x, Ordinate o) {
		double oneMx = 1 - x;
		double oneMx2 = oneMx*oneMx;
		o.y = d0 + d1*oneMx + d2*oneMx2 + d3*oneMx2*oneMx;
		o.yp = -d1 - 2*d2*oneMx - 3*d3*oneMx2;
		o.ypp = 2*d2 + 6*d3*oneMx;
	}
	
	//-----------------------------------------------------------------------------------

	/**
	*  Calculate the ordinate equation coefficients required
	*  for a modified, uncambered, NACA 4 digit airfoil.
	**/
	private void calcCoefficients() {
		a0 = Math.sqrt(2*RLE)*0.2/TOC;
		d0 = 0.002;
		double xM2 = xMaxT*xMaxT;
		d1 = 0.1*(2.24 - 5.42*xMaxT + 12.3*xM2)/(1 - 0.878*xMaxT);
		double oneMxM = 1 - xMaxT;
		double oneMxM2 = oneMxM*oneMxM;
		d3 = (3*d1 - 0.588/oneMxM)/(3*oneMxM2);
		d2 = -1.5*oneMxM*d3 - 0.5*d1/oneMxM;
		a3 = 0.1/xM2/xMaxT + (2*d1*oneMxM - 0.588)/(2*xMaxT*oneMxM2) -
						3*a0/(8*Math.pow(xMaxT, 2.5));
		a2 = -0.10/xM2 + 0.5*a0/Math.pow(xMaxT, 1.5) - 2*xMaxT*a3;
		a1 = -0.5*a0/Math.sqrt(xMaxT) - 2*xMaxT*a2 - 3*xM2*a3;
	}

	/**
	*  Lookup the appropriate RLE value from the LE index and t/c.
	**/
	private static double lookupRle(double thickness, int LEindex) {
		--LEindex;
		
		//	Do a range check.
		if (LEindex < 0)
			LEindex = 0;
		else if (LEindex > 7)
			LEindex = 7;
		
		//	Find the indexes of the toc values that bound the thickness.
		int length = toc.length;
		int i=0;
		for (; i < length; ++i)
			if (toc[i] > thickness)
				break;
		int indexL = 0;
		if (i == length)
			indexL = i - 2;
		else if (i != 0)
			indexL = i - 1;
		int indexH = indexL + 1;
		
		//	Do the interpolation into the table.
		double value = lineInterp(toc[indexH], tableRle[indexH][LEindex],
								toc[indexL], tableRle[indexL][LEindex], thickness);
		
		return value;
	}
	
	/**
	*  Simple linear 1D interpolation between two points.
	*
	*  @param   x1,y1  Coordinates of the 1st point (the high point).
	*  @param   x2,y2  Coordinates of the 2nd point (the low point).
	*  @param   x      The X coordinate of the point for which we want to
	*                  interpolate to determine a Y coordinate.  Will
	*                  extrapolate if X is outside of the bounds of the 
	*                  point arguments.
	*  @return  The interpolated Y value corresponding to the input X
	*           value is returned.
	**/
	private static double lineInterp( double x1, double y1, double x2, double y2, double x ) {
		return ((y2 - y1)/(x2 - x1)*(x - x1) + y1);
	}


	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA4ModUncambered...");
		
		System.out.println("Creating a NACA 0010-64 airfoil...");
		Airfoil af = new NACA4ModUncambered(0.10, 6, 0.4, 1);
		
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


