package sandbox.mr;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.componentmodel.InnerCalculator;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import configuration.MyConfiguration;
import configuration.enumerations.MethodEnum;
import standaloneutils.GeometryCalc;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import standaloneutils.customdata.MyPoint;
import writers.JPADStaticWriteUtils;


public class WingCalculator { 

	//----------------------------------------------------------
	// NEASTED CLASS CALC CL WING 
	//----------------------------------------------------------

	public class CalcCLWingCurve { //Roskam pp 290-296

		private Amount<javax.measure.quantity.Angle> _alphaOne , _alphaTwo;
		int numberOfValues = 10;// 10 values
		private double _clOne , _clTwo, cLSlopeDeg, cLStar, alphaMaxDouble, alphaStarDoubleDeg, alphaMaxDoubleDegree; 
		private double a, b, c ,d , q;
		private double [] alphaArrayCurve = new double [numberOfValues+2];
		private double [] clArrayCurve = new double [numberOfValues+2];
		private double alphaMinCurve = -2.0;
		private double cLSlopeAnderson, cLStarIntegral;

		//---------------------------------------------------------LINEAR TRAIT		

		/**
		 * This function determines the linear trait slope of the CL-alpha curve using the NasaBlackwell method.
		 * It evaluate CL wing in correspondence of two alpha and calculates the equation of the line.
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha
		 */  

		public double linearSlope(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha){

			_alphaOne = Amount.valueOf(toRadians(0.), SI.RADIAN);
			_clOne = theCLatAlpha.nasaBlackwell(_alphaOne);
			System.out.println("CL at alpha " + _alphaOne + " = "+ _clOne);

			_alphaTwo = Amount.valueOf(toRadians(4.), SI.RADIAN);
			double alphaTwoDouble = 4.0;
			_clTwo = theCLatAlpha.nasaBlackwell(_alphaTwo);
			System.out.println("CL at alpha " + _alphaTwo + " = "+ _clTwo);

			double cLSlope = (_clTwo-_clOne)/_alphaTwo.getEstimatedValue();
			cLSlopeDeg = Math.toRadians(cLSlope);

			q = _clTwo- cLSlopeDeg* alphaTwoDouble;
			System.out.println("q --> " + q);
			return cLSlope;
		}


		//---------------------------------------------------------LINEAR TRAIT	- Integral Mean


		public double linearSlopeIntegral(LSAerodynamicsManager theLSAnalysis, double alphaStarDeg){

			LSAerodynamicsManager.CalcCLAlpha theCLAlpha= theLSAnalysis.new CalcCLAlpha();
			cLSlopeAnderson = theCLAlpha.integralMean2D();
			cLSlopeDeg = Math.toRadians(cLSlopeAnderson);
			System.out.println("CL slope Anderson " + cLSlopeAnderson);
			Amount<javax.measure.quantity.Angle> alphaStarIntegral = Amount.valueOf(toRadians(alphaStarDeg), SI.RADIAN);
			LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha= theLSAnalysis.new CalcCLAtAlpha();
			double cLStarIntegral = theCLatAlpha.nasaBlackwell(alphaStarIntegral);
			q = cLStarIntegral - cLSlopeDeg * alphaStarDeg;


			return cLSlopeAnderson;
		}




		//---------------------------------------------------------NON LINEAR TRAIT			


		/**
		 * This function creates the cubic that approximates the non-linear trait of the curve. The curve is built
		 * from the vertex ( alpha_max, CL_max ) and a point ( alpha_star , CL_star ) solving a linear system. 
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha
		 * @param Amount<Angle> alphaAtCLMax
		 * @param double Alpha star in degree
		 * @param double CL MAX 
		 * 
		 */  

