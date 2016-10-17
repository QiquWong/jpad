package sandbox.mr.ExecutableTestModifiedNasaBlackwell;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.NasaBlackwell;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Calculator {

	//	public static void calculateAll(InputOutputTree input, int numberOfAirfoil)
	//	{
	//		calculateClvsAlphaAirfoil(input, numberOfAirfoil);
	//		
	//	}
	public static  void calculateClvsAlphaAirfoil (InputOutputTree input, int numberOfAirfoil ){

		int nValue = input.getNumberOfPoint2DCurve();
		double [] clAirfoil = new double [nValue];

		double alpha;

		// set value 

		double clStar, clAlpha, alphaStar, alphaStall, alphaZeroLift, clMax, clZero;

		alphaZeroLift = input.getAlphaZeroLiftDistribution().get(numberOfAirfoil).getEstimatedValue();
		alphaStar = input.getAlphaStarDistribution().get(numberOfAirfoil).getEstimatedValue();
		alphaStall = input.getAlphaStallDistribution().get(numberOfAirfoil).getEstimatedValue();
		clAlpha = input.getClAlphaDistribution().get(numberOfAirfoil);
		clMax = input.getMaximumliftCoefficientDistribution().get(numberOfAirfoil);
		clZero = -clAlpha*alphaZeroLift;
		clStar = clAlpha*alphaStar + clZero;

		// set alpha array

		// built of curve
		for (int i=0; i<nValue; i++){
			alpha = input.getAlphaArrayCompleteCurveAirfoil()[i];	
			if ( alpha < alphaStar ) {
				clAirfoil[i] = clAlpha*alpha+clZero;
			}
			else {
				double[][] matrixData = { {Math.pow(alphaStall, 3),
					Math.pow(alphaStall, 2), alphaStall,1.0},
						{3* Math.pow(alphaStall, 2), 2*alphaStall, 1.0, 0.0},
						{3* Math.pow(alphaStar, 2), 2*alphaStar, 1.0, 0.0},
						{Math.pow(alphaStar, 3), Math.pow(alphaStar, 2),
							alphaStar,1.0}};
				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {clMax, 0,clAlpha, clStar};
				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);
				double a = solSystem[0];
				double b = solSystem[1];
				double c = solSystem[2];
				double d = solSystem[3];

				clAirfoil[i] = a * Math.pow(alpha,3) + b * Math.pow(alpha, 2) + c * alpha +d; }
		}

		input.getClArrayCompleteCurveAirfoil().add(numberOfAirfoil, clAirfoil);

	}

	public static  void calculateModifiedStallPath(InputOutputTree input ){

		// DATA
		
		double alphaInitial = -4.0;
		double alphaFinal = 30.0;
		int numberOfAlpha = 35;
		
		double vortexSemiSpanToSemiSpanRatio = 1.0/(2.0*input.getNumberOfPointSemispan());

		double[] alphaArrayNasaBlackwell = MyArrayUtils.linspace(alphaInitial, alphaFinal, numberOfAlpha);
		double[] clDistributionActualNasaBlackwell = new double[input.getNumberOfPointSemispan()];
		double[] clDistributionModifiedFromAirfoil = new double[input.getNumberOfPointSemispan()];
		
		boolean firstIntersectionFound = false;
		
		int indexOfFirstIntersection = 0;
		int indexOfAlphaFirstIntersection = 0;
		double diffCLapp = 0;
		double diffCLappOld = 0;
		double diffCL = 0;
		double accuracy =0.0001;
		double deltaAlpha = 0.0;
		Amount<Angle> alphaNew = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		List<Double> cL3D = new ArrayList<Double>();

		// Object definition
		
		System.out.println("\n\n NASA BLACKWELL");
		System.out.println("semi span " + input.getSemiSpan().getEstimatedValue());
		System.out.println("surface " + input.getSurface());
		System.out.println("y station " + Arrays.toString(input.getyStationsAdimensional()));
		System.out.println("chord " + Arrays.toString(input.getChordCompleteDistribution()));
		System.out.println("xle distribution " + Arrays.toString(input.getxLECompleteDistribution()));
		System.out.println("dihedral " + input.getDihedralDistribution().toString());
		System.out.println("twist " + Arrays.toString(input.getTwistDistributionRadian()));
		System.out.println("alpha zero lift " + Arrays.toString(input.getAlphaZeroLiftDistributionRadian()));
		System.out.println("vortex " + vortexSemiSpanToSemiSpanRatio);
		System.out.println("Mach " + input.getMachNumber());
		System.out.println("Altitude " + input.getAltitude().doubleValue(SI.METER));
		
		NasaBlackwell theNasaBlackwellCalculator = new NasaBlackwell(
				input.getSemiSpan().doubleValue(SI.METER),
				input.getSurface().doubleValue(SI.SQUARE_METRE),
			    input.getyStationsAdimensional(),
				MyArrayUtils.convertToDoublePrimitive(input.getChordCompleteDistribution()),
				MyArrayUtils.convertToDoublePrimitive(input.getxLECompleteDistribution()),
				MyArrayUtils.convertListOfAmountodoubleArray(input.getDihedralDistribution()),
				MyArrayUtils.convertToDoublePrimitive(input.getTwistDistributionRadian()),
				MyArrayUtils.convertToDoublePrimitive(input.getAlphaZeroLiftDistributionRadian()),
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				input.getMachNumber(),
				input.getAltitude().doubleValue(SI.METER)
				);

		// ITERATIVE PROCESS
			
			//Find the intersection 
		
		System.out.println(" eta " + Arrays.toString(input.getyStationsAdimensional()));
			for (int i=0; i < alphaArrayNasaBlackwell.length; i++) {
				if(firstIntersectionFound == false) {
					theNasaBlackwellCalculator.calculate(
							Amount.valueOf(
									alphaArrayNasaBlackwell[i],
									NonSI.DEGREE_ANGLE)
							);
					clDistributionActualNasaBlackwell = 
							theNasaBlackwellCalculator
							.get_clTotalDistribution()
							.toArray();
					System.out.println("\nalpha " + alphaArrayNasaBlackwell[i]);
					System.out.println(" cl Nasa Blackwell " + Arrays.toString(clDistributionActualNasaBlackwell));
			
					for (int ii=0; ii<input.getNumberOfPointSemispan(); ii++){
					clDistributionModifiedFromAirfoil[ii] = calculateActualViscousClAirfoil(
							input,
							clDistributionActualNasaBlackwell[ii],
							input.getyStationsAdimensional()[ii],
							ii);
					
					}
					System.out.println(" cl correct " + Arrays.toString(clDistributionModifiedFromAirfoil));
					double clIntegral = MyMathUtils.integrate1DTrapezoidLinear(
							input.getyStationsAdimensional(),
							clDistributionModifiedFromAirfoil,
							0,
							1);
					cL3D.add(clIntegral);

					for(int j =0; j < input.getNumberOfPointSemispan(); j++) {
//					if( clDistributionActualNasaBlackwell[j] > input.getCompleteAirfoilClMaxDistribution()[j]) {					
					if( Math.abs((clDistributionModifiedFromAirfoil[j] - input.getCompleteAirfoilClMaxDistribution()[j]))<0.000001) {
							firstIntersectionFound = true;
							indexOfFirstIntersection = j;
							System.out.println(" index of first intersection " + indexOfFirstIntersection);
							break;
						}
					}
				}
				else {
					indexOfAlphaFirstIntersection = i;
					System.out.println(" index of alpha first intersection " + indexOfAlphaFirstIntersection);
					break;
				}
			}
		
System.out.println(" j index " + indexOfFirstIntersection + " this value must be diff from 50");
System.out.println(" i index " + indexOfAlphaFirstIntersection + " this value must be diff from 31");

System.out.println("cl max airfoil " + Arrays.toString(input.getCompleteAirfoilClMaxDistribution()));
System.out.println(" cl 3d " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(cL3D)));

		// After find the first point where CL_wing > Cl_MAX_airfoil, starts an iteration on alpha
		// in order to improve the accuracy.
