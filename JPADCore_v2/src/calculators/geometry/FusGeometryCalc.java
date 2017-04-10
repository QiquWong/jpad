package calculators.geometry;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

/**
 * In this class there are some methods which computes fuselage geometry parameters.
 * In the /JPADCore/src/aircraft/components/fuselage/Fuselage.java there are methods 
 * which computes the same paramaters.
 * 
 * TODO: delete the methods in this class and use that into Fusalege.java
 * 
 * @author Vincenzo Cusati
 */

public class FusGeometryCalc {

	/**
	 * @author Vincenzo Cusati 
	 * 
	 * @param fuselageDiameter
	 * @param noseFinenessRatio
	 * @return the length of the fuselage nose.
	 */
	public static double calculateFuselageNoseLength(double fuselageDiameter, double noseFinenessRatio){
		return	fuselageDiameter*noseFinenessRatio;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageLength
	 * @param fuselageNoseLength
	 * @param fuselageTailLength
	 * @return the length of the fuselage cabin.
	 */

	public static double calculateFuselageCabinLength(double fuselageLength, double fuselageNoseLength, double fuselageTailLength){
		return	fuselageLength-fuselageNoseLength-fuselageTailLength;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageDiameter
	 * @param tailFinenessRatio
	 * @return the length of the fuselage tailcone.
	 */

	public static double calculateFuselageTailLength(double fuselageDiameter, double tailFinenessRatio){
		return	fuselageDiameter*tailFinenessRatio;
	}

	/**
	 * @author Vincenzo Cusati 
	 * 
	 * @param fuselageDiameter
	 * @param fuselageNoseLength
	 * @return the estimation of the nose wet surface of the fuselage
	 * 
	 * (see  I. Kroo et al., Aircraft Design: Synthesis and Analysis, Stanford 
	 * University, January 2001.
	 * URL http://adg.stanford.edu/aa241/AircraftDesign.html)
	 */
	public static double calcFuselageNoseWetSurface(double fuselageDiameter, double fuselageNoseLength){
		double sWetNose = 0.75*Math.PI*fuselageDiameter*fuselageNoseLength;
		return sWetNose;
	}

	/**
	 * @author Vincenzo Cusati
	 * @param fuselageDiameter
	 * @param fuselageNoseLength
	 * @return the estimation of the cabin wet surface of the fuselage
	 */
	public static double calcFuselageCabinWetSurface(
			double fuselageDiameter, double fuselageCabinLength){
		double sWetCabin = Math.PI*fuselageDiameter*fuselageCabinLength;

		return sWetCabin;
	}

	/**
	 * @author Vincenzo Cusati
	 *
	 * @param fuselageDiameter
	 * @param fuselageTailLength
	 * @return the estimation of the tail wet surface of the fuselage
	 * 
	 * (see  I. Kroo et al., Aircraft Design: Synthesis and Analysis, Stanford 
	 * University, January 2001.
	 * URL http://adg.stanford.edu/aa241/AircraftDesign.html)
	 */
	public static double calcFuselageTailWetSurface(double fuselageDiameter, double fuselageTailLength){
		double sWetTail = 0.72*Math.PI*fuselageDiameter*fuselageTailLength;

		return sWetTail;
	}

//	public static double calcFuselageFrontalSurface(double fuselageDiameter){
//		return Math.PI*Math.pow(fuselageDiameter, 2)/4;
//	}

	/**
	 * @author Vincenzo Cusati
	 *
	 * @param fuselageDiameter
	 * @param fuselageLength
	 * @param fuselageNoseLength
	 * @param fuselageCabinLength
	 * @param fuselageTailLength
	 * @return the estimation of the fuselage wet surface
	 */
	public static double calcFuselageWetSurface(double fuselageDiameter, double fuselageLength,
			double fuselageNoseLength, double fuselageCabinLength, double fuselageTailLength){

		double sWetNose = calcFuselageNoseWetSurface(fuselageDiameter,fuselageNoseLength);
		double sWetTail = calcFuselageTailWetSurface(fuselageDiameter,fuselageTailLength);
		double sWetCabin= calcFuselageCabinWetSurface(fuselageDiameter, fuselageCabinLength);

		double fuselageWetSurface = sWetNose + sWetCabin + sWetTail;

		return fuselageWetSurface;

	}
	
	public static double calculateSfront(Amount<Length> fuselageDiameter){
		return Math.PI*Math.pow(fuselageDiameter.doubleValue(SI.METER), 2)/4;
	}
}
