package sandbox2.mr;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.ACPerformanceCalculator.ACPerformanceCalculatorBuilder;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyVariableToWrite;

/************************************************************************************************************************
 * THIS CLASS IS A PROTOTYPE OF THE NEW ACStabilityManager																*
 * After it will be necessary to change the name of the class and to read the data from other analysis, not from file.  *
 * Moreover the Test_Stability class will be the new test class of the executable										*
 * 																														*
 * @author Manuela Ruocco																								*
 ***********************************************************************************************************************/

public class StabilityCalculator {

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
	
	
	//-------------------------------------------------------------------------------------------------------------------------
	//----------------------------------
	// VARIABLE DECLARATION			   ||
	// output    					   ||
	//----------------------------------

	//AnglesArray-------------------------------------
	//----------------------------------------------------------------
	private List<Amount<Angle>> _alphasWing;
	private List<Amount<Angle>> _alphasTail;
	private List<Amount<Angle>> _downwashAngle;
	private List<Amount<Angle>> _downwashGradient;
	
	//Lift -------------------------------------------
	//----------------------------------------------------------------


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
		this._downwashAngle = new ArrayList<>();
		this._downwashGradient = new ArrayList<>();
		
		if ( this._downwashConstant == Boolean.TRUE){
			// DOWNWASH CONSTANT
		}
		
		if ( this._downwashConstant == Boolean.FALSE){
			// DOWNWASH variable
		}
		

		// alpha tail array
		this._alphasTail = new ArrayList<>();
		for (int i=0; i<_numberOfAlphasBody; i++){
			this._alphasTail.add(
					Amount.valueOf((
							this._alphasBody.get(i).doubleValue(NonSI.DEGREE_ANGLE) + this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE)-
							this._downwashAngle.get(i).doubleValue(NonSI.DEGREE_ANGLE) - this._wingAngleOfIncidence.doubleValue(NonSI.DEGREE_ANGLE))
							, NonSI.DEGREE_ANGLE
							)
					);
		}
	}

	/*****************************************************************************************************************************************
	 * When this class will begin the ACStabilityManager, this method will be simply eliminated            									 *
	 * 												*																						 *
	 *****************************************************************************************************************************************/
	
	
	public void printAllData(){
		MyConfiguration.customizeAmountOutput();
		
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
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._alphasBody, "Alphas Body array", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._alphasWing, "Alphas Wing array", ",");

		System.out.println("\nWING ----------\n-------------------");
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
		System.out.println("Dihedral Break Points --> " + this._wingDihedralBreakPoints);
		System.out.println("Alpha zero lift Break Points --> " + this._wingAlphaZeroLiftBreakPoints);
		System.out.println("Alpha star Break Points --> " + this._wingAlphaStarBreakPoints);
		System.out.println("Cl max Break Points --> " + this._wingClMaxBreakPoints);
		
		//distribution		
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._wingChordsDistribution, "Chord Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._wingXleDistribution, "XLE Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._wingTwistDistribution, "Twist Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._wingDihedralDistribution, "Dihedral Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._wingAlphaStarDistribution, "Alpha star Distribution", ",");
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
		
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._hTailChordsDistribution, "Chord Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._hTailXleDistribution, "XLE Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._hTailTwistDistribution, "Twist Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._hTailDihedralDistribution, "Dihedral Distribution", ",");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._hTailAlphaStarDistribution, "Alpha star Distribution", ",");
		System.out.println("\nCl max Distribution --> " + this._hTailClMaxDistribution);

		System.out.println("\nELEVATOR ----------\n-------------------");
		MyArrayUtils.writeListOfAmountAsExcelFormat(this._anglesOfElevatorDeflection,"Angles of Elevator Deflection ", ",");
		System.out.println("\nFlaps type --> " + this._elevatorType);
		System.out.println("Eta in Elevator = " + this._elevatorEtaIn);
		System.out.println("Eta out Elevator = " + this._elevatorEtaOut);
		System.out.println("Elevator chord ratio = " + this._elevatorCfC);

		System.out.println("\nEngines ----------\n-------------------");
		
		System.out.println("PLOT LIST");
		for (int i=0; i<this._plotList.size(); i++)
		System.out.println( this._plotList.get(i));
	}

	
	/******************************************************************************************************************************************
	 * Following there are the calculators                                                                                                    *
	 * 												*																						  *
	 *****************************************************************************************************************************************/
	
	//--------------------------------------------------------------------------------------------------------
	//CALCULATORS---------------------------------------													 |
	//--------------------------------------------------------------------------------------------------------
	
	
	public void downwashCalculator(){}
	
	
	
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
		return _downwashAngle;
	}

	public List<Amount<Angle>> get_downwashGradient() {
		return _downwashGradient;
	}

	public void set_alphasWing(List<Amount<Angle>> _alphasWing) {
		this._alphasWing = _alphasWing;
	}

	public void set_alphasTail(List<Amount<Angle>> _alphasTail) {
		this._alphasTail = _alphasTail;
	}

	public void set_downwashAngle(List<Amount<Angle>> _downwashAngle) {
		this._downwashAngle = _downwashAngle;
	}

	public void set_downwashGradient(List<Amount<Angle>> _downwashGradient) {
		this._downwashGradient = _downwashGradient;
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

}