//
//		for (int k = indexOfFirstIntersection; k< input.getNumberOfPointSemispan(); k++) {
//			diffCLapp = ( clDistributionActualNasaBlackwell[k] -  input.getMaximumliftCoefficientDistribution().get(k));
//			diffCL = Math.max(diffCLapp, diffCLappOld);
//			diffCLappOld = diffCL;
//		}
//		if( Math.abs(diffCL) < accuracy){
//			_cLMax.put(MethodEnum.NASA_BLACKWELL, theNasaBlackwellCalculator.getCLCurrent());
//			_alphaMaxLinear.put(
//					MethodEnum.NASA_BLACKWELL,
//					Amount.valueOf(
//							theNasaBlackwellCalculator.getAlphaCurrent(),
//							NonSI.DEGREE_ANGLE)
//					); 
//		}
//		else{
//			deltaAlpha = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] 
//					- alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection-1];
//			alphaNew = Amount.valueOf(
//					(alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection] - (deltaAlpha/2)),
//					NonSI.DEGREE_ANGLE
//					).to(SI.RADIAN);
//			double alphaOld = alphaArrayNasaBlackwell[indexOfAlphaFirstIntersection]; 
//			diffCLappOld = 0;
//			while ( diffCL > accuracy){
//				diffCL = 0;
//				theNasaBlackwellCalculator.calculate(alphaNew);
//				clDistributionActualNasaBlackwell = theNasaBlackwellCalculator
//						.get_clTotalDistribution()
//						.toArray();
//				for (int m =0; m< input.getNumberOfPointSemispan(); m++) {
//					diffCLapp = clDistributionActualNasaBlackwell[m] - input.getMaximumliftCoefficientDistribution().get(m);
//
//					if ( diffCLapp > 0 ){
//						diffCL = Math.max(diffCLapp,diffCLappOld);
//						diffCLappOld = diffCL;
//					}
//
//				}
//				deltaAlpha = Math.abs(alphaOld - alphaNew.doubleValue(NonSI.DEGREE_ANGLE));
//				alphaOld = alphaNew.doubleValue(NonSI.DEGREE_ANGLE);
//				if (diffCL == 0){ //this means that diffCL would have been negative
//					alphaNew = Amount.valueOf(
//							alphaOld + (deltaAlpha/2),
//							NonSI.DEGREE_ANGLE
//							);
//					diffCL = 1; // generic positive value in order to enter again in the while cycle 
//					diffCLappOld = 0;
//				}
//				else { 
//					if(deltaAlpha > 0.005){
//						alphaNew = Amount.valueOf(
//								alphaOld - (deltaAlpha/2),
//								NonSI.DEGREE_ANGLE
//								);	
//						diffCLappOld = 0;
//						if ( diffCL < accuracy) break;
//					}
//					else {
//						alphaNew = Amount.valueOf(
//								alphaOld - (deltaAlpha),
//								NonSI.DEGREE_ANGLE
//								);	
//						diffCLappOld = 0;
//						if ( diffCL < accuracy) 
//							break;
//					}
//				}
//			}
//			theNasaBlackwellCalculator.calculate(alphaNew.to(SI.RADIAN));
//			_liftCoefficientDistributionAtCLMax.put(
//					MethodEnum.NASA_BLACKWELL,
//					theNasaBlackwellCalculator.get_clTotalDistribution().toArray()
//					);
//			_cLMax.put(MethodEnum.NASA_BLACKWELL, theNasaBlackwellCalculator.getCLCurrent())	;
//			_alphaMaxLinear.put(MethodEnum.NASA_BLACKWELL, alphaNew);
//		}
	}
	
	
	public static  double calculateActualViscousClAirfoil(InputOutputTree input, double clInviscidLocal, double adimentionalStation, int station){
		
		double alphaLocalActual;
		double clZeroLocal = MyMathUtils.getInterpolatedValue1DLinear(
				input.getyStationsAdimensional(),
				MyArrayUtils.convertToDoublePrimitive(input.getCompleteAirfoilClZeroDistribution()), 
				adimentionalStation
				);
//		System.out.println(" cl zero " + clZeroLocal);
		
		double clAlphaLocal = MyMathUtils.getInterpolatedValue1DLinear(
				input.getyStationsAdimensional(),
				MyArrayUtils.convertToDoublePrimitive(input.getCompleteAirfoilClAlphaDistribution()), 
				adimentionalStation
				);
		
//		System.out.println(" cl alpha"  + clAlphaLocal);
		
		// First of all it is necessary to calculate the angle of attack at cl
		
		alphaLocalActual = (clInviscidLocal - clZeroLocal)/clAlphaLocal;
		
//		System.out.println(" angle of attack " + alphaLocalActual);
		
		double [] clVector = new double [input.getNumberOfPoint2DCurve()];
		for (int i=0 ; i< input.getNumberOfPoint2DCurve(); i++){
		clVector[i] = input.getClAirfoilMatrix()[i][station];
		}
		double clViscousActual = MyMathUtils.getInterpolatedValue1DLinear(
				input.getAlphaArrayCompleteCurveAirfoil(),
				clVector,
				alphaLocalActual
				);
		
//		System.out.println("cl actual " + clViscousActual);
		return clViscousActual;
	}
	
}

