package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AirfoilTypeEnum;
import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * A collection of Aerodynamic static functions
 * 
 * @author Lorenzo Attanasio
 */
public class AerodynamicCalc {

	private AerodynamicCalc() {}

	public static Double calculateSwetTotal(Double ... d) {
		// Approximately:
		return MyArrayUtils.sumArrayElements(d);
	}

	public static double calculatePrandtlGlauertCorrection(double mach) {
		return sqrt(1 - pow(mach,2));
	}
	
	public static double calculate3Deffect(double mach, double ar, double cLalpha2D) {
		double effMach = calculatePrandtlGlauertCorrection(mach);
		return effMach * ar / (cLalpha2D*57.3 / 2*Math.PI*effMach);
	}
	
	/** 
	 * Calculate friction coefficient
	 * 
	 * @author Lorenzo Attanasio
	 * @param reynolds
	 * @param mach
	 * @param xTransition
	 * @return
	 */
	public static double calculateCf(double reynolds, double mach, double xTransition) {
		return AerodynamicCalc.calculateCfLam(reynolds)*xTransition + AerodynamicCalc.calculateCfTurb(reynolds, mach)*(1-xTransition);
	}

	/**
	 * Calculate turbulent friction coefficient
	 * 
	 * @author Lorenzo Attanasio
	 * @param reynolds
	 * @param mach
	 * @return
	 */
	public static double calculateCfTurb(double reynolds, double mach) {
		return 0.455/(Math.pow(Math.log10(reynolds),2.58)*Math.pow(1+0.144*(Math.pow(mach,2)),0.65));
	}

