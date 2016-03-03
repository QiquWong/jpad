package sandbox.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.RRQRDecomposition;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.componentmodel.InnerCalculator;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import database.databasefunctions.DatabaseReader;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

public class StabilityCalculator {

	/**
	 * This method evaluates the tau factor reading external databases.
	 *
	 * @param chordRatio --> cf/c
	 * @param aircraft
	 * @param angle of deflection of the elevator in deg or radians
	 *
	 * @author  Manuela Ruocco
	 */

	public double calculateTauIndex(double chordRatio,
			Aircraft aircraft,
			Amount<Angle> deflection
			)
	{
		if (deflection.getUnit() == SI.RADIAN){
			deflection = deflection.to(NonSI.DEGREE_ANGLE);
		}
		double deflectionAngleDeg = deflection.getEstimatedValue();

		double aspectratioHorizontalTail = aircraft.get_HTail().get_aspectRatio();

		double etaDelta = aircraft
				.get_theAerodynamics()
				.get_highLiftDatabaseReader()
				.getEtaDeltaVsDeltaFlapPlain(deflectionAngleDeg, chordRatio);
		//System.out.println(" eta delta = " + etaDelta );

		double deltaAlpha2D = aircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				.getD_Alpha_d_Delta_2d_VS_cf_c(chordRatio);
		//System.out.println(" delta alfa 2d = " + deltaAlpha2D );

		double deltaAlpha2D3D = aircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
						aspectratioHorizontalTail,
						deltaAlpha2D
						);
		//System.out.println(" delta alfa 3d/2d = " + deltaAlpha2D3D );

		double tauIndex = deltaAlpha2D3D * deltaAlpha2D * etaDelta;
		return tauIndex;
	}

	/**
	 * This method evaluates the lift coefficient of the entire aircraft at alpha= alpha body.
	 *
	 * @param aircraft
	 * @param angle of attack between the flow direction and the fuselage reference line
	 * @param angle of deflection of the elevator in deg or radians
	 * @param MyAirfoil the mean airfoil of the wing
	 * @param chord ratio of elevator
	 * @param eta the pressure ratio. For T tail is 1.
	 *
	 *@return CL
	 *
	 * @author  Manuela Ruocco
	 */
	public double calculateCLCompleteAircraft (Aircraft aircraft,
			Amount<Angle> alphaBody,
			MyAirfoil meanAirfoil,
			Amount<Angle> deflection,
			double chordRatio,
			List<Double[]> deltaFlap,
			List<FlapTypeEnum> flapType,
			List<Double> deltaSlat,
			List<Double> etaInFlap,
			List<Double> etaOutFlap,
			List<Double> etaInSlat,
			List<Double> etaOutSlat, 
			List<Double> cfc,
			List<Double> csc,
			List<Double> leRadiusSlatRatio,
			List<Double> cExtcSlat
			)
	{
		double etaRatio = aircraft.get_HTail().getAerodynamics().get_dynamicPressureRatio();
		LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator =
				aircraft.get_wing().getAerodynamics()
				.new CalcCLAtAlpha();
		double alphaWingAngle = alphaBody.getEstimatedValue()+ aircraft.get_wing().get_iw().getEstimatedValue();
		Amount<Angle> alphaWing = Amount.valueOf(alphaWingAngle, SI.RADIAN);

		double cLWing = theCLWingCalculator.nasaBlackwellCompleteCurve(alphaWing);

		System.out.println("the CL of wing at alpha body =(deg)" +
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " is " + cLWing);

		double cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(
				alphaBody,
				meanAirfoil,
				false
				);

		System.out.println("the CL of wing body at alpha body =(deg)" +
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " is " + cLWingBody);

		LSAerodynamicsManager.CalcCLAtAlpha theCLHorizontalTailCalculator =
				aircraft.get_HTail().getAerodynamics()
				.new CalcCLAtAlpha();

		DownwashCalculator theDownwashCalculator = new DownwashCalculator(aircraft);
		theDownwashCalculator.calculateDownwashNonLinearDelft();
		double downwash = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);
		Amount<Angle> downwashAmount = Amount.valueOf(downwash, NonSI.DEGREE_ANGLE);

		double cLHTail = theCLHorizontalTailCalculator
				.getCLHTailatAlphaBodyWithElevator(
						chordRatio, alphaBody, deflection, downwashAmount, 
						deltaFlap, flapType,deltaSlat,
						etaInFlap, etaOutFlap, etaInSlat, etaOutSlat, 
						cfc, csc, leRadiusSlatRatio, cExtcSlat
						);

		System.out.println("the CL of horizontal Tail at alpha body =(deg)" +
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
				" for delta = (deg) "
				+ deflection.getEstimatedValue()
				+ " is " + cLHTail);

		double hTailSurface = aircraft.get_HTail().get_surface().getEstimatedValue();
		double wingSurface = aircraft.get_wing().get_surface().getEstimatedValue();

		double cLTotal = cLWingBody + cLHTail * (hTailSurface / wingSurface ) * etaRatio;
		return cLTotal;
	}


	/**
	 * This method calculates the shift of aerodynamic center due to fuselage 
	 *
	 * @param cMaFuselage (1/deg)
	 * @param cLalphaWing (1/rad)
	 *
	 *
	 * @author  Manuela Ruocco
	 */

	public double calcDeltaXACFuselage (double cMaFuselage, double cLAlphaWing){
	
		return -(cMaFuselage/ (cLAlphaWing/57.3));
	}
	
	
	
	/**
	 * This method calculates the Pitching moment coefficient of a wing respect a point of MAC.
	 *
	 * @param Amount<Angle> alphaLocal
	 * @param Double xPercentMAC
	 *
	 *
	 * @author  Manuela Ruocco
	 */

