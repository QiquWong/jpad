/*
*   DigitalFilter  -- Interface for digital filters used in control system modeling.
*
*   Copyright (C) 2003-2004 by Joseph A. Huwaldt.
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
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
package standaloneutils.mathtools;

/**
*  Interface in common to all digital filters.  These are digital filters
*  that are typically used in control system simulations.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  August 25, 2003
*  @version November 19, 2004
**/
public interface DigitalFilter {

	/**
	*  Performs a digital filter calculation.  The filter algorithm has an output hold mode
	*  and an I.C. mode.   The I.C. mode overrides all other modes.
	*
	*  @param  input  Current input value to be filtered.
	*  @param  gain   Filter gain constant.
	*  @param  hold   If true, the filter will hold the current output value until
	*                 this value becomes false again.
	*  @param  IC     If true, the filter will initialize on the ICValue value;
	*                 setting the output value to the ICValue value.
	*  @param  ICValue The value the filter is to be ICed to when the "IC" flag is true.
	*
	*  @return A digitally filtered value.
	* */
	public float filter(float input, float gain, boolean hold, boolean IC, float ICValue);
	

	/**
	*  Sets the previous input value used by this filter.
	**/
	public void setPreviousInput(float value);

	/**
	*  Sets the previous output value used by this filter.
	**/
	public void setPreviousOutput(float value);


}
