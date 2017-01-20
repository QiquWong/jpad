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

import aircraft.components.Aircraft;
import analyses.OperatingConditions;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.EngineOperatingConditionEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

public class DescentCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private Amount<Velocity> _speedDescentCAS;
	private Amount<Velocity> _rateOfDescent;
	private Amount<Length> _initialDescentAltitude;
	private Amount<Length> _endDescentAltitude;
	private Amount<Mass> _initialDescentMass;
	private Double[] _polarCLClean;
	private Double[] _polarCDClean;
	private MyInterpolatingFunction _sfcFunctionDescent;

	//............................................................................................
	// Output:
	private List<Amount<Length>> _descentLengths;
	private List<Amount<Duration>> _descentTimes;
	private List<Amount<Angle>> _descentAngles;
	private Amount<Length> _totalDescentLength;
	private Amount<Duration> _totalDescentTime;
	private Amount<Mass> _totalDescentFuelUsed;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public DescentCalc(
			Aircraft theAircraft,
			Amount<Velocity> speedDescentCAS,
			Amount<Velocity> rateOfDescent,
			Amount<Length> initialDescentAltitude,
			Amount<Length> endDescentAltitude,
			Amount<Mass> initialDescentMass,
			Double[] polarCLClean,
			Double[] polarCDClean
			) {
		
		this._theAircraft = theAircraft;
		this._speedDescentCAS = speedDescentCAS;
		this._rateOfDescent = rateOfDescent;
		this._initialDescentAltitude = initialDescentAltitude; 
		this._endDescentAltitude = endDescentAltitude;
		this._initialDescentMass = initialDescentMass;
		this._polarCLClean = polarCLClean;
		this._polarCDClean = polarCDClean;
		
		this._descentLengths = new ArrayList<>();
		this._descentTimes = new ArrayList<>();
		this._descentAngles = new ArrayList<>();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateDescentPerformance() {
		
		List<Double> sigmaList = new ArrayList<>();
		List<Amount<Velocity>> speedListTAS = new ArrayList<>();
		List<Amount<Velocity>> horizontalSpeedListTAS = new ArrayList<>();
		List<Double> machList = new ArrayList<>();
		List<Amount<Mass>> aircraftMassPerStep = new ArrayList<>();
		List<Double> cLSteps = new ArrayList<>();
		List<Amount<Force>> dragPerStep = new ArrayList<>();
		List<Amount<Force>> thrustPerStep = new ArrayList<>();
		List<Double> sfcIdleInterpolation = new ArrayList<>();
		List<Double> sfcCruiseInterpolation = new ArrayList<>();
		List<Double> thrustIdleInterpolation = new ArrayList<>();
		List<Double> thrustCruiseInterpolation = new ArrayList<>();
		List<Double> interpolatedSFC = new ArrayList<>();
		List<Double> fuelFlows = new ArrayList<>();
		List<Amount<Mass>> fuelUsedPerStep = new ArrayList<>();
		
		double[] altitudeDescent = MyArrayUtils.linspace(
				_initialDescentAltitude.doubleValue(SI.METER),
				_endDescentAltitude.doubleValue(SI.METER),
				5
				);
		
		sigmaList.add(OperatingConditions.getAtmosphere(
				_initialDescentAltitude.doubleValue(SI.METER))
				.getDensity()*1000
				/1.225
				);
		speedListTAS.add(
				_speedDescentCAS.to(SI.METERS_PER_SECOND)
				.divide(Math.sqrt(sigmaList.get(0))));
		machList.add(
				SpeedCalc.calculateMach(
						_initialDescentAltitude.doubleValue(SI.METER),
						speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND)
						)
				);
		_descentAngles.add(
				Amount.valueOf(
						_rateOfDescent.to(SI.METERS_PER_SECOND).divide(speedListTAS.get(0)).getEstimatedValue(), 
						SI.RADIAN
						)
				.to(NonSI.DEGREE_ANGLE)
				);
		horizontalSpeedListTAS.add(
				Amount.valueOf(
						speedListTAS.get(0).times(
								Math.cos(_descentAngles.get(0).doubleValue(SI.RADIAN))
								).getEstimatedValue(),
						SI.METERS_PER_SECOND
						)
				);
		_descentTimes.add(
				Amount.valueOf(
						(altitudeDescent[0] - altitudeDescent[1])
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
		cLSteps.add(
				LiftCalc.calculateLiftCoeff(
						Math.cos(_descentAngles.get(0).doubleValue(SI.RADIAN))*
						aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
							*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
						speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND),
						_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
						_initialDescentAltitude.doubleValue(SI.METER)
						)
				);
		dragPerStep.add(
				Amount.valueOf(
						DragCalc.calculateDragAtSpeed(
								aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								_initialDescentAltitude.doubleValue(SI.METER),
								_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
								speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND),
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertToDoublePrimitive(_polarCLClean),
										MyArrayUtils.convertToDoublePrimitive(_polarCDClean),
										cLSteps.get(0))
								),
						SI.NEWTON
						)
				);
		thrustPerStep.add(
				Amount.valueOf(
						(_rateOfDescent.to(SI.METERS_PER_SECOND)
						.divide(speedListTAS.get(0).to(SI.METERS_PER_SECOND))
						.times(aircraftMassPerStep.get(0).to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue())
						)
						.getEstimatedValue()
						- dragPerStep.get(0).doubleValue(SI.NEWTON),
						SI.NEWTON
						)
				);
		
		//---------------------------------------------------------------------------------------
		// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
		
		sfcIdleInterpolation.add(
				EngineDatabaseManager.getSFC(
						machList.get(0),
						altitudeDescent[0],
						EngineDatabaseManager.getThrustRatio(
								machList.get(0),
								altitudeDescent[0],
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
		thrustIdleInterpolation.add(
				ThrustCalc.calculateThrustDatabase(
						_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_theAircraft.getPowerPlant().getEngineNumber(),
						1.0, // phi
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineType(),
						EngineOperatingConditionEnum.DESCENT, 
						_theAircraft.getPowerPlant(),
						altitudeDescent[0],
						machList.get(0)
						)
				);
		
		sfcCruiseInterpolation.add(
				EngineDatabaseManager.getSFC(
						machList.get(0),
						altitudeDescent[0],
						EngineDatabaseManager.getThrustRatio(
								machList.get(0),
								altitudeDescent[0],
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
		thrustCruiseInterpolation.add(
				ThrustCalc.calculateThrustDatabase(
						_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
						_theAircraft.getPowerPlant().getEngineNumber(),
						1.0, // phi
						_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
						_theAircraft.getPowerPlant().getEngineType(),
						EngineOperatingConditionEnum.CRUISE, 
						_theAircraft.getPowerPlant(),
						altitudeDescent[0],
						machList.get(0)
						)
				);
		
		interpolatedSFC.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] { thrustIdleInterpolation.get(0), thrustCruiseInterpolation.get(0) },
						new double[] { sfcIdleInterpolation.get(0), sfcCruiseInterpolation.get(0) },
						thrustPerStep.get(0).doubleValue(SI.NEWTON)
						)
				);
				
		if(thrustPerStep.get(0).doubleValue(SI.NEWTON)
				*(0.224809)*(0.454/60)
				*interpolatedSFC.get(0) > 0)
			fuelFlows.add(
					thrustPerStep.get(0).doubleValue(SI.NEWTON)
					*(0.224809)*(0.454/60)
					*interpolatedSFC.get(0)
					);
		else
			fuelFlows.add(0.0);
		
		//---------------------------------------------------------------------------------------
		
		fuelUsedPerStep.add(
				Amount.valueOf(
						fuelFlows.get(0)
						*_descentTimes.get(0).doubleValue(NonSI.MINUTE),
						SI.KILOGRAM
						)
				);
		
		for(int i=1; i<altitudeDescent.length; i++) {
			sigmaList.add(OperatingConditions.getAtmosphere(
					altitudeDescent[i])
					.getDensity()*1000
					/1.225
					);
			speedListTAS.add(
					_speedDescentCAS.to(SI.METERS_PER_SECOND)
					.divide(Math.sqrt(sigmaList.get(i))));
			machList.add(
					SpeedCalc.calculateMach(
							altitudeDescent[i],
							speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
							)
					);
			_descentAngles.add(
					Amount.valueOf(
							_rateOfDescent.to(SI.METERS_PER_SECOND).divide(speedListTAS.get(i)).getEstimatedValue(), 
							SI.RADIAN
							)
					.to(NonSI.DEGREE_ANGLE)
					);
			horizontalSpeedListTAS.add(
					Amount.valueOf(
							speedListTAS.get(i).times(
									Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))
									).getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			_descentTimes.add(
					Amount.valueOf(
							(altitudeDescent[i-1] - altitudeDescent[i])
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
							fuelUsedPerStep.stream().mapToDouble(f -> f.doubleValue(SI.KILOGRAM)).sum(),
							SI.KILOGRAM)
							)
					);
			cLSteps.add(
					LiftCalc.calculateLiftCoeff(
							Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))*
							aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
								*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
							speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND),
							_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
							_initialDescentAltitude.doubleValue(SI.METER)
							)
					);
			dragPerStep.add(
					Amount.valueOf(
							DragCalc.calculateDragAtSpeed(
									aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									_initialDescentAltitude.doubleValue(SI.METER),
									_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
									speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND),
									MyMathUtils.getInterpolatedValue1DLinear(
											MyArrayUtils.convertToDoublePrimitive(_polarCLClean),
											MyArrayUtils.convertToDoublePrimitive(_polarCDClean),
											cLSteps.get(i))
									),
							SI.NEWTON
							)
					);
			thrustPerStep.add(
					Amount.valueOf(
							(_rateOfDescent.to(SI.METERS_PER_SECOND)
							.divide(speedListTAS.get(i).to(SI.METERS_PER_SECOND))
							.times(aircraftMassPerStep.get(i).to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue())
							)
							.getEstimatedValue()
							- dragPerStep.get(i).doubleValue(SI.NEWTON),
							SI.NEWTON
							)
					);
			
			//---------------------------------------------------------------------------------------
			// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
			
			sfcIdleInterpolation.add(
					EngineDatabaseManager.getSFC(
							machList.get(i),
							altitudeDescent[i],
							EngineDatabaseManager.getThrustRatio(
									machList.get(0),
									altitudeDescent[0],
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
			thrustIdleInterpolation.add(
					ThrustCalc.calculateThrustDatabase(
							_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_theAircraft.getPowerPlant().getEngineNumber(),
							1.0, // phi
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
							_theAircraft.getPowerPlant().getEngineType(),
							EngineOperatingConditionEnum.DESCENT, 
							_theAircraft.getPowerPlant(),
							altitudeDescent[i],
							machList.get(i)
							)
					);
			
			sfcCruiseInterpolation.add(
					EngineDatabaseManager.getSFC(
							machList.get(i),
							altitudeDescent[i],
							EngineDatabaseManager.getThrustRatio(
									machList.get(i),
									altitudeDescent[i],
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
			thrustCruiseInterpolation.add(
					ThrustCalc.calculateThrustDatabase(
							_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_theAircraft.getPowerPlant().getEngineNumber(),
							1.0, // phi
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
							_theAircraft.getPowerPlant().getEngineType(),
							EngineOperatingConditionEnum.CRUISE, 
							_theAircraft.getPowerPlant(),
							altitudeDescent[i],
							machList.get(i)
							)
					);
			
			interpolatedSFC.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							new double[] { thrustIdleInterpolation.get(i), thrustCruiseInterpolation.get(i) },
							new double[] { sfcIdleInterpolation.get(i), sfcCruiseInterpolation.get(i) },
							thrustPerStep.get(i).doubleValue(SI.NEWTON)
							)
					);
					
			if(thrustPerStep.get(i).doubleValue(SI.NEWTON)
					*(0.224809)*(0.454/60)
					*interpolatedSFC.get(i) > 0)
				fuelFlows.add(
						thrustPerStep.get(i).doubleValue(SI.NEWTON)
						*(0.224809)*(0.454/60)
						*interpolatedSFC.get(i)
						);
			else
				fuelFlows.add(0.0);
			
			//---------------------------------------------------------------------------------------
			
			fuelUsedPerStep.add(
					Amount.valueOf(
							fuelFlows.get(i)
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
						fuelUsedPerStep.stream()
						.mapToDouble( f -> f.doubleValue(SI.KILOGRAM))
						.sum(),
						SI.KILOGRAM
						); 
		
	}
	
	public void plotDescentPerformance(String descentFolderPath) {
		
		double[] altitude = MyArrayUtils.linspace(
				_initialDescentAltitude.doubleValue(NonSI.FOOT),
				
				_endDescentAltitude.doubleValue(NonSI.FOOT),
				5
				);
		
		double[] timePlot = new double[_descentTimes.size()];
		timePlot[0] = 0;
		for(int i=1; i<_descentTimes.size(); i++) 
			timePlot[i] = timePlot[i-1] + _descentTimes.get(i).doubleValue(NonSI.MINUTE);
			
		double[] rangePlot = new double[_descentLengths.size()];
		rangePlot[0] = 0;
		for(int i=1; i<_descentLengths.size(); i++) 
			rangePlot[i] = rangePlot[i-1] + _descentLengths.get(i).doubleValue(NonSI.NAUTICAL_MILE);
		
		MyChartToFileUtils.plotNoLegend(
				timePlot,
				altitude,
				0.0, null, null, null,
				"Time", "Altitude",
				"min", "ft",
				descentFolderPath, "Descent_phase_vs_time_(min)"
				);
		MyChartToFileUtils.plotNoLegend(
				rangePlot,
				altitude,
				0.0, null, null, null,
				"Distance", "Altitude",
				"nmi", "ft",
				descentFolderPath, "Descent_phase_vs_distance_(nmi)"
				);
		
		double[] descentThetaDegree = new double[_descentLengths.size()];
		for(int i=0; i<descentThetaDegree.length; i++)
			descentThetaDegree[i] = _descentAngles.get(i).doubleValue(NonSI.DEGREE_ANGLE);
		MyChartToFileUtils.plotNoLegend(
				descentThetaDegree,
				altitude,
				_descentAngles.get(0).doubleValue(NonSI.DEGREE_ANGLE), null, null, null,
				"Descent angle", "Altitude",
				"deg", "m",
				descentFolderPath, "Descent_angle_vs_distance_(deg)"
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
}
