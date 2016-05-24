package sandbox.mr;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;


import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAerodynamics;
import aircraft.auxiliary.airfoil.MyGeometry;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.NasaBlackwell;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcLiftDistribution;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import igeo.IVec2R.Angle;
import javafx.util.Pair;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Test_MR_CLalpha {


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------

		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL vs Alpha evaluation" + File.separator);


		// -----------------------------------------------------------------------
		// Generate default Wing
		// -----------------------------------------------------------------------


		// Wing
		double xAw = 11.0; //meter 
		double yAw = 0.0;
		double zAw = 1.6;
		double iw = 0.0;
		LiftingSurface2Panels theWing = new LiftingSurface2Panels(
				"Wing", // name
				"Data from AC_ATR_72_REV05.pdf", 
				xAw, yAw, zAw, iw, 
				ComponentEnum.WING
				); 


		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();


		// Center of Gravity
		double xCgLocal= 1.5; // meter 
		double yCgLocal= 0;
		double zCgLocal= 0;

		CenterOfGravity cg = new CenterOfGravity(
				Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
				Amount.valueOf(yCgLocal, SI.METER),
				Amount.valueOf(zCgLocal, SI.METER),
				Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
				Amount.valueOf(yAw, SI.METER),
				Amount.valueOf(zAw, SI.METER),
				Amount.valueOf(0.0, SI.METER),// origin of BRF
				Amount.valueOf(0.0, SI.METER),
				Amount.valueOf(0.0, SI.METER)
				);

		cg.calculateCGinBRF();
		theWing.set_cg(cg);

		// -----------------------------------------------------------------------
		// Operating Condition
		// -----------------------------------------------------------------------

		OperatingConditions theOperatingConditions = new OperatingConditions();				

		
		// -----------------------------------------------------------------------
		// Set Wing Data
		// -----------------------------------------------------------------------

		theWing.set_surface(Amount.valueOf(75.5,SI.SQUARE_METRE));
		theWing.set_chordRoot(Amount.valueOf(2.92194, SI.METER));
		theWing.set_chordKink(Amount.valueOf(2.92194, SI.METER));
		theWing.set_chordRoot(Amount.valueOf(2.92194, SI.METER));
		theWing.set_aspectRatio(12.0);
		theWing.set_spanStationKink(0.33);
		theWing.set_taperRatioCrankedWing(0.57);
		theWing.set_iw(Amount.valueOf(0.0,SI.RADIAN));
		theWing.set_sweepQuarterChordEq(Amount.valueOf(Math.toRadians(2.0), SI.RADIAN));
		theWing.set_twistTip( Amount.valueOf(Math.toRadians(0.0),SI.RADIAN));
		theWing.set_twistKink( Amount.valueOf(Math.toRadians(0.0),SI.RADIAN));
		theWing.set_tc_root(0.18);
		theWing.set_tc_kink(0.18);
		theWing.set_tc_tip(0.136);
		
		
		System.out.println("\n\n-----------------------------");
		System.out.println("|          WING             |");
		System.out.println("-----------------------------");

		System.out.println("\n\nRoot Chord " + theWing.get_chordRoot());
		System.out.println("Root Kink " + theWing.get_chordRoot());
		System.out.println("Tip Chord " + theWing.get_chordTip());
		System.out.println("Aspect Ratio " + theWing.get_aspectRatio());
	
		
		// -----------------------------------------------------------------------
		// Set Airfoils Data
		// -----------------------------------------------------------------------

		
		System.out.println("\n\n-----------------------------");
		System.out.println("|        AIRFOILS           |");
		System.out.println("-----------------------------");
		
		
		MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
		airfoilRoot.getGeometry().set_deltaYPercent(5.5);
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18);
		airfoilRoot.getAerodynamics().set_alphaZeroLift( Amount.valueOf(Math.toRadians(-4.6), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clAlpha(6.48);
		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(9),SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clStar(1.53);
		airfoilRoot.getAerodynamics().set_alphaStall( Amount.valueOf(Math.toRadians(18.0), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clMax(1.9);
		System.out.println("\n \n \t ROOT \n");
		System.out.println("Maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());	
		System.out.println("alpha max --> " + airfoilRoot.getAerodynamics().get_alphaStall().to(NonSI.DEGREE_ANGLE));			
		System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());


		MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
		airfoilKink.getGeometry().set_deltaYPercent(5.5);
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18);
		airfoilKink.getAerodynamics().set_alphaZeroLift( Amount.valueOf(Math.toRadians(-4.6), SI.RADIAN));
		airfoilKink.getAerodynamics().set_clAlpha(6.48);
		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(9),SI.RADIAN));
		airfoilKink.getAerodynamics().set_clStar(1.53);
		airfoilKink.getAerodynamics().set_alphaStall( Amount.valueOf(Math.toRadians(18.0), SI.RADIAN));
		airfoilKink.getAerodynamics().set_clMax(1.9);
		System.out.println("\n \n \t KINK \n");
		System.out.println("Maximum thickness " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilKink.getAerodynamics().get_clMax());	
		System.out.println("alpha max --> " + airfoilKink.getAerodynamics().get_alphaStall().to(NonSI.DEGREE_ANGLE));			
		System.out.println("LE sharpness parameter Root " + airfoilKink.getGeometry().get_deltaYPercent());
		

		MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);
		airfoilTip.getGeometry().set_deltaYPercent(4.5);
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.136);
		airfoilTip.getAerodynamics().set_alphaZeroLift( Amount.valueOf(Math.toRadians(-3.68), SI.RADIAN));
		airfoilTip.getAerodynamics().set_clAlpha(6.72);
		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(7.5),SI.RADIAN));
		airfoilTip.getAerodynamics().set_clStar(1.31);
		airfoilTip.getAerodynamics().set_alphaStall( Amount.valueOf(Math.toRadians(17.5), SI.RADIAN));
		airfoilTip.getAerodynamics().set_clMax(2.1);
		System.out.println("\n \n \t TIP \n");
		System.out.println("Maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTip.getAerodynamics().get_clMax());	
		System.out.println("alpha max --> " + airfoilTip.getAerodynamics().get_alphaStall().to(NonSI.DEGREE_ANGLE));			
		System.out.println("LE sharpness parameter Root " + airfoilTip.getGeometry().get_deltaYPercent());


		// -----------------------------------------------------------------------
		// Set Operating Condition Data
		// -----------------------------------------------------------------------


		theOperatingConditions.set_machCurrent(0.23);

		
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Operating condition");
		System.out.println("-----------------------------------------------------");
		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		System.out.println("\tAltitude: " + theOperatingConditions.get_altitude());
		System.out.println("----------------------");

		
		

		// -----------------------------------------------------------------------
		// Update Data
		// -----------------------------------------------------------------------
		theWing.calculateGeometry();
	    theWing.getGeometry().calculateAll();
	    theWing.updateAirfoilsGeometry();
		
		
		
		// allocate manager
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
				theOperatingConditions,
				theWing
				);

		theWing.setAerodynamics(theLSAnalysis);
		theLSAnalysis.initializeDependentData();
		

		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();

		//--------------------------------------------------------------------------------------
		// Set databases

		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);


		//------------- CL CALCULATORS
		
		// Airfoils
		
		airfoilRoot.getAerodynamics().plotClvsAlpha(subfolderPath, "Root");
		airfoilKink.getAerodynamics().plotClvsAlpha(subfolderPath, "Kink");
		airfoilTip.getAerodynamics().plotClvsAlpha(subfolderPath, "Tip");
		

		// Wing 
		  
		double [] alphaArrayTemp = {0,
				8,
				16,
				18,
				20,
				22,
				24};
		
		MyArray alphaArrayActual = new MyArray();
		
		
		
		for (int i=0; i<alphaArrayTemp.length;i++){
			alphaArrayActual.set(i, Math.toRadians(alphaArrayTemp[i]));}
			
		// stall path
		
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();

		double [] cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurveArray(alphaArrayActual, false);
		
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("WRITING TO CHART CL vs ALPHA CURVE stall path");
		System.out.println("-----------------------------------------------------");
		
		MyChartToFileUtils.plotNoLegend(
				alphaArrayTemp , cLWingCleanArray,
				null, null, null, null,
				"alpha", "CL",
				"deg", "",
				subfolderPath," CL vs Alpha stall path " );
		
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("DONE");
		System.out.println("-----------------------------------------------------");
		
		
		// Modified Stall Path 
		
		
			
		double [] cLWing = LiftCalc.calculateCLArraymodifiedStallPath(alphaArrayActual, theWing);
	