public class CalcPitchingMoment{

	// VARIABLE DECLARATION--------------------------------------

	LiftingSurface theLiftingSurface;
	OperatingConditions theConditions;

	double meanAerodinamicChord, xMAC, yMAC, cLLocal, qValue, alphaLocalAirfoil;
	double [] xLEActualArray, yArray, cMACAirfoils, pitchingMomentAirfoilsDueToLift,
	liftForceAirfoils, cMAirfoilsDueToLift, armMomentAirfoils, pitchingMomentLiftingSurface, cMLiftingSurfaceArray,
	yStationsNB, cLDistributionNB, chordLocal, xcPArrayLRF, xACArrayLRF, clNasaBlackwell;
	Double[] cLDistribution;
	int nPointSemiSpan;
	List<MyAirfoil> airfoilList = new ArrayList<MyAirfoil>();;
	LSAerodynamicsManager theLSManager;

	// BUILDER--------------------------------------

	public CalcPitchingMoment(LiftingSurface theLiftingSurface, OperatingConditions theConditions) {

		this.theLiftingSurface = theLiftingSurface;
		this.theConditions = theConditions;

		theLSManager = theLiftingSurface.getAerodynamics();

		meanAerodinamicChord = theLiftingSurface.get_meanAerodChordActual().getEstimatedValue();
		xMAC = theLiftingSurface.get_xLEMacActualLRF().getEstimatedValue();
		nPointSemiSpan = theLSManager.get_nPointsSemispanWise();


		// initializing array
		xLEActualArray = new double [nPointSemiSpan];
		yArray = new double [nPointSemiSpan];
		yArray = MyArrayUtils.linspace(0., theLiftingSurface.get_span().getEstimatedValue()/2, nPointSemiSpan);
		cMACAirfoils = new double [nPointSemiSpan];
		cLDistribution = new Double [nPointSemiSpan];
		pitchingMomentAirfoilsDueToLift = new double [nPointSemiSpan];
		cMAirfoilsDueToLift = new double [nPointSemiSpan];
		liftForceAirfoils = new double [nPointSemiSpan];
		armMomentAirfoils = new double [nPointSemiSpan];
		pitchingMomentLiftingSurface = new double [nPointSemiSpan];
		cMLiftingSurfaceArray = new double [nPointSemiSpan];
		chordLocal = new double [nPointSemiSpan];
		xcPArrayLRF = new double [nPointSemiSpan];
		xACArrayLRF = new double [nPointSemiSpan];

		for (int i=0; i<nPointSemiSpan ; i++){
			xLEActualArray[i] = theLiftingSurface.getXLEAtYActual(yArray[i]);
//			System.out.println("xle array " + Arrays.toString(xLEActualArray) );
			airfoilList.add(i,theLSManager.calculateIntermediateAirfoil(
					theLiftingSurface, yArray[i]) );
			chordLocal[i] = theLiftingSurface.getChordAtYActual(yArray[i]);
//			System.out.println(" chord local " + Arrays.toString(chordLocal));
			cMACAirfoils[i] = airfoilList.get(i).getAerodynamics().get_cmAC();
//			System.out.println(" cm ac airfoil " + Arrays.toString(cMACAirfoils));
			xACArrayLRF[i] = airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX()*
					chordLocal[i] + xLEActualArray[i];
		}



	}


