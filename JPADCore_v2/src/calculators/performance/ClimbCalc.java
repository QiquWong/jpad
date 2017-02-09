package calculators.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	private List<RCMap> _rcMapAEO;
	private List<RCMap> _rcMapOEI;
	private CeilingMap _ceilingMapAEO;
	private CeilingMap _ceilingMapOEI;
	private List<DragMap> _dragListAEO;
	private List<ThrustMap> _thrustListAEO;
	private List<DragMap> _dragListOEI;
	private List<ThrustMap> _thrustListOEI;
	private Map<String, List<Double>> _efficiencyMapAltitudeAEO;
	private Map<String, List<Double>> _efficiencyMapAltitudeOEI;
	private List<Amount<Duration>> _climbTimeListAEO;
	private List<Amount<Duration>> _climbTimeListRCmax;
	private List<Amount<Mass>> _fuelUsedList;
	private Amount<Length> _absoluteCeilingAEO;
	private Amount<Length> _serviceCeilingAEO;
	private Amount<Duration> _minimumClimbTimeAEO;
	private Amount<Duration> _climbTimeAtSpecificClimbSpeedAEO;
	private Amount<Force> _thrustAtClimbStart;
	private Amount<Force> _thrustAtClimbEnding;
	private Amount<Force> _dragAtClimbStart;
	private Amount<Force> _dragAtClimbEnding;
	
	private Amount<Length> _absoluteCeilingOEI;
	private Amount<Length> _serviceCeilingOEI;
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
		
		this._rcMapAEO = new ArrayList<>();
		this._rcMapOEI = new ArrayList<>();
		this._dragListAEO = new ArrayList<>();
		this._thrustListAEO = new ArrayList<>();
		this._dragListOEI = new ArrayList<>();
		this._thrustListOEI = new ArrayList<>();
		this._fuelUsedList = new ArrayList<>();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	public void calculateClimbPerformance(
			Amount<Mass> startClimbMassAEO,
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
		// ALL OPERATIVE ENGINES (AEO)
		_rcMapAEO = new ArrayList<RCMap>();
		
		_dragListAEO = new ArrayList<DragMap>();
		_thrustListAEO = new ArrayList<ThrustMap>();
		_efficiencyMapAltitudeAEO = new HashMap<>();
		
		double[] speedArrayAEO = new double[100];
		
		for(int i=0; i<altitudeArray.length; i++) {
			//..................................................................................................
			speedArrayAEO = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							altitudeArray[i],
							startClimbMassAEO.times(AtmosphereCalc.g0).getEstimatedValue(),
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
			_dragListAEO.add(
					DragCalc.calculateDragAndPowerRequired(
							altitudeArray[i],
							startClimbMassAEO.times(AtmosphereCalc.g0).getEstimatedValue(),
							speedArrayAEO,
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
			_thrustListAEO.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							altitudeArray[i],
							1.0, 	// throttle setting array
							speedArrayAEO,
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
			for(int j=0; j<_dragListAEO.get(i).getSpeed().length; j++) {
				liftAltitudeParameterization.add(
						LiftCalc.calculateLift(
								_dragListAEO.get(i).getSpeed()[j],
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								_dragListAEO.get(i).getAltitude(),
								LiftCalc.calculateLiftCoeff(
										startClimbMassAEO.times(AtmosphereCalc.g0).getEstimatedValue(),
										_dragListAEO.get(i).getSpeed()[j],
										_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
										_dragListAEO.get(i).getAltitude()
										)
								)			
						);
				efficiencyListCurrentAltitude.add(
						liftAltitudeParameterization.get(j)
						/ _dragListAEO.get(i).getDrag()[j]
						);
			}
			_efficiencyMapAltitudeAEO.put(
					"Altitude = " + _dragListAEO.get(i).getAltitude(),
					efficiencyListCurrentAltitude
					);
		}
		
		_thrustAtClimbStart = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(0).getSpeed(),
								_thrustListAEO.get(0).getThrust(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_thrustAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(_thrustListAEO.size()-1).getSpeed(),
								_thrustListAEO.get(_thrustListAEO.size()-1).getThrust(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbStart = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAEO.get(0).getSpeed(),
								_dragListAEO.get(0).getDrag(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAEO.get(_dragListAEO.size()-1).getSpeed(),
								_dragListAEO.get(_dragListAEO.size()-1).getDrag(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		
		//..................................................................................................
		_rcMapAEO.addAll(
				RateOfClimbCalc.calculateRC(
						altitudeArray,
						new double[] {1.0}, 	// throttle setting array
						new double[] {startClimbMassAEO.times(AtmosphereCalc.g0).getEstimatedValue()},
						new EngineOperatingConditionEnum[] {EngineOperatingConditionEnum.CLIMB}, 
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_dragListAEO,
						_thrustListAEO
						)
				);
		//..................................................................................................
		_ceilingMapAEO = PerformanceCalcUtils.calculateCeiling(_rcMapAEO);
		//..................................................................................................
		// COLLECTING RESULTS
		_absoluteCeilingAEO = Amount.valueOf(
				_ceilingMapAEO.getAbsoluteCeiling(),
				SI.METER
				);
		
		_serviceCeilingAEO = Amount.valueOf(
				_ceilingMapAEO.getServiceCeiling(),
				SI.METER
				);
		
		_minimumClimbTimeAEO = PerformanceCalcUtils.calculateMinimumClimbTime(_rcMapAEO).to(NonSI.MINUTE);
		
		if(_climbSpeed != null)
			_climbTimeAtSpecificClimbSpeedAEO = PerformanceCalcUtils.calculateClimbTime(_rcMapAEO, _climbSpeed).to(NonSI.MINUTE);
		
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

		}
		
		//--------------------------------------------------------------------------------------
		// TIME AT RCMax SPEED
		_climbTimeListRCmax = new ArrayList<>();
		_climbTimeListRCmax.add(Amount.valueOf(0.0, SI.SECOND));
		
		for(int i=1; i<_rcMapAEO.size(); i++) {
			
			_climbTimeListRCmax.add(
					_climbTimeListRCmax.get(_climbTimeListRCmax.size()-1)
					.plus(
							Amount.valueOf(
									MyMathUtils.integrate1DSimpsonSpline(
											new double[] { 
													_rcMapAEO.get(i-1).getAltitude(),
													_rcMapAEO.get(i).getAltitude()
											},
											new double[] {
													1/_rcMapAEO.get(i-1).getRCmax(),
													1/_rcMapAEO.get(i).getRCmax()
											}
											),
									SI.SECOND
									)
							)
					);
		}
		
		//--------------------------------------------------------------------------------------
		// TIME AT Climb Speed
		_climbTimeListAEO = new ArrayList<>();
		_climbTimeListAEO.add(Amount.valueOf(0.0, SI.SECOND));
		List<Double> rcAtClimbSpeed = new ArrayList<>();
		List<Amount<Velocity>> climbSpeedTAS = new ArrayList<>();
		
		for(int i=0; i<_rcMapAEO.size(); i++) {
			if(_climbSpeed != null) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				
				rcAtClimbSpeed.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_rcMapAEO.get(i).getSpeed(),
								_rcMapAEO.get(i).getRC(),
								_climbSpeed.doubleValue(SI.METERS_PER_SECOND)
								)
						);
				climbSpeedTAS.add(_climbSpeed.divide(Math.sqrt(sigma)));
			}
		}
		
		for(int i=1; i<_rcMapAEO.size(); i++) {
			_climbTimeListAEO.add(
					_climbTimeListAEO.get(_climbTimeListAEO.size()-1)
					.plus(
							Amount.valueOf(
									MyMathUtils.integrate1DSimpsonSpline(
											new double[] { 
													_rcMapAEO.get(i-1).getAltitude(),
													_rcMapAEO.get(i).getAltitude()
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
		}

		_minimumClimbTimeAEO = _climbTimeListRCmax.get(_climbTimeListRCmax.size()-1);
		
		if(_climbSpeed != null)
			_climbTimeAtSpecificClimbSpeedAEO = _climbTimeListAEO.get(_climbTimeListAEO.size()-1);
		
		//----------------------------------------------------------------------------------
		// SFC, TIME AND RANGE IN AEO CONDITION (for the mission profile)
		List<Double> fuelFlowListClimb = new ArrayList<>();
		List<Amount<Duration>> climbTimeListAEO = new ArrayList<>();
		List<Amount<Length>> rangeArrayClimb = new ArrayList<>();
		
		climbTimeListAEO.add(Amount.valueOf(0.0, SI.SECOND));
		rangeArrayClimb.add(Amount.valueOf(0.0, SI.METER));
		
		for(int i=1; i<_rcMapAEO.size(); i++) {
			
			if(_climbSpeed == null) {
				climbTimeListAEO.add(
						climbTimeListAEO.get(climbTimeListAEO.size()-1)
						.plus(
								Amount.valueOf(
										MyMathUtils.integrate1DSimpsonSpline(
												new double[] { 
														_rcMapAEO.get(i-1).getAltitude(),
														_rcMapAEO.get(i).getAltitude()
												},
												new double[] {
														1/_rcMapAEO.get(i-1).getRCmax(),
														1/_rcMapAEO.get(i).getRCmax()
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
										((_rcMapAEO.get(i-1).getRCMaxSpeed()+_rcMapAEO.get(i).getRCMaxSpeed())/2)
										*(climbTimeListAEO.get(i).minus(climbTimeListAEO.get(i-1)).doubleValue(SI.SECOND))
										*Math.cos(((_rcMapAEO.get(i-1).getTheta()+_rcMapAEO.get(i).getTheta())/2)),
										SI.METER
										)
								)
						);
			}
			else {
				climbTimeListAEO.add(
						climbTimeListAEO.get(climbTimeListAEO.size()-1)
						.plus(
								Amount.valueOf(
										MyMathUtils.integrate1DSimpsonSpline(
												new double[] { 
														_rcMapAEO.get(i-1).getAltitude(),
														_rcMapAEO.get(i).getAltitude()
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
										*(climbTimeListAEO.get(i).minus(climbTimeListAEO.get(i-1)).doubleValue(SI.SECOND))
										*Math.cos(((_rcMapAEO.get(i-1).getTheta()+_rcMapAEO.get(i).getTheta())/2)),
										SI.METER
										)
								)
						);
			}
			
		}
		
		_climbTotalRange = rangeArrayClimb.get(rangeArrayClimb.size()-1);
		
		for(int i=0; i<_rcMapAEO.size(); i++) {

			if(_climbSpeed == null) {
				fuelFlowListClimb.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(i).getSpeed(),
								_thrustListAEO.get(i).getThrust(),
								_rcMapAEO.get(i).getRCMaxSpeed()
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								SpeedCalc.calculateMach(
										_rcMapAEO.get(i).getAltitude(),
										_rcMapAEO.get(i).getRCMaxSpeed()
										),
								_rcMapAEO.get(i).getAltitude(),
								(MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAEO.get(i).getSpeed(),
										_thrustListAEO.get(i).getThrust(),
										_rcMapAEO.get(i).getRCMaxSpeed()
										)/2)/_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CLIMB,
								_theAircraft.getPowerPlant()
								)
						);
				_fuelUsedList.add(
						Amount.valueOf(
								fuelFlowListClimb.get(i)*_climbTimeListRCmax.get(i).doubleValue(NonSI.MINUTE),
								SI.KILOGRAM
								)
						);
			}
			else {
				fuelFlowListClimb.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(i).getSpeed(),
								_thrustListAEO.get(i).getThrust(),
								climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
								)
						*(0.224809)*(0.454/60)
						*EngineDatabaseManager.getSFC(
								SpeedCalc.calculateMach(
										_rcMapAEO.get(i).getAltitude(),
										climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
										),
								_rcMapAEO.get(i).getAltitude(),
								(MyMathUtils.getInterpolatedValue1DLinear(
										_thrustListAEO.get(i).getSpeed(),
										_thrustListAEO.get(i).getThrust(),
										climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
										)/2)/_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CLIMB,
								_theAircraft.getPowerPlant()
								)
						);
				_fuelUsedList.add(
						Amount.valueOf(
								fuelFlowListClimb.get(i)*_climbTimeListAEO.get(i).doubleValue(NonSI.MINUTE),
								SI.KILOGRAM
								)
						);
			}
		}

		_climbTotalFuelUsed = Amount.valueOf(
				MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								climbTimeListAEO.stream()
									.map(t -> t.to(NonSI.MINUTE))
										.collect(Collectors.toList()
												)
										),
						MyArrayUtils.convertToDoublePrimitive(fuelFlowListClimb)
						),
				SI.KILOGRAM					
				);
		
	}
	
	public void plotClimbPerformance(List<PerformancePlotEnum> plotList, String climbFolderPath) {
		
		if(plotList.contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CLIMB)) {

			//.......................................................
			// AEO
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> dragAndThrustAEO = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getSpeed()));
				dragAndThrustAEO.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getDrag()));
				legend.add("Drag at " + _dragListAEO.get(i).getAltitude() + " m");
			}
			for (int i=0; i<_thrustListAEO.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAEO.get(i).getSpeed()));
				dragAndThrustAEO.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAEO.get(i).getThrust()));
				legend.add("Thrust at " + _thrustListAEO.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, dragAndThrustAEO,
						"Drag and Thrust curves (AEO)",
						"Speed", "Forces",
						null, null, null, null,
						"m/s", "N",
						true, legend,
						climbFolderPath, "Drag_and_Thrust_curves_CLIMB_AEO"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> dragAndThrustOEI = new ArrayList<Double[]>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				dragAndThrustOEI.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getDrag()));
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
			// AEO
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> powerNeededAndAvailableAEO = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getSpeed()));
				powerNeededAndAvailableAEO.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getPower()));
				legend.add("Power needed at " + _dragListAEO.get(i).getAltitude() + " m");
			}
			for (int i=0; i<_thrustListAEO.size(); i++) {
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAEO.get(i).getSpeed()));
				powerNeededAndAvailableAEO.add(MyArrayUtils.convertFromDoublePrimitive(_thrustListAEO.get(i).getPower()));
				legend.add("Power available at " + _thrustListAEO.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, powerNeededAndAvailableAEO,
						"Power Needed and Power Available curves (AEO)",
						"Speed", "Power",
						null, null, null, null,
						"m/s", "W",
						true, legend,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CLIMB_AEO"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> powerNeededAndAvailableOEI = new ArrayList<Double[]>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				powerNeededAndAvailableOEI.add(MyArrayUtils.convertFromDoublePrimitive(_dragListAEO.get(i).getPower()));
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
			// AEO
			List<Double[]> speedListAltitudeParameterizationAEO = new ArrayList<>();
			List<Double[]> efficiencyListAltitudeParameterizationAEO = new ArrayList<>();
			List<String> legendAltitudeAEO = new ArrayList<>();
			for(int i=0; i<_dragListAEO.size(); i++) {
				speedListAltitudeParameterizationAEO.add(
						MyArrayUtils.convertFromDoublePrimitive(
								_dragListAEO.get(i).getSpeed()
								)
						);
				efficiencyListAltitudeParameterizationAEO.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitudeAEO.get(
												"Altitude = " + _dragListAEO.get(i).getAltitude()
												)
										)
								)
						);
				legendAltitudeAEO.add("Altitude = " + _dragListAEO.get(i).getAltitude());
			}
			
			try {
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAEO, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Speed", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeAEO,
						climbFolderPath, "Efficiency_curves_altitude_AEO"
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
			// AEO
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> speedCAS = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> rateOfClimbAEO = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_rcMapAEO.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAEO.get(i).getSpeed()));
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
				rateOfClimbAEO.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAEO.get(i).getRC()));
				legend.add("Rate of climb at " + _rcMapAEO.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, rateOfClimbAEO,
						"Rate of Climb curves (AEO)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AEO_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, rateOfClimbAEO,
						"Rate of Climb curves (AEO)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AEO_CAS"
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbAEO,
						"Rate of Climb curves (AEO)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						" ", "m/s",
						true, legend,
						climbFolderPath, "Rate_of_Climb_curves_AEO_MACH"
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
						" ", "m/s",
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
			// AEO
			List<Double[]> speed = new ArrayList<Double[]>();
			List<Double[]> speedCAS = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> climbAngleAEO = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<String>();

			for (int i=0; i<_rcMapAEO.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speed.add(MyArrayUtils.convertFromDoublePrimitive(_rcMapAEO.get(i).getSpeed()));
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
				climbAngleAEO.add(
						MyArrayUtils.convertFromDoublePrimitive(
								MyArrayUtils.scaleArray(
										_rcMapAEO.get(i).getGamma(),
										57.3)
								)
						);
				legend.add("Climb angle at " + _rcMapAEO.get(i).getAltitude() + " m");
			}

			try {
				MyChartToFileUtils.plot(
						speed, climbAngleAEO,
						"Climb angle curves (AEO)",
						"Speed (TAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AEO_TAS"
						);
				MyChartToFileUtils.plot(
						speedCAS, climbAngleAEO,
						"Climb angle curves (AEO)",
						"Speed (CAS)", "Climb angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AEO_CAS"
						);
				MyChartToFileUtils.plot(
						mach, climbAngleAEO,
						"Climb angle curves (AEO)",
						"Mach", "Climb angle",
						null, null, null, null,
						" ", "deg",
						true, legend,
						climbFolderPath, "Climb_angle_curves_AEO_MACH"
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
						" ", "deg",
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
			// AEO
			List<Double> maxRateOfClimbListAEO = new ArrayList<Double>();
			List<Double> altitudeListAEO = new ArrayList<Double>();

			maxRateOfClimbListAEO.add(0.0);
			altitudeListAEO.add(_absoluteCeilingAEO.doubleValue(SI.METER));
			for(int i=0; i<_rcMapAEO.size(); i++) {
				maxRateOfClimbListAEO.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getRCmax());
				altitudeListAEO.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude());
			}

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListAEO),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"m/s", "m",
					climbFolderPath, "Max_Rate_of_Climb_envelope_(AEO)"
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
			// AEO
			List<Double> maxClimbAngleListAEO = new ArrayList<Double>();
			List<Double> altitudeListAEO = new ArrayList<Double>();
			
			maxClimbAngleListAEO.add(0.0);
			altitudeListAEO.add(_absoluteCeilingAEO.doubleValue(SI.METER));
			for(int i=0; i<_rcMapAEO.size(); i++) {
				maxClimbAngleListAEO.add(MyArrayUtils.getMax(_rcMapAEO.get(_rcMapAEO.size()-1-i).getGamma()));
				altitudeListAEO.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude());
			}
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListAEO),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_(AEO)"
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
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListOEI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_(OEI)"
					);
			
		}
		
		if(plotList.contains(PerformancePlotEnum.CLIMB_TIME)) {

			//.......................................................
			// AEO (V CLimb)
			List<Double[]> timeList = new ArrayList<>();
			List<Double[]> altitudeListAEO = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<>();
			
			Double[] altitudeAEO = new Double[_rcMapAEO.size()];
			for(int i=0; i<_rcMapAEO.size(); i++) {
				altitudeAEO[i] = _rcMapAEO.get(i).getAltitude();
			}
			
			altitudeListAEO.add(altitudeAEO);
			altitudeListAEO.add(altitudeAEO);
			timeList.add(MyArrayUtils.convertListOfAmountToDoubleArray(
					_climbTimeListAEO.stream()
					.map(t -> t.to(NonSI.MINUTE))
					.collect(Collectors.toList())
					));
			timeList.add(MyArrayUtils.convertListOfAmountToDoubleArray(
					_climbTimeListRCmax.stream()
					.map(t -> t.to(NonSI.MINUTE))
					.collect(Collectors.toList())
					));
			legend.add("Climb Speed");
			legend.add("RC max speed");
			
			try {
				MyChartToFileUtils.plot(
						timeList, altitudeListAEO,
						"Climb times",
						"Time", "Altitude",
						null, null, null, null,
						"min", "m",
						true, legend,
						climbFolderPath, "Climb_Time_(AEO)"
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			
		}
		
		if(plotList.contains(PerformancePlotEnum.CLIMB_FUEL_USED)) {

			List<Double> altitudeList = new ArrayList<Double>();
			List<Double> fuelUsedList = new ArrayList<Double>();
			
			for(int i=0; i<_rcMapAEO.size(); i++) {
				altitudeList.add(_rcMapAEO.get(i).getAltitude());	
				fuelUsedList.add(_fuelUsedList.get(i).doubleValue(SI.KILOGRAM));
			}
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(altitudeList),
					MyArrayUtils.convertToDoublePrimitive(fuelUsedList),
					0.0, null, 0.0, null,
					"Altitude", "Fuel  used",
					"m", "kg",
					climbFolderPath, "Fuel_used_(AEO)"
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


	public List<RCMap> getRCMapAEO() {
		return _rcMapAEO;
	}


	public void setRCMapAEO(List<RCMap> _rcMapAEo) {
		this._rcMapAEO = _rcMapAEO;
	}


	public List<RCMap> getRCMapOEI() {
		return _rcMapOEI;
	}


	public void setRCMapOEI(List<RCMap> _rcMapOEI) {
		this._rcMapOEI = _rcMapOEI;
	}


	public CeilingMap getCeilingMapAEO() {
		return _ceilingMapAEO;
	}


	public void setCeilingMapAEO(CeilingMap _ceilingMapAEO) {
		this._ceilingMapAEO = _ceilingMapAEO;
	}


	public CeilingMap getCeilingMapOEI() {
		return _ceilingMapOEI;
	}


	public void setCeilingMapOEI(CeilingMap _ceilingMapOEI) {
		this._ceilingMapOEI = _ceilingMapOEI;
	}


	public List<DragMap> getDragListAEO() {
		return _dragListAEO;
	}


	public void setDragListAEO(List<DragMap> _dragListAEO) {
		this._dragListAEO = _dragListAEO;
	}


	public List<ThrustMap> getThrustListAEO() {
		return _thrustListAEO;
	}


	public void setThrustListAEO(List<ThrustMap> _thrustListAEO) {
		this._thrustListAEO = _thrustListAEO;
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


	public Amount<Length> getAbsoluteCeilingAEO() {
		return _absoluteCeilingAEO;
	}


	public void setAbsoluteCeilingAEO(Amount<Length> _absoluteCeilingAEO) {
		this._absoluteCeilingAEO = _absoluteCeilingAEO;
	}


	public Amount<Length> getServiceCeilingAEO() {
		return _serviceCeilingAEO;
	}


	public void setServiceCeilingAEO(Amount<Length> _serviceCeilingAEO) {
		this._serviceCeilingAEO = _serviceCeilingAEO;
	}


	public Amount<Duration> getMinimumClimbTimeAEO() {
		return _minimumClimbTimeAEO;
	}


	public void setMinimumClimbTimeAEO(Amount<Duration> _minimumClimbTimeAEO) {
		this._minimumClimbTimeAEO = _minimumClimbTimeAEO;
	}


	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedAEO() {
		return _climbTimeAtSpecificClimbSpeedAEO;
	}


	public void setClimbTimeAtSpecificClimbSpeedAEO(Amount<Duration> _climbTimeAtSpecificClimbSpeedAEO) {
		this._climbTimeAtSpecificClimbSpeedAEO = _climbTimeAtSpecificClimbSpeedAEO;
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

	public Map<String, List<Double>> getEfficiencyMapAltitudeAEO() {
		return _efficiencyMapAltitudeAEO;
	}

	public void setEfficiencyMapAltitudeAEO(Map<String, List<Double>> _efficiencyMapAltitude) {
		this._efficiencyMapAltitudeAEO = _efficiencyMapAltitude;
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

	public List<Amount<Duration>> getClimbTimeListAEO() {
		return _climbTimeListAEO;
	}

	public void setClimbTimeListAEO(List<Amount<Duration>> _climbTimeListAEO) {
		this._climbTimeListAEO = _climbTimeListAEO;
	}
	
	public List<Amount<Duration>> getClimbTimeListRCmax() {
		return _climbTimeListRCmax;
	}

	public void setClimbTimeListRCmax(List<Amount<Duration>> _climbTimeListRCmax) {
		this._climbTimeListRCmax = _climbTimeListRCmax;
	}

	public List<Amount<Mass>> getFuelUsedList() {
		return _fuelUsedList;
	}

	public void setFuelUsedList(List<Amount<Mass>> _fuelUsedList) {
		this._fuelUsedList = _fuelUsedList;
	}
	
}
