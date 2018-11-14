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
import database.databasefunctions.engine.EngineDatabaseManager_old;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class PayloadRangeCalcMissionProfile{
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	
	// INPUT DATA:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Amount<Length> _takeOffMissionAltitude;
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _maxFuelMass;
	private Amount<Mass> _singlePassengerMass;
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
	private Double[] _polarCLLanding;
	private Double[] _polarCDLanding;
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
	private Amount<Velocity> _windSpeed;
	private MyInterpolatingFunction _mu;
	private MyInterpolatingFunction _muBrake;
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
	private boolean _calculateSFCCruise;
	private boolean _calculateSFCAlternateCruise;
	private boolean _calculateSFCHolding;
	
	// TO EVALUATE:
	private Boolean _missionProfileStopped = Boolean.FALSE;
	private Amount<Length> _rangeAtMaxPayload;
	private Amount<Length> _rangeAtDesignPayload;
	private Amount<Length> _rangeAtMaxFuel;	
	private Amount<Length> _rangeAtZeroPayload;
	private Amount<Mass> _takeOffMassZeroPayload;
	private Amount<Mass> _maxPayload;
	private Amount<Mass> _designPayload;
	private Amount<Mass> _payloadAtMaxFuel;
	private Integer _passengersNumberAtMaxPayload;
	private Integer _passengersNumberAtDesignPayload;
	private Integer _passengersNumberAtMaxFuel;
	private Amount<Mass> _requiredMassAtMaxPayload;
	private Amount<Mass> _requiredMassAtDesignPayload;
	
	private List<Amount<Length>> _rangeArray;
	private List<Double> _payloadArray;
	
	//-------------------------------------------------------------------------------------
	// BUILDER
	
	public PayloadRangeCalcMissionProfile(
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Length> takeOffMissionAltitude,
			Amount<Mass> maximumTakeOffMass,
			Amount<Mass> operatingEmptyMass,
			Amount<Mass> maxFuelMass,
			Amount<Mass> singlePassengerMass,
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
		this._takeOffMissionAltitude = takeOffMissionAltitude;
		this._maximumTakeOffMass = maximumTakeOffMass;
		this._operatingEmptyMass = operatingEmptyMass;
		this._maxFuelMass = maxFuelMass;
		this._singlePassengerMass = singlePassengerMass;
		this._firstGuessCruiseLength = firstGuessCruiseLength;
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
		this._cLmaxTakeOff =  cLmaxTakeOff;
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
		
	}

	//-------------------------------------------------------------------------------------
	// METHODS
	
	/**************************************************************************************
	 * This method calculates the range of a given airplane, with given engine type,
	 * in case of Maximum TO Weight and Max Payload.
	 *
	 * @author Vittorio Trifari
	 * @return range the range value in [nmi]
	 */
	private Amount<Length> calcRangeAtGivenPayload(
			Amount<Mass> maxTakeOffMassCurrent,
			Amount<Mass> payloadMass,
			Amount<Velocity> vMC
			) {	

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
		DescentCalc theThirdDescentCalculator = null;
		LandingCalc theLandingCalculator = null;
		
		//----------------------------------------------------------------------
		// TAKE-OFF
		Amount<Length> rangeTakeOff = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelTakeOff = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// CLIMB
		Amount<Length> rangeClimb = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelClimb = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtClimbStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// CRUISE
		Amount<Length> rangeCruise = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelCruise = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtCruiseStart = Amount.valueOf(0.0, SI.KILOGRAM);
		double meanCruiseMachNumber = 0.0;
		double meanFuelFlow = 0.0;
		//......................................................................
		// FIRST DESCENT
		Amount<Length> rangeFirstDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelFirstDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtFirstDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// SECOND CLIMB
		Amount<Length> rangeSecondClimb = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelSecondClimb = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondClimbStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// ALTERNATE CRUISE
		Amount<Length> rangeAlternateCruise = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelAlternateCruise = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtAlternateCruiseStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// SECOND DESCENT
		Amount<Length> rangeSecondDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelSecondDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtSecondDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// HOLDING
		Amount<Length> rangeHolding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelHolding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtHoldingStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// THIRD DESCENT
		Amount<Length> rangeThirdDescent = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelThirdDescent = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtThirdDescentStart = Amount.valueOf(0.0, SI.KILOGRAM);
		//......................................................................
		// LANDING
		Amount<Length> rangeLanding = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		Amount<Mass> fuelLanding = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> aircraftMassAtLandingStart = Amount.valueOf(0.0, SI.KILOGRAM);

		//----------------------------------------------------------------------
		// ITERATION START ...
		//----------------------------------------------------------------------
		Amount<Length> totalMissionRange = Amount.valueOf(0.0, SI.METER);
		
		Amount<Mass> initialMissionMass = maxTakeOffMassCurrent.to(SI.KILOGRAM); 
		
		Amount<Mass> targetFuelMass = 
				maxTakeOffMassCurrent.to(SI.KILOGRAM)
				.minus(_operatingEmptyMass.to(SI.KILOGRAM))
				.minus(payloadMass.to(SI.KILOGRAM));
		
		Amount<Length> initialCruiseLength = _firstGuessCruiseLength.to(NonSI.NAUTICAL_MILE);
		Amount<Length> newCruiseLength = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		int i=0;
		
		Amount<Mass> totalFuelUsed = Amount.valueOf(0.0, SI.KILOGRAM);

		do {

			if(i >= 1)
				initialCruiseLength = newCruiseLength;
			
			if(i > 100) {
				System.err.println("WARNING: (PAYLOAD-RANGE) MAXIMUM NUMBER OF ITERATION REACHED");
				break;
			}

			//--------------------------------------------------------------------
			// TAKE-OFF
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
					initialMissionMass.to(SI.KILOGRAM),
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

			rangeTakeOff = groundRollDistanceTakeOff
					.plus(rotationDistanceTakeOff)
					.plus(airborneDistanceTakeOff);			
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

			aircraftMassAtClimbStart = initialMissionMass.minus(fuelTakeOff.to(SI.KILOGRAM));

			theClimbCalculator.calculateClimbPerformance(
					aircraftMassAtClimbStart,
					aircraftMassAtClimbStart,
					_obstacleTakeOff.to(SI.METER),
					_theOperatingConditions.getAltitudeCruise().to(SI.METER),
					false,
					false
					);

			rangeClimb = theClimbCalculator.getClimbTotalRange();
			fuelClimb = theClimbCalculator.getClimbTotalFuelUsed();

			//--------------------------------------------------------------------
			// CRUISE (CONSTANT MACH AND ALTITUDE)
			aircraftMassAtCruiseStart = 
					initialMissionMass
					.minus(fuelTakeOff.to(SI.KILOGRAM))
					.minus(fuelClimb.to(SI.KILOGRAM));

			rangeCruise = initialCruiseLength;
			totalMissionRange = Amount.valueOf(0.0, SI.METER);

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
					setMissionProfileStopped(Boolean.TRUE);
					System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
					if (cruiseMaxMachNumberErrorFlag == false) {
						System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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
						setMissionProfileStopped(Boolean.TRUE);
						System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
						if (cruiseMaxMachNumberErrorFlag == false ) {
							System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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

				fuelCruise =
						Amount.valueOf(
								fuelUsedPerStep.stream()
								.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
								.sum(),
								SI.KILOGRAM
								);
				
				meanFuelFlow = 
						fuelFlows.stream()
						.mapToDouble( f -> f)
						.average()
						.getAsDouble();
				meanCruiseMachNumber = 
						cruiseMissionMachNumber.stream()
						.mapToDouble( f -> f)
						.average()
						.getAsDouble();
				
				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				aircraftMassAtFirstDescentStart = 
						initialMissionMass
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
				fuelFirstDescent = theFirstDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);

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

				aircraftMassAtSecondClimbStart = initialMissionMass
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
				fuelSecondClimb = theSecondClimbCalculator.getClimbTotalFuelUsed();

				//--------------------------------------------------------------------
				// ALTERNATE CRUISE (AT MAX EFFICIENCY)
				aircraftMassAtAlternateCruiseStart = 
						initialMissionMass
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
						setMissionProfileStopped(Boolean.TRUE);
						System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
							System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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
							setMissionProfileStopped(Boolean.TRUE);
							System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) ALTERNATE CRUISE MACH NUMBER = 0.0. RETURNING ... ");
							return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
								System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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

					fuelAlternateCruise =
							Amount.valueOf(
									fuelUsedPerStepAlternateCruise.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);

					//--------------------------------------------------------------------
					// DESCENT (up to HOLDING altitude)
					aircraftMassAtSecondDescentStart = 
							initialMissionMass
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
					fuelSecondDescent = theSecondDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);

					//--------------------------------------------------------------------
					// HOLDING (BEST ENDURANCE)
					aircraftMassAtHoldingStart = 
							initialMissionMass
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
						setMissionProfileStopped(Boolean.TRUE);
						System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
						System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THE BEST HOLDING MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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
							setMissionProfileStopped(Boolean.TRUE);
							System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
							return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
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
							System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THE BEST HOLDING CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
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
					fuelHolding =
							Amount.valueOf(
									fuelUsedPerStepHolding.stream()
									.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
									.sum(),
									SI.KILOGRAM
									);

					//--------------------------------------------------------------------
					// DESCENT (up to LANDING altitude)
					aircraftMassAtThirdDescentStart = 
							initialMissionMass
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
					fuelThirdDescent = theThirdDescentCalculator.getTotalDescentFuelUsed().to(SI.KILOGRAM);

					//--------------------------------------------------------------------
					// LANDING
					aircraftMassAtLandingStart = 
							initialMissionMass
							.minus(fuelTakeOff.to(SI.KILOGRAM))
							.minus(fuelClimb.to(SI.KILOGRAM))
							.minus(fuelCruise.to(SI.KILOGRAM))
							.minus(fuelFirstDescent.to(SI.KILOGRAM))
							.minus(fuelSecondClimb.to(SI.KILOGRAM))
							.minus(fuelAlternateCruise.to(SI.KILOGRAM))
							.minus(fuelSecondDescent.to(SI.KILOGRAM))
							.minus(fuelHolding.to(SI.KILOGRAM))
							.minus(fuelThirdDescent.to(SI.KILOGRAM));

					theLandingCalculator = new LandingCalc(
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
					Amount<Duration> timeLanding = theLandingCalculator.getTime().get(theLandingCalculator.getTime().size()-1);
					fuelLanding = Amount.valueOf(
							timeLanding.to(NonSI.MINUTE).times(_landingFuelFlow).getEstimatedValue(),
							SI.KILOGRAM
							);

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
						setMissionProfileStopped(Boolean.TRUE);
						System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - PAYLOAD-RANGE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL TO ZERO, RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					}
				}
				
				//--------------------------------------------------------------------
				// RANGE
				List<Amount<Length>> rangeList = new ArrayList<>();

				rangeList.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
				rangeList.add(rangeList.get(0).plus(rangeTakeOff.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(1).plus(rangeClimb.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(2).plus(rangeCruise.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(3).plus(rangeFirstDescent.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(4).plus(rangeSecondClimb.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(5).plus(rangeAlternateCruise.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(6).plus(rangeSecondDescent.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(7).plus(rangeHolding.to(NonSI.NAUTICAL_MILE)));
				rangeList.add(rangeList.get(8).plus(rangeThirdDescent).to(NonSI.NAUTICAL_MILE));
				rangeList.add(rangeList.get(9).plus(rangeLanding).to(NonSI.NAUTICAL_MILE));

				totalMissionRange = rangeList.get(rangeList.size()-1);

				//--------------------------------------------------------------------
				// USED FUEL
				List<Amount<Mass>> fuelUsedList = new ArrayList<>();

				fuelUsedList.add(Amount.valueOf(0.0, SI.KILOGRAM));
				fuelUsedList.add(fuelUsedList.get(0).plus(fuelTakeOff.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(1).plus(fuelClimb.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(2).plus(fuelCruise.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(3).plus(fuelFirstDescent.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(4).plus(fuelSecondClimb.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(5).plus(fuelAlternateCruise.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(6).plus(fuelSecondDescent.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(7).plus(fuelHolding.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(8).plus(fuelThirdDescent.to(SI.KILOGRAM)));
				fuelUsedList.add(fuelUsedList.get(9).plus(fuelLanding.to(SI.KILOGRAM)));

				totalFuelUsed = fuelUsedList.get(fuelUsedList.size()-1);

			} 
			
			//.....................................................................
			// NEW CRUISE LENGTH
			Amount<Mass> deltaFuel = targetFuelMass.times(1-_fuelReserve).minus(totalFuelUsed);
			Amount<Length> deltaCruiseLength = 
					Amount.valueOf(
							(deltaFuel.doubleValue(SI.KILOGRAM)/meanFuelFlow)
							*meanCruiseMachNumber
							*_theOperatingConditions.getAtmosphereCruise().getSpeedOfSound()
							*60,
							SI.METER
							);
			newCruiseLength = initialCruiseLength.to(NonSI.NAUTICAL_MILE)
					.plus(deltaCruiseLength.to(NonSI.NAUTICAL_MILE)); 
			i++;
			
		} while ( Math.abs(
				(targetFuelMass.to(SI.KILOGRAM).minus(totalFuelUsed.to(SI.KILOGRAM)))
				.divide(targetFuelMass.to(SI.KILOGRAM))
				.times(100)
				.getEstimatedValue()
				)- (_fuelReserve*100)
				>= 0.01
				);
		
		//----------------------------------------------------------------------
		if(theFirstDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - FIRST DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theSecondDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - SECOND DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theThirdDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - THIRD DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		
		return totalMissionRange.to(NonSI.NAUTICAL_MILE).minus(_alternateCruiseLength.to(NonSI.NAUTICAL_MILE));
	}
	
	/******************************************************************************************
	 * Method that allows users to generate the Range array to be used in 
	 * Payload-Range plot.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRange(Amount<Velocity> vMC) {
		
		_rangeArray = new ArrayList<>();
		
		// RANGE AT MAX PAYLOAD
		_rangeAtMaxPayload = calcRangeAtGivenPayload(
				_maximumTakeOffMass.to(SI.KILOGRAM),
				_singlePassengerMass.to(SI.KILOGRAM).times(_theAircraft.getCabinConfiguration().getMaximumPassengerNumber()),
				vMC
				);
		_maxPayload = _singlePassengerMass.to(SI.KILOGRAM).times(_theAircraft.getCabinConfiguration().getMaximumPassengerNumber());
		_passengersNumberAtMaxPayload = _theAircraft.getCabinConfiguration().getMaximumPassengerNumber();
		_requiredMassAtMaxPayload = _maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(_operatingEmptyMass.to(SI.KILOGRAM))
				.minus(_singlePassengerMass.to(SI.KILOGRAM)
						.times(_theAircraft.getCabinConfiguration().getMaximumPassengerNumber()
								)
						);
		
		// RANGE AT DESIGN PAYLOAD
		_rangeAtDesignPayload = calcRangeAtGivenPayload(
				_maximumTakeOffMass.to(SI.KILOGRAM),
				_singlePassengerMass.to(SI.KILOGRAM).times(_theAircraft.getCabinConfiguration().getActualPassengerNumber()),
				vMC
				);
		_designPayload = _singlePassengerMass.to(SI.KILOGRAM).times(_theAircraft.getCabinConfiguration().getActualPassengerNumber());
		_passengersNumberAtDesignPayload = _theAircraft.getCabinConfiguration().getActualPassengerNumber();
		_requiredMassAtDesignPayload = _maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(_operatingEmptyMass.to(SI.KILOGRAM))
				.minus(_singlePassengerMass.to(SI.KILOGRAM)
						.times(_theAircraft.getCabinConfiguration().getActualPassengerNumber()
								)
						);
		
		// RANGE AT MAX FUEL
		_rangeAtMaxFuel = calcRangeAtGivenPayload(
				_maximumTakeOffMass.to(SI.KILOGRAM),
				_maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(_operatingEmptyMass.to(SI.KILOGRAM))
				.minus(_maxFuelMass.to(SI.KILOGRAM)),
				vMC
				);
		_payloadAtMaxFuel = 
				_maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(_operatingEmptyMass.to(SI.KILOGRAM))
				.minus(_maxFuelMass.to(SI.KILOGRAM));
		_passengersNumberAtMaxFuel = (int) Math.round(
				(_maximumTakeOffMass.to(SI.KILOGRAM)
						.minus(_operatingEmptyMass.to(SI.KILOGRAM))
						.minus(_maxFuelMass.to(SI.KILOGRAM)))
				.divide(_singlePassengerMass.to(SI.KILOGRAM))
				.getEstimatedValue()
				);
		
		// RANGE AT ZERO PAYLOAD
		if(_rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
			if(_rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
				if(_rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
					_rangeAtZeroPayload = calcRangeAtGivenPayload(
							_operatingEmptyMass.plus(_maxFuelMass),
							Amount.valueOf(0.0, SI.KILOGRAM),
							vMC
							);
				}
				else {
					_rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}
			}
			else {
				_rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
			}
		}
		else {
			_rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		}
		_takeOffMassZeroPayload = _operatingEmptyMass.plus(_maxFuelMass);
		
		// POINT 1
		_rangeArray.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		_rangeArray.add(_rangeAtMaxPayload.to(NonSI.NAUTICAL_MILE));
		// POINT 3
		_rangeArray.add(_rangeAtDesignPayload.to(NonSI.NAUTICAL_MILE));
		// POINT 4
		_rangeArray.add(_rangeAtMaxFuel.to(NonSI.NAUTICAL_MILE));
		// POINT 4
		_rangeArray.add(_rangeAtZeroPayload.to(NonSI.NAUTICAL_MILE));
		
		//--------------------------------------------------------------------------------------
		// PAYLOAD ARRAY (both conditions)
		_payloadArray = new ArrayList<Double>();
		
		// POINT 1
		_payloadArray.add(_passengersNumberAtMaxPayload.doubleValue());
		// POINT 2
		_payloadArray.add(_passengersNumberAtMaxPayload.doubleValue());
		// POINT 3
		_payloadArray.add(_passengersNumberAtDesignPayload.doubleValue());
		// POINT 4
		_payloadArray.add(_passengersNumberAtMaxFuel.doubleValue());
		// POINT 5
		_payloadArray.add(0.0);

	}

	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, for the best range
	 * Mach and the current one, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeChart(String subFolderPath){

		double rangeDoubleArray_Imperial[] = MyArrayUtils.convertListOfAmountTodoubleArray(_rangeArray);
		double rangeDoubleArray_SI[] = 
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_rangeArray.stream()
						.map(x -> x.to(SI.KILOMETER))
						.collect(Collectors.toList())
						);
		double payloadDoubleArray[]= MyArrayUtils.convertToDoublePrimitive(_payloadArray);

		String[] legendValue_SI = new String[1];
		String[] legendValue_Imperial = new String[1];
		legendValue_SI[0] = 
				"Altitude = "
				+ _theOperatingConditions.getAltitudeCruise().to(SI.METER); 
		legendValue_Imperial[0] = 
				"Altitude = "
				+ _theOperatingConditions.getAltitudeCruise().to(NonSI.FOOT);

		MyChartToFileUtils.plot(
				rangeDoubleArray_SI, payloadDoubleArray,    // array to plot
				null, null, 0.0, null,					    // axis with limits
				"Range", "Payload", "km", "No. Pass",	    // label with unit
				legendValue_SI,								// legend
				subFolderPath, "Payload-Range_SI",          // output informations
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance());		    
		MyChartToFileUtils.plot(
				rangeDoubleArray_Imperial, payloadDoubleArray,    // array to plot
				null, null, 0.0, null,					          // axis with limits
				"Range", "Payload", "nmi", "No. Pass",	          // label with unit
				legendValue_Imperial,						      // legend
				subFolderPath, "Payload-Range_IMPERIAL",          // output informations
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance());         
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\t\tRANGE AT MAX PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + _rangeAtMaxPayload + "\n")
				.append("\t\t\tMax take-off mass = " + _maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + _maxPayload + "\n")
				.append("\t\t\tPassengers number = " + _passengersNumberAtMaxPayload + "\n")
				.append("\t\t\tFuel mass required= " + _requiredMassAtMaxPayload + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT DESIGN PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + _rangeAtDesignPayload + "\n")
				.append("\t\t\tMax take-off mass = " + _maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + _designPayload + "\n")
				.append("\t\t\tPassengers number = " + _passengersNumberAtDesignPayload + "\n")
				.append("\t\t\tFuel mass required= " + _requiredMassAtDesignPayload + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT MAX FUEL\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + _rangeAtMaxFuel + "\n")
				.append("\t\t\tMax take-off mass = " + _maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + _payloadAtMaxFuel + "\n")
				.append("\t\t\tPassengers number = " + _passengersNumberAtMaxFuel + "\n")
				.append("\t\t\tFuel mass required = " + _maxFuelMass + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT ZERO PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + _rangeAtZeroPayload + "\n")
				.append("\t\t\tMax take-off mass = " + _takeOffMassZeroPayload + "\n")
				.append("\t\t\tPayload mass = " + 0.0 + " kg \n")
				.append("\t\t\tPassengers number = " + 0.0 + "\n")
				.append("\t\t\tFuel mass required= " + _maxFuelMass + "\n");
		
		sb.append("\t-------------------------------------\n");
		
		return sb.toString();
	}
	//----------------------------------------------------------------------------
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

	public Amount<Length> getTakeOffMissionAltitude() {
		return _takeOffMissionAltitude;
	}

	public void setTakeOffMissionAltitude(Amount<Length> _takeOffMissionAltitude) {
		this._takeOffMissionAltitude = _takeOffMissionAltitude;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}

	public Amount<Mass> getMaxFuelMass() {
		return _maxFuelMass;
	}

	public void setMaxFuelMass(Amount<Mass> _maxFuelMass) {
		this._maxFuelMass = _maxFuelMass;
	}

	public Amount<Mass> getSinglePassengerMass() {
		return _singlePassengerMass;
	}

	public void setSinglePassengerMass(Amount<Mass> _singlePassengerMass) {
		this._singlePassengerMass = _singlePassengerMass;
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

	public Amount<?> get_cLAlphaTakeOff() {
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

	public Amount<Length> getRangeAtMaxPayload() {
		return _rangeAtMaxPayload;
	}

	public void setRangeAtMaxPayload(Amount<Length> _rangeAtMaxPayload) {
		this._rangeAtMaxPayload = _rangeAtMaxPayload;
	}

	public Amount<Length> getRangeAtDesignPayload() {
		return _rangeAtDesignPayload;
	}

	public void setRangeAtDesignPayload(Amount<Length> _rangeAtDesignPayload) {
		this._rangeAtDesignPayload = _rangeAtDesignPayload;
	}

	public Amount<Length> getRangeAtMaxFuel() {
		return _rangeAtMaxFuel;
	}

	public void setRangeAtMaxFuel(Amount<Length> _rangeAtMaxFuel) {
		this._rangeAtMaxFuel = _rangeAtMaxFuel;
	}

	public List<Amount<Length>> getRangeArray() {
		return _rangeArray;
	}

	public void setRangeArray(List<Amount<Length>> _rangeArray) {
		this._rangeArray = _rangeArray;
	}

	public List<Double> getPayloadArray() {
		return _payloadArray;
	}

	public void setPayloadArray(List<Double> _payloadArray) {
		this._payloadArray = _payloadArray;
	}

	public Amount<Mass> getMaxPayload() {
		return _maxPayload;
	}

	public void setMaxPayload(Amount<Mass> _maxPayload) {
		this._maxPayload = _maxPayload;
	}

	public Amount<Mass> getDesignPayload() {
		return _designPayload;
	}

	public void setDesignPayload(Amount<Mass> _designPayload) {
		this._designPayload = _designPayload;
	}

	public Amount<Mass> getPayloadAtMaxFuel() {
		return _payloadAtMaxFuel;
	}

	public void setPayloadAtMaxFuel(Amount<Mass> _payloadAtMaxFuel) {
		this._payloadAtMaxFuel = _payloadAtMaxFuel;
	}

	public Integer getPassengersNumberAtMaxPayload() {
		return _passengersNumberAtMaxPayload;
	}

	public void setPassengersNumberAtMaxPayload(Integer _passengersNumberAtMaxPayload) {
		this._passengersNumberAtMaxPayload = _passengersNumberAtMaxPayload;
	}

	public Integer getPassengersNumberAtDesignPayload() {
		return _passengersNumberAtDesignPayload;
	}

	public void setPassengersNumberAtDesignPayload(Integer _passengersNumberAtDesignPayload) {
		this._passengersNumberAtDesignPayload = _passengersNumberAtDesignPayload;
	}

	public Integer getPassengersNumberAtMaxFuel() {
		return _passengersNumberAtMaxFuel;
	}

	public void setPassengersNumberAtMaxFuel(Integer _passengersNumberAtMaxFuel) {
		this._passengersNumberAtMaxFuel = _passengersNumberAtMaxFuel;
	}

	public Amount<Mass> getRequiredMassAtMaxPayload() {
		return _requiredMassAtMaxPayload;
	}

	public void setRequiredMassAtMaxPayload(Amount<Mass> _requiredMassAtMaxPayload) {
		this._requiredMassAtMaxPayload = _requiredMassAtMaxPayload;
	}

	public Amount<Mass> getRequiredMassAtDesignPayload() {
		return _requiredMassAtDesignPayload;
	}

	public void setRequiredMassAtDesignPayload(Amount<Mass> _requiredMassAtDesignPayload) {
		this._requiredMassAtDesignPayload = _requiredMassAtDesignPayload;
	}

	public Amount<Length> getRangeAtZeroPayload() {
		return _rangeAtZeroPayload;
	}

	public void setRangeAtZeroPayload(Amount<Length> _rangeAtZeroPayload) {
		this._rangeAtZeroPayload = _rangeAtZeroPayload;
	}

	public Amount<Mass> getTakeOffMassZeroPayload() {
		return _takeOffMassZeroPayload;
	}

	public void setTakeOffMassZeroPayload(Amount<Mass> _takeOffMassZeroPayload) {
		this._takeOffMassZeroPayload = _takeOffMassZeroPayload;
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

	public boolean isCalculateSFCCruise() {
		return _calculateSFCCruise;
	}

	public void setCalculateSFCCruise(boolean _calculateSFCCruise) {
		this._calculateSFCCruise = _calculateSFCCruise;
	}

	public boolean isCalculateSFCAlternateCruise() {
		return _calculateSFCAlternateCruise;
	}

	public void setCalculateSFCAlternateCruise(boolean _calculateSFCAlternateCruise) {
		this._calculateSFCAlternateCruise = _calculateSFCAlternateCruise;
	}

	public boolean isCalculateSFCHolding() {
		return _calculateSFCHolding;
	}

	public void setCalculateSFCHolding(boolean _calculateSFCHolding) {
		this._calculateSFCHolding = _calculateSFCHolding;
	}

	public Boolean getMissionProfileStopped() {
		return _missionProfileStopped;
	}

	public void setMissionProfileStopped(Boolean _missionProfileStopped) {
		this._missionProfileStopped = _missionProfileStopped;
	}

}