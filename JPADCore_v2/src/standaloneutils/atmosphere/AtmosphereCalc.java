package standaloneutils.atmosphere;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import standaloneutils.MyUnits;
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

	private static final StdAtmos1976 atmosphere = new StdAtmos1976(0.0);

	public static StdAtmos1976 getAtmosphere(double altitude) {
		atmosphere.setAltitude(altitude);
		return atmosphere;
	}

	public static double getDensity(double altitude) {
		atmosphere.setAltitude(altitude);
		return atmosphere.getDensity()*1000.;
	}

	public static double getTemperature(double altitude) {
		atmosphere.setAltitude(altitude);
		return atmosphere.getTemperature();
	}	
	
	public static double getSpeedOfSound(double altitude) {
		atmosphere.setAltitude(altitude);
		return atmosphere.getSpeedOfSound();
	}

	/**
	 * Return altitude given air density
	 * 
	 * @author Lorenzo Attanasio
	 * @see https://en.wikipedia.org/wiki/Barometric_formula
	 * @param density (kg/m3)
	 * @return altitude (m)
	 */
	public static double getAltitude(double density) {

		if (density > rho0.doubleValue(VolumetricDensity.UNIT)) density = rho0.doubleValue(VolumetricDensity.UNIT); 
		
		// Below 11 km
		if (density > 0.36391)
			return getAltitudeWithTemperatureLapse(density, rho0.doubleValue(VolumetricDensity.UNIT), 
					t0.doubleValue(SI.KELVIN), 0., l0);

		// Below 20 km
		if (density < 0.36391 && density > 0.08803)
			return getAltitudeWithNoTemperatureLapse(density, 0.36391, 216.65, 11000.);

		// Below 32 km
		if (density < 0.08803 && density > 0.01322)
			return getAltitudeWithTemperatureLapse(density, 0.08803, 
					216.15, 20000., 0.001);

		// Below 47 km
		if (density < 0.01322 && density > 0.00143)
			return getAltitudeWithTemperatureLapse(density, 0.01322, 
					228.65, 32000., 0.0028);

		// Below 51 km
		if (density < 0.00143 && density > 0.00086)
			return getAltitudeWithNoTemperatureLapse(density, 0.00143, 270.65, 47000.);

		// Below 71 km
		if (density < 0.00086 && density > 0.000064)
			return getAltitudeWithTemperatureLapse(density, 0.00086, 
					270.65, 51000., -0.0028);

		// Above 71 km
		if (density < 0.000064)
			return getAltitudeWithTemperatureLapse(density, 0.000064, 
					214.65, 71000., -0.002);

		return -1.;
	}
	
//	public static double getAltitudeFromDensity(double density) {
//		return getAltitudeFromPressure();
//	}
	
	public static double getAltitudeFromTemperature(double temperature, double tb, double Lb, double hb) {
		return (temperature - tb) / Lb + hb;
	}
	
//	public static double getAltitudeFromTemperature(double temperature) {
//		return getAltitudeFromTemperature(temperature, t0.doubleValue(SI.KELVIN), Lb, hb);
//	}
	
	
	public static double getAltitudeFromPressure(double pressure) {

		// Below 11 km
		if (pressure > 22632.1)
			return getAltitudeWithTemperatureLapseFromPressure(pressure, p0.doubleValue(Pressure.UNIT), 
					t0.doubleValue(SI.KELVIN), 0., l0);

		// Below 20 km
		if (pressure < 22632.1 && pressure > 5474.89)
			return getAltitudeWithNoTemperatureLapseFromPressure(pressure, 22632.1, 216.65, 11000.);

		// Below 32 km
		if (pressure < 5474.89 && pressure > 868.02)
			return getAltitudeWithTemperatureLapseFromPressure(pressure, 5474.89, 
					216.15, 20000., 0.001);

		// Below 47 km
		if (pressure < 868.02 && pressure > 110.91)
			return getAltitudeWithTemperatureLapseFromPressure(pressure, 868.02, 
					228.65, 32000., 0.0028);

		// Below 51 km
		if (pressure < 110.91 && pressure > 66.94)
			return getAltitudeWithNoTemperatureLapseFromPressure(pressure, 110.91, 270.65, 47000.);

		// Below 71 km
		if (pressure < 66.94 && pressure > 3.96)
			return getAltitudeWithTemperatureLapseFromPressure(pressure, 66.94, 
					270.65, 51000., -0.0028);

		// Above 71 km
		if (pressure < 3.96)
			return getAltitudeWithTemperatureLapseFromPressure(pressure, 3.96, 
					214.65, 71000., -0.002);

		return -1.;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param density
	 * @param rhob
	 * @param Tb
	 * @param hb
	 * @param Lb
	 * @return
	 */
	private static double getAltitudeWithTemperatureLapse(double density, double rhob, double Tb, double hb, double Lb) {
		double exp = - 1. - g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)*M/(R*Lb);
		return hb - (Tb/Lb) * (1. - Math.pow(density/rhob, 1./exp));	
//		return 44.3308 - 42.2665 * Math.pow(density, 0.234969);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param density
	 * @param rhob
	 * @param Tb
	 * @param hb
	 * @return
	 */
	private static double getAltitudeWithNoTemperatureLapse(double density, double rhob, double Tb, double hb) {
		return -R*Tb * Math.log(density/rhob) / (g0.doubleValue(SI.METERS_PER_SQUARE_SECOND) * M) + hb;
	}
	
	private static double getAltitudeWithTemperatureLapseFromPressure(double pressure, double pb, double Tb, double hb, double Lb) {
		double ppbExp = Math.pow(pressure/pb, R*Lb/(g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)*M));
		return Tb * ( 1 - ppbExp) / (Lb * ppbExp) + hb;
	}

	private static double getAltitudeWithNoTemperatureLapseFromPressure(double pressure, double pb, double Tb, double hb) {
		return -R*Tb/(g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)*M) * Math.log(pressure/pb) + hb;
	}

	// Dynamic viscosity accordring to Sutherland Law
	// https://www.cfd-online.com/Wiki/Sutherland%27s_law
	public static double getDynamicViscosity(double altitude) {
		double t = getTemperature(altitude); 
		double muRef = 1.716E-5;
		double tRef = 273.15;
		double s = 110.4;
		return muRef*Math.pow(t / tRef, 1.5)
				* (tRef + s)
				/ (t + s);
	}
	
}
