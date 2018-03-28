/*
 *   StdAtmos1962 -- Calculates the properties of the U.S. 1962 Standard Atmosphere.
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
*  This class provides methods for calculating the atmospheric
*  properties of the U.S. Standard Atmosphere, 1962, assuming
*  an inverse square gravitational field.  This assumption
*  yields data that agrees with the COESA document within 1%
*  at all altitudes up to 700,000 meters (434.96 miles).
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt  Date:	July 10, 1999
*  @version April 1, 2014
*/
public final class StdAtmos1962 extends StdAtmos {

	//	Standard gravitational acceleration ft/s^2.
	private static final double g0 = 32.1740484;

	//	Ratio of specific heats of air.
	private static final double GAMMA = 1.4;

	// Molecular weight.
	private static final double WM0 = 28.9644;

	//	Radius of the Earth (ft).
	private static final double REARTH = 20890855.;

	//	Gas constant (deg R/ft).
	private static final double GMR = 0.018743418;

	//	Standard sea level pressure in Newtons/m^2 (101325).
	private static final double kP0 = 2116.2165/0.09290304*4.448222;

	//	Standard sea level temperature in deg. Kelvin (288.15).
	private static final double kT0 = 518.67*5./9.;

	//	Standard sea level density in kg/L (g/cm^3 -- 0.00122501).
	private static final double kRHO0=0.00237691/0.3048*9.80665/28.316846592*0.45359237;

	//	Standard sea level speed of sound in m/s (340.292036).
	private static final double ka0 = 661.4748/3600*1852;

	//	1962 Standard Atmosphere tables (altitude in ft, temperature in deg. R, pressure in lb/ft^2).
	private static final double[] htab = {   -16404, 0, 36089, 65617, 104987,
                                            154199, 170604, 200131, 250186, 291160 };

	private static final double[] ZM = { 295276, 328084, 360892, 393701, 492126,
					                    524934, 557743, 623360, 754593, 984252,
					                    1312336, 1640420, 1968504, 2296588 };

	private static final double[] WM = { 28.9644, 28.88, 28.56, 28.07, 26.92,
					                    26.66, 26.4, 25.85, 24.7, 22.66,
					                    19.94, 17.94, 16.84, 16.17 };

	private static final double[] TM = { 577.17, 518.67, 389.97, 389.97, 411.57,
					                    487.17, 487.17, 454.77, 325.17, 325.17,
					                    379.17, 469.17, 649.17, 1729.17, 1999.17,
					                    2179.17, 2431.17, 2791.17, 3295.17, 3889.17,
					                    4357.17, 4663.17, 4861.17 };

	private static final double[] PM = { 3711.0839, 2116.2165, 472.67563, 114.34314,
					                    18.128355, 2.3162178, 1.2321972, 3.8030279E-01,
					                    2.1671352E-02, 3.4313478E-03, 6.2773411E-04, 1.5349091E-04,
					                    5.2624212E-05, 1.0561806E-05, 7.7083076E-06, 5.8267151E-06,
					                    3.5159854E-06, 1.4520255E-06, 3.9290563E-07, 8.4030242E-08,
					                    2.2835256E-08, 7.1875452E-09 };

	//-----------------------------------------------------------------------------------
	/**
	*  Constructor that assumes an altitude of 0 meters,
	*/
	public StdAtmos1962() {
		super();
	}

