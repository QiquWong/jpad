package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.MathArrays;
import org.jscience.physics.amount.Amount;

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcAlpha0L;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;


public class LiftCalc {

	private LiftCalc() {}

	/**
	 * Helmbold-Diederich formula
	 * 
	 * @param arVertical
	 * @param sweepV_c2 (deg)
	 * @param eff3D
	 * @param effMach
	 * @return vertical tail lift coefficient slope (1/rad)
	 */
	public static double calculateCLalphaHelmboldDiederich(double ar, double cLalpha2D, double sweepV_c2, double mach) {

		if (cLalpha2D == 0.) return 0.;

		double effMach = AerodynamicCalc.calculatePrandtlGlauertCorrection(mach);
		//double eff3D = effMach * ar / (Math.toDegrees(cLalpha2D) / 2*Math.PI*effMach);
		double eff3D = effMach * ar / (cLalpha2D / (2*Math.PI*effMach));

		//		return 2. * Math.PI * ar / 
		//        		(2 + Math.sqrt( Math.pow(eff3D,2) * (1 + Math.tan(Math.toRadians(sweepV_c2) / Math.pow(effMach, 2)) + 4) ));
		return 2. * Math.PI * ar / 
				(2 + Math.sqrt(Math.pow(eff3D,2) * (1 + Math.pow(Math.tan(Math.toRadians(sweepV_c2)),2) / Math.pow(effMach, 2)) + 4 ));
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param lift
	 * @param speed
	 * @param surface
	 * @param altitude
	 * @return
	 */
	public static double calculateLiftCoeff(double lift, double speed, double surface, double altitude) {
		return 2.*lift/(speed*speed*AtmosphereCalc.getDensity(altitude)*surface);
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param alpha0L
	 * @param cLalpha
	 * @return
	 */
	public static double calculateLiftCoefficientAtAlpha0(double alpha0L, double cLalpha) {
		return - alpha0L*cLalpha;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param ar
	 * @param machCurrent
	 * @param sweepLEEquivalent
	 * @param taperRatioEquivalent
	 * @return
	 */
	public static double kFactorPolhamus(double ar, double machCurrent, 
			Amount<Angle> sweepLEEquivalent, double taperRatioEquivalent) {

		double kPolhamus = 0.;
		double aR = ar;

		if (machCurrent < 0.7 
				&& sweepLEEquivalent.getEstimatedValue() < Amount.valueOf(32., NonSI.DEGREE_ANGLE).getEstimatedValue()
				&& taperRatioEquivalent > 0.4
				&& taperRatioEquivalent < 1.
				&& aR > 3 && aR < 8) {

			if(aR < 4) {
				kPolhamus = 1 + 
						aR
						*(1.87 - 0.000233 * sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						/100;
			} else  {
				kPolhamus = 1 + (
						(8.2 - 2.3*sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						- aR * (0.22 - 0.153 *sweepLEEquivalent.doubleValue(NonSI.DEGREE_ANGLE))
						)/100;
			} 
		}

		return kPolhamus;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param ar
	 * @param machCurrent
	 * @param sweepLEEquivalent
	 * @param taperRatioEquivalent
	 * @return
	 */
	public static double calculateCLalphaPolhamus(double ar, double machCurrent, 
			Amount<Angle> sweepLEEquivalent, double taperRatioEquivalent) {

		double cLAlpha = 0.;
		double kPolhamus = LiftCalc.kFactorPolhamus(
				ar, machCurrent, 
				sweepLEEquivalent, taperRatioEquivalent);

		if (kPolhamus != 0.) {

			double sweepHalfEq = LSGeometryCalc.calculateSweep(ar, taperRatioEquivalent, sweepLEEquivalent.doubleValue(SI.RADIAN), 0.5, 0.).doubleValue(SI.RADIAN);

			cLAlpha = 2*Math.PI*ar
					/(2 + sqrt(
							( (pow(ar, 2)*(1 - pow(machCurrent,2))
									/pow(kPolhamus, 2))
									*(1 + pow(tan(sweepHalfEq), 2)
									/(1 - pow(machCurrent,2))
											) + 4) ));
		}	

		return cLAlpha;
	}

	/**
	 * @author Lorenzo Attanasio
	 * 
	 * @param machCurrent
	 * @param ar
	 * @param semispan
	 * @param sweepHalfChordEq
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCLalphaAndersonSweptCompressibleSubsonic(
			double machCurrent, double ar, double semispan, double sweepHalfChordEq,
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		double cLAlpha = 0.;
		double cLAlphaMean2D = calcCLalphaIntegralMean2D(Math.pow(semispan, 2)/ar, semispan, yStations, clAlphaVsY, chordsVsY);

		if (machCurrent < 1) {
			cLAlpha = cLAlphaMean2D * cos(sweepHalfChordEq) 
					/(sqrt(1 - pow(machCurrent,2)
							* pow(cos(sweepHalfChordEq), 2) 
							+ pow(cLAlphaMean2D 
									* cos(sweepHalfChordEq)
									/(Math.PI*ar), 2)) 
							+ cLAlphaMean2D 
							* cos(sweepHalfChordEq)/(Math.PI*ar)
							);

		} else {
			System.out.println("This method can be used in subsonic regime only");
		}

		return cLAlpha;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * 
	 * @param surface
	 * @param semispan
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCLalphaIntegralMean2D(double surface, double semispan, 
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		return (2./surface) * MyMathUtils.integrate1DSimpsonSpline(
				yStations, 
				MathArrays.ebeMultiply(clAlphaVsY, chordsVsY),
				0., semispan*0.9995);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 3 DLR pdf
	 * 
	 * @param alpha
	 */
	public static double calcCLatAlphaLinearDLR(double alpha, double ar) {
		return alpha * 5.53 * ar / (ar + 1.76);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 * @see page 185 Sforza (2014)
	 * 
	 * @param clMax airfoil mean maximum lift coefficient
	 * @param cLAlpha
	 * @param tr taper ratio
	 * @param sweepLE LE sweep
	 * @param ar aspect ratio
	 * @param twist radians
	 * @param engineType
	 */
	public static double calculateCLmaxPhillipsAndAlley(
			double clMax, double cLAlpha, 
			double tr, double sweepLE, 
			double ar, double twist,
			EngineTypeEnum engineType) {

		double cLclMax = (0.952 - 0.45*pow(tr - 0.5, 2)) * pow(ar/12., 0.03);
		double kLambda1 = 0.15 + 18.5*(tr - 0.4)/ar;
		double kLambda2 = 0.55 + 12.*(tr - 0.275)/ar;
		double kLambda = 1 + kLambda1*sweepLE - kLambda2*pow(sweepLE,1.2);
		double kLS = 1 + (0.0042*ar - 0.068)*(1 + 2.3*cLAlpha*twist/clMax);
		double kOmega = 0.;

		if (engineType.equals(EngineTypeEnum.TURBOPROP)) {
			kOmega = 0.1;
		} else if(engineType.equals(EngineTypeEnum.TURBOFAN)) {
			kOmega = -0.2;
		}

		return cLclMax*kLS*kLambda*clMax*(1 - kOmega*cLAlpha*(-twist)/clMax);
	}


	public static double[] calculateCLvsAlphaArrayNasaBlackwell(
			LiftingSurface2Panels theLiftingSurface,
			MyArray alphaArray,
			int nValue,
			boolean printResults
			)
	{

		double cLMax = 0;
		double alphaMaxDouble = 0;
		LSAerodynamicsManager theLsManager = theLiftingSurface.getAerodynamics();
		double [] cLActualArray = new double[nValue];
		LSAerodynamicsManager.CalcCLAtAlpha theClatAlphaCalculator =theLsManager.new CalcCLAtAlpha();
		double cLStar = 0, cLTemp, qValue, a ,b ,c ,d;
		Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator =theLsManager.new MeanAirfoil();
		Airfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theLiftingSurface);
		double alphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
		Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);
		theLiftingSurface.getAerodynamics().set_alphaStar( alphaStarAmount);
		double alphaActual = 0;
		Amount<Angle> alphaMax;
		double cLStarWing=0, cLLinearSlope = 0, cLAlphaZero, alphaZeroLiftWingClean;
		cLStarWing = theClatAlphaCalculator.nasaBlackwell(alphaStarAmount);
		theLiftingSurface.getAerodynamics().setcLStarWing(cLStarWing);
		theLiftingSurface.getAerodynamics().set_alphaStar(Amount.valueOf(alphaStar,SI.RADIAN));
		for (int i=0; i<nValue; i++ ){
			alphaActual = alphaArray.get(i);


			cLTemp = theClatAlphaCalculator.nasaBlackwell(alphaTemp);
			if (alphaActual < alphaStar){    //linear trait
				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
				theLiftingSurface.getAerodynamics().setcLLinearSlopeNB(cLLinearSlope);
				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
				qValue = cLStarWing - cLLinearSlope*alphaStar;
				cLAlphaZero = qValue;
				theLiftingSurface.getAerodynamics().setcLAlphaZero(cLAlphaZero);
				alphaZeroLiftWingClean = -qValue/cLLinearSlope;
				theLiftingSurface.getAerodynamics().setAlphaZeroLiftWingClean(alphaZeroLiftWingClean);
				cLActualArray[i] = cLLinearSlope * alphaActual+ qValue;
				//System.out.println(" CL Actual = " + cLActual );
			}

			else {  // non linear trait

				theLsManager.calcAlphaAndCLMax(meanAirfoil);
				cLMax = theLsManager.get_cLMaxClean();
				alphaMax = theLsManager.get_alphaMaxClean();	
				alphaMaxDouble = alphaMax.getEstimatedValue();

				cLLinearSlope = (cLStarWing - cLTemp)/alphaStar;
				//System.out.println("CL Linear Slope [1/rad] = " + cLLinearSlope);
				double[][] matrixData = { {Math.pow(alphaMaxDouble, 3), Math.pow(alphaMaxDouble, 2), alphaMaxDouble,1.0},
						{3* Math.pow(alphaMaxDouble, 2), 2*alphaMaxDouble, 1.0, 0.0},
						{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
						{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),alphaStar,1.0}};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {cLMax, 0,cLLinearSlope, cLStarWing};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];

				cLActualArray[i] = a * Math.pow(alphaActual, 3) + 
						b * Math.pow(alphaActual, 2) + 
						c * alphaActual + d;
			}

		}
		if(printResults==true){
			System.out.println("\n -----------CLEAN-------------- ");
			System.out.println(" alpha max " + alphaMaxDouble*57.3 + " (deg)");
			System.out.println(" alpha star " + alphaStar*57.3 + " (deg)");
			System.out.println(" cL max " + cLMax);
			System.out.println(" cL star " + cLStarWing);
			System.out.println(" cL alpha " + cLLinearSlope + " (1/rad)");
			System.out.println("\n\n");}
		printResults=false;

		return cLActualArray;
	}




	public static double[] calculateCLvsAlphaHighLiftArrayNasaBlackwell(
			LiftingSurface2Panels theLiftingSurface,
			MyArray alphaArray, 
			int nValue,
			double cLalphaNew,
			double deltaCL0Flap,
			double deltaAlphaMaxFlap,
			double cLMaxFlap,
			double deltaClmaxSlat
			)
	{
		double cLMax = 0;
		double alphaMaxDouble = 0;
		double alphaActual = 0;
		LSAerodynamicsManager theLsManager = theLiftingSurface.getAerodynamics();
		double [] cLActualArray = new double[nValue];
		double cLAlphaFlap = cLalphaNew*57.3; // need it in 1/rad

		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator =theLsManager.new MeanAirfoil();
		Airfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theLiftingSurface);
		double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();

		Amount<Angle> alphaStarCleanAmount = Amount.valueOf(alphaStarClean, SI.RADIAN);

		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLvsAlphaCurve = theLsManager.new CalcCLvsAlphaCurve();
		LSAerodynamicsManager.CalcCLAtAlpha theCLCleanCalculator = theLsManager.new CalcCLAtAlpha();
		double cLStarClean = theCLCleanCalculator.nasaBlackwellCompleteCurveValue(alphaStarCleanAmount);

		double cL0Clean =  theCLCleanCalculator.nasaBlackwellCompleteCurveValue(Amount.valueOf(0.0, SI.RADIAN));
		double cL0HighLift = cL0Clean + deltaCL0Flap;
		double qValue = cL0HighLift;
		double alphaStar = (cLStarClean - qValue)/cLAlphaFlap;
		theLsManager.calcAlphaAndCLMax(meanAirfoil);
		double cLMaxClean = theLsManager.get_cLMaxClean();
		Amount<Angle> alphaMax = theLsManager.get_alphaMaxClean();	
		alphaMaxDouble = alphaMax.getEstimatedValue();

		double alphaMaxHighLift;

		if(deltaClmaxSlat == 0)
			alphaMaxHighLift = alphaMax.getEstimatedValue() + deltaAlphaMaxFlap/57.3;
		else
			alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLalphaNew) 
			+ theLsManager.get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(
					theLiftingSurface
					.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
					meanAirfoil.getGeometry().get_deltaYPercent());

		alphaMaxHighLift = Amount.valueOf(alphaMaxHighLift, SI.RADIAN).getEstimatedValue();

		double alphaStarFlap; 

		if(deltaClmaxSlat == 0)
			alphaStarFlap = (alphaStar + alphaStarClean)/2;
		else
			alphaStarFlap = alphaMaxHighLift-(alphaMax.to(SI.RADIAN).getEstimatedValue()-alphaStarClean);

		double cLStarFlap = cLAlphaFlap * alphaStarFlap + qValue;	
		for (int i=0; i<nValue; i++ ){
			alphaActual = alphaArray.get(i);

			if (alphaActual < alphaStarFlap ){ 
				cLActualArray[i] = cLAlphaFlap * alphaActual + qValue;	
			}
			else{
				double[][] matrixData = { {Math.pow(alphaMaxHighLift, 3), Math.pow(alphaMaxHighLift, 2)
					, alphaMaxHighLift,1.0},
						{3* Math.pow(alphaMaxHighLift, 2), 2*alphaMaxHighLift, 1.0, 0.0},
						{3* Math.pow(alphaStarFlap, 2), 2*alphaStarFlap, 1.0, 0.0},
						{Math.pow(alphaStarFlap, 3), Math.pow(alphaStarFlap, 2),alphaStarFlap,1.0}};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


				double [] vector = {cLMaxFlap, 0,cLAlphaFlap, cLStarFlap};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				double a = solSystem[0];
				double b = solSystem[1];
				double c = solSystem[2];
				double d = solSystem[3];

				cLActualArray[i] = a * Math.pow(alphaActual, 3) + 
						b * Math.pow(alphaActual, 2) + 
						c * alphaActual + d;
			}

		}
		return cLActualArray;
	}


	// TO DO move here the cl wing body calculator
	public static double[] calculateCLvsAlphaArrayWingBody(
			LiftingSurface2Panels theLiftingSurface,
			MyArray alphaArray,
			int nValue,
			boolean printResults
			)
	{
		double[] xArray = {0.0, 0.0};
		return xArray;
	}

	/**
	 * 
	 *This method evaluates the CL vs Alpha array for given alpha array. In order to evaluate the curve
	 *for each angle of attack the load distribution using Nasa Blackwell method is evaluated. At 50 station spemi-span wise 
	 *is calculated the local lift coefficient. It's important to remember that Nasa Blackwell method is inviscid, so the
	 *obtained values of cl are non viscous. With these value of cl it's calculated the angle of attack in the linear 
	 *curve of cl vs alpha for the airfoils and the obtained value of alpha is used in order to evaluate the viscous cl of
	 *the airfoils. Starting from this new distribution of cl it's evaluated the viscous cL of the wing with an integral.
	 *
	 *@param alpha array in rad
	 *
	 *@author Manuela Ruocco
	 *
	 */
	@SuppressWarnings("static-access")
	public static double[] calculateCLArraymodifiedStallPath(MyArray alphaArray, LiftingSurface2Panels theLiftingSurface){


		// VARIABLE DECLARATION
		Amount<Angle> alphaActual;
		double qValue, cLWingActual = 0;
		double [] clNasaBlackwell = new double [alphaArray.size()];

		List<Airfoil> airfoilList = new ArrayList<Airfoil>();

		LSAerodynamicsManager theLSManager = theLiftingSurface.getAerodynamics();
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();

		int nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
		double [] yArray = MyArrayUtils.linspace(0., theLiftingSurface.get_span().getEstimatedValue()/2, nPointSemiSpan);
		double [] yArrayND = MyArrayUtils.linspace(0., 1, nPointSemiSpan);
		double [] cLDistributionInviscid = new double [nPointSemiSpan];
		double [] alphaLocalAirfoil = new double [nPointSemiSpan];
		double [] clDisributionReal = new double [nPointSemiSpan];

		double [] cLWingArray = new double [alphaArray.size()];


		for (int j=0 ; j<nPointSemiSpan; j++){
			airfoilList.add(j,theLSManager.calculateIntermediateAirfoil(
					theLiftingSurface, yArray[j]) );
			airfoilList.get(j).getAerodynamics().calculateClvsAlpha();}


		// iterations
		for (int ii=0; ii<alphaArray.size(); ii++){
			alphaActual = Amount.valueOf(alphaArray.get(ii),SI.RADIAN);

			calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
			clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
			clNasaBlackwell[clNasaBlackwell.length-1] = 0;

			for (int i=0 ; i<nPointSemiSpan ;  i++){
				cLDistributionInviscid[i] = clNasaBlackwell[i];
				//			System.out.println( " cl local " + cLLocal);
				qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlphaInterp(0.0);
				//			System.out.println(" qValue " + qValue );
				alphaLocalAirfoil[i] = (cLDistributionInviscid[i]-qValue)/airfoilList.get(i).getAerodynamics().get_clAlpha();
				//			System.out.println(" alpha local airfoil " + alphaLocalAirfoil);
				clDisributionReal[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
						//alphaLocal.getEstimatedValue()+
						alphaLocalAirfoil[i]);
				//					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
			}
			cLWingActual = MyMathUtils.integrate1DSimpsonSpline(yArrayND, clDisributionReal);

			cLWingArray[ii] = cLWingActual;
		}

		return cLWingArray;
	}



	public static double calculateCLMax(double [] maximumLiftCoefficient, 
			double semispan, 
			double surface,
			double[] yStationsActual,
			double[] chordsVsYActual,
			double[] xLEvsYActual,
			double[] dihedral,
			double[] twist,
			double[] alpha0l,
			double vortexSemiSpanToSemiSpanRatio,
			double alpha,
			double mach,
			double altitude){

		// parameters definition

		double cLMax = 0;
		Amount<Angle> alphaAtCLMaX = null;

		int _nPointsSemispanWise = (int)(1./(2*vortexSemiSpanToSemiSpanRatio));
		int stepsToStallCounter = 0;
		double accuracy =0.0001;
		double diffCL = 0;
		double diffCLappOld = 0;
		double diffCLapp = 0;
		double deltaAlpha;
		double alphaNew = 0;
		double alphaOld;
		boolean _findStall = false;
		Amount<Angle> alphaNewAmount;
		boolean found = false;

		Amount<Angle> alphaStart = Amount.valueOf(toRadians(-2.), SI.RADIAN);
		Amount<Angle> alphaEnd = Amount.valueOf(toRadians(38.), SI.RADIAN);
		int _numberOfAlpha = 15; 
		MyArray alphaArray = new MyArray();
		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));

		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
				semispan, 
				surface,
				yStationsActual,
				chordsVsYActual,
				xLEvsYActual,
				dihedral,
				twist,
				alpha0l,
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				mach,
				altitude);

