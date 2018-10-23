package calculators.performance;

import java.util.ArrayList;
import java.util.Arrays;
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

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import analyses.OperatingConditions;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
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
import standaloneutils.MyUnits;
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
	private List<Double> _fuelFlowList;
	private List<Double> _sfcList;
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
			boolean performOEI,
			boolean performCeilings
			) {
		
		if(initialClimbAltitude.doubleValue(SI.METER) == finalClimbAltitude.doubleValue(SI.METER))
			finalClimbAltitude = Amount.valueOf(
					initialClimbAltitude.doubleValue(SI.METER) + 0.00001,
					SI.METER
					);
		
		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_theAircraft.getWing());
		
		double[] altitudeArray = MyArrayUtils.linspace(
				initialClimbAltitude.doubleValue(SI.METER),
				finalClimbAltitude.doubleValue(SI.METER),
				10
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
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							Arrays.stream(_polarCLClimb).mapToDouble(cL -> cL).max().getAsDouble()
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
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_cLmaxClean,
							MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClimb),
							_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
							meanAirfoil.getThicknessToChordRatio(),
							meanAirfoil.getType()
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
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								_dragListAEO.get(i).getAltitude(),
								LiftCalc.calculateLiftCoeff(
										startClimbMassAEO.times(AtmosphereCalc.g0).getEstimatedValue(),
										_dragListAEO.get(i).getSpeed()[j],
										_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
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
								SpeedCalc.calculateTAS(
										_climbSpeed.to(SI.METERS_PER_SECOND),
										altitudeArray[0]
										).doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_thrustAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(_thrustListAEO.size()-1).getSpeed(),
								_thrustListAEO.get(_thrustListAEO.size()-1).getThrust(),
								SpeedCalc.calculateTAS(
										_climbSpeed.to(SI.METERS_PER_SECOND),
										altitudeArray[altitudeArray.length-1]
										).doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbStart = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAEO.get(0).getSpeed(),
								_dragListAEO.get(0).getDrag(),
								SpeedCalc.calculateTAS(
										_climbSpeed.to(SI.METERS_PER_SECOND),
										altitudeArray[0]
										).doubleValue(SI.METERS_PER_SECOND)
								),
						SI.NEWTON
						).to(NonSI.POUND_FORCE);
		_dragAtClimbEnding = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAEO.get(_dragListAEO.size()-1).getSpeed(),
								_dragListAEO.get(_dragListAEO.size()-1).getDrag(),
								SpeedCalc.calculateTAS(
										_climbSpeed.to(SI.METERS_PER_SECOND),
										altitudeArray[altitudeArray.length-1]
										).doubleValue(SI.METERS_PER_SECOND)
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
		if(performCeilings == true) {
			
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
			
		}
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
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								Arrays.stream(_polarCLClimb).mapToDouble(cL -> cL).max().getAsDouble()
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
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								_cLmaxClean,
								MyArrayUtils.convertToDoublePrimitive(_polarCLClimb),
								MyArrayUtils.sumNumberToArrayEBE(MyArrayUtils.convertToDoublePrimitive(_polarCDClimb), _dragDueToEnigneFailure),
								_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
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
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_dragListOEI.get(i).getAltitude(),
									LiftCalc.calculateLiftCoeff(
											startClimbMassOEI.times(AtmosphereCalc.g0).getEstimatedValue(),
											_dragListOEI.get(i).getSpeed()[j],
											_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
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
			if(performCeilings == true) {

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
						).getDensityRatio();
				
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
		_fuelFlowList = new ArrayList<>();
		_sfcList = new ArrayList<>();
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
				_sfcList.add(
						EngineDatabaseManager.getSFC(
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
				_fuelFlowList.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(i).getSpeed(),
								_thrustListAEO.get(i).getThrust(),
								_rcMapAEO.get(i).getRCMaxSpeed()
								)
						*(0.224809)*(0.454/60)
						*_sfcList.get(i)
						);
				_fuelUsedList.add(
						Amount.valueOf(
								_fuelFlowList.get(i)*_climbTimeListRCmax.get(i).doubleValue(NonSI.MINUTE),
								SI.KILOGRAM
								)
						);
			}
			else {
				_sfcList.add(
						EngineDatabaseManager.getSFC(
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
				_fuelFlowList.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_thrustListAEO.get(i).getSpeed(),
								_thrustListAEO.get(i).getThrust(),
								climbSpeedTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
								)
						*(0.224809)*(0.454/60)
						*_sfcList.get(i)
						);
				_fuelUsedList.add(
						Amount.valueOf(
								_fuelFlowList.get(i)*_climbTimeListAEO.get(i).doubleValue(NonSI.MINUTE),
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
						MyArrayUtils.convertToDoublePrimitive(_fuelFlowList)
						),
				SI.KILOGRAM					
				);
		
	}
	
	public void plotClimbPerformance(List<PerformancePlotEnum> plotList, String climbFolderPath) {
		
		if(plotList.contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CLIMB)) {

			//.......................................................
			// AEO
			List<Double[]> speed_SI = new ArrayList<Double[]>();
			List<Double[]> dragAndThrustAEO_SI = new ArrayList<Double[]>();
			List<Double[]> speed_Imperial = new ArrayList<Double[]>();
			List<Double[]> dragAndThrustAEO_Imperial = new ArrayList<Double[]>();
			List<String> legend_SI = new ArrayList<String>();
			List<String> legend_Imperial = new ArrayList<String>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				speed_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(SI.METERS_PER_SECOND))
								.toArray()
								)
						);
				speed_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				dragAndThrustAEO_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getDrag())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(SI.NEWTON))
								.toArray()
								)
						);
				dragAndThrustAEO_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getDrag())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
								.toArray()
								)
						);
				legend_SI.add("Drag at " + _dragListAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Drag at " + Amount.valueOf(_dragListAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}
			for (int i=0; i<_thrustListAEO.size(); i++) {
				speed_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(SI.METERS_PER_SECOND))
								.toArray()
								)
						);
				speed_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				dragAndThrustAEO_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getThrust())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(SI.NEWTON))
								.toArray()
								)
						);
				dragAndThrustAEO_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getThrust())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
								.toArray()
								)
						);
				legend_SI.add("Thrust at " + _thrustListAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Thrust at " + Amount.valueOf(_thrustListAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}

			try {
				MyChartToFileUtils.plot(
						speed_SI, dragAndThrustAEO_SI,
						"Drag and Thrust curves (AEO)",
						"Speed", "Forces",
						null, null, null, null,
						"m/s", "N",
						true, legend_SI,
						climbFolderPath, "Drag_and_Thrust_curves_CLIMB_AEO_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speed_Imperial, dragAndThrustAEO_Imperial,
						"Drag and Thrust curves (AEO)",
						"Speed", "Forces",
						null, null, null, null,
						"kn", "lb",
						true, legend_Imperial,
						climbFolderPath, "Drag_and_Thrust_curves_CLIMB_AEO_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> dragAndThrustOEI_SI = new ArrayList<Double[]>();
			List<Double[]> dragAndThrustOEI_Imperial = new ArrayList<Double[]>();

			for (int i=0; i<_dragListOEI.size(); i++) {
				dragAndThrustOEI_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getDrag())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(SI.NEWTON))
								.toArray()
								)
						);
				dragAndThrustOEI_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getDrag())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
								.toArray()
								)
						);
			}
			for (int i=0; i<_thrustListOEI.size(); i++) {
				dragAndThrustOEI_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListOEI.get(i).getThrust())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(SI.NEWTON))
								.toArray()
								)
						);
				dragAndThrustOEI_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListOEI.get(i).getThrust())
								.map(x -> Amount.valueOf(x, SI.NEWTON).doubleValue(NonSI.POUND_FORCE))
								.toArray()
								)
						);
			}

			try {
				MyChartToFileUtils.plot(
						speed_SI, dragAndThrustOEI_SI,
						"Drag and Thrust curves (OEI)",
						"Speed", "Forces",
						null, null, null, null,
						"m/s", "N",
						true, legend_SI,
						climbFolderPath, "Drag_and_Thrust_curves_CONTINUOUS_OEI_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speed_Imperial, dragAndThrustOEI_Imperial,
						"Drag and Thrust curves (OEI)",
						"Speed", "Forces",
						null, null, null, null,
						"kn", "lb",
						true, legend_Imperial,
						climbFolderPath, "Drag_and_Thrust_curves_CONTINUOUS_OEI_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
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
			List<Double[]> speed_SI = new ArrayList<Double[]>();
			List<Double[]> speed_Imperial = new ArrayList<Double[]>();
			List<Double[]> powerNeededAndAvailableAEO_SI = new ArrayList<Double[]>();
			List<Double[]> powerNeededAndAvailableAEO_Imperial = new ArrayList<Double[]>();
			List<String> legend_SI = new ArrayList<String>();
			List<String> legend_Imperial = new ArrayList<String>();

			for (int i=0; i<_dragListAEO.size(); i++) {
				speed_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(SI.METERS_PER_SECOND))
								.toArray()
								)
						);
				speed_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				powerNeededAndAvailableAEO_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(SI.WATT))
								.toArray()
								)
						);
				powerNeededAndAvailableAEO_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
								.toArray()
								)
						);
				legend_SI.add("Power needed at " + _dragListAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Power needed at " + Amount.valueOf(_dragListAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}
			for (int i=0; i<_thrustListAEO.size(); i++) {
				speed_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(SI.METERS_PER_SECOND))
								.toArray()
								)
						);
				speed_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				powerNeededAndAvailableAEO_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(SI.WATT))
								.toArray()
								)
						);
				powerNeededAndAvailableAEO_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListAEO.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
								.toArray()
								)
						);
				legend_SI.add("Power available at " + _thrustListAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Power available at " + Amount.valueOf(_thrustListAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}

			try {
				MyChartToFileUtils.plot(
						speed_SI, powerNeededAndAvailableAEO_SI,
						"Power Needed and Power Available curves (AEO)",
						"Speed", "Power",
						null, null, null, null,
						"m/s", "W",
						true, legend_SI,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CLIMB_AEO_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speed_Imperial, powerNeededAndAvailableAEO_Imperial,
						"Power Needed and Power Available curves (AEO)",
						"Speed", "Power",
						null, null, null, null,
						"kn", "hp",
						true, legend_Imperial,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CLIMB_AEO_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> powerNeededAndAvailableOEI_SI = new ArrayList<Double[]>();
			List<Double[]> powerNeededAndAvailableOEI_Imperial = new ArrayList<Double[]>();

			for (int i=0; i<_dragListOEI.size(); i++) {
				powerNeededAndAvailableOEI_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(SI.WATT))
								.toArray()
								)
						);
				powerNeededAndAvailableOEI_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
								.toArray()
								)
						);
			}
			for (int i=0; i<_thrustListOEI.size(); i++) {
				powerNeededAndAvailableOEI_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListOEI.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(SI.WATT))
								.toArray()
								)
						);
				powerNeededAndAvailableOEI_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_thrustListOEI.get(i).getPower())
								.map(x -> Amount.valueOf(x, SI.WATT).doubleValue(NonSI.HORSEPOWER))
								.toArray()
								)
						);
			}

			try {
				MyChartToFileUtils.plot(
						speed_SI, powerNeededAndAvailableOEI_SI,
						"Power Needed and Power Available curves (OEI)",
						"Speed", "Power",
						null, null, null, null,
						"m/s", "W",
						true, legend_SI,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CONTINUOUS_OEI_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speed_Imperial, powerNeededAndAvailableOEI_Imperial,
						"Power Needed and Power Available curves (OEI)",
						"Speed", "Power",
						null, null, null, null,
						"kn", "hp",
						true, legend_Imperial,
						climbFolderPath, "Power_Needed_and_Power_Available_curves_CONTINUOUS_OEI_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
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
			List<Double[]> speedListAltitudeParameterizationAEO_TAS_SI = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationAEO_TAS_Imperial = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationAEO_CAS_SI = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationAEO_CAS_Imperial = new ArrayList<>();
			List<Double[]> machListAltitudeParameterizationAEO = new ArrayList<>();
			List<Double[]> efficiencyListAltitudeParameterizationAEO = new ArrayList<>();
			List<String> legendAltitudeAEO_SI = new ArrayList<>();
			List<String> legendAltitudeAEO_Imperial = new ArrayList<>();
			for(int i=0; i<_dragListAEO.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_dragListAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speedListAltitudeParameterizationAEO_TAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_dragListAEO.get(i).getSpeed()
								)
						);
				speedListAltitudeParameterizationAEO_TAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				speedListAltitudeParameterizationAEO_CAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> x*Math.sqrt(sigma))
								.toArray()
								)
						);
				speedListAltitudeParameterizationAEO_CAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> x*Math.sqrt(sigma))
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				machListAltitudeParameterizationAEO.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListAEO.get(i).getSpeed())
								.map(x -> x/speedOfSound)
								.toArray()
								)
						);
				efficiencyListAltitudeParameterizationAEO.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitudeAEO.get(
												"Altitude = " + _dragListAEO.get(i).getAltitude()
												)
										)
								)
						);
				legendAltitudeAEO_SI.add("Altitude = " + _dragListAEO.get(i).getAltitude() + " m");
				legendAltitudeAEO_Imperial.add("Altitude = " + Amount.valueOf(_dragListAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}
			
			try {
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAEO_TAS_SI, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Speed (TAS)", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeAEO_SI,
						climbFolderPath, "Efficiency_curves_altitude_AEO_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAEO_TAS_Imperial, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Speed (TAS)", "Efficiency",
						null, null, null, null,
						"kn", "",
						true, legendAltitudeAEO_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_AEO_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAEO_CAS_SI, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Speed (CAS)", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeAEO_SI,
						climbFolderPath, "Efficiency_curves_altitude_AEO_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationAEO_CAS_Imperial, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Speed (CAS)", "Efficiency",
						null, null, null, null,
						"kn", "",
						true, legendAltitudeAEO_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_AEO_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						machListAltitudeParameterizationAEO, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Mach", "Efficiency",
						null, null, null, null,
						"", "",
						true, legendAltitudeAEO_SI,
						climbFolderPath, "Efficiency_curves_altitude_AEO_Mach_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						machListAltitudeParameterizationAEO, efficiencyListAltitudeParameterizationAEO,
						"Efficiency curves at different altitudes (AEO)",
						"Mach", "Efficiency",
						null, null, null, null,
						"", "",
						true, legendAltitudeAEO_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_AEO_Mach_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> speedListAltitudeParameterizationOEI_TAS_SI = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationOEI_TAS_Imperial = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationOEI_CAS_SI = new ArrayList<>();
			List<Double[]> speedListAltitudeParameterizationOEI_CAS_Imperial = new ArrayList<>();
			List<Double[]> machListAltitudeParameterizationOEI = new ArrayList<>();
			List<Double[]> efficiencyListAltitudeParameterizationOEI = new ArrayList<>();
			List<String> legendAltitudeOEI_SI = new ArrayList<>();
			List<String> legendAltitudeOEI_Imperial = new ArrayList<>();
			for(int i=0; i<_dragListOEI.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_dragListAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speedListAltitudeParameterizationOEI_TAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_dragListOEI.get(i).getSpeed()
								)
						);
				speedListAltitudeParameterizationOEI_TAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				speedListAltitudeParameterizationOEI_CAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getSpeed())
								.map(x -> x*Math.sqrt(sigma))
								.toArray()
								)
						);
				speedListAltitudeParameterizationOEI_CAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getSpeed())
								.map(x -> x*Math.sqrt(sigma))
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				machListAltitudeParameterizationOEI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_dragListOEI.get(i).getSpeed())
								.map(x -> x/speedOfSound)
								.toArray()
								)
						);
				efficiencyListAltitudeParameterizationOEI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.convertToDoublePrimitive(
										_efficiencyMapAltitudeOEI.get(
												"Altitude = " + _dragListOEI.get(i).getAltitude()
												)
										)
								)
						);
				legendAltitudeOEI_SI.add("Altitude = " + _dragListOEI.get(i).getAltitude() + " m");
				legendAltitudeOEI_Imperial.add("Altitude = " + Amount.valueOf(_dragListOEI.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}
			
			try {
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationOEI_TAS_SI, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Speed (TAS)", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeOEI_SI,
						climbFolderPath, "Efficiency_curves_altitude_OEI_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationOEI_TAS_Imperial, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Speed (TAS)", "Efficiency",
						null, null, null, null,
						"kn", "",
						true, legendAltitudeOEI_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_OEI_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationOEI_CAS_SI, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Speed (CAS)", "Efficiency",
						null, null, null, null,
						"m/s", "",
						true, legendAltitudeOEI_SI,
						climbFolderPath, "Efficiency_curves_altitude_OEI_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						speedListAltitudeParameterizationOEI_CAS_Imperial, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Speed (CAS)", "Efficiency",
						null, null, null, null,
						"kn", "",
						true, legendAltitudeOEI_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_OEI_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						machListAltitudeParameterizationOEI, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Mach", "Efficiency",
						null, null, null, null,
						"", "",
						true, legendAltitudeOEI_SI,
						climbFolderPath, "Efficiency_curves_altitude_OEI_Mach_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				
				MyChartToFileUtils.plot(
						machListAltitudeParameterizationOEI, efficiencyListAltitudeParameterizationOEI,
						"Efficiency curves at different altitudes (OEI)",
						"Mach", "Efficiency",
						null, null, null, null,
						"", "",
						true, legendAltitudeOEI_Imperial,
						climbFolderPath, "Efficiency_curves_altitude_OEI_Mach_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
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
			List<Double[]> speedTAS_SI = new ArrayList<Double[]>();
			List<Double[]> speedTAS_Imperial = new ArrayList<Double[]>();
			List<Double[]> speedCAS_SI = new ArrayList<Double[]>();
			List<Double[]> speedCAS_Imperial = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> rateOfClimbAEO_SI = new ArrayList<Double[]>();
			List<Double[]> rateOfClimbAEO_Imperial = new ArrayList<Double[]>();
			List<String> legend_SI = new ArrayList<String>();
			List<String> legend_Imperial = new ArrayList<String>();

			for (int i=0; i<_rcMapAEO.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speedTAS_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_rcMapAEO.get(i).getSpeed()));
				speedTAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_rcMapAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				speedCAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)),
										Math.sqrt(sigma)
										)
								)
						);
				speedCAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										Arrays.stream(MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)))
										.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
										.toArray(),
										Math.sqrt(sigma)
										)
								)
						);
				mach.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)),
										1/speedOfSound
										)
								)
						);
				rateOfClimbAEO_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_rcMapAEO.get(i).getRC()));
				rateOfClimbAEO_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_rcMapAEO.get(i).getRC())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(MyUnits.FOOT_PER_MINUTE))
								.toArray()
								)
						);
				legend_SI.add("Rate of climb at " + _rcMapAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Rate of climb at " + Amount.valueOf(_rcMapAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}

			try {
				MyChartToFileUtils.plot(
						speedTAS_SI, rateOfClimbAEO_SI,
						"Rate of Climb curves (AEO)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_AEO_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_SI, rateOfClimbAEO_SI,
						"Rate of Climb curves (AEO)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_AEO_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbAEO_SI,
						"Rate of Climb curves (AEO)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						" ", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_AEO_MACH_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedTAS_Imperial, rateOfClimbAEO_Imperial,
						"Rate of Climb curves (AEO)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"kn", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_AEO_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_Imperial, rateOfClimbAEO_Imperial,
						"Rate of Climb curves (AEO)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"kn", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_AEO_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbAEO_Imperial,
						"Rate of Climb curves (AEO)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						" ", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_AEO_MACH_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> rateOfClimbOEI_SI = new ArrayList<Double[]>();
			List<Double[]> rateOfClimbOEI_Imperial = new ArrayList<Double[]>();

			for (int i=0; i<_rcMapOEI.size(); i++) {
				rateOfClimbOEI_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_rcMapOEI.get(i).getRC()));
				rateOfClimbOEI_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_rcMapOEI.get(i).getRC())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(MyUnits.FOOT_PER_MINUTE))
								.toArray()
								)
						);
			}

			try {
				MyChartToFileUtils.plot(
						speedTAS_SI, rateOfClimbOEI_SI,
						"Rate of Climb curves (OEI)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_OEI_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_SI, rateOfClimbOEI_SI,
						"Rate of Climb curves (OEI)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"m/s", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_OEI_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbOEI_SI,
						"Rate of Climb curves (OEI)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						" ", "m/s",
						true, legend_SI,
						climbFolderPath, "Rate_of_Climb_curves_OEI_MACH_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedTAS_Imperial, rateOfClimbOEI_Imperial,
						"Rate of Climb curves (OEI)",
						"Speed (TAS)", "Rate of Climb",
						null, null, null, null,
						"kn", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_OEI_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_Imperial, rateOfClimbOEI_Imperial,
						"Rate of Climb curves (OEI)",
						"Speed (CAS)", "Rate of Climb",
						null, null, null, null,
						"kn", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_OEI_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, rateOfClimbOEI_Imperial,
						"Rate of Climb curves (OEI)",
						"Mach", "Rate of Climb",
						null, null, null, null,
						" ", "ft/min",
						true, legend_Imperial,
						climbFolderPath, "Rate_of_Climb_curves_OEI_MACH_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		if(plotList.contains(PerformancePlotEnum.CLIMB_ANGLE_CURVES)) {

			List<Double[]> speedTAS_SI = new ArrayList<Double[]>();
			List<Double[]> speedTAS_Imperial = new ArrayList<Double[]>();
			List<Double[]> speedCAS_SI = new ArrayList<Double[]>();
			List<Double[]> speedCAS_Imperial = new ArrayList<Double[]>();
			List<Double[]> mach = new ArrayList<Double[]>();
			List<Double[]> climbAngleAEO = new ArrayList<Double[]>();
			List<String> legend_SI = new ArrayList<String>();
			List<String> legend_Imperial = new ArrayList<String>();

			for (int i=0; i<_rcMapAEO.size(); i++) {
				
				double sigma = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getDensity()*1000/1.225;
				double speedOfSound = OperatingConditions.getAtmosphere(
						_rcMapAEO.get(i).getAltitude()
						).getSpeedOfSound();
				
				speedTAS_SI.add(MyArrayUtils.convertFromDoubleToPrimitive(_rcMapAEO.get(i).getSpeed()));
				speedTAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								Arrays.stream(_rcMapAEO.get(i).getSpeed())
								.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
								.toArray()
								)
						);
				speedCAS_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)),
										Math.sqrt(sigma)
										)
								)
						);
				speedCAS_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										Arrays.stream(MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)))
										.map(x -> Amount.valueOf(x, SI.METERS_PER_SECOND).doubleValue(NonSI.KNOT))
										.toArray(),
										Math.sqrt(sigma)
										)
								)
						);
				mach.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										MyArrayUtils.convertToDoublePrimitive(speedTAS_SI.get(i)),
										1/speedOfSound
										)
								)
						);
				climbAngleAEO.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										_rcMapAEO.get(i).getGamma(),
										57.3)
								)
						);
				legend_SI.add("Climb Angle at " + _rcMapAEO.get(i).getAltitude() + " m");
				legend_Imperial.add("Climb Angle at " + Amount.valueOf(_rcMapAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT) + " ft");
			}

			try {
				MyChartToFileUtils.plot(
						speedTAS_SI, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Speed (TAS)", "Climb Angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_AEO_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_SI, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Speed (CAS)", "Climb Angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_AEO_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedTAS_Imperial, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Speed (TAS)", "Climb Angle",
						null, null, null, null,
						"kn", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_AEO_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_Imperial, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Speed (CAS)", "Climb Angle",
						null, null, null, null,
						"kn", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_AEO_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Mach", "Climb Angle",
						null, null, null, null,
						" ", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_AEO_MACH_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, climbAngleAEO,
						"Climb Angle curves (AEO)",
						"Mach", "Climb Angle",
						null, null, null, null,
						" ", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_AEO_MACH_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			//.......................................................
			// OEI
			List<Double[]> climbAngleOEI = new ArrayList<Double[]>();

			for (int i=0; i<_rcMapOEI.size(); i++) 
				climbAngleOEI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyArrayUtils.scaleArray(
										_rcMapOEI.get(i).getGamma(),
										57.3)
								)
						);

			try {
				MyChartToFileUtils.plot(
						speedTAS_SI, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Speed (TAS)", "Climb Angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_OEI_TAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_SI, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Speed (CAS)", "Climb Angle",
						null, null, null, null,
						"m/s", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_OEI_CAS_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedTAS_Imperial, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Speed (TAS)", "Climb Angle",
						null, null, null, null,
						"kn", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_OEI_TAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						speedCAS_Imperial, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Speed (CAS)", "Climb Angle",
						null, null, null, null,
						"kn", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_OEI_CAS_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Mach", "Climb Angle",
						null, null, null, null,
						" ", "deg",
						true, legend_SI,
						climbFolderPath, "Climb_angle_curves_OEI_MACH_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						mach, climbAngleOEI,
						"Climb Angle curves (OEI)",
						"Mach", "Climb Angle",
						null, null, null, null,
						" ", "deg",
						true, legend_Imperial,
						climbFolderPath, "Climb_angle_curves_OEI_MACH_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
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
			List<Double> maxRateOfClimbListAEO_SI = new ArrayList<Double>();
			List<Double> maxRateOfClimbListAEO_Imperial = new ArrayList<Double>();
			List<Double> altitudeListAEO_SI = new ArrayList<Double>();
			List<Double> altitudeListAEO_Imperial = new ArrayList<Double>();

			for(int i=0; i<_rcMapAEO.size(); i++) {
				maxRateOfClimbListAEO_SI.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getRCmax());
				maxRateOfClimbListAEO_Imperial.add(
						Amount.valueOf(_rcMapAEO.get(_rcMapAEO.size()-1-i).getRCmax(), SI.METERS_PER_SECOND)
						.doubleValue(MyUnits.FOOT_PER_MINUTE));
				altitudeListAEO_SI.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude());
				altitudeListAEO_Imperial.add(
						Amount.valueOf(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude(), SI.METER)
						.doubleValue(NonSI.FOOT));
			}

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListAEO_SI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO_SI),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"m/s", "m",
					climbFolderPath, "Max_Rate_of_Climb_envelope_AEO_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListAEO_Imperial),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO_Imperial),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"ft/min", "ft",
					climbFolderPath, "Max_Rate_of_Climb_envelope_AEO_IMPERIAL",true
					);

			//.......................................................
			// OEI
			List<Double> maxRateOfClimbListOEI_SI = new ArrayList<Double>();
			List<Double> maxRateOfClimbListOEI_Imperial = new ArrayList<Double>();
			List<Double> altitudeListOEI_SI = new ArrayList<Double>();
			List<Double> altitudeListOEI_Imperial = new ArrayList<Double>();

			for(int i=0; i<_rcMapOEI.size(); i++) {
				maxRateOfClimbListOEI_SI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getRCmax());
				maxRateOfClimbListOEI_Imperial.add(
						Amount.valueOf(_rcMapOEI.get(_rcMapOEI.size()-1-i).getRCmax(), SI.METERS_PER_SECOND)
						.doubleValue(MyUnits.FOOT_PER_MINUTE));
				altitudeListOEI_SI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude());
				altitudeListOEI_Imperial.add(
						Amount.valueOf(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude(), SI.METER)
						.doubleValue(NonSI.FOOT));
			}

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListOEI_SI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI_SI),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"m/s", "m",
					climbFolderPath, "Max_Rate_of_Climb_envelope_OEI_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxRateOfClimbListOEI_Imperial),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI_Imperial),
					0.0, null, 0.0, null,
					"Maximum Rate of Climb", "Altitude",
					"ft/min", "ft",
					climbFolderPath, "Max_Rate_of_Climb_envelope_OEI_IMPEIRAL",true
					);

		}
		
		if(plotList.contains(PerformancePlotEnum.MAX_CLIMB_ANGLE_ENVELOPE)) {

			//.......................................................
			// AEO
			List<Double> maxClimbAngleListAEO = new ArrayList<Double>();
			List<Double> altitudeListAEO_SI = new ArrayList<Double>();
			List<Double> altitudeListAEO_Imperial = new ArrayList<Double>();
			
			for(int i=0; i<_rcMapAEO.size(); i++) {
				maxClimbAngleListAEO.add(MyArrayUtils.getMax(_rcMapAEO.get(_rcMapAEO.size()-1-i).getGamma()));
				altitudeListAEO_SI.add(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude());
				altitudeListAEO_Imperial.add(
						Amount.valueOf(_rcMapAEO.get(_rcMapAEO.size()-1-i).getAltitude(), SI.METER)
						.doubleValue(NonSI.FOOT));
			}
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListAEO),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO_SI),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_AEO_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListAEO),
					MyArrayUtils.convertToDoublePrimitive(altitudeListAEO_Imperial),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "ft",
					climbFolderPath, "Max_Climb_Angle_envelope_AEO_IMPERIAL",true
					);
			
			//.......................................................
			// OEI
			List<Double> maxClimbAngleListOEI = new ArrayList<Double>();
			List<Double> altitudeListOEI_SI = new ArrayList<Double>();
			List<Double> altitudeListOEI_Imperial = new ArrayList<Double>();
			
			for(int i=0; i<_rcMapOEI.size(); i++) {
				maxClimbAngleListOEI.add(MyArrayUtils.getMax(_rcMapOEI.get(_rcMapOEI.size()-1-i).getGamma()));
				altitudeListOEI_SI.add(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude());	
				altitudeListOEI_Imperial.add(
						Amount.valueOf(_rcMapOEI.get(_rcMapOEI.size()-1-i).getAltitude(), SI.METER)
						.doubleValue(NonSI.FOOT));
			}
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListOEI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI_SI),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "m",
					climbFolderPath, "Max_Climb_Angle_envelope_OEI_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(maxClimbAngleListOEI),
					MyArrayUtils.convertToDoublePrimitive(altitudeListOEI_Imperial),
					0.0, null, 0.0, null,
					"Maximum Climb Angle", "Altitude",
					"rad", "ft",
					climbFolderPath, "Max_Climb_Angle_envelope_OEI_IMPERIAL",true
					);
			
		}
		
		if(plotList.contains(PerformancePlotEnum.CLIMB_TIME)) {

			//.......................................................
			// AEO (V CLimb and V @ RCmax)
			List<Double[]> timeList = new ArrayList<>();
			List<Double[]> altitudeListAEO_SI = new ArrayList<Double[]>();
			List<Double[]> altitudeListAEO_Imperial = new ArrayList<Double[]>();
			List<String> legend = new ArrayList<>();
			
			Double[] altitudeAEO_SI = new Double[_rcMapAEO.size()];
			Double[] altitudeAEO_Imperial = new Double[_rcMapAEO.size()];
			for(int i=0; i<_rcMapAEO.size(); i++) {
				altitudeAEO_SI[i] = _rcMapAEO.get(i).getAltitude();
				altitudeAEO_Imperial[i] = Amount.valueOf(_rcMapAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT);
			}
			
			altitudeListAEO_SI.add(altitudeAEO_SI);
			altitudeListAEO_SI.add(altitudeAEO_SI);
			altitudeListAEO_Imperial.add(altitudeAEO_Imperial);
			altitudeListAEO_Imperial.add(altitudeAEO_Imperial);
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
						timeList, altitudeListAEO_SI,
						"Climb times",
						"Time", "Altitude",
						null, null, null, null,
						"min", "m",
						true, legend,
						climbFolderPath, "Climb_Time_AEO_SI",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
				MyChartToFileUtils.plot(
						timeList, altitudeListAEO_Imperial,
						"Climb times",
						"Time", "Altitude",
						null, null, null, null,
						"min", "ft",
						true, legend,
						climbFolderPath, "Climb_Time_AEO_IMPERIAL",
						_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
						);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			
		}
		
		if(plotList.contains(PerformancePlotEnum.CLIMB_FUEL_USED)) {

			List<Double> altitudeList_SI = new ArrayList<Double>();
			List<Double> altitudeList_Imperial = new ArrayList<Double>();
			List<Double> fuelUsedList = new ArrayList<Double>();
			
			for(int i=0; i<_rcMapAEO.size(); i++) {
				altitudeList_SI.add(_rcMapAEO.get(i).getAltitude());	
				altitudeList_Imperial.add(Amount.valueOf(_rcMapAEO.get(i).getAltitude(), SI.METER).doubleValue(NonSI.FOOT));
				fuelUsedList.add(_fuelUsedList.get(i).doubleValue(SI.KILOGRAM));
			}
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
					MyArrayUtils.convertToDoublePrimitive(fuelUsedList),
					0.0, null, 0.0, null,
					"Altitude", "Fuel  used",
					"m", "kg",
					climbFolderPath, "Fuel_used_AEO_SI",true
					);
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
					MyArrayUtils.convertToDoublePrimitive(fuelUsedList),
					0.0, null, 0.0, null,
					"Altitude", "Fuel  used",
					"ft", "kg",
					climbFolderPath, "Fuel_used_AEO_IMPERIAL",true
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


	public void setRCMapAEO(List<RCMap> _rcMapAEO) {
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

	public List<Double> getFuelFlowList() {
		return _fuelFlowList;
	}

	public void setFuelFlowList(List<Double> _fuelFlowList) {
		this._fuelFlowList = _fuelFlowList;
	}

	public List<Double> getSFCList() {
		return _sfcList;
	}

	public void setSFCList(List<Double> _sfcList) {
		this._sfcList = _sfcList;
	}
	
}
