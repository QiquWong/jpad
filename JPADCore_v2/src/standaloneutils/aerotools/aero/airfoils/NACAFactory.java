/*
*   NACAFactory -- Factory class for creating NACA analytical airfoils.
*
*   Copyright (C) 2000-2012 by Joseph A. Huwaldt
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
*  This class contains a method for parsing a NACA airfoil
*  designation string (such as "NACA 2312") and returning
*  a corresponding Airfoil object.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 20, 2000
*  @version September 15, 2012
**/
public class NACAFactory {

	/**
	*  Returns an Airfoil object corresponding to the
	*  airfoil designation sting passed in.  For example,
	*  if "NACA 0012" or "0012" are passed in a
	*  NACA4Uncambered object is returned with a thickness
	*  of 12% toc.
	*
	*  @param  designation  The designation of the airfoil that is desired.
	*  @param  chord        The chord length for the airfoil.
	*  @return An airfoil corresponding to the designation passed in.
	*  @throws IllegalArgumentException if the designation is for an airfoil
	*          type that is unknown to this factory class.
	**/
	public static Airfoil create(String designation, double chord) throws IllegalArgumentException {
		Airfoil af = null;
			
		designation = designation.toUpperCase();

		//	Strip off the "NACA" designator if it is there.
		int pos = designation.indexOf("NACA");
		if (pos >= 0)
			designation = designation.substring(pos+4);
		
		designation = designation.trim();
		
		int length = designation.length();
		
		if (length >= 5 && designation.startsWith("6"))
			//	We have a 6 or 6*A series airfoil.
			af = parse6Series(designation, chord);
			
		else if (length == 4)
			//	We have a 4 digit airfoil.  Parse out the pieces.
			af = parse4Digit(designation, chord);
			
		else if (length == 5)
			//	We have a 5 digit airfoil.  Parse out the pieces.
			af = parse5Digit(designation, chord);
		
		else if (length == 6) {
			int digits = Integer.parseInt(designation.substring(0,2));
			
			if (digits == 16)
				//	We have a 16 series section (really a modified 4 digit section).
				af = parse16Series(designation, chord);
		
		} else if (designation.indexOf("-") == 4) {
			//	We have a modified 4 digit airfoil with camber.  Parse out the pieces.
			af = parseMod4Digit(designation, chord);
			
		}
		
		if (af == null)
			throw new IllegalArgumentException("Unknown airfoil type: " + designation);
		
		return af;
	}
	
	/**
	*  Parse the designation string for a NACA 4 digit airfoil.
	**/
	private static Airfoil parse4Digit(String designation, double chord) {
		Airfoil af;
		
		//	We have a 4 digit airfoil.  Parse out the pieces.
		double camber = Integer.parseInt(designation.substring(0,1));
		camber /= 100;
		double xcamber = Integer.parseInt(designation.substring(1,2));
		xcamber /= 10;
		double thickness = Integer.parseInt(designation.substring(2));
		thickness /= 100;
		
		if (camber == 0)
			af = new NACA4Uncambered(thickness, chord);
		else
			af = new NACA4Cambered(thickness, camber, xcamber, chord);
		
		return af;
	}
	
	
	/**
	*  Parse the designation string for a NACA 5 digit airfoil.
	**/
	private static Airfoil parse5Digit(String designation, double chord) {
		Airfoil af;
		
		//	We have a 5 digit airfoil.  Parse out the pieces.
		int code = Integer.parseInt(designation.substring(0,2));
		int reflex = Integer.parseInt(designation.substring(2,3));
		double thickness = Integer.parseInt(designation.substring(3));
		thickness /= 100;
			
		if (reflex == 0)
			af = new NACA5Cambered(thickness, code, chord);
		else
			af = new NACA5Reflexed(thickness, code, chord);
		
		return af;
	}
	
	/**
	*  Parse the designation string for a NACA 16 series airfoil
	*  (actually a modified 4 digit in disguise).
	**/
	private static Airfoil parse16Series(String designation, double chord) {
		Airfoil af;
		
		//	16 series section (really a modified 4 digit section).
		double thickness = Integer.parseInt(designation.substring(4));
		thickness /= 100;
		int LEindex = 4;
		double xThickness = 0.5;
		
		af = new NACA4ModUncambered(thickness, LEindex, xThickness, chord);
		
		return af;
	}
	
