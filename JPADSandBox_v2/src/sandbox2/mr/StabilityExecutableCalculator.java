package sandbox2.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import analyses.ACPerformanceManager.ACPerformanceCalculatorBuilder;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.NasaBlackwell;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyVariableToWrite;

/************************************************************************************************************************
 * This class contains all the calculators for the stability	
 * 								*
 * @author Manuela Ruocco																								*
 ***********************************************************************************************************************/
public class StabilityExecutableCalculator {
	
	//FINAL variables (in alpha body local)
	List<Amount<Length>> horizontalDistance = new ArrayList<>();
	List<Amount<Length>> verticalDistance = new ArrayList<>();
	List<Amount<Angle>> downwashAngle = new ArrayList<>();
	List<Double> downwashGradient = new ArrayList<>();
	Amount<Angle> epsilonZero;
	
	//FINAL variables CLMAX
	double cLMaxFinal;
	Amount<Angle> alphaMaxLinear;
	double []  liftDistributionAtCLMax;
	
	//database
	String databaseFolderPath;
	String aerodynamicDatabaseFileName;
	String highLiftDatabaseFileName;
	AerodynamicDatabaseReader aeroDatabaseReader;
	HighLiftDatabaseReader highLiftDatabaseReader;
	double downwashGradientConstant;
	Amount<Length> verticalDistanceConstant;
	
	
	public void calculateDownwashNonLinearSlingerland(
			StabilityExecutableManager theStabilityManager,
			Amount<Length> horizontalDistanceInitial,
			Amount<Length> verticalDistanceInitial,
			double [] clAlphaArray,
			double []  alphasWing, // deg
			double [] alphaBody //deg
			){
		
		
		double zApexWing = theStabilityManager.getZApexWing().doubleValue(SI.METER);
		double zApexHTail = theStabilityManager.getZApexHTail().doubleValue(SI.METER);
		
		// angles
		double iw = theStabilityManager.getWingAngleOfIncidence().doubleValue(SI.RADIAN); //rad
		double alphaZeroLift = 0 ; //rad
		alphaZeroLift = theStabilityManager.getWingAlphaZeroLiftCONDITION().doubleValue(SI.RADIAN); 

		double startingAngle = iw - alphaZeroLift; // rad
		
		// Alpha Absolute array 
		
				double alphaFirst = 0.0;
				double alphaLast = 40.0;
				int nValue = 100;

				double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue); //deg
				double [] alphaWingArray =  new double [alphaAbsoluteArray.length]; //deg
				for(int i=0; i< alphaAbsoluteArray.length; i++){
					alphaWingArray[i] = alphaAbsoluteArray[i] + alphaZeroLift*57.3; 
				}
				double deltaAlpha = Amount.valueOf(
						Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue(); // rad

				Double[] cLAlphaArray = MyMathUtils.getInterpolatedValue1DLinear(
						alphasWing,
						clAlphaArray, 
						alphaWingArray);
		
				
		// calculate first values
				Amount<Length> zDistanceZero = null;
				Amount<Length> xDistanceZero = horizontalDistanceInitial; 
				
				if (zApexWing < zApexHTail  ){

					zDistanceZero = Amount.valueOf(
							verticalDistanceInitial.doubleValue(SI.METER) + (
									(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle))),
							SI.METER);
				}

				if (zApexWing > zApexHTail  ){

					zDistanceZero = Amount.valueOf(
							verticalDistanceInitial.doubleValue(SI.METER) - (
									(horizontalDistanceInitial.doubleValue(SI.METER) *
									Math.tan(startingAngle))),
							SI.METER);
				}

				zDistanceZero = Amount.valueOf(
						zDistanceZero.doubleValue(SI.METER) * 
						Math.cos(startingAngle), SI.METER);
				
		double epsilonTemp, zDistTemp, downwashRad, epsilonTempRad,  downwashGradientArrayTemp;
		double zTemp = 0;
		Amount<Angle> epsilonZero;
		
		// Initialize Array
		
		double [] downwashArray = new double [nValue];
		double [] downwashGradientArray = new double [nValue];
		double [] alphaBodyArray = new double [nValue];
		double [] zDistanceArray = new double [nValue];
		double [] xDistanceArray = new double [nValue];	
		
		// First step
		
		zDistanceArray[0] = zDistanceZero.doubleValue(SI.METER);
		xDistanceArray[0] = xDistanceZero.doubleValue(SI.METER);
		downwashGradientArray[0] = calculateDownwashGradientSlingerland( xDistanceArray[0],
				zDistanceArray[0], 
				cLAlphaArray[0], 
				theStabilityManager.getWingSweepQuarterChord(),
				theStabilityManager.getWingAspectRatio(),
				theStabilityManager.getWingSemiSpan()
				);

		epsilonZero = Amount.valueOf(downwashGradientArray[0]*(-alphaZeroLift*57.3), NonSI.DEGREE_ANGLE);
		
