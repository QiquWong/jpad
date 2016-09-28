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
		double alphaMin = alphaZeroLift - 3;
		double alphaMax = alphaStall + 4;
		MyArray alphaArray = new MyArray();
		alphaArray.linspace(alphaMin, alphaMax, nValue);
		input.getAlphaArrayCompleteCurveAirfoil().add(numberOfAirfoil, alphaArray.toArray());
		
		// built of curve
		for (int i=0; i<nValue; i++){
		alpha = alphaArray.get(i);	
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
	
	public static  void calculateStallPath(InputOutputTree input, int numberOfAirfoil ){
		
		// DATA
		 int numberOfAlpha = 31;
		 double alphaInitial = -2.0;
		 double alphaFinal = 28.0;
		 
		 double [] alphaStallPathArray = new double[numberOfAlpha];
		 
		 alphaStallPathArray = MyArrayUtils.linspace(alphaInitial, alphaFinal, numberOfAlpha);
		 
		 
		 
		
		// output values
		// These values must to fill the field in output tree
		

		
	}
	}
	
		