/*
*   ControlAero3DOFTabular -- Tabular 3-DOF flight control aerodynamic model.
*   
*   Copyright (C) 2005-2006 by Joseph A. Huwaldt
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
package standaloneutils.aerotools.aero;

import standaloneutils.aerotools.tools.tables.FloatTable;

/**
*  A 3DOF aerodynamic model of flight control deflection that makes use of
*  data tabulated as a function of angle of attack, Mach number, and
*  control deflection angle.  No ground effect.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: October 5, 2005
*  @version June 9, 2006
**/
public class ControlAero3DOFTabular implements ControlAero3DOF {

	//  The tabulated data.
	private FloatTable DCL = null;
	private FloatTable DCD = null;
	private FloatTable DCM = null;
	
	//  The independent index for AOA.
	private int AOAi = 0;
	//  the independent index for Mach.
	private int Machi = 1;
	//  the independent index for def.
	private int Defi = 2;
	
	//  Storage space for independent variables.
	private float[] independents = new float[3];
	
	//  Used to transform from wind to body axes.
	private static final double D2R = Math.PI/180.;
	private float cosA = 1;
	private float sinA = 0;
	

	/**
	*  Construct a tabular 3-DOF aerodynamics control surface model.
	*
	*  @param liftTable The wind axis lift coefficient increment tabulated as a function of AOA, Mach, and Def.
	*  @param dragTable The wind axis drag coefficient increment tabulated as a function of AOA, Mach, and Def.
	*  @param pmTable   The wind axis pitching moment coef. increment tabulated as a fn. of AOA, Mach, and Def.
	*  @param AOAName   The name of the independent variable that represents AOA in degrees.
	*  @param MachName  The name of the independent variable that represents Mach number.
	*  @param DefName   The name of the independent variable that represents control deflection angle.
	**/
	public ControlAero3DOFTabular(FloatTable liftTable, FloatTable dragTable,
							FloatTable pmTable, String AOAName, String MachName, String DefName) throws IllegalArgumentException {
		
		if (liftTable.dimensions() != 3 || dragTable.dimensions() != 3 || pmTable.dimensions() != 3)
			throw new IllegalArgumentException("Aerodynamic coefficient tables must have 3 independent variables!");
		
		this.DCL = liftTable;
		this.DCD = dragTable;
		this.DCM = pmTable;
		
		//  Determine which independent is AOA, which is Mach number, and which is deflection angle.
		String[] names = liftTable.getIndepNames();
		if (names[0].equals(AOAName)) {
			AOAi = 0;
			
			if (names[1].equals(MachName)) {
				Machi = 1;
				Defi = 2;
			} else if (names[1].equals(DefName)) {
				Defi = 1;
				Machi = 2;
			} else
				throw new IllegalArgumentException("Supplied independent parameter names not found in tables!");
			
		} else if (names[0].equals(MachName)) {
			Machi = 0;
			
			if (names[1].equals(AOAName)) {
				AOAi = 1;
				Defi = 2;
			} else if (names[1].equals(DefName)) {
				Defi = 1;
				AOAi = 2;
			} else
				throw new IllegalArgumentException("Supplied independent parameter names not found in tables!");
			
		} else if (names[0].equals(DefName)) {
			Defi = 0;
			
			if (names[1].equals(AOAName)) {
				AOAi = 1;
				Machi = 2;
			} else if (names[1].equals(MachName)) {
				Machi = 1;
				AOAi = 2;
			} else
				throw new IllegalArgumentException("Supplied independent parameter names not found in tables!");
			
		} else
			throw new IllegalArgumentException("Supplied independent parameter names not found in tables!");
		
		//  Check to make sure other tables have same independents.
		names = dragTable.getIndepNames();
		if (!names[AOAi].equals(AOAName) || !names[Machi].equals(MachName) || !names[Defi].equals(DefName))
			throw new IllegalArgumentException("Drag table's independent variables are not the same as the lift table's.");
			
		names = pmTable.getIndepNames();
		if (!names[AOAi].equals(AOAName) || !names[Machi].equals(MachName) || !names[Defi].equals(DefName))
			throw new IllegalArgumentException("PM table's independent variables are not the same as the lift table's.");
	}
	
	/**
	*  Method that sets the angle of attack in degrees.  The
	*  angle of attack indepenent variable in the table must be defined
	*  in degrees.
	*
	*  @param  AOA  Angle of attack in degrees.
	**/
	public void setAOA(float AOA) {
		independents[AOAi] = AOA;
		AOA *= D2R;
		cosA = (float)Math.cos(AOA);
		sinA = (float)Math.sin(AOA);
	}
	
