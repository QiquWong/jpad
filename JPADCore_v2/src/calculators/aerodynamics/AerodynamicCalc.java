package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.stat.regression.SimpleRegression;
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
						theWing.getSweepQuarterChordEquivalent().doubleValue(SI.RADIAN)
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

	/**
	 * Estimates the oswald factor from the real polar curve using least square method to find the slope of 
	 * CD(CL^2)
	 * 
	 * @author Vittorio Trifari
	 * @param polarCL
	 * @param polarCD
	 * @param aspectRatio
	 * @return the oswald factor
	 */
	public static Double estimateOswaldFactorFormAircraftDragPolar(Double[] polarCL, Double[] polarCD, Double aspectRatio) {

		if(polarCL.length != polarCD.length) {
			System.err.println("POLAR CL AND CD MUST HAVE THE SAME LENGTH !!");
			return null;
		}

		Double[] squareCL = MyArrayUtils.convertListOfDoubleToDoubleArray(
				Arrays.stream(polarCL).map(cL -> Math.pow(cL, 2)).collect(Collectors.toList())
				);

		SimpleRegression sr = new SimpleRegression(true);
		for(int i=0; i<squareCL.length; i++) 
			sr.addData(squareCL[i], polarCD[i]);

		double slope = sr.getSlope();

		return 1/(aspectRatio*Math.PI*slope);

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
	 * This method evaluates the variable downwash gradient using Roskam method
	 * 
	 * @author Manuela Ruocco

	 */
	public static List<Double> calculateVariableDownwashRoskam( // it returns downwash gradient list
			double aspectRatio,
			double taperRatio,
			Amount<Length> zApexWing,
			Amount<Length> zApexHTail,
			Amount<Angle> iw,
			Amount<Angle> alphaZeroLiftWing,
			Amount<Length> horizontalDistanceInitial, 
			Amount<Length> verticalDistanceInitial,
			Amount<Angle> sweepQuarterChord,
			List<Amount<Angle>> alphasBody) {

		// constant values (ka, kl)
		double ka = (1/aspectRatio)-(1/(1+Math.pow(aspectRatio, 1.7)));
		double kL = (10-3*taperRatio)/7;
		//---------------------------------
		// variable value (kh)	

		Amount<Angle> startingAngle = 
		          iw.to(SI.RADIAN)
		          	.minus(alphaZeroLiftWing.to(SI.RADIAN));

		// Alpha Absolute array 
		double alphaFirst = 0.0;
		double alphaLast = 40.0;
		int nValue = 100;

		double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue); //deg
		double [] alphaWingArray =  new double [alphaAbsoluteArray.length]; //deg
		for(int i=0; i< alphaAbsoluteArray.length; i++){
			alphaWingArray[i] = alphaAbsoluteArray[i] + alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE); 
		}
		double deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue(); // rad


		Amount<Length> zDistanceZero = null;
		Amount<Length> xDistanceZero = horizontalDistanceInitial;
		
		if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

			zDistanceZero = Amount.valueOf(
					verticalDistanceInitial.doubleValue(SI.METER) + (
							(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

			zDistanceZero = Amount.valueOf(
					verticalDistanceInitial.doubleValue(SI.METER) - (
							(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		zDistanceZero = Amount.valueOf(
				zDistanceZero.doubleValue(SI.METER) * 
				Math.cos(startingAngle.doubleValue(SI.RADIAN)), SI.METER);


		double kH = (
				1-zDistanceZero.doubleValue(SI.METER)) /
				Math.cbrt(
						2*horizontalDistanceInitial.doubleValue(SI.METER));

		//initializing array
		double [] downwashArray = new double [nValue]; //deg
		double [] downwashGradientArray = new double [nValue];
		double [] alphaBodyArray = new double [nValue];
		double [] zDistanceArray = new double [nValue];
		double [] xDistanceArray = new double [nValue];	

		// First step

		zDistanceArray[0] = zDistanceZero.doubleValue(SI.METER);
		xDistanceArray[0] = xDistanceZero.doubleValue(SI.METER);
		downwashGradientArray[0] = 4.44* Math.pow(
				(ka*kL*kH*Math.sqrt(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)))), 
				1.19); 
		
		downwashArray[0] = 0.0;
		
		alphaBodyArray[0] = alphaAbsoluteArray[0] 
				- iw.doubleValue(NonSI.DEGREE_ANGLE)
				+ alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE);
	
		
		// Other steps
		double epsilonTemp = 0.0; //deg
		double downwashGradientTemp = 0.0;
		double zTemp = 0.0; //meter
		
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
			epsilonTemp = downwashArray[i-1];
			int ii=0;
			alphaBodyArray[i] = 
					alphaAbsoluteArray[i] 
							- iw.doubleValue(NonSI.DEGREE_ANGLE) 
							+ alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE);
			
			while(ii<3){ // ?
			//distance
			if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp)));
			}

			if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp)));
			}

			zTemp = 
					zTemp * 
					Math.cos(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp);
			
			
			//downwash gradient
			downwashGradientTemp = 4.44* Math.pow(
					(ka*kL*kH*Math.sqrt(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)))), 
					1.19); 
			
			//downwash angle
			epsilonTemp = downwashArray[i-1] +  Math.toDegrees(downwashGradientTemp*deltaAlpha); //deg 
			ii++;
			}
			//-----
			
			downwashGradientArray[i] = downwashGradientTemp;
			downwashArray[i] = epsilonTemp;
			zDistanceArray[0] = zTemp;
			xDistanceArray[0] = xDistanceZero.doubleValue(SI.METER);
		}
			
	// interpolating function
		List<Double> downwashGradientList = new ArrayList<>();
		
			
		downwashGradientList = MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
					alphaBodyArray,
					downwashGradientArray,
					MyArrayUtils.convertListOfAmountTodoubleArray(alphasBody)
					));
	
		
		return downwashGradientList;
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
	 * This method evaluates the variable downwash gradient using Roskam method
	 * 
	 * @author Manuela Ruocco

	 */
	public static List<Double> calculateVariableDownwashGradientRoskamWithMachEffect(double aspectRatio, 
			double taperRatio, 
			Amount<Length> zApexWing,
			Amount<Length> zApexHTail,
			Amount<Angle> iw,
			Amount<Angle> alphaZeroLiftWing,
			Amount<Length> horizontalDistanceInitial, 
			Amount<Length> verticalDistanceInitial, 
			Amount<Angle> sweepQuarterChord, 
			double clAlphaMachZero,
			double clAlpha,
			List<Amount<Angle>> alphasBody) {

		double machCorrection = clAlpha/clAlphaMachZero;
		List<Double> downwashGradientMach = new ArrayList<>();
		
		List<Double> downwashGradientMachZero = calculateVariableDownwashRoskam(
				aspectRatio,
				taperRatio, 
				zApexWing,
				zApexHTail,
				iw,
				alphaZeroLiftWing,
				horizontalDistanceInitial,
				verticalDistanceInitial, 
				sweepQuarterChord,
				alphasBody
				);

		for (int i=0; i<alphasBody.size(); i++){
		downwashGradientMach.set(i,
				machCorrection * downwashGradientMachZero.get(i));
		}
		return downwashGradientMach;
	}
	
	public static Amount<Angle> calculateDownwashAngleLinearSlingerland(
			Double rHorizontalDistance,
			Double mVerticalDistance,
			Double cl,
			Amount<Angle> sweepQuarterChord,
			double aspectRatio,
			Amount<Length> semispanWing){

		double keGamma, keGammaZero;

		double rPow=Math.pow(rHorizontalDistance/semispanWing.doubleValue(SI.METER),2);
		double mpow=Math.pow(mVerticalDistance/semispanWing.doubleValue(SI.METER), 2);

		keGamma=(0.1124+0.1265*sweepQuarterChord.doubleValue(SI.RADIAN)+0.1766*Math.pow(sweepQuarterChord.doubleValue(SI.RADIAN),2))
				/rPow+0.1024/(rHorizontalDistance/semispanWing.doubleValue(SI.METER))+2;
		keGammaZero=0.1124/rPow+0.1024/(rHorizontalDistance/semispanWing.doubleValue(SI.METER))+2;

		double kFraction=keGamma/keGammaZero;
		double first= ((rHorizontalDistance/semispanWing.doubleValue(SI.METER))/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));

		double downwashAngleLinearatZ=kFraction*(first+second*third)*((cl/(Math.PI*aspectRatio)));

		return Amount.valueOf(downwashAngleLinearatZ, SI.RADIAN);

	}

	public static List<Amount<Angle>> calculateDownwashAngleNonLinearSlingerland(
			Amount<Angle> iw,
			Amount<Length> zApexWing,
			Amount<Length> zApexHTail,
			Amount<Angle> alphaZeroLiftWing,
			Amount<Angle> sweepQuarterChordWing,
			Double aspectRatio,
			Amount<Length> wingSemiSpan, 
			Amount<Length> horizontalDistanceInitial,
			Amount<Length> verticalDistanceInitial,
			double [] clArray,
			double [] alphasWing, // deg
			double [] alphaBody   //deg
			){
		Amount<Angle> startingAngle = 
				iw.to(SI.RADIAN)
				.minus(alphaZeroLiftWing.to(SI.RADIAN));

		// Alpha Absolute array 
		double alphaFirst = 0.0;
		double alphaLast = 40.0;
		int nValue = 100;

		double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue); //deg
		double [] alphaWingArray =  new double [alphaAbsoluteArray.length]; //deg
		for(int i=0; i< alphaAbsoluteArray.length; i++){
			alphaWingArray[i] = alphaAbsoluteArray[i] + alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE); 
		}
		double deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue(); // rad

		Double[] cLArray = MyMathUtils.getInterpolatedValue1DLinear(
				alphasWing,
				clArray, 
				alphaWingArray);


		// calculate first values
		Amount<Length> zDistanceZero = null;
		Amount<Length> xDistanceZero = horizontalDistanceInitial; 

		if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

			zDistanceZero = Amount.valueOf(
					verticalDistanceInitial.doubleValue(SI.METER) + (
							(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

			zDistanceZero = Amount.valueOf(
					verticalDistanceInitial.doubleValue(SI.METER) - (
							(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		zDistanceZero = Amount.valueOf(
				zDistanceZero.doubleValue(SI.METER) * 
				Math.cos(startingAngle.doubleValue(SI.RADIAN)), SI.METER);

		double epsilonTemp, downwashArrayTemp;
		double zTemp = 0;

		// Initialize Array

		double [] downwashArray = new double [nValue];
		double [] alphaBodyArray = new double [nValue];
		double [] zDistanceArray = new double [nValue];
		double [] xDistanceArray = new double [nValue];	

		// First step

		zDistanceArray[0] = zDistanceZero.doubleValue(SI.METER);
		xDistanceArray[0] = xDistanceZero.doubleValue(SI.METER);
		downwashArray[0] = calculateDownwashAngleLinearSlingerland(
				xDistanceArray[0],
				zDistanceArray[0], 
				cLArray[0], 
				sweepQuarterChordWing,
				aspectRatio,
				wingSemiSpan
				).doubleValue(SI.RADIAN);

		alphaBodyArray[0] = alphaAbsoluteArray[0] 
				- iw.doubleValue(NonSI.DEGREE_ANGLE)
				+ alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE);

		// Other step
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){

			epsilonTemp = downwashArray[i-1];

			if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp)));
			}

			if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp)));
			}

			zTemp = 
					zTemp * 
					Math.cos(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + epsilonTemp);


			downwashArrayTemp = calculateDownwashAngleLinearSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zTemp, 
					cLArray[i], 
					sweepQuarterChordWing,
					aspectRatio,
					wingSemiSpan
					).doubleValue(SI.RADIAN);

			downwashArray[i] =  downwashArrayTemp;

			if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i])));
			}

			if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i])));
			}

			zDistanceArray[i] =
					zDistanceArray[i] * 
					Math.cos(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i]);

			downwashArray[i] = calculateDownwashAngleLinearSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zDistanceArray[i],
					cLArray[i], 
					sweepQuarterChordWing,
					aspectRatio,
					wingSemiSpan
					).doubleValue(SI.RADIAN);

			if (zApexWing.doubleValue(SI.METER) < zApexHTail.doubleValue(SI.METER)){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i])));
			}

			if (zApexWing.doubleValue(SI.METER) >= zApexHTail.doubleValue(SI.METER)){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i])));
			}

			zDistanceArray[i] =
					zDistanceArray[i] * 
					Math.cos(startingAngle.doubleValue(SI.RADIAN)- i * deltaAlpha + downwashArray[i]);

			xDistanceArray[i] = xDistanceZero.doubleValue(SI.METER);
			downwashArray[i] = calculateDownwashAngleLinearSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zDistanceArray[i],
					cLArray[i], 
					sweepQuarterChordWing,
					aspectRatio,
					wingSemiSpan
					).doubleValue(SI.RADIAN);

			alphaBodyArray[i] = 
					alphaAbsoluteArray[i] 
							- iw.doubleValue(NonSI.DEGREE_ANGLE) 
							+ alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE);
		}

		Double[] downwashArrayTemporary = MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				downwashArray,
				alphaBody
				);

		int k=0, j=0;

		while ( alphaBodyArray[0] > alphaBody[j]){
			j++;
		}

		double gradientTemporary = (downwashArrayTemporary[j+2]-downwashArrayTemporary[j+1])/( alphaBodyArray[j+2]- alphaBodyArray[j+1]);

		while ( alphaBodyArray[0] > alphaBody[k]){
			downwashArrayTemporary[k] = gradientTemporary*(alphaBodyArray[k]-alphaBodyArray[j+1])+downwashArrayTemporary[j+1];
			k++;
		}
		
		List<Amount<Angle>> downwashAngle = new ArrayList<>();
		
		for (int i=0; i<alphaBody.length; i++)
			downwashAngle.add(Amount.valueOf(downwashArrayTemporary[i]*57.3,NonSI.DEGREE_ANGLE));
		
		return downwashAngle;
		
	}
	
	public static List<Amount<Angle>> calculateDownwashAngleFromDownwashGradient(
			List<Double> downwashGradientList,
			List<Amount<Angle>> alphasBodyList,
			Amount<Angle> iw,
			Amount<Angle> alphaZeroLiftWing
			){
		
		List<Amount<Angle>> downwashAngleTemp = new ArrayList<>();
		// Alpha Absolute array 
		double alphaFirst = 0.0;
		double alphaLast = 40.0;
		int nValue = 100;
		

		double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue); //deg
		double [] alphaWingArray =  new double [alphaAbsoluteArray.length]; //deg
		double [] alphaBodyArray =  new double [alphaAbsoluteArray.length]; //deg
		List<Double> downwashGradientInterp = new ArrayList<>();
		
		for(int i=0; i< alphaAbsoluteArray.length; i++){
			alphaWingArray[i] = alphaAbsoluteArray[i] + alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE); 
			alphaBodyArray[i] = 
					alphaAbsoluteArray[i] 
							- iw.doubleValue(NonSI.DEGREE_ANGLE) 
							+ alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE);
		}
		double deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue(); // rad

		downwashGradientInterp = MyArrayUtils.convertDoubleArrayToListDouble(
				MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(alphasBodyList),
					MyArrayUtils.convertToDoublePrimitive(downwashGradientList),
				    alphaBodyArray
					));
	
		
		// first value
		downwashAngleTemp.set(0, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
		// other values
		for (int i=0; i<alphaAbsoluteArray.length; i++){
		downwashAngleTemp.set(i, Amount.valueOf(
				downwashAngleTemp.get(i-1).doubleValue(NonSI.DEGREE_ANGLE) + 
				downwashGradientInterp.get(i)*deltaAlpha, 
				NonSI.DEGREE_ANGLE));
		}
		
		List<Amount<Angle>> downwashAngle = new ArrayList<>();
		
		downwashAngle = MyArrayUtils.convertDoubleArrayToListOfAmount(
			MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				MyArrayUtils.convertListOfAmountTodoubleArray(downwashAngleTemp),
				MyArrayUtils.convertListOfAmountTodoubleArray(alphasBodyList)
				), NonSI.DEGREE_ANGLE);
		
		return downwashAngle;
	}
	
	public static List<Amount<Length>> calculateVortexPlaneHorizontalTailVerticalDistance (
			Amount<Angle> iw,
			Amount<Angle> alphaZeroLiftWing,
			Amount<Length> horizontalDistanceInitial,
			Amount<Length> verticalDistanceInitial,
			List<Amount<Angle>> alphaBody,       // deg
			List<Amount<Angle>> downwashAngles  // deg
			) {
		
		if(alphaBody.size() != downwashAngles.size()) {
			System.err.println("ALPHA BODY AND DOWNWASH ARRAYS MUST HAVE THE SAME LENGTH !!!");
			return null;
		}
		
		List<Amount<Angle>> absoluteAlphas = new ArrayList<>();
		for(int i=0; i<alphaBody.size(); i++) {
			absoluteAlphas.add(
					Amount.valueOf(
							alphaBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) 
									+ iw.doubleValue(NonSI.DEGREE_ANGLE) 
									- alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE),
									NonSI.DEGREE_ANGLE
							)
					);
		}
		
		List<Amount<Length>> verticalDistanceList = new ArrayList<>();
		for(int i=0; i<alphaBody.size(); i++) {
			verticalDistanceList.add(
					Amount.valueOf(
							verticalDistanceInitial.doubleValue(SI.METER)
							- (horizontalDistanceInitial.doubleValue(SI.METER)
									* Math.tan(
											iw.doubleValue(SI.RADIAN)
											- alphaZeroLiftWing.doubleValue(SI.RADIAN)
											- absoluteAlphas.get(i).doubleValue(SI.RADIAN)
											+ downwashAngles.get(i).doubleValue(SI.RADIAN)
											)
									),
							SI.METER
							)
					);
		}
		
		return verticalDistanceList;
		
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
			List<Double> liftingSurfaceCLforCMMatrix,
			List<List<Double>> liftingSurfaceCmACDistribution,
			List<Double> liftingSurfaceXACadimensionalDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix// references angle of attack of the list of list airfoilClMatrix
			){

		List<Double> cpDistribution = new ArrayList<>();

		double[]  clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew;
		double cmActual;
		
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
			
			cmActual = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCLforCMMatrix),
					MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCmACDistribution.get(ii)),
					clInducedDistributionAtAlphaNew[ii]
					);
			
			cpDistribution.add(ii,0.25-
					(cmActual/
					clInducedDistributionAtAlphaNew[ii]));

		}
		cpDistribution.add(numberOfPointSemiSpanWise-1,0.0);
		return cpDistribution;
	}

	public static Double calculateDynamicPressureRatio(Double positionRelativeToAttachment){
		Double dynamicPressureRatio = null;
		double [] dynamicPressureRatioValues = {0.85, 0.95, 1};
		double [] PositionValues = {0.0, 0.5, 1};

		dynamicPressureRatio = MyMathUtils.getInterpolatedValue1DLinear(
				PositionValues, 
				dynamicPressureRatioValues,
				positionRelativeToAttachment);

		return dynamicPressureRatio;
	}

	public static List<Amount<Angle>> calculateDeltaEEquilibrium (
			Map<Amount<Angle>, List<Double>> liftCoefficientHorizontalTailWithRespectToDeltaE,
			List<Amount<Angle>> deltaEForEquilibrium,
			List<Double> cLEquilibriumHorizontalTail,
			List<Amount<Angle>> alphaBodyList
			)
	{
		List<Amount<Angle>> deltaEEquilibrium = new ArrayList<>();

		alphaBodyList.stream().forEach( ab-> {

			int i = alphaBodyList.indexOf(ab);
			List<Double> temporaryCL = new ArrayList<>();
			List<Double> temporaryCLFinal = new ArrayList<>();
			List<Amount<Angle>> temporaryDeltaE = new ArrayList<>();
			deltaEForEquilibrium.stream().forEach( de-> {
				temporaryCL.add(liftCoefficientHorizontalTailWithRespectToDeltaE.get(de).get(i));
				for (int ii=0; ii<temporaryCL.size()-1 ; ii++){
					if(temporaryCL.get(ii) < temporaryCL.get(ii+1))
					{
						temporaryCLFinal.add(temporaryCL.get(ii));
						temporaryDeltaE.add(deltaEForEquilibrium.get(ii));
					}
				}
			});
			deltaEEquilibrium.add(
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(temporaryCLFinal),
									MyArrayUtils.convertListOfAmountTodoubleArray(temporaryDeltaE),
									cLEquilibriumHorizontalTail.get(i)),
							NonSI.DEGREE_ANGLE)
					);
		});


		return deltaEEquilibrium;
	}
}


