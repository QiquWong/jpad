/*
 *   StdAtmos -- Abstract class providing interface for standard atmosphere models.
 *   
 *   Copyright (C) 1999-2014 by Joseph A. Huwaldt
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
 */
package standaloneutils.aerotools.aero;



/**
*  An abstract class that provides standard functionality
*  for all atmosphere models that sub-class off of this
*  one.  Sub-classes must provide a method for calculating
*  the atmosphere properties of density ratio, temperature
*  ratio and pressure ratio.  Also methods must be provided
*  that simply return the values of temperature, pressure,
*  density and speed of sound at sea-level/reference conditions.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt  Date:	September 27, 1998
*  @version April 1, 2014
*/
public abstract class StdAtmos implements java.io.Serializable {

	/*
	 * 	The geometric altitude in meters.
	 */
	protected  double alt = 0;

	/*
	 * 	The density at altitude / sea-level standard density.
	 */
	protected  double sigma = 1;

	/*
	 * 	The pressure at altitude / sea-level standard pressure.
	 */
	protected  double delta = 1;

	/*
	 * 	The temperature at altitude / sea-level standard temperature.
	 */
	protected  double theta = 1;
	
	/*
	 * 	The speed of sound at altitude / sea-level standard speed of sound.
	 */
	protected double cs = 1;

	//-----------------------------------------------------------------------------------
	/**
	*  Get geometric altitude currently being used for standard
	*  atmosphere calculations.
	*
	*  @return Returns altitude where the standard atmosphere is
	*          being calculated in meters.
	*/
	public final double getAltitude() {
		return alt;
	}

	/**
	*  Get the density at altitude divided by the sea-level
	*  standard density.
	*
	*  @return Returns the density ratio at altitude.
	*/
	public final double getDensityRatio() {
		return sigma;
	}

	/**
	*  Get the pressure at altitude divided by the sea-level
	*  standard pressure.
	*
	*  @return Returns the pressure ratio at altitude.
	*/
	public final double getPressureRatio() {
		return delta;
	}

	/**
	*  Get the temperature at altitude divided by the sea-level
	*  standard temperature.
	*
	*  @return Returns the temperature ratio at altitude.
	*/
	public final double getTemperatureRatio() {
		return theta;
	}

	/**
	*  Get the speed of sound at altitude divided by the sea-level
	*  standard speed of sound.
	*
	*  @return Returns the speed of sound ratio at altitude.
	*/
	public final double getSpeedOfSoundRatio() {
		return cs;
	}

	/**
	*  Get the static air temperature at altitude.
	*
	*  @return Returns the temperature at altitude in units of deg. K.
	*/
	public final double getTemperature() {
		return T0()*theta;
	}

	/**
	*  Get the static air pressure at altitude.
	*
	*  @return Returns the pressure at altitude in units of N/m^2.
	*/
	public final double getPressure() {
		return P0()*delta;
	}

	/**
	*  Get the static air density at altitude.
	*
	*  @return Returns the density at altitude in units of g/cm^3.
	*/
	public final double getDensity() {
		return RHO0()*sigma;
	}

	/**
	*  Get the speed of sound at altitude.
	*
	*  @return Returns the speed of sound at altitude in units of m/s.
	*/
	public final double getSpeedOfSound() {
		return a0()*cs;
	}

	/**
	*  Creates a String representation of this object.
	*
	*  @return The String representation of this StdAtmos object.
	*/
    @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("h = ");
		buffer.append(alt);
		buffer.append(" meters, DensityR = ");
		buffer.append(sigma);
		buffer.append(", PressureR = ");
		buffer.append(delta);
		buffer.append(", TemperatureR = ");
		buffer.append(theta);
		return buffer.toString();
	}

	//-----------------------------------------------------------------------------------
	/**
	*  Returns the standard sea level temperature for this
	*  atmosphere model.  Value returned in Kelvins.
	*
	*  @return Returns the standard sea level temperature in K.
	*/
	public abstract double T0();

	/**
	*  Returns the standard sea level pressure for this
	*  atmosphere model.  Value returned in Newtons/m^2.
	*
	*  @return Returns the standard sea level pressure in N/m^2.
	*/
	public abstract double P0();

	/**
	*  Returns the standard sea level density for this
	*  atmosphere model.  Value returned in kg/L (g/cm^3).
	*
	*  @return Returns the standard sea level density in kg/L
	*          (g/cm^3).
	*/
	public abstract double RHO0();

	/**
	*  Returns the standard sea level speed of sound for this
	*  atmosphere model.  Value returned in meters/sec.
	*
	*  @return Returns the standard sea level density in m/s.
	*/
	public abstract double a0();

	/**
	*  Returns the minimum altitude supported by this
	*  atmosphere model.  Sub-classes should return the
	*  minimum altitude supported the the sub-class'
	*  atmosphere model.
	*
	*  @return Returns the minimum altitude supported by this
	*          atmosphere model.
	*/
	public abstract double minAltitude();

	/**
	*  Returns the maximum altitude supported by this
	*  atmosphere model.  Sub-classes should return the
	*  maximum altitude supported the the sub-class'
	*  atmosphere model.
	*
	*  @return Returns the maximum altitude supported by this
	*          atmosphere model.
	*/
	public abstract double maxAltitude();

	/**
	*  Sets the geometric altitude where the standard
	*  atmosphere is to be calculated.
	*
	*  @param  altitude Geometric altitude at which standard atmosphere is
	*          to be calculated (in meters).
	*/
	public abstract void setAltitude( double altitude ) throws IllegalArgumentException;

}