		public void nonLinearPart(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha,
				Amount<javax.measure.quantity.Angle> alphaAtCLMax, double alphaStarDeg , double cLMax){


			alphaMaxDouble = alphaAtCLMax.getEstimatedValue();
			//alphaStarDoubleDeg = alphaStarDeg;
			alphaStarDoubleDeg = 9.5;
			alphaMaxDoubleDegree = Math.toDegrees(alphaMaxDouble);
			Amount<javax.measure.quantity.Angle> alphaStar = Amount.valueOf(toRadians(alphaStarDoubleDeg), SI.RADIAN);
			cLStar = theCLatAlpha.nasaBlackwell(alphaStar);
			double cLSlope = linearSlope(theCLatAlpha);
			System.out.println("CL Slope [1/rad] = " + cLSlope);
			System.out.println("CL Slope [1/deg] = " + cLSlopeDeg);


			System.out.println("alpha max double deg" + alphaMaxDoubleDegree);
			double[][] matrixData = { {Math.pow(alphaMaxDoubleDegree, 3), Math.pow(alphaMaxDoubleDegree, 2), alphaMaxDoubleDegree,1.0},
					{3* Math.pow(alphaMaxDoubleDegree, 2), 2*alphaMaxDoubleDegree, 1.0, 0.0},
					{3* Math.pow(alphaStarDoubleDeg, 2), 2*alphaStarDoubleDeg, 1.0, 0.0},
					{Math.pow(alphaStarDoubleDeg, 3), Math.pow(alphaStarDoubleDeg, 2),alphaStarDoubleDeg,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {cLMax, 0,cLSlopeDeg, cLStar};
			System.out.println("vector " +Arrays.toString(vector));


			System.out.println("cl star " + cLStar);
			System.out.println("cl max " +  cLMax);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]+ "  -  " + matrixData[0][3]);
			System.out.println( matrixData[1][0] + "  -  " + matrixData[1][1]+ "  -  "  + matrixData[1][2]+ "  -  "  + matrixData[1][3]);
			System.out.println( matrixData[2][0]+ "  -  "  + matrixData[2][1] + "  -  " + matrixData[2][2] + "  -  " + matrixData[2][3]);
			System.out.println(matrixData[3][0] + "  -  " +  matrixData[3][1] + "  -  " + matrixData[3][2]+ "  -  " + matrixData[3][3]);

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			a = solSystem[0];
			b = solSystem[1];
			c = solSystem[2];
			d = solSystem[3];
		}



		//---------------------------------------------------------NON LINEAR TRAIT	- integral		

		public void nonLinearPartIntegral(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha,
				Amount<javax.measure.quantity.Angle> alphaAtCLMax, double alphaStarDeg ,
				LSAerodynamicsManager theLSAnalysis, double cLMax){


			alphaMaxDouble = alphaAtCLMax.getEstimatedValue();
			//Amount<javax.measure.quantity.Angle> alphaStar = airfoil.getAerodynamics().get_alphaStar();
			alphaStarDoubleDeg = alphaStarDeg;
			alphaMaxDoubleDegree = Math.toDegrees(alphaMaxDouble);
			Amount<javax.measure.quantity.Angle> alphaStar = Amount.valueOf(toRadians(alphaStarDoubleDeg), SI.RADIAN);
			cLStar = theCLatAlpha.nasaBlackwell(alphaStar);
			double cLSlope = linearSlopeIntegral(theLSAnalysis, alphaStarDeg);
			System.out.println("CL Slope [1/rad] = " + cLSlope);
			System.out.println("CL Slope [1/deg] = " + cLSlopeDeg);


			System.out.println("alpha max double deg" + alphaMaxDoubleDegree);
			double[][] matrixData = { {Math.pow(alphaMaxDoubleDegree, 3), Math.pow(alphaMaxDoubleDegree, 2), alphaMaxDoubleDegree,1.0},
					{3* Math.pow(alphaMaxDoubleDegree, 2), 2*alphaMaxDoubleDegree, 1.0, 0.0},
					{3* Math.pow(alphaStarDoubleDeg, 2), 2*alphaStarDoubleDeg, 1.0, 0.0},
					{Math.pow(alphaStarDoubleDeg, 3), Math.pow(alphaStarDoubleDeg, 2),alphaStarDoubleDeg,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {cLMax, 0,cLSlopeDeg, cLStar};
			System.out.println("vector " +Arrays.toString(vector));


			System.out.println("cl star " + cLStar);
			System.out.println("cl max " +  cLMax);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]+ "  -  " + matrixData[0][3]);
			System.out.println( matrixData[1][0] + "  -  " + matrixData[1][1]+ "  -  "  + matrixData[1][2]+ "  -  "  + matrixData[1][3]);
			System.out.println( matrixData[2][0]+ "  -  "  + matrixData[2][1] + "  -  " + matrixData[2][2] + "  -  " + matrixData[2][3]);
			System.out.println(matrixData[3][0] + "  -  " +  matrixData[3][1] + "  -  " + matrixData[3][2]+ "  -  " + matrixData[3][3]);

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			a = solSystem[0];
			b = solSystem[1];
			c = solSystem[2];
			d = solSystem[3];
		}


		//---------------------------------------------------------NON LINEAR TRAIT	--- parabola		


		/**
		 * This function creates the parabola that approximates the non-linear trait of the curve. The parabola is built
		 * from the vertex ( alpha_max, CL_max ) and a point ( alpha_star , CL_star ) solving a linear system. 
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha
		 * @param Amount<Angle> alphaAtCLMax
		 * @param double Alpha star in degree
		 * @param double CL MAX 
		 * 
		 */  

