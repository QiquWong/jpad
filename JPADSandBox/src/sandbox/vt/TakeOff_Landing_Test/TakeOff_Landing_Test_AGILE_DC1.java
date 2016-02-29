package sandbox.vt.TakeOff_Landing_Test;

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

public class TakeOff_Landing_Test_AGILE_DC1 {

	private static long _startTimeCalculation, _startTimeGraph, _startTimeBalancedCalculation,
						_startTimeBalancedGraph, _stopTimeBalancedGraph, _stopTimeCalculation,
						_stopTimeGraph, _stopTimeBalancedCalculation, _stopTimeTotal,
						_elapsedTimeTotal, _elapsedTimeCalculation, _elapsedTimeGraph,
						_elapsedTimeBalancedCalculation, _elapsedTimeBalancedGraph;

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
	public TakeOff_Landing_Test_AGILE_DC1() {
		theCmdLineParser = new CmdLineParser(this);
	}

	public static void main(String[] args) throws CmdLineException, InstantiationException, IllegalAccessException {

		System.out.println("-----------------------------------------------------------");
		System.out.println("TakeOff_Landing_Test :: AGILE_DC1");
		System.out.println("-----------------------------------------------------------\n");

		TakeOff_Landing_Test_AGILE_DC1 main = new TakeOff_Landing_Test_AGILE_DC1();

		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();

		//------------------------------------------------------------------------------------
		// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
		OperatingConditions theCondition = new OperatingConditions();
		theCondition.set_altitude(Amount.valueOf(0.0, SI.METER));
		theCondition.set_machCurrent(0.15);
		theCondition.calculate();

		Aircraft aircraft = Aircraft.createDefaultAircraft(AircraftEnum.AGILE_DC1);
		aircraft.set_name("AGILE_DC1");

		aircraft.get_weights().set_MTOM(Amount.valueOf(42000, SI.KILOGRAM));
		aircraft.get_wing().set_aspectRatio(9.5);
		aircraft.get_wing().set_surface(Amount.valueOf(90, SI.SQUARE_METRE));
		
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
		theAnalysis.updateGeometry(aircraft);
		
		//----------------------------------------------------------------------------------
		// TakeOff - Ground Roll Distance Test
		//----------------------------------------------------------------------------------
		_startTimeCalculation = System.currentTimeMillis();
		Amount<Duration> dtRot = Amount.valueOf(3, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		double mu = 0.025;
		double muBrake = 0.3;
		double kAlphaDot = 0.06; // [1/deg]
		double kcLMax = 0.85;
		double kRot = 1.05;
		double kLO = 1.1;
		double kFailure = 1.0;

//		PARAMETERS USED TO CONSIDER THE PARABOLIC DRAG POLAR CORRECTION AT HIGH CL
//		double k1 = 0.078;
//		double k2 = 0.365;
		double k1 = 0.0;
		double k2 = 0.0;

		double oswald = 0.85;
		double cD0 = 0.0187;
		double cLmaxTO = 2.1;
		double cL0 = 0.69;
		double cLalphaFlap = 0.087;
		double deltaCD0FlapLandingGear = 0.007 + 0.010; 
		
		double phi = 1.0;
		double alphaReductionRate = -3; // [deg/s]
		Amount<Length> wingToGroundDistance = Amount.valueOf(4.25, SI.METER);
		Amount<Length> obstacle = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		Amount<Velocity> vWind = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> iw = Amount.valueOf(2.0, NonSI.DEGREE_ANGLE);
		CalcTakeOff_Landing theTakeOffLandingCalculator = new CalcTakeOff_Landing(
				aircraft,
				theCondition,
				dtRot,
				dtHold,
				kcLMax,
				kRot,
				kLO,
				kFailure,
				k1,
				k2,
				phi,
				kAlphaDot,
				alphaReductionRate,
				mu,
				muBrake,
				wingToGroundDistance,
				obstacle,
				vWind,
				alphaGround,
				iw,
				cD0,
				oswald,
				cLmaxTO,
				cL0,
				cLalphaFlap,
				deltaCD0FlapLandingGear
				);

		theTakeOffLandingCalculator.calculateTakeOffDistanceODE(null, false);
		_stopTimeCalculation = System.currentTimeMillis();
		_startTimeGraph = System.currentTimeMillis();
		theTakeOffLandingCalculator.createTakeOffCharts();
		_stopTimeGraph = System.currentTimeMillis();
		_startTimeBalancedCalculation = System.currentTimeMillis();
		theTakeOffLandingCalculator.calculateBalancedFieldLength();
		_stopTimeBalancedCalculation = System.currentTimeMillis();
		_startTimeBalancedGraph = System.currentTimeMillis();
		theTakeOffLandingCalculator.createBalancedFieldLengthChart();
		_stopTimeBalancedGraph = System.currentTimeMillis();
		_stopTimeTotal = System.currentTimeMillis();

		_elapsedTimeTotal = _stopTimeTotal - _startTimeCalculation;
		_elapsedTimeCalculation = _stopTimeCalculation - _startTimeCalculation;
		_elapsedTimeGraph = _stopTimeGraph - _startTimeGraph;
		_elapsedTimeBalancedCalculation = _stopTimeBalancedCalculation - _startTimeBalancedCalculation;
		_elapsedTimeBalancedGraph = _stopTimeBalancedGraph - _startTimeBalancedGraph;

		System.out.println("\n------------------COMPUTATIONAL TIME-----------------------");
		System.out.println("\nANALYSIS TIME = " + (get_elapsedTime()) + " millisenconds");
		System.out.println("\nCALCULATION TIME = " + (get_elapsedTimeCalculation()) + " millisenconds");
		System.out.println("\nBALANCED FIELD LENGTH CALCULATION TIME = " + (get_elapsedTimeBalanced()) + " millisenconds");
		System.out.println("\nBALANCED FIELD LENGTH GRAPH TIME = " + (get_elapsedTimeBalancedGraph()) + " millisenconds");
		System.out.println("\nGRAPHICS TIME = " + (get_elapsedTimeGraph()) + " millisenconds");
		System.out.println("-----------------------------------------------------------\n");
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("\nBALANCED FIELD LENGTH = " + theTakeOffLandingCalculator.getBalancedFieldLength());
		System.out.println("\nDecision Speed (V1/VsTO) = " + theTakeOffLandingCalculator.getV1().divide(theTakeOffLandingCalculator.getvSTakeOff()));
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

	public static long get_elapsedTimeBalanced() {
		return _elapsedTimeBalancedCalculation;
	}

	public static long get_elapsedTimeBalancedGraph() {
		return _elapsedTimeBalancedGraph;
	}
}