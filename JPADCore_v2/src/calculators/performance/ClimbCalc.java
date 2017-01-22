package calculators.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class ClimbCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Double _cLmaxClean;
	private Double[] _polarCLClimb;
	private Double[] _polarCDClimb;
	private Amount<Velocity> _climbSpeed;
	private Double _dragDueToEnigneFailure;
	//............................................................................................
	// Output:
	private List<RCMap> _rcMapAOE;
	private List<RCMap> _rcMapOEI;
	private CeilingMap _ceilingMapAOE;
	private CeilingMap _ceilingMapOEI;
	private List<DragMap> _dragListAOE;
	private List<ThrustMap> _thrustListAOE;
	private List<DragMap> _dragListOEI;
	private List<ThrustMap> _thrustListOEI;
	private Map<String, List<Double>> _efficiencyMapAltitudeAOE;
	private Map<String, List<Double>> _efficiencyMapAltitudeOEI;
	private Amount<Velocity> _maxRateOfClimbAtCruiseAltitudeAOE;
	private Amount<Angle> _maxThetaAtCruiseAltitudeAOE;
	private Amount<Length> _absoluteCeilingAOE;
	private Amount<Length> _serviceCeilingAOE;
	private Amount<Duration> _minimumClimbTimeAOE;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedAOE;
	private Amount<Force> _thrustAtClimbStart;
	private Amount<Force> _thrustAtClimbEnding;
	private Amount<Force> _dragAtClimbStart;
	private Amount<Force> _dragAtClimbEnding;
	
	private Amount<Length> _absoluteCeilingOEI;
	private Amount<Length> _serviceCeilingOEI;
	private Amount<Duration> _minimumClimbTimeOEI;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedOEI;
	private Amount<Length> _climbTotalRange;
	private Amount<Mass> _climbTotalFuelUsed;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public ClimbCalc(
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Double cLmaxClean,
			Double[] polarCLClimb,
			Double[] polarCDClimb,
			Amount<Velocity> climbSpeed,
			Double dragDueToEnigneFailure
			) {
		
		this._theAircraft = theAircraft;
		this._theOperatingConditions = theOperatingConditions;
		this._cLmaxClean = cLmaxClean;
		this._polarCLClimb = polarCLClimb;
		this._polarCDClimb = polarCDClimb;
		this._climbSpeed = climbSpeed;
		this._dragDueToEnigneFailure = dragDueToEnigneFailure;
		
		this._rcMapAOE = new ArrayList<>();
		this._rcMapOEI = new ArrayList<>();
		this._dragListAOE = new ArrayList<>();
		this._thrustListAOE = new ArrayList<>();
		this._dragListOEI = new ArrayList<>();
		this._thrustListOEI = new ArrayList<>();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	public void calculateClimbPerformance(
			Amount<Mass> startClimbMassAOE,
			Amount<Mass> startClimbMassOEI,
			Amount<Length> initialClimbAltitude,
			Amount<Length> finalClimbAltitude,
			boolean performOEI
			) {
		
		if(initialClimbAltitude.doubleValue(SI.METER) == finalClimbAltitude.doubleValue(SI.METER))
			finalClimbAltitude = Amount.valueOf(
					initialClimbAltitude.doubleValue(SI.METER) + 0.00001,
					SI.METER
					);
		
		Airfoil meanAirfoil = new Airfoil(
				LiftingSurface.calculateMeanAirfoil(_theAircraft.getWing()),
				_theAircraft.getWing().getAerodynamicDatabaseReader()
				);
		
		double[] altitudeArray = MyArrayUtils.linspace(
				initialClimbAltitude.doubleValue(SI.METER),
				finalClimbAltitude.doubleValue(SI.METER),
				5
				);
							
		//----------------------------------------------------------------------------------
		// ALL OPERATIVE ENGINES (AOE)
		_rcMapAOE = new ArrayList<RCMap>();
		
		_dragListAOE = new ArrayList<DragMap>();
		_thrustListAOE = new ArrayList<ThrustMap>();
		_efficiencyMapAltitudeAOE = new HashMap<>();
		
		double[] speedArrayAOE = new double[100];
		
		for(int i=0; i<altitudeArray.length; i++) {
			//..................................................................................................
			speedArrayAOE = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							altitudeArray[i],
							startClimbMassAOE.times(AtmosphereCalc.g0).getEstimatedValue(),
							_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							_cLmaxClean
							),
					SpeedCalc.calculateTAS(
							_theOperatingConditions.getMachCruise(),
							altitudeArray[i]
							),
					100
					);
			//..................................................................................................
			_dragListAOE.add(
					DragCalc.calculateDragAndPowerRequired(
							altitudeArray[i],
							startClimbMassAOE.times(AtmosphereCalc.g0).getEstimatedValue(),
							speedArrayAOE,
							_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							_cLmaxClean,
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
							meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
							meanAirfoil.getAirfoilCreator().getType()
							)
					);
					
			//..................................................................................................
			_thrustListAOE.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							altitudeArray[i],
							1.0, 	// throttle setting array
							speedArrayAOE,
							EngineOperatingConditionEnum.CLIMB,
							_theAircraft.getPowerPlant().getEngineType(), 
							_theAircraft.getPowerPlant(),
							_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_theAircraft.getPowerPlant().getEngineNumber(),
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
							)
					);
			//..................................................................................................
			List<Double> liftAltitudeParameterization = new ArrayList<>();
			List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
			for(int j=0; j<_dragListAOE.get(i).getSpeed().length; j++) {
				liftAltitudeParameterization.add(
						LiftCalc.calculateLift(
								_dragListAOE.get(i).getSpeed()[j],
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_dragListAOE.get(i).getAltitude(),
								LiftCalc.calculateLiftCoeff(
										startClimbMassAOE.times(AtmosphereCalc.g0).getEstimatedValue(),
										_dragListAOE.get(i).getSpeed()[j],
										_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
										_dragListAOE.get(i).getAltitude()
										)
								)			
						);
				efficiencyListCurrentAltitude.add(
						liftAltitudeParameterization.get(j)
						/ _dragListAOE.get(i).getDrag()[j]
						);
			}
			_efficiencyMapAltitudeAOE.put(
					"Altitude = " + _dragListAOE.get(i).getAltitude(),
					efficiencyListCurrentAltitude
					);
		}
		
		_thrustAtClimbStart = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAOE.get(0).getSpeed(),
								_thrustListAOE.get(0).getThrust(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_thrustAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAOE.get(_thrustListAOE.size()-1).getSpeed(),
								_thrustListAOE.get(_thrustListAOE.size()-1).getThrust(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbStart = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAOE.get(0).getSpeed(),
								_dragListAOE.get(0).getDrag(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAOE.get(_dragListAOE.size()-1).getSpeed(),
								_dragListAOE.get(_dragListAOE.size()-1).getDrag(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		
		//..................................................................................................
		_rcMapAOE.addAll(
				RateOfClimbCalc.calculateRC(
						altitudeArray,
						new double[] {1.0}, 	// throttle setting array
						new double[] {startClimbMassAOE.times(AtmosphereCalc.g0).getEstimatedValue()},
						new EngineOperatingConditionEnum[] {EngineOperatingConditionEnum.CLIMB}, 
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_dragListAOE,
						_thrustListAOE
						)
				);
		//..................................................................................................
		_ceilingMapAOE = PerformanceCalcUtils.calculateCeiling(_rcMapAOE);
		//..................................................................................................
		// COLLECTING RESULTS
		_absoluteCeilingAOE = Amount.valueOf(
				_ceilingMapAOE.getAbsoluteCeiling(),
				SI.METER
				);
		
		_serviceCeilingAOE = Amount.valueOf(
				_ceilingMapAOE.getServiceCeiling(),
				SI.METER
				);
		
		_maxRateOfClimbAtCruiseAltitudeAOE = Amount.valueOf(
				_rcMapAOE.get(_rcMapAOE.size()-1).getRCmax(),
				SI.METERS_PER_SECOND
				);
		
		_maxThetaAtCruiseAltitudeAOE = Amount.valueOf(
				MyArrayUtils.getMax(_rcMapAOE.get(_rcMapAOE.size()-1).getGamma()),
				SI.RADIAN
				);
		
		_minimumClimbTimeAOE = PerformanceCalcUtils.calculateMinimumClimbTime(_rcMapAOE).to(NonSI.MINUTE);
		
		if(_climbSpeed != null)
			_climbTimeAtSpecificClimbSpeedAOE = PerformanceCalcUtils.calculateClimbTime(_rcMapAOE, _climbSpeed).to(NonSI.MINUTE);
		
		//----------------------------------------------------------------------------------
		// ONE ENGINE INOPERATIVE (OEI)
		if(performOEI == true) {
			
			_rcMapOEI = new ArrayList<RCMap>();

			double[] speedArrayOEI = new double[100];

			_dragListOEI = new ArrayList<DragMap>();
			_thrustListOEI = new ArrayList<ThrustMap>();
			_efficiencyMapAltitudeOEI = new HashMap<>();

			for(int i=0; i<altitudeArray.length; i++) {
				//..................................................................................................
				speedArrayOEI = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitudeArray[i],
								startClimbMassOEI.times(AtmosphereCalc.g0).getEstimatedValue(),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean
								),
						SpeedCalc.calculateTAS(
								_theOperatingConditions.getMachCruise(),
								altitudeArray[i]
								),
						100
						);
				//..................................................................................................
				_dragListOEI.add(
						DragCalc.calculateDragAndPowerRequired(
								altitudeArray[i],
								startClimbMassOEI.times(AtmosphereCalc.g0).getEstimatedValue(),
								speedArrayOEI,
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.sumNumberToArrayEBE(MyArrayUtils.convertToDoublePrimitive(_polarCDClimb), _dragDueToEnigneFailure),
								_theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
								meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
								meanAirfoil.getAirfoilCreator().getType()
								)
						);
				//..................................................................................................
				_thrustListOEI.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								altitudeArray[i],
								1.0, 	// throttle setting array
								speedArrayOEI,
								EngineOperatingConditionEnum.CONTINUOUS,
								_theAircraft.getPowerPlant().getEngineType(), 
								_theAircraft.getPowerPlant(),
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber()-1,
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR()
								)
						);
				//..................................................................................................
				List<Double> liftAltitudeParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
				for(int j=0; j<_dragListOEI.get(i).getSpeed().length; j++) {
					liftAltitudeParameterization.add(
							LiftCalc.calculateLift(
									_dragListOEI.get(i).getSpeed()[j],
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									_dragListOEI.get(i).getAltitude(),
									LiftCalc.calculateLiftCoeff(
											startClimbMassOEI.times(AtmosphereCalc.g0).getEstimatedValue(),
											_dragListOEI.get(i).getSpeed()[j],
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_dragListOEI.get(i).getAltitude()
											)
									)			
							);
					efficiencyListCurrentAltitude.add(
							liftAltitudeParameterization.get(j)
							/ _dragListOEI.get(i).getDrag()[j]
							);
				}
				_efficiencyMapAltitudeOEI.put(
						"Altitude = " + _dragListOEI.get(i).getAltitude(),
						efficiencyListCurrentAltitude
						);
			}
			//..................................................................................................
			_rcMapOEI.addAll(
					RateOfClimbCalc.calculateRC(
							altitudeArray,
							new double[] {1.0}, 	// throttle setting array
							new double[] {startClimbMassOEI.times(AtmosphereCalc.g0).getEstimatedValue()},
							new EngineOperatingConditionEnum[] {EngineOperatingConditionEnum.CONTINUOUS}, 
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
							_dragListOEI,
							_thrustListOEI
							)
					);
			//..................................................................................................
			_ceilingMapOEI = PerformanceCalcUtils.calculateCeiling(_rcMapOEI);
			//..................................................................................................
			// COLLECTING RESULTS
			_absoluteCeilingOEI = Amount.valueOf(
					_ceilingMapOEI.getAbsoluteCeiling(),
					SI.METER
					);

			_serviceCeilingOEI = Amount.valueOf(
					_ceilingMapOEI.getServiceCeiling(),
					SI.METER
					);

			_minimumClimbTimeOEI = PerformanceCalcUtils.calculateMinimumClimbTime(_rcMapOEI).to(NonSI.MINUTE);

			if(_climbSpeed != null)
				_climbTimeAtSpecificClimbSpeedOEI = PerformanceCalcUtils.calculateClimbTime(_rcMapOEI, _climbSpeed).to(NonSI.MINUTE);

		}
		
		//----------------------------------------------------------------------------------
		// SFC AND RANGE IN AOE CONDITION (for the mission profile)
		List<Double> sfcListClimb = new ArrayList<>();
		List<Amount<Duration>> timeArrayClimb = new ArrayList<>();
		List<Amount<Length>> rangeArrayClimb = new ArrayList<>();
		List<Double> rcAtClimbSpeed = new ArrayList<>();
		List<Amount<Velocity>> climbSpeedTAS = new ArrayList<>();
		
		timeArrayClimb.add(Amount.valueOf(0.0, SI.SECOND));
		rangeArrayClimb.add(Amount.valueOf(0.0, SI.METER));
		
		for(int i=0; i<_rcMapAOE.size(); i++) {
			if(_climbSpeed != null) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAOE.get(i).getAltitude()
						).getDensity()*1000/1.225;
				
				rcAtClimbSpeed.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_rcMapAOE.get(i).getSpeed(),
								_rcMapAOE.get(i).getRC(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								)
						);
				climbSpeedTAS.add(_climbSpeed.divide(Math.sqrt(sigma)));
			}
		}
		
		for(int i=1; i<_rcMapAOE.size(); i++) {
			
			if(_climbSpeed == null) {
				timeArrayClimb.add(
						timeArrayClimb.get(timeArrayClimb.size()-1)
						.plus(
								Amount.valueOf(
										MyMathUtils.integrate1DSimpsonSpline(
												new double[] { 
														_rcMapAOE.get(i-1).getAltitude(),
														_rcMapAOE.get(i).getAltitude()
												},
												new double[] {
														1/_rcMapAOE.get(i-1).getRCmax(),
														1/_rcMapAOE.get(i).getRCmax()
												}
												),
										SI.SECOND
										)
								)
						);

				rangeArrayClimb.add(
						rangeArrayClimb.get(rangeArrayClimb.size()-1)
						.plus(
								Amount.valueOf(
										((_rcMapAOE.get(i-1).getRCMaxSpeed()+_rcMapAOE.get(i).getRCMaxSpeed())/2)
										*(timeArrayClimb.get(i).minus(timeArrayClimb.get(i-1)).doubleValue(SI.SECOND))
										*Math.cos(((_rcMapAOE.get(i-1).getTheta()+_rcMapAOE.get(i).getTheta())/2)),
										SI.METER
										)
								)
						);
			}
			else {
				timeArrayClimb.add(
						timeArrayClimb.get(timeArrayClimb.size()-1)
						.plus(
								Amount.valueOf(
										MyMathUtils.integrate1DSimpsonSpline(
												new double[] { 
														_rcMapAOE.get(i-1).getAltitude(),
														_rcMapAOE.get(i).getAltitude()
												},
												new double[] {
														1/rcAtClimbSpeed.get(i-1),
														1/rcAtClimbSpeed.get(i)
												}
												),
										SI.SECOND
										)
								)
						);

				rangeArrayClimb.add(
						rangeArrayClimb.get(rangeArrayClimb.size()-1)
						.plus(
								Amount.valueOf(
										((climbSpeedTAS.get(i-1).plus(climbSpeedTAS.get(i))).divide(2)).getEstimatedValue()
										*(timeArrayClimb.get(i).minus(timeArrayClimb.get(i-1)).doubleValue(SI.SECOND))
										*Math.cos(((_rcMapAOE.get(i-1).getTheta()+_rcMapAOE.get(i).getTheta())/2)),
										SI.METER
										)
								)
						);
			}
			
		}
		
		_climbTotalRange = rangeArrayClimb.get(rangeArrayClimb.size()-1);
		
		for(int i=0; i<_rcMapAOE.size(); i++) {

			if(_climbSpeed == null)
				sfcListClimb.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAOE.get(i).getSpeed(),
								_thrustListAOE.get(i).getThrust(),
								_rcMapAOE.get(i).getRCMaxSpeed()
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								SpeedCalc.calculateMach(
										_rcMapAOE.get(i).getAltitude(),
										_rcMapAOE.get(i).getRCMaxSpeed()
										),
								_rcMapAOE.get(i).getAltitude(),
								(MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAOE.get(i).getSpeed(),
										_thrustListAOE.get(i).getThrust(),
										_rcMapAOE.get(i).getRCMaxSpeed()
										)/2)/_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CLIMB,
								_theAircraft.getPowerPlant()
								)
						);
			
			else
				sfcListClimb.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAOE.get(i).getSpeed(),
								_thrustListAOE.get(i).getThrust(),
								climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								SpeedCalc.calculateMach(
										_rcMapAOE.get(i).getAltitude(),
										climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
										),
								_rcMapAOE.get(i).getAltitude(),
								(MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAOE.get(i).getSpeed(),
										_thrustListAOE.get(i).getThrust(),
										climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
										)/2)/_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CLIMB,
								_theAircraft.getPowerPlant()
								)
						);
		}

		_climbTotalFuelUsed = Amount.valueOf(
				MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								timeArrayClimb.stream()
									.map(t -> t.to(NonSI.MINUTE))
										.collect(Collectors.toList()
												)
										),
						MyArrayUtils.convertToDoublePrimitive(sfcListClimb)
						),
				SI.KILOGRAM					
				);
		
	}
	
	public void plotClimbPerformance(List<PerformancePlotEnum> plotList, String climbFolderPath) {
		
		if(plotList.contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CLIMB)) {

			//.......................................................
			// AOE
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> dragAndThrustAOE = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_dragListAOE.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getSpeed()));
				dragAndThrustAOE.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getDrag()));
				legend.add("Drag at " + _dragListAOE.get(i).getAltitude() + " m");
			}
			for (int i=0; i<_thrustListAOE.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAOE.get(i).getSpeed()));
				dragAndThrustAOE.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAOE.get(i).getThrust()));
				legend.add("Thrust at " + _thrustListAOE.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, dragAndThrustAOE,
						"Drag and Thrust curves (AOE)",
						"Speed", "Forces",
						null, null, null, null,
						"m/s", "N",
						true, legend,
						climbFolderPath, "Drag_and_Thrust_curves_CLIMB_AOE"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> dragAndThrustOEI = new ArrayList<Double[]>();

			for (int i=0; i<_dragListAOE.size(); i++) {
				dragAndThrustOEI.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getDrag()));
			}
			for (int i=0; i<_thrustListOEI.size(); i++) {
				dragAndThrustOEI.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListOEI.get(i).getThrust()));
			}

			try {
				MyChartToFileUtils.plot(
						speed, dragAndThrustOEI,
						"Drag and Thrust curves (OEI)",
						"Speed", "Forces",
						null, null, null, null,
						"m/s", "N",
						true, legend,
						climbFolderPath, "Drag_and_Thrust_curves_CONTINOUS_OEI"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CLIMB)) {

			//.......................................................
			// AOE
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> powerNeededAndAvailableAOE = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_dragListAOE.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getSpeed()));
				powerNeededAndAvailableAOE.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getPower()));
				legend.add("Power needed at " + _dragListAOE.get(i).getAltitude() + " m");
			}
			for (int i=0; i<_thrustListAOE.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAOE.get(i).getSpeed()));
				powerNeededAndAvailableAOE.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAOE.get(i).getPower()));
				legend.add("Power available at " + _thrustListAOE.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, powerNeededAndAvailableAOE,
						"Power Needed and Power Available curves (AOE)",
						"Speed", "Power",
						null, null, null, null,
						"m/s", "W",
						true, legend,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CLIMB_AOE"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> powerNeededAndAvailableOEI = new ArrayList<Double[]>();

			for (int i=0; i<_dragListAOE.size(); i++) {
				powerNeededAndAvailableOEI.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAOE.get(i).getPower()));
			}
			for (int i=0; i<_thrustListOEI.size(); i++) {
				powerNeededAndAvailableOEI.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListOEI.get(i).getPower()));
			}

			try {
				MyChartToFileUtils.plot(
						speed, powerNeededAndAvailableOEI,
						"Power Needed and Power Available curves (OEI)",
						"Speed", "Power",
						null, null, null, null,
						"m/s", "W",
						true, legend,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CONTINOUS_OEI"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.EFFICIENCY_CURVES_CLIMB)) {

			//.......................................................
			// AOE
			List<Double[]> speedListAltitudeParameterizationAOE = new ArrayList<>();
			List<Double[]> efficiencyListAltitudeParameterizationAOE = new ArrayList<>();
			List<String> legendAltitudeAOE = new ArrayList<>();
			for(int i=0; i<_dragListAOE.size(); i++) {
				speedListAltitudeParameterizationAOE.add(
						MyArrayUtils.convertFromDoublePrimitive(
								_dragListAOE.get(i).getSpeed()
								)
						);
				efficiencyListAltitudeParameterizationAOE.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitudeAOE.get(
												"Altitude = " + _dragListAOE.get(i).getAltitude()
												)
										)
								)
						);
				legendAltitudeAOE.add("Altitude = " + _dragListAOE.get(i).getAltitude());
			}
			
			try {
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAOE, efficiencyListAltitudeParameterizationAOE,
						"Efficiency curves at different altitudes (AOE)",
						"Speed", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeAOE,
						climbFolderPath, "Efficiency_curves_altitude_AOE"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> speedListAltitudeParameterizationOEI = new ArrayList<>();
			List<Double[]> efficiencyListAltitudeParameterizationOEI = new ArrayList<>();
			List<String> legendAltitudeOEI = new ArrayList<>();
			for(int i=0; i<_dragListOEI.size(); i++) {
				speedListAltitudeParameterizationOEI.add(
						MyArrayUtils.convertFromDoublePrimitive(
								_dragListOEI.get(i).getSpeed()
								)
						);
				efficiencyListAltitudeParameterizationOEI.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitudeOEI.get(
												"Altitude = " + _dragListOEI.get(i).getAltitude()
												)
										)
								)
						);
				legendAltitudeOEI.add("Altitude = " + _dragListOEI.get(i).getAltitude());
			}
			
			try {
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationOEI, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Speed", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeOEI,
						climbFolderPath, "Efficiency_curves_altitude_OEI"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.RATE_OF_CLIMB_CURVES)) {

			//.......................................................
			// AOE
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> speedCAS = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> rateOfClimbAOE = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_rcMapAOE.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAOE.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAOE.get(i).getAltitude()
						).getSpeedOfSound();
				
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAOE.get(i).getSpeed()));
				speedCAS.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speed.get(i)),
										Math.sqrt(sigma)
										)
								)
						);
				mach.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speed.get(i)),
										1/speedOfSound
										)
								)
						);
				rateOfClimbAOE.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAOE.get(i).getRC()));
				legend.add("Rate of climb at " + _rcMapAOE.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, rateOfClimbAOE,
						"Rate of Climb curves (AOE)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AOE_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, rateOfClimbAOE,
						"Rate of Climb curves (AOE)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AOE_CAS"
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbAOE,
						"Rate of Climb curves (AOE)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AOE_MACH"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> rateOfClimbOEI = new ArrayList<Double[]>();

			for (int i=0; i<_rcMapOEI.size(); i++) {
				rateOfClimbOEI.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapOEI.get(i).getRC()));
			}

			try {
				MyChartToFileUtils.plot(
						speed, rateOfClimbOEI,
						"Rate of Climb curves (OEI)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_OEI_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, rateOfClimbOEI,
						"Rate of Climb curves (OEI)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_OEI_CAS"
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbOEI,
						"Rate of Climb curves (OEI)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_OEI_MACH"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.CLIMB_ANGLE_CURVES)) {

			//.......................................................
			// AOE
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> speedCAS = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> climbAngleAOE = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_rcMapAOE.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAOE.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAOE.get(i).getAltitude()
						).getSpeedOfSound();
				
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAOE.get(i).getSpeed()));
				speedCAS.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speed.get(i)),
										Math.sqrt(sigma)
										)
								)
						);
				mach.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speed.get(i)),
										1/speedOfSound
										)
								)
						);
				climbAngleAOE.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										_rcMapAOE.get(i).getGamma(),
										57.3)
								)
						);
				legend.add("Climb angle at " + _rcMapAOE.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, climbAngleAOE,
						"Climb angle curves (AOE)",
						"Speed (TAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AOE_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, climbAngleAOE,
						"Climb angle curves (AOE)",
						"Speed (CAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AOE_CAS"
						);
				MyChartToFileUtils.plot(
						mach, climbAngleAOE,
						"Climb angle curves (AOE)",
						"Mach", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AOE_MACH"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> climbAngleOEI = new ArrayList<Double[]>();

			for (int i=0; i<_rcMapOEI.size(); i++) {
				climbAngleOEI.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										_rcMapOEI.get(i).getGamma(),
										57.3)
								)
						);
			}

			try {
				MyChartToFileUtils.plot(
						speed, climbAngleOEI,
						"Climb angle curves (OEI)",
						"Speed (TAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_OEI_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, climbAngleOEI,
						"Climb angle curves (OEI)",
						"Speed (CAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_OEI_CAS"
						);
				MyChartToFileUtils.plot(
						mach, climbAngleOEI,
						"Climb angle curves (OEI)",
						"Mach", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_OEI_MACH"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.MAX_RATE_OF_CLIMB_ENVELOPE)) {

			//.......................................................
			// AOE
			List<Double> maxRateOfClimbListAOE = new ArrayList<Double>();
			List<Double> altitudeListAOE = new ArrayList<Double>();

			maxRateOfClimbListAOE.add(0.0);
			altitudeListAOE.add(_absoluteCeilingAOE.doubleValue(SI.METER));
			for(int i=0; i<_rcMapAOE.size(); i++) {
				maxRateOfClimbListAOE.add(_rcMapAOE.get(_rcMapAOE.size()-1-i).getRCmax());
				altitudeListAOE.add(_rcMapAOE.get(_rcMapAOE.size()-1-i).getAltitude());
			}

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListAOE),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAOE),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"m/s", "m",
					climbFolderPath, "Max_Rate_of_Climb_envelope_(AOE)"
					);

			//.......................................................
			// OEI
			List<Double> maxRateOfClimbListOEI = new ArrayList<Double>();
			List<Double> altitudeListOEI = new ArrayList<Double>();

			maxRateOfClimbListOEI.add(0.0);
			altitudeListOEI.add(_absoluteCeilingOEI.doubleValue(SI.METER));
			for(int i=0; i<_rcMapOEI.size(); i++) {
				maxRateOfClimbListOEI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getRCmax());
				altitudeListOEI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude());
			}

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListOEI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"m/s", "m",
					climbFolderPath, "Max_Rate_of_Climb_envelope_(OEI)"
					);

		}
		
		if(plotList.contains(PerformancePlotEnum.MAX_CLIMB_ANGLE_ENVELOPE)) {

			//.......................................................
			// AOE
			List<Double> maxClimbAngleListAOE = new ArrayList<Double>();
			List<Double> altitudeListAOE = new ArrayList<Double>();
			
			maxClimbAngleListAOE.add(0.0);
			altitudeListAOE.add(_absoluteCeilingAOE.doubleValue(SI.METER));
			for(int i=0; i<_rcMapAOE.size(); i++) {
				maxClimbAngleListAOE.add(MyArrayUtils.getMax(_rcMapAOE.get(_rcMapAOE.size()-1-i).getGamma()));
				altitudeListAOE.add(_rcMapAOE.get(_rcMapAOE.size()-1-i).getAltitude());
			}
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListAOE),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAOE),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_(AOE)"
					);
			
			//.......................................................
			// OEI
			List<Double> maxClimbAngleListOEI = new ArrayList<Double>();
			List<Double> altitudeListOEI = new ArrayList<Double>();
			
			maxClimbAngleListOEI.add(0.0);
			altitudeListOEI.add(_absoluteCeilingOEI.doubleValue(SI.METER));
			for(int i=0; i<_rcMapOEI.size(); i++) {
				altitudeListOEI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude());	
				maxClimbAngleListOEI.add(MyArrayUtils.getMax(_rcMapOEI.get(_rcMapOEI.size()-1-i).getGamma()));
			}
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListAOE),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_(OEI)"
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


	public Double getCLmaxClean() {
		return _cLmaxClean;
	}


	public void setCLmaxClean(Double _cLmaxClean) {
		this._cLmaxClean = _cLmaxClean;
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


	public Amount<Velocity> getClimbSpeed() {
		return _climbSpeed;
	}


	public void setClimbSpeed(Amount<Velocity> _climbSpeed) {
		this._climbSpeed = _climbSpeed;
	}


	public Double getDragDueToEnigneFailure() {
		return _dragDueToEnigneFailure;
	}


	public void setDragDueToEnigneFailure(Double _dragDueToEnigneFailure) {
		this._dragDueToEnigneFailure = _dragDueToEnigneFailure;
	}


	public List<RCMap> getRCMapAOE() {
		return _rcMapAOE;
	}


	public void setRCMapAOE(List<RCMap> _rcMapAOE) {
		this._rcMapAOE = _rcMapAOE;
	}


	public List<RCMap> getRCMapOEI() {
		return _rcMapOEI;
	}


	public void setRCMapOEI(List<RCMap> _rcMapOEI) {
		this._rcMapOEI = _rcMapOEI;
	}


	public CeilingMap getCeilingMapAOE() {
		return _ceilingMapAOE;
	}


	public void setCeilingMapAOE(CeilingMap _ceilingMapAOE) {
		this._ceilingMapAOE = _ceilingMapAOE;
	}


	public CeilingMap getCeilingMapOEI() {
		return _ceilingMapOEI;
	}


	public void setCeilingMapOEI(CeilingMap _ceilingMapOEI) {
		this._ceilingMapOEI = _ceilingMapOEI;
	}


	public List<DragMap> getDragListAOE() {
		return _dragListAOE;
	}


	public void setDragListAOE(List<DragMap> _dragListAOE) {
		this._dragListAOE = _dragListAOE;
	}


	public List<ThrustMap> getThrustListAOE() {
		return _thrustListAOE;
	}


	public void setThrustListAOE(List<ThrustMap> _thrustListAOE) {
		this._thrustListAOE = _thrustListAOE;
	}


	public List<DragMap> getDragListOEI() {
		return _dragListOEI;
	}


	public void setDragListOEI(List<DragMap> _dragListOEI) {
		this._dragListOEI = _dragListOEI;
	}


	public List<ThrustMap> getThrustListOEI() {
		return _thrustListOEI;
	}


	public void setThrustListOEI(List<ThrustMap> _thrustListOEI) {
		this._thrustListOEI = _thrustListOEI;
	}


	public Amount<Velocity> getMaxRateOfClimbAtCruiseAltitudeAOE() {
		return _maxRateOfClimbAtCruiseAltitudeAOE;
	}


	public void setMaxRateOfClimbAtCruiseAltitudeAOE(Amount<Velocity> _maxRateOfClimbAtCruiseAltitudeAOE) {
		this._maxRateOfClimbAtCruiseAltitudeAOE = _maxRateOfClimbAtCruiseAltitudeAOE;
	}


	public Amount<Angle> getMaxThetaAtCruiseAltitudeAOE() {
		return _maxThetaAtCruiseAltitudeAOE;
	}


	public void setMaxThetaAtCruiseAltitudeAOE(Amount<Angle> _maxThetaAtCruiseAltitudeAOE) {
		this._maxThetaAtCruiseAltitudeAOE = _maxThetaAtCruiseAltitudeAOE;
	}


	public Amount<Length> getAbsoluteCeilingAOE() {
		return _absoluteCeilingAOE;
	}


	public void setAbsoluteCeilingAOE(Amount<Length> _absoluteCeilingAOE) {
		this._absoluteCeilingAOE = _absoluteCeilingAOE;
	}


	public Amount<Length> getServiceCeilingAOE() {
		return _serviceCeilingAOE;
	}


	public void setServiceCeilingAOE(Amount<Length> _serviceCeilingAOE) {
		this._serviceCeilingAOE = _serviceCeilingAOE;
	}


	public Amount<Duration> getMinimumClimbTimeAOE() {
		return _minimumClimbTimeAOE;
	}


	public void setMinimumClimbTimeAOE(Amount<Duration> _minimumClimbTimeAOE) {
		this._minimumClimbTimeAOE = _minimumClimbTimeAOE;
	}


	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedAOE() {
		return _climbTimeAtSpecificClimbSpeedAOE;
	}


	public void setClimbTimeAtSpecificClimbSpeedAOE(Amount<Duration> _climbTimeAtSpecificClimbSpeedAOE) {
		this._climbTimeAtSpecificClimbSpeedAOE = _climbTimeAtSpecificClimbSpeedAOE;
	}


	public Amount<Length> getAbsoluteCeilingOEI() {
		return _absoluteCeilingOEI;
	}


	public void setAbsoluteCeilingOEI(Amount<Length> _absoluteCeilingOEI) {
		this._absoluteCeilingOEI = _absoluteCeilingOEI;
	}


	public Amount<Length> getServiceCeilingOEI() {
		return _serviceCeilingOEI;
	}


	public void setServiceCeilingOEI(Amount<Length> _serviceCeilingOEI) {
		this._serviceCeilingOEI = _serviceCeilingOEI;
	}


	public Amount<Duration> getMinimumClimbTimeOEI() {
		return _minimumClimbTimeOEI;
	}


	public void setMinimumClimbTimeOEI(Amount<Duration> _minimumClimbTimeOEI) {
		this._minimumClimbTimeOEI = _minimumClimbTimeOEI;
	}


	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedOEI() {
		return _climbTimeAtSpecificClimbSpeedOEI;
	}


	public void setClimbTimeAtSpecificClimbSpeedOEI(Amount<Duration> _climbTimeAtSpecificClimbSpeedOEI) {
		this._climbTimeAtSpecificClimbSpeedOEI = _climbTimeAtSpecificClimbSpeedOEI;
	}

	public Amount<Length> getClimbTotalRange() {
		return _climbTotalRange;
	}

	public void setClimbTotalRange(Amount<Length> _climbTotalRange) {
		this._climbTotalRange = _climbTotalRange;
	}

	public Amount<Mass> getClimbTotalFuelUsed() {
		return _climbTotalFuelUsed;
	}

	public void setClimbTotalFuelUsed(Amount<Mass> _climbTotalFuelUsed) {
		this._climbTotalFuelUsed = _climbTotalFuelUsed;
	}

	public Map<String, List<Double>> getEfficiencyMapAltitudeAOE() {
		return _efficiencyMapAltitudeAOE;
	}

	public void setEfficiencyMapAltitudeAOE(Map<String, List<Double>> _efficiencyMapAltitude) {
		this._efficiencyMapAltitudeAOE = _efficiencyMapAltitude;
	}

	public Map<String, List<Double>> getEfficiencyMapAltitudeOEI() {
		return _efficiencyMapAltitudeOEI;
	}

	public void setEfficiencyMapAltitudeOEI(Map<String, List<Double>> _efficiencyMapAltitudeOEI) {
		this._efficiencyMapAltitudeOEI = _efficiencyMapAltitudeOEI;
	}

	public Amount<Force> getDragAtClimbEnding() {
		return _dragAtClimbEnding;
	}

	public void setDragAtClimbEnding(Amount<Force> _dragAtClimbEnding) {
		this._dragAtClimbEnding = _dragAtClimbEnding;
	}

	public Amount<Force> getDragAtClimbStart() {
		return _dragAtClimbStart;
	}

	public void setDragAtClimbStart(Amount<Force> _dragAtClimbStart) {
		this._dragAtClimbStart = _dragAtClimbStart;
	}

	public Amount<Force> getThrustAtClimbEnding() {
		return _thrustAtClimbEnding;
	}

	public void setThrustAtClimbEnding(Amount<Force> _thrustAtClimbEnding) {
		this._thrustAtClimbEnding = _thrustAtClimbEnding;
	}

	public Amount<Force> getThrustAtClimbStart() {
		return _thrustAtClimbStart;
	}

	public void setThrustAtClimbStart(Amount<Force> _thrustAtClimbStart) {
		this._thrustAtClimbStart = _thrustAtClimbStart;
	}
	
}
