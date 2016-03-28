package sandbox.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;

public class StabilityCalculator {

	public double calculateTauIndex(double chordRatio,
			Aircraft aircraft,
			Amount<Angle> deflection
			)
	{
		double deflectionAngleDeg;

		if (deflection.getUnit() == SI.RADIAN){
			deflection = deflection.to(NonSI.DEGREE_ANGLE);
		}
		if(deflection.getEstimatedValue()<0){
			deflectionAngleDeg = -deflection.getEstimatedValue();}

		else{
			deflectionAngleDeg = deflection.getEstimatedValue();}


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


	public class CalcCLHTail{

		// VARIABLE DECLARATION--------------------------------------

		LiftingSurface theLiftingSurface;
		OperatingConditions theConditions;

		double [] alphaArrayWithTau, clHTailDeflected;
		Double [] cLHtailCleanExtended;


		public double[] cLHtailWithElevatorDeflection(LiftingSurface hTail,
				OperatingConditions theOperatingCondition, 
				double deltaE, double tauValue, double[] cLCleanArray, double[] alphaTailArray){

			LSAerodynamicsManager.MeanAirfoil theMeanAirfoil = hTail.getAerodynamics().new MeanAirfoil();
			MyAirfoil meanAirfoil = theMeanAirfoil.calculateMeanAirfoil(hTail);

			int nPoints = 60;


			List<Double[]> deltaFlap = new ArrayList<Double[]>();
			List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
			List<Double> etaInFlap = new ArrayList<Double>();
			List<Double> etaOutFlap = new ArrayList<Double>();
			List<Double> cfc = new ArrayList<Double>();

			Double[] deltaFlapDouble =  new Double [1];

			if(deltaE<0){
				deltaFlapDouble[0] = -deltaE;}
			else
				deltaFlapDouble[0] = deltaE;

			deltaFlap.add(deltaFlapDouble);
			flapType.add(FlapTypeEnum.PLAIN);
			etaInFlap.add(hTail.get_etaIn());
			etaOutFlap.add(hTail.get_etaOut());
			cfc.add(hTail.get_CeCt());

			LSAerodynamicsManager.CalcHighLiftDevices theHighLiftCalculator = hTail.getAerodynamics().new
					CalcHighLiftDevices(hTail, theOperatingCondition,
							deltaFlap, flapType, null,
							etaInFlap, etaOutFlap, null,
							null, cfc, null, null, 
							null);

			theHighLiftCalculator.calculateHighLiftDevicesEffects();

			// cl alpha clean

			double clAlphaClean = ((cLCleanArray[2] - cLCleanArray[1])/(alphaTailArray[2]-alphaTailArray[1]));

			
			// alphaZeroLift clean
			
			double alphaZeroLiftWingClean = hTail.getAerodynamics().getAlphaZeroLiftWingClean();
			

			// alpha zero lift 
			double alphaZeroLift = alphaZeroLiftWingClean -(tauValue * deltaE);


			// cl alpha new 

			double clAlphaDeltaE = theHighLiftCalculator.getcLalpha_new();

			// q value

			double qValue = - clAlphaDeltaE*alphaZeroLift;


			// alpha Star


			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
			double clStarClean = clAlphaClean  * alphaStarClean;
			double alphaStarNew = (clStarClean - qValue)/clAlphaDeltaE;

			double alphaStarElevator = alphaStarClean + (-tauValue*deltaE);
			double cLstarElevator = clAlphaDeltaE * alphaStarElevator + qValue;


			// alpha max and cl max

			double alphaMaxClean = hTail.getAerodynamics().get_alphaMaxClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
			double cLMaxClean =hTail.getAerodynamics().get_cLMaxClean();




			double deltaAlphaMax;

		
			deltaAlphaMax = theHighLiftCalculator.getDeltaAlphaMaxFlap();

			
			double deltaCLMax;

			if (deltaE<0)
				deltaCLMax = -theHighLiftCalculator.getDeltaCLmax_flap();

			else
				deltaCLMax = theHighLiftCalculator.getDeltaCLmax_flap();

			double cLMaxElevator = cLMaxClean + deltaCLMax;
			double alphaStallElevator = ((cLMaxElevator - qValue)/clAlphaDeltaE)+ deltaAlphaMax;


			alphaArrayWithTau = MyArrayUtils.linspace(alphaZeroLift-10 , alphaStallElevator+1.9 , nPoints);
			clHTailDeflected = new double [alphaArrayWithTau.length];

			
			// curve 

			double alpha;

			double[][] matrixData = {
					{Math.pow(alphaStarElevator, 4), Math.pow(alphaStarElevator, 3),Math.pow(alphaStarElevator, 2), alphaStarElevator,1.0},
					{4* Math.pow(alphaStarElevator, 3), 3*Math.pow(alphaStarElevator, 2), 2*alphaStarElevator,1.0, 0.0},
					{12 *Math.pow(alphaStarElevator, 2), 6* alphaStarElevator, 2.0, 0.0, 0.0},
					{Math.pow(alphaStallElevator, 4), Math.pow(alphaStallElevator, 3),Math.pow(alphaStallElevator, 2), alphaStallElevator,1.0},
					{4* Math.pow(alphaStallElevator, 3), 3*Math.pow(alphaStallElevator, 2), 2*alphaStallElevator,1.0, 0.0}
			};
				
				

			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


			double [] vector = {cLstarElevator,clAlphaDeltaE, 0,cLMaxElevator,0};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];
			double e = solSystem[4];

			for (int i=0; i<alphaArrayWithTau.length ; i++){

				alpha = alphaArrayWithTau[i];
				if (alpha < alphaStarElevator){
					clHTailDeflected[i] = clAlphaDeltaE * alpha + qValue;
				}

				else{
					clHTailDeflected[i] = a *Math.pow(alpha, 4)+ b * Math.pow(alpha, 3) + 
							c * Math.pow(alpha, 2) + 
							d * alpha + e;

				}
			}
			
			
					

						
			return clHTailDeflected;
		}


		public double[] getAlphaArrayWithTau() {
			return alphaArrayWithTau;
		}

	}
	
	
	/**
	 * This method calculates the Pitching moment coefficient of a lifting surface
	 *  respect a given point of MAC.
	 *
	 *
	 *
	 * @author  Manuela Ruocco
	 */

public class CalcPitchingMomentAC{

