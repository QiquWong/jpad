/*
*   ControlHMLinearizedTabular -- Tabular flight control aerodynamic hinge-moment model using a linearized component build-up.
*   
*   Copyright (C) 2006 by Joseph A. Huwaldt
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
*  A model of flight control surface aerodynamic hinge-moments that
*  makes use of a linearized component build-up where each component is data
*  tabulated as a function of Mach number.  No ground effect.  <br>
*  The buildup is as follows:  Ch = Cho + Cha*alpha + Chd*def  <br>
*  Where Cho, Cha and Chd are all functions of Mach number.  Cha is
*  the change in hinge-moment with a change in angle-of-attack in
*  units of (1/deg) and Chd is the change in hinge-moment with a
*  change in control surface deflection angle in units of (1/deg).
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: June 9, 2006
*  @version June 9, 2006
**/
public class ControlHMLinearizedTabular implements ControlHingeMoments {

	//  The tabulated data.
	private FloatTable _Chot = null;
	private FloatTable _Chat = null;
	private FloatTable _Chdt = null;
	
	//  The input values.
	private float _Mach = 0;			//  Mach number.
	private float _alpha = 0;		//  Angle of attack in degrees.
	private float _delta = 0;		//  Control surface deflection angle in degrees.
	

	/**
	*  Construct a linearized, tabular control surface aerodynamic hinge-moment model.
	*
	*  @param ChoTable  The zero AOA, zero deflection hinge-moment coefficient as a function of Mach.
	*  @param ChaTable  The change in hinge-moment due to a change in AOA as a function of Mach.
	*  @param ChdTable  The change in hinge-moment due to a change in deflection angle as a function of Mach.
	*  @param MachName  The name of the independent variable that represents Mach number.
	**/
	public ControlHMLinearizedTabular(FloatTable ChoTable, FloatTable ChaTable,
							FloatTable ChdTable, String MachName) throws IllegalArgumentException {
		
		if (ChoTable.dimensions() != 1 || ChaTable.dimensions() != 1 || ChdTable.dimensions() != 1)
			throw new IllegalArgumentException("Hinge-moment coefficient tables must have 1 independent variable (Mach)!");
		
		this._Chot = ChoTable;
		this._Chat = ChaTable;
		this._Chdt = ChdTable;
		
		
		//  Check to make sure the tables have same independents.
		String name = ChoTable.getIndepName(0);
		if (!name.equals(MachName))
			throw new IllegalArgumentException("The \"" + ChoTable.getTableName() +
								"\" table's independent variable is not named \"" + MachName + "\".");
			
		name = ChaTable.getIndepName(0);
		if (!name.equals(MachName))
			throw new IllegalArgumentException("The \"" + ChaTable.getTableName() +
								"\" table's independent variable is not named \"" + MachName + "\".");

		name = ChdTable.getIndepName(0);
		if (!name.equals(MachName))
			throw new IllegalArgumentException("The \"" + ChdTable.getTableName() +
								"\" table's independent variable is not named \"" + MachName + "\".");
	}
	
	/**
	*  Method that sets the angle of attack in degrees.
	*
	*  @param  AOA  Angle of attack in degrees.
	**/
	public void setAOA(float AOA) {
		_alpha = AOA;
	}
	
	/**
	*  Returns the current angle of attack value.
	*
	*  @return  Angle of attack in degrees.
	**/
	public float getAOA() {
		return _alpha;
	}
	
	/**
	*  Method that sets the Mach number.
	*
	*  @param Mach  The Mach number.
	**/
	public void setMach(float Mach) {
		_Mach = Mach;
	}
	
	/**
	*  Returns the current Mach number value.
	*
	*  @return  Mach number.
	**/
	public float getMach() {
		return _Mach;
	}
	
	/**
	*  Returns the minimum Mach breakpoint in the table.
	**/
	public float getMinMach() {
		return _Chot.getBreakpoint(0, 0);
	}
	
	/**
	*  Returns the maximum Mach breakpoint in the table.
	**/
	public float getMaxMach() {
		int size = _Chot.getNumBreakpoints(0);
		return _Chot.getBreakpoint(0, size-1);
	}
	
	/**
	*  Sets the control deflection angle in degrees.
	*
	*  @param  def  Deflection angle in degrees.
	**/
	public void setDef(float def) {
		_delta = def;
	}
	
	/**
	*  Returns the current control surface deflection angle in degrees.
	*
	*  @return  Deflection angle in degrees.
	**/
	public float getDef() {
		return _delta;
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
	*  Returns the non-dimensional hinge-moment coefficient: Ch = H.M. / (qbar*Sc*cc).
	*  Where H.M. is the dimensional hinge moment, qbar is the dynamic pressure, 
	*  Sc = the reference area of the control surface and cc = the reference
	*  chord length of the control surface. <br>
	*  WARNING:  This method will extrapolate beyond
	*  the edges of the table if Mach exceeds the table limits!
	**/
	public float getCh() {
		float Cho = _Chot.lookup(_Mach);
		float Cha = _Chat.lookup(_Mach);
		float Chd = _Chdt.lookup(_Mach);
		
		float Ch = Cho + Cha*_alpha + Chd*_delta;
		
		return Ch;
	}
	
	
	/**
	*  Return a string representation of this object.
	**/
	public String toString() {
		StringBuffer buf = new StringBuffer(_Chot.toString());
		buf.append(", ");
		buf.append(_Chat.toString());
		buf.append(", ");
		buf.append(_Chdt.toString());
		return buf.toString();
	}
	
}
