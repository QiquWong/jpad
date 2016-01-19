package calculators.performance;

import writers.JPADStaticWriteUtils;

public class EnduranceCalc {

	/**
	 * @author Lorenzo Attanasio
	 * @param eta
	 * @param c Fuel consumption (N/(J/s)/s)
	 * @param cl
	 * @param cd
	 * @param rho
	 * @param surface
	 * @param w0
	 * @param wf
	 * @return endurance (s)
	 */
	public static double calculateEnduranceBreguetPropeller(double eta, double c, 
			double cl, double cd, double rho, double surface, double w0, double wf) {
		return (eta/c) * Math.pow(cl, 1.5)/cd * Math.sqrt(2*rho*surface) * (Math.pow(wf, -0.5) - Math.pow(w0, -0.5));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param eta propeller efficiency
	 * @param sfc Specific Fuel Consumption (lb/(hp*h))
	 * @param cl
	 * @param cd
	 * @param rho (kg/m3)
	 * @param surface (m2)
	 * @param w0 Initial weight (kg)
	 * @param wf Final weight (kg)
	 * @return endurance (h)
	 */
	public static double calculateEnduranceBreguetPropellerSFC(double eta, double sfc, 
			double cl, double cd, double rho, double surface, double w0, double wf) {
		return 53.5*calculateEnduranceBreguetPropeller(eta, sfc, cl, cd, rho, surface, w0, wf);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param sfcj Specific Fuel Consumption Jet (lb/(lb*h))
	 * @param cl
	 * @param cd
	 * @param w0 (kg, lb, N)
	 * @param wf (kg, lb, N)
	 * @return endurance (h)
	 */
	public static double calculateEnduranceBreguetJet(double sfcj, double cl, double cd, double w0, double wf) {
		double endurance = (1./sfcj) * cl/cd * Math.log(w0/wf);
		JPADStaticWriteUtils.logToConsole("Endurance (Breguet) is: " + endurance + " h");
		return (1./sfcj) * cl/cd * Math.log(w0/wf);
	}

}
