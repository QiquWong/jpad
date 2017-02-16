package calculators.aerodynamics;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class AirfoilCalc {

// Methods useful for case:  INPUT values---> curve
/**
 * This static method evaluates the lift curve of an airfoil on a given array of angle of attack
 * 
 * @author Manuela Ruocco
 */
	
	public static void calculateClCurve(
			double cL0,
			double cLmax,
			Amount<Angle> alphaStar,
			Amount<Angle> alphaStall,
			Amount<?> cLAlpha,
			List<Amount<Angle>> alphaArray,
			AirfoilCreator theAirfoilCreator
			) {

		Double[] cLArray = new Double[alphaArray.size()];

		// cubic interpolation for non linear trait
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		double e = 0.0;

		double cLStar = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				* alphaStar.doubleValue(NonSI.DEGREE_ANGLE))
				+ cL0;
		
		for(int i=0; i<alphaArray.size(); i++) {
			if(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) <= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)) {
				cLArray[i] = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
						* alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						+ cL0;
			}
			else {
				double[][] matrixData = { 
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 4),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{4* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
								3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 4),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
									alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									1.0},
						{4* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
										3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
										2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0,
										0.0},
						{12* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
											6*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
											2.0,
											0.0,
											0.0},
									};

				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				
				double [] vector = {
						cLmax,
						0,
						cLStar,
						cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
						0
						};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];
				e = solSystem[4];

				cLArray[i] = a * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 4) + 
						b * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 3) + 
						c * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 2) +
						d * alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
						e;
			}
		}
		theAirfoilCreator.setClCurve(MyArrayUtils.convertDoubleArrayToListDouble(cLArray));
	}
	
/**
 * Evaluate Cd using a parabolic polar curve
 * 
 * @author Manuela Ruocco
 */
	
	public static void calculateCdvsClCurve(
			double cdMin,
			double clAtCdMin,
			double kFctorDragPolar,
			Double [] clCurveAirfoil,
			AirfoilCreator theAirfoilCreator
			) {
		
		Double [] cdCurve = null;
		
		for (int i=0; i<clCurveAirfoil.length; i++){
			cdCurve[i] = cdMin + Math.pow(( clCurveAirfoil[i] - clAtCdMin), 2)*kFctorDragPolar;
		}
		theAirfoilCreator.setCdCurve(MyArrayUtils.convertDoubleArrayToListDouble(cdCurve));
	}
	
	/**
	 * Evaluate Cm using a linear curve until the end of linearity angle, and a parabolic interpolation until the stall
	 * 
	 * @author Manuela Ruocco
	 */
		
		public static void calculateCmvsAlphaCurve(
				double cmAC,
				double cmAlpha,
				double cmACStall,
				Amount<Angle> alphaStar,
				Amount<Angle> alphaStall,
				List<Amount<Angle>> alphaArray,
				AirfoilCreator theAirfoilCreator
				) {
			
			Double [] cmCurve = null;
			
			// parabolic interpolation for non linear trait
			double a = 0.0;
			double b = 0.0;
			double c = 0.0;
			
			// last linear value
			double cmMaxLinear = cmAlpha *alphaStar.doubleValue(NonSI.DEGREE_ANGLE) + cmAC;
			
			for (int i=0; i<alphaArray.size(); i++){
				if(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE)<= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)){
				cmCurve[i] = cmAlpha *alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) + cmAC;
			}
				else{
					double[][] matrixData = { 
							{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
								alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
								1.0},
							{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
									alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
									1.0},
							{2* alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0,
										0.0},
					};

					RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
					double [] vector = {
							cmMaxLinear,
							cmACStall,
							cmAlpha,
					};

					double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

					a = solSystem[0];
					b = solSystem[1];
					c = solSystem[2];

					cmCurve[i] = a * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 2) + 
							b * alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
							c;
				}				
			}		
			theAirfoilCreator.setCmCurve(MyArrayUtils.convertDoubleArrayToListDouble(cmCurve));
		}	
	
		
// Methods useful for case:  INPUT curve---> values
		//LIFT
		//-------------------------------------------------------
		
	public static void extractLiftCharacteristicsfromCurve(
			Double[] clLiftCurve,
			List<Amount<Angle>> alphaArrayforClCurve,
			AirfoilCreator theAirfoilCreator
			){
		
		double clAlpha, clZero, clStar, clMax;
		Amount<Angle> alphaZeroLift, alphaStar, alphaStall;
		
		// cl alpha
		
		clAlpha = ((clLiftCurve[1] - clLiftCurve[0])/
				  (alphaArrayforClCurve.get(1).doubleValue(NonSI.DEGREE_ANGLE)- alphaArrayforClCurve.get(0).doubleValue(NonSI.DEGREE_ANGLE)) + 
				  (clLiftCurve[2] - clLiftCurve[1])/
				  (alphaArrayforClCurve.get(2).doubleValue(NonSI.DEGREE_ANGLE) - alphaArrayforClCurve.get(1).doubleValue(NonSI.DEGREE_ANGLE)))/2;
		
		theAirfoilCreator.setClAlphaLinearTrait(Amount.valueOf(
				clAlpha,
				NonSI.DEGREE_ANGLE.inverse()));
		
		// cl zero
		
		clZero = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaArrayforClCurve),
				MyArrayUtils.convertToDoublePrimitive(clLiftCurve),
				0.0
				);
		
		theAirfoilCreator.setClAtAlphaZero(clZero);
		
		// alpha zero lift
		
		alphaZeroLift = Amount.valueOf(-clZero/clAlpha, 
				NonSI.DEGREE_ANGLE
				);
		
		theAirfoilCreator.setAlphaZeroLift(alphaZeroLift);
		
		// cl max
		
		clMax = MyArrayUtils.getMax(clLiftCurve);
		int indexOfMax = MyArrayUtils.getIndexOfMax(clLiftCurve);
		
		theAirfoilCreator.setClMax(clMax);
		
		// alpha stall
		
		alphaStall = alphaArrayforClCurve.get(indexOfMax);
		
		theAirfoilCreator.setAlphaStall(alphaStall);
		
		// cl star 
		
		int j=0;
		double clLinear = clAlpha*alphaArrayforClCurve.get(j).doubleValue(NonSI.DEGREE_ANGLE)+clZero;
		
		while (Math.abs(clLiftCurve[j]-clLinear) < 0.001) {
			j++;
			clLinear = clAlpha*alphaArrayforClCurve.get(j).doubleValue(NonSI.DEGREE_ANGLE)+clZero;	
		}
		
		clStar = clLiftCurve[j];
		
		theAirfoilCreator.setClEndLinearTrait(clStar);
		
		// alpha star
		
		alphaStar = alphaArrayforClCurve.get(j);
		
		theAirfoilCreator.setAlphaLinearTrait(alphaStar);
	
		
	}	
		
}
