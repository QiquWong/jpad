/*
*   ControlAero3DOF -- Interface for 3-DOF Static flight control aerodynamic models.
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


/**
*  Interface in common to all 3-DOF static flight control aerodynamics models.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: October 5, 2005
*  @version June 6, 2006
**/
public interface ControlAero3DOF {

	/**
	*  Method that sets the angle of attack in degrees.
	*
	*  @param  AOA  Angle of attack in degrees.
	**/
	public void setAOA(float AOA);
	
	/**
	*  Method that sets the Mach number.
	*
	*  @param Mach  The Mach number.
	**/
	public void setMach(float Mach);
	
	/**
	*  Sets the control deflection angle in degrees
	*
	*  @param  def  Deflection angle in degrees.
	**/
	public void setDef(float def);
	
	/**
	*  Sets the height above ground level (for ground effect modeling).
	*
	*  @param  hob  The height to the aero reference point
	*               divided by the reference span "bref".
	**/
	public void setHOB(float hob);
	
	/**
	*  Returns the wind axis lift coefficient increment due to control deflection.
	**/
	public float getDCL();
	
	/**
	*  Method that returns the wind axis drag coefficient increment due to control deflection.
	**/
	public float getDCD();
	
	/**
	*  Method that returns the wind axis pitching moment coefficient increment due to control deflection.
	**/
	public float getDCM();
	
	/**
	*  Returns the body axis normal force coefficient increment due to control deflection.
	**/
	public float getDCN();
	
	/**
	*  Method that returns the body axis axial force coefficient increment due to control deflection.
	**/
	public float getDCA();
	
	/**
	*  Method that returns the body axis pitching moment coefficient increment due to control deflection.
	*  (Which should be identical to the wind-axis pitching moment increment for a
	*  3-DOF model with no sideslip).
	**/
	public float getDCMb();
	
}