		public void nonLinearPartParabola(LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha,
				Amount<javax.measure.quantity.Angle> alphaAtCLMax, double alphaStarDeg, double cLMax ){


			alphaMaxDouble = alphaAtCLMax.getEstimatedValue();
			alphaStarDoubleDeg = alphaStarDeg;
			alphaMaxDoubleDegree = Math.toDegrees(alphaMaxDouble);
			Amount<javax.measure.quantity.Angle> alphaStar = Amount.valueOf(toRadians(alphaStarDoubleDeg), SI.RADIAN);
			cLStar = theCLatAlpha.nasaBlackwell(alphaStar);
			double cLSlope = linearSlope(theCLatAlpha);
			System.out.println("CL Slope [1/rad] = " + cLSlope);
			System.out.println("CL Slope [1/deg] = " + cLSlopeDeg);


			System.out.println("alpha max double deg" + alphaMaxDoubleDegree);
			double[][] matrixData = { {Math.pow(alphaMaxDoubleDegree, 3), Math.pow(alphaMaxDoubleDegree, 2), alphaMaxDoubleDegree,1.0},
					{3* Math.pow(alphaMaxDoubleDegree, 2), 2*alphaMaxDoubleDegree, 1.0, 0.0},
					{3* Math.pow(alphaStarDoubleDeg, 2), 2*alphaStarDoubleDeg, 1.0, 0.0},
					{Math.pow(alphaStarDoubleDeg, 3), Math.pow(alphaStarDoubleDeg, 2),alphaStarDoubleDeg,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {cLMax, 0,cLSlopeDeg, cLStar};
			System.out.println("vector " +Arrays.toString(vector));


			System.out.println("cl star " + cLStar);
			System.out.println("cl max " +  cLMax);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]);
			System.out.println("Matrix \n " + matrixData[0][0] + "  -  " +  matrixData[0][1] + "  -  " + matrixData[0][2]+ "  -  " + matrixData[0][3]);
			System.out.println( matrixData[1][0] + "  -  " + matrixData[1][1]+ "  -  "  + matrixData[1][2]+ "  -  "  + matrixData[1][3]);
			System.out.println( matrixData[2][0]+ "  -  "  + matrixData[2][1] + "  -  " + matrixData[2][2] + "  -  " + matrixData[2][3]);
			System.out.println(matrixData[3][0] + "  -  " +  matrixData[3][1] + "  -  " + matrixData[3][2]+ "  -  " + matrixData[3][3]);

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			a = solSystem[0];
			b = solSystem[1];
			c = solSystem[2];
			d = solSystem[3];
		}

		//--------------------------------------------------------BUILT CURVE AND PLOTTING

		/**
		 * This function creates and plots the CL-alpha curve of a Wing Known the alpha_max. It use the NasaBlackwell 
		 * method in order to evaluate the slope of the linear trait and it builds the non-linear trait
		 * using a parabola. This method calls two methods that build separately the linear and non linear part.
		 * 
		 * @author Manuela Ruocco
		 * @param LSAerodynamicsManager
		 * @param Amount<Angle> alphaAtCLMax
		 */  

		public void cLWingCurvePlot(LSAerodynamicsManager theLSAnalysis ,  
				Amount<Angle> alphaAtCLMax, double alphaStarDeg, double clMax){ 

			LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha= theLSAnalysis.new CalcCLAtAlpha();

			//nonLinearPartIntegral(theCLatAlpha, alphaAtCLMax, alphaStarDeg, theLSAnalysis );
			nonLinearPart(theCLatAlpha, alphaAtCLMax, alphaStarDeg, clMax );


			// PLOT
			String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL_Wing" + File.separator);


			double alphaMaxDegree = Math.toDegrees(alphaMaxDouble);
			double alphaMaxCurve = alphaMaxDegree + 1.0;
			System.out.println("alphaMax = " + alphaMaxDouble);
			System.out.println("alphaMaxDegree = " + alphaMaxDegree);
			System.out.println("alpha Max Curve = " + alphaMaxCurve);

			// Linear Part

			System.out.println("alpha min " + alphaMinCurve);
			alphaArrayCurve [0] = alphaMinCurve ; // initial value --> alpha =-2
			alphaArrayCurve [1] = alphaStarDoubleDeg; // last linear value --> alpha star
			clArrayCurve [0] = cLSlopeDeg*alphaArrayCurve [0] + q;
			clArrayCurve [1] = cLSlopeDeg*alphaArrayCurve [1] + q;

			System.out.println("alpha star " + alphaStarDoubleDeg);


			// Non-Linear Part

			double deltaAlpha = (alphaMaxCurve-alphaStarDoubleDeg)/numberOfValues;

			for ( int i=0; i<numberOfValues; i++ ){ 
				int j = i+2;
				alphaArrayCurve[j] = alphaStarDoubleDeg + (i+1)*deltaAlpha;
				clArrayCurve[j] =  a * Math.pow(alphaArrayCurve[j], 3)+ b* Math.pow(alphaArrayCurve[j], 2) + 
						c * alphaArrayCurve[j]+ d;
			}
			System.out.println("alpha Array --> " + Arrays.toString(alphaArrayCurve));
			System.out.println("alpha Array --> " + Arrays.toString(clArrayCurve));


			MyChartToFileUtils.plotNoLegend
			(alphaArrayCurve, clArrayCurve, -3.0,alphaMaxCurve 
					+ 2.0, -1.0, 2.0, "alpha", "CL", "deg" , "", subfolderPath, "CLalpha");


			System.out.println("-----------------------------------------------------");
			System.out.println("\t \t DONE PLOTTING CL alpha CURVE ");


		}