	//METHODS--------------------------------------

	public double calculateCMIntegral (Amount<Angle> alphaLocal, double xPercentMAC){
		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
			alphaLocal = alphaLocal.to(SI.RADIAN);
//
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();

		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);

		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();
		
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
		clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
		
		for (int i=0 ; i<nPointSemiSpan ;  i++){
			cLLocal = clNasaBlackwell[i];
			qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(0.0);
			alphaLocalAirfoil = (cLLocal-qValue)/airfoilList.get(i).getAerodynamics().get_clAlpha();
			
			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
					alphaLocalAirfoil+
					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
//			System.out.println(" cl distribution " + Arrays.toString(cLDistribution));
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
//			System.out.println(" lift force " +  Arrays.toString(liftForceAirfoils));
			xcPArrayLRF [i] = chordLocal[i] *(
					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
					(cMACAirfoils[i]/cLDistribution[i]));
//			System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
			armMomentAirfoils[i] = Math.abs(xMAC + (xPercentMAC * meanAerodinamicChord))-
					 (xLEActualArray[i]+ xcPArrayLRF[i]);
//			System.out.println(" arm " +  Arrays.toString(armMomentAirfoils));
//			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) > (xLEActualArray[i]+ xcPArrayLRF[i])){
//			 armMomentAirfoils[i] = (xMAC + (xPercentMAC * meanAerodinamicChord))-
//					 (xLEActualArray[i]+ xcPArrayLRF[i]);}
//			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) < (xLEActualArray[i]+ xcPArrayLRF[i])){
//				 armMomentAirfoils[i] = (xLEActualArray[i]+ xcPArrayLRF[i]) - 
//						 (xMAC + (xPercentMAC * meanAerodinamicChord));}
//			if((xMAC + (xPercentMAC * meanAerodinamicChord)) == (xLEActualArray[i]+ xcPArrayLRF[i])){
//				armMomentAirfoils[i] = 0;
//			}
			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i];
			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
					(dynamicPressure * Math.pow(chordLocal[i], 2));
			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];

		}
		//cMACAirfoils[cMACAirfoils.length-1] = 0;
//		System.out.println(" cl " + Arrays.toString(cLDistribution));
//		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
//		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
//		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
//		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
//		System.out.println(" Chord " + Arrays.toString(chordLocal));
//		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
//		System.out.println(" dynamic pressure " + dynamicPressure);
//		System.out.println(" MAC " + meanAerodinamicChord);
		double[] yStationsND = theLSManager.get_yStationsND();
		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);

