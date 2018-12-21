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

import org.inferred.freebuilder.shaded.com.google.googlejavaformat.Op;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class DescentCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private Amount<Velocity> _speedDescentCAS;
	private Amount<Velocity> _rateOfDescent;
	private Amount<Length> _initialDescentAltitude;
	private Amount<Length> _endDescentAltitude;
	private Amount<Mass> _initialDescentMass;
	private Double[] _polarCLClean;
	private Double[] _polarCDClean;
	private MyInterpolatingFunction _sfcFunctionDescent;

	private final int maxIterationNumber = 50;
	
	//............................................................................................
	// Output:
	private List<Amount<Length>> _descentAltitudes;
	private List<Amount<Length>> _descentLengths;
	private List<Amount<Duration>> _descentTimes;
	private List<Amount<Angle>> _descentAngles;
	private List<Amount<Velocity>> _speedListTAS;
	private List<Amount<Velocity>> _rateOfDescentList;
	private List<Double> _cLSteps;
	private List<Amount<Force>> _cruiseThrustFromDatabase;
	private List<Amount<Force>> _flightIdleThrustFromDatabase;
	private List<Amount<Force>> _thrustPerStep;
	private List<Amount<Force>> _dragPerStep;
	private List<Double> _fuelFlowCruiseList;
	private List<Double> _fuelFlowFlightIdleList;
	private List<Double> _interpolatedFuelFlowList;
	private List<Amount<Mass>> _fuelUsedPerStep;
	private Amount<Length> _totalDescentLength;
	private Amount<Duration> _totalDescentTime;
	private Amount<Mass> _totalDescentFuelUsed;

	//............................................................................................
	// ERROR FLAGS:
	private boolean _descentMaxIterationErrorFlag;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public DescentCalc(
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Velocity> speedDescentCAS,
			Amount<Velocity> rateOfDescent,
			Amount<Length> initialDescentAltitude,
			Amount<Length> endDescentAltitude,
			Amount<Mass> initialDescentMass,
			Double[] polarCLClean,
			Double[] polarCDClean
			) {
		
		this._theAircraft = theAircraft;
		this._theOperatingConditions = theOperatingConditions;
		this._speedDescentCAS = speedDescentCAS;
		this._rateOfDescent = rateOfDescent;
		this._initialDescentAltitude = initialDescentAltitude; 
		this._endDescentAltitude = endDescentAltitude;
		this._initialDescentMass = initialDescentMass;
		this._polarCLClean = polarCLClean;
		this._polarCDClean = polarCDClean;
		
		this._descentAltitudes = new ArrayList<>();
		this._descentLengths = new ArrayList<>();
		this._descentTimes = new ArrayList<>();
		this._descentAngles = new ArrayList<>();
		this._speedListTAS = new ArrayList<>();
		this._cLSteps = new ArrayList<>();
		this._cruiseThrustFromDatabase = new ArrayList<>();
		this._flightIdleThrustFromDatabase = new ArrayList<>();
		this._thrustPerStep = new ArrayList<>();
		this._dragPerStep = new ArrayList<>();
		this._fuelFlowCruiseList = new ArrayList<>();
		this._fuelFlowFlightIdleList = new ArrayList<>();
		this._interpolatedFuelFlowList = new ArrayList<>();
		this._fuelUsedPerStep = new ArrayList<>();
		this._rateOfDescentList = new ArrayList<>();
		this._interpolatedFuelFlowList = new ArrayList<>();
		
		this.setDescentMaxIterationErrorFlag(false);
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateDescentPerformance() {
		
		List<Double> sigmaList = new ArrayList<>();
		List<Amount<Velocity>> horizontalSpeedListTAS = new ArrayList<>();
		List<Double> machList = new ArrayList<>();
		List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
		List<Double> cDSteps = new ArrayList<>();
		List<Double> efficiencyPerStep = new ArrayList<>();
		List<Amount<Force>> interpolatedThrustList = new ArrayList<>();
		List<Double> weightCruise = new ArrayList<>();
		List<Double> weightFlightIdle = new ArrayList<>();
		
		_descentAltitudes =
				MyArrayUtils.convertDoubleArrayToListOfAmount(
						MyArrayUtils.linspace(
								_initialDescentAltitude.doubleValue(SI.METER),
								_endDescentAltitude.doubleValue(SI.METER),
								5
								),
						SI.METER
						);
		
		sigmaList.add(OperatingConditions.getAtmosphere(
				_initialDescentAltitude.doubleValue(SI.METER))
				.getDensity()*1000
				/1.225
				);
		_speedListTAS.add(
				_speedDescentCAS.to(SI.METERS_PER_SECOND)
				.divide(Math.sqrt(sigmaList.get(0))));
		machList.add(
				SpeedCalc.calculateMach(
						_initialDescentAltitude,
						_theOperatingConditions.getDeltaTemperatureCruise(),
						_speedListTAS.get(0)
						)
				);
		_descentAngles.add(
				Amount.valueOf(
						Math.asin(
								_rateOfDescent.doubleValue(SI.METERS_PER_SECOND)
								/_speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND)
								), 
						SI.RADIAN
						)
				.to(NonSI.DEGREE_ANGLE)
				);
		horizontalSpeedListTAS.add(
				Amount.valueOf(
						_speedListTAS.get(0).times(
								Math.cos(_descentAngles.get(0).doubleValue(SI.RADIAN))
								).getEstimatedValue(),
						SI.METERS_PER_SECOND
						)
				);
		_descentTimes.add(
				Amount.valueOf(
						(_descentAltitudes.get(0).doubleValue(SI.METER)
								- _descentAltitudes.get(1).doubleValue(SI.METER))
						*1/_rateOfDescent.doubleValue(SI.METERS_PER_SECOND),
						SI.SECOND
						)
				.to(NonSI.MINUTE)
				);
		_descentLengths.add(
						Amount.valueOf(
								horizontalSpeedListTAS.get(0)
								.times(_descentTimes.get(0).to(SI.SECOND))
								.getEstimatedValue(),
								SI.METER
								)
						.to(NonSI.NAUTICAL_MILE)
				);
		aircraftMassPerStep.add(_initialDescentMass);
		_cLSteps.add(
				LiftCalc.calculateLiftCoeff(
						Math.cos(_descentAngles.get(0).doubleValue(SI.RADIAN))*
						aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
						_speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND),
						_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
						_initialDescentAltitude.doubleValue(SI.METER)
						)
				);
		cDSteps.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(_polarCLClean),
						MyArrayUtils.convertToDoublePrimitive(_polarCDClean),
						_cLSteps.get(0))
				);
		efficiencyPerStep.add(
				_cLSteps.get(0)
				/cDSteps.get(0)
				);
		_dragPerStep.add(
				Amount.valueOf(
						DragCalc.calculateDragAtSpeed(
								aircraftMassPerStep.get(0).times(AtmosphereCalc.g0).getEstimatedValue(),
								_descentAltitudes.get(0).doubleValue(SI.METER),
								_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
								_speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND),
								cDSteps.get(0)
								),
						SI.NEWTON
						)
				);
		
		//---------------------------------------------------------------------------------------
		// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
		_cruiseThrustFromDatabase.add(
				Amount.valueOf(
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								1.0, // phi
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.CRUISE,
								_theAircraft.getPowerPlant(),
								_descentAltitudes.get(0).doubleValue(SI.METER), 
								machList.get(0)
								),
						SI.NEWTON
						)
				);
		
		_flightIdleThrustFromDatabase.add(
				Amount.valueOf(
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
								_theAircraft.getPowerPlant().getEngineNumber(),
								1.0, // phi
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.DESCENT,
								_theAircraft.getPowerPlant(),
								_descentAltitudes.get(0).doubleValue(SI.METER), 
								machList.get(0)
								),
						SI.NEWTON
						)
				);
		
		// first guess values
		weightCruise.add(0.5);
		weightFlightIdle.add(0.5);
		
		interpolatedThrustList.add( 
				(_cruiseThrustFromDatabase.get(0).times(weightCruise.get(0)))
				.plus(_flightIdleThrustFromDatabase.get(0).times(weightFlightIdle.get(0)))
				);
		
		_rateOfDescentList.add(
				Amount.valueOf(
						(interpolatedThrustList.get(0).to(SI.NEWTON)
						.minus(_dragPerStep.get(0).to(SI.NEWTON)))
						.times(_speedListTAS.get(0).to(SI.METERS_PER_SECOND))
						.divide(aircraftMassPerStep.get(0).to(SI.KILOGRAM)
								.times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND))
								.getEstimatedValue()
								)
						.getEstimatedValue(),
						SI.METERS_PER_SECOND
						)
				);
		
		int iter=0;
		// iterative loop for the definition of the cruise and flight idle weights
		while (
				(Math.abs(
						(-_rateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)
								-_rateOfDescentList.get(0).doubleValue(MyUnits.FOOT_PER_MINUTE))
						) 
						/ _rateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)
						)
				> 0.05
				) {
			
			if(iter > maxIterationNumber) {
				if (getDescentMaxIterationErrorFlag() == false ) {
				setDescentMaxIterationErrorFlag(true);
				break;
				}
			}
			
			double rateOfDescentRatio = Math.abs(
					_rateOfDescentList.get(0).doubleValue(SI.METERS_PER_SECOND)/
					(-_rateOfDescent.doubleValue(SI.METERS_PER_SECOND))
					);
			
			if (rateOfDescentRatio < 1) {
				double weightCruiseTemp = weightCruise.get(0);
				weightCruise.remove(0);
				weightCruise.add(weightCruiseTemp*rateOfDescentRatio);

				weightFlightIdle.remove(0);
				weightFlightIdle.add(1-weightCruise.get(0));
			}
			else {
				double weightFlightIdleTemp = weightFlightIdle.get(0);
				weightFlightIdle.remove(0);
				weightFlightIdle.add(weightFlightIdleTemp*(1/rateOfDescentRatio));

				weightCruise.remove(0);
				weightCruise.add(1-weightFlightIdle.get(0));
			}
			
			interpolatedThrustList.remove(0);
			interpolatedThrustList.add( 
					(_cruiseThrustFromDatabase.get(0).times(weightCruise.get(0)))
					.plus(_flightIdleThrustFromDatabase.get(0).times(weightFlightIdle.get(0)))
					);
			
			_rateOfDescentList.remove(0);
			_rateOfDescentList.add(
					Amount.valueOf(
							(interpolatedThrustList.get(0).to(SI.NEWTON)
							.minus(_dragPerStep.get(0).to(SI.NEWTON)))
							.times(_speedListTAS.get(0).to(SI.METERS_PER_SECOND))
							.divide(aircraftMassPerStep.get(0).to(SI.KILOGRAM)
									.times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND))
									.getEstimatedValue()
									)
							.getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			
			iter++;
		}
		
		_thrustPerStep.add(interpolatedThrustList.get(0));
		
		_fuelFlowCruiseList.add(
				_cruiseThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*EngineDatabaseManager_old.getSFC(
						machList.get(0),
						_descentAltitudes.get(0).doubleValue(SI.METER),
						EngineDatabaseManager_old.getThrustRatio(
								machList.get(0),
								_descentAltitudes.get(0).doubleValue(SI.METER),
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
		
		_fuelFlowFlightIdleList.add(
				_flightIdleThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*EngineDatabaseManager_old.getSFC(
						machList.get(0),
						_descentAltitudes.get(0).doubleValue(SI.METER),
						EngineDatabaseManager_old.getThrustRatio(
								machList.get(0),
								_descentAltitudes.get(0).doubleValue(SI.METER),
								_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
								_theAircraft.getPowerPlant().getEngineType(),
								EngineOperatingConditionEnum.DESCENT,
								_theAircraft.getPowerPlant()
								),
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineType(),
						EngineOperatingConditionEnum.DESCENT,
						_theAircraft.getPowerPlant()
						)
				);
		
		_interpolatedFuelFlowList.add(
				(_fuelFlowCruiseList.get(0)*weightCruise.get(0))
				+ (_fuelFlowFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		
		//---------------------------------------------------------------------------------------
		_fuelUsedPerStep.add(
				Amount.valueOf(
						_interpolatedFuelFlowList.get(0)
						*_descentTimes.get(0).doubleValue(NonSI.MINUTE),
						SI.KILOGRAM
						)
				);
		
		for(int i=1; i<_descentAltitudes.size()-1; i++) {
			sigmaList.add(OperatingConditions.getAtmosphere(
					_descentAltitudes.get(i).doubleValue(SI.METER))
					.getDensity()*1000
					/1.225
					);
			_speedListTAS.add(
					_speedDescentCAS.to(SI.METERS_PER_SECOND)
					.divide(Math.sqrt(sigmaList.get(i))));
			machList.add(
					SpeedCalc.calculateMach(
							_descentAltitudes.get(i).doubleValue(SI.METER),
							_speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
							)
					);
			_descentAngles.add(
					Amount.valueOf(
							Math.asin(
									_rateOfDescent.doubleValue(SI.METERS_PER_SECOND)
									/_speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
									), 
							SI.RADIAN
							)
					.to(NonSI.DEGREE_ANGLE)
					);
			horizontalSpeedListTAS.add(
					Amount.valueOf(
							_speedListTAS.get(i).times(
									Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))
									).getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			_descentTimes.add(
					Amount.valueOf(
							(_descentAltitudes.get(i-1).doubleValue(SI.METER) 
									- _descentAltitudes.get(i).doubleValue(SI.METER))
							*1/_rateOfDescent.doubleValue(SI.METERS_PER_SECOND),
							SI.SECOND
							)
					.to(NonSI.MINUTE)
					);
			_descentLengths.add(
							Amount.valueOf(
									horizontalSpeedListTAS.get(i)
									.times(_descentTimes.get(i).to(SI.SECOND))
									.getEstimatedValue(),
									SI.METER
									)
							.to(NonSI.NAUTICAL_MILE)
					);
			aircraftMassPerStep.add(
					aircraftMassPerStep.get(i-1)
					.minus(Amount.valueOf(
							_fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
							SI.KILOGRAM)
							)
					);
			_cLSteps.add(
					LiftCalc.calculateLiftCoeff(
							Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))*
							aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
								*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							_speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND),
							_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
							_descentAltitudes.get(i).doubleValue(SI.METER)
							)
					);
			cDSteps.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(_polarCLClean),
							MyArrayUtils.convertToDoublePrimitive(_polarCDClean),
							_cLSteps.get(i))
					);
			efficiencyPerStep.add(
					_cLSteps.get(i)
					/cDSteps.get(i)
					);
			_dragPerStep.add(
					Amount.valueOf(
							DragCalc.calculateDragAtSpeed(
									aircraftMassPerStep.get(i).times(AtmosphereCalc.g0).getEstimatedValue(),
									_descentAltitudes.get(i).doubleValue(SI.METER),
									_theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
									_speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									cDSteps.get(i)
									),
							SI.NEWTON
							)
					);
			
			//---------------------------------------------------------------------------------------
			// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
			_cruiseThrustFromDatabase.add(
					i,
					Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									1.0, // phi
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getPowerPlant().getEngineType(),
									EngineOperatingConditionEnum.CRUISE,
									_theAircraft.getPowerPlant(),
									_descentAltitudes.get(i).doubleValue(SI.METER), 
									machList.get(i)
									),
							SI.NEWTON
							)
					);
			
			_flightIdleThrustFromDatabase.add(
					i,
					Amount.valueOf(
							ThrustCalc.calculateThrustDatabase(
									_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
									_theAircraft.getPowerPlant().getEngineNumber(),
									1.0, // phi
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getPowerPlant().getEngineType(),
									EngineOperatingConditionEnum.DESCENT,
									_theAircraft.getPowerPlant(),
									_descentAltitudes.get(i).doubleValue(SI.METER), 
									machList.get(i)
									),
							SI.NEWTON
							)
					);
			
			// first guess values
			weightCruise.add(i, 0.5);
			weightFlightIdle.add(i, 0.5);
			
			interpolatedThrustList.add( 
					i,
					(_cruiseThrustFromDatabase.get(i).times(weightCruise.get(i)))
					.plus(_flightIdleThrustFromDatabase.get(i).times(weightFlightIdle.get(i)))
					);
			
			_rateOfDescentList.add(
					i,
					Amount.valueOf(
							(interpolatedThrustList.get(i).to(SI.NEWTON)
							.minus(_dragPerStep.get(i).to(SI.NEWTON)))
							.times(_speedListTAS.get(i).to(SI.METERS_PER_SECOND))
							.divide(aircraftMassPerStep.get(i).to(SI.KILOGRAM)
									.times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND))
									.getEstimatedValue()
									)
							.getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			
			iter=0;
			// iterative loop for the definition of the cruise and flight idle weights
			while (
					(Math.abs(
							(-_rateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)
									-_rateOfDescentList.get(i).doubleValue(MyUnits.FOOT_PER_MINUTE))
							) 
							/ _rateOfDescent.doubleValue(MyUnits.FOOT_PER_MINUTE)
							)
					> 0.05
					) {
				
				if(iter > maxIterationNumber) {
					if (getDescentMaxIterationErrorFlag() == false) {
					setDescentMaxIterationErrorFlag(true);
					}
					break;
				}

				double rateOfDescentRatio = Math.abs(
						_rateOfDescentList.get(i).doubleValue(SI.METERS_PER_SECOND)/
						(-_rateOfDescent.doubleValue(SI.METERS_PER_SECOND))
						);

				if (rateOfDescentRatio < 1) {
					double weightCruiseTemp = weightCruise.get(i);
					weightCruise.remove(i);
					weightCruise.add(i, weightCruiseTemp*rateOfDescentRatio);

					weightFlightIdle.remove(i);
					weightFlightIdle.add(i, 1-weightCruise.get(i));
				}
				else {
					double weightFlightIdleTemp = weightFlightIdle.get(i);
					weightFlightIdle.remove(i);
					weightFlightIdle.add(weightFlightIdleTemp*(1/rateOfDescentRatio));

					weightCruise.remove(i);
					weightCruise.add(1-weightFlightIdle.get(i));
				}

				interpolatedThrustList.remove(i);
				interpolatedThrustList.add(
						i,
						(_cruiseThrustFromDatabase.get(i).times(weightCruise.get(i)))
						.plus(_flightIdleThrustFromDatabase.get(i).times(weightFlightIdle.get(i)))
						);

				_rateOfDescentList.remove(i);
				_rateOfDescentList.add(
						i,
						Amount.valueOf(
								(interpolatedThrustList.get(i).to(SI.NEWTON)
								.minus(_dragPerStep.get(i).to(SI.NEWTON)))
								.times(_speedListTAS.get(i).to(SI.METERS_PER_SECOND))
								.divide(aircraftMassPerStep.get(i).to(SI.KILOGRAM)
										.times(AtmosphereCalc.g0.to(SI.METERS_PER_SQUARE_SECOND))
										.getEstimatedValue()
										)
								.getEstimatedValue(),
								SI.METERS_PER_SECOND
								)
						);
				iter++;
			}
			
			_thrustPerStep.add(i, interpolatedThrustList.get(i));
			
			_fuelFlowCruiseList.add(
					i,
					_cruiseThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*EngineDatabaseManager_old.getSFC(
							machList.get(i),
							_descentAltitudes.get(i).doubleValue(SI.METER),
							EngineDatabaseManager_old.getThrustRatio(
									machList.get(i),
									_descentAltitudes.get(i).doubleValue(SI.METER),
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
			
			_fuelFlowFlightIdleList.add(
					i,
					_flightIdleThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*EngineDatabaseManager_old.getSFC(
							machList.get(i),
							_descentAltitudes.get(i).doubleValue(SI.METER),
							EngineDatabaseManager_old.getThrustRatio(
									machList.get(i),
									_descentAltitudes.get(i).doubleValue(SI.METER),
									_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
									_theAircraft.getPowerPlant().getEngineType(),
									EngineOperatingConditionEnum.DESCENT,
									_theAircraft.getPowerPlant()
									),
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
							_theAircraft.getPowerPlant().getEngineType(),
							EngineOperatingConditionEnum.DESCENT,
							_theAircraft.getPowerPlant()
							)
					);
			
			_interpolatedFuelFlowList.add(
					(_fuelFlowCruiseList.get(i)*weightCruise.get(i))
					+ (_fuelFlowFlightIdleList.get(i)*weightFlightIdle.get(i))
					);
			
			//---------------------------------------------------------------------------------------
			
			_fuelUsedPerStep.add(
					Amount.valueOf(
							_interpolatedFuelFlowList.get(i)
							*_descentTimes.get(i).doubleValue(NonSI.MINUTE),
							SI.KILOGRAM
							)
					);
		}
		
		_totalDescentLength = 
				Amount.valueOf(
						_descentLengths.stream()
						.mapToDouble( f -> f.doubleValue(NonSI.NAUTICAL_MILE))
						.sum(),
						NonSI.NAUTICAL_MILE
						); 
		
		_totalDescentTime = 
				Amount.valueOf(
						_descentTimes.stream()
						.mapToDouble( f -> f.doubleValue(NonSI.MINUTE))
						.sum(),
						NonSI.MINUTE
						); 
		
		_totalDescentFuelUsed = 
				Amount.valueOf(
						_fuelUsedPerStep.stream()
						.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
						.sum(),
						SI.KILOGRAM
						); 
		
	}
	
	public void plotDescentPerformance(String descentFolderPath) {
		
		List<Amount<Duration>> timePlot = new ArrayList<>();
		timePlot.add(Amount.valueOf(0.0, NonSI.MINUTE));
		for(int i=0; i<_descentTimes.size(); i++) 
			timePlot.add(
					timePlot.get(i).to(NonSI.MINUTE)
					.plus(_descentTimes.get(i).to(NonSI.MINUTE))
					);
			
		List<Amount<Length>> rangePlot = new ArrayList<>();
		rangePlot.add(Amount.valueOf(0.0, NonSI.NAUTICAL_MILE));
		for(int i=0; i<_descentLengths.size(); i++) 
			rangePlot.add(
					rangePlot.get(i).to(NonSI.NAUTICAL_MILE)
					.plus(_descentLengths.get(i).to(NonSI.NAUTICAL_MILE))
					);
		
		List<Amount<Mass>> fuelUsedPlot = new ArrayList<>();
		fuelUsedPlot.add(Amount.valueOf(0.0, SI.KILOGRAM));
		for(int i=0; i<_descentLengths.size(); i++) 
			fuelUsedPlot.add(
					fuelUsedPlot.get(i).to(SI.KILOGRAM)
					.plus(_fuelUsedPerStep.get(i).to(SI.KILOGRAM))
					);
		
		//............................................................................................
		// DESCENT TIME
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timePlot),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Altitude",
				"min", "m",
				descentFolderPath, "Descent_Time_SI",true
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(timePlot),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Altitude",
				"min", "ft",
				descentFolderPath, "Descent_Time_IMPERIAL",true
				);
		
		//............................................................................................
		// DESCENT RANGE
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						rangePlot.stream()
						.map(x -> x.to(SI.KILOMETER))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Distance", "Altitude",
				"km", "m",
				descentFolderPath, "Descent_Range_SI",true
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(rangePlot),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Distance", "Altitude",
				"nmi", "ft",
				descentFolderPath, "Descent_Range_IMPERIAL",true
				);
		
		//............................................................................................
		// DESCENT FUEL USED
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(fuelUsedPlot),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Fuel used", "Altitude",
				"kg", "m",
				descentFolderPath, "Descent_Fuel_Used_SI",true
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						fuelUsedPlot.stream()
						.map(x -> x.to(NonSI.POUND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Fuel used", "Altitude",
				"lb", "ft",
				descentFolderPath, "Descent_Fuel_Used_IMPERIAL",true
				);
		
	}

	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	
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

	public Amount<Length> getIntialDescentAltitude() {
		return _initialDescentAltitude;
	}

	public void setIntialDescentAltitude(Amount<Length> _initialDescentAltitude) {
		this._initialDescentAltitude = _initialDescentAltitude;
	}

	public Amount<Length> getEndDescentAltitude() {
		return _endDescentAltitude;
	}

	public void setEndDescentAltitude(Amount<Length> _endDescentAltitude) {
		this._endDescentAltitude = _endDescentAltitude;
	}

	public List<Amount<Length>> getDescentLengths() {
		return _descentLengths;
	}

	public void setDescentLengths(List<Amount<Length>> _descentLengths) {
		this._descentLengths = _descentLengths;
	}

	public List<Amount<Duration>> getDescentTimes() {
		return _descentTimes;
	}

	public void setDescentTimes(List<Amount<Duration>> _descentTimes) {
		this._descentTimes = _descentTimes;
	}

	public List<Amount<Angle>> getDescentAngles() {
		return _descentAngles;
	}

	public void setDescentAngles(List<Amount<Angle>> _descentAngles) {
		this._descentAngles = _descentAngles;
	}

	public Amount<Length> getTotalDescentLength() {
		return _totalDescentLength;
	}

	public void setTotalDescentLength(Amount<Length> _totalDescentLength) {
		this._totalDescentLength = _totalDescentLength;
	}

	public Amount<Duration> getTotalDescentTime() {
		return _totalDescentTime;
	}

	public void setTotalDescentTime(Amount<Duration> _totalDescentTime) {
		this._totalDescentTime = _totalDescentTime;
	}

	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

	public Amount<Mass> getTotalDescentFuelUsed() {
		return _totalDescentFuelUsed;
	}

	public void setTotalDescentFuelUsed(Amount<Mass> _totalDescentFuelUsed) {
		this._totalDescentFuelUsed = _totalDescentFuelUsed;
	}

	public Amount<Mass> getInitialDescentMass() {
		return _initialDescentMass;
	}

	public void setInitialDescentMass(Amount<Mass> _initialDescentMass) {
		this._initialDescentMass = _initialDescentMass;
	}

	public Double[] getPolarCLClean() {
		return _polarCLClean;
	}

	public void setPolarCLClean(Double[] _polarCLClean) {
		this._polarCLClean = _polarCLClean;
	}

	public Double[] getPolarCDClean() {
		return _polarCDClean;
	}

	public void setPolarCDClean(Double[] _polarCDClean) {
		this._polarCDClean = _polarCDClean;
	}
	
	public MyInterpolatingFunction getSFCFunctionDescent() {
		return _sfcFunctionDescent;
	}

	public void setSFCFunctionDescent(MyInterpolatingFunction _sfcFunctionDescent) {
		this._sfcFunctionDescent = _sfcFunctionDescent;
	}

	public List<Amount<Velocity>> getSpeedListTAS() {
		return _speedListTAS;
	}

	public void setSpeedListTAS(List<Amount<Velocity>> _speedListTAS) {
		this._speedListTAS = _speedListTAS;
	}

	public List<Double> getCLSteps() {
		return _cLSteps;
	}

	public void setCLSteps(List<Double> _cLSteps) {
		this._cLSteps = _cLSteps;
	}

	public List<Amount<Force>> getThrustPerStep() {
		return _thrustPerStep;
	}

	public void setThrustPerStep(List<Amount<Force>> _thrustPerStep) {
		this._thrustPerStep = _thrustPerStep;
	}

	public List<Amount<Force>> getDragPerStep() {
		return _dragPerStep;
	}

	public void setDragPerStep(List<Amount<Force>> _dragPerStep) {
		this._dragPerStep = _dragPerStep;
	}

	public List<Amount<Length>> getDescentAltitudes() {
		return _descentAltitudes;
	}

	public void setDescentAltitudes(List<Amount<Length>> _descentAltitudes) {
		this._descentAltitudes = _descentAltitudes;
	}

	public List<Amount<Mass>> getFuelUsedPerStep() {
		return _fuelUsedPerStep;
	}

	public void setFuelUsedPerStep(List<Amount<Mass>> _fuelUsedPerStep) {
		this._fuelUsedPerStep = _fuelUsedPerStep;
	}

	public List<Double> getInterpolatedFuelFlowList() {
		return _interpolatedFuelFlowList;
	}

	public void setInterpolatedFuelFlowList(List<Double> _interpolatedFuelFlowList) {
		this._interpolatedFuelFlowList = _interpolatedFuelFlowList;
	}

	public List<Amount<Force>> getFlightIdleThrustFromDatabase() {
		return _flightIdleThrustFromDatabase;
	}

	public void setFlightIdleThrustFromDatabase(List<Amount<Force>> _flightIdleThrustFromDatabase) {
		this._flightIdleThrustFromDatabase = _flightIdleThrustFromDatabase;
	}

	public List<Amount<Force>> getCruiseThrustFromDatabase() {
		return _cruiseThrustFromDatabase;
	}

	public void setCruiseThrustFromDatabase(List<Amount<Force>> _cruiseThrustFromDatabase) {
		this._cruiseThrustFromDatabase = _cruiseThrustFromDatabase;
	}

	public List<Double> getFuelFlowCruiseList() {
		return _fuelFlowCruiseList;
	}

	public void setFuelFlowCruiseList(List<Double> _fuelFlowCruiseList) {
		this._fuelFlowCruiseList = _fuelFlowCruiseList;
	}

	public List<Double> getFuelFlowFlightIdleList() {
		return _fuelFlowFlightIdleList;
	}

	public void setFuelFlowFlightIdleList(List<Double> _fuelFlowFlightIdleList) {
		this._fuelFlowFlightIdleList = _fuelFlowFlightIdleList;
	}

	public int getMaxIterationNumber() {
		return maxIterationNumber;
	}

	public List<Amount<Velocity>> getRateOfDescentList() {
		return _rateOfDescentList;
	}

	public void setRateOfDescentList(List<Amount<Velocity>> _rateOfDescentList) {
		this._rateOfDescentList = _rateOfDescentList;
	}

	public boolean getDescentMaxIterationErrorFlag() {
		return _descentMaxIterationErrorFlag;
	}

	public void setDescentMaxIterationErrorFlag(boolean _descentMaxIterationErrorFlag) {
		this._descentMaxIterationErrorFlag = _descentMaxIterationErrorFlag;
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}

	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
}
