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

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.ACPerformanceManager.ACPerformanceCalculatorBuilder;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
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
	private List<Double> _wingCmACDistribution;  // not from input

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
	private List<Double> _hTailCmACDistribution;  // not from input

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
	double [] alphaZeroLiftRadHTail;
	double [] twistDistributionRadHTail;

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
	private Double _deltaCMc4;
	private Double[] _alphaArrayPlotHighLift;
	private Double[] _wingLiftCoefficient3DCurveHighLift;
	private Double[] _wingLiftCoefficient3DCurveHighLiftWINGARRAY;


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
	private Map <Amount<Angle>, Double> _cLAlphaElevatorDeg = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double> _deltacLZeroElevator = new HashMap<Amount<Angle>, Double>();
	private Map <Amount<Angle>, Double[]>  _hTailLiftCoefficient3DCurveWithElevator = new HashMap<Amount<Angle>, Double[]>();

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
	private Double _fuselageWingClAlphaDeg;
	private Double _fuselageWingClMax;
	private Double _fuselageWingClZero;
	private Amount<Angle> _fuselageWingAlphaStar;
	private Amount<Angle> _fuselageWingAlphaStall;
	private Amount<?> _fuselageWingClAlpha;
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
	//------------------------------------------------
	private List<Double> _wingDragCoefficient3DCurve = new ArrayList<Double>();
	private List<List<Double>> _wingAirfoilsCoefficientCurve;

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
	private Map<MethodEnum, Double> _fuselageCM0 = new HashMap<MethodEnum, Double>();
	private Map<MethodEnum, Double> _fuselageCMAlpha = new HashMap<MethodEnum, Double>();
	private List<Double> _fuselageMomentCoefficient = new ArrayList<Double>();
	private Map<MethodEnum, Amount<Length>> _wingBodyXACBRF = new HashMap<MethodEnum, Amount<Length>>();
	private Double _deltaXACdueToFuselage;

	
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
	private List<Amount<Angle>> _deltaEEquilibrium = new ArrayList<>();
	//Distributions -------------------------------------------
	//----------------------------------------------------------------
	
	private List<List<Double>> _clWingDistribution = new ArrayList<>();
	private List<List<Double>> _clHtailDistribution = new ArrayList<>();
	private List<List<Double>> _centerOfPressureWingDistribution = new ArrayList<>();
	private List<List<Double>> _centerOfPressurehTailDistribution = new ArrayList<>();
	private List<List<Double>> _cMWingDistribution = new ArrayList<>();
	private List<List<Double>> _cMHTailDistribution = new ArrayList<>();
	private List<List<Amount<Angle>>> _alphaIWingDistribution = new ArrayList<>();
	private List<List<Amount<Angle>>> _alphaIHtailDistribution = new ArrayList<>();
	
	
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

		this._dynamicPressureRatio = AerodynamicCalc.calculateDynamicPressureRatio(
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

						// xle
						Double [] xleDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingXleBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingXleDistribution = new ArrayList<>();
						for(int i=0; i<xleDistributionArray.length; i++)
							_wingXleDistribution.add(Amount.valueOf(xleDistributionArray[i], SI.METER));

						// yle
						
						_wingYLEBreakPoints = new ArrayList<>();
						this._wingYLEDistribution = new ArrayList<>();
						if(_wingDihedralBreakPoints.get(0).equals(Amount.valueOf(0.0,NonSI.DEGREE_ANGLE))){
							for(int i=0; i<_wingYAdimensionalDistribution.size(); i++)
								_wingYLEDistribution.add(Amount.valueOf(0.0, SI.METER));
							}
						
						else{
						
						
						if(_wingNumberOfGivenSections == 3){
						Amount<Length> yLEKink = Amount.valueOf(
								_wingAdimentionalKinkStation*
								_wingSemiSpan.doubleValue(SI.METER)*
								Math.tan(_wingDihedralBreakPoints.get(_wingNumberOfGivenSections-1).doubleValue(SI.RADIAN)),
								SI.METER
								);
						
						Amount<Length> yLETip = Amount.valueOf(
								_wingSemiSpan.doubleValue(SI.METER)*
								Math.tan(_wingDihedralBreakPoints.get(_wingNumberOfGivenSections-1).doubleValue(SI.RADIAN)),
								SI.METER
								);
						
						_wingYLEBreakPoints.add(0, Amount.valueOf(0.0,SI.METER));
						_wingYLEBreakPoints.add(1, yLEKink);
						_wingYLEBreakPoints.add(2, yLETip);
						}
						
						if(_wingNumberOfGivenSections == 2){
						Amount<Length> yLETip = Amount.valueOf(
								_wingSemiSpan.doubleValue(SI.METER)*
								Math.tan(_wingDihedralBreakPoints.get(_wingNumberOfGivenSections-1).doubleValue(SI.RADIAN)),
								SI.METER
								);
						
						_wingYLEBreakPoints.add(0, Amount.valueOf(0.0,SI.METER));
						_wingYLEBreakPoints.add(1, yLETip);
						}
						
						Double [] yLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
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

						// dihedral
						Double [] dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingDihedralDistribution = new ArrayList<>();
						for(int i=0; i<dihedralDistributionArray.length; i++)
							_wingDihedralDistribution.add(Amount.valueOf(dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));

						// alpha zero lift
						Double [] alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaZeroLiftBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingAlphaZeroLiftDistribution = new ArrayList<>();
						for(int i=0; i<alphaZeroLiftDistributionArray.length; i++)
							_wingAlphaZeroLiftDistribution.add(Amount.valueOf(alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));

						// alpha star
						Double [] alphaStarDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertListOfAmountTodoubleArray(_wingAlphaStarBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingAlphaStarDistribution = new ArrayList<>();
						for(int i=0; i<alphaStarDistributionArray.length; i++)
							_wingAlphaStarDistribution.add(Amount.valueOf(alphaStarDistributionArray[i], NonSI.DEGREE_ANGLE));

						// cl max
						Double [] clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingClMaxBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingClMaxDistribution = new ArrayList<>();
						for(int i=0; i<clMaxDistributionArray.length; i++)
							_wingClMaxDistribution.add(clMaxDistributionArray[i]);

						//cl alpha
						Double [] clAlphaDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalBreakPoints),
								MyArrayUtils.convertToDoublePrimitive(_wingClAlphaBreakPointsDeg),
								MyArrayUtils.convertToDoublePrimitive(_wingYAdimensionalDistribution)
								);	
						this._wingClAlphaDistributionDeg = new ArrayList<>();
						for(int i=0; i<clAlphaDistributionArray.length; i++)
							_wingClAlphaDistributionDeg.add(clAlphaDistributionArray[i]);

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
						this._wingCmACDistribution = new ArrayList<>();
						for(int i=0; i<cmacDistributionArray.length; i++)
							_wingCmACDistribution.add(cmacDistributionArray[i]);
						
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
						this._hTailCmACDistribution = new ArrayList<>();
						for(int i=0; i<cmacDistributionArrayHTail.length; i++)
							_hTailCmACDistribution.add(cmacDistributionArrayHTail[i]);


						alphaZeroLiftRad = new double [_wingNumberOfPointSemiSpanWise];
						twistDistributionRad = new double [_wingNumberOfPointSemiSpanWise];
						for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
							alphaZeroLiftRad[i] = _wingAlphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
							twistDistributionRad[i] =  _wingTwistDistribution.get(i).doubleValue(SI.RADIAN);
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
						this._horizontalDistanceQuarterChordWingHTail =  this._horizontalDistanceQuarterChordWingHTail;

						// the horizontal distance is always the same, the vertical changes in function of the angle of attack.

						
	// FUSELAGE -----------
						
 _fuselageFrontSurface = Amount.valueOf(Math.PI*Math.pow(_fuselageDiameter.doubleValue(SI.METER), 2)/4, SI.SQUARE_METRE);

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
				twistDistributionRad,
				alphaZeroLiftRad,
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
				twistDistributionRad,
				alphaZeroLiftRad,
				_wingVortexSemiSpanToSemiSpanRatio,
				0.0,
				0.0,
				this.getAltitude().doubleValue(SI.METER)
				);

		// horizontal tail
		theNasaBlackwellCalculatorMachActualHTail = new NasaBlackwell(
				this._hTailSemiSpan.doubleValue(SI.METER),
				this._hTailSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._hTailXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_wingDihedralDistributionNull),
				twistDistributionRadHTail,
				alphaZeroLiftRadHTail,
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

		double downwashGradientConstantSlingerland = theStabilityCalculator.calculateDownwashGradientSlingerland(
				_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
				_verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER), 
				this._wingcLAlphaRadCONDITION, 
				this._wingSweepQuarterChord,
				this._wingAspectRatio, 
				this._wingSemiSpan);

		for (int i=0; i<this._numberOfAlphasBody; i++){
			_downwashGradientConstantSlingerland.add(downwashGradientConstantSlingerland);
		}

		double epsilonZeroSlingerland = - downwashGradientConstantSlingerland * 
				_wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE);

		//fill the downwash array
		for (int i=0; i<this._numberOfAlphasBody; i++){
			this._downwashAngleConstantSlingerland.add(i,
					Amount.valueOf( epsilonZeroSlingerland + 
							_downwashGradientConstantSlingerland.get(i)*
							_alphasWing.get(i).doubleValue(NonSI.DEGREE_ANGLE)
							, NonSI.DEGREE_ANGLE));
		}


		//--------------end linear downwash-----------------------------------------


			theStabilityCalculator.calculateDownwashNonLinearSlingerland(
					this, 
					_horizontalDistanceQuarterChordWingHTail,
					_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
					MyArrayUtils.convertToDoublePrimitive(this._wingclAlphaArrayCONDITION),
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
								this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))-
								this._downwashAngleConstantRoskam.get(i).doubleValue(NonSI.DEGREE_ANGLE)+this._hTailAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE)
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
								this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))-
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
		System.out.println("\nCl max Distribution --> " + this._wingClMaxDistribution);
		if(_wingDragMethod == MethodEnum.AIRFOIL_INPUT){
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
		if(_wingDragMethod == MethodEnum.AIRFOIL_INPUT){
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
		if(_hTailDragMethod == MethodEnum.AIRFOIL_INPUT){
			sb.append("\t\tCL Htail = " + Arrays.toString(_hTailliftCoefficient3DCurve) + "\n")
			.append("\t\tCD Parasite = " + _hTailParasiteDragCoefficientDistribution+ "\n")
			.append("\t\tCD Induced = " + _hTailInducedDragCoefficientDistribution+ "\n")
			.append("\t\tCD Total = " + _hTailDragCoefficient3DCurve+ "\n")
			.append("\t\tEta stations = " + _hTailYAdimensionalDistribution+ "\n")
			;
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
		sb.append("\t\tMoment Coefficient with respect to AC FINAL " + _wingFinalMomentumPole + " --> " + _wingMomentCoefficientConstant + "\n");
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
		sb.append("\t\tMoment Coefficient with respect to AC FINAL with cm ac variable " + _wingFinalMomentumPole + " --> " +_wingMomentCoefficientFinalACVariable+ "\n")
		;
		}
		double xac = _wingFinalMomentumPole + _deltaXACdueToFuselage;
		sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCM wing respect to CG with pendular stability, AC wing = " + _wingFinalMomentumPole + " AC wing body " + 
				  xac + " = " + _wingMomentCoefficientPendular + "\n")
		.append("\t\tCM wing respect to CG without pendular stability, AC wing = " + _wingFinalMomentumPole + " AC wing body " + 
				  xac + " = " + _wingMomentCoefficientNOPendular + "\n")
		;
		
		sb.append("\t-------------------------------------\n")
		.append("\tHorizontal Tail\n")
		.append("\t-------------------------------------\n")
		.append("\t\tMAC = " +_hTailMAC+ "\n")
		.append("\t\tx MAC = " +_hTailMeanAerodynamicChordLeadingEdgeX+ "\n")
		.append("\t\tXAC MAC = " +_hTailXACMAC+ "\n")
		.append("\t\tXAC MAC percent = " +_hTailXACMACpercent+ "\n")
		.append("\t\tXAC LRF = " +_hTailXACLRF+ "\n")
		.append("\t\tXAC BRF = " +_hTailXACBRF+ "\n")
		.append("\t\teta Stations = " +_hTailYAdimensionalDistribution+ "\n")
		.append("\t\tMoment Coefficient with respect to AC DE Young Harper --> " +_hTailMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)+ "\n")
		.append("\t\tMoment Coefficient with respect to AC NAPOLITANO DATCOM --> " +_hTailMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)+ "\n")
		;
		for (int i=0; i<_hTailMomentumPole.size(); i++){
			sb.append("\t\tMoment with Coefficient respect to " + _hTailMomentumPole.get(i) + "--> " +_hTailMomentCoefficients.get(i)+ "\n");
		}
		;
		;
		
		sb.append("\t-------------------------------------\n")
		.append("\tFuselage\n")
		.append("\t-------------------------------------\n")
		.append("\t\tCM0 = " +_fuselageCM0+ "\n")
		.append("\t\tCM alpha = " +_fuselageCMAlpha+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCM fuselage = " +_fuselageMomentCoefficient+ "\n")
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
		sb.append("\t\tCL Eq. = " +_totalEquilibriumLiftCoefficient+ "\n")
;
	

		sb.append("\nCOMPONENTS MOMENT COEFFICIENT REAPECT TO CG \n")
		.append("-------------------------------------\n")
		.append("\t\tX cg = " + _xCGAircraft + " Y cg = " + _yCGAircraft + " Z cg = " + _zCGAircraft + "\n\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._alphasBody, "\t\tAlpha Body", ","))
		.append("\t\tCM wing no pendular stability = " + _wingMomentCoefficientNOPendular + "\n")
		.append("\t\tCM wing with pendular stability = " + _wingMomentCoefficientPendular + "\n")
		.append("\t\tCM Horizontal tail = " + _hTailMomentCoefficientPendular + "\n")
	    .append("\t\tCM Fuselage = " + _fuselageMomentCoefficient + "\n")
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
		sb.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasTail, "\n\t\tAlpha Body", ","))
		.append("\t\tcl wing = " + _hTailEquilibriumLiftCoefficient + "\n\n");
		return sb.toString();
	}

	public void plot( String folderPathName, String folderPathNameDistribution) throws InstantiationException, IllegalAccessException{

		// DOWNWASH e DOWNWASH GRADIENT	
		//------------------------------------------------------------------------------------------------------------
		if ( this._downwashConstant == Boolean.TRUE){
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE)) {

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
						"Downwash Angle");

				System.out.println("Plot Downwash Angle Chart ---> DONE \n");
			}


			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT)) {

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
						"Downwash Gradient");

				System.out.println("Plot Downwash Gradient Chart ---> DONE \n");

			}
		}

		if ( _downwashConstant == Boolean.FALSE){
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE)) {
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
						"Downwash Angle");

				System.out.println("Plot Downwash Angle Chart ---> DONE \n");
			}


			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT)) {
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
						"Downwash Gradient");

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
						"Horizontal Distance");

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
						"Vertical Distance");

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
					"Wing Lift Coefficient 3D curve clean");

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
					"Wing Cl distribution at CL max Clean");

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
						"Wing Lift Coefficient 3D curve High lift ");

				
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
					"Horizontal Tail Lift Coefficient 3D curve clean");

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
			yList.add(MyArrayUtils.convertFromDoublePrimitive(_hTailliftCoefficientDistributionatCLMax));
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
					"Horizontal Tail Cl distribution at CL max Clean");

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
					"Horizontal Tail CL vs alpha with elevator deflection");

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
					"Wing Fuselage Lift Coefficient 3D curve clean");

			System.out.println("Plot CL wing fuselage clean Chart ---> DONE \n");
		}
		
		// CD
		//------------------------------------------------------------------------------------------------------------
		
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_CLEAN)){
			if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT){
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
					"Wing Parasite drag Coefficient");

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
					"Wing Induced drag Coefficient");

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
					"Wing Total drag Coefficient");

			System.out.println("Plot wing Total drag Coefficient chart ---> DONE \n");
			
			}
		}
		
		//htail--------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE_CLEAN)){
			if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT){
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
					"Horizontal Tail Parasite drag Coefficient");

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
					"Horizontal Tail Induced drag Coefficient");

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
					"Horizontal Tail Total drag Coefficient");

			System.out.println("Plot Horizontal Tail Total drag Coefficient chart ---> DONE \n");
			}
		}

		// MOMENT
		//------------------------------------------------------------------------------------------------------------
		if(_plotList.contains(AerodynamicAndStabilityPlotEnum.FUSELAGE_CM_PLOT)) {

			List<Double[]> xList = new ArrayList<>();
			List<Double[]> yList = new ArrayList<>();
			List<String> legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_fuselageMomentCoefficient));
			legend.add("null");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Fuselage Moment Coefficient", 
					"alpha_b", "CM", 
					null, null,
					null, null,
					"deg", "",
					false,
					legend,
					folderPathName,
					"Fuselage Moment Coefficient");

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

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientAC.get(MethodEnum.DEYOUNG_HARPER)));
			yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_wingMomentCoefficientAC.get(MethodEnum.NAPOLITANO_DATCOM)));
			legend.add("AC calculated by De Young Harper formula");
			legend.add("AC calculated by Napoltano-Datcom formula");

			MyChartToFileUtils.plot(
					xList, 
					yList, 
					"Wing Moment Coefficient with respect to AC", 
					"alpha_w", "CM", 
					null, null,
					null, null,
					"deg", "",
					true,
					legend,
					folderPathName,
					"Wing Moment Coefficient with respect to AC");

		System.out.println("Plot Wing Moment Coefficient Chart with respect to AC---> DONE \n");
		
		xList = new ArrayList<>();
		yList = new ArrayList<>();
		legend = new ArrayList<>();

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
				"Wing Moment Coefficient with respect to AC indicated = " + _wingFinalMomentumPole);

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
					"Wing Moment Coefficient");
		
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
					"Horizontal Tail Moment Coefficient with respect to AC");

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
					"Horizontal Tail Moment Coefficient");
		
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
					"Horizontal Tail Moment Coefficient");
		
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
					"Total Lift Coefficient");
		
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
					false,
					legend,
					folderPathName,
					"Horizontal tail equilibrium lift coefficient");
		
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
					"Total equilibrium lift coefficient");
		
			System.out.println("Total equilibrium lift coefficient ---> DONE \n");
			
			
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
					"Components Moment coefficient with respect TO CG");
		
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
						"Total Moment Coefficient vs CL");
			
				System.out.println("Plot Total Lift Coefficient Chart var delta e ---> DONE \n");
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
							"Total Moment Coefficient vs alpha");
				
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
					"Wing Lift Coefficient Distribution");
		
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
					"Horizontal Tail Lift Coefficient Distribution");
		
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
					"Wing Moment Coefficient Distribution with respect to " + _wingFinalMomentumPole);
		
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
					"Horizontal Tail Moment Coefficient Distribution with respect to " + _hTailFinalMomentumPole);
		
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
				"Wing Induced angle of attack distribution");

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
				"Horizontal Tail Induced angle of attack distribution");

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
				"Wing center of pressuredistribution");

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
				"Horizontal Tail center of pressuredistribution");

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

	public void calculateWingHighLiftCharacteristics(){
		double cLCurrent = theNasaBlackwellCalculatorMachActualWing.getCLCurrent();

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

	public void calculateHTailLiftCharacteristics(){
		// cl alpha 
		theNasaBlackwellCalculatorMachActualHTail.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachActual = theNasaBlackwellCalculatorMachActualHTail.getCLCurrent();
		theNasaBlackwellCalculatorMachActualHTail.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachActual = theNasaBlackwellCalculatorMachActualHTail.getCLCurrent();
		this._hTailcLAlphaRad = (clTwoMachActual-clOneMachActual)/toRadians(4);
		this._hTailcLAlphaDeg = (clTwoMachActual-clOneMachActual)/(4);
		this._hTailclAlpha = Amount.valueOf( this._hTailcLAlphaRad , SI.RADIAN.inverse());

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
		Amount<Angle> deltaAlpha = Amount.valueOf(
				aeroDatabaseReader
				.getDAlphaVsLambdaLEVsDy(
						_hTailSweepLE.doubleValue(NonSI.DEGREE_ANGLE),
						deltaYPercent
						),
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

			_tauElevator.put(_anglesOfElevatorDeflection.get(i),
					LiftCalc.calculateTauIndexElevator(
							_elevatorCfC, 
							_hTailAspectRatio,
							highLiftDatabaseReader, 
							aeroDatabaseReader, 
							_anglesOfElevatorDeflection.get(i)
							));

			//------------------------------------------------------
			// ALPHA ZERO LIFT HIGH LIFT
			_hTailalphaZeroLiftElevator.put(
					_anglesOfElevatorDeflection.get(i),
					Amount.valueOf(
							_hTailAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE) - 
							(_tauElevator.get(_anglesOfElevatorDeflection.get(i))*_anglesOfElevatorDeflection.get(i).doubleValue(NonSI.DEGREE_ANGLE)),
							NonSI.DEGREE_ANGLE));

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

		 this._deltaEAnglesArray = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.convertFromDoublePrimitive(
						MyArrayUtils.linspace(-30, 5, 50)), NonSI.DEGREE_ANGLE);
		
		
		for (int i=0; i<this._deltaEAnglesArray.size(); i++){

			theStabilityCalculator.calculateElevatorEffects(
					this,
					_deltaEAnglesArray.get(i));

			_tauElevatorArray.put(_deltaEAnglesArray.get(i),
					LiftCalc.calculateTauIndexElevator(
							_elevatorCfC, 
							_hTailAspectRatio,
							highLiftDatabaseReader, 
							aeroDatabaseReader, 
							_deltaEAnglesArray.get(i)
							)); 
		}
	}


	public void calculateWingFuselageLiftCharacterstics(){

		// cl alpha	
		_fuselageWingClAlphaDeg = LiftCalc.calculateCLAlphaFuselage(
				_wingcLAlphaDegCONDITION, 
				_wingSpan, 
				_fuselageDiameter);

		_fuselageWingClAlpha = Amount.valueOf(this._fuselageWingClAlphaDeg, NonSI.DEGREE_ANGLE.inverse());

		// cl Zero
		_fuselageWingClZero = -_fuselageWingClAlphaDeg * _wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE);

		// cl max
		_fuselageWingClMax = _wingcLMaxCONDITION;

		//cl star
		_fuselageWingCLStar = _wingcLStarCONDITION;

		//alphaStar
		_fuselageWingAlphaStar = Amount.valueOf(
				(_fuselageWingCLStar - _fuselageWingClZero)/_fuselageWingClAlphaDeg, 
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
				this._fuselageWingClAlpha,
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

		if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT){	 // _wingliftCoefficient3DCurveCONDITION, _wingCdAirfoilDistribution
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
			clListDragWing = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoublePrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));

		_wingCdAirfoilDistribution = AerodynamicCalc.calculateCDorCMMatrixAirfoils(
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
		
		_wingCLAirfoilsDistributionFinal = AerodynamicCalc.calculateCLMatrixAirfoils(
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
			clListMomentWing = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoublePrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));
			_wingCLMomentAirfoilOutput = clListMomentWing;
			
			// matrix
			_wingCMMomentAirfoilOutput = AerodynamicCalc.calculateCDorCMMatrixAirfoils(
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

		if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT){	 // _wingliftCoefficient3DCurveCONDITION, _wingCdAirfoilDistribution

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
			clListDragTail = MyArrayUtils.convertDoubleArrayToListDouble(MyArrayUtils.convertFromDoublePrimitive(
					MyArrayUtils.linspace(minValue, maxValue, _numberOfAlphasBody)));
	
		
		_hTailCdAirfoilDistribution = AerodynamicCalc.calculateCDorCMMatrixAirfoils(
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
		
		_hTailCLAirfoilsDistributionFinal = AerodynamicCalc.calculateCLMatrixAirfoils(
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
		
		if(this._wingDragMethod==MethodEnum.AIRFOIL_INPUT){
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
				

			
			// TOTAL DRAG--------------------------------------------
			
			for (int i=0; i<_numberOfAlphasBody; i++){
				_wingDragCoefficient3DCurve.add(
						i,
						_wingParasiteDragCoefficientDistribution.get(i)+_wingInducedDragCoefficientDistribution.get(i));
			}
			
		}

	}
	public void calculateHTailDragCharacterstics(){
		if(this._hTailDragMethod==MethodEnum.AIRFOIL_INPUT){
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
			
			
			// TOTAL DRAG--------------------------------------------
			
		for (int i=0; i<_numberOfAlphasBody; i++){
			_hTailDragCoefficient3DCurve.add(
					i,
					_hTailParasiteDragCoefficientDistribution.get(i)+_hTailInducedDragCoefficientDistribution.get(i));
		}			
	}

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
		_deltaXACdueToFuselage = - (_fuselageCMAlpha.get(MethodEnum.FUSDES)/_wingcLAlphaDegCONDITION);
		
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
	// respect TO AC DE YOUNG HARPER	
	Amount<Length> momentumPoleYH = Amount.valueOf( 
			_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
			_wingXACMACpercent.get(MethodEnum.DEYOUNG_HARPER)* 
			_wingMAC.doubleValue(SI.METER), SI.METER);
		
	if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){
		_wingMomentCoefficientAC.put(MethodEnum.DEYOUNG_HARPER, 
				MomentCalc.calcCMLiftingSurfaceWithIntegral(
				theNasaBlackwellCalculatorMachActualWing, 
				_alphasWing, 
				_wingMAC,
				_wingYDistribution, 
				_wingCl0Distribution, 
				_wingClAlphaDistributionDeg, 
				_wingCmACDistribution, 
				_wingXACDistribution, 
				_wingChordsDistribution, 
				_wingXleDistribution, 
				_wingCLAirfoilsDistributionFinal, 
				_alphasWing,
				_wingSurface, 
				momentumPoleYH
				));
	}
	
	if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){
		_wingMomentCoefficientAC.put(MethodEnum.DEYOUNG_HARPER, 
				MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
				theNasaBlackwellCalculatorMachActualWing, 
				_alphasWing, 
				_wingMAC,
				_wingYDistribution, 
				_wingCl0Distribution, 
				_wingClAlphaDistributionDeg, 
				_wingCLMomentAirfoilOutput,
				_wingCMMomentAirfoilOutput,
				_wingXACDistribution, 
				_wingChordsDistribution, 
				_wingXleDistribution, 
				_wingCLAirfoilsDistributionFinal, 
				_alphasWing,
				_wingSurface, 
				momentumPoleYH
				));
	}
		// respect TO AC NAPOLITANO DATCOM	
		Amount<Length> momentumPoleND = Amount.valueOf( 
				_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
				_wingXACMACpercent.get(MethodEnum.NAPOLITANO_DATCOM)* 
				_wingMAC.doubleValue(SI.METER), SI.METER);
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.CONSTANT){		
			_wingMomentCoefficientAC.put(MethodEnum.NAPOLITANO_DATCOM, 
					MomentCalc.calcCMLiftingSurfaceWithIntegral(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphasWing, 
					_wingMAC,
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCmACDistribution, 
					_wingXACDistribution, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingSurface, 
					momentumPoleND
					));
		}
		
		if(this.getWingairfoilMomentCoefficientCurve()==MethodEnum.INPUTCURVE){		
			_wingMomentCoefficientAC.put(MethodEnum.NAPOLITANO_DATCOM, 
					MomentCalc.calcCMLiftingSurfaceWithIntegralACVariable(
					theNasaBlackwellCalculatorMachActualWing, 
					_alphasWing, 
					_wingMAC,
					_wingYDistribution, 
					_wingCl0Distribution, 
					_wingClAlphaDistributionDeg, 
					_wingCLMomentAirfoilOutput,
					_wingCMMomentAirfoilOutput,
					_wingXACDistribution, 
					_wingChordsDistribution, 
					_wingXleDistribution, 
					_wingCLAirfoilsDistributionFinal, 
					_alphasWing,
					_wingSurface, 
					momentumPoleND
					));
		}
			
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
						_wingCmACDistribution, 
						_wingXACDistribution, 
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
						_wingXACDistribution, 
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

			_wingMomentCoefficientFinal = 
						MomentCalc.calcCMLiftingSurfaceWithIntegral(
						theNasaBlackwellCalculatorMachActualWing, 
						_alphasWing, 
						_wingMAC,
						_wingYDistribution, 
						_wingCl0Distribution, 
						_wingClAlphaDistributionDeg, 
						_wingCmACDistribution, 
						_wingXACDistribution, 
						_wingChordsDistribution, 
						_wingXleDistribution, 
						_wingCLAirfoilsDistributionFinal, 
						_alphasWing,
						_wingSurface, 
						momentumPoleFinal
						);
			
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
							_wingXACDistribution, 
							_wingChordsDistribution, 
							_wingXleDistribution, 
							_wingCLAirfoilsDistributionFinal, 
							_alphasWing,
							_wingSurface, 
							momentumPoleFinal
							);
			_wingMomentCoefficientConstant = _wingMomentCoefficientFinal ;
			_wingMomentCoefficientFinal = _wingMomentCoefficientFinalACVariable;
			}	
					
		
	}	
	
	
	public void calculateHtailMomentCharacterstics(){
		
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
						_hTailCmACDistribution, 
						_hTailXACDistribution, 
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
						_hTailCmACDistribution, 
						_hTailXACDistribution, 
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
							_hTailCmACDistribution, 
							_hTailXACDistribution, 
							_hTailChordsDistribution, 
							_hTailXleDistribution, 
							_hTailCLAirfoilsDistributionFinal, 
							_alphasTail,
							_hTailSurface, 
							hTailMomentumPoleYH
							));
		}
	}
	public void calculateFuselageMomentCharacterstics(){
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
		
		
		System.out.println(" CMO " + fusDesDatabaseReader.getCM0FR());
		System.out.println(" CMn " + fusDesDatabaseReader.getdCMn());
		System.out.println(" CMt " + fusDesDatabaseReader.getdCMt());
		System.out.println(" fus surface " + fusSurfRatio);
		System.out.println(" fus diam  " + _fuselageDiameter);
		System.out.println(" wing mac " + _wingMAC);
		
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
							_alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) - 
							_wingAlphaZeroLiftCONDITION.doubleValue(NonSI.DEGREE_ANGLE)) + _fuselageCM0.get(MethodEnum.FUSDES));
		}
	}

	public void calculateTotalLiftCoefficient(){
		List<Double> _clTotalList;
		for (int i=0; i<this._anglesOfElevatorDeflection.size(); i++){
			_clTotalList = new ArrayList<>();
			for (int ii=0; ii<getNumberOfAlphasBody(); ii++){
				_clTotalList.add(_wingliftCoefficient3DCurveCONDITION[ii] + (
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
			_wingNormalCoefficient.add(
					i,
					_wingliftCoefficient3DCurveCONDITION[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN))+
					_wingDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)));

			_wingHorizontalCoefficient.add(
					i,
					_wingDragCoefficient3DCurve.get(i)*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)) - 
					_wingliftCoefficient3DCurveCONDITION[i]*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN))
					);	
		}
		
		// moment 
		Amount<Length> _wingHorizontalDistanceACtoCG,_wingVerticalDistranceACtoCG;
		
		
		_wingHorizontalDistanceACtoCG = Amount.valueOf(
				_xCGAircraft.doubleValue(SI.METER) - (
						_xApexWing.doubleValue(SI.METER) +
						_wingMeanAerodynamicChordLeadingEdgeX.doubleValue(SI.METER) + 
						(_wingFinalMomentumPole)*_wingMAC.doubleValue(SI.METER)
						), 
				SI.METER);
		
		double zVerticalDistance;

			zVerticalDistance = _zApexWing.doubleValue(SI.METER)+_wingZACMAC.doubleValue(SI.METER) - _zCGAircraft.doubleValue(SI.METER);
		
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
						_hTailliftCoefficient3DCurve[i]*Math.cos(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantRoskam.get(i).doubleValue(SI.RADIAN))+
						_hTailDragCoefficient3DCurve.get(i)*Math.sin(_alphasBody.get(i).doubleValue(SI.RADIAN)-_downwashAngleConstantRoskam.get(i).doubleValue(SI.RADIAN)));

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
							);
				}
			}
			if(_downwashConstant == Boolean.TRUE){
				for (int i=0; i<_numberOfAlphasBody; i++){
					_hTailMomentCoefficientPendular.add(i,
							(_hTailNormalCoefficientDownwashConstant.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE))*(_hTailHorizontalDistanceACtoCG.doubleValue(SI.METER)/ _wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio)) 
							+ (_hTailHorizontalCoefficientDownwashConstant.get(i)*(_hTailSurface.doubleValue(SI.SQUARE_METRE)/_wingSurface.doubleValue(SI.SQUARE_METRE)) * (_hTailVerticalDistranceACtoCG.doubleValue(SI.METER)/_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio))
							);
				}
			}


		
			//PROPELLER------------------------------------
		
			//TOTAL------------------------------------
			// delta e =0
			for (int ii=0; ii<_numberOfAlphasBody; ii++){
			_totalMomentCoefficientPendular.add(ii,
					_wingMomentCoefficientPendular.get(ii) +
					_fuselageMomentCoefficient.get(ii) +
					_hTailMomentCoefficientPendular.get(ii));
			}
	
			
			//other values
			List<Double> normalCoefficients, horizontalCoefficients, momentCoefficentHTail, momentCoefficientTotal ;
			
			//normal and horizontal forces
			for (int i=0; i<_anglesOfElevatorDeflection.size(); i++){
				normalCoefficients = new ArrayList<>();
				horizontalCoefficients = new ArrayList<>();
				momentCoefficentHTail = new ArrayList<>();
				momentCoefficientTotal = new ArrayList<>();
			
				if(_downwashConstant == Boolean.TRUE){
					for (int ii=0; ii<_numberOfAlphasBody; ii++){
						normalCoefficients.add(ii,
								_hTailLiftCoefficient3DCurveWithElevator.get(_anglesOfElevatorDeflection.get(i))[ii]*
								Math.cos(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN))+
								_hTailDragCoefficient3DCurve.get(ii)*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleConstantRoskam.get(ii).doubleValue(SI.RADIAN)));

						horizontalCoefficients.add(ii, 
								_hTailDragCoefficient3DCurve.get(i)*
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
								_hTailDragCoefficient3DCurve.get(ii)*
								Math.sin(_alphasBody.get(ii).doubleValue(SI.RADIAN)-
										_downwashAngleVariableSlingerland.get(ii).doubleValue(SI.RADIAN)));

						horizontalCoefficients.add(ii,
								_hTailDragCoefficient3DCurve.get(ii)*
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
											_wingMAC.doubleValue(SI.METER)*_dynamicPressureRatio))
							);
				
				momentCoefficientTotal.add(ii,
						_wingMomentCoefficientPendular.get(ii) + _fuselageMomentCoefficient.get(ii) + momentCoefficentHTail.get(ii));
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
				(-_wingMomentCoefficientPendular.get(i)-_fuselageMomentCoefficient.get(i))*
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
				MyArrayUtils.linspace(-45, 10, 12), NonSI.DEGREE_ANGLE);
		Map <Amount<Angle>, Double[]> _clMapForDeltaeElevator = new HashMap<Amount<Angle>, Double[]>();
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
		
		for (int i=0; i<_numberOfAlphasBody-10; i++){
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
							MyArrayUtils.convertFromDoublePrimitive(
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
							MyArrayUtils.convertFromDoublePrimitive(
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
					_wingCmACDistribution, 
					_wingXACDistribution, 
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
					_hTailCmACDistribution, 
					_hTailXACDistribution, 
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
							_wingCmACDistribution,
							_wingXACDistribution, 
							_wingCLAirfoilsDistributionFinal, 
							_alphasWing));
		}
		
		for (int i=0; i<alphaTailSize; i++){
			_centerOfPressurehTailDistribution.add(i, 
					AerodynamicCalc.calcCenterOfPressureDistribution(
							theNasaBlackwellCalculatorMachActualHTail,  
							_alphaHorizontalTailForDistribution.get(i), 
							_hTailCl0Distribution, 
							_hTailClAlphaistributionDeg, 
							_hTailCmACDistribution, 
							_hTailXACDistribution, 
							_hTailCLAirfoilsDistributionFinal, 
							_alphasTail));
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
		return _wingCmACDistribution;
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
		return _hTailCmACDistribution;
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
		this._wingCmACDistribution = _wingCmACDistribution;
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
		this._hTailCmACDistribution = _hTailCmACDistribution;
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
}
