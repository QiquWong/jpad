package sandbox2.mr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.ACPerformanceCalculator.ACPerformanceCalculatorBuilder;
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
	
	//FINAL variables CLMAX
	double cLMaxFinal;
	Amount<Angle> alphaMaxLinear;
	double []  liftDistributionAtCLMax;
	
	public void calculateDownwashNonLinearSlingerland(
			Amount<Length> rootChord,
			Amount<Length> semiSpanWing,
			Amount<Angle> sweepQuarterChord,
			double aspectRatio,
			Amount<Angle> wingAngleOfIncidence,
			Amount<Angle> wingAlphaZeroLift,
			Amount<Length> horizontalDistanceInitial,
			Amount<Length> verticalDistanceInitial,
			Amount<Length> verticalDistanceInitialComplete,
			Amount<Length> zACWing,
			Amount<Length> zACHtail,
			double [] clAlphaArray,
			double []  alphaWingArrayforCLAlpha, // 1/rad
			double [] alphaBody //deg
			){
		
		double cRoot = rootChord.doubleValue(SI.METER);
		double iw = wingAngleOfIncidence.doubleValue(SI.RADIAN); //rad 
		double alphaZeroLift = wingAlphaZeroLift.doubleValue(SI.RADIAN); //rad; 
		double zTemp , xTemp;

		double m0Value = 0;
		double d0Value = 0;
		double angle =0.0;


		if ( zACHtail.doubleValue(SI.METER) > zACWing.doubleValue(SI.METER) ) {
			if ( verticalDistanceInitial.doubleValue(SI.METER) < 0 )
				m0Value = verticalDistanceInitial.doubleValue(SI.METER) - 0.75 * cRoot * Math.sin(wingAngleOfIncidence.doubleValue(SI.RADIAN));
			if ( verticalDistanceInitial.doubleValue(SI.METER) < 0 )
				m0Value = verticalDistanceInitial.doubleValue(SI.METER) + 0.75 * cRoot * Math.sin(wingAngleOfIncidence.doubleValue(SI.RADIAN));	
		}

		if ( zACHtail.doubleValue(SI.METER) < zACWing.doubleValue(SI.METER) ) {
			if ( verticalDistanceInitial.doubleValue(SI.METER) < 0 )
				m0Value = verticalDistanceInitial.doubleValue(SI.METER) + 0.75 * cRoot * Math.sin(wingAngleOfIncidence.doubleValue(SI.RADIAN));
			if ( verticalDistanceInitial.doubleValue(SI.METER) > 0 )
				m0Value = verticalDistanceInitial.doubleValue(SI.METER) - 0.75 * cRoot * Math.sin(wingAngleOfIncidence.doubleValue(SI.RADIAN));
		}


		d0Value = horizontalDistanceInitial.doubleValue(SI.METER) - 0.75 * cRoot * Math.cos(wingAngleOfIncidence.doubleValue(SI.RADIAN));
		
		
		double dValue = Math.sqrt((Math.pow(m0Value, 2)) 
				+(Math.pow(d0Value, 2)));
		
		double phiAngle = Math.atan(( m0Value)/(d0Value));  // rad	
		
		if ( zACHtail.doubleValue(SI.METER) > zACWing.doubleValue(SI.METER) ) {
		angle = phiAngle + iw - alphaZeroLift;}
		if ( zACHtail.doubleValue(SI.METER) < zACWing.doubleValue(SI.METER) ) {
		angle =  -iw + alphaZeroLift;}
		
		double zDistanceZero = verticalDistanceInitialComplete.doubleValue(SI.METER) * Math.cos(angle);
		System.out.println(" VERTICAL DISTANCE VARIABLE " + zDistanceZero);
		double xDistanceZero = (dValue * Math.cos(angle)) + ( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift)));

		// Alpha Absolute array 
		
		double alphaFirst = 0.0;
		double alphaLast = 20.0;
		int nValue = (int) Math.ceil(( alphaLast - alphaFirst ) * 4); //0.25 deg

		double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue);
		double [] alphaWingArray =  new double [alphaAbsoluteArray.length];
		for(int i=0; i< alphaAbsoluteArray.length; i++){
			alphaWingArray[i] = alphaAbsoluteArray[i]+alphaZeroLift*57.3;
		}
		double deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue();

		Double[] cLAlphaArray = MyMathUtils.getInterpolatedValue1DLinear(
				alphaWingArrayforCLAlpha,
				clAlphaArray, 
				alphaWingArray);
				
		
		double epsilonTemp, zDistTemp, downwashRad, epsilonTempRad,  downwashGradientArrayTemp;
		
		// Initialize Array
		
		double [] downwashArray = new double [nValue];
		double [] downwashGradientArray = new double [nValue];
		double [] alphaBodyArray = new double [nValue];
		double [] zDistanceArray = new double [nValue];
		double [] xDistanceArray = new double [nValue];
		
		
		// First step
		
		downwashArray[0] = 0.0;
		zDistanceArray[0] = zDistanceZero;
		xDistanceArray[0] = xDistanceZero;
		downwashGradientArray[0] = calculateDownwashGradientSlingerland( xDistanceArray[0],
				zDistanceArray[0], 
				cLAlphaArray[0], 
				sweepQuarterChord,
				aspectRatio,
				semiSpanWing
				);
		alphaBodyArray[0] = alphaAbsoluteArray[0]- Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
		
		
		// Other step
		
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
			
			epsilonTemp = downwashGradientArray[i-1] * alphaAbsoluteArray[i]; // epsilon in deg, using gradient at previous step
			epsilonTempRad = Amount.valueOf(
					Math.toRadians(epsilonTemp), SI.RADIAN).getEstimatedValue();
			
			
			
			zTemp = verticalDistanceInitialComplete.doubleValue(SI.METER) * Math.cos(angle - i * deltaAlpha + epsilonTempRad);
			xTemp = (dValue * Math.cos(angle - i*deltaAlpha + epsilonTempRad)) + 
					( (0.74) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + epsilonTempRad)));
			
			
			downwashGradientArrayTemp = calculateDownwashGradientSlingerland( xTemp,
					zTemp, 
					cLAlphaArray[0], 
					sweepQuarterChord,
					aspectRatio,
					semiSpanWing
					);
					
			downwashArray[i] = downwashArray[i-1] + downwashGradientArrayTemp * deltaAlpha*57.3; 
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			
			zDistanceArray[i] = verticalDistanceInitialComplete.doubleValue(SI.METER) * Math.cos(angle - i * deltaAlpha + downwashRad);
			xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
					( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
			
			downwashGradientArray[i] = calculateDownwashGradientSlingerland( xDistanceArray[i],
					zDistanceArray[i], 
					cLAlphaArray[i], 
					sweepQuarterChord,
					aspectRatio,
					semiSpanWing
					);

			downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			
			zDistanceArray[i] = verticalDistanceInitialComplete.doubleValue(SI.METER) * Math.cos(angle - i * deltaAlpha + downwashRad);
			xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
					( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
			
			downwashGradientArray[i] = calculateDownwashGradientSlingerland( xDistanceArray[i],
					zDistanceArray[i], 
					cLAlphaArray[i], 
					sweepQuarterChord,
					aspectRatio,
					semiSpanWing
					);
			
			downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
			
			alphaBodyArray[i] = alphaAbsoluteArray[i] - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
			
		}
		
		System.out.println("\n Downwash Arrays");
		System.out.println("DownwashGradient " + Arrays.toString(downwashGradientArray));
		System.out.println("Downwash Angle (deg) " + Arrays.toString(downwashArray));
		System.out.println("Alpha Absolute (deg) " + Arrays.toString(alphaAbsoluteArray));
		System.out.println("Alpha Body (deg)" + Arrays.toString(alphaBodyArray));
		System.out.println("m Distances  (m) " + Arrays.toString(zDistanceArray));	
		System.out.println("x Distances (m) " + Arrays.toString(xDistanceArray));	
		
		
		
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
		double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(0.0, 30, 31);
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

}