package standaloneutils.atmosphere;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import standaloneutils.aerotools.aero.StdAtmos1976;

/**
 * Hold atmosphere model and sea level constants (temperature = 15 °C).
 * Everything is given in SI units.
 * @author Lorenzo Attanasio
 *
 */
public class AtmosphereCalc {

	public static final Amount<Temperature> t0 = Amount.valueOf(288.15, SI.KELVIN);
	public static final double l0 = -0.0065;
	public static final Amount<Pressure> p0 = Amount.valueOf(101325., SI.PASCAL);
	public static final Amount<VolumetricDensity> rho0 = Amount.valueOf(1.225, VolumetricDensity.UNIT);
	public static final Amount<Velocity> a0 = Amount.valueOf(340.27, SI.METERS_PER_SECOND);
	public static final Amount<Acceleration> g0 = Amount.valueOf(9.80665, SI.METERS_PER_SQUARE_SECOND);
	public static final double gamma = 1.4;
	public static final double M = 0.0289644; // molar mass of Earth's air (kg/mol)
	public static final double R = 8.31432; // universal gas constant for air N m/(mol K)

	private static final StdAtmos1976 atmosphere = new StdAtmos1976(0.0, 0.0);

	public static StdAtmos1976 getAtmosphere(double altitude, double deltaTemperature) {
		if(altitude < 0.0) {
//			System.err.println("WARNING: (ATMOSHERE - ALTITUDE) NEGATIVE ALTITUDE. SETTING 0.0 ... ");
			altitude = 0.0;
		}
			
		atmosphere.setAltitudeAndDeltaTemperature(altitude, deltaTemperature);
		return atmosphere;
	}

	public static double getDensity(double altitude, double deltaTemperature) {
		atmosphere.setAltitudeAndDeltaTemperature(altitude, deltaTemperature);
		return atmosphere.getDensity()*1000.;
	}

	public static double getTemperature(double altitude, double deltaTemperature) {
		atmosphere.setAltitudeAndDeltaTemperature(altitude, deltaTemperature);
		return atmosphere.getTemperature();
	}	
	
	public static double getSpeedOfSound(double altitude, double deltaTemperature) {
		atmosphere.setAltitudeAndDeltaTemperature(altitude, deltaTemperature);
		return atmosphere.getSpeedOfSound();
	}

	// Dynamic viscosity accordring to Sutherland Law
	// https://www.cfd-online.com/Wiki/Sutherland%27s_law
	public static double getDynamicViscosity(double altitude, double deltaTemperature) {
		double t = getTemperature(altitude, deltaTemperature); 
		double muRef = 1.716E-5;
		double tRef = 273.15;
		double s = 110.4;
		return muRef*Math.pow(t / tRef, 1.5)
				* (tRef + s)
				/ (t + s);
	}
	
}
