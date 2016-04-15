package sandbox.mr.StabilityTest;
//package sandbox.mr;
//
//import static java.lang.Math.toRadians;
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
//import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
//
//import aircraft.OperatingConditions;
//import aircraft.auxiliary.airfoil.MyAirfoil;
//import aircraft.componentmodel.InnerCalculator;
//import aircraft.components.Aircraft;
//import aircraft.components.liftingSurface.LSAerodynamicsManager;
//import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
//import database.databasefunctions.DatabaseReader;
//import aircraft.components.liftingSurface.LiftingSurface;
//import standaloneutils.MyChartToFileUtils;
//import standaloneutils.MyMathUtils;
//import standaloneutils.customdata.MyArray;
//
//public class StabilityCalculatorInduced {
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
//			double chordRatio
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
////		double cLHTail = theCLHorizontalTailCalculator.getCLHTailatAlphaBodyWithElevator(
////				chordRatio, alphaBody, deflection, downwashAmount,
////				);
//		
////		System.out.println("the CL of horizontal Tail at alpha body =(deg)" + 
////				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
////				" for delta = (deg) "
////				+ deflection.getEstimatedValue() 
////				+ " is " + cLHTail);
//		
//		double hTailSurface = aircraft.get_HTail().get_surface().getEstimatedValue();
//		double wingSurface = aircraft.get_wing().get_surface().getEstimatedValue();
//		
////		double cLTotal = cLWingBody + cLHTail * (hTailSurface / wingSurface ) * etaRatio;
////		return cLTotal;
//		return 0.0;
//	}
//	
//	
//	/**
//	 * This class manages the calculation of the Pitching moment coefficient of a lifting surface respect to a quarter of MAC
//	 * starting from the lifting characteristics and moment ones of the airfoils.
//	 * 
//	 * @param aircraft
//	 * @param angle of attack between the flow direction and the fuselage reference line
//	 * @param angle of deflection of the elevator in deg or radians
//	 * @param MyAirfoil the mean airfoil of the wing
//	 * @param chord ratio of elevator
//	 * @param eta the pressure ratio. For T tail is 1.
//	 * 
//	 *
//	 * @author  Manuela Ruocco
//	 */
//
//public class CalcLSPitchingMoment{
//	
//	// VARIABLE DECLARATION--------------------------------------
//	
//	LiftingSurface theLiftingSurface;
//	OperatingConditions theConditions;
//	AlphaEffective theAlphaCalculator;
//	
//	double meanAerodinamicChord, xMAC, yMAC;
//	double [] xLEActualArray, yArray, cMACAirfoils, pitchingMomentAirfoilsDueToLift, 
//	liftForceAirfoils, cMAirfoilsDueToLift, armMomentAirfoils, pitchingMomentLiftingSurface, cMLiftingSurfaceArray,
//	yStationsNB, cLDistributionNB, chordLocal, xcPArrayLRF, alphaEffective;
//	Double[] cLDistribution,  alphaEffectiveDistribution;
//	int nPointSemiSpan;
//	List<MyAirfoil> airfoilList = new ArrayList<MyAirfoil>();;
//	LSAerodynamicsManager theLSManager; 
//	
//	// BUILDER--------------------------------------
//	
//	public CalcLSPitchingMoment(LiftingSurface theLiftingSurface, OperatingConditions theConditions) {
//		
//		this.theLiftingSurface = theLiftingSurface;
//		this.theConditions = theConditions;
//		
//		theLSManager = theLiftingSurface.getAerodynamics();
//		theAlphaCalculator = new AlphaEffective(theLSManager, theLiftingSurface, theConditions);
//		meanAerodinamicChord = theLiftingSurface.get_meanAerodChordActual().getEstimatedValue();
//		xMAC = theLiftingSurface.get_xLEMacActualLRF().getEstimatedValue();
//		nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
//		
//		
//		// initializing array
//		xLEActualArray = new double [nPointSemiSpan];
//		alphaEffective = new double [nPointSemiSpan];
//		yArray = new double [nPointSemiSpan];
//		yArray = theLSManager.get_yStations();
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
//		alphaEffectiveDistribution = new Double [nPointSemiSpan];
//		
//		
//		for (int i=0; i<nPointSemiSpan ; i++){
//			xLEActualArray[i] = theLiftingSurface.getXLEAtYActual(yArray[i]);
//			airfoilList.add(i,LSAerodynamicsManager.calculateIntermediateAirfoil(
//					theLiftingSurface, yArray[i]) );
//			chordLocal[i] = theLiftingSurface.getChordAtYActual(yArray[i]);
//			cMACAirfoils[i] = airfoilList.get(i).getAerodynamics().get_cmAC();
//					 
//		}
//		
//		
//					
//	}
//	
//	
//	//METHODS--------------------------------------
//	public double calculateCMQuarterMACIntegral (Amount<Angle> alphaLocal){
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
//	
//		alphaEffective = theAlphaCalculator.calculateAlphaEffective(alphaLocal);
//		alphaEffectiveDistribution = MyMathUtils.getInterpolatedValue1DLinear(
//				theAlphaCalculator.getyStationsActual(), alphaEffective, yArray);
//		
//		for (int i=0 ; i<nPointSemiSpan ;  i++){
//			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
//					alphaEffectiveDistribution[i]+ 
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
//	}
//	
//	
//	public void plotCMatAlpha(Amount<Angle> alphaLocal, String subfolderPath){
//		calculateCMQuarterMACIntegral(alphaLocal);
//		
//		double[] yStationsND = theLSManager.get_yStationsND();
//		MyChartToFileUtils.plotNoLegend(
//				yStationsND ,cMLiftingSurfaceArray,
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
///**
// * This class manages the calculation of the Pitching moment coefficient due to the power plant.
// * The method that calculates the pitching moment slope respect CL recognize the aircraft engine type
// *  
// * 
// * @param aircraft
// * @param angle of attack between the flow direction and the fuselage reference line
// * @param angle of deflection of the elevator in deg or radians
// * @param MyAirfoil the mean airfoil of the wing
// * @param chord ratio of elevator
// * @param eta the pressure ratio. For T tail is 1. 
// * 
// *
// * @author  Manuela Ruocco
// */
//
//public class CalcPowerPlantPitchingMoment{
//	
//	double pitchingMomentDerThrust;
//
//	
//	// calcolo delle derivate per spinta e forza assiale per turboprop e turbofan
//	// plot
//	// 
//	
//	public double calcPitchingMomentDerThrust (Aircraft aircraft, 
//			OperatingConditions conditions, 
//			double etaEfficiency, double liftCoefficient){
//		
//		Amount<Power> power = aircraft.get_powerPlant().get_engineList().get(0).get_p0();
//		double engineNumber = aircraft.get_powerPlant().get_engineNumber();
//		double powerDouble = engineNumber * power.getEstimatedValue();
//		double density = conditions.get_densityCurrent().getEstimatedValue();
//		double surface = aircraft.get_wing().get_surface().getEstimatedValue();
//		double wingLoad = (liftCoefficient*(0.5 * density * Math.pow(
//				conditions.get_tas().getEstimatedValue(), 2))
//				*surface)/
//				surface;
//		
//		double meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordEq().getEstimatedValue();
//		
//		double kFactor = (3 * powerDouble) /( 2 * Math.sqrt((2/density))) * Math.pow( wingLoad , (3/2));
//		
//		double thrustArm = aircraft.get_powerPlant().get_engineList().get(0).get_Z0().getEstimatedValue();
//		pitchingMomentDerThrust = kFactor* etaEfficiency * Math.sqrt(liftCoefficient) * (thrustArm/meanAerodynamicChord) / surface;
//		
//		return pitchingMomentDerThrust;
//	}
//
//	public double getPitchingMomentDerThrust() {
//		return pitchingMomentDerThrust;
//	}
//
//	public void setPitchingMomentDerThrust(double pitchingMomentDerThrust) {
//		this.pitchingMomentDerThrust = pitchingMomentDerThrust;
//	}
//
//}
//}
//