	/**
	 * Behind ADAS pdf, beginning at page 63.
	 * 
	 * @param re
	 * @return
	 */
	public static double calculateCfLam(double re) {
		return 1.328/Math.sqrt(re);
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param mach
	 * @param machTransonicThreshold
	 * @param lenght
	 * @param k
	 * @return
	 */
	public static double calculateReCutOff(
			double mach, double machTransonicThreshold, 
			double lenght, double k){

		if (mach <= machTransonicThreshold) 
			return 38.21*Math.pow(lenght/k, 1.053);  //Reynolds  cut-off for wing   // ADAS pag 91. Subsonic case
		else 
			return 44.62*Math.pow(lenght/k,1.053)*Math.pow(mach, 1.16); // Transonic or supersonic case
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param mach
	 * @param machTransonicThreshold
	 * @param density
	 * @param tas
	 * @param mu
	 * @param lenght
	 * @param roughness
	 * @return
	 */
	public static double calculateReynoldsEffective(
			double mach, double machTransonicThreshold,
			double density, double tas, double mu,
			double lenght, double roughness){

		double re = calculateReynolds(density, tas, lenght, mu);

		if (calculateReCutOff(mach, machTransonicThreshold, lenght, roughness) < re) {
			re = calculateReCutOff(mach, machTransonicThreshold, lenght, roughness);
		}

		return re;
	}
	
	public static double calculateReynoldsEffective(
			double mach, double machTransonicThreshold,
			double altitude, double lenght, double roughness){

		double re = calculateReynolds(altitude, mach, lenght);

		if (calculateReCutOff(mach, machTransonicThreshold, lenght, roughness) < re) {
			re = calculateReCutOff(mach, machTransonicThreshold, lenght, roughness);
		}

		return re;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param density
	 * @param tas
	 * @param lenght
	 * @param mu
	 * @return
	 */
	public static double calculateReynolds(
			double density, double tas, double lenght,
			double mu) {
		return density*tas*lenght/mu;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param altitude
	 * @param mach
	 * @param lenght
	 * @return
	 */
	public static double calculateReynolds(double altitude, double mach, double lenght) {
		return calculateReynolds(AtmosphereCalc.getDensity(altitude), 
				mach*AtmosphereCalc.getSpeedOfSound(altitude), lenght, 
				calculateDynamicViscosity(AtmosphereCalc.getAtmosphere(altitude).getTemperature()));
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param temperature
	 * @param t0 (K)
	 * @param mu0 (Pa*s) grc.nasa.gov/WWW/BGH/Viscosity.html
	 * @param c (K)
	 * @return
	 */
	public static double calculateDynamicViscosity(double temperature, double t0, double mu0, double c) {
		return mu0*((t0 + c) / (temperature + c)) 
				* Math.pow(temperature/t0, 1.5);	
	}

	public static double calculateDynamicViscosity(double temperature) {
		return calculateDynamicViscosity(temperature, 288.166667, 17.33e-6, 110.4);
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param cL
	 * @param k
	 * @param sweepHalfChord
	 * @param tcMax
	 * @return
	 */
	public static double calculateMachCriticalKornMason(
			double cL, double k, double sweepHalfChord, double tcMax) {

		// Here _maxThicknessMean is meant in the free stream direction
		return k/cos(sweepHalfChord) - 0.108 
				- 0.1*cL/pow(cos(sweepHalfChord), 3)
				- tcMax/pow(cos(sweepHalfChord), 2);
	}

	/**
	 * 
	 * @param cL
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return
	 */
	public static double calculateMachCriticalKornMason(
			double cL, double sweepHalfChord, 
			double tcMax, AirfoilTypeEnum airfoilType) {
		double k = 0.95;
		if (airfoilType == AirfoilTypeEnum.CONVENTIONAL) k = 0.87;
		return calculateMachCriticalKornMason(cL, k, sweepHalfChord, tcMax);
	}

	
	public static double calculateMachCriticalKornMason(
			double cL, Amount<Angle> sweepHalfChord, 
			double tcMax, AirfoilTypeEnum airfoilType) {
		return calculateMachCriticalKornMason(cL, sweepHalfChord.doubleValue(SI.RADIAN), tcMax, airfoilType);
	}

	/**
	 * This static method allows users to calculate the crest critical Mach number using the 
	 * Kroo graph which adapts the Shevell graph for swept wing. From this graph the following
	 * equation has been derived (see CIORNEI, Simona: Mach Number, Relative Thickness, Sweep 
	 * and Lift Coefficient Of The Wing – An Empirical Investigation of Parameters and Equations.
	 * Hamburg University of Applied Sciences, Department of Automotive and Aeronautical 
	 * Engineering, Project, 2005). Furthermore a correction for the modern supercritical 
	 * airfoils have been added in order to make results more reliable.
	 * 
	 * @author Vittorio Trifari
	 * @param cL
	 * @param sweepHalfChord
	 * @param tcMax
	 * @param airfoilType
	 * @return m_cr the crest critical Mach number from Kroo equation (2001)
	 */
	public static double calculateMachCriticalKroo(
			double cL, Amount<Angle> sweepHalfChord,
			double tcMax, AirfoilTypeEnum airfoilType) {
		
		double y = cL/(Math.pow(cos(sweepHalfChord.doubleValue(SI.RADIAN)),2));
		double x = tcMax/(Math.cos(sweepHalfChord.doubleValue(SI.RADIAN)));
		
		double machCr = ((2.8355*Math.pow(x, 2)) - (1.9072*x) + 0.9499 - (0.2*y) + (0.4262*x*y)) /
				      (Math.cos(sweepHalfChord.getEstimatedValue()) );
		
		// this method work for peaky airfoils; for modern supercritical some corrections
		// have to be made.
		if (airfoilType.equals(AirfoilTypeEnum.SUPERCRITICAL))
			machCr += 0.035;
		else if (airfoilType.equals(AirfoilTypeEnum.MODERN_SUPERCRITICAL))
			machCr += 0.06;
		
		return machCr;
	}

	/**
	 * Evaluate oswald factor with Howe method
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 7 DLR pdf
	 * 
	 * @param lambda
	 * @param ar
	 * @param tc
	 * @param phi25
	 * @param ne
	 * @param mach
	 * @return
	 */
	public static double calculateOswaldHowe(
			double lambda, double ar, double tc, 
			double phi25, double ne, double mach) {

		double f = 0.005 * (1 + 1.5*Math.pow(lambda-0.6, 2));

		return 1./ ( (1+0.12*Math.pow(mach,2)) 
						* (1 + (0.142 + f * ar * Math.pow(10*tc, 0.33))/Math.pow(Math.cos(phi25), 2) +
								(0.1*(3*ne + 1))/Math.pow(4+ar, 0.8) ));
	}

	/**
	 * Evaluate oswald factor with DLR method (entire aircraft)
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 9 DLR pdf
	 * 
	 * @param taperRatioOpt
	 * @param arW
	 * @param bW
	 * @param dihedralMean
	 * @param wingletHeight
	 * @param fuselageMaxDiam
	 * @param typeVehicle
	 * @param mach
	 * @return
	 */
	public static double calculateOswaldDLR(
			Aircraft theAircraft,
			double mach) {

		LiftingSurface theWing = theAircraft.getWing();
		Fuselage theFuselage = theAircraft.getFuselage();
		AircraftTypeEnum typeVehicle = theAircraft.getTypeVehicle();
		
		double ae = -0.001521, be = 10.82, f, oswald,
				kef, e_theo, keD0, 
				lambda_opt,
				delta_lambda, keM = 1.;

		lambda_opt = 0.45
				*Math.pow(
						Math.E,
						theWing.getSweepQuarterChordEquivalent(false).doubleValue(SI.RADIAN)
						);
		delta_lambda = -0.357 + lambda_opt;
		f = 0.0524*Math.pow(lambda_opt - delta_lambda,4) 
				- 0.15*Math.pow(lambda_opt - delta_lambda,3) 
				+ 0.1659*Math.pow(lambda_opt - delta_lambda, 2) 
				- 0.0706*(lambda_opt - delta_lambda)
				+ 0.0119;

		e_theo = 1/(f*theWing.getAspectRatio());

		kef = 1 - (2*
				(Math.pow(
						theFuselage.getSectionHeight().divide(theWing.getSpan()).getEstimatedValue(), 2)
						)
				);

		switch(typeVehicle) {
		case JET : keD0 = 0.873; break;
		case BUSINESS_JET : keD0 = 0.864; break;
		case TURBOPROP: keD0 = 0.804; break;
		case GENERAL_AVIATION: keD0 = 0.804; break;
		case FIGHTER: keD0 = 0.8; break; // ???
		default: keD0 = 0.8; break;
		}

		if (mach > 0.3) {
			keM = ae*Math.pow((mach/0.3 - 1), be) + 1;
		}

		oswald = e_theo*kef*keD0*keM;

		// Kroo method: needs whole aircraft CD0 
		//			double Q = 1/(e_theo*kef), P = 0.38*CD0;
		//			double eKroo = keM/(Q + P*_AR);

		double kWL = 2.83;

		oswald = oswald*Math.pow(1+(2./kWL)*
				(theWing.getLiftingSurfaceCreator().getWingletHeight().divide(theWing.getSpan()).getEstimatedValue()),2);

		double keGamma = Math.pow(
				Math.cos(theWing.getLiftingSurfaceCreator().getDihedralMean().doubleValue(SI.RADIAN)),
				-2);
		//			double keGamma = Math.pow((1 + (1/kWL)*(1/Math.cos(_dihedral) - 1)),2);
		double eWingletGamma = oswald*keGamma;
		return eWingletGamma;
	}

	/**
	 * Evaluate oswald factor with Grosu method
	 * 
	 * @author Lorenzo Attanasio
	 * @param tc
	 * @param arW
	 * @param cL
	 * @return
	 */
	public static double calculateOswaldGrosu(double tc, double arW, double cL) {
		// page 3 DLR pdf
		return 1/(1.08 + (0.028*tc/Math.pow(cL,2))*Math.PI*arW);
	}

	/**
	 * Evaluate oswald factor with Raymer method
	 * 
	 * @author Lorenzo Attanasio
	 * @param sweepLEEquivalent
	 * @param arW
	 * @return
	 */
	public static double calculateOswaldRaymer(double sweepLEEquivalent, double arW) {
		if (sweepLEEquivalent > 5*Math.PI/180.){
			return 4.61*(1 - 0.045
					* Math.pow(arW,0.68)) 
					* Math.pow(Math.cos(sweepLEEquivalent), 0.15) 
					- 3.1;
		} 

		return 1.78*(1 - 0.045*Math.pow(arW, 0.68)) - 0.64;
	}

	public static Double calculateRoughness(double cd0) {
		return cd0*0.06;
	}

	public static Double calculateCoolings(double cd0) {
		return cd0*0.08;
	}

	/**
	 * This method evaluates the downwash gradient using Roskam method
	 * 
	 * @author Manuela Ruocco

	 */
	public static Double calculateDownwashRoskam(double aspectRatio, double taperRatio, double adimensionalHorizontalDistance, 
			double adimensionalVetricalDistance, Amount<Angle> sweepQuarterChord) {

		double ka = (1/aspectRatio)-(1/(1+Math.pow(aspectRatio, 1.7)));
		double kL = (10-3*taperRatio)/7;
		double kH = (
				1-adimensionalVetricalDistance) /
				Math.cbrt(
						2*adimensionalHorizontalDistance);

		Double downwashGradient =4.44* Math.pow(
				(ka*kL*kH*Math.sqrt(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)))), 
				1.19);
		return downwashGradient;
	}

	/**
	 * This method evaluates the downwash gradient using Roskam method
	 * 
	 * @author Manuela Ruocco

	 */
	public static Double calculateDownwashRoskamWithMachEffect(double aspectRatio, double taperRatio, double adimensionalHorizontalDistance, 
			double adimensionalVetricalDistance, Amount<Angle> sweepQuarterChord, double clAlphaMachZero, double clAlpha) {

		double machCorrection = clAlpha/clAlphaMachZero;
		Double downwashGradientMachZero = calculateDownwashRoskam(
				aspectRatio,
				taperRatio, 
				adimensionalHorizontalDistance,
				adimensionalVetricalDistance, 
				sweepQuarterChord
				);

		Double downwashGradientMach = machCorrection * downwashGradientMachZero;
		return downwashGradientMach;
	}
	/**
	 * This method evaluates the distribution of the induced angle of attack
	 * 
	 * @author Manuela Ruocco

	 */
	public static List<Amount<Angle>> calculateInducedAngleOfAttackDistribution(
			Amount<Angle> angleOfAttack,
			NasaBlackwell theNasaBlackwellCalculator,
			Amount<Length> altitude,
			Double machNumber,
			int _numberOfPointSemiSpan
			){

		double [] addend = new double[_numberOfPointSemiSpan];
		List<Amount<Angle>> inducedAngleOfAttack = new ArrayList<>();
		double [] verticalVelocity = new double[_numberOfPointSemiSpan];
		double summ = 0;
		int lowerLimit = 0, upperLimit=(_numberOfPointSemiSpan-1);

		theNasaBlackwellCalculator.calculate(angleOfAttack);
		theNasaBlackwellCalculator.calculateVerticalVelocity(angleOfAttack);
		double [][] influenceFactor = theNasaBlackwellCalculator.getInfluenceFactor();
		double [] gamma = theNasaBlackwellCalculator.getGamma();

		StdAtmos1976 _atmosphereCruise = new StdAtmos1976(altitude.doubleValue(SI.METER));
		Amount<Velocity> Tas = Amount.valueOf(machNumber * _atmosphereCruise.getSpeedOfSound(), SI.METERS_PER_SECOND);

		for (int i=0 ; i<_numberOfPointSemiSpan; i++){
			for (int j = 0; j<_numberOfPointSemiSpan; j++){

				addend[j] =  gamma [j] * influenceFactor [i][j];

				summ = MyMathUtils.summation(lowerLimit, upperLimit, addend);
			}
			verticalVelocity [i]= (1/(4*Math.PI)) * (summ*0.3048);

			inducedAngleOfAttack.add(i, Amount.valueOf(
					Math.atan(verticalVelocity[i]/Tas.doubleValue(SI.METERS_PER_SECOND))*57.3/2,NonSI.DEGREE_ANGLE));

		}
		return inducedAngleOfAttack;
	}
	
	
	public static List<Double> calcCenterOfPressureDistribution(
			NasaBlackwell theNasaBlackwellCalculator,
			Amount<Angle> angleOfAttack,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmACDistribution,
			List<Double> liftingSurfaceXACadimensionalDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix// references angle of attack of the list of list airfoilClMatrix
			){

		List<Double> cpDistribution = new ArrayList<>();

		double[]  clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew;

		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();
			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(angleOfAttack);
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();

			for (int ii=0; ii<numberOfPointSemiSpanWise-1; ii++){
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<angleOfAttack.doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
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
				cpDistribution.add(ii,liftingSurfaceXACadimensionalDistribution.get(ii) -
						(liftingSurfaceCmACDistribution.get(ii)/
								clInducedDistributionAtAlphaNew[ii]));
			}
			cpDistribution.add(numberOfPointSemiSpanWise-1,0.0);
	return cpDistribution;
			}}
			

