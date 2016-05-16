package sandbox.mr.ExecutableWing;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jboss.netty.util.internal.SystemPropertyUtil;
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
		MyArray clMaxInput = new MyArray(input.getNumberOfSections());


		double [] chordInputDouble = new double [input.getNumberOfSections()];
		double [] xleInputDouble = new double [input.getNumberOfSections()];
		double [] dihedralInputDouble = new double [input.getNumberOfSections()];
		double [] twistInputDouble = new double [input.getNumberOfSections()];
		double [] alphaStarInputDouble = new double [input.getNumberOfSections()];
		double [] alpha0lInputDouble = new double [input.getNumberOfSections()];
		double [] clMaxInputDouble = new double [input.getNumberOfSections()];


		for (int i =0; i<input.getNumberOfSections(); i++){
			chordInputDouble[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInputDouble[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			dihedralInputDouble[i] = Math.toRadians(input.getDihedralDistribution().get(i).getEstimatedValue());
			twistInputDouble[i] = Math.toRadians(input.getTwistDistribution().get(i).getEstimatedValue());
			alphaStarInputDouble[i] = Math.toRadians(input.getAlphaStarDistribution().get(i).getEstimatedValue());
			alpha0lInputDouble[i] = Math.toRadians(input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue());
			clMaxInputDouble[i] = input.getMaximumliftCoefficientDistribution().get(i);
		}


		chordInput = new MyArray(chordInputDouble);
		xleInput = new MyArray(xleInputDouble);
		dihedralInput = new MyArray(dihedralInputDouble);
		twistInput = new MyArray(twistInputDouble);
		alphaStarInput = new MyArray(alphaStarInputDouble);
		alpha0lInput = new MyArray(alpha0lInputDouble);
		clMaxInput = new MyArray(clMaxInputDouble);

		//output

		MyArray chordsVsYActual = new MyArray(numberOfPoint);
		MyArray xLEvsYActual = new MyArray(numberOfPoint);
		MyArray dihedralActual = new MyArray(numberOfPoint);
		MyArray twistActual = new MyArray(numberOfPoint);
		MyArray alphaStarActual = new MyArray(numberOfPoint);
		MyArray alpha0lActual = new MyArray(numberOfPoint);
		MyArray clMaxActual = new MyArray(numberOfPoint);


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

		clMaxActual = MyArray.createArray(
				clMaxInput.interpolate(
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
		System.out.println("Cl max distribution = " + clMaxActual.toString());


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

		double cLMax = LiftCalc.calculateCLMax(clMaxActual.toArray(),
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

		System.out.println(" cl max " + cLMax);

		// alpha stall
		double alphaStall = ((cLMax-cLZero)/Math.toRadians(cLAlpha) + input.getDeltaAlpha());

		System.out.println(" alpha Stall (deg) = " +  alphaStall);



		// RESULTS

//		System.out.println(" \n-----------CLEAN CONFIGURATION-------------- ");
//		System.out.println(" Alpha max = " + input.getAlphaMaxClean().getEstimatedValue() + " " + input.getAlphaMaxClean().getUnit());
//		System.out.println(" Alpha star = " + input.getAlphaStarClean().getEstimatedValue() + " " + input.getAlphaStarClean().getUnit());
//		System.out.println(" CL max = " + input.getcLmaxClean());
//		System.out.println(" CL star = " + input.getcLstarClean());
//		System.out.println(" CL alpha = " + input.getcLAlphaClean().getEstimatedValue() + " " + input.getcLAlphaClean().getUnit());




		//--------------------------------------------------------------------------------------
		// BUILDING CLEAN CURVE:
		//--------------------------------------------------------------------------------------
		double alphaCleanFirst = -10.0;
		Amount<Angle> alphaActual;

		int nPoints = 40;

		Amount<Angle> alphaStarClean = alphaStarAmount.to(NonSI.DEGREE_ANGLE); //deg
		double cLStarClean = cLStar;
		double cLalphaClean = Math.toRadians(cLAlpha); // 1/deg
		double cL0Clean = cLZero;

		double cLMaxClean = cLMax;
		Amount<Angle> alphaMaxClean = Amount.valueOf(alphaStall, NonSI.DEGREE_ANGLE);

		double[] alphaCleanArrayPlot = new double[nPoints];
		double[] cLCleanArrayPlot = new double[nPoints]; 

		Double [] alphaCleanArrayPlotDouble = MyArrayUtils.linspaceDouble(alphaCleanFirst, alphaMaxClean.getEstimatedValue() + 2, nPoints);
		
		for (int i=0; i<alphaCleanArrayPlotDouble.length; i++){
			alphaCleanArrayPlot[i] = alphaCleanArrayPlotDouble[i];
		}
		cLCleanArrayPlot = new double [nPoints];

		double[][] matrixDataClean = { {Math.pow(alphaMaxClean.getEstimatedValue(), 3),
			Math.pow(alphaMaxClean.getEstimatedValue(), 2),
			alphaMaxClean.getEstimatedValue(),1.0},
				{3* Math.pow(alphaMaxClean.getEstimatedValue(), 2),
				2*alphaMaxClean.getEstimatedValue(), 1.0, 0.0},
				{3* Math.pow(alphaStarClean.getEstimatedValue(), 2),
					2*alphaStarClean.getEstimatedValue(), 1.0, 0.0},
				{Math.pow(alphaStarClean.getEstimatedValue(), 3),
						Math.pow(alphaStarClean.getEstimatedValue(), 2),
						alphaStarClean.getEstimatedValue(),1.0}};

		RealMatrix mc = MatrixUtils.createRealMatrix(matrixDataClean);
		double [] vectorClean = {cLMaxClean, 0, cLalphaClean, cLStarClean};

		double [] solSystemC = MyMathUtils.solveLinearSystem(mc, vectorClean);

		double aC = solSystemC[0];
		double bC = solSystemC[1];
		double cC = solSystemC[2];
		double dC = solSystemC[3];

		for ( int i=0 ; i< alphaCleanArrayPlot.length ; i++){
			alphaActual = Amount.valueOf(toRadians(alphaCleanArrayPlot[i]), SI.RADIAN);
			if (alphaActual.getEstimatedValue() < alphaStarClean.getEstimatedValue()) { 
				cLCleanArrayPlot[i] = cLalphaClean*alphaActual.getEstimatedValue() + cL0Clean;}
			else {
				cLCleanArrayPlot[i] = aC * Math.pow(alphaActual.getEstimatedValue(), 3) + 
						bC * Math.pow(alphaActual.getEstimatedValue(), 2) + 
						cC * alphaActual.getEstimatedValue() + dC;
			}
		}


		String folderPathHL = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		
	System.out.println(" \n-----------WRITING CHART TO FILE-------------- ");
		
		MyChartToFileUtils.plotNoLegend(
				alphaCleanArrayPlot, 
				cLCleanArrayPlot,
				null,
				null,
				null,
				null,
				"alpha", "CL",
				"deg", "",
				JPADStaticWriteUtils.createNewFolder(folderPathHL + "Wing Charts" + File.separator),
				"CL curve high lift");

		System.out.println(" \n-------------------DONE----------------------- ");
		
	}
}