//		System.out.println(" CM " + pitchingMomentCoefficient);

		return pitchingMomentCoefficient;
	}



	public double calculateCMQuarterMACIntegral (Amount<Angle> alphaLocal){
		return calculateCMIntegral (alphaLocal, 0.25);
/*
		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
			alphaLocal = alphaLocal.to(SI.RADIAN);

		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();

		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);

		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();

		for (int i=0 ; i<nPointSemiSpan ;  i++){
			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
					alphaLocal.getEstimatedValue()+
					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
			xcPArrayLRF [i] = chordLocal[i] *(
					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
					(cMACAirfoils[i]/cLDistribution[i]));
			 armMomentAirfoils[i] = (xMAC + (0.25 * meanAerodinamicChord))-
					 (xLEActualArray[i]+ xcPArrayLRF[i]);
			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i];
			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
					(dynamicPressure * Math.pow(chordLocal[i], 2));
			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];

		}
		//cMACAirfoils[cMACAirfoils.length-1] = 0;
//		System.out.println(" cl " + Arrays.toString(cLDistribution));
//		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
//		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
//		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
//		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
//		System.out.println(" Chord " + Arrays.toString(chordLocal));
//		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
//		System.out.println(" dynamic pressure " + dynamicPressure);
//		System.out.println(" MAC " + meanAerodinamicChord);
		double[] yStationsND = theLSManager.get_yStationsND();
		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);

//		System.out.println(" CM " + pitchingMomentCoefficient);

		return pitchingMomentCoefficient;
*/
	}

	public double calculateCMIntegralACAirfoil (Amount<Angle> alphaLocal, double xPercentMAC){
		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
			alphaLocal = alphaLocal.to(SI.RADIAN);
//
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();

		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);

		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();

		for (int i=0 ; i<nPointSemiSpan ;  i++){
			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
					alphaLocal.getEstimatedValue()+
					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
//			System.out.println(" cl distribution " + Arrays.toString(cLDistribution));
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
//			System.out.println(" lift force " +  Arrays.toString(liftForceAirfoils));
			xcPArrayLRF [i] = chordLocal[i] *(
					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
					(cMACAirfoils[i]/cLDistribution[i]));
//			System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
			armMomentAirfoils[i] = Math.abs(xMAC + (xPercentMAC * meanAerodinamicChord))-
					 (xACArrayLRF[i]);
//			System.out.println(" arm " +  Arrays.toString(armMomentAirfoils));
//			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) > (xLEActualArray[i]+ xcPArrayLRF[i])){
//			 armMomentAirfoils[i] = (xMAC + (xPercentMAC * meanAerodinamicChord))-
//					 (xLEActualArray[i]+ xcPArrayLRF[i]);}
//			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) < (xLEActualArray[i]+ xcPArrayLRF[i])){
//				 armMomentAirfoils[i] = (xLEActualArray[i]+ xcPArrayLRF[i]) - 
//						 (xMAC + (xPercentMAC * meanAerodinamicChord));}
//			if((xMAC + (xPercentMAC * meanAerodinamicChord)) == (xLEActualArray[i]+ xcPArrayLRF[i])){
//				armMomentAirfoils[i] = 0;
//			}
			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i]+
					airfoilList.get(i).getAerodynamics().get_cmAC()*(dynamicPressure * Math.pow(chordLocal[i], 2)) ;
			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
					(dynamicPressure * Math.pow(chordLocal[i], 2));
			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];

		}
		//cMACAirfoils[cMACAirfoils.length-1] = 0;
//		System.out.println(" cl " + Arrays.toString(cLDistribution));
//		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
//		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
//		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
//		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
//		System.out.println(" Chord " + Arrays.toString(chordLocal));
//		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
//		System.out.println(" dynamic pressure " + dynamicPressure);
//		System.out.println(" MAC " + meanAerodinamicChord);
		double[] yStationsND = theLSManager.get_yStationsND();
		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);

//		System.out.println(" CM " + pitchingMomentCoefficient);

		return pitchingMomentCoefficient;
	}
	public void plotCMatAlpha(Amount<Angle> alphaLocal, String subfolderPath){
		calculateCMQuarterMACIntegral(alphaLocal);

		double[] yStationsND = theLSManager.get_yStationsND();
		MyChartToFileUtils.plotNoLegend(
				yStationsND , cMLiftingSurfaceArray,
				null, null, null, null,
				"eta stat.", "CM",
				"", "",
				subfolderPath," Moment Coefficient distribution for " + theLiftingSurface.get_type() );



	}


	/**
	 * This method calculates AC of a lifting surface as a percentage of MAC 
	 *
	 * @return Double xPercentMAC
	 *
	 *
	 * @author  Manuela Ruocco
	 */
	
	
	public double getACLiftingSurface(){
		double cMTempAlphaFirst;
		double cMTempAlphaSecond;
		double cMDiff;
		double percent = 0.25;
		
		Amount<Angle> alphaFirst;
		Amount<Angle> alphaSecond;
		
		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator = theLiftingSurface.getAerodynamics().new MeanAirfoil();
		MyAirfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theLiftingSurface);
	
		alphaFirst = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		alphaSecond= Amount.valueOf(2.0, NonSI.DEGREE_ANGLE);
//		alphaSecond = Amount.valueOf(
//				meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue()/2,
//				SI.RADIAN);
//		
		cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
		cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);
		
		cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
