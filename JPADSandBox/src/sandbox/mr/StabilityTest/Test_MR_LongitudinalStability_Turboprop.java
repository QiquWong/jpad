package sandbox.mr.StabilityTest;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.RRQRDecomposition;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScaledRamp.Threshold;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.componentmodel.InnerCalculator;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.FusAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcAlpha0L;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCDAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import calculators.aerodynamics.LiftCalc;
import database.databasefunctions.DatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import javafx.util.Pair;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;


public class Test_MR_LongitudinalStability_Turboprop {
	
	

		//------------------------------------------------------------------------------------------
		// VARIABLE DECLARATION:
		@Option(name = "-i", aliases = { "--input" }, required = false,
				usage = "my input file")
		private File _inputFile;
		
		//take off file
		
		@Option(name = "-to", aliases = { "--input_to" }, required = false,
				usage = "my input TO file")
		private File _inputFileTakeOff;
		
		//landing file
		
		@Option(name = "-land", aliases = { "--input_land" }, required = false,
				usage = "my input LA file")
		private File _inputFileLanding;

		// declaration necessary for Concrete Object usage
		public CmdLineParser theCmdLineParser;
		public JPADXmlReader reader;

		@Argument
		private List<String> arguments = new ArrayList<String>();

		//------------------------------------------------------------------------------------------
		//BUILDER:
		public Test_MR_LongitudinalStability_Turboprop() {
			theCmdLineParser = new CmdLineParser(this);
		}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, CmdLineException {

		System.out.println("------------------------------------");
		System.out.println("\n Longitudinal Stability Test - ATR72 ");
		System.out.println("\n------------------------------------");

		Test_MR_LongitudinalStability_Turboprop main = new Test_MR_LongitudinalStability_Turboprop();
		
		
		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------

		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability_Turboprop" + File.separator);
		String subfolderPathTakeOFF = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability_Turboprop_TakeOff" + File.separator);
		
		//----------------------------------------------------------------------------------
		// Default folders creation:

		MyConfiguration.initWorkingDirectoryTree();
		
		
	
		// -----------------------------------------------------------------------
		// DEFINITION OF DEFAULT AIRCRAFT
		// -----------------------------------------------------------------------
		
		//------------------------------------------------------------------------------------
		// Default Aircraft
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		aircraft.set_name("ATR-72");
		System.out.println("\nDefault aircraft: " + aircraft.get_name() + "\n");

		//------------------------------------------------------------------------------------
		// Wing and Tail
		LiftingSurface2Panels theWing = aircraft.get_wing();
		LiftingSurface2Panels horizontalTail = aircraft.get_HTail();
		

		
		// -----------------------------------------------------------------------
		// OPERATING CONDITIONS
		// -----------------------------------------------------------------------
		
		OperatingConditions theConditions = new OperatingConditions();
		theConditions.set_alphaCurrent(Amount.valueOf(toRadians(2.), SI.RADIAN));
//		System.out.println("\n OPERATING CONDITIONS: ");
//		System.out.println("Altitude " + theConditions.get_altitude());
//		System.out.println("Mach number " + theConditions.get_machCurrent()+"\n");
//		System.out.println("Alpha Current " + theConditions.get_alphaCurrent().to(NonSI.DEGREE_ANGLE) + "\n");
		
		
		
		
		
		// -----------------------------------------------------------------------
		// ANALYSIS MANAGERS AND DATABASES DEFINITION
		// -----------------------------------------------------------------------
		
		//--------------------------------------------------------------------------------------
		// Aerodynamic managers
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theConditions);
		theAnalysis.updateGeometry(aircraft);
		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theConditions,
				theWing,
				aircraft
				);
		
		theWing.setAerodynamics(theLSAnalysis);


		//------------------------------------------------------------------------------------
		// Setup database(s)
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

		
		
		//--------------------------------------------------------------------------------------
		// Set databases
		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);
		
		
		// -----------------------------------------------------------------------
		// AIRFOILS
		// -----------------------------------------------------------------------

		
		System.out.println("\nWING AIRFOILS:");

		//AIRFOIL 1
		double yLocRoot = 0.0;
		Airfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(4.5);
		

		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
		System.out.println("Root Chord [m] = " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness = " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Root = " + airfoilRoot.getGeometry().get_deltaYPercent());


		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		Airfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(4.5);


		
		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("Kink Station [m] = " + yLocKink);
		System.out.println("Kink Chord [m] = " + theWing.get_chordKink().getEstimatedValue() );
		System.out.println("Kink maximum thickness = " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilKink.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink = " + airfoilKink.getGeometry().get_deltaYPercent());


		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		Airfoil airfoilTip = theWing.get_theAirfoilsList().get(2);
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(3.7);
		

		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilTip.get_family());
		System.out.println("tip Chord [m] = " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness = " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTip.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip = " + airfoilTip.getGeometry().get_deltaYPercent()+ "\n");


		//--------------------------------------------------------------------------------------
		// Assign airfoil

		List<Airfoil> myAirfoilList = new ArrayList<Airfoil>();
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
		Airfoil airfoilRootHorizontalTail = new Airfoil(
				horizontalTail, yLocRootH, "0012");
		airfoilRoot.getAerodynamics().set__deltaYPercent(3.0);
		airfoilTip.getAerodynamics().set__deltaYPercent(3.0);
		airfoilRootHorizontalTail.getGeometry().update(yLocRootH);  // define chord
		airfoilRootHorizontalTail.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

		double yLocTipH = aircraft.get_HTail().get_semispan().getEstimatedValue();
		Airfoil airfoilTipHorizontalTail = new Airfoil(
				horizontalTail, yLocTipH, "0012");
		airfoilTip.getGeometry().set_deltaYPercent(3.0);
		airfoilTipHorizontalTail.getGeometry().update(yLocTipH);  // define chord
		airfoilTipHorizontalTail.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

		List<Airfoil> myAirfoilListHorizontalTail = new ArrayList<Airfoil>();
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



		//--------------------------------------------------------------------------------------
		// Mean Airfoil

		LSAerodynamicsManager.MeanAirfoil theMeanAirfoilCalculator = theLSAnalysis.new MeanAirfoil();
		Airfoil meanAirfoil = theMeanAirfoilCalculator.calculateMeanAirfoil(theWing);


		
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


		// do Analysis
		
		System.out.println("\n\n-----------------------------------");
		System.out.println("\nANALYSIS ");
		System.out.println("\n------------------------------------");
		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.BALANCE,
				AnalysisTypeEnum.AERODYNAMIC_AND_STABILITY
				);

		theLSAnalysis.setHighLiftDatabaseReader(highLiftDatabaseReader);
		theLSHorizontalTail.setHighLiftDatabaseReader(highLiftDatabaseReader);
		theWing.setAerodynamics(theLSAnalysis);
		horizontalTail.setAerodynamics(theLSHorizontalTail);

		
		
		// -----------------------------------------------------------------------
		// READING XML FILE
		// -----------------------------------------------------------------------

		System.out.println("\n\n------------------------------------");
		System.out.println("\n READING XML FILE...  ");
		System.out.println("\n------------------------------------");
		
		// Arguments check
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		main.theCmdLineParser.parseArgument(args);
		String path = main.get_inputFile().getAbsolutePath();
		JPADXmlReader reader = new JPADXmlReader(path);
		String pathTakeOff = null, pathLanding=null;
		
		if(args.length >0){
			if(main.get_inputFileTakeOff()!= null)
		pathTakeOff = main.get_inputFileTakeOff().getAbsolutePath();
			if(main.get_inputFileLanding()!= null)
		pathLanding = main.get_inputFileLanding().getAbsolutePath();
			}

		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading \n");

		
		//Reading data
		
		double etaInboard = Double.parseDouble(reader.getXMLPropertiesByPath("//Elevator_inboard").get(0));
		double etaOutboard = Double.parseDouble(reader.getXMLPropertiesByPath("//Elevator_outboard").get(0));
		double chordRatio = Double.parseDouble(reader.getXMLPropertiesByPath("//Ce_c").get(0));
		aircraft.get_HTail().set_CeCt(chordRatio);
		aircraft.get_HTail().set_etaIn(etaInboard);
		aircraft.get_HTail().set_etaOut(etaOutboard);
		
		Amount<Angle> deflectionElevator = reader.getXMLAmountWithUnitByPath("//Deflection").to(NonSI.DEGREE_ANGLE);
		//double deflectionElevator = Double.parseDouble(reader.getXMLPropertyByPath("//Deflection"));
		double dynamicPressureRatio = Double.parseDouble(reader.getXMLPropertiesByPath("//Dynamic_Pressure_Ratio").get(0));
		Amount<Length> fanDiameter = reader.getXMLAmountWithUnitByPath("//Fan_Diameter").to(SI.METER);
		double etaEfficiency = Double.parseDouble(reader.getXMLPropertiesByPath("//Efficiency").get(0));
		double nBlade = Double.parseDouble(reader.getXMLPropertiesByPath("//NBlade").get(0));
		double machTO = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_TO").get(0));
		double machL = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_L").get(0));
		double machCruise = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_cruise").get(0));
		double deltaDrag = Double.parseDouble(reader.getXMLPropertiesByPath("//deltaFactorDrag").get(0));
		theWing.setDeltaFactorDrag(deltaDrag);
		Amount<Length> cruiseAltitude = reader.getXMLAmountWithUnitByPath("//Cruise_Altitude").to(SI.METER);
		
		
		aircraft.get_HTail().getAerodynamics().set_dynamicPressureRatio(dynamicPressureRatio);
		aircraft.get_powerPlant().setEtaEfficiency(etaEfficiency);
		aircraft.get_powerPlant().setnBlade(nBlade);
		aircraft.get_powerPlant().setFanDiameter(fanDiameter);
		double deflectionElevatorDouble = deflectionElevator.getEstimatedValue();
		
		aircraft.get_theAerodynamics().set_machCruise(machCruise);
		aircraft.get_theAerodynamics().set_machTakeOFF(machTO);
		aircraft.get_theAerodynamics().set_machLanding(machL);
		aircraft.get_theAerodynamics().setCruiseAltitude(cruiseAltitude);
		
		List<Double[]> deltaFlap = new ArrayList<Double[]>();
		List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
		List<Double> eta_in_flap = new ArrayList<Double>();
		List<Double> eta_out_flap = new ArrayList<Double>();
		List<Double> cf_c = new ArrayList<Double>();

		Double[] deltaFlapDouble =  new Double [1];
		deltaFlapDouble[0] = deflectionElevatorDouble;

		deltaFlap.add(deltaFlapDouble);
		flapType.add(FlapTypeEnum.PLAIN);
		eta_in_flap.add(etaInboard);
		eta_out_flap.add(etaOutboard);
		cf_c.add(chordRatio);
		
		aircraft.get_landingGear().set_X0(Amount.valueOf(0.4*aircraft.get_fuselage().get_len_T().getEstimatedValue(), SI.METER));

		// -----------------------------------------------------------------------
		// STABILITY 
		// -----------------------------------------------------------------------
		
		System.out.println( "path take off " + pathTakeOff);
		System.out.println( "path landing " + pathLanding);
		
		Amount<Angle> alphaBody = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
		Amount<Angle> alphaMin = Amount.valueOf(Math.toRadians(-5), SI.RADIAN);
		Amount<Angle> alphaMax = Amount.valueOf(Math.toRadians(23), SI.RADIAN);
		