//		System.out.println(" alpha Array " + Arrays.toString(alphaArray) );
//		System.out.println(" cl "+  Arrays.toString(cLWing));

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("WRITING TO CHART CL vs ALPHA CURVE stall path mod.");
		System.out.println("-----------------------------------------------------");
		
		MyChartToFileUtils.plotNoLegend(
				alphaArrayTemp , cLWing,
				null, null, null, null,
				"alpha", "CL",
				"deg", "",
				subfolderPath," CL vs Alpha from modified stall path " );
		
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("DONE");
		System.out.println("-----------------------------------------------------");		
		

		// cl max
		
		double clMaxPhilipps = theLSAnalysis.getCalculateCLMaxClean().phillipsAndAlley();
		
		System.out.println(" cl max philips " + clMaxPhilipps); //1.91
		
		
		
		// stall path
		
		System.out.println("\n-------------------------------------");
		System.out.println("\t \t \tWRITING STALL PATH CHART TO FILE  ");

		LSAerodynamicsManager.CalcCLMaxClean theCLmaxAnalysis = theLSAnalysis.new CalcCLMaxClean();
		MyArray clMaxAirfoil = theCLmaxAnalysis.getClAirfoils();
		Amount<javax.measure.quantity.Angle> alphaWingStall = theLSAnalysis.get_alphaStall();
		double alphaSecond = theLSAnalysis.getAlphaArray().get(3);
		double alphaThird = theLSAnalysis.getAlphaArray().get(6);
		MyArray clAlphaStall = theLSAnalysis.getcLMap()
				.getCxyVsAlphaTable()
				.get(MethodEnum.NASA_BLACKWELL ,alphaWingStall);
		MyArray clSecond = theLSAnalysis.getcLMap()
				.getCxyVsAlphaTable()
				.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaSecond, SI.RADIAN));
		MyArray clThird = theLSAnalysis.getcLMap()
				.getCxyVsAlphaTable()
				.get(MethodEnum.NASA_BLACKWELL ,Amount.valueOf(alphaThird, SI.RADIAN));

		double [][] semiSpanAd = {
				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND(),
				theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};

		double [][] clDistribution = {
				clMaxAirfoil.getRealVector().toArray(),
				clSecond.getRealVector().toArray(),
				clThird.getRealVector().toArray(),
				clAlphaStall.getRealVector().toArray()};

		String [] legend = new String [4];
		legend[0] = "CL max airfoil";
		legend[1] = "CL distribution at alpha " 
				+ Math.toDegrees( alphaSecond);
		legend[2] = "CL distribution at alpha " 
				+ Math.toDegrees( alphaThird);
		legend[3] = "CL distribution at alpha " 
				+ Math.toDegrees( alphaWingStall.getEstimatedValue());

		MyChartToFileUtils.plot(
				semiSpanAd,	clDistribution, // array to plot
				0.0, 1.0, 0.0, 2.6,					    // axis with limits
				"eta", "CL", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "Stall Path of Wing ");			    // output informations

		System.out.println("\t \t \tDONE  ");
	}
}