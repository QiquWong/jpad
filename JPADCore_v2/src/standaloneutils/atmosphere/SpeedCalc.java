package standaloneutils.atmosphere;

import static java.lang.Math.sqrt;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class SpeedCalc {

	/**
	 * 
	 * @param altitude
	 * @param weight
	 * @param surface
	 * @param CLmax
	 * @return
	 */
	public static Amount<Velocity> calculateSpeedStall(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> weight,
			Amount<Area> surface, double CLmax) {
		return Amount.valueOf(
				sqrt((2*weight.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
						/(surface.doubleValue(SI.SQUARE_METRE)*CLmax*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)))),
				SI.METERS_PER_SECOND
				);
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
	
	public static Amount<Velocity> calculateTAS(double mach, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {
		return Amount.valueOf(
				mach*AtmosphereCalc.getSpeedOfSound(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)),
				SI.METERS_PER_SECOND
				);
	}

	public static Amount<Velocity> calculateTAS(Amount<Velocity> VCAS, Amount<Length> altitude, Amount<Temperature> deltaTemperature) {
		return VCAS.to(SI.METERS_PER_SECOND).divide(Math.sqrt(AtmosphereCalc.getAtmosphere(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)).getDensityRatio()));
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

	/**
	 * 
	 * @param altitude (m)
	 * @param speed (m/s)
	 * @return
	 */
	public static double calculateMach(Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Velocity> speed) {
		return speed.doubleValue(SI.METERS_PER_SECOND)
				/AtmosphereCalc.getAtmosphere(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS)).getSpeedOfSound();
	}

}