//		Amount<Angle> alphaMin = Amount.valueOf(Math.toRadians(-6), SI.RADIAN);
//		Amount<Angle> alphaMax = Amount.valueOf(Math.toRadians(25), SI.RADIAN);
		
		ACStabilityManager theStabilityManager = new ACStabilityManager(meanAirfoil, aircraft, ConditionEnum.CRUISE,
				alphaMin, alphaMax, alphaBody , true, subfolderPath, pathTakeOff);
		
		ACStabilityManager theStabilityManagerTakeOFF = new ACStabilityManager(meanAirfoil, aircraft, ConditionEnum.TAKE_OFF,
				alphaMin, alphaMax, alphaBody , true, subfolderPathTakeOFF, pathTakeOff);
 
	theStabilityManager.calculateAll();
//	theStabilityManagerTakeOFF.calculateAll();
		
		


		// CL ANALYSIS
		
	System.out.println(" \nAirfoils analysis-----------");		
	theConditions.set_machCurrent(0.2);
	theConditions.calculate();
	double reRoot = theConditions.calculateRe(theWing.get_chordRoot().getEstimatedValue(),1);
	System.out.println( "Reynolds number root station " + reRoot);
	double reKink = theConditions.calculateRe(theWing.get_chordKink().getEstimatedValue(),1);
	System.out.println( "Reynolds number kink station " + reKink);
	double reTip = theConditions.calculateRe(theWing.get_chordTip().getEstimatedValue(),1);
	System.out.println( "Reynolds number tip station " + reTip);
	
	String subfolderPathAirfoil = JPADStaticWriteUtils.createNewFolder(folderPath + "Wing airfoil data" + File.separator);
	
	airfoilRoot.getAerodynamics().plotClvsAlpha(subfolderPathAirfoil, "Root");
	airfoilKink.getAerodynamics().plotClvsAlpha(subfolderPathAirfoil, "Kink");
	airfoilTip.getAerodynamics().plotClvsAlpha(subfolderPathAirfoil, "Tip");
		
	
	LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();
	double [] cLWingCleanArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(alphaMin,alphaMax,60, false);
	
	double[] alphaArrayCL = MyArrayUtils.linspace(alphaMin.getEstimatedValue(), alphaMax.getEstimatedValue(), 60);
	
	System.out.println("\n \n-----------------------------------------------------");
	System.out.println("WRITING TO CHART CL vs ALPHA CURVE stall path");
	System.out.println("-----------------------------------------------------");
	
	double [] alphaArrayCLDeg = new double [60];

	for (int i=0; i<60; i++){
		alphaArrayCLDeg[i] = Math.toDegrees(alphaArrayCL[i]);
	}
