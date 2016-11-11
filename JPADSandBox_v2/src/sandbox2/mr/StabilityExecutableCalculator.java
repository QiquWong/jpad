package sandbox2.mr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.ACPerformanceCalculator.ACPerformanceCalculatorBuilder;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
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

		public void calculateDownwashGradientNonLinearDelft( 
				Double horizontalAdimensionalDistanceNOANGLE,
				Double verticalAdimensionalDistanceNOANGLE,
				List<Amount<Angle>> alphaBodyArray,
				Amount<Angle> alphaZeroLiftWing,
				Amount<Angle> angleOfIncidenceWing,
				List<Amount<?>> clAlphaWing,
				double downwashGradientConstant
				){

			// define lists and arrays
			List<Amount<Angle>> downwashAngleTemp = new ArrayList<>();
			List<Double> downwashGradientTemp = new ArrayList<>();
			List<Double> horizontalLengthTemp = new ArrayList<>();
			List<Double> verticalLengthTemp = new ArrayList<>();
			Double horizontalDistanceOld;
			Double verticalDistanceOld;
			
			// define new alpha body vector
			double [] alphaBodyTemp = MyArrayUtils.linspace(
					alphaZeroLiftWing.doubleValue(NonSI.DEGREE_ANGLE) - angleOfIncidenceWing.doubleValue(NonSI.DEGREE_ANGLE),
					alphaBodyArray.get(alphaBodyArray.size()-1).doubleValue(NonSI.DEGREE_ANGLE),
					50);
			double deltaAlphaRad = alphaBodyTemp[1] - alphaBodyTemp[0];
			
			List<Amount<Angle>> alphaBodyTempList = new ArrayList<>();
			for (int i=0; i<alphaBodyArray.size(); i++){
				alphaBodyTempList.add(i,Amount.valueOf(alphaBodyTemp[i], NonSI.DEGREE_ANGLE));
			}
			
			//first value
			downwashAngleTemp.add(0, Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
			downwashGradientTemp.add(0, downwashGradientConstant);
			horizontalLengthTemp.add(0,horizontalAdimensionalDistanceNOANGLE * 
					Math.cos(angleOfIncidenceWing.doubleValue(SI.RADIAN)-
							alphaZeroLiftWing.doubleValue(SI.RADIAN)));
			verticalLengthTemp.add(0,horizontalAdimensionalDistanceNOANGLE * 
					Math.cos(angleOfIncidenceWing.doubleValue(SI.RADIAN)-
							alphaZeroLiftWing.doubleValue(SI.RADIAN)));
			
			//other values
			horizontalDistanceOld = horizontalAdimensionalDistanceNOANGLE * Math.cos(
					angleOfIncidenceWing.doubleValue(SI.RADIAN)-
					alphaZeroLiftWing.doubleValue(SI.RADIAN) + deltaAlphaRad - downwashAngleTemp.get(i-1).doubleValue(SI.RADIAN));
			
			
			zDistanceArray[0] = zDistanceZero;
			xDistanceArray[0] = xDistanceZero;
			downwashGradientArray[0] = calculateDownwashGradientConstantDelft(zDistanceArray[0], xDistanceArray[0], cLAlphaArray[0]);
			alphaBodyArray[0] = alphaAbsoluteArray[0]- Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
			
		
			for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
				
				epsilonTemp = downwashGradientArray[i-1] * alphaAbsoluteArray[i]; // epsilon in deg, using gradient at previous step
				epsilonTempRad = Amount.valueOf(
						Math.toRadians(epsilonTemp), SI.RADIAN).getEstimatedValue();
				
				zTemp = dValue * Math.sin(angle - i * deltaAlpha + epsilonTempRad);
				xTemp = (dValue * Math.cos(angle - i*deltaAlpha + epsilonTempRad)) + 
						( (0.74) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + epsilonTempRad)));
				
				downwashGradientArrayTemp = calculateDownwashGradientConstantDelft(zTemp, xTemp, cLAlphaArray[i]);
				
//				downwashArray[i] = downwashGradientArrayTemp * alphaAbsoluteArray[i]; 
				downwashArray[i] = downwashArray[i-1] + downwashGradientArrayTemp * deltaAlpha*57.3; 
				downwashRad = Amount.valueOf(
						Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
				
				zDistanceArray[i] = dValue * Math.sin(angle - i * deltaAlpha + downwashRad);
				xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
						( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
				
				downwashGradientArray[i] = calculateDownwashGradientConstantDelft(zDistanceArray[i], xDistanceArray[i], cLAlphaArray[i]);
//				downwashArray[i] = downwashGradientArray[i] * alphaAbsoluteArray[i]; 
				downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
				downwashRad = Amount.valueOf(
						Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
				
				zDistanceArray[i] = dValue * Math.sin(angle - i * deltaAlpha + downwashRad);
				xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
						( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
				
				downwashGradientArray[i] = calculateDownwashGradientConstantDelft(zDistanceArray[i], xDistanceArray[i], cLAlphaArray[i]);
//				downwashArray[i] = downwashGradientArray[i] * alphaAbsoluteArray[i]; 
				downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
				
				alphaBodyArray[i] = alphaAbsoluteArray[i] - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
				
				
				
				
				// fill the downwash gradient array 
				
				List<Double> downwashGradient = MyArrayUtils.convertListOfDoubleToDoubleArray(
						MyMathUtils.getInterpolatedValue1DLinear(
								alphaBodyTemp, 
								downwashGradientTemp,
								MyArrayUtils.convertListOfAmountTodoubleArray(alphaBodyArray)));
		}
		
	public void calculateDownwashAngleNonLinearDelft(){}
	
	public double calculateDownwashGradientSlingerland(
			Double rHorizontalDistance,
			Double mVerticalDistance,
			Double clAlphaRad,
			Amount<Angle> sweepQuarterChord,
			double aspectRatio){
		
		double keGamma, keGammaZero;

		double rPow=Math.pow(rHorizontalDistance,2);
		double mpow=Math.pow(mVerticalDistance, 2);

		keGamma=(0.1124+0.1265*sweepQuarterChord.doubleValue(SI.RADIAN)+0.1766*Math.pow(sweepQuarterChord.doubleValue(SI.RADIAN),2))
				/rPow+0.1024/rHorizontalDistance+2;
		keGammaZero=0.1124/rPow+0.1024/rHorizontalDistance+2;

		double kFraction=keGamma/keGammaZero;
		double first= (rHorizontalDistance/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));

		double downwashGradientLinearatZ=kFraction*(first+second*third)*((clAlphaRad/(Math.PI*aspectRatio)));

		return downwashGradientLinearatZ;

	}
	 
 }