		//--------------------------------------------- GETTERS

		public Amount<Angle> get_AlphaOne() {
			return _alphaOne;
		}

		public Amount<Angle> get_AlphaTwo() {
			return _alphaTwo;
		}

		public double get_ClOne() {
			return _clOne;
		}

		public double get_ClTwo() {
			return _clTwo;
		}



		//----------------------------------------------------------
		// INTERMEDIATE PROFILE
		//----------------------------------------------------------

		/**
		 * This function calculates the characteristics of an intermediate airfoil, having as input the 
		 * airfoils at root, kink and tip and relates positions.
		 * This function creates a new airfoil with its own characteristics.
		 * 
		 * @author Manuela Ruocco
		 * @param MyAirfoil Airfoilroot
		 * @param Amount<Length> root position
		 * @param MyAirfoil Airfoilkink
		 * @param Amount<Length> Kink position
		 * @param MyAirfoil Airfoiltip
		 * @param Amount<Length> Tip position
		 */  


		public MyAirfoil calculateIntermediateProfile(MyAirfoil airfoilRoot, Amount<Length> root , 
				MyAirfoil airfoilKink, Amount<Length> kink ,
				MyAirfoil airfoilTip, Amount<Length> tip){ 

			return airfoilTip;

		}

		/**
		 * This function calculates the characteristics of an intermediate airfoil, having as input the 
		 * airfoils at root and tip and relates positions.
		 * This function creates a new airfoil with its own characteristics.
		 * 
		 * @author Manuela Ruocco
		 * @param MyAirfoil Airfoilroot
		 * @param Amount<Length> root position
		 * @param MyAirfoil Airfoiltip
		 * @param Amount<Length> Tip position
		 */  

		public MyAirfoil calculateIntermediateProfile(MyAirfoil airfoilRoot, Amount<Length> root ,
				MyAirfoil airfoilTip, Amount<Length> tip){

			return airfoilTip;

		}
	}



	//----------------------------------------------------------
	// MEAN AIRFOIL
	//----------------------------------------------------------


	public class MeanAirfoil { //Behind ADAS p39
		private double influenceAreaRoot, influenceAreaKink, influenceAreaTip ;
		private double kRoot, kKink, kTip;
		private double rootChord, kinkChord, tipChord, dimensionalKinkStation, dimensionalOverKink;
		private double alphaStarRoot, alphaStarKink, alphaStarTip;
		private double alphaZeroLiftRoot, alphaZeroLiftKink, alphaZeroLiftTip;
		private double clAplhaRoot, clAplhaKink, clAplhaTip;
		private double clStarRoot, clStarKink, clStarTip;
		private double alphaMaxRoot, alphaMaxKink, alphaMaxTip;
		private double clMaxRoot, clMaxKink, clMaxTip;
		private double cdMinRoot, cdMinKink, cdMinTip;
		private double cl_cdMinRoot, cl_cdMinKink, cl_cdMinTip;
		private double kDragPolarRoot, kDragPolarKink, kDragPolarTip;
		private double x_acRoot, x_acKink, x_acTip;
		private double cm_acRoot, cm_acKink, cm_acTip;
		private double cm_ac_StallRoot, cm_ac_StallKink, cm_ac_StallTip;
		private double cmAlpha_acRoot, cmalpha_acKink, cmAlpha_acTip;
		private double reynoldsCruiseRoot, reynoldsCruiseKink, reynoldsCruiseTip;
		private double reynoldsStallRoot, reynoldsStallKink, reynoldsStallTip;
		private double twistRoot, twistKink, twistTip;
		private double phi_TERoot, phi_TEKink, phi_TETip;
		private double radius_LERoot, radius_LEKink, radius_LETip;
		private double thicknessOverChordUnit_Root, thicknessOverChordUnit_Kink, thicknessOverChordUnit_Tip;
		private double maxThicknessOverChord_Root, maxThicknessOverChord_Kink, maxThicknessOverChord_Tip;



		/**
		 * This function calculates the characteristics of the mean airfoil using the influence areas.
		 * This function creates a new airfoil with its own characteristics that are the characteristics of the
		 * mean airfoil.
		 * 
		 * @author Manuela Ruocco ft Vittorio Trifari
		 * @param Airfoilroot
		 * @param root position
		 * @param Airfoilkink
		 * @param Kink position
		 * @param Airfoiltip
		 * @param Tip position
		 */  



