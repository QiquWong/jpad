// This is the Test class for Longitudinal Stability. The object of analysis is an aircraft.
//
// The reference angle of attack is alphaBody, that is the angle between the direction of asimptotic
// velocity and the reference line of fuselage. So, for each component is necessary to evaluate the
// aerodynamic characteristics, such as lift, drag and moment, having alphaBody as input.
// Moreover for each component are drawn the aerodynamic curves in function of local angle of attack.

// alphaWing = alphaBody + iWing
// The angle of incidence is defined as the angle between the chord line and a reference line of the fuselage
// alphaHorizontal = alphaBody - downwashAngle +iHorizontal

// So alphaBody is the input data, iWing and iHorizontal are geometry data and downwashAngle must be calculated


package sandbox.mr;

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
import aircraft.components.fuselage.FusAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import functions.Linspace;
import javafx.util.Pair;
import sandbox.mr.MyStabilityCalculator.CalcPitchingMomentAC;
import sandbox.mr.WingCalculator.MeanAirfoil;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Test_MR_07_LongitudinalStability_TurboFan {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {

		System.out.println("------------------------------------");
		System.out.println("\n Longitudinal Stability Test ");
		System.out.println("\n------------------------------------");

		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------


		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability_TurboFan" + File.separator);

		//----------------------------------------------------------------------------------
		// Default folders creation:

		MyConfiguration.initWorkingDirectoryTree();


		//------------------------------------------------------------------------------------
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.B747_100B);
		aircraft.set_name("B747-100B");
		System.out.println("\nDefault aircraft: " + aircraft.get_name() + "\n");


		//------------------------------------------------------------------------------------
		// Operating Condition

		OperatingConditions theConditions = new OperatingConditions();
		theConditions.set_alphaCurrent(Amount.valueOf(toRadians(2.), SI.RADIAN));
		theConditions.set_altitude(Amount.valueOf(10000.0, SI.METER));
		theConditions.set_machCurrent(0.84);
		theConditions.calculate();
		System.out.println("\n OPERATING CONDITIONS: ");
		System.out.println("Altitude " + theConditions.get_altitude());
		System.out.println("Mach number " + theConditions.get_machCurrent()+"\n");



		//------------------------------------------------------------------------------------
		// Wing and Tail
		LiftingSurface theWing = aircraft.get_wing();
		LiftingSurface horizontalTail = aircraft.get_HTail();


		//--------------------------------------------------------------------------------------
		// Angle of attack

		Amount<Angle> alphaBody = theConditions.get_alphaCurrent();
		Amount<Angle> alphaWing = Amount.valueOf(alphaBody.getEstimatedValue()+theWing.get_iw().getEstimatedValue(), SI.RADIAN);
		System.out.println(" alpha body = " + alphaBody.to(NonSI.DEGREE_ANGLE));


		//--------------------------------------------------------------------------------------
		// Aerodynamic managers
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theConditions);
		theAnalysis.updateGeometry(aircraft);
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theConditions,
				theWing,
				aircraft
				);

		aircraft.get_wing().setAerodynamics(theLSAnalysis);


		//--------------------------------------------------------------------------------------
		// Set databases
		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);


		//--------------------------------------------------------------------------------------
		FusAerodynamicsManager theFuselageManager = new FusAerodynamicsManager(theConditions, aircraft);



		//--------------------------------------------------------------------------------------
		// Define airfoils

		System.out.println("\nWING AIRFOILS:");

		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
		System.out.println("Root Chord [m] = " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness = " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Root = " + airfoilRoot.getGeometry().get_deltaYPercent());


		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("Kink Station [m] = " + yLocKink);
		System.out.println("Kink Chord [m] = " + theWing.get_chordKink().getEstimatedValue() );
		System.out.println("Kink maximum thickness = " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilKink.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink = " + airfoilKink.getGeometry().get_deltaYPercent());


		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord [m] = " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness = " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTip.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip = " + airfoilTip.getGeometry().get_deltaYPercent()+ "\n");


		//--------------------------------------------------------------------------------------
		// Assign airfoil

		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry();
		theLSAnalysis.initializeDependentData();
		aircraft.get_exposedWing().set_theAirfoilsList(myAirfoilList);
		aircraft.get_exposedWing().updateAirfoilsGeometryExposedWing( aircraft);


		//--------------------------------------------------------------------------------------
		//Horizontal Tail

		System.out.println("\nHORIZONTAL TAIL AIRFOILS:");

		double yLocRootH = 0.0;
		MyAirfoil airfoilRootHorizontalTail = new MyAirfoil(
				horizontalTail, yLocRootH, "0012");
		airfoilRootHorizontalTail.getGeometry().update(yLocRootH);  // define chord
		airfoilRootHorizontalTail.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

		double yLocTipH = aircraft.get_HTail().get_semispan().getEstimatedValue();
		MyAirfoil airfoilTipHorizontalTail = new MyAirfoil(
				horizontalTail, yLocTipH, "0012");
		airfoilTipHorizontalTail.getGeometry().update(yLocTipH);  // define chord
		airfoilTipHorizontalTail.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

		List<MyAirfoil> myAirfoilListHorizontalTail = new ArrayList<MyAirfoil>();
		myAirfoilListHorizontalTail.add(0, airfoilRootHorizontalTail);
		myAirfoilListHorizontalTail.add(1, airfoilTipHorizontalTail);
		horizontalTail.set_theAirfoilsList(myAirfoilListHorizontalTail);
		horizontalTail.updateAirfoilsGeometry();

		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRootHorizontalTail.get_family());
		System.out.println("Root Chord [m] = " + horizontalTail.get_chordRoot() );
		System.out.println("Root maximum thickness = " + airfoilRootHorizontalTail.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRootHorizontalTail.getAerodynamics().get_clMax());

		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilRootHorizontalTail.get_family());
		System.out.println("Root Chord [m] = " + horizontalTail.get_chordTip());
		System.out.println("Root maximum thickness = " + airfoilTipHorizontalTail.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTipHorizontalTail.getAerodynamics().get_clMax());


		LSAerodynamicsManager theLSHorizontalTail = new LSAerodynamicsManager(
				theConditions,
				horizontalTail,
				aircraft);


		aircraft.get_HTail().setAerodynamics(theLSHorizontalTail);

		theLSHorizontalTail.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);


		//theLSHorizontalTail.initializeDependentData();


		//--------------------------------------------------------------------------------------
		// Mean Airfoil

		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator = theLSAnalysis.new MeanAirfoil();
		MyAirfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theWing);



		//--------------------------------------------------------------------------------------
		// Aerodynamic Analysis

		//		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		//		CenterOfGravity cgMTOM = new CenterOfGravity();
		//
		//		// x_cg in body-ref.-frame
		//		cgMTOM.set_xBRF(Amount.valueOf(23.1, SI.METER));
		//		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		//		cgMTOM.set_zBRF(Amount.valueOf(0.0, SI.METER));
		//
		//		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		//		aircraft.get_HTail().calculateArms(aircraft);
		//		aircraft.get_VTail().calculateArms(aircraft);
		//
		//		theAnalysis.doAnalysis(aircraft,
		//				AnalysisTypeEnum.AERODYNAMIC
		//				);
		//
		//
		//--------------------------------------------------------------------------------------
		// Exposed Wing

		System.out.println("\n\nEXPOSED WING:");
		double yLocRootExposed = aircraft.get_exposedWing().get_theAirfoilsListExposed().get(0).getGeometry().get_yStation();
		System.out.println(" Root station exposed wing (m) = " + yLocRootExposed);
		System.out.println(" Kink station exposed wing (m) = " + theWing.get_theAirfoilsList().get(1).getGeometry().get_yStation());
		System.out.println(" Wing span (m) = " + theWing.get_span().getEstimatedValue());
		System.out.println(" Exposed wing span (m) = "  + aircraft.get_exposedWing().get_span().getEstimatedValue());

		LSAerodynamicsManager.CalcAlpha0L theAlphaZeroLiftCalculator = theLSAnalysis.new CalcAlpha0L();
		Amount<Angle> alpha0LExposedWing = theAlphaZeroLiftCalculator.integralMeanExposedWithTwist();
		Amount<Angle> alpha0LWing = theAlphaZeroLiftCalculator.integralMeanNoTwist();

		System.out.println("\nAlpha Zero Lift Exposed Wing (deg) = " + alpha0LExposedWing.to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		System.out.println("Alpha Zero Lift Wing (deg) = " + alpha0LWing.to(NonSI.DEGREE_ANGLE).getEstimatedValue());



		//--------------------------------------------------------------------------------------
		// Data

		double chordRatio = 0.3;
		double deflectionElevator = 20.0; //deg
		aircraft.get_HTail().getAerodynamics().set_dynamicPressureRatio(0.85);  // h tail is in fuselage

		List<Double[]> deltaFlap = new ArrayList<Double[]>();
		List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
		List<Double> eta_in_flap = new ArrayList<Double>();
		List<Double> eta_out_flap = new ArrayList<Double>();
		List<Double> cf_c = new ArrayList<Double>();

		Double[] deltaFlapDouble =  new Double [1];
		deltaFlapDouble[0] = deflectionElevator;

		deltaFlap.add(deltaFlapDouble);
		flapType.add(FlapTypeEnum.PLAIN);
		eta_in_flap.add(0.0);
		eta_out_flap.add(horizontalTail.get_semispan().getEstimatedValue());
		cf_c.add(chordRatio);

		// -----------------------------------------------------------------------
		// LIFT CHARACTERISTICS
		// -----------------------------------------------------------------------

		//		 Considering an angle of attack alphaBody, the aircraft components generates a lift. So
		//		 it is necessary to evaluate these contributes. In particular, first of all, is necessary to
		//		 evaluate the CL of isolated wing. This value will be correct with fuselage influence.
		//		 Afterwards it is necessary to evaluate the contribute of horizontal tail.
		//		 It is important to note that each component works at a different angle of attack, meanwhile
		//		 for longitudinal stability the reference angle of attack is alphaBody.

		System.out.println("\n\n------------------------------------");
		System.out.println("\n LIFT CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");


		// ------------------Wing---------------
		double cLIsolatedWing;
		double cLAlphaWingBody;

		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");
		System.out.println("\n \t Data: ");
		System.out.println("Angle of attack alpha body (deg) = " + Math.ceil(alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()));
		System.out.println("Angle of incidence of wing (deg) = " +  Math.ceil(theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()));


		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate stall path of wing");
		System.out.println("-----------------------------------------------------");


		LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = theLSAnalysis.new CalcCLAtAlpha();
		cLIsolatedWing = theCLWingCalculator.nasaBlackwellAlphaBody(alphaBody);

		theLSAnalysis.PlotCLvsAlphaCurve(subfolderPath);
		System.out.println("-------------------------------------");
		System.out.println("CL of Isolated wing at alpha body = " + cLIsolatedWing);
		System.out.println("\n \t \t \tDONE PLOTTING CL VS ALPHA CURVE  ");

		// -----------------------------------------------------------------------
		// Using NASA-Blackwell method in order to estimate the lifting surface CLmax
		// -----------------------------------------------------------------------


		LSAerodynamicsManager.CalcCLMaxClean theCLmaxAnalysis = theLSAnalysis.new CalcCLMaxClean(); //is nested
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLAnalysis = theLSAnalysis.new CalcCLvsAlphaCurve();
		theCLAnalysis.nasaBlackwell();
		theCLmaxAnalysis.nasaBlackwell();
		Amount<javax.measure.quantity.Angle> alphaAtCLMax = theLSAnalysis.get_alphaStall();
		double clMax = theCLWingCalculator.nasaBlackwell(alphaAtCLMax);


		// PLOT

		System.out.println("\n \n \t \t WRITING CHART TO FILE. Stall path. ");


		// interpolation of CL MAX_airfoil
		MyArray clMaxAirfoil = theCLmaxAnalysis.getClAirfoils();

		MyArray clAlphaThird = theLSAnalysis.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL ,alphaAtCLMax);

		double [][] semiSpanAd = {theLSAnalysis.get_yStationsND(), theLSAnalysis.get_yStationsND()};

		double [][] clDistribution = {clMaxAirfoil.getRealVector().toArray(), clAlphaThird.getRealVector().toArray()};
		String [] legend = new String [4];
		legend[0] = "CL max airfoil";
		legend[1] = "CL distribution at alpha " + Math.toDegrees( alphaAtCLMax.getEstimatedValue());

		MyChartToFileUtils.plot(
				semiSpanAd,	clDistribution, // array to plot
				0.0, 1.0, 0.0, 2.0,					    // axis with limits
				"eta", "CL", "", "",	    // label with unit
				legend,					// legend
				subfolderPath, "Stall Path of Wing ");			    // output informations

		System.out.println("-----------------------------------------------------");
		System.out.println("\t \t DONE ");


		// ------------------Fuselage---------------

		System.out.println("\n ------------------- ");
		System.out.println("|     FUSELAGE       |");
		System.out.println(" ------------------- \n\n");

		double cLAlphaWing = theLSAnalysis.getcLLinearSlopeNB();
		cLAlphaWingBody = theFuselageManager.calculateCLAlphaFuselage(cLAlphaWing);

		theWing.getAerodynamics().calcAlphaAndCLMax(meanAirfoil);
		double cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody, meanAirfoil, true);
		System.out.println("-------------------------------------");
		System.out.println(" CL of Wing Body at alpha body = " + cLWingBody);

		aircraft.get_theAerodynamics().PlotCLvsAlphaCurve(meanAirfoil, subfolderPath);
		System.out.println("\n \t \t \tDONE PLOTTING CL VS ALPHA CURVE  ");




		// ------------------Downwash---------------

		System.out.println("\n ------------------- ");
		System.out.println("|  HORIZONTAL TAIL   |");
		System.out.println(" ------------------- \n\n");

		System.out.println("\n-----Start of downwash calculation-----\n" );

		DownwashCalculator theDownwashCalculator = new DownwashCalculator(aircraft);

		theDownwashCalculator.calculateDownwashNonLinearDelft();

		theDownwashCalculator.plotDownwashDelftWithPath(subfolderPath);
		theDownwashCalculator.plotDownwashGradientDelftWithPath(subfolderPath);
		theDownwashCalculator.plotZDistanceWithPath(subfolderPath);
		System.out.println("\n\n\t\t\tDONE PLOTTING DOWNWASH ANGLE vs ALPHA BODY");

		double downwash = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);
		Amount<Angle> downwashAmountRadiant = Amount.valueOf(Math.toRadians(downwash), SI.RADIAN);
		System.out.println( "\nAt alpha " + alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + " (deg) the downwash angle is (deg) = " + downwash );

		double alphaTail =  alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ horizontalTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue()- downwash ;
		Amount<Angle> angleTailAmount =  Amount.valueOf(Math.toRadians(alphaTail), SI.RADIAN);


		// ------------------Horizontal Tail---------------

		System.out.println("\n \n-----------angles-------------- ");
		System.out.println("Angle of attack alpha body (deg) = " + Math.ceil(alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()));
		System.out.println("Angle of incidence of horizontal tail (deg) " + horizontalTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		System.out.println("Downwash Angle at Alpha Body (deg) " + downwash );

		double angleHorizontalDouble = alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				- downwash +  horizontalTail.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		Amount<Angle> alphaHorizontalTail = Amount.valueOf(Math.toRadians(angleHorizontalDouble), SI.RADIAN);
		System.out.println("Angle of Attack of Horizontal Tail (deg) "
				+ alphaHorizontalTail.to(NonSI.DEGREE_ANGLE).getEstimatedValue());

		LSAerodynamicsManager.CalcCLAtAlpha theCLHorizontalTailCalculator =
				theLSHorizontalTail
				.new CalcCLAtAlpha();


		// -----------------------------------------------------------------------
		// Using NASA-Blackwell method in order to estimate the lifting surface CLmax
		// -----------------------------------------------------------------------

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate stall path of horiontal tail");
		System.out.println("-----------------------------------------------------");


		LSAerodynamicsManager.CalcCLMaxClean theCLmaxHTailAnalysis = theLSHorizontalTail.new CalcCLMaxClean(); //is nested
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLHTailAnalysis = theLSHorizontalTail.new CalcCLvsAlphaCurve();
		theCLHTailAnalysis.nasaBlackwell();
		theCLmaxHTailAnalysis.nasaBlackwell();
		Amount<javax.measure.quantity.Angle> alphaHTailAtCLMax = theLSHorizontalTail.get_alphaStall();
		double clMaxHtail = theCLHorizontalTailCalculator.nasaBlackwell(alphaHTailAtCLMax);


		// PLOT

		System.out.println("\n \n \t \t WRITING CHART TO FILE. Stall path. ");
		System.out.println("-----------------------------------------------------");


		// interpolation of CL MAX_airfoil
		MyArray clMaxAirfoilHtail = theCLmaxHTailAnalysis.getClAirfoils();

		MyArray clAlphaThirdHTail = theLSHorizontalTail.getcLMap().getCxyVsAlphaTable().get(MethodEnum.NASA_BLACKWELL ,alphaHTailAtCLMax);

		double [][] semiSpanAdHTail = {theLSHorizontalTail.get_yStationsND(), theLSHorizontalTail.get_yStationsND()};

		double [][] clDistributionHtail = {clMaxAirfoilHtail.getRealVector().toArray(), clAlphaThirdHTail.getRealVector().toArray()};
		String [] legendHtail = new String [4];
		legendHtail[0] = "CL max airfoil";
		legendHtail[1] = "CL distribution at alpha " + Math.toDegrees( alphaHTailAtCLMax.getEstimatedValue());

		MyChartToFileUtils.plot(
				semiSpanAdHTail, clDistributionHtail, // array to plot
				0.0, 1.0, 0.0, null,					    // axis with limits
				"eta", "CL", "", "",	    // label with unit
				legendHtail,					// legend
				subfolderPath, "Stall Path of Horizontal Tail ");			    // output informations

		System.out.println("-----------------------------------------------------");
		System.out.println("\t \t DONE ");


		// CL calculation

		double cLHorizontalTail = theCLHorizontalTailCalculator.nasaBlackwellalphaBody(alphaBody, downwashAmountRadiant);

		System.out.println("CL of horizontal tail at alpha body = " + cLHorizontalTail);

		theLSHorizontalTail.PlotCLvsAlphaCurve(subfolderPath);
		LSAerodynamicsManager.CalcAlpha0L theAlphaZeroLiftCalculatorTail = theLSHorizontalTail.new CalcAlpha0L();
		Amount<Angle> alpha0LTail = theAlphaZeroLiftCalculatorTail.integralMeanNoTwist();

		System.out.println("\n\n\t\t\tDONE PLOTTING CL vs ALPHA HORIZONTAL TAIL");



		// TAU

		System.out.println("\n-----START OF TAU CALCULATION-----\n" );

		MyStabilityCalculator theStablityCalculator = new MyStabilityCalculator();

		Amount<Angle> deflection;
		int nValueDelta = 7;

		double[] deflectionArray = MyArrayUtils.linspace(0.0, 30.0, nValueDelta);
		double[] tau = new double [deflectionArray.length];

		for ( int i=0 ; i<deflectionArray.length ; i++ ){
			deflection = Amount.valueOf(deflectionArray[i], NonSI.DEGREE_ANGLE);
			tau[i] = theStablityCalculator.calculateTauIndex(chordRatio, aircraft, deflection);

			System.out.println("\n For an elevator deflection of " + deflection.getEstimatedValue() +
					" deg, the tau parameter is " + tau[i] );
		}

		// Plot

		Double [] cLVector = new Double[2];
		double [] cLVectorTemp = new double[2];
		Double [] alphaVector = new Double[2];
		List<Double[]> cLListPlot = new ArrayList<Double[]>();
		List<Double[]> alphaListPlot = new ArrayList<Double[]>();

		// first value
		double [] cLPlot = theLSHorizontalTail.get_cLArrayPlot();
		double [] alphaPlot = theLSHorizontalTail.get_alphaArrayPlot();
		Double [] cLPlotDouble = new Double [cLPlot.length];
		Double [] alphaPlotDouble = new Double [alphaPlot.length];

		for ( int k=0 ; k< cLPlot.length ; k++){
			cLPlotDouble[k] = (Double)cLPlot[k];
			alphaPlotDouble[k] = (Double)alphaPlot[k];
		}
		cLListPlot.add(cLPlotDouble);
		alphaListPlot.add(alphaPlotDouble);

		for (int j=1 ; j<nValueDelta ; j++ ){
			cLVectorTemp = theCLHorizontalTailCalculator.calculateCLWithElevatorDeflection(
					deltaFlap,
					flapType,
					null,
					eta_in_flap,
					eta_out_flap,
					null,
					null,
					cf_c,
					null,
					null,
					null);
			cLVector[0] = (Double)cLVectorTemp[0];
			cLVector[1] = (Double)cLVectorTemp[1];
			cLListPlot.add(cLVector);

			alphaVector = theCLHorizontalTailCalculator.getAlphaTailArrayDouble();
			alphaListPlot.add(alphaVector);
		}

		List<String> legendStall  = new ArrayList<>();
		legendStall.add("clean");

		for (int j=1 ; j<nValueDelta ; j++){
			legendStall.add("delta = (deg) " + deflectionArray[j]);
		}

		MyChartToFileUtils.plotJFreeChart(alphaListPlot,
				cLListPlot,
				"CL vs alpha",
				"alpha",
				"CL",
				-12.0, 22.0, 0.0,null,
				"deg",
				"",
				true,
				legendStall,
				subfolderPath,
				"CL alpha Horizontal Tail with Elevator");

		System.out.println("\n\n\t\t\tDONE PLOTTING CL vs ALPHA HORIZONTAL TAIL WITH ELEVATOR DEFLECTION");



		// ------------------Complete Aircraft---------------

		System.out.println("\n-----Complete Aircraft-----\n" );

		double etaRatio = 1.0; // T tail
		Amount<Angle> deflectionAngle = Amount.valueOf(20, NonSI.DEGREE_ANGLE);
		double cLTotal = theStablityCalculator.calculateCLCompleteAircraft(
				aircraft,
				alphaBody,
				meanAirfoil,
				deflectionAngle,
				chordRatio,
				deltaFlap,
				flapType,
				null,
				eta_in_flap,
				eta_out_flap,
				null,
				null,
				cf_c,
				null,
				null,
				null);

		System.out.println("\n the CL of aircraft at alpha body =(deg)" +
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
				" for delta = (deg) "
				+ deflectionAngle.getEstimatedValue()
				+ " is " + cLTotal);




		// -----------------------------------------------------------------------
		// DRAG CHARACTERISTICS
		// -----------------------------------------------------------------------


		System.out.println("\n\n------------------------------------");
		System.out.println("\n DRAG CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");

		// Wing
		System.out.println("\n ------------------- ");
		System.out.println("|       WING        |");
		System.out.println(" ------------------- \n\n");

		LSAerodynamicsManager.CalcCDAtAlpha theCDWingCalculator = theLSAnalysis
				.new CalcCDAtAlpha();
		double cDIsolatedWing = theCDWingCalculator.integralFromCdAirfoil(
				alphaWing, MethodEnum.NASA_BLACKWELL, theLSAnalysis);
		System.out.println(" CD of Wing at alpha body = "
				+ alphaBody.to(NonSI.DEGREE_ANGLE)
				+ " is " + cDIsolatedWing);

//		System.out.println(" ...waiting for plotting");
//		theLSAnalysis.PlotCDvsAlphaCurve(subfolderPath);
//		System.out.println("\n\n\t\t\tDONE PLOTTING CD vs ALPHA WING");


		// Horizontal Tail

		System.out.println("\n ------------------- ");
		System.out.println("|  HORIZONTAL TAIL   |");
		System.out.println(" ------------------- \n\n");

		LSAerodynamicsManager.CalcCDAtAlpha theCDHTailCalculator = theLSHorizontalTail.new CalcCDAtAlpha();
		double cDHorizontalTail = theCDHTailCalculator.integralFromCdAirfoil(
				angleTailAmount, MethodEnum.NASA_BLACKWELL, theLSAnalysis);
		System.out.println("\n CD of Horizontal Tail at alpha body = (deg) "
				+ alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " is " + cDHorizontalTail
				);

//		System.out.println(" ...waiting for plotting");
//		theLSHorizontalTail.PlotCDvsAlphaCurve(subfolderPath);
//		System.out.println("\n\n\t\t\tDONE PLOTTING CD vs ALPHA H TAIL CLEAN");

		// elevator contribute



		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSHorizontalTail
				.new CalcHighLiftDevices(
						horizontalTail,
						theConditions,
						deltaFlap,
						flapType,
						null,
						eta_in_flap,
						eta_out_flap,
						null,
						null,
						cf_c,
						null,
						null,
						null
						);

		highLiftCalculator.calculateHighLiftDevicesEffects();

		System.out.println("\n\ndelta CD_0 elevator=" + highLiftCalculator.getDeltaCD());

		double cDTotal = highLiftCalculator.getDeltaCD()+cDHorizontalTail;
		System.out.println("\n CD of Horizontal Tail at alpha body = (deg) "
				+ alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " with a deflection of (deg) " + deflectionElevator + " is " + cDTotal
				);




		// -----------------------------------------------------------------------
		// PITCHING MOMENT
		// -----------------------------------------------------------------------


		System.out.println("\n\n------------------------------------");
		System.out.println("\n PITCHING MOMENT  ");
		System.out.println("\n------------------------------------");

		double cMWing;

		MyStabilityCalculator.CalcPitchingMomentAC theCMCalculator = theStablityCalculator
				.new CalcPitchingMomentAC(theWing, theConditions);
		cMWing = theCMCalculator.calculateCMQuarterMACIntegral(alphaWing);
		System.out.println(" CM Wing at alpha " + alphaWing + " is " + cMWing);
		//theCMCalculator.plotCMatAlpha(alphaWing, subfolderPath);
		//System.out.println("\n\n\t\t\tDONE PLOTTING CM vs eta");


		// PLOT

		int numAlpha = 40;
		double [] alphaVectorCM = new double [numAlpha];
		double alphaStart = 0;
		MyArray alphaArray = new MyArray();
		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart, alphaStart+(numAlpha/2), numAlpha));
		double [] cMVector = new double [numAlpha];
		double [] alphaArraydouble = new double [numAlpha];

		for (int i=0; i<numAlpha; i++){
			cMVector[i] = theCMCalculator.calculateCMQuarterMACIntegral(
					Amount.valueOf(Math.toRadians(alphaArray.get(i)), SI.RADIAN));
			alphaArraydouble[i] = alphaArray.get(i);

			}


		MyChartToFileUtils.plotNoLegend(
				alphaArraydouble , cMVector,
				null, null, null, null,
				"alpha", "CM",
				"deg", "",
				subfolderPath," Moment Coefficient vs alpha for Wing" );

		System.out.println("\n\n\t\t\tDONE PLOTTING CM vs ALPHA FOR WING");

	}

}


