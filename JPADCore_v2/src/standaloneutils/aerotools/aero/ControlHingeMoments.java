/*
*   ControlHingeMoments -- Interface for aerodynamic flight control hinge-moment models.
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


/**
*  Interface in common to all aerodynamic flight control hinge-moment models.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: June 9, 2006
*  @version June 9, 2006
**/
public interface ControlHingeMoments {

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
	*  Sets the control deflection angle in degrees.
	*
	*  @param  def  Deflection angle.
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
	*  Returns the non-dimensional hinge-moment coefficient: Ch = H.M. / (qbar*Sc*cc).
	*  Where H.M. is the dimensional hinge moment, qbar is the dynamic pressure, 
	*  Sc = the reference area of the control surface and cc = the reference
	*  chord length of the control surface.
	**/
	public float getCh();
		
}