		public MyAirfoil calculateMeanAirfoil (LiftingSurface theWing , MyAirfoil airfoilRoot, 
				MyAirfoil airfoilKink, MyAirfoil airfoilTip){

			MyAirfoil meanAirfoil = new MyAirfoil(theWing);

			System.out.println( "---------------------------------------");
			System.out.println( "STARTING EVALUATION OF THE MEAN AIRFOIL");
			System.out.println( "---------------------------------------");
			System.out.println("\n \nSTART OF THE EVALUTATION OF THE INFLUENCE AREAS...");

			rootChord = theWing.get_chordRoot().getEstimatedValue();
			kinkChord = theWing.get_chordKink().getEstimatedValue();
			tipChord = theWing.get_chordTip().getEstimatedValue();
			dimensionalKinkStation = theWing.get_spanStationKink()*theWing.get_semispan().getEstimatedValue();
			dimensionalOverKink = theWing.get_semispan().getEstimatedValue() - dimensionalKinkStation;

			influenceAreaRoot = rootChord * dimensionalKinkStation/2;
			influenceAreaKink = (kinkChord * dimensionalKinkStation/2) + (kinkChord * dimensionalOverKink/2);
			influenceAreaTip = tipChord * dimensionalOverKink/2;

			System.out.println("The influence area of root chord is [m^] = " + influenceAreaRoot );
			System.out.println("The influence area of kink chord is [m^] = " + influenceAreaKink );
			System.out.println("The influence area of tip chord is [m^] = " + influenceAreaTip);

			kRoot = 2*influenceAreaRoot/theWing.get_surface().getEstimatedValue();
			kKink = 2*influenceAreaKink/theWing.get_surface().getEstimatedValue();
			kTip = 2*influenceAreaTip/theWing.get_surface().getEstimatedValue();

			System.out.println("The coefficients of influence areas are: \n k1= " + kRoot + 
					"\n k2= " + kKink + "\n k3= " + kTip);

			System.out.println( "\n \n---------------------------------------");
			System.out.println("DONE");

			//ALPHA ZERO LIFT
			alphaZeroLiftRoot = airfoilRoot.getAerodynamics().get_alphaZeroLift().getEstimatedValue();
			alphaZeroLiftKink = airfoilKink.getAerodynamics().get_alphaZeroLift().getEstimatedValue();
			alphaZeroLiftTip = airfoilTip.getAerodynamics().get_alphaZeroLift().getEstimatedValue();

			double alphaZeroLiftMeanAirfoil = alphaZeroLiftRoot * kRoot + alphaZeroLiftKink * kKink + alphaZeroLiftTip * kTip;

			meanAirfoil.getAerodynamics().set_alphaZeroLift(
					Amount.valueOf(
							(alphaZeroLiftMeanAirfoil), SI.RADIAN));

			//CL_ALPHA
			clAplhaRoot = airfoilRoot.getAerodynamics().get_clAlpha();
			clAplhaKink = airfoilKink.getAerodynamics().get_clAlpha();
			clAplhaTip = airfoilTip.getAerodynamics().get_clAlpha();

			double clAlphaMeanAirfoil = clAplhaRoot * kRoot + clAplhaKink * kKink + clAplhaTip * kTip;

			meanAirfoil.getAerodynamics().set_clAlpha(clAlphaMeanAirfoil);

			//CL STAR
			clStarRoot = airfoilRoot.getAerodynamics().get_clStar();
			clStarKink = airfoilKink.getAerodynamics().get_clStar();
			clStarTip = airfoilTip.getAerodynamics().get_clStar();

			double clStarMeanAirfoil = clStarRoot * kRoot + clStarKink * kKink + clStarTip * kTip;

			meanAirfoil.getAerodynamics().set_clStar(clStarMeanAirfoil);

			//ALPHA MAX
			alphaMaxRoot = airfoilRoot.getAerodynamics().get_alphaStall().getEstimatedValue();
			alphaMaxKink = airfoilKink.getAerodynamics().get_alphaStall().getEstimatedValue();
			alphaMaxTip = airfoilTip.getAerodynamics().get_alphaStall().getEstimatedValue();

			double alphaMaxMeanAirfoil = alphaMaxRoot * kRoot + alphaMaxKink * kKink + alphaMaxTip * kTip;

			meanAirfoil.getAerodynamics().set_alphaStall(
					Amount.valueOf(
							(alphaMaxMeanAirfoil), SI.RADIAN));

			//CL MAX
			clMaxRoot = airfoilRoot.getAerodynamics().get_clMax();
			clMaxKink = airfoilKink.getAerodynamics().get_clMax();
			clMaxTip = airfoilTip.getAerodynamics().get_clMax();

			double clMaxMeanAirfoil = clMaxRoot * kRoot + clMaxKink * kKink + clMaxTip * kTip;

			meanAirfoil.getAerodynamics().set_clMax(clMaxMeanAirfoil);

			//CD MIN
			cdMinRoot = airfoilRoot.getAerodynamics().get_cdMin();
			cdMinKink = airfoilKink.getAerodynamics().get_cdMin();
			cdMinTip = airfoilTip.getAerodynamics().get_cdMin();

			double cdMinMeanAirfoil = cdMinRoot * kRoot + cdMinKink * kKink + cdMinTip * kTip;

			meanAirfoil.getAerodynamics().set_cdMin(cdMinMeanAirfoil);

			//CL AT CD MIN
			cl_cdMinRoot = airfoilRoot.getAerodynamics().get_clAtCdMin();
			cl_cdMinKink = airfoilKink.getAerodynamics().get_clAtCdMin();
			cl_cdMinTip = airfoilTip.getAerodynamics().get_clAtCdMin();

			double cl_cdMinMeanAirfoil = cl_cdMinRoot * kRoot + cl_cdMinKink * kKink + cl_cdMinTip * kTip;

			meanAirfoil.getAerodynamics().set_clAtCdMin(cl_cdMinMeanAirfoil);

			//K FACTOR DRAG POLAR
			kDragPolarRoot = airfoilRoot.getAerodynamics().get_kFactorDragPolar();
			kDragPolarKink = airfoilKink.getAerodynamics().get_kFactorDragPolar();
			kDragPolarTip = airfoilTip.getAerodynamics().get_kFactorDragPolar();

			double kDragPolarMeanAirfoil = kDragPolarRoot * kRoot + kDragPolarKink * kKink + kDragPolarTip * kTip;

			meanAirfoil.getAerodynamics().set_kFactorDragPolar(kDragPolarMeanAirfoil);

			//Xac
			x_acRoot = airfoilRoot.getAerodynamics().get_aerodynamicCenterX();
			x_acKink = airfoilKink.getAerodynamics().get_aerodynamicCenterX();
			x_acTip = airfoilTip.getAerodynamics().get_aerodynamicCenterX();

			double x_acMeanAirfoil = x_acRoot * kRoot + x_acKink * kKink + x_acTip * kTip;

			meanAirfoil.getAerodynamics().set_aerodynamicCenterX(x_acMeanAirfoil);

			//CMac
			cm_acRoot = airfoilRoot.getAerodynamics().get_cmAC();
			cm_acKink = airfoilKink.getAerodynamics().get_cmAC();
			cm_acTip = airfoilTip.getAerodynamics().get_cmAC();

			double cm_acMeanAirfoil = cm_acRoot * kRoot + cm_acKink * kKink + cm_acTip * kTip;

			meanAirfoil.getAerodynamics().set_cmAC(cm_acMeanAirfoil);

			//CMac_Stall
			cm_ac_StallRoot = airfoilRoot.getAerodynamics().get_cmACStall();
			cm_ac_StallKink = airfoilKink.getAerodynamics().get_cmACStall();
			cm_ac_StallTip = airfoilTip.getAerodynamics().get_cmACStall();

			double cm_acStallMeanAirfoil = cm_ac_StallRoot * kRoot + cm_ac_StallKink * kKink + cm_ac_StallTip * kTip;

			meanAirfoil.getAerodynamics().set_cmACStall(cm_acStallMeanAirfoil);

			//CM ALPHA LE
			cmAlpha_acRoot = airfoilRoot.getAerodynamics().get_cmAlphaAC();
			cmalpha_acKink = airfoilKink.getAerodynamics().get_cmAlphaAC();
			cmAlpha_acTip = airfoilTip.getAerodynamics().get_cmAlphaAC();

			double cmAlpha_acMeanAirfoil = cmAlpha_acRoot * kRoot + cmalpha_acKink * kKink + cmAlpha_acTip * kTip;

			meanAirfoil.getAerodynamics().set_cmAlphaAC(cmAlpha_acMeanAirfoil);

			//REYNOLDS CRUISE
			reynoldsCruiseRoot = airfoilRoot.getAerodynamics().get_reynoldsCruise();
			reynoldsCruiseKink = airfoilKink.getAerodynamics().get_reynoldsCruise();
			reynoldsCruiseTip = airfoilTip.getAerodynamics().get_reynoldsCruise();

			double reynoldsCruiseMeanAirfoil = reynoldsCruiseRoot * kRoot + reynoldsCruiseKink * kKink + reynoldsCruiseTip * kTip;

			meanAirfoil.getAerodynamics().set_reynoldsCruise(reynoldsCruiseMeanAirfoil);

			//REYNOLDS STALL
			reynoldsStallRoot = airfoilRoot.getAerodynamics().get_reynoldsNumberStall();
			reynoldsStallKink = airfoilKink.getAerodynamics().get_reynoldsNumberStall();
			reynoldsStallTip = airfoilTip.getAerodynamics().get_reynoldsNumberStall();

			double reynoldsStallMeanAirfoil = reynoldsStallRoot * kRoot + reynoldsStallKink * kKink + reynoldsStallTip * kTip;

			meanAirfoil.getAerodynamics().set_reynoldsNumberStall(reynoldsStallMeanAirfoil);

			//TWIST
			twistRoot = airfoilRoot.getGeometry().get_twist().getEstimatedValue();
			twistKink = airfoilKink.getGeometry().get_twist().getEstimatedValue();
			twistTip = airfoilTip.getGeometry().get_twist().getEstimatedValue();

			double twistMeanAirfoil = twistRoot * kRoot + twistKink * kKink + twistTip * kTip;

			meanAirfoil.getGeometry().set_twist(
					Amount.valueOf(
							(twistMeanAirfoil), SI.RADIAN));

			//PHI_TE
			phi_TERoot = airfoilRoot.getGeometry().get_anglePhiTE().getEstimatedValue();
			phi_TEKink = airfoilKink.getGeometry().get_anglePhiTE().getEstimatedValue();
			phi_TETip = airfoilTip.getGeometry().get_anglePhiTE().getEstimatedValue();

			double phi_TEMeanAirfoil = phi_TERoot * kRoot + phi_TEKink * kKink + phi_TETip * kTip;

			meanAirfoil.getGeometry().set_anglePhiTE(
					Amount.valueOf(
							(phi_TEMeanAirfoil), SI.RADIAN));

			//RADIUS LE
			radius_LERoot = airfoilRoot.getGeometry().get_radiusLE();
			radius_LEKink = airfoilKink.getGeometry().get_radiusLE();
			radius_LETip = airfoilTip.getGeometry().get_radiusLE();

			double radius_LEMeanAirfoil = radius_LERoot * kRoot + radius_LEKink * kKink + radius_LETip * kTip;

			meanAirfoil.getGeometry().set_radiusLE(radius_LEMeanAirfoil);

			//THICKNESS OVER CHORD UNIT
			thicknessOverChordUnit_Root = airfoilRoot.getGeometry().get_thicknessOverChordUnit();
			thicknessOverChordUnit_Kink = airfoilKink.getGeometry().get_thicknessOverChordUnit();
			thicknessOverChordUnit_Tip = airfoilTip.getGeometry().get_thicknessOverChordUnit();

			double thicknessOverChordUnit_MeanAirfoil = thicknessOverChordUnit_Root * kRoot + thicknessOverChordUnit_Kink * kKink + thicknessOverChordUnit_Tip * kTip;

			meanAirfoil.getGeometry().set_thicknessOverChordUnit(thicknessOverChordUnit_MeanAirfoil);

			//MAX THICKNESS OVER CHORD
			maxThicknessOverChord_Root = airfoilRoot.getGeometry().get_maximumThicknessOverChord();
			maxThicknessOverChord_Kink = airfoilKink.getGeometry().get_maximumThicknessOverChord();
			maxThicknessOverChord_Tip = airfoilTip.getGeometry().get_maximumThicknessOverChord();

			double maxThicknessOverChord_MeanAirfoil = maxThicknessOverChord_Root * kRoot + maxThicknessOverChord_Kink * kKink + maxThicknessOverChord_Tip * kTip;

			meanAirfoil.getGeometry().set_thicknessOverChordUnit(maxThicknessOverChord_MeanAirfoil);

			//ALPHA STAR 

			alphaStarRoot = airfoilRoot.getAerodynamics().get_alphaStar().getEstimatedValue();
			alphaStarKink = airfoilKink.getAerodynamics().get_alphaStar().getEstimatedValue();
			alphaStarTip = airfoilTip.getAerodynamics().get_alphaStar().getEstimatedValue();

			double alphaStarMeanAirfoil = alphaStarRoot * kRoot + alphaStarKink * kKink + alphaStarTip * kTip;

			meanAirfoil.getAerodynamics().set_alphaStar(
					Amount.valueOf(
							(alphaStarMeanAirfoil), SI.RADIAN));



			//LEADING EDGE SHARPNESS PARAMETER

			double LESharpnessParameterRoot = airfoilRoot.getGeometry().get_deltaYPercent();
			double LESharpnessParameterKink =  airfoilKink.getGeometry().get_deltaYPercent();
			double LESharpnessParameterTip =  airfoilTip.getGeometry().get_deltaYPercent();

			double meanLESharpParam =LESharpnessParameterRoot * kRoot + LESharpnessParameterKink *  kKink +
					LESharpnessParameterTip * kTip;

			meanAirfoil.getGeometry().set_deltaYPercent(meanLESharpParam);

			return meanAirfoil;

		}
	}






