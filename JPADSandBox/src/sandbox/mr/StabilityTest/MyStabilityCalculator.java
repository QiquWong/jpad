package sandbox.mr.StabilityTest;
//package sandbox.mr;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.measure.quantity.Angle;
//import javax.measure.quantity.Power;
//import javax.measure.unit.NonSI;
//import javax.measure.unit.SI;
//
//import org.apache.commons.math3.linear.RRQRDecomposition;
//import org.jscience.physics.amount.Amount;
//
//import aircraft.OperatingConditions;
//import aircraft.auxiliary.airfoil.MyAirfoil;
//import aircraft.componentmodel.InnerCalculator;
//import aircraft.components.Aircraft;
//import aircraft.components.liftingSurface.LSAerodynamicsManager;
//import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCDAtAlpha;
//import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
//import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
//import database.databasefunctions.DatabaseReader;
//import aircraft.components.liftingSurface.LiftingSurface;
//import configuration.enumerations.ComponentEnum;
//import configuration.enumerations.EngineTypeEnum;
//import configuration.enumerations.FlapTypeEnum;
//import configuration.enumerations.MethodEnum;
//import standaloneutils.MyArrayUtils;
//import standaloneutils.MyChartToFileUtils;
//import standaloneutils.MyMathUtils;
//import standaloneutils.customdata.CenterOfGravity;
//import standaloneutils.customdata.MyArray;
//
//public class MyStabilityCalculator {
//
//	double deltaACWingBody;
//	
//	/**
//	 * This method evaluates the tau factor reading external databases.
//	 *
//	 * @param chordRatio --> cf/c
//	 * @param aircraft
//	 * @param angle of deflection of the elevator in deg or radians
//	 *
//	 * @author  Manuela Ruocco
//	 */
//
//	public double calculateTauIndex(double chordRatio,
//			Aircraft aircraft,
//			Amount<Angle> deflection
//			)
//	{
//		if (deflection.getUnit() == SI.RADIAN){
//			deflection = deflection.to(NonSI.DEGREE_ANGLE);
//		}
//		double deflectionAngleDeg = deflection.getEstimatedValue();
//
//		double aspectratioHorizontalTail = aircraft.get_HTail().get_aspectRatio();
//
//		double etaDelta = aircraft
//				.get_theAerodynamics()
//				.get_highLiftDatabaseReader()
//				.getEtaDeltaVsDeltaFlapPlain(deflectionAngleDeg, chordRatio);
//		//System.out.println(" eta delta = " + etaDelta );
//
//		double deltaAlpha2D = aircraft
//				.get_theAerodynamics()
//				.get_aerodynamicDatabaseReader()
//				.getD_Alpha_d_Delta_2d_VS_cf_c(chordRatio);
//		//System.out.println(" delta alfa 2d = " + deltaAlpha2D );
//
//		double deltaAlpha2D3D = aircraft
//				.get_theAerodynamics()
//				.get_aerodynamicDatabaseReader()
//				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
//						aspectratioHorizontalTail,
//						deltaAlpha2D
//						);
//		//System.out.println(" delta alfa 3d/2d = " + deltaAlpha2D3D );
//
//		double tauIndex = deltaAlpha2D3D * deltaAlpha2D * etaDelta;
//		return tauIndex;
//	}
//
//	/**
//	 * This method evaluates the lift coefficient of the entire aircraft at alpha= alpha body.
//	 *
//	 * @param aircraft
//	 * @param angle of attack between the flow direction and the fuselage reference line
//	 * @param angle of deflection of the elevator in deg or radians
//	 * @param MyAirfoil the mean airfoil of the wing
//	 * @param chord ratio of elevator
//	 * @param eta the pressure ratio. For T tail is 1.
//	 *
//	 *@return CL
//	 *
//	 * @author  Manuela Ruocco
//	 */
//	public double calculateCLCompleteAircraft (Aircraft aircraft,
//			Amount<Angle> alphaBody,
//			MyAirfoil meanAirfoil,
//			Amount<Angle> deflection,
//			double chordRatio,
//			List<Double[]> deltaFlap,
//			List<FlapTypeEnum> flapType,
//			List<Double> deltaSlat,
//			List<Double> etaInFlap,
//			List<Double> etaOutFlap,
//			List<Double> etaInSlat,
//			List<Double> etaOutSlat, 
//			List<Double> cfc,
//			List<Double> csc,
//			List<Double> leRadiusSlatRatio,
//			List<Double> cExtcSlat
//			)
//	{
//		double etaRatio = aircraft.get_HTail().getAerodynamics().get_dynamicPressureRatio();
//		LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator =
//				aircraft.get_wing().getAerodynamics()
//				.new CalcCLAtAlpha();
//		double alphaWingAngle = alphaBody.getEstimatedValue()+ aircraft.get_wing().get_iw().getEstimatedValue();
//		Amount<Angle> alphaWing = Amount.valueOf(alphaWingAngle, SI.RADIAN);
//
//		double cLWing = theCLWingCalculator.nasaBlackwellCompleteCurve(alphaWing);
//
//		System.out.println("the CL of wing at alpha body =(deg)" +
//				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
//				+ " is " + cLWing);
//
//		double cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(
//				alphaBody,
//				meanAirfoil,
//				false
//				);
//
//		System.out.println("the CL of wing body at alpha body =(deg)" +
//				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
//				+ " is " + cLWingBody);
//
//		LSAerodynamicsManager.CalcCLAtAlpha theCLHorizontalTailCalculator =
//				aircraft.get_HTail().getAerodynamics()
//				.new CalcCLAtAlpha();
//
//		DownwashCalculator theDownwashCalculator = new DownwashCalculator(aircraft);
//		theDownwashCalculator.calculateDownwashNonLinearDelft();
//		double downwash = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);
//		Amount<Angle> downwashAmount = Amount.valueOf(downwash, NonSI.DEGREE_ANGLE);
//
//		double cLHTail = theCLHorizontalTailCalculator
//				.getCLHTailatAlphaBodyWithElevator(
//						chordRatio, alphaBody, deflection, downwashAmount, 
//						deltaFlap, flapType,deltaSlat,
//						etaInFlap, etaOutFlap, etaInSlat, etaOutSlat, 
//						cfc, csc, leRadiusSlatRatio, cExtcSlat
//						);
//
//		System.out.println("the CL of horizontal Tail at alpha body =(deg)" +
//				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
//				" for delta = (deg) "
//				+ deflection.getEstimatedValue()
//				+ " is " + cLHTail);
//
//		double hTailSurface = aircraft.get_HTail().get_surface().getEstimatedValue();
//		double wingSurface = aircraft.get_wing().get_surface().getEstimatedValue();
//
//		double cLTotal = cLWingBody + cLHTail * (hTailSurface / wingSurface ) * etaRatio;
//		return cLTotal;
//	}
//
//
//	/**
//	 * This method calculates the shift of aerodynamic center due to fuselage 
//	 *
//	 * @param cMaFuselage (1/deg)
//	 * @param cLalphaWing (1/rad)
//	 *
//	 *
//	 * @author  Manuela Ruocco
//	 */
//
//	public double calcDeltaXACFuselage (double cMaFuselage, double cLAlphaWing){
//	
//		deltaACWingBody = -(cMaFuselage/ (cLAlphaWing/57.3));
//		return deltaACWingBody;
//	}
//	
//	
//	
//	/**
//	 * This method calculates the Pitching moment coefficient of a lifting surface
//	 *  respect a given point of MAC.
//	 *
//	 *
//	 *
//	 * @author  Manuela Ruocco
//	 */
//
//public class CalcPitchingMomentAC{
//
//	// VARIABLE DECLARATION--------------------------------------
//
//	LiftingSurfaceCreator theLiftingSurface;
//	OperatingConditions theConditions;
//
//	double meanAerodinamicChord, xMAC, yMAC, cLLocal, qValue, alphaLocalAirfoil;
//	double [] xLEActualArray, yArray, cMACAirfoils, pitchingMomentAirfoilsDueToLift,
//	liftForceAirfoils, cMAirfoilsDueToLift, armMomentAirfoils, pitchingMomentLiftingSurface, cMLiftingSurfaceArray,
//	yStationsNB, cLDistributionNB, chordLocal, xcPArrayLRF, xACArrayLRF, clNasaBlackwell;
//	Double[] cLDistribution;
//	int nPointSemiSpan;
//	List<MyAirfoil> airfoilList = new ArrayList<MyAirfoil>();
//	LSAerodynamicsManager theLSManager;
//
//	// BUILDER--------------------------------------
//
//	public CalcPitchingMomentAC(LiftingSurfaceCreator theLiftingSurface, OperatingConditions theConditions) {
//
//		this.theLiftingSurface = theLiftingSurface;
//		this.theConditions = theConditions;
//
//		theLSManager = theLiftingSurface.getAerodynamics();
//
//		meanAerodinamicChord = theLiftingSurface.get_meanAerodChordActual().getEstimatedValue();
//		xMAC = theLiftingSurface.get_xLEMacActualLRF().getEstimatedValue();
//		nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
//
//
//		// initializing array
//		xLEActualArray = new double [nPointSemiSpan];
//		yArray = new double [nPointSemiSpan];
//		yArray = MyArrayUtils.linspace(0., theLiftingSurface.get_span().getEstimatedValue()/2, nPointSemiSpan);
//		cMACAirfoils = new double [nPointSemiSpan];
//		cLDistribution = new Double [nPointSemiSpan];
//		pitchingMomentAirfoilsDueToLift = new double [nPointSemiSpan];
//		cMAirfoilsDueToLift = new double [nPointSemiSpan];
//		liftForceAirfoils = new double [nPointSemiSpan];
//		armMomentAirfoils = new double [nPointSemiSpan];
//		pitchingMomentLiftingSurface = new double [nPointSemiSpan];
//		cMLiftingSurfaceArray = new double [nPointSemiSpan];
//		chordLocal = new double [nPointSemiSpan];
//		xcPArrayLRF = new double [nPointSemiSpan];
//		xACArrayLRF = new double [nPointSemiSpan];
//
//		for (int i=0; i<nPointSemiSpan ; i++){
//			xLEActualArray[i] = theLiftingSurface.getXLEAtYActual(yArray[i]);
////			System.out.println("xle array " + Arrays.toString(xLEActualArray) );
//			airfoilList.add(i,theLSManager.calculateIntermediateAirfoil(
//					theLiftingSurface, yArray[i]) );
//			chordLocal[i] = theLiftingSurface.getChordAtYActual(yArray[i]);
////			System.out.println(" chord local " + Arrays.toString(chordLocal));
//			cMACAirfoils[i] = airfoilList.get(i).getAerodynamics().get_cmAC();
////			System.out.println(" cm ac airfoil " + Arrays.toString(cMACAirfoils));
//			xACArrayLRF[i] = airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX()*
//					chordLocal[i] + xLEActualArray[i];
//		}
//
//
//
//	}
//
//
//	//METHODS--------------------------------------
//
//	public double calculateCMIntegral (Amount<Angle> alphaLocal, double xPercentMAC){
//		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
//			alphaLocal = alphaLocal.to(SI.RADIAN);
////
////		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
////		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
//		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
////		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();
//
//		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);
//
//		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();
//		
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
//		clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		
////		System.out.println("\n\n alpha input " + alphaLocal.getEstimatedValue()*57.3);
//		for (int i=0 ; i<nPointSemiSpan ;  i++){
//			cLLocal = clNasaBlackwell[i];
////			System.out.println( " cl local " + cLLocal);
//			qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(0.0);
////			System.out.println(" qValue " + qValue );
//			alphaLocalAirfoil = (cLLocal-qValue)/airfoilList.get(i).getAerodynamics().get_clAlpha();
////			System.out.println(" alpha local airfoil " + alphaLocalAirfoil);
//			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
//					 //alphaLocal.getEstimatedValue()+
//					alphaLocalAirfoil+
//					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
////			System.out.println(" cl local " + cLDistribution[i]);
////			System.out.println(" cl distribution " + Arrays.toString(cLDistribution));
//			if(theLiftingSurface.get_type().equals(ComponentEnum.HORIZONTAL_TAIL)){
//				liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure *
//						theLiftingSurface.getAerodynamics().get_dynamicPressureRatio()*
//						chordLocal[i];
//			}
//			else
//			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
////			System.out.println(" lift force " +  Arrays.toString(liftForceAirfoils));
//			xcPArrayLRF [i] = chordLocal[i] *(
//					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
//					(cMACAirfoils[i]/cLDistribution[i]));
////			System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
//			armMomentAirfoils[i] = Math.abs(xMAC + (xPercentMAC * meanAerodinamicChord))-
//					 (xLEActualArray[i]+ xcPArrayLRF[i]);
////			System.out.println(" arm " +  Arrays.toString(armMomentAirfoils));
////			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) > (xLEActualArray[i]+ xcPArrayLRF[i])){
////			 armMomentAirfoils[i] = (xMAC + (xPercentMAC * meanAerodinamicChord))-
////					 (xLEActualArray[i]+ xcPArrayLRF[i]);}
////			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) < (xLEActualArray[i]+ xcPArrayLRF[i])){
////				 armMomentAirfoils[i] = (xLEActualArray[i]+ xcPArrayLRF[i]) - 
////						 (xMAC + (xPercentMAC * meanAerodinamicChord));}
////			if((xMAC + (xPercentMAC * meanAerodinamicChord)) == (xLEActualArray[i]+ xcPArrayLRF[i])){
////				armMomentAirfoils[i] = 0;
////			}
//			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i];
//			
//			if(theLiftingSurface.get_type().equals(ComponentEnum.HORIZONTAL_TAIL)){
//			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
//					(dynamicPressure * 
//							theLiftingSurface.getAerodynamics().get_dynamicPressureRatio()*
//							Math.pow(chordLocal[i], 2));}
//			else{
//				cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
//				(dynamicPressure * Math.pow(chordLocal[i], 2));}
//				
//			cMLiftingSurfaceArray[cMLiftingSurfaceArray.length-1] = 0;
//			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];
//
//		}
//		//cMACAirfoils[cMACAirfoils.length-1] = 0;
////		System.out.println(" cl " + Arrays.toString(cLDistribution));
////		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
////		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
////		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
////		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
////		System.out.println(" Chord " + Arrays.toString(chordLocal));
////		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
////		System.out.println(" dynamic pressure " + dynamicPressure);
////		System.out.println(" MAC " + meanAerodinamicChord);
//		double[] yStationsND = theLSManager.get_yStationsND();
//		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);
//
////		System.out.println(" CM " + pitchingMomentCoefficient);
//
//		return pitchingMomentCoefficient;
//	}
//
//
//
//	public double calculateCMQuarterMACIntegral (Amount<Angle> alphaLocal){
//		return calculateCMIntegral (alphaLocal, 0.25);
///*
//		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
//			alphaLocal = alphaLocal.to(SI.RADIAN);
//
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
//		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();
//
//		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);
//
//		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();
//
//		for (int i=0 ; i<nPointSemiSpan ;  i++){
//			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
//					alphaLocal.getEstimatedValue()+
//					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
//			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
//			xcPArrayLRF [i] = chordLocal[i] *(
//					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
//					(cMACAirfoils[i]/cLDistribution[i]));
//			 armMomentAirfoils[i] = (xMAC + (0.25 * meanAerodinamicChord))-
//					 (xLEActualArray[i]+ xcPArrayLRF[i]);
//			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i];
//			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
//					(dynamicPressure * Math.pow(chordLocal[i], 2));
//			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];
//
//		}
//		//cMACAirfoils[cMACAirfoils.length-1] = 0;
////		System.out.println(" cl " + Arrays.toString(cLDistribution));
////		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
////		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
////		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
////		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
////		System.out.println(" Chord " + Arrays.toString(chordLocal));
////		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
////		System.out.println(" dynamic pressure " + dynamicPressure);
////		System.out.println(" MAC " + meanAerodinamicChord);
//		double[] yStationsND = theLSManager.get_yStationsND();
//		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);
//
////		System.out.println(" CM " + pitchingMomentCoefficient);
//
//		return pitchingMomentCoefficient;
//*/
//	}
//
//	public double calculateCMIntegralACAirfoil (Amount<Angle> alphaLocal, double xPercentMAC){
//		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
//			alphaLocal = alphaLocal.to(SI.RADIAN);
////
////		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
////		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
//		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
////		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();
//
//		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);
//
//		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();
//
//		for (int i=0 ; i<nPointSemiSpan ;  i++){
//			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
//					alphaLocal.getEstimatedValue()+
//					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
////			System.out.println(" cl distribution " + Arrays.toString(cLDistribution));
//			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
////			System.out.println(" lift force " +  Arrays.toString(liftForceAirfoils));
//			xcPArrayLRF [i] = chordLocal[i] *(
//					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
//					(cMACAirfoils[i]/cLDistribution[i]));
////			System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
//			armMomentAirfoils[i] = Math.abs(xMAC + (xPercentMAC * meanAerodinamicChord))-
//					 (xACArrayLRF[i]);
////			System.out.println(" arm " +  Arrays.toString(armMomentAirfoils));
////			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) > (xLEActualArray[i]+ xcPArrayLRF[i])){
////			 armMomentAirfoils[i] = (xMAC + (xPercentMAC * meanAerodinamicChord))-
////					 (xLEActualArray[i]+ xcPArrayLRF[i]);}
////			if ( (xMAC + (xPercentMAC * meanAerodinamicChord)) < (xLEActualArray[i]+ xcPArrayLRF[i])){
////				 armMomentAirfoils[i] = (xLEActualArray[i]+ xcPArrayLRF[i]) - 
////						 (xMAC + (xPercentMAC * meanAerodinamicChord));}
////			if((xMAC + (xPercentMAC * meanAerodinamicChord)) == (xLEActualArray[i]+ xcPArrayLRF[i])){
////				armMomentAirfoils[i] = 0;
////			}
//			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i]+
//					airfoilList.get(i).getAerodynamics().get_cmAC()*(dynamicPressure * Math.pow(chordLocal[i], 2)) ;
//			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
//					(dynamicPressure * Math.pow(chordLocal[i], 2));
//			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];
//
//		}
//		//cMACAirfoils[cMACAirfoils.length-1] = 0;
////		System.out.println(" cl " + Arrays.toString(cLDistribution));
////		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
////		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
////		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
////		System.out.println(" cm total " + Arrays.toString(cMLiftingSurfaceArray));
////		System.out.println(" Chord " + Arrays.toString(chordLocal));
////		System.out.println(" arms " + Arrays.toString(armMomentAirfoils));
////		System.out.println(" dynamic pressure " + dynamicPressure);
////		System.out.println(" MAC " + meanAerodinamicChord);
//		double[] yStationsND = theLSManager.get_yStationsND();
//		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yStationsND, cMLiftingSurfaceArray);
//
////		System.out.println(" CM " + pitchingMomentCoefficient);
//
//		return pitchingMomentCoefficient;
//	}
//	public void plotCMatAlpha(Amount<Angle> alphaLocal, String subfolderPath){
//		calculateCMQuarterMACIntegral(alphaLocal);
//
//		double[] yStationsND = theLSManager.get_yStationsND();
//		MyChartToFileUtils.plotNoLegend(
//				yStationsND , cMLiftingSurfaceArray,
//				null, null, null, null,
//				"eta stat.", "CM",
//				"", "",
//				subfolderPath," Moment Coefficient distribution for " + theLiftingSurface.get_type() );
//
//
//
//	}
//
//
//	/**
//	 * This method calculates AC of a lifting surface as a percentage of MAC 
//	 *
//	 * @return Double xPercentMAC
//	 *
//	 *
//	 * @author  Manuela Ruocco
//	 */
//	
//	
//	public double getACLiftingSurface(){
//		double cMTempAlphaFirst;
//		double cMTempAlphaSecond;
//		double cMDiff;
//		double percent = 0.25;
//		
//		Amount<Angle> alphaFirst;
//		Amount<Angle> alphaSecond;
//		
//		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator = theLiftingSurface.getAerodynamics().new MeanAirfoil();
//		MyAirfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theLiftingSurface);
//	
//		alphaFirst = Amount.valueOf(0.1, NonSI.DEGREE_ANGLE);
//		alphaSecond= Amount.valueOf(2.0, NonSI.DEGREE_ANGLE);
////		alphaSecond = Amount.valueOf(
////				meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue()/2,
////				SI.RADIAN);
////		
//		cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
//		cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);
//		
//		cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
////		System.out.println(" cm first " + cMTempAlphaFirst);
////		System.out.println(" cm second " + cMTempAlphaSecond);
////		System.out.println("cm diff " + cMDiff);
//		while ( Math.abs(cMDiff) > 0.00003){
//			if ((cMTempAlphaFirst > 0 & cMTempAlphaSecond <0) || ( (cMTempAlphaSecond - cMTempAlphaFirst) < 0)){
//
//				percent = percent + 0.0001;	
////				System.out.println(" percent " + percent);
//				cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
//				cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);
//				cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
////				System.out.println(" first");
////				System.out.println(" cm first " + cMTempAlphaFirst);
////				System.out.println(" cm second " + cMTempAlphaSecond);
////				System.out.println("cm diff " + cMDiff);
//			}
//			
//				if ((cMTempAlphaFirst < 0 & cMTempAlphaSecond > 0) || ( (cMTempAlphaSecond - cMTempAlphaFirst) > 0)){
//
//					percent = percent - 0.0001;	
////					System.out.println(" percent " + percent);
//					cMTempAlphaFirst = calculateCMIntegral ( alphaFirst, percent);
//					cMTempAlphaSecond = calculateCMIntegral ( alphaSecond, percent);	
//					cMDiff = cMTempAlphaSecond-cMTempAlphaFirst;
////					System.out.println("second");
////					System.out.println(" cm first " + cMTempAlphaFirst);
////					System.out.println(" cm second " + cMTempAlphaSecond);
////					System.out.println("cm diff " + cMDiff);
////				}
//				}}
//				
//		return percent;
//		
//		
//	}
//	
//	
//	
//	public double[] getcMLiftingSurfaceArray() {
//		return cMLiftingSurfaceArray;
//	}
//
//
//	public double[] getyArray() {
//		return yArray;
//	}
//	
//}
//
//
//
///**
// * This class calculates the Pitching moment coefficient respect CG.
// *
// * @author  Manuela Ruocco
// */
//
//public class CalcPitchingMomentCG{
//	
//	// Variable Declaration
//	
//	OperatingConditions theConditions;
//	Aircraft theAircraft;
//	
//	int nValue = 30;
//	int nValueRed = 6;
//	double alphaFirst = -5.0;
//	double alphaEnd = 15.0;
//	double [] alphaBodyArray = MyArrayUtils.linspace(alphaFirst, alphaEnd, nValue);
////	double [] alphaBodyArrayComplete = MyArrayUtils.linspace(alphaFirst, alphaEnd, nValue);
//	
//	double [] cMvsAlphaWingArray = new double [nValue];
//	double [] cMvsAlphaWingBodyArray = new double [nValue];
//	double [] cMvsAlphaHTailArray = new double [nValue];
//	double [] cMvsAlphaThrustArray = new double [nValue];
//	double [] cMvsAlphaCompleteArray =new double [nValue];
//	
//	double cm0Fuselage;
//	double xBRFcg;
//	double zBRFcg;
//	 
//	Amount<Angle> alphaWing;
//	Amount<Angle> alphaBody;
//	Amount<Angle> alphaTail;
//	Amount<Angle> downwash;
//
//
//	List<Double[]> deltaFlap = new ArrayList<Double[]>();
//	List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
//	List<Double> eta_in_flap = new ArrayList<Double>();
//	List<Double> eta_out_flap = new ArrayList<Double>();
//	List<Double> cf_c = new ArrayList<Double>();
//	
//	double [] cLWingBody = new double [nValue];
//	double [] cDWingBody = new double [nValue];
//	double [] cNWingBody = new double [nValue];
//	double [] cCWingBody = new double [nValue];
//	double [] cMACWingBody = new double [nValue];
//	
//	double [] cLIsolatedWing = new double [nValue];
//	double [] cDIsolatedWing = new double [nValue];
//	double [] cNIsolatedWing = new double [nValue];
//	double [] cCIsolatedWing = new double [nValue];
//	double [] cMACIsolatedWing = new double [nValue];
//
//	double [] cLhTail = new double [nValue];
//	double [] cDhTail = new double [nValue];
//	double [] cNhTail = new double [nValue];
//	double [] cChTail = new double [nValue];
//	double [] cMAChTail = new double [nValue];
//	
//	public CalcPitchingMomentCG(CenterOfGravity cgPosition, OperatingConditions theConditions, Aircraft theAircraft,
//			List<Double[]> deltaFlap,
//			List<FlapTypeEnum> flapType,
//			List<Double> deltaSlat,
//			List<Double> etaInFlap,
//			List<Double> etaOutFlap,
//			List<Double> etaInSlat,
//			List<Double> etaOutSlat, 
//			List<Double> cfc,
//			List<Double> csc,
//			List<Double> leRadiusSlatRatio,
//			List<Double> cExtcSlat,
//			double cm0Fuselage
//			) {
//		
//		
//		this.theAircraft = theAircraft;
//		this.theConditions = theConditions;
//		this.deltaFlap = deltaFlap;
//		this.flapType = flapType;
//		this.eta_in_flap = etaInFlap;
//		this.eta_out_flap = etaOutFlap;
//		this.cf_c = cfc;
//		this.cm0Fuselage = cm0Fuselage;
//		
//		this.xBRFcg = cgPosition.get_xBRF().getEstimatedValue();
//		this.zBRFcg = cgPosition.get_zBRF().getEstimatedValue();
//	}
//	
//	/**
//	 * This method calculates the Pitching moment coefficient of a component respect CG.
//	 *
//	 * @author  Manuela Ruocco
//	 */
//	
//	public void calculateCMvsAlphaComponent(ComponentEnum component){
//		
//		if (component == ComponentEnum.WING){
//			double aCWing, acWingBody;
//	
//			LiftingSurfaceCreator theWing = theAircraft.get_wing();
//			LSAerodynamicsManager theLSManager = theAircraft.get_wing().getAerodynamics();
//			LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = 
//					theLSManager
//					.new CalcCLAtAlpha();
//			LSAerodynamicsManager.CalcCDAtAlpha theCDWingCalculator = theLSManager
//					.new CalcCDAtAlpha();
//			
//			CalcPitchingMomentAC theCMACCalculator = new CalcPitchingMomentAC(theWing, theConditions);
//			
//			aCWing = theCMACCalculator.getACLiftingSurface();
//
//			// CG coordinates
//			
//			double xW = xBRFcg - (aCWing * theWing.get_meanAerodChordActual().getEstimatedValue() + 
//					theWing.get_xLEMacActualBRF().getEstimatedValue());
////			System.out.println(" xle brf mac " + theWing.get_xLEMacActualBRF().getEstimatedValue() );
//			double zW = -(zBRFcg - theWing.get_aerodynamicCenterZ().getEstimatedValue());
//			
////			System.out.println(" xw " + xW);
////			System.out.println(" zW " + zW);
////			
////			System.out.println(" xc " + xW/theWing.get_meanAerodChordActual().getEstimatedValue());
////			System.out.println(" zc " + zW/theWing.get_meanAerodChordActual().getEstimatedValue());
//			
//			for (int i=0 ; i<nValue ; i++){
//			  alphaWing = Amount.valueOf(
//					  Math.toRadians(alphaBodyArray[i]+ theWing.get_iw().to(NonSI.DEGREE_ANGLE)
//							  .getEstimatedValue()),
//					  SI.RADIAN);
//			  alphaBody = Amount.valueOf(Math.toRadians(alphaBodyArray[i]), SI.RADIAN);
//			  
//			  
//			  cLIsolatedWing[i] = theCLWingCalculator.nasaBlackwellAlphaBody(alphaBody);
//			  cDIsolatedWing[i] =  theCDWingCalculator.integralFromCdAirfoil(
//						alphaWing, MethodEnum.NASA_BLACKWELL, theLSManager);
//			
//				  
//			  cNIsolatedWing[i] =  cLIsolatedWing[i] * Math.cos(alphaWing.getEstimatedValue()) +
//					  cDIsolatedWing[i] * Math.sin(alphaWing.getEstimatedValue());
//			  
//			  cCIsolatedWing[i] = cDIsolatedWing[i] * Math.cos(alphaWing.getEstimatedValue()) -
//					  cLIsolatedWing[i] * Math.sin(alphaWing.getEstimatedValue());
//			  
//			  
//			  cMACIsolatedWing[i] = theCMACCalculator.calculateCMIntegral(alphaWing, aCWing);
//			  
//			  
//			  cMvsAlphaWingArray [i] = cNIsolatedWing[i] * (xW/theWing.get_meanAerodChordActual().getEstimatedValue())
//					  + cCIsolatedWing[i]*(zW/theWing.get_meanAerodChordActual().getEstimatedValue())
//					  + cMACIsolatedWing[i];
////			  
////			  cMvsAlphaWingArray [i] = cNIsolatedWing[i] * 0.05
////					  + cCIsolatedWing[i]*0.1
////					  + cMACIsolatedWing[i];
//			  
//			}
////			for (int i=0; i<nValue ;i++){
////				
////			cMvsAlphaWingArray[i] = MyMathUtils
////					.getInterpolatedValue1DSpline(alphaBodyArray, cMvsAlphaWingArray , alphaBodyArrayComplete[i]);
////			cLIsolatedWing[i] =  MyMathUtils
////					.getInterpolatedValue1DSpline(alphaBodyArray, cLIsolatedWing , alphaBodyArrayComplete[i]);
////			cDIsolatedWing[i] =  MyMathUtils
////					.getInterpolatedValue1DSpline(alphaBodyArray, cLIsolatedWing , alphaBodyArrayComplete[i]);
////			}
//			}
//		if (component == ComponentEnum.HORIZONTAL_TAIL){
//			double aChTail;
//			
//			
//			LiftingSurfaceCreator theHorizontalTail = theAircraft.get_HTail();
//			LSAerodynamicsManager theLSManager = theAircraft.get_HTail().getAerodynamics();
//			LSAerodynamicsManager.CalcCLAtAlpha theCLhTailCalculator = 
//					theLSManager
//					.new CalcCLAtAlpha();
//			LSAerodynamicsManager.CalcCDAtAlpha theCDhTailCalculator = theLSManager
//					.new CalcCDAtAlpha();
//			LSAerodynamicsManager.CalcHighLiftDevices theHighLiftCalc = theLSManager.new 	CalcHighLiftDevices(theHorizontalTail, theConditions,
//					deltaFlap, flapType,null,
//					eta_in_flap, eta_out_flap, null, null, 
//					cf_c, null, null, null
//					);
//			
//			CalcPitchingMomentAC theCMACCalculator = new CalcPitchingMomentAC(theHorizontalTail, theConditions);
//			DownwashCalculator theDownwashCalculator = new DownwashCalculator(theAircraft);
//			theDownwashCalculator.calculateDownwashNonLinearDelft();
//			aChTail = theLSManager.getCalculateXAC().deYoungHarper();
//			
//			// CG coordinates
//
//			double xH = (aChTail * theHorizontalTail.get_meanAerodChordActual().getEstimatedValue() + 
//					theHorizontalTail.get_xLEMacActualBRF().getEstimatedValue()) - xBRFcg; 
//			//						System.out.println(" xle brf mac " + theWing.get_xLEMacActualBRF().getEstimatedValue() );
//			double zH = -(zBRFcg - theHorizontalTail.get_aerodynamicCenterZ().getEstimatedValue());
//			
//	
//			for (int i=0 ; i<nValue ; i++){
//				 alphaBody = Amount.valueOf(Math.toRadians(alphaBodyArray[i]), SI.RADIAN);
//				  double downwashAngle = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);
//				  
//				  downwash = Amount.valueOf(downwashAngle, NonSI.DEGREE_ANGLE);
//				  alphaTail = Amount.valueOf(Math.toRadians(alphaBodyArray[i]- downwashAngle
//						  +  theHorizontalTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()), SI.RADIAN);
//				
//				  double chordRatio = cf_c.get(0);
//				  Amount<Angle> deflection = Amount.valueOf(Math.toRadians(deltaFlap.get(0)[0]), SI.RADIAN);
//				 
//				  if (deflection.getEstimatedValue() == 0.0 ){
//					  cLhTail[i] = theCLhTailCalculator.nasaBlackwellCompleteCurve(alphaBody);
//				  }
//				  else{
//				  cLhTail[i] = theCLhTailCalculator.getCLHTailatAlphaBodyWithElevator(
//							chordRatio, alphaBody, deflection, downwash, 
//							deltaFlap, flapType,null,
//							eta_in_flap, eta_out_flap, null, null, 
//							cf_c, null, null, null
//							);}
//				  
//				  cDhTail[i] =  theCDhTailCalculator.integralFromCdAirfoil(
//						  alphaTail, MethodEnum.NASA_BLACKWELL, theLSManager);
//				
//				  cDhTail[i] = cDhTail[i] + theHighLiftCalc.getDeltaCD();
//					  
//				  cNhTail[i] =  cLhTail[i] * Math.cos(alphaTail.getEstimatedValue()) +
//						  cDhTail[i] * Math.sin(alphaTail.getEstimatedValue());
//				  
//				  cChTail[i] = cDhTail[i] * Math.cos(alphaTail.getEstimatedValue()) -
//						  cLhTail[i] * Math.sin(alphaTail.getEstimatedValue());
//				  
//				  
//				  cMAChTail[i] = theCMACCalculator.calculateCMIntegral(alphaTail, aChTail);
//				  cMAChTail[i] = cMAChTail[i] + theHighLiftCalc.getDeltaCM_c4();
//				  
////				  cMvsAlphaWingArray [i] = cNIsolatedWing[i] * (xW/theWing.get_meanAerodChordActual().getEstimatedValue())
////						  + cCIsolatedWing[i]*(zW/theWing.get_meanAerodChordActual().getEstimatedValue())
////						  + cMACIsolatedWing[i];
//				  
////				  cMvsAlphaHTailArray [i] = -cNhTail[i] * theHorizontalTail.get_volumetricRatio() * 
////						  theHorizontalTail.getAerodynamics().get_dynamicPressureRatio()+
////						  cChTail[i]* theHorizontalTail.get_volumetricRatio() * 
////						  theHorizontalTail.getAerodynamics().get_dynamicPressureRatio() +
////						  cMAChTail[i] * theHorizontalTail.getAerodynamics().get_dynamicPressureRatio() * 
////						  (theHorizontalTail.get_surface().getEstimatedValue()/theAircraft.get_wing().get_surface().getEstimatedValue()) *
////						  (theHorizontalTail.get_meanAerodChordActual().getEstimatedValue()/theAircraft.get_wing().get_meanAerodChordActual().getEstimatedValue())
////						  ;
//				  
////				  System.out.println(" xh " + xH);
//				  
//				  cMvsAlphaHTailArray [i] = -cNhTail[i] * (theHorizontalTail.get_surface().getEstimatedValue()/
//						  theAircraft.get_wing().get_surface().getEstimatedValue())								
//						  * ( xH /theAircraft.get_wing().get_meanAerodChordActual().getEstimatedValue())*
//						  theHorizontalTail.getAerodynamics().get_dynamicPressureRatio()+
//						  cChTail[i] * (theHorizontalTail.get_surface().getEstimatedValue()/
//								  theAircraft.get_wing().get_surface().getEstimatedValue())								
//								  * ( xH /theAircraft.get_wing().get_meanAerodChordActual().getEstimatedValue())*
//						  theHorizontalTail.getAerodynamics().get_dynamicPressureRatio() +
//						  cMAChTail[i] * theHorizontalTail.getAerodynamics().get_dynamicPressureRatio() * 
//						  (theHorizontalTail.get_surface().getEstimatedValue()/theAircraft.get_wing().get_surface().getEstimatedValue()) *
//						  (theHorizontalTail.get_meanAerodChordActual().getEstimatedValue()/theAircraft.get_wing().get_meanAerodChordActual().getEstimatedValue())
//						  ;
//				  
//			
//		}
//		}
//			if (component == ComponentEnum.WINGBODY){
//				
//				double aCWing, acWingBody;
//				
//
//				
//				LiftingSurfaceCreator theWing = theAircraft.get_wing();
//				LSAerodynamicsManager theLSManager = theAircraft.get_wing().getAerodynamics();
//				LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = 
//						theLSManager
//						.new CalcCLAtAlpha();
//				LSAerodynamicsManager.CalcCDAtAlpha theCDWingCalculator = theLSManager
//						.new CalcCDAtAlpha();
//				
//				CalcPitchingMomentAC theCMACCalculator = new CalcPitchingMomentAC(theWing, theConditions);
//				
//				aCWing = theCMACCalculator.getACLiftingSurface() - deltaACWingBody;
//
//				// CG coordinates
//				
//				double xW = xBRFcg - (aCWing * theWing.get_meanAerodChordActual().getEstimatedValue() + 
//						theWing.get_xLEMacActualBRF().getEstimatedValue());
////				System.out.println(" xle brf mac " + theWing.get_xLEMacActualBRF().getEstimatedValue() );
//				double zW = -(zBRFcg - theWing.get_aerodynamicCenterZ().getEstimatedValue());
//				
////				System.out.println(" xw " + xW);
////				System.out.println(" zW " + zW);
////				
////				System.out.println(" xc " + xW/theWing.get_meanAerodChordActual().getEstimatedValue());
////				System.out.println(" zc " + zW/theWing.get_meanAerodChordActual().getEstimatedValue());
//				LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator = theLSManager.new MeanAirfoil();
//				MyAirfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theWing);
//				
//				for (int i=0 ; i<nValue ; i++){
//				  alphaWing = Amount.valueOf(
//						  Math.toRadians(alphaBodyArray[i]+ theWing.get_iw().to(NonSI.DEGREE_ANGLE)
//								  .getEstimatedValue()),
//						  SI.RADIAN);
//				  alphaBody = Amount.valueOf(Math.toRadians(alphaBodyArray[i]), SI.RADIAN);
//				  
//				  
//				  cLWingBody[i] = theAircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody, meanAirfoil, false);
//				  
//				  cDWingBody[i] =  theCDWingCalculator.integralFromCdAirfoil(
//							alphaWing, MethodEnum.NASA_BLACKWELL, theLSManager);
//				
//					  
//				  cNWingBody[i] =  cLWingBody[i] * Math.cos(alphaWing.getEstimatedValue()) +
//						  cDWingBody[i] * Math.sin(alphaWing.getEstimatedValue());
//				  
//				  cCWingBody[i] = cDWingBody[i] * Math.cos(alphaWing.getEstimatedValue()) -
//						  cLWingBody[i] * Math.sin(alphaWing.getEstimatedValue());
//				  
//				  
//				  cMACWingBody[i] = theCMACCalculator.calculateCMIntegral(alphaWing, aCWing) +  cm0Fuselage;
//				  
//				  
//				  cMvsAlphaWingArray [i] = cNIsolatedWing[i] * (xW/theWing.get_meanAerodChordActual().getEstimatedValue())
//						  + cCIsolatedWing[i]*(zW/theWing.get_meanAerodChordActual().getEstimatedValue())
//						  + cMACIsolatedWing[i];
////				  
////				  cMvsAlphaWingBodyArray [i] = cNWingBody[i] * 0.05
////						  + cCWingBody[i]*0.1
////						  + cMACWingBody[i];
////				  
//				}
//			}
//
//	
//	}
//	
//
//	public void calculateCMvsAlphaAircraft(){
//		
//		double [] cLTotal = new double[nValue];
//		double [] cDTotal = new double[nValue];
//		double [] thrustMoment = new double[nValue];
//		
//		calculateCMvsAlphaComponent(ComponentEnum.WINGBODY);
//		calculateCMvsAlphaComponent(ComponentEnum.WING);
//		calculateCMvsAlphaComponent(ComponentEnum.HORIZONTAL_TAIL);
//		CalcPowerPlantPitchingMoment thePowerMomentCalculator = new  CalcPowerPlantPitchingMoment();
//		
//		double hTailSurface = theAircraft.get_HTail().get_surface().getEstimatedValue();
//		double wingSurface = theAircraft.get_wing().get_surface().getEstimatedValue();
//		
//		for (int i=0 ; i<nValue; i++){
//		
//		cLTotal[i] = cLWingBody[i] + cLhTail[i] * (hTailSurface/wingSurface) *
//				theAircraft.get_HTail().getAerodynamics().get_dynamicPressureRatio();
//		
//		cDTotal[i] = cDWingBody[i] + cDhTail[i] * (hTailSurface/wingSurface) *
//				theAircraft.get_HTail().getAerodynamics().get_dynamicPressureRatio();
//		
//		thrustMoment [i] = thePowerMomentCalculator.calcPitchingMomentThrust(
//				theAircraft, theConditions, cLTotal[i], cDTotal[i]);
//		
//		 cMvsAlphaThrustArray[i] = thrustMoment[i]/(theConditions.get_dynamicPressure().getEstimatedValue()
//				 * theAircraft.get_wing().get_surface().getEstimatedValue() * 
//				 theAircraft.get_wing().get_meanAerodChordActual().getEstimatedValue());
//		 
//		 cMvsAlphaCompleteArray[i] = cMvsAlphaWingBodyArray [i] + cMvsAlphaHTailArray[i] + cMvsAlphaThrustArray[i];
//
//		}
//		
//		
//	}
//	
//	public void getCMvsAlphaComponent(Amount<Angle> alphaBody, ComponentEnum component){}
//	
//	public void getCMvsAlphaAircraft(Amount<Angle> alphaBody, ComponentEnum component){}
//	
//	public void plotCMvsAlphaComponent(String subfolderPath, ComponentEnum component){
//		
//		if (component == ComponentEnum.WING){
//			
//			MyChartToFileUtils.plotNoLegend(
//					alphaBodyArray , cMvsAlphaWingArray,
//					null, null, null, null,
//					"alpha_Body", "CM_CG",
//					"deg", "",
//					subfolderPath," Moment Coefficient vs alpha for Wing at CG aircraft" );
//		}
//		if (component == ComponentEnum.HORIZONTAL_TAIL){
//
//			MyChartToFileUtils.plotNoLegend(
//					alphaBodyArray , cMvsAlphaHTailArray,
//					null, null, null, null,
//					"alpha_Body", "CM_CG",
//					"deg", "",
//					subfolderPath," Moment Coefficient vs alpha for Horizontal Tail at CG aircraft" );
//		}
//
//		}
//		
//		
//	
//	
//	public void plotCMvsAlphaAircraft(String subfolderPath){
//		
////		System.out.println(" h tail " + Arrays.toString(cMvsAlphaHTailArray));
////		System.out.println(" wing " + Arrays.toString(cMvsAlphaWingArray));
////		System.out.println(" wing body " + Arrays.toString(cMvsAlphaWingBodyArray));
////		System.out.println(" thrust " + Arrays.toString(cMvsAlphaThrustArray));
//		MyChartToFileUtils.plotNoLegend(
//				alphaBodyArray , cMvsAlphaCompleteArray,
//				null, null, null, null,
//				"alpha_Body", "CM_CG",
//				"deg", "",
//				subfolderPath," Moment Coefficient (CG) vs alpha for aircraft" );
//		
//	}
//	
//
//
//	public double[] getAlphaBodyArray() {
//		return alphaBodyArray;
//	}
//	
//	public void setAlphaBodyArray(double[] alphaBodyArray) {
//		this.alphaBodyArray = alphaBodyArray;
//	}
//
//	public double[] getcMvsAlphaWingArray() {
//		return cMvsAlphaWingArray;
//	}
//
//	public double[] getcMvsAlphaWingBodyArray() {
//		return cMvsAlphaWingBodyArray;
//	}
//
//	public double[] getcMvsAlphaHTailArray() {
//		return cMvsAlphaHTailArray;
//	}
//
//	public double[] getcMvsAlphaThrustArray() {
//		return cMvsAlphaThrustArray;
//	}
//	
//}
//
//
//
//
///**
// * This class manages the calculation of the Pitching moment coefficient due to the power plant.
// * The method that calculates the pitching moment slope respect CL recognize the aircraft engine type
// *  
// * 
// *
// * @author  Manuela Ruocco
// */
//
////TOD0 --> set nacelle distance (z,x)
//
//public class CalcPowerPlantPitchingMoment{
//	
//	double pitchingMomentDerThrust;
//	double dCmnddCL = 0;
//	
//	public double calcPitchingMomentDerThrust (Aircraft aircraft, 
//			OperatingConditions conditions, 
//			double etaEfficiency, double liftCoefficient){
//		
//		Amount<Power> power = aircraft.get_powerPlant().get_engineList().get(0).get_p0();
//		double engineNumber = aircraft.get_powerPlant().get_engineNumber();
//		double powerDouble = engineNumber * power.getEstimatedValue();
//		double density = conditions.get_densityCurrent().getEstimatedValue();
//
//		double surface = aircraft.get_wing().get_surface().getEstimatedValue();
//		double wingLoad = (liftCoefficient*0.5 * density*Math.pow(
//				conditions.get_tas().getEstimatedValue(), 2)/9.81);
//
//		double meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordEq().getEstimatedValue();
////		System.out.println(" lift coefficient " + liftCoefficient);
//		double kFactor = (3 * powerDouble) /( 2 * Math.sqrt((2/density))) * Math.pow( wingLoad , (3/2));
//		kFactor = 0.2;
//		double thrustArm = aircraft.get_powerPlant().get_engineList().get(0).get_Z0().getEstimatedValue();
//		pitchingMomentDerThrust = kFactor* Math.sqrt(liftCoefficient) * (thrustArm/meanAerodynamicChord) / surface;
//		
//		return pitchingMomentDerThrust;
//	}
//	
//	public double calcPitchingMomentThrust (Aircraft aircraft, 
//			OperatingConditions conditions, 
//			double cLTotal, double cDTotal){
//		
//		double weight = (aircraft.get_weights().get_MTOW().getEstimatedValue()
//				- aircraft.get_weights().get_MZFW().getEstimatedValue())/2;
//		double dynamicPressureatCL  =  weight/(aircraft.get_wing().get_surface().getEstimatedValue() * cLTotal);
//		double thrustTotal = dynamicPressureatCL * aircraft.get_wing().get_surface().getEstimatedValue()* cDTotal;
//		double distance = -aircraft
//				.get_theNacelles()
//				.get_nacellesList()
//				.get(0).get_cg().get_zBRF().getEstimatedValue();
//		double pitchingMoment = thrustTotal * distance; 
//		double pitchingMomentCoefficient = pitchingMoment/(
//				conditions.get_dynamicPressure().getEstimatedValue()
//				* aircraft.get_wing().get_surface().getEstimatedValue() * 
//				aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue()); // t/qSc
//		return pitchingMomentCoefficient;
//		
//		
//	}
//	
//	
//	public double calcPitchingMomentDerNonAxial (Aircraft aircraft, 
//			double nBlades, double diameter,
//			double clAlpha){
//		
//		if( aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP){
//		double dCndAlpha=0.0;
//		
//		if(nBlades == 2)
//			dCndAlpha = 0.0024; // 1/deg
//		
//		if(nBlades == 4)
//			dCndAlpha = 0.0040; // 1/deg
//		
//		if(nBlades == 6)
//			dCndAlpha = 0.0065; // 1/deg
//		
//		double surfaceP = Math.PI * Math.pow(diameter/2, 2);
//		double surface = aircraft.get_wing().get_surface().getEstimatedValue();
//		double surfaceRatio = surfaceP/surface;
////		System.out.println(" surface ratio " + surfaceRatio);
//		double numProp = aircraft.get_powerPlant().get_engineNumber();
//	
//		double distance = Math.abs(aircraft
//				.get_theNacelles()
//				.get_nacellesList()
//				.get(0).get_cg().get_xBRF().getEstimatedValue()-
//				aircraft.get_theBalance().get_xCoGMeanAtOEM().doubleValue());
////		System.out.println(" nac " +aircraft
////				.get_theNacelles().get_cgList().get(0).get_xBRF().getEstimatedValue());
////		System.out.println(" x cg "+aircraft.get_theBalance().get_xCoGMeanAtOEM().doubleValue());
////		System.out.println(" xcgg " + distance);
//		// set this
//		double meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue();
//		//double depsdalpha = 1.5;
//		double position = -distance/aircraft.get_wing().getChordAtYActual(0.0);
//		// System.out.println(" position " + position);
//		double depsdalpha = aircraft
//				.get_theAerodynamics()
//				.get_aerodynamicDatabaseReader()
//				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
//						position,
//						aircraft.get_wing().get_aspectRatio()
//						);
//		dCmnddCL = dCndAlpha * surfaceRatio * 
//				(distance / meanAerodynamicChord) * (depsdalpha/clAlpha) * numProp;
//		}
//		
//		
//		return dCmnddCL;
//		
//	}
//
//	public double getPitchingMomentDerThrust() {
//		return pitchingMomentDerThrust;
//	}
//
//	public void setPitchingMomentDerThrust(double pitchingMomentDerThrust) {
//		this.pitchingMomentDerThrust = pitchingMomentDerThrust;
//	}
//}
//}
