package sandbox.mr.ExecutableWing;

import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.NasaBlackwell;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class WingAerodynamicCalc {

	public static void calculateAll(InputOutputTree input){
		
	
		// distribution
		
		int numberOfPoint = input.getNumberOfPointSemispan();
		double [] yStationInput = new double [input.getNumberOfSections()];
		
		for (int i=0; i<yStationInput.length -1; i++){
			yStationInput [i] = input.getyAdimensionalStationInput().get(i);
					}
		
		double [] yStationActual = new double [numberOfPoint];
		yStationActual = MyArrayUtils.linspace(0, 1, numberOfPoint);

		
		//input
		
		double [] chordInput = new double [input.getNumberOfSections()];
		double [] xleInput = new double [input.getNumberOfSections()];
		double [] dihedralInput = new double [input.getNumberOfSections()];
		double [] twistInput = new double [input.getNumberOfSections()];
		double [] alpha0lInput = new double [input.getNumberOfSections()];
		
		
		for (int i =0; i<input.getNumberOfSections()-1; i++){
			chordInput[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInput[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			dihedralInput[i] = input.getDihedralDistribution().get(i).getEstimatedValue();
			twistInput[i] = input.getTwistDistribution().get(i).getEstimatedValue();
			alpha0lInput[i] = input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue();	
		}
		
		
		//output
		
		double [] chordsVsYActual = new double [numberOfPoint];
		double [] xLEvsYActual = new double [numberOfPoint];
		double [] dihedral = new double [numberOfPoint];
		double [] twist = new double [numberOfPoint];
		double [] alpha0l = new double [numberOfPoint];
		
		Double [] chordsVsYActualDouble = MyMathUtils.getInterpolatedValue1DLinear(yStationInput,chordInput , yStationActual);
		Double [] xLEvsYActualDouble = MyMathUtils.getInterpolatedValue1DLinear(yStationInput,xleInput , yStationActual);
		Double [] dihedralDouble = MyMathUtils.getInterpolatedValue1DLinear(yStationInput,dihedralInput , yStationActual);
		Double [] twistDouble = MyMathUtils.getInterpolatedValue1DLinear(yStationInput,twistInput , yStationActual);
		Double [] alpha0lDouble = MyMathUtils.getInterpolatedValue1DLinear(yStationInput,alpha0lInput , yStationActual);
		
		for (int i=0; i<yStationActual.length-1; i++){
			
			chordsVsYActual[i] = chordsVsYActualDouble[i];
			xLEvsYActual[i] = xLEvsYActualDouble[i];
			dihedral[i] = dihedralDouble[i];
			twist[i] = twistDouble[i];
			alpha0l[i] = alpha0lDouble[i];
			
			
		}
		
		
		// other
		
		double vortexSemiSpanToSemiSpanRatio = 1/(2*input.getNumberOfPointSemispan());
		
		// alpha zero lift
		
		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
				input.getSemiSpan().getEstimatedValue(), 
				input.getSurface().getEstimatedValue(),
				yStationActual,
				chordsVsYActual,
				xLEvsYActual,
				dihedral,
				twist,
				alpha0l,
				vortexSemiSpanToSemiSpanRatio,
				input.getAlphaCurrent().getEstimatedValue(),
				input.getMachNumber(),
				input.getAltitude().getEstimatedValue());
		
//	theNasaBlackwellCalculator.calculate(Amount.valueOf()));
	}
}
