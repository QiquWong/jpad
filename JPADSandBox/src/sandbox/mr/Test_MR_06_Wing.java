// This test generates a default wing in order to do some aerodynamic analysis on it.
// Three airfoils are given as input data (with relatives value) and the purpose is the evaluation of the 
// following value:

// Lift curve of wing ( linear and non linear parts)
// Drag polar for each intermediate profile
// Moment curve for each intermediate profile
// Distribution of drag coefficient 
// Distribution of Moment



package sandbox.mr;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hpsf.NoSingleSectionException;
import org.jscience.physics.amount.Amount;

import com.google.common.collect.TreeBasedTable;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAerodynamics;
import aircraft.auxiliary.airfoil.MyGeometry;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.NasaBlackwell;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcLiftDistribution;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import igeo.IVec2R.Angle;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Test_MR_06_Wing {

	public static void main(String[] args) {

		Amount<javax.measure.quantity.Angle> deltaAlphaMax; 
		MyAirfoil meanAirfoil;


		// -----------------------------------------------------------------------
		// Generate default Wing
		// -----------------------------------------------------------------------
		//
		//		// Fuselage
		//		Fuselage theFuselage = new Fuselage(
		//				"Fuselage", // name
		//				"Data from AC_ATR_72_REV05.pdf", // description
		//				0.0, 0.0, 0.0 // Fuselage apex (x,y,z)-coordinates in construction axes
		//				);
		//
		//		// Wing
		//		double xAw = 11.0; //meter 
		//		double yAw = 0.0;
		//		double zAw = 1.6;
		//		double iw = 0.0;
		//		LiftingSurface theWing = new LiftingSurface(
		//				"Wing", // name
		//				"Data from AC_ATR_72_REV05.pdf", 
		//				xAw, yAw, zAw, iw, 
		//				ComponentEnum.WING,
		//				theFuselage // let her see the fuselage
		//				); 
		//
		//		theWing.calculateGeometry();
		//		theWing.getGeometry().calculateAll();
		//
		//		// Center of Gravity
		//		double xCgLocal= 1.5; // meter 
		//		double yCgLocal= 0;
		//		double zCgLocal= 0;
		//
		//		CenterOfGravity cg = new CenterOfGravity(
		//				Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
		//				Amount.valueOf(yCgLocal, SI.METER),
		//				Amount.valueOf(zCgLocal, SI.METER),
		//				Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
		//				Amount.valueOf(yAw, SI.METER),
		//				Amount.valueOf(zAw, SI.METER),
		//				Amount.valueOf(0.0, SI.METER),// origin of BRF
		//				Amount.valueOf(0.0, SI.METER),
		//				Amount.valueOf(0.0, SI.METER)
		//				);
		//
		//		cg.calculateCGinBRF();
		//		theWing.set_cg(cg);
		//
		//		// Default operating conditions
		//		OperatingConditions theOperatingConditions = new OperatingConditions();				
		//
		//		System.out.println("\n \n-----------------------------------------------------");
		//		System.out.println("Operating condition");
		//		System.out.println("-----------------------------------------------------");
		//		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		//		System.out.println("\tAltitude: " + theOperatingConditions.get_altitude());
		//		System.out.println("----------------------");
		//
		//
		//		// allocate manager
		//		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
		//				theOperatingConditions,
		//				theWing
		//				);
		//
		//
		//		WingCalculator theWngAnalysis = new WingCalculator();


		//--------------------------------------------------------------------------------------
		// B747-100B
		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theOperatingConditions = new OperatingConditions();
		theOperatingConditions.set_altitude(Amount.valueOf(10000.0, SI.METER));
		theOperatingConditions.set_machCurrent(0.84);
		theOperatingConditions.calculate();

		Aircraft aircraft = Aircraft.createDefaultAircraft("B747-100B");
		aircraft.set_name("B747-100B");

		LiftingSurface theWing = aircraft.get_wing();

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingConditions);
		theAnalysis.updateGeometry(aircraft);


		//--------------------------------------------------------------------------------------
		// B747-100B
		//--------------------------------------------------------------------------------------
		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(23.1, SI.METER)); 
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(0.0, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);


		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theOperatingConditions,
				theWing,
				aircraft
				);



		// -----------------------------------------------------------------------
		// Database
		// -----------------------------------------------------------------------


		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String databaseFileName = "Aerodynamic_Database_Ultimate.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath, databaseFileName);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);



		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);

		theAnalysis.doAnalysis(aircraft,AnalysisTypeEnum.AERODYNAMIC);

		theWing.setAerodynamics(theLSAnalysis);

		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();

		WingCalculator theWngAnalysis = new WingCalculator();


		// -----------------------------------------------------------------------
		// Define airfoil
		// -----------------------------------------------------------------------

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("AIRFOIL");
		System.out.println("-----------------------------------------------------");



		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot, "23-018");
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
		System.out.println("Root Chord " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());		
		System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());


		//		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_clAlpha(6.07);
		//		airfoilRoot.getAerodynamics().set_clMax(1.3);
		//		airfoilRoot.getAerodynamics().set_clStar(1.06);



		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink, "23-015");
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("Kink Station " + yLocKink);
		System.out.println("Kink Chord " + theWing.get_chordKink().getEstimatedValue() );
		System.out.println("Kink maximum thickness " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink " + airfoilKink.getGeometry().get_deltaYPercent());

		//		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_clAlpha(6.07);
		//		airfoilKink.getAerodynamics().set_clMax(1.3);
		//		airfoilKink.getAerodynamics().set_clStar(1.06);


		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		//		airfoilTip.getAerodynamics().set_clMax(1.0);
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());

		//		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.6), SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(6.0),SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_clAlpha(6.01);
		//		airfoilTip.getAerodynamics().set_clMax(1.0);
		//		airfoilTip.getAerodynamics().set_clStar(0.63);

		// -----------------------------------------------------------------------
		// Assign airfoil
		// -----------------------------------------------------------------------


		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry(); 
		theLSAnalysis.initializeDependentData();


		// -----------------------------------------------------------------------
		// Mean airfoil 
		// -----------------------------------------------------------------------

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate the mean airfoil characteristics");
		System.out.println("-----------------------------------------------------");

		WingCalculator.MeanAirfoil theMeanAirfoilCalculator = theWngAnalysis.new MeanAirfoil();
		meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theWing, airfoilRoot, airfoilKink, airfoilTip);
		double meanAlphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();


		System.out.println("\nThe mean alpha star is [rad] = " + meanAlphaStar);
		double alphaStarDeg = Math.toDegrees(meanAlphaStar);
		System.out.println("The mean alpha star is [deg] = " + alphaStarDeg);

		double meanLESharpnessParameter = meanAirfoil.getGeometry().get_deltaYPercent();

		// -----------------------------------------------------------------------
		// Using NASA-Blackwell method for estimating the lifting surface CLmax
		// -----------------------------------------------------------------------

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate CL max wing");
		System.out.println("-----------------------------------------------------");


		LSAerodynamicsManager.CalcCLMaxClean theCLmaxAnalysis = theLSAnalysis.new CalcCLMaxClean(); //is nested
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLAnalysis = theLSAnalysis.new CalcCLvsAlphaCurve();
		LSAerodynamicsManager.CalcCLAtAlpha theCLatAlpha= theLSAnalysis.new CalcCLAtAlpha();
		System.out.println("\tEvaluate CL distribution using Nasa-Blackwell method");
		theCLAnalysis.nasaBlackwell(); //it's possible to set alpha values 
		//theCLAnalysis.allMethods();;
		System.out.println("\n \tEvaluate CL max using CL distribution");
		theCLmaxAnalysis.nasaBlackwell();
		//theCLmaxAnalysis.allMethods(); 
		Amount<javax.measure.quantity.Angle> alphaAtCLMax = theLSAnalysis.get_alphaStall();
		System.out.println("alpha CL max : " + alphaAtCLMax);
		double clMax = theCLatAlpha.nasaBlackwell(alphaAtCLMax);
		System.out.println("cl " + clMax);


		// PLOT

		System.out.println("\n \n \t \t WRITING CHART TO FILE. Evaluating CL_MAX ");
		System.out.println("-----------------------------------------------------");


		// interpolation of CL MAX_airfoil
		MyArray clMaxAirfoil = theCLmaxAnalysis.getClAirfoils();
		System.out.println("CL max airfoil " + clMaxAirfoil);

		// CL distribution
		//		Amount<javax.measure.quantity.Angle> alphaFirst = Amount.valueOf(toRadians(2.), SI.RADIAN);
		//		MyArray clAlphaFirst = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL , alphaFirst);
		//		//double clAlphaFirst = theCLatAlpha.nasaBlackwell(alphaFirst);
		//		System.out.println("CL distribution at alpha " + alphaFirst + " --> " + clAlphaFirst );
		//
		//		Amount<javax.measure.quantity.Angle> alphaSecond = Amount.valueOf(toRadians(6), SI.RADIAN);
		//		MyArray clAlphaSecond = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL , alphaSecond);
		//		//double clAlphaSecond = theCLatAlpha.nasaBlackwell(alphaSecond);
		//		System.out.println("CL distribution at alpha " + alphaSecond + " --> " + clAlphaSecond );

		MyArray clAlphaThird = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL ,alphaAtCLMax);
		//double [] clAlphaThird = theCLAnalysis.nasaBlackwell();
		System.out.println("CL distribution at alpha " + alphaAtCLMax + " --> " + clAlphaThird );
		//System.out.println("CL distribution at alpha " + alphaAtCLMax + " --> " +clMax );


		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL_Wing_testMR" + File.separator);

		double [][] semiSpanAd = {theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};
		//				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};


		//		double [][] clDistribution = {clMaxAirfoil.getRealVector().toArray(), clAlphaFirst.getRealVector().toArray(),
		//				clAlphaSecond.getRealVector().toArray(), clAlphaThird.getRealVector().toArray()};

		double [][] clDistribution = {clMaxAirfoil.getRealVector().toArray(), clAlphaThird.getRealVector().toArray()};
		String [] legend = new String [4];
		legend[0] = "CL max airfoil";
		//		legend[1] = "CL distribution at alpha " + Math.toDegrees(alphaFirst.getEstimatedValue());
		//		legend[2] = "CL distribution at alpha " + Math.toDegrees(alphaSecond.getEstimatedValue());
		legend[1] = "CL distribution at alpha " + Math.toDegrees( alphaAtCLMax.getEstimatedValue());	

		MyChartToFileUtils.plot(
				semiSpanAd,	clDistribution, // array to plot
				0.0, 1.0, 0.0, 2.0,					    // axis with limits
				"eta", "CL", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "Stall ");			    // output informations

		System.out.println("-----------------------------------------------------");
		System.out.println("\t \t DONE ");


		// -----------------------------------------------------------------------
		// Evaluate alpha max
		// -----------------------------------------------------------------------

		// With NASA Blackwell method we can evaluate the CL max inviscid. So we can use a correction in alpha max  //Sforza p150

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("STARTING EVALUATE DELTA ALPHA MAX");
		System.out.println("-----------------------------------------------------");


		System.out.println(" the mean LE sharpness parameter is : " + meanLESharpnessParameter);
		System.out.println("the LE sweep angle is " +  theWing.get_sweepLEEquivalent());
		deltaAlphaMax = Amount.valueOf(toRadians (theLSAnalysis.get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(theWing.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue() ,
				meanLESharpnessParameter )), SI.RADIAN);;
				System.out.println("Delta  alpha max " + deltaAlphaMax);
				Amount<javax.measure.quantity.Angle> alphaAtCLMaxNew =  Amount.valueOf((alphaAtCLMax.getEstimatedValue() + deltaAlphaMax.getEstimatedValue()), SI.RADIAN);
				System.out.println( "Alpha max " + alphaAtCLMaxNew );




				// -----------------------------------------------------------------------
				// Evaluate wing Lift curve
				// -----------------------------------------------------------------------

				// Now we have all ingredient to evaluate the wing lift curve.
				// The slope of linear part is given by the Nasa- Blackwell method. Known two points ( = evaluating CL at two
				// different alpha ) we can know the slope.
				// From CL alpha wing and Alpha_max, we can evaluate the non-linear part.


				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("STARTING EVALUATE WING LIFT CURVE");
				System.out.println("-----------------------------------------------------");



				// cl alpha
				WingCalculator.CalcCLWingCurve theCLCurve = theWngAnalysis.new CalcCLWingCurve();
				theCLCurve.cLWingCurvePlot(theLSAnalysis, alphaAtCLMaxNew, alphaStarDeg, clMax);



				// -----------------------------------------------------------------------
				// Generate an intermediate airfoil
				// -----------------------------------------------------------------------


				WingCalculator.IntermediateAirfoil theIntermediate = theWngAnalysis.new IntermediateAirfoil();
				MyAirfoil intermediateAirfoil = theIntermediate.calculateIntermediateAirfoil(theWing, airfoilRoot, airfoilKink, airfoilTip, 5.3);

				// print value to check the result
				System.out.println(" cl max kink " + airfoilKink.getAerodynamics().get_clMax());
				System.out.println(" cl max intermediate "  + intermediateAirfoil.getAerodynamics().get_clMax());
				System.out.println(" cl max tip " + airfoilTip.getAerodynamics().get_clMax());

				System.out.println(" alpha stall kink " + airfoilKink.getAerodynamics().get_alphaStall());
				System.out.println(" alpha stall intermediate "  + intermediateAirfoil.getAerodynamics().get_alphaStall());
				System.out.println(" alpha stall tip " + airfoilTip.getAerodynamics().get_alphaStall());

				System.out.println(" cl alpha kink " + airfoilKink.getAerodynamics().get_clAlpha());
				System.out.println(" cl alpha intermediate "  + intermediateAirfoil.getAerodynamics().get_clAlpha());
				System.out.println(" cl alpha tip " + airfoilTip.getAerodynamics().get_clAlpha());

				System.out.println(" alpha star kink " + airfoilKink.getAerodynamics().get_alphaStar());
				System.out.println(" alpha star intermediate "  + intermediateAirfoil.getAerodynamics().get_alphaStar());
				System.out.println(" alpha star tip " + airfoilTip.getAerodynamics().get_alphaStar());



				// -----------------------------------------------------------------------
				// Evaluate airfoil Lift curve
				// -----------------------------------------------------------------------

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("STARTING EVALUATE CL vs ALPHA CURVE OF AIRFOIL ");
				System.out.println("-----------------------------------------------------");

				String folderPathAirfoil = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
				String subfolderPathAirfoil = JPADStaticWriteUtils.createNewFolder(folderPathAirfoil + "CL_Airfoil" + File.separator);

				MyAirfoil airfoilPlot;
				airfoilPlot = intermediateAirfoil;

				double [] alphaArrayAirfoil = new double [40];
				double [] clArrayAirfoil = new double [40];

				alphaArrayAirfoil[1] = Amount.valueOf(-2, NonSI.DEGREE_ANGLE).getEstimatedValue();
				for (int i=1 ; i<alphaArrayAirfoil.length ; i++){
					alphaArrayAirfoil[i] = alphaArrayAirfoil[i-1] + Amount.valueOf(0.5, NonSI.DEGREE_ANGLE).getEstimatedValue();
				}
				System.out.println( " alpha array --> " + Arrays.toString(alphaArrayAirfoil));
				for (int i=0 ; i<clArrayAirfoil.length; i++){
					clArrayAirfoil[i] = airfoilPlot.getAerodynamics().calculateClAtAlpha(
							Amount.valueOf(toRadians(alphaArrayAirfoil[i]), SI.RADIAN).getEstimatedValue());

				} 
				System.out.println( " cl array --> " + Arrays.toString(clArrayAirfoil));

				//	    MyChartToFileUtils.plotNoLegend
				//		(alphaArrayAirfoil, clArrayAirfoil,-2.0, 20.0 ,
				//				-0.5,2.0, "alpha", "CL", "deg" , "", subfolderPathAirfoil, "CLalphaAirfoilRoot");

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("DONE ");
				System.out.println("-----------------------------------------------------");


				// -----------------------------------------------------------------------
				// Evaluate wing lift distribution
				// -----------------------------------------------------------------------

				System.out.println(" cl max root " + airfoilRoot.getAerodynamics().get_clMax());
				System.out.println(" cl max airfoil 0 " + theWing.get_theAirfoilsList().get(0).getAerodynamics().get_clMax());


				System.out.println(" cl max kink " + airfoilKink.getAerodynamics().get_clMax());
				System.out.println(" cl max airfoil 1 " + theWing.get_theAirfoilsList().get(1).getAerodynamics().get_clMax());

				System.out.println(" cl max tip " + airfoilTip.getAerodynamics().get_clMax());
				System.out.println(" cl max airfoil 2 " + theWing.get_theAirfoilsList().get(2).getAerodynamics().get_clMax());

				System.out.println(" y station " + Arrays.toString(theLSAnalysis.get_yStations()));



				// -----------------------------------------------------------------------
				// Evaluate effective angle of attack
				// -----------------------------------------------------------------------

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("STARTING EVALUATE EFFECTIVE ANGLE OF ATTACK ");
				System.out.println("-----------------------------------------------------");

				double[] alphaEffective;

				AlphaEffective theAlphaCalculator = new AlphaEffective(theLSAnalysis, theWing, theOperatingConditions);
				Amount<javax.measure.quantity.Angle> inputAngle = Amount.valueOf(toRadians(8.), SI.RADIAN);

				alphaEffective = theAlphaCalculator.calculateAlphaEffective(inputAngle);

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println(" alpha --> " + inputAngle);
				System.out.println(" alpha effective --> " + Arrays.toString(alphaEffective));

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("DONE");
				System.out.println("-----------------------------------------------------");


				// -----------------------------------------------------------------------
				// Evaluate CD
				// ----------------------------------------------------------------------- 
				//
				//				System.out.println("\n \n-----------------------------------------------------");
				//				System.out.println("STARTING EVALUATE CD ");
				//				System.out.println("-----------------------------------------------------");
				//
				//				// NB --> best practice to define an object CalcLiftDistribution
				//				LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSAnalysis.getCalculateLiftDistribution();
				//				calculateLiftDistribution.getNasaBlackwell().calculate(alphaFirst);
				//
				//				// calculation of the cd 
				//				LSAerodynamicsManager.CalcCDAtAlpha calculateCD =  theLSAnalysis.new CalcCDAtAlpha();
				//
				//				double CD = calculateCD.integralFromCdAirfoil(alphaFirst, MethodEnum.NASA_BLACKWELL, theLSAnalysis);
				//
				//				System.out.println(" CD of wing at alpha " + alphaFirst.to(NonSI.DEGREE_ANGLE) + " = " + CD);
				//
				//
				//				airfoilRoot.getAerodynamics().calculateCdAtAlpha(alphaFirst);
				//
				//				airfoilKink.getAerodynamics().plotPolar();
				//				airfoilKink.getAerodynamics().plotClvsAlpha();


				//	   double cdKink = calculateCd.calcCDatAlphaNasaBlackwell(alphaSecond, theLSAnalysis);
				//	   double cdKinkSchrenk = calculateCd.calcCDatAlphaSchrenk(alphaSecond, theLSAnalysis);
				//	  
				//	   System.out.println(" cd kink at alpha with Nasa Blackwell = " + alphaSecond.getEstimatedValue()* 57.3 + " deg = " + cdKink);
				//	   System.out.println(" cd kink at alpha with Schrenk = " + alphaSecond.getEstimatedValue()* 57.3 + " deg = " + cdKinkSchrenk);// output informations
				//	   
				//	   // plotting polar drag of airfoil
				//	   calculateCd.plotPolar(theLSAnalysis, MethodEnum.NASA_BLACKWELL);
				//	   calculateCd.plotPolar(theLSAnalysis, MethodEnum.SCHRENK);
				//	   
				//	   // calculation of the Cd Distribution
				//	   double [] cdDistribution;
				//	   LSAerodynamicsManager.CalcCdDistribution theCDDistribution = theLSAnalysis.new CalcCdDistribution();
				//	   cdDistribution = theCDDistribution.nasaBlackwell(alphaFirst, theLSAnalysis);
				//	   
				//	   String subfolderPathCD = JPADStaticWriteUtils.createNewFolder(folderPath + "CD_distribution" + File.separator);
				//	   
				//	   // plotting the CD distribution
				//	   MyChartToFileUtils.plotNoLegend(
				//				theLSAnalysis.get_yStationsND(),	cdDistribution, 
				//				0.0, 1.0, 0.0, 0.1,					    // axis with limits
				//				"eta", "Cd", "", "",	   				
				//				subfolderPathCD, "cd distribution alpha = " + alphaFirst.getEstimatedValue());	
				//	   
				//	   
				//	  // calculation of CD
				//	   
				//	   LSAerodynamicsManager.CalcCDAtAlpha theCDCalculator= theLSAnalysis.new CalcCDAtAlpha();
				//	   
				//	   theCDCalculator.integralFromCdAirfoil(alphaFirst, MethodEnum.NASA_BLACKWELL, theLSAnalysis);   


	}

}