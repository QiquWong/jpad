package standaloneutils.atmosphere;

public class TemperatureCalc {

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see Sforza 2014, page 447 (473 pdf)
	 * @param mach
	 * @return
	 */
	public static double calculateStagnationTemperatureToStaticTemperatureRatio(double mach) {
		return 1 + ((AtmosphereCalc.gamma-1)/2)*mach*mach;
	}

}
