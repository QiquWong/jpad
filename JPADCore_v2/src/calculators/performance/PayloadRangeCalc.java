package calculators.performance;

import java.util.ArrayList;
import java.util.List;

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
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class PayloadRangeCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// INUPT:
	//............................................................................................
	public static final int numberOfStepClimb = 10;
	public static final int numberOfStepCruise = 30;
	public static final int numberOfStepDescent = 10;
	public static final int numberOfStepAlternateCruise = 10; 
	public static final int numberOfStepHolding = 10; 
	
	// Geomtrical data (from Aircraft Object)
	private Aircraft theAircraft;
	
	// Mission profile info
	private Amount<Length> missionRange;
	private Amount<Length> alternateCruiseAltitude;
	private Amount<Length> alternateCruiseRange;
	private Amount<Length> holdingAltitude;
	private Amount<Duration> holdingDuration;
	private double fuelReserve;
	private Amount<Mass> firstGuessInitialFuelMass;
	
	// Operating Conditions
	private OperatingConditions theOperatingConditions;
	
	// Weights
	private Amount<Mass> maximumTakeOffMass;
	private Amount<Mass> operatingEmptyMass;
	private Amount<Mass> singlePassengerMass;
	private Amount<Mass> maxFuelMass;
	private Amount<Mass> maxPayload;
	private int deisgnPassengersNumber;
	
	// Aerodynamics
	private double cLmaxClean;
	private double cLmaxTakeOff;
	private Amount<?> cLAlphaTakeOff;
	private double cLZeroTakeOff;
	private double cLmaxLanding;
	private Amount<?> cLAlphaLanding;
	private double cLZeroLanding;
	private double[] polarCLTakeOff;
	private double[] polarCDTakeOff;
	private double[] polarCLClimb;
	private double[] polarCDClimb;
	private double[] polarCLCruise;
	private double[] polarCDCruise;
	private double[] polarCLLanding;
	private double[] polarCDLanding;
	
	// Take-Off input data
	private Amount<Velocity> windSpeed;
	private MyInterpolatingFunction mu;
	private MyInterpolatingFunction muBrake;
	private Amount<Duration> dtHold;
	private Amount<Angle> alphaGround;
	private Amount<Length> obstacleTakeOff;
	private double kRotation;
	private double kCLmaxTakeOff;
	private double dragDueToEnigneFailure;
	private double kAlphaDot;
	private double alphaDotInitial;

	// Landing input data
	private Amount<Length> obstacleLanding;
	private Amount<Angle> approachAngle;
	private double kCLmaxLanding;
	private double kApproach;
	private double kFlare;
	private double kTouchDown;
	private Amount<Duration> freeRollDuration;
	
	// Climb input data
	private Amount<Velocity> climbSpeed;
	
	// Descent input data
	private Amount<Velocity> speedDescentCAS;
	private Amount<Velocity> rateOfDescent;
	
	// Calibration factors - Thrust
	double takeOffCalibrationFactorThrust;
	double aprCalibrationFactorThrust;
	double climbCalibrationFactorThrust;
	private double continuousCalibrationFactorThrust;
	double cruiseCalibrationFactorThrust;
	double flightIdleCalibrationFactorThrust;
	double groundIdleCalibrationFactorThrust;
	
	// Calibration factors - SFC
	double takeOffCalibrationFactorSFC;
	double aprCalibrationFactorSFC;
	double climbCalibrationFactorSFC;
	double cruiseCalibrationFactorSFC;
	double flightIdleCalibrationFactorSFC;
	double groundIdleCalibrationFactorSFC;
	
	//............................................................................................
	// Output:
	private Boolean missionProfileStopped = Boolean.FALSE;
	
	//----------------------------------------------------------------------
	// QUANTITES TO BE MONITORED DURING MISSION PROFILE ANALYSIS
	//----------------------------------------------------------------------
	// TAKE-OFF
	private List<Amount<Length>> rangeTakeOff;
	private List<Amount<Mass>> fuelUsedTakeOff;
	
	//......................................................................
	// CLIMB
	private List<Amount<Length>> rangeClimb;
	private List<Amount<Mass>> fuelUsedClimb;
	
	//......................................................................
	// CRUISE
	private List<Amount<Length>> rangeCruise;
	private List<Amount<Mass>> fuelUsedCruise;
	
	//......................................................................
	// FIRST DESCENT
	private List<Amount<Length>> rangeFirstDescent;
	private List<Amount<Mass>> fuelUsedFirstDescent;
	
	//......................................................................
	// SECOND CLIMB
	private List<Amount<Length>> rangeSecondClimb;
	private List<Amount<Mass>> fuelUsedSecondClimb;
	
	//......................................................................
	// ALTERNATE CRUISE
	private List<Amount<Length>> rangeAlternateCruise;
	private List<Amount<Mass>> fuelUsedAlternateCruise;
	
	//......................................................................
	// SECOND DESCENT
	private List<Amount<Length>> rangeSecondDescent;
	private List<Amount<Mass>> fuelUsedSecondDescent;
	
	//......................................................................
	// HOLDING
	private List<Amount<Length>> rangeHolding;
	private List<Amount<Mass>> fuelUsedHolding;
	
	//......................................................................
	// LANDING
	private List<Amount<Length>> rangeLanding;
	private List<Amount<Mass>> fuelUsedLanding;
	
	// TO EVALUATE:
	private Amount<Length> rangeAtMaxPayload;
	private Amount<Length> rangeAtDesignPayload;
	private Amount<Length> rangeAtMaxFuel;	
	private Amount<Length> rangeAtZeroPayload;
	private Amount<Mass> takeOffMassZeroPayload;
	private Amount<Mass> designPayload;
	private Amount<Mass> payloadAtMaxFuel;
	private Integer passengersNumberAtMaxPayload;
	private Integer passengersNumberAtDesignPayload;
	private Integer passengersNumberAtMaxFuel;
	private Amount<Mass> requiredMassAtMaxPayload;
	private Amount<Mass> requiredMassAtDesignPayload;
	
	private List<Amount<Length>> rangeArray;
	private List<Double> payloadPassengerNumberArray;
	private List<Amount<Mass>> payloadMassArray;
	private List<Amount<Mass>> takeOffMassArray;
	
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public PayloadRangeCalc(
			Aircraft theAircraft, 
			OperatingConditions theOperatingConditions,
			Amount<Length> missionRange,
			Amount<Length> alternateCruiseRange,
			Amount<Length> alternateCruiseAltitude,
			Amount<Duration> holdingDuration,
			Amount<Length> holdingAltitude,
			double fuelReserve,
			Amount<Mass> firstGuessInitialFuelMass,
			Amount<Mass> maximumTakeOffMass,
			Amount<Mass> operatingEmptyMass, 
			Amount<Mass> singlePassengerMass,
			Amount<Mass> maximumFuelMass,
			Amount<Mass> maximumPayload,
			int deisgnPassengersNumber,
			double cLmaxClean, 
			double cLmaxTakeOff,
			Amount<?> cLAlphaTakeOff, 
			double cLZeroTakeOff,
			double cLmaxLanding,
			Amount<?> cLAlphaLanding,
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
			double kCLmaxTakeOff,
			double dragDueToEnigneFailure,
			double kAlphaDot,
			double alphaDotInitial,
			Amount<Length> obstacleLanding,
			Amount<Angle> approachAngle, 
			double kCLmaxLanding,
			double kApproach, 
			double kFlare, 
			double kTouchDown,
			Amount<Duration> freeRollDuration,
			Amount<Velocity> climbSpeed,
			Amount<Velocity> speedDescentCAS,
			Amount<Velocity> rateOfDescent,
			double takeOffCalibrationFactorThrust,
			double climbCalibrationFactorThrust,
			double continuousCalibrationFactorThrust,
			double cruiseCalibrationFactorThrust,
			double flightIdleCalibrationFactorThrust,
			double groundIdleCalibrationFactorThrust,
			double takeOffCalibrationFactorSFC,
			double climbCalibrationFactorSFC,
			double cruiseCalibrationFactorSFC,
			double flightIdleCalibrationFactorSFC,
			double groundIdleCalibrationFactorSFC
			) {
		
		this.theAircraft = theAircraft;
		this.theOperatingConditions = theOperatingConditions;
		this.missionRange = missionRange;
		this.alternateCruiseRange = alternateCruiseRange;
		this.alternateCruiseAltitude = alternateCruiseAltitude;
		this.holdingAltitude = holdingAltitude;
		this.holdingDuration = holdingDuration;
		this.fuelReserve = fuelReserve;
		this.firstGuessInitialFuelMass = firstGuessInitialFuelMass;
		this.maximumTakeOffMass = maximumTakeOffMass;
		this.operatingEmptyMass = operatingEmptyMass;
		this.singlePassengerMass = singlePassengerMass;
		this.maxFuelMass = maximumFuelMass;
		this.maxPayload = maximumPayload;
		this.deisgnPassengersNumber = deisgnPassengersNumber;
		this.cLmaxClean = cLmaxClean;
		this.cLmaxTakeOff = cLmaxTakeOff;
		this.cLAlphaTakeOff = cLAlphaTakeOff;
		this.cLZeroTakeOff = cLZeroTakeOff;
		this.cLAlphaLanding = cLAlphaLanding;
		this.cLmaxLanding = cLmaxLanding;
		this.cLZeroLanding = cLZeroLanding;
		this.polarCLTakeOff = polarCLTakeOff;
		this.polarCDTakeOff = polarCDTakeOff;
		this.polarCLClimb = polarCLClimb;
		this.polarCDClimb = polarCDClimb;
		this.polarCLCruise = polarCLCruise;
		this.polarCDCruise = polarCDCruise;
		this.polarCLLanding = polarCLLanding;
		this.polarCDLanding = polarCDLanding;
		this.windSpeed = windSpeed;
		this.mu = mu;
		this.muBrake = muBrake;
		this.dtHold = dtHold;
		this.alphaGround = alphaGround;
		this.obstacleTakeOff = obstacleTakeOff;
		this.kRotation = kRotation;
		this.kCLmaxTakeOff = kCLmaxTakeOff;
		this.dragDueToEnigneFailure = dragDueToEnigneFailure;
		this.kAlphaDot = kAlphaDot;
		this.alphaDotInitial = alphaDotInitial;
		this.obstacleLanding = obstacleLanding;
		this.approachAngle = approachAngle;
		this.kCLmaxLanding = kCLmaxLanding;
		this.kApproach = kApproach;
		this.kFlare = kFlare;
		this.kTouchDown = kTouchDown;
		this.freeRollDuration = freeRollDuration;
		this.climbSpeed = climbSpeed;
		this.speedDescentCAS = speedDescentCAS;
		this.rateOfDescent = rateOfDescent;
		this.takeOffCalibrationFactorThrust = takeOffCalibrationFactorThrust;
		this.climbCalibrationFactorThrust = climbCalibrationFactorThrust;
		this.continuousCalibrationFactorThrust = continuousCalibrationFactorThrust;
		this.cruiseCalibrationFactorThrust = cruiseCalibrationFactorThrust;
		this.flightIdleCalibrationFactorThrust = flightIdleCalibrationFactorThrust;
		this.groundIdleCalibrationFactorThrust = groundIdleCalibrationFactorThrust;
		this.takeOffCalibrationFactorSFC = takeOffCalibrationFactorSFC;
		this.climbCalibrationFactorSFC = climbCalibrationFactorSFC;
		this.cruiseCalibrationFactorSFC = cruiseCalibrationFactorSFC;
		this.flightIdleCalibrationFactorSFC = flightIdleCalibrationFactorSFC;
		this.groundIdleCalibrationFactorSFC = groundIdleCalibrationFactorSFC;
		
	}

	//--------------------------------------------------------------------------------------------
	// METHODS:

	private void initializePhasesLists(boolean isCruiseLoop, boolean isAlternateCruiseLoop) {

		if(isCruiseLoop == false && isAlternateCruiseLoop == false) {
			//----------------------------------------------------------------------
			// TAKE-OFF
			this.rangeTakeOff = new ArrayList<>();
			this.fuelUsedTakeOff = new ArrayList<>();
			//......................................................................
			// CLIMB
			this.rangeClimb = new ArrayList<>();
			this.fuelUsedClimb = new ArrayList<>();
		}

		if (isAlternateCruiseLoop == false) {
			//......................................................................
			// CRUISE
			this.rangeCruise = new ArrayList<>();
			this.fuelUsedCruise = new ArrayList<>();
			//......................................................................
			// FIRST DESCENT
			this.rangeFirstDescent = new ArrayList<>();
			this.fuelUsedFirstDescent = new ArrayList<>();
			//......................................................................
			// SECOND CLIMB
			this.rangeSecondClimb = new ArrayList<>();
			this.fuelUsedSecondClimb = new ArrayList<>();
		}
		//......................................................................
		// ALTERNATE CRUISE
		this.rangeAlternateCruise = new ArrayList<>();
		this.fuelUsedAlternateCruise = new ArrayList<>();
		//......................................................................
		// SECOND DESCENT
		this.rangeSecondDescent = new ArrayList<>();
		this.fuelUsedSecondDescent = new ArrayList<>();
		//......................................................................
		// HOLDING
		this.rangeHolding = new ArrayList<>();
		this.fuelUsedHolding = new ArrayList<>();
		//......................................................................
		// LANDING
		this.rangeLanding = new ArrayList<>();
		this.fuelUsedLanding = new ArrayList<>();
	}
	
	/******************************************************************************************
	 * Method that allows users to generate the Range array to be used in 
	 * Payload-Range plot.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRange(Amount<Velocity> vMC) {
		
		rangeArray = new ArrayList<>();
		
		// RANGE AT MAX PAYLOAD
		rangeAtMaxPayload = calcRangeAtGivenPayload(
				maximumTakeOffMass.to(SI.KILOGRAM),
				maxPayload.to(SI.KILOGRAM),
				vMC
				);
		passengersNumberAtMaxPayload = (int) (maxPayload.doubleValue(SI.KILOGRAM)/singlePassengerMass.doubleValue(SI.KILOGRAM));
		requiredMassAtMaxPayload = maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(operatingEmptyMass.to(SI.KILOGRAM))
				.minus(maxPayload.to(SI.KILOGRAM));
		
		// RANGE AT DESIGN PAYLOAD
		rangeAtDesignPayload = calcRangeAtGivenPayload(
				maximumTakeOffMass.to(SI.KILOGRAM),
				singlePassengerMass.to(SI.KILOGRAM).times(theAircraft.getCabinConfiguration().getDesignPassengerNumber()),
				vMC
				);
		designPayload = singlePassengerMass.to(SI.KILOGRAM).times(theAircraft.getCabinConfiguration().getDesignPassengerNumber());
		passengersNumberAtDesignPayload = theAircraft.getCabinConfiguration().getDesignPassengerNumber();
		requiredMassAtDesignPayload = maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(operatingEmptyMass.to(SI.KILOGRAM))
				.minus(singlePassengerMass.to(SI.KILOGRAM)
						.times(theAircraft.getCabinConfiguration().getDesignPassengerNumber()
								)
						);
		
		// RANGE AT MAX FUEL
		rangeAtMaxFuel = calcRangeAtGivenPayload(
				maximumTakeOffMass.to(SI.KILOGRAM),
				maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(operatingEmptyMass.to(SI.KILOGRAM))
				.minus(maxFuelMass.to(SI.KILOGRAM)),
				vMC
				);
		payloadAtMaxFuel = maximumTakeOffMass.to(SI.KILOGRAM)
				.minus(operatingEmptyMass.to(SI.KILOGRAM))
				.minus(maxFuelMass.to(SI.KILOGRAM)
						);
		passengersNumberAtMaxFuel = (int) Math.round(
				(maximumTakeOffMass.to(SI.KILOGRAM)
						.minus(operatingEmptyMass.to(SI.KILOGRAM))
						.minus(maxFuelMass.to(SI.KILOGRAM)))
				.divide(singlePassengerMass.to(SI.KILOGRAM))
				.getEstimatedValue()
				);
		
		// RANGE AT ZERO PAYLOAD
		if(rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
			if(rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
				if(rangeAtMaxPayload.doubleValue(NonSI.NAUTICAL_MILE)!= 0.0) {
					rangeAtZeroPayload = calcRangeAtGivenPayload(
							operatingEmptyMass.plus(getMaxFuelMass()),
							Amount.valueOf(0.0, SI.KILOGRAM),
							vMC
							);
				}
				else {
					rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}
			}
			else {
				rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
			}
		}
		else {
			rangeAtZeroPayload = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		}
		takeOffMassZeroPayload = operatingEmptyMass.plus(maxFuelMass);
		
		// POINT 1
		rangeArray.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		// POINT 2
		rangeArray.add(rangeAtMaxPayload.to(NonSI.NAUTICAL_MILE));
		// POINT 3
		rangeArray.add(rangeAtDesignPayload.to(NonSI.NAUTICAL_MILE));
		// POINT 4
		rangeArray.add(rangeAtMaxFuel.to(NonSI.NAUTICAL_MILE));
		// POINT 4
		rangeArray.add(rangeAtZeroPayload.to(NonSI.NAUTICAL_MILE));
		
		//--------------------------------------------------------------------------------------
		// PAYLOAD ARRAY (PAX NUMBER) 
		payloadPassengerNumberArray = new ArrayList<Double>();
		
		// POINT 1
		payloadPassengerNumberArray.add(passengersNumberAtMaxPayload.doubleValue());
		// POINT 2
		payloadPassengerNumberArray.add(passengersNumberAtMaxPayload.doubleValue());
		// POINT 3
		payloadPassengerNumberArray.add(passengersNumberAtDesignPayload.doubleValue());
		// POINT 4
		payloadPassengerNumberArray.add(passengersNumberAtMaxFuel.doubleValue());
		// POINT 5
		payloadPassengerNumberArray.add(0.0);

		//--------------------------------------------------------------------------------------
		// PAYLOAD ARRAY (MASS)
		payloadMassArray = new ArrayList<Amount<Mass>>();

		// POINT 1
		payloadMassArray.add(maxPayload);
		// POINT 2
		payloadMassArray.add(maxPayload);
		// POINT 3
		payloadMassArray.add(designPayload);
		// POINT 4
		payloadMassArray.add(getPayloadAtMaxFuel());
		// POINT 5
		payloadMassArray.add(Amount.valueOf(0.0, SI.KILOGRAM));
		
		//--------------------------------------------------------------------------------------
		// TAKE-OFF MASS ARRAY
		takeOffMassArray = new ArrayList<Amount<Mass>>();

		// POINT 1
		takeOffMassArray.add(maxPayload.to(SI.KILOGRAM));
		// POINT 2
		takeOffMassArray.add(maxPayload.to(SI.KILOGRAM).plus(requiredMassAtMaxPayload.to(SI.KILOGRAM)));
		// POINT 3
		takeOffMassArray.add(designPayload.to(SI.KILOGRAM).plus(requiredMassAtDesignPayload.to(SI.KILOGRAM)));
		// POINT 4
		takeOffMassArray.add(payloadAtMaxFuel.to(SI.KILOGRAM).plus(maxFuelMass.to(SI.KILOGRAM)));
		// POINT 5
		takeOffMassArray.add(maxFuelMass.to(SI.KILOGRAM));

		
	}
	
	private Amount<Length> calcRangeAtGivenPayload(
			Amount<Mass> maxTakeOffMassCurrent,
			Amount<Mass> payloadMass,
			Amount<Velocity> vMC
			) {	

		//----------------------------------------------------------------------
		// ERROR FLAGS
		boolean cruiseMaxMachNumberErrorFlag = false;
		boolean alternateCruiseBestMachNumberErrorFlag = false;
		boolean holdingBestMachNumberErrorFlag = false;
		
		//----------------------------------------------------------------------
		// PHASE CALCULATORS
		TakeOffCalc theTakeOffCalculator = null;
		ClimbCalc theClimbCalculator = null;
		ClimbCalc theSecondClimbCalculator = null;
		DescentCalc theFirstDescentCalculator = null;
		DescentCalc theSecondDescentCalculator = null;
		LandingCalc theLandingCalculator = null;
		
		//----------------------------------------------------------------------
		// ITERATION START ...
		//----------------------------------------------------------------------
		Amount<Mass> initialMissionMass = maxTakeOffMassCurrent.to(SI.KILOGRAM); 
		
		Amount<Mass> targetFuelMass = 
				maxTakeOffMassCurrent.to(SI.KILOGRAM)
				.minus(operatingEmptyMass.to(SI.KILOGRAM))
				.minus(payloadMass.to(SI.KILOGRAM));

		// first guess values
		Amount<Length> currentCruiseRange = missionRange.to(NonSI.NAUTICAL_MILE);
		Amount<Length> currentAlternateCruiseRange = alternateCruiseRange.to(NonSI.NAUTICAL_MILE);
		int i=0;
		
		Amount<Mass> totalFuelUsed = Amount.valueOf(0.0, SI.KILOGRAM);
		
		do {
			
			initializePhasesLists(false, false);

			if(i > 100) {
				System.err.println("WARNING: (PAYLOAD-RANGE) MAXIMUM NUMBER OF ITERATION REACHED");
				break;
			}

			//--------------------------------------------------------------------
			// TAKE-OFF
			Amount<Length> wingToGroundDistance = 
					theAircraft.getFuselage().getHeightFromGround()
					.plus(theAircraft.getFuselage().getSectionCylinderHeight().divide(2))
					.plus(theAircraft.getWing().getZApexConstructionAxes()
							.plus(theAircraft.getWing().getSemiSpan()
									.times(
											Math.sin(
													theAircraft.getWing()	
													.getDihedralMean()
													.doubleValue(SI.RADIAN)
													)
											)
									)
							);

			theTakeOffCalculator = new TakeOffCalc(
					theAircraft.getWing().getAspectRatio(),
					theAircraft.getWing().getSurfacePlanform(),
					theAircraft.getFuselage().getUpsweepAngle(),
					theAircraft.getPowerPlant(),
					polarCLTakeOff,
					polarCDTakeOff,
					theOperatingConditions.getAltitudeTakeOff(),
					theOperatingConditions.getDeltaTemperatureTakeOff(),
					maximumTakeOffMass,
					dtHold,
					kCLmaxTakeOff,
					kRotation,
					alphaDotInitial,
					dragDueToEnigneFailure, 
					1.0, // throttle take-off (100%) 
					kAlphaDot,
					mu,
					muBrake,
					obstacleTakeOff, 
					wingToGroundDistance,
					windSpeed,
					alphaGround,
					cLmaxTakeOff,
					cLZeroTakeOff, 
					cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
					takeOffCalibrationFactorThrust, 
					1.0, // apr thrust correction factor (not needed)
					groundIdleCalibrationFactorThrust, 
					takeOffCalibrationFactorSFC, 
					1.0, // apr SFC correction factor (not needed)
					groundIdleCalibrationFactorSFC,
					1.0, // take-off NOx correction factor (not needed)
					1.0, // take-off CO correction factor (not needed)
					1.0, // take-off HC correction factor (not needed)
					1.0, // take-off Soot correction factor (not needed)
					1.0, // take-off CO2 correction factor (not needed)
					1.0, // take-off SOx correction factor (not needed)
					1.0, // take-off H2O correction factor (not needed)
					1.0, // apr NOx correction factor (not needed)
					1.0, // apr CO correction factor (not needed)
					1.0, // apr HC correction factor (not needed)
					1.0, // apr Soot correction factor (not needed)
					1.0, // apr CO2 correction factor (not needed)
					1.0, // apr SOx correction factor (not needed)
					1.0, // apr H2O correction factor (not needed)
					1.0, // gidl NOx correction factor (not needed)
					1.0, // gidl CO correction factor (not needed)
					1.0, // gidl HC correction factor (not needed)
					1.0, // gidl Soot correction factor (not needed)
					1.0, // gidl CO2 correction factor (not needed)
					1.0, // gidl SOx correction factor (not needed)
					1.0 // gidl H2O correction factor (not needed)
					);

			theTakeOffCalculator.calculateTakeOffDistanceODE(null, false, false, vMC);
			
			rangeTakeOff.addAll(theTakeOffCalculator.getGroundDistance());			
			fuelUsedTakeOff.addAll(theTakeOffCalculator.getFuelUsed());
			
			//--------------------------------------------------------------------
			// CLIMB 
			Amount<Mass> initialMassClimb = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);
			
			theClimbCalculator = new ClimbCalc(
					theAircraft,
					theOperatingConditions,
					cLmaxClean, 
					polarCLClimb,
					polarCDClimb,
					climbSpeed,
					dragDueToEnigneFailure,
					climbCalibrationFactorThrust,
					continuousCalibrationFactorThrust,
					climbCalibrationFactorSFC,
					1.0, // climb NOx correction factor (not needed)
					1.0, // climb CO correction factor (not needed)
					1.0, // climb HC correction factor (not needed)
					1.0, // climb Soot correction factor (not needed)
					1.0, // climb CO2 correction factor (not needed)
					1.0, // climb SOx correction factor (not needed)
					1.0 // climb H2O correction factor (not needed)
					);
			theClimbCalculator.setNumberOfStepClimb(numberOfStepClimb);
			 
			theClimbCalculator.calculateClimbPerformance(
					initialMassClimb,
					initialMassClimb,
					obstacleTakeOff.to(SI.METER),
					theOperatingConditions.getAltitudeCruise().to(SI.METER),
					false,
					false
					);

			rangeClimb.addAll(theClimbCalculator.getRangeClimb());			
			fuelUsedClimb.addAll(theClimbCalculator.getFuelUsedClimb());
			
			//--------------------------------------------------------------------
			// CRUISE (CONSTANT MACH AND ALTITUDE)
			Amount<Mass> initialMassCruise = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
					- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			initializePhasesLists(true, false);

			List<Amount<Length>> cruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
					MyArrayUtils.linspace(
							0.0,
							currentCruiseRange.doubleValue(SI.METER),
							numberOfStepCruise
							),
					SI.METER
					);

			Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(theAircraft.getWing());

			int nPointSpeed = 1000;
			List<Amount<Velocity>> speedArray = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									SpeedCalc.calculateSpeedStall(
											theOperatingConditions.getAltitudeCruise(),
											theOperatingConditions.getDeltaTemperatureCruise(),
											initialMassCruise,
											theAircraft.getWing().getSurfacePlanform(),
											MyArrayUtils.getMax(polarCLClimb)
											).doubleValue(SI.METERS_PER_SECOND),
									SpeedCalc.calculateTAS(
											1.0,
											theOperatingConditions.getAltitudeCruise(),
											theOperatingConditions.getDeltaTemperatureCruise()
											).doubleValue(SI.METERS_PER_SECOND),
									nPointSpeed
									),
							SI.METERS_PER_SECOND
							);

			List<DragMap> dragList = new ArrayList<>();
			dragList.add(
					DragCalc.calculateDragAndPowerRequired(
							theOperatingConditions.getAltitudeCruise(),
							theOperatingConditions.getDeltaTemperatureCruise(),
							initialMassCruise,
							speedArray,
							theAircraft.getWing().getSurfacePlanform(),
							MyArrayUtils.getMax(polarCLClimb),
							polarCLCruise,
							polarCDCruise,
							theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
							meanAirfoil.getThicknessToChordRatio(),
							meanAirfoil.getType()
							)
					);

			List<ThrustMap> thrustList = new ArrayList<>();
			thrustList.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							theOperatingConditions.getAltitudeCruise(),
							theOperatingConditions.getDeltaTemperatureCruise(),
							theOperatingConditions.getThrottleCruise(),
							initialMassCruise,
							speedArray, 
							EngineOperatingConditionEnum.CRUISE, 
							theAircraft.getPowerPlant(),
							false, 
							cruiseCalibrationFactorThrust
							)
					);

			List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
			intersectionList.add(
					PerformanceCalcUtils.calculateDragThrustIntersection(
							theOperatingConditions.getAltitudeCruise(),
							theOperatingConditions.getDeltaTemperatureCruise(),
							speedArray,
							initialMassCruise,
							theOperatingConditions.getThrottleCruise(), 
							EngineOperatingConditionEnum.CRUISE, 
							theAircraft.getWing().getSurfacePlanform(),
							MyArrayUtils.getMax(polarCLClimb),
							dragList,
							thrustList
							)
					);

			if(intersectionList.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
				missionProfileStopped = Boolean.TRUE;
				System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
				return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
			}

			List<Double> cruiseMissionMachNumber = new ArrayList<>();
			List<Amount<Velocity>> cruiseSpeedList = new ArrayList<>();
			List<Amount<Velocity>> cruiseSpeedCASList = new ArrayList<>();
			double sigma = OperatingConditions.getAtmosphere(
					theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
					theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
					).getDensityRatio();
			if(theOperatingConditions.getMachCruise() <= intersectionList.get(0).getMaxMach()) {
				cruiseMissionMachNumber.add(theOperatingConditions.getMachCruise());
				cruiseSpeedList.add(
						SpeedCalc.calculateTAS(
								theOperatingConditions.getMachCruise(),
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise()
								).to(NonSI.KNOT)
						);

				cruiseSpeedCASList.add(cruiseSpeedList.get(0).times(Math.sqrt(sigma)));
			}
			else {
				cruiseMissionMachNumber.add(intersectionList.get(0).getMaxMach());
				cruiseSpeedList.add(intersectionList.get(0).getMaxSpeed().to(NonSI.KNOT));
				cruiseSpeedCASList.add(cruiseSpeedList.get(0).times(Math.sqrt(sigma)));
				if(cruiseMaxMachNumberErrorFlag == false) {
					System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
					cruiseMaxMachNumberErrorFlag = true;
				}
			}

			List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
			aircraftMassPerStep.add(initialMassCruise);

			List<Double> cLSteps = new ArrayList<>();
			cLSteps.add(
					LiftCalc.calculateLiftCoeff(
							Amount.valueOf(
									aircraftMassPerStep.get(0)
									.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
									.doubleValue(SI.KILOGRAM),
									SI.NEWTON
									),
							cruiseSpeedList.get(0),
							theAircraft.getWing().getSurfacePlanform(),
							theOperatingConditions.getAltitudeCruise(),
							theOperatingConditions.getDeltaTemperatureCruise()
							)
					);
			List<Double> cDSteps = new ArrayList<>();
			cDSteps.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							polarCLCruise,
							polarCDCruise,
							cLSteps.get(0)
							)
					+ DragCalc.calculateCDWaveLockKorn(
							cLSteps.get(0), 
							cruiseMissionMachNumber.get(0), 
							AerodynamicCalc.calculateMachCriticalKornMason(
									cLSteps.get(0), 
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(), 
									meanAirfoil.getType()
									)
							)
					//								(IRON LOOP2) + (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
					//								+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
					//								-0.003767654434394
					//								)
					);

			List<Amount<Force>> dragPerStep = new ArrayList<>();
			dragPerStep.add(
					DragCalc.calculateDragAtSpeed(
							theOperatingConditions.getAltitudeCruise(),
							theOperatingConditions.getDeltaTemperatureCruise(),
							theAircraft.getWing().getSurfacePlanform(),
							cruiseSpeedList.get(0),
							cDSteps.get(0)
							)
					);

			List<Amount<Force>> thrustFormDatabaseList = new ArrayList<>();
			for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
				thrustFormDatabaseList.add(
						ThrustCalc.calculateThrustDatabase(
								theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
								EngineOperatingConditionEnum.CRUISE,
								theOperatingConditions.getAltitudeCruise(),
								cruiseMissionMachNumber.get(0),
								theOperatingConditions.getDeltaTemperatureCruise(), 
								theOperatingConditions.getThrottleCruise(), 
								cruiseCalibrationFactorThrust
								)
						);
			}

			List<Double> phi = new ArrayList<>();
			try {
				phi.add(dragPerStep.get(0).doubleValue(SI.NEWTON)
						/ thrustFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
						);
			} catch (ArithmeticException e) {
				System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
				return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
			}

			if(phi.get(0) > 1.0) {
				phi.remove(0);
				phi.add(0, 1.0);
			}

			List<Double> sfcFormDatabaseList = new ArrayList<>();
			List<Double> fuelFlowFormDatabaseList = new ArrayList<>();
			for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
				sfcFormDatabaseList.add(
						theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
								cruiseMissionMachNumber.get(0),
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								phi.get(0),
								EngineOperatingConditionEnum.CRUISE,
								cruiseCalibrationFactorSFC
								)
						);
				fuelFlowFormDatabaseList.add(
						thrustFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
						*(0.224809)*(0.454/60)
						*sfcFormDatabaseList.get(iEng)
						);
			}

			List<Double> sfcList = new ArrayList<>();
			sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

			List<Double> fuelFlows = new ArrayList<>();
			fuelFlows.add(fuelFlowFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

			List<Amount<Duration>> times = new ArrayList<>();
			times.add(Amount.valueOf(0.0, SI.SECOND));

			List<Amount<Mass>> fuelUsedPerStep = new ArrayList<>();
			fuelUsedPerStep.add(Amount.valueOf(0.0, SI.KILOGRAM));

			for (int j=1; j<cruiseSteps.size(); j++) {

				times.add(
						times.get(times.size()-1)
						.plus(
								Amount.valueOf(
										(cruiseSteps.get(j).doubleValue(SI.METER)
												- cruiseSteps.get(j-1).doubleValue(SI.METER))
										/ cruiseSpeedList.get(j-1).doubleValue(SI.METERS_PER_SECOND),
										SI.SECOND
										).to(NonSI.MINUTE)
								)
						);

				fuelUsedPerStep.add(
						fuelUsedPerStep.get(fuelUsedPerStep.size()-1)
						.plus(
								Amount.valueOf(
										fuelFlows.get(j-1)
										* (times.get(j).doubleValue(NonSI.MINUTE) - times.get(j-1).doubleValue(NonSI.MINUTE)),
										SI.KILOGRAM
										)
								)
						);

				aircraftMassPerStep.add(
						aircraftMassPerStep.get(j-1).to(SI.KILOGRAM)
						.minus(
								Amount.valueOf(
										fuelUsedPerStep.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStep.get(j-1).doubleValue(SI.KILOGRAM),
										SI.KILOGRAM
										)
								)
						);

				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(),
								aircraftMassPerStep.get(j),
								speedArray,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								polarCLCruise,
								polarCDCruise,
								theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								theOperatingConditions.getAltitudeCruise(), 
								theOperatingConditions.getDeltaTemperatureCruise(),
								theOperatingConditions.getThrottleCruise(),
								aircraftMassPerStep.get(j), 
								speedArray, 
								EngineOperatingConditionEnum.CRUISE, 
								theAircraft.getPowerPlant(), 
								false, 
								cruiseCalibrationFactorThrust
								)
						);

				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(), 
								speedArray,
								aircraftMassPerStep.get(j),
								theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								dragList,
								thrustList
								)
						);

				if(intersectionList.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				if(theOperatingConditions.getMachCruise() <= intersectionList.get(j).getMaxMach()) {
					cruiseMissionMachNumber.add(theOperatingConditions.getMachCruise());
					cruiseSpeedList.add(
							SpeedCalc.calculateTAS(
									theOperatingConditions.getMachCruise(),
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise()
									).to(NonSI.KNOT)
							);
					cruiseSpeedCASList.add(cruiseSpeedList.get(j).times(Math.sqrt(sigma)));
				}
				else {
					cruiseMissionMachNumber.add(intersectionList.get(j).getMaxMach());
					cruiseSpeedList.add(intersectionList.get(j).getMaxSpeed().to(NonSI.KNOT));
					cruiseSpeedCASList.add(cruiseSpeedList.get(j).times(Math.sqrt(sigma)));
					if(cruiseMaxMachNumberErrorFlag == false) {
						System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THE ASSIGNED CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						cruiseMaxMachNumberErrorFlag = true;
					}
				}

				cLSteps.add(
						LiftCalc.calculateLiftCoeff(
								Amount.valueOf(
										aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										SI.NEWTON
										), 
								cruiseSpeedList.get(j),
								theAircraft.getWing().getSurfacePlanform(),
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise()
								)
						);

				cDSteps.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								polarCLCruise,
								polarCDCruise,
								cLSteps.get(j))
						+ DragCalc.calculateCDWaveLockKorn(
								cLSteps.get(j), 
								cruiseMissionMachNumber.get(j), 
								AerodynamicCalc.calculateMachCriticalKornMason(
										cLSteps.get(j), 
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(), 
										meanAirfoil.getType()
										)
								)
						//									+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
						//											+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
						//											-0.003767654434394
						//											)
						);

				dragPerStep.add(
						DragCalc.calculateDragAtSpeed(
								theOperatingConditions.getAltitudeCruise(),
								theOperatingConditions.getDeltaTemperatureCruise(), 
								theAircraft.getWing().getSurfacePlanform(), 
								cruiseSpeedList.get(j),
								cDSteps.get(j)
								)
						);

				thrustFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					thrustFormDatabaseList.add(
							ThrustCalc.calculateThrustDatabase(
									theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
									EngineOperatingConditionEnum.CRUISE,
									theOperatingConditions.getAltitudeCruise(),
									cruiseMissionMachNumber.get(j),
									theOperatingConditions.getDeltaTemperatureCruise(), 
									theOperatingConditions.getThrottleCruise(), 
									cruiseCalibrationFactorThrust
									)
							);
				}

				try {
					phi.add(dragPerStep.get(j).doubleValue(SI.NEWTON)
							/ thrustFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
							);
				} catch (ArithmeticException e) {
					System.err.println("WARNING: (CRUISE - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				if(phi.get(j) > 1.0) {
					phi.remove(j);
					phi.add(j, 1.0);
				}

				sfcFormDatabaseList = new ArrayList<>();
				fuelFlowFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					sfcFormDatabaseList.add(
							theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
									cruiseMissionMachNumber.get(j),
									theOperatingConditions.getAltitudeCruise(),
									theOperatingConditions.getDeltaTemperatureCruise(),
									phi.get(j),
									EngineOperatingConditionEnum.CRUISE,
									cruiseCalibrationFactorSFC
									)
							);
					fuelFlowFormDatabaseList.add(
							thrustFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*sfcFormDatabaseList.get(iEng)
							);
				}

				sfcList.add(sfcFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

				fuelFlows.add(fuelFlowFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

			}

			rangeCruise.addAll(cruiseSteps);			
			fuelUsedCruise.addAll(fuelUsedPerStep);

			//--------------------------------------------------------------------
			// DESCENT (up to HOLDING altitude)
			Amount<Mass> initialMassDescent = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
					- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
					- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			theFirstDescentCalculator = new DescentCalc(
					theAircraft,
					theOperatingConditions,
					speedDescentCAS,
					rateOfDescent,
					theOperatingConditions.getAltitudeCruise().to(SI.METER),
					holdingAltitude.to(SI.METER),
					initialMassDescent,
					polarCLClimb,
					polarCDClimb,
					cruiseCalibrationFactorThrust,
					cruiseCalibrationFactorSFC,
					flightIdleCalibrationFactorThrust,
					flightIdleCalibrationFactorSFC,
					1.0, // cruise NOx correction factor (not needed)
					1.0, // cruise CO correction factor (not needed)
					1.0, // cruise HC correction factor (not needed)
					1.0, // cruise Soot correction factor (not needed)
					1.0, // cruise CO2 correction factor (not needed)
					1.0, // cruise SOx correction factor (not needed)
					1.0, // cruise H2O correction factor (not needed)
					1.0, // fidl NOx correction factor (not needed)
					1.0, // fidl CO correction factor (not needed)
					1.0, // fidl HC correction factor (not needed)
					1.0, // fidl Soot correction factor (not needed)
					1.0, // fidl CO2 correction factor (not needed)
					1.0, // fidl SOx correction factor (not needed)
					1.0 // fidl H2O correction factor (not needed)
					);
			theFirstDescentCalculator.setNumberOfStepDescent(numberOfStepDescent);
			
			theFirstDescentCalculator.calculateDescentPerformance();

			rangeFirstDescent.addAll(theFirstDescentCalculator.getDescentLengths());			
			fuelUsedFirstDescent.addAll(theFirstDescentCalculator.getFuelUsedPerStep());

			//--------------------------------------------------------------------
			// SECOND CLIMB (up to ALTERNATE altitude)
			Amount<Mass> initialMassSecondClimb = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
					- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
					- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
					- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			theSecondClimbCalculator = new ClimbCalc(
					theAircraft,
					theOperatingConditions,
					cLmaxClean, 
					polarCLClimb,
					polarCDClimb,
					climbSpeed,
					dragDueToEnigneFailure,
					climbCalibrationFactorThrust,
					continuousCalibrationFactorThrust,
					climbCalibrationFactorSFC,
					1.0, // climb NOx correction factor (not needed)
					1.0, // climb CO correction factor (not needed)
					1.0, // climb HC correction factor (not needed)
					1.0, // climb Soot correction factor (not needed)
					1.0, // climb CO2 correction factor (not needed)
					1.0, // climb SOx correction factor (not needed)
					1.0 // climb H2O correction factor (not needed)
					);
			theSecondClimbCalculator.setNumberOfStepClimb(numberOfStepClimb);
			
			theSecondClimbCalculator.calculateClimbPerformance(
					initialMassSecondClimb,
					initialMassSecondClimb,
					holdingAltitude.to(SI.METER),
					alternateCruiseAltitude.to(SI.METER),
					false,
					false
					);

			rangeSecondClimb.addAll(theSecondClimbCalculator.getRangeClimb());			
			fuelUsedSecondClimb.addAll(theSecondClimbCalculator.getFuelUsedClimb());

			//--------------------------------------------------------------------
			// ALTERNATE CRUISE (AT MAX EFFICIENCY)
			Amount<Mass> initialMassAlternateCruise = Amount.valueOf(
					initialMissionMass.doubleValue(SI.KILOGRAM)
					- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
					- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
					- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
					- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
					- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			for (int iAlternate=0; iAlternate < 5; iAlternate++) {

				initializePhasesLists(false, true);

				List<Amount<Length>> alternateCruiseSteps = MyArrayUtils.convertDoubleArrayToListOfAmount( 
						MyArrayUtils.linspace(
								0.0,
								currentAlternateCruiseRange.doubleValue(SI.METER),
								numberOfStepAlternateCruise
								),
						SI.METER
						);

				List<Amount<Velocity>> speedArrayAlternate = 
						MyArrayUtils.convertDoubleArrayToListOfAmount(
								MyArrayUtils.linspace(
										SpeedCalc.calculateSpeedStall(
												alternateCruiseAltitude,
												theOperatingConditions.getDeltaTemperatureCruise(),
												initialMassAlternateCruise,
												theAircraft.getWing().getSurfacePlanform(),
												MyArrayUtils.getMax(polarCLClimb)
												).doubleValue(SI.METERS_PER_SECOND),
										SpeedCalc.calculateTAS(
												1.0,
												alternateCruiseAltitude,
												theOperatingConditions.getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND),
										nPointSpeed
										),
								SI.METERS_PER_SECOND
								);

				List<DragMap> dragListAlternate = new ArrayList<>();
				dragListAlternate.add(
						DragCalc.calculateDragAndPowerRequired(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise(),
								initialMassAlternateCruise,
								speedArrayAlternate,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								polarCLCruise,
								polarCDCruise,
								theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				List<ThrustMap> thrustListAlternate = new ArrayList<>();
				thrustListAlternate.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise(),
								theOperatingConditions.getThrottleCruise(),
								initialMassAlternateCruise,
								speedArrayAlternate,
								EngineOperatingConditionEnum.CRUISE,
								theAircraft.getPowerPlant(),
								false,
								cruiseCalibrationFactorThrust
								)
						);

				List<DragThrustIntersectionMap> intersectionListAlternate = new ArrayList<>();
				intersectionListAlternate.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise(),
								speedArrayAlternate,
								initialMassAlternateCruise,
								theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								dragListAlternate,
								thrustListAlternate
								)
						);

				if(intersectionListAlternate.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) CRUISE MACH NUMBER = 0.0. RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				List<Double> rangeFactorAlternateCruiseList = new ArrayList<>();
				double[] cLRangeAlternateCruiseArray = MyArrayUtils.linspace(
						0.1,
						MyArrayUtils.getMax(polarCLCruise),
						50
						); 

				for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {
					if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
							|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
						rangeFactorAlternateCruiseList.add(
								Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
								/ MyMathUtils.getInterpolatedValue1DLinear(
										polarCLCruise,
										polarCDCruise,
										cLRangeAlternateCruiseArray[iCL]
										)
								);
					else {
						rangeFactorAlternateCruiseList.add(
								cLRangeAlternateCruiseArray[iCL]
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLCruise,
												polarCDCruise,
												cLRangeAlternateCruiseArray[iCL]
												)
								);
					}
				}

				int iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
				double bestMachAlternateCruise = SpeedCalc.calculateMach(
						alternateCruiseAltitude,
						theOperatingConditions.getDeltaTemperatureCruise(),
						SpeedCalc.calculateSpeedAtCL(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								initialMassAlternateCruise,
								theAircraft.getWing().getSurfacePlanform(), 
								cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
								)
						);

				List<Double> alternateCruiseMachNumberList = new ArrayList<>();
				List<Amount<Velocity>> alternateCruiseSpeedList = new ArrayList<>();
				List<Amount<Velocity>> alternateCruiseSpeedCASList = new ArrayList<>();
				double sigmaAlternateCruise = OperatingConditions.getAtmosphere(
						alternateCruiseAltitude.doubleValue(SI.METER),
						theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
						).getDensityRatio();
				if(bestMachAlternateCruise <= intersectionListAlternate.get(0).getMaxMach()) {
					alternateCruiseMachNumberList.add(bestMachAlternateCruise);
					alternateCruiseSpeedList.add(
							SpeedCalc.calculateTAS(
									bestMachAlternateCruise,
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise()
									).to(NonSI.KNOT)
							);
					alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(0).times(Math.sqrt(sigmaAlternateCruise)));
				}
				else {
					alternateCruiseMachNumberList.add(intersectionListAlternate.get(0).getMaxMach());
					alternateCruiseSpeedList.add(intersectionListAlternate.get(0).getMaxSpeed().to(NonSI.KNOT));
					alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(0).times(Math.sqrt(sigmaAlternateCruise)));
					if(alternateCruiseBestMachNumberErrorFlag == false) {
						System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						alternateCruiseBestMachNumberErrorFlag = true;
					}
				}

				List<Amount<Mass>> aircraftMassPerStepAlternateCruise = new ArrayList<>();
				aircraftMassPerStepAlternateCruise.add(initialMassAlternateCruise);

				List<Double> cLStepsAlternateCruise = new ArrayList<>();
				cLStepsAlternateCruise.add(
						LiftCalc.calculateLiftCoeff(
								Amount.valueOf(
										aircraftMassPerStepAlternateCruise.get(0)
										.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
										.doubleValue(SI.KILOGRAM),
										SI.NEWTON
										),
								alternateCruiseSpeedList.get(0),
								theAircraft.getWing().getSurfacePlanform(),
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise()
								)
						);

				List<Double> cDStepsAlternateCruise = new ArrayList<>();
				cDStepsAlternateCruise.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								polarCLCruise,
								polarCDCruise,
								cLStepsAlternateCruise.get(0)
								)
						+ DragCalc.calculateCDWaveLockKorn(
								cLStepsAlternateCruise.get(0), 
								alternateCruiseMachNumberList.get(0), 
								AerodynamicCalc.calculateMachCriticalKornMason(
										cLStepsAlternateCruise.get(0), 
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(), 
										meanAirfoil.getType()
										)
								)
						//									(IRON LOOP2) + (-0.000000000002553*Math.pow(aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM), 2)
						//									+ 0.000000209147028*aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)	
						//									-0.003767654434394
						//									)
						);

				List<Amount<Force>> dragPerStepAlternateCruise = new ArrayList<>();
				dragPerStepAlternateCruise.add(
						DragCalc.calculateDragAtSpeed(
								alternateCruiseAltitude,
								theOperatingConditions.getDeltaTemperatureCruise(),
								theAircraft.getWing().getSurfacePlanform(),
								alternateCruiseSpeedList.get(0),
								cDStepsAlternateCruise.get(0)
								)
						);

				List<Amount<Force>> thrustAlternateCruiseFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					thrustAlternateCruiseFormDatabaseList.add(
							ThrustCalc.calculateThrustDatabase(
									theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
									EngineOperatingConditionEnum.CRUISE,
									alternateCruiseAltitude,
									alternateCruiseMachNumberList.get(0),
									theOperatingConditions.getDeltaTemperatureCruise(), 
									theOperatingConditions.getThrottleCruise(), 
									cruiseCalibrationFactorThrust
									)
							);
				}

				List<Double> phiAlternateCruise = new ArrayList<>();
				try {
					phiAlternateCruise.add(dragPerStepAlternateCruise.get(0).doubleValue(SI.NEWTON)
							/ thrustAlternateCruiseFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
							);
				} catch (ArithmeticException e) {
					System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				if(phiAlternateCruise.get(0) > 1.0) {
					phiAlternateCruise.remove(0);
					phiAlternateCruise.add(0, 1.0);
				}

				List<Double> sfcAlternateCruiseFormDatabaseList = new ArrayList<>();
				List<Double> fuelFlowAlternateCruiseFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					sfcAlternateCruiseFormDatabaseList.add(
							theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
									alternateCruiseMachNumberList.get(0),
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									phiAlternateCruise.get(0),
									EngineOperatingConditionEnum.CRUISE,
									cruiseCalibrationFactorSFC
									)
							);
					fuelFlowAlternateCruiseFormDatabaseList.add(
							thrustAlternateCruiseFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*sfcAlternateCruiseFormDatabaseList.get(iEng)
							);
				}

				List<Double> sfcAlternateCruiseList = new ArrayList<>();
				sfcAlternateCruiseList.add(sfcAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

				List<Double> fuelFlowsAlternateCruise = new ArrayList<>();
				fuelFlowsAlternateCruise.add(fuelFlowAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				List<Amount<Duration>> timesAlternateCruise = new ArrayList<>();
				timesAlternateCruise.add(Amount.valueOf(0.0, SI.SECOND));

				List<Amount<Mass>> fuelUsedPerStepAlternateCruise = new ArrayList<>();
				fuelUsedPerStepAlternateCruise.add(Amount.valueOf(0.0, SI.KILOGRAM));

				for (int j=1; j<alternateCruiseSteps.size(); j++) {

					timesAlternateCruise.add(
							timesAlternateCruise.get(timesAlternateCruise.size()-1)
							.plus(
									Amount.valueOf(
											(alternateCruiseSteps.get(j).doubleValue(SI.METER)
													- alternateCruiseSteps.get(j-1).doubleValue(SI.METER))
											/ alternateCruiseSpeedList.get(j-1).doubleValue(SI.METERS_PER_SECOND),
											SI.SECOND
											).to(NonSI.MINUTE)
									)
							);

					fuelUsedPerStepAlternateCruise.add(
							fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1)
							.plus(
									Amount.valueOf(
											fuelFlowsAlternateCruise.get(j-1)
											* (timesAlternateCruise.get(j).doubleValue(NonSI.MINUTE) - timesAlternateCruise.get(j-1).doubleValue(NonSI.MINUTE)),
											SI.KILOGRAM
											)
									)
							);

					aircraftMassPerStepAlternateCruise.add(
							aircraftMassPerStepAlternateCruise.get(j-1)
							.minus(
									Amount.valueOf(
											fuelUsedPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepAlternateCruise.get(j-1).doubleValue(SI.KILOGRAM),
											SI.KILOGRAM
											)
									)
							);

					dragListAlternate.add(
							DragCalc.calculateDragAndPowerRequired(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									aircraftMassPerStepAlternateCruise.get(j),
									speedArrayAlternate,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									polarCLCruise,
									polarCDCruise,
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					thrustListAlternate.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									theOperatingConditions.getThrottleCruise(),
									aircraftMassPerStepAlternateCruise.get(j),
									speedArrayAlternate,
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getPowerPlant(),
									false,
									cruiseCalibrationFactorThrust
									)
							);

					intersectionListAlternate.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									speedArrayAlternate,
									aircraftMassPerStepAlternateCruise.get(j),
									theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									dragListAlternate,
									thrustListAlternate
									)
							);

					if(intersectionListAlternate.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) ALTERNATE CRUISE MACH NUMBER = 0.0. RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					}

					rangeFactorAlternateCruiseList = new ArrayList<>();
					for (int iCL=0; iCL<cLRangeAlternateCruiseArray.length; iCL++) {
						if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
								|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
							rangeFactorAlternateCruiseList.add(
									Math.pow(cLRangeAlternateCruiseArray[iCL], (1/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLCruise,
											polarCDCruise,
											cLRangeAlternateCruiseArray[iCL]
											)
									);
						else 
							rangeFactorAlternateCruiseList.add(
									cLRangeAlternateCruiseArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													polarCLCruise,
													polarCDCruise,
													cLRangeAlternateCruiseArray[iCL]
													)
									);
					}

					iBestCLAlternateCruise = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(rangeFactorAlternateCruiseList));
					bestMachAlternateCruise = SpeedCalc.calculateMach(
							alternateCruiseAltitude,
							theOperatingConditions.getDeltaTemperatureCruise(),
							SpeedCalc.calculateSpeedAtCL(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(),
									aircraftMassPerStepAlternateCruise.get(j),
									theAircraft.getWing().getSurfacePlanform(), 
									cLRangeAlternateCruiseArray[iBestCLAlternateCruise]
									)
							);

					if(bestMachAlternateCruise <= intersectionListAlternate.get(j).getMaxMach()) {
						alternateCruiseMachNumberList.add(bestMachAlternateCruise);
						alternateCruiseSpeedList.add(
								SpeedCalc.calculateTAS(
										bestMachAlternateCruise,
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise()
										).to(NonSI.KNOT)
								);
						alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(j).times(Math.sqrt(sigmaAlternateCruise)));
					}
					else {
						alternateCruiseMachNumberList.add(intersectionListAlternate.get(j).getMaxMach());
						alternateCruiseSpeedList.add(intersectionListAlternate.get(j).getMaxSpeed().to(NonSI.KNOT));
						alternateCruiseSpeedCASList.add(alternateCruiseSpeedList.get(j).times(Math.sqrt(sigmaAlternateCruise)));
						if(alternateCruiseBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THE BEST ALTERNATE CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							alternateCruiseBestMachNumberErrorFlag = true;
						}
					}

					cLStepsAlternateCruise.add(
							LiftCalc.calculateLiftCoeff(
									Amount.valueOf(
											aircraftMassPerStepAlternateCruise.get(j).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											SI.NEWTON
											),
									alternateCruiseSpeedList.get(j),
									theAircraft.getWing().getSurfacePlanform(),
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise()
									)
							);

					cDStepsAlternateCruise.add(
							MyMathUtils.getInterpolatedValue1DLinear(
									polarCLCruise,
									polarCDCruise,
									cLStepsAlternateCruise.get(j))
							+ DragCalc.calculateCDWaveLockKorn(
									cLStepsAlternateCruise.get(j), 
									alternateCruiseMachNumberList.get(j), 
									AerodynamicCalc.calculateMachCriticalKornMason(
											cLStepsAlternateCruise.get(j), 
											theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
											meanAirfoil.getThicknessToChordRatio(), 
											meanAirfoil.getType()
											)
									)
							//										+ (-0.000000000002553*Math.pow(aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM), 2)
							//												+ 0.000000209147028*aircraftMassPerStep.get(j).doubleValue(SI.KILOGRAM)	
							//												-0.003767654434394
							//												)
							);

					dragPerStepAlternateCruise.add(
							DragCalc.calculateDragAtSpeed(
									alternateCruiseAltitude,
									theOperatingConditions.getDeltaTemperatureCruise(), 
									theAircraft.getWing().getSurfacePlanform(), 
									alternateCruiseSpeedList.get(j),
									cDStepsAlternateCruise.get(j)
									)
							);

					thrustAlternateCruiseFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						thrustAlternateCruiseFormDatabaseList.add(
								ThrustCalc.calculateThrustDatabase(
										theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
										theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
										EngineOperatingConditionEnum.CRUISE,
										alternateCruiseAltitude,
										alternateCruiseMachNumberList.get(j),
										theOperatingConditions.getDeltaTemperatureCruise(), 
										theOperatingConditions.getThrottleCruise(), 
										cruiseCalibrationFactorThrust
										)
								);
					}

					try {
						phiAlternateCruise.add(dragPerStepAlternateCruise.get(j).doubleValue(SI.NEWTON)
								/ thrustAlternateCruiseFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								);
					} catch (ArithmeticException e) {
						System.err.println("WARNING: (ALTERNATE CRUISE - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					}

					if(phiAlternateCruise.get(j) > 1.0) {
						phiAlternateCruise.remove(j);
						phiAlternateCruise.add(j, 1.0);
					}

					sfcAlternateCruiseFormDatabaseList = new ArrayList<>();
					fuelFlowAlternateCruiseFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						sfcAlternateCruiseFormDatabaseList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
										alternateCruiseMachNumberList.get(j),
										alternateCruiseAltitude,
										theOperatingConditions.getDeltaTemperatureCruise(),
										phiAlternateCruise.get(j),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorSFC
										)
								);
						fuelFlowAlternateCruiseFormDatabaseList.add(
								thrustAlternateCruiseFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*sfcAlternateCruiseFormDatabaseList.get(iEng)
								);
					}

					sfcAlternateCruiseList.add(sfcAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

					fuelFlowsAlternateCruise.add(fuelFlowAlternateCruiseFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				}

				rangeAlternateCruise.addAll(alternateCruiseSteps);			
				fuelUsedAlternateCruise.addAll(fuelUsedPerStepAlternateCruise);

				//--------------------------------------------------------------------
				// DESCENT (up to HOLDING altitude)
				Amount<Mass> initialMassSecondDescent = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
						- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
						- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);

				theSecondDescentCalculator = new DescentCalc(
						theAircraft,
						theOperatingConditions,
						speedDescentCAS,
						rateOfDescent,
						alternateCruiseAltitude.to(SI.METER),
						holdingAltitude.to(SI.METER),
						initialMassSecondDescent,
						polarCLClimb,
						polarCDClimb,
						cruiseCalibrationFactorThrust,
						cruiseCalibrationFactorSFC,
						flightIdleCalibrationFactorThrust,
						flightIdleCalibrationFactorSFC,
						1.0, // cruise NOx correction factor (not needed)
						1.0, // cruise CO correction factor (not needed)
						1.0, // cruise HC correction factor (not needed)
						1.0, // cruise Soot correction factor (not needed)
						1.0, // cruise CO2 correction factor (not needed)
						1.0, // cruise SOx correction factor (not needed)
						1.0, // cruise H2O correction factor (not needed)
						1.0, // fidl NOx correction factor (not needed)
						1.0, // fidl CO correction factor (not needed)
						1.0, // fidl HC correction factor (not needed)
						1.0, // fidl Soot correction factor (not needed)
						1.0, // fidl CO2 correction factor (not needed)
						1.0, // fidl SOx correction factor (not needed)
						1.0 // fidl H2O correction factor (not needed)
						);
				theSecondDescentCalculator.setNumberOfStepDescent(numberOfStepDescent);
				
				theSecondDescentCalculator.calculateDescentPerformance();

				rangeSecondDescent.addAll(theSecondDescentCalculator.getDescentLengths());			
				fuelUsedSecondDescent.addAll(theSecondDescentCalculator.getFuelUsedPerStep());

				//--------------------------------------------------------------------
				// HOLDING (BEST ENDURANCE) - CLIMB DELTA TEMPERATURE
				Amount<Mass> initialMassHolding = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
						- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
						- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
						- theSecondDescentCalculator.getFuelUsedPerStep().get(theSecondDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);

				List<Amount<Duration>> timeHoldingArray = MyArrayUtils.convertDoubleArrayToListOfAmount( 
						MyArrayUtils.linspace(
								0.0,
								holdingDuration.doubleValue(NonSI.MINUTE),
								numberOfStepHolding
								),
						NonSI.MINUTE
						);

				List<Amount<Velocity>> speedArrayHolding = MyArrayUtils.convertDoubleArrayToListOfAmount( 
						MyArrayUtils.linspace(
								SpeedCalc.calculateSpeedStall(
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										initialMassHolding,
										theAircraft.getWing().getSurfacePlanform(),
										MyArrayUtils.getMax(polarCLClimb)
										).doubleValue(SI.METERS_PER_SECOND),
								SpeedCalc.calculateTAS(
										1.0,
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb()
										).doubleValue(SI.METERS_PER_SECOND),
								nPointSpeed
								),
						SI.METERS_PER_SECOND
						);

				List<DragMap> dragListHolding = new ArrayList<>();
				dragListHolding.add(
						DragCalc.calculateDragAndPowerRequired(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								initialMassHolding,
								speedArrayHolding,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								polarCLClimb,
								polarCDClimb,
								theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				List<ThrustMap> thrustListHolding = new ArrayList<>();
				thrustListHolding.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								theOperatingConditions.getThrottleCruise(),
								initialMassHolding,
								speedArrayHolding,
								EngineOperatingConditionEnum.CRUISE,
								theAircraft.getPowerPlant(),
								false,
								cruiseCalibrationFactorThrust
								)
						);

				List<DragThrustIntersectionMap> intersectionListHolding = new ArrayList<>();
				intersectionListHolding.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								speedArrayHolding,
								initialMassHolding,
								theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								theAircraft.getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(polarCLClimb),
								dragListHolding,
								thrustListHolding
								)
						);

				if(intersectionListHolding.get(0).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				List<Double> enduranceFactorHoldingList = new ArrayList<>();
				double[] cLEnduranceHoldingArray = MyArrayUtils.linspace(
						0.1,
						MyArrayUtils.getMax(polarCLClimb),
						50
						); 
				for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {
					if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
							|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
						enduranceFactorHoldingList.add(
								cLEnduranceHoldingArray[iCL]
										/ MyMathUtils.getInterpolatedValue1DLinear(
												polarCLClimb,
												polarCDClimb,
												cLEnduranceHoldingArray[iCL]
												)
								);
					else 
						enduranceFactorHoldingList.add(
								Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
								/ MyMathUtils.getInterpolatedValue1DLinear(
										polarCLClimb,
										polarCDClimb,
										cLEnduranceHoldingArray[iCL]
										)
								);
				}

				int iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
				double bestMachHolding = SpeedCalc.calculateMach(
						holdingAltitude,
						theOperatingConditions.getDeltaTemperatureClimb(),
						SpeedCalc.calculateSpeedAtCL(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								initialMassHolding,
								theAircraft.getWing().getSurfacePlanform(), 
								cLEnduranceHoldingArray[iBestSpeedHolding]
								)
						);

				List<Double> holdingMachNumberList = new ArrayList<>();
				List<Amount<Velocity>> holdingSpeedList = new ArrayList<>();
				List<Amount<Velocity>> holdingSpeedCASList = new ArrayList<>();
				double sigmaHolding = OperatingConditions.getAtmosphere(
						holdingAltitude.doubleValue(SI.METER),
						theOperatingConditions.getDeltaTemperatureClimb().doubleValue(SI.CELSIUS)
						).getDensityRatio();
				if(bestMachHolding <= intersectionListHolding.get(0).getMaxMach()) {
					holdingMachNumberList.add(bestMachHolding);
					holdingSpeedList.add(
							SpeedCalc.calculateTAS(
									bestMachHolding,
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb()
									).to(NonSI.KNOT)
							);
					holdingSpeedCASList.add(holdingSpeedList.get(0).times(Math.sqrt(sigmaHolding)));
				}
				else {
					holdingMachNumberList.add(intersectionListHolding.get(0).getMaxMach());
					holdingSpeedList.add(intersectionListHolding.get(0).getMaxSpeed().to(NonSI.KNOT));
					holdingSpeedCASList.add(holdingSpeedList.get(0).times(Math.sqrt(sigmaHolding)));
					if(holdingBestMachNumberErrorFlag == false) {
						System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THE BEST HOLDING MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
						holdingBestMachNumberErrorFlag = true;
					}
				}

				List<Amount<Mass>> aircraftMassPerStepHolding = new ArrayList<>();
				aircraftMassPerStepHolding.add(initialMassHolding);

				List<Double> cLStepsHolding = new ArrayList<>();
				cLStepsHolding.add(
						LiftCalc.calculateLiftCoeff(
								Amount.valueOf(
										aircraftMassPerStepHolding.get(0).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										SI.NEWTON
										),
								holdingSpeedList.get(0),
								theAircraft.getWing().getSurfacePlanform(),
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb()
								)
						);

				List<Double> cDStepsHolding = new ArrayList<>();
				cDStepsHolding.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								polarCLClimb,
								polarCDClimb,
								cLStepsHolding.get(0)
								)
						+ DragCalc.calculateCDWaveLockKorn(
								cLStepsHolding.get(0), 
								holdingMachNumberList.get(0), 
								AerodynamicCalc.calculateMachCriticalKornMason(
										cLStepsHolding.get(0), 
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(), 
										meanAirfoil.getType()
										)
								)
						);

				List<Amount<Force>> dragPerStepHolding = new ArrayList<>();
				dragPerStepHolding.add(
						DragCalc.calculateDragAtSpeed(
								holdingAltitude,
								theOperatingConditions.getDeltaTemperatureClimb(),
								theAircraft.getWing().getSurfacePlanform(),
								holdingSpeedList.get(0),
								cDStepsHolding.get(0)
								)
						);

				List<Amount<Force>> thrustHoldingFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					thrustHoldingFormDatabaseList.add(
							ThrustCalc.calculateThrustDatabase(
									theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
									theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
									EngineOperatingConditionEnum.CRUISE,
									holdingAltitude,
									holdingMachNumberList.get(0),
									theOperatingConditions.getDeltaTemperatureClimb(), 
									theOperatingConditions.getThrottleCruise(), 
									cruiseCalibrationFactorThrust
									)
							);
				}

				List<Double> phiHolding = new ArrayList<>();
				try {
					phiHolding.add(dragPerStepHolding.get(0).doubleValue(SI.NEWTON)
							/ thrustHoldingFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
							);
				} catch (ArithmeticException e) {
					System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}

				if(phiHolding.get(0) > 1.0) {
					phiHolding.remove(0);
					phiHolding.add(0, 1.0);
				}

				List<Double> sfcHoldingFormDatabaseList = new ArrayList<>();
				List<Double> fuelFlowHoldingFormDatabaseList = new ArrayList<>();
				for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
					sfcHoldingFormDatabaseList.add(
							theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
									holdingMachNumberList.get(0),
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									phiHolding.get(0),
									EngineOperatingConditionEnum.CRUISE,
									cruiseCalibrationFactorSFC
									)
							);
					fuelFlowHoldingFormDatabaseList.add(
							thrustHoldingFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
							*(0.224809)*(0.454/60)
							*sfcHoldingFormDatabaseList.get(iEng)
							);
				}

				List<Double> sfcHoldingList = new ArrayList<>();
				sfcHoldingList.add(sfcHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

				List<Double> fuelFlowsHolding = new ArrayList<>();
				fuelFlowsHolding.add(fuelFlowHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				List<Amount<Mass>> fuelUsedPerStepHolding = new ArrayList<>();
				fuelUsedPerStepHolding.add(Amount.valueOf(0.0, SI.KILOGRAM));

				for(int j=1; j<timeHoldingArray.size(); j++) {

					fuelUsedPerStepHolding.add(
							fuelUsedPerStepHolding.get(fuelUsedPerStepHolding.size()-1)
							.plus(
									Amount.valueOf(
											fuelFlowsHolding.get(j-1)
											* (timeHoldingArray.get(j).doubleValue(NonSI.MINUTE) - timeHoldingArray.get(j-1).doubleValue(NonSI.MINUTE)),
											SI.KILOGRAM
											)
									)
							);

					aircraftMassPerStepHolding.add(
							aircraftMassPerStepHolding.get(j-1)
							.minus(
									Amount.valueOf(
											fuelUsedPerStepHolding.get(j).doubleValue(SI.KILOGRAM) - fuelUsedPerStepHolding.get(j-1).doubleValue(SI.KILOGRAM),
											SI.KILOGRAM
											)
									)
							);

					dragListHolding.add(
							DragCalc.calculateDragAndPowerRequired(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									aircraftMassPerStepHolding.get(j),
									speedArrayHolding,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									polarCLClimb,
									polarCDClimb,
									theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(),
									meanAirfoil.getType()
									)
							);

					thrustListHolding.add(
							ThrustCalc.calculateThrustAndPowerAvailable(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									theOperatingConditions.getThrottleCruise(),
									aircraftMassPerStepHolding.get(j),
									speedArrayHolding,
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getPowerPlant(),
									false,
									cruiseCalibrationFactorThrust
									)
							);

					intersectionListHolding.add(
							PerformanceCalcUtils.calculateDragThrustIntersection(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									speedArrayHolding,
									aircraftMassPerStepHolding.get(j),
									theOperatingConditions.getThrottleCruise(),
									EngineOperatingConditionEnum.CRUISE,
									theAircraft.getWing().getSurfacePlanform(),
									MyArrayUtils.getMax(polarCLClimb),
									dragListHolding,
									thrustListHolding
									)
							);

					if(intersectionListHolding.get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) < 0.01) {
						missionProfileStopped = Boolean.TRUE;
						System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) HOLDING MACH NUMBER = 0.0. RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					}

					enduranceFactorHoldingList = new ArrayList<>();
					for (int iCL=0; iCL<cLEnduranceHoldingArray.length; iCL++) {
						if(theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOJET) 
								|| theAircraft.getPowerPlant().getEngineType().contains(EngineTypeEnum.TURBOFAN) )
							enduranceFactorHoldingList.add(
									cLEnduranceHoldingArray[iCL]
											/ MyMathUtils.getInterpolatedValue1DLinear(
													polarCLClimb,
													polarCDClimb,
													cLEnduranceHoldingArray[iCL]
													)
									);
						else 
							enduranceFactorHoldingList.add(
									Math.pow(cLEnduranceHoldingArray[iCL], (3/2))
									/ MyMathUtils.getInterpolatedValue1DLinear(
											polarCLClimb,
											polarCDClimb,
											cLEnduranceHoldingArray[iCL]
											)
									);
					}

					iBestSpeedHolding = MyArrayUtils.getIndexOfMax(MyArrayUtils.convertToDoublePrimitive(enduranceFactorHoldingList));
					bestMachHolding = SpeedCalc.calculateMach(
							holdingAltitude,
							theOperatingConditions.getDeltaTemperatureClimb(),
							SpeedCalc.calculateSpeedAtCL(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(),
									aircraftMassPerStepHolding.get(0),
									theAircraft.getWing().getSurfacePlanform(), 
									cLEnduranceHoldingArray[iBestSpeedHolding]
									)
							);

					if(bestMachHolding <= intersectionListHolding.get(j).getMaxMach()) {
						holdingMachNumberList.add(bestMachHolding);
						holdingSpeedList.add(
								SpeedCalc.calculateTAS(
										bestMachHolding,
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb()
										).to(NonSI.KNOT)
								);
						holdingSpeedCASList.add(holdingSpeedList.get(j).times(Math.sqrt(sigmaHolding)));
					}
					else {
						holdingMachNumberList.add(intersectionListHolding.get(j).getMaxMach());
						holdingSpeedList.add(intersectionListHolding.get(j).getMaxSpeed().to(NonSI.KNOT));
						holdingSpeedCASList.add(holdingSpeedList.get(j).times(Math.sqrt(sigmaHolding)));
						if(holdingBestMachNumberErrorFlag == false) {
							System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THE BEST HOLDING CRUISE MACH NUMBER IS BIGGER THAN THE MAXIMUM MACH NUMBER. MAXIMUM MACH NUMBER WILL BE USED.");
							holdingBestMachNumberErrorFlag = true;
						}

					}

					cLStepsHolding.add(
							LiftCalc.calculateLiftCoeff(
									Amount.valueOf(
											aircraftMassPerStepHolding.get(j).doubleValue(SI.KILOGRAM)
											*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											SI.NEWTON
											),
									holdingSpeedList.get(j),
									theAircraft.getWing().getSurfacePlanform(),
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb()
									)
							);

					cDStepsHolding.add(
							MyMathUtils.getInterpolatedValue1DLinear(
									polarCLClimb,
									polarCDClimb,
									cLStepsHolding.get(j))
							+ DragCalc.calculateCDWaveLockKorn(
									cLStepsHolding.get(j), 
									holdingMachNumberList.get(j), 
									AerodynamicCalc.calculateMachCriticalKornMason(
											cLStepsHolding.get(j), 
											theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
											meanAirfoil.getThicknessToChordRatio(), 
											meanAirfoil.getType()
											)
									)
							);

					dragPerStepHolding.add(
							DragCalc.calculateDragAtSpeed(
									holdingAltitude,
									theOperatingConditions.getDeltaTemperatureClimb(), 
									theAircraft.getWing().getSurfacePlanform(), 
									holdingSpeedList.get(j),
									cDStepsHolding.get(j)
									)
							);

					thrustHoldingFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						thrustHoldingFormDatabaseList.add(
								ThrustCalc.calculateThrustDatabase(
										theAircraft.getPowerPlant().getEngineList().get(iEng).getT0(),
										theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng),
										EngineOperatingConditionEnum.CRUISE,
										holdingAltitude,
										holdingMachNumberList.get(j),
										theOperatingConditions.getDeltaTemperatureClimb(), 
										theOperatingConditions.getThrottleCruise(), 
										cruiseCalibrationFactorThrust
										)
								);
					}

					try {
						phiHolding.add(dragPerStepHolding.get(j).doubleValue(SI.NEWTON)
								/ thrustHoldingFormDatabaseList.stream().mapToDouble(thr -> thr.doubleValue(SI.NEWTON)).sum()
								);
					} catch (ArithmeticException e) {
						System.err.println("WARNING: (HOLDING - PAYLOAD-RANGE) THRUST FROM DATABASE = 0.0, CANNOT DIVIDE BY 0.0! RETURNING ... ");
						return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
					}

					if(phiHolding.get(j) > 1.0) {
						phiHolding.remove(j);
						phiHolding.add(j, 1.0);
					}

					sfcHoldingFormDatabaseList = new ArrayList<>();
					fuelFlowHoldingFormDatabaseList = new ArrayList<>();
					for(int iEng=0; iEng<theAircraft.getPowerPlant().getEngineNumber(); iEng++) {
						sfcHoldingFormDatabaseList.add(
								theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(iEng).getSfc(
										holdingMachNumberList.get(j),
										holdingAltitude,
										theOperatingConditions.getDeltaTemperatureClimb(),
										phiHolding.get(j),
										EngineOperatingConditionEnum.CRUISE,
										cruiseCalibrationFactorSFC
										)
								);
						fuelFlowHoldingFormDatabaseList.add(
								thrustHoldingFormDatabaseList.get(iEng).doubleValue(SI.NEWTON)
								*(0.224809)*(0.454/60)
								*sfcHoldingFormDatabaseList.get(iEng)
								);
					}

					sfcHoldingList.add(sfcHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).average().getAsDouble());

					fuelFlowsHolding.add(fuelFlowHoldingFormDatabaseList.stream().mapToDouble(ff -> ff.doubleValue()).sum());

				}

				fuelUsedHolding.addAll(fuelUsedPerStepHolding);
				for(int iHold=0; iHold<timeHoldingArray.size(); iHold++) 
					rangeHolding.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));

				//--------------------------------------------------------------------
				// LANDING
				Amount<Mass> intialMassLanding = Amount.valueOf(
						initialMissionMass.doubleValue(SI.KILOGRAM)
						- theTakeOffCalculator.getFuelUsed().get(theTakeOffCalculator.getFuelUsed().size()-1).doubleValue(SI.KILOGRAM)
						- theClimbCalculator.getFuelUsedClimb().get(theClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStep.get(fuelUsedPerStep.size()-1).doubleValue(SI.KILOGRAM)
						- theFirstDescentCalculator.getFuelUsedPerStep().get(theFirstDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
						- theSecondClimbCalculator.getFuelUsedClimb().get(theSecondClimbCalculator.getFuelUsedClimb().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStepAlternateCruise.get(fuelUsedPerStepAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
						- theSecondDescentCalculator.getFuelUsedPerStep().get(theSecondDescentCalculator.getFuelUsedPerStep().size()-1).doubleValue(SI.KILOGRAM)
						- fuelUsedPerStepHolding.get(fuelUsedPerStepHolding.size()-1).doubleValue(SI.KILOGRAM),
						SI.KILOGRAM
						);

				theLandingCalculator = new LandingCalc(
						holdingAltitude,
						theOperatingConditions.getAltitudeLanding(), 
						theOperatingConditions.getDeltaTemperatureLanding(), 
						approachAngle, 
						intialMassLanding,
						theAircraft.getPowerPlant(),
						polarCLLanding,
						polarCDLanding, 
						theAircraft.getWing().getAspectRatio(), 
						theAircraft.getWing().getSurfacePlanform(),
						freeRollDuration,
						mu, 
						muBrake,
						wingToGroundDistance, 
						kCLmaxLanding, 
						cLmaxLanding, 
						cLZeroLanding, 
						cLAlphaLanding, 
						theOperatingConditions.getThrottleLanding(), 
						cruiseCalibrationFactorThrust,
						flightIdleCalibrationFactorThrust,
						groundIdleCalibrationFactorThrust,
						cruiseCalibrationFactorSFC,
						flightIdleCalibrationFactorSFC,
						groundIdleCalibrationFactorSFC,
						1.0, // cruise NOx correction factor (not needed)
						1.0, // cruise CO correction factor (not needed)
						1.0, // cruise HC correction factor (not needed)
						1.0, // cruise Soot correction factor (not needed)
						1.0, // cruise CO2 correction factor (not needed)
						1.0, // cruise SOx correction factor (not needed)
						1.0, // cruise H2O correction factor (not needed)
						1.0, // fidl NOx correction factor (not needed)
						1.0, // fidl CO correction factor (not needed)
						1.0, // fidl HC correction factor (not needed)
						1.0, // fidl Soot correction factor (not needed)
						1.0, // fidl CO2 correction factor (not needed)
						1.0, // fidl SOx correction factor (not needed)
						1.0, // fidl H2O correction factor (not needed)
						1.0, // gidl NOx correction factor (not needed)
						1.0, // gidl CO correction factor (not needed)
						1.0, // gidl HC correction factor (not needed)
						1.0, // gidl Soot correction factor (not needed)
						1.0, // gidl CO2 correction factor (not needed)
						1.0, // gidl SOx correction factor (not needed)
						1.0, // gidl H2O correction factor (not needed)
						theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);

				theLandingCalculator.calculateLanding(true);

				rangeLanding.addAll(theLandingCalculator.getGroundDistanceList());			
				fuelUsedLanding.addAll(theLandingCalculator.getFuelUsedList());

				//.....................................................................
				// CHECK ON TOTAL ALTERNATE RANGE
				Amount<Length> totalAlternateRange = Amount.valueOf(
						rangeSecondClimb.get(rangeSecondClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeAlternateCruise.get(rangeAlternateCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeSecondDescent.get(rangeSecondDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
						+ rangeHolding.get(rangeHolding.size()-1).doubleValue(NonSI.NAUTICAL_MILE),
						NonSI.NAUTICAL_MILE
						);
				if(Math.abs(alternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) - totalAlternateRange.doubleValue(NonSI.NAUTICAL_MILE)) < 1.0)
					break;

				//.....................................................................
				// NEW ITERATION ALTERNATE CRUISE LENGTH
				currentAlternateCruiseRange = Amount.valueOf( 
						currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
						+ ( alternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE)
								- rangeSecondClimb.get(rangeSecondClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeAlternateCruise.get(rangeAlternateCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeSecondDescent.get(rangeSecondDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								- rangeHolding.get(rangeHolding.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
								),
						NonSI.NAUTICAL_MILE
						);
				if(currentAlternateCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
					missionProfileStopped = Boolean.TRUE;
					System.err.println("WARNING: (NEW ALTERNATE CRUISE LENGTH EVALUATION - PAYLOAD-RANGE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL THAN ZERO, RETURNING ... ");
					return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
				}
			}

			//.....................................................................
			// CHECK ON TOTAL MISSION FUEL
			totalFuelUsed = Amount.valueOf(
					fuelUsedTakeOff.get(fuelUsedTakeOff.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedClimb.get(fuelUsedClimb.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedCruise.get(fuelUsedCruise.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedFirstDescent.get(fuelUsedFirstDescent.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedSecondClimb.get(fuelUsedSecondClimb.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedAlternateCruise.get(fuelUsedAlternateCruise.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedSecondDescent.get(fuelUsedSecondDescent.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedHolding.get(fuelUsedHolding.size()-1).doubleValue(SI.KILOGRAM)
					+ fuelUsedLanding.get(fuelUsedLanding.size()-1).doubleValue(SI.KILOGRAM),
					SI.KILOGRAM
					);

			//.....................................................................
			// NEW ITERATION CRUISE LENGTH
			Amount<Mass> deltaFuel = targetFuelMass.times(1-fuelReserve).minus(totalFuelUsed);
			double meanCruiseFuelFlow = fuelFlows.stream().mapToDouble(ff -> ff).average().getAsDouble();
			double meanCruiseMachNumber = cruiseMissionMachNumber.stream().mapToDouble(m -> m).average().getAsDouble();
			double speedOfSound = AtmosphereCalc.getSpeedOfSound(
					theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER),
					theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
					);
			Amount<Length> deltaCruiseLength = 
					Amount.valueOf(
							(deltaFuel.doubleValue(SI.KILOGRAM)/meanCruiseFuelFlow)
							*meanCruiseMachNumber
							*speedOfSound
							*60,
							SI.METER
							);
			currentCruiseRange = 
					currentCruiseRange.to(NonSI.NAUTICAL_MILE)
					.plus(deltaCruiseLength.to(NonSI.NAUTICAL_MILE));

			if(currentCruiseRange.doubleValue(NonSI.NAUTICAL_MILE) <= 0.0) {
				missionProfileStopped = Boolean.TRUE;
				System.err.println("WARNING: (NEW CRUISE LENGTH EVALUATION - PAYLOAD RANGE) THE NEW ALTERNATE CRUISE LENGTH IS LESS OR EQUAL THAN ZERO, RETURNING ... ");
				return Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
			}

			i++;

		} while ( Math.abs(
				((targetFuelMass.to(SI.KILOGRAM).times(1-fuelReserve)).minus(totalFuelUsed.to(SI.KILOGRAM)))
				.divide(((targetFuelMass.to(SI.KILOGRAM).times(1-fuelReserve))))
				.times(100)
				.getEstimatedValue()
				) >= 0.01
				);
//		while ( Math.abs(
//				(targetFuelMass.to(SI.KILOGRAM).minus(totalFuelUsed.to(SI.KILOGRAM)))
//				.divide(targetFuelMass.to(SI.KILOGRAM))
//				.times(100)
//				.getEstimatedValue()
//				)- (fuelReserve*100)
//				>= 0.01
//				);

		//----------------------------------------------------------------------
		if(theFirstDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - FIRST DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}
		if(theSecondDescentCalculator.getDescentMaxIterationErrorFlag() == true) {
			System.err.println("WARNING: (ITERATIVE LOOP CRUISE/IDLE - SECOND DESCENT) MAX NUMBER OF ITERATION REACHED. THE RATE OF DESCENT MAY DIFFER FROM THE SPECIFIED ONE...");					
		}

		return Amount.valueOf(
				rangeTakeOff.get(rangeTakeOff.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeClimb.get(rangeClimb.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeCruise.get(rangeCruise.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeFirstDescent.get(rangeFirstDescent.size()-1).doubleValue(NonSI.NAUTICAL_MILE)
				+ rangeLanding.get(rangeLanding.size()-1).doubleValue(NonSI.NAUTICAL_MILE),
				NonSI.NAUTICAL_MILE
				);
	}

	/********************************************************************************************
	 * This method allows users to plot the Payload-Range chart, for the best range
	 * Mach and the current one, to the output default folder.
	 * 
	 * @author Vittorio Trifari
	 */
	public void createPayloadRangeChart(String subFolderPath){

		List<String> legend = new ArrayList<>();
		legend.add("Payload");
		legend.add("Payload and Fuel");
		
		List<Double[]> xListSI = new ArrayList<>();
		xListSI.add(MyArrayUtils.convertFromDoubleToPrimitive(rangeArray.stream().mapToDouble(r -> r.doubleValue(SI.KILOMETER)).toArray()));
		xListSI.add(MyArrayUtils.convertFromDoubleToPrimitive(rangeArray.stream().mapToDouble(r -> r.doubleValue(SI.KILOMETER)).toArray()));
		
		List<Double[]> xListIMPERIAL = new ArrayList<>();
		xListIMPERIAL.add(MyArrayUtils.convertFromDoubleToPrimitive(rangeArray.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).toArray()));
		xListIMPERIAL.add(MyArrayUtils.convertFromDoubleToPrimitive(rangeArray.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).toArray()));
		
		List<Double[]> yListSI = new ArrayList<>();
		yListSI.add(MyArrayUtils.convertFromDoubleToPrimitive(payloadMassArray.stream().mapToDouble(r -> r.doubleValue(SI.KILOGRAM)).toArray()));
		yListSI.add(MyArrayUtils.convertFromDoubleToPrimitive(takeOffMassArray.stream().mapToDouble(r -> r.doubleValue(SI.KILOGRAM)).toArray()));
		
		List<Double[]> yListIMPERIAL = new ArrayList<>();
		yListIMPERIAL.add(MyArrayUtils.convertFromDoubleToPrimitive(payloadMassArray.stream().mapToDouble(r -> r.doubleValue(NonSI.POUND)).toArray()));
		yListIMPERIAL.add(MyArrayUtils.convertFromDoubleToPrimitive(takeOffMassArray.stream().mapToDouble(r -> r.doubleValue(NonSI.POUND)).toArray()));
		
		try {
			MyChartToFileUtils.plot(
					xListSI, 
					yListSI, 
					"Payload-Range ( OWE = " + operatingEmptyMass.to(SI.KILOGRAM) + ")", "Range", "Masses", 
					0.0, null, 0.0, null, 
					"km", "kg", 
					true, legend, 
					subFolderPath, "Payload-Range_Mass_SI", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			MyChartToFileUtils.plot(
					xListIMPERIAL, 
					yListIMPERIAL, 
					"Payload-Range ( OWE = " + operatingEmptyMass.to(NonSI.POUND) + ")", "Range", "Masses", 
					0.0, null, 0.0, null, 
					"nm", "lb", 
					true, legend, 
					subFolderPath, "Payload-Range_Mass_IMPERIAL", 
					theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		MyChartToFileUtils.plotNoLegend(
				rangeArray.stream().mapToDouble(r -> r.doubleValue(SI.KILOMETER)).toArray(),
				payloadPassengerNumberArray.stream().mapToDouble(m -> m.doubleValue()).toArray(),   
				0.0, null, 0.0, null,
				"Range", "Payload", 
				"km", "No. Passengers",
				subFolderPath, "Payload-Range_Passengers_SI",     
				theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		MyChartToFileUtils.plotNoLegend(
				rangeArray.stream().mapToDouble(r -> r.doubleValue(NonSI.NAUTICAL_MILE)).toArray(),
				payloadPassengerNumberArray.stream().mapToDouble(m -> m.doubleValue()).toArray(),   
				0.0, null, 0.0, null,
				"Range", "Payload", 
				"nm", "No. Passengers",
				subFolderPath, "Payload-Range_Passengers_IMPERIAL",     
				theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\t\tRANGE AT MAX PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + getRangeAtMaxPayload() + "\n")
				.append("\t\t\tMax take-off mass = " + maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + getMaxPayload() + "\n")
				.append("\t\t\tPassengers number = " + getPassengersNumberAtMaxPayload() + "\n")
				.append("\t\t\tFuel mass required= " + getRequiredMassAtMaxPayload() + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT DESIGN PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + getRangeAtDesignPayload() + "\n")
				.append("\t\t\tMax take-off mass = " + maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + getDesignPayload() + "\n")
				.append("\t\t\tPassengers number = " + getPassengersNumberAtDesignPayload() + "\n")
				.append("\t\t\tFuel mass required= " + getRequiredMassAtDesignPayload() + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT MAX FUEL\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + getRangeAtMaxFuel() + "\n")
				.append("\t\t\tMax take-off mass = " + maximumTakeOffMass + "\n")
				.append("\t\t\tPayload mass = " + getPayloadAtMaxFuel() + "\n")
				.append("\t\t\tPassengers number = " + getPassengersNumberAtMaxFuel() + "\n")
				.append("\t\t\tFuel mass required = " + getMaxFuelMass() + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tRANGE AT ZERO PAYLOAD\n")
				.append("\t\t.....................................\n")
				.append("\t\t\tRange = " + getRangeAtZeroPayload() + "\n")
				.append("\t\t\tMax take-off mass = " + getTakeOffMassZeroPayload() + "\n")
				.append("\t\t\tPayload mass = " + 0.0 + " kg \n")
				.append("\t\t\tPassengers number = " + 0.0 + "\n")
				.append("\t\t\tFuel mass required= " + getMaxFuelMass() + "\n");
		
		sb.append("\t-------------------------------------\n");
		
		return sb.toString();
	}
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public Aircraft getTheAircraft() {
		return theAircraft;
	}

	public void setTheAircraft(Aircraft theAircraft) {
		this.theAircraft = theAircraft;
	}

	public Amount<Length> getMissionRange() {
		return missionRange;
	}

	public void setMissionRange(Amount<Length> missionRange) {
		this.missionRange = missionRange;
	}

	public Amount<Length> getAlternateCruiseRange() {
		return alternateCruiseRange;
	}

	public void setAlternateCruiseRange(Amount<Length> alternateCruiseRange) {
		this.alternateCruiseRange = alternateCruiseRange;
	}

	public Amount<Duration> getHoldingDuration() {
		return holdingDuration;
	}

	public void setHoldingDuration(Amount<Duration> holdingDuration) {
		this.holdingDuration = holdingDuration;
	}

	public double getFuelReserve() {
		return fuelReserve;
	}

	public void setFuelReserve(double fuelReserve) {
		this.fuelReserve = fuelReserve;
	}

	public Amount<Mass> getFirstGuessInitialFuelMass() {
		return firstGuessInitialFuelMass;
	}

	public void setFirstGuessInitialFuelMass(Amount<Mass> firstGuessInitialFuelMass) {
		this.firstGuessInitialFuelMass = firstGuessInitialFuelMass;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
		this.maximumTakeOffMass = maximumTakeOffMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> operatingEmptyMass) {
		this.operatingEmptyMass = operatingEmptyMass;
	}

	public Amount<Mass> getSinglePassengerMass() {
		return singlePassengerMass;
	}

	public void setSinglePassengerMass(Amount<Mass> singlePassengerMass) {
		this.singlePassengerMass = singlePassengerMass;
	}

	public int getDeisngPassengersNumber() {
		return deisgnPassengersNumber;
	}

	public void setDeisngPassengersNumber(int deisngPassengersNumber) {
		this.deisgnPassengersNumber = deisngPassengersNumber;
	}

	public double getcLmaxClean() {
		return cLmaxClean;
	}

	public void setcLmaxClean(double cLmaxClean) {
		this.cLmaxClean = cLmaxClean;
	}

	public double getcLmaxTakeOff() {
		return cLmaxTakeOff;
	}

	public void setcLmaxTakeOff(double cLmaxTakeOff) {
		this.cLmaxTakeOff = cLmaxTakeOff;
	}

	public Amount<?> getcLAlphaTakeOff() {
		return cLAlphaTakeOff;
	}

	public void setcLAlphaTakeOff(Amount<?> cLAlphaTakeOff) {
		this.cLAlphaTakeOff = cLAlphaTakeOff;
	}

	public double getcLZeroTakeOff() {
		return cLZeroTakeOff;
	}

	public void setcLZeroTakeOff(double cLZeroTakeOff) {
		this.cLZeroTakeOff = cLZeroTakeOff;
	}

	public double getcLmaxLanding() {
		return cLmaxLanding;
	}

	public void setcLmaxLanding(double cLmaxLanding) {
		this.cLmaxLanding = cLmaxLanding;
	}

	public Amount<?> getcLAlphaLanding() {
		return cLAlphaLanding;
	}

	public void setcLAlphaLanding(Amount<?> cLAlphaLanding) {
		this.cLAlphaLanding = cLAlphaLanding;
	}

	public double getcLZeroLanding() {
		return cLZeroLanding;
	}

	public void setcLZeroLanding(double cLZeroLanding) {
		this.cLZeroLanding = cLZeroLanding;
	}

	public double[] getPolarCLTakeOff() {
		return polarCLTakeOff;
	}

	public void setPolarCLTakeOff(double[] polarCLTakeOff) {
		this.polarCLTakeOff = polarCLTakeOff;
	}

	public double[] getPolarCDTakeOff() {
		return polarCDTakeOff;
	}

	public void setPolarCDTakeOff(double[] polarCDTakeOff) {
		this.polarCDTakeOff = polarCDTakeOff;
	}

	public double[] getPolarCLClimb() {
		return polarCLClimb;
	}

	public void setPolarCLClimb(double[] polarCLClimb) {
		this.polarCLClimb = polarCLClimb;
	}

	public double[] getPolarCDClimb() {
		return polarCDClimb;
	}

	public void setPolarCDClimb(double[] polarCDClimb) {
		this.polarCDClimb = polarCDClimb;
	}

	public double[] getPolarCLCruise() {
		return polarCLCruise;
	}

	public void setPolarCLCruise(double[] polarCLCruise) {
		this.polarCLCruise = polarCLCruise;
	}

	public double[] getPolarCDCruise() {
		return polarCDCruise;
	}

	public void setPolarCDCruise(double[] polarCDCruise) {
		this.polarCDCruise = polarCDCruise;
	}

	public double[] getPolarCLLanding() {
		return polarCLLanding;
	}

	public void setPolarCLLanding(double[] polarCLLanding) {
		this.polarCLLanding = polarCLLanding;
	}

	public double[] getPolarCDLanding() {
		return polarCDLanding;
	}

	public void setPolarCDLanding(double[] polarCDLanding) {
		this.polarCDLanding = polarCDLanding;
	}

	public Amount<Velocity> getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(Amount<Velocity> windSpeed) {
		this.windSpeed = windSpeed;
	}

	public MyInterpolatingFunction getMu() {
		return mu;
	}

	public void setMu(MyInterpolatingFunction mu) {
		this.mu = mu;
	}

	public MyInterpolatingFunction getMuBrake() {
		return muBrake;
	}

	public void setMuBrake(MyInterpolatingFunction muBrake) {
		this.muBrake = muBrake;
	}

	public Amount<Duration> getDtHold() {
		return dtHold;
	}

	public void setDtHold(Amount<Duration> dtHold) {
		this.dtHold = dtHold;
	}

	public Amount<Angle> getAlphaGround() {
		return alphaGround;
	}

	public void setAlphaGround(Amount<Angle> alphaGround) {
		this.alphaGround = alphaGround;
	}

	public Amount<Length> getObstacleTakeOff() {
		return obstacleTakeOff;
	}

	public void setObstacleTakeOff(Amount<Length> obstacleTakeOff) {
		this.obstacleTakeOff = obstacleTakeOff;
	}

	public double getkRotation() {
		return kRotation;
	}

	public void setkRotation(double kRotation) {
		this.kRotation = kRotation;
	}

	public double getkCLmaxTakeOff() {
		return kCLmaxTakeOff;
	}

	public void setkCLmaxTakeOff(double kCLmaxTakeOff) {
		this.kCLmaxTakeOff = kCLmaxTakeOff;
	}

	public double getDragDueToEnigneFailure() {
		return dragDueToEnigneFailure;
	}

	public void setDragDueToEnigneFailure(double dragDueToEnigneFailure) {
		this.dragDueToEnigneFailure = dragDueToEnigneFailure;
	}

	public double getkAlphaDot() {
		return kAlphaDot;
	}

	public void setkAlphaDot(double kAlphaDot) {
		this.kAlphaDot = kAlphaDot;
	}

	public double getAlphaDotInitial() {
		return alphaDotInitial;
	}

	public void setAlphaDotInitial(double alphaDotInitial) {
		this.alphaDotInitial = alphaDotInitial;
	}

	public Amount<Length> getObstacleLanding() {
		return obstacleLanding;
	}

	public void setObstacleLanding(Amount<Length> obstacleLanding) {
		this.obstacleLanding = obstacleLanding;
	}

	public Amount<Angle> getApproachAngle() {
		return approachAngle;
	}

	public void setApproachAngle(Amount<Angle> approachAngle) {
		this.approachAngle = approachAngle;
	}

	public double getkCLmaxLanding() {
		return kCLmaxLanding;
	}

	public void setkCLmaxLanding(double kCLmaxLanding) {
		this.kCLmaxLanding = kCLmaxLanding;
	}

	public double getkApproach() {
		return kApproach;
	}

	public void setkApproach(double kApproach) {
		this.kApproach = kApproach;
	}

	public double getkFlare() {
		return kFlare;
	}

	public void setkFlare(double kFlare) {
		this.kFlare = kFlare;
	}

	public double getkTouchDown() {
		return kTouchDown;
	}

	public void setkTouchDown(double kTouchDown) {
		this.kTouchDown = kTouchDown;
	}

	public Amount<Duration> getFreeRollDuration() {
		return freeRollDuration;
	}

	public void setFreeRollDuration(Amount<Duration> freeRollDuration) {
		this.freeRollDuration = freeRollDuration;
	}

	public Amount<Velocity> getClimbSpeed() {
		return climbSpeed;
	}

	public void setClimbSpeed(Amount<Velocity> climbSpeed) {
		this.climbSpeed = climbSpeed;
	}

	public Amount<Velocity> getSpeedDescentCAS() {
		return speedDescentCAS;
	}

	public void setSpeedDescentCAS(Amount<Velocity> speedDescentCAS) {
		this.speedDescentCAS = speedDescentCAS;
	}

	public Amount<Velocity> getRateOfDescent() {
		return rateOfDescent;
	}

	public void setRateOfDescent(Amount<Velocity> rateOfDescent) {
		this.rateOfDescent = rateOfDescent;
	}

	public double getTakeOffCalibrationFactorThrust() {
		return takeOffCalibrationFactorThrust;
	}

	public void setTakeOffCalibrationFactorThrust(double takeOffCalibrationFactorThrust) {
		this.takeOffCalibrationFactorThrust = takeOffCalibrationFactorThrust;
	}

	public double getAprCalibrationFactorThrust() {
		return aprCalibrationFactorThrust;
	}

	public void setAprCalibrationFactorThrust(double aprCalibrationFactorThrust) {
		this.aprCalibrationFactorThrust = aprCalibrationFactorThrust;
	}

	public double getClimbCalibrationFactorThrust() {
		return climbCalibrationFactorThrust;
	}

	public void setClimbCalibrationFactorThrust(double climbCalibrationFactorThrust) {
		this.climbCalibrationFactorThrust = climbCalibrationFactorThrust;
	}

	public double getContinuousCalibrationFactorThrust() {
		return continuousCalibrationFactorThrust;
	}

	public void setContinuousCalibrationFactorThrust(double continuousCalibrationFactorThrust) {
		this.continuousCalibrationFactorThrust = continuousCalibrationFactorThrust;
	}

	public double getCruiseCalibrationFactorThrust() {
		return cruiseCalibrationFactorThrust;
	}

	public void setCruiseCalibrationFactorThrust(double cruiseCalibrationFactorThrust) {
		this.cruiseCalibrationFactorThrust = cruiseCalibrationFactorThrust;
	}

	public double getFlightIdleCalibrationFactorThrust() {
		return flightIdleCalibrationFactorThrust;
	}

	public void setFlightIdleCalibrationFactorThrust(double flightIdleCalibrationFactorThrust) {
		this.flightIdleCalibrationFactorThrust = flightIdleCalibrationFactorThrust;
	}

	public double getGroundIdleCalibrationFactorThrust() {
		return groundIdleCalibrationFactorThrust;
	}

	public void setGroundIdleCalibrationFactorThrust(double groundIdleCalibrationFactorThrust) {
		this.groundIdleCalibrationFactorThrust = groundIdleCalibrationFactorThrust;
	}

	public double getTakeOffCalibrationFactorSFC() {
		return takeOffCalibrationFactorSFC;
	}

	public void setTakeOffCalibrationFactorSFC(double takeOffCalibrationFactorSFC) {
		this.takeOffCalibrationFactorSFC = takeOffCalibrationFactorSFC;
	}

	public double getAprCalibrationFactorSFC() {
		return aprCalibrationFactorSFC;
	}

	public void setAprCalibrationFactorSFC(double aprCalibrationFactorSFC) {
		this.aprCalibrationFactorSFC = aprCalibrationFactorSFC;
	}

	public double getClimbCalibrationFactorSFC() {
		return climbCalibrationFactorSFC;
	}

	public void setClimbCalibrationFactorSFC(double climbCalibrationFactorSFC) {
		this.climbCalibrationFactorSFC = climbCalibrationFactorSFC;
	}

	public double getCruiseCalibrationFactorSFC() {
		return cruiseCalibrationFactorSFC;
	}

	public void setCruiseCalibrationFactorSFC(double cruiseCalibrationFactorSFC) {
		this.cruiseCalibrationFactorSFC = cruiseCalibrationFactorSFC;
	}

	public double getFlightIdleCalibrationFactorSFC() {
		return flightIdleCalibrationFactorSFC;
	}

	public void setFlightIdleCalibrationFactorSFC(double flightIdleCalibrationFactorSFC) {
		this.flightIdleCalibrationFactorSFC = flightIdleCalibrationFactorSFC;
	}

	public double getGroundIdleCalibrationFactorSFC() {
		return groundIdleCalibrationFactorSFC;
	}

	public void setGroundIdleCalibrationFactorSFC(double groundIdleCalibrationFactorSFC) {
		this.groundIdleCalibrationFactorSFC = groundIdleCalibrationFactorSFC;
	}

	public Boolean getMissionProfileStopped() {
		return missionProfileStopped;
	}

	public void setMissionProfileStopped(Boolean missionProfileStopped) {
		this.missionProfileStopped = missionProfileStopped;
	}

	public Amount<Length> getRangeAtMaxPayload() {
		return rangeAtMaxPayload;
	}

	public void setRangeAtMaxPayload(Amount<Length> rangeAtMaxPayload) {
		this.rangeAtMaxPayload = rangeAtMaxPayload;
	}

	public Amount<Length> getRangeAtDesignPayload() {
		return rangeAtDesignPayload;
	}

	public void setRangeAtDesignPayload(Amount<Length> rangeAtDesignPayload) {
		this.rangeAtDesignPayload = rangeAtDesignPayload;
	}

	public Amount<Length> getRangeAtMaxFuel() {
		return rangeAtMaxFuel;
	}

	public void setRangeAtMaxFuel(Amount<Length> rangeAtMaxFuel) {
		this.rangeAtMaxFuel = rangeAtMaxFuel;
	}

	public Amount<Length> getRangeAtZeroPayload() {
		return rangeAtZeroPayload;
	}

	public void setRangeAtZeroPayload(Amount<Length> rangeAtZeroPayload) {
		this.rangeAtZeroPayload = rangeAtZeroPayload;
	}

	public Amount<Mass> getTakeOffMassZeroPayload() {
		return takeOffMassZeroPayload;
	}

	public void setTakeOffMassZeroPayload(Amount<Mass> takeOffMassZeroPayload) {
		this.takeOffMassZeroPayload = takeOffMassZeroPayload;
	}

	public Amount<Mass> getMaxPayload() {
		return maxPayload;
	}

	public void setMaxPayload(Amount<Mass> maxPayload) {
		this.maxPayload = maxPayload;
	}

	public Amount<Mass> getDesignPayload() {
		return designPayload;
	}

	public void setDesignPayload(Amount<Mass> designPayload) {
		this.designPayload = designPayload;
	}

	public Integer getPassengersNumberAtMaxPayload() {
		return passengersNumberAtMaxPayload;
	}

	public void setPassengersNumberAtMaxPayload(Integer passengersNumberAtMaxPayload) {
		this.passengersNumberAtMaxPayload = passengersNumberAtMaxPayload;
	}

	public Integer getPassengersNumberAtDesignPayload() {
		return passengersNumberAtDesignPayload;
	}

	public void setPassengersNumberAtDesignPayload(Integer passengersNumberAtDesignPayload) {
		this.passengersNumberAtDesignPayload = passengersNumberAtDesignPayload;
	}

	public Integer getPassengersNumberAtMaxFuel() {
		return passengersNumberAtMaxFuel;
	}

	public void setPassengersNumberAtMaxFuel(Integer passengersNumberAtMaxFuel) {
		this.passengersNumberAtMaxFuel = passengersNumberAtMaxFuel;
	}

	public Amount<Mass> getRequiredMassAtMaxPayload() {
		return requiredMassAtMaxPayload;
	}

	public void setRequiredMassAtMaxPayload(Amount<Mass> requiredMassAtMaxPayload) {
		this.requiredMassAtMaxPayload = requiredMassAtMaxPayload;
	}

	public Amount<Mass> getRequiredMassAtDesignPayload() {
		return requiredMassAtDesignPayload;
	}

	public void setRequiredMassAtDesignPayload(Amount<Mass> requiredMassAtDesignPayload) {
		this.requiredMassAtDesignPayload = requiredMassAtDesignPayload;
	}

	public List<Amount<Length>> getRangeArray() {
		return rangeArray;
	}

	public void setRangeArray(List<Amount<Length>> rangeArray) {
		this.rangeArray = rangeArray;
	}

	public Amount<Mass> getMaxFuelMass() {
		return maxFuelMass;
	}

	public void setMaxFuelMass(Amount<Mass> maxFuelMass) {
		this.maxFuelMass = maxFuelMass;
	}

	public List<Double> getPayloadPassengerNumberArray() {
		return payloadPassengerNumberArray;
	}

	public void setPayloadPassengerNumberArray(List<Double> payloadPassengerNumberArray) {
		this.payloadPassengerNumberArray = payloadPassengerNumberArray;
	}

	public List<Amount<Mass>> getPayloadMassArray() {
		return payloadMassArray;
	}

	public void setPayloadMassArray(List<Amount<Mass>> payloadMassArray) {
		this.payloadMassArray = payloadMassArray;
	}

	public Amount<Mass> getPayloadAtMaxFuel() {
		return payloadAtMaxFuel;
	}

	public void setPayloadAtMaxFuel(Amount<Mass> payloadAtMaxFuel) {
		this.payloadAtMaxFuel = payloadAtMaxFuel;
	}

	public List<Amount<Mass>> getTakeOffMassArray() {
		return takeOffMassArray;
	}

	public void setTakeOffMassArray(List<Amount<Mass>> takeOffMassArray) {
		this.takeOffMassArray = takeOffMassArray;
	}

}
