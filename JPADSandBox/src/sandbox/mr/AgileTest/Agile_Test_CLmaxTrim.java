package sandbox.mr.AgileTest;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.FusAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import javafx.util.Pair;
import sandbox.mr.StabilityTest.ACStabilityManager;
import sandbox.mr.StabilityTest.Test_MR_LongitudinalStability_Turboprop;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Agile_Test_CLmaxTrim {

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
	public Agile_Test_CLmaxTrim() {
		theCmdLineParser = new CmdLineParser(this);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, CmdLineException {

		System.out.println("------------------------------------");
		System.out.println("\n Longitudinal Stability Test - AGILE DC-1 ");
		System.out.println("\n------------------------------------");

		Test_MR_LongitudinalStability_Turboprop main = new Test_MR_LongitudinalStability_Turboprop();


		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------

		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability_AGILE" + File.separator);
		String subfolderPathTakeOFF = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability_AGILE_TakeOff" + File.separator);

		//----------------------------------------------------------------------------------
		// Default folders creation:

		MyConfiguration.initWorkingDirectoryTree();



		// -----------------------------------------------------------------------
		// DEFINITION OF DEFAULT AIRCRAFT
		// -----------------------------------------------------------------------

		//------------------------------------------------------------------------------------
		// Default Aircraft
		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		aircraft.set_name("Agile DC1");
		System.out.println("\nDefault aircraft: " + aircraft.get_name() + "\n");

		//------------------------------------------------------------------------------------
		// Wing and Tail
		LiftingSurface theWing = aircraft.get_wing();
		LiftingSurface horizontalTail = aircraft.get_HTail();


		// update of the wing with new model parameters

		theWing.set_surface(Amount.valueOf(82.7, SI.SQUARE_METRE));
		theWing.set_aspectRatio(9.54);
		theWing.set_taperRatioEquivalent(0.217);
		theWing.set_taperRatioInnerPanel(0.425);
		theWing.set_taperRatioOuterPanel(0.387);
		theWing.set_taperRatioCrankedWing(0.1645);
		theWing.set_spanStationKink(0.398);
		theWing.set_extensionLERootChordLinPanel(0.16886);
		theWing.set_extensionTERootChordLinPanel(0.505361);
		theWing.set_iw(Amount.valueOf(Math.toRadians(2.5),SI.RADIAN));

		theWing.set_dihedralInnerPanel(Amount.valueOf(Math.toRadians(6.0), SI.RADIAN));
		theWing.set_dihedralOuterPanel(Amount.valueOf(Math.toRadians(6.0), SI.RADIAN));
		MyArray _dihedral = new MyArray(new double[] {theWing.get_dihedralInnerPanel().getEstimatedValue(), theWing.get_dihedralOuterPanel().getEstimatedValue()});
		theWing.set_dihedral(_dihedral);
		theWing.set_chordRoot(Amount.valueOf(6.39, SI.METER));
		theWing.set_chordKink(Amount.valueOf(2.716, SI.METER)); 
		theWing.set_chordTip(Amount.valueOf(1.051, SI.METER)); 
		theWing.set_tc_root(0.161);
		theWing.set_tc_kink(0.149);
		theWing.set_tc_tip(0.119);
		theWing.set_xLERoot(Amount.valueOf(0.0, SI.METER));
		theWing.set_xLEKink(Amount.valueOf(3.707, SI.METER));
		theWing.set_xLETip(Amount.valueOf(8.305, SI.METER));
		theWing.set_sweepQuarterChordEq(Amount.valueOf(26.3, NonSI.DEGREE_ANGLE));
		theWing.set_sweepLEEquivalent(
				theWing.calculateSweep(
						theWing.get_sweepQuarterChordEq().getEstimatedValue(),
						0.0,
						0.25)
				); 



		// -----------------------------------------------------------------------
		// OPERATING CONDITIONS
		// -----------------------------------------------------------------------

		OperatingConditions theConditions = new OperatingConditions();
		// TakeOff/Landing conditions:
		theConditions.set_altitude(Amount.valueOf(0.0, SI.METER));
		theConditions.set_machCurrent(0.15);
		theConditions.calculate();





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

		// Set database directory	
				String databaseFolderPathFus = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
				String databaseFileName = "FusDes_database.h5";

		//--------------------------------------------------------------------------------------
		FusAerodynamicsManager theFuselageManager = new FusAerodynamicsManager(theConditions, aircraft);


		double finenessRatio       = aircraft.get_fuselage().get_lambda_F().doubleValue();
		double noseFinenessRatio   = aircraft.get_fuselage().get_lambda_N().doubleValue();
		double tailFinenessRatio   = aircraft.get_fuselage().get_lambda_T().doubleValue();
		double upsweepAngle 	   = aircraft.get_fuselage().get_upsweepAngle().getEstimatedValue();
		double windshieldAngle     = aircraft.get_fuselage().get_windshieldAngle().getEstimatedValue();
		double xPositionPole	   = 0.5;
		double fusSurfRatio = aircraft.get_fuselage().get_area_C().doubleValue(SI.SQUARE_METRE)/
				aircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE);

		FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPathFus, databaseFileName);
		fusDesDatabaseReader.runAnalysis(noseFinenessRatio, windshieldAngle, finenessRatio, tailFinenessRatio, upsweepAngle, xPositionPole);
		// -----------------------------------------------------------------------
		// AIRFOILS
		// -----------------------------------------------------------------------


		System.out.println("\nWING AIRFOILS:");

		//AIRFOILS DEFINITION (initialize and set data):
		//AIRFOIL ROOT
		double yLocRoot = 0.0;		
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot);
		airfoilRoot.getAerodynamics().set_clAlpha(7.0336);
		airfoilRoot.getAerodynamics().set_clStar(1.2);
		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(9.5), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(22.5), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_clMax(2.0);
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.161);
		airfoilRoot.getGeometry().set_radiusLE(0.03892);
		airfoilRoot.getGeometry().set_deltaYPercent(4.375);
		airfoilRoot.set_chordLocal(6.39);
		airfoilRoot.getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.9864), SI.RADIAN));
		airfoilRoot.getAerodynamics().set_cdMin(0.0);
		airfoilRoot.getAerodynamics().set_clAtCdMin(0.0);
		airfoilRoot.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilRoot.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilRoot.getAerodynamics().set_cmAC(0.0);
		airfoilRoot.getAerodynamics().set_cmACStall(0.0);
		airfoilRoot.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilRoot.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilRoot.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilRoot.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilRoot.getGeometry().set_anglePhiTE(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		airfoilRoot.getGeometry().set_thicknessOverChordUnit(0.0);

		//AIRFOIL KINK
		double yLocKink = 5.5919;	
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink);
		airfoilKink.getAerodynamics().set_clAlpha(6.9533);
		airfoilKink.getAerodynamics().set_clStar(1.2);
		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(8.5), SI.RADIAN));
		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(21.0), SI.RADIAN));
		airfoilKink.getAerodynamics().set_clMax(2.0);
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.149);
		airfoilKink.getGeometry().set_radiusLE(0.04265);
		airfoilKink.getGeometry().set_deltaYPercent(3.88);
		airfoilKink.set_chordLocal(2.716);
		airfoilKink.getGeometry().set_twist(Amount.valueOf(Math.toRadians(-1.592), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.6289), SI.RADIAN));
		airfoilKink.getAerodynamics().set_cdMin(0.0);
		airfoilKink.getAerodynamics().set_clAtCdMin(0.0);
		airfoilKink.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilKink.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilKink.getAerodynamics().set_cmAC(0.0);
		airfoilKink.getAerodynamics().set_cmACStall(0.0);
		airfoilKink.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilKink.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilKink.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilKink.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilKink.getGeometry().set_anglePhiTE(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilKink.getGeometry().set_thicknessOverChordUnit(0.0);

		//AIRFOIL TIP
		double yLocTip = 14.05;	
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip);
		airfoilTip.getAerodynamics().set_clAlpha(6.6210);
		airfoilTip.getAerodynamics().set_clStar(1.15);
		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(8.0), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(15.5), NonSI.DEGREE_ANGLE));
		airfoilTip.getAerodynamics().set_clMax(1.8);
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.119);
		airfoilTip.getGeometry().set_radiusLE(0.01011);
		airfoilTip.getGeometry().set_deltaYPercent(2.92);
		airfoilTip.set_chordLocal(1.051);
		airfoilTip.getGeometry().set_twist(Amount.valueOf(Math.toRadians(-4.0), SI.RADIAN));
		// the followings are not necessaries to the high lift devices effects analysis
		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-3.1795), SI.RADIAN));
		airfoilTip.getAerodynamics().set_cdMin(0.0);
		airfoilTip.getAerodynamics().set_clAtCdMin(0.0);
		airfoilTip.getAerodynamics().set_kFactorDragPolar(0.0);
		airfoilTip.getAerodynamics().set_aerodynamicCenterX(0.0);
		airfoilTip.getAerodynamics().set_cmAC(0.0);
		airfoilTip.getAerodynamics().set_cmACStall(0.0);
		airfoilTip.getAerodynamics().set_cmAlphaLE(0.0);
		airfoilTip.getAerodynamics().set_reynoldsCruise(0.0);
		airfoilTip.getAerodynamics().set_reynoldsNumberStall(0.0);
		airfoilTip.getGeometry().set_twist(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilTip.getGeometry().set_anglePhiTE(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		airfoilTip.getGeometry().set_thicknessOverChordUnit(0.0);

		// ASSIGN AIRFOILS:
		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.get_theAirfoilsList().get(1).getGeometry().set_twist(Amount.valueOf(Math.toRadians(-1.0), SI.RADIAN));
		theWing.get_theAirfoilsList().get(2).getGeometry().set_twist(Amount.valueOf(Math.toRadians(-5.0), SI.RADIAN));
		//	theWing.get_theAirfoilsList().get(1).getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));
		//	theWing.get_theAirfoilsList().get(2).getGeometry().set_twist(Amount.valueOf(Math.toRadians(0.0), SI.RADIAN));

		//------------------------------------------------------------------------------------
		// UPDATE DATA
		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();
		theWing.updateAirfoilsGeometry();

		theWing.setAerodynamics(theLSAnalysis);
		//	theLSAnalysis.initializeDependentData();

		// MEAN AIRFOIL:
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("Starting evaluate the mean airfoil characteristics");
		System.out.println("-----------------------------------------------------");

		MyAirfoil meanAirfoil = theWing
				.getAerodynamics()
				.new MeanAirfoil()
				.calculateMeanAirfoil(
						theWing
						);

		double meanAlphaStar = meanAirfoil.getAerodynamics().get_alphaStar().getEstimatedValue();
		System.out.println("\nThe mean alpha star is [rad] = " + meanAlphaStar);
		double alphaStarDeg = Math.toDegrees(meanAlphaStar);
		System.out.println("The mean alpha star is [deg] = " + alphaStarDeg);
		double meanLESharpnessParameter = meanAirfoil.getGeometry().get_deltaYPercent();
		Amount<Angle> deltaAlphaMax;


		//--------------------------------------------------------------------------------------
		//Horizontal Tail

		System.out.println("\nHORIZONTAL TAIL AIRFOILS:");

		double yLocRootH = 0.0;
		MyAirfoil airfoilRootHorizontalTail = new MyAirfoil(
				horizontalTail, yLocRootH, "0012");
		airfoilRoot.getAerodynamics().set__deltaYPercent(3.0);
		airfoilTip.getAerodynamics().set__deltaYPercent(3.0);
		airfoilRootHorizontalTail.getGeometry().update(yLocRootH);  // define chord
		airfoilRootHorizontalTail.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

		double yLocTipH = aircraft.get_HTail().get_semispan().getEstimatedValue();
		MyAirfoil airfoilTipHorizontalTail = new MyAirfoil(
				horizontalTail, yLocTipH, "0012");
		airfoilTip.getGeometry().set_deltaYPercent(3.0);
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



		// do Analysis

		System.out.println("\n\n-----------------------------------");
		System.out.println("\nANALYSIS ");
		System.out.println("\n------------------------------------");
		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.BALANCE,
				AnalysisTypeEnum.AERODYNAMIC
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
		double machTO = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_TO").get(0));
		double machL = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_L").get(0));
		double machCruise = Double.parseDouble(reader.getXMLPropertiesByPath("//Mach_number_cruise").get(0));
		double deltaDrag = Double.parseDouble(reader.getXMLPropertiesByPath("//deltaFactorDrag").get(0));
		theWing.setDeltaFactorDrag(deltaDrag);
		Amount<Length> cruiseAltitude = reader.getXMLAmountWithUnitByPath("//Cruise_Altitude").to(SI.METER);

		aircraft.get_HTail().getAerodynamics().set_dynamicPressureRatio(dynamicPressureRatio);
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

		//	Amount<Angle> alphaMin = Amount.valueOf(Math.toRadians(-6), SI.RADIAN);
		//	Amount<Angle> alphaMax = Amount.valueOf(Math.toRadians(25), SI.RADIAN);

		//	ACStabilityManager theStabilityManager = new ACStabilityManager(meanAirfoil, aircraft, ConditionEnum.CRUISE,
		//		alphaMin, alphaMax, alphaBody , true, subfolderPath, pathTakeOff);

		ACStabilityManager theStabilityManagerLanding = new ACStabilityManager(meanAirfoil, aircraft, ConditionEnum.LANDING,
				alphaMin, alphaMax, alphaBody , true, subfolderPathTakeOFF, pathTakeOff);

		//theStabilityManager.calculateAll();
		theStabilityManagerLanding.calculateAll();




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
		//MyChartToFileUtils.plotNoLegend(
		//		alphaArrayCLDeg , cLWingCleanArray,
		//		null, null, null, null,
		//		"alpha", "CL",
		//		"deg", "",
		//		subfolderPathAirfoil," CL vs Alpha stall path " );
		//
		//System.out.println("\n \n-----------------------------------------------------");
		//System.out.println("DONE");
		//System.out.println("-----------------------------------------------------");






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
		//	System.out.println(" alpha Array " + Arrays.toString(alphaArray) );
		//	System.out.println(" cl "+  Arrays.toString(cLWing));


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