	public class IntermediateAirfoil { 
		double rootChord, kinkChord, tipChord, dimensionalKinkStation, dimensionalOverKink;
		private double intermediateClMax, intermediateEta,intermediateTwist, intermediateChord, intermediateDistanceAC, intermediateXac ,
		intermediateAlphaZL, intermediateAlphaStar,intermediateClStar,
		intermediateClMaxSweep, intermediateClatMinCD, intermediateCdMin, intermediateCm,
		intermediateCmAlphaLE , intermediateAerodynamicCentre, intermediateMaxThickness , 
		intermediateReynolds, intermediatekFactorPolar, intermediateClAlpha, intermediateAlphaStall;


		/**
		 * This function calculates the characteristics of an intermediate airfoil.
		 * 
		 * @author Manuela Ruocco
		 * @param Airfoilroot
		 * @param Airfoilkink
		 * @param Airfoiltip
		 * @param Dimensional station where the airfoil is located.
		 */ 

		public MyAirfoil calculateIntermediateAirfoil (LiftingSurface theWing , MyAirfoil airfoilRoot, 
				MyAirfoil airfoilKink, MyAirfoil airfoilTip, double yLoc){

			MyAirfoil intermediateAirfoil = new MyAirfoil(theWing);

//			System.out.println( "---------------------------------------");
//			System.out.println( "STARTING EVALUATION OF INTERMEDIATE AIRFOIL");
//			System.out.println( "---------------------------------------");
//			System.out.println( " The position of arifoil is --> " + yLoc);

			rootChord = theWing.get_chordRoot().getEstimatedValue();
			kinkChord = theWing.get_chordKink().getEstimatedValue();
			tipChord = theWing.get_chordTip().getEstimatedValue();
			dimensionalKinkStation = theWing.get_spanStationKink()*theWing.get_semispan().getEstimatedValue();
			dimensionalOverKink = theWing.get_semispan().getEstimatedValue() - dimensionalKinkStation;



			// ETA
			intermediateEta = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_etaAirfoil().toArray(), yLoc);
			intermediateAirfoil.getGeometry().set_etaLocation(intermediateEta);

