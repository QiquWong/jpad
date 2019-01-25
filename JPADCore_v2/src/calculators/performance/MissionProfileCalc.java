package calculators.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import analyses.OperatingConditions;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class MissionProfileCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Amount<Length> _takeOffMissionAltitude;
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _singlePassengerMass;
	private int _passengersNumber;
	private Amount<Mass> _firstGuessInitialFuelMass;
	private Amount<Length> _missionRange;
	private MyInterpolatingFunction _sfcFunctionCruise;
	private MyInterpolatingFunction _sfcFunctionAlternateCruise;
	private MyInterpolatingFunction _sfcFunctionHolding;
	private Amount<Length> _alternateCruiseLength;
	private Amount<Length> _alternateCruiseAltitude;
	private Amount<Duration> _holdingDuration;
	private Amount<Length> _holdingAltitude;
	private double _holdingMachNumber;
	private double _landingFuelFlow;
	private double _fuelReserve;
	private double _cLmaxClean;
	private double _cLmaxTakeOff;
	private Amount<?> _cLAlphaTakeOff;
	private double _cLZeroTakeOff;
	private double _cLmaxLanding;
	private double _cLZeroLanding;
	private double[] _polarCLTakeOff;
	private double[] _polarCDTakeOff;
	private double[] _polarCLClimb;
	private double[] _polarCDClimb;
	private double[] _polarCLCruise;
	private double[] _polarCDCruise;
	private double[] _polarCLLanding;
	private double[] _polarCDLanding;
	private Amount<Velocity> _windSpeed;
	private MyInterpolatingFunction _mu;
	private MyInterpolatingFunction _muBrake;
	private Amount<Duration> _dtHold;
	private Amount<Angle> _alphaGround;
	private Amount<Length> _obstacleTakeOff;
	private double _kRotation;
	private double _kCLmaxTakeOff;
	private double _dragDueToEnigneFailure;
	private double _kAlphaDot;
	private Amount<Length> _obstacleLanding;
	private Amount<Angle> _approachAngle;
	private double _kCLmaxLanding;
	private double _kApproach;
	private double _kFlare;
	private double _kTouchDown;
	private Amount<Duration> _freeRollDuration;
	private Amount<Velocity> _climbSpeed;
	private Amount<Velocity> _speedDescentCAS;
	private Amount<Velocity> _rateOfDescent;
	private boolean _calculateSFCCruise;
	private boolean _calculateSFCAlternateCruise;
	private boolean _calculateSFCHolding;
	
	//............................................................................................
	// Output:
	private Boolean _missionProfileStopped = Boolean.FALSE;
	
	private List<Amount<Length>> _altitudeList;
	private List<Amount<Length>> _rangeList;
	private List<Amount<Duration>> _timeList;
	private List<Amount<Mass>> _fuelUsedList;
	private List<Amount<Mass>> _massList;
	private List<Amount<Velocity>> _speedCASMissionList;
	private List<Amount<Velocity>> _speedTASMissionList;
	private List<Double> _machMissionList;
	private List<Double> _liftingCoefficientMissionList;
	private List<Double> _dragCoefficientMissionList;
	private List<Double> _efficiencyMissionList;
	private List<Amount<Force>> _thrustMissionList;
	private List<Amount<Force>> _dragMissionList;
	private List<Amount<Velocity>> _rateOfClimbMissionList;
	private List<Amount<Angle>> _climbAngleMissionList;
	private List<Double> _fuelFlowMissionList;
	private List<Double> _sfcMissionList;
	private List<Double> _throttleMissionList;
	
	private Amount<Mass> _initialFuelMass;
	private Amount<Mass> _totalFuel;
	private Amount<Mass> _blockFuel;
	private Amount<Duration> _totalTime;
	private Amount<Duration> _blockTime;
	private Amount<Length> _totalRange;
	private Amount<Mass> _initialMissionMass;
	private Amount<Mass> _endMissionMass;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public MissionProfileCalc(
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Mass> maximumTakeOffMass,
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> singlePassengerMass,
			Integer passengersNumber,
			Amount<Mass> firstGuessInitialFuelMass,
			Amount<Length> missionRange,
			Amount<Length> takeOffMissionAltitude,
			Amount<Length> firstGuessCruiseLength,
			boolean calculateSFCCruise,
			boolean calculateSFCAlternateCruise,
			boolean calculateSFCHolding,
			MyInterpolatingFunction sfcFunctionCruise,
			MyInterpolatingFunction sfcFunctionAlternateCruise,
			MyInterpolatingFunction sfcFunctionHolding,
			Amount<Length> alternateCruiseLength,
			Amount<Length> alternateCruiseAltitude,
			Amount<Duration> holdingDuration,
			Amount<Length> holdingAltitude,
			double holdingMachNumber,
			double landingFuelFlow,
			double fuelReserve,
			double cLmaxClean,
			double cLmaxTakeOff,
			Amount<?> cLAlphaTakeOff,
			double cLZeroTakeOff,
			double cLmaxLanding,
			double cLZeroLanding,
			double[] polarCLTakeOff,
			double[] polarCDTakeOff,
			double[] polarCLClimb,
			double[] polarCDClimb,
			double[] polarCLCruise,
			double[] polarCDCruise,
			double[] polarCLLanding,
			double[] polarCDLanding,
			Amount<Velocity> windSpeed,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Duration> dtHold,
			Amount<Angle> alphaGround,
			Amount<Length> obstacleTakeOff,
			double kRotation,
			double kLiftOff,
			double kCLmaxTakeOff,
			double dragDueToEnigneFailure,
			double kAlphaDot,
			Amount<Length> obstacleLanding,
			Amount<Angle> approachAngle,
			double kCLmaxLanding,
			double kApproach,
			double kFlare,
			double kTouchDown,
			Amount<Duration> freeRollDuration,
			Amount<Velocity> climbSpeed,
			Amount<Velocity> speedDescentCAS,
			Amount<Velocity> rateOfDescent
			) {
		
		this._theAircraft = theAircraft; 
		this._theOperatingConditions = theOperatingConditions;
		this._maximumTakeOffMass = maximumTakeOffMass;
		this._operatingEmptyMass = operatingEmptyMass;
		this._singlePassengerMass = singlePassengerMass;
		this._passengersNumber = passengersNumber;
		this._firstGuessInitialFuelMass = firstGuessInitialFuelMass;
		this._missionRange = missionRange;
		this._takeOffMissionAltitude = takeOffMissionAltitude;
		this._calculateSFCCruise = calculateSFCCruise;
		this._calculateSFCAlternateCruise = calculateSFCAlternateCruise;
		this._calculateSFCHolding = calculateSFCHolding;
		this._sfcFunctionCruise = sfcFunctionCruise;
		this._sfcFunctionAlternateCruise = sfcFunctionAlternateCruise;
		this._sfcFunctionHolding = sfcFunctionHolding;
		this._alternateCruiseLength = alternateCruiseLength;
		this._alternateCruiseAltitude = alternateCruiseAltitude;
		this._holdingDuration = holdingDuration;
		this._holdingAltitude = holdingAltitude;
		this._holdingMachNumber = holdingMachNumber;
		this._landingFuelFlow = landingFuelFlow;
		this._fuelReserve = fuelReserve;
		this._cLmaxClean = cLmaxClean;
		this._cLmaxTakeOff = cLmaxTakeOff;
		this._cLAlphaTakeOff = cLAlphaTakeOff;
		this._cLZeroTakeOff = cLZeroTakeOff;
		this._cLmaxLanding = cLmaxLanding;
		this._cLZeroLanding = cLZeroLanding;
		this._polarCLTakeOff = polarCLTakeOff;
		this._polarCDTakeOff = polarCDTakeOff;
		this._polarCLClimb = polarCLClimb;
		this._polarCDClimb = polarCDClimb;
		this._polarCLCruise = polarCLCruise;
		this._polarCDCruise = polarCDCruise;
		this._polarCLLanding = polarCLLanding;
		this._polarCDLanding = polarCDLanding;
		this._windSpeed = windSpeed;
		this._mu = mu;
		this._muBrake = muBrake;
		this._dtHold = dtHold;
		this._alphaGround = alphaGround;
		this._obstacleTakeOff = obstacleTakeOff;
		this._kRotation = kRotation;
		this._kCLmaxTakeOff = kCLmaxTakeOff;
		this._dragDueToEnigneFailure = dragDueToEnigneFailure;
		this._kAlphaDot = kAlphaDot;
		this._obstacleLanding = obstacleLanding;
		this._approachAngle = approachAngle;
		this._kCLmaxLanding = kCLmaxLanding;
		this._kApproach = kApproach;
		this._kFlare = kFlare;
		this._kTouchDown = kTouchDown;
		this._freeRollDuration = freeRollDuration;
		this._climbSpeed = climbSpeed;
		this._speedDescentCAS = speedDescentCAS;
		this._rateOfDescent = rateOfDescent;

		this._altitudeList = new ArrayList<>();
		this._rangeList = new ArrayList<>();
		this._timeList = new ArrayList<>();
		this._fuelUsedList = new ArrayList<>();
		this._massList = new ArrayList<>();
		this._speedCASMissionList = new ArrayList<>();
		this._speedTASMissionList = new ArrayList<>();
		this._machMissionList = new ArrayList<>();
		this._liftingCoefficientMissionList = new ArrayList<>();
		this._dragCoefficientMissionList = new ArrayList<>();
		this._efficiencyMissionList = new ArrayList<>();
		this._thrustMissionList = new ArrayList<>();
		this._dragMissionList = new ArrayList<>();
		this._rateOfClimbMissionList = new ArrayList<>();
		this._climbAngleMissionList = new ArrayList<>();
		this._fuelFlowMissionList = new ArrayList<>();
		this._sfcMissionList = new ArrayList<>();
		this._throttleMissionList = new ArrayList<>();

	}

	//--------------------------------------------------------------------------------------------
	// METHODS:

	public void calculateProfiles(Amount<Velocity> vMC) {

		_initialMissionMass = _operatingEmptyMass
				.plus(_singlePassengerMass.times(_passengersNumber))
				.plus(_firstGuessInitialFuelMass); 

		_initialFuelMass = _firstGuessInitialFuelMass;

		//----------------------------------------------------------------------
		// ERROR FLAGS
		boolean cruiseMaxMachNumberErrorFlag = false;
		boolean alternateCruiseBestMachNumberErrorFlag = false;
		
		//----------------------------------------------------------------------
		// PHASE CALCULATORS
		TakeOffCalc theTakeOffCalculator = null;
		ClimbCalc theClimbCalculator = null;
		ClimbCalc theSecondClimbCalculator = null;
		DescentCalc theFirstDescentCalculator = null;
		DescentCalc theSecondDescentCalculator = null;
		LandingCalc theLandingCalculator = null;
		
		//----------------------------------------------------------------------
		// QUANTITES TO BE ADDED IN LISTS AT THE END OF THE ITERATION
		//----------------------------------------------------------------------
		// TAKE-OFF
		Amount<Length> rangeTakeOff = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeTakeOff = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelTakeOff = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtTakeOffStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtTakeOffEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtTakeOffStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtTakeOffEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtTakeOffStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtTakeOffEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtTakeOffStart = 0.0;
		double cLAtTakeOffEnding = 0.0;
		double cDAtTakeOffStart = 0.0;
		double cDAtTakeOffEnding = 0.0;
		Amount<Force> thrustAtTakeOffStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtTakeOffEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtTakeOffStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtTakeOffEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtTakeOffStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtTakeOffEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtTakeOffStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtTakeOffEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtTakeOffStart = 0.0;
		double fuelFlowAtTakeOffEnding = 0.0;
		double sfcAtTakeOffStart = 0.0;
		double sfcAtTakeOffEnding = 0.0;
		//......................................................................
		// CLIMB
		Amount<Length> rangeClimb = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeClimb = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelClimb = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtClimbStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtClimbEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtClimbStart = 0.0;
		double cLAtClimbEnding = 0.0;
		double cDAtClimbStart = 0.0;
		double cDAtClimbEnding = 0.0;
		Amount<Force> thrustAtClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtClimbStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtClimbEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtClimbStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtClimbEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtClimbStart = 0.0;
		double fuelFlowAtClimbEnding = 0.0;
		double sfcAtClimbStart = 0.0;
		double sfcAtClimbEnding = 0.0;
		//......................................................................
		// CRUISE
		Amount<Length> rangeCruise = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeCruise = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelCruise = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtCruiseStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtCruiseEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtCruiseStart = 0.0;
		double cLAtCruiseEnding = 0.0;
		double cDAtCruiseStart = 0.0;
		double cDAtCruiseEnding = 0.0;
		double throttleCruiseStart = 0.0;
		double throttleCruiseEnding = 0.0;
		Amount<Force> thrustAtCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtCruiseStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtCruiseEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtCruiseStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtCruiseEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtCruiseStart = 0.0;
		double fuelFlowAtCruiseEnding = 0.0;
		double sfcAtCruiseStart = 0.0;
		double sfcAtCruiseEnding = 0.0;
		//......................................................................
		// FIRST DESCENT
		Amount<Length> rangeFirstDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeFirstDescent = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelFirstDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtFirstDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtFirstDescentEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtFirstDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtFirstDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtFirstDescentStart = 0.0;
		double cLAtFirstDescentEnding = 0.0;
		double cDAtFirstDescentStart = 0.0;
		double cDAtFirstDescentEnding = 0.0;
		Amount<Force> thrustAtFirstDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtFirstDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtFirstDescentStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtFirstDescentEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtFirstDescentStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtFirstDescentStart = 0.0;
		double fuelFlowAtFirstDescentEnding = 0.0;
		double sfcAtFirstDescentStart = 0.0;
		double sfcAtFirstDescentEnding = 0.0;
		//......................................................................
		// SECOND CLIMB
		Amount<Length> rangeSecondClimb = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeSecondClimb = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelSecondClimb = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondClimbStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondClimbEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtSecondClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtSecondClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtSecondClimbStart = 0.0;
		double cLAtSecondClimbEnding = 0.0;
		double cDAtSecondClimbStart = 0.0;
		double cDAtSecondClimbEnding = 0.0;
		Amount<Force> thrustAtSecondClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtSecondClimbStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtSecondClimbEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtSecondClimbStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtSecondClimbStart = 0.0;
		double fuelFlowAtSecondClimbEnding = 0.0;
		double sfcAtSecondClimbStart = 0.0;
		double sfcAtSecondClimbEnding = 0.0;
		//......................................................................
		// ALTERNATE CRUISE
		Amount<Length> rangeAlternateCruise = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeAlternateCruise = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelAlternateCruise = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtAlternateCruiseStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtAlternateCruiseEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtAlternateCruiseStart = 0.0;
		double cLAtAlternateCruiseEnding = 0.0;
		double cDAtAlternateCruiseStart = 0.0;
		double cDAtAlternateCruiseEnding = 0.0;
		double throttleAlternateCruiseStart = 0.0;
		double throttleAlternateCruiseEnding = 0.0;
		Amount<Force> thrustAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtAlternateCruiseStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtAlternateCruiseEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtAlternateCruiseStart = 0.0;
		double fuelFlowAtAlternateCruiseEnding = 0.0;
		double sfcAtAlternateCruiseStart = 0.0;
		double sfcAtAlternateCruiseEnding = 0.0;
		//......................................................................
		// SECOND DESCENT
		Amount<Length> rangeSecondDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeSecondDescent = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelSecondDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondDescentEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtSecondDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtSecondDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtSecondDescentStart = 0.0;
		double cLAtSecondDescentEnding = 0.0;
		double cDAtSecondDescentStart = 0.0;
		double cDAtSecondDescentEnding = 0.0;
		Amount<Force> thrustAtSecondDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtSecondDescentStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtSecondDescentEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtSecondDescentStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtSecondDescentStart = 0.0;
		double fuelFlowAtSecondDescentEnding = 0.0;
		double sfcAtSecondDescentStart = 0.0;
		double sfcAtSecondDescentEnding = 0.0;
		//......................................................................
		// HOLDING
		Amount<Length> rangeHolding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeHolding = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelHolding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtHoldingStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtHoldingEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtHoldingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtHoldingEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtHoldingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtHoldingEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtHoldingStart = 0.0;
		double cLAtHoldingEnding = 0.0;
		double cDAtHoldingStart = 0.0;
		double cDAtHoldingEnding = 0.0;
		double throttleHoldingStart = 0.0;
		double throttleHoldingEnding = 0.0;
		Amount<Force> thrustAtHoldingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtHoldingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtHoldingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtHoldingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtHoldingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtHoldingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtHoldingStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtHoldingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtHoldingStart = 0.0;
		double fuelFlowAtHoldingEnding = 0.0;
		double sfcAtHoldingStart = 0.0;
		double sfcAtHoldingEnding = 0.0;
		//......................................................................
		// THIRD DESCENT
		Amount<Length> rangeThirdDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeThirdDescent = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelThirdDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtThirdDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtThirdDescentEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtThirdDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtThirdDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtThirdDescentStart = 0.0;
		double cLAtThirdDescentEnding = 0.0;
		double cDAtThirdDescentStart = 0.0;
		double cDAtThirdDescentEnding = 0.0;
		Amount<Force> thrustAtThirdDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtThirdDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtThirdDescentStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtThirdDescentEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtThirdDescentStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtThirdDescentStart = 0.0;
		double fuelFlowAtThirdDescentEnding = 0.0;
		double sfcAtThirdDescentStart = 0.0;
		double sfcAtThirdDescentEnding = 0.0;
		//......................................................................
		// LANDING
		Amount<Length> rangeLanding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Duration> timeLanding = Amount.valueOf(0.0, NonSI.MINUTE);
		Amount<Mass> fuelLanding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtLandingStart = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtLandingEnding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Velocity> speedTASAtLandingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedTASAtLandingEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtLandingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedCASAtLandingEnding = Amount.valueOf(0.0, NonSI.KNOT);
		double cLAtLandingStart = 0.0;
		double cLAtLandingEnding = 0.0;
		double cDAtLandingStart = 0.0;
		double cDAtLandingEnding = 0.0;
		Amount<Force> thrustAtLandingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtLandingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtLandingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtLandingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Velocity> rateOfClimbAtLandingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Velocity> rateOfClimbAtLandingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
		Amount<Angle> climbAngleAtLandingStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> climbAngleAtLandingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double fuelFlowAtLandingStart = 0.0;
		double fuelFlowAtLandingEnding = 0.0;
		double sfcAtLandingStart = 0.0;
		double sfcAtLandingEnding = 0.0;

		//----------------------------------------------------------------------
		// ITERATION START ...
		//----------------------------------------------------------------------
		if(_initialMissionMass.doubleValue(SI.KILOGRAM) > _maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {
			_initialMissionMass = _maximumTakeOffMass;
			_initialFuelMass = _maximumTakeOffMass
					.minus(_operatingEmptyMass)
					.minus(_singlePassengerMass.times(_passengersNumber)); 
		}

		Amount<Mass> newInitialFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		_totalFuel = Amount.valueOf(0.0, SI.KILOGRAM);
		int i = 0;

		do {

			if(i >= 1)
				_initialFuelMass = newInitialFuelMass;

			if(i > 100) {
				System.err.println("WARNING: (MISSION PROFILE) MAXIMUM NUMBER OF ITERATION REACHED");
				break;
			}

			//--------------------------------------------------------------------
			// TAKE-OFF
			aircraftMassAtTakeOffStart = _initialMissionMass.to(SI.KILOGRAM);

			Amount<Length> wingToGroundDistance = 
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionCylinderHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(
											Math.sin(
													_theAircraft.getWing()	
													.getDihedralMean()
													.doubleValue(SI.RADIAN)
													)
											)
									)
							);

			theTakeOffCalculator = new TakeOffCalc(
					_theAircraft.getWing().getAspectRatio(),
					_theAircraft.getWing().getSurfacePlanform(),
					_theAircraft.getPowerPlant(),
					_polarCLTakeOff,
					_polarCDTakeOff,
					_takeOffMissionAltitude.to(SI.METER),
					_theOperatingConditions.getMachTakeOff(),
					_initialMissionMass,
					_dtHold,
					_kCLmax,
					_kRotation,
					_kLiftOff,
					_dragDueToEnigneFailure,
					_theOperatingConditions.getThrottleGroundIdleTakeOff(),
					_theOperatingConditions.getThrottleTakeOff(), 
					_kAlphaDot,
					_mu,
					_muBrake,
					wingToGroundDistance,
					_obstacleTakeOff.to(SI.METER),
					_windSpeed.to(SI.METERS_PER_SECOND),
					_alphaGround.to(NonSI.DEGREE_ANGLE),
					_theAircraft.getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE),
					_cLmaxTakeOff,
					_cLZeroTakeOff,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
					);

			theTakeOffCalculator.calculateTakeOffDistanceODE(null, false, false, vMC);

			Amount<Length> groundRollDistanceTakeOff = theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0);
			Amount<Length> rotationDistanceTakeOff = 
					theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1)
					.minus(groundRollDistanceTakeOff);
			Amount<Length> airborneDistanceTakeOff = 
					theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2)
					.minus(rotationDistanceTakeOff)
					.minus(groundRollDistanceTakeOff);

			rangeTakeOff = groundRollDistanceTakeOff.plus(rotationDistanceTakeOff).plus(airborneDistanceTakeOff);			
			timeTakeOff = theTakeOffCalculator.getTakeOffResults().getTime().get(2);
			fuelTakeOff = Amount.valueOf(
					MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									theTakeOffCalculator.getTime().stream()
									.map(t -> t.to(NonSI.MINUTE))
									.collect(Collectors.toList()
											)
									),
							MyArrayUtils.convertToDoublePrimitive(theTakeOffCalculator.getFuelFlow())
							),
					SI.KILOGRAM					
					);
			aircraftMassAtTakeOffEnding = aircraftMassAtTakeOffStart.to(SI.KILOGRAM).minus(fuelTakeOff.to(SI.KILOGRAM));

			speedTASAtTakeOffStart = theTakeOffCalculator.getSpeed()
					.get(0)
					.to(NonSI.KNOT);
			speedCASAtTakeOffStart = theTakeOffCalculator.getSpeed()
					.get(0)
					.to(NonSI.KNOT)
					.times(
							Math.sqrt(
									AtmosphereCalc.getDensity(_obstacleTakeOff.doubleValue(SI.METER))
									/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
									)
							);
			speedTASAtTakeOffEnding = theTakeOffCalculator.getSpeed()
					.get(theTakeOffCalculator.getSpeed().size()-1)
					.to(NonSI.KNOT);
			speedCASAtTakeOffEnding = theTakeOffCalculator.getSpeed()
					.get(theTakeOffCalculator.getSpeed().size()-1)
					.to(NonSI.KNOT)
					.times(
							Math.sqrt(
									AtmosphereCalc.getDensity(_obstacleTakeOff.doubleValue(SI.METER))
									/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
									)
							);
			cLAtTakeOffStart = theTakeOffCalculator.getcL().get(0);
			cLAtTakeOffEnding = theTakeOffCalculator.getcL().get(theTakeOffCalculator.getcL().size()-1);
			cDAtTakeOffStart = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLTakeOff),
							MyArrayUtils.convertToDoublePrimitive(_polarCDTakeOff),
							cLAtTakeOffStart
							);
			cDAtTakeOffEnding = 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLTakeOff),
							MyArrayUtils.convertToDoublePrimitive(_polarCDTakeOff),
							cLAtTakeOffEnding
							);
			thrustAtTakeOffStart = theTakeOffCalculator.getThrust().get(0).to(NonSI.POUND_FORCE);
			thrustAtTakeOffEnding = theTakeOffCalculator.getThrust()
					.get(theTakeOffCalculator.getThrust().size()-1)
					.to(NonSI.POUND_FORCE);
			dragAtTakeOffStart = theTakeOffCalculator.getDrag().get(0).to(NonSI.POUND_FORCE);
			dragAtTakeOffEnding = theTakeOffCalculator.getDrag()
					.get(theTakeOffCalculator.getDrag().size()-1)
					.to(NonSI.POUND_FORCE);
			rateOfClimbAtTakeOffStart = theTakeOffCalculator.getRateOfClimb().get(0).to(MyUnits.FOOT_PER_MINUTE);
			rateOfClimbAtTakeOffEnding = theTakeOffCalculator.getRateOfClimb()
					.get(theTakeOffCalculator.getRateOfClimb().size()-1)
					.to(MyUnits.FOOT_PER_MINUTE);
			climbAngleAtTakeOffStart = theTakeOffCalculator.getGamma().get(0).to(NonSI.DEGREE_ANGLE);
			climbAngleAtTakeOffEnding = theTakeOffCalculator.getGamma().get(theTakeOffCalculator.getGamma().size()-1).to(NonSI.DEGREE_ANGLE);
			fuelFlowAtTakeOffStart = theTakeOffCalculator.getFuelFlow().get(0)*2.20462/0.016667;
			fuelFlowAtTakeOffEnding = theTakeOffCalculator.getFuelFlow().get(theTakeOffCalculator.getFuelFlow().size()-1)*2.20462/0.016667;
			sfcAtTakeOffStart = fuelFlowAtTakeOffStart/thrustAtTakeOffStart.doubleValue(NonSI.POUND_FORCE);
			sfcAtTakeOffEnding = fuelFlowAtTakeOffEnding/thrustAtTakeOffEnding.doubleValue(NonSI.POUND_FORCE);

			//--------------------------------------------------------------------
			// CLIMB
			theClimbCalculator = new ClimbCalc(
					_theAircraft,
					_theOperatingConditions,
					_cLmaxClean, 
					_polarCLClimb,
					_polarCDClimb,
					_climbSpeed, 
					_dragDueToEnigneFailure 
					);

			aircraftMassAtClimbStart = _initialMissionMass.minus(fuelTakeOff.to(SI.KILOGRAM));

			theClimbCalculator.calculateClimbPerformance(
					aircraftMassAtClimbStart,
					aircraftMassAtClimbStart,
					_obstacleTakeOff.to(SI.METER),
					_theOperatingConditions.getAltitudeCruise().to(SI.METER),
					false,
					false
					);

			rangeClimb = theClimbCalculator.getClimbTotalRange();
			timeClimb = null;
			if(_climbSpeed != null)
				timeClimb = theClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
			else
				timeClimb = theClimbCalculator.getMinimumClimbTimeAEO();
			fuelClimb = theClimbCalculator.getClimbTotalFuelUsed();

			aircraftMassAtClimbEnding = aircraftMassAtClimbStart.to(SI.KILOGRAM).minus(fuelClimb.to(SI.KILOGRAM));

			speedTASAtClimbStart = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
					.divide(
							Math.sqrt(
									OperatingConditions.getAtmosphere(
											_obstacleTakeOff.doubleValue(SI.METER)
											)
									.getDensityRatio()
									)
							);
			speedCASAtClimbStart = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT);
			speedTASAtClimbEnding = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
					.divide(
							Math.sqrt(
									OperatingConditions.getAtmosphere(
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
											)
									.getDensityRatio()
									)
							);
			speedCASAtClimbEnding = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT);

			cLAtClimbStart = LiftCalc.calculateLiftCoeff(
					aircraftMassAtClimbStart.times(AtmosphereCalc.g0).getEstimatedValue(),
					speedTASAtClimbStart.doubleValue(SI.METERS_PER_SECOND),					
					_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
					0.0
					);
			cLAtClimbEnding = LiftCalc.calculateLiftCoeff(
					aircraftMassAtClimbEnding
					.times(AtmosphereCalc.g0)
					.getEstimatedValue(),
					speedTASAtClimbEnding.doubleValue(SI.METERS_PER_SECOND),					
					_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
					_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
					);
			cDAtClimbStart = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
					MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
					cLAtClimbStart
					);
			cDAtClimbEnding = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
					MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
					cLAtClimbEnding
					);
			thrustAtClimbStart = theClimbCalculator.getThrustAtClimbStart().to(NonSI.POUND_FORCE);
			thrustAtClimbEnding = theClimbCalculator.getThrustAtClimbEnding().to(NonSI.POUND_FORCE);
			dragAtClimbStart = theClimbCalculator.getDragAtClimbStart().to(NonSI.POUND_FORCE);
			dragAtClimbEnding = theClimbCalculator.getDragAtClimbEnding().to(NonSI.POUND_FORCE);
			rateOfClimbAtClimbStart = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							theClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
							theClimbCalculator.getRCMapAEO().get(0).getRC(),
							speedTASAtClimbStart.doubleValue(SI.METERS_PER_SECOND)
							),
					SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
			rateOfClimbAtClimbEnding = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							theClimbCalculator.getRCMapAEO().get(theClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
							theClimbCalculator.getRCMapAEO().get(theClimbCalculator.getRCMapAEO().size()-1).getRC(),
							speedTASAtClimbEnding.doubleValue(SI.METERS_PER_SECOND)
							),
					SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
			climbAngleAtClimbStart = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							theClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
							theClimbCalculator.getRCMapAEO().get(0).getClimbAngleList(),
							speedTASAtClimbStart.doubleValue(SI.METERS_PER_SECOND)
							),
					SI.RADIAN).to(NonSI.DEGREE_ANGLE);
			climbAngleAtClimbEnding = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							theClimbCalculator.getRCMapAEO().get(theClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
							theClimbCalculator.getRCMapAEO().get(theClimbCalculator.getRCMapAEO().size()-1).getClimbAngleList(),
							speedTASAtClimbEnding.doubleValue(SI.METERS_PER_SECOND)
							),
					SI.RADIAN).to(NonSI.DEGREE_ANGLE);
			fuelFlowAtClimbStart = theClimbCalculator.getFuelFlowList().get(0)*2.20462/0.016667;
			fuelFlowAtClimbEnding = theClimbCalculator.getFuelFlowList().get(theClimbCalculator.getFuelFlowList().size()-1)*2.20462/0.016667;
			sfcAtClimbStart = theClimbCalculator.getSFCList().get(0);
			sfcAtClimbEnding = theClimbCalculator.getSFCList().get(theClimbCalculator.getSFCList().size()-1);

			//--------------------------------------------------------------------
			// CRUISE (CONSTANT MACH AND ALTITUDE)
			aircraftMassAtCruiseStart = 
					_initialMissionMass
					.minus(fuelTakeOff.to(SI.KILOGRAM))
					.minus(fuelClimb.to(SI.KILOGRAM));

			rangeCruise = _missionRange;
			_totalRange = Amount.valueOf(0.0, SI.METER);

			for (int iCruise=0; iCruise < 5; iCruise++) {
				double[] cruiseSteps = MyArrayUtils.linspace(
						0.0,
						rangeCruise.doubleValue(SI.METER),
						5
						);

				Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_theAircraft.getWing());

				int nPointSpeed = 1000;
				double[] speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								(aircraftMassAtCruiseStart
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLClimb)
								),
						SpeedCalc.calculateTAS(
								1.0,
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
								),
						nPointSpeed
						);

				List<DragMap> dragList = new ArrayList<>();
				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								(aircraftMassAtCruiseStart
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								speedArray,
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLClimb),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				List<ThrustMap> thrustList = new ArrayList<>();
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								1.0,
								speedArray,
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineType(), 
								_theAircraft.getPowerPlant(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
								)
						);

				List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								speedArray,
								(aircraftMassAtCruiseStart
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								1.0,
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLClimb),
								dragList,
								thrustList
								)
						);

				if(intersectionList.get(0).getMaxSpeed() < 0.01) {
					_missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
					return;
				}

				List<Double> cruiseMissionMachNumber = new ArrayList<>();
				List<Amount<Velocity>> cruiseSpeedList = new ArrayList<>();
				if(_theOperatingConditions.getMachCruise() <= intersectionList.get(0).getMaxMach()) {
					cruiseMissionMachNumber.add(_theOperatingConditions.getMachCruise());
					cruiseSpeedList.add(
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											_theOperatingConditions.getMachCruise(),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND).to(NonSI.KNOT)
							);
				}
				else {
					cruiseMissionMachNumber.add(intersectionList.get(0).getMaxMach());
					cruiseSpeedList.add(
							Amount.valueOf(
									intersectionList.get(0).getMaxSpeed(),
									SI.METERS_PER_SECOND
									).to(NonSI.KNOT)
							);
					if(cruiseMaxMachNumberErrorFlag == false) {
						System.err.println("WARNING: (CRUISE - MISSION PROFILE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						cruiseMaxMachNumberErrorFlag = true;
					}
				}

				List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
				aircraftMassPerStep.add(aircraftMassAtCruiseStart);

				List<Double> cLSteps = new ArrayList<>();
				cLSteps.add(
						LiftCalc.calculateLiftCoeff(
								aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
								*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								cruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
								)
						);

				List<Amount<Force>> dragPerStep = new ArrayList<>();
				dragPerStep.add(
						Amount.valueOf(
								DragCalc.calculateDragAtSpeed(
										aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										cruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLSteps.get(0))
//										+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
//												+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
//												-0.003767654434394
//												)
										),
								SI.NEWTON
								)
						);

				List<Double> phi = new ArrayList<>();
				phi.add(dragPerStep.get(0).to(SI.NEWTON)
						.divide(
								ThrustCalc.calculateThrustDatabase(
										_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
										_theAircraft.getPowerPlant().getEngineNumber(),
										1.0,
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(),
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant(),
										_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
										cruiseMissionMachNumber.get(0)
										)
								)
						.getEstimatedValue()
						);

				List<Double> fuelFlows = new ArrayList<>();
				if(phi.get(0) > 1.0) {
					phi.remove(0);
					phi.add(0, 1.0);
				}
				if(_calculateSFCCruise)
					fuelFlows.add(
							dragPerStep.get(0).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*(2-phi.get(0))
							*EngineDatabaseManager_old.getSFC(
									cruiseMissionMachNumber.get(0),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
									EngineDatabaseManager_old.getThrustRatio(
											cruiseMissionMachNumber.get(0),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(), 
											EngineOperatingConditionEnum.CRUISE, 
											_theAircraft.getPowerPlant()
											),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getPowerPlant().getEngineType(), 
									EngineOperatingConditionEnum.CRUISE, 
									_theAircraft.getPowerPlant()
									)
							);
				else
					fuelFlows.add(
							dragPerStep.get(0).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*_sfcFunctionCruise.value(phi.get(0))
							);

				List<Amount<Duration>> times = new ArrayList<>(); 
				times.add(
						Amount.valueOf(
								(cruiseSteps[1]-cruiseSteps[0])/cruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
								SI.SECOND
								).to(NonSI.MINUTE)
						);

				List<Amount<Mass>> fuelUsedPerStep = new ArrayList<>();
				fuelUsedPerStep.add(
						Amount.valueOf(
								fuelFlows.get(0)
								*times.get(0).doubleValue(NonSI.MINUTE),
								SI.KILOGRAM
								)
						);

				for (int j=1; j<cruiseSteps.length-1; j++) {

					aircraftMassPerStep.add(
							aircraftMassPerStep.get(j-1)
							.minus(Amount.valueOf(
									fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
									SI.KILOGRAM)
									)
							);

					dragList.add(
							DragCalc.calculateDragAndPowerRequired(
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
									(aircraftMassPerStep.get(j)
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									speedArray,
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					thrustList.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
									1.0,
									speedArray,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineType(), 
									_theAircraft.getPowerPlant(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							);

					intersectionList.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
									speedArray,
									(aircraftMassPerStep.get(j)
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									1.0,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									dragList,
									thrustList
									)
							);

					if(intersectionList.get(j).getMaxSpeed() < 0.01) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					if(_theOperatingConditions.getMachCruise() <= intersectionList.get(j).getMaxMach()) {
						cruiseMissionMachNumber.add(_theOperatingConditions.getMachCruise());
						cruiseSpeedList.add(
								Amount.valueOf(
										SpeedCalc.calculateTAS(
												_theOperatingConditions.getMachCruise(),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
												),
										SI.METERS_PER_SECOND).to(NonSI.KNOT)
								);
					}
					else {
						cruiseMissionMachNumber.add(intersectionList.get(j).getMaxMach());
						cruiseSpeedList.add(
								Amount.valueOf(
										intersectionList.get(j).getMaxSpeed(),
										SI.METERS_PER_SECOND
										).to(NonSI.KNOT)
								);
						if(cruiseMaxMachNumberErrorFlag == false) {
							System.err.println("WARNING: (CRUISE - MISSION PROFILE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							cruiseMaxMachNumberErrorFlag = true;
						}
					}

					cLSteps.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									cruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									)
							);
					

					dragPerStep.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											cruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLSteps.get(j))
//											+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
//													+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
//													-0.003767654434394
//													)
											),
									SI.NEWTON
									)
							);

					phi.add(dragPerStep.get(j).to(SI.NEWTON)
							.divide(
									ThrustCalc.calculateThrustDatabase(
											_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
											_theAircraft.getPowerPlant().getEngineNumber(),
											1.0,
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.CRUISE,
											_theAircraft.getPowerPlant(),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
											cruiseMissionMachNumber.get(j)
											)
									)
							.getEstimatedValue()
							);

					if(phi.get(j) > 1.0) {
						phi.remove(j);
						phi.add(j, 1.0);
					}
					if(_calculateSFCCruise)
						fuelFlows.add(
								dragPerStep.get(j).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*(2-phi.get(j))
								*EngineDatabaseManager_old.getSFC(
										cruiseMissionMachNumber.get(j),
										_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
										EngineDatabaseManager_old.getThrustRatio(
												cruiseMissionMachNumber.get(j),
												_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER), 
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(), 
												EngineOperatingConditionEnum.CRUISE, 
												_theAircraft.getPowerPlant()
												),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(), 
										EngineOperatingConditionEnum.CRUISE, 
										_theAircraft.getPowerPlant()
										)
								);
					else
						fuelFlows.add(
								dragPerStep.get(j).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionCruise.value(phi.get(j))
								);

					times.add(
							Amount.valueOf(
									(cruiseSteps[j]-cruiseSteps[j-1])/cruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									).to(NonSI.MINUTE)
							);

					fuelUsedPerStep.add(
							Amount.valueOf(
									fuelFlows.get(j)
									*times.get(j).doubleValue(NonSI.MINUTE),
									SI.KILOGRAM
									)
							);
				}

				timeCruise =
						Amount.valueOf(
								times.stream()
								.mapToDouble( t -> t.doubleValue(NonSI.MINUTE))
								.sum(),
								NonSI.MINUTE
								); 
				fuelCruise =
						Amount.valueOf(
								fuelUsedPerStep.stream()
								.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
								.sum(),
								SI.KILOGRAM
								);
				aircraftMassAtCruiseEnding = aircraftMassAtCruiseStart.to(SI.KILOGRAM).minus(fuelCruise.to(SI.KILOGRAM));

				speedTASAtCruiseStart = cruiseSpeedList.get(0).to(NonSI.KNOT);
				speedCASAtCruiseStart = cruiseSpeedList.get(0).times(
						Math.sqrt(
								AtmosphereCalc.getDensity(_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER))
								/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
								)
						).to(NonSI.KNOT);
				speedTASAtCruiseEnding = cruiseSpeedList.get(cruiseSpeedList.size()-1).to(NonSI.KNOT);
				speedCASAtCruiseEnding = cruiseSpeedList.get(cruiseSpeedList.size()-1).times(
						Math.sqrt(
								AtmosphereCalc.getDensity(_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER))
								/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
								)
						).to(NonSI.KNOT);
				cLAtCruiseStart = cLSteps.get(0);
				cLAtCruiseEnding = cLSteps.get(cLSteps.size()-1);
				cDAtCruiseStart = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtCruiseStart
						);
				cDAtCruiseEnding = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtCruiseEnding
						);
				dragAtCruiseStart = dragPerStep.get(0).to(NonSI.POUND_FORCE);
				dragAtCruiseEnding = dragPerStep.get(dragPerStep.size()-1).to(NonSI.POUND_FORCE);
				thrustAtCruiseStart = dragAtCruiseStart;
				thrustAtCruiseEnding = dragAtCruiseEnding;
				throttleCruiseStart = phi.get(0);
				throttleCruiseEnding = phi.get(phi.size()-1);
				rateOfClimbAtCruiseStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
				rateOfClimbAtCruiseEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
				climbAngleAtCruiseStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
				climbAngleAtCruiseEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
				fuelFlowAtCruiseStart = fuelFlows.get(0)*2.20462/0.016667;
				fuelFlowAtCruiseEnding = fuelFlows.get(fuelFlows.size()-1)*2.20462/0.016667;
				sfcAtCruiseStart = fuelFlowAtCruiseStart/thrustAtCruiseStart.doubleValue(NonSI.POUND_FORCE);
				sfcAtCruiseEnding = fuelFlowAtCruiseEnding/thrustAtCruiseEnding.doubleValue(NonSI.POUND_FORCE);

				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				aircraftMassAtFirstDescentStart = 
						_initialMissionMass
						.minus(fuelTakeOff.to(SI.KILOGRAM))
						.minus(fuelClimb.to(SI.KILOGRAM))
						.minus(fuelCruise.to(SI.KILOGRAM));

				theFirstDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_theOperatingConditions.getAltitudeCruise().to(SI.METER),
						_holdingAltitude.to(SI.METER),
						aircraftMassAtFirstDescentStart,
						_polarCLClimb,
						_polarCDClimb
						);

				theFirstDescentCalculator.calculateDescentPerformance();

				rangeFirstDescent = theFirstDescentCalculator.getTotalDescentLength().to(NonSI.NAUTICAL_MILE);
				timeFirstDescent = theFirstDescentCalculator.getTotalDescentTime().to(NonSI.MINUTE);
				fuelFirstDescent = theFirstDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);
				aircraftMassAtFirstDescentEnding = aircraftMassAtFirstDescentStart.to(SI.KILOGRAM).minus(fuelFirstDescent.to(SI.KILOGRAM));

				speedTASAtFirstDescentStart = theFirstDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
				speedCASAtFirstDescentStart = theFirstDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
				speedTASAtFirstDescentEnding = theFirstDescentCalculator.getSpeedListTAS()
						.get(theFirstDescentCalculator.getSpeedListTAS().size()-1)
						.to(NonSI.KNOT);
				speedCASAtFirstDescentEnding = theFirstDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
				cLAtFirstDescentStart = theFirstDescentCalculator.getCLSteps().get(0);
				cLAtFirstDescentEnding = theFirstDescentCalculator.getCLSteps().get(theFirstDescentCalculator.getCLSteps().size()-1);
				cDAtFirstDescentStart = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtFirstDescentStart
						);
				cDAtFirstDescentEnding = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtFirstDescentEnding
						);
				thrustAtFirstDescentStart = theFirstDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
				thrustAtFirstDescentEnding = theFirstDescentCalculator.getThrustPerStep()
						.get(theFirstDescentCalculator.getThrustPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				dragAtFirstDescentStart = theFirstDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
				dragAtFirstDescentEnding = theFirstDescentCalculator.getDragPerStep()
						.get(theFirstDescentCalculator.getDragPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				rateOfClimbAtFirstDescentStart = theFirstDescentCalculator.getRateOfDescentList().get(0).to(MyUnits.FOOT_PER_MINUTE);
				rateOfClimbAtFirstDescentEnding = theFirstDescentCalculator.getRateOfDescentList()
						.get(theFirstDescentCalculator.getRateOfDescentList().size()-1)
						.to(MyUnits.FOOT_PER_MINUTE);
				climbAngleAtFirstDescentStart = theFirstDescentCalculator.getDescentAngles().get(0).to(NonSI.DEGREE_ANGLE);
				climbAngleAtFirstDescentEnding = theFirstDescentCalculator.getDescentAngles()
						.get(theFirstDescentCalculator.getDescentAngles().size()-1)
						.to(NonSI.DEGREE_ANGLE);
				fuelFlowAtFirstDescentStart = theFirstDescentCalculator.getInterpolatedFuelFlowList().get(0)*2.20462/0.016667;
				fuelFlowAtFirstDescentEnding = theFirstDescentCalculator.getInterpolatedFuelFlowList()
						.get(theFirstDescentCalculator.getInterpolatedFuelFlowList().size()-1)
						*2.20462/0.016667;
				sfcAtFirstDescentStart = fuelFlowAtFirstDescentStart/thrustAtFirstDescentStart.doubleValue(NonSI.POUND_FORCE);
				sfcAtFirstDescentEnding = fuelFlowAtFirstDescentEnding/thrustAtFirstDescentEnding.doubleValue(NonSI.POUND_FORCE);

				//--------------------------------------------------------------------
				// SECOND CLIMB (up to ALTERNATE altitude)
				theSecondClimbCalculator = new ClimbCalc(
						_theAircraft,
						_theOperatingConditions,
						_cLmaxClean, 
						_polarCLClimb,
						_polarCDClimb,
						_climbSpeed, 
						_dragDueToEnigneFailure 
						);

				aircraftMassAtSecondClimbStart = _initialMissionMass
						.minus(fuelTakeOff.to(SI.KILOGRAM))
						.minus(fuelClimb.to(SI.KILOGRAM))
						.minus(fuelCruise.to(SI.KILOGRAM))
						.minus(fuelFirstDescent.to(SI.KILOGRAM));

				theSecondClimbCalculator.calculateClimbPerformance(
						aircraftMassAtSecondClimbStart,
						aircraftMassAtSecondClimbStart,
						_holdingAltitude.to(SI.METER),
						_alternateCruiseAltitude.to(SI.METER),
						false,
						false
						);

				rangeSecondClimb = theSecondClimbCalculator.getClimbTotalRange();
				timeSecondClimb = null;
				if(_climbSpeed != null)
					timeSecondClimb = theSecondClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
				else
					timeSecondClimb = theSecondClimbCalculator.getMinimumClimbTimeAEO();
				fuelSecondClimb = theSecondClimbCalculator.getClimbTotalFuelUsed();

				aircraftMassAtSecondClimbEnding = aircraftMassAtSecondClimbStart.to(SI.KILOGRAM).minus(fuelSecondClimb.to(SI.KILOGRAM));

				if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
					speedTASAtSecondClimbStart = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(_obstacleLanding.doubleValue(SI.METER))
											.getDensityRatio()
											)
									);
					speedCASAtSecondClimbStart = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT);
					speedTASAtSecondClimbEnding = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(
													_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
													)
											.getDensityRatio()
											)
									);
					speedCASAtSecondClimbEnding = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT);

					cLAtSecondClimbStart = LiftCalc.calculateLiftCoeff(
							aircraftMassAtSecondClimbStart.times(AtmosphereCalc.g0).getEstimatedValue(),
							speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							0.0
							);
					cLAtSecondClimbEnding = LiftCalc.calculateLiftCoeff(
							aircraftMassAtSecondClimbEnding
							.times(AtmosphereCalc.g0)
							.getEstimatedValue(),
							speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
							);
					cDAtSecondClimbStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondClimbStart
							);
					cDAtSecondClimbEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondClimbEnding
							);
					thrustAtSecondClimbStart = theSecondClimbCalculator.getThrustAtClimbStart().to(NonSI.POUND_FORCE);
					thrustAtSecondClimbEnding = theSecondClimbCalculator.getThrustAtClimbEnding().to(NonSI.POUND_FORCE);
					dragAtSecondClimbStart = theSecondClimbCalculator.getDragAtClimbStart().to(NonSI.POUND_FORCE);
					dragAtSecondClimbEnding = theSecondClimbCalculator.getDragAtClimbEnding().to(NonSI.POUND_FORCE);
					rateOfClimbAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(0).getRC(),
									speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtSecondClimbEnding = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getRC(),
									speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.METERS_PER_SECOND).to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(0).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(0).getClimbAngleList(),
									speedTASAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.RADIAN).to(NonSI.DEGREE_ANGLE);
					climbAngleAtSecondClimbStart = Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getSpeedList(), 
									theSecondClimbCalculator.getRCMapAEO().get(theSecondClimbCalculator.getRCMapAEO().size()-1).getClimbAngleList(),
									speedTASAtSecondClimbEnding.doubleValue(SI.METERS_PER_SECOND)
									),
							SI.RADIAN).to(NonSI.DEGREE_ANGLE);
					fuelFlowAtSecondClimbStart = theSecondClimbCalculator.getFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtSecondClimbEnding = theSecondClimbCalculator.getFuelFlowList().get(theSecondClimbCalculator.getFuelFlowList().size()-1)*2.20462/0.016667;
					sfcAtSecondClimbStart = theSecondClimbCalculator.getSFCList().get(0);
					sfcAtSecondClimbEnding = theSecondClimbCalculator.getSFCList().get(theSecondClimbCalculator.getSFCList().size()-1);
				}

				//--------------------------------------------------------------------
				// ALTERNATE CRUISE (AT MAX EFFICIENCY)
				aircraftMassAtAlternateCruiseStart = 
						_initialMissionMass
						.minus(fuelTakeOff.to(SI.KILOGRAM))
						.minus(fuelClimb.to(SI.KILOGRAM))
						.minus(fuelCruise.to(SI.KILOGRAM))
						.minus(fuelFirstDescent.to(SI.KILOGRAM))
						.minus(fuelSecondClimb.to(SI.KILOGRAM));

				rangeAlternateCruise = _alternateCruiseLength;

				for (int iAlternate=0; iAlternate < 5; iAlternate++) {
					double[] speedArrayAlternate = MyArrayUtils.linspace(
							SpeedCalc.calculateSpeedStall(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb)
									),
							SpeedCalc.calculateTAS(
									1.0,
									_alternateCruiseAltitude.doubleValue(SI.METER)
									),
							nPointSpeed
							);

					List<DragMap> dragListAlternate = new ArrayList<>();
					dragListAlternate.add(
							DragCalc.calculateDragAndPowerRequired(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									speedArrayAlternate,
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListAlternate = new ArrayList<>();
					thrustListAlternate.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									1.0,
									speedArrayAlternate,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineType(), 
									_theAircraft.getPowerPlant(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							);

					List<DragThrustIntersectionMap> intersectionListAlternate = new ArrayList<>();
					intersectionListAlternate.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									speedArrayAlternate,
									(aircraftMassAtAlternateCruiseStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									1.0,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									dragListAlternate,
									thrustListAlternate
									)
							);

					if(intersectionListAlternate.get(0).getMaxSpeed() < 0.01) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Amount<Mass>> aircraftMassPerStepAlternateCruise = new ArrayList<>();
					aircraftMassPerStepAlternateCruise.add(aircraftMassAtAlternateCruiseStart);

					List<Double> rangeFactorAlternateCruiseList = new ArrayList<>();
					double[] cLRangeAlternateCruiseArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(_polarCLCruise),
							50
							); 
					for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							rangeFactorAlternateCruiseList.add(
									Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
											MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
											cLRangeAlternateCruiseArray[iCL]
											)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

							rangeFactorAlternateCruiseList.add(
									cLRangeAlternateCruiseArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLRangeAlternateCruiseArray[iCL]
													)
									);

						}
					}

					int iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
					double bestMachAlternateCruise = SpeedCalc.calculateMach(
							_alternateCruiseAltitude.doubleValue(SI.METER), 
							SpeedCalc.calculateSpeedAtCL(
									aircraftMassPerStepAlternateCruise.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
									AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER)),
									cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
									)
							);

					List<Double> alternateCruiseMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> alternateCruiseSpeedList = new ArrayList<>();
					if(bestMachAlternateCruise <= intersectionListAlternate.get(0).getMaxMach()) {
						alternateCruiseMachNumberList.add(bestMachAlternateCruise);
						alternateCruiseSpeedList.add(
								Amount.valueOf(
										SpeedCalc.calculateTAS(
												bestMachAlternateCruise,
												_alternateCruiseAltitude.doubleValue(SI.METER)
												),
										SI.METERS_PER_SECOND).to(NonSI.KNOT)
								);
					}
					else {
						alternateCruiseMachNumberList.add(intersectionListAlternate.get(0).getMaxMach());
						alternateCruiseSpeedList.add(
								Amount.valueOf(
										intersectionListAlternate.get(0).getMaxSpeed(),
										SI.METERS_PER_SECOND
										).to(NonSI.KNOT)
								);
						if(alternateCruiseBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							alternateCruiseBestMachNumberErrorFlag = true;
						}
					}

					double[] alternateCruiseSteps = MyArrayUtils.linspace(
							0.0,
							rangeAlternateCruise.doubleValue(SI.METER),
							5
							);

					List<Double> cLStepsAlternateCruise = new ArrayList<>();
					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepAlternateCruise.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_alternateCruiseAltitude.doubleValue(SI.METER)
									)
							);

					List<Amount<Force>> dragPerStepAlternateCruise = new ArrayList<>();
					dragPerStepAlternateCruise.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStepAlternateCruise.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_alternateCruiseAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLStepsAlternateCruise.get(0))
