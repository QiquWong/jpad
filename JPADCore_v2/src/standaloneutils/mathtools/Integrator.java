/*
*   Integrator  -- An integrator digital filter implementation.
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
*  Performs a digital integrator using an F(Z) first order equation.  The algorithm
*  uses the Tustin approximation method to calculate the output variable.  The
*  integrator algorithm has an output hold mode and an I.C. mode.   The I.C.
*  mode overrides all other modes.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  August 27, 2003
*  @version November 19, 2004
**/
public class Integrator implements DigitalFilter {
	
	private static final float kOneHalf = 0.5F;

	private float prevInput = Float.NaN;
	private float prevOutput = Float.NaN;
	private boolean icReset = true;
	private float UL = Float.MAX_VALUE;
	private float LL = -UL;
	

	/**
	*  Performs a digital integrator using an F(Z) first order equation.  The algorithm
	*  uses the Tustin approximation method to calculate the output variable.  The
	*  intregrator algorithm has an output hold mode and an I.C. mode.   The I.C.
	*  mode overrides all other modes.  By default, the previous value for input is
	*  set to the current input and the previous value for output is set to zero
	*  on the first entry into this method. This can be overridden by calling
	*  setPreviousInput() and setPreviousOutput() before calling this method for the
	*  first time.
	*
	*  @param  input  Current input value to be integrated.
	*  @param  gain   Integration time constant as calculated by "calcFilterGain()".
	*  @param  hold   If true, the filter will hold the current output value until
	*                 this value becomes false again.
	*  @param  IC     If true, the filter will initialize on the ICValue value;
	*                 setting the output value to the ICValue value.
	*  @param  ICValue The value the filter is to be ICed to when the "IC" flag is true.
	*
	*  @return A digitally integrated value using an F(Z) first order equation.
	* */
	public float filter(float input, float gain, boolean hold, boolean IC, float ICValue) {

		float output = prevOutput;

		//	If initial entry following construction.
		if (icReset) {
			icReset = false;
			if (Float.isNaN(prevInput))
				prevInput = input;
			if (Float.isNaN(prevOutput))
				output = 0;
		}
		
		if (IC)
			output = ICValue;

		else if (!hold)
			output = (input + prevInput)*kOneHalf*gain + output;

		//	Limit output value to the specified range.
		if (output > UL)
			output = UL;
		else if (output < LL)
			output = LL;

		prevInput = input;
		prevOutput = output;
		
		return output;
	}

	
	/**
	*  The integrator gain should be calculated as follows:
	*    gain = k*T.
	*  For example:  k = 2.0, T = 0.016, then gain = 0.032.
	*  This method carries out this calculation as a convenience for
	*  the user.
	*
	*  @param  T   The time between sample periods.
	*  @param  k   The integration gain.
	*  @return  The integrator time constant to be used.
	**/
	public static final float calcFilterGain(float T, float k) {
		float output = k*T;
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
	*  Sets the upper limit for the integrator.
	**/
	public void setUpperLimit(float value) {
		UL = value;
	}

	/**
	*  Sets the lower limit for the integrator.
	**/
	public void setLowerLimit(float value) {
		LL = value;
	}


	/**
	*  Method used to test this class.
	**/
	public static void main(String[] args) {
		System.out.println("Testing Integrator...");

		//	Set "simulated" sample period (seconds).
		float T = 0.016F;

		//	Set the integrator gain for the response.
		float k = 2;

		//	Compute the filter gain.
		float gain = Integrator.calcFilterGain(T, k);
		
		//	Create a filter object.
		Integrator integrator = new Integrator();
		
		//	Set upper and lower integration limits.
		integrator.setUpperLimit(2);
		integrator.setLowerLimit(-2);

		//	Run two "seconds" worth of data.
		int count = Math.round(2F/T);
		float output = 0;
		System.out.println("      t = 0,\toutput = " + output);
		for (int i=0; i < count; ++i) {
			output = integrator.filter(1F, gain, false, false, 0);
			if (i%10 == 0)
				System.out.println("      t = " + ((i+1)*T) + ",\toutput = " + output);
		}
		System.out.println("      t = " + ((count+1)*T) + ",\toutput = " + output);

		System.out.println("    gain = " + gain + ", should be = 0.032");
		System.out.println("    output at t = " + ((count+1)*T) + " sec = " + output);
		System.out.println("Done testing Integrator.");
	}
}
