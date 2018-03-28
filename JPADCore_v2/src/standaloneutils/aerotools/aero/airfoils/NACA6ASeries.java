/*
*   NACA6ASeries -- An arbitrary NACA 6*A series airfoil.
*
*   Copyright (C) 2010-2013 by Joseph A. Huwaldt
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


/**
*  <p> This class represents an arbitrary NACA 6*A series
*      airfoil section such as a NACA 63A-020 airfoil.
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
*  @author  Joseph A. Huwaldt   Date:  June 5, 2010
*  @version March 7, 2013
**/
public abstract class NACA6ASeries extends NACA6Series {
	
	/**
	*  Create a NACA 6*A series airfoil with the specified parameters.
	*
	*  @param  CLi        Design lift coefficient (e.g.: 63A-206 has CLi = 0.2).
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA6ASeries(double CLi, double thickness, double length) {
		super(CLi, thickness, length);
	}
	
	//-----------------------------------------------------------------------------------
	
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
    @Override
	protected final boolean modCamberLine(double x, boolean if6xa, double ycmb, double tanth, double ycp2, double cli, double[] outputs) {
		ycmb *= 0.97948;
		tanth *= 0.97948;
		if ( tanth <= -.24521*cli )	{
			ycmb = 0.24521*cli*(1.-x);
			ycp2 = 0.0;
			tanth = -0.24521*cli;
			if6xa = true;
		}
		outputs[0] = ycmb;
		outputs[1] = tanth;
		outputs[2] = ycp2;
		return if6xa;
	}
	
	
	private transient double s1=0, s2=0, b1=0, b2=0;
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
    @Override
	protected final void modTrailingEdge(double x, double xu, double yu, double xl, double yl, double[] outputs) {
		if ( x <= 0.825 ) {
			double x2 = 1.0;
			double x1 = xu;
			double y2 = 0.;
			double y1 = yu;
			s1 = (y2-y1)/(x2-x1);
			s2 = (y2-yl)/(x2-xl);
			b1 = y2 - s1*x2;
			b2 = y2 - s2*x2;
		}
		yu = s1*xu + b1;
		yl = s2*xl + b2;
		
		outputs[0] = yu;
		outputs[1] = yl;
	}
	
	
}