	/**
	*  Returns the current angle of attack value.
	*
	*  @return  Angle of attack in degrees.
	**/
	public float getAOA() {
		return independents[AOAi];
	}
	
	/**
	*  Returns the minimum AOA breakpoint in the table in degrees.
	**/
	public float getMinAOA() {
		return DCL.getBreakpoint(AOAi, 0);
	}
	
	/**
	*  Returns the maximum AOA breakpoint in the table in degrees.
	**/
	public float getMaxAOA() {
		int size = DCL.getNumBreakpoints(AOAi);
		return DCL.getBreakpoint(AOAi, size-1);
	}
	
	/**
	*  Method that sets the Mach number.
	*
	*  @param Mach  The Mach number.
	**/
	public void setMach(float Mach) {
		independents[Machi] = Mach;
	}
	
	/**
	*  Returns the current Mach number value.
	*
	*  @return  Mach number.
	**/
	public float getMach() {
		return independents[Machi];
	}
	
	/**
	*  Returns the minimum Mach breakpoint in the table.
	**/
	public float getMinMach() {
		return DCL.getBreakpoint(Machi, 0);
	}
	
	/**
	*  Returns the maximum Mach breakpoint in the table.
	**/
	public float getMaxMach() {
		int size = DCL.getNumBreakpoints(Machi);
		return DCL.getBreakpoint(Machi, size-1);
	}
	
	/**
	*  Sets the control deflection angle in degrees.  The deflection
	*  angle indepenent variable in the table must be defined
	*  in degrees.
	*
	*  @param  def  Deflection angle in degrees.
	**/
	public void setDef(float def) {
		independents[Defi] = def;
	}
	
	/**
	*  Returns the current control surface deflection angle in degrees.
	*
	*  @return  Deflection angle in degrees.
	**/
	public float getDef() {
		return independents[Defi];
	}
	
	/**
	*  Returns the minimum control deflection angle breakpoint in the table in degrees.
	**/
	public float getMinDef() {
		return DCL.getBreakpoint(Defi, 0);
	}
	
	/**
	*  Returns the maximum control deflection angle breakpoint in the table in degrees.
	**/
	public float getMaxDef() {
		int size = DCL.getNumBreakpoints(Defi);
		return DCL.getBreakpoint(Defi, size-1);
	}
	
	/**
	*  Sets the height above ground level (for ground effect modeling).
	*  This method is ignored by this implementation which does not
	*  account for ground effect.
	*
	*  @param  hob  The height to the aero reference point
	*               divided by the reference span "bref".
	**/
	public void setHOB(float hob) { }
	
	/**
	*  Returns the wind axis lift coefficient increment due to control deflection.
	*  WARNING:  This method will extrapolate beyond
	*  the edges of the table if AOA or Mach exceed table limits!
	**/
	public float getDCL() {
		return DCL.lookup(independents);
	}
	
	/**
	*  Method that returns the wind axis drag coefficient increment due to control deflection.
	*  WARNING:  This method will extrapolate beyond
	*  the edges of the table if AOA or Mach exceed table limits!
	**/
	public float getDCD() {
		return DCD.lookup(independents);
	}
	
	/**
	*  Method that returns the wind axis pitching moment coefficient increment due to control deflection.
	*  WARNING:  This method will extrapolate beyond
	*  the edges of the table if AOA or Mach exceed table limits!
	**/
	public float getDCM() {
		return DCM.lookup(independents);
	}
	
	/**
	*  Returns the body axis normal force coefficient increment due to control deflection.
	**/
	public float getDCN() {
		return getDCL()*cosA + getDCD()*sinA;
	}
	
	/**
	*  Method that returns the body axis axial force coefficient increment due to control deflection.
	**/
	public float getDCA() {
		return getDCD()*cosA - getDCL()*sinA;
	}
	
	/**
	*  Method that returns the body axis pitching moment coefficient increment due to control deflection.
	*  (Which should be identical to the wind-axis pitching moment for a
	*  3-DOF model with no sideslip).
	**/
	public float getDCMb() {
		return getDCM();
	}
	
	/**
	*  Return a string representation of this object.
	**/
	public String toString() {
		StringBuffer buf = new StringBuffer(DCL.toString());
		buf.append(", ");
		buf.append(DCD.toString());
		buf.append(", ");
		buf.append(DCM.toString());
		return buf.toString();
	}
	
}
