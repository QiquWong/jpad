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
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
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
	private double[] _polarCLClean;
	private double[] _polarCDClean;
	private MyInterpolatingFunction _sfcFunctionDescent;
	private double _cruiseThrustCorrectionFactor, _cruiseSfcCorrectionFactor, _fidlThrustCorrectionFactor, _fidlSfcCorrectionFactor,
	_cruiseCalibrationFactorEmissionIndexNOx, _cruiseCalibrationFactorEmissionIndexCO, _cruiseCalibrationFactorEmissionIndexHC, 
	_cruiseCalibrationFactorEmissionIndexSoot, _cruiseCalibrationFactorEmissionIndexCO2, _cruiseCalibrationFactorEmissionIndexSOx, 
	_cruiseCalibrationFactorEmissionIndexH2O, _fidlCalibrationFactorEmissionIndexNOx, _fidlCalibrationFactorEmissionIndexCO, 
	_fidlCalibrationFactorEmissionIndexHC, _fidlCalibrationFactorEmissionIndexSoot, _fidlCalibrationFactorEmissionIndexCO2, 
	_fidlCalibrationFactorEmissionIndexSOx, _fidlCalibrationFactorEmissionIndexH2O;

	private final int maxIterationNumber = 50;
	
	//............................................................................................
	// Output:
	private List<Amount<Length>> _descentAltitudes;
	private List<Amount<Length>> _descentLengths;
	private List<Amount<Duration>> _descentTimes;
	private List<Amount<Velocity>> _speedListTAS;
	private List<Amount<Velocity>> _speedListCAS;
	private List<Double> _machList;
	private List<Amount<Velocity>> _rateOfDescentList;
	private List<Amount<Angle>> _descentAngles;
	private List<Double> _cLSteps;
	private List<Double> _cDSteps;
	private List<Double> _efficiencyPerStep;		
	private List<Amount<Force>> _cruiseThrustFromDatabase;
	private List<Amount<Force>> _flightIdleThrustFromDatabase;
	private List<Amount<Force>> _thrustPerStep;
	private List<Double> _throttlePerStep;
	private List<Amount<Force>> _dragPerStep;
	private List<Double> _fuelFlowCruiseList;
	private List<Double> _fuelFlowFlightIdleList;
	private List<Double> _interpolatedFuelFlowList;
	private List<Double> _sfcCruiseList;
	private List<Double> _sfcFlightIdleList;
	private List<Double> _interpolatedSFCList;
	private List<Amount<Mass>> _fuelUsedPerStep;
	private List<Amount<Mass>> _emissionNOxPerStep;
	private List<Amount<Mass>> _emissionCOPerStep;
	private List<Amount<Mass>> _emissionHCPerStep;
	private List<Amount<Mass>> _emissionSootPerStep;
	private List<Amount<Mass>> _emissionCO2PerStep;
	private List<Amount<Mass>> _emissionSOxPerStep;
	private List<Amount<Mass>> _emissionH2OPerStep;
	private List<Amount<Mass>> _aircraftMassPerStep;
	private Amount<Length> _totalDescentLength;
	private Amount<Duration> _totalDescentTime;
	private Amount<Mass> _totalDescentFuelUsed;
	private Amount<Mass> _totalDescentNOxEmissions;
	private Amount<Mass> _totalDescentCOEmissions;
	private Amount<Mass> _totalDescentHCEmissions;
	private Amount<Mass> _totalDescentSootEmissions;
	private Amount<Mass> _totalDescentCO2Emissions;
	private Amount<Mass> _totalDescentSOxEmissions;
	private Amount<Mass> _totalDescentH2OEmissions;
	
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
			double[] polarCLClean,
			double[] polarCDClean,
			double cruiseThrustCorrectionFactor,
			double cruiseSfcCorrectionFactor,
			double fidlThrustCorrectionFactor,
			double fidlSfcCorrectionFactor,
			double cruiseCalibrationFactorEmissionIndexNOx, 
			double cruiseCalibrationFactorEmissionIndexCO, 
			double cruiseCalibrationFactorEmissionIndexHC, 
			double cruiseCalibrationFactorEmissionIndexSoot, 
			double cruiseCalibrationFactorEmissionIndexCO2, 
			double cruiseCalibrationFactorEmissionIndexSOx, 
			double cruiseCalibrationFactorEmissionIndexH2O, 
			double fidlCalibrationFactorEmissionIndexNOx, 
			double fidlCalibrationFactorEmissionIndexCO, 
			double fidlCalibrationFactorEmissionIndexHC, 
			double fidlCalibrationFactorEmissionIndexSoot, 
			double fidlCalibrationFactorEmissionIndexCO2, 
			double fidlCalibrationFactorEmissionIndexSOx, 
			double fidlCalibrationFactorEmissionIndexH2O
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
		this._cruiseThrustCorrectionFactor = cruiseThrustCorrectionFactor;
		this._fidlThrustCorrectionFactor = fidlThrustCorrectionFactor;
		this._cruiseSfcCorrectionFactor = cruiseSfcCorrectionFactor;
		this._fidlSfcCorrectionFactor = fidlSfcCorrectionFactor;
		this._cruiseCalibrationFactorEmissionIndexNOx = cruiseCalibrationFactorEmissionIndexNOx;
		this._cruiseCalibrationFactorEmissionIndexCO = cruiseCalibrationFactorEmissionIndexCO; 
		this._cruiseCalibrationFactorEmissionIndexHC = cruiseCalibrationFactorEmissionIndexHC;
		this._cruiseCalibrationFactorEmissionIndexSoot = cruiseCalibrationFactorEmissionIndexSoot; 
		this._cruiseCalibrationFactorEmissionIndexCO2 = cruiseCalibrationFactorEmissionIndexCO2; 
		this._cruiseCalibrationFactorEmissionIndexSOx = cruiseCalibrationFactorEmissionIndexSOx; 
		this._cruiseCalibrationFactorEmissionIndexH2O = cruiseCalibrationFactorEmissionIndexH2O; 
		this._fidlCalibrationFactorEmissionIndexNOx = fidlCalibrationFactorEmissionIndexNOx; 
		this._fidlCalibrationFactorEmissionIndexCO = fidlCalibrationFactorEmissionIndexCO; 
		this._fidlCalibrationFactorEmissionIndexHC = fidlCalibrationFactorEmissionIndexHC; 
		this._fidlCalibrationFactorEmissionIndexSoot = fidlCalibrationFactorEmissionIndexSoot; 
		this._fidlCalibrationFactorEmissionIndexCO2 = fidlCalibrationFactorEmissionIndexCO2; 
		this._fidlCalibrationFactorEmissionIndexSOx = fidlCalibrationFactorEmissionIndexSOx; 
		this._fidlCalibrationFactorEmissionIndexH2O = fidlCalibrationFactorEmissionIndexH2O;
		
		this._descentAltitudes = new ArrayList<>();
		this._descentLengths = new ArrayList<>();
		this._descentTimes = new ArrayList<>();
		this._speedListTAS = new ArrayList<>();
		this._speedListCAS = new ArrayList<>();
		this._machList = new ArrayList<>();
		this._rateOfDescentList = new ArrayList<>();
		this._descentAngles = new ArrayList<>();
		this._cLSteps = new ArrayList<>();
		this._cDSteps = new ArrayList<>();
		this._efficiencyPerStep = new ArrayList<>();		
		this._cruiseThrustFromDatabase = new ArrayList<>();
		this._flightIdleThrustFromDatabase = new ArrayList<>();
		this._thrustPerStep = new ArrayList<>();
		this._throttlePerStep = new ArrayList<>();
		this._dragPerStep = new ArrayList<>();
		this._fuelFlowCruiseList = new ArrayList<>();
		this._fuelFlowFlightIdleList = new ArrayList<>();
		this._interpolatedFuelFlowList = new ArrayList<>();
		this._sfcCruiseList = new ArrayList<>();
		this._sfcFlightIdleList = new ArrayList<>();
		this._interpolatedSFCList = new ArrayList<>();
		this._fuelUsedPerStep = new ArrayList<>();
		this._emissionNOxPerStep = new ArrayList<>();
		this._emissionCOPerStep = new ArrayList<>();
		this._emissionHCPerStep = new ArrayList<>();
		this._emissionSootPerStep = new ArrayList<>();
		this._emissionCO2PerStep = new ArrayList<>();
		this._emissionSOxPerStep = new ArrayList<>();
		this._emissionH2OPerStep = new ArrayList<>();
		this._aircraftMassPerStep = new ArrayList<>();
		
		this._descentMaxIterationErrorFlag = false;
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateDescentPerformance() {
		
		List<Double> sigmaList = new ArrayList<>();
		List<Amount<Velocity>> horizontalSpeedListTAS = new ArrayList<>();
		List<Amount<Force>> interpolatedThrustList = new ArrayList<>();
		List<Double> weightCruise = new ArrayList<>();
		List<Double> weightFlightIdle = new ArrayList<>();
		
		Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_theAircraft.getWing());
		
		_descentAltitudes =
				MyArrayUtils.convertDoubleArrayToListOfAmount(
						MyArrayUtils.linspace(
								_initialDescentAltitude.doubleValue(SI.METER),
								_endDescentAltitude.doubleValue(SI.METER),
								10
								),
						SI.METER
						);
		
		/* Initialization of the first step */
		sigmaList.add(
				OperatingConditions.getAtmosphere(
						_initialDescentAltitude.doubleValue(SI.METER),
						_theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
						).getDensityRatio()
				);
		_speedListTAS.add(
				_speedDescentCAS.to(SI.METERS_PER_SECOND)
				.divide(Math.sqrt(sigmaList.get(0))));
		_speedListCAS.add(_speedDescentCAS.to(SI.METERS_PER_SECOND));
		_machList.add(
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
		_descentTimes.add(Amount.valueOf(0.0, SI.SECOND));
		_descentLengths.add(Amount.valueOf(0.0, SI.METER));
		_aircraftMassPerStep.add(_initialDescentMass);
		_cLSteps.add(
				LiftCalc.calculateLiftCoeff(
						Amount.valueOf(
								Math.cos(
										_descentAngles.get(0).doubleValue(SI.RADIAN))
								*_aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
								*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
								SI.NEWTON
								),
						_speedListTAS.get(0),
						_theAircraft.getWing().getSurfacePlanform(),
						_initialDescentAltitude, 
						_theOperatingConditions.getDeltaTemperatureCruise()
						)
				);
		_cDSteps.add(
				MyMathUtils.getInterpolatedValue1DLinear(
						_polarCLClean,
						_polarCDClean,
						_cLSteps.get(0))
				+ DragCalc.calculateCDWaveLockKorn(
						_cLSteps.get(0), 
						_machList.get(0), 
						AerodynamicCalc.calculateMachCriticalKornMason(
								_cLSteps.get(0), 
								_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(), 
								meanAirfoil.getType()
								)
						)
				);
		_efficiencyPerStep.add(_cLSteps.get(0)/_cDSteps.get(0));
		_dragPerStep.add(
				DragCalc.calculateDragAtSpeed(
						_descentAltitudes.get(0), 
						_theOperatingConditions.getDeltaTemperatureCruise(),
						_theAircraft.getWing().getSurfacePlanform(), 
						_speedListTAS.get(0), 
						_cDSteps.get(0)
						)
				);
		
		//---------------------------------------------------------------------------------------
		// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
		List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
		List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
		for(int ieng=0; ieng<_theAircraft.getPowerPlant().getEngineNumber(); ieng++) {
			cruiseThrustDatabaseTemp.add(
					ThrustCalc.calculateThrustDatabase(
							_theAircraft.getPowerPlant().getEngineList().get(ieng).getT0(),
							_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng),
							EngineOperatingConditionEnum.CRUISE, 
							_descentAltitudes.get(0), 
							_machList.get(0), 
							_theOperatingConditions.getDeltaTemperatureCruise(), 
							_theOperatingConditions.getThrottleCruise(),
							_cruiseThrustCorrectionFactor
							)
					);

			flightIdleThrustDatabaseTemp.add(
					ThrustCalc.calculateThrustDatabase(
							_theAircraft.getPowerPlant().getEngineList().get(ieng).getT0(), 
							_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng),
							EngineOperatingConditionEnum.FIDL, 
							_descentAltitudes.get(0), 
							_machList.get(0), 
							_theOperatingConditions.getDeltaTemperatureCruise(), 
							_theOperatingConditions.getThrottleCruise(),
							_fidlThrustCorrectionFactor
							)
					);
		}

		_cruiseThrustFromDatabase.add(
				Amount.valueOf(
						cruiseThrustDatabaseTemp.stream().mapToDouble(cthr -> cthr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						)
				);
		_flightIdleThrustFromDatabase.add(
				Amount.valueOf(
						flightIdleThrustDatabaseTemp.stream().mapToDouble(fthr -> fthr.doubleValue(SI.NEWTON)).sum(),
						SI.NEWTON
						)
				);
		
		// first guess values
		weightCruise.add(0.5);
		weightFlightIdle.add(0.5);

		interpolatedThrustList.add( 
				Amount.valueOf(
						(_cruiseThrustFromDatabase.get(0).doubleValue(SI.NEWTON)*weightCruise.get(0))
						+ (_flightIdleThrustFromDatabase.get(0).doubleValue(SI.NEWTON)*weightFlightIdle.get(0)),
						SI.NEWTON)
				);

		_rateOfDescentList.add(
				Amount.valueOf(
						( (interpolatedThrustList.get(0).doubleValue(SI.NEWTON)
								- _dragPerStep.get(0).doubleValue(SI.NEWTON))
						*_speedListTAS.get(0).doubleValue(SI.METERS_PER_SECOND) )
						/ (_aircraftMassPerStep.get(0).doubleValue(SI.KILOGRAM)
								*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND)
								),
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
				if (_descentMaxIterationErrorFlag == false ) {
				_descentMaxIterationErrorFlag = true;
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
					(_cruiseThrustFromDatabase.get(0).to(SI.NEWTON).times(weightCruise.get(0)))
					.plus(_flightIdleThrustFromDatabase.get(0).to(SI.NEWTON).times(weightFlightIdle.get(0)))
					);
			
			_rateOfDescentList.remove(0);
			_rateOfDescentList.add(
					Amount.valueOf(
							((interpolatedThrustList.get(0).to(SI.NEWTON)
							.minus(_dragPerStep.get(0).to(SI.NEWTON)))
							.times(_speedListTAS.get(0).to(SI.METERS_PER_SECOND)))
							.divide(_aircraftMassPerStep.get(0)
									.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
									.doubleValue(SI.KILOGRAM)
									)
							.getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			
			iter++;
		}
		
		_thrustPerStep.add(interpolatedThrustList.get(0));
		
		List<Double> sfcCruiseList = new ArrayList<>();
		List<Double> sfcFlightIdleList = new ArrayList<>();
		for(int ieng=0; ieng<_theAircraft.getPowerPlant().getEngineNumber(); ieng++) {
			sfcCruiseList.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
							_machList.get(0),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseSfcCorrectionFactor
							)
					);
			sfcFlightIdleList.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
							_machList.get(0),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlSfcCorrectionFactor
							)
					);
		}
		
		double sfcCruise = sfcCruiseList.stream().mapToDouble(s -> s).average().getAsDouble();
		double sfcFlightIdle = sfcFlightIdleList.stream().mapToDouble(s -> s).average().getAsDouble();
		
		_sfcCruiseList.add(sfcCruise);
		_sfcFlightIdleList.add(sfcFlightIdle);
		_interpolatedSFCList.add(
				(_sfcCruiseList.get(0)*weightCruise.get(0))
				+ (_sfcFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		
		_fuelFlowCruiseList.add(
				_cruiseThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*sfcCruise
				);
		
		_fuelFlowFlightIdleList.add(
				_flightIdleThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*sfcFlightIdle
				);
		
		_interpolatedFuelFlowList.add(
				(_fuelFlowCruiseList.get(0)*weightCruise.get(0))
				+ (_fuelFlowFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		
		//---------------------------------------------------------------------------------------
		_fuelUsedPerStep.add(Amount.valueOf(0.0, SI.KILOGRAM));
		
		/* TODO: CONTINUE FROM HERE FOLLOWING ClimbCalc APPROACH (REMEBER TO INSERT EMISSIONS)
		
		/* Step by step calculation */
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
							_descentAltitudes.get(i),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_speedListTAS.get(i)
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
							Amount.valueOf(
									Math.cos(
											_descentAngles.get(i).doubleValue(SI.RADIAN))
									*aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									SI.NEWTON
									),
							_speedListTAS.get(i),
							_theAircraft.getWing().getSurfacePlanform(),
							_descentAltitudes.get(i),
							_theOperatingConditions.getDeltaTemperatureCruise()
							)
					);
			cDSteps.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							_polarCLClean,
							_polarCDClean,
							_cLSteps.get(i))
					);
			efficiencyPerStep.add(
					_cLSteps.get(i)
					/cDSteps.get(i)
					);
			_dragPerStep.add(
					DragCalc.calculateDragAtSpeed(
							_descentAltitudes.get(i),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theAircraft.getWing().getSurfacePlanform(),
							_speedListTAS.get(i),
							cDSteps.get(i)
							)
					);
			
			//---------------------------------------------------------------------------------------
			// SFC INTERPOLATION BETWEEN CRUISE AND IDLE:
			cruiseThrustDatabaseTemp = new ArrayList<>();
			flightIdleThrustDatabaseTemp = new ArrayList<>();
			
			for(int ieng=0; ieng<_theAircraft.getPowerPlant().getEngineNumber(); ieng++) {

				cruiseThrustDatabaseTemp.add(
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(ieng).getT0(), 
								_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng),
								EngineOperatingConditionEnum.CRUISE, 
								_descentAltitudes.get(i), 
								machList.get(i), 
								_theOperatingConditions.getDeltaTemperatureCruise(), 
								_theOperatingConditions.getThrottleCruise(),
								_cruiseThrustCorrectionFactor
								)
						);

				flightIdleThrustDatabaseTemp.add(
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(ieng).getT0(), 
								_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng),
								EngineOperatingConditionEnum.FIDL, 
								_descentAltitudes.get(i), 
								machList.get(i), 
								_theOperatingConditions.getDeltaTemperatureCruise(), 
								_theOperatingConditions.getThrottleCruise(),
								_fidlThrustCorrectionFactor
								)
						);
			}

			_cruiseThrustFromDatabase.add(
					i,
					Amount.valueOf(
							cruiseThrustDatabaseTemp.stream().mapToDouble(cthr -> cthr.doubleValue(SI.NEWTON)).sum(),
							SI.NEWTON
							)
					);
			_flightIdleThrustFromDatabase.add(
					i,
					Amount.valueOf(
							flightIdleThrustDatabaseTemp.stream().mapToDouble(fthr -> fthr.doubleValue(SI.NEWTON)).sum(),
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
			
			sfcCruiseList = new ArrayList<>();
			sfcFlightIdleList = new ArrayList<>();
			for(int ieng=0; ieng<_theAircraft.getPowerPlant().getEngineNumber(); ieng++) {
				sfcCruiseList.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
								machList.get(i),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseSfcCorrectionFactor
								)
						);
				sfcFlightIdleList.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
								machList.get(i),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlSfcCorrectionFactor
								)
						);
			}
			
			sfcCruise = sfcCruiseList.stream().mapToDouble(s -> s).average().getAsDouble();
			sfcFlightIdle = sfcFlightIdleList.stream().mapToDouble(s -> s).average().getAsDouble();
			
			_fuelFlowCruiseList.add(
					i,
					_cruiseThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*sfcCruise
					);
			
			_fuelFlowFlightIdleList.add(
					i,
					_flightIdleThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*sfcFlightIdle
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

	public double[] getPolarCLClean() {
		return _polarCLClean;
	}

	public void setPolarCLClean(double[] _polarCLClean) {
		this._polarCLClean = _polarCLClean;
	}

	public double[] getPolarCDClean() {
		return _polarCDClean;
	}

	public void setPolarCDClean(double[] _polarCDClean) {
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

	public List<Amount<Velocity>> getSpeedListCAS() {
		return _speedListCAS;
	}

	public void setSpeedListCAS(List<Amount<Velocity>> _speedListCAS) {
		this._speedListCAS = _speedListCAS;
	}

	public List<Double> getMachList() {
		return _machList;
	}

	public void setMachList(List<Double> _machList) {
		this._machList = _machList;
	}

	public List<Double> getCLSteps() {
		return _cLSteps;
	}

	public void setCLSteps(List<Double> _cLSteps) {
		this._cLSteps = _cLSteps;
	}

	public List<Double> getCDSteps() {
		return _cDSteps;
	}

	public void setCDSteps(List<Double> _cDSteps) {
		this._cDSteps = _cDSteps;
	}

	public List<Double> getEfficiencyPerStep() {
		return _efficiencyPerStep;
	}

	public void setEfficiencyPerStep(List<Double> _efficiencyPerStep) {
		this._efficiencyPerStep = _efficiencyPerStep;
	}

	public List<Amount<Force>> getThrustPerStep() {
		return _thrustPerStep;
	}

	public void setThrustPerStep(List<Amount<Force>> _thrustPerStep) {
		this._thrustPerStep = _thrustPerStep;
	}

	public List<Double> getThrottlePerStep() {
		return _throttlePerStep;
	}

	public void setThrottlePerStep(List<Double> _throttlePerStep) {
		this._throttlePerStep = _throttlePerStep;
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

	public List<Amount<Mass>> getEmissionNOxPerStep() {
		return _emissionNOxPerStep;
	}

	public void setEmissionNOxPerStep(List<Amount<Mass>> _emissionNOxPerStep) {
		this._emissionNOxPerStep = _emissionNOxPerStep;
	}

	public List<Amount<Mass>> getEmissionCOPerStep() {
		return _emissionCOPerStep;
	}

	public void setEmissionCOPerStep(List<Amount<Mass>> _emissionCOPerStep) {
		this._emissionCOPerStep = _emissionCOPerStep;
	}

	public List<Amount<Mass>> getEmissionHCPerStep() {
		return _emissionHCPerStep;
	}

	public void setEmissionHCPerStep(List<Amount<Mass>> _emissionHCPerStep) {
		this._emissionHCPerStep = _emissionHCPerStep;
	}

	public List<Amount<Mass>> getEmissionSootPerStep() {
		return _emissionSootPerStep;
	}

	public void setEmissionSootPerStep(List<Amount<Mass>> _emissionSootPerStep) {
		this._emissionSootPerStep = _emissionSootPerStep;
	}

	public List<Amount<Mass>> getEmissionCO2PerStep() {
		return _emissionCO2PerStep;
	}

	public void setEmissionCO2PerStep(List<Amount<Mass>> _emissionCO2PerStep) {
		this._emissionCO2PerStep = _emissionCO2PerStep;
	}

	public List<Amount<Mass>> getEmissionSOxPerStep() {
		return _emissionSOxPerStep;
	}

	public void setEmissionSOxPerStep(List<Amount<Mass>> _emissionSOxPerStep) {
		this._emissionSOxPerStep = _emissionSOxPerStep;
	}

	public List<Amount<Mass>> getEmissionH2OPerStep() {
		return _emissionH2OPerStep;
	}

	public void setEmissionH2OPerStep(List<Amount<Mass>> _emissionH2OPerStep) {
		this._emissionH2OPerStep = _emissionH2OPerStep;
	}

	public List<Amount<Mass>> getAircraftMassPerStep() {
		return _aircraftMassPerStep;
	}

	public void setAircraftMassPerStep(List<Amount<Mass>> _aircraftMassPerStep) {
		this._aircraftMassPerStep = _aircraftMassPerStep;
	}

	public List<Double> getInterpolatedFuelFlowList() {
		return _interpolatedFuelFlowList;
	}

	public void setInterpolatedFuelFlowList(List<Double> _interpolatedFuelFlowList) {
		this._interpolatedFuelFlowList = _interpolatedFuelFlowList;
	}

	public List<Double> getInterpolatedSFCList() {
		return _interpolatedSFCList;
	}

	public void setInterpolatedSFCList(List<Double> _interpolatedSFCList) {
		this._interpolatedSFCList = _interpolatedSFCList;
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

	public List<Double> getSfcCruiseList() {
		return _sfcCruiseList;
	}

	public void setSfcCruiseList(List<Double> _sfcCruiseList) {
		this._sfcCruiseList = _sfcCruiseList;
	}

	public List<Double> getFuelFlowFlightIdleList() {
		return _fuelFlowFlightIdleList;
	}

	public void setFuelFlowFlightIdleList(List<Double> _fuelFlowFlightIdleList) {
		this._fuelFlowFlightIdleList = _fuelFlowFlightIdleList;
	}

	public List<Double> getSfcFlightIdleList() {
		return _sfcFlightIdleList;
	}

	public void setSfcFlightIdleList(List<Double> _sfcFlightIdleList) {
		this._sfcFlightIdleList = _sfcFlightIdleList;
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

	public double getCruiseThrustCorrectionFactor() {
		return _cruiseThrustCorrectionFactor;
	}

	public void setCruiseThrustCorrectionFactor(double _cruiseThrustCorrectionFactor) {
		this._cruiseThrustCorrectionFactor = _cruiseThrustCorrectionFactor;
	}

	public double getCruiseSfcCorrectionFactor() {
		return _cruiseSfcCorrectionFactor;
	}

	public void setCruiseSfcCorrectionFactor(double _cruiseSfcCorrectionFactor) {
		this._cruiseSfcCorrectionFactor = _cruiseSfcCorrectionFactor;
	}

	public double getFidlThrustCorrectionFactor() {
		return _fidlThrustCorrectionFactor;
	}

	public void setFidlThrustCorrectionFactor(double _fidlThrustCorrectionFactor) {
		this._fidlThrustCorrectionFactor = _fidlThrustCorrectionFactor;
	}

	public double getFidlSfcCorrectionFactor() {
		return _fidlSfcCorrectionFactor;
	}

	public void setFidlSfcCorrectionFactor(double _fidlSfcCorrectionFactor) {
		this._fidlSfcCorrectionFactor = _fidlSfcCorrectionFactor;
	}

	public double getCruiseCalibrationFactorEmissionIndexNOx() {
		return _cruiseCalibrationFactorEmissionIndexNOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexNOx(double _cruiseCalibrationFactorEmissionIndexNOx) {
		this._cruiseCalibrationFactorEmissionIndexNOx = _cruiseCalibrationFactorEmissionIndexNOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO() {
		return _cruiseCalibrationFactorEmissionIndexCO;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO(double _cruiseCalibrationFactorEmissionIndexCO) {
		this._cruiseCalibrationFactorEmissionIndexCO = _cruiseCalibrationFactorEmissionIndexCO;
	}

	public double getCruiseCalibrationFactorEmissionIndexHC() {
		return _cruiseCalibrationFactorEmissionIndexHC;
	}

	public void setCruiseCalibrationFactorEmissionIndexHC(double _cruiseCalibrationFactorEmissionIndexHC) {
		this._cruiseCalibrationFactorEmissionIndexHC = _cruiseCalibrationFactorEmissionIndexHC;
	}

	public double getCruiseCalibrationFactorEmissionIndexSoot() {
		return _cruiseCalibrationFactorEmissionIndexSoot;
	}

	public void setCruiseCalibrationFactorEmissionIndexSoot(double _cruiseCalibrationFactorEmissionIndexSoot) {
		this._cruiseCalibrationFactorEmissionIndexSoot = _cruiseCalibrationFactorEmissionIndexSoot;
	}

	public double getCruiseCalibrationFactorEmissionIndexCO2() {
		return _cruiseCalibrationFactorEmissionIndexCO2;
	}

	public void setCruiseCalibrationFactorEmissionIndexCO2(double _cruiseCalibrationFactorEmissionIndexCO2) {
		this._cruiseCalibrationFactorEmissionIndexCO2 = _cruiseCalibrationFactorEmissionIndexCO2;
	}

	public double getCruiseCalibrationFactorEmissionIndexSOx() {
		return _cruiseCalibrationFactorEmissionIndexSOx;
	}

	public void setCruiseCalibrationFactorEmissionIndexSOx(double _cruiseCalibrationFactorEmissionIndexSOx) {
		this._cruiseCalibrationFactorEmissionIndexSOx = _cruiseCalibrationFactorEmissionIndexSOx;
	}

	public double getCruiseCalibrationFactorEmissionIndexH2O() {
		return _cruiseCalibrationFactorEmissionIndexH2O;
	}

	public void setCruiseCalibrationFactorEmissionIndexH2O(double _cruiseCalibrationFactorEmissionIndexH2O) {
		this._cruiseCalibrationFactorEmissionIndexH2O = _cruiseCalibrationFactorEmissionIndexH2O;
	}

	public double getFidlCalibrationFactorEmissionIndexNOx() {
		return _fidlCalibrationFactorEmissionIndexNOx;
	}

	public void setFidlCalibrationFactorEmissionIndexNOx(double _fidlCalibrationFactorEmissionIndexNOx) {
		this._fidlCalibrationFactorEmissionIndexNOx = _fidlCalibrationFactorEmissionIndexNOx;
	}

	public double getFidlCalibrationFactorEmissionIndexCO() {
		return _fidlCalibrationFactorEmissionIndexCO;
	}

	public void setFidlCalibrationFactorEmissionIndexCO(double _fidlCalibrationFactorEmissionIndexCO) {
		this._fidlCalibrationFactorEmissionIndexCO = _fidlCalibrationFactorEmissionIndexCO;
	}

	public double getFidlCalibrationFactorEmissionIndexHC() {
		return _fidlCalibrationFactorEmissionIndexHC;
	}

	public void setFidlCalibrationFactorEmissionIndexHC(double _fidlCalibrationFactorEmissionIndexHC) {
		this._fidlCalibrationFactorEmissionIndexHC = _fidlCalibrationFactorEmissionIndexHC;
	}

	public double getFidlCalibrationFactorEmissionIndexSoot() {
		return _fidlCalibrationFactorEmissionIndexSoot;
	}

	public void setFidlCalibrationFactorEmissionIndexSoot(double _fidlCalibrationFactorEmissionIndexSoot) {
		this._fidlCalibrationFactorEmissionIndexSoot = _fidlCalibrationFactorEmissionIndexSoot;
	}

	public double getFidlCalibrationFactorEmissionIndexCO2() {
		return _fidlCalibrationFactorEmissionIndexCO2;
	}

	public void setFidlCalibrationFactorEmissionIndexCO2(double _fidlCalibrationFactorEmissionIndexCO2) {
		this._fidlCalibrationFactorEmissionIndexCO2 = _fidlCalibrationFactorEmissionIndexCO2;
	}

	public double getFidlCalibrationFactorEmissionIndexSOx() {
		return _fidlCalibrationFactorEmissionIndexSOx;
	}

	public void setFidlCalibrationFactorEmissionIndexSOx(double _fidlCalibrationFactorEmissionIndexSOx) {
		this._fidlCalibrationFactorEmissionIndexSOx = _fidlCalibrationFactorEmissionIndexSOx;
	}

	public double getFidlCalibrationFactorEmissionIndexH2O() {
		return _fidlCalibrationFactorEmissionIndexH2O;
	}

	public void setFidlCalibrationFactorEmissionIndexH2O(double _fidlCalibrationFactorEmissionIndexH2O) {
		this._fidlCalibrationFactorEmissionIndexH2O = _fidlCalibrationFactorEmissionIndexH2O;
	}

	public Amount<Mass> getTotalDescentNOxEmissions() {
		return _totalDescentNOxEmissions;
	}

	public void setTotalDescentNOxEmissions(Amount<Mass> _totalDescentNOxEmissions) {
		this._totalDescentNOxEmissions = _totalDescentNOxEmissions;
	}

	public Amount<Mass> getTotalDescentCOEmissions() {
		return _totalDescentCOEmissions;
	}

	public void setTotalDescentCOEmissions(Amount<Mass> _totalDescentCOEmissions) {
		this._totalDescentCOEmissions = _totalDescentCOEmissions;
	}

	public Amount<Mass> getTotalDescentHCEmissions() {
		return _totalDescentHCEmissions;
	}

	public void setTotalDescentHCEmissions(Amount<Mass> _totalDescentHCEmissions) {
		this._totalDescentHCEmissions = _totalDescentHCEmissions;
	}

	public Amount<Mass> getTotalDescentSootEmissions() {
		return _totalDescentSootEmissions;
	}

	public void setTotalDescentSootEmissions(Amount<Mass> _totalDescentSootEmissions) {
		this._totalDescentSootEmissions = _totalDescentSootEmissions;
	}

	public Amount<Mass> getTotalDescentCO2Emissions() {
		return _totalDescentCO2Emissions;
	}

	public void setTotalDescentCO2Emissions(Amount<Mass> _totalDescentCO2Emissions) {
		this._totalDescentCO2Emissions = _totalDescentCO2Emissions;
	}

	public Amount<Mass> getTotalDescentSOxEmissions() {
		return _totalDescentSOxEmissions;
	}

	public void setTotalDescentSOxEmissions(Amount<Mass> _totalDescentSOxEmissions) {
		this._totalDescentSOxEmissions = _totalDescentSOxEmissions;
	}

	public Amount<Mass> getTotalDescentH2OEmissions() {
		return _totalDescentH2OEmissions;
	}

	public void setTotalDescentH2OEmissions(Amount<Mass> _totalDescentH2OEmissions) {
		this._totalDescentH2OEmissions = _totalDescentH2OEmissions;
	}
}
