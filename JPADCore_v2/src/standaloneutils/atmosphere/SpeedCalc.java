package standaloneutils.atmosphere;

import static java.lang.Math.sqrt;

import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import standaloneutils.MyArrayUtils;

public class SpeedCalc {

	/**
	 * 
	 * @param altitude
	 * @param weight
	 * @param surface
	 * @param CLmax
	 * @return
	 */
	public static double calculateSpeedStall(
			double altitude, double weight,
			double surface, double CLmax) {
		return sqrt((2*weight)/(surface*CLmax*AtmosphereCalc.getDensity(altitude)));
	}

	/**
	 * 
	 * @param lift (N)
	 * @param s (m2)
	 * @param rho (kg/m3)
	 * @param cl
	 * @return speed (m/s)
	 */
	public static double calculateSpeedAtCL(double lift, double s, double rho, double cl) {
		return Math.sqrt(2*lift/(rho*s*cl));
	}

	/**
	 * @see Sforza 2014, page 447 (473 pdf)
	 * @param pt stagnation pressure
	 * @param p static pressure
	 * @return
	 */
	public static double calculateCAS(double pt, double p) {
		return Math.sqrt((2*AtmosphereCalc.gamma/(AtmosphereCalc.gamma-1))
				* (AtmosphereCalc.p0.getEstimatedValue()/AtmosphereCalc.rho0.getEstimatedValue())
				* (Math.pow(1 + (pt-p)/AtmosphereCalc.p0.getEstimatedValue(), (AtmosphereCalc.gamma-1)/AtmosphereCalc.gamma)
						- 1));
	}
	
	public static double calculateTAS(double mach, double altitude) {
		return mach*AtmosphereCalc.getSpeedOfSound(altitude);
	}

	public static Amount<Velocity> calculateTAS(Amount<Velocity> VTAS, double altitude) {
		return VTAS.to(SI.METERS_PER_SECOND).divide(Math.sqrt(AtmosphereCalc.getAtmosphere(altitude).getDensityRatio()));
	}
	
	/**
	 * @see Sforza 2014, page 447 (473 pdf)
	 * @author Lorenzo Attanasio
	 * @param pt stagnation pressure
	 * @param p static pressure
	 * @param rho density
	 * @return
	 */
	public static double calculateIsentropicVelocity(double pt, double p, double rho) {
		return Math.sqrt((2*AtmosphereCalc.gamma/(AtmosphereCalc.gamma-1))
				* (p/rho)
				* (Math.pow(1 + (pt-p)/p, (AtmosphereCalc.gamma-1)/AtmosphereCalc.gamma)
						- 1));
	}

	public static double[] createSpeedArray(double stallSpeed, double maxSpeed, int size) {
		return MyArrayUtils.linspace(stallSpeed, maxSpeed, size);
	}
	
	public static double[] createSpeedArray(
			double altitude, double weight, 
			double surface, double CLmax, 
			double maxSpeed, int size) {
		return MyArrayUtils.linspace(calculateSpeedStall(altitude, weight, surface, CLmax), maxSpeed, size);
	}

	/**
	 * 
	 * @param altitude (m)
	 * @param speed (m/s)
	 * @return
	 */
	public static double calculateMach(double altitude, double speed) {
		return speed/AtmosphereCalc.getAtmosphere(altitude).getSpeedOfSound();
	}

}