			// TWIST
			intermediateTwist = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_twistVsY().toArray(), yLoc);
			intermediateAirfoil.getGeometry().set_twist(Amount.valueOf((intermediateTwist), SI.RADIAN));

			// CL MAX
			intermediateClMax = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_clMaxVsY().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_clMax(intermediateClMax);

			// CHORD
			intermediateAirfoil.getGeometry().update(yLoc);

			// ALFA ZERO LIFT
			intermediateAlphaZL = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_alpha0VsY().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_alphaZeroLift(Amount.valueOf((intermediateAlphaZL), SI.RADIAN));

			//CL ALPHA
			intermediateClAlpha = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_clAlpha_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_clAlpha(intermediateClAlpha);

			// ALFA STAR
			intermediateAlphaStar = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_alphaStar_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_alphaStar((Amount.valueOf((intermediateAlphaStar), SI.RADIAN)));

			//ALFA STALL
			intermediateAlphaStall = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_alphaStall().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_alphaStall((Amount.valueOf((intermediateAlphaStall), SI.RADIAN)));

			// CL STAR 
			intermediateClStar =  MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_clStar_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_clStar(intermediateClStar);

			// CL MAX SWEEP
			//intermediateClMaxSweep =  MyMathUtils.getInterpolatedValue1DLinear(
			//	theWing.get_yStationsAirfoil().toArray(),theWing.get_clMaxSweep_y().toArray(), yLoc);
			//intermediateAirfoil.getAerodynamics().set_clMaxSweep(intermediateClMaxSweep);