		downwashArray[0] = epsilonZero.doubleValue(NonSI.DEGREE_ANGLE) + downwashGradientArray[0]*alphaWingArray[0];
		alphaBodyArray[0] = alphaAbsoluteArray[0] - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);

		this.epsilonZero = epsilonZero;
		this.downwashGradientConstant = downwashGradientArray[0];
		this.verticalDistanceConstant = Amount.valueOf(zDistanceArray[0], SI.METER);
		
		// Other step
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
			
			epsilonTemp = epsilonZero.doubleValue(NonSI.DEGREE_ANGLE) + downwashGradientArray[i-1]*alphaWingArray[i];
			epsilonTempRad = Amount.valueOf(
					Math.toRadians(epsilonTemp), SI.RADIAN).getEstimatedValue();

			if (zApexWing < zApexHTail  ){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
								Math.tan(startingAngle- i * deltaAlpha + epsilonTempRad)));
			}

			if (zApexWing > zApexHTail  ){

				zTemp = 
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
								Math.tan(startingAngle- i * deltaAlpha + epsilonTempRad)));
			}

			zTemp = 
					zTemp * 
					Math.cos(startingAngle- i * deltaAlpha + epsilonTempRad);
	
		
			downwashGradientArrayTemp = calculateDownwashGradientSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zTemp, 
					cLAlphaArray[i], 
					theStabilityManager.getWingSweepQuarterChord(),
					theStabilityManager.getWingAspectRatio(),
					theStabilityManager.getWingSemiSpan()
					);
					
			downwashArray[i] = epsilonZero.doubleValue(NonSI.DEGREE_ANGLE) + downwashGradientArrayTemp*alphaWingArray[i];
				
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
	
					if (zApexWing < zApexHTail  ){

						zDistanceArray[i] =
								verticalDistanceInitial.doubleValue(SI.METER) + (
										(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle- i * deltaAlpha + downwashRad)));
					}

					if (zApexWing > zApexHTail  ){

						zDistanceArray[i] =
								verticalDistanceInitial.doubleValue(SI.METER) - (
										(horizontalDistanceInitial.doubleValue(SI.METER) *
										Math.tan(startingAngle- i * deltaAlpha + downwashRad)));
					}

					zDistanceArray[i] =
							zDistanceArray[i] * 
							Math.cos(startingAngle- i * deltaAlpha + downwashRad);

			downwashGradientArray[i] = calculateDownwashGradientSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zDistanceArray[i],
					cLAlphaArray[i], 
					theStabilityManager.getWingSweepQuarterChord(),
					theStabilityManager.getWingAspectRatio(),
					theStabilityManager.getWingSemiSpan()
					);

			downwashArray[i] = epsilonZero.doubleValue(NonSI.DEGREE_ANGLE) + downwashGradientArray[i]*alphaWingArray[i];

			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			
			if (zApexWing < zApexHTail  ){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) + (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
								Math.tan(startingAngle- i * deltaAlpha + downwashRad)));
			}

			if (zApexWing > zApexHTail  ){

				zDistanceArray[i] =
						verticalDistanceInitial.doubleValue(SI.METER) - (
								(horizontalDistanceInitial.doubleValue(SI.METER) *
								Math.tan(startingAngle- i * deltaAlpha + downwashRad)));
			}

			zDistanceArray[i] =
					zDistanceArray[i] * 
					Math.cos(startingAngle- i * deltaAlpha + downwashRad);

			xDistanceArray[i] = xDistanceZero.doubleValue(SI.METER);
			downwashGradientArray[i] = calculateDownwashGradientSlingerland( 
					xDistanceZero.doubleValue(SI.METER),
					zDistanceArray[i],
					cLAlphaArray[i], 
					theStabilityManager.getWingSweepQuarterChord(),
					theStabilityManager.getWingAspectRatio(),
					theStabilityManager.getWingSemiSpan()
					);
			
			downwashArray[i] = epsilonZero.doubleValue(NonSI.DEGREE_ANGLE) + downwashGradientArray[i]*alphaWingArray[i];
			alphaBodyArray[i] = alphaAbsoluteArray[i] - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
		}
		