		for (int j=0; j < _numberOfAlpha; j++) {
			if (found == false) {
				Amount<Angle> alphaInputAngle = Amount.valueOf(alphaArray.get(j), SI.RADIAN);
				MyArray clDistributionArray = new MyArray();
				
				theNasaBlackwellCalculator.calculate(alphaInputAngle);
				clDistributionArray = theNasaBlackwellCalculator.get_clTotalDistribution();

				for(int i =0; i< _nPointsSemispanWise; i++) {
					if (found == false 
							&& clDistributionArray.get(i)
							> maximumLiftCoefficient[i] ) {	

						for (int k =i; k< _nPointsSemispanWise; k++) {
							diffCLapp = ( clDistributionArray.get(k) -  maximumLiftCoefficient[k]);
							diffCL = Math.max(diffCLapp, diffCLappOld);
							diffCLappOld = diffCL;
						}
						if( Math.abs(diffCL) < accuracy){
							cLMax = theNasaBlackwellCalculator.get_cLEvaluated();
							found = true;
							alphaAtCLMaX = alphaArray.getAsAmount(j); 
						}

						else{
							deltaAlpha = alphaArray.getAsAmount(j).getEstimatedValue()
									- alphaArray.getAsAmount(j-1).getEstimatedValue();
							alphaNew = alphaArray.getAsAmount(j).getEstimatedValue() - (deltaAlpha/2);
							alphaOld = alphaArray.getAsAmount(j).getEstimatedValue(); 
							alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
							diffCLappOld = 0;
							while ( diffCL > accuracy){
								theNasaBlackwellCalculator.calculate(alphaNewAmount);
								clDistributionArray = theNasaBlackwellCalculator.get_clTotalDistribution();
								diffCL = 0;

								for (int m =0; m< _nPointsSemispanWise; m++) {
									diffCLapp = (clDistributionArray.get(m) -  maximumLiftCoefficient[m]);

									if ( diffCLapp > 0 ){
										diffCL = Math.max(diffCLapp,diffCLappOld);
										diffCLappOld = diffCL;
									}

								}
								deltaAlpha = Math.abs(alphaOld - alphaNew);
								alphaOld = alphaNew;
								if (diffCL == 0 ){
									alphaNew = alphaOld + (deltaAlpha/2);
									alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
									diffCL = 1;
									diffCLappOld = 0;
								}
								else { 
									if(deltaAlpha > 0.005){
										alphaNew = alphaOld - (deltaAlpha/2);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;
									}
									else {
										alphaNew = alphaOld - (deltaAlpha);	
										alphaNewAmount = Amount.valueOf(alphaNew, SI.RADIAN);
										diffCLappOld = 0;
										if ( diffCL < accuracy) break;}}

							}
							found = true;
						}
						alphaAtCLMaX = Amount.valueOf(alphaNew, SI.RADIAN);
				}
			}
		}
		}
			theNasaBlackwellCalculator.calculate(alphaAtCLMaX);
			double cLMaxActual = theNasaBlackwellCalculator.get_cLCurrent();
			return cLMaxActual;
		}

	}