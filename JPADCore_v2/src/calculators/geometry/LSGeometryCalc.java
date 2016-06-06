package calculators.geometry;

import static java.lang.Math.pow;
import static java.lang.Math.tan;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;


public class LSGeometryCalc {

	/**
	 * Get the Lifting Surface surface given its span and aspect ratio
	 *  
	 * @param span
	 * @param ar
	 * @return
	 */
	public static double calculateSurface(double span, double ar) {
		return Math.pow(span, 2)/ar;
	}

	public static double calculateSpan(double surface, double ar) {
		return Math.sqrt(ar*surface);
	}

	public static double calculateAR(double span, double surface) {
		return Math.pow(span, 2)/surface;
	}

	/**
	 * 
	 * @param ar
	 * @param taperRatioEquivalent
	 * @param sweepAtY
	 * @param x
	 * @param y
	 * @return
	 */
	public static Amount<Angle> calculateSweep(
			double ar, double taperRatioEquivalent, 
			double sweepAtY, double x, double y) {

		return Amount.valueOf(
				Math.atan(
						Math.tan(sweepAtY) -
						(4./ar)*
						((x - y)*(1 - taperRatioEquivalent)/(1 + taperRatioEquivalent))),
				SI.RADIAN);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mac
	 * @return dimensional distance of the aerodynamic center (AC) from the MAC
	 * leading edge assuming that the AC is at 0.25% of the MAC
	 */
	public static double calcXacFromLEMacQuarter(double mac) {
		return mac*0.25;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param xACfromLEMac
	 * @param xLEMacGRF
	 * @return dimensional distance of the aerodynamic center (AC) from a 
	 * Generic Reference Frame (GRF) origin
	 */
	public static double calcXacInGRF(double xACfromLEMac, double xLEMacGRF) {
		return xACfromLEMac + xLEMacGRF;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mac
	 * @param xLEMacGRF
	 * @return dimensional distance of the aerodynamic center (AC) from a 
	 * Generic Reference Frame (GRF) origin assuming that the AC is at 0.25% of the MAC
	 */
	public static double calcXacInGRFQuarterMAC(double mac, double xLEMacGRF) {
		return calcXacInGRF(calcXacFromLEMacQuarter(mac), xLEMacGRF);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 555 Sforza
	 * 
	 * @param ar
	 * @param mac
	 * @param taperRatioEquivalent
	 * @param sweepQuarterChordEq
	 * @return dimensional distance of the aerodynamic center (AC) from the MAC
	 * leading edge using De Young - Harper method
	 */
	public static double calcXacFromLEMacDeYoungHarper(
			double ar, double mac, double taperRatioEquivalent,
			double sweepQuarterChordEq) {

		return mac*(0.25 + ar * tan(sweepQuarterChordEq)
		* (0.342 - 0.567*taperRatioEquivalent 
				- 0.908*pow(taperRatioEquivalent,2))
		/(10*(1 + taperRatioEquivalent + pow(taperRatioEquivalent,2))));
	}
	
	/*
	 * TO DO: implement me
	 * see Aerodynamic_Database_Ultimate.h5 
	 *   --> (x_bar_ac_w)_k1_vs_lambda
	 *   --> (x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)
	 *   -->(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)
	 *   see sandbox adm 
	 */
	public static double calcXacFromNapolitanoDatcom(double mac,
			double taperRatio, double sweepAngleLE, double aspectRatio,  
			double mach,AerodynamicDatabaseReader adbr) {
		double k1= adbr.getX_bar_ac_w_k1_vs_lambda(taperRatio);
		double k2=adbr.getX_bar_ac_w_k2_vs_L_LE_AR_lambda(taperRatio, sweepAngleLE, aspectRatio);
		double xBarAc=adbr.getX_bar_ac_w_x_ac_over_root_chord_vs_tan_L_LE_over_beta_AR_times_tan_L_LE_lambda(taperRatio, sweepAngleLE, mach, aspectRatio);
		
		double xACNapolitano =  mac*k1*(xBarAc-k2);
		return xACNapolitano;
	}

}
