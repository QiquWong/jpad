package sandbox.vt.HighLift_Test;

import static java.lang.Math.toRadians;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import sandbox.mr.WingCalculator;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class TestCleanWingCLmax {

	public static void main(String[] args) {

		Amount<Angle> deltaAlphaMax; 
		MyAirfoil meanAirfoil;

		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// Setup database(s)	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		//--------------------------------------------------------------------------------------
		// B747-100B
		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_altitude(Amount.valueOf(10000.0, SI.METER));
		theCondition.set_machCurrent(0.84);
		theCondition.calculate();
		
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.B747_100B);
		aircraft.set_name("B747-100B");

		LiftingSurface theWing = aircraft.get_wing();

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);
		
		//--------------------------------------------------------------------------------------
		// ATR-72
		//------------------------------------------------------------------------------------
//		OperatingConditions theCondition = new OperatingConditions();
//
//		Aircraft aircraft = Aircraft.createDefaultAircraft("ATR-72");
//		aircraft.set_name("ATR-72");
//
//		LiftingSurface theWing = aircraft.get_wing();
//
//		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
//		theAnalysis.updateGeometry(aircraft);

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

		//--------------------------------------------------------------------------------------
		// ATR-72
		//--------------------------------------------------------------------------------------
		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
//		CenterOfGravity cgMTOM = new CenterOfGravity();
//
//		// x_cg in body-ref.-frame
//		cgMTOM.set_xBRF(Amount.valueOf(12.0, SI.METER));
//		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
//		cgMTOM.set_zBRF(Amount.valueOf(2.3, SI.METER));
//
//		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
//		aircraft.get_HTail().calculateArms(aircraft);
//		aircraft.get_VTail().calculateArms(aircraft);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theCondition,
				theWing,
				aircraft
				);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);

		theAnalysis.doAnalysis(aircraft,AnalysisTypeEnum.AERODYNAMIC);

		theLSAnalysis.setHighLiftDatabaseReader(highLiftDatabaseReader);
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
		System.out.println("CL max --> " + airfoilKink.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink " + airfoilKink.getGeometry().get_deltaYPercent());

		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTip.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());

		// -----------------------------------------------------------------------
		// Assign airfoil
		// -----------------------------------------------------------------------

		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry();
		aircraft.get_exposedWing().set_theAirfoilsList(myAirfoilList);
		aircraft.get_exposedWing().updateAirfoilsGeometryExposedWing( aircraft);

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
		System.out.println("\n \tEvaluate CL max using CL distribution");
		theCLmaxAnalysis.nasaBlackwell();
		Amount<Angle> alphaAtCLMax = theLSAnalysis.get_alphaStall();
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
//		Amount<Angle> alphaFirst = Amount.valueOf(toRadians(2), SI.RADIAN);
//		MyArray clAlphaFirst = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL , alphaFirst);
//		System.out.println("CL distribution at alpha " + alphaFirst + " --> " + clAlphaFirst );
//
//		Amount<Angle> alphaSecond = Amount.valueOf(toRadians(6), SI.RADIAN);
//		MyArray clAlphaSecond = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL , alphaSecond);
//		System.out.println("CL distribution at alpha " + alphaSecond + " --> " + clAlphaSecond );

		MyArray clAlphaThird = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL ,alphaAtCLMax);
		System.out.println("CL distribution at alpha " + alphaAtCLMax + " --> " + clAlphaThird );

		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL_Wing" + File.separator);

		double [][] semiSpanAd = {theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};

		double [][] clDistribution = {clMaxAirfoil.getRealVector().toArray(),
//				clAlphaFirst.getRealVector().toArray(),
//				clAlphaSecond.getRealVector().toArray(),
				clAlphaThird.getRealVector().toArray()};

		String [] legend = new String [4];
		legend[0] = "CL max airfoil";
//		legend[1] = "CL distribution at alpha " + Math.toDegrees(alphaFirst.getEstimatedValue());
//		legend[2] = "CL distribution at alpha " + Math.toDegrees(alphaSecond.getEstimatedValue());
		legend[1] = "CL distribution at alpha " + Math.toDegrees(alphaAtCLMax.getEstimatedValue());	

		MyChartToFileUtils.plot(
				semiSpanAd,	clDistribution, // array to plot
				0.0, 1.0, 0.0, 2.0,			// axis with limits
				"eta", "CL", "", "",	    // label with unit
				legend,						// legend
				subfolderPath, "Stall ");	// output informations

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
		System.out.println("the LE sweep angle is " +  theWing.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE));
		deltaAlphaMax = Amount.valueOf(toRadians (theLSAnalysis.get_AerodynamicDatabaseReader().getD_Alpha_Vs_LambdaLE_VsDy(theWing.get_sweepLEEquivalent().to(NonSI.DEGREE_ANGLE).getEstimatedValue() ,
				meanLESharpnessParameter )), SI.RADIAN);
		System.out.println("Delta  alpha max " + deltaAlphaMax);
		Amount<Angle> alphaAtCLMaxNew =  Amount.valueOf((alphaAtCLMax.getEstimatedValue() + deltaAlphaMax.getEstimatedValue()), SI.RADIAN);
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
		// Evaluate wing lift distribution
		// -----------------------------------------------------------------------

		System.out.println(" cl max root " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println(" cl max airfoil 0 " + theWing.get_theAirfoilsList().get(0).getAerodynamics().get_clMax());

		System.out.println(" cl max kink " + airfoilKink.getAerodynamics().get_clMax());
		System.out.println(" cl max airfoil 1 " + theWing.get_theAirfoilsList().get(1).getAerodynamics().get_clMax());

		System.out.println(" cl max tip " + airfoilTip.getAerodynamics().get_clMax());
		System.out.println(" cl max airfoil 2 " + theWing.get_theAirfoilsList().get(2).getAerodynamics().get_clMax());

		System.out.println(" y station " + Arrays.toString(theLSAnalysis.get_yStations()));

	}
}
