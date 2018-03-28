/*
*   FirstOrderLag  -- A first-order lag digital filter implementation
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
*  Performs a digital lag filter using an F(Z) first order equation.  The algorithm
*  uses the Tustin approximation method to calculate the output variable.  The first
*  order lag filter algorithm has an output hold mode and an I.C. mode.   The I.C.
*  mode overrides all other modes.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  August 25, 2003
*  @version November 19, 2004
**/
public class FirstOrderLag implements DigitalFilter {
	
	private static final float kOneHalf = 0.5F;

	private float prevInput = Float.NaN;
	private float prevOutput = Float.NaN;
	private boolean icReset = true;


	/**
	*  Performs a digital lag filter using an F(Z) first order equation.  The algorithm
	*  uses the Tustin approximation method to calculate the output variable.  The first
	*  order lag filter algorithm has an output hold mode and an I.C. mode.   The I.C.
	*  mode overrides all other modes.  By default, the previous values for input and
	*  output are set to the current input value on the first entry into this method.
	*  This can be overridden by calling setPreviousInput() and setPreviousOutput()
	*  before calling this method for the first time.
	*
	*  @param  input  Current input value to be lagged.
	*  @param  gain   Filter gain constant as calculated by "calcGainConstant()".
	*  @param  hold   If true, the filter will hold the current output value until
	*                 this value becomes false again.
	*  @param  IC     If true, the filter will initialize on the ICValue value;
	*                 setting the output value to the ICValue value.
	*  @param  ICValue The value the filter is to be ICed to when the "IC" flag is true.
	*
	*  @return A digitally lagged value using an F(Z) first order equation.
	* */
	public float filter(float input, float gain, boolean hold, boolean IC, float ICValue) {

		float output = prevOutput;

		//	Initial condition mode overrides other modes.
		if (IC) {
			icReset = false;
			output = ICValue;

		} else {
			if (icReset) {
				icReset = false;
				
				//	If previous output value has not been set, set it to current input.
				if (Float.isNaN(prevOutput)) {
					prevOutput = input;
					output = input;
				}
				
				//	If previous input value has not been set, set it to current input.
				if (Float.isNaN(prevInput))
					prevInput = input;

			}
			
			if (!hold)
				output = ((input + prevInput)*kOneHalf - output)*gain + output;
		}

		prevInput = input;
		prevOutput = output;
		
		return output;
	}

	
	/**
	*  The first order filter gain should be calculated as follows:
	*    gain = (2*tan(T/(2*tau)))/(1 + tan(T/(2*tau))).
	*  For example:  tau = 0.75, T = 0.016, then gain = 0.021109.
	*  This method carries out this calculation as a convenience for
	*  the user.
	*
	*  @param  T   The time between sample periods.
	*  @param  tau The filter time constant.
	*  @return  The filter gain to be used.
	**/
	public static final float calcFilterGain(float T, float tau) {
		float tanv = (float)Math.tan(T/(2*tau));
		float output = 2*tanv/(1 + tanv);
		return output;
	}


	/**
	*  Sets the previous input value used by this filter.
	**/
	public void setPreviousInput(float value) {
		prevInput = value;
	}

	/**
	*  Sets the previous output value used by this filter.
	**/
	public void setPreviousOutput(float value) {
		prevOutput = value;
	}


	/**
	*  Method used to test this class.
	**/
	public static void main(String[] args) {
		System.out.println("Testing FirstOrderLag...");

		//	Set "simulated" sample period (seconds).
		float T = 0.016F;

		//	Set the time constant for the response.
		float tau = 0.75F;

		//	Compute the filter gain.
		float gain = FirstOrderLag.calcFilterGain(T, tau);
		
		//	Create a filter object.
		DigitalFilter lag = new FirstOrderLag();
		
		//	Set the previous input/output values of the filter.
		lag.setPreviousInput(0);
		lag.setPreviousOutput(0);

		//	Run two "seconds" worth of data.
		int count = Math.round(2F/T);
		float output = 0;
		for (int i=0; i < count; ++i)
			output = lag.filter(1F, gain, false, false, 0);

		System.out.println("    gain = " + gain + ", should be = 0.021109");
		System.out.println("    output at t = " + ((count+1)*T) + " sec = " + output);
		System.out.println("    ideal output is = " + (float)(1 - Math.exp(-2/tau)));
		System.out.println("Done testing FirstOrderLag.");
	}
}