	// VARIABLE DECLARATION--------------------------------------

	LiftingSurface theLiftingSurface;
	OperatingConditions theConditions;

	double meanAerodinamicChord, xMAC, yMAC, cLLocal, qValue, alphaLocalAirfoil;
	double [] xLEActualArray, yArray, cMACAirfoils, pitchingMomentAirfoilsDueToLift,
	liftForceAirfoils, cMAirfoilsDueToLift, armMomentAirfoils, pitchingMomentLiftingSurface, cMLiftingSurfaceArray,
	yStationsNB, cLDistributionNB, chordLocal, xcPArrayLRF, xACArrayLRF, clNasaBlackwell;
	Double[] cLDistribution;
	int nPointSemiSpan;
	List<MyAirfoil> airfoilList = new ArrayList<MyAirfoil>();
	LSAerodynamicsManager theLSManager;

	// BUILDER--------------------------------------

	@SuppressWarnings("static-access")
	public CalcPitchingMomentAC(LiftingSurface theLiftingSurface, OperatingConditions theConditions) {

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
//			System.out.println(chordLocal[i] );
		}

	

	}


	//METHODS--------------------------------------

	public double calculateCMIntegral (Amount<Angle> alphaLocal, double xPercentMAC){
		if (alphaLocal.getUnit() == NonSI.DEGREE_ANGLE)
			alphaLocal = alphaLocal.to(SI.RADIAN);
//		System.out.println("\nalpha " + alphaLocal.to(NonSI.DEGREE_ANGLE));
//
//		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
//		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		//cLDistributionNB = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		yStationsNB = calculateLiftDistribution.getNasaBlackwell().getyStations();

		//cLDistribution = MyMathUtils.getInterpolatedValue1DLinear(yStationsNB, cLDistributionNB, yArray);

		double dynamicPressure = theConditions.get_dynamicPressure().getEstimatedValue();
		
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();
		calculateLiftDistribution.getNasaBlackwell().calculate(alphaLocal);
		clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
//		System.out.println(" x mac " + xMAC);
//		System.out.println("\n\n alpha input " + alphaLocal.getEstimatedValue()*57.3);
		for (int i=0 ; i<nPointSemiSpan ;  i++){
			cLLocal = clNasaBlackwell[i];
//			System.out.println( " cl local " + cLLocal);
			qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(0.0);
//			System.out.println(" qValue " + qValue );
			alphaLocalAirfoil = (cLLocal-qValue)/airfoilList.get(i).getAerodynamics().get_clAlpha();
//			System.out.println(" alpha local airfoil " + alphaLocalAirfoil);
			cLDistribution[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
					 //alphaLocal.getEstimatedValue()+
					alphaLocalAirfoil+
					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
//			System.out.println(" cl local " + cLDistribution[i]);
//			System.out.println(" cl distribution " + Arrays.toString(cLDistribution));
			if(theLiftingSurface.get_type().equals(ComponentEnum.HORIZONTAL_TAIL)){
				liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure *
						theLiftingSurface.getAerodynamics().get_dynamicPressureRatio()*
						theLiftingSurface.get_meanGeometricChord().getEstimatedValue();
			}
			else
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * chordLocal[i];
//			System.out.println(liftForceAirfoils[i]);
//			if((cLDistribution[i]) > 0 ){
			xcPArrayLRF [i] = chordLocal[i] *(
					airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() -
					(cMACAirfoils[i]/cLDistribution[i]));
//			System.out.println(xLEActualArray[i]+ xcPArrayLRF[i]);
//			}
//			else{xcPArrayLRF [i] = chordLocal[i] *(
//					-airfoilList.get(i).getAerodynamics().get_aerodynamicCenterX() +
//					(cMACAirfoils[i]/cLDistribution[i]));
	
//			}
//			System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
			armMomentAirfoils[i] = (xMAC + (xPercentMAC * meanAerodinamicChord))-
					 (xLEActualArray[i]+ xcPArrayLRF[i]);
			if ( i ==nPointSemiSpan-1){
				armMomentAirfoils[i] = 0;
			}
//			System.out.println(  armMomentAirfoils[i]);
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
			
			if(theLiftingSurface.get_type().equals(ComponentEnum.HORIZONTAL_TAIL)){
			cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
					(dynamicPressure * 
							theLiftingSurface.getAerodynamics().get_dynamicPressureRatio()*
							Math.pow(theLiftingSurface.get_meanGeometricChord().getEstimatedValue(), 2));}
			else{
				cMLiftingSurfaceArray[i] = pitchingMomentAirfoilsDueToLift [i]/
				(dynamicPressure * Math.pow(theLiftingSurface.get_meanGeometricChord().getEstimatedValue(), 2));}
				
			cMLiftingSurfaceArray[cMLiftingSurfaceArray.length-1] = 0;
			//cMLiftingSurfaceArray[i] = cMACAirfoils[i] +  cMAirfoilsDueToLift[i];

		}
		//cMACAirfoils[cMACAirfoils.length-1] = 0;
//		System.out.println(" cm airfoils " + Arrays.toString(cMACAirfoils));
//		System.out.println(" cl " + Arrays.toString(cLDistribution));
//		System.out.println(" x cp " +   Arrays.toString(xcPArrayLRF));
//		System.out.println(" lift " + Arrays.toString(liftForceAirfoils));
//		System.out.println(" cm due to lift " + Arrays.toString(cMAirfoilsDueToLift));
	
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
			liftForceAirfoils [i] = cLDistribution[i] * dynamicPressure * theLiftingSurface.get_meanGeometricChord().getEstimatedValue();
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
					(dynamicPressure * Math.pow(theLiftingSurface.get_meanGeometricChord().getEstimatedValue(), 2));
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
	
		alphaFirst = Amount.valueOf(1.0, NonSI.DEGREE_ANGLE);
		alphaSecond= Amount.valueOf(4.0, NonSI.DEGREE_ANGLE);
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
		while ( Math.abs(cMDiff) > 0.000003){
			   if ((cMTempAlphaFirst > 0 & cMTempAlphaSecond <0) || ( (cMTempAlphaSecond - cMTempAlphaFirst) < 0)){

				percent = percent + 0.0001;	
				System.out.println(" percent " + percent);
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


}