//		System.out.println("\n Downwash Arrays");
//		System.out.println("DownwashGradient " + Arrays.toString(downwashGradientArray));
//		System.out.println("Downwash Angle (deg) " + Arrays.toString(downwashArray));
//		System.out.println("Alpha Absolute (deg) " + Arrays.toString(alphaAbsoluteArray));
//		System.out.println("Alpha Body (deg)" + Arrays.toString(alphaBodyArray));
//		System.out.println("m Distances  (m) " + Arrays.toString(zDistanceArray));	
//		System.out.println("x Distances (m) " + Arrays.toString(xDistanceArray));	

		Double[] downwashArrayTemporary = MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				downwashArray,
				alphaBody
				);
		
		Double[] downwashGradientArrayTemporary = MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				downwashGradientArray,
				alphaBody
				);
		
		Double[] horizontalDistanceTemporary = MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				xDistanceArray,
				alphaBody
				);
		
		Double[] verticalDistanceTemporary = MyMathUtils.getInterpolatedValue1DLinear(
				alphaBodyArray,
				zDistanceArray,
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
				
		for (int i=0; i<alphaBody.length; i++){
		downwashGradient.add(downwashGradientArrayTemporary[i]);
		downwashAngle.add(Amount.valueOf(downwashArrayTemporary[i],NonSI.DEGREE_ANGLE));
		horizontalDistance.add(Amount.valueOf(horizontalDistanceTemporary[i],SI.METER));
		verticalDistance.add(Amount.valueOf(verticalDistanceTemporary[i],SI.METER));
		}
	}

	public double calculateDownwashGradientSlingerland(
			Double rHorizontalDistance,
			Double mVerticalDistance,
			Double clAlphaRad,
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

		double downwashGradientLinearatZ=kFraction*(first+second*third)*((clAlphaRad/(Math.PI*aspectRatio)));

		return downwashGradientLinearatZ;

	}

	
	
	public void nasaBlackwellCLMax(
			int _numberOfPointSemiSpanWise,
			NasaBlackwell theNasaBlackwellCalculator,
			List<Double>_clMaxDistribution
			) {

		double _vortexSemiSpanToSemiSpanRatio = 1./(2*_numberOfPointSemiSpanWise);
		double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(0.0, 40, 31);
		double[] clDistributionActualNasaBlackwell = new double[_numberOfPointSemiSpanWise]; 
		boolean firstIntersectionFound = false;
		int indexOfFirstIntersection = 0;
		int indexOfAlphaFirstIntersection = 0;
		double diffCLapp = 0;
		double diffCLappOld = 0;
		double diffCL = 0;
		double accuracy =0.0001;
		double deltaAlpha = 0.0;
		Amount<Angle> alphaNew = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		

			for (int i=0; i < alphaArrayNasaBlackwell.length; i++) {
				if(firstIntersectionFound == false) {
					theNasaBlackwellCalculator.calculate(
							Amount.valueOf(
									alphaArrayNasaBlackwell[i],
									NonSI.DEGREE_ANGLE).to(SI.RADIAN)
							);
					clDistributionActualNasaBlackwell = 
							theNasaBlackwellCalculator
							.getClTotalDistribution()
							.toArray();

					for(int j =0; j < _numberOfPointSemiSpanWise; j++) {
						if( clDistributionActualNasaBlackwell[j] > _clMaxDistribution.get(j)) {
							firstIntersectionFound = true;
							indexOfFirstIntersection = j;
							break;
						}
					}
				}
				else {
					indexOfAlphaFirstIntersection = i;
					break;
				}
			}
	
		
		//@author Manuela ruocco
		// After find the first point where CL_wing > Cl_MAX_airfoil, starts an iteration on alpha
		// in order to improve the accuracy.

		for (int k = indexOfFirstIntersection; k< _numberOfPointSemiSpanWise; k++) {
			diffCLapp = ( clDistributionActualNasaBlackwell[k] -  _clMaxDistribution.get(k));
			diffCL = Math.max(diffCLapp, diffCLappOld);
			diffCLappOld = diffCL;
		}
		if( Math.abs(diffCL) < accuracy){
			cLMaxFinal =  theNasaBlackwellCalculator.getCLCurrent();
			alphaMaxLinear = 
					Amount.valueOf(
							theNasaBlackwellCalculator.getAlphaCurrent(),
							NonSI.DEGREE_ANGLE);
					 
		}
		else{
			deltaAlpha = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] 
						- alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection-1];
			alphaNew = Amount.valueOf(
					(alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] - (deltaAlpha/2)),
					NonSI.DEGREE_ANGLE
					).to(SI.RADIAN);
			double alphaOld = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection]; 
			diffCLappOld = 0;
			while ( diffCL > accuracy){
				diffCL = 0;
				theNasaBlackwellCalculator.calculate(alphaNew);
				clDistributionActualNasaBlackwell = theNasaBlackwellCalculator
						.getClTotalDistribution()
							.toArray();
				for (int m =0; m< _numberOfPointSemiSpanWise; m++) {
					diffCLapp = clDistributionActualNasaBlackwell[m] - _clMaxDistribution.get(m);

					if ( diffCLapp > 0 ){
						diffCL = Math.max(diffCLapp,diffCLappOld);
						diffCLappOld = diffCL;
					}

				}
				deltaAlpha = Math.abs(alphaOld - alphaNew.doubleValue(NonSI.DEGREE_ANGLE));
				alphaOld = alphaNew.doubleValue(NonSI.DEGREE_ANGLE);
				if (diffCL == 0){ //this means that diffCL would have been negative
					alphaNew = Amount.valueOf(
							alphaOld + (deltaAlpha/2),
							NonSI.DEGREE_ANGLE
							);
					diffCL = 1; // generic positive value in order to enter again in the while cycle 
					diffCLappOld = 0;
				}
				else { 
					if(deltaAlpha > 0.005){
						alphaNew = Amount.valueOf(
								alphaOld - (deltaAlpha/2),
								NonSI.DEGREE_ANGLE
								);	
						diffCLappOld = 0;
						if ( diffCL < accuracy) break;
					}
					else {
						alphaNew = Amount.valueOf(
								alphaOld - (deltaAlpha),
								NonSI.DEGREE_ANGLE
								);	
						diffCLappOld = 0;
						if ( diffCL < accuracy) 
							break;
					}
				}
			}
			theNasaBlackwellCalculator.calculate(alphaNew.to(SI.RADIAN));
			liftDistributionAtCLMax = 
					theNasaBlackwellCalculator.getClTotalDistribution().toArray();
					
			cLMaxFinal=  theNasaBlackwellCalculator.getCLCurrent()	;
			alphaMaxLinear =  alphaNew;
		}
	}
	
	public void calculateHighLiftDevicesEffects(
			StabilityExecutableManager theStabilityManager,
			List<Amount<Angle>> flapDeflections,
			List<Amount<Angle>> slatDeflections,
			Double currentLiftingCoefficient
			) {
		

		// Setup database(s)

		databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		highLiftDatabaseFileName = "HighLiftDatabase.h5";
		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
		
		//--------------------------------------------
		// Managing flaps types:
		List<Double> flapTypeIndex = new ArrayList<Double>();
		List<Double> deltaFlapRef = new ArrayList<Double>();

		for(int i=0; i<theStabilityManager.getWingNumberOfFlaps(); i++) {
			if(theStabilityManager.getWingFlapType().get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
				flapTypeIndex.add(1.0);
				deltaFlapRef.add(45.0);
			}
			else if(theStabilityManager.getWingFlapType().get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapTypeIndex.add(2.0);
				deltaFlapRef.add(50.0);
			}
			else if(theStabilityManager.getWingFlapType().get(i) == FlapTypeEnum.PLAIN) {
				flapTypeIndex.add(3.0);
				deltaFlapRef.add(60.0);
			}
			else if(theStabilityManager.getWingFlapType().get(i) == FlapTypeEnum.FOWLER) {
				flapTypeIndex.add(4.0);
				deltaFlapRef.add(40.0);
			}
			else if(theStabilityManager.getWingFlapType().get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
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

		for(int i=0; i<theStabilityManager.getWingNumberOfFlaps(); i++) {
			etaInFlap.add(theStabilityManager.getWingEtaInFlap().get(i));
			etaOutFlap.add(theStabilityManager.getWingEtaOutFlap().get(i));
			cfc.add(theStabilityManager.getWingFlapCfC().get(i));
		}
		if(theStabilityManager.getWingNumberOfSlats()!=0) {
			for(int i=0; i<theStabilityManager.getWingNumberOfSlats(); i++) {
				etaInSlat.add(theStabilityManager.getWingEtaInSlat().get(i));
				etaOutSlat.add(theStabilityManager.getWingEtaOutSlat().get(i));
				csc.add(theStabilityManager.getWingSlatCsC().get(i));
				cExtcSlat.add(theStabilityManager.getWingCExtCSlat().get(i));
			}
		}
		
		//--------------------------------------------
		// Creating arrays of the required parameters to be interpolated:
		double [] clAlphaMeanFlap = new double [theStabilityManager.getWingNumberOfFlaps()];
		double [] clZeroMeanFlap = new double [theStabilityManager.getWingNumberOfFlaps()];
		double [] maxTicknessMeanFlap = new double [theStabilityManager.getWingNumberOfFlaps()];
		double [] maxTicknessMeanSlat = new double [theStabilityManager.getWingNumberOfSlats()];
		double [] maxTicknessFlapStations = new double [2*theStabilityManager.getWingNumberOfFlaps()];
		double [] clAlphaFlapStations = new double [2*theStabilityManager.getWingNumberOfFlaps()];
		double [] clZeroFlapStations = new double [2*theStabilityManager.getWingNumberOfFlaps()];
		double [] leRadiusMeanSlat = new double [theStabilityManager.getWingNumberOfSlats()];
		double [] chordMeanSlat = new double [theStabilityManager.getWingNumberOfSlats()];
		double [] leadingEdgeRadiusSlatStations = new double [2*theStabilityManager.getWingNumberOfSlats()];
		double [] maxTicknessSlatStations = new double [2*theStabilityManager.getWingNumberOfSlats()];
		double [] chordSlatStations = new double [2*theStabilityManager.getWingNumberOfSlats()];
		
		double [] influenceFactor = new double [2];
		
		for ( int i=0; i< theStabilityManager.getWingNumberOfFlaps(); i++){
			int kk = i*2;
			
			clAlphaFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
							MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getWingClAlphaBreakPointsDeg()),
					etaOutFlap.get(i));
			
			clAlphaFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getWingClAlphaBreakPointsDeg()),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingCl0BreakPoints()
							),
					etaInFlap.get(i));
			
			clZeroFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingCl0BreakPoints()
							),
					etaOutFlap.get(i));
			
			maxTicknessFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.get_wingMaxThicknessBreakPoints()
							),
					etaInFlap.get(i));
			
			maxTicknessFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.getWingYAdimensionalBreakPoints()
							),
					MyArrayUtils.convertToDoublePrimitive(
							theStabilityManager.get_wingMaxThicknessBreakPoints()
							),
					etaOutFlap.get(i));
			
			try {
				influenceFactor = calculateInfluenceFactorsMeanAirfoilFlap(
						etaInFlap.get(i),
						etaOutFlap.get(i),
						theStabilityManager
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
		
		if(theStabilityManager.getWingNumberOfSlats()!=0) 
			for ( int i=0; i< theStabilityManager.getWingNumberOfSlats(); i++){
				int kk = i*2;

				leadingEdgeRadiusSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theStabilityManager.getWingLERadiusBreakPoints()
								),
						etaOutSlat.get(i));

				leadingEdgeRadiusSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theStabilityManager.getWingLERadiusBreakPoints()
								),
						etaInSlat.get(i));

				chordSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theStabilityManager.getWingChordsBreakPoints()
								),
						etaInSlat.get(i));

				chordSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								theStabilityManager.getWingChordsBreakPoints()
								),
						etaOutSlat.get(i));


				maxTicknessSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.get_wingMaxThicknessBreakPoints()
								),
						etaInSlat.get(i));

				maxTicknessSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.getWingYAdimensionalBreakPoints()
								),
						MyArrayUtils.convertToDoublePrimitive(
								theStabilityManager.get_wingMaxThicknessBreakPoints()
								),
						etaOutSlat.get(i));

				try {
					influenceFactor = calculateInfluenceFactorsMeanAirfoilFlap(
							etaInSlat.get(i),
							etaOutSlat.get(i),
							theStabilityManager
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
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlapPlain(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								cfc.get(i)
								)
						);
			else
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlap(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								flapTypeIndex.get(i))
						);
		}

		List<Double> deltaCl0First = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0First.add(
					alphaDelta.get(i).doubleValue()
					*etaDeltaFlap.get(i).doubleValue()
					*flapDeflections.get(i).doubleValue(SI.RADIAN)
					*(clAlphaMeanFlap[i]*57.3)
					);

		List<Double> deltaCCfFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCCfFlap.add(
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(
							flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							flapTypeIndex.get(i)
							)
					);

		List<Double> cFirstCFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*cfc.get(i).doubleValue()));

		List<Double> deltaCl0FlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0FlapList.add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(clZeroMeanFlap[i]*(cFirstCFlap.get(i).doubleValue()-1))
					);
		theStabilityManager.setDeltaCl0FlapList(deltaCl0FlapList);
		
		double deltaCl0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCl0Flap += deltaCl0FlapList.get(i);
		theStabilityManager.setDeltaCl0Flap(deltaCl0Flap);
		
		//---------------------------------------------------------------
		// deltaClmax (flap)
		List<Double> deltaClmaxBase = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxBase.add(
					highLiftDatabaseReader
					.getDeltaCLmaxBaseVsTc(
							maxTicknessMeanFlap[i],
							flapTypeIndex.get(i)
							)
					);

		List<Double> k1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			if (cfc.get(i) <= 0.30)
				k1.add(highLiftDatabaseReader
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
			k2.add(highLiftDatabaseReader
					.getK2VsDeltaFlap(
							flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							flapTypeIndex.get(i)
							)
					);

		List<Double> k3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			k3.add(highLiftDatabaseReader
					.getK3VsDfDfRef(
							flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							deltaFlapRef.get(i),
							flapTypeIndex.get(i)
							)
					);

		List<Double> deltaClmaxFlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxFlapList.add(
					k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmaxBase.get(i).doubleValue()
					);
		theStabilityManager.setDeltaClmaxFlapList(deltaClmaxFlapList);
		
		double deltaClmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaClmaxFlap += deltaClmaxFlapList.get(i);
		theStabilityManager.setDeltaClmaxFlap(deltaClmaxFlap);
		
		//---------------------------------------------------------------
		// deltaClmax (slat)
		if(!slatDeflections.isEmpty()) {

			List<Double> dCldDelta = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				dCldDelta.add(highLiftDatabaseReader
						.getDCldDeltaVsCsC(csc.get(i))
						);

			List<Double> etaMaxSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				etaMaxSlat.add(highLiftDatabaseReader
						.getEtaMaxVsLEradiusTicknessRatio(
								leRadiusMeanSlat[i]/(chordMeanSlat[i]),
								maxTicknessMeanSlat[i]
										)
						);

			List<Double> etaDeltaSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				etaDeltaSlat.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaSlat(slatDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);

			List<Double> deltaClmaxSlatList = new ArrayList<>();
			for(int i=0; i<slatDeflections.size(); i++)
				deltaClmaxSlatList.add(
						dCldDelta.get(i).doubleValue()
						*etaMaxSlat.get(i).doubleValue()
						*etaDeltaSlat.get(i).doubleValue()
						*slatDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE)
						*cExtcSlat.get(i).doubleValue()
						);
			theStabilityManager.setDeltaClmaxSlatList(deltaClmaxSlatList);
			
			double deltaClmaxSlat = 0.0;
			for(int i=0; i<slatDeflections.size(); i++)
				deltaClmaxSlat += deltaClmaxSlatList.get(i);
			theStabilityManager
			.setDeltaClmaxSlat(deltaClmaxSlat);
					
			
			//---------------------------------------------------------------
			// deltaCLmax (slat)
			List<Double> kLambdaSlat = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				kLambdaSlat.add(
						Math.pow(Math.cos(theStabilityManager.getWingSweepQuarterChord().doubleValue(SI.RADIAN)),0.75)
						*(1-(0.08*Math.pow(Math.cos(theStabilityManager.getWingSweepQuarterChord().doubleValue(SI.RADIAN)), 2)))
						);

			List<Double> slatSurface = new ArrayList<Double>();
			for(int i=0; i<slatDeflections.size(); i++)
				slatSurface.add(
						Math.abs(theStabilityManager.getWingSpan().doubleValue(SI.METER)
								/2*theStabilityManager.getWingChordsBreakPoints().get(0).doubleValue(SI.METER)
								*(2-(1-theStabilityManager.getWingTaperRatio())*(etaInSlat.get(i)+etaOutSlat.get(i)))
								*(etaOutSlat.get(i)-etaInSlat.get(i))
								)
						);

			List<Double> deltaCLmaxSlatList = new ArrayList<>();
			for(int i=0; i<slatDeflections.size(); i++)
				deltaCLmaxSlatList.add(
						deltaClmaxSlatList.get(i)
						*(slatSurface.get(i)/theStabilityManager.getWingSurface().doubleValue(SI.SQUARE_METRE))
						*kLambdaSlat.get(i));
			theStabilityManager.setDeltaCLmaxSlatList(deltaCLmaxSlatList);

			double deltaCLmaxSlat = 0.0;
			for(int i=0; i<slatDeflections.size(); i++)
				deltaCLmaxSlat += deltaCLmaxSlatList.get(i);
			theStabilityManager.setDeltaCLmaxSlat(deltaCLmaxSlat);
		}
		else {
			theStabilityManager.setDeltaClmaxSlatList(null);
			theStabilityManager.setDeltaClmaxSlat(null);
			theStabilityManager.setDeltaCLmaxSlatList(null);
			theStabilityManager.setDeltaCLmaxSlat(null);
		}
			
		//---------------------------------------------------------------
		// deltaCL0 (flap)
		List<Double> kc = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kc.add(highLiftDatabaseReader
					.getKcVsAR(
							theStabilityManager.getWingAspectRatio(),
							alphaDelta.get(i))	
					);

		List<Double> kb = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kb.add(highLiftDatabaseReader
					.getKbVsFlapSpanRatio(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theStabilityManager.getWingTaperRatio())	
					);

		double cLLinearSlope = theStabilityManager
				.get_wingcLAlphaDeg();

		List<Double> deltaCL0FlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCL0FlapList.add(
					kb.get(i).doubleValue()
					*kc.get(i).doubleValue()
					*deltaCl0FlapList.get(i)
					*((cLLinearSlope)/(clAlphaMeanFlap[i]))
					);
		theStabilityManager.setDeltaCL0FlapList(deltaCL0FlapList);

		double deltaCL0Flap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCL0Flap += deltaCL0FlapList.get(i);
		theStabilityManager.setDeltaCL0Flap(deltaCL0Flap);
		
		//---------------------------------------------------------------
		// deltaCLmax (flap)
		List<Double> flapSurface = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			flapSurface.add(
					Math.abs(
							theStabilityManager.getWingSpan().doubleValue(SI.METER)							
							/2*theStabilityManager.getWingChordsBreakPoints().get(0).doubleValue(SI.METER)
							*(2-((1-theStabilityManager.getWingTaperRatio())*(etaInFlap.get(i)+etaOutFlap.get(i))))
							*(etaOutFlap.get(i)-etaInFlap.get(i))
							)
					);

		List<Double> kLambdaFlap = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			kLambdaFlap.add(
					Math.pow(Math.cos(theStabilityManager.getWingSweepQuarterChord().doubleValue(SI.RADIAN)),0.75)
					*(1-(0.08*Math.pow(Math.cos(theStabilityManager.getWingSweepQuarterChord().doubleValue(SI.RADIAN)), 2)))
					);

		List<Double> deltaCLmaxFlapList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlapList.add(
					deltaClmaxFlapList.get(i)
					*(flapSurface.get(i)/theStabilityManager.getWingSurface().doubleValue(SI.SQUARE_METRE))
					*kLambdaFlap.get(i)
					);
		theStabilityManager
			.setDeltaCLmaxFlapList( deltaCLmaxFlapList);
					
		
		double deltaCLmaxFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlap += deltaCLmaxFlapList.get(i).doubleValue();
		theStabilityManager.setDeltaCLmaxFlap(deltaCLmaxFlap);

		//---------------------------------------------------------------
		// new CLalpha

		List<Double> cLalphaFlapList = new ArrayList<Double>();
		List<Double> swf = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			cLalphaFlapList.add(
					cLLinearSlope
					*(1+((deltaCL0FlapList.get(i)/
							deltaCl0FlapList.get(i))
							*(cFirstCFlap.get(i)*(1-((cfc.get(i))*(1/cFirstCFlap.get(i))
									*Math.pow(Math.sin(flapDeflections.get(i).doubleValue(SI.RADIAN)), 2)))-1))));
			swf.add(flapSurface.get(i)/theStabilityManager.getWingSurface().doubleValue(SI.SQUARE_METRE));
		}

		double swfTot = 0;
		for(int i=0; i<swf.size(); i++)
			swfTot += swf.get(i);

		double cLAlphaFlap = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			cLAlphaFlap += cLalphaFlapList.get(i)*swf.get(i);

		cLAlphaFlap /= swfTot;
		theStabilityManager.setCLAlphaHighLift(cLAlphaFlap); // 1/deg
		
		//---------------------------------------------------------------
		// deltaCD
		List<Double> delta1 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta1.add(
						highLiftDatabaseReader
						.getDelta1VsCfCPlain(
								cfc.get(i),
								maxTicknessMeanFlap[i]
								)
						);
			else
				delta1.add(
						highLiftDatabaseReader
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
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapPlain(flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						);
			else
				delta2.add(
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapSlotted(
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE),
								maxTicknessMeanFlap[i]
								)
						);
		}

		List<Double> delta3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			delta3.add(
					highLiftDatabaseReader
					.getDelta3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theStabilityManager.getWingTaperRatio()
							)
					);
		}

		List<Double> deltaCDList = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++) {
			deltaCDList.add(
					delta1.get(i)*
					delta2.get(i)*
					delta3.get(i)
					);
		}
		theStabilityManager.setDeltaCD0List(deltaCDList);
		
		
		double deltaCD = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCD += deltaCDList.get(i).doubleValue();
		theStabilityManager.setDeltaCD0(deltaCD);

		//---------------------------------------------------------------
		// deltaCM_c/4
		List<Double> mu1 = new ArrayList<Double>();
		for (int i=0; i<flapTypeIndex.size(); i++)
			if(flapTypeIndex.get(i) == 3.0)
				mu1.add(
						highLiftDatabaseReader
						.getMu1VsCfCFirstPlain(
								(cfc.get(i))*(1/cFirstCFlap.get(i)),
								flapDeflections.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								)
						);
			else
				mu1.add(highLiftDatabaseReader
						.getMu1VsCfCFirstSlottedFowler((cfc.get(i))*(1/cFirstCFlap.get(i)))
						);

		List<Double> mu2 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu2.add(highLiftDatabaseReader
					.getMu2VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theStabilityManager.getWingTaperRatio()
							)
					);

		List<Double> mu3 = new ArrayList<Double>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			mu3.add(highLiftDatabaseReader
					.getMu3VsBfB(
							etaInFlap.get(i),
							etaOutFlap.get(i),
							theStabilityManager.getWingTaperRatio()
							)
					);

		
		List<Double> deltaCMc4List = new ArrayList<>();
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMc4List.add(
					(mu2.get(i)*(-(mu1.get(i)
							*deltaClmaxFlapList.get(i)
							*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
									*((cFirstCFlap.get(i))-1)
									*(currentLiftingCoefficient + 
											(deltaClmaxFlapList.get(i)
											*(1-(flapSurface.get(i)/theStabilityManager
													.getWingSurface().doubleValue(SI.SQUARE_METRE)))))
									*(1/8)))) + (0.7*(theStabilityManager
											.getWingAspectRatio()/(1+(theStabilityManager
													.getWingAspectRatio()/2)))
											*mu3.get(i)
											*deltaClmaxFlapList.get(i)
											*Math.tan(theStabilityManager
													.getWingSweepQuarterChord().doubleValue(SI.RADIAN)))
					);
		theStabilityManager.setDeltaCMc4List(deltaCMc4List);

		double deltaCMC4 = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMC4 += deltaCMc4List.get(i).doubleValue();
		theStabilityManager.setDeltaCMc4(deltaCMC4);
	}
	
	
	public static double[] calculateInfluenceFactorsMeanAirfoilFlap(
			double etaIn,
			double etaOut,
			StabilityExecutableManager theStabilityManager
			
			) throws InstantiationException, IllegalAccessException{

		double [] influenceAreas = new double [2];
		double [] influenceFactors = new double [2];
		
		double chordIn = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						theStabilityManager.getWingYAdimensionalBreakPoints()
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						theStabilityManager.getWingChordsBreakPoints()
						),
				etaIn
				);

		double chordOut = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						theStabilityManager.getWingYAdimensionalBreakPoints()
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						theStabilityManager.getWingChordsBreakPoints()
						),
				etaOut
				);
		
		influenceAreas[0] = (chordIn * ((etaOut - etaIn)*theStabilityManager.getWingSemiSpan().doubleValue(SI.METER)))/2;
		influenceAreas[1] = (chordOut * ((etaOut - etaIn)*theStabilityManager.getWingSemiSpan().doubleValue(SI.METER)))/2;
		
		// it returns the influence coefficient
		
		influenceFactors[0] = influenceAreas[0]/(influenceAreas[0] + influenceAreas[1]);
		influenceFactors[1] = influenceAreas[1]/(influenceAreas[0] + influenceAreas[1]);
		
		return influenceFactors;
}

	public static double[] calculateInfluenceFactorsMeanAirfoilElevator(
			double etaIn,
			double etaOut,
			StabilityExecutableManager theStabilityManager
			
			) throws InstantiationException, IllegalAccessException{

		double [] influenceAreas = new double [2];
		double [] influenceFactors = new double [2];
		
		double chordIn = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						theStabilityManager.getHTailYAdimensionalBreakPoints()
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						theStabilityManager.getHTailChordsBreakPoints()
						),
				etaIn
				);

		double chordOut = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						theStabilityManager.getHTailYAdimensionalBreakPoints()
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						theStabilityManager.getHTailChordsBreakPoints()
						),
				etaOut
				);
		
		influenceAreas[0] = (chordIn * ((etaOut - etaIn)*theStabilityManager.getHTailSemiSpan().doubleValue(SI.METER)))/2;
		influenceAreas[1] = (chordOut * ((etaOut - etaIn)*theStabilityManager.getHTailSemiSpan().doubleValue(SI.METER)))/2;
		
		// it returns the influence coefficient
		
		influenceFactors[0] = influenceAreas[0]/(influenceAreas[0] + influenceAreas[1]);
		influenceFactors[1] = influenceAreas[1]/(influenceAreas[0] + influenceAreas[1]);
		
		return influenceFactors;
}
	
	public void calculateElevatorEffects(
			StabilityExecutableManager theStabilityManager,
			Amount<Angle> elevatorDeflectionAngle
			) throws InstantiationException, IllegalAccessException {
		
		int correctionFactor = 0;
		Amount<Angle> databaseValue = null;
		if(elevatorDeflectionAngle.doubleValue(NonSI.DEGREE_ANGLE)<0){
			databaseValue = elevatorDeflectionAngle.opposite();
			correctionFactor = -1;
		}
		if(elevatorDeflectionAngle.doubleValue(NonSI.DEGREE_ANGLE)>=0){
			databaseValue = elevatorDeflectionAngle;
			correctionFactor = 1;
		}

		// Setup database(s)

		databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		highLiftDatabaseFileName = "HighLiftDatabase.h5";
		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
		
		double [] elevatorInfluenceFactor = new double [2];
		double cfc = theStabilityManager.getElevatorCfC();
		
		// set elevator type --> plain flap
		
		Double elevatorTypeIndex = 3.0;
		Double deltaElevatorRef = 60.0;
		Double cLalphaFlapList, swf, deltaCCfElevator, cFirstCElevator;
		
		deltaCCfElevator =(
				highLiftDatabaseReader
				.getDeltaCCfVsDeltaFlap(
						databaseValue.doubleValue(NonSI.DEGREE_ANGLE),
						elevatorTypeIndex
						));

		cFirstCElevator = (1+(deltaCCfElevator*theStabilityManager.getElevatorCfC()));
		elevatorInfluenceFactor = calculateInfluenceFactorsMeanAirfoilElevator(
				theStabilityManager.getElevatorEtaIn(),
				theStabilityManager.getElevatorEtaOut(), 
				theStabilityManager
				);
		
		double maxTicknessFlapStationsIn = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailMaxThicknessBreakPoints()),
				theStabilityManager.getElevatorEtaIn());
		
		double maxTicknessFlapStationsOut = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailMaxThicknessBreakPoints()),
				theStabilityManager.getElevatorEtaOut());
		
		double clAlphaFlapStationsIn = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailClAlphaBreakPointsDeg()),
				theStabilityManager.getElevatorEtaIn());
		
		double clAlphaFlapStationsOut = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailClAlphaBreakPointsDeg()),
				theStabilityManager.getElevatorEtaOut());
		
		double clZeroFlapStationsIn = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailCl0BreakPoints()),
				theStabilityManager.getElevatorEtaIn());
		
		double clZeroFlapStationsOut = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailYAdimensionalBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(theStabilityManager.getHTailCl0BreakPoints()),
				theStabilityManager.getElevatorEtaOut());
		
		Double maxTicknessMeanElevator = maxTicknessFlapStationsIn* elevatorInfluenceFactor[0] + 
				maxTicknessFlapStationsOut* elevatorInfluenceFactor[1];
		
		Double clAlphaMeanElevator = clAlphaFlapStationsIn * elevatorInfluenceFactor[0] + 
				clAlphaFlapStationsOut*elevatorInfluenceFactor[1];
		
		Double cl0MeanElevator = clZeroFlapStationsIn * elevatorInfluenceFactor[0] + 
				clZeroFlapStationsOut*elevatorInfluenceFactor[1];
		
		//---------------------------------------------------------------
		// deltaClmax 
		
		Double deltaClmaxBase;
			deltaClmaxBase =highLiftDatabaseReader.getDeltaCLmaxBaseVsTc(maxTicknessMeanElevator,elevatorTypeIndex);
		Double k1 = null;
			if (cfc <= 0.30)
				k1 = (highLiftDatabaseReader.getK1vsFlapChordRatio(cfc, elevatorTypeIndex));
			else if ((cfc > 0.30))
				k1 = ((608.31*Math.pow(cfc, 5))-(626.15*Math.pow(cfc, 4))+(263.4*Math.pow(cfc, 3))-(62.946*Math.pow(cfc, 2))
						+(10.638*cfc)+0.0064);
		Double k2 = null;
			k2 = highLiftDatabaseReader
					.getK2VsDeltaFlap(
							databaseValue.doubleValue(NonSI.DEGREE_ANGLE),
							elevatorTypeIndex
							);
		Double k3= null;
			k3=highLiftDatabaseReader
					.getK3VsDfDfRef(
							databaseValue.doubleValue(NonSI.DEGREE_ANGLE),
							deltaElevatorRef,
							elevatorTypeIndex
							);
		Double deltaClmaxElevator;
		deltaClmaxElevator=
					k1*k2*k3*deltaClmaxBase.doubleValue();
		
		//---------------------------------------------------------------
		// deltaCLmax
		Double elevatorSurface;
		elevatorSurface =
					Math.abs(
							theStabilityManager.getHTailSpan().doubleValue(SI.METER)							
							/2*theStabilityManager.getHTailChordsBreakPoints().get(0).doubleValue(SI.METER)
							*(2-((1-theStabilityManager.getHTailTaperRatio())*
									(theStabilityManager.getElevatorEtaIn()+
											theStabilityManager.getElevatorEtaOut())))
							*(theStabilityManager.getElevatorEtaOut()-theStabilityManager.getElevatorEtaIn())
							);

		Double kLambdaFlap;

			kLambdaFlap=
					Math.pow(Math.cos(theStabilityManager.getHTailSweepQuarterChord().doubleValue(SI.RADIAN)),0.75)
					*(1-(0.08*Math.pow(Math.cos(theStabilityManager.getHTailSweepQuarterChord().doubleValue(SI.RADIAN)), 2)))
					;

		Double deltaCLmaxElevator;
		 deltaCLmaxElevator = 
				 deltaClmaxElevator
					*(elevatorSurface/theStabilityManager.getHTailSurface().doubleValue(SI.SQUARE_METRE))*kLambdaFlap;
		 
		theStabilityManager
			.getDeltaCLMaxElevator().put(elevatorDeflectionAngle, correctionFactor*deltaCLmaxElevator);
		
		//---------------------------------------------------------------
		// deltaCD
		Double delta1;

				delta1 = (
						highLiftDatabaseReader
						.getDelta1VsCfCPlain(
								cfc,
								maxTicknessMeanElevator
								)
						);


		Double delta2;

				delta2 = (
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapPlain(Math.abs(elevatorDeflectionAngle.doubleValue(NonSI.DEGREE_ANGLE)))
						);
		

		Double delta3;
			delta3=(
					highLiftDatabaseReader
					.getDelta3VsBfB(
							theStabilityManager.getElevatorEtaIn(),
							theStabilityManager.getElevatorEtaOut(),
							theStabilityManager.getHTailTaperRatio()
							)
					);


		Double deltaCDList;

			deltaCDList = (
					delta1*
					delta2*
					delta3
					);
		
		theStabilityManager
		.getDeltaCD0Elevator().put(elevatorDeflectionAngle, deltaCDList);

	}
	
	public List<Amount<Angle>> calculateInducedAngleOfAttackDistribution(
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
			
//			System.out.println("\n \n------------------------------------------- ");
//			System.out.println("\nVertical velocity " + verticalVelocity[i] );
//			System.out.println("Velocity " + velocity);

			inducedAngleOfAttack.add(i, Amount.valueOf(
					Math.atan(verticalVelocity[i]/Tas.doubleValue(SI.METERS_PER_SECOND))*57.3/2,NonSI.DEGREE_ANGLE));

//			System.out.println( alphaInduced[i]);
		//	System.out.println( yStationsActual[i]/semispan);		
//			System.out.println(" alpha actual " + alphaInitial.getEstimatedValue());

		}
		return inducedAngleOfAttack;

	}
	public List<Amount<Length>> getHorizontalDistance() {
		return horizontalDistance;
	}

	public List<Amount<Length>> getVerticalDistance() {
		return verticalDistance;
	}

	public List<Amount<Angle>> getDownwashAngle() {
		return downwashAngle;
	}

	public List<Double> getDownwashGradient() {
		return downwashGradient;
	}

	public void setHorizontalDistance(List<Amount<Length>> horizontalDistance) {
		this.horizontalDistance = horizontalDistance;
	}

	public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
		this.verticalDistance = verticalDistance;
	}

	public void setDownwashAngle(List<Amount<Angle>> downwashAngle) {
		this.downwashAngle = downwashAngle;
	}

	public void setDownwashGradient(List<Double> downwashGradient) {
		this.downwashGradient = downwashGradient;
	}

	public double getcLMaxFinal() {
		return cLMaxFinal;
	}

	public Amount<Angle> getAlphaMaxLinear() {
		return alphaMaxLinear;
	}

	public double[] getLiftDistributionAtCLMax() {
		return liftDistributionAtCLMax;
	}

	public void setcLMaxFinal(double cLMaxFinal) {
		this.cLMaxFinal = cLMaxFinal;
	}

	public void setAlphaMaxLinear(Amount<Angle> alphaMaxLinear) {
		this.alphaMaxLinear = alphaMaxLinear;
	}

	public void setLiftDistributionAtCLMax(double[] liftDistributionAtCLMax) {
		this.liftDistributionAtCLMax = liftDistributionAtCLMax;
	}

	public Amount<Angle> getEpsilonZero() {
		return epsilonZero;
	}

	public void setEpsilonZero(Amount<Angle> epsilonZero) {
		this.epsilonZero = epsilonZero;
	}

	public double getDownwashGradientConstant() {
		return downwashGradientConstant;
	}

	public void setDownwashGradientConstant(double downwashGradientConstant) {
		this.downwashGradientConstant = downwashGradientConstant;
	}

	public Amount<Length> getVerticalDistanceConstant() {
		return verticalDistanceConstant;
	}

	public void setVerticalDistanceConstant(Amount<Length> verticalDistanceConstant) {
		this.verticalDistanceConstant = verticalDistanceConstant;
	}

}