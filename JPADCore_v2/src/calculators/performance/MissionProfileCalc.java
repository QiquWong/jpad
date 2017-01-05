package calculators.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;

public class MissionProfileCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Amount<Length> _takeOffMissionAltitude;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _singlePassengerMass;
	private Amount<Mass> _firstGuessInitialFuelMass;
	private Amount<Length> _firstGuessCruiseLength;
	private Double _cruiseMissionMachNumber;
	private Amount<Length> _alternateCruiseLength;
	private Amount<Length> _alternateCruiseAltitude;
	private Double _alternateCruiseMachNumber;
	private Amount<Duration> _holdingDuration;
	private Amount<Length> _holdingAltitude;
	private Double _holdingMachNumber;
	private Double _landingFuelFlow;
	private Double _fuelReserve;
	private Double _cLmaxClean;
	private Amount<?> _cLAlphaClean;
	private Double _cLmaxTakeOff;
	private Amount<?> _cLAlphaTakeOff;
	private Double _cLZeroTakeOff;
	private Double _cLmaxLanding;
	private Amount<?> _cLAlphaLanding;
	private Double _cLZeroLanding;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Double[] _polarCLTakeOff;
	private Double[] _polarCDTakeOff;
	private Double[] _polarCLLanding;
	private Double[] _polarCDLanding;
	private Amount<Velocity> _windSpeed;
	private Double _mu;
	private Double _muBrake;
	private Amount<Duration> _dtRotation;
	private Amount<Duration> _dtHold;
	private Amount<Angle> _alphaGround;
	private Amount<Length> _obstacleTakeOff;
	private Double _kRotation;
	private Double _kLiftOff;
	private Double _kCLmax;
	private Double _dragDueToEnigneFailure;
	private Double _kAlphaDot;
	private Double _alphaReductionRate;
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
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> singlePassengerMass,
			Amount<Mass> firstGuessInitialFuelMass,
			Amount<Length> takeOffMissionAltitude,
			Amount<Length> firstGuessCruiseLength,
			Double cruiseMissionMachNumber,
			Amount<Length> alternateCruiseLength,
			Amount<Length> alternateCruiseAltitude,
			Double alternateCruiseMachNumber,
			Amount<Duration> holdingDuration,
			Amount<Length> holdingAltitude,
			Double holdingMachNumber,
			Double landingFuelFlow,
			Double fuelReserve,
			Double cLmaxClean,
			Amount<?> cLAlphaClean,
			Double cLmaxTakeOff,
			Amount<?> cLAlphaTakeOff,
			Double cLZeroTakeOff,
			Double cLmaxLanding,
			Amount<?> cLAlphaLanding,
			Double cLZeroLanding,
			Double[] polarCLCruise,
			Double[] polarCDCruise,
			Double[] polarCLClimb,
			Double[] polarCDClimb,
			Double[] polarCLTakeOff,
			Double[] polarCDTakeOff,
			Double[] polarCLLanding,
			Double[] polarCDLanding,
			Amount<Velocity> windSpeed,
			Double mu,
			Double muBrake,
			Amount<Duration> dtRotation,
			Amount<Duration> dtHold,
			Amount<Angle> alphaGround,
			Amount<Length> obstacleTakeOff,
			Double kRotation,
			Double kLiftOff,
			Double kCLmax,
			Double dragDueToEnigneFailure,
			Double kAlphaDot,
			Double alphaReductionRate,
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
		this._operatingEmptyMass = operatingEmptyMass;
		this._singlePassengerMass = singlePassengerMass;
		this._firstGuessInitialFuelMass = firstGuessInitialFuelMass;
		this._takeOffMissionAltitude = takeOffMissionAltitude;
		this._firstGuessCruiseLength = firstGuessCruiseLength;
		this._cruiseMissionMachNumber = cruiseMissionMachNumber;
		this._alternateCruiseLength = alternateCruiseLength;
		this._alternateCruiseAltitude = alternateCruiseAltitude;
		this._alternateCruiseMachNumber = alternateCruiseMachNumber;
		this._holdingDuration = holdingDuration;
		this._holdingAltitude = holdingAltitude;
		this._holdingMachNumber = holdingMachNumber;
		this._landingFuelFlow = landingFuelFlow;
		this._fuelReserve = fuelReserve;
		this._cLmaxClean = cLmaxClean;
		this._cLAlphaClean = cLAlphaClean;
		this._cLmaxTakeOff = cLmaxTakeOff;
		this._cLAlphaTakeOff = cLAlphaTakeOff;
		this._cLZeroTakeOff = cLZeroTakeOff;
		this._cLmaxLanding = cLmaxLanding;
		this._cLAlphaLanding = cLAlphaLanding;
		this._cLZeroLanding = cLZeroLanding;
		this._polarCLCruise = polarCLCruise;
		this._polarCDCruise = polarCDCruise;
		this._polarCLClimb = polarCLClimb;
		this._polarCDClimb = polarCDClimb;
		this._polarCLTakeOff = polarCLTakeOff;
		this._polarCDTakeOff = polarCDTakeOff;
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
		this._alphaReductionRate = alphaReductionRate;
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
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateProfiles() {

		_initialMissionMass = _operatingEmptyMass
				.plus(_singlePassengerMass.times(_theAircraft.getCabinConfiguration().getNPax()))
				.plus(_firstGuessInitialFuelMass); 
		
		_initialFuelMass = _firstGuessInitialFuelMass;
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
					_theAircraft,
					_takeOffMissionAltitude,
					_theOperatingConditions.getMachTakeOff(),
					_initialMissionMass,
					_dtRotation,
					_dtHold,
					_kCLmax,
					_kRotation,
					_kLiftOff,
					_dragDueToEnigneFailure,
					_theOperatingConditions.getThrottleTakeOff(), 
					_kAlphaDot,
					_alphaReductionRate,
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

			theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);

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

			theClimbCalculator.calculateClimbPerformance(intialClimbMass, intialClimbMass);

			Amount<Length> totalClimbRange = theClimbCalculator.getClimbTotalRange();
			Amount<Duration> totalClimbTime = null;
			if(_climbSpeed != null)
				totalClimbTime = theClimbCalculator.getClimbTimeAtSpecificClimbSpeedAOE();
			else
				totalClimbTime = theClimbCalculator.getMinimumClimbTimeAOE();
			Amount<Mass> totalClimbFuelUsed = theClimbCalculator.getClimbTotalFuelUsed();

			//--------------------------------------------------------------------
			// CRUISE
			
			Amount<Length> cruiseLength = _firstGuessCruiseLength;
			_totalMissionRange = Amount.valueOf(0.0, SI.METER);
			
			while (
					Math.abs(
							(_theAircraft.getTheAnalysisManager().getReferenceRange().to(NonSI.NAUTICAL_MILE)
									.plus(_alternateCruiseLength.to(NonSI.NAUTICAL_MILE)))
							.minus(_totalMissionRange.to(NonSI.NAUTICAL_MILE))
							.doubleValue(NonSI.NAUTICAL_MILE)
							) 
					>= 0.001
					) {
				
				Amount<Mass> intialCruiseMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed);

				Amount<Duration> cruiseTime = 
						Amount.valueOf(
								cruiseLength.to(SI.METER)
								.divide(_cruiseMissionMachNumber
										*_theOperatingConditions.getAtmosphereCruise().getSpeedOfSound()
										)
								.getEstimatedValue(),
								SI.SECOND
								);

				Double sfcCruise = 
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								_theOperatingConditions.getThrottleCruise(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant(),
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								_cruiseMissionMachNumber
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								_cruiseMissionMachNumber,
								_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
								EngineDatabaseManager.getThrustRatio(
										_cruiseMissionMachNumber,
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
								);

				Amount<Mass> totalCruiseFuelUsed = 
						Amount.valueOf(
								cruiseTime.doubleValue(NonSI.MINUTE)
								*sfcCruise,
								SI.KILOGRAM
								);

				//--------------------------------------------------------------------
				// DESCENT (up to ALTERNATE altitude)
				Amount<Mass> intialFirstDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed);

				DescentCalc theFirstDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_theOperatingConditions.getAltitudeCruise(),
						_alternateCruiseAltitude
						);

				theFirstDescentCalculator.calculateDescentPerformance();
				Amount<Length> firstDescentLength = theFirstDescentCalculator.getTotalDescentLength();
				Amount<Duration> firstDescentTime = theFirstDescentCalculator.getTotalDescentTime();
				Amount<Mass> firstDescentFuelUsed = theFirstDescentCalculator.getTotalDescentFuelUsed();

				//--------------------------------------------------------------------
				// ALTERNATE CRUISE
				Amount<Mass> intialAlternateCruiseMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed);

				double speedOfSoundAlternateCruiseAltitude = new StdAtmos1976(_alternateCruiseAltitude.doubleValue(SI.METER)).getSpeedOfSound();

				Amount<Duration> alternateCruiseTime = 
						Amount.valueOf(
								_alternateCruiseLength.to(SI.METER)
								.divide(_alternateCruiseMachNumber*speedOfSoundAlternateCruiseAltitude)
								.getEstimatedValue(),
								SI.SECOND
								);

				Double sfcAlternateCruise = 
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								_theOperatingConditions.getThrottleCruise(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant(),
								_alternateCruiseAltitude.doubleValue(SI.METER),
								_alternateCruiseMachNumber
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								_alternateCruiseMachNumber,
								_alternateCruiseAltitude.doubleValue(SI.METER),
								EngineDatabaseManager.getThrustRatio(
										_alternateCruiseMachNumber,
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
								);

				Amount<Mass> totalAlternateCruiseFuelUsed = 
						Amount.valueOf(
								alternateCruiseTime.doubleValue(NonSI.MINUTE)
								*sfcAlternateCruise,
								SI.KILOGRAM
								);

				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				Amount<Mass> intialSecondDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalAlternateCruiseFuelUsed);

				DescentCalc theSecondDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_alternateCruiseAltitude,
						_holdingAltitude
						);

				theSecondDescentCalculator.calculateDescentPerformance();
				Amount<Length> secondDescentLength = theSecondDescentCalculator.getTotalDescentLength();
				Amount<Duration> secondDescentTime = theSecondDescentCalculator.getTotalDescentTime();
				Amount<Mass> secondDescentFuelUsed = theSecondDescentCalculator.getTotalDescentFuelUsed();

				//--------------------------------------------------------------------
				// HOLDING
				Amount<Mass> intialHoldingMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalAlternateCruiseFuelUsed)
						.minus(secondDescentFuelUsed);

				Double sfcHolding = 
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								1.0, // throttle
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.DESCENT,
								_theAircraft.getPowerPlant(),
								_holdingAltitude.doubleValue(SI.METER),
								_holdingMachNumber
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								_holdingMachNumber,
								_holdingAltitude.doubleValue(SI.METER),
								EngineDatabaseManager.getThrustRatio(
										_holdingMachNumber,
										_holdingAltitude.doubleValue(SI.METER),
										_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
										_theAircraft.getPowerPlant().getEngineType(),
										EngineOperatingConditionEnum.DESCENT,
										_theAircraft.getPowerPlant()
										),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.DESCENT,
								_theAircraft.getPowerPlant()
								);

				Amount<Mass> totalHoldingFuelUsed = 
						Amount.valueOf(
								_holdingDuration.doubleValue(NonSI.MINUTE)
								*sfcHolding,
								SI.KILOGRAM
								);

				//--------------------------------------------------------------------
				// DESCENT (up to LANDING altitude)
				Amount<Mass> intialThirdDescentMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalAlternateCruiseFuelUsed)
						.minus(secondDescentFuelUsed)
						.minus(totalHoldingFuelUsed);

				DescentCalc theThirdDescentCalculator = new DescentCalc(
						_theAircraft,
						_speedDescentCAS,
						_rateOfDescent,
						_holdingAltitude,
						Amount.valueOf(15.24, SI.METER)
						);

				theThirdDescentCalculator.calculateDescentPerformance();
				Amount<Length> thirdDescentLength = theThirdDescentCalculator.getTotalDescentLength();
				Amount<Duration> thirdDescentTime = theThirdDescentCalculator.getTotalDescentTime();
				Amount<Mass> thirdDescentFuelUsed = theThirdDescentCalculator.getTotalDescentFuelUsed();

				//--------------------------------------------------------------------
				// LANDING
				Amount<Mass> intialLandingMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
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
						_theOperatingConditions.getReverseThrottleLanding(),
						_freeRollDuration
						);

				theLandingCalculator.calculateLandingDistance();

				Amount<Length> landingDistance = theLandingCalculator.getsTotal();
				Amount<Duration> landingDuration = theLandingCalculator.getTime().get(theLandingCalculator.getTime().size()-1);
				Amount<Mass> landingFuelUsed = 
						Amount.valueOf(
								landingDuration.to(NonSI.MINUTE).times(_landingFuelFlow).getEstimatedValue(),
								SI.KILOGRAM
								);


				//--------------------------------------------------------------------
				// ALTITUDE
				_altitudeList.clear();
				
				_altitudeList.add(Amount.valueOf(0.0, SI.METER));
				_altitudeList.add(theTakeOffCalculator.getObstacle().to(SI.METER));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(SI.METER));
				_altitudeList.add(_theOperatingConditions.getAltitudeCruise().to(SI.METER));
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
				_rangeList.add(_rangeList.get(4).plus(_alternateCruiseLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(5).plus(secondDescentLength.to(SI.KILOMETER)));
				_rangeList.add(_rangeList.get(6));
				_rangeList.add(_rangeList.get(7).plus(thirdDescentLength).to(SI.KILOMETER));
				_rangeList.add(_rangeList.get(8).plus(landingDistance).to(SI.KILOMETER));

				_totalMissionRange = _rangeList.get(_rangeList.size()-1);

				//--------------------------------------------------------------------
				// TIME
				_timeList.clear();
				
				_timeList.add(Amount.valueOf(0.0, NonSI.MINUTE));
				_timeList.add(_timeList.get(0).plus(takeOffDuration.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(1).plus(totalClimbTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(2).plus(cruiseTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(3).plus(firstDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(4).plus(alternateCruiseTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(5).plus(secondDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(6).plus(_holdingDuration.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(7).plus(thirdDescentTime.to(NonSI.MINUTE)));
				_timeList.add(_timeList.get(8).plus(landingDuration.to(NonSI.MINUTE)));

				_totalMissionTime = _timeList.get(_timeList.size()-1);

				//--------------------------------------------------------------------
				// USED FUEL
				_fuelUsedList.clear();
				
				_fuelUsedList.add(Amount.valueOf(0.0, SI.KILOGRAM));
				_fuelUsedList.add(_fuelUsedList.get(0).plus(takeOffUsedFuel.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(1).plus(totalClimbFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(2).plus(totalCruiseFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(3).plus(firstDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(4).plus(totalAlternateCruiseFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(5).plus(secondDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(6).plus(totalHoldingFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(7).plus(thirdDescentFuelUsed.to(SI.KILOGRAM)));
				_fuelUsedList.add(_fuelUsedList.get(8).plus(landingFuelUsed.to(SI.KILOGRAM)));


				_totalFuelUsed = _fuelUsedList.get(_fuelUsedList.size()-1);

				//--------------------------------------------------------------------
				// WEIGHT VARIATION
				_massList.clear();
				
				_massList.add(_initialMissionMass);
				_massList.add(intialClimbMass);
				_massList.add(intialCruiseMass);
				_massList.add(intialFirstDescentMass);
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
						(_theAircraft.getTheAnalysisManager().getReferenceRange().to(NonSI.NAUTICAL_MILE)
								.plus(_alternateCruiseLength).to(NonSI.NAUTICAL_MILE))
						.minus(_totalMissionRange).to(NonSI.NAUTICAL_MILE)
						);
			}
			
			//.....................................................................
			// NEW INITIAL MISSION MASS
			newInitialFuelMass = _totalFuelUsed.to(SI.KILOGRAM).divide(1-_fuelReserve); 
			_initialMissionMass = _operatingEmptyMass
					.plus(_singlePassengerMass.times(_theAircraft.getCabinConfiguration().getNPax()))
					.plus(newInitialFuelMass); 
			i++;
			
		}
		_initialFuelMass = newInitialFuelMass;
	}

	public void plotProfiles(
			List<PerformancePlotEnum> _plotList,
			String _missionProfilesFolderPath) {
		
		if(_plotList.contains(PerformancePlotEnum.RANGE_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(_rangeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeList),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"km", "m",
					_missionProfilesFolderPath, "Range_Profile_(km)"
					);
			
			double[] rangeNauticalMiles = new double[_rangeList.size()];
			for(int i=0; i<rangeNauticalMiles.length; i++)
				rangeNauticalMiles[i] = _rangeList.get(i).doubleValue(NonSI.NAUTICAL_MILE);
			MyChartToFileUtils.plotNoLegend(
					rangeNauticalMiles,
					MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeList),
					0.0, null, 0.0, null,
					"Range", "Altitude",
					"nmi", "m",
					_missionProfilesFolderPath, "Range_Profile_(nmi)"
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.TIME_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(_timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeList),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "m",
					_missionProfilesFolderPath, "Time_Profile_(minutes)"
					);
			
			double[] timeHours = new double[_timeList.size()];
			for(int i=0; i<timeHours.length; i++)
				timeHours[i] = _timeList.get(i).doubleValue(NonSI.HOUR);
			MyChartToFileUtils.plotNoLegend(
					timeHours,
					MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeList),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"hr", "m",
					_missionProfilesFolderPath, "Time_Profile_(hours)"
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_USED_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(_fuelUsedList),
					MyArrayUtils.convertListOfAmountTodoubleArray(_altitudeList),
					0.0, null, 0.0, null,
					"Fuel used", "Altitude",
					"kg", "m",
					_missionProfilesFolderPath, "Fuel_used_Profile_(kg)"
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.WEIGHT_PROFILE)) { 
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(_timeList),
					MyArrayUtils.convertListOfAmountTodoubleArray(_massList),
					0.0, null, null, null,
					"Time", "Aircraft mass",
					"s", "kg",
					_missionProfilesFolderPath, "Mass_Profile_(kg)"
					);
			
		}
		
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

	public Amount<?> getCLAlphaClean() {
		return _cLAlphaClean;
	}

	public void setCLAlphaClean(Amount<?> _cLAlphaClean) {
		this._cLAlphaClean = _cLAlphaClean;
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

	public Amount<?> getCLAlphaLanding() {
		return _cLAlphaLanding;
	}

	public void setCLAlphaLanding(Amount<?> _cLAlphaLanding) {
		this._cLAlphaLanding = _cLAlphaLanding;
	}

	public Double getCLZeroLanding() {
		return _cLZeroLanding;
	}

	public void setCLZeroLanding(Double _cLZeroLanding) {
		this._cLZeroLanding = _cLZeroLanding;
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

	public Amount<Velocity> getWindSpeed() {
		return _windSpeed;
	}

	public void setWindSpeed(Amount<Velocity> _windSpeed) {
		this._windSpeed = _windSpeed;
	}

	public Double getMu() {
		return _mu;
	}

	public void setMu(Double _mu) {
		this._mu = _mu;
	}

	public Double getMuBrake() {
		return _muBrake;
	}

	public void setMuBrake(Double _muBrake) {
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

	public Double getAlphaReductionRate() {
		return _alphaReductionRate;
	}

	public void setAlphaReductionRate(Double _alphaReductionRate) {
		this._alphaReductionRate = _alphaReductionRate;
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

	public Double getCruiseMissionMachNumber() {
		return _cruiseMissionMachNumber;
	}

	public void setCruiseMissionMachNumber(Double _cruiseMissionMachNumber) {
		this._cruiseMissionMachNumber = _cruiseMissionMachNumber;
	}

	public Double getAlternateCruiseMachNumber() {
		return _alternateCruiseMachNumber;
	}

	public void setAlternateCruiseMachNumber(Double _alternateCruiseMachNumber) {
		this._alternateCruiseMachNumber = _alternateCruiseMachNumber;
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
}