			// CL AT CD MIN
			intermediateClatMinCD =  MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_clAtCdMin_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_clAtCdMin(intermediateClatMinCD);

			// CD MIN
			intermediateCdMin =   MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_cdMin_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_cdMin(intermediateCdMin);

			// CM AC
			intermediateCm = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_cmAC_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_cmAC(intermediateCm );

			// CM AT LE
			intermediateCmAlphaLE = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_cmAlphaLE_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_cmAlphaLE(intermediateCmAlphaLE);

			// AERODYNAMIC CENTRE X CHOORD
			intermediateAerodynamicCentre = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_aerodynamicCenterXcoord_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_aerodynamicCenterX(intermediateAerodynamicCentre);

			// MAX THICKNESS
			intermediateMaxThickness = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_maxThicknessVsY().toArray(), yLoc);
			intermediateAirfoil.getGeometry().set_maximumThicknessOverChord(intermediateMaxThickness);	

			// K FACTOR DRAG POLAR
			intermediatekFactorPolar = MyMathUtils.getInterpolatedValue1DLinear(
					theWing.get_yStationsAirfoil().toArray(),theWing.get_kFactorDragPolar_y().toArray(), yLoc);
			intermediateAirfoil.getAerodynamics().set_kFactorDragPolar(intermediatekFactorPolar);


			return intermediateAirfoil;

		}
	}



			//--------------------------
			// STARTING EVALUATE CL LOCAL 
			//---------------------------
			
	

			
			

			
}