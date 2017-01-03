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
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
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
			Amount<Length> endDescentAltitude
			) {
		
		this._theAircraft = theAircraft;
		this._speedDescentCAS = speedDescentCAS;
		this._rateOfDescent = rateOfDescent;
		this._initialDescentAltitude = initialDescentAltitude; 
		this._endDescentAltitude = endDescentAltitude;
		
		this._descentLengths = new ArrayList<>();
		this._descentTimes = new ArrayList<>();
		this._descentAngles = new ArrayList<>();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateDescentPerformance() {
		
		_descentLengths = new ArrayList<>();
		_descentTimes = new ArrayList<>();
		_descentAngles = new ArrayList<>();
		
		List<Amount<Velocity>> speedListTAS = new ArrayList<>();
		List<Amount<Velocity>> horizontalSpeedListTAS = new ArrayList<>();
		List<Double> sfcListDescent = new ArrayList<>();
		
		double[] altitudeDescent = MyArrayUtils.linspace(
				_initialDescentAltitude.doubleValue(SI.METER),
				_endDescentAltitude.doubleValue(SI.METER),
				5
				);
		
		double sigma = 0.0;
		
		for(int i=0; i<altitudeDescent.length; i++) {

			sigma = OperatingConditions.getAtmosphere(
					altitudeDescent[i]).getDensity()*1000
					/1.225;
			
			speedListTAS.add(_speedDescentCAS.divide(sigma));
			
			_descentAngles.add(
					Amount.valueOf(
							_rateOfDescent.divide(speedListTAS.get(i)).getEstimatedValue(), 
							SI.RADIAN
							)
					);
			
			horizontalSpeedListTAS.add(
					Amount.valueOf(
							speedListTAS.get(i).times(
									Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))
									).getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			
			sfcListDescent.add(
					ThrustCalc.calculateThrustDatabase(
							_theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON),
							_theAircraft.getPowerPlant().getEngineNumber(),
							1.0,
							_theAircraft.getPowerPlant().getEngineList().get(0).getBPR(),
							_theAircraft.getPowerPlant().getEngineType(),
							EngineOperatingConditionEnum.DESCENT,
							_theAircraft.getPowerPlant(),
							altitudeDescent[i],
							SpeedCalc.calculateMach(
									altitudeDescent[i],
									speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
									)
							)
					*(0.224809)*(0.454/60)
					*EngineDatabaseManager.getSFC(
							SpeedCalc.calculateMach(
									altitudeDescent[i],
									speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
									),
							altitudeDescent[i],
							EngineDatabaseManager.getThrustRatio(
									SpeedCalc.calculateMach(
											altitudeDescent[i],
											speedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
											),
									altitudeDescent[i],
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
		}

		_descentLengths.add(Amount.valueOf(0.0, SI.KILOMETER));
		_descentTimes.add(Amount.valueOf(0.0, NonSI.MINUTE));
		
		for(int i=1; i<altitudeDescent.length; i++) {
			
			_descentTimes.add(
					_descentTimes.get(_descentTimes.size()-1)
					.plus(
							Amount.valueOf(
									(altitudeDescent[i-1] - altitudeDescent[i])
									*1/_rateOfDescent.doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									)
							.to(NonSI.MINUTE)
							)
					);
			
			_descentLengths.add(
					_descentLengths.get(_descentLengths.size()-1)
					.plus(
							Amount.valueOf(
									((horizontalSpeedListTAS.get(i-1).plus(horizontalSpeedListTAS.get(i))).divide(2))
									.times((_descentTimes.get(i).to(SI.SECOND)
											.minus(_descentTimes.get(i-1).to(SI.SECOND))
											))
									.times(
											Math.cos(
													_descentAngles.get(i-1).to(SI.RADIAN).plus(_descentAngles.get(i).to(SI.RADIAN)).divide(2)
													.doubleValue(SI.RADIAN)
													)
											)
									.getEstimatedValue(),
									SI.METER
									)
							.to(SI.KILOMETER)
							)
					);
		}
		
		_totalDescentLength = _descentLengths.get(_descentLengths.size()-1);
		_totalDescentTime = _descentTimes.get(_descentTimes.size()-1);
		
		_totalDescentFuelUsed = 
				Amount.valueOf(
						MyMathUtils.integrate1DSimpsonSpline(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										_descentTimes.stream()
											.map(t -> t.to(NonSI.MINUTE))
												.collect(Collectors.toList()
														)
												),
								MyArrayUtils.convertToDoublePrimitive(sfcListDescent)
								),
						SI.KILOGRAM					
						);
		
	}
	
	public void plotDescentPerformance(String descentFolderPath) {
		
		double[] altitude = MyArrayUtils.linspace(
				_initialDescentAltitude.doubleValue(SI.METER),
				_endDescentAltitude.doubleValue(SI.METER),
				5
				);
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(_descentTimes),
				altitude,
				0.0, null, null, null,
				"Time", "Altitude",
				"min", "m",
				descentFolderPath, "Descent_phase_vs_time_(min)"
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(_descentLengths),
				altitude,
				0.0, null, null, null,
				"Distance", "Altitude",
				"km", "m",
				descentFolderPath, "Descent_phase_vs_distance_(km)"
				);
		
		double[] descentLengthsNauticalMiles = new double[_descentLengths.size()];
		for(int i=0; i<descentLengthsNauticalMiles.length; i++)
			descentLengthsNauticalMiles[i] = _descentLengths.get(i).doubleValue(NonSI.NAUTICAL_MILE);
		MyChartToFileUtils.plotNoLegend(
				descentLengthsNauticalMiles,
				altitude,
				0.0, null, null, null,
				"Distance", "Altitude",
				"nmi", "m",
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
	
}
