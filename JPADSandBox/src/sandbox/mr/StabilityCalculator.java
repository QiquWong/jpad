package sandbox.mr;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.componentmodel.InnerCalculator;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import database.databasefunctions.DatabaseReader;
import aircraft.components.liftingSurface.LiftingSurface;
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
	public double claculateCLCompleteAircraft (Aircraft aircraft,
			Amount<Angle> alphaBody,
			MyAirfoil meanAirfoil,
			Amount<Angle> deflection,
			double chordRatio,
			double etaRatio
			)
	{
		
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
		
		double cLHTail = theCLHorizontalTailCalculator.getCLHTailatAlphaBodyWithElevator(chordRatio, alphaBody, deflection, downwashAmount);
		
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
	 * This method calculates the Pitching moment coefficient of a wing respect to a quarter of MAC
	 * starting from the lifting characteristics and moment ones of the airfoils.
	 * 
	 * @param aircraft
	 * @param angle of attack between the flow direction and the fuselage reference line
	 * @param angle of deflection of the elevator in deg or radians
	 * @param MyAirfoil the mean airfoil of the wing
	 * @param chord ratio of elevator
	 * @param eta the pressure ratio. For T tail is 1.
	 * 
	 *
	 * @author  Manuela Ruocco
	 */

public class CalcPitchingMoment{
	
	// VARIABLE DECLARATION--------------------------------------
	
	LiftingSurface theLiftingSurface;
	
	double meanAerodinamicChord, xMAC, yMAC;
	double [] xLEActualArray, yArray, cMACAirfoils, pitchingMomentAirfoilsDueToLift, 
	liftForceAirfoils, cMAirfoilsDueToLift, armMomentAirfoils, pitchingMomentLiftingSurface, cMLiftingSurfaceArray,
	yStationsNB, cLDistributionNB, chordLocal;
	Double[] cLDistribution;
	int nPointSemiSpan;
	List<MyAirfoil> airfoilList = new ArrayList<MyAirfoil>();;
	LSAerodynamicsManager theLSManager; 
	
	// BUILDER--------------------------------------
	
	public CalcPitchingMoment(LiftingSurface theLiftingSurface) {
		
		this.theLiftingSurface = theLiftingSurface;
	
		theLSManager = theLiftingSurface.getAerodynamics();
		
		meanAerodinamicChord = theLiftingSurface.get_meanAerodChordActual().getEstimatedValue();
		xMAC = theLiftingSurface.get_xLEMacActualLRF().getEstimatedValue();
		nPointSemiSpan = theLSManager.get_nPointsSemispanWise();

		// initializing array
		xLEActualArray = new double [nPointSemiSpan];
		yArray = new double [nPointSemiSpan];
		yArray = theLSManager.get_yStations();
		cMACAirfoils = new double [nPointSemiSpan];
		cLDistribution = new Double [nPointSemiSpan];
		pitchingMomentAirfoilsDueToLift = new double [nPointSemiSpan];
		cMAirfoilsDueToLift = new double [nPointSemiSpan];
		liftForceAirfoils = new double [nPointSemiSpan];
		armMomentAirfoils = new double [nPointSemiSpan];
		pitchingMomentLiftingSurface = new double [nPointSemiSpan];
		cMLiftingSurfaceArray = new double [nPointSemiSpan];	
				
		
		for (int i=0; i<nPointSemiSpan ; i++){
			System.out.println("i " + i);
			xLEActualArray[i] = theLiftingSurface.getXLEAtYActual(yArray[i]);
			airfoilList.add(i,theLSManager.calculateIntermediateAirfoil(
					theLiftingSurface, yArray[i]) );
			chordLocal[i] = theLiftingSurface.getChordAtYActual(yArray[i]);
			cMACAirfoils[i] = airfoilList.get(i).getAerodynamics().get_cmAC();
			 armMomentAirfoils[i] = (xMAC - (
					 xLEActualArray[i]+airfoilList
					 .get(i).getAerodynamics()
					 .get_aerodynamicCenterX()) 
					 ) + (0.25 * meanAerodinamicChord);
		}
					
	}
	
	
	//METHODS--------------------------------------
	public double calculateCMQuarterMACIntegral (Amount<Angle> alphaLocal, OperatingConditions theCondition){
		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
			alphaLocal = alphaLocal.to(SI.RADIAN);
		
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();

		cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);

		double dynamicPressure = theCondition.get_dynamicPressure().getEstimatedValue();
		
		for (int i=0 ; i<nPointSemiSpan ;  i++){
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
			pitchingMomentAirfoilsDueToLift [i] = liftForceAirfoils[i] * armMomentAirfoils[i];
			cMAirfoilsDueToLift[i] = pitchingMomentAirfoilsDueToLift [i]/
					(dynamicPressure * Math.pow(chordLocal[i], 2));
			cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];
		}
		
		
		double pitchingMomentCoefficient = MyMathUtils.integrate1DSimpsonSpline(yArray, cMLiftingSurfaceArray);
		
		return pitchingMomentCoefficient;
	}
	
	
	public void plotCMatAlpha(Amount<Angle> alphaLocal, OperatingConditions theCondition, String subfolderPath){
		calculateCMQuarterMACIntegral(alphaLocal, theCondition);
		
		MyChartToFileUtils.plotNoLegend(
				yArray, cMACAirfoils,
				null, null, null, null,
				"station", "CM",
				"m", "", 
				subfolderPath," Moment Coefficient distribution for " + theLiftingSurface.get_type() );	
				
			
	
	}
	
	
	public double[] getcMLiftingSurfaceArray() {
		return cMLiftingSurfaceArray;
	}


	public double[] getyArray() {
		return yArray;
	}

}

}
