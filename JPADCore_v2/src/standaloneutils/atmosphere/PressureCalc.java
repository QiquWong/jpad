package standaloneutils.atmosphere;

public class PressureCalc {

	/**
	 * @see Sforza 2014, page 447 (473 pdf)
	 * @author Lorenzo Attanasio
	 * @param mach
	 * @param staticPressure
	 * @return
	 */
	public static double calculateDynamicPressure(double mach, double staticPressure) {
		return 0.5*AtmosphereCalc.gamma*staticPressure*mach*mach;
	}

	/** 
	 * @author Lorenzo Attanasio
	 * @see Sforza 2014, page 447 (473 pdf)
	 * @param mach
	 * @return
	 */
	public static double calculatePressureCoefficient(double mach) {
		return (2/(AtmosphereCalc.gamma*mach*mach)) 
				* ( Math.pow(
						1 + ((AtmosphereCalc.gamma-1)/2)*mach*mach, AtmosphereCalc.gamma/(AtmosphereCalc.gamma-1))
						- 1);
	}
	
	public static double getPressure(double density, double R, double T, double M) {
		return density * R * T / M;
	}

}
