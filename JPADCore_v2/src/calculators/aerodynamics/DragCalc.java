package calculators.aerodynamics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.lang3.ArrayUtils;
import org.jscience.physics.amount.Amount;

import aircraft.components.LandingGears;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.geometry.FusNacGeometryCalc;
import calculators.performance.customdata.DragMap;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.WindshieldTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
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
	public static Amount<Force> calculateDragAtSpeed(
			Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature,
			Amount<Area> surface, Amount<Velocity> speed,
			double cD) {
		return Amount.valueOf(
				0.5
				*AtmosphereCalc.getDensity(altitude.doubleValue(SI.METER), deltaTemperature.doubleValue(SI.CELSIUS))
				*Math.pow(speed.doubleValue(SI.METERS_PER_SECOND), 2)
				*surface.doubleValue(SI.SQUARE_METRE)
				*cD,
				SI.NEWTON
				);
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
	public static Amount<Force> calculateDragAtSpeed(Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature, 
			Amount<Area> surface, Amount<Velocity> speed, double cd0, double cL, double ar, double e,
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		
		return calculateDragAtSpeed( weight, altitude, deltaTemperature, surface, speed, 
				calculateCDTotal(cd0, cL, ar, e, SpeedCalc.calculateMach(altitude, deltaTemperature, speed), 
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
	public static Amount<Force> calculateDragAtSpeedLevelFlight(Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature,  
			Amount<Area> surface, Amount<Velocity> speed, double cd0, double ar, double e,
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		
		return calculateDragAtSpeed(weight, altitude, deltaTemperature, surface, speed, cd0, 
				LiftCalc.calculateLiftCoeff(
						Amount.valueOf(weight.doubleValue(SI.KILOGRAM)*9.81, SI.NEWTON),
						speed,
						surface, 
						altitude, 
						deltaTemperature
						), 
				ar, e, sweepHalfChord, tcMax, airfoilType);
	}
	
	public static Double calculateCD0Upsweep(  // page 67 Behind ADAS (Stanford)
			Amount<Area> cylindricalSectionBaseArea,
			Amount<Area> wingSurface,
			Amount<Length> lengthTailCone,
			Amount<Length> zCamber75
			) {
		return 0.075 
				*(cylindricalSectionBaseArea.doubleValue(SI.SQUARE_METRE)/wingSurface.doubleValue(SI.SQUARE_METRE))
				*(zCamber75.doubleValue(SI.METER)/(0.75*lengthTailCone.doubleValue(SI.METER)));
	}
	
	public static Double calculateCD0Windshield(
			MethodEnum method,
			WindshieldTypeEnum windshieldType,
			Amount<Area> windshieldArea,
			Amount<Area> cylindricalSectionBaseArea,
			Amount<Area> wingSurface
			) {
		
		Double cDWindshield = 0.0;
		Double deltaCd = 0.0;

		switch(method){ // Behind ADAS page 101
		case SEMIEMPIRICAL : {
			cDWindshield = 0.07*windshieldArea.doubleValue(SI.SQUARE_METRE)/
					wingSurface.doubleValue(SI.SQUARE_METRE);
		} break;
		case NACA : { // NACA report 730, poor results
			cDWindshield =  0.08*cylindricalSectionBaseArea.doubleValue(SI.SQUARE_METRE)/
					wingSurface.doubleValue(SI.SQUARE_METRE);
		} break;
		case ROSKAM : { // page 134 Roskam, part VI
			switch(windshieldType){
			case FLAT_PROTRUDING : {deltaCd = .016;}; break;
			case FLAT_FLUSH : {deltaCd = .011;}; break;
			case SINGLE_ROUND : {deltaCd = .002;}; break;
			case SINGLE_SHARP : {deltaCd = .005;}; break;
			case DOUBLE : {deltaCd = .002;}; break;
			default : {deltaCd = 0.0;}; break;
			}
			cDWindshield = (deltaCd*cylindricalSectionBaseArea.doubleValue(SI.SQUARE_METRE))/
					wingSurface.doubleValue(SI.SQUARE_METRE);
		} break;
		default : {
			System.out.println("Inside default branch of calculateWindshield method, class MyFuselage");
			cDWindshield =  0.0;
		} break;
		}

		return cDWindshield;
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
			Double formFactor, 
			Double cf, 
			Double sWet, 
			Double referenceSurface
			){
		return formFactor*cf*sWet/referenceSurface;
	}

	public static Double calculateCD0ParasiteLiftingSurface(
			LiftingSurface theLiftingSurface,
			Double machTransonicThreshold,
			Double mach,
			Amount<Length> altitude,
			Amount<Temperature> deltaTemperature
			){
		
		Double cD0Parasite = 0.0;
		Double cF = 0.0;
		
		Double reynolds = AerodynamicCalc.calculateReynolds(
				altitude,
				deltaTemperature,
				mach,
				theLiftingSurface.getMeanAerodynamicChord()
				);
		
		if (AerodynamicCalc.calculateReCutOff(
				mach,
				machTransonicThreshold,
				theLiftingSurface.getMeanAerodynamicChord(), 
				theLiftingSurface.getRoughness()) < 
				reynolds) {

			reynolds = AerodynamicCalc.calculateReCutOff(
					mach,
					machTransonicThreshold,
					theLiftingSurface.getMeanAerodynamicChord(), 
					theLiftingSurface.getRoughness());

			cF  = (AerodynamicCalc.calculateCf(
					reynolds, mach, 
					theLiftingSurface.getXTransitionUpper()) 
					+ AerodynamicCalc.calculateCf(
							reynolds, 
							mach,
							theLiftingSurface.getXTransitionLower()))/2;

		} else // XTRANSITION!!!
		{
			cF  = (AerodynamicCalc.calculateCf(
					reynolds,
					mach,
					theLiftingSurface.getXTransitionUpper()) + 
					AerodynamicCalc.calculateCf(
							reynolds,
							mach,
							theLiftingSurface.getXTransitionLower()))/2; 

		}

		if (theLiftingSurface.getType() == ComponentEnum.WING) {
			cD0Parasite = 
					cF * theLiftingSurface.getFormFactor() 
					* theLiftingSurface.getSurfaceWettedExposed().getEstimatedValue()
					/ theLiftingSurface.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);			

		} else { //TODO NEED TO EVALUATE Exposed Wetted surface also for Vtail and Htail
			cD0Parasite = 
					cF * theLiftingSurface.getFormFactor() 
					* theLiftingSurface.getSurfaceWetted().doubleValue(SI.SQUARE_METRE)
					/ theLiftingSurface.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
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
	public static Double calculateCD0Base(MethodEnum method, Double cd0Parasite, 
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
				Math.pow(Math.cos(theLiftingSurface.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)) ,2))
				* 0.3 
				* theLiftingSurface.getExposedLiftingSurface().getSurfaceWetted().doubleValue(SI.SQUARE_METRE)
				/ theLiftingSurface.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);

		return cDGap; 
	}
	
	public static Double calculateCD0Total(
			Double cD0TotalFuselage,
			Double cD0TotalWing,
			Double cD0TotalNacelles,
			Double cD0TotalHTail,
			Double cD0TotalVTail,
			Double cD0TotalCanard,
			Double deltaCD0FLap,
			Amount<Area> sWet
			) {
		Double cD0Total;
//		Double cD0Parasite = cD0ParasiteFuselage 
//				+ cD0ParasiteWing 
//				+ cD0ParasiteNacelles 
//				+ cD0ParasiteHTail 
//				+ cD0ParasiteVTail
//				+ cD0ParasiteCanard;
//		
		Double cD0 = cD0TotalFuselage 
				+ cD0TotalWing 
				+ cD0TotalNacelles 
				+ cD0TotalHTail
				+ cD0TotalVTail
				+ cD0TotalCanard
				+ deltaCD0FLap;

		//	Double cDRough = AerodynamicCalc.calculateRoughness(cD0);
//		Double cDExc = cD0 * calculateKExcrescences(sWet.doubleValue(SI.SQUARE_METRE));
//		Double cDCool = AerodynamicCalc.calculateCoolings(cD0Parasite);

//		cD0Total = (cD0 + cDExc + cDCool);
		cD0Total = cD0;

		return cD0Total; 
	}
	
	public static Double calculateCD0Total(
			Double cD0TotalFuselage,
			Double cD0TotalWing,
			Double cD0TotalNacelles,
			Double cD0TotalHTail,
			Double cD0TotalVTail,
			Double deltaCD0FLap,
			Amount<Area> sWet
			) {
		Double cD0Total;
//		Double cD0Parasite = cD0ParasiteFuselage 
//				+ cD0ParasiteWing 
//				+ cD0ParasiteNacelles 
//				+ cD0ParasiteHTail 
//				+ cD0ParasiteVTail;
		
		Double cD0 = cD0TotalFuselage 
				+ cD0TotalWing 
				+ cD0TotalNacelles 
				+ cD0TotalHTail
				+ cD0TotalVTail
				+ deltaCD0FLap;

	//	Double cDRough = AerodynamicCalc.calculateRoughness(cD0);
//		Double cDExc = cD0 * calculateKExcrescences(sWet.doubleValue(SI.SQUARE_METRE));
//		Double cDCool = AerodynamicCalc.calculateCoolings(cD0Parasite);

//		cD0Total = (cD0 + cDExc + cDCool);
		cD0Total = cD0;

		return cD0Total; 
	}
	
	/**
	 * @author Manuela Ruocco
	 * @return
	 */
	public static Double calculateCD0Excrescences(
			Double cD0TotalFuselage, 
			Double cD0TotalWing, 
			Double cD0TotalNacelles,
			Double cD0TotalHTail, 
			Double cD0TotalVTail,
			Double S_wet) {
		
		
		Double cD0 = cD0TotalFuselage 
				+ cD0TotalWing 
				+ cD0TotalNacelles 
				+ cD0TotalHTail
				+ cD0TotalVTail;

		Double cD0Excrescences = cD0 * calculateKExcrescences(S_wet);
		
		return cD0Excrescences;
	}
	
	/**
	 * @author Manuela Ruocco
	 * @return
	 */
	public static Double calculateCD0Cooling(
			Double cD0ParasiteFuselage, 
			Double cD0ParasiteWing, 
			Double cD0ParasiteNacelles,
			Double cD0ParasiteHTail, 
			Double cD0ParasiteVTail
			) {
		
		Double cD0Parasite = cD0ParasiteFuselage 
		+ cD0ParasiteWing 
		+ cD0ParasiteNacelles 
		+ cD0ParasiteHTail 
		+ cD0ParasiteVTail;


		Double cDCool = AerodynamicCalc.calculateCoolings(cD0Parasite);
		
		return cDCool;
	}
	
	/**
	 * @author Manuela Ruocco
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
				/(wing.getSurfacePlanform().getEstimatedValue());
				
		if(landingGears.getMountingPosition() == LandingGearsMountingPositionEnum.WING) {
		
			Amount<Area> flapSurface = Amount.valueOf(
					wing.getSpan().getEstimatedValue()							
					/2*wing.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					*(2-((1-wing.getEquivalentWing().getPanels().get(0).getTaperRatio())
							*(wing.getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
									+wing.getSymmetricFlaps().get(0).getInnerStationSpanwisePosition())))
					*(wing.getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
							-wing.getSymmetricFlaps().get(0).getInnerStationSpanwisePosition()),
					SI.SQUARE_METRE
					);
			
			functionAlphaDeltaFlap = 
					Math.pow(1-(0.04
							*(cL+(deltaCL0flap*((1.5*(wing.getSurfacePlanform().divide(flapSurface).getEstimatedValue()))-1))
									)
							/(landingGears.getMainLegsLenght().getEstimatedValue()
									/(wing.getSurfacePlanform().getEstimatedValue()
											/wing.getSpan().getEstimatedValue())
									)
							)
							,2);
		}
		else if((landingGears.getMountingPosition() == LandingGearsMountingPositionEnum.FUSELAGE)
				|| (landingGears.getMountingPosition() == LandingGearsMountingPositionEnum.NACELLE)) {
		
			functionAlphaDeltaFlap = 
					Math.pow(1-(0.04*cL/(landingGears.getMainLegsLenght().getEstimatedValue()
									/(wing.getSurfacePlanform().getEstimatedValue()
											/wing.getSpan().getEstimatedValue())
									)
							)
							,2);		
		}
		
		deltaCD0 = deltaCD0Basic*functionAlphaDeltaFlap;
		
		return deltaCD0;
	}
	
	
	/**
	 * @author Manuela Ruocco
	 * @param cL
	 * @param machCurrent
	 * @param machCr
	 * @return
	 */
	public static double calculateDeltaCD0DueToWingFuselageInterference( double hw, double tcRoot, Amount<Area> sWet, Amount<Length> chordRoot) {

		double deltaCD0 = 0.0;
		
		deltaCD0 = (0.5 * Math.pow(hw, 2) + 1.25 * hw + 0.75) * 2.16 * Math.pow(chordRoot.doubleValue(SI.METER),2) * Math.pow(tcRoot,3);  
				
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
	 * @author Manuela Ruocco
	 * @param cL
	 * @param machCurrent
	 * @param machCr
	 * @return
	 */
	public static double calculateOswaldFactorModificationDueToWinglet(Amount<Length> wingletHeight, Amount<Length> wingSpan ) {

		double oswaldModificationFactor = 0.0;
		double [] wingletHeightValues = {0, 0.1};
		double [] oswaldFactorValues = {1, 1.1};
		
		double diff = wingletHeight.doubleValue(SI.METER)/wingSpan.doubleValue(SI.METER);
		oswaldModificationFactor = MyMathUtils.getInterpolatedValue1DLinear(wingletHeightValues, oswaldFactorValues, diff);
		return oswaldModificationFactor;
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
		double cdWawe = calculateCDWaveLockKorn(cL, mach, AerodynamicCalc.calculateMachCriticalKornMason(cL, sweepHalfChord, tcMax, airfoilType));
		return cdWawe;
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
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateCDWaveLockKorn(cL, mach, AerodynamicCalc.calculateMachCriticalKroo(cL, sweepHalfChord, tcMax, airfoilType));
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
	public static double calculateCDWingBodyInterferences(double hw, Amount<Length> rootChord, double maximumThicknessRootChord) {
		
		double cDWingFuselageInterferences = (
				(0.5*Math.pow(hw, 2) + 1.25 * hw + 0.75) * 2.16 * Math.pow(rootChord.doubleValue(SI.METER),2) * Math.pow(maximumThicknessRootChord,3)
				);
		
		return cDWingFuselageInterferences;
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
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateCDTotal(cd0, cL, ar, e, mach, 
				calculateCDWaveLockKornCriticalMachKroo(cL, mach, sweepHalfChord, tcMax, airfoilType));
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
	public static List<Amount<Force>> calculateDragVsSpeed(
			Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature, 
			Amount<Area> surface, double cD0, double ar, double oswald,
			List<Amount<Velocity>> speed, 
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType
			) {

		List<Amount<Force>> drag = new ArrayList<>();
		for (int i=0; i< speed.size(); i++){
			double cL = LiftCalc.calculateLiftCoeff(Amount.valueOf(weight.doubleValue(SI.KILOGRAM)*9.81, SI.NEWTON), speed.get(i), surface, altitude, deltaTemperature);
			drag.add(calculateDragAtSpeed(weight, altitude, deltaTemperature, surface, speed.get(i), cD0, cL, ar, oswald, sweepHalfChord, tcMax, airfoilType));
		}
		return drag;
	}
	
	/**
	 * This method allows the user to evaluate the drag as function of the speed using a known 
	 * polar curve. (Useful when the parabolic polar model is too simple)
	 * 
	 * @author Vittorio Trifari
	 * @param weight
	 * @param altitude
	 * @param surface
	 * @param polarCL
	 * @param polarCD
	 * @param speed
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static List<Amount<Force>> calculateDragVsSpeed(
			Amount<Mass> weight, Amount<Length> altitude, Amount<Temperature> deltaTemperature, 
			Amount<Area> surface,
			double[] polarCL,
			double[] polarCD,
			List<Amount<Velocity>> speed, 
			Amount<Angle> sweepHalfChord, double tcMax, AirfoilTypeEnum airfoilType
			) {

		double cL, cD, cDWave, cDTot;
		List<Amount<Force>> drag = new ArrayList<>();
		for (int i=0; i< speed.size(); i++){
			cL = LiftCalc.calculateLiftCoeff(Amount.valueOf(weight.doubleValue(SI.KILOGRAM)*9.81, SI.NEWTON), speed.get(i), surface, altitude, deltaTemperature);
			cD = MyMathUtils.getInterpolatedValue1DLinear(polarCL, polarCD, cL);
			cDWave = calculateCDWaveLockKorn(
					cL, 
					SpeedCalc.calculateMach(altitude, deltaTemperature, speed.get(i)), 
					AerodynamicCalc.calculateMachCriticalKornMason(
							cL, 
							sweepHalfChord,
							tcMax, 
							airfoilType)
					);
			cDTot = cD + cDWave;
			drag.add(calculateDragAtSpeed(weight, altitude, deltaTemperature, surface, speed.get(i), cDTot));
		}
		return drag;
	}
	
	/**
	 * @author Vittorio Trifari
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
			List<Amount<Length>> altitude, Amount<Temperature> deltaTemperature, List<Amount<Mass>> weight,
			List<Amount<Velocity>> speed,
			Amount<Area> surface, double CLmax, double cD0,
			double ar, double oswald, Amount<Angle> sweepHalfChord, 
			double tcMax, AirfoilTypeEnum airfoilType) {

		List<DragMap> list = new ArrayList<DragMap>();

		for(int k=0; k<weight.size(); k++) {
				for(int i=0; i<altitude.size(); i++) {

					list.add(new DragMap(weight.get(k), altitude.get(i), deltaTemperature,
							calculateDragVsSpeed(weight.get(k), altitude.get(i), deltaTemperature, 
									surface, cD0, ar, oswald, speed, sweepHalfChord, tcMax, airfoilType), 
									speed));
				}
		}

		return list;
	}

	/**
	 * @author Vittorio Trifari
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
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> weight, List<Amount<Velocity>> speed,
			Amount<Area> surface,
			double cLmax, double cD0, double ar,
			double oswald, Amount<Angle> sweepHalfChord, double tcMax, 
			AirfoilTypeEnum airfoilType) {

		return new DragMap(weight, altitude, deltaTemperature, 
				calculateDragVsSpeed(weight, altitude, deltaTemperature, 
						surface, cD0, ar, oswald, speed, sweepHalfChord, tcMax, airfoilType), 
						speed);
	}

	public static List<DragMap> calculateDragAndPowerRequired(
			List<Amount<Length>> altitude,
			Amount<Temperature> deltaTemperature,
			List<Double> phi,
			List<Amount<Mass>> weight,
			List<Amount<Velocity>> speed,
			Amount<Area> surface,
			double cLmax,
			double polarCL[],
			double polarCD[],
			Amount<Angle> sweepHalfChord,
			double tcMax, 
			AirfoilTypeEnum airfoilType
			) {

		List<DragMap> list = new ArrayList<DragMap>();

		for(int f=0; f<phi.size(); f++) {
			for(int i=0; i<altitude.size(); i++) {
				for(int k=0; k<weight.size(); k++) {

				list.add(new DragMap(
						weight.get(k),
						altitude.get(i),
						deltaTemperature,
						calculateDragVsSpeed(
								weight.get(k),
								altitude.get(i),
								deltaTemperature,
								surface,
								polarCL,
								polarCD,
								speed,
								sweepHalfChord,
								tcMax,
								airfoilType
								), 
						speed)
						);
				}
			}
		}

		return list;
	}
	
	/**
	 * This methods allows the user to evaluate required power and drag using a known polar curve
	 * given as input.
	 * 
	 * @author Vittorio Trifari
	 * @param altitude
	 * @param weight
	 * @param speed
	 * @param surface
	 * @param CLmax
	 * @param polarCL
	 * @param polarCD
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static DragMap calculateDragAndPowerRequired(
			Amount<Length> altitude, Amount<Temperature> deltaTemperature, Amount<Mass> weight, List<Amount<Velocity>> speed,
			Amount<Area> surface,
			double cLmax,
			double polarCL[],
			double polarCD[],
			Amount<Angle> sweepHalfChord,
			double tcMax, 
			AirfoilTypeEnum airfoilType) {

		return new DragMap(
				weight,
				altitude,
				deltaTemperature,
				calculateDragVsSpeed(
						weight,
						altitude,
						deltaTemperature,
						surface,
						polarCL,
						polarCD,
						speed,
						sweepHalfChord,
						tcMax,
						airfoilType
						), 
				speed
				);
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

	
	public static List<Double> calculateCDInducedParabolic(
			Double[] clWing, double aspectRatio, double oswaldFactor) {
		List<Double> cdInduced = new ArrayList<>();
		
		for (int i=0;i<clWing.length; i++){
			cdInduced.add(i, (clWing[i]*clWing[i])/(Math.PI * aspectRatio * oswaldFactor));
		}
		return cdInduced;
	}

	
	public static Map<String, Double> calculateMaximumEfficiency(double ar, double e, double cD0, double rho, double W, double S) {
		
		Map<String, Double> results = new HashMap<String, Double>();
		
		results.put("E_max", Math.sqrt(Math.PI*ar*e/(4*cD0)));
		results.put("CL_E", Math.sqrt(Math.PI*ar*e*cD0));
		results.put("CD_E", 2*cD0);
		results.put("Drag_E", W/results.get("E_max"));
		results.put("Speed_E", Math.sqrt(2*W/(rho*S*results.get("CL_E"))));
		
		return results;
	}

	public static Map<String, Double> calculateMinimumPower(double ar, double e, double cD0, double rho, double W, double S) {
		
		Map<String, Double> results = new HashMap<String, Double>();
		
		results.put("E_P", Math.sqrt(0.75)*Math.sqrt(Math.PI*ar*e/(4*cD0)));
		results.put("CL_P", Math.sqrt(3)*Math.sqrt(Math.PI*ar*e*cD0));
		results.put("CD_P", 4*cD0);
		results.put("Drag_P", (W/(Math.sqrt(Math.PI*ar*e/(4*cD0))))*2./Math.sqrt(3));
		results.put("Speed_P", (Math.sqrt(2*W/Math.sqrt(Math.PI*ar*e*cD0)))/Math.pow(3, 0.25));
		results.put("Power_P", W*results.get("Speed_P")/results.get("E_P"));
		results.put("Power_E", results.get("Power_P")*Math.pow(27, 0.25)/2.0);
		
		return results;
	}

	public static Map<String, Double> calculateMaximumRange(double ar, double e, double cD0, double rho, double W, double S) {
		
		Map<String, Double> results = new HashMap<String, Double>();
		
		results.put("E_A", Math.sqrt(0.75)*Math.sqrt(Math.PI*ar*e/(4*cD0)));
		results.put("CL_A", (Math.sqrt(Math.PI*ar*e*cD0))/Math.sqrt(3));
		results.put("CD_A", (4./3.)*cD0);
		results.put("Drag_A", (W/(Math.sqrt(Math.PI*ar*e/(4*cD0))))*2./Math.sqrt(3));
		results.put("Speed_A", (Math.sqrt(2*W/Math.sqrt(Math.PI*ar*e*cD0)))*Math.pow(3, 0.25));
		results.put("Power_A", Math.sqrt(3)*(W*((Math.sqrt(2*W/Math.sqrt(Math.PI*ar*e*cD0)))/Math.pow(3, 0.25))/Math.sqrt(0.75)*Math.sqrt(Math.PI*ar*e/(4*cD0))));
		
		return results;
	}
	
	public static List<Double> calculateParasiteDragLiftingSurfaceFromAirfoil (
			List<Amount<Angle>> alphasArray,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilCdMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Double> ClReferenceOfCdMatrix,// references Cl of the list of list airfoilCdMatrix
			List<Amount<Length>> chordDistribution,
			Amount<Area> surface,
			List<Amount<Length>> yDimensionalDistribution
			) 
			{
		
		List<Double> clReferenceOfCdMatrixCut = new ArrayList<>();
		for(int i=0; i<airfoilCdMatrix.get(0).size(); i++)
			clReferenceOfCdMatrixCut.add(ClReferenceOfCdMatrix.get(i));
		
		List<Double> parasiteDrag = new ArrayList<Double>();
	
		double [] cdDistributionAtAlpha, cCd, clDistributionfromNasaBlackwell;
		int numberOfPointSemiSpanWise = chordDistribution.size();
		
		for (int i=0; i<alphasArray.size(); i++){

			cdDistributionAtAlpha = new double [numberOfPointSemiSpanWise];
			cCd =  new double [numberOfPointSemiSpanWise];
			clDistributionfromNasaBlackwell = new double [numberOfPointSemiSpanWise];
			
			theNasaBlackwellCalculator.calculate(alphasArray.get(i));
			clDistributionfromNasaBlackwell =theNasaBlackwellCalculator.getClTotalDistribution().toArray();

			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				
				if(Double.isNaN(clDistributionfromNasaBlackwell[ii]))
					clDistributionfromNasaBlackwell[ii] = 0.0;
					
				List<Double> ClReeferenceCDMatrixNew = new ArrayList<>();
				
				 for(int iii=0; iii<ClReferenceOfCdMatrix.size()-1; iii++) {
					 if(ClReferenceOfCdMatrix.get(iii+1)-ClReferenceOfCdMatrix.get(iii)>0) {
						 ClReeferenceCDMatrixNew.add(iii, ClReferenceOfCdMatrix.get(iii));
				 }
					 else {
						 ClReeferenceCDMatrixNew.add(iii, ClReeferenceCDMatrixNew.get(iii-1)+ClReeferenceCDMatrixNew.get(iii-1)*0.001);
					 }
				 }
				
	        	 if(ClReferenceOfCdMatrix.get(ClReferenceOfCdMatrix.size()-1) > ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-2))
				 ClReeferenceCDMatrixNew.add(ClReferenceOfCdMatrix.size()-1,ClReferenceOfCdMatrix.get(ClReferenceOfCdMatrix.size()-1));
				 else
					 ClReeferenceCDMatrixNew.add(
							 ClReferenceOfCdMatrix.size()-1,
							 ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-1) +
							 ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-1)*0.001);
						 
					
				cdDistributionAtAlpha[ii] = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(ClReeferenceCDMatrixNew), 
						MyArrayUtils.convertToDoublePrimitive(airfoilCdMatrix.get(ii)), 
						clDistributionfromNasaBlackwell[ii]
						);		
				cCd[ii] = chordDistribution.get(ii).doubleValue(SI.METER) * cdDistributionAtAlpha[ii];
			}	

			parasiteDrag.add(
					i,
					(2/surface.doubleValue(SI.SQUARE_METRE))* MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistribution),
							cCd)
					);
		}
		return parasiteDrag;
	
			}
	
	public static List<Double> calculateInducedDragLiftingSurfaceFromAirfoil (List<Amount<Angle>> alphasArray,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			List<Amount<Length>> chordDistribution,
			Amount<Area> surface,
			List<Amount<Length>> yDimensionalDistribution,
			List<Double> clZeroDistribution,
			List<Double> clAlphaDegDistribution,
			List<List<Amount<Angle>>> inducedAngleOfAttackDistribution // the distribution of induced angle of attack varying on the list alphasArray
			) 
			{
		List<Double> inducedDrag = new ArrayList<Double>();
		
		double [] cdInducedDistributionAtAlpha, clInducedDistributionAtAlpha, cCd;
		double [] clInducedDistributionAtAlphaNew, alphaDistribution;
		
		int numberOfPointSemiSpanWise = chordDistribution.size();
		
		double minAlpha = MyArrayUtils.getMin(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(anglesOfAttackClMatrix)));

		for (int i=0; i<alphasArray.size(); i++){
			cdInducedDistributionAtAlpha = new double [numberOfPointSemiSpanWise];
			clInducedDistributionAtAlpha = new double [numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double [numberOfPointSemiSpanWise];
			alphaDistribution = new double [numberOfPointSemiSpanWise];
			cCd = new double [numberOfPointSemiSpanWise];
			
			theNasaBlackwellCalculator.calculate(alphasArray.get(i));
			clInducedDistributionAtAlpha = theNasaBlackwellCalculator.getClTotalDistribution().toArray();

			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				alphaDistribution [ii] = (clInducedDistributionAtAlpha[ii] - clZeroDistribution.get(ii))/
						clAlphaDegDistribution.get(ii);

				if(alphaDistribution [ii] < minAlpha){
					clInducedDistributionAtAlphaNew[ii] = 
							clAlphaDegDistribution.get(ii)*
							alphaDistribution [ii] + 
							clZeroDistribution.get(ii);
				}
				
				else{
				clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
						MyArrayUtils.convertToDoublePrimitive(
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										airfoilClMatrix.get(ii))),
						alphaDistribution[ii]
						);
				}
				cdInducedDistributionAtAlpha[ii] = clInducedDistributionAtAlphaNew[ii] * 
						Math.tan(inducedAngleOfAttackDistribution.get(i).get(ii).doubleValue(SI.RADIAN));
				
				
				cCd[ii] = chordDistribution.get(ii).doubleValue(SI.METER) * cdInducedDistributionAtAlpha[ii];
			}
			
			inducedDrag.add(
					i,
					(2/surface.doubleValue(SI.SQUARE_METRE)) * MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(yDimensionalDistribution),
							cCd)
					);
			
			
		}
		return inducedDrag;
			}	
	
	public static List<Double> calculateParasiteDragDistributionFromAirfoil (
			int numberOfPointSemiSpanWise,
			Amount<Angle> angleOfAttack,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilCdMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Double> ClReferenceOfCdMatrix// references Cl of the list of list airfoilCdMatrix
			){
		
		List<Double> parasiteDragDistribution = new ArrayList<Double>();
		
		double [] clDistributionfromNasaBlackwell = new double [numberOfPointSemiSpanWise];
		theNasaBlackwellCalculator.calculate(angleOfAttack);
		clDistributionfromNasaBlackwell =theNasaBlackwellCalculator.getClTotalDistribution().toArray();

		for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
			
			if(Double.isNaN(clDistributionfromNasaBlackwell[ii]))
				clDistributionfromNasaBlackwell[ii] = 0.0;
//			
//			if(ClReferenceOfCdMatrix.size() > airfoilCdMatrix.get(0).size()) 
//				parasiteDragDistribution.add(ii, MyMathUtils.getInterpolatedValue1DLinear(
//						MyArrayUtils.convertToDoublePrimitive(ClReferenceOfCdMatrix.subList(0, airfoilCdMatrix.get(ii).size())), 
//						MyArrayUtils.convertToDoublePrimitive(airfoilCdMatrix.get(ii)), 
//						clDistributionfromNasaBlackwell[ii]
//						));
//			else if(ClReferenceOfCdMatrix.size() < airfoilCdMatrix.get(0).size())
//				parasiteDragDistribution.add(ii, MyMathUtils.getInterpolatedValue1DLinear(
//						MyArrayUtils.convertToDoublePrimitive(ClReferenceOfCdMatrix), 
//						MyArrayUtils.convertToDoublePrimitive(airfoilCdMatrix.get(ii).subList(0, ClReferenceOfCdMatrix.size())), 
//						clDistributionfromNasaBlackwell[ii]
//						));
//			else

 			List<Double> ClReeferenceCDMatrixNew = new ArrayList<>();
			
			 for(int iii=0; iii<ClReferenceOfCdMatrix.size()-1; iii++) {
				 if(ClReferenceOfCdMatrix.get(iii+1)-ClReferenceOfCdMatrix.get(iii)>0) {
					 ClReeferenceCDMatrixNew.add(iii, ClReferenceOfCdMatrix.get(iii));
			 }
				 else {
					 ClReeferenceCDMatrixNew.add(iii, ClReeferenceCDMatrixNew.get(iii-1)+ClReeferenceCDMatrixNew.get(iii-1)*0.001);
				 }
			 }
			
        	 if(ClReferenceOfCdMatrix.get(ClReferenceOfCdMatrix.size()-1) > ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-2))
			 ClReeferenceCDMatrixNew.add(ClReferenceOfCdMatrix.size()-1,ClReferenceOfCdMatrix.get(ClReferenceOfCdMatrix.size()-1));
			 else
				 ClReeferenceCDMatrixNew.add(
						 ClReferenceOfCdMatrix.size()-1,
						 ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-1) +
						 ClReeferenceCDMatrixNew.get(ClReeferenceCDMatrixNew.size()-1)*0.001);
					 
				parasiteDragDistribution.add(ii, MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(ClReeferenceCDMatrixNew), 
						MyArrayUtils.convertToDoublePrimitive(airfoilCdMatrix.get(ii)), 
						clDistributionfromNasaBlackwell[ii]
						));
					
		}	
		
		
		return parasiteDragDistribution;
	}
	
	public static List<Double> calculateInducedDragDistribution (
			int numberOfPointSemiSpanWise,
			Amount<Angle> angleOfAttack,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			List<Double> clZeroDistribution,
			List<Double> clAlphaDegDistribution,
			List<Amount<Angle>> inducedAngleOfAttackDistribution // the distribution of induced angle of attack varying on the list alphasArray

			){
		
		List<Double> inducedDragDistribution = new ArrayList<Double>();
		
		double [] clDistributionfromNasaBlackwell = new double [numberOfPointSemiSpanWise];
		double [] clInducedDistributionAtAlphaNew = new double [numberOfPointSemiSpanWise];
		theNasaBlackwellCalculator.calculate(angleOfAttack);
		
		clDistributionfromNasaBlackwell =theNasaBlackwellCalculator.getClTotalDistribution().toArray();
		double [] alphaDistribution  = new double [numberOfPointSemiSpanWise];
		
		double minAlpha = MyArrayUtils.getMin(MyArrayUtils.convertDoubleArrayToListDouble(
				MyArrayUtils.convertListOfAmountToDoubleArray(anglesOfAttackClMatrix)));
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				
				if(Double.isNaN(clDistributionfromNasaBlackwell[ii]))
					clDistributionfromNasaBlackwell[ii] = 0.0;
				
				alphaDistribution [ii] = (clDistributionfromNasaBlackwell[ii] - clZeroDistribution.get(ii))/
						clAlphaDegDistribution.get(ii);

				if(alphaDistribution [ii] < minAlpha){
					clInducedDistributionAtAlphaNew[ii] = 
							clAlphaDegDistribution.get(ii)*
							alphaDistribution [ii] + 
							clZeroDistribution.get(ii);
				}
				
				else{
				clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
						MyArrayUtils.convertToDoublePrimitive(
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										airfoilClMatrix.get(ii))),
						alphaDistribution[ii]
						);
				}
				inducedDragDistribution.add(ii, clInducedDistributionAtAlphaNew[ii] * 
						Math.tan(inducedAngleOfAttackDistribution.get(ii).doubleValue(SI.RADIAN)));
				
			}

		
		return inducedDragDistribution;
	}
	
	public static List<Double> calculateTotalDragDistributionFromAirfoil (
			int numberOfPointSemiSpanWise,
			Amount<Angle> angleOfAttack,
			NasaBlackwell theNasaBlackwellCalculator,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			List<List<Double>> airfoilCdMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Double> ClReferenceOfCdMatrix,// references Cl of the list of list airfoilCdMatrix
			List<Double> clZeroDistribution,
			List<Double> clAlphaDegDistribution,
			List<Amount<Angle>> inducedAngleOfAttackDistribution // the distribution of induced angle of attack varying on the list alphasArray

			){
		List<Double> totalDragDistribution = new ArrayList<Double>();
		
		List<Double> parasiteDragDistribution = calculateParasiteDragDistributionFromAirfoil(
				numberOfPointSemiSpanWise, 
				angleOfAttack, 
				theNasaBlackwellCalculator, 
				airfoilCdMatrix,
				ClReferenceOfCdMatrix
				);
		
		List<Double> inducedDragDistribution = calculateInducedDragDistribution(
				numberOfPointSemiSpanWise, 
				angleOfAttack, 
				theNasaBlackwellCalculator, 
				airfoilClMatrix, 
				anglesOfAttackClMatrix, 
				clZeroDistribution, 
				clAlphaDegDistribution, 
				inducedAngleOfAttackDistribution
				);
		
		for (int i=0; i<numberOfPointSemiSpanWise; i++){
			totalDragDistribution.add(
					i,
					parasiteDragDistribution.get(i)+inducedDragDistribution.get(i));
		}
		
		
		return totalDragDistribution;
		
	}
	
	 /**
	  * @see NASA TN D-6800 (pag.139 pdf)
	  * 
	  * @param xStations ()
	  * @param alphaBody
	  * @param volume
	  * @param k2k1
	  * @param maxDiameter
	  * @param maxDiameterPosition
	  * @param length
	  * @param wingSurface
	  * @param outlineXZUpperCurveX
	  * @param outlineXZUpperCurveZ
	  * @param outlineXZLowerCurveX
	  * @param outlineXZLowerCurveZ
	  * @param outlineXYSideRCurveX
	  * @param outlineXYSideRCurveY
	  * @return @return the CDi of the fuselage or of the nacelle
	  */
	public static Double calculateCDInducedFuselageOrNacelle(
			List<Amount<Length>> xStations,
			Amount<Angle> alphaBody,
			Double currentMachNumber,
			Amount<Volume> volume,
			Double k2k1,
			Amount<Length> maxDiameter,
			Amount<Length> maxDiameterPosition,
			Amount<Length> length,
			Amount<Area> wingSurface,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {
		
		double eta = 0.0000000081*Math.pow(length.doubleValue(SI.METER)/maxDiameter.doubleValue(SI.METER),5)
				- 0.0000010400*Math.pow(length.doubleValue(SI.METER)/maxDiameter.doubleValue(SI.METER),4) 
				+ 0.0000549040*Math.pow(length.doubleValue(SI.METER)/maxDiameter.doubleValue(SI.METER),3) 
				- 0.0015946361*Math.pow(length.doubleValue(SI.METER)/maxDiameter.doubleValue(SI.METER),2) 
				+ 0.0296842457*(length.doubleValue(SI.METER)/maxDiameter.doubleValue(SI.METER)) 
				+ 0.5038353579;
		
		List<Amount<Length>> equivalentDiametersList = xStations.stream()
				.map(x -> FusNacGeometryCalc.calculateEquivalentDiameterAtX(
						x,
						outlineXZUpperCurveX, 
						outlineXZUpperCurveZ,
						outlineXZLowerCurveX, 
						outlineXZLowerCurveZ, 
						outlineXYSideRCurveX, 
						outlineXYSideRCurveY)
						)
				.collect(Collectors.toList()); 
		
		Amount<Area> maxCrossEquivalentArea = Amount.valueOf( 
				equivalentDiametersList.stream()
				.map(d -> Amount.valueOf(
						Math.pow(d.doubleValue(SI.METER)/2, 2)*Math.PI,
						SI.SQUARE_METRE
						))
				.mapToDouble(a -> a.doubleValue(SI.SQUARE_METRE))
				.max()
				.getAsDouble(),
				SI.SQUARE_METRE
				);
		
		Double x0 = (0.374 + 0.533*(maxDiameterPosition.doubleValue(SI.METER)/length.doubleValue(SI.METER)))*length.doubleValue(NonSI.FOOT);
		
		Double cDc = 49.844*Math.pow(currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN)), 6)
				- 148.56*Math.pow(currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN)), 5) 
				+ 162.32*Math.pow(currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN)), 4)
				- 80.611*Math.pow(currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN)), 3)
				+ 19.433*Math.pow(currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN)), 2) 
				- 1.8178*currentMachNumber*Math.sin(alphaBody.doubleValue(SI.RADIAN))
				+ 1.2014;
		
		double[] xStationsIntegral = MyArrayUtils.linspace(
				x0,
				length.doubleValue(NonSI.FOOT),
				50
				);
		double[] etaRcDcIntegral = new double[xStationsIntegral.length];
		for (int i = 0; i < etaRcDcIntegral.length; i++) 			
			etaRcDcIntegral[i] = eta*cDc*equivalentDiametersList.get(i).divide(2).doubleValue(NonSI.FOOT);
		
		Double integral = MyMathUtils.integrate1DSimpsonSpline(
				xStationsIntegral,
				etaRcDcIntegral
				);
		
		Double cDi = (
				(2*Math.pow(alphaBody.doubleValue(SI.RADIAN),2)*k2k1*maxCrossEquivalentArea.doubleValue(MyUnits.FOOT2)
						/Math.pow(volume.doubleValue(MyUnits.FOOT3), (2/3)))
				+ ((2*Math.pow(alphaBody.doubleValue(SI.RADIAN),3)/Math.pow(volume.doubleValue(MyUnits.FOOT3), (2/3)))*integral)
				)
				*(Math.pow(volume.doubleValue(MyUnits.FOOT3), (2/3))/wingSurface.doubleValue(MyUnits.FOOT2));
		
		return cDi;
		
	}
	
	public static List<Double> calculateTotalPolarFromEquation(
			List<Double> wingDragCoefficient,
			List<Double> horizontalTailDragCoefficientCurve,
			Double verticalTailDragCoefficient,
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			Amount<Area> verticalTailSurface,
			List<Double> fuselageDragCoefficient,
			List<Double> nacellesDragCoefficient,
			Double cD0LandingGear,
			Double cD0Miscellaneous,
			Double horizontalTailDynamicPressureRatio,
			List<Amount<Angle>> alphaBodyList
			){

		List<Double> cdTotalList = new ArrayList<>();
		
		//TOTAL DRAG CALCULATION
		alphaBodyList.stream().forEach( ab-> {

		int i = alphaBodyList.indexOf(ab);
					
		cdTotalList.add(
					wingDragCoefficient.get(i)+
					(horizontalTailDragCoefficientCurve.get(i)*horizontalTailDynamicPressureRatio*
							(horizontalTailSurface.doubleValue(SI.SQUARE_METRE)/wingSurface.doubleValue(SI.SQUARE_METRE)))+
					(verticalTailDragCoefficient*horizontalTailDynamicPressureRatio*
							(verticalTailSurface.doubleValue(SI.SQUARE_METRE)/wingSurface.doubleValue(SI.SQUARE_METRE)))+
					fuselageDragCoefficient.get(i)+
					nacellesDragCoefficient.get(i)+
					cD0LandingGear+
					cD0Miscellaneous);
				}
				);
	
		return cdTotalList;
	}
	
	public static List<Double> calculateTrimmedPolar(
			Map<Amount<Angle>, List<Double>> dragCoefficientHorizontalTailWithRespectToDeltaE,
			List<Amount<Angle>> equilibriumDeltaE,
			List<Amount<Angle>> deltaEVectorForEquilibrium,
			List<Amount<Angle>> alphaBodyList
			) {
		List<Double> trimmedPolar = new ArrayList<>();

		alphaBodyList.stream().forEach( ab-> {

			int i = alphaBodyList.indexOf(ab);
			List<Double> temporaryCD = new ArrayList<>();
			List<Double> temporaryCDFinal = new ArrayList<>();
			List<Amount<Angle>> temporaryDeltaE = new ArrayList<>();
			deltaEVectorForEquilibrium.stream().forEach( de-> 
			temporaryCD.add(dragCoefficientHorizontalTailWithRespectToDeltaE.get(de).get(i))
					);
			
			int ii=0;
			int lastIndexTemporaryCD = 0;
			while (ii < temporaryCD.size()-1) {
				if(temporaryCD.get(ii) > temporaryCD.get(ii+1)) {
					temporaryCDFinal.add(temporaryCD.get(ii));
					temporaryDeltaE.add(deltaEVectorForEquilibrium.get(ii));
					lastIndexTemporaryCD++;
				}
				ii++;
			}
			temporaryCDFinal.add(lastIndexTemporaryCD, temporaryCD.get(lastIndexTemporaryCD));
			temporaryDeltaE.add(deltaEVectorForEquilibrium.get(lastIndexTemporaryCD));

			ArrayUtils.reverse(MyArrayUtils.convertListOfDoubleToDoubleArray(temporaryCDFinal));
			ArrayUtils.reverse(MyArrayUtils.convertListOfAmountToDoubleArray(temporaryDeltaE));

			trimmedPolar.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(temporaryDeltaE),
							MyArrayUtils.convertToDoublePrimitive(temporaryCDFinal),
							equilibriumDeltaE.get(i).doubleValue(NonSI.DEGREE_ANGLE))
					);
		});


		return trimmedPolar;
	}
	
	public static List<Double> calculateTrimmedPolarV2(
			Double cD0VerticalTail,
			Double cD0Fuselage,
			Double cD0WingFuselageInterference,
			Double cD0Nacelle,
			Double cD0Cooling,
			Double cD0excrescences,
			Double cD0Wawe,
			Double cD0canard,
			Double cD0Wing,
			Double cD0HorizontalTail,
			Double cD0Flap,
			Double cD0LandingGear,
			List<Double> cLTotalTrimmed,
			Double wingAspectRatio,
			Double aircraftOswaldFactor
			){
		List<Double> trimmedPolar = new ArrayList<>();
		
		Double cD0TotalOtherSources = 
				cD0WingFuselageInterference +
				cD0Cooling +
				cD0excrescences +
				cD0Flap;
		
		
		Double cD0TotalLiftingSurfaces = cD0canard +
				cD0Wing +
				cD0HorizontalTail +
				cD0VerticalTail +
				cD0Fuselage +
				cD0Nacelle
				;
		
		cLTotalTrimmed.stream().forEach( cle-> {
		trimmedPolar.add(
				cD0TotalOtherSources + cD0TotalLiftingSurfaces + cD0Wawe + Math.pow(cle, 2)/(Math.PI * wingAspectRatio * aircraftOswaldFactor)
				);
		});
		
		return trimmedPolar;
	}
}
