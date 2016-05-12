package sandbox.mr.ExecutableWing;

import java.util.Arrays;

import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.NasaBlackwell;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

public class WingAerodynamicCalc {

	public static void calculateAll(InputOutputTree input){
		
	
		// distribution
		
		int numberOfPoint = input.getNumberOfPointSemispan();
		double [] yStationInput = new double [input.getNumberOfSections()];
		
		for (int i=0; i<yStationInput.length; i++){
			yStationInput [i] = input.getyAdimensionalStationInput().get(i);
					}
		
		double [] yStationActual = new double [numberOfPoint];
		yStationActual = MyArrayUtils.linspace(0, 1, numberOfPoint);

		
		//input
		
		MyArray chordInput = new MyArray(input.getNumberOfSections());
		MyArray xleInput = new MyArray(input.getNumberOfSections());
		MyArray dihedralInput = new MyArray(input.getNumberOfSections());
		MyArray twistInput = new MyArray(input.getNumberOfSections());
		MyArray alpha0lInput = new MyArray(input.getNumberOfSections());
		
		double [] chordInputDouble = new double [input.getNumberOfSections()];
		double [] xleInputDouble = new double [input.getNumberOfSections()];
		double [] dihedralInputDouble = new double [input.getNumberOfSections()];
		double [] twistInputDouble = new double [input.getNumberOfSections()];
		double [] alpha0lInputDouble = new double [input.getNumberOfSections()];
		
		for (int i =0; i<input.getNumberOfSections(); i++){
			chordInputDouble[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInputDouble[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			dihedralInputDouble[i] = input.getDihedralDistribution().get(i).getEstimatedValue();
			twistInputDouble[i] = input.getTwistDistribution().get(i).getEstimatedValue();
			alpha0lInputDouble[i] = input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue();	
		}
		
		chordInput.createArray(chordInputDouble);
		xleInput.createArray(xleInputDouble);
		dihedralInput.createArray(dihedralInputDouble);
		twistInput.createArray(twistInputDouble);
		alpha0lInput.createArray(alpha0lInputDouble);
		
		//output
		
		MyArray chordsVsYActual = new MyArray(numberOfPoint);
		MyArray xLEvsYActual = new MyArray(numberOfPoint);
		MyArray dihedralActual = new MyArray(numberOfPoint);
		MyArray twistActual = new MyArray(numberOfPoint);
		MyArray alpha0lActual = new MyArray(numberOfPoint);
		
		
		xLEvsYActual = MyArray.createArray(
				xleInput.interpolate(
						yStationInput,
						yStationActual));
		
		chordsVsYActual = MyArray.createArray(
				chordInput.interpolate(
						yStationInput,
						yStationActual));
		
		dihedralActual = MyArray.createArray(
				dihedralInput.interpolate(
						yStationInput,
						yStationActual));
		
		twistActual = MyArray.createArray(
				twistInput.interpolate(
						yStationInput,
						yStationActual));
		
		
		alpha0lActual = MyArray.createArray(
				alpha0lInput.interpolate(
						yStationInput,
						yStationActual));
		
System.out.println("\n---------ACTUAL PARAMETER DISTRIBUTION------------");	
System.out.println("Chord distribution (m) = " + chordsVsYActual.toString());
System.out.println("x le distribution (m) = " + xLEvsYActual.toString());
System.out.println("dihedral distribution (°) = " + dihedralActual.toString());
System.out.println("twist distribution (°) = " + twistActual.toString());
System.out.println("alpha zero lift distribution (°) = " + alpha0lActual.toString());

		// other
		
		double vortexSemiSpanToSemiSpanRatio = 1/(2*input.getNumberOfPointSemispan());
		
		// alpha zero lift and cl alpha
		
//		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
//				input.getSemiSpan().getEstimatedValue(), 
//				input.getSurface().getEstimatedValue(),
//				yStationActual,
//				chordsVsYActual,
//				xLEvsYActual,
//				dihedral,
//				twist,
//				alpha0l,
//				vortexSemiSpanToSemiSpanRatio,
//				input.getAlphaCurrent().getEstimatedValue(),
//				input.getMachNumber(),
//				input.getAltitude().getEstimatedValue());
//		
////	theNasaBlackwellCalculator.calculate(Amount.valueOf()));
	}
}
