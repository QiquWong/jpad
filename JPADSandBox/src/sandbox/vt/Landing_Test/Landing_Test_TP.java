package sandbox.vt.Landing_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
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
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.customdata.CenterOfGravity;

public class Landing_Test_TP {

	private static long _startTimeCalculation, _startTimeGraph, _stopTimeCalculation,
	_stopTimeGraph, _stopTimeTotal,	_elapsedTimeTotal, 
	_elapsedTimeCalculation, _elapsedTimeGraph;

	//	TODO: example of custom NON_SI unit
	//	private static Unit<? extends Quantity> angularRateUnit = (NonSI.DEGREE_ANGLE).divide((SI.SECOND));

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	//------------------------------------------------------------------------------------------
	//BUILDER:
	public Landing_Test_TP() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("Landing_Test :: ATR-72");
		System.out.println("-----------------------------------------------------------\n");

		Landing_Test_TP main = new Landing_Test_TP();

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

		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_altitude(Amount.valueOf(0.0, SI.METER));
		theCondition.set_machCurrent(0.15);
		theCondition.calculate();

		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.ATR72);
		aircraft.set_name("ATR-72");
		//		aircraft.get_weights().set_MLW(Amount.valueOf(22350*9.81, SI.NEWTON)); // optional MLW from brochure

		LiftingSurface theWing = aircraft.get_wing();

		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);

		// Set the CoG(Bypass the Balance analysis allowing to perform Aerodynamic analysis only)
		CenterOfGravity cgMTOM = new CenterOfGravity();

		// x_cg in body-ref.-frame
		cgMTOM.set_xBRF(Amount.valueOf(12.0, SI.METER));
		cgMTOM.set_yBRF(Amount.valueOf(0.0, SI.METER));
		cgMTOM.set_zBRF(Amount.valueOf(2.3, SI.METER));

		aircraft.get_theBalance().set_cgMTOM(cgMTOM);
		aircraft.get_HTail().calculateArms(aircraft);
		aircraft.get_VTail().calculateArms(aircraft);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theCondition,
				theWing,
				aircraft
				);

		theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);

		theAnalysis.doAnalysis(aircraft, AnalysisTypeEnum.AERODYNAMIC);

		theLSAnalysis.setHighLiftDatabaseReader(highLiftDatabaseReader);
		theWing.setAerodynamics(theLSAnalysis);

		// Define airfoil
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("AIRFOILS");
		System.out.println("-----------------------------------------------------");

		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = theWing.get_theAirfoilsList().get(0);
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
		MyAirfoil airfoilKink = theWing.get_theAirfoilsList().get(1);
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
		MyAirfoil airfoilTip = theWing.get_theAirfoilsList().get(2);
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(4.307);
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord [m] = " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness = " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilTip.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip = " + airfoilTip.getGeometry().get_deltaYPercent());


		//--------------------------------------------------------------------------------------
		// Assign airfoil

		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry();
		aircraft.get_exposedWing().set_theAirfoilsList(myAirfoilList);
		aircraft.get_exposedWing().updateAirfoilsGeometryExposedWing( aircraft);

		//----------------------------------------------------------------------------------
		// High Lift Devices Input
		List<Double[]> deltaFlap = new ArrayList<Double[]>();
		List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
		List<Double> etaInFlap = new ArrayList<Double>();
		List<Double> etaOutFlap = new ArrayList<Double>();
		List<Double> cfc = new ArrayList<Double>();

		// XML reading phase:
		// Arguments check
		if (args.length == 0){
			System.err.println("NO INPUT FILE GIVEN --> TERMINATING");
			return;
		}
		main.theCmdLineParser.parseArgument(args);
		String path = main.get_inputFile().getAbsolutePath();
		JPADXmlReader reader = new JPADXmlReader(path);

		System.out.println("-----------------------------------------------------------");
		System.out.println("XML File Path : " + path);
		System.out.println("-----------------------------------------------------------");
		System.out.println("Initialize reading \n");

		List<String> flapNumberProperty = reader.getXMLPropertiesByPath("//Flap_Number");
		int flapNumber = Integer.valueOf(flapNumberProperty.get(0));
		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//FlapType");
		List<String> cfcProperty = reader.getXMLPropertiesByPath("//Cf_c");
		List<String> deltaFlap1Property = reader.getXMLPropertiesByPath("//Delta_Flap1");
		List<String> deltaFlap2Property = reader.getXMLPropertiesByPath("//Delta_Flap2");
		List<String> etaInProperty = reader.getXMLPropertiesByPath("//Flap_inboard");
		List<String> etaOutProperty = reader.getXMLPropertiesByPath("//Flap_outboard");

		for(int i=0; i<flapTypeProperty.size(); i++) {
			if(flapTypeProperty.get(i).equals("SINGLE_SLOTTED"))
				flapType.add(FlapTypeEnum.SINGLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("DOUBLE_SLOTTED"))
				flapType.add(FlapTypeEnum.DOUBLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("PLAIN"))
				flapType.add(FlapTypeEnum.PLAIN);
			else if(flapTypeProperty.get(i).equals("FOWLER"))
				flapType.add(FlapTypeEnum.FOWLER);
			else if(flapTypeProperty.get(i).equals("TRIPLE_SLOTTED"))
				flapType.add(FlapTypeEnum.TRIPLE_SLOTTED);
			else {
				System.err.println("NO VALID FLAP TYPE!!");
				return;
			}
		}

		Double[] deltaFlap1Array = new Double[deltaFlap1Property.size()];
		for(int i=0; i<deltaFlap1Array.length; i++)
			deltaFlap1Array[i] = Double.valueOf(deltaFlap1Property.get(i));

		Double[] deltaFlap2Array = new Double[deltaFlap2Property.size()];
		for(int i=0; i<deltaFlap1Array.length; i++)
			deltaFlap2Array[i] = Double.valueOf(deltaFlap2Property.get(i));

		deltaFlap.add(deltaFlap1Array);
		deltaFlap.add(deltaFlap2Array);

		for(int i=0; i<cfcProperty.size(); i++)
			cfc.add(Double.valueOf(cfcProperty.get(i)));
		for(int i=0; i<etaInProperty.size(); i++)
			etaInFlap.add(Double.valueOf(etaInProperty.get(i)));
		for(int i=0; i<etaOutProperty.size(); i++)
			etaOutFlap.add(Double.valueOf(etaOutProperty.get(i)));

		LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
				.new CalcHighLiftDevices(
						aircraft.get_wing(),
						theCondition,
						deltaFlap,
						flapType,
						null,
						etaInFlap,
						etaOutFlap,
						null,
						null,
						cfc,
						null,
						null,
						null
						);

		//		highLiftCalculator.calculateHighLiftDevicesEffects();
		//
		//		//----------------------------------------------------------------------------------
		//		// Results print
		//		System.out.println("\ndeltaCl0_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCl0_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCl0_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCl0_flap = \n" + highLiftCalculator.getDeltaCl0_flap());
		//
		//		System.out.println("\n\ndeltaCL0_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCL0_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCL0_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCL0_flap = \n" + highLiftCalculator.getDeltaCL0_flap());
		//
		//		System.out.println("\n\ndeltaClmax_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaClmax_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaClmax_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaClmax_flap = \n" + highLiftCalculator.getDeltaClmax_flap());
		//
		//		System.out.println("\n\ndeltaCLmax_flap_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCLmax_flap_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCLmax_flap_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCLmax_flap = \n" + highLiftCalculator.getDeltaCLmax_flap());
		//
		//		System.out.println("\n\ncLalpha_new_list = ");
		//		for(int i=0; i<highLiftCalculator.getcLalpha_new_list().size(); i++)
		//			System.out.print(highLiftCalculator.getcLalpha_new_list().get(i) + " ");
		//
		//		System.out.println("\n\ncLalpha_new = \n" + highLiftCalculator.getcLalpha_new());
		//
		//		System.out.println("\n\ndeltaAlphaMax_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaAlphaMax_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaAlphaMax_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaAlphaMax = \n" + highLiftCalculator.getDeltaAlphaMaxFlap());
		//
		//		System.out.println("\n\ndeltaCD_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCD_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCD_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCD = \n" + highLiftCalculator.getDeltaCD());
		//
		//		System.out.println("\n\ndeltaCMc_4_list = ");
		//		for(int i=0; i<highLiftCalculator.getDeltaCM_c4_list().size(); i++)
		//			System.out.print(highLiftCalculator.getDeltaCM_c4_list().get(i) + " ");
		//
		//		System.out.println("\n\ndeltaCMc_4 = \n" + highLiftCalculator.getDeltaCM_c4());
		//
		//		highLiftCalculator.plotHighLiftCurve();

		//----------------------------------------------------------------------------------
		// Landing - Ground Roll Distance Test
		//----------------------------------------------------------------------------------
		_startTimeCalculation = System.currentTimeMillis();
		double mu = 0.03;
		double muBrake = 0.5;
		double kA = 1.3; // [1/deg]
		double kFlare = 1.23;
		double kTD = 1.15;
		double phiRev = 0.25;
		Amount<Duration> nFreeRoll = Amount.valueOf(3, SI.SECOND);
		Amount<Length> wingToGroundDistance = Amount.valueOf(4.0, SI.METER);
		Amount<Length> obstacle = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		Amount<Velocity> vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> iw = Amount.valueOf(1.5, NonSI.DEGREE_ANGLE);
		Amount<Angle> thetaApproach = Amount.valueOf(4.0, NonSI.DEGREE_ANGLE);
		CalcLanding theLandingCalculator = new CalcLanding(
				aircraft,
				theCondition,
				highLiftCalculator,
				kA,
				kFlare,
				kTD,
				mu,
				muBrake,
				wingToGroundDistance,
				obstacle,
				vWind,
				alphaGround,
				iw,
				thetaApproach,
				nFreeRoll
				);

		theLandingCalculator.calculateLandingDistance(phiRev);
		_stopTimeCalculation = System.currentTimeMillis();
		_startTimeGraph = System.currentTimeMillis();
		theLandingCalculator.createLandingCharts();
		_stopTimeGraph = System.currentTimeMillis();
		_stopTimeTotal = System.currentTimeMillis();

		_elapsedTimeTotal = _stopTimeTotal - _startTimeCalculation;
		_elapsedTimeCalculation = _stopTimeCalculation - _startTimeCalculation;
		_elapsedTimeGraph = _stopTimeGraph - _startTimeGraph;

		System.out.println("\n------------------COMPUTATIONAL TIME-----------------------");
		System.out.println("\nANALYSIS TIME = " + (get_elapsedTime()) + " millisenconds");
		System.out.println("\nCALCULATION TIME = " + (get_elapsedTimeCalculation()) + " millisenconds");
		System.out.println("\nGRAPHICS TIME = " + (get_elapsedTimeGraph()) + " millisenconds");
		System.out.println("-----------------------------------------------------------\n");

		System.out.println("\n------------------------RESULTS----------------------------");
		System.out.println("\nAIRBORNE DISTANCE = " + theLandingCalculator.getsApproach());
		System.out.println("\nFLARE DISTANCE = " + theLandingCalculator.getsFlare());
		System.out.println("\nGROUND ROLL DISTANCE = " + theLandingCalculator.getsGround());
		System.out.println("\nTOTAL LANDING DISTANCE = " + theLandingCalculator.getsTotal());
		System.out.println("-----------------------------------------------------------\n");	
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public File get_inputFile() {
		return _inputFile;
	}

	public static long get_elapsedTime() {
		return _elapsedTimeTotal;
	}

	public static long get_elapsedTimeGraph() {
		return _elapsedTimeGraph;
	}

	public static long get_elapsedTimeCalculation() {
		return _elapsedTimeCalculation;
	}
}