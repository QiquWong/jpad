package analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import calculators.aerodynamics.NasaBlackwell;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;

/**
 * Evaluate and store aerodynamic parameters relative to the whole aircraft.
 * Calculations are handled through static libraries which are properly called in this class;
 * other methods are instead used to get the aerodynamic parameters from each component
 * in order to obtain quantities relative to the whole aircraft.
 */ 

public class ACAerodynamicCalculator {

	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW ACAerodynamicsManager (WORK IN PROGRESS)
	 * 
	 * @author Vittorio Trifari, Manuela Ruocco, Agostino De Marco
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	IACAerodynamicCalculator _theAerodynamicBuilderInterface;
	//..............................................................................
	// FROM INPUT (Passed from ACAnalysisManager)
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private List<ConditionEnum> _theCondition;
	//..............................................................................
	// FROM INPUT (Passed from File)
	private List<Double> _xCGAircraft;
	private List<Double> _zCGAircraft;
	private Amount<Angle> _alphaBodyInitial;
	private Amount<Angle> _alphaBodyFinal;
	private int _numberOfAlphasBody;
	private int _wingNumberOfPointSemiSpanWise;
	private List<Amount<Angle>> _alphaWingForDistribution;
	private List<Amount<Angle>> _alphaHorizontalTailForDistribution;
	private boolean _downwashConstant; // if TRUE--> constant, if FALSE--> variable
	private Double _wingMomentumPole;  // pole referred to M.A.C.
	private Double _hTailMomentumPole; // pole referred to M.A.C.
	private Double _dynamicPressureRatio;
	//..............................................................................
	// DERIVED INPUT
	
	// Global
	private List<Amount<Angle>> _alphaBodyList;
	
	// Wing
	private Amount<Length> _wingZACRoot;
	private Double _wingVortexSemiSpanToSemiSpanRatio;
	private double _wingCLAlphaMachZero;
	private Airfoil _wingMeanAirfoil;
	
	
	//..............................................................................
	// OUTPUT
	
	

	
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
