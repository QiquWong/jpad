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

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
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
	private Integer _passengersNumber;
	private Amount<Mass> _firstGuessInitialFuelMass;
	private Amount<Length> _missionRange;
	private Amount<Length> _firstGuessCruiseLength;
	private MyInterpolatingFunction _sfcFunctionCruise;
	private MyInterpolatingFunction _sfcFunctionAlternateCruise;
	private MyInterpolatingFunction _sfcFunctionHolding;
	private Amount<Length> _alternateCruiseLength;
	private Amount<Length> _alternateCruiseAltitude;
	private Amount<Duration> _holdingDuration;
	private Amount<Length> _holdingAltitude;
	private Double _holdingMachNumber;
	private Double _landingFuelFlow;
	private Double _fuelReserve;
	private Double _cLmaxClean;
	private Double _cLmaxTakeOff;
	private Amount<?> _cLAlphaTakeOff;
	private Double _cLZeroTakeOff;
	private Double _cLmaxLanding;
	private Double _cLZeroLanding;
	private Double[] _polarCLTakeOff;
	private Double[] _polarCDTakeOff;
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
	private Double[] _polarCLLanding;
	private Double[] _polarCDLanding;
	private Amount<Velocity> _windSpeed;
	private MyInterpolatingFunction _mu;
	private MyInterpolatingFunction _muBrake;
	private Amount<Duration> _dtRotation;
	private Amount<Duration> _dtHold;
	private Amount<Angle> _alphaGround;
	private Amount<Length> _obstacleTakeOff;
	private Double _kRotation;
	private Double _kLiftOff;
	private Double _kCLmax;
	private Double _dragDueToEnigneFailure;
	private Double _kAlphaDot;
	private Amount<Length> _obstacleLanding;
	private Amount<Angle> _thetaApproach;
	private Double _kApproach;
	private Double _kFlare;
	private Double _kTouchDown;
	private Amount<Duration> _freeRollDuration;
	private Amount<Velocity> _climbSpeed;
	private Amount<Velocity> _speedDescentCAS;
	private Amount<Velocity> _rateOfDescent;
	
	//............................................................................................
	// Output:
	private List<Amount<Length>> _altitudeList;
	private List<Amount<Length>> _rangeList;
	private List<Amount<Duration>> _timeList;
	private List<Amount<Mass>> _fuelUsedList;
	private List<Amount<Mass>> _massList;
	private List<Amount<Velocity>> _speedTASMissionList;
	private List<Double> _machMissionList;
	private List<Double> _liftingCoefficientMissionList;
	private List<Double> _dragCoefficientMissionList;
	private List<Double> _efficiencyMissionList;
	private List<Amount<Force>> _thrustMissionList;
	private List<Amount<Force>> _dragMissionList;
	
	private Amount<Mass> _initialFuelMass;
	private Amount<Mass> _totalFuelUsed;
	private Amount<Duration> _totalMissionTime;
	private Amount<Length> _totalMissionRange;
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
			MyInterpolatingFunction sfcFunctionCruise,
			MyInterpolatingFunction sfcFunctionAlternateCruise,
			MyInterpolatingFunction sfcFunctionHolding,
			Amount<Length> alternateCruiseLength,
			Amount<Length> alternateCruiseAltitude,
			Amount<Duration> holdingDuration,
			Amount<Length> holdingAltitude,
			Double holdingMachNumber,
			Double landingFuelFlow,
			Double fuelReserve,
			Double cLmaxClean,
			Double cLmaxTakeOff,
			Amount<?> cLAlphaTakeOff,
			Double cLZeroTakeOff,
			Double cLmaxLanding,
			Double cLZeroLanding,
			Double[] polarCLTakeOff,
			Double[] polarCDTakeOff,
			Double[] polarCLClimb,
			Double[] polarCDClimb,
			Double[] polarCLCruise,
			Double[] polarCDCruise,
			Double[] polarCLLanding,
			Double[] polarCDLanding,
			Amount<Velocity> windSpeed,
			MyInterpolatingFunction mu,
			MyInterpolatingFunction muBrake,
			Amount<Duration> dtRotation,
			Amount<Duration> dtHold,
			Amount<Angle> alphaGround,
			Amount<Length> obstacleTakeOff,
			Double kRotation,
			Double kLiftOff,
			Double kCLmax,
			Double dragDueToEnigneFailure,
			Double kAlphaDot,
			Amount<Length> obstacleLanding,
			Amount<Angle> thetaApproach,
			Double kApproach,
			Double kFlare,
			Double kTouchDown,
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
		this._firstGuessCruiseLength = firstGuessCruiseLength;
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
		this._dtRotation = dtRotation;
		this._dtHold = dtHold;
		this._alphaGround = alphaGround;
		this._obstacleTakeOff = obstacleTakeOff;
		this._kRotation = kRotation;
		this._kLiftOff = kLiftOff;
		this._kCLmax = kCLmax;
		this._dragDueToEnigneFailure = dragDueToEnigneFailure;
		this._kAlphaDot = kAlphaDot;
		this._obstacleLanding = obstacleLanding;
		this._thetaApproach = thetaApproach;
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
		this._speedTASMissionList = new ArrayList<>();
		this._machMissionList = new ArrayList<>();
		this._liftingCoefficientMissionList = new ArrayList<>();
		this._dragCoefficientMissionList = new ArrayList<>();
		this._efficiencyMissionList = new ArrayList<>();
		this._thrustMissionList = new ArrayList<>();
		this._dragMissionList = new ArrayList<>();
				
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateProfiles() {

		_initialMissionMass = _operatingEmptyMass
				.plus(_singlePassengerMass.times(_passengersNumber))
				.plus(_firstGuessInitialFuelMass); 
		
		_initialFuelMass = _firstGuessInitialFuelMass;
		
		//----------------------------------------------------------------------
		// QUANTITES TO BE ADDED IN LISTS AT THE END OF THE ITERATION
		//----------------------------------------------------------------------
		// TAKE-OFF
		Amount<Velocity> speedAtTakeOffEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtTakeOffStart = 0.0;
		Double cLAtTakeOffEnding = 0.0;
		Double cDAtTakeOffStart = 0.0;
		Double cDAtTakeOffEnding = 0.0;
		Amount<Force> thrustAtTakeOffStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtTakeOffEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtTakeOffEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// CLIMB
		Amount<Velocity> speedAtClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtClimbStart = 0.0;
		Double cLAtClimbEnding = 0.0;
		Amount<Force> thrustAtClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// CRUISE
		Amount<Velocity> speedAtCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtCruiseStart = 0.0;
		Double cLAtCruiseEnding = 0.0;
		Amount<Force> thrustAtCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// FIRST DESCENT
		Amount<Velocity> speedAtFirstDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtFirstDescentStart = 0.0;
		Double cLAtFirstDescentEnding = 0.0;
		Amount<Force> thrustAtFirstDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtFirstDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtFirstDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// SECOND CLIMB
		Amount<Velocity> speedAtSecondClimbStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtSecondClimbStart = 0.0;
		Double cLAtSecondClimbEnding = 0.0;
		Amount<Force> thrustAtSecondClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondClimbStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondClimbEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// ALTERNATE CRUISE
		Amount<Velocity> speedAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtAlternateCruiseStart = 0.0;
		Double cLAtAlternateCruiseEnding = 0.0;
		Amount<Force> thrustAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtAlternateCruiseStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtAlternateCruiseEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// SECOND DESCENT
		Amount<Velocity> speedAtSecondDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtSecondDescentStart = 0.0;
		Double cLAtSecondDescentEnding = 0.0;
		Amount<Force> thrustAtSecondDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtSecondDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// HOLDING
		Amount<Velocity> speedAtHoldingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtHoldingEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtHoldingStart = 0.0;
		Double cLAtHoldingEnding = 0.0;
		Amount<Force> thrustAtHoldingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtHoldingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtHoldingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtHoldingEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// THIRD DESCENT
		Amount<Velocity> speedAtThirdDescentStart = Amount.valueOf(0.0, NonSI.KNOT);
		Amount<Velocity> speedAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtThirdDescentStart = 0.0;
		Double cLAtThirdDescentEnding = 0.0;
		Amount<Force> thrustAtThirdDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtThirdDescentStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtThirdDescentEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		//......................................................................
		// LANDING
		Amount<Velocity> speedAtLandingStart = Amount.valueOf(0.0, NonSI.KNOT);
		Double cLAtLandingStart = 0.0;
		Double cLAtLandingEnding = 0.0;
		Double cDAtLandingStart = 0.0;
		Double cDAtLandingEnding = 0.0;
		Amount<Force> thrustAtLandingGroundRollStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> thrustAtLandingGroundRollEnding = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		Amount<Force> dragAtLandingStart = Amount.valueOf(0.0, NonSI.POUND_FORCE);
		
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
		_totalFuelUsed = Amount.valueOf(0.0, SI.KILOGRAM);
		int i = 0;
		
		while (
				(Math.abs(
						(_initialFuelMass.to(SI.KILOGRAM).minus(_totalFuelUsed.to(SI.KILOGRAM)))
						.divide(_initialFuelMass.to(SI.KILOGRAM))
						.times(100)
						.getEstimatedValue()
						)- (_fuelReserve*100))
				>= 0.01
				) {
			
			if(i >= 1)
				_initialFuelMass = newInitialFuelMass;
			
			if(i > 100) {
				System.err.println("\t\nMAXIMUM NUMBER OF ITERATION REACHED :: MISSION PROFILE");
				break;
			}
			
			//--------------------------------------------------------------------
			// TAKE-OFF
			Amount<Length> wingToGroundDistance = 
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(
											Math.sin(
													_theAircraft.getWing()	
													.getLiftingSurfaceCreator()	
													.getDihedralMean()
													.doubleValue(SI.RADIAN)
													)
											)
									)
							);

			TakeOffCalc theTakeOffCalculator = new TakeOffCalc(
					_theAircraft.getWing().getAspectRatio(),
					_theAircraft.getWing().getSurface(),
					_theAircraft.getPowerPlant(),
					_polarCLTakeOff,
					_polarCDTakeOff,
					_takeOffMissionAltitude.to(SI.METER),
					_theOperatingConditions.getMachTakeOff(),
					_initialMissionMass,
					_dtRotation,
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
					_obstacleTakeOff,
					_windSpeed,
					_alphaGround,
					_theAircraft.getWing().getRiggingAngle(),
					_cLmaxTakeOff,
					_cLZeroTakeOff,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
					);

			theTakeOffCalculator.calculateTakeOffDistanceODE(null, false, false);

			Amount<Length> groundRollDistanceTakeOff = theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0);
			Amount<Length> rotationDistanceTakeOff = 
					theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1)
					.minus(groundRollDistanceTakeOff);
			Amount<Length> airborneDistanceTakeOff = 
					theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2)
					.minus(rotationDistanceTakeOff)
					.minus(groundRollDistanceTakeOff);
			Amount<Length> takeOffDistanceAOE = 
					groundRollDistanceTakeOff
					.plus(rotationDistanceTakeOff)
					.plus(airborneDistanceTakeOff);			
			Amount<Duration> takeOffDuration = theTakeOffCalculator.getTakeOffResults().getTime().get(2);

			Amount<Mass> takeOffUsedFuel = Amount.valueOf(
					MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									theTakeOffCalculator.getTime().stream()
									.map(t -> t.to(NonSI.MINUTE))
									.collect(Collectors.toList()
											)
									),
							MyArrayUtils.convertToDoublePrimitive(theTakeOffCalculator.getSfc())
							),
					SI.KILOGRAM					
					);
					
			speedAtTakeOffEnding = theTakeOffCalculator.getSpeed()
					.get(theTakeOffCalculator.getSpeed().size()-1)
					.to(NonSI.KNOT);
			cLAtTakeOffStart = theTakeOffCalculator.getcLground();
			cLAtTakeOffEnding = theTakeOffCalculator.getcL().get(theTakeOffCalculator.getcL().size()-1);
			cDAtTakeOffStart = theTakeOffCalculator.getcD().get(0);
			cDAtTakeOffEnding = theTakeOffCalculator.getcD().get(theTakeOffCalculator.getcD().size()-1);
			thrustAtTakeOffStart = theTakeOffCalculator.getThrust().get(0).to(NonSI.POUND_FORCE);
			thrustAtTakeOffEnding = theTakeOffCalculator.getThrust()
					.get(theTakeOffCalculator.getThrust().size()-1)
					.to(NonSI.POUND_FORCE);
			dragAtTakeOffEnding = theTakeOffCalculator.getDrag()
					.get(theTakeOffCalculator.getDrag().size()-1)
					.to(NonSI.POUND_FORCE);
			
			//--------------------------------------------------------------------
			// CLIMB
			ClimbCalc theClimbCalculator = new ClimbCalc(
					_theAircraft,
					_theOperatingConditions,
					_cLmaxClean, 
					_polarCLClimb,
					_polarCDClimb,
					_climbSpeed, 
					_dragDueToEnigneFailure 
					);

			Amount<Mass> intialClimbMass = _initialMissionMass.minus(takeOffUsedFuel);

			theClimbCalculator.calculateClimbPerformance(
					intialClimbMass,
					intialClimbMass,
					Amount.valueOf(0.0, SI.METER),
					_theOperatingConditions.getAltitudeCruise().to(SI.METER),
					false
					);

			Amount<Length> totalClimbRange = theClimbCalculator.getClimbTotalRange();
			Amount<Duration> totalClimbTime = null;
			if(_climbSpeed != null)
				totalClimbTime = theClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
			else
				totalClimbTime = theClimbCalculator.getMinimumClimbTimeAEO();
			Amount<Mass> totalClimbFuelUsed = theClimbCalculator.getClimbTotalFuelUsed();

			speedAtClimbStart = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
					.divide(
							Math.sqrt(
									OperatingConditions.getAtmosphere(0.0)
									.getDensityRatio()
									)
							);
			speedAtClimbEnding = theClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
					.divide(
							Math.sqrt(
									OperatingConditions.getAtmosphere(
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
											)
									.getDensityRatio()
									)
							);
			cLAtClimbStart = LiftCalc.calculateLiftCoeff(
					intialClimbMass.times(AtmosphereCalc.g0).getEstimatedValue(),
					speedAtClimbStart.doubleValue(SI.METERS_PER_SECOND),					
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					0.0
					);
			cLAtClimbEnding = LiftCalc.calculateLiftCoeff(
					intialClimbMass
					.minus(totalClimbFuelUsed)
					.times(AtmosphereCalc.g0)
					.getEstimatedValue(),
					speedAtClimbStart.doubleValue(SI.METERS_PER_SECOND),					
					_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
					_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
					);
			thrustAtClimbStart = theClimbCalculator.getThrustAtClimbStart().to(NonSI.POUND_FORCE);
			thrustAtClimbEnding = theClimbCalculator.getThrustAtClimbEnding().to(NonSI.POUND_FORCE);
			dragAtClimbStart = theClimbCalculator.getDragAtClimbStart().to(NonSI.POUND_FORCE);
			dragAtClimbEnding = theClimbCalculator.getDragAtClimbEnding().to(NonSI.POUND_FORCE);
			
			//--------------------------------------------------------------------
			// CRUISE
			
			Amount<Mass> intialCruiseMass = 
					_initialMissionMass
					.minus(takeOffUsedFuel)
					.minus(totalClimbFuelUsed);
			
			Amount<Length> cruiseLength = _firstGuessCruiseLength;
			_totalMissionRange = Amount.valueOf(0.0, SI.METER);
			
			while (
					Math.abs(
							(_missionRange.to(NonSI.NAUTICAL_MILE)
									.plus(_alternateCruiseLength.to(NonSI.NAUTICAL_MILE))
							.minus(_totalMissionRange.to(NonSI.NAUTICAL_MILE)))
							.divide(_missionRange.to(NonSI.NAUTICAL_MILE)
									.plus(_alternateCruiseLength.to(NonSI.NAUTICAL_MILE)))
							.getEstimatedValue()
							*100
							) 
					>= 0.001
					) {
				
				double[] cruiseSteps = MyArrayUtils.linspace(
						0.0,
						cruiseLength.doubleValue(SI.METER),
						5
						);
				
				Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(_theAircraft.getWing()));
				
				int nPointSpeed = 1000;
				double[] speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								(intialCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise)
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
								(intialCruiseMass
									.times(AtmosphereCalc.g0)
									.getEstimatedValue()
									),
								speedArray,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
				
				List<ThrustMap> thrustList = new ArrayList<>();
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_theOperatingConditions.getThrottleCruise(),
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
								(intialCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								dragList,
								thrustList
								)
						);
				
				List<Double> cruiseMissionMachNumber = new ArrayList<>();
				cruiseMissionMachNumber.add(intersectionList.get(0).getMaxMach());
				
				List<Amount<Velocity>> cruiseSpeedList = new ArrayList<>();
				cruiseSpeedList.add(
						Amount.valueOf(
								intersectionList.get(0).getMaxSpeed(),
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
				
				List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
				aircraftMassPerStep.add(intialCruiseMass);
				
				List<Double> cLSteps = new ArrayList<>();
				cLSteps.add(
						LiftCalc.calculateLiftCoeff(
								aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								cruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
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
										_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
										cruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLSteps.get(0))
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
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
									_theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
									meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
									meanAirfoil.getAirfoilCreator().getType()
									)
							);
					
					thrustList.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
									_theOperatingConditions.getThrottleCruise(),
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
									_theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLCruise),
									dragList,
									thrustList
									)
							);
					
					cruiseMissionMachNumber.add(intersectionList.get(j).getMaxMach());
					
					cruiseSpeedList.add(
							Amount.valueOf(
									intersectionList.get(j).getMaxSpeed(),
									SI.METERS_PER_SECOND
									).to(NonSI.KNOT)
							);
					
					cLSteps.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									cruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
									)
							);
					dragPerStep.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											cruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLSteps.get(j))
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
				
				Amount<Mass> totalCruiseFuelUsed =
						Amount.valueOf(
								fuelUsedPerStep.stream()
								.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
								.sum(),
								SI.KILOGRAM
								); 
				
				Amount<Duration> cruiseTime =
						Amount.valueOf(
								times.stream()
								.mapToDouble( t -> t.doubleValue(NonSI.MINUTE))
								.sum(),
								NonSI.MINUTE
								); 

				speedAtCruiseStart = cruiseSpeedList.get(0).to(NonSI.KNOT);
				speedAtCruiseEnding = cruiseSpeedList.get(cruiseSpeedList.size()-1).to(NonSI.KNOT);
				cLAtCruiseStart = cLSteps.get(0);
				cLAtCruiseEnding = cLSteps.get(cLSteps.size()-1);
				dragAtCruiseStart = dragPerStep.get(0).to(NonSI.POUND_FORCE);
				dragAtCruiseEnding = dragPerStep.get(dragPerStep.size()-1).to(NonSI.POUND_FORCE);
				thrustAtCruiseStart = dragAtCruiseStart;
				thrustAtCruiseEnding = dragAtCruiseEnding;
				
				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				Amount<Mass> intialFirstDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed);

				DescentCalc theFirstDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_theOperatingConditions.getAltitudeCruise().to(SI.METER),
						_holdingAltitude.to(SI.METER),
						intialFirstDescentMass,
						_polarCLClimb,
						_polarCDClimb
						);


				theFirstDescentCalculator.calculateDescentPerformance();
				Amount<Length> firstDescentLength = theFirstDescentCalculator.getTotalDescentLength();
				Amount<Duration> firstDescentTime = theFirstDescentCalculator.getTotalDescentTime();
				Amount<Mass> firstDescentFuelUsed = theFirstDescentCalculator.getTotalDescentFuelUsed();

				speedAtFirstDescentStart = theFirstDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
				speedAtFirstDescentEnding = theFirstDescentCalculator.getSpeedListTAS()
						.get(theFirstDescentCalculator.getSpeedListTAS().size()-1)
						.to(NonSI.KNOT);
				cLAtFirstDescentStart = theFirstDescentCalculator.getCLSteps().get(0);
				cLAtFirstDescentEnding = theFirstDescentCalculator.getCLSteps().get(theFirstDescentCalculator.getCLSteps().size()-1);
				thrustAtFirstDescentStart = theFirstDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
				thrustAtFirstDescentEnding = theFirstDescentCalculator.getThrustPerStep()
						.get(theFirstDescentCalculator.getThrustPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				dragAtFirstDescentStart = theFirstDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
				dragAtFirstDescentEnding = theFirstDescentCalculator.getDragPerStep()
						.get(theFirstDescentCalculator.getDragPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				
				//--------------------------------------------------------------------
				// SECOND CLIMB (up to ALTERNATE altitude)
				ClimbCalc theSecondClimbCalculator = new ClimbCalc(
						_theAircraft,
						_theOperatingConditions,
						_cLmaxClean, 
						_polarCLClimb,
						_polarCDClimb,
						_climbSpeed, 
						_dragDueToEnigneFailure 
						);

				Amount<Mass> intialSecondClimbMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed);

				theSecondClimbCalculator.calculateClimbPerformance(
						intialSecondClimbMass,
						intialSecondClimbMass,
						_holdingAltitude.to(SI.METER),
						_alternateCruiseAltitude.to(SI.METER),
						false
						);

				Amount<Length> totalSecondClimbRange = theSecondClimbCalculator.getClimbTotalRange();
				Amount<Duration> totalSecondClimbTime = null;
				if(_climbSpeed != null)
					totalSecondClimbTime = theSecondClimbCalculator.getClimbTimeAtSpecificClimbSpeedAEO();
				else
					totalSecondClimbTime = theSecondClimbCalculator.getMinimumClimbTimeAEO();
				Amount<Mass> totalSecondClimbFuelUsed = theSecondClimbCalculator.getClimbTotalFuelUsed();
				
				if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
					speedAtSecondClimbStart = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(
													_holdingAltitude.doubleValue(SI.METER)
													)
											.getDensityRatio()
											)
									);
					speedAtSecondClimbEnding = theSecondClimbCalculator.getClimbSpeed().to(NonSI.KNOT)
							.divide(
									Math.sqrt(
											OperatingConditions.getAtmosphere(
													_alternateCruiseAltitude.doubleValue(SI.METER)
													)
											.getDensityRatio()
											)
									);
					cLAtSecondClimbStart = LiftCalc.calculateLiftCoeff(
							intialSecondClimbMass.times(AtmosphereCalc.g0).getEstimatedValue(),
							speedAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							_holdingAltitude.doubleValue(SI.METER)
							);
					cLAtSecondClimbEnding = LiftCalc.calculateLiftCoeff(
							intialSecondClimbMass
							.minus(totalSecondClimbFuelUsed)
							.times(AtmosphereCalc.g0)
							.getEstimatedValue(),
							speedAtSecondClimbStart.doubleValue(SI.METERS_PER_SECOND),					
							_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							_alternateCruiseAltitude.doubleValue(SI.METER)
							);
					thrustAtSecondClimbStart = theSecondClimbCalculator.getThrustAtClimbStart().to(NonSI.POUND_FORCE);
					thrustAtSecondClimbEnding = theSecondClimbCalculator.getThrustAtClimbEnding().to(NonSI.POUND_FORCE);
					dragAtSecondClimbStart = theSecondClimbCalculator.getDragAtClimbStart().to(NonSI.POUND_FORCE);
					dragAtSecondClimbEnding = theSecondClimbCalculator.getDragAtClimbEnding().to(NonSI.POUND_FORCE);
				}
				//--------------------------------------------------------------------
				// ALTERNATE CRUISE
				Amount<Mass> intialAlternateCruiseMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed);
				
				double[] speedArrayAlternate = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_alternateCruiseAltitude.doubleValue(SI.METER),
								(intialAlternateCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise)
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
								(intialAlternateCruiseMass
									.times(AtmosphereCalc.g0)
									.getEstimatedValue()
									),
								speedArrayAlternate,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
								MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
								_theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
				
				List<ThrustMap> thrustListAlternate = new ArrayList<>();
				thrustListAlternate.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_alternateCruiseAltitude.doubleValue(SI.METER),
								_theOperatingConditions.getThrottleCruise(),
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
								(intialAlternateCruiseMass
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								MyArrayUtils.getMax(_polarCLCruise),
								dragListAlternate,
								thrustListAlternate
								)
						);
				
				List<Double> alternateCruiseMachNumberList = new ArrayList<>();
				alternateCruiseMachNumberList.add(intersectionListAlternate.get(0).getMaxMach());
				
				List<Amount<Velocity>> alternateCruiseSpeedList = new ArrayList<>();
				alternateCruiseSpeedList.add(
						Amount.valueOf(
								intersectionListAlternate.get(0).getMaxSpeed(),
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
				
				double[] alternateCruiseSteps = MyArrayUtils.linspace(
						0.0,
						_alternateCruiseLength.doubleValue(SI.METER),
						5
						);
				
				List<Amount<Mass>> aircraftMassPerStepAlternateCruise = new ArrayList<>();
				aircraftMassPerStepAlternateCruise.add(intialAlternateCruiseMass);
				
				List<Double> cLStepsAlternateCruise = new ArrayList<>();
				cLStepsAlternateCruise.add(
						LiftCalc.calculateLiftCoeff(
								aircraftMassPerStepAlternateCruise.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
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
										_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
										alternateCruiseSpeedList.get(0).doubleValue(SI.METERS_PER_SECOND),
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLStepsAlternateCruise.get(0))
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
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
									MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
									_theAircraft.getWing().getSweepHalfChordEquivalent().doubleValue(SI.RADIAN),
									meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
									meanAirfoil.getAirfoilCreator().getType()
									)
							);
					
					thrustListAlternate.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									_alternateCruiseAltitude.doubleValue(SI.METER),
									_theOperatingConditions.getThrottleCruise(),
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
									_theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									MyArrayUtils.getMax(_polarCLCruise),
									dragListAlternate,
									thrustListAlternate
									)
							);
					
					alternateCruiseMachNumberList.add(intersectionListAlternate.get(j).getMaxMach());
					
					alternateCruiseSpeedList.add(
							Amount.valueOf(
									intersectionListAlternate.get(j).getMaxSpeed(),
									SI.METERS_PER_SECOND
									).to(NonSI.KNOT)
							);
					
					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_alternateCruiseAltitude.doubleValue(SI.METER)
									)
							);
					dragPerStepAlternateCruise.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_alternateCruiseAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											alternateCruiseSpeedList.get(j).doubleValue(SI.METERS_PER_SECOND),
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLStepsAlternateCruise.get(j))
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
				
				Amount<Mass> totalAlternateCruiseFuelUsed =
						Amount.valueOf(
								fuelUsedPerStepAlternateCruise.stream()
								.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
								.sum(),
								SI.KILOGRAM
								); 
				
				Amount<Duration> alternateCruiseTime =
						Amount.valueOf(
								timesAlternateCruise.stream()
								.mapToDouble( t -> t.doubleValue(NonSI.MINUTE))
								.sum(),
								NonSI.MINUTE
								);

				if (_alternateCruiseAltitude.doubleValue(SI.METER) != 15.24) {
					speedAtAlternateCruiseStart =
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											alternateCruiseMachNumberList.get(0),
											_alternateCruiseAltitude.doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).to(NonSI.KNOT);
					speedAtAlternateCruiseEnding = 
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											alternateCruiseMachNumberList.get(0),
											_alternateCruiseAltitude.doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).to(NonSI.KNOT);
					cLAtAlternateCruiseStart = cLStepsAlternateCruise.get(0);
					cLAtAlternateCruiseEnding = cLStepsAlternateCruise.get(cLStepsAlternateCruise.size()-1);
					dragAtAlternateCruiseStart = dragPerStepAlternateCruise.get(0).to(NonSI.POUND_FORCE);
					dragAtAlternateCruiseEnding = dragPerStepAlternateCruise.get(dragPerStepAlternateCruise.size()-1).to(NonSI.POUND_FORCE);
					thrustAtAlternateCruiseStart = dragAtAlternateCruiseStart;
					thrustAtAlternateCruiseEnding = dragAtAlternateCruiseEnding;
				}
				
				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				Amount<Mass> intialSecondDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed)
						.minus(totalAlternateCruiseFuelUsed);

				DescentCalc theSecondDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_alternateCruiseAltitude.to(SI.METER),
						_holdingAltitude.to(SI.METER),
						intialSecondDescentMass,
						_polarCLClimb,
						_polarCDClimb
						);

				theSecondDescentCalculator.calculateDescentPerformance();
				Amount<Length> secondDescentLength = theSecondDescentCalculator.getTotalDescentLength();
				Amount<Duration> secondDescentTime = theSecondDescentCalculator.getTotalDescentTime();
				Amount<Mass> secondDescentFuelUsed = theSecondDescentCalculator.getTotalDescentFuelUsed();

				speedAtSecondDescentStart = theSecondDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
				speedAtSecondDescentEnding = theSecondDescentCalculator.getSpeedListTAS()
						.get(theSecondDescentCalculator.getSpeedListTAS().size()-1)
						.to(NonSI.KNOT);
				cLAtSecondDescentStart = theSecondDescentCalculator.getCLSteps().get(0);
				cLAtSecondDescentEnding = theSecondDescentCalculator.getCLSteps().get(theSecondDescentCalculator.getCLSteps().size()-1);
				thrustAtSecondDescentStart = theSecondDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
				thrustAtSecondDescentEnding = theSecondDescentCalculator.getThrustPerStep()
						.get(theSecondDescentCalculator.getThrustPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				dragAtSecondDescentStart = theSecondDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
				dragAtSecondDescentEnding = theSecondDescentCalculator.getDragPerStep()
						.get(theSecondDescentCalculator.getDragPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				
				//--------------------------------------------------------------------
				// HOLDING
				Amount<Mass> intialHoldingMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed)
						.minus(totalAlternateCruiseFuelUsed)
						.minus(secondDescentFuelUsed);

				List<Amount<Mass>> aircraftMassListHolding = new ArrayList<>();
				List<Double> cLListHolding = new ArrayList<>();
				List<Amount<Force>> dragListHolding = new ArrayList<>();
				List<Double> phiListHolding = new ArrayList<>();
				List<Double> fuelFlowListHolding = new ArrayList<>();
				List<Amount<Mass>> fuelUsedPerStepHolding = new ArrayList<>();
				
				double[] timeHolding = MyArrayUtils.linspace(
						0.0,
						_holdingDuration.doubleValue(NonSI.MINUTE),
						5
						);
				
				double speedTASHolding = SpeedCalc.calculateTAS(
						_holdingMachNumber,
						_holdingAltitude.doubleValue(SI.METER)
						);
				
				aircraftMassListHolding.add(intialHoldingMass);
				
				cLListHolding.add(
						LiftCalc.calculateLiftCoeff(
								aircraftMassListHolding.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
								speedTASHolding,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_holdingAltitude.doubleValue(SI.METER)
								)				
						);
				dragListHolding.add(
						Amount.valueOf(
								DragCalc.calculateDragAtSpeed(
										aircraftMassListHolding.get(0).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										_holdingAltitude.doubleValue(SI.METER),
										_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
										speedTASHolding,
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
												MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
												cLListHolding.get(0))
										),
								SI.NEWTON
								)
						);
				phiListHolding.add(
						dragListHolding.get(0).to(SI.NEWTON)
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
										_holdingMachNumber
										)
								)
						.getEstimatedValue()
						);
				if(phiListHolding.get(0) < 1.0) {
					fuelFlowListHolding.add(
							dragListHolding.get(0).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*_sfcFunctionHolding.value(phiListHolding.get(0))
							);
				}
				fuelUsedPerStepHolding.add(
						Amount.valueOf(
								fuelFlowListHolding.get(0)
								*(timeHolding[1]-timeHolding[0]),
								SI.KILOGRAM
								)
						);
				
				for(int j=1; j<timeHolding.length-1; j++) {
					
					aircraftMassListHolding.add(
							aircraftMassListHolding.get(j-1)
							.minus(fuelUsedPerStepHolding.get(j-1))
							);
					
					cLListHolding.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassListHolding.get(j).times(AtmosphereCalc.g0).getEstimatedValue(),
									speedTASHolding,
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_holdingAltitude.doubleValue(SI.METER)
									)				
							);
					dragListHolding.add(
							Amount.valueOf(
									DragCalc.calculateDragAtSpeed(
											aircraftMassListHolding.get(j).doubleValue(SI.KILOGRAM)
												*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											_holdingAltitude.doubleValue(SI.METER),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											speedTASHolding,
											MyMathUtils.getInterpolatedValue1DLinear(
													MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
													MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
													cLListHolding.get(j))
											),
									SI.NEWTON
									)
							);
					phiListHolding.add(
							dragListHolding.get(j).to(SI.NEWTON)
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
											_holdingMachNumber
											)
									)
							.getEstimatedValue()
							);
					if(phiListHolding.get(j) < 1.0) {
						fuelFlowListHolding.add(
								dragListHolding.get(j).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionHolding.value(phiListHolding.get(j))
								);
					}
					fuelUsedPerStepHolding.add(
							Amount.valueOf(
									fuelFlowListHolding.get(j)
									*(timeHolding[j+1]-timeHolding[j]),
									SI.KILOGRAM
									)
							);
					
				}
				
				Amount<Mass> totalHoldingFuelUsed = 
						Amount.valueOf(
								fuelUsedPerStepHolding.stream()
								.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
								.sum(),
								SI.KILOGRAM
								); 

				speedAtHoldingStart = 
						Amount.valueOf(
								SpeedCalc.calculateTAS(
										_holdingMachNumber,
										_holdingAltitude.doubleValue(SI.METER)
										),
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT);
				speedAtHoldingEnding = 
						Amount.valueOf(
								SpeedCalc.calculateTAS(
										_holdingMachNumber,
										_holdingAltitude.doubleValue(SI.METER)
										),
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT);
				cLAtHoldingStart = cLListHolding.get(0);
				cLAtHoldingEnding = cLListHolding.get(cLListHolding.size()-1);
				dragAtHoldingStart = dragListHolding.get(0).to(NonSI.POUND_FORCE);
				dragAtHoldingEnding = dragListHolding.get(dragListHolding.size()-1).to(NonSI.POUND_FORCE);
				thrustAtHoldingStart = dragAtHoldingStart;
				thrustAtHoldingEnding = dragAtHoldingEnding;
				
				//--------------------------------------------------------------------
				// DESCENT (up to LANDING altitude)
				Amount<Mass> intialThirdDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed)
						.minus(totalAlternateCruiseFuelUsed)
						.minus(secondDescentFuelUsed)
						.minus(totalHoldingFuelUsed);

				DescentCalc theThirdDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_holdingAltitude.to(SI.METER),
						Amount.valueOf(15.24, SI.METER),
						intialThirdDescentMass,
						_polarCLClimb,
						_polarCDClimb
						);

				theThirdDescentCalculator.calculateDescentPerformance();
				Amount<Length> thirdDescentLength = theThirdDescentCalculator.getTotalDescentLength();
				Amount<Duration> thirdDescentTime = theThirdDescentCalculator.getTotalDescentTime();
				Amount<Mass> thirdDescentFuelUsed = theThirdDescentCalculator.getTotalDescentFuelUsed();

				speedAtThirdDescentStart = theThirdDescentCalculator.getSpeedListTAS().get(0).to(NonSI.KNOT);
				speedAtThirdDescentEnding = theThirdDescentCalculator.getSpeedListTAS()
						.get(theThirdDescentCalculator.getSpeedListTAS().size()-1)
						.to(NonSI.KNOT);
				cLAtThirdDescentStart = theThirdDescentCalculator.getCLSteps().get(0);
				cLAtThirdDescentEnding = theThirdDescentCalculator.getCLSteps().get(theThirdDescentCalculator.getCLSteps().size()-1);
				thrustAtThirdDescentStart = theThirdDescentCalculator.getThrustPerStep().get(0).to(NonSI.POUND_FORCE);
				thrustAtThirdDescentEnding = theThirdDescentCalculator.getThrustPerStep()
						.get(theThirdDescentCalculator.getThrustPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				dragAtThirdDescentStart = theThirdDescentCalculator.getDragPerStep().get(0).to(NonSI.POUND_FORCE);
				dragAtThirdDescentEnding = theThirdDescentCalculator.getDragPerStep()
						.get(theThirdDescentCalculator.getDragPerStep().size()-1)
						.to(NonSI.POUND_FORCE);
				
				//--------------------------------------------------------------------
				// LANDING
				Amount<Mass> intialLandingMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed)
						.minus(totalAlternateCruiseFuelUsed)
						.minus(secondDescentFuelUsed)
						.minus(totalHoldingFuelUsed)
						.minus(thirdDescentFuelUsed);

				LandingCalc theLandingCalculator = new LandingCalc(
						_theAircraft, 
						_theOperatingConditions,
						intialLandingMass,
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

				Amount<Length> landingDistance = theLandingCalculator.getsTotal();
				Amount<Duration> landingDuration = theLandingCalculator.getTime().get(theLandingCalculator.getTime().size()-1);
				Amount<Mass> landingFuelUsed = 
						Amount.valueOf(
								landingDuration.to(NonSI.MINUTE).times(_landingFuelFlow).getEstimatedValue(),
								SI.KILOGRAM
								);

				speedAtLandingStart = theLandingCalculator.getvA().to(NonSI.KNOT);
				cLAtLandingStart = LiftCalc.calculateLiftCoeff(
						intialLandingMass.times(AtmosphereCalc.g0).getEstimatedValue(),
						speedAtLandingStart.doubleValue(SI.METERS_PER_SECOND),
						_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
						15.24
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
				thrustAtLandingGroundRollStart = theLandingCalculator.getThrust().get(0).to(NonSI.POUND_FORCE);
				thrustAtLandingGroundRollEnding = theLandingCalculator.getThrust()
						.get(theLandingCalculator.getThrust().size()-1)
						.to(NonSI.POUND_FORCE);
				dragAtLandingStart = theLandingCalculator.getDrag().get(0).to(NonSI.POUND_FORCE);

				//--------------------------------------------------------------------
				// ALTITUDE
				_altitudeList.clear();
				
				_altitudeList.add(Amount.valueOf(0.0, SI.METER));
				_altitudeList.add(theTakeOffCalculator.getObstacle().to(SI.METER));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(SI.METER));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(SI.METER));
				_altitudeList.add(_holdingAltitude.to(SI.METER));
				_altitudeList.add(_alternateCruiseAltitude.to(SI.METER));
				_altitudeList.add(_alternateCruiseAltitude.to(SI.METER));
				_altitudeList.add(_holdingAltitude.to(SI.METER));
				_altitudeList.add(_holdingAltitude.to(SI.METER));
				_altitudeList.add(Amount.valueOf(15.24, SI.METER)); // landing obstacle
				_altitudeList.add(Amount.valueOf(0.0, SI.METER)); 

				//--------------------------------------------------------------------
				// RANGE
				_rangeList.clear();
				
				_rangeList.add(Amount.valueOf(0.0, SI.KILOMETER));
				_rangeList.add(_rangeList.get(0).plus(takeOffDistanceAOE.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(1).plus(totalClimbRange.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(2).plus(cruiseLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(3).plus(firstDescentLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(4).plus(totalSecondClimbRange.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(5).plus(_alternateCruiseLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(6).plus(secondDescentLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(7));
				_rangeList.add(_rangeList.get(8).plus(thirdDescentLength).to(SI.KILOMETER));
				_rangeList.add(_rangeList.get(9).plus(landingDistance).to(SI.KILOMETER));

				_totalMissionRange = _rangeList.get(_rangeList.size()-1);

				//--------------------------------------------------------------------
				// TIME
				_timeList.clear();
				
				_timeList.add(Amount.valueOf(0.0, NonSI.MINUTE));
				_timeList.add(_timeList.get(0).plus(takeOffDuration.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(1).plus(totalClimbTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(2).plus(cruiseTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(3).plus(firstDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(4).plus(totalSecondClimbTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(5).plus(alternateCruiseTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(6).plus(secondDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(7).plus(_holdingDuration.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(8).plus(thirdDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(9).plus(landingDuration.to(NonSI.MINUTE)));

				_totalMissionTime = _timeList.get(_timeList.size()-1);

				//--------------------------------------------------------------------
				// USED FUEL
				_fuelUsedList.clear();
				
				_fuelUsedList.add(Amount.valueOf(0.0, SI.KILOGRAM));
				_fuelUsedList.add(_fuelUsedList.get(0).plus(takeOffUsedFuel.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(1).plus(totalClimbFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(2).plus(totalCruiseFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(3).plus(firstDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(4).plus(totalSecondClimbFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(5).plus(totalAlternateCruiseFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(6).plus(secondDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(7).plus(totalHoldingFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(8).plus(thirdDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(9).plus(landingFuelUsed.to(SI.KILOGRAM)));


				_totalFuelUsed = _fuelUsedList.get(_fuelUsedList.size()-1);

				//--------------------------------------------------------------------
				// WEIGHT VARIATION
				_massList.clear();
				
				_massList.add(_initialMissionMass);
				_massList.add(intialClimbMass);
				_massList.add(intialCruiseMass);
				_massList.add(intialFirstDescentMass);
				_massList.add(intialSecondClimbMass);
				_massList.add(intialAlternateCruiseMass);
				_massList.add(intialSecondDescentMass);
				_massList.add(intialHoldingMass);
				_massList.add(intialThirdDescentMass);
				_massList.add(intialLandingMass);
				_massList.add(intialLandingMass.minus(landingFuelUsed));

				_endMissionMass = _massList.get(_massList.size()-1);
			
				//.....................................................................
				// NEW ITERATION CRUISE LENGTH
				cruiseLength = cruiseLength.to(NonSI.NAUTICAL_MILE).plus( 
						(_missionRange.to(NonSI.NAUTICAL_MILE)
								.plus(_alternateCruiseLength).to(NonSI.NAUTICAL_MILE))
						.minus(_totalMissionRange).to(NonSI.NAUTICAL_MILE)
						);
			}
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			newInitialFuelMass = _totalFuelUsed.to(SI.KILOGRAM).divide(1-_fuelReserve); 
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
			
		}
		//----------------------------------------------------------------------
		// ITERATION ENDING ... collecting results
		//----------------------------------------------------------------------
		
		//......................................................................
		_speedTASMissionList.add(Amount.valueOf(0.0, NonSI.KNOT));
		_speedTASMissionList.add(speedAtTakeOffEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtFirstDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtFirstDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtSecondClimbStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtSecondClimbEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtAlternateCruiseStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtAlternateCruiseEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtSecondDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtSecondDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtHoldingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtHoldingEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtThirdDescentStart.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtThirdDescentEnding.to(NonSI.KNOT));
		_speedTASMissionList.add(speedAtLandingStart.to(NonSI.KNOT));
		_speedTASMissionList.add(Amount.valueOf(0.0, NonSI.KNOT));
		
		//......................................................................
		_machMissionList.add(0.0);
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
		_machMissionList.add(0.0);
		
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
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtClimbStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtClimbEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtCruiseStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtCruiseEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtFirstDescentStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtFirstDescentEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtSecondClimbStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
						cLAtSecondClimbEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtAlternateCruiseStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtAlternateCruiseEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtSecondDescentStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtSecondDescentEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtHoldingStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtHoldingEnding
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtThirdDescentStart
						)
				);
		_dragCoefficientMissionList.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLCruise),
						MyArrayUtils.convertToDoublePrimitive(_polarCDCruise),
						cLAtThirdDescentEnding
						)
				);
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
		_thrustMissionList.add(thrustAtLandingGroundRollStart.to(NonSI.POUND_FORCE));
		_thrustMissionList.add(thrustAtLandingGroundRollEnding.to(NonSI.POUND_FORCE));
		
		//......................................................................
		_dragMissionList.add(Amount.valueOf(0.0, NonSI.POUND_FORCE));
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
		_dragMissionList.add(Amount.valueOf(0.0, NonSI.POUND_FORCE));
		
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
				.append("\t\tTotal mission distance = " + _missionRange.to(NonSI.NAUTICAL_MILE)
				.plus(_alternateCruiseLength) + "\n")
				.append("\t\tTotal mission duration = " + _totalMissionTime + "\n")
				.append("\t\tAircraft mass at mission start = " + _initialMissionMass + "\n")
				.append("\t\tAircraft mass at mission end = " + _endMissionMass + "\n")
				.append("\t\tInitial fuel mass for the assigned mission = " + _initialFuelMass + "\n")
				.append("\t\tTotal fuel mass used = " + _totalFuelUsed + "\n")
				.append("\t\tFuel reserve = " + _fuelReserve*100 + " %\n")
				.append("\t\tDesign passengers number = " + _theAircraft.getCabinConfiguration().getNPax() + "\n")
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
				.append("\t\t.....................................\n")
				.append("\t\tCLIMB\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at climb start  = " + _speedTASMissionList.get(2).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at climb ending  = " + _speedTASMissionList.get(3).to(NonSI.KNOT) + " \n")
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
				.append("\t\t.....................................\n")
				.append("\t\tCRUISE\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at cruise start  = " + _speedTASMissionList.get(4).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at cruise ending  = " + _speedTASMissionList.get(5).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at cruise start  = " + _machMissionList.get(4) + " \n")
				.append("\t\tMach at cruise ending  = " + _machMissionList.get(5) + " \n")
				.append("\t\tCL at cruise start  = " + _liftingCoefficientMissionList.get(4) + " \n")
				.append("\t\tCL at cruise ending  = " + _liftingCoefficientMissionList.get(5) + " \n")
				.append("\t\tCD at cruise start  = " + _dragCoefficientMissionList.get(4) + " \n")
				.append("\t\tCD at cruise ending  = " + _dragCoefficientMissionList.get(5) + " \n")
				.append("\t\tEfficiency at cruise start  = " + _efficiencyMissionList.get(4) + " \n")
				.append("\t\tEfficiency at cruise ending  = " + _efficiencyMissionList.get(5) + " \n")
				.append("\t\tThrust at cruise start  = " + _thrustMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at cruise ending  = " + _thrustMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise start  = " + _dragMissionList.get(4).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at cruise ending  = " + _dragMissionList.get(5).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tFIRST DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at first descent start  = " + _speedTASMissionList.get(6).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at first descent ending  = " + _speedTASMissionList.get(7).to(NonSI.KNOT) + " \n")
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
				.append("\t\tDrag at first descent ending  = " + _dragMissionList.get(7).to(NonSI.POUND_FORCE) + " \n");
		
		if(_alternateCruiseAltitude.doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
			sb.append("\t\t.....................................\n")
			.append("\t\tSECOND CLIMB\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second climb start  = " + _speedTASMissionList.get(8).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second climb ending  = " + _speedTASMissionList.get(9).to(NonSI.KNOT) + " \n")
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
			.append("\t\t.....................................\n")
			.append("\t\tALTERNATE CRUISE\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at alternate cruise start  = " + _speedTASMissionList.get(10).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at alternate cruise ending  = " + _speedTASMissionList.get(11).to(NonSI.KNOT) + " \n")
			.append("\t\tMach at alternate cruise start  = " + _machMissionList.get(10) + " \n")
			.append("\t\tMach at alternate cruise ending  = " + _machMissionList.get(11) + " \n")
			.append("\t\tCL at alternate cruise start  = " + _liftingCoefficientMissionList.get(10) + " \n")
			.append("\t\tCL at alternate cruise ending  = " + _liftingCoefficientMissionList.get(11) + " \n")
			.append("\t\tCD at alternate cruise start  = " + _dragCoefficientMissionList.get(10) + " \n")
			.append("\t\tCD at alternate cruise ending  = " + _dragCoefficientMissionList.get(11) + " \n")
			.append("\t\tEfficiency at alternate cruise start  = " + _efficiencyMissionList.get(10) + " \n")
			.append("\t\tEfficiency at alternate cruise ending  = " + _efficiencyMissionList.get(11) + " \n")
			.append("\t\tThrust at alternate cruise start  = " + _thrustMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tThrust at alternate cruise ending  = " + _thrustMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise start  = " + _dragMissionList.get(10).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\tDrag at alternate cruise ending  = " + _dragMissionList.get(11).to(NonSI.POUND_FORCE) + " \n")
			.append("\t\t.....................................\n")
			.append("\t\tSECOND DESCENT\n")
			.append("\t\t.....................................\n")
			.append("\t\tSpeed (TAS) at second descent start  = " + _speedTASMissionList.get(12).to(NonSI.KNOT) + " \n")
			.append("\t\tSpeed (TAS) at second descent ending  = " + _speedTASMissionList.get(13).to(NonSI.KNOT) + " \n")
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
			.append("\t\tDrag at second descent ending  = " + _dragMissionList.get(13).to(NonSI.POUND_FORCE) + " \n");
		}
		if(_holdingDuration.doubleValue(NonSI.MINUTE) != 0.0) {
				sb.append("\t\t.....................................\n")
				.append("\t\tHOLDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at holding start  = " + _speedTASMissionList.get(14).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at holding ending  = " + _speedTASMissionList.get(15).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at holding start  = " + _machMissionList.get(14) + " \n")
				.append("\t\tMach at holding ending  = " + _machMissionList.get(15) + " \n")
				.append("\t\tCL at holding start  = " + _liftingCoefficientMissionList.get(14) + " \n")
				.append("\t\tCL at holding ending  = " + _liftingCoefficientMissionList.get(15) + " \n")
				.append("\t\tCD at holding start  = " + _dragCoefficientMissionList.get(14) + " \n")
				.append("\t\tCD at holding ending  = " + _dragCoefficientMissionList.get(15) + " \n")
				.append("\t\tEfficiency at holding start  = " + _efficiencyMissionList.get(14) + " \n")
				.append("\t\tEfficiency at holding ending  = " + _efficiencyMissionList.get(15) + " \n")
				.append("\t\tThrust at holding start  = " + _thrustMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at holding ending  = " + _thrustMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding start  = " + _dragMissionList.get(14).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at holding ending  = " + _dragMissionList.get(15).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\t.....................................\n")
				.append("\t\tTHIRD DESCENT\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at third descent start  = " + _speedTASMissionList.get(16).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at third descent ending  = " + _speedTASMissionList.get(17).to(NonSI.KNOT) + " \n")
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
				.append("\t\tDrag at third descent ending  = " + _dragMissionList.get(17).to(NonSI.POUND_FORCE) + " \n");
		}
		
				sb.append("\t\t.....................................\n")
				.append("\t\tLANDING\n")
				.append("\t\t.....................................\n")
				.append("\t\tSpeed (TAS) at landing start  = " + _speedTASMissionList.get(18).to(NonSI.KNOT) + " \n")
				.append("\t\tSpeed (TAS) at landing ending  = " + _speedTASMissionList.get(19).to(NonSI.KNOT) + " \n")
				.append("\t\tMach at landing start  = " + _machMissionList.get(18) + " \n")
				.append("\t\tMach at landing ending  = " + _machMissionList.get(19) + " \n")
				.append("\t\tCL at landing start  = " + _liftingCoefficientMissionList.get(18) + " \n")
				.append("\t\tCL at landing ending  = " + _liftingCoefficientMissionList.get(19) + " \n")
				.append("\t\tCD at landing start  = " + _dragCoefficientMissionList.get(18) + " \n")
				.append("\t\tCD at landing ending  = " + _dragCoefficientMissionList.get(19) + " \n")
				.append("\t\tEfficiency at landing start  = " + _efficiencyMissionList.get(18) + " \n")
				.append("\t\tEfficiency at landing ending  = " + _efficiencyMissionList.get(19) + " \n")
				.append("\t\tThrust at landing ground-roll start  = " + _thrustMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tThrust at landing ground-roll ending  = " + _thrustMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing ground-roll start  = " + _dragMissionList.get(18).to(NonSI.POUND_FORCE) + " \n")
				.append("\t\tDrag at landing ground-roll ending  = " + _dragMissionList.get(19).to(NonSI.POUND_FORCE) + " \n")
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

	public Amount<Length> getFirstGuessCruiseLength() {
		return _firstGuessCruiseLength;
	}

	public void setFirstGuessCruiseLength(Amount<Length> _firstGuessCruiseLength) {
		this._firstGuessCruiseLength = _firstGuessCruiseLength;
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

	public Double getFuelReserve() {
		return _fuelReserve;
	}

	public void setFuelReserve(Double _fuelReserve) {
		this._fuelReserve = _fuelReserve;
	}

	public Double getCLmaxClean() {
		return _cLmaxClean;
	}

	public void setCLmaxClean(Double _cLmaxClean) {
		this._cLmaxClean = _cLmaxClean;
	}

	public Double getCLmaxTakeOff() {
		return _cLmaxTakeOff;
	}

	public void setCLmaxTakeOff(Double _cLmaxTakeOff) {
		this._cLmaxTakeOff = _cLmaxTakeOff;
	}

	public Amount<?> getCLAlphaTakeOff() {
		return _cLAlphaTakeOff;
	}

	public void setCLAlphaTakeOff(Amount<?> _cLAlphaTakeOff) {
		this._cLAlphaTakeOff = _cLAlphaTakeOff;
	}

	public Double getCLZeroTakeOff() {
		return _cLZeroTakeOff;
	}

	public void setCLZeroTakeOff(Double _cLZeroTakeOff) {
		this._cLZeroTakeOff = _cLZeroTakeOff;
	}

	public Double getCLmaxLanding() {
		return _cLmaxLanding;
	}

	public void setCLmaxLanding(Double _cLmaxLanding) {
		this._cLmaxLanding = _cLmaxLanding;
	}

	public Double getCLZeroLanding() {
		return _cLZeroLanding;
	}

	public void setCLZeroLanding(Double _cLZeroLanding) {
		this._cLZeroLanding = _cLZeroLanding;
	}

	public Double[] getPolarCLClimb() {
		return _polarCLClimb;
	}

	public void setPolarCLClimb(Double[] _polarCLClimb) {
		this._polarCLClimb = _polarCLClimb;
	}

	public Double[] getPolarCDClimb() {
		return _polarCDClimb;
	}

	public void setPolarCDClimb(Double[] _polarCDClimb) {
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

	public Amount<Duration> getDtRotation() {
		return _dtRotation;
	}

	public void setDtRotation(Amount<Duration> _dtRotation) {
		this._dtRotation = _dtRotation;
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

	public Double getKRotation() {
		return _kRotation;
	}

	public void setKRotation(Double _kRotation) {
		this._kRotation = _kRotation;
	}

	public Double getKLiftOff() {
		return _kLiftOff;
	}

	public void setKLiftOff(Double _kLiftOff) {
		this._kLiftOff = _kLiftOff;
	}

	public Double getKCLmax() {
		return _kCLmax;
	}

	public void setKCLmax(Double _kCLmax) {
		this._kCLmax = _kCLmax;
	}

	public Double getDragDueToEnigneFailure() {
		return _dragDueToEnigneFailure;
	}

	public void setDragDueToEnigneFailure(Double _dragDueToEnigneFailure) {
		this._dragDueToEnigneFailure = _dragDueToEnigneFailure;
	}

	public Double getKAlphaDot() {
		return _kAlphaDot;
	}

	public void setKAlphaDot(Double _kAlphaDot) {
		this._kAlphaDot = _kAlphaDot;
	}

	public Amount<Length> getObstacleLanding() {
		return _obstacleLanding;
	}

	public void setObstacleLanding(Amount<Length> _obstacleLanding) {
		this._obstacleLanding = _obstacleLanding;
	}

	public Amount<Angle> getThetaApproach() {
		return _thetaApproach;
	}

	public void setThetaApproach(Amount<Angle> _thetaApproach) {
		this._thetaApproach = _thetaApproach;
	}

	public Double getKApproach() {
		return _kApproach;
	}

	public void setKApproach(Double _kApproach) {
		this._kApproach = _kApproach;
	}

	public Double getKFlare() {
		return _kFlare;
	}

	public void setKFlare(Double _kFlare) {
		this._kFlare = _kFlare;
	}

	public Double getKTouchDown() {
		return _kTouchDown;
	}

	public void setKTouchDown(Double _kTouchDown) {
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

	public Amount<Mass> getTotalFuelUsed() {
		return _totalFuelUsed;
	}

	public void setTotalFuelUsed(Amount<Mass> _totalFuelUsed) {
		this._totalFuelUsed = _totalFuelUsed;
	}

	public Amount<Duration> getTotalMissionTime() {
		return _totalMissionTime;
	}

	public void setTotalMissionTime(Amount<Duration> _totalMissionTime) {
		this._totalMissionTime = _totalMissionTime;
	}

	public Amount<Length> getTotalMissionRange() {
		return _totalMissionRange;
	}

	public void setTotalMissionRange(Amount<Length> _totalMissionRange) {
		this._totalMissionRange = _totalMissionRange;
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

	public Double getHoldingMachNumber() {
		return _holdingMachNumber;
	}

	public void setHoldingMachNumber(Double _holdingMachNumber) {
		this._holdingMachNumber = _holdingMachNumber;
	}

	public Double getLandingFuelFlow() {
		return _landingFuelFlow;
	}

	public void setLandingFuelFlow(Double _landingFuelFlow) {
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

	public Double[] getPolarCLCruise() {
		return _polarCLCruise;
	}

	public void setPolarCLCruise(Double[] _polarCLCruise) {
		this._polarCLCruise = _polarCLCruise;
	}

	public Double[] getPolarCDCruise() {
		return _polarCDCruise;
	}

	public void setPolarCDCruise(Double[] _polarCDCruise) {
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

	public Double[] getPolarCLTakeOff() {
		return _polarCLTakeOff;
	}

	public void setPolarCLTakeOff(Double[] _polarCLTakeOff) {
		this._polarCLTakeOff = _polarCLTakeOff;
	}

	public Double[] getPolarCDTakeOff() {
		return _polarCDTakeOff;
	}

	public void setPolarCDTakeOff(Double[] _polarCDTakeOff) {
		this._polarCDTakeOff = _polarCDTakeOff;
	}

	public Double[] getPolarCLLanding() {
		return _polarCLLanding;
	}

	public void setPolarCLLanding(Double[] _polarCLLanding) {
		this._polarCLLanding = _polarCLLanding;
	}

	public Double[] getPolarCDLanding() {
		return _polarCDLanding;
	}

	public void setPolarCDLanding(Double[] _polarCDLanding) {
		this._polarCDLanding = _polarCDLanding;
	}
}
