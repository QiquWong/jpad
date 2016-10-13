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

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsManager;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
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
		double eff3D = effMach * ar / (Math.toDegrees(cLalpha2D) / 2*Math.PI*effMach);

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
			AerodynamicDatabaseReader aeroDatabaseReader
			) {

		double cLclMax = (0.952 - 0.45*pow(tr - 0.5, 2)) * pow(ar/12., 0.03);
		double kLambda1 = 0.15 + 18.5*(tr - 0.4)/ar;
		double kLambda2 = 0.55 + 12.*(tr - 0.275)/ar;
		double kLambda = 1 + kLambda1*sweepLE - kLambda2*pow(sweepLE,1.2);
		double kLS = 1 + (0.0042*ar - 0.068)*(1 + 2.3*cLAlpha*twist/clMax);
		double kOmega = aeroDatabaseReader.getKOmegePhillipsAndAlley(
				cLAlpha,
				twist,
				clMax,
				tr,
				ar
				);

		return cLclMax*kLS*kLambda*clMax*(1 - kOmega*cLAlpha*(-twist)/clMax);
	}

	public static double calculateCLAtAlphaNonLinearTrait(
			Amount<Angle> alphaActual,
			Amount<?> cLAlpha,
			double cLStar,
			Amount<Angle> alphaStar,
			double cLmax,
			Amount<Angle> alphaStall
			) {
		
		double cLActual = 0.0;
			
		double[][] matrixData = { 
				{
					Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
					Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
					alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
					1.0
				},
				{
					3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
					2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
					1.0,
					0.0
				},
				{
					3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
					2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
					1.0,
					0.0
				},
				{
					Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
					Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
					alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
					1.0
				}
		};
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		double [] vector = {
				cLmax,
				0,
				cLAlpha.to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue(),
				cLStar
				};

		double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

		double a = solSystem[0];
		double b = solSystem[1];
		double c = solSystem[2];
		double d = solSystem[3];

		cLActual = 
				a * Math.pow(alphaActual.doubleValue(NonSI.DEGREE_ANGLE), 3) + 
				b * Math.pow(alphaActual.doubleValue(NonSI.DEGREE_ANGLE), 2) + 
				c * alphaActual.doubleValue(NonSI.DEGREE_ANGLE) + 
				d;
		
		return cLActual;
		
	}

	public static Double[] calculateCLvsAlphaArray(
			double cL0,
			double cLStar,
			double cLmax,
			Amount<Angle> alphaStar,
			Amount<Angle> alphaStall,
			Amount<?> cLAlpha,
			Double[] alphaArray
			) {
	
		Double[] cLArray = new Double[alphaArray.length];
		
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		
		for(int i=0; i<alphaArray.length; i++) {
			if(alphaArray[i] <= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)) {
				cLArray[i] = (cLAlpha.to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue()
						* alphaArray[i])
						+ cL0;
			}
			else {
				double[][] matrixData = { 
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
									2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									1.0,
									0.0},
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
										Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
										alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0}
									};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {
						cLmax,
						0,
						cLAlpha.to(NonSI.DEGREE_ANGLE).inverse().getEstimatedValue(),
						cLStar
						};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];

				cLArray[i] = a * Math.pow(alphaArray[i], 3) + 
						b * Math.pow(alphaArray[i], 2) + 
						c * alphaArray[i] +
						d;
			}
				
		}
		
		return cLArray;
	}

	public static double[] calculateCLvsAlphaHighLiftArrayNasaBlackwell(
			LiftingSurface theLiftingSurface,
			MyArray alphaArray, 
			int nValue,
			double cLalphaNew,
			double deltaCL0Flap,
			double deltaAlphaMaxFlap,
			double cLMaxFlap,
			double deltaClmaxSlat
			)
	{
		double alphaActual = 0;
		LSAerodynamicsManager theLsManager = theLiftingSurface.getAerodynamics();
		double [] cLActualArray = new double[nValue];
		double cLAlphaFlap = cLalphaNew*57.3; // need it in 1/rad

		Airfoil meanAirfoil = new Airfoil(
				LiftingSurface.calculateMeanAirfoil(theLiftingSurface), 
				theLiftingSurface.getAerodynamicDatabaseReader());
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
		Amount<Angle> alphaMax = theLsManager.getAlphaMaxClean();	

		double alphaMaxHighLift;

		if(deltaClmaxSlat == 0)
			alphaMaxHighLift = alphaMax.getEstimatedValue() + deltaAlphaMaxFlap/57.3;
		else
			alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLalphaNew) 
			+ theLsManager.getAerodynamicDatabaseReader().getDAlphaVsLambdaLEVsDy(
					theLiftingSurface
					.getSweepLEEquivalent(false).to(NonSI.DEGREE_ANGLE).getEstimatedValue(),
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
			LiftingSurface theLiftingSurface,
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
	public static double[] calculateCLArraymodifiedStallPath(MyArray alphaArray, LiftingSurface theLiftingSurface){


		// VARIABLE DECLARATION
		Amount<Angle> alphaActual;
		double qValue, cLWingActual = 0;
		double [] clNasaBlackwell = new double [alphaArray.size()];

		List<Airfoil> airfoilList = new ArrayList<Airfoil>();

		LSAerodynamicsManager theLSManager = theLiftingSurface.getAerodynamics();
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();

		int nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
		double [] yArray = MyArrayUtils.linspace(0., theLiftingSurface.getSpan().getEstimatedValue()/2, nPointSemiSpan);
		double [] yArrayND = MyArrayUtils.linspace(0., 1, nPointSemiSpan);
		double [] cLDistributionInviscid = new double [nPointSemiSpan];
		double [] alphaLocalAirfoil = new double [nPointSemiSpan];
		double [] clDisributionReal = new double [nPointSemiSpan];

		double [] cLWingArray = new double [alphaArray.size()];


		for (int j=0 ; j<nPointSemiSpan; j++){
			airfoilList.add(j, new Airfoil(theLiftingSurface
						.calculateAirfoilAtY(theLiftingSurface, yArray[j]),
						theLiftingSurface.getAerodynamicDatabaseReader())
					);
			airfoilList.get(j).getAerodynamics().calculateClvsAlpha();}


		// iterations
		for (int ii=0; ii<alphaArray.size(); ii++){
			alphaActual = Amount.valueOf(alphaArray.get(ii),SI.RADIAN);

			calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
			clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().getClTotalDistribution().toArray();
			clNasaBlackwell[clNasaBlackwell.length-1] = 0;

			for (int i=0 ; i<nPointSemiSpan ;  i++){
				cLDistributionInviscid[i] = clNasaBlackwell[i];
				//			System.out.println( " cl local " + cLLocal);
				qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlphaInterp(0.0);
				//			System.out.println(" qValue " + qValue );
				alphaLocalAirfoil[i] = (cLDistributionInviscid[i]-qValue)/airfoilList.get(i).getAerodynamics().getClAlpha().getEstimatedValue();
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

	public static double calculateCLMax(
			double[] maximumLiftCoefficient, 
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
		Amount<Angle> alphaEnd = Amount.valueOf(toRadians(28.), SI.RADIAN);
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
				clDistributionArray = theNasaBlackwellCalculator.getClTotalDistribution();

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
								clDistributionArray = theNasaBlackwellCalculator.getClTotalDistribution();
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
			double cLMaxActual = theNasaBlackwellCalculator.getCLCurrent();
			return cLMaxActual;
		}

	/*********************************************************************************************
	 * This method calculate high lift devices effects on lift coefficient curve of the 
	 * airfoil and wing throughout semi-empirical formulas; in particular DeltaCl0, DeltaCL0
	 * DeltaCLmax and DeltaClmax are calculated for flaps when only DeltaClmax and DeltaCLmax
	 * are calculated for slats. Moreover an evaluation of new CLapha slope and CD are performed
	 * for the wing. 
	 * 
	 * @author Vittorio Trifari
	 */
	public static void calculateHighLiftDevicesEffects(
			LiftingSurface theLiftingSurface,
			OperatingConditions theOperatingConditions,
			List<Amount<Angle>> deltaFlap,
			List<Amount<Angle>> deltaSlat,
			Double currentLiftingCoefficient
			) {
		
		List<SymmetricFlapCreator> flapList = theLiftingSurface.getLiftingSurfaceCreator().getSymmetricFlaps();
		List<SlatCreator> slatList = theLiftingSurface.getLiftingSurfaceCreator().getSlats();
		
		if(deltaFlap.size() != flapList.size()) {
			System.err.println("ERROR THE FLAP DEFLECTIONS MUST BE EQUAL TO THE NUMBER OF FLAPS!");
			return;
		}
		
		if(!slatList.isEmpty()) 
				if(deltaSlat.size() != slatList.size()) {
			System.err.println("ERROR THE SLAT DEFLECTIONS MUST BE EQUAL TO THE NUMBER OF SLATS!");
			return;
		}
		
		//--------------------------------------------
		// Managing flaps types:
		List<Double> flapTypeIndex = new ArrayList<Double>();
		List<Double> deltaFlapRef = new ArrayList<Double>();

		for(int i=0; i<flapList.size(); i++) {
			if(flapList.get(i).getType() == FlapTypeEnum.SINGLE_SLOTTED) {
				flapTypeIndex.add(1.0);
				deltaFlapRef.add(45.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapTypeIndex.add(2.0);
				deltaFlapRef.add(50.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.PLAIN) {
				flapTypeIndex.add(3.0);
				deltaFlapRef.add(60.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.FOWLER) {
				flapTypeIndex.add(4.0);
				deltaFlapRef.add(40.0);
			}
			else if(flapList.get(i).getType() == FlapTypeEnum.TRIPLE_SLOTTED) {
				flapTypeIndex.add(5.0);
				deltaFlapRef.add(50.0);
			}
		}
		//--------------------------------------------
		// Creating lists of flaps geometric parameters:
		List<Double> etaInFlap = new ArrayList<Double>();
		List<Double> etaOutFlap = new ArrayList<Double>();
		List<Double> cfc = new ArrayList<Double>();
		
		List<Double> etaInSlat = new ArrayList<Double>();
		List<Double> etaOutSlat = new ArrayList<Double>();
		List<Double> csc = new ArrayList<Double>();
		List<Double> cExtcSlat = new ArrayList<Double>();

		for(int i=0; i<flapList.size(); i++) {
			etaInFlap.add(flapList.get(i).getInnerStationSpanwisePosition());
			etaOutFlap.add(flapList.get(i).getOuterStationSpanwisePosition());
			cfc.add(flapList.get(i).getMeanChordRatio());
			if(!slatList.isEmpty()) {
				etaInSlat.add(slatList.get(i).getInnerStationSpanwisePosition());
				etaOutSlat.add(slatList.get(i).getOuterStationSpanwisePosition());
				csc.add(slatList.get(i).getMeanChordRatio());
				cExtcSlat.add(slatList.get(i).getExtensionRatio());
			}
		}
		//--------------------------------------------
		// Creating arrays of the required parameters to be interpolated:
		double [] clAlphaMeanFlap = new double [flapList.size()];
		double [] clZeroMeanFlap = new double [flapList.size()];
		double [] maxTicknessMeanFlap = new double [flapList.size()];
		double [] maxTicknessMeanSlat = new double [slatList.size()];
		double [] maxTicknessFlapStations = new double [2*flapList.size()];
		double [] clAlphaFlapStations = new double [2*flapList.size()];
		double [] clZeroFlapStations = new double [2*flapList.size()];
		double [] leRadiusMeanSlat = new double [slatList.size()];
		double [] chordMeanSlat = new double [slatList.size()];
		double [] leadingEdgeRadiusSlatStations = new double [2*slatList.size()];
		double [] maxTicknessSlatStations = new double [2*slatList.size()];
		double [] chordSlatStations = new double [2*slatList.size()];
		
		double [] influenceFactor = new double [2];
		
		for ( int i=0; i< flapList.size(); i++){
			int kk = i*2;
			
			clAlphaFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertListOfAmountodoubleArray(
							theLiftingSurface.getClAlphaVsY()
							),
					etaOutFlap.get(i));
			
			clAlphaFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertListOfAmountodoubleArray(
							theLiftingSurface.getClAlphaVsY()
							),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getCl0VsY()
							),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getCl0VsY()
							),
					etaOutFlap.get(i));
			
			maxTicknessFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getMaxThicknessVsY()
							),
					etaInFlap.get(i));
			
			maxTicknessFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getMaxThicknessVsY()
							),
					etaOutFlap.get(i));
			
			try {
				influenceFactor = LiftingSurface.calculateInfluenceFactorsMeanAirfoilFlap(
						etaInFlap.get(i),
						etaOutFlap.get(i),
						theLiftingSurface
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			clAlphaMeanFlap[i] = clAlphaFlapStations[kk] * influenceFactor[0] + clAlphaFlapStations[kk+1]*influenceFactor[1];
			clZeroMeanFlap[i] = clZeroFlapStations[kk] * influenceFactor[0] + clZeroFlapStations[kk+1]*influenceFactor[1];
			maxTicknessMeanFlap[i] = maxTicknessFlapStations[kk]* influenceFactor[0] + maxTicknessFlapStations[kk+1]*influenceFactor[1];
		}
		
		if(slatList != null) 
			for ( int i=0; i< slatList.size(); i++){
				int kk = i*2;

				leadingEdgeRadiusSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theLiftingSurface.getRadiusLEVsY()
								),
						etaOutSlat.get(i));

				leadingEdgeRadiusSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theLiftingSurface.getRadiusLEVsY()
								),
						etaInSlat.get(i));

				chordSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()
								),
						etaInSlat.get(i));

				chordSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()
								),
						etaOutSlat.get(i));


				maxTicknessSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getMaxThicknessVsY()
								),
						etaInSlat.get(i));

				maxTicknessSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getLiftingSurfaceCreator().getEtaBreakPoints()
								),
						MyArrayUtils.convertToDoublePrimitive(
								theLiftingSurface.getMaxThicknessVsY()
								),
						etaOutSlat.get(i));

				try {
					influenceFactor = LiftingSurface.calculateInfluenceFactorsMeanAirfoilFlap(
							etaInSlat.get(i),
							etaOutSlat.get(i),
							theLiftingSurface
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				leRadiusMeanSlat[i] = leadingEdgeRadiusSlatStations[kk] * influenceFactor[0] + leadingEdgeRadiusSlatStations[kk+1]*influenceFactor[1];
				chordMeanSlat[i] = chordSlatStations[kk] * influenceFactor[0] + chordSlatStations[kk+1]*influenceFactor[1];
				maxTicknessMeanSlat[i] = maxTicknessSlatStations[kk] * influenceFactor[0] + maxTicknessSlatStations[kk+1]*influenceFactor[1];

			}

		//---------------------------------------------
		// deltaCl0 (flap)
		List<Double> thetaF = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) 
			thetaF.add(Math.acos((2*cfc.get(i))-1));

		List<Double> alphaDelta = new ArrayList<Double>();
		for(int i=0; i<thetaF.size(); i++)
			alphaDelta.add(1-((thetaF.get(i)-Math.sin(thetaF.get(i)))/Math.PI));

		List<Double> etaDeltaFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				etaDeltaFlap.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getEtaDeltaVsDeltaFlapPlain(
								deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								cfc.get(i)
								)
						);
			else
				etaDeltaFlap.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getEtaDeltaVsDeltaFlap(
								deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								flapTypeIndex.get(i))
						);
		}

		List<Double> deltaCl0First = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0First.add(
					alphaDelta.get(i).doubleValue()
					*etaDeltaFlap.get(i).doubleValue()
					*deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE)
					*clAlphaMeanFlap[i]
					);

		List<Double> deltaCCfFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCCfFlap.add(
					theLiftingSurface
					.getHighLiftDatabaseReader()
					.getDeltaCCfVsDeltaFlap(
							deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							flapTypeIndex.get(i)
							)
					);

		List<Double> cFirstCFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*cfc.get(i).doubleValue()));

		for(int i=0; i<flapTypeIndex.size(); i++)
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCl0FlapList().add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(clZeroMeanFlap[i]*(cFirstCFlap.get(i).doubleValue()-1))
					);

		double deltaCl0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0Flap += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCl0FlapList().get(i);
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCl0Flap(deltaCl0Flap);
		
		//---------------------------------------------------------------
		// deltaClmax (flap)
		List<Double> deltaClmaxBase = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxBase.add(
					theLiftingSurface
					.getHighLiftDatabaseReader()
					.getDeltaCLmaxBaseVsTc(
							maxTicknessMeanFlap[i],
							flapTypeIndex.get(i)
							)
					);

		List<Double> k1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if (cfc.get(i) <= 0.30)
				k1.add(theLiftingSurface
						.getHighLiftDatabaseReader()
						.getK1vsFlapChordRatio(cfc.get(i), flapTypeIndex.get(i))
						);
			else if ((cfc.get(i) > 0.30) && ((flapTypeIndex.get(i) == 2) || (flapTypeIndex.get(i) == 4) || (flapTypeIndex.get(i) == 5)))
				k1.add(0.04*(cfc.get(i)*100));
			else if ((cfc.get(i) > 0.30) && ((flapTypeIndex.get(i) == 1) || (flapTypeIndex.get(i) == 3) ))
				k1.add((608.31*Math.pow(cfc.get(i), 5))
						-(626.15*Math.pow(cfc.get(i), 4))
						+(263.4*Math.pow(cfc.get(i), 3))
						-(62.946*Math.pow(cfc.get(i), 2))
						+(10.638*cfc.get(i))
						+0.0064
						);

		List<Double> k2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			k2.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getK2VsDeltaFlap(
							deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							flapTypeIndex.get(i)
							)
					);

		List<Double> k3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			k3.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getK3VsDfDfRef(
							deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							deltaFlapRef.get(i),
							flapTypeIndex.get(i)
							)
					);

		for(int i=0; i<flapTypeIndex.size(); i++)
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().add(
					k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmaxBase.get(i).doubleValue()
					);

		double deltaClmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxFlap += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(i).doubleValue();
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaClmaxFlap(deltaClmaxFlap);
		
		//---------------------------------------------------------------
		// deltaClmax (slat)
		if(!deltaSlat.isEmpty()) {

			List<Double> dCldDelta = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				dCldDelta.add(theLiftingSurface
						.getHighLiftDatabaseReader()
						.getDCldDeltaVsCsC(csc.get(i))
						);

			List<Double> etaMaxSlat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				etaMaxSlat.add(theLiftingSurface
						.getHighLiftDatabaseReader()
						.getEtaMaxVsLEradiusTicknessRatio(
								leRadiusMeanSlat[i]/(chordMeanSlat[i]),
								maxTicknessMeanSlat[i]
										)
						);

			List<Double> etaDeltaSlat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				etaDeltaSlat.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getEtaDeltaVsDeltaSlat(deltaSlat.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);

			for(int i=0; i<deltaSlat.size(); i++)
				theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxSlatList().add(
						dCldDelta.get(i).doubleValue()
						*etaMaxSlat.get(i).doubleValue()
						*etaDeltaSlat.get(i).doubleValue()
						*deltaSlat.get(i).doubleValue(NonSI.DEGREE_ANGLE)
						*cExtcSlat.get(i).doubleValue()
						);

			double deltaClmaxSlat = 0.0;
			for(int i=0; i<deltaSlat.size(); i++)
				deltaClmaxSlat += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxSlatList().get(i).doubleValue();
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaClmaxSlat(deltaClmaxSlat);
		}
		else
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaClmaxFlapList(null);
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaClmaxFlap(null);

		//---------------------------------------------------------------
		// deltaCL0 (flap)
		List<Double> kc = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kc.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getKcVsAR(
							theLiftingSurface.getAspectRatio(),
							alphaDelta.get(i))	
					);

		List<Double> kb = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kb.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getKbVsFlapSpanRatio(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing())	
					);

		double cLLinearSlope = theLiftingSurface
				.getTheAerodynamicsCalculator()
					.getCLAlpha()
						.get(MethodEnum.NASA_BLACKWELL)
							.getEstimatedValue();

		for(int i=0; i<flapTypeIndex.size(); i++)
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCL0FlapList().add(
					kb.get(i).doubleValue()
					*kc.get(i).doubleValue()
					*theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCl0FlapList().get(i).doubleValue()
					*((cLLinearSlope)/clAlphaMeanFlap[i])
					);

		double deltaCL0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCL0Flap += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCL0FlapList().get(i).doubleValue();
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCL0Flap(deltaCL0Flap);
		
		//---------------------------------------------------------------
		// deltaCLmax (flap)
		List<Double> flapSurface = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			flapSurface.add(
					Math.abs(
							theLiftingSurface.getSpan().getEstimatedValue()							
							/2*theLiftingSurface.getLiftingSurfaceCreator().getRootChordEquivalentWing().getEstimatedValue()
							*(2-((1-theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing())*(etaInFlap.get(i)+etaOutFlap.get(i))))
							*(etaOutFlap.get(i)-etaInFlap.get(i))
							)
					);

		List<Double> kLambdaFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kLambdaFlap.add(
					Math.pow(Math.cos(theLiftingSurface.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().getEstimatedValue()),0.75)
					*(1-(0.08*Math.pow(Math.cos(theLiftingSurface.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().getEstimatedValue()), 2)))
					);

		for(int i=0; i<flapTypeIndex.size(); i++)
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCLmaxFlapList().add(
					theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(i)
					*(flapSurface.get(i)/theLiftingSurface.getSurface().getEstimatedValue())
					*kLambdaFlap.get(i)
					);

		double deltaCLmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlap += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCLmaxFlapList().get(i).doubleValue();
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCLmaxFlap(deltaCLmaxFlap);

		//---------------------------------------------------------------
		// deltaCLmax (slat)
		if(!deltaSlat.isEmpty()) {

			List<Double> kLambdaSlat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				kLambdaSlat.add(
						Math.pow(Math.cos(theLiftingSurface.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().getEstimatedValue()),0.75)
						*(1-(0.08*Math.pow(Math.cos(theLiftingSurface.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().getEstimatedValue()), 2)))
						);

			List<Double> slatSurface = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				slatSurface.add(
						Math.abs(theLiftingSurface.getSpan().getEstimatedValue()
								/2*theLiftingSurface.getLiftingSurfaceCreator().getRootChordEquivalentWing().getEstimatedValue()
								*(2-(1-theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing())*(etaInSlat.get(i)+etaOutSlat.get(i)))
								*(etaOutSlat.get(i)-etaInSlat.get(i))
								)
						);

			for(int i=0; i<deltaSlat.size(); i++)
				theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCLmaxSlatList().add(
						theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxSlatList().get(i)
						*(slatSurface.get(i)/theLiftingSurface.getSurface().getEstimatedValue())
						*kLambdaSlat.get(i));

			double deltaCLmaxSlat = 0.0;
			for(int i=0; i<deltaSlat.size(); i++)
				deltaCLmaxSlat += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCLmaxSlatList().get(i).doubleValue();
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCLmaxSlat(deltaCLmaxSlat);
		}
		else
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCLmaxFlapList(null);
			theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCLmaxFlap(null);
			
		//---------------------------------------------------------------
		// new CLalpha

		List<Double> cLalphaFlapList = new ArrayList<Double>();
		List<Double> swf = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			cLalphaFlapList.add(
					cLLinearSlope*(Math.PI/180)
					*(1+((theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCL0FlapList().get(i)/
							theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCl0FlapList().get(i))
							*(cFirstCFlap.get(i)*(1-((cfc.get(i))*(1/cFirstCFlap.get(i))
									*Math.pow(Math.sin(deltaFlap.get(i).doubleValue(SI.RADIAN)), 2)))-1))));
			swf.add(flapSurface.get(i)/theLiftingSurface.getSurface().getEstimatedValue());
		}

		double swfTot = 0;
		for(int i=0; i<swf.size(); i++)
			swfTot += swf.get(i);

		double cLAlphaFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			cLAlphaFlap += cLalphaFlapList.get(i)*swf.get(i);

		cLAlphaFlap /= swfTot;
		theLiftingSurface.getTheAerodynamicsCalculator().getCLAlphaHighLift().put(
				MethodEnum.EMPIRICAL,
				Amount.valueOf(
						cLAlphaFlap,
						NonSI.DEGREE_ANGLE
						).inverse()
				);
		
		//---------------------------------------------------------------
		// deltaCD
		List<Double> delta1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta1.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getDelta1VsCfCPlain(
								cfc.get(i),
								maxTicknessMeanFlap[i]
								)
						);
			else
				delta1.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getDelta1VsCfCSlotted(
								cfc.get(i),
								maxTicknessMeanFlap[i]
								)
						);
		}

		List<Double> delta2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta2.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getDelta2VsDeltaFlapPlain(deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);
			else
				delta2.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getDelta2VsDeltaFlapSlotted(
								deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								maxTicknessMeanFlap[i]
								)
						);
		}

		List<Double> delta3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			delta3.add(
					theLiftingSurface
					.getHighLiftDatabaseReader()
					.getDelta3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing()
							)
					);
		}

		for(int i=0; i<flapTypeIndex.size(); i++) {
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCDList().add(
					delta1.get(i)*
					delta2.get(i)*
					delta3.get(i)
					);
		}

		double deltaCD = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCD += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCDList().get(i).doubleValue();
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCD(deltaCD);

		//---------------------------------------------------------------
		// deltaCM_c/4
		List<Double> mu1 = new ArrayList<Double>();
		for (int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) == 3.0)
				mu1.add(
						theLiftingSurface
						.getHighLiftDatabaseReader()
						.getMu1VsCfCFirstPlain(
								(cfc.get(i))*(1/cFirstCFlap.get(i)),
								deltaFlap.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								)
						);
			else
				mu1.add(theLiftingSurface
						.getHighLiftDatabaseReader()
						.getMu1VsCfCFirstSlottedFowler((cfc.get(i))*(1/cFirstCFlap.get(i)))
						);

		List<Double> mu2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu2.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getMu2VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing()
							)
					);

		List<Double> mu3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu3.add(theLiftingSurface
					.getHighLiftDatabaseReader()
					.getMu3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theLiftingSurface.getLiftingSurfaceCreator().getTaperRatioEquivalentWing()
							)
					);

		for(int i=0; i<flapTypeIndex.size(); i++)
			theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCMc4List().add(
					(mu2.get(i)*(-(mu1.get(i)
							*theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(i)
							*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
									*((cFirstCFlap.get(i))-1)
									*(currentLiftingCoefficient + 
											(theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(i)
											*(1-(flapSurface.get(i)/theLiftingSurface
													.getSurface()
													.getEstimatedValue()))))
									*(1/8)))) + (0.7*(theLiftingSurface
											.getAspectRatio()/(1+(theLiftingSurface
													.getAspectRatio()/2)))
											*mu3.get(i)
											*theLiftingSurface.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(i)
											*Math.tan(theLiftingSurface
													.getLiftingSurfaceCreator()
													.getSweepQuarterChordEquivalentWing()
													.getEstimatedValue()))
					);

		double deltaCMC4 = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMC4 += theLiftingSurface.getTheAerodynamicsCalculator().getDeltaCMc4List().get(i).doubleValue();
		theLiftingSurface.getTheAerodynamicsCalculator().setDeltaCMc4(deltaCMC4);
	}
}