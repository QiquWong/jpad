package sandbox2.mr;

import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.ACPerformanceCalculator.ACPerformanceCalculatorBuilder;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.NasaBlackwell;
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

	//Wing -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Length> _xApexWing;
	private Amount<Length> _yApexWing;
	private Amount<Length> _zApexWing;

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

	private AirfoilFamilyEnum _wingMeanAirfoilFamily;
	private Double _wingMaxThicknessMeanAirfoil;

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

	private List<Double> _wingClAlphaDistributionDeg;
	private List<Double> _wingMaxThicknessBreakPoints;
	private List<Double> _wingMaxThicknessDistribution;  // not from input
	private List<Amount<Length>> _wingLERadiusBreakPoints;
	private List<Amount<Length>> _wingLERadiusDistribution;  // not from input


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


	//Horizontal Tail -------------------------------------------
	//----------------------------------------------------------------
	private Amount<Length> _xApexHTail;
	private Amount<Length> _yApexHTail;
	private Amount<Length> _zApexHTail;

	private Amount<Area> _hTailSurface;
	private Double _hTailAspectRatio;
	private Amount<Length> _hTailSpan;  //not from input
	private Amount<Length> _hTailSemiSpan;  // not from input
	private int _hTailNumberOfPointSemiSpanWise;
	private Double _hTailadimentionalKinkStation;
	private int _hTailnumberOfGivenSections;

	private AirfoilFamilyEnum _hTailMeanAirfoilFamily;
	private Double _hTailMaxThicknessMeanAirfoil;
	private Double _hTailVortexSemiSpanToSemiSpanRatio;

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
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailNOANGLE; // this is the distance between the ac of wing and h tail mesured along
	// the BRF 
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	String databaseFolderPath;
	String aerodynamicDatabaseFileName;
	String highLiftDatabaseFileName;
	AerodynamicDatabaseReader aeroDatabaseReader;
	HighLiftDatabaseReader highLiftDatabaseReader;

	double [] alphaZeroLiftRad;
	double [] twistDistributionRad ;

	//-------------------------------------------------------------------------------------------------------------------------
	//----------------------------------
	// VARIABLE DECLARATION			   ||
	// output    					   ||
	//----------------------------------

	//AnglesArray-------------------------------------
	//----------------------------------------------------------------
	private List<Amount<Angle>> _alphasWing;
	private List<Amount<Angle>> _alphasTail;
	private List<Double> _downwashGradientConstant;
	private List<Amount<Angle>> _downwashAngleConstant;
	private List<Double> _downwashGradientVariable;
	private List<Amount<Angle>> _downwashAngleVariable;
	private List<Amount<Length>> _horizontalDistance;
	private List<Amount<Length>> _verticalDistance;
	private List<Amount<Length>> _horizontalDistanceConstant = new ArrayList<>();
	private List<Amount<Length>> _verticalDistanceConstant = new ArrayList<>();


	//Lift -------------------------------------------
	//----------------------------------------------------------------

	// taken from LSAerodynamicsCalculator
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
	private Double[] _wingliftCoefficient3DCurveHighLift;
	private double [] _wingliftCoefficientDistributionatCLMax;
	private Double [] _wingclAlphaArray;


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


	//Drag -------------------------------------------
	//----------------------------------------------------------------


	//Moment -------------------------------------------
	//----------------------------------------------------------------


	//Stability -------------------------------------------
	//----------------------------------------------------------------

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
		aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

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


		this._wingVortexSemiSpanToSemiSpanRatio = 1./(2*this._wingNumberOfPointSemiSpanWise);
		this._hTailVortexSemiSpanToSemiSpanRatio = 1./(2*this._hTailNumberOfPointSemiSpanWise);

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
					this._wingClAlphaDistributionDeg.get(i)) ;
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


		alphaZeroLiftRad = new double [_wingNumberOfPointSemiSpanWise];
		twistDistributionRad = new double [_wingNumberOfPointSemiSpanWise];
		for (int i=0; i<_wingNumberOfPointSemiSpanWise; i++){
			alphaZeroLiftRad[i] = _wingAlphaZeroLiftDistribution.get(i).doubleValue(SI.RADIAN);
			twistDistributionRad[i] =  _wingTwistDistribution.get(i).doubleValue(SI.RADIAN);
		}



		//---------------
		// Other values       |
		//---------------

		this._horizontalDistanceQuarterChordWingHTail = Amount.valueOf(
				(this._xApexHTail.doubleValue(SI.METER) + this._hTailChordsBreakPoints.get(0).doubleValue(SI.METER)/4)- 
				(this._xApexWing.doubleValue(SI.METER) + this._wingChordsBreakPoints.get(0).doubleValue(SI.METER)/4),
				SI.METER
				);

		if ( this._wingAlphaZeroLift == null ){

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
		}

		if ( (this._zApexWing.doubleValue(SI.METER) > 0 && this._zApexHTail.doubleValue(SI.METER) > 0 ) ||
				(this._zApexWing.doubleValue(SI.METER) < 0 && this._zApexHTail.doubleValue(SI.METER) < 0 )){
			this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
					this._zApexHTail.doubleValue(SI.METER) - this._zApexWing.doubleValue(SI.METER), SI.METER);
		}

		if ( (this._zApexWing.doubleValue(SI.METER) > 0 && this._zApexHTail.doubleValue(SI.METER) < 0 ) ||
				(this._zApexWing.doubleValue(SI.METER) < 0 && this._zApexHTail.doubleValue(SI.METER) > 0 )){ // different sides
			if(this._zApexWing.doubleValue(SI.METER) < 0 ){
				this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
						this._zApexHTail.doubleValue(SI.METER) + Math.abs(this._zApexWing.doubleValue(SI.METER)), SI.METER);	
			}

			if(this._zApexWing.doubleValue(SI.METER) > 0 ){
				this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
						-( Math.abs(this._zApexHTail.doubleValue(SI.METER)) + this._zApexWing.doubleValue(SI.METER)), SI.METER);	
			}
		}

		// vertical and horizontal distances from AC

		this._verticalDistanceZeroLiftDirectionWingHTailNOANGLE = this._verticalDistanceZeroLiftDirectionWingHTail ;		
		this._horizontalDistanceQuarterChordWingHTailNOANGLE =  this._horizontalDistanceQuarterChordWingHTail;

		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.
		
		if (this._zApexWing.doubleValue(SI.METER) < this._zApexHTail.doubleValue(SI.METER)  ){

			this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER) + (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
							Math.tan(this._wingAngleOfIncidence.doubleValue(SI.RADIAN) -
									this._wingAlphaZeroLift.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		if (this._zApexWing.doubleValue(SI.METER) > this._zApexHTail.doubleValue(SI.METER)  ){

			this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER) - (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
							Math.tan(this._wingAngleOfIncidence.doubleValue(SI.RADIAN) -
									this._wingAlphaZeroLift.doubleValue(SI.RADIAN)))),
					SI.METER);
		}

		// distance along direction
		this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = this._verticalDistanceZeroLiftDirectionWingHTail ;		

		this._verticalDistanceZeroLiftDirectionWingHTail = Amount.valueOf(
				this._verticalDistanceZeroLiftDirectionWingHTail.doubleValue(SI.METER) * 
				Math.cos(this._wingAngleOfIncidence.doubleValue(SI.RADIAN)-
						this._wingAlphaZeroLift.doubleValue(SI.RADIAN)), SI.METER);


		System.out.println(" HORIZONTAL DISTANCE FIRST VALUE " + _horizontalDistanceQuarterChordWingHTail + "this value is the same");
		System.out.println(" VERTICAL DISTANCE FIRST VALUE " + _verticalDistanceZeroLiftDirectionWingHTail);

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

		// alpha wing array

		this._alphasWing = new ArrayList<>();
		for (int i=0; i<_numberOfAlphasBody; i++){
			this._alphasWing.add(
					Amount.valueOf((
							this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))
							, NonSI.DEGREE_ANGLE
							)
					);
		}

		// downwash calculator

	}
	public void initializeDownwashArray(){ 
		this._downwashAngleVariable = new ArrayList<>();
		this._downwashGradientVariable = new ArrayList<>();
		this._downwashAngleConstant = new ArrayList<>();
		this._downwashGradientConstant = new ArrayList<>();

		// calculate cl alpha at M=0 and M=current
		if ( this._downwashConstant == Boolean.TRUE){
			NasaBlackwell theNasaBlackwellCalculatorMachZero = new NasaBlackwell(
					this._wingSemiSpan.doubleValue(SI.METER),
					this._wingSurface.doubleValue(SI.SQUARE_METRE),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistribution),
					MyArrayUtils.convertListOfAmountTodoubleArray(this._wingDihedralDistribution),
					twistDistributionRad,
					alphaZeroLiftRad,
					_wingVortexSemiSpanToSemiSpanRatio,
					0.0,
					0.0,
					this.getAltitude().doubleValue(SI.METER)
					);


			theNasaBlackwellCalculatorMachZero.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
			double clOneMachZero = theNasaBlackwellCalculatorMachZero.getCLCurrent();
			theNasaBlackwellCalculatorMachZero.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
			double clTwoMachZero = theNasaBlackwellCalculatorMachZero.getCLCurrent();


			double cLAlphaMachZero = (clTwoMachZero-clOneMachZero)/toRadians(4);

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
				_downwashGradientConstant.add(downwashGradientConstant);}

			//fill the downwash array
			for (int i=0; i<this._numberOfAlphasBody; i++){
				this._downwashAngleConstant.add(i,
						Amount.valueOf(
								this._downwashGradientConstant.get(i)*(
										this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) - (-
												this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE) + 
												this._wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE))), NonSI.DEGREE_ANGLE));
			}
		}

		if ( this._downwashConstant == Boolean.FALSE){

			// DOWNWASH variable

			this._wingclAlphaArray = LiftCalc.calculateCLAlphaArray(_wingliftCoefficient3DCurve, _alphasWing);
			theStabilityCalculator.calculateDownwashNonLinearSlingerland(
					this._wingChordsBreakPoints.get(0), 
					this._wingSemiSpan, 
					this._wingSweepQuarterChord,
					this._wingAspectRatio, 
					this._wingAngleOfIncidence, 
					this._wingAlphaZeroLift, 
					this._horizontalDistanceQuarterChordWingHTailNOANGLE, 
					this._verticalDistanceZeroLiftDirectionWingHTailNOANGLE, 
					this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE,
					this._zApexWing, 
					this._zApexHTail, 
					MyArrayUtils.convertToDoublePrimitive(this._wingclAlphaArray),
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)),
					MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasBody)));


			_downwashGradientVariable = theStabilityCalculator.getDownwashGradient();
			_downwashAngleVariable= theStabilityCalculator.getDownwashAngle();
			_horizontalDistance = theStabilityCalculator.getHorizontalDistance();
			_verticalDistance = theStabilityCalculator.getVerticalDistance();

			for (int i=0; i<_downwashAngleVariable.size(); i++){
				_horizontalDistanceConstant.add(i,_horizontalDistance.get(0));
				_verticalDistanceConstant.add(i,_verticalDistance.get(0));
			}

			double downwashGradientConstant  =  _downwashGradientVariable.get(0);



			for (int i=0; i<this._numberOfAlphasBody; i++){
				_downwashGradientConstant.add(i,downwashGradientConstant);}

			//fill the downwash array
			for (int i=0; i<this._numberOfAlphasBody; i++){
				this._downwashAngleConstant.add(i,
						Amount.valueOf(
								this._downwashGradientConstant.get(i)*(
										this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) - (-
												this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE) + 
												this._wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE))), NonSI.DEGREE_ANGLE));}

		}


		// alpha tail array
		//		this._alphasTail = new ArrayList<>();
		//		for (int i=0; i<_numberOfAlphasBody; i++){
		//			this._alphasTail.add(
		//					Amount.valueOf((
		//							this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE)-
		//							this._downwashAngle.get(i).doubleValue(NonSI.DEGREE_ANGLE) - this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))
		//							, NonSI.DEGREE_ANGLE
		//							)
		//					);
		//		}
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
		System.out.println("Distributions: " );
		System.out.println("Chord Break Points --> " + this._wingChordsBreakPoints);
		System.out.println("XLE Break Points --> " + this._wingXleBreakPoints);
		System.out.println("Twist Break Points --> " + this._wingTwistBreakPoints);
		System.out.println("Max thickness Break Points--> " + this._wingMaxThicknessBreakPoints);
		System.out.println("Dihedral Break Points --> " + this._wingDihedralBreakPoints);
		System.out.println("Alpha zero lift Break Points --> " + this._wingAlphaZeroLiftBreakPoints);
		System.out.println("Cl zero Break Points --> " + this._wingCl0BreakPoints);
		System.out.println("Alpha star Break Points --> " + this._wingAlphaStarBreakPoints);
		System.out.println("Cl max Break Points --> " + this._wingClMaxBreakPoints);

		//distribution		
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingChordsDistribution, "Chord Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingXleDistribution, "XLE Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingTwistDistribution, "Twist Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingDihedralDistribution, "Dihedral Distribution", ",");
		MyArrayUtils.printListOfAmountWithUnitsInEvidence(this._wingAlphaStarDistribution, "Alpha star Distribution", ",");
		System.out.println("\nCl max Distribution --> " + this._wingClMaxDistribution);

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
		System.out.println("Distributions: " );
		System.out.println("Chord Break Points --> " + this._hTailChordsBreakPoints);
		System.out.println("XLE Break Points --> " + this._hTailXleBreakPoints);
		System.out.println("Twist Break Points --> " + this._hTailTwistBreakPoints);
		System.out.println("Dihedral Break Points --> " + this._hTailDihedralBreakPoints);
		System.out.println("Alpha zero lift Break Points --> " + this._hTailAlphaZeroLiftBreakPoints);
		System.out.println("Alpha star Break Points --> " + this._hTailAlphaStarBreakPoints);
		System.out.println("Cl max Break Points --> " + this._hTailClMaxBreakPoints);

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
		.append("\t\tCL star = " + _wingcLStar+ "\n")
		.append("\t\tAlpha star = " + _wingalphaStar+ "\n")
		.append("\t\tCL max = " + _wingcLMax+ "\n")
		.append("\t\tAlpha stall = " + _wingalphaStall+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this. _alphasWing, "\t\tAlpha Wing", ","))
		.append("\t\tCL 3D Curve = " + Arrays.toString(_wingliftCoefficient3DCurve)+ "\n")
		.append("\t\tEta stations = " + _wingYAdimensionalDistribution+ "\n")
		.append("\t\tCl distribution at CL max = " + Arrays.toString(_wingliftCoefficientDistributionatCLMax) + "\n")
		.append("\t\tDownwash Gradient Constant = " + _downwashGradientConstant.get(0)+ "\n")
		.append(MyArrayUtils.ListOfAmountWithUnitsInEvidenceString(this._downwashAngleConstant, "\t\tDownwash angle with Constant Gradient", ","))
		.append("\t-------------------------------------\n")
		;
		
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

		sb.append("\tFuselage\n")
		.append("\t-------------------------------------\n")
		.append("\t\tGround roll distance = " + _downwashGradientConstant + "\n")
		.append("\t-------------------------------------\n")
		;

		sb.append("\tHorizontal Tail\n")
		.append("\t-------------------------------------\n")
		.append("\t\tGround roll distance = " + _downwashGradientConstant + "\n")
		.append("\t-------------------------------------\n")
		;

		sb.append("DRAG\n")
		.append("-------------------------------------\n")
		.append("\tWing\n")
		.append("\t-------------------------------------\n")
		.append("\t\tDownwash Gradient Constant = " + _downwashGradientConstant+ "\n")
		.append("\t-------------------------------------\n")
		;

		return sb.toString();

	}

	public void plot( String folderPathName) throws InstantiationException, IllegalAccessException{

		// DOWNWASH e DOWNWASH GRADIENT	
		if ( this._downwashConstant == Boolean.TRUE){
			if(_plotList.contains(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE)) {

				List<Double[]> xList = new ArrayList<>();
				List<Double[]> yList = new ArrayList<>();
				List<String> legend = new ArrayList<>();

				xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasBody));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleConstant));
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
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientConstant));
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
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleConstant));
				yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_downwashAngleVariable));
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
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientConstant));
				yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_downwashGradientVariable));
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

		// CL ALPHA
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
					"alpha_b", "epsilon", 
					null, null,
					null, null,
					"deg", "deg",
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
					"Cl distribution at CL max Clean", 
					"eta", "Cl", 
					null, null,
					null, null,
					"", "",
					true,
					legend,
					folderPathName,
					"Cl distribution at CL max Clean");

			System.out.println("Plot Cl distribution at CL max ---> DONE \n");
			
			// HIGH LIFT
			if (_theCondition == ConditionEnum.TAKE_OFF || _theCondition == ConditionEnum.LANDING){
			xList = new ArrayList<>();
			yList = new ArrayList<>();
			legend = new ArrayList<>();

			xList.add(MyArrayUtils.convertListOfAmountToDoubleArray(_alphasWing));
			xList.add(_alphaArrayPlotHighLift);
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

			System.out.println("Plot CL high lift Chart ---> DONE \n");
			}
		}

	}

	/******************************************************************************************************************************************
	 * Following there are the calculators                                                                                                    *
	 * 												*																						  *
	 *****************************************************************************************************************************************/

	//--------------------------------------------------------------------------------------------------------
	//CALCULATORS---------------------------------------													 |
	//--------------------------------------------------------------------------------------------------------

	public void calculateWingLiftCharacteristics(){
		//------------WING VALUES

		NasaBlackwell theNasaBlackwellCalculatorMachActual = new NasaBlackwell(
				this._wingSemiSpan.doubleValue(SI.METER),
				this._wingSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingDihedralDistribution),
				twistDistributionRad,
				alphaZeroLiftRad,
				_wingVortexSemiSpanToSemiSpanRatio,
				0.0,
				this._machCurrent,
				this.getAltitude().doubleValue(SI.METER)
				);

		// cl alpha 
		theNasaBlackwellCalculatorMachActual.calculate(Amount.valueOf(toRadians(0.), SI.RADIAN));
		double clOneMachActual = theNasaBlackwellCalculatorMachActual.getCLCurrent();
		theNasaBlackwellCalculatorMachActual.calculate(Amount.valueOf(toRadians(4.), SI.RADIAN));
		double clTwoMachActual = theNasaBlackwellCalculatorMachActual.getCLCurrent();

		this._wingcLAlphaRad = (clTwoMachActual-clOneMachActual)/toRadians(4);
		this._wingcLAlphaDeg = (clTwoMachActual-clOneMachActual)/(4);

		this._wingclAlpha = Amount.valueOf( this._wingcLAlphaRad , SI.RADIAN.inverse());


		// alpha zero lift is calculated before

		//cl zero

		this._wingcLZero = LiftCalc.calculateLiftCoefficientAtAlpha0(
				_wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE),
				this._wingcLAlphaDeg
				);

		// alphaStar e cl star

		double dimensionalOverKink = _wingSemiSpan.doubleValue(SI.METER) - _wingYBreakPoints.get(1).doubleValue(SI.METER);

		double influenceAreaRoot = _wingChordsBreakPoints.get(0).doubleValue(SI.METER) * _wingYBreakPoints.get(1).doubleValue(SI.METER)/2;
		double influenceAreaKink = (_wingChordsBreakPoints.get(1).doubleValue(SI.METER) * _wingYBreakPoints.get(1).doubleValue(SI.METER)/2) +
				(_wingChordsBreakPoints.get(1).doubleValue(SI.METER) * dimensionalOverKink/2);
		double influenceAreaTip = _wingChordsBreakPoints.get(2).doubleValue(SI.METER) * dimensionalOverKink/2;

		double kRoot = 2*influenceAreaRoot/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		double kKink = 2*influenceAreaKink/this._wingSurface.doubleValue(SI.SQUARE_METRE);
		double kTip = 2*influenceAreaTip/this._wingSurface.doubleValue(SI.SQUARE_METRE);

		double alphaStar =  _wingAlphaStarBreakPoints.get(0).doubleValue(NonSI.DEGREE_ANGLE) * kRoot +
				_wingAlphaStarBreakPoints.get(1).doubleValue(NonSI.DEGREE_ANGLE) * kKink + 
				_wingAlphaStarBreakPoints.get(2).doubleValue(NonSI.DEGREE_ANGLE) * kTip;

		this._wingalphaStar= (Amount.valueOf(alphaStar, NonSI.DEGREE_ANGLE));

		theNasaBlackwellCalculatorMachActual.calculate(this._wingalphaStar);
		double cLStar = theNasaBlackwellCalculatorMachActual.get_cLEvaluated();


		this._wingcLStar = cLStar;


		// CLMAX 

		theStabilityCalculator.nasaBlackwellCLMax(
				_wingNumberOfPointSemiSpanWise,
				theNasaBlackwellCalculatorMachActual,
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


		this._wingliftCoefficient3DCurve = LiftCalc.calculateCLvsAlphaArray(
				this._wingcLZero,
				this._wingcLMax,
				this._wingalphaStar,
				this._wingalphaStall,
				this._wingclAlpha,
				MyArrayUtils.convertListOfAmountToDoubleArray(this._alphasWing)
				);
	}

	public void calculateWingHighLiftCharacteristics(){

		NasaBlackwell theNasaBlackwellCalculatorMachActual = new NasaBlackwell(
				this._wingSemiSpan.doubleValue(SI.METER),
				this._wingSurface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingYDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingChordsDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingXleDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(this._wingDihedralDistribution),
				twistDistributionRad,
				alphaZeroLiftRad,
				_wingVortexSemiSpanToSemiSpanRatio,
				0.0,
				this._machCurrent,
				this.getAltitude().doubleValue(SI.METER)
				);
		
		double cLCurrent = theNasaBlackwellCalculatorMachActual.getCLCurrent();
		
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
		// ALPHA STAR HIGH LIFT
		_cLStarHighLift= 
				(_cLAlphaHighLiftDEG
				* _alphaStarHighLift
					.doubleValue(NonSI.DEGREE_ANGLE)
				+ _cLZeroHighLift);
		
		//------------------------------------------------------
		
		
//		// CD0 HIGH LIFT
//		_cD0HighLift= 
//				_cD0.get(MethodEnum.CLASSIC)
//				+ _deltaCD0.get(MethodEnum.EMPIRICAL)
//				);

		_alphaArrayPlotHighLift = MyArrayUtils.linspaceDouble(
				_alphaZeroLiftHighLift.doubleValue(NonSI.DEGREE_ANGLE)-2,
				_alphaStallHighLift.doubleValue(NonSI.DEGREE_ANGLE) + 3,
				_numberOfAlphasBody
				);
		
		_wingLiftCoefficient3DCurveHighLift = 
				LiftCalc.calculateCLvsAlphaArray(
						_cLZeroHighLift,
						_cLMaxHighLift,
						_alphaStarHighLift,
						_alphaStallHighLift,
						Amount.valueOf(_cLAlphaHighLiftDEG, NonSI.DEGREE_ANGLE.inverse()),
						_alphaArrayPlotHighLift
						);
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
		return _downwashAngleVariable;
	}

	public List<Double> get_downwashGradient() {
		return _downwashGradientVariable;
	}

	public void set_alphasWing(List<Amount<Angle>> _alphasWing) {
		this._alphasWing = _alphasWing;
	}

	public void set_alphasTail(List<Amount<Angle>> _alphasTail) {
		this._alphasTail = _alphasTail;
	}

	public void set_downwashAngle(List<Amount<Angle>> _downwashAngle) {
		this._downwashAngleVariable = _downwashAngle;
	}

	public void set_downwashGradient(List<Double> _downwashGradient) {
		this._downwashGradientVariable = _downwashGradient;
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



}