//	MyChartToFileUtils.plotNoLegend(
//			alphaArrayCLDeg , cLWingCleanArray,
//			null, null, null, null,
//			"alpha", "CL",
//			"deg", "",
//			subfolderPathAirfoil," CL vs Alpha stall path " );
//	
//	System.out.println("\n \n-----------------------------------------------------");
//	System.out.println("DONE");
//	System.out.println("-----------------------------------------------------");
	
	
	
	
	
	
		//---------------------
		System.out.println("\n\n ---------CL  vs Alpha Calc-------");
		MyArray alphaArrayActual =new MyArray();
		
		int  nVal =50;
		
		double angle = Math.toRadians(24);
		alphaArrayActual.linspace(-0.03, angle,  nVal);
		double[] alphaArray = new double[ nVal];
		double [] cLWing = LiftCalc.calculateCLArraymodifiedStallPath(alphaArrayActual, theWing);
		
		for (int i=0; i< nVal; i++){
			alphaArray[i] = Math.toDegrees(alphaArrayActual.get(i));
		}
//		System.out.println(" alpha Array " + Arrays.toString(alphaArray) );
//		System.out.println(" cl "+  Arrays.toString(cLWing));
		
		
		MyChartToFileUtils.plotNoLegend(
				alphaArray , cLWing,
				null, null, null, null,
				"alpha", "CL",
				"deg", "",
				subfolderPath," CL vs Alpha from Airfoils " );

		
		
		
	}
	
	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}


	public File get_inputFileTakeOff() {
		return _inputFileTakeOff;
	}


	public void set_inputFileTakeOff(File _inputFileTakeOff) {
		this._inputFileTakeOff = _inputFileTakeOff;
	}


	public File get_inputFileLanding() {
		return _inputFileLanding;
	}


	public void set_inputFileLanding(File _inputFileLanding) {
		this._inputFileLanding = _inputFileLanding;
	}

}
