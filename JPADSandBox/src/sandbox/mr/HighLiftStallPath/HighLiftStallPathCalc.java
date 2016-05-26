
package sandbox.mr.HighLiftStallPath;

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

public class HighLiftStallPathCalc {


	public static void calculateAll(InputOutputTree input) throws InstantiationException, IllegalAccessException{

		// Define input vector 

		int numberOfPoint = input.getNumberOfPointSemispan();
		double [] yStationInput = new double [input.getNumberOfSections()];
		double [] yStationDimensional = new double [numberOfPoint];

		
		for (int i=0; i<yStationInput.length; i++){
			yStationInput [i] = input.getyAdimensionalStationInput().get(i);
		}

		//input

//		MyArray chordInput = new MyArray(input.getNumberOfSections());
//		MyArray xleInput = new MyArray(input.getNumberOfSections());
//		MyArray dihedralInput = new MyArray(input.getNumberOfSections());
//		MyArray twistInput = new MyArray(input.getNumberOfSections());
//		MyArray alphaStarInput = new MyArray(input.getNumberOfSections());
//		MyArray alpha0lInput = new MyArray(input.getNumberOfSections());
//		MyArray clMaxInput = new MyArray(input.getNumberOfSections());


		double [] chordInput = new double [input.getNumberOfSections()];
		double [] xleInput = new double [input.getNumberOfSections()];
		double [] dihedralInput = new double [input.getNumberOfSections()];
		double [] twistInput = new double [input.getNumberOfSections()];
		double [] alphaStarInput = new double [input.getNumberOfSections()];
		double [] alpha0lInput = new double [input.getNumberOfSections()];
		double [] clMaxInput = new double [input.getNumberOfSections()];


		for (int i =0; i<input.getNumberOfSections(); i++){
			chordInput[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInput[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			dihedralInput[i] = Math.toRadians(input.getDihedralDistribution().get(i).getEstimatedValue());
			twistInput[i] = Math.toRadians(input.getTwistDistribution().get(i).getEstimatedValue());
			alphaStarInput[i] = Math.toRadians(input.getAlphaStarDistribution().get(i).getEstimatedValue());
			alpha0lInput[i] = Math.toRadians(input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue());
			clMaxInput[i] = input.getMaximumliftCoefficientDistribution().get(i);
		}


//		chordInput = new MyArray(chordInputDouble);
//		xleInput = new MyArray(xleInputDouble);
//		dihedralInput = new MyArray(dihedralInputDouble);
//		twistInput = new MyArray(twistInputDouble);
//		alphaStarInput = new MyArray(alphaStarInputDouble);
//		alpha0lInput = new MyArray(alpha0lInputDouble);
//		clMaxInput = new MyArray(clMaxInputDouble);
//		

		
		// Define new exended vectors
		
			// y Stations
		
		
			// chords
		
			// xle
		
			// alpha zero lift
		
			// alpha star
		
			// cl max
		
		//Calculate flapped curve
		
			// alpha0L
		
			// cL alpha new
		
			// alpha star
		
			// FLAPPED STALL PATH
		
	    // Print results
		
	   // plot
	}
}