//		System.out.println(" cm first " + cMTempAlphaFirst);
//		System.out.println(" cm second " + cMTempAlphaSecond);
//		System.out.println("cm diff " + cMDiff);
		while ( Math.abs(cMDiff) > 0.00003){
			if ((cMTempAlphaFirst > 0 & cMTempAlphaSecond <0) || ( (cMTempAlphaSecond - cMTempAlphaFirst) < 0)){

				percent = percent + 0.0001;	
//				System.out.println(" percent " + percent);
				cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
				cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);
				cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
//				System.out.println(" first");
//				System.out.println(" cm first " + cMTempAlphaFirst);
//				System.out.println(" cm second " + cMTempAlphaSecond);
//				System.out.println("cm diff " + cMDiff);
			}
			
				if ((cMTempAlphaFirst < 0 & cMTempAlphaSecond > 0) || ( (cMTempAlphaSecond - cMTempAlphaFirst) > 0)){

					percent = percent - 0.0001;	
//					System.out.println(" percent " + percent);
					cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
					cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);	
					cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
//					System.out.println("second");
//					System.out.println(" cm first " + cMTempAlphaFirst);
//					System.out.println(" cm second " + cMTempAlphaSecond);
//					System.out.println("cm diff " + cMDiff);
//				}
				}}
				
		return percent;
		
		
	}
	
	
	
	public double[] getcMLiftingSurfaceArray() {
		return cMLiftingSurfaceArray;
	}


	public double[] getyArray() {
		return yArray;
	}
	
}

/**
 * This class manages the calculation of the Pitching moment coefficient due to the power plant.
 * The method that calculates the pitching moment slope respect CL recognize the aircraft engine type
 *  
 * 
 *
 * @author  Manuela Ruocco
 */

//TOD0 --> set nacelle distance (z,x)

public class CalcPowerPlantPitchingMoment{
	
	double pitchingMomentDerThrust;
	double dCmnddCL = 0;
	
	public double calcPitchingMomentDerThrust (Aircraft aircraft, 
			OperatingConditions conditions, 
			double etaEfficiency, double liftCoefficient){
		
		Amount<Power> power = aircraft.get_powerPlant().get_engineList().get(0).get_p0();
		double engineNumber = aircraft.get_powerPlant().get_engineNumber();
		double powerDouble = engineNumber * power.getEstimatedValue();
		double density = conditions.get_densityCurrent().getEstimatedValue();

		double surface = aircraft.get_wing().get_surface().getEstimatedValue();
		double wingLoad = (liftCoefficient*0.5 * density*Math.pow(
				conditions.get_tas().getEstimatedValue(), 2)/9.81);

		double meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordEq().getEstimatedValue();
//		System.out.println(" lift coefficient " + liftCoefficient);
		double kFactor = (3 * powerDouble) /( 2 * Math.sqrt((2/density))) * Math.pow( wingLoad , (3/2));
		kFactor = 0.2;
		double thrustArm = aircraft.get_powerPlant().get_engineList().get(0).get_Z0().getEstimatedValue();
		pitchingMomentDerThrust = kFactor* Math.sqrt(liftCoefficient) * (thrustArm/meanAerodynamicChord) / surface;
		
		return pitchingMomentDerThrust;
	}
	

	public double calcPitchingMomentDerNonAxial (Aircraft aircraft, 
			double nBlades, double diameter,
			double clAlpha){
		
		if( aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP){
		double dCndAlpha=0.0;
		
		if(nBlades == 2)
			dCndAlpha = 0.0024; // 1/deg
		
		if(nBlades == 4)
			dCndAlpha = 0.0040; // 1/deg
		
		if(nBlades == 6)
			dCndAlpha = 0.0065; // 1/deg
		
		double surfaceP = Math.PI * Math.pow(diameter/2, 2);
		double surface = aircraft.get_wing().get_surface().getEstimatedValue();
		double surfaceRatio = surfaceP/surface;
//		System.out.println(" surface ratio " + surfaceRatio);
		double numProp = aircraft.get_powerPlant().get_engineNumber();
		
		double distance = 1.4; // set this
		double meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue();
		//double depsdalpha = 1.5;
		double position = -distance/aircraft.get_wing().getChordAtYActual(0.0);
		// System.out.println(" position " + position);
		double depsdalpha = aircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
						position,
						aircraft.get_wing().get_aspectRatio()
						);
		dCmnddCL = dCndAlpha * surfaceRatio * 
				(distance / meanAerodynamicChord) * (depsdalpha/clAlpha) * numProp;
		}
		
		
		return dCmnddCL;
		
	}

	public double getPitchingMomentDerThrust() {
		return pitchingMomentDerThrust;
	}

	public void setPitchingMomentDerThrust(double pitchingMomentDerThrust) {
		this.pitchingMomentDerThrust = pitchingMomentDerThrust;
	}
}
}