    /**
     * Constructor taking a value of geometric altitude in meters. If the specified
     * altitude is outside the range -5000 to 90,000 meters, an IllegalArgumentException
     * exception is thrown.
     *
     * @param altitude The geometric altitude above MSL at which the standard atmosphere
     * is to be calculated; value given in meters.
     */
	public StdAtmos1962( double altitude ) throws IllegalArgumentException {

		//	Set the altitude (if out of range, throw exception).
		if ( altitude < - 5000 )
			throw new IllegalArgumentException( "Altitude can not be less than -5 km." );
		
		else {
			if ( altitude > 700000 )
				throw new IllegalArgumentException( "Altitude can not be greater than 700 km." );
			
			else {
				//	Reset the altitude only if it has changed.
				if ( altitude != alt ) {
					alt = altitude;
					
					//	Go off and calculate the standard atmosphere parameters.
					calculate();
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------
	/**
	*  Returns the standard sea level temperature for this
	*  atmosphere model.  Value returned in Kelvins.
	*
	*  @return Returns the standard sea level temperature in K.
	*/
    @Override
	public final double T0() {
		return kT0;
	}

	/**
	*  Returns the standard sea level pressure for this
	*  atmosphere model.  Value returned in Newtons/m^2.
	*
	*  @return Returns the standard sea level pressure in N/m^2.
	*/
    @Override
	public final double P0() {
		return kP0;
	}

	/**
	*  Returns the standard sea level density for this
	*  atmosphere model.  Value returned in kg/L (g/cm^3).
	*
	*  @return Returns the standard sea level density in kg/L
	*          (g/cm^3).
	*/
    @Override
	public final double RHO0() {
		return kRHO0;
	}

	/**
	*  Returns the standard sea level speed of sound for this
	*  atmosphere model.  Value returned in meters/sec.
	*
	*  @return Returns the standard sea level density in m/s.
	*/
    @Override
	public final double a0() {
		return ka0;
	}

	/**
	*  Returns the minimum altitude supported by this
	*  atmosphere model.
	*
	*  @return Returns the minimum altitude supported by this
	*          atmosphere model in meters.
	*/
    @Override
	public double minAltitude() {
		return -5000;       //  = -16404.*0.3048;
	}

	/**
	*  Returns the maximum altitude supported by this
	*  atmosphere model.
	*
	*  @return Returns the maximum altitude supported by this
	*          atmosphere model in meters.
	*/
    @Override
	public double maxAltitude() {
		return 700000;
	}

    /**
     * Sets the geometric altitude where the standard atmosphere is to be calculated.
     *
     * @param altitude The geometric altitude above MSL at which standard atmosphere is to
     * be calculated (in meters).
     */
    @Override
	public void setAltitude( double altitude ) throws IllegalArgumentException {
		//	Set the altitude (if out of range, throw exception).
		if ( altitude < - 5000 )
			throw new IllegalArgumentException( "Altitude can not be less than -5 km." );
		
		else {
			if ( altitude > 700000 )
				throw new IllegalArgumentException( "Altitude can not be greater than 700 km." );
			
			else {
				//	Reset the altitude only if it has changed.
				if ( altitude != alt ) {
					alt = altitude;
					
					//	Go off and calculate the standard atmosphere parameters.
					calculate();
				}
			}
		}
	}

	/**
	*  Calculates the properties of the US 1962 Standard Atmosphere.
	*/
	private void calculate() {
		//	Loop counters.
		int j = 0;
		
		//	Geometric and Geopotential altitude (ft).
		double Z, G;
		
		double TMS, EM;
		double P, Rho, T;
		
		// First convert the altitude from meters to feet.
		Z = alt/0.3048;
		
		G = REARTH/(REARTH + Z);
		G *= G*g0;
		
		if ( Z <= 295276 ) {
			// TMS linear with geopotential, calculate H and search.
			double H = REARTH*Z/(REARTH + Z);
			
			// Find region in geopotential table for this altitude.
			for ( int i = 1; i < htab.length; ++i ) {
				j = i - 1;
				if ( htab[i] > H )
					break;
			}
			
			// Calculate TMS slope, TMS, and set mol wt stuff.
			double ELH = (TM[j+1] - TM[j])/(htab[j+1] - htab[j]);
			TMS = TM[j] + ELH*(H - htab[j]);
//			ELZ = ELH*G/g0;
//			DMDZ = 0;
			EM = WM0;
			
			// Check TMS slope and calculate pressure.
			if ( ELH == 0 )
				// Zero slope pressure equation (lbs/ft^2).
				P = PM[j]*Math.exp( GMR*(htab[j] - H)/TMS );
			
			else
				// Non-zero slope pressure equation (lbs/ft^2).
				P = PM[j]*Math.pow( TM[j]/TMS, GMR/ELH );

		} else {
			// TMS linear with Z. Search matrix.
			
			int k = 0;
			for ( int i = 1; i < ZM.length; ++i ) {
				j = i + 8;
				k = i - 1;
				if ( ZM[i] > Z )
					break;
			}
			
			// Calculate TMS, slope, and stuff.
			double ELZ = (TM[j+1] - TM[j]) / (ZM[k+1] - ZM[k]);
			TMS = TM[j] + ELZ * (Z - ZM[k]);
			double DMDZ = (WM[k+1] - WM[k]) / (ZM[k+1] - ZM[k]);
			EM = WM[k] + DMDZ*(Z - ZM[k]);
			double ZLZ = Z - TMS/ELZ;
			
			// Pressure equation for TMS linear with Z (lbs/ft^2).
			double temp = Math.exp( GMR/ELZ*(REARTH/(REARTH + ZLZ)) );
			P = PM[j]*temp*temp;
			temp = Math.log( TMS*(REARTH + ZM[k])/TM[j]/(REARTH + Z) );
			P *= (Z - ZM[k])*(REARTH + ZLZ)/(REARTH + Z)/(REARTH + ZM[k]) - temp;
		}
		
		// Calculate pressure ratio.
		delta = P/2116.2165;
		
		// Calculate density (slug/ft^3).
		Rho = GMR*P/g0/TMS;
		sigma = Rho/0.00237691;
		
		// Calculate temperature (deg Rankine).
		T = EM*TMS/WM0;
		theta = T/518.67;
	}


	/**
	*  A simple method to test the 1962 standard atmosphere.
	*/
	public static void main(String args[]) {
	
		System.out.println("\nTesting StdAtmos1962 class:");
		
		float h = 0;
		StdAtmos atmos = new StdAtmos1962(h);
		System.out.println("    minAltitude = " + atmos.minAltitude() + ", maxAltitude = " + atmos.maxAltitude() +
								" m.");
		System.out.println("    h = " + h + " m, delta = " + atmos.getPressureRatio() + ", sigma = " +
									atmos.getDensityRatio() + ", theta = " + atmos.getTemperatureRatio() + ".");
		h = 20000;
		atmos = new StdAtmos1962(h);
		System.out.println("    h = " + h + " m, delta = " + atmos.getPressureRatio() + ", sigma = " +
									atmos.getDensityRatio() + ", theta = " + atmos.getTemperatureRatio() + ".");
		System.out.println("    h = " + h + " m, P = " + atmos.getPressure() + " N/m^2, density = " +
									atmos.getDensity() + " g/cm^3, T = " + atmos.getTemperature() + " K.");
		System.out.println("    h = " + h + " m, speed of sound = " + atmos.getSpeedOfSound() + " m/s.");
		
		System.out.println("Done!");
	}

}


