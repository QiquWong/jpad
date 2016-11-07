package calculators.aerodynamics;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.components.LandingGears;
import aircraft.components.LandingGears.MountingPosition;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import calculators.performance.LandingCalc;
import calculators.performance.customdata.DragMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class DragCalc {

	private DragCalc() { }
	
	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param weight
	 * @param altitude
	 * @param surface
	 * @param speed
	 * @param cD
	 * @return
	 */
	public static double calculateDragAtSpeed(
			double weight, double altitude, 
			double surface,double speed,
			double cD) {
		return 0.5*AtmosphereCalc.getDensity(altitude)*speed*speed*surface*cD;
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param weight
	 * @param altitude
	 * @param surface
	 * @param speed
	 * @param cd0
	 * @param cL
	 * @param ar
	 * @param e
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return drag (N)
	 */
	public static double calculateDragAtSpeed(double weight, double altitude, 
			double surface, double speed, double cd0, double cL, double ar, double e,
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		
		return calculateDragAtSpeed( weight, altitude, surface, speed, 
				calculateCDTotal(cd0, cL, ar, e, SpeedCalc.calculateMach(altitude, speed), 
						sweepHalfChord, tcMax, airfoilType) );
	}
	
	/**
	 * 
	 * @param weight
	 * @param altitude
	 * @param surface
	 * @param speed
	 * @param cd0
	 * @param ar
	 * @param e
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return drag (N)
	 */
	public static double calculateDragAtSpeedLevelFlight(double weight, double altitude, 
			double surface, double speed, double cd0, double ar, double e,
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		
		return calculateDragAtSpeed(weight, altitude, surface, speed, cd0, 
				LiftCalc.calculateLiftCoeff(weight, speed, surface, altitude), 
				ar, e, sweepHalfChord, tcMax, airfoilType);
	}
	

	/**
	 * @author Lorenzo Attanasio
	 * @param formFactor
	 * @param cf
	 * @param sWet
	 * @param referenceSurface
	 * @return
	 */
	public static Double calculateCD0Parasite(
			LiftingSurface theLiftingSurface,
			OperatingConditions theOperatingConditions
			){
		
		Double cD0Parasite = 0.0;
		Double cF = 0.0;
		Double reynolds = theOperatingConditions.calculateRe(
				theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue(), 
				theLiftingSurface.getLiftingSurfaceCreator().getRoughness().getEstimatedValue());
		
		if (theOperatingConditions.calculateReCutOff(
				theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue(), 
				theLiftingSurface.getLiftingSurfaceCreator().getRoughness().getEstimatedValue()) < 
				reynolds) {

			reynolds = theOperatingConditions.calculateReCutOff(
					theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue(),
					theLiftingSurface.getLiftingSurfaceCreator().getRoughness().getEstimatedValue());

			cF  = (AerodynamicCalc.calculateCf(
					reynolds, theOperatingConditions.getMachCurrent(), 
					theLiftingSurface.getLiftingSurfaceCreator().getXTransitionUpper()) 
					+ AerodynamicCalc.calculateCf(
							reynolds, 
							theOperatingConditions.getMachCurrent(),
							theLiftingSurface.getLiftingSurfaceCreator().getXTransitionLower()))/2;

		} else // XTRANSITION!!!
		{
			cF  = (AerodynamicCalc.calculateCf(
					reynolds,
					theOperatingConditions.getMachCurrent(),
					theLiftingSurface.getLiftingSurfaceCreator().getXTransitionUpper()) + 
					AerodynamicCalc.calculateCf(
							reynolds,
							theOperatingConditions.getMachCurrent(),
							theLiftingSurface.getLiftingSurfaceCreator().getXTransitionLower()))/2; 

		}

		if (theLiftingSurface.getType() == ComponentEnum.WING) {
			cD0Parasite = 
					cF * theLiftingSurface.getFormFactor() 
					* theLiftingSurface.getLiftingSurfaceCreator().getSurfaceWettedExposed().getEstimatedValue()
					/ theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE);			

		} else { //TODO NEED TO EVALUATE Exposed Wetted surface also for Vtail and Htail
			cD0Parasite = 
					cF * theLiftingSurface.getFormFactor() 
					* theLiftingSurface.getLiftingSurfaceCreator().getSurfaceWetted().doubleValue(SI.SQUARE_METRE)
					/ theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE);
		}

		return cD0Parasite;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param method
	 * @param cd0Parasite
	 * @param S_ref
	 * @param dbase
	 * @param d
	 * @return
	 */
	public static Double calculateCd0Base(MethodEnum method, Double cd0Parasite, 
			Double S_ref, Double dbase, Double d) { 
		// The bigger dbase the lower CdBase
		switch(method){
		case NICOLAI_2013 : {return 0.029 * Math.pow(dbase/d,3)/Math.sqrt(cd0Parasite);} // page 354 Nicolai pdf
		case MATLAB : {return (0.029 * Math.pow(dbase/d,3)/(Math.pow(cd0Parasite*(S_ref/(Math.PI*Math.pow(d,2)/4)),0.5))) * Math.pow(S_ref/(Math.PI*Math.pow(d,2)/4),-1);}
		default : return 0.;
		}
	}

	public static Double calculateCDGap(LiftingSurface theLiftingSurface) {
		Double cDGap = 0.0002*(
				Math.pow(Math.cos(theLiftingSurface.getSweepQuarterChordEquivalent(false).doubleValue(SI.RADIAN)) ,2))
				* 0.3 
				* theLiftingSurface.getLiftingSurfaceCreator().getSurfaceWettedExposed().doubleValue(SI.SQUARE_METRE)
				/ theLiftingSurface.getSurface().doubleValue(SI.SQUARE_METRE);

		return cDGap; 
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param S_wet
	 * @return
	 */
	public static Double calculateKExcrescences(Double S_wet) {
		return 4e-19*Math.pow(S_wet,4) 
				- 2e-14*Math.pow(S_wet,3) 
				+ 5e-10*Math.pow(S_wet,2) 
				- 7e-6*S_wet + 0.0825;
	}

	/***********************************************************************************************
	 * The drag coefficient of the undercarriage is the sum of the main an nose gears contributions.
	 * The DeltaCD0LandingGears is made up of two other contributions:
	 * DeltaCD0basic and a function of alpha and delta flap.
	 * 
	 * N.B.: the flap considered is the one related to the undercarriage position (if present).
	 * 
	 * N.B.: deltaCL0flap have to be passed as zero or null in case the undercarriage position
	 * 		 is not WING !!
	 * 
	 * --------------------------------------------------------------------------------------
	 * DeltaCD0LandingGears = f(alpha, deltaFlap)*DeltaCD0basic
	 * 
	 * --------------------------------------------------------------------------------------
	 * DeltaCD0basic = (1.5*sum(S_frontal_tires) + 0.75*sum(S_rear_tires))/(S_wing)
	 * 
	 * --------------------------------------------------------------------------------------
	 * f(alpha, deltaFlap) = {1-(0.04*((CL+DeltaCL0_flap*((1.5*(S/Swf))-1))/(l_uc/cg)}^2
	 * 
	 * --------------------------------------------------------------------------------------
	 * where l_uc is the length of the main gear leg and cg is the mean geometric chord of the wing
	 * (S/b).
	 * 
	 * @see: Torenbeek 1976 pag.570(pdf)
	 */
	public static double calculateDeltaCD0LandingGears(
			LiftingSurface wing,
			LandingGears landingGears,
			Double cL,
			Double deltaCL0flap
			) {
		
		if(deltaCL0flap == null)
			deltaCL0flap = 0.0;
		
		double deltaCD0 = 0.0;
		double deltaCD0Basic = 0.0;
		double functionAlphaDeltaFlap = 0.0;
		
		Amount<Area> frontalTiresTotalArea = Amount.valueOf(0.0, SI.SQUARE_METRE);
		Amount<Area> rearTiresTotalArea = Amount.valueOf(0.0, SI.SQUARE_METRE);
		
		for(int i=0; i<landingGears.getNumberOfFrontalWheels(); i++) {
			frontalTiresTotalArea = frontalTiresTotalArea.plus(landingGears.getFrontalWheelsHeight().times(landingGears.getFrontalWheelsWidth()));
		}
		
		for(int i=0; i<landingGears.getNumberOfRearWheels(); i++) {
			rearTiresTotalArea = rearTiresTotalArea.plus(landingGears.getRearWheelsHeight().times(landingGears.getRearWheelsWidth()));
		}
		
		deltaCD0Basic = ((1.5*frontalTiresTotalArea.getEstimatedValue())
				+(0.75*rearTiresTotalArea.getEstimatedValue()))
				/(wing.getSurface().getEstimatedValue());
				
		if(landingGears.getMountingPosition() == MountingPosition.WING) {
		
			Amount<Area> flapSurface = Amount.valueOf(
					wing.getSpan().getEstimatedValue()							
					/2*wing.getLiftingSurfaceCreator().getRootChordEquivalentWing().getEstimatedValue()
					*(2-((1-wing.getLiftingSurfaceCreator().getTaperRatioEquivalentWing())
							*(wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
									+wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getInnerStationSpanwisePosition())))
					*(wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
							-wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getInnerStationSpanwisePosition()),
					SI.SQUARE_METRE
					);
			
			functionAlphaDeltaFlap = 
					Math.pow(1-(0.04
							*(cL+(deltaCL0flap*((1.5*(wing.getSurface().divide(flapSurface).getEstimatedValue()))-1))
									)
							/(landingGears.getMainLegsLenght().getEstimatedValue()
									/(wing.getSurface().getEstimatedValue()/wing.getSpan().getEstimatedValue())
									)
							)
							,2);
		}
		else if((landingGears.getMountingPosition() == MountingPosition.FUSELAGE)
				|| (landingGears.getMountingPosition() == MountingPosition.NACELLE)) {
		
			functionAlphaDeltaFlap = 
					Math.pow(1-(0.04*cL/(landingGears.getMainLegsLenght().getEstimatedValue()
									/(wing.getSurface().getEstimatedValue()/wing.getSpan().getEstimatedValue())
									)
							)
							,2);		
		}
		
		deltaCD0 = deltaCD0Basic*functionAlphaDeltaFlap;
		
		return deltaCD0;
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param cL
	 * @param machCurrent
	 * @param machCr
	 * @return
	 */
	public static double calculateCDWaveLockKorn(double cL, double machCurrent, double machCr) {

		double diff = machCurrent - machCr;

		if (diff > 0) return 20.*Math.pow(diff,4);
		return 0.;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param cL
	 * @param mach
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double calculateCDWaveLockKorn(double cL, double mach, 
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateCDWaveLockKorn(cL, mach, AerodynamicCalc.calculateMachCriticalKornMason(cL, sweepHalfChord, tcMax, airfoilType));
	}
	
	/**
	 * @author Vittorio Trifari
	 * @param cL
	 * @param mach
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double calculateCDWaveLockKornCriticalMachKroo(double cL, double mach, 
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateCDWaveLockKorn(cL, mach, AerodynamicCalc.calculateMachCriticalKroo(cL, Amount.valueOf(sweepHalfChord,SI.RADIAN), tcMax, airfoilType));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param cd0
	 * @param cL
	 * @param ar
	 * @param e
	 * @param mach
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double calculateCDTotal(
			double cd0, double cL, double ar, double e, double mach, 
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateCDTotal(cd0, cL, ar, e, mach, 
				calculateCDWaveLockKorn(cL, mach, sweepHalfChord, tcMax, airfoilType));
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param cD0
	 * @param cL
	 * @param ar
	 * @param e
	 * @param mach
	 * @param cDwave
	 * @return
	 */
	public static double calculateCDTotal(
			double cD0, double cL, double ar, double e, double mach, 
			double cDwave) {
		return cD0 + cL*cL*calculateKfactorDragPolar(ar,e) + cDwave;
	}

	public static double calculateKfactorDragPolar(double ar, double e) {
		return 1./(Math.PI*ar*e);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param weight
	 * @param phi
	 * @param altitude
	 * @param surface
	 * @param cD0
	 * @param ar
	 * @param oswald
	 * @param speed
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double[] calculateDragVsSpeed(
			double weight, double altitude, 
			double surface, double cD0, double ar, double oswald,
			double speed[], 
			double sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType
			) {

		double cL, mach, cD;
		double[] drag = new double[speed.length];
		for (int i=0; i< speed.length; i++){
			cL = LiftCalc.calculateLiftCoeff(weight, speed[i], surface, altitude);
			mach = SpeedCalc.calculateMach(altitude, speed[i]);
			cD = calculateCDTotal(cD0, cL, ar, oswald, mach, sweepHalfChord, tcMax, airfoilType);
			drag[i] = calculateDragAtSpeed(weight, altitude, surface, speed[i], cD);
		}
		return drag;
	}
	
	/**
	 * @author Lorenzo Attanasio
	 * @param altitude (m)
	 * @param phi
	 * @param weight (N)
	 * @param speed (m/s)
	 * @param surface (m2)
	 * @param CLmax
	 * @param cD0
	 * @param ar
	 * @param oswald
	 * @param sweepHalfChord (rad)
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static List<DragMap> calculateDragAndPowerRequired(
			double[] altitude, double[] phi, double[] weight,
			double[] speed,
			double surface, double CLmax, double cD0,
			double ar, double oswald, double sweepHalfChord, 
			double tcMax, AirfoilTypeEnum airfoilType) {

		int nAlt = altitude.length;
		List<DragMap> list = new ArrayList<DragMap>();

		for(int k=0; k<weight.length; k++) {
				for(int i=0; i<nAlt; i++) {

					list.add(new DragMap(weight[k], altitude[i], 
							calculateDragVsSpeed(weight[k], altitude[i], 
									surface, cD0, ar, oswald, speed, sweepHalfChord, tcMax, airfoilType), 
									speed));
				}
		}

		return list;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param weight
	 * @param speed
	 * @param surface
	 * @param CLmax
	 * @param cD0
	 * @param ar
	 * @param oswald
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static DragMap calculateDragAndPowerRequired(
			double altitude, double weight, double[] speed,
			double surface,
			double CLmax, double cD0, double ar,
			double oswald, double sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType) {

		return new DragMap(weight, altitude, 
				calculateDragVsSpeed(weight, altitude, 
						surface, cD0, ar, oswald, speed, sweepHalfChord, tcMax, airfoilType), 
						speed);

	}

	/**
	 * @author Lorenzo Attanasio
	 * @param rho density (kg/m3)
	 * @param cD0 zero lift drag coefficient
	 * @param w weight (N)
	 * @param s surface (m2)
	 * @param v speed (m/s)
	 * @param K
	 * @return
	 */
	public static double calculatePowerRequiredAtSpeed(
			double rho, double cD0, double w, double s, double v, double K) {
		return 0.5 * rho*cD0*s*v*v*v + (2/rho)*s*K*(w/s)*(w/s)/v;
	}
	
	/**
	 * @author Vincenzo Cusati
	 * 
	 * This function calculates the fuselage drag coefficient using the FusDes method
	 * 
	 *(see "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 * 
	 * @param kn
	 * @param kc
	 * @param kt
	 * @param sWet
	 * @param sWetNose
	 * @param sWetCabin
	 * @param sWetTail
	 * @param sFront
	 * @param cDFlatPlate
	 * @return
	 */
	public static double dragFusDesCalc (double kn, double kc, double kt,
			double sWet, double sWetNose, double sWetCabin, double sWetTail,
			double sFront, double cDFlatPlate){
		return (kn*(sWetNose/sWet) + kc*(sWetCabin/sWet) + kt*(sWetTail/sWet))*cDFlatPlate*sWet/sFront; 
	}

}
