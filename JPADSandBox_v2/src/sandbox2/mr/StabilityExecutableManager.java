package sandbox2.mr;

import static java.lang.Math.pow;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jboss.netty.util.internal.SystemPropertyUtil;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountException;

import aircraft.Aircraft;
import aircraft.components.LandingGears;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AirfoilCalc;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.aerodynamics.NasaBlackwell;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import javaslang.Tuple;
import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyVariableToWrite;

/************************************************************************************************************************
 * THIS CLASS IS A PROTOTYPE OF THE NEW ACStabilityManager																*
 * After it will be necessary to change the name of the class and to read the data from other analysis, not from file.  *
 * Moreover the Test_Stability class will be the new test class of the executable										*
 * 																														*
 * @author Manuela Ruocco																								*
 ***********************************************************************************************************************/

public class StabilityExecutableManager {

	//----------------------------------
	// VARIABLE DECLARATION			   ||
	// input						   ||
	//----------------------------------


	/** ****************************************************************************************************************************************
	 * When this class will begin the ACStabilityManager, these values will be read from the Aircraft that has to pass to the builder pattern *
	 * 												*																						 *
	 *****************************************************************************************************************************************/

	//Operating Conditions -------------------------------------------
	//----------------------------------------------------------------

	private String _aircraftName;
	private Amount<Length> _xCGAircraft;
	private Amount<Length> _yCGAircraft;
	private Amount<Length> _zCGAircraft;

	private Amount<Length> _altitude;
	private Double _machCurrent;
	private Double _reynoldsCurrent;

	private Amount<Angle> _alphaBodyInitial;
	private Amount<Angle> _alphaBodyFinal;
	private int _numberOfAlphasBody;
	private List<Amount<Angle>> _alphasBody;  // not from input

	private ConditionEnum _theCondition;

	private boolean _downwashConstant; // se TRUE--> constant, se FALSE--> variable

	private List<Double> _wingMomentumPole;  // pole adimentionalized on MAC
	private List<Double> _hTailMomentumPole; // pole adimentionalized on MAC
	
	private List<Amount<Angle>> _alphaWingForDistribution;
	private List<Amount<Angle>> _alphaHorizontalTailForDistribution;
	
	private Double _dynamicPressureRatio;

	
	
	//Wing -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Length> _xApexWing;
	private Amount<Length> _yApexWing;
	private Amount<Length> _zApexWing;
	
	private Amount<Length> _zACRootWing;

	private Amount<Area> _wingSurface;
	private Double _wingAspectRatio;
	private Amount<Length> _wingSpan;  //not from input
	private Amount<Length> _wingSemiSpan;  // not from input
	private int _wingNumberOfPointSemiSpanWise;
	private Double _wingAdimentionalKinkStation;
	private int _wingNumberOfGivenSections;
	private int _wingNumberOfGivenSectionsCLEAN;
	private Amount<Angle> _wingAngleOfIncidence;
	private Double _wingTaperRatio;
	private Amount<Angle> _wingSweepQuarterChord;
	private Amount<Angle> _wingSweepLE;
	private Double _wingVortexSemiSpanToSemiSpanRatio;
	private double cLAlphaMachZero;

	private AirfoilFamilyEnum _wingMeanAirfoilFamily;
	private Double _wingMaxThicknessMeanAirfoil;
	
	//airfoil input curve 
	private MethodEnum _wingairfoilLiftCoefficientCurve;
	private List<List<Amount<Angle>>> alphaAirfoilsWing = new ArrayList<>();
	private List<List<Double>> clDistributionAirfoilsWing = new ArrayList<List<Double>>();
	private List<List<Amount<Angle>>> _wingInducedAngleOfAttack = new ArrayList<>();
	private List<List<Double>> _wingCLAirfoilsDistribution = new ArrayList<List<Double>>();
	private List<List<Double>> _wingCLAirfoilsDistributionFinal = new ArrayList<List<Double>>();
	//input
	private MethodEnum _wingairfoilMomentCoefficientCurve;
	private List<List<Double>> _wingCLMomentAirfoilInput = new ArrayList<List<Double>>();
	private List<List<Double>> _wingCMMomentAirfoilInput = new ArrayList<List<Double>>();
	//output
	private List<Double> _wingCLMomentAirfoilOutput = new ArrayList<Double>();
	private List<List<Double>> _wingCMMomentAirfoilOutput = new ArrayList<List<Double>>();
	
	// input distributions 
	private List<Double> _wingYAdimensionalBreakPoints;
	private List<Amount<Length>> _wingYBreakPoints;
	private List<Double> _wingYAdimensionalDistribution;  // not from input
	private List<Amount<Length>> _wingYDistribution;
	private List<Amount<Length>> _wingChordsBreakPoints;
	private List<Amount<Length>> _wingChordsDistribution;  // not from input
	private List<Amount<Length>> _wingXleBreakPoints;
	private List<Amount<Length>> _wingXleDistribution;  // not from input
	private List<Amount<Angle>> _wingTwistBreakPoints;
	private List<Amount<Angle>> _wingTwistDistribution;  // not from input
	private List<Amount<Angle>> _wingDihedralBreakPoints;
	private List<Amount<Angle>> _wingDihedralDistribution;  // not from input
	private List<Amount<Angle>> _wingAlphaZeroLiftBreakPoints;
	private List<Amount<Angle>> _wingAlphaZeroLiftDistribution;  // not from input
	private List<Amount<Angle>> _wingAlphaStarBreakPoints;
	private List<Amount<Angle>> _wingAlphaStarDistribution;  // not from input
	private List<Double> _wingClMaxBreakPoints;
	private List<Double> _wingClMaxDistribution;  // not from input
	private List<Double> _wingCl0BreakPoints;
	private List<Double> _wingCl0Distribution;  // not from input
	private List<Double> _wingClAlphaBreakPointsDeg;
	private List<Double> _wingXACBreakPoints;
	private List<Double> _wingXACDistribution;  // not from input
	private List<Double> _wingCmACBreakPoints;
	private List<Double> _wingCmC4Distribution;  // not from input
	
	//clean 
	private List<Double> _wingYAdimensionalBreakPointsCLEAN;
	private List<Amount<Length>> _wingYBreakPointsCLEAN;
	private List<Double> _wingYAdimensionalDistributionCLEAN;  // not from input
	private List<Amount<Length>> _wingYDistributionCLEAN;
	private List<Amount<Length>> _wingChordsBreakPointsCLEAN;
	private List<Amount<Length>> _wingChordsDistributionCLEAN;  // not from input
	private List<Amount<Length>> _wingXleBreakPointsCLEAN;
	private List<Amount<Length>> _wingXleDistributionCLEAN;  // not from input
	private List<Amount<Angle>> _wingTwistBreakPointsCLEAN;
	private List<Amount<Angle>> _wingTwistDistributionCLEAN;  // not from input
	private List<Amount<Angle>> _wingDihedralBreakPointsCLEAN;
	private List<Amount<Angle>> _wingDihedralDistributionCLEAN;  // not from input
	private List<Amount<Angle>> _wingAlphaZeroLiftBreakPointsCLEAN;
	private List<Amount<Angle>> _wingAlphaZeroLiftDistributionCLEAN;  // not from input
	private List<Amount<Angle>> _wingAlphaStarBreakPointsCLEAN;
	private List<Amount<Angle>> _wingAlphaStarDistributionCLEAN;  // not from input
	private List<Double> _wingClMaxBreakPointsCLEAN;
	private List<Double> _wingClMaxDistributionCLEAN;  // not from input
	private List<Double> _wingCl0BreakPointsCLEAN;
	private List<Double> _wingCl0DistributionCLEAN;  // not from input
	private List<Double> _wingClAlphaBreakPointsDegCLEAN;
	private List<Double> _wingClAlphaDistributionDegCLEAN;
	private List<Double> _wingMaxThicknessBreakPointsCLEAN;
	private List<Double> _wingMaxThicknessDistributionCLEAN;  // not from input
	private List<Amount<Length>> _wingLERadiusBreakPointsCLEAN;
	private List<Amount<Length>> _wingLERadiusDistributionCLEAN;  // not from input

	private List<Double> _wingClAlphaDistributionDeg;
	private List<Double> _wingMaxThicknessBreakPoints;
	private List<Double> _wingMaxThicknessDistribution;  // not from input
	private List<Amount<Length>> _wingLERadiusBreakPoints;
	private List<Amount<Length>> _wingLERadiusDistribution;  // not from input
	private List<Amount<Length>> _wingYLEBreakPoints;
	private List<Amount<Length>> _wingYLEDistribution;  // not from input


	//High lift devices -------------------------------------------
	//----------------------------------------------------------------
	private int _wingNumberOfFlaps;
	private int _wingNumberOfSlats;
	private List<Amount<Angle>> _wingDeltaFlap;
	private List<FlapTypeEnum> _wingFlapType;
	private List<Double> _wingEtaInFlap;
	private List<Double> _wingEtaOutFlap;
	private List<Double> _wingFlapCfC;
	private List<Amount<Angle>> _wingDeltaSlat;
	private List<Double> _wingEtaInSlat;
	private List<Double> _wingEtaOutSlat;
	private List<Double> _wingSlatCsC;
	private List<Double> _wingCExtCSlat;
	private List<Double> _wingLeRadiusCSLat;
	private Map<Double, Map<Double, Double[]>> _supermappa = new HashMap<>();
	 

	//Fuselage -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Length> _fuselageDiameter;
	private Amount<Length> _fuselageLength;
	private Double _fuselageNoseFinessRatio;
	private Double _fuselageFinessRatio;
	private Double _fuselageTailFinessRatio;
	private Amount<Angle> _fuselageWindshieldAngle;
	private Amount<Angle> _fuselageUpSweepAngle;
	private Double _fuselageXPercentPositionPole;
	private Amount<Area> _fuselageFrontSurface;
	private double _cM0fuselage;
	private double _cMalphafuselage;
	
	//airfoil input curve 
	private List<Amount<Angle>> alphasFuselagePolar = new ArrayList<>(); // are in alfa body
	private List<Double> cdDistributionFuselage = new ArrayList<>();
	private List<Double> cdDistributionFuselageFinal = new ArrayList<>();

	//Horizontal Tail -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Length> _xApexHTail;
	private Amount<Length> _yApexHTail;
	private Amount<Length> _zApexHTail;

	private Amount<Length> _verticalTailSpan;
	
	private Amount<Area> _hTailSurface;
	private Double _hTailAspectRatio;
	private Double _hTailTaperRatio;
	private Amount<Length> _hTailSpan;  //not from input
	private Amount<Length> _hTailSemiSpan;  // not from input
	private int _hTailNumberOfPointSemiSpanWise;
	private Double _hTailadimentionalKinkStation;
	private int _hTailnumberOfGivenSections;
	private Amount<Angle> _hTailSweepLE;
	private Amount<Angle> _hTailSweepQuarterChord;

	private AirfoilFamilyEnum _hTailMeanAirfoilFamily;
	private Double _hTailMaxThicknessMeanAirfoil;
	private Double _hTailVortexSemiSpanToSemiSpanRatio;

	//airfoil input curve 
	private MethodEnum _hTailairfoilLiftCoefficientCurve;
	private List<List<Amount<Angle>>> alphaAirfoilsHTail = new ArrayList<>();
	private List<List<Double>> clDistributionAirfoilsHTail = new ArrayList<List<Double>>();
	private List<List<Double>> _hTailCLAirfoilsDistribution = new ArrayList<List<Double>>();
	private List<List<Amount<Angle>>> _hTailInducedAngleOfAttack = new ArrayList<>();
	private List<List<Double>> _hTailCLAirfoilsDistributionFinal = new ArrayList<List<Double>>();
	
	Amount<Length> _hTailHorizontalDistanceACtoCG, _hTailVerticalDistranceACtoCG;
	
	// input distributions
	private List<Double> _hTailYAdimensionalBreakPoints;
	private List<Amount<Length>> _hTailYBreakPoints;
	private List<Double> _hTailYAdimensionalDistribution;  // not from input
	private Amount<Angle> _hTailAngleOfIncidence;
	private List<Amount<Length>> _hTailYDistribution;
	private List<Amount<Length>> _hTailChordsBreakPoints;
	private List<Amount<Length>> _hTailChordsDistribution;  // not from input
	private List<Amount<Length>> _hTailXleBreakPoints;
	private List<Amount<Length>> _hTailXleDistribution;  // not from input
	private List<Amount<Angle>> _hTailTwistBreakPoints;
	private List<Amount<Angle>> _hTailTwistDistribution;  // not from input
	private List<Amount<Angle>> _hTailDihedralBreakPoints;
	private List<Amount<Angle>> _hTailDihedralDistribution;  // not from input
	private List<Amount<Angle>> _hTailAlphaZeroLiftBreakPoints;
	private List<Amount<Angle>> _hTailAlphaZeroLiftDistribution;  // not from input
	private List<Amount<Angle>> _hTailAlphaStarBreakPoints;
	private List<Amount<Angle>> _hTailAlphaStarDistribution;  // not from input
	private List<Double> _hTailClMaxBreakPoints;
	private List<Double> _hTailClMaxDistribution;  // not from input
	private List<Double> _hTailMaxThicknessBreakPoints;
	private List<Double> _hTailMaxThicknessDistribution;  // not from input 
	private List<Double> _hTailClAlphaBreakPointsDeg;
	private List<Double> _hTailCl0BreakPoints;
	private List<Double> _hTailCl0Distribution  = new ArrayList<>();  // not from input
	private List<Double> _hTailClAlphaistributionDeg  = new ArrayList<>();
	private List<Double> _hTailXACBreakPoints;
	private List<Double> _hTailXACDistribution;  // not from input
	private List<Double> _hTailCmACBreakPoints;
	private List<Double> _hTailCmC4Distribution;  // not from input

	//Elevator-------------------------------------------
	//----------------------------------------------------------------
	private List<Amount<Angle>> _anglesOfElevatorDeflection;
	private FlapTypeEnum _elevatorType;
	private Double _elevatorEtaIn;
	private Double _elevatorEtaOut;
	private Double _elevatorCfC;


	//Engines -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Angle> _tiltingAngle;
	// TODO: continue?

	//POLAR -------------------------------------------
	//----------------------------------------------------------------
	private MethodEnum _wingDragMethod;
	private MethodEnum _hTailDragMethod;

	//PLOT-----------------------------------------------
	//----------------------------------------------------------------
	private List<AerodynamicAndStabilityPlotEnum> _plotList = new ArrayList<AerodynamicAndStabilityPlotEnum>();
	private boolean _plotCheck;


	//OTHER VARIABLES-------------------------------------
	//----------------------------------------------------------------
	StabilityExecutableCalculator theStabilityCalculator = new StabilityExecutableCalculator();
	private Amount<Length> _horizontalDistanceQuarterChordWingHTail; // distance to use in Roskam downwash method. DIMENSIONAL
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTail;
	private Amount<Length> _horizontalDistanceQuarterChordWingHTailNOANGLE; // this is the distance between the ac of wing and h tail mesured along
	// the BRF 
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL; // this is the distance between the ac of wing and h tail mesured along
	// the BRF 
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	String databaseFolderPath;
	String aerodynamicDatabaseFileName;
	String highLiftDatabaseFileName;
	String fusDesDatabaseFileName;
	AerodynamicDatabaseReader aeroDatabaseReader;
	HighLiftDatabaseReader highLiftDatabaseReader;
	FusDesDatabaseReader fusDesDatabaseReader;
	double [] alphaZeroLiftRad;
	double [] twistDistributionRad;
	double [] alphaZeroLiftRadCLEAN;
	double [] twistDistributionRadCLEAN;
	double [] alphaZeroLiftRadHTail;
	double [] twistDistributionRadHTail;
	
	private double _deltaCD0Miscellaneus; // vertical, interference..
	private double _cDLandingGear;
	Amount<Length> _landingGearArm;
	Amount<Length> _zLandingGear;

	//Influence Areas
	//-----------------------------------------------------------------------------------
	double dimensionalOverKink;
	double influenceAreaRoot;
	double influenceAreaKink;
	double influenceAreaTip;
	double kRoot;
	double kKink;
	double kTip;
	double dimensionalOverKinkHTail;
	double influenceAreaRootHTail;
	double influenceAreaKinkHTail;
	double influenceAreaTipHTail;
	double kRootHTail;
	double kKinkHTail;
	double kTipHTail;

	//Calculators
	//--------------------------------------------------------------------------------------
	NasaBlackwell theNasaBlackwellCalculatorMachActualWingCLEAN;
	NasaBlackwell theNasaBlackwellCalculatorMachZeroCLEAN;
	NasaBlackwell theNasaBlackwellCalculatorMachActualWing;
	NasaBlackwell theNasaBlackwellCalculatorMachZero;
	NasaBlackwell theNasaBlackwellCalculatorMachActualHTail;

	//-------------------------------------------------------------------------------------------------------------------------
	//----------------------------------
	// VARIABLE DECLARATION			   ||
	// output    					   ||
	//----------------------------------

	//AnglesArray-------------------------------------
	//----------------------------------------------------------------
	private List<Amount<Angle>> _alphasWing;
	private List<Amount<Angle>> _alphasTail;
	private List<Double> _downwashGradientConstantRoskam;
	private List<Amount<Angle>> _downwashAngleConstantRoskam;
	private List<Double> _downwashGradientConstantSlingerland;
	private List<Amount<Angle>> _downwashAngleConstantSlingerland;
	private List<Double> _downwashGradientVariableSlingerland;
	private List<Amount<Angle>> _downwashAngleVariableSlingerland;
	private List<Amount<Angle>> _downwashAngleVariableSlingerlandOld;
	private List<Amount<Length>> _horizontalDistance;
	private List<Amount<Length>> _verticalDistance;
	private List<Amount<Length>> _horizontalDistanceConstant = new ArrayList<>();
	private List<Amount<Length>> _verticalDistanceConstant = new ArrayList<>();


	//Lift -------------------------------------------
	//----------------------------------------------------------------

	// wing
	private Amount<Angle> _wingAlphaZeroLift;
	private Amount<Angle> _wingalphaStar;
	private Amount<Angle> _wingalphaMaxLinear;
	private Amount<Angle> _wingalphaStall;
	private Double _wingcLZero;
	private Double _wingcLStar;
	private Double _wingcLMax;
	private Double _wingcLAlphaRad;
	private Double _wingcLAlphaDeg;
	private Amount<?> _wingclAlpha;
	private Double _cLAtAlpha;
	private Double[] _wingliftCoefficient3DCurve;
	private double [] _wingliftCoefficientDistributionatCLMax;
	private Double [] _wingclAlphaArray;
	private Double [] _wingclAlphaArrayHighLift;


	//High Lift--------------------------------------
	//----------------------------------------------------------------
	private Double _cLAtAlphaHighLift;
	private Amount<Angle> _alphaZeroLiftHighLift;
	private Amount<Angle> _alphaStarHighLift;
	private Amount<Angle> _alphaStallHighLift;
	private Double _cLZeroHighLift;
	private Double _cLStarHighLift;
	private Double _cLMaxHighLift;
	private Double _cLAlphaHighLiftDEG;
	private Double _cD0HighLift;
	private List<Double> _deltaCl0FlapList;
	private Double _deltaCl0Flap;
	private List<Double> _deltaCL0FlapList;
	private Double _deltaCL0Flap;
	private List<Double> _deltaClmaxFlapList;
	private Double _deltaClmaxFlap;
	private List<Double> _deltaCLmaxFlapList;
	private Double _deltaCLmaxFlap;
	private List<Double> _deltaClmaxSlatList;
	private Double _deltaClmaxSlat;
	private List<Double> _deltaCLmaxSlatList;
	private Double _deltaCLmaxSlat;
	private List<Double> _deltaCD0List;
	private Double _deltaCD0;
	private List<Double> _deltaCMc4List;
	private Double _deltaCMc4ListElevator;
	private Double _deltaCMc4;
	private Double _deltaCMc4Elevator;
	private Double[] _alphaArrayPlotHighLift;
	private Double[] _wingLiftCoefficient3DCurveHighLift;
	private Double[] _wingLiftCoefficient3DCurveHighLiftWINGARRAY;
	
	private double[] _wingLiftCoefficientModified;


	// Horizontal Tail clean
	private Amount<Angle> _hTailAlphaZeroLift;
	private Amount<Angle> _hTailalphaStar;
	private Amount<Angle> _hTailalphaMaxLinear;
	private Amount<Angle> _hTailalphaStall;
	private Double _hTailcLZero;
	private Double _hTailcLStar;
	private Double _hTailcLMax;
	private Double _hTailcLAlphaRad;
	private Double _hTailcLAlphaDeg;
	private Amount<?> _hTailclAlpha;
	private Double[] _hTailliftCoefficient3DCurve;
	private double [] _hTailliftCoefficientDistributionatCLMax;
	private Double [] _hTailclAlphaArray;
	private Double [] _hTailclAlphaArrayHighLift;

	//Elevator
	//these values are maps and the key is the angle of elevatordeflection
	private Map <Amount<Angle>, Double> _tauElevator = new HashMap<Amount<Angle>, Double>();
	
	private Map <Amount<Angle>, Double> _deltaCLMaxElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _deltaCD0Elevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _cLAlphaElevatorDeg = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _deltacLZeroElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double[]>  _hTailLiftCoefficient3DCurveWithElevator = new HashMap<Amount<Angle>, Double[]>();
	private Map <Amount<Angle>, Double[]>  _hTailDragCoefficient3DCurveWithElevator = new HashMap<Amount<Angle>, Double[]>();

	private Map <Amount<Angle>, Double> _hTailcLMaxElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Amount<Angle>> _hTailalphaZeroLiftElevator = new HashMap<Amount<Angle>, Amount<Angle>>();
	private Map <Amount<Angle>, Amount<Angle>> _hTailalphaStarElevator = new HashMap<Amount<Angle>,Amount<Angle>>();
	private Map <Amount<Angle>, Amount<Angle>> _hTailalphaStallLiftElevator = new HashMap<Amount<Angle>, Amount<Angle>>();
	private Map <Amount<Angle>, Double> _hTailCLZeroElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _hTailCLStarElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _hTailCLAlphaElevator = new HashMap<Amount<Angle>, Double>();

	// tau graph
	private Map <Amount<Angle>, Double> _tauElevatorArray = new HashMap<Amount<Angle>, Double>();
	List<Amount<Angle>> _deltaEAnglesArray = new ArrayList<>();

	//Actual
	private Amount<Angle> _wingAlphaZeroLiftCONDITION;
	private Amount<Angle> _wingalphaStarCONDITION;
	private Amount<Angle> _wingalphaMaxLinearCONDITION;
	private Amount<Angle> _wingalphaStallCONDITION;
	private Double _wingcLZeroCONDITION;
	private Double _wingcLStarCONDITION;
	private Double _wingcLMaxCONDITION;
	private Double _wingcLAlphaRadCONDITION;
	private Double _wingcLAlphaDegCONDITION;
	private Amount<?> _wingclAlphaCONDITION;
	private Double _cLAtAlphaCONDITION;
	private Double[] _wingliftCoefficient3DCurveCONDITION;
	private double [] _wingliftCoefficientDistributionatCLMaxCONDITION;
	private Double [] _wingclAlphaArrayCONDITION;

	//Fuselage 
	private Amount<?> _fuselageWingClAlphaDeg;
	private Double _fuselageWingClMax;
	private Double _fuselageWingClZero;
	private Amount<Angle> _fuselageWingAlphaStar;
	private Amount<Angle> _fuselageWingAlphaStall;
	private Double _fuselageWingClAlpha;
	private Double _fuselageWingCLStar;
	private Double[] _fuselagewingliftCoefficient3DCurve;

	//Total
	private  Map <Amount<Angle>, List<Double>> _totalLiftCoefficient = new HashMap<Amount<Angle>, List<Double>>();

	//Drag -------------------------------------------
	//----------------------------------------------------------------

	//wing
	private Double _wingCD0;
	private Double _wingOswaldFactor;
	private Double _wingCDInduced;
	private Double _wingCDWave;
	private Double[] _wingPolar3DCurve;
	private List<Double> _wingParasiteDragCoefficientDistribution = new ArrayList<Double>();
	private List<Double> _wingInducedDragCoefficientDistribution = new ArrayList<Double>();
	private List<Double> _wingInducedDragCoefficientDistributionParabolic = new ArrayList<Double>();
	//------------------------------------------------
	private List<Double> _wingDragCoefficient3DCurve = new ArrayList<Double>();
	private List<Double> _wingDragCoefficient3DCurveTemp = new ArrayList<Double>();
	private List<List<Double>> _wingAirfoilsCoefficientCurve;

	private MethodEnum _deltaDueToFlapMethod;
	//input
	//INPUT-wing
	private List<Double> cLWingDragPolar;
	private List<Double> cDPolarWing;

	//CALCULATED_INPUT_AIRFOIL-wing
	private List<Double> clListDragWing = new ArrayList<>();
	private List<Double> clListMomentWing = new ArrayList<>();
	private List<List<Double>> clPolarAirfoilWingDragPolar = new ArrayList<List<Double>>();
	private List<List<Double>> cDPolarAirfoilsWing= new ArrayList<List<Double>>();
	private List<List<Double>> _wingCdAirfoilDistributionInputStations = new ArrayList<List<Double>>();
	private List<List<Double>> _wingCdAirfoilDistribution = new ArrayList<List<Double>>();

	//INPUT-htail
	private List<Double> clListDragTail = new ArrayList<>();
	private List<Double> cLhTailDragPolar;
	private List<Double> cDPolarhTail;

	//CALCULATED_INPUT_AIRFOIL-htail
	private List<List<Double>>  clPolarAirfoilHTailDragPolar= new ArrayList<List<Double>>();
	private List<List<Double>> cDPolarAirfoilsHTail= new ArrayList<List<Double>>();
	private List<List<Double>> _hTailCdAirfoilDistribution = new ArrayList<List<Double>>();
	private List<List<Double>> _hTailCdAirfoilDistributionInputStations = new ArrayList<List<Double>>();


	//horizontal tail
	private Double _hTailCD0;
	private Double _hTailOswaldFactor;
	private Double _hTailCDInduced;
	private Double _hTailCDWave;
	private Double[] _hTailPolar3DCurve;
	private List<Double> _hTailParasiteDragCoefficientDistribution = new ArrayList<Double>();
	private List<Double> _hTailInducedDragCoefficientDistribution = new ArrayList<Double>();
	private List<Double> _hTailDragCoefficientDistribution;
	private List<Amount<Force>> _hTailDragDistribution;
	private List<Double> _hTailDragCoefficient3DCurve = new ArrayList<Double>();
	private Double[] _hTailliftCoefficient3DCurveCONDITION;
	
	//Total
	private  Map <Amount<Angle>, List<Double>> _totalDragPolar = new HashMap<Amount<Angle>, List<Double>>();

	//Moment -------------------------------------------
	//----------------------------------------------------------------
	
	//wing
	private Double _wingFinalMomentumPole;
	private Double _hTailFinalMomentumPole;
	
	private Map<MethodEnum, Amount<Length>> _wingXACLRF = new HashMap<MethodEnum, Amount<Length>>();
	private Map<MethodEnum, Amount<Length>> _wingXACMAC = new HashMap<MethodEnum, Amount<Length>>();
	private Map<MethodEnum, Double> _wingXACMACpercent = new HashMap<MethodEnum, Double>();
	private Map<MethodEnum, Amount<Length>> _wingXACBRF = new HashMap<MethodEnum, Amount<Length>>();
	private Amount<Length> _wingMAC;
	private Amount<Length>_wingMeanAerodynamicChordLeadingEdgeX;
	private Map<MethodEnum, List<Double>> _wingMomentCoefficientAC = new HashMap<MethodEnum, List<Double>>();
	private List<List<Double>>_wingMomentCoefficients = new ArrayList<>();
	private List<Double>_wingMomentCoefficientFinal = new ArrayList<>();
	private List<Double>_wingMomentCoefficientConstant = new ArrayList<>();
	private List<Double>_wingMomentCoefficientFinalACVariable = new ArrayList<>();
	private List<Double>_hTailMomentCoefficientFinal = new ArrayList<>();
	private Map<Amount<Angle>, List<Double>> _hTailMomentCoefficientFinalElevator = new HashMap<Amount<Angle>, List<Double>>();
	

	
	private Amount<Length> _wingZACMAC;
	private Amount<Length> _wingYACMAC;

	
	//h tail
	private Map<MethodEnum, Amount<Length>> _hTailXACLRF = new HashMap<MethodEnum, Amount<Length>>();
	private Map<MethodEnum, Amount<Length>> _hTailXACBRF = new HashMap<MethodEnum, Amount<Length>>();
	private Map<MethodEnum, Amount<Length>> _hTailXACMAC = new HashMap<MethodEnum, Amount<Length>>();
	private Map<MethodEnum, Double> _hTailXACMACpercent = new HashMap<MethodEnum, Double>();
	private Amount<Length> _hTailMAC;
	private Amount<Length> _hTailMeanAerodynamicChordLeadingEdgeX;
	private Map<MethodEnum, List<Double>> _hTailMomentCoefficientAC = new HashMap<MethodEnum, List<Double>>();
	private List<List<Double>>_hTailMomentCoefficients = new ArrayList<>();
	
	//fuselage
	private MethodEnum _fuselageMomentMethod;
	private Map<MethodEnum, Double> _fuselageCM0 = new HashMap<MethodEnum, Double>();
	private Map<MethodEnum, Double> _fuselageCMAlpha = new HashMap<MethodEnum, Double>();
	private List<Double> _fuselageMomentCoefficient = new ArrayList<Double>();
	private List<Double> _fuselageMomentCoefficientdueToDrag = new ArrayList<Double>();
	private Map<MethodEnum, Amount<Length>> _wingBodyXACBRF = new HashMap<MethodEnum, Amount<Length>>();
	private Double _deltaXACdueToFuselage;

	//landing gear 
	private List<Double> _landingGearMomentDueToDrag = new ArrayList<Double>();
	
	//Stability -------------------------------------------
	//----------------------------------------------------------------
	private List<Double> _wingNormalCoefficient = new ArrayList<>();
	private List<Double> _hTailNormalCoefficient = new ArrayList<>();
	private List<Double> _hTailNormalCoefficientDownwashConstant = new ArrayList<>();
	private List<Double> _wingHorizontalCoefficient= new ArrayList<>();
	private List<Double> _hTailHorizontalCoefficient= new ArrayList<>();
	private List<Double> _hTailHorizontalCoefficientDownwashConstant= new ArrayList<>();
	private List<Double> _wingMomentCoefficientNOPendular= new ArrayList<>();
	private List<Double> _wingMomentCoefficientPendular= new ArrayList<>();
	private List<Double> _hTailMomentCoefficientPendular= new ArrayList<>();
	private List<Double> _totalMomentCoefficientPendular= new ArrayList<>();
	private  Map <Amount<Angle>, List<Double>>_hTailNormalCoefficientDeltaE = new HashMap<Amount<Angle>, List<Double>>();
	private  Map <Amount<Angle>, List<Double>>_hTailHorizontalCoefficientDeltaE = new HashMap<Amount<Angle>, List<Double>>();
	private  Map <Amount<Angle>, List<Double>>_hTailMomentCoefficientPendularDeltaE = new HashMap<Amount<Angle>, List<Double>>();
	private  Map <Amount<Angle>, List<Double>>_totalMomentCoefficientPendularDeltaE = new HashMap<Amount<Angle>, List<Double>>();
	
	// cl equilibrium
	private List<Double> _hTailEquilibriumLiftCoefficient = new ArrayList<>();
	private List<Double> _totalEquilibriumLiftCoefficient= new ArrayList<>();
	private List<Double> _hTailEquilibriumLiftCoefficientConstant = new ArrayList<>();
	private List<Double> _totalEquilibriumLiftCoefficientConstant = new ArrayList<>();
	private List<Double> _totalTrimDrag = new ArrayList<>();
	private List<Amount<Angle>> _deltaEEquilibrium = new ArrayList<>();
	
	Map <Amount<Angle>, Double[]> _clMapForDeltaeElevator = new HashMap<Amount<Angle>, Double[]>();
	int numberOfIterationforDeltaE;
	//Distributions -------------------------------------------
	//----------------------------------------------------------------
	
	private List<List<Double>> _clWingDistribution = new ArrayList<>();
	private Double [] _cl3DCurveWingFlapped;
	private List<List<Double>> _clHtailDistribution = new ArrayList<>();
	private List<List<Double>> _centerOfPressureWingDistribution = new ArrayList<>();
	private List<List<Double>> _centerOfPressurehTailDistribution = new ArrayList<>();
	private List<List<Double>> _cMWingDistribution = new ArrayList<>();
	private List<List<Double>> _cMHTailDistribution = new ArrayList<>();
	private List<List<Amount<Angle>>> _alphaIWingDistribution = new ArrayList<>();
	private List<List<Amount<Angle>>> _alphaIHtailDistribution = new ArrayList<>();
	
	private List<double [] > _clNasaBlackwellDistributionModified = new ArrayList<>();
	
	
	//Flapped CL CURVE -------------------------------------------
	//----------------------------------------------------------------
	
	private Double _clZeroFlapped;
	private Double _clAlphaDegFlapped;
	private Double _clAlphaRadFlapped;
	private Amount<?> _wingclAlphaFlapped;
	private Double _clMaxFlapped;
	private Amount<Angle> _alphaStarFlapped;
	private Amount<Angle> _alphaStallFlapped;
	private Amount<Angle> _alphaStallLinearFlapped ;
	private Amount<Angle> _alphaZeroLiftFlapped;
	private List<Double> _clMaxDistributionFlapped;
	
	List<List<Double>> clDistributions; 
	Double[] cl3D;
	
	MethodEnum _horizontalTailCL;
	
	Amount<Length> _wingHorizontalDistanceACtoCG,_wingVerticalDistranceACtoCG;
	/*****************************************************************************************************************************************
	 * In this section the arrays are initialized. These initialization wont be made in the final version because                            *
	 * it will be recalled from the wing
	 * 												*																						 *
	 *****************************************************************************************************************************************/

	//----------------------------------
	//INITIALIZE DATA           	   ||
	//----------------------------------

	public void initializeData(){
		MyConfiguration.customizeAmountOutput();

		// Setup database(s)

		databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		highLiftDatabaseFileName = "HighLiftDatabase.h5";
		fusDesDatabaseFileName = "FusDes_database.h5";
		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
		fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFileName);

		// dependent variables
		numberOfIterationforDeltaE = 10;
		// alpha body array
				double[] alphaBodyTemp = new double[this._numberOfAlphasBody];
				alphaBodyTemp = MyArrayUtils.linspace(
						_alphaBodyInitial.doubleValue(NonSI.DEGREE_ANGLE),
						_alphaBodyFinal.doubleValue(NonSI.DEGREE_ANGLE),
						_numberOfAlphasBody);

				// fill the array of alpha Body

				this._alphasBody = new ArrayList<>();
				for (int i=0; i<_numberOfAlphasBody; i++){
					this._alphasBody.add(
							Amount.valueOf(alphaBodyTemp[i], NonSI.DEGREE_ANGLE)
							);
				}

		this._wingSpan = Amount.valueOf(
				Math.sqrt(
						this._wingSurface.doubleValue(SI.SQUARE_METRE) *
						(this._wingAspectRatio)),
				SI.METER
				);

		this._wingSemiSpan = Amount.valueOf(
				this._wingSpan.doubleValue(SI.METER)/2,
				SI.METER
				);

		this._hTailSpan = Amount.valueOf(
				Math.sqrt(
						this._hTailSurface.doubleValue(SI.SQUARE_METRE) *
						(this._hTailAspectRatio)),
				SI.METER
				);

		this._hTailSemiSpan = Amount.valueOf(
				this._hTailSpan.doubleValue(SI.METER)/2,
				SI.METER
				);

		this._elevatorType = FlapTypeEnum.PLAIN;

		this._wingTaperRatio = this._wingChordsBreakPoints.get(_wingNumberOfGivenSections-1).doubleValue(SI.METER)/
				this._wingChordsBreakPoints.get(0).doubleValue(SI.METER);

		this._hTailTaperRatio = this._hTailChordsBreakPoints.get(1).doubleValue(SI.METER)/
				this._hTailChordsBreakPoints.get(0).doubleValue(SI.METER);

		this._hTailSweepLE = Amount.valueOf(Math.atan(
				(_hTailXleBreakPoints.get(1).doubleValue(SI.METER)/_hTailSemiSpan.doubleValue(SI.METER))), SI.RADIAN);

		this._hTailSweepQuarterChord = Amount.valueOf(Math.atan(
				((_hTailXleBreakPoints.get(1).doubleValue(SI.METER)
						+(_hTailChordsBreakPoints.get(1).doubleValue(SI.METER)*0.25))-
						(_hTailChordsBreakPoints.get(0).doubleValue(SI.METER)*0.25))/
				_hTailSemiSpan.doubleValue(SI.METER)), SI.RADIAN);

		this._wingVortexSemiSpanToSemiSpanRatio = 1./(2*this._wingNumberOfPointSemiSpanWise);
		this._hTailVortexSemiSpanToSemiSpanRatio = 1./(2*this._hTailNumberOfPointSemiSpanWise);

		this._dynamicPressureRatio = AerodynamicCalc.calculateHTailDynamicPressureRatio(
				_zApexHTail.doubleValue(SI.METER)/_verticalTailSpan.doubleValue(SI.METER)
				);
		
				
		//---------------
		// Arrays        |
		//---------------

		//---------------
		// Distributions |
		//---------------

		// y adimensional distribution 
		//wing
		double[] yAdimentionalDistributionTemp = new double[this._wingNumberOfPointSemiSpanWise];
		yAdimentionalDistributionTemp = MyArrayUtils.linspace(
				0.0,
				1.0,
				this._wingNumberOfPointSemiSpanWise);

		this._wingYAdimensionalDistribution = new ArrayList<>();
		for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
			this._wingYAdimensionalDistribution.add(
					yAdimentionalDistributionTemp[i]
					);
		}

		//h tail
		yAdimentionalDistributionTemp = new double[this._hTailNumberOfPointSemiSpanWise];
		this._hTailYAdimensionalDistribution = new ArrayList<>();
		yAdimentionalDistributionTemp = MyArrayUtils.linspace(
				0.0,
				1.0,
				this._hTailNumberOfPointSemiSpanWise);

		for (int i=0; i<_hTailNumberOfPointSemiSpanWise; i++){
			this._hTailYAdimensionalDistribution.add(
					yAdimentionalDistributionTemp[i]
					);
		}

		// y dimensional distribution 
		// wing

		this._wingYBreakPoints = new ArrayList<>();
		for (int i=0; i<_wingNumberOfGivenSections; i++){
			this._wingYBreakPoints.add(
					Amount.valueOf((_wingYAdimensionalBreakPoints.get(i) * _wingSemiSpan.doubleValue(SI.METER)), SI.METER)
					);
		}

		if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
			this._wingYBreakPointsCLEAN = new ArrayList<>();
			for (int i=0; i<_wingNumberOfGivenSectionsCLEAN; i++){
				this._wingYBreakPointsCLEAN.add(
						Amount.valueOf((_wingYAdimensionalBreakPointsCLEAN.get(i) * _wingSemiSpan.doubleValue(SI.METER)), SI.METER)
						);
			}
			
			double[] yDistributionTemp = new double[this._wingNumberOfPointSemiSpanWise];
			yDistributionTemp = MyArrayUtils.linspace(
					0.0,
					this._wingSemiSpan.doubleValue(SI.METER),
					this._wingNumberOfPointSemiSpanWise);

			this._wingYDistributionCLEAN = new ArrayList<>();
			for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
				this._wingYDistributionCLEAN.add(
						Amount.valueOf(yDistributionTemp[i], SI.METER)
						);
			}
		
		}
		double[] yDistributionTemp = new double[this._wingNumberOfPointSemiSpanWise];
		yDistributionTemp = MyArrayUtils.linspace(
				0.0,
				this._wingSemiSpan.doubleValue(SI.METER),
				this._wingNumberOfPointSemiSpanWise);

		this._wingYDistribution = new ArrayList<>();
		for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
			this._wingYDistribution.add(
					Amount.valueOf(yDistributionTemp[i], SI.METER)
					);
		}

		//htail 
		yDistributionTemp = new double[this._hTailNumberOfPointSemiSpanWise];
		yDistributionTemp = MyArrayUtils.linspace(
				0.0,
				this._hTailSemiSpan.doubleValue(SI.METER),
				this._hTailNumberOfPointSemiSpanWise);

		this._hTailYDistribution = new ArrayList<>();
		for (int i=0; i<_hTailNumberOfPointSemiSpanWise; i++){
			this._hTailYDistribution.add(
					Amount.valueOf(yDistributionTemp[i], SI.METER)
					);
		}


		// Influence areas Wing
		if (_theCondition == ConditionEnum.CRUISE){
		if (this._wingNumberOfGivenSections == 3){
			dimensionalOverKink = _wingSemiSpan.doubleValue(SI.METER) - _wingYBreakPoints.get(1).doubleValue(SI.METER);
			influenceAreaRoot = _wingChordsBreakPoints.get(0).doubleValue(SI.METER) * _wingYBreakPoints.get(1).doubleValue(SI.METER)/2;
			influenceAreaKink = (_wingChordsBreakPoints.get(1).doubleValue(SI.METER) * _wingYBreakPoints.get(1).doubleValue(SI.METER)/2) +
					(_wingChordsBreakPoints.get(1).doubleValue(SI.METER) * dimensionalOverKink/2);
			influenceAreaTip = _wingChordsBreakPoints.get(2).doubleValue(SI.METER) * dimensionalOverKink/2;
			kRoot = 2*influenceAreaRoot/this._wingSurface.doubleValue(SI.SQUARE_METRE);
			kKink = 2*influenceAreaKink/this._wingSurface.doubleValue(SI.SQUARE_METRE);
			kTip = 2*influenceAreaTip/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		}

		if (this._wingNumberOfGivenSections == 2){			
			influenceAreaRoot = _wingChordsBreakPoints.get(0).doubleValue(SI.METER) * 
					_wingSemiSpan.doubleValue(SI.METER)/2;;
					influenceAreaTip = _wingChordsBreakPoints.get(1).doubleValue(SI.METER) * 
							_wingSemiSpan.doubleValue(SI.METER)/2;;
							kRoot = 2*influenceAreaRoot/this._wingSurface.doubleValue(SI.SQUARE_METRE);
							kTip = 2*influenceAreaTip/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		}
		}

		if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
		if (this._wingNumberOfGivenSectionsCLEAN == 3){
			dimensionalOverKink = _wingSemiSpan.doubleValue(SI.METER) - _wingYBreakPointsCLEAN.get(1).doubleValue(SI.METER);
			influenceAreaRoot = _wingChordsBreakPointsCLEAN.get(0).doubleValue(SI.METER) * _wingYBreakPointsCLEAN.get(1).doubleValue(SI.METER)/2;
			influenceAreaKink = (_wingChordsBreakPointsCLEAN.get(1).doubleValue(SI.METER) * _wingYBreakPointsCLEAN.get(1).doubleValue(SI.METER)/2) +
					(_wingChordsBreakPointsCLEAN.get(1).doubleValue(SI.METER) * dimensionalOverKink/2);
			influenceAreaTip = _wingChordsBreakPointsCLEAN.get(2).doubleValue(SI.METER) * dimensionalOverKink/2;
			kRoot = 2*influenceAreaRoot/this._wingSurface.doubleValue(SI.SQUARE_METRE);
			kKink = 2*influenceAreaKink/this._wingSurface.doubleValue(SI.SQUARE_METRE);
			kTip = 2*influenceAreaTip/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		}

		if (this._wingNumberOfGivenSectionsCLEAN == 2){			
			influenceAreaRoot = _wingChordsBreakPointsCLEAN.get(0).doubleValue(SI.METER) * 
					_wingSemiSpan.doubleValue(SI.METER)/2;;
					influenceAreaTip = _wingChordsBreakPointsCLEAN.get(1).doubleValue(SI.METER) * 
							_wingSemiSpan.doubleValue(SI.METER)/2;;
							kRoot = 2*influenceAreaRoot/this._wingSurface.doubleValue(SI.SQUARE_METRE);
							kTip = 2*influenceAreaTip/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		}
		}
		// Influence areas Htail

		influenceAreaRootHTail = _hTailChordsBreakPoints.get(0).doubleValue(SI.METER) * 
				_hTailSemiSpan.doubleValue(SI.METER)/2;;
				influenceAreaTipHTail = _hTailChordsBreakPoints.get(1).doubleValue(SI.METER) * 
						_hTailSemiSpan.doubleValue(SI.METER)/2;;
						kRootHTail = 2*influenceAreaRootHTail/this._hTailSurface.doubleValue(SI.SQUARE_METRE);
						kTipHTail = 2*influenceAreaTipHTail/this._hTailSurface.doubleValue(SI.SQUARE_METRE);


						//WING--------------------------------------------------------
						// chords
						Double [] chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingChordsBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingChordsDistribution = new ArrayList<>();
						for(int i=0; i<chordDistributionArray.length; i++)
							_wingChordsDistribution.add(Amount.valueOf(chordDistributionArray[i], SI.METER));

						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingChordsBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingChordsDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<chordDistributionArray.length; i++)
								_wingChordsDistributionCLEAN.add(Amount.valueOf(chordDistributionArray[i], SI.METER));
						}
						
						
						// xle
						Double [] xleDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingXleBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingXleDistribution = new ArrayList<>();
						for(int i=0; i<xleDistributionArray.length; i++)
							_wingXleDistribution.add(Amount.valueOf(xleDistributionArray[i], SI.METER));
						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							xleDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingXleBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingXleDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<xleDistributionArray.length; i++)
								_wingXleDistributionCLEAN.add(Amount.valueOf(xleDistributionArray[i], SI.METER));
						}

						// yle
						
						_wingYLEBreakPoints = new ArrayList<>();
						this._wingYLEDistribution = new ArrayList<>();
						if(_wingDihedralBreakPoints.get(0).equals(Amount.valueOf(0.0,NonSI.DEGREE_ANGLE))){
							for(int i=0; i<_wingYAdimensionalDistribution.size(); i++)
								_wingYLEDistribution.add(Amount.valueOf(0.0, SI.METER));
							}
						
						else{
						
						
						
				
						Amount<Length> yLETip = Amount.valueOf(
								_wingSemiSpan.doubleValue(SI.METER)*
								Math.tan(_wingDihedralBreakPoints.get(_wingNumberOfGivenSections-1).doubleValue(SI.RADIAN)),
								SI.METER
								);
						
						_wingYLEBreakPoints.add(0, Amount.valueOf(0.0,SI.METER));
						_wingYLEBreakPoints.add(1, yLETip);
						
						double[] yBreakPointsYLE ={0,1};
						Double [] yLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								yBreakPointsYLE,
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingYLEBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
					
						for(int i=0; i<yLEDistributionArray.length; i++)
							_wingYLEDistribution.add(Amount.valueOf(yLEDistributionArray[i], SI.METER));}
						
			
						
						
						// twist
						Double [] twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingTwistBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingTwistDistribution = new ArrayList<>();
						for(int i=0; i<twistDistributionArray.length; i++)
							_wingTwistDistribution.add(Amount.valueOf(twistDistributionArray[i], NonSI.DEGREE_ANGLE));
						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingTwistBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingTwistDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<twistDistributionArray.length; i++)
								_wingTwistDistributionCLEAN.add(Amount.valueOf(twistDistributionArray[i], NonSI.DEGREE_ANGLE));
						}

						// dihedral
						Double [] dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingDihedralDistribution = new ArrayList<>();
						for(int i=0; i<dihedralDistributionArray.length; i++)
							_wingDihedralDistribution.add(Amount.valueOf(dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingDihedralDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<dihedralDistributionArray.length; i++)
								_wingDihedralDistributionCLEAN.add(Amount.valueOf(dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
						}

						// alpha zero lift
						Double [] alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaZeroLiftBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingAlphaZeroLiftDistribution = new ArrayList<>();
						for(int i=0; i<alphaZeroLiftDistributionArray.length; i++)
							_wingAlphaZeroLiftDistribution.add(Amount.valueOf(alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));

						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaZeroLiftBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingAlphaZeroLiftDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<alphaZeroLiftDistributionArray.length; i++)
								_wingAlphaZeroLiftDistributionCLEAN.add(Amount.valueOf(alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
						}
						
						// alpha star
						Double [] alphaStarDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaStarBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingAlphaStarDistribution = new ArrayList<>();
						for(int i=0; i<alphaStarDistributionArray.length; i++)
							_wingAlphaStarDistribution.add(Amount.valueOf(alphaStarDistributionArray[i], NonSI.DEGREE_ANGLE));

						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
							alphaStarDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaStarBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingAlphaStarDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<alphaStarDistributionArray.length; i++)
								_wingAlphaStarDistributionCLEAN.add(Amount.valueOf(alphaStarDistributionArray[i], NonSI.DEGREE_ANGLE));
						}
						
						// cl max
						Double [] clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingClMaxBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingClMaxDistribution = new ArrayList<>();
						for(int i=0; i<clMaxDistributionArray.length; i++)
							_wingClMaxDistribution.add(clMaxDistributionArray[i]);

						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){ 
							clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingClMaxBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingClMaxDistributionCLEAN = new ArrayList<>();
							for(int i=0; i<clMaxDistributionArray.length; i++)
								_wingClMaxDistributionCLEAN.add(clMaxDistributionArray[i]);
						}
						//cl alpha
						Double [] clAlphaDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingClAlphaBreakPointsDeg),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingClAlphaDistributionDeg = new ArrayList<>();
						for(int i=0; i<clAlphaDistributionArray.length; i++)
							_wingClAlphaDistributionDeg.add(clAlphaDistributionArray[i]);

						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){ 
							clAlphaDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingClAlphaBreakPointsDegCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingClAlphaDistributionDegCLEAN = new ArrayList<>();
							for(int i=0; i<clAlphaDistributionArray.length; i++)
								_wingClAlphaDistributionDegCLEAN.add(clAlphaDistributionArray[i]);
						}
						//cl zero
						this._wingCl0BreakPoints = new ArrayList<>();
						this._wingCl0Distribution = new ArrayList<>();
						for (int i=0; i<_wingNumberOfGivenSections; i++){
							this._wingCl0BreakPoints.add(i, - this._wingAlphaZeroLiftBreakPoints.get(i).doubleValue(NonSI.DEGREE_ANGLE)*
									this._wingClAlphaBreakPointsDeg.get(i)) ;
						}
						Double [] clZeroDistribution = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingCl0BreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						for(int i=0; i<clZeroDistribution.length; i++)
							_wingCl0Distribution.add(clZeroDistribution[i]);

						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){ 
							this._wingCl0BreakPointsCLEAN = new ArrayList<>();
							this._wingCl0DistributionCLEAN = new ArrayList<>();
							for (int i=0; i<_wingNumberOfGivenSectionsCLEAN; i++){
								this._wingCl0BreakPointsCLEAN.add(i, - this._wingAlphaZeroLiftBreakPointsCLEAN.get(i).doubleValue(NonSI.DEGREE_ANGLE)*
										this._wingClAlphaBreakPointsDegCLEAN.get(i)) ;
							}
							clZeroDistribution = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingCl0BreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							for(int i=0; i<clZeroDistribution.length; i++)
								_wingCl0DistributionCLEAN.add(clZeroDistribution[i]);
						}

						// max thickness
						Double [] maxThicknessDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingMaxThicknessBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingMaxThicknessDistribution = new ArrayList<>();
						for(int i=0; i<maxThicknessDistributionArray.length; i++)
							_wingMaxThicknessDistribution.add(maxThicknessDistributionArray[i]);

						// max thickness mean airfoil
						if (this._wingNumberOfGivenSections == 3){
							this._wingMaxThicknessMeanAirfoil =  _wingMaxThicknessBreakPoints.get(0) * kRoot +
									_wingMaxThicknessBreakPoints.get(1) * kKink + 
									_wingMaxThicknessBreakPoints.get(2) * kTip;
						}
						if (this._wingNumberOfGivenSections == 2){
							this._wingMaxThicknessMeanAirfoil =  _wingMaxThicknessBreakPoints.get(0) * kRoot +
									_wingMaxThicknessBreakPoints.get(1) * kTip;
						}

						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){ 
							maxThicknessDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingMaxThicknessBreakPointsCLEAN),
									MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
									);	
							this._wingMaxThicknessDistribution = new ArrayList<>();
							for(int i=0; i<maxThicknessDistributionArray.length; i++)
								_wingMaxThicknessDistribution.add(maxThicknessDistributionArray[i]);

							// max thickness mean airfoil
							if (this._wingNumberOfGivenSectionsCLEAN == 3){
								this._wingMaxThicknessMeanAirfoil =  _wingMaxThicknessBreakPointsCLEAN.get(0) * kRoot +
										_wingMaxThicknessBreakPointsCLEAN.get(1) * kKink + 
										_wingMaxThicknessBreakPointsCLEAN.get(2) * kTip;
							}
							if (this._wingNumberOfGivenSectionsCLEAN == 2){
								this._wingMaxThicknessMeanAirfoil =  _wingMaxThicknessBreakPointsCLEAN.get(0) * kRoot +
										_wingMaxThicknessBreakPointsCLEAN.get(1) * kTip;
							}
						}
						//x ac airfoils
						Double [] xacDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingXACBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingXACDistribution = new ArrayList<>();
						for(int i=0; i<xacDistributionArray.length; i++)
							_wingXACDistribution.add(xacDistributionArray[i]);
						
						//cmac airfoil
						Double [] cmacDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingCmACBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingCmC4Distribution = new ArrayList<>();
						for(int i=0; i<cmacDistributionArray.length; i++)
							_wingCmC4Distribution.add(cmacDistributionArray[i]);
						
						//HTAIL----------------------------
						// chords
						Double [] chordDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailChordsBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailChordsDistribution = new ArrayList<>();
						for(int i=0; i<chordDistributionArrayHtail.length; i++)
							_hTailChordsDistribution.add(Amount.valueOf(chordDistributionArrayHtail[i], SI.METER));

						// xle
						Double [] xleDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailXleBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailXleDistribution = new ArrayList<>();
						for(int i=0; i<xleDistributionArrayHtail.length; i++)
							_hTailXleDistribution.add(Amount.valueOf(xleDistributionArrayHtail[i], SI.METER));

						// twist
						Double [] twistDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailTwistBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailTwistDistribution = new ArrayList<>();
						for(int i=0; i<twistDistributionArrayHtail.length; i++)
							_hTailTwistDistribution.add(Amount.valueOf(twistDistributionArrayHtail[i], NonSI.DEGREE_ANGLE));

						// dihedral
						Double [] dihedralDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailDihedralBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailDihedralDistribution = new ArrayList<>();
						for(int i=0; i<dihedralDistributionArrayHtail.length; i++)
							_hTailDihedralDistribution.add(Amount.valueOf(dihedralDistributionArrayHtail[i], NonSI.DEGREE_ANGLE));

						// alpha zero lift
						Double [] alphaZeroLiftDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailAlphaZeroLiftBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailAlphaZeroLiftDistribution = new ArrayList<>();
						for(int i=0; i<alphaZeroLiftDistributionArrayHtail.length; i++)
							_hTailAlphaZeroLiftDistribution.add(Amount.valueOf(alphaZeroLiftDistributionArrayHtail[i], NonSI.DEGREE_ANGLE));

						// alpha star
						Double [] alphaStarDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_hTailAlphaStarBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailAlphaStarDistribution = new ArrayList<>();
						for(int i=0; i<alphaStarDistributionArrayHtail.length; i++)
							_hTailAlphaStarDistribution.add(Amount.valueOf(alphaStarDistributionArrayHtail[i], NonSI.DEGREE_ANGLE));

						// cl max
						Double [] clMaxDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailClMaxBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailClMaxDistribution = new ArrayList<>();
						for(int i=0; i<clMaxDistributionArrayHtail.length; i++)
							_hTailClMaxDistribution.add(clMaxDistributionArrayHtail[i]);
						
						//x ac airfoils
						Double [] xacDistributionArrayHtail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailXACBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailXACDistribution = new ArrayList<>();
						for(int i=0; i<xacDistributionArrayHtail.length; i++)
							_hTailXACDistribution.add(xacDistributionArrayHtail[i]);
						
						//cmac airfoil
						Double [] cmacDistributionArrayHTail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailCmACBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailCmC4Distribution = new ArrayList<>();
						for(int i=0; i<cmacDistributionArrayHTail.length; i++)
							_hTailCmC4Distribution.add(cmacDistributionArrayHTail[i]);


						alphaZeroLiftRad = new double [_wingNumberOfPointSemiSpanWise];
						twistDistributionRad = new double [_wingNumberOfPointSemiSpanWise];
						for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
							alphaZeroLiftRad[i] = _wingAlphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
							twistDistributionRad[i] =  _wingTwistDistribution.get(i).doubleValue(SI.RADIAN);
						}
						
						if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition== ConditionEnum.LANDING){
						alphaZeroLiftRadCLEAN = new double [_wingNumberOfPointSemiSpanWise];
						twistDistributionRadCLEAN = new double [_wingNumberOfPointSemiSpanWise];
						for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
							alphaZeroLiftRadCLEAN[i] = _wingAlphaZeroLiftDistributionCLEAN.get(i).doubleValue(SI.RADIAN);
							twistDistributionRadCLEAN[i] =  _wingTwistDistributionCLEAN.get(i).doubleValue(SI.RADIAN);
						}
						}

						alphaZeroLiftRadHTail = new double [_hTailNumberOfPointSemiSpanWise];
						twistDistributionRadHTail = new double [_hTailNumberOfPointSemiSpanWise];
						for (int i=0; i<_hTailNumberOfPointSemiSpanWise; i++){
							alphaZeroLiftRadHTail[i] = _hTailAlphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
							twistDistributionRadHTail[i] =  _hTailTwistDistribution.get(i).doubleValue(SI.RADIAN);
						}


						// max thickness
						// max thickness mean airfoil
						this._hTailMaxThicknessMeanAirfoil = _hTailMaxThicknessBreakPoints.get(0)*kRootHTail + 
								_hTailMaxThicknessBreakPoints.get(1)*kTipHTail;

						// clzero

						this._hTailCl0BreakPoints = new ArrayList<>();
						for (int i=0; i<_hTailnumberOfGivenSections; i++){
							this._hTailCl0BreakPoints.add(i,
									- this._hTailAlphaZeroLiftBreakPoints.get(i).doubleValue(NonSI.DEGREE_ANGLE)*
									this._hTailClAlphaBreakPointsDeg.get(i)) ;
						}
						
						//cl alpha
						Double [] clAlphaDistributionArrayTail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailClAlphaBreakPointsDeg),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						this._hTailClAlphaistributionDeg = new ArrayList<>();
						for(int i=0; i<clAlphaDistributionArrayTail.length; i++)
							_hTailClAlphaistributionDeg.add(clAlphaDistributionArrayTail[i]);

						//cl zero
			
						Double [] clZeroDistributionTail = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailCl0BreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_hTailYAdimensionalDistribution)
								);	
						for(int i=0; i<clZeroDistributionTail.length; i++)
							_hTailCl0Distribution.add(clZeroDistributionTail[i]);


						//---------------
						// Other values       |
						//---------------

						// zAC root wing
						
						double deltaZ = _wingXACBreakPoints.get(0)*
								_wingChordsBreakPoints.get(0).doubleValue(SI.METER)*
								Math.tan(_wingAngleOfIncidence.doubleValue(SI.RADIAN));

						_zACRootWing = Amount.valueOf(_zApexWing.doubleValue(SI.METER) - deltaZ, SI.METER);
						
						//Horizontal and vertical distance
						this._horizontalDistanceQuarterChordWingHTail = Amount.valueOf(
								(this._xApexHTail.doubleValue(SI.METER) + this._hTailChordsBreakPoints.get(0).doubleValue(SI.METER)/4)- 
								(this._xApexWing.doubleValue(SI.METER) + this._wingChordsBreakPoints.get(0).doubleValue(SI.METER)/4),
								SI.METER
								);

						if ( (this._zApexWing.doubleValue(SI.METER) > 0 && this._zApexHTail.doubleValue(SI.METER) > 0 ) ||
								(this._zApexWing.doubleValue(SI.METER) < 0 && this._zApexHTail.doubleValue(SI.METER) < 0 )){
							this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
									this._zApexHTail.doubleValue(SI.METER) - this._zACRootWing.doubleValue(SI.METER), SI.METER);
						}

						if ( (this._zApexWing.doubleValue(SI.METER) > 0 && this._zApexHTail.doubleValue(SI.METER) < 0 ) ||
								(this._zApexWing.doubleValue(SI.METER) < 0 && this._zApexHTail.doubleValue(SI.METER) > 0 )){ // different sides
							if(this._zApexWing.doubleValue(SI.METER) < 0 ){
								this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
										this._zApexHTail.doubleValue(SI.METER) + Math.abs(this._zACRootWing.doubleValue(SI.METER)), SI.METER);	
							}

							if(this._zApexWing.doubleValue(SI.METER) > 0 ){
								this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
										-( Math.abs(this._zApexHTail.doubleValue(SI.METER)) + this._zACRootWing.doubleValue(SI.METER)), SI.METER);	
							}
						}

						// vertical and horizontal distances from AC

						this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = this._verticalDistanceZeroLiftDirectionWingHTail ;		

						// the horizontal distance is always the same, the vertical changes in function of the angle of attack.

						
	// FUSELAGE -----------
						
 _fuselageFrontSurface = Amount.valueOf(Math.PI*Math.pow(_fuselageDiameter.doubleValue(SI.METER), 2)/4, SI.SQUARE_METRE);

 cdDistributionFuselageFinal	= MyArrayUtils.convertDoubleArrayToListDouble(
		 MyMathUtils.getInterpolatedValue1DLinear(
				 MyArrayUtils.convertListOfAmountTodoubleArray(alphasFuselagePolar), 
				 MyArrayUtils.convertToDoublePrimitive(cdDistributionFuselage), 
				 MyArrayUtils.convertListOfAmountTodoubleArray(_alphasBody)
				 )
		 );
	}

	public void initializeCalculators(){	
		
		List<Amount<Angle>> _wingDihedralDistributionNull = new ArrayList<>() ; 
		for (int i=0; i<_wingDihedralDistribution.size(); i++){
			_wingDihedralDistributionNull.add(i, Amount.valueOf(0.0, SI.RADIAN));
		}
		//NASA BLACKWELL
		// wing
		theNasaBlackwellCalculatorMachActualWing = new NasaBlackwell(
				this._wingSemiSpan.doubleValue(SI.METER),
				this._wingSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
				_wingTwistDistribution,
				_wingAlphaStarDistribution,
				_wingVortexSemiSpanToSemiSpanRatio,
				0.0,
				this._machCurrent,
				this.getAltitude().doubleValue(SI.METER)
				);

		theNasaBlackwellCalculatorMachZero = new NasaBlackwell(
				this._wingSemiSpan.doubleValue(SI.METER),
				this._wingSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
				_wingTwistDistribution,
				_wingAlphaStarDistribution,
				_wingVortexSemiSpanToSemiSpanRatio,
				0.0,
				0.0,
				this.getAltitude().doubleValue(SI.METER)
				);
		
		if( _theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
			theNasaBlackwellCalculatorMachActualWingCLEAN = new NasaBlackwell(
					this._wingSemiSpan.doubleValue(SI.METER),
					this._wingSurface.doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
					_wingTwistDistributionCLEAN,
					_wingAlphaStarDistributionCLEAN,
					_wingVortexSemiSpanToSemiSpanRatio,
					0.0,
					this._machCurrent,
					this.getAltitude().doubleValue(SI.METER)
					);

			theNasaBlackwellCalculatorMachZeroCLEAN = new NasaBlackwell(
					this._wingSemiSpan.doubleValue(SI.METER),
					this._wingSurface.doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistributionCLEAN),
					MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
					_wingTwistDistributionCLEAN,
					_wingAlphaStarDistributionCLEAN,
					_wingVortexSemiSpanToSemiSpanRatio,
					0.0,
					0.0,
					this.getAltitude().doubleValue(SI.METER)
					);
		}

		// horizontal tail
		theNasaBlackwellCalculatorMachActualHTail = new NasaBlackwell(
				this._hTailSemiSpan.doubleValue(SI.METER),
				this._hTailSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
				_hTailTwistDistribution,
				_hTailAlphaZeroLiftDistribution,
				_hTailVortexSemiSpanToSemiSpanRatio,
				0.0,
				this._machCurrent,
				this.getAltitude().doubleValue(SI.METER)
				);


	}

	/*****************************************************************************************************************************************
	 * In this section the alphas arrays are initialized. These initialization will be also in the final version
	 * 												*																						 *
	 *****************************************************************************************************************************************/

	public void initializeAlphaArrays(){	

		// alpha body array
		double[] alphaBodyTemp = new double[this._numberOfAlphasBody];
		alphaBodyTemp = MyArrayUtils.linspace(
				_alphaBodyInitial.doubleValue(NonSI.DEGREE_ANGLE),
				_alphaBodyFinal.doubleValue(NonSI.DEGREE_ANGLE),
				_numberOfAlphasBody);

		// fill the array of alpha Body

		this._alphasBody = new ArrayList<>();
		for (int i=0; i<_numberOfAlphasBody; i++){
			this._alphasBody.add(
					Amount.valueOf(alphaBodyTemp[i], NonSI.DEGREE_ANGLE)
					);
		}

		// alpha wing array CLEAN
		this._alphasWing = new ArrayList<>();
		for (int i=0; i<_numberOfAlphasBody; i++){
			this._alphasWing.add(
					Amount.valueOf((
							this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))
							, NonSI.DEGREE_ANGLE
							)
					);
		}

	}

	public void initializeDownwashArray(){ 
		this._downwashAngleVariableSlingerland = new ArrayList<>();
		this._downwashGradientVariableSlingerland = new ArrayList<>();
		this._downwashAngleConstantRoskam = new ArrayList<>();
		this._downwashGradientConstantRoskam = new ArrayList<>();
		this._downwashAngleConstantSlingerland = new ArrayList<>();
		this._downwashGradientConstantSlingerland = new ArrayList<>();

		//DISTANCES
		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.
		if (this._zApexWing.doubleValue(SI.METER) < this._zApexHTail.doubleValue(SI.METER)  ){

			this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) + (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
									Math.tan(this._wingAngleOfIncidence.doubleValue(SI.RADIAN) -
											this._wingAlphaZeroLiftCONDITION.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		if (this._zApexWing.doubleValue(SI.METER) > this._zApexHTail.doubleValue(SI.METER)  ){

			this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) - (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
									Math.tan(this._wingAngleOfIncidence.doubleValue(SI.RADIAN) -
											this._wingAlphaZeroLiftCONDITION.doubleValue(SI.RADIAN)))),
					SI.METER);
		}


		this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
				this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE.doubleValue(SI.METER) * 
				Math.cos(this._wingAngleOfIncidence.doubleValue(SI.RADIAN)-
						this._wingAlphaZeroLiftCONDITION.doubleValue(SI.RADIAN)), SI.METER);


		//ROSKAM -----------------------------------------
		//----------------------------------------------------------------------------------------------		

		// calculate cl alpha at M=0 and M=current

		theNasaBlackwellCalculatorMachZero.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachZero = theNasaBlackwellCalculatorMachZero.getCLCurrent();
		theNasaBlackwellCalculatorMachZero.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachZero = theNasaBlackwellCalculatorMachZero.getCLCurrent();
		cLAlphaMachZero = (clTwoMachZero-clOneMachZero)/toRadians(4);

		// Roskam method
		double downwashGradientConstant = AerodynamicCalc.calculateDownwashRoskamWithMachEffect(
				this._wingAspectRatio, 
				this._wingTaperRatio, 
				this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER)/this._wingSpan.doubleValue(SI.METER), 
				this._verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER)/this._wingSpan.doubleValue(SI.METER), 
				this._wingSweepQuarterChord,
				cLAlphaMachZero, 
				this._wingcLAlphaRad
				);

		for (int i=0; i<this._numberOfAlphasBody; i++){
			_downwashGradientConstantRoskam.add(downwashGradientConstant);}

		double epsilonZeroRoskam = - _downwashGradientConstantRoskam.get(0) * _wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE);
		//fill the downwash array
		for (int i=0; i<this._numberOfAlphasBody; i++){
			this._downwashAngleConstantRoskam.add(i,
					Amount.valueOf( epsilonZeroRoskam + _downwashGradientConstantRoskam.get(i)* _alphasWing.get(i).doubleValue(NonSI.DEGREE_ANGLE)
							, NonSI.DEGREE_ANGLE));
		}


		//Slingerland

//		double downwashGradientConstantSlingerland = theStabilityCalculator.calculateDownwashGradientSlingerland(
//				_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
//				_verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER), 
//				this._wingcLAlphaRadCONDITION, 
//				this._wingSweepQuarterChord,
//				this._wingAspectRatio, 
//				this._wingSemiSpan);
//
//		for (int i=0; i<this._numberOfAlphasBody; i++){
//			_downwashGradientConstantSlingerland.add(downwashGradientConstantSlingerland);
//		}
//
//		double epsilonZeroSlingerland = - downwashGradientConstantSlingerland * 
//				_wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE);
//
//		//fill the downwash array
//		for (int i=0; i<this._numberOfAlphasBody; i++){
//			this._downwashAngleConstantSlingerland.add(i,
//					Amount.valueOf( epsilonZeroSlingerland + 
//							_downwashGradientConstantSlingerland.get(i)*
//							_alphasWing.get(i).doubleValue(NonSI.DEGREE_ANGLE)
//							, NonSI.DEGREE_ANGLE));
//		}
		
		for (int i=0; i<this._numberOfAlphasBody; i++){
			_downwashGradientConstantSlingerland.add(0.0);}
		
		
		for (int i=0; i<this._numberOfAlphasBody; i++){
			double cl = _wingcLAlphaDeg*_alphasWing.get(i).doubleValue(NonSI.DEGREE_ANGLE)+_wingcLZeroCONDITION;
			
			double downwashConstantSlingerland = theStabilityCalculator.calculateDownwashGradientSlingerlandNew(
					_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
					_verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER), 
					cl, 
					this._wingSweepQuarterChord,
					this._wingAspectRatio, 
					this._wingSemiSpan);
			
			this._downwashAngleConstantSlingerland.add(i,
					Amount.valueOf(Math.toDegrees(downwashConstantSlingerland),NonSI.DEGREE_ANGLE)
					);
			}
		


		//--------------end linear downwash-----------------------------------------


//			theStabilityCalculator.calculateDownwashNonLinearSlingerland(
//					this, 
//					_horizontalDistanceQuarterChordWingHTail,
//					_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
//					MyArrayUtils.convertToDoublePrimitive(this._wingclAlphaArrayCONDITION),
//					MyArrayUtils.convertListOfAmountTodoubleArray(_alphasWing),
//					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasBody)));
//
//			_downwashAngleVariableSlingerlandOld= theStabilityCalculator.getDownwashAngle();

	  //-------------------new downwash---------------------------------------------
			

			theStabilityCalculator.calculateDownwashNonLinearSlingerlandNew(
					this, 
					_horizontalDistanceQuarterChordWingHTail,
					_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
					MyArrayUtils.convertToDoublePrimitive(this._wingliftCoefficient3DCurveCONDITION),
					MyArrayUtils.convertListOfAmountTodoubleArray(_alphasWing),
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasBody)));

			_downwashGradientVariableSlingerland = theStabilityCalculator.getDownwashGradient();
			_downwashAngleVariableSlingerland= theStabilityCalculator.getDownwashAngle();
			_horizontalDistance = theStabilityCalculator.getHorizontalDistance();
			_verticalDistance = theStabilityCalculator.getVerticalDistance();


			for (int i=0; i<this._numberOfAlphasBody; i++){
				_verticalDistanceConstant.add(i, theStabilityCalculator.getVerticalDistanceConstant());
				_horizontalDistanceConstant.add(i,_horizontalDistance.get(0));
			}

		}

	public void initializeHTailArray(){ 

		if ( _downwashConstant == Boolean.TRUE){
			this._alphasTail = new ArrayList<>();
			for (int i=0; i<_numberOfAlphasBody; i++){
				this._alphasTail.add(
						Amount.valueOf((
								this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) )-
								this._downwashAngleConstantSlingerland.get(i).doubleValue(NonSI.DEGREE_ANGLE)+this._hTailAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE)
								, NonSI.DEGREE_ANGLE
								)
						);
			}
		}

		if ( _downwashConstant == Boolean.FALSE){
			this._alphasTail = new ArrayList<>();
			for (int i=0; i<_numberOfAlphasBody; i++){
				this._alphasTail.add(
						Amount.valueOf((
								this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) )-
								this._downwashAngleVariableSlingerland.get(i).doubleValue(NonSI.DEGREE_ANGLE)+this._hTailAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE)
								, NonSI.DEGREE_ANGLE
								)
						);
			}
		}
	}
	/*****************************************************************************************************************************************
	 * When this class will begin the ACStabilityManager, this method will be simply eliminated            									 *
	 * 												*																						 *
	 *****************************************************************************************************************************************/


	public void printAllData(){


		System.out.println("LONGITUDINAL STATIC STABILITY TEST");
		System.out.println("------------------------------------------------------------------------------");

		System.out.println("\n\n*------------------------------------------*");
		System.out.println("*              Aircraft Data               *");
		System.out.println("*------------------------------------------*\n");

		System.out.println("\nOPERATING CONDITIONS ----------\n-------------------");
		System.out.println("X CG of Aircraft in BRF = " + this._xCGAircraft);
		System.out.println("Y CG of Aircraft in BRF = " + this._yCGAircraft);
		System.out.println("Z CG of Aircraft in BRF = " + this._zCGAircraft);
		System.out.println("\nFlight condition --> " + this._theCondition +"\n");
		System.out.println("Mach number Current = " + this._machCurrent);
		System.out.println("Reynolds number Current = " + this._reynoldsCurrent);
		System.out.println("Altitude = " + this._altitude);
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._alphasBody, "Alphas Body array", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._alphasWing, "Alphas Wing array", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._alphasTail, "Alphas Tail array", ",");

		System.out.println("\n\nWING ----------\n-------------------");
		System.out.println("X apex = " + this._xApexWing);
		System.out.println("Y apex = " + this._yApexWing);
		System.out.println("Z apex = " + this._zApexWing);
		System.out.println("Number of points along semispan for analysis = " + this._wingNumberOfPointSemiSpanWise);
		System.out.println("Surface = " + this._wingSurface);
		System.out.println("Span = " + this._wingSpan);
		System.out.println("Adimensional kink station = " + this._wingAdimentionalKinkStation);
		System.out.println("y Adimensional Stations --> " + this._wingYAdimensionalBreakPoints);
		System.out.println("Mean airfoil type --> " + this._wingMeanAirfoilFamily);
		System.out.println("Mean airfoil thickness --> " + this._wingMaxThicknessMeanAirfoil);
		System.out.println("Distributions: " );
		System.out.println("Chord Break Points --> " + this._wingChordsBreakPoints);
		System.out.println("XLE Break Points --> " + this._wingXleBreakPoints);
		System.out.println("YLE Break Points --> " + this._wingYLEBreakPoints);
		System.out.println("Twist Break Points --> " + this._wingTwistBreakPoints);
		System.out.println("Max thickness Break Points--> " + this._wingMaxThicknessBreakPoints);
		System.out.println("Dihedral Break Points --> " + this._wingDihedralBreakPoints);
		System.out.println("Alpha zero lift Break Points --> " + this._wingAlphaZeroLiftBreakPoints);
		System.out.println("Cl zero Break Points --> " + this._wingCl0BreakPoints);
		System.out.println("Alpha star Break Points --> " + this._wingAlphaStarBreakPoints);
		System.out.println("Cl max Break Points --> " + this._wingClMaxBreakPoints);
		//distribution	
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingYDistribution, "Y Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingChordsDistribution, "Chord Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingXleDistribution, "XLE Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingYLEDistribution, "YLE Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingTwistDistribution, "Twist Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingDihedralDistribution, "Dihedral Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingAlphaStarDistribution, "Alpha star Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingAlphaZeroLiftDistribution, "\nAlpha zero lift Distribution", ",");
		System.out.println("\nCl alpha distribution (1/deg) " + this._wingClAlphaDistributionDeg);
		System.out.println("\nMax thickness Distribution --> " + this._wingMaxThicknessDistribution);
		System.out.println("\nCl max Distribution --> " + this._wingClMaxDistribution);
		if(_wingDragMethod == MethodEnum.AIRFOIL_INPUT || _wingDragMethod == MethodEnum.PARASITE_AIRFOIL_INPUT){
			int sec;
			for (int i = 0; i< this._wingNumberOfGivenSections; i++){
				sec =i+1;
				System.out.println("\tcl polar section " + sec + " =  -->" + clPolarAirfoilWingDragPolar.get(i));
				System.out.println("\tcd polar section " + sec + " =  -->" + cDPolarAirfoilsWing.get(i) );
			}
		}
		if(_wingDragMethod == MethodEnum.INPUT){
			System.out.println("\t\tCL polar =  -->" + Arrays.toString(_wingliftCoefficient3DCurveCONDITION) + "\n");
			System.out.println("\t\tCD polar section =  -->" + _wingDragCoefficient3DCurve + "\n");
		}


		if (this._theCondition == ConditionEnum.TAKE_OFF || this._theCondition == ConditionEnum.LANDING) { 
			System.out.println("\nHIGH LIFT DEVICES ----------\n-------------------");
			System.out.println("Number of flaps = " + this._wingNumberOfFlaps);
			System.out.println("Flaps type --> " + this._wingFlapType);
			System.out.println("Eta in Flaps --> " + this._wingEtaInFlap);
			System.out.println("Eta out Flaps --> " + this._wingEtaOutFlap);
			System.out.println("Flap chord ratio --> " + this._wingFlapCfC);
			System.out.println("Flap deflection --> " + this._wingDeltaFlap);
			System.out.println("\nNumber of Slats --> " + this._wingSlatCsC.size());
			System.out.println("Slat deflection --> " + this._wingDeltaSlat);
			System.out.println("Eta in Slats --> " + this._wingEtaInSlat);
			System.out.println("Eta out SLats --> " + this._wingEtaOutSlat);
		}

		System.out.println("\nFUSELAGE----------\n-------------------");
		System.out.println("Fuselage diameter = " + this._fuselageDiameter);
		System.out.println("Fuselage length = " + this._fuselageLength);
		System.out.println("Nose finess ratio = " + this._fuselageNoseFinessRatio);
		System.out.println("Tail finess ratio = " + this._fuselageTailFinessRatio);
		System.out.println("Windshield angle = " + this._fuselageWindshieldAngle);
		System.out.println("Upsweep angle = " + this._fuselageUpSweepAngle);
		System.out.println("x percent position pole = " + this._fuselageXPercentPositionPole);
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this. alphasFuselagePolar, "alphas Body input for fuselage polar", ",");
		System.out.println("\nFuselage cd polar = " + this.cdDistributionFuselage);	
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._alphasBody, "Alphas Body array", ",");
		System.out.println("\nFuselage cd polar = " + this.cdDistributionFuselageFinal);	
		


		System.out.println("\nHORIZONTAL TAIL ----------\n-------------------");
		System.out.println("X apex = " + this._xApexHTail);
		System.out.println("Y apex = " + this._yApexHTail);
		System.out.println("Z apex = " + this._zApexHTail);
		System.out.println("Number of points along semispan for analysis = " + this._hTailNumberOfPointSemiSpanWise);
		System.out.println("Surface = " + this._hTailSurface);
		System.out.println("Span = " + this._hTailSpan);
		System.out.println("y Adimensional Stations =" + this._hTailYAdimensionalBreakPoints);
		System.out.println("Mean airfoil type --> " + this._hTailMeanAirfoilFamily);
		System.out.println("Mean airfoil thickness --> " + this._hTailMaxThicknessMeanAirfoil);
		System.out.println("Distributions: " );
		System.out.println("Chord Break Points --> " + this._hTailChordsBreakPoints);
		System.out.println("XLE Break Points --> " + this._hTailXleBreakPoints);
		System.out.println("Twist Break Points --> " + this._hTailTwistBreakPoints);
		System.out.println("Dihedral Break Points --> " + this._hTailDihedralBreakPoints);
		System.out.println("Alpha zero lift Break Points --> " + this._hTailAlphaZeroLiftBreakPoints);
		System.out.println("Alpha star Break Points --> " + this._hTailAlphaStarBreakPoints);
		System.out.println("Cl max Break Points --> " + this._hTailClMaxBreakPoints);
		if(_hTailairfoilLiftCoefficientCurve == MethodEnum.INPUT){
			for (int i = 0; i< this._hTailnumberOfGivenSections; i++){
				int sec = i+1;
			MyArrayUtils.printListOfAmountWithUnitsInEvidence(this.alphaAirfoilsHTail.get(i), "Alpha airfoil number " + sec +" --> ", ",");
			System.out.println("Cl airfoil number  " + sec +" --> " + this.clDistributionAirfoilsHTail);
		}
			 }

		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._hTailChordsDistribution, "Chord Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._hTailXleDistribution, "XLE Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._hTailTwistDistribution, "Twist Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._hTailDihedralDistribution, "Dihedral Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._hTailAlphaStarDistribution, "Alpha star Distribution", ",");
		System.out.println("\nCl max Distribution --> " + this._hTailClMaxDistribution);

		System.out.println("\nELEVATOR ----------\n-------------------");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._anglesOfElevatorDeflection,"Angles of Elevator Deflection ", ",");
		System.out.println("\nFlaps type --> " + this._elevatorType);
		System.out.println("Eta in Elevator = " + this._elevatorEtaIn);
		System.out.println("Eta out Elevator = " + this._elevatorEtaOut);
		System.out.println("Elevator chord ratio = " + this._elevatorCfC);

		System.out.println("\nEngines ----------\n-------------------");

		System.out.println("\nPLOT LIST ----------\n-------------------");
		for (int i=0; i<this._plotList.size(); i++)
			System.out.println( this._plotList.get(i));
	}


	public String printAllResults(){


		MyConfiguration.customizeAmountOutput();


		StringBuilder sb = new StringBuilder()
				.append("\n\n---------------------------------------\n")
				.append("Longitudinal Static Stability Results\n")
				.append("---------------------------------------\n")
				;

		sb.append("LIFT\n")
		.append("-------------------------------------\n")
		.append("\tWing\n")
		.append("\t-------------------------------------\n")
		.append("\t\talpha zero lift = " + _wingAlphaZeroLift+ "\n" )
		.append("\t\tCL zero = " + _wingcLZero+ "\n")
		.append("\t\tCL alpha = " + _wingclAlpha+ "\n")
		.append("\t\tCL alpha M=0 = " + cLAlphaMachZero+ "\n")
		.append("\t\tCL star = " + _wingcLStar+ "\n")
		.append("\t\tAlpha star = " + _wingalphaStar+ "\n")
		.append("\t\tCL max = " + _wingcLMax+ "\n")
		.append("\t\tAlpha stall = " + _wingalphaStall+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
		.append("\t\tCL 3D Curve = " + Arrays.toString(_wingliftCoefficient3DCurve)+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
		.append("\t\tCL 3D Curve Modified = " + Arrays.toString(_wingLiftCoefficientModified)+ "\n")
		.append("\t\tEta stations = " + _wingYAdimensionalDistribution+ "\n")
		.append("\t\tCl distribution at CL max ( alpha = " + _wingalphaMaxLinear + " ) = " + Arrays.toString(_wingliftCoefficientDistributionatCLMax) + "\n")
		;

		if(_theCondition == ConditionEnum.TAKE_OFF || _theCondition== ConditionEnum.LANDING){
			sb.append("HIGH LIFT\n")
			.append("-------------------------------------\n")
			.append("\tWing\n")
			.append("\t-------------------------------------\n")
			.append("\t\talpha zero lift high lift = " + _alphaZeroLiftHighLift+ "\n" )
			.append("\t\tCL zero high lift = " + _cLZeroHighLift+ "\n")
			.append("\t\tCL alpha high lift = " + _cLAlphaHighLiftDEG+ "\n")
			.append("\t\tCL star high lift = " + _cLStarHighLift+ "\n")
			.append("\t\tAlpha star high lift = " + _alphaStarHighLift+ "\n")
			.append("\t\tCL max high lift = " + _cLMaxHighLift+ "\n")
			.append("\t\tAlpha stall high lift = " +_alphaStallHighLift+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
			.append("\t\tCL 3D Curve high lift = " + Arrays.toString(_wingLiftCoefficient3DCurveHighLift)+ "\n")
			;
		}

		if(_downwashConstant == Boolean.TRUE){
			sb.append("DOWNWASH\n")
			.append("-------------------------------------\n")
			.append("\t\tDownwash Gradient Constant Roskam = " + _downwashGradientConstantRoskam.get(0)+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantRoskam, "\t\tDownwash angle with Constant Gradient Roskam", ","))
			.append("\t\tDownwash Gradient Constant Slingerland = " + _downwashGradientConstantSlingerland.get(0)+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantSlingerland, "\t\tDownwash angle with Constant Gradient Slingerland", ","))
			.append("\t-------------------------------------\n")
			;
		}

		if(_downwashConstant == Boolean.FALSE){
			sb.append("DOWNWASH\n")
			.append("-------------------------------------\n")
			.append("\t\tDownwash Gradient Constant Roskam = " + _downwashGradientConstantRoskam.get(0)+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantRoskam, "\t\tDownwash angle with Constant Gradient Roskam", ","))
			.append("\t\tDownwash Gradient Constant Slingerland = " + _downwashGradientConstantSlingerland.get(0)+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantSlingerland, "\t\tDownwash angle with Constant Gradient Slingerland", ","))
			.append("\t\tDownwash Gradient Variable Slingerland = " + _downwashGradientVariableSlingerland+ "\n")
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantSlingerland, "\t\tDownwash angle with Constant Gradient Slingerland", ","))
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleVariableSlingerland, "\t\tDownwash angle with Variable Gradient Slingerland", ","))
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._horizontalDistance, "\t\thorizontal distance Variable Slingerland", ","))
			.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._verticalDistance, "\t\tVertical distance with Variable Gradient Slingerland", ","))
			;
		}


		sb.append("\t-------------------------------------\n")
		.append("\tFuselage\n")
		.append("\t-------------------------------------\n")
		.append("\t\talpha zero lift = " + _wingAlphaZeroLift+ "\n" )
		.append("\t\tCL zero = " + _fuselageWingClZero+ "\n")
		.append("\t\tCL alpha = " + _fuselageWingClAlphaDeg+ "\n")
		.append("\t\tAlpha star = " + _fuselageWingAlphaStar+ "\n")
		.append("\t\tCL max = " + _fuselageWingClMax+ "\n")
		.append("\t\tAlpha stall = " + _fuselageWingAlphaStall+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCL 3D Curve = " + Arrays.toString(_fuselagewingliftCoefficient3DCurve)+ "\n")
		;

		sb.append("\t-------------------------------------\n")
		.append("\tHorizontal Tail\n")
		.append("\t-------------------------------------\n")
		.append("\t\talpha zero lift = " + _hTailAlphaZeroLift+ "\n" )
		.append("\t\tCL zero = " + _hTailcLZero+ "\n")
		.append("\t\tCL alpha = " + _hTailclAlpha+ "\n")
		.append("\t\tCL star = " +_hTailcLStar+ "\n")
		.append("\t\tAlpha star = " + _hTailalphaStar+ "\n")
		.append("\t\tCL max = " + _hTailcLMax+ "\n")
		.append("\t\tAlpha stall = " + _hTailalphaStall+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasTail, "\t\tAlpha Tail", ","))
		.append("\t\tCL 3D Curve = " + Arrays.toString(_hTailliftCoefficient3DCurve)+ "\n")
		.append("\t\tEta stations = " + _hTailYAdimensionalDistribution+ "\n")
		.append("\t\tCl distribution at CL max ( alpha = " + _hTailalphaMaxLinear + " ) = " + Arrays.toString(_hTailliftCoefficientDistributionatCLMax) + "\n")
		.append("\t\tDynamic pressure Ratio = " + _dynamicPressureRatio+ "\n")
		;

		sb.append("\t-------------------------------------\n")
		.append("\tElevator\n")
		.append("\t-------------------------------------\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasTail, "\t\tAlpha Tail", ","))
		;	
		for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
			sb.append("\t\tCL at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + Arrays.toString(this._hTailLiftCoefficient3DCurveWithElevator.get( _anglesOfElevatorDeflection.get(i))))
			.append("\n")
			;
		}
		sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._deltaEAnglesArray, "\t\tdelta e elevator ", ","));
		sb.append("\t\ttau index --> [ ");
		for (int ii = 0; ii< this._deltaEAnglesArray.size()-1; ii++){
			sb.append( this._tauElevatorArray.get( _deltaEAnglesArray.get(ii)) + " , ")
			;
		}
		sb.append( this._tauElevatorArray.get( _deltaEAnglesArray.get(this._deltaEAnglesArray.size()-1)) + " ] " );


		sb.append("\nDRAG\n")
		.append("-------------------------------------\n")
		.append("\tWing\n")
		.append("\t-------------------------------------\n")
		;
		if(_wingDragMethod == MethodEnum.AIRFOIL_INPUT || _wingDragMethod == MethodEnum.PARASITE_AIRFOIL_INPUT){
			sb.append("\t\tCL Wing = " + Arrays.toString(_wingliftCoefficient3DCurveCONDITION) + "\n")
			.append("\t\tCD Parasite = " + _wingParasiteDragCoefficientDistribution+ "\n")
			.append("\t\tCD Induced = " + _wingInducedDragCoefficientDistribution+ "\n")
			.append("\t\tCD Total = " + _wingDragCoefficient3DCurve+ "\n")
			.append("\t\tEta stations = " + _wingYAdimensionalDistribution+ "\n")
			;
		}
		
		if(_wingDragMethod == MethodEnum.INPUT){
			sb.append("\t\tCL polar =  -->" + Arrays.toString(_wingliftCoefficient3DCurveCONDITION))
			.append("\n")
			.append("\t\tCD polar section =  -->" + _wingDragCoefficient3DCurve)
			.append("\n")
			;
		}

		sb.append("\t-------------------------------------\n")
		.append("\tHorizontal Tail\n")
		.append("\t-------------------------------------\n")
		;
		if(_hTailDragMethod == MethodEnum.AIRFOIL_INPUT || _hTailDragMethod == MethodEnum.PARASITE_AIRFOIL_INPUT){
			sb.append("\t\tCL Htail = " + Arrays.toString(_hTailliftCoefficient3DCurve) + "\n")
			.append("\t\tCD Parasite = " + _hTailParasiteDragCoefficientDistribution+ "\n")
			.append("\t\tCD Induced = " + _hTailInducedDragCoefficientDistribution+ "\n")
			.append("\t\tCD Total = " + _hTailDragCoefficient3DCurve+ "\n")
			.append("\t\tEta stations = " + _hTailYAdimensionalDistribution+ "\n");
			sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasTail, "\t\tAlpha Tail", ","));
			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
							sb.append("\n\t\tCD Htail at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" 
					+ Arrays.toString(this._hTailDragCoefficient3DCurveWithElevator.get( _anglesOfElevatorDeflection.get(i))));
			}
		}
		if(_hTailDragMethod == MethodEnum.INPUT){
			sb.append("\t\tCL polar =  -->" + Arrays.toString(_hTailliftCoefficient3DCurveCONDITION))
			.append("\n")
			.append("\t\tCD polar section =  -->" + _hTailDragCoefficient3DCurve)
			.append("\n")
			;
		}
		
		sb.append("\nMOMENT\n")
		.append("-------------------------------------\n")
		.append("\tWing\n")
		.append("\t-------------------------------------\n")
		.append("\t\tMAC = " +_wingMAC+ "\n")
		.append("\t\tx MAC = " +_wingMeanAerodynamicChordLeadingEdgeX + "\n")
		.append("\t\ty MAC = " +_wingYACMAC + "\n")
		.append("\t\tz MAC = " +_wingZACMAC + "\n")
		.append("\t\tXAC MAC = " +_wingXACMAC+ "\n")
		.append("\t\tXAC MAC percent = " +_wingXACMACpercent+ "\n")
		.append("\t\tXAC LRF = " +_wingXACLRF+ "\n")
		.append("\t\tXAC BRF = " +_wingXACBRF+ "\n")
		.append("\t\tDelta ac due to fuselage = " + _deltaXACdueToFuselage + "\n")
		.append("\t\tXAC wing body BRF = " +_wingBodyXACBRF+ "\n")
		.append("\t\teta Stations = " +_wingYAdimensionalDistribution+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
		.append("\t\tMoment Coefficient with respect to AC DE Young Harper --> " +_wingMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)+ "\n")
		.append("\t\tMoment Coefficient with respect to AC NAPOLITANO DATCOM --> " +_wingMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)+ "\n")
		;
		for (int i=0; i<_wingMomentumPole.size(); i++){
			sb.append("\t\tMoment Coefficient with respect to " + _wingMomentumPole.get(i) + "--> " +_wingMomentCoefficients.get(i)+ "\n");
		}
		;
		sb.append("\n\t\tMoment Coefficient FINAL (used in stability equation) with respect to C/4  --> " +_wingMomentCoefficientFinal+ "\n")
		;
		
		double xac = _wingFinalMomentumPole + _deltaXACdueToFuselage;
		
		sb.append("\t-------------------------------------\n")
		.append("\tHorizontal Tail\n")
		.append("\t-------------------------------------\n")
		.append("\t\tMAC = " +_hTailMAC+ "\n")
		.append("\t\tx MAC = " +_hTailMeanAerodynamicChordLeadingEdgeX+ "\n")
		.append("\t\tXAC MAC = " +_hTailXACMAC+ "\n")
		.append("\t\tXAC MAC percent = " +_hTailXACMACpercent+ "\n")
		.append("\t\tXAC LRF = " +_hTailXACLRF+ "\n")
		.append("\t\tXAC BRF = " +_hTailXACBRF+ "\n")
//		.append("\t\teta Stations = " +_hTailYAdimensionalDistribution+ "\n")
//		.append("\t\tMoment Coefficient with respect to AC DE Young Harper --> " +_hTailMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)+ "\n")
//		.append("\t\tMoment Coefficient with respect to AC NAPOLITANO DATCOM --> " +_hTailMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)+ "\n")
//		;
//		for (int i=0; i<_hTailMomentumPole.size(); i++){
//			sb.append("\t\tMoment with Coefficient respect to " + _hTailMomentumPole.get(i) + "--> " +_hTailMomentCoefficients.get(i)+ "\n");
//		}
//		;
		;
		
		sb.append("\t-------------------------------------\n")
		.append("\tFuselage\n")
		.append("\t-------------------------------------\n")
		.append("\t\tCM0 = " +_fuselageCM0+ "\n")
		.append("\t\tCM alpha = " +_fuselageCMAlpha+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCM fuselage = " +_fuselageMomentCoefficient+ "\n")
		.append("\t\tCM fuselage due to drag = " +_fuselageMomentCoefficientdueToDrag+ "\n")
		;
		
		sb.append("\t-------------------------------------\n")
		.append("\tGlobal\n")
		.append("\t-------------------------------------\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasBody, "\t\tAlpha Body", ","))
		;	
		for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
			sb.append("\t\tCL total at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (this._totalLiftCoefficient.get( _anglesOfElevatorDeflection.get(i))))
			.append("\n")
			;
		}
		
		sb.append("\t\tCL tail Eq. = " +_hTailEquilibriumLiftCoefficient+ "\n");
		sb.append("\t\tCL Eq. = " +_totalEquilibriumLiftCoefficient+ "\n");
		sb.append("\t\tCD Eq. = " +_totalTrimDrag+ "\n")
	
;
	

		sb.append("\nCOMPONENTS MOMENT COEFFICIENT REAPECT TO CG \n")
		.append("-------------------------------------\n")
		.append("\t\tX cg = " + _xCGAircraft + " Y cg = " + _yCGAircraft + " Z cg = " + _zCGAircraft + "\n\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCM wing no pendular stability = " + _wingMomentCoefficientNOPendular + "\n")
		.append("\t\tCM wing with pendular stability = " + _wingMomentCoefficientPendular + "\n")
		.append("\t\tCM Horizontal tail = " + _hTailMomentCoefficientPendular + "\n")
	    .append("\t\tCM Fuselage = " + _fuselageMomentCoefficient + "\n")
	    .append("\t\tCM Fuselage due to drag = " + _fuselageMomentCoefficientdueToDrag + "\n")
	    .append("\t\tCM Total delta e = 0 = " + _totalMomentCoefficientPendular + "\n")
	    .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\n\t\tAlpha Body", ","))
		;
		for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
			sb.append("\t\tCM total at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (this._totalMomentCoefficientPendularDeltaE.get( _anglesOfElevatorDeflection.get(i))))
			.append("\n")
			;
		}
		sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._deltaEEquilibrium, "\t\tdelta E Eq. = ", ","));
//-------------------------------		
		sb.append("\nCOMPONENTS MOMENT COEFFICIENT REAPECT TO CG \n")
		.append("-------------------------------------\n")
		.append("\t\tX cg = " + _xCGAircraft + " Y cg = " + _yCGAircraft + " Z cg = " + _zCGAircraft + "\n\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasBody, "", ","))
		.append( _wingMomentCoefficientNOPendular + "\n")
		.append(_wingMomentCoefficientPendular + "\n")
		.append(_hTailMomentCoefficientPendular + "\n")
	    .append( _fuselageMomentCoefficient + "\n")
	    .append( _totalMomentCoefficientPendular + "\n")
	    .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "", ","))
		;
		for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
			sb.append( (this._totalMomentCoefficientPendularDeltaE.get( _anglesOfElevatorDeflection.get(i))))
			.append("\n")
			;
		}
		
//-----------------------------------	
		
				sb.append("\nPOLAR\n")
				.append("-------------------------------------\n")
				.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
				.append("\t\tWing drag Polar = " + _wingDragCoefficient3DCurve + "\n")
				.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasTail, "\t\tAlpha Tail", ","));
				for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
					sb.append("\t\tTail drag polar at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (Arrays.toString(this._hTailDragCoefficient3DCurveWithElevator.get( _anglesOfElevatorDeflection.get(i)))))
				.append("\n");
				};
				sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","));
				sb.append("\t\tFuselage drag = " + cdDistributionFuselageFinal + "\n");
				if(_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING)
				sb.append("\t\tLanding gear drag = " + _cDLandingGear + "\n");
				sb.append("\t\tMiscellaneous drag = " + _deltaCD0Miscellaneus + "\n");
				sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","));
				for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
					sb.append("\t\tCL total at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (this._totalLiftCoefficient.get( _anglesOfElevatorDeflection.get(i))))
					.append("\n");
					sb.append("\t\tCD total at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (this._totalDragPolar.get( _anglesOfElevatorDeflection.get(i))))
					.append("\n")
					;
				}
					for (int i = 0; i< this._anglesOfElevatorDeflection.size(); i++){
						sb.append("\t\tCD total at delta_e= " + _anglesOfElevatorDeflection.get(i) + " -->" + (this._totalDragPolar.get( _anglesOfElevatorDeflection.get(i))))
						.append("\n")
						;
				}

//-----------------------------------	
			
		sb.append("\nDISTRIBUTIONS\n")
		.append("-------------------------------------\n")
		.append("\t\teta wing = " + _wingYAdimensionalDistribution + "\n\n")
		.append("\t\teta horizontal tail = " + _hTailYAdimensionalDistribution + "\n")
		;
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			sb.append("\t\tCl wing at alpha = " + _alphaWingForDistribution.get(i) + " --> " +_clWingDistribution.get(i)+ "\n");
		}
		;
		for (int i=0; i<_alphaHorizontalTailForDistribution.size(); i++){
			sb.append("\t\tCl horizontal tail at alpha = " + _alphaHorizontalTailForDistribution.get(i) + " --> " +_clHtailDistribution.get(i)+ "\n");
		}
		sb.append("\n");
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			sb.append("\t\tCm wing at alpha = " + _alphaWingForDistribution.get(i) + " --> " +_cMWingDistribution.get(i)+ "\n");
		}
		;		
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphaIWingDistribution.get(i), "\t\tAlpha_i wing at alpha = " +  _alphaWingForDistribution.get(i)  + " deg --> " , ","));
		}
		;
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			sb.append("\t\tXcp wing at alpha = " + _alphaWingForDistribution.get(i) + " --> " +_centerOfPressureWingDistribution.get(i)+ "\n");
		}
		;
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			sb.append("\t\tCl distribution Modified = " + _alphaWingForDistribution.get(i) + " --> " +Arrays.toString(_clNasaBlackwellDistributionModified.get(i))+ "\n");
		}
		;
		sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasTail, "\n\t\tAlpha Body", ","))
		.append("\t\tcl wing = " + _hTailEquilibriumLiftCoefficient + "\n\n");
		
//  	//DISTANCES ----------------------------------------------------------
		sb.append("\nDISTANCES\n")
		.append("-------------------------------------\n")
		.append("\t\tWing horizontal distance = " + _wingHorizontalDistanceACtoCG + "\n")
		.append("\t\tWing vertical distance = " + _wingVerticalDistranceACtoCG + "\n")
		.append("\t\tHorizontal tail horizontal distance = " + _hTailHorizontalDistanceACtoCG + "\n")
		.append("\t\tHorizontal tail vertical distance = " + _hTailVerticalDistranceACtoCG + "\n")
		.append("\t\tFuselage vertical distance = (-) " + _zCGAircraft + "\n")
		.append("\t\tLanding gear arm = " + _landingGearArm + "\n");
		
//		// CL FLAPPED CURVE
//		
//		if(_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
//		sb.append("\nFLAPPED 3D CURVE\n")
//		.append("-------------------------------------\n")
//		.append("\t\talpha zero lift = " + _alphaZeroLiftFlapped + "\n" )
//		.append("\t\tCL zero = " + _clZeroFlapped+ "\n")
//		.append("\t\tCL alpha = " + _clAlphaDegFlapped+ "\n")
//		.append("\t\tAlpha star = " + _alphaStarFlapped+ "\n")
//		.append("\t\tCL max = " + _clMaxFlapped+ "\n")
//		.append("\t\tAlpha stall = " + _alphaStallFlapped+ "\n")
//		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
//		.append("\t\tCL 3D Curve = " + Arrays.toString(_cl3DCurveWingFlapped)+ "\n")
//		.append("\t\teta wing = " + _wingYAdimensionalDistribution + "\n\n")
//		.append("\t\tCL max distribution at alpha " + _alphaStallLinearFlapped + " = " + _clMaxDistributionFlapped + "\n\n")
//		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphaWingForDistribution, "\t\tAlpha Wing", ","))
//		.append("\t\tCL 3D Curve  = " + Arrays.toString(cl3D) + "\n\n");
//		for (int i=0; i<_alphaWingForDistribution.size(); i++){
//			sb.append("\t\tCl distribution  at alpha " + _alphaWingForDistribution.get(i) +  " = " + clDistributions.get(i) + "\n\n");
//		}
//		}
		
		//DRAG ----------------------------------------------------------
		sb.append("\nDRAG BREAKDOWN\n")
	   .append("-------------------------------------\n")
	   .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasBody, "\t\tAlpha Body", ","))
	   .append("\t\tCD Parasite = " + _wingParasiteDragCoefficientDistribution+ "\n")
	   .append("\t\tCD Induced = " + _wingInducedDragCoefficientDistribution+ "\n")
	   .append("\t\tCD Total = " + _wingDragCoefficient3DCurve+ "\n")
	   .append("\t\tFuselage drag = " + cdDistributionFuselageFinal + "\n")
		.append("\t\tCD Parasite TAIL  = " + _hTailParasiteDragCoefficientDistribution+ "\n")
		.append("\t\tCD Induced TAIL= " + _hTailInducedDragCoefficientDistribution+ "\n")
		.append("\t\tCD Total TAIL = " + _hTailDragCoefficient3DCurve+ "\n")
		.append("\t\tLanding gear drag = " + _cDLandingGear + "\n")
		.append("\t\tMiscellaneous drag = " + _deltaCD0Miscellaneus + "\n")
		.append("\t\tCD total at delta_e= 0  -->" + (this._totalDragPolar.get(0.0)))
		 .append("-------------------------------------\n")
		 .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstantSlingerland, "\t\tDownwash angle constant new", ","))
		 .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleVariableSlingerland, "\t\tDownwash angle variable new", ","));
		// .append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleVariableSlingerlandOld, "\t\tDownwash angle variable old", ","));
		return sb.toString();
	}

	public void plot( String folderPathName, String folderPathNameDistribution) throws InstantiationException, IllegalAccessException{

		// DOWNWASH e DOWNWASH GRADIENT	
		//------------------------------------------------------------------------------------------------------------
		if ( this._downwashConstant == Boolean.TRUE){
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_ANGLE)) {

				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleConstantRoskam));
				legend.add("null");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Downwash Angle", 
						"alpha_b", "epsilon", 
						null, null,
						null, null,
						"deg", "deg",
						false,
						legend,
						folderPathName,
						"Downwash Angle", true);

				System.out.println("Plot Downwash Angle Chart ---> DONE \n");
			}


			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_GRADIENT)) {

				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientConstantRoskam));
				legend.add("null");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Downwash Gradient", 
						"alpha_b", "d epsilon / d alpha", 
						null, null,
						null, null,
						"deg", "",
						false,
						legend,
						folderPathName,
						"Downwash Gradient", true);

				System.out.println("Plot Downwash Gradient Chart ---> DONE \n");

			}
		}

		if ( _downwashConstant == Boolean.FALSE){
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_ANGLE)) {
				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleConstantSlingerland));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleVariableSlingerland));
				legend.add("Downwash gradient constant");
				legend.add("Downwash gradient variable");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Downwash Angle", 
						"alpha_b", "epsilon", 
						null, null,
						null, null,
						"deg", "deg",
						true,
						legend,
						folderPathName,
						"Downwash Angle", true);

				System.out.println("Plot Downwash Angle Chart ---> DONE \n");
			}


			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_GRADIENT)) {
				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientConstantSlingerland));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientVariableSlingerland));
				legend.add("Downwash gradient constant");
				legend.add("Downwash gradient variable");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Downwash Gradient", 
						"alpha_b", "d epsilon / d alpha", 
						null, null,
						null, null,
						"deg", "",
						true,
						legend,
						folderPathName,
						"Downwash Gradient", true);

				System.out.println("Plot Downwash Gradient Chart ---> DONE \n");

				xList = new ArrayList<>();
				yList = new ArrayList<>();
				legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_horizontalDistanceConstant));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_horizontalDistance));
				legend.add("Downwash gradient constant");
				legend.add("Downwash gradient variable");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Horizontal Distance", 
						"alpha_b", "x dist", 
						null, null,
						null, null,
						"deg", "m",
						true,
						legend,
						folderPathName,
						"Horizontal Distance", true);

				System.out.println("Plot Horizontal Distance Chart ---> DONE \n");

				xList = new ArrayList<>();
				yList = new ArrayList<>();
				legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_verticalDistanceConstant));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_verticalDistance));
				legend.add("Downwash gradient constant");
				legend.add("Downwash gradient variable");

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Vertical Distance", 
						"alpha_b", "z dist", 
						null, null,
						null, null,
						"deg", "m",
						true,
						legend,
						folderPathName,
						"Vertical Distance", true);

				System.out.println("Plot Vertical Distance Chart ---> DONE \n");
			}
		}

		// CL ALPHA WING
		//------------------------------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_CL_CURVE_CLEAN)) {


			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
			yList.add(_wingliftCoefficient3DCurve);
			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Lift Coefficient 3D curve clean", 
					"alpha_w", "CL", 
					null, null,
					null, null,
					"deg", "",
					false,
					legend,
					folderPathName,
					"Wing Lift Coefficient 3D curve clean", true);

			System.out.println("Plot CL clean Chart ---> DONE \n");



			// CL DISTRIBUTION AT CL MAX
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			Double[] _wingliftCLMaxDistribution = new Double [_wingNumberOfPointSemiSpanWise];
			for (int i=0; i< _wingNumberOfPointSemiSpanWise ; i++){
				_wingliftCLMaxDistribution[i] = _wingliftCoefficientDistributionatCLMax[i];
			}
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
			yList.add(_wingliftCLMaxDistribution);
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingClMaxDistribution));
			legend.add("Cl distribution at CL max");
			legend.add("Cl max airfoils");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Cl distribution at CL max Clean", 
					"eta", "Cl", 
					null, 1.0,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Wing Cl distribution at CL max Clean", true);

			System.out.println("Plot Cl distribution at CL max ---> DONE \n");

			// HIGH LIFT
			if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
				xList = new ArrayList<>();
				yList = new ArrayList<>();
				legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
				yList.add(_wingliftCoefficient3DCurve);
				yList.add(_wingLiftCoefficient3DCurveHighLift);
				legend.add("Clean");
				legend.add("High lift " + _theCondition);

				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Wing Lift Coefficient 3D curve High lift", 
						"alpha_w", "CL", 
						null, null,
						null, null,
						"deg", "",
						true,
						legend,
						folderPathName,
						"Wing Lift Coefficient 3D curve High lift ", true);

				
			}
		}

		// CL ALPHA HORIZONTAL TAIL
		//------------------------------------------------------------------------------------------------------------

		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_CL_CURVE_CLEAN)) {


			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
			yList.add(_hTailliftCoefficient3DCurve);
			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Lift Coefficient 3D curve clean", 
					"alpha_t", "CL", 
					null, null,
					null, null,
					"deg", "",
					false,
					legend,
					folderPathName,
					"Horizontal Tail Lift Coefficient 3D curve clean", true);

			System.out.println("Plot H tail CL clean Chart ---> DONE \n");


			// CL DISTRIBUTION AT CL MAX
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			Double[] _hTailliftCLMaxDistribution = new Double [_hTailNumberOfPointSemiSpanWise];
			for (int i=0; i< _hTailNumberOfPointSemiSpanWise ; i++){
				_hTailliftCLMaxDistribution[i] = _hTailliftCoefficientDistributionatCLMax[i];
			}
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
			yList.add(MyArrayUtils.convertFromDoubleToPrimitive(_hTailliftCoefficientDistributionatCLMax));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailClMaxDistribution));
			legend.add("Cl distribution at CL max");
			legend.add("Cl max airfoils");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Cl distribution at CL max Clean", 
					"eta", "Cl", 
					null, 1.0,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail Cl distribution at CL max Clean", true);

			System.out.println("Plot Htail Cl distribution at CL max ---> DONE \n");
		}
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_CL_CURVE_ELEVATOR)) {
			// CL DISTRIBUTION AT CL MAX
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
				yList.add(_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i)));
				legend.add("delta e = " + _anglesOfElevatorDeflection.get(i));
			}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail CL vs alpha with elevator deflection", 
					"alpha_h", "CL", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail CL vs alpha with elevator deflection" ,true);

			System.out.println("Plot Htail CL vs alpha with elevator deflection---> DONE \n");
		}

		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_CL_CURVE_CLEAN)){
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(_wingliftCoefficient3DCurve);
			yList.add(_fuselagewingliftCoefficient3DCurve);

			legend.add("wing");
			legend.add("wing fuselage");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Fuselage Lift Coefficient 3D curve clean", 
					"alpha_b", "CL", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Wing Fuselage Lift Coefficient 3D curve clean", true);

			System.out.println("Plot CL wing fuselage clean Chart ---> DONE \n");
		}
		
		// CD
		//------------------------------------------------------------------------------------------------------------
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE)){
			if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT || this._wingDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			yList.add(_wingliftCoefficient3DCurveCONDITION);
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingParasiteDragCoefficientDistribution));


			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Parasite drag Coefficient", 
					"CDp", "CL", 
					null, null,
					null, null,
					"", "",
					false,
					legend,
					folderPathName,
					"Wing Parasite drag Coefficient", true);

			System.out.println("Plot wing Parasite drag Coefficient chart ---> DONE \n");
			
			//--induced----------------------------------------------------------------------------------------
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingInducedDragCoefficientDistribution));
			yList.add(_wingliftCoefficient3DCurveCONDITION);


			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Induced drag Coefficient", 
					"CDi", "CL", 
					null, null,
					null, null,
					"", "",
					false,
					legend,
					folderPathName,
					"Wing Induced drag Coefficient", true);

			System.out.println("Plot wing Induced drag Coefficient chart ---> DONE \n");
			
			//--Total----------------------------------------------------------------------------------------
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingInducedDragCoefficientDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingParasiteDragCoefficientDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingDragCoefficient3DCurve));
			// TODO add total
			yList.add(_wingliftCoefficient3DCurveCONDITION);
			yList.add(_wingliftCoefficient3DCurveCONDITION);
			yList.add(_wingliftCoefficient3DCurveCONDITION);

			legend.add("CDp");
			legend.add("Cdi");
			legend.add("Cdtot");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Total drag Coefficient", 
					"CDi", "CL", 
					null, null,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Wing Total drag Coefficient", true);

			System.out.println("Plot wing Total drag Coefficient chart ---> DONE \n");
			
			}
		}
		
		//htail--------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE)){
			if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT || this._hTailDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			yList.add(_hTailliftCoefficient3DCurve);
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailParasiteDragCoefficientDistribution));


			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Parasite drag Coefficient", 
					"CDp", "CL", 
					null, null,
					null, null,
					"", "",
					false,
					legend,
					folderPathName,
					"Horizontal Tail Parasite drag Coefficient",true);

			System.out.println("Plot Horizontal Tail Parasite drag Coefficient chart ---> DONE \n");
			
			//--induced----------------------------------------------------------------------------------------
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailInducedDragCoefficientDistribution));
			yList.add(_hTailliftCoefficient3DCurve);


			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Induced drag Coefficient", 
					"CDi", "CL", 
					null, null,
					null, null,
					"", "",
					false,
					legend,
					folderPathName,
					"Horizontal Tail Induced drag Coefficient", true);

			System.out.println("Plot Horizontal Tail Induced drag Coefficient chart ---> DONE \n");
			
			//--Total----------------------------------------------------------------------------------------
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailInducedDragCoefficientDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailParasiteDragCoefficientDistribution));
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailDragCoefficient3DCurve));
			// TODO add total
			yList.add(_hTailliftCoefficient3DCurve);
			yList.add(_hTailliftCoefficient3DCurve);
			yList.add(_hTailliftCoefficient3DCurve);

			legend.add("CDp");
			legend.add("Cdi");
			legend.add("Cdtot");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Total drag Coefficient", 
					"CDi", "CL", 
					null, null,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail Total drag Coefficient", true);

			System.out.println("Plot Horizontal Tail Total drag Coefficient chart ---> DONE \n");
			
			
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				//yList.add(_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i)));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
				xList.add(_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i)));
				legend.add("delta e = " + _anglesOfElevatorDeflection.get(i));
			}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail CD vs CL with elevator deflection", 
					"CD", "CL", 
					null, null,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail CD vs CL with elevator deflection", true);

			System.out.println("Plot Htail CD vs CL with elevator deflection---> DONE \n");
			
			
			}
		}

		// MOMENT
		//------------------------------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.FUSELAGE_CM_PLOT)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_fuselageMomentCoefficient));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_fuselageMomentCoefficientdueToDrag));
			legend.add("moment");
			legend.add("moment due to drag");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Fuselage Moment Coefficient", 
					"alpha_b", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Fuselage Moment Coefficient", true);

			System.out.println("Plot Fuselage Moment Coefficient Chart ---> DONE \n");
//			System.out.println("Plot CL high lift Chart ---> DONE \n");
//			System.out.println("xac wing " + _wingXACBreakPoints);
//			System.out.println("xac wing distribution " + _wingXACDistribution);
//			System.out.println("xac h tail " + _hTailXACBreakPoints);
//			System.out.println("xac h tail distribution " + _hTailXACDistribution);
//			System.out.println("cmac wing " + _wingCmACBreakPoints);
//			System.out.println("cmac wing distribution " + _wingCmACDistribution);
//			System.out.println("cmac h tail " + _hTailCmACBreakPoints);
//			System.out.println("cmac h tail distribution " + _hTailCmACDistribution);
//			System.out.println("poli wing " + _wingMomentumPole);
//			System.out.println("poli htail " + _hTailMomentumPole);
			
		}
		//WING---------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_CM_AERODYNAMIC_CENTER)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

//			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
//			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
//			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)));
//			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)));
//			legend.add("AC calculated by De Young Harper formula");
//			legend.add("AC calculated by Napoltano-Datcom formula");
//
//			MyChartToFileUtils.plot(
//					xList, 
//					yList, 
//					"Wing Moment Coefficient with respect to AC", 
//					"alpha_w", "CM", 
//					null, null,
//					null, null,
//					"deg", "",
//					true,
//					legend,
//					folderPathName,
//					"Wing Moment Coefficient with respect to AC");
//
//		System.out.println("Plot Wing Moment Coefficient Chart with respect to AC---> DONE \n");
//		
//		xList = new ArrayList<>();
//		yList = new ArrayList<>();
//		legend = new ArrayList<>();

		xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientFinal));
		legend.add("null");

		MyChartToFileUtils.plot(
				xList, 
				yList, 
				"Wing Moment Coefficient with respect to AC indicated = " + _wingFinalMomentumPole, 
				"alpha_w", "CM", 
				null, null,
				//_wingMomentCoefficientFinal.get(0)-0.25, _wingMomentCoefficientFinal.get(0)+0.1,
				null, null,
				"deg", "",
				false,
				legend,
				folderPathName,
				"Wing Moment Coefficient with respect to AC indicated = " + _wingFinalMomentumPole, true);

	System.out.println("Plot Wing Moment Coefficient Chart with respect to AC indicated---> DONE \n");
		
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_CM_QUARTER_CHORD)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_wingMomentumPole.size(); j++){
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficients.get(j)));
			legend.add("CM with with respect to "+ _wingMomentumPole.get(j) + " of MAC");}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Moment Coefficient", 
					"alpha_w", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Wing Moment Coefficient", true);
		
			System.out.println("Plot Wing Moment Coefficient Chart with respect to other poles---> DONE \n");
		}
		
		
		//HORIZONTAL TAIL---------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_CM_AERODYNAMIC_CENTER)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)));
			legend.add("AC calculated by De Young Harper formula");
			legend.add("AC calculated by Napoltano-Datcom formula");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Moment Coefficient with respect to AC", 
					"alpha_h", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail Moment Coefficient with respect to AC", true);

		System.out.println("Plot Horizontal Tail Moment Coefficient Chart with respect to AC---> DONE \n");
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_CM_QUARTER_CHORD)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_hTailMomentumPole.size(); j++){
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailMomentCoefficients.get(j)));
			legend.add("CM with respect to "+ _hTailMomentumPole.get(j) + " of MAC");}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Moment Coefficient", 
					"alpha_t", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail Moment Coefficient", true);
		
			System.out.println("Plot Horizontal Tail Moment Coefficient Chart with respect to other poles---> DONE \n");
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_CM_QUARTER_CHORD)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_hTailMomentumPole.size(); j++){
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailMomentCoefficients.get(j)));
			legend.add("CM with respect to "+ _hTailMomentumPole.get(j) + " of MAC");}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Moment Coefficient", 
					"alpha_t", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Horizontal Tail Moment Coefficient", true);
		
			System.out.println("Plot Horizontal Tail Moment Coefficient Chart with respect to other poles---> DONE \n");
		}
		// MOMENT
	   //------------------------------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CL_TOTAL)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalLiftCoefficient.get(_anglesOfElevatorDeflection.get(i))));
			legend.add("delta e =  "+ _anglesOfElevatorDeflection.get(i));
			}
			
			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Total Lift Coefficient", 
					"alpha_b", "CL", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Total Lift Coefficient", true);
		
			System.out.println("Plot Total Lift Coefficient Chart ---> DONE \n");
			
			// CL EQUILIBRIUM
			
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasTail));
				yList.add(_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i)));
				legend.add("delta e = " + _anglesOfElevatorDeflection.get(i));
			}
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailEquilibriumLiftCoefficient));
			legend.add("CL equilibrium");
			
			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal tail equilibrium lift coefficient", 
					"alpha_t", "CL ", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Horizontal tail equilibrium lift coefficient", true);
		
			System.out.println("Horizontal tail equilibrium lift coefficient ---> DONE \n");
			
			// total
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalLiftCoefficient.get(_anglesOfElevatorDeflection.get(i))));
				legend.add("delta e =  "+ _anglesOfElevatorDeflection.get(i));
				}
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalEquilibriumLiftCoefficient));
			legend.add("CL equilibrium");
			
			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Total equilibrium lift coefficient", 
					"alpha_b", "CL ", 
					null, null,
					null, null,
					"deg", "",
					false,
					legend,
					folderPathName,
					"Total equilibrium lift coefficient", true);
		
			System.out.println("Total equilibrium lift coefficient ---> DONE \n");
			
			// delta e equilibrium
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_deltaEEquilibrium));
				legend.add("null");

			
			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Equilibrium angle of deflection", 
					"alpha_b", "delta e e ", 
					null, null,
					null, null,
					"deg", "deg",
					false,
					legend,
					folderPathName,
					"Equilibrium angle of deflection", true);
		
			System.out.println("Total equilibrium angle deflection ---> DONE \n");
			
			
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_ALPHA_BODY_COMPONENTS)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientPendular));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientNOPendular));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_fuselageMomentCoefficient));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailMomentCoefficientPendular));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalMomentCoefficientPendular));
		
			legend.add("WING Whitout pendular stability");
			legend.add("WING Whit pendular stability");
			legend.add("Fuselage");
			legend.add("Horizontal Tail. delta e = 0 deg");
			legend.add("Total");
			
			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Components Moment coefficient with respect TO CG", 
					"alpha_b", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Components Moment coefficient with respect TO CG", true);
		
			System.out.println("Plot Components Moment coefficient with respect TO CG Chart ---> DONE \n");
		}
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_CL_DELTAE)) {

				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalMomentCoefficientPendularDeltaE.get(_anglesOfElevatorDeflection.get(i)))); 
				xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalLiftCoefficient.get(_anglesOfElevatorDeflection.get(i))));
				legend.add("delta e =  "+ _anglesOfElevatorDeflection.get(i));
				}
				
				MyChartToFileUtils.plot(
						xList, 
						yList, 
						"Total Moment Coefficient vs CL", 
						"CL", "CM", 
						null, null,
						null, null,
						"", "",
						true,
						legend,
						folderPathName,
						"Total Moment Coefficient vs CL",true);
			
				System.out.println("Plot Total Moment Coefficient Chart var delta e ---> DONE \n");
			}
				if(_plotList.contains(AerodynamicAndStabilityPlotEnum.AIRCRAFT_CM_VS_ALPHA_BODY)) {

					List<Double[]> xList = new ArrayList<>();
					List<Double[]> yList = new ArrayList<>();
					List<String> legend = new ArrayList<>();

					for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
					yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_totalMomentCoefficientPendularDeltaE.get(_anglesOfElevatorDeflection.get(i))));
					xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
					legend.add("delta e =  "+ _anglesOfElevatorDeflection.get(i));
					}
					
					MyChartToFileUtils.plot(
							xList, 
							yList, 
							"Total Moment Coefficient vs alpha", 
							"alpha_b", "CM", 
							null, null,
							null, null,
							"deg", "",
							true,
							legend,
							folderPathName,
							"Total Moment Coefficient vs alpha", true);
				
					System.out.println("Plot Total Lift Coefficient var delta e vs alpha Chart ---> DONE \n");
				}
			
		
		// DISTRIBUTION
		//------------------------------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CL_DISTRIBUTION_WING)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaWingForDistribution.size(); j++){
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_clWingDistribution.get(j)));
			legend.add("Cl distribution at alpha " + _alphaWingForDistribution.get(j) );}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Lift Coefficient Distribution", 
					"alpha_w", "Cl", 
					0., 1.,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathNameDistribution,
					"Wing Lift Coefficient Distribution", true);
		
			System.out.println("Plot Wing Lift Coefficient Distribution Chart ---> DONE \n");
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CL_DISTRIBUTION_HORIZONTAL_TAIL)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaHorizontalTailForDistribution.size(); j++){
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_clHtailDistribution.get(j)));
			legend.add("Cl distribution at alpha " + _alphaHorizontalTailForDistribution.get(j) );}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Lift Coefficient Distribution", 
					"alpha_h", "Cl", 
					0., 1.,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathNameDistribution,
					"Horizontal Tail Lift Coefficient Distribution", true);
		
			System.out.println("Plot Horizontal Tail Lift Coefficient Distribution Chart ---> DONE \n");
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CM_DISTRIBUTION_WING)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaWingForDistribution.size(); j++){
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_cMWingDistribution.get(j)));
			legend.add("Cm distribution at alpha " + _alphaWingForDistribution.get(j) );}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Moment Coefficient Distribution with respect to " + _wingFinalMomentumPole, 
					"alpha_w", "Cl", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathNameDistribution,
					"Wing Moment Coefficient Distribution with respect to " + _wingFinalMomentumPole, true);
		
			System.out.println("Plot Wing Moment Coefficient Distribution with respect to " + _wingFinalMomentumPole+ " Chart ---> DONE \n");
		}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CM_DISTRIBUTION_HORIZONTAL_TAIL)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaHorizontalTailForDistribution.size(); j++){
			xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_cMHTailDistribution.get(j)));
			legend.add("Cm distribution at alpha " + _alphaHorizontalTailForDistribution.get(j) );}

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Horizontal Tail Moment Coefficient Distribution with respect to " + _hTailFinalMomentumPole, 
					"alpha_t", "Cl", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathNameDistribution,
					"Horizontal Tail Moment Coefficient Distribution with respect to " + _hTailFinalMomentumPole, true);
		
			System.out.println("Plot Horizontal Tail Moment Coefficient Distribution with respect to " + _hTailFinalMomentumPole + " Chart ---> DONE \n");
		}
		
		
		//--induced angle of attack----------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.INDUCED_ALPHA_DISTRIBUTION_WING)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaWingForDistribution.size(); j++){
				xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaIWingDistribution.get(j)));
				legend.add("Induced angle of attack at alpha = " + _alphaWingForDistribution.get(j) );}

		MyChartToFileUtils.plot(
				xList, 
				yList, 
				"Wing Induced angle of attack distribution", 
				"eta", "alpha_i", 
				null, null,
				null, null,
				"", "deg",
				true,
				legend,
				folderPathNameDistribution,
				"Wing Induced angle of attack distribution", true);

		System.out.println("Plot wing Induced angle of attack distribution chart ---> DONE \n");}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.INDUCED_ALPHA_DISTRIBUTION_HORIZONTAL_TAIL)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaHorizontalTailForDistribution.size(); j++){
				xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphaIHtailDistribution.get(j)));
				legend.add("Induced angle of attack at alpha = " + _alphaHorizontalTailForDistribution.get(j) );}

		MyChartToFileUtils.plot(
				xList, 
				yList, 
				"Horizontal Tail Induced angle of attack distribution", 
				"eta", "alpha_i", 
				null, null,
				null, null,
				"", "deg",
				true,
				legend,
				folderPathNameDistribution,
				"Horizontal Tail Induced angle of attack distribution", true);

		System.out.println("Plot Horizontal Tail Induced angle of attack distribution chart ---> DONE \n");}
		
		//--center of pressure----------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CENTER_OF_PRESSURE_DISTRIBUTION_WING)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaWingForDistribution.size(); j++){
				xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingYAdimensionalDistribution));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_centerOfPressureWingDistribution.get(j)));
				legend.add("Center of pressure at alpha = " + _alphaWingForDistribution.get(j) );}

		MyChartToFileUtils.plot(
				xList, 
				yList, 
				"Wing center of pressure distribution", 
				"eta", "Xcp/c", 
				null, null,
				null, null,
				"", "",
				true,
				legend,
				folderPathNameDistribution,
				"Wing center of pressuredistribution", true);

		System.out.println("Plot wing center of pressure distribution chart ---> DONE \n");}
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.CENTER_OF_PRESSURE_DISTRIBUTION_HORIZONTAL_TAIL)) {
			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			for (int j=0; j<_alphaHorizontalTailForDistribution.size(); j++){
				xList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_hTailYAdimensionalDistribution));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_centerOfPressurehTailDistribution.get(j)));
				legend.add("Center of pressure at alpha = " + _alphaHorizontalTailForDistribution.get(j) );}

		MyChartToFileUtils.plot(
				xList, 
				yList, 
				"Horizontal Tail center of pressure distribution", 
				"eta", "cp", 
				null, null,
				null, null,
				"", "",
				true,
				legend,
				folderPathNameDistribution,
				"Horizontal Tail center of pressuredistribution", true);

		System.out.println("Plot Horizontal Tail center of pressure distribution chart ---> DONE \n");}
	}

	/******************************************************************************************************************************************
	 * Following there are the calculators                                                                                                    *
	 * 												*																						  *
	 *****************************************************************************************************************************************/

	//--------------------------------------------------------------------------------------------------------
	//CALCULATORS---------------------------------------													 |
	//--------------------------------------------------------------------------------------------------------

	public void calculateWingLiftCharacteristics(){
		if (_theCondition == ConditionEnum.CRUISE) {
		// cl alpha 
		theNasaBlackwellCalculatorMachActualWing.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachActual = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		theNasaBlackwellCalculatorMachActualWing.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachActual = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		this._wingcLAlphaRad = (clTwoMachActual-clOneMachActual)/toRadians(4);
		this._wingcLAlphaDeg = (clTwoMachActual-clOneMachActual)/(4);
		this._wingclAlpha = Amount.valueOf( this._wingcLAlphaRad , SI.RADIAN.inverse());

		// alpha zero lift
		this._wingAlphaZeroLift = (
				Amount.valueOf(
						AnglesCalc.alpha0LintegralMeanWithTwist(
								this._wingSurface.doubleValue(SI.SQUARE_METRE),
								this._wingSemiSpan.doubleValue(SI.METER), 
								MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this._wingAlphaZeroLiftDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this.getWingTwistDistribution())
								),
						NonSI.DEGREE_ANGLE
						));

		
		//cl zero
		this._wingcLZero = LiftCalc.calculateLiftCoefficientAtAlpha0(
				_wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE),
				this._wingcLAlphaDeg
				);

		// alphaStar e cl star
		double alphaStar =  _wingAlphaStarBreakPoints.get(0).doubleValue(NonSI.DEGREE_ANGLE) * kRoot +
				_wingAlphaStarBreakPoints.get(1).doubleValue(NonSI.DEGREE_ANGLE) * kKink + 
				_wingAlphaStarBreakPoints.get(2).doubleValue(NonSI.DEGREE_ANGLE) * kTip;
		this._wingalphaStar = (Amount.valueOf(alphaStar, NonSI.DEGREE_ANGLE));
		theNasaBlackwellCalculatorMachActualWing.calculate(this._wingalphaStar);
		double cLStar = theNasaBlackwellCalculatorMachActualWing.get_cLEvaluated();
		this._wingcLStar = cLStar;


		// CLMAX 
		theStabilityCalculator.nasaBlackwellCLMax(
				_wingNumberOfPointSemiSpanWise,
				theNasaBlackwellCalculatorMachActualWing,
				_wingClMaxDistribution);
		this._wingcLMax = theStabilityCalculator.getcLMaxFinal();
		this._wingalphaMaxLinear = theStabilityCalculator.getAlphaMaxLinear();
		this._wingliftCoefficientDistributionatCLMax = (
				theStabilityCalculator.liftDistributionAtCLMax);

		// Alpha Stall
		double deltaYPercent =  aeroDatabaseReader
				.getDeltaYvsThickness(
						_wingMaxThicknessMeanAirfoil,
						_wingMeanAirfoilFamily
						);
		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						_wingSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);
		this._wingalphaStall = 
				this._wingalphaMaxLinear
				.plus(deltaAlpha);



		// Initialize Alpha Array Clean
		initializeAlphaArrays();
		// 3D curve	
		this._wingliftCoefficient3DCurve = new Double[this._alphasWing.size()];
		this._wingliftCoefficient3DCurve = 
				LiftCalc.calculateCLvsAlphaArray(
				this._wingcLZero,
				this._wingcLMax,
				this._wingalphaStar,
				this._wingalphaStall,
				this._wingclAlpha,
				MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
				);

		//clAlpha Variable
		this._wingclAlphaArray = LiftCalc.calculateCLAlphaArray(_wingliftCoefficient3DCurve, _alphasWing);

		// cl distribution
		theNasaBlackwellCalculatorMachActualWing.calculate(_wingalphaMaxLinear);
		_wingliftCoefficientDistributionatCLMax = theNasaBlackwellCalculatorMachActualWing.getClTotalDistribution().toArray();

		// set condition Actual
		if (_theCondition == ConditionEnum.CRUISE)
		{
			_wingAlphaZeroLiftCONDITION = this._wingAlphaZeroLift;
			_wingalphaStarCONDITION = this._wingalphaStar;
			_wingalphaMaxLinearCONDITION = this._wingalphaMaxLinear;
			_wingalphaStallCONDITION = this._wingalphaStall;
			_wingcLZeroCONDITION = this._wingcLZero;
			_wingcLStarCONDITION = this._wingcLStar;
			_wingcLMaxCONDITION = this._wingcLMax;
			_wingcLAlphaRadCONDITION = this._wingcLAlphaRad;
			_wingcLAlphaDegCONDITION = this._wingcLAlphaDeg;
			_wingclAlphaCONDITION = this._wingclAlpha;
			_wingliftCoefficient3DCurveCONDITION = this._wingliftCoefficient3DCurve;
			_wingliftCoefficientDistributionatCLMaxCONDITION = this._wingliftCoefficientDistributionatCLMax;
			_wingclAlphaArrayCONDITION = this._wingclAlphaArray;
		}
		}
		
		if (_theCondition == ConditionEnum.TAKE_OFF ||_theCondition == ConditionEnum.LANDING ) {
			// cl alpha 
			theNasaBlackwellCalculatorMachActualWingCLEAN.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
			double clOneMachActual = theNasaBlackwellCalculatorMachActualWingCLEAN.getCLCurrent();
			theNasaBlackwellCalculatorMachActualWingCLEAN.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
			double clTwoMachActual = theNasaBlackwellCalculatorMachActualWingCLEAN.getCLCurrent();
			this._wingcLAlphaRad = (clTwoMachActual-clOneMachActual)/toRadians(4);
			this._wingcLAlphaDeg = (clTwoMachActual-clOneMachActual)/(4);
			this._wingclAlpha = Amount.valueOf( this._wingcLAlphaRad , SI.RADIAN.inverse());

			// alpha zero lift
			this._wingAlphaZeroLift = (
					Amount.valueOf(
							AnglesCalc.alpha0LintegralMeanWithTwist(
									this._wingSurface.doubleValue(SI.SQUARE_METRE),
									this._wingSemiSpan.doubleValue(SI.METER), 
									MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistributionCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistributionCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(this._wingAlphaZeroLiftDistributionCLEAN),
									MyArrayUtils.convertListOfAmountTodoubleArray(this.getWingTwistDistributionCLEAN())
									),
							NonSI.DEGREE_ANGLE
							));

			
			//cl zero
			this._wingcLZero = LiftCalc.calculateLiftCoefficientAtAlpha0(
					_wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE),
					this._wingcLAlphaDeg
					);

			// alphaStar e cl star
			double alphaStar =  _wingAlphaStarBreakPoints.get(0).doubleValue(NonSI.DEGREE_ANGLE) * kRoot +
					_wingAlphaStarBreakPoints.get(1).doubleValue(NonSI.DEGREE_ANGLE) * kKink + 
					_wingAlphaStarBreakPoints.get(2).doubleValue(NonSI.DEGREE_ANGLE) * kTip;
			this._wingalphaStar = (Amount.valueOf(alphaStar, NonSI.DEGREE_ANGLE));
			theNasaBlackwellCalculatorMachActualWingCLEAN.calculate(this._wingalphaStar);
			double cLStar = theNasaBlackwellCalculatorMachActualWingCLEAN.get_cLEvaluated();
			this._wingcLStar = cLStar;


			// CLMAX 
			theStabilityCalculator.nasaBlackwellCLMax(
					_wingNumberOfPointSemiSpanWise,
					theNasaBlackwellCalculatorMachActualWingCLEAN,
					_wingClMaxDistributionCLEAN);
			this._wingcLMax = theStabilityCalculator.getcLMaxFinal();
			this._wingalphaMaxLinear = theStabilityCalculator.getAlphaMaxLinear();
			this._wingliftCoefficientDistributionatCLMax = (
					theStabilityCalculator.liftDistributionAtCLMax);

			// Alpha Stall
			double deltaYPercent =  aeroDatabaseReader
					.getDeltaYvsThickness(
							_wingMaxThicknessMeanAirfoil,
							_wingMeanAirfoilFamily
							);
			Amount<Angle> deltaAlpha = Amount.valueOf(
					aeroDatabaseReader
					.getDAlphaVsLambdaLEVsDy(
							_wingSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);
			this._wingalphaStall = 
					this._wingalphaMaxLinear
					.plus(deltaAlpha);



			// Initialize Alpha Array Clean
			initializeAlphaArrays();
			// 3D curve	
			this._wingliftCoefficient3DCurve = LiftCalc.calculateCLvsAlphaArray(
					this._wingcLZero,
					this._wingcLMax,
					this._wingalphaStar,
					this._wingalphaStall,
					this._wingclAlpha,
					MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
					);

			//clAlpha Variable
			this._wingclAlphaArray = LiftCalc.calculateCLAlphaArray(_wingliftCoefficient3DCurve, _alphasWing);

			// cl distribution
			theNasaBlackwellCalculatorMachActualWing.calculate(_wingalphaMaxLinear);
			_wingliftCoefficientDistributionatCLMax = theNasaBlackwellCalculatorMachActualWing.getClTotalDistribution().toArray();

			
		}
		
		
	}

	public void calculateWingHighLiftCharacteristics(){
		double cLCurrent = theNasaBlackwellCalculatorMachActualWingCLEAN.getCLCurrent();

		theStabilityCalculator.calculateHighLiftDevicesEffects(
				this,
				this._wingDeltaFlap,
				this._wingDeltaSlat,
				cLCurrent
				);

		//------------------------------------------------------
		// CL ZERO HIGH LIFT
		_cLZeroHighLift = _wingcLZero + _deltaCL0Flap;

		//------------------------------------------------------
		// ALPHA ZERO LIFT HIGH LIFT
		_alphaZeroLiftHighLift = 
				Amount.valueOf(
						-(_cLZeroHighLift /_cLAlphaHighLiftDEG),
						NonSI.DEGREE_ANGLE
						);

		//------------------------------------------------------
		// CL MAX HIGH LIFT
		if(_deltaCLmaxSlat == null){
			_cLMaxHighLift = 
					_wingcLMax
					+ _deltaCLmaxFlap;}

		else {
			_cLMaxHighLift = 
					_wingcLMax
					+ _deltaCLmaxFlap
					+ _deltaCLmaxSlat;
		}

		//------------------------------------------------------
		// ALPHA STALL HIGH LIFT
		double deltaYPercent = aeroDatabaseReader
				.getDeltaYvsThickness(
						_wingMaxThicknessMeanAirfoil,
						_wingMeanAirfoilFamily
						);

		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						_wingSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);

		_alphaStallHighLift=
				Amount.valueOf((((_cLMaxHighLift - _cLZeroHighLift) /_cLAlphaHighLiftDEG) + deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE)),
						NonSI.DEGREE_ANGLE);

		//------------------------------------------------------
		// ALPHA STAR HIGH LIFT
		_alphaStarHighLift=
				Amount.valueOf(
						_alphaStallHighLift.doubleValue(NonSI.DEGREE_ANGLE)
						-(_wingalphaStall.doubleValue(NonSI.DEGREE_ANGLE)
								- _wingalphaStar.doubleValue(NonSI.DEGREE_ANGLE)),
						NonSI.DEGREE_ANGLE);
		//------------------------------------------------------
		// CL STAR HIGH LIFT
		_cLStarHighLift= 
				(_cLAlphaHighLiftDEG
						* _alphaStarHighLift
						.doubleValue(NonSI.DEGREE_ANGLE)
						+ _cLZeroHighLift);


		_wingLiftCoefficient3DCurveHighLift = 
				LiftCalc.calculateCLvsAlphaArray(
						_cLZeroHighLift,
						_cLMaxHighLift,
						_alphaStarHighLift,
						_alphaStallHighLift,
						Amount.valueOf(_cLAlphaHighLiftDEG, NonSI.DEGREE_ANGLE.inverse()),
						MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
						);

		//------------------------------------------------------
		// cl alpha array
		this._wingclAlphaArrayHighLift = LiftCalc.calculateCLAlphaArray(_wingLiftCoefficient3DCurveHighLift, _alphasWing);

		if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING)
		{
			_wingAlphaZeroLiftCONDITION = this._alphaZeroLiftHighLift;
			_wingalphaStarCONDITION = this._alphaStarHighLift;
			_wingalphaMaxLinearCONDITION = Amount.valueOf((((_cLMaxHighLift - _cLZeroHighLift) /_cLAlphaHighLiftDEG)),
					NonSI.DEGREE_ANGLE);
			_wingalphaStallCONDITION = this._alphaStallHighLift;;
			_wingcLZeroCONDITION = this._cLZeroHighLift;
			_wingcLStarCONDITION = this._cLStarHighLift;
			_wingcLMaxCONDITION = this._cLMaxHighLift;
			_wingcLAlphaRadCONDITION = this._cLAlphaHighLiftDEG*57.3;
			_wingcLAlphaDegCONDITION = this._cLAlphaHighLiftDEG;
			_wingclAlphaCONDITION = Amount.valueOf(this._cLAlphaHighLiftDEG, NonSI.DEGREE_ANGLE.inverse());
			_wingliftCoefficient3DCurveCONDITION = this._wingLiftCoefficient3DCurveHighLift;
			_wingliftCoefficientDistributionatCLMaxCONDITION = this._wingliftCoefficientDistributionatCLMax;
			_wingclAlphaArrayCONDITION = this._wingclAlphaArrayHighLift;
		}

	}

	// this method calculates the lift curve modifying the nasa blackwell method with airfoils data
	public void calculateWingLiftCharacteristicsFromAirfoil(){
		
		if(!_alphaWingForDistribution.contains(_wingAlphaZeroLiftCONDITION))
			_alphaWingForDistribution.add(_alphaWingForDistribution.size(), _wingAlphaZeroLiftCONDITION);
		if(!_alphaHorizontalTailForDistribution.contains(_hTailAlphaZeroLift))
			_alphaHorizontalTailForDistribution.add(_alphaHorizontalTailForDistribution.size(), _hTailAlphaZeroLift);
		
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			_clNasaBlackwellDistributionModified.add(i,
				LiftCalc.calculateNasaBlackwellDistributionFromAirfoil(
				_alphaWingForDistribution.get(i),
				theNasaBlackwellCalculatorMachActualWing, 
				_wingCLAirfoilsDistributionFinal, 
				_alphasWing,
				_wingCl0Distribution,
				_wingClAlphaDistributionDeg,
				_wingYDistribution
				
				));
		}
		
		_wingLiftCoefficientModified = LiftCalc.calculate3DCLfromNasaBlacwellModified(
				_alphasWing, 
				theNasaBlackwellCalculatorMachActualWing, 
				_wingCLAirfoilsDistributionFinal, 
				_alphasWing,
				_wingChordsDistribution, 
				_wingSurface, 
				_wingCl0Distribution,
				_wingClAlphaDistributionDeg,
				_wingYDistribution
				);
				
		
	}
	
	public void calculateHTailLiftCharacteristics(){
		// cl alpha 
		theNasaBlackwellCalculatorMachActualHTail.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachActual = theNasaBlackwellCalculatorMachActualHTail.getCLCurrent();
		theNasaBlackwellCalculatorMachActualHTail.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachActual = theNasaBlackwellCalculatorMachActualHTail.getCLCurrent();
		this._hTailcLAlphaRad = (clTwoMachActual-clOneMachActual)/toRadians(4);
		this._hTailcLAlphaDeg = (clTwoMachActual-clOneMachActual)/(4);
		this._hTailclAlpha = Amount.valueOf( this._hTailcLAlphaRad , SI.RADIAN.inverse());

		
		if(_horizontalTailCL==MethodEnum.FROM_CFD) {
			this._hTailclAlpha = this._hTailclAlpha.times(1.459);
		}
		
		// alpha zero lift
		this._hTailAlphaZeroLift = (
				Amount.valueOf(
						AnglesCalc.alpha0LintegralMeanWithTwist(
								this._hTailSurface.doubleValue(SI.SQUARE_METRE),
								this._hTailSemiSpan.doubleValue(SI.METER), 
								MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailYDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailChordsDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailAlphaZeroLiftDistribution),
								MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailTwistDistribution)
								),
						NonSI.DEGREE_ANGLE
						));

		//cl zero
		this._hTailcLZero = LiftCalc.calculateLiftCoefficientAtAlpha0(
				_hTailAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE),
				this._hTailcLAlphaDeg
				);

		// alphaStar e cl star
		double alphaStar =  _hTailAlphaStarBreakPoints.get(0).doubleValue(NonSI.DEGREE_ANGLE) * kRootHTail +
				_hTailAlphaStarBreakPoints.get(1).doubleValue(NonSI.DEGREE_ANGLE) * kTipHTail;
		this._hTailalphaStar = (Amount.valueOf(alphaStar, NonSI.DEGREE_ANGLE));
		theNasaBlackwellCalculatorMachActualHTail.calculate(this._hTailalphaStar);
		double cLStar = theNasaBlackwellCalculatorMachActualHTail.get_cLEvaluated();
		this._hTailcLStar = cLStar;


		// CLMAX 
		theStabilityCalculator.nasaBlackwellCLMax(
				_hTailNumberOfPointSemiSpanWise,
				theNasaBlackwellCalculatorMachActualHTail,
				_hTailClMaxDistribution);
		this._hTailcLMax = theStabilityCalculator.getcLMaxFinal();
		this._hTailalphaMaxLinear = theStabilityCalculator.getAlphaMaxLinear();
		this._hTailliftCoefficientDistributionatCLMax = (
				theStabilityCalculator.liftDistributionAtCLMax);

		// Alpha Stall
		double deltaYPercent =  aeroDatabaseReader
				.getDeltaYvsThickness(
						_hTailMaxThicknessMeanAirfoil,
						_hTailMeanAirfoilFamily
						);
//		Amount<Angle> deltaAlpha = Amount.valueOf(
//				aeroDatabaseReader
//				.getDAlphaVsLambdaLEVsDy(
//						_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
//						deltaYPercent
//						),
//				NonSI.DEGREE_ANGLE);
		Amount<Angle> deltaAlpha = Amount.valueOf(4,
				NonSI.DEGREE_ANGLE);
		this._hTailalphaStall = 
				this._hTailalphaMaxLinear
				.plus(deltaAlpha);



		// Initialize Alpha Array Clean
		initializeAlphaArrays();
		// 3D curve	
		this._hTailliftCoefficient3DCurve = LiftCalc.calculateCLvsAlphaArray(
				this._hTailcLZero,
				this._hTailcLMax,
				this._hTailalphaStar,
				this._hTailalphaStall,
				this._hTailclAlpha,
				MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasTail)
				);


		// cl distribution
		theNasaBlackwellCalculatorMachActualHTail.calculate(_hTailalphaMaxLinear);
		_hTailliftCoefficientDistributionatCLMax = theNasaBlackwellCalculatorMachActualHTail.getClTotalDistribution().toArray();
	}

	public void calculateHTailLiftCharacteristicsWithElevatorDeflection() throws InstantiationException, IllegalAccessException{

		Amount<Angle> _anglesOfDeflection = null;

		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){

			theStabilityCalculator.calculateElevatorEffects(
					this,
					_anglesOfElevatorDeflection.get(i));

			if( _horizontalTailCL==MethodEnum.FROM_CFD){
			_tauElevator.put(_anglesOfElevatorDeflection.get(i),
					LiftCalc.calculateTauIndexElevator(
							_elevatorCfC, 
							_hTailAspectRatio,
							highLiftDatabaseReader, 
							aeroDatabaseReader, 
							_anglesOfElevatorDeflection.get(i)
							) 
					- 0.0374    // NANDO CORRECTION
					);
			}
			
			if( _horizontalTailCL==MethodEnum.SEMIEMPIRICAL){
				_tauElevator.put(_anglesOfElevatorDeflection.get(i),
						LiftCalc.calculateTauIndexElevator(
								_elevatorCfC, 
								_hTailAspectRatio,
								highLiftDatabaseReader, 
								aeroDatabaseReader, 
								_anglesOfElevatorDeflection.get(i)
								)
						);
			}

			//------------------------------------------------------
			// ALPHA ZERO LIFT HIGH LIFT
			_hTailalphaZeroLiftElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf(
							_hTailAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE) - 
							(_tauElevator.get(_anglesOfElevatorDeflection.get(i))*_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE));

			
			if( _horizontalTailCL==MethodEnum.FROM_CFD){
			// CL ALPHA ---- NANDO CORRECTION--------------
			
			if( Math.abs(_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)) <=15){
			_hTailCLAlphaElevator.put(_anglesOfElevatorDeflection.get(i),
					_hTailcLAlphaDeg*(
				 -0.0006*Math.pow(Math.abs(_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)),3) + 
				 0.0092*Math.pow(Math.abs(_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)), 2) -
				 0.0023* Math.abs(_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)) + 
				 1.459)
				 );
			}
			
			if( Math.abs(_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)) >15){
				_hTailCLAlphaElevator.put(_anglesOfElevatorDeflection.get(i),
						_hTailcLAlphaDeg*(
					 -0.0006*Math.pow(Math.abs(15),3) + 
					 0.0092*Math.pow(Math.abs(15), 2) -
					 0.0023* Math.abs(15) + 
					 1.459)
					 );
			}
		
			
			//------------------------------------------------------
			// CL ZERO HIGH LIFT
			_hTailCLZeroElevator.put(
					_anglesOfElevatorDeflection.get(i), 
					-_hTailCLAlphaElevator.get(_anglesOfElevatorDeflection.get(i)) *
					_hTailalphaZeroLiftElevator.get(_anglesOfElevatorDeflection.get(i)).doubleValue(NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// CL MAX HIGH LIFT

			_hTailcLMaxElevator.put(
					_anglesOfElevatorDeflection.get(i),
					_hTailcLMax + _deltaCLMaxElevator.get(_anglesOfElevatorDeflection.get(i)));


			//------------------------------------------------------
			// ALPHA STALL HIGH LIFT
			double deltaYPercent = aeroDatabaseReader
					.getDeltaYvsThickness(
							_hTailMaxThicknessMeanAirfoil,
							_hTailMeanAirfoilFamily
							);

			Amount<Angle> deltaAlpha = Amount.valueOf(
					aeroDatabaseReader
					.getDAlphaVsLambdaLEVsDy(
							_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);

			_hTailalphaStallLiftElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf((((_hTailcLMaxElevator.get(_anglesOfElevatorDeflection.get(i)) - 
							_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i))) /
							_hTailCLAlphaElevator.get(_anglesOfElevatorDeflection.get(i)))
							+ deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// ALPHA STAR HIGH LIFT		
			_hTailalphaStarElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf(_hTailalphaStar.doubleValue(NonSI.DEGREE_ANGLE)-
							(_tauElevator.get(_anglesOfElevatorDeflection.get(i)) * _anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)), 
							NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// CL STAR HIGH LIFT
			_hTailCLStarElevator.put(
					_anglesOfElevatorDeflection.get(i),
					_hTailCLAlphaElevator.get(_anglesOfElevatorDeflection.get(i)) * 
					_hTailalphaStarElevator.get(_anglesOfElevatorDeflection.get(i)).doubleValue(NonSI.DEGREE_ANGLE)+
					_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i))); 

			_hTailLiftCoefficient3DCurveWithElevator.put(
					_anglesOfElevatorDeflection.get(i),
					LiftCalc.calculateCLvsAlphaArray(
							_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailcLMaxElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailalphaStarElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailalphaStallLiftElevator.get(_anglesOfElevatorDeflection.get(i)),
							Amount.valueOf(_hTailCLAlphaElevator.get(_anglesOfElevatorDeflection.get(i)), NonSI.DEGREE_ANGLE.inverse()),
							MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasTail)
							));
		}
			
			if(_horizontalTailCL == MethodEnum.SEMIEMPIRICAL) {
			//------------------------------------------------------
			// CL ZERO HIGH LIFT
			_hTailCLZeroElevator.put(
					_anglesOfElevatorDeflection.get(i), 
					-_hTailcLAlphaDeg *
					_hTailalphaZeroLiftElevator.get(_anglesOfElevatorDeflection.get(i)).doubleValue(NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// CL MAX HIGH LIFT

			_hTailcLMaxElevator.put(
					_anglesOfElevatorDeflection.get(i),
					_hTailcLMax + _deltaCLMaxElevator.get(_anglesOfElevatorDeflection.get(i)));


			//------------------------------------------------------
			// ALPHA STALL HIGH LIFT
			double deltaYPercent = aeroDatabaseReader
					.getDeltaYvsThickness(
							_hTailMaxThicknessMeanAirfoil,
							_hTailMeanAirfoilFamily
							);

			Amount<Angle> deltaAlpha = Amount.valueOf(
					aeroDatabaseReader
					.getDAlphaVsLambdaLEVsDy(
							_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);

			_hTailalphaStallLiftElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf((((_hTailcLMaxElevator.get(_anglesOfElevatorDeflection.get(i)) - 
							_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i))) /
							_hTailcLAlphaDeg) + deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// ALPHA STAR HIGH LIFT		
			_hTailalphaStarElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf(_hTailalphaStar.doubleValue(NonSI.DEGREE_ANGLE)-
							(_tauElevator.get(_anglesOfElevatorDeflection.get(i)) * _anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)), 
							NonSI.DEGREE_ANGLE));

			//------------------------------------------------------
			// CL STAR HIGH LIFT
			_hTailCLStarElevator.put(
					_anglesOfElevatorDeflection.get(i),
					_hTailcLAlphaDeg * 
					_hTailalphaStarElevator.get(_anglesOfElevatorDeflection.get(i)).doubleValue(NonSI.DEGREE_ANGLE)+
					_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i))); 

			_hTailLiftCoefficient3DCurveWithElevator.put(
					_anglesOfElevatorDeflection.get(i),
					LiftCalc.calculateCLvsAlphaArray(
							_hTailCLZeroElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailcLMaxElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailalphaStarElevator.get(_anglesOfElevatorDeflection.get(i)),
							_hTailalphaStallLiftElevator.get(_anglesOfElevatorDeflection.get(i)),
							Amount.valueOf(_hTailcLAlphaDeg, NonSI.DEGREE_ANGLE.inverse()),
							MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasTail)
							));
			}
		}
		

		 this._deltaEAnglesArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.convertFromDoubleToPrimitive(
						MyArrayUtils.linspace(-30, 5, 50)), NonSI.DEGREE_ANGLE);
		
		
		for (int i=0; i<this._deltaEAnglesArray.size(); i++){

			theStabilityCalculator.calculateElevatorEffects(
					this,
					_deltaEAnglesArray.get(i));

			if(_horizontalTailCL == MethodEnum.FROM_CFD){
			_tauElevatorArray.put(_deltaEAnglesArray.get(i),
					LiftCalc.calculateTauIndexElevator(
							_elevatorCfC, 
							_hTailAspectRatio,
							highLiftDatabaseReader, 
							aeroDatabaseReader, 
							_deltaEAnglesArray.get(i)
							)
					- 0.0374    // NANDO CORRECTION
					); 
		}
			if(_horizontalTailCL == MethodEnum.SEMIEMPIRICAL){
				_tauElevatorArray.put(_deltaEAnglesArray.get(i),
						LiftCalc.calculateTauIndexElevator(
								_elevatorCfC, 
								_hTailAspectRatio,
								highLiftDatabaseReader, 
								aeroDatabaseReader, 
								_deltaEAnglesArray.get(i)
								)
						); 
			
		}
		}
		
	}


	public void calculateWingFuselageLiftCharacterstics(){

		// cl alpha	
		_fuselageWingClAlphaDeg = LiftCalc.calculateCLAlphaFuselage(
				_wingclAlpha, 
				_wingSpan, 
				_fuselageDiameter);

		_fuselageWingClAlpha = this._fuselageWingClAlphaDeg.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();

		// cl Zero
		_fuselageWingClZero = -_fuselageWingClAlpha * _wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE);

		// cl max
		_fuselageWingClMax = _wingcLMaxCONDITION;

		//cl star
		_fuselageWingCLStar = _wingcLStarCONDITION;

		//alphaStar
		_fuselageWingAlphaStar = Amount.valueOf(
				(_fuselageWingCLStar - _fuselageWingClZero)/_fuselageWingClAlpha, 
				NonSI.DEGREE_ANGLE);

		//alpha stall
		double deltaAlphaStarDeg = 	_fuselageWingAlphaStar.doubleValue(NonSI.DEGREE_ANGLE) - 
				_wingalphaStarCONDITION.doubleValue(NonSI.DEGREE_ANGLE);

		_fuselageWingAlphaStall = Amount.valueOf(
				_wingalphaStallCONDITION.doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
				NonSI.DEGREE_ANGLE);

		// curve 
		this._fuselagewingliftCoefficient3DCurve = LiftCalc.calculateCLvsAlphaArray(
				this._fuselageWingClZero,
				this._fuselageWingClMax,
				this._fuselageWingAlphaStar,
				this._fuselageWingAlphaStall,
				this._fuselageWingClAlphaDeg,
				MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
				);

	}

	public void initializeDragArray(){ 
// WING-------------------------------------------------------------
		//------------cd curve---------------------------
	
		if(this._wingDragMethod==MethodEnum.INPUT){

			Double [] wingDragCurve = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(cLWingDragPolar),
					MyArrayUtils.convertToDoublePrimitive(cDPolarWing),
					MyArrayUtils.convertToDoublePrimitive(_wingliftCoefficient3DCurveCONDITION));

			for (int i=0; i<wingDragCurve.length; i++)
				_wingDragCoefficient3DCurve.add(i,wingDragCurve[i]);
		}

		if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT || this._wingDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){	 // _wingliftCoefficient3DCurveCONDITION, _wingCdAirfoilDistribution
			double minValue = 0.0;
			double maxValue = 0.0;
			for (int i=0; i<_wingNumberOfGivenSections; i++){
				int limit = clPolarAirfoilWingDragPolar.get(i).size();
				for(int ii=0; ii<limit; ii++){
					if(clPolarAirfoilWingDragPolar.get(i).get(ii)<minValue)
						minValue = clPolarAirfoilWingDragPolar.get(i).get(ii);
					if(clPolarAirfoilWingDragPolar.get(i).get(ii)>maxValue)
						maxValue = clPolarAirfoilWingDragPolar.get(i).get(ii);
				}
			}
			clListDragWing = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));

		_wingCdAirfoilDistribution = AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
				clListDragWing,
				clPolarAirfoilWingDragPolar, 
				cDPolarAirfoilsWing,
				_wingYAdimensionalBreakPoints, 
				_wingYAdimensionalDistribution
				);
		
		}	
			// TODO 
			if(this._wingDragMethod==MethodEnum.CLASSIC){}
			if(this._wingDragMethod==MethodEnum.AIRFOIL_DISTRIBUTION){}
	

		//-----------cl curve--------------------------------
		if(_wingairfoilLiftCoefficientCurve == MethodEnum.INPUT){
		
		_wingCLAirfoilsDistributionFinal = AirfoilCalc.calculateCLMatrixAirfoils(
				_alphasWing, 
				alphaAirfoilsWing,
				clDistributionAirfoilsWing,
				_wingYAdimensionalBreakPoints, 
				_wingYAdimensionalDistribution
				);

	}
		
		//-----cm curve--------------------------------
		
		if(_wingairfoilMomentCoefficientCurve==MethodEnum.INPUTCURVE){
			// cl array
			double minValue = 1.0;
			double maxValue = 0.0;
			for (int i=0; i<_wingNumberOfGivenSections; i++){
				int limit = _wingCLMomentAirfoilInput.get(i).size();
				for(int ii=0; ii<limit; ii++){
					if(_wingCLMomentAirfoilInput.get(i).get(ii)<minValue)
						minValue = _wingCLMomentAirfoilInput.get(i).get(ii);
					if(_wingCLMomentAirfoilInput.get(i).get(ii)>maxValue)
						maxValue = _wingCLMomentAirfoilInput.get(i).get(ii);
				}
			}
			clListMomentWing = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));
			_wingCLMomentAirfoilOutput = clListMomentWing;
			
			// matrix
			_wingCMMomentAirfoilOutput = AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
					clListMomentWing,
					_wingCLMomentAirfoilInput, 
					_wingCMMomentAirfoilInput,
					_wingYAdimensionalBreakPoints, 
					_wingYAdimensionalDistribution
					);

		}
		
// HORIZONTAL TAIL ----------------------------------------
		//---------cd curve-----------------------
		if(this._hTailDragMethod==MethodEnum.INPUT){

			Double [] hTailDragCurve = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(cLhTailDragPolar),
					MyArrayUtils.convertToDoublePrimitive(cDPolarhTail),
					MyArrayUtils.convertToDoublePrimitive(_hTailliftCoefficient3DCurveCONDITION));

			for (int i=0; i<hTailDragCurve.length; i++)
				_hTailDragCoefficient3DCurve.add(i,hTailDragCurve[i]);
		}

		if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT ||  this._hTailDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){	 // _wingliftCoefficient3DCurveCONDITION, _wingCdAirfoilDistribution

			double minValue = 0.0;
			double maxValue = 0.0;
			for (int i=0; i<_hTailnumberOfGivenSections; i++){
				int limit = clPolarAirfoilHTailDragPolar.get(i).size();
				for(int ii=0; ii<limit; ii++){
					if(clPolarAirfoilHTailDragPolar.get(i).get(ii)<minValue)
						minValue = clPolarAirfoilHTailDragPolar.get(i).get(ii);
					if(clPolarAirfoilHTailDragPolar.get(i).get(ii)>maxValue)
						maxValue = clPolarAirfoilHTailDragPolar.get(i).get(ii);
				}
			}
			clListDragTail = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));
	
		
		_hTailCdAirfoilDistribution = AirfoilCalc.calculateAerodynamicCoefficientsMatrixAirfoils(
				clListDragTail,
				clPolarAirfoilHTailDragPolar, 
				cDPolarAirfoilsHTail,
				_hTailYAdimensionalBreakPoints, 
				_hTailYAdimensionalDistribution
				);
		}
		
			// TODO 
			if(this._hTailDragMethod==MethodEnum.CLASSIC){}
			if(this._hTailDragMethod==MethodEnum.AIRFOIL_DISTRIBUTION){}
	

		//-----------cl curve--------------------------------
		if(_hTailairfoilLiftCoefficientCurve == MethodEnum.INPUT){
		
		_hTailCLAirfoilsDistributionFinal = AirfoilCalc.calculateCLMatrixAirfoils(
				_alphasTail, 
				alphaAirfoilsHTail,
				clDistributionAirfoilsHTail,
				_hTailYAdimensionalBreakPoints, 
				_hTailYAdimensionalDistribution
				);

		}
	}

	public void calculateWingDragCharacterstics(){
		// if  MethodEnum = INPUT the arrays are already builded  
		
		if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT || this._wingDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
//			
//			// PARASITE DRAG-----------------------------------------
		
			_wingParasiteDragCoefficientDistribution = DragCalc.calculateParasiteDragLiftingSurfaceFromAirfoil(
					_alphasWing,
					theNasaBlackwellCalculatorMachActualWing,
					_wingCdAirfoilDistribution,
					clListDragWing, 
					_wingChordsDistribution, 
					_wingSurface, 
					_wingYDistribution
					);
		
			
			// INDUCED DRAG------------------------------------------
			  //induced angle of attack
			for (int i=0; i<_numberOfAlphasBody; i++){
				_wingInducedAngleOfAttack.add(i,theStabilityCalculator.calculateInducedAngleOfAttackDistribution(
						_alphasWing.get(i), 
						theNasaBlackwellCalculatorMachActualWing, 
						_altitude, 
						_machCurrent, 
						_wingNumberOfPointSemiSpanWise
						));
			}
			
			if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT){
			_wingInducedDragCoefficientDistribution = DragCalc.calculateInducedDragLiftingSurfaceFromAirfoil(
					_alphasWing,
					theNasaBlackwellCalculatorMachActualWing, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingChordsDistribution, 
					_wingSurface, 
					_wingYDistribution,
					_wingCl0Distribution,
					_wingClAlphaDistributionDeg,
					_wingInducedAngleOfAttack
					);
			}
				
			if(this._wingDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
				_wingInducedDragCoefficientDistribution = DragCalc.calculateCDInducedParabolic(
						_wingliftCoefficient3DCurveCONDITION,
						_wingAspectRatio,
						_wingOswaldFactor);
						
			}
		}
			
			// TOTAL DRAG--------------------------------------------
			
			for (int i=0; i<_numberOfAlphasBody; i++){
				_wingDragCoefficient3DCurveTemp.add(
						i,
						_wingParasiteDragCoefficientDistribution.get(i)+_wingInducedDragCoefficientDistribution.get(i));
			}
			
			if (_theCondition == ConditionEnum.CRUISE){
				_wingDragCoefficient3DCurve = _wingDragCoefficient3DCurveTemp;
			}

			if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
				if(this._deltaDueToFlapMethod==MethodEnum.AIRFOIL_INPUT){
					_wingDragCoefficient3DCurve = _wingDragCoefficient3DCurveTemp;

				}

				if(this._deltaDueToFlapMethod==MethodEnum.SEMIEMPIRICAL){
					// delta CD0

				
					for (int i=0; i<_numberOfAlphasBody; i++){
						_wingDragCoefficient3DCurve.add(i, _wingDragCoefficient3DCurveTemp.get(i) + _deltaCD0);
					}

				}
			}
		
	}
	public void calculateHTailDragCharacterstics() throws InstantiationException, IllegalAccessException{
		if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT || this._hTailDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT ){
			// PARASITE DRAG-----------------------------------------
			
				_hTailParasiteDragCoefficientDistribution = DragCalc.calculateParasiteDragLiftingSurfaceFromAirfoil(
						_alphasTail,
						theNasaBlackwellCalculatorMachActualHTail,
						_hTailCdAirfoilDistribution,
						clListDragTail, 
						_hTailChordsDistribution, 
						_hTailSurface, 
						_hTailYDistribution
						);
			
				
				// INDUCED DRAG------------------------------------------
				  //induced angle of attack
				for (int i=0; i<_numberOfAlphasBody; i++){
					_hTailInducedAngleOfAttack.add(i,theStabilityCalculator.calculateInducedAngleOfAttackDistribution(
							_alphasTail.get(i), 
							theNasaBlackwellCalculatorMachActualHTail, 
							_altitude, 
							_machCurrent, 
							_hTailNumberOfPointSemiSpanWise
							));
				}
				
				if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT){
				_hTailInducedDragCoefficientDistribution = DragCalc.calculateInducedDragLiftingSurfaceFromAirfoil(
						_alphasTail,
						theNasaBlackwellCalculatorMachActualHTail, 
						_hTailCLAirfoilsDistributionFinal,
						_alphasTail,
						_hTailChordsDistribution, 
						_hTailSurface, 
						_hTailYDistribution,
						_hTailCl0Distribution,
						_hTailClAlphaistributionDeg,
						_hTailInducedAngleOfAttack
						);
				}
				
				if(this._hTailDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
					_hTailInducedDragCoefficientDistribution = DragCalc.calculateCDInducedParabolic(
							_hTailLiftCoefficient3DCurveWithElevator.get(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)),
							_hTailAspectRatio,
							_hTailOswaldFactor);
							
				}
						
			}
			
			
			// TOTAL DRAG--------------------------------------------
			
		for (int i=0; i<_numberOfAlphasBody; i++){
			_hTailDragCoefficient3DCurve.add(
					i,
					_hTailParasiteDragCoefficientDistribution.get(i)+_hTailInducedDragCoefficientDistribution.get(i));
		}			
		
		// WITH ELEVATOR DEFLECTION----------------------------------
		
		
		Double [] cdTemp;
		_deltaCD0Elevator = new HashMap<Amount<Angle>, Double>();
		
		if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT){
		
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){ 
			theStabilityCalculator.calculateElevatorEffects(this, _anglesOfElevatorDeflection.get(i));
			cdTemp = new Double[_numberOfAlphasBody];
			
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
				cdTemp[ii] = _hTailDragCoefficient3DCurve.get(ii)
						+ _deltaCD0Elevator.get(_anglesOfElevatorDeflection.get(i));
			}
			
			_hTailDragCoefficient3DCurveWithElevator.put(
				_anglesOfElevatorDeflection.get(i),
				cdTemp
					);
		}
		}
		
		
		if(this._hTailDragMethod==MethodEnum.PARASITE_AIRFOIL_INPUT){
		
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){ 
			List<Double> inducedDragTemp = new ArrayList<>();
			inducedDragTemp = DragCalc.calculateCDInducedParabolic(
						_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i)),
						_hTailAspectRatio,
						_hTailOswaldFactor);
						
			theStabilityCalculator.calculateElevatorEffects(this, _anglesOfElevatorDeflection.get(i));
			cdTemp = new Double[_numberOfAlphasBody];
			
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
				
				
				cdTemp[ii] = _hTailParasiteDragCoefficientDistribution.get(ii)+
						inducedDragTemp.get(ii) 
					+	_deltaCD0Elevator.get(_anglesOfElevatorDeflection.get(i));
			}
			
			_hTailDragCoefficient3DCurveWithElevator.put(
				_anglesOfElevatorDeflection.get(i),
				cdTemp
					);
		}
		}
		
	}

//	public static void calculateDeltaCD0LandingGears() {
//		
//		if(deltaCL0flap == null)
//			deltaCL0flap = 0.0;
//		
//		double deltaCD0 = 0.0;
//		double deltaCD0Basic = 0.0;
//		double functionAlphaDeltaFlap = 0.0;
//		
//		Amount<Area> frontalTiresTotalArea = Amount.valueOf(0.0, SI.SQUARE_METRE);
//		Amount<Area> rearTiresTotalArea = Amount.valueOf(0.0, SI.SQUARE_METRE);
//		
//		for(int i=0; i<landingGears.getNumberOfFrontalWheels(); i++) {
//			frontalTiresTotalArea = frontalTiresTotalArea.plus(landingGears.getFrontalWheelsHeight().times(landingGears.getFrontalWheelsWidth()));
//		}
//		
//		for(int i=0; i<landingGears.getNumberOfRearWheels(); i++) {
//			rearTiresTotalArea = rearTiresTotalArea.plus(landingGears.getRearWheelsHeight().times(landingGears.getRearWheelsWidth()));
//		}
//		
//		deltaCD0Basic = ((1.5*frontalTiresTotalArea.getEstimatedValue())
//				+(0.75*rearTiresTotalArea.getEstimatedValue()))
//				/(wing.getSurface().getEstimatedValue());
//				
//		if(landingGears.getMountingPosition() == MountingPosition.WING) {
//		
//			Amount<Area> flapSurface = Amount.valueOf(
//					wing.getSpan().getEstimatedValue()							
//					/2*wing.getLiftingSurfaceCreator().getRootChordEquivalentWing().getEstimatedValue()
//					*(2-((1-wing.getLiftingSurfaceCreator().getTaperRatioEquivalentWing())
//							*(wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
//									+wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getInnerStationSpanwisePosition())))
//					*(wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getOuterStationSpanwisePosition()
//							-wing.getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getInnerStationSpanwisePosition()),
//					SI.SQUARE_METRE
//					);
//			
//			functionAlphaDeltaFlap = 
//					Math.pow(1-(0.04
//							*(cL+(deltaCL0flap*((1.5*(wing.getSurface().divide(flapSurface).getEstimatedValue()))-1))
//									)
//							/(landingGears.getMainLegsLenght().getEstimatedValue()
//									/(wing.getSurface().getEstimatedValue()/wing.getSpan().getEstimatedValue())
//									)
//							)
//							,2);
//		}
//		else if((landingGears.getMountingPosition() == MountingPosition.FUSELAGE)
//				|| (landingGears.getMountingPosition() == MountingPosition.NACELLE)) {
//		
//			functionAlphaDeltaFlap = 
//					Math.pow(1-(0.04*cL/(landingGears.getMainLegsLenght().getEstimatedValue()
//									/(wing.getSurface().getEstimatedValue()/wing.getSpan().getEstimatedValue())
//									)
//							)
//							,2);		
//		}
//		
//		deltaCD0 = deltaCD0Basic*functionAlphaDeltaFlap;
//		
//		return deltaCD0;
//	}
	public void calculateWingXAC(){
		
		// MAC
		Double mac = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_wingYDistribution), // y
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_wingChordsDistribution.stream()
						.map(c -> c.pow(2))
						.collect(Collectors.toList())
						) // c^2
				);
		mac = 2.0 * mac / _wingSurface.doubleValue(SI.SQUARE_METRE); // *= 2/S
		_wingMAC = Amount.valueOf(mac,1e-9,SI.METRE);
		
		Tuple2<
		List<Amount<Length>>, // Xle
		List<Amount<Length>>  // c
	> xleTimeCTuple = Tuple.of(_wingXleDistribution, _wingChordsDistribution);
	
	List<Double> xleTimeC = IntStream.range(0, _wingYDistribution.size())
			.mapToObj(i -> 
				xleTimeCTuple._1.get(i).doubleValue(SI.METRE)
				*xleTimeCTuple._2.get(i).doubleValue(SI.METRE)) // xle * c
			.collect(Collectors.toList());
	
	Double xle = MyMathUtils.integrate1DSimpsonSpline(
			MyArrayUtils.convertListOfAmountTodoubleArray(
					_wingYDistribution), // y
			MyArrayUtils.convertToDoublePrimitive(xleTimeC) // xle * c
		);
	
	
		xle = 2.0 * xle / _wingSurface.doubleValue(SI.SQUARE_METRE);
		_wingMeanAerodynamicChordLeadingEdgeX = Amount.valueOf(xle,1e-9,SI.METRE);
	
	  // AC 
		// de young harper
		Amount<Length> _wingXACMRFdeYoung;
		
		_wingXACMAC.put(MethodEnum.DEYOUNG_HARPER,
				Amount.valueOf(LSGeometryCalc.calcXacFromLEMacDeYoungHarper(
				_wingAspectRatio,
				_wingMAC.doubleValue(SI.METER), 
				_wingTaperRatio,
				_wingSweepQuarterChord.doubleValue(SI.RADIAN)
				),
				SI.METER));
		
		_wingXACMACpercent.put(MethodEnum.DEYOUNG_HARPER, 
				(_wingXACMAC.get(MethodEnum.DEYOUNG_HARPER).doubleValue(SI.METER) / _wingMAC.doubleValue(SI.METER))
				);
		
		_wingXACLRF.put(MethodEnum.DEYOUNG_HARPER,
				_wingXACMAC.get(MethodEnum.DEYOUNG_HARPER).plus(_wingMeanAerodynamicChordLeadingEdgeX));
		
		_wingXACBRF.put(MethodEnum.DEYOUNG_HARPER,
				_wingXACMAC.get(MethodEnum.DEYOUNG_HARPER).plus(_wingMeanAerodynamicChordLeadingEdgeX).plus(_xApexWing));
		
		 // Napolitano Datcom
		Amount<Length> _wingXACMRFNapolitano;
		
		_wingXACMAC.put(MethodEnum.NAPOLITANO_DATCOM,
				Amount.valueOf(
						LSGeometryCalc.calcXacFromNapolitanoDatcom(
								_wingMAC.doubleValue(SI.METER),
								_wingTaperRatio,
								_wingSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
								_wingAspectRatio,  
								_machCurrent,
								aeroDatabaseReader
								),
						SI.METER));
		
		_wingXACMACpercent.put(MethodEnum.NAPOLITANO_DATCOM, 
				(_wingXACMAC.get(MethodEnum.NAPOLITANO_DATCOM).doubleValue(SI.METER) / _wingMAC.doubleValue(SI.METER))
				);
		
		_wingXACLRF.put(
				MethodEnum.NAPOLITANO_DATCOM,
				_wingXACMAC.get(MethodEnum.NAPOLITANO_DATCOM).plus(_wingMeanAerodynamicChordLeadingEdgeX)
						);
		
		_wingXACBRF.put(MethodEnum.NAPOLITANO_DATCOM,
				_wingXACMAC.get(MethodEnum.NAPOLITANO_DATCOM).plus(_wingMeanAerodynamicChordLeadingEdgeX).plus(_xApexWing));
		

		_wingZACMAC = LSGeometryCalc.calcZacFromIntegral(
				_wingSurface,
				_wingYLEDistribution,
				_wingChordsDistribution,
				_wingYDistribution
				);
		
		_wingYACMAC = LSGeometryCalc.calcZacFromIntegral(
				_wingSurface,
				_wingYDistribution,
				_wingChordsDistribution,
				_wingYDistribution
				);
				
		
	}
	public void calculateWingBodyXAC(){
		if (_fuselageMomentMethod ==  MethodEnum.FUSDES){
		_deltaXACdueToFuselage = - (_fuselageCMAlpha.get(MethodEnum.FUSDES)/_wingcLAlphaDegCONDITION);
		}
		
		if (_fuselageMomentMethod ==  MethodEnum.INPUT){
			_deltaXACdueToFuselage = - (_fuselageCMAlpha.get(MethodEnum.INPUT)/_wingcLAlphaDegCONDITION);	
		}
		
		_wingBodyXACBRF.put(MethodEnum.DEYOUNG_HARPER,
				Amount.valueOf(_wingXACBRF.get(MethodEnum.DEYOUNG_HARPER).doubleValue(SI.METER) + _deltaXACdueToFuselage,
						SI.METER)
				);
		
		_wingBodyXACBRF.put(MethodEnum.NAPOLITANO_DATCOM,
				Amount.valueOf(_wingXACBRF.get(MethodEnum.NAPOLITANO_DATCOM).doubleValue(SI.METER) + _deltaXACdueToFuselage,
						SI.METER)
				);
		
	}
	
	public void calculateHTailXAC(){	
		// MAC
		Double mac = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_hTailYDistribution), // y
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_hTailChordsDistribution.stream()
						.map(c -> c.pow(2))
						.collect(Collectors.toList())
						) // c^2
				);
		mac = 2.0 * mac / _hTailSurface.doubleValue(SI.SQUARE_METRE); // *= 2/S
		_hTailMAC = Amount.valueOf(mac,1e-9,SI.METRE);
		
		Tuple2<
		List<Amount<Length>>, // Xle
		List<Amount<Length>>  // c
	> xleTimeCTuple = Tuple.of(_hTailXleDistribution, _hTailChordsDistribution);
	
	List<Double> xleTimeC = IntStream.range(0, _hTailYDistribution.size())
			.mapToObj(i -> 
				xleTimeCTuple._1.get(i).doubleValue(SI.METRE)
				*xleTimeCTuple._2.get(i).doubleValue(SI.METRE)) // xle * c
			.collect(Collectors.toList());
	
	Double xle = MyMathUtils.integrate1DSimpsonSpline(
			MyArrayUtils.convertListOfAmountTodoubleArray(
					_hTailYDistribution), // y
			MyArrayUtils.convertToDoublePrimitive(xleTimeC) // xle * c
		);
	
	
		xle = 2.0 * xle / _hTailSurface.doubleValue(SI.SQUARE_METRE);
		_hTailMeanAerodynamicChordLeadingEdgeX = Amount.valueOf(xle,1e-9,SI.METRE);
	
	  // AC 
		// de young harper
		Amount<Length> _wingXACMRFdeYoung;
		
		_hTailXACMAC.put(MethodEnum.DEYOUNG_HARPER,
				Amount.valueOf(LSGeometryCalc.calcXacFromLEMacDeYoungHarper(
				_hTailAspectRatio,
				_hTailMAC.doubleValue(SI.METER), 
				_hTailTaperRatio,
				_hTailSweepQuarterChord.doubleValue(SI.RADIAN)
				),
				SI.METER));
		
		_hTailXACMACpercent.put(MethodEnum.DEYOUNG_HARPER, 
				(_hTailXACMAC.get(MethodEnum.DEYOUNG_HARPER).doubleValue(SI.METER) / _hTailMAC.doubleValue(SI.METER))
				);
		
		_hTailXACLRF.put(MethodEnum.DEYOUNG_HARPER,
				_hTailXACMAC.get(MethodEnum.DEYOUNG_HARPER).plus(_hTailMeanAerodynamicChordLeadingEdgeX));
		
		 // Napolitano Datcom
		Amount<Length> _wingXACMRFNapolitano;
		
		_hTailXACMAC.put(MethodEnum.NAPOLITANO_DATCOM,
				Amount.valueOf(
						LSGeometryCalc.calcXacFromNapolitanoDatcom(
								_hTailMAC.doubleValue(SI.METER),
								_hTailTaperRatio,
								_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
								_hTailAspectRatio,  
								_machCurrent,
								aeroDatabaseReader
								),
						SI.METER));
		
		_hTailXACMACpercent.put(MethodEnum.NAPOLITANO_DATCOM, 
				(_hTailXACMAC.get(MethodEnum.NAPOLITANO_DATCOM).doubleValue(SI.METER) / _hTailMAC.doubleValue(SI.METER))
				);
		
		_hTailXACLRF.put(
				MethodEnum.NAPOLITANO_DATCOM,
				_hTailXACMAC.get(MethodEnum.NAPOLITANO_DATCOM).plus(_hTailMeanAerodynamicChordLeadingEdgeX)
						);
		
		_hTailXACBRF.put(MethodEnum.NAPOLITANO_DATCOM,
				_hTailXACMAC.get(MethodEnum.DEYOUNG_HARPER).plus(_wingMeanAerodynamicChordLeadingEdgeX).plus(_xApexHTail));

	}
	
//-----------	
	public void calculateWingMomentCharacterstics(){
		
//	// respect TO AC DE YOUNG HARPER	
//	Amount<Length> momentumPoleYH = Amount.valueOf( 
//			_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
//			_wingXACMACpercent.get(MethodEnum.DEYOUNG_HARPER)* 
//			_wingMAC.doubleValue(SI.METER), SI.METER);
//		
//	if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){
//		_wingMomentCoefficientAC.put(MethodEnum.DEYOUNG_HARPER, 
//				MomentCalc.calcCMLiftingSurfaceWithIntegral(
//				theNasaBlackwellCalculatorMachActualWing, 
//				_alphasWing, 
//				_wingMAC,
//				_wingYDistribution, 
//				_wingCl0Distribution, 
//				_wingClAlphaDistributionDeg, 
//				_wingCmACDistribution, 
//				_wingXACDistribution, 
//				_wingChordsDistribution, 
//				_wingXleDistribution, 
//				_wingCLAirfoilsDistributionFinal, 
//				_alphasWing,
//				_wingSurface, 
//				momentumPoleYH
//				));
//	}
//	
//	if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
//		_wingMomentCoefficientAC.put(MethodEnum.DEYOUNG_HARPER, 
//				MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
//				theNasaBlackwellCalculatorMachActualWing, 
//				_alphasWing, 
//				_wingMAC,
//				_wingYDistribution, 
//				_wingCl0Distribution, 
//				_wingClAlphaDistributionDeg, 
//				_wingCLMomentAirfoilOutput,
//				_wingCMMomentAirfoilOutput,
//				_wingXACDistribution, 
//				_wingChordsDistribution, 
//				_wingXleDistribution, 
//				_wingCLAirfoilsDistributionFinal, 
//				_alphasWing,
//				_wingSurface, 
//				momentumPoleYH
//				));
//	}
//		// respect TO AC NAPOLITANO DATCOM	
//		Amount<Length> momentumPoleND = Amount.valueOf( 
//				_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
//				_wingXACMACpercent.get(MethodEnum.NAPOLITANO_DATCOM)* 
//				_wingMAC.doubleValue(SI.METER), SI.METER);
//		
//		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){		
//			_wingMomentCoefficientAC.put(MethodEnum.NAPOLITANO_DATCOM, 
//					MomentCalc.calcCMLiftingSurfaceWithIntegral(
//					theNasaBlackwellCalculatorMachActualWing, 
//					_alphasWing, 
//					_wingMAC,
//					_wingYDistribution, 
//					_wingCl0Distribution, 
//					_wingClAlphaDistributionDeg, 
//					_wingCmACDistribution, 
//					_wingXACDistribution, 
//					_wingChordsDistribution, 
//					_wingXleDistribution, 
//					_wingCLAirfoilsDistributionFinal, 
//					_alphasWing,
//					_wingSurface, 
//					momentumPoleND
//					));
//		}
//		
//		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){		
//			_wingMomentCoefficientAC.put(MethodEnum.NAPOLITANO_DATCOM, 
//					MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
//					theNasaBlackwellCalculatorMachActualWing, 
//					_alphasWing, 
//					_wingMAC,
//					_wingYDistribution, 
//					_wingCl0Distribution, 
//					_wingClAlphaDistributionDeg, 
//					_wingCLMomentAirfoilOutput,
//					_wingCMMomentAirfoilOutput,
//					_wingXACDistribution, 
//					_wingChordsDistribution, 
//					_wingXleDistribution, 
//					_wingCLAirfoilsDistributionFinal, 
//					_alphasWing,
//					_wingSurface, 
//					momentumPoleND
//					));
//		}
//			
//			// respect TO another pole	
//			Amount<Length> momentumPole;
//			for (int j=0; j<_wingMomentumPole.size(); j++){
//			momentumPole = Amount.valueOf( 
//					_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
//					_wingMomentumPole.get(j)* 
//					_wingMAC.doubleValue(SI.METER), SI.METER);
//			
//			if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){	
//				_wingMomentCoefficients.add(
//						MomentCalc.calcCMLiftingSurfaceWithIntegral(
//						theNasaBlackwellCalculatorMachActualWing, 
//						_alphasWing, 
//						_wingMAC,
//						_wingYDistribution, 
//						_wingCl0Distribution, 
//						_wingClAlphaDistributionDeg, 
//						_wingCmACDistribution, 
//						_wingXACDistribution, 
//						_wingChordsDistribution, 
//						_wingXleDistribution, 
//						_wingCLAirfoilsDistributionFinal, 
//						_alphasWing,
//						_wingSurface, 
//						momentumPole
//						));
//			}
//			
//			if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){	
//				_wingMomentCoefficients.add(
//						MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
//						theNasaBlackwellCalculatorMachActualWing, 
//						_alphasWing, 
//						_wingMAC,
//						_wingYDistribution, 
//						_wingCl0Distribution, 
//						_wingClAlphaDistributionDeg, 
//						_wingCLMomentAirfoilOutput,
//						_wingCMMomentAirfoilOutput,
//						_wingXACDistribution, 
//						_wingChordsDistribution, 
//						_wingXleDistribution, 
//						_wingCLAirfoilsDistributionFinal, 
//						_alphasWing,
//						_wingSurface, 
//						momentumPole
//						));
//			}
//			}
//			
//			// respect TO final pole	
//			Amount<Length> momentumPoleFinal;
//			momentumPoleFinal = Amount.valueOf( 
//					_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
//					_wingFinalMomentumPole* 
//					_wingMAC.doubleValue(SI.METER), SI.METER);
//
//			_wingMomentCoefficientConstant = 
//						MomentCalc.calcCMLiftingSurfaceWithIntegral(
//						theNasaBlackwellCalculatorMachActualWing, 
//						_alphasWing, 
//						_wingMAC,
//						_wingYDistribution, 
//						_wingCl0Distribution, 
//						_wingClAlphaDistributionDeg, 
//						_wingCmACDistribution, 
//						_wingXACDistribution, 
//						_wingChordsDistribution, 
//						_wingXleDistribution, 
//						_wingCLAirfoilsDistributionFinal, 
//						_alphasWing,
//						_wingSurface, 
//						momentumPoleFinal
//						);
//			
//			if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){
//				_wingMomentCoefficientFinal = _wingMomentCoefficientConstant;
//			}
//			
//			if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
//			_wingMomentCoefficientFinalACVariable =
//					MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
//							theNasaBlackwellCalculatorMachActualWing, 
//							_alphasWing, 
//							_wingMAC,
//							_wingYDistribution, 
//							_wingCl0Distribution, 
//							_wingClAlphaDistributionDeg, 
//							_wingCLMomentAirfoilOutput,
//							_wingCMMomentAirfoilOutput, 
//							_wingXACDistribution, 
//							_wingChordsDistribution, 
//							_wingXleDistribution, 
//							_wingCLAirfoilsDistributionFinal, 
//							_alphasWing,
//							_wingSurface, 
//							momentumPoleFinal
//							);
//			_wingMomentCoefficientFinal = _wingMomentCoefficientFinalACVariable;
//			}	
//					
//	
	
		// respect TO another pole	
		Amount<Length> momentumPole;
		for (int j=0; j<_wingMomentumPole.size(); j++){
		momentumPole = Amount.valueOf( 
				_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_wingMomentumPole.get(j)* 
				_wingMAC.doubleValue(SI.METER), SI.METER);
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){	
			_wingMomentCoefficients.add(
					MomentCalc.calcCMLiftingSurfaceWithIntegral(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphasWing, 
					_wingMAC,
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCmC4Distribution, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingSurface, 
					momentumPole
					));
		}
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){	
			_wingMomentCoefficients.add(
					MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphasWing, 
					_wingMAC,
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCLMomentAirfoilOutput,
					_wingCMMomentAirfoilOutput, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingSurface, 
					momentumPole
					));
		}
		}
		
		// respect TO final pole	
		Amount<Length> momentumPoleFinal;
		momentumPoleFinal = Amount.valueOf( 
				_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_wingFinalMomentumPole* 
				_wingMAC.doubleValue(SI.METER), SI.METER);

		_wingMomentCoefficientConstant = 
					MomentCalc.calcCMLiftingSurfaceWithIntegral(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphasWing, 
					_wingMAC,
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCmC4Distribution, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingSurface, 
					momentumPoleFinal
					);
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){
			_wingMomentCoefficientFinal = _wingMomentCoefficientConstant;
		}
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
		_wingMomentCoefficientFinalACVariable =
				MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
						theNasaBlackwellCalculatorMachActualWing, 
						_alphasWing, 
						_wingMAC,
						_wingYDistribution, 
						_wingCl0Distribution, 
						_wingClAlphaDistributionDeg, 
						_wingCLMomentAirfoilOutput,
						_wingCMMomentAirfoilOutput, 
						_wingChordsDistribution, 
						_wingXleDistribution, 
						_wingCLAirfoilsDistributionFinal, 
						_alphasWing,
						_wingSurface, 
						momentumPoleFinal
						);
		_wingMomentCoefficientFinal = _wingMomentCoefficientFinalACVariable;
		}	
				

		if( _theCondition == ConditionEnum.TAKE_OFF|| _theCondition== ConditionEnum.LANDING){
			for (int i=0; i<_numberOfAlphasBody; i++){
				_wingMomentCoefficientFinal.set(i,
						_wingMomentCoefficientFinal.get(i)+_deltaCMc4);
			}
		}
		
	}	
	
	
	public void calculateHtailMomentCharacterstics() throws InstantiationException, IllegalAccessException{
		
		// respect TO AC DE YOUNG HARPER	
		Amount<Length> hTailMomentumPoleYH = Amount.valueOf( 
				_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_hTailXACMACpercent.get(MethodEnum.DEYOUNG_HARPER)* 
				_hTailMAC.doubleValue(SI.METER), SI.METER);

		_hTailMomentCoefficientAC.put(MethodEnum.DEYOUNG_HARPER, 
				MomentCalc.calcCMLiftingSurfaceWithIntegral(
						theNasaBlackwellCalculatorMachActualHTail, 
						_alphasTail, 
						_hTailMAC,
						_hTailYDistribution, 
						_hTailCl0Distribution, 
						_hTailClAlphaistributionDeg, 
						_hTailCmC4Distribution, 
						_hTailChordsDistribution, 
						_hTailXleDistribution, 
						_hTailCLAirfoilsDistributionFinal, 
						_alphasTail,
						_hTailSurface, 
						hTailMomentumPoleYH
						));

		// respect TO AC NAPOLITANO DATCOM	
	   hTailMomentumPoleYH = Amount.valueOf( 
				_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_hTailXACMACpercent.get(MethodEnum.NAPOLITANO_DATCOM)* 
				_hTailMAC.doubleValue(SI.METER), SI.METER);

	   _hTailMomentCoefficientAC.put(MethodEnum.NAPOLITANO_DATCOM, 
				MomentCalc.calcCMLiftingSurfaceWithIntegral(
						theNasaBlackwellCalculatorMachActualHTail, 
						_alphasTail, 
						_hTailMAC,
						_hTailYDistribution, 
						_hTailCl0Distribution, 
						_hTailClAlphaistributionDeg, 
						_hTailCmC4Distribution, 
						_hTailChordsDistribution, 
						_hTailXleDistribution, 
						_hTailCLAirfoilsDistributionFinal, 
						_alphasTail,
						_hTailSurface, 
						hTailMomentumPoleYH
						));


		// respect TO another pole	
		Amount<Length> momentumPole;
		for (int j=0; j<_hTailMomentumPole.size(); j++){
			momentumPole = Amount.valueOf( 
					_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
					_hTailMomentumPole.get(j)* 
					_hTailMAC.doubleValue(SI.METER), SI.METER);

			_hTailMomentCoefficients.add(
					MomentCalc.calcCMLiftingSurfaceWithIntegral(
							theNasaBlackwellCalculatorMachActualHTail, 
							_alphasTail, 
							_hTailMAC,
							_hTailYDistribution, 
							_hTailCl0Distribution, 
							_hTailClAlphaistributionDeg, 
							_hTailCmC4Distribution, 
							_hTailChordsDistribution, 
							_hTailXleDistribution, 
							_hTailCLAirfoilsDistributionFinal, 
							_alphasTail,
							_hTailSurface, 
							hTailMomentumPoleYH
							));
			
		// respect TO FINAL POLE
			
			Amount<Length> _hTailPole;
			_hTailPole = Amount.valueOf( 
					_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) +
					_hTailFinalMomentumPole* 
					_hTailMAC.doubleValue(SI.METER), SI.METER);


			_hTailMomentCoefficientFinal=
					MomentCalc.calcCMLiftingSurfaceWithIntegral(
							theNasaBlackwellCalculatorMachActualHTail, 
							_alphasTail, 
							_hTailMAC,
							_hTailYDistribution, 
							_hTailCl0Distribution, 
							_hTailClAlphaistributionDeg, 
							_hTailCmC4Distribution, 
							_hTailChordsDistribution, 
							_hTailXleDistribution, 
							_hTailCLAirfoilsDistributionFinal, 
							_alphasTail,
							_hTailSurface, 
								_hTailPole
								);
			
			for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){ 
		
				
				theStabilityCalculator.calculateElevatorEffects(this, _anglesOfElevatorDeflection.get(i));
				Double [] cmTemp = new Double[_numberOfAlphasBody];
				
				for (int ii=0; ii<_numberOfAlphasBody; ii++){
					cmTemp[ii] = _hTailMomentCoefficientFinal.get(ii)+ _deltaCMc4Elevator;
				}
				
				_hTailMomentCoefficientFinalElevator.put(
						_anglesOfElevatorDeflection.get(i),
						MyArrayUtils.convertDoubleArrayToListDouble(cmTemp)
						);
			}
			
			
		}
	}
	public void calculateFuselageMomentCharacterstics(){
		//due to moment
		
		if (_fuselageMomentMethod ==  MethodEnum.FUSDES){

			// fusdes
			double fusSurfRatio = _fuselageFrontSurface.doubleValue(SI.SQUARE_METRE)/
					_wingSurface.doubleValue(SI.SQUARE_METRE);

			fusDesDatabaseReader.runAnalysis(
					_fuselageNoseFinessRatio,
					_fuselageWindshieldAngle.doubleValue(NonSI.DEGREE_ANGLE), 
					_fuselageFinessRatio, 
					_fuselageTailFinessRatio, 
					_fuselageUpSweepAngle.doubleValue(NonSI.DEGREE_ANGLE),
					_fuselageXPercentPositionPole);

//			System.out.println(" CMO " + fusDesDatabaseReader.getCM0FR());
//			System.out.println(" CMn " + fusDesDatabaseReader.getdCMn());
//			System.out.println(" CMt " + fusDesDatabaseReader.getdCMt());
//			System.out.println(" fus surface " + fusSurfRatio);
//			System.out.println(" fus diam  " + _fuselageDiameter);
//			System.out.println(" wing mac " + _wingMAC);

			_fuselageCM0.put(MethodEnum.FUSDES,MomentCalc.calcCM0Fuselage(
					fusDesDatabaseReader.getCM0FR(),
					fusDesDatabaseReader.getdCMn(),
					fusDesDatabaseReader.getdCMt())* 
					fusSurfRatio*_fuselageDiameter.doubleValue(SI.METER)/
					_wingMAC.doubleValue(SI.METRE));

			_fuselageCMAlpha.put(MethodEnum.FUSDES, MomentCalc.calcCMAlphaFuselage(
					fusDesDatabaseReader.getCMaFR(),
					fusDesDatabaseReader.getdCMan(),
					fusDesDatabaseReader.getdCMat())* 
					fusSurfRatio*_fuselageDiameter.doubleValue(SI.METER)/
					_wingMAC.doubleValue(SI.METRE));

			// Array 

			for (int i=0; i<_numberOfAlphasBody; i++){
				_fuselageMomentCoefficient.add(i,
						_fuselageCMAlpha.get(MethodEnum.FUSDES)*(
								_alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE)) + _fuselageCM0.get(MethodEnum.FUSDES));
			}
		}

		// INPUT method--------------
		if (_fuselageMomentMethod ==  MethodEnum.INPUT){


			_fuselageCM0.put(MethodEnum.INPUT, _cM0fuselage);
			_fuselageCMAlpha.put(MethodEnum.INPUT, _cMalphafuselage);


			// Array 

			for (int i=0; i<_numberOfAlphasBody; i++){
				_fuselageMomentCoefficient.add(i,
						_fuselageCMAlpha.get(MethodEnum.INPUT)*(
								_alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE)) + _fuselageCM0.get(MethodEnum.INPUT));
			}
		}

	}
	public void calculateMomentdueToLandingGear(){
		_landingGearArm = Amount.valueOf(
				_zLandingGear.doubleValue(SI.METER)+_zCGAircraft.doubleValue(SI.METER),
				SI.METER);
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			_landingGearMomentDueToDrag.add(i, 
					_cDLandingGear * (_landingGearArm.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)) );	
		}
	}
	
	public void calculateTotalLiftCoefficient(){
		List<Double> _clTotalList;
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){
			_clTotalList = new ArrayList<>();
			for (int ii=0; ii<getNumberOfAlphasBody(); ii++){
				_clTotalList.add(_fuselagewingliftCoefficient3DCurve[ii] + (
						_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
						_dynamicPressureRatio*
						(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE))));
			}
			_totalLiftCoefficient.put(_anglesOfElevatorDeflection.get(i),
					_clTotalList
					);
		}
	}
		
	public void calculateMomentCoefficientRespectToCG(){ // CM CG
	
		//WING------------------------------------
		
		//normal force array

		for (int i=0; i<_numberOfAlphasBody; i++){
//			_wingNormalCoefficient.add(
//					i,
//					_wingliftCoefficient3DCurveCONDITION[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN))+
//					_wingDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)));
			
			_wingNormalCoefficient.add(
					i,
					_fuselagewingliftCoefficient3DCurve[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN))+
					_wingDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)));

			
			_wingHorizontalCoefficient.add(
					i,
					_wingDragCoefficient3DCurve.get(i)*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)) - 
					_wingliftCoefficient3DCurveCONDITION[i]*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN))
					);	
		}
		
		// moment 
		
		
		_wingHorizontalDistanceACtoCG = Amount.valueOf(
				_xCGAircraft.doubleValue(SI.METER) - (
						_xApexWing.doubleValue(SI.METER) +
						_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
						(_wingFinalMomentumPole)*_wingMAC.doubleValue(SI.METER)
						), 
				SI.METER);
		
		double zVerticalDistance;

			//zVerticalDistance = _zApexWing.doubleValue(SI.METER)+_wingZACMAC.doubleValue(SI.METER) - _zCGAircraft.doubleValue(SI.METER);
		
			zVerticalDistance = _zApexWing.doubleValue(SI.METER) - _zCGAircraft.doubleValue(SI.METER);
			
		_wingVerticalDistranceACtoCG = Amount.valueOf(zVerticalDistance, SI.METER);
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			_wingMomentCoefficientPendular.add(i,
					(_wingNormalCoefficient.get(i)*(_wingHorizontalDistanceACtoCG.doubleValue(SI.METER)/ _wingMAC.doubleValue(SI.METER))) + 
					(_wingHorizontalCoefficient.get(i) * (_wingVerticalDistranceACtoCG.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)))+
					_wingMomentCoefficientFinal.get(i)
					);
		}
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			_wingMomentCoefficientNOPendular.add(i,
					(_wingNormalCoefficient.get(i)*(_wingHorizontalDistanceACtoCG.doubleValue(SI.METER)/ _wingMAC.doubleValue(SI.METER))) + 
					_wingMomentCoefficientFinal.get(i)
					);
		}
		
		//HORIZONTAL TAIL------------------------------------
		
		// forces
			for (int i=0; i<_numberOfAlphasBody; i++){
				_hTailNormalCoefficientDownwashConstant.add(
						i,
						_hTailliftCoefficient3DCurve[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantSlingerland.get(i).doubleValue(SI.RADIAN))+
						_hTailDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantSlingerland.get(i).doubleValue(SI.RADIAN)));

				_hTailHorizontalCoefficientDownwashConstant.add(
						i,
						_hTailDragCoefficient3DCurve.get(i)*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantRoskam.get(i).doubleValue(SI.RADIAN)) - 
						_hTailliftCoefficient3DCurve[i]*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantRoskam.get(i).doubleValue(SI.RADIAN))
						);	
			}
		
			for (int i=0; i<_numberOfAlphasBody; i++){
				_hTailNormalCoefficient.add(
						i,
						_hTailliftCoefficient3DCurve[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleVariableSlingerland.get(i).doubleValue(SI.RADIAN))+
						_hTailDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleVariableSlingerland.get(i).doubleValue(SI.RADIAN)));

				_hTailHorizontalCoefficient.add(
						i,
						_hTailDragCoefficient3DCurve.get(i)*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleVariableSlingerland.get(i).doubleValue(SI.RADIAN)) - 
						_hTailliftCoefficient3DCurve[i]*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleVariableSlingerland.get(i).doubleValue(SI.RADIAN))
						);	
			}


			//distance 

			_hTailHorizontalDistanceACtoCG = Amount.valueOf(
					_xCGAircraft.doubleValue(SI.METER) - (
							_xApexHTail.doubleValue(SI.METER) +
							_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
							(_hTailFinalMomentumPole)*_hTailMAC.doubleValue(SI.METER)
							), 
					SI.METER);

			double zVerticalDistanceHtail;

			zVerticalDistanceHtail = _zApexHTail.doubleValue(SI.METER) - _zCGAircraft.doubleValue(SI.METER);

			_hTailVerticalDistranceACtoCG = Amount.valueOf(zVerticalDistanceHtail, SI.METER);

			if(_downwashConstant == Boolean.FALSE){
				for (int i=0; i<_numberOfAlphasBody; i++){
					_hTailMomentCoefficientPendular.add(i,
							(_hTailNormalCoefficient.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE))*(_hTailHorizontalDistanceACtoCG.doubleValue(SI.METER)/ _wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)) 
							+ (_hTailHorizontalCoefficient.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)) * (_hTailVerticalDistranceACtoCG.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio))			
							+ _hTailMomentCoefficientFinal.get(i)*
							(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
									_wingSurface.doubleValue(SI.SQUARE_METRE))*
							(_hTailMAC.doubleValue(SI.METER)/
									_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)
							);
				}
			}
		
			if(_downwashConstant == Boolean.TRUE){
				for (int i=0; i<_numberOfAlphasBody; i++){
					_hTailMomentCoefficientPendular.add(i,
							(_hTailNormalCoefficientDownwashConstant.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE))*(_hTailHorizontalDistanceACtoCG.doubleValue(SI.METER)/ _wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)) 
							+ (_hTailHorizontalCoefficientDownwashConstant.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)) * (_hTailVerticalDistranceACtoCG.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio))
							+ _hTailMomentCoefficientFinal.get(i)*
							(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
									_wingSurface.doubleValue(SI.SQUARE_METRE))*
							(_hTailMAC.doubleValue(SI.METER)/
									_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)
							);
				}
			}


			//FUSELAGE-----------------------------------
			
			// Moment due to drag
			
			for (int i=0; i<_numberOfAlphasBody; i++){
				_fuselageMomentCoefficientdueToDrag.add(i, 
						cdDistributionFuselageFinal.get(i) * (-_zCGAircraft.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)) );	
			}
			
			//PROPELLER------------------------------------
		
			//TOTAL------------------------------------
			// delta e =0
			if(_theCondition == ConditionEnum.CRUISE){
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
			_totalMomentCoefficientPendular.add(ii,
					_wingMomentCoefficientPendular.get(ii) +
					_fuselageMomentCoefficient.get(ii) +
					_fuselageMomentCoefficientdueToDrag.get(ii)+
					_hTailMomentCoefficientPendular.get(ii));
			}
			}
			
			// delta e =0
			if(_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
			_totalMomentCoefficientPendular.add(ii,
					_wingMomentCoefficientPendular.get(ii) +
					_fuselageMomentCoefficient.get(ii) +
					_fuselageMomentCoefficientdueToDrag.get(ii)+
					_hTailMomentCoefficientPendular.get(ii)+
			_landingGearMomentDueToDrag.get(ii));
			}
			}
	
			
			//Whith elevator deflection------------------------------------------------
			
			List<Double> normalCoefficients, horizontalCoefficients, momentCoefficentHTail, momentCoefficientTotal ;
			
			//normal and horizontal forces
			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				normalCoefficients = new ArrayList<>();
				horizontalCoefficients = new ArrayList<>();
				momentCoefficentHTail = new ArrayList<>();
				momentCoefficientTotal = new ArrayList<>();
			
//				if(_downwashConstant == Boolean.TRUE){
//					for (int ii=0; ii<_numberOfAlphasBody; ii++){
//						normalCoefficients.add(ii,
//								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
//								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN))+
//								_hTailDragCoefficient3DCurve.get(ii)*
//								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)));
//
//						horizontalCoefficients.add(ii, 
//								_hTailDragCoefficient3DCurve.get(i)*
//								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)) - 
//								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
//								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)));
//					}
//				}
//				if(_downwashConstant == Boolean.FALSE){
//					for (int ii=0; ii<_numberOfAlphasBody; ii++){
//						normalCoefficients.add(ii, 
//								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
//								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN))+
//								_hTailDragCoefficient3DCurve.get(ii)*
//								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)));
//
//						horizontalCoefficients.add(ii,
//								_hTailDragCoefficient3DCurve.get(ii)*
//								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)) - 
//								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
//								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
//										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)));
//					}
//				}
//				
				
				if(_downwashConstant == Boolean.TRUE){
					for (int ii=0; ii<_numberOfAlphasBody; ii++){
						normalCoefficients.add(ii,
								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN))+
								_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)));

						horizontalCoefficients.add(ii, 
								_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)) - 
								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)));
					}
				}
				if(_downwashConstant == Boolean.FALSE){
					for (int ii=0; ii<_numberOfAlphasBody; ii++){
						normalCoefficients.add(ii, 
								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN))+
								_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)));

						horizontalCoefficients.add(ii,
								_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)) - 
								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)));
					}
				}
				_hTailNormalCoefficientDeltaE.put(_anglesOfElevatorDeflection.get(i),normalCoefficients);
				_hTailHorizontalCoefficientDeltaE.put(_anglesOfElevatorDeflection.get(i),horizontalCoefficients);	

				//moment
				
				for (int ii=0; ii<_numberOfAlphasBody; ii++){
				momentCoefficentHTail.add(ii,
						(_hTailNormalCoefficientDeltaE.get(_anglesOfElevatorDeflection.get(i)).get(ii)*
								(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
										_wingSurface.doubleValue(SI.SQUARE_METRE))*
								(_hTailHorizontalDistanceACtoCG.doubleValue(SI.METER)/
										_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)) 
							+ (_hTailHorizontalCoefficientDeltaE.get(_anglesOfElevatorDeflection.get(i)).get(ii)*
									(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
											_wingSurface.doubleValue(SI.SQUARE_METRE)) * 
									(_hTailVerticalDistranceACtoCG.doubleValue(SI.METER)/
											_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio))+
							_hTailMomentCoefficientFinalElevator.get(_anglesOfElevatorDeflection.get(i)).get(ii)*
							(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
									_wingSurface.doubleValue(SI.SQUARE_METRE))*
							(_hTailMAC.doubleValue(SI.METER)/
									_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)
							);

				if(_theCondition== ConditionEnum.CRUISE){
					momentCoefficientTotal.add(ii,
							_wingMomentCoefficientPendular.get(ii) +
							_fuselageMomentCoefficient.get(ii) +
							_fuselageMomentCoefficientdueToDrag.get(ii)+
							momentCoefficentHTail.get(ii));
				}
				
				if(_theCondition== ConditionEnum.TAKE_OFF || _theCondition== ConditionEnum.LANDING){
					momentCoefficientTotal.add(ii,
							_wingMomentCoefficientPendular.get(ii) +
							_fuselageMomentCoefficient.get(ii) +
							_fuselageMomentCoefficientdueToDrag.get(ii)+
							_landingGearMomentDueToDrag.get(ii)+
							momentCoefficentHTail.get(ii));
				}
				
				
				
				}
		
			 
				_hTailMomentCoefficientPendularDeltaE.put(
						_anglesOfElevatorDeflection.get(i),
						momentCoefficentHTail
						);
				_totalMomentCoefficientPendularDeltaE.put(
						_anglesOfElevatorDeflection.get(i),
						momentCoefficientTotal
						);
				
			}
	}
	public void calculateHTailEquilibriumLiftCoefficient(){
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			
			_hTailEquilibriumLiftCoefficient.add(i,
					(-_wingMomentCoefficientPendular.get(i)-_fuselageMomentCoefficient.get(i)-_fuselageMomentCoefficientdueToDrag.get(i))*
					(_wingSurface.doubleValue(SI.SQUARE_METRE)/_hTailSurface.doubleValue(SI.SQUARE_METRE)) *
					(_wingMAC.doubleValue(SI.METER)/_hTailHorizontalDistanceACtoCG.doubleValue(SI.METER))*
					(1/_dynamicPressureRatio)
					);
	
		}
	}
	public void calculateTotalEquilibriumLiftCoefficient(){
		for (int i=0; i<_numberOfAlphasBody; i++){
			_totalEquilibriumLiftCoefficient.add(i,
					_wingliftCoefficient3DCurveCONDITION[i] + _hTailEquilibriumLiftCoefficient.get(i)*_dynamicPressureRatio*
					(_hTailSurface.doubleValue(SI.SQUARE_METRE)/
							_wingSurface.doubleValue(SI.SQUARE_METRE))
					);
			}
	}
	
	public void calculateDeltaeEquilibrium() throws InstantiationException, IllegalAccessException{
		
		// create array delta e
		
		List<Amount<Angle>> deltaEforDeltaEEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(-45, 10, numberOfIterationforDeltaE), NonSI.DEGREE_ANGLE);

		double tauActual, clZero, clMax, clStar;
		Amount<Angle> alphaZeroLift, alphaStall, alphaStar;
		
		List<List<Double>> clDeltaEFinalList = new ArrayList<>();
		
		for (int i=0; i<deltaEforDeltaEEquilibrium.size(); i++){
			// tau-------------
			theStabilityCalculator.calculateElevatorEffects(
					this,
					deltaEforDeltaEEquilibrium.get(i));

			tauActual = 
					LiftCalc.calculateTauIndexElevator(
							_elevatorCfC, 
							_hTailAspectRatio,
							highLiftDatabaseReader, 
							aeroDatabaseReader, 
							deltaEforDeltaEEquilibrium.get(i)
							);
			
			// values------------

			//------------------------------------------------------
			// ALPHA ZERO LIFT HIGH LIFT
			alphaZeroLift = 
					Amount.valueOf(
							_hTailAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE) - 
							(tauActual*deltaEforDeltaEEquilibrium.get(i).doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE);

			//------------------------------------------------------
			// CL ZERO HIGH LIFT
			clZero = 
					-_hTailcLAlphaDeg *
					alphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE);

			//------------------------------------------------------
			// CL MAX HIGH LIFT

			clMax =
					_hTailcLMax + _deltaCLMaxElevator.get(deltaEforDeltaEEquilibrium.get(i));


			//------------------------------------------------------
			// ALPHA STALL HIGH LIFT
			double deltaYPercent = aeroDatabaseReader
					.getDeltaYvsThickness(
							_hTailMaxThicknessMeanAirfoil,
							_hTailMeanAirfoilFamily
							);

			Amount<Angle> deltaAlpha = Amount.valueOf(
					aeroDatabaseReader
					.getDAlphaVsLambdaLEVsDy(
							_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
							deltaYPercent
							),
					NonSI.DEGREE_ANGLE);

			alphaStall = 
					Amount.valueOf((((clMax) - 
							clZero) /
							_hTailcLAlphaDeg) + deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE),
							NonSI.DEGREE_ANGLE);

			//------------------------------------------------------
			// ALPHA STAR HIGH LIFT		
			alphaStar = 
					Amount.valueOf(_hTailalphaStar.doubleValue(NonSI.DEGREE_ANGLE)-
							(tauActual) * deltaEforDeltaEEquilibrium.get(i).doubleValue(NonSI.DEGREE_ANGLE), 
							NonSI.DEGREE_ANGLE);

			//------------------------------------------------------
			// CL STAR HIGH LIFT
			clStar = 
					_hTailcLAlphaDeg * 
					alphaStar.doubleValue(NonSI.DEGREE_ANGLE)+
					clZero; 
			
			
			// curve----------------
			_clMapForDeltaeElevator.put(
					deltaEforDeltaEEquilibrium.get(i),
					LiftCalc.calculateCLvsAlphaArray(
							clZero,
							clMax,
							alphaStar,
							alphaStall,
							Amount.valueOf(_hTailcLAlphaDeg, NonSI.DEGREE_ANGLE.inverse()),
							MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasTail)
							));
		}
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			Double [] clTemp = new Double[deltaEforDeltaEEquilibrium.size()];
			for (int ii = 0; ii<deltaEforDeltaEEquilibrium.size(); ii++){
				clTemp[ii] = _clMapForDeltaeElevator.get(deltaEforDeltaEEquilibrium.get(ii))[i];
			}
			clDeltaEFinalList.add(i, MyArrayUtils.convertDoubleArrayToListDouble(clTemp));
		
		
		_deltaEEquilibrium.add(i, Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(clDeltaEFinalList.get(i))),
						MyArrayUtils.convertListOfAmountTodoubleArray(deltaEforDeltaEEquilibrium),
						_hTailEquilibriumLiftCoefficient.get(i))
				, 
				NonSI.DEGREE_ANGLE));
		
		}
	}
	
	public void calculateTrimmedPolar() throws InstantiationException, IllegalAccessException{
		List<Amount<Angle>> deltaEforCDEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(-45, 10, numberOfIterationforDeltaE), NonSI.DEGREE_ANGLE);
		
		Map <Amount<Angle>, Double[]> _cdMapForDeltaeElevator = new HashMap<Amount<Angle>, Double[]>();
		List<List<Double>> cdDeltaEFinalList = new ArrayList<>();
		
		// build n curves of total drag
		
		
		for (int i=0; i<deltaEforCDEquilibrium.size(); i++){
			Double[] _cdTotalList = new Double [getNumberOfAlphasBody()];
			
			//-----------
			List<Double> inducedDragTemp = new ArrayList<>();
			inducedDragTemp = DragCalc.calculateCDInducedParabolic(
					    _clMapForDeltaeElevator.get(deltaEforCDEquilibrium.get(i)),
						_hTailAspectRatio,
						_hTailOswaldFactor);
						
			theStabilityCalculator.calculateElevatorEffects(this, deltaEforCDEquilibrium.get(i));
			Double [] cdTemp = new Double[_numberOfAlphasBody];
			
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
				
				
				cdTemp[ii] = _hTailParasiteDragCoefficientDistribution.get(ii)+
						inducedDragTemp.get(ii) 
					+ _deltaCD0Elevator.get(deltaEforCDEquilibrium.get(i));
			}
			//---------

			
			for (int ii=0; ii<getNumberOfAlphasBody(); ii++){
				_cdTotalList[ii]=
						 _wingDragCoefficient3DCurve.get(ii) + (
								cdTemp[ii]*
						_dynamicPressureRatio*
						(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)))+
						cdDistributionFuselageFinal.get(ii)+
						_deltaCD0Miscellaneus
						;
			}
			_cdMapForDeltaeElevator.put(deltaEforCDEquilibrium.get(i),
					_cdTotalList
					);
		}
		
		// build  cd at alpha array
		
		for (int i=0; i<_numberOfAlphasBody; i++){
			Double [] cdTemp = new Double[deltaEforCDEquilibrium.size()];
			for (int ii = 0; ii<deltaEforCDEquilibrium.size(); ii++){
				cdTemp[ii] = _cdMapForDeltaeElevator.get(deltaEforCDEquilibrium.get(ii))[i];
			}
			cdDeltaEFinalList.add(i, MyArrayUtils.convertDoubleArrayToListDouble(cdTemp));
		}
		// build trim polar curve
		for (int i=0; i< _deltaEEquilibrium.size(); i++){
		_totalTrimDrag.add(i,
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(deltaEforCDEquilibrium),
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfDoubleToDoubleArray(cdDeltaEFinalList.get(i))),
						_deltaEEquilibrium.get(i).doubleValue(NonSI.DEGREE_ANGLE)));
		}
	}
	// DISTRIBUTIONS----------------------------------------------------------
	
	
	public void calculateDistributions(){
		
		// initialize alpha array

		if(!_alphaWingForDistribution.contains(_wingAlphaZeroLiftCONDITION))
			_alphaWingForDistribution.add(_alphaWingForDistribution.size(), _wingAlphaZeroLiftCONDITION);
		if(!_alphaHorizontalTailForDistribution.contains(_hTailAlphaZeroLift))
			_alphaHorizontalTailForDistribution.add(_alphaHorizontalTailForDistribution.size(), _hTailAlphaZeroLift);

		int alphaWingSize = _alphaWingForDistribution.size();
		int alphaTailSize = _alphaHorizontalTailForDistribution.size();
		// cl 

		for (int i=0; i<alphaWingSize; i++){
			theNasaBlackwellCalculatorMachActualWing.calculate(_alphaWingForDistribution.get(i));
			_clWingDistribution.add(i, 
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									theNasaBlackwellCalculatorMachActualWing.getClTotalDistribution().toArray())));
		}

		for (int i=0; i<alphaTailSize; i++){
			theNasaBlackwellCalculatorMachActualHTail.calculate(_alphaHorizontalTailForDistribution.get(i));
			if (_alphaHorizontalTailForDistribution.get(i).equals(_hTailAlphaZeroLift)){
			List<Double> zeros = new ArrayList<>();
			for (int j=0; j<_hTailNumberOfPointSemiSpanWise; j++)
				zeros.add(j, 0.0);
			_clHtailDistribution.add(i, zeros);}
			else{
			_clHtailDistribution.add(i, 
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.convertFromDoubleToPrimitive(
									theNasaBlackwellCalculatorMachActualHTail.getClTotalDistribution().toArray())));
			}
		}

		
		// cm
		
		Amount<Length> momentumPoleYH = Amount.valueOf( 
				_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_wingFinalMomentumPole* 
				_wingMAC.doubleValue(SI.METER), SI.METER);
	
		
		Amount<Length> hTailomentumPoleYH = Amount.valueOf( 
				_hTailMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_hTailFinalMomentumPole* 
				_hTailMAC.doubleValue(SI.METER), SI.METER);
	
		for (int i=0; i<alphaWingSize; i++){
			_cMWingDistribution.add(i,
					MomentCalc.calcCmDistributionLiftingSurfaceWithIntegral(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphaWingForDistribution.get(i),
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCmC4Distribution, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					momentumPoleYH
					));		
		}
		
		for (int i=0; i<alphaTailSize; i++){
			_cMHTailDistribution.add(i,
					MomentCalc.calcCmDistributionLiftingSurfaceWithIntegral(
					theNasaBlackwellCalculatorMachActualHTail, 
					_alphaHorizontalTailForDistribution.get(i),
					_hTailYDistribution, 
					_hTailCl0Distribution, 
					_hTailClAlphaistributionDeg, 
					_hTailCmC4Distribution, 
					_hTailChordsDistribution, 
					_hTailXleDistribution, 
					_hTailCLAirfoilsDistributionFinal, 
					_alphasTail,
					hTailomentumPoleYH
					));		
		}

		// induced angle of Attack

		for (int i=0; i<alphaWingSize; i++){
			_alphaIWingDistribution.add(i, 
					AerodynamicCalc.calculateInducedAngleOfAttackDistribution(
							_alphaWingForDistribution.get(i), 
							theNasaBlackwellCalculatorMachActualWing, 
							_altitude, 
							_machCurrent, 
							_wingNumberOfPointSemiSpanWise
							));
		}
		
		for (int i=0; i<alphaTailSize; i++){
			_alphaIHtailDistribution.add(i, 
					AerodynamicCalc.calculateInducedAngleOfAttackDistribution(
							_alphaHorizontalTailForDistribution.get(i), 
							theNasaBlackwellCalculatorMachActualHTail, 
							_altitude, 
							_machCurrent, 
							_hTailNumberOfPointSemiSpanWise
							));
		}
		
		// center of pressure 
		for (int i=0; i<alphaWingSize; i++){
			_centerOfPressureWingDistribution.add(i, 
					AerodynamicCalc.calcCenterOfPressureDistribution(
							theNasaBlackwellCalculatorMachActualWing,  
							_alphaWingForDistribution.get(i),
							_wingCl0Distribution, 
							_wingClAlphaDistributionDeg, 
							_wingCLMomentAirfoilOutput,
							_wingCMMomentAirfoilOutput,
							_wingXACDistribution, 
							_wingCLAirfoilsDistributionFinal, 
							_alphasWing));
		}
		
//		for (int i=0; i<alphaTailSize; i++){
//			_centerOfPressurehTailDistribution.add(i, 
//					AerodynamicCalc.calcCenterOfPressureDistribution(
//							theNasaBlackwellCalculatorMachActualHTail,  
//							_alphaHorizontalTailForDistribution.get(i), 
//							_hTailCl0Distribution, 
//							_hTailClAlphaistributionDeg, 
//							_hTailCmACDistribution, 
//							_hTailXACDistribution, 
//							_hTailCLAirfoilsDistributionFinal, 
//							_alphasTail));
//			}
	}
	
	public void calculateFlappedCurve(){

		// CL0 -------------
		theNasaBlackwellCalculatorMachActualWing.calculate(Amount.valueOf(0.0, SI.RADIAN));
		_clZeroFlapped = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		
		// CL ALPHA--------------
		theNasaBlackwellCalculatorMachActualWing.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachActual = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		theNasaBlackwellCalculatorMachActualWing.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachActual = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		_clAlphaRadFlapped = (clTwoMachActual-clOneMachActual)/toRadians(4);
		_clAlphaDegFlapped = (clTwoMachActual-clOneMachActual)/(4);
		_wingclAlphaFlapped = Amount.valueOf( _clAlphaRadFlapped , SI.RADIAN.inverse());
		
		// DELTA ALPHA 
		double deltaYPercent = aeroDatabaseReader
				.getDeltaYvsThickness(
						_wingMaxThicknessMeanAirfoil,
						_wingMeanAirfoilFamily
						);

		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						_wingSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
				NonSI.DEGREE_ANGLE);
		
		// ALPHA STALL LINEAR-----------------------
		
		_alphaStallFlapped =  Amount.valueOf(13, NonSI.DEGREE_ANGLE);
		_alphaStallLinearFlapped = Amount.valueOf(
				_alphaStallFlapped.doubleValue(NonSI.DEGREE_ANGLE) - deltaAlpha.doubleValue(NonSI.DEGREE_ANGLE), 
				NonSI.DEGREE_ANGLE);
		
		// CL MAX----------------------
		
		theNasaBlackwellCalculatorMachActualWing.calculate(_alphaStallLinearFlapped);
		 _clMaxDistributionFlapped = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoubleToPrimitive(
				 theNasaBlackwellCalculatorMachActualWing.getClTotalDistribution().toArray()));
		_clMaxFlapped = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();
		
		// ALPHA STAR----------------------
		
		_alphaStarFlapped=
				Amount.valueOf(
						_alphaStallFlapped.doubleValue(NonSI.DEGREE_ANGLE)
						-(_wingalphaStall.doubleValue(NonSI.DEGREE_ANGLE)
								- _wingalphaStar.doubleValue(NonSI.DEGREE_ANGLE)),
						NonSI.DEGREE_ANGLE);
				
		// ALPHA ZERO LIFT
		
		_alphaZeroLiftFlapped = 
				Amount.valueOf(
						-(_clZeroFlapped  /_clAlphaDegFlapped ),
						NonSI.DEGREE_ANGLE
						);
		// 3D CURVE-------------
		
		_cl3DCurveWingFlapped = LiftCalc.calculateCLvsAlphaArray(
				_clZeroFlapped,
				_clMaxFlapped,
				_alphaStarFlapped,
				_alphaStallFlapped,
				_wingclAlphaFlapped,
				MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
				);
}
	
	public void calculateCL(){
		double [] alphaDistribution;
		clDistributions = new ArrayList<>();
		List<Double> clDistributionActual;
		cl3D = new Double[_alphaWingForDistribution.size()]; 
		Double[] cCL = new Double[_wingNumberOfPointSemiSpanWise]; 
		for (int i=0; i<_alphaWingForDistribution.size(); i++){
			alphaDistribution = new double [_wingNumberOfPointSemiSpanWise];
			clDistributionActual = new ArrayList<>();
			for (int ii=0; ii<_wingNumberOfPointSemiSpanWise; ii++){
				alphaDistribution[ii] = _alphaWingForDistribution.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
						_wingTwistDistribution.get(ii).doubleValue(NonSI.DEGREE_ANGLE)
						+ _alphaIWingDistribution.get(i).get(ii).doubleValue(NonSI.DEGREE_ANGLE);
	
				
				clDistributionActual.add(ii,
						MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(_alphasWing),
								MyArrayUtils.convertToDoublePrimitive(
										MyArrayUtils.convertListOfDoubleToDoubleArray(
												_wingCLAirfoilsDistributionFinal.get(ii))),
								alphaDistribution[ii]
								));
			}
			clDistributions.add(i, clDistributionActual);
			for (int j=0; j<_wingNumberOfPointSemiSpanWise; j++){
				cCL[j] = clDistributions.get(i).get(j)*_wingChordsDistribution.get(j).doubleValue(SI.METER);
			}
			cl3D[i] = 	(2/_wingSurface.doubleValue(SI.SQUARE_METRE)) * MyMathUtils.integrate1DSimpsonSpline(
					MyArrayUtils.convertListOfAmountTodoubleArray(_wingYDistribution),
					MyArrayUtils.convertToDoublePrimitive(cCL));
		}
	}
	
	// POLAR CURVE
	public void calculatePolarCurve(){

		if (_theCondition == ConditionEnum.CRUISE){
		List<Double> _cdTotalList;
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){
			_cdTotalList = new ArrayList<>();
			for (int ii=0; ii<getNumberOfAlphasBody(); ii++){
				_cdTotalList.add(_wingDragCoefficient3DCurve.get(ii) + (
						_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
						_dynamicPressureRatio*
						(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)))+
						cdDistributionFuselageFinal.get(ii)+
						_deltaCD0Miscellaneus
						);
			}
			_totalDragPolar.put(_anglesOfElevatorDeflection.get(i),
					_cdTotalList
					);
		}
		}
		
		if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition ==ConditionEnum.LANDING){
		List<Double> _cdTotalList;
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){
			_cdTotalList = new ArrayList<>();
			for (int ii=0; ii<getNumberOfAlphasBody(); ii++){
				_cdTotalList.add(_wingDragCoefficient3DCurve.get(ii) + (
						_hTailDragCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
						_dynamicPressureRatio*
						(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)))+
						cdDistributionFuselageFinal.get(ii)+
						_cDLandingGear+
						_deltaCD0Miscellaneus
						);
			}
			_totalDragPolar.put(_anglesOfElevatorDeflection.get(i),
					_cdTotalList
					);
		}
		}
	}
	


	//Getters and setters
	public boolean getDownwashConstant() {
		return _downwashConstant;
	}

	public void setDownwashConstant(boolean _downwashConstant) {
		this._downwashConstant = _downwashConstant;
	}

	public Amount<Length> getXCGAircraft() {
		return _xCGAircraft;
	}

	public Amount<Length> getYCGAircraft() {
		return _yCGAircraft;
	}

	public Amount<Length> getZCGAircraft() {
		return _zCGAircraft;
	}

	public Amount<Length> getAltitude() {
		return _altitude;
	}

	public Double getMachCurrent() {
		return _machCurrent;
	}

	public Double getReynoldsCurrent() {
		return _reynoldsCurrent;
	}

	public Amount<Angle> getAlphaBodyInitial() {
		return _alphaBodyInitial;
	}

	public Amount<Angle> getAlphaBodyFinal() {
		return _alphaBodyFinal;
	}

	public int getNumberOfAlphasBody() {
		return _numberOfAlphasBody;
	}

	public List<Amount<Angle>> getAlphasBody() {
		return _alphasBody;
	}

	public ConditionEnum getTheCondition() {
		return _theCondition;
	}

	public Amount<Length> getXApexWing() {
		return _xApexWing;
	}

	public Amount<Length> getYApexWing() {
		return _yApexWing;
	}

	public Amount<Length> getZApexWing() {
		return _zApexWing;
	}

	public Amount<Area> getWingSurface() {
		return _wingSurface;
	}

	public Double getWingAspectRatio() {
		return _wingAspectRatio;
	}

	public Amount<Length> getWingSpan() {
		return _wingSpan;
	}

	public Amount<Length> getWingSemiSpan() {
		return _wingSemiSpan;
	}

	public int getWingNumberOfPointSemiSpanWise() {
		return _wingNumberOfPointSemiSpanWise;
	}

	public Double getWingAdimentionalKinkStation() {
		return _wingAdimentionalKinkStation;
	}

	public int getWingNumberOfGivenSections() {
		return _wingNumberOfGivenSections;
	}

	public AirfoilFamilyEnum getWingMeanAirfoilFamily() {
		return _wingMeanAirfoilFamily;
	}

	public Double getWingMaxThicknessMeanAirfoil() {
		return _wingMaxThicknessMeanAirfoil;
	}

	public List<Double> getWingYAdimensionalBreakPoints() {
		return _wingYAdimensionalBreakPoints;
	}

	public List<Amount<Length>> getWingYBreakPoints() {
		return _wingYBreakPoints;
	}

	public List<Double> getWingYAdimensionalDistribution() {
		return _wingYAdimensionalDistribution;
	}

	public List<Amount<Length>> getWingYDistribution() {
		return _wingYDistribution;
	}

	public List<Amount<Length>> getWingChordsBreakPoints() {
		return _wingChordsBreakPoints;
	}

	public List<Amount<Length>> getWingChordsDistribution() {
		return _wingChordsDistribution;
	}

	public List<Amount<Length>> getWingXleBreakPoints() {
		return _wingXleBreakPoints;
	}

	public List<Amount<Length>> getWingXleDistribution() {
		return _wingXleDistribution;
	}

	public List<Amount<Angle>> getWingTwistBreakPoints() {
		return _wingTwistBreakPoints;
	}

	public List<Amount<Angle>> getWingTwistDistribution() {
		return _wingTwistDistribution;
	}

	public List<Amount<Angle>> getWingDihedralBreakPoints() {
		return _wingDihedralBreakPoints;
	}

	public List<Amount<Angle>> getWingDihedralDistribution() {
		return _wingDihedralDistribution;
	}

	public List<Amount<Angle>> getWingAlphaZeroLiftBreakPoints() {
		return _wingAlphaZeroLiftBreakPoints;
	}

	public List<Amount<Angle>> getWingAlphaZeroLiftDistribution() {
		return _wingAlphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> getWingAlphaStarBreakPoints() {
		return _wingAlphaStarBreakPoints;
	}

	public List<Amount<Angle>> getWingAlphaStarDistribution() {
		return _wingAlphaStarDistribution;
	}

	public List<Double> getWingClMaxBreakPoints() {
		return _wingClMaxBreakPoints;
	}

	public List<Double> getWingClMaxDistribution() {
		return _wingClMaxDistribution;
	}

	public List<Amount<Angle>> getWingDeltaFlap() {
		return _wingDeltaFlap;
	}

	public List<FlapTypeEnum> getWingFlapType() {
		return _wingFlapType;
	}

	public List<Double> getWingEtaInFlap() {
		return _wingEtaInFlap;
	}

	public List<Double> getWingEtaOutFlap() {
		return _wingEtaOutFlap;
	}

	public List<Double> getWingFlapCfC() {
		return _wingFlapCfC;
	}

	public List<Amount<Angle>> getWingDeltaSlat() {
		return _wingDeltaSlat;
	}

	public List<Double> getWingEtaInSlat() {
		return _wingEtaInSlat;
	}

	public List<Double> getWingEtaOutSlat() {
		return _wingEtaOutSlat;
	}

	public List<Double> getWingSlatCsC() {
		return _wingSlatCsC;
	}

	public List<Double> getWingCExtCSlat() {
		return _wingCExtCSlat;
	}

	public List<Double> getWingLeRadiusCSLat() {
		return _wingLeRadiusCSLat;
	}

	public Amount<Length> getFuselageDiameter() {
		return _fuselageDiameter;
	}

	public Amount<Length> getFuselageLength() {
		return _fuselageLength;
	}

	public Double getFuselageNoseFinessRatio() {
		return _fuselageNoseFinessRatio;
	}

	public Double getFuselageFinessRatio() {
		return _fuselageFinessRatio;
	}

	public Double getFuselageTailFinessRatio() {
		return _fuselageTailFinessRatio;
	}

	public Amount<Angle> getFuselageWindshieldAngle() {
		return _fuselageWindshieldAngle;
	}

	public Amount<Angle> getFuselageUpSweepAngle() {
		return _fuselageUpSweepAngle;
	}

	public Double getFuselageXPercentPositionPole() {
		return _fuselageXPercentPositionPole;
	}

	public Amount<Length> getXApexHTail() {
		return _xApexHTail;
	}

	public Amount<Length> getYApexHTail() {
		return _yApexHTail;
	}

	public Amount<Length> getZApexHTail() {
		return _zApexHTail;
	}

	public Amount<Area> getHTailSurface() {
		return _hTailSurface;
	}

	public Double getHTailAspectRatio() {
		return _hTailAspectRatio;
	}

	public Amount<Length> getHTailSpan() {
		return _hTailSpan;
	}

	public Amount<Length> getHTailSemiSpan() {
		return _hTailSemiSpan;
	}

	public int getHTailNumberOfPointSemiSpanWise() {
		return _hTailNumberOfPointSemiSpanWise;
	}

	public Double getHTailadimentionalKinkStation() {
		return _hTailadimentionalKinkStation;
	}

	public int getHTailnumberOfGivenSections() {
		return _hTailnumberOfGivenSections;
	}

	public AirfoilFamilyEnum getHTailMeanAirfoilFamily() {
		return _hTailMeanAirfoilFamily;
	}

	public Double getHTailMaxThicknessMeanAirfoil() {
		return _hTailMaxThicknessMeanAirfoil;
	}

	public List<Double> getHTailYAdimensionalBreakPoints() {
		return _hTailYAdimensionalBreakPoints;
	}

	public List<Amount<Length>> getHTailYBreakPoints() {
		return _hTailYBreakPoints;
	}

	public List<Double> getHTailYAdimensionalDistribution() {
		return _hTailYAdimensionalDistribution;
	}

	public List<Amount<Length>> getHTailYDistribution() {
		return _hTailYDistribution;
	}

	public List<Amount<Length>> getHTailChordsBreakPoints() {
		return _hTailChordsBreakPoints;
	}

	public List<Amount<Length>> getHTailChordsDistribution() {
		return _hTailChordsDistribution;
	}

	public List<Amount<Length>> getHTailXleBreakPoints() {
		return _hTailXleBreakPoints;
	}

	public List<Amount<Length>> getHTailXleDistribution() {
		return _hTailXleDistribution;
	}

	public List<Amount<Angle>> getHTailTwistBreakPoints() {
		return _hTailTwistBreakPoints;
	}

	public List<Amount<Angle>> getHTailTwistDistribution() {
		return _hTailTwistDistribution;
	}

	public List<Amount<Angle>> getHTailDihedralBreakPoints() {
		return _hTailDihedralBreakPoints;
	}

	public List<Amount<Angle>> getHTailDihedralDistribution() {
		return _hTailDihedralDistribution;
	}

	public List<Amount<Angle>> getHTailAlphaZeroLiftBreakPoints() {
		return _hTailAlphaZeroLiftBreakPoints;
	}

	public List<Amount<Angle>> getHTailAlphaZeroLiftDistribution() {
		return _hTailAlphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> getHTailAlphaStarBreakPoints() {
		return _hTailAlphaStarBreakPoints;
	}

	public List<Amount<Angle>> getHTailAlphaStarDistribution() {
		return _hTailAlphaStarDistribution;
	}

	public List<Double> getHTailClMaxBreakPoints() {
		return _hTailClMaxBreakPoints;
	}

	public List<Double> getHTailClMaxDistribution() {
		return _hTailClMaxDistribution;
	}

	public List<Amount<Angle>> getAnglesOfElevatorDeflection() {
		return _anglesOfElevatorDeflection;
	}

	public FlapTypeEnum getElevatorType() {
		return _elevatorType;
	}

	public Double getElevatorEtaIn() {
		return _elevatorEtaIn;
	}

	public Double getElevatorEtaOut() {
		return _elevatorEtaOut;
	}

	public Double getElevatorCfC() {
		return _elevatorCfC;
	}

	public Amount<Angle> getTiltingAngle() {
		return _tiltingAngle;
	}

	public void setXCGAircraft(Amount<Length> _xCGAircraft) {
		this._xCGAircraft = _xCGAircraft;
	}

	public void setYCGAircraft(Amount<Length> _yCGAircraft) {
		this._yCGAircraft = _yCGAircraft;
	}

	public void setZCGAircraft(Amount<Length> _zCGAircraft) {
		this._zCGAircraft = _zCGAircraft;
	}

	public void setAltitude(Amount<Length> _altitude) {
		this._altitude = _altitude;
	}

	public void setMachCurrent(Double _machCurrent) {
		this._machCurrent = _machCurrent;
	}

	public void setReynoldsCurrent(Double _reynoldsCurrent) {
		this._reynoldsCurrent = _reynoldsCurrent;
	}

	public void setAlphaBodyInitial(Amount<Angle> _alphaBodyInitial) {
		this._alphaBodyInitial = _alphaBodyInitial;
	}

	public void setAlphaBodyFinal(Amount<Angle> _alphaBodyFinal) {
		this._alphaBodyFinal = _alphaBodyFinal;
	}

	public void setNumberOfAlphasBody(int _numberOfAlphasBody) {
		this._numberOfAlphasBody = _numberOfAlphasBody;
	}

	public void setAlphasBody(List<Amount<Angle>> _alphasBody) {
		this._alphasBody = _alphasBody;
	}

	public void setTheCondition(ConditionEnum _theCondition) {
		this._theCondition = _theCondition;
	}

	public void setXApexWing(Amount<Length> _xApexWing) {
		this._xApexWing = _xApexWing;
	}

	public void setYApexWing(Amount<Length> _yApexWing) {
		this._yApexWing = _yApexWing;
	}

	public void setZApexWing(Amount<Length> _zApexWing) {
		this._zApexWing = _zApexWing;
	}

	public void setWingSurface(Amount<Area> _wingSurface) {
		this._wingSurface = _wingSurface;
	}

	public void setWingAspectRatio(Double _wingAspectRatio) {
		this._wingAspectRatio = _wingAspectRatio;
	}

	public void setWingSpan(Amount<Length> _wingSpan) {
		this._wingSpan = _wingSpan;
	}

	public void setWingSemiSpan(Amount<Length> _wingSemiSpan) {
		this._wingSemiSpan = _wingSemiSpan;
	}

	public void setWingNumberOfPointSemiSpanWise(int _wingNumberOfPointSemiSpanWise) {
		this._wingNumberOfPointSemiSpanWise = _wingNumberOfPointSemiSpanWise;
	}

	public void setWingAdimentionalKinkStation(Double _wingAdimentionalKinkStation) {
		this._wingAdimentionalKinkStation = _wingAdimentionalKinkStation;
	}

	public void setWingNumberOfGivenSections(int _wingNumberOfGivenSections) {
		this._wingNumberOfGivenSections = _wingNumberOfGivenSections;
	}

	public void setWingMeanAirfoilFamily(AirfoilFamilyEnum _wingMeanAirfoilFamily) {
		this._wingMeanAirfoilFamily = _wingMeanAirfoilFamily;
	}

	public void setWingMaxThicknessMeanAirfoil(Double _wingMaxThicknessMeanAirfoil) {
		this._wingMaxThicknessMeanAirfoil = _wingMaxThicknessMeanAirfoil;
	}

	public void setWingYAdimensionalBreakPoints(List<Double> _wingYAdimensionalBreakPoints) {
		this._wingYAdimensionalBreakPoints = _wingYAdimensionalBreakPoints;
	}

	public void setWingYBreakPoints(List<Amount<Length>> _wingYBreakPoints) {
		this._wingYBreakPoints = _wingYBreakPoints;
	}

	public void setWingYAdimensionalDistribution(List<Double> _wingYAdimensionalDistribution) {
		this._wingYAdimensionalDistribution = _wingYAdimensionalDistribution;
	}

	public void setWingYDistribution(List<Amount<Length>> _wingYDistribution) {
		this._wingYDistribution = _wingYDistribution;
	}

	public void setWingChordsBreakPoints(List<Amount<Length>> _wingChordsBreakPoints) {
		this._wingChordsBreakPoints = _wingChordsBreakPoints;
	}

	public void setWingChordsDistribution(List<Amount<Length>> _wingChordsDistribution) {
		this._wingChordsDistribution = _wingChordsDistribution;
	}

	public void setWingXleBreakPoints(List<Amount<Length>> _wingXleBreakPoints) {
		this._wingXleBreakPoints = _wingXleBreakPoints;
	}

	public void setWingXleDistribution(List<Amount<Length>> _wingXleDistribution) {
		this._wingXleDistribution = _wingXleDistribution;
	}

	public void setWingTwistBreakPoints(List<Amount<Angle>> _wingTwistBreakPoints) {
		this._wingTwistBreakPoints = _wingTwistBreakPoints;
	}

	public void setWingTwistDistribution(List<Amount<Angle>> _wingTwistDistribution) {
		this._wingTwistDistribution = _wingTwistDistribution;
	}

	public void setWingDihedralBreakPoints(List<Amount<Angle>> _wingDihedralBreakPoints) {
		this._wingDihedralBreakPoints = _wingDihedralBreakPoints;
	}

	public void setWingDihedralDistribution(List<Amount<Angle>> _wingDihedralDistribution) {
		this._wingDihedralDistribution = _wingDihedralDistribution;
	}

	public void setWingAlphaZeroLiftBreakPoints(List<Amount<Angle>> _wingAlphaZeroLiftBreakPoints) {
		this._wingAlphaZeroLiftBreakPoints = _wingAlphaZeroLiftBreakPoints;
	}

	public void setWingAlphaZeroLiftDistribution(List<Amount<Angle>> _wingAlphaZeroLiftDistribution) {
		this._wingAlphaZeroLiftDistribution = _wingAlphaZeroLiftDistribution;
	}

	public void setWingAlphaStarBreakPoints(List<Amount<Angle>> _wingAlphaStarBreakPoints) {
		this._wingAlphaStarBreakPoints = _wingAlphaStarBreakPoints;
	}

	public void setWingAlphaStarDistribution(List<Amount<Angle>> _wingAlphaStarDistribution) {
		this._wingAlphaStarDistribution = _wingAlphaStarDistribution;
	}

	public void setWingClMaxBreakPoints(List<Double> _wingClMaxBreakPoints) {
		this._wingClMaxBreakPoints = _wingClMaxBreakPoints;
	}

	public void setWingClMaxDistribution(List<Double> _wingClMaxDistribution) {
		this._wingClMaxDistribution = _wingClMaxDistribution;
	}

	public int getWingNumberOfFlaps() {
		return _wingNumberOfFlaps;
	}

	public void setWingNumberOfFlaps(int _wingNumberOfFlaps) {
		this._wingNumberOfFlaps = _wingNumberOfFlaps;
	}

	public int getWingNumberOfSlats() {
		return _wingNumberOfSlats;
	}

	public void setWingNumberOfSlats(int _wingNumberOfSlats) {
		this._wingNumberOfSlats = _wingNumberOfSlats;
	}

	public void setWingDeltaFlap(List<Amount<Angle>> _wingDeltaFlap) {
		this._wingDeltaFlap = _wingDeltaFlap;
	}

	public void setWingFlapType(List<FlapTypeEnum> _wingFlapType) {
		this._wingFlapType = _wingFlapType;
	}

	public void setWingEtaInFlap(List<Double> _wingEtaInFlap) {
		this._wingEtaInFlap = _wingEtaInFlap;
	}

	public void setWingEtaOutFlap(List<Double> _wingEtaOutFlap) {
		this._wingEtaOutFlap = _wingEtaOutFlap;
	}

	public void setWingFlapCfC(List<Double> _wingFlapCfC) {
		this._wingFlapCfC = _wingFlapCfC;
	}

	public void setWingDeltaSlat(List<Amount<Angle>> _wingDeltaSlat) {
		this._wingDeltaSlat = _wingDeltaSlat;
	}

	public void setWingEtaInSlat(List<Double> _wingEtaInSlat) {
		this._wingEtaInSlat = _wingEtaInSlat;
	}

	public void setWingEtaOutSlat(List<Double> _wingEtaOutSlat) {
		this._wingEtaOutSlat = _wingEtaOutSlat;
	}

	public void setWingSlatCsC(List<Double> _wingSlatCsC) {
		this._wingSlatCsC = _wingSlatCsC;
	}

	public void setWingCExtCSlat(List<Double> _wingCExtCSlat) {
		this._wingCExtCSlat = _wingCExtCSlat;
	}

	public void setWingLeRadiusCSLat(List<Double> _wingLeRadiusCSLat) {
		this._wingLeRadiusCSLat = _wingLeRadiusCSLat;
	}

	public void setFuselageDiameter(Amount<Length> _fuselageDiameter) {
		this._fuselageDiameter = _fuselageDiameter;
	}

	public void setFuselageLength(Amount<Length> _fuselageLength) {
		this._fuselageLength = _fuselageLength;
	}

	public void setFuselageNoseFinessRatio(Double _fuselageNoseFinessRatio) {
		this._fuselageNoseFinessRatio = _fuselageNoseFinessRatio;
	}

	public void setFuselageFinessRatio(Double _fuselageFinessRatio) {
		this._fuselageFinessRatio = _fuselageFinessRatio;
	}

	public void setFuselageTailFinessRatio(Double _fuselageTailFinessRatio) {
		this._fuselageTailFinessRatio = _fuselageTailFinessRatio;
	}

	public void setFuselageWindshieldAngle(Amount<Angle> _fuselageWindshieldAngle) {
		this._fuselageWindshieldAngle = _fuselageWindshieldAngle;
	}

	public void setFuselageUpSweepAngle(Amount<Angle> _fuselageUpSweepAngle) {
		this._fuselageUpSweepAngle = _fuselageUpSweepAngle;
	}

	public void setFuselageXPercentPositionPole(Double _fuselageXPercentPositionPole) {
		this._fuselageXPercentPositionPole = _fuselageXPercentPositionPole;
	}

	public void setXApexHTail(Amount<Length> _xApexHTail) {
		this._xApexHTail = _xApexHTail;
	}

	public void setYApexHTail(Amount<Length> _yApexHTail) {
		this._yApexHTail = _yApexHTail;
	}

	public void setZApexHTail(Amount<Length> _zApexHTail) {
		this._zApexHTail = _zApexHTail;
	}

	public void setHTailSurface(Amount<Area> _hTailSurface) {
		this._hTailSurface = _hTailSurface;
	}

	public void setHTailAspectRatio(Double _hTailAspectRatio) {
		this._hTailAspectRatio = _hTailAspectRatio;
	}

	public void setHTailSpan(Amount<Length> _hTailSpan) {
		this._hTailSpan = _hTailSpan;
	}

	public void setHTailSemiSpan(Amount<Length> _hTailSemiSpan) {
		this._hTailSemiSpan = _hTailSemiSpan;
	}

	public void setHTailNumberOfPointSemiSpanWise(int _hTailNumberOfPointSemiSpanWise) {
		this._hTailNumberOfPointSemiSpanWise = _hTailNumberOfPointSemiSpanWise;
	}

	public void setHTailadimentionalKinkStation(Double _hTailadimentionalKinkStation) {
		this._hTailadimentionalKinkStation = _hTailadimentionalKinkStation;
	}

	public void setHTailnumberOfGivenSections(int _hTailnumberOfGivenSections) {
		this._hTailnumberOfGivenSections = _hTailnumberOfGivenSections;
	}

	public void setHTailMeanAirfoilFamily(AirfoilFamilyEnum _hTailMeanAirfoilFamily) {
		this._hTailMeanAirfoilFamily = _hTailMeanAirfoilFamily;
	}

	public void setHTailMaxThicknessMeanAirfoil(Double _hTailMaxThicknessMeanAirfoil) {
		this._hTailMaxThicknessMeanAirfoil = _hTailMaxThicknessMeanAirfoil;
	}

	public void setHTailYAdimensionalBreakPoints(List<Double> _hTailYAdimensionalBreakPoints) {
		this._hTailYAdimensionalBreakPoints = _hTailYAdimensionalBreakPoints;
	}

	public void setHTailYBreakPoints(List<Amount<Length>> _hTailYBreakPoints) {
		this._hTailYBreakPoints = _hTailYBreakPoints;
	}

	public void setHTailYAdimensionalDistribution(List<Double> _hTailYAdimensionalDistribution) {
		this._hTailYAdimensionalDistribution = _hTailYAdimensionalDistribution;
	}

	public void setHTailYDistribution(List<Amount<Length>> _hTailYDistribution) {
		this._hTailYDistribution = _hTailYDistribution;
	}

	public void setHTailChordsBreakPoints(List<Amount<Length>> _hTailChordsBreakPoints) {
		this._hTailChordsBreakPoints = _hTailChordsBreakPoints;
	}

	public void setHTailChordsDistribution(List<Amount<Length>> _hTailChordsDistribution) {
		this._hTailChordsDistribution = _hTailChordsDistribution;
	}

	public void setHTailXleBreakPoints(List<Amount<Length>> _hTailXleBreakPoints) {
		this._hTailXleBreakPoints = _hTailXleBreakPoints;
	}

	public void setHTailXleDistribution(List<Amount<Length>> _hTailXleDistribution) {
		this._hTailXleDistribution = _hTailXleDistribution;
	}

	public void setHTailTwistBreakPoints(List<Amount<Angle>> _hTailTwistBreakPoints) {
		this._hTailTwistBreakPoints = _hTailTwistBreakPoints;
	}

	public void setHTailTwistDistribution(List<Amount<Angle>> _hTailTwistDistribution) {
		this._hTailTwistDistribution = _hTailTwistDistribution;
	}

	public void setHTailDihedralBreakPoints(List<Amount<Angle>> _hTailDihedralBreakPoints) {
		this._hTailDihedralBreakPoints = _hTailDihedralBreakPoints;
	}

	public void setHTailDihedralDistribution(List<Amount<Angle>> _hTailDihedralDistribution) {
		this._hTailDihedralDistribution = _hTailDihedralDistribution;
	}

	public void setHTailAlphaZeroLiftBreakPoints(List<Amount<Angle>> _hTailAlphaZeroLiftBreakPoints) {
		this._hTailAlphaZeroLiftBreakPoints = _hTailAlphaZeroLiftBreakPoints;
	}

	public void setHTailAlphaZeroLiftDistribution(List<Amount<Angle>> _hTailAlphaZeroLiftDistribution) {
		this._hTailAlphaZeroLiftDistribution = _hTailAlphaZeroLiftDistribution;
	}

	public void setHTailAlphaStarBreakPoints(List<Amount<Angle>> _hTailAlphaStarBreakPoints) {
		this._hTailAlphaStarBreakPoints = _hTailAlphaStarBreakPoints;
	}

	public void setHTailAlphaStarDistribution(List<Amount<Angle>> _hTailAlphaStarDistribution) {
		this._hTailAlphaStarDistribution = _hTailAlphaStarDistribution;
	}

	public void setHTailClMaxBreakPoints(List<Double> _hTailClMaxBreakPoints) {
		this._hTailClMaxBreakPoints = _hTailClMaxBreakPoints;
	}

	public void setHTailClMaxDistribution(List<Double> _hTailClMaxDistribution) {
		this._hTailClMaxDistribution = _hTailClMaxDistribution;
	}

	public void setAnglesOfElevatorDeflection(List<Amount<Angle>> _anglesOfElevatorDeflection) {
		this._anglesOfElevatorDeflection = _anglesOfElevatorDeflection;
	}

	public void setElevatorType(FlapTypeEnum _elevatorType) {
		this._elevatorType = _elevatorType;
	}

	public void setElevatorEtaIn(Double _elevatorEtaIn) {
		this._elevatorEtaIn = _elevatorEtaIn;
	}

	public void setElevatorEtaOut(Double _elevatorEtaOut) {
		this._elevatorEtaOut = _elevatorEtaOut;
	}

	public void setElevatorCfC(Double _elevatorCfC) {
		this._elevatorCfC = _elevatorCfC;
	}

	public void setTiltingAngle(Amount<Angle> _tiltingAngle) {
		this._tiltingAngle = _tiltingAngle;
	}

	public boolean getPlotCheck() {
		return _plotCheck;
	}

	public void setPlotCheck(boolean plotCheck) {
		this._plotCheck = plotCheck;
	}

	public List<AerodynamicAndStabilityPlotEnum> getPlotList() {
		return _plotList;
	}

	public void setPlotList(List<AerodynamicAndStabilityPlotEnum> plotList) {
		this._plotList = plotList;
	}

	public List<Amount<Angle>> get_alphasWing() {
		return _alphasWing;
	}

	public List<Amount<Angle>> get_alphasTail() {
		return _alphasTail;
	}

	public List<Amount<Angle>> get_downwashAngle() {
		return _downwashAngleVariableSlingerland;
	}

	public List<Double> get_downwashGradient() {
		return _downwashGradientVariableSlingerland;
	}

	public void set_alphasWing(List<Amount<Angle>> _alphasWing) {
		this._alphasWing = _alphasWing;
	}

	public void set_alphasTail(List<Amount<Angle>> _alphasTail) {
		this._alphasTail = _alphasTail;
	}

	public void set_downwashAngle(List<Amount<Angle>> _downwashAngle) {
		this._downwashAngleVariableSlingerland = _downwashAngle;
	}

	public void set_downwashGradientSlingerland(List<Double> _downwashGradient) {
		this._downwashGradientVariableSlingerland = _downwashGradient;
	}

	public Amount<Angle> getWingAngleOfIncidence() {
		return _wingAngleOfIncidence;
	}

	public void setWingAngleOfIncidence(Amount<Angle> _wingAngleOfIncidence) {
		this._wingAngleOfIncidence = _wingAngleOfIncidence;
	}

	public Amount<Angle> getHTailAngleOfIncidence() {
		return _hTailAngleOfIncidence;
	}

	public void setHTailAngleOfIncidence(Amount<Angle> _hTailAngleOfIncidence) {
		this._hTailAngleOfIncidence = _hTailAngleOfIncidence;
	}

	public Double getWingTaperRatio() {
		return _wingTaperRatio;
	}

	public Amount<Angle> getWingSweepQuarterChord() {
		return _wingSweepQuarterChord;
	}

	public void setWingTaperRatio(Double _wingTaperRatio) {
		this._wingTaperRatio = _wingTaperRatio;
	}

	public void setWingSweepQuarterChord(Amount<Angle> _wingSweepQuarterChord) {
		this._wingSweepQuarterChord = _wingSweepQuarterChord;
	}

	public Amount<Angle> getWingSweepLE() {
		return _wingSweepLE;
	}

	public void setWingSweepLE(Amount<Angle> _wingSweepLE) {
		this._wingSweepLE = _wingSweepLE;
	}


	public List<Double> getWingClAlphaBreakPointsDeg() {
		return _wingClAlphaBreakPointsDeg;
	}


	public List<Double> getWingClAlphaDistributionDeg() {
		return _wingClAlphaDistributionDeg;
	}


	public void setWingClAlphaBreakPointsDeg(List<Double> _wingClAlphaBreakPointsDeg) {
		this._wingClAlphaBreakPointsDeg = _wingClAlphaBreakPointsDeg;
	}


	public void setWingClAlphaDistributionDeg(List<Double> _wingClAlphaDistributionDeg) {
		this._wingClAlphaDistributionDeg = _wingClAlphaDistributionDeg;
	}


	public List<Double> get_wingMaxThicknessBreakPoints() {
		return _wingMaxThicknessBreakPoints;
	}


	public List<Double> get_wingMaxThicknessDistribution() {
		return _wingMaxThicknessDistribution;
	}


	public void setWingMaxThicknessBreakPoints(List<Double> _wingMaxThicknessBreakPoints) {
		this._wingMaxThicknessBreakPoints = _wingMaxThicknessBreakPoints;
	}


	public void setWingMaxThicknessDistribution(List<Double> _wingMaxThicknessDistribution) {
		this._wingMaxThicknessDistribution = _wingMaxThicknessDistribution;
	}


	public List<Double> getWingCl0BreakPoints() {
		return _wingCl0BreakPoints;
	}


	public List<Double> getWingCl0Distribution() {
		return _wingCl0Distribution;
	}


	public void setWingCl0BreakPoints(List<Double> _wingCl0BreakPoints) {
		this._wingCl0BreakPoints = _wingCl0BreakPoints;
	}


	public void setWingCl0Distribution(List<Double> _wingCl0Distribution) {
		this._wingCl0Distribution = _wingCl0Distribution;
	}


	public Double getCLAtAlphaHighLift() {
		return _cLAtAlphaHighLift;
	}


	public Amount<Angle> getAlphaZeroLiftHighLift() {
		return _alphaZeroLiftHighLift;
	}


	public Amount<Angle> getAlphaStarHighLift() {
		return _alphaStarHighLift;
	}


	public Amount<Angle> getAlphaStallHighLift() {
		return _alphaStallHighLift;
	}


	public Double getCLZeroHighLift() {
		return _cLZeroHighLift;
	}


	public Double getCLStarHighLift() {
		return _cLStarHighLift;
	}


	public Double getCLMaxHighLift() {
		return _cLMaxHighLift;
	}


	public Double getCLAlphaHighLift() {
		return _cLAlphaHighLiftDEG;
	}


	public void setCLAtAlphaHighLift(Double _cLAtAlphaHighLift) {
		this._cLAtAlphaHighLift = _cLAtAlphaHighLift;
	}


	public void setAlphaZeroLiftHighLift(Amount<Angle> _alphaZeroLiftHighLift) {
		this._alphaZeroLiftHighLift = _alphaZeroLiftHighLift;
	}


	public void setAlphaStarHighLift(Amount<Angle> _alphaStarHighLift) {
		this._alphaStarHighLift = _alphaStarHighLift;
	}


	public void setAlphaStallHighLift(Amount<Angle> _alphaStallHighLift) {
		this._alphaStallHighLift = _alphaStallHighLift;
	}


	public void setCLZeroHighLift(Double _cLZeroHighLift) {
		this._cLZeroHighLift = _cLZeroHighLift;
	}


	public void setCLStarHighLift(Double _cLStarHighLift) {
		this._cLStarHighLift = _cLStarHighLift;
	}


	public void setCLMaxHighLift(Double _cLMaxHighLift) {
		this._cLMaxHighLift = _cLMaxHighLift;
	}


	public void setCLAlphaHighLift(Double _cLAlphaHighLift) {
		this._cLAlphaHighLiftDEG = _cLAlphaHighLift;
	}


	public List<Double> getDeltaCl0FlapList() {
		return _deltaCl0FlapList;
	}


	public Double getDeltaCl0Flap() {
		return _deltaCl0Flap;
	}


	public List<Double> getDeltaCL0FlapList() {
		return _deltaCL0FlapList;
	}


	public Double getDeltaCL0Flap() {
		return _deltaCL0Flap;
	}


	public List<Double> getDeltaClmaxFlapList() {
		return _deltaClmaxFlapList;
	}


	public Double getDeltaClmaxFlap() {
		return _deltaClmaxFlap;
	}


	public List<Double> getDeltaCLmaxFlapList() {
		return _deltaCLmaxFlapList;
	}


	public Double getDeltaCLmaxFlap() {
		return _deltaCLmaxFlap;
	}


	public List<Double> getDeltaClmaxSlatList() {
		return _deltaClmaxSlatList;
	}


	public Double getDeltaClmaxSlat() {
		return _deltaClmaxSlat;
	}


	public List<Double> getDeltaCLmaxSlatList() {
		return _deltaCLmaxSlatList;
	}


	public Double getDeltaCLmaxSlat() {
		return _deltaCLmaxSlat;
	}


	public List<Double> getDeltaCD0List() {
		return _deltaCD0List;
	}


	public Double getDeltaCD0() {
		return _deltaCD0;
	}


	public List<Double> getDeltaCMc4List() {
		return _deltaCMc4List;
	}


	public Double getDeltaCMc4() {
		return _deltaCMc4;
	}


	public void setDeltaCl0FlapList(List<Double> _deltaCl0FlapList) {
		this._deltaCl0FlapList = _deltaCl0FlapList;
	}


	public void setDeltaCl0Flap(Double _deltaCl0Flap) {
		this._deltaCl0Flap = _deltaCl0Flap;
	}


	public void setDeltaCL0FlapList(List<Double> _deltaCL0FlapList) {
		this._deltaCL0FlapList = _deltaCL0FlapList;
	}


	public void setDeltaCL0Flap(Double _deltaCL0Flap) {
		this._deltaCL0Flap = _deltaCL0Flap;
	}


	public void setDeltaClmaxFlapList(List<Double> _deltaClmaxFlapList) {
		this._deltaClmaxFlapList = _deltaClmaxFlapList;
	}


	public void setDeltaClmaxFlap(Double _deltaClmaxFlap) {
		this._deltaClmaxFlap = _deltaClmaxFlap;
	}


	public void setDeltaCLmaxFlapList(List<Double> _deltaCLmaxFlapList) {
		this._deltaCLmaxFlapList = _deltaCLmaxFlapList;
	}


	public void setDeltaCLmaxFlap(Double _deltaCLmaxFlap) {
		this._deltaCLmaxFlap = _deltaCLmaxFlap;
	}


	public void setDeltaClmaxSlatList(List<Double> _deltaClmaxSlatList) {
		this._deltaClmaxSlatList = _deltaClmaxSlatList;
	}


	public void setDeltaClmaxSlat(Double _deltaClmaxSlat) {
		this._deltaClmaxSlat = _deltaClmaxSlat;
	}


	public void setDeltaCLmaxSlatList(List<Double> _deltaCLmaxSlatList) {
		this._deltaCLmaxSlatList = _deltaCLmaxSlatList;
	}


	public void setDeltaCLmaxSlat(Double _deltaCLmaxSlat) {
		this._deltaCLmaxSlat = _deltaCLmaxSlat;
	}


	public void setDeltaCD0List(List<Double> _deltaCD0List) {
		this._deltaCD0List = _deltaCD0List;
	}


	public void setDeltaCD0(Double _deltaCD0) {
		this._deltaCD0 = _deltaCD0;
	}


	public void setDeltaCMc4List(List<Double> _deltaCMc4List) {
		this._deltaCMc4List = _deltaCMc4List;
	}


	public void setDeltaCMc4(Double _deltaCMc4) {
		this._deltaCMc4 = _deltaCMc4;
	}


	public Double get_wingcLAlphaRad() {
		return _wingcLAlphaRad;
	}


	public Double get_wingcLAlphaDeg() {
		return _wingcLAlphaDeg;
	}


	public void setWingcLAlphaRad(Double _wingcLAlphaRad) {
		this._wingcLAlphaRad = _wingcLAlphaRad;
	}


	public List<Amount<Length>> getWingLERadiusBreakPoints() {
		return _wingLERadiusBreakPoints;
	}


	public void setWingcLAlphaDeg(Double _wingcLAlphaDeg) {
		this._wingcLAlphaDeg = _wingcLAlphaDeg;
	}


	public void setWingLERadiusBreakPoints(List<Amount<Length>> _wingLERadiusBreakPoints) {
		this._wingLERadiusBreakPoints = _wingLERadiusBreakPoints;
	}


	public List<Double> get_wingClAlphaBreakPointsDeg() {
		return _wingClAlphaBreakPointsDeg;
	}


	public Amount<Angle> getWingAlphaZeroLift() {
		return _wingAlphaZeroLift;
	}


	public void setWingAlphaZeroLift(Amount<Angle> _wingAlphaZeroLift) {
		this._wingAlphaZeroLift = _wingAlphaZeroLift;
	}


	public Amount<Angle> getWingAlphaZeroLiftCONDITION() {
		return _wingAlphaZeroLiftCONDITION;
	}


	public Amount<Angle> getWingalphaStarCONDITION() {
		return _wingalphaStarCONDITION;
	}


	public Amount<Angle> getWingalphaMaxLinearCONDITION() {
		return _wingalphaMaxLinearCONDITION;
	}


	public Amount<Angle> getWingalphaStallCONDITION() {
		return _wingalphaStallCONDITION;
	}


	public Double getWingcLZeroCONDITION() {
		return _wingcLZeroCONDITION;
	}


	public Double getWingcLStarCONDITION() {
		return _wingcLStarCONDITION;
	}


	public Double getWingcLMaxCONDITION() {
		return _wingcLMaxCONDITION;
	}


	public Double getWingcLAlphaRadCONDITION() {
		return _wingcLAlphaRadCONDITION;
	}


	public Double getWingcLAlphaDegCONDITION() {
		return _wingcLAlphaDegCONDITION;
	}


	public Amount<?> getWingclAlphaCONDITION() {
		return _wingclAlphaCONDITION;
	}


	public Double[] getWingliftCoefficient3DCurveCONDITION() {
		return _wingliftCoefficient3DCurveCONDITION;
	}


	public double[] getWingliftCoefficientDistributionatCLMaxCONDITION() {
		return _wingliftCoefficientDistributionatCLMaxCONDITION;
	}


	public Double[] getWingclAlphaArrayCONDITION() {
		return _wingclAlphaArrayCONDITION;
	}


	public void setWingAlphaZeroLiftCONDITION(Amount<Angle> _wingAlphaZeroLiftCONDITION) {
		this._wingAlphaZeroLiftCONDITION = _wingAlphaZeroLiftCONDITION;
	}


	public void setWingalphaStarCONDITION(Amount<Angle> _wingalphaStarCONDITION) {
		this._wingalphaStarCONDITION = _wingalphaStarCONDITION;
	}


	public void setWingalphaMaxLinearCONDITION(Amount<Angle> _wingalphaMaxLinearCONDITION) {
		this._wingalphaMaxLinearCONDITION = _wingalphaMaxLinearCONDITION;
	}


	public void setWingalphaStallCONDITION(Amount<Angle> _wingalphaStallCONDITION) {
		this._wingalphaStallCONDITION = _wingalphaStallCONDITION;
	}


	public void setWingcLZeroCONDITION(Double _wingcLZeroCONDITION) {
		this._wingcLZeroCONDITION = _wingcLZeroCONDITION;
	}


	public void setWingcLStarCONDITION(Double _wingcLStarCONDITION) {
		this._wingcLStarCONDITION = _wingcLStarCONDITION;
	}


	public void setWingcLMaxCONDITION(Double _wingcLMaxCONDITION) {
		this._wingcLMaxCONDITION = _wingcLMaxCONDITION;
	}


	public void setWingcLAlphaRadCONDITION(Double _wingcLAlphaRadCONDITION) {
		this._wingcLAlphaRadCONDITION = _wingcLAlphaRadCONDITION;
	}


	public void setWingcLAlphaDegCONDITION(Double _wingcLAlphaDegCONDITION) {
		this._wingcLAlphaDegCONDITION = _wingcLAlphaDegCONDITION;
	}


	public void setWingclAlphaCONDITION(Amount<?> _wingclAlphaCONDITION) {
		this._wingclAlphaCONDITION = _wingclAlphaCONDITION;
	}


	public void setWingliftCoefficient3DCurveCONDITION(Double[] _wingliftCoefficient3DCurveCONDITION) {
		this._wingliftCoefficient3DCurveCONDITION = _wingliftCoefficient3DCurveCONDITION;
	}


	public void setWingliftCoefficientDistributionatCLMaxCONDITION(
			double[] _wingliftCoefficientDistributionatCLMaxCONDITION) {
		this._wingliftCoefficientDistributionatCLMaxCONDITION = _wingliftCoefficientDistributionatCLMaxCONDITION;
	}


	public void setWingclAlphaArrayCONDITION(Double[] _wingclAlphaArrayCONDITION) {
		this._wingclAlphaArrayCONDITION = _wingclAlphaArrayCONDITION;
	}


	public List<Double> getHTailMaxThicknessBreakPoints() {
		return _hTailMaxThicknessBreakPoints;
	}


	public List<Double> getHTailMaxThicknessDistribution() {
		return _hTailMaxThicknessDistribution;
	}


	public void setHTailMaxThicknessBreakPoints(List<Double> _hTailMaxThicknessBreakPoints) {
		this._hTailMaxThicknessBreakPoints = _hTailMaxThicknessBreakPoints;
	}


	public void setHTailMaxThicknessDistribution(List<Double> _hTailMaxThicknessDistribution) {
		this._hTailMaxThicknessDistribution = _hTailMaxThicknessDistribution;
	}


	public Double getHTailTaperRatio() {
		return _hTailTaperRatio;
	}


	public void setHTailTaperRatio(Double _hTailTaperRatio) {
		this._hTailTaperRatio = _hTailTaperRatio;
	}


	public Amount<Angle> getHTailSweepLE() {
		return _hTailSweepLE;
	}


	public Amount<Angle> getHTailSweepQuarterChord() {
		return _hTailSweepQuarterChord;
	}


	public void setHTailSweepLE(Amount<Angle> _hTailSweepLE) {
		this._hTailSweepLE = _hTailSweepLE;
	}


	public void setHTailSweepQuarterChord(Amount<Angle> _hTailSweepQuarterChord) {
		this._hTailSweepQuarterChord = _hTailSweepQuarterChord;
	}


	public Map<Amount<Angle>, Double> getDeltaCLMaxElevator() {
		return _deltaCLMaxElevator;
	}


	public void setDeltaCLMaxElevator(Map<Amount<Angle>, Double> _deltaCLMaxElevator) {
		this._deltaCLMaxElevator = _deltaCLMaxElevator;
	}


	public List<Double> getHTailClAlphaBreakPointsDeg() {
		return _hTailClAlphaBreakPointsDeg;
	}


	public void setHTailClAlphaBreakPointsDeg(List<Double> _hTailClAlphaBreakPointsDeg) {
		this._hTailClAlphaBreakPointsDeg = _hTailClAlphaBreakPointsDeg;
	}


	public List<Double> getHTailCl0BreakPoints() {
		return _hTailCl0BreakPoints;
	}


	public void setHTailCl0BreakPoints(List<Double> _hTailCl0BreakPoints) {
		this._hTailCl0BreakPoints = _hTailCl0BreakPoints;
	}


	public Double getHTailcLAlphaDeg() {
		return _hTailcLAlphaDeg;
	}


	public void setHTailcLAlphaDeg(Double _hTailcLAlphaDeg) {
		this._hTailcLAlphaDeg = _hTailcLAlphaDeg;
	}


	public Map<Amount<Angle>, Double> getCLAlphaElevatorDeg() {
		return _cLAlphaElevatorDeg;
	}


	public void setCLAlphaElevatorDeg(Map<Amount<Angle>, Double> _cLAlphaElevatorDeg) {
		this._cLAlphaElevatorDeg = _cLAlphaElevatorDeg;
	}


	public Map<Amount<Angle>, Double> getDeltacLZeroElevator() {
		return _deltacLZeroElevator;
	}


	public void setDeltacLZeroElevator(Map<Amount<Angle>, Double> _deltacLZeroElevator) {
		this._deltacLZeroElevator = _deltacLZeroElevator;
	}


	public String getAircraftName() {
		return _aircraftName;
	}


	public void setAircraftName(String _aircraftName) {
		this._aircraftName = _aircraftName;
	}

	public Map<Amount<Angle>, Double> getHTailCLAlphaElevator() {
		return _hTailCLAlphaElevator;
	}

	public void setHTailCLAlphaElevator(Map<Amount<Angle>, Double> _hTailCLAlphaElevator) {
		this._hTailCLAlphaElevator = _hTailCLAlphaElevator;
	}

	public MethodEnum getwingDragMethod() {
		return _wingDragMethod;
	}

	public void setwingDragMethod(MethodEnum theDragMethod) {
		this._wingDragMethod = theDragMethod;
	}

	public MethodEnum getHTailDragMethod() {
		return _hTailDragMethod;
	}

	public void setHTailDragMethod(MethodEnum _hTailDragMethod) {
		this._hTailDragMethod = _hTailDragMethod;
	}

	public List<Double> getAlphaWingDragPolar() {
		return cLWingDragPolar;
	}

	public List<Double> getcDPolarWing() {
		return cDPolarWing;
	}

	public List<List<Double>> getcDPolarAirfoilsWing() {
		return cDPolarAirfoilsWing;
	}

	public List<Double> getAlphahTailDragPolar() {
		return cLhTailDragPolar;
	}

	public List<Double> getcDPolarhTail() {
		return cDPolarhTail;
	}

	public List<List<Double>> getcDPolarAirfoilsHTail() {
		return cDPolarAirfoilsHTail;
	}

	public void setclWingDragPolar(List<Double> alphaWingDragPolar) {
		this.cLWingDragPolar = alphaWingDragPolar;
	}

	public void setcDPolarWing(List<Double> cDPolarWing) {
		this.cDPolarWing = cDPolarWing;
	}

	public void setcDPolarAirfoilsWing(List<List<Double>> cDPolarAirfoilsWing) {
		this.cDPolarAirfoilsWing = cDPolarAirfoilsWing;
	}

	public void setAlphahTailDragPolar(List<Double> alphahTailDragPolar) {
		this.cLhTailDragPolar = alphahTailDragPolar;
	}

	public void setcDPolarhTail(List<Double> cDPolarhTail) {
		this.cDPolarhTail = cDPolarhTail;
	}

	public void setcDPolarAirfoilsHTail(List<List<Double>> cDPolarAirfoilsHTail) {
		this.cDPolarAirfoilsHTail = cDPolarAirfoilsHTail;
	}

	public List<List<Double>> getClPolarAirfoilWingDragPolar() {
		return clPolarAirfoilWingDragPolar;
	}

	public List<List<Double>> getClPolarAirfoilHTailDragPolar() {
		return clPolarAirfoilHTailDragPolar;
	}

	public void setClPolarAirfoilWingDragPolar(List<List<Double>> clPolarAirfoilWingDragPolar) {
		this.clPolarAirfoilWingDragPolar = clPolarAirfoilWingDragPolar;
	}

	public void setClPolarAirfoilHTailDragPolar(List<List<Double>> clPolarAirfoilHTailDragPolar) {
		this.clPolarAirfoilHTailDragPolar = clPolarAirfoilHTailDragPolar;
	}

	public List<Double> getcLhTailDragPolar() {
		return cLhTailDragPolar;
	}

	public void setcLhTailDragPolar(List<Double> cLhTailDragPolar) {
		this.cLhTailDragPolar = cLhTailDragPolar;
	}

	public MethodEnum getWingairfoilLiftCoefficientCurve() {
		return _wingairfoilLiftCoefficientCurve;
	}

	public List<List<Amount<Angle>>> getAlphaAirfoilsWing() {
		return alphaAirfoilsWing;
	}

	public List<List<Double>> getClDistributionAirfoilsWing() {
		return clDistributionAirfoilsWing;
	}

	public List<List<Double>> getWingCLAirfoilsDistribution() {
		return _wingCLAirfoilsDistribution;
	}

	public void setWingairfoilLiftCoefficientCurve(MethodEnum _wingairfoilLiftCoefficientCurve) {
		this._wingairfoilLiftCoefficientCurve = _wingairfoilLiftCoefficientCurve;
	}

	public void setAlphaAirfoilsWing(List<List<Amount<Angle>>> alphaAirfoilsWing) {
		this.alphaAirfoilsWing = alphaAirfoilsWing;
	}

	public void setClDistributionAirfoilsWing(List<List<Double>> clDistributionAirfoilsWing) {
		this.clDistributionAirfoilsWing = clDistributionAirfoilsWing;
	}

	public void setWingCLAirfoilsDistribution(List<List<Double>> _wingCLAirfoilsDistribution) {
		this._wingCLAirfoilsDistribution = _wingCLAirfoilsDistribution;
	}

	public MethodEnum getHTailairfoilLiftCoefficientCurve() {
		return _hTailairfoilLiftCoefficientCurve;
	}

	public List<List<Amount<Angle>>> getAlphaAirfoilsHTail() {
		return alphaAirfoilsHTail;
	}

	public List<List<Double>> getClDistributionAirfoilsHTail() {
		return clDistributionAirfoilsHTail;
	}

	public List<List<Double>> getHTailCLAirfoilsDistribution() {
		return _hTailCLAirfoilsDistribution;
	}

	public void setHTailairfoilLiftCoefficientCurve(MethodEnum _hTailairfoilLiftCoefficientCurve) {
		this._hTailairfoilLiftCoefficientCurve = _hTailairfoilLiftCoefficientCurve;
	}

	public void setAlphaAirfoilsHTail(List<List<Amount<Angle>>> alphaAirfoilsHTail) {
		this.alphaAirfoilsHTail = alphaAirfoilsHTail;
	}

	public void setClDistributionAirfoilsHTail(List<List<Double>> clDistributionAirfoilsHTail) {
		this.clDistributionAirfoilsHTail = clDistributionAirfoilsHTail;
	}

	public void setHTailCLAirfoilsDistribution(List<List<Double>> _hTailCLAirfoilsDistribution) {
		this._hTailCLAirfoilsDistribution = _hTailCLAirfoilsDistribution;
	}

	public List<Double> getWingXACBreakPoints() {
		return _wingXACBreakPoints;
	}

	public List<Double> getWingXACDistribution() {
		return _wingXACDistribution;
	}

	public List<Double> getWingCmACBreakPoints() {
		return _wingCmACBreakPoints;
	}

	public List<Double> getWingCmACDistribution() {
		return _wingCmC4Distribution;
	}

	public List<Double> getHTailXACBreakPoints() {
		return _hTailXACBreakPoints;
	}

	public List<Double> getHTailXACDistribution() {
		return _hTailXACDistribution;
	}

	public List<Double> geHTailCmACBreakPoints() {
		return _hTailCmACBreakPoints;
	}

	public List<Double> getHTailCmACDistribution() {
		return _hTailCmC4Distribution;
	}

	public void setWingXACBreakPoints(List<Double> _wingXACBreakPoints) {
		this._wingXACBreakPoints = _wingXACBreakPoints;
	}

	public void setWingXACDistribution(List<Double> _wingXACDistribution) {
		this._wingXACDistribution = _wingXACDistribution;
	}

	public void setWingCmACBreakPoints(List<Double> _wingCmACBreakPoints) {
		this._wingCmACBreakPoints = _wingCmACBreakPoints;
	}

	public void setWingCmACDistribution(List<Double> _wingCmACDistribution) {
		this._wingCmC4Distribution = _wingCmACDistribution;
	}

	public void setHTailXACBreakPoints(List<Double> _hTailXACBreakPoints) {
		this._hTailXACBreakPoints = _hTailXACBreakPoints;
	}

	public void setHTailXACDistribution(List<Double> _hTailXACDistribution) {
		this._hTailXACDistribution = _hTailXACDistribution;
	}

	public void setHTailCmACBreakPoints(List<Double> _hTailCmACBreakPoints) {
		this._hTailCmACBreakPoints = _hTailCmACBreakPoints;
	}

	public void setHTailCmACDistribution(List<Double> _hTailCmACDistribution) {
		this._hTailCmC4Distribution = _hTailCmACDistribution;
	}

	public List<Double> getWingMomentumPole() {
		return _wingMomentumPole;
	}

	public List<Double> getHTailMomentumPole() {
		return _hTailMomentumPole;
	}

	public void setWingMomentumPole(List<Double> _wingMomentumPole) {
		this._wingMomentumPole = _wingMomentumPole;
	}

	public void setHTailMomentumPole(List<Double> _hTailMomentumPole) {
		this._hTailMomentumPole = _hTailMomentumPole;
	}

	public List<Amount<Angle>> getAlphaWingForDistribution() {
		return _alphaWingForDistribution;
	}

	public void setAlphaWingForDistribution(List<Amount<Angle>> _alphaWingForDistribution) {
		this._alphaWingForDistribution = _alphaWingForDistribution;
	}

	public List<Amount<Angle>> getAphaHorizontalTailForDistribution() {
		return _alphaHorizontalTailForDistribution;
	}

	public void setAlphaHorizontalTailForDistribution(List<Amount<Angle>> _alphaHorizontalTailForDistribution) {
		this._alphaHorizontalTailForDistribution = _alphaHorizontalTailForDistribution;
	}

	public Double getWingFinalMomentumPole() {
		return _wingFinalMomentumPole;
	}

	public Double getHTailFinalMomentumPole() {
		return _hTailFinalMomentumPole;
	}

	public void setWingFinalMomentumPole(Double _wingFinalMomentumPole) {
		this._wingFinalMomentumPole = _wingFinalMomentumPole;
	}

	public void setHTailFinalMomentumPole(Double _hTailFinalMomentumPole) {
		this._hTailFinalMomentumPole = _hTailFinalMomentumPole;
	}

	public Amount<Length> getVerticalTailSpan() {
		return _verticalTailSpan;
	}

	public void setVerticalTailSpan(Amount<Length> _verticalTailSpan) {
		this._verticalTailSpan = _verticalTailSpan;
	}

	public MethodEnum getWingairfoilMomentCoefficientCurve() {
		return _wingairfoilMomentCoefficientCurve;
	}

	public void setWingairfoilMomentCoefficientCurve(MethodEnum _wingairfoilMomentCoefficientCurve) {
		this._wingairfoilMomentCoefficientCurve = _wingairfoilMomentCoefficientCurve;
	}

	public List<List<Double>> getWingCLMomentAirfoilInput() {
		return _wingCLMomentAirfoilInput;
	}

	public List<List<Double>> getWingCMMomentAirfoilInput() {
		return _wingCMMomentAirfoilInput;
	}

	public List<Double> getWingCLMomentAirfoilOutput() {
		return _wingCLMomentAirfoilOutput;
	}

	public List<List<Double>> getWingCMMomentAirfoilOutput() {
		return _wingCMMomentAirfoilOutput;
	}

	public void setWingCLMomentAirfoilInput(List<List<Double>> _wingCLMomentAirfoilInput) {
		this._wingCLMomentAirfoilInput = _wingCLMomentAirfoilInput;
	}

	public void setWingCMMomentAirfoilInput(List<List<Double>> _wingCMMomentAirfoilInput) {
		this._wingCMMomentAirfoilInput = _wingCMMomentAirfoilInput;
	}

	public void setWingCLMomentAirfoilOutput(List<Double> _wingCLMomentAirfoilOutput) {
		this._wingCLMomentAirfoilOutput = _wingCLMomentAirfoilOutput;
	}

	public void setWingCMMomentAirfoilOutput(List<List<Double>> _wingCMMomentAirfoilOutput) {
		this._wingCMMomentAirfoilOutput = _wingCMMomentAirfoilOutput;
	}

	public Map<Amount<Angle>, Double> getDeltaCD0Elevator() {
		return _deltaCD0Elevator;
	}

	public void setDeltaCD0Elevator(Map<Amount<Angle>, Double> _deltaCD0Elevator) {
		this._deltaCD0Elevator = _deltaCD0Elevator;
	}

	public List<Double> getWingYAdimensionalBreakPointsCLEAN() {
		return _wingYAdimensionalBreakPointsCLEAN;
	}

	public List<Amount<Length>> getWingYBreakPointsCLEAN() {
		return _wingYBreakPointsCLEAN;
	}

	public List<Double> getWingYAdimensionalDistributionCLEAN() {
		return _wingYAdimensionalDistributionCLEAN;
	}

	public List<Amount<Length>> getWingYDistributionCLEAN() {
		return _wingYDistributionCLEAN;
	}

	public List<Amount<Length>> getWingChordsBreakPointsCLEAN() {
		return _wingChordsBreakPointsCLEAN;
	}

	public List<Amount<Length>> getWingChordsDistributionCLEAN() {
		return _wingChordsDistributionCLEAN;
	}

	public List<Amount<Length>> getWingXleBreakPointsCLEAN() {
		return _wingXleBreakPointsCLEAN;
	}

	public List<Amount<Length>> getWingXleDistributionCLEAN() {
		return _wingXleDistributionCLEAN;
	}

	public List<Amount<Angle>> getWingTwistBreakPointsCLEAN() {
		return _wingTwistBreakPointsCLEAN;
	}

	public List<Amount<Angle>> getWingTwistDistributionCLEAN() {
		return _wingTwistDistributionCLEAN;
	}

	public List<Amount<Angle>> getWingDihedralBreakPointsCLEAN() {
		return _wingDihedralBreakPointsCLEAN;
	}

	public List<Amount<Angle>> getWingDihedralDistributionCLEAN() {
		return _wingDihedralDistributionCLEAN;
	}

	public List<Amount<Angle>> getWingAlphaZeroLiftBreakPointsCLEAN() {
		return _wingAlphaZeroLiftBreakPointsCLEAN;
	}

	public List<Amount<Angle>> getWingAlphaZeroLiftDistributionCLEAN() {
		return _wingAlphaZeroLiftDistributionCLEAN;
	}

	public List<Amount<Angle>> getWingAlphaStarBreakPointsCLEAN() {
		return _wingAlphaStarBreakPointsCLEAN;
	}

	public List<Amount<Angle>> getWingAlphaStarDistributionCLEAN() {
		return _wingAlphaStarDistributionCLEAN;
	}

	public List<Double> getWingClMaxBreakPointsCLEAN() {
		return _wingClMaxBreakPointsCLEAN;
	}

	public List<Double> getWingClMaxDistributionCLEAN() {
		return _wingClMaxDistributionCLEAN;
	}

	public List<Double> getWingCl0BreakPointsCLEAN() {
		return _wingCl0BreakPointsCLEAN;
	}

	public List<Double> getWingCl0DistributionCLEAN() {
		return _wingCl0DistributionCLEAN;
	}


	public void setWingYAdimensionalBreakPointsCLEAN(List<Double> _wingYAdimensionalBreakPointsCLEAN) {
		this._wingYAdimensionalBreakPointsCLEAN = _wingYAdimensionalBreakPointsCLEAN;
	}

	public void setWingYBreakPointsCLEAN(List<Amount<Length>> _wingYBreakPointsCLEAN) {
		this._wingYBreakPointsCLEAN = _wingYBreakPointsCLEAN;
	}

	public void setWingYAdimensionalDistributionCLEAN(List<Double> _wingYAdimensionalDistributionCLEAN) {
		this._wingYAdimensionalDistributionCLEAN = _wingYAdimensionalDistributionCLEAN;
	}

	public void setWingYDistributionCLEAN(List<Amount<Length>> _wingYDistributionCLEAN) {
		this._wingYDistributionCLEAN = _wingYDistributionCLEAN;
	}

	public void setWingChordsBreakPointsCLEAN(List<Amount<Length>> _wingChordsBreakPointsCLEAN) {
		this._wingChordsBreakPointsCLEAN = _wingChordsBreakPointsCLEAN;
	}

	public void setWingChordsDistributionCLEAN(List<Amount<Length>> _wingChordsDistributionCLEAN) {
		this._wingChordsDistributionCLEAN = _wingChordsDistributionCLEAN;
	}

	public void setWingXleBreakPointsCLEAN(List<Amount<Length>> _wingXleBreakPointsCLEAN) {
		this._wingXleBreakPointsCLEAN = _wingXleBreakPointsCLEAN;
	}

	public void setWingXleDistributionCLEAN(List<Amount<Length>> _wingXleDistributionCLEAN) {
		this._wingXleDistributionCLEAN = _wingXleDistributionCLEAN;
	}

	public void setWingTwistBreakPointsCLEAN(List<Amount<Angle>> _wingTwistBreakPointsCLEAN) {
		this._wingTwistBreakPointsCLEAN = _wingTwistBreakPointsCLEAN;
	}

	public void setWingTwistDistributionCLEAN(List<Amount<Angle>> _wingTwistDistributionCLEAN) {
		this._wingTwistDistributionCLEAN = _wingTwistDistributionCLEAN;
	}

	public void setWingDihedralBreakPointsCLEAN(List<Amount<Angle>> _wingDihedralBreakPointsCLEAN) {
		this._wingDihedralBreakPointsCLEAN = _wingDihedralBreakPointsCLEAN;
	}

	public void setWingDihedralDistributionCLEAN(List<Amount<Angle>> _wingDihedralDistributionCLEAN) {
		this._wingDihedralDistributionCLEAN = _wingDihedralDistributionCLEAN;
	}

	public void setWingAlphaZeroLiftBreakPointsCLEAN(List<Amount<Angle>> _wingAlphaZeroLiftBreakPointsCLEAN) {
		this._wingAlphaZeroLiftBreakPointsCLEAN = _wingAlphaZeroLiftBreakPointsCLEAN;
	}

	public void setWingAlphaZeroLiftDistributionCLEAN(List<Amount<Angle>> _wingAlphaZeroLiftDistributionCLEAN) {
		this._wingAlphaZeroLiftDistributionCLEAN = _wingAlphaZeroLiftDistributionCLEAN;
	}

	public void setWingAlphaStarBreakPointsCLEAN(List<Amount<Angle>> _wingAlphaStarBreakPointsCLEAN) {
		this._wingAlphaStarBreakPointsCLEAN = _wingAlphaStarBreakPointsCLEAN;
	}

	public void setWingAlphaStarDistributionCLEAN(List<Amount<Angle>> _wingAlphaStarDistributionCLEAN) {
		this._wingAlphaStarDistributionCLEAN = _wingAlphaStarDistributionCLEAN;
	}

	public void setWingClMaxBreakPointsCLEAN(List<Double> _wingClMaxBreakPointsCLEAN) {
		this._wingClMaxBreakPointsCLEAN = _wingClMaxBreakPointsCLEAN;
	}

	public void setWingClMaxDistributionCLEAN(List<Double> _wingClMaxDistributionCLEAN) {
		this._wingClMaxDistributionCLEAN = _wingClMaxDistributionCLEAN;
	}

	public void setWingCl0BreakPointsCLEAN(List<Double> _wingCl0BreakPointsCLEAN) {
		this._wingCl0BreakPointsCLEAN = _wingCl0BreakPointsCLEAN;
	}

	public void setWingCl0DistributionCLEAN(List<Double> _wingCl0DistributionCLEAN) {
		this._wingCl0DistributionCLEAN = _wingCl0DistributionCLEAN;
	}


	public List<Double> getWingClAlphaBreakPointsDegCLEAN() {
		return _wingClAlphaBreakPointsDegCLEAN;
	}

	public void setWingClAlphaBreakPointsDegCLEAN(List<Double> _wingClAlphaBreakPointsDegCLEAN) {
		this._wingClAlphaBreakPointsDegCLEAN = _wingClAlphaBreakPointsDegCLEAN;
	}

	public List<Double> getWingClAlphaDistributionDegCLEAN() {
		return _wingClAlphaDistributionDegCLEAN;
	}

	public List<Double> getWingMaxThicknessBreakPointsCLEAN() {
		return _wingMaxThicknessBreakPointsCLEAN;
	}

	public List<Double> getWingMaxThicknessDistributionCLEAN() {
		return _wingMaxThicknessDistributionCLEAN;
	}

	public List<Amount<Length>> getWingLERadiusBreakPointsCLEAN() {
		return _wingLERadiusBreakPointsCLEAN;
	}

	public List<Amount<Length>> getWingLERadiusDistributionCLEAN() {
		return _wingLERadiusDistributionCLEAN;
	}

	public void setWingClAlphaDistributionDegCLEAN(List<Double> _wingClAlphaDistributionDegCLEAN) {
		this._wingClAlphaDistributionDegCLEAN = _wingClAlphaDistributionDegCLEAN;
	}

	public void setWingMaxThicknessBreakPointsCLEAN(List<Double> _wingMaxThicknessBreakPointsCLEAN) {
		this._wingMaxThicknessBreakPointsCLEAN = _wingMaxThicknessBreakPointsCLEAN;
	}

	public void setWingMaxThicknessDistributionCLEAN(List<Double> _wingMaxThicknessDistributionCLEAN) {
		this._wingMaxThicknessDistributionCLEAN = _wingMaxThicknessDistributionCLEAN;
	}

	public void setWingLERadiusBreakPointsCLEAN(List<Amount<Length>> _wingLERadiusBreakPointsCLEAN) {
		this._wingLERadiusBreakPointsCLEAN = _wingLERadiusBreakPointsCLEAN;
	}

	public void setWingLERadiusDistributionCLEAN(List<Amount<Length>> _wingLERadiusDistributionCLEAN) {
		this._wingLERadiusDistributionCLEAN = _wingLERadiusDistributionCLEAN;
	}

	public int getWingNumberOfGivenSectionsCLEAN() {
		return _wingNumberOfGivenSectionsCLEAN;
	}

	public void setWingNumberOfGivenSectionsCLEAN(int _wingNumberOfGivenSectionsCLEAN) {
		this._wingNumberOfGivenSectionsCLEAN = _wingNumberOfGivenSectionsCLEAN;
	}

	public MethodEnum getDeltaDueToFlapMethod() {
		return _deltaDueToFlapMethod;
	}

	public void setDeltaDueToFlapMethod(MethodEnum _deltaDueToFlapMethod) {
		this._deltaDueToFlapMethod = _deltaDueToFlapMethod;
	}

	public List<Amount<Angle>> getAlphasFuselagePolar() {
		return alphasFuselagePolar;
	}

	public List<Double> getCdDistributionFuselage() {
		return cdDistributionFuselage;
	}

	public void setAlphasFuselagePolar(List<Amount<Angle>> alphasFuselagePolar) {
		this.alphasFuselagePolar = alphasFuselagePolar;
	}

	public void setCdDistributionFuselage(List<Double> cdDistributionFuselage) {
		this.cdDistributionFuselage = cdDistributionFuselage;
	}

	public Double getWingOswaldFactor() {
		return _wingOswaldFactor;
	}

	public void setWingOswaldFactor(Double _wingOswaldFactor) {
		this._wingOswaldFactor = _wingOswaldFactor;
	}

	public Double getHTailOswaldFactor() {
		return _hTailOswaldFactor;
	}

	public void setHTailOswaldFactor(Double _hTailOswaldFactor) {
		this._hTailOswaldFactor = _hTailOswaldFactor;
	}

	public double getDeltaCD0Miscellaneus() {
		return _deltaCD0Miscellaneus;
	}

	public void setDeltaCD0Miscellaneus(double _deltaCD0Miscellaneus) {
		this._deltaCD0Miscellaneus = _deltaCD0Miscellaneus;
	}

	public double get_cDLandingGear() {
		return _cDLandingGear;
	}

	public void set_cDLandingGear(double _cDLandingGear) {
		this._cDLandingGear = _cDLandingGear;
	}

	public MethodEnum getFuselageMomentMethod() {
		return _fuselageMomentMethod;
	}

	public void setFuselageMomentMethod(MethodEnum _fuselageMomentMethod) {
		this._fuselageMomentMethod = _fuselageMomentMethod;
	}

	public double getCM0fuselage() {
		return _cM0fuselage;
	}

	public double getCMalphafuselage() {
		return _cMalphafuselage;
	}

	public void setCM0fuselage(double _cM0fuselage) {
		this._cM0fuselage = _cM0fuselage;
	}

	public void setCMalphafuselage(double _cMalphafuselage) {
		this._cMalphafuselage = _cMalphafuselage;
	}

	public Amount<Length> get_zLandingGear() {
		return _zLandingGear;
	}

	public void set_zLandingGear(Amount<Length> _zLandingGear) {
		this._zLandingGear = _zLandingGear;
	}

	public Double get_deltaCMc4ListElevator() {
		return _deltaCMc4ListElevator;
	}

	public void set_deltaCMc4ListElevator(Double _deltaCMc4ListElevator) {
		this._deltaCMc4ListElevator = _deltaCMc4ListElevator;
	}

	public Double get_deltaCMc4Elevator() {
		return _deltaCMc4Elevator;
	}

	public void set_deltaCMc4Elevator(Double _deltaCMc4Elevator) {
		this._deltaCMc4Elevator = _deltaCMc4Elevator;
	}

	public MethodEnum get_horizontalWingCL() {
		return _horizontalTailCL;
	}

	public void set_horizontalWingCL(MethodEnum _horizontalWingCL) {
		this._horizontalTailCL = _horizontalWingCL;
	}

	public Double[] getWingliftCoefficient3DCurve() {
		return _wingliftCoefficient3DCurve;
	}

	public void setWingliftCoefficient3DCurve(Double[] _wingliftCoefficient3DCurve) {
		this._wingliftCoefficient3DCurve = _wingliftCoefficient3DCurve;
	}

	public double[] getWingliftCoefficientDistributionatCLMax() {
		return _wingliftCoefficientDistributionatCLMax;
	}

	public void setWingliftCoefficientDistributionatCLMax(double[] _wingliftCoefficientDistributionatCLMax) {
		this._wingliftCoefficientDistributionatCLMax = _wingliftCoefficientDistributionatCLMax;
	}

	public Amount<?> getFuselageWingClAlphaDeg() {
		return _fuselageWingClAlphaDeg;
	}

	public void setFuselageWingClAlphaDeg(Amount<?> _fuselageWingClAlphaDeg) {
		this._fuselageWingClAlphaDeg = _fuselageWingClAlphaDeg;
	}

	public Double[] getFuselagewingliftCoefficient3DCurve() {
		return _fuselagewingliftCoefficient3DCurve;
	}

	public void setFuselagewingliftCoefficient3DCurve(Double[] _fuselagewingliftCoefficient3DCurve) {
		this._fuselagewingliftCoefficient3DCurve = _fuselagewingliftCoefficient3DCurve;
	}

	public String get_aircraftName() {
		return _aircraftName;
	}

	public Amount<Length> get_xCGAircraft() {
		return _xCGAircraft;
	}

	public Amount<Length> get_yCGAircraft() {
		return _yCGAircraft;
	}

	public Amount<Length> get_zCGAircraft() {
		return _zCGAircraft;
	}

	public Amount<Length> get_altitude() {
		return _altitude;
	}

	public Double get_machCurrent() {
		return _machCurrent;
	}

	public Double get_reynoldsCurrent() {
		return _reynoldsCurrent;
	}

	public Amount<Angle> get_alphaBodyInitial() {
		return _alphaBodyInitial;
	}

	public Amount<Angle> get_alphaBodyFinal() {
		return _alphaBodyFinal;
	}

	public int get_numberOfAlphasBody() {
		return _numberOfAlphasBody;
	}

	public List<Amount<Angle>> get_alphasBody() {
		return _alphasBody;
	}

	public ConditionEnum get_theCondition() {
		return _theCondition;
	}

	public boolean is_downwashConstant() {
		return _downwashConstant;
	}

	public List<Double> get_wingMomentumPole() {
		return _wingMomentumPole;
	}

	public List<Double> get_hTailMomentumPole() {
		return _hTailMomentumPole;
	}

	public List<Amount<Angle>> get_alphaWingForDistribution() {
		return _alphaWingForDistribution;
	}

	public List<Amount<Angle>> get_alphaHorizontalTailForDistribution() {
		return _alphaHorizontalTailForDistribution;
	}

	public Double get_dynamicPressureRatio() {
		return _dynamicPressureRatio;
	}

	public Amount<Length> get_xApexWing() {
		return _xApexWing;
	}

	public Amount<Length> get_yApexWing() {
		return _yApexWing;
	}

	public Amount<Length> get_zApexWing() {
		return _zApexWing;
	}

	public Amount<Length> get_zACRootWing() {
		return _zACRootWing;
	}

	public Amount<Area> get_wingSurface() {
		return _wingSurface;
	}

	public Double get_wingAspectRatio() {
		return _wingAspectRatio;
	}

	public Amount<Length> get_wingSpan() {
		return _wingSpan;
	}

	public Amount<Length> get_wingSemiSpan() {
		return _wingSemiSpan;
	}

	public int get_wingNumberOfPointSemiSpanWise() {
		return _wingNumberOfPointSemiSpanWise;
	}

	public Double get_wingAdimentionalKinkStation() {
		return _wingAdimentionalKinkStation;
	}

	public int get_wingNumberOfGivenSections() {
		return _wingNumberOfGivenSections;
	}

	public int get_wingNumberOfGivenSectionsCLEAN() {
		return _wingNumberOfGivenSectionsCLEAN;
	}

	public Amount<Angle> get_wingAngleOfIncidence() {
		return _wingAngleOfIncidence;
	}

	public Double get_wingTaperRatio() {
		return _wingTaperRatio;
	}

	public Amount<Angle> get_wingSweepQuarterChord() {
		return _wingSweepQuarterChord;
	}

	public Amount<Angle> get_wingSweepLE() {
		return _wingSweepLE;
	}

	public Double get_wingVortexSemiSpanToSemiSpanRatio() {
		return _wingVortexSemiSpanToSemiSpanRatio;
	}

	public double getcLAlphaMachZero() {
		return cLAlphaMachZero;
	}

	public AirfoilFamilyEnum get_wingMeanAirfoilFamily() {
		return _wingMeanAirfoilFamily;
	}

	public Double get_wingMaxThicknessMeanAirfoil() {
		return _wingMaxThicknessMeanAirfoil;
	}

	public MethodEnum get_wingairfoilLiftCoefficientCurve() {
		return _wingairfoilLiftCoefficientCurve;
	}

	public List<List<Amount<Angle>>> get_wingInducedAngleOfAttack() {
		return _wingInducedAngleOfAttack;
	}

	public List<List<Double>> get_wingCLAirfoilsDistribution() {
		return _wingCLAirfoilsDistribution;
	}

	public List<List<Double>> get_wingCLAirfoilsDistributionFinal() {
		return _wingCLAirfoilsDistributionFinal;
	}

	public MethodEnum get_wingairfoilMomentCoefficientCurve() {
		return _wingairfoilMomentCoefficientCurve;
	}

	public List<List<Double>> get_wingCLMomentAirfoilInput() {
		return _wingCLMomentAirfoilInput;
	}

	public List<List<Double>> get_wingCMMomentAirfoilInput() {
		return _wingCMMomentAirfoilInput;
	}

	public List<Double> get_wingCLMomentAirfoilOutput() {
		return _wingCLMomentAirfoilOutput;
	}

	public List<List<Double>> get_wingCMMomentAirfoilOutput() {
		return _wingCMMomentAirfoilOutput;
	}

	public List<Double> get_wingYAdimensionalBreakPoints() {
		return _wingYAdimensionalBreakPoints;
	}

	public List<Amount<Length>> get_wingYBreakPoints() {
		return _wingYBreakPoints;
	}

	public List<Double> get_wingYAdimensionalDistribution() {
		return _wingYAdimensionalDistribution;
	}

	public List<Amount<Length>> get_wingYDistribution() {
		return _wingYDistribution;
	}

	public List<Amount<Length>> get_wingChordsBreakPoints() {
		return _wingChordsBreakPoints;
	}

	public List<Amount<Length>> get_wingChordsDistribution() {
		return _wingChordsDistribution;
	}

	public List<Amount<Length>> get_wingXleBreakPoints() {
		return _wingXleBreakPoints;
	}

	public List<Amount<Length>> get_wingXleDistribution() {
		return _wingXleDistribution;
	}

	public List<Amount<Angle>> get_wingTwistBreakPoints() {
		return _wingTwistBreakPoints;
	}

	public List<Amount<Angle>> get_wingTwistDistribution() {
		return _wingTwistDistribution;
	}

	public List<Amount<Angle>> get_wingDihedralBreakPoints() {
		return _wingDihedralBreakPoints;
	}

	public List<Amount<Angle>> get_wingDihedralDistribution() {
		return _wingDihedralDistribution;
	}

	public List<Amount<Angle>> get_wingAlphaZeroLiftBreakPoints() {
		return _wingAlphaZeroLiftBreakPoints;
	}

	public List<Amount<Angle>> get_wingAlphaZeroLiftDistribution() {
		return _wingAlphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> get_wingAlphaStarBreakPoints() {
		return _wingAlphaStarBreakPoints;
	}

	public List<Amount<Angle>> get_wingAlphaStarDistribution() {
		return _wingAlphaStarDistribution;
	}

	public List<Double> get_wingClMaxBreakPoints() {
		return _wingClMaxBreakPoints;
	}

	public List<Double> get_wingClMaxDistribution() {
		return _wingClMaxDistribution;
	}

	public List<Double> get_wingCl0BreakPoints() {
		return _wingCl0BreakPoints;
	}

	public List<Double> get_wingCl0Distribution() {
		return _wingCl0Distribution;
	}

	public List<Double> get_wingXACBreakPoints() {
		return _wingXACBreakPoints;
	}

	public List<Double> get_wingXACDistribution() {
		return _wingXACDistribution;
	}

	public List<Double> get_wingCmACBreakPoints() {
		return _wingCmACBreakPoints;
	}

	public List<Double> get_wingCmC4Distribution() {
		return _wingCmC4Distribution;
	}

	public List<Double> get_wingYAdimensionalBreakPointsCLEAN() {
		return _wingYAdimensionalBreakPointsCLEAN;
	}

	public List<Amount<Length>> get_wingYBreakPointsCLEAN() {
		return _wingYBreakPointsCLEAN;
	}

	public List<Double> get_wingYAdimensionalDistributionCLEAN() {
		return _wingYAdimensionalDistributionCLEAN;
	}

	public List<Amount<Length>> get_wingYDistributionCLEAN() {
		return _wingYDistributionCLEAN;
	}

	public List<Amount<Length>> get_wingChordsBreakPointsCLEAN() {
		return _wingChordsBreakPointsCLEAN;
	}

	public List<Amount<Length>> get_wingChordsDistributionCLEAN() {
		return _wingChordsDistributionCLEAN;
	}

	public List<Amount<Length>> get_wingXleBreakPointsCLEAN() {
		return _wingXleBreakPointsCLEAN;
	}

	public List<Amount<Length>> get_wingXleDistributionCLEAN() {
		return _wingXleDistributionCLEAN;
	}

	public List<Amount<Angle>> get_wingTwistBreakPointsCLEAN() {
		return _wingTwistBreakPointsCLEAN;
	}

	public List<Amount<Angle>> get_wingTwistDistributionCLEAN() {
		return _wingTwistDistributionCLEAN;
	}

	public List<Amount<Angle>> get_wingDihedralBreakPointsCLEAN() {
		return _wingDihedralBreakPointsCLEAN;
	}

	public List<Amount<Angle>> get_wingDihedralDistributionCLEAN() {
		return _wingDihedralDistributionCLEAN;
	}

	public List<Amount<Angle>> get_wingAlphaZeroLiftBreakPointsCLEAN() {
		return _wingAlphaZeroLiftBreakPointsCLEAN;
	}

	public List<Amount<Angle>> get_wingAlphaZeroLiftDistributionCLEAN() {
		return _wingAlphaZeroLiftDistributionCLEAN;
	}

	public List<Amount<Angle>> get_wingAlphaStarBreakPointsCLEAN() {
		return _wingAlphaStarBreakPointsCLEAN;
	}

	public List<Amount<Angle>> get_wingAlphaStarDistributionCLEAN() {
		return _wingAlphaStarDistributionCLEAN;
	}

	public List<Double> get_wingClMaxBreakPointsCLEAN() {
		return _wingClMaxBreakPointsCLEAN;
	}

	public List<Double> get_wingClMaxDistributionCLEAN() {
		return _wingClMaxDistributionCLEAN;
	}

	public List<Double> get_wingCl0BreakPointsCLEAN() {
		return _wingCl0BreakPointsCLEAN;
	}

	public List<Double> get_wingCl0DistributionCLEAN() {
		return _wingCl0DistributionCLEAN;
	}

	public List<Double> get_wingClAlphaBreakPointsDegCLEAN() {
		return _wingClAlphaBreakPointsDegCLEAN;
	}

	public List<Double> get_wingClAlphaDistributionDegCLEAN() {
		return _wingClAlphaDistributionDegCLEAN;
	}

	public List<Double> get_wingMaxThicknessBreakPointsCLEAN() {
		return _wingMaxThicknessBreakPointsCLEAN;
	}

	public List<Double> get_wingMaxThicknessDistributionCLEAN() {
		return _wingMaxThicknessDistributionCLEAN;
	}

	public List<Amount<Length>> get_wingLERadiusBreakPointsCLEAN() {
		return _wingLERadiusBreakPointsCLEAN;
	}

	public List<Amount<Length>> get_wingLERadiusDistributionCLEAN() {
		return _wingLERadiusDistributionCLEAN;
	}

	public List<Double> get_wingClAlphaDistributionDeg() {
		return _wingClAlphaDistributionDeg;
	}

	public List<Amount<Length>> get_wingLERadiusBreakPoints() {
		return _wingLERadiusBreakPoints;
	}

	public List<Amount<Length>> get_wingLERadiusDistribution() {
		return _wingLERadiusDistribution;
	}

	public List<Amount<Length>> get_wingYLEBreakPoints() {
		return _wingYLEBreakPoints;
	}

	public List<Amount<Length>> get_wingYLEDistribution() {
		return _wingYLEDistribution;
	}

	public int get_wingNumberOfFlaps() {
		return _wingNumberOfFlaps;
	}

	public int get_wingNumberOfSlats() {
		return _wingNumberOfSlats;
	}

	public List<Amount<Angle>> get_wingDeltaFlap() {
		return _wingDeltaFlap;
	}

	public List<FlapTypeEnum> get_wingFlapType() {
		return _wingFlapType;
	}

	public List<Double> get_wingEtaInFlap() {
		return _wingEtaInFlap;
	}

	public List<Double> get_wingEtaOutFlap() {
		return _wingEtaOutFlap;
	}

	public List<Double> get_wingFlapCfC() {
		return _wingFlapCfC;
	}

	public List<Amount<Angle>> get_wingDeltaSlat() {
		return _wingDeltaSlat;
	}

	public List<Double> get_wingEtaInSlat() {
		return _wingEtaInSlat;
	}

	public List<Double> get_wingEtaOutSlat() {
		return _wingEtaOutSlat;
	}

	public List<Double> get_wingSlatCsC() {
		return _wingSlatCsC;
	}

	public List<Double> get_wingCExtCSlat() {
		return _wingCExtCSlat;
	}

	public List<Double> get_wingLeRadiusCSLat() {
		return _wingLeRadiusCSLat;
	}

	public Map<Double, Map<Double, Double[]>> get_supermappa() {
		return _supermappa;
	}

	public Amount<Length> get_fuselageDiameter() {
		return _fuselageDiameter;
	}

	public Amount<Length> get_fuselageLength() {
		return _fuselageLength;
	}

	public Double get_fuselageNoseFinessRatio() {
		return _fuselageNoseFinessRatio;
	}

	public Double get_fuselageFinessRatio() {
		return _fuselageFinessRatio;
	}

	public Double get_fuselageTailFinessRatio() {
		return _fuselageTailFinessRatio;
	}

	public Amount<Angle> get_fuselageWindshieldAngle() {
		return _fuselageWindshieldAngle;
	}

	public Amount<Angle> get_fuselageUpSweepAngle() {
		return _fuselageUpSweepAngle;
	}

	public Double get_fuselageXPercentPositionPole() {
		return _fuselageXPercentPositionPole;
	}

	public Amount<Area> get_fuselageFrontSurface() {
		return _fuselageFrontSurface;
	}

	public double get_cM0fuselage() {
		return _cM0fuselage;
	}

	public double get_cMalphafuselage() {
		return _cMalphafuselage;
	}

	public List<Double> getCdDistributionFuselageFinal() {
		return cdDistributionFuselageFinal;
	}

	public Amount<Length> get_xApexHTail() {
		return _xApexHTail;
	}

	public Amount<Length> get_yApexHTail() {
		return _yApexHTail;
	}

	public Amount<Length> get_zApexHTail() {
		return _zApexHTail;
	}

	public Amount<Length> get_verticalTailSpan() {
		return _verticalTailSpan;
	}

	public Amount<Area> get_hTailSurface() {
		return _hTailSurface;
	}

	public Double get_hTailAspectRatio() {
		return _hTailAspectRatio;
	}

	public Double get_hTailTaperRatio() {
		return _hTailTaperRatio;
	}

	public Amount<Length> get_hTailSpan() {
		return _hTailSpan;
	}

	public Amount<Length> get_hTailSemiSpan() {
		return _hTailSemiSpan;
	}

	public int get_hTailNumberOfPointSemiSpanWise() {
		return _hTailNumberOfPointSemiSpanWise;
	}

	public Double get_hTailadimentionalKinkStation() {
		return _hTailadimentionalKinkStation;
	}

	public int get_hTailnumberOfGivenSections() {
		return _hTailnumberOfGivenSections;
	}

	public Amount<Angle> get_hTailSweepLE() {
		return _hTailSweepLE;
	}

	public Amount<Angle> get_hTailSweepQuarterChord() {
		return _hTailSweepQuarterChord;
	}

	public AirfoilFamilyEnum get_hTailMeanAirfoilFamily() {
		return _hTailMeanAirfoilFamily;
	}

	public Double get_hTailMaxThicknessMeanAirfoil() {
		return _hTailMaxThicknessMeanAirfoil;
	}

	public Double get_hTailVortexSemiSpanToSemiSpanRatio() {
		return _hTailVortexSemiSpanToSemiSpanRatio;
	}

	public MethodEnum get_hTailairfoilLiftCoefficientCurve() {
		return _hTailairfoilLiftCoefficientCurve;
	}

	public List<List<Double>> get_hTailCLAirfoilsDistribution() {
		return _hTailCLAirfoilsDistribution;
	}

	public List<List<Amount<Angle>>> get_hTailInducedAngleOfAttack() {
		return _hTailInducedAngleOfAttack;
	}

	public List<List<Double>> get_hTailCLAirfoilsDistributionFinal() {
		return _hTailCLAirfoilsDistributionFinal;
	}

	public Amount<Length> get_hTailHorizontalDistanceACtoCG() {
		return _hTailHorizontalDistanceACtoCG;
	}

	public Amount<Length> get_hTailVerticalDistranceACtoCG() {
		return _hTailVerticalDistranceACtoCG;
	}

	public List<Double> get_hTailYAdimensionalBreakPoints() {
		return _hTailYAdimensionalBreakPoints;
	}

	public List<Amount<Length>> get_hTailYBreakPoints() {
		return _hTailYBreakPoints;
	}

	public List<Double> get_hTailYAdimensionalDistribution() {
		return _hTailYAdimensionalDistribution;
	}

	public Amount<Angle> get_hTailAngleOfIncidence() {
		return _hTailAngleOfIncidence;
	}

	public List<Amount<Length>> get_hTailYDistribution() {
		return _hTailYDistribution;
	}

	public List<Amount<Length>> get_hTailChordsBreakPoints() {
		return _hTailChordsBreakPoints;
	}

	public List<Amount<Length>> get_hTailChordsDistribution() {
		return _hTailChordsDistribution;
	}

	public List<Amount<Length>> get_hTailXleBreakPoints() {
		return _hTailXleBreakPoints;
	}

	public List<Amount<Length>> get_hTailXleDistribution() {
		return _hTailXleDistribution;
	}

	public List<Amount<Angle>> get_hTailTwistBreakPoints() {
		return _hTailTwistBreakPoints;
	}

	public List<Amount<Angle>> get_hTailTwistDistribution() {
		return _hTailTwistDistribution;
	}

	public List<Amount<Angle>> get_hTailDihedralBreakPoints() {
		return _hTailDihedralBreakPoints;
	}

	public List<Amount<Angle>> get_hTailDihedralDistribution() {
		return _hTailDihedralDistribution;
	}

	public List<Amount<Angle>> get_hTailAlphaZeroLiftBreakPoints() {
		return _hTailAlphaZeroLiftBreakPoints;
	}

	public List<Amount<Angle>> get_hTailAlphaZeroLiftDistribution() {
		return _hTailAlphaZeroLiftDistribution;
	}

	public List<Amount<Angle>> get_hTailAlphaStarBreakPoints() {
		return _hTailAlphaStarBreakPoints;
	}

	public List<Amount<Angle>> get_hTailAlphaStarDistribution() {
		return _hTailAlphaStarDistribution;
	}

	public List<Double> get_hTailClMaxBreakPoints() {
		return _hTailClMaxBreakPoints;
	}

	public List<Double> get_hTailClMaxDistribution() {
		return _hTailClMaxDistribution;
	}

	public List<Double> get_hTailMaxThicknessBreakPoints() {
		return _hTailMaxThicknessBreakPoints;
	}

	public List<Double> get_hTailMaxThicknessDistribution() {
		return _hTailMaxThicknessDistribution;
	}

	public List<Double> get_hTailClAlphaBreakPointsDeg() {
		return _hTailClAlphaBreakPointsDeg;
	}

	public List<Double> get_hTailCl0BreakPoints() {
		return _hTailCl0BreakPoints;
	}

	public List<Double> get_hTailCl0Distribution() {
		return _hTailCl0Distribution;
	}

	public List<Double> get_hTailClAlphaistributionDeg() {
		return _hTailClAlphaistributionDeg;
	}

	public List<Double> get_hTailXACBreakPoints() {
		return _hTailXACBreakPoints;
	}

	public List<Double> get_hTailXACDistribution() {
		return _hTailXACDistribution;
	}

	public List<Double> get_hTailCmACBreakPoints() {
		return _hTailCmACBreakPoints;
	}

	public List<Double> get_hTailCmC4Distribution() {
		return _hTailCmC4Distribution;
	}

	public List<Amount<Angle>> get_anglesOfElevatorDeflection() {
		return _anglesOfElevatorDeflection;
	}

	public FlapTypeEnum get_elevatorType() {
		return _elevatorType;
	}

	public Double get_elevatorEtaIn() {
		return _elevatorEtaIn;
	}

	public Double get_elevatorEtaOut() {
		return _elevatorEtaOut;
	}

	public Double get_elevatorCfC() {
		return _elevatorCfC;
	}

	public Amount<Angle> get_tiltingAngle() {
		return _tiltingAngle;
	}

	public MethodEnum get_wingDragMethod() {
		return _wingDragMethod;
	}

	public MethodEnum get_hTailDragMethod() {
		return _hTailDragMethod;
	}

	public List<AerodynamicAndStabilityPlotEnum> get_plotList() {
		return _plotList;
	}

	public boolean is_plotCheck() {
		return _plotCheck;
	}

	public StabilityExecutableCalculator getTheStabilityCalculator() {
		return theStabilityCalculator;
	}

	public Amount<Length> get_horizontalDistanceQuarterChordWingHTail() {
		return _horizontalDistanceQuarterChordWingHTail;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTail() {
		return _verticalDistanceZeroLiftDirectionWingHTail;
	}

	public Amount<Length> get_horizontalDistanceQuarterChordWingHTailNOANGLE() {
		return _horizontalDistanceQuarterChordWingHTailNOANGLE;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL() {
		return _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE() {
		return _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public String getDatabaseFolderPath() {
		return databaseFolderPath;
	}

	public String getAerodynamicDatabaseFileName() {
		return aerodynamicDatabaseFileName;
	}

	public String getHighLiftDatabaseFileName() {
		return highLiftDatabaseFileName;
	}

	public String getFusDesDatabaseFileName() {
		return fusDesDatabaseFileName;
	}

	public AerodynamicDatabaseReader getAeroDatabaseReader() {
		return aeroDatabaseReader;
	}

	public HighLiftDatabaseReader getHighLiftDatabaseReader() {
		return highLiftDatabaseReader;
	}

	public FusDesDatabaseReader getFusDesDatabaseReader() {
		return fusDesDatabaseReader;
	}

	public double[] getAlphaZeroLiftRad() {
		return alphaZeroLiftRad;
	}

	public double[] getTwistDistributionRad() {
		return twistDistributionRad;
	}

	public double[] getAlphaZeroLiftRadCLEAN() {
		return alphaZeroLiftRadCLEAN;
	}

	public double[] getTwistDistributionRadCLEAN() {
		return twistDistributionRadCLEAN;
	}

	public double[] getAlphaZeroLiftRadHTail() {
		return alphaZeroLiftRadHTail;
	}

	public double[] getTwistDistributionRadHTail() {
		return twistDistributionRadHTail;
	}

	public double get_deltaCD0Miscellaneus() {
		return _deltaCD0Miscellaneus;
	}

	public Amount<Length> get_landingGearArm() {
		return _landingGearArm;
	}

	public double getDimensionalOverKink() {
		return dimensionalOverKink;
	}

	public double getInfluenceAreaRoot() {
		return influenceAreaRoot;
	}

	public double getInfluenceAreaKink() {
		return influenceAreaKink;
	}

	public double getInfluenceAreaTip() {
		return influenceAreaTip;
	}

	public double getkRoot() {
		return kRoot;
	}

	public double getkKink() {
		return kKink;
	}

	public double getkTip() {
		return kTip;
	}

	public double getDimensionalOverKinkHTail() {
		return dimensionalOverKinkHTail;
	}

	public double getInfluenceAreaRootHTail() {
		return influenceAreaRootHTail;
	}

	public double getInfluenceAreaKinkHTail() {
		return influenceAreaKinkHTail;
	}

	public double getInfluenceAreaTipHTail() {
		return influenceAreaTipHTail;
	}

	public double getkRootHTail() {
		return kRootHTail;
	}

	public double getkKinkHTail() {
		return kKinkHTail;
	}

	public double getkTipHTail() {
		return kTipHTail;
	}

	public NasaBlackwell getTheNasaBlackwellCalculatorMachActualWingCLEAN() {
		return theNasaBlackwellCalculatorMachActualWingCLEAN;
	}

	public NasaBlackwell getTheNasaBlackwellCalculatorMachZeroCLEAN() {
		return theNasaBlackwellCalculatorMachZeroCLEAN;
	}

	public NasaBlackwell getTheNasaBlackwellCalculatorMachActualWing() {
		return theNasaBlackwellCalculatorMachActualWing;
	}

	public NasaBlackwell getTheNasaBlackwellCalculatorMachZero() {
		return theNasaBlackwellCalculatorMachZero;
	}

	public NasaBlackwell getTheNasaBlackwellCalculatorMachActualHTail() {
		return theNasaBlackwellCalculatorMachActualHTail;
	}

	public List<Double> get_downwashGradientConstantRoskam() {
		return _downwashGradientConstantRoskam;
	}

	public List<Amount<Angle>> get_downwashAngleConstantRoskam() {
		return _downwashAngleConstantRoskam;
	}

	public List<Double> get_downwashGradientConstantSlingerland() {
		return _downwashGradientConstantSlingerland;
	}

	public List<Amount<Angle>> get_downwashAngleConstantSlingerland() {
		return _downwashAngleConstantSlingerland;
	}

	public List<Double> get_downwashGradientVariableSlingerland() {
		return _downwashGradientVariableSlingerland;
	}

	public List<Amount<Angle>> get_downwashAngleVariableSlingerland() {
		return _downwashAngleVariableSlingerland;
	}

	public List<Amount<Angle>> get_downwashAngleVariableSlingerlandOld() {
		return _downwashAngleVariableSlingerlandOld;
	}

	public List<Amount<Length>> get_horizontalDistance() {
		return _horizontalDistance;
	}

	public List<Amount<Length>> get_verticalDistance() {
		return _verticalDistance;
	}

	public List<Amount<Length>> get_horizontalDistanceConstant() {
		return _horizontalDistanceConstant;
	}

	public List<Amount<Length>> get_verticalDistanceConstant() {
		return _verticalDistanceConstant;
	}

	public Amount<Angle> get_wingAlphaZeroLift() {
		return _wingAlphaZeroLift;
	}

	public Amount<Angle> get_wingalphaStar() {
		return _wingalphaStar;
	}

	public Amount<Angle> get_wingalphaMaxLinear() {
		return _wingalphaMaxLinear;
	}

	public Amount<Angle> get_wingalphaStall() {
		return _wingalphaStall;
	}

	public Double get_wingcLZero() {
		return _wingcLZero;
	}

	public Double get_wingcLStar() {
		return _wingcLStar;
	}

	public Double get_wingcLMax() {
		return _wingcLMax;
	}

	public Amount<?> get_wingclAlpha() {
		return _wingclAlpha;
	}

	public Double get_cLAtAlpha() {
		return _cLAtAlpha;
	}

	public Double[] get_wingliftCoefficient3DCurve() {
		return _wingliftCoefficient3DCurve;
	}

	public double[] get_wingliftCoefficientDistributionatCLMax() {
		return _wingliftCoefficientDistributionatCLMax;
	}

	public Double[] get_wingclAlphaArray() {
		return _wingclAlphaArray;
	}

	public Double[] get_wingclAlphaArrayHighLift() {
		return _wingclAlphaArrayHighLift;
	}

	public Double get_cLAtAlphaHighLift() {
		return _cLAtAlphaHighLift;
	}

	public Amount<Angle> get_alphaZeroLiftHighLift() {
		return _alphaZeroLiftHighLift;
	}

	public Amount<Angle> get_alphaStarHighLift() {
		return _alphaStarHighLift;
	}

	public Amount<Angle> get_alphaStallHighLift() {
		return _alphaStallHighLift;
	}

	public Double get_cLZeroHighLift() {
		return _cLZeroHighLift;
	}

	public Double get_cLStarHighLift() {
		return _cLStarHighLift;
	}

	public Double get_cLMaxHighLift() {
		return _cLMaxHighLift;
	}

	public Double get_cLAlphaHighLiftDEG() {
		return _cLAlphaHighLiftDEG;
	}

	public Double get_cD0HighLift() {
		return _cD0HighLift;
	}

	public List<Double> get_deltaCl0FlapList() {
		return _deltaCl0FlapList;
	}

	public Double get_deltaCl0Flap() {
		return _deltaCl0Flap;
	}

	public List<Double> get_deltaCL0FlapList() {
		return _deltaCL0FlapList;
	}

	public Double get_deltaCL0Flap() {
		return _deltaCL0Flap;
	}

	public List<Double> get_deltaClmaxFlapList() {
		return _deltaClmaxFlapList;
	}

	public Double get_deltaClmaxFlap() {
		return _deltaClmaxFlap;
	}

	public List<Double> get_deltaCLmaxFlapList() {
		return _deltaCLmaxFlapList;
	}

	public Double get_deltaCLmaxFlap() {
		return _deltaCLmaxFlap;
	}

	public List<Double> get_deltaClmaxSlatList() {
		return _deltaClmaxSlatList;
	}

	public Double get_deltaClmaxSlat() {
		return _deltaClmaxSlat;
	}

	public List<Double> get_deltaCLmaxSlatList() {
		return _deltaCLmaxSlatList;
	}

	public Double get_deltaCLmaxSlat() {
		return _deltaCLmaxSlat;
	}

	public List<Double> get_deltaCD0List() {
		return _deltaCD0List;
	}

	public Double get_deltaCD0() {
		return _deltaCD0;
	}

	public List<Double> get_deltaCMc4List() {
		return _deltaCMc4List;
	}

	public Double get_deltaCMc4() {
		return _deltaCMc4;
	}

	public Double[] get_alphaArrayPlotHighLift() {
		return _alphaArrayPlotHighLift;
	}

	public Double[] get_wingLiftCoefficient3DCurveHighLift() {
		return _wingLiftCoefficient3DCurveHighLift;
	}

	public Double[] get_wingLiftCoefficient3DCurveHighLiftWINGARRAY() {
		return _wingLiftCoefficient3DCurveHighLiftWINGARRAY;
	}

	public double[] get_wingLiftCoefficientModified() {
		return _wingLiftCoefficientModified;
	}

	public Amount<Angle> get_hTailAlphaZeroLift() {
		return _hTailAlphaZeroLift;
	}

	public Amount<Angle> get_hTailalphaStar() {
		return _hTailalphaStar;
	}

	public Amount<Angle> get_hTailalphaMaxLinear() {
		return _hTailalphaMaxLinear;
	}

	public Amount<Angle> get_hTailalphaStall() {
		return _hTailalphaStall;
	}

	public Double get_hTailcLZero() {
		return _hTailcLZero;
	}

	public Double get_hTailcLStar() {
		return _hTailcLStar;
	}

	public Double get_hTailcLMax() {
		return _hTailcLMax;
	}

	public Double get_hTailcLAlphaRad() {
		return _hTailcLAlphaRad;
	}

	public Double get_hTailcLAlphaDeg() {
		return _hTailcLAlphaDeg;
	}

	public Amount<?> get_hTailclAlpha() {
		return _hTailclAlpha;
	}

	public Double[] get_hTailliftCoefficient3DCurve() {
		return _hTailliftCoefficient3DCurve;
	}

	public double[] get_hTailliftCoefficientDistributionatCLMax() {
		return _hTailliftCoefficientDistributionatCLMax;
	}

	public Double[] get_hTailclAlphaArray() {
		return _hTailclAlphaArray;
	}

	public Double[] get_hTailclAlphaArrayHighLift() {
		return _hTailclAlphaArrayHighLift;
	}

	public Map<Amount<Angle>, Double> get_tauElevator() {
		return _tauElevator;
	}

	public Map<Amount<Angle>, Double> get_deltaCLMaxElevator() {
		return _deltaCLMaxElevator;
	}

	public Map<Amount<Angle>, Double> get_deltaCD0Elevator() {
		return _deltaCD0Elevator;
	}

	public Map<Amount<Angle>, Double> get_cLAlphaElevatorDeg() {
		return _cLAlphaElevatorDeg;
	}

	public Map<Amount<Angle>, Double> get_deltacLZeroElevator() {
		return _deltacLZeroElevator;
	}

	public Map<Amount<Angle>, Double[]> get_hTailLiftCoefficient3DCurveWithElevator() {
		return _hTailLiftCoefficient3DCurveWithElevator;
	}

	public Map<Amount<Angle>, Double[]> get_hTailDragCoefficient3DCurveWithElevator() {
		return _hTailDragCoefficient3DCurveWithElevator;
	}

	public Map<Amount<Angle>, Double> get_hTailcLMaxElevator() {
		return _hTailcLMaxElevator;
	}

	public Map<Amount<Angle>, Amount<Angle>> get_hTailalphaZeroLiftElevator() {
		return _hTailalphaZeroLiftElevator;
	}

	public Map<Amount<Angle>, Amount<Angle>> get_hTailalphaStarElevator() {
		return _hTailalphaStarElevator;
	}

	public Map<Amount<Angle>, Amount<Angle>> get_hTailalphaStallLiftElevator() {
		return _hTailalphaStallLiftElevator;
	}

	public Map<Amount<Angle>, Double> get_hTailCLZeroElevator() {
		return _hTailCLZeroElevator;
	}

	public Map<Amount<Angle>, Double> get_hTailCLStarElevator() {
		return _hTailCLStarElevator;
	}

	public Map<Amount<Angle>, Double> get_hTailCLAlphaElevator() {
		return _hTailCLAlphaElevator;
	}

	public Map<Amount<Angle>, Double> get_tauElevatorArray() {
		return _tauElevatorArray;
	}

	public List<Amount<Angle>> get_deltaEAnglesArray() {
		return _deltaEAnglesArray;
	}

	public Amount<Angle> get_wingAlphaZeroLiftCONDITION() {
		return _wingAlphaZeroLiftCONDITION;
	}

	public Amount<Angle> get_wingalphaStarCONDITION() {
		return _wingalphaStarCONDITION;
	}

	public Amount<Angle> get_wingalphaMaxLinearCONDITION() {
		return _wingalphaMaxLinearCONDITION;
	}

	public Amount<Angle> get_wingalphaStallCONDITION() {
		return _wingalphaStallCONDITION;
	}

	public Double get_wingcLZeroCONDITION() {
		return _wingcLZeroCONDITION;
	}

	public Double get_wingcLStarCONDITION() {
		return _wingcLStarCONDITION;
	}

	public Double get_wingcLMaxCONDITION() {
		return _wingcLMaxCONDITION;
	}

	public Double get_wingcLAlphaRadCONDITION() {
		return _wingcLAlphaRadCONDITION;
	}

	public Double get_wingcLAlphaDegCONDITION() {
		return _wingcLAlphaDegCONDITION;
	}

	public Amount<?> get_wingclAlphaCONDITION() {
		return _wingclAlphaCONDITION;
	}

	public Double get_cLAtAlphaCONDITION() {
		return _cLAtAlphaCONDITION;
	}

	public Double[] get_wingliftCoefficient3DCurveCONDITION() {
		return _wingliftCoefficient3DCurveCONDITION;
	}

	public double[] get_wingliftCoefficientDistributionatCLMaxCONDITION() {
		return _wingliftCoefficientDistributionatCLMaxCONDITION;
	}

	public Double[] get_wingclAlphaArrayCONDITION() {
		return _wingclAlphaArrayCONDITION;
	}

	public Amount<?> get_fuselageWingClAlphaDeg() {
		return _fuselageWingClAlphaDeg;
	}

	public Double get_fuselageWingClMax() {
		return _fuselageWingClMax;
	}

	public Double get_fuselageWingClZero() {
		return _fuselageWingClZero;
	}

	public Amount<Angle> get_fuselageWingAlphaStar() {
		return _fuselageWingAlphaStar;
	}

	public Amount<Angle> get_fuselageWingAlphaStall() {
		return _fuselageWingAlphaStall;
	}

	public Double get_fuselageWingClAlpha() {
		return _fuselageWingClAlpha;
	}

	public Double get_fuselageWingCLStar() {
		return _fuselageWingCLStar;
	}

	public Double[] get_fuselagewingliftCoefficient3DCurve() {
		return _fuselagewingliftCoefficient3DCurve;
	}

	public Map<Amount<Angle>, List<Double>> get_totalLiftCoefficient() {
		return _totalLiftCoefficient;
	}

	public Double get_wingCD0() {
		return _wingCD0;
	}

	public Double get_wingOswaldFactor() {
		return _wingOswaldFactor;
	}

	public Double get_wingCDInduced() {
		return _wingCDInduced;
	}

	public Double get_wingCDWave() {
		return _wingCDWave;
	}

	public Double[] get_wingPolar3DCurve() {
		return _wingPolar3DCurve;
	}

	public List<Double> get_wingParasiteDragCoefficientDistribution() {
		return _wingParasiteDragCoefficientDistribution;
	}

	public List<Double> get_wingInducedDragCoefficientDistribution() {
		return _wingInducedDragCoefficientDistribution;
	}

	public List<Double> get_wingInducedDragCoefficientDistributionParabolic() {
		return _wingInducedDragCoefficientDistributionParabolic;
	}

	public List<Double> get_wingDragCoefficient3DCurve() {
		return _wingDragCoefficient3DCurve;
	}

	public List<Double> get_wingDragCoefficient3DCurveTemp() {
		return _wingDragCoefficient3DCurveTemp;
	}

	public List<List<Double>> get_wingAirfoilsCoefficientCurve() {
		return _wingAirfoilsCoefficientCurve;
	}

	public MethodEnum get_deltaDueToFlapMethod() {
		return _deltaDueToFlapMethod;
	}

	public List<Double> getcLWingDragPolar() {
		return cLWingDragPolar;
	}

	public List<Double> getClListDragWing() {
		return clListDragWing;
	}

	public List<Double> getClListMomentWing() {
		return clListMomentWing;
	}

	public List<List<Double>> get_wingCdAirfoilDistributionInputStations() {
		return _wingCdAirfoilDistributionInputStations;
	}

	public List<List<Double>> get_wingCdAirfoilDistribution() {
		return _wingCdAirfoilDistribution;
	}

	public List<Double> getClListDragTail() {
		return clListDragTail;
	}

	public List<List<Double>> get_hTailCdAirfoilDistribution() {
		return _hTailCdAirfoilDistribution;
	}

	public List<List<Double>> get_hTailCdAirfoilDistributionInputStations() {
		return _hTailCdAirfoilDistributionInputStations;
	}

	public Double get_hTailCD0() {
		return _hTailCD0;
	}

	public Double get_hTailOswaldFactor() {
		return _hTailOswaldFactor;
	}

	public Double get_hTailCDInduced() {
		return _hTailCDInduced;
	}

	public Double get_hTailCDWave() {
		return _hTailCDWave;
	}

	public Double[] get_hTailPolar3DCurve() {
		return _hTailPolar3DCurve;
	}

	public List<Double> get_hTailParasiteDragCoefficientDistribution() {
		return _hTailParasiteDragCoefficientDistribution;
	}

	public List<Double> get_hTailInducedDragCoefficientDistribution() {
		return _hTailInducedDragCoefficientDistribution;
	}

	public List<Double> get_hTailDragCoefficientDistribution() {
		return _hTailDragCoefficientDistribution;
	}

	public List<Amount<Force>> get_hTailDragDistribution() {
		return _hTailDragDistribution;
	}

	public List<Double> get_hTailDragCoefficient3DCurve() {
		return _hTailDragCoefficient3DCurve;
	}

	public Double[] get_hTailliftCoefficient3DCurveCONDITION() {
		return _hTailliftCoefficient3DCurveCONDITION;
	}

	public Map<Amount<Angle>, List<Double>> get_totalDragPolar() {
		return _totalDragPolar;
	}

	public Double get_wingFinalMomentumPole() {
		return _wingFinalMomentumPole;
	}

	public Double get_hTailFinalMomentumPole() {
		return _hTailFinalMomentumPole;
	}

	public Map<MethodEnum, Amount<Length>> get_wingXACLRF() {
		return _wingXACLRF;
	}

	public Map<MethodEnum, Amount<Length>> get_wingXACMAC() {
		return _wingXACMAC;
	}

	public Map<MethodEnum, Double> get_wingXACMACpercent() {
		return _wingXACMACpercent;
	}

	public Map<MethodEnum, Amount<Length>> get_wingXACBRF() {
		return _wingXACBRF;
	}

	public Amount<Length> get_wingMAC() {
		return _wingMAC;
	}

	public Amount<Length> get_wingMeanAerodynamicChordLeadingEdgeX() {
		return _wingMeanAerodynamicChordLeadingEdgeX;
	}

	public Map<MethodEnum, List<Double>> get_wingMomentCoefficientAC() {
		return _wingMomentCoefficientAC;
	}

	public List<List<Double>> get_wingMomentCoefficients() {
		return _wingMomentCoefficients;
	}

	public List<Double> get_wingMomentCoefficientFinal() {
		return _wingMomentCoefficientFinal;
	}

	public List<Double> get_wingMomentCoefficientConstant() {
		return _wingMomentCoefficientConstant;
	}

	public List<Double> get_wingMomentCoefficientFinalACVariable() {
		return _wingMomentCoefficientFinalACVariable;
	}

	public List<Double> get_hTailMomentCoefficientFinal() {
		return _hTailMomentCoefficientFinal;
	}

	public Map<Amount<Angle>, List<Double>> get_hTailMomentCoefficientFinalElevator() {
		return _hTailMomentCoefficientFinalElevator;
	}

	public Amount<Length> get_wingZACMAC() {
		return _wingZACMAC;
	}

	public Amount<Length> get_wingYACMAC() {
		return _wingYACMAC;
	}

	public Map<MethodEnum, Amount<Length>> get_hTailXACLRF() {
		return _hTailXACLRF;
	}

	public Map<MethodEnum, Amount<Length>> get_hTailXACBRF() {
		return _hTailXACBRF;
	}

	public Map<MethodEnum, Amount<Length>> get_hTailXACMAC() {
		return _hTailXACMAC;
	}

	public Map<MethodEnum, Double> get_hTailXACMACpercent() {
		return _hTailXACMACpercent;
	}

	public Amount<Length> get_hTailMAC() {
		return _hTailMAC;
	}

	public Amount<Length> get_hTailMeanAerodynamicChordLeadingEdgeX() {
		return _hTailMeanAerodynamicChordLeadingEdgeX;
	}

	public Map<MethodEnum, List<Double>> get_hTailMomentCoefficientAC() {
		return _hTailMomentCoefficientAC;
	}

	public List<List<Double>> get_hTailMomentCoefficients() {
		return _hTailMomentCoefficients;
	}

	public MethodEnum get_fuselageMomentMethod() {
		return _fuselageMomentMethod;
	}

	public Map<MethodEnum, Double> get_fuselageCM0() {
		return _fuselageCM0;
	}

	public Map<MethodEnum, Double> get_fuselageCMAlpha() {
		return _fuselageCMAlpha;
	}

	public List<Double> get_fuselageMomentCoefficient() {
		return _fuselageMomentCoefficient;
	}

	public List<Double> get_fuselageMomentCoefficientdueToDrag() {
		return _fuselageMomentCoefficientdueToDrag;
	}

	public Map<MethodEnum, Amount<Length>> get_wingBodyXACBRF() {
		return _wingBodyXACBRF;
	}

	public Double get_deltaXACdueToFuselage() {
		return _deltaXACdueToFuselage;
	}

	public List<Double> get_landingGearMomentDueToDrag() {
		return _landingGearMomentDueToDrag;
	}

	public List<Double> get_wingNormalCoefficient() {
		return _wingNormalCoefficient;
	}

	public List<Double> get_hTailNormalCoefficient() {
		return _hTailNormalCoefficient;
	}

	public List<Double> get_hTailNormalCoefficientDownwashConstant() {
		return _hTailNormalCoefficientDownwashConstant;
	}

	public List<Double> get_wingHorizontalCoefficient() {
		return _wingHorizontalCoefficient;
	}

	public List<Double> get_hTailHorizontalCoefficient() {
		return _hTailHorizontalCoefficient;
	}

	public List<Double> get_hTailHorizontalCoefficientDownwashConstant() {
		return _hTailHorizontalCoefficientDownwashConstant;
	}

	public List<Double> get_wingMomentCoefficientNOPendular() {
		return _wingMomentCoefficientNOPendular;
	}

	public List<Double> get_wingMomentCoefficientPendular() {
		return _wingMomentCoefficientPendular;
	}

	public List<Double> get_hTailMomentCoefficientPendular() {
		return _hTailMomentCoefficientPendular;
	}

	public List<Double> get_totalMomentCoefficientPendular() {
		return _totalMomentCoefficientPendular;
	}

	public Map<Amount<Angle>, List<Double>> get_hTailNormalCoefficientDeltaE() {
		return _hTailNormalCoefficientDeltaE;
	}

	public Map<Amount<Angle>, List<Double>> get_hTailHorizontalCoefficientDeltaE() {
		return _hTailHorizontalCoefficientDeltaE;
	}

	public Map<Amount<Angle>, List<Double>> get_hTailMomentCoefficientPendularDeltaE() {
		return _hTailMomentCoefficientPendularDeltaE;
	}

	public Map<Amount<Angle>, List<Double>> get_totalMomentCoefficientPendularDeltaE() {
		return _totalMomentCoefficientPendularDeltaE;
	}

	public List<Double> get_hTailEquilibriumLiftCoefficient() {
		return _hTailEquilibriumLiftCoefficient;
	}

	public List<Double> get_totalEquilibriumLiftCoefficient() {
		return _totalEquilibriumLiftCoefficient;
	}

	public List<Double> get_hTailEquilibriumLiftCoefficientConstant() {
		return _hTailEquilibriumLiftCoefficientConstant;
	}

	public List<Double> get_totalEquilibriumLiftCoefficientConstant() {
		return _totalEquilibriumLiftCoefficientConstant;
	}

	public List<Double> get_totalTrimDrag() {
		return _totalTrimDrag;
	}

	public List<Amount<Angle>> get_deltaEEquilibrium() {
		return _deltaEEquilibrium;
	}

	public Map<Amount<Angle>, Double[]> get_clMapForDeltaeElevator() {
		return _clMapForDeltaeElevator;
	}

	public int getNumberOfIterationforDeltaE() {
		return numberOfIterationforDeltaE;
	}

	public List<List<Double>> get_clWingDistribution() {
		return _clWingDistribution;
	}

	public Double[] get_cl3DCurveWingFlapped() {
		return _cl3DCurveWingFlapped;
	}

	public List<List<Double>> get_clHtailDistribution() {
		return _clHtailDistribution;
	}

	public List<List<Double>> get_centerOfPressureWingDistribution() {
		return _centerOfPressureWingDistribution;
	}

	public List<List<Double>> get_centerOfPressurehTailDistribution() {
		return _centerOfPressurehTailDistribution;
	}

	public List<List<Double>> get_cMWingDistribution() {
		return _cMWingDistribution;
	}

	public List<List<Double>> get_cMHTailDistribution() {
		return _cMHTailDistribution;
	}

	public List<List<Amount<Angle>>> get_alphaIWingDistribution() {
		return _alphaIWingDistribution;
	}

	public List<List<Amount<Angle>>> get_alphaIHtailDistribution() {
		return _alphaIHtailDistribution;
	}

	public List<double[]> get_clNasaBlackwellDistributionModified() {
		return _clNasaBlackwellDistributionModified;
	}

	public Double get_clZeroFlapped() {
		return _clZeroFlapped;
	}

	public Double get_clAlphaDegFlapped() {
		return _clAlphaDegFlapped;
	}

	public Double get_clAlphaRadFlapped() {
		return _clAlphaRadFlapped;
	}

	public Amount<?> get_wingclAlphaFlapped() {
		return _wingclAlphaFlapped;
	}

	public Double get_clMaxFlapped() {
		return _clMaxFlapped;
	}

	public Amount<Angle> get_alphaStarFlapped() {
		return _alphaStarFlapped;
	}

	public Amount<Angle> get_alphaStallFlapped() {
		return _alphaStallFlapped;
	}

	public Amount<Angle> get_alphaStallLinearFlapped() {
		return _alphaStallLinearFlapped;
	}

	public Amount<Angle> get_alphaZeroLiftFlapped() {
		return _alphaZeroLiftFlapped;
	}

	public List<Double> get_clMaxDistributionFlapped() {
		return _clMaxDistributionFlapped;
	}

	public List<List<Double>> getClDistributions() {
		return clDistributions;
	}

	public Double[] getCl3D() {
		return cl3D;
	}

	public MethodEnum get_horizontalTailCL() {
		return _horizontalTailCL;
	}

	public Amount<Length> get_wingHorizontalDistanceACtoCG() {
		return _wingHorizontalDistanceACtoCG;
	}

	public Amount<Length> get_wingVerticalDistranceACtoCG() {
		return _wingVerticalDistranceACtoCG;
	}

	public void set_aircraftName(String _aircraftName) {
		this._aircraftName = _aircraftName;
	}

	public void set_xCGAircraft(Amount<Length> _xCGAircraft) {
		this._xCGAircraft = _xCGAircraft;
	}

	public void set_yCGAircraft(Amount<Length> _yCGAircraft) {
		this._yCGAircraft = _yCGAircraft;
	}

	public void set_zCGAircraft(Amount<Length> _zCGAircraft) {
		this._zCGAircraft = _zCGAircraft;
	}

	public void set_altitude(Amount<Length> _altitude) {
		this._altitude = _altitude;
	}

	public void set_machCurrent(Double _machCurrent) {
		this._machCurrent = _machCurrent;
	}

	public void set_reynoldsCurrent(Double _reynoldsCurrent) {
		this._reynoldsCurrent = _reynoldsCurrent;
	}

	public void set_alphaBodyInitial(Amount<Angle> _alphaBodyInitial) {
		this._alphaBodyInitial = _alphaBodyInitial;
	}

	public void set_alphaBodyFinal(Amount<Angle> _alphaBodyFinal) {
		this._alphaBodyFinal = _alphaBodyFinal;
	}

	public void set_numberOfAlphasBody(int _numberOfAlphasBody) {
		this._numberOfAlphasBody = _numberOfAlphasBody;
	}

	public void set_alphasBody(List<Amount<Angle>> _alphasBody) {
		this._alphasBody = _alphasBody;
	}

	public void set_theCondition(ConditionEnum _theCondition) {
		this._theCondition = _theCondition;
	}

	public void set_downwashConstant(boolean _downwashConstant) {
		this._downwashConstant = _downwashConstant;
	}

	public void set_wingMomentumPole(List<Double> _wingMomentumPole) {
		this._wingMomentumPole = _wingMomentumPole;
	}

	public void set_hTailMomentumPole(List<Double> _hTailMomentumPole) {
		this._hTailMomentumPole = _hTailMomentumPole;
	}

	public void set_alphaWingForDistribution(List<Amount<Angle>> _alphaWingForDistribution) {
		this._alphaWingForDistribution = _alphaWingForDistribution;
	}

	public void set_alphaHorizontalTailForDistribution(List<Amount<Angle>> _alphaHorizontalTailForDistribution) {
		this._alphaHorizontalTailForDistribution = _alphaHorizontalTailForDistribution;
	}

	public void set_dynamicPressureRatio(Double _dynamicPressureRatio) {
		this._dynamicPressureRatio = _dynamicPressureRatio;
	}

	public void set_xApexWing(Amount<Length> _xApexWing) {
		this._xApexWing = _xApexWing;
	}

	public void set_yApexWing(Amount<Length> _yApexWing) {
		this._yApexWing = _yApexWing;
	}

	public void set_zApexWing(Amount<Length> _zApexWing) {
		this._zApexWing = _zApexWing;
	}

	public void set_zACRootWing(Amount<Length> _zACRootWing) {
		this._zACRootWing = _zACRootWing;
	}

	public void set_wingSurface(Amount<Area> _wingSurface) {
		this._wingSurface = _wingSurface;
	}

	public void set_wingAspectRatio(Double _wingAspectRatio) {
		this._wingAspectRatio = _wingAspectRatio;
	}

	public void set_wingSpan(Amount<Length> _wingSpan) {
		this._wingSpan = _wingSpan;
	}

	public void set_wingSemiSpan(Amount<Length> _wingSemiSpan) {
		this._wingSemiSpan = _wingSemiSpan;
	}

	public void set_wingNumberOfPointSemiSpanWise(int _wingNumberOfPointSemiSpanWise) {
		this._wingNumberOfPointSemiSpanWise = _wingNumberOfPointSemiSpanWise;
	}

	public void set_wingAdimentionalKinkStation(Double _wingAdimentionalKinkStation) {
		this._wingAdimentionalKinkStation = _wingAdimentionalKinkStation;
	}

	public void set_wingNumberOfGivenSections(int _wingNumberOfGivenSections) {
		this._wingNumberOfGivenSections = _wingNumberOfGivenSections;
	}

	public void set_wingNumberOfGivenSectionsCLEAN(int _wingNumberOfGivenSectionsCLEAN) {
		this._wingNumberOfGivenSectionsCLEAN = _wingNumberOfGivenSectionsCLEAN;
	}

	public void set_wingAngleOfIncidence(Amount<Angle> _wingAngleOfIncidence) {
		this._wingAngleOfIncidence = _wingAngleOfIncidence;
	}

	public void set_wingTaperRatio(Double _wingTaperRatio) {
		this._wingTaperRatio = _wingTaperRatio;
	}

	public void set_wingSweepQuarterChord(Amount<Angle> _wingSweepQuarterChord) {
		this._wingSweepQuarterChord = _wingSweepQuarterChord;
	}

	public void set_wingSweepLE(Amount<Angle> _wingSweepLE) {
		this._wingSweepLE = _wingSweepLE;
	}

	public void set_wingVortexSemiSpanToSemiSpanRatio(Double _wingVortexSemiSpanToSemiSpanRatio) {
		this._wingVortexSemiSpanToSemiSpanRatio = _wingVortexSemiSpanToSemiSpanRatio;
	}

	public void setcLAlphaMachZero(double cLAlphaMachZero) {
		this.cLAlphaMachZero = cLAlphaMachZero;
	}

	public void set_wingMeanAirfoilFamily(AirfoilFamilyEnum _wingMeanAirfoilFamily) {
		this._wingMeanAirfoilFamily = _wingMeanAirfoilFamily;
	}

	public void set_wingMaxThicknessMeanAirfoil(Double _wingMaxThicknessMeanAirfoil) {
		this._wingMaxThicknessMeanAirfoil = _wingMaxThicknessMeanAirfoil;
	}

	public void set_wingairfoilLiftCoefficientCurve(MethodEnum _wingairfoilLiftCoefficientCurve) {
		this._wingairfoilLiftCoefficientCurve = _wingairfoilLiftCoefficientCurve;
	}

	public void set_wingInducedAngleOfAttack(List<List<Amount<Angle>>> _wingInducedAngleOfAttack) {
		this._wingInducedAngleOfAttack = _wingInducedAngleOfAttack;
	}

	public void set_wingCLAirfoilsDistribution(List<List<Double>> _wingCLAirfoilsDistribution) {
		this._wingCLAirfoilsDistribution = _wingCLAirfoilsDistribution;
	}

	public void set_wingCLAirfoilsDistributionFinal(List<List<Double>> _wingCLAirfoilsDistributionFinal) {
		this._wingCLAirfoilsDistributionFinal = _wingCLAirfoilsDistributionFinal;
	}

	public void set_wingairfoilMomentCoefficientCurve(MethodEnum _wingairfoilMomentCoefficientCurve) {
		this._wingairfoilMomentCoefficientCurve = _wingairfoilMomentCoefficientCurve;
	}

	public void set_wingCLMomentAirfoilInput(List<List<Double>> _wingCLMomentAirfoilInput) {
		this._wingCLMomentAirfoilInput = _wingCLMomentAirfoilInput;
	}

	public void set_wingCMMomentAirfoilInput(List<List<Double>> _wingCMMomentAirfoilInput) {
		this._wingCMMomentAirfoilInput = _wingCMMomentAirfoilInput;
	}

	public void set_wingCLMomentAirfoilOutput(List<Double> _wingCLMomentAirfoilOutput) {
		this._wingCLMomentAirfoilOutput = _wingCLMomentAirfoilOutput;
	}

	public void set_wingCMMomentAirfoilOutput(List<List<Double>> _wingCMMomentAirfoilOutput) {
		this._wingCMMomentAirfoilOutput = _wingCMMomentAirfoilOutput;
	}

	public void set_wingYAdimensionalBreakPoints(List<Double> _wingYAdimensionalBreakPoints) {
		this._wingYAdimensionalBreakPoints = _wingYAdimensionalBreakPoints;
	}

	public void set_wingYBreakPoints(List<Amount<Length>> _wingYBreakPoints) {
		this._wingYBreakPoints = _wingYBreakPoints;
	}

	public void set_wingYAdimensionalDistribution(List<Double> _wingYAdimensionalDistribution) {
		this._wingYAdimensionalDistribution = _wingYAdimensionalDistribution;
	}

	public void set_wingYDistribution(List<Amount<Length>> _wingYDistribution) {
		this._wingYDistribution = _wingYDistribution;
	}

	public void set_wingChordsBreakPoints(List<Amount<Length>> _wingChordsBreakPoints) {
		this._wingChordsBreakPoints = _wingChordsBreakPoints;
	}

	public void set_wingChordsDistribution(List<Amount<Length>> _wingChordsDistribution) {
		this._wingChordsDistribution = _wingChordsDistribution;
	}

	public void set_wingXleBreakPoints(List<Amount<Length>> _wingXleBreakPoints) {
		this._wingXleBreakPoints = _wingXleBreakPoints;
	}

	public void set_wingXleDistribution(List<Amount<Length>> _wingXleDistribution) {
		this._wingXleDistribution = _wingXleDistribution;
	}

	public void set_wingTwistBreakPoints(List<Amount<Angle>> _wingTwistBreakPoints) {
		this._wingTwistBreakPoints = _wingTwistBreakPoints;
	}

	public void set_wingTwistDistribution(List<Amount<Angle>> _wingTwistDistribution) {
		this._wingTwistDistribution = _wingTwistDistribution;
	}

	public void set_wingDihedralBreakPoints(List<Amount<Angle>> _wingDihedralBreakPoints) {
		this._wingDihedralBreakPoints = _wingDihedralBreakPoints;
	}

	public void set_wingDihedralDistribution(List<Amount<Angle>> _wingDihedralDistribution) {
		this._wingDihedralDistribution = _wingDihedralDistribution;
	}

	public void set_wingAlphaZeroLiftBreakPoints(List<Amount<Angle>> _wingAlphaZeroLiftBreakPoints) {
		this._wingAlphaZeroLiftBreakPoints = _wingAlphaZeroLiftBreakPoints;
	}

	public void set_wingAlphaZeroLiftDistribution(List<Amount<Angle>> _wingAlphaZeroLiftDistribution) {
		this._wingAlphaZeroLiftDistribution = _wingAlphaZeroLiftDistribution;
	}

	public void set_wingAlphaStarBreakPoints(List<Amount<Angle>> _wingAlphaStarBreakPoints) {
		this._wingAlphaStarBreakPoints = _wingAlphaStarBreakPoints;
	}

	public void set_wingAlphaStarDistribution(List<Amount<Angle>> _wingAlphaStarDistribution) {
		this._wingAlphaStarDistribution = _wingAlphaStarDistribution;
	}

	public void set_wingClMaxBreakPoints(List<Double> _wingClMaxBreakPoints) {
		this._wingClMaxBreakPoints = _wingClMaxBreakPoints;
	}

	public void set_wingClMaxDistribution(List<Double> _wingClMaxDistribution) {
		this._wingClMaxDistribution = _wingClMaxDistribution;
	}

	public void set_wingCl0BreakPoints(List<Double> _wingCl0BreakPoints) {
		this._wingCl0BreakPoints = _wingCl0BreakPoints;
	}

	public void set_wingCl0Distribution(List<Double> _wingCl0Distribution) {
		this._wingCl0Distribution = _wingCl0Distribution;
	}

	public void set_wingClAlphaBreakPointsDeg(List<Double> _wingClAlphaBreakPointsDeg) {
		this._wingClAlphaBreakPointsDeg = _wingClAlphaBreakPointsDeg;
	}

	public void set_wingXACBreakPoints(List<Double> _wingXACBreakPoints) {
		this._wingXACBreakPoints = _wingXACBreakPoints;
	}

	public void set_wingXACDistribution(List<Double> _wingXACDistribution) {
		this._wingXACDistribution = _wingXACDistribution;
	}

	public void set_wingCmACBreakPoints(List<Double> _wingCmACBreakPoints) {
		this._wingCmACBreakPoints = _wingCmACBreakPoints;
	}

	public void set_wingCmC4Distribution(List<Double> _wingCmC4Distribution) {
		this._wingCmC4Distribution = _wingCmC4Distribution;
	}

	public void set_wingYAdimensionalBreakPointsCLEAN(List<Double> _wingYAdimensionalBreakPointsCLEAN) {
		this._wingYAdimensionalBreakPointsCLEAN = _wingYAdimensionalBreakPointsCLEAN;
	}

	public void set_wingYBreakPointsCLEAN(List<Amount<Length>> _wingYBreakPointsCLEAN) {
		this._wingYBreakPointsCLEAN = _wingYBreakPointsCLEAN;
	}

	public void set_wingYAdimensionalDistributionCLEAN(List<Double> _wingYAdimensionalDistributionCLEAN) {
		this._wingYAdimensionalDistributionCLEAN = _wingYAdimensionalDistributionCLEAN;
	}

	public void set_wingYDistributionCLEAN(List<Amount<Length>> _wingYDistributionCLEAN) {
		this._wingYDistributionCLEAN = _wingYDistributionCLEAN;
	}

	public void set_wingChordsBreakPointsCLEAN(List<Amount<Length>> _wingChordsBreakPointsCLEAN) {
		this._wingChordsBreakPointsCLEAN = _wingChordsBreakPointsCLEAN;
	}

	public void set_wingChordsDistributionCLEAN(List<Amount<Length>> _wingChordsDistributionCLEAN) {
		this._wingChordsDistributionCLEAN = _wingChordsDistributionCLEAN;
	}

	public void set_wingXleBreakPointsCLEAN(List<Amount<Length>> _wingXleBreakPointsCLEAN) {
		this._wingXleBreakPointsCLEAN = _wingXleBreakPointsCLEAN;
	}

	public void set_wingXleDistributionCLEAN(List<Amount<Length>> _wingXleDistributionCLEAN) {
		this._wingXleDistributionCLEAN = _wingXleDistributionCLEAN;
	}

	public void set_wingTwistBreakPointsCLEAN(List<Amount<Angle>> _wingTwistBreakPointsCLEAN) {
		this._wingTwistBreakPointsCLEAN = _wingTwistBreakPointsCLEAN;
	}

	public void set_wingTwistDistributionCLEAN(List<Amount<Angle>> _wingTwistDistributionCLEAN) {
		this._wingTwistDistributionCLEAN = _wingTwistDistributionCLEAN;
	}

	public void set_wingDihedralBreakPointsCLEAN(List<Amount<Angle>> _wingDihedralBreakPointsCLEAN) {
		this._wingDihedralBreakPointsCLEAN = _wingDihedralBreakPointsCLEAN;
	}

	public void set_wingDihedralDistributionCLEAN(List<Amount<Angle>> _wingDihedralDistributionCLEAN) {
		this._wingDihedralDistributionCLEAN = _wingDihedralDistributionCLEAN;
	}

	public void set_wingAlphaZeroLiftBreakPointsCLEAN(List<Amount<Angle>> _wingAlphaZeroLiftBreakPointsCLEAN) {
		this._wingAlphaZeroLiftBreakPointsCLEAN = _wingAlphaZeroLiftBreakPointsCLEAN;
	}

	public void set_wingAlphaZeroLiftDistributionCLEAN(List<Amount<Angle>> _wingAlphaZeroLiftDistributionCLEAN) {
		this._wingAlphaZeroLiftDistributionCLEAN = _wingAlphaZeroLiftDistributionCLEAN;
	}

	public void set_wingAlphaStarBreakPointsCLEAN(List<Amount<Angle>> _wingAlphaStarBreakPointsCLEAN) {
		this._wingAlphaStarBreakPointsCLEAN = _wingAlphaStarBreakPointsCLEAN;
	}

	public void set_wingAlphaStarDistributionCLEAN(List<Amount<Angle>> _wingAlphaStarDistributionCLEAN) {
		this._wingAlphaStarDistributionCLEAN = _wingAlphaStarDistributionCLEAN;
	}

	public void set_wingClMaxBreakPointsCLEAN(List<Double> _wingClMaxBreakPointsCLEAN) {
		this._wingClMaxBreakPointsCLEAN = _wingClMaxBreakPointsCLEAN;
	}

	public void set_wingClMaxDistributionCLEAN(List<Double> _wingClMaxDistributionCLEAN) {
		this._wingClMaxDistributionCLEAN = _wingClMaxDistributionCLEAN;
	}

	public void set_wingCl0BreakPointsCLEAN(List<Double> _wingCl0BreakPointsCLEAN) {
		this._wingCl0BreakPointsCLEAN = _wingCl0BreakPointsCLEAN;
	}

	public void set_wingCl0DistributionCLEAN(List<Double> _wingCl0DistributionCLEAN) {
		this._wingCl0DistributionCLEAN = _wingCl0DistributionCLEAN;
	}

	public void set_wingClAlphaBreakPointsDegCLEAN(List<Double> _wingClAlphaBreakPointsDegCLEAN) {
		this._wingClAlphaBreakPointsDegCLEAN = _wingClAlphaBreakPointsDegCLEAN;
	}

	public void set_wingClAlphaDistributionDegCLEAN(List<Double> _wingClAlphaDistributionDegCLEAN) {
		this._wingClAlphaDistributionDegCLEAN = _wingClAlphaDistributionDegCLEAN;
	}

	public void set_wingMaxThicknessBreakPointsCLEAN(List<Double> _wingMaxThicknessBreakPointsCLEAN) {
		this._wingMaxThicknessBreakPointsCLEAN = _wingMaxThicknessBreakPointsCLEAN;
	}

	public void set_wingMaxThicknessDistributionCLEAN(List<Double> _wingMaxThicknessDistributionCLEAN) {
		this._wingMaxThicknessDistributionCLEAN = _wingMaxThicknessDistributionCLEAN;
	}

	public void set_wingLERadiusBreakPointsCLEAN(List<Amount<Length>> _wingLERadiusBreakPointsCLEAN) {
		this._wingLERadiusBreakPointsCLEAN = _wingLERadiusBreakPointsCLEAN;
	}

	public void set_wingLERadiusDistributionCLEAN(List<Amount<Length>> _wingLERadiusDistributionCLEAN) {
		this._wingLERadiusDistributionCLEAN = _wingLERadiusDistributionCLEAN;
	}

	public void set_wingClAlphaDistributionDeg(List<Double> _wingClAlphaDistributionDeg) {
		this._wingClAlphaDistributionDeg = _wingClAlphaDistributionDeg;
	}

	public void set_wingMaxThicknessBreakPoints(List<Double> _wingMaxThicknessBreakPoints) {
		this._wingMaxThicknessBreakPoints = _wingMaxThicknessBreakPoints;
	}

	public void set_wingMaxThicknessDistribution(List<Double> _wingMaxThicknessDistribution) {
		this._wingMaxThicknessDistribution = _wingMaxThicknessDistribution;
	}

	public void set_wingLERadiusBreakPoints(List<Amount<Length>> _wingLERadiusBreakPoints) {
		this._wingLERadiusBreakPoints = _wingLERadiusBreakPoints;
	}

	public void set_wingLERadiusDistribution(List<Amount<Length>> _wingLERadiusDistribution) {
		this._wingLERadiusDistribution = _wingLERadiusDistribution;
	}

	public void set_wingYLEBreakPoints(List<Amount<Length>> _wingYLEBreakPoints) {
		this._wingYLEBreakPoints = _wingYLEBreakPoints;
	}

	public void set_wingYLEDistribution(List<Amount<Length>> _wingYLEDistribution) {
		this._wingYLEDistribution = _wingYLEDistribution;
	}

	public void set_wingNumberOfFlaps(int _wingNumberOfFlaps) {
		this._wingNumberOfFlaps = _wingNumberOfFlaps;
	}

	public void set_wingNumberOfSlats(int _wingNumberOfSlats) {
		this._wingNumberOfSlats = _wingNumberOfSlats;
	}

	public void set_wingDeltaFlap(List<Amount<Angle>> _wingDeltaFlap) {
		this._wingDeltaFlap = _wingDeltaFlap;
	}

	public void set_wingFlapType(List<FlapTypeEnum> _wingFlapType) {
		this._wingFlapType = _wingFlapType;
	}

	public void set_wingEtaInFlap(List<Double> _wingEtaInFlap) {
		this._wingEtaInFlap = _wingEtaInFlap;
	}

	public void set_wingEtaOutFlap(List<Double> _wingEtaOutFlap) {
		this._wingEtaOutFlap = _wingEtaOutFlap;
	}

	public void set_wingFlapCfC(List<Double> _wingFlapCfC) {
		this._wingFlapCfC = _wingFlapCfC;
	}

	public void set_wingDeltaSlat(List<Amount<Angle>> _wingDeltaSlat) {
		this._wingDeltaSlat = _wingDeltaSlat;
	}

	public void set_wingEtaInSlat(List<Double> _wingEtaInSlat) {
		this._wingEtaInSlat = _wingEtaInSlat;
	}

	public void set_wingEtaOutSlat(List<Double> _wingEtaOutSlat) {
		this._wingEtaOutSlat = _wingEtaOutSlat;
	}

	public void set_wingSlatCsC(List<Double> _wingSlatCsC) {
		this._wingSlatCsC = _wingSlatCsC;
	}

	public void set_wingCExtCSlat(List<Double> _wingCExtCSlat) {
		this._wingCExtCSlat = _wingCExtCSlat;
	}

	public void set_wingLeRadiusCSLat(List<Double> _wingLeRadiusCSLat) {
		this._wingLeRadiusCSLat = _wingLeRadiusCSLat;
	}

	public void set_supermappa(Map<Double, Map<Double, Double[]>> _supermappa) {
		this._supermappa = _supermappa;
	}

	public void set_fuselageDiameter(Amount<Length> _fuselageDiameter) {
		this._fuselageDiameter = _fuselageDiameter;
	}

	public void set_fuselageLength(Amount<Length> _fuselageLength) {
		this._fuselageLength = _fuselageLength;
	}

	public void set_fuselageNoseFinessRatio(Double _fuselageNoseFinessRatio) {
		this._fuselageNoseFinessRatio = _fuselageNoseFinessRatio;
	}

	public void set_fuselageFinessRatio(Double _fuselageFinessRatio) {
		this._fuselageFinessRatio = _fuselageFinessRatio;
	}

	public void set_fuselageTailFinessRatio(Double _fuselageTailFinessRatio) {
		this._fuselageTailFinessRatio = _fuselageTailFinessRatio;
	}

	public void set_fuselageWindshieldAngle(Amount<Angle> _fuselageWindshieldAngle) {
		this._fuselageWindshieldAngle = _fuselageWindshieldAngle;
	}

	public void set_fuselageUpSweepAngle(Amount<Angle> _fuselageUpSweepAngle) {
		this._fuselageUpSweepAngle = _fuselageUpSweepAngle;
	}

	public void set_fuselageXPercentPositionPole(Double _fuselageXPercentPositionPole) {
		this._fuselageXPercentPositionPole = _fuselageXPercentPositionPole;
	}

	public void set_fuselageFrontSurface(Amount<Area> _fuselageFrontSurface) {
		this._fuselageFrontSurface = _fuselageFrontSurface;
	}

	public void set_cM0fuselage(double _cM0fuselage) {
		this._cM0fuselage = _cM0fuselage;
	}

	public void set_cMalphafuselage(double _cMalphafuselage) {
		this._cMalphafuselage = _cMalphafuselage;
	}

	public void setCdDistributionFuselageFinal(List<Double> cdDistributionFuselageFinal) {
		this.cdDistributionFuselageFinal = cdDistributionFuselageFinal;
	}

	public void set_xApexHTail(Amount<Length> _xApexHTail) {
		this._xApexHTail = _xApexHTail;
	}

	public void set_yApexHTail(Amount<Length> _yApexHTail) {
		this._yApexHTail = _yApexHTail;
	}

	public void set_zApexHTail(Amount<Length> _zApexHTail) {
		this._zApexHTail = _zApexHTail;
	}

	public void set_verticalTailSpan(Amount<Length> _verticalTailSpan) {
		this._verticalTailSpan = _verticalTailSpan;
	}

	public void set_hTailSurface(Amount<Area> _hTailSurface) {
		this._hTailSurface = _hTailSurface;
	}

	public void set_hTailAspectRatio(Double _hTailAspectRatio) {
		this._hTailAspectRatio = _hTailAspectRatio;
	}

	public void set_hTailTaperRatio(Double _hTailTaperRatio) {
		this._hTailTaperRatio = _hTailTaperRatio;
	}

	public void set_hTailSpan(Amount<Length> _hTailSpan) {
		this._hTailSpan = _hTailSpan;
	}

	public void set_hTailSemiSpan(Amount<Length> _hTailSemiSpan) {
		this._hTailSemiSpan = _hTailSemiSpan;
	}

	public void set_hTailNumberOfPointSemiSpanWise(int _hTailNumberOfPointSemiSpanWise) {
		this._hTailNumberOfPointSemiSpanWise = _hTailNumberOfPointSemiSpanWise;
	}

	public void set_hTailadimentionalKinkStation(Double _hTailadimentionalKinkStation) {
		this._hTailadimentionalKinkStation = _hTailadimentionalKinkStation;
	}

	public void set_hTailnumberOfGivenSections(int _hTailnumberOfGivenSections) {
		this._hTailnumberOfGivenSections = _hTailnumberOfGivenSections;
	}

	public void set_hTailSweepLE(Amount<Angle> _hTailSweepLE) {
		this._hTailSweepLE = _hTailSweepLE;
	}

	public void set_hTailSweepQuarterChord(Amount<Angle> _hTailSweepQuarterChord) {
		this._hTailSweepQuarterChord = _hTailSweepQuarterChord;
	}

	public void set_hTailMeanAirfoilFamily(AirfoilFamilyEnum _hTailMeanAirfoilFamily) {
		this._hTailMeanAirfoilFamily = _hTailMeanAirfoilFamily;
	}

	public void set_hTailMaxThicknessMeanAirfoil(Double _hTailMaxThicknessMeanAirfoil) {
		this._hTailMaxThicknessMeanAirfoil = _hTailMaxThicknessMeanAirfoil;
	}

	public void set_hTailVortexSemiSpanToSemiSpanRatio(Double _hTailVortexSemiSpanToSemiSpanRatio) {
		this._hTailVortexSemiSpanToSemiSpanRatio = _hTailVortexSemiSpanToSemiSpanRatio;
	}

	public void set_hTailairfoilLiftCoefficientCurve(MethodEnum _hTailairfoilLiftCoefficientCurve) {
		this._hTailairfoilLiftCoefficientCurve = _hTailairfoilLiftCoefficientCurve;
	}

	public void set_hTailCLAirfoilsDistribution(List<List<Double>> _hTailCLAirfoilsDistribution) {
		this._hTailCLAirfoilsDistribution = _hTailCLAirfoilsDistribution;
	}

	public void set_hTailInducedAngleOfAttack(List<List<Amount<Angle>>> _hTailInducedAngleOfAttack) {
		this._hTailInducedAngleOfAttack = _hTailInducedAngleOfAttack;
	}

	public void set_hTailCLAirfoilsDistributionFinal(List<List<Double>> _hTailCLAirfoilsDistributionFinal) {
		this._hTailCLAirfoilsDistributionFinal = _hTailCLAirfoilsDistributionFinal;
	}

	public void set_hTailHorizontalDistanceACtoCG(Amount<Length> _hTailHorizontalDistanceACtoCG) {
		this._hTailHorizontalDistanceACtoCG = _hTailHorizontalDistanceACtoCG;
	}

	public void set_hTailVerticalDistranceACtoCG(Amount<Length> _hTailVerticalDistranceACtoCG) {
		this._hTailVerticalDistranceACtoCG = _hTailVerticalDistranceACtoCG;
	}

	public void set_hTailYAdimensionalBreakPoints(List<Double> _hTailYAdimensionalBreakPoints) {
		this._hTailYAdimensionalBreakPoints = _hTailYAdimensionalBreakPoints;
	}

	public void set_hTailYBreakPoints(List<Amount<Length>> _hTailYBreakPoints) {
		this._hTailYBreakPoints = _hTailYBreakPoints;
	}

	public void set_hTailYAdimensionalDistribution(List<Double> _hTailYAdimensionalDistribution) {
		this._hTailYAdimensionalDistribution = _hTailYAdimensionalDistribution;
	}

	public void set_hTailAngleOfIncidence(Amount<Angle> _hTailAngleOfIncidence) {
		this._hTailAngleOfIncidence = _hTailAngleOfIncidence;
	}

	public void set_hTailYDistribution(List<Amount<Length>> _hTailYDistribution) {
		this._hTailYDistribution = _hTailYDistribution;
	}

	public void set_hTailChordsBreakPoints(List<Amount<Length>> _hTailChordsBreakPoints) {
		this._hTailChordsBreakPoints = _hTailChordsBreakPoints;
	}

	public void set_hTailChordsDistribution(List<Amount<Length>> _hTailChordsDistribution) {
		this._hTailChordsDistribution = _hTailChordsDistribution;
	}

	public void set_hTailXleBreakPoints(List<Amount<Length>> _hTailXleBreakPoints) {
		this._hTailXleBreakPoints = _hTailXleBreakPoints;
	}

	public void set_hTailXleDistribution(List<Amount<Length>> _hTailXleDistribution) {
		this._hTailXleDistribution = _hTailXleDistribution;
	}

	public void set_hTailTwistBreakPoints(List<Amount<Angle>> _hTailTwistBreakPoints) {
		this._hTailTwistBreakPoints = _hTailTwistBreakPoints;
	}

	public void set_hTailTwistDistribution(List<Amount<Angle>> _hTailTwistDistribution) {
		this._hTailTwistDistribution = _hTailTwistDistribution;
	}

	public void set_hTailDihedralBreakPoints(List<Amount<Angle>> _hTailDihedralBreakPoints) {
		this._hTailDihedralBreakPoints = _hTailDihedralBreakPoints;
	}

	public void set_hTailDihedralDistribution(List<Amount<Angle>> _hTailDihedralDistribution) {
		this._hTailDihedralDistribution = _hTailDihedralDistribution;
	}

	public void set_hTailAlphaZeroLiftBreakPoints(List<Amount<Angle>> _hTailAlphaZeroLiftBreakPoints) {
		this._hTailAlphaZeroLiftBreakPoints = _hTailAlphaZeroLiftBreakPoints;
	}

	public void set_hTailAlphaZeroLiftDistribution(List<Amount<Angle>> _hTailAlphaZeroLiftDistribution) {
		this._hTailAlphaZeroLiftDistribution = _hTailAlphaZeroLiftDistribution;
	}

	public void set_hTailAlphaStarBreakPoints(List<Amount<Angle>> _hTailAlphaStarBreakPoints) {
		this._hTailAlphaStarBreakPoints = _hTailAlphaStarBreakPoints;
	}

	public void set_hTailAlphaStarDistribution(List<Amount<Angle>> _hTailAlphaStarDistribution) {
		this._hTailAlphaStarDistribution = _hTailAlphaStarDistribution;
	}

	public void set_hTailClMaxBreakPoints(List<Double> _hTailClMaxBreakPoints) {
		this._hTailClMaxBreakPoints = _hTailClMaxBreakPoints;
	}

	public void set_hTailClMaxDistribution(List<Double> _hTailClMaxDistribution) {
		this._hTailClMaxDistribution = _hTailClMaxDistribution;
	}

	public void set_hTailMaxThicknessBreakPoints(List<Double> _hTailMaxThicknessBreakPoints) {
		this._hTailMaxThicknessBreakPoints = _hTailMaxThicknessBreakPoints;
	}

	public void set_hTailMaxThicknessDistribution(List<Double> _hTailMaxThicknessDistribution) {
		this._hTailMaxThicknessDistribution = _hTailMaxThicknessDistribution;
	}

	public void set_hTailClAlphaBreakPointsDeg(List<Double> _hTailClAlphaBreakPointsDeg) {
		this._hTailClAlphaBreakPointsDeg = _hTailClAlphaBreakPointsDeg;
	}

	public void set_hTailCl0BreakPoints(List<Double> _hTailCl0BreakPoints) {
		this._hTailCl0BreakPoints = _hTailCl0BreakPoints;
	}

	public void set_hTailCl0Distribution(List<Double> _hTailCl0Distribution) {
		this._hTailCl0Distribution = _hTailCl0Distribution;
	}

	public void set_hTailClAlphaistributionDeg(List<Double> _hTailClAlphaistributionDeg) {
		this._hTailClAlphaistributionDeg = _hTailClAlphaistributionDeg;
	}

	public void set_hTailXACBreakPoints(List<Double> _hTailXACBreakPoints) {
		this._hTailXACBreakPoints = _hTailXACBreakPoints;
	}

	public void set_hTailXACDistribution(List<Double> _hTailXACDistribution) {
		this._hTailXACDistribution = _hTailXACDistribution;
	}

	public void set_hTailCmACBreakPoints(List<Double> _hTailCmACBreakPoints) {
		this._hTailCmACBreakPoints = _hTailCmACBreakPoints;
	}

	public void set_hTailCmC4Distribution(List<Double> _hTailCmC4Distribution) {
		this._hTailCmC4Distribution = _hTailCmC4Distribution;
	}

	public void set_anglesOfElevatorDeflection(List<Amount<Angle>> _anglesOfElevatorDeflection) {
		this._anglesOfElevatorDeflection = _anglesOfElevatorDeflection;
	}

	public void set_elevatorType(FlapTypeEnum _elevatorType) {
		this._elevatorType = _elevatorType;
	}

	public void set_elevatorEtaIn(Double _elevatorEtaIn) {
		this._elevatorEtaIn = _elevatorEtaIn;
	}

	public void set_elevatorEtaOut(Double _elevatorEtaOut) {
		this._elevatorEtaOut = _elevatorEtaOut;
	}

	public void set_elevatorCfC(Double _elevatorCfC) {
		this._elevatorCfC = _elevatorCfC;
	}

	public void set_tiltingAngle(Amount<Angle> _tiltingAngle) {
		this._tiltingAngle = _tiltingAngle;
	}

	public void set_wingDragMethod(MethodEnum _wingDragMethod) {
		this._wingDragMethod = _wingDragMethod;
	}

	public void set_hTailDragMethod(MethodEnum _hTailDragMethod) {
		this._hTailDragMethod = _hTailDragMethod;
	}

	public void set_plotList(List<AerodynamicAndStabilityPlotEnum> _plotList) {
		this._plotList = _plotList;
	}

	public void set_plotCheck(boolean _plotCheck) {
		this._plotCheck = _plotCheck;
	}

	public void setTheStabilityCalculator(StabilityExecutableCalculator theStabilityCalculator) {
		this.theStabilityCalculator = theStabilityCalculator;
	}

	public void set_horizontalDistanceQuarterChordWingHTail(Amount<Length> _horizontalDistanceQuarterChordWingHTail) {
		this._horizontalDistanceQuarterChordWingHTail = _horizontalDistanceQuarterChordWingHTail;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTail(Amount<Length> _verticalDistanceZeroLiftDirectionWingHTail) {
		this._verticalDistanceZeroLiftDirectionWingHTail = _verticalDistanceZeroLiftDirectionWingHTail;
	}

	public void set_horizontalDistanceQuarterChordWingHTailNOANGLE(
			Amount<Length> _horizontalDistanceQuarterChordWingHTailNOANGLE) {
		this._horizontalDistanceQuarterChordWingHTailNOANGLE = _horizontalDistanceQuarterChordWingHTailNOANGLE;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL) {
		this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE) {
		this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public void setDatabaseFolderPath(String databaseFolderPath) {
		this.databaseFolderPath = databaseFolderPath;
	}

	public void setAerodynamicDatabaseFileName(String aerodynamicDatabaseFileName) {
		this.aerodynamicDatabaseFileName = aerodynamicDatabaseFileName;
	}

	public void setHighLiftDatabaseFileName(String highLiftDatabaseFileName) {
		this.highLiftDatabaseFileName = highLiftDatabaseFileName;
	}

	public void setFusDesDatabaseFileName(String fusDesDatabaseFileName) {
		this.fusDesDatabaseFileName = fusDesDatabaseFileName;
	}

	public void setAeroDatabaseReader(AerodynamicDatabaseReader aeroDatabaseReader) {
		this.aeroDatabaseReader = aeroDatabaseReader;
	}

	public void setHighLiftDatabaseReader(HighLiftDatabaseReader highLiftDatabaseReader) {
		this.highLiftDatabaseReader = highLiftDatabaseReader;
	}

	public void setFusDesDatabaseReader(FusDesDatabaseReader fusDesDatabaseReader) {
		this.fusDesDatabaseReader = fusDesDatabaseReader;
	}

	public void setAlphaZeroLiftRad(double[] alphaZeroLiftRad) {
		this.alphaZeroLiftRad = alphaZeroLiftRad;
	}

	public void setTwistDistributionRad(double[] twistDistributionRad) {
		this.twistDistributionRad = twistDistributionRad;
	}

	public void setAlphaZeroLiftRadCLEAN(double[] alphaZeroLiftRadCLEAN) {
		this.alphaZeroLiftRadCLEAN = alphaZeroLiftRadCLEAN;
	}

	public void setTwistDistributionRadCLEAN(double[] twistDistributionRadCLEAN) {
		this.twistDistributionRadCLEAN = twistDistributionRadCLEAN;
	}

	public void setAlphaZeroLiftRadHTail(double[] alphaZeroLiftRadHTail) {
		this.alphaZeroLiftRadHTail = alphaZeroLiftRadHTail;
	}

	public void setTwistDistributionRadHTail(double[] twistDistributionRadHTail) {
		this.twistDistributionRadHTail = twistDistributionRadHTail;
	}

	public void set_deltaCD0Miscellaneus(double _deltaCD0Miscellaneus) {
		this._deltaCD0Miscellaneus = _deltaCD0Miscellaneus;
	}

	public void set_landingGearArm(Amount<Length> _landingGearArm) {
		this._landingGearArm = _landingGearArm;
	}

	public void setDimensionalOverKink(double dimensionalOverKink) {
		this.dimensionalOverKink = dimensionalOverKink;
	}

	public void setInfluenceAreaRoot(double influenceAreaRoot) {
		this.influenceAreaRoot = influenceAreaRoot;
	}

	public void setInfluenceAreaKink(double influenceAreaKink) {
		this.influenceAreaKink = influenceAreaKink;
	}

	public void setInfluenceAreaTip(double influenceAreaTip) {
		this.influenceAreaTip = influenceAreaTip;
	}

	public void setkRoot(double kRoot) {
		this.kRoot = kRoot;
	}

	public void setkKink(double kKink) {
		this.kKink = kKink;
	}

	public void setkTip(double kTip) {
		this.kTip = kTip;
	}

	public void setDimensionalOverKinkHTail(double dimensionalOverKinkHTail) {
		this.dimensionalOverKinkHTail = dimensionalOverKinkHTail;
	}

	public void setInfluenceAreaRootHTail(double influenceAreaRootHTail) {
		this.influenceAreaRootHTail = influenceAreaRootHTail;
	}

	public void setInfluenceAreaKinkHTail(double influenceAreaKinkHTail) {
		this.influenceAreaKinkHTail = influenceAreaKinkHTail;
	}

	public void setInfluenceAreaTipHTail(double influenceAreaTipHTail) {
		this.influenceAreaTipHTail = influenceAreaTipHTail;
	}

	public void setkRootHTail(double kRootHTail) {
		this.kRootHTail = kRootHTail;
	}

	public void setkKinkHTail(double kKinkHTail) {
		this.kKinkHTail = kKinkHTail;
	}

	public void setkTipHTail(double kTipHTail) {
		this.kTipHTail = kTipHTail;
	}

	public void setTheNasaBlackwellCalculatorMachActualWingCLEAN(
			NasaBlackwell theNasaBlackwellCalculatorMachActualWingCLEAN) {
		this.theNasaBlackwellCalculatorMachActualWingCLEAN = theNasaBlackwellCalculatorMachActualWingCLEAN;
	}

	public void setTheNasaBlackwellCalculatorMachZeroCLEAN(NasaBlackwell theNasaBlackwellCalculatorMachZeroCLEAN) {
		this.theNasaBlackwellCalculatorMachZeroCLEAN = theNasaBlackwellCalculatorMachZeroCLEAN;
	}

	public void setTheNasaBlackwellCalculatorMachActualWing(NasaBlackwell theNasaBlackwellCalculatorMachActualWing) {
		this.theNasaBlackwellCalculatorMachActualWing = theNasaBlackwellCalculatorMachActualWing;
	}

	public void setTheNasaBlackwellCalculatorMachZero(NasaBlackwell theNasaBlackwellCalculatorMachZero) {
		this.theNasaBlackwellCalculatorMachZero = theNasaBlackwellCalculatorMachZero;
	}

	public void setTheNasaBlackwellCalculatorMachActualHTail(NasaBlackwell theNasaBlackwellCalculatorMachActualHTail) {
		this.theNasaBlackwellCalculatorMachActualHTail = theNasaBlackwellCalculatorMachActualHTail;
	}

	public void set_downwashGradientConstantRoskam(List<Double> _downwashGradientConstantRoskam) {
		this._downwashGradientConstantRoskam = _downwashGradientConstantRoskam;
	}

	public void set_downwashAngleConstantRoskam(List<Amount<Angle>> _downwashAngleConstantRoskam) {
		this._downwashAngleConstantRoskam = _downwashAngleConstantRoskam;
	}

	public void set_downwashGradientConstantSlingerland(List<Double> _downwashGradientConstantSlingerland) {
		this._downwashGradientConstantSlingerland = _downwashGradientConstantSlingerland;
	}

	public void set_downwashAngleConstantSlingerland(List<Amount<Angle>> _downwashAngleConstantSlingerland) {
		this._downwashAngleConstantSlingerland = _downwashAngleConstantSlingerland;
	}

	public void set_downwashGradientVariableSlingerland(List<Double> _downwashGradientVariableSlingerland) {
		this._downwashGradientVariableSlingerland = _downwashGradientVariableSlingerland;
	}

	public void set_downwashAngleVariableSlingerland(List<Amount<Angle>> _downwashAngleVariableSlingerland) {
		this._downwashAngleVariableSlingerland = _downwashAngleVariableSlingerland;
	}

	public void set_downwashAngleVariableSlingerlandOld(List<Amount<Angle>> _downwashAngleVariableSlingerlandOld) {
		this._downwashAngleVariableSlingerlandOld = _downwashAngleVariableSlingerlandOld;
	}

	public void set_horizontalDistance(List<Amount<Length>> _horizontalDistance) {
		this._horizontalDistance = _horizontalDistance;
	}

	public void set_verticalDistance(List<Amount<Length>> _verticalDistance) {
		this._verticalDistance = _verticalDistance;
	}

	public void set_horizontalDistanceConstant(List<Amount<Length>> _horizontalDistanceConstant) {
		this._horizontalDistanceConstant = _horizontalDistanceConstant;
	}

	public void set_verticalDistanceConstant(List<Amount<Length>> _verticalDistanceConstant) {
		this._verticalDistanceConstant = _verticalDistanceConstant;
	}

	public void set_wingAlphaZeroLift(Amount<Angle> _wingAlphaZeroLift) {
		this._wingAlphaZeroLift = _wingAlphaZeroLift;
	}

	public void set_wingalphaStar(Amount<Angle> _wingalphaStar) {
		this._wingalphaStar = _wingalphaStar;
	}

	public void set_wingalphaMaxLinear(Amount<Angle> _wingalphaMaxLinear) {
		this._wingalphaMaxLinear = _wingalphaMaxLinear;
	}

	public void set_wingalphaStall(Amount<Angle> _wingalphaStall) {
		this._wingalphaStall = _wingalphaStall;
	}

	public void set_wingcLZero(Double _wingcLZero) {
		this._wingcLZero = _wingcLZero;
	}

	public void set_wingcLStar(Double _wingcLStar) {
		this._wingcLStar = _wingcLStar;
	}

	public void set_wingcLMax(Double _wingcLMax) {
		this._wingcLMax = _wingcLMax;
	}

	public void set_wingcLAlphaRad(Double _wingcLAlphaRad) {
		this._wingcLAlphaRad = _wingcLAlphaRad;
	}

	public void set_wingcLAlphaDeg(Double _wingcLAlphaDeg) {
		this._wingcLAlphaDeg = _wingcLAlphaDeg;
	}

	public void set_wingclAlpha(Amount<?> _wingclAlpha) {
		this._wingclAlpha = _wingclAlpha;
	}

	public void set_cLAtAlpha(Double _cLAtAlpha) {
		this._cLAtAlpha = _cLAtAlpha;
	}

	public void set_wingliftCoefficient3DCurve(Double[] _wingliftCoefficient3DCurve) {
		this._wingliftCoefficient3DCurve = _wingliftCoefficient3DCurve;
	}

	public void set_wingliftCoefficientDistributionatCLMax(double[] _wingliftCoefficientDistributionatCLMax) {
		this._wingliftCoefficientDistributionatCLMax = _wingliftCoefficientDistributionatCLMax;
	}

	public void set_wingclAlphaArray(Double[] _wingclAlphaArray) {
		this._wingclAlphaArray = _wingclAlphaArray;
	}

	public void set_wingclAlphaArrayHighLift(Double[] _wingclAlphaArrayHighLift) {
		this._wingclAlphaArrayHighLift = _wingclAlphaArrayHighLift;
	}

	public void set_cLAtAlphaHighLift(Double _cLAtAlphaHighLift) {
		this._cLAtAlphaHighLift = _cLAtAlphaHighLift;
	}

	public void set_alphaZeroLiftHighLift(Amount<Angle> _alphaZeroLiftHighLift) {
		this._alphaZeroLiftHighLift = _alphaZeroLiftHighLift;
	}

	public void set_alphaStarHighLift(Amount<Angle> _alphaStarHighLift) {
		this._alphaStarHighLift = _alphaStarHighLift;
	}

	public void set_alphaStallHighLift(Amount<Angle> _alphaStallHighLift) {
		this._alphaStallHighLift = _alphaStallHighLift;
	}

	public void set_cLZeroHighLift(Double _cLZeroHighLift) {
		this._cLZeroHighLift = _cLZeroHighLift;
	}

	public void set_cLStarHighLift(Double _cLStarHighLift) {
		this._cLStarHighLift = _cLStarHighLift;
	}

	public void set_cLMaxHighLift(Double _cLMaxHighLift) {
		this._cLMaxHighLift = _cLMaxHighLift;
	}

	public void set_cLAlphaHighLiftDEG(Double _cLAlphaHighLiftDEG) {
		this._cLAlphaHighLiftDEG = _cLAlphaHighLiftDEG;
	}

	public void set_cD0HighLift(Double _cD0HighLift) {
		this._cD0HighLift = _cD0HighLift;
	}

	public void set_deltaCl0FlapList(List<Double> _deltaCl0FlapList) {
		this._deltaCl0FlapList = _deltaCl0FlapList;
	}

	public void set_deltaCl0Flap(Double _deltaCl0Flap) {
		this._deltaCl0Flap = _deltaCl0Flap;
	}

	public void set_deltaCL0FlapList(List<Double> _deltaCL0FlapList) {
		this._deltaCL0FlapList = _deltaCL0FlapList;
	}

	public void set_deltaCL0Flap(Double _deltaCL0Flap) {
		this._deltaCL0Flap = _deltaCL0Flap;
	}

	public void set_deltaClmaxFlapList(List<Double> _deltaClmaxFlapList) {
		this._deltaClmaxFlapList = _deltaClmaxFlapList;
	}

	public void set_deltaClmaxFlap(Double _deltaClmaxFlap) {
		this._deltaClmaxFlap = _deltaClmaxFlap;
	}

	public void set_deltaCLmaxFlapList(List<Double> _deltaCLmaxFlapList) {
		this._deltaCLmaxFlapList = _deltaCLmaxFlapList;
	}

	public void set_deltaCLmaxFlap(Double _deltaCLmaxFlap) {
		this._deltaCLmaxFlap = _deltaCLmaxFlap;
	}

	public void set_deltaClmaxSlatList(List<Double> _deltaClmaxSlatList) {
		this._deltaClmaxSlatList = _deltaClmaxSlatList;
	}

	public void set_deltaClmaxSlat(Double _deltaClmaxSlat) {
		this._deltaClmaxSlat = _deltaClmaxSlat;
	}

	public void set_deltaCLmaxSlatList(List<Double> _deltaCLmaxSlatList) {
		this._deltaCLmaxSlatList = _deltaCLmaxSlatList;
	}

	public void set_deltaCLmaxSlat(Double _deltaCLmaxSlat) {
		this._deltaCLmaxSlat = _deltaCLmaxSlat;
	}

	public void set_deltaCD0List(List<Double> _deltaCD0List) {
		this._deltaCD0List = _deltaCD0List;
	}

	public void set_deltaCD0(Double _deltaCD0) {
		this._deltaCD0 = _deltaCD0;
	}

	public void set_deltaCMc4List(List<Double> _deltaCMc4List) {
		this._deltaCMc4List = _deltaCMc4List;
	}

	public void set_deltaCMc4(Double _deltaCMc4) {
		this._deltaCMc4 = _deltaCMc4;
	}

	public void set_alphaArrayPlotHighLift(Double[] _alphaArrayPlotHighLift) {
		this._alphaArrayPlotHighLift = _alphaArrayPlotHighLift;
	}

	public void set_wingLiftCoefficient3DCurveHighLift(Double[] _wingLiftCoefficient3DCurveHighLift) {
		this._wingLiftCoefficient3DCurveHighLift = _wingLiftCoefficient3DCurveHighLift;
	}

	public void set_wingLiftCoefficient3DCurveHighLiftWINGARRAY(Double[] _wingLiftCoefficient3DCurveHighLiftWINGARRAY) {
		this._wingLiftCoefficient3DCurveHighLiftWINGARRAY = _wingLiftCoefficient3DCurveHighLiftWINGARRAY;
	}

	public void set_wingLiftCoefficientModified(double[] _wingLiftCoefficientModified) {
		this._wingLiftCoefficientModified = _wingLiftCoefficientModified;
	}

	public void set_hTailAlphaZeroLift(Amount<Angle> _hTailAlphaZeroLift) {
		this._hTailAlphaZeroLift = _hTailAlphaZeroLift;
	}

	public void set_hTailalphaStar(Amount<Angle> _hTailalphaStar) {
		this._hTailalphaStar = _hTailalphaStar;
	}

	public void set_hTailalphaMaxLinear(Amount<Angle> _hTailalphaMaxLinear) {
		this._hTailalphaMaxLinear = _hTailalphaMaxLinear;
	}

	public void set_hTailalphaStall(Amount<Angle> _hTailalphaStall) {
		this._hTailalphaStall = _hTailalphaStall;
	}

	public void set_hTailcLZero(Double _hTailcLZero) {
		this._hTailcLZero = _hTailcLZero;
	}

	public void set_hTailcLStar(Double _hTailcLStar) {
		this._hTailcLStar = _hTailcLStar;
	}

	public void set_hTailcLMax(Double _hTailcLMax) {
		this._hTailcLMax = _hTailcLMax;
	}

	public void set_hTailcLAlphaRad(Double _hTailcLAlphaRad) {
		this._hTailcLAlphaRad = _hTailcLAlphaRad;
	}

	public void set_hTailcLAlphaDeg(Double _hTailcLAlphaDeg) {
		this._hTailcLAlphaDeg = _hTailcLAlphaDeg;
	}

	public void set_hTailclAlpha(Amount<?> _hTailclAlpha) {
		this._hTailclAlpha = _hTailclAlpha;
	}

	public void set_hTailliftCoefficient3DCurve(Double[] _hTailliftCoefficient3DCurve) {
		this._hTailliftCoefficient3DCurve = _hTailliftCoefficient3DCurve;
	}

	public void set_hTailliftCoefficientDistributionatCLMax(double[] _hTailliftCoefficientDistributionatCLMax) {
		this._hTailliftCoefficientDistributionatCLMax = _hTailliftCoefficientDistributionatCLMax;
	}

	public void set_hTailclAlphaArray(Double[] _hTailclAlphaArray) {
		this._hTailclAlphaArray = _hTailclAlphaArray;
	}

	public void set_hTailclAlphaArrayHighLift(Double[] _hTailclAlphaArrayHighLift) {
		this._hTailclAlphaArrayHighLift = _hTailclAlphaArrayHighLift;
	}

	public void set_tauElevator(Map<Amount<Angle>, Double> _tauElevator) {
		this._tauElevator = _tauElevator;
	}

	public void set_deltaCLMaxElevator(Map<Amount<Angle>, Double> _deltaCLMaxElevator) {
		this._deltaCLMaxElevator = _deltaCLMaxElevator;
	}

	public void set_deltaCD0Elevator(Map<Amount<Angle>, Double> _deltaCD0Elevator) {
		this._deltaCD0Elevator = _deltaCD0Elevator;
	}

	public void set_cLAlphaElevatorDeg(Map<Amount<Angle>, Double> _cLAlphaElevatorDeg) {
		this._cLAlphaElevatorDeg = _cLAlphaElevatorDeg;
	}

	public void set_deltacLZeroElevator(Map<Amount<Angle>, Double> _deltacLZeroElevator) {
		this._deltacLZeroElevator = _deltacLZeroElevator;
	}

	public void set_hTailLiftCoefficient3DCurveWithElevator(
			Map<Amount<Angle>, Double[]> _hTailLiftCoefficient3DCurveWithElevator) {
		this._hTailLiftCoefficient3DCurveWithElevator = _hTailLiftCoefficient3DCurveWithElevator;
	}

	public void set_hTailDragCoefficient3DCurveWithElevator(
			Map<Amount<Angle>, Double[]> _hTailDragCoefficient3DCurveWithElevator) {
		this._hTailDragCoefficient3DCurveWithElevator = _hTailDragCoefficient3DCurveWithElevator;
	}

	public void set_hTailcLMaxElevator(Map<Amount<Angle>, Double> _hTailcLMaxElevator) {
		this._hTailcLMaxElevator = _hTailcLMaxElevator;
	}

	public void set_hTailalphaZeroLiftElevator(Map<Amount<Angle>, Amount<Angle>> _hTailalphaZeroLiftElevator) {
		this._hTailalphaZeroLiftElevator = _hTailalphaZeroLiftElevator;
	}

	public void set_hTailalphaStarElevator(Map<Amount<Angle>, Amount<Angle>> _hTailalphaStarElevator) {
		this._hTailalphaStarElevator = _hTailalphaStarElevator;
	}

	public void set_hTailalphaStallLiftElevator(Map<Amount<Angle>, Amount<Angle>> _hTailalphaStallLiftElevator) {
		this._hTailalphaStallLiftElevator = _hTailalphaStallLiftElevator;
	}

	public void set_hTailCLZeroElevator(Map<Amount<Angle>, Double> _hTailCLZeroElevator) {
		this._hTailCLZeroElevator = _hTailCLZeroElevator;
	}

	public void set_hTailCLStarElevator(Map<Amount<Angle>, Double> _hTailCLStarElevator) {
		this._hTailCLStarElevator = _hTailCLStarElevator;
	}

	public void set_hTailCLAlphaElevator(Map<Amount<Angle>, Double> _hTailCLAlphaElevator) {
		this._hTailCLAlphaElevator = _hTailCLAlphaElevator;
	}

	public void set_tauElevatorArray(Map<Amount<Angle>, Double> _tauElevatorArray) {
		this._tauElevatorArray = _tauElevatorArray;
	}

	public void set_deltaEAnglesArray(List<Amount<Angle>> _deltaEAnglesArray) {
		this._deltaEAnglesArray = _deltaEAnglesArray;
	}

	public void set_wingAlphaZeroLiftCONDITION(Amount<Angle> _wingAlphaZeroLiftCONDITION) {
		this._wingAlphaZeroLiftCONDITION = _wingAlphaZeroLiftCONDITION;
	}

	public void set_wingalphaStarCONDITION(Amount<Angle> _wingalphaStarCONDITION) {
		this._wingalphaStarCONDITION = _wingalphaStarCONDITION;
	}

	public void set_wingalphaMaxLinearCONDITION(Amount<Angle> _wingalphaMaxLinearCONDITION) {
		this._wingalphaMaxLinearCONDITION = _wingalphaMaxLinearCONDITION;
	}

	public void set_wingalphaStallCONDITION(Amount<Angle> _wingalphaStallCONDITION) {
		this._wingalphaStallCONDITION = _wingalphaStallCONDITION;
	}

	public void set_wingcLZeroCONDITION(Double _wingcLZeroCONDITION) {
		this._wingcLZeroCONDITION = _wingcLZeroCONDITION;
	}

	public void set_wingcLStarCONDITION(Double _wingcLStarCONDITION) {
		this._wingcLStarCONDITION = _wingcLStarCONDITION;
	}

	public void set_wingcLMaxCONDITION(Double _wingcLMaxCONDITION) {
		this._wingcLMaxCONDITION = _wingcLMaxCONDITION;
	}

	public void set_wingcLAlphaRadCONDITION(Double _wingcLAlphaRadCONDITION) {
		this._wingcLAlphaRadCONDITION = _wingcLAlphaRadCONDITION;
	}

	public void set_wingcLAlphaDegCONDITION(Double _wingcLAlphaDegCONDITION) {
		this._wingcLAlphaDegCONDITION = _wingcLAlphaDegCONDITION;
	}

	public void set_wingclAlphaCONDITION(Amount<?> _wingclAlphaCONDITION) {
		this._wingclAlphaCONDITION = _wingclAlphaCONDITION;
	}

	public void set_cLAtAlphaCONDITION(Double _cLAtAlphaCONDITION) {
		this._cLAtAlphaCONDITION = _cLAtAlphaCONDITION;
	}

	public void set_wingliftCoefficient3DCurveCONDITION(Double[] _wingliftCoefficient3DCurveCONDITION) {
		this._wingliftCoefficient3DCurveCONDITION = _wingliftCoefficient3DCurveCONDITION;
	}

	public void set_wingliftCoefficientDistributionatCLMaxCONDITION(
			double[] _wingliftCoefficientDistributionatCLMaxCONDITION) {
		this._wingliftCoefficientDistributionatCLMaxCONDITION = _wingliftCoefficientDistributionatCLMaxCONDITION;
	}

	public void set_wingclAlphaArrayCONDITION(Double[] _wingclAlphaArrayCONDITION) {
		this._wingclAlphaArrayCONDITION = _wingclAlphaArrayCONDITION;
	}

	public void set_fuselageWingClAlphaDeg(Amount<?> _fuselageWingClAlphaDeg) {
		this._fuselageWingClAlphaDeg = _fuselageWingClAlphaDeg;
	}

	public void set_fuselageWingClMax(Double _fuselageWingClMax) {
		this._fuselageWingClMax = _fuselageWingClMax;
	}

	public void set_fuselageWingClZero(Double _fuselageWingClZero) {
		this._fuselageWingClZero = _fuselageWingClZero;
	}

	public void set_fuselageWingAlphaStar(Amount<Angle> _fuselageWingAlphaStar) {
		this._fuselageWingAlphaStar = _fuselageWingAlphaStar;
	}

	public void set_fuselageWingAlphaStall(Amount<Angle> _fuselageWingAlphaStall) {
		this._fuselageWingAlphaStall = _fuselageWingAlphaStall;
	}

	public void set_fuselageWingClAlpha(Double _fuselageWingClAlpha) {
		this._fuselageWingClAlpha = _fuselageWingClAlpha;
	}

	public void set_fuselageWingCLStar(Double _fuselageWingCLStar) {
		this._fuselageWingCLStar = _fuselageWingCLStar;
	}

	public void set_fuselagewingliftCoefficient3DCurve(Double[] _fuselagewingliftCoefficient3DCurve) {
		this._fuselagewingliftCoefficient3DCurve = _fuselagewingliftCoefficient3DCurve;
	}

	public void set_totalLiftCoefficient(Map<Amount<Angle>, List<Double>> _totalLiftCoefficient) {
		this._totalLiftCoefficient = _totalLiftCoefficient;
	}

	public void set_wingCD0(Double _wingCD0) {
		this._wingCD0 = _wingCD0;
	}

	public void set_wingOswaldFactor(Double _wingOswaldFactor) {
		this._wingOswaldFactor = _wingOswaldFactor;
	}

	public void set_wingCDInduced(Double _wingCDInduced) {
		this._wingCDInduced = _wingCDInduced;
	}

	public void set_wingCDWave(Double _wingCDWave) {
		this._wingCDWave = _wingCDWave;
	}

	public void set_wingPolar3DCurve(Double[] _wingPolar3DCurve) {
		this._wingPolar3DCurve = _wingPolar3DCurve;
	}

	public void set_wingParasiteDragCoefficientDistribution(List<Double> _wingParasiteDragCoefficientDistribution) {
		this._wingParasiteDragCoefficientDistribution = _wingParasiteDragCoefficientDistribution;
	}

	public void set_wingInducedDragCoefficientDistribution(List<Double> _wingInducedDragCoefficientDistribution) {
		this._wingInducedDragCoefficientDistribution = _wingInducedDragCoefficientDistribution;
	}

	public void set_wingInducedDragCoefficientDistributionParabolic(
			List<Double> _wingInducedDragCoefficientDistributionParabolic) {
		this._wingInducedDragCoefficientDistributionParabolic = _wingInducedDragCoefficientDistributionParabolic;
	}

	public void set_wingDragCoefficient3DCurve(List<Double> _wingDragCoefficient3DCurve) {
		this._wingDragCoefficient3DCurve = _wingDragCoefficient3DCurve;
	}

	public void set_wingDragCoefficient3DCurveTemp(List<Double> _wingDragCoefficient3DCurveTemp) {
		this._wingDragCoefficient3DCurveTemp = _wingDragCoefficient3DCurveTemp;
	}

	public void set_wingAirfoilsCoefficientCurve(List<List<Double>> _wingAirfoilsCoefficientCurve) {
		this._wingAirfoilsCoefficientCurve = _wingAirfoilsCoefficientCurve;
	}

	public void set_deltaDueToFlapMethod(MethodEnum _deltaDueToFlapMethod) {
		this._deltaDueToFlapMethod = _deltaDueToFlapMethod;
	}

	public void setcLWingDragPolar(List<Double> cLWingDragPolar) {
		this.cLWingDragPolar = cLWingDragPolar;
	}

	public void setClListDragWing(List<Double> clListDragWing) {
		this.clListDragWing = clListDragWing;
	}

	public void setClListMomentWing(List<Double> clListMomentWing) {
		this.clListMomentWing = clListMomentWing;
	}

	public void set_wingCdAirfoilDistributionInputStations(List<List<Double>> _wingCdAirfoilDistributionInputStations) {
		this._wingCdAirfoilDistributionInputStations = _wingCdAirfoilDistributionInputStations;
	}

	public void set_wingCdAirfoilDistribution(List<List<Double>> _wingCdAirfoilDistribution) {
		this._wingCdAirfoilDistribution = _wingCdAirfoilDistribution;
	}

	public void setClListDragTail(List<Double> clListDragTail) {
		this.clListDragTail = clListDragTail;
	}

	public void set_hTailCdAirfoilDistribution(List<List<Double>> _hTailCdAirfoilDistribution) {
		this._hTailCdAirfoilDistribution = _hTailCdAirfoilDistribution;
	}

	public void set_hTailCdAirfoilDistributionInputStations(List<List<Double>> _hTailCdAirfoilDistributionInputStations) {
		this._hTailCdAirfoilDistributionInputStations = _hTailCdAirfoilDistributionInputStations;
	}

	public void set_hTailCD0(Double _hTailCD0) {
		this._hTailCD0 = _hTailCD0;
	}

	public void set_hTailOswaldFactor(Double _hTailOswaldFactor) {
		this._hTailOswaldFactor = _hTailOswaldFactor;
	}

	public void set_hTailCDInduced(Double _hTailCDInduced) {
		this._hTailCDInduced = _hTailCDInduced;
	}

	public void set_hTailCDWave(Double _hTailCDWave) {
		this._hTailCDWave = _hTailCDWave;
	}

	public void set_hTailPolar3DCurve(Double[] _hTailPolar3DCurve) {
		this._hTailPolar3DCurve = _hTailPolar3DCurve;
	}

	public void set_hTailParasiteDragCoefficientDistribution(List<Double> _hTailParasiteDragCoefficientDistribution) {
		this._hTailParasiteDragCoefficientDistribution = _hTailParasiteDragCoefficientDistribution;
	}

	public void set_hTailInducedDragCoefficientDistribution(List<Double> _hTailInducedDragCoefficientDistribution) {
		this._hTailInducedDragCoefficientDistribution = _hTailInducedDragCoefficientDistribution;
	}

	public void set_hTailDragCoefficientDistribution(List<Double> _hTailDragCoefficientDistribution) {
		this._hTailDragCoefficientDistribution = _hTailDragCoefficientDistribution;
	}

	public void set_hTailDragDistribution(List<Amount<Force>> _hTailDragDistribution) {
		this._hTailDragDistribution = _hTailDragDistribution;
	}

	public void set_hTailDragCoefficient3DCurve(List<Double> _hTailDragCoefficient3DCurve) {
		this._hTailDragCoefficient3DCurve = _hTailDragCoefficient3DCurve;
	}

	public void set_hTailliftCoefficient3DCurveCONDITION(Double[] _hTailliftCoefficient3DCurveCONDITION) {
		this._hTailliftCoefficient3DCurveCONDITION = _hTailliftCoefficient3DCurveCONDITION;
	}

	public void set_totalDragPolar(Map<Amount<Angle>, List<Double>> _totalDragPolar) {
		this._totalDragPolar = _totalDragPolar;
	}

	public void set_wingFinalMomentumPole(Double _wingFinalMomentumPole) {
		this._wingFinalMomentumPole = _wingFinalMomentumPole;
	}

	public void set_hTailFinalMomentumPole(Double _hTailFinalMomentumPole) {
		this._hTailFinalMomentumPole = _hTailFinalMomentumPole;
	}

	public void set_wingXACLRF(Map<MethodEnum, Amount<Length>> _wingXACLRF) {
		this._wingXACLRF = _wingXACLRF;
	}

	public void set_wingXACMAC(Map<MethodEnum, Amount<Length>> _wingXACMAC) {
		this._wingXACMAC = _wingXACMAC;
	}

	public void set_wingXACMACpercent(Map<MethodEnum, Double> _wingXACMACpercent) {
		this._wingXACMACpercent = _wingXACMACpercent;
	}

	public void set_wingXACBRF(Map<MethodEnum, Amount<Length>> _wingXACBRF) {
		this._wingXACBRF = _wingXACBRF;
	}

	public void set_wingMAC(Amount<Length> _wingMAC) {
		this._wingMAC = _wingMAC;
	}

	public void set_wingMeanAerodynamicChordLeadingEdgeX(Amount<Length> _wingMeanAerodynamicChordLeadingEdgeX) {
		this._wingMeanAerodynamicChordLeadingEdgeX = _wingMeanAerodynamicChordLeadingEdgeX;
	}

	public void set_wingMomentCoefficientAC(Map<MethodEnum, List<Double>> _wingMomentCoefficientAC) {
		this._wingMomentCoefficientAC = _wingMomentCoefficientAC;
	}

	public void set_wingMomentCoefficients(List<List<Double>> _wingMomentCoefficients) {
		this._wingMomentCoefficients = _wingMomentCoefficients;
	}

	public void set_wingMomentCoefficientFinal(List<Double> _wingMomentCoefficientFinal) {
		this._wingMomentCoefficientFinal = _wingMomentCoefficientFinal;
	}

	public void set_wingMomentCoefficientConstant(List<Double> _wingMomentCoefficientConstant) {
		this._wingMomentCoefficientConstant = _wingMomentCoefficientConstant;
	}

	public void set_wingMomentCoefficientFinalACVariable(List<Double> _wingMomentCoefficientFinalACVariable) {
		this._wingMomentCoefficientFinalACVariable = _wingMomentCoefficientFinalACVariable;
	}

	public void set_hTailMomentCoefficientFinal(List<Double> _hTailMomentCoefficientFinal) {
		this._hTailMomentCoefficientFinal = _hTailMomentCoefficientFinal;
	}

	public void set_hTailMomentCoefficientFinalElevator(
			Map<Amount<Angle>, List<Double>> _hTailMomentCoefficientFinalElevator) {
		this._hTailMomentCoefficientFinalElevator = _hTailMomentCoefficientFinalElevator;
	}

	public void set_wingZACMAC(Amount<Length> _wingZACMAC) {
		this._wingZACMAC = _wingZACMAC;
	}

	public void set_wingYACMAC(Amount<Length> _wingYACMAC) {
		this._wingYACMAC = _wingYACMAC;
	}

	public void set_hTailXACLRF(Map<MethodEnum, Amount<Length>> _hTailXACLRF) {
		this._hTailXACLRF = _hTailXACLRF;
	}

	public void set_hTailXACBRF(Map<MethodEnum, Amount<Length>> _hTailXACBRF) {
		this._hTailXACBRF = _hTailXACBRF;
	}

	public void set_hTailXACMAC(Map<MethodEnum, Amount<Length>> _hTailXACMAC) {
		this._hTailXACMAC = _hTailXACMAC;
	}

	public void set_hTailXACMACpercent(Map<MethodEnum, Double> _hTailXACMACpercent) {
		this._hTailXACMACpercent = _hTailXACMACpercent;
	}

	public void set_hTailMAC(Amount<Length> _hTailMAC) {
		this._hTailMAC = _hTailMAC;
	}

	public void set_hTailMeanAerodynamicChordLeadingEdgeX(Amount<Length> _hTailMeanAerodynamicChordLeadingEdgeX) {
		this._hTailMeanAerodynamicChordLeadingEdgeX = _hTailMeanAerodynamicChordLeadingEdgeX;
	}

	public void set_hTailMomentCoefficientAC(Map<MethodEnum, List<Double>> _hTailMomentCoefficientAC) {
		this._hTailMomentCoefficientAC = _hTailMomentCoefficientAC;
	}

	public void set_hTailMomentCoefficients(List<List<Double>> _hTailMomentCoefficients) {
		this._hTailMomentCoefficients = _hTailMomentCoefficients;
	}

	public void set_fuselageMomentMethod(MethodEnum _fuselageMomentMethod) {
		this._fuselageMomentMethod = _fuselageMomentMethod;
	}

	public void set_fuselageCM0(Map<MethodEnum, Double> _fuselageCM0) {
		this._fuselageCM0 = _fuselageCM0;
	}

	public void set_fuselageCMAlpha(Map<MethodEnum, Double> _fuselageCMAlpha) {
		this._fuselageCMAlpha = _fuselageCMAlpha;
	}

	public void set_fuselageMomentCoefficient(List<Double> _fuselageMomentCoefficient) {
		this._fuselageMomentCoefficient = _fuselageMomentCoefficient;
	}

	public void set_fuselageMomentCoefficientdueToDrag(List<Double> _fuselageMomentCoefficientdueToDrag) {
		this._fuselageMomentCoefficientdueToDrag = _fuselageMomentCoefficientdueToDrag;
	}

	public void set_wingBodyXACBRF(Map<MethodEnum, Amount<Length>> _wingBodyXACBRF) {
		this._wingBodyXACBRF = _wingBodyXACBRF;
	}

	public void set_deltaXACdueToFuselage(Double _deltaXACdueToFuselage) {
		this._deltaXACdueToFuselage = _deltaXACdueToFuselage;
	}

	public void set_landingGearMomentDueToDrag(List<Double> _landingGearMomentDueToDrag) {
		this._landingGearMomentDueToDrag = _landingGearMomentDueToDrag;
	}

	public void set_wingNormalCoefficient(List<Double> _wingNormalCoefficient) {
		this._wingNormalCoefficient = _wingNormalCoefficient;
	}

	public void set_hTailNormalCoefficient(List<Double> _hTailNormalCoefficient) {
		this._hTailNormalCoefficient = _hTailNormalCoefficient;
	}

	public void set_hTailNormalCoefficientDownwashConstant(List<Double> _hTailNormalCoefficientDownwashConstant) {
		this._hTailNormalCoefficientDownwashConstant = _hTailNormalCoefficientDownwashConstant;
	}

	public void set_wingHorizontalCoefficient(List<Double> _wingHorizontalCoefficient) {
		this._wingHorizontalCoefficient = _wingHorizontalCoefficient;
	}

	public void set_hTailHorizontalCoefficient(List<Double> _hTailHorizontalCoefficient) {
		this._hTailHorizontalCoefficient = _hTailHorizontalCoefficient;
	}

	public void set_hTailHorizontalCoefficientDownwashConstant(List<Double> _hTailHorizontalCoefficientDownwashConstant) {
		this._hTailHorizontalCoefficientDownwashConstant = _hTailHorizontalCoefficientDownwashConstant;
	}

	public void set_wingMomentCoefficientNOPendular(List<Double> _wingMomentCoefficientNOPendular) {
		this._wingMomentCoefficientNOPendular = _wingMomentCoefficientNOPendular;
	}

	public void set_wingMomentCoefficientPendular(List<Double> _wingMomentCoefficientPendular) {
		this._wingMomentCoefficientPendular = _wingMomentCoefficientPendular;
	}

	public void set_hTailMomentCoefficientPendular(List<Double> _hTailMomentCoefficientPendular) {
		this._hTailMomentCoefficientPendular = _hTailMomentCoefficientPendular;
	}

	public void set_totalMomentCoefficientPendular(List<Double> _totalMomentCoefficientPendular) {
		this._totalMomentCoefficientPendular = _totalMomentCoefficientPendular;
	}

	public void set_hTailNormalCoefficientDeltaE(Map<Amount<Angle>, List<Double>> _hTailNormalCoefficientDeltaE) {
		this._hTailNormalCoefficientDeltaE = _hTailNormalCoefficientDeltaE;
	}

	public void set_hTailHorizontalCoefficientDeltaE(Map<Amount<Angle>, List<Double>> _hTailHorizontalCoefficientDeltaE) {
		this._hTailHorizontalCoefficientDeltaE = _hTailHorizontalCoefficientDeltaE;
	}

	public void set_hTailMomentCoefficientPendularDeltaE(
			Map<Amount<Angle>, List<Double>> _hTailMomentCoefficientPendularDeltaE) {
		this._hTailMomentCoefficientPendularDeltaE = _hTailMomentCoefficientPendularDeltaE;
	}

	public void set_totalMomentCoefficientPendularDeltaE(
			Map<Amount<Angle>, List<Double>> _totalMomentCoefficientPendularDeltaE) {
		this._totalMomentCoefficientPendularDeltaE = _totalMomentCoefficientPendularDeltaE;
	}

	public void set_hTailEquilibriumLiftCoefficient(List<Double> _hTailEquilibriumLiftCoefficient) {
		this._hTailEquilibriumLiftCoefficient = _hTailEquilibriumLiftCoefficient;
	}

	public void set_totalEquilibriumLiftCoefficient(List<Double> _totalEquilibriumLiftCoefficient) {
		this._totalEquilibriumLiftCoefficient = _totalEquilibriumLiftCoefficient;
	}

	public void set_hTailEquilibriumLiftCoefficientConstant(List<Double> _hTailEquilibriumLiftCoefficientConstant) {
		this._hTailEquilibriumLiftCoefficientConstant = _hTailEquilibriumLiftCoefficientConstant;
	}

	public void set_totalEquilibriumLiftCoefficientConstant(List<Double> _totalEquilibriumLiftCoefficientConstant) {
		this._totalEquilibriumLiftCoefficientConstant = _totalEquilibriumLiftCoefficientConstant;
	}

	public void set_totalTrimDrag(List<Double> _totalTrimDrag) {
		this._totalTrimDrag = _totalTrimDrag;
	}

	public void set_deltaEEquilibrium(List<Amount<Angle>> _deltaEEquilibrium) {
		this._deltaEEquilibrium = _deltaEEquilibrium;
	}

	public void set_clMapForDeltaeElevator(Map<Amount<Angle>, Double[]> _clMapForDeltaeElevator) {
		this._clMapForDeltaeElevator = _clMapForDeltaeElevator;
	}

	public void setNumberOfIterationforDeltaE(int numberOfIterationforDeltaE) {
		this.numberOfIterationforDeltaE = numberOfIterationforDeltaE;
	}

	public void set_clWingDistribution(List<List<Double>> _clWingDistribution) {
		this._clWingDistribution = _clWingDistribution;
	}

	public void set_cl3DCurveWingFlapped(Double[] _cl3DCurveWingFlapped) {
		this._cl3DCurveWingFlapped = _cl3DCurveWingFlapped;
	}

	public void set_clHtailDistribution(List<List<Double>> _clHtailDistribution) {
		this._clHtailDistribution = _clHtailDistribution;
	}

	public void set_centerOfPressureWingDistribution(List<List<Double>> _centerOfPressureWingDistribution) {
		this._centerOfPressureWingDistribution = _centerOfPressureWingDistribution;
	}

	public void set_centerOfPressurehTailDistribution(List<List<Double>> _centerOfPressurehTailDistribution) {
		this._centerOfPressurehTailDistribution = _centerOfPressurehTailDistribution;
	}

	public void set_cMWingDistribution(List<List<Double>> _cMWingDistribution) {
		this._cMWingDistribution = _cMWingDistribution;
	}

	public void set_cMHTailDistribution(List<List<Double>> _cMHTailDistribution) {
		this._cMHTailDistribution = _cMHTailDistribution;
	}

	public void set_alphaIWingDistribution(List<List<Amount<Angle>>> _alphaIWingDistribution) {
		this._alphaIWingDistribution = _alphaIWingDistribution;
	}

	public void set_alphaIHtailDistribution(List<List<Amount<Angle>>> _alphaIHtailDistribution) {
		this._alphaIHtailDistribution = _alphaIHtailDistribution;
	}

	public void set_clNasaBlackwellDistributionModified(List<double[]> _clNasaBlackwellDistributionModified) {
		this._clNasaBlackwellDistributionModified = _clNasaBlackwellDistributionModified;
	}

	public void set_clZeroFlapped(Double _clZeroFlapped) {
		this._clZeroFlapped = _clZeroFlapped;
	}

	public void set_clAlphaDegFlapped(Double _clAlphaDegFlapped) {
		this._clAlphaDegFlapped = _clAlphaDegFlapped;
	}

	public void set_clAlphaRadFlapped(Double _clAlphaRadFlapped) {
		this._clAlphaRadFlapped = _clAlphaRadFlapped;
	}

	public void set_wingclAlphaFlapped(Amount<?> _wingclAlphaFlapped) {
		this._wingclAlphaFlapped = _wingclAlphaFlapped;
	}

	public void set_clMaxFlapped(Double _clMaxFlapped) {
		this._clMaxFlapped = _clMaxFlapped;
	}

	public void set_alphaStarFlapped(Amount<Angle> _alphaStarFlapped) {
		this._alphaStarFlapped = _alphaStarFlapped;
	}

	public void set_alphaStallFlapped(Amount<Angle> _alphaStallFlapped) {
		this._alphaStallFlapped = _alphaStallFlapped;
	}

	public void set_alphaStallLinearFlapped(Amount<Angle> _alphaStallLinearFlapped) {
		this._alphaStallLinearFlapped = _alphaStallLinearFlapped;
	}

	public void set_alphaZeroLiftFlapped(Amount<Angle> _alphaZeroLiftFlapped) {
		this._alphaZeroLiftFlapped = _alphaZeroLiftFlapped;
	}

	public void set_clMaxDistributionFlapped(List<Double> _clMaxDistributionFlapped) {
		this._clMaxDistributionFlapped = _clMaxDistributionFlapped;
	}

	public void setClDistributions(List<List<Double>> clDistributions) {
		this.clDistributions = clDistributions;
	}

	public void setCl3D(Double[] cl3d) {
		cl3D = cl3d;
	}

	public void set_horizontalTailCL(MethodEnum _horizontalTailCL) {
		this._horizontalTailCL = _horizontalTailCL;
	}

	public void set_wingHorizontalDistanceACtoCG(Amount<Length> _wingHorizontalDistanceACtoCG) {
		this._wingHorizontalDistanceACtoCG = _wingHorizontalDistanceACtoCG;
	}

	public void set_wingVerticalDistranceACtoCG(Amount<Length> _wingVerticalDistranceACtoCG) {
		this._wingVerticalDistranceACtoCG = _wingVerticalDistranceACtoCG;
	}



}
