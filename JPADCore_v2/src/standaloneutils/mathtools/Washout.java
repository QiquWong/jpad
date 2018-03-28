/*
*   Washout  -- A high pass washout digital filter implementation
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
*  Performs a digital high pass filter.  The Tustin approximation is used to
*  compute the dynamics of the washout.  The washout filter algorithm has an
*  output hold mode and an I.C. mode.   The I.C. mode overrides all other modes.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  August 26, 2003
*  @version August 27, 2003
**/
public class Washout implements DigitalFilter {
	
	private float prevInput = Float.NaN;
	private float prevOutput = Float.NaN;
	private float n1 = Float.NaN, n2 = Float.NaN;
	private boolean icReset = true;


	/**
	*  Performs a digital high pass filter.  The Tustin approximation is used to
	*  compute the dynamics of the washout.  The washout filter algorithm has an
	*  output hold mode and an I.C. mode.   The I.C. mode overrides all other modes.
  	*  By default, the previous values for input and output are set to the current
	*  input value on the first entry into this method. This can be overridden by
	*  calling setPreviousInput() and setPreviousOutput() before calling this
	*  method for the first time.
	*
	*  @param  input  Current input value to be lagged.
	*  @param  gain   Steady state gain.
	*  @param  hold   If true, the filter will hold the current output value until
	*                 this value becomes false again.
	*  @param  IC     If true, the filter will initialize on the ICValue value;
	*                 setting the output value to the ICValue value.
	*  @param  ICValue The value the filter is to be ICed to when the "IC" flag is true.
	*
	*  @return A digitally washed out value.
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
				output = n1*output + n2*gain*(input - prevInput);
		}

		prevInput = input;
		prevOutput = output;
		
		return output;
	}


	/**
	*  Sets the washout time constant N1.  This value should be calculated
	*  using "calcN1()", but could be something else if you know
	*  what you are doing.
	**/
	public void setN1(float value) {
		n1 = value;
	}

	/**
	*  Sets the washout time constant N2.  This value should be calculated
	*  using "calcN2()", but could be something else if you know
	*  what you are doing.
	**/
	public void setN2(float value) {
		n2 = value;
	}

	/**
	*  The washout time constant N1 should be calculated as follows:
	*    n1 = exp(-T/tau).
	*  For example:  tau = 0.75, T = 0.016, then n1 = 0.9789.
	*  This method carries out this calculation as a convenience for
	*  the user.
	*
	*  @param  T   The time between sample periods.
	*  @param  tau The filter time constant.
	*  @return  The washout time contstant N1 to be used.
	**/
	public static final float calcN1(float T, float tau) {
		float output = (float)Math.exp(-T/tau);
		return output;
	}

	/**
	*  The washout time constant N2 should be calculated as follows:
	*    n2 = (1 + n1)/2.
	*  For example:  tau = 0.75, T = 0.016, then n2 = 0.9894.
	*  This method carries out this calculation as a convenience for
	*  the user.
	*
	*  @param  T   The time between sample periods.
	*  @param  tau The filter time constant.
	*  @return  The washout time contstant N2 to be used.
	**/
	public static final float calcN2(float T, float tau) {
		float n1 = calcN1(T, tau);
		float output = (1 + n1)/2;
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
		System.out.println("Testing Washout...");

		//	Set "simulated" sample period (seconds).
		float T = 0.016F;

		//	Set the time constant for the response.
		float tau = 0.75F;

		//	Compute the filter time constants.
		float n1 = Washout.calcN1(T, tau);
		float n2 = Washout.calcN2(T, tau);

		//	Set a filter gain.
		float gain = 0.95F;
		
		//	Create a filter object.
		Washout washout = new Washout();
		washout.setN1(n1);
		washout.setN2(n2);
		
		//	Set the previous input/output values of the filter.
		washout.setPreviousInput(0);
		washout.setPreviousOutput(0);

		//	Run two "seconds" worth of data.
		int count = Math.round(2F/T);
		float output = 0;
		System.out.println("      t = 0,\toutput = " + output);
		for (int i=0; i < count; ++i) {
			output = washout.filter(1F, gain, false, false, 0);
			if (i%10 == 0)
				System.out.println("      t = " + ((i+1)*T) + ",\toutput = " + output);
		}
		System.out.println("      t = " + ((count+1)*T) + ",\toutput = " + output);

		System.out.println("    n1 = " + n1 + ", should be = 0.9789");
		System.out.println("    n2 = " + n2 + ", should be = 0.9894");
		System.out.println("    output at t=2 sec = " + output);
		System.out.println("Done testing Washout.");
	}
}