//											+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
//													+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
//													-0.003767654434394
//													)
											),
									SI.NEWTON
									)
							);

					List<Double> phiAlternateCruise = new ArrayList<>();
					phiAlternateCruise.add(dragPerStepAlternateCruise.get(0).to(SI.NEWTON)
							.divide(
									ThrustCalc.calculateThrustDatabase(
											_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
											_theAircraft.getPowerPlant().getEngineNumber(),
											1.0,
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.CRUISE,
											_theAircraft.getPowerPlant(),
											_alternateCruiseAltitude.doubleValue(SI.METER), 
											alternateCruiseMachNumberList.get(0)
											)
									)
							.getEstimatedValue()
							);

					List<Double> fuelFlowsAlternateCruise = new ArrayList<>();
					if(phiAlternateCruise.get(0) > 1.0) {
						phiAlternateCruise.remove(0);
						phiAlternateCruise.add(0, 1.0);
					}
					if(_calculateSFCAlternateCruise)
						fuelFlowsAlternateCruise.add(
								dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*(2-phiAlternateCruise.get(0))
								*EngineDatabaseManager_old.getSFC(
										alternateCruiseMachNumberList.get(0),
										_alternateCruiseAltitude.doubleValue(SI.METER), 
										EngineDatabaseManager_old.getThrustRatio(
												alternateCruiseMachNumberList.get(0),
												_alternateCruiseAltitude.doubleValue(SI.METER), 
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(), 
												EngineOperatingConditionEnum.CRUISE, 
												_theAircraft.getPowerPlant()
												),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(), 
										EngineOperatingConditionEnum.CRUISE, 
										_theAircraft.getPowerPlant()
										)
								);
					else
						fuelFlowsAlternateCruise.add(
								dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(0))
								);

					List<Amount<Duration>> timesAlternateCruise = new ArrayList<>(); 
					timesAlternateCruise.add(
							Amount.valueOf(
									(alternateCruiseSteps[1]-alternateCruiseSteps[0])
									/alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									).to(NonSI.MINUTE)
							);

					List<Amount<Mass>> fuelUsedPerStepAlternateCruise = new ArrayList<>();
					fuelUsedPerStepAlternateCruise.add(
							Amount.valueOf(
									fuelFlowsAlternateCruise.get(0)
									*timesAlternateCruise.get(0).doubleValue(NonSI.MINUTE),
									SI.KILOGRAM
									)
							);

					for (int j=1; j<alternateCruiseSteps.length-1; j++) {

						aircraftMassPerStepAlternateCruise.add(
								aircraftMassPerStepAlternateCruise.get(j-1)
								.minus(Amount.valueOf(
										fuelUsedPerStepAlternateCruise.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
										SI.KILOGRAM)
										)
								);

						dragListAlternate.add(
								DragCalc.calculateDragAndPowerRequired(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										(aircraftMassPerStepAlternateCruise.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										speedArrayAlternate,
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
										MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
										_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListAlternate.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										1.0,
										speedArrayAlternate,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineType(), 
										_theAircraft.getPowerPlant(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
										_theAircraft.getPowerPlant().getEngineNumber(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
										)
								);

						intersectionListAlternate.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										_alternateCruiseAltitude.doubleValue(SI.METER),
										speedArrayAlternate,
										(aircraftMassPerStepAlternateCruise.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										1.0,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										dragListAlternate,
										thrustListAlternate
										)
								);

						if(intersectionListAlternate.get(j).getMaxSpeed() < 0.01) {
							_missionProfileStopped = Boolean.TRUE;
							System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) ALTERNATE CRUISE MACH NUMBER = 0.0. RETURNING ... ");
							return;
						}

						rangeFactorAlternateCruiseList = new ArrayList<>();
						for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {

							if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

								rangeFactorAlternateCruiseList.add(
										Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLRangeAlternateCruiseArray[iCL]
												)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

								rangeFactorAlternateCruiseList.add(
										cLRangeAlternateCruiseArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
														MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
														cLRangeAlternateCruiseArray[iCL]
														)
										);

							}
						}

						iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
						bestMachAlternateCruise = SpeedCalc.calculateMach(
								_alternateCruiseAltitude.doubleValue(SI.METER), 
								SpeedCalc.calculateSpeedAtCL(
										aircraftMassPerStepAlternateCruise.get(j).times(AtmosphereCalc.g0).getEstimatedValue(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER)),
										cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
										)
								);

						if(bestMachAlternateCruise <= intersectionListAlternate.get(j).getMaxMach()) {
							alternateCruiseMachNumberList.add(bestMachAlternateCruise);
							alternateCruiseSpeedList.add(
									Amount.valueOf(
											SpeedCalc.calculateTAS(
													bestMachAlternateCruise,
													_alternateCruiseAltitude.doubleValue(SI.METER)
													),
											SI.METERS_PER_SECOND).to(NonSI.KNOT)
									);
						}
						else {
							alternateCruiseMachNumberList.add(intersectionListAlternate.get(j).getMaxMach());
							alternateCruiseSpeedList.add(
									Amount.valueOf(
											intersectionListAlternate.get(j).getMaxSpeed(),
											SI.METERS_PER_SECOND
											).to(NonSI.KNOT)
									);
							if(alternateCruiseBestMachNumberErrorFlag == false) {
								System.err.println("WARNING: (ALTERNATE CRUISE - MISSION PROFILE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
								alternateCruiseBestMachNumberErrorFlag = true;
							}
						}

						cLStepsAlternateCruise.add(
								LiftCalc.calculateLiftCoeff(
										aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										_alternateCruiseAltitude.doubleValue(SI.METER)
										)
								);
						dragPerStepAlternateCruise.add(
								Amount.valueOf(
										DragCalc.calculateDragAtSpeed(
												aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												_alternateCruiseAltitude.doubleValue(SI.METER),
												_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
												MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
														MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
														cLStepsAlternateCruise.get(j))
//												+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
//														+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
//														-0.003767654434394
//														)
												),
										SI.NEWTON
										)
								);

						phiAlternateCruise.add(dragPerStepAlternateCruise.get(j).to(SI.NEWTON)
								.divide(
										ThrustCalc.calculateThrustDatabase(
												_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
												_theAircraft.getPowerPlant().getEngineNumber(),
												1.0,
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(),
												EngineOperatingConditionEnum.CRUISE,
												_theAircraft.getPowerPlant(),
												_alternateCruiseAltitude.doubleValue(SI.METER), 
												alternateCruiseMachNumberList.get(j)
												)
										)
								.getEstimatedValue()
								);

						if(phiAlternateCruise.get(j) > 1.0) {
							phiAlternateCruise.remove(j);
							phiAlternateCruise.add(j, 1.0);
						}

						if(_calculateSFCAlternateCruise)
							fuelFlowsAlternateCruise.add(
									dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*(2-phiAlternateCruise.get(j))
									*EngineDatabaseManager_old.getSFC(
											alternateCruiseMachNumberList.get(j),
											_alternateCruiseAltitude.doubleValue(SI.METER), 
											EngineDatabaseManager_old.getThrustRatio(
													alternateCruiseMachNumberList.get(j),
													_alternateCruiseAltitude.doubleValue(SI.METER), 
													_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
													_theAircraft.getPowerPlant().getEngineType(), 
													EngineOperatingConditionEnum.CRUISE, 
													_theAircraft.getPowerPlant()
													),
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(), 
											EngineOperatingConditionEnum.CRUISE, 
											_theAircraft.getPowerPlant()
											)
									);
						else
							fuelFlowsAlternateCruise.add(
									dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(j))
									);

						timesAlternateCruise.add(
								Amount.valueOf(
										(alternateCruiseSteps[j]-alternateCruiseSteps[j-1])
										/alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										SI.SECOND
										).to(NonSI.MINUTE)
								);

						fuelUsedPerStepAlternateCruise.add(
								Amount.valueOf(
										fuelFlowsAlternateCruise.get(j)
										*timesAlternateCruise.get(j).doubleValue(NonSI.MINUTE),
										SI.KILOGRAM
										)
								);
					}

					timeAlternateCruise =
							Amount.valueOf(
									timesAlternateCruise.stream()
									.mapToDouble( t -> t.doubleValue(NonSI.MINUTE))
									.sum(),
									NonSI.MINUTE
									);
					fuelAlternateCruise =
							Amount.valueOf(
									fuelUsedPerStepAlternateCruise.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);
					aircraftMassAtAlternateCruiseEnding = aircraftMassAtAlternateCruiseStart.to(SI.KILOGRAM).minus(fuelAlternateCruise.to(SI.KILOGRAM));

					if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
						speedTASAtAlternateCruiseStart = alternateCruiseSpeedList.get(0).to(NonSI.KNOT);
						speedTASAtAlternateCruiseEnding = alternateCruiseSpeedList.get(alternateCruiseSpeedList.size()-1).to(NonSI.KNOT);
						speedCASAtAlternateCruiseStart = alternateCruiseSpeedList.get(0).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						speedCASAtAlternateCruiseEnding = alternateCruiseSpeedList.get(alternateCruiseSpeedList.size()-1).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_alternateCruiseAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						cLAtAlternateCruiseStart = cLStepsAlternateCruise.get(0);
						cLAtAlternateCruiseEnding = cLStepsAlternateCruise.get(cLStepsAlternateCruise.size()-1);
						cDAtAlternateCruiseStart = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								cLAtAlternateCruiseStart
								);
						cDAtAlternateCruiseEnding = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								cLAtAlternateCruiseEnding
								);
						dragAtAlternateCruiseStart = dragPerStepAlternateCruise.get(0).to(NonSI.POUND_FORCE);
						dragAtAlternateCruiseEnding = dragPerStepAlternateCruise.get(dragPerStepAlternateCruise.size()-1).to(NonSI.POUND_FORCE);
						thrustAtAlternateCruiseStart = dragAtAlternateCruiseStart;
						thrustAtAlternateCruiseEnding = dragAtAlternateCruiseEnding;
						throttleAlternateCruiseStart = phiAlternateCruise.get(0);
						throttleAlternateCruiseEnding = phiAlternateCruise.get(phiAlternateCruise.size()-1);
						rateOfClimbAtAlternateCruiseStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						rateOfClimbAtAlternateCruiseEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						climbAngleAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						climbAngleAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						fuelFlowAtAlternateCruiseStart = fuelFlowsAlternateCruise.get(0)*2.20462/0.016667;
						fuelFlowAtAlternateCruiseEnding = fuelFlowsAlternateCruise.get(fuelFlowsAlternateCruise.size()-1)*2.20462/0.016667;
						sfcAtAlternateCruiseStart = fuelFlowAtAlternateCruiseStart/thrustAtAlternateCruiseStart.doubleValue(NonSI.POUND_FORCE);
						sfcAtAlternateCruiseEnding = fuelFlowAtAlternateCruiseEnding/thrustAtAlternateCruiseEnding.doubleValue(NonSI.POUND_FORCE);
					}

					//--------------------------------------------------------------------
					// DESCENT (up to HOLDING altitude)
					aircraftMassAtSecondDescentStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM));

					theSecondDescentCalculator = new DescentCalc(
							_theAircraft,
							_speedDescentCAS,
							_rateOfDescent,
							_alternateCruiseAltitude.to(SI.METER),
							_holdingAltitude.to(SI.METER),
							aircraftMassAtSecondDescentStart,
							_polarCLClimb,
							_polarCDClimb
							);

					theSecondDescentCalculator.calculateDescentPerformance();

					rangeSecondDescent = theSecondDescentCalculator.getTotalDescentLength().to(NonSI.NAUTICAL_MILE);
					timeSecondDescent = theSecondDescentCalculator.getTotalDescentTime().to(NonSI.MINUTE);
					fuelSecondDescent = theSecondDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);
					aircraftMassAtSecondDescentEnding = aircraftMassAtSecondDescentStart.to(SI.KILOGRAM).minus(fuelSecondDescent.to(SI.KILOGRAM));

					speedTASAtSecondDescentStart = theSecondDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
					speedTASAtSecondDescentEnding = theSecondDescentCalculator.getSpeedListTAS()
							.get(theSecondDescentCalculator.getSpeedListTAS().size()-1)
							.to(NonSI.KNOT);
					speedCASAtSecondDescentStart = theSecondDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					speedCASAtSecondDescentEnding = theSecondDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					cLAtSecondDescentStart = theSecondDescentCalculator.getCLSteps().get(0);
					cLAtSecondDescentEnding = theSecondDescentCalculator.getCLSteps().get(theSecondDescentCalculator.getCLSteps().size()-1);
					cDAtSecondDescentStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondDescentStart
							);
					cDAtSecondDescentEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtSecondDescentEnding
							);
					thrustAtSecondDescentStart = theSecondDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
					thrustAtSecondDescentEnding = theSecondDescentCalculator.getThrustPerStep()
							.get(theSecondDescentCalculator.getThrustPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					dragAtSecondDescentStart = theSecondDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
					dragAtSecondDescentEnding = theSecondDescentCalculator.getDragPerStep()
							.get(theSecondDescentCalculator.getDragPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					rateOfClimbAtSecondDescentStart = theSecondDescentCalculator.getRateOfDescentList().get(0).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtSecondDescentEnding = theSecondDescentCalculator.getRateOfDescentList()
							.get(theSecondDescentCalculator.getRateOfDescentList().size()-1)
							.to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtSecondDescentStart = theSecondDescentCalculator.getDescentAngles().get(0).to(NonSI.DEGREE_ANGLE);
					climbAngleAtSecondDescentEnding = theSecondDescentCalculator.getDescentAngles()
							.get(theSecondDescentCalculator.getDescentAngles().size()-1)
							.to(NonSI.DEGREE_ANGLE);
					fuelFlowAtSecondDescentStart = theSecondDescentCalculator.getInterpolatedFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtSecondDescentEnding = theSecondDescentCalculator.getInterpolatedFuelFlowList()
							.get(theSecondDescentCalculator.getInterpolatedFuelFlowList().size()-1)
							*2.20462/0.016667;
					sfcAtSecondDescentStart = fuelFlowAtSecondDescentStart/thrustAtSecondDescentStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtSecondDescentEnding = fuelFlowAtSecondDescentEnding/thrustAtSecondDescentEnding.doubleValue(NonSI.POUND_FORCE);

					//--------------------------------------------------------------------
					// HOLDING (BEST ENDURANCE)
					aircraftMassAtHoldingStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM));

					double[] timeHoldingArray = MyArrayUtils.linspace(
							0.0,
							_holdingDuration.doubleValue(NonSI.MINUTE),
							5
							);

					double[] speedArrayHolding = MyArrayUtils.linspace(
							SpeedCalc.calculateSpeedStall(
									_holdingAltitude.doubleValue(SI.METER),
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb)
									),
							SpeedCalc.calculateTAS(
									1.0,
									_holdingAltitude.doubleValue(SI.METER)
									),
							nPointSpeed
							);

					List<DragMap> dragListHolding = new ArrayList<>();
					dragListHolding.add(
							DragCalc.calculateDragAndPowerRequired(
									_holdingAltitude.doubleValue(SI.METER),
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									speedArrayHolding,
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
									MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					List<ThrustMap> thrustListHolding = new ArrayList<>();
					thrustListHolding.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_holdingAltitude.doubleValue(SI.METER),
									1.0,
									speedArrayHolding,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineType(), 
									_theAircraft.getPowerPlant(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
									)
							);

					List<DragThrustIntersectionMap> intersectionListHolding = new ArrayList<>();
					intersectionListHolding.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									_holdingAltitude.doubleValue(SI.METER),
									speedArrayHolding,
									(aircraftMassAtHoldingStart
											.times(AtmosphereCalc.g0)
											.getEstimatedValue()
											),
									1.0,
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLClimb),
									dragListHolding,
									thrustListHolding
									)
							);

					if(intersectionListHolding.get(0).getMaxSpeed() < 0.01) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (HOLDING - MISSION PROFILE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
						return;
					}

					List<Amount<Mass>> aircraftMassPerStepHolding = new ArrayList<>();
					aircraftMassPerStepHolding.add(aircraftMassAtHoldingStart);

					List<Double> enduranceFactorHoldingList = new ArrayList<>();
					double[] cLEnduranceHoldingArray = MyArrayUtils.linspace(
							0.1,
							MyArrayUtils.getMax(_polarCLClimb),
							50
							); 
					for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {

						if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

							enduranceFactorHoldingList.add(
									cLEnduranceHoldingArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
													MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
													cLEnduranceHoldingArray[iCL]
													)
									);

						else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
								|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

							enduranceFactorHoldingList.add(
									Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
											MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
											cLEnduranceHoldingArray[iCL]
											)
									);

						}
					}

					int iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
					double bestMachHolding = SpeedCalc.calculateMach(
							_holdingAltitude.doubleValue(SI.METER), 
							SpeedCalc.calculateSpeedAtCL(
									aircraftMassPerStepHolding.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
									AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER)),
									cLEnduranceHoldingArray[iBestSpeedHolding]
									)
							);

					List<Double> holdingMachNumberList = new ArrayList<>();
					List<Amount<Velocity>> holdingSpeedList = new ArrayList<>();
					if(bestMachHolding <= intersectionListHolding.get(0).getMaxMach()) {
						holdingMachNumberList.add(bestMachHolding);
						holdingSpeedList.add(
								Amount.valueOf(
										SpeedCalc.calculateTAS(
												bestMachHolding,
												_holdingAltitude.doubleValue(SI.METER)
												),
										SI.METERS_PER_SECOND).to(NonSI.KNOT)
								);
					}
					else {
						holdingMachNumberList.add(intersectionListHolding.get(0).getMaxMach());
						holdingSpeedList.add(
								Amount.valueOf(
										intersectionListHolding.get(0).getMaxSpeed(),
										SI.METERS_PER_SECOND
										).to(NonSI.KNOT)
								);
						System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
					}

					List<Double> cLStepsHolding = new ArrayList<>();
					cLStepsHolding.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									holdingSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_holdingAltitude.doubleValue(SI.METER)
									)
							);

					List<Amount<Force>> dragPerStepHolding = new ArrayList<>();
					dragPerStepHolding.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_holdingAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
											holdingSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
													MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
													cLStepsHolding.get(0))
											),
									SI.NEWTON
									)
							);

					List<Double> phiHolding = new ArrayList<>();
					phiHolding.add(dragPerStepHolding.get(0).to(SI.NEWTON)
							.divide(
									ThrustCalc.calculateThrustDatabase(
											_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
											_theAircraft.getPowerPlant().getEngineNumber(),
											1.0,
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(),
											EngineOperatingConditionEnum.CRUISE,
											_theAircraft.getPowerPlant(),
											_holdingAltitude.doubleValue(SI.METER), 
											holdingMachNumberList.get(0)
											)
									)
							.getEstimatedValue()
							);

					List<Double> fuelFlowsHolding = new ArrayList<>();
					if(phiHolding.get(0) > 1.0) {
						phiHolding.remove(0);
						phiHolding.add(0, 1.0);
					}
					if(_calculateSFCHolding)
						fuelFlowsHolding.add(
								dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*(2-phiHolding.get(0))
								*EngineDatabaseManager_old.getSFC(
										holdingMachNumberList.get(0),
										_holdingAltitude.doubleValue(SI.METER), 
										EngineDatabaseManager_old.getThrustRatio(
												holdingMachNumberList.get(0),
												_holdingAltitude.doubleValue(SI.METER), 
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(), 
												EngineOperatingConditionEnum.CRUISE, 
												_theAircraft.getPowerPlant()
												),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(), 
										EngineOperatingConditionEnum.CRUISE, 
										_theAircraft.getPowerPlant()
										)
								);
					else
						fuelFlowsHolding.add(
								dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionHolding.value(phiHolding.get(0))
								);

					List<Amount<Mass>> fuelUsedPerStepHolding = new ArrayList<>();
					fuelUsedPerStepHolding.add(
							Amount.valueOf(
									fuelFlowsHolding.get(0)
									*(timeHoldingArray[1]-timeHoldingArray[0]),
									SI.KILOGRAM
									)
							);

					for(int j=1; j<timeHoldingArray.length-1; j++) {

						aircraftMassPerStepHolding.add(
								aircraftMassPerStepHolding.get(j-1)
								.minus(Amount.valueOf(
										fuelUsedPerStepHolding.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
										SI.KILOGRAM)
										)
								);

						dragListHolding.add(
								DragCalc.calculateDragAndPowerRequired(
										_holdingAltitude.doubleValue(SI.METER),
										(aircraftMassPerStepHolding.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										speedArrayHolding,
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
										MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
										_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
										meanAirfoil.getThicknessToChordRatio(),
										meanAirfoil.getType()
										)
								);

						thrustListHolding.add(
								ThrustCalc.calculateThrustAndPowerAvailable(
										_holdingAltitude.doubleValue(SI.METER),
										1.0,
										speedArrayHolding,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineType(), 
										_theAircraft.getPowerPlant(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
										_theAircraft.getPowerPlant().getEngineNumber(),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
										)
								);

						intersectionListHolding.add(
								PerformanceCalcUtils.calculateDragThrustIntersection(
										_holdingAltitude.doubleValue(SI.METER),
										speedArrayHolding,
										(aircraftMassPerStepHolding.get(j)
												.times(AtmosphereCalc.g0)
												.getEstimatedValue()
												),
										1.0,
										EngineOperatingConditionEnum.CRUISE,
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										MyArrayUtils.getMax(_polarCLClimb),
										dragListHolding,
										thrustListHolding
										)
								);

						if(intersectionListHolding.get(j).getMaxSpeed() < 0.01) {
							_missionProfileStopped = Boolean.TRUE;
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
							return;
						}

						enduranceFactorHoldingList = new ArrayList<>();
						for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {

							if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOJET) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOFAN) )

								enduranceFactorHoldingList.add(
										cLEnduranceHoldingArray[iCL]
												/ MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
														MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
														cLEnduranceHoldingArray[iCL]
														)
										);

							else if(_theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP) 
									|| _theAircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON) ) {

								enduranceFactorHoldingList.add(
										Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
										/ MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
												MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
												cLEnduranceHoldingArray[iCL]
												)
										);

							}
						}

						iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
						bestMachHolding = SpeedCalc.calculateMach(
								_holdingAltitude.doubleValue(SI.METER), 
								SpeedCalc.calculateSpeedAtCL(
										aircraftMassPerStepHolding.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER)),
										cLEnduranceHoldingArray[iBestSpeedHolding]
										)
								);

						if(bestMachHolding <= intersectionListHolding.get(j).getMaxMach()) {
							holdingMachNumberList.add(bestMachHolding);
							holdingSpeedList.add(
									Amount.valueOf(
											SpeedCalc.calculateTAS(
													bestMachHolding,
													_holdingAltitude.doubleValue(SI.METER)
													),
											SI.METERS_PER_SECOND).to(NonSI.KNOT)
									);
						}
						else {
							holdingMachNumberList.add(intersectionListHolding.get(j).getMaxMach());
							holdingSpeedList.add(
									Amount.valueOf(
											intersectionListHolding.get(j).getMaxSpeed(),
											SI.METERS_PER_SECOND
											).to(NonSI.KNOT)
									);
							System.err.println("WARNING: (HOLDING - MISSION PROFILE) THE BEST HOLDING CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						}

						cLStepsHolding.add(
								LiftCalc.calculateLiftCoeff(
										aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										holdingSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
										_holdingAltitude.doubleValue(SI.METER)
										)
								);
						dragPerStepHolding.add(
								Amount.valueOf(
										DragCalc.calculateDragAtSpeed(
												aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
												_holdingAltitude.doubleValue(SI.METER),
												_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
												holdingSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
												MyMathUtils.getInterpolatedValue1DLinear(
														MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
														MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
														cLStepsHolding.get(j))
												),
										SI.NEWTON
										)
								);

						phiHolding.add(dragPerStepHolding.get(j).to(SI.NEWTON)
								.divide(
										ThrustCalc.calculateThrustDatabase(
												_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
												_theAircraft.getPowerPlant().getEngineNumber(),
												1.0,
												_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
												_theAircraft.getPowerPlant().getEngineType(),
												EngineOperatingConditionEnum.CRUISE,
												_theAircraft.getPowerPlant(),
												_holdingAltitude.doubleValue(SI.METER), 
												holdingMachNumberList.get(j)
												)
										)
								.getEstimatedValue()
								);

						if(phiHolding.get(j) > 1.0) {
							phiHolding.remove(j);
							phiHolding.add(j, 1.0);
						}

						if(_calculateSFCHolding)
							fuelFlowsHolding.add(
									dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*(2-phiHolding.get(j))
									*EngineDatabaseManager_old.getSFC(
											holdingMachNumberList.get(j),
											_holdingAltitude.doubleValue(SI.METER), 
											EngineDatabaseManager_old.getThrustRatio(
													holdingMachNumberList.get(j),
													_holdingAltitude.doubleValue(SI.METER), 
													_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
													_theAircraft.getPowerPlant().getEngineType(), 
													EngineOperatingConditionEnum.CRUISE, 
													_theAircraft.getPowerPlant()
													),
											_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
											_theAircraft.getPowerPlant().getEngineType(), 
											EngineOperatingConditionEnum.CRUISE, 
											_theAircraft.getPowerPlant()
											)
									);
						else
							fuelFlowsHolding.add(
									dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
									*(0.224809)*(0.454/60)
									*_sfcFunctionHolding.value(phiHolding.get(j))
									);

						fuelUsedPerStepHolding.add(
								Amount.valueOf(
										fuelFlowsHolding.get(j)
										*(timeHoldingArray[j+1]-timeHoldingArray[j]),
										SI.KILOGRAM
										)
								);
					}

					rangeHolding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					timeHolding = _holdingDuration.to(NonSI.MINUTE);
					fuelHolding =
							Amount.valueOf(
									fuelUsedPerStepHolding.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);
					aircraftMassAtHoldingEnding = aircraftMassAtHoldingStart.to(SI.KILOGRAM).minus(fuelHolding.to(SI.KILOGRAM));

					if (_holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
						speedTASAtHoldingStart = holdingSpeedList.get(0).to(NonSI.KNOT);
						speedTASAtHoldingEnding = holdingSpeedList.get(holdingSpeedList.size()-1).to(NonSI.KNOT);
						speedCASAtHoldingStart = holdingSpeedList.get(0).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						speedCASAtHoldingEnding = holdingSpeedList.get(holdingSpeedList.size()-1).times(
								Math.sqrt(
										AtmosphereCalc.getDensity(_holdingAltitude.doubleValue(SI.METER))
										/_theOperatingConditions.getDensityTakeOff().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
										)
								).to(NonSI.KNOT);
						cLAtHoldingStart = cLStepsHolding.get(0);
						cLAtHoldingEnding = cLStepsHolding.get(cLStepsHolding.size()-1);
						cDAtHoldingStart = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
								cLAtHoldingStart
								);
						cDAtHoldingEnding = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
								cLAtHoldingEnding
								);
						dragAtHoldingStart = dragPerStepHolding.get(0).to(NonSI.POUND_FORCE);
						dragAtHoldingEnding = dragPerStepHolding.get(dragPerStepHolding.size()-1).to(NonSI.POUND_FORCE);
						thrustAtHoldingStart = dragAtHoldingStart;
						thrustAtHoldingEnding = dragAtHoldingEnding;
						throttleHoldingStart = phiHolding.get(0);
						throttleHoldingEnding = phiHolding.get(phiHolding.size()-1);
						rateOfClimbAtHoldingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						rateOfClimbAtHoldingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
						climbAngleAtHoldingStart = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						climbAngleAtHoldingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
						fuelFlowAtHoldingStart = fuelFlowsHolding.get(0)*2.20462/0.016667;
						fuelFlowAtHoldingEnding = fuelFlowsHolding.get(fuelFlowsHolding.size()-1)*2.20462/0.016667;
						sfcAtHoldingStart = fuelFlowAtHoldingStart/thrustAtHoldingStart.doubleValue(NonSI.POUND_FORCE);
						sfcAtHoldingEnding = fuelFlowAtHoldingEnding/thrustAtHoldingEnding.doubleValue(NonSI.POUND_FORCE);
					}

					//--------------------------------------------------------------------
					// DESCENT (up to LANDING altitude)
					aircraftMassAtThirdDescentStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM))
							.minus(fuelHolding.to(SI.KILOGRAM));

					theThirdDescentCalculator = new DescentCalc(
							_theAircraft,
							_speedDescentCAS,
							_rateOfDescent,
							_holdingAltitude.to(SI.METER),
							Amount.valueOf(15.24, SI.METER),
							aircraftMassAtThirdDescentStart,
							_polarCLClimb,
							_polarCDClimb
							);

					theThirdDescentCalculator.calculateDescentPerformance();

					rangeThirdDescent = theThirdDescentCalculator.getTotalDescentLength().to(NonSI.NAUTICAL_MILE);
					timeThirdDescent = theThirdDescentCalculator.getTotalDescentTime().to(NonSI.MINUTE);
					fuelThirdDescent = theThirdDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);
					aircraftMassAtThirdDescentEnding = aircraftMassAtThirdDescentStart.to(SI.KILOGRAM).minus(fuelThirdDescent.to(SI.KILOGRAM));

					speedTASAtThirdDescentStart = theThirdDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
					speedTASAtThirdDescentEnding = theThirdDescentCalculator.getSpeedListTAS()
							.get(theThirdDescentCalculator.getSpeedListTAS().size()-1)
							.to(NonSI.KNOT);
					speedCASAtThirdDescentStart = theThirdDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					speedCASAtThirdDescentEnding = theThirdDescentCalculator.getSpeedDescentCAS().to(NonSI.KNOT);
					cLAtThirdDescentStart = theThirdDescentCalculator.getCLSteps().get(0);
					cLAtThirdDescentEnding = theThirdDescentCalculator.getCLSteps().get(theThirdDescentCalculator.getCLSteps().size()-1);
					cDAtThirdDescentStart = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtThirdDescentStart
							);
					cDAtThirdDescentEnding = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							cLAtThirdDescentEnding
							);
					thrustAtThirdDescentStart = theThirdDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
					thrustAtThirdDescentEnding = theThirdDescentCalculator.getThrustPerStep()
							.get(theThirdDescentCalculator.getThrustPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					dragAtThirdDescentStart = theThirdDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
					dragAtThirdDescentEnding = theThirdDescentCalculator.getDragPerStep()
							.get(theThirdDescentCalculator.getDragPerStep().size()-1)
							.to(NonSI.POUND_FORCE);
					rateOfClimbAtThirdDescentStart = theThirdDescentCalculator.getRateOfDescentList().get(0).to(MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtThirdDescentEnding = theThirdDescentCalculator.getRateOfDescentList()
							.get(theThirdDescentCalculator.getRateOfDescentList().size()-1)
							.to(MyUnits.FOOT_PER_MINUTE);
					climbAngleAtThirdDescentStart = theThirdDescentCalculator.getDescentAngles().get(0).to(NonSI.DEGREE_ANGLE);
					climbAngleAtThirdDescentEnding = theThirdDescentCalculator.getDescentAngles()
							.get(theThirdDescentCalculator.getDescentAngles().size()-1)
							.to(NonSI.DEGREE_ANGLE);
					fuelFlowAtThirdDescentStart = theThirdDescentCalculator.getInterpolatedFuelFlowList().get(0)*2.20462/0.016667;
					fuelFlowAtThirdDescentEnding = theThirdDescentCalculator.getInterpolatedFuelFlowList()
							.get(theThirdDescentCalculator.getInterpolatedFuelFlowList().size()-1)
							*2.20462/0.016667;
					sfcAtThirdDescentStart = fuelFlowAtThirdDescentStart/thrustAtThirdDescentStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtThirdDescentEnding = fuelFlowAtThirdDescentEnding/thrustAtThirdDescentEnding.doubleValue(NonSI.POUND_FORCE);

					//--------------------------------------------------------------------
					// LANDING
					aircraftMassAtLandingStart = 
							_initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM))
							.minus(fuelHolding.to(SI.KILOGRAM))
							.minus(fuelThirdDescent.to(SI.KILOGRAM));

					theLandingCalculator = new LandingCalcSemiempirical(
							_theAircraft, 
							_theOperatingConditions,
							aircraftMassAtLandingStart,
							_kApproach,
							_kFlare,
							_kTouchDown,
							_mu,
							_muBrake,
							wingToGroundDistance,
							_obstacleLanding, 
							_windSpeed,
							_alphaGround,
							_theAircraft.getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE),
							_thetaApproach,
							_cLmaxLanding,
							_cLZeroLanding,
							_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
							_theOperatingConditions.getThrottleGroundIdleLanding(),
							_freeRollDuration,
							_polarCLLanding,
							_polarCDLanding
							);

					theLandingCalculator.calculateLandingDistance();

					rangeLanding = theLandingCalculator.getsTotal().to(NonSI.NAUTICAL_MILE);			
					timeLanding = theLandingCalculator.getTime().get(theLandingCalculator.getTime().size()-1);
					fuelLanding = Amount.valueOf(
							timeLanding.to(NonSI.MINUTE).times(_landingFuelFlow).getEstimatedValue(),
							SI.KILOGRAM
							);
					aircraftMassAtLandingEnding = aircraftMassAtLandingStart.to(SI.KILOGRAM).minus(fuelLanding.to(SI.KILOGRAM));

					speedTASAtLandingStart = theLandingCalculator.getSpeed()
							.get(0)
							.to(NonSI.KNOT);
					speedCASAtLandingStart = theLandingCalculator.getSpeed()
							.get(0)
							.to(NonSI.KNOT)
							.times(
									Math.sqrt(
											AtmosphereCalc.getDensity(_obstacleLanding.doubleValue(SI.METER))
											/_theOperatingConditions.getDensityLanding().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
											)
									);
					speedTASAtLandingEnding = theLandingCalculator.getSpeed()
							.get(theLandingCalculator.getSpeed().size()-1)
							.to(NonSI.KNOT);
					speedCASAtLandingEnding = theLandingCalculator.getSpeed()
							.get(theLandingCalculator.getSpeed().size()-1)
							.to(NonSI.KNOT)
							.times(
									Math.sqrt(
											AtmosphereCalc.getDensity(_obstacleLanding.doubleValue(SI.METER))
											/_theOperatingConditions.getDensityLanding().doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER)
											)
									);
					cLAtLandingStart = LiftCalc.calculateLiftCoeff(
							aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue(),
							theLandingCalculator.getvA().doubleValue(SI.METERS_PER_SECOND),
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_obstacleLanding.doubleValue(SI.METER)
							);
					cLAtLandingEnding = theLandingCalculator.getcLground();
					cDAtLandingStart = 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_polarCLLanding),
									MyArrayUtils.convertToDoublePrimitive(_polarCDLanding),
									cLAtLandingStart
									);
					cDAtLandingEnding = 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(_polarCLLanding),
									MyArrayUtils.convertToDoublePrimitive(_polarCDLanding),
									cLAtLandingEnding
									);
					rateOfClimbAtLandingStart = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
					rateOfClimbAtLandingEnding = Amount.valueOf(0.0, MyUnits.FOOT_PER_MINUTE);
					climbAngleAtLandingStart = Amount.valueOf(-3.0, NonSI.DEGREE_ANGLE);
					climbAngleAtLandingEnding = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
					dragAtLandingStart = Amount.valueOf( 
							DragCalc.calculateDragAtSpeed(
									aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue(), 
									_obstacleLanding.doubleValue(SI.METER),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									theLandingCalculator.getvA().doubleValue(SI.METERS_PER_SECOND),
									cDAtLandingStart
									),
							SI.NEWTON
							).to(NonSI.POUND_FORCE);
					dragAtLandingEnding = theLandingCalculator.getDrag()
							.get(theLandingCalculator.getDrag().size()-1)
							.to(NonSI.POUND_FORCE);
					thrustAtLandingStart = Amount.valueOf(
							(climbAngleAtLandingStart.doubleValue(SI.RADIAN)
									*aircraftMassAtLandingStart.to(SI.KILOGRAM).times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND)).getEstimatedValue())
							+ (cDAtLandingStart/cLAtLandingStart),
							SI.NEWTON
							).to(NonSI.POUND_FORCE);
					thrustAtLandingEnding = theLandingCalculator.getThrust()
							.get(theLandingCalculator.getThrust().size()-1)
							.to(NonSI.POUND_FORCE);
					fuelFlowAtLandingStart = _landingFuelFlow*2.20462/0.016667;
					fuelFlowAtLandingEnding = _landingFuelFlow*2.20462/0.016667;
					sfcAtLandingStart = fuelFlowAtLandingStart/thrustAtLandingStart.doubleValue(NonSI.POUND_FORCE);
					sfcAtLandingEnding = fuelFlowAtLandingEnding/thrustAtLandingEnding.doubleValue(NonSI.POUND_FORCE);

					//.....................................................................
					// NEW ITERATION ALTERNATE CRUISE LENGTH
					rangeAlternateCruise = rangeAlternateCruise.to(NonSI.NAUTICAL_MILE).plus( 
							_alternateCruiseLength.to(NonSI.NAUTICAL_MILE)
							.minus(rangeSecondClimb.to(NonSI.NAUTICAL_MILE)
									.plus(rangeAlternateCruise.to(NonSI.NAUTICAL_MILE))
									.plus(rangeSecondDescent.to(NonSI.NAUTICAL_MILE))
									.plus(rangeHolding.to(NonSI.NAUTICAL_MILE))
									.plus(rangeThirdDescent.to(NonSI.NAUTICAL_MILE))
									)
							);
					if(rangeAlternateCruise.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
						_missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
						return;
					}
				}
				
				//.....................................................................
				// NEW ITERATION CRUISE LENGTH
				rangeCruise = rangeCruise.to(NonSI.NAUTICAL_MILE).plus( 
						_missionRange.to(NonSI.NAUTICAL_MILE)
						.minus(rangeTakeOff.to(NonSI.NAUTICAL_MILE)
								.plus(rangeClimb.to(NonSI.NAUTICAL_MILE))
								.plus(rangeCruise.to(NonSI.NAUTICAL_MILE))
								.plus(rangeFirstDescent.to(NonSI.NAUTICAL_MILE))
								.plus(rangeLanding.to(NonSI.NAUTICAL_MILE))
								)
						);
				if(rangeCruise.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
					_missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (NEW CRUISE LENGTH EVALUATION - MISSION PROFILE) THE NEW CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
					return;
				}
				
				//--------------------------------------------------------------------
				// ALTITUDE
				_altitudeList.clear();

				_altitudeList.add(Amount.valueOf(0.0, NonSI.FOOT));
				_altitudeList.add(_obstacleTakeOff.to(NonSI.FOOT));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(NonSI.FOOT));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_alternateCruiseAltitude.to(NonSI.FOOT));
				_altitudeList.add(_alternateCruiseAltitude.to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_holdingAltitude.to(NonSI.FOOT));
				_altitudeList.add(_obstacleLanding.to(NonSI.FOOT)); 
				_altitudeList.add(Amount.valueOf(0.0, SI.METER)); 

				//--------------------------------------------------------------------
				// RANGE
				_rangeList.clear();

				_rangeList.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
				_rangeList.add(_rangeList.get(0).plus(rangeTakeOff.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(1).plus(rangeClimb.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(2).plus(rangeCruise.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(3).plus(rangeFirstDescent.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(4).plus(rangeSecondClimb.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(5).plus(rangeAlternateCruise.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(6).plus(rangeSecondDescent.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(7).plus(rangeHolding.to(NonSI.NAUTICAL_MILE)));
				_rangeList.add(_rangeList.get(8).plus(rangeThirdDescent).to(NonSI.NAUTICAL_MILE));
				_rangeList.add(_rangeList.get(9).plus(rangeLanding).to(NonSI.NAUTICAL_MILE));

				_totalRange = _rangeList.get(_rangeList.size()-1);

				//--------------------------------------------------------------------
				// TIME
				_timeList.clear();

				_timeList.add(Amount.valueOf(0.0, NonSI.MINUTE));
				_timeList.add(_timeList.get(0).plus(timeTakeOff.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(1).plus(timeClimb.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(2).plus(timeCruise.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(3).plus(timeFirstDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(4).plus(timeSecondClimb.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(5).plus(timeAlternateCruise.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(6).plus(timeSecondDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(7).plus(timeHolding.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(8).plus(timeThirdDescent.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(9).plus(timeLanding.to(NonSI.MINUTE)));

				_totalTime = _timeList.get(_timeList.size()-1);
				_blockTime = timeTakeOff.to(NonSI.MINUTE)
						.plus(timeClimb.to(NonSI.MINUTE))
						.plus(timeCruise.to(NonSI.MINUTE))
						.plus(timeFirstDescent.to(NonSI.MINUTE))
						.plus(timeLanding.to(NonSI.MINUTE));

				//--------------------------------------------------------------------
				// USED FUEL
				_fuelUsedList.clear();

				_fuelUsedList.add(Amount.valueOf(0.0, SI.KILOGRAM));
				_fuelUsedList.add(_fuelUsedList.get(0).plus(fuelTakeOff.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(1).plus(fuelClimb.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(2).plus(fuelCruise.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(3).plus(fuelFirstDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(4).plus(fuelSecondClimb.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(5).plus(fuelAlternateCruise.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(6).plus(fuelSecondDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(7).plus(fuelHolding.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(8).plus(fuelThirdDescent.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(9).plus(fuelLanding.to(SI.KILOGRAM)));

				_totalFuel = _fuelUsedList.get(_fuelUsedList.size()-1);
				_blockFuel = fuelTakeOff.to(SI.KILOGRAM)
						.plus(fuelClimb.to(SI.KILOGRAM))
						.plus(fuelCruise.to(SI.KILOGRAM))
						.plus(fuelFirstDescent.to(SI.KILOGRAM))
						.plus(fuelLanding.to(SI.KILOGRAM));

				//--------------------------------------------------------------------
				// WEIGHT VARIATION
				_massList.clear();

				_massList.add(_initialMissionMass.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtTakeOffEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtClimbEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtCruiseEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtFirstDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtSecondClimbEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtAlternateCruiseEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtSecondDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtHoldingEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtThirdDescentEnding.to(SI.KILOGRAM));
				_massList.add(aircraftMassAtLandingEnding.to(SI.KILOGRAM));

				_endMissionMass = _massList.get(_massList.size()-1);
				
			} 
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			newInitialFuelMass = _totalFuel.to(SI.KILOGRAM).divide(1-_fuelReserve); 
			_initialMissionMass = _operatingEmptyMass
					.plus(_singlePassengerMass.times(_passengersNumber))
					.plus(newInitialFuelMass); 
			
			if(_initialMissionMass.doubleValue(SI.KILOGRAM) > _maximumTakeOffMass.doubleValue(SI.KILOGRAM)) {

				System.err.println("MAXIMUM TAKE-OFF MASS SURPASSED !! REDUCING PASSENGERS NUMBER TO INCREASE THE FUEL ... ");
				
				_passengersNumber += (int) Math.ceil(
						(_maximumTakeOffMass.minus(_initialMissionMass))
						.divide(_singlePassengerMass)
						.getEstimatedValue()
						)
						;
				_initialMissionMass = _maximumTakeOffMass;
				
			}
			
			i++;
			
		} while ( Math.abs(
				(_initialFuelMass.to(SI.KILOGRAM).minus(_totalFuel.to(SI.KILOGRAM)))
				.divide(_initialFuelMass.to(SI.KILOGRAM))
				.times(100)
				.getEstimatedValue()
				)- (_fuelReserve*100)
				>= 0.01
				);
		
		if(theFirstDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - FIRST DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theSecondDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - SECOND DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theThirdDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - THIRD DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		
		//----------------------------------------------------------------------
		// ITERATION ENDING ... collecting results
		//----------------------------------------------------------------------
		
		//......................................................................
		_speedCASMissionList.add(speedCASAtTakeOffStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtTakeOffEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtClimbStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtClimbEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtCruiseStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtCruiseEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtFirstDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtFirstDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondClimbStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondClimbEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtAlternateCruiseStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtAlternateCruiseEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtSecondDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtHoldingStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtHoldingEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtThirdDescentStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtThirdDescentEnding.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtLandingStart.to(NonSI.KNOT));
		_speedCASMissionList.add(speedCASAtLandingEnding.to(NonSI.KNOT));
		
		//......................................................................
		_speedTASMissionList.add(speedTASAtTakeOffStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtTakeOffEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtFirstDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtFirstDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtAlternateCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtAlternateCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtSecondDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtHoldingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtHoldingEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtThirdDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtThirdDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtLandingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedTASAtLandingEnding.to(NonSI.KNOT));
		
		//......................................................................
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(0).doubleValue(SI.METER),
						_speedTASMissionList.get(0).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(1).doubleValue(SI.METER),
						_speedTASMissionList.get(1).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						0.0,
						_speedTASMissionList.get(2).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(3).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(4).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(5).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
						_speedTASMissionList.get(6).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(7).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(8).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(9).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(10).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(11).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_alternateCruiseAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(12).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(13).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(14).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(15).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_holdingAltitude.doubleValue(SI.METER),
						_speedTASMissionList.get(16).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						Amount.valueOf(50, NonSI.FOOT).doubleValue(SI.METER),
						_speedTASMissionList.get(17).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						Amount.valueOf(50, NonSI.FOOT).doubleValue(SI.METER),
						_speedTASMissionList.get(18).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_machMissionList.add(
				SpeedCalc.calculateMach(
						_altitudeList.get(_altitudeList.size()-1).doubleValue(SI.METER),
						_speedTASMissionList.get(19).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		
		//......................................................................
		_liftingCoefficientMissionList.add(cLAtTakeOffStart);
		_liftingCoefficientMissionList.add(cLAtTakeOffEnding);
		_liftingCoefficientMissionList.add(cLAtClimbStart);
		_liftingCoefficientMissionList.add(cLAtClimbEnding);
		_liftingCoefficientMissionList.add(cLAtCruiseStart);
		_liftingCoefficientMissionList.add(cLAtCruiseEnding);
		_liftingCoefficientMissionList.add(cLAtFirstDescentStart);
		_liftingCoefficientMissionList.add(cLAtFirstDescentEnding);
		_liftingCoefficientMissionList.add(cLAtSecondClimbStart);
		_liftingCoefficientMissionList.add(cLAtSecondClimbEnding);
		_liftingCoefficientMissionList.add(cLAtAlternateCruiseStart);
		_liftingCoefficientMissionList.add(cLAtAlternateCruiseEnding);
		_liftingCoefficientMissionList.add(cLAtSecondDescentStart);
		_liftingCoefficientMissionList.add(cLAtSecondDescentEnding);
		_liftingCoefficientMissionList.add(cLAtHoldingStart);
		_liftingCoefficientMissionList.add(cLAtHoldingEnding);
		_liftingCoefficientMissionList.add(cLAtThirdDescentStart);
		_liftingCoefficientMissionList.add(cLAtThirdDescentEnding);
		_liftingCoefficientMissionList.add(cLAtLandingStart);
		_liftingCoefficientMissionList.add(cLAtLandingEnding);
		
		//......................................................................
		_dragCoefficientMissionList.add(cDAtTakeOffStart);
		_dragCoefficientMissionList.add(cDAtTakeOffEnding);
		_dragCoefficientMissionList.add(cDAtClimbStart);
		_dragCoefficientMissionList.add(cDAtClimbEnding);
		_dragCoefficientMissionList.add(cDAtCruiseStart);
		_dragCoefficientMissionList.add(cDAtCruiseEnding);
		_dragCoefficientMissionList.add(cDAtFirstDescentStart);
		_dragCoefficientMissionList.add(cDAtFirstDescentEnding);
		_dragCoefficientMissionList.add(cDAtSecondClimbStart);
		_dragCoefficientMissionList.add(cDAtSecondClimbEnding);
		_dragCoefficientMissionList.add(cDAtAlternateCruiseStart);
		_dragCoefficientMissionList.add(cDAtAlternateCruiseEnding);
		_dragCoefficientMissionList.add(cDAtSecondDescentStart);
		_dragCoefficientMissionList.add(cDAtSecondDescentEnding);
		_dragCoefficientMissionList.add(cDAtHoldingStart);
		_dragCoefficientMissionList.add(cDAtHoldingEnding);
		_dragCoefficientMissionList.add(cDAtThirdDescentStart);
		_dragCoefficientMissionList.add(cDAtThirdDescentEnding);
		_dragCoefficientMissionList.add(cDAtLandingStart);
		_dragCoefficientMissionList.add(cDAtLandingEnding);

		//......................................................................
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(0)/_dragCoefficientMissionList.get(0));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(1)/_dragCoefficientMissionList.get(1));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(2)/_dragCoefficientMissionList.get(2));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(3)/_dragCoefficientMissionList.get(3));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(4)/_dragCoefficientMissionList.get(4));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(5)/_dragCoefficientMissionList.get(5));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(6)/_dragCoefficientMissionList.get(6));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(7)/_dragCoefficientMissionList.get(7));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(8)/_dragCoefficientMissionList.get(8));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(9)/_dragCoefficientMissionList.get(9));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(10)/_dragCoefficientMissionList.get(10));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(11)/_dragCoefficientMissionList.get(11));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(12)/_dragCoefficientMissionList.get(12));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(13)/_dragCoefficientMissionList.get(13));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(14)/_dragCoefficientMissionList.get(14));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(15)/_dragCoefficientMissionList.get(15));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(16)/_dragCoefficientMissionList.get(16));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(17)/_dragCoefficientMissionList.get(17));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(18)/_dragCoefficientMissionList.get(18));
		_efficiencyMissionList.add(_liftingCoefficientMissionList.get(19)/_dragCoefficientMissionList.get(19));
		
		//......................................................................
		_throttleMissionList.add(throttleCruiseStart);
		_throttleMissionList.add(throttleCruiseEnding);
		_throttleMissionList.add(throttleAlternateCruiseStart);
		_throttleMissionList.add(throttleAlternateCruiseEnding);
		_throttleMissionList.add(throttleHoldingStart);
		_throttleMissionList.add(throttleHoldingEnding);
		
		//......................................................................
		_thrustMissionList.add(thrustAtTakeOffStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtTakeOffEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtClimbStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtClimbEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtCruiseStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtCruiseEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtFirstDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtFirstDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondClimbStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondClimbEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtAlternateCruiseStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtAlternateCruiseEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtSecondDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtHoldingStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtHoldingEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtThirdDescentStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtThirdDescentEnding.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtLandingStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtLandingEnding.to(NonSI.POUND_FORCE));
		
		//......................................................................
		_dragMissionList.add(dragAtTakeOffStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtTakeOffEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtClimbStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtClimbEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtCruiseStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtCruiseEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtFirstDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtFirstDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondClimbStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondClimbEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtAlternateCruiseStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtAlternateCruiseEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtSecondDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtHoldingStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtHoldingEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtThirdDescentStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtThirdDescentEnding.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtLandingStart.to(NonSI.POUND_FORCE));
		_dragMissionList.add(dragAtLandingEnding.to(NonSI.POUND_FORCE));
		
		//......................................................................
		_rateOfClimbMissionList.add(rateOfClimbAtTakeOffStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtTakeOffEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtClimbStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtClimbEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtCruiseStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtCruiseEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtFirstDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtFirstDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondClimbStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondClimbEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtAlternateCruiseStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtAlternateCruiseEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtSecondDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtHoldingStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtHoldingEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtThirdDescentStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtThirdDescentEnding.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtLandingStart.to(MyUnits.FOOT_PER_MINUTE));
		_rateOfClimbMissionList.add(rateOfClimbAtLandingEnding.to(MyUnits.FOOT_PER_MINUTE));
		
		//......................................................................
		_climbAngleMissionList.add(climbAngleAtTakeOffStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtTakeOffEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtClimbStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtClimbEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtCruiseStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtCruiseEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtFirstDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtFirstDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondClimbStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondClimbEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtAlternateCruiseStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtAlternateCruiseEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtSecondDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtHoldingStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtHoldingEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtThirdDescentStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtThirdDescentEnding.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtLandingStart.to(NonSI.DEGREE_ANGLE));
		_climbAngleMissionList.add(climbAngleAtLandingEnding.to(NonSI.DEGREE_ANGLE));
		
		//......................................................................
		// in (lb/hr)
		_fuelFlowMissionList.add(fuelFlowAtTakeOffStart);
		_fuelFlowMissionList.add(fuelFlowAtTakeOffEnding);
		_fuelFlowMissionList.add(fuelFlowAtClimbStart);
		_fuelFlowMissionList.add(fuelFlowAtClimbEnding);
		_fuelFlowMissionList.add(fuelFlowAtCruiseStart);
		_fuelFlowMissionList.add(fuelFlowAtCruiseEnding);
		_fuelFlowMissionList.add(fuelFlowAtFirstDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtFirstDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtSecondClimbStart);
		_fuelFlowMissionList.add(fuelFlowAtSecondClimbEnding);
		_fuelFlowMissionList.add(fuelFlowAtAlternateCruiseStart);
		_fuelFlowMissionList.add(fuelFlowAtAlternateCruiseEnding);
		_fuelFlowMissionList.add(fuelFlowAtSecondDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtSecondDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtHoldingStart);
		_fuelFlowMissionList.add(fuelFlowAtHoldingEnding);
		_fuelFlowMissionList.add(fuelFlowAtThirdDescentStart);
		_fuelFlowMissionList.add(fuelFlowAtThirdDescentEnding);
		_fuelFlowMissionList.add(fuelFlowAtLandingStart);
		_fuelFlowMissionList.add(fuelFlowAtLandingEnding);
		
		//......................................................................
		// in (lb/(lb*hr))
		_sfcMissionList.add(sfcAtTakeOffStart);
		_sfcMissionList.add(sfcAtTakeOffEnding);
		_sfcMissionList.add(sfcAtClimbStart);
		_sfcMissionList.add(sfcAtClimbEnding);
		_sfcMissionList.add(sfcAtCruiseStart);
		_sfcMissionList.add(sfcAtCruiseEnding);
		_sfcMissionList.add(sfcAtFirstDescentStart);
		_sfcMissionList.add(sfcAtFirstDescentEnding);
		_sfcMissionList.add(sfcAtSecondClimbStart);
		_sfcMissionList.add(sfcAtSecondClimbEnding);
		_sfcMissionList.add(sfcAtAlternateCruiseStart);
		_sfcMissionList.add(sfcAtAlternateCruiseEnding);
		_sfcMissionList.add(sfcAtSecondDescentStart);
		_sfcMissionList.add(sfcAtSecondDescentEnding);
		_sfcMissionList.add(sfcAtHoldingStart);
		_sfcMissionList.add(sfcAtHoldingEnding);
		_sfcMissionList.add(sfcAtThirdDescentStart);
		_sfcMissionList.add(sfcAtThirdDescentEnding);
		_sfcMissionList.add(sfcAtLandingStart);
		_sfcMissionList.add(sfcAtLandingEnding);
		
		_initialFuelMass = newInitialFuelMass;
	}

	public void plotProfiles(
			List<PerformancePlotEnum> _plotList,
			String _missionProfilesFolderPath) {
		
		if(_plotList.contains(PerformancePlotEnum.RANGE_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_rangeList.stream()
							.map(r -> r.to(SI.KILOMETER))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"km", "m",
					_missionProfilesFolderPath, "Range_Profile_SI",true
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_rangeList.stream()
							.map(r -> r.to(NonSI.NAUTICAL_MILE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"nmi", "ft",
					_missionProfilesFolderPath, "Range_Profile_IMPERIAL",true
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.TIME_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "m",
					_missionProfilesFolderPath, "Time_Profile_(min)_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "ft",
					_missionProfilesFolderPath, "Time_Profile_(min)_IMPERIAL",true
					);
			
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "m",
					_missionProfilesFolderPath, "Time_Profile_(hours)_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.HOUR))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "ft",
					_missionProfilesFolderPath, "Time_Profile_(hours)_IMPERIAL",true
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_USED_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_fuelUsedList.stream()
							.map(f -> f.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(SI.METER))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"kg", "m",
					_missionProfilesFolderPath, "Fuel_used_Profile_SI",true
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_fuelUsedList.stream()
							.map(f -> f.to(NonSI.POUND))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_altitudeList.stream()
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"lb", "ft",
					_missionProfilesFolderPath, "Fuel_used_Profile_IMPERIAL",true
					);
		}
		
		if(_plotList.contains(PerformancePlotEnum.WEIGHT_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_massList.stream()
							.map(m -> m.to(SI.KILOGRAM))
							.collect(Collectors.toList()
									)
							),
					0.0, null, null, null,
					"Time", "Aircraft mass",
					"min", "kg",
					_missionProfilesFolderPath, "Mass_Profile",true
					);
		}
		
	}
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\t\tMission distance = " + _missionRange.to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tTotal mission distance (plus alternate) = " + _totalRange.to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tBlock time = " + _blockTime + "\n")
				.append("\t\tTotal mission duration = " + _totalTime + "\n")
				.append("\t\tAircraft mass at mission start = " + _initialMissionMass + "\n")
				.append("\t\tAircraft mass at mission end = " + _endMissionMass + "\n")
				.append("\t\tInitial fuel mass for the assigned mission = " + _initialFuelMass + "\n")
				.append("\t\tBlock fuel = " + _blockFuel + "\n")
				.append("\t\tTotal fuel = " + _totalFuel + "\n")
				.append("\t\tFuel reserve = " + _fuelReserve*100 + " %\n")
				.append("\t\tDesign passengers number = " + _theAircraft.getCabinConfiguration().getDesignPassengerNumber() + "\n")
				.append("\t\tPassengers number for this mission = " + _passengersNumber + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off range = " + _rangeList.get(1).to(NonSI.NAUTICAL_MILE) + " \n")
				.append("\t\tClimb range = " + _rangeList.get(2).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(1).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tCruise range = " + _rangeList.get(3).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(2).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tFirst descent range = " + _rangeList.get(4).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(3).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tSecond climb range = " + _rangeList.get(5).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(4).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tAlternate cruise range = " + _rangeList.get(6).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(5).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tSecond descent range = " + _rangeList.get(7).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(6).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tHolding range = " + _rangeList.get(8).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(7).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tThird descent range = " + _rangeList.get(9).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(8).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\tLanding range = " + _rangeList.get(10).to(NonSI.NAUTICAL_MILE).minus(_rangeList.get(9).to(NonSI.NAUTICAL_MILE)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tAltitude at take-off ending = " + _altitudeList.get(1).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at climb ending = " + _altitudeList.get(2).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at cruise ending = " + _altitudeList.get(3).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at first descent ending = " + _altitudeList.get(4).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at second climb ending = " + _altitudeList.get(5).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at alternate cruise ending = " + _altitudeList.get(6).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at second descent ending = " + _altitudeList.get(7).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at holding ending = " + _altitudeList.get(8).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at third descent ending = " + _altitudeList.get(9).to(NonSI.FOOT) + " \n")
				.append("\t\tAltitude at landing ending = " + _altitudeList.get(10).to(NonSI.FOOT) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off duration = " + _timeList.get(1).to(NonSI.MINUTE) + " \n")
				.append("\t\tClimb duration = " + _timeList.get(2).to(NonSI.MINUTE).minus(_timeList.get(1).to(NonSI.MINUTE)) + " \n")
				.append("\t\tCruise duration = " + _timeList.get(3).to(NonSI.MINUTE).minus(_timeList.get(2).to(NonSI.MINUTE))+ " \n")
				.append("\t\tFirst descent duration = " + _timeList.get(4).to(NonSI.MINUTE).minus(_timeList.get(3).to(NonSI.MINUTE)) + " \n")
				.append("\t\tSecond climb duration = " + _timeList.get(5).to(NonSI.MINUTE).minus(_timeList.get(4).to(NonSI.MINUTE)) + " \n")
				.append("\t\tAlternate cruise duration = " + _timeList.get(6).to(NonSI.MINUTE).minus(_timeList.get(5).to(NonSI.MINUTE)) + " \n")
				.append("\t\tSecond descent duration = " + _timeList.get(7).to(NonSI.MINUTE).minus(_timeList.get(6).to(NonSI.MINUTE)) + " \n")
				.append("\t\tHolding duration = " + _timeList.get(8).to(NonSI.MINUTE).minus(_timeList.get(7).to(NonSI.MINUTE)) + " \n")
				.append("\t\tThird descent duration = " + _timeList.get(9).to(NonSI.MINUTE).minus(_timeList.get(8).to(NonSI.MINUTE)) + " \n")
				.append("\t\tLanding duration = " + _timeList.get(10).to(NonSI.MINUTE).minus(_timeList.get(9).to(NonSI.MINUTE)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off used fuel = " + _fuelUsedList.get(1).to(SI.KILOGRAM) + " \n")
				.append("\t\tClimb used fuel = " + _fuelUsedList.get(2).to(SI.KILOGRAM).minus(_fuelUsedList.get(1).to(SI.KILOGRAM)) + " \n")
				.append("\t\tCruise used fuel = " + _fuelUsedList.get(3).to(SI.KILOGRAM).minus(_fuelUsedList.get(2).to(SI.KILOGRAM)) + "\n")
				.append("\t\tFirst descent used fuel = " + _fuelUsedList.get(4).to(SI.KILOGRAM).minus(_fuelUsedList.get(3).to(SI.KILOGRAM)) + " \n")
				.append("\t\tSecond climb used fuel = " + _fuelUsedList.get(5).to(SI.KILOGRAM).minus(_fuelUsedList.get(4).to(SI.KILOGRAM)) + " \n")
				.append("\t\tAlternate cruise used fuel = " + _fuelUsedList.get(6).to(SI.KILOGRAM).minus(_fuelUsedList.get(5).to(SI.KILOGRAM)) + "\n")
				.append("\t\tSecond descent used fuel = " + _fuelUsedList.get(7).to(SI.KILOGRAM).minus(_fuelUsedList.get(6).to(SI.KILOGRAM)) + "\n")
				.append("\t\tHolding used fuel = " + _fuelUsedList.get(8).to(SI.KILOGRAM).minus(_fuelUsedList.get(7).to(SI.KILOGRAM)) + " \n")
				.append("\t\tThird descent used fuel = " + _fuelUsedList.get(9).to(SI.KILOGRAM).minus(_fuelUsedList.get(8).to(SI.KILOGRAM)) + " \n")
				.append("\t\tLanding used fuel = " + _fuelUsedList.get(10).to(SI.KILOGRAM).minus(_fuelUsedList.get(9).to(SI.KILOGRAM)) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tAircraft weight at take-off start  = " + _massList.get(1).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at climb start = " + _massList.get(2).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at cruise start = " + _massList.get(3).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at first descent start = " + _massList.get(4).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at second climb start = " + _massList.get(5).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at alternate cruise start = " + _massList.get(6).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at second descent start = " + _massList.get(7).to(SI.KILOGRAM) + "\n")
				.append("\t\tAircraft weight at holding start = " + _massList.get(8).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at third descent start = " + _massList.get(9).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at landing start = " + _massList.get(10).to(SI.KILOGRAM) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTAKE-OFF\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at take-off start  = " + _speedTASMissionList.get(0).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at take-off ending  = " + _speedTASMissionList.get(1).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at take-off start  = " + _speedCASMissionList.get(0).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at take-off ending  = " + _speedCASMissionList.get(1).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at take-off start  = " + _machMissionList.get(0) + " \n")
				.append("\t\tMach at take-off ending  = " + _machMissionList.get(1) + " \n")
				.append("\t\tCL at take-off start  = " + _liftingCoefficientMissionList.get(0) + " \n")
				.append("\t\tCL at take-off ending  = " + _liftingCoefficientMissionList.get(1) + " \n")
				.append("\t\tCD at take-off start  = " + _dragCoefficientMissionList.get(0) + " \n")
				.append("\t\tCD at take-off ending  = " + _dragCoefficientMissionList.get(1) + " \n")
				.append("\t\tEfficiency at take-off start  = " + _efficiencyMissionList.get(0) + " \n")
				.append("\t\tEfficiency at take-off ending  = " + _efficiencyMissionList.get(1) + " \n")
				.append("\t\tThrust at take-off start  = " + _thrustMissionList.get(0).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at take-off ending  = " + _thrustMissionList.get(1).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at take-off start  = " + _dragMissionList.get(0).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at take-off ending  = " + _dragMissionList.get(1).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at take-off start  = " + _rateOfClimbMissionList.get(0).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at take-off ending  = " + _rateOfClimbMissionList.get(1).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at take-off start  = " + _climbAngleMissionList.get(0).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at take-off ending  = " + _climbAngleMissionList.get(1).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at take-off start  = " + _fuelFlowMissionList.get(0) + " lb/hr\n")
				.append("\t\tFuel flow at take-off ending  = " + _fuelFlowMissionList.get(1) + " lb/hr\n")
				.append("\t\tSFC at take-off start  = " + _sfcMissionList.get(0) + " lb/(lb*hr)\n")
				.append("\t\tSFC at take-off ending  = " + _sfcMissionList.get(1) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tCLIMB\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at climb start  = " + _speedTASMissionList.get(2).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at climb ending  = " + _speedTASMissionList.get(3).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at climb start  = " + _speedCASMissionList.get(2).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at climb ending  = " + _speedCASMissionList.get(3).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at climb start  = " + _machMissionList.get(2) + " \n")
				.append("\t\tMach at climb ending  = " + _machMissionList.get(3) + " \n")
				.append("\t\tCL at climb start  = " + _liftingCoefficientMissionList.get(2) + " \n")
				.append("\t\tCL at climb ending  = " + _liftingCoefficientMissionList.get(3) + " \n")
				.append("\t\tCD at climb start  = " + _dragCoefficientMissionList.get(2) + " \n")
				.append("\t\tCD at climb ending  = " + _dragCoefficientMissionList.get(3) + " \n")
				.append("\t\tEfficiency at climb start  = " + _efficiencyMissionList.get(2) + " \n")
				.append("\t\tEfficiency at climb ending  = " + _efficiencyMissionList.get(3) + " \n")
				.append("\t\tThrust at climb start  = " + _thrustMissionList.get(2).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at climb ending  = " + _thrustMissionList.get(3).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at climb start  = " + _dragMissionList.get(2).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at climb ending  = " + _dragMissionList.get(3).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at climb start  = " + _rateOfClimbMissionList.get(2).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at climb ending  = " + _rateOfClimbMissionList.get(3).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at climb start  = " + _climbAngleMissionList.get(2).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at climb ending  = " + _climbAngleMissionList.get(3).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at climb start  = " + _fuelFlowMissionList.get(2) + " lb/hr\n")
				.append("\t\tFuel flow at climb ending  = " + _fuelFlowMissionList.get(3) + " lb/hr\n")
				.append("\t\tSFC at climb start  = " + _sfcMissionList.get(2) + " lb/(lb*hr)\n")
				.append("\t\tSFC at climb ending  = " + _sfcMissionList.get(3) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tCRUISE\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at cruise start  = " + _speedTASMissionList.get(4).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at cruise ending  = " + _speedTASMissionList.get(5).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at cruise start  = " + _speedCASMissionList.get(4).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at cruise ending  = " + _speedCASMissionList.get(5).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at cruise start  = " + _machMissionList.get(4) + " \n")
				.append("\t\tMach at cruise ending  = " + _machMissionList.get(5) + " \n")
				.append("\t\tCL at cruise start  = " + _liftingCoefficientMissionList.get(4) + " \n")
				.append("\t\tCL at cruise ending  = " + _liftingCoefficientMissionList.get(5) + " \n")
				.append("\t\tCD at cruise start  = " + _dragCoefficientMissionList.get(4) + " \n")
				.append("\t\tCD at cruise ending  = " + _dragCoefficientMissionList.get(5) + " \n")
				.append("\t\tEfficiency at cruise start  = " + _efficiencyMissionList.get(4) + " \n")
				.append("\t\tEfficiency at cruise ending  = " + _efficiencyMissionList.get(5) + " \n")
				.append("\t\tThrottle at cruise start  = " + _throttleMissionList.get(0) + " \n")
				.append("\t\tThrottle at cruise ending  = " + _throttleMissionList.get(1) + " \n")
				.append("\t\tThrust at cruise start  = " + _thrustMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at cruise ending  = " + _thrustMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise start  = " + _dragMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise ending  = " + _dragMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at cruise start  = " + _rateOfClimbMissionList.get(4).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at cruise ending  = " + _rateOfClimbMissionList.get(5).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at cruise start  = " + _climbAngleMissionList.get(4).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at cruise ending  = " + _climbAngleMissionList.get(5).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at cruise start  = " + _fuelFlowMissionList.get(4) + " lb/hr\n")
				.append("\t\tFuel flow at cruise ending  = " + _fuelFlowMissionList.get(5) + " lb/hr\n")
				.append("\t\tSFC at cruise start  = " + _sfcMissionList.get(4) + " lb/(lb*hr)\n")
				.append("\t\tSFC at cruise ending  = " + _sfcMissionList.get(5) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tFIRST DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at first descent start  = " + _speedTASMissionList.get(6).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at first descent ending  = " + _speedTASMissionList.get(7).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at first descent start  = " + _speedCASMissionList.get(6).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at first descent ending  = " + _speedCASMissionList.get(7).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at first descent start  = " + _machMissionList.get(6) + " \n")
				.append("\t\tMach at first descent ending  = " + _machMissionList.get(7) + " \n")
				.append("\t\tCL at first descent start  = " + _liftingCoefficientMissionList.get(6) + " \n")
				.append("\t\tCL at first descent ending  = " + _liftingCoefficientMissionList.get(7) + " \n")
				.append("\t\tCD at first descent start  = " + _dragCoefficientMissionList.get(6) + " \n")
				.append("\t\tCD at first descent ending  = " + _dragCoefficientMissionList.get(7) + " \n")
				.append("\t\tEfficiency at first descent start  = " + _efficiencyMissionList.get(6) + " \n")
				.append("\t\tEfficiency at first descent ending  = " + _efficiencyMissionList.get(7) + " \n")
				.append("\t\tThrust at first descent start  = " + _thrustMissionList.get(6).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at first descent ending  = " + _thrustMissionList.get(7).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at first descent start  = " + _dragMissionList.get(6).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at first descent ending  = " + _dragMissionList.get(7).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at first descent start  = " + _rateOfClimbMissionList.get(6).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at first descent ending  = " + _rateOfClimbMissionList.get(7).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at first descent start  = " + _climbAngleMissionList.get(6).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at first descent ending  = " + _climbAngleMissionList.get(7).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at first descent start  = " + _fuelFlowMissionList.get(6) + " lb/hr\n")
				.append("\t\tFuel flow at first descent ending  = " + _fuelFlowMissionList.get(7) + " lb/hr\n")
				.append("\t\tSFC at first descent start  = " + _sfcMissionList.get(6) + " lb/(lb*hr)\n")
				.append("\t\tSFC at first desecnt ending  = " + _sfcMissionList.get(7) + " lb/(lb*hr)\n");
		
		if(_alternateCruiseAltitude.doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
			sb.append("\t\t.....................................\n")
			.append("\t\tSECOND CLIMB\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second climb start  = " + _speedTASMissionList.get(8).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second climb ending  = " + _speedTASMissionList.get(9).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second climb start  = " + _speedCASMissionList.get(8).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second climb ending  = " + _speedCASMissionList.get(9).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at second climb start  = " + _machMissionList.get(8) + " \n")
			.append("\t\tMach at second climb ending  = " + _machMissionList.get(9) + " \n")
			.append("\t\tCL at second climb start  = " + _liftingCoefficientMissionList.get(8) + " \n")
			.append("\t\tCL at second climb ending  = " + _liftingCoefficientMissionList.get(9) + " \n")
			.append("\t\tCD at second climb start  = " + _dragCoefficientMissionList.get(8) + " \n")
			.append("\t\tCD at second climb ending  = " + _dragCoefficientMissionList.get(9) + " \n")
			.append("\t\tEfficiency at second climb start  = " + _efficiencyMissionList.get(8) + " \n")
			.append("\t\tEfficiency at second climb ending  = " + _efficiencyMissionList.get(9) + " \n")
			.append("\t\tThrust at second climb start  = " + _thrustMissionList.get(8).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at second climb ending  = " + _thrustMissionList.get(9).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second climb start  = " + _dragMissionList.get(8).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second climb ending  = " + _dragMissionList.get(9).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at second climb start  = " + _rateOfClimbMissionList.get(8).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at second climb ending  = " + _rateOfClimbMissionList.get(9).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at second climb start  = " + _climbAngleMissionList.get(8).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at second climb ending  = " + _climbAngleMissionList.get(9).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at second climb start  = " + _fuelFlowMissionList.get(8) + " lb/hr\n")
			.append("\t\tFuel flow at second climb ending  = " + _fuelFlowMissionList.get(9) + " lb/hr\n")
			.append("\t\tSFC at second climb start  = " + _sfcMissionList.get(8) + " lb/(lb*hr)\n")
			.append("\t\tSFC at second climb ending  = " + _sfcMissionList.get(9) + " lb/(lb*hr)\n")
			.append("\t\t.....................................\n")
			.append("\t\tALTERNATE CRUISE\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at alternate cruise start  = " + _speedTASMissionList.get(10).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at alternate cruise ending  = " + _speedTASMissionList.get(11).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at alternate cruise start  = " + _speedCASMissionList.get(10).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at alternate cruise ending  = " + _speedCASMissionList.get(11).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at alternate cruise start  = " + _machMissionList.get(10) + " \n")
			.append("\t\tMach at alternate cruise ending  = " + _machMissionList.get(11) + " \n")
			.append("\t\tCL at alternate cruise start  = " + _liftingCoefficientMissionList.get(10) + " \n")
			.append("\t\tCL at alternate cruise ending  = " + _liftingCoefficientMissionList.get(11) + " \n")
			.append("\t\tCD at alternate cruise start  = " + _dragCoefficientMissionList.get(10) + " \n")
			.append("\t\tCD at alternate cruise ending  = " + _dragCoefficientMissionList.get(11) + " \n")
			.append("\t\tEfficiency at alternate cruise start  = " + _efficiencyMissionList.get(10) + " \n")
			.append("\t\tEfficiency at alternate cruise ending  = " + _efficiencyMissionList.get(11) + " \n")
			.append("\t\tThrottle at alternate cruise start  = " + _throttleMissionList.get(2) + " \n")
			.append("\t\tThrottle at alternate cruise ending  = " + _throttleMissionList.get(3) + " \n")
			.append("\t\tThrust at alternate cruise start  = " + _thrustMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at alternate cruise ending  = " + _thrustMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise start  = " + _dragMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise ending  = " + _dragMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at alternate cruise start  = " + _rateOfClimbMissionList.get(10).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at alternate cruise ending  = " + _rateOfClimbMissionList.get(11).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at alternate cruise start  = " + _climbAngleMissionList.get(10).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at alternate cruise ending  = " + _climbAngleMissionList.get(11).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at alternate cruise start  = " + _fuelFlowMissionList.get(10) + " lb/hr\n")
			.append("\t\tFuel flow at alternate cruise ending  = " + _fuelFlowMissionList.get(11) + " lb/hr\n")
			.append("\t\tSFC at alternate cruise start  = " + _sfcMissionList.get(10) + " lb/(lb*hr)\n")
			.append("\t\tSFC at alternate cruise ending  = " + _sfcMissionList.get(11) + " lb/(lb*hr)\n")
			.append("\t\t.....................................\n")
			.append("\t\tSECOND DESCENT\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second descent start  = " + _speedTASMissionList.get(12).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second descent ending  = " + _speedTASMissionList.get(13).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second descent start  = " + _speedCASMissionList.get(12).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (CAS) at second descent ending  = " + _speedCASMissionList.get(13).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at second descent start  = " + _machMissionList.get(12) + " \n")
			.append("\t\tMach at second descent ending  = " + _machMissionList.get(13) + " \n")
			.append("\t\tCL at second descent start  = " + _liftingCoefficientMissionList.get(12) + " \n")
			.append("\t\tCL at second descent ending  = " + _liftingCoefficientMissionList.get(13) + " \n")
			.append("\t\tCD at second descent start  = " + _dragCoefficientMissionList.get(12) + " \n")
			.append("\t\tCD at second descent ending  = " + _dragCoefficientMissionList.get(13) + " \n")
			.append("\t\tEfficiency at second descent start  = " + _efficiencyMissionList.get(12) + " \n")
			.append("\t\tEfficiency at second descent ending  = " + _efficiencyMissionList.get(13) + " \n")
			.append("\t\tThrust at second descent start  = " + _thrustMissionList.get(12).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at second descent ending  = " + _thrustMissionList.get(13).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second descent start  = " + _dragMissionList.get(12).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at second descent ending  = " + _dragMissionList.get(13).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tRate of climb at second descent start = " + _rateOfClimbMissionList.get(12).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tRate of climb at second descent ending  = " + _rateOfClimbMissionList.get(13).to(MyUnits.FOOT_PER_MINUTE) + " \n")
			.append("\t\tClimb angle at second descent start  = " + _climbAngleMissionList.get(12).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tClimb angle at second descent ending  = " + _climbAngleMissionList.get(13).to(NonSI.DEGREE_ANGLE) + " \n")
			.append("\t\tFuel flow at second descent start  = " + _fuelFlowMissionList.get(12) + " lb/hr\n")
			.append("\t\tFuel flow at second descent ending  = " + _fuelFlowMissionList.get(13) + " lb/hr\n")
			.append("\t\tSFC at second descent start  = " + _sfcMissionList.get(12) + " lb/(lb*hr)\n")
			.append("\t\tSFC at second descent ending  = " + _sfcMissionList.get(13) + " lb/(lb*hr)\n");
		}
		if(_holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
				sb.append("\t\t.....................................\n")
				.append("\t\tHOLDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at holding start  = " + _speedTASMissionList.get(14).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at holding ending  = " + _speedTASMissionList.get(15).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at holding start  = " + _speedCASMissionList.get(14).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at holding ending  = " + _speedCASMissionList.get(15).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at holding start  = " + _machMissionList.get(14) + " \n")
				.append("\t\tMach at holding ending  = " + _machMissionList.get(15) + " \n")
				.append("\t\tCL at holding start  = " + _liftingCoefficientMissionList.get(14) + " \n")
				.append("\t\tCL at holding ending  = " + _liftingCoefficientMissionList.get(15) + " \n")
				.append("\t\tCD at holding start  = " + _dragCoefficientMissionList.get(14) + " \n")
				.append("\t\tCD at holding ending  = " + _dragCoefficientMissionList.get(15) + " \n")
				.append("\t\tEfficiency at holding start  = " + _efficiencyMissionList.get(14) + " \n")
				.append("\t\tEfficiency at holding ending  = " + _efficiencyMissionList.get(15) + " \n")
				.append("\t\tThrottle at holding start  = " + _throttleMissionList.get(4) + " \n")
				.append("\t\tThrottle at holding ending  = " + _throttleMissionList.get(5) + " \n")
				.append("\t\tThrust at holding start  = " + _thrustMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at holding ending  = " + _thrustMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding start  = " + _dragMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding ending  = " + _dragMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at holding start = " + _rateOfClimbMissionList.get(14).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at holding ending  = " + _rateOfClimbMissionList.get(15).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at holding start  = " + _climbAngleMissionList.get(14).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at holding ending  = " + _climbAngleMissionList.get(15).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at holding start  = " + _fuelFlowMissionList.get(14) + " lb/hr\n")
				.append("\t\tFuel flow at holding ending  = " + _fuelFlowMissionList.get(15) + " lb/hr\n")
				.append("\t\tSFC at holding start  = " + _sfcMissionList.get(14) + " lb/(lb*hr)\n")
				.append("\t\tSFC at holding ending  = " + _sfcMissionList.get(15) + " lb/(lb*hr)\n")
				.append("\t\t.....................................\n")
				.append("\t\tTHIRD DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at third descent start  = " + _speedTASMissionList.get(16).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at third descent ending  = " + _speedTASMissionList.get(17).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at third descent start  = " + _speedCASMissionList.get(16).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at third descent ending  = " + _speedCASMissionList.get(17).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at third descent start  = " + _machMissionList.get(16) + " \n")
				.append("\t\tMach at third descent ending  = " + _machMissionList.get(17) + " \n")
				.append("\t\tCL at third descent start  = " + _liftingCoefficientMissionList.get(16) + " \n")
				.append("\t\tCL at third descent ending  = " + _liftingCoefficientMissionList.get(17) + " \n")
				.append("\t\tCD at third descent start  = " + _dragCoefficientMissionList.get(16) + " \n")
				.append("\t\tCD at third descent ending  = " + _dragCoefficientMissionList.get(17) + " \n")
				.append("\t\tEfficiency at third descent start  = " + _efficiencyMissionList.get(16) + " \n")
				.append("\t\tEfficiency at third descent ending  = " + _efficiencyMissionList.get(17) + " \n")
				.append("\t\tThrust at third descent start  = " + _thrustMissionList.get(16).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at third descent ending  = " + _thrustMissionList.get(17).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at third descent start  = " + _dragMissionList.get(16).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at third descent ending  = " + _dragMissionList.get(17).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at third descent start = " + _rateOfClimbMissionList.get(16).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at third descent ending  = " + _rateOfClimbMissionList.get(17).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at third descent start  = " + _climbAngleMissionList.get(16).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at third descent ending  = " + _climbAngleMissionList.get(17).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at third descent start  = " + _fuelFlowMissionList.get(16) + " lb/hr\n")
				.append("\t\tFuel flow at third descent ending  = " + _fuelFlowMissionList.get(17) + " lb/hr\n")
				.append("\t\tSFC at third descent start  = " + _sfcMissionList.get(16) + " lb/(lb*hr)\n")
				.append("\t\tSFC at third descent ending  = " + _sfcMissionList.get(17) + " lb/(lb*hr)\n");
		}
		
				sb.append("\t\t.....................................\n")
				.append("\t\tLANDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at landing start  = " + _speedTASMissionList.get(18).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at landing ending  = " + _speedTASMissionList.get(19).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at landing start  = " + _speedCASMissionList.get(18).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (CAS) at landing ending  = " + _speedCASMissionList.get(19).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at landing start  = " + _machMissionList.get(18) + " \n")
				.append("\t\tMach at landing ending  = " + _machMissionList.get(19) + " \n")
				.append("\t\tCL at landing start  = " + _liftingCoefficientMissionList.get(18) + " \n")
				.append("\t\tCL at landing ending  = " + _liftingCoefficientMissionList.get(19) + " \n")
				.append("\t\tCD at landing start  = " + _dragCoefficientMissionList.get(18) + " \n")
				.append("\t\tCD at landing ending  = " + _dragCoefficientMissionList.get(19) + " \n")
				.append("\t\tEfficiency at landing start  = " + _efficiencyMissionList.get(18) + " \n")
				.append("\t\tEfficiency at landing ending  = " + _efficiencyMissionList.get(19) + " \n")
				.append("\t\tThrust at landing start  = " + _thrustMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at landing ending  = " + _thrustMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing start  = " + _dragMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing ending  = " + _dragMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tRate of climb at landing start = " + _rateOfClimbMissionList.get(18).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tRate of climb at landing ending  = " + _rateOfClimbMissionList.get(19).to(MyUnits.FOOT_PER_MINUTE) + " \n")
				.append("\t\tClimb angle at landing start  = " + _climbAngleMissionList.get(18).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tClimb angle at landing ending  = " + _climbAngleMissionList.get(19).to(NonSI.DEGREE_ANGLE) + " \n")
				.append("\t\tFuel flow at landing start  = " + _fuelFlowMissionList.get(18) + " lb/hr\n")
				.append("\t\tFuel flow at landing ending  = " + _fuelFlowMissionList.get(19) + " lb/hr\n")
				.append("\t\tSFC at landing start  = " + _sfcMissionList.get(18) + " lb/(lb*hr)\n")
				.append("\t\tSFC at landing ending  = " + _sfcMissionList.get(19) + " lb/(lb*hr)\n")
				.append("\t-------------------------------------\n")
				;
		
		return sb.toString();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}

	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}

	public Amount<Length> getAlternateCruiseLength() {
		return _alternateCruiseLength;
	}

	public void setAlternateCruiseLength(Amount<Length> _alternateCruiseLength) {
		this._alternateCruiseLength = _alternateCruiseLength;
	}

	public Amount<Length> getAlternateCruiseAltitude() {
		return _alternateCruiseAltitude;
	}

	public void setAlternateCruiseAltitude(Amount<Length> _alternateCruiseAltitude) {
		this._alternateCruiseAltitude = _alternateCruiseAltitude;
	}

	public Amount<Duration> getHoldingDuration() {
		return _holdingDuration;
	}

	public void setHoldingDuration(Amount<Duration> _holdingDuration) {
		this._holdingDuration = _holdingDuration;
	}

	public Amount<Length> getHoldingAltitude() {
		return _holdingAltitude;
	}

	public void setHoldingAltitude(Amount<Length> _holdingAltitude) {
		this._holdingAltitude = _holdingAltitude;
	}

	public double getFuelReserve() {
		return _fuelReserve;
	}

	public void setFuelReserve(double _fuelReserve) {
		this._fuelReserve = _fuelReserve;
	}

	public double getCLmaxClean() {
		return _cLmaxClean;
	}

	public void setCLmaxClean(double _cLmaxClean) {
		this._cLmaxClean = _cLmaxClean;
	}

	public double getCLmaxTakeOff() {
		return _cLmaxTakeOff;
	}

	public void setCLmaxTakeOff(double _cLmaxTakeOff) {
		this._cLmaxTakeOff = _cLmaxTakeOff;
	}

	public Amount<?> getCLAlphaTakeOff() {
		return _cLAlphaTakeOff;
	}

	public void setCLAlphaTakeOff(Amount<?> _cLAlphaTakeOff) {
		this._cLAlphaTakeOff = _cLAlphaTakeOff;
	}

	public double getCLZeroTakeOff() {
		return _cLZeroTakeOff;
	}

	public void setCLZeroTakeOff(double _cLZeroTakeOff) {
		this._cLZeroTakeOff = _cLZeroTakeOff;
	}

	public double getCLmaxLanding() {
		return _cLmaxLanding;
	}

	public void setCLmaxLanding(double _cLmaxLanding) {
		this._cLmaxLanding = _cLmaxLanding;
	}

	public double getCLZeroLanding() {
		return _cLZeroLanding;
	}

	public void setCLZeroLanding(double _cLZeroLanding) {
		this._cLZeroLanding = _cLZeroLanding;
	}

	public double[] getPolarCLClimb() {
		return _polarCLClimb;
	}

	public void setPolarCLClimb(double[] _polarCLClimb) {
		this._polarCLClimb = _polarCLClimb;
	}

	public double[] getPolarCDClimb() {
		return _polarCDClimb;
	}

	public void setPolarCDClimb(double[] _polarCDClimb) {
		this._polarCDClimb = _polarCDClimb;
	}

	public Amount<Velocity> getWindSpeed() {
		return _windSpeed;
	}

	public void setWindSpeed(Amount<Velocity> _windSpeed) {
		this._windSpeed = _windSpeed;
	}

	public MyInterpolatingFunction getMu() {
		return _mu;
	}

	public void setMu(MyInterpolatingFunction _mu) {
		this._mu = _mu;
	}

	public MyInterpolatingFunction getMuBrake() {
		return _muBrake;
	}

	public void setMuBrake(MyInterpolatingFunction _muBrake) {
		this._muBrake = _muBrake;
	}

	public Amount<Duration> getDtHold() {
		return _dtHold;
	}

	public void setDtHold(Amount<Duration> _dtHold) {
		this._dtHold = _dtHold;
	}

	public Amount<Angle> getAlphaGround() {
		return _alphaGround;
	}

	public void setAlphaGround(Amount<Angle> _alphaGround) {
		this._alphaGround = _alphaGround;
	}

	public Amount<Length> getObstacleTakeOff() {
		return _obstacleTakeOff;
	}

	public void setObstacleTakeOff(Amount<Length> _obstacleTakeOff) {
		this._obstacleTakeOff = _obstacleTakeOff;
	}

	public double getKRotation() {
		return _kRotation;
	}

	public void setKRotation(double _kRotation) {
		this._kRotation = _kRotation;
	}

	public double getKCLmaxTakeOff() {
		return _kCLmaxTakeOff;
	}

	public void setKCLmaxTakeOff(double _kCLmaxTakeOff) {
		this._kCLmaxTakeOff = _kCLmaxTakeOff;
	}
	
	public double getKCLmaxLanding() {
		return _kCLmaxTakeOff;
	}

	public void setKCLmaxLanding(double _kCLmaxLanding) {
		this._kCLmaxLanding = _kCLmaxLanding;
	}

	public double getDragDueToEnigneFailure() {
		return _dragDueToEnigneFailure;
	}

	public void setDragDueToEnigneFailure(double _dragDueToEnigneFailure) {
		this._dragDueToEnigneFailure = _dragDueToEnigneFailure;
	}

	public double getKAlphaDot() {
		return _kAlphaDot;
	}

	public void setKAlphaDot(double _kAlphaDot) {
		this._kAlphaDot = _kAlphaDot;
	}

	public Amount<Length> getObstacleLanding() {
		return _obstacleLanding;
	}

	public void setObstacleLanding(Amount<Length> _obstacleLanding) {
		this._obstacleLanding = _obstacleLanding;
	}

	public Amount<Angle> getApproachAngle() {
		return _approachAngle;
	}

	public void setApproachAngle(Amount<Angle> _thetaApproach) {
		this._approachAngle = _thetaApproach;
	}

	public double getKApproach() {
		return _kApproach;
	}

	public void setKApproach(double _kApproach) {
		this._kApproach = _kApproach;
	}

	public double getKFlare() {
		return _kFlare;
	}

	public void setKFlare(double _kFlare) {
		this._kFlare = _kFlare;
	}

	public double getKTouchDown() {
		return _kTouchDown;
	}

	public void setKTouchDown(double _kTouchDown) {
		this._kTouchDown = _kTouchDown;
	}

	public Amount<Duration> getFreeRollDuration() {
		return _freeRollDuration;
	}

	public void setFreeRollDuration(Amount<Duration> _freeRollDuration) {
		this._freeRollDuration = _freeRollDuration;
	}

	public Amount<Velocity> getClimbSpeed() {
		return _climbSpeed;
	}

	public void setClimbSpeed(Amount<Velocity> _climbSpeed) {
		this._climbSpeed = _climbSpeed;
	}

	public Amount<Velocity> getSpeedDescentCAS() {
		return _speedDescentCAS;
	}

	public void setSpeedDescentCAS(Amount<Velocity> _speedDescentCAS) {
		this._speedDescentCAS = _speedDescentCAS;
	}

	public Amount<Velocity> getRateOfDescent() {
		return _rateOfDescent;
	}

	public void setRateOfDescent(Amount<Velocity> _rateOfDescent) {
		this._rateOfDescent = _rateOfDescent;
	}

	public List<Amount<Length>> getAltitudeList() {
		return _altitudeList;
	}

	public void setAltitudeList(List<Amount<Length>> _altitudeList) {
		this._altitudeList = _altitudeList;
	}

	public List<Amount<Length>> getRangeList() {
		return _rangeList;
	}

	public void setRangeList(List<Amount<Length>> _rangeList) {
		this._rangeList = _rangeList;
	}

	public List<Amount<Duration>> getTimeList() {
		return _timeList;
	}

	public void setTimeList(List<Amount<Duration>> _timeList) {
		this._timeList = _timeList;
	}

	public List<Amount<Mass>> getFuelUsedList() {
		return _fuelUsedList;
	}

	public void setFuelUsedList(List<Amount<Mass>> _fuelUsedList) {
		this._fuelUsedList = _fuelUsedList;
	}

	public List<Amount<Mass>> getMassList() {
		return _massList;
	}

	public void setMassList(List<Amount<Mass>> _massList) {
		this._massList = _massList;
	}

	public Amount<Mass> getTotalFuel() {
		return _totalFuel;
	}

	public void setTotalFuel(Amount<Mass> _totalFuelUsed) {
		this._totalFuel= _totalFuelUsed;
	}
	
	public Amount<Mass> getBlockFuel() {
		return _blockFuel;
	}

	public void setBlockFuel(Amount<Mass> _blockFuel) {
		this._blockFuel= _blockFuel;
	}

	public Amount<Duration> getTotalTime() {
		return _totalTime;
	}

	public void setTotalTime(Amount<Duration> _totalMissionTime) {
		this._totalTime = _totalMissionTime;
	}

	public Amount<Duration> getBlockTime() {
		return _blockTime;
	}

	public void setBlockTime(Amount<Duration> _blockTime) {
		this._blockTime = _blockTime;
	}
	
	public Amount<Length> getTotalRange() {
		return _totalRange;
	}

	public void setTotalRange(Amount<Length> _totalMissionRange) {
		this._totalRange = _totalMissionRange;
	}

	public Amount<Mass> getEndMissionMass() {
		return _endMissionMass;
	}

	public void setEndMissionMass(Amount<Mass> _endMissionMass) {
		this._endMissionMass = _endMissionMass;
	}

	public Amount<Length> getTakeOffMissionAltitude() {
		return _takeOffMissionAltitude;
	}

	public void setTakeOffMissionAltitude(Amount<Length> _takeOffMissionAltitude) {
		this._takeOffMissionAltitude = _takeOffMissionAltitude;
	}

	public double getHoldingMachNumber() {
		return _holdingMachNumber;
	}

	public void setHoldingMachNumber(double _holdingMachNumber) {
		this._holdingMachNumber = _holdingMachNumber;
	}

	public double getLandingFuelFlow() {
		return _landingFuelFlow;
	}

	public void setLandingFuelFlow(double _landingFuelFlow) {
		this._landingFuelFlow = _landingFuelFlow;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}

	public Amount<Mass> getSinglePassengerMass() {
		return _singlePassengerMass;
	}

	public void setSinglePassengerMass(Amount<Mass> _singlePassengerMass) {
		this._singlePassengerMass = _singlePassengerMass;
	}

	public Amount<Mass> getFirstGuessInitialFuelMass() {
		return _firstGuessInitialFuelMass;
	}

	public void setFirstGuessInitialFuelMass(Amount<Mass> _firstGuessInitialFuelMass) {
		this._firstGuessInitialFuelMass = _firstGuessInitialFuelMass;
	}

	public Amount<Mass> getInitialMissionMass() {
		return _initialMissionMass;
	}

	public void setInitialMissionMass(Amount<Mass> _initialMissionMass) {
		this._initialMissionMass = _initialMissionMass;
	}

	public Amount<Mass> getInitialFuelMass() {
		return _initialFuelMass;
	}

	public void setInitialFuelMass(Amount<Mass> _initialFuelMass) {
		this._initialFuelMass = _initialFuelMass;
	}

	public Integer getPassengersNumber() {
		return _passengersNumber;
	}

	public void setPassengersNumber(Integer _passengersNumber) {
		this._passengersNumber = _passengersNumber;
	}

	public Amount<Length> getMissionRange() {
		return _missionRange;
	}

	public void setMissionRange(Amount<Length> _missionRange) {
		this._missionRange = _missionRange;
	}

	public double[] getPolarCLCruise() {
		return _polarCLCruise;
	}

	public void setPolarCLCruise(double[] _polarCLCruise) {
		this._polarCLCruise = _polarCLCruise;
	}

	public double[] getPolarCDCruise() {
		return _polarCDCruise;
	}

	public void setPolarCDCruise(double[] _polarCDCruise) {
		this._polarCDCruise = _polarCDCruise;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}

	public MyInterpolatingFunction getSFCFunctionCruise() {
		return _sfcFunctionCruise;
	}

	public void setSFCFunctionCruise(MyInterpolatingFunction _sfcFunctionCruise) {
		this._sfcFunctionCruise = _sfcFunctionCruise;
	}

	public MyInterpolatingFunction getSFCFunctionAlternateCruise() {
		return _sfcFunctionAlternateCruise;
	}

	public void setSFCFunctionAlternateCruise(MyInterpolatingFunction _sfcFunctionAlternateCruise) {
		this._sfcFunctionAlternateCruise = _sfcFunctionAlternateCruise;
	}

	public MyInterpolatingFunction getSFCFunctionHolding() {
		return _sfcFunctionHolding;
	}

	public void setSFCFunctionHolding(MyInterpolatingFunction _sfcFunctionHolding) {
		this._sfcFunctionHolding = _sfcFunctionHolding;
	}

	public List<Amount<Velocity>> getSpeedTASMissionList() {
		return _speedTASMissionList;
	}

	public void setSpeedTASMissionList(List<Amount<Velocity>> _speedTASMissionList) {
		this._speedTASMissionList = _speedTASMissionList;
	}

	public List<Amount<Velocity>> getSpeedCASMissionList() {
		return _speedCASMissionList;
	}

	public void setSpeedCASMissionList(List<Amount<Velocity>> _speedCASMissionList) {
		this._speedCASMissionList = _speedCASMissionList;
	}

	public List<Double> getMachMissionList() {
		return _machMissionList;
	}

	public void setMachMissionList(List<Double> _machMissionList) {
		this._machMissionList = _machMissionList;
	}

	public List<Double> getLiftingCoefficientMissionList() {
		return _liftingCoefficientMissionList;
	}

	public void setLiftingCoefficientMissionList(List<Double> _liftingCoefficientMissionList) {
		this._liftingCoefficientMissionList = _liftingCoefficientMissionList;
	}

	public List<Double> getDragCoefficientMissionList() {
		return _dragCoefficientMissionList;
	}

	public void setDragCoefficientMissionList(List<Double> _dragCoefficientMissionList) {
		this._dragCoefficientMissionList = _dragCoefficientMissionList;
	}

	public List<Double> getEfficiencyMissionList() {
		return _efficiencyMissionList;
	}

	public void setEfficiencyMissionList(List<Double> _efficiencyMissionList) {
		this._efficiencyMissionList = _efficiencyMissionList;
	}

	public List<Amount<Force>> getThrustMissionList() {
		return _thrustMissionList;
	}

	public void setThrustMissionList(List<Amount<Force>> _thrustMissionList) {
		this._thrustMissionList = _thrustMissionList;
	}

	public List<Amount<Force>> getDragMissionList() {
		return _dragMissionList;
	}

	public void setDragMissionList(List<Amount<Force>> _dragMissionList) {
		this._dragMissionList = _dragMissionList;
	}

	public double[] getPolarCLTakeOff() {
		return _polarCLTakeOff;
	}

	public void setPolarCLTakeOff(double[] _polarCLTakeOff) {
		this._polarCLTakeOff = _polarCLTakeOff;
	}

	public double[] getPolarCDTakeOff() {
		return _polarCDTakeOff;
	}

	public void setPolarCDTakeOff(double[] _polarCDTakeOff) {
		this._polarCDTakeOff = _polarCDTakeOff;
	}

	public double[] getPolarCLLanding() {
		return _polarCLLanding;
	}

	public void setPolarCLLanding(double[] _polarCLLanding) {
		this._polarCLLanding = _polarCLLanding;
	}

	public double[] getPolarCDLanding() {
		return _polarCDLanding;
	}

	public void setPolarCDLanding(double[] _polarCDLanding) {
		this._polarCDLanding = _polarCDLanding;
	}

	public boolean isCalculateSFCCruise() {
		return _calculateSFCCruise;
	}

	public void setCalculateSFCCruise(boolean calculateSFCCruise) {
		this._calculateSFCCruise = calculateSFCCruise;
	}

	public boolean isCalculateSFCAlternateCruise() {
		return _calculateSFCAlternateCruise;
	}

	public void setCalculateSFCAlternateCruise(boolean calculateSFCAlternateCruise) {
		this._calculateSFCAlternateCruise = calculateSFCAlternateCruise;
	}

	public boolean isCalculateSFCHolding() {
		return _calculateSFCHolding;
	}

	public void setCalculateSFCHolding(boolean calculateSFCHolding) {
		this._calculateSFCHolding = calculateSFCHolding;
	}

	public Boolean getMissionProfileStopped() {
		return _missionProfileStopped;
	}

	public void setMissionProfileStopped(Boolean missionProfileStopped) {
		this._missionProfileStopped = missionProfileStopped;
	}

	public List<Amount<Velocity>> getRateOfClimbMissionList() {
		return _rateOfClimbMissionList;
	}

	public void setRateOfClimbMissionList(List<Amount<Velocity>> _rateOfClimbMissionList) {
		this._rateOfClimbMissionList = _rateOfClimbMissionList;
	}

	public List<Amount<Angle>> getClimbAngleMissionList() {
		return _climbAngleMissionList;
	}

	public void setClimbAngleMissionList(List<Amount<Angle>> _climbAngleMissionList) {
		this._climbAngleMissionList = _climbAngleMissionList;
	}

	public List<Double> getFuelFlowMissionList() {
		return _fuelFlowMissionList;
	}

	public void setFuelFlowMissionList(List<Double> _fuelFlowMissionList) {
		this._fuelFlowMissionList = _fuelFlowMissionList;
	}

	public List<Double> getSFCMissionList() {
		return _sfcMissionList;
	}

	public void setSFCMissionList(List<Double> _sfcMissionList) {
		this._sfcMissionList = _sfcMissionList;
	}

	public List<Double> getThrottleMissionList() {
		return _throttleMissionList;
	}

	public void setThrottleMissionList(List<Double> _throttleMissionList) {
		this._throttleMissionList = _throttleMissionList;
	}

}