	/**
	*  Parse the designation string for a NACA modified 4 digit airfoil.
	**/
	private static Airfoil parseMod4Digit(String designation, double chord) {
		Airfoil af;
		
		//	We have a modified 4 digit airfoil with camber.  Parse out the pieces.
		double camber = Integer.parseInt(designation.substring(0,1));
		camber /= 100;
		double xcamber = Integer.parseInt(designation.substring(1,2));
		xcamber /= 10;
		double thickness = Integer.parseInt(designation.substring(2,4));
		thickness /= 100;
		int LEindex = Integer.parseInt(designation.substring(5,6));
		double xThickness = Integer.parseInt(designation.substring(6));
		xThickness /= 10;
		
		if (camber == 0)
			af = new NACA4ModUncambered(thickness, LEindex, xThickness, chord);
		else
			af = new NACA4ModCambered(thickness, camber, xcamber, LEindex, xThickness, chord);
		
		return af;
	}
	
	/**
	*  Parse the designation string for a NACA 6 or 6*A series airfoil.
	**/
	private static Airfoil parse6Series(String designation, double chord) {
		Airfoil af = null;
		
		//	Get the profile index.
		int profile = Integer.parseInt(designation.substring(1,2));
		
		int dashOffset = designation.indexOf('-');
		if (dashOffset > 0)	dashOffset = 1;
		else dashOffset = 0;
		
		if ("A".equals(designation.substring(2,3))) {
			//	We have a 6*A series airfoil.
			
			//	Get the design lift coefficient.
			double CLi = Integer.parseInt(designation.substring(3+dashOffset,4+dashOffset));
			CLi /= 10.;
			
			//	Get the t/c.
			double thickness = Integer.parseInt(designation.substring(4+dashOffset));
			thickness /= 100.;
			
			switch (profile) {
				case 3:
					af = new NACA63ASeries(CLi, thickness, chord);
					break;
					
				case 4:
					af = new NACA64ASeries(CLi, thickness, chord);
					break;

				case 5:
					af = new NACA65ASeries(CLi, thickness, chord);
					break;
					
				default:
					break;
			}
			
		} else {
			//	We have a 6 series airfoil.
			
			//	Get the design lift coefficient.
			double CLi = Integer.parseInt(designation.substring(2+dashOffset,3+dashOffset));
			CLi /= 10.;
			
			//	Get the t/c.
			double thickness = Integer.parseInt(designation.substring(3+dashOffset));
			thickness /= 100.;
			
			switch (profile) {
				case 3:
					af = new NACA63Series(CLi, thickness, chord);
					break;
					
				case 4:
					af = new NACA64Series(CLi, thickness, chord);
					break;

				case 5:
					af = new NACA65Series(CLi, thickness, chord);
					break;
				
				case 6:
					af = new NACA66Series(CLi, thickness, chord);
					break;
				
				case 7:
					af = new NACA67Series(CLi, thickness, chord);
					break;

				default:
					break;
			}
			
		}

		return af;
	}
	
	/**
	*  A simple method to test this class.
	**/
	public static void main(String[] args) {
	
		System.out.println("Testing NACAFactory...");
		
		Airfoil af = NACAFactory.create("NACA 0012", 1);
		System.out.println("    NACA 4 Uncambered = " + af);
		
		af = NACAFactory.create(" 6409 ", 1);
		System.out.println("    NACA 4 Cambered = " + af);
		
		af = NACAFactory.create("NACA23012", 1);
		System.out.println("    NACA 5 digit unreflexed = " + af);
		
		af = NACAFactory.create("NACA23112", 1);
		System.out.println("    NACA 5 digit reflexed = " + af);
		
		af = NACAFactory.create("0009-34", 1);
		System.out.println("    Modified 4 digit uncambered = " + af);
		
		af = NACAFactory.create("2410-34", 1);
		System.out.println("    Modified 4 digit cambered = " + af);
		
		af = NACAFactory.create("16-021", 1);
		System.out.println("    16 series = " + af);
		
		af = NACAFactory.create("63-206", 1);
		System.out.println("    6 series = " + af);
		
		af = NACAFactory.create("63A212", 1);
		System.out.println("    6*A series = " + af);
		
		System.out.println("Done!");
	}
	
}

