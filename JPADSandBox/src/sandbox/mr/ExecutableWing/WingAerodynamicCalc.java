package sandbox.mr.ExecutableWing;

import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jboss.netty.util.internal.SystemPropertyUtil;
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
		double [] yStationDimensional = new double [numberOfPoint];
		yStationActual = MyArrayUtils.linspace(0, 1, numberOfPoint);
		yStationDimensional = MyArrayUtils.linspace(0, input.getSemiSpan().getEstimatedValue(), numberOfPoint);

		//input

		MyArray chordInput = new MyArray(input.getNumberOfSections());
		MyArray xleInput = new MyArray(input.getNumberOfSections());
		MyArray dihedralInput = new MyArray(input.getNumberOfSections());
		MyArray twistInput = new MyArray(input.getNumberOfSections());
		MyArray alphaStarInput = new MyArray(input.getNumberOfSections());
		MyArray alpha0lInput = new MyArray(input.getNumberOfSections());

		double [] chordInputDouble = new double [input.getNumberOfSections()];
		double [] xleInputDouble = new double [input.getNumberOfSections()];
		double [] dihedralInputDouble = new double [input.getNumberOfSections()];
		double [] twistInputDouble = new double [input.getNumberOfSections()];
		double [] alphaStarInputDouble = new double [input.getNumberOfSections()];
		double [] alpha0lInputDouble = new double [input.getNumberOfSections()];

		for (int i =0; i<input.getNumberOfSections(); i++){
			chordInputDouble[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInputDouble[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			dihedralInputDouble[i] = Math.toRadians(input.getDihedralDistribution().get(i).getEstimatedValue());
			twistInputDouble[i] = Math.toRadians(input.getTwistDistribution().get(i).getEstimatedValue());
			alphaStarInputDouble[i] = Math.toRadians(input.getAlphaStarDistribution().get(i).getEstimatedValue());
			alpha0lInputDouble[i] = Math.toRadians(input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue());	
		}

		chordInput = new MyArray(chordInputDouble);
		xleInput = new MyArray(xleInputDouble);
		dihedralInput = new MyArray(dihedralInputDouble);
		twistInput = new MyArray(twistInputDouble);
		alphaStarInput = new MyArray(alphaStarInputDouble);
		alpha0lInput = new MyArray(alpha0lInputDouble);

		//output

		MyArray chordsVsYActual = new MyArray(numberOfPoint);
		MyArray xLEvsYActual = new MyArray(numberOfPoint);
		MyArray dihedralActual = new MyArray(numberOfPoint);
		MyArray twistActual = new MyArray(numberOfPoint);
		MyArray alphaStarActual = new MyArray(numberOfPoint);
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

		alphaStarActual = MyArray.createArray(
				alphaStarInput.interpolate(
						yStationInput,
						yStationActual));
		
		alpha0lActual = MyArray.createArray(
				alpha0lInput.interpolate(
						yStationInput,
						yStationActual));

		System.out.println("\n---------ACTUAL PARAMETER DISTRIBUTION------------");	
		System.out.println("--------------------------------------------------");
		System.out.println("Y Stations  " + Arrays.toString(yStationActual));
		System.out.println("Y Dimensional Stations  " + Arrays.toString(yStationDimensional));
		System.out.println("Chord distribution (m) = " + chordsVsYActual.toString());
		System.out.println("x le distribution (m) = " + xLEvsYActual.toString());
		System.out.println("dihedral distribution (rad) = " + dihedralActual.toString());
		System.out.println("twist distribution (rad) = " + twistActual.toString());
		System.out.println("alpha star distribution (rad) = " + alphaStarActual.toString());
		System.out.println("alpha zero lift distribution (rad) = " + alpha0lActual.toString());

		
		// other

		double vortexSemiSpanToSemiSpanRatio = (1./(2*input.getNumberOfPointSemispan()));
//		System.out.println(" vortex " + vortexSemiSpanToSemiSpanRatio);
		
		// alpha zero lift and cl alpha
		
		NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
				input.getSemiSpan().getEstimatedValue(), 
				input.getSurface().getEstimatedValue(),
				yStationDimensional,
				chordsVsYActual.toArray(),
				xLEvsYActual.toArray(),
				dihedralActual.toArray(),
				twistActual.toArray(),
				alpha0lActual.toArray(),
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				input.getMachNumber(),
				input.getAltitude().getEstimatedValue());

		Amount<Angle> alphaFirst = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
		Amount<Angle> alphaSecond = Amount.valueOf(Math.toRadians(4.0), SI.RADIAN);

		theNasaBlackwellCalculator.calculate(alphaFirst);
		double [] clDistribution = theNasaBlackwellCalculator.get_clTotalDistribution().toArray();
		double cLFirst = theNasaBlackwellCalculator.get_cLEvaluated();//MyMathUtils.integrate1DTrapezoidLinear(yStationActual, clDistribution, 0, 1);
		System.out.println("\n\n cL alpha 2 " + cLFirst);
		
		theNasaBlackwellCalculator.calculate(alphaSecond);
		double cLSecond = theNasaBlackwellCalculator.get_cLEvaluated();
		System.out.println(" cL alpha 4 " + cLSecond);
		
		double cLAlpha = (cLSecond - cLFirst)/(alphaSecond.getEstimatedValue()-alphaFirst.getEstimatedValue()); // 1/rad
		input.setClAlpha(cLAlpha);
		
		System.out.println(" \n ");
		System.out.println(" cL ALPHA " + cLAlpha);
		
		
		Amount<Angle> alphaZero = Amount.valueOf(0.0, SI.RADIAN);
		
		theNasaBlackwellCalculator.calculate(alphaZero);
		double cLZero = theNasaBlackwellCalculator.get_cLEvaluated();
		System.out.println(" cl zero " + cLZero);
		
		double alphaZeroLift = -(cLZero)/cLAlpha;
		input.setAlphaZeroLift(Amount.valueOf(Math.toDegrees(alphaZeroLift), NonSI.DEGREE_ANGLE));
		System.out.println(" alpha zero lift (deg) " + Math.toDegrees(alphaZeroLift));
		
		
		// alpha Star
		
		double rootChord = chordInput.get(0);
		double kinkChord = MyMathUtils.getInterpolatedValue1DLinear(yStationActual, chordsVsYActual.toArray(),
				input.getAdimensionalKinkStation());
		double tipChord = chordInput.get(chordInput.size()-1);
		
		double alphaStarRoot= alphaStarInput.get(0);
		double alphaStarKink = MyMathUtils.getInterpolatedValue1DLinear(yStationActual, alphaStarActual.toArray(),
				input.getAdimensionalKinkStation());
		double alphaStarTip = alphaStarInput.get(chordInput.size()-1);
	
		double dimensionalKinkStation = input.getAdimensionalKinkStation()*input.getSemiSpan().getEstimatedValue();
		double dimensionalOverKink = input.getSemiSpan().getEstimatedValue() - dimensionalKinkStation;

		double influenceAreaRoot = rootChord * dimensionalKinkStation/2;
		double influenceAreaKink = (kinkChord * dimensionalKinkStation/2) + (kinkChord * dimensionalOverKink/2);
		double influenceAreaTip = tipChord * dimensionalOverKink/2;
		
		double kRoot = 2*influenceAreaRoot/input.getSurface().getEstimatedValue();
		double kKink = 2*influenceAreaKink/input.getSurface().getEstimatedValue();
		double kTip = 2*influenceAreaTip/input.getSurface().getEstimatedValue();

		
		double alphaStar =  alphaStarRoot * kRoot + alphaStarKink * kKink + alphaStarTip * kTip;

		
		System.out.println(" alpha star (deg) " + Math.toDegrees(alphaStar));
		
		Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);

		theNasaBlackwellCalculator.calculate(alphaStarAmount);
		double cLStar = theNasaBlackwellCalculator.get_cLEvaluated();
		System.out.println(" cL star " + cLStar);
		
		// cl Max
		
		
		// alpha stall
		
		
		// curve
		
}
}