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
	private List<Double> _emissionIndexNOxCruiseList;
	private List<Double> _emissionIndexNOxFlightIdleList;
	private List<Double> _interpolatedEmissionIndexNOxList;
	private List<Double> _emissionIndexCOCruiseList;
	private List<Double> _emissionIndexCOFlightIdleList;
	private List<Double> _interpolatedEmissionIndexCOList;
	private List<Double> _emissionIndexHCCruiseList;
	private List<Double> _emissionIndexHCFlightIdleList;
	private List<Double> _interpolatedEmissionIndexHCList;
	private List<Double> _emissionIndexSootCruiseList;
	private List<Double> _emissionIndexSootFlightIdleList;
	private List<Double> _interpolatedEmissionIndexSootList;
	private List<Double> _emissionIndexCO2CruiseList;
	private List<Double> _emissionIndexCO2FlightIdleList;
	private List<Double> _interpolatedEmissionIndexCO2List;
	private List<Double> _emissionIndexSOxCruiseList;
	private List<Double> _emissionIndexSOxFlightIdleList;
	private List<Double> _interpolatedEmissionIndexSOxList;
	private List<Double> _emissionIndexH2OCruiseList;
	private List<Double> _emissionIndexH2OFlightIdleList;
	private List<Double> _interpolatedEmissionIndexH2OList;
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
		this._emissionIndexNOxCruiseList = new ArrayList<>();
		this._emissionIndexNOxFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexNOxList = new ArrayList<>();
		this._emissionIndexCOCruiseList = new ArrayList<>();
		this._emissionIndexCOFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexCOList = new ArrayList<>();
		this._emissionIndexHCCruiseList = new ArrayList<>();
		this._emissionIndexHCFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexHCList = new ArrayList<>();
		this._emissionIndexSootCruiseList = new ArrayList<>();
		this._emissionIndexSootFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexSootList = new ArrayList<>();
		this._emissionIndexCO2CruiseList = new ArrayList<>();
		this._emissionIndexCO2FlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexCO2List = new ArrayList<>();
		this._emissionIndexSOxCruiseList = new ArrayList<>();
		this._emissionIndexSOxFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexSOxList = new ArrayList<>();
		this._emissionIndexH2OCruiseList = new ArrayList<>();
		this._emissionIndexH2OFlightIdleList = new ArrayList<>();
		this._interpolatedEmissionIndexH2OList = new ArrayList<>();
		
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
		_fuelUsedPerStep.add(Amount.valueOf(0.0, SI.KILOGRAM));
		_emissionNOxPerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionCOPerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionHCPerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionSootPerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionCO2PerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionSOxPerStep.add(Amount.valueOf(0.0, SI.GRAM));
		_emissionH2OPerStep.add(Amount.valueOf(0.0, SI.GRAM));
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
		// INTERPOLATION BETWEEN CRUISE AND IDLE:
		List<Amount<Force>> cruiseThrustDatabaseTemp = new ArrayList<>();
		List<Amount<Force>> flightIdleThrustDatabaseTemp = new ArrayList<>();
		List<Double> sfcCruiseList = new ArrayList<>();
		List<Double> sfcFlightIdleList = new ArrayList<>();
		List<Double> cruiseEmissionNOxIndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionCOIndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionHCIndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionSootIndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionCO2IndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionSOxIndexListTemp = new ArrayList<>();
		List<Double> cruiseEmissionH2OIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionNOxIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionCOIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionHCIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionSootIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionCO2IndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionSOxIndexListTemp = new ArrayList<>();
		List<Double> flightIdleEmissionH2OIndexListTemp = new ArrayList<>();
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
			cruiseEmissionNOxIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexNOx
							)
					);
			flightIdleEmissionNOxIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexNOx
							)
					);
			cruiseEmissionCOIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexCO
							)
					);
			flightIdleEmissionCOIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexCO
							)
					);
			cruiseEmissionHCIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexHC
							)
					);
			flightIdleEmissionHCIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexHC
							)
					);
			cruiseEmissionSootIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexSoot
							)
					);
			flightIdleEmissionSootIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexSoot
							)
					);
			cruiseEmissionCO2IndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexCO2
							)
					);
			flightIdleEmissionCO2IndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexCO2
							)
					);
			cruiseEmissionSOxIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexSOx
							)
					);
			flightIdleEmissionSOxIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexSOx
							)
					);
			cruiseEmissionH2OIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE,
							_cruiseCalibrationFactorEmissionIndexH2O
							)
					);
			flightIdleEmissionH2OIndexListTemp.add(
					_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
							SpeedCalc.calculateMach(
									_descentAltitudes.get(0),
									_theOperatingConditions.getDeltaTemperatureCruise(),
									_speedListTAS.get(0)
									),
							_descentAltitudes.get(0),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theOperatingConditions.getThrottleCruise(),
							EngineOperatingConditionEnum.FIDL,
							_fidlCalibrationFactorEmissionIndexH2O
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
		_sfcCruiseList.add(sfcCruiseList.stream().mapToDouble(s -> s).average().getAsDouble());
		_sfcFlightIdleList.add(sfcFlightIdleList.stream().mapToDouble(s -> s).average().getAsDouble());
		_fuelFlowCruiseList.add(
				_cruiseThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*_sfcCruiseList.get(0)
				);
		
		_fuelFlowFlightIdleList.add(
				_flightIdleThrustFromDatabase.get(0).doubleValue(SI.NEWTON)
				*0.454
				*0.224809
				/60
				*_sfcFlightIdleList.get(0)
				);
		_emissionIndexNOxCruiseList.add(cruiseEmissionNOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexNOxFlightIdleList.add(flightIdleEmissionNOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexCOCruiseList.add(cruiseEmissionCOIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexCOFlightIdleList.add(flightIdleEmissionCOIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexHCCruiseList.add(cruiseEmissionHCIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexHCFlightIdleList.add(flightIdleEmissionHCIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexSootCruiseList.add(cruiseEmissionSootIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexSootFlightIdleList.add(flightIdleEmissionSootIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexCO2CruiseList.add(cruiseEmissionCO2IndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexCO2FlightIdleList.add(flightIdleEmissionCO2IndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexSOxCruiseList.add(cruiseEmissionSOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexSOxFlightIdleList.add(flightIdleEmissionSOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexH2OCruiseList.add(cruiseEmissionH2OIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		_emissionIndexH2OFlightIdleList.add(flightIdleEmissionH2OIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
		
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
		_throttlePerStep.add(_thrustPerStep.get(0).doubleValue(SI.NEWTON) / _theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON));
		_interpolatedSFCList.add(
				(_sfcCruiseList.get(0)*weightCruise.get(0))
				+ (_sfcFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedFuelFlowList.add(
				(_fuelFlowCruiseList.get(0)*weightCruise.get(0))
				+ (_fuelFlowFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexNOxList.add(
				(_emissionIndexNOxCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexNOxFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexCOList.add(
				(_emissionIndexCOCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexCOFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexHCList.add(
				(_emissionIndexHCCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexHCFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexSootList.add(
				(_emissionIndexSootCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexSootFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexCO2List.add(
				(_emissionIndexCO2CruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexCO2FlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexSOxList.add(
				(_emissionIndexSOxCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexSOxFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		_interpolatedEmissionIndexH2OList.add(
				(_emissionIndexH2OCruiseList.get(0)*weightCruise.get(0))
				+ (_emissionIndexH2OFlightIdleList.get(0)*weightFlightIdle.get(0))
				);
		
		/* Step by step calculation */
		for(int i=1; i<_descentAltitudes.size()-1; i++) {
			sigmaList.add(
					OperatingConditions.getAtmosphere(
							_descentAltitudes.get(i).doubleValue(SI.METER),
							_theOperatingConditions.getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
							).getDensityRatio()
					);
			_speedListTAS.add(
					_speedDescentCAS.to(SI.METERS_PER_SECOND)
					.divide(Math.sqrt(sigmaList.get(i))));
			_speedListCAS.add(_speedDescentCAS.to(SI.METERS_PER_SECOND));
			_machList.add(
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
					_descentTimes.get(_descentTimes.size()-1)
					.plus(
							Amount.valueOf(
									(_descentAltitudes.get(i-1).doubleValue(SI.METER) 
											- _descentAltitudes.get(i).doubleValue(SI.METER))
									*1/_rateOfDescent.doubleValue(SI.METERS_PER_SECOND),
									SI.SECOND
									)
							)
					.to(NonSI.MINUTE)
					);
			_descentLengths.add(
					_descentLengths.get(_descentLengths.size()-1)
					.plus(
							Amount.valueOf(
									( (horizontalSpeedListTAS.get(i).doubleValue(SI.METERS_PER_SECOND)
											+ horizontalSpeedListTAS.get(i-1).doubleValue(SI.METERS_PER_SECOND)) / 2 )
									*(_descentTimes.get(i).doubleValue(SI.SECOND) - _descentTimes.get(i).doubleValue(SI.SECOND)),
									SI.METER
									)
							.to(NonSI.NAUTICAL_MILE)
							)
					);
			
			/* ------ First guess values assuming an airaft mass equal to the previous step -------*/
			_aircraftMassPerStep.add(_aircraftMassPerStep.get(i-1));
			_cLSteps.add(
					LiftCalc.calculateLiftCoeff(
							Amount.valueOf(
									Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))
									*_aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
									*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									SI.NEWTON
									),
							_speedListTAS.get(i),
							_theAircraft.getWing().getSurfacePlanform(),
							_descentAltitudes.get(i),
							_theOperatingConditions.getDeltaTemperatureCruise()
							)
					);
			_cDSteps.add(
					MyMathUtils.getInterpolatedValue1DLinear(
							_polarCLClean,
							_polarCDClean,
							_cLSteps.get(i))
					+ DragCalc.calculateCDWaveLockKorn(
							_cLSteps.get(i), 
							_machList.get(i), 
							AerodynamicCalc.calculateMachCriticalKornMason(
									_cLSteps.get(i), 
									_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
									meanAirfoil.getThicknessToChordRatio(), 
									meanAirfoil.getType()
									)
							)
					);
			_efficiencyPerStep.add(
					_cLSteps.get(i)
					/_cDSteps.get(i)
					);
			_dragPerStep.add(
					DragCalc.calculateDragAtSpeed(
							_descentAltitudes.get(i),
							_theOperatingConditions.getDeltaTemperatureCruise(),
							_theAircraft.getWing().getSurfacePlanform(),
							_speedListTAS.get(i),
							_cDSteps.get(i)
							)
					);
			/* ------------------------------------------------------------------------------------ */
			
			//---------------------------------------------------------------------------------------
			// INTERPOLATION BETWEEN CRUISE AND IDLE:
			cruiseThrustDatabaseTemp = new ArrayList<>();
			flightIdleThrustDatabaseTemp = new ArrayList<>();
			sfcCruiseList = new ArrayList<>();
			sfcFlightIdleList = new ArrayList<>();
			cruiseEmissionNOxIndexListTemp = new ArrayList<>();
			cruiseEmissionCOIndexListTemp = new ArrayList<>();
			cruiseEmissionHCIndexListTemp = new ArrayList<>();
			cruiseEmissionSootIndexListTemp = new ArrayList<>();
			cruiseEmissionCO2IndexListTemp = new ArrayList<>();
			cruiseEmissionSOxIndexListTemp = new ArrayList<>();
			cruiseEmissionH2OIndexListTemp = new ArrayList<>();
			flightIdleEmissionNOxIndexListTemp = new ArrayList<>();
			flightIdleEmissionCOIndexListTemp = new ArrayList<>();
			flightIdleEmissionHCIndexListTemp = new ArrayList<>();
			flightIdleEmissionSootIndexListTemp = new ArrayList<>();
			flightIdleEmissionCO2IndexListTemp = new ArrayList<>();
			flightIdleEmissionSOxIndexListTemp = new ArrayList<>();
			flightIdleEmissionH2OIndexListTemp = new ArrayList<>();
			
			for(int ieng=0; ieng<_theAircraft.getPowerPlant().getEngineNumber(); ieng++) {

				cruiseThrustDatabaseTemp.add(
						ThrustCalc.calculateThrustDatabase(
								_theAircraft.getPowerPlant().getEngineList().get(ieng).getT0(), 
								_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng),
								EngineOperatingConditionEnum.CRUISE, 
								_descentAltitudes.get(i), 
								_machList.get(i), 
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
								_machList.get(i), 
								_theOperatingConditions.getDeltaTemperatureCruise(), 
								_theOperatingConditions.getThrottleCruise(),
								_fidlThrustCorrectionFactor
								)
						);
				sfcCruiseList.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
								_machList.get(i),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseSfcCorrectionFactor
								)
						);
				sfcFlightIdleList.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSfc(
								_machList.get(i),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlSfcCorrectionFactor
								)
						);
				cruiseEmissionNOxIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexNOx
								)
						);
				flightIdleEmissionNOxIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getNOxEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexNOx
								)
						);
				cruiseEmissionCOIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexCO
								)
						);
				flightIdleEmissionCOIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCOEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexCO
								)
						);
				cruiseEmissionHCIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexHC
								)
						);
				flightIdleEmissionHCIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getHCEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexHC
								)
						);
				cruiseEmissionSootIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexSoot
								)
						);
				flightIdleEmissionSootIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSootEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexSoot
								)
						);
				cruiseEmissionCO2IndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexCO2
								)
						);
				flightIdleEmissionCO2IndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getCO2EmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexCO2
								)
						);
				cruiseEmissionSOxIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexSOx
								)
						);
				flightIdleEmissionSOxIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getSOxEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexSOx
								)
						);
				cruiseEmissionH2OIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.CRUISE,
								_cruiseCalibrationFactorEmissionIndexH2O
								)
						);
				flightIdleEmissionH2OIndexListTemp.add(
						_theAircraft.getPowerPlant().getEngineDatabaseReaderList().get(ieng).getH2OEmissionIndex(
								SpeedCalc.calculateMach(
										_descentAltitudes.get(i),
										_theOperatingConditions.getDeltaTemperatureCruise(),
										_speedListTAS.get(i)
										),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theOperatingConditions.getThrottleCruise(),
								EngineOperatingConditionEnum.FIDL,
								_fidlCalibrationFactorEmissionIndexH2O
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
			_sfcCruiseList.add(i, sfcCruiseList.stream().mapToDouble(s -> s).average().getAsDouble());
			_sfcFlightIdleList.add(i, sfcFlightIdleList.stream().mapToDouble(s -> s).average().getAsDouble());
			_fuelFlowCruiseList.add(
					i,
					_cruiseThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*sfcCruiseList.get(i)
					);
			
			_fuelFlowFlightIdleList.add(
					i,
					_flightIdleThrustFromDatabase.get(i).doubleValue(SI.NEWTON)
					*0.454
					*0.224809
					/60
					*sfcFlightIdleList.get(i)
					);
			_emissionIndexNOxCruiseList.add(i, cruiseEmissionNOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexNOxFlightIdleList.add(i, flightIdleEmissionNOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexCOCruiseList.add(i, cruiseEmissionCOIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexCOFlightIdleList.add(i, flightIdleEmissionCOIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexHCCruiseList.add(i, cruiseEmissionHCIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexHCFlightIdleList.add(i, flightIdleEmissionHCIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexSootCruiseList.add(i, cruiseEmissionSootIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexSootFlightIdleList.add(i, flightIdleEmissionSootIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexCO2CruiseList.add(i, cruiseEmissionCO2IndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexCO2FlightIdleList.add(i, flightIdleEmissionCO2IndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexSOxCruiseList.add(i, cruiseEmissionSOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexSOxFlightIdleList.add(i, flightIdleEmissionSOxIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexH2OCruiseList.add(i, cruiseEmissionH2OIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			_emissionIndexH2OFlightIdleList.add(i, flightIdleEmissionH2OIndexListTemp.stream().mapToDouble(e -> e).average().getAsDouble());
			
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
							((interpolatedThrustList.get(i).to(SI.NEWTON)
							.minus(_dragPerStep.get(i).to(SI.NEWTON)))
							.times(_speedListTAS.get(i).to(SI.METERS_PER_SECOND)))
							.divide(_aircraftMassPerStep.get(i).to(SI.KILOGRAM)
									.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
									)
							.getEstimatedValue(),
							SI.METERS_PER_SECOND
							)
					);
			
			iter=0;
			/* 
			 * iterative loop for the definition of the cruise and flight idle weights. This updates also
			 * the fuel used, aircraft mass, cL, cD, efficiency, dragPerStep and thrustPerStep
			 */
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
					if (_descentMaxIterationErrorFlag == false) {
					_descentMaxIterationErrorFlag = true;
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

				if(iter > 0) { /* Not added yet at the first iter, to be replaced only from iter=1 on */
					_thrustPerStep.remove(i);
					_throttlePerStep.remove(i);
					_interpolatedSFCList.remove(i);
					_interpolatedFuelFlowList.remove(i);
					_interpolatedEmissionIndexNOxList.remove(i);
					_interpolatedEmissionIndexCOList.remove(i);
					_interpolatedEmissionIndexHCList.remove(i);
					_interpolatedEmissionIndexSootList.remove(i);
					_interpolatedEmissionIndexCO2List.remove(i);
					_interpolatedEmissionIndexSOxList.remove(i);
					_interpolatedEmissionIndexH2OList.remove(i);
					_fuelUsedPerStep.remove(i);
					_aircraftMassPerStep.remove(i);
					_cLSteps.remove(i);
					_cDSteps.remove(i);
					_efficiencyPerStep.remove(i);
					_dragPerStep.remove(i);
				}
				
				_thrustPerStep.add(i, interpolatedThrustList.get(i));
				_throttlePerStep.add(i, _thrustPerStep.get(i).doubleValue(SI.NEWTON) / _theAircraft.getPowerPlant().getT0Total().doubleValue(SI.NEWTON));
				_interpolatedSFCList.add(
						(_sfcCruiseList.get(i)*weightCruise.get(i))
						+ (_sfcFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedFuelFlowList.add(
						i,
						(_fuelFlowCruiseList.get(i)*weightCruise.get(i))
						+ (_fuelFlowFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexNOxList.add(
						i,
						(_emissionIndexNOxCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexNOxFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexCOList.add(
						i,
						(_emissionIndexCOCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexCOFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexHCList.add(
						i,
						(_emissionIndexHCCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexHCFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexSootList.add(
						i,
						(_emissionIndexSootCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexSootFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexCO2List.add(
						i,
						(_emissionIndexCO2CruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexCO2FlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexSOxList.add(
						i,
						(_emissionIndexSOxCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexSOxFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_interpolatedEmissionIndexH2OList.add(
						i,
						(_emissionIndexH2OCruiseList.get(i)*weightCruise.get(i))
						+ (_emissionIndexH2OFlightIdleList.get(i)*weightFlightIdle.get(i))
						);
				_fuelUsedPerStep.add(
						i,
						_fuelUsedPerStep.get(i-1)
						.plus(
								Amount.valueOf(
										((_interpolatedFuelFlowList.get(i) + _interpolatedFuelFlowList.get(i-1)) /2)
										*(_descentTimes.get(i).doubleValue(NonSI.MINUTE) - _descentTimes.get(i-1).doubleValue(NonSI.MINUTE)),
										SI.KILOGRAM
										)
								)
						);
				_emissionNOxPerStep.add(
						i,
						_emissionNOxPerStep.get(_emissionNOxPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexNOxList.get(i) + _interpolatedEmissionIndexNOxList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionCOPerStep.add(
						i,
						_emissionCOPerStep.get(_emissionCOPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexCOList.get(i) + _interpolatedEmissionIndexCOList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionHCPerStep.add(
						i,
						_emissionHCPerStep.get(_emissionHCPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexHCList.get(i) + _interpolatedEmissionIndexHCList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionSootPerStep.add(
						i,
						_emissionSootPerStep.get(_emissionSootPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexSootList.get(i) + _interpolatedEmissionIndexSootList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionCO2PerStep.add(
						i,
						_emissionCO2PerStep.get(_emissionCO2PerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexCO2List.get(i) + _interpolatedEmissionIndexCO2List.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionSOxPerStep.add(
						i,
						_emissionSOxPerStep.get(_emissionSOxPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexSOxList.get(i) + _interpolatedEmissionIndexSOxList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_emissionH2OPerStep.add(
						i,
						_emissionH2OPerStep.get(_emissionH2OPerStep.size()-1)
						.plus(
								Amount.valueOf(
										((_interpolatedEmissionIndexH2OList.get(i) + _interpolatedEmissionIndexH2OList.get(i-1)) /2)
										*(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
										SI.GRAM
										)
								)
						);
				_aircraftMassPerStep.add(
						i,
						_aircraftMassPerStep.get(i-1)
						.minus(Amount.valueOf(
								(_fuelUsedPerStep.get(i).doubleValue(SI.KILOGRAM) - _fuelUsedPerStep.get(i-1).doubleValue(SI.KILOGRAM)),
								SI.KILOGRAM)
								)
						);
				_cLSteps.add(
						i,
						LiftCalc.calculateLiftCoeff(
								Amount.valueOf(
										Math.cos(_descentAngles.get(i).doubleValue(SI.RADIAN))
										*_aircraftMassPerStep.get(i).doubleValue(SI.KILOGRAM)
										*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
										SI.NEWTON
										),
								_speedListTAS.get(i),
								_theAircraft.getWing().getSurfacePlanform(),
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise()
								)
						);
				_cDSteps.add(
						i,
						MyMathUtils.getInterpolatedValue1DLinear(
								_polarCLClean,
								_polarCDClean,
								_cLSteps.get(i))
						+ DragCalc.calculateCDWaveLockKorn(
								_cLSteps.get(i), 
								_machList.get(i), 
								AerodynamicCalc.calculateMachCriticalKornMason(
										_cLSteps.get(i), 
										_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										meanAirfoil.getThicknessToChordRatio(), 
										meanAirfoil.getType()
										)
								)
						);
				_efficiencyPerStep.add(
						i,
						_cLSteps.get(i)
						/_cDSteps.get(i)
						);
				_dragPerStep.add(
						i,
						DragCalc.calculateDragAtSpeed(
								_descentAltitudes.get(i),
								_theOperatingConditions.getDeltaTemperatureCruise(),
								_theAircraft.getWing().getSurfacePlanform(),
								_speedListTAS.get(i),
								_cDSteps.get(i)
								)
						);
				
				_rateOfDescentList.remove(i);
				_rateOfDescentList.add(
						i,
						Amount.valueOf(
								(interpolatedThrustList.get(i).to(SI.NEWTON)
								.minus(_dragPerStep.get(i).to(SI.NEWTON)))
								.times(_speedListTAS.get(i).to(SI.METERS_PER_SECOND))
								.divide(_aircraftMassPerStep.get(i).to(SI.KILOGRAM)
										.times(AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND))
										)
								.getEstimatedValue(),
								SI.METERS_PER_SECOND
								)
						);
				
				iter++;
			}
			
		}
		
		_totalDescentLength = _descentLengths.get(_descentLengths.size()-1).to(NonSI.NAUTICAL_MILE); 
		_totalDescentTime = _descentTimes.get(_descentTimes.size()-1).to(NonSI.MINUTE);
		_totalDescentFuelUsed = _fuelUsedPerStep.get(_fuelUsedPerStep.size()).to(SI.KILOGRAM);
		_totalDescentNOxEmissions = _emissionNOxPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentCOEmissions = _emissionCOPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentHCEmissions = _emissionHCPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentSootEmissions = _emissionSootPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentCO2Emissions = _emissionCO2PerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentSOxEmissions = _emissionSOxPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		_totalDescentH2OEmissions = _emissionH2OPerStep.get(_emissionNOxPerStep.size()).to(SI.KILOGRAM);
		
	}
	
	public void plotDescentPerformance(String descentFolderPath) {
		
		//............................................................................................
		// DESCENT TIME
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentTimes.stream()
						.map(t -> t.to(SI.SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Altitude",
				"s", "m",
				descentFolderPath, "Descent_Time_SI",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentTimes.stream()
						.map(t -> t.to(SI.SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Time", "Altitude",
				"s", "ft",
				descentFolderPath, "Descent_Time_IMPERIAL",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		//............................................................................................
		// DESCENT RANGE
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentLengths.stream()
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
				descentFolderPath, "Descent_Range_SI",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentLengths.stream()
						.map(x -> x.to(NonSI.NAUTICAL_MILE))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Distance", "Altitude",
				"nmi", "ft",
				descentFolderPath, "Descent_Range_IMPERIAL",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		//............................................................................................
		// DESCENT FUEL USED
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_fuelUsedPerStep.stream()
						.map(f -> f.to(SI.KILOGRAM))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Fuel used", "Altitude",
				"kg", "m",
				descentFolderPath, "Descent_Fuel_Used_SI",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_fuelUsedPerStep.stream()
						.map(f -> f.to(NonSI.POUND))
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
				descentFolderPath, "Descent_Fuel_Used_IMPERIAL",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		
		//............................................................................................
		// DESCENT AIRCRAFT MASS
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_aircraftMassPerStep.stream()
						.map(f -> f.to(SI.KILOGRAM))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(SI.METER))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Aircraft Mass", "Altitude",
				"kg", "m",
				descentFolderPath, "Descent_Aircraft_Mass_SI",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_aircraftMassPerStep.stream()
						.map(f -> f.to(NonSI.POUND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentAltitudes.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Aircraft Mass", "Altitude",
				"lb", "ft",
				descentFolderPath, "Descent_Aircraft_Mass_IMPERIAL",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
		//............................................................................................
		// DESCENT THROTTLE 
		MyChartToFileUtils.plotNoLegend(
				_throttlePerStep.stream().mapToDouble(t -> t).toArray(),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						_descentTimes.stream()
						.map(x -> x.to(SI.SECOND))
						.collect(Collectors.toList())
						),
				0.0, null, 0.0, null,
				"Throttle setting", "Time",
				"", "s",
				descentFolderPath, "Descent_Throttle_Setting",
				_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
				);
	
		//............................................................................................
		// DESCENT THROTTLE 
		List<Double[]> thrust_SI = new ArrayList<Double[]>();
		List<Double[]> thrust_Imperial = new ArrayList<Double[]>();
		List<Double[]> time = new ArrayList<Double[]>();
		List<String> legend = new ArrayList<>();

		thrust_SI.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_thrustPerStep.stream()
						.map(x -> x.to(SI.NEWTON))
						.collect(Collectors.toList())
						)
				);
		thrust_SI.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_cruiseThrustFromDatabase.stream()
						.map(x -> x.to(SI.NEWTON))
						.collect(Collectors.toList())
						)
				);
		thrust_SI.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_flightIdleThrustFromDatabase.stream()
						.map(x -> x.to(SI.NEWTON))
						.collect(Collectors.toList())
						)
				);
		thrust_Imperial.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_thrustPerStep.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						)
				);
		thrust_Imperial.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_cruiseThrustFromDatabase.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						)
				);
		thrust_Imperial.add(
				MyArrayUtils.convertListOfAmountToDoubleArray(
						_flightIdleThrustFromDatabase.stream()
						.map(x -> x.to(NonSI.POUND_FORCE))
						.collect(Collectors.toList())
						)
				);
		legend.add("Interpolated Thrust");
		legend.add("Max Cruise Thrust");
		legend.add("Flight Idle Thrust");
		for(int i=0; i<thrust_SI.size(); i++) 
			time.add(
					MyArrayUtils.convertListOfAmountToDoubleArray(
							_descentTimes.stream()
							.map(x -> x.to(SI.SECOND))
							.collect(Collectors.toList())
							)
					);
			
		try {
			MyChartToFileUtils.plot(
					time, thrust_SI,
					"Thrust Interpolation during descent",
					"Time", "Thrust",
					null, null, null, null,
					"s", "N",
					true, legend,
					descentFolderPath, "Thrust_Interpolation_Descent_SI",
					_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
			
			MyChartToFileUtils.plot(
					time, thrust_Imperial,
					"Thrust Interpolation during descent",
					"Time", "Thrust",
					null, null, null, null,
					"s", "lbf",
					true, legend,
					descentFolderPath, "Thrust_Interpolation_Descent_IMPERIAL",
					_theAircraft.getTheAnalysisManager().getCreateCSVPerformance()
					);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
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

	public List<Double> getEmissionIndexNOxCruiseList() {
		return _emissionIndexNOxCruiseList;
	}

	public void setEmissionIndexNOxCruiseList(List<Double> _emissionIndexNOxCruiseList) {
		this._emissionIndexNOxCruiseList = _emissionIndexNOxCruiseList;
	}

	public List<Double> getEmissionIndexNOxFlightIdleList() {
		return _emissionIndexNOxFlightIdleList;
	}

	public void setEmissionIndexNOxFlightIdleList(List<Double> _emissionIndexNOxFlightIdleList) {
		this._emissionIndexNOxFlightIdleList = _emissionIndexNOxFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexNOxList() {
		return _interpolatedEmissionIndexNOxList;
	}

	public void setInterpolatedEmissionIndexNOxList(List<Double> _interpolatedEmissionIndexNOxList) {
		this._interpolatedEmissionIndexNOxList = _interpolatedEmissionIndexNOxList;
	}

	public List<Double> getEmissionIndexCOCruiseList() {
		return _emissionIndexCOCruiseList;
	}

	public void setEmissionIndexCOCruiseList(List<Double> _emissionIndexCOCruiseList) {
		this._emissionIndexCOCruiseList = _emissionIndexCOCruiseList;
	}

	public List<Double> getEmissionIndexCOFlightIdleList() {
		return _emissionIndexCOFlightIdleList;
	}

	public void setEmissionIndexCOFlightIdleList(List<Double> _emissionIndexCOFlightIdleList) {
		this._emissionIndexCOFlightIdleList = _emissionIndexCOFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexCOList() {
		return _interpolatedEmissionIndexCOList;
	}

	public void setInterpolatedEmissionIndexCOList(List<Double> _interpolatedEmissionIndexCOList) {
		this._interpolatedEmissionIndexCOList = _interpolatedEmissionIndexCOList;
	}

	public List<Double> getEmissionIndexHCCruiseList() {
		return _emissionIndexHCCruiseList;
	}

	public void setEmissionIndexHCCruiseList(List<Double> _emissionIndexHCCruiseList) {
		this._emissionIndexHCCruiseList = _emissionIndexHCCruiseList;
	}

	public List<Double> getEmissionIndexHCFlightIdleList() {
		return _emissionIndexHCFlightIdleList;
	}

	public void setEmissionIndexHCFlightIdleList(List<Double> _emissionIndexHCFlightIdleList) {
		this._emissionIndexHCFlightIdleList = _emissionIndexHCFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexHCList() {
		return _interpolatedEmissionIndexHCList;
	}

	public void setInterpolatedEmissionIndexHCList(List<Double> _interpolatedEmissionIndexHCList) {
		this._interpolatedEmissionIndexHCList = _interpolatedEmissionIndexHCList;
	}

	public List<Double> getEmissionIndexSootCruiseList() {
		return _emissionIndexSootCruiseList;
	}

	public void setEmissionIndexSootCruiseList(List<Double> _emissionIndexSootCruiseList) {
		this._emissionIndexSootCruiseList = _emissionIndexSootCruiseList;
	}

	public List<Double> getEmissionIndexSootFlightIdleList() {
		return _emissionIndexSootFlightIdleList;
	}

	public void setEmissionIndexSootFlightIdleList(List<Double> _emissionIndexSootFlightIdleList) {
		this._emissionIndexSootFlightIdleList = _emissionIndexSootFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexSootList() {
		return _interpolatedEmissionIndexSootList;
	}

	public void setInterpolatedEmissionIndexSootList(List<Double> _interpolatedEmissionIndexSootList) {
		this._interpolatedEmissionIndexSootList = _interpolatedEmissionIndexSootList;
	}

	public List<Double> getEmissionIndexCO2CruiseList() {
		return _emissionIndexCO2CruiseList;
	}

	public void setEmissionIndexCO2CruiseList(List<Double> _emissionIndexCO2CruiseList) {
		this._emissionIndexCO2CruiseList = _emissionIndexCO2CruiseList;
	}

	public List<Double> getEmissionIndexCO2FlightIdleList() {
		return _emissionIndexCO2FlightIdleList;
	}

	public void setEmissionIndexCO2FlightIdleList(List<Double> _emissionIndexCO2FlightIdleList) {
		this._emissionIndexCO2FlightIdleList = _emissionIndexCO2FlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexCO2List() {
		return _interpolatedEmissionIndexCO2List;
	}

	public void setInterpolatedEmissionIndexCO2List(List<Double> _interpolatedEmissionIndexCO2List) {
		this._interpolatedEmissionIndexCO2List = _interpolatedEmissionIndexCO2List;
	}

	public List<Double> getEmissionIndexSOxCruiseList() {
		return _emissionIndexSOxCruiseList;
	}

	public void setEmissionIndexSOxCruiseList(List<Double> _emissionIndexSOxCruiseList) {
		this._emissionIndexSOxCruiseList = _emissionIndexSOxCruiseList;
	}

	public List<Double> getEmissionIndexSOxFlightIdleList() {
		return _emissionIndexSOxFlightIdleList;
	}

	public void setEmissionIndexSOxFlightIdleList(List<Double> _emissionIndexSOxFlightIdleList) {
		this._emissionIndexSOxFlightIdleList = _emissionIndexSOxFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexSOxList() {
		return _interpolatedEmissionIndexSOxList;
	}

	public void setInterpolatedEmissionIndexSOxList(List<Double> _interpolatedEmissionIndexSOxList) {
		this._interpolatedEmissionIndexSOxList = _interpolatedEmissionIndexSOxList;
	}

	public List<Double> getEmissionIndexH2OCruiseList() {
		return _emissionIndexH2OCruiseList;
	}

	public void setEmissionIndexH2OCruiseList(List<Double> _emissionIndexH2OCruiseList) {
		this._emissionIndexH2OCruiseList = _emissionIndexH2OCruiseList;
	}

	public List<Double> getEmissionIndexH2OFlightIdleList() {
		return _emissionIndexH2OFlightIdleList;
	}

	public void setEmissionIndexH2OFlightIdleList(List<Double> _emissionIndexH2OFlightIdleList) {
		this._emissionIndexH2OFlightIdleList = _emissionIndexH2OFlightIdleList;
	}

	public List<Double> getInterpolatedEmissionIndexH2OList() {
		return _interpolatedEmissionIndexH2OList;
	}

	public void setInterpolatedEmissionIndexH2OList(List<Double> _interpolatedEmissionIndexH2OList) {
		this._interpolatedEmissionIndexH2OList = _interpolatedEmissionIndexH2OList;
	}
}
