package sandbox.mr;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;

public class DownwashCalculator_07 {

	/**
	 * This method calculates the downwash gradient using Delft formula. The downwash gradient is 
	 * considered as constant. The distances considered in the formula are geometric and fixed.
	 * 
	 * Distance along X axis -- > Distance between the points at c/4 of the mean 
	 *	                          aerodynamic chord of the wing and the same point 
	 *	                          of the horizontal tail.
	 *	                          
	 * Distance along Z axis -- >Distance between the horizontal tail the vortex
	 *                           shed plane, which can be approximated with the plane
	 *                           from the wing root chord.
	 * 
	 * @param Aircraft
	 * @param distVortexPlane Distance between the horizontal tail the vortex
	 * shed plane, which can be approximated with the plane from the wing root chord.
	 * 
	 * @author  Manuela Ruocco
	 */

	//	public double calculateDownwashDelft(Aircraft aircraft, double distAerodynamicCenter,double distVortexPlane,
	//			double clAlfa, double wingSpan, double sweepQuarterChordEq ){}


	public double calculateDownwashLinearDelft(Aircraft aircraft, double distVortexPlane){

		double downwashGradientLinear;
		double distAerodynamicCenter; // Distance between the points at c/4 of the mean 
		// aerodynamic chord of the wing and the same point 
		// of the horizontal tail.

		distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		double semiWingSpan = aircraft.get_exposedWing().get_semispan().getEstimatedValue();
		double clAlfa = aircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();
		double sweepQuarterChordEq = aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue();
		double aspectRatio = aircraft.get_exposedWing().get_aspectRatio();

		double keGamma, keGammaZero;	

		double r=distAerodynamicCenter/semiWingSpan;
		double rPow=Math.pow(r,2);
		double m=distVortexPlane/semiWingSpan;
		double mpow=Math.pow(m, 2);

		keGamma=(0.1124+0.1265*sweepQuarterChordEq+0.1766*Math.pow(sweepQuarterChordEq,2))
				/rPow+0.1024/r+2;
		keGammaZero=0.1124/rPow+0.1024/r+2;

		double kFraction=keGamma/keGammaZero;
		double first= (r/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));

		downwashGradientLinear=kFraction*(first+second*third)*(clAlfa/(Math.PI*aspectRatio));

		return downwashGradientLinear;

	}	



	/**
	 * This method calculates the distance along Z axis between the AC of the Htail and the 
	 * zero-lift line of the exposed wing.
	 * 
	 * @param Aircraft
	 * 
	 * @author  Manuela Ruocco
	 */


	//	 This method calculates the distance along Z axis between the AC of the Htail and the 
	//	 zero-lift line of the exposed wing. The following treatment is referred to an exposed wing
	//	 so it will called more briefly wing.
	//	 This distance is the sum of the Z coordinate of the Htail and the vertical component between body reference line and the zero lift line of the wing.
	//	 In order to evaluate this contribution it is necessary to translate the body reference line
	//	 of a x quantity until the trailing edge of the root airfoil. 
	//	
	//  	fig. complete

	//	 It is possible to evaluate this quantity starting from the knowledge of wing root chord, the
	//	 angle of incidence of the wing, and the origin of LRF.
	//	
	//      first particular	
	//	
	//     formula
	//	
	//   Now it's possible to evaluate the distance --NOME-- between the new body reference line and the
	//  zero-lift wing line that is easy obtainable known the position of the aerodynamic center and the root chord 
	//	of the wing
	//	
	//  second particular	
	//	
	//  formula



	public double distanceZeroLiftLineACHorizontalTail(Aircraft aircraft){

		// Data
		double zHTailAC = aircraft.get_HTail().get_Z0().getEstimatedValue();
		double cRootExposedWing = aircraft.get_exposedWing().get_theAirfoilsListExposed()
				.get(0).get_chordLocal();
		double angleOfIncidenceExposed = aircraft.get_wing().get_iw().getEstimatedValue()
				+ aircraft.get_exposedWing().get_twistDistributionExposed().get(0);
		double zWing = aircraft.get_wing().get_Z0().getEstimatedValue();
		double distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		double alphaZeroLiftRootExposed = aircraft.get_exposedWing().get_alpha0lDistributionExposed().get(0);


		double newBRLine = zWing - cRootExposedWing*Math.sin(angleOfIncidenceExposed);
		double hTailNewBRLine = zHTailAC - newBRLine;


		LSAerodynamicsManager.CalcXAC theXACCalculator = aircraft.get_wing().getAerodynamics().new CalcXAC();
		double xACLRF = theXACCalculator.deYoungHarper() + aircraft.get_wing().get_xLEMacActualLRF().getEstimatedValue();
		double  xACRootExposed = xACLRF - aircraft.get_wing().getXLEAtYActual(aircraft.get_fuselage().getWidthAtX(
				aircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue()).doubleValue());
		double distTrailingEdgeWingXACH = distAerodynamicCenter - (cRootExposedWing - xACRootExposed);
		double bRLineZeroLiftLine = distTrailingEdgeWingXACH * Math.tan(angleOfIncidenceExposed + alphaZeroLiftRootExposed);


		double distance = hTailNewBRLine + bRLineZeroLiftLine;
		return distance;

	}


	/**
	 * This method calculates the distance along Z axis between the AC of the Htail and the 
	 * direction of flow not considering the deflection due to downwash.
	 * 
	 * @param Aircraft
	 * 
	 * @author  Manuela Ruocco
	 */
	

	//	 In order to evaluate the effective distance between the horizontal tail an the vortex shed plane
	// it's necessary to evaluate the same distance not considering the deflection due to downwash.
	// This deflection, in fact, is obtainable with an iterative process in witch the starting 
	// value of downwash is obtained from the previous step.
	//
	//  graph
	// particular 
	
	public double distanceVortexShedPlaneACHTailNoDownwash(Aircraft aircraft, Amount<Angle> alphaAbsolute){

		double zeroLiftDistance = distanceZeroLiftLineACHorizontalTail(aircraft);
		//TODO continue here
		
		double distance = 0.0; //seroLiftDistance - (z-y) 
				return distance;
	}

}
