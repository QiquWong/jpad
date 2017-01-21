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
import jahuwaldt.aero.StdAtmos1976;
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
	private Double _alternateCruiseMachNumber;
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
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
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
			Double alternateCruiseMachNumber,
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
			Double[] polarCLClimb,
			Double[] polarCDClimb,
			Double[] polarCLCruise,
			Double[] polarCDCruise,
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
		this._alternateCruiseMachNumber = alternateCruiseMachNumber;
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
		this._polarCLClimb = polarCLClimb;
		this._polarCDClimb = polarCDClimb;
		this._polarCLCruise = polarCLCruise;
		this._polarCDCruise = polarCDCruise;
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
				.plus(_singlePassengerMass.times(_passengersNumber))
				.plus(_firstGuessInitialFuelMass); 
		
		_initialFuelMass = _firstGuessInitialFuelMass;
		
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
					_theOperatingConditions.getThrottleGroundIdleTakeOff(),
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

			theClimbCalculator.calculateClimbPerformance(
					intialClimbMass,
					intialClimbMass,
					Amount.valueOf(0.0, SI.METER),
					_theOperatingConditions.getAltitudeToReach(),
					false
					);

			Amount<Length> totalClimbRange = theClimbCalculator.getClimbTotalRange();
			Amount<Duration> totalClimbTime = null;
			if(_climbSpeed != null)
				totalClimbTime = theClimbCalculator.getClimbTimeAtSpecificClimbSpeedAOE();
			else
				totalClimbTime = theClimbCalculator.getMinimumClimbTimeAOE();
			Amount<Mass> totalClimbFuelUsed = theClimbCalculator.getClimbTotalFuelUsed();

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
				
				Airfoil meanAirfoil = new Airfoil(
						LiftingSurface.calculateMeanAirfoil(_theAircraft.getWing()),
						_theAircraft.getWing().getAerodynamicDatabaseReader()
						);
				
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
								_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
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
				
				double cruiseMissionMachNumber = intersectionList.get(0).getMaxMach();
				double speed = intersectionList.get(0).getMaxSpeed();
				
				List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
				aircraftMassPerStep.add(intialCruiseMass);
				
				List<Double> cLSteps = new ArrayList<>();
				cLSteps.add(
						LiftCalc.calculateLiftCoeff(
								aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								speed,
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
										speed,
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
										cruiseMissionMachNumber
										)
								)
						.getEstimatedValue()
						);
				
				List<Double> fuelFlows = new ArrayList<>();
				if(phi.get(0) < 1.001) {
					fuelFlows.add(
							dragPerStep.get(0).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*_sfcFunctionCruise.value(phi.get(0))
							);
				}
				
				List<Amount<Duration>> times = new ArrayList<>(); 
				times.add(
						Amount.valueOf(
								(cruiseSteps[1]-cruiseSteps[0])/speed,
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
					cLSteps.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									speed,
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
											speed,
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
											cruiseMissionMachNumber
											)
									)
							.getEstimatedValue()
							);
					
					if(phi.get(j) < 1.0) {
						fuelFlows.add(
								dragPerStep.get(j).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionCruise.value(phi.get(j))
								);
					}
					
					times.add(
							Amount.valueOf(
									(cruiseSteps[j]-cruiseSteps[j-1])/speed,
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
						_theOperatingConditions.getAltitudeCruise(),
						_holdingAltitude,
						intialFirstDescentMass,
						_polarCLCruise,
						_polarCDCruise
						);


				theFirstDescentCalculator.calculateDescentPerformance();
				Amount<Length> firstDescentLength = theFirstDescentCalculator.getTotalDescentLength();
				Amount<Duration> firstDescentTime = theFirstDescentCalculator.getTotalDescentTime();
				Amount<Mass> firstDescentFuelUsed = theFirstDescentCalculator.getTotalDescentFuelUsed();

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
						_holdingAltitude,
						_alternateCruiseAltitude,
						false
						);

				Amount<Length> totalSecondClimbRange = theSecondClimbCalculator.getClimbTotalRange();
				Amount<Duration> totalSecondClimbTime = null;
				if(_climbSpeed != null)
					totalSecondClimbTime = theSecondClimbCalculator.getClimbTimeAtSpecificClimbSpeedAOE();
				else
					totalSecondClimbTime = theSecondClimbCalculator.getMinimumClimbTimeAOE();
				Amount<Mass> totalSecondClimbFuelUsed = theClimbCalculator.getClimbTotalFuelUsed();
				
				//--------------------------------------------------------------------
				// ALTERNATE CRUISE
				Amount<Mass> intialAlternateCruiseMass = 
						_initialMissionMass
						.minus(takeOffUsedFuel)
						.minus(totalClimbFuelUsed)
						.minus(totalCruiseFuelUsed)
						.minus(firstDescentFuelUsed)
						.minus(totalSecondClimbFuelUsed);
				
				double speedOfSoundAlternateCruiseAltitude = new StdAtmos1976(
						_alternateCruiseAltitude.doubleValue(SI.METER)
						).getSpeedOfSound();
				double speedAlternateCruise = _alternateCruiseMachNumber
						*speedOfSoundAlternateCruiseAltitude;
				
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
								speedAlternateCruise,
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
										speedAlternateCruise,
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
										_alternateCruiseMachNumber
										)
								)
						.getEstimatedValue()
						);
				
				List<Double> fuelFlowsAlternateCruise = new ArrayList<>();
				if(phiAlternateCruise.get(0) < 1.0) {
					fuelFlowsAlternateCruise.add(
							dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(0))						
							);
				}
				
				List<Amount<Duration>> timesAlternateCruise = new ArrayList<>(); 
				timesAlternateCruise.add(
						Amount.valueOf(
								(alternateCruiseSteps[1]-alternateCruiseSteps[0])/speedAlternateCruise,
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
					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									speedAlternateCruise,
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
											speedAlternateCruise,
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
											_alternateCruiseMachNumber
											)
									)
							.getEstimatedValue()
							);
					
					if(phiAlternateCruise.get(j) < 1.0) {
						fuelFlowsAlternateCruise.add(
								dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*_sfcFunctionAlternateCruise.value(phiAlternateCruise.get(j))
								);
					}
					
					timesAlternateCruise.add(
							Amount.valueOf(
									(alternateCruiseSteps[j]-alternateCruiseSteps[j-1])/speedAlternateCruise,
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
						_alternateCruiseAltitude,
						_holdingAltitude,
						intialSecondDescentMass,
						_polarCLCruise,
						_polarCDCruise
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
										_alternateCruiseAltitude.doubleValue(SI.METER), 
										_alternateCruiseMachNumber
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
											_alternateCruiseAltitude.doubleValue(SI.METER), 
											_alternateCruiseMachNumber
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
						_holdingAltitude,
						Amount.valueOf(15.24, SI.METER),
						intialThirdDescentMass,
						_polarCLCruise,
						_polarCDCruise
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
				
				_passengersNumber += (int) Math.floor(
						(_maximumTakeOffMass.minus(_initialMissionMass))
						.divide(_singlePassengerMass)
						.getEstimatedValue()
						)
						;
				_initialMissionMass = _maximumTakeOffMass;
				
			}
			
			i++;
			
		}
		_initialFuelMass = newInitialFuelMass;
	}

	public void plotProfiles(
			List<PerformancePlotEnum> _plotList,
			String _missionProfilesFolderPath) {
		
		if(_plotList.contains(PerformancePlotEnum.RANGE_PROFILE)) { 
			
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
					_missionProfilesFolderPath, "Range_Profile"
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
							.map(a -> a.to(NonSI.FOOT))
							.collect(Collectors.toList()
									)
							),
					0.0, null, 0.0, null,
					"Time", "Altitude",
					"min", "ft",
					_missionProfilesFolderPath, "Time_Profile_(min)"
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
					_missionProfilesFolderPath, "Time_Profile_(hours)"
					);
			
		}
		
		if(_plotList.contains(PerformancePlotEnum.FUEL_USED_PROFILE)) { 
			
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
					_missionProfilesFolderPath, "Fuel_used_Profile_(lb)"
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_fuelUsedList.stream()
							.map(f -> f.to(SI.KILOGRAM))
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
					"kg", "ft",
					_missionProfilesFolderPath, "Fuel_used_Profile_(kg)"
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
					_missionProfilesFolderPath, "Mass_Profile_(kg)"
					);
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_timeList.stream()
							.map(t -> t.to(NonSI.MINUTE))
							.collect(Collectors.toList()
									)
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							_massList.stream()
							.map(m -> m.to(NonSI.POUND))
							.collect(Collectors.toList()
									)
							),
					0.0, null, null, null,
					"Time", "Aircraft mass",
					"min", "lb",
					_missionProfilesFolderPath, "Mass_Profile_(lb)"
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
				.append("\t\tAircraft weight at third descnet start = " + _massList.get(9).to(SI.KILOGRAM) + " \n")
				.append("\t\tAircraft weight at landing start = " + _massList.get(10).to(SI.KILOGRAM) + " \n")
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